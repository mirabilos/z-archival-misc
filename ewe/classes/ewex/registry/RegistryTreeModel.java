package ewex.registry;
import ewe.sys.Device;

//##################################################################
public class RegistryTreeModel extends ewe.ui.TreeModelAdapter{
//##################################################################

public boolean remote = false;
{
	dynamicCanExpand = true;
}
//-------------------------------------------------------------------
protected ewe.fx.IImage getIcon(Object parentNode,int childIndex)
//-------------------------------------------------------------------
{
	if (parentNode == null)
		return !remote ? Device.computer : Device.palm;
	else
		return super.getIcon(parentNode,childIndex);
}
//-------------------------------------------------------------------
protected String getDisplayString(Object parentNode,int childIndex)
//-------------------------------------------------------------------
{
	if (parentNode == null) {
		return remote ? "Mobile Device" : "My Computer";
	}else if (parentNode == this){
		return Registry.roots[childIndex];
	}else if (parentNode instanceof RegistryKey){
		return ((RegistryKey)parentNode).getSubKey(childIndex);
	}else
		return "Child: "+childIndex;
}
//-------------------------------------------------------------------
protected byte getFlags(Object parentNode,int childIndex,byte savedFlags)
//-------------------------------------------------------------------
{
	Object created = createObjectFor(parentNode,childIndex);
	if (created instanceof RegistryKey){
		int num = ((RegistryKey)created).getSubKeyCount();
		if (num != 0) return (byte)(CanExpand|IsNode);
		else return (byte)IsNode;
	}
	return savedFlags;
}

protected long [] getChildIndexes(Object parent)
{
	if (parent instanceof RegistryKey){
		return (long [])((RegistryKey)parent).getSubKeys(RegistryKey.GET_INDEXES_AS_LONGS);
	}else{
		return new long[]{1L,2L,3L,4L,5L,6L};
	}
}
protected int getChildCount(Object parent)
{
	if (parent instanceof RegistryKey){
		return ((RegistryKey)parent).getSubKeyCount();
	}
	return 0;
}
//-------------------------------------------------------------------
protected Object createObjectFor(Object parent,int child)
//-------------------------------------------------------------------
{
	if (parent == null) return this;
	else if (parent == this) {
		RegistryKey rk = remote ? Registry.getRemoteKey(child,"",false,false):Registry.getLocalKey(child,"",false,false);
		return rk;
	}else {
		RegistryKey rk = (RegistryKey)parent;
		rk = rk.getSubKey(rk.getSubKey(child));
		return rk;
	}
}

public RegistryKey getKeyAt(int whichLine)
{
	Object created = createObjectFor(getParentObject(whichLine),getIndexFor(whichLine));
	if (created instanceof RegistryKey) return (RegistryKey)created;
	return null;
}
//##################################################################
}
//##################################################################

