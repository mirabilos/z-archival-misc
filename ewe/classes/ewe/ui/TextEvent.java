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

/**
 A TextEvent is sent to an EditControl (mInput, mTextPad) usually during or upon completion
 of a <b>native</b> text input.
 */
public class TextEvent extends Event
{

/**
	The event type for a text entered event.
*/
public static final int TEXT_ENTERED = 150;
/**
	The event type for a text changed event.
*/
public static final int TEXT_CHANGED = 151;
/**
	This is sent to the Window indicating that the input box has closed.
*/
//public static final int INPUT_CLOSED = 151;

static final int TEXT_EVENT_FIRST = TEXT_ENTERED;
static final int TEXT_EVENT_LAST = TEXT_CHANGED;//INPUT_CLOSED;
/**
	The text that was entered for a TEXT_ENTERED message.
*/
public String entered;

/**
	Extra flags for the entered data.
*/
public int flags;

/**
This is used with the TEXT_ENTERED event and indicates that the input was not cancelled.
**/
public static final int FLAG_TEXT_WAS_ENTERED = 0x1;
/**
This is used with the TEXT_ENTERED event and indicates that the input ended by the UP cursor key.
**/
public static final int FLAG_CLOSED_BY_UP_KEY = 0x2;
/**
This is used with the TEXT_ENTERED event and indicates that the input ended by the DOWN cursor key.
**/
public static final int FLAG_CLOSED_BY_DOWN_KEY = 0x4;
/**
This is used with the TEXT_ENTERED event and indicates that the input ended by the ENTER key.
**/
public static final int FLAG_CLOSED_BY_ENTER_KEY = 0x8;
/**
This is used with the TEXT_ENTERED event and indicates that the input ended by the ENTER key.
**/
public static final int FLAG_CLOSED_BY_SOFT_KEY = 0x10;
}

