package samples.database.example;
import ewe.database.*;
import ewe.sys.*;

//##################################################################
public class DatabaseApp{
//##################################################################
//
// Open and initialize if necessary a new database.
//
//===================================================================
public static Database openMyDatabase(String name) throws ewe.io.IOException
//===================================================================
{
	Database db = DatabaseManager.initializeDatabase(null,name,new TestData());
	if (db != null){
		//
		// Do your extra initialization here.
		//
		db.setSorts(new TestData(),"By Name$i|lastName,firstNames,gender|By Salary$i|salary,lastName,firstNames");
		db.save();
		db.close();
	}
	return DatabaseManager.openDatabase(null,name,"rw");
}
//
// Here we add entries.
//
//===================================================================
public static void addRandomEntries(Database db, int num) throws ewe.io.IOException
//===================================================================
{
	FoundEntries fe = db.getEmptyEntries();
	//
	// Use addData() to add data that is represented by a Java object.
	// The object must be the same as that used in openMyDatabase.
	//
	for (int i = 0; i<num; i++)
		fe.addData(DataRandomizer.getRandomData(2));
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

	Database db = openMyDatabase("TestData");
	//
	if (false && db.getEntriesCount() == 0) addRandomEntries(db,20);
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
	String searchFor = "*";
	if (args.length != 0) searchFor = args[0];
	//
	FoundEntries found = null;
	if (!searchFor.equals("$"))
		found = getForLastName(db,searchFor);
	else
		found = getSortedBySalary(db);

	Vm.debug(printEntries(found));
	//
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
