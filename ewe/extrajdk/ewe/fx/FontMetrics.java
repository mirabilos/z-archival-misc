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
package ewe.fx;

import ewe.applet.Applet;
import ewe.fx.print.PrinterJob;

/**
 * FontMetrics computes font metrics including string width and height.
 * <p>
 * FontMetrics are usually used to obtain information about the widths and
 * heights of characters and strings when drawing text on a surface.
 * A FontMetrics object references a font and surface since fonts may have
 * different metrics on different surfaces.
 * <p>
 * Here is an example that uses FontMetrics to get the width of a string:
 *
 * <pre>
 * ...
 * Font font = new Font("Helvetica", Font.BOLD, 10);
 * FontMetrics fm = getFontMetrics();
 * String s = "This is a line of text.";
 * int stringWidth = fm.getTextWidth(s);
 * ...
 * </pre>
 */

public class FontMetrics
{
Font font;
ISurface surface;
int ascent;
int descent;
int leading;
private java.awt.FontMetrics awtFM;

//-------------------------------------------------------------------
java.awt.FontMetrics getAwtFM()
//-------------------------------------------------------------------
{/*
	if (surface instanceof PrinterJob){
		Graphics g = ((PrinterJob)surface).currentPage;
		if (g == null) return null;
		return g._g.getFontMetrics(font.getAWTFont());
	}else
	*/
		return //Applet.getDisplayed().
			java.awt.Toolkit.getDefaultToolkit().getFontMetrics(font.getAWTFont());
}
/**
 * Constructs a font metrics object referencing the given font and surface.
 * <p>
 * If you are trying to create a font metrics object in a Control subclass,
 * use the getFontMetrics() method in the Control class.
 * @see ewe.ui.Control#getFontMetrics(ewe.fx.Font font)
 */
public FontMetrics(Font font, ISurface surface)
	{
	this.font = font;
	this.surface = surface;
	awtFM = getAwtFM();
	ascent = awtFM.getAscent();
	descent = awtFM.getDescent();
	leading = awtFM.getLeading();
	}


/**
 * Returns the ascent of the font. This is the distance from the baseline
 * of a character to its top.
 */
public int getAscent()
	{
	return ascent;
	}

/**
 * Returns the width of the given character in pixels.
 */

public int getCharWidth(char c)
	{
	java.awt.FontMetrics fm = awtFM;//getAwtFM();
	return fm.charWidth(c);
	}

/**
 * Returns the descent of a font. This is the distance from the baseline
 * of a character to the bottom of the character.
 */
public int getDescent()
	{
	return descent;
	}

/**
 * Returns the height of the referenced font. This is equal to the font's
 * ascent plus its descent. This does not include leading (the space between lines).
 */
public int getHeight()
	{
	return ascent + descent;
	}

/**
 * Returns the external leading which is the space between lines.
 */
public int getLeading()
	{
	return leading;
	}
/**
 * Returns the width of the given text string in pixels.
 */
public int getTextWidth(String s)
	{
	java.awt.FontMetrics fm = awtFM;//getAwtFM();
	return fm.stringWidth(Graphics.getDisplayable(s));
	}

/**
 * Returns the width of the given text in pixels.
 * @param chars the text character array
 * @param start the start position in array
 * @param count the number of characters
 */

public int getTextWidth(char chars[], int start, int count)
	{
		return getTextWidth(new String(chars, start, count));
	}

public Font getFont() {return font;} // MLB
/**
 * Returns the positions of each individual character in a formatted String. This takes into account
	special characters like TAB (9). You can also use this to find the total width of the string. This would be
	equal to the last entry in the int array.
* @param s The string to display
* @param fts Options for the formatted text.
* @param positions This is the destination for the positions of each character. If this is null or not big enough
to hold all the widths, a new one will be created which is big enough and returned. The value at index 0, will be
the position of the character at index 1. The position of the character at index 0 is always 0.
* @return The array of character widths - one entry for each character in s.
*/
public int [] getFormattedTextPositions(String s,FormattedTextSpecs fts,int positions[])
{
	int len = s.length();
	if (positions == null) positions = new int[len];
	if (len > positions.length) positions = new int[len];
	int tw = FormattedTextSpecs.getTabWidth(fts,this);
	char [] all = ewe.sys.Vm.getStringChars(s);
	int next = 0;
	for (int i = 0; i<all.length; i++){
		char c = all[i];
		if (c != '\t') next += getCharWidth(c);
		else{
			next = next+tw;
			next -= next%tw;
		}
		positions[i] = next;
	}
	return positions;
}
/**
 * Get a new FontMetrics for a different font but the same ISurface.
 * @param f the new Font.
 * @return the FontMetrics for the new font.
 */
//===================================================================
public FontMetrics getNewFor(Font f)
//===================================================================
{
	return new FontMetrics(f,surface);
}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof FontMetrics)) return super.equals(other);
	Font f = ((FontMetrics)other).font;
	return font.name.equalsIgnoreCase(f.name) && font.size == f.size && font.style == f.style;
}

}
