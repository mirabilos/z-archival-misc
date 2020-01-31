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
* A RangeList manages a set of Range object - but you should never add
* Range objects to it directly. The RangeList always ensures that its
* collection of Ranges are fully optimized. That is if two ranges
* are contiguous, they are merged to form a single range. Similarly
* overlapping ranges are also merged. The ranges
* are also always sorted in increasing order.
**/
//##################################################################
public class RangeList extends Vector{
//##################################################################

protected static Range buff = new Range(0,-1);

//===================================================================
public boolean inRange(int start) {return inRange(start,start);}
//===================================================================

//===================================================================
public boolean inRange(int start,int end)
//===================================================================
{
	if (start > end){
		int t = end; end = start; start = t;
	}
	int idx = findRangeWith(start);
	if (idx == -1) return false;
	Range p = (Range)items[idx];
	return p.last >= end;
}
//===================================================================
public int findRangeWith(int value)
//===================================================================
{
	for (int i = 0; i<count; i++){
		Range p = (Range)items[i];
		if (p.last < value) continue;
		if (p.first > value) return -1;
		return i;
	}
	return -1;
}
//===================================================================
public void addRange(int start,int end)
//===================================================================
{
	if (start > end){
		int t = end; end = start; start = t;
	}
	Range p = new Range(start,end);
	for (int i = 0; i<count; i++){
		Range p2 = (Range)items[i];
		if (p2.first <= start) continue;
		insert(i,p);
		p = null;
		break;
	}
	if (p != null) add(p);
	//System.out.println(this);
	fixup();
	//ewe.sys.Vm.debug(toString());
	//System.out.println("------------------------------------------------");
}

//===================================================================
public void removeRange(int start, int end)
//===================================================================
{
	if (start > end){
		int t = end; end = start; start = t;
	}
	for (int i = 0; i<count; i++){
		Range me = (Range)items[i];
		if (me.first <= start){
			if (me.last < start) continue;
			if (me.last >= end) { //Here the found range covers the selected range.
				Range p2 = new Range(end+1,me.last);
				insert(i+1,p2);
			}
			me.last = start-1;
		}else if (me.first <= end){
			me.first = end+1;
		}else
			break;
	}
	//System.out.println(this);
	fixup();
	//ewe.sys.Vm.debug(toString());
	//System.out.println("------------------------------------------------");
}
//-------------------------------------------------------------------
protected void fixup()
//-------------------------------------------------------------------
{
	for (int i = 0; i<count-1; i++){
		Range me = (Range)items[i];
		if (me.last < me.first) continue;
		for (int j = i+1; j<count; j++){
			Range other = (Range)items[j];
			if (other.first <= me.last+1){
				me.last = Math.max(me.last,other.last);
				other.last = other.first-1;
			}else
				break;
		}
	}
	for (int i = 0; i<count; i++){
		Range me = (Range)items[i];
		if (me.last < me.first) del(i--);
	}
}
/*
public static void main(String args[])
{
	RangeList r = new RangeList();
	r.addRange(5,8);
	r.addRange(1,3);
	r.addRange(12,20);
	r.addRange(4,10);
	r.addRange(11,11);
	r.removeRange(10,10);
	r.removeRange(8,12);
	r.addRange(24,27);
	r.removeRange(4,26);
}
*/
//===================================================================
public int countItems()
//===================================================================
{
	int c = 0;
	for (int i = 0; i<count; i++){
		Range me = (Range)items[i];
		if (me.last < me.first) continue;
		c += me.last-me.first+1;
	}
	return c;
}
//===================================================================
public int [] toIntArray()
//===================================================================
{
	int total = countItems();
	int [] ret = new int[total];
	int c = 0;
	for (int i = 0; i<count; i++){
		Range me = (Range)items[i];
		if (me.last < me.first) continue;
		int len = me.last-me.first+1;
		Utils.getIntSequence(ret,c,me.first,1,len);
		c += len;
	}
	return ret;
}
//===================================================================
public int getItemAtIndex(int idx)
//===================================================================
{
	int c = 0;
	for (int i = 0; i<count; i++){
		Range me = (Range)items[i];
		if (me.last < me.first) continue;
		int len = me.last-me.first+1;
		if (c+len > idx) return me.first+(idx-c);
		c += len;
	}
	return -1;
}
/**
* Use this to shift up or down, but be careful shifting down.
**/
//===================================================================
public void shift(int fromWhere,int places)
//===================================================================
{
	int c = 0;
	for (int i = 0; i<count; i++){
		Range me = (Range)items[i];
		if (me.last < me.first) continue;
		if (me.last < fromWhere) continue;
		if (me.first < fromWhere){
			Range p2 = new Range(fromWhere,me.last);
			me.last = fromWhere-1;
			insert(i+1,p2);
			continue;
		}else{
			me.first += places;
			me.last += places;
		}
	}
}
//===================================================================
public int countRanges()
//===================================================================
{
	return count;
}
//===================================================================
public Range rangeAt(int rangeIndex,Range dest)
//===================================================================
{
	if (rangeIndex < 0 || rangeIndex >= count) return null;
	if (dest == null) dest = new Range(0,-1);
	dest.set((Range)get(rangeIndex));
	return dest;
}
//===================================================================
public RangeList(){}
//===================================================================

//===================================================================
public void removeAndShiftUp(int start,int end)
//===================================================================
{
	removeRange(start,end);
	shift(end,-((end-start)+1));
}
//##################################################################
}
//##################################################################

