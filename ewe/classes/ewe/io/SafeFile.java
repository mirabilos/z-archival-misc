package ewe.io;
/**
* This is used to "safely" modify the contents of a file. This is used when it is critical
* that the information in a file not be corrupted due to a crash during writing.<p>
* To use it to modify a file you would follow the following procedure.
* <pre>
* 1. Create a SafeFile for the actual data file.
*
* SafeFile sf = new SafeFile(new File("\Program Files\MyApp\Config.dat"));
*
* 2. Request a temporary file to write new data to.
*
* File f = sf.getTempFile();
*
* 3. Copy or write the data to the temp file.
*
* 4. When the file is complete call setNewFile() to cause it to switch over
* to the temp file.
*
* sf.setNewFile(f);
*
* This does the following:
*
* - The temporary file is renamed to a ".new" file.
* - Any ".bak" (backup) file is deleted.
* - The actual data file is renamed to a ".bak" file.
* - The ".new" file is renamed to the actual data file.
* - If the SafeFile is created with keepBackup as false the ".bak" file is deleted.
*
* </pre>
* This sequence is such that if at any point in the procedure there should be a system
* crash, either the original file would be unchanged, or it can be recovered from the ".bak"
* (the old version) or ".new" (the new version).
* <p>
* Creating a SafeFile() for an actual file will prompt the system to ensure that the file
* exists. If it does not it will be recreated (if possible) from the ".bak" or ".new" file. Therefore
* when accessing that particular file, you should always use a SafeFile() to do so.
**/

//#####################################################################
public class SafeFile{
//#####################################################################

protected File file;
protected String fileName;
protected boolean keepBackup = true;
/**
 * Get the actual file.
 * @return The actual file.
 */
//===================================================================
public File getFile() {return file;}
//===================================================================

//-------------------------------------------------------------------
protected String	getFullPath()
//-------------------------------------------------------------------
{
	return fileName;
}
//-------------------------------------------------------------------
protected File getNewFile()
//-------------------------------------------------------------------
{
	return file.getNew(getNewFileName());
}
/**
 * Get a temporary file that will eventually be used to replace the actual file. This
	file is always the same name as the actual file with a ".tmp" added to it.
 * @return A new ".tmp" file.
 */
//==================================================================
public File	getTempFile()
//==================================================================
{
	return file.getNew(getFullPath()+".tmp");
}
/**
 * Safely rename the file so that it becomes the orignal file.
 * @param f The new file to be renamed.
 * @return true if successful.
 * @exception IOException
 */
//===================================================================
public boolean setNewFile(File f) throws IOException
//===================================================================
{
	return setNewFile(f,false);
}
/**
 * Safely rename the file so that it becomes the orignal file.
 * @param f The new file to be renamed.
 * @param updateTime true if the new file should have its time updated to the current time.
 * @return true if successful.
 * @exception IOException
 */
//==================================================================
public boolean setNewFile(File f,boolean updateTime) throws IOException
//==================================================================
{
	File f2 = getNewFile();
	if (f2.exists()) f2.delete();
	if (!f.move(f2)) return false;
	boolean b = swapToNew();
	if (!b) return b;
	if (updateTime) file.setModified(new ewe.sys.Time());
	return true;
}
//-------------------------------------------------------------------
protected String getNewFileName()
//-------------------------------------------------------------------
{
	return getFullPath()+".new";
}
//-------------------------------------------------------------------
protected File getBackupFile()
//-------------------------------------------------------------------
{
	return file.getNew(getBackupFileName());
}

//-------------------------------------------------------------------
protected String getBackupFileName()
//-------------------------------------------------------------------
{
	return getFullPath()+".bak";
}
/**
* NOT IMPLEMENTED YET. This will overwrite the old backup file (if it exists)
* with the current file. It leaves the current file as it is.
*/
//==================================================================
public boolean backupCurrent() throws IOException
//==================================================================
{
	return true;
}
/**
* This will do the following:
* <nl>
* <li> Remove the old backup file (if it exists).
* <li> Rename the current file to be the backup (".bak") file.
* <li> Rename the new (".new") file to be the current file.
* <li> If keepBackup is false, the backup will be deleted.
* </nl>
*/
//-------------------------------------------------------------------
protected boolean swapToNew() throws IOException
//-------------------------------------------------------------------
{
	File newFile = getNewFile(), backup = getBackupFile();
//..................................................................
// If the new file does not exist make sure the current file is there.
// If it is not there then recreate it from the backup.
//..................................................................
	if (!newFile.exists()) {
		if (file.exists()) return false; // No swapover necessary.
		if (backup.exists()) {
			try {
				if (!backup.move(file)) throw new Exception();
				file = file.getNew(getFullPath());
				return false;
			}catch(Exception e) {
				throw new IOException("Couldn't recover file from backup: "+backup.getFullPath());
			}
		}
		return false;
	}
//..................................................................
// New file does exist. Make it the current file now.
//..................................................................
	if (file.exists()) {
	//..................................................................
	// If the backup file exists, then remove it so that the current file
	// can be made the backup file.
	//..................................................................
		if (backup.exists()) {
				if (!backup.delete())
				throw new IOException("Couldn't delete backup file: "+backup.getFullPath());
		}
		//..................................................................
		// Now rename the current file to the backup file.
		//..................................................................
		try{
			if (!file.move(backup)) throw new Exception();
			file = file.getNew(getFullPath());
		}catch(Exception e){
			throw new IOException("Couldn't backup current file: "+file+"->"+backup);
		}
	}
	//..................................................................
	// Now make the new file the current file.
	//..................................................................
	try{
		if (!newFile.move(file)) throw new Exception();
		file = file.getNew(getFullPath());
	}catch(Exception e){
		throw new IOException("Couldn't make the new file into the current file: "+getFullPath());
	}
	if (!keepBackup && backup.exists())
		backup.delete();
	return true;
}

/**
 * Create a new SafeFile. The creation of the SafeFile will automatically cause
	 the system to check to see if the file exists and if it does not, but a ".bak" or ".new"
		does exist, then the ".bak" or ".new" will be renamed to the actual file.
 * @param file The actual file to be updated.
 * @param keepBackup specifies whether a backup of the previous version is to be kept.
 * @exception IOException if an exception occurs.
 */
//==================================================================
public SafeFile(File file,boolean keepBackup) throws IOException
//==================================================================
{
	try{
		this.keepBackup = keepBackup;
		fileName = file.getFullPath();
		this.file = file;
		swapToNew();
	}catch(IOException e){
		throw e;
	}catch(NullPointerException e2){
		throw e2;
	}catch(Throwable t){
		throw new IOException("Could not create safe file: "+t.toString());
	}
}
/**
 * Create a new SafeFile. The creation of the SafeFile will automatically cause
	 the system to check to see if the file exists and if it does not, but a ".bak" or ".new"
		does exist, then the ".bak" or ".new" will be renamed to the actual file.
 * @param file The actual file to be updated.
 * @exception IOException if an exception occurs.
 */
//==================================================================
public SafeFile(File file) throws IOException
//==================================================================
{
	this(file,true);
}
//#####################################################################
}
//#####################################################################

