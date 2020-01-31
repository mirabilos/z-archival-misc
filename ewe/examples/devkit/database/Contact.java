package samples.database;
import ewe.database.DataValidator;
import ewe.database.Database;
import ewe.database.DatabaseEntry;
import ewe.database.InvalidDataException;
import ewe.io.IOException;
//import ewe.util.ByteArray;

//##################################################################
public class Contact implements ewe.util.Encodable, DataValidator{//extends LiveObject implements HasProperties{
//##################################################################

static final boolean doAddTest = true;
static final boolean doAppend = true;
public int contactID = 0;
public String lastName = "Branson";
public String firstNames = "Richard";
public String company = "Virgin";
public int age = 55;
public ewe.sys.DayOfYear dob = new ewe.sys.DayOfYear();
public ewe.sys.Decimal salary = new ewe.sys.Decimal(12345.50);
public String title = "Mr.";
public boolean male = true;
//public ewe.fx.ImageBytes picture = new ewe.fx.ImageBytes(ewe.sys.Vm.readResource(null,"BasdeoPanday.jpg"));
//public mImage icon = new mImage("samples/database/Icon6.png");

//
// This field is retrieved from the database, but never sent to the database.
//
//public long _OID;

public String toString() {return getName();}
public String getName() {return lastName+", "+firstNames+" of "+company+", born: "+dob+" and is "+(male ? "male." : "female.");}

public String _sorts =
"By Name$i|lastName,firstNames|By Age$i|age,lastName,firstNames|By Company$i|company,lastName,firstNames|By DOB$i|dob,lastName,firstNames|By Salary$i|salary,lastName,firstNames";
public String _formats =
"salary;10;;E,age;5;;E,dob;30,firstNames;30,title;5,male;5,icon;5:2";

//===================================================================
public void randomize()
//===================================================================
{
	ContactTester.getRandomContact(true,this);
}

//===================================================================
public void validateEntry(Database db,DatabaseEntry newData,DatabaseEntry oldData)
throws IOException
//===================================================================
{
	if (newData == null){//Must be deleting.
		if (oldData.getField(db.findField("lastName"),"").equalsIgnoreCase("Brereton"))
			throw new InvalidDataException("Cannot delete a member of the Brereton family!");
	}
}

//##################################################################
}
//##################################################################
