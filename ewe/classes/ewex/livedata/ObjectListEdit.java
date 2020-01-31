package ewex.livedata;
import ewe.data.*;
import ewe.ui.*;
import ewe.util.*;
/*
// header - edit "Data/yourJavaHeader" to customize
// contents - edit "EventHandlers/Java file/onCreate" to customize
//
*/
//##################################################################
public abstract class ObjectListEdit extends Form{
//##################################################################

public LiveData model,backup;
public Editor editor, display;

public boolean execEditor = true;
public boolean useMultiPanel = false;
public int execMode = Gui.FILL_FRAME;
public boolean keepUndoCopy = true;

public Vector objects = new Vector();
LiveData editing;
String editingName;
int editingIndex;

//===================================================================
public LiveData getForIndex(int which)
//===================================================================
{
	return getForObject(objects.get(which));
}
//===================================================================
public LiveData getForObject(Object obj)
//===================================================================
{
	if (obj instanceof LiveData) return (LiveData)obj;
	return null;
}
//===================================================================
public String getDisplayName(int index)
//===================================================================
{
	return ((LiveData)getForIndex(index)).getName();
}
//===================================================================
public int getObjectCount() {return objects.size();}
//===================================================================

//===================================================================
public void editNow(int which)
//===================================================================
{
	editingIndex = which;
	editing = getForIndex(which);
	editingName = getDisplayName(which);
	if (editing == null) return;
	if (keepUndoCopy) backup = (LiveData)editing.getCopy();
	editor.setObject(editing);
	if (execEditor)
		editor.exec(null,this,execMode);
	else makeEditorVisible();
}
//===================================================================
public void makeEditorVisible(){}
public void makeListVisible(){}
//===================================================================
protected void objectEdited(LiveData edited,int index)
//===================================================================
{

}
//===================================================================
public void stopEditing(boolean accept)
//===================================================================
{
	if (editing != null) {
		if (accept) editor.fromControls();
		else if (backup != null) editing.copyFrom(backup);
		if (accept) objectEdited(editing,editingIndex);
		editing = null;
		if (editingName != null)
			if (!editingName.equals(getDisplayName(editingIndex)))
				nameChanged(editingIndex);
	}
	makeListVisible();
}
//===================================================================
public abstract void nameChanged(int index);
//===================================================================

//===================================================================
public void setObjects(Vector v)
//===================================================================
{
	objects = v;
	objectsSet();
}
//-------------------------------------------------------------------
protected void objectsSet() {}
//-------------------------------------------------------------------

//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DataChangeEvent && !execEditor){
		Control c = null;
		for (c = (Control)ev.target; c != null; c = c.getParent())
			if (c == editor) break;
		if (c != null){
			if (editingName != null && editingIndex != -1)
				if (!editingName.equals(getDisplayName(editingIndex))){
						nameChanged(editingIndex);
				}
		}
	}
	super.onEvent(ev);
}
//##################################################################
}
//##################################################################


