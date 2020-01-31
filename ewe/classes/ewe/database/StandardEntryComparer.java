/* $MirOS: contrib/hosted/ewe/classes/ewe/database/StandardEntryComparer.java,v 1.2 2007/08/30 22:39:22 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2007 Thorsten “mirabilos” Glaser, Dr. Robert “Pfeffer” Arnold  *
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

//##################################################################
public class StandardEntryComparer extends EntryComparer{
//##################################################################
private int [] criteria;
{
	compareAsDatabaseEntries = true;
}
//===================================================================
public int[] getCriteria()
//===================================================================
{
	return DatabaseUtils.copyCriteria(criteria);
}
//===================================================================
public StandardEntryComparer(Database db, int sortID) throws IllegalArgumentException, java.lang.Exception
//===================================================================
{
	this(db,db.toCriteria(sortID));
}
//===================================================================
public StandardEntryComparer(Database db, int[] criteria) throws IllegalArgumentException, java.lang.Exception
//===================================================================
{
	super(db);
	if (criteria == null) throw new NullPointerException();
	this.criteria = DatabaseUtils.copyCriteria(criteria);
}
//===================================================================
public EntrySelector toEntrySelector(Object searchData, boolean hasWildCards)
//===================================================================
{/*
	if (searchData instanceof Object[]){
		int num = ((Object[])searchData).length;
		if (num > criteria.length) num = criteria.length;
		return new EntrySelector(db,searchData,DatabaseUtils.getCriteriaSubset(criteria,num),hasWildCards);
	}
	*/
	return new EntrySelector(db,searchData,criteria,hasWildCards);
}
//-------------------------------------------------------------------
protected int compareEntries(Object one, Object two)
//-------------------------------------------------------------------
{
	if (one == two) return 0;
	if (one == null) return -1;
	else if (two == null) return 1;
	else return ((DatabaseEntry)one).compareTo((DatabaseEntry)two,criteria,false);
}

//##################################################################
}
//##################################################################
