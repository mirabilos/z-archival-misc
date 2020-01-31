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
import ewe.util.Vector;
import ewe.util.Range;
import ewe.util.Intable;
import ewe.sys.Convert;
import ewe.sys.mThread;

//##################################################################
public class FontUpDownChooser extends Holder{
//##################################################################

public static final int OPTION_NO_NAME = 0x1;
public static final int OPTION_NO_SIZE = 0x2;
public static final int OPTION_NO_STYLE = 0x4;
public static final int OPTION_NO_SAMPLE = 0x8;

//public String name;
//public int size;
//public int style;

private UpDownInput nameChooser, sizeChooser, styleChooser;
private mLabel sample;

private static String sampleText = "Sample Text";
private static String[] styles = {"Plain","Bold","Italic","B & I"};
private static int[] fontStyles = {Font.PLAIN,Font.BOLD,Font.ITALIC,Font.BOLD|Font.ITALIC};

//===================================================================
public static String getStyleName(int fontStyle)
//===================================================================
{
	return styles[getStyleIndex(fontStyle)];
}

//-------------------------------------------------------------------
private static int getStyleIndex(int fontStyle)
//-------------------------------------------------------------------
{
	switch(fontStyle & (Font.BOLD|Font.ITALIC)){
		case (Font.BOLD|Font.ITALIC): return 3;
		case Font.ITALIC: return 2;
		case Font.BOLD: return 1;
		default: return 0;
	}
}
//===================================================================
public Font toFont()
//===================================================================
{
	return new Font(nameChooser.getText(),fontStyles[styleChooser.getInt()],sizeChooser.getInt());
}
//===================================================================
public void fromFont(Font f)
//===================================================================
{
	nameChooser.setText(f.getName());
	sizeChooser.setInt(f.getSize());
	styleChooser.setInt(getStyleIndex(f.getStyle()));
	//ewe.sys.Vm.debug("FS: "+style);
	sample.setFont(f);
	sample.repaintNow();
}
//===================================================================
public FontUpDownChooser(int options)
//===================================================================
{
	CellPanel cp = new CellPanel();
	nameChooser = new UpDownInput(30);
	if ((options & OPTION_NO_NAME) == 0)
		cp.addNext(nameChooser).setControl(HFILL|CENTER).setTag(INSETS,new Insets(0,0,0,2));
	//addField(nameChooser,"name");
	nameChooser.textValues = new Vector(Font.listFonts(mApp.mainApp));
	nameChooser.dataChangeOnEachPress = true;
	sizeChooser = new UpDownInput(2);
	if ((options & OPTION_NO_SIZE) == 0)
		cp.addNext(sizeChooser).setCell(DONTSTRETCH).setControl(HFILL).setTag(INSETS,new Insets(0,0,0,2));
	//addField(sizeChooser,"size");
	sizeChooser.integerValues = new Range(4,99);
	sizeChooser.wrapAround = false;
	sizeChooser.dataChangeOnEachPress = true;
	sizeChooser.integerDigits = 2;
	styleChooser = new UpDownInput(6);
	if ((options & OPTION_NO_STYLE) == 0)
		cp.addNext(styleChooser).setCell(DONTSTRETCH).setControl(HFILL|CENTER);
	//addField(styleChooser,"style");
	styleChooser.dataChangeOnEachPress = true;
	styleChooser.textValues = new Vector(styles);
	styleChooser.zeroIndexedText = true;
	addLast(cp);//.setCell(HSTRETCH);
	sample = new mLabel(sampleText);
	sample.setTextSize(15,3);
	sample.anchor = CENTER;
	sample.backGround = Color.White;
	Font f = mApp.mainApp.getFont();
	fromFont(f);
	//addLast(sample);
}

//===================================================================
public FontUpDownChooser()
//===================================================================
{
	this(0);
}
//
private static int lastPopupOptions;
private static ControlPopupForm popup;
//
//===================================================================
public static ControlPopupForm getPopup(int options)
//===================================================================
{
	if (popup != null && lastPopupOptions == options) return popup;
	return popup = new FontUpDownChooserPopup(lastPopupOptions = options);
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DataChangeEvent){
		sample.setFont(toFont());
		sample.repaintNow();
	}
	super.onEvent(ev);
}
//##################################################################
public static class FontUpDownChooserPopup extends ControlPopupForm{
//##################################################################
FontUpDownChooser input;
//===================================================================
public FontUpDownChooserPopup(int options)
//===================================================================
{
	//putByClient = false;
	setBorder(BDR_OUTLINE|BF_RECT,1);
	backGround = Color.White;
	CellPanel cp = new CellPanel();
	cp.addNext(input = new FontUpDownChooser(options)).setCell(HSTRETCH);
	addCloseControls(cp);
	addLast(cp);
	if ((options & OPTION_NO_SAMPLE) == 0) {
		endRow();
		addLast(input.sample);
	}
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.target == input){
		if (ev.type == ev.PRESSED)
			exit(IDOK);
		else if (ev.type == ev.CANCELLED)
			exit(IDCANCEL);
	}
	else super.onControlEvent(ev);
}

//-------------------------------------------------------------------
protected void transferToClient(Control client)
//-------------------------------------------------------------------
{
	if (client instanceof FontInput)
		((FontInput)client).fromFont(input.toFont());
}
/**
* This is called by setFor(Control who) and gives you an opportunity to
* modify the Form based on the client control.
* @param who The new client control.
*/
//------------------------------------------------------------------
protected void startingInput(Control who)
//-------------------------------------------------------------------
{
	if (who instanceof FontInput)
		input.fromFont(((FontInput)client).toFont());
}
//##################################################################
}
//##################################################################

//##################################################################
}
//##################################################################

