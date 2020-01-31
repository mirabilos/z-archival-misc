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
import ewe.data.*;
import ewe.util.*;
import ewe.reflect.*;
import ewe.fx.*;
//##################################################################
public class ListEditor extends Editor{
//##################################################################

/**
* These are the items being edited.
**/
public Vector items = new Vector();

public boolean confirmDelete = true;//false;

//-------------------------------------------------------------------
static IImage getIcon(String image)
//-------------------------------------------------------------------
{
	return ImageCache.cache.get(image.endsWith(".bmp") ?  image : "ewe/"+image+"small.bmp",Color.White);
}

public static IImage upIcon = getIcon("uparrow");
public static IImage downIcon = getIcon("downarrow");
public static IImage topIcon = getIcon("toparrow");
public static IImage bottomIcon = getIcon("bottomarrow");
public static IImage editIcon = getIcon("edit");

//===================================================================
public Editor getEditor(Object ld,int editor)
//===================================================================
{
	if (editor == -1) return null;
	if (ld instanceof LiveData) return ((LiveData)ld).getEditor(editor);
	Editor ed = new Editor();
	ed.setFields(ld);
	return ed;
}
//===================================================================
public ListEditor(){}
//===================================================================
public ListEditor(LiveData forWhat,int display,int editor,int defaultEditor,boolean vertical,int buttons)
//===================================================================
{
	model = forWhat;
	doStandardSetup(vertical,buttons,getEditor(forWhat,display),getEditor(forWhat,editor),getEditor(forWhat,defaultEditor));
}

//===================================================================
public ListEditor(Object forWhat,boolean vertical,int buttons)
//===================================================================
{
	model = forWhat;
	boolean pda = Gui.screenIs(Gui.PDA_SCREEN);
	int display = pda ? -1 : 0;
	int edit = pda ? 0 : -1;
	int def = model instanceof Copyable ? 0 : -1;
	vSplit = pda;
	if (!(model instanceof Copyable)) buttons &= ~ TOOL_BUTTON;
	doStandardSetup(vertical,buttons,getEditor(forWhat,display),getEditor(forWhat,edit),getEditor(forWhat,def));
}

/**
Set the list of items. The what parameter must be a Vector or an Object[]. If it is
a Vector, then the "items" field is set to it - otherwise a new Vector is created
to hold all the elements of the array and that is assigned to "items".
**/
//===================================================================
public void setData(Object what)
//===================================================================
{
	if (what instanceof Object[])
		what = new Vector((Object[])what);
	if (what == null)
		what = new Vector();
	if (what instanceof Vector) {
		items = (Vector)what;
		itemsChanged(false,-1);
	}
}
/**
Get the list of items. The what parameter must be a Vector. The Vector is first
cleared and then all the items in the "items" field is copied into it.
**/
//===================================================================
public void getData(Object obj)
//===================================================================
{
	if (obj == items) return;
	if (!(obj instanceof Vector)) return;
	((Vector)obj).clear();
	((Vector)obj).addAll(items);
}

/**
 * Get the number of items.
 */
//===================================================================
public int getItemCount()
//===================================================================
{
	return items.size();
}
/**
 * Get the item at the specified index.
 * @param index The index of the item to get.
 * @return The item at the index.
 */
//===================================================================
public Object getItem(int index)
//===================================================================
{
	return items.get(index);
}
public Object model;
/**
* Get the name for the item. By default if it is a LiveData, it will return getName(), otherwise
* it will return item.toString();
**/
//===================================================================
public String getNameFor(Object item)
//===================================================================
{
	if (item == null) return "(null)";
	if (item instanceof LiveData) return ((LiveData)item).getName();
	return item.toString();
}
//===================================================================
public Object getToPutInList(Object item)
//===================================================================
{
	return getNameFor(item);
}
/**
* This is the name of the item being edited.
**/
public String itemName = "Item";

/**
* This is the list of items.
**/
public mList list;

public static final int UPDOWN_BUTTONS = 0x1;
public static final int TOPBOTTOM_BUTTONS = 0x2;
public static final int DELETE_BUTTON = 0x4;
public static final int NEW_BUTTON = 0x8;
public static final int EDIT_BUTTON = 0x10;
public static final int TOOL_BUTTON = 0x20;

public String listTitle = null;

public Menu toolsMenu;

//===================================================================
public void make(boolean reMake)
//===================================================================
{
	if (!made || reMake){
		itemsChanged(false,-1);
	}
	super.make(reMake);
}
//-------------------------------------------------------------------
protected mButton addButton(CellPanel addTo,boolean vertical,String image,String title,String field)
//-------------------------------------------------------------------
{
	IImage mi = getIcon(image);
	return addButton(addTo,vertical,mi,title,field);
}
//-------------------------------------------------------------------
protected mButton addButton(CellPanel addTo,boolean vertical,IImage mi,String title,String field)
//-------------------------------------------------------------------
{
	mButton b;
	if (mi.getWidth() == 0) b = new mButton(title);
	else b = new mButton(new IconAndText(mi,"",getFontMetrics()));
	addTo.addNext(b);
	addField(b,field);
	if (vertical) addTo.endRow();
	return b;
}
/**
* Get the toolbar with the specified buttons.
**/

//===================================================================
private MenuItem iconizeMenu(Menu m,String text,IImage image)
//===================================================================
{
	MenuItem mi = new MenuItem().iconize(text,image,true);
	//mi.image = new IconAndText,getFontMetrics());
	mi.subMenu = m;
	return mi;
}
//===================================================================
public CellPanel getToolBar(boolean vertical,int buttons)
//===================================================================
{
	CellPanel cp = new ButtonBar();
	cp.modify(MouseSensitive,0);
	FontMetrics fm = getFontMetrics();

	IImage toolIcon = new mImage("ewe/SmallConfig.bmp","ewe/SmallConfigMask.bmp");

	Menu m;
	Menu context = new Menu();
	Menu newMenu = m = new Menu();
	m.text = "New";
	m.addItem(new MenuItem("newItemOnTop",new IconAndText(topIcon,"New "+itemName+" On Top",fm)));
	m.addItem(new MenuItem("newItemAboveSelected",new IconAndText(upIcon,"New "+itemName+" Above Selected",fm)));
	m.addItem(new MenuItem("newItemBelowSelected",new IconAndText(downIcon,"New "+itemName+" Below Selected",fm)));
	m.addItem(new MenuItem("newItem",new IconAndText(bottomIcon,"New "+itemName+" At Bottom",fm)));
	Menu moveMenu = m = new Menu();
	m.text = "Move";
	m.addItem(new MenuItem("moveToTop",new IconAndText(topIcon,itemName+" To Top",fm)));
	m.addItem(new MenuItem("moveUp",new IconAndText(upIcon,itemName+" Up",fm)));
	m.addItem(new MenuItem("moveDown",new IconAndText(downIcon,itemName+" Down",fm)));
	m.addItem(new MenuItem("moveToBottom",new IconAndText(bottomIcon,itemName+" To Bottom",fm)));
	Menu deleteMenu = m = new Menu();
	m.addItem(new MenuItem("dontDelete",new IconAndText(stop,"No, Don't Delete "+itemName,fm)));
	m.addItem(new MenuItem("yesDelete",new IconAndText(tick,"Yes, Delete "+itemName,fm)));

	if (toolsMenu == null) toolsMenu = new Menu();
	toolsMenu.text = "Tools";
	if (defaultEditor != null){
		toolsMenu.addItem(new MenuItem("editDefault",new IconAndText(new mImage("ewe/editsmall.bmp",Color.White),"Edit Default "+itemName,getFontMetrics())));
		toolsMenu.addItem(new MenuItem("setDefault",new IconAndText(new mImage("ewe/savesmall.bmp",Color.White),"Set As Default "+itemName,getFontMetrics())));
	}
	if (editor != null){
		context.addItem(new MenuItem("editItem",new IconAndText(editIcon,"Edit "+itemName,fm)));
		context.addItem("-");
	}
	context.addItem(iconizeMenu(newMenu,"New",getIcon("new")));
	context.addItem(iconizeMenu(moveMenu,"Move",upIcon));
	if (confirmDelete){
		context.addItem(iconizeMenu(deleteMenu,"Delete",getIcon("delete")));
	}else{
		context.addItem(new MenuItem("deleteItem",new IconAndText(getIcon("delete"),"Delete",fm)));
	}
	if (toolsMenu.items.size() > 0) {
		context.addItem("-");
		context.addItem(iconizeMenu(toolsMenu,"Tools",toolIcon));
	}
	addField(list,"contextMenuItem").setMenu(context);
	if ((buttons & TOOL_BUTTON) != 0){
		PullDownMenu pdm = new ButtonPullDownMenu("",toolsMenu);
		pdm.arrowDirection = 0;
		pdm.image = toolIcon;
		addField(pdm,"toolMenuItem");
		cp.addNext(pdm).setToolTip("Additional Tools");
		if (vertical) cp.endRow();
	}
	if ((buttons & NEW_BUTTON) != 0){
		addField(addButton(cp,vertical,"new","New","newItem"),"newMenu").setToolTip("Add a new "+itemName+"\n(Hold down for options)").setMenu(newMenu);
	}
	if (((buttons & EDIT_BUTTON) != 0) && (editor != null))
		addButton(cp,vertical,editIcon,"Edit","editItem").setToolTip("Edit "+itemName);
	if ((buttons & TOPBOTTOM_BUTTONS) != 0) addButton(cp,vertical,topIcon,"Top","moveToTop").setToolTip("Move "+itemName+" to Top");
	if ((buttons & UPDOWN_BUTTONS) != 0){
		addButton(cp,vertical,upIcon,"Up","moveUp").setToolTip("Move "+itemName+" Up");
		addButton(cp,vertical,downIcon,"Down","moveDown").setToolTip("Move "+itemName+" Down");
	}
	if ((buttons & TOPBOTTOM_BUTTONS) != 0) addButton(cp,vertical,bottomIcon,"Down","moveToBottom").setToolTip("Move "+itemName+" to Bottom");

	if ((buttons & DELETE_BUTTON) != 0){
		Control c = addButton(cp,vertical,"delete","Delete","deleteItem").setToolTip("Delete "+itemName+
			(confirmDelete ? "\n(Hold down to confirm)":""));
		if (confirmDelete){
			addField(c,"deleteMenu").setMenu(deleteMenu);
		}
	}

	return cp;
}
public MenuItem newMenu = new MenuItem();
public MenuItem deleteMenu = new MenuItem();
public MenuItem toolMenuItem = new MenuItem();
public MenuItem contextMenuItem = new MenuItem();
/**
* If a splittable panel is used, this says if the two sections are vertically layed out.
**/
//===================================================================
public boolean vSplit = true;
//===================================================================

public Editor display;
public Editor editor;
public Editor defaultEditor;

public boolean useSplittablePanel = true;
public boolean editorOnTop = true;
public int listRows = 5, listColumns = 30;
//===================================================================
public void doStandardSetup(boolean verticalToolBar,int buttons,Editor toDisplay,Editor toEdit,Editor defaultEditor)
//===================================================================
{
	display = toDisplay;
	editor = toEdit;
	this.defaultEditor = defaultEditor;
	if (toEdit != null) {
		Gui.setOKCancel(toEdit);
		toEdit.defaultTitleTo("Edit "+itemName);
	}
	if (defaultEditor != null) {
		Gui.setOKCancel(defaultEditor);
		defaultEditor.defaultTitleTo("Edit Default "+itemName);
	}
	if (toEdit != null && toDisplay != null)
		toDisplay.modify(DisplayOnly,0);
	if (toEdit != null && defaultEditor == null && model instanceof Copyable) this.defaultEditor = toEdit;
 	list = new mList(listRows,listColumns,false);
	list.notifyDataChangeOnSelect = false;
	list.setToolTip("Right-click for Options");
	addField(list,"theList");
	CellPanel listArea = new CellPanel();
	if (toEdit != null && buttons != 0) buttons |= EDIT_BUTTON;
	CellPanel cp = getToolBar(verticalToolBar,buttons);
	listArea.setText(listTitle);
	listArea.addNext(new ScrollBarPanel(list));
	if (verticalToolBar) listArea.addNext(cp).setCell(VSTRETCH).setControl(DONTFILL|CENTER);
	else listArea.endRow();
	if (!verticalToolBar) listArea.addLast(cp).setCell(HSTRETCH).setControl(DONTFILL|CENTER);
	CellPanel top, bottom = null;
	if (useSplittablePanel && toDisplay != null){
		int type = vSplit ? SplittablePanel.VERTICAL : SplittablePanel.HORIZONTAL;
		SplittablePanel sp = new SplittablePanel(type);
		top = sp.getNextPanel();
		bottom = sp.getNextPanel();
		addNext(sp);
	}else{
		addNext(top = new CellPanel());
		if (vSplit || toDisplay == null) endRow();
		if (toDisplay != null) addLast(bottom = new CellPanel());
	}
	CellPanel editorPanel = editorOnTop ? top : bottom;
	if (toDisplay != null) {
		editorPanel.setBorder(mInput.inputEdge|BF_RECT,4);
	}
	if (toDisplay != null){
		int constraints = toDisplay.constraints;
		top.addLast(editorOnTop ? toDisplay : listArea);
		bottom.addLast(editorOnTop ? listArea : toDisplay);
		addField(toDisplay,"theEditor");
		(editorOnTop ? top : bottom).constraints = constraints;

	}else{
		top.addLast(listArea);
	}
}
//===================================================================
public void itemsChanged(boolean doNotify,int select)
//===================================================================
{
	int got = list.modify(Invisible,0);
	list.removeAll();
	for (int i = 0; i<items.size(); i++)
		list.addItem(getToPutInList(items.get(i)));
	list.select(select);
	if (select != -1) list.makeItemVisible(select);
	if (doNotify) notifyDataChange();
	list.updateItems();
	list.restore(got,Invisible);
	list.repaintNow();
	//if (doNotify)
	newSelected(select);
}
Object editing = null;
//-------------------------------------------------------------------
protected void newSelected(int idx)
//-------------------------------------------------------------------
{
	if (display != null){
		if (idx == -1) display.setObject(editing = null);
		else display.setObject(editing = items.get(idx));
	}
	modifyFields("deleteItem,moveUp,moveDown,moveToTop,moveToBottom,editItem",list.selectedIndex == -1,Disabled,0,true);
}
//===================================================================
public void shown()
//===================================================================
{
	super.shown();
	newSelected(-1);
}
//===================================================================
public Object getNewObject()
//===================================================================
{
	if (model instanceof Copyable) return ((Copyable)model).getCopy();
	else {
		try{
			return model.getClass().newInstance();
		}catch(Exception e){
			return null;
		}
	}
}
/**
* This should be called if an item has its display name changed.
**/
//===================================================================
public void nameChanged(int idx)
//===================================================================
{
	list.changeItem(idx,getToPutInList(items.get(idx)));
	list.updateItems();
}
//===================================================================
public void doDelete(int idx,boolean doNotify)
//===================================================================
{
	if (idx<0 || idx>=items.size()) return;
	int was = list.selectedIndex;
	int toSel = was;
	if (idx == was)
		if (idx >= items.size()-1)
			toSel--;
	items.del(idx);
	itemsChanged(doNotify,toSel);
}

//-------------------------------------------------------------------
protected boolean execEditor(Object toEdit,Editor editor)
//-------------------------------------------------------------------
{
		Object dv = null;
		if (toEdit instanceof DataUnit) dv = ((DataUnit)toEdit).getCopy();
		editor.setObject(toEdit);
		if (editor.execute(null,Gui.FILL_FRAME) == IDOK) return true;
		if (dv != null) ((DataUnit)toEdit).copyFrom(dv);
		return false;
}
//===================================================================
public void doEdit(int idx)
//===================================================================
{
	if (editor != null && idx >= 0 && idx < items.size())
		if (execEditor(items.get(idx),editor))
			itemsChanged(true,idx);
}
/**
* This adds a new item as if the user had pressed the New Item button.
* @param toAdd The object to add.
* @param index The index to put the new object in. If it is -1 then it is added to the end.
* @param doNotify If this is true then a standard DataChanged event is generated.
*/
//===================================================================
public void doAddNew(Object toAdd,int index,boolean doNotify)
//===================================================================
{
	if (index == -1) items.add(toAdd);
	else items.add(index,toAdd);
	itemsChanged(doNotify,index == -1 ? items.size()-1 : index);
}
/**
* This adds a new item as if the user had pressed the New Item button.
* @param toAdd The object to add.
*/
//===================================================================
public void doAddNew(Object toAdd)
//===================================================================
{
	doAddNew(toAdd,-1,true);
}

//-------------------------------------------------------------------
protected void shift(int idx,int dx)
//-------------------------------------------------------------------
{
	Object was = items.get(idx);
	items.del(idx);
	items.add(idx+dx,was);
	itemsChanged(true,idx+dx);
}

//-------------------------------------------------------------------
void addNewAndEdit(int location)
//-------------------------------------------------------------------
{
	if (location < 0) location = 0;
	if (location > items.size()) location = items.size();
	Object newOne = getNewObject();
	if (newOne == null) return;
	if (editor != null) {
		editor.setObject(newOne);
		if (editor.execute() != IDOK) return;
	}
	doAddNew(newOne,location,true);
}
//===================================================================
public void action(FieldTransfer ft,Editor ed)
//===================================================================
{
	action(ft.fieldName,ed);
}
//===================================================================
public void action(String name,Editor ed)
//===================================================================
{
	int idx = list.selectedIndex;
	if (name.equals("deleteItem")){
		if (confirmDelete)
			if (new MessageBox("Delete "+itemName,"Delete selected "+itemName+"?",MBYESNO).execute() != IDOK) return;
		doDelete(idx,true);
	}else if (name.equals("yesDelete")){
		doDelete(idx,true);
	}else if (name.equals("newItem")){
		addNewAndEdit(items.size());
	}else if (name.equals("newItemOnTop")){
		addNewAndEdit(0);
	}else if (name.equals("newItemAboveSelected")){
		addNewAndEdit(idx);
	}else if (name.equals("newItemBelowSelected")){
		addNewAndEdit(idx+1);
	}else if (name.equals("moveUp")){
		if (idx <= 0) return;
		shift(idx,-1);
	}else if (name.equals("moveDown")){
		int len = items.size();
		if (idx < 0 || idx >= len-1) return;
		shift(idx,1);
	}else if (name.equals("moveToTop")){
		if (idx <= 0) return;
		shift(idx,-idx);
	}else if (name.equals("moveToBottom")){
		int len = items.size();
		if (idx < 0 || idx >= len-1) return;
		shift(idx,len-idx-1);
	}else if (name.equals("editItem")){
		if (idx == -1) return;
		doEdit(idx);
	}else if (name.equals("editDefault")){
		if (model != null && defaultEditor != null)
			execEditor(model,defaultEditor);
	}else if (name.equals("setDefault")){
		if (idx == -1) return;
		if (new MessageBox("Set as Default "+itemName,"Set this "+itemName+" to be the default?",MBYESNO).execute() != IDOK) return;
		Object got = getItem(idx);
		if (got instanceof Copyable) model = ((Copyable)got).getCopy();
	}else if (name.equals("theList") && idx != -1 && editor != null){//Double clicked
		doEdit(idx);
	}
}
//===================================================================
public void fieldEvent(FieldTransfer ft,Editor ed,Object event)
//===================================================================
{
	if (ft.fieldName.equals("theList") && (event instanceof ListEvent)){
		if 	(((Event)event).type == ListEvent.SELECTED){
			newSelected(list.selectedIndex);
		}
	}
}
//===================================================================
public void fieldChanged(FieldTransfer ft,Editor ed)
//===================================================================
{
	if (ft.fieldName.equals("theEditor")){
		if (editing == null || list.selectedIndex == -1) return;
		if (!getToPutInList(editing).equals(list.getItemAt(list.selectedIndex).label)){
			nameChanged(list.selectedIndex);
		}
	}else if (ft.fieldName.equals("newMenu")){
		action(newMenu.label,ed);
	}else if (ft.fieldName.equals("deleteMenu")){
		action(deleteMenu.label,ed);
	}else if (ft.fieldName.equals("toolMenuItem")){
		action(toolMenuItem.label,ed);
	}else if (ft.fieldName.equals("contextMenuItem")){
		action(contextMenuItem.label,ed);
	}else if (ft.fieldName.equals("theList")){
		//ewe.sys.Vm.debug("List changed!");
	}
}

//===================================================================
public static void main(String args[]) throws Exception
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	Class c = Class.forName(args[0]);
	ListEditor ed = new ListEditor(c.newInstance(),true,0xffffffff);
	ed.title = c.getName();
	ed.execute();
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################

