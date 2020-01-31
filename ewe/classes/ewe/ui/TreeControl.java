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
public class TreeControl extends TableControl implements Selectable{
//##################################################################

public boolean doSelections = true;
{
	model = new TreeTableModel();
	model.table = this;
	modify(WantHoldDown,WantDrag);
//FIX - Remove This
//	Menu m = new Menu(ewe.util.mString.split("Delete One|Expand To|Insert Before|Insert After|Delete Two|Delete Three"),"Test");
//	setMenu(m);
}

//===================================================================
public TreeTableModel getTreeTableModel() {return (TreeTableModel)model;}
//===================================================================

//===================================================================
public Point getSelectedCell(Point dest)
//===================================================================
{
	int [] all = getTreeTableModel().getSelectedLines();
	if (all.length == 0) return null;
	if (dest == null) dest = new Point();
	dest.y = all[0];
	dest.x = ((TreeTableModel)model).columnOf(dest.y)+1;
	return dest;
}
//===================================================================
public void setData(Object data)
//===================================================================
{
	if (data instanceof ewe.data.TreeNode){
		TreeTableModel tm = getTreeTableModel();
		tm.setRootObject((ewe.data.TreeNode)data);
		tm.expandToLevel(tm.expansionLevel);
	}
}
//===================================================================
public boolean scrollToVisible(int index)
//===================================================================
{
	TreeTableModel ttm = getTreeTableModel();
	int co = ttm.columnOf(index);
	boolean scrolled = scrollToVisible(index,co);
	int need = ttm.getDataWidth(index)+ttm.getColWidth(co);
	int have = width;
	if (firstCol < co){
		for (int i = co-1; i >= firstCol; i--){
			have -= ttm.getColWidth(i);
			if (have < need){
				firstCol = i+1;
				scrolled =  true;
				break;
			}
		}
	}
	if (firstCol > co) firstCol = co;
	return scrolled;
}
//===================================================================
public void penPressed(Point p)
//===================================================================
{
	if (menuIsActive()) {
		menuState.closeMenu();
		return;
	}
	((TreeTableModel)model).pressed(cellAtPoint(p.x,p.y,null));
}
//===================================================================
public boolean getDataToDragAndDrop(DragContext dc)
//===================================================================
{
	return ((TreeTableModel)model).getDataToDragAndDrop(dc);
}
//===================================================================
public void startDragging(DragContext dc)
//===================================================================
{
	tryDragAndDrop(dc);
}
//===================================================================
public void penDoubleClicked(Point p)
//===================================================================
{
	((TreeTableModel)model).doubleClicked(cellAtPoint(p.x,p.y,null));
}
//===================================================================
public void penClicked(Point p){((TreeTableModel)model).clicked(cellAtPoint(p.x,p.y,null));}
//===================================================================
public void penReleased(Point p){ if (menuIsActive()) return; ((TreeTableModel)model).released(cellAtPoint(p.x,p.y,null));}
//===================================================================
public int indexOfPoint(int x,int y)
//===================================================================
{
	Point p = cellAtPoint(x,y,null);
	if (p == null) return -1;
	return p.y;
}
//===================================================================
public void paintLine(int index)
//===================================================================
{
	((TreeTableModel)model).paintLine(this,getGraphics(),index);
}
//===================================================================
public boolean doMenu(Point where)
//===================================================================
{
	TreeTableModel treeModel = getTreeTableModel();
	Point p = cellAtPoint(where.x,where.y,null);
	if (p == null) return false;
	//if (!treeModel.isSelected(p.y)) return false;
	if (p.x <= treeModel.columnOf(p.y)) return false;
	Menu m = treeModel.getMenuFor(p.y);
	if (allowClipboardOperations) m = getClipboardMenu(m);
	if (m != null){
		setMenu(m);
		menuState.outsideOfControl = false;
		return tryStartMenu(where);
	}
	return false;
}
/*
static int testInsert[] = {101,102,103};
static byte testFlags[] = {(byte)0xff,(byte)0xff,(byte)0xff};
//===================================================================
public void popupMenuEvent(Object selected)
//===================================================================
{
	MenuItem mi = (MenuItem)selected;
	TreeTableModel tt = getTreeTableModel();
	if (tt.selectedLine == -1) return;
	String what = mi.label;
	if (what.equals("Delete One")){
		tt.delete(tt.selectedLine,1);
		update(true);
	}else if (what.equals("Expand To")){
		int [] to = new int[]{0,1,2};
		boolean did = tt.expandTo(to);
		update(true);
		if (did) {
			int idx = tt.indexOf(to);
			tt.select(idx);
			System.out.println(idx);
		}
	}else if (what.equals("Insert Before")){
		int [] addr = tt.addressOf(tt.selectedLine);
		if (addr.length == 0) return;
		int childNum = addr[addr.length-1];
		int parent = tt.findParent(tt.selectedLine);
		tt.insert(parent,childNum,testInsert,testFlags);
		update(true);
	}else if (what.equals("Insert After")){
		int [] addr = tt.addressOf(tt.selectedLine);
		if (addr.length == 0) return;
		int childNum = addr[addr.length-1];
		int parent = tt.findParent(tt.selectedLine);
		tt.insert(parent,childNum+1,testInsert,testFlags);
		update(true);
	}
	super.popupMenuEvent(selected);
}
*/

//===================================================================
public void cursorTo(int row,int col,boolean selectNew)
//===================================================================
{
	if (row == -2 || col == -2){
		cursor.set(-2,-2);
		return;
	}
	if (row < 0 || row >= model.numRows || cursor.y == row) return;
	cursor.set(1,row);
	TreeTableModel tm = (TreeTableModel)model;
	if (selectNew){
		int [] sel = tm.getSelectedLines();
		boolean rp = false;
		if (sel.length == 1){
			tm.select(sel[0],false);
		}else if (sel.length > 1){
			tm.selectAll(false);
			rp = true;
		}
		tm.select(row,true);
		if (scrollToVisible(row)) rp = true;//,col))  rp = true;
		if (rp) repaintNow();
		fireSelectionEvent();
		if (clickMode) clicked(row,col);
	}
}
//===================================================================
public void cursorTo(int row, boolean selectNew)
//===================================================================
{
	cursorTo(row,getTreeTableModel().columnOf(row),selectNew);
}
//===================================================================
public void onKeyEvent(KeyEvent ev)
//===================================================================
{
	TreeTableModel ttm = (TreeTableModel)model;
	if (ev.type == ev.KEY_PRESS){
		if (ev.key == IKeys.DOWN){
			if (checkFirstKey()) return;
			if (cursor.y >= model.numRows-1){
				if (listMode > 0) cursorTo(0,cursor.x+listMode,true);
			}else
				cursorTo(cursor.y+1,true);//,cursor.x,true);
		}else if (ev.key == IKeys.UP){
			if (checkFirstKey()) return;
			if (cursor.y <= 0){
				if (listMode > 0) cursorTo(model.numRows-1,cursor.x-listMode,true);
			}else
				cursorTo(cursor.y-1,true);//cursor.x,true);
		}else if (ev.key == IKeys.RIGHT){
			if (checkFirstKey()) return;
			if (ttm.canDoExpand(cursor.y)){
				ttm.fullExpandCollapse(cursor.y);
			}
			int idx = ttm.findChild(cursor.y,0);
			if (idx == 0) ttm.fireSelection();
			else cursorTo(idx,true);
			//cursorTo(cursor.y,cursor.x+1,true);
		}else if (ev.key == IKeys.LEFT){
			if (checkFirstKey()) return;
			int p = ttm.findParent(cursor.y);
			if (p != -1){
				ttm.fullExpandCollapse(p);
				cursorTo(p,true);
			}
		}else if (ev.isActionKey()){
			if (checkFirstKey()) return;
			ttm.fullExpandCollapse(cursor.y);
			ttm.fireSelection();
		}else
			super.onKeyEvent(ev);
	}else
		super.onKeyEvent(ev);
}

//===================================================================
public boolean noSelection()
//===================================================================
{
	((TreeTableModel)model).selectAll(false);
	return true;
}
//===================================================================
public boolean replaceSelection(Object with)
//===================================================================
{
	return false;
}
//===================================================================
public boolean deleteSelection()
//===================================================================
{
	return false;
}
//===================================================================
public Object getSelection()
//===================================================================
{
	return ((TreeTableModel)model).getDataToTransfer();
}
//===================================================================
public boolean hasSelection()
//===================================================================
{
	return ((TreeTableModel)model).countSelectedLines() > 0;
}
//-------------------------------------------------------------------
protected void dataBeingRemoved(Object data,DragContext dc)
//-------------------------------------------------------------------
{
	TreeTableModel treeModel = getTreeTableModel();
	int [] all = treeModel.getSelectedLines();
	treeModel.cutNodes.clear();
	for (int i = 0; i<all.length; i++){
		ewe.data.TreeNode tn = treeModel.getTreeNodeAt(all[i]);
		if (tn != null) treeModel.cutNodes.add(tn);
	}
	//((TreeTableModel)model).cutLines.copyCopiesFrom(((TreeTableModel)model).selectedLines);
	repaintNow();
}
//-------------------------------------------------------------------
protected void dataTransferCancelled(Object data)
//-------------------------------------------------------------------
{
	((TreeTableModel)model).cutNodes.clear();//cutLines.clear();
	repaintNow();
}
//-------------------------------------------------------------------
protected void dataAccepted(Control byWho,Object data,String action)
//-------------------------------------------------------------------
{
	((TreeTableModel)model).cutNodes.clear();//cutLines.clear();
	repaintNow();
}

//===================================================================
public void deleteAndUpdate(ewe.data.MutableTreeNode child,ewe.data.MutableTreeNode parent)
//===================================================================
{
	int idx = parent.indexOfChild(child);
	if (idx == -1) return;
	parent.removeChild(child);
	((TreeTableModel)model).deleted(parent,idx);
	((TreeTableModel)model).update();
}
//===================================================================
public void updateInsertion(ewe.data.TreeNode insertedChild)
//===================================================================
{
	ewe.data.TreeNode parent = insertedChild.getParent();
	if (parent == null) return;
	((TreeTableModel)model).inserted(parent,insertedChild,true);
	((TreeTableModel)model).update();
}

//##################################################################
public static class ControlTreeNode extends ewe.data.LiveTreeNode{
//##################################################################

public Control control;

public ControlTreeNode(){}
public ControlTreeNode(Control control,Font fm){this.control = control; control.font = fm; control.make(false); control.setRect(0,0,control.getPreferredSize(Dimension.buff).width,control.getPreferredSize(Dimension.buff).height);}

//##################################################################
}
//##################################################################

//##################################################################
}
//##################################################################

