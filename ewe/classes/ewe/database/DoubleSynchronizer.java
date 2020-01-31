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
import ewe.util.*;
import ewe.sys.Time;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.ui.*;
/**
* This Synchronizes two Databases where you have full local access to both.
**/
//##################################################################
public class DoubleSynchronizer extends Synchronizer{
//##################################################################

protected Database remote;

//===================================================================
public DoubleSynchronizer(Database local, Database remote)
//===================================================================
{
	super(local);
	this.remote = remote;
}
EntriesView myUnsynched;
long [] myDeleted;
FoundEntries myByOid;

//===================================================================
public int countRemoteUnsynchronizedEntries() throws ewe.io.IOException
//===================================================================
{
	if (myUnsynched == null) myUnsynched = getUnsynchronized(null,remote);
	return myUnsynched.size();
}
//===================================================================
public byte [] getRemoteUnsynchronizedEntry(int index) throws ewe.io.IOException
//===================================================================
{
	countRemoteUnsynchronizedEntries();
	DatabaseEntry de = myUnsynched.get(index);
	return de.encode();
}
//===================================================================
public void markRemoteAsSynchronized(int index) throws ewe.io.IOException
//===================================================================
{
	countRemoteUnsynchronizedEntries();
	markAsSynchronized(myUnsynched,index);
}
//===================================================================
public int countRemoteDeletedEntries() throws ewe.io.IOException
//===================================================================
{
	if (myDeleted == null) myDeleted = remote.getDeletedEntries();
	return myDeleted.length;
}
//===================================================================
public long getRemoteDeletedEntry(int index) throws ewe.io.IOException
//===================================================================
{
	countRemoteDeletedEntries();
	return myDeleted[index];
}
//===================================================================
public void eraseDeletedOnRemote(int index) throws ewe.io.IOException
//===================================================================
{
	long which = getRemoteDeletedEntry(index);
	remote.eraseDeletedEntry(which);
}
//===================================================================
public void eraseEntryOnRemote(long OID) throws ewe.io.IOException
//===================================================================
{
	if (myByOid == null) myByOid = getByOID(null,remote);
	int found = findByOID(myByOid,OID);
	if (found != -1) myByOid.erase(found);
}
//===================================================================
public void sendEntryToRemote(DatabaseEntry de) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry r = remote.getNewData();
	r.duplicateFrom(de);
	if (myByOid == null) myByOid = getByOID(null,remote);
	addOrReplace(de,myByOid);
}


//##################################################################
}
//##################################################################

