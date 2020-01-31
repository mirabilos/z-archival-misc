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
import ewe.data.PropertyList;
import ewe.sys.*;
import ewe.reflect.*;

//##################################################################
public class ExecTransfer implements Runnable{
//##################################################################

public Handle handle;
public ewe.io.FileSaver saver;
public PropertyList sourceProperties;
public boolean doTransfer;

/**
* A value sent to the createForm method. It indicates that the new Form should be created
* and returned. This will be called even if an exec transfer is not being done.
**/
public static final int CREATE_NEW_FORM = 1;
/**
* A value sent to the createNewForm method. It indicates that the new Form has exited and that
* the original Form should be recreated and returned. It is only called if an exec transfer IS
* being done.
**/
public static final int RECREATE_ORIGINAL_FORM = 2;
/**
* A value sent to the createForm method. It indicates that the new Form has exited but the
* original Form is still displayed. No Form should be returned. It is only called if an exec
* transfer is NOT being done.
**/
public static final int NEW_FORM_EXITED = 3;
/**
* True by default - set this to false if you do not want the new Form to affect the changed
* state of any FileSaver. In that case setFileSaver() will not be called
* on the new Form.
**/
public boolean oldFileSaverListensToNewForm = true;
/**
* False by default - set this to true if you want the new Form to check the FileSaver for before
* saving before closing. In that case you may need to set the ObjectToSave property of the new
* Form if you need it to save something other than object being edited.
**/
public boolean newFormChecksFileSaverOnExit;


/**
 * This is used to get the original displayed Form ONLY if an exec transfer is not being done. If
 * it returns null then an exec transfer is being done and the original Form is no longer available.
 */
//===================================================================
public Form getOrignalForm()
//===================================================================
{
	return originalForm;
}

private Form originalForm;
private boolean oldFormChecksFileSaverOnExit;
private int methodParams = 3;
private Object formCreator;
private ewe.reflect.Method formCreatorMethod;

/**
* This creates an ExecTransfer that will do the transfer only if the VM reports that
* the executing device is considered a low memory device.
**/
//===================================================================
public ExecTransfer(Form sourceForm)
//===================================================================
{
	this(sourceForm,(Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_LOW_MEMORY) != 0,null,null);
}

/**
* This creates an ExecTransfer that will do the transfer only if the doTransfer parameter
* is true.
 * @param sourceForm The original currently displaying Form
 * @param doTransfer If this is true then an exec transfer will be done, otherwise the new Form
 */
//===================================================================
public ExecTransfer(Form sourceForm,boolean doTransfer)
//===================================================================
{
	this(sourceForm,doTransfer,null,null);
}
/**
* This creates an ExecTransfer that will do the transfer only if the VM reports that
* the executing device is considered a low memory device.
 * @param sourceForm The original currently displaying Form
 * @param formCreator An object that will create the new Form and re-create the original Form on request.
 * @param formCreatorMethodName The name of the method of the formCreator that will be called.<br>
This method must be of the form:<br>
<b>ewe.ui.Form any_method_name(ewe.ui.ExecTransfer transfer,boolean createNewForm);</b> or <br>
<b>ewe.ui.Form any_method_name(ewe.ui.ExecTransfer transfer,boolean createNewForm,int newFormExitCode);</b>
 * @exception IllegalArgumentException if the method could not be found in the supplied formCreator object.
 */
//===================================================================
public ExecTransfer(Form sourceForm,Object formCreator,String formCreatorMethodName)
throws IllegalArgumentException
//===================================================================
{
	this(sourceForm,(Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_LOW_MEMORY) != 0,formCreator,formCreatorMethodName);
}
/**
* This creates an ExecTransfer that will do the transfer only if the VM reports that
* the executing device is considered a low memory device.
 * @param sourceForm The original currently displaying Form
 * @param doTransfer If this is true then an exec transfer will be done, otherwise the new Form
	will be displayed without the old one being removed.
 * @param formCreator An object that will create the new Form and re-create the original Form on request.
 * @param formCreatorMethodName The name of the method of the formCreator that will be called.<br>
This method must be of the form:<br>
<b>ewe.ui.Form any_method_name(ewe.ui.ExecTransfer transfer,boolean createNewForm);</b> or <br>
<b>ewe.ui.Form any_method_name(ewe.ui.ExecTransfer transfer,boolean createNewForm,int newFormExitCode);</b>
 * @exception IllegalArgumentException if the method could not be found in the supplied formCreator object.
 */
//===================================================================
public ExecTransfer(Form sourceForm,boolean doTransfer,Object formCreator,String formCreatorMethodName)
throws IllegalArgumentException
//===================================================================
{
	if (formCreator == null) {
		formCreator = this;
		formCreatorMethodName = "createForm";
	}
	this.formCreator = formCreator;
	ewe.reflect.Reflect r = ewe.reflect.Reflect.getForObject(formCreator);
	if (r != null)
		formCreatorMethod = r.getMethod(formCreatorMethodName,"(Lewe/ui/ExecTransfer;II)Lewe/ui/Form;",ewe.reflect.Reflect.PUBLIC);
	if (formCreatorMethod == null) throw new IllegalArgumentException("Can't find method: "+formCreatorMethodName);
	sourceProperties = sourceForm.getProperties();
	if (sourceForm instanceof Editor){
		saver = ((Editor)sourceForm).getFileSaver();
		oldFormChecksFileSaverOnExit = sourceProperties.getBoolean("CheckFileSaverOnExit",false);
	}
	this.doTransfer = doTransfer;
	if (doTransfer) handle = sourceForm.closeForTransfer(true,true);
	else {
		originalForm = sourceForm;
		handle = originalForm.handle;
	}
	new mThread(this).start();
}
//===================================================================
public void run()
//===================================================================
{
	int ex = 0;
	Form f = null;
	try{
		f = createForm(CREATE_NEW_FORM,0);
		if (saver != null && oldFileSaverListensToNewForm && f instanceof Editor)
			((Editor)f).setFileSaver(saver,newFormChecksFileSaverOnExit);
		Handle h = f.exec(true);
		f = null;
		ex = Form.waitUntilClosed(h);
	}catch(Exception e){
	}
	if (!doTransfer){
		createForm(NEW_FORM_EXITED,ex);
		return;
	}
	Form.showWait();
	f = createForm(RECREATE_ORIGINAL_FORM,ex);
	if (saver != null && f instanceof Editor)
		((Editor)f).setFileSaver(saver,oldFormChecksFileSaverOnExit);
	if (f != null) f.exec(handle);
	else Form.cancelWait();
}
/**
* This stops the Handle of the sourceForm. This can be used to abort the
* the recreation of the source, requesting that it report that it exited instead.
* @param exitCode The exit code to return.
* @return always a null Form.
*/
//===================================================================
public Form stopSourceForm(int exitCode)
//===================================================================
{
	Form.stopFormHandle(handle,exitCode);
	return null;
}
//-------------------------------------------------------------------
private Form createForm(int value,int retCode)
//-------------------------------------------------------------------
{
	if (formCreatorMethod == null)
		throw new IllegalStateException("Don't know how to create the Form");
	Wrapper [] all = new Wrapper[methodParams];
	all[0] = new Wrapper().setObject(this);
	all[1] = new Wrapper().setInt(value);
	all[2] = new Wrapper().setInt(retCode);
	Wrapper ret = new Wrapper();
	Wrapper got = formCreatorMethod.invoke(formCreator,all,ret);
	if (got == null) return null;
	return (Form)got.getObject();
}
//===================================================================
public Form createForm(ExecTransfer et,int creationAction,int newFormRetCode)
//===================================================================
{
	throw new IllegalStateException("You must override createForm()");
}
//##################################################################
}
//##################################################################

