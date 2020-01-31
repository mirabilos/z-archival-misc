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
import ewe.util.Comparer;
import ewe.util.ObjectFinder;
import ewe.util.Utils;
import ewe.util.ByteArray;
import ewe.io.DataProcessor;
import ewe.sys.Vm;
import ewe.io.IOException;

//##################################################################
public class EntrySelector{
//##################################################################
private DatabaseEntry buffer;
public DatabaseEntry de;
public Database db;
public int [] criteria;
public boolean hasWildCards = false;

public boolean forceAFilterSearch = false;

//===================================================================
public int[] getCriteria()
//===================================================================
{
	int[] ret = new int[criteria.length];
	System.arraycopy(criteria,0,ret,0,ret.length);
	return ret;
}
/**
 * Encode and append the EntrySelector to a ByteArray.
 * Override this if you are inheriting from EntrySelector.
 * @param destination the destination ByteArray. If this is null a new one will be created.
 * @param encryptor an optional encryptor for the field search data.
 * @return the destination ByteArray or a new ByteArray.
 * @exception IOException if there was an error encrypting the data.
 */
//===================================================================
public ByteArray encode(ByteArray destination,DataProcessor encryptor) throws IOException
//===================================================================
{
	if (destination == null) destination = new ByteArray();
	int where = destination.length;
	destination.makeSpace(where,4);
	de.encode(destination,encryptor);
	Utils.writeInt(destination.length-where-4,destination.data,where,4);
	where = destination.length;
	destination.makeSpace(where,2);
	destination.data[where] = hasWildCards ? (byte)1:(byte)0;
	int num = criteria == null ? 0 : criteria.length;
	destination.data[where+1] = (byte)num;
	where = destination.length;
	destination.makeSpace(where,4*num);
	for (int i = 0; i<num; i++){
		Utils.writeInt(criteria[i],destination.data,where,4);
		where += 4;
	}
	return destination;
}
/**
 * Decode the EntrySelector for use with the specified Database.
 * Override this if you are inheriting from EntrySelector.
 * @param db The Database this comparer will be used with.
 * @param source The encoded bytes.
 * @param offset The offset in the array of the bytes.
 * @param length The number of bytes.
 * @param decryptor An optional decryptor to decrypt the data.
 * @return the number of bytes read.
 * @exception IOException if there is an error decyrpting the data.
 */
//===================================================================
public int decode(Database db, byte[] source,int offset,DataProcessor decryptor) throws IOException
//===================================================================
{
	int original = offset;
	de = db.getNewData();
	int size = Utils.readInt(source,offset,4);
	offset += 4;
	de.decode(source,offset,size,decryptor);
	hasWildCards = source[offset++] != 0;
	int num = (int)source[offset++] & 0xff;
	criteria = new int[num];
	for (int i = 0; i<num; i++){
		criteria[i] = Utils.readInt(source,offset,4);
		offset += 4;
	}
	return offset-original;
}


/**
 Return if the specified FoundEntries is sorted by a search criteria that is compatible with
 this EntrySelector, such that a binary-chop search is possible using this EntrySelector
 on the specified FoundEntries. If forceAFilterSearch is true, this will always return false.
 */
//===================================================================
public boolean canSearch(FoundEntries fe)
//===================================================================
{
	if (forceAFilterSearch) return false;
	return fe.searchIsCompatibleWithSortState(criteria);
}
/**
 Return if the specified sort criteria is compatible with
 this EntrySelector, such that a binary-chop search is possible using this EntrySelector
 on the database. If forceAFilterSearch is true, this will always return false.
 */
//===================================================================
public boolean canSearch(int[] sortCriteria)
//===================================================================
{
	if (forceAFilterSearch) return false;
	return DatabaseUtils.searchIsCompatibleWithSort(criteria,sortCriteria);
}
/**
 Return if the specified sort ID for the EntrySelector's Database is compatible with
 this EntrySelector, such that a binary-chop search is possible using this EntrySelector
 on the database. If forceAFilterSearch is true, this will always return false.
 */
//===================================================================
public boolean canSearch(int sortID) throws IllegalArgumentException
//===================================================================
{
	if (forceAFilterSearch) return false;
	return canSearch(db.toCriteria(sortID));
}
//===================================================================
public EntrySelector(Database db,Object primarySearchFields,int sortID, boolean hasWildCards)
//===================================================================
{
	setup(db,primarySearchFields, DatabaseUtils.getCriteriaSubset(db,db.toCriteria(sortID),primarySearchFields), hasWildCards);
}
//===================================================================
public EntrySelector(FoundEntries fe,Object primarySearchFields, boolean hasWildCards)
//===================================================================
{
	int[] sortCriteria = fe.getSortCriteria();
	if (sortCriteria == null) throw new IllegalStateException();
	setup(fe.getDatabase(),primarySearchFields, DatabaseUtils.getCriteriaSubset(fe.getDatabase(),sortCriteria,primarySearchFields), hasWildCards);
}
//===================================================================
public EntrySelector(FoundEntries fe,Object searchData, int numberOfCriteria, boolean hasWildCards)
//===================================================================
{
	int[] sortCriteria = fe.getSortCriteria();
	if (sortCriteria == null) throw new IllegalStateException();
	setup(fe.getDatabase(),searchData, DatabaseUtils.getCriteriaSubset(sortCriteria,numberOfCriteria), hasWildCards);
}
//===================================================================
public EntrySelector(Database db,Object searchData, int [] criteria, boolean hasWildCards)
//===================================================================
{
	setup(db,searchData,DatabaseUtils.getCriteriaSubset(db,criteria,searchData),hasWildCards);
}
//===================================================================
public EntrySelector(Database db,Object searchData, int sortID, int numberOfCriteria, boolean hasWildCards)
throws IllegalArgumentException
//===================================================================
{
	setup(db,searchData,DatabaseUtils.getCriteriaSubset(db.toCriteria(sortID),numberOfCriteria),hasWildCards);
}
//-------------------------------------------------------------------
void setup(Database db,Object searchData, int [] criteria, boolean hasWildCards)
//-------------------------------------------------------------------
{
	this.criteria = criteria;
	this.db = db;
	buffer = db.getNewData();
	this.hasWildCards = hasWildCards;
	if (searchData == null) searchData = db.getNewData();
	if (!(searchData instanceof DatabaseEntry)) de = db.getNewData();
	if (searchData instanceof DatabaseEntry){
		de = (DatabaseEntry)searchData;
		if (de.getDatabase() != db) throw new IllegalArgumentException();
	}else if (db.isInstanceOfDataObject(searchData)){
		de.setData(searchData);
	}else if (DatabaseUtils.isCollection(searchData)){
		int len = DatabaseUtils.lengthOfCollection(searchData);
		for (int i = 0; i<len && i<criteria.length; i++)
			de.setFieldValue(DatabaseUtils.criteriaToField(criteria[i]),DatabaseUtils.criteriaToType(criteria[i]),DatabaseUtils.getInCollection(searchData,i));
	}else if (criteria.length == 1){
		de.setFieldValue(DatabaseUtils.criteriaToField(criteria[0]),DatabaseUtils.criteriaToType(criteria[0]),searchData);
	}else if (criteria.length == 0){
	}else throw new IllegalArgumentException();
}

//-------------------------------------------------------------------
protected int doCompare(DatabaseEntry myData, DatabaseEntry other)
//-------------------------------------------------------------------
{
	return de.compareTo(other,criteria,hasWildCards);
}
//===================================================================
public int compare(Object dataOrEntry) throws IllegalArgumentException
//===================================================================
{
	DatabaseEntry other = (dataOrEntry instanceof DatabaseEntry) ? (DatabaseEntry)dataOrEntry : buffer;
	if (other == buffer) other.setData(dataOrEntry);
	return doCompare(de,other);
}
//===================================================================
//public boolean lookingFor(Object obj) {return compare((DatabaseEntry)obj) == 0;}
//===================================================================

/*
//===================================================================
public int compare(Object willBeIgnored, Object entry)
//===================================================================
{
	return compare((DatabaseEntry)entry);
}
*/

//===================================================================
public ObjectFinder toObjectFinder()
//===================================================================
{
	return new ObjectFinder(){
		public boolean lookingFor(Object other){
			return compare(other) == 0;
		}
	};
}
//===================================================================
public Comparer toComparer()
//===================================================================
{
	return new Comparer(){
		public int compare(Object myData,Object other){
			return EntrySelector.this.compare(other);
		}
	};
}

//##################################################################
}
//##################################################################

