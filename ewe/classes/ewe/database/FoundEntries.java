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
import ewe.util.EventDispatcher;
import ewe.util.Range;

/**
* A FoundEntries object holds a sorted view of a set of records in a Database. The
* actual data is not stored in the FoundEntries, rather references to the records
* are held instead (usually as 32-bit integers or other such value).
* <p>
* You get a FoundEntries object by calling one of the getFoundEntries() methods on the Database
* or you can call getEmptyEntries() to get an empty FoundEntries which you can then add records
* to.
* <p>
* You use FoundEntries to retrieve, add, delete and modify records and the FoundEntries will
* maintain its sort state during these operations.<p>
*
* You can get an EntriesView object for a FoundEntries using getEmptyEntries() or getFullView()
* and then use that EntriesView object to search the data using any criteria. You can then create
* a new FoundEntries from the results of the search using getSubSet() and then create a new
* FoundEntries object sorted by some other criteria using one of the sort() methods.
**/

//##################################################################
public interface FoundEntries extends DatabaseTypes{
//##################################################################
/**
 * Get the database associated with the FoundEntries.
 */
//===================================================================
public Database getDatabase();
//===================================================================

/**
 * Return the number of entries in the FoundEntries.
 */
//===================================================================
public int size();
//===================================================================
/**
* Create a new DatabaseEntry for this FoundEntries.
**/
//===================================================================
public DatabaseEntry getNew();
//===================================================================
/**
* This saves changes to the data entry but does NOT rearrange current FoundEntries
* object which means it is no longer considered sorted.
* @param index The index of the entry.
* @param changed the changed entry.
* @exception IOException on error.
 * @exception IllegalStateException if the found entries is not allowed to be in an unsorted state
 * (say because it belongs to a DatabaseIndex).
*/
//===================================================================
//public void set(int index,DatabaseEntry changed) throws IOException, IllegalStateException;
//===================================================================
/**
* This saves a new entry to the database AND adds it to this FoundEntries
* object. It returns the index at which the new value was placed - it is always
* placed at the end which means the FoundEntries will no longer be sorted.
 * @exception IllegalStateException if the found entries is not allowed to be in an unsorted state
 * (say because it belongs to a DatabaseIndex).
**/
//===================================================================
//public int append(DatabaseEntry toAdd) throws IOException, IllegalStateException;
//===================================================================
/**
* This saves a new entry to the database AND adds it to this FoundEntries
* object. It returns the index at which the new value was placed such that
* it maintains the sort order.
**/
//===================================================================
public int add(DatabaseEntry toAdd) throws IOException;
//===================================================================
/**
* This deletes the data AND removes it from this FoundEntries list.
**/
//===================================================================
public void delete(int index) throws IOException;
//===================================================================
/**
* This erases the data without marking it as deleted AND removes it from this FoundEntries list.
**/
//===================================================================
public void erase(int index) throws IOException;
//===================================================================
/**
* Get the data at the specified index.
* @param index The index to read from.
* @param data a destination DatabaseEntry, which can be null.
* @return the destination DatabaseEntry or a newly created one.
* @exception IOException on error.
*/
//===================================================================
public DatabaseEntry get(int index, DatabaseEntry data) throws IOException;
//===================================================================
/**
* Get the data at the specified index, creating a new DatabaseEntry to get it from.
* @param index The index to read from.
* @return the destination DatabaseEntry or a newly created one.
* @exception IOException on error.
*/
//===================================================================
public DatabaseEntry get(int index) throws IOException;
//===================================================================
/**
 * Return the index of the entry in the FoundEntries object.
 * @param entry the entry to look for.
 * @return the index of the entry or -1 if it is not found.
 */
//===================================================================
public int indexOf(DatabaseEntry entry);
//===================================================================
/**
* This sorts the FoundEntries in the current thread using the specified sortID.
* Other threads will be allowed to run and can monitor the progress and stop
* the sort by calling stop() on the handle (if it is not null).
* @param h the Handle for monitoring the sort. This can be null but if it is the possibility
* exists that the sort may run natively thereby not giving other threads a chance to run.
* @param criteria the sort criteria.
* @return a new FoundEntries or null if the sort was aborted.
* @exception IOException if there was an error reading data for sorting.
*/
//===================================================================
public FoundEntries sort(Handle h,int[] criteria) throws IOException;
//===================================================================
/**
 * Sort the found entries in a separate thread.
 * If there is an error or the sorting is aborted the FoundEntries will be in its original state.
 * @param database The database to sort the entries.
 * @param criteria The criteria for sorting.
 * @return a Handle that you can use to monitor the sorting. When the Handle reports success
 * the returnValue of the handle will hold a new FoundEntries object which contains the entries
 * re-sorted by the specified criteria.
 */
//===================================================================
public Handle sort(int[] criteria) throws IOException;
//===================================================================
/**
* This sorts the FoundEntries in the current thread using the specified sortID.
* Other threads will be allowed to run and can monitor the progress and stop
* the sort by calling stop() on the handle (if it is not null).
* @param h the Handle for monitoring the sort. This can be null but if it is the possibility
* exists that the sort may run natively thereby not giving other threads a chance to run.
* @param sortID The sortID in the database to sort by.
* @return a new FoundEntries or null if the sort was aborted.
* @exception IllegalArgumentException if the sortID is not valid.
* @exception IOException if there was an error reading data for sorting.
*/
//===================================================================
public FoundEntries sort(Handle h,int sortID) throws IllegalArgumentException, IOException;
//===================================================================
/**
 * Sort the found entries in a separate thread.
 * @param sortID The sortID in the database to sort by.
 * @return a Handle that you can use to monitor the sorting. When the Handle reports success
 * the returnValue of the handle will hold a new FoundEntries object which contains the entries
 * re-sorted by the specified id.
 */
//===================================================================
public Handle sort(int sortID) throws IllegalArgumentException;
//===================================================================
/**
* This sorts the FoundEntries in the current thread using the specified Comparer.
* Other threads will be allowed to run and can monitor the progress and stop
* the sort by calling stop() on the handle (if it is not null).
* @param h the Handle for monitoring the sort. This can be null but if it is the possibility
* exists that the sort may run natively thereby not giving other threads a chance to run.
* @param comparer a Comparer used to compare each entry.
* @return a new FoundEntries or null if the sort was aborted.
* @exception IOException if there was an error reading data for sorting.
*/
//===================================================================
public FoundEntries sort(Handle h,Comparer comparer) throws IOException;
//===================================================================
/**
 * Sort the found entries in a separate thread.
* @param comparer a Comparer used to compare each entry.
 * @return a Handle that you can use to monitor the sorting. When the Handle reports success
 * the returnValue of the handle will hold a new FoundEntries object which contains the entries
 * re-sorted by the specified comparer.
 */
//===================================================================
public Handle sort(Comparer comparer) throws IOException;
//===================================================================
/**
* Get a full copy of this FoundEntries, including the entries themselves.
**/
//===================================================================
public FoundEntries getCopy();
//===================================================================
/**
* Re-sort the entries based on the most recent sort.
* @param h A handle that can be used to monitor the connection, or null.
* @return true if the sort completed successfully, false if it was aborted.
* @exception IOException on error.
* @exception IllegalStateException if there is no current sort associated with the FoundEntries.
*/
//===================================================================
//public boolean sort(Handle h) throws IllegalStateException, IOException;
//===================================================================
//===================================================================
//public Handle sort();
//===================================================================
/**
* Re-sort the entries based on the most recent sort.
* @deprecated use sort(Handle) instead.
*/
//===================================================================
//public void reSort() throws IOException;
//===================================================================
/**
* Re-sort the entries based on the most recent sort.
* @deprecated use sort(Handle) instead.
*/
//===================================================================
//public boolean reSort(Handle h) throws IOException;
//===================================================================
/**
* Return if the entries are sorted.
**/
//===================================================================
public boolean isSorted();
//===================================================================
/**
* Return the ID of the sort used to sort the database by if one was used.
* If a non-standard Comparer was used, or if the entries are not sorted, this will return 0.
**/
//===================================================================
public int getSortEntry();
//===================================================================
/**
* Return the current sort criteria if one was used.
* If a non-standard Comparer was used, or if the entries are not sorted, this will return null.
**/
//===================================================================
public int[] getSortCriteria();
//===================================================================
/**
* Return the current sort Comparer. This will always return a valid object
* as long as the FoundEntries was sorted via some method.
**/
//===================================================================
public Comparer getSortComparer();
//===================================================================
/**
* Return if the current FoundEntries is sorted in descending order.
**/
//===================================================================
//public boolean getSortIsDescending();
//===================================================================
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
public long getCurrentState();
//===================================================================
/**
 * Return if the FoundEntries has changed since the previous state.
 */
//===================================================================
public boolean hasChangedSince(long previousState);
//===================================================================
/**
 * Mark the FoundEntries as having been changed in some way.
 */
//===================================================================
public void change();
//===================================================================
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
public Object getData(int index, Object destination) throws IllegalStateException,IllegalArgumentException, IOException;
//===================================================================
/**
 * Get the data object from the specified index.
 * @param index the index in the FoundEntries object.
 * @return the data in a new Object.
  * @exception IllegalStateException if a new object could not be created.
  * @exception IOException if there is an error reading the data.
 */
//===================================================================
public Object getData(int index) throws IllegalStateException, IOException;
//===================================================================
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
public int setData(int index, Object data) throws IllegalArgumentException, IOException;
//===================================================================
/**
* This saves changes to the data entry and will rearrange current entries
* to maintain its sort order.
* @param index The index of the entry.
* @param changed the changed entry.
* @return the index where the entry is now.
* @exception IOException on error.
*/
//===================================================================
public int set(int index,DatabaseEntry changed) throws IOException;
//===================================================================
/**
 * This saves changes to the entry but does not modify its special synchronization fields.
 * It will rearrange current entries to maintain its sort order.
* @param index The index of the entry.
* @param changed the changed entry.
* @return the index where the entry is now.
* @exception IOException on error.
 */
//===================================================================
public int store(int index,DatabaseEntry changed) throws IOException;
//===================================================================

/**
 * Set the data at the specified index using the data object. This does not maintain the
 * sort order.
 * @param index the index in the FoundEntries object.
 * @param data the data object.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IllegalStateException if the found entries is not allowed to be in an unsorted state
 * (say because it belongs to a DatabaseIndex).
 * @exception IOException if there is an error writing the data.
 */
//===================================================================
//public void setDataInPlace(int index, Object data) throws IllegalStateException,IllegalArgumentException, IOException;
//===================================================================
/**
 * Add a new entry using the data object. This maintains the
 * sort order.
 * @param data the data object.
 * @return the index of the new entry.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IOException if there is an error writing the data.
 */
//===================================================================
public int addData(Object data) throws IllegalArgumentException, IOException;
//===================================================================
/**
 * Add a new entry using the data object to the end of the FoundEntries.
 * This does not maintains the sort order.
 * @param data the data object.
 * @return the index of the new entry.
 * @exception IllegalArgumentException if the destination object is not the right type.
 * @exception IOException if there is an error writing the data.
 * @exception IllegalStateException if the found entries is not allowed to be in an unsorted state
 * (say because it belongs to a DatabaseIndex).
 */
//===================================================================
//public int appendData(Object data) throws IllegalArgumentException, IOException, IllegalStateException;
//===================================================================
/**
 * Add an entry to the FoundEntries if it was not added before. This does not change the
 * data in the database.
 * @param entry the entry to include.
 * @return the index of the entry as it is now in the database.
 * @exception IllegalArgumentException if the entry is not a valid saved entry.
 * @exception IOException on error.
 */
//===================================================================
public int include(DatabaseEntry entry) throws IllegalArgumentException, IOException;
//===================================================================
/**
 * Exclude the entry from this FoundEntries if it is in it.
 * @param entry the entry to exclude.
 * @exception IllegalArgumentException if the entry is not a valid saved entry.
 * @exception IOException on error.
 * @exception IllegalStateException if the FoundEntries is all-inclusive or is a DatabaseIndex.
 */
//===================================================================
public void exclude(DatabaseEntry entry) throws IllegalArgumentException, IOException, IllegalStateException;
//===================================================================
/**
 * Exclude the entry from this FoundEntries.
 * @param index the index of the entry.
 * @exception IOException on error.
 * @exception IllegalStateException if the FoundEntries is all-inclusive or is a DatabaseIndex.
 */
//===================================================================
public void exclude(int index) throws IOException, IllegalStateException;
//===================================================================
/**
 * If a FoundEntries is all inclusive then adding entries to the database will automatically
 * add the new entries to the FoundEntries. FoundEntries associated with a DatabaseIndex
 * are all inclusive.
* @param allInclusive true to set the FoundEntries to all inclusive, false to set to be not
* all inclusive.
* @exception IllegalArgumentException if you try to set a FoundEntries associated with a
* DatabaseIndex to non all inclusive.
*/
//===================================================================
public void setAllInclusive(boolean allInclusive) throws IllegalArgumentException;
//===================================================================
/**
 * If a FoundEntries is all inclusive then adding entries to the database will automatically
 * add the new entries to the FoundEntries. FoundEntries associated with a DatabaseIndex
 * are all inclusive.
 * @return true if the FoundEntries is considered all inclusive.
 */
//===================================================================
public boolean isAllInclusive();
//===================================================================
//===================================================================
//public IntArray filter(Handle h,ObjectFinder finder,IntArray destination) throws IOException;
//===================================================================
//===================================================================
//public IntArray find(Handle h,Object searchData,Comparer comparer,IntArray destination) throws IOException, IllegalStateException;
//===================================================================
//===================================================================
//public IntArray find(Handle h,Object primaryFieldDataMask,IntArray destination) throws IOException, IllegalStateException;
//===================================================================
//===================================================================
//public Handle filter(ObjectFinder finder,IntArray destination);
//===================================================================
//===================================================================
//public Handle find(Object searchData,Comparer comparer,IntArray destination);
//===================================================================
//===================================================================
//public Handle find(Object primaryFieldDataMask,IntArray destination);
//===================================================================
//===================================================================
//public IntArray findAll(Object searchData,Comparer comparer,IntArray dest) throws IOException, IllegalStateException;
//===================================================================
//===================================================================
//public IntArray findAll(Object primaryFieldDataMask,IntArray dest) throws IOException, IllegalStateException;
//===================================================================
//===================================================================
//public IntArray findAll(Comparer comparer) throws IOException, IllegalStateException;
//===================================================================
//===================================================================
//public IntArray filterAll(ObjectFinder finder,IntArray dest)  throws IOException;
//===================================================================
//===================================================================
//public IntArray filterAll(Object searchData, Comparer comparer, IntArray dest) throws IOException;
//===================================================================
/**
 * If you add a new entry to a sorted FoundEntries, then that new entry may match
 * exactly other entries already in the FoundEntries. The FoundEntries then must
 * decide whether to add the new one before the other matching entries or after
 * the other matching entries. This method tells the FoundEntries whether to add
 * the new one before or after(the default).
 * @param addFirst if this is true then the new entry is added before the matching entries.
 */
//===================================================================
public void setAddNewEntriesFirst(boolean addFirst);
//===================================================================
/**
* Check if the proposed search criteria is compatible with the FoundEntries current sort.
* If this returns false then a call to search() will have to filter all the data (i.e.
* step through each one in turn). If it returns true then a search() will perform a
* quick binary search on the data.
* @param criteria the proposed sort criteria.
* @return true if it is compatible with the current sort, false if not.
*/
//===================================================================
public boolean searchIsCompatibleWithSortState(int[] criteria);
//===================================================================
/*
Search for entries either using a quick find() method (if the criteria is compatible
with the FoundEntries current sortCriteria AND the FoundEntries is in a sorted state) or
by using a long filter() method (otherwise).

 * @param h a handle to monitor the search.
 * @param searchData This must be one of the following:
<ul>
<li><b>An array of objects.</b> Each element of the array will be a field value wrapped
in an appropriate object if necessary (e.g. an INTEGER value must be stored in a ewe.sys.Long
Object). Each element must match an element in criteria (if criteria is null then the
FoundEntries current criteria will be used).
<li><b>A DatabaseEntry object.</b> The fields that you want to search for as specified
by the search criteria must be set in the object. If no criteria is specified then all the sort criteria is used.
<li><b>A data object.</b> This data object must be an instance of the one associated
with the database. The fields that you want to search for must be set in the object. If
no criteria is specified then all the sort criteria is used.
</ul>
 * @param criteria the search criteria. If this is null then the FoundEntries own search
 * criteria will be used. If that is also null an IllegalArgumentException will be thrown.
 * @param hasWildCards set this true if the search data contains wild cards. If not then
 * exact matches will be searched for.
 * @return The destination IntArray or a new one, containing the found data.
 */




/**
 * Get an EntrySelector for the data given the search data.
 * @param searchData the data to search for.
 * @param hasWildCards this should be true if the data contains wild cards.
 * @return a new EntrySelector for the data given the search data.
 * @exception IllegalStateException
 */
//===================================================================
public EntrySelector getEntrySelector(Object searchData,boolean hasWildCards) throws IllegalStateException;
//===================================================================
/**
 * Get a new EntriesView for this FoundEntries that is initially empty.
 */
//===================================================================
public EntriesView getEmptyView();
//===================================================================
/**
 * Get an EntriesView that contains all the entries in this FoundEntries.
 * @param destination an optional EntriesView object to hold the data.
 * @return the destination or new EntriesView.
 */
//===================================================================
//===================================================================
public EntriesView getFullView(EntriesView destination);
//===================================================================
/**
* Get a SubSet of this FoundEntries using the indexes included in the view.
* If this FoundEntries is sorted the specified view will first be sorted before
* the new FoundEntries is created.
**/
//===================================================================
public FoundEntries getSubSet(EntriesView view);
//===================================================================
/**
* Include all the entries as specified from the view of another FoundEntries
* in this FoundEntries.
* @param h an optional handle used to monitor and possibly abort the process.
* @param viewFromOtherFoundEntries a view into another FoundEntries object.
* @return true if the operation completed successfully, false if it did not.
* @exception IOException
*/
//===================================================================
public boolean includeAll(Handle h,EntriesView viewFromOtherFoundEntries)
throws IOException;
//===================================================================
/**
 * Exclude all the entries as specified from the view of another FoundEntries
 * from this FoundEntries.
	* @param h an optional handle used to monitor and possibly abort the process.
	* @param viewFromOtherFoundEntries a view into another FoundEntries object.
	* @return true if the operation completed successfully, false if it did not.
 * @exception IOException
 */
//===================================================================
public boolean excludeAll(Handle h,EntriesView viewFromOtherFoundEntries)
throws IOException;
//===================================================================
/**
* This finds the first index of the entry which (according to the provided
* Comparer) matches a search criteria.
* A binary chop search is done so it is
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
* @param h an optional handle used to monitor and possibly abort the process.
* @param searchData search data possibly used by the comparer - this can be null.
* @param comparer a comparer to compare the search data with the data in records of the FoundEntries.
* @return the index of the first matching entry or -1 if no match was found.
* @exception IOException
* @exception IllegalStateException if this FoundEntries is not sorted.
* @exception HandleStoppedException if the operation was aborted.
*/
//===================================================================
public int findFirst(Handle h,Object searchData,Comparer comparer)
throws IOException, IllegalStateException, HandleStoppedException;
//===================================================================
/**
* This finds the last index of the entry which (according to the provided
* Comparer) matches a search criteria.
* A binary chop search is done so it is
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
* @param h an optional handle used to monitor and possibly abort the process.
* @param searchData search data possibly used by the comparer.
* @param comparer a comparer to compare the search data with the data in records of the FoundEntries.
* @return the index of the first matching entry or -1 if no match was found.
* @exception IOException
* @exception IllegalStateException if this FoundEntries is not sorted.
* @exception HandleStoppedException if the operation was aborted.
*/
//===================================================================
public int findLast(Handle h,Object searchData,Comparer comparer)
throws IOException, IllegalStateException, HandleStoppedException;
//===================================================================
/**
* Find the index of the first entry that matches a search criteria.
* @param h an optional handle used to monitor and possibly abort the process.
* @param searchData search data possibly used by the comparer.
* @param hasWildCards set this true if the search data has wildcard data (e.g. '*' characters).
* @return the index of the first matching entry or -1 if no match was found.
* @exception IOException
* @exception IllegalStateException if this FoundEntries is not sorted.
* @exception HandleStoppedException if the operation was aborted.
*/
//===================================================================
public int findFirst(Handle h,Object searchData,boolean hasWildCards)
throws IOException, IllegalStateException, HandleStoppedException;
//===================================================================
/**
* Find the index of the last entry that matches a search criteria.
* @param h an optional handle used to monitor and possibly abort the process.
* @param searchData search data possibly used by the comparer.
* @param hasWildCards set this true if the search data has wildcard data (e.g. '*' characters).
* @return the index of the first matching entry or -1 if no match was found.
* @exception IOException
* @exception IllegalStateException if this FoundEntries is not sorted.
* @exception HandleStoppedException if the operation was aborted.
*/
//===================================================================
public int findLast(Handle h,Object searchData,boolean hasWildCards)
throws IOException, IllegalStateException, HandleStoppedException;
//===================================================================
/**
* Find the index where the specified entry would be added in the FoundEntries.
**/
//===================================================================
public int findInsertIndex(DatabaseEntry toAdd) throws ewe.io.IOException;
//===================================================================
/**
 * Find the range of indexes which match the search criteria as specified in the EntrySelector
 * parameter.
 * @param h An optional handle that can be used to monitor the progress of the operation.
 * @param selector An EntrySelector used to determine which entries to include in the range.
 * @param dest An optional destination Range object.
 * @return The destination or new Range object. If it returns null then the search was
 * aborted because the stop() method was called on the Handle. If it returns a Range where
 * the "first" field is less than zero then this indicates that no entries match. Otherwise
 * it will return a valid Range where the matching entries are specified by the "first" and
 * "last" fields of the returned Range (first and last are both inclusive).
 * @exception IOException On error reading the database.
 * @exception IllegalArgumentException If the EntrySelector specifies a search that is not
 * compatible with the sort criteria used by the FoundEntries.
 */
//===================================================================
public Range findRange(Handle h,EntrySelector selector,Range dest) throws IOException, IllegalArgumentException;
//===================================================================
/**
 * Find, using a binary chop search, all entries in the FoundEntries that match the
 * criteria as specified by the selector.
 * @param h an optional handle used to monitor and possibly abort the search.
 * @param selector an EntrySelector created using getEntrySelector().
 * @param dest an optional destination IntArray.
 * @return the destination IntArray or a new IntArray if dest was null. If the operation was
 * aborted, null will be returned.
 * @exception IOException
 */
//===================================================================
public IntArray findAll(Handle h,EntrySelector selector,IntArray dest) throws IOException;
//===================================================================
/**
 * Find, using an entry by entry sequential search, all entries in the FoundEntries that match the
 * criteria as specified by the selector.
 * @param h an optional handle used to monitor and possibly abort the search.
 * @param finder an ObjectFinder used to select the entries.
 * @param dest an optional destination IntArray.
 * @return the destination IntArray or a new IntArray if dest was null. If the operation was
 * aborted, null will be returned.
 * @exception IOException
 */
//===================================================================
public IntArray filterAll(Handle h,ObjectFinder finder,IntArray dest) throws IOException;
//===================================================================
/**
* Get the EventDispatcher for this FoundEntries.
**/
//===================================================================
public EventDispatcher getEventDispatcher();
//===================================================================
//
//FIXME
//
/**
* Get the DatabaseIndex associated with the FoundEntries if one is.
* @return the DatabaseIndex associated with the FoundEntries or null if none is.
*/
//===================================================================
//public DatabaseIndex getDatabaseIndex();
//===================================================================
/**
* This method will go through the entries and add to the FoundEntries any entries that
* are not included in the FoundEntries. Since this may take some time it is done in
* a new Thread.
* @return A handle with which you can check the progress of the operation and abort the
* operation if necessary.
*/
//===================================================================
//public Handle includeMissingEntries();
//===================================================================

//
// FIXME
//
/*
//===================================================================
public IntArray getIndexSet(RangeList ranges,IntArray destination);
//===================================================================
//===================================================================
public void invertIndexSet(IntArray indexSet);
//===================================================================
*/
//##################################################################
}
//##################################################################

