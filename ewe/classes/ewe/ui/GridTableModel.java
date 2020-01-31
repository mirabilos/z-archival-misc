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
import ewe.sys.Vm;
import ewe.util.*;

/**

This is a table model which displays a Grid of data. It also has the
capability to correctly display Control objects.<p>

To use this you do the following:<p>
<ol>
<li>Create a Grid containing the data, and optionally create Vectors to hold
the row and column headers.</li>
<li>Call setDataAndHeaders(Grid data, Vector columnHeaders, Vector rowHeaders) to
set the data and headers. Either of the columnHeaders or rowHeaders can be null.</li>
<li>Get a FontMetrics object for the Font that you intend to use in the TableControl. The easiest
way is to call getFontMetrics() on the TableControl you intend to use (after you have
optionally changed the "font" variable in the TableControl).</li>
<li>Call calculateSizes(fontMetrics) on the GridTableModel so that it calculates the correct
width and heights for the cells for that Font.</li>
<li>Optionally set "hasPreferredSize" to true if you want to TableControl to calculate
its preferredSize to be the size of the entire table. For large tables you may not want
to do this, but rather set the preferredSize explicitly using setPreferredSize().</li>
<li>Call setTableModel(myGridTableModel) on the TableControl you want to use.</li>
</ol>
Here is an example use:
<pre>
import ewe.ui.*;
import ewe.util.*;

//##################################################################
public class TestGridTableModel extends Editor{
//##################################################################

//===================================================================
public TestGridTableModel()
//===================================================================
{
	TableControl t = new TableControl();
	//
	// Create the data Grid and header Vector.
	//
	Grid g = new Grid();
	Vector headers = new Vector();
	//
	for (int row = -1; row != 10; row++){
		for (int col = 0; col != 5; col++){
			if (row == -1)
				headers.add("Header: "+col);
			else
				g.add("Cell("+row+", "+col+")",false);
		}
		g.endRow();
	}
	//
	// Create the GridTableModel.
	//
	GridTableModel gtm = new GridTableModel();
	//
	// Set the data and the headers.
	// We won't use row headers in our example.
	//
	gtm.setDataAndHeaders(g,headers,null);
	//
	// Call this to correctly calculate the size of the each cell
	// given the Font that you intend to use.
	// If you intend to use the default font of this Editor then
	// you can use this.getFontMetrics() instead of t.getFontMetrics().
	//
	t.font = mApp.findFont("fixed",true);
	gtm.calculateSizes(t.getFontMetrics());
	//
	// If this is not true the Table will not have a preferred size
	// and will be initially displayed very small.
	//
	gtm.hasPreferredSize = true;
	//
	t.setTableModel(gtm);
	addLast(new ScrollBarPanel(t));
}
//##################################################################
}
//##################################################################
</pre>

**/
//##################################################################
public class GridTableModel extends TableModel{
//##################################################################
/**
* The data to be displayed.
**/
protected Grid data;
/**
* The column headers if any.
**/
protected Vector columnHeaders;
/**
* The row headers if any.
**/
protected Vector rowHeaders;
/**
* This contains any Control objects which may be within the grid of data. Note that if
* there are no controls then this Vector will be null.
**/
//protected Vector controls;
/**
* Set the data and the headers. Each element in the Vectors or Grid must
* be either a String or an array of Strings for multiline display.
**/
//===================================================================
public void setDataAndHeaders(Grid data,Vector columnHeaders,Vector rowHeaders)
//===================================================================
{
	this.data = data;
	this.columnHeaders = columnHeaders;
	this.rowHeaders = rowHeaders;
	hasColumnHeaders = (columnHeaders != null);
	hasRowHeaders = (rowHeaders != null);
}
/**
The widths of the cells as used by calculateColWidth(). There must be on entry for
each cell
*/
protected int [] widths;
protected int [] heights;

String [] single = new String[0];

/**
* This always returns null. The value returned by getCellData() is used for display.
**/
//===================================================================
//public Object getCellText(int row,int col){return null;}
//===================================================================
/**
* This returns the data in the grid and columnHeaders/rowHeaders unmodified.
**/
//===================================================================
public Object getCellData(int row,int col)
//===================================================================
{
	if (row == -1) {
		if (columnHeaders == null || col == -1) return null;
		return (columnHeaders.get(col));
	}
	if (col == -1){
		if (rowHeaders == null || row == -1) return null;
		return (rowHeaders.get(row));
	}
	return data.objectAt(row,col);
}
/**
* This calculates the size of cell given the FontMetrics.
**/
//-------------------------------------------------------------------
protected void checkCellSize(FontMetrics fm,int row,int col,Dimension d)
//-------------------------------------------------------------------
{
	d.width = d.height = 0;
	Object data = getCellData(row,col);
	if (data instanceof String || data instanceof String []){
		String [] got = getLinesFor(data);
		if (got == null) d.width = d.height = 0;
		else Graphics.getSize(fm,got,0,got.length,d);
	}else if (data instanceof IImage){
		d.width = ((IImage)data).getWidth();
		d.height = ((IImage)data).getHeight();
	}else if (data instanceof Control){
		((Control)data).make(false);
		((Control)data).getPreferredSize(d);
		//table.add((Control)data);
		//if (controls == null) controls = new Vector();
		//controls.add(data);
	}
	if (d.width != 0) d.width += gap*2;
	if (d.height != 0) d.height += gap*2;
	if (widths[col+1] < d.width) widths[col+1] = d.width;
	if (heights[row+1] < d.height) heights[row+1] = d.height;
}
//public Menu getMenuFor(int row,int col)
//{
//	return new Menu(mString.split("One|Two|Three is long|-|Four"),"");
//}
/**
* Returns the row height.
**/
//===================================================================
public int calculateRowHeight(int row)
//===================================================================
{
	if (row < -1 || row >= numRows) return 0;
	if (fillToEqualHeights) return super.calculateRowHeight(row);
	return heights[row+1];
}
/**
* Returns the column width.
**/
//===================================================================
public int calculateColWidth(int col)
//===================================================================
{
	if (col < -1 || col >= numCols) return 0;
	if (fillToEqualWidths) return super.calculateColWidth(col);
	return widths[col+1];
}

private FontMetrics lastFontMetrics;
/**
* This calculates the correct sizes for the rows and columns. Make sure you call
* setDataAndHeaders() first.
**/
//===================================================================
public void calculateSizes(FontMetrics fm)
//===================================================================
{
	lastFontMetrics = fm;
	numRows = data.rows;
	numCols = data.columns;
	if (columnHeaders != null && columnHeaders.size() > numCols)
		numCols = columnHeaders.size();
	if (rowHeaders != null && rowHeaders.size() > numRows)
		numRows = rowHeaders.size();
	widths = new int[numCols+1];
	heights = new int[numRows+1];
	Dimension d = new Dimension();
	for (int r = -1; r<numRows; r++)
		for (int c = -1; c<numCols; c++)
			checkCellSize(fm,r,c,d);
}
//===================================================================
public Dimension getCellPreferredSize(int row,int col,FontMetrics fm,Dimension dest)
//===================================================================
{
	dest = Dimension.unNull(dest);
	if (widths == null || heights == null) return dest;
	dest.width = widths[col+1];
	dest.height = heights[row+1];
	return dest;
}
/**
Call this method if you have changed any data in the grid for
the TableModel. It will recalculate the number of rows and cell sizes based on
the same font metrics that was used last time.
 * @param updateTable if this is true then a call to update(true) will be made
 * on the containing TableControl.
 */
//===================================================================
public void updateData(boolean updateTable)
//===================================================================
{
	FontMetrics fc = lastFontMetrics;
	if (fc == null)
		if (table == null) fc = mApp.mainApp.getFontMetrics();
		else fc = table.getFontMetrics();
	calculateSizes(fc);
	if (updateTable && table != null) table.update(true);
}
/*
//-------------------------------------------------------------------
void startingPaint(Graphics g)
//-------------------------------------------------------------------
{
	//if (controls == null) return;
	//for (int i = 0; i<controls.size(); i++) table.remove((Control)controls.get(i));
}
*/
//##################################################################
}
//##################################################################

