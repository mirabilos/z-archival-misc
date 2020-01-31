/*
Copyright (c) 2001 Michael L Brereton  All rights reserved.

This software is furnished under the Gnu General Public License, Version 2, June 1991,
and may be used only in accordance with the terms of that license. This source code
must be distributed with a copy of this license. This software and documentation,
and its copyrights are owned by Michael L Brereton and are protected by copyright law.

If this notice is followed by a Wabasoft Copyright notice, then this software
is a modified version of the original as provided by Wabasoft. Wabasoft also
retains all rights as stipulated in the Gnu General Public License. These modifications
were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May
2001.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.
*/

package ewe.ui.formatted;
import ewe.ui.*;
import ewe.util.*;
import ewe.fx.*;

/**
* A HotSpot represents a Hyperlink to a location within this document (if the link starts with '#')
* or to another document. It is also used to represent a location within a document that another
* HotSpot can link to (in which case it is effectively invisible).<p>
*
* The hyperlink that the HotSpot refers to is stored in the "data" member variable as a String.
* If this String starts with '!' then it is a book mark in the document that other hyperlinks
* will refer to. If it does not start with '!' then it is a clickable hyperlink to another
* location or document.
**/

	//##################################################################
	public class HotSpot extends TextFormatter{
	//##################################################################
	public static Color hotColor = new Color(0,0,255);
	{
		cursor = ewe.sys.Vm.HAND_CURSOR;
		color = hotColor;
	}

	//===================================================================
	public HotSpot()
	//===================================================================
	{
	}
	//===================================================================
	public HotSpot(int line,int character,int length)
	//===================================================================
	{
		super(line,character,length);
	}
		/**
	 * Returns if this need to be applied at the start or during the line.
	 */
	//===================================================================
	public boolean applyBefore()
	//===================================================================
	{
		return true;
	}
/*
	//===================================================================
	public Object getToolTip()
	//===================================================================
	{
		Object got = super.getToolTip();
		if (got != null) return got;
		return (cursor != 0) ? data.toString() : null;
	}
*/
	//##################################################################
	}
	//##################################################################

