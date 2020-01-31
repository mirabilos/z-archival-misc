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
import ewe.filechooser.*;
import ewe.io.File;
import ewe.sys.Vm;
import ewe.util.mString;

//##################################################################
public class mFileInput extends Editor{
//##################################################################

{
	modify(TakeControlEvents,0);
}
public Control input = new mInput();
public ButtonControl select = new mButton("Select");

/**
* This is sent to the persistentHistoryKey of the created FileChooser.
**/
public String persistentHistoryKey;

public String file = "";
public String extraDirectory = "";
public ewe.io.File fileModel = ewe.sys.Vm.newFileObject();
public void setText(String text){input.setText(file = text);}
public String getText() {return input.getText();}

public int fileChooserOptions = FileChooser.OPEN;
public ewe.util.Vector masks = new ewe.util.Vector();
public int frameOption = Gui.FILL_FRAME;
public String defaultExtension = null;
public static ewe.fx.IImage chooseImage = ewe.fx.ImageCache.cache.get("ewe/ChooseFile.bmp",ewe.fx.Color.White);
//===================================================================
public mFileInput setTitleAndTip(String title)
//===================================================================
{
	select.setToolTip(this.title = title);
	return this;
}
//===================================================================
public mFileInput() {this(null,20);}
//===================================================================
public mFileInput(String mask,int length)
//===================================================================
{
	input.columns = length;
	select.modify(MouseSensitive,0);
	Gui.iconize(select,chooseImage,false,null);
	if (mask != null) masks.add(mask);
}
/**
This constructor is used by UIBuilder.
**/
//===================================================================
public mFileInput(ewe.data.PropertyList fieldProperties)
//===================================================================
{
	mFileInput mf = this;
	ewe.data.PropertyList pl = fieldProperties;
	mf.fileChooserOptions = pl.getInt("fileChooserOptions",mf.fileChooserOptions);
	mf.title = pl.getString("title","Select File...");
	mf.defaultExtension = pl.getString("defaultExtension",mf.defaultExtension);
	String masks = pl.getString("masks","");
	String allMasks[] = mString.split(masks,';');
	for (int i = 0; i<allMasks.length; i++) mf.masks.add(allMasks[i]);
}
//===================================================================
public void setData(Object data)
//===================================================================
{
	if (data instanceof File){
		File f = (File)data;
		fileModel = f.getNew("/");
		setText(f.getFullPath());
	}
}
//===================================================================
public void getData(Object data)
//===================================================================
{
	if (data instanceof File){
		File f = (File)data;
		f.set(null,file);
	}
}
//===================================================================
public void make(boolean remake)
//===================================================================
{
	if (!made){
		addField(addNext(input),"file");
		if (select != null){
			addField(addNext(select).setCell(VSTRETCH),"sel");
			select.modify(NoFocus,0);
		}
	}
	super.make(remake);
}
//===================================================================
public void onDataChangeEvent(DataChangeEvent ev)
//===================================================================
{
	super.onDataChangeEvent(ev);
	if (ev.target == input) notifyDataChange();
}
/**
* This is called to create a new FileChooser when the select button is pressed. Override it
* if necessary to provide a new type of FileChooser (e.g. an ImageFileChooser).
**/
//-------------------------------------------------------------------
protected FileChooser createFileChooser(int options,String initial,File fileModel)
//-------------------------------------------------------------------
{
	return new FileChooser(fileChooserOptions,initial,fileModel);
}
/**
* If you override this, first call super.setupFileChooser(fc) and then continue
* with your further modifications.
**/
//-------------------------------------------------------------------
protected void setupFileChooser(FileChooser fc)
//-------------------------------------------------------------------
{
	fc.defaultExtension = defaultExtension;
	fc.title = title;
	for (int i = 0; i<masks.size(); i++){
		if (i == 0) fc.mask = masks.get(i).toString();
		fc.addMask(masks.get(i).toString());
	}
	fc.persistentHistoryKey = persistentHistoryKey;
}
/**
 * This gets called when the FileChooser box accepts a new file name.
 * @param fileName the new file selected.
 */
public void newFileSelected(String fileName)
{
	file = fileName;
	toControls("file");
	notifyDataChange();
}
/**
 * This is called when the Select File button is pressed.
 *
 */
public void selectNow()
{
	if (fileModel == null) fileModel = ewe.sys.Vm.newFileObject();
	if ((getModifiers(true) & (NotEditable|DisplayOnly)) != 0) return;
	String ed = extraDirectory == null ? "" : extraDirectory;
	if (ed.length() > 1) ed = ewe.util.mString.removeTrailingSlash(extraDirectory);
	if (ed.length() != 0) {
		File ff = fileModel.getNew(ed);
		if (ff != null) ff = ff.getChild(file);
		if (ff != null) ed = ff.getFullPath();
	}else
		ed = file;
	Form.showWait();
	FileChooser fc = createFileChooser(fileChooserOptions,ed,fileModel);
	setupFileChooser(fc);
	int got = fc.execute(null,frameOption);
	try{
		if (got == IDCANCEL) return;
		String cf = fc.getChosen();
		if (!cf.equals(file)) newFileSelected(cf);
	}finally{
		fc.dismantle();
		fc = null;
	}
}
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor f)
//===================================================================
{
	if (ft.fieldName.equals("sel")){
		selectNow();
	}
}
/**
* This returns a File as specified by the file name and of the same type as the file model.
**/
//===================================================================
public ewe.io.File getFile()
//===================================================================
{
	return fileModel.getNew(file);
}
//##################################################################
}
//##################################################################

