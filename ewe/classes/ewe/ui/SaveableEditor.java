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
package ewe.ui;
import ewe.data.*;
import ewe.util.*;
import ewe.reflect.*;
import ewe.fx.*;
import ewe.io.*;

/**
* A SaveableEditor is designed to hold another editor and then allow you to open and
* save to disk the object being edited. There are two ways to use a SaveableEditor:
* <p>
* The simplest way is to use the constructor <b>SaveableEditor(LiveData ld,boolean isList)</b>.
* This constructor creates a SaveableEditor that has full file functionality using an editor
* returned by ld.getEditor(0) (the object's default editor). If <b>isList</b> is true, then a
* ListEditor will be used and you will be able to edit and save a complete list of the data.
* This method works in exactly the same way as if you had run the command line: <br>
* <b>ewe ewe.ui.SaveableEditor -list mypackage.MyObjectName</b>
* <p>
* The other way to use a SaveableEditor is to extend it and in the constructor, after calling the
* super class constructor, you do the following:
* <ul>
* <li>You place <b>either</b> the "fileButtons" bar in the editor <b>or</b> the "fileMenu" pull-down menu
* in the editor.
* <li>You set the default file extension in the file saver by calling <b>saver.setDefaultFileType("*.mask","Description")</b>
* or you replace the saver.
* </ul>
* <p>
* If you call <b>processArgs()</b> it will process the command line arguments, which allows for the automatic loading
* of a data value.</b>
**/
//##################################################################
public class SaveableEditor extends Editor{
//##################################################################

public LiveData model;
public Editor edited;
static boolean isBig;// = Gui.screenIs(Gui.BIG_SCREEN);

//-------------------------------------------------------------------
CellPanel addButton(String name,String action,String icon,CellPanel dest,Menu m)
//-------------------------------------------------------------------
{
	return addButton(name,action,ImageCache.cache.get("ewe/"+icon,Color.White),dest,m);
}
//-------------------------------------------------------------------
CellPanel addButton(String name,String action,IImage icon,CellPanel dest,Menu m)
//-------------------------------------------------------------------
{
	if (dest == null) {
		dest = new ButtonBar();
		dest.equalWidths = isBig;
		dest.modify(MouseSensitive,0);
	}
	mButton b = new mButton(name);
	addField(b,action);
	Gui.iconize(b,icon,true,null);
	MenuItem mi = new MenuItem().iconize(name,icon,true);
	mi.action = action;
	if (m != null) m.addItem(mi);
	dest.addNext(b);
	return dest;
}

public CellPanel fileButtons;
public PullDownMenu fileMenu;
public MenuItem fileItem = new MenuItem();


/**
* This gets added to the top of the editor, before the main editor. It is initially empty, you can add to it
* if you want.
**/
public CellPanel top = new CellPanel();

public CellPanel tools;
public CellPanel contents;

//-------------------------------------------------------------------
private SaveableEditor()
//-------------------------------------------------------------------
{
	boolean isList = false;
	LiveData ld = new LiveObject();
	for (int i = 0; i<mApp.programArguments.length; i++){
		String arg = mApp.programArguments[i];
		if (arg.equalsIgnoreCase("-list")) isList = true;
		else{
			Reflect r = Reflect.getForName(arg);
			if (r != null) {
				Object obj = r.newInstance();
				if (obj instanceof LiveData){
					ld = (LiveData)obj;
				}
			}
		}
	}
	defaultSetup(ld,isList);
}
//===================================================================
public void defaultSetup(LiveData ld,boolean isList)
//===================================================================
{
	if (isList){
		ListObject lo = new ListObject();
		lo.model = ld;
		ld = lo;
	}
	setup(ld,ld.getEditor(0));
	addFileControls(true);
	FileSaver saver = (FileSaver)getFileSaver();
	if (isList) saver.setDefaultFileType("list","Object List");
	else saver.setDefaultFileType("data","Object Data");
}
/**
* This is the constructor to use to produce a default implementation of SaveableEditor.
* This implementation provides functionality for New, Open, Save, Save As and Exit.
* After using this constructor you do not need to call any more SaveableEditor methods in order
* to create a useable editor.
* @param ld the LiveData object to edit.
* @param isList set this true if you wish to edit a list of the data instead of just one.
*/
//===================================================================
public SaveableEditor(LiveData ld,boolean isList)
//===================================================================
{
	defaultSetup(ld,isList);
}
/**
* This calls SaveableEditor(model, model.getEditor(whichEditor)). After calling this method
* you will have to add the fileMenu and/or the fileButtons as you need.
* @param model the LiveData object to edit.
* @param editor an editor for the model to use.
*/
//===================================================================
public SaveableEditor(LiveData model,int whichEditor)
//===================================================================
{
	this(model,model.getEditor(whichEditor));
}
/**
* This calls SaveableEditor(LiveData model,Editor editor) with a null editor.
* @param ld the LiveData object to edit.
*/
//===================================================================
public SaveableEditor(LiveData ld)
//===================================================================
{
	this(ld,null);
}
/**
* This method calls the setup(LiveData model,Editor editor) method. setup()
* will build the fileMenu menu and the fileButtons toolbar, but does not add them
* into the SaveableEditor - you must do that yourself. Additionally, the editor itself
* is not added into the SaveableEditor.
* @param model the LiveData object to edit.
* @param editor an editor for the model to use.
*/
//===================================================================
public SaveableEditor(LiveData model,Editor editor)
//===================================================================
{
	setup(model,editor);
}

/**
* This will build the fileMenu menu and the fileButtons toolbar, but does not add them
* into the SaveableEditor - you must do that yourself. Additionally, the editor itself
* is not added into the SaveableEditor.
* @param model the LiveData object to edit.
* @param editor an editor for the model to use.
*/
//-------------------------------------------------------------------
protected void setup(LiveData model,Editor editor)
//-------------------------------------------------------------------
{
	if (editor == null) editor = this;
	isBig = Gui.screenIs(Gui.BIG_SCREEN);
	this.model = model;
	this.edited = editor;
	editor.setBorder(mInput.inputEdge|BF_RECT,3);
	CellPanel [] both = addToolbar();

	both[1].addLast(top).setCell(HSTRETCH);
	if (editor != this) addField(both[1].addLast(editor),"editor");
	getProperties().set("EditorContents",both[1]);
	tools = both[0];

	resizable = editor.resizable;
	moveable = editor.moveable;
	title = editor.title;
	CellPanel cp = null;
	Menu m = new Menu();
 	cp = addButton("New$n","newObject","newsmall.bmp",cp,m);
 	cp = addButton("Open$o","openObject","opensmall.bmp",cp,m);
	m.addItem("-");
 	cp = addButton("Save$s","saveObject","savesmall.bmp",cp,m);
 	cp = addButton("Save as..$a","saveAsObject","saveassmall.bmp",cp,m);
	m.addItem("-");
 	cp = addButton("Exit$x","exit","exitsmall.bmp",cp,m);
	cp.borderWidth = 3;
	cp.borderStyle = BDR_NOBORDER;//EDGE_SUNKEN;
	fileButtons = cp;
	fileMenu = new ButtonPullDownMenu("File",m);
	addField(fileMenu,"fileItem");
	setFileSaver(new ewe.io.FileSaver(),true);
	//addLast(cp).setCell(HSTRETCH);
}

//public ewe.io.FileSaver saver = new ewe.io.FileSaver();

//===================================================================
public void processArgs()
//===================================================================
{
	if (mApp.programArguments.length > 0){
		new ewe.sys.TaskObject(){
			protected void doRun(){
				//ewe.sys.Vm.debug("Loading: "+mApp.programArguments[0]);
				FileSaver saver = (FileSaver)getFileSaver();
				saver.tryOpen(edited.myObject,mApp.programArguments[0],null);
				edited.toControls();
			}
		}.startTask();
	}
}
/*
//===================================================================
public void fieldChanged(String name,Editor ed)
//===================================================================
{
	FileSaver saver = getSaver();
	if (saver != null
	if (ft.name.equals("editor")){
		saver.hasChanged = true;
	}else if (ft.fieldName.equals("fileItem")){
		action(fileItem.action,ed);
	}
}
*/
/*
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor ed)
//===================================================================
{
	action(ft.fieldName,ed);
}
*/
//===================================================================
public void action(String name,Editor ed)
//===================================================================
{
	FileSaver saver = (FileSaver)getFileSaver();
	if (saver == null) return;

	if (name.equals("saveObject")){
		saver.save(false,edited.myObject,null);
	}else if (name.equals("saveAsObject")){
		saver.save(true,edited.myObject,null);
	}else if (name.equals("openObject")){
		if (saver.open(edited.myObject,edited.myObject,null))
			edited.toControls();
	}else if (name.equals("newObject")){
		Object obj = edited.myObject;
		if (obj instanceof DataUnit){
			Object newOne = ((DataUnit)obj).getNew();
			if (saver.newData(edited.myObject,null,null))
				edited.setObject(newOne);
		}
	}else if (name.equals("exit")){
		exit(0);
	}
}
/**
* This adds the File controls. If the screen is big the buttons will
* be added to whereIfBig, otherwise the menu will be added to whereIfSmall. If menuOnly
* is true then only the menu will be added at the whereIfBig location, not the buttons.
**/
//===================================================================
public void addFileControls(CellPanel whereIfBig,CellPanel whereIfSmall,boolean menuOnly)
//===================================================================
{
	if (Gui.screenIs(Gui.BIG_SCREEN)) whereIfBig.addNext(menuOnly ? (Control)fileMenu : (Control)fileButtons,HSTRETCH,DONTFILL|CENTER);
	else whereIfSmall.addNext(fileMenu,DONTSTRETCH,HFILL);
}
/**
* If the screen is big this will add the file buttons to either the top or bottom of the editor. If the screen
* is not big, the file menu will be added to the title bar.
**/
//===================================================================
public void addFileControls(boolean buttonsOnTop)
//===================================================================
{
	if (!Gui.screenIs(Gui.BIG_SCREEN)){
		if (titleControls == null) titleControls = new CellPanel();
		exitButtonDefined = true;
		windowFlagsToClear = Window.FLAG_HAS_TITLE;
	}
	addFileControls(buttonsOnTop ? top : (CellPanel)this,titleControls,false);
}
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	new SaveableEditor().execute();
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

