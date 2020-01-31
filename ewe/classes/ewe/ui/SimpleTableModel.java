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
class SimpleTableModel extends TableModel{
//##################################################################

/**
* This paints a matrix of cells. The graphics has been translated so that the top
* left of the upper-left cell should be painted at 0,0. cells.width and cell.height
* give the width and height allocated to the cells. It should not paint any cells
* which is completely outside the bounds.
**/
//===================================================================
public void paintTableCell(TableControl tc,Graphics g,Rect cells)
//===================================================================
{
	findCellsInArea(cells,false);
	int lr = cells.y+cells.height;
	int lc = cells.x+cells.width;
	Rect rect = new Rect();
//..................................................................
	int r = cells.y, c = cells.x;
	TableCellAttributes a = getCellAttributes(r,c,false,tca);
	Insets in = getCellInsets(r,c,insets);
	a = tc.overrideAttributes(a);
	if (a.fontMetrics == null) a.fontMetrics = tc.getFontMetrics();
//..................................................................
	int x = 0, y = 0;
	for (r = cells.y; r<lr; r++){
		x = 0;
		int ch = getRowHeight(r);
		for (c = cells.x; c<lc; c++){
			int cw = getColWidth(c);
			rect.x = x; rect.y = y;
			rect.width = cw; rect.height = ch;
			g.draw3DRect(rect,a.borderStyle,a.flat,a.fillColor,a.borderColor);
			Insets.apply(in,rect);
			tca.text = getCellText(r,c);
			paintTableCellText(tc,g,r,c,tca,rect,tca.text);
			x += cw;
		}
		y += ch;
	}
}
//##################################################################
}
//##################################################################

