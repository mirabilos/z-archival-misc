package samples.database.example2;
import ewe.database.*;
import ewe.sys.*;
import ewe.io.IOException;
import ewe.io.File;
/**
This example uses a Record object to read and write data into the database.
**/
//##################################################################
public class DatabaseApp{
//##################################################################

//-------------------------------------------------------------------
static void initializeDatabase(Database db) throws IOException
//-------------------------------------------------------------------
{
	int ln = db.addField("lastName",db.STRING);
	int fn = db.addField("firstNames",db.STRING);
	db.addField("salary",db.DECIMAL);
	int children = db.addField("children",db.INTEGER);
	int dob = db.addField("dob",db.DATE);
	db.addSpecialField(db.OID_FIELD);
	int byLastName = db.addSort("By Last Name",db.SORT_IGNORE_CASE,ln,fn,dob,0);
	int byChildren = db.addSort("By Children",0,children);
	db.indexBy(null,byLastName,"By Last Name");
	db.indexBy(null,byChildren,"By Children");
	//db.setSorts(new TestData(),"By Name$i|lastName,firstNames,gender|By Salary$i|salary,lastName,firstNames");
	db.save();
	db.close();
}

//===================================================================
public static DatabaseEntry getRandomData(DatabaseEntry de)
//===================================================================
{
	Database db = de.getDatabase();
	TestData td = DataRandomizer.getRandomData(2);
	de.reset();
	de.setField(db.findField("lastName"),td.lastName);
	de.setField(db.findField("firstNames"),td.firstNames);
	de.setField(db.findField("salary"),td.salary);
	de.setField(db.findField("children"),td.retirementAge);
	de.setField(db.findField("dob"),new DayOfYear());
	return de;
}
//
// Here we add entries.
//
//===================================================================
public static void addRandomEntries(Database db, int num) throws ewe.io.IOException
//===================================================================
{
	DatabaseEntry de = db.getNewData();
	//
	// Use addData() to add data that is represented by a Java object.
	// The object must be the same as that used in openMyDatabase.
	//
	for (int i = 0; i<num; i++)
		db.append(getRandomData(de));
	db.reIndex(null);
}

//===================================================================
public static FoundEntries getForLastName(Database db,String lastNameMask)
throws ewe.io.IOException
//===================================================================
{
	return db.getFoundEntries(null,db.findSort("By Name"),lastNameMask);
}
//===================================================================
public static FoundEntries getSortedBySalary(Database db)
throws ewe.io.IOException
//===================================================================
{
	return db.getFoundEntries(null,db.findSort("By Salary"));
}
//===================================================================
public static String printEntries(FoundEntries fe)
throws ewe.io.IOException
//===================================================================
{
	String ret = "";
	for (int i = 0; i<fe.size(); i++){
		if (i != 0) ret += "\n";
		TestData data = (TestData)fe.getData(i);
		ret += printEntry(data);
	}
	return ret;
}

//===================================================================
public static String printEntry(TestData data)
//===================================================================
{
	String ret = "";
	ret += data.lastName+", "+data.firstNames;
	ret += "; "+data.gender+"; $"+data.salary;
	return ret;
}

//=================================================================
public static void main(String[] args)
throws ewe.io.IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);

	File f = new File("TestData2.db");
	Database db;
	if (!f.exists()){
		db = new RecordFile(f,"rw");
		initializeDatabase(db);
	}
	if (args.length != 0 && args[0].equalsIgnoreCase("append")){
		ewe.sys.Vm.debug("Opening for Append");
		db = new RecordFile(f,"a");
		addRandomEntries(db,20);
		db.close();
		ewe.sys.Vm.debug("Done.");
		ewe.sys.Vm.exit(0);
	}
	ewe.sys.Vm.debug("Opening for RW");
	db = new RecordFile(f,"rw");
	//
	//if (false && db.getEntriesCount() == 0) addRandomEntries(db,20);
	//
	Vm.debug("Number of entries: "+db.getEntriesCount());
	//
	// Now we can do a search.
	//
	// If the first argument arg[0] is a '$' then all entries
	// sorted by salary will be displayed.
	//
	// Otherwise the first argument is considered to be a mask for
	// searching for the last name.
	//
	/*
	String searchFor = "*";
	if (args.length != 0) searchFor = args[0];
	//
	FoundEntries found = null;
	if (!searchFor.equals("$"))
		found = getForLastName(db,searchFor);
	else
		found = getSortedBySalary(db);

	Vm.debug(printEntries(found));
	*/
	//
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
