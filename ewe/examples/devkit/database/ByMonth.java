package samples.database;
import ewe.database.*;
import ewe.sys.Time;

//##################################################################
public class ByMonth extends EntryComparer{
//##################################################################

int dobID, dobType;
Time onetime = new Time(), twotime = new Time();

//===================================================================
public ByMonth()
//===================================================================
{
	compareAsDatabaseEntries = true;
}

//===================================================================
public void setDatabase(Database db)
//===================================================================
{
	super.setDatabase(db);
	dobID = db.findField("dob");
	dobType = db.getFieldType(dobID);
}
//===================================================================
public int compareEntries(Object one,Object two)
//===================================================================
{
	if (dobID == 0) return 0;
	DatabaseEntry o = (DatabaseEntry)one;
	DatabaseEntry t = (DatabaseEntry)two;
	Time t1 = (Time)o.getFieldValue(dobID,dobType,onetime);
	Time t2 = (Time)t.getFieldValue(dobID,dobType,twotime);
	//ewe.sys.Vm.debug("Comparing: "+t1+"<>"+t2);
	if (t1 == t2) return 0;
	if (t1 == null) return -1;
	else if (t2 == null) return 1;
	else {
		int d = t1.month-t2.month;
		if (d != 0) return d;
		d = t1.day-t2.day;
		if (d != 0) return d;
		return t1.year-t2.year;
	}
}
//##################################################################
}
//##################################################################
