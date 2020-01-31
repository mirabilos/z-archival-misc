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


/**
* A weak set is an unordered collection of weak references to objects. Any
* object within this set is elligable for garbage collection if it is not
* pointed to by at least one non-weak reference.
**/
//##################################################################
public class WeakSet{
//##################################################################

Object [] objects = new Object[0]; //This MUST be the FIRST variable.
int num; //This MUST be the SECOND variable.

/**
* This adds a reference to the set. Adding null will be ignored and adding an object
* which is already in the set will be ignored.
**/
public native void add(Object what);
/**
* This removes a reference from the set if it is in it.
**/
public native void remove(Object what);
/**
* This removes all references in the set.
**/
public native void clear();
/**
* This returns an array containing the references. NOTE THAT THERE MAY BE NULL REFERENCES IN THE ARRAY
* as between the time the number of items is determined and the array is allocated some items
* may have been removed.
**/
public native Object [] getRefs();
/**
* This finds an object in the reference list using the ObjectFinder. This has the advantage of not
* creating an array everytime you are looking for an object.
**/
public native Object find(ObjectFinder f);
/**
 * Checks if an Object is contained in the Weak Set.
 * @param Object The Object to look for.
 * @return true if the Object is in the set.
 */
//===================================================================
public native boolean contains(final Object who);
//===================================================================
/*
{
	if (who == null) return false;
	return find(new ObjectFinder(){
		public boolean lookingFor(Object obj){
			return obj == who;
		}
	}) != null;
}
	*/

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
/**
* If this returns true  - it indicates that there are definitely no entries. If it is false, it
* indicates that there MAY be some entries. This is a very fast method that does not check any
* live references.
**/
//===================================================================
public native boolean isEmpty();
//===================================================================

//public static WeakSet all = new WeakSet();
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

