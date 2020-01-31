/* $MirOS: contrib/hosted/ewe/classes/ewe/database/Synchronizer.java,v 1.2 2007/08/30 22:45:53 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2007 Thorsten Glaser <tg@mirbsd.de>                            *
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
import ewe.sys.Vm;
import ewe.ui.*;
/**
* This class provides utilities for synchronizing items in a mobile database with
* another. The methods in here only support synchronization with a single other database,
* you will have to implement more complex methods to support any other type of synchronization.
**/
//##################################################################
public class Synchronizer{
//##################################################################

Database database;
public Time syncTime = new Time();

//===================================================================
public Synchronizer(Database database)
//===================================================================
{
	this.database = database;
}
//-------------------------------------------------------------------
private int checkSort(String sortName)
//-------------------------------------------------------------------
{
	return checkSort(database,sortName);
}
//-------------------------------------------------------------------
protected static int checkSort(Database database,String sortName) throws IllegalStateException
//-------------------------------------------------------------------
{
	int got = database.findSort(sortName);
	if (got == 0) throw new IllegalStateException("Database does not have the appropriate sort criteria.");
	return got;
}
//===================================================================
public static EntriesView getUnsynchronized(Handle h,Database database) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	FoundEntries all = database.getFoundEntries(h,checkSort(database,database.SyncSortName));
	if (all == null) return null;
	return all.getEmptyView().search(h,new EntrySelector(all,(Object)null,1,true){
		protected int doCompare(DatabaseEntry data,DatabaseEntry de){
			if (!de.hasField(Database.FLAGS_FIELD)) return 1;
			int flags = de.getField(Database.FLAGS_FIELD,0);
			if ((flags & Database.FLAG_SYNCHRONIZED) != 0) return -1;
			return 0;
		}
	});
}
/**
* This gets all the unsynchronized entries, but not the deleted entries. You
* can get deleted items from the database directly.
 * @exception ewe.io.IOException
 * @exception IllegalStateException If the database does not have the "_SortBySync" sort
**/
//===================================================================
public EntriesView getUnsynchronized(Handle h) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return getUnsynchronized(h,database);
}
//===================================================================
public static void markAsSynchronized(DatabaseEntry entry)
//===================================================================
{
	int value = entry.getField(Database.FLAGS_FIELD,0);
	value |= Database.FLAG_SYNCHRONIZED;
	entry.setField(Database.FLAGS_FIELD,value);
}
/**
* This marks the entry at the specified index as being synchronized.
**/
//===================================================================
public static void markAsSynchronized(EntriesView entries,int index) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry de = entries.get(index);
	if (de != null){
		markAsSynchronized(de);
		entries.store(index,de);
	}
}
//===================================================================
public static FoundEntries getByOID(Handle h,Database database) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return database.getFoundEntries(h,checkSort(database,Database.OidSortName));
}
//===================================================================
public FoundEntries getByOID(Handle h) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return getByOID(h,database);
}
/**
* This locates the entry with the specified OID. The FoundEntries object must
* have been found using the findByOID() method (i.e. using the sortByOid
* criteria).
* @param entries The FoundEntries of all the entries.
* @param oid The oid looking for.
* @return The index of the entry, -1 if it was not found.
*/
//===================================================================
public static int findByOID(FoundEntries entries,long oid) throws ewe.io.IOException
//===================================================================
{
	//return entries.findFirst(entries.getSearchData(Database.OID_FIELD,new ewe.sys.Long().set(oid)),entries.getFieldComparer());
	try{
		return entries.findFirst(null,new ewe.sys.Long().set(oid),false);
	}catch(HandleStoppedException e){
		return -1;
	}
}
/**
 * Get the OID for an entry.
 * @param entry The entry to look for.
 * @return the OID for the entry.
 * @exception IllegalStateException if the entry does not have an OID assigned.
 */
//===================================================================
public static long getOID(DatabaseEntry entry) throws IllegalStateException
//===================================================================
{
	if (!entry.hasField(Database.OID_FIELD)) throw new IllegalStateException("No OID assigned.");
	return entry.getField(Database.OID_FIELD,(long)0);
}
/**
 * Returns the state of the synchronized flag.
 * @param entry The entry to check.
 * @return true if the entry has the syncrhonized flag set, false if not.
 * @exception IllegalStateException If synchronization data is not set in the entry.
 */
//===================================================================
public boolean isSynchronized(DatabaseEntry entry) throws IllegalStateException
//===================================================================
{
	if (!entry.hasField(Database.FLAGS_FIELD)) throw new IllegalStateException("No Flags assigned.");
	return ((entry.getField(Database.FLAGS_FIELD,(int)0) & Database.FLAG_SYNCHRONIZED) != 0);
}
//===================================================================
public static int getMatchingEntry(DatabaseEntry remoteEntry,FoundEntries myEntriesSortedByOID) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	long oid = getOID(remoteEntry);
	return findByOID(myEntriesSortedByOID,oid);
}
/**
* Given a remote entry, add or replace it in this database.
**/
//===================================================================
public static void addOrReplace(DatabaseEntry entry,FoundEntries myEntriesSortedByOID) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	int toReplace = getMatchingEntry(entry,myEntriesSortedByOID);
	if (toReplace == -1){
		//ewe.sys.Vm.debug("Adding: "+entry.getField(Database.OID_FIELD,(long)0)+" = "+entry.getData());
		DatabaseEntry de = myEntriesSortedByOID.getNew();
		de.duplicateFrom(entry);
		markAsSynchronized(de);
		de.store();
	}else{
		DatabaseEntry de = myEntriesSortedByOID.get(toReplace);
		/*
		ewe.sys.Vm.debug("Replacing oid: "+de.getField(Database.OID_FIELD,(long)0)+" with: "+
			entry.getField(Database.OID_FIELD,(long)0));
		ewe.sys.Vm.debug("Replacing: "+de.getData()+"with: "+entry.getData());
		*/
		de.duplicateFrom(entry);
		markAsSynchronized(de);
		myEntriesSortedByOID.store(toReplace,de);
	}
}
/**
 * This is called after an OID marked as deleted on this database has been
 * erased from the remote database. It erases it from this database too.
 * @param oid The oid to erase.
 * @exception ewe.io.IOException
 */
//===================================================================
public void eraseDeleted(long oid) throws ewe.io.IOException
//===================================================================
{
	database.eraseDeletedEntry(oid);
}
/**
* Given the OID of an entry in the remote database, erase it completely from this database.
**/
//===================================================================
public void erase(long oid,FoundEntries myEntriesSortedByOID) throws ewe.io.IOException
//===================================================================
{
	int found = findByOID(myEntriesSortedByOID,oid);
	if (found == -1) return;
	myEntriesSortedByOID.erase(found);
}

//===================================================================
public int countRemoteUnsynchronizedEntries() throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//===================================================================
public int countRemoteDeletedEntries() throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//===================================================================
public long getRemoteDeletedEntry(int index) throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//===================================================================
public void eraseDeletedOnRemote(int index) throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//===================================================================
public void eraseEntryOnRemote(long OID) throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//===================================================================
public byte [] getRemoteUnsynchronizedEntry(int index) throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//===================================================================
public void markRemoteAsSynchronized(int index) throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//===================================================================
public void sendEntryToRemote(DatabaseEntry de) throws ewe.io.IOException
//===================================================================
{
	throw new IllegalStateException("You must override this method.");
}
//-------------------------------------------------------------------
protected String getDisplayString(DatabaseEntry entry)
//-------------------------------------------------------------------
{
	return mString.toString(entry.getData(null));
}
//-------------------------------------------------------------------
protected String getResolveString(DatabaseEntry remote,DatabaseEntry local)
//-------------------------------------------------------------------
{
	return "The item:\n"+getDisplayString(remote)+
	"\n was modified on the remote database \nand on the local database.\n\nWhat action should be taken?";
}

protected int lastConflictCommand = 0;

public static final int REMOTE_TO_LOCAL = 1;
public static final int LOCAL_TO_REMOTE = 2;
public static final int RESOLVE_LATER = 3;
public static final int ALWAYS = 0x40000000;
public static final int CANCELLED = 0;
/**
 * This must resolve a conflict when an item has been modified on both databases.
 * @param remote The remote entry.
 * @param local The local entry.
 * @return RESOLVE_LATER or REMOTE_TO_LOCAL or LOCAL_TO_REMOTE or CANCELLED. This can
	 also be ORed with ALWAYS so that the value will apply to other conflicts.
 */
//===================================================================
public int resolveConflict(DatabaseEntry remote,DatabaseEntry local)
//===================================================================
{
	MessageBox mb = new MessageBox("Resolve Conflict",getResolveString(remote,local),MessageBox.MBOKCANCEL);
	mb.windowFlagsToClear |= Window.FLAG_HAS_CLOSE_BUTTON;
	ResolveConflict rc = new ResolveConflict();
	rc.fromCommand(lastConflictCommand);
	mb.getAfterPanel().addLast(rc.getEditor(0)).setTag(mb.INSETS,new ewe.fx.Insets(4,2,4,2));
	if (mb.execute() == mb.IDCANCEL) return CANCELLED;
	return lastConflictCommand = rc.toCommand();
}
/**
* This is called at the end of the syncrhonization. Override if necessary.
**/
//===================================================================
public void close()
//===================================================================
{
}
//===================================================================
public Handle synchronize()
//===================================================================
{
	final Handle h = new Handle();
	new ewe.sys.mThread(){
		public void run(){
			try{
				try{
					synchronize(h);
				}finally{
					close();
				}
				h.set(h.Succeeded);
			}catch(Exception e){
				e.printStackTrace();
				h.errorObject = e;
				h.set(h.Failed);
			}
		}
	}.start();
	return h;
}

/**
 * Start the synchronization and display an optional progress bar.
 * @param title if this is non-null a progress bar display will be shown.
 * @return The Handle that monitors the progress of the synchronization.
 */
//===================================================================
public Handle synchronize(final String title)
//===================================================================
{
	final Handle h = synchronize();
	if (title != null)
		new ewe.sys.mThread(){
			public void run(){
				ProgressBarForm pbf = new ProgressBarForm();
				pbf.showSubTask = true;
				pbf.showMainTask = false;
				pbf.showStop = true;
				pbf.execute(h,title);
			}
		}.start();
	return h;
}
/**
* If this is true then the local database will take entries from remote database
* but not vice versa. It is false by default.
**/
public boolean remoteToLocalOnly = false;
/**
* If this is true, then deleting an item on one database will not cause the item
* to be deleted in the other. It is false by default.
**/
public boolean dontSynchronizeDeletedItems = false;
/**
* This goes through the motion of synchronization but does not actually make any
* changes to either database.
**/
public boolean dontSyncJustDebug = false;

//===================================================================
public boolean synchronize(Handle progress) throws ewe.io.IOException
//===================================================================
{
	Handle h = progress == null ? new Handle() : progress;
//.................................................
	h.doing = "Checking databases...";
	h.progress = 0;
	h.set(h.Running);
	h.changed();
//.................................................
	int num = countRemoteUnsynchronizedEntries();
	int deleted = dontSynchronizeDeletedItems ? 0 : countRemoteDeletedEntries();
	int always = 0;
	Vector dontSync = new Vector();
	int total = num+deleted;
	//Vm.out.println("Total local to sync: "+total);
	int remoteSize = total;
	if (!remoteToLocalOnly){
 		total += getUnsynchronized(h.getSubHandle()).size();
		total += dontSynchronizeDeletedItems ? 0 : database.getDeletedSince(null).length;
	}
	//Vm.out.println("Total local & remote to sync: "+total);
	FoundEntries fe = num == 0 && deleted == 0 ? null : getByOID(h.getSubHandle());
	if (num != 0){
		for (int i = 0; i<num && !h.shouldStop; i++){
			try{
				h.doing = "Syncing: "+(i+1)+" of: "+total;
				h.progress = i/(float)total;
				h.changed();
				byte [] un = getRemoteUnsynchronizedEntry(i);
				if (un == null) break;
				DatabaseEntry de = fe.getNew();
				de.decode(un,0,un.length);
				if (dontSyncJustDebug)
					ewe.sys.Vm.debug("Remote entry: "+de.getData(null));
				int found = getMatchingEntry(de,fe);
				if (found != -1){
					DatabaseEntry my = fe.get(found);
					if (!isSynchronized(my)){
						int resolve = always == 0 ?  resolveConflict(de,my) : always;
						int r = resolve & ~ALWAYS;
						if (r == CANCELLED) return false;
						if ((resolve & ALWAYS) != 0) always = r;
						if (r == LOCAL_TO_REMOTE) {
							if (!dontSyncJustDebug)
								markRemoteAsSynchronized(i);
							else
								ewe.sys.Vm.debug("Marking remote entry as synchronized.");
							continue;
						}else if (r == RESOLVE_LATER){
							dontSync.add(new ewe.sys.Long().set(getOID(de)));
							//ewe.sys.Vm.debug("Resolve later: "+database.getData(de,null));
							continue;
						}
					}
				}
				if (!dontSyncJustDebug){
					addOrReplace(de,fe);
					markRemoteAsSynchronized(i);
				}else{
					ewe.sys.Vm.debug("Accepting remote entry.");
				}
			}catch(IllegalStateException e){
			}
		}
	}
	if (deleted != 0){
		for (int i = 0; i<deleted && !h.shouldStop; i++){
			h.doing = "Syncing: "+(num+i+1)+" of: "+total;
			h.progress = (num+i)/(float)total;
			h.changed();
			long del = getRemoteDeletedEntry(i);
			if (del == 0) break;
			if (!dontSyncJustDebug){
				erase(del,fe);
				eraseDeletedOnRemote(i);
			}else{
				ewe.sys.Vm.debug("Erasing deleted entry on local and remote.");
			}
		}
	}
	//ewe.sys.Vm.debug("--------- Local to remote ---------- : "+total);
//..................................................................................
	if (!remoteToLocalOnly){
//..................................................................................
		EntriesView mine = getUnsynchronized(h.getSubHandle());
		for (int i = 0; i<mine.size() && !h.shouldStop; i++){
			h.doing = "Syncing: "+(remoteSize+i+1)+" of: "+total;
			h.progress = (remoteSize+i)/(float)total;
			h.changed();
			DatabaseEntry de = mine.get(i);
			if (dontSyncJustDebug)
				ewe.sys.Vm.debug("Local entry: "+de.getData(null));
			if (dontSync.size() != 0)
				if (dontSync.find(new ewe.sys.Long().set(getOID(de))) != -1) continue;
			//ewe.sys.Vm.debug("Local->Remote: "+database.getData(de,null));
			if (!dontSyncJustDebug){
				sendEntryToRemote(de);
				markAsSynchronized(mine,i);
			}else{
				ewe.sys.Vm.debug("Sending entry and synchronizing.");
			}
		}
		if (!dontSynchronizeDeletedItems){
			long [] del = database.getDeletedSince(null);
			int ds = remoteSize+mine.size();
			for (int i = 0; i<del.length && !h.shouldStop; i++){
				h.doing = "Syncing: "+(ds+i+1)+" of: "+total;
				h.progress = (ds+i)/(float)total;
				h.changed();
				if (!dontSyncJustDebug){
					eraseEntryOnRemote(del[i]);
					eraseDeleted(del[i]);
				}else{
					ewe.sys.Vm.debug("Erasing on local and remote.");
				}
			}
		}
	}
//..................................................................................
	h.doing = "Complete.";
	h.progress = 1.0f;
	h.changed();
	return true;
}
/**
* This finds all items modified since the specfied date.
* @param Time The time after which you want to check for modifications.
* @return The entries found.
* @exception ewe.io.IOException
* @exception IllegalStateException if the database does not have the "Sort by Modified" sort
* criteria set.
*/
/*
//===================================================================
public FoundEntries getModifiedSince(final Time when) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	int id = database.findSort(database.ModifiedSortName);
	if (id == 0) throw new IllegalStateException("The database is not set to sort by Modified Date");
	FoundEntries all = database.getEntries(id);
	if (when == null) return all;
	return all.getSubSet(all.findAll(null,new Comparer(){
		Time myTime = new Time();
		public int compare(Object one,Object two){
			DatabaseEntry de = (DatabaseEntry)two;
			if (!de.hasField(Database.MODIFIED_FIELD)) return 1;
			Time t = de.getField(Database.MODIFIED_FIELD,myTime);
			return when.compareTo(t);
		}
	},null));
}
*/
/**
* This finds all items modified since the database was synchronized with another database.
**/
/*
//===================================================================
public FoundEntries getModifiedSinceLastSynchronized(int remoteDatabaseID) throws ewe.io.IOException, IllegalStateException
//===================================================================
{
	return getModifiedSince(database.getSynchronizedTime(remoteDatabaseID));
}
*/
/**
* This marks the database as having being fully synchronized with another at a point in time.
**/
/*
//===================================================================
public void finalizeSynchronization(int remoteDatabaseID,Time when) throws ewe.io.IOException
//===================================================================
{
	database.setSynchronizedTime(remoteDatabaseID,when == null ? new Time() : when);
}
*/

/*
public static void main(String args[])throws ewe.io.IOException
{
	ewe.sys.Vm.startEwe(args);
	Synchronizer sync = new Synchronizer(DataStore.openDatabase(args[0],"rw"));
	EntriesView view = sync.getUnsynchronized();
	for (int i = 0; i<fe.size(); i++){
		Object got = fe.getData(i);
		ewe.sys.Vm.debug(got.toString());
		sync.markAsSynchronized(view,i);
	}
	ewe.sys.Vm.debug("Done.");
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################


