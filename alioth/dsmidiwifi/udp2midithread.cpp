// DSMIDIWiFi Server - Forwards MIDI messages between local appliations
// and Nintendo DS handhelds in the local network.
//
// This is part of DSMI - DS Music Interface - http://dsmi.tobw.net
//
// Copyright (c) 2018 mirabilos (tg@debian.org)
// Copyright (C) 2007 Tobias Weyand (me@dsmi.tobw.net)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

#include "udp2midithread.h"

#include <iostream>

Udp2MidiThread::Udp2MidiThread(QObject *parent)
	:QThread(parent)
{
	abort = false;
}

Udp2MidiThread::~Udp2MidiThread()
{
	mutex.lock();
	abort = true;
	mutex.unlock();
	wait();
	freeSeq();
}

bool
Udp2MidiThread::go(Midi2UdpThread *midi2udp)
{
	this->midi2udp = midi2udp;
	this->lowernote = midi2udp->getRaiseNote();

	// Initialise midi port
	if (!initSeq())
		return (false);

	// run thread
	if (!isRunning())
		start(LowPriority);

	return (true);
}

void
Udp2MidiThread::run()
{
	QUdpSocket *udpSocket = new QUdpSocket();
	if (!udpSocket->bind(PC_PORT)) {
		printf("Could not bind to port %d!\n", PC_PORT);
		return;
	}

	forever {
		if (abort) {
			delete udpSocket;
			return;
		}

		if (!udpSocket->waitForReadyRead(250))
			continue;

		// Receive from UDP
		QHostAddress from_address;
		int len = udpSocket->readDatagram((char*)midimsg,
		    MAX_MIDI_MESSAGE_LENGTH, &from_address);

		if (len == -1) {
			printf("udp2midi: Error receiving data!\n");
			continue;
		}

		if ((len == 3) && (midimsg[0] == 0) &&
		    (midimsg[1] == 0) && (midimsg[2] == 0)) {
			string from_ip = from_address.toString().toStdString();
			printf("Keepalive from: %s\n", from_ip.c_str());
			midi2udp->add_ip(from_ip);
			continue;
		}

		// Send to MIDI
		printf("udp2midi: Sending event: ");
		for (int i = 0; i < len; ++i)
		    printf("0x%02X ", midimsg[i]);
		printf("\n");

		if (snd_midi_event_encode(eventparser,
		    midimsg, len, midi_event) < 0) {
			printf("Error encoding midi event!\n");
			goto out;
		}

		switch (midi_event->type) {
		case SND_SEQ_EVENT_NOTEOFF:
			midi_event->data.note.velocity = 0;
			/* FALLTHROUGH */
		case SND_SEQ_EVENT_NOTEON:
			unsigned char thenote = midi_event->data.note.note;
			thenote -= this->lowernote;
			if (thenote > 127) {
				printf("udp2midi: dropping note %u lowered to out of bounds %u\n",
				    midi_event->data.note.note, thenote);
				goto out;
			}
			midi_event->data.note.note = thenote;

			printf("udp2midi: Note %s (%3u): %3u, channel %u\n",
			    midi_event->data.note.velocity ? "ON " : "off",
			    midi_event->data.note.velocity,
			    midi_event->data.note.note,
			    midi_event->data.control.channel);
			break;
		}

		snd_seq_ev_set_subs(midi_event);
		snd_seq_ev_set_direct(midi_event);
		snd_seq_ev_set_source(midi_event, midi_out_port);

		snd_seq_event_output_direct(seq_handle, midi_event);
 out:
		snd_midi_event_reset_encode(eventparser);
		snd_seq_free_event(midi_event);
	}
}

bool
Udp2MidiThread::initSeq()
{
	if (snd_seq_open(&seq_handle, "default", SND_SEQ_OPEN_OUTPUT, 0) < 0) {
		printf("udp2midi: Error opening ALSA sequencer.\n");
		seq_handle = 0;
		return (false);
	}

	snd_seq_set_client_name(seq_handle, "DSMIDIWIFI UDP2MIDI");

	if ((midi_out_port = snd_seq_create_simple_port(seq_handle,
	    "DSMIDIWIFI UDP2MIDI OUT",
	    SND_SEQ_PORT_CAP_READ | SND_SEQ_PORT_CAP_SUBS_READ,
	    SND_SEQ_PORT_TYPE_APPLICATION)) < 0) {
		printf("udp2midi: Error creating MIDI port!\n");
 out:
		snd_seq_close(seq_handle);
		seq_handle = 0;
		return (false);
	}

	if (snd_midi_event_new(MAX_MIDI_MESSAGE_LENGTH, &eventparser)) {
		printf("udp2midi: Error making midi event parser!\n");
		goto out;
	}

	snd_midi_event_init(eventparser);

	midi_event = (snd_seq_event_t*)malloc(sizeof(snd_seq_event_t));

	return (true);
}

void
Udp2MidiThread::freeSeq()
{
	if (seq_handle && (snd_seq_close(seq_handle) < 0))
		printf("udp2midi: Error closing socket!\n");
}
