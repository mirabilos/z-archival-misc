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
import ewe.ui.*;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;
import ewe.fx.ImageCache;
import ewe.fx.Color;
import ewe.sys.Device;
//##################################################################
public class SimpleFileChooser extends Form{
//##################################################################

protected String type;
protected int options;
protected File currentDir, curFile;
protected File model;
protected mList fileList;
protected mInput fileName;
protected FileChooserParameters fcp;
protected FileChooserFolderChoice folder;
protected mChoice maskChoice, locationChoice;
protected Control fileNameControl;
protected String[] roots;
protected Vector currentFiles;
protected Vector masks;
protected String currentMask;

private boolean noDir, noExt, isDirSelect, isSlow;

//===================================================================
public SimpleFileChooser(FileChooserParameters fcp)
//===================================================================
{
	init(fcp);
}
//-------------------------------------------------------------------
protected void init(FileChooserParameters fcp)
//-------------------------------------------------------------------
{
	closedFocus = true;
	this.fcp = fcp;
	type = fcp.getString(fcp.TYPE,fcp.TYPE_OPEN);
	title = fcp.getString(fcp.TITLE,"Select file.");
	model = (File)fcp.getValue(fcp.FILE_MODEL,File.getNewFile());
	currentDir = model.getNewFile(fcp.getString(fcp.START_LOCATION,File.getProgramDirectory()));
	curFile = currentDir.getNew("");
	options = fcp.getInt(fcp.OPTIONS,0);
	if (type.equalsIgnoreCase(fcp.TYPE_OPEN)) options |= fcp.OPTION_FILE_MUST_EXIST;
	isDirSelect = type.equalsIgnoreCase(fcp.TYPE_DIRECTORY_SELECT);
	//
	if (!currentDir.isDirectory())
		currentDir = model.getNewFile(File.getProgramDirectory());
	//
	noDir = (options & fcp.OPTION_NO_DIRECTORY_CHANGE) != 0;
	noExt = (options & fcp.OPTION_DONT_SHOW_FILE_EXTENSION) != 0;
	isSlow = ((currentDir.getFlags() & File.FLAG_SLOW_LIST) != 0 || ewe.sys.Vm.isMobile());

	InputStack is = new InputStack();

	Vector locations = fcp.getPropertyValues(fcp.LOCATION);
	if (locations.size() != 0){
		is.add(locationChoice = new mChoice(),"Location");
		int which = -1;
		for (int i = 0; i<locations.size(); i++){
			FileChooserLink fcl = (FileChooserLink)locations.get(i);
			fcl.addToMenu(locationChoice,getFontMetrics());
			if (which != -1) continue;
			File f2 = fcl.toFile(currentDir);
			for(File ff = currentDir; ff != null; ff = ff.getParentFile()){
				if (ff.equals(f2)){
					which = i;
					break;
				}
			}
		}
		locationChoice.selectedIndex = which == -1 ? 0 : which;
	}
	if (!noDir){
		is.add(folder = new FileChooserFolderChoice(),"Folder");
		folder.prompt = "Select Folder";
	}
	masks = fcp.getPropertyValues(fcp.FILE_MASK);
	if (masks.size() == 0) masks.add("*.* - All files.");
	currentMask = masks.get(0).toString();
	if (masks.size() > 1){
		is.add(maskChoice = new mChoice(),"Type");
		maskChoice.items.addAll(masks);
		maskChoice.selectedIndex = 0;
	}
	if (true || Gui.isSmartPhone){
		fileName = isDirSelect ? new mInput() : is.addInput("File","");
		fileNameControl = fileName;
	}else{
		// I've disabled this.
		/*
		mComboBox mcb = new mComboBox();
		fileName = mcb.input;
		mcb.choice.items.addAll(masks);
		is.add(mcb,"File");
		fileNameControl = mcb;
		*/
	}
	fileName.wantReturn = true;
	addLast(is).setCell(HSTRETCH);
	//
	fileList = new mList(20,40,false){
		//==================================================================
		public void onKeyEvent(KeyEvent ev)
		//==================================================================
		{
			if (ev.type == ev.KEY_PRESS){
				if (ev.key == IKeys.RIGHT || ev.key == IKeys.LEFT){
					tryNext(false);
					return;
				}
			}else if (ev.key == IKeys.BACKSPACE && !noDir){
				if (currentDir.getParentFile() != null)
					setDirectory(currentDir.getParentFile());
			}
			super.onKeyEvent(ev);
		}
	};
	addLast(new VerticalScrollPanel(fileList));
	fileList.options |= fileList.OPTION_SELECT_FIRST_ON_KEY_FOCUS|fileList.OPTION_CURSOR_EXIT_UP;
	//
	firstFocus = isDirSelect ? (Control)fileList : (Control)fileName;
	Object got = currentDir.getNew("").getInfo(File.INFO_ROOT_LIST,null,null,0);
	if (got instanceof String[]) roots = (String[])got;
	setDirectory(currentDir,false);

	exitButtonDefined = true;
	addButton(ok = makeButtonForForm("Accept",tick,0));
	addButton(cancel = makeDefaultButton(CANCELB,true));
}

//-------------------------------------------------------------------
protected String getCurrentMask()
//-------------------------------------------------------------------
{
	return currentMask;
}

//-------------------------------------------------------------------
protected void setDirectory(File newDir)
//-------------------------------------------------------------------
{
	setDirectory(newDir,false);
}

private static Vector readingDir;

//-------------------------------------------------------------------
protected void setDirectory(File newDir,boolean focusOnFile)
//-------------------------------------------------------------------
{
	if (isSlow) {
		if (readingDir == null) {
			readingDir = new Vector();
			readingDir.add("Reading directory...");
		}
		updateFileList(readingDir,false);
	}
	//
	currentDir.set(null,newDir.getFullPath());
	//
	if (folder != null){
		folder.updateMenu(newDir,roots,null);
		folder.repaintNow();
	}
	//
	Vector all = new Vector();
	if (isDirSelect) all.addAll(currentDir.listMultiple("*.*",File.LIST_DIRECTORIES_ONLY));
	else{
		int opts = 0;
		if (noDir) opts |= File.LIST_FILES_ONLY;
		all.addAll(currentDir.listMultiple(getCurrentMask(),opts));
	}
	if (!noDir && currentDir.getParent() != null){
		all.add(0,"..");
	}
	updateFileList(all,focusOnFile);
}

private MenuItem readingItem;

//-------------------------------------------------------------------
private MenuItem toReadingItem(String name, FontMetrics fm)
//-------------------------------------------------------------------
{
	if (readingItem == null){
		MenuItem mi = readingItem = new MenuItem(name);
		mi.image = new IconAndText(ImageCache.cache.get("ewe/WaitSmall.bmp",Color.White),name,fm);
		((IconAndText)mi.image).textColor = null;
	}
	return readingItem;
}
//-------------------------------------------------------------------
private MenuItem toMenuItem(String name, boolean isDir, FontMetrics fm)
//-------------------------------------------------------------------
{
	MenuItem mi = new MenuItem(name);
	if (isDir){
		if (name.equals("..")) mi.image = new IconAndText(Device.folderUp,"Parent Folder",fm);
		else mi.image = new IconAndText(File.getIcon(File.ClosedFolderIcon),name,fm);
	}else{
		mi.image = new IconAndText(File.getIcon(File.PageIcon),name,fm);
	}
	if (mi.image instanceof IconAndText)
		((IconAndText)mi.image).textColor = null;
	return mi;
}

//-------------------------------------------------------------------
protected void updateFileList(Vector all,boolean focusOnFile)
//-------------------------------------------------------------------
{
	currentFiles = all;
	if (fileList == null) return;
	fileList.items.clear();
	if (all.size() == 0){
		fileList.items.add("(No files)");
	}else if (all == readingDir){
		fileList.items.add(toReadingItem(all.get(0).toString(),getFontMetrics()));
	}else{
		FontMetrics fm = getFontMetrics();
		File fc = currentDir.getNew("");
		for (int i = 0; i<all.size(); i++){
			String fn = all.get(i).toString();
			fc.set(currentDir,fn);
			if (fc.isDirectory() || !noExt){
				fileList.items.add(toMenuItem(fn,fc.isDirectory(),fm));
			}else{
				fileList.items.add(toMenuItem(mString.leftOf(fn,'.'),fc.isDirectory(),fm));
			}
			//if (fc.isDirectory()) fileList.items.add("["+fn+"]");
			//else fileList.items.add(fn);
		}
	}
	fileList.select(-1,true);
	fileList.updateItems();
	if (isDirSelect) {
		int idx = all.size() == 0 ? -1 : 0;
		if (idx == 0){
			if (all.size() > 1 && all.get(0).toString().equals(".."))
				idx = 1;
		}
		fileList.select(idx,true);
	}
	if (fileList.makeVisible(0)) fileList.repaintNow();
	if (focusOnFile && !isDirSelect) focusOnFileName(true);
}

//-------------------------------------------------------------------
protected void focusOnFileName(boolean clearFirst)
//-------------------------------------------------------------------
{
	if (clearFirst) fileName.setText("");
	Gui.takeFocus(fileName,ByRequest);
	fileName.startActiveInput(true);
}
//-------------------------------------------------------------------
protected File getCurrentlySelectedFile()
//-------------------------------------------------------------------
{
	int which = fileList.selectedIndex;
	if (which >= 0 && which < currentFiles.size()){
		curFile.set(currentDir,currentFiles.get(which).toString());
		return curFile;
	}
	return null;
}
//-------------------------------------------------------------------
protected String getCurrentlySelectedFileName()
//-------------------------------------------------------------------
{
	int which = fileList.selectedIndex;
	if (which >= 0 && which < currentFiles.size())
		return currentFiles.get(which).toString();
	return null;
}

//-------------------------------------------------------------------
protected boolean fileChosenAction(String fileName)
//-------------------------------------------------------------------
{
	if (fileName.trim().length() == 0) return false;
	String name = fileName;
	if (name.equals("..")){
		File up = currentDir.getParentFile();
		if (up == null) return false;
		setDirectory(up,true);
		return false;
	}
	fileName.replace('\\','/');
	if (fileName.indexOf('/') != -1){
		int last = fileName.lastIndexOf('/');
		String dir = fileName.substring(0,last);
		name = fileName.substring(last+1);
		if (!noDir){
			curFile.set(currentDir,dir);
			if (curFile.isDirectory())
				setDirectory(curFile);
		}
	}
	if (name.indexOf('*') != -1 || name.indexOf('?') != -1){
		currentMask = name;
		setDirectory(currentDir);
		return false;
	}
	curFile.set(currentDir,fileName);
	return fileChosenAction(curFile);
}
/**
This is called if a particular file is chosen.
It returns true if the file was accepted and the FileChooser closed.
**/
//-------------------------------------------------------------------
protected boolean fileChosenAction(File f)
//-------------------------------------------------------------------
{
	if (f.isDirectory() && !noDir){
		setDirectory(f);
		return false;
	}
	//

	return tryAccept(f);
}
//-------------------------------------------------------------------
protected boolean tryAccept(File f)
//-------------------------------------------------------------------
{
	//
	// Directory Select should go direct to accept(f)
	//
	String fname = f.getName();
	//

	if (fname.indexOf('.') == -1){
		String defExt = fcp.getString(fcp.DEFAULT_EXTENSION,null);
		if (defExt != null){
			if (!defExt.startsWith(".")) defExt = "."+defExt;
			if (defExt != null) fname += defExt;
			f = f.getNew(f.getParentFile(),fname);
		}
	}
	//
	if (f.isDirectory() || (((options & fcp.OPTION_FILE_MUST_EXIST) != 0) && !f.exists())){
		Gui.flashMessage("No such file!",this);
		focusOnFileName(false);
		return false;
	}
	//
	if (type.equalsIgnoreCase(fcp.TYPE_SAVE) && f.exists() && ((options & fcp.OPTION_NO_CONFIRM_OVERWRITE) == 0)){
		MessageBox mb = new MessageBox("Overwrite File","Overwrite this file?\n\n"+f.getName(),MBYESNO);
		if (mb.execute() != IDYES) return false;
	}
	//
	// At this point, the chose file is acceptable.
	//
	accept(f);
	return true;
}
//-------------------------------------------------------------------
protected void accept(File f)
//-------------------------------------------------------------------
{
	fcp.set(fcp.CHOSEN_FILE,f);
	fcp.set(fcp.CHOSEN_FILES,new File[]{f});
	//ewe.sys.Vm.debug("Accept: "+f.toString());
	close(IDOK);
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DataChangeEvent){
		if (ev.target == folder){
			setDirectory(folder.getSelectedFolder());
		}else if (ev.target == fileList){
			File c = getCurrentlySelectedFile();
			if (c == null) return;
			if (!c.isDirectory()){
				String s = currentFiles.get(fileList.selectedIndex).toString();
				if (noExt) s = mString.leftOf(s,'.');
				fileName.setText(s);
			}else
				fileName.setText("");
			/* This was used when a combo was used for File name and File mask.
		}else if (ev.target == fileNameControl){
			if ((ev.flags & mInput.DATA_CHANGED_BY_ENTER) != 0) return;
			fileChosenAction(fileName.getText());
			*/
		}else if (ev.target == maskChoice){
			currentMask = maskChoice.getText();
			setDirectory(currentDir,false);
		}else if (ev.target == locationChoice){
			FileChooserLink fcl = (FileChooserLink)((MenuItem)locationChoice.getSelectedItem()).data;
			if (fcl != null) setDirectory(fcl.toFile(currentDir),false);
		}
	}else if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED){
		if (ev.target == fileList){
			String nm = getCurrentlySelectedFileName();
			if (nm.equals("..")) fileChosenAction(nm);
			else{
				File f = getCurrentlySelectedFile();
				if (f == null) return;
				if (f.isDirectory()) setDirectory(f,true);
				else fileChosenAction(f);
			}
		}else if (ev.target == fileNameControl){
			fileChosenAction(fileName.getText());
		}else if (ev.target == ok){
			if (isDirSelect) accept(currentDir);
			else fileChosenAction(fileName.getText());
			return; // Must not call super.onEvent for this.
		}
	}
	super.onEvent(ev);
}
//===================================================================
public SimpleFileChooser()
//===================================================================
{
	FileChooserParameters fcp = new FileChooserParameters();
	//fcp.set(fcp.TYPE,fcp.TYPE_DIRECTORY_SELECT);
	//fcp.setInt(fcp.OPTIONS,fcp.OPTION_SIMPLE);
	fcp.set(fcp.DEFAULT_EXTENSION,"tif");
	fcp.set(fcp.TYPE,fcp.TYPE_SAVE);
	//fcp.add(fcp.FILE_MASK,"*.tif - Tiff Files");
	//fcp.add(fcp.FILE_MASK,"*.dll - DLL Files");
	//fcp.add(fcp.FILE_MASK,"*.exe - EXE Files");
	fcp.set(fcp.START_LOCATION,"/Projects/ewe/classes");
	fcp.add(fcp.LOCATION,new FileChooserLink("C:\\Bat",null));
	fcp.add(fcp.LOCATION,new FileChooserLink("f:\\Projects",null));
	init(fcp);
}
//##################################################################
}
//##################################################################

