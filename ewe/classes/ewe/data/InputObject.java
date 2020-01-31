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
import ewe.reflect.*;
/**
This is a utility object which can be used to input data from a user. There are two
basic ways of doing this:
<nl>
<li>Construct it using InputObject(String fields,int inputLength) and provide a text
formatted specification for "fields".</li>
<li>Override InputObject and override the setupInputStack() method.</li>
</nl>
<p>
Using the first method, constructing an InputObject with a field specification, the format for
the "fields" variable is as follows:
<p><b>"prompt$hotkey|fieldName$fieldType|prompt$hotkey|fieldName$fieldType|..."</b><p>
<b>prompt</b> can be any string which will be displayed as a prompt for an input field. If you end the prompt with
the sequence "$x", where 'x' can be any character, then 'x' will be considered the hot key for that field.<br>
<b>fieldName</b> must be a field name for the previous prompt. This must be followed by a '$' and then an optional
java type in 'L' format. For example if you want to input a ewe.sys.Time object then you should have: "$Lewe/sys/Time;"
immediately following the field name. If you want to enter an integer then you should have: "$I" immediately following the
field name. A '$' with nothing after it denotes a String.
<p>
For example:
<pre>
public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	PropertyList defaultValues = PropertyList.fromStrings("name|Homer Simpson|age|42");
	PropertyList entered = new InputObject("Name:$n|name$|Age:$a|age$I",30).input(null,"Please enter data!",defaultValues);
	if (entered != null) ewe.sys.Vm.debug(entered.toString());
	ewe.sys.Vm.exit(0);
}
</pre>
<p>
With the second method, in the overriden setupInputStack(InputStack is,Editor ed) you should add the appropiate
input controls and fields to the InputStack 'is' and editor 'ed'.
**/
//##################################################################
public class InputObject extends LiveObject implements HasProperties{
//##################################################################
/**
* This is the default input length, currently set to 30 characters.
**/
public int inputLength = 30;
/**
* This is the specification of the fields to be input.
**/
public String fields = "";

PropertyList pl = new PropertyList();
/**
* Get the property list which holds the values for the entered fields.
**/
public PropertyList getProperties(){return pl;}

//===================================================================
public InputObject(){}
//===================================================================
/**
* Create an InputObject with the specified fields and with a particular input length. Each field
* must specify its type.
* @param fields This can be in two formats:<br>
* <b>Either:</b> field1$type1,field2$type2,field3%type3,... <br>
* <b>Or:</b> Prompt1$hotKey1|field1$type1|Prompt2$hotKey2|field2$type2|...<br>
* Note that the first one is comma separated while the second is '|' separated.<p>
* The type of the field must be specified and should be for primitive (non-object values):
* I (integer), D (double), Z (boolean) or, to specify an object type: <b>L</b><i>full_class_name</i><b>;</b>.
* Note that can just have a '$' with nothing following to specify a String. Note also that for
* object values, you must use a '/' instead of a '.' to separate packages. e.g. <b>Lewe/sys/Time;</b>
* @param inputLength The length of the inputs.
*/
//===================================================================
public InputObject(String fields,int inputLength)
//===================================================================
{
	this.fields = fields;
	this.inputLength = inputLength;
}
/**
* You can override this to add your own custom fields and controls.
**/
//-------------------------------------------------------------------
protected void setupInputStack(InputStack is,Editor ed)
//-------------------------------------------------------------------
{
	UIBuilder ui = new UIBuilder(ed,this,is);
	ui.addAll(fields);
/*
	is.addInputs(ed,fields);
*/
}
/**
* You should not need to override this, but you can if you want to completely
* change the way the input form will look.
**/
//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int which)
//===================================================================
{
	InputStack is = new InputStack();
	is.inputLength = inputLength;
	cp.addLast(is).setCell(cp.HSTRETCH);
	setupInputStack(is,ed);
	Gui.setOKCancel(ed);
}
/**
* This does the following:
* <ul>
* <li>Sets the values of the fields to be as specified in the "values" PropertyList parameter. If
* this is null then no change will be made.</li>
* <li>Creates an Editor for the InputObject and sets the title.</li>
* <li>Calls execute() on the Editor to display the input form.</li>
* <li>If the user presses OK, it will return a PropertyList containing entries for the data entered. If the
* user pressed Cancel, it will return null.</li>
* </ul>
* You can retrieve the data entered for each field by calling getValue("fieldName",defaultValue) on the returned
* PropertyList.
**/
//===================================================================
public PropertyList input(Frame parent,String title,PropertyList values)
//===================================================================
{
	if (values == null) values = new PropertyList();
	getProperties().set(values);
	Editor ed = getEditor(0);
	ed.title = title;
	if (ed.execute(parent,Gui.CENTER_FRAME) != ed.IDOK) return null;
	return getProperties();
}
/**
* This shoudl create the new Editor object but not add anything to it. This is called
* by getEditor(). By default it returns a standard Editor object.
**/
//-------------------------------------------------------------------
protected Editor makeNewEditor(int options)
//-------------------------------------------------------------------
{
	return new Editor(){
		protected boolean canExit(int exitCode){
			if (!super.canExit(exitCode)) return false;
			if (exitCode != IDCANCEL) return isValid(InputObject.this.getProperties(),this);
			else return true;
		}
	};
}
/**
 * Use this if you wish from the isValid() method to display a message if the input is invalid.
 * @param title the title of the message box.
 * @param message the text of the message.
 */
//-------------------------------------------------------------------
protected void showErrorMessage(String title, String message)
//-------------------------------------------------------------------
{
	new MessageBox(title,message,MessageBox.MBOK).execute();
}


/**
 * This is called before the InputObject form exits with an exit code of IDOK. If it returns false the form will not exit.
 * @param pl the PropertyList holding the input data.
 * @param ed the Editor form used for input.
 * @return true to allow the input form to exit, false to prevent an exit.
 */
//-------------------------------------------------------------------
protected boolean isValid(PropertyList pl, Editor ed)
//-------------------------------------------------------------------
{
	return true;
}
/**
This allows you to test the InputObject with specified data. Simply execute:<br>
<b>ewe ewe.data.InputObjct "field_list" ["default_data"]</b><br>
To display an input box for the specified field list. The default_data is optional.<p>
Try it out with the following field lists:<br>
<b>"lastName$,firstNames$,smoker$Z,salary$D,dob$Lewe/sys/Time;</b><br>
and<br>
<b>"Contact Name$n|name$|Company$c|company$|Age$a|age$I"</b>
**/
public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	String defaultValue = args.length > 1 ? args[1] : "name|Homer Simpson|age|42";
	String fields = args.length > 0 ? args[0] : "Name:$n|name$|Age:$a|age$I";
	PropertyList defaultValues = PropertyList.fromStrings(defaultValue);
	PropertyList entered = new InputObject(fields,30).input(null,"Please enter data!",defaultValues);
	if (entered != null) {
		ewe.sys.Vm.out().println("Entered: "+entered.toString());
		ewe.sys.Vm.out().println("Closing in 10 seconds...");
		ewe.sys.mThread.nap(10000);
	}
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

