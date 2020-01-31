/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
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
package ewe.filechooser;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.io.File;
import ewe.io.FileChooserParameters;
import ewe.sys.Vm;
import ewe.sys.VMOptions;
import ewe.sys.Time;
import ewe.sys.Locale;
import ewe.sys.Device;
import ewe.data.*;
import ewe.reflect.Type;
/**
* This class exists only as a container for some of the FileChooser static variables.
**/
//##################################################################
public class FileChooserBase extends Editor{
//##################################################################
public static ewe.fx.IImage folderUp = Device.folderUp;
public static ewe.fx.IImage drive = Device.drive;
public static ewe.fx.IImage computer = Device.computer;
public static ewe.fx.IImage palm = Device.palm;
public static ewe.fx.IImage handHeld = Device.handHeld;
/**
* Do not set this directly.
**/
public static ImageCache toolButtons;

public static boolean allowFileViewing = !ewe.sys.Vm.isMobile();

/**
* This is initially null. You can create a Vector for it and then add some FileChooserLink objects to it.
These links will show up for all FileChooser boxes for this application.
**/
public static Vector globalLinks;
/**
* Do not set this initially, it is only used when the user accepts a file. Then, this can be used
* along with the "chosenFiles" Vector for selecting multiple files.
**/
public String chosenDirectory;
/**
* Do not set this initially, it is only used when the user accepts a file. Then, this can be used
* along with the "chosenDirectory" field for selecting multiple files. It will contain a set of file names.
**/
public Vector chosenFiles;
/**
* This is the standard allFilesMask. By default it is "*.* - All Files.".
**/
public static String allFilesMask = "*.* - All Files.";
/**
* This is an option for use in the constructor. It is mutually exclusive with:
* OPEN, DIRECTORY_SELECT, BROWSE
**/
public static final int SAVE = 1;
/**
* This is an option for use in the constructor. It is mutually exclusive with:
* SAVE, DIRECTORY_SELECT, BROWSE
**/
public static final int OPEN = 2;
/**
* This is an option for use in the constructor. It is mutually exclusive with:
* SAVE, OPEN, BROWSE
**/
public static final int DIRECTORY_SELECT = 3;
/**
* This is an option for use in the constructor. It is mutually exclusive with:
* SAVE, OPEN, DIRECTORY_SELECT
**/
public static final int BROWSE = 0;
/**
* This is an option for use in the constrcutor.
* It will cause a single click on a file to select the file and close the FileChooser.

**/
public static final int QUICK_SELECT = FileChooserParameters.OPTION_QUICK_SELECT;
/**
* This is an option for use in the constrcutor.
* For opening, this indicates that the file must exist.
**/
public static final int FILE_MUST_EXIST = FileChooserParameters.OPTION_FILE_MUST_EXIST;
/**
* This is an option for use in the constrcutor.
* This indicates that the directory tree should be displayed.
**/
public static final int DIRECTORY_TREE = FileChooserParameters.OPTION_DIRECTORY_TREE;
/**
* This is an option for use in the constrcutor.
* This indicates that the FileChooser should not allow the directory to be changed.
**/
public static final int NO_DIRECTORY_CHANGE = FileChooserParameters.OPTION_NO_DIRECTORY_CHANGE;
/**
* This is an option for use in the constrcutor.
* This indicates that the FileChooser will accept any file name given - even with wildcard characters ('*')
**/
public static final int ACCEPT_ANY = FileChooserParameters.OPTION_ACCEPT_ANY;
/**
* This is an option for use in the constrcutor - the same as READ_ONLY.
* This disallows any kind of modification to the file system within the FileChooser.
**/
public static final int NO_WRITING = FileChooserParameters.OPTION_READ_ONLY;
/**
* This is an option for use in the constrcutor - the same as NO_WRITING.
* This disallows any kind of modification to the file system within the FileChooser.
**/
public static final int READ_ONLY = FileChooserParameters.OPTION_READ_ONLY;
/**
* This is an option for use in the constrcutor.
* This should be used with DIRECTORY_SELECT to indicate that you are selecting an install directory.
**/
public static final int INSTALL_SELECT = FileChooserParameters.OPTION_INSTALL_SELECT;
/**
* This is an option for use in the constrcutor.
* This forces the use of the Desktop version of the FileChooser.
**/
public static final int DESKTOP_VERSION = FileChooserParameters.OPTION_DESKTOP_VERSION;
/**
* This is an option for use in the constrcutor.
* This tells the system not to confirm overwritting of existing files when the SAVE option is used.
**/
public static final int NO_CONFIRM_OVERWRITE = FileChooserParameters.OPTION_NO_CONFIRM_OVERWRITE;
/**
* This is an option for use in the constrcutor.
* This tells the FileChooser that you are going to be adding extra controls and so a Split panel should
* be used to place the extra control in.
**/
public static final int EXTRA_CONTROL = 0x800;
/**
* This is an option for use in the constrcutor.
* This allows the user to select multiple files.
**/
public static final int MULTI_SELECT = FileChooserParameters.OPTION_MULTI_SELECT;
/**
* This is an option for use in the constrcutor.
* This uses an Explorer type display, with no file mask and no file choosing.
**/
public static final int EXPLORER_TYPE = 0x2000;
/**
* This is an option for use in the constrcutor.
* This is no longer used.
**/
public static final int LAUNCHER_TYPE = 0x4000|EXPLORER_TYPE;
/**
* This is an option for use in the constrcutor.
* This indicates that program execution should not be allowed.
**/
public static final int NO_EXECUTE = FileChooserParameters.OPTION_NO_EXECUTE;
/**
* This is an option for use in the constrcutor.
* This indicates that the file chooser will be embedded in another control
* and should not close when a file is chosen, but rather just fire an ControlEvent.ACTION event.
**/
public static final int EMBEDDED = 0x10000;
public static final int DONT_SHOW_FILE_EXTENSION = FileChooserParameters.OPTION_DONT_SHOW_FILE_EXTENSION;
/**
* This contains the directories or names of recently opened files.
**/
public static Vector history = new Vector();
/**
* This sets the number of history entries. By default it is 10.
**/
public static int historySize = 10;
/**
* This indicates that only directories should be placed in the history Vector. By default
* it is true.
**/
public static boolean historyDirectoriesOnly = true;

static Vector extensions = new Vector(), icons = new Vector();
public static boolean wideScreen = Gui.screenIs(Gui.WIDE_SCREEN);
public static boolean desktopWide = Gui.screenIs(Gui.DESKTOP_WIDTH);
public static FileChooserOptions lastOptions = new FileChooserOptions();
static Type web = new Type("ewe.ui.WebBrowser");
static Type eweView = new Type("ewe.io.EweFileBrowser");
static Type zipView = new Type("ewe.zip.ZipFileBrowser");


//##################################################################
}
//##################################################################

