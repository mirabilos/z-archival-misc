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
import ewe.reflect.Wrapper;

//##################################################################
public class FontChooser extends ewe.data.LiveObject{
//##################################################################

/**
* If this is set true, then the text in the drop-down menu for Fonts will
* be in the same Font instead of being rendered in their own face.
**/
public static boolean dontShowDifferentFonts;

public static String [] fonts;
protected static ewe.util.Vector fontItems;
public int size = 14;
public String font = "Helvetica";
public boolean bold = false;
public boolean italic = false;
public boolean underline = false;
public boolean chooseStyle = false;
public boolean chooseSize = true;
public boolean chooseName = true;
//public String _fields = "size,font,bold,italic,chooseStyle,underline";

//===================================================================
public FontChooser() {this(true);}
//===================================================================

//===================================================================
public FontChooser(boolean chooseStyle)
//===================================================================
{
	this.chooseStyle = chooseStyle;
}
//===================================================================
public boolean _getSetField(String fieldName, Wrapper wrapper, boolean isGet)
//===================================================================
{
	if (fieldName.equals("theFont")){
		if (isGet) wrapper.setObject(toFont());
		else fromFont((Font)wrapper.getObject());
		return true;
	}else
		return super._getSetField(fieldName,wrapper,isGet);
}

//===================================================================
protected ScrollablePanel getEditorScroller(CellPanel cp)
//===================================================================
{
	return null;
}
//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int which)
//===================================================================
{

	if (Gui.isSmartPhone){
		int options = 0;
		if (!chooseStyle) options |= FontInput.OPTION_NO_STYLE;
		if (!chooseSize) options |= FontInput.OPTION_NO_SIZE;
		if (!chooseName) options |= FontInput.OPTION_NO_NAME;
		ed.addField(cp.addLast(new FontInput(options)),"theFont$Lewe/fx/Font;");
		return;
	}

	if (fonts == null){
		fonts = Font.listFonts(mApp.mainApp);
		ewe.util.Utils.sort(fonts,ewe.sys.Vm.getLocale().getStringComparer(ewe.sys.Locale.IGNORE_CASE),false);
		fontItems = new ewe.util.Vector(fonts);
		//
		if (!dontShowDifferentFonts){
			fontItems.clear();
			Font gf = mApp.guiFont;
			int size = gf.getSize();
			for (int i = 0; i<fonts.length; i++){
				Font f = new Font(fonts[i],Font.PLAIN,size);
				FontMetrics fm = new FontMetrics(f,mApp.mainApp);
				IImage ln = new IconAndText(null,fonts[i],fm);//fonts[i],fm);
				fontItems.add(new MenuItem(fonts[i],ln));
			}
		}
	}
	mComboBox mcb = new mComboBox();
	mcb.input.columns = 20;
	mcb.input.prompt = "Font Name";
	mcb.choice.items = fontItems;//new ewe.util.Vector(fonts);
	ed.addField(cp.addNext(mcb),"font").setCell(cp.HSTRETCH);
	if (chooseSize){
		mcb = new mComboBox(new String[]{"8","10","11","12","14","16","20"},0);
		mcb.input.columns = 3;
		mcb.input.prompt = "Font Size";
		ed.addField(cp.addNext(mcb),"size").setCell(cp.DONTSTRETCH);
	}
	if (chooseStyle){
		CellPanel style = new CellPanel(); style.equalWidths = true;
		ed.addField(style.addNext(new ButtonCheckBox("B")),"bold");
		ed.addField(style.addNext(new ButtonCheckBox("I")),"italic");
		ed.addField(style.addNext(new ButtonCheckBox("U")),"underline");
		cp.addNext(style).setCell(cp.DONTSTRETCH).setTag(cp.INSETS,new ewe.fx.Insets(0,4,0,0));
	}
	if (chooseSize){
		CellPanel sz = new CellPanel(); sz.equalWidths = true;
		ed.addField(sz.addNext(new mButton("+")),"increase");
		ed.addField(sz.addNext(new mButton("-")),"decrease");
		cp.addNext(sz).setCell(cp.DONTSTRETCH).setTag(cp.INSETS,new ewe.fx.Insets(0,4,0,0));
	}
}
//===================================================================
public void fromFont(Font f)
//===================================================================
{
	if (f == null) return;
	int style = f.getStyle();
	bold = (style & Font.BOLD) != 0;
	italic = (style & Font.ITALIC) != 0;
	underline = (style & Font.UNDERLINE) != 0;
	font = f.getName();
	size = f.getSize();
}
//===================================================================
public Font toFont()
//===================================================================
{
	int style = Font.PLAIN;
	if (bold) style |= Font.BOLD;
	if (italic) style |= Font.ITALIC;
	if (underline) style |= Font.UNDERLINE;
	return new Font(font,style,size);
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("increase")){
		size++;
	}else if (fieldName.equals("decrease")){
		size--;
	}else {
		super.action(fieldName,ed);
	}
	ed.toControls("size");
	ed.notifyDataChange();
}
//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
/*
	if (fieldName.equals("font") && !dontShowDifferentFonts){
			mComboBox mc  = (mComboBox)ed.findFieldTransfer(fieldName).dataInterface;
			mc.input.font = new Font(font,Font.PLAIN,size);
			mc.input.repaintNow();
	}
*/
	super.fieldChanged(fieldName,ed);
}
//##################################################################
}
//##################################################################

