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
* This is a utility class which can be the building blocks of linked list
* implementation. It has a native method to quickly traverse the list
* in both direction.
**/
//##################################################################
public class LinkedListElement{
//##################################################################
//---------------------------- Do not move these two.
public LinkedListElement next;
public LinkedListElement prev;
//----------------------------
/**
* Starting from the "start" element count "elements" units down. Returns
* null if it runs out of elements. If elements is negative, it will return
* the last element in the chain.
**/
//===================================================================
public static LinkedListElement getNext(LinkedListElement start,int elements)
//===================================================================
{
	if (start == null) return null;
	LinkedListElement cur = start;
	for (int i = 0; i<elements || elements<0; i++)
		if (cur.next == null)
			if (elements < 0) return cur;
			else return null;
		else cur = cur.next;
	return cur;
}
/**
* Starting from the "start" element count "elements" units back. Returns
* null if it runs out of elements. If elements is negative, it will return
* the first element in the chain.
**/
//===================================================================
public static LinkedListElement getPrev(LinkedListElement start,int elements)
//===================================================================
{
	if (start == null) return null;
	LinkedListElement cur = start;
	for (int i = 0; i<elements || elements<0; i++)
		if (cur.prev == null)
			if (elements < 0) return cur;
			else return null;
		else cur = cur.prev;
	return cur;
}

//===================================================================
public static void removeSection(LinkedListElement start,LinkedListElement last)
//===================================================================
{
	if (last == null) last = start;
	if (start.prev != null) start.prev.next = last.next;
	if (last.next != null) last.next.prev = start.prev;
	start.prev = last.next = null;
}
//===================================================================
public static void addSectionAfter(LinkedListElement after,LinkedListElement startToAdd)
//===================================================================
{
	if (startToAdd == null || after == null) return;
	LinkedListElement last = getNext(startToAdd,-1);
	if (after.next != null) after.next.prev = last;
	last.next = after.next;
	after.next = startToAdd;
	startToAdd.prev = after;
}
//===================================================================
public static void addSectionBefore(LinkedListElement before,LinkedListElement startToAdd)
//===================================================================
{
	if (startToAdd == null || before == null) return;
	LinkedListElement last = getNext(startToAdd,-1);
	startToAdd.prev = before.prev;
	if (before.prev != null) before.prev.next = startToAdd;
	before.prev = last;
	last.next = before;
}
/**
* This replaces a section of elements with a new section. The oldsection has its two ends
* terminated with nulls so that it can be placed in a different list.
**/
//===================================================================
public static void replaceSection(LinkedListElement start,LinkedListElement end,LinkedListElement newSection)
//===================================================================
{
	if (start == null || end == null) return;
	LinkedListElement before = start.prev;
	LinkedListElement after = end.next;
	start.prev = null;
	end.next = null;
	if (before != null) before.next = after;
	if (after != null) after.prev = before;
	if (newSection == null) return;
	if (before != null) before.next = newSection;
	LinkedListElement lastSection = getNext(newSection,-1);
	if (after != null) after.prev = lastSection;
	newSection.prev = before;
	lastSection.next = after;
}
/**
* Count how many are in the list starting from (and including) start.
**/
//===================================================================
public static int countNext(LinkedListElement start)
//===================================================================
{
	return countInRange(start,null);
}
/**
* Count how many are in the list starting from (and including) start going backwards.
**/
//===================================================================
public static int countPrev(LinkedListElement start)
//===================================================================
{
	return countInRange(null,start);
}
/**
* Counts the number of elements in the inclusive range start -> end.
* If the end parameter is null it counts from the start to the end of the list.
* If the start parameter is null it counts from end backwards to the start of the list.
**/
//===================================================================
public static int countInRange(LinkedListElement start,LinkedListElement end)
//===================================================================
{
	boolean back = false;
	if (start == null)
		if (end == null) return 0;
		else {
			start = end;
			back = true;
		}
	for (int i = 0;;){
		if (start == null) return i;
		i++;
		if (start == end) return i;
		else start = back ? start.prev : start.next;
	}
}
//===================================================================
public static void toArray(LinkedListElement first,int length,LinkedListElement [] elements)
//===================================================================
{
	for (int i = 0; i<length; i++){
		elements[i] = first;
		first = first.next;
	}
}
//##################################################################
}
//##################################################################
