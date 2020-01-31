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
package ewe.sys;
import ewe.fx.*;
import ewe.io.*;

//##################################################################
public interface VmConstants{
//##################################################################
/*
 *  flag values for fuSound and fdwSound arguments on [snd]PlaySound
 */
public static final int SND_SYNC            = 0x0000  ; /* play synchronously (default) */
public static final int SND_ASYNC           = 0x0001  ; /* play asynchronously */
public static final int SND_NODEFAULT       = 0x0002  ; /* silence (!default) if sound not found */
public static final int SND_MEMORY          = 0x0004  ; /* pszSound points to a memory file */
public static final int SND_LOOP            = 0x0008  ; /* loop the sound until next sndPlaySound */
public static final int SND_NOSTOP          = 0x0010  ; /* don't stop any currently playing sound */

public static final int SND_NOWAIT	= 0x00002000 ; /* don't wait if the driver is busy */
public static final int SND_ALIAS       = 0x00010000 ; /* name is a registry alias */
public static final int SND_ALIAS_ID	= 0x00110000 ; /* alias is a predefined ID */
public static final int SND_FILENAME    = 0x00020000 ; /* name is file name */
public static final int SND_RESOURCE    = 0x00040004 ; /* name is resource name or atom */

/**
* Used with getParameter() it gets VM flag bits.
**/
public static final int VM_FLAGS = 4;
/**
* This is a VM flag bit which indicates that the main program class was
* loaded from a class file rather than from a ewe file. This only works
* from the desktop.
**/
public static final int VM_FLAG_USING_CLASSES = 0x1;
/**
* This is a VM flag bit which indicates that the VM is running on a Mobile PC.
**/
public static final int VM_FLAG_IS_MOBILE = 0x2;
/**
* This is a VM flag bit which indicates that the VM is running on a slow machine.
**/
public static final int VM_FLAG_SLOW_MACHINE = 0x4;
/**
* This is a VM flag bit which indicates that the VM is running on a system with a monochrome display.
**/
public static final int VM_FLAG_IS_MONOCHROME = 0x8;
/**
* This is a VM flag bit which indicates that the VM is running on a system that has no keyboard.
**/
public static final int VM_FLAG_NO_KEYBOARD = 0x10;
/**
* This is a VM flag bit which indicates that the VM is running on a system that has no mouse pointer.
**/
public static final int VM_FLAG_NO_MOUSE_POINTER = 0x20;
/**
* This is a VM flag bit which indicates that the VM is running as an Applet in a browser.
**/
public static final int VM_FLAG_IS_APPLET = 0x40;
/**
* This is a VM flag bit which indicates that multiple native windows are not supported.
**/
public static final int VM_FLAG_NO_WINDOWS = 0x80;
/**
* This is a VM flag bit which indicates that the platform has a SipButton at the bottom that may overwrite
* program controls (only PocketPC has this).
**/
public static final int VM_FLAG_SIP_BUTTON_ON_SCREEN = 0x100;
/**
* This is a VM flag bit which indicates that the platform does not want CR characters in its text files. Generally
* this indicates a Unix system.
**/
public static final int VM_FLAG_NO_CR = 0x200;
/**
* This is a VM flag bit which indicates that the program was run with a command line requesting the screen
* to be rotated by 90 degrees clockwise.
**/
public static final int VM_FLAG_ROTATE_SCREEN = 0x400;
/**
* This is a VM flag bit which indicates that the program is running on the Sharp Zaurus Jeode(R) EVM. If it is
* then a number of bug fixes must be implemented and other flags set.
**/
public static final int VM_FLAG_IS_ZAURUS_EVM = 0x800;
/**
* This is a VM flag bit which indicates that the program is running on low-memory device
* such as the Casio BE300. Your application should minimize on the complexity of its user
* interface in such cases.
**/
public static final int VM_FLAG_LOW_MEMORY = 0x1000;
/**
* This is a VM flag bit which indicates that the program was run with a command line requesting the screen
* to be rotated by 90 degrees anti-clockwise.
**/
public static final int VM_FLAG_COUNTER_ROTATE_SCREEN = 0x2000;
/**
This is a VM flag bit which indicates that the platform has no mouse OR touchscreen capabilities and
only keyboard navigation is possible - for example on many SmartPhone devices.
**/
public static final int VM_FLAG_NO_PEN = 0x4000;
/**
This is a VM flag bit which indicates that text input is not possible unless a native platform
input box is used. This is necessary for certain platforms such as MS SmartPhone devices.
**/
public static final int VM_FLAG_USE_NATIVE_TEXT_INPUT = 0x8000;
/**
This is a VM flag bit which indicates that the device has at least 2 general purpose
"Soft" keys, such as MS SmartPhone devices.
**/
public static final int VM_FLAG_HAS_SOFT_KEYS = 0x10000;
/**
This is a VM flag bit which indicates that the device will ALWAYS display the SIP
button. This flag can be explicitly set using Vm.setParameter(SET_ALWAYS_SHOW_SIP_BUTTON,1);
**/
public static final int VM_FLAG_SIP_BUTTON_ALWAYS_SHOWN = 0x20000;
/**
This is a VM flag bit which indicates that the device will ALWAYS display the SIP
button. This flag can be explicitly set using Vm.setParameter(SET_ALWAYS_SHOW_SIP_BUTTON,1);
**/
public static final int VM_FLAG_NO_GUI = 0x40000;

public static final int VM_FILE_SEPARATOR = 6;
public static final int VM_PATH_SEPARATOR = 7;
/**
* This is the only one apart from DEFAULT_CURSOR you should use directly with
* ewe.sys.Vm.setCursor(). All others should be used with Control.setCursor().
**/
public static final int WAIT_CURSOR  = 1;
public static final int IBEAM_CURSOR  = 2;
public static final int HAND_CURSOR  = 3;
public static final int CROSS_CURSOR  = 4;
public static final int LEFT_RIGHT_CURSOR  = 5;
public static final int UP_DOWN_CURSOR  = 6;
/**
* This cursor is an arrow and an hour glass. Use this when an individual component
* will not respond because it is busy, but other components will work.
**/
public static final int BUSY_CURSOR = 7;
public static final int DRAG_SINGLE_CURSOR = 8;
public static final int DRAG_MULTIPLE_CURSOR = 9;
public static final int COPY_SINGLE_CURSOR = 10;
public static final int COPY_MULTIPLE_CURSOR = 11;
public static final int DONT_DROP_CURSOR = 12;
public static final int MOVE_CURSOR = 13;
public static final int RESIZE_CURSOR = 14;
public static final int INVISIBLE_CURSOR = 15;
/**
* This means no <b>special</b> cursor;
**/
public static final int NO_CURSOR = 0;
public static final int DEFAULT_CURSOR = 0;

/**
 * An option for Vm.memoryMapFile() - it tells the system that if it
 * <b>must</b> provide the options requested (like MEMORY_MAP_ON_DEMAND)
 * and if it cannot, then it should throw an exception instead of
 * continuing.
 */
public static final int MEMORY_MAP_STRICT = 0x1;
/**
 * An option for Vm.memoryMapFile() - it tells the system to map in only the
 * area of the file that is being accessed via a read/write operation.
 * <p>
 * Not all systems will support this option, in which case it is ignored
 * and the entire file is mapped into memory.
 */
public static final int MEMORY_MAP_ON_DEMAND = 0x2;

//##################################################################
}
//##################################################################

