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
import ewe.util.*;

//##################################################################
class textPadState{
//##################################################################
int cursorLine, cursorPos;
int firstLine, xShift;
int selStartLine = -1, selStartPos;
int selEndLine, selEndPos;

boolean selectionEnabled = true;

public boolean isInSelection(int line)
{
	if (selStartLine == -1) return false;
	return (selStartLine <= line && selEndLine >= line);
}
public boolean isInSelection(int line,int ch)
{
	if (!isInSelection(line)) return false;
	if (selStartLine == line && selStartPos > ch) return false;
	if (selEndLine == line && selEndPos <= ch) return false;
	return true;
}
public textPadState(){}
public textPadState getCopy()
{
	textPadState tas = new textPadState();
	tas.cursorLine = cursorLine;
	tas.cursorPos = cursorPos;
	tas.xShift = xShift;
	tas.firstLine = firstLine;
	tas.selStartLine = selStartLine;
	tas.selStartPos = selStartPos;
	tas.selEndLine = selEndLine;
	tas.selEndPos = selEndPos;
	return tas;
}
public boolean hasSelection()
{
	return selStartLine != -1;
}
public boolean selectionStartsFromFirstCharacter()
{
	return selStartLine == 0 && selStartPos == 0;
}
public boolean displayChanged(textPadState other)
{
	if (other.firstLine != firstLine) return true;
	if (other.xShift != xShift) return true;
	return false;
}
//===================================================================
public void fixSel(DisplayLine lines,int numLines)
//===================================================================
{
	if (selStartLine < 0) selStartLine = 0;
	if (selEndLine < 0) selEndLine = 0;
	if (selStartLine >= numLines) selStartLine = numLines-1;
	if (selEndLine >= numLines) {
		selEndLine = numLines-1;
		selEndPos = DisplayLine.getNext(lines,selEndLine).toString().length();
	}
	DisplayLine s = (DisplayLine)DisplayLine.getNext(lines,selStartLine);
	if (selStartPos < 0) selStartPos = 0;
	if (selStartPos > s.length()) selStartPos = s.length();
	s = (DisplayLine)DisplayLine.getNext(lines,selEndLine);
	if (selEndPos < 0) selEndPos = 0;
	if (selEndPos > s.length()) selEndPos = s.length();
}

//===================================================================
public void newCursorPos(int ch,int ln,boolean takeSel,DisplayLine lines,int numLines)
//===================================================================
{
	if (takeSel){
		if (!hasSelection()) {
			selStartLine = selEndLine = cursorLine;
			selStartPos = selEndPos = cursorPos;
		}
	}
	boolean wasOnStart = (cursorLine == selStartLine && cursorPos == selStartPos);
	cursorLine = ln;
	cursorPos = ch;
	if (takeSel){
		if (wasOnStart) {
			if (cursorLine > selEndLine || ((cursorLine == selEndLine) && (cursorPos > selEndPos))){
				selStartLine = selEndLine;
				selStartPos = selEndPos;
				selEndLine = cursorLine;
				selEndPos = cursorPos;
			}else{
				selStartLine = cursorLine;
				selStartPos = cursorPos;
			}
		}else{
			if (cursorLine < selStartLine || ((cursorLine == selStartLine) && (cursorPos < selStartPos))){
				selEndLine = selStartLine;
				selEndPos = selStartPos;
				selStartLine = cursorLine;
				selStartPos = cursorPos;
			}else{
				selEndLine = cursorLine;
				selEndPos = cursorPos;
			}
		}
		fixSel(lines,numLines);
	}
}

public boolean charPointAfterSelectionRemoved(Point charPoint)
{
	if (isInSelection(charPoint.y,charPoint.x)) return false;
	if (selStartLine == -1) return true;
	if (selStartLine == selEndLine){
		if (charPoint.y != selStartLine) return true;
		if (charPoint.x < selStartPos) return true;
		charPoint.x -= (selEndPos-selStartPos);
		return true;
	}else{ // Multilined.
		int numGoing = selEndLine-selStartLine;
		if (charPoint.y <= selStartLine) return true;
		if (charPoint.y == selEndLine)
			charPoint.x -= selEndPos;
		charPoint.y -= numGoing;
		return true;
	}
}
//##################################################################
}
//##################################################################

