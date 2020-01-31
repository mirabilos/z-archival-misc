/* $MirOS: contrib/hosted/ewe/classes/ewe/ui/TableControl.java,v 1.2 2008/05/02 20:52:01 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
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
* This is a fairly powerful Table implementation. It uses this TableControl
* as the actual Control to be displayed on the screen and to respond to user inputs.
* It also uses a TableModel to specify the particulars of the table cells and
* table data.
**/
//##################################################################
public class TableControl extends Container implements ScrollClient{
//##################################################################

protected TableModel model;

//public boolean doSelections = true;
/**
* Set this to false so that only one cell/cell group can be selected
* at a time.
**/
public boolean allowDragSelection = true;
/**
* Allow disconnected blocks to be selected.
**/
public boolean multiSelect = false;
/**
* If this is true then pen presses act as if CONTROL is always pressed.
**/
public boolean penSelectMode = (ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_KEYBOARD) != 0 ? true : false;
/**
* This is if the table is to be considered a wrapping list (such as in a FileChooser). A value greater than zero
* indicates this mode. The value itself states how many columns are considered as being a single item.
**/
public int listMode = 0;

/**
* This is the cursor position. This will generally follow the selection.
**/
public Point cursor = new Point(-2,-2);

public boolean allowClipboardOperations = false;

public boolean autoScrollToVisible = true;
public boolean clickClearsItself = true;
{
	modify(WantHoldDown|WantDrag|TakesKeyFocus,0);
	model = new TableModel();
	model.table = this;
	PenEvent.wantPenMoved(this,PenEvent.WANT_PEN_MOVED_INSIDE,true);
	backGround = Color.White;
}
private static Dimension cursorSize = new Dimension(1,1);

//===================================================================
public Dimension getCursorSize()
//===================================================================
{
	if (model == null || model.cursorSize == null)
		return cursorSize;
	return model.cursorSize;
}

//===================================================================
public TableModel getTableModel() {return model;}
//===================================================================
public void setTableModel(TableModel m) {model = m; model.table = this;}
//===================================================================
/**
* This is called after the TableModel calls its own getTableCellAttributes(). This
* gives the opportunity to modify the attributes or return a completely new attribute.
**/
//===================================================================
public TableCellAttributes overrideAttributes(TableCellAttributes ta)
//===================================================================
{
	return ta;
}
/**
* The selection rectangle.
**/
//-------------------------------------------------------------------
protected Rect selection = new Rect(), oldSelection = new Rect();
protected Vector extendedSelection = new Vector();
//-------------------------------------------------------------------
protected boolean inRect(int row,int col,Rect selection)
//-------------------------------------------------------------------
{
	if ((row >= selection.y && row < selection.y+selection.height) || row == -1)
		if ((col >= selection.x && col < selection.x+selection.width) || col == -1)
			return true;
	return false;
}
/**
* Returns if the cell in row and col are selected.
**/
//===================================================================
public boolean isSelected(int row,int col)
//===================================================================
{
	if ((row == -1) && (selection.height != model.numRows)) return false;
	if ((col == -1) && (selection.width != model.numCols)) return false;
	if (inRect(row,col,selection)) return true;
	for (int i = 0; i<extendedSelection.size(); i++)
		if (inRect(row,col,(Rect)extendedSelection.get(i)))
			return true;
	return false;
}
/**
* This return the selected rectangle of cells. If either width or height
* is zero - no cells are selected.
**/
//===================================================================
public Rect getSelection(Rect dest)
//===================================================================
{
	if (dest == null) dest = new Rect();
	return dest.set(selection);
}
/**
* This returns a single selected cell. It will work for TreeControls as
* well as TableControls. It will return null if no cell is selected.
**/
//===================================================================
public Point getSelectedCell(Point dest)
//===================================================================
{
	Rect r = getSelection(null);
	if (r.width == 0 || r.height == 0) return null;
	if (dest == null) dest = new Point();
	dest.set(r.x,r.y);
	return dest;
}
/**
* This returns all selected cells as a Vector of Rect objects.
**/
//===================================================================
public Vector getSelectedCells(Vector dest)
//===================================================================
{
	if (dest == null) dest = new Vector();
	dest.clear();
	for (int i = 0; i<extendedSelection.size(); i++) dest.add(extendedSelection.get(i));
	if (selection.width != 0 && selection.height != 0) dest.add(new Rect().set(selection));
	return dest;
}

//===================================================================
public boolean isIn(int col,int row,Rect where)
//===================================================================
{
	if (row == -1)
		return where.isIn(col,0) && where.isIn(col,model.numRows-1);
	if (col == -1)
		return where.isIn(0,row) && where.isIn(model.numCols-1,row);
	return where.isIn(col,row);
}
//===================================================================
public Rect findSelectedBlock(int row,int col,Rect dest)
//===================================================================
{
	dest = Rect.unNull(dest);
	if (isIn(col,row,selection)) return dest.set(selection);
	for (int i = 0; i<extendedSelection.size(); i++){
		Rect r = (Rect)extendedSelection.get(i);
		if (isIn(col,row,r)) return dest.set(r);
	}
	return dest;
}
/**
* Reports whether at least one cell is selected.
**/
//===================================================================
public boolean isASelection()
//===================================================================
{
	if (selection.width > 0 && selection.height > 0) return true;
	return extendedSelection.size() != 0;
}
//===================================================================
public void clearSelectedBlock(Rect block)
//===================================================================
{
	if (block == null) return;
	if (selection.equals(block)) selection.set(0,0,0,0);
	for (int i = 0; i<extendedSelection.size(); i++)
		if (extendedSelection.get(i).equals(block))
			extendedSelection.del(i--);
}
//===================================================================
public void clearSelection(int row,int col,Rect getOldSelection)
//===================================================================
{

	if (getOldSelection != null) getOldSelection.set(0,0,0,0);
	getOldSelection = findSelectedBlock(row,col,getOldSelection);
	if (getOldSelection == null) return;
	clearSelectedBlock(getOldSelection);
}
/**
* This clears the selection. It will NOT repaint the table. This only returns the most recently selected
* rectangle in a set of selected rectangles.
**/
//===================================================================
public void clearSelection(Rect getOldSelection)
//===================================================================
{
	if (getOldSelection != null)
		getOldSelection.set(selection);
	selection.set(0,0,0,0);
}
/**
* This clears the selection. It will NOT repaint the table. This only returns the most recently selected
* rectangle in a set of selected rectangles.
**/
//===================================================================
public void clearSelectedCells(Vector getOldSelection)
//===================================================================
{
	if (getOldSelection != null)
		getSelectedCells(getOldSelection);
	selection.set(0,0,0,0);
	extendedSelection.clear();
}
/*
//===================================================================
public void clearExtendedSelectedCells(Vector getOldSelection)
//===================================================================
{

}
*/
/**
* This repaints the most recently selected cell rectangle.
**/
//===================================================================
public void paintSelection() {paintCells(null,selection);}
//===================================================================
public void paintSelectedCells()
//===================================================================
{
	paintCells(null,getSelectedCells(null));
}
/**
* This adds to the selection. It will NOT repaint the table.
**/
//===================================================================
public void addToSelection(int row,int col)
//===================================================================
{/*
	Rect r = new Rect(col,row,1,1);
	if (row == -1){
		r.y = 0; r.height = model.numRows;
	}
	if (col == -1){
		r.x = 0; r.width = model.numCols;
	}
	if (selection.width == 0 || selection.height == 0) selection.set(r);
	else selection.unionWith(r);
	*/
	addToSelection(new Rect(col,row,1,1),false);
}
/**
* This adds a section to the selection, extending the current selection
* to include all cells in between.
**/
//===================================================================
public void addToSelection(Rect r,boolean repaint)
//===================================================================
{
	addToSelection(r,true,repaint);
}
//===================================================================
public boolean startNewSelectionBlock()
//===================================================================
{
	if (selection.width != 0 && selection.height != 0){
		extendedSelection.add(new Rect().set(selection));
		selection.set(0,0,0,0);
		return true;
	}
	return false;
}
/**
* This adds a section to the selection and will either extend the selection
* or add it as an individual selection.
**/
//===================================================================
public void addToSelection(Rect r,boolean extend,boolean repaint)
//===================================================================
{
	if (extend){
		if (selection.width == 0 || selection.height == 0) {
			if (r.height > model.numRows) r.height = model.numRows;
			if (r.width > model.numCols) r.width = model.numCols;
			selection.set(r);
		}else if (r.width > model.numCols) {
			r.width = model.numCols;
			if (r.height > model.numRows) r.height = model.numRows;
			int dy = 0;
			if (selection.y < r.y) dy = 1;
			else if (selection.y > r.y) dy = -1;
			for (int i = selection.y+dy;; i += dy){
				startNewSelectionBlock();
				selection.set(new Rect(0,i,r.width,1));
				if (i == r.y) break;
			}
		}
		else if (r.height > model.numRows) {
			r.height = model.numRows;
			int dx = 0;
			if (selection.x < r.x) dx = 1;
			else if (selection.x > r.x) dx = -1;
			for (int i = selection.x+dx;; i += dx){
				startNewSelectionBlock();
				selection.set(new Rect(i,0,1,r.height));
				if (i == r.x) break;
			}
		}else{
			if (listMode < 1){
				selection.unionWith(r);
			}else{
				int rows = model.numRows;
				int dx = 0;
				if (selection.x < r.x) dx = 1;
				else if (selection.x > r.x) dx = -1;
				Rect r2 = new Rect();
				while(selection.x != r.x){
					int x = selection.x;
					selection.unionWith(
					dx == -1 ? r2.set(x,0,1,selection.y):r2.set(x,selection.y,1,rows-selection.y));
					startNewSelectionBlock();
					selection.set(x+dx,dx == -1 ? rows-1 : 0,0,0);
				}
				if (dx != 0){
					selection.unionWith(dx == -1 ? r2.set(selection.x,r.y,1,rows-r.y) : r2.set(selection.x,0,1,r.y+1));
				}else{
					if (r.y > selection.y)
						selection.unionWith(r2.set(selection.x,selection.y,1,r.y-selection.y+1));
					else
						selection.unionWith(r2.set(selection.x,r.y,1,selection.y-r.y));
				}
			}
		}
	}else{
		if (r.height > model.numRows) r.height = model.numRows;
		if (r.width > model.numCols) r.width = model.numCols;
		startNewSelectionBlock();
		selection.set(r);
	}
	if (repaint) repaintNow();
}
/**
* Find out which cell is at the point (x,y). If no cell is at that point it will
* return null. Calls the cellAtPoint() method with dataOnly false.
**/
//===================================================================
public Point cellAtPoint(int x,int y,Point dest) {return cellAtPoint(x,y,dest,false);}
//===================================================================
public Point firstCellToPaint(int x,int y,Point dest)
//===================================================================
{
	dest = cellAtPoint(x,y,dest,true);
	if (dest == null) return dest;
	if (model.hasSpanningColumns) dest.x = firstCol;
	return dest;
}
/**
* Find out which cell is at the point (x,y). If no cell is at that point it will
* return null. If dataOnly is true, then the headers will not be included.
**/
//===================================================================
public Point cellAtPoint(int x,int y,Point dest,boolean dataOnly)
//===================================================================
{
	int hh = model.getRowHeight(-1);
	int hw = model.getColWidth(-1);
	if (dest == null) dest = new Point();
	dest.y = dest.x = -2;

	if (x < hw){
 		if (!dataOnly) dest.x = -1;
		else if (firstCol <= 0) return null;
		else dest.x = firstCol-1;
	}else {
		int xp = hw;
		for (int c = firstCol; c<model.numCols; c++){
			xp += model.getColWidth(c);
			if (xp > x) {
				dest.x = c;
				break;
			}
		}
		if (dest.x == -2) return null;
	}
	if (y < hh){
 		if (!dataOnly) dest.y = -1;
		else if (firstRow <= 0) return null;
		else dest.y = firstRow-1;
	}else {
		int yp = hh;
		for (int r = firstRow; r<model.numRows; r++){
			yp += model.getRowHeight(r);
			if (yp > y) {
				dest.y = r;
				break;
			}
		}
		if (dest.y == -2) return null;
	}
	return dest;
}
int firstCol = 0, firstRow = 0;

private ImageBuffer cellBuffer = new ImageBuffer();
private Rect cellRect = new Rect();
/**
* Repaint a Cell.
**/
//===================================================================
public void repaintCell(int row,int col)
//===================================================================
{
	if (row < firstRow || col < firstCol) return;
	Rect r = cellRect;
	if (!model.getCellRect(row,col,r)) return;
	Graphics gt = getGraphics();
	if (gt == null) return;
	Graphics g = cellBuffer.get(r.width,r.height,false);
	g.translate(-r.x,-r.y);
	model.paintTableCell(this,g,row,col);
	g.translate(r.x,r.y);
	getScreenRect(row,col,r);
	gt.reduceClip(r);
	gt.drawImage(cellBuffer.image,r.x,r.y);
	gt.free();
	//g.free();
}

boolean menuWasActive = false;
Point newSelection = null;
public static Vector oldExtendedSelection = new Vector();
boolean dontDrag = false;
//===================================================================
public void penPressed(Point p)
//===================================================================
{
	dontDrag = false;
	if (stretchCol != null || stretchRow != null) return;
	int mod = currentPenEvent.modifiers;
	if (penSelectMode && ((mod & IKeys.SHIFT) == 0)) mod |= IKeys.CONTROL;
	if (!multiSelect) mod &= ~(IKeys.CONTROL|IKeys.SHIFT);
	//if (menuState == null) return;
	menuWasActive = false;
	if (menuIsActive()) {
		menuState.closeMenu();
		menuWasActive = true;
	}
	newSelection = null;
	Point p2 = cellAtPoint(p.x,p.y,null);
	//repaintCell(p2.y,p2.x);if (dontDrag = true) return;

	if (model.penPressed(p,p2)) {
		dontDrag = true;
		return;
	}
	if (p2 != null) {
		if (!canSelect(p2.y,p2.x)) return;
		if (!isSelected(p2.y,p2.x)){
			selectAndUpdate(p2.y,p2.x,((mod & (IKeys.SHIFT)) == 0) && (!multiSelect || ((mod & (IKeys.CONTROL)) == 0)),(mod & IKeys.CONTROL) != 0);
		}else{
		}
	}
	//repaintNow();
}
//===================================================================
public void selectAndUpdate(int row,int col,boolean clearOld,boolean startNewBlock)
//===================================================================
{
		boolean wasSel = clearOld && isASelection();
		if (wasSel) clearSelectedCells(oldExtendedSelection);
		if (startNewBlock) startNewSelectionBlock();
		model.select(row,col,true);
		//addToSelection(p2.y,p2.x);
		newSelection = new Point(col,row);
		if (wasSel) paintCells(null,oldExtendedSelection);// || p2.x == -1 || p2.y == -1) repaintNow();
		paintCells(null,extendedSelection);
		paintSelection();//repaintCell(p2.y,p2.x);
		fireSelectionEvent();
}
//===================================================================
public void selectAndUpdate(int row,int col)
//===================================================================
{
	selectAndUpdate(row,col,true,false);
}
//===================================================================
public void penReleased(Point p,boolean isDouble)
//===================================================================
{
	if (stretchCol != null) return;
	if (menuWasActive || menuIsActive() || dontDrag) return;
	Point p2 = cellAtPoint(p.x,p.y,null);
	if (p2 != null) cursorTo(p2.y,p2.x,false);
	boolean rp = false;
	boolean multiple = false;
	if (newSelection != null || (clickMode && !multipleSelected()) || isDouble) {
//Pressed on a previously unselected cell.
		if (clickMode) {
			if (p2 == null) clearSelection(oldSelection);
			else if ((!p2.equals(newSelection) && (newSelection != null)) || clickClearsItself) clearSelection(oldSelection);
			if (p2 != null)
				if (p2.equals(newSelection)){
					if (autoScroll(p2.y,p2.x)) repaintNow();
					else paintCells(null,oldSelection);
					clicked(p2.y,p2.x);
					return;
				}else if (newSelection == null && isDouble){
					doubleClicked(p2.y,p2.x);
					return;
				}
			if (rp) repaintNow();
			else paintCells(null,oldSelection);
			return;
		}
		if (p2 != null)
			if (autoScroll(p2.y,p2.x)) repaintNow();
		return;
	}
//Not a new selection. i.e. pressed on a cell that was already selected.
	if (p2 == null) return; //Invalid cell.
//Not a new selection. i.e. pressed on a cell that was already selected.
	if (isSelected(p2.y,p2.x)){
		clearSelection(p2.y,p2.x,oldSelection);
		if (autoScroll(p2.y,p2.x)) rp = true;
		if (rp) repaintNow();
		else paintCells(null,oldSelection);
		fireSelectionEvent();
		return;
		//repaintNow();
	}
	if (autoScroll(p2.y,p2.x)) rp = true;
	if (rp) repaintNow();
}
//===================================================================
public boolean doMenu(Point where)
//===================================================================
{
	Point p = cellAtPoint(where.x,where.y,null);
	Menu m = (p == null) ? model.getMenuOutsideCells(where) : model.getMenuFor(p.y,p.x);
	//if (!isSelected(p.y,p.x)) return;
	if (allowClipboardOperations) m = getClipboardMenu(m);
	if (m != null){
		menuWasActive = true;
		if (clickMode && !clickClearsItself && (newSelection != null)) penReleased(where);
		menuWasActive = false;
		setMenu(m);
		menuState.outsideOfControl = false;
		return tryStartMenu(where);
	}
	return false;
}
/**
* Indicates whether click mode is selected or not.
**/
protected boolean clickMode = false;
/**
* Returns whether a row and column can be selected.
**/
//===================================================================
public boolean canSelect(int row,int col)
//===================================================================
{
	if (clickMode)
		if (row == -1 || col == -1) return false;
	if (col >= model.numCols || col < -1 || row >= model.numRows || row < -1) return false;
	return model.canSelect(row,col);
}
/**
* This sets ClickMode on or off. ClickMode is where pen presses can only
* "click" a cell. The cell is selected when the pen is held on it and then
* released when the pen is released - firing a TableEvent. This is useful
* when using the table as a keypad of some sort.
* Only single cells can be clicked.
**/
//===================================================================
public boolean setClickMode(boolean mode)
//===================================================================
{

	boolean ret = clickMode;
	clickMode = mode;
	//if (mode) modify(0,WantDrag);
	//else modify(WantDrag,0);
	return ret;
}
/**
* This gets called when a cell is clicked (Pressed and Released quickly).
**/
//===================================================================
public void clicked(int row,int col)
//===================================================================
{
	fireClickedEvent(row,col,false);
}
//===================================================================
public void doubleClicked(int row,int col)
//===================================================================
{
	fireClickedEvent(row,col,true);
}

/**
* You can check this in the clicked(int row, int col) method to how the click was generated.
* If TableEvent.FLAG_SELECTED_BY_ARROWKEY is true, then the user navigated to that cell
* via the keyboard and not be a pen/mouse press.
**/
protected int clickedFlags;

/**
* This fires the click event.
**/
//===================================================================
public void fireClickedEvent(int row,int col,boolean isDouble)
//===================================================================
{
	TableEvent te = new TableEvent(isDouble ? TableEvent.CELL_DOUBLE_CLICKED : TableEvent.CELL_CLICKED,this);
	te.flags |= clickedFlags;
	te.row = row; te.col = col;
	te.cellData = model.getCellData(row,col);
	postEvent(te);
}
/**
* This fires the selection event.
**/
//===================================================================
public void fireSelectionEvent(int flags)
//===================================================================
{
	TableEvent te = new TableEvent(TableEvent.SELECTION_CHANGED,this);
	te.flags |= flags;//BUG in 1.29 modifiers;
	postEvent(te);
}
/**
* This fires the selection event.
**/
//===================================================================
public void fireSelectionEvent()
//===================================================================
{
	fireSelectionEvent(0);
}
/*
//===================================================================
public void oldpenReleased(Point p)
//===================================================================
{
	if (menuWasActive) return;
	Point p2 = cellAtPoint(p.x,p.y,null);
	boolean rp = false;
	if (newSelection != null) {
		if (clickMode) {
			clearSelection();
			if (p2 != null)
				if (p2.equals(newSelection)){
					rp = autoScroll(p2.y,p2.x);
					clicked(p2.y,p2.x);
				}
			if (rp) repaintNow();
			else repaintCell(newSelection.y,newSelection.x);
			return;
		}
		if (p2 != null)
			if (autoScroll(p2.y,p2.x)) repaintNow();
		return;
	}
	if (p2 == null) return;
	if (isSelected(p2.y,p2.x)){
		clearSelection();
		fireSelectionEvent();
		rp = true;
		//repaintNow();
	}
	if (autoScroll(p2.y,p2.x)) rp = true;
	if (rp) repaintNow();
}
*/

//===================================================================
public void penReleased(Point p) {penReleased(p,false);}
public void penDoubleClicked(Point p) {penReleased(p,true);}
//===================================================================

/**
* In listMode this returns true if more than one cell is selected. Otherwise
* it returns true if more than one block is selected.
**/
//===================================================================
public boolean multipleSelected()
//===================================================================
{
	if (extendedSelection.size() > 0) return true;
	if (listMode > 0) return selection.height > 1;
	return false;
}
//-------------------------------------------------------------------
void getIndexes(Rect r,IntArray dest)
//-------------------------------------------------------------------
{
	int rows = model.numRows;
	for (int y = r.y; y < r.y+r.height; y++)
		for (int x = r.x; x < r.x+r.width; x += listMode)
			dest.add(x*rows+y);
}
/**
* This is only relevant with listMode being true. It returns a list of
* all selected indexes, in no particular order.
**/
//===================================================================
public int [] getSelectedIndexes()
//===================================================================
{
	IntArray ia = new IntArray();
	for (int i = 0; i<extendedSelection.size(); i++){
		Rect r = (Rect)extendedSelection.get(i);
		if (r.width <= 0 || r.height <= 0) continue;
		getIndexes(r,ia);
	}
	getIndexes(selection,ia);
	return ia.toIntArray();
}
/**
* This is only relevant with listMode being true. It selects all
* items in the list or deselects all items in the list. It does not
* do a repaint.
**/
//===================================================================
public void selectAllIndexes(boolean selectOn)
//===================================================================
{
	clearSelectedCells(null);
	if (!selectOn) return;
	int nr = model.numRows;
	if (nr <= 0) return;
	for (int c = 0; c<model.numCols; c++){
		int y = nr-1;
		if (c == model.numCols-1)
			for (;y>0;y--)
				if (model.canSelect(y,c))
					break;
		if (y < 0) break;
		extendedSelection.add(new Rect(c,0,1,y+1));
	}
}
/**
* This is only relevant with listMode being true. It inverts the
* selection. It does not do a repaint.
**/
//===================================================================
public void invertSelectedIndexes()
//===================================================================
{
	Vector v = new Vector();
	for (int c = 0; c<model.numCols; c++){
		Rect r = null;
		for (int y = 0; y<model.numRows; y++){
			if (!isSelected(y,c) && model.canSelect(y,c)){
				if (r != null) r.height++;
				else r = new Rect(c,y,1,1);
			}else{
				if (r != null) v.add(r);
				r = null;
			}
		}
		if (r != null) v.add(r);
	}
	clearSelectedCells(null);
	extendedSelection.addAll(v);
}
/**
* Get the on screen rectangle for the cell.
**/
//===================================================================
public Rect getScreenRect(int row,int col,Rect dest)
//===================================================================
{
	Rect rc = new Rect();
	if (dest == null) dest = new Rect();
	int hh = model.getRowHeight(-1);
	int hw = model.getColWidth(-1);
	model.getCellRect(row,col,dest);
	dest.x += hw;
	dest.y += hh;
	/*
	model.getCellRect(firstRow,firstCol,rc);
	model.getCellRect(row,col,dest);
	if (row == -1) dest.y = 0;
	else dest.y = dest.y-rc.y+hh;
	if (col == -1) dest.x = 0;
	else dest.x = dest.x-rc.x+hw;
	*/
	return dest;
}
//===================================================================
public void startDropMenu(Point p)
//===================================================================
{
	if (getMenu() != null && !menuIsActive()){
		Point p2 = cellAtPoint(p.x,p.y,null);
		if (p2 == null) return;
		menuState.doShowMenu(p,true,getScreenRect(p2.y,p2.x,null));
	}
}
Point firstDrag = null;
Point lastDrag = null;
Image stretchImage;
//===================================================================
public void startDragging(DragContext dc)
//===================================================================
{
  if (stretchCol != null | stretchRow != null){
		Image got = stretchImage = new Image(stretchCol != null ? 3 : width,stretchRow != null ? 3 : height);
		Graphics g = new Graphics(got);
		g.setColor(Color.Black);
		g.fillRect(0,0,got.getWidth(),got.getHeight());
		g.free();

		DragContext.ImageDragInWindow id = dc.startImageDrag(got,new Point(got.getWidth()/2,got.getHeight()/2),this);
		Rect r = Gui.getRectInWindow(this,null,false);
		id.dragLimits = r;
		dc.imageDrag();
		return;
	}
	if (clickMode  || dontDrag) return;
	if (!allowDragSelection) return;
	if (menuIsActive()) return;
	firstDrag = cellAtPoint(dc.start.x,dc.start.y,null);
	if (firstDrag == null) return;
	lastDrag = null;
}
//===================================================================
public void stopDragging(DragContext dc)
//===================================================================
{
	if (dontDrag) return;
	boolean str = stretchImage != null;
	if (stretchImage != null){
		dc.stopImageDrag();
		stretchImage.free();
		stretchImage = null;
		if (stretchCol != null){
			int change = dc.curPoint.x-dc.start.x;
			model.setColAdjust(stretchCol.x,change);
		}else{
			int change = dc.curPoint.y-dc.start.y;
			model.setRowAdjust(stretchRow.y,change);
		}
		update(true);
	}
	if (clickMode && !str) penReleased(dc.curPoint);
}
//===================================================================
public void dragged(DragContext dc)
//===================================================================
{
	if (stretchImage != null){
		dc.imageDrag();
		return;
	}
	if (clickMode) return;
	dc.rate = 0;
	if (!allowDragSelection) return;
	if (menuIsActive() || firstDrag == null) return;
	Point now = cellAtPoint(dc.curPoint.x,dc.curPoint.y,null,(firstDrag.x != -1 && firstDrag.y != -1));
	if (now == null) return;
	if (lastDrag == null) lastDrag = new Point(now.x,now.y);
	else if (lastDrag.equals(now)) return;
	lastDrag.set(now);
	clearSelection(oldSelection);
	model.select(firstDrag.y,firstDrag.x,true);
	model.select(lastDrag.y,lastDrag.x,true);
	//repaintSelection();
	boolean stv = scrollToVisible(lastDrag.y,lastDrag.x);
	if (stv) {
		dc.rate = 500;
		repaintNow();
	}else{
		oldSelection.unionWith(selection);
		paintCells(null,oldSelection);
	}
	fireSelectionEvent();
}
/**
* Find the area on the table which contains the data, not the headers.
* This is the scrollable area on the screen.
**/
//===================================================================
public Rect getDataArea(Rect dest)
//===================================================================
{
	Dimension s = getSize(null);
	dest = Rect.unNull(dest).set(model.getColWidth(-1),model.getRowHeight(-1),s.width,s.height);
	dest.width -= dest.x;
	dest.height -= dest.y;
	return dest;
}
/**
* Find out if the cell at row and col is completely visible (completely == true) or at
* least partially visible (completely == false).
**/
//===================================================================
public boolean isVisible(int row,int col,boolean completely)
//===================================================================
{
	Rect area = getDataArea(null);
	area.x = firstCol; area.y = firstRow;
	model.findCellsInArea(area,completely);
	if (row < area.y || row >= area.y+area.height) return false;
	if (col < area.x || col >= area.x+area.width) return false;
	return true;
}
//-------------------------------------------------------------------
boolean autoScroll(int row,int col)
//-------------------------------------------------------------------
{
	if (!autoScrollToVisible) return false;
	return scrollToVisible(row,col);
}
/**
* Scrolls the data so that the cell at row and col is completely visible.
* Returns true if a scroll was actually done. It does NOT repaint.
**/
//===================================================================
public boolean scrollToVisible(int row,int col)
//===================================================================
{
	Rect r = getDataArea(null);
	int w = r.width, h = r.height;
	int fr = row+1, fc = col+1;
	if (row <= firstRow) {
		if (row > -1) fr = row;
	}else {
		for(fr = row+1;fr > firstRow && fr > 0;fr--) {
			h -= model.getRowHeight(fr-1);
			if (h < 0) break;
		}
		if (fr > row) fr = row;
	}
	if (col <= firstCol) {
		if (col > -1) fc = col;
	}else {
		for(fc = col+1;fc > firstCol && fc > 0;fc--) {
			w -= model.getColWidth(fc-1);
			if (w < 0) break;
		}
		if (fc > col) fc = col;
	}
	return changeOrigin(fr,fc);
}
/**
* Set the top-left cell being displayed.
**/
//===================================================================
public boolean changeOrigin(int fr,int fc)
//===================================================================
{
	if (fc != firstCol && !model.canHScroll) fc = firstCol;
	if (fr != firstRow && !model.canVScroll) fr = firstRow;
	if (fc == firstCol && fr == firstRow) return false;
	boolean vscroll = firstRow != fr;
	boolean hscroll = firstCol != fc;

	if (vscroll && !hscroll){
		doScroll(Vertical,TrackTo,fr);
		return false;
	}else if (hscroll && !vscroll){
		doScroll(Horizontal,TrackTo,fc);
		return false;
	}else{
		firstRow = fr;
		firstCol = fc;
		if (ss != null) {
			ss.updateScroll(Vertical);
			ss.updateScroll(Horizontal);
		}
		updateControls();
		return true;
	}
}
/**
* This removes all controls within the table.
**/
//===================================================================
public void updateControls()
//===================================================================
{
	Iterator it = getChildren();
	Control f = Gui.focusedControl();
	if (f != this)
		for (Control c = f; c != null; c = c.getParent())
			if (c == this){
				int was = f.modify(Invisible,0);
				Gui.takeFocus(null,ByRequest);//(this,ByRequest);
				f.restore(was,Invisible);
			}
	removeAll();
}
/**
* Update the table to reflect any changes. If you call with repaint false
* then any scroll bars will be updated but no repaint will be called.
**/
//===================================================================
public void update(boolean repaint)
//===================================================================
{
	if (ss != null) {
		ss.checkScrolls();
	}
	if (repaint) repaintNow();
}
/*
public void doTest()
{
	clearSelection();
	if (!test){
		addToSelection(2,3);
		//addToSelection(5,4);
		addToSelection(0,2);
	}
	repaintNow();
	test = !test;
}
public boolean test = false;
*/

//===================================================================
public void paintBackground(Graphics g)
//===================================================================
{
	g.setColor(hasModifier(Disabled,true) ? Color.LightGray : getBackground());
	g.setDrawOp(g.DRAW_OVER);
	g.fillRect(0,0,width,height);
}

//===================================================================
public void repaintNow(Graphics g,Rect area)
//===================================================================
{
	updateControls();
	super.repaintNow(g,area);
}
//-------------------------------------------------------------------
ewe.sys.Lock paintLock = new ewe.sys.Lock();
//-------------------------------------------------------------------

//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	//updateControls();
	if (paintLock.grab())try{
		BufferedGraphics bg = new BufferedGraphics(g,getDim(null));
		g = bg.getGraphics();
		try{
			Rect cells = new Rect();
			if (area == null) {
				paintCells(g,(Rect)null);
				return;
			}
			paintBackground(g);
			model.startingPaint(g);
			Rect r = new Rect().set(area);
			int hh = model.getRowHeight(-1);
			int hw = model.getColWidth(-1);
			boolean doUp = r.y < hh, doLeft = r.x < hw;
			boolean doCorner = doUp && doLeft;
			if (doUp) {
				int diff = hh-r.y;
				r.y = hh;
				r.height -= diff;
			}
			if (doLeft) {
				int diff = hw-r.x;
				r.x = hw;
				r.width -= diff;
			}
			Rect r2 = new Rect();
			Point p = firstCellToPaint(r.x,r.y,new Point());//cellAtPoint(r.x,r.y,new Point(),true);
			int tx = 0, ty = 0;
			if (p != null){
				getScreenRect(p.y,p.x,Rect.buff);
				tx = Rect.buff.x;
				ty = Rect.buff.y;
				r.width += r.x-tx;
				r.height += r.y-ty;
				r.x = p.x; r.y = p.y;
				g.translate(tx,ty);
				model.paintTableCell(this,g,r2.set(r));
				g.translate(-tx,-ty);
			}
			else {
				//if (true) return;
				//ewe.sys.Vm.debug("Was: "+r.width);
				getScreenRect(firstRow,firstCol,Rect.buff);
				tx = Rect.buff.x;
				ty = Rect.buff.y;
				r.width += r.x-tx;
				r.height += r.y-ty;
				r.x = firstCol; r.y = firstRow;
			}
			if (doUp) {
				g.translate(tx,0);
				model.paintTableCell(this,g,r2.set(r.x,-1,r.width,hh));
				g.translate(-tx,0);
			}
			if (doLeft) {
				g.translate(0,ty);
				model.paintTableCell(this,g,r2.set(-1,r.y,hw,r.height));
				g.translate(0,-ty);
			}
			if (doCorner){
				model.paintTableCell(this,g,r2.set(-1,-1,hw,hh));
			}
			//}
		}finally{
			g = bg.release();
		}
		//paintCells(g,null);
	}finally{
		paintLock.release();
	}
}

//-------------------------------------------------------------------
boolean fixPaintLimits(Rect cells,Point translate,boolean horizontal)
//-------------------------------------------------------------------
{
	if (horizontal){
		int sub = firstCol-cells.x;
		if (sub >= 0){
			cells.x = firstCol;
			cells.width -= sub;
		}
		if (cells.width <= 0) return false;
		int cx = model.getColWidth(-1);
		int cw = 0;
		for (int i = firstCol; i<cells.x && cx<width; i++) cx += model.getColWidth(i);
		for (int i = cells.x; i<cells.x+cells.width && (cx+cw)<width; i++) cw += model.getColWidth(i);
		cells.width = cw;
		translate.x = cx;
		return (cw > 0);
	}else{
		int sub = firstRow-cells.y;
		if (sub >= 0) {
			cells.y = firstRow;
			cells.height -= sub;
		}
		if (cells.height <= 0) return false;
		int cy = model.getRowHeight(-1);
		int ch = 0;
		for (int i = firstRow; i<cells.y && cy<height; i++) cy += model.getRowHeight(i);
		for (int i = cells.y; i<cells.y+cells.height && (cy+ch)<height; i++) ch += model.getRowHeight(i);
		cells.height = ch;
		translate.y = cy;
		return (ch > 0);
	}
}
//-------------------------------------------------------------------
ImageBuffer im = new ImageBuffer();
//-------------------------------------------------------------------
//-------------------------------------------------------------------
void paintClipped(Graphics g,Rect h,Rect clip)
//-------------------------------------------------------------------
{
	h.getIntersection(Rect.buff.set(0,0,width,height),h);
	if (clip != null) h.getIntersection(clip,h);
	g.setClip(h.x,h.y,h.width,h.height);
	g.drawImage(im.image,h.x,h.y);
	if (clip != null) g.setClip(clip.x,clip.y,clip.width,clip.height);
	else g.clearClip();
}

//-------------------------------------------------------------------
Graphics getBuff(int width,int height)
//-------------------------------------------------------------------
{
	Graphics g = im.get(width,height);
	g.setColor(getBackground());
	g.fillRect(0,0,width,height);
	return g;
}

//===================================================================
public void paintCells(Graphics gr,Vector rectsToPaint)
//===================================================================
{
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	if (rectsToPaint == null) paintCells(g,(Rect)null);
	else{
		for (int i = 0; i<rectsToPaint.size(); i++)
			paintCells(g,(Rect)rectsToPaint.get(i));
	}
	if (g != gr) g.free();
}
//===================================================================
public void paintCells(Graphics gr,Rect cellsToPaint)
//===================================================================
{
	Graphics g = gr;
	if (g == null) g = getGraphics();
	if (g == null) return;
	Rect clip = g.getClip(new Rect());
	if (cellsToPaint == null) {
		paintBackground(g);
		model.startingPaint(g);
		cellsToPaint = new Rect(-1,-1,model.numCols+1,model.numRows+1);
	}
	Rect cells = new Rect().set(cellsToPaint);
	Rect toDo = new Rect();
	int hh = model.getRowHeight(-1);
	int hw = model.getColWidth(-1);

	Point translate = new Point();
	toDo.set(cells);
	if (toDo.x == -1) {
		toDo.x = 0; toDo.width--;
	}

	if (toDo.y == -1) {
		toDo.y = 0; toDo.height--;
	}
	Rect h = new Rect();
	if (fixPaintLimits(toDo,translate,true))
		if (fixPaintLimits(toDo,translate,false)){
			model.paintTableCell(this,getBuff(toDo.width,toDo.height),h.set(toDo));
			paintClipped(g,h.set(translate.x,translate.y,toDo.width,toDo.height),clip);
		}
	if (cells.y == -1 && model.hasColumnHeaders) {
		model.paintTableCell(this,getBuff(toDo.width,hh),h.set(toDo.x,-1,toDo.width,hh));
		paintClipped(g,h.set(translate.x,0,toDo.width,hh),clip);
	}
	if (cells.x == -1 && model.hasRowHeaders){
		model.paintTableCell(this,getBuff(hw,toDo.height),h.set(-1,toDo.y,hw,toDo.height));
		paintClipped(g,h.set(0,translate.y,hw,toDo.height),clip);
	}
	if (cells.y == -1 && cells.x == -1 && model.hasColumnHeaders && model.hasRowHeaders) {
		model.paintTableCell(this,getBuff(hw,hh),h.set(-1,-1,hw,hh));
		paintClipped(g,h.set(0,0,hw,hh),clip);
	}
	if (g != gr) g.free();
}
//===================================================================
public int getLastColToShow()
//===================================================================
{
	Dimension size = getSize(null);
	if (size == null) return 0;
	return getLastColToShow(size.width);
}
//===================================================================
public int getLastColToShow(int forWidth)
//===================================================================
{
	int w = forWidth-model.getColWidth(-1);
	int c = model.numCols-1;
	for(;c >= 0;c--){
		int cw = model.getColWidth(c);
		w -= cw;
		if (w < 0) break;
	}
	if (w < 0) c++;
	if (c > model.numCols-1) c = model.numCols-1;
	return c;
}
//===================================================================
public int getLastRowToShow()
//===================================================================
{
	Dimension size = getSize(null);
	if (size == null) return 0;
	return getLastRowToShow(size.height);
}
//===================================================================
public int getLastRowToShow(int forHeight)
//===================================================================
{
	int h = forHeight-model.getRowHeight(-1);
	int r = model.numRows-1;
	for(;r >= 0;r--){
		int rh = model.getRowHeight(r);
		h -= rh;
		if (h < 0) break;
	}
	if (h < 0) r++;
	if (r > model.numRows-1) r = model.numRows-1;
	return r;
}
/**
* This returns which rows and columns are on the screen.
* x = first column on screen.
* y = first row on screen.
* width = number of columns fully visible.
* height = number of rows fully visible.
**/
//===================================================================
public Rect getOnScreen(Rect dest)
//===================================================================
{
	return getOnScreen(firstCol,firstRow,dest);
}
/**
* This returns which rows and columns are on the screen, given the
* specified firstCol and firstRow.
* x = first column on screen.
* y = first row on screen.
* width = number of columns fully visible.
* height = number of rows fully visible.
**/
//===================================================================
public Rect getOnScreen(int firstCol,int firstRow,Rect dest)
//===================================================================
{
	if (dest == null) dest = new Rect();
	dest.x = firstCol; dest.y = firstRow;
	dest.width = dest.height = 0;
	Dimension s = getSize(null);
	if (s == null) return dest;
	int w = s.width-model.getColWidth(-1);
	int h = s.height-model.getRowHeight(-1);
	int nc = model.numCols;
	for (int c = firstCol; c<nc && w >= 0; c++){
		int cw = model.getColWidth(c);
		w -= cw;
		if (w < 0) break;
		dest.width++;
	}
	int nr = model.numRows;
	for (int r = firstRow; r<nr && h >= 0; r++){
		int rh = model.getRowHeight(r);
		h -= rh;
		if (h < 0) break;
		dest.height++;
	}
	return dest;
}
/**
* Find out the cell coordinates if a page up is done.
**/
//===================================================================
public Point getPageUp(Point dest)
//===================================================================
{
	if (dest == null) dest = new Point();
	dest.x = firstCol-1; dest.y = firstRow-1;

	Dimension s = getSize(null);
	if (s == null) return dest;
	int w = s.width-model.getColWidth(-1);
	int h = s.height-model.getRowHeight(-1);
	for (int c = firstCol-1; c>=0 && w >= 0; c--){
		int cw = model.getColWidth(c);
		w -= cw;
		if (w < 0) break;
		dest.x = c;
	}
	int nr = model.numRows;
	for (int r = firstRow-1; r>=0 && h >= 0; r--){
		int rh = model.getRowHeight(r);
		h -= rh;
		if (h < 0) break;
		dest.y = r;
	}
	if (dest.x < 0) dest.x = 0;
	if (dest.y < 0) dest.y = 0;
	return dest;
}
//-------------------------------------------------------------------
//ScrollServer ss;
//-------------------------------------------------------------------
//===================================================================
public int getActual(int which)
//===================================================================
{
	if (which == Vertical) return getLastRowToShow()+1;
	else return getLastColToShow()+1;
}
//===================================================================

public int getVisible(int which,int forSize)
//===================================================================
{
	return 1;
}
//===================================================================
//public void setServer(ScrollServer server) {ss = server;}
//===================================================================
public int getCurrent(int which)
//===================================================================
{
	if (which == Vertical) return firstRow;
	else return firstCol;

}
//===================================================================
public boolean needScrollBar(int which,int forSize)
//===================================================================
{
	if (which == Vertical) {
		if (!model.canVScroll) return false;
		else return getLastRowToShow(forSize) > 0;
	}
	else
		if (!model.canHScroll) return false;
		else{
			return getLastColToShow(forSize) > 0;
		}
}

//===================================================================
public boolean canGo(int orientation,int direction,int position)
//===================================================================
{
	if (orientation == Vertical)
		if (direction == Higher)
			return position < getLastRowToShow();
		else
			return position > 0;
	else{
		if (direction == Higher)
			return position < getLastColToShow();
		else{
			return position > 0;
		}
	}
}
//-------------------------------------------------------------------
boolean vscroll(int was,int now)
//-------------------------------------------------------------------
{
	if (was == now) return false;
	if (!model.canScreenScroll()) return true;
	else{
		Rect r = getDataArea(null);
		int cw = model.getColWidth(-1);
		r.width += cw;
		r.x -= cw;
		int dx = now > was ? 1 : -1, difference = 0;
		if (dx == -1) for (int i = was-1; i>=now; i+=dx) {
			difference += model.getRowHeight(i);
			if (difference >= r.height) return true;
		}else for (int i = was; i != now; i+=dx) {
			difference += model.getRowHeight(i);
			if (difference >= r.height) return true;
		}
		if (r.height > difference){
			if (now > was)
				scrollAndRepaint(r.x,r.y+difference,r.width,r.height-difference,r.x,r.y);
			else
				scrollAndRepaint(r.x,r.y,r.width,r.height-difference,r.x,r.y+difference);
			return false;
		}else
			return true;
	}
}
//-------------------------------------------------------------------
boolean hscroll(int was,int now)
//-------------------------------------------------------------------
{
	if (was == now) return false;
	if (!model.canScreenScroll()) return true;
	else{
		Rect r = getDataArea(null);
		int rh = model.getRowHeight(-1);
		r.height += rh;
		r.y -= rh;
		int dy = now > was ? 1 : -1, difference = 0;
		if (dy == -1) for (int i = was-1; i>=now; i+=dy) {
			difference += model.getColWidth(i);
			if (difference >= r.width) return true;
		}else for (int i = was; i != now; i+=dy) {
			difference += model.getColWidth(i);
			if (difference >= r.width) return true;
		}
		if (r.width > difference){
			if (now > was)
				scrollAndRepaint(r.x+difference,r.y,r.width-difference,r.height,r.x,r.y);
			else
				scrollAndRepaint(r.x,r.y,r.width-difference,r.height,r.x+difference,r.y);
			return false;
		}else
			return true;
	}
}
//==================================================================
public void doScroll(int which,int action,int value)
//==================================================================
{
	Rect onScreen = null;
	Point upScreen = null;
	boolean repaint = true;
	if (action == PageHigher){
		onScreen = getOnScreen(null);
		if (onScreen.width <= 0) onScreen.width = 1;
		if (onScreen.height <= 0) onScreen.height = 1;
	}
	else if (action == PageLower)
		upScreen = getPageUp(null);
	int pfr = firstRow, pfc = firstCol;

	if (which == Vertical) {
		int was = firstRow;
		if (action == ScrollHigher)  firstRow = model.scrollTo(firstRow,firstRow+1,action,false);
		else if (action == ScrollLower) firstRow = model.scrollTo(firstRow,firstRow-1,action,false);
		else if (action == PageHigher) firstRow = model.scrollTo(firstRow,firstRow+onScreen.height,action,false);
		else if (action == PageLower) firstRow = model.scrollTo(firstRow,upScreen.y,action,false);
		else if (action == TrackTo) firstRow =  model.scrollTo(firstRow,value,action,false);
		int lastRow = getLastRowToShow();
		if (firstRow > lastRow) firstRow = lastRow;
		if (firstRow < 0) firstRow = 0;
		repaint = vscroll(was,firstRow);
	}else {
		int was = firstCol;
		if (action == ScrollHigher) firstCol = model.scrollTo(firstCol,firstCol+1,action,true);
		else if (action == ScrollLower) firstCol = model.scrollTo(firstCol,firstCol-1,action,true);
		else if (action == PageHigher) firstCol = model.scrollTo(firstCol,firstCol+onScreen.width,action,true);
		else if (action == PageLower) firstCol = model.scrollTo(firstCol,upScreen.x,action,true);
		else if (action == TrackTo) firstCol = model.scrollTo(firstCol,value,action,true);
		int lastCol = getLastColToShow();
		if (firstCol > lastCol) firstCol = lastCol;
		if (firstCol < 0) firstCol = 0;
		repaint = hscroll(was,firstCol);
	}
	//updateControls(); //This will be done by doPaint(
	if ((firstRow != pfr || firstCol != pfc) && repaint) repaintNow();
	if (ss != null) ss.updateScroll(which);
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	super.make(reMake);
	model.made();
}
//==================================================================
public void resizeTo(int width,int height)
//==================================================================
{
	updateControls();
	super.resizeTo(width,height);
	model.resized(width,height);
}
/**
* This overrides doPaintChildren() to do nothing. Child controls of a
* Table must be handled differently. This is done correctly in GridTableModel.
**/
//==================================================================
public void doPaintChildren(Graphics g, int x, int y, int w, int h){}
//==================================================================
//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	Dimension r = model.getPreferredSize(null);
	preferredWidth = r.width; preferredHeight = r.height;
}
/**
 * Return a Tool Tip for the x,y position (in pixels) on the table. In this
	method you can do:
	<pre>
	Point cell = cellAtPoint(x,y);
	if (cell == null) return null; //Not on a cell.
	// Now cell.x will have the column and cell.y will have the row
	// of the cell the mouse is over and you can then display a tool tip for that cell.
	<pre>
 * @param x The x co-ordinate in pixels of the mouse.
 * @param y The y co-ordinate in pixels of the mouse.
 * @return An acceptable ToolTip object.
 */
//===================================================================
public Object getToolTip(int x,int y)
//===================================================================
{
	Object ret = model.getToolTip(x,y);
	if (ret == null) ret = super.getToolTip(x,y);
	return ret;
}
/*
//===================================================================
public Control getNextKeyFocus(Control sourceChild,boolean forwards)
//===================================================================
{
	if ((modifiers & TakesKeyFocus) != 0) return this;
	return null;
}
*/
//===================================================================
public void clearCursor() {cursorTo(-2,-2,false);}
//===================================================================
public void cursorTo(int row,int col,boolean selectNew)
//===================================================================
{
	if (row != -2 && col != -2 && !canSelect(row,col)) return;
	Dimension cs = getCursorSize();
	int w = cs.width; if (w < 0) w = model.numCols;
	int h = cs.height; if (h < 0) h = model.numRows;
	if (row >= 0 && col >= 0 && w != 0 && h != 0){
		col = w == 0 ? 0 : (col/w)*w;
		row = h == 0 ? 0 : (row/h)*h;
	}
	cursor.set(col,row);
	if (selectNew && w != 0 && h != 0){
		clearSelectedCells(oldExtendedSelection);
		paintCells(null,oldExtendedSelection);
		if (row != -2 && col != -2){
			if (scrollToVisible(row,col)) repaintNow();
			if (w != 0 && h != 0){
				addToSelection(Rect.buff.set(col,row,w,h),true);
			}else{
				model.select(row,col,true);
			}
			fireSelectionEvent(TableEvent.FLAG_SELECTED_BY_ARROWKEY);
			clickedFlags = TableEvent.FLAG_SELECTED_BY_ARROWKEY;
			if (clickMode) clicked(row,col);
			clickedFlags = 0;
		}
	}
}
//-------------------------------------------------------------------
protected boolean checkFirstKey()
//-------------------------------------------------------------------
{
	if (cursor.x == -2 || cursor.y == -2){
		//ewe.sys.Vm.debug("Cursor to 0");
		cursorTo(0,0,true);
		return true;
	}
	return false;
}
protected boolean doHotKey(Control parent,KeyEvent key)
{
	if (model.doHotKey(key)) return true;
	return super.doHotKey(parent,key);
}
//-------------------------------------------------------------------
protected Control getFirstFocus()
//-------------------------------------------------------------------
{
	if ((modifiers & TakesKeyFocus) != 0) return this;
	return super.getFirstFocus();
}
//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	Dimension cs = getCursorSize();
	int w = cs.width; if (w < 0) w = 0;
	int h = cs.height; if (h < 0) h = 0;
		//ewe.sys.Vm.debug("Key!");
	if (model.onKeyEvent(ev)) return;
	if (ev.type == ev.KEY_PRESS){
		if (ev.key == IKeys.DOWN){
			if (checkFirstKey()) return;
			if (cursor.y >= model.numRows-1){
				if (listMode > 0) cursorTo(0,cursor.x+listMode,true);
			}else
				cursorTo(cursor.y+h,cursor.x,true);
		}else if (ev.key == IKeys.UP){
			if (checkFirstKey()) return;
			if (cursor.y <= 0){
				if (listMode > 0) cursorTo(model.numRows-1,cursor.x-listMode,true);
			}else
				if (cursor.y-h >= 0)
					cursorTo(cursor.y-h,cursor.x,true);
		}else if (ev.key == IKeys.RIGHT){
			if (checkFirstKey()) return;
			cursorTo(cursor.y,cursor.x+w,true);
		}else if (ev.key == IKeys.LEFT){
			if (checkFirstKey()) return;
			if (cursor.x-w >= 0)
				cursorTo(cursor.y,cursor.x-w,true);
		}else if (ev.key == IKeys.MENU){
			Point p = getSelectedCell(null);
			if (p != null){
				Rect r = getScreenRect(p.y,p.x,null);
				if (checkMenuKey(ev,new Point(r.x,r.y)))
					return;
			}
			isSomeonesHotKey(ev);
		}else if (clickMode && cursor.x >= 0 && cursor.y >= 0){
			if (ev.key == IKeys.ENTER || ev.key == IKeys.ACTION || ev.key == ' ')
				clicked(cursor.y,cursor.x);
			else if (ev.key == IKeys.ESCAPE){
				cursorTo(-2,-2,true);
			}
		}else
			super.onKeyEvent(ev);
	}else
		super.onKeyEvent(ev);
}
//===================================================================
public void popupMenuEvent(MenuEvent ev)
//===================================================================
{
	if (!model.popupMenuEvent(ev)) super.popupMenuEvent(ev);
}

private Point penOver = new Point();
private Rect penOverRect = new Rect();
private Point stretchCol, stretchRow;
//-------------------------------------------------------------------
protected Point overColStretch(int x,int y)
//-------------------------------------------------------------------
{
	if (!model.canHScroll || ss == null || menuIsActive()) return null;
	Point where = cellAtPoint(x,y,penOver,false);
	if (where == null) return where;
	if (where.y != -1) return null;
	Rect r = getScreenRect(where.y,where.x,penOverRect);
	if (r == null) return null;
	if (x >= r.x+r.width-2) return where;
	if (x <= r.x+2 && where.x > -1) {
		where.x--;
		if (where.x == -1 && !model.hasRowHeaders) return null;
		return where;
	}
	return null;
}
//-------------------------------------------------------------------
protected Point overRowStretch(int x,int y)
//-------------------------------------------------------------------
{
	if (!model.canVScroll || ss == null || menuIsActive()) return null;
	Point where = cellAtPoint(x,y,penOver,false);
	if (where == null) return where;
	if (where.x != -1) return null;
	Rect r = getScreenRect(where.y,where.x,penOverRect);
	if (r == null) return null;
	if (y >= r.y+r.height-2) return where;
	if (y <= r.y+2 && where.y > -1) {
		where.y--;
		if (where.y == -1 && !model.hasColumnHeaders) return null;
		return where;
	}
	return null;
}
//===================================================================
public void onPenEvent(PenEvent pe)
//===================================================================
{
	if (pe.type == PenEvent.PEN_MOVE){
		Point where = overColStretch(pe.x,pe.y);
		if (where != null) setCursor(ewe.sys.Vm.LEFT_RIGHT_CURSOR);
		else if ((where = overRowStretch(pe.x,pe.y)) != null)
			setCursor(ewe.sys.Vm.UP_DOWN_CURSOR);
		else
			setCursor(0);
	}else if (pe.type == PenEvent.PEN_DOWN){
		stretchCol = stretchRow = null;
		if ((stretchCol = overColStretch(pe.x,pe.y)) == null)
			stretchRow = overRowStretch(pe.x,pe.y);
	}
	super.onPenEvent(pe);
}
//##################################################################
}
//##################################################################
