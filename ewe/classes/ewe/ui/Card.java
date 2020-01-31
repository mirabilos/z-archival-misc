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
/**
* A Card represents a Control that has been added to a Control that implements MultiPanel -
* a CardPanel, for example.
**/
//##################################################################
public class Card{
//##################################################################
/**
* This is the longName given to the item when it was added to the MultiPanel.
**/
public String longName;
/**
* This is the short (tab) name given to the item when it was added to the MultiPanel.
**/
public String tabName;
/**
* This is the CellPanel which contains the item which was added to the MultiPanel. If
* the item added already was a CellPanel, then this MAY be the added item.
**/
public CellPanel panel;
/**
* This is the item that was added to the MultiPanel.
**/
public Control item;
/**
* If this is not null then this will be displayed instead of the tab text. Use an IconAndText
* if you want to display both an Icon and text.
**/
public ewe.fx.IImage image;
/**
* If this is not null then it will be displayed when the card is <b>not</b> selected.
**/
public ewe.fx.IImage closedImage;
/**
* If this is not null then this will be the tool tip for the tab. Otherwise the tool tip will be the image
* (if there is one) or tab name.
**/
public Object tip;
/*
* These are flags that apply to the Card. Currently defined flags are DISABLED and HIDDEN.
*/
public int flags;
/**
* A flag.
**/
public static final int DISABLED = 0x1;
/**
* A flag.
**/
public static final int HIDDEN = 0x2;
/**
* A flag.
**/
public static final int CLOSEABLE = 0x4;
/**
* A flag.
**/
public static final int ALREADY_MADE = 0x8;

/**
 * Call this after you have specified the tab name. This will create an IconAndText image for the
 * tab and, tells the Card to
 * display an icon only when it is deslected ONLY if it is running on a PDA sized screen.
 * @param icon The icon to use with this tab.
* @return itself.
*/
//===================================================================
public Card iconize(ewe.fx.IImage icon)
//===================================================================
{
	return iconize(icon,Gui.screenIs(Gui.PDA_SCREEN));
}
/**
 * Call this after you have specified the tab name. This will create an IconAndText image for the
 * tab AND set the closedImage to be the icon.
 * @param icon The icon to use with this tab.
 * @param iconOnlyWhenClosed Indicates that only the icon should be displayed when the tab
	is not selected.
* @return itself.
*/
//===================================================================
public Card iconize(ewe.fx.IImage icon,boolean iconOnlyWhenClosed)
//===================================================================
{
	image = new ewe.fx.IconAndText(icon,tabName,null);
	((ewe.fx.IconAndText)image).textColor = ewe.fx.Color.Black;
	if (iconOnlyWhenClosed) closedImage = icon;
	else closedImage = null;
	return this;
}
/**
 * Call this after you have specified the tab name. This will create an IconAndText image for the
 * tab AND set the closedImage to be the icon. The image is retrieved from the ImageCache.
 * @param imageName The name of the icon.
 * @param maskOrColor A mask image or color value.
 * @param iconOnlyWhenClosed Indicates that only the icon should be displayed when the tab
	is not selected.
* @return itself.
 */
//===================================================================
public Card iconize(String imageName,Object maskOrColor,boolean iconOnlyWhenClosed)
//===================================================================
{
	return iconize(ewe.fx.ImageCache.cache.get(imageName,maskOrColor),iconOnlyWhenClosed);
}
/**
 * This iconizes the tab with the icon of the specified name and, tells the Card to
 * display an icon only when it is deslected ONLY if it is running on a PDA sized screen.
 * @param imageName The name of the icon.
 * @param maskOrColor A mask image or color value.
 * @return this Card.
 */
//===================================================================
public Card iconize(String imageName,Object maskOrColor)
//===================================================================
{
	return iconize(imageName,maskOrColor,Gui.screenIs(Gui.PDA_SCREEN));
}
/**
 * This iconizes the tab with the icon of the specified name and, tells the Card to
 * display an icon only when it is deslected ONLY if it is running on a PDA sized screen.
 * @return this Card.
 */
//===================================================================
public Card iconize(String imageName)
//===================================================================
{
	return iconize(imageName,null);
}
//##################################################################
}
//##################################################################

