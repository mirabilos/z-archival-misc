
package ewe.zip;
import ewe.sys.Time;
import ewe.io.*;
import ewe.util.*;
import ewe.filechooser.*;
import ewe.ui.*;
import ewe.sys.Handle;

//##################################################################
public class ZipFileBrowser implements Runnable{
//##################################################################

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
String checkArg(int which,String zipFile)
//===================================================================
{
	String arg = mApp.programArguments[which];
	if (arg.startsWith("-")){
		if (arg.equalsIgnoreCase("-images")) images = true;
	}else if (zipFile == null) zipFile = arg;
	return zipFile;
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
	fcd.title = "Zip File Viewer Options";
	fcd.chooserTitle = "Zip File Contents";
}
boolean isRunning = false;
//===================================================================
public void doRun()
//===================================================================
{
	String which = streamName;
	int arg = 0;
	for (arg = 0; arg<mApp.programArguments.length; arg++)
		which = checkArg(arg,which);

	if (which == null && streamName == null && inputStream == null){
		FileChooser fc = new FileChooser(FileChooser.OPEN,"/");
		fc.associateIcon("zip",new ewe.fx.mImage("ewe/zipsmall.bmp",ewe.fx.Color.White));
		fc.title = "Select a Zip File";
		fc.addMask("*.zip,*.jar - Zip and Jar Files");
		fc.addMask(fc.allFilesMask);
		if (fc.execute() == fc.IDCANCEL)
			return;
		which = fc.getChosen();
	}
	ProgressBarForm pbf = getProgress("Opening zip file...");
	ZipFile zf = null;
	try{
		if (inputStream instanceof RandomAccessStream)
			zf = new ZipFile((RandomAccessStream)inputStream,pbf.handle);
		else if (inputStream != null)
			zf = new ZipFile(ewe.io.MemoryFile.createFrom(inputStream,null),pbf.handle);
		else
			zf = new ZipFile(ewe.sys.Vm.openRandomAccessStream(which,ewe.io.RandomAccessFile.READ_ONLY),pbf.handle);

		if (streamName != null) zf.zipName = streamName;
	}catch(Exception e){
	}finally{
		pbf.close(0);
	}
	if (zf == null || !zf.isOpen()){
		if (!pbf.handle.shouldStop)
			new MessageBox("Error","That file cannot be read!",MessageBox.MBOK).execute();
		if (exitSystemOnClose) ewe.sys.Vm.exit(0);
		return;
	}
	pbf = getProgress("Creating file tree...");
	ZipEntryFile zef = null;
	try{
		zef = new ZipEntryFile(zf,pbf.handle);
		zef.simulateCanWrite = simulateCanWrite;
	}catch(Exception e){

	}finally{
		pbf.close(0);
	}
	if (zef == null || !zef.isValid()){
		if (!pbf.handle.shouldStop)
			new MessageBox("Error","The tree could not be created!",MessageBox.MBOK).execute();
		if (exitSystemOnClose) ewe.sys.Vm.exit(0);
		return;
	}
	boolean es = exitSystemOnClose;
	exitSystemOnClose = false;
	fcd.aFile = zef;
	fcd.doExec = doExec;
	isRunning = true;
	fcd.runDemo(es);
}
//===================================================================
public  ZipFileBrowser()
//===================================================================
{
}
/**
Create an EweFileBrowser that will automatically show itself.
**/
//===================================================================
public ZipFileBrowser(File f, boolean isModal, long maxSize)
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
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	ZipFileBrowser zb = new ZipFileBrowser();
	zb.exitSystemOnClose = true;
	zb.run();
	if (!zb.isRunning) ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

