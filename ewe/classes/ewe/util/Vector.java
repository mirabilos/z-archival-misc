/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

/**
 * A vector is an array of object references. The vector grows and shrinks
 * dynamically as objects are added and removed.
 * <p>
 * Here is an example showing a vector being used:
 *
 * <pre>
 * ...
 * Vector vec = new Vector();
 * vec.add(obj1);
 * vec.add(obj2);
 * ...
 * vec.insert(3, obj3);
 * vec.del(2);
 * if (vec.getCount() > 5)
 * ...
 * </pre>
 */
public class Vector extends ewe.data.DataObject
{
/* Do not move these. They are used for native methods.*/
Object items[];
int count;

/** Constructs an empty vector. */
public Vector()
	{
	this(8);
	}

/**
 * Constructs an empty vector with a given initial size. The size is
 * the initial size of the vector's internal object array. The vector
 * will grow as needed when objects are added.
 */
public Vector(int size)
	{
	items = new Object[size];
	}

public Vector(Object [] items)
{
	this(8);
	addAll(items);
}

//===================================================================
public void copyFrom(Object other)
//===================================================================
{
	super.copyFrom(other);
	if (other instanceof Vector){
		clear();
		items = new Object[((Vector)other).size()];
		((Vector)other).copyInto(items);
		count = items.length;
	}else if (other instanceof Object []){
		clear();
		addAll((Object [])other);
	}
}
//===================================================================
public void addCopiesFrom(Object [] object,int start,int length)
//===================================================================
{
	clear();
	if (items.length < length) items = new Object[length];
	for (int i = 0; i<length; i++)
		items[i] = Utils.getCopy(object[i+start]);
	count = length;
}
//===================================================================
public void copyCopiesFrom(Object other)
//===================================================================
{
	if (other instanceof Vector){
		Vector v = (Vector)other;
		addCopiesFrom(v.items,0,v.count);
	}else if (other instanceof Object []){
		Object [] ot = (Object [])other;
		addCopiesFrom(ot,0,ot.length);
	}
}
/**
* This returns a Vector that is a copy of this one, where all the items
* are themselves copies of the original items.
**/
//===================================================================
public Vector getFullCopy()
//===================================================================
{
	Vector v = (Vector)getNew();
	v.clear();
	v.copyCopiesFrom(this);
	return v;
}
/** Adds an object to the end of the vector. */
public void add(Object obj)
	{

	if (count < items.length)
		items[count++] = obj;
	else
		add(count, obj);
	}


/**
 * Insert an object at the given index.
 * @param index The index to insert the object.
 * @param obj The object to add.
 * @deprecated Use add(index,obj) instead.
 */
public void insert(int index, Object obj)
{
	add(index,obj);
}

/** Inserts an object at the given index. */
public void add(int index, Object obj)
	{
	//ewe.sys.Vm.debug(index+", "+count+", "+items.length);
	if (count == items.length)
		{
		// double size of items array
		Object newItems[] = new Object[(items.length * 2)+1];
		Vm.copyArray(items, 0, newItems, 0, count);
		items = newItems;
		}
	if (index != count)
		Vm.copyArray(items, index, items, index + 1, count - index);
	items[index] = obj;
	count++;
	}

/** Deletes the object reference at the given index. */
public void del(int index)
	{
	if (index != count - 1)
		Vm.copyArray(items, index + 1, items, index, count - index - 1);
	items[count - 1] = null;
	count--;
	}

/** Returns the object at the given index. */
public Object get(int index)
	{
	if (index >= count)
		index = items.length; // force an out of range error
	return items[index];
	}

/** Sets the object at the given index. */
public void set(int index, Object obj)
	{
	if (index >= count)
		index = items.length; // force an out of range error
	items[index] = obj;
	}

/**
 * Finds the index of the given object. The list is searched using a O(n) linear
 * search through all the objects in the vector.
 */
//MLB replaced with a new version.
/*
public int find(Object obj)
	{
	for (int i = 0; i < count; i++)
		if (items[i] == obj)
			return i;
	return -1;
	}
*/
/** Returns the number of objects in the vector. */
public int getCount()
	{
	return count;
	}

/** Converts the vector to an array of objects. */
public Object []toObjectArray()
	{
	Object objs[] = new Object[count];
	Vm.copyArray(items, 0, objs, 0, count);
	return objs;
	}
//MLB
//===================================================================
public void clear(){count = 0; Utils.zeroArrayRegion(items,0,items.length);}
//===================================================================


/**
 * Get an Iterator for the vector. The iterator that is returned supports the remove() method.
 * @return an Iterator for the vector.
 */
//===================================================================
public Iterator iterator()
//===================================================================
{return new VectorIterator(this);}


/**
 * Get an Enumeration for the elements of the vector.
 * @return an Enumeration for the elements of the vector.
 */
//===================================================================
public Enumeration elements()
//===================================================================
{return new VectorIterator(this);}

//===================================================================
public int size() {return count;}
//===================================================================

//===================================================================
public int compare(Object one,Object two)
//===================================================================
{
	return Utils.compare(one,two);
}
/**
 * Finds the index of the given object. The list is searched using a O(n) linear
 * search through all the objects in the vector.
 */
//===================================================================
public int find(Object obj)
//===================================================================
	{
	for (int i = 0; i < count; i++)
		if (compare(items[i],obj) == 0)
			return i;
	return -1;
	}

public boolean contains(Object obj) {return find(obj) != -1;}

//===================================================================
public void remove(Object obj)
//===================================================================
{
	int idx = find(obj);
	if (idx != -1) del(idx);
}

//===================================================================
public void addAll(Object [] objs, int start, int length)
//===================================================================
{
	if (objs == null) return;
	if (count+length > items.length){
		Object [] no = new Object[count+length+20];
		ewe.sys.Vm.copyArray(items,0,no,0,count);
		items = no;
	}
	if (length > 0) ewe.sys.Vm.copyArray(objs,start,items,count,length);
	count += length;
}
//===================================================================
public void addAll(Object [] objs)
//===================================================================
{
	if (objs == null) return;
	else addAll(objs,0,objs.length);
}
//===================================================================
public void addAll(Vector objs)
//===================================================================
{
	if (objs == null) return;
	addAll(objs.items,0,objs.count);
}
/**
* The destination array MUST be big enough to hold the vector.
**/
//===================================================================
public void copyInto(Object [] array,int destPos)
//===================================================================
{
	ewe.sys.Vm.copyArray(items,0,array,destPos,count);
	//for (int i = 0; i<count; i++) array[i+destPos] = items[i];
}
/**
* The destination array MUST be big enough to hold the vector.
**/
//===================================================================
public void copyInto(Object [] array) {copyInto(array,0);}
//===================================================================
public Object find(ObjectFinder finder)
//===================================================================
{
	for (int i = 0; i<count; i++) if (finder.lookingFor(items[i])) return items[i];
	return null;
}
/**
* This adds an object to the end of the Vector v. If v is null a new Vector is created
* and returned.
**/
//===================================================================
public static Vector add(Vector v,Object toAdd)
//===================================================================
{
	if (v == null) v = new Vector();
	v.add(toAdd);
	return v;
}
/**
* This inserts an object to the front of the Vector v. If v is null a new Vector is created
* and returned. This is not meant to be a high-performance Stack implementation.
**/
//===================================================================
public static Vector push(Vector v,Object toPush)
//===================================================================
{
	if (v == null) v = new Vector();
	v.insert(0,toPush);
	return v;
}
/**
* This removes and returns the object at the front of the Vector v. If v is null OR v is
* empty, it will return null. This is not meant to be a high-performance Stack implementation.
**/
//===================================================================
public static Object pop(Vector v)
//===================================================================
{
	if (v == null) return null;
	if (v.size() == 0) return null;
	Object ret = v.get(0);
	v.del(0);
	return ret;
}
/**
* This returns an iterator for the specified Vector. If the vector is null it will return
* an Iterator with no elements.
**/
//===================================================================
public static Iterator iterator(Vector v)
//===================================================================
{
	return new VectorIterator(v);
}
//===================================================================
public String toString()
//===================================================================
{
	String out = "{";
	for (int i = 0; i<size(); i++){
		if (i != 0) out+=",";
		out += mString.toString(get(i));
	}
	out += "}";
	return out;
}
/**
 * Copy the vector into the destination array, creating a new one if the provided destination is not
	big enough to hold all the elements. If the provided destination array is big enough for the vector,
	the elements are copied into the array starting from the first index. If the array is bigger than the
	size of the vector, the extra elements of the array are set to null. If the array is smaller than the
	vector, then a new array is created, whose runtime type is the same as the destination parameter, and
	whose size is the same size as the vector. The vector elements are then copied into the newly created
	array and this array is then returned.
 * @param [] dest A destination object array to copy the vector elements into.
 * @return The destination object array if it is big enough, or a new array if it is not.
 */
//===================================================================
public Object [] toArray(Object [] dest)
//===================================================================
{
	if (dest == null) throw new NullPointerException();
	if (dest.length >= count) {
		copyInto(dest);
		for (int i = count; i<dest.length; i++) dest[i] = null;
	}else{
		Class c = dest.getClass();
		Class cp = c.getComponentType();
		ewe.reflect.Reflect r = new ewe.reflect.Reflect(cp);
		dest = (Object [])r.newArray(count);
		copyInto(dest);
	}
	return dest;
}
/**
* Create a new Object array and copy all the elements into it.
* @return A new Object array containing the vector's elements.
*/
//===================================================================
public Object [] toArray()
//===================================================================
{
	return toArray(new Object[0]);
}

/**
* This is the same as get().
**/
//===================================================================
public Object elementAt(int index)
//===================================================================
{
	return get(index);
}

//===================================================================
public void addElement(Object what)
//===================================================================
{
	add(what);
}

//===================================================================
public void setElementAt(Object obj,int index)
//===================================================================
{
	set(index,obj);
}

//===================================================================
public void setSize(int size)
//===================================================================
{
	if (size < 0) size = 0;
	if (size < count){
		Utils.zeroArrayRegion(items,size,count-size);
	}else if (size > count){
		if (size > items.length){
			Object[] n = new Object[size];
			ewe.sys.Vm.copyArray(items,0,n,0,count);
			items = n;
		}else{
			Utils.zeroArrayRegion(items,count,size-count);
		}
	}
	count = size;
}
//===================================================================
public void removeElementAt(int index)
//===================================================================
{
	del(index);
}
//===================================================================
public void insertElementAt(Object obj,int index)
//===================================================================
{
	add(index,obj);
}
//===================================================================
public boolean isEmpty()
//===================================================================
{
	return count <= 0;
}
//===================================================================
public void removeAllElements()
//===================================================================
{
	clear();
}

//===================================================================
public boolean sort(Handle h,Comparer comparer,boolean descending)
//===================================================================
{
	if (items.length != count){
		Object[] no = new Object[count];
		System.arraycopy(items,0,no,0,count);
		items = no;
	}
	return Utils.sort(h,items,comparer,descending);
}

//===================================================================
public void sort(Comparer comparer,boolean descending)
//===================================================================
{
	sort(null,comparer,descending);
}
public void zero() {items = new Object[0]; count = 0;}
}

