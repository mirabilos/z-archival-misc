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
import ewe.sys.Vm;

/**
* A TagList stores a list of Tags (a positive integer tag value associated with an Object value)
* using arrays of integers and object references instead of arrays of Tag objects.<p>
* A TagList only allows the storage of one Object value for each distinct Tag value. That is,
* if you call set(int tag, Object value) and a value already exists for that integer tag value,
* then this new value will overwrite the old one.
**/
//##################################################################
public class TagList{
//##################################################################

protected int num = 0;
int tags [] = null;
Object values [] = null;
public int expandSize = 5;

public static final int EmptyTag = -1;
//===================================================================
public int size() {return num;}
//===================================================================

//-------------------------------------------------------------------
protected int getTagIndex(int tag)
//-------------------------------------------------------------------
{
	int startIndex = 0;
	for (int i = startIndex; i<num; i++)
		if (tags[i] == tag) return i;
	return -1;
}
//-------------------------------------------------------------------
protected int getEmptyIndex() {return getTagIndex(EmptyTag);}
//-------------------------------------------------------------------

/**
* Get the tag at the specified index. A tag value of EmptyTag (-1) indicates
* not tag value stored at that point.
**/
//===================================================================
public Tag getAtIndex(int index,Tag dest)
//===================================================================
{
	if (index >= num || index < 0) return null;
	if (dest == null) dest = new Tag();
	dest.tag = tags[index]; dest.value = values[index];
	return dest;
}
/**
* Get the tag Object for the specified tag integer value.
* @param tag The integer tag value to look for.
* @param dest An optional destination tag object.
* @return The destination or new Tag object containing the tag data, or null if
* the tag value is not stored.
*/
//==================================================================
public Tag get(int tag,Tag dest) {return getAtIndex(getTagIndex(tag),dest);}
//===================================================================

/**
* @param tag The integer tag value to look for.
 * @param defaultValue a default Object value to return if the tag is not found
 * @return The object value of that tag, or the default value if it is not found.
 */
//===================================================================
public Object getValue(int tag,Object defaultValue)
//===================================================================
{
	int idx = getTagIndex(tag);
	if (idx == -1) return defaultValue;
	return values[idx];
}
//===================================================================
void goingToAdd(int toAdd)
//===================================================================
{
	int len = values == null ? 0 : values.length;
	int more = num+toAdd-len;
	if (more <= 0) return;
	if (more < expandSize) more = expandSize;
	int [] nt = new int[len+more];
	if (len != 0) Vm.copyArray(tags,0,nt,0,len);
	tags = nt;
	Object [] nv = new Object[len+more];
	if (len != 0) Vm.copyArray(values,0,nv,0,len);
	values = nv;
}
/**
* Set the Object value for a tag.
* @param tag The integer tag value to set.
* @param value The Object value to set.
*/
//===================================================================
public void set(int tag,Object value)
//===================================================================
{
	int idx = getTagIndex(tag);
	if (idx == -1) idx = getEmptyIndex();
	if (idx != -1){
		tags[idx] = tag;
		values[idx] = value;
		return;
	}
	goingToAdd(1);
	tags[num] = tag;
	values[num] = value;
	num++;
}
/**
* Set all the tag values from the other TagList.
* @param tl Another TagList to copy values from.
*/
//===================================================================
public void set(TagList tl)
//===================================================================
{
	if (tl == null) return;
	Tag tg = null;
	for (int i = 0; i<tl.size(); i++) {
		tg = (Tag)tl.getAtIndex(i,tg);
		set(tg.tag,tg.value);
	}
}
/**
* Remove a tag value.
* @param tag The integer tag value to remove.
*/
//===================================================================
public void clear(int tag)
//===================================================================
{
	int idx = getTagIndex(tag);
	if (idx == -1) return;
	tags[idx] = EmptyTag;
	values[idx] = null;
}
/**
* Set a value for a tag if it has not already been set.
* @param tag The integer tag value.
* @param value The object tag value.
*/
//===================================================================
public void defaultTo(int tag,Object value)
//===================================================================
{
	int idx = getTagIndex(tag);
	if (idx != -1) return;
	set(tag,value);
}
/**
* Check if the specified tag has a value set for it.
* @param tag The integer tag value to check for.
*/
//===================================================================
public boolean hasTag(int tag) {return getTagIndex(tag) != -1;}
//===================================================================

//##################################################################
}
//##################################################################

