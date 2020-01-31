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
package ewe.datastore;
import ewe.util.*;

//##################################################################
public class FoundEntries{
//##################################################################
IntArray ids = new IntArray(); //This must be the first variable.
Database table; //This must be the second variable.
FieldSortEntry fs; //This must be the third variable.

ByteArray buffer = new ByteArray();
DatabaseEntry data = new DatabaseEntry();

public boolean addNewOnesLast = true;
protected boolean sortState = true;

//ByteArray dataBuffer = new ByteArray();
//DatabaseEntry dataData = new DatabaseEntry();

//===================================================================
public Database getDatabase()
//===================================================================
{
	return table;
}
/**
 * Get the SortID used.
 */
//===================================================================
public int getSortEntry()
//===================================================================
{
	if (fs == null) return 0;
	else return fs.id;
}

/**
* This returns true if the FoundEntries is in a sorted stated as specified
* by its sort criteria. If the SortID used was 0 - indicating no sorting is to
* be done, then this will always return true. If a Sort is used, but the
* FoundEntries has been made temporarily unsorted using setDataInPlace() or
* using appendData() then this return false.
**/
//===================================================================
public boolean isSorted()
//===================================================================
{
	if (fs == null) return true;
	return sortState;
}
/**
* You can only search if the FoundEntries uses a valid sort criteria AND is
* in a fully sorted state. You can always filter regardless of the sort state
* since filtering goes through each entry sequentially.
**/
//===================================================================
public boolean canSearch()
//===================================================================
{
	return fs != null && sortState;
}

//-------------------------------------------------------------------
private void checkSort(boolean forFind) throws IllegalStateException
//-------------------------------------------------------------------
{
	if (forFind){
		if (fs == null || !sortState) throw new IllegalStateException("FoundEntries is not sorted.");
	}else if (!isSorted()) throw new IllegalStateException("FoundEntries is not sorted.");
}
/**
* Returns the number of entries.
**/
//===================================================================
public int size() {return ids.length;}
//===================================================================


//-------------------------------------------------------------------
DatabaseEntry setFound(DatabaseEntry ret)
//-------------------------------------------------------------------
{
	if (ret == null) return ret;
	ret.found = this;
	return ret;
}
/**
* This creates a new DatabasEntry and sets its fields from the specified data object.
* It does not add it into the database or the FoundEntries list.
**/
//===================================================================
public DatabaseEntry createEntryFor(Object data)
//===================================================================
{
	DatabaseEntry dbe = getNew();
	table.setData(dbe,data);
	return dbe;
}
/*
Create a new object and retrieve its fields from the data at the specified index.
 * For this to work the
 * data table must have been setup to use a specific object for data transfer between
 * the table and stored entries, and that object must have a default public constructor.
 * @param index The index of the entry.
	@return the object with the data loaded from that index.
 * @exception ewe.io.IOException
	@exception IllegalStateException if a new object could not be created.
 */
//===================================================================
public Object getData(int index) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return getData(index,null);
}
/**
 * Retrieve the fields or the entry at the specified index.
 * For this to work the
 * data table must have been setup to use a specific object for data transfer between
 * the table and stored entries.
 * @param index The index of the entry.
 * @param data The object that will hold the data or null to create a new one.
 * @exception ewe.io.IOException
 */
//===================================================================
public Object getData(int index,Object data) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	DatabaseEntry d = get(index,new ByteArray(),new DatabaseEntry());
	return table.getData(d,data);
}
/**
 * Set the fields or the entry at the specified index and moves the data to a new index
 * appropriate for the sort criteria.
 * For this to work the
 * data table must have been setup to use a specific object for data transfer between
 * the table and stored entries.
 * @param index The index of the entry.
 * @param data The object that holds the data.
 * @exception ewe.io.IOException
 * @return The new index of the changed data.
 */
//===================================================================
public int setData(int index,Object data) throws ewe.io.IOException
//===================================================================
{
	checkSort(false);
	DatabaseEntry d = get(index,new ByteArray(),new DatabaseEntry());
	table.setData(d,data);
	return change(d);
}
/**
 * Set the fields or the entry at the specified index.
 * For this to work the
 * data table must have been setup to use a specific object for data transfer between
 * the table and stored entries.
 * @param index The index of the entry.
 * @param data The object that holds the data.
 * @exception ewe.io.IOException
 */
//===================================================================
public void setDataInPlace(int index,Object data) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry d = get(index,new ByteArray(),new DatabaseEntry());
	table.setData(d,data);
	set(d);
}
/**
 * Add new data.
 * For this to work the
 * data table must have been setup to use a specific object for data transfer between
 * the table and stored entries.
 * @param data The object that holds the data.
 * @exception ewe.io.IOException
 * @return The index of the added data.
 */
//===================================================================
public int addData(Object data) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry d = getNew();
	table.setData(d,data);
	return add(d);
}
/**
 * Append new data.
 * For this to work the
 * data table must have been setup to use a specific object for data transfer between
 * the table and stored entries.
 * @param data The object that holds the data.
 * @exception ewe.io.IOException
 * @return The index of the appended data.
 */
//===================================================================
public int appendData(Object data) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry d = getNew();
	table.setData(d,data);
	return append(d);
}
/**
* Get the DatabaseEntry for the entry at the specified index. buffer and dest
* are optional ByteArray buffer and DatabaseEntry destination objects which may
* be null. If they are not null they will be used during the get operation. This
* allows you to minimize on object creation by using the same ByteArray and
* DatabaseEntry while processing a large number of entries.
**/
//===================================================================
public DatabaseEntry get(int index,ByteArray buffer,DatabaseEntry dest) throws ewe.io.IOException
//===================================================================
{
	if (index >= ids.length || index < 0) throw new IndexOutOfBoundsException();
	return setFound(table.getData(ids.data[index],buffer,dest));
}
/**
* This calls get(index,buffer,dest) with null buffer and dest parameters.
**/
//===================================================================
public DatabaseEntry get(int index) throws ewe.io.IOException {return get(index,null,null);}
//===================================================================
/**
* This calls getNew(buffer,dest) with null buffer and dest parameters.
**/
//===================================================================
public DatabaseEntry getNew() {return getNew(null,null);}
//===================================================================
/**
* This requests a new DatabaseEntry which can be later inserted into the
* database and this FoundEntries object (in the correct place) using the
* add() method.
**/
//===================================================================
public DatabaseEntry getNew(ByteArray buffer,DatabaseEntry dest)
//===================================================================
{
	return setFound(table.getNewData(buffer,dest));
}
private final static String we = "Error writing to database.";
/*
//-------------------------------------------------------------------
int writeError() throws ewe.io.IOException
//-------------------------------------------------------------------
{
	if (table != null)
		if (table.storage != null){
			table.storage.throwException(we);
			return -1;
		}
	throw new ewe.io.IOException(we);
}
*/
/**
* This saves a new entry to the database AND adds it to this FoundEntries
* object. It returns the index at which the new value was placed - it is placed
* according to the sort order used to create this FoundEntries.
**/
//===================================================================
public int add(DatabaseEntry toAdd) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	checkSort(false);
	toAdd.save();
	int insertPoint = findInsertIndex(toAdd);
	if (insertPoint == -1) return -1;
	ids.insert(toAdd.stored,insertPoint);
	return insertPoint;
}
/**
* This saves a new entry to the database AND adds it to this FoundEntries
* object. It returns the index at which the new value was placed - it is always
* placed at the end.
**/
//===================================================================
public int append(DatabaseEntry toAdd) throws ewe.io.IOException
//===================================================================
{
	if (toAdd == null) return -1;
	sortState = false;
	toAdd.save();
	ids.insert(toAdd.stored,size());
	return size()-1;
}
/**
* This saves changes to the data entry but does NOT rearrange current FoundEntries
* object so that it is still sorted correctly. It returns the new index of the entry
* in the FoundEntries object.
**/
//===================================================================
public void set(DatabaseEntry changed) throws ewe.io.IOException
//===================================================================
{
	if (changed == null) return;
	sortState = false;
	int where = ids.indexOf(changed.stored);
	changed.save();
	ids.data[where] = changed.stored;
}
/**
* This stores the DatabaseEntry but uses the Database.storeEntry() method to preserve
* all the data in the entry. It does NOT rearrange current FoundEntries
* object so that it is still sorted correctly.
**/
//===================================================================
public void store(DatabaseEntry changed) throws ewe.io.IOException
//===================================================================
{
	int where = ids.indexOf(changed.stored);
	table.storeEntry(changed);
	if (where != -1) ids.data[where] = changed.stored;
}
/**
* This saves changes to the data entry AND re-arranges it in the current FoundEntries
* object so that it is still sorted correctly. It returns the new index of the entry
* in the FoundEntries object.
**/
//===================================================================
public int change(DatabaseEntry changed) throws ewe.io.IOException
//===================================================================
{
	if (changed == null) return -1;
	//ewe.sys.Vm.debug("Removing: "+changed.stored);
	ids.remove(changed.stored);
	changed.save();
	int insertPoint = findInsertIndex(changed);
	if (insertPoint == -1) return -1;
	//ewe.sys.Vm.debug("Inserting: "+changed.stored);
	ids.insert(changed.stored,insertPoint);
	return insertPoint;
}
/**
* This deletes the data AND removes it from this FoundEntries list.
**/
//===================================================================
public void delete(DatabaseEntry toDelete) throws ewe.io.IOException
//===================================================================
{
	if (toDelete == null) return;
	toDelete.delete();
	ids.remove(toDelete.stored);
}
/**
* This deletes the data AND removes it from this FoundEntries list.
**/
//===================================================================
public void delete(int index) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry de = get(index);
	if (de == null) return;
	de.delete();
	ids.remove(de.stored);
}
/**
* This erases the item completely.
**/
//===================================================================
public void erase(int index) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry de = get(index);
	if (de == null) return;
	table.eraseEntry(de);
	ids.remove(de.stored);
}
/**
* This erases the item completely.
**/
//===================================================================
public void erase(DatabaseEntry toErase) throws ewe.io.IOException
//===================================================================
{
	if (toErase == null) return;
	table.eraseEntry(toErase);
	ids.remove(toErase.stored);
}

/**
* This finds the index where a new DatabaseEntry would be inserted with the
* sort criteria. You would not likely use this, because you would just use
* the add() method which does the insertion and returns where it was inserted.
* @param toAdd The DatabaseEntry to add.
* @return the insert point.
* @exception ewe.io.IOException
* @exception IllegalStateException If the Database is temporarily in an unsorted state.
*/
//===================================================================
public int findInsertIndex(DatabaseEntry toAdd) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	if (toAdd == null) throw new NullPointerException();
	int insertPoint;
	if (fs == null)
		insertPoint = addNewOnesLast ? ids.length : 0;
	else
		insertPoint = addNewOnesLast ?
			findLastInsertIndex(toAdd,getFieldComparer()):
			findFirstInsertIndex(toAdd,getFieldComparer());
	return insertPoint;
}
/**
* This gets the standard DatabaseFieldComparer which has been set up
* to use masks (* and ?) characters for comparison operations on Strings.
**/
//===================================================================
public DatabaseFieldComparer getFieldComparer()
//===================================================================
{
	return new DatabaseFieldComparer(table,fs,true);
}
/**
* This gets an ObjectFinder which finds entries where the entry has the
* specified value for the specified field ID.
**/
//===================================================================
public ObjectFinder getFieldFilter(int fieldID,Object value,int options)
//===================================================================
{
	DatabaseEntry ded = getNew();
	ded.setFieldValue(fieldID,value);
	return new DatabaseFieldComparer(table,ded,options);
}
//-------------------------------------------------------------------
private int exactMatch(int idx,Object searchData,Comparer comparer) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	if (idx >= ids.length || idx < 0) return -1;
	if (comparer.compare(searchData,get(idx,buffer,data)) != 0) return -1;
	return idx;
}
/**
* This finds the first index of the entry which (according to the provided
* Comparer) matches a search criteria. A binary search is done so it is
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
public int findFirst(Object searchData,Comparer comparer) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	searchData = fixCompareData(searchData,comparer);
	return exactMatch(findFirstInsertIndex(searchData,comparer),searchData,comparer);
}
/**
* This is a quick method of searching which uses the standard DatabaseFieldComparer.
* @deprecated use findFirst(Object primaryFieldDataMask) instead.
**/
//===================================================================
public int findFirst(int fieldID,Object mask) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return findFirst(getSearchData(fieldID,mask),getFieldComparer());
}
/**
		This searches for the first record in which the primary sort field is considered
		to match the field data mask.
 * @param primaryFieldDataMask An object representation of the field data mask to search for.
	For example if the field is a String field, then "B*" could be used as a mask.
 * @return The index of the first record to match the search criteria, or -1 if it was not found.
 * @exception IllegalStateException If the FoundEntries is not fully sorted.
 */
//===================================================================
public int findFirst(Object primaryFieldDataMask) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	checkSort(true);
	return findFirst(getSearchData(fs.field1,primaryFieldDataMask),getFieldComparer().setUseFirstFieldOnly());
}
/**
		This searches for the last record in which the primary sort field is considered
		to match the field data mask.
 * @param primaryFieldDataMask An object representation of the field data mask to search for.
	For example if the field is a String field, then "B*" could be used as a mask.
 * @return The index of the last record to match the search criteria, or -1 if it was not found.
 * @exception IllegalStateException If the FoundEntries is not fully sorted.
 */
//===================================================================
public int findLast(Object primaryFieldDataMask) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	checkSort(true);
	return findLast(getSearchData(fs.field1,primaryFieldDataMask),getFieldComparer().setUseFirstFieldOnly());
}

//===================================================================
public Object getSearchData(int fieldID,Object mask)
//===================================================================
{
	DatabaseEntry ded = getNew();
	ewe.reflect.Reflect data = table.getObjectClass();
	if (data != null)
		if (data.isInstance(mask)){
			table.setData(ded,mask);
			return ded;
		}
	ded.setFieldValue(fieldID,mask);
	return ded;
}
/**
* This is a quick method of searching which uses the standard DatabaseFieldComparer.
**/
//===================================================================
public int findLast(int fieldID,Object mask) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return findLast(getSearchData(fieldID,mask),getFieldComparer());
}

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
public int findLast(Object searchData,Comparer comparer) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	searchData = fixCompareData(searchData,comparer);
	return exactMatch(findLastInsertIndex(searchData,comparer)-1,searchData,comparer);
}
/**
* This finds the index of the first entry which is the correct insertion
* point for the provided comparer. This entry point is defined as the lowest index
* which is >= the search criteria AND the index before it is < the search criteria.
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
public int findFirstInsertIndex(Object searchData,Comparer comparer) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	checkSort(true);
	searchData = fixCompareData(searchData,comparer);
	int size = size();
	if (fs == null || size < 1) return 0;
	boolean flip = ((fs.type & table.SORT_DESCENDING) != 0);
//Fix - implement a binary chop search.
	int ul = size, ll = -1;
	while(true) {
		if (ul-ll <= 1) {
			//System.out.println(ul);
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		int cmp = comparer.compare(searchData,get(where,buffer,data));
		if (flip) cmp = -cmp;
		if (cmp > 0) ll = where;
		else ul = where;
	}
/*
	for (int i = 0; i<max; i++){
		int cmp = comparer.compare(searchData,get(i,buffer,data));
		if (flip) cmp = -cmp;
		if (cmp <= 0) return i;
	}
	return max;
*/
}
/**
* This finds the index of the last entry which is the correct insertion
* point for the provided comparer. This entry point is defined as the lowest index
* which is > the search criteria AND the index before it is <= the search criteria.
* <p>
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
public int findLastInsertIndex(Object searchData,Comparer comparer) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	checkSort(true);
	searchData = fixCompareData(searchData,comparer);
	int size = size();
	if (fs == null || size < 1) return 0;
	boolean flip = ((fs.type & table.SORT_DESCENDING) != 0);
	int ul = size, ll = -1;
	while(true) {
		if (ul-ll <= 1) {
			//System.out.println(ul);
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		int cmp = comparer.compare(searchData,get(where,buffer,data));
		if (flip) cmp = -cmp;
		if (cmp >= 0) ll = where;
		else ul = where;
	}
/*
	for (int i = max-1; i>=0; i--){
		int cmp = comparer.compare(searchData,get(i,buffer,data));
		if (flip) cmp = -cmp;
		if (cmp >= 0) return i+1;
	}
	return max;
*/
}
//===================================================================
public void reSort(int sortID) throws ewe.io.IOException
//===================================================================
{
	table.sortFieldData(sortID,table.getLocale(),this);
	sortState = true;
}

//===================================================================
public void reSort() throws ewe.io.IOException
//===================================================================
{
	if (fs == null) sortState = true;
	else reSort(fs.id);
}
//===================================================================
public FoundEntries getCopy() {return getCopy(true);}
//===================================================================
//-------------------------------------------------------------------
protected FoundEntries getCopy(boolean copyData)
//-------------------------------------------------------------------
{
	FoundEntries fe = new FoundEntries();
	fe.table = table;
	fe.fs = fs;
	fe.addNewOnesLast = addNewOnesLast;
	fe.sortState = sortState;
	if (copyData) {
		fe.ids.data = ids.appendTo(null);
		fe.ids.length = ids.length;
	}
	return fe;
}

//-------------------------------------------------------------------
private Object fixCompareData(Object data,Comparer comparer)
//-------------------------------------------------------------------
{
	if (!(comparer instanceof DatabaseFieldComparer) || (data instanceof DatabaseEntry)) return data;
	return createEntryFor(data);
}
/**
* This searches for the data and returns the subset which encompasses
* the value returned by findFirst() and that returned by findLast(). The
* returned array is a set of indexes into this FoundEntries.
**/
//===================================================================
public IntArray findAll(Object searchData,Comparer comparer,IntArray dest) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	searchData = fixCompareData(searchData,comparer);
	if (dest == null) dest = new IntArray();
	dest.clear();
	int first = findFirst(searchData,comparer);
	if (first == -1) return dest;
	int last = findLast(searchData,comparer);
	if (last < first) return dest;
	int num = last-first+1;
	if (dest.data.length < num) dest.data = new int[num];
	ewe.util.Utils.getIntSequence(dest.data,0,first,1,num);
	dest.length = num;
	return dest;
}
/**
* This calls findAll(Object searchData,Comparer comparer,IntArray dest) with a null searchData
* and a null dest.
**/
//===================================================================
public IntArray findAll(Comparer comparer) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return findAll(null,comparer,null);
}
/**
 * Find all the entries where the primary sort field matches the specified mask.
 * @param primaryFieldDataMask The data to be searched for.
 * @param dest The destination IntArray (may be null).
 * @return The destination IntArray or a new IntArray.
 * @exception ewe.io.IOException
 * @exception IllegalStateException If the FoundEntries is not fully sorted.
 */
//===================================================================
public IntArray findAll(Object primaryFieldDataMask,IntArray dest) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	checkSort(true);
	return findAll(getSearchData(fs.field1,primaryFieldDataMask),getFieldComparer().setUseFirstFieldOnly(),dest);
}
/**
* This steps through the data sequentially checking each entry found. It will
* work for unsorted data or when you are searching for a field which is not
* the main sort field. The returned array is a set of indexes into this FoundEntries.
* @param finder The ObjectFinder to be used. Its lookingFor() method will be called with
* a DatabaseEntry object as the parameter. If lookingFor() returns true, then the record
* will be placed in the returned IntArray.
* @param dest An optional destination IntArray. If it is null then a new one will be created.
* @return An IntArray containing the indexes of the records found.
* @exception ewe.io.IOException
*/
//===================================================================
public IntArray filterAll(ObjectFinder finder,IntArray dest)  throws ewe.io.IOException
//===================================================================
{
	if (dest == null) dest = new IntArray();
	dest.clear();
	int max = size();
	for (int i = 0; i<max; i++)
		if (finder.lookingFor(get(i,buffer,data)))
			dest.add(i);
	return dest;
}


/**
* This steps through the data sequentially checking each entry found. It will
* work for unsorted data or when you are searching for a field which is not
* the main sort field. The returned array is a set of indexes into this FoundEntries.
 * @param searchData The searchData will be passed to the Comparer's compare() method as
	the first parameter. A DatabaseEntry will be passed as the second.
 * @param comparer The compare() method of this object will be called for each record. The searchData will be passed to the Comparer's compare() method as
	the first parameter. A DatabaseEntry will be passed as the second. If the compare() method
	returns 0, then the record is placed in the returned IntArray.
* @param dest An optional destination IntArray. If it is null then a new one will be created.
* @return An IntArray containing the indexes of the records found.
* @exception ewe.io.IOException
 */
//===================================================================
public IntArray filterAll(Object searchData, Comparer comparer, IntArray dest) throws ewe.io.IOException
//===================================================================
{
	return filterAll(new ComparerObjectFinder(comparer,searchData),dest);
}
/**
* This steps through the data sequentially checking each entry found. It will
* work for unsorted data or when you are searching for a field which is not
* the main sort field. The returned array is a set of indexes into this FoundEntries.
* This calls filterAll(ObjectFinder finder, IntArray dest) with a null IntArray.
* @param finder The ObjectFinder to be used. Its lookingFor() method will be called with
* a DatabaseEntry object as the parameter. If lookingFor() returns true, then the record
* will be placed in the returned IntArray.
* @return An IntArray containing the indexes of the records found.
* @exception ewe.io.IOException
**/
//===================================================================
public IntArray filterAll(ObjectFinder finder) throws ewe.io.IOException
//===================================================================
{
	return filterAll(finder,null);
}
/**
* Returns a FoundEntries subset which contains the entries at the indexes
* specified in the set.
* @param set The IntArray returned by one of the findAll(), filterAll() or getRange() methods.
* @return
*/
//===================================================================
public FoundEntries getSubSet(IntArray set)
//===================================================================
{
	FoundEntries fe = getCopy(false);
	for (int i = 0; i<set.length; i++)
		fe.ids.add(ids.data[set.data[i]]);
	return fe;
}
/**
 * Get an array of indexes representing a portion of the entries in this FoundEntries object.
 * You can use the returned value to create a new FoundEntries that is a subset of this FoundEntries
 * using getSubSet().
 * @param start The first index in the range.
 * @param length The number of indexes in the range.
* @param destination An optional destination IntArray object. If it is null a new one will be
* created and returned.
* @return The range of indexes in an IntArray object.
*/
//===================================================================
public IntArray getRange(int start, int length, IntArray destination)
//===================================================================
{
	if (start < 0 || start+length > size()) throw new IndexOutOfBoundsException();
	if (destination == null) destination = new IntArray();
	if (destination.data == null || destination.data.length < length)
		destination.data = new int[length];
	destination.length = length;
	if (length != 0) ewe.util.Utils.getIntSequence(destination.data,0,start,1,length);
	return destination;
}

//##################################################################
}
//##################################################################

