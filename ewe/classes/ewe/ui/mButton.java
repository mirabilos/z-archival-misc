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
public class mButton extends ButtonControl{
//##################################################################
/**
* The position of the text relative to any image. Should be
* Graphics.Up or Graphics.Down
**/
public int textPosition = Graphics.Up;
/**
* If this is set, then the inside will be filled with this color.
**/
public Color insideColor = null;
private Color oldBackground;
private boolean switched = false;

/**
An optional action label for the button. When the button generates a ControlEvent.PRESSED event
this value will be placed in the action value of the event. If this is null, then the text of the
button is put instead.
**/
public String action;
/*
* The direction of any arrow. If it is zero there is no arrow.
**/
//===================================================================
public void gotFocus(int how)
//===================================================================
{
	if (useNativeTextInput && !switched){
			switched = true;
			oldBackground = insideColor;
			insideColor = Color.LightGreen;
	}
	super.gotFocus(how);
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	if (switched){
		insideColor = oldBackground;
		switched = false;
	}
	super.lostFocus(how);
}

{
	borderWidth = 1;
	startDragResolution = 10;
}
//==================================================================
public mButton(){this("");}
public mButton(IImage image) {text = ""; this.image = image;}
public mButton(String txt)
{
	if (txt == null) txt = "";
	text = Gui.getTextFrom(txt);
	setHotKey(0,Gui.getHotKeyFrom(txt));
}

/**
* This creates an mButton using the ImageCache to load an icon for the button.
**/
//===================================================================
public mButton(String text,String imageName,Object maskOrColor)
//===================================================================
{
	image = ImageCache.cache.get(imageName,maskOrColor);
	if (text != null) {
		setHotKey(0,Gui.getHotKeyFrom(text));
		image = new IconAndText(image,makeHot(Gui.getTextFrom(text)),null);
		((IconAndText)image).textColor = null;
	}
}
protected ButtonObject buttonObject;
//===================================================================
public void make(boolean remake)
//===================================================================
{
	if (buttonObject == null) buttonObject = new ButtonObject(this);
}
//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	Dimension d = buttonObject.calculateSize(new Dimension());
	preferredWidth = d.width;
	preferredHeight = d.height;
}
//==================================================================
public static Color getImageColor(Color fore,Color back,boolean isEnabled,boolean flat,boolean pressState)
//==================================================================
{
	if (!isEnabled) return Color.DarkGray;
	if (!pressState || !flat) return fore;
	return back;
}
//==================================================================
public Color getImageColor()
//==================================================================
{
	int flags = getModifiers(true);
	return getImageColor(getForeground(),getBackground(),((((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0)) && (((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))),((flags & DrawFlat) != 0),pressState);
}
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	buttonObject.update(this);
	buttonObject.paint(g);
}
//##################################################################
}
//##################################################################



