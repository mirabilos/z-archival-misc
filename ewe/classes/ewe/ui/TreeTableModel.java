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
import ewe.sys.Vm;
import ewe.data.*;
import ewe.util.*;
import ewe.reflect.Type;

//##################################################################
public class TreeTableModel extends TableModel{
//##################################################################

{
	hasRowHeaders = hasColumnHeaders = false;
	numRows = 0;
	numCols = 0;
	hasSpanningColumns = true;
}

/**
* This always returns false.
**/
//===================================================================
public boolean canScreenScroll()
//===================================================================
{
	return false;
}

protected long [] matrix = new long[0];
int rowHeight = 10;

protected TreeNode rootObject;

private boolean expandingCollapsing = false;

/**
* If this is true then no lines connecting nodes will be drawn.
**/
public boolean noLines = false;
/**
* If this is true then expanding/collapsing a node will automatically select that node. By default this is false.
**/
public boolean selectExpanded = false;
/**
* If this is true then the wait cursor will be shown when expanding node.
**/
public boolean showWaitCursor = false;
/**
* If this is true then the "canExpand" state of a node will be queried every time it is displayed.
**/
public boolean dynamicCanExpand = false;
/**
* This is used to indicate that nodes can have different sizes.
**/
public boolean hasControls = false;
/**
* This is the default number of levels to expand to when the tree is newly displayed. By default it is 1.
**/
public int expansionLevel = 1;
/**
* Set this true to hide the Root node.
**/
public boolean hideRoot = false;


static public IImage expandingIcon = ImageCache.cache.get("ewe/WaitSmall.bmp",Color.White);

/**
* This determines from the TreeNode whether it is a leaf and whether it can expand.
**/
//===================================================================
public byte toFlags(TreeNode what)
//===================================================================
{
	if (what != null){
		byte b = 0;
		if (what.isLeaf()) return b;
		else b |= IsNode;
		if (what.canExpand()) b |= CanExpand;
		return b;
	}
	else return 0;
}
/**
* Creates a reference for the TreeNode. By default this returns 0.
**/
//===================================================================
public long toReference(TreeNode what)
//===================================================================
{
	return 0;
}
//===================================================================
public void setRootObject(TreeNode root)
//===================================================================
{
	rootObject = root;
	init(0,toFlags(root));
}

//===================================================================
public TreeNode getRootObject() {return rootObject;}
//===================================================================

public int selectedLine = -1;
public RangeList selectedLines = new RangeList();
//public RangeList cutLines = new RangeList();
public Vector ranges = new Vector();
{
	ranges.add(selectedLines);
//	ranges.add(cutLines);
}
public Vector cutNodes = new Vector();
public Vector expandingNodes = new Vector();
/** A Node status. */
public static final int IsNode = 0x20;
/** A Node status. */
public static final int CanExpand = 0x40;
/** A Node status. */
public static final int HasMoreSiblings = 0x80;
/** A Node status. */
public static final int IsExpanded = 0x100;
/** A Node status. */
public static final int HasChildren = 0x200;

protected static final int FlagMask = IsNode|CanExpand|IsExpanded|HasMoreSiblings|HasChildren;
protected static final int DepthMask = 0x1f;
protected static final int PatternMask = ~(FlagMask|DepthMask);

public static IImage openFolder = ewe.io.File.getIcon(ewe.io.File.OpenFolderIcon);//new mImage("ewe/OpenFolder.bmp",Color.White);
public static IImage closedFolder = ewe.io.File.getIcon(ewe.io.File.ClosedFolderIcon);//new mImage("ewe/ClosedFolder.bmp",Color.White);
public static IImage page = ewe.io.File.getIcon(ewe.io.File.PageIcon);//new mImage("ewe/Page.bmp","ewe/PageMask.bmp");

//===================================================================
public TreeTableModel()
//===================================================================
{
}
/**
* This is called by the table to tell the model that it wants to scroll either
* vertically or horizontally. The method should return the correct position for
* the table to scroll to, given the current position and the requested position.
* By default it returns the requested position.
**/
//===================================================================
public int scrollTo(int current,int requested,int scrollAction,boolean horizontal)
//===================================================================
{
	if (hideRoot && requested == 1){
 		if (scrollAction == IScroll.TrackTo) return current;
 		if (current == 0) requested = 2;
		else requested = 0;
	}
	return super.scrollTo(current,requested,scrollAction,horizontal);
}

//-------------------------------------------------------------------
int countDepth()
//-------------------------------------------------------------------
{
	int len = matrix.length;
	int max = 0;

	for (int i = 0; i<len; i+=2){
		int dp = (int)matrix[i] & 0x1f;
		if (dp > max) max = dp;
	}
	return max;
}
//-------------------------------------------------------------------
protected void expandMatrix(int afterIndex,int length)
//-------------------------------------------------------------------
{
	int nl = matrix.length+(length*2);
	long [] nw = new long[nl];
	if (afterIndex >= 0) {
		Vm.copyArray(matrix,0,nw,0,(afterIndex+1)*2);
		nl -= (afterIndex+1)*2;
	}
	nl -= (length*2);
	if (afterIndex < (matrix.length/2)-1)
		Vm.copyArray(matrix,(afterIndex+1)*2,nw,(afterIndex+1+length)*2,nl);
	matrix = nw;
	if (selectedLine > afterIndex && selectedLine >= 0) selectedLine += length;
	for (int i = 0; i<ranges.size(); i++)
		((RangeList)ranges.get(i)).shift(afterIndex+1,length);
	/*
	for (int i = 0; i<selectedLines.length; i++){
		int idx = selectedLines.data[i];
		if (idx > afterIndex && idx >= 0)
			selectedLines.data[i] += length;
	}
	*/
}
/**
* Return the number of children for a node at the specified index.
**/
//===================================================================
public int countChildren(int parent)
//===================================================================
{
	if (parent < 0 || parent >= matrix.length/2) return 0;
	int flag = (int)matrix[parent*2];
	if ((flag & HasChildren) == 0) return 0;
	int children = 1;
	int child = parent+1;
	while((child = findNextSibling(child)) != -1) children++;
	return children;
}
/**
* Count all child nodes of a parent, including children of its children and so on.
**/
//===================================================================
public int countTotalChildNodes(int parent)
//===================================================================
{
	if (parent < 0 || parent >= matrix.length/2) return 0;
	int flag = (int)matrix[parent*2];
	if ((flag & HasChildren) == 0) return 0;
	int children = 0;
	for (int i = parent+1; i<matrix.length/2;i++){
		children++;
		int c = countTotalChildNodes(i);
		children += c;
		flag = (int)matrix[i*2];
		i+=c;
		if ((flag & HasMoreSiblings) == 0) break;
	}
	return children;
}
/**
* If you override this to react to a remove, then make sure you call this.
**/
//-------------------------------------------------------------------
protected int removeEntries(int start,int toRemove)
//-------------------------------------------------------------------
{
	int last = start+toRemove-1;

	if (hasControls)
		for (int i = start; i<start+toRemove; i++){
			TreeNode tn = getTreeNodeAt(i);
			if (tn instanceof TreeControl.ControlTreeNode)
				table.remove(((TreeControl.ControlTreeNode)tn).control);
		}
	int nl = matrix.length-(toRemove*2);
	long [] nw = new long[nl];
	Vm.copyArray(matrix,0,nw,0,start*2);
	nl -= start*2;
	Vm.copyArray(matrix,(start+toRemove)*2,nw,start*2,nl);
	matrix = nw;
	boolean removed = false;
	for (int i = 0; i<ranges.size(); i++)
		((RangeList)ranges.get(i)).removeAndShiftUp(start,start+toRemove-1);
	if (selectedLine >= start && selectedLine >= 0) {
		if (selectedLine <= last) {
			selectedLine = -1;
			removed = true;
		}else selectedLine -= toRemove;
	}
	if (removed) fireSelection();
	return toRemove;
}
//-------------------------------------------------------------------
protected int collapseMatrix(int parent)
//-------------------------------------------------------------------
{
	int toRemove = countTotalChildNodes(parent);
	if (toRemove == 0) return 0;
	return removeEntries(parent+1,toRemove);
}

//===================================================================
public void init(long rootObjectRef,byte flags)
//===================================================================
{
	matrix = new long[2];
	matrix[0] = flags & (IsNode|CanExpand);
	matrix[1] = rootObjectRef;
	numCols = 2;
	numRows = 1;
}

//-------------------------------------------------------------------
protected int shrunk(int byHowMany)
//-------------------------------------------------------------------
{
	numRows -= byHowMany;
	numCols = countDepth()+2;
	return byHowMany;
}
//-------------------------------------------------------------------
protected void modify(int start,int length,int on,int off)
//-------------------------------------------------------------------
{
	for (int i = 0; i<length; i++){
		matrix[(i+start)*2] &= ~off;
		matrix[(i+start)*2] |= on;
	}
}
/**
* Find the next sibling. Returns -1 if it cannot be found.
**/
//===================================================================
public int findNextSibling(int where)
//===================================================================
{
	int depth = (int)matrix[where*2]&0x1f;
	for (int i = where+1; i<numRows; i++){
		int dd = depth-(int)(matrix[i*2]&0x1f);
		if (dd == 0) return i;
		else if (dd > 0) return -1;
	}
	return -1;
}
/**
* Find the previous sibling. Returns -1 if it cannot be found.
**/
//===================================================================
public int findPreviousSibling(int where)
//===================================================================
{
	return findParentOrPreviousSibling(where,false);
}
/**
* Find the parent. Returns -1 if it cannot be found.
**/
//===================================================================
public int findParent(int where)
//===================================================================
{
	return findParentOrPreviousSibling(where,true);
}
/**
* Find the child within the parent. Returns -1 if it cannot be found.
**/
//===================================================================
public int findChild(int parent,int whichChild)
//===================================================================
{
	int cur = parent;
	if ((matrix[cur*2] & HasChildren) == 0) return -1; //This has no children displayed.
	cur++; //Now on the first child.
	for (int ch = 0; ch<whichChild; ch++){
		cur = findNextSibling(cur);
		if (cur == -1) return -1;
	}
	return cur;
}
/**
* Finds the index of the address specified. If the address is not displayed
* it will return -1.
**/
//===================================================================
public int indexOf(int [] address)
//===================================================================
{
	if (address == null) return -1;
	int cur = 0;
	for (int i = 0; i<address.length; i++)
		if ((cur = findChild(cur,address[i])) == -1) return -1;
	return cur;
}
/**
* Return the address of the TreeNode relative to the root TreeNode.
**/
//===================================================================
public int [] addressOf(TreeNode tn)
//===================================================================
{
	if (rootObject == null || tn == null) return null;
	return DataUtils.addressOfChild(rootObject,tn);
}
/**
* Returns the column which contains the '+'/'-' box of the entry at
* the following index. The column which contains the icon and text
* is the one after.
**/
//===================================================================
public int columnOf(int index)
//===================================================================
{
	if (index < 0 || index >= numRows) return -1;
	return (int)matrix[index*2] & 0x1f;
}
/**
* This will expand down the tree until the address specified is displayed. It
* will returns the index of the final item or -1 if it could not expand.
**/
//===================================================================
public int expandTo(int [] address)
//===================================================================
{
	if (address == null) return -1;
	int cur = 0;
	for (int i = 0; i<address.length; i++){
		int flags = (int)matrix[cur*2];
		if ((flags & IsExpanded) == 0)
			if ((flags & CanExpand) != 0)
				doExpand(cur);
			else
				return -1; //Can't expand parent.
		if ((matrix[cur*2] & IsExpanded) == 0) return -1; // Could not expand.
		if ((cur = findChild(cur,address[i])) == -1) return -1; // Could not locate child.
	}
	return cur;
}

//-------------------------------------------------------------------
private void expandDown(int who,int levels)
//-------------------------------------------------------------------
{
	if (who == -1 || levels == 0) return;
	doExpand(who);
	levels--;
	if (levels == 0) return;
	int num = countChildren(who);
	for (int i = 0; i<num; i++)
		expandDown(findChild(who,i),levels);
}
/**
 * This expands the tree a specified number of levels deep, below the root.
 * @param level The number of levels below the root to display.
 */
//===================================================================
public void expandToLevel(int level)
//===================================================================
{
	expandDown(0,level);
}
/**
* This will exapand down the tree until the address specified is displayed. It
* will return true if the expansion was successfull.
**/
//===================================================================
public int expandTo(int [] address,boolean selectIt)
//===================================================================
{
	int ret = expandTo(address);
	boolean repaint = false;
	if ((ret != -1) && selectIt){
		if (selectedLine != ret)
			select(ret);
		((TreeControl)table).scrollToVisible(ret);
	}
	update();
	return ret;
}
//-------------------------------------------------------------------
protected int findParentOrPreviousSibling(int where,boolean parent)
//-------------------------------------------------------------------
{
	int depth = (int)matrix[where*2] & 0x1f;
	for (int i = where-1; i>=0; i--){
		int dd = depth-(int)(matrix[i*2] & 0x1f);
		if (dd == 0) {if (!parent) return i;}
		else if (dd > 0)
			return parent ? i : -1;
	}
	return -1;
}
/**
* Collapse the index. It returns the number of items which have now been
* hidden.
**/
//===================================================================
public int collapse(int whichIndex)
//===================================================================
{
	if (whichIndex < 0 || whichIndex >= matrix.length*2) return 0;
	int flag = (int)matrix[whichIndex*2];
	if ((flag & IsExpanded) == 0) return 0;
	int did = collapseMatrix(whichIndex);
	flag &= ~ (IsExpanded|HasChildren);
	matrix[whichIndex*2] = flag;
	return shrunk(did);
}
/**
* Expand the index with the list of object references and the list
* of flags.
**/
//===================================================================
public int expand(int whichIndex,long [] with,byte [] flags)
//===================================================================
{
	//new Exception().printStackTrace();
	int n = with.length;
	//if (n == 0) return 0;
	int cur = whichIndex < 0 ? 0 : (int)matrix[whichIndex*2];
	if (whichIndex >= 0) {
		cur |= IsExpanded;
		if (with.length != 0) cur |= HasChildren;
		matrix[whichIndex*2] = cur;
	}
	int nd = (cur & 0x1f)+1;
	if (nd > 22) return 0; //Can't expand any further.
	int newVal = (cur & ~0x1f)|nd;
	if ((cur & HasMoreSiblings) != 0){
		newVal |= (1 << (8+(24-nd)));
	}
	expandMatrix(whichIndex,n);
	int idx = (whichIndex+1)*2;
	cur = newVal;
	for (int i = 0; i<n; i++){
		cur &= (~(IsExpanded|CanExpand|IsNode|HasChildren|HasMoreSiblings));
		if (flags != null) {
			cur |= flags[i] & (CanExpand|IsNode);
		}
		if (i <n-1) cur |= HasMoreSiblings;
		matrix[idx++] = cur;
		matrix[idx++] = with[i];
	}
	if ((nd+2) > numCols) numCols = (nd+2);
	numRows += with.length;
	return with.length;
}
/**
* Remove a set of entries. This will only work if the entries are contiguous
* along the same depth AND none have any children. Collapse any open items you
* want to delete before calling this.
*
* It returns the number of items removed.
**/
//===================================================================
public int delete(int line,int length)
//===================================================================
{
	if (length == 0) return 0;
	int depth = -1;
	int val = 0;
	for (int i = 0; i<length; i++){
		val = (int)matrix[(line+i)*2];
		int dp = val & 0x1f;
		if (depth == -1) depth = dp;
		else if (dp != depth) return 0; // Must all be the same depth.
		if ((val & HasChildren) != 0) return 0; // Must not have any children.
	}
	// At this point val will be the flags of the last entry in the set.
	if (line != 0){
		if ((val & HasMoreSiblings) == 0) { // Am removing the last one in the sibling list.
			int sib = findPreviousSibling(line);
			if (sib != -1){
				matrix[sib*2] &= ~HasMoreSiblings;
				modify(sib+1,(line-sib-1),0,1 << (8+(23-depth)));
			}else{
				int par = findParent(line);
				matrix[par*2] &= ~(HasChildren|IsExpanded);
			}
		}
	}
	return shrunk(removeEntries(line,length));
}
/**
* This will only insert IF there are already children. If not, then it will do an
* expand and ignore the asChild parameter.
**/
//===================================================================
public int insert(int parent,int asChild,long []what,byte []flags)
//===================================================================
{
	int n = what.length;
	if (n == 0) return 0;
	int pFlags = (int)matrix[parent*2];
	if ((pFlags & HasChildren) == 0) return expand(parent,what,flags);
	int pattern = (int)matrix[(parent+1)*2] & ~FlagMask;
	int num = countChildren(parent);
	if (asChild < 0) asChild = 0;
	if (asChild > num) asChild = num;
	int where = 0;
	if (asChild < num){
		where = findChild(parent,asChild);
		where--;
	}else{
		where = findChild(parent,num-1);
		matrix[where*2] |= HasMoreSiblings;
		int last = where;
		where += countTotalChildNodes(where);
		modify(last+1,where-last,1 << (8+(23-(pattern & 0x1f))),0);
	}
	expandMatrix(where,what.length);
	int idx = (where+1)*2;
	for (int i = 0; i<n; i++){
		int cur = pattern;
		if (flags != null) cur |= flags[i] & (CanExpand|IsNode);
		if (i<n-1 || asChild < num) cur |= HasMoreSiblings;
		matrix[idx++] = cur;
		matrix[idx++] = what[i];
	}
	numRows += n;
	return n;
}
/**
* Find the object reference number of the line.
**/
//===================================================================
public long objectAt(int line)
//===================================================================
{
	if (line < 0 || line*2 >= matrix.length) return -1;
	return matrix[line*2+1];
}
/**
* Returns the flags of the line.
**/
//===================================================================
public int flagsAt(int line)
//===================================================================
{
	if (line < 0 || line*2 >= matrix.length) return -1;
	return (int)matrix[line*2];
}
/**
* This changes the flags for the node on a line. You should only use the flags IsNode and CanExpand.
**/
//===================================================================
public void updateFlags(int line,byte newFlags)
//===================================================================
{
	if (line < 0 || line*2 >= matrix.length) return;
	matrix[line*2] &= ~(IsNode|CanExpand);
	matrix[line*2] |= (long)newFlags & (IsNode|CanExpand);
}
/**
* Find the address of the line. This gives a set of child numbers
* with the first one being the child of the root. Therefore an
* address of {1,2,4} means the object is the 5th child(index starts at zero)
* of the 3rd child of the 2nd child of the root.
*
* If the address is of zero length, then it IS the root.
*
* If the address is null, then it is not in the tree.
*
* Here is a good way to locate an object from the address.
*
* Object child = root;
* for (int i = 0; i<address.length; i++)
* child = child.getChild(address[i]);
* // At this point child is now the object at the specified address.
**/
//===================================================================
public int [] addressOf(int line)
//===================================================================
{
	if (line < 0 || line >= matrix.length/2) return null;
	int add = 0;
	int cd = (int)matrix[line*2] & 0x1f;
	int address [] = new int[cd];
	if (cd == 0) return address;
	for (int ln = line-1; ln >= 0; ln--){
		int flg = (int)matrix[ln*2];
		if ((flg & 0x1f) == cd) add++;
		else if ((flg & 0x1f) < cd) {
			cd--;
			address[cd] = add;
			if (cd == 0) return address;
			add = 0;
		}
	}
	return address;
}

String printAddress(int [] addr)
{
	if (addr == null) return "null";
	String out = "[";
	for (int i = 0; i<addr.length; i++) {
		if (i != 0) out += ",";
		out += addr[i];
	}
	out += "]";
	return out;
}

static int curObj = 1;

/**
* This is called by the default doExpand(). It will call getTreeNodeAt()
* and then call expanding() as a preparation to expand.
**/
//-------------------------------------------------------------------
protected TreeNode getNodeToExpand(final int whichIndex)
//-------------------------------------------------------------------
{
  if (expandingNodes.size() != 0) return null;
	final TreeNode node = getTreeNodeAt(whichIndex);
	if (node != null)
		if (expandingNodes.find(node) != -1) return null;
		try{
			expanding(node);
		}catch(ewe.sys.SlowTaskException e){
			//ewe.sys.Vm.debug("STE");
			//nal int prev = (table != null) ? table.modify(table.Disabled,0) : 0;
			expandingNodes.add(node);
			paintLine(whichIndex);
			e.getHandle().waitOnFlags(ewe.sys.Handle.Stopped,ewe.sys.TimeOut.Forever);
			expandingNodes.remove(node);
			update();
			/*
			e.getHandle().callWhenStopped(new ewe.sys.CallBack(){
				public void callBack(Object data){
					expandingNodes.remove(node);
					// (table != null) table.restore(prev,table.Disabled);
					doExpand(whichIndex,node);
					table.update(true);
				}
			});
			return null;
			*/
		}
	return node;
}
/**
* This is called by the default doExpand(). It will call getTreeNodeAt()
* and then call expanding() as a preparation to expand.
**/
//-------------------------------------------------------------------
protected TreeNode getNodeToCollapse(int whichIndex)
//-------------------------------------------------------------------
{
	TreeNode node = getTreeNodeAt(whichIndex);
	if (node != null) collapsing(node);
	return node;
}
/**
 * Returns if the item at the specified index can be expanded.
 */
//===================================================================
public boolean canDoExpand(int index)
//===================================================================
{
	if (index < 0 || index >= matrix.length*2) return false;
	int val = (int)matrix[index*2];
	return (((val & IsExpanded) == 0) && ((val & CanExpand) != 0));
}
/**
 * Returns if the item at the specified index can be collapsed.
 */
//===================================================================
public boolean canDoCollapse(int index)
//===================================================================
{
	if (index < 0 || index >= matrix.length*2) return false;
	int val = (int)matrix[index*2];
	return ((val & IsExpanded) != 0);
}
//-------------------------------------------------------------------
protected void expanding(final TreeNode node) throws ewe.sys.SlowTaskException
//-------------------------------------------------------------------
{
	node.expand();
}
protected void collapsing(TreeNode node){node.collapse();}
//-------------------------------------------------------------------

//===================================================================
public int doExpand(int whichIndex)
//===================================================================
{
	return doExpand(whichIndex,null);
}
/**
* Override this to do expansion.
*
* This explicitly expands the item at the specified index. It is as if
* the user pressed the '+' symbol of the node. It returns the number of
* lines added.
**/
//===================================================================
public int doExpand(int whichIndex,TreeNode node)
//===================================================================
{
	if (expandingCollapsing) return 0;
	if (!canDoExpand(whichIndex)) return 0;
	if (true) {
		int old = 0;
		//if (showWaitCursor) ewe.sys.Vm.showWait(true);
		try{
		if (node == null) node = getNodeToExpand(whichIndex);
		if (node == null) return 0;
		int n = node.getChildCount();
		//if (n == 0) return 0;
		long [] refs = new long[n];
		byte [] flags = new byte[n];
		Iterator it = node.getChildren();
		for (int i = 0; i<n && it.hasNext(); i++){
			TreeNode tn = (TreeNode)it.next();
			refs[i] = toReference(tn);
			flags[i] = toFlags(tn);
		}
		int ret = 0;
		if (n == 0){
			if (!node.canExpand()) matrix[whichIndex*2] &= ~(CanExpand|IsExpanded);
		}else
			ret = expand(whichIndex,refs,flags);
		  return ret;
		}finally{
		  //if (showWaitCursor) ewe.sys.Vm.showWait(false);
		}
	}else{
		long [] ni = new long[3];
		ni[0] = curObj++;
		ni[1] = curObj++;
		ni[2] = curObj++;
		byte [] by = new byte[3];
		by[0] = by[1] = by[2] = (byte)(IsNode|CanExpand);
		return expand(whichIndex,ni,by);
	}
}
//===================================================================
public TreeNode getTreeNodeAt(int index)
//===================================================================
{
	if (rootObject == null) return null;
	return DataUtils.getChildAt(rootObject,addressOf(index));
}
/**
* Return the line of the specified node IF it is displayed. If it is not
* displayed it will return -1.
**/
//===================================================================
public int indexOf(TreeNode node)
//===================================================================
{
	return indexOf(addressOf(node));
}
//===================================================================
public int doCollapse(int whichIndex)
//===================================================================
{
	TreeNode node = getNodeToCollapse(whichIndex);
	//if (node == null) return 0;
	return collapse(whichIndex);
}

private static Type liveData;
private static Object[] none;

//===================================================================
public String getDisplayString(int forLine,TreeNode node)
//===================================================================
{
	if (node == null) node = getTreeNodeAt(forLine);
	if (liveData == null) {
		liveData = new Type("ewe.data.LiveData");
		none = new Object[0];
	}
	if (liveData.isInstance(node)){
		String s = (String)liveData.invoke(node,"getName()Ljava/lang/String;",none);
		return s+(expandingNodes.find(node) != -1 ? "(Expanding...)" : "");
	}
	return ""+matrix[forLine*2+1];
}
//===================================================================
public IImage getIcon(int forLine,TreeNode node)
//===================================================================
{
	if (node == null) node = getTreeNodeAt(forLine);
	if (liveData == null) {
		liveData = new Type("ewe.data.LiveData");
		none = new Object[0];
	}
	if (liveData.isInstance(node)){
		return (IImage)liveData.invoke(node,"getIcon()Lewe/fx/IImage;",none);
	}
	return null;
}



/**
* This assumes you are using TreeNode objects with the control. It will delete a single
* TreeNode as a child of another node ON THE DISPLAY. You must have already deleted it from
* the object tree. If the parent node was not expanded it will have no effect.
**/
//===================================================================
public boolean deleted(TreeNode parent,int previousIndexOfDeletedChild)
//===================================================================
{
	int line = indexOf(parent);
	if (line == -1) return true;
	updateFlags(line,toFlags(parent));
	int chld = findChild(line,previousIndexOfDeletedChild);
	if (chld == -1) return true;
	doCollapse(chld);
	delete(chld,1);
	return true;
}
/**
* This assumes you are using TreeNode objects with the control. It will insert a single
* TreeNode as a child of another node ON THE DISPLAY. You must have already inserted it into
* the object tree. If the parent node was not expanded it will simply expand the parent node
* and thereby display the new node. If the parent node is not displayed at all it will have no
* effect. It will not update the display, call update() after to do so.
**/
//===================================================================
public boolean inserted(TreeNode parent,TreeNode child,boolean selectChild)
//===================================================================
{
	int line = indexOf(parent);
	int id = parent.indexOfChild(child);
	if (line == -1) return false;
	if (id == -1) return false;
	updateFlags(line,toFlags(parent));
	if ((getDisplayStatus(line) & IsExpanded) == 0){
		doExpand(line);
	}else{
		long [] ref = new long[1];
		byte [] flag = new byte[1];
		ref[0] = toReference(child);
		flag[0] = toFlags(child);
		insert(line,id,ref,flag);
	}
	if (selectChild){
		int idx = indexOf(child);
		if (idx != -1){
			select(idx);
			if (table != null) ((TreeControl)table).scrollToVisible(idx);
		}
	}
	return true;
}

/**
 * Get the display status for the specified line. This will
 * Additional verbose
 * @param forLine
 * @return
 */
//===================================================================
public int getDisplayStatus(int forLine)
//===================================================================
{
	return flagsAt(forLine);
}
//===================================================================
public byte getFlags(int forLine,TreeNode node)
//===================================================================
{
	return (byte)(flagsAt(forLine) & ~0x1f);
}
//===================================================================
public int calculateRowHeight(int row)
//===================================================================
{
	if (row == -1) return 0;
	if (row == 0 && hideRoot) return 0;
	if (!hasControls) return rowHeight;
	return Math.max(rowHeight,getHeightOfObject(getDataObjectAt(row)));
}
//===================================================================
public int getHeightOfObject(Object obj)
//===================================================================
{
	if (obj == null) return 0;
	if (obj instanceof Control) return ((Control)obj).getDim(null).height;
	else if (obj instanceof TreeControl.ControlTreeNode)
		return getHeightOfObject(((TreeControl.ControlTreeNode)obj).control);
	else
		return 0;
}
//===================================================================
public int calculateColWidth(int col)
//===================================================================
{
	if (col == -1) return 0;
	if (col == numCols-1) return table.width;
	if (col == 0 && hideRoot) return 0;
	else return 17;
}

static Dimension nodeDim = new Dimension();
static Rect nodeRect = new Rect();

//===================================================================
public Rect getTextRect(int index,Rect dest)
//===================================================================
{
	if (index < 0 || index >= matrix.length/2) return null;
	Rect r = Rect.unNull(dest);
	int h = getRowHeight(0);
	r.x = ((int)(matrix[index*2]&0x1f)+1-table.firstCol)*getColWidth(0)+16+4;
	r.y = (index-table.firstRow)*h+2;
	r.height = h-4;
	r.width = table.width-r.x-16;
	return r;
}
public Color selectedColor = new Color(0,0,0xff);

//===================================================================
public boolean isCut(TreeNode node)
//===================================================================
{
	if (cutNodes.size() == 0) return false;
	for (TreeNode n = node; n != null; n = n.getParent())
		if (cutNodes.find(n) != -1) return true;
	return false;
}
//-------------------------------------------------------------------
protected int getDrawOptions(int line,TreeNode node)
//-------------------------------------------------------------------
{
	return isCut(node) ? IImage.DISABLED : 0;
}
//===================================================================
public int getDataWidth(int line)
//===================================================================
{
	TreeNode node = getTreeNodeAt(line);
	String txt = getDisplayString(line,node);
	return table.getFontMetrics().getTextWidth(txt)+18;
}
//===================================================================
public void drawNodeData(FontMetrics fm,Graphics g,int line,Rect where,Rect whereInTable,TreeNode node)
//===================================================================
{
	if (line == 0 && hideRoot) return;
	int options = (int)matrix[line*2];
	if (node == null) node = getTreeNodeAt(line);
	if (node instanceof TreeControl.ControlTreeNode){
		Control c = ((TreeControl.ControlTreeNode)node).control;
		table.add(c);
		Rect r = where;
		c.setLocation(whereInTable.x,whereInTable.y);
		//c.setRect(x+gap,y+gap,r.width-gap*2,r.height-gap*2);
		g.translate(r.x,r.y);
		c.repaintNow(g,null);
		g.translate(-r.x,-r.y);
		return;
	}
	IImage image = expandingNodes.find(node) != -1 ? expandingIcon : getIcon(line,node);
	Color bk = table.getBackground();
	if (image == null){
		if ((options & IsNode) != 0)
			if ((options & IsExpanded) != 0)
				image = openFolder;
			else
				image = closedFolder;
		else
			image = page;
	}
	g.setColor(bk);
	g.fillRect(where.x,where.y,table.width,where.height);
	nodeRect.set(where.x,where.y+(where.height-16)/2,16,16);
	int ops = getDrawOptions(line,node);
	image.draw(g,nodeRect.x,nodeRect.y,ops & ~IImage.OUTLINED);
	String text = getDisplayString(line,node);
	g.getSize(fm,text,nodeDim);
	nodeRect.set(where);
	nodeRect.x += image.getWidth()+2;
	nodeRect.width = nodeDim.width+4;
	nodeRect.height = nodeDim.height+4;
	if (isSelected(line) && ((ops & IImage.DISABLED) == 0)){
		g.setColor(selectedColor);
		g.fillRect(nodeRect.x,nodeRect.y,nodeRect.width,nodeRect.height);
		g.setColor(Color.Black);
		g.drawRect(nodeRect.x,nodeRect.y,nodeRect.width,nodeRect.height);
		//g.draw3DRect(nodeRect,table.BDR_OUTLINE|table.BF_RECT,false,selectedColor,Color.Black);
		g.setColor(Color.White);
	}else{
		g.setColor(bk);
		g.fillRect(nodeRect.x,nodeRect.y,nodeRect.width,nodeRect.height);
		g.setColor(Color.Black);
		if ((ops & IImage.OUTLINED) != 0) g.drawRect(nodeRect.x,nodeRect.y,nodeRect.width,nodeRect.height);
		//g.draw3DRect(nodeRect,table.BDR_NOBORDER,false,table.getBackground(),Color.Black);
		g.setColor(((ops & IImage.DISABLED) == 0) ? Color.Black : Color.DarkGray);
	}
	nodeRect.x += 2;
	nodeRect.y += 2;
	g.setFont(fm.getFont());
	g.drawText(text,nodeRect.x,nodeRect.y);
}

//===================================================================
public void drawNodeLine(Graphics g,Rect r,boolean nextTo,int flags,int row,TreeNode node)
//===================================================================
{
	boolean noLine = false;
	if (hideRoot) {
		if (row == 0) return;
		if (findParent(row) == 0) noLine = true;
	}
	g.setColor(table.getBackground());
	g.fillRect(r.x,r.y,r.width,r.height);
	Color line = Color.DarkGray;
	g.setColor(line);
	int midx = r.x+r.width/2;
	int midy = r.y+r.height/2;
	if (nextTo){
		if (!noLines){
			if (row != 0 && !noLine) g.drawLine(midx,r.y,midx,midy);
			if (((flags & HasMoreSiblings) != 0) && !noLine) g.drawLine(midx,midy,midx,r.y+r.height);
			g.drawLine(midx,midy,r.x+r.width,midy);
		}
		if ((flags & CanExpand) != 0){
			g.setColor(Color.White);
			g.fillRect(midx-4,midy-4,9,9);
			g.setColor(Color.Black);
			g.drawRect(midx-4,midy-4,9,9);
			g.drawLine(midx-2,midy,midx+2,midy);
			if ((flags & IsExpanded) == 0) g.drawLine(midx,midy-2,midx,midy+2);
		}
	}else{
		if (!noLines) g.drawLine(midx,r.y,midx,r.y+r.height);
	}
}
//===================================================================
public void paintTableCell(TableControl tc,Graphics g,int row,int col) {deferPaintTableCell(tc,g,row,col);}
//===================================================================

//-------------------------------------------------------------------
private int getFlagsForDisplay(int line)
//-------------------------------------------------------------------
{
	int val = (int)matrix[line*2];
	if (!dynamicCanExpand) return val;
	int flg = getFlags(line,null);
	val = (val & ~(CanExpand|IsNode))| (flg & (CanExpand|IsNode));
	matrix[line*2] = val;
	return val;
}
//===================================================================
public void paintLine(int line)
//===================================================================
{
	if (table == null) return;
	Graphics g = table.getGraphics();
	if (g == null) return;
	paintLine(table,g,line);
	g.free();
}
//===================================================================
public void paintLine(TableControl tc,Graphics g,int line)
//===================================================================
{
	if (line == 0 && hideRoot) return;
	if (line < tc.firstRow || line >= numRows) return;
	if (g == null) return;
	rect.y = 0;
	for (int r = tc.firstRow; r<numRows && r<line; r++){
		rect.y += getRowHeight(r);
		if (rect.y >= tc.height) return;
	}
	rect.x = 0;
	rect.width = 17;
	rect.height = getRowHeight(line);
	int c, r = line;
	int val = getFlagsForDisplay(line);
	int dp = val & 0x1f;

	BufferedGraphics bg = new BufferedGraphics(g,new Rect().set(0,rect.y,tc.width,rect.height));
	g = bg.getGraphics();
	g.setColor(table.getBackground());
	g.fillRect(0,rect.y,tc.width,rect.height);
	g.setColor(Color.Black);
	FontMetrics fm = tc.getFontMetrics();
	g.setFont(fm.getFont());

	try{
		for (c = tc.firstCol; c<numCols; c++){
			String toDraw = null;
			if (c == dp+1){
				drawNodeData(fm,g,line,rect,rect,null);
			}else if (c == dp){
				drawNodeLine(g,rect,true,val,line,null);
				/*
				toDraw = (val & HasMoreSiblings) != 0 ? "|" : "'";
				if ((val & CanExpand) == 0) toDraw += "-";
				else if ((val & IsExpanded) != 0) toDraw += "=";
				else toDraw += "+";
				*/
			}else{
				int mask = 1 << (8+(24-c-1));
				if ((val & mask) != 0)
					if (!hideRoot || c != 1)
						drawNodeLine(g,rect,false,val,line,null);
			}
			if (toDraw != null) g.drawText(toDraw,rect.x,rect.y);
			rect.x += getColWidth(c);
		}
	}finally{
		g = bg.release();
	}
}
private Rect sr = new Rect(), cur = new Rect();
//===================================================================
public void paintTableCell(TableControl tc,Graphics g,Rect cells)
//===================================================================
{
	if (g == null) return;
	findCellsInArea(cells,false);
	tc.getScreenRect(cells.y,cells.x,sr);
	int lr = cells.y+cells.height;
	int lc = cells.x+cells.width;
	int x = 0, y = 0;
	FontMetrics fm = tc.getFontMetrics();
	g.setFont(fm.getFont());
	TreeNode node = getTreeNodeAt(cells.y);
	int depth = (int)matrix[cells.y*2] & 0x1f;
	boolean first = true;
	cur.y = sr.y;
	for (int r = cells.y; r<lr; r++){
		if (!first && node != null){
			int d2 = (int)matrix[r*2] & 0x1f;
			if (d2 > depth) {
				TreeNode par = node;
				node = node.getChild(0);
				//if (node == null) ewe.sys.Vm.debug("Node: "+par.toString()+" child(0) is null!");
			}else {
				TreeNode par = node;
				node = DataUtils.getSibling(DataUtils.getParent(node,depth-d2),+1);
				//if (node == null) ewe.sys.Vm.debug("Node: "+par.toString()+" sibling is null!");
			}
			depth = d2;
		}else first = false;
		cur.x = sr.x;
		x = 0;
		int ch = getRowHeight(r);
		if (r == -1) continue;
		if (r >= numRows) return;
		int val = getFlagsForDisplay(r);//(int)matrix[r*2];
		for (int c = cells.x; c<lc; c++){
			if (c == -1) continue;
			if (c > (val&0x1f)+1) continue; //Past depth of this row.
			int cw = getColWidth(c);
			rect.x = x; rect.y = y;
			rect.width = cw; rect.height = ch;
			String toDraw = null;
			g.setColor(Color.Black);
			if (c == (val & 0x1f)+1){
				drawNodeData(fm,g,r,rect,cur,node);
			}else if (c == (val & 0x1f)){
				drawNodeLine(g,rect,true,val,r,node);
				/*
				toDraw = (val & HasMoreSiblings) != 0 ? "|" : "'";
				if ((val & CanExpand) == 0) toDraw += "-";
				else if ((val & IsExpanded) != 0) toDraw += "=";
				else toDraw += "+";
				*/
			}else{
				int mask = 1 << (8+(24-c-1));
				if ((val & mask) != 0)
					if (!hideRoot || c != 1)
						drawNodeLine(g,rect,false,val,r,node);
			}
			if (toDraw != null) g.drawText(toDraw,rect.x,rect.y);
			x += cw;
			cur.x += cw;
		}
		y += ch;
		cur.y += ch;
	}
}

/**
* This will either expand or collapse a node depending on its current state. It will
* also auto-select the item if selectExpanded is true.
**/
//===================================================================
public void fullExpandCollapse(int line)
//===================================================================
{
	int val = (int)matrix[line*2];
	if ((val & CanExpand) == 0) return;
	if ((val & IsExpanded) == 0){
		doExpand(line);
	}else{
		doCollapse(line);
	}
	expandingCollapsing = true;
	update();
	if (selectExpanded) {
		if (line != selectedLine){
			select(line);
			fireSelection();
		}
	}
	expandingCollapsing = false;
}
Point pressedOn = null;

/**
* This returns getTreeNodeAt(line).
**/
//===================================================================
public Object getDataObjectAt(int line)
//===================================================================
{
	return getTreeNodeAt(line);
}
//===================================================================
public Object getDataToTransfer()
//===================================================================
{
	int [] all = getSelectedLines();
	if (all.length == 0) return null;
	Object [] got = new Object[all.length];
	for (int i = 0; i<all.length; i++)
		got[i] = getDataObjectAt(all[i]);
	return got;
}
//===================================================================
public boolean getDataToDragAndDrop(DragContext dc)
//===================================================================
{
	dc.dataToDrag = getDataToTransfer();
	if (dc.dataToDrag == null) return false;
	dc.isMultiple = countSelectedLines() > 1;
	return true;
}
//===================================================================
public void clicked(Point where)
//===================================================================
{
	released(where);
}

/**
 * Select an item - this does not fire a selection event.
 * @param line the index of the line to select.
 */
//===================================================================
public void selectOnly(int line)
//===================================================================
{
	int num = selectedLines.countItems();
	if (num == 1){
		if (isSelected(line)) return;
		select(selectedLines.getItemAtIndex(0),false);
	}
	if (num > 1) selectAll(false);
	if (line >= 0) select(line,true);
	if (num > 1) update();
}
//===================================================================
public void released(Point where)
//===================================================================
{
	boolean ctrl = (table.currentPenEvent.modifiers & IKeys.CONTROL) != 0 && table.multiSelect;
	if (table.penSelectMode && table.multiSelect) ctrl = true;
	if (pressedOn == null) return;
	if (!pressedOn.equals(where)) return;
	if (isSelected(where.y)){
		if (!ctrl && table.multiSelect){
			if (selectedLines.countItems() == 1)
				select(selectedLines.getItemAtIndex(0),false);
			else{
				selectAll(false);
				update();
			}
		}else if (table.multiSelect)
			select(where.y,false);
		fireSelection();
	}
}
//===================================================================
public void pressed(Point where)
//===================================================================
{
	boolean shifted = (table.currentPenEvent.modifiers & IKeys.SHIFT) != 0 && table.multiSelect;
	boolean ctrl = (table.currentPenEvent.modifiers & IKeys.CONTROL) != 0 && table.multiSelect;
	if (table.penSelectMode && table.multiSelect) ctrl = true;
	pressedOn = null;
	if (where == null) return;
	if (where.y < 0 || where.y >= numRows) return;
	table.cursorTo(where.y,1,false);
	int val = (int)matrix[where.y*2];
	if (where.x == (val & 0x1f) && ((val & CanExpand) != 0)) {
		fullExpandCollapse(where.y);
	}else if (where.x >= (val & 0x1f)+1){
		if (!isSelected(where.y)){
			if (shifted && selectedLine != -1){
				int dy = where.y < selectedLine ? +1 : -1;
				int last = selectedLine;
				for (int y = where.y; y != last; y+=dy)
					select(y,true);
			}else{
				if (!ctrl && table.multiSelect){
					if (selectedLines.countItems() == 1)
						select(selectedLines.getItemAtIndex(0),false);
					else{
						selectAll(false);
						update();
					}
				}
				select(where.y,true);
			}
			fireSelection();
		}else{
			pressedOn = where;
		}
	}
}
//===================================================================
public void doubleClicked(Point where)
//===================================================================
{
	if (where == null || table.penSelectMode) return;
	if (where.y < 0 || where.y >= numRows) return;
	int val = (int)matrix[where.y*2];
	if ((where.x > (val & 0x1f)))
		if (isSelected(where.y)) table.notifyAction();
}
//===================================================================
public void fireSelection()
//===================================================================
{
	table.postEvent(new TreeEvent(TreeEvent.NODE_SELECTED,table,selectedLine));
}
//==================================================================
public void select(int index,boolean selected)
//==================================================================
{
	boolean multi = table == null ? false : table.multiSelect;
	if (selected && !multi) select(selectedLine,false);
	selectedLines.removeRange(index,index);
	if (selected) {
		selectedLines.addRange(index,index);
		selectedLine = index;
	}else if (selectedLine == index) selectedLine = -1;
	if (index != -1 && table != null)
		paintLine(index);
		//paintLine(table,table.getGraphics(),index);
}
//===================================================================
public int countSelectedLines()
//===================================================================
{
	return selectedLines.countItems();
}

//===================================================================
public RangeList getSelectedRanges()
//===================================================================
{
	return (RangeList)selectedLines.getFullCopy();
}
//==================================================================
public int [] getSelectedLines()
//==================================================================
{
	return selectedLines.toIntArray();
}
//==================================================================
public boolean isSelected(int index)
//==================================================================
{
	return selectedLines.inRange(index,index);//indexOf(index) != -1;
}
/**
* This either clears the selection or selects all. It will not do a repaint.
**/
//===================================================================
public void selectAll(boolean selectOn)
//===================================================================
{
	selectedLines.clear();
	if (selectOn){
		int t = matrix.length/2;
		selectedLines.addRange(0,t-1);
		selectedLine = t-1;
	}else
		selectedLine = -1;
}
//===================================================================
public void setSelectedLines(int [] all)
//===================================================================
{
	selectedLines.clear();
	if (all == null) return;
	//int max = getItemCount();
	for (int i = 0; i<all.length; i++){
		int idx = all[i];
		//if (idx < 0 || idx >= max) continue;
		selectedLines.addRange(idx,idx);
	}
	update();
}
/**
* Selects the specified line.
**/
//===================================================================
public void select(int line)
//===================================================================
{
	int oldSelected = selectedLine;
	selectedLines.clear();
	select(line,true);
	selectedLine = line;
	if (oldSelected == selectedLine) return;
	if (oldSelected != -1)
		paintLine(table,table.getGraphics(),oldSelected);
	if (selectedLine != -1)
		paintLine(table,table.getGraphics(),selectedLine);
	int [] add = addressOf(selectedLine);
	//System.out.println(""+selectedLine+" = "+printAddress(add)+" = "+indexOf(add));
}
//===================================================================
public void update() {table.update(true);}
//===================================================================
public void made()
//===================================================================
{
	Graphics.getSize(table.getFontMetrics(),"Xy",nodeDim);
	rowHeight = Math.max(nodeDim.height+4,18);
	expandToLevel(expansionLevel);
}
/**
* This will cause the selected Node to be collapsed and then expanded again.
**/
//===================================================================
public void reExpandSelectedNode() {reExpandNode(selectedLine);}
//===================================================================
/**
* This will cause the selected Node to be collapsed and then expanded again.
**/
//===================================================================
public void reExpandNode(int index)
//===================================================================
{
	if (index == -1) return;
	int val = (int)matrix[index*2];
	TreeNode node = getTreeNodeAt(index);
	if (node != null){

		node.collapse();
		if (node.canExpand()) matrix[index*2] |= CanExpand;
		else matrix[index*2] &= ~CanExpand;
	}
	if ((val & IsExpanded) != 0) doCollapse(index);
	doExpand(index);
	update();
}
//===================================================================
public Menu getMenuFor(int line)
//===================================================================
{
	return null;
}
//##################################################################
}
//##################################################################

