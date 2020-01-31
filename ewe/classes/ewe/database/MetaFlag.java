package ewe.database;
import ewe.io.IOException;
/**
This represents a single integer value that can be stored in a Database using
its MetaData facilities. After creating it you can call getValue() and saveValue()
to read and write it to the database.
**/
//##################################################################
public class MetaFlag{
//##################################################################
private Object metaLocation;
private int currentValue;
private boolean isSaved;
private String name;
private Database db;
/**
Create a MetaFlag with the specified name for the specified database. Note that
if the db is open for read write this will automatically create and save the MetaData if
it does not already exist.
**/
//===================================================================
public MetaFlag(String name,Database db) throws IOException
//===================================================================
{
	this.name = name;
	this.db = db;
	int size = db.metaDataLength(name);
	isSaved = size != -1;
	if (!isSaved && !db.isOpenForReadWrite())
		metaLocation = null;
	else
		metaLocation = db.getMetaData(name,4,false);
	if (isSaved) currentValue = db.readMetaDataInt(metaLocation,0);
	else if (db.isOpenForReadWrite()) {
		setValue(0);
		isSaved = true;
	}
}
//===================================================================
public boolean isSaved()
//===================================================================
{
	return isSaved;
}
//===================================================================
public int getValue()
//===================================================================
{
	return currentValue;
}
//===================================================================
public boolean canWrite()
//===================================================================
{
	return db.isOpenForReadWrite();
}
/**
This sets the current value. It is kept locally in a field and will only
be written to the DB if it is different from what is stored on the DB.
**/
//===================================================================
public void setValue(int value) throws IOException
//===================================================================
{
	if (currentValue != value || !isSaved){
		if (db.isOpenForReadWrite()){
			db.writeMetaDataInt(metaLocation,0,value);
			isSaved = true;
		}
	}
	currentValue = value;
}
//##################################################################
}
//##################################################################

