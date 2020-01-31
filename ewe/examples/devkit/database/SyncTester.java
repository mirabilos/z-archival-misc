package samples.database;
import ewe.database.Database;
import ewe.database.DatabaseTableModel;
import ewe.database.DoubleSynchronizer;
import ewe.database.EntriesView;
import ewe.database.RecordFile;
import ewe.database.Synchronizer;
import ewe.filechooser.FileChooser;
import ewe.io.File;
import ewe.io.IOException;
import ewe.ui.Editor;
import ewe.ui.Form;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mList;
import ewe.ui.mTabbedPanel;
import ewe.util.Vector;

//##################################################################
public class SyncTester extends Editor{
//##################################################################

mList deleted;
DatabaseTableModel dtm;
Database db;
public Vector delList = new Vector();
//===================================================================
public SyncTester(Database db,String name) throws IOException
//===================================================================
{
	this.db = db;
	title = "Sync Info: "+name;
	mTabbedPanel mt = new mTabbedPanel();
	mt.cardPanel.autoScroll = false;
	addLast(mt);
	Form f = new Form();
	int[] special = new int[]{Database.OID_FIELD};
	EntriesView ev = Synchronizer.getUnsynchronized(null,db);
	dtm = new DatabaseTableModel(db);//,special,true);
	dtm.setView(ev);
	dtm.getTableForm(f);
	mt.addItem(f,"Unsynchronized",null);
	long [] del = db.getDeletedEntries();
	for (int i = 0; i<del.length; i++){
		delList.add(new ewe.sys.Long().set(del[i]));
	}
	f = new Form();
	deleted = new mList(10,20,true);
	addField(deleted,"delList");
	f.addLast(new ScrollBarPanel(deleted));
	addField(f.addLast(new mButton("Erase")).setCell(HSTRETCH),"erase");
	mt.addItem(f,"Deleted",null);
	addField(addLast(new mButton("Synchronize With...")).setCell(HSTRETCH),"syncWith");
}
public void action(String field,Editor ed)
{
	if (field.equals("erase")){
		int idx = deleted.selectedIndex;
		if (idx != -1){
			ewe.sys.Long val = (ewe.sys.Long)delList.get(idx);
			try{
				db.eraseDeletedEntry(val.value);
				delList.del(idx);
				toControls("delList");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}else if (field.equals("syncWith")){
		try{
			FileChooser fc = new FileChooser();
			fc.title = "Open Database";
			fc.addMask("*.db - Database");
			if (fc.execute() == IDCANCEL) return;
			File toOpen = fc.getChosenFile();
			Database other = new RecordFile(toOpen,"rw");
			DoubleSynchronizer ds = new DoubleSynchronizer(db,other);
			//ds.dontSyncJustDebug = true;
			ds.synchronize("Synchronizing...");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
//##################################################################
}
//##################################################################
