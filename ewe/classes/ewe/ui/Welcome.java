/* $MirOS: contrib/hosted/ewe/classes/ewe/ui/Welcome.java,v 1.2 2008/04/30 23:19:27 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import ewe.util.*;
import ewe.reflect.Reflect;
import ewex.registry.*;
import ewe.reflect.Type;

//##################################################################
public class Welcome extends mApp{
//##################################################################
//Font boldFont;
//Font plainFont;
String version = "version ";

/** Constructs the welcome application. */
String message;
String welcomeMessage;
LocalResource lr;

//-------------------------------------------------------------------
protected void setupMainWindow()
//-------------------------------------------------------------------
{
	welcomeMessage = "Welcome to Ewe";
	Locale l = Vm.getLocale();
	lr = l.getLocalResource("ewe.ui.Welcome",false);
	welcomeMessage = (String)lr.get(100,"Welcome to Ewe");
	windowTitle = welcomeMessage;
	windowFlagsToClear = FLAG_IS_VISIBLE;
	super.setupMainWindow();
}
Reflect config, registry;

Object configObject;

//===================================================================
public void run()
//===================================================================
	{
	try{
		int v = ewe.sys.Vm.getVersion();
		String version = ""+(v/100)+".";
		v = v%100;
		if (v < 10) version += "0";
		version += v;
	/*
	new ewe.sys.mThread(){
		public void run(){
			while(true){
				nap(3000);
				ewe.sys.Vm.debug("",100);
			}
		}
	}.start();
	*/
	ewe.sys.Vm.showWait(false);
	MessageBox mb =
	new MessageBox(
	"MirEwe VM","Ewe Launcher/VM Version "+version+"\nThe MirOS Project\nmiros-discuss@mirbsd.org\n\nFreeware Version\n\nThe launcher is loading...",0);//Gui.isSmartPhone ? Form.MBOK : 0);
	mb.doBeep = false;
	mb.show();
	mb.waitUntilPainted(500);
	//mThread.nap(1000);
	//Form.showWait();
	String toRun = Vm.isMobile() || true ? "Launcher" : "Config";
	if (programArguments.length != 0) toRun = programArguments[0];
	if (toRun.equalsIgnoreCase("Launcher")){
		Form f = null;
		if (!Gui.isSmartPhone)
			f = (Form)new Type("ewe.ui.VMLauncher").newInstance("(Lewe/ui/Form;)V",new Object[]{mb});
		if (f == null)
			f = (Form)new Type("ewe.ui.PhoneVMLauncher").newInstance("(Lewe/ui/Form;)V",new Object[]{mb});
		if (f != null){
			f.title = "Ewe Launcher - V"+version;
			f.execute();//Gui.NEW_WINDOW);
		}else{
			if (mb != null) mb.waitUntilClosed();
		}
	}else{
		try{
			Reflect r = Reflect.getForName("apps.eweconfig.EweConfig");
			if (r != null)
				((Form)r.newInstance()).execute();
		}catch(Throwable t){
		}
	}
	}catch(Throwable t){
		t.printStackTrace();
	}
	exit(0);
	}
/*
	public void tryForm(Object who)
	{
		if (!(who instanceof Form)) exit(0);
		else{
			clearWindowFlags(FLAG_IS_VISIBLE);
			Form f2 = (Form)who;
			f2.exitSystemOnClose = true;
			f2.show(Gui.NEW_WINDOW);
		}
	}
	public void action(ewe.reflect.FieldTransfer ft,Editor f)
	{
		f.exitSystemOnClose = false;
		f.exit(0);
		if (ft.fieldName.equals("runDemo")){
			clearWindowFlags(FLAG_IS_VISIBLE);
			new ewe.filechooser.FileChooserDemo().runDemo(true);
		}else if (ft.fieldName.equals("doConfig")){
			tryForm(new apps.eweconfig.EweConfig());
			//tryForm(config.newInstance());
		//}else if (ft.fieldName.equals("runZip")){
			//tryForm(new ewe.zip.ZipFileBrowser());
		}else if (ft.fieldName.equals("runRegistry")){
			if (ewe.sys.Coroutine.getCurrent() == null){
			}
			Object who = registry.newInstance();
			tryForm(who);
		}
	}

	//===================================================================
	void setAppLevel()
	//===================================================================
	{
		if (!Registry.isInitialized(false)) return;
		RegistryKey eweDll = Registry.getLocalKey(Registry.HKEY_CLASSES_ROOT,"EweFile10\\DLL",true,false);
		if (eweDll == null) return;
		String eweLocation = (String)eweDll.getValue(null);
		if (eweLocation == null) return;
		eweLocation = eweLocation.substring(0,eweLocation.length()-3)+"exe";
		//RegistryKey wrongLevel = Registry.getLocalKey(Registry.HKEY_LOCAL_MACHINE,"SOFTWARE\\CASIO\\COShell\\AppCategory\\0",true,false);
		//if (wrongLevel != null) wrongLevel.delete();
		RegistryKey appLevel = Registry.getLocalKey(Registry.HKEY_LOCAL_MACHINE,"SOFTWARE\\CASIO\\COShell\\AppCategory\\00",true,false);
		if (appLevel == null) return;
		int highest = 0;
		for (int i = 0;;i++){
			StringBuffer name = new StringBuffer();
			Object al = appLevel.getValue(i,name);
			if (al == null) break;
			if (!(al instanceof String)) continue;
			String got = (String)al;
			int val = ewe.sys.Convert.toInt(name.toString());
			got = got.toUpperCase();
			if (got.endsWith("EWE.EXE")){
				int l = got.length();
				boolean isIt = (l == 7);
				if (!isIt){
					char c = got.charAt(l-8);
					isIt = (c == '\\' || c == '/');
				}
				if (isIt) {
					highest = val-1;
					break;
				}
			}
			if (val > highest) highest = val;
		}
		String index = ""+(highest+1);
		int idx = eweLocation.indexOf('\\');
		if (idx == 0) {
			eweLocation = eweLocation.substring(1,eweLocation.length());
			idx = eweLocation.indexOf("\\");
		}
		if (idx != -1)
			eweLocation = eweLocation.substring(idx+1,eweLocation.length());
		//ewe.sys.Vm.debug("Setting: "+index+", "+eweLocation);
		appLevel.setValue(index,eweLocation);
	}
*/
//##################################################################
}
//##################################################################
