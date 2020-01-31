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
import ewe.io.*;
import ewe.sys.*;
import ewe.reflect.*;

//##################################################################
public class DataTableSpecs implements Encodable{
//##################################################################

public int myOID;
public long lastSyncTime;
public int lastAssigned;
public long [] syncTimes = new long[0];

//===================================================================
public static int getNewOID()
//===================================================================
{
	Time t = new Time();
	int val = (t.month*31+t.day)*24*60*60;
	val += t.hour*60*60;
	val += t.minute*60;
	val += t.second;
	val <<= 5;
	ewe.sys.Math.srand(t.millis);
	val |= (ewe.sys.Math.rand() % 32);
	return val;
}

//-------------------------------------------------------------------
private int findSync(int remoteDatabaseID)
//-------------------------------------------------------------------
{
	for (int i = 0; i<syncTimes.length-1; i+=2)
		if ((int)syncTimes[i] == remoteDatabaseID) return i;
	return -1;
}
//===================================================================
public void setSynchronizedTime(int remoteDatabaseID,ewe.sys.Time syncTime)
//===================================================================
{
	int idx = findSync(remoteDatabaseID);
	if (idx == -1){
		int nl = syncTimes.length+2;
		long [] s = new long[nl];
		ewe.sys.Vm.copyArray(syncTimes,0,s,0,syncTimes.length);
		syncTimes = s;
		idx = nl-2;
	}
	syncTimes[idx] = remoteDatabaseID;
	syncTimes[idx+1] = syncTime == null ? 0 : syncTime.getEncodedTime();
}
//===================================================================
public Time getSynchronizedTime(int remoteDatabaseID)
//===================================================================
{
	int idx = findSync(remoteDatabaseID);
	if (idx == -1) return null;
	long value = syncTimes[idx+1];
	if (value == 0) return null;
	Time t = new Time();
	t.setEncodedTime(value);
	return t;
}
//##################################################################
}
//##################################################################

