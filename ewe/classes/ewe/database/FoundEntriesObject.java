/* $MirOS: contrib/hosted/ewe/classes/ewe/database/FoundEntriesObject.java,v 1.2 2008/04/10 16:17:20 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
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
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.sys.Convert;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.sys.Lock;
import ewe.sys.Vm;
import ewe.util.ByteArray;
import ewe.util.Comparer;
import ewe.util.EventDispatcher;
import ewe.util.IntArray;
import ewe.util.ObjectFinder;
import ewe.util.Range;
import ewe.util.Utils;
import ewe.util.WeakSet;
//##################################################################
public abstract class FoundEntriesObject implements FoundEntries{
//##################################################################
protected IntArray ids;
protected Database database;
protected DatabaseEntryObject buffer, searcher;
//protected DatabaseIndexObject index;

protected boolean sortState;
protected int mySortID;
protected int[] sortCriteria;
protected Comparer sortComparer;
//protected boolean sortIsDescending;
protected boolean addNewOnesFirst = false;
//..................................................................
// Don't copy.
//..................................................................
private long currentState = 0;
protected Lock lock = new Lock();
protected boolean isAllInclusive;
protected WeakSet views;
protected EventDispatcher dispatcher;
//.....................................................
protected String indexName;
protected OutputStream indexRecorder;
//-------------------------------------------------------------------
protected FoundEntriesObject(Database database)
//-------------------------------------------------------------------
{
	this.database = database;
	buffer = (DatabaseEntryObject)getNew();
	searcher = (DatabaseEntryObject)getNew();
}

protected boolean needCompact = false;

/**
 * This is used for debugging only.
 *
 */
/*
public boolean validateEntriesData(String text)
{
	if (ids.data == null || ids.data.length < ids.length){
		if (text == null) text = "";
		DatabaseManager.logError(text+" - Bad data length: Data = "+(ids.data == null ? "null" : Convert.toString(ids.data.length))+"\n at:\n"+Vm.getStackTrace(new Exception()));
		return false;
	}
	return true;
}
*/

//===================================================================
public boolean validateEntries()
//===================================================================
{
	//if (!validateEntriesData("validateEntries")) return false;
	int idx = Utils.indexOf(ids.data,0,0,ids.length,false);
	if (idx < 0 || idx >= ids.length) return true;
	DatabaseManager.logError("Zero index at: "+idx+" in length: "+ids.length+"\n"+Vm.getStackTrace(new Exception()));
	return false;
}

/**
 * Get the database associated with the FoundEntries.
 */
//===================================================================
public Database getDatabase() {return database;}
//===================================================================
/**
* Create a new DatabaseEntry for this FoundEntries.
**/
//===================================================================
public DatabaseEntry getNew()
//===================================================================
{
	return database.getNewData();
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
//===================================================================
public void setAddNewEntriesFirst(boolean addFirst)
//===================================================================
{
	addNewOnesFirst = addFirst;
}

//-------------------------------------------------------------------
protected abstract boolean doSortMe(Handle h,Comparer c) throws IOException;
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected void setSort(int sortID,Comparer c)
//-------------------------------------------------------------------
{
	mySortID = sortID;
	try {
		if (c == null && sortID != 0)
			c = new StandardEntryComparer(database,sortID);
	} catch (Exception e) {
		e.printStackTrace();
		c = null;
	}
	sortComparer = c;
	sortCriteria = c instanceof StandardEntryComparer ? ((StandardEntryComparer)sortComparer).getCriteria() : null;
	sortState = c != null;
}
//-------------------------------------------------------------------
protected boolean sortMe(Handle h,Comparer c,int sortID) throws IOException
//-------------------------------------------------------------------
{
	if (h == null) h = new Handle();
	try {
		if (c == null)
			c = new StandardEntryComparer(database,sortID);
	} catch (Exception e) {
		e.printStackTrace();
		c = null;
	}
	if (!doSortMe(h,c)) return false;
	mySortID = sortID;
	sortCriteria = c instanceof StandardEntryComparer ? ((StandardEntryComparer)c).getCriteria() : null;
	sortComparer = c;
	//sortIsDescending = descending;
	sortState = true;
	return true;
}
//-------------------------------------------------------------------
private FoundEntriesObject sort(Handle h, Comparer c, int sortID) throws IOException
//-------------------------------------------------------------------
{
	FoundEntriesObject fe = (FoundEntriesObject)getCopy();
	if (!fe.sortMe(h,c,sortID)) return null;
	return fe;
}
//-------------------------------------------------------------------
private Handle sort(final Comparer c, final int sortID)
//-------------------------------------------------------------------
{
	return new ewe.sys.TaskObject(){
		protected void doRun(){
			handle.resetTime("Sorting");
			try{
				handle.returnValue = sort(handle,c,sortID);
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
//===================================================================
public Handle sort(int sortID) throws IllegalArgumentException
//===================================================================
{
	try {
		return sort(new StandardEntryComparer(database,sortID),sortID);
	} catch (Exception e) {
		throw new IllegalArgumentException("cought unexpected" +
		    " exception, shouldn't happen", e);
	}
}
//===================================================================
public Handle sort(int[] criteria)
//===================================================================
{
	try {
		return sort(new StandardEntryComparer(database,criteria),0);
	} catch (Exception e) {
		throw new IllegalArgumentException("cought unexpected" +
		    " exception, shouldn't happen", e);
	}
}
//===================================================================
public Handle sort(Comparer comparer)
//===================================================================
{
	return sort(comparer,0);
}

//===================================================================
public FoundEntries sort(Handle h,int sortID) throws IOException
//===================================================================
{
	try {
		return sort(h,new StandardEntryComparer(database,sortID),sortID);
	} catch (Exception e) {
		throw new IllegalArgumentException("cought unexpected" +
		    " exception, shouldn't happen", e);
	}
}
//===================================================================
public FoundEntries sort(Handle h,int[] criteria) throws IOException
//===================================================================
{
	try {
		return sort(h,new StandardEntryComparer(database,criteria),0);
	} catch (Exception e) {
		throw new IllegalArgumentException("cought unexpected" +
		    " exception, shouldn't happen", e);
	}
}
//===================================================================
public FoundEntries sort(Handle h,Comparer comparer) throws IOException
//===================================================================
{
	return sort(h,comparer,0);
}

//===================================================================
public boolean isSorted()
//===================================================================
{
	return sortState;
}
//===================================================================
public int getSortEntry()
//===================================================================
{
	return mySortID;
}
//===================================================================
public int[] getSortCriteria()
//===================================================================
{
	return sortCriteria;
}
//===================================================================
public Comparer getSortComparer()
//===================================================================
{
	return sortComparer;
}
/*
//===================================================================
public boolean getSortIsDescending()
//===================================================================
{
	return sortIsDescending;
}
*/
//===================================================================
public int size()
//===================================================================
{
	return ids == null ? 0 : ids.length;
}

private void logIndexError(int index)
{
	String out = "Error in FoundEntriesObject.get(int,DatabaseEntry).\n";
	out += "Item: "+index+" chosen. ids.length = "+ids.length+" data.length = "
	+(ids.data == null ? "(null)" : (""+ids.data.length));
	if (ids.data != null && index >= 0 && index < ids.data.length)
		out += "\nValue at that location: "+ids.data[index];
	DatabaseManager.logError(out);
	/*
	DatabaseManager.logError("Index of 0: "+Utils.indexOf(ids.data,0,0,ids.length,false));
	validateEntries();
	*/
	throw new IndexOutOfBoundsException();
}
/**
* Get the data at the specified index.
* @param index The index to read from.
* @param data a destination DatabaseEntry, which can be null.
* @return the destination DatabaseEntry or a newly created one.
* @exception IOException on error.
*/
//===================================================================
public DatabaseEntry get(int index, DatabaseEntry data) throws IOException
//===================================================================
{
	if (index >= ids.length || index < 0)
		logIndexError(index);
	else if (ids.data == null || index >= ids.data.length || ids.data[index] == 0)
		logIndexError(index);
	if (index >= ids.length || index < 0) throw new IndexOutOfBoundsException();
	if (data == null) data = getNew();
	data.clearDataAndSpecialFields();
	doLoad(index,(DatabaseEntryObject)data);
	return data;
}
/**
* Get the data at the specified index, creating a new DatabaseEntry to get it from.
* @param index The index to read from.
* @param data a destination DatabaseEntry, which can be null.
* @return the destination DatabaseEntry or a newly created one.
* @exception IOException on error.
*/
//===================================================================
public DatabaseEntry get(int index) throws IOException
//===================================================================
{
	return get(index,null);
}

/**
 * Get the data object from the specified index.
 * @param index the index in the FoundEntries object.
 * @param destination a destination object. If this is null a new one will be created if
 * possible.
 * @return the destination or new object.
 * @exception IllegalArgumentException if the destination object is not the right type.
  * @exception IllegalStateException if a new object was requested but could not be created.
  * @exception IOException if there is an error reading the data.
 */
//===================================================================
public Object getData(int index, Object destination) throws IllegalStateException,IllegalArgumentException, IOException
//===================================================================
{
	lock.synchronize(); try{
		get(index, buffer);
		return buffer.getData(destination);
	}finally{lock.release();}
}
/**
 * Get the data object from the specified index.
 * @param index the index in the FoundEntries object.
 * @return the data in a new Object.
  * @exception IllegalStateException if a new object could not be created.
  * @exception IOException if there is an error reading the data.
 */
//===================================================================
public Object getData(int index) throws IllegalStateException, IOException
//===================================================================
{
	return getData(index,null);
}
/**
 * Set the data at the specified index using the data object.
 * This will maintain the sort order.
 * @param index the index in the FoundEntries object.
 * @param data the data object.
 * @return the new index of the data.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IOException if there is an error writing the data.
*/
//===================================================================
public int setData(int index, Object data) throws IllegalArgumentException, IOException
//===================================================================
{
	lock.synchronize(); try{
		get(index,buffer);
		buffer.setData(data);
		int ret = set(index,buffer);
		return ret;
	}finally{lock.release();}
}
/**
 * Set the data at the specified index using the data object. This does not maintain the
 * sort order.
 * @param index the index in the FoundEntries object.
 * @param data the data object.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IOException if there is an error writing the data.
 */
	/*
//===================================================================
public void setDataInPlace(int index, Object data) throws IllegalArgumentException, IOException
//===================================================================
{
	lock.synchronize(); try{
		buffer.setData(data);
		set(index,buffer);
	}finally{lock.release();}
}
*/
/**
 * Add a new entry using the data object. This maintains the
 * sort order.
 * @param data the data object.
 * @return the index of the new entry.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IOException if there is an error writing the data.
 */
//===================================================================
public int addData(Object data) throws IllegalArgumentException, IOException
//===================================================================
{
	lock.synchronize(); try{
		buffer.clearDataAndSpecialFields();
		buffer.setData(data);
		return add(buffer);
	}finally{lock.release();}
}
/**
 * Add a new entry using the data object to the end of the FoundEntries.
 * This does not maintains the sort order.
 * @param data the data object.
 * @return the index of the new entry.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IOException if there is an error writing the data.
 */
	/*
//===================================================================
public int appendData(Object data) throws IllegalArgumentException, IOException
//===================================================================
{
	lock.synchronize(); try{
		buffer.setData(data);
		return append(buffer);
	}finally{lock.release();}
}
*/
//===================================================================
public int set(int index,DatabaseEntry entry) throws IOException
//===================================================================
{
/*
	if (!sortState){
		set(index,entry);
		return index;
	}
*/
	DatabaseEntryObject e = (DatabaseEntryObject)entry;
	lock.synchronize(); try{
		e.modifyingInside = this;
		return doChange(index,e);
	}finally{e.modifyingInside = null; lock.release();}
}
//===================================================================
public int store(int index,DatabaseEntry entry) throws IOException
//===================================================================
{
	DatabaseEntryObject e = (DatabaseEntryObject)entry;
	lock.synchronize(); try{
		e.modifyingInside = this;
		return doStore(index,e);
	}finally{e.modifyingInside = null; lock.release();}
}

/**
* This saves changes to the data entry but does NOT rearrange current FoundEntries
* object which means it is no longer considered sorted.
* @param index The index of the entry.
* @param changed the changed entry.
* @exception IOException on error.
*/
/*
//-------------------------------------------------------------------
private void set(int index,DatabaseEntry entry) throws IOException
//-------------------------------------------------------------------
{
	DatabaseEntryObject e = (DatabaseEntryObject)entry;
	lock.synchronize(); try{
		sortState = false;
		e.modifyingInside = this;
		doSet(index,e);
	}finally{e.modifyingInside = null; lock.release();}
}
*/
/**
* This saves a new entry to the database AND adds it to this FoundEntries
* object. It returns the index at which the new value was placed - it is always
* placed at the end which means the FoundEntries will no longer be sorted.
**/
/*
//-------------------------------------------------------------------
private int append(DatabaseEntry entry) throws IOException
//-------------------------------------------------------------------
{
	DatabaseEntryObject e = (DatabaseEntryObject)entry;
	lock.synchronize(); try{
		sortState = false;
		e.modifyingInside = this;
		return doAppend(e);
	}finally{e.modifyingInside = null; lock.release();}
}
*/
/**
* This saves a new entry to the database AND adds it to this FoundEntries
* object. It returns the index at which the new value was placed such that
* it maintains the sort order.
**/
//===================================================================
public int add(DatabaseEntry entry) throws IOException
//===================================================================
{
	//if (!sortState) append(entry);
	DatabaseEntryObject e = (DatabaseEntryObject)entry;
	lock.synchronize(); try{
		e.modifyingInside = this;
		return doAdd(e);
	}finally{e.modifyingInside = null; lock.release();}
}
//===================================================================
public void delete(int index) throws IOException
//===================================================================
{
	lock.synchronize(); try{
		get(index,buffer);
		buffer.modifyingInside = this;
		doDelete(index,(DatabaseEntryObject)buffer);
	}finally{buffer.modifyingInside = null; lock.release();}
	//}finally{lock.release();}
}
//===================================================================
public void erase(int index) throws IOException
//===================================================================
{
	lock.synchronize(); try{
		get(index,buffer);
		buffer.modifyingInside = this;
		doErase(index,(DatabaseEntryObject)buffer);
	}finally{buffer.modifyingInside = null; lock.release();}
	//}finally{lock.release();}
}

//===================================================================
public int include(DatabaseEntry entry) throws IllegalArgumentException, IOException
//===================================================================
{
	if (!entry.isSaved() || entry.isADeletedEntry()) throw new IllegalArgumentException();
	int where = indexOf(entry);
	if (where != -1) return where;
	int ret = doInclude((DatabaseEntryObject)entry);
	return ret;
}
//===================================================================
public void exclude(DatabaseEntry entry) throws IllegalArgumentException, IOException
//===================================================================
{
	if (!entry.isSaved() || entry.isADeletedEntry()) throw new IllegalArgumentException();
	if (isAllInclusive || (this instanceof DatabaseIndex)) throw new IllegalStateException();
	int idx = indexOf(entry);
	if (idx != -1) doExclude(indexOf(entry));
}
//===================================================================
public void exclude(int index) throws IOException
//===================================================================
{
	if (isAllInclusive || (this instanceof DatabaseIndex)) throw new IllegalStateException();
	doExclude(index);
}
//-------------------------------------------------------------------
protected void deleted(int index) throws IOException
//-------------------------------------------------------------------
{
	doExclude(index);
}
protected abstract int doAdd(DatabaseEntryObject entry) throws IOException;
protected abstract int doChange(int index,DatabaseEntryObject entry) throws IOException;
protected abstract int doStore(int index,DatabaseEntryObject entry) throws IOException;
//protected abstract int doAppend(DatabaseEntryObject entry) throws IOException;
//protected abstract void doSet(int index,DatabaseEntryObject entry) throws IOException;
protected abstract void doDelete(int index,DatabaseEntryObject entry) throws IOException;
protected abstract void doErase(int index,DatabaseEntryObject entry) throws IOException;
protected abstract int doInclude(DatabaseEntryObject entry) throws IOException;
protected abstract void doExclude(int index) throws IOException;
protected abstract void doUpdate(int index,DatabaseEntryObject entry) throws IOException;
protected abstract void doLoad(int index,DatabaseEntryObject entry) throws IOException;
protected abstract void indexFromFoundEntries(FoundEntries entries);
protected abstract boolean readFrom(InputStream stream,Handle h,int size) throws IOException;

/**
Write the FoundEntries to an OutputStream and put in the returnValue of the Handle,
an ewe.sys.Long object holding the number of bytes written. If stream is null then set the
returnValue of the Handle to be an ewe.sys.Long object that holds the number of bytes needed.
**/
protected abstract boolean writeTo(OutputStream stream,Handle h) throws IOException;
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected boolean writeAndReturnTrue(OutputStream stream, ByteArray out, Handle h)
throws IOException
//-------------------------------------------------------------------
{
	if (stream != null) stream.write(out.data,0,out.length);
	if (h != null) {
		h.returnValue = new ewe.sys.Long().set(out.length);
		h.setProgress(1f);
	}
	return true;
}

//===================================================================
public boolean isAllInclusive()
//===================================================================
{
	return isAllInclusive;// || index != null;
}
//===================================================================
public void setAllInclusive(boolean allInclusive)
//===================================================================
{
	//if (!allInclusive && index != null)
	if (!allInclusive && (this instanceof DatabaseIndex)) throw new IllegalArgumentException();
	isAllInclusive = allInclusive;
}
/*
//===================================================================
public DatabaseIndex getDatabaseIndex()
//===================================================================
{
	return index;
}
*/
//-------------------------------------------------------------------
private void checkSort(boolean forFind) throws IllegalStateException
//-------------------------------------------------------------------
{
	if (forFind){
		if (!sortState) throw new IllegalStateException("FoundEntries is not sorted.");
	}else if (!isSorted()) throw new IllegalStateException("FoundEntries is not sorted.");
}
/**
* Check if the proposed search criteria is compatible with the FoundEntries current sort.
* If this returns false then a call to search() will have to filter all the data (i.e.
* step through each one in turn). If it returns true then a search() will perform a
* quick binary search on the data.
* @param criteria the proposed sort criteria.
* @return true if it is compatible with the current sort, false if not.
*/
//===================================================================
public boolean searchIsCompatibleWithSortState(int[] criteria)
//===================================================================
{
	if (!sortState || sortCriteria == null || criteria == null) return false;
	return DatabaseUtils.searchIsCompatibleWithSort(criteria,sortCriteria);
}
//===================================================================
public boolean searchWillFilter(Object searchCriteria)
//===================================================================
{
	if (searchCriteria instanceof EntrySelector)
		return !((EntrySelector)searchCriteria).canSearch(this);
	else if (searchCriteria instanceof ObjectFinder)
		return true;
	else
		return false;
}
static Handle dummyHandle = new Handle();

//-------------------------------------------------------------------
private int findFirstInsertIndex(Handle h,Object searchData,Comparer comparer)
throws IOException, IllegalStateException, HandleStoppedException
//-------------------------------------------------------------------
{
	if (h == null) h = dummyHandle;
	//if (sortComparer == null) throw new IllegalStateException();
	//searchData = fixCompareData(searchData,comparer);
	int size = size();
	if (size < 1) return 0;
	//boolean flip = sortIsDescending;
	int ul = size, ll = -1;
	while(!h.shouldStop) {
		if (ul-ll <= 1) {
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		try{
			//if (true) where = size+1; <= Force an error.
			int cmp = comparer.compare(searchData,get(where,searcher));
			//if (flip) cmp = -cmp;
			if (cmp > 0) ll = where;
			else ul = where;
		}catch(RuntimeException oob){
			DatabaseManager.logError(
			"Got: "+oob.toString()+"\nI though size was: "+size+", it is now: "+size());
			throw oob;
		}
	}
	throw new HandleStoppedException();
}
//-------------------------------------------------------------------
private int findLastInsertIndex(Handle h,Object searchData,Comparer comparer)
throws IOException, IllegalStateException, HandleStoppedException
//-------------------------------------------------------------------
{
	if (h == null) h = dummyHandle;
	//checkSort(true);
	//searchData = fixCompareData(searchData,comparer);
	int size = size();
	if (size < 1) return 0;
	//boolean flip = sortIsDescending;
	int ul = size, ll = -1;
	while(!h.shouldStop) {
		if (ul-ll <= 1) {
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		try{
			//if (true) where = size+1; <= Force an error.
			int cmp = comparer.compare(searchData,get(where,searcher));
			//if (flip) cmp = -cmp;
			if (cmp >= 0) ll = where;
			else ul = where;
		}catch(RuntimeException oob){
			DatabaseManager.logError(
			"Got: "+oob.toString()+"\nI though size was: "+size+", it is now: "+size());
			throw oob;
		}
	}
	throw new HandleStoppedException();
}
//===================================================================
public Range findRange(Handle h,EntrySelector selector,Range dest) throws IOException
//===================================================================
{
	if (!selector.canSearch(this)) throw new IllegalArgumentException();
	if (dest == null) dest = new Range(-1,-1);
	if (h == null) h = dummyHandle;
	try{
		Comparer c = selector.toComparer();
		int first = findFirst(h,null,c);
		if (first != -1){
			dest.first = first;
			h.setProgress(0.5f);
			int last = findLast(h,null,c);
			if (last >= first)
				dest.last = last;
			else
				dest.first = dest.last = -1;
		}else
			dest.first = dest.last = -1;
		h.setProgress(1.0f);
		return dest;
	}catch(ewe.sys.HandleStoppedException e){
		return null;
	}
}
//-------------------------------------------------------------------
private IntArray search(Handle h, ObjectFinder finder, EntrySelector selector,IntArray dest)
throws IOException
//-------------------------------------------------------------------
{
	if (dest == null) dest = new IntArray();
	if (h == null) h = dummyHandle;
	h.resetTime("Searching");
	DatabaseEntry look = getNew();
	if (finder != null){
		int max = size();
		for (int i = 0; i<max && !h.shouldStop; i++){
			get(i,look);
			if (finder.lookingFor(look)) dest.add(i);
			h.setProgress((float)i/(float)max);
		}
	}else{
		//checkSort(true);
		try{
			Comparer c = selector.toComparer();
			int first = findFirst(h,null,c);
			if (first != -1){
				h.setProgress(0.5f);
				int last = findLast(h,null,c);
				if (last >= first){
					int num = last-first+1;
					if (dest.data.length < num) dest.data = new int[num];
					Utils.getIntSequence(dest.data,0,first,1,num);
					dest.length = num;
				}
			}
		}catch(ewe.sys.HandleStoppedException e){
			return null;
		}
	}
	if (!h.shouldStop) h.setProgress(1);
	else return null;
	return dest;
}
//-------------------------------------------------------------------
private int exactMatch(int idx,Object searchData,Comparer comparer) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	if (idx >= ids.length || idx < 0) return -1;
	if (comparer.compare(searchData,get(idx,buffer)) != 0) return -1;
	return idx;
}
//===================================================================
public IntArray findAll(Handle h,EntrySelector selector,IntArray dest)
throws IOException
//===================================================================
{
	if (selector.canSearch(this)) return search(h,null,selector,dest);
	else return search(h,selector.toObjectFinder(),null,dest);
}
//===================================================================
public IntArray filterAll(Handle h,ObjectFinder finder,IntArray dest)
throws IOException
//===================================================================
{
	return search(h,finder,null,dest);
}

//-------------------------------------------------------------------
protected void checkView(EntriesView view) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (view.fe != this) throw new IllegalArgumentException("EntriesView is not for this FoundEntries");
}
//===================================================================
public EntriesView getEmptyView()
//===================================================================
{
	if (views == null) views = new WeakSet();
	EntriesView ev = new EntriesView(this);
	views.add(ev);
	return ev;
}
/**
* If new Index is -1 the entry at oldIndex was deleted and the entry e will be null.
* If oldIndex was
* -1, then this is the addition of a new entry. Otherwise this indicates
* the repositioning of an entry.
**/
//-------------------------------------------------------------------
protected void recordChange(int oldIndex, int newIndex, DatabaseEntry e) throws IOException
//-------------------------------------------------------------------
{
	change();
	if (views == null) return;
	Object[] all = views.getRefs();
	for (int i = 0; i<all.length; i++){
		EntriesView v = (EntriesView)all[i];
		if (v != null) v.changed(oldIndex, newIndex);
	}
	int type = DatabaseEvent.ENTRY_CHANGED;
	if (oldIndex == -1) type = DatabaseEvent.ENTRY_ADDED;
	else if (newIndex == -1) type = DatabaseEvent.ENTRY_DELETED;
	dispatchEvent(type);
}
//===================================================================
public EntriesView getFullView(EntriesView dest)
//===================================================================
{
	if (dest == null) dest = getEmptyView();
	checkView(dest);
	dest.clear();
	dest.addRange(0,size());
	return dest;
}

//===================================================================
public boolean includeExclude(Handle h,EntriesView viewFromOtherFoundEntries,boolean isInclude)
throws IOException
//===================================================================
{
	if (h == null) h = new Handle();
	lock.synchronize(); try{
		h.resetTime(isInclude ? "Including" : "Excluding");
		int max = viewFromOtherFoundEntries.size();
		for (int i = 0; !h.shouldStop && i<max; i++){
			DatabaseEntry de = viewFromOtherFoundEntries.get(i,buffer);
			if (de != null)
				if (isInclude) include(de);
				else exclude(de);
			h.setProgress((float)i/(float)max);
		}
		h.setProgress(0);
		return !h.shouldStop;
	}finally{lock.release();}
}
//===================================================================
public boolean includeAll(Handle h,EntriesView viewFromOtherFoundEntries)
throws IOException
//===================================================================
{
	return includeExclude(h,viewFromOtherFoundEntries,true);
}
//===================================================================
public boolean excludeAll(Handle h,EntriesView viewFromOtherFoundEntries)
throws IOException
//===================================================================
{
	return includeExclude(h,viewFromOtherFoundEntries,false);
}

//-------------------------------------------------------------------
protected abstract void copyEntriesFrom(FoundEntriesObject other,int first,int num);
protected abstract void clearEntries();
protected abstract void addEntriesFrom(FoundEntriesObject other,int first,int num);

//-------------------------------------------------------------------
protected void copyStateFrom(FoundEntriesObject fe)
//-------------------------------------------------------------------
{
	sortState = fe.sortState;
	mySortID = fe.mySortID;
	sortCriteria = DatabaseUtils.copyCriteria(fe.sortCriteria);
	sortComparer = fe.sortComparer;
	addNewOnesFirst = fe.addNewOnesFirst;

}
//-------------------------------------------------------------------
protected FoundEntriesObject getNewFoundEntries()
//-------------------------------------------------------------------
{
	FoundEntriesObject fe = (FoundEntriesObject)database.getEmptyEntries();
	fe.copyStateFrom(this);
	return fe;
}

//===================================================================
public FoundEntries getCopy()
//===================================================================
{
	FoundEntriesObject fe = getNewFoundEntries();
	fe.copyEntriesFrom(this,0,size());
	return fe;
}
//===================================================================
public FoundEntries getSubSet(EntriesView view) throws IllegalArgumentException
//===================================================================
{
	if (view != null) checkView(view);
	FoundEntriesObject fe = getNewFoundEntries();
	if (view != null) {
		view.sort(false);
		IntArray ranges = view.toRanges(null);
		for (int i = 0; i<ranges.length; i+=2){
			fe.addEntriesFrom(this,ranges.data[i],ranges.data[i+1]);
		}
	}
	return fe;
}
//===================================================================
public int findInsertIndex(DatabaseEntry toAdd) throws ewe.io.IOException
//===================================================================
{
	if (toAdd == null) throw new NullPointerException();
	int insertPoint;
	if (!sortState)
		insertPoint = !addNewOnesFirst ? size() : 0;
	else try{
		insertPoint = !addNewOnesFirst ?
			findLastInsertIndex(null,toAdd,sortComparer):
			findFirstInsertIndex(null,toAdd,sortComparer);
	}catch(HandleStoppedException e){
		return -1;
	}
	return insertPoint;
}
/*
//===================================================================
public int findFirstInsertIndex(Handle h,Object searchData,Comparer comparer) throws IOException, IllegalStateException, HandleStoppedException
//===================================================================
{
	if (h == null) h = dummyHandle;
	checkSort(true);
	searchData = fixCompareData(searchData,comparer);
	int size = size();
	if (size < 1) return 0;
	boolean flip = sortIsDescending;
	int ul = size, ll = -1;
	while(!h.shouldStop) {
		if (ul-ll <= 1) {
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		int cmp = comparer.compare(searchData,get(where,buffer));
		if (flip) cmp = -cmp;
		if (cmp > 0) ll = where;
		else ul = where;
	}
	throw new HandleStoppedException();
}
//===================================================================
public int findLastInsertIndex(Handle h,Object searchData,Comparer comparer) throws IOException, IllegalStateException, HandleStoppedException
//===================================================================
{
	if (h == null) h = dummyHandle;
	checkSort(true);
	searchData = fixCompareData(searchData,comparer);
	int size = size();
	if (size < 1) return 0;
	boolean flip = sortIsDescending;
	int ul = size, ll = -1;
	while(!h.shouldStop) {
		if (ul-ll <= 1) {
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		int cmp = comparer.compare(searchData,get(where,buffer));
		if (flip) cmp = -cmp;
		if (cmp >= 0) ll = where;
		else ul = where;
	}
	throw new HandleStoppedException();
}
*/
/**
* This finds the first index of the entry which (according to the provided
* Comparer) matches a search criteria. A binary search is done so it is
* assumed that all indexes between the one found by findFirst and by findLast
* inclusive (for the same finder) will also match. It returns -1 if no
* match could be found.<p>
*
* During the search the comparer will have its compare() method called
* with the searchData parameter object as the first parameter and a
* DatabaseEntry object as the second parameter. The comparer should return
* <pre>
* <0 if the DatabaseEntry is considered greater than the search data.
* 0 if the DatabaseEntry is considered to match the search data.
* >0 if the DatabaseEntry is considered less than the search data.
* <pre>
* The Comparer should NOT take into account if the SORT_DESCENDING option
* is set - that will be taken care of by the search routine.
**/
//===================================================================
public int findFirst(Handle h,Object searchData,Comparer comparer) throws IOException, IllegalStateException, HandleStoppedException
//===================================================================
{
	//searchData = fixCompareData(searchData,comparer);
	if (!sortState) throw new IllegalStateException("FoundEntries is not sorted.");
	return exactMatch(findFirstInsertIndex(h,searchData,comparer),searchData,comparer);
}
/*
//===================================================================
public int findFirst(Object searchData,Comparer comparer) throws IOException, IllegalArgumentException
//===================================================================
{
	try{
		return findFirst(null,searchData,comparer);
	}catch(HandleStoppedException e){
		return -1;
	}
}
*/
/**
* This finds the last index of the entry which (according to the provided
* ObjectFinder) matches a search criteria. A binary search is done so it is
* assumed that all indexes between the one found by findFirst and by findLast
* inclusive (for the same finder) will also match. It returns -1 if no
* match could be found.
*
* During the search the comparer will have its compare() method called
* with the searchData parameter object as the first parameter and a
* DatabaseEntry object as the second parameter. The comparer should return
* <pre>
* <0 if the DatabaseEntry is considered greater than the search data.
* 0 if the DatabaseEntry is considered to match the search data.
* >0 if the DatabaseEntry is considered less than the search data.
* <pre>
* The Comparer should NOT take into account if the SORT_DESCENDING option
* is set - that will be taken care of by the search routine.
**/
//===================================================================
public int findLast(Handle h,Object searchData,Comparer comparer) throws ewe.io.IOException, IllegalStateException, HandleStoppedException
//===================================================================
{
	//searchData = fixCompareData(searchData,comparer);
	if (!sortState) throw new IllegalStateException("FoundEntries is not sorted.");
	return exactMatch(findLastInsertIndex(h,searchData,comparer)-1,searchData,comparer);
}
/*
//===================================================================
public int findLast(Object searchData,Comparer comparer) throws IOException, IllegalArgumentException
//===================================================================
{
	try{
		return findLast(null,searchData,comparer);
	}catch(HandleStoppedException e){
		return -1;
	}
}
*/
//===================================================================
public EntrySelector getEntrySelector(Object searchData,boolean hasWildCards) throws IllegalStateException
//===================================================================
{
	if (!(sortComparer instanceof StandardEntryComparer))
		throw new IllegalStateException("FoundEntries not sorted with a StandardEntryComparer");
	return ((StandardEntryComparer)sortComparer).toEntrySelector(searchData,hasWildCards);
}

//===================================================================
public int findFirst(Handle h, Object searchData,boolean hasWildCards)
throws IOException, IllegalStateException, HandleStoppedException
//===================================================================
{
	return findFirst(h,null,getEntrySelector(searchData,hasWildCards).toComparer());
}
//===================================================================
public int findLast(Handle h, Object searchData,boolean hasWildCards)
throws IOException, IllegalStateException, HandleStoppedException
//===================================================================
{
	return findLast(h,null,getEntrySelector(searchData,hasWildCards).toComparer());
}

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
public String toString()
{
	return "("+Integer.toHexString(System.identityHashCode(this))
	+") Length: "+ids.length+", Data: "+(ids.data == null ? "null" : Convert.toString(ids.data.length));
}

//##################################################################
}
//##################################################################
