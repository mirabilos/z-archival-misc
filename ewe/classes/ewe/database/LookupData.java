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
import ewe.io.File;
import ewe.sys.Handle;
import ewe.util.Vector;
import ewe.data.DataObject;
import ewe.util.ObjectFinder;
import ewe.reflect.Reflect;
import ewe.reflect.Field;
import ewe.reflect.Wrapper;
//##################################################################
public class LookupData extends DataObject{
//##################################################################
/**
* This is the view into the Database created by getLookup().
**/
protected EntriesView view;
/**
 * Return the view used to lookup data in the database. This should only be called on
 * an object returned by one of the getLookup() values. If you are using this to do any
 * lookups yourself, you should call openLookup() on the view before looking up data and
 * then call closeLookup() afterwards.
 * @return the view used to lookup data in the database.
 */
//===================================================================
public EntriesView getLookupView()
//===================================================================
{
	return view;
}

/**
This is used to open the specified database. By default it uses a default DatabaseManager
as returned by DatabaseManager.getDefaultDatabaseMaker(), but you can override this to
open the Database in another way. This method is called by the getLookup() method - which
opens the database in read-only mode.
* @param databaseFile the File to open.
* @param mode the mode - either "r" or "rw".
* @return an open database.
 * @exception IOException if an error occurs.
*/
//===================================================================
public Database openDatabase(File databaseFile, String mode)
throws IOException
//===================================================================
{
	DatabaseMaker dm = DatabaseManager.getDefaultDatabaseMaker();
	if (!dm.databaseIsValid(databaseFile)) throw new IOException("Invalid or missing database: "+databaseFile);
	return dm.openDatabase(databaseFile,mode);
}
/**
 * Get a LookupData which can be used to lookup values based on the specified sort.
 * @param databaseFile the databaseFile to open (in read-only mode).
 * @param sortName an optional sort name. If null then the first stored sort is used.
 * @return a LookupData object that can be used to lookup values based on the specified sort.
 * @exception IOException if an error occurs.
 */
//===================================================================
public LookupData getLookup(File databaseFile, String sortName)
throws IOException
//===================================================================
{
	return getLookup(openDatabase(databaseFile,"r"),sortName);
}
/**
 * Get a LookupData which can be used to lookup values based on the specified sort.
 * @param openDatabase the open database to lookup in.
 * @param sortName an optional sort name. If null then the first stored sort is used.
 * @return a LookupData object that can be used to lookup values based on the specified sort.
 * @exception IOException if an error occurs.
 */
//===================================================================
public LookupData getLookup(Database openDatabase, String sortName)
throws IOException
//===================================================================
{
	Database db = openDatabase;
	int sortID = 0;
	if (sortName == null){
		int[]all = db.getSorts();
		if (all.length > 0) sortID = all[0];
	}else{
		sortID = db.findSort(sortName);
	}
	FoundEntries fe = db.getFoundEntries(null,sortID);
	LookupData ret = (LookupData) getNew();
	ret.view = fe.getEmptyView();
	ret.view.enableLookupMode();
	return ret;
}
/**
 * This looks up for all records which match the search criteria.
 * Only call this on an object returned by getLookup().
 * @param h an optional Handle that can be used to monitor or stop the search.
 * @param searches the fields to search for.
 * @param hasWildcards true if the fields contain wildcard characters.
 * @return a Vector of LookupData objects which contain the data from the matching records or
 * null if the search was aborted through the Handle h.
 * @exception IOException if an error occurs.
 */
//===================================================================
public Vector lookup(Handle h, Object[] searches, boolean hasWildcards)
throws IOException
//===================================================================
{
	view.openLookup();
	try{
		Vector v = new Vector();
		view.clear();
		if (view.search(h,searches,hasWildcards) == null)
			return null;
		for (int i = 0; i<view.size(); i++)
			v.add(view.getData(i));
		view.clear();
		return v;
	}finally{
		view.closeLookup();
	}
}
/**
 * This looks up for all records which match the search criteria.
 * Only call this on an object returned by getLookup().
 * @param h an optional Handle that can be used to monitor or stop the search.
 * @param filter an ObjectFinder that will filter each database record.
 * @return a Vector of LookupData objects which contain the data from the matching records or
 * null if the search was aborted through the Handle h.
 * @exception IOException if an error occurs.
 */
//===================================================================
public Vector lookup(Handle h, ObjectFinder filter) throws IOException
//===================================================================
{
	view.openLookup();
	try{
		Vector v = new Vector();
		view.clear();
		if (view.search(h,filter) == null)
			return null;
		for (int i = 0; i<view.size(); i++)
			v.add(view.getData(i));
		view.clear();
		return v;
	}finally{
		view.closeLookup();
	}
}
/**
 * This looks up for all records which match the search criteria.
 * Only call this on an object returned by getLookup().
 * @param searches the fields to search for.
 * @param hasWildcards true if the fields contain wildcard characters.
 * @return a Vector of LookupData objects which contain the data from the matching records.
 * @exception IOException if an error occurs.
 */
//===================================================================
public Vector lookup(Object[] searches, boolean hasWildcards)
throws IOException
//===================================================================
{
	return lookup(null,searches,hasWildcards);
}
/**
 * This looks up for all records which match the search criteria.
 * Only call this on an object returned by getLookup().
 * @param search one field to search for.
 * @param hasWildcards true if the field contain wildcard characters.
 * @return a Vector of LookupData objects which contain the data from the matching records.
 * @exception IOException if an error occurs.
 */
//===================================================================
public Vector lookup(Object search, boolean hasWildcards)
throws IOException
//===================================================================
{
	return lookup(new Object[]{search},hasWildcards);
}
/**
 * This looks up for one record which matches the search criteria.
 * Only call this on an object returned by getLookup().
 * @param searches the fields to search for.
 * @param hasWildcards true if the fields contain wildcard characters.
 * @return one LookupData object which contain the first matching data from the matching records or null
 * if none match.
 * @exception IOException if an error occurs.
 */
//===================================================================
public LookupData lookupOne(Object[] search, boolean hasWildcards) throws IOException
//===================================================================
{
	Vector v = lookup(search,hasWildcards);
	if (v.size() == 0) return null;
	return (LookupData)v.get(0);
}
/**
 * This looks up for one record which matches the search criteria.
 * Only call this on an object returned by getLookup().
 * @param search one field to search for.
 * @param hasWildcards true if the field contain wildcard characters.
 * @return one LookupData object which contain the first matching data from the matching records or null
 * if none match.
 * @exception IOException if an error occurs.
 */
//===================================================================
public LookupData lookupOne(Object search, boolean hasWildcards) throws IOException
//===================================================================
{
	return lookupOne(new Object[]{search},hasWildcards);
}
/**
 * This looks up for all records which match the search criteria.
 * Only call this on an object returned by getLookup().
 * @param filter an ObjectFinder that will filter each database record.
 * @return a Vector of LookupData objects which contain the data from the matching records.
 * @exception IOException if an error occurs.
 */
//===================================================================
public Vector lookup(ObjectFinder filter) throws IOException
//===================================================================
{
	return lookup(null,filter);
}
/**
 * This looks up for all records which match the search criteria and returns the first match.
 * Only call this on an object returned by getLookup().
 * @param filter an ObjectFinder that will filter each database record.
 * @return one LookupData object which contain the first matching data from the matching records or null
 * if none match.
 * @exception IOException if an error occurs.
 */
//===================================================================
public LookupData lookupOne(ObjectFinder filter) throws IOException
//===================================================================
{
	Vector v = lookup(filter);
	if (v.size() == 0) return null;
	return (LookupData)v.get(0);
}
/**
 * Close the lookup database.
 * @exception IOException on IO error.
 */
//===================================================================
public void closeLookup() throws IOException
//===================================================================
{
	view.getFoundEntries().getDatabase().close();
}
/*
//===================================================================
public Wrapper getFieldValue(String fieldName)
//===================================================================
{
	Field f = Reflect.getForObject(this).getField(fieldName,Reflect.PUBLIC);
	if (f == null) return null;
	return f.getValue(this,new Wrapper());
}
//===================================================================
public String getStringField(String fieldName)
//===================================================================
{
	Wrapper w = getFieldValue(fieldName);
	return w == null ? null : (String)w.getObject();
}
//===================================================================
public double getDoubleField(String fieldName)
//===================================================================
{
	Wrapper w = getFieldValue(fieldName);
	return w == null ? 0 : w.toDouble();
}
//===================================================================
public int getIntField(String fieldName)
//===================================================================
{
	Wrapper w = getFieldValue(fieldName);
	return w == null ? 0 : w.toInt();
}
*/
//##################################################################
}
//##################################################################

