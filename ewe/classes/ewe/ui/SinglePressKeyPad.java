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
import ewe.fx.*;
import ewe.util.*;

//##################################################################
public class SinglePressKeyPad extends ControlPopupForm {
//##################################################################
String myText = "";
Vector theKeys = new Vector();
Vector strings = new Vector();
//==================================================================
public SinglePressKeyPad()
//==================================================================
{
	modify(DrawFlat,0);
	columns = 3;
}

//==================================================================
public void addKeys(String key,String values)
//==================================================================
{
	String [] got = mString.split(key,'|');
	String [] strs = got;
	if (values != null) strs = mString.split(values,'|');
	for (int i = 0; i<got.length; i++) {
		theKeys.add(new mButton(got[i]));
		strings.add(strs[i]);
	}
}

//==================================================================
public void make(boolean reMake)
//==================================================================
{
	if (made) return;
	CellPanel keys = new CellPanel();
	addLast(keys);
	int c = 0;
	for (int i = 0; i<theKeys.size(); i++) {
		mButton b = (mButton)theKeys.get(i);
		keys.addNext(b);
		c++;
		if (c >= columns) {
			keys.endRow();
			c = 0;
		}
	}
	super.make(reMake);
}
//==================================================================
public FormFrame getFormFrame(int options)
//==================================================================
{
	FormFrame ff = super.getFormFrame(options);
	ff.contentsOnly = true;
	ff.borderWidth = 1;
	return ff;
}
//==================================================================
protected void doReset() {myText = client.getText();}
protected void doClear() {myText = "";}
//==================================================================
//==================================================================
protected void pressed(Control who)
//==================================================================
{
	if (who == reset) doReset();
	for (int i = 0; i<theKeys.size(); i++) {
		mButton b = (mButton)theKeys.get(i);
		if (who == b) {
			myText = (String)strings.get(i);
			exit(IDOK);
			return;
		}
	}
	//if (who == clear) doClear();
}
//==================================================================
public void onControlEvent(ControlEvent ev)
//==================================================================
{
	if (ev.type == ev.PRESSED) pressed((Control)ev.target);
	super.onControlEvent(ev);
}

//==================================================================
public void close(int exitCode)
//==================================================================
{
	super.close(exitCode);
	int o = client.modify(Invisible,0);
	if (exitCode == IDOK) client.setText(myText);
	client.restore(o,Invisible);
	if (client instanceof mInput) ((mInput)client).selectAll();
	client.repaintDataNow();
	if (exitCode == IDOK)
		client.notifyDataChange();
}


//##################################################################
}
//##################################################################

