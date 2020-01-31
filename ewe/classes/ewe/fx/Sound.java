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
package ewe.fx;

/**
 * Sound is used to play sounds such as beeps and tones.
 * <p>
 * Playing beeps is supported under all platforms but tones are only supported
 * where the underlying platform supports generating tones. Tones aren't supported
 * under Java or Windows CE.
 * <p>
 * Here is an example that beeps the speaker and plays a tone:
 *
 * <pre>
 * Sound.beep();
 * Sound.tone(4000, 300);
 * </pre>
 */
public class Sound
{
/** Plays the device's default beep sound. */
public native static void beep();

public static final int MB_OK                       = 0x00000000;
public static final int MB_ICONHAND                 = 0x00000010;
public static final int MB_ICONQUESTION             = 0x00000020;
public static final int MB_ICONEXCLAMATION          = 0x00000030;
public static final int MB_ICONASTERISK             = 0x00000040;

/** Plays one of the device's beep sounds. */
public native static void beep(int sound);

/**
 * Plays a tone of the specified frequency for the specified
 * duration. Tones will only play under Win32 and PalmOS, they won't
 * play under Java or Windows CE due to underlying platform limitations.
 * @param freq frequency in hertz from 32 to 32767
 * @param duration duration in milliseconds
 */
public native static void tone(int freq, int duration);

}
