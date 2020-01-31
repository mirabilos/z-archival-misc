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
class FormattedTextMakerBase{
//##################################################################


final static String nbsp = ""+(char)0xa0;
static Object [] replacements = {"&gt;",">","&lt;","<","&nbsp;",nbsp,"&copy;",""+(char)169,"&quot;","\"","&amp;","&"};
static Object [] htmlReplacements = {"\r"," ","\n"," ","\t"," ","  "," "};
static Object [] preReplacements = {" ",nbsp};

static String amps = ">gt|<lt|"+(char)160+"nbsp|"+(char)169+"copy|\"quot|&amp";
static Vector ampList;
static final String colors = "black000000green008000silverC0C0C0lime00FF00"+
"gray808080olive808000"+
"whiteFFFFFFyellowFFFF00"+
"maroon800000navy000080"+
"redFF0000blue0000FF"+
"purple800080teal008080"+
"fuchsiaFF00FFaqua00FFFF";
protected static final int LF_ORDERED_LIST = 0x80000000;
public static final int BOLD = 1;
public static final int ITALIC = 2;
public static final int UNDERLINE = 3;
public static final int HYPERLINK = 9;
public static final int ANCHOR = 10;
public static final int SPAN = 12;
public static final int FONT = 14;
public static final int BIG = 15;
public static final int SMALL = 16;
public static final int TELETYPE = 17;
public static final int EM = 18;
public static final int STRONG = 19;
public static final int DFN = 20;
public static final int IMAGE = 21;

//public static final int FIRST_SINGLE_LINE = 100;
//public static final int FIRST_BLOCK_LEVEL = 100;

public static final int BLOCK_LEVEL = 0x08000000;
public static final int STARTS_FRESH = 0x04000000;
public static final int IS_PARAGRAPH = 0x02000000;

public static final int CENTERED = 100|BLOCK_LEVEL;
public static final int HEADING1 = 101|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING2 = 102|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING3 = 103|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING4 = 104|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING5 = 105|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int HEADING6 = 106|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int TITLE = 107|BLOCK_LEVEL;
public static final int HR = 108|BLOCK_LEVEL;
public static final int PARAGRAPH = 109|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int DIV = 110|BLOCK_LEVEL;
public static final int PREFORMAT = 111|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int BLOCKQUOTE = 112|BLOCK_LEVEL;
public static final int BLOCKIMAGE = 113|BLOCK_LEVEL;
public static final int ADDRESS = 114|BLOCK_LEVEL;
public static final int BODY = 115|BLOCK_LEVEL;
public static final int TABLE = 116|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int TROW = 117|BLOCK_LEVEL;//|IS_PARAGRAPH;
public static final int TCELL = 118|BLOCK_LEVEL;//|IS_PARAGRAPH;
//public static final int LAST_SINGLE_LINE = 199;

public static final int TEST = 211|BLOCK_LEVEL;

public static final int UL = 212|BLOCK_LEVEL;
public static final int OL = 213|BLOCK_LEVEL;
public static final int ULI = 214|BLOCK_LEVEL|STARTS_FRESH;
public static final int OLI = 215|BLOCK_LEVEL|STARTS_FRESH;
public static final int DL = 216|BLOCK_LEVEL|IS_PARAGRAPH;
public static final int DT = 217|BLOCK_LEVEL|STARTS_FRESH;
public static final int DD = 218|BLOCK_LEVEL;
//public static final int LAST_BLOCK_LEVEL = 299;

private static int[] theValues = {BOLD,ITALIC,UNDERLINE,HYPERLINK,ANCHOR,SPAN,FONT,BIG,SMALL,TELETYPE,EM,STRONG,DFN,IMAGE,BLOCK_LEVEL,STARTS_FRESH,IS_PARAGRAPH,CENTERED,HEADING1,HEADING2,HEADING3,HEADING4,HEADING5,HEADING6,TITLE,HR,PARAGRAPH,DIV,PREFORMAT,BLOCKQUOTE,BLOCKIMAGE,ADDRESS,BODY,TABLE,TROW,TCELL,TEST,UL,OL,ULI,OLI,DL,DT,DD};
private static String[] theNames = {"BOLD","ITALIC","UNDERLINE","HYPERLINK","ANCHOR","SPAN","FONT","BIG","SMALL","TELETYPE","EM","STRONG","DFN","IMAGE","BLOCK_LEVEL","STARTS_FRESH","IS_PARAGRAPH","CENTERED","HEADING1","HEADING2","HEADING3","HEADING4","HEADING5","HEADING6","TITLE","HR","PARAGRAPH","DIV","PREFORMAT","BLOCKQUOTE","BLOCKIMAGE","ADDRESS","BODY","TABLE","TROW","TCELL","TEST","UL","OL","ULI","OLI","DL","DT","DD"};
public String valueToName(int value)
{
for (int i = 0; i<theValues.length; i++)
	if (theValues[i] == value) return theNames[i];
return null;
}

//##################################################################
}
//##################################################################

