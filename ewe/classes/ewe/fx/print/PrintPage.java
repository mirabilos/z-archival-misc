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
import ewe.fx.Rect;
import ewe.fx.Graphics;
import ewe.sys.SystemResourceException;
/**
A PrintPage is used to help you draw a section of, or complete page, on a PrintSurface - usually
as provided by a PrinterJob.
It does this by requesting successive PageRect buffers from the PrintSurface such that it eventually
completely covers the area that you wish to draw on. The getNext() method is used to retrieve
successive PageRect sub-areas until the entire area is covered.
<p>
Additionally, each PageRect created has its Graphics translated such that drawing relative to an
origin of (0,0) each time will always be drawn relative to the top left corner of the
page section that the PrintPage covers.
**/
//##################################################################
public class PrintPage{
//##################################################################

private PrintSurface pj;
private double x,y,width,height,xdpi,ydpi;
private int hints;

/**
Return the PrintSurface being used by the PrintPage.
**/
//===================================================================
public PrintSurface getPrintSurface()
//===================================================================
{
	return pj;
}
/**
Create a PrintPage to cover a specific area on the PrintSurface with a specified x-DPI and y-DPI.
* @param pixelWidth The desired width of the page area in pixels.
* @param pixelHeight The desired height of the page area in pixels.
* @param p The PrintSurface.
* @param x The x position of the area from the left edge of the paper in Points (1/72 of an inch).
* @param y The y position of the area from the top edge of the paper in Points (1/72 of an inch).
* @param width The width of the area in Points (1/72 of an inch).
* @param height The height of the area in Points (1/72 of an inch).
* @param xDPI The desired DPI horizontally for drawing.
* @param yDPI The desired DPI vertically for drawing.
* @param hints any of the PrintSurface.HINT_XXX values OR'ed together.
**/
//===================================================================
public PrintPage(PrintSurface p, double x, double y, double width, double height, double xdpi, double ydpi, int hints)
//===================================================================
{
	pj = p;
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
	this.xdpi = xdpi;
	this.ydpi = ydpi;
	this.hints = hints & ~PrintSurface.HINT_DONT_RESIZE;
}
/**
Create a PrintPage to cover a specific area on the PrintSurface where the xDPI and
the yDPI are calculated such that they result in a pixel width and pixel height equal
to those specified as parameters.
* @param pixelWidth The desired width of the page area in pixels.
* @param pixelHeight The desired height of the page area in pixels.
* @param p The PrintSurface.
* @param x The x position of the area from the left edge of the paper in Points (1/72 of an inch).
* @param y The y position of the area from the top edge of the paper in Points (1/72 of an inch).
* @param width The width of the area in Points (1/72 of an inch).
* @param height The height of the area in Points (1/72 of an inch).
* @param hints any of the PrintSurface.HINT_XXX values OR'ed together.
*/
//===================================================================
public PrintPage(int pixelWidth, int pixelHeight,PrintSurface p, double x, double y, double width, double height, int hints)
//===================================================================
{
	this(p,x,y,width,height,(pixelWidth*72.0)/width,(pixelHeight*72.0)/height,hints);
}

/**
Returns true if the specified co-ordinate in Points is within the current PageRect. Note that
the x and y origin are relative to the top left of the area covered by the PrintPage, and not
the full page.
* @param pageRect the current PageRect.
* @param x the x co-ordinate in Points (1/72 of an inch).
* @param y the y co-ordinate in Points (1/72 of an inch).
* @return true if the specified co-ordinate in Points is within this PageRect.
*/
//===================================================================
public boolean isWithin(PageRect pageRect, double x, double y)
//===================================================================
{
	return pageRect.isWithin(x+this.x, y+this.y);
}
/**
Returns true if the specified rectangle in Points intersects the current PageRect. Note that
the x and y origin are relative to the top left of the area covered by the PrintPage, and not
the full page.
* @param pageRect the current PageRect.
* @param x the x co-ordinate in Points (1/72 of an inch).
* @param y the y co-ordinate in Points (1/72 of an inch).
* @param width the width in Points (1/72 of an inch).
* @param height the height in Points (1/72 of an inch).
* @return true if the specified rectangle in Points intersects with this PageRect.
*/
//===================================================================
public boolean isWithin(PageRect pageRect, double x, double y, double width, double height)
//===================================================================
{
	return pageRect.isWithin(x+this.x, y+this.y,width,height);
}
/**
Returns true if the specified rectangle in Points intersects the current PageRect. Note that
the x and y origin are relative to the top left of the area covered by the PrintPage, and not
the full page.
* @param pageRect the current PageRect.
* @param pointRect the area in Points.
* @return true if the specified rectangle in Points intersects with this PageRect.
*/
//===================================================================
public boolean isWithin(PageRect pageRect, PointRect area)
//===================================================================
{
	return pageRect.isWithin(area.x+this.x, area.y+this.y,area.width,area.height);
}
/**
Scale a PointRect containing Point dimensions into pixel dimensions for any PageRect
created by this PrintPage.
**/
//===================================================================
public Rect scaleToPixels(PointRect pr, Rect destination)
//===================================================================
{
	return pr.scaleToRect(destination,xdpi/72,xdpi/72);
}
/**
Return the area within the PrintPage as covered by the PageRect specified.
**/
//===================================================================
public PointRect getPageRectArea(PageRect pr,PointRect destination)
//===================================================================
{
	if (destination == null) destination = new PointRect();
	return destination.set(pr.getX()-x,pr.getY()-y,pr.getWidthOnPaper(),pr.getHeightOnPaper());
}
private double blockWidth = 1*72, blockHeight = 1*72;
/**
 * Call this before calling getNextRect() for the first time on the PrintPage - to suggest
 * the PageRect block size to use. This may not be used if it is too big.
 * @param blockWidth the suggested block width in Points (1/72 or an inch).
 * @param blockHeight the suggested block height in Points (1/72 or an inch).
 */
//===================================================================
public void requestBlockSize(double blockWidth, double blockHeight)
//===================================================================
{
	this.blockWidth = blockWidth;
	this.blockHeight = blockHeight;
}

private int phase = 0;

private PageRect current;
private int numRows, numCols;
private int curRow, curCol;
private double xOffset, yOffset;
private double rightStrip, bottomHeight, bottomWidth;

//-------------------------------------------------------------------
private PageRect translate(PageRect pr)
//-------------------------------------------------------------------
{
	pr.getGraphics().translate((int)((x-pr.getX())*pr.getXPointToPixelScale()),(int)((y-pr.getY())*pr.getYPointToPixelScale()));
	return pr;
}
/**
Call this method repeatedly until it returns null - indicating the page is complete.
Each time you call it, a section of the whole page will be returned as a PageRect.
The returned PageRect will have its Graphics translated such that
the co-ordinates 0,0 will always refer to the top-left pixel of the entire page area of the PrintPage.
<p>
Because of this, you can print an entire page by simply painting the exact same set of data to each
PageRect that you receive - or you can call the isWithin() method to determine if an area or point
is within the current PageRect - in which case there is no need to draw it.
**/
//===================================================================
public PageRect getNext()
//===================================================================
{
	if (current != null) pj.putPageRect(current);
 	else if (phase != 0) return null;
	//
	while(true){
		if (phase == 0){
			//
			// This is the first time it has been called.
			//
			if (blockWidth > width) blockWidth = width;
			if (blockHeight > height) blockHeight = height;
			current = pj.getPageRect(x,y,blockWidth,blockHeight,xdpi,ydpi,hints);
			if (current == null) throw new SystemResourceException("Can't create any page rect!");
			numRows = (int)(height/current.getHeightOnPaper());
			bottomHeight = height-numRows*current.getHeightOnPaper();
			numCols = (int)(width/current.getWidthOnPaper());
			rightStrip = width-numCols*current.getWidthOnPaper();
			bottomWidth = width-rightStrip;
			phase = 1;
			return translate(current);
		}else if (phase == 1){
			//
			// We are now doing the first phase, the main grid.
			//
			if (curCol == numCols-1){
				curCol = 0;
				if (curRow == numRows-1){
					//
					// Here we should move to phase 2, where we do the strip to the right.
					//
					pj.cancelPageRect(current);
					current = null;
					phase = 2;
					continue;
				}else
					curRow++;
			}else{
				curCol++;
			}
			double nx = curCol*current.getWidthOnPaper(), ny = curRow*current.getHeightOnPaper();
			pj.movePageRect(current,x+nx,y+ny);
			return translate(current);
		}else if (phase == 2){
			if (current == null){
				//
				// Now will the area on the right strip actually be equivalent to any pixels?
				//
				if ((int)((rightStrip*xdpi)/72.0) <= 0) {
					phase = 3;
					continue;
				}
				double bw = rightStrip, bh = blockHeight;
				if (bh > height) bh = height;
				current = pj.getPageRect(x+width-rightStrip,y,bw,bh,xdpi,ydpi,pj.HINT_DONT_RESIZE_WIDTH);
				if (current == null) throw new SystemResourceException("Can't create any page rect!");
				curRow = curCol = 0;
				numRows = (int)(height/current.getHeightOnPaper());
				return translate(current);
			}else{
				double hp = current.getHeightOnPaper();
				if (curRow == numRows-1){
					curRow++;// Now it is equal to numRows. If there is no extra strip then we will move to phase 3.
					double leftAtBottom = height-numRows*hp;
					if ((int)((leftAtBottom*ydpi)/72.0) > 0){
						// Yes there is one strip left.
						pj.cancelPageRect(current);
						current = null;
						current = pj.getPageRect(x+width-rightStrip,y+numRows*hp,rightStrip,leftAtBottom,xdpi,ydpi,pj.HINT_DONT_RESIZE);
						if (current == null) throw new SystemResourceException("Can't create any page rect!");
						return translate(current);
					}
				}
				if (curRow == numRows){
					pj.cancelPageRect(current);
					current = null;
					phase = 3;
					continue;
				}else{
					curRow++;
					pj.movePageRect(current,current.getX(),y+curRow*hp);
					return translate(current);
				}
			}
		}else if (phase == 3){
			if (current == null){
				//
				// Now will the area on the bottom strip actually be equivalent to any pixels?
				//
				if ((int)((bottomHeight*ydpi)/72.0) <= 0) {
					phase = 4;
					continue;
				}
				double bw = blockWidth, bh = bottomHeight;
				if (bw > bottomWidth) bw = bottomWidth;
				current = pj.getPageRect(x,y+height-bottomHeight,bw,bh,xdpi,ydpi,pj.HINT_DONT_RESIZE_HEIGHT);
				if (current == null) throw new SystemResourceException("Can't create any page rect!");
				curRow = curCol = 0;
				numCols = (int)(bottomWidth/current.getWidthOnPaper());
				return translate(current);
			}else{
				double wp = current.getWidthOnPaper();
				if (curCol == numCols-1){
					curCol++;// Now it is equal to numCols. If there is no extra strip then we will move to phase 4.
					double leftAtRight = bottomWidth-numCols*wp;
					if ((int)((leftAtRight*xdpi)/72.0) > 0){
						// Yes there is one strip left.
						pj.cancelPageRect(current);
						current = null;
						current = pj.getPageRect(x+(numCols*wp),y+height-bottomHeight,leftAtRight,bottomHeight,xdpi,ydpi,pj.HINT_DONT_RESIZE);
						if (current == null) throw new SystemResourceException("Can't create any page rect!");
						return translate(current);
					}
				}
				if (curCol == numCols){
					pj.cancelPageRect(current);
					current = null;
					phase = 4;
					continue;
				}else{
					curCol++;
					pj.movePageRect(current,x+curCol*wp,current.getY());
					return translate(current);
				}
			}
		}else {//if (phase == 4){
			return null;
		}
			//throw new IllegalStateException("The Mosaic is already finished.");
	}
}
//##################################################################
}
//##################################################################

