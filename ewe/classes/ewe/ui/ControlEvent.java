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

import ewe.sys.*;

/**
 * ControlEvent is an event posted by a control.
 */

public class ControlEvent extends Event
{
/** The event type for a pressed event. */
public static final int PRESSED = 300;
/** The event type for a focus in event. */
public static final int FOCUS_IN = 301;
/** The event type for a focus out event. */
public static final int FOCUS_OUT = 302;
/** The event type for a timer event. */
public static final int TIMER = 303;
/** The event type for a Cancel event (e.g. ESC pressed). */
public static final int CANCELLED = 304;
/** The event type for a Exit event (i.e. one of the exit keys was pressed). */
public static final int EXITED = 305;
/** The event type for when a control shows its associated menu. */
public static final int MENU_SHOWN = 306;

public ControlEvent()
	{
	}

/**
 * Constructs a control event of the given type.
 * @param type the type of event
 * @param c the target control
 */
public ControlEvent(int type, Control c)
	{
	this.type = type;
	target = c;
	timeStamp = Vm.getTimeStamp();
	if (type == PRESSED){
		if (c instanceof mButton)
			action = ((mButton)c).action;
		if (action == null && c != null) action = c.getText();
	}
	}

//MLB
public static final int /*TEXT_CHANGED = 40000,*/ OPERATION_CANCELLED = 40001, POPUP_CLOSED = 40002;

/**
* This is set if one of the exit keys was pressed.
**/
public int exitKey;
/**
* This is set if one of the exit keys was pressed.
**/
public int exitKeyModifiers;
/**
* If the event is a FOCUS_IN then this is the control that is losing focus. If it is a FOCUS_OUT then this
* is the control that is taking away the focus.
**/
public Control oldOrNewFocus;
/**
* If the event is a PRESSED event, and an mButton generated the event, then
* this value will be equal to the "action" value of the button, or the button's text
* if its action is null.
**/
public String action;
}

