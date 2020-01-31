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
public class QuickLayout implements LayoutManager{
//##################################################################

public int unitPreferredWidth = 100;
public int unitPreferredHeight = 30;
public boolean stretchLastColumn = true;
public boolean stretchLastRow = true;
public boolean stretchFirstColumn = false;
public boolean stretchFirstRow = false;

//===================================================================
public void layout(Grid grid,Panel panel,Rect panelRect)
//===================================================================
{
	Dimension d = new Dimension(20,20);
	if (grid == null) return;
	int y = panelRect.y;
	for (int r = 0; r<grid.rows; r++){
		int x = panelRect.x;
		for (int c = 0; c<grid.columns; c++){
			int h = 0, width = 0;
			Control cn = (Control)grid.objectAt(r,c);
			if (cn == null) continue;
			d.width = unitPreferredWidth;
			if (stretchFirstColumn && c == 0)
				d.width = panelRect.width-((grid.columns-1)*unitPreferredWidth);
			else if (stretchLastColumn && !stretchFirstColumn && c == grid.columns-1)
				d.width = panelRect.width-x+panelRect.x;
			d.height = unitPreferredHeight;
			if (stretchFirstRow && r == 0)
				d.height = panelRect.height-((grid.rows-1)*unitPreferredHeight);
			else if (stretchLastRow && !stretchFirstRow && r == grid.rows-1)
				d.height = panelRect.height-y+panelRect.y;

			//cn.getPreferredSize(d);
			width += d.width;
			h += d.height;
			Insets in = (Insets)panel.getControlTag(panel.INSETS,cn,panel.noInsets);
			width += in.left+in.right;
			h += in.top+in.bottom;
			cn.setRect(x+in.left,y+in.top,d.width-in.left-in.right,d.height-in.top-in.bottom);
			x += width;
		}
		y += unitPreferredHeight;
	}
}

//===================================================================
public Dimension getPreferredSize(Grid controls,Panel panel,Dimension destination)
//===================================================================
{
	if (destination == null) destination = new Dimension();
	destination.set(0,0);
	if (controls == null) return destination;
	destination.width = controls.columns*unitPreferredWidth;
	destination.height = controls.rows*unitPreferredHeight;
	return destination;
}



//##################################################################
}
//##################################################################

