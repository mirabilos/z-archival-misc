/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.applet;

import ewe.ui.MainWindow;

public class WinTimer extends Thread
{
MainWindow win;
private int interval = 0;
private boolean shouldStop = false;

public WinTimer(MainWindow win)
	{
	this.win = win;
	start();
	}

public void setInterval(int millis)
	{
	interval = millis;
	interrupt();
	}

public void stopGracefully()
	{
	// NOTE: It's not a good idea to call stop() on threads since
	// it can cause the JVM to crash.
	shouldStop = true;
	interrupt();
	}

public void run()
	{
	while (!shouldStop)
		{
		boolean doTick = true;
		int millis = interval;
		if (millis <= 0)
			{
			// NOTE: Netscape navigator doesn't support interrupt()
			// so we sleep here less than we would normally need to
			// (1 second) if we're not doing anything to check if
			// the timer should start in case interrupt didn't work
			millis = 1 * 1000;
			doTick = false;
			}
		try { sleep(millis); }
		catch (InterruptedException e) { doTick = false; }
		if (doTick && Applet.currentApplet.mainWindow != null)
			Applet.currentApplet.mainWindow.doTimerTick();
		}
	}
}
