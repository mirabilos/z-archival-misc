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
package ewe.data;
import ewe.util.*;

//##################################################################
public class MultiListSelect extends PropertyList {//implements Textable{
//##################################################################

/**
* It is not necessary for this value to be set. It can be null.
**/
public AvailableLists availableLists = null;
public boolean singleItemPerList = false;
/**
* If this is true then the list will default to adding an empty selection for a list
* if there is no entry for that list. By default it is false.
**/
public boolean createIfNotPresent = false;

public String _fields = "availableLists,singleItemPerList,createIfNotPresent";

//-------------------------------------------------------------------
protected Property getOrCreateEntry(String listName,boolean create)
//-------------------------------------------------------------------
{
	Property p = get(listName);
	if (p == null && create) {
		p = new Property(listName,null);
		if (!singleItemPerList) p.value = new SelectedItems();
		add(p);
	}
	return p;
}
//===================================================================
public Vector getSelectedFor(String listName) {return getSelectedFor(listName,createIfNotPresent);}
//===================================================================
public Vector getSelectedFor(String listName,boolean createAndAdd)
//===================================================================
{
	Property p = getOrCreateEntry(listName,createAndAdd);
	if (p == null) return new SelectedItems();
	if (p.value instanceof SelectedItems) return (Vector)p.value;
	Vector v = new SelectedItems();
	v.add(p.value);
	return v;
}
//===================================================================
public int [] getSelectedIndexesFor(String listName)
//===================================================================
{
	if (availableLists == null) return null;
	Vector which = availableLists.getList(listName);
	if (which == null) return null;
	Property p = getOrCreateEntry(listName,false);
	if (p == null) return new int[0];
	Vector sel = null;
	if (p.value instanceof SelectedItems) {
		sel = (Vector)p.value;
	}else{
		sel = new SelectedItems();
		sel.add(p.value);
	}
	int [] ret = new int[sel.size()];
	for (int i = 0; i<sel.size(); i++) ret[i] = which.find(sel.get(i));
	return ret;
}
//===================================================================
public boolean isSelected(String listName,Object value)
//===================================================================
{
	if (value == null) return false;
	Property p = getOrCreateEntry(listName,true);
	if (p == null) return false;
	if (singleItemPerList)
		return value.equals(p.value);
	Vector v = (Vector)p.value;
	if (v == null) return false;
	for (int i = 0; i<v.size(); i++)
		if (value.equals(v.get(i))) return true;
	return false;
}
//===================================================================
public void select(String listName,int index,boolean doSet)
//===================================================================
{
	if (availableLists == null) return;
	Vector which = availableLists.getList(listName);
	if (which == null) return;
	if (index < 0 || index >= which.size()) return;
	select(listName,which.get(index),doSet);
}
//===================================================================
public void select(String listName,Object value,boolean doSet)
//===================================================================
{
	Property p = getOrCreateEntry(listName,true);
	if (doSet){
		if (isSelected(listName,value)) return;
		else {
			if (singleItemPerList) p.value = value;
			else {
				Vector v2 = (Vector)p.value;
				v2.add(value);
			}
		}
	}else {
		if (singleItemPerList) {
			if (p.value == value) p.value = null;
		}else {
			Vector v2 = (Vector)p.value;
			v2.remove(value);
		}
	}
}
//===================================================================
public void setSelectedFor(String listName,Vector v)
//===================================================================
{
	Property p = getOrCreateEntry(listName,true);
	if (v == null) v = new Vector();
	if (singleItemPerList) {
		p.value = v.size() == 0 ? null : v.get(0);
	}else {
		Vector v2 = (Vector)p.value;
		v2.clear();
		v2.addAll(v);
	}
}

//===================================================================
public void selectAll(String listName) {selectAll(listName,availableLists);}
//===================================================================

//===================================================================
public void selectAll(String listName,AvailableLists all)
//===================================================================
{
	if (all != null)
		setSelectedFor(listName,all.getList(listName));
}
//===================================================================
public void selectNone(String listName)
//===================================================================
{
	setSelectedFor(listName,null);
}
/**
Create a Vector that holds the data at the specified indexes from the sourceData Vector.
**/
//===================================================================
public static Vector toVector(Vector sourceData, int[] indexes)
//===================================================================
{
	Vector r = new Vector();
	int ss = sourceData.size();
	if (indexes == null) return r;
	for (int i = 0; i<indexes.length; i++){
		int ii = indexes[i];
		if (ii < 0 || ii >= ss) continue;
		r.add(sourceData.get(ii));
	}
	return r;
}
	//##################################################################
	public static class SelectedItems extends Vector{
	//##################################################################
	public SelectedItems(){super();}
	//##################################################################
	}
	//##################################################################

	//##################################################################
	public static class AvailableLists extends PropertyList {//implements ListObserver{
	//##################################################################
	/*
	public ListObserver.WeakListObserverCollection observers =
		new ListObserver.WeakListObserverCollection(){
			protected Object [] getTheItems() {return new Object[0];}
		};
	*/
	//===================================================================
	public Object getCopy()
	//===================================================================
	{
		return getFullCopy();
	}
	//===================================================================
	public void addList(String listName,Vector itemsInList) {set(listName,itemsInList);}
	public void removeList(String listName) {remove(listName);}
	public Vector getList(String listName) {return (Vector)getValue(listName,null);}
	//===================================================================
	//public void addObserver(ListObserver ob) {observers.addObserver(ob);}
	//===================================================================
	/**
	* By default, this does nothing except to further notify the observers.
	**/
	//public void listItemsChanged(Object which,Object [] newItems){observers.listChanged();}
	//##################################################################
	}
	//##################################################################
/*
//===================================================================
public String toText()
//===================================================================
{
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<size(); i++) {
		Property p = (Property)get(i);
		sb.append(textEncodeString(p.name));
		sb.append('=');
		Vector v = getSelectedFor(p.name,false);
		for (int j = 0; j<v.size(); j++) {
			sb.append(textEncodeString(v.get(j).toString()));
			if (j+1 < v.size()) sb.append('|');
		}
		if (i+1 < size()) sb.append('&');
	}
	return sb.toString();
}

//===================================================================
public void fromText(String st)
//===================================================================
{
	removeAllElements();
	Vector v = new Vector();
	String [] entries = Utils.split(st,'&');
	for (int e = 0; e < entries.length; e++) {
		String [] nv = Utils.split(entries[e],'=');
		String name = textDecodeString(nv[0]);
		String [] values = Utils.split(nv[1],'|');
		v.removeAllElements();
		for (int i = 0; i<values.length; i++) {
			String s = textDecodeString(values[i]);
			v.addElement(s);
		}
		setSelectedFor(name,v);
	}
}
//===================================================================
public void setText(String st) {fromText(st);}
public String getText() {return toText();}
//===================================================================
*/
	//##################################################################
	public static class SingleListSelect extends MultiListSelect{
	//##################################################################
	protected String listName = "";

	//===================================================================
	public void copyFrom(Object other)
	//===================================================================
	{
		super.copyFrom(other);
		if (other instanceof MultiListSelect.SingleListSelect)
			listName = ((MultiListSelect.SingleListSelect)other).listName;
	}

	//===================================================================
	public SingleListSelect(Vector all, Vector selected, boolean singleItemOnly)
	//===================================================================
	{
		this.singleItemPerList = singleItemOnly;
		listName = "list";
		this.availableLists = new MultiListSelect.AvailableLists();
		this.availableLists.addList(listName,all);
		this.setSelectedFor(listName,selected);
	}
	//===================================================================
	public SingleListSelect(Vector all, int selectedIndex)
	//===================================================================
	{
		this(all,MultiListSelect.toVector(all,new int[]{selectedIndex}),true);
	}
	//===================================================================
	public SingleListSelect(){}
	//===================================================================

	//===================================================================
	public SingleListSelect(Vector all,Vector selected){this(all,selected,"list");}
	//===================================================================

	//===================================================================
	public SingleListSelect(Vector all,Vector selected,String listName)
	//===================================================================
	{
		this.listName = listName;
		this.availableLists = new MultiListSelect.AvailableLists();
		this.availableLists.addList(listName,all);
		this.setSelectedFor(listName,selected);
	}
	//===================================================================
	public Vector getSelected() {return this.getSelectedFor(listName);}
	public void setSelected(Vector sel) {this.setSelectedFor(listName,sel);}
	//===================================================================
	public int [] getSelectedIndexes()
	//===================================================================
	{
		return this.getSelectedIndexesFor(listName);
	}
	//===================================================================
	public void setSelectedIndexes(int[] selected)
	//===================================================================
	{
		setSelected(MultiListSelect.toVector(getList(),selected));
	}
	//===================================================================
	public boolean isSelected(Object value)
	//===================================================================
	{
		return this.isSelected(listName,value);
	}
	//===================================================================
	public void select(Object value,boolean doSelect)
	//===================================================================
	{
		this.select(listName,value,doSelect);
	}
	//===================================================================
	public void select(int index,boolean doSet)
	//===================================================================
	{
		this.select(listName,index,doSet);
	}
	//===================================================================
	public String getListName() {return listName;}
	//===================================================================
	public Vector getList()
	//===================================================================
	{
		if (MultiListSelect.SingleListSelect.this.availableLists == null) return null;
		return MultiListSelect.SingleListSelect.this.availableLists.getList(listName);
	}

	//##################################################################
	}
	//##################################################################

public String toString()
{
	return "Available: "+availableLists.toString()+", "+super.toString();
}
//##################################################################
}
//##################################################################

