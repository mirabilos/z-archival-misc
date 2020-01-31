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
import ewe.util.*;
import ewe.data.*;
import ewe.fx.*;
import ewe.sys.Device;

//##################################################################
public class ButtonListSelect extends TextDisplayButton{
//##################################################################
/**
This holds the data that the ButtonListSelect uses for input and
display. Use setData() and getData() to set and get it.
**/
protected MultiListSelect.SingleListSelect data;

/**
This is the text that will be displayed in the button if no item is
selected.
**/
public String noSelectionText = "(None)";

/**
This is not created until the button is first pressed.
**/
protected ListSelect mySelect;

//===================================================================
public String getDisplayText()
//===================================================================
{
	if (data == null) return noSelectionText == null ? "" : noSelectionText;
	Vector all = data.getList();
	int[] chosen = data.getSelectedIndexes();
	if (chosen.length == 0) return noSelectionText == null ? "" : noSelectionText;
	StringBuffer sb = new StringBuffer();
	String first = all.get(chosen[0]).toString();
	sb.append(first);
	if (chosen.length == 1) return sb.toString();
	int width = this.width-12;
	if (width <= 0){
		if (chosen.length > 1) sb.append(",...");
	}else{
		FontMetrics fm = getFontMetrics();
		int soFar = fm.getTextWidth(first);
		int comma = fm.getTextWidth(",");
		for (int i = 1; i<chosen.length; i++){
			String t = all.get(chosen[i]).toString();
			int len = fm.getTextWidth(t);
			if (len+comma+soFar > width){
				sb.append(",...");
				break;
			}
			sb.append(",");
			sb.append(t);
			soFar += comma+len;
		}
	}
	return sb.toString();
}

//===================================================================
public void doAction(int how)
//===================================================================
{
	super.doAction(how);
	if (data == null) return;
	if (mySelect == null) mySelect = new ListSelect(prompt,data);
	int[] old = data.getSelectedIndexes();
	if (mySelect.execute(getFrame(),Gui.CENTER_FRAME) == Form.IDCANCEL){
		data.setSelectedIndexes(old);
		mySelect.setData(data);
	}
	repaintNow();
}
//===================================================================
public void setData(Object data)
//===================================================================
{
	this.data = (MultiListSelect.SingleListSelect)data;
	mySelect = null;
}

//===================================================================
public void setData(Vector allChoices, Vector selected)
//===================================================================
{
	setData(new MultiListSelect.SingleListSelect(allChoices,selected,false));
}
//===================================================================
public MultiListSelect.SingleListSelect getData()
//===================================================================
{
	return data;
}
//##################################################################
}
//##################################################################

