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
public class ListSelect extends Editor{
//##################################################################

	public MultiListSelect selectData = null;//new MultiListSelect.SingleListSelect(new Vector);//,new Vector(new Object[]{"Two"}),"list");
	public String listName = "list";
	public ListSelectTable table;
	public mChoice lists = new mChoice();
	public CellPanel listSelector = new CellPanel();
	public ScrollablePanel scrollPanel;

	public boolean showSelectionAtFocus = Device.hasFlag(Device.VM_FLAG_NO_PEN);

	public MultiListSelect.AvailableLists available = new MultiListSelect.AvailableLists();
	{
	windowFlagsToSet &= ~Window.FLAG_MAXIMIZE_ON_PDA;
	}


//===================================================================
public static int select(String title, Frame parent, Vector allChoices, int initialSelection)
//===================================================================
{
	int[] ret = select(title,parent,new MultiListSelect.SingleListSelect(allChoices,initialSelection));
	if (ret == null || ret.length < 0) return -1;
	return ret[0];
}
//===================================================================
public static int[] select(String title, Frame parent, Vector allChoices, Vector initialSelection, boolean singleSelectionOnly)
//===================================================================
{
	return select(title,parent,new MultiListSelect.SingleListSelect(allChoices,initialSelection,singleSelectionOnly));
}
//===================================================================
public static int[] select(String title, Frame parent, MultiListSelect.SingleListSelect ms)
//===================================================================
{
	ListSelect ls = new ListSelect(title,ms);
	if (ls.execute(parent,Gui.CENTER_FRAME) == IDCANCEL) return null;
	return ms.getSelectedIndexes();
}
/**
Create a ListSelect for a particular SingleListSlect as a pop-up control, that can be
used again if necessary.
**/
//===================================================================
public ListSelect(String title, MultiListSelect.SingleListSelect ms)
//===================================================================
{
	this(false,null,false);
	ListSelect ls = this;
	ls.setData(ms);
	ls.title = title;
	ls.windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
	ls.hasTopBar = true;
	ls.scrollPanel.borderStyle = BDR_NOBORDER;
	ls.scrollPanel.borderWidth = 0;
	Gui.setOKCancel(ls);
}

	//===================================================================
	MultiListSelect.AvailableLists getAvailable()
	//===================================================================
	{
		if (selectData == null) return available;
		if (selectData.availableLists == null) return available;
		return selectData.availableLists;
	}
	//===================================================================
	public ListSelect()
	//===================================================================
	{
		this(true,"List:",true);
	}
	//===================================================================
	public ListSelect(boolean multiple,String listPrompt,boolean allowRearrange)
	//===================================================================
	{
		title = "List Select";
		CellPanel cp = listSelector;
		if (listPrompt != null) cp.addNext(new mLabel(listPrompt),DONTSTRETCH,DONTFILL|WEST);
		if (multiple) cp.addLast(addField(lists,"listName"),HSTRETCH,DONTFILL|WEST);
		addLast(cp).setCell(HSTRETCH);
		ScrollBarPanel sbp;
		CellPanel cp3 = new CellPanel();
		cp3.addNext(scrollPanel = sbp = new ScrollBarPanel((ScrollClient)addField(table = new ListSelectTable(),"selection")));//.setPreferredSize(400,200);
		table.multiSelect = allowRearrange;
		table.allowDragSelection = false;
		if (allowRearrange){
			CellPanel cp2 = new CellPanel();
			cp2.addLast(getToggleChooseAllButton());
			cp2.addLast(addField(new mButton(ListEditor.topIcon),"moveToTop"));
			cp2.addLast(addField(new mButton(ListEditor.upIcon),"moveUp"));
			cp2.addLast(addField(new mButton(ListEditor.downIcon),"moveDown"));
			cp2.addLast(addField(new mButton(ListEditor.bottomIcon),"moveToBottom"));
			cp3.addNext(cp2).setCell(VSTRETCH).setControl(DONTFILL|CENTER);
		}
		cp3.endRow();
		addLast(cp3);
		//addLast(addField(new mButton("Print"),"print")).setCell(HSTRETCH);
		//resizable = true;
		sbp.borderWidth = 3;
		sbp.borderStyle = mInput.inputEdge|BF_RECT;
		table.backGround = Color.White;
		lists.modify(NotAnEditor,KeepImage);
	}
	//===================================================================
	public void setData(MultiListSelect select,String name)
	//===================================================================
	{
		selectData = select;
		listName = name;
		relayout(false);
	}
	//===================================================================
	public void setData(Object data)
	//===================================================================
	{
		selectData = (MultiListSelect)data;
		lists.items.clear();
		if (selectData != null)
			if (getAvailable() != null){
				for (int i = 0; i<getAvailable().size(); i++){
					Property p = (Property)getAvailable().get(i);
					lists.items.add(p.name);
				}
			}
		if (lists.items.size() == 0) lists.selectedIndex = -1;
		else
			listName = mString.toString(lists.items.get(lists.selectedIndex = 0));
		if (table != null) table.newData();
		//listSelector.relayout(true);
		Control c = table.getParent();
		if (c instanceof ScrollablePanel){
			ScrollablePanel sbp = (ScrollablePanel)c;
			sbp.relayout(true);
		}
		relayout(false);
	}
	//===================================================================
	int getSelectedRow()
	//===================================================================
	{
		Rect all = table.getSelection(null);
		if (all == null) return -1;
		if (all.height != 1) return -1;
		return all.y;
	}
	//===================================================================
	public mButton getToggleChooseAllButton()
	//===================================================================
	{
		mButton mb = new mButton(ImageCache.cache.get("ewe/optionssmall.bmp",Color.White));
		addField(mb.setToolTip("Choose All/None"),"allOrNone");
		return mb;
	}
	//===================================================================
	public void action(ewe.reflect.FieldTransfer ft,Editor ed)
	//===================================================================
	{
		int row = getSelectedRow();
		String name = ft.fieldName;
		ListSelectTable.ListSelectTableModel lsm = (ListSelectTable.ListSelectTableModel)table.getTableModel();
		if (name.equals("print")){
			ewe.sys.Vm.debug(""+selectData.getCopy());
		}else if (name.equals("moveToTop")){
			lsm.moveTo(row,0);
		}else if (name.equals("moveToBottom")){
			lsm.moveTo(row,lsm.numRows-1);
		}else if (name.equals("moveUp")){
			if (row != 0) lsm.moveTo(row,row-1);
		}else if (name.equals("moveDown")){
			if (row != lsm.numRows-1) lsm.moveTo(row,row+1);
		}else if (name.equals("allOrNone")){
			toggleChooseAll();
		}
	}
	//===================================================================
	public void fieldChanged(ewe.reflect.FieldTransfer ft,Editor ed)
	//===================================================================
	{
		if (ft.fieldName.equals("listName"))
			table.newData();
	}
	/**
	* This implements fieldEvent() in FieldListener. By default it does nothing.
	**/
	//===================================================================
	public void fieldEvent(ewe.reflect.FieldTransfer ft,Editor ed,Object event)
	//===================================================================
	{
		if (ft.fieldName.equals("selection") && event instanceof TableEvent){
			if (((TableEvent)event).type == TableEvent.SELECTION_CHANGED){
				int which = getSelectedRow();
				if (which != -1 && selectData != null){
					Vector v = getAvailable().getList(listName);
					if (v != null) selected(which,listName,v.get(which));
				}
			}
		}
	}

	//===================================================================
	public Vector getActiveList()
	//===================================================================
	{
		return getAvailable().getList(listName);
	}
	//===================================================================
	public int[] getChosenItems()
	//===================================================================
	{
		ListSelectTable.ListSelectTableModel tm = (ListSelectTable.ListSelectTableModel)table.getTableModel();
		IntArray idx = new IntArray();
		for (int i = 0; i<tm.numRows; i++)
			if (tm.boxAt(i).getState()) idx.add(i);
		return idx.toIntArray();
	}
	/**
	 * Choose or clear all the options in the current list.
	 * @param chooseOrClear true to choose all options, false to clear.
	 */
	//===================================================================
	public void chooseAll(boolean chooseOrClear)
	//===================================================================
	{
		if (selectData == null) return;
		selectData.selectNone(listName);
		ListSelectTable.ListSelectTableModel tm = (ListSelectTable.ListSelectTableModel)table.getTableModel();
		for (int i = 0; i<tm.numRows; i++){
			tm.boxAt(i).setState(chooseOrClear);
			selectData.select(listName,i,chooseOrClear);
		}
		notifyDataChange();
	}
	//===================================================================
	public void toggleChooseAll()
	//===================================================================
	{
		ListSelectTable.ListSelectTableModel tm = (ListSelectTable.ListSelectTableModel)table.getTableModel();
		if (getChosenItems().length != tm.numRows) chooseAll(true);
		else chooseAll(false);
	}
	/**
	* This gets called when an item is highlighted, not when it is chosen.
	**/
	//-------------------------------------------------------------------
	protected void selected(int row,String listName,Object selectedItem)
	//-------------------------------------------------------------------
	{
//		ewe.sys.Vm.debug(row+", "+listName+", "+selectedItem);
	}
	//##################################################################
	class ListSelectTable extends TableControl {
	//##################################################################

	boolean amFocused = false;
	//===================================================================
	public void gotFocus(int how)
	//===================================================================
	{
		amFocused = true;
		if (showSelectionAtFocus && getSelectedRow() == -1 && getTableModel().numRows >= 1){
			cursorTo(0,1,true);
		}
			else paintSelectedCells();
		super.gotFocus(how);
	}
	//===================================================================
	public void lostFocus(int how)
	//===================================================================
	{
		amFocused = false;
		paintSelectedCells();
		super.lostFocus(how);
	}
	//===================================================================
	public void onKeyEvent(KeyEvent ev)
	//===================================================================
	{
		if (ev.type != ev.KEY_PRESS) super.onKeyEvent(ev);
		else if (ev.isMoveToNextControlKey(true) || ev.isMoveToNextControlKey(false))
			standardOnKeyEvent(ev);
		else
			super.onKeyEvent(ev);
	}
	//-------------------------------------------------------------------
	protected boolean checkFirstKey()
	//-------------------------------------------------------------------
	{
		if (getTableModel().numRows == 0) return true;
		if (cursor.x == -2 || cursor.y == -2){
			cursorTo(0,1,true);
			return true;
		}
		return false;
	}

	ListSelectTableModel tm;
	//===================================================================
	ListSelectTable()
	//===================================================================
	{
		setTableModel(tm = new ListSelectTableModel());
		tm.newData();
	}

	//===================================================================
	void newData(){tm.newData(); clearSelectedCells(null); update(true);}
	//===================================================================


	//##################################################################
	class ListSelectTableModel extends GridTableModel implements EventListener{
	//##################################################################
	{
		hasColumnHeaders = hasRowHeaders = false;
		numCols = 2;
		hasPreferredSize = true;
		canHScroll = false;
		gap = 4;
		dontFocusOnChildren = true;
	}
	/**
	* This is called by the table to tell the model that it wants to select
	* a particular cell. The model should then call table.addToSelection() to
	* add it to the selection. Alternately the model can add more or different
	* cells.
	**/
	/*
	//===================================================================
	public void select(int row,int col,boolean selectOn)
	//===================================================================
	{
		if (selectOn && col == 1){
			this.table.addToSelection(Rect.buff.set(0,row,2,1),false);
		}else
			super.select(row,col,selectOn);
	}
	*/
	//===================================================================
	public boolean canSelect(int row,int col)
	//===================================================================
	{
		return col != 0;
	}
	//##################################################################
	class checkBox extends mCheckBox{
	//##################################################################
	int row;
	Object data;
	public checkBox(int row){super(""); this.row = row;}
	//##################################################################
	}
	//##################################################################

	//-------------------------------------------------------------------
	void newData()
	//-------------------------------------------------------------------
	{
		Grid g = new Grid();
		Vector v = (selectData != null) ? getAvailable().getList(listName) : null;
		CheckBoxGroup cb = new CheckBoxGroup();
		if (v != null)
			for (int i = 0; i<v.size(); i++){
				Object data = v.get(i);
				checkBox mc = new checkBox(i);
				if (selectData.singleItemPerList) mc.setGroup(cb);
				mc.backGround = Color.White;
				mc.prompt = mString.toString(data);
				mc.data = data;
				mc.setState(selectData.isSelected(listName,data));
				mc.addListener(this);
				mc.modify(NoFocus,TakesKeyFocus);
				g.add(mc,false);
				g.add(data,true);
			}
		setDataAndHeaders(g,null,null);
		FontMetrics fm = getFontMetrics();
		calculateSizes(fm);
	}

	//===================================================================
	checkBox boxAt(int row)
	//===================================================================
	{
		return (checkBox)data.objectAt(row,0);
	}
	//===================================================================
	void moveTo(int row,int dest)
	//===================================================================
	{
		if (row == -1) return;
		boolean wasSel = boxAt(row).getState();
		Object r = data.get(row);
		data.del(row);
		data.add(dest,r);
		TableControl tc = ListSelectTableModel.this.table;
		tc.clearSelectedCells(null);
		ListSelectTableModel.this.select(dest,1,true);
		tc.scrollToVisible(dest,0);
		tc.repaintNow();
		rearranged(wasSel);
	}
	//===================================================================
	void rearranged(boolean updateSel)
	//===================================================================
	{
		Vector v = new Vector();
		for (int i = 0; i<numRows; i++){
			checkBox cb = boxAt(i);
			cb.row = i;
			v.add(cb.data);
		}
		getAvailable().set(listName,v);
		if (updateSel) updateSelected();
	}
	//===================================================================
	void updateSelected()
	//===================================================================
	{
		selectData.selectNone(listName);
		for (int i = 0; i<numRows; i++){
			checkBox cb2 = boxAt(i);
			selectData.select(listName,i,cb2.getState());
		}
		ListSelect.this.notifyDataChange();
	}
	//===================================================================
	public void onEvent(Event ev)
	//===================================================================
	{
		if ((ev.type == DataChangeEvent.DATA_CHANGED) && (ev.target instanceof mCheckBox)){
			checkBox cb = ((checkBox)ev.target);
			int row = cb.row;
			boolean sel = cb.getState();
			if (ListSelectTableModel.this.table.isSelected(row,1)){
				for (int i = 0; i<numRows; i++){
					if (i == row) continue;
					if (ListSelectTableModel.this.table.isSelected(i,1)){
						checkBox cb2 = boxAt(i);
						cb2.setState(sel);
					}
				}
			}
			updateSelected();
		}
	}
	//===================================================================
	public int calculateColWidth(int col)
	//===================================================================
	{
		if (col == 1) return this.table.width-getColWidth(0);
		int got = super.calculateColWidth(col);
		return got;
	}
	/*
	//===================================================================
	public int calculateRowHeight(int row)
	//===================================================================
	{
		if (row == -1) return 0;
		else return dataSize.height;
	}
	*/
	private Insets insets = new Insets(2,2,2,2);
	//-------------------------------------------------------------------
	public Insets getCellInsets(int row,int col,Insets in)
	//-------------------------------------------------------------------
	{
		if (col != 1) return null;
		return insets;
	}
	//===================================================================
	public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta)
	//===================================================================
	{
		ta = super.getCellAttributes(row,col,isSelected,ta);
		ta.borderStyle = BDR_NOBORDER;
		ta.anchor = LEFT;
		if (hasModifier(Disabled,true)) {
			ta.fillColor = Color.LightGray;
			ta.foreground = Color.DarkGray;
		}else{
			ta.fillColor = isSelected ? Color.LightGray : this.table.backGround;
			if (isSelected && amFocused)
				ta.fillColor = Color.LightBlue;
			ta.foreground = this.table.getForeground();
		}
		if (ta.data instanceof Control) ((Control)ta.data).backGround = ta.fillColor;
		return ta;
	}
	//===================================================================
	public boolean onKeyEvent(KeyEvent ev)
	//===================================================================
	{
		if (ev.type != ev.KEY_PRESS) return super.onKeyEvent(ev);
		if (ev.isActionKey()){
			int r = getSelectedRow();
			if (r == -1) return false;
			mCheckBox mc = boxAt(r);
			if (mc == null) return false;
			if (mc.group != null && mc.getState()) return true;
			mc.setState(!mc.getState());
			updateSelected();
			return true;
		}else{
			return super.onKeyEvent(ev);
		}
	}

	//##################################################################
	}
	//##################################################################

	//##################################################################
	}
	//##################################################################
	/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	int[] got = select(
		"Select one!",null,
		new Vector(new String[]{"One","Two","Three","Four","Five","Six"}),
		new Vector(new String[]{"Two","Three"}),false);
	for (int i = 0; i<got.length; i++)
		ewe.sys.Vm.debug("Got: "+got[i]);
	//ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

