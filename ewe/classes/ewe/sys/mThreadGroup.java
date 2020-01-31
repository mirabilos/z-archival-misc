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
package ewe.sys;
import ewe.security.mSecurityManager;

//##################################################################
public class mThreadGroup{
//##################################################################

protected String name;
private mThreadGroup group;
private ewe.util.Vector elements = new ewe.util.Vector();

int maxPriority = mThread.MAX_PRIORITY;
boolean daemon = false;

//===================================================================
public mThreadGroup(String name)
//===================================================================
{
	this(null,name);
}
//===================================================================
public mThreadGroup(mThreadGroup group,String name)
//===================================================================
{
	this.group = group == null ? null : verifyThreadGroup(group);
	this.name = name;
	if (group != null) group.add(this);
}

//-------------------------------------------------------------------
static mThreadGroup verifyThreadGroup(mThreadGroup toAssign)
//-------------------------------------------------------------------
{
	SecurityManager mg = System.getSecurityManager();
	if (mg instanceof mSecurityManager)
		return ((mSecurityManager)mg).checkAssignMThreadGroup(toAssign);
	else
		return toAssign;
}
//-------------------------------------------------------------------
void add(Object group)
//-------------------------------------------------------------------
{
	if (group != null) elements.add(group);
}
//-------------------------------------------------------------------
void ended(Object who)
//-------------------------------------------------------------------
{
	if (who != null) elements.remove(who);
	if (elements.size() == 0)
		if (group != null)
			group.ended(this);
}
//===================================================================
public void uncaughtException(mThread thread,Throwable t)
//===================================================================
{
	if (group != null) group.uncaughtException(thread,t);
	else {
		t.printStackTrace();
	}
}
//===================================================================
public int activeCount()
//===================================================================
{
	int c = 0;
	for (int i = 0; i<elements.size(); i++){
		Object obj = elements.get(i);
		if (obj instanceof mThread) c++;
		else if (obj instanceof mThreadGroup) c += ((mThreadGroup)obj).activeCount();
	}
	return c;
}
//===================================================================
public int activeGroupCount()
//===================================================================
{
	int c = 0;
	for (int i = 0; i<elements.size(); i++){
		Object obj = elements.get(i);
		if (obj instanceof mThreadGroup) {
			c++;
			c += ((mThreadGroup)obj).activeCount();
		}
	}
	return c;
}
//-------------------------------------------------------------------
private int enumerate(mThread [] list,int index,boolean recurse)
//-------------------------------------------------------------------
{
	for (int i = 0; i<elements.size() && index < list.length; i++){
		Object obj = elements.get(i);
		if (obj instanceof mThread)
			list[index++] = (mThread)obj;
	}
	if (recurse){
		for (int i = 0; i<elements.size() && index < list.length; i++){
			Object obj = elements.get(i);
			if (obj instanceof mThreadGroup)
				index = ((mThreadGroup)obj).enumerate(list,index,recurse);
		}
	}
	return index;
}

//===================================================================
public void enumerate(mThread [] list,boolean recurse)
//===================================================================
{
	enumerate(list,0,recurse);
}
//===================================================================
public void enumerate(mThread [] list)
//===================================================================
{
	enumerate(list,0,true);
}
//-------------------------------------------------------------------
private int enumerate(mThreadGroup [] list,int index,boolean recurse)
//-------------------------------------------------------------------
{
	for (int i = 0; i<elements.size() && index < list.length; i++){
		Object obj = elements.get(i);
		if (obj instanceof mThreadGroup)
			list[index++] = (mThreadGroup)obj;
	}
	if (recurse){
		for (int i = 0; i<elements.size() && index < list.length; i++){
			Object obj = elements.get(i);
			if (obj instanceof mThreadGroup)
				index = ((mThreadGroup)obj).enumerate(list,index,recurse);
		}
	}
	return index;
}
//===================================================================
public void enumerate(mThreadGroup [] list,boolean recurse)
//===================================================================
{
	enumerate(list,0,recurse);
}
//===================================================================
public void enumerate(mThreadGroup [] list)
//===================================================================
{
	enumerate(list,0,true);
}
//===================================================================
public String getName()
//===================================================================
{
	return name;
}
//===================================================================
public mThreadGroup getParent()
//===================================================================
{
	return group;
}
//===================================================================
public String toString()
//===================================================================
{
	return "mThreadGroup: "+name;
}
//===================================================================
public boolean parentOf(mThreadGroup g)
//===================================================================
{
	for (g = g.group;;g = g.group)
		if (g == null) return false;
		else if (g == this) return true;
}
//===================================================================
public void setMaxPriority(int maxPriority)
//===================================================================
{
	this.maxPriority = maxPriority;
}
//===================================================================
public int getMaxPriority()
//===================================================================
{
	return maxPriority;
}
//===================================================================
public void setDaemon(boolean daemon)
//===================================================================
{
	this.daemon = daemon;
}
//===================================================================
public boolean isDaemon()
//===================================================================
{
	return daemon;
}
//##################################################################
}
//##################################################################

