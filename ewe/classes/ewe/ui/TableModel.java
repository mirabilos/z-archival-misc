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
import ewe.util.Vector;

//##################################################################
public class TableModel implements UIConstants ,EventListener{
//##################################################################
/**
* This is the gap (in pixels) between the data in a cell and its borders. If getCellInsets() returns
* null, then this gap will be used as the top,left,bottom and right insets.
**/
public int gap = 2;
/**
* If this is true then the data will be clipped to fit into the cells.
* If you know the data will not need clipping then switching this off
* will produce faster cell drawing. By default it is off.
**/
public boolean clipData = false;
/**
* The number of rows in the table.
**/
public int numRows = 4;
/**
* The number of columns in the table.
**/
public int numCols = 4;
/**
* Specifies whether the table has column headers.
**/
public boolean hasColumnHeaders = true;
/**
* Specifies whether the table has row headers.
**/
public boolean hasRowHeaders = true;
/**
* Specifies whether the table should have all cell widths equal
* and fit exactly in the display area.
**/
public boolean fillToEqualWidths = false;
/**
* Specifies whether the table should have all cell heights equal
* and fit exactly in the display area.
**/
public boolean fillToEqualHeights = false;
/**
* Specifies whether the table has a preferred size.
**/
public boolean hasPreferredSize = false;
/**
* Specifies whether the table can scroll horizontally.
**/
public boolean canHScroll = true;
/**
* Specifies whether the table can scroll vertically.
**/
public boolean canVScroll = true;
/**
* This is the table which contains the model.
**/
protected TableControl table;
/**
* This specifies how many columns you can scroll as a unit.
**/
public int horizontalScrollUnit = 1;
/**
* This specifies how many rows you can scroll as a unit.
**/
public int verticalScrollUnit = 1;
/**
* This must be set true if you have data in columns which span across multiple columns.
**/
public boolean hasSpanningColumns = false;
/**
* This must be set true if you have data in rows which span across multiple rows.
**/
public boolean hasSpanningRows = false;
/**
* This will shade every other row.
**/
public boolean shadeAlternateRows = false;
/**
* This will result in an adjustment to one column affecting all columns. It does not count for the
* header column.
**/
public boolean allColumnsSameSize = false;
/**
* This will result in an adjustment to one row affecting all row. It does not count for the
* header row.
**/
public boolean allRowsSameSize = false;
/**
* This is set to the width of the 'X' character of the Font of the table once the control has been made. It is
* set in the made() method.
**/
public int charWidth = 10;
/**
* This is set to the height of the Font of the table once the control has been made. It is
* set in the made() method.
**/
public int charHeight = 20;

public int preferredRows = 0;
public int preferredCols = 0;
/**
* This specifies the size of the cursor, in cells. A null value indicates a standard
* cursor one cell wide and high. A Dimension with either width or height being 0 indicates
* no cursor. A Dimension with width being -1 indicates that the entire row must be selected.
* A Dimension with height being -1 indicates that the entire column must be selected.
**/
public Dimension cursorSize;

//===================================================================
public boolean hasActiveControls()
//===================================================================
{
	if (table == null) return false;
	return table.children != null;
}
public void onEvent(Event ev){}
/**
* This is called by the table to tell the model that it wants to select
* a particular cell. The model should then call table.addToSelection() to
* add it to the selection. Alternately the model can add more or different
* cells.
**/
//===================================================================
public void select(int row,int col,boolean selectOn)
//===================================================================
{
	if (selectOn){
		if (row == -1 && col == -1)
			Rect.buff.set(0,0,numCols+1,numRows+1);
		else{
			Dimension cs = table.getCursorSize();
			int w = cs.width; if (w < 0) w = numCols;
			int h = cs.height; if (h < 0) h = numRows;
			if (w != 0 && h != 0){
				if (row > 0) row = h == 0 ? 0 : (row/h)*h;
				if (col > 0) col = w == 0 ? 0 : (col/w)*w;
			}else{
				w = h = 1;
			}
			if (row == -1)
				Rect.buff.set(col,0,w,numRows+1);
			else if (col == -1)
				Rect.buff.set(0,row,numCols+1,h);
			else
				Rect.buff.set(col,row,w,h);
		}
		table.addToSelection(Rect.buff,false);
		//ewe.sys.Vm.debug("Buf: "+Rect.buff);
	}
}

/**
* This is used to report whether a quick pixel scroll can be done on the table.
**/
//===================================================================
public boolean canScreenScroll()
//===================================================================
{
	return !hasActiveControls();
}
//-------------------------------------------------------------------
int doScrollTo(int current,int requested,int scrollAction,boolean horizontal,int resolution)
//-------------------------------------------------------------------
{
	if (resolution == 1) return requested;
	int cm = current/resolution;
	int rm = requested/resolution;
	if (rm == cm) if (requested > current) rm++;
	if (scrollAction == IScroll.TrackTo)
		if (current-requested > -resolution && current-requested < resolution && cm*resolution == current)
			return current;
	return rm*resolution;
}
/**
* This is called by the table to tell the model that it wants to scroll either
* vertically or horizontally. The method should return the correct position for
* the table to scroll to, given the current position and the requested position.
* By default it returns the requested position.
**/
//===================================================================
public int scrollTo(int current,int requested,int scrollAction,boolean horizontal)
//===================================================================
{
	if (horizontal) return doScrollTo(current,requested,scrollAction,true,horizontalScrollUnit);
	else return doScrollTo(current,requested,scrollAction,true,verticalScrollUnit);
}
ewe.util.Vector colAdjustments;

//===================================================================
final private Point getColAdjust(int col)
//===================================================================
{
	if (allColumnsSameSize && col > 0) return getColAdjust(0);
	if (colAdjustments != null)
		for (int i = 0; i<colAdjustments.size(); i++){
			Object got = colAdjustments.get(i);
			if (got instanceof Point){
				Point p = (Point)got;
				if (p.x == col) return p;
			}
		}
	return null;
}

//-------------------------------------------------------------------
final private ewe.util.Vector getRowAdjustments()
//-------------------------------------------------------------------
{
	Vector v = new Vector();
	if (colAdjustments == null) return v;
	for (int i = 0; i<colAdjustments.size(); i++)
		if (colAdjustments.get(i) instanceof Dimension)
			v.add(colAdjustments.get(i));
	return v;
}

//-------------------------------------------------------------------
protected final void remapColumns(int [] oldPositions)
//-------------------------------------------------------------------
{
	if (allColumnsSameSize) return;
	Vector v = getRowAdjustments();
	if (oldPositions != null)
		for (int i = 0; i<oldPositions.length; i++){
			Point p = getColAdjust(oldPositions[i]);
			if (p != null) v.add(new Point(i,p.y));
		}
	colAdjustments = v;
	if (colAdjustments.size() == 0) colAdjustments = null;
}
//===================================================================
public void clearCellAdjustments()
//===================================================================
{
	colAdjustments = null;
}
//===================================================================
void setColAdjust(int col,int dx)
//===================================================================
{
	if (allColumnsSameSize && col > 0) col = 0;
	Point p = getColAdjust(col);
	if (p == null){
		p = new Point(col,0);
		if (colAdjustments == null) colAdjustments = new ewe.util.Vector();
		colAdjustments.add(p);
	}
	p.y += dx;
	int width = calculateColWidth(col);
	if (getMaxColWidth(col) >= 0)
		if (width+p.y > getMaxColWidth(col)) p.y = getMaxColWidth(col)-width;
	if (width+p.y < getMinColWidth(col)) p.y = getMinColWidth(col)-width;
}
//-------------------------------------------------------------------
protected int getMinColWidth(int col) {return 6;}
/**
* If this returns -1 there will be no maximum.
**/
protected int getMaxColWidth(int col) {return -1;}
/**
* If this returns -1 there will be no maximum.
**/
protected int getMaxRowHeight(int row) {return -1;}
protected int getMinRowHeight(int row) {return 6;}
//-------------------------------------------------------------------
//===================================================================
Dimension getRowAdjust(int row)
//===================================================================
{
	if (allRowsSameSize && row > 0) return getRowAdjust(0);
	if (colAdjustments != null)
		for (int i = 0; i<colAdjustments.size(); i++){
			Object got = colAdjustments.get(i);
			if (got instanceof Dimension){
				Dimension p = (Dimension)got;
				if (p.height == row) return p;
			}
		}
	return null;
}
//===================================================================
void setRowAdjust(int row,int dy)
//===================================================================
{
	if (allRowsSameSize && row > 0) row = 0;
	Dimension p = getRowAdjust(row);
	if (p == null){
		p = new Dimension(0,row);
		if (colAdjustments == null) colAdjustments = new ewe.util.Vector();
		colAdjustments.add(p);
	}
	p.width += dy;
	int height = calculateRowHeight(row);
	if (getMaxRowHeight(row) >= 0)
		if (height+p.width > getMaxRowHeight(row)) p.width = getMaxRowHeight(row)-height;
	if (height+p.width < getMinRowHeight(row)) p.width = getMinRowHeight(row)-height;
}

/**
* Do not override this. Instead override calculateColWidth().
**/
//===================================================================
public final int getColWidth(int col)
//===================================================================
{
	int width = calculateColWidth(col);
	Point p = getColAdjust(col);
	if (p != null) width += p.y;
	return width;
}
/**
* Do not override this. Instead override calculateRowHeight().
**/
//===================================================================
public final int getRowHeight(int row)
//===================================================================
{
	int height = calculateRowHeight(row);
	Dimension p = getRowAdjust(row);
	if (p != null) height += p.width;
	return height;
}

/**
 * This is called by calculateColWidth(). It should give the number of characters that should
 * fit in a column.
 */
//-------------------------------------------------------------------
protected int calculateTextCharsInColumn(int col)
//-------------------------------------------------------------------
{
	return 10;
}
/**
 * This is called by calculateColWidth(). It should give the number of text lines that should
 * fit in a row.
 */
//-------------------------------------------------------------------
protected int calculateTextLinesInRow(int row)
//-------------------------------------------------------------------
{
	return 1;
}
/**
* This returns the full width of the column. If the requested col is -1 (header col) and there are
* no row headers it should return 0.
**/
//===================================================================
public int calculateColWidth(int col)
//===================================================================
{
	if ((col == -1) && !hasRowHeaders) return 0;
	if (fillToEqualWidths){
		int cols = numCols;
		if (hasRowHeaders) {
			cols++;
			col++;
		}
		Dimension size = table.getSize(Dimension.buff);
		if (size.width == 0) return 0;
		int w = size.width/cols;
		if (col < size.width%cols) w++;
		return w;
	}else return charWidth*calculateTextCharsInColumn(col);
}
/**
* This returns the full height of the row. If the requested row is -1 (header row) and there are
* no column headers it should return 0.
**/
//===================================================================
public int calculateRowHeight(int row)
//===================================================================
{
	if ((row == -1) && !hasColumnHeaders) return 0;
	if (fillToEqualHeights){
		int rows = numRows;
		if (hasColumnHeaders) {
			rows++;
			row++;
		}
		Dimension size = table.getSize(Dimension.buff);
		if (size.height == 0) return 0;
		int h = size.height/rows;
		if (row < size.height%rows) h++;
		return h;
	}else return (charHeight*calculateTextLinesInRow(row))+6;
}

/**
* Set this to be the default insets for each cell. By default it is null.
**/
public Insets cellInsets = null;
/**
* Get the insets of the data in the specified cell.
* If "insets" is null a new one will be created. If this function
* returns null it is to be interpreted as zero insets. Insets are used for all data
* EXCEPT Controls.
**/
//===================================================================
public Insets getCellInsets(int row,int col,Insets insets)
//===================================================================
{
	return cellInsets;
}
/**
* Get the bounds of the specified cell within the table - with 0,0 being
* the upper left corner of cell(table.firstRow,table.firstCol) - Note that the headers are considered
* outside of the table in this case (header indexes are -1). This is done
* because when the table is being scrolled the headers will not move and
* be handled separately. Dest must NOT be null.
* @param row
* @param col
* @param dest
* @return true if the cell is at least partly visible, false if the cell is not visible at all.
*/
//===================================================================
public boolean getCellRect(int row,int col,Rect dest)
//===================================================================
{
/*
	int x = -getColWidth(-1), y = -getRowHeight(-1);
	for (int i = -1; i<col; i++) x += getColWidth(i);
	for (int i = -1; i<row; i++) y += getRowHeight(i);
	dest.x = x; dest.y = y; dest.width = getColWidth(col); dest.height = getRowHeight(row);
*/
	int sx = getColWidth(-1), sy = getRowHeight(-1);
	int w = table.width-sx, h = table.height-sy;
	dest.x = w; dest.y = h; // Put it outside the table.
	dest.width = col >= numCols ? 0 : getColWidth(col); dest.height = row >= numRows ? 0 : getRowHeight(row);
	int x = 0, y = 0;
	dest.x = dest.y = 0;
	if ((row < table.firstRow && row != -1)|| (col < table.firstCol && col != -1))
		return false;
	for (int i = table.firstCol; i<col && x<w; i++) x += getColWidth(i);
	if (x >= w) return false;
	dest.x = x;
	for (int i = table.firstRow; i<row && y<h; i++) y += getRowHeight(i);
	if (y >= h) return false;
	dest.y = y;
	return true;
}
/**
* This may return any object. If getCellText() returns a String or array of
* Strings then the data returned by this will be ignored and the text will
* be displayed as the contents via paintTableCellText().
*
* Otherwise - if getCellText() return null - a call to paintTableCellData()
* will be used, with the data returned by this method.
*
* By default, paintTableCellData() calls toString() on the data and displays
* it in the cell. To display other things, you will have to override
* paintTableCellData().
**/
//===================================================================
public Object getCellData(int row,int col)
//===================================================================
{
	return null;
}
/**
* This may return a String or an array of Strings (for multiline text) or
* it may return null. If it returns null then the value returned by getCellData(r,c)
* will be used to paint the cell contents.
**/
//===================================================================
public Object getCellText(int row,int col)
//===================================================================
{
	Object obj = getCellData(row,col);
	if (obj instanceof String || obj instanceof String[]) return obj;
	return null;
}

//===================================================================
public Dimension getCellPreferredSize(int row,int col,FontMetrics fm,Dimension dest)
//===================================================================
{
	dest = Dimension.unNull(dest);
	Object txt = getCellText(row,col);
	String [] all = getLinesFor(txt);
	if (all == null) return dest.set(20,20);
	Graphics.getSize(fm,all,0,all.length,dest);
	dest.width += 8;
	dest.height += 8;
	return dest;
}
/**
* In order for this to do anything you must set "hasPreferredSize" to true.
**/
//===================================================================
public Dimension getPreferredSize(Dimension dest)
//===================================================================
{
	dest = Dimension.unNull(dest).set(10,10);
	if (!hasPreferredSize) {
		if (preferredRows != 0){
			if (preferredCols == 0) preferredCols = numCols;
		}else if (preferredCols != 0){
			if (preferredRows == 0) preferredRows = numRows;
		}
		if (preferredRows != 0 && preferredCols != 0){
			int w = 0, h = 0;
			for (int i = hasRowHeaders ? -1 : 0; i<preferredCols; i++)
				w += calculateColWidth(i);
			for (int i = hasColumnHeaders ? -1 : 0; i<preferredRows; i++)
				h += calculateRowHeight(i);
			int extra = 0;
			if (preferredCols != numCols+(hasRowHeaders ? 1 : 0) || preferredRows != numRows+(hasColumnHeaders ? 1 : 0)) extra = 16;
			dest.width = w+extra;
			dest.height = h+extra;
		}
		return dest;
	}
	FontMetrics fm = table.getFontMetrics();
	if (fm == null) return dest;
	int width = 0;
	int height = 0;
	Dimension d = new Dimension();
	for (int r = hasColumnHeaders ? -1 : 0; r<numRows; r++){
		int h = 0, w = 0;
		for (int c = hasRowHeaders ? -1 : 0; c<numCols; c++){
			getCellPreferredSize(r,c,fm,d);
			w += d.width;
			if (d.height > h) h = d.height;
		}
		if (w > width) width = w;
		height += h;
	}
	return dest.set(width,height);
}
//===================================================================
protected int fixBorder(int style,int row,int col,boolean flat)
//===================================================================
{
	if (flat) style = ((style|table.BDR_OUTLINE) & ~(table.BF_RECT))|table.BF_BOTTOM|table.BF_RIGHT;
	if (true){//((style & table.BDR_OUTLINE) == table.BDR_OUTLINE) || flat){
		if ((row == table.firstRow && !hasColumnHeaders) || row == -1) style |= table.BF_TOP;
		if ((col == table.firstCol && !hasRowHeaders) || col == -1) style |= table.BF_LEFT;
	}
	return style;
}
/**
Returns if there is an active CellControl at the specified row and column.
**/
//===================================================================
public boolean isActiveCellControl(int row, int column)
//===================================================================
{
	return activeCellControl != null && activeCellControl.cell.y == row && activeCellControl.cell.x == column;
}
/**
* Get the attributes for a cell. Note that even though a TableCellAttributes is passed to
* the method, for effeciency a completely different one may be returned. You should therefore
* not attempt to modify or reuse the one that is returned.
**/
//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta)
//===================================================================
{
	if (ta == null) ta = new TableCellAttributes();
	ta.flat |= table.globalDrawFlat || table.hasModifier(DrawFlat,false);
	ta.alignment = Control.CENTER;
	ta.anchor = Control.CENTER;
	ta.clipCellData = clipData;
	ta.hSpan = ta.vSpan = 1;
	ta.drawImageOptions = 0;
	ta.foreground = Color.Black;
	if (row == -1 || col == -1) {
		ta.fillColor = Color.LightGray;
		ta.borderStyle = fixBorder(
		/*ta.EDGE_RAISED|ta.BF_BOTTOM|*/
		(Control.standardEdge)|ta.BF_BOTTOM|ta.BF_RIGHT
		,row,col,ta.flat);
		if (ta.flat) ta.borderColor = Color.Black;
	}else{
		ta.fillColor = Color.White;
		ta.borderStyle = fixBorder(
		//(mInput.inputEdge)//
		ta.BDR_OUTLINE
		|ta.BF_BOTTOM|ta.BF_RIGHT,row,col,ta.flat);
		ta.borderColor = (((mInput.inputEdge|BF_RECT) & ta.EDGE_ETCHED) == ta.EDGE_ETCHED) && !ta.flat ? Color.LighterGray : Color.Black;
	}
	if (isSelected && row != -1 && col != -1) ta.fillColor = Color.LightGray;
	else if (isActiveCellControl(row,col)) ta.fillColor = Color.LightGreen;
	else
		if (row != -1 && col != -1)
			if (shadeAlternateRows && (row & 1) == 1) ta.fillColor = Color.LighterGray;
	ta.text = getCellText(row,col);
	ta.data = getCellData(row,col);
	if (activeCellControl != null && !activeCellControl.isInvisible)
		if (activeCellControl.cell.y == row && activeCellControl.cell.x == col){
			ta.text = null;
			ta.data = activeCellControl.control;
		}
	return ta;
}
/**
* This should return whether or not a particular cell may be selected. Note that
* selecting a column or row header (row == -1 or col == -1) results in the entire
* row/column being selected.
**/
//===================================================================
public boolean canSelect(int row,int col)
//===================================================================
{
	return true;
}
/**
* This finds the range of cells in the given area. The area parameter must be set up as follows:
* area.x = col of top left cell.
* area.y = row of top left cell.
* area.width = width of area on the screen.
* area.height = height of area on the screen.
* When the function returns area.width and area.height will be modified as:
* area.width = number of cells displayed horizontally.
* area.height = number of cells displayed vertically.
*
* If onlyCompletely is true the number of cells shows how many cells are COMPLETELY in the area.
**/
//===================================================================
public void findCellsInArea(Rect area,boolean onlyCompletely)
//===================================================================
{
	int w = 0;
	if (hasSpanningColumns){
		//area.x = 0;
		w = numCols;
	}else
		for (int c = area.x; c < numCols && area.width > 0; c++){
			area.width -= getColWidth(c);
			if (area.width >= 0) w++;
			else if (!onlyCompletely) w++;
		}
	int h = 0;
	for (int r = area.y; r < numRows && area.height > 0; r++){
		area.height -= getRowHeight(r);
		if (area.height >= 0) h++;
		else if (!onlyCompletely) h++;
	}
	area.width = w;
	area.height = h;
}
/*
* Buffer variables.
*/
protected TableCellAttributes tca = new TableCellAttributes();
protected Rect rect = new Rect();
protected Insets insets = new Insets(0,0,0,0);

//-------------------------------------------------------------------
protected void inset(int row,int col,Rect rect)
//-------------------------------------------------------------------
{
	Insets in = getCellInsets(row,col,insets);
	if (in == null)
 		if (gap != 0){
			in = insets;
			insets.top = insets.bottom = insets.left = insets.right = gap;
		}else
			return;
	Insets.apply(in,rect);
}
/**
* The real biz. Paint a particular table cell. The Graphics g will be translated
* so that its origin will be the top left of cell(0,0). Remember that headers are
* on column -1 and row -1.
**/
//===================================================================
public void paintTableCell(TableControl tc,Graphics g,int row,int col)
//===================================================================
{
	g.setFont(table.getFont());
	int extraWidth = 0;
	while(col != 0){
		TableCellAttributes a = getCellAttributes(row,col,false,tca);
		if (a.hSpan != 0) break;
		if (a.hSpan > 1) hasSpanningColumns = true;
		extraWidth += getColWidth(col);
		col--;
	}
	if (!getCellRect(row,col,rect)) return;
	rect.width += extraWidth;
	boolean sel = tc.isSelected(row,col);
	TableCellAttributes a = getCellAttributes(row,col,sel,tca);
	a.row = row; a.col = col; a.isSelected = sel;
	a = tc.overrideAttributes(a);
	Rect clip = null;
	if (a.clipCellData) {
		clip = g.getClip(oldClip);
		smallClip.set(rect);
		if (clip != null) smallClip.getIntersection(clip,smallClip);
		g.setClip(smallClip.x,smallClip.y,smallClip.width,smallClip.height);
	}
	g.draw3DRect(rect,a.borderStyle,a.flat,a.fillColor,a.borderColor);
	inset(row,col,rect);
	g.setColor(tca.foreground);
	if (a.fillColor != null) g.setBackground(a.fillColor);
	if (tca.text != null)
		paintTableCellText(tc,g,row,col,tca,rect,tca.text);
	else
		paintTableCellData(tc,g,row,col,tca,rect,tca.data);
	if (a.clipCellData)
		if (clip != null) g.setClip(clip.x,clip.y,clip.width,clip.height);
		else g.clearClip();
}
private static Rect rct = new Rect();
//-------------------------------------------------------------------
protected void deferPaintTableCell(TableControl tc,Graphics g,int row,int col)
//-------------------------------------------------------------------
{
	paintTableCell(tc,g,rct.set(col,row,1,1));
}
Rect oldClip = new Rect(), smallClip = new Rect();
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
	/* For testing only. Doesn't actually work properly - the graphics is not translated correctly.
	if (true){
		for (int r = cells.y; r<lr; r++)
			for (int c = cells.x; c<lc; c++)
				paintTableCell(tc,g,r,c);
		return;
	}
	*/
	int x = 0, y = 0;
	Rect clip = g.getClip(oldClip);
	for (int r = cells.y; r<lr; r++){
		x = 0;
		int ch = getRowHeight(r);
		int fc = cells.x;
		while(fc > 0){
			TableCellAttributes a = getCellAttributes(r,fc,tc.isSelected(r,fc),tca);
			if (a.hSpan == 0) {
				fc--;
				x -= getColWidth(fc);
			}else {
				if (a.hSpan > 1) hasSpanningColumns = true;
				break;
			}
		}
		for (int c = fc; c<lc; c++){
			TableCellAttributes a = getCellAttributes(r,c,tc.isSelected(r,c),tca);
			int cw = getColWidth(c);
			if (a.hSpan == 0) continue;
			if (a.hSpan > 1) hasSpanningColumns = true;
			for (int c2 = 1; c2 != a.hSpan && c2+c<lc; c2++)
				cw += getColWidth(c+c2);
			rect.x = x; rect.y = y;
			rect.width = cw; rect.height = ch;
			a = tc.overrideAttributes(a);
			if(a.clipCellData){
				smallClip.set(rect);
				if (clip != null) smallClip.getIntersection(clip,smallClip);
				g.setClip(smallClip.x,smallClip.y,smallClip.width,smallClip.height);
			}else if (clip != null) g.setClip(clip.x,clip.y,clip.width,clip.height);
				else g.clearClip();
			g.draw3DRect(rect,a.borderStyle,a.flat,a.fillColor,a.borderColor);
			g.setColor(tca.foreground);
			if (a.fillColor != null) g.setBackground(a.fillColor);
			inset(r,c,rect);
			if (tca.text != null)
				paintTableCellText(tc,g,r,c,tca,rect,tca.text);
			else
				paintTableCellData(tc,g,r,c,tca,rect,tca.data);
			x += cw;
		}
		y += ch;
	}
	if (clip != null) g.setClip(clip.x,clip.y,clip.width,clip.height);
	else g.clearClip();
}

private String [] single = new String[1];
//-------------------------------------------------------------------
protected String [] getLinesFor(Object obj)
//-------------------------------------------------------------------
{
	if (obj instanceof String []) return (String [])obj;
	else if (obj instanceof String){
		String s = (String)obj;
		if (s.indexOf('\n') != -1)
			return ewe.util.mString.split(s,'\n');
		single[0] = s;
		return single;
	}else return null;
}
/*
//-------------------------------------------------------------------
private final Control added(Control c)
//-------------------------------------------------------------------
{
	return c.getParent();
}
*/
//-------------------------------------------------------------------
private final void removeControl(Control c)
//-------------------------------------------------------------------
{
	table.remove(c);
}
//-------------------------------------------------------------------
private final void paintCellControl(Graphics g,int row,int col,TableCellAttributes tca,Rect r,Control c,boolean removeAfter)
//-------------------------------------------------------------------
{
	if (table.contains(c)) removeControl(c);
	Rect r2 = table.getScreenRect(row,col,null);
	Insets in = getCellInsets(row,col,insets);
	if (in == null){
		in = insets;
		insets.top = insets.bottom = insets.left = insets.right = gap;
	}
	int x = in.left;// + ((col < 0) ? 0 : getColWidth(-1));
	int y = in.top;// + ((row < 0) ? 0 : getRowHeight(-1));
	c.backGround = tca.fillColor;
	//c.foreGround = table.getForeground();
	table.add(c);
	int was = c.modify(Invisible,0);
	c.make(false);
	c.restore(was,Invisible);
	c.getPreferredSize(Dimension.buff);
	int myW = Dimension.buff.width, myH = Dimension.buff.height;
	int val = tca.anchor;
	if ((val & (HEXPAND|HCONTRACT|VEXPAND|VCONTRACT)) == 0){
		if ((val & (LEFT|RIGHT)) == (LEFT|RIGHT)) val |= HFILL;
		if ((val & (TOP|BOTTOM)) == (TOP|BOTTOM)) val |= VFILL;
	}
	int w = r2.width-in.left-in.right;
	int h = r2.height-in.top-in.bottom;
	if (w < 0) w = 1;
	if (h < 0) h = 1;
	if (myW > w && ((val & HCONTRACT) != 0)) myW = w;
	if (myW < w && ((val & HEXPAND) != 0)) myW = w;
	if (myH > h && ((val & VCONTRACT) != 0)) myH = h;
	if (myH < h && ((val & VEXPAND) != 0)) myH = h;
	if ((val & RIGHT) != 0) x += (w-myW);
	else if ((val & LEFT) != 0);
	else x += (w-myW)/2;
	if ((val & BOTTOM) != 0) y += (h-myH);
	else if ((val & TOP) != 0);
	else y += (h-myH)/2;
	w = myW;
	h = myH;
	c.setRect(r2.x+x,r2.y+y,w,h);
	//ewe.sys.Vm.debug(c+", "+(r2.x+x)+", "+(r2.y+y)+", "+w+", "+h+", inside: "+r2.width+" x "+r2.height);
	//table.add(c);
	g.translate(r.x+x-in.left,r.y+y-in.top);
	c.repaintNow(g,null);
	g.translate(-r.x-x+in.left,-r.y-y+in.top);
	if (removeAfter) table.remove(c);
}
/**
* Paint the data within the cell. This can print text, IImages and Controls.
**/
//-------------------------------------------------------------------
protected void paintTableCellData(TableControl tc,Graphics g,int row,int col,TableCellAttributes tca,Rect r,Object data)
//-------------------------------------------------------------------
{
	if (data == null) return;
	if (data instanceof String || data instanceof String [])
		paintTableCellText(tc,g,row,col,tca,r,data);
	else if (data instanceof IImage){
		IImage mi = (IImage)data;
		Rect r2 = new Rect().set(0,0,mi.getWidth(),mi.getHeight());
		g.anchor(r2,r,tca.anchor);
		((IImage)data).draw(g,r2.x,r2.y,tca.drawImageOptions);
	}else if (data instanceof ControlProxy){
		paintCellControl(g,row,col,tca,r,((ControlProxy)data).control,true);
	}else if (data instanceof Control){
		paintCellControl(g,row,col,tca,r,(Control)data,false);
	}else
		paintTableCellText(tc,g,row,col,tca,r,data.toString());
}

/**
* Paint the data within the cell.
**/
/*
//===================================================================
protected void paintTableCellData(TableControl tc,Graphics g,int row,int col,TableCellAttributes tca,Rect r,Object data)
//===================================================================
{
	if (data == null) return;
	if (data instanceof String || data instanceof String []) {
		paintTableCellText(tc,g,row,col,tca,r,data);
	}else if (data instanceof IImage){
		IImage mi = (IImage)data;
		Rect r2 = new Rect().set(0,0,mi.getWidth(),mi.getHeight());
		g.anchor(r2,r,tca.anchor);
		((IImage)data).draw(g,r2.x,r2.y,tca.drawImageOptions);
	}else
		paintTableCellText(tc,g,row,col,tca,r,data.toString());
}
*/
//===================================================================
protected void paintTableCellText(TableControl tc,Graphics g,int row,int col,TableCellAttributes tca,Rect r,Object text)
//===================================================================
{
	String [] out = getLinesFor(text);
	if (out == null) return;
	if (tca.fontMetrics == null) tca.fontMetrics = tc.getFontMetrics();
	g.setFont(tca.fontMetrics.getFont());
	g.setColor(tca.foreground);
	g.drawText(tca.fontMetrics,out,r,tca.alignment,tca.anchor,0,out.length);
}
/**
* This is called when the TableControl is about to do a repaint.
**/
//===================================================================
void startingPaint(Graphics g){}
//===================================================================
/**
* This is called after the TableControl has been made.
**/
//===================================================================
public void made()
//===================================================================
{
	if (table != null) {
		FontMetrics fm = table.getFontMetrics();
		charWidth = fm.getCharWidth('X');
		charHeight = fm.getAscent()+fm.getDescent();
	}
}
/**
* This is called after the TableControl has been resized.
**/
//==================================================================
public void resized(int width,int height){}
//==================================================================


/**
 * Return a Tool Tip for the x,y position (in pixels) on the table. In this
	method you can do:
	<pre>
	Point cell = table.cellAtPoint(x,y,null);
	if (cell == null) return null; //Not on a cell.
	// Now cell.x will have the column and cell.y will have the row
	// of the cell the mouse is over and you can then display a tool tip for that cell.
	<pre>
 * @param x The x co-ordinate in pixels of the mouse.
 * @param y The y co-ordinate in pixels of the mouse.
 * @return An acceptable ToolTip object.
 */
//===================================================================
public Object getToolTip(int x,int y) {return null;}
//===================================================================


public Menu getMenuFor(int row,int col) {return null;}
public Menu getMenuOutsideCells(Point screenPoint) {return null;}
public boolean popupMenuEvent(MenuEvent ev){return false;}

public CellControl activeCellControl;
/**
* If this returns true, then the TableControl will not process a pen press any
* further.
**/
//===================================================================
public boolean penPressed(Point onTable,Point cell)
//===================================================================
{
	return checkControlFor(onTable,cell,table.ByDeferredPen);
}
/**
 * This tells the model to close any active control. It returns true if a
 * control was actually active.
 */
//===================================================================
public boolean closeActiveControl()
//===================================================================
{
	if (activeCellControl == null) return false;
	activeCellControl.exit();
	return true;
}
/**
* This tells the model to select the row of a cell whenever that cell is edited. By default it is true.
**/
public boolean selectRowWhenEditing = true;

//-------------------------------------------------------------------
protected void startedEditing(Point cell)
//-------------------------------------------------------------------
{
}
/**
* This is called by the TableControl before it processes the event. If this method
* returns true, then the TableControl will not process the key.
**/
//===================================================================
public boolean onKeyEvent(KeyEvent ev)
//===================================================================
{
	return false;
}
//===================================================================
public boolean doHotKey(KeyEvent ev)
//===================================================================
{
	return false;
}
//-------------------------------------------------------------------
protected final boolean checkControlFor(Point onTable,Point cell,int how)
//-------------------------------------------------------------------
{
	if (cell == null) return activeCellControl != null;
	if (activeCellControl != null)
		if (!activeCellControl.cell.equals(cell))
			activeCellControl.exit();
		else
			return true;
	if ((activeCellControl = getCellControlFor(cell)) == null) return false;
	if (selectRowWhenEditing) table.selectAndUpdate(cell.y,-1);
	boolean moved = false;
	if (onTable == null || !activeCellControl.takeFirstPress)
		if (moved = table.scrollToVisible(cell.y,cell.x))
			table.update(true);
	activeCellControl.show(how);
	if (onTable != null && !moved && activeCellControl.takeFirstPress){
		Control c = Gui.focusedControl();
		if (c instanceof EditControl) ((EditControl)c).justGotFocus = true;
		table.transferPenPress(c);
	}
	startedEditing(cell);
	return true;
}

//-------------------------------------------------------------------
protected final void show(CellControl cc,int how)
//-------------------------------------------------------------------
{
	if (activeCellControl != null){
		if (activeCellControl == cc) return;
		activeCellControl.exit();
		activeCellControl = null;
	}
	activeCellControl = cc;
	activeCellControl.show(how);
}
/**
 * Get a Control to edit data within a particular cell within a CellControl object.
 * This is called when the pen is
 * pressed on a Control. By default this method calls getControlFor() and if that does
 * not return null, then a new CellControl is created and returned for that Control.
 * @param cell the cell the CellControl is for.
 * @return a CellControl used to edit data for a particular cell, or null if editing
 * that cell data is not allowed.
 */
//-------------------------------------------------------------------
protected CellControl getCellControlFor(Point cell)
//-------------------------------------------------------------------
{
	Control c = getControlFor(cell.y,cell.x);
	if (c == null) return null;
	return new CellControl(cell,c);
}
/**
 * This is called by getCellControlFor() and is used to return a Control to allow editing
 * of a cell's contents.
 * By default this method returns null.
 * @param row the row of the cell.
 * @param col the column of the cell.
 * @return a Control used to edit data for a particular cell, or null if editing
 * that cell data is not allowed.
 */
//-------------------------------------------------------------------
protected Control getControlFor(int row,int col) {return null;}
//-------------------------------------------------------------------

/**
This class is used to contain and control a Control that is used to edit data
within a Cell in the TableModel.
**/
//##################################################################
public class CellControl implements EventListener{
//##################################################################
/** The x (column) and y (row) of the cell being edited. **/
public Point cell;
/** The Control that is being used for editing. **/
public Control control;
/** If this is true then the Control will "exit" if it loses focus - usually this means
disappearing and leaving only the cell data itself. This is true by default.**/
public boolean exitOnLostFocus = true;
/** If this is true then the same mouse/pen press used to activate the CellControl is passed
to the Control itself. This is true by default.**/
public boolean takeFirstPress = true;
/** If this is true then as soon as data is changed the Control will exit - usually
disappearing and leaving only the cell data itself. This is false by default.**/
public boolean exitOnDataChange = false;
/** If this is true then as soon as any popup Frame shown by the Control is closed the
Control will exit - usually
disappearing and leaving only the cell data itself. This is false by default.**/
public boolean exitOnPopupClosed = false;
/** If this is true then the Control is assumed to show a popup frame when
displayed. In this case the Control itself is made invisible and only the
popup frame will be shown. This is useful for controls like mChoice or NumberEntry.**/
public boolean popupOnly = false;

int oldModifiers, oldControlModifiers;
boolean isInvisible;
//-------------------------------------------------------------------
protected void setControl(Control control)
//-------------------------------------------------------------------
{
	if (control == null) return;
	this.control = control;
	control.addListener(this);
	control.exitKeys = new int[]{IKeys.TAB,IKeys.UP,IKeys.DOWN,IKeys.LEFT,IKeys.RIGHT,IKeys.ENTER,IKeys.ESCAPE};
}
//===================================================================
public CellControl(Point cell,Control control)
//===================================================================
{
	this.cell = new Point(cell.x,cell.y);
	setControl(control);
}
boolean exited = false;
//===================================================================
public void exit()
//===================================================================
{
	if (exited) return;
	table.restore(oldModifiers,table.NoFocus);
	exited = true;
	if (control.exitEntry(0,0)) control.removeListener(this);
	activeCellControl = null;
	removeControl(control);
	table.repaintCell(cell.y,cell.x);
	control.restore(popupOnly ? Control.Invisible : 0,oldControlModifiers);
}

//===================================================================
public void show(int how)
//===================================================================
{
	oldControlModifiers = control.modify(popupOnly ? Control.Invisible : 0,0);
	control.fromField();
	table.repaintCell(cell.y,cell.x);
	Gui.takeFocus(control,how);
	oldModifiers = table.modify(table.NoFocus,0);
	exited = false;
	if (popupOnly && activeCellControl == this){
		isInvisible = true;
		table.repaintCell(cell.y,cell.x);
		isInvisible = false;
	}
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof ControlEvent){
 		if (ev.type == ControlEvent.FOCUS_OUT){
			if (exitOnLostFocus) exit();
		}else if (ev.type == ControlEvent.EXITED){
			exit();
			control.removeListener(this);
			ControlEvent ce = (ControlEvent)ev;
			if (ce.exitKey == IKeys.TAB) moveControlTo(cell.y,cell.x+((ce.exitKeyModifiers & IKeys.SHIFT) == 0 ? 1 : -1));
			else if (ce.exitKey == IKeys.RIGHT) moveControlTo(cell.y,cell.x+1);
			else if (ce.exitKey == IKeys.LEFT) moveControlTo(cell.y,cell.x-1);
			else if (ce.exitKey == IKeys.UP) moveControlTo(cell.y-1,cell.x);
			else if (ce.exitKey == IKeys.DOWN) moveControlTo(cell.y+1,cell.x);
		}else if (ev.type == ControlEvent.POPUP_CLOSED){
			if (exitOnPopupClosed || popupOnly) exit();
		}
	}else if (ev instanceof DataChangeEvent){
		notifyDataChange(cell);
		if (exitOnDataChange) exit();
	}
}
//##################################################################
}
//##################################################################

protected static ControlProxy controlProxy = new ControlProxy();

//##################################################################
public static class ControlProxy{
//##################################################################

public Control control;

public ControlProxy set(Control c){control = c; return this;}

//##################################################################
}
//##################################################################

//-------------------------------------------------------------------
final void moveControlTo(int row,int col)
//-------------------------------------------------------------------
{
	if (col >= numCols) col = numCols-1;
	if (col < 0) col = 0;
	if (row >= numRows) row = numRows-1;
	if (row < 0) row = 0;
	checkControlFor(null,new Point(col,row),table.ByKeyboard);
}
/**
 * This is called by a CellControl when it gets a DataChanged event from its Control.
 * @param cell the cell containing the Control. By default this will call notifyDataChange()
 * in the containing TableControl with the <b>cause</b> field being set to the Point indicating
 * the cell that the data was changed in.
 */
//-------------------------------------------------------------------
protected void notifyDataChange(Point cell)
//-------------------------------------------------------------------
{
	DataChangeEvent dce = new DataChangeEvent(DataChangeEvent.DATA_CHANGED,table);
	dce.cause = new Point(cell.x,cell.y);
	table.notifyDataChange(dce);
}
//##################################################################
}
//##################################################################

/*
18, 0
true, false
PTC: (0,0,240,278)
FCA: (0,0,2,1)
PTC: (0,-1,240,18)
FCA: (0,-1,2,1)
Did up!
18, 0
true, false
PTC: (0,0,240,278)
FCA: (0,0,2,1)
PTC: (0,-1,240,18)
FCA: (0,-1,2,1)
Did up!
*/
