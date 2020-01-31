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
* A Map is a collection that maps a key to a value. A Hashtable is an instance of
* a Map.
**/
//##################################################################
public interface Map{
//##################################################################
/**
 * Provides an Iterator to iterate through all the entries. Each object returned by the
	iterator will be a Map.MapEntry object.
 * @return An Iterator to iterate through all the entries.
 */
public Iterator entries();

/**
 * Get the number of entries in the Map.
 * @return The number of entries in the Map.
 */
public int size();
/**
 * Remove all entries in the Map.
 */
public void clear();
/**
 * Checks if an entry with the specified key is in the Map.
 * @param key The key to look for.
 * @return true if the key is in the Map.
 */
public boolean containsKey(Object key);
/**
 * Checks if at least one entry with the specified value is in the Map.
 * @param value The value to look for.
 * @return true if the value is in the Map at least once.
 */
public boolean containsValue(Object value);
/**
 * Get the object with the specified key.
 * @param key The key to look for.
 * @return The object associated with the key or null if there is no matching key.
 */
public Object get(Object key);
/**
 * Put the object and key in the Map. Any old entry with the same key will be replaced.
 * @param key The key for the object.
 * @param value The object to put
 * @return The same as value.
 */
public Object put(Object key,Object value);
/**
 * Remove the object and key pair from the Map.
 * @param key The key for the Object to replace.
 * @return The value for the key that was removed if it was in the Map.
 */
public Object remove(Object key);

public interface MapEntry{
	public Object setValue(Object value);
	public Object getValue();
	public Object getKey();
	public boolean equals(Object other);
	public int hashCode();
}
//##################################################################
}
//##################################################################


