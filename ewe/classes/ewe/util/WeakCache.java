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
import ewe.sys.mThread;
/**
This is similar to a Hashtable in that you associate values with keys. However
only weak references are kept to the keys - which means that the key values
can be garbage collected, in which case it will not be possible to access the object
values the keys refer to.<p>
The clean() method explicitly removes entries where the keys have been garbage
collected.<p>
A background thread is created to clean all existing WeakCache objects at regular
intervals. The default interval is 1 minute, but this can be changed by calling
setCleanInterval().
**/
//##################################################################
public class WeakCache{
//##################################################################

Vector refs = new Vector(), data = new Vector();

static void startCleanThread(Object forWho)
{
	if (cleanThread == null){
		caches = new WeakSet();
		cleanThread = new mThread(){
			public void run(){
				while(true){
					try{
						sleep(cleanInterval);
					}catch(Exception e){}
					Object[] all = caches.getRefs();
					for (int i = 0; i<all.length; i++){
						if (all[i] == null) continue;
						if (all[i] instanceof WeakCache)
							((WeakCache)all[i]).clean();
						else if (all[i] instanceof WeakHashtable)
							((WeakHashtable)all[i]).clean();
					}
				}
			}
		};
		cleanThread.start();
	}
	caches.add(forWho);
}
//===================================================================
public WeakCache()
//===================================================================
{
	startCleanThread(this);
}
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
	if (interval <= 0) interval = 60*1000;
	if (cleanThread != null) cleanThread.interrupt();
}
/**
 * Remove all entries for which the key has been garbage collected.
 */
//===================================================================
public void clean()
//===================================================================
{
	for (int i = 0; i<refs.size(); i++){
		WeakReference wr = (WeakReference)refs.get(i);
		if (wr.get() == null){
			refs.removeElementAt(i);
			data.removeElementAt(i);
			i--;
		}
	}
}
private static mThread cleanThread;
private static long cleanInterval = /*60**/1000;
private static WeakSet caches;

//-------------------------------------------------------------------
int indexOf(Object key)
//-------------------------------------------------------------------
{
	if (key == null) return -1;
	for (int i = 0; i<refs.size(); i++){
		WeakReference wr = (WeakReference)refs.get(i);
		if (wr == key) return i;
		Object g = wr.get();
		if (g == key) return i;
		if (g == null){
			refs.removeElementAt(i);
			data.removeElementAt(i);
			i--;
		}
	}
	return -1;
}
//===================================================================
public void put(Object key, Object data)
//===================================================================
{
	if (key == null) throw new NullPointerException();
	int where = indexOf(key);
	if (where != -1) this.data.set(where,data);
	else {
		this.data.add(data);
		refs.add(key instanceof WeakReference ? key : new WeakReference(key));
	}
}
//===================================================================
public Object get(Object key)
//===================================================================
{
	if (key == null) return null;
	int where = indexOf(key);
	return where == -1 ? null : data.get(where);
}
/**
 * Each object returned by the returned Iterator will be a WeakReference.
 * @return an Iterator that iterates through the WeakReferences.
 */
//===================================================================
public Iterator keys()
//===================================================================
{
	return refs.iterator();
}
//##################################################################
}
//##################################################################


