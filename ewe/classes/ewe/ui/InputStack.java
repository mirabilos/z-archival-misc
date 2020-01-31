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
public class InputStack extends CellPanel {
//##################################################################
public boolean doPopupInput = false;
public boolean doubleLined = false;
public int inputLength = 10;
{
	//borderWidth = 2;
	//borderStyle = EDGE_ETCHED;
	defaultTags.set(INSETS,new Insets(1,1,1,1));
	columns = 1;
}
public Vector controls = new Vector(), prompts = new Vector();

//===================================================================
public void make(boolean remake)
//===================================================================
{
	int col = columns;
	for (int i = 0; i<controls.size(); i++){
		col--;
		boolean last = false;
		if (col == 0) {
			last = true;
			col = columns;
		}
		Control c = (Control)controls.get(i);
		String p  = (String)prompts.get(i);
		Control l = null;
		if (p != null){
			int hAlign = EAST;
			if (doubleLined) hAlign = WEST;
			l = getLabel(p);
			addNext(l,doubleLined).setCell(DONTSTRETCH).setControl(DONTFILL|hAlign);
		}
		addNext(c,last).setCell(HSTRETCH).setControl(HFILL|WEST);
		if (p == null && !doubleLined) c.setTag(SPAN,new Dimension(2,1));
		c.prompt = p;
		c.setPromptControl(l);
		if (l != null)
			if (l instanceof mLabel) ((mLabel)l).control = c;
	}
	super.make(remake);
}

//==================================================================
public void add(Control c,String prompt)
//==================================================================
{
	controls.add(c);
	prompts.add(prompt);
	if (!(c instanceof InputStack)) c.columns = inputLength;
}

//===================================================================
public static String appendToPrompt(String promptWithHotkey,String moreText)
//===================================================================
{
	int idx = promptWithHotkey.indexOf('$');
	if (idx == -1 || idx != promptWithHotkey.length()-2) return promptWithHotkey+moreText;
	else return promptWithHotkey.substring(0,idx)+moreText+promptWithHotkey.substring(idx);
}
//------------------------------------------------------------------
protected Control getLabel(String text)
//------------------------------------------------------------------
{
	return new mLabel(text);
}
//------------------------------------------------------------------
protected void popup(mInput what) {if (doPopupInput) InputForm.attach(what);}
//------------------------------------------------------------------
protected mComboBox addComboBox(mComboBox cb,String p)
//------------------------------------------------------------------
{
	add(cb,p);
	popup(cb.input);
	return cb;
}
//==================================================================
public mLabel addLabel(String prompt) {Control l = getLabel(prompt); add(l,null); return (mLabel)l;}
public mInput addInput(String prompt,String initial) {mInput i = new mInput(initial); add(i,prompt); popup(i);return i;}
public mCheckBox addCheckBox(String prompt) {return addCheckBox(prompt,null,false);}
public mCheckBox addCheckBox(String prompt,CheckBoxGroup group,boolean state)
{mCheckBox mc = new mCheckBox(); add(mc,prompt); if (group != null) mc.setGroup(group); mc.setState(state); return mc;}
public mChoice addChoice(String prompt) {mChoice mc = new mChoice(); add(mc,prompt); return mc;}
public mChoice addChoice(String prompt,String [] choices,int initialChoice) {mChoice mc = (mChoice)addChoice(prompt); mc.set(choices,initialChoice); return mc;}
public mButton addButton(String prompt) {mButton mb = new mButton(prompt); add(mb,null); return mb;}
public InputStack addInputStack(String prompt,int columns) {InputStack is = new InputStack(); add(is,prompt); is.columns = columns; is.borderStyle = Graphics.EDGE_SUNKEN; return is;}
public CellPanel addPanel(String prompt,boolean spanBoth) {CellPanel mp = new CellPanel(); if (spanBoth) {if (prompt != null) add(getLabel(prompt),null); add(mp,null);} else add(mp,prompt); return mp;}
public mTextArea addTextArea(String prompt,int rows,int columns,boolean scrollable) {if (prompt != null) add(getLabel(prompt),null); mTextArea mt = new mTextArea(rows,columns); if (scrollable) add(mt.getScrollablePanel(),null); else add(mt,null); return mt;}
public mComboBox addComboBox(String prompt) {return addComboBox(new mComboBox(),prompt);}
public mComboBox addComboBox(String prompt,String [] choices,int initialChoice) {return addComboBox(new mComboBox(choices,initialChoice),prompt);}
public mComboBox addComboBox(String prompt,String [] choices,String initialChoice) {return addComboBox(new mComboBox(choices,initialChoice),prompt);}
public CheckBoxGroup addChecks(String prompt,String choices [],int columns)
{
	CellPanel p2 = new CellPanel();
	CheckBoxGroup cg = new CheckBoxGroup();
	cg.makePanel(choices,p2,columns);
	add(p2,prompt);
	return cg;
}
/**
 * This converts a name with underscores to a prompt with capital letters and spaces where
 * the underscores were.
 * @param name
 * @return The converted name.
 */
//===================================================================
public static String nameToPrompt(String name)
//===================================================================
{
	if (name == null) return null;
	char [] all = ewe.sys.Vm.getStringChars(name);
	char [] another = new char[all.length*2];
	boolean cap = true;
	boolean hasUnder = false;
	int j = 0;
	for (int i = 0; i<all.length; i++){
		if (all[i] == '$') return new String(another,0,j);
		if (all[i] == '_') {
			hasUnder = true;
			another[j++] = ' ';
			cap = true;
		}else{
			if (cap) another[j++] = Character.toUpperCase(all[i]);
			else if (i != 0 && !hasUnder && Character.isUpperCase(all[i])){
				if (Character.isUpperCase(all[i-1])) another[j++] = all[i];
				else {
					another[j++] = ' ';
					another[j++] = all[i];
				}
			}else another[j++] = all[i];
			cap = false;
		}
	}
	return new String(another,0,j);
}

//===================================================================
public void addInputs(Editor ed,String promptsAndFields)
//===================================================================
{
	if (promptsAndFields.indexOf('|') == -1) addInputFields(ed,promptsAndFields);
	else{
		String [] pf = mString.split(promptsAndFields,'|');
		for (int i = 0; i<pf.length-1; i+=2)
			ed.addField(addInput(pf[i],""),pf[i+1]);
	}
}
/**
 * Add a set of inputs given the field names. The prompts will be created from the field names.
 * @param ed An editor to add the fields to.
 * @param fields The field names - the prompts will be derived from them.
 */
//===================================================================
public void addInputFields(Editor ed,String fields)
//===================================================================
{
	String [] pf = mString.split(fields,fields.indexOf('|') == -1 ? ',' : '|');
	for (int i = 0; i<pf.length; i++)
		ed.addField(addInput(nameToPrompt(pf[i])+":",""),pf[i]);
}
//===================================================================
public void addChecks(Editor ed,String promptsAndFields,boolean exclusive)
//===================================================================
{
	addChecks(ed,promptsAndFields,exclusive,false);
}
//===================================================================
public void addChecks(Editor ed,String promptsAndFields,boolean exclusive,boolean checkOnLeft)
//===================================================================
{
	String [] pf = mString.split(promptsAndFields,'|');
	CheckBoxGroup cg = exclusive ? new CheckBoxGroup() : null;
	for (int i = 0; i<pf.length-1; i+=2){
		mCheckBox mc;
		if (checkOnLeft) add(mc = new mCheckBox(pf[i]),null);
		else mc = addCheckBox(pf[i]);
		ed.addField(mc,pf[i+1]);
		if (cg != null) mc.setGroup(cg);
	}
}
//##################################################################
}
//##################################################################

