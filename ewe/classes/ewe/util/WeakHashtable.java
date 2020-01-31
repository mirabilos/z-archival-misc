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
import ewe.reflect.WeakReference;
/**
A WeakHashtable is used to store data that may be garbage collected. The keys for
the data are stored in the hashtable normally, but the data is stored via a weak reference.
**/
//##################################################################
public class WeakHashtable{
//##################################################################

protected Hashtable table;

/**
 * Set the interval between cache cleaning in milliseconds.
 * @param interval the interval in milliseconds. If this is less 1 then the default
 * value of 60000 (1 minute) will be used.
 * This applies to both WeakCache and WeakHashtable objects.
 */
//===================================================================
public static void setCleanInterval(long interval)
//===================================================================
{
	WeakCache.setCleanInterval(interval);
}
//===================================================================
public WeakHashtable(int initialCapacity,float loadFactor)
//===================================================================
{
	table = new Hashtable(initialCapacity,loadFactor);
	WeakCache.startCleanThread(this);
}
//===================================================================
public WeakHashtable(int initialCapacity)
//===================================================================
{
	table = new Hashtable(initialCapacity);
	WeakCache.startCleanThread(this);
}
//===================================================================
public WeakHashtable()
//===================================================================
{
	table = new Hashtable();
	WeakCache.startCleanThread(this);
}
/**
 * Put data into the table with the specified key.
 * @param key the key to associate with the data.
 * @param data the data that will be stored weakly in the table.
 */
//===================================================================
public void put(Object key, Object data)
//===================================================================
{
	table.put(key,new WeakReference(data));
}
/**
 * Return the object associated with the key. If none exists, or if the
 * data object has been garbage collected, it returns null.
 * @param key the key to search on.
 * @return the object associated with the key or null if none exists or if
 * the data has been garbage collected.
 */
//===================================================================
public Object get(Object key)
//===================================================================
{
	WeakReference wr = (WeakReference)table.get(key);
	if (wr == null) return null;
	Object ret = wr.get();
	if (ret == null) table.remove(key);
	return ret;
}
/**
* Remove the entry with the specified key.
**/
//===================================================================
public void remove(Object key)
//===================================================================
{
	table.remove(key);
}
/**
Remove all data in the table.
**/
//===================================================================
public void clear()
//===================================================================
{
	table.clear();
}

private static Vector toRemove;

/**
Remove all entries that have been garbage collected already.
**/
//===================================================================
public void clean()
//===================================================================
{
	if (toRemove == null) toRemove = new Vector();
	for (Iterator it = table.entries(); it.hasNext();){
		Map.MapEntry me = (Map.MapEntry)it.next();
		WeakReference wr = (WeakReference)me.getValue();
		if (wr.get() == null) toRemove.add(me.getKey());
	}
	for (int i = 0; i<toRemove.size(); i++)
		table.remove(toRemove.get(i));
}
/**
 * Return the number of keys in the WeakHashtable.
 */
//===================================================================
public int size()
//===================================================================
{
	return table.size();
}
/**
* Return an Enumeration on all the keys in the WeakHashtable.
**/
//===================================================================
public Enumeration keys()
//===================================================================
{
	return table.keys();
}
//##################################################################
}
//##################################################################

