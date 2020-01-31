package samples.ui;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.data.*;
import samples.data.PersonInfo;

//##################################################################
public class Trees extends Editor{
//##################################################################


TreeControl tree;
TreeTableModel treeModel;

public LiveTreeNode group = new Group("Everyone");
public MenuItem groupMenu = new MenuItem();

//===================================================================
public Trees()
//===================================================================
{
	title = "Tree Sample";
	resizable = true;
	//
	// The MyTree class is defined below.
	//
	addLast(new ScrollBarPanel(tree = new MyTree())).setPreferredSize(300,200);
	treeModel = tree.getTreeTableModel();
	//
	//treeModel.hideRoot = true;
	//
	treeModel.hasControls = true;
	treeModel.expansionLevel = 2;
	//
	// groupMenu is of type MenuItem (see above) - which you use when you want to react to popup menu
	// events for a control, separate from "normal" events from that control.
	//
	addField(tree,"groupMenu");
	addField(addNext(new mButton("Open New Window")).setCell(HSTRETCH),"newWindow");
	addField(addNext(new mButton("Add Everyone")).setCell(HSTRETCH),"add");
	//
	Group g;
	group.addChild(g = new Group("Simpsons"));
	g.addChild(new PersonInfo("Simpson","Homer Jay",1,4,1950,PersonInfo.Male));//.set(PersonInfo.homer,null));
	g.addChild(new PersonInfo("Simpson","Bart",22,8,1993,PersonInfo.Male));//.set(PersonInfo.bart,null).makeEditorTree(tree));
	g.addChild(new PersonInfo("Simpson","Lisa",5,11,1995,PersonInfo.Female));//.set(PersonInfo.lisa,null));
	group.addChild(g = new Group("Random"));
	g.addChild(new PersonInfo("Smith","John",4,5,1960,PersonInfo.Male));//.set(PersonInfo.adultMale,null));
	g.addChild(new PersonInfo("Doe","Jane",9,9,1986,PersonInfo.Female));//.set(PersonInfo.childFemale,null));
	//
	tree.backGround = Color.White;
	//tree.getTreeTableModel().setRootObject(group);
}
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor ed)
//===================================================================
{
	if (ft.fieldName.equals("newWindow")){
		new Trees().show();
	}else if (ft.fieldName.equals("add")){
		tree.getTreeTableModel().setRootObject(group);
		tree.update(true);
	}
}

//===================================================================
public void fieldChanged(ewe.reflect.FieldTransfer ft,Editor ed)
//===================================================================
{
	if (ft.fieldName.equals("groupMenu")){
		if (groupMenu.label.equals("Add Group") || groupMenu.label.equalsIgnoreCase("Add Sub-Group")){
			String newGroup = new InputBox("New Group").input("NewGroup",20);
			if (newGroup == null) return;
			Group g = new Group(newGroup);
			LiveTreeNode tn = (LiveTreeNode)treeModel.getTreeNodeAt(treeModel.selectedLine);
			tn.addChild(g);
			tree.updateInsertion(g);
		}else if (groupMenu.label.startsWith("Delete")){
			LiveTreeNode tn = (LiveTreeNode)treeModel.getTreeNodeAt(treeModel.selectedLine);
			LiveTreeNode p = (LiveTreeNode)tn.getParent();
			tree.deleteAndUpdate(tn,p);
		}else if (groupMenu.label.startsWith("Rename")){
			TreeNode tn = (LiveTreeNode)treeModel.getTreeNodeAt(treeModel.selectedLine);
			if (tn instanceof Group){
				Group g = (Group)tn;
				String newGroup = new InputBox("Rename Group").input(g.getName(),20);
				if (newGroup == null) return;
				g.name = newGroup;
				treeModel.paintLine(treeModel.selectedLine);
			}
		}else if (groupMenu.label.startsWith("Edit") || groupMenu.label.startsWith("Add Person")){
			PersonInfo pi = new PersonInfo();
			LiveTreeNode tn = (LiveTreeNode)treeModel.getTreeNodeAt(treeModel.selectedLine);
			if (tn instanceof PersonInfo) pi.copyFrom(tn);
			Editor ped = pi.getEditor(0);
			Gui.setOKCancel(ped);
			int got = ped.execute();
			if (got == ped.IDCANCEL) return;
			if (tn instanceof PersonInfo) {
				((PersonInfo)tn).copyFrom(pi);
				treeModel.paintLine(treeModel.selectedLine);
			}else{
				tn.addChild(pi);
				tree.updateInsertion(pi);
			}
		}
	}
}

//##################################################################
class MyTree extends TreeControl{
//##################################################################

//===================================================================
public MyTree()
//===================================================================
{
	setTableModel(new MyTreeModel());
	modify(WantDrag,0);
	allowClipboardOperations = true;
}
//===================================================================
public boolean takeData(Object data,DragContext how)
//===================================================================
{
	if (how == null){
		LiveTreeNode tn = (LiveTreeNode)treeModel.getTreeNodeAt(treeModel.selectedLine);
		if (!(tn instanceof Group)){
			new MessageBox("Error","Can only paste into a group!",0).exec();
			return false;
		}
		tn.addChild((MutableTreeNode)data);
		updateInsertion((MutableTreeNode)data);
		return true;
	}else
		return true;
}
//===================================================================
public boolean acceptsData(Object data,DragContext how)
//===================================================================
{
	if (!(data instanceof MutableTreeNode)) return false;
	TreeNode tn = treeModel.getTreeNodeAt(treeModel.selectedLine);
	if (!(tn instanceof Group) || treeModel.isCut(tn)) return false;
	return true;
}
//===================================================================
public void dataDraggedOver(Object data,Point curPoint,PenEvent ev)
//===================================================================
{
	TreeTableModel tm = getTreeTableModel();
	int line = indexOfPoint(curPoint.x,curPoint.y);
	Object node = tm.getTreeNodeAt(line);
	if (node instanceof Group) {
		willAcceptDrop();
		tm.selectOnly(line);
	}else{
		tm.selectOnly(-1);
		dontAcceptDrop();
	}
}
//===================================================================
public boolean dataDroppedOn(Object data,Point where,DragContext dc)
//===================================================================
{
	LiveTreeNode tn = (LiveTreeNode)treeModel.getTreeNodeAt(treeModel.selectedLine);
	if (!(tn instanceof Group) || !(data instanceof LiveTreeNode)) return false;
	LiveTreeNode mt = (LiveTreeNode)data;
	tn.addChild(mt);
	if ("copyGroup".equals(dc.dropAction))
		while(mt.getChildCount() != 0)
			mt.removeChild((MutableTreeNode)mt.getChild(0));
	updateInsertion(mt);
	notifyDataChange();
	return true;
}

//===================================================================
public void dataAccepted(Control byWho,Object data,String action)
//===================================================================
{
	Vector v = treeModel.cutNodes;
	if (v.size() == 0) return;
	if (!action.startsWith("copy")) {
		for (int i = 0; i<v.size(); i++){
			Object obj = v.get(i);
			if (!(obj instanceof MutableTreeNode)) continue;
			MutableTreeNode tn = (MutableTreeNode)obj;
			TreeNode p = tn.getParent();
			if (p != null){
				if (v.find(p) != -1) continue;
				if (p instanceof MutableTreeNode){
					deleteAndUpdate(tn,(MutableTreeNode)p);
					notifyDataChange();
				}
			}
		}
	}
	v.clear();
	update(true);
	//int which = treeModel.cutLines
}
//===================================================================
public boolean getDataToDragAndDrop(DragContext dc)
//===================================================================
{
	if (!super.getDataToDragAndDrop(dc)) return false;
	if (dc.dataToDrag instanceof Group)
		dc.setDropOptions(
		new MenuItem[]{new MenuItem("Copy Group Only","copyGroup"),new MenuItem("Copy Group and Children","copy"),new MenuItem("Move Here","move")},
		false,true);
	else
		dc.setDropOptions(false,true);
	return true;
}
//===================================================================
public Menu getClipboardMenu(Menu addTo)
//===================================================================
{
	addTo = super.getClipboardMenu(addTo);
	if (addTo == null) return null;
	LiveTreeNode tn = (LiveTreeNode)treeModel.getTreeNodeAt(treeModel.selectedLine);
	Object dt = getClipObject();
	if (dt instanceof Group && tn instanceof Group){
		addTo.addItem("Paste Group Only");
	}
	return addTo;
}
//===================================================================
public void popupMenuEvent(Object selectedItem)
//===================================================================
{
	if (selectedItem.toString().equals("Paste Group Only")){
		LiveTreeNode mt = (LiveTreeNode)getClipObject();
		while(mt.getChildCount() != 0)
			mt.removeChild((MutableTreeNode)mt.getChild(0));
		takeFromClipboard(mt,"copyGroup");
	}else{
		super.popupMenuEvent(selectedItem);
	}
}
//##################################################################
class MyTreeModel extends TreeTableModel{
//##################################################################


//===================================================================
public Object getDataToTransfer()
//===================================================================
{
	if (selectedLine == -1) return null;
	Object o = getDataObjectAt(selectedLine);
	Object ret = null;
	if (o instanceof Group){
		ret = ((Group)o).getCopy();
		((Group)ret).copyChildrenFrom((Group)o);
	}else if (o instanceof PersonInfo)
		ret = ((PersonInfo)o).getCopy();
	return ret;
}
//===================================================================
public Menu getMenuFor(int idx)
//===================================================================
{
	TreeNode tn = getTreeNodeAt(idx);
	if (tn instanceof PersonInfo){
		return new Menu(new String[]{"Edit Person","Delete Person"},"Drop");
	}else if (tn instanceof Group){
		if (tn.getParent() != null)
			return new Menu(new String[]{"Add Person","Add Sub-Group","Rename Group","Delete Group"},"Drop");
		else
			return new Menu(new String[]{"Add Group"},"Drop");
	}
	return null;
}
//##################################################################
}
//##################################################################


//##################################################################
}
//##################################################################

//##################################################################
public static class Group extends LiveTreeNode{
//##################################################################

public Group(){};
public Group(String nm){name = nm;}
public String name = "unnamed";

public String _fields = "name";

public String getName()
{
	return name;
}
public boolean isLeaf() {return false;}
public static ewe.fx.IImage peopleIcon = new ewe.fx.mImage("samples/peoplesmall.bmp",ewe.fx.Color.White);
//===================================================================
public ewe.fx.IImage getIcon() {return peopleIcon;}
//===================================================================
public String toString() {return "Group: "+getName();}

//===================================================================
public void copyChildrenFrom(Group other)
//===================================================================
{
	if (other == null) return;
	for (Iterator it = other.getChildren(); it.hasNext();){
		LiveTreeNode ltn = (LiveTreeNode)it.next();
		LiveTreeNode ltn2 = (LiveTreeNode)ltn.getCopy();
		addChild(ltn2);
		if (ltn2 instanceof Group) ((Group)ltn2).copyChildrenFrom((Group)ltn);
	}
}
//##################################################################
}
//##################################################################
//##################################################################
}
//##################################################################
