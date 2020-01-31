/* java.util.ewe.EweEntry
   Copyright (C) 2001 Free Software Foundation, Inc.

This file is part of Jazzlib.

Jazzlib is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

Jazzlib is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

As a special exception, if you link this library with other files to
produce an executable, this library does not by itself cause the
resulting executable to be covered by the GNU General Public License.
This exception does not however invalidate any other reasons why the
executable file might be covered by the GNU General Public License. */

package ewe.io;
import ewe.sys.Time;
import ewe.util.*;
import ewe.filechooser.*;
import ewe.ui.*;
import ewe.sys.Handle;
//##################################################################
public class EweFileBrowser implements Runnable{
//##################################################################
/*
//-------------------------------------------------------------------
protected void setupMainWindow()
//-------------------------------------------------------------------
{
	windowFlagsToClear = FLAG_IS_VISIBLE;
	super.setupMainWindow();
}
*/
/*
{
title = "Ewe File Browser";
windowFlagsToClear = Window.FLAG_IS_VISIBLE;
exitSystemOnClose = true;
}
*/
public boolean exitSystemOnClose = false, doExec = true;
//===================================================================
ProgressBarForm getProgress(String doWhat)
//===================================================================
{
	Handle h = new Handle();
	ProgressBarForm pbf = new ProgressBarForm();
	pbf.showStop = true;
	pbf.showBar = false;
	pbf.setTask(h,doWhat);
	pbf.exec();
	return pbf;
}
boolean images = false;

public String streamName = null;
public Stream inputStream = null;
public boolean simulateCanWrite = false;

//===================================================================
String checkArg(int which,String image)
//===================================================================
{
	String arg = mApp.programArguments[which];
	if (arg.startsWith("-")){
		if (arg.equalsIgnoreCase("-images")) images = true;
	}else if (image == null) image = arg;
	return image;
}
/*
//===================================================================
public void formShown()
//===================================================================
{
	super.formShown();
	new ewe.sys.Coroutine(this,100);
}
*/
//===================================================================
public void run()
//===================================================================
{
	doRun();
	//ewe.sys.Coroutine.sleep(-1);
}

public FileChooserDemo fcd = new FileChooserDemo();

{
	fcd.initial = "/";
	fcd.asABrowser = fcd.browseOnly = true;
	fcd.title = "Ewe File Viewer Options";
	fcd.chooserTitle = "Ewe File Contents";
}

//===================================================================
public  EweFileBrowser()
//===================================================================
{
}
/**
Create an EweFileBrowser that will automatically show itself.
**/
//===================================================================
public EweFileBrowser(File f, boolean isModal, long maxSize)
throws IOException
//===================================================================
{
	if (maxSize > 0 && f.getLength() > maxSize) throw new IOException();
	try{
		inputStream = f.toRandomAccessStream("r");
	}catch(IOException e){
		inputStream = f.toReadableStream();
	}
	streamName = f.getFileExt();
	exitSystemOnClose = false;
	doExec = isModal;
	new ewe.sys.Coroutine(this);
}
//===================================================================
public void doRun()
//===================================================================
{
	String which = streamName;
	int arg = 0;
	for (arg = 0; arg<mApp.programArguments.length; arg++)
		which = checkArg(arg,which);

	if (which == null && streamName == null && inputStream == null){
		FileChooser fc = new FileChooser(FileChooser.OPEN,"./");
		fc.associateIcon("ewe",new ewe.fx.mImage("ewe/ewesmall.bmp",ewe.fx.Color.White));
		fc.title = "Select a Ewe File";
		fc.addMask("*.ewe - Ewe Files");
		fc.addMask(fc.allFilesMask);
		if (fc.execute() == fc.IDCANCEL) return;
		which = fc.getChosen();
	}
	ProgressBarForm pbf = getProgress("Opening ewe file...");
	EweFile zf = null;
	if (inputStream instanceof RandomAccessStream)
		zf = new EweFile((RandomAccessStream)inputStream,pbf.handle);
	else if (inputStream != null)
		zf = new EweFile(ewe.io.MemoryFile.createFrom(inputStream,null),pbf.handle);
	else
		zf = new EweFile(ewe.sys.Vm.openRandomAccessStream(which,ewe.io.RandomAccessFile.READ_ONLY),pbf.handle);

	if (streamName != null) zf.eweName = streamName;
	pbf.close(0);
	if (!zf.isOpen()){
		if (!pbf.handle.shouldStop)
			new MessageBox("Error","That file cannot be read!",MessageBox.MBOK).execute();
		return;
	}
	pbf = getProgress("Creating file tree...");
	EweFile.EweEntryFile zef = zf.getEweEntryFile(pbf.handle);
	zef.simulateCanWrite = simulateCanWrite;
	pbf.close(0);
	if (!zef.isValid()){
		if (!pbf.handle.shouldStop)
			new MessageBox("Error","The tree could not be created!",MessageBox.MBOK).execute();
		return;
	}
	boolean es = exitSystemOnClose;
	exitSystemOnClose = false;
	fcd.aFile = zef;
	fcd.doExec = doExec;
	fcd.runDemo(es);
/*
	FileChooser fc =
		images ?
		new ImageFileChooser(FileChooser.OPEN|FileChooser.DIRECTORY_TREE,"/",zef):
		new FileChooser(FileChooser.OPEN|FileChooser.DIRECTORY_TREE,"/",zef);
	fc.title = "Contents of: "+f.getFileExt();
	fc.addMask("*.* - All Files");
	fc.execute();
	exit(0);
*/
}
//##################################################################
}
//##################################################################

