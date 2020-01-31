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
package ewe.io;

//##################################################################
public interface FilePermissions{
//##################################################################

/**
* A DOS type file flag.
**/
public final static int FLAG_HIDDEN = 0x1000;
/**
* A DOS type file flag.
**/
public final static int FLAG_SYSTEM = 0x2000;
/**
* A DOS type file flag.
**/
public final static int FLAG_ARCHIVE = 0x4000;
/**
* A permission/flag for files. This flag can be used with DOS type file systems AND with
* UNIX systems. On a UNIX system, attempting to set this flag will switch off write permission
* for all users (owner/group/Other), and attempting to clear this flag will switch on write permission to
* owner only.
**/
public final static int FLAG_READONLY = 0x8000;
/**
* A DOS type file flag - indicates the flag is in ROM.
**/
public final static int FLAG_ROM = 0x10000;
/**
* A DOS type file flag - indicates the flag is in ROM and is designed to execute in place.
**/
public final static int FLAG_ROMMODULE = 0x20000;
/**
* A UNIX type permission.
**/
public final static int OWNER_READ = 0x100;
/**
* A UNIX type permission.
**/
public final static int OWNER_WRITE = 0x80;
/**
* A UNIX type permission.
**/
public final static int OWNER_EXECUTE = 0x40;
/**
* A UNIX type permission.
**/
public final static int GROUP_READ = 0x20;
/**
* A UNIX type permission.
**/
public final static int GROUP_WRITE = 0x10;
/**
* A UNIX type permission.
**/
public final static int GROUP_EXECUTE = 0x8;
/**
* A UNIX type permission.
**/
public final static int OTHER_READ = 0x4;
/**
* A UNIX type permission.
**/
public final static int OTHER_WRITE = 0x2;
/**
* A UNIX type permission.
**/
public final static int OTHER_EXECUTE = 0x1;
/**
* All the OWNER_ and GROUP_ and OTHER_ permissions together.
**/
public final static int ALL_UNIX_PERMISSIONS = 0x1ff;
/**
* All the FLAG_ flags together.
**/
public final static int ALL_DOS_FLAGS = 0x3f000;


//##################################################################
}
//##################################################################

