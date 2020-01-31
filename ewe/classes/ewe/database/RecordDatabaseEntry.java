/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.database;
//##################################################################
public class RecordDatabaseEntry extends ByteArrayDatabaseEntry{
//##################################################################
int stored;

//-------------------------------------------------------------------
protected RecordDatabaseEntry(DatabaseObject obj)
//-------------------------------------------------------------------
{
	super(obj);
}

//===================================================================
public void pointTo(DatabaseEntry other) throws IllegalArgumentException
//===================================================================
{
	stored = (other == null) ? 0 : ((RecordDatabaseEntry)other).stored;
	isDeleted = false;
}
//===================================================================
public boolean isPointingTo(DatabaseEntry other) throws IllegalArgumentException
//===================================================================
{
	return stored != 0 && stored == ((RecordDatabaseEntry)other).stored;
}
//===================================================================
public void reset()
//===================================================================
{
	stored = 0;
	super.reset();
}
//===================================================================
public boolean isSaved()
//===================================================================
{
	return stored != 0 && !isADeletedEntry();
}
/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Math.random();
	time.setTime(now);
	ewe.sys.Math.srand(ewe.sys.Vm.getTimeStamp());
	DatabaseEntry db = new DatabaseEntry();
	names.data = new char[1000];
	db.clear();
	db.myData.data = new byte[4000];
	db.clear();
	RandomAccessDatabaseStream s = new RandomAccessDatabaseStream(new ewe.io.RandomAccessFile("records.dat","rw"),"rw");
	final RecordFile rf = new RecordFile();
	rf.setDatabaseStream(s);
	if (args.length != 0 && args[0].equals("-write")){
		int thousands = 1;
		if (args.length > 1){
			thousands = ewe.sys.Convert.toInt(args[1]);
			if (thousands == 0) thousands = 1;
		}
		rf.initialize();
		int num = ewe.sys.Vm.countObjects(false);
		for (int j = 0; j<thousands; j++){
			for (int i = 0; i<1000; i++){
				rf.addRecord(getRandomContact(true,db));
			}
			ewe.sys.Vm.debug("Did: "+(j+1)*1000);
		}
		int more = ewe.sys.Vm.countObjects(false)-num;
		//ewe.sys.Vm.debug("More: "+more);
	}
	final FoundEntries fe = rf.getEntries();
	final DatabaseTableModel dtm = new DatabaseTableModel(rf);
	final DatabaseEntry de = fe.getNew();
	dtm.setEntries(fe);
	dtm.setFields(de,OID_FIELD+"$J;25;r;;F;HexDisplay,1$,2$,3$I;5;;E,4$Lewe/sys/Time;30",null);
	ewe.ui.Form f = dtm.getTableForm(null);
	final ewe.ui.ProgressAndControl pbf = new ewe.ui.ProgressAndControl();
	f.addLast(pbf).setCell(f.HSTRETCH);
	pbf.controls.addNext(new ewe.ui.mButton("Clear deleted"){
		public void doAction(int how){
			try{
				rf.eraseDeletedEntries();
				ewe.sys.Vm.debug("Deleted erased.");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});
	pbf.controls.addNext(new ewe.ui.mButton("List deleted"){
		public void doAction(int how){
			try{
				ewe.util.IntArray del = rf.getDeletedEntries(null);
				Time when = new Time();
				when.format = "ddd MMM yyyy, HH:mm:ss.SSSS";
				ewe.sys.Vm.debug("Deleted -----------");
				for (int i = 0; i<del.length; i++){
					long oid = rf.getDeletedEntry(del.data[i],when);
					ewe.sys.Vm.debug(Convert.longToHexString(oid)+", "+when);
				}
				ewe.sys.Vm.debug("-------------------");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});
	pbf.controls.addNext(new ewe.ui.mButton("Delete it"){
		public void doAction(int how){
			try{
				fe.delete(0);
				dtm.entriesChanged();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	});
	pbf.controls.addNext(new ewe.ui.mButton("Sort by Name"){
		public void doAction(int how){
			new ewe.sys.TaskObject(){
				protected void doRun(){

				//	int[] criteria = new int[2];
				//	criteria[0] = de.toCriteria(1,STRING,0);
				//	criteria[1] = de.toCriteria(2,STRING,0);


			//int[] criteria = new int[1];
			//criteria[0] = de.toCriteria(4,DATE_TIME,0);

					int[] criteria = new int[1];
					criteria[0] = de.toCriteria(OID_FIELD,LONG,0);

					ewe.sys.Handle h = fe.sort(criteria,false);
					h.doing = "Sorting";
					pbf.startTask(h,null);
					try{
						//ewe.sys.Vm.debug("Sorting...");
						h.waitOn(h.Success);
						dtm.entriesChanged();
						//ewe.sys.Vm.debug("Done...");
					}catch(ewe.sys.HandleStoppedException e){
						//ewe.sys.Vm.debug("Failed...");
					}catch(InterruptedException e){
						//ewe.sys.Vm.debug("Interrupted");
					}
					pbf.endTask();
				}
			}.startTask();
		}
	});
	f.execute();
	//new ewesoft.apps.HexView(db.getDataForSaving()).execute();
	ewe.sys.Vm.exit(0);
}
*/
private RecordDatabaseEntry(){super(null);}
/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	ByteArrayDatabaseEntry de = new RecordDatabaseEntry();
	mImage mi = new mImage("ewe/ewebig.bmp");
	ByteArray ba = new ByteArray();
	mi.encodeBytes(ba);
	de.setFieldValue(1,BYTE_ARRAY,ba);
	de.decodeRecord();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################


