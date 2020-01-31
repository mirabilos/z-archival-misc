/*
Copyright (c) 2001 Michael L Brereton  All rights reserved.

This software is furnished under the Gnu General Public License, Version 2, June 1991,
and may be used only in accordance with the terms of that license. This source code
must be distributed with a copy of this license. This software and documentation,
and its copyrights are owned by Michael L Brereton and are protected by copyright law.

If this notice is followed by a Wabasoft Copyright notice, then this software
is a modified version of the original as provided by Wabasoft. Wabasoft also
retains all rights as stipulated in the Gnu General Public License. These modifications
were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May
2001.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.
*/

package ewe.data;
import ewe.util.*;
import ewe.ui.*;
import ewe.reflect.*;
import ewe.fx.*;
import ewe.sys.*;

/**
* This is used by a FieldTableModel to export table data to the clipboard or to a file. It
* als provides an Editor which the user can use to specify particular details about the
* export. You can execute  <b>ewe ewe.data.FieldTableExportSpecs</b> to see what it looks like.
**/
//##################################################################
public class FieldTableExportSpecs extends LiveObject{
//##################################################################

public static String lastExport = "TableData.txt";

public boolean columnHeaders = true;
public boolean rowHeaders = true;
public boolean reorderRows = true;
public boolean selectedOnly = false;
public boolean entireTable = true;

public boolean toClipboard = true;
public boolean toFile = false;
public boolean useTab = true;
public boolean useSpace = false;
public boolean useOther = false;
public String fieldSeparator = "";
public String fileName = "myFile";
public ewe.io.File outputFile =
	ewe.sys.Vm.newFileObject().getNew(lastExport);
public MultiListSelect.SingleListSelect columns;
public boolean appendFile = false;

FieldTableModel model;
//===================================================================
public FieldTableExportSpecs() {this(new FieldTableModel());}
//===================================================================

//===================================================================
public FieldTableExportSpecs(FieldTableModel model)
//===================================================================
{
	this.model = model;
}

//===================================================================
public void addToPanel(CellPanel c,Editor f,int which)
//===================================================================
{
	f.title = "Export Table Data";
	boolean doWide = !Gui.screenIs(Gui.LONG_SCREEN) && Gui.screenIs(Gui.WIDE_SCREEN);
	mTabbedPanel tp = new mTabbedPanel(); tp.cardPanel.autoScroll = false;
	c.addLast(tp);
 	CellPanel cp = new CellPanel(); cp.borderWidth = 2; cp.borderStyle = cp.BDR_NOBORDER;// mInput.inputEdge;
	CellPanel cp2 = new CellPanel();
	cp.defaultTags.set(cp.INSETS,new ewe.fx.Insets(1,2,1,2));
	InputStack is;
	is = new InputStack(); is.setText("Export"); is.columns = 2;
	is.addChecks(f,"Entire Table|entireTable|Selected Rows|selectedOnly",true,true);
	cp2.addLast(is).setCell(cp.HSTRETCH);
	is = new InputStack(); is.setText("Field Separator"); is.columns = 2;
	is.addChecks(f,"Tabs|useTab|Spaces|useSpace|Other|useOther",true,true);
	is.add(f.addField(new mInput(),"fieldSeparator"),null);
	cp2.addLast(is).setCell(cp.HSTRETCH);
	cp.addNext(cp2).setCell(cp.HSTRETCH);
	if (!doWide) cp.endRow();
	cp2 = new CellPanel();
	is = new InputStack(); is.setText("Destination");
	is.addChecks(f,"Export to clipboard|toClipboard|Export to file|toFile",true,true);
	InputStack is2 = new InputStack(); is2.borderStyle = is2.BDR_NOBORDER;
	mFileInput mf = new mFileInput();
	mf.title = "Select Export Output File";
	mf.defaultExtension = "txt";
	mf.masks.add("*.txt - Text files");
	mf.masks.add("*.* - All files");
	mf.fileChooserOptions = ewe.filechooser.FileChooser.SAVE;
	mf.select.setToolTip("Select Export Output File");
	is2.add(f.addField(mf,"outputFile"),"File:");
	is.add(is2,null);
	cp2.addLast(is).setCell(cp.HSTRETCH);
	cp.addLast(cp2);
	tp.addItem(cp,"Export",null);
	if (model.allColumns != null){
		cp = new CellPanel();
		cp.addNext(model.getColumnSelector(columns = (MultiListSelect.SingleListSelect)model.allColumns.getCopy()));
		if (!doWide) cp.endRow();
		is = new InputStack(); is.borderWidth = 3; is.borderStyle = is.EDGE_ETCHED; is.setText("Export");
		is.addChecks(f,"Column Headers|columnHeaders|Row Headers|rowHeaders|Re-order Rows|reorderRows",false,true);
		cp.addLast(is).setCell(cp.VSTRETCH);
		model.allColumns.setSelected((Vector)model.displayFields.getCopy());
		tp.addItem(cp,"Data",null);
	}
	ProgressAndControl pc = new ProgressAndControl();
	ButtonBar bb = new ButtonBar(); bb.borderWidth = 3; bb.borderStyle = bb.EDGE_ETCHED;
	bb.addNext(f.addField(new mButton("Export").setHotKey(0,'x'),"export"));
	bb.addNext(f.cancel = new mButton("Cancel").setHotKey(0,'c'));
	pc.controls.addLast(bb);
	c.addLast(pc).setCell(cp.HSTRETCH);
	f.getProperties().set("progress",pc);
	//f.resizable = true;

}

//===================================================================
public void fieldEvent(ewe.reflect.FieldTransfer ft,Editor ed,Object event)
//===================================================================
{
	if (ft.fieldName.equals("_editor_") && (event instanceof EditorEvent)){
		EditorEvent ede = (EditorEvent)event;
		if (ede.affects("toFile")) ed.modifyFields("outputFile",!toFile,ed.Disabled,0,true);
		if (ede.affects("rowHeaders")) ed.modifyFields("reorderRows",!rowHeaders,ed.Disabled,0,true);
	}
}
//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("useTab") || fieldName.equals("useSpace")){
		fieldSeparator = "";
		ed.toControls("fieldSeparator");
	}else if (fieldName.equals("fieldSeparator")){
		useTab = useSpace = false;
		useOther = true;
		ed.toControls("useTab,useSpace,useOther");
	}else if (fieldName.equals("useOther")){
		if (useOther){
			Gui.takeFocus((Control)ed.findFieldTransfer("fieldSeparator").dataInterface,0);
		}
	}else if (fieldName.equals("rowHeaders")){
		if (!rowHeaders) {
			reorderRows = false;
			ed.toControls("reorderRows");
		}
	}
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("export")){
		if (toFile) lastExport = outputFile.getFullPath();
		Handle h =
			//new samples.progress.DataTransfer()
			model.getExportTask(this)
			.startTask();
		ProgressAndControl pc = (ProgressAndControl)ed.getProperties().getValue("progress",null);
		pc.startTask(h,"Exporting Data");
		if (h.waitOnFlags(h.Succeeded,TimeOut.Forever))
			new MessageBox("Export Complete","Data exported successfully.",MessageBox.MBOK).execute();
		else
			if (h.errorObject instanceof Throwable)
				new ReportException((Throwable)h.errorObject,null,null,false).execute();
			else
				new MessageBox("Export Error","There was an error exporting the data!",MessageBox.MBOK).execute();
		pc.endTask();
	}
}
//##################################################################
}
//##################################################################

