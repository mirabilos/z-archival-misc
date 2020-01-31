/*
Note - This is the Linux version of VMApps.java
*/
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
import ewe.util.*;
import ewex.registry.*;

//##################################################################
public class VMApps extends ewe.data.LiveObject{
//##################################################################

public Vector apps = new Vector();

public String _fields = "apps";

//-------------------------------------------------------------------
protected TextEncoder encode(TextEncoder te)
//-------------------------------------------------------------------
{
	encodeFields(_fields,te,"VMApps");
	return super.encode(te);
}
//-------------------------------------------------------------------
protected TextDecoder decode(TextDecoder te)
//-------------------------------------------------------------------
{
	decodeFields(_fields,te,"VMApps");
	return super.decode(te);
}

//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int options)
//===================================================================
{
}
//===================================================================
public boolean read()
//===================================================================
{
	try{
		return ewe.io.IO.getConfigInfo(this,"Ewesoft\\EweApps");
	}catch(Exception e){
		return false;
	}
}
//===================================================================
public boolean save()
//===================================================================
{
	try{
		ewe.io.IO.saveConfigInfo(this,"Ewesoft\\EweApps");
		return true;
	}catch(ewe.io.IOException e){
		return false;
	}
}

//##################################################################
}
//##################################################################

