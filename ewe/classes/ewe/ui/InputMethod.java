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
import ewe.sys.*;
import ewe.util.*;

//##################################################################
public class InputMethod implements TimerProc{
//##################################################################
public int keyTime = 500;

public Vector cycledKeys;

//===================================================================
public void clearCycledKeys()
//===================================================================
{
	if (cycledKeys != null) cycledKeys = null;
}
//===================================================================
public void addCycledKeys(int keyPress, long[] keys)
//===================================================================
{
	if (cycledKeys == null) cycledKeys = new Vector();
	cycledKeys.add(new Tag(keyPress,keys));
}
//===================================================================
public void addCycledKeys(int keyPress, String keys)
//===================================================================
{
	long[] k = new long[keys.length()];
	for (int i = 0; i<keys.length(); i++)
		k[i] = keys.charAt(i) & 0xffffffff;
	addCycledKeys(keyPress,k);
}
//-------------------------------------------------------------------
protected long modifyCycledKey(KeyEvent rxed, int keyToSend, int modifiers)
//-------------------------------------------------------------------
{
	return (modifiers << 32)|keyToSend;
}
//-------------------------------------------------------------------
protected KeyEvent checkCycledKeys(KeyEvent rxed)
//-------------------------------------------------------------------
{
	if (cycledKeys == null) return null;
	for (int i = 0; i<cycledKeys.size(); i++){
		Tag t = (Tag)cycledKeys.get(i);
		if (t.tag != rxed.key) continue;
		long[] all = (long[])t.value;
		if (all == null || all.length < 1) continue;
		KeyEvent last = getLastCycledKey(rxed);
		int ni = 0;
		if (last != null){
			for (int id = 0; id<all.length; id++){
				if (last.key == (int)(all[id] & 0xffffffff)){
					ni = (id+1)%all.length;
					break;
				}
			}
		}
		long modifiedKey = modifyCycledKey(rxed,(int)(all[ni] &0xffffffff),(int)((all[ni]>>32) &0xffffffff));
		if (modifiedKey == -1) return null;
		return makeCycledKeyEvent(rxed,(int)(modifiedKey&0xffffffff),(int)((modifiedKey>>32) &0xffffffff));
	}
	return null;
}
/**
This is the only method called by an EditControl. It is usually called
by the EditControl before any other key handling during active input.<p>
If this method wishes to modify the key event in some way, it must return
a <b>new</b> key event. If it returns null then the EditControl will handle
the original KeyEvent as normal.
**/
//===================================================================
public KeyEvent handleKey(KeyEvent inputEvent)
//===================================================================
{
	if ((inputEvent.type != KeyEvent.KEY_PRESS) || !(inputEvent.target instanceof Control))
		return null;
	KeyEvent ret = handleKeyPress(inputEvent,(Control)inputEvent.target);
	if (ret == null) cancelConditionalKey();
	return ret;
}
/**
Override this to provide functionality.<p>
If this method wishes to modify the key event in some way, it must return
a <b>new</b> key event. If it returns null then the EditControl will handle
the original KeyEvent as normal.
**/
//-------------------------------------------------------------------
protected KeyEvent handleKeyPress(KeyEvent inputEvent,Control target)
//-------------------------------------------------------------------
{
	return checkCycledKeys(inputEvent);
}
/**
Use this to create a new KeyEvent to return during handleKey.
**/
//-------------------------------------------------------------------
protected KeyEvent makeKeyEvent(KeyEvent rxed, int key, int modifiers)
//-------------------------------------------------------------------
{
	KeyEvent r = new KeyEvent();
	r.type = r.KEY_PRESS;
	r.target = rxed.target;
	r.key = key;
	r.modifiers = modifiers | IKeys.FROM_INPUT_METHOD;
	return r;
}

private int conditionalTime = 0;
protected KeyEvent conditionalKey;
protected int lastKeyPress;

//-------------------------------------------------------------------
protected KeyEvent getLastCycledKey(KeyEvent rxed)
//-------------------------------------------------------------------
{
	if (lastKeyPress != rxed.key) cancelConditionalKey();
	lastKeyPress = rxed.key;
	return conditionalKey;
}
//-------------------------------------------------------------------
protected KeyEvent makeCycledKeyEvent(KeyEvent rxed, int keyToReturn, int modifiers)
//-------------------------------------------------------------------
{
	modifiers |= IKeys.CONDITIONAL;
	if (conditionalKey != null) modifiers |= IKeys.REPLACEMENT;
	KeyEvent ret = makeKeyEvent(rxed,keyToReturn,modifiers);
	timeConditionalKey(ret,keyTime);
	return ret;
}

//-------------------------------------------------------------------
protected void timeConditionalKey(KeyEvent ke,int keyTime)
//-------------------------------------------------------------------
{
	conditionalTime = mApp.mainApp.requestTimer(this,keyTime);
	conditionalKey = ke;
}
//-------------------------------------------------------------------
protected void cancelConditionalKey()
//-------------------------------------------------------------------
{
	if (conditionalTime != 0) ticked(conditionalTime,0);
}
//===================================================================
public void ticked(int id, int howLate)
//===================================================================
{
	if (id == conditionalTime){
		conditionalTime = 0;
		conditionalKey.modifiers &= ~IKeys.CONDITIONAL;
		conditionalKey.modifiers |= IKeys.REPLACEMENT;
		dispatch(conditionalKey);
		conditionalKey = null;
	}
}

//-------------------------------------------------------------------
protected void dispatch(KeyEvent ev)
//-------------------------------------------------------------------
{
	((Control)ev.target).onKeyEvent(ev);
}
//##################################################################
}
//##################################################################

