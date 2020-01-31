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
import ewe.util.mString;

//##################################################################
public class DisplayLineSpecs{
//##################################################################
//These are input parameters.
public char [] source;
public int start;
public int length;
public FontMetrics fm;
public int displayWidth;
//These are output parameters.
public int widthOfFirst;
public int widthOfWidest;
public int widthOfAll;
public int displayLength;
public int widest;
public int flags;
//Some more input.
public FormattedTextSpecs format;
protected int tabWidth;
protected int fixedWidth;
protected char[] widthCacheChars;
protected int[] widthCache;
//
public DisplayLine.WidthProvider widthProvider;

//===================================================================
public DisplayLine toDisplayLine(boolean update,int options)
//===================================================================
{
	DisplayLine dl = new DisplayLine();
	dl.displayWidth = displayWidth;
	if ((options & DisplayLine.SPLIT_GET_LENGTHS) == 0)
		dl.line = new String(source,start,displayLength);
	dl.lengthOfLine = displayLength;
	dl.flags = flags;
	if ((options & DisplayLine.SPLIT_NO_WRAP) != 0) dl.width = widthOfAll;
	else dl.width = widthOfFirst;
	if (update) {
		start += displayLength;
		length -= displayLength;
		if ((flags & DisplayLine.ENDS_WITH_NEWLINE) != 0) {
			start++;
			length--;
		}
		widthOfFirst = widthOfAll = widthOfWidest = flags = displayLength = 0;
	}
	return dl;
}
//===================================================================
public DisplayLine split(int options)
//===================================================================
{
	tabWidth = FormattedTextSpecs.getTabWidth(format,fm);
	int num = 0;
	fixedWidth = fm.getCharWidth('X');
	if (fixedWidth != fm.getCharWidth('.')){
 		fixedWidth = -1;
		widthCacheChars = new char[255];
		widthCache = new int[255];
		widthCache[0] = fm.getCharWidth((char)0);
	}
	DisplayLine list = new DisplayLine(), last = list;
	while(true){
		if (widthProvider != null) displayWidth = widthProvider.getWidthFor(this,num,start,(DisplayLine)list.next);
		if (!calculate(options)) break;
		num++;
		DisplayLine d2 = toDisplayLine(true,options);
		if (last != list) d2.prev = last;
		last.next = d2;
		last = d2;
	}
	return (DisplayLine)list.next;
}
//-------------------------------------------------------------------
protected native boolean calculate(int options);
//-------------------------------------------------------------------

//===================================================================
public static native int getWidth(DisplayLine first,FontMetrics fm,int options);
//===================================================================
/*
{
	int widest = 0;
	if ((options & DisplayLine.SPLIT_NO_WRAP) != 0){
		while (first != null){
			int w = fm.getTextWidth(first.line);
			if (w > widest) widest = w;
			first = (DisplayLine)first.next;
		}
	}else{
		while (first != null){
			int idx = first.line.indexOf(' ');
			int w = 0;
			if (idx == -1){
				w = fm.getTextWidth(first.line);
			}else{
				w = fm.getTextWidth(ewe.sys.Vm.getStringChars(first.line),0,idx+1);
			}
			if (w > widest) widest = w;
			first = (DisplayLine)first.next;
		}
	}
	return widest;
}
*/
//##################################################################
}
//##################################################################

