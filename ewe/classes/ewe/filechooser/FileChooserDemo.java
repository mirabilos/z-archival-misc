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
import ewe.data.*;

//##################################################################
public class FileChooserDemo extends LiveObject implements Runnable{
//##################################################################

public boolean
	showTree = false,
	quickSelect = false,
	useImages = false,
	browseOnly = true,
	multiSelect = false,
	explorer = false;

public String chooserTitle = "File Chooser";
public String title = "FileChooser Demo Options";
public String masks = "";
public int type = 0;
public boolean asABrowser = false;
public boolean doExec = false;
//===================================================================
public void addToPanel(CellPanel cp,Editor f,int which)
//===================================================================
{
	f.windowFlagsToClear = Window.FLAG_HAS_CLOSE_BUTTON;
	f.title = title;
	InputStack is = new InputStack();
	if (!asABrowser){
		is.add(f.addField(new mChoice(new String[]{"File(s)","Directory"},0),"type"),"Chooser Type");
		cp.addLast(is).setCell(f.HSTRETCH);
	}
	is = new InputStack(); //is.columns = 2;
	is.add(f.addField(new mCheckBox(),"showTree"),"Directory Tree");
	is.add(f.addField(new mCheckBox(),"useImages"),"Image Preview");
	if (!asABrowser){
		is.add(f.addField(new mCheckBox(),"multiSelect"),"Multi-Select");
		is.add(f.addField(new mCheckBox(),"browseOnly"),"Browse Only");
		is.add(f.addField(new mCheckBox(),"explorer"),"Explorer");
		is.add(f.addField(new mCheckBox(),"quickSelect"),"Quick Select");
		//if (Gui.screenIs(Gui.WIDE_SCREEN))
		is.add(f.addField(new mInput(),"masks"),"File Masks:");
	}
	cp.addLast(is).setCell(f.HSTRETCH);
	f.doButtons(f.DEFOKB|f.DEFCANCELB);
	f.cancel.setHotKey(0,IKeys.ESCAPE);
	f.exitButtonDefined = true;
	f.setTextSize(title.length(),-1);
	//f.resizable = true;
	//f.titleCancel = new mButton(f.cross);
	//f.titleOK = new mButton(f.tick);
}
boolean exitSystem = false;

//===================================================================
public void runDemo(boolean xs){
//===================================================================
	exitSystem = xs;
	new ewe.sys.Coroutine(this,100);
}

public ewe.io.File aFile = ewe.sys.Vm.newFileObject();
public String initial = ewe.io.File.getProgramDirectory();
//===================================================================
public void run()
//===================================================================
{
	//int rot = 0;
	while(true){
		Editor ed = getEditor(0);
		int doDemo = ed.execute();
		if (doDemo == Form.IDCANCEL) break;
		//rot = (rot+1) % 3; mApp.rotateScreen(rot);
		int options = 0;
		if (quickSelect) options |= FileChooser.QUICK_SELECT;
		if (showTree) options |= FileChooser.DIRECTORY_TREE;
		if (multiSelect) options |= FileChooser.MULTI_SELECT;
		if (explorer) options |= FileChooser.EXPLORER_TYPE;
		if (type == 1) options |= FileChooser.DIRECTORY_SELECT;
		else options |= browseOnly|asABrowser ? FileChooser.BROWSE : FileChooser.OPEN;
		FileChooser f =
			useImages ?
			new ImageFileChooser(options,initial,aFile):
			new FileChooser(options,initial,aFile);
		String [] ms = mString.split(masks,',');
		for (int i = 0; i<ms.length; i++) f.addMask(ms[i]);
		//if (!useImages) f.addMask("*.* - All Files.");
		f.title = chooserTitle;
		if (doExec) f.exec();
		else f.show();
		if (f.waitUntilClosed() == f.IDOK){
			if (asABrowser) return;
			MessageBox mb = new MessageBox("Chosen","You chose:\n"+f.getChosen()+((f.chosenFiles.size() > 1) ? "\nand more." : ""),0);
			mb.execute();
		}
		if (asABrowser) break;
	}
	if (exitSystem) mApp.mainApp.exit(0);
}

public static void main(String [] args)
{
	ewe.sys.Vm.startEwe(args);
	//mApp.rotateScreen(mApp.ROTATE_COUNTER_CLOCKWISE);
	new FileChooserDemo().run();
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

