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
import ewe.sys.Time;
import ewe.sys.Handle;
import ewe.ui.*;
import ewe.io.*;

//##################################################################
public class RemoteSynchronizer extends Synchronizer{
//##################################################################
RemoteCallHandler remote;
FoundEntries unsynced;
FoundEntries oids;
public Handle handle;

//===================================================================
public void close()
//===================================================================
{
	remote.closeConnection();
}
//===================================================================
public RemoteSynchronizer(Database local,Stream connection)
//===================================================================
{
	super(local);
	this.remote = new RemoteCallHandler(connection,this);
	handle = remote.handle;
}

//===================================================================
public int countRemoteUnsynchronizedEntries() throws ewe.io.IOException
//===================================================================
{
	return new RemoteCall("countUnsynced()I").call(remote,null).getInt();
}
//===================================================================
public byte [] getRemoteUnsynchronizedEntry(int index) throws ewe.io.IOException
//===================================================================
{
	//ewe.sys.Vm.debug("Calling getUnsynced");
	return (byte [])new RemoteCall("getUnsynced(I)[B").add(index).call(remote,null).getObject();
}
//===================================================================
public void markRemoteAsSynchronized(int index) throws ewe.io.IOException
//===================================================================
{
	new RemoteCall("markAsSynchronized(I)V").add(index).call(remote,null);
}
//===================================================================
public void sendEntryToRemote(DatabaseEntry de) throws ewe.io.IOException
//===================================================================
{
	new RemoteCall("acceptEntry([B)V").add(de.toBytes()).call(remote,null);
}
//===================================================================
public int countRemoteDeletedEntries() throws ewe.io.IOException
//===================================================================
{
	return new RemoteCall("countDeleted()I").call(remote,null).getInt();
}
//===================================================================
public long getRemoteDeletedEntry(int index) throws ewe.io.IOException
//===================================================================
{
	return new RemoteCall("getDeleted(I)J").add(index).call(remote,null).getLong();
}
//===================================================================
public void eraseDeletedOnRemote(int index) throws ewe.io.IOException
//===================================================================
{
	new RemoteCall("eraseDeleted(I)V").add(index).call(remote,null);
}
//===================================================================
public void eraseEntryOnRemote(long OID) throws ewe.io.IOException
//===================================================================
{
	new RemoteCall("eraseEntry(J)V").add(OID).call(remote,null);
}


long [] deleted;
//===================================================================
public int countDeleted() throws ewe.io.IOException
//===================================================================
{
	if (deleted == null) deleted = database.getDeletedSince(null);
	return deleted.length;
}
//===================================================================
public long getDeleted(int index) throws ewe.io.IOException
//===================================================================
{
	int size = countDeleted();
	if (index >= size) return 0;
	return deleted[index];
}
//===================================================================
public void eraseDeleted(int index) throws ewe.io.IOException
//===================================================================
{
	database.eraseEntry(database.getDeletedEntry(deleted[index]));
}
//===================================================================
public void eraseEntry(long oid) throws ewe.io.IOException
//===================================================================
{
	if (oids == null) oids = getByOID();
	int idx = findByOID(oids,oid);
	if (idx != -1) oids.erase(idx);
}
/**
* This is called remotely by the controlling synchronizer.
**/
//===================================================================
public void markAsSynchronized(int index) throws ewe.io.IOException
//===================================================================
{
	//ewe.sys.Vm.debug("markAsSynchronized("+index+")");
	markAsSynchronized(unsynced,index);
}
//===================================================================
public int countUnsynced() throws ewe.io.IOException
//===================================================================
{
	//ewe.sys.Vm.debug("countUnsynced()");
	if (unsynced == null) unsynced = getUnsynchronized();
	return unsynced.size();
}
//===================================================================
public byte [] getUnsynced(int index) throws ewe.io.IOException
//===================================================================
{
	//ewe.sys.Vm.debug("getUnsynced("+index+")");
	int size = countUnsynced();
	if (index >= size) return null;
	DatabaseEntry de = unsynced.get(index);
	return de.toBytes();
}
//===================================================================
public void acceptEntry(byte [] data) throws ewe.io.IOException
//===================================================================
{
	//ewe.sys.Vm.debug("acceptEntry()");
	if (oids == null) oids = getByOID();
	DatabaseEntry de = oids.getNew();
	de.decodeFrom(data);
	addOrReplace(de,oids);
}
/*
//===================================================================
public static void testSynchronize(Database local,Database remote) throws ewe.io.IOException
//===================================================================
{
	ewe.net.Socket [] socks = ewe.net.SocketMaker.pipe();
	Synchronizer s = new RemoteSynchronizer(local,socks[0]);
	new RemoteSynchronizer(remote,socks[1]);
	s.synchronize("Synchronizing...");
}
*/
//===================================================================
public static RemoteSynchronizer synchronizeOnRemoteConnection(String databaseName,String [] remoteEweApplications,ewe.sys.TimeOut timeout,int makeOptions)
throws ewe.io.IOException, ewe.sys.TimedOutException, InterruptedException
//===================================================================
{
	Database db = DataStore.openDatabase(databaseName,"rw");
	RemoteConnection rc = RemoteConnection.getConnection();
	ewe.net.Socket sock = rc.makeSyncConnection("Sync-"+databaseName,remoteEweApplications,timeout,makeOptions);
	return new RemoteSynchronizer(db,sock);
}
//===================================================================
public static RemoteSynchronizer synchronizeOnRemoteConnection(String databaseName,String remoteEweApplication,ewe.sys.TimeOut timeout,int makeOptions)
throws ewe.io.IOException, ewe.sys.TimedOutException, InterruptedException
//===================================================================
{
	return synchronizeOnRemoteConnection(databaseName,new String[]{remoteEweApplication},timeout,makeOptions);
}
//===================================================================
public Handle synchronize(String desktopMessage,final String mobileMessage)
//===================================================================
{
	if (ewe.sys.Vm.isMobile()) {
		if (mobileMessage != null)
			new ewe.sys.mThread(){
				public void run(){
					ewe.ui.ProgressBarForm.display("Synchronizing",mobileMessage,null);
					try{
						handle.waitUntilStopped();
					}catch(Exception e){
					}finally{
						ewe.ui.ProgressBarForm.clear();
					}
				}
			}.start();
		return handle;
	}else{
		return synchronize(desktopMessage);
	}
}
/*
//===================================================================
public static void main(String args[]) throws ewe.io.IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	if (args.length > 1)
		testSynchronize(DataStore.openDatabase(args[0],"rw"),DataStore.openDatabase(args[1],"rw"));
	else{
		try{
			synchronizeOnRemoteConnection(args[0],(String)null,new ewe.sys.TimeOut(1000*30),RemoteConnection.MANUAL_MOBILE_START_ON_EMULATOR)
			.synchronize("Synchronizing","Desktop is synchronizing").
			waitUntilStopped();
		}catch(InterruptedException e){
		}catch(ewe.sys.TimedOutException e){
			e.printStackTrace();
		}
	}
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

