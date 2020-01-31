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
package ewe.database;
import ewe.io.IOException;
import ewe.sys.Handle;
import ewe.util.ObjectFinder;
import ewe.util.IntArray;
import ewe.util.Comparer;
import ewe.util.RangeList;
import ewe.sys.HandleStoppedException;
import ewe.sys.Lock;
import ewe.util.Utils;
import ewe.util.EventDispatcher;
import ewe.util.Range;

/**
* An EntriesView is a view into a FoundEntries object while a FoundEntries object is a view into a database.
* An EntriesView is effectively a set of indexes that identify a set of entries in the associated
* FoundEntries. An EntriesView is a better object to use when displaying or manipulating a set
* of DatabaseEntries because you can re-order an EntriesView any way you wish without affecting
* the sorted order of its source FoundEntries. Then, re-sorting the EntriesView becomes a simple
* task because it simply involves sorting the indexes within the view (which can be done
* very quickly since it does not need to access the database to do this).
**/
//##################################################################
public class EntriesView{
//##################################################################

FoundEntries fe;
private IntArray indexes = new IntArray();
private int sortState = 1;
private EventDispatcher dispatcher;
private long currentState;
public boolean allInclusive;

private boolean usingLookupMode;

/**
This method tells the database that it will be used for read-only lookups and that it can
close its underlying file if it wishes - and later re-open it when lookups are done without
re-reading the database info.<p>
Not all Databases may support this and if it does not, it will return false - although normal
database operations will not be affected. In other words - if this method returns false, the
application can continue as if it returned true - but its operations will not be optimized
in this way.
 * @return true if this optimization is enabled, false if not.
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public boolean enableLookupMode() throws IOException
//===================================================================
{
	if (usingLookupMode) return true;
	return usingLookupMode = fe.getDatabase().enableLookupMode();
}
/**
 * This is used with enableLookupMode() - it tells the database that data is about to be read in
 * and if the underlying file is closed - then it should be re-opened and kept open until closeLookup()
 * is called. Each call to openLookup() should have a corresponding call to closeLookup().
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public void openLookup() throws IOException
//===================================================================
{
	if (!usingLookupMode) return;
	fe.getDatabase().openLookup();
}
/**
 * This is used with enableLookupMode() - it tells the database that data reading is complete
 * and the underlying file may be closed.
 * Each call to openLookup() should have a corresponding call to closeLookup().
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public void closeLookup() throws IOException
//===================================================================
{
	if (!usingLookupMode) return;
	fe.getDatabase().closeLookup();
}
/**
* Checks if the indexes in the EntriesView are sorted as per the sort
* criteria used by the source FoundEntries. The sorted state of the EntriesView
* can change as data is added or edited in it.
**/
//===================================================================
public boolean isSorted(boolean descending)
//===================================================================
{
	if (descending && sortState == -1) return true;
	else if (!descending && sortState == 1) return true;
	return false;
}
/**
* Get the FoundEntries for the view.
**/
//===================================================================
public FoundEntries getFoundEntries()
//===================================================================
{
	return fe;
}
/**
* Create a new EntriesView for a FoundEntries, but which initially contains
* no data.
**/
//===================================================================
public EntriesView(FoundEntries fe)
//===================================================================
{
	this.fe = fe;
	sortState = 1;
}
/**
* Get the indexes of the entries in the FoundEntries in this view.
**/
//===================================================================
public int[] getIndexes()
//===================================================================
{
	return indexes.toIntArray();
}
/**
 * Append all the indexes stored in this EntriesView into an IntArray.
 * @param destination The destination IntArray. If it is null a new one is created and returned.
 * @return the destination IntArray or a new IntArray.
 */
//===================================================================
public IntArray getIndexes(IntArray destination)
//===================================================================
{
	if (destination == null) destination = new IntArray();
	destination.append(indexes.data,0,indexes.length);
	return destination;
}
/**
 * Clear the EntriesView. It will then contain no indexes within it.
 */
//===================================================================
public void clear()
//===================================================================
{
	indexes.clear();
	sortState = 1;
}
/**
* Sort the indexes in the EntriesView. This is a fast sort that only sorts indexes without
* having to refer to the data within the database.
* @param descending true if you want it sorted in descending order.
*/
//===================================================================
public void sort(boolean descending)
//===================================================================
{
	if (!isSorted(descending)) Utils.sort(indexes.data,indexes.length,null,descending);
	sortState = descending ? -1 : 1;
}
//-------------------------------------------------------------------
private boolean appendRange(int startIndex,int endIndex)
//-------------------------------------------------------------------
{
	int[] newRange = new int[endIndex-startIndex+1];
	Utils.getIntSequence(newRange,0,startIndex,1,newRange.length);
	indexes.append(newRange,0,newRange.length);
	return true;
}
//-------------------------------------------------------------------
private void append(int index)
//-------------------------------------------------------------------
{
	indexes.append(index);
	sortState = 0;
	change();
	dispatchEvent(DatabaseEvent.ENTRY_ADDED);
}
private IntArray myRanges;
/**
 * Add a range of indexes to the EntriesView. This will place the view in an unsorted state.
 * @param startIndex The first index into the FoundEntries.
 * @param endIndex The last index into the FoundEntries, which must be greater than or equal to startIndex.
 */
//===================================================================
public void addRange(int startIndex, int endIndex)
//===================================================================
{
	sort(false);
	boolean wasAdded = false;
	if (startIndex < 0) startIndex = 0;
	if (endIndex >= fe.size()) endIndex = fe.size()-1;
	if (startIndex > endIndex) return;
	myRanges = toRanges(myRanges);
	for (int i = 0; i<myRanges.length; i+=2){
		int s = myRanges.data[i];
		int e = s+myRanges.data[i+1]-1;
		if (s > startIndex){
			int last = s-1;
			if (last > endIndex) last = endIndex;
			wasAdded = appendRange(startIndex,last);
			startIndex = s;
		}
		if (startIndex > endIndex) break;
		if (s <= startIndex && e >= startIndex)
			startIndex = e+1; // Confirm this. July 2004
		if (startIndex > endIndex) break;
	}
	if (startIndex <= endIndex) wasAdded = appendRange(startIndex,endIndex);
	if (wasAdded){
		change();
		sortState = 0;
		dispatchEvent(DatabaseEvent.ENTRY_ADDED);
	}
}
/**
 * Add a range of indexes from the EntriesView. The view is first sorted in normal order before this
 * is done to allow for effecient range removal.
 * @param startIndex The first index into the FoundEntries.
 * @param endIndex The last index into the FoundEntries, which must be greater than or equal to startIndex.
 */
//===================================================================
public void removeRange(int startIndex,int endIndex)
//===================================================================
{
	sort(false);
	boolean wasRemoved = false;
	if (startIndex < 0) startIndex = 0;
	if (endIndex >= fe.size()) endIndex = fe.size()-1;
	if (startIndex > endIndex) return;
	for (int i = 0; i<indexes.length && indexes.data[i]<=endIndex; i++){
		if (indexes.data[i] >= startIndex && indexes.data[i] <= endIndex){
			wasRemoved = true;
			int j = i+1;
			for (; indexes.data[j] < endIndex && j<indexes.length; j++)
				;
			int toMove = indexes.length-j;
			if (toMove != 0) ewe.sys.Vm.copyArray(indexes.data,j,indexes.data,i,toMove);
			indexes.length -= (j-i);
			break;
		}
	}
	if (wasRemoved){
		change();
		dispatchEvent(DatabaseEvent.ENTRY_DELETED);
	}
}
/**
* Add all the indexes - sorting this view before the add is done.
**/
//===================================================================
public void includeAll(IntArray toAdd)
//===================================================================
{
	sort(false);
	Utils.sort(toAdd.data,toAdd.length,null,false);
	int max = indexes.length;
	int last = -1;
	int size = fe.size();
	for (int i = 0, t = 0; t < toAdd.length; t++){
		int a = toAdd.data[t];
		if (a == last) continue;
		if (a < 0 || a >= size) break;
		if (i < max){
			if (indexes.data[i] > a) {
				indexes.add(a);
				last = a;
			}
			else if (indexes.data[i] < a) {
				t--;
				i++;
			}
		}else{
			indexes.add(a);
			last = a;
		}
	}
	change();
	sortState = 0;
}

private IntArray searcher;
private Range range;

Lock lock = new Lock();

//-------------------------------------------------------------------
private EntriesView search(Handle h,final ObjectFinder finder,final EntrySelector selector)
throws IOException
//-------------------------------------------------------------------
{
	lock.synchronize(); try{
		if (usingLookupMode) openLookup();
		try{
			if (finder == null && selector.canSearch(fe)){
				range = fe.findRange(h,selector,range);
				if (range == null) return null;
				if (range.first >= 0)
					addRange(range.first,range.last);
				return this;
			}
			if (searcher == null) searcher = new IntArray();
			searcher.clear();
				IntArray got = finder == null ? fe.findAll(h,selector,searcher) : fe.filterAll(h,finder,searcher);
				if (got == null) return null;
			includeAll(searcher);
			return this;
		}finally{
			if (usingLookupMode) closeLookup();
		}
	}finally{lock.release();}
}
//-------------------------------------------------------------------
private Handle search(final ObjectFinder finder,final EntrySelector selector)
//-------------------------------------------------------------------
{
	return new ewe.sys.TaskObject(){
		protected void doRun(){
			handle.resetTime("Searching");
			try{
				handle.returnValue = search(handle,finder,selector);
				if (handle.returnValue == null){
					handle.set(Handle.Aborted);
					return;
				}
				handle.setProgress(1);
				handle.set(Handle.Succeeded);
			}catch(Exception e){
				handle.failed(e);
			}
		}
	}.startTask();
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView.
* @param h An optional handle that can be used to monitor and stop the search.
* @param selector an object that is used to select which entries will be included in the results.
* @return <b>this</b> EntriesView with the results of the search added to the indexes already in this
* view. If the search was aborted null will be returned.
* @exception IOException
* @exception IllegalArgumentException
* @exception IllegalStateException
*/
//===================================================================
public EntriesView search(Handle h, EntrySelector selector) throws IOException, IllegalArgumentException, IllegalStateException
//===================================================================
{
	return search(h,null,selector);
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView. This method
* will always cause a filter to be performed instead of a binary search.
* @param h An optional handle that can be used to monitor and stop the search.
* @param finder an object that is used to select which entries will be included in the results.
* @return <b>this</b> EntriesView with the results of the search added to the indexes already in this
* view. If the search was aborted null will be returned.
* @exception IOException
* @exception IllegalArgumentException
* @exception IllegalStateException
 */
//===================================================================
public EntriesView search(Handle h, ObjectFinder finder) throws IOException, IllegalArgumentException, IllegalStateException
//===================================================================
{
	return search(h,finder,null);
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView.
* @param h An optional handle that can be used to monitor and stop the search.
* @param searchData data used for the primary sort fields when searching.
* @param hasWildCards set this true if the data contain wild card specifications (e.g. '*' characters for Strings).
* @return <b>this</b> EntriesView with the results of the search added to the indexes already in this
* view. If the search was aborted null will be returned.
* @exception IOException
* @exception IllegalArgumentException
* @exception IllegalStateException
*/
//===================================================================
public EntriesView search(Handle h, Object searchData, boolean hasWildCards) throws IOException, IllegalArgumentException, IllegalStateException
//===================================================================
{
	return search(h,null,fe.getEntrySelector(searchData,hasWildCards));
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView.
* @param h An optional handle that can be used to monitor and stop the search.
* @param searchData data used for the primary sort fields when searching.
* @return <b>this</b> EntriesView with the results of the search added to the indexes already in this
* view. If the search was aborted null will be returned.
* @exception IOException
* @exception IllegalArgumentException
* @exception IllegalStateException
*/
//===================================================================
public EntriesView search(Handle h, Object searchData) throws IOException, IllegalArgumentException, IllegalStateException
//===================================================================
{
	return search(h,searchData,true);
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView, using
* a separate thread to do the searching.
* @param selector an EntrySelector for selecting the entries.
* @return a Handle that can be used to monitor and abort the process. When the Handle reports
* success then the found entries will have been added to this EntriesView.
*/
//===================================================================
public Handle search(EntrySelector selector)
//===================================================================
{
	return search((ObjectFinder)null,selector);
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView, using
* a separate thread to do the searching.
* @param finder an ObjectFinder for selecting the entries.
* @return a Handle that can be used to monitor and abort the process. When the Handle reports
* success then the found entries will have been added to this EntriesView.
*/
//===================================================================
public Handle search(ObjectFinder finder)
//===================================================================
{
	return search(finder,null);
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView, using
* a separate thread to do the searching.
* @param searchData data used for the primary sort fields when searching.
* @param hasWildCards set this true if the data contain wild card specifications (e.g. '*' characters for Strings).
* @return a Handle that can be used to monitor and abort the process. When the Handle reports
* success then the found entries will have been added to this EntriesView.
*/
//===================================================================
public Handle search(Object searchData, boolean hasWildCards)
//===================================================================
{
	return search((ObjectFinder)null,fe.getEntrySelector(searchData,hasWildCards));
}
/**
* Search for entries in the associated FoundEntries and add them to this EntriesView, using
* a separate thread to do the searching.
* @param searchData data used for the primary sort fields when searching.
* @return a Handle that can be used to monitor and abort the process. When the Handle reports
* success then the found entries will have been added to this EntriesView.
*/
//===================================================================
public Handle search(Object searchData)
//===================================================================
{
	return search(searchData,true);
}
/**
* Filter all entries in the associated FoundEntries and add them to this EntriesView.
* @param h An optional handle that can be used to monitor and stop the search.
* @param searchData data used for the primary sort fields when searching.
* @return <b>this</b> EntriesView with the results of the search added to the indexes already in this
* view. If the search was aborted null will be returned.
* @exception IOException
* @exception IllegalArgumentException
* @exception IllegalStateException
*/
//===================================================================
public EntriesView filter(Handle h, Object searchData) throws IOException, IllegalArgumentException, IllegalStateException
//===================================================================
{
	return filter(h,searchData,true);
}
/**
* Filter all entries in the associated FoundEntries and add them to this EntriesView.
* @param h An optional handle that can be used to monitor and stop the search.
* @param searchData data used for the primary sort fields when searching.
* @param hasWildCards set this true if the data contain wild card specifications (e.g. '*' characters for Strings).
* @return <b>this</b> EntriesView with the results of the search added to the indexes already in this
* view. If the search was aborted null will be returned.
* @exception IOException
* @exception IllegalArgumentException
* @exception IllegalStateException
 */
//===================================================================
public EntriesView filter(Handle h, Object searchData, boolean hasWildCards) throws IOException, IllegalArgumentException, IllegalStateException
//===================================================================
{
	return search(h,fe.getEntrySelector(searchData,hasWildCards).toObjectFinder(),null);
}
/**
* Filter all entries in the associated FoundEntries and add them to this EntriesView, using
* a separate thread to do the filtering.
* @param searchData data used for the primary sort fields when searching.
* @param hasWildCards set this true if the data contain wild card specifications (e.g. '*' characters for Strings).
* @return a Handle that can be used to monitor and abort the process. When the Handle reports
* success then the found entries will have been added to this EntriesView.
*/
//===================================================================
public Handle filter(Object searchData, boolean hasWildCards) throws IllegalArgumentException, IllegalStateException
//===================================================================
{
	return search(fe.getEntrySelector(searchData,hasWildCards).toObjectFinder(),null);
}
/**
* Filter all entries in the associated FoundEntries and add them to this EntriesView, using
* a separate thread to do the filtering.
* @param searchData data used for the primary sort fields when searching.
* @return a Handle that can be used to monitor and abort the process. When the Handle reports
* success then the found entries will have been added to this EntriesView.
*/
//===================================================================
public Handle filter(Object searchData) throws IllegalArgumentException, IllegalStateException
//===================================================================
{
	return filter(searchData,true);
}
/**
* Refresh this EntriesView to view the entire source FoundEntries.
**/
//===================================================================
public void getFullFiew()
//===================================================================
{
	clear();
	fe.getFullView(this);
}
/**
 * Return the number of indexes in the view.
 */
//===================================================================
public int size()
//===================================================================
{
	return indexes.length;
}
/**
 * Get the DatabaseEntry at the specified index in the view.
 * @param indexInView the index of the entry in this view.
 * @param de an optional destination DatabaseEntry.
 * @return the destination DatabaseEntry or a new entry if de was null.
 * @exception IOException if there was an error reading the entry.
 */
//===================================================================
public DatabaseEntry get(int indexInView, DatabaseEntry de)
throws IOException
//===================================================================
{
	if (usingLookupMode) fe.getDatabase().openLookup();
	try{
		return fe.get(index(indexInView),de);
	}finally{
		if (usingLookupMode) fe.getDatabase().closeLookup();
	}
}
/**
 * Get the DatabaseEntry at the specified index in the view as a data object.
 * @param indexInView the index of the entry in this view.
 * @param dest an optional destination object.
 * @return the destination Object or a new object if dest was null.
 * @exception IOException if there was an error reading the entry.
 */
//===================================================================
public Object getData(int indexInView, Object dest)
throws IOException
//===================================================================
{
	if (usingLookupMode) fe.getDatabase().openLookup();
	try{
		return fe.getData(index(indexInView),dest);
	}finally{
		if (usingLookupMode) fe.getDatabase().closeLookup();
	}
}
/**
 * Get the DatabaseEntry at the specified index in the view.
 * @param indexInView the index of the entry in this view.
 * @return a new DatabaseEntry.
 * @exception IOException if there was an error reading the entry.
 */
//===================================================================
public DatabaseEntry get(int indexInView)
throws IOException
//===================================================================
{
	if (usingLookupMode) fe.getDatabase().openLookup();
	try{
		return fe.get(index(indexInView));
	}finally{
		if (usingLookupMode) fe.getDatabase().closeLookup();
	}
}
/**
 * Get the DatabaseEntry at the specified index in the view as a data object.
 * @param indexInView the index of the entry in this view.
 * @return a new data Object.
 * @exception IOException if there was an error reading the entry.
 */
//===================================================================
public Object getData(int indexInView)
throws IOException
//===================================================================
{
	if (usingLookupMode) fe.getDatabase().openLookup();
	try{
		return fe.getData(index(indexInView));
	}finally{
		if (usingLookupMode) fe.getDatabase().closeLookup();
	}
}
/**
* Change the database entry at the specified index.
* @param indexInView the index of the entry in this view.
* @param de a database entry object containing the new data.
 * @exception IOException if there was an error writing the entry.
*/
//===================================================================
public void set(int indexInView, DatabaseEntry de)
throws IOException
//===================================================================
{
	fe.set(index(indexInView),de);
}
/**
* Change the database entry at the specified index.
* @param indexInView the index of the entry in this view.
* @param data an object containing the new data.
 * @exception IOException if there was an error writing the entry.
*/
//===================================================================
public void setData(int indexInView, Object data)
throws IOException
//===================================================================
{
	fe.setData(index(indexInView),data);
}
/**
Return a new DatabaseEntry for use with this database.
**/
//===================================================================
public DatabaseEntry getNew()
//===================================================================
{
	return fe.getNew();
}
/**
 * Store the DatabaseEntry at the specified index without modifying any of
 * its special synchronization fields.
 * @param indexInView the index of the entry in this view.
 * @param de a database entry object containing the new data.
 * @exception IOException if there was an error writing the entry.
 */
//===================================================================
public void store(int indexInView, DatabaseEntry de)
throws IOException
//===================================================================
{
	fe.store(index(indexInView),de);
}
/**
 * Add a new DatabaseEntry to the view (it is added to the end)
 * and save it in the FoundEntries and database.
 * @param de a DatabaseEntry object holding the data to be saved.
 * @return the index where the new entry was added to the view.
 * @exception IOException
 */
//===================================================================
public int add(DatabaseEntry de)
throws IOException
//===================================================================
{
	int where = fe.add(de);
	if (!allInclusive) append(where);
	return indexes.length-1;
}
/**
 * Add a new record to the view (it is added to the end)
 * and save it in the FoundEntries and database.
 * @param data a data object holding the data to be saved.
 * @return the index where the new entry was added to the view.
 * @exception IOException
 */
//===================================================================
public int addData(Object data)
throws IOException
//===================================================================
{
	int where = fe.addData(data);
	if (!allInclusive) append(where);
	return indexes.length-1;
}
/**
 * Delete the record at the specified index from the database. It will also remove the entry
 * from this EntriesView.
 * @param indexInView the index of the entry in the view.
 * @exception IOException
 */
//===================================================================
public void delete(int indexInView)
throws IOException
//===================================================================
{
	fe.delete(index(indexInView));
	//
	// fe.delete() will prompt a call to change()
	// which will then remove it from this view.
	//
}
private static native void nativeToRanges(int[] source,int length,IntArray dest);
private static native int nativeAdjustIndexes(int[] indexes,int length,int oldIndex,int newIndex);
private static boolean hasNative = false;
/**
* Convert the indexes in this view to a sequence of ranges.
**/
//===================================================================
public IntArray toRanges(IntArray dest)
//===================================================================
{
	return toRanges(indexes.data,indexes.length,dest);
}
//-------------------------------------------------------------------
private static IntArray toRanges(int[] source, int length, IntArray dest)
//-------------------------------------------------------------------
{
	if (dest == null) dest = new IntArray();
	dest.clear();
	if (hasNative) try{
		nativeToRanges(source,length,dest);
		return dest;
	}catch(SecurityException e){
		hasNative = false;
	}catch(UnsatisfiedLinkError er){
		hasNative = false;
	}
	if (length > 0){
		int st = source[0];
		int last = st;
		for (int i = 1; i<length; i++){
			int now = source[i];
			if (now <= last+1) {
				last = now;
				continue;
			}
			dest.append(st);
			dest.append(last-st+1);
			st = last = now;
		}
		dest.append(st);
		dest.append(last-st+1);
	}
	return dest;
}
//-------------------------------------------------------------------
protected void changed(int oldIndex, int newIndex)
//-------------------------------------------------------------------
{
	boolean wasIn = oldIndex == -1 ? false : indexes.indexOf(oldIndex) != -1;
	boolean wasAdded = oldIndex == -1 && allInclusive;
	sortState = 0;
	if (oldIndex != newIndex){
		if (hasNative) try{
			int usedToBe = nativeAdjustIndexes(indexes.data,indexes.length,oldIndex,newIndex);
			if (newIndex == -1 && usedToBe != -1) indexes.removeAtIndex(usedToBe);
		}catch(SecurityException e){
			hasNative = false;
		}catch(UnsatisfiedLinkError er){
			hasNative = false;
		}
		if (!hasNative){
			int[] data = indexes.data;
			int length = indexes.length;
			if (newIndex == -1){
				int old = -1;
				for (int i = 0; i<length; i++)
					if (data[i] == oldIndex) old = i;
					else if (data[i] > oldIndex) data[i]--;
				if (old != -1) indexes.removeAtIndex(old);
			}else if (oldIndex == -1){
				for (int i = 0; i<length; i++)
					if (data[i] >= newIndex) data[i]++;
			}else if (newIndex < oldIndex){
				for (int i = 0; i<length; i++)
					if (data[i] == oldIndex) data[i] = newIndex;
					else if (data[i] >= newIndex && data[i] < oldIndex) data[i]++;
			}else{ //newIndex > oldIndex
				for (int i = 0; i<length; i++)
					if (data[i] == oldIndex) data[i] = newIndex;
					else if (data[i] > oldIndex && data[i] <= newIndex) data[i]--;
			}
		}
	}
	if (allInclusive && oldIndex == -1 && newIndex != -1) append(newIndex);
	if (wasIn) dispatchEvent(newIndex == -1 ? DatabaseEvent.ENTRY_DELETED : DatabaseEvent.ENTRY_CHANGED);
	else if (wasAdded) dispatchEvent(DatabaseEvent.ENTRY_ADDED);
	if (wasIn || wasAdded) change();
}

/**
* Find out the index of the specified entry in this view - if it exists in the view.
* @param de the entry to look for.
* @return the index of the entry in this view or -1 if it is not in this view.
*/
//===================================================================
public int indexInView(DatabaseEntry de)
//===================================================================
{
	int idx = fe.indexOf(de);
	if (idx == -1) return -1;
	return indexInView(idx);
}
/**
 * Determine the index of the database entry in the View, based on its index
 * in the FoundEntries.
 * @param indexInFoundEntries The index of the record in the FoundEntries.
 * @return the index of the entry in this View or -1 if it is not included in the view.
 */
//===================================================================
public int indexInView(int indexInFoundEntries)
//===================================================================
{
	return indexes.indexOf(indexInFoundEntries);
}
/**
* This is the same as index(). It returns the index of an item in
* the FoundEntries, given the index of the item in this view.
**/
//===================================================================
public int indexInFoundEntries(int indexInView)
//===================================================================
{
	return indexes.data[indexInView];
}
/**
* This is the same as indexInFoundEntries(). It returns the index of an item in
* the FoundEntries, given the index of the item in this view.
**/
//===================================================================
public int index(int indexInView)
//===================================================================
{
	return indexes.data[indexInView];
}
/**
 * Include the specified entry in this view, but only if it is already included in its
 * FoundEntries.
 * @param entry the entry to include.
 * @return the index of the included entry in the View or -1 if it was not included
 * because it is not in the FoundEntries.
 */
//===================================================================
public int include(DatabaseEntry entry)
//===================================================================
{
	int idx = fe.indexOf(entry);
	if (idx == -1) return -1;
	return include(idx);
}
/**
 * Include the specified entry in this view.
 * @param indexInFoundEntries the index of the entry in the FoundEntries.
 * @return the index of the included entry in the View or -1 if it was not included
 * because it is not in the FoundEntries.
 */
//===================================================================
public int include(int indexInFoundEntries)
//===================================================================
{
	if (indexInFoundEntries < 0 || indexInFoundEntries >= fe.size()) return -1;
	int iv = indexInView(indexInFoundEntries);
	if (iv != -1) return iv;
	append(indexInFoundEntries);
	return indexes.length-1;
}
/**
 * Include all the entries in the other view in this view.
 * @param other The other EntriesView
* @exception IllegalArgumentException if the other EntriesView is not of the same FoundEntries.
*/
//===================================================================
public void include(EntriesView other) throws IllegalArgumentException
//===================================================================
{
	if (other == null) return;
	if (fe != other.fe) throw new IllegalArgumentException();
	other.sort(false);
	IntArray r = other.toRanges(null);
	for (int i = 0; i<r.length; i += 2)
		addRange(r.data[i],r.data[i]+r.data[i+1]-1);
}
/**
 * Exclude all the entries in the other view from this view.
 * @param other The other EntriesView.
 * @exception IllegalArgumentException  if the other EntriesView is not of the same FoundEntries.
 */
//===================================================================
public void exclude(EntriesView other) throws IllegalArgumentException
//===================================================================
{
	if (other == null) return;
	if (fe != other.fe) throw new IllegalArgumentException();
	other.sort(false);
	IntArray r = other.toRanges(null);
	for (int i = 0; i<r.length; i += 2)
		removeRange(r.data[i],r.data[i]+r.data[i+1]-1);
}
/**
* Exclude the entry from this view.
**/
//===================================================================
public void exclude(DatabaseEntry entry)
//===================================================================
{
	int idx = fe.indexOf(entry);
	if (idx == -1) return;
	exclude(idx);
}
/**
* Exclude the entry from this view.
**/
//===================================================================
public void exclude(int indexInFoundEntries)
//===================================================================
{
	int idx = indexInView(indexInFoundEntries);
	if (idx == -1) return;
	indexes.removeAtIndex(idx);
	change();
	dispatchEvent(DatabaseEvent.ENTRY_DELETED);
}

/**
 * Exclude the entry form this view.
 * @param indexInView the index of the entry in this view.
 */
//===================================================================
public void excludeAt(int indexInView)
//===================================================================
{
	if (indexInView < 0 || indexInView >= indexes.length) return;
	indexes.removeAtIndex(indexInView);
	change();
	dispatchEvent(DatabaseEvent.ENTRY_DELETED);
}
/**
* Get all the indexes as an arry of ints. This does not sort the view.
**/
//===================================================================
public int[] getIndexesInFoundEntries()
//===================================================================
{
	return indexes.toIntArray();
}
/**
 * Get the EventDispatcher for this view.
 */
//===================================================================
public EventDispatcher getEventDispatcher()
//===================================================================
{
	if (dispatcher == null) dispatcher = new EventDispatcher();
	return dispatcher;
}
//-------------------------------------------------------------------
protected void dispatchEvent(int type)
//-------------------------------------------------------------------
{
	if (dispatcher != null && !dispatcher.isEmpty())
		dispatcher.dispatch(new DatabaseEvent(type,this));
}
/**
 * Get the current changed state of the FoundEntries. You can check
 * if the FoundEntries have been changed later by calling hasChangedSince().
 * The changed state does not have to do with the data stored in the database
 * but rather whether the internal index table has changed. If this FoundEntries
 * is stored in a file then if it has changed since the last state then it would
 * need to be saved again.
 * @return a reference to its current state.
 */
//===================================================================
public long getCurrentState() {return currentState;}
//===================================================================
/**
 * Return if the FoundEntries has changed since the previous state.
 */
//===================================================================
public boolean hasChangedSince(long previousState) {return previousState != currentState;}
//===================================================================
/**
 * Mark the FoundEntries as having been changed in some way.
 */
//===================================================================
public void change(){currentState++;}
//===================================================================
/**
 * Use this if you used enableLookupMode() to get a new empty EntriesView for the same FoundEntries as this view.
 */
//===================================================================
public EntriesView getEmptyView()
//===================================================================
{
	EntriesView ev = fe.getEmptyView();
	ev.usingLookupMode = usingLookupMode;
	return ev;
}

//##################################################################
}
//##################################################################

