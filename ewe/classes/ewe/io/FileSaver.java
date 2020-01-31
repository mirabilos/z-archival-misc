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
//import ewe.filechooser.*;
import ewe.util.Vector;
import ewe.data.LiveData;
//##################################################################
public class FileSaver extends ewe.util.Errorable implements EventListener{
//##################################################################
/**
* This is the file model to use. By default it is a normal local file.
**/
public File fileModel = ewe.sys.Vm.newFileObject();
/**
* This is the last file that was saved. If it is null then the FileChooser
* box will be brought up on saving.
**/
public String lastSaved = null;
protected String defaultFileName = "";
/**
* This is the default extension for the file name. It can be null for
* no extension.
**/
public String defaultExtension = null;
/**
* This is the title of the file save box. By default it is "Save file as...";
**/
public String saveTitle = "Save file as...";
/**
* This is the title of the file open box. By default it is "Open file...";
**/
public String openTitle = "Open file...";
/**
* This is a list of masks to be sent to the FileChooser. You can explicitly put
* masks in here or you can use the setDefaultFileType() method.
**/
public Vector masks = new Vector();
/**
* This is set to any exception that may have occured during loading/saving.
**/
public Exception exception;
/**
* This is assigned to the persistentHistoryKey of the FileChooser created.
**/
public String persistentHistoryKey;
/**
* Set this true to always ignore the parent Frame and have all new Frames appear in
* their own window.
**/
public boolean ignoreParentFrame = true;
/**
* You can set this to a textCodec to use for reading and writing text data using this FileSaver.
**/
public TextCodec textCodec;
/**
* This applies for saving text. If this is true then an ending LineFeed will not be appended
* to the saved text.
**/
public boolean dontAppendLineFeed = false;
//-------------------------------------------------------------------
private Frame toParent(Frame parent)
//-------------------------------------------------------------------
{
	return ignoreParentFrame ? null : parent;
}
//===================================================================
public FileSaver(){this("");}
//===================================================================
public FileSaver(String defaultFileName) {this(defaultFileName,null);}
//===================================================================
public FileSaver(String defaultFileName,String actualFileName)
//===================================================================
{
	this.defaultFileName = defaultFileName;
	lastSaved = actualFileName;
	masks.add("*.* - All Files.");//FileChooser.allFilesMask);
}
/**
* This will sets the default file type and adds the mask to the list of masks. By default, "*.* - All files." is initially
* added to the masks. Do NOT put a '.' before the extension.
**/
//===================================================================
public void setDefaultFileType(String extension,String description)
//===================================================================
{
	String msk = "*."+extension;
	if (description != null) msk += " - "+description;
	masks.add(0,msk);
	defaultFileName = "unnamed."+extension;
	defaultExtension = extension;
}
/**
* This returns a String which is the file name of the destination to save to. If lastSaved is null (i.e. no save
* has been done yet, it will call saveAs(parent).
**/
//==================================================================
public String save(Frame parent)
//==================================================================
{
	if (lastSaved == null) return saveAs(parent);
	else return lastSaved;
}
//===================================================================
public String getInitialFile()
//===================================================================
{
	if (lastSaved != null){
		File p = fileModel.getNew(lastSaved).getParentFile();
		return fileModel.getNew(p,defaultFileName).getFullPath();
	}
	return defaultFileName;
}
//-------------------------------------------------------------------
protected FileChooserParameters getFileChooser(String type,String initial,File model)
//-------------------------------------------------------------------
{
	FileChooserParameters fcp = new FileChooserParameters();
	fcp.set(fcp.TYPE,type);
	fcp.set(fcp.START_LOCATION,initial);
	fcp.set(fcp.FILE_MODEL,model);
	fcp.set(fcp.DEFAULT_EXTENSION,defaultExtension);
	for (int i = 0; i<masks.size(); i++)
		fcp.add(fcp.FILE_MASK,masks.get(i).toString().trim());
	fcp.set(fcp.PERSISTENT_HISTORY,persistentHistoryKey);
	return fcp;
}
/**
* This brings up the FileChooser
* box and lets the user select a file to save to. If the user cancels this will return null, otherwise it will
* return the file name chosen by the user.
**/
//===================================================================
public String saveAs(Frame parent)
//===================================================================
{
	FileChooserParameters fcp = getFileChooser(FileChooserParameters.TYPE_SAVE,getInitialFile(),fileModel);
	fcp.set(fcp.TITLE,saveTitle);
	if (!fileModel.executeFileChooser(fcp)) return null;
	lastSaved = fcp.getValue(fcp.CHOSEN_FILE,null).toString();
	return lastSaved;
}
/*
//-------------------------------------------------------------------
protected FileChooser getFileChooser(int options,String s,File model)
//-------------------------------------------------------------------
{
	FileChooser fc = new FileChooser(options,s,model);
	fc.persistentHistoryKey = persistentHistoryKey;
	return fc;
}
*/
/**
* This brings up the FileChooser
* box and lets the user select a file to save to. If the user cancels this will return null, otherwise it will
* return the file name chosen by the user.
**/
/*
//===================================================================
public String saveAs(Frame parent)
//===================================================================
{
	FileChooser fc = getFileChooser(FileChooser.SAVE,getInitialFile(),fileModel);
	fc.title = saveTitle;
	fc.defaultExtension = defaultExtension;
	for (int i = 0; i<masks.size(); i++)
		fc.addMask(masks.get(i).toString().trim());
	fc.persistentHistoryKey = persistentHistoryKey;
	if (fc.execute() == fc.IDCANCEL)
		return null;
	lastSaved = fc.getChosen();
	return lastSaved;
}
*/
/**
* This checks if the specified file is readable. If it is it will return
* the file. If it is not it will call open(parent) and return the file
* selected by the user.
**/
//===================================================================
public String tryOpen(String file,Frame parent)
//===================================================================
{
	File f = fileModel.getNew(file);//ewe.sys.Vm.newFileObject().getNew(file);
	if (!f.canRead()) return open(parent);
	lastSaved = file;
	return file;
}
//===================================================================
public boolean tryOpen(Object data,String file,Frame parent)
//===================================================================
{
	String name = tryOpen(file,parent);
	if (name == null) return false;
	return doOpen(data,file,parent);
}
/**
* This brings up a FileChooser box to let the user select a file to open. If the
* user cancels it will return null.
**/
//===================================================================
public String open(Frame parent)
//===================================================================
{
	FileChooserParameters fcp = getFileChooser(FileChooserParameters.TYPE_OPEN,getInitialFile(),fileModel);
	fcp.set(fcp.TITLE,openTitle);
	if (!fileModel.executeFileChooser(fcp)) return null;
	lastSaved = fcp.getValue(fcp.CHOSEN_FILE,null).toString();
	return lastSaved;
}
/**
* This brings up a FileChooser box to let the user select a file to open. If the
* user cancels it will return null.
**/
/*
//===================================================================
public String open(Frame parent)
//===================================================================
{
	FileChooser fc = getFileChooser(FileChooser.OPEN,getInitialFile(),fileModel);
	fc.title = openTitle;
	fc.defaultExtension = defaultExtension;
	for (int i = 0; i<masks.size(); i++)
		fc.addMask(masks.get(i).toString().trim());
	if (fc.execute() == fc.IDCANCEL)
		return null;
	return lastSaved = fc.getChosen();
}
*/
/**
* This sets hasChanged to false and lastSaved to newFileName.
**/
//===================================================================
public void newData(String newFileName)
//===================================================================
{
	hasChanged = false;
	lastSaved = newFileName;
}
/**
* If hasChanged is true, this will attempt to save the current object by prompting
* the user to confirm saving changes.<p>
* This method return true if:<p>
* <nl>
* <li>hasChanged is false - meaning no save is necessary.</li>
* <li>hasChanged is true and the user says "no" to saving the current data.</li>
* <li>hasChanged is true and the user says "yes" to saving the current data, and the data is successfully saved.</li>
* </nl>
* If the user says "cancel" or a save fails the method will return false.
**/
//===================================================================
public boolean checkSave(Object data,Frame parent)
//===================================================================
{
	if (!hasChanged || data == null) return true;
	int ret = new MessageBox("Save Changes?","Do you wish to save changes?",MessageBox.MBYESNOCANCEL).execute(toParent(parent),Gui.CENTER_FRAME);
	if (ret == Form.IDCANCEL) return false;
	if (ret == Form.IDNO) return true;
	return save(false,data,parent);
}
/**
* This says whether the current file has been changed.
**/
public boolean hasChanged;
/**
* If this is true then any IO errors encountered during doSave() or doOpen()
* is reported.
**/
public boolean reportIOErrors = true;

//-------------------------------------------------------------------
protected boolean returnError(String error,Frame parent)
//-------------------------------------------------------------------
{
	if (reportIOErrors)
		new MessageBox("Error",error,MessageBox.MBOK).execute(toParent(parent),Gui.CENTER_FRAME);
	return super.returnError(error,false);
}
/**
 * This creates a Stream to write to the destination file name.
 * @param fileName The file name to write to.
 * @param parent The parent frame (may be null).
 * @return A Stream for writing to the destination file.
 * @exception IOException if no Stream to the file could be created. If reportIOErrors is true, then
 * the exception will be displayed in a standard exception dialog.
 */
//-------------------------------------------------------------------
protected Stream getOutputStream(String fileName,Frame parent) throws IOException
//-------------------------------------------------------------------
{
	try{
		return fileModel.getNew(fileName).toWritableStream(false);
	}catch(IOException e){
		exception = e;
		if (reportIOErrors) new ReportException(e,null,null,false).execute(toParent(parent),Gui.CENTER_FRAME);
		throw e;
	}
}
/**
 * This creates a Stream to read from to the source file name.
 * @param fileName The file name to read from.
 * @param parent The parent frame (may be null).
 * @return A Stream for writing to the destination file.
 * @exception IOException if no Stream to the file could be created. If reportIOErrors is true, then
 * the exception will be displayed in a standard exception dialog.
 */
//-------------------------------------------------------------------
protected Stream getInputStream(String fileName,Frame parent) throws IOException
//-------------------------------------------------------------------
{
	try{
		return fileModel.getNew(fileName).toReadableStream();
	}catch(IOException ex){
		exception = ex;
		if (reportIOErrors) new ReportException(ex,null,null,false).execute(toParent(parent),Gui.CENTER_FRAME);
		throw ex;
	}
}

//-------------------------------------------------------------------
protected boolean closeAndReturn(String fileName,Frame parent,Stream s,boolean success)
//-------------------------------------------------------------------
{
	s.close();
	if (success) {
		lastSaved = fileName;
		hasChanged = false;
		return true;
	}
	return returnError("Could not write to output file:\n"+fileName,parent);
}
/**
* This attempts a save operation on the data - and you should override this
* as necessary. It attempts to convert the data to a string and then saves it.
* Any error in saving is reported.
**/
//===================================================================
public boolean doSave(Object toSave,String fileName,Frame parent)
//===================================================================
{
	exception = null;
	try{
		if (toSave instanceof ewe.data.SaveableObject){
			((ewe.data.SaveableObject)toSave).saveObject(fileModel.getNew(fileName));
		}else{
			TextWriter pw = new TextWriter(fileModel.getNew(fileName).toWritableStream(false));
			if (textCodec != null) pw.codec = (TextCodec)textCodec.getCopy();
			try{
				String out = null;
				if (toSave instanceof String) out = (String)toSave;
				else if (toSave instanceof LiveData)
					out = ((LiveData)toSave).textEncode();
				else
					out = ewe.util.TextEncoder.toString(toSave);
				if (out == null) throw new IOException("Could not save.");
				pw.print(out);
				if (!dontAppendLineFeed) pw.println();
			}finally{
				pw.close();
			}
		}
		lastSaved = fileName;
		hasChanged = false;
		return true;
	}catch(Exception ex){
		exception = ex;
		new ReportException(ex,null,null,false).execute(toParent(parent),Gui.CENTER_FRAME);
		return false;
	}
}
/**
* This attempts an open operation on the data - and you should override this
* as necessary. It attempts to read a line of text from the file and
* then call textDecode() on the object.
**/
//===================================================================
public boolean doOpen(Object toOpen,String fileName,Frame parent)
//===================================================================
{
	exception = null;
	try{
		if (toOpen instanceof ewe.data.SaveableObject){
			((ewe.data.SaveableObject)toOpen).openObject(fileModel.getNew(fileName));
		}else if (toOpen instanceof LiveData){
			TextReader br = new TextReader(fileModel.getNew(fileName).toReadableStream());
			if (textCodec != null) br.codec = (TextCodec)textCodec.getCopy();
			String got = br.readLine();
			br.close();
			if (got == null)
				throw new IOException("Could not read from input file: "+fileName);
			((LiveData)toOpen).textDecode(got);
		}else
			throw new IOException("Cannot open that object!");
		lastSaved = fileName;
		hasChanged = false;
		return true;
	}catch(Exception ex){
		exception = ex;
		new ReportException(ex,null,null,false).execute(toParent(parent),Gui.CENTER_FRAME);
		return false;
	}
}
/**
* This attempts to open a file, after saving the old file if necessary.
**/
//===================================================================
public boolean open(Object oldData,Object newData,Frame parent)
//===================================================================
{
	exception = null;
	if (!checkSave(oldData,parent)) return false;
	String in = open(parent);
	if (in == null) return false;
	return doOpen(newData,in,parent);
}

//===================================================================
public String openText(String fileName,Frame parent)
//===================================================================
{
	exception = null;
	if (fileName == null) return null;
	File file = fileModel.getNew(fileName);
	/*
	StreamReader br = new StreamReader(in);
	//br.encoding = null; <- Why did I have this?
	String txt = "";
	while(true){
		String line = br.readLine();
		if (line == null) break;
		txt += line+"\n";
	}
	br.close();
	*/
	try{
		String got = TextReader.readAll(file.toReadableStream(), (textCodec != null) ? (TextCodec)textCodec.getCopy() : null);
		lastSaved = fileName;
		hasChanged = false;
		return got;
		/*
		MemoryFile mf = new MemoryFile(in,"r");
		JavaUtf8Codec ju = new JavaUtf8Codec(JavaUtf8Codec.STRIP_CR);
		ewe.util.CharArray ca = ju.decodeText(mf.data.data,0,mf.data.length,true,null);
		return new String(ca.data,0,ca.data.length);
		*/
		//return ewe.util.Utils.decodeJavaUtf8String(mf.data.data,0,mf.data.length);
	}catch(IOException e){
		returnError(e.getMessage(),parent);
		return null;
	}
}
/**
 * This opens and reads the file as a string. It will call checkSave() on the oldData if it is not null.
 * @param oldData Data to check for saving first (may be null).
 * @param parent The parent Frame (may be null).
 * @return The text contained in the file or null on error or cancellation by the user.
 */
//===================================================================
public String open(String oldData,Frame parent)
//===================================================================
{
	exception = null;
	if (oldData != null)
		if (!checkSave(oldData,parent)) return null;
	return openText(open(parent),parent);
}
/**
* This attempts to save the file.
**/
//===================================================================
public boolean save(boolean saveAs,Object data,Frame parent)
//===================================================================
{
	exception = null;
	String out = saveAs ? saveAs(parent) : save(parent);
	if (out == null) return false;
	return doSave(data,out,parent);
}
/**
* This tells the FileSaver that new data is about to be created and
* that the old data should be saved if it has been changed. newFileName
* can be null to say that the new data is as yet unnamed.
**/
//===================================================================
public boolean newData(Object oldData,String newFileName,Frame parent)
//===================================================================
{
	exception = null;
	if (!checkSave(oldData,parent)) return false;
	newData(newFileName);
	return true;
}

//===================================================================
public void onEvent(Event ev)
//===================================================================
{
	if (ev instanceof DataChangeEvent) hasChanged = true;
}
/**
* A Form can call this when the user has requested to close the Form. It should
* be called within the canClose() method.
* @param f The Form checking for exit.
* @param retCode The return code the Form should exit with.
* @return true if the Form should exit, false if it should not.
*/
//===================================================================
public boolean checkExit(final Form f,Object dataToSave, final int retCode)
//===================================================================
{
	exception = null;
	ewe.reflect.Wrapper w = new ewe.sys.ThreadTask(){
		protected void doTask(boolean inSeparateThread,Object data,ewe.reflect.Wrapper ret){
			boolean cs = checkSave(data,null);
			ret.setBoolean(cs);
			if (!cs || !inSeparateThread) return;
			f.close(retCode);
		}
	}.execute(dataToSave);
	if (w == null) return false;
	else return w.getBoolean();
}
/**
* This returns the last saved or opened File. If it is null then no save/open has
* been done.
**/
//===================================================================
public File getSavedFile()
//===================================================================
{
	if (lastSaved == null) return null;
	return fileModel.getNew(lastSaved);
}
//===================================================================
public String getLastSaved()
//===================================================================
{
	return lastSaved;
}
//===================================================================
public boolean getHasChanged()
//===================================================================
{
	return hasChanged;
}
//===================================================================
public Exception getException()
//===================================================================
{
	return exception;
}

public void setHasChanged(boolean hasChanged)
{
	this.hasChanged = hasChanged;
}

public void setLastSaved(String lastSaved)
{
	this.lastSaved = lastSaved;
}

//##################################################################
}
//##################################################################


