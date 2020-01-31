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

#include <QApplication>

#include "wifimidiwindow.h"

extern "C" int
main(int argc, char *argv[])
{
	QApplication app(argc, argv);
	QApplication::setApplicationName("DSMIDIWiFi");
	const QStringList args = QApplication::arguments();

	if (args.size() != 2) {
		fprintf(stderr, "E: missing note argument!\n");
 usage:
		fprintf(stderr, "I: syntax: DSMIDIWiFi <note>"
		    "\n	where <note> is the MIDI note to map A₄ to (normally 69)"
		    "\n	(Android Piano For You needs 57, TouchDAW 81, YMMV…)\n");
		return (1);
	}
	bool ok;
	int note = args.at(1).toInt(&ok, 0);
	if (!ok) {
		fprintf(stderr, "E: note argument <%s> not a number!\n",
		    qPrintable(args.at(1)));
		goto usage;
	}
	if (note < 0 || note > 127) {
		fprintf(stderr, "E: note argument %d out of [0‥127] range!\n", note);
		goto usage;
	}

	WifiMidiWindow wmw(note);
	if (!wmw.good())
		return (1);
	wmw.show();
	return (app.exec());
}
