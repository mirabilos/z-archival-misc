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
import ewe.io.*;

//##################################################################
public abstract class Process{
//##################################################################
/**
 * Kills the process.
 */
public abstract void destroy();
/**
 * Wait until the process exits.
 */
public abstract void waitFor();
/**
* Wait until the process exits.
* @param t The length of time to wait.
* @return true if the process did exit, false if the timeout expired before the process exited.
*/
public abstract boolean waitFor(TimeOut t);
/**
 * Get the exit value of the process.
 * @return the exit value of the process.
 * @exception IllegalThreadStateException if the process is still running.
 */
public abstract int exitValue() throws IllegalThreadStateException;
/**
 * Return an Input Stream to read from the standard error output of the process.
 */
public abstract Stream getErrorStream();
/**
 * Return an Output Stream to write to the standard input of the process.
 */
public abstract Stream getOutputStream();
/**
 * Return an Input Stream to read from the standard output of the process.
 */
public abstract Stream getInputStream();
//##################################################################
}
//##################################################################

