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
package ewe.datastore;
import ewe.util.*;
import ewe.io.IOException;

//##################################################################
class Changes{
//##################################################################
IntArray changes = new IntArray();
Vector entries = new Vector();
DataStorage storage;
//===================================================================
Changes(DataStorage ds)
//===================================================================
{
	storage = ds;
}
//===================================================================
public Changes clear() {changes.clear(); entries.clear(); return this;}
//===================================================================
//===================================================================
public void monitor(DataEntry de)
//===================================================================
{
	if (de == null) return;
	if (entries.find(storage.finder.set(de.myLocation)) == null)
		entries.add(de.getCopy());
}
//===================================================================
public void check(int was,int is,int offset)
//===================================================================
{
	if (was != is) {
		changes.add(offset);
		changes.add(is);
	}
}

//-------------------------------------------------------------------
boolean implementChanges()
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Implementing changes...");
	for (int i = 0; i<changes.length; i+=2){
		if (storage.writeIntAt(changes.data[i],changes.data[i+1]) != 4)
			return false;
	}
	if (!storage.flush()) return false;
	if (storage.writeIntAt(16,0) != 4) return false;
	if (!storage.flush()) return false;
	return true;
}

//===================================================================
boolean readChanges() throws IOException
//===================================================================
{
	changes.clear();
	if (!storage.stream.seek(16)) throw new IOException("Error reading database.");
	if (storage.readAnInt() == 0) return false;
	if (!storage.stream.seek(24)) throw new IOException("Error reading database.");
	while(true){
		int loc = storage.readAnInt();
		if (loc == 0) break;
		changes.add(loc);
		changes.add(storage.readAnInt());
	}
	return true;
}

//===================================================================
public boolean doChanges()
//===================================================================
{
	changes.clear();

	for (int i = 0; i<entries.size(); i++){
		DataEntry de = (DataEntry)entries.get(i);
		check(de.next,de.copiedFrom.next,de.myLocation+0);
		check(de.prev,de.copiedFrom.prev,de.myLocation+4);
		check(de.parent,de.copiedFrom.parent,de.myLocation+8);
		check(de.children,de.copiedFrom.children,de.myLocation+12);
		check(de.dataLocation,de.copiedFrom.dataLocation,de.myLocation+16);
		check(de.allocatedLength,de.copiedFrom.allocatedLength,de.myLocation+20);
	}
	//
	int loc = 24;
	for (int i = 0; i<changes.length; i++){
		if (storage.writeIntAt(loc,changes.data[i]) != 4) return false;
		loc += 4;
	}
	if (storage.writeIntAt(loc,0) != 4) return false;
	if (!storage.flush()) return false;
	//
	if (storage.writeIntAt(16,1) != 4) return false;
	if (!storage.flush()) return false;
	//
	//if (true) return false;
	//
	return implementChanges();
}

//##################################################################
}
//##################################################################

