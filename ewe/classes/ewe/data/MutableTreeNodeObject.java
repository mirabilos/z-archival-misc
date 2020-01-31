package ewe.data;
import ewe.util.Vector;
import ewe.util.Iterator;
import ewe.util.mString;
import ewe.util.ObjectIterator;

//##################################################################
public class MutableTreeNodeObject implements MutableTreeNode{
//##################################################################

/**
* The parent of this Node.
**/
protected TreeNode parent;
/**
* The children of this Node. This will be NULL until children are added.
**/
protected Vector children;

//===================================================================
public TreeNode getChild(int index)
//===================================================================
{
	if (children == null) return null;
	if (index >= children.size() || index < 0) return null;
	return (TreeNode)children.get(index);
}
//===================================================================
public Iterator getChildren()
//===================================================================
{
	if (children == null) return new ObjectIterator(null);
	else return children.iterator();
}
//===================================================================
public int getChildCount()
//===================================================================
{
	if (children == null) return 0;
	return children.size();
}
//===================================================================
public TreeNode getParent() {return parent;}
//===================================================================
public void setParent(TreeNode p) {parent = p;}
//===================================================================
public void removeChild(MutableTreeNode ch)
//===================================================================
{
	if (ch == null) return;
	if (children == null) return;
	int idx = children.find(ch);
	if (idx == -1) return;
	children.del(idx);
	ch.setParent(null);
	if (children.size() == 0) children = null;
}
//===================================================================
public void addChild(MutableTreeNode ch)
//===================================================================
{
	if (children == null) children = new Vector();
	insertChild(ch,children.size());
}
//===================================================================
public void insertChild(MutableTreeNode ch,int index)
//===================================================================
{
	if (ch == null) return;
	if (children == null) children = new Vector();
	if (children.find(ch) != -1) children.remove(ch);
	if (index < 0 || index > children.size()) index = children.size();
	children.insert(index,ch);
	ch.setParent(this);
}
//===================================================================
public int indexOfChild(TreeNode child)
//===================================================================
{
	if (children == null) return -1;
	return children.find(child);
}
public boolean canExpand() {return getChildCount() != 0;}
public boolean expand(){return true;}
public boolean isLeaf() {return getChildCount() == 0;}
public boolean collapse() {return true;}

//##################################################################
}
//##################################################################

