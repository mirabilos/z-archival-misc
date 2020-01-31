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
package ewe.ui;
import ewe.fx.*;
import ewe.sys.*;
import ewe.io.FileChooserParameters;
import ewe.io.File;
//import ewe.filechooser.FileChooser;
//##################################################################
public class LaunchPanel extends Editor{
//##################################################################

public static VMApps apps = new VMApps();
public static LaunchPanel launcher;
LauncherModel model;
public MenuItem menu;
LaunchTable table;

//===================================================================
public LaunchPanel()
//===================================================================
{
	title = "Ewe Launcher";
	setPreferredSize(240,320);
	windowFlagsToSet = Window.FLAG_MAXIMIZE_ON_PDA;
	launcher = this;
	TableControl tc = table = new LaunchTable();
	tc.setTableModel(model = new LauncherModel());
	addField(tc,"menu");
	ScrollBarPanel sb = new ScrollBarPanel(tc);
	sb.vbar.setFollowTracking(true);
	addLast(sb);
	apps.read();
}

//===================================================================
public boolean add(String pathName,Frame parent)
//===================================================================
{
	VMApp app = new VMApp();
	if (!app.makeFrom(pathName,parent)) return false;
	if (!editApp(app,parent)) return false;
	return add(app);
}

//===================================================================
public boolean add(VMApp app)
//===================================================================
{
	app.load();
	apps.apps.add(app);
	model.updateDisplay();
	apps.save();
	return true;
}

//===================================================================
boolean editApp(VMApp app,Frame parent)
//===================================================================
{
	if (app == null) return false;
	VMApp ed = (VMApp)app.getCopy();
	Form f = ed.getEditor(0);
	Gui.setOKCancel(f);
	if (f.execute(parent,Gui.CENTER_FRAME) != IDOK) return false;
	app.copyFrom(ed);
	return true;
}
//===================================================================
public boolean doAdd()
//===================================================================
{
	VMApp newOne = new VMApp();
	Editor ed = newOne.getEditor(newOne.ADD_NEW);
	Gui.setOKCancel(ed);
	int got = ed.execute(getFrame(),Gui.CENTER_FRAME);
	if (got == IDCANCEL) return false;
	if (got == 100){
		FileChooserParameters fcp = new FileChooserParameters();
		fcp.set(fcp.TYPE,"open");
		fcp.setInt(fcp.OPTIONS,fcp.OPTION_QUICK_SELECT);
		fcp.set(fcp.TITLE, "Choose Ewe Application");
		fcp.add(fcp.FILE_MASK,"*.ewe;*.class - Ewe and Class Files");
		fcp.set(fcp.START_LOCATION,ewe.sys.Vm.getProperty("DOCUMENTS_DIR","/"));
		if (!File.getNewFile().executeFileChooser(fcp)) return false;
		String f = fcp.getValue(fcp.CHOSEN_FILE,null).toString();
		return add(f,getFrame());
	/*
		FileChooser fc = new FileChooser(FileChooser.OPEN|FileChooser.QUICK_SELECT,);
		fc.title =;
		fc.addMask();
		//fc.addMask("*.class - Class Files");
		if (fc.execute() == IDCANCEL) return false;
		String f = fc.getChosen();
		return add(f,getFrame());
		*/
	}
	return add(newOne);
}
//===================================================================
public void action(String field,Editor ed)
//===================================================================
{
	VMApp app = null;
	Rect r = table.getSelection(new Rect());
	if (r != null) app = getApp(r.y,r.x);
	if (field.equals("Delete") && app != null){
		if (new MessageBox("Delete Link","Delete link to\n"+app.name+"?",MBYESNO).execute(getFrame(),Gui.CENTER_FRAME) == IDYES){
			apps.apps.remove(app);
			apps.save();
			table.clearSelection(null);
			model.updateDisplay();
		}
	}else if (field.equals("Add New")){
		doAdd();
	}else if (field.equals("Edit") && app != null){
		if (editApp(app,getFrame())) {
			apps.save();
			model.updateDisplay();
		}
	}else if (field.equals("Run") && app != null){
		app.run();
	}else if (app != null)
		VMOptions.doExtraContextAction(app,field,ed.getFrame());
}
//===================================================================
VMApp getApp(int row,int col)
//===================================================================
{
	int idx = model.toIconIndex(row,col);
	if (idx >= 0) return (VMApp)apps.apps.get(idx);
	return null;
}
//===================================================================
public void update()
//===================================================================
{
	model.updateDisplay();
}
//##################################################################
class LaunchTable extends TableControl{
//##################################################################

//===================================================================
LaunchTable()
//===================================================================
{
	setClickMode(true);
	//clickClearsItself = false;
}
//===================================================================
public void clicked(int row,int col)
//===================================================================
{
	if ((clickedFlags & TableEvent.FLAG_SELECTED_BY_ARROWKEY) != 0)
		return;
	VMApp app = getApp(row,col);
	if (app != null) app.run();
}

//##################################################################
}
//##################################################################

//##################################################################
class LauncherModel extends IconTableModel{
//##################################################################

private IImage eweIcon;

{
	eweIcon = ImageCache.cache.get("ewe/ewebig.bmp",Color.White);
	setVMode(true);
}
public Menu getMenuFor(int row,int col)
{
	int idx = toIconIndex(row,col);
	if (idx >= 0){
		String [] got = new String[]{"Run","Edit","Delete","Add New"};
		String [] extra = VMOptions.getExtraContextActions();
		if (extra != null) got = (String [])ewe.util.Utils.appendArray(got,extra);
		return new Menu(got,"Menu");
	}else
		return new Menu(new String[]{"Add New"},"Menu");
}
public int getIconCount() {return apps.apps.size();}
public String getIconText(int idx)
{
	VMApp app = (VMApp)apps.apps.get(idx);
	return app.name;
}
public IImage getIconImage(int idx)
{
	VMApp app = (VMApp)apps.apps.get(idx);
	if (app.image != null) return app.image;
	return eweIcon;
}
//##################################################################
}
//##################################################################

//##################################################################
}
//##################################################################

