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
import ewe.data.PropertyList;
import ewe.fx.Color;
import ewe.fx.IImage;
import ewe.ui.formatted.FormattedTextMaker;
import ewe.ui.formatted.TextDisplay;
//##################################################################
public class HtmlDisplay extends TextDisplay  {
//##################################################################

{
	forcedActualWidth = 5000;
	spacing = 2;
}
private FormattedTextMaker maker;

public ewe.data.PropertyList headerData = new ewe.data.PropertyList();
public ewe.data.PropertyList bodyData = new ewe.data.PropertyList();

//===================================================================
public HtmlDisplay(){}
//===================================================================
public HtmlDisplay(int rows,int columns)
//===================================================================
{
	super(rows,columns);
}
/**
* Call this before setting properties for the HTML decoder.
**/
//===================================================================
public void startHtml()
//===================================================================
{
	if (maker != null) maker.removeFrom(this);
	maker = new FormattedTextMaker();
}
/**
* Make sure you call startHtml(), before calling this.
* After you do that you can then set properties for the decoder. These
* include:<dl>
* <dt>"documentRoot"<dd>The root of the document. Images with relative path
* names will be searched for relative to this path.
* </dl>
**/
//===================================================================
public ewe.data.PropertyList getDecoderProperties()
//===================================================================
{
	if (maker != null) return maker.properties;
	else return ewe.data.PropertyList.nullPropertyList;
}
//===================================================================
public void addHtml(String htmlText,ewe.sys.Handle h)
//===================================================================
{
	maker.parseHtml(this,htmlText,h);
}
//===================================================================
public FormattedTextMaker endHtml()
//===================================================================
{
	maker.endHtml();
	return endMaker();
}

//-------------------------------------------------------------------
FormattedTextMaker endMaker()
//-------------------------------------------------------------------
{
	headerData = maker.headerData;
	bodyData = maker.bodyData;
	try{
		pageColor = (Color)PropertyList.getValue(bodyData,"background",Color.White);
		foreGround =(Color)PropertyList.getValue(bodyData,"foreground",Color.Black);
		backgroundImage = (IImage)PropertyList.getValue(bodyData,"backgroundImage",null);
	}catch(Exception e){}
	int was = modify(Invisible,0);
	maker.addTo(this);
	scrollTo(0,false);
	restore(was,Invisible);
	repaintDataNow();

	//clearHistory();
	//markHistory();
	return maker;
}
//===================================================================
public void displayPropertiesChanged()
//===================================================================
{
	try{
		pageColor = (Color)PropertyList.getValue(bodyData,"background",Color.White);
		foreGround =(Color)PropertyList.getValue(bodyData,"foreground",Color.Black);
		backgroundImage = (IImage)PropertyList.getValue(bodyData,"backgroundImage",null);
	}catch(Exception e){}
	super.displayPropertiesChanged();
}
//===================================================================
public void setHtml(String htmlText)
//===================================================================
{
	setHtml(htmlText,null,new ewe.sys.Handle());
}
//===================================================================
public FormattedTextMaker setHtml(String htmlText,ewe.data.PropertyList properties,ewe.sys.Handle h)
//===================================================================
{
	startHtml();
	if (properties != null) getDecoderProperties().set(properties);
	addHtml(htmlText,h);
	return endHtml();
}

//===================================================================
public void setHtml(FormattedTextMaker maker,ewe.data.PropertyList properties)
//===================================================================
{
	if (this.maker != null) this.maker.removeFrom(this);
	this.maker = maker;
	if (properties != null) getDecoderProperties().set(properties);
	endMaker();
}

//===================================================================
public void formClosing()
//===================================================================
{
	super.formClosing();
	if (maker != null) maker.removeFrom(this);
}
//===================================================================
public void setPlainText(String text)
//===================================================================
{
	if (maker != null) maker.removeFrom(this);
	setText(text);
	//markHistory();
}
//##################################################################
}
//##################################################################

