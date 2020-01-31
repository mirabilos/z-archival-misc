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
import ewe.data.PropertyList;
import ewe.sys.Handle;
import ewe.graphics.AniImage;

	//##################################################################
	class TextPosition{
	//##################################################################
	// Do not move these next three -------------------
	DisplayLine line;
	int lineIndex;
	int characterIndex;
	//-------------------------------------------------
	//===================================================================
	public TextPosition(){}
	//===================================================================
	public TextPosition(mTextPad pad)
	//===================================================================
	{
		line = pad.getLine(0);
		lineIndex = characterIndex = 0;
	}
	//===================================================================
	public TextPosition(mTextPad pad,int lineIndex,int charIndex)
	//===================================================================
	{
		line = pad.getLine(lineIndex);
		this.lineIndex = lineIndex;
		this.characterIndex = charIndex;
	}
	//===================================================================
	TextPosition getCopy()
	//===================================================================
	{
		TextPosition tp = new TextPosition();
		tp.copyFrom(this);
		return tp;
	}
	//===================================================================
	void copyFrom(TextPosition tp)
	//===================================================================
	{
		line = tp.line;
		lineIndex = tp.lineIndex;
		characterIndex = tp.characterIndex;
	}
	native boolean nativeFindCharacter(int indexOfCharacter);
	boolean hasNative = true;
	//===================================================================
	boolean findCharacter(int indexOfCharacter)
	//===================================================================
	{
		if (hasNative) try{
			return nativeFindCharacter(indexOfCharacter);
		}catch(SecurityException se){
			hasNative = false;
		}catch(UnsatisfiedLinkError ue){
			hasNative = false;
		}
		if (characterIndex > indexOfCharacter) return false;
		while(true){
			int tl = line.trueLength();
			if (characterIndex+tl > indexOfCharacter && tl != 0)
				return true;
			characterIndex += tl;
			lineIndex++;
			if (line.next == null) return false;
			line = (DisplayLine)line.next;
		}
	}
	//===================================================================
	boolean moveToNextLine()
	//===================================================================
	{
		lineIndex++;
		characterIndex += line.trueLength();
		line = (DisplayLine)line.next;
		return line != null;
	}
	//##################################################################
	}
	//##################################################################

