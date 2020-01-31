/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
import ewe.ui.*;
/**
* This is provided on some platforms that allow it and only some of the fields
* will be relevant on some. It is modeled after the Win32/WinCE MEMORYSTATUS structure.
**/
//##################################################################
public class MemoryStatus{
//##################################################################
/**
* This will be a value from 0 to 100 indicating a percentage of used memory.
**/
public int memoryLoad;
/**
* The total number of bytes in physical memory.
**/
public int totalPhysical;
/**
* The available number of bytes in physical memory.
**/
public int availablePhysical;
/**
* The total number of virtual memory bytes.
**/
public int totalVirtual;
/**
* The available number of virtual memory bytes.
**/
public int availableVirtual;

/**
* This will return a MemoryStatus that has the difference in the memory values
* of this MemoryStatus object, and the current memory values as reported by the system.
* @return A new MemoryStatus or null if the current memory
* status cannot be determined.
*/
//===================================================================
public MemoryStatus getChange()
//===================================================================
{
	MemoryStatus ms = ewe.sys.Vm.getSystemMemoryStatus();
	if (ms == null) return null;
	ms.memoryLoad -= memoryLoad;
	ms.totalPhysical -= totalPhysical;
	ms.availablePhysical -= availablePhysical;
	ms.totalVirtual -= totalVirtual;
	ms.availableVirtual -= availableVirtual;
	return ms;
}
//===================================================================
void copyFrom(MemoryStatus ms)
//===================================================================
{
	memoryLoad = ms.memoryLoad;
	totalPhysical = ms.totalPhysical;
	availablePhysical = ms.availablePhysical;
	totalVirtual = ms.totalVirtual;
	availableVirtual = ms.availableVirtual;
}
/**
* Refresh the values in this MemoryStatus with a new values.
**/
//===================================================================
public boolean refresh()
//===================================================================
{
	MemoryStatus ms = ewe.sys.Vm.getSystemMemoryStatus();
	if (ms == null) return false;
	copyFrom(ms);
	return true;
}
/**
* This will display the memory status on the screen.
**/
/*
//===================================================================
public static void main(String args[])
//===================================================================
{
	Vm.startEwe(args);
	MemoryStatus ms = ewe.sys.Vm.getSystemMemoryStatus();
	if (ms == null) ms = new MemoryStatus();
	final MemoryStatus m2 = ms;
	ewe.ui.Editor ed = new ewe.ui.Editor(){
		{
			setFields(m2);
			title = "Memory Status";
			ButtonBar bb = new ButtonBar();
			addLast(bb).setCell(bb.HSTRETCH);
			bb.addNext(addField(new mButton("Refresh"),"refresh"));
			bb.addNext(addField(new mButton("Get Change"),"change"));
		}
		public void action(String name,Editor ed){
			if (name.equals("refresh")){
				m2.refresh();
				toControls();
			}
			if (name.equals("change")){
				MemoryStatus ch = m2.getChange();
				if (ch != null) m2.copyFrom(ch);
				toControls();
			}
		}
	};
	ed.title = "Memory Status";
	ed.execute();
	Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

