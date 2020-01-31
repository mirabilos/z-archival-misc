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

#include "midi2udpthread.h"

#include <arpa/inet.h>

Midi2UdpThread::Midi2UdpThread(QObject *parent)
	:QThread(parent)
{
	abort = false;
}

Midi2UdpThread::~Midi2UdpThread()
{
	mutex.lock();
	abort = true;
	mutex.unlock();
	wait();
	freeSeq();
}

bool
Midi2UdpThread::go(int note)
{
	this->raisenote = note - 69;

	// Initialise midi port
	if (!initSeq())
		return (false);

	// start expecing MIDI events
	npfd = snd_seq_poll_descriptors_count(seq_handle, POLLIN);
	pfd = (struct pollfd *)calloc(npfd, sizeof(struct pollfd));
	snd_seq_poll_descriptors(seq_handle, pfd, npfd, POLLIN);

	// run thread
	if (!isRunning())
		start(LowPriority);

	return (true);
}

void
Midi2UdpThread::add_ip(string ip)
{
	// Duplicates won't be added
	ds_ips.insert(ip);
}

void
Midi2UdpThread::run()
{
	QUdpSocket *udpSocket;
	udpSocket = new QUdpSocket(0);

	forever {
		if (abort) {
			delete udpSocket;
			return;
		}

		if (poll(pfd, npfd, 250) <= 0)
			continue;

		// Get MIDI event
		snd_seq_event_input(seq_handle, &midi_event);

		if (midi_event->type == SND_SEQ_EVENT_NOTEON ||
		    midi_event->type == SND_SEQ_EVENT_NOTEOFF) {
			unsigned char thenote = midi_event->data.note.note;
			thenote += this->raisenote;
			if (thenote > 127) {
				printf("midi2udp: dropping note %u raised to out of bounds %u\n",
				    midi_event->data.note.note, thenote);
				continue;
			}
			midi_event->data.note.note = thenote;
		}

		long len = snd_midi_event_decode(eventparser, midimsg,
		    MAX_MIDI_MESSAGE_LENGTH, midi_event);

		if (len < 0) {
			printf("midi2udp: Error decoding midi event!\n");
		} else {
			printf("midi2udp: got midi event: ");
			for (int i = 0; i < len; ++i)
				printf("0x%02X ", midimsg[i]);
			printf("\n");

			// Send it over UDP
			for (set<string>::iterator ip_it = ds_ips.begin();
			    ip_it != ds_ips.end(); ++ip_it) {
				QString toS((*ip_it).c_str());
				printf("sending to %s\n", (*ip_it).c_str());
				QHostAddress to(toS);
				udpSocket->writeDatagram((char*)midimsg,
				    len, to, DS_PORT);
			}
		}

		snd_seq_free_event(midi_event);

		snd_midi_event_reset_decode(eventparser);
	}
}

bool
Midi2UdpThread::initSeq()
{
	if (snd_seq_open(&seq_handle, "default", SND_SEQ_OPEN_INPUT, 0) < 0) {
		printf("midi2udp: Error opening ALSA sequencer.\n");
		seq_handle = 0;
		return (false);
	}

	snd_seq_set_client_name(seq_handle, "DSMIDIWIFI MIDI2UDP");

	if ((midi_in_port = snd_seq_create_simple_port(seq_handle,
	    "DSMIDIWIFI MIDI2UDP IN",
	    SND_SEQ_PORT_CAP_WRITE | SND_SEQ_PORT_CAP_SUBS_WRITE,
	    SND_SEQ_PORT_TYPE_APPLICATION)) < 0) {
		printf("midi2udp: Error creating MIDI port!\n");
 out:
		snd_seq_close(seq_handle);
		seq_handle = 0;
		return (false);
	}

	if (snd_midi_event_new(MAX_MIDI_MESSAGE_LENGTH, &eventparser)) {
		printf("midi2udp: Error making midi event parser!\n");
		goto out;
	}

	snd_midi_event_init(eventparser);

	midi_event = (snd_seq_event_t*)malloc(sizeof(snd_seq_event_t));

	return (true);
}

void
Midi2UdpThread::freeSeq()
{
	if (seq_handle && (snd_seq_close(seq_handle) < 0))
		printf("midi2udp: Error closing socket!\n");
}
