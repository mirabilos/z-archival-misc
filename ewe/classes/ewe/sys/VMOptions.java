/*
Note - This is the Linux version of VMOptions.java
*/
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
import ewe.ui.*;
import ewe.util.*;
import ewe.data.*;
import ewe.filechooser.*;
import ewe.fx.Font;
import ewe.io.*;
import ewex.registry.*;

//##################################################################
public class VMOptions extends ewe.data.LiveObject{
//##################################################################
static {
	//Control.doubleBuffer = false;
}

public static boolean isMobile()
{
	return Vm.isMobile();
}

public boolean showIcon = false;
public boolean launcherTree = false;
public String launcherStart = "\\";
public boolean exitAfterLaunch = isMobile();
public boolean singleWindowed = false;
public boolean fixedSIPButton = false;
public boolean keepVmResident = false; //Not used in Linux.
public String systemFontName = "helvetica";
public int systemFontSize = !isMobile() ? 12 : 10;
public int guiFontStyle = 0;
public int style = Gui.STYLE_ETCHED;
public FontChooser fontChooser = new FontChooser(false);
public String fixedFontName = "fixed";
public String pathToEwe = isMobile() ? "/home/QtPalmtop/bin" : "/usr/bin";
public boolean useSIP = false;
public boolean firstRun = true;
public String _fields = "keepVmResident,singleWindowed,fixedSIPButton,launcherTree,systemFontName,systemFontSize,guiFontStyle,showIcon,style,exitAfterLaunch,pathToEwe,useSIP,firstRun";

public static String currentDir = "C:\\";

private static String [] extras;

private final static String eweUtils = "/bin/eweutils.ewe";

public static String qpeDir()
{
	return Vm.getProperty("QPEDIR","/home/QtPalmtop");
}
public static File getQtopiaDirectory()
{
	return new File(qpeDir());
}
public static boolean isQtopia()
{
	return Vm.getProperty("QPEDIR",null) != null;
}
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

public static String [] getExtraActions()
{
	if (!isMobile() || !isQtopia()) return new String[0];
	String e = "Install Application";
	e += "|Install Ewe Utility";//|Uninstall Application";
	//e += "|Install EweSync Server";
	return extras = mString.split(e);
}

public static String [] getExtraContextActions()
{
	return new String []{"Install"};
}

public static boolean doExtraContextAction(VMApp app,String action,Frame parent)
{
	if (action.equals("Install")){
		app.copyOver = false;
		return installEwe(app,parent);
	}
	return false;
}
public static boolean tryCopy(Stream in,Stream out,String action,Frame parent)
{
	if (in == null || out == null){
		if (in != null) in.close();
		if (out != null) out.close();
		return false;
	}
	IOTransfer it = new IOTransfer(in,out);
	Handle h = it.exec(action,parent);
	boolean didIt = h.waitOnFlags(Handle.Succeeded,TimeOut.Forever);
	in.close();
	out.close();
	if (!didIt) new MessageBox("Error","Copy operation failed!",MessageBox.MBOK).execute(parent,Gui.CENTER_FRAME);
	return didIt;
}

public static boolean doExtraAction(String actionName,Frame parent)
{
	if (extras == null) return false;
	if (actionName.equals(extras[0])){
		FileChooser fc = new FileChooser(FileChooser.OPEN|FileChooser.QUICK_SELECT,ewe.sys.Vm.getProperty("DOCUMENTS_DIR","/"));
		fc.title = "Choose Ewe Application";
		fc.addMask("*.ewe - Ewe Files");
		if (fc.execute() == fc.IDCANCEL) return false;
		return installEwe(fc.getChosen(),parent);
	}else if (actionName.equals(extras[1])){
		new EweUtilityChooser().getEditor(0).execute(parent,Gui.FILL_FRAME);
		return true;
		//return installEwe(qpeDir()+eweUtils,parent);
	}else if (actionName.equals("Run EweSync Server")){
		try{
			Vm.execEwe(new String[]{"ewe.io.RemoteConnection"},null);
			Vm.exit(0);
			return true;
		}catch(Exception e){
			new ReportException(e,null,null,false).execute();
			return false;
		}
	}else if (actionName.equals("Restart Qtopia")){
		ewe.sys.Vm.debug("Restarting...");
		mApp.mainApp.doSpecialOp(mApp.SPECIAL_RESTART_GUI,null);
	}
		return false;
}

public static boolean installEwe(String eweFile,Frame parent)
{
		String f = eweFile;
		VMApp app = new VMApp();
		app.makeFrom(f,parent);
		return installEwe(app,parent);
}
public static boolean installEwe(VMApp app,Frame parent)
{
	return installEwe(app,parent,true,true);
}
private static boolean changeEwePermisions()
{
	String text =
		"The Ewe VM must be run with root\n"+
		"privilege to install applications.\n\n"+
		"To do this press and hold the Ewe icon\n"+
		"in the Applications Tab. Then select the:\n"+
		"\"Execute with root privilege\" option.\n\n"+
		"Then restart the Ewe VM and try to\n"+
		"install the application again.";
	MessageBox mb = new MessageBox("Root Privilege Needed",text,MessageBox.MBOK);
	mb.execute();
	return false;
}
public static boolean installEwe(VMApp app,Frame parent,boolean prompt,boolean showRestart)
{
		File qt = new File(qpeDir());
		File exe = qt.getChild("bin/Ewe_Test_Run");
		try{
			Stream out = exe.toWritableStream(false);
			PrintWriter pw = new PrintWriter(out);
			pw.println("Test");
			pw.close();
			if (!exe.changePermissionsAndFlags(FilePermissions.GROUP_EXECUTE|FilePermissions.OWNER_EXECUTE|FilePermissions.OTHER_EXECUTE,0)){
				exe.delete();
				throw new IOException();
			}
			exe.delete();
		}catch(IOException e){
			return changeEwePermisions();
		}
		File target = (app.target == null || app.target.length() == 0) ? null : new File(app.target);
		String base = app.baseName();
		if (app.name.length() == 0) app.name = base;

		if (prompt){
			Editor ed = app.getEditor(app.ZAURUS_INSTALL);
			Gui.setOKCancel(ed);
			if (ed.execute(parent,Gui.CENTER_FRAME) != ed.IDOK) return false;
		}

		PropertyList pl = app.chosenProperties();
		if (pl == null) pl = PropertyList.nullPropertyList;
		base = app.baseName();
		File dest = new File(app.location);
		File cat = qt.getChild("apps/"+app.category);
		if (!cat.isDirectory()){
			if (prompt)
				if (new MessageBox("No Program Tab","No "+app.category+" program tab exists.\nCreate it now?",MessageBox.MBYESNO).execute(parent,Gui.CENTER_FRAME)
	           != MessageBox.IDYES) return false;
			boolean err = false;
			err = !cat.mkdir();
			if (!err){
				Stream s = cat.getChild(".directory").getOutputStream();
				if (s == null) err = true;
				else{
					StreamWriter sw = new StreamWriter(s); sw.useCR = false;
					sw.println("[Desktop Entry]");
					sw.println("Name="+app.category);
					sw.println("Icon="+(app.category.equals("Ewe") ? "ewe" : "AppsIcon"));
					sw.close();
					File appsDir = cat.getParentFile();
					File order = appsDir.getChild(".order");
					if (order.exists()){
						try{
							Stream in = order.toReadableStream();
							BufferedReader br = new BufferedReader(new InputStreamReader(in));
							File temp = appsDir.getChild("order_temp");
							PrintWriter pw = new PrintWriter(temp.toWritableStream(false));
							boolean sent = false;
							while(true){
								String line = br.readLine();
								if (line == null) break;
								pw.println(line);
								if (line.equals("Applications") && !sent){
									pw.println(app.category);
									sent = true;
								}
							}
							br.close();
							if (!sent) pw.println(app.category);
							pw.close();
							order.delete();
							temp.move(order);
						}catch(IOException e){}
					}
				}
			}
			if (err){
				return changeEwePermisions();
				/*
				new MessageBox("Error","Sorry could not create "+app.category+" tab.\nPlease try a different category.",MessageBox.MBOK).execute(parent,Gui.CENTER_FRAME);
				return false;
				*/
			}

			if (app.category.equals("Ewe")){
			/* Don't do this.
				File ewefrom = qt.getChild("apps/Applications/ewe.desktop");
				if (ewefrom.exists()) {
					if (new MessageBox("Move Ewe Runtime","Do you want to move the Ewe Runtime\nicon to the Ewe program tab?",MessageBox.MBYESNO).execute(parent,Gui.CENTER_FRAME)
           	== MessageBox.IDYES)
						ewefrom.move(qt.getChild("apps/Ewe/ewe.desktop"));
				}
			*/
			}
		}


		String error = null;
		boolean made = app.copyOver ? dest.mkdirs() : true;
		if (!made) error = "Cannot create destination directory!";
		else {
			File to = target != null ? dest.getChild(target.getName()) : null;
			if (app.copyOver && target != null)
				if (!tryCopy(target.getInputStream(),to.getOutputStream(),"Copying Ewe File",parent)) return false;
			if (!app.copyOver && target != null) to = target;
			RandomAccessFile raf = target != null ? new RandomAccessFile(target,RandomAccessFile.READ_ONLY) : null;

			String icon = pl.getString("Icon",null);
			while(true){
				boolean duplicate = false;
				if (!duplicate) duplicate = qt.getChild("apps/"+app.category+"/"+base+".desktop").exists();
				if (!duplicate) duplicate = qt.getChild("bin/"+base+"_Run").exists();
				if (!duplicate && (icon != null)) duplicate = qt.getChild("pics/"+base+"_Icon.png").exists();
				if (!duplicate && (icon != null)) duplicate = qt.getChild("pics/"+base+"_Icon.bmp").exists();
				if (!duplicate || !prompt) break;

				int dr = new MessageBox("Run Script Exists",
					"An application named "+base+" exists.\n\n"+
				  "Press \"Yes\" to replace it, \"No\" to rename it.",MessageBox.MBYESNOCANCEL).execute(parent,Gui.CENTER_FRAME);
				if (dr == MessageBox.IDCANCEL) return false;
				if (dr == MessageBox.IDYES) break;
				base = new InputBox("Rename Application").input(parent,base,20);
				if (base == null) return false;
			}

			if (icon != null){
				String ext = ".png";
				Stream in = null;
				if (raf != null) try{
				in = EweFile.getInputStream(raf,icon+ext,false);
				if (in == null) {
					ext = ".bmp";
					in = EweFile.getInputStream(raf,icon+ext,false);
				}
				}catch(IOException e){}
				if (in == null) icon = null;
				else{
					icon = base+"_Icon"+ext;
					File di = qt.getChild("pics/"+icon);
					if (!tryCopy(in,di.getOutputStream(),"Copying Icon File",parent)) return changeEwePermisions();
				}
			}else{
				if (app.iconIsImage && app.image != null){
					icon = base+"_Icon.png";
					File di = qt.getChild("pics/"+icon);
					ewe.fx.PNGEncoder pe = new ewe.fx.PNGEncoder();
					try{
						Stream out = di.toWritableStream(false);
						pe.writeImage(out,app.image);
						out.close();
					}catch(Exception e){
						return changeEwePermisions();
					}
				}
			}
			File dtop = qt.getChild("apps/"+app.category+"/"+base+".desktop");
			exe = qt.getChild("bin/"+base+"_Run");
			Stream s = exe.getOutputStream();
			if (s == null) error = "Cannot create run script!";
			else{
				StreamWriter sw = new StreamWriter(s); sw.useCR = false;
				sw.println(
				//mApp.vmOptions.pathToEwe+
				qpeDir()+"/bin/ewe -XappName=$0 "+
				app.vmArgs+" "+
				app.startClass+
				(target == null ? " " : " \""+to.getFullPath()+"\" ")+
				app.args
				);
				sw.close();
				try{
					if (!exe.changePermissionsAndFlags(FilePermissions.GROUP_EXECUTE|FilePermissions.OWNER_EXECUTE|FilePermissions.OTHER_EXECUTE,0))
						throw new IOException();
				}catch(IOException e){
						error = "Cannot make run file executable.";
				}
				//Vm.debug("Desktop: "+dtop);
				s = dtop.getOutputStream();
				if (s == null) error = "Cannot create Desktop entry!";
				else{
					sw = new StreamWriter(s); sw.useCR = false;
					sw.println("[Desktop Entry]");
					sw.println("Comment="+pl.getString("Comment",app.name));
					sw.println("Name="+app.name);
					sw.println("Exec="+base+"_Run");
					sw.println("Icon="+(icon == null ? "ewe" : base+"_Icon"));
					sw.println("Type=Application");
					sw.println("HidePrivilege=0");
					sw.close();
				}
				if (!showRestart) return true;
				new MessageBox("Restart Qtopia","Qtopia will now be restarted.",MessageBox.MBOK).execute(parent,Gui.CENTER_FRAME);
				mApp.mainApp.doSpecialOp(mApp.SPECIAL_RESTART_GUI,null);
				return true;
		}
		if (error != null){
			return changeEwePermisions();
			//new MessageBox("Error!",error,MessageBox.MBOK).execute(parent,Gui.CENTER_FRAME);
		}
		return false;
	}
	return true;
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
	try{
	boolean mobile = isMobile();
	CellPanel all = cp;
	enableEditorScrolling(ed,true);
	/*
	new CellPanel();
	ScrollBarPanel scp = new ScrollBarPanel(new ScrollableHolder(all));
	cp.addLast(scp);
	*/
	UIBuilder b = new UIBuilder(ed,this,all);
	ed.title = Gui.isSmartPhone ? "Ewe Options" : "Ewe VM/Laucher Options";
	ed.windowFlagsToClear |= Window.FLAG_HAS_CLOSE_BUTTON;
	boolean sp = ed.modifyForSmartPhone();
	InputStack is = null;
	//cp.setCell(cp.DONTSTRETCH);
	b.open().setControl(cp.HCONTRACT|cp.CENTER);
		if (true/*!sp*/){
			is = b.openInputStack();
			if (sp) is.doubleLined = true;
			is.setCell(cp.HSTRETCH);
				b.addAll("Ewe Dir:|pathToEwe");
				//if (mobile && !sp) b.addAll("Use Input Panel|useSIP");
				//b.add("createShortcut",new mButton("Place Ewe on Start Menu")).setControl(cp.DONTFILL);
			b.close(true);
		}
		//
		// True/False options.
		//
		is = b.openInputStack(sp ? null : "Launcher/Files");
			is.columns = sp ? 1 : 2;
			is.setCell(cp.HSTRETCH);
			if (!sp){
				//if (mobile)
				b.addAll("Use Input Panel|useSIP|Fixed SIP Button|fixedSIPButton");
				b.addAll("Single Window|singleWindowed|Taskbar Icon|showIcon|Dir. Tree|launcherTree|Exit After Launch|exitAfterLaunch");
			}else
				//b.addAll("Keep Vm Resident:|keepVmResident|Exit After Launch|exitAfterLaunch");
				;//b.addAll("Exit After Launch|exitAfterLaunch");
		b.close(true);

		if (!sp){
			b.open("System Font:").setCell(cp.HSTRETCH);//.setControl(cp.HCONTRACT|cp.CENTER);
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
	}catch(Exception e){
		e.printStackTrace();
	}
}


//===================================================================
public void win32_addToPanel(CellPanel cp,Editor ed,int options)
//===================================================================
{
	UIBuilder b = new UIBuilder(ed,this,cp);
	ed.title = "Ewe VM/Laucher Options";
	ed.windowFlagsToClear |= Window.FLAG_HAS_CLOSE_BUTTON;
	b.open().setControl(cp.HCONTRACT|cp.CENTER);
		InputStack is2 = b.openInputStack();
		is2.setCell(cp.HSTRETCH);
		b.addAll("Ewe Dir|pathToEwe");
		if (isMobile()) b.addAll("Use Input Panel|useSIP");
		//b.add("createShortcut",new mButton("Place Ewe on Start Menu")).setControl(cp.DONTFILL);
		//is.setControl(cp.HCONTRACT|cp.CENTER);
		b.close(true);
		InputStack is = b.openInputStack("Launcher/Files");
		is.columns = 2;
		//is.setControl(cp.HCONTRACT|cp.CENTER);
		is.setCell(cp.HSTRETCH);
		b.addAll("Taskbar Icon|showIcon|Dir. Tree|launcherTree|Exit After Launch|exitAfterLaunch");

		b.close(true);
		b.open("System Font").setCell(cp.HSTRETCH);//.setControl(cp.HCONTRACT|cp.CENTER);
			//b.endRow();
			//b.open();
		b.add("fontChooser",fontChooser.getEditor(0)).setCell(cp.HSTRETCH).setControl(cp.HCONTRACT|cp.LEFT);
		fontChooser.font = systemFontName;
		fontChooser.size = systemFontSize;
			//b.close();
		b.endRow();
			b.open();
				b.add(new mLabel("GUI Font:")).setCell(cp.DONTSTRETCH).setControl(cp.HCONTRACT|cp.LEFT);
				b.add("guiFontStyle",new mChoice(new String[]{"Plain","Bold","Italic","Bold & Italic"},0)).setControl(cp.HCONTRACT|cp.LEFT);
			b.close();
		b.close(true);
		b.openInputStack("Look and Feel").setCell(cp.HSTRETCH);//.setControl(cp.HCONTRACT|cp.CENTER);
			b.add("Style","style",new mChoice(new String[]{"3-D","Etched","Soft","Palm"},0));
		b.close(true);
		b.open().setCell(cp.HSTRETCH);//.setBorder(cp.EDGE_ETCHED,2);
			//b.open();
				b.add("save",new mButton("Save","ewe/savesmall.bmp",ewe.fx.Color.White)).setControl(cp.DONTFILL);
			//b.close();
		b.close();
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
		apply();
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
		return ewe.io.IO.getConfigInfo(this,"Ewesoft\\EweVM",IO.SAVE_IN_FILE);
	}catch(ewe.io.IOException e){
		return false;
	}
}
//===================================================================
public boolean save()
//===================================================================
{
	try{
		ewe.io.IO.saveConfigInfo(this,"Ewesoft\\EweVM",IO.SAVE_IN_FILE);
		return true;
	}catch(ewe.io.IOException e){
		return false;
	}
}
//===================================================================
public boolean apply()
//===================================================================
{
	Gui.setStyle(style);
	ewe.sys.Vm.setParameter(ewe.sys.Vm.SET_USE_SIP,useSIP ? 1 : 0);
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

static String [] utils = new String[]
{
	"EweSync Server","File System Viewer","Zip File Viewer","Text Editor","PNG Icon Maker"
};

static String [] apps = new String[]
{
	"ewe.io.RemoteConnection","ewe.filechooser.FileChooserDemo","ewe.zip.ZipFileBrowser",
	"ewe.ui.Notepad","ewe.graphics.PngIconMaker"
};

static String [] images = new String[]
{
	"ewesync","filechooserdemo","zipfilebrowser","notepad","pngiconmaker"
};

	//##################################################################
	public static class EweUtilityChooser extends ewe.data.LiveObject{
	//##################################################################

	public MultiListSelect.SingleListSelect utilities = new MultiListSelect.SingleListSelect(new ewe.util.Vector(utils),null);

	//===================================================================
	public void addToPanel(CellPanel cp,Editor ed,int which)
	//===================================================================
	{
		ed.title = "Select Ewe Utilities";
		cp.addLast(ed.addField(new ListSelect(false,"Available Utilities",false),"utilities"));
		cp.addLast(ed.addField(new mButton("Install"),"install")).setCell(ed.HSTRETCH).setControl(ed.DONTFILL);
	}

	//===================================================================
	public void action(String name,Editor ed)
	//===================================================================
	{
		int [] chosen = utilities.getSelectedIndexes();
		if (chosen.length == 0) return;
		for (int i = 0; i<chosen.length; i++){
			int idx = chosen[i];
			VMApp app = new VMApp();
			app.name = utils[idx];
			app.startClass = apps[idx];
			try{
				app.image = new ewe.fx.mImage("ewe/sys/"+images[idx]+".png");
				app.iconIsImage = true;
			}catch(Exception e){
			}
			if (!installEwe(app,ed.getFrame(),false,i == chosen.length-1)) break;
		}
		ed.exit(0);
	}
	//##################################################################
	}
	//##################################################################

//===================================================================
public static void doStartUp(final Frame parent)
//===================================================================
{
	if (!isQtopia()) return;
	VMOptions vo = mApp.vmOptions;
	if (!vo.firstRun) return;
	vo.firstRun = false;
	vo.save();
	File base = getQtopiaDirectory();
	//MessageBox mb = new MessageBox("Data",Vm.getProperty("user.name","<>")+"\n"+qpeDir(),MessageBox.MBOK);
	//mb.execute();
	if (!isMobile() || !base.isDirectory()) return;
	if (base.getChild("apps/Ewe/RemoteConnection.desktop").exists())
		return;
	new mThread(){
		public void run(){
			if (new MessageBox(
			"EweSync Installation",
			"The EweSync Server will now be installed.\nIt will be placed in a new Ewe application tab in the Paltmop environment.",
			MessageBox.MBOKCANCEL).execute() == MessageBox.IDCANCEL){
				new MessageBox("Installation Cancelled",
				"You can install the EweSync Server later using the \"Install Ewe Utility\" menu item.",
				MessageBox.MBOK).execute();
				return;
			}
			VMApp va = new VMApp();
			va.name = "EweSync Server";
			va.startClass = "ewe.io.RemoteConnection";
			try{
				va.image = new ewe.fx.mImage("ewe/sys/ewesync.png");
				va.iconIsImage = true;
			}catch(Exception e){
			}
			installEwe(va,parent,false,true);
		}
	}.start();
}
//##################################################################
}
//##################################################################

