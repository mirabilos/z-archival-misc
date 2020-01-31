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

/**
* This is used for calculated formatted text metrics.
**/
//##################################################################
public class FormattedTextSpecs{
//##################################################################
//
// Do not move any of these variables.
//
/**
* There are currently no options defined.
**/
public int options;
/**
* If this is greater than 0, it is taken to be the number of characters wide a Tab stop
* will be. If you set it to zero, then you should set tabPixelWidth to be greater than 0.
* By default it is 8.
@see #tabPixelWidth
**/
public int tabCharacterWidth = 8;
/**
* If this is greater than 0, it is taken to be the number of pixels wide a Tab stop
* will be.
@see #tabCharacterWidth
**/
public int tabPixelWidth = 0;
/**
* If this is not null these will be the calculated positions of each character in a String.
**/
public int [] calculatedPositions;
/**
* Metrics for the text.
**/
public FontMetrics metrics;
/**
* This is used by getWidthAndPositions(). If it discovers no formatting it will set it false.
**/
public boolean isFormatted = false;
/**
* The position (in pixels) of the leftmost character on the line when displayed.
**/
public int firstCharPosition = 0;
/**
* Space to put before the first character on the line.
**/
public int leftMargin = 0;
/**
* The position (in pixels) of the leftmost character on the line when displayed.
**/
public int rightMargin = 0;
/**
* The extra space used by other formatters.
**/
public int extraSpaceUsed = 0;
/**
* This is the width of the line (in pixels) that the formatted text will be displayed in.
**/
public int displayLineWidth = 0;
/**
* This is the height of the line (in pixels) that the formatted text will be displayed in.
**/
public int displayLineHeight = 0;
public char [] charsToDraw;
public int numCharsToDraw;

public static int LINE_FLAG_CANCEL_ALIGNMENTS = 0x1;
/**
* This can be one of the LINE_FLAG_XXXX values.
**/
public int lineFlags;
public Color backgroundColor;
/**
* Return the tabWidth in pixels.
**/
//===================================================================
public static int getTabWidth(FormattedTextSpecs fts,FontMetrics fm)
//===================================================================
{
	if (fm == null)
		if (fts != null) fm = fts.metrics;

	if (fts == null)
		if (fm == null)
			return 20;
		else
			return fm.getCharWidth('X')*8;
	else
		if (fts.tabPixelWidth > 0)
			return fts.tabPixelWidth;
		else if (fts.tabCharacterWidth <= 0)
			if (fm == null) return 20;
			else return fm.getCharWidth('X')*8;
		else if (fm == null) return 20;
		else return fm.getCharWidth('X')*fts.tabCharacterWidth;
}
/**
 * Calculate the positions of each character in the String. If the String is formatted (i.e. has TAB)
	characters in it) then the variable isFormatted will be set true and the calculatedPositions
	variable will hold the positions of each character.
 * @param s The string to check.
 * @param fts The FormattedTextSpecs for the calculation.
 * @param fm The FontMetrics being used.
 * @param alwaysFormatted if this is true consider it to always be formatted.
 * @return the full width of the String.
 */
//===================================================================
public static int getWidthAndPositions(String s,FormattedTextSpecs fts,FontMetrics fm,boolean alwaysFormatted)
//===================================================================
{
	if (fts == null) return fm.getTextWidth(s);
	if (alwaysFormatted) fts.isFormatted = true;
	else fts.isFormatted = s.indexOf('\t') != -1;
	if (!fts.isFormatted) return fm.getTextWidth(s);
	fts.calculatedPositions = fm.getFormattedTextPositions(s,fts,fts.calculatedPositions);
	int len = s.length();
	if (len == 0) return 0;
	else return fts.calculatedPositions[len-1];
}
/**
 * Adjust the positions of the characters as currently specified by calculatedPositions.
 * @param startFrom The character to start adjusting from.
 * @param change The amount to adjust the position.
 * @return the adjusted positions.
 */
//-------------------------------------------------------------------
protected int [] adjustPositions(int startFrom, int change)
//-------------------------------------------------------------------
{
	for (int i = startFrom; i<calculatedPositions.length; i++)
		calculatedPositions[i] += change;
	return calculatedPositions;
}
//===================================================================
public int [] insertSpace(int beforeCharacter, int space)
//===================================================================
{
	if (beforeCharacter == 0) {
		firstCharPosition += space;
		return calculatedPositions;
	}else
		return adjustPositions(beforeCharacter-1,space);
}
//===================================================================
public int [] changeAndAdjustPositions(int [] newPositions,int startFrom, int length)
//===================================================================
{
	if (length == 0) return calculatedPositions;
	int oldWidth = widthOf(startFrom,length);
	int theChange = newPositions[length-1]-oldWidth;
	int off = startFrom == 0 ? 0 : calculatedPositions[startFrom-1];
	for (int i = 0; i<length; i++)
		calculatedPositions[startFrom+i] = newPositions[i]+off;
	adjustPositions(startFrom+length,theChange);
	return calculatedPositions;
}
//===================================================================
public int widthOf(int startFrom,int length)
//===================================================================
{
	if (length == 0) return 0;
	return calculatedPositions[startFrom+length-1]-(startFrom == 0 ? 0 : calculatedPositions[startFrom-1]);
}
//##################################################################
}
//##################################################################

