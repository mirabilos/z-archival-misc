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
package ewe.sys;
import ewe.fx.Font;
import ewe.ui.CellPanel;
import ewe.ui.Editor;
import ewe.ui.FontChooser;
import ewe.ui.Frame;
import ewe.ui.Gui;
import ewe.ui.InputStack;
import ewe.ui.MessageBox;
import ewe.ui.UIBuilder;
import ewe.ui.Window;
import ewe.ui.mApp;
import ewe.ui.mButton;
import ewe.ui.mChoice;
import ewe.ui.mLabel;
import ewe.util.TextDecoder;
import ewe.util.TextEncoder;

//##################################################################
public class VMOptions extends ewe.data.LiveObject{
//##################################################################

public boolean showIcon = false;
public boolean launcherTree = false;
public String launcherStart = "\\";
public boolean exitAfterLaunch = Vm.isMobile();
public boolean keepVmResident = false;//Vm.isMobile();
public boolean singleWindowed = false;
public boolean fixedSIPButton = false;
public String systemFontName = "Helvetica";
public int systemFontSize = !Vm.isMobile() ? 14 : 12;
public int guiFontStyle = 0;
public int style = Gui.STYLE_ETCHED;
public FontChooser fontChooser = new FontChooser(false);
public String fixedFontName = "Courier New";
public boolean useSIP = true;
public String _fields = "fixedSIPButton,singleWindowed,keepVmResident,launcherTree,systemFontName,systemFontSize,guiFontStyle,showIcon,style,exitAfterLaunch,useSIP";
public String pathToEwe="/Program Files/ewe";
public static String currentDir = "C:\\";
//private FontInput fontInput;

//===================================================================
public static VMOptions getVMOptions()
//===================================================================
{
	return (VMOptions)ewe.ui.mApp.vmOptions;
}

//===================================================================
public void readAndApply()
//===================================================================
{
	read();
	apply();
	Font sys = toFont();
	mApp.addFont(sys,"system");
	mApp.addFont(sys,"text");
	mApp.addFont(sys.changeStyle(guiFontStyle),"gui");
	mApp.addFont(sys.changeNameAndSize(fixedFontName,sys.getSize()+2),"fixed");
	mApp.addFont(sys.changeNameAndSize(null,sys.getSize()-2),"small");
	mApp.addFont(sys.changeNameAndSize(null,sys.getSize()+2),"big");
}
//public String aTest = "This is a test";

//===================================================================
public static String [] getExtraActions()
//===================================================================
{
	return new String[]{"Unload Vm from Memory$u"};
}
//===================================================================
public static boolean doExtraAction(String actionName,Frame parent)
//===================================================================
{
	if (actionName.equals("Unload Vm from Memory")){
		try{
			Vm.preloadVM(false);
		}catch(Throwable e){}
		new MessageBox("VM Unloaded","The VM has been unloaded from memory.",MessageBox.MBOK).execute();
		return true;
	}
	return false;
}
//===================================================================
public static String [] getExtraContextActions()
//===================================================================
{
	return new String [0];
}

//===================================================================
public static boolean doExtraContextAction(VMApp app,String action,Frame parent)
//===================================================================
{
	return false;
}

//-------------------------------------------------------------------
protected TextEncoder encode(TextEncoder te)
//-------------------------------------------------------------------
{
	encodeFields(_fields,te,"VMOptions");
	return super.encode(te);
}
//-------------------------------------------------------------------
protected TextDecoder decode(TextDecoder te)
//-------------------------------------------------------------------
{
	decodeFields(_fields,te,"VMOptions");
	return super.decode(te);
}

//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int options)
//===================================================================
{
	UIBuilder b = new UIBuilder(ed,this,cp);
	ed.title = Gui.isSmartPhone ? "Ewe Options" : "Ewe VM/Laucher Options";
	ed.windowFlagsToClear |= Window.FLAG_HAS_CLOSE_BUTTON;
	boolean sp = ed.modifyForSmartPhone();
	enableEditorScrolling(ed,true);
	//cp.setCell(cp.DONTSTRETCH);
	b.open().setControl(cp.HCONTRACT|cp.CENTER);
		if (!sp){
			b.open().setCell(cp.HSHRINK);
				b.add("createShortcut",new mButton("Place Ewe on Start Menu")).setControl(cp.DONTFILL);
			b.close(true);
		}
		//
		// True/False options.
		//
		InputStack is = b.openInputStack(sp ? null : "Launcher/Files");
			is.columns = sp ? 1 : 2;
			is.setCell(cp.HSTRETCH);
			if (!sp)
				b.addAll("Use Input Panel|useSIP|Fixed SIP Button|fixedSIPButton|Single Window|singleWindowed|Taskbar Icon|showIcon|Dir. Tree|launcherTree|Exit After Launch|exitAfterLaunch|Keep Vm Resident|keepVmResident");
			else{
				is.doubleLined = sp;
				//is.addInput("Hello","What?");
				b.addAll("Keep Vm Resident|keepVmResident|Exit After Launch|exitAfterLaunch");
			}
		b.close(true);

		if (!sp){
			b.open("System Font").setCell(cp.HSTRETCH);//.setControl(cp.HCONTRACT|cp.CENTER);
			b.add("fontChooser",fontChooser.getEditor(0)).setCell(cp.HSTRETCH).setControl(cp.HCONTRACT|cp.LEFT);
			fontChooser.font = systemFontName;
			fontChooser.size = systemFontSize;
			b.endRow();
				b.open();
					b.add(new mLabel("GUI Font:")).setCell(cp.DONTSTRETCH).setControl(cp.HCONTRACT|cp.LEFT);
					b.add("guiFontStyle",new mChoice(new String[]{"Plain","Bold","Italic","Bold & Italic"},0)).setControl(cp.HCONTRACT|cp.LEFT);
				b.close();
			b.close(true);
			b.openInputStack("Look and Feel:").setCell(cp.HSTRETCH);//.setControl(cp.HCONTRACT|cp.CENTER);
				b.add("Style","style",new mChoice(new String[]{"3-D","Etched","Soft","Palm"},0));
			b.close(true);
		}else{
			is = b.openInputStack();
				is.setCell(cp.HSTRETCH);
				is.doubleLined = true;
				b.add("System Font:","fontChooser",fontChooser.getEditor(0));//fontInput = new FontInput(FontInput.OPTION_NO_STYLE));
				fontChooser.font = systemFontName;
				fontChooser.size = systemFontSize;
				//fontInput.fromFont(new Font(systemFontName,Font.PLAIN,systemFontSize));
				b.add("guiFontStyle",new mChoice(new String[]{"Plain","Bold","Italic","Bold & Italic"},0));//.setControl(cp.HCONTRACT|cp.LEFT);
				b.add("Look and Feel:","style",new mChoice(new String[]{"3-D","Etched","Soft","Palm"},0));
			b.close(true);
		}
		//
		// Save the options.
		//
		if (!sp){
			b.open().setCell(cp.HSTRETCH);//.setBorder(cp.EDGE_ETCHED,2);
					b.add("save",new mButton("Save","ewe/savesmall.bmp",ewe.fx.Color.White)).setControl(cp.DONTFILL);
			b.close();
		}
		//
		// Close it.
		//
	b.close();
	Gui.setOKCancel(ed);
}

//===================================================================
public ewe.fx.Font toFont()
//===================================================================
{
	return new ewe.fx.Font(systemFontName,0,systemFontSize);
}

boolean messageGiven = false;
//===================================================================
public void showRestartMessage()
//===================================================================
{
	if (!messageGiven)
		new MessageBox("Restart Required","You must restart the Ewe Launcher\nfor changes to take effect.",MessageBox.MBOK).execute();
	messageGiven = true;
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("setStart")){
		launcherStart = currentDir;
		ed.toControls("launcherStart");
	}else if (fieldName.equals("createShortcut")){
		if (!createShortcutToEwe())
			new MessageBox("Create Shortcut Failed","Could not create shortcut!",MessageBox.MBOK).execute();
	}else if (fieldName.equals("save")){
		save();
		mApp.setupFonts();
		ed.repaintNow();
	}
}
//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("fontChooser")){
		systemFontName = fontChooser.font;
		systemFontSize = fontChooser.size;
	}
	super.fieldChanged(fieldName,ed);
}
//===================================================================
public boolean read()
//===================================================================
{
	try{
		return ewe.io.IO.getConfigInfo(this,"Ewesoft\\EweVM");
	}catch(Exception e){
		//new ewe.ui.ReportException(e).execute();
		return false;
	}
}
//===================================================================
public boolean save()
//===================================================================
{
	try{
		ewe.io.IO.saveConfigInfo(this,"Ewesoft\\EweVM");
		return true;
	}catch(ewe.io.IOException e){
		//new ewe.ui.ReportException(e).execute();
		return false;
	}
}
//===================================================================
public boolean apply()
//===================================================================
{
	Gui.setStyle(style);
	ewe.sys.Vm.setParameter(ewe.sys.Vm.SET_USE_SIP,useSIP ? 1 : 0);
	try{
		Vm.preloadVM(keepVmResident);
	}catch(Throwable e){}
	return true;
}
//===================================================================
public static boolean createShortcutToEwe()
//===================================================================
{
	try{
		String el = ewe.sys.Vm.getPathToEweVM();
		String wm = ewex.registry.Registry.getSpecialFolder(ewex.registry.Registry.FOLDER_STARTMENU);
		if (el == null || wm == null) throw new NullPointerException();
		ewex.registry.Registry.createShortcut(el,null,wm+"/Ewe.lnk");
		return true;
	}catch(Exception e){
		return false;
	}
}
//===================================================================
public static void doStartUp(final Frame parent){}
//===================================================================

//##################################################################
}
//##################################################################
