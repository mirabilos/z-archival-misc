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
package ewe.io;
import ewe.ui.*;
import ewe.sys.*;
import ewe.util.Vector;
/**
* This class will copy multiple source files to multiple destination
* files.
**/
//##################################################################
public class CopyOver extends TaskObject{
//##################################################################

public String displayTitle = "Copying Files";
public File sourceDirectory;
public File destDirectory;
public String [] sourceFiles;
Streamable [] sourceStreams;

public boolean shouldStop = false;
public boolean doMove = false;
public boolean yesToAll = false;
public boolean noToAll = false;
public boolean alwaysContinue = false;
public boolean alwaysStop = false;
public boolean makeCopies = false;
public boolean dontCopySubdirectoryContents = false;
public boolean doCopySubdirectoryContents = false;
public Frame parentFrame;

public static final int IDALWAYS_YES = 1000;
public static final int IDALWAYS_NO = 1001;
public static final int IDALWAYS_CONTINUE = 1000;
protected String [] destFiles;
//===================================================================
public static int yesNo(Frame parentFrame,final String theTitle,final String theMessage)
//===================================================================
{
	MessageBox mb = new MessageBox(){
		Control yesToAll,noToAll;
		{
			this.title = theTitle;
			messageText = theMessage;
			addButton(yes = new mButton("Yes").setHotKey(0,'y'));
			addButton(yesToAll = new mButton("Yes For All").setHotKey(0,'a'));
			addButton(no = new mButton("No").setHotKey(0,'n'));
			addButton(noToAll = new mButton("No For All").setHotKey(0,'l'));
			addButton(cancel = new mButton("Cancel").setHotKey(0,'c'));
			if (!Gui.screenIs(Gui.WIDE_SCREEN)) buttonsPerRow = 2;
		}
		public void onControlEvent(ControlEvent ev){
			if (ev.type == ev.PRESSED){
				if (ev.target == yesToAll) exit(IDALWAYS_YES);
				else if (ev.target == noToAll) exit(IDALWAYS_NO);
				else
					super.onControlEvent(ev);
			}else
				super.onControlEvent(ev);
		}
	};
	return mb.execute(parentFrame,Gui.CENTER_FRAME);
}
//===================================================================
public static int continueOp(Frame parentFrame,final String op,final String file)
//===================================================================
{
	MessageBox mb = new MessageBox(){
		Control always;
		{
			title = "File Error";
			addButton(ok = new mButton("Yes").setHotKey(0,'y'));
			addButton(always = new mButton("Always continue").setHotKey(0,'a'));
			addButton(cancel = new mButton("Cancel").setHotKey(0,'c'));
			if (file != null)
				messageText = file+"\nThis file could not be "+op+".";
			else
				messageText = "The "+op+" operation could not be done.";
			messageText += "\nDo you want to continue?";
		}
		public void onControlEvent(ControlEvent ev){
			if (ev.type == ev.PRESSED && ev.target == always)
				exit(IDALWAYS_CONTINUE);
			else
				super.onControlEvent(ev);
		}
	};
	return mb.execute(parentFrame,Gui.CENTER_FRAME);
}
//===================================================================
public static String getFileInfo(File f)
//===================================================================
{
		Time t = new Time();
		f.getModified(t);
		ewe.sys.Locale l = ewe.sys.Vm.getLocale();
		return f.getFileExt()+"\n"+l.format(l.FORMAT_PARSE_NUMBER,ewe.sys.Long.l1.set(f.getLength()),",")+" bytes\n"+t+" "+t.toString(t,l.getString(l.TIME_FORMAT,0,0),l);
}

//===================================================================
protected void error(String err)
//===================================================================
{
	//ewe.sys.Vm.debug(err);
	handle.error = err;
	handle.set(handle.Failed);
}
CopyOverProgress cop;
Handle curFileHandle;

//-------------------------------------------------------------------
boolean checkContinue(String op,String file)
//-------------------------------------------------------------------
{
	if (alwaysStop){
		shouldStop = true;
		handle.set(Handle.Failed);
		return false;
	}
	if (alwaysContinue) return true;
	int co = continueOp(parentFrame,op,file);
	if (co == Form.IDCANCEL) {
		shouldStop = true;
		handle.set(Handle.Aborted|Handle.Stopped);
		return false;
	}else if (co == IDALWAYS_CONTINUE)
		alwaysContinue = true;
	return true;
}
//-------------------------------------------------------------------
int yesNo(String title,String message)
//-------------------------------------------------------------------
{
	int ret = Form.IDCANCEL;
	if (yesToAll) ret = IDALWAYS_YES;
	else if (noToAll) ret = IDALWAYS_NO;
	else if (alwaysStop) ret = Form.IDCANCEL;
	else ret = yesNo(parentFrame,title,message);
	switch(ret){
		case IDALWAYS_YES:
			yesToAll = true;
		case Form.IDYES:
			return Form.IDYES;
		case IDALWAYS_NO:
			noToAll = true;
		case Form.IDNO:
			return Form.IDNO;
		case Form.IDCANCEL:
			shouldStop = true;
			handle.set(Handle.Aborted|Handle.Stopped);
			return Form.IDCANCEL;
		default:
			return ret;
	}
}
//===================================================================
public static boolean getFiles(File f,String name,ewe.util.Vector addTo,Handle handle)
//===================================================================
{
	addTo.add(name);
	if (!f.isDirectory()) return true;
	if (handle != null)
		if (handle.shouldStop) return false;
	String [] all = f.list();
	if (all == null) all = new String[0];
	for (int i = 0; i<all.length; i++)
		if (!getFiles(f.getChild(all[i]),name+"/"+all[i],addTo,handle)) return false;
	if (handle != null) Coroutine.sleep(0);
	return true;
}

//===================================================================
boolean adjustFileList()
//===================================================================
{
	if (sourceFiles == null){
		if (sourceStreams != null){
			sourceFiles = new String[sourceStreams.length];
			for (int i = 0; i<sourceStreams.length; i++)
				sourceFiles[i] = sourceStreams[i].getName();
		}
		return true;
	}
	if (dontCopySubdirectoryContents || doMove) return true;
	File check = sourceDirectory.getNew("");
	Vector v = new Vector();
	for (int i = 0; i<sourceFiles.length; i++){
		check.set(sourceDirectory,sourceFiles[i]);
		if (check.isDirectory()){
			if (!doCopySubdirectoryContents){
				int ret = new MessageBox("Copy Folder Contents?",
				"Do you want to copy the contents\nof selected folders?",Form.MBYESNOCANCEL).execute(parentFrame,Gui.CENTER_FRAME);
				if (ret == Form.IDNO) return true;
				else if (ret == Form.IDCANCEL) return false;

				doCopySubdirectoryContents = true;
			}
			if (!getFiles(check,sourceFiles[i],v,handle)) return false;
		}else
			v.add(sourceFiles[i]);
	}
	sourceFiles = new String[v.size()];
	v.copyInto(sourceFiles);
	return true;
}
//===================================================================
public void doRun()
//===================================================================
{
		handle.doing = "Getting File List";
		handle.changed();
		Coroutine.sleep(0);
		int totalBytes = 0;
		int copied = 0;

		if (!adjustFileList()) {
			handle.set(handle.Aborted|handle.Stopped);
			return;
		}

		handle.doing = "Copy/Move Progress";
		handle.changed();
		Coroutine.sleep(0);

		for (int i = 0; i<sourceFiles.length && !shouldStop; i++){
			File src = sourceDirectory.getChild(sourceFiles[i]);
			totalBytes += src.getLength();
		}
		if (totalBytes == 0) totalBytes = 1;
		int lastCopied = 0;

		if (sourceDirectory.equals(destDirectory)){
			if (!makeCopies) {
				handle.error = "Copying files into the same directory.";
				handle.set(handle.Failed);
				return;
			}
			destFiles = new String[sourceFiles.length];
			doMove = false;
			for (int i = 0; i<sourceFiles.length && !shouldStop; i++){
				String df = sourceFiles[i];
				String nf = df;
				for (int j= 1;j>0;j++){
					nf = j == 1 ? "CopyOf"+df : "Copy"+j+"Of"+df;
					if (!destDirectory.getChild(nf).exists() || destDirectory.getChild(nf).isDirectory()) break;
				}
				destFiles[i] = nf;
			}
		}
		for (int i = 0; i<sourceFiles.length && !shouldStop; i++){
			copied += lastCopied;
			handle.changed();
			sleep(0);
			File src = sourceDirectory.getChild(sourceFiles[i]);
			File dest = destDirectory.getChild(destFiles == null ? sourceFiles[i] : destFiles[i]);
			File parent = dest.getParentFile();
			lastCopied = src.getLength();

			if (parent != null)
				if (!parent.mkdirs()){
					error("Could not create destination directory: "+parent.getFullPath());
					return;
				}

				if (dest.exists() && !dest.isDirectory()){
					int ny = yesNo("Overwrite file?","Overwrite the file:\n"+getFileInfo(dest)+"\nwith:\n"+getFileInfo(src));
					if (ny == Form.IDCANCEL) break;
					else if (ny == Form.IDNO) continue;
					if (!dest.delete())
						if (!checkContinue("overwritten",sourceFiles[i])) break;
						else continue;
				}

				boolean doCopy = !doMove;

				if (doMove)
					if (!src.move(dest))
						doCopy = true;

				if (doCopy){
					if (src.isDirectory()){
						if (dest.isDirectory()) continue;
						else if (dest.exists()) {
							if (!checkContinue("overwritten",sourceFiles[i])) break;
							else continue;
						}else if (!dest.createDir()){
							if (!checkContinue("created",sourceFiles[i])) break;
							else continue;
						}
						continue;
					}
					Stream in = src.getInputStream();
					if (in == null){
						if (!checkContinue("read",sourceFiles[i])) break;
						else continue;
					}
					Stream out = dest.getOutputStream();
					if (out == null){
						in.close();
						if (!checkContinue("written",sourceFiles[i])) break;
						else continue;
					}
					IOTransfer tx = new IOTransfer(in,out);

					//tx.sleepTime = 100;
					//tx.bufferSize = 256;
					tx.totalToCopy = src.getLength();
					Handle h = curFileHandle = tx.getHandle();//startTask();
					h.doing = dest.getFileExt();
					//ewe.sys.Vm.debug("Copying: "+h.doing);
					if (cop != null) cop.fileProgress.setTask(h,"Copying");
					else h.start();
					//ewe.sys.Vm.debug("Copying: "+src.getName()+" to "+dest.getFullPath());
					while(true){
						h.waitOnFlags(h.Changed,TimeOut.Forever);
						handle.progress = (float)(copied+tx.copied)/totalBytes;
						handle.changed();
						int val = h.check();
						if ((val & handle.Stopped) == 0) continue;
						in.close();
						out.close();
						//ewe.sys.Vm.debug("Closing output!");
						if ((val & handle.Aborted) != 0){
							handle.set(handle.Stopped|handle.Aborted);
							return;
						}else
						break;
					}
					if ((h.check() & handle.Success) == 0)
						if (!checkContinue("copied",sourceFiles[i])) break;
						else continue;

					Time was = src.getModified(null);
					if (was != null) dest.setModified(was);

					if (doMove)
						if (!src.delete())
							if (!checkContinue("removed",sourceFiles[i])) break;
							else continue;
				}
		}// End for()
		copied += lastCopied;
		if (shouldStop) {
			if ((handle.check() & handle.Stopped) == 0) handle.set(handle.Stopped|handle.Aborted);
		}else handle.set(handle.Succeeded);
		handle.changed();
		//ewe.sys.Vm.debug("Done");
}

//-------------------------------------------------------------------
void stopCopying()
//-------------------------------------------------------------------
{
	handle.stop(0);
	if (curFileHandle != null) curFileHandle.stop(0);
}
Form progress;
//===================================================================
public Handle runWithProgressDisplay()
//===================================================================
{
	cop = new CopyOverProgress(this);
	progress = cop;
	cop.title = displayTitle;
	cop.exec(parentFrame,null,Gui.CENTER_FRAME);
	cop.overallProgress.setTask(handle,"Copying");
	return handle;
}
//===================================================================
public boolean execute(Frame parent,StringBuffer error)
//===================================================================
{
	parentFrame = parent;
	if (error == null) error = new StringBuffer();
 	error.setLength(0);
	Handle h = runWithProgressDisplay();
	if (!h.waitOnFlags(Handle.Success,TimeOut.Forever)){
		error.append(h.error);
		progress.waitUntilClosed();
		return false;
	}
	progress.waitUntilClosed();
	return true;
}

//##################################################################
}
//##################################################################


