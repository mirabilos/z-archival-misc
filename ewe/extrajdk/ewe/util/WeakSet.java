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
package ewe.util;

import ewe.sys.*;
import ewe.reflect.WeakReference;//java.lang.ref.WeakReference;
/**
* A weak set is an unorderd collection of weak references to objects. Any
* object within this set is elligeble for garbage collection if it is not
* pointed to by at least one non-weak reference. This implementation is very
* different to the native version.
**/
//##################################################################
public class WeakSet{ //VERY DIFFERENT TO NATIVE VERSION.
//##################################################################

boolean useRefs = true;

//===================================================================
public WeakSet()
//===================================================================
{
	try{
		new java.lang.ref.WeakReference(this);
	}catch(Error e){
		useRefs = false;
	}
}
Vector refs = new Vector();

void cleanup()
{
	if (!useRefs) return;
	Vector toGo = new Vector();
	for (int i = 0; i<refs.size(); i++){
		WeakReference wr = (WeakReference)refs.get(i);
		if (wr.get() == null) toGo.add(wr);
	}
	for (int i = 0; i<toGo.size(); i++)
		refs.remove(toGo.get(i));
}

//-------------------------------------------------------------------
private Object toObject(Object what)
//-------------------------------------------------------------------
{
	if (!useRefs) return what;
	else return ((WeakReference)what).get();
}
//-------------------------------------------------------------------
private Object toRef(Object what)
//-------------------------------------------------------------------
{
	if (!useRefs) return what;
	return new WeakReference(what);
}
/**
* This adds a reference to the set. Adding null will be ignored and adding an object
* which is already in the set will be ignored.
**/
//===================================================================
public void add(Object what)
//===================================================================
{
	if (what != null){
		cleanup();
		for (int i = 0; i<refs.size(); i++)
			if (toObject(refs.get(i)) == what) return;
		refs.add(toRef(what));
	}
}
/**
* This removes a reference from the set if it is in it.
**/
//===================================================================
public void remove(Object what)
//===================================================================
{
	if (what != null){
		cleanup();
		for (int i = 0; i<refs.size(); i++)
			if (toObject(refs.get(i)) == what){
				refs.del(i);
				return;
			}
	}
}
/**
* This removes all references in the set.
**/
public void clear() {refs.clear();}
/**
* This returns an array containing the references. NOTE THAT THERE MAY BE NULL REFERENCES IN THE ARRAY
* as between the time the time the number of items is determined and the array is allocated some items
* may have been removed.
**/
public Object [] getRefs()
{
	cleanup();
	Vector got = new Vector();
	for (int i = 0; i<refs.size(); i++){
		//WeakReference wr = (WeakReference)refs.get(i);
		//Object f = wr.get();
		Object f = toObject(refs.get(i));
		if (f != null) got.add(f);
	}
	Object [] ret = new Object[got.size()];
	got.copyInto(ret);
	return ret;
}
/**
* This finds an object int the reference list using the ObjectFinder. This has the advantage of not
* creating an array everytime you are looking for an object.
**/
public Object find(ObjectFinder f)
{
	cleanup();
	for (int i = 0; i<refs.size(); i++){
		Object g = toObject(refs.get(i));
		if (g != null)
			if (f.lookingFor(g)) return g;
	}
	return null;
}

public boolean contains(Object who)
{
	if (who == null) return false;
	for (int i = 0; i<refs.size(); i++)
		if (toObject(refs.get(i)) == who) return true;
	return false;
}
/**
* This counts the number of live references. However this number may be
* reduced unpredictably.
**/
//===================================================================
public int count()
//===================================================================
{
	Object [] all = getRefs();
	int num = 0;
	for (int i = 0; i<all.length; i++) if (all[i] != null) num++;
	return num;
}

//===================================================================
public boolean isEmpty()
//===================================================================
{
	return refs.size() == 0;
}
/**
* Get an Iterator to go through all the entries. The iterator will not return
* a null reference when next() is called (unless hasNext() returns false).
**/
//===================================================================
public Iterator entries()
//===================================================================
{
	return new IteratorEnumerator(){
		int nextIndex = 0;
		Object [] refs = getRefs();
		public boolean hasNext(){
			for(;nextIndex < refs.length;nextIndex++)
				if (refs[nextIndex] != null) return true;
			return false;
		}
		public Object next(){
			if (!hasNext()) return null;
			return refs[nextIndex++];
		}
	};
}
//##################################################################
}
//##################################################################
