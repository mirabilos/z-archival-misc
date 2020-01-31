/* $MirOS: contrib/hosted/ewe/classes/ewe/database/SafeDBAccess.java,v 1.2 2007/08/30 22:35:57 tg Exp $ */
/*-
 * Created on May 2, 2005
 *
 * Michael L Brereton - www.ewesoft.com
 */
package ewe.database;

import ewe.database.Database;
import ewe.database.DatabaseMaker;
import ewe.database.DatabaseManager;
import ewe.database.EntriesView;
import ewe.database.FoundEntries;
import ewe.database.FoundEntriesObject;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.io.Stream;
import ewe.io.StreamUtils;
import ewe.sys.Lock;
import ewe.sys.Time;
import ewe.util.Hashtable;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipInputStream;
import ewe.util.zip.ZipOutputStream;

/**
 * @author Michael L Brereton
 *
 */
//####################################################
public class SafeDBAccess {

protected File directory;
protected String name;
protected String[] sortNames;
protected String previousMode;
protected int updateCount;
protected Hashtable views;
protected EntriesView anyView;

/**
 * This is the currently open Database. It is null if it is not open.
 */
public Database opened;
/**
 * If the Database is opened, this is the mode it was opened in. If it is open for
 * update, it will be "rw", otherwise it may be "r" or "rw".
 */
public String openMode;
/**
 * If the Database is open for updating, this will be true.
 */
public boolean isUpdating;
/**
 * All operations are synchronized on this lock.
 */
public Lock lock = new Lock();
/**
 * This is the DatabaseMaker used to open the Database. By default it is the
 * value returned by DatabaseManager.getDefaultDatabaseMaker();, but you can
 * replace it after calling the constructor.
 */
public DatabaseMaker maker = DatabaseManager.getDefaultDatabaseMaker();

/**
 * Set this true to temporarily disable restoring from a backup. If this is true and
 * open() or startUpdate() is called, and the database is either corrupt or missing
 * then an IOException will be thrown instead of a restore from backup.
 */
public boolean disableRestore;

/**
 * This gets the EntriesView created when the Database was opened for the specified
 * sortName.
 * @param sortName one of the sortNames provided in the constructor.
 * @return the created EntriesView or null if the name is incorrect or if the database
 * is not open.
 */
public EntriesView getView(String sortName)
{
	if (views == null) return null;
	return (EntriesView)views.get(sortName);
}
public EntriesView getAnyView()
{
	if (opened == null) return null;
	if (sortNames != null && sortNames.length != 0) return getView(sortNames[0]);
	return anyView;
}
/**
 * Create a SafeDBAccess object. After creating it, use open() or startUpdate() to
 * actually open the database.
 * @param directory The directory which contains the database.
 * @param dbName The root name (without the ".db" extension) of the database.
 * @param sortNames an optional set of sortNames for which EntriesView objects
 * will be created.
 */
public SafeDBAccess(File directory, String dbName, String[] sortNames)
{
	this.directory = directory;
	this.name = dbName;
	if (dbName.toUpperCase().endsWith(".DB"))
		name = name.substring(0,name.length()-3);
	this.sortNames = sortNames;
}

/**
 * This gets called when the DB has been successfully opened. By default this does
 * nothing.
 * @param db the open database.
 * @param mode the mode it was opened in.
 * @param forUpdate true if has been opened for updating.
 */
protected void databaseOpened(Database db,String mode,boolean forUpdate)
{
}
/**
 * This is called when the database has been closed. By default this does nothing.
 */
protected void databaseClosed()
{

}
/**
 * This restores the dabase from the backup file if one exists. The database must
 * be closed before this is done.
 * @throws IOException if disableRestore is true.
 * @throws RestoreException if there is any problem restoring the database, including a missing
 * or invalid backup zip file.
 */
protected void restore() throws IOException
{
	if (disableRestore) throw new IOException("Restore from backup required but disabled.");
	try{
		File backup = directory.getChild(name+".zip");
		if (!backup.exists())
			throw new RestoreException("No backup to restore from!");
		File jdb = directory.getChild(name+".db");
		if (jdb.exists() && !jdb.delete())
			throw new RestoreException("Cannot delete corrupted file - "+jdb);
		ZipInputStream in = new ZipInputStream(backup.toReadableStream());
		ZipEntry ze = in.getNextEntry();
		if (ze == null)
			throw new RestoreException("No data in zip file - "+backup);
		File temp = directory.getChild(name+".db_");
		OutputStream out = new OutputStream(temp.toWritableStream(false));
		StreamUtils.transfer(null,in,out);
		out.close();
		in.close();
		if (!temp.move(jdb))
			throw new RestoreException("Cannot rename temporary file - "+jdb);
	}catch(IOException e){
		if (!(e instanceof RestoreException))
			throw new RestoreException("Error during restore from backup!",e);
		else
			throw e;
	}
}
//
// The database must be closed before calling this.
//
/**
 * This makes a backup of the database. The database must be closed for this to work.
 * @throws BackupException if an error occurs.
 */
protected void makeBackup() throws BackupException
{
	try{
		File jdb = directory.getChild(name+".db");
		if (!jdb.exists()) throw new BackupException("Database file does not exist!");
		Database db = _validateAndOpen(jdb,"r");
		db.close();
		File backup = directory.getChild(name+".zip");
		Stream out = backup.toWritableStream(false);
		ZipOutputStream zos = new ZipOutputStream(out);
		ZipEntry ze = new ZipEntry(name+".db");
		ze.setMethod(ZipEntry.DEFLATED);
		long theSize = jdb.getLength();
		ze.setSize(theSize);
		ze.setTime(jdb.getModified(null));
		zos.putNextEntry(ze);
		InputStream in = new InputStream(jdb.toReadableStream());
		StreamUtils.transfer(null,in,zos);
		in.close();
		zos.finish();
		zos.close();
	}catch(IOException e){
		if (!(e instanceof BackupException))
			throw new BackupException("Error making backup.",e);
		else
			throw (BackupException)e;
	}
}
private Database _validateAndOpen(File f,String mode) throws IOException
{
	Database db = maker.openDatabase(f,mode);//new RecordFile(f,mode);
	//
	// Make sure all records are ok.
	//
	try{
		FoundEntries all = db.getFoundEntries(null,0);
		anyView = null;
		if (sortNames == null || sortNames.length == 0){
			all.setAllInclusive(true);
			anyView = all.getEmptyView();
		}
		if (sortNames != null) {
			views = new Hashtable();
			for (int i = 0; i<sortNames.length; i++){
				int id = db.findSort(sortNames[i]);
				if (id == 0) throw new IOException("Sort name: "+sortNames[i]+" not found.");
				FoundEntries fe = db.getFoundEntries(null,id);
				if ((fe instanceof FoundEntriesObject) && !((FoundEntriesObject)fe).validateEntries())
					throw new IOException("Invalid "+name+" index - "+sortNames[i]);
				fe.setAllInclusive(true);
				views.put(sortNames[i],fe.getEmptyView());
			}
		}
		return db;
	}catch(IOException e){
		try{
			if (db != null) db.close();
		}catch(IOException e2){}
		throw e;
	}
}
private void _validateAndOpen(File f,String mode,boolean forUpdate) throws IOException
{
		opened = _validateAndOpen(f,mode);
		openMode = mode;
		isUpdating = forUpdate;
		databaseOpened(opened,mode,forUpdate);
}

private void _open(String mode, boolean forUpdate) throws IOException
{
	File db = directory.getChild(name+".db");
	boolean didRestore = false;
	while(true){
		if (!db.exists()){
			didRestore = true;
			restore();
		}
		try{
			_validateAndOpen(db,mode,false);
			return;
		}catch(IOException e){
			if (didRestore) throw e;
			didRestore = true;
			restore();
		}
	}

}
private void _close() throws IOException
{
	if (opened == null) return;
	openMode = null;
	Database db = opened;
	opened = null;
	views = null;
	db.close();
}
/**
 * Close the database if it is not open for updating. endUpdate() should be used for
 * when closing after an update.
 * @return true on successful closing, false if it is currently open for updating.
 * @throws IOException on an IOError.
 */
public boolean close() throws IOException
{
	lock.synchronize();try{
		if (isUpdating) return false;
		_close();
		databaseClosed();
		return true;
	}finally{lock.release();}
}
/**
 * Open the database in either "r" or "rw" mode. This operation cannot be done if
 * the databse is opened for update via startUpdate(). If it is opened for update
 * it will return null.
 * <p>
 * @param mode either "r" or "rw" - any other value will generate an IllegalArgumentException
 * @return the opened database.
 * @throws IOException on any IO error.
 * @throws IllegalArgumentException if the mode is invalid.
 */
public Database open(String mode) throws IOException
{
	lock.synchronize();try{
		//
		if (isUpdating) return null;
		//
		if (mode.equals("r")){
			if (openMode != null) return opened;
		}else if (mode.equals("rw")){
			if (openMode != null && openMode.equals(mode)) return opened;
		}else{
			throw new IllegalArgumentException("Bad mode: "+mode);
		}
		_close();
		_open(mode,false);
		return opened;
	}finally{lock.release();}
}

/**
 * This calls openForCreate() and then calls closeCreate(). It is only useful if
 * initializer is not null other wise a database with no fields would be created.
 * A NullPointerException is thrown if initializer is null.
 * @param initializer the initializer used to initialize the database, which must be
 * non-null.
 * @return true if the database was created, false if it was not because it already
 * existed.
 * @throws IOException if an IO error occurs during creation.
 */
public boolean create(Object initializer) throws IOException
{
	if (initializer == null) throw new NullPointerException();
	lock.synchronize();try{
		if (openForCreate(initializer) == null) return false;
		closeCreate();
		return true;
	}finally{lock.release();}
}
/**
 * Open the database for creation only if it does not already exist. Make sure you
 * call closeCreate() on the returned Database when complete.
 * @param initializer an optional Object used to initialize the sorts and fields.
 * @return the open database ready for initialization if it did not exist, or null
 * if it did exist.
 * @throws IOException if an error occurs.
 */
public Database openForCreate(Object initializer) throws IOException
{
	lock.synchronize();try{
		if (opened != null) return null;
		File file = directory.getChild(name+".db");
		if (file.exists()) return null;
		File temp = directory.getChild(name+".db_");
		if (temp.exists() && !temp.delete())
			throw new IOException("Could not delete temporary file.");
		Database open = maker.openDatabase(temp,"rw");
		if (initializer != null){
			open.ensureFields(initializer,null,null);
			open.ensureSorts(initializer,null);
			open.save();
		}
		opened = open;
		openMode = "rw";
		return open;
	}finally{lock.release();}
}

/**
 * Call this after initializing with openForCreate(). If openForCreate() returned
 * null then do not call this method.
 * @throws IOException
 */
public void closeCreate() throws IOException
{
	lock.synchronize();try{
		if (opened == null) return;
		opened.save();
		opened.close();
		openMode = null;
		opened = null;
		File file = directory.getChild(name+".db");
		File temp = directory.getChild(name+".db_");
		if (!temp.move(file)) throw new IOException("Could not rename temporary file.");
	}finally{lock.release();}
}
/**
 * Close the database if it is open, make a backup and then open it in "rw" mode
 * for updating. During the update process the db is renamed to have a ".db_" extension
 * and will not be renamed back until the update is complete via endUpdate().
 * <p>
 * You can call startUpdate() multiple times but for each call to startUpdate() there
 * must be a corresponding call to endUpdate(). Update mode is only exited when the
 * last endUpdate() is called.
 * <p>
 * @return the opened database.
 * @throws IOException if an IO error occurs.
 */
public Database startUpdate() throws IOException
{
	lock.synchronize();try{
		if (isUpdating) {
			updateCount++;
			return opened;
		}
		previousMode = openMode;
		_close();
		makeBackup();
		File temp = directory.getChild(name+".db_");
		if (temp.exists())
			if (!temp.delete()) throw new IOException("Could not remove old temporary file - "+temp);
		File db = directory.getChild(name+".db");
		if (!db.exists()) restore();
		if (!db.renameTo(temp)) throw new IOException("Could not create new temporary file - "+temp);
		_validateAndOpen(temp,"rw",true);
		updateCount = 1;
		return opened;
	}finally{lock.release();}
}

/**
 * Abort the update process, close the db and do not rename the temp file to ".db".
 * The result of this is that the next time an open is done the backup file will be
 * used.
 * @return false if it is not in an update, true otherwise.
 * @throws IOException if an IOException occurs.
 */
public boolean abortUpdate() throws IOException
{
	lock.synchronize();try{
		if (!isUpdating) return false;
		isUpdating = false;
		updateCount = 0;
		_close();
		return true;
	}finally{lock.release();}
}
/**
 * End the update process. This will close the database and rename the ".db_" file
 * back to the ".db" file. After calling this you must call open() again to re-open
 * the database.
 * @return false if it is not in an update, true otherwise.
 * @throws IOException if an IOException occurs.
 */
public boolean endUpdate() throws IOException
{
	lock.synchronize();try{
		if (!isUpdating) return false;
		if (updateCount > 0) updateCount--;
		if (updateCount != 0) return true;
		isUpdating = false;
		_close();
		File temp = directory.getChild(name+".db_");
		if (!temp.exists()) throw new IOException("Could not find temporary update file - "+temp);
		File db = directory.getChild(name+".db");
		if (!temp.renameTo(db)) throw new IOException("Could not rename temporary update file - "+temp);
		if (previousMode != null) _open(previousMode,false);
		return true;
	}finally{lock.release();}
}

/**
 * This causes the database to be closed (if it is not open for updating) and then
 * backed up. If it was open before it will be re-opened in the same mode as before.
 * @return true if a backup was done, false if no backup was done because it was in
 * update mode.
 * @throws IOException if an IO error occurs.
 */
public boolean backupNow() throws IOException
{
	lock.synchronize();try{
		if (isUpdating) return false;
		previousMode = this.openMode;
		_close();
		makeBackup();
		if (previousMode != null) _open(previousMode,false);
		return true;
	}finally{lock.release();}
}
/**
 * If no backup exists at all, make it now.
 * @throws IOException if an IO error occurs.
 */
public void verifyBackup() throws IOException
{
	lock.synchronize();try{
		if (!directory.getChild(name+".zip").exists()) backupNow();
	}finally{lock.release();}
}
/**
 * Check if a backup for the database exists.
 * @return true if a backup for the database exists.
 */
public boolean backupExists()
{
	lock.synchronize();try{
		File f = directory.getChild(name+".zip");
		return f.exists();
	}finally{lock.release();}
}
/**
 * Return the time the backup was last made.
 * @param dest an optional destination Time.
 * @return the time the backup was last made, or null if the backup does not exist.
 */
public Time getBackupModifiedTime()
{
	lock.synchronize();try{
		File f = directory.getChild(name+".zip");
		if (!f.exists()) return null;
		return f.getModified(new Time());
	}finally{lock.release();}
}

/**
 * Return the time elapsed in milliseconds between the last backup time and
 * the provided Time.
 * @param since the Time to compare to the backup time, or null to use the current time.
 * @return the time elapsed in milliseconds between the last backup time and
 * the provided Time, or 0 if the backup time appears more recent than the "since" time
 * or -1 if no backup exists.
 */
public long timeSinceBackup(Time since)
{
	lock.synchronize();try{
		File f = directory.getChild(name+".zip");
		if (!f.exists()) return -1;
		if (since == null) since = new Time();
		long ret = since.getTime()-f.getModified(new Time()).getTime();
		if (ret < 0) ret = 0;
		return ret;
	}finally{lock.release();}
}

/**
 * Check if the database does exist.
 * This will only work if it is not open for update.
 * @return true if the dabase exists, false if it does not exist or if it is open for update.
 */
public boolean databaseExists()
{
	return getDatabaseModifiedTime(null) != null;
}
/**
 * Return the time the dabase was last modified.
 * This will only work if it is not open for update.
 * @param dest an optional destination Time.
 * @return the time the database was last modified, or null if the database does not
 * exist or is open for update.
 */
public Time getDatabaseModifiedTime(Time dest)
{
	lock.synchronize();try{
		if (isUpdating) return null;
		File f = directory.getChild(name+".db");
		if (!f.exists()) return null;
		return f.getModified(new Time());
	}finally{lock.release();}
}

}
//####################################################
