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
package ewe.fx.print;
import ewe.data.LiveObject;
import ewe.ui.CellPanel;
import ewe.ui.Editor;
import ewe.ui.UIBuilder;
import ewe.util.mString;

//##################################################################
public class PageFormatData extends LiveObject{
//##################################################################
public static final int LETTER = 0, LEGAL = 1, CUSTOM = 2, CONTINUOUS = 3;

public int type = LETTER;
public double width = 8.5, height = 11;
public double left = 0.75, right = 0.75, top = 0.75, bottom = 0.75;

public boolean separateSizeAndMargins = true;
public boolean showContinuous = false;

//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int which)
//===================================================================
{
	UIBuilder ui = UIBuilder.newPanel(cp,ed,this);
		if (separateSizeAndMargins) ui.getOpenPanel().setCell(cp.HSTRETCH).setText("Paper Size (Inches)");
		ui.openInputStack().setControl(ed.VFILL|ed.WEST);
		String choices ="Letter - 8.5\"x11\"|Legal - 8.5\"x14\"|Custom";
		if (showContinuous) choices += "|Continuous";
			ui.addChoice("Paper Type:","type",mString.split(choices,'|'));
		ui.close(true);
		ui.openInputStack().columns = 2;
			ui.addAll("width,height");
		ui.close(true);
	ui.close(true);
	ui.openInputStack().columns = 2;
		if (separateSizeAndMargins) ui.getOpenPanel().setText("Margins (Inches)");
		ui.addAll("left,top,right,bottom");
	ui.close(true);
}
//===================================================================
public void type_changed(Editor ed)
//===================================================================
{
	if (type == LETTER){
		width = 8.5;
		height = 11;
	}else if (type == LEGAL){
		width = 8.5;
		height = 14;
	}else if (type == CONTINUOUS){
		top = bottom = 0;
		width = 8.5;
		height = 11;
	}
	if (ed != null) {
		ed.toControls("width,height,top,bottom");
		ed.modifyFields("top,bottom,height",type == CONTINUOUS,ed.Disabled,0,true);
	}
}
//===================================================================
public PageFormat toPageFormat()
//===================================================================
{
	PageFormat pf = new PageFormat();
	pf.requestPageSize(width*72,height*72);
	pf.requestImageableArea(left*72,top*72,(width-left-right)*72,(height-top-bottom)*72);
	return pf;
}
//##################################################################
}
//##################################################################

