package ewex.livedata;
import ewe.data.*;
import ewe.ui.*;
import ewe.util.*;

//##################################################################
public class ListEdit extends ObjectListEdit{
//##################################################################

public ChoiceControl list;
public boolean hSplit = true;
protected boolean made = false;
protected MultiPanel multiPanel;
protected CellPanel listVisiblePanel,editorVisiblePanel;

//===================================================================
public ListEdit()
//===================================================================
{
}
//===================================================================
protected ChoiceControl makeList()
//===================================================================
{
	return new mList(10,20,false);
}
//===================================================================
protected Control getListDisplay(ChoiceControl list)
//===================================================================
{
	if (list instanceof ScrollClient)
		return new ScrollBarPanel((ScrollClient)list);
	else
		return new ScrollBarPanel(new ScrollableHolder(list));
}
//===================================================================
public void make(boolean reMake)
//===================================================================
{
	if (!made) {
		made = true;
		list = makeList();
		listVisiblePanel = new CellPanel();
		editorVisiblePanel = new CellPanel();
		editorVisiblePanel.addLast(display != null ? display : editor);
		if (!execEditor || display != null){
			if (useMultiPanel){
				multiPanel = new CardPanel();
				multiPanel.addItem(listVisiblePanel,"List",null);
				multiPanel.addItem(editorVisiblePanel,"Editor",null);
				addLast((Control)multiPanel);
			}else{
				int type = hSplit ? SplittablePanel.VERTICAL : SplittablePanel.HORIZONTAL;
				SplittablePanel sp = new SplittablePanel(type);
				CellPanel cp = sp.getNextPanel();
				cp.addLast(listVisiblePanel);
				cp = sp.getNextPanel();
				cp.addLast(editorVisiblePanel);
				addLast(sp);
			}
		}else
			addLast(list);
		setupPanels();
		objectsSet();
	}
	super.make(reMake);
}
Control acceptEdit, cancelEdit;
//-------------------------------------------------------------------
protected void setupPanels()
//-------------------------------------------------------------------
{
	if (execEditor && display == null){
		CellPanel b = new CellPanel();
		b.equalWidths = true;
		editorVisiblePanel.addLast(b,b.HSTRETCH,b.HFILL);
		b.addNext(acceptEdit = new mButton("Accept"));
		b.addLast(cancelEdit = new mButton("Cancel"));
	}
}
//-------------------------------------------------------------------
protected void objectsSet()
//-------------------------------------------------------------------
{
	if (list == null) return;
	list.items.clear();
	int s = getObjectCount();
	for (int i = 0; i<s; i++)
		list.addItem(getDisplayName(i));
	if (list instanceof mList) ((mList)list).selectAll(false);
	list.repaintNow();
}

//===================================================================
public void nameChanged(int index)
//===================================================================
{
	list.changeItem(index,getDisplayName(index));
}
//===================================================================
public void makeEditorVisible(){if (multiPanel != null) multiPanel.select("Editor");}
public void makeListVisible(){if (multiPanel != null) multiPanel.select("List");}
//===================================================================
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.type == ev.PRESSED){
		if (ev.target == acceptEdit) stopEditing(true);
		else if (ev.target == cancelEdit) stopEditing(false);
		else super.onControlEvent(ev);
	}else if (ev instanceof MenuEvent && ev.target == list  && !list.menuIsActive()) {
		if (ev.type == ListEvent.SELECTED) {
			int idx = list.selectedIndex;
			if (idx != -1){
				stopEditing(true);
				if (display == null) editNow(idx);
				else display.setObject(getForIndex(idx));
			}
		}
	}else if (ev instanceof MenuEvent && ev.target == list){
	/*
		if (ev.type == MenuEvent.SELECTED){
			String which = ((MenuItem)((MenuEvent)ev).selectedItem).label;
			if (which.equals("Delete")) //Delete
				remove();
			else if (which.equals("Add Copy"))
				addCopy();
			else if (which.equals("To Bottom"))
				toBottom();
			else if (which.equals("To Top"))
				toTop();
		}
	*/
	}else{
		super.onControlEvent(ev);
	}
}
//##################################################################
}
//##################################################################

