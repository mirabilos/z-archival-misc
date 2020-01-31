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
package ewe.filechooser;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.io.File;
import ewe.sys.Time;
import ewe.sys.Locale;
import ewe.data.*;

//##################################################################
public class FileTree extends TreeTableModel implements FileClipboard.FileClipboardSource{
//##################################################################

public File afile;
public FileChooser chooser;
{
	selectExpanded = true;
	showWaitCursor = false;
}
//===================================================================
public FileTree(String start,File afile,FileChooser chooser)
//===================================================================
{
	this(start,afile);
	this.chooser = chooser;
}
//===================================================================
public FileTree(String start,File afile)
//===================================================================
{
	this.afile = afile;
	DirectoryNode dn = new DirectoryNode(start,afile);
	setRootObject(dn);
}
//===================================================================
public File getFileAt(int selectedLine)
//===================================================================
{
	TreeNode tn = getTreeNodeAt(selectedLine);
	if (tn == null) return null;
	return ((DirectoryNode)tn).toFile();
}

//===================================================================
public void pressed(Point p)
//===================================================================
{
	if ((table.currentPenEvent.modifiers & PenEvent.RIGHT_BUTTON) != 0) return;
	super.pressed(p);
}

TreeNode targetNode  = null;

//===================================================================
void expandAndSelect(TreeNode node)
//===================================================================
{
	if (node != null) expandAndSelect(indexOf(node));
}

//===================================================================
void expandAndSelect(int i)
//===================================================================
{
	if (i != -1){
		reExpandNode(i);
		select(i,true);
		fireSelection();
	}
}
//===================================================================
public boolean popupMenuEvent(MenuEvent me)
//===================================================================
{
	final TreeNode tn = targetNode;
	targetNode = null;
	if (tn == null) return true;
	paintLine(indexOf(tn));
	if (me.type == me.ABORTED) return true;
	if (me.type == me.SELECTED){
		MenuItem mi = (MenuItem)me.selectedItem;
		if (mi.equals("Paste")){
			final File got = ((DirectoryNode)tn).toFile();
			if (got == null) return true;
			if (!got.isDirectory()) return true;
			new ewe.sys.TaskObject(){
				public void doRun(){
					FileChooser.doPaste(got,chooser);
					reExpandNode(indexOf(tn));
				}
			}.startTask();
		}else if (mi.equals("Cut")){
			FileClipboard.clipboard.set(((DirectoryNode)tn.getParent()).toFile(),new String[]{((DirectoryNode)tn).getName()},true,this);
			cutNodes.add(tn);
			paintLine(indexOf(tn));
		}else if (mi.equals("Copy")){
			FileClipboard.clipboard.set(((DirectoryNode)tn.getParent()).toFile(),new String[]{((DirectoryNode)tn).getName()},false,this);
		}else if (mi.equals("Delete")){
			final TableControl tb = table;
			final Frame fr = table.getFrame();
			new ewe.sys.TaskObject(){
				public void doRun(){
					if (new MessageBox("Delete?","Delete the selected folder?",Form.MBYESNO).execute(fr,Gui.CENTER_FRAME) != Form.IDOK)
						return;
					File f = ((DirectoryNode)tn).toFile();
					if (!f.delete()){
						new MessageBox("Delete Failed","The Folder could not be deleted",Form.MBOK).execute(fr,Gui.CENTER_FRAME);
						return;
					}
					expandAndSelect(tn.getParent());
				}
			}.startTask();
		}else if (mi.equals("Rename")){
			final TableControl tb = table;
			final Frame fr = table.getFrame();
			new ewe.sys.TaskObject(){
				public void doRun(){
					DirectoryNode dn = (DirectoryNode)tn;
					String newName = new InputBox("Rename File").input(fr,dn.getName(),30);
					if (newName == null) return;
					if (newName.indexOf('/') != -1 || newName.indexOf('\\') != -1 || newName.indexOf(':') != -1){
						new MessageBox("Error","Bad file name",Form.MBOK).execute(fr,Gui.CENTER_FRAME);
						return;
					}
					File f = dn.toFile();
					int i = indexOf(tn);
					if (!f.rename(newName)){
						new MessageBox("Rename Failed","The Folder could not be renamed",Form.MBOK).execute(fr,Gui.CENTER_FRAME);
						return;
					}
					expandAndSelect(tn.getParent());
				}
			}.startTask();
		}
	}
	return true;
}
//===================================================================
public boolean fileClipboardOperation(FileClipboard clip,int op)
//===================================================================
{
	if (cutNodes.size() == 0) return true;
	DirectoryNode dn = (DirectoryNode)cutNodes.get(0);
	cutNodes.clear();
	if (op == FILES_REJECTED){
		update();
	}else{
		DirectoryNode parent = (DirectoryNode)dn.getParent();
		if (parent != null) {
			parent.removeChild(dn);
			expandAndSelect(parent);
		}
	}
	return true;
}
//===================================================================
public int getDrawOptions(int line,TreeNode node)
//===================================================================
{
	int ret = 0;
	if (node == targetNode) ret = IImage.OUTLINED;
	ret |= super.getDrawOptions(line,node);
	return ret;
}

//===================================================================
public Menu getMenuFor(int line)
//===================================================================
{
	if (chooser != null)
		if ((chooser.type & chooser.NO_WRITING) != 0) return null;
	if ((afile.getFlags() & afile.FLAG_FILE_SYSTEM_IS_READ_ONLY) != 0) return null;
	TreeNode node = getTreeNodeAt(line);
	if (node == null) return null;
	TreeNode p = node.getParent();
	if (p == null) return null;
	Menu m = new Menu();
	if (p.getParent() != null) {
		m.addItem("Rename");
		m.addItem("Delete");
	}
	if (p.getParent() != null && !isCut(node)) m.addItem("Cut");
	if (p.getParent() != null) m.addItem("Copy");
	if (FileClipboard.clipboard.canPasteInto(((DirectoryNode)node).toFile())) m.addItem("Paste");
	if (m.items.size() == 0) return null;
	targetNode = node;
	paintLine(line);
	return m;
}
//===================================================================
public boolean isCut(TreeNode node)
//===================================================================
{
	if (cutNodes.size() == 0) return false;
	if (cutNodes.find(node) != -1) return true;
	return false;
}
//-------------------------------------------------------------------
protected void expanding(final TreeNode node) throws ewe.sys.SlowTaskException
//-------------------------------------------------------------------
{
	ewe.sys.SlowTaskException e = ewe.sys.SlowTaskException.getNew();
	if (e == null) node.expand();
	else{
		if (new ewe.sys.TaskObject(e.getHandle()){
			protected void doRun(){
				node.expand();
				//ewe.sys.mThread.nap(5000);
			}
		}.startTask().waitOnFlags(ewe.sys.Handle.Stopped,new ewe.sys.TimeOut(250)))
			return;
		throw e;
	}
}

//##################################################################
}
//##################################################################

//##################################################################
class DirectoryNode extends LiveTreeNode{
//##################################################################

public String name;
public File afile = ewe.sys.Vm.newFileObject();

//===================================================================
public DirectoryNode(String name,File f)
//===================================================================
{
	this.name = name;
	afile = f;
}

//===================================================================
public void copyFrom(Object other)
//===================================================================
{
	if (other instanceof DirectoryNode){
		((DirectoryNode)other).name = name;
		((DirectoryNode)other).afile = afile;
	}
	super.copyFrom(other);
}
//===================================================================
public String getName()
//===================================================================
{
	if (name == null) {
		String ret = (String)afile.getInfo(afile.INFO_DEVICE_NAME,null,null,0);
		if (ret == null) ret = "My Computer";
		return ret;
}
	else return name;
}
//===================================================================
public File toFile()
//===================================================================
{
	if (name == null) return null;
	String nm = name;
	for (DirectoryNode dn = ((DirectoryNode)getParent()); dn != null; dn = ((DirectoryNode)dn.getParent())){
		if (dn.name == null) break;
		char c = dn.name.charAt(dn.name.length()-1);
		if (c == '\\' || c == '/' || c == ':')
			nm = dn.name+nm;
		else
			nm = dn.name+"/"+nm;
	}
	return afile.getNew(nm);
}

//===================================================================
public String [] list()
//===================================================================
{
	if (name == null) {
		String [] ret = (String[])afile.getInfo(File.INFO_ROOT_LIST,null,null,0);
		if (ret == null) ret = new String[]{"/"};
		return ret;
	}
	else return toFile().list(null,File.LIST_DIRECTORIES_ONLY);
}
//===================================================================
public boolean canExpand()
//===================================================================
{
	if (children != null)
		if (children.size() == 0) return false;
	if (((afile.getFlags() & afile.FLAG_SLOW_CHILD_COUNT) != 0) || false) return true;
	if (name == null) return true;
	int len = name.length();
	if (len != 0)
		if (name.charAt(len-1) == '\\' || name.charAt(len-1) == '/') return true;
	return toFile().list(null,File.LIST_CHECK_FOR_ANY_MATCHING_CHILDREN|File.LIST_DIRECTORIES_ONLY) != null;
}
//===================================================================
public boolean isLeaf() {return false;}
//===================================================================
public boolean expand()
//===================================================================
{
	if (children != null) return true;
	children = new Vector();
	String [] subs = list();
	if (subs != null)
		for (int i = 0; i<subs.length; i++){
			addChild(new DirectoryNode(subs[i],afile));
		}
	return true;
}
//===================================================================
public boolean collapse()
//===================================================================
{
	children = null;
	return true;
}

public String toString() {return getName();}

//===================================================================
public IImage getIcon()
//===================================================================
{
	String s = name;
	if (s != null){
		if (s.indexOf(':') != -1)
			return FileChooser.drive;
	}else{
		IImage got = (IImage)afile.getInfo(afile.INFO_DEVICE_ICON,null,null,0);
		if (got != null) return got;
		int opt = ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS);
		if ((opt & ewe.sys.Vm.VM_FLAG_IS_MOBILE) == 0)
			return FileChooser.computer;
		else if ((opt & ewe.sys.Vm.VM_FLAG_NO_KEYBOARD) != 0)
			return FileChooser.palm;
		else
			return FileChooser.handHeld;
	}
	return null;
}
//##################################################################
}
//##################################################################

