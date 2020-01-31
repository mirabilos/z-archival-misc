package samples.database;
import ewe.database.Database;
import ewe.database.DatabaseEntry;
import ewe.database.DatabaseMaker;
import ewe.database.DatabaseManager;
import ewe.database.FoundEntries;
import ewe.database.IndexEntry;
import ewe.filechooser.FileChooser;
import ewe.io.File;
import ewe.io.IOException;
import ewe.io.ReadOnlyException;
import ewe.reflect.Method;
import ewe.reflect.Modifier;
import ewe.reflect.Reflect;
import ewe.reflect.Wrapper;
import ewe.sys.Handle;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.Editor;
import ewe.ui.Form;
import ewe.ui.InputBox;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.ui.ReportException;
import ewe.ui.mButton;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.util.Vector;

//##################################################################
public class DatabaseTester extends Editor{
//##################################################################
Database db;
public String sortBy, indexBy, customIndex = "samples.database.ByMonth";
mChoice indexedBy, sorts;
String myName;
public String password = "ContactTester";
Control passwordButton;

String[] getIndexes()
{
	IndexEntry[] idx = db.getIndexes();
	String[] ret = new String[idx.length];
	for (int i = 0; i<idx.length; i++)
		ret[i] = idx[i].name;
	return ret;
}
String[] getSorts()
{
	int[] all = db.getSorts();
	Vector v = new Vector();
	for (int i = 0; i<all.length; i++){
		String name = db.getSortName(all[i]);
		if (name.startsWith("_")) continue;
		v.add(name);
		if (sortBy == null) sortBy = name;
	}
	for (int i = 0; i<all.length; i++){
		String name = db.getSortName(all[i]);
		if (!name.startsWith("_")) continue;
		v.add(name);
		if (sortBy == null) sortBy = name;
	}
	v.add("<unsorted>");
	String [] sorts = new String[v.size()];
	v.copyInto(sorts);
	return sorts;
}
String[] getIndexesAndSorts()
{
	Vector v = new Vector(getIndexes());
	String [] sorts = getSorts();
	for (int i = 0; i<sorts.length; i++)
		if (!v.contains(sorts[i])) v.add(sorts[i]);
	String [] indexesAndSorts = new String[v.size()];
	v.copyInto(indexesAndSorts);
	return indexesAndSorts;
}

//===================================================================
public DatabaseTester(Database db)
//===================================================================
{
	myName = db.getProperties().getString("baseName","Unnamed Database");
	title = "Testing: "+myName;
	this.db = db;
	if (sortBy == null) sortBy = "<unsorted>";
	indexBy = sortBy;
	CellPanel cp = new CellPanel();
	cp = new CellPanel();
	cp.setText("Password");
	passwordButton = cp;
	addField(cp.addNext(new mButton("Use:")),"usePassword").setCell(DONTSTRETCH);
	mInput mi = new mInput(); mi.isPassword = true;
	addField(cp.addLast(mi),"password");
	addLast(cp).setCell(HSTRETCH);
	cp = new CellPanel();
	cp.setText("New FoundEntries");
	addField(cp.addNext(new mButton("Create")),"create").setCell(DONTSTRETCH);
	addField(cp.addNext(sorts = new mChoice(getIndexesAndSorts(),0)),"sortBy");
	addLast(cp).setCell(HSTRETCH);
	cp = new CellPanel();
	cp.setText("Add Index");
	cp.addNext(new mLabel("Already Indexed By:")).setCell(DONTSTRETCH);
	cp.addNext(indexedBy = new mChoice(getIndexes(),0));
	cp.endRow();
	addField(cp.addNext(new mButton("Add")),"addIndex").setCell(DONTSTRETCH);
	addField(cp.addLast(new mChoice(getSorts(),0)),"indexBy");
	addField(cp.addNext(new mButton("Add Custom")),"addCustom").setCell(DONTSTRETCH);
	addField(cp.addLast(new mInput()),"customIndex");
	addLast(cp).setCell(HSTRETCH);
	cp = new CellPanel();
	cp.setText("Synchronization");
	addField(cp.addNext(new mButton("Get Unsynchronized and Deleted")),"getunsync").setCell(HSTRETCH);
	addLast(cp).setCell(HSTRETCH);

	cp = new CellPanel();
	cp.setText("Other");
	addField(cp.addNext(new mButton("Add Random Entries")),"randomize").setCell(HSTRETCH);
	addLast(cp).setCell(HSTRETCH);
}
//-------------------------------------------------------------------
void updateIndexedBy()
//-------------------------------------------------------------------
{
	indexedBy.set(getIndexes(),0);
	sorts.set(getIndexesAndSorts(),0);
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("usePassword")){
		String p = password;
		if (p.trim().length() == 0) p = null;
		try{
			if (!db.usePassword(p)){
				new MessageBox("Not a valid password","That password is not valid.",MBOK).execute();
			}else{
				passwordButton.modify(Control.Disabled,0);
				passwordButton.repaintNow();
			}
		}catch(Exception e){
			new ReportException(e,null,null,false).execute();
		}
	}else if (fieldName.equals("addCustom")){
		final Reflect r = Reflect.loadForName(customIndex);
		if (r == null) new MessageBox("Not a valid Class","That class name is not valid.",MBOK).execute();
		Handle h = ProgressBarForm.execute("Adding Index",
			new ewe.sys.TaskObject(){
				protected void doRun(){
					try{
						if (!db.indexBy(handle,r.getReflectedClass(),null))
							handle.set(handle.Aborted);
						else{
							db.save();
							updateIndexedBy();
							handle.set(handle.Succeeded);
						}
					}catch(Exception e){
						handle.failed(e);
					}
				}
			}.startTask());

	}
	else if (fieldName.equals("randomize")){
		final Object data = db.getNewDataObject();
		Reflect r = Reflect.getForObject(data);
		final Method md = r.getMethod("randomize","()V",Method.PUBLIC);
		if (md == null || Modifier.isStatic(md.getModifiers())) {
			new MessageBox("No randomize() method","There is no randomize() method\nfor the data object.",MBOK).execute();
			return;
		}
		String toRandomize = new InputBox("Number of entries").input("100",20);
		if (toRandomize != null){
			final int num = ewe.sys.Convert.parseInt(toRandomize);
			if (num > 0){
				Handle h = new ewe.sys.TaskObject(){
					protected void doRun(){
						try{
							Wrapper [] w = new Wrapper[0];
							handle.resetTime("Randomizing");
							DatabaseEntry de = db.getNewData();
							for (int i = 0; i<num && !handle.shouldStop; i++){
								md.invoke(data,w,new Wrapper());
								de.reset();
								de.setData(data);
								de.save();
								handle.setProgress((float)(i+1)/(float)num);
							}
							handle.set(Handle.Succeeded);
						}catch(Exception e){
							new ReportException(e,null,null,false).execute();
							handle.fail(e);
						}
					}
				}.startTask();
				ProgressBarForm.execute("Randomizing Entries",h);
			}
		}
	}else if (fieldName.equals("create")){
		int sortId = db.findSort(sortBy);
		Handle h =
			ProgressBarForm.execute("Getting Found Entries",
			sortId != 0 ? db.getFoundEntries(sortId) :
				(!sortBy.equals("<unsorted>") ?
					db.getFoundEntries(sortBy) : db.getFoundEntries(0)));
		FoundEntries fe = (FoundEntries)h.returnValue;
		if (fe == null) return;
		Form f = new FoundEntriesTester(fe);
		f.title = "Sort: "+sortBy;
		f.show();
	}else if (fieldName.equals("getunsync")){
		try{
			new SyncTester(db,myName).show();
		}catch(IOException e){

		}
	}else if (fieldName.equals("addIndex")){
		final int sortId = db.findSort(indexBy);
		if (sortId == 0) return;
		Handle h = ProgressBarForm.execute("Adding Index",
			new ewe.sys.TaskObject(){
				protected void doRun(){
					try{
						if (!db.indexBy(handle,sortId,null))
							handle.set(handle.Aborted);
						else{
							db.save();
							updateIndexedBy();
							handle.set(handle.Succeeded);
						}
					}catch(Exception e){
						handle.failed(e);
					}
				}
			}.startTask());
	}
}
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	File toOpen = null;
	boolean readOnlyMode = false;
	if (args.length > 0){
		if (args[0].equalsIgnoreCase("/r") || args[0].equalsIgnoreCase("-r")){
			readOnlyMode = true;
			if (args.length > 1)
 				toOpen = File.getNewFile(args[1]);
		}else{
			toOpen = File.getNewFile(args[0]);
		}
	}
	if (toOpen == null){
		FileChooser fc = new FileChooser();
		fc.title = "Open Database";
		fc.addMask("*.db,*.cdb - Database, Compressed Database");
		if (fc.execute() == IDCANCEL) ewe.sys.Vm.exit(0);
		toOpen = fc.getChosenFile();
	}
	Database db = null;
	DatabaseMaker maker = DatabaseManager.getDefaultDatabaseMaker();
	ewe.sys.Vm.debug("ReadOnly: "+readOnlyMode);
	try{
		db = maker.openDatabase(toOpen,readOnlyMode ? "r" : "rw");
	}catch(ReadOnlyException e){
		db = maker.openDatabase(toOpen,"r");
	}
	DatabaseTester dt = new DatabaseTester(db);
	dt.exitSystemOnClose = true;
	dt.show();
	dt.waitUntilClosed();
	db.close();
	ewe.sys.Vm.exit(0);
}


//##################################################################
}
//##################################################################
