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
import ewe.fx.*;

//##################################################################
public class Notepad extends AppForm{
//##################################################################

public String theText = "";
/*
new String(new char[]{
0xc0, 0xd0, 0xe0, 0xf0
});
*/
public FontChooser fontChooser = new FontChooser();
public mTextPad textPad;
public ewe.io.FileSaver saver = new ewe.io.FileSaver();

static String [] toolImages;
public boolean autoWrap = true;
Menu fileMenu;
//===================================================================
public Notepad()
//===================================================================
{
	this(new mTextPad(20,60));
}
//===================================================================
public Notepad(final mTextPad textPad)
//===================================================================
{
	super(true,true);
	this.textPad = textPad;
	textPad.dontWantPopup = true;
	saver.textCodec = new ewe.io.JavaUtf8Codec(ewe.io.JavaUtf8Codec.STRIP_CR);
	saver.dontAppendLineFeed = true;
	acceptsDroppedFiles = true;
	resizeOnSIP = true;
	title = "Ewe Notepad";
	saver.setDefaultFileType("txt","Text Files.");
	textPad.font = mApp.findFont("fixed");
	textPad.minYScroll = 0;
	textPad.inputState |= textPad.STATE_AUTOTAB;
	addField(textPad,"theText");
	//textPad.modify(DisplayOnly,0);
	ScrollBarPanel sp = new ScrollBarPanel(textPad);
	//sp.setScrollBarSize(40,40,30);
	data.addLast(sp);
	MenuBar mb = menus;
	Menu m = setupStandardFileCommands(SHOW_NEW_BUTTON|SHOW_OPEN_BUTTON|SHOW_SAVE_BUTTON,tools,"Text Document");
	//Menu m2 = new Menu(new String[]{"One","Two"},"What"); m.addItem(m2);
	PullDownMenu pdm = mb.addMenu(m,"File");
	m = new Menu(textPad.getTextPadMenu(null));
	pdm = new PullDownMenu("Edit",m){
		//===================================================================
		public boolean checkMenu(Menu m)
		//===================================================================
		{
			return textPad.checkClipboardOperations(m);
		}
		//===================================================================
		public void popupMenuEvent(Object selectedItem)
		//===================================================================
		{
			textPad.popupMenuEvent(selectedItem);
			//textPad.takeFocus(ByRequest);
		}
		};
	mb.addMenu(pdm);
	tools.modifyAll(NoFocus|MouseSensitive,TakesKeyFocus,true);
	if (true){
		CellPanel tl = new CellPanel();
		tl.addLast(addField(fontChooser.getEditor(0),"fontChooser")).setCell(HSHRINK);
		tl.addLast(addField(new mCheckBox("Auto-wrap Lines"),"autoWrap")).setCell(HSHRINK);
		tabs.addCard(tl,"Tools",null).iconize("ewe/SmallConfig.bmp","ewe/SmallConfigMask.bmp");
	}
	//firstFocus = textPad;
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	fontChooser.fromFont(textPad.getFont());
	super.make(reMake);
}
//===================================================================
public boolean checkChange()
//===================================================================
{
	if (textPad.hasChanged()) {
		fromControls("theText");
		saver.hasChanged = true;
		textPad.updateData();
	}
	return saver.hasChanged;
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	checkChange();
	if (fieldName.equals("New")){
		if (saver.checkSave(theText,getFrame())){
			theText = "";
			saver.newData(null);
			toControls("theText");
		}
	//}else if (fieldName.equals("Extra")){
		//fileMenu.addItem(new MenuItem("Another One"));
	}else if (fieldName.equals("Exit")){
			exit(0);
	}else if (fieldName.equals("Save As")){
		saver.save(true,theText,getFrame());
	}else if (fieldName.equals("Save")){
		saver.save(false,theText,getFrame());
	}else if (fieldName.equals("Open")){
		String got = saver.open(theText,getFrame());
		if (got == null) return;
		theText = got;
		toControls("theText");
	}
}

public boolean canExit(int code)
{
	checkChange();
	if (!saver.checkSave(theText,getFrame())) return false;
	return super.canExit(code);
}


//===================================================================
public boolean open(String fileName)
//===================================================================
{
	String got = saver.openText(fileName,getFrame());
	if (got == null) return false;
	theText = got;
	toControls("theText");
	return true;
}
//===================================================================
public void update()
//===================================================================
{
		textPad.update();
		textPad.repaintNow();
}
//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("theText"))
		saver.hasChanged = true;
	else if (fieldName.equals("autoWrap")){
		textPad.wrapToScreenSize = autoWrap;
		update();
	}else if (fieldName.equals("fontChooser")){
		textPad.font = fontChooser.toFont();
		update();
	}
}
//===================================================================
public void filesDropped(String[] fileName)
//===================================================================
{
	ewe.sys.Vm.debug("FD: "+fileName);
	checkChange();
	if (!saver.checkSave(theText,getFrame())) return;
	open(fileName[0]);
}
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	String toOpen = null;
	if (args.length != 0) toOpen = args[0];
	Notepad np = new Notepad();
	np.exitSystemOnClose = true;
	np.show();
	if (toOpen != null) np.open(args[0]);
}
//##################################################################
}
//##################################################################

