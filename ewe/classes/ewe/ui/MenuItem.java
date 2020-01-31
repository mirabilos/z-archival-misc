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
public class MenuItem{
//##################################################################

/**
* The displayed label for the MenuItem.
**/
public String label = "";
/**
* The action String for the MenuItem. Unless you set it to be something different
* it is usually set to be the same as the label.
**/
public String action = null;
/**
* Modifiers for the MenuItem.
**/
public int modifiers;
/**
* This can be set to be a sub-menu for this item.
**/
public Menu subMenu;
/**
* An optional ID for the item.
**/
public int id;
/**
* If this is not null then this will be displayed instead of the label. If you want
* to display text with an icon, use an IconAndText image.
**/
public IImage image;
/**
* An optional hotkey for the MenuItem.
**/
public int hotkey;
/**
* Optional data to associate with the MenuItem.
**/
public Object data;
/**
* This is -1 by default, which means that the index level should be calculated
* automatically. In order for this to have any effect the ChoiceControl using it
* must have indentDropItems true.
**/
public int indentLevel = -1;
/**
* A MenuItem modifier.
**/
public static int Disabled = 0x1;
/**
* A MenuItem modifier.
**/
public static int Separator = 0x2;
/**
* A MenuItem modifier.
**/
public static int Checked = 0x4;
/**
* A MenuItem modifier.
**/
public static int Selected = 0x8;

//===================================================================
public MenuItem(){}
//===================================================================
/**
* This creates the menu item with the action and label being equal to text. The
* text may have a hot key encoded within it.
* @param text The action and label for the menu item. This may have a hotkey embedded in it.
* @param mods Optional modifiers for the item.
* @param sub An optional sub-menu for the item.
*/
//===================================================================
public MenuItem(String text,int mods,Menu sub)
//===================================================================
{
	action = label = Gui.getTextFrom(text);
	hotkey = Gui.getHotKeyFrom(text);
	if (label.equals("-")) modifiers = Separator;
	modifiers |= mods;
	subMenu = sub;
}
/**
* Set the action, label and hotkey of the MenuItem to be the specified parameter,
* allowing the parameter to have a Hot Key encoded in it.
* @param labelAndAction The action and label for the menu item. This may have a hotkey embedded in it.
* @return this MenuItem
*/
//===================================================================
public MenuItem set(String labelAndAction)
//===================================================================
{
	action = label = Gui.getTextFrom(labelAndAction);
	hotkey = Gui.getHotKeyFrom(labelAndAction);
	return this;
}
/**
* Set the action and label of the MenuItem to be the specified parameter,
* assuming that the parameter is pure text only with no Hot Key encoded in it.
* @param labelAndAction The action and label for the menu item. This may have a hotkey embedded in it.
* @return this MenuItem
**/
//===================================================================
public MenuItem setText(String labelAndAction)
//===================================================================
{
	action = label = labelAndAction;
	hotkey = 0;
	return this;
}
/**
* This creates the menu item with the action and label being equal to text.
* @param text The action and label for the menu item. This may have a hotkey embedded in it.
**/
//===================================================================
public MenuItem(String text)
//===================================================================
{
	this(text,0,null);
}
/**
* This creates the menu item with the action and label being equal to text. However
* the label will not be displayed if the image is not null. Use an IconAndText image to display
* an icon along with text.
* @param text The action and label for the menu item. This may have a hotkey embedded in it.
* @param image An image to use as the display for the item.
*/
//===================================================================
public MenuItem(String text,IImage image)
//===================================================================
{
	this(text);
	this.image = image;
	if (image instanceof IconAndText) {
		((IconAndText)image).textColor = null;
	}
}
/**
 * Set the label, hotkey and image for the item.
 * @param text The text for the label which may have a hotkey encoded within it.
 * If the action field is null when this is called it will also be set to the label.
 * @param image The image which may be a small icon to be associated with the text or
 * it may be the entire display line.
 * @param leaveText If this is true then it is assumed that the image is meant to be an
 * icon to be associated with the item.
 * @return this MenuItem.
 */
//===================================================================
public MenuItem iconize(String text,IImage image,boolean leaveText)
//===================================================================
{
	label = Gui.getTextFrom(text);
	if (action == null) action = label;
	hotkey = Gui.getHotKeyFrom(text);
	if (text != null && leaveText) {
		this.image = new IconAndText(image,Gui.makeHot(text),null);
		((IconAndText)this.image).textColor = null;
	}else
		this.image = image;
	return this;
}
/**
* This creates a MenuItem using the ImageCache to load an icon for the item to be associated
* with the text label.
* @param text The action and label for the menu item. This may have a hotkey embedded in it.
* @param imageName The name of an image.
* @param maskOrColor An optional mask or transparent color for the image.
**/
//===================================================================
public MenuItem(String text,String imageName,Object maskOrColor)
//===================================================================
{
	this(text,new IconAndText(ewe.fx.ImageCache.cache.get(imageName,maskOrColor),Gui.makeHot(text),null));
}
//===================================================================
public MenuItem(String label,String action)
//===================================================================
{
	this(label);
	this.action = action;
}
//===================================================================
public String toString() {return label;}
//===================================================================
public boolean equals(Object what)
//===================================================================
{
	if (what == null) return false;
	if (what instanceof MenuItem) return what == this;
	return label.equals(what.toString());
}
//##################################################################
}
//##################################################################

