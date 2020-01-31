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
package ewe.ui;

import ewe.fx.*;
import ewe.sys.Vm;

import ewe.applet.WinTimer;
import ewe.applet.Applet;

/**
 * MainWindow is the main window of a UI based application.
 * <p>
 * All Waba programs with a user-interface must have a main window.
 * <p>
 * Here is an example showing a basic application:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * public void onStart()
 *  {
 *  ... initialization code ...
 *  Label label = new Label("Name:");
 *  label.setRect(..);
 *  add(label);
 *  }
 * }
 * </pre>
 */

public class MainWindow extends Window
{
WinTimer _winTimer; //JAVA Only.

Timer timers;
Font _cachedFont;
FontMetrics _cachedFontMetrics;
public static final Font defaultFont = new Font("Helvetica", Font.PLAIN, 12);
static MainWindow _mainWindow;

/** Constructs a main window. */
public MainWindow()
	{
	_mainWindow = this;
	_winTimer = new WinTimer(this); // Java
	//_nativeCreate(); Not Java
	}

//private native void _nativeCreate(); Not Java

/**
 * Notifies the application that it should stop executing and exit. It will
 * exit after executing any pending events. If the underlying system supports
 * it, the exitCode passed is returned to the program that started the app.
 */
public void exit(int exitCode) // Java
	{
	onExit();
	shutdownAll();
	if (ewe.applet.Applet.currentApplet.isApplication)
		System.exit(exitCode);
	else{
		if (ewe.applet.Applet.currentApplet.frame != null){
			ewe.applet.Applet.currentApplet.frame.hide();
		}
	}
	}
//public native void exit(int exitCode); Not Java


/** Returns the MainWindow of the current application. */
public static MainWindow getMainWindow()
	{
	return _mainWindow;
	}

/** Returns the font metrics for a given font. */
public FontMetrics getFontMetrics(Font font)
	{
	if (font == _cachedFont)
		return _cachedFontMetrics;
	_cachedFont = font;
	_cachedFontMetrics = new FontMetrics(font, this);
	return _cachedFontMetrics;
	}

/**
 * Adds a timer to a control. This method is protected, the public
 * method to add a timer to a control is the addTimer() method in
 * the Control class.
 */
protected Timer addTimer(Control target, int millis)
	{
	Timer t = new Timer();
	t.target = target;
	t.millis = millis;
	t.lastTick = Vm.getTimeStamp();
	t.next = timers;
	timers = t;
	_onTimerTick();
	return t;
	}

/**
 * Removes a timer. This method returns true if the timer was found
 * and removed and false if the given timer could not be found.
 */
public boolean removeTimer(Timer timer)
	{
	if (timer == null)
		return false;
	Timer t = timers;
	Timer prev = null;
	while (t != timer)
		{
		if (t == null)
			return false;
		prev = t;
		t = t.next;
		}
	if (prev == null)
		timers = t.next;
	else
		prev.next = t.next;
	_onTimerTick();
	return true;
	}

/**
 * Called just before an application exits.
 */
public void onExit()
	{
	}

/**
 * Called when an application starts. Initialization code is usually either placed
 * in this method or simply in the app's constructor. This method is called
 * just after the app's constructor is called.
 */
public void onStart() throws Exception
	{
	}

/**
 * Called by the VM to process timer interrupts. This method is not private
 * to prevent the compiler from removing it during optimization.
 */
public void _onTimerTick()
	{
	synchronized(Applet.uiLock){
	int minInterval = 0;
	int now = Vm.getTimeStamp();
	Timer timer = timers;
	while (timer != null)
		{
		int diff = now - timer.lastTick;
		if (diff < 0)
			diff += (1 << 30); // wrap around - max stamp is (1 << 30)
		int interval;
		if (diff >= timer.millis)
			{
			// post TIMER event
			Control c = timer.target;
			_controlEvent.type = ControlEvent.TIMER;
			_controlEvent.target = c;
			c.postEvent(_controlEvent);
			timer.lastTick = now;
			interval = timer.millis;
			}
		else
			interval = timer.millis - diff;
		if (interval < minInterval || minInterval == 0)
			minInterval = interval;
		timer = timer.next;
		}
	_setTimerInterval(minInterval);
	if (needsPaint)
		_doPaint(paintX, paintY, paintWidth, paintHeight);
	}
	}

/**
 * Called to set the VM's timer interval. This method is not public,
 * you should use the addTimer() method in the Control class to
 * create a timer.
 */

protected void _setTimerInterval(int milliseconds)
	{
	_winTimer.setInterval(milliseconds);
	}

public void _stopTimer()
	{
	_winTimer.stopGracefully();
	}

}
