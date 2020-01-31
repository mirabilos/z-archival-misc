/*
Note - This is the Linux version of VMApp.java
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
import ewex.registry.*;
import ewe.fx.*;
import ewe.io.*;
//##################################################################
public class VMApp extends ewe.data.LiveObject{
//##################################################################

public String name = "";
public String args = "";
public String target = "";
public String programDir = "";
public String category = "Ewe";
public boolean copyOver = false;
public boolean execWithEwe = true;
public IImage image;
public String location = "";
public int install;
public String startClass = "";
public String vmArgs = "";
public ImageBytes imageBytes;
public boolean iconIsImage;

PropertyList all = null;
public String _fields = "startClass,name,args,target,programDir,execWithEwe,install,vmArgs,imageBytes,iconIsImage";

//-------------------------------------------------------------------
protected TextEncoder encode(TextEncoder te)
//-------------------------------------------------------------------
{
	if (image != null)
		imageBytes = new ImageBytes(image);
	else
		imageBytes = null;
	encodeFields(_fields,te,"VMApp");
	return super.encode(te);
}
//-------------------------------------------------------------------
protected TextDecoder decode(TextDecoder te)
//-------------------------------------------------------------------
{
	image = null;
	decodeFields(_fields,te,"VMApp");
	if (imageBytes != null){
		try{
			image = imageBytes.pngDecode();
		}catch(Exception e){
		}
	}else{
		//ewe.sys.Vm.debug("No image bytes found!");
	}
	/** Don't load - except when first using it - or possibly when installing.
	if (true)
		ewe.sys.Vm.debug("Not doing Load!");
	else
		load();
	**/
	return super.decode(te);
}
public void copyFrom(Object other)
{
	if (other instanceof VMApp)
		all = ((VMApp)other).all;
	super.copyFrom(other);
}
public String baseName()
{
	String ret = null;
	if (all != null)
		if (install < all.size()){
			ret = ((Property)all.get(install)).name;
			if (ret.equals("default")) ret = null;
		}
	if (ret == null && target.toLowerCase().endsWith(".ewe")){
		ret = new File(target).getName();
		ret = ret.substring(0,ret.length()-4);
	}
	if (ret == null){
		String start = target;
		if (start == null || start.length() == 0)
			start = startClass;
		ret = ewe.util.mString.rightOf(start,'.');
		if (ret.length() == 0) ret = start;
	}
	if (ret != null)
		ret = ret.replace(' ','_');
	return ret;
}
public PropertyList chosenProperties()
{
	if (all == null) return null;
	if (all.size() == 0) return null;
	if (install >= all.size()) install = 0;
	return (PropertyList) ((Property)all.get(install)).value;
}
/**
An option for getEditor().
**/
public static final int ZAURUS_INSTALL = 1;
public static final int ADD_NEW = 2;
//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int options)
//===================================================================
{
	ed.title = "Ewe Application";
	UIBuilder b = new UIBuilder(ed,this,cp);
	if (options == ADD_NEW && Gui.isSmartPhone) {
		CellPanel cpp = b.open();
			cpp.addLast(ed.addField(new mButton("Select Ewe/Class File"),"select"));
		b.close(true).setCell(cp.HSTRETCH);
	}
	InputStack is = b.openInputStack();
	is.doubleLined = Gui.isSmartPhone;
	is.inputLength = 30;
	int len = all == null ? 1 : all.size();
	if (len > 1){
		String [] ch = new String[len];
		for (int i = 0; i<len; i++) ch[i] = ((Property)all.get(i)).name;
		b.add("Install:","install",new mChoice(ch,0));
	}
	if (options == 0 || options == ADD_NEW){
			is.inputLength = 30;
			b.addAll("Name:|name|Run Class:|startClass|Arguments:|args|VM Options:|vmArgs");
		b.close();
		if (options == ADD_NEW && !Gui.isSmartPhone) ed.addButton(ed.addField(new mButton("Select Ewe/Class File"),"select"));
	}else if (options == ZAURUS_INSTALL){
			b.addAll("Name:|name|Run Class:|startClass|Arguments:|args|VM Options:|vmArgs|Location:|location|Copy to Location:|copyOver");
			b.add("Category:","category",new mChoice(mString.split("Applications|Ewe|Games|Settings|Jeode"),0));
		b.close();
	}
}

//===================================================================
public void fieldChanged(String field,Editor ed)
//===================================================================
{
	if (field.equals("install")){
		if (selectInstall(install)) ed.toControls();
	}
	super.fieldChanged(field,ed);
}

//===================================================================
public void action(String field,Editor ed)
//===================================================================
{
	if (field.equals("select")){
		ed.exit(100);
	}else
		super.action(field,ed);
}
//===================================================================
public boolean selectInstall(int index)
//===================================================================
{
	String home = Vm.getProperty("HOME",null);
	if (home == null) home = "/home/"+Vm.getProperty("user.name","root");
	Property p = (Property)all.get(index);
	if (p == null) return false;
	PropertyList pl = (PropertyList)p.value;
	File f = new File(target);
	String fn = f.getName();
	fn = fn.substring(0,fn.length()-4);
	name = pl.getString("Name",fn);
	location = pl.getString("Location",home+"/"+fn);
	copyOver = (location.length() != 0);
	category = pl.getString("Category","Applications");
	startClass = pl.getString("Class","");
	vmArgs = pl.getString("VMOptions","");
	args = pl.getString("Arguments","");
	return true;
}
//===================================================================
public boolean makeFrom(String target,Frame parent)
//===================================================================
{
	copyOver = true;
	if (target.endsWith(".class")){
		//String path = target.substring(0,target.length()-6);
		StringBuffer programDir = new StringBuffer();
		String cls = ewe.ui.UIBuilder.askClassName(target,programDir,parent);
		if (cls == null) return false;
		this.programDir = programDir.toString();
		//this.target = cls;
		this.startClass = cls;
		return true;
	}else if (target.endsWith(".ewe")){
		try{
			ProgressBarForm.display(null,"Reading Ewe file...",parent);
			this.target = target;
			all = load();
			if (all != null){
				selectInstall(0);
			}
		}finally{
			ProgressBarForm.clear();
		}
		return true;
	}else
		return false;
}

//===================================================================
public boolean run()
//===================================================================
{
	String a = target;
	if (target.endsWith(".ewe")) a = "\""+a+"\"";
	if (startClass.length() != 0) a = startClass+" "+a;
	a += " "+args;
	if (vmArgs != null) a = vmArgs+" "+a;
	if (programDir.length() != 0)
		a = "/d \""+programDir+"\" "+a;
	boolean ret = Vm.runEweVM(a);
	if (VMOptions.getVMOptions().exitAfterLaunch) Vm.exit(0);
	return ret;
}

//===================================================================
public ewe.data.PropertyList getInstallProperties(RandomAccessStream stream)
//===================================================================
{
		RandomAccessStream s = stream;
		if (s == null) s = new RandomAccessFile(new File(target),RandomAccessFile.READ_ONLY);
		ewe.data.PropertyList pl = new ewe.data.PropertyList();
		try{
			Stream in = EweFile.getInputStream(s,"_install_.txt",false);
			pl.readConfigFile(new StreamReader(in));
			in.close();
			return pl;
		}catch(Exception e){
			pl = new PropertyList();
			PropertyList pl2 = new PropertyList();
			pl.set("default",pl2);
			return pl;
		}finally{
			if (stream == null) s.close();
		}
}
/**
* Load up the icon for the shortcut.
**/
//===================================================================
public ewe.data.PropertyList load()
//===================================================================
{
	if (target.endsWith(".ewe")){
		RandomAccessStream s = new RandomAccessFile(new File(target),RandomAccessFile.READ_ONLY);
		try{
			all = getInstallProperties(s);
			if (install > all.size()) install = 0;
			PropertyList pl = (PropertyList)((Property)all.get(install)).value;
			String icon = pl.getString("Icon",null);
			if (icon != null)
				image = EweFile.getImageAndMask(s,icon);
			return all;
		}catch(Exception e){
			return null;
		}finally{
			s.close();
		}
	}else {
	}
		return null;
}
//##################################################################
}
//##################################################################

