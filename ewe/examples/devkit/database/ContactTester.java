package samples.database;
import ewe.database.*;
import ewe.sys.*;
import ewe.io.*;
import ewe.util.*;
import ewe.sys.Time;
import ewe.math.BigDecimal;
import ewe.data.*;
import ewe.ui.*;

//##################################################################
public class ContactTester extends LiveObject{
//##################################################################

public Contact contact = new Contact();

//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int which)
//===================================================================
{
	Editor ce = new Editor();
	ce.setFields(contact);
	ce.setText("Contact");
	cp.addLast(ce);
}
static String allLastNames[] =
{
"Brereton","Mahabir","Charles","Wallace","Monsegue","Raymond","Granger",
"Rousea","Rampersad","Joseph","Khan","Hay","DeLima","Farfan","Che Ting",
"Bush","Clinton","Reagan","Gorbachev","Ali","Ghandi","Ramnarine","Chirac",
"Beck","Thomas","Daniels","Goldberg","Willis","Foster","Roberts","Jolie",
};

public static String allFirstNames[] =
{
"Michael","Asha","Peter","Jean-Anne","Valerie","Norma","Pat","Ken",
"Jill","Margaret","Abraham","Karen","Krystal","Jennifer","George","Rita",
"James","Louis","Lia","Raymon","Antonio",
"Vincent","Damian","Anne","Mary","Sylvia",
"Scott","Deborah","Samuel","Che","Andre",
"Jeff","Angelina","Jeff","Whoopie","Bruce","Jodie","Julia"
};

public static String allCompanies[] =
{
	"Ewesoft","Virgin","Oracle","Microsoft","PeopleSoft","CA",
	"Sun","Red Hat","Sony-Ericsson","Sharp","Casio","Compaq","Dell","Gateway","Toshiba",
	"T&TEC","TSTT","WASA","NIB","British Airways"
};
static int rand()
{
	return (int)(java.lang.Math.random()*0xffff);
}
//===================================================================
public static String getRandomString(String[] strings)
//===================================================================
{
	return strings[rand()%strings.length];
}
static CharArray names = new CharArray();
static String space = " ";
//===================================================================
public static CharArray getRandomFirstName(int size)
//===================================================================
{
	names.length  = 0;
	for (int i = 0; i<size; i++){
		if (i != 0) names.append(space);
		names.append(allFirstNames[rand()%allFirstNames.length]);
	}
	return names;
}

static int oneDay = 24*60*60*1000;
static Time time = new Time();
static long now = time.getTime();

public static Contact getRandomContact(boolean longNames, Contact dest)
{
	if (dest == null) dest = new Contact();
	dest.lastName = getRandomString(allLastNames);
	dest.firstNames = getRandomFirstName(longNames ? 3 : 1).toString();
	long newTime = now+(long)((java.lang.Math.random()-0.5)*10000L*oneDay);
	dest.dob.setTime(newTime);
	dest.male = java.lang.Math.random() < 0.5;
	dest.age = 1+(rand()%100);
	dest.company = getRandomString(allCompanies);
	dest.salary = new Decimal(new BigDecimal(java.lang.Math.random()*20000.0).setScale(2,BigDecimal.ROUND_UP));
	try{
		//dest.icon = new ewe.fx.mImage("samples/database/Icon"+((rand()%6)+1)+".png");
	}catch(Exception e){}
	return dest;
}
//===================================================================
public static Handle addRandomContacts(final int num, final DatabaseEntry de)
//===================================================================
{
	TaskObject to = new ewe.sys.TaskObject(){
		protected void doRun(){
			try{
				Contact c = new Contact();
				handle.resetTime("Randomizing");
				for (int i = 0; i<num && !handle.shouldStop; i++){
					de.reset();
					de.setData(getRandomContact(true,c));
					de.save();
					handle.setProgress((float)(i+1)/(float)num);
				}
				handle.set(Handle.Succeeded);
			}catch(Exception e){
				e.printStackTrace();
				handle.fail(e);
			}
		}
	};
	return to.startTask();
	/*
	to.exec("Randomizing",null);
	try{
		to.getHandle().waitUntilStopped();
	}catch(InterruptedException e){}
	*/
}
//-------------------------------------------------------------------
static void dump(Database db) throws IOException
//-------------------------------------------------------------------
{
	Vm.debug("Database: -------------");
	for (Iterator it = db.entries(); it.hasNext();){
		Contact c = (Contact)((DatabaseEntry)it.next()).getData();
		Vm.debug(c.toString());
	}
	Vm.debug("-------------");
}
//===================================================================
static void dump(FoundEntries entries) throws IOException
//===================================================================
{
	Vm.debug("Entries: -------------");
	for (int i = 0; i<entries.size(); i++){
		Contact c = (Contact)entries.getData(i);
		Vm.debug(c.toString());
	}
	Vm.debug("-------------");
}
//===================================================================
static void dump(EntriesView entries) throws IOException
//===================================================================
{
	Vm.debug("View: -------------");
	for (int i = 0; i<entries.size(); i++){
		Contact c = (Contact)entries.getData(i,null);
		Vm.debug(c.toString());
	}
	Vm.debug("-------------");
}
//===================================================================
public static Handle showProgress(String doing,Handle h)
//===================================================================
{
	ProgressBarForm pbf = new ProgressBarForm();
	pbf.bar.setPreferredSize(300,-1);
	pbf.horizontalLayout = false;
	pbf.showStop =
	pbf.showSubTask =
	pbf.showTaskInBar =
	pbf.showTimeLeft = true;
	pbf.execute(h,doing);
	return h;
}
//===================================================================
public static void runTest(String[] args) throws IOException
//===================================================================
{
	Database db;
	String dname = "Contacts";
	if (!DatabaseManager.databaseExists(null,dname)){
		db = DatabaseManager.initializeDatabase(null,dname,new Contact());
		if (db != null){
			db.modifyField(db.findField("contactID"),db.FIELD_MODIFIER_INTEGER_AUTO_INCREMENT,
				new ewe.sys.Long().set(100));
			db.indexBy(db.findSort("By Name"));
			//db.setPassword("ContactTester");
			db.enableSynchronization(0);
			db.close();
		}
	}
	db = DatabaseManager.openDatabase(null,dname,"rw");
	Form f = new DatabaseTester(db);
	f.show();
	f.waitUntilClosed();
	db.close();
}
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	runTest(args);
	ewe.sys.Vm.exit(0);
}



//##################################################################
}
//##################################################################
