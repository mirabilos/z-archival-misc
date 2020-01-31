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
public DisplayLine.WidthProvider widthProvider;
protected int fixedWidth;

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
boolean debug = false;
private char[] widthCacheChars;
private int[] widthCache;

//===================================================================
public DisplayLine split(int options)
//===================================================================
{
	tabWidth = FormattedTextSpecs.getTabWidth(format,fm);
	fixedWidth = -1;
	if (fm.getCharWidth('X') == fm.getCharWidth('.')) {
		fixedWidth = fm.getCharWidth('X');
	}else{
		widthCacheChars = new char[255];
		widthCache = new int[255];
		widthCache[0] = fm.getCharWidth((char)0);
	}
	int num = 0;
	DisplayLine list = new DisplayLine(), last = list;
	debug = true;
	while(true){
		if (widthProvider != null) displayWidth = widthProvider.getWidthFor(this,num,start,(DisplayLine)list.next);
		if (!calculate(options)) break;
		num++;
		DisplayLine d2 = toDisplayLine(true,options);
		debug = false;
		if (last != list) d2.prev = last;
		last.next = d2;
		last = d2;
	}
	return (DisplayLine)list.next;
}
//-------------------------------------------------------------------
protected boolean calculate(int options) {return notNativeCalculate(options);}
//-------------------------------------------------------------------
//-------------------------------------------------------------------
protected boolean notNativeCalculate(int options)
//-------------------------------------------------------------------
{
	boolean wrap = ((options & DisplayLine.SPLIT_NO_WRAP) == 0);
	widthOfWidest = widthOfFirst = widthOfAll = 0;
	int sw = fm.getCharWidth(' ');
	int cw = 0, lp = -1, lw = 0;
	if (length <= 0) return false;
	boolean first = true;
	for (int i = 0; i<length+1; i++){
		if (i == length){
			displayLength = i;
			break;
		}
		char ch = source[start+i];
		boolean sp = ch == ' ';
		boolean nl = ch == '\n';
		boolean tab = ch == '\t';
		int charWidth = sp ? sw : tab ? tabWidth-((lw+cw)%tabWidth) : fixedWidth;
		if (charWidth == -1) {
			int idx = ch & 0xff;
			if (widthCacheChars[idx] == ch) charWidth = widthCache[idx];
			else{
				widthCache[idx] = charWidth = fm.getCharWidth(ch);
				widthCacheChars[idx] = ch;
			}
		}
		if (tab) sp = true;
		if (nl) {
			charWidth = 0;
			lp = i-1;
		}else
			if (sp) lp = i;
		//System.out.println(lw+cw+charWidth+" <> "+displayWidth);
		if ((lw+cw+charWidth >= displayWidth && lp >= 0 && wrap) || nl){
			if (sp) cw += charWidth;
			if (cw > widthOfWidest) widthOfWidest = cw;
			displayLength = lp+1;
			lw += cw;
			cw = 0;
			if (nl) flags |= DisplayLine.ENDS_WITH_NEWLINE;
			break;
		}
		cw += charWidth;
		if (cw > widthOfWidest) widthOfWidest = cw;
		if (sp) {
			lw += cw;
			if (first) widthOfFirst = cw;
			first = false;
			cw = 0;
		}
	}
	if (first)
		widthOfFirst = widthOfAll = widthOfWidest;
	else
		widthOfAll = lw+cw;
	if (!wrap) widthOfWidest = widthOfAll;
	if (widthOfWidest > widest) widest = widthOfWidest;
	return true;
}
//===================================================================
public static int getWidth(DisplayLine first,FontMetrics fm,int options)
//===================================================================
{
	int widest = 0;
	while (first != null){
		if (first.width > widest) widest = first.width;
		first = (DisplayLine)first.next;
	}
	return widest;
/*
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
*/
}

//##################################################################
}
//##################################################################
