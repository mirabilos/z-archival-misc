package samples.database;
import ewe.database.Database;
import ewe.database.DatabaseTableModel;
import ewe.database.EntriesView;
import ewe.database.EntrySelector;
import ewe.database.FoundEntries;
import ewe.database.GetSearchCriteria;
import ewe.io.IOException;
import ewe.sys.Handle;
import ewe.ui.ButtonBar;
import ewe.ui.Editor;
import ewe.ui.Form;
import ewe.ui.ProgressAndControl;
import ewe.ui.ReportException;
import ewe.ui.mCheckBox;
import ewe.ui.mInput;
import ewe.ui.mTabbedPanel;

//##################################################################
public class FoundEntriesTester extends Editor{
//##################################################################

FoundEntries fe;
EntriesView view;
public String message = "";
//===================================================================
public FoundEntriesTester(FoundEntries fe)
//===================================================================
{
	this.fe = fe;
	int[] special = new int[]{Database.OID_FIELD};

 	view = fe.getEmptyView();
	mTabbedPanel mt = new mTabbedPanel();
	mt.cardPanel.autoScroll = false;
	addLast(mt);
	Form f = new Form();
	DatabaseTableModel dtm = new DatabaseTableModel(fe.getDatabase());//,special,true);
	dtm.setEntries(fe);
	mt.addItem(getTestForm(dtm,false),"FoundEntries",null);

	f = new Form();
	dtm = new DatabaseTableModel(fe.getDatabase());//,special,true);
	dtm.setView(view);
	mt.addItem(getTestForm(dtm,true),"EntriesView",null);
	mInput mi = new mInput();
	mi.modify(DisplayOnly,0);
	addField(addLast(mi).setCell(HSTRETCH),"message");
}

//===================================================================
public Handle doSearch(final EntrySelector sel,final DatabaseTableModel dtm,final ProgressAndControl pc, final boolean isFilter)
throws IOException
//===================================================================
{
	if (isFilter)
		return new ewe.sys.TaskObject(){
			protected void doRun(){
				EntriesView ev = view;
				long now = System.currentTimeMillis();
				Handle h = ev.search(sel);
				pc.startTask(h,"Searching");
				if (waitOnSuccess(h,false)){
					now = System.currentTimeMillis()-now;
					message = "Search time: "+now+" ms.";
					toControls("message");
					dtm.entriesChanged();
				}
				pc.endTask();
			}
		}.startTask();
	else{
			long now = System.currentTimeMillis();
			view.search(null,sel);
			now = System.currentTimeMillis()-now;
			message = "Search time: "+now+" ms.";
			toControls("message");
			dtm.entriesChanged();
			return new Handle(Handle.Succeeded,null);
	}
}
//===================================================================
public Form getTestForm(final DatabaseTableModel dtm,final boolean forView)
//===================================================================
{
	Editor ed = new Editor(){
		mCheckBox ai;
		ProgressAndControl pc;
		{
			dtm.getTableForm(this,true);
			pc = new ProgressAndControl();
			addLast(pc).setCell(HSTRETCH);
			ButtonBar bb = new ButtonBar();
			addField(bb.add("Refresh"),"refresh");
			if (forView) {
				addField(bb.add("Search"),"search");
				addField(bb.add("Filter"),"filter");
				addField(bb.add("Clear"),"clear");
				bb.endRow();
				addField(bb.add("ReSort"),"sort");
				addField(bb.add("Exclude"),"exclude");
				addField(bb.add("LookupMode"),"lookupMode");
				/*
				addField(bb.addNext(ai = new mCheckBox("All Inclusive")),"allInclusive");
				*/
			}else{
				addField(bb.add("Delete"),"deleteFromFound");
			}
			addField(bb.add("New"),"new");
			pc.controls.addLast(bb);
			pc.pbf.bar.showPercent = true;
			pc.pbf.showTimeLeft = true;
		}

		public void fieldChanged(String fieldName,Editor ed)
		{
			if (fieldName.equals("allInclusive")){
				view.allInclusive = ai.getState();
			}
		}
		public void action(String fieldName,Editor ed){
		try{
			if (fieldName.equals("clear")){
				view.clear();
				dtm.entriesChanged();
			}else if (fieldName.equals("lookupMode")){
				view.enableLookupMode();
			}else if (fieldName.equals("refresh")){
				dtm.entriesChanged();
			}else if (fieldName.equals("filter")){
				final EntrySelector es = new GetSearchCriteria(fe.getDatabase()).input("Filter Criteria");
				if (es == null) return;
				doSearch(es,dtm,pc,true);
			}else if (fieldName.equals("deleteFromFound")){
				int row = dtm.getSelectedEntry();
				fe.delete(row);
				dtm.entriesChanged();
			}else if (fieldName.equals("new")){
				dtm.addNew();
			}else if (fieldName.equals("sort")){
				view.sort(false);
				dtm.entriesChanged();
			}else if (fieldName.equals("exclude")){
				int row = dtm.getSelectedEntry();
				if (row == -1) return;
				view.exclude(row);
				dtm.entriesChanged();
			}else if (fieldName.equals("search")){
				if (fe.getSortCriteria() == null) {
					action("filter",ed);
					return;
				}
				final EntrySelector es = new GetSearchCriteria(fe).input("Search Criteria");
				if (es == null) return;
				doSearch(es,dtm,pc,false);
			}
			}catch(Exception e){
				new ReportException(e,null,null,false).execute();
			}
		}
	}
	;
	return ed;
}

//##################################################################
}
//##################################################################
