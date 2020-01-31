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

/**
* This is a table model that displays an icon with associated text below it. The display
* is actually treated as a list that scrolls either vertically or horizontally. All you
* need to do is set the minHeight/minWidth values and then override getIconCount(), getIconText()
* and getIconImage().
**/
//##################################################################
public class IconTableModel extends TableModel{
//##################################################################

/**
* The minimum width for each cell.
**/
public int minWidth = 75;
/**
* The minimum height for each cell.
**/
public int minHeight = 75;
/**
* If this is true then the table will scroll vertically only. If it is false it will
* scroll horizontally only. Use setVMode() to change.
**/
public boolean vmode = true;
/**
* This should return the number of icons.
**/
public  int getIconCount()
{
	return 20;
}
/**
* This is false by default and tells the table model to not automatically select the scrolling mode
* (vertical or horizontal) appropriate for the size of the table.
**/
public boolean dontAutoSelectScrollMode = false;

/**
 * Get the text for the specified icon.
 * @param iconIndex The index of the icon.
 * @return the text for the specified icon.
 */
public  String getIconText(int iconIndex)
{
	return "Icon Display: "+(iconIndex+1);
}
/**
 * Get the image for the specified icon.
 * @param iconIndex The index of the icon.
 * @return the image for the specified icon.
 */
public  IImage getIconImage(int iconIndex)
{
	return ImageCache.cache.get("ewe/ewebig.bmp",ewe.fx.Color.White);
}
/**
* Update the table display after any changes.
**/
//===================================================================
public void updateDisplay()
//===================================================================
{
	if (table != null){
		Dimension d = table.getSize(null);
		resized(d.width,d.height);
		table.update(true);
	}
}

//===================================================================
public IconTableModel()
//===================================================================
{
	numRows = 0;
	numCols = 0;
	hasRowHeaders = false;
	hasColumnHeaders = false;
	hasPreferredSize = false;
	clipData = true;
	vmode = true;
	canVScroll = true;
	canHScroll = false;
	allColumnsSameSize = true;
}

//===================================================================
public int calculateColWidth(int col)
//===================================================================
{
	if (col == -1) return 0;
	if (vmode){
		int num = numCols;
		int w = table.getSize(null).width;
		if (num < 0) return 1;
		else return w/num;
	}else{
		return minWidth;
	}
}

//===================================================================
public int calculateRowHeight(int row)
//===================================================================
{
	if (row == -1) return 0;
	if (!vmode){
		int num = numRows;
		int h = table.getSize(null).height;
		if (num < 0) return 1;
		else return h/num;
	}else{
		return minHeight;
	}
}

/**
* Calculate the rows and columns based on the size of the table.
**/
//-------------------------------------------------------------------
protected void calculateRC()
//-------------------------------------------------------------------
{
	Dimension d = table.getSize(null);
	if (vmode){
		numCols = d.width/minWidth;
		if (numCols == 0) numCols = 1;
		numRows = (getIconCount()+numCols-1)/numCols;
	}else{
		numRows = d.height/minHeight;
		if (numRows == 0) numRows = 1;
		numCols = (getIconCount()+numRows-1)/numRows;
	}
}
//===================================================================
public void resized(int width,int height)
//===================================================================
{
	if (!dontAutoSelectScrollMode){
		if (height/minHeight < 2) {
			vmode = false;
			canHScroll = true;
			canVScroll = false;
		}else{
			vmode = true;
			canVScroll = true;
			canHScroll = false;
		}
	}
	calculateRC();
}
//===================================================================
public void setVMode(boolean vmode)
//===================================================================
{
	this.vmode = vmode;
	canVScroll = vmode;
	canHScroll = !vmode;
	updateDisplay();
}
//===================================================================
public Object getToolTip(int x,int y)
//===================================================================
{
	Point cell = table.cellAtPoint(x,y,null);
	if (cell == null) return null; //Not on a cell.
	int idx = toIconIndex(cell.y,cell.x);
	if (idx == -1) return null;
	return getIconToolTip(idx);
}
/**
* Get the tooltip for the icon at the specified index. By default it returns the
* text of the icon.
**/
//===================================================================
public Object getIconToolTip(int index)
//===================================================================
{
	return getIconText(index);
}
	//##################################################################
	class Icon implements IImage{
	//##################################################################

	public IImage icon;
	public String text;
	FontMetrics font;
	String [] lines;
	Dimension size = new Dimension();
	Point imagePoint = new Point();
	Rect textRect = new Rect();
	Rect drawRect = new Rect();
	public Icon set(IImage ic,String txt,int width)
	{
		text = txt;
		icon = ic;
		if (text != null){
			lines = DisplayLine.toLines(DisplayLine.splitLines(text,font,width,0)[0]);
			Graphics.getSize(font,lines,0,lines.length,size);
			textRect.set(0,0,size.width,size.height);
		}else{
			size.width = size.height = 0;
			lines = null;
		}
		size.height += icon.getHeight()+2;
		textRect.y += icon.getHeight()+2;
		imagePoint.x = imagePoint.y = 0;
		int dw = icon.getWidth()-size.width;
		if (dw > 0) {
			size.width += dw;
			textRect.x += dw/2;
		}else{
			imagePoint.x += (-dw)/2;
		}
		return this;
	}
	public int getWidth() {return size.width;}
	public int getHeight() {return size.height;}

	public Color getBackground() {return ewe.fx.Color.White;}
	public void free(){}
	public void draw(Graphics g,int x,int y,int options)
	{
		g.setColor(Color.Black);
		icon.draw(g,x+imagePoint.x,y+imagePoint.y,options);
		if (lines != null){
			drawRect.set(textRect);
			drawRect.x += x;
			drawRect.y += y;
			g.drawText(font,lines,drawRect,g.CENTER,g.CENTER);
		}
	}
public int [] getPixels(int[] dest,int offset,int x,int y,int width,int height,int options)
{return null;}
/**
 * Returns whether the image uses the Alpha channel.
 */
public boolean usesAlpha() {return false;}
	//##################################################################
	}
	//##################################################################

Icon icon = new Icon();


/**
 * Get the index of the icon given the row and column. Returns -1 if it
 * is out of range.
 */
//===================================================================
public int toIconIndex(int row,int col)
//===================================================================
{
	if (row == -1 || col == -1) return -1;
	int idx = vmode ? row*numCols+col : col*numRows+row;
	if (idx < 0 || idx >= getIconCount()) return -1;
	return idx;
}
/**
* Get the row and column of the icon at the specified index.
* @param index The index of the icon.
* @param where A Point to store the x (column) and y (row) location of its cell.
* This parameter may be null, in which case the return value will simply indicate if the
* index is valid or not.
* @return true if the index is within range, false if not.
*/
//===================================================================
public boolean toCell(int index,Point where)
//===================================================================
{
	if (index < 0 || index >= getIconCount()) return false;
	if (where != null){
		where.y = index/numCols;
		where.x = index%numCols;
	}
	return true;
}
//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes tca)
//===================================================================
{
	tca = super.getCellAttributes(row,col,isSelected,tca);
	if (row == -1 || col == -1) return tca;
	tca.data = tca.text = null;
	tca.borderStyle = table.BDR_NOBORDER;
	int idx = toIconIndex(row,col);
	if (idx >= 0) {
		tca.data = icon.set(getIconImage(idx),getIconText(idx),getColWidth(col)-4);
	}
	return tca;
}
//===================================================================
public boolean canSelect(int row,int col)
//===================================================================
{
	if (row == -1 || col == -1) return false;
	int idx = row*numCols+col;
	return (idx >= 0 && idx < getIconCount());
}
//===================================================================
public void made()
//===================================================================
{
	icon.font = table.getFontMetrics();
	super.made();
}

//===================================================================
public TableControl makeTable()
//===================================================================
{
	TableControl tc = new TableControl();
	tc.addListener(this);
	tc.setClickMode(true);
	tc.clickClearsItself = false;
	tc.setTableModel(this);
	return tc;
}
/**
 * If an icon in the table returned by makeTable() is clicked, this method is called.
 * by default it does nothing, override it to do something useful.
 * @param index The index of the icon that was clicked.
 */
//===================================================================
public void iconClicked(int index)
//===================================================================
{
}
/**
 * If an icon in the table returned by makeTable() is selected with the arrow keys,
 * this method is called.
 * by default it does nothing, override it to do something useful.
 * @param index The index of the icon that was clicked.
 */
//===================================================================
public void iconSelected(int index)
//===================================================================
{
}
//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof TableEvent && ev.type == TableEvent.CELL_CLICKED){
		TableEvent te = (TableEvent)ev;
		int idx = toIconIndex(te.row,te.col);
		if (idx != -1){
			if ((ev.flags & TableEvent.FLAG_SELECTED_BY_ARROWKEY) != 0){
				iconSelected(idx);
			}else{
				iconClicked(idx);
			}
		}
	}
	super.onEvent(ev);
}
//##################################################################
}
//##################################################################

