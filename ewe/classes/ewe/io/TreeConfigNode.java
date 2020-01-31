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
package ewe.io;
import ewe.sys.*;
import ewe.data.*;
import ewe.util.*;

//##################################################################
public class TreeConfigNode extends ewe.data.LiveTreeNode implements HasProperties, LocalResource{
//##################################################################
public String name = "/";
public PropertyList properties = new PropertyList();
public PropertyList getProperties(){return properties;}

public String _fields = "name,properties";
//===================================================================
public String getName() {return name;}
//===================================================================
public TreeConfigNode(){}
//===================================================================

public TreeConfigNode(String name)
//===================================================================
{
	this.name = name;
}
//===================================================================
public String toString()
//===================================================================
{
	return name+", "+properties;
}
//===================================================================
public Object get(int value,Object defaultValue)
//===================================================================
{
	return get(ewe.sys.Convert.toString(value),defaultValue);
}
//===================================================================
public Object get(String value,Object defaultValue)
//===================================================================
{
	return properties.getValue(value,defaultValue);
}
//##################################################################
}
//##################################################################

