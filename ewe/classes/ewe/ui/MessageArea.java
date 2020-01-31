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
public class MessageArea extends mLabel{
//##################################################################
protected String [] lines = null;
{
	format = new FormattedTextSpecs();
}
//==================================================================
public MessageArea(int rows,int columns) {super(rows,columns);}
public MessageArea(String text) {super(text,false);}
//==================================================================
{
	isData = true;
	alignment = anchor = Gui.CENTER;
	//defaultAddMeCellConstraints = mPanel.Stretch;
	//defaultAddMeControlConstraints = mPanel.Fill;
	//defaultAddMeAnchor = mPanel.Center;
}
//===================================================================
public void update()
//===================================================================
{
	lines = splitLines();
}

//===================================================================
public String [] splitLines()
//===================================================================
{
	return ewe.util.mString.split(text,'\n');
}
//===================================================================
public String [] getLines()
//===================================================================
{
	if (lines == null) lines = splitLines();
	return lines;
}
//===================================================================
public void setText(String txt) {text = txt; if (text == null) text = ""; lines = splitLines(); repaintNow();}
//===================================================================

//##################################################################
}
//##################################################################

