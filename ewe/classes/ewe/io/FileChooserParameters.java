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
import ewe.data.PropertyList;

//##################################################################
public class FileChooserParameters extends PropertyList{
//##################################################################

public static final String TYPE_OPEN = "open";
public static final String TYPE_SAVE = "save";
public static final String TYPE_DIRECTORY_SELECT = "directory";
//public static final String TYPE_OPEN = "open";
//public static final String TYPE_OPEN = "open";

/**
This property is used with a FileChooserLink object to specify a location
folder the user can select. There can be multiple values for LOCATION.
**/
public static final String LOCATION = "location";

public static final String PERSISTENT_HISTORY = "persistentHistory";
/**
This property is used to set the title of the FileChooser.
should be "open" or "save" or "directory" or "browse".
**/
public static final String TITLE = "title";
/**
This is property is used to select the type of the FileChooser. This
should be "open" or "save" or "browse".
**/
public static final String TYPE = "type";
/**
This is property is used to set the default extension.
**/
public static final String DEFAULT_EXTENSION = "defaultExtension";
/**
This is a property for the FileChooser, which should be a String value.
**/
public static final String START_LOCATION = "startLocation";
/**
This is a property for the FileChooser - you can have more than one, which should be a String value.
**/
public static final String FILE_MASK = "mask";
/**
This is a property for the FileChooser - it should be of type ewe.io.File.
**/
public static final String FILE_MODEL = "fileModel";
/**
This is set on return - it is the list of chosen files as an array of File objects.
If multiple file selection is not allowed, there will be only one element in the list.
**/
public static final String CHOSEN_FILES = "chosenFiles";
/**
This is set on return - it is the chosen file as an ewe.io.File object.
**/
public static final String CHOSEN_FILE = "chosenFile";
/**
This is used to set the options for the FileChooser. This should be an integer value
which would be any of the OPTION_XXX values ORed together.
**/
public static final String OPTIONS = "options";
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_QUICK_SELECT = 0x4;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_FILE_MUST_EXIST = 0x8;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_DIRECTORY_TREE = 0x10;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_NO_DIRECTORY_CHANGE = 0x20;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_ACCEPT_ANY = 0x40;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_READ_ONLY = 0x80;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_INSTALL_SELECT = 0x100;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_DESKTOP_VERSION = 0x200;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_NO_CONFIRM_OVERWRITE = 0x400;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_MULTI_SELECT = 0x1000;
/**
This is an option to be used with the OPTIONS property
- it indicates
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_NO_EXECUTE = 0x8000;
//
// Start others at 0x100000
//
/**
This is an option to be used with the OPTIONS property
- it indicates that file extensions should not be displayed.
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_DONT_SHOW_FILE_EXTENSION = 0x100000;
/**
This is an option to be used with the OPTIONS property.
- it is OPTION_DONT_SHOW_EXTENSION|OPTION_NO_DIRECTORY_CHANGE.
You can bitwise OR together this option with any of the others.
**/
public static final int OPTION_SIMPLE = OPTION_DONT_SHOW_FILE_EXTENSION|OPTION_NO_DIRECTORY_CHANGE;


//##################################################################
}
//##################################################################

