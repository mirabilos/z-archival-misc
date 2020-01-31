package ewex.registry;
import ewe.data.*;
import ewe.util.Vector;

//##################################################################
public class RegistryKeyNode extends LiveTreeNode{
//##################################################################

public RegistryKey key;

//===================================================================
public RegistryKeyNode(RegistryKey key)
//===================================================================
{
	this.key = key;
}

//===================================================================
public String getName() {return key.getName();}
//===================================================================
public String toString() {return getName();}
//===================================================================
public boolean canExpand() {return true;}
//===================================================================
public boolean expand()
//===================================================================
{
	children = new Vector();
	String [] all = (String [])key.getSubKeys(0);
	ewe.util.Utils.sort(all,ewe.sys.Vm.getLocale().getStringComparer(ewe.sys.Locale.IGNORE_CASE),false);
	for (int i = 0;i<all.length;i++){
		RegistryKey nk = key.getSubKey(all[i]);
		addChild(new RegistryKeyNode(nk));
	}
	return true;
}
//===================================================================
public boolean isLeaf() {return false;}
//===================================================================
public boolean collapse()
//===================================================================
{
	children = null;
	return true;
}

//##################################################################
}
//##################################################################

