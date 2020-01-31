/* $MirOS: contrib/hosted/ewe/classes/ewe/ui/PhoneVMLauncher.java,v 1.2 2008/04/30 23:19:27 tg Exp $ */

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
import ewe.util.Vector;
import ewe.sys.*;
import ewe.io.FileChooserParameters;
import ewe.io.File;
import ewe.fx.ImageCache;
import ewe.fx.IImage;
import ewe.fx.Color;
//##################################################################
public class PhoneVMLauncher extends Form{
//##################################################################

public static VMApps apps = new VMApps();
public PhoneMenu phoneMenu;

//===================================================================
public PhoneVMLauncher()
//===================================================================
{
	this(null);
}
//===================================================================
public PhoneVMLauncher(Form welcomeScreen)
//===================================================================
{
	this.welcomeScreen = welcomeScreen;
	windowTitle = "Ewe VM Launcher.";
	title = "Ewe Applications.";
	//hasTopBar = true;
	PhoneMenu pm = phoneMenu = new PhoneMenu();
	addLast(pm);
	apps.read();
	appsToMenu();

	if (Gui.isSmartPhone){
		makeSoftKeys("Exit",null,new Object[]{"Run Application|Run","-","Add Application|Add","Edit Application|Edit","Delete Application|Delete","-","VM Options","About Ewe VM","Exit"},"Menu");
	}
}

static Form welcomeScreen;

//===================================================================
public void runSelected()
//===================================================================
{
	VMApp app = getSelectedApp();
	if (app != null) app.run();
}
//===================================================================
public void deleteSelected()
//===================================================================
{
	int idx = getSelectedItem();
	if (idx != -1){
		VMApp app = getSelectedApp();
		MessageBox mb = new MessageBox("Remove App","Remove the link to\nthis Application?\n\n"+app.name,MBYESNO);
		if (mb.execute() != IDYES) return;
		apps.apps.removeElementAt(idx);
		appsToMenu();
		if (!phoneMenu.makeVisible(0))
			phoneMenu.repaintNow();
		phoneMenu.setSelectedItem(phoneMenu.countItems() == 0 ? -1 : 0);
		apps.save();
	}
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
	apps.save();
	return true;
}
//===================================================================
public void doEdit()
//===================================================================
{
	VMApp app = getSelectedApp();
	if (app == null) return;
	if (editApp(app,getFrame())){
		int it = getSelectedItem();
		phoneMenu.items.set(it,toMenuItem(app));
		phoneMenu.repaintItem(it);
		apps.save();
	}
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
	appsToMenu();
	phoneMenu.setSelectedItem(apps.apps.size()-1,true);
	apps.save();
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
	}
	return add(newOne);
}
//===================================================================
public boolean handleAction(String action)
//===================================================================
{
	if (action.equalsIgnoreCase("Run")) runSelected();
	else if (action.equalsIgnoreCase("Delete")) deleteSelected();
	else if (action.equalsIgnoreCase("Add")) doAdd();
	else if (action.equalsIgnoreCase("Edit")) doEdit();
	else if (action.equals("Exit")) exit(0);
	else if (action.equals("VM Options")){
		VMOptions vo = VMOptions.getVMOptions();
		VMOptions tv = (VMOptions)vo.getCopy();
		if (tv.getEditor(0).execute() == IDCANCEL) return true;
		vo.copyFrom(tv);
		vo.save();
		vo.apply();
		return true;
	}else if (action.equals("About Ewe VM")){
			int v = ewe.sys.Vm.getVersion();
			String version = ""+(v/100)+".";
			v = v%100;
			if (v < 10) version += "0";
			version += v;
			if (!new ewe.reflect.Type("ewe.filechooser.FileChooser").exists()) version = "Micro-"+version;
		String about = "MirEwe Virtual Machine,\nVersion " + version +
		    "\n\nby The MirOS Project\nmiros-discuss@mirbsd.org\n" +
		    "Derived from the Ewe VM by Michael L Brereton\n" +
		    "Derived from the Waba VM by Rick Wild\n\n" +
		    "This is free software distributed under the\n" +
		    "GNU Lesser General Public Licence,\n" +
		    "please visit http://www.fsf.org/ to get a copy.";
		MessageBox mb = new MessageBox("About Ewe VM",about,MBOK);
		mb.execute();
	}
	else return super.handleAction(action);
	return true;
}
//-------------------------------------------------------------------
protected void formShown()
//-------------------------------------------------------------------
{
	super.formShown();
	phoneMenu.setSelectedItem(phoneMenu.countItems() == 0 ? -1 : 0);
	if (welcomeScreen != null) welcomeScreen.exit(0);
	ewe.sys.VMOptions.doStartUp(getFrame());
}
//-------------------------------------------------------------------
protected MenuItem toMenuItem(VMApp va)
//-------------------------------------------------------------------
{
	IImage ii = va.image;
	if (ii == null) ii = ImageCache.cache.get("ewe/ewebig.bmp",Color.White);
	return phoneMenu.makeItem(ii,va.name);
}
//-------------------------------------------------------------------
protected void appsToMenu()
//-------------------------------------------------------------------
{
	Vector v = apps.apps;
	phoneMenu.setSelectedItem(-1);
	phoneMenu.items.clear();
	for (int i = 0; i<v.size(); i++)
		phoneMenu.items.add(toMenuItem((VMApp)v.get(i)));
}
//-------------------------------------------------------------------
protected int getSelectedItem()
//-------------------------------------------------------------------
{
	return phoneMenu.getSelectedItem();
}
//-------------------------------------------------------------------
protected VMApp getSelectedApp()
//-------------------------------------------------------------------
{
	int idx = getSelectedItem();
	if (idx == -1) return null;
	return (VMApp)apps.apps.get(idx);
}
//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.type == ev.PRESSED){
		if (ev.target == phoneMenu){
			runSelected();
		}
	}
}

//##################################################################
}
//##################################################################
