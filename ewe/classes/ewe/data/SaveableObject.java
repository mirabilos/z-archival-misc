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
package ewe.data;
import ewe.util.*;
import ewe.ui.*;
import ewe.io.*;
//##################################################################
public class SaveableObject extends LiveObject{
//##################################################################

public MenuItem chosenMenuItem = new MenuItem();

/**
* This is used to reset the Object to a "new" state. By default it will
* copy from a new instance of this class.
**/
//===================================================================
public void newObject()
//===================================================================
{
	copyFrom(getNew());
}
/**
* This is used to save the Object state. By default it will convert the
* object to a text string and then save it.
**/
//===================================================================
public void saveObject(ewe.io.File destination) throws ewe.io.IOException
//===================================================================
{
	String text = textEncode();
	ewe.io.StreamWriter sw = new ewe.io.StreamWriter(destination.toWritableStream(false));
	sw.println(text);
	sw.close();
	savedTo(destination);
}
/**
 * This is used to retrieve the Object state from a File. By default it will
 * treat the file as a text string and decode it.
 */
//===================================================================
public void openObject(ewe.io.File source) throws ewe.io.IOException
//===================================================================
{
	ewe.io.StreamReader sr = new ewe.io.StreamReader(source.toReadableStream());
	String got = sr.readLine();
	sr.close();
	try{
		if (got == null) throw new Exception();
		textDecode(got);
		openedFrom(source);
	}catch(Exception e){
		throw new ewe.io.IOException("The file: "+source+" did not contain correct data.");
	}
}

public static final int NewAction = 1;
public static final int OpenAction = 2;
public static final int SaveAction = 3;
public static final int ExitAction = 4;


/**
 * This will be called if an exception occured during processing of a File action. By
 * default it do nothing if the FileSaver is not null, otherwise it will display
 * a standard ReportException box.
 * @param whichFileAction one of NewAction, OpenAction, SaveAction.
 * @param e The exception that occured.
 */
//-------------------------------------------------------------------
protected void fileExceptionOccured(int whichFileAction,Exception e,FileSaver saver)
//-------------------------------------------------------------------
{
	if (saver == null || true)
		new ReportException(e,null,null,false).exec();
}
//===================================================================
public void New_action(Editor ed)
//===================================================================
{
	ewe.io.FileSaver saver = ed.getFileSaver();
	if (saver == null) return;
	try{
		if (!saver.newData(this,null,ed.getFrame())){
			if (saver.getException() != null) throw saver.getException();
			return;
		}
		newObject();
		ed.toControls();
	}catch(Exception e){
		//e.printStackTrace();
		fileExceptionOccured(NewAction,e,saver);
	}
}
//-------------------------------------------------------------------
private void doSave(boolean saveAs,Editor ed)
//-------------------------------------------------------------------
{
	ewe.io.FileSaver saver = ed.getFileSaver();
	if (saver == null) return;
	try{
		if (!saver.save(saveAs,this,ed.getFrame())){
			if (saver.getException() != null) throw saver.getException();
			return;
		}
	}catch(Exception e){
		//e.printStackTrace();
		fileExceptionOccured(SaveAction,e,saver);
	}

}
//===================================================================
public void Save_action(Editor ed)
//===================================================================
{
	doSave(false,ed);
}
//===================================================================
public void Save_As_action(Editor ed)
//===================================================================
{
	doSave(true,ed);
}
//===================================================================
public void Open_action(Editor ed)
//===================================================================
{
	ewe.io.FileSaver saver = ed.getFileSaver();
	if (saver == null) return;
	try{
		Object n = getNew();
		if (!saver.open(this,n,ed.getFrame())){
			if (saver.getException() != null) throw saver.getException();
			return;
		}
		copyFrom(n);
		ed.toControls();
	}catch(Exception e){
		//e.printStackTrace();
		fileExceptionOccured(OpenAction,e,saver);
	}
}
//===================================================================
public void Exit_action(Editor ed)
//===================================================================
{
	ewe.io.FileSaver saver = ed.getFileSaver();
	if (saver == null) return;
	try{
		Object n = getNew();
		if (!saver.checkSave(this,ed.getFrame())){
			if (saver.getException() != null) throw saver.getException();
			return;
		}
		saver.setHasChanged(false);
		ed.exit(0);
	}catch(Exception e){
		//e.printStackTrace();
		fileExceptionOccured(OpenAction,e,saver);
	}
}
/**
 * This is called after a successful save.
 * @param saved The File the object was saved to.
 */
//-------------------------------------------------------------------
protected void savedTo(ewe.io.File saved)
//-------------------------------------------------------------------
{
}
/**
 * This is called after a successful open.
 * @param saved The File the object was saved to.
 */
//-------------------------------------------------------------------
protected void openedFrom(ewe.io.File opened)
//-------------------------------------------------------------------
{
}
/**
 * This attempts to open a file to load this object.
 * @param source The File object or file name String.
 * @param ed The editor used to edit the SaveableObject.
 * @param showOpenIfFailed If this is true then the FileSaver associated with the editor (if any)
	will be displayed to attempt to open a new File instead.
 * @param showException If this is true then the full file open error will be displayed if an
	error occured opening the file.
 * @return true if a file was opened, false otherwise.
 */
//===================================================================
public boolean tryOpen(Object source,Editor ed,boolean showOpenIfFailed,boolean showException)
//===================================================================
{
	File f = (source instanceof File) ? (File)source : File.getNewFile(source.toString());
	try{
		openObject(f);
		if (ed != null){
			FileSaver fs = ed.getFileSaver();
			if (fs != null) {
				fs.setLastSaved(f.getFullPath());
				fs.setHasChanged(false);
			}
		}
		return true;
	}catch(IOException e){
		if (showException)
			new ReportException(e,"Could not open file.","Check the file name.",false).execute();
		else
			new MessageBox("File Error","Could not open file:\n"+source,MessageBox.MBOK).execute();
		if (ed == null) return false;
		FileSaver fs = ed.getFileSaver();
		if (fs == null) return false;
		return fs.open(null,this,ed.getFrame());
	}
}
/**
 * This attempts to open a file to load this object.
 * @param source The File object or file name String.
 * @param ed The editor used to edit the SaveableObject.
 * @return true if a file was opened, false otherwise.
 */
//===================================================================
public boolean tryOpen(Object source,Editor ed)
//===================================================================
{
	return tryOpen(source,ed,true,false);
}
//##################################################################
}
//##################################################################

