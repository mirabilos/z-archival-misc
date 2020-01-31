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
* This is a utility for storing an expandable array of integers. Similar
* to a Vector but for int values instead.
**/
//##################################################################
public class IntArray implements Copyable, Encodable{
//##################################################################
/**
* This is the array of integers itself. This can be changed while the
* array is being used.
**/
public int [] data; //Do not move this! It should be the first variable.
/**
* This specifies the number of valid values in the array.
**/
public int length = 0;// Do not move this! It should be the second variable.
/**
* This specifies the number of values to increase by when expansion is
* needed. If it is zero or negative then everytime an increase is needed
* the size of the data will be doubled (this is the default).
**/
public int growSize;// Do not move this! It should be the third variable.

//===================================================================
public Object getCopy()
//===================================================================
{
	IntArray ia = new IntArray();
	ia.length = length;
	ia.data = new int[length];
	if (length != 0) ewe.sys.Vm.copyArray(data,0,ia.data,0,length);
	ia.growSize = growSize;
	return ia;
}

/**
* Creates an IntArray with an initial size of 100 and a grow size of 100.
**/
//===================================================================
public IntArray(){this(100,-1);}
//===================================================================
/**
* Creates an IntArray with the specified initial size and grow size.
**/
//===================================================================
public IntArray(int initialSize,int growSize)
//===================================================================
{
	this.growSize = growSize;
	data = new int[initialSize];
}
/**
* Clears the IntArray completely.
**/
//===================================================================
public void clear() {length = 0;}
//===================================================================
/**
* Append a value to the end of the array.
**/
//===================================================================
public void add(int value)
//===================================================================
{
	if (length == data.length){
		int growSize = this.growSize;
		if (growSize <= 0) growSize = length+1;
		if (growSize > 100000) growSize = 100000;
		int [] dt = new int[length+growSize];
		ewe.sys.Vm.copyArray(data,0,dt,0,length);
		data = dt;
	}
	data[length++] = value;
}
//===================================================================
public void append(int value)
//===================================================================
{
	add(value);
}
/**
* This adds space to the IntArray at the specified index, increasing the
* length value by num. After this is
* done you can safely write into the data starting at index "where" for
* a length of num.
**/
//===================================================================
public boolean makeSpace(int where,int num)
//===================================================================
{
	if (num <= 0) return true;
	if (length+num > data.length) {
		int toAdd = num-(data.length-length);
		int growSize = this.growSize;
		if (growSize <= 0) growSize = data.length+1;
		if (growSize > 100000) growSize = 100000;
		if (toAdd < growSize) toAdd = growSize;
		int [] dt = new int[data.length+toAdd];
		ewe.sys.Vm.copyArray(data,0,dt,0,length);
		data = dt;
	}
	if (where < length)
		ewe.sys.Vm.copyArray(data,where,data,where+num,length-where);
	length += num;
	return true;
}

//===================================================================
public void insert(int[] values,int offset,int length,int where)
//===================================================================
{
	int myLength = this.length;
	try{
		makeSpace(where,length);
		ewe.sys.Vm.copyArray(values,offset,data,where,length);
	}catch(RuntimeException e){
		ewe.sys.Vm.debug(where+", "+values.length+", "+offset+", "+length);
		ewe.sys.Vm.debug(myLength+", "+data.length+", "+where+", "+this.length);
		throw e;
	}
}
//===================================================================
public void append(int[] values,int offset,int length)
//===================================================================
{
	insert(values,offset,length,this.length);
}

/**
* Insert a value at the specified index.
**/
//===================================================================
public void insert(int value,int index)
//===================================================================
{
	int toAdd = 0;
	if (index <= length) toAdd = 1;
	else toAdd = (index-length)+1;
	if (length+toAdd > data.length) {
		int growSize = this.growSize;
		if (growSize <= 0) growSize = data.length+1;
		if (growSize > 100000) growSize = 100000;
		if (toAdd < growSize) toAdd = growSize;
		int [] dt = new int[length+toAdd];
		ewe.sys.Vm.copyArray(data,0,dt,0,length);
		data = dt;
	}
	if (index < length) ewe.sys.Vm.copyArray(data,index,data,index+1,length-index);
	data[index] = value;
	length++;
}
static boolean hasNative = true;
native int nativeIndexOf(int value);
/**
* Find the index of the specified value. Returns -1 if it could not
* be found.
**/
//===================================================================
public int indexOf(int value)
//===================================================================
{
	if (hasNative) try{
		return nativeIndexOf(value);
	}catch(SecurityException e){
		hasNative = false;
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}
	for (int i = 0; i<length; i++)
		if (data[i] == value) return i;
	return -1;
}
/**
* Remove the value if it is in the array.
**/
//===================================================================
public void remove(int value)
//===================================================================
{
	while(true){
		int i = indexOf(value);
		if (i == -1) return;
		removeAtIndex(i);
	}
}

/**
 * Remove the value at the specified index.
 */
//===================================================================
public void removeAtIndex(int index)
//===================================================================
{
	if (index == -1) return;
	ewe.sys.Vm.copyArray(data,index+1,data,index,length-index-1);
	length--;
}
/**
* Return a copy of the array of integers whose length exactly holds
* all added integers.
**/
//===================================================================
public int [] toIntArray()
//===================================================================
{
	int [] ret = new int[length];
	ewe.sys.Vm.copyArray(data,0,ret,0,length);
	return ret;
}
/**
* Copy all values into the destination array at the specified offset.
**/
//===================================================================
public void copyInto(int [] dest,int offset)
//===================================================================
{
	ewe.sys.Vm.copyArray(data,0,dest,offset,length);
}

/**
* Append all values in this IntArray to the specified destination array.
* If the destination array is null it is treated as an array of zero
* length. A new int array is returned which holds the new array of integers.
**/
//===================================================================
public int [] appendTo(int [] dest) {return appendTo(dest,false);}
//===================================================================
/**
* Append all values in this IntArray to the specified destination array with
* the option of reversing the order of integers in this IntArray before appending.
* If the destination array is null it is treated as an array of zero
* length. A new int array is returned which holds the new array of integers.
**/
//===================================================================
public int [] appendTo(int [] dest,boolean reverse)
//===================================================================
{
	if (dest == null) dest = new int[0];
	int dl = dest.length;
	int [] ret = new int[dl+length];
	ewe.sys.Vm.copyArray(dest,0,ret,0,dl);
	if (reverse)
		for (int i = length-1; i>=0; i--) ret[dl++] = data[i];
	else ewe.sys.Vm.copyArray(data,0,ret,dl,length);
	return ret;
}
//##################################################################
}
//##################################################################

