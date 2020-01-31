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

//##################################################################
public class PasswordDisplayMaker{
//##################################################################

public static String hidden = mInput.hidden;
public static char defaultPasswordCharacter = '*';

private String myHidden;
private char passwordCharacter;

//===================================================================
public PasswordDisplayMaker(char passwordCharacter)
//===================================================================
{
	this.passwordCharacter = passwordCharacter;
}
//===================================================================
public String getDisplay(int length)
//===================================================================
{
	return getDisplay(length,passwordCharacter);
}
//===================================================================
public String getDisplay(int length,char passwordCharacter)
//===================================================================
{
	int len = length;
	if (hidden == null || hidden.length() == 0) hidden = "*";
	if (hidden.charAt(0) != passwordCharacter){
		if (myHidden == null){
			char[] r = new char[10];
			for (int i = 0; i<r.length; i++)
				r[i] = passwordCharacter;
			myHidden = new String(r);
		}
 		while (myHidden.length() < len) myHidden += myHidden;
		return myHidden.substring(0,len);
	}
 	while (hidden.length() < len) hidden += hidden;
	return hidden.substring(0,len);
}

//##################################################################
}
//##################################################################

