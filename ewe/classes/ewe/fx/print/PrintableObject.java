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
package ewe.fx.print;
import ewe.sys.Handle;
import ewe.fx.Rect;
import ewe.fx.Graphics;
import ewe.fx.Pen;
import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.FontTools;
/**
A very useful base class for implementing a Printable object, which uses
PrintPage objects to paint to each page.
**/
//##################################################################
public abstract class PrintableObject implements Printable{
//##################################################################

/**
This is 360 by default, but you can change it to any value you want. It is used
by getXdpiFor() and getYdpiFor().
**/
protected double drawDPI = 360;

protected double defaultBlockSize = 3*72;

/**
By default whenever a new page is started a PrintPage is created that covers the entire
area. However if this variable is set true, the the PrintPage will cover the imageable area
only.
**/
protected boolean useImageableAreaOnly = false;
/**
This is the PageFormat for the page being printed now.
**/
protected PageFormat currentPageFormat;
/**
This is the PageRect that is currently being drawn on.
**/
protected PageRect currentPageRect;
/**
This is the PrintPage being used for the current page.
**/
protected PrintPage currentPrintPage;
/**
Return the current page imageable area in points. The x and y co-ordinats of the
returned PointRect will always be 0,0.
**/
//-------------------------------------------------------------------
protected PointRect getCurrentImageableArea()
//-------------------------------------------------------------------
{
	return new PointRect(currentPageFormat.imageableX, currentPageFormat.imageableY, currentPageFormat.imageableWidth, currentPageFormat.imageableHeight);
}
/**
Return the full size of the current page in points. The x and y co-ordinats of the
returned PointRect will always be 0,0.
**/
//-------------------------------------------------------------------
protected PointRect getCurrentFullPage()
//-------------------------------------------------------------------
{
	return new PointRect(0,0,currentPageFormat.fullPageWidth,currentPageFormat.fullPageHeight);
}
//-------------------------------------------------------------------
protected double getXdpiFor(int pageIndex) {return drawDPI;}
//-------------------------------------------------------------------
//-------------------------------------------------------------------
protected double getYdpiFor(int pageIndex) {return drawDPI;}
//-------------------------------------------------------------------
/**
This should return the area on the specified page that you want to draw
on. By default this will return a PointRect equal to the entire area of the
page, unless useImageableAreaOnly is true, in which case it will cover the
imageable area of the currentPageFormat.
**/
//-------------------------------------------------------------------
protected PointRect getAreaFor(int pageIndex)
//-------------------------------------------------------------------
{
	return useImageableAreaOnly ? getCurrentImageableArea() : getCurrentFullPage();
}
/**
By default this returns 0.
**/
//-------------------------------------------------------------------
protected int getHintsFor(int pageIndex)
//-------------------------------------------------------------------
{
	return 0;
}
/**
This is done at the start of each page.
By default this method will create a PrintPage that will cover the area returned by getAreaFor()
using the dpis as returned by getXdpiFor() and getYdpiFor(), but you can override this
to get a PrintPage in a different way.
**/
//-------------------------------------------------------------------
protected PrintPage getPrintPageFor(PrintSurface printSurface, int pageIndex)
//-------------------------------------------------------------------
{
	PointRect pr = getAreaFor(pageIndex);
	PrintPage pp = new PrintPage(printSurface,pr.x,pr.y,pr.width,pr.height,getXdpiFor(pageIndex),getYdpiFor(pageIndex),getHintsFor(pageIndex));
	pp.requestBlockSize(defaultBlockSize,defaultBlockSize);
	return pp;
}
/**
Scale the specified PointRect to integer pixel co-ordinates for the current
PageRect area.
**/
//-------------------------------------------------------------------
protected Rect scaleToPixels(PointRect pr, Rect dest)
//-------------------------------------------------------------------
{
	return currentPrintPage.scaleToPixels(pr,dest);
}
/**
Return if the specified area given in Points, relative to the top-left point
of the current print area (as selected by getAreaFor) is within the current
PageRect buffer. If it is not then you should not attempt to draw anything within that
area.
**/
//-------------------------------------------------------------------
protected boolean isWithinCurrentArea(PointRect pr)
//-------------------------------------------------------------------
{
	return currentPrintPage.isWithin(currentPageRect,pr);
}

private PointRect currentArea = new PointRect();
/**
Get the current area within the current PrintPage (not within the current page).
**/
//-------------------------------------------------------------------
protected PointRect getCurrentArea()
//-------------------------------------------------------------------
{
	return currentPrintPage.getPageRectArea(currentPageRect,currentArea);
}
/**
Scale a dimension, width or height value from point size to pixels for the
current PrintPage. In this case the result will always be at least 1 pixel.
**/
//-------------------------------------------------------------------
protected int scaleDimension(double pointSize)
//-------------------------------------------------------------------
{
	return currentPageRect.scaleDimension(pointSize);
}
/**
A Pen size that is greater than 1 will result in lines that extend to either side
of the actual line. This method will adust the bounds of a Rect such that when
it is drawn with a specific pen size (in points)
**/
//-------------------------------------------------------------------
protected Rect adjustForPen(double penSize, Rect source, Rect destination)
//-------------------------------------------------------------------
{
	int sz = scaleDimension(penSize);
	int ex = (sz-1)/2;
	int x = source.x+ex, y = source.y+ex, width = source.width-(sz-1), height = source.height-(sz-1);
	if (destination == null) destination = new Rect();
	return destination.set(x,y,width,height);
}
//-------------------------------------------------------------------
protected void adjustForPen(double penSize, Rect source)
//-------------------------------------------------------------------
{
	adjustForPen(penSize, source, source);
}

/**
This is the main method you must override. The default implementation
draws a box with diagonals across the printable area of the page.
**/
//-------------------------------------------------------------------
protected boolean print(Handle h, Graphics g, int pageIndex)
//-------------------------------------------------------------------
{
	double penSize = 9;
	int sc = scaleDimension(penSize);
	Rect r = scaleToPixels(getCurrentImageableArea(),null);
	g.setPen(new Pen(Color.Black,Pen.SOLID,sc));
	adjustForPen(penSize,r);
	if (true){
		g.drawRect(r.x,r.y,r.width,r.height);
		g.drawLine(r.x,r.y,r.x+r.width,r.y+r.height);
		g.drawLine(r.x,r.y+r.height,r.x+r.width,r.y);
	}
	Font f = ewe.ui.mApp.findFont("fixed");
	f = FontTools.getFontForHeight(scaleDimension(0.25*72),g.getFontMetrics(f));
	g.setFont(f);
	FontMetrics fm = g.getFontMetrics(f);
	String txt = "Printing Page "+(pageIndex+1);
	int w = fm.getTextWidth(txt);
	int offset = (r.width-w)/2;
	g.drawText(txt,r.x+offset,r.y+sc);
	return true;
}
//===================================================================
public final boolean print(Handle handle,PrintSurface printSurface,PageFormat page,int pageIndex)
//===================================================================
{
	if (handle == null) handle = new Handle();
	currentPageFormat = page;
	currentPrintPage = getPrintPageFor(printSurface,pageIndex);
	for (PageRect pr = currentPrintPage.getNext(); pr != null; pr = currentPrintPage.getNext()){
		if (handle.shouldStop) return false;
		currentPageRect = pr;
		if (!print(handle,pr.getGraphics(),pageIndex)) return false;
	}
	return true;
}
/**
By default this returns UNKNOWN_NUMBER_OF_PAGES.
**/
//===================================================================
public int countPages(PageFormat pf){return UNKNOWN_NUMBER_OF_PAGES;}
//===================================================================

/**
You must override to indicate whether a page is valid or not. Returning
false will end printing. The pageIndex starts from 0 for the first page.
**/
//===================================================================
public abstract boolean validatePage(PageFormat pf, int pageIndex);
//===================================================================

//##################################################################
}
//##################################################################

