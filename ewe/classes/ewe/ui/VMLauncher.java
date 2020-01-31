/* $MirOS: contrib/hosted/ewe/classes/ewe/ui/VMLauncher.java,v 1.2 2008/04/30 23:19:27 tg Exp $ */

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
import ewe.filechooser.FileChooser;
import ewe.util.Vector;
import ewe.reflect.Type;
//##################################################################
public class VMLauncher extends Editor{
//##################################################################

private Type web = new Type("ewe.ui.WebBrowser");
private Type notepad = new Type("ewe.ui.Notepad");

//===================================================================
public VMLauncher()
//===================================================================
{
	this(null);
}
//===================================================================
public VMLauncher(Form welcomeScreen)
//===================================================================
{
	this.welcomeScreen = welcomeScreen;
	boolean onTop = Gui.screenIs(Gui.WIDE_SCREEN);
	mTabbedPanel mt = (mTabbedPanel)addTabbedPanel(false)[0];
	mt.cardPanel.autoScroll = false;
	ewe.sys.VMOptions vmoptions = ewe.sys.VMOptions.getVMOptions();
	if (vmoptions.showIcon)
		taskbarIcon = new Window.TaskBarIconInfo("ewe/ewesmall.bmp","ewe/ewesmallmask.bmp","Ewe VM");
	mt.addCard(new LaunchPanel(),"Applications",null).iconize("ewe/programsmall.bmp",ewe.fx.Color.White);
	//
	String initial = "\\";
	int type = FileChooser.BROWSE|FileChooser.LAUNCHER_TYPE;
	initial = null;//vmoptions.launcherStart;
	if (vmoptions.launcherTree) type |= FileChooser.DIRECTORY_TREE;
	else type &= ~FileChooser.DIRECTORY_TREE;
	Type ty = new Type("ewe.filechooser.FileChooser");
	if (ty.exists())
		mt.addCard((Control)ty.newInstance("(ILjava/lang/String;)",new Object[]{new Integer(type),initial}),"File System",null).iconize("ewe/ChooseFile.bmp",ewe.fx.Color.White);
	//
	mt.cardPanel.autoScroll = true;
	mt.addCard(vmoptions.getEditor(0),"VM Options",null).iconize("ewe/optionssmall.bmp",ewe.fx.Color.White);
	CellPanel cp = mt.getExtraControls(true);
	String utils = "";
	if (LaunchPanel.launcher != null) utils = "Add Launch Item$a";
	if (notepad.exists()) utils += "|Text Editor$t";
	if (web.exists()) utils += "|HTML Viewer$h";
	utils += "|Registry View$r";
	if (ewe.reflect.Reflect.getForName("apps.eweconfig.EweConfig") != null) utils += "|EweSync Configuration$c";
	if (!ewe.sys.Vm.isMobile()) utils += "|-|Zaurus EweSync Server|EweSync Emulator";
	String [] extra = ewe.sys.VMOptions.getExtraActions();
	if (extra.length != 0){
		utils += "|-";
		for (int i = 0; i<extra.length; i++)
			utils += "|"+extra[i];
	}
	utils += 	"|-|About Ewe VM$e|Exit$x";
	Menu um = new Menu(ewe.util.mString.split(utils),"Tools");
	PullDownMenu pdm = new ButtonPullDownMenu("",um);
	pdm.arrowDirection = mt.tabLocation == mt.NORTH ? Down : Up;
	pdm.borderWidth = 0; pdm.borderStyle = BDR_NOBORDER;
	pdm.image = ewe.fx.ImageCache.cache.get("ewe/ewesmall.bmp",ewe.fx.Color.White);
	pdm.setToolTip(new ewe.fx.IconAndText(pdm.image,"Tools",null));
	MenuBar mb = new MenuBar();
	mb.addMenu(pdm);
	cp.addLast(mb);
	addField(mb,"tools");
	//cp.addLast(new mButton("Another$a","ewe/ewesmall.bmp",ewe.fx.Color.White));
}

public MenuItem tools;

static Form welcomeScreen;
//===================================================================
public void formShown()
//===================================================================
{
	super.formShown();
	if (welcomeScreen != null) welcomeScreen.exit(0);
	ewe.sys.VMOptions.doStartUp(getFrame());
}
//===================================================================
public void action(String field,Editor ed)
//===================================================================
{
	if (field.equals("Add Launch Item"))
		LaunchPanel.launcher.doAdd();
	else if (field.equals("About Ewe VM")){
			int v = ewe.sys.Vm.getVersion();
			String version = ""+(v/100)+".";
			v = v%100;
			if (v < 10) version += "0";
			version += v;
			if (!new ewe.reflect.Type("ewe.filechooser.FileChooser").exists()) version = "Micro-"+version;
			String about = "<head><title>About Ewe VM</title>" +
			    "</head><body><b><center>" +
			    "MirEwe Virtual Machine, Version&nbsp;" + version +
			    " by The&nbsp;MirOS&nbsp;Project</center></b><p>" +
			    "<center><a href=\"http://mirbsd.de/\">" +
			    "miros-discuss@mirbsd.org</a></center><p>" +
			    "Derived from the Ewe VM by Michael L Brereton<p>" +
			    "Derived from the Waba VM by Rick Wild<p>" +
			    "This is free software distributed under the " +
			    "GNU Lesser General Public Licence, " +
			    "please visit http://www.fsf.org/ to get a copy." +
			    "</body>";
			if (web.exists()){
				IWebBrowser wb = (IWebBrowser)web.newInstance();
				((Form)wb).exec(isModal());
				wb.setHtml(about,"about:Ewe",null);
			}
	}else if (field.equals("Exit")){
		exit(0);
	}else if (field.startsWith("EweSync C")){
		try{
			ewe.reflect.Reflect r = ewe.reflect.Reflect.getForName("apps.eweconfig.EweConfig");
			if (r != null)
				((Form)r.newInstance()).execute();
		}catch(Throwable t){}
	}else if (field.equals("Text Editor") && notepad.exists()){
		Form rf = (Form)notepad.newInstance();
		rf.setPreferredSize(640,480);
		rf.execute();
	}else if (field.equals("HTML Viewer") && web.exists()){
		Form rf = (Form)web.newInstance();
		//rf.htmlProperties.set("maxImageSize",new ewe.fx.Dimension(16,16));
		//rf.htmlProperties.setBoolean("allowImages",false);
		//rf.htmlProperties.setBoolean("allowAnimatedImages",false);
		rf.setPreferredSize(640,480);
		rf.execute();
	}else if (field.equals("Registry View")){
		Form rf = new ewex.registry.RegistryViewForm(false);
		rf.setPreferredSize(640,480);
		ewe.sys.Device.preventIdleState(true);
		rf.execute();
		ewe.sys.Device.preventIdleState(false);
	}else if (field.equals("Zaurus EweSync Server")){
		try{
			ewe.sys.Vm.execEwe(new String[]{"ewe.io.RemoteConnection","zaurus"},null);
		}catch(ewe.io.IOException e){
		}
	}else if (field.equals("EweSync Emulator")){
		try{
			ewe.sys.Vm.execEwe(new String[]{"ewe.io.RemoteConnection","emulator"},null);
		}catch(ewe.io.IOException e){
		}
	}else
		ewe.sys.VMOptions.doExtraAction(field,getFrame());
}
//##################################################################
}
//##################################################################
