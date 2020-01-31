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
package ewe.fx;
import ewe.ui.mApp;
import ewe.reflect.WeakReference;
import ewe.util.Enumeration;
import ewe.util.mString;
/**
IconAndText is a special type of IImage that draws an Icon and then a line of text. It
is used by a number of UI objects, including Menu, buttons, etc.<p>
IconAndText also supports multiple images and text placed at a particular position
along the width of the image. This is used by some controls to display images and text
lined up in columns (e.g. the ewe.filechooser.FileChooser).<p>
IconAndText objects are mutable/re-usable. You can change the icon and text and add and
remove columns dynamically.<p>
When using the IconAndText in column mode, the background of the image is drawn using
the background of the destination graphics if the background field is null. If the
destination graphics background is null, then the background will not be drawn.
**/

//##################################################################
public class IconAndText implements OnScreenImage, ImageRefresher{
//##################################################################
/**
* The icon being used.
**/
public IImage icon;
/**
* The text being used.
**/
public String text;
/**
* The text being used if it consists of multiple lines.
**/
public String[] lines;
/**
* The FontMetrics being used.
**/
public FontMetrics fontMetrics;
/**
* The textPosition which can be Graphics.Right, Left, Up or Down.
* When a textPosition of Up or Down is used, you cannot use the addColumns methods.
**/
public int textPosition = Graphics.Right;
/**
* If multiple lines are used for the text this denotes how those lines
* are aligned within the space allocated for the text. By default this is Graphics.Center
* but you could also make it Graphics.LEFT or Graphics.RIGHT;
**/
public int multiLineTextAlignment = Graphics.CENTER;
/**
* A background color. Defaults to null.
**/
public Color background = null;
/**
* If this is null, then the text will be drawn in the foreground color of the destination Graphics context.
**/
public Color textColor = null;
/**
* This is the width of the IconAndText
**/
public int width;
/**
* This is the height of the IconAndText
**/
public int height;
/**
* This is the height of the text.
**/
public int textHeight;
/**
* This is the height of the text.
**/
public int textWidth;

/**
* This always returns null.
**/
public int [] getPixels(int[] dest,int offset,int x,int y,int width,int height,int options)
{return null;}
/**
* This always returns false.
**/
public boolean usesAlpha(){return false;}


private WeakReference refresher;

//===================================================================
public void setRefresher(ImageRefresher refresher)
//===================================================================
{
	this.refresher = new WeakReference(refresher);
}
//===================================================================
public ImageRefresher getRefresher()
//===================================================================
{
	return refresher == null ? null : (ImageRefresher)refresher.get();
}
//===================================================================
public boolean changeRefresher(ImageRefresher newRefresher, ImageRefresher oldRefresher)
//===================================================================
{
	if (getRefresher() != oldRefresher) return false;
	setRefresher(newRefresher);
	return true;
}
//===================================================================
public void refresh(IImage icon, int options)
//===================================================================
{
	ImageRefresher ir = getRefresher();
	//System.out.println(this+" - IR: "+ir);
	if (ir == null) return;
	ir.refresh(this,0);
}
/*
protected void finalize()
{
	System.out.println("FI: "+this);
}
*/
//-------------------------------------------------------------------
void unRefreshAll()
//-------------------------------------------------------------------
{
	if (icon instanceof OnScreenImage)
		((OnScreenImage)icon).changeRefresher(null,this);
	for (int i = 0; i<numColumns; i++){
			TextEntry te = (TextEntry)columns.get(i);
			if (te.textOrIcon instanceof OnScreenImage)
				((OnScreenImage)te.textOrIcon).changeRefresher(null,this);
	}
}
//-------------------------------------------------------------------
void added(IImage add)
//-------------------------------------------------------------------
{
	if (add instanceof OnScreenImage)
		((OnScreenImage)add).setRefresher(this);

}
//===================================================================
public IconAndText()
//==================================================================
{
}
/**
 * Create an IconAndText for the specified icon and text.
 * @param icon The image to use for the icon.
 * @param text The text to display with the icon.
 * @param fontMetrics optional FontMetrics and Font to be used with the icon.
 */
//===================================================================
public IconAndText(IImage icon,String text,FontMetrics fontMetrics,int textPosition)
//===================================================================
{
	this();
	set(icon,text,fontMetrics,textPosition);
}
/**
 * Create an IconAndText for the specified icon and text.
 * @param icon The image to use for the icon.
 * @param text The text to display with the icon.
 * @param fontMetrics optional FontMetrics and Font to be used with the icon.
 */
//===================================================================
public IconAndText(IImage icon,String text,FontMetrics fontMetrics)
//===================================================================
{
	this(icon,text,fontMetrics,Graphics.Right);
}
/**
 * Create an IconAndText using the ImageCache.
 * @param text The text for the IconAndText
 * @param iconName The name of the saved image.
 * @param maskOrColor A name of a mask image or a color mask, or null.
 * @param fm optional FontMetrics to be used.
 */
//===================================================================
public IconAndText(String text,String iconName,Object maskOrColor,FontMetrics fm)
//===================================================================
{
	this(ImageCache.cache.get(iconName,maskOrColor),text,fm);
}
/**
 * Set the text and background colors.
 * @return this IconAndText.
 */
//===================================================================
public IconAndText setColor(Color textColor,Color backgroundColor)
//===================================================================
{
	this.textColor = textColor;
	this.background = backgroundColor;
	return this;
}
/**
Change the text alignment parameters and recalculate the IconAndText size.
**/
//===================================================================
public void changeTextPosition(int textPosition, int multiLineTextAlignment)
//===================================================================
{
	if (this.textPosition == textPosition && this.multiLineTextAlignment == multiLineTextAlignment) return;
	this.textPosition = textPosition;
	this.multiLineTextAlignment = multiLineTextAlignment;
	resetSize();
}
/**
Change the Font used by the IconAndText and recalculate its size.
**/
//===================================================================
public void changeFontMetrics(FontMetrics fontMetrics)
//===================================================================
{
	if (this.fontMetrics != null && this.fontMetrics.equals(fontMetrics)) return;
	this.fontMetrics = fontMetrics;
	resetSize();
}
//-------------------------------------------------------------------
void resetSize()
//-------------------------------------------------------------------
{
	lines = null;
	if (text != null && text.indexOf('\n') != -1)
		lines = mString.split(text,'\n');
	Dimension size = new Dimension();
	FontMetrics fm = fontMetrics;
	if (fm == null) fm = mApp.mainApp.getFontMetrics();
	textHeight = textWidth = 0;
	if (lines != null)
		Graphics.getSize(fm,lines,0,lines.length,size,null);
	else if (text != null)
		Graphics.getSize(fm,text,size);
	textHeight = size.height;
	textWidth = size.width;
	if (textPosition == Graphics.Up || textPosition == Graphics.Down){
		if (icon != null) {
			size.height += 2;
			size.height += icon.getHeight();
			size.width = Math.max(icon.getWidth(),size.width);
		}
	}else{
		if (icon != null) {
			size.width += 2;
			size.width += icon.getWidth();
			size.height = Math.max(icon.getHeight(),size.height);
		}
	}
	width = size.width;
	height = size.height;
}

private int numColumns = 0;
private ewe.util.Vector columns;

	//##################################################################
	class TextEntry{
	//##################################################################

	Object textOrIcon;
	int length;
	int anchor;

	TextEntry(Object t,int l,int a){set(t,l,a);}
	void set(Object t,int l,int a){textOrIcon = t; length = l; anchor = a;}
	//##################################################################
	}
	//##################################################################


/**
 * This can be used to change the icon and text, clearing any extra column data if any
 * is present. The original Font and text position is used.
 * @param icon The new icon to use.
 * @param text The new text to use.
 */
//===================================================================
public void set(IImage icon,String text)
//===================================================================
{
	clear();
	this.icon = icon;
	this.text = text;
	resetSize();
	added(icon);
}
/**
 * This can be used to reset the IconAndText to new icon and text.
 * is present. The original Font and text position is used.
 * @param icon The new icon to use.
 * @param text The new text to use.
 */
//===================================================================
public void set(IImage icon,String text,FontMetrics fontMetrics,int textPosition)
//===================================================================
{
	clear();
	this.icon = icon;
	this.text = text;
	//if (fontMetrics == null) fontMetrics = Application.mainApp.getFontMetrics();
	this.fontMetrics = fontMetrics;
	this.textPosition = textPosition;
	textColor = Color.Black;
	resetSize();
	added(icon);
}

/**
 * Set the icon and text to null and clear all column information.
 */
//===================================================================
public void clear() {icon = null; text = null; clearColumns();}
//===================================================================
/**
 * Clear all column information for this IconAndText.
 */
//===================================================================
public synchronized void clearColumns()
//===================================================================
{
	unRefreshAll();
	numColumns = 0;
	resetSize();
		if (columns != null)
			for (Enumeration e = columns.elements(); e.hasMoreElements();){
				TextEntry te = (TextEntry)e.nextElement();
				te.textOrIcon = null;
			}
}
/**
 * This adds a new column of data along the line. It is added to the right of
 * the previous column or original text and icon.
 * @param textOrIcon either a String or an IImage to display in this column.
 * @param width The width of this new column. If this is -1 then the width will be calculated.
 * @param anchor The anchor (either Graphics.RIGHT or LEFT OR'ed with TOP or BOTTOM) for the text or image.
 */
//===================================================================
public void addColumn(Object textOrIcon,int width,int anchor)
//===================================================================
{
	addColumn(textOrIcon,width,anchor,-1);
}
/**
 * This adds a new column of data along the line. It is added to the right of
 * the previous column or original text and icon.
 * @param textOrIcon either a String or an IImage to display in this column.
 * @param width The width of this new column. If this is -1 then the width will be calculated.
 * @param anchor The anchor (one of Graphics.RIGHT or LEFT OR'ed with TOP or BOTTOM) for the text or image.
 * @param addIndex The index at which the column will be inserted, which may be -1 to
 * indicate that it should be added to the end.
 */
//===================================================================
public synchronized void addColumn(Object textOrIcon,int width,int anchor,int addIndex)
//===================================================================
{
	//
	// If icon and text are set then add them as columns.
	//
	if (icon != null || text != null){
		IImage ic = icon; icon = null;
		String tx = text; text = null;
		if (ic != null) addColumn(ic,ic.getWidth()+2,Graphics.WEST);
		if (tx != null) addColumn(tx);
	}
	FontMetrics fm = fontMetrics;
	if (fm == null) fm = mApp.mainApp.getFontMetrics();
	if (addIndex < 0) addIndex = numColumns;
	if (columns == null) columns = new ewe.util.Vector();
	numColumns++;
	Dimension size = new Dimension();
	if (textOrIcon instanceof String)
		Graphics.getSize(fm,(String)textOrIcon,size);
	else{
		size.width = ((IImage)textOrIcon).getWidth();
		added((IImage)textOrIcon);
	}
	if (width < 0) width = size.width;
	int sz = columns.size();
	if (addIndex == numColumns-1){
		if (numColumns > sz) columns.add(new TextEntry(textOrIcon,width,anchor));
		else ((TextEntry)columns.get(numColumns-1)).set(textOrIcon,width,anchor);
	}else{
		if (numColumns < sz){
			TextEntry te = (TextEntry)columns.get(sz-1);
			te.set(textOrIcon,width,anchor);
			columns.removeElementAt(sz-1);
			columns.add(addIndex,te);
		}else
			columns.add(addIndex,new TextEntry(textOrIcon,width,anchor));
	}
	if (textOrIcon instanceof String){
		this.width += width;
		height = Math.max(size.height,height);
		textHeight = size.height;
	}else if (textOrIcon instanceof IImage){
		this.width += size.width;
		height = Math.max(((IImage)textOrIcon).getHeight(),height);
	}
}
/**
 * This adds a new column of data along the line. It is added to the right of
 * the previous column or original text and icon.
 * @param textOrIcon either a String or an IImage to display in this column.
 */
//===================================================================
public void addColumn(Object textOrIcon)
//===================================================================
{
	addColumn(textOrIcon,-1,Graphics.WEST);
}
//===================================================================

//===================================================================
public int getHeight() {return height;}
//===================================================================

//===================================================================
public int getWidth() {return width;}
//===================================================================

//
// FIXME
//
private static String [] strings = new String[1];
private static Rect colrect = new Rect();

//===================================================================
public synchronized void draw(Graphics g,int x,int y,int options)
//===================================================================
{
	FontMetrics fm = fontMetrics;
	if (fm == null) fm = mApp.mainApp.getFontMetrics();
	if ((options & ewe.ui.ChoiceControl.INDENT_ITEM_FLAG) != 0){
		x += (options & 0xff) * 6;
		options &= ~0xff;
	}
	Color c = g.getColor();
	if (icon != null) {
		//g.setColor(g.getBackground());
		//g.fillRect(x,y,icon.getWidth()+2,height);
		switch(textPosition){
			case Graphics.Up : icon.draw(g,x+(width-icon.getWidth())/2,y+textHeight+2,options); break;
			case Graphics.Down : icon.draw(g,x+(width-icon.getWidth())/2,y,options); break;
			case Graphics.Left : icon.draw(g,x+textWidth+2,(height-icon.getHeight())/2+y,options); break;
			default:
				icon.draw(g,x,(height-icon.getHeight())/2+y,options); break;
		}
	}
	Color chosen = (options & DISABLED) != 0 ? Color.DarkGray : textColor == null ? c : textColor;
	g.setColor(chosen);
	g.setFont(fm.getFont());
	colrect.set(x,y,width,height);
	if (lines != null){
		if (icon != null){
			if (textPosition == Graphics.Right) colrect.x += icon.getWidth()+2;
			if (textPosition == Graphics.Down) colrect.y += icon.getHeight()+2;
			if (textPosition == Graphics.Right || textPosition == Graphics.Left) colrect.width -= icon.getWidth()+2;
			else colrect.height -= icon.getHeight()+2;
		}
		g.drawText(fm,lines,colrect,multiLineTextAlignment,Graphics.CENTER,0,lines.length,null);
	}else if (text != null){
		if (icon != null && textPosition == Graphics.Right) x += icon.getWidth()+2;
		if (icon != null && textPosition == Graphics.Down) y += icon.getHeight()+2;
		switch(textPosition){
			case Graphics.Up:
			case Graphics.Down:
				g.drawText(text,x+(width-textWidth)/2,y); break;
			default:
				g.drawText(text,x,y+(height-textHeight)/2); break;
		}
	}else if (columns != null){
		if (icon != null) x += icon.getWidth()+2;
		for (int i = 0; i<numColumns; i++){
			TextEntry te = (TextEntry)columns.get(i);
			Color bg = background;
			if (Color.isNull(bg)) bg = g.getBackground();
			if (!Color.isNull(bg)){
				g.setColor(bg);
				g.fillRect(x,y,te.length,height);
			}
			g.setColor((options & DISABLED) != 0 ? Color.DarkGray : c);
			colrect.set(x,y,te.length,height);
			if (te.textOrIcon instanceof String){
				strings[0] = (String)te.textOrIcon;
				g.drawText(fm,strings,colrect,ewe.ui.Control.LEFT,te.anchor);
			}else if (te.textOrIcon instanceof IImage){
				g.drawImage((IImage)te.textOrIcon,options,colrect,te.anchor);
			}
			x += te.length;
		}
	}
	if ((options & OUTLINED) != 0){
		g.setColor(Color.Black);
		g.drawRect(0,0,width,height);
	}
	g.setColor(c);
}
//===================================================================
public Color getBackground() {return background;}
//===================================================================

//===================================================================
public void free(){clear();}
//===================================================================

//##################################################################
}
//##################################################################

