/* $MirOS: contrib/hosted/ewe/classes/ewe/util/Hashtable.java,v 1.3 2007/08/30 23:16:07 tg Exp $ */

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
* A Hashtable is an implementation of a Map. This is a collection that
* associates Object values with Object keys. You search for an object
* in the Map by using the key.
**/
//##################################################################
public class Hashtable implements Map{
//##################################################################

HashEntry [] table;


//===================================================================
public Iterator entries() {return new HashEntryIterator();}
//===================================================================

/**
 * The total number of entries in the hash table.
 */
private int count;

/**
 * The table is rehashed when its size exceeds this threshold.  (The
 * value of this field is (int)(capacity * loadFactor).)
 */
private int threshold;

/**
 * The load factor for the hashtable.
 */
private float loadFactor;
	/**
	 * Constructs a  hashtable with the specified initial
	 * capacity and the specified load factor.
	 */
	//===================================================================
	public Hashtable(int initialCapacity, float loadFactor) {
	//===================================================================
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: "+
				initialCapacity);
		if (loadFactor <= 0)
			throw new IllegalArgumentException("Illegal Load: "+loadFactor);

		if (initialCapacity==0)
			initialCapacity = 1;
		this.loadFactor = loadFactor;
		table = new HashEntry[initialCapacity];
		threshold = (int)(initialCapacity * loadFactor);
	}
	/**
	 * Constructs a new hashtable with the specified initial capacity
	 * and a default load factor of 0.75
	 */
	//===================================================================
	public Hashtable(int initialCapacity) {
	//===================================================================
		this(initialCapacity, 0.75f);
	}
	/**
	 * Constructs a new hashtable with the default initial capacity
	 * and a default load factor of 0.75
	 */
	//===================================================================
	public Hashtable() {
	//===================================================================
		this(11, 0.75f);
	}
	//===================================================================
	public int size() {
	//===================================================================
		return count;
	}
	//===================================================================
	public boolean isEmpty() {
	//===================================================================
		return count == 0;
	}

	private HashEntry findKey(Object key,int code,int index)
	{
		if (index == -1) index = (code & 0x7FFFFFFF) % table.length;
		for (HashEntry e = table[index] ; e != null ; e = e.next)
			if (e.key == key) return e;
			else if (e.key.equals(key)) return e;
		return null;
	}

	//===================================================================
	public boolean containsValue(Object value) {
	//===================================================================
		for (int i = table.length ; i-- > 0 ;) {
			for (HashEntry e = table[i] ; e != null ; e = e.next)
				if (e.value == value) return true;
				else if (e.value != null)
					if (e.equals(value)) return true;
		}
		return false;
	}
	//===================================================================
	public boolean containsKey(Object key) {
	//===================================================================
		return findKey(key,key.hashCode(),-1) != null;
	}
	//===================================================================
	public Object remove(Object key){
	//===================================================================
		int code = key.hashCode();
		int index = (code & 0x7FFFFFFF) % table.length;
		HashEntry e = findKey(key,code,index);
		if (e == null) return null;
		count--;
		if (table[index] == e) table[index] = e.next;
		else{
			HashEntry b;
			for (b = table[index]; b.next != e && b != null; b = b.next);
			if (b != null) b.next = e.next;
		}
		e.next = null;
		return e.value;
	}
	//===================================================================
	public Object put(Object key,Object value)
	//===================================================================
	{
		int code = key.hashCode();
		int index = (code & 0x7FFFFFFF) % table.length;
		HashEntry e = findKey(key,code,index);
		Object ret = null;
		if (e == null) {
			e = new HashEntry();
			e.code = code;
			e.key = key;
			e.value = value;
			e.next = table[index];
			table[index] = e;
			ret = null;
			count++;
			if (count >= threshold) rehash();
		}else{
			ret = e.value;
			e.value = value;
		}
		return ret;
	}

	//===================================================================
	public void clear() {table = new HashEntry[table.length]; count = 0;}
	//===================================================================
	public Object get(Object key)
	//===================================================================
	{
		HashEntry e = findKey(key,key.hashCode(),-1);
		if (e == null) return null;
		else return e.value;
	}
	//-------------------------------------------------------------------
	protected void rehash()
	//-------------------------------------------------------------------
	{
		//ewe.sys.Vm.debug("Rehashing!");
		HashEntry []old = table;
		table = new HashEntry[old.length*2+1];
		threshold = (int)(table.length*loadFactor);
		for (int i = 0; i<old.length; i++){
			for (HashEntry e = old[i]; e != null;){
				HashEntry n = e.next;
				int index = (e.code & 0x7FFFFFFF) % table.length;
				e.next = table[index];
				table[index] = e;
				e = n;
			}
		}
	}
	//===================================================================
	public Enumeration elements()
	//===================================================================
	{
		return new HashValueEnumeration();
	}
	//===================================================================
	public Enumeration keys()
	//===================================================================
	{
		return new HashKeyEnumeration();
	}
	//##################################################################
	class HashValueEnumeration implements Enumeration{
	//##################################################################
	Iterator it;
	HashValueEnumeration()
	{
		it = entries();
	}
	//===================================================================
	public boolean hasMoreElements()
	//===================================================================
	{
		return it.hasNext();
	}
	//===================================================================
	public Object nextElement()
	//===================================================================
	{
		Map.MapEntry me = (Map.MapEntry)it.next();
		if (me == null) return null;
		return me.getValue();
	}

	//##################################################################
	}
	//##################################################################
	//##################################################################
	class HashKeyEnumeration implements Enumeration{
	//##################################################################
	Iterator it;
	HashKeyEnumeration()
	{
		it = entries();
	}
	//===================================================================
	public boolean hasMoreElements()
	//===================================================================
	{
		return it.hasNext();
	}
	//===================================================================
	public Object nextElement()
	//===================================================================
	{
		Map.MapEntry me = (Map.MapEntry)it.next();
		if (me == null) return null;
		return me.getKey();
	}

	//##################################################################
	}
	//##################################################################

	//##################################################################
	class HashEntryIterator extends IteratorEnumerator{
	//##################################################################

	int index;
	HashEntry nextEntry;

	//-------------------------------------------------------------------
	void findNext()
	//-------------------------------------------------------------------
	{
		for(index++; index < table.length; index++){
			if (table[index] != null){
				nextEntry = table[index];
				return;
			}
		}
		nextEntry = null;
	}
	//-------------------------------------------------------------------
	HashEntryIterator() {index = -1; findNext();}
	//-------------------------------------------------------------------
	//===================================================================
	public boolean hasNext()
	//===================================================================
	{
		return nextEntry != null;
	}
	//===================================================================
	public Object next()
	//===================================================================
	{
		if (nextEntry == null) return null;
		HashEntry n = nextEntry;
		nextEntry = null;
		if (n.next != null) nextEntry = n.next;
		else findNext();
		return n;
	}

	//##################################################################
	}
	//##################################################################

	//##################################################################
	class HashEntry implements Map.MapEntry {
	//##################################################################

	Object value, key;
	HashEntry next;
	int code;

	public Object setValue(Object value) {Object old = this.value; this.value = value; return old;}
	public Object getValue() {return value;}
	public Object getKey() {return key;}

	//===================================================================
	public boolean equals(Object other)
	//===================================================================
	{
		if (!(other instanceof Map.MapEntry)) return super.equals(other);
		Map.MapEntry e1 = this;
		Map.MapEntry e2 = (Map.MapEntry)other;
		return
     (e1.getKey()==null ?
      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
     (e1.getValue()==null ?
      e2.getValue()==null : e1.getValue().equals(e2.getValue()));
 }
	//===================================================================
	public int hashCode()
	//===================================================================
	{
		return
     (key==null   ? 0 : key.hashCode()) ^
     (value==null ? 0 : value.hashCode());
 	}

	//##################################################################
	}
	//##################################################################


//##################################################################
}
//##################################################################

