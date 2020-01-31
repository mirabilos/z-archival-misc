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
import ewe.fx.Image;
import ewe.ui.Frame;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.sys.TaskObject;
import ewe.sys.Task;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

//##################################################################
public class PrinterJob extends PrinterJobObject{
//##################################################################
protected java.awt.print.PrinterJob nativeJob;
//public Graphics currentPage;
public boolean dialogBoxShown = false;
public static final int HINT_DONT_RESIZE_WIDTH = 0x1;
public static final int HINT_DONT_RESIZE_HEIGHT = 0x2;
/**
This is a "hint" flag for getPageRect. If it is specified and the PageRect of the
requested size and dpi could not be created, then the PrinterJob should not attempt
to create a smaller one.
**/
public static final int HINT_DONT_RESIZE = HINT_DONT_RESIZE_WIDTH|HINT_DONT_RESIZE_HEIGHT;
public static final int HINT_MONO_IMAGE = 0x10;
public static final int HINT_GRAYSCALE_IMAGE = 0x20;
/**
* Get a new PrinterJob object. Under some ewe environments (e.g.
* under Win32) this will automatically bring up the PrinterSelect
* dialog box. If the user cancels the dialog box, getPrinterJob() will return
* false.
**/
//===================================================================
public static PrinterJob getPrinterJob()
//===================================================================
{
	java.awt.print.PrinterJob nj = java.awt.print.PrinterJob.getPrinterJob();
	if (nj == null) return null;
	PrinterJob pj = new PrinterJob();
	pj.nativeJob = nj;
	return pj;
}
/**
* This displays the printer select/setup dialog box. Some ewe environments
* will always bring up the printer dialog box when a PrinterJob is created.
* If alwaysShow is false, the printDialog will only be brought up if it was
* not brought up on creation. If alwaysShow is true, it is always brought up.
* <p>
* Because the box may be brought up on creation on some systems, but not others
* the correct way to ensure that it is brought up once only is:
* <pre>
* PrinterJob pj = PrinterJob.getPrinterJob();
* if (pj != null)
*  if (pj.printDialog(false))
*   pj.print(myPrinter,null);
*
**/
//===================================================================
public boolean printDialog(Frame parent)
//===================================================================
{
	if (true){//alwaysShow || !dialogBoxShown){
		final Handle h = new Handle();
		new Thread(){
			public void run(){
				try{
					ewe.ui.Window.enterNativeDialog();
					if (nativeJob.printDialog())
						h.set(h.Succeeded);
					else
						h.set(h.Failed);
				}finally{
					ewe.ui.Window.exitNativeDialog();
				}
			}
		}.start();
		try{
			h.waitOn(Handle.Success);
			return true;
		}catch(HandleStoppedException e){
			return false;
		}catch(InterruptedException e){
			return false;
		}
		//return nativeJob.printDialog();
	}else
		return true;
}

//===================================================================
PageFormat toEwePageFormat(java.awt.print.PageFormat pf,PageFormat pg)
//===================================================================
{
	if (pf == null) return null;
	if (pg == null) pg = new PageFormat();
	//pg.pointsPerInch = 72;
	//int pi = pg.pointsPerInch;
	pg.imageableWidth = pf.getImageableWidth();//(int)(((pf.getImageableWidth()*pi)/72)*xscale)-(int)(20*xscale);
	pg.imageableHeight = pf.getImageableHeight();//(int)(((pf.getImageableHeight()*pi)/72)*yscale)-(int)(20*yscale);
	pg.fullPageWidth = pf.getWidth();//(int)(((pf.getWidth()*pi)/72)*xscale);
	pg.fullPageHeight = pf.getHeight();//(int)(((pf.getHeight()*pi)/72)*yscale);
	pg.imageableX = pf.getImageableX();//(int)(pf.getImageableX()*xscale);
	pg.imageableY = pf.getImageableY();//(int)(pf.getImageableY()*yscale);
	//System.out.print(pf.getImageableWidth()+", "+pf.getImageableHeight()+", "+pf.getWidth()+", "+pf.getHeight());
	//System.out.println(", "+pf.getImageableX()+", "+pf.getImageableY());
	//pg.pixelsPerInch = (int)(xscale*72);
	return pg;
}
//===================================================================
java.awt.print.PageFormat toJavaPageFormat(PageFormat pf)
//===================================================================
{
	//if (pf == null) return null;
	java.awt.print.PageFormat jpf = new java.awt.print.PageFormat();
	if (pf.requestedX != -1 || pf.requestedFullWidth != -1){
		java.awt.print.Paper p = new java.awt.print.Paper();
		if (pf.requestedFullWidth != -1) p.setSize(pf.requestedFullWidth,pf.requestedFullHeight);
		if (pf.requestedX != -1) p.setImageableArea(pf.requestedX,pf.requestedY,pf.requestedWidth,pf.requestedHeight);
		jpf.setPaper(p);
	}
	return jpf;
}

Graphics2D currentPage;
int gotID;
int numPages;
//-------------------------------------------------------------------
private PrintSurface getPrintSurface(PageFormat pf)
//-------------------------------------------------------------------
{
	return new PrintSurfaceObject(pf){
		{
			testPutRect = true;
		}
		public void putPageRect(PageRect r){
			PrinterJob.this.putPageRect(r);
		}
		public Image getImageBuffer(int width, int height){
			return PrinterJob.this.getImageBuffer(width,height);
		}
	};
}

/**
* This starts the printing process going. The print() method of the toPrint
* object will be called with a page index starting from 0 and increasing until
* the validatePage method returns false. The format parameter will eventually be used
* to set a particular page format, but at the moment is not used.
**/
//===================================================================
public Task getPrintTask(final Printable toPrint,final PageFormat format,final PrintOptions options)
//===================================================================
{
	final PrinterJob pj = this;
	return new TaskObject(){
		protected void doRun(){
			try{
				gotID = -1;
				handle.startDoing("Starting print job.");
				java.awt.print.Printable pr = new java.awt.print.Printable(){
					boolean done = false;
					PageCounter pc;
					public int print(java.awt.Graphics pg,java.awt.print.PageFormat pf,int pageIndex){
						try{
							//Vm.debug("Printing: "+pageIndex);
							if (done) return NO_SUCH_PAGE;
							PageFormat epf = toEwePageFormat(pf,format);
							if (pc == null) pc = new PageCounter(handle,toPrint,format,options);
							//
							// For some reason, this almost always gets called twice - the first time round
							// with an identity transform.
							//
							currentPage = (Graphics2D)pg;
							/*
							if (gotID != pageIndex && currentPage.getTransform().isIdentity()){
								gotID = pageIndex;
								return PAGE_EXISTS;
							}
							*/
							if (gotID != pageIndex){
								if (!pc.moveToNextPage()) {
									done = true;
									return NO_SUCH_PAGE;
								}
								gotID = pageIndex;
							}
							if (!toPrint.print(handle,getPrintSurface(epf),epf,pc.currentPageIndex)){
								done = true;
								return NO_SUCH_PAGE;
							}
							return PAGE_EXISTS;
						}catch(Exception e){
							handle.fail(e);
							return NO_SUCH_PAGE;
						}
					}
				};
				java.awt.print.PageFormat jpf = toJavaPageFormat(format);
				nativeJob.setPrintable(pr,jpf);
				nativeJob.print();
				if (handle.shouldStop) handle.set(handle.Aborted);
				else handle.set(handle.Succeeded);
				//Vm.debug("Succeeded!");
			}catch(Exception e){
				e.printStackTrace();
				handle.fail(e);
			}finally{
				printingComplete();
			}
		}
	};
}
/**
 * Call this within the Printable.print() method to get an off-screen Image to draw on which will
 * eventually be sent to the printer.
 * <p>
 * Printing images on a printer graphics can be flakey. Both Win32 and Java have some peculiarities
 * as far as this is concerned. How the image is created can have a profound effect on it.
 * <p>
 * This method will provide an image that you can draw to and then later draw to the printer graphics.
 * <p>
 * @param g The printer Graphics object as sent to the print() method of the Printable object.
 * @param width The width of the image needed.
 * @param height The height of the image needed.
 * @param imageOptions These are hints to the method to tell it what kind of image is going to be used
 * but this method may override these options if necessary.
 * @return an Image to use for off-screen drawing.
 */

//-------------------------------------------------------------------
private Image getImageBuffer(int width,int height)
//-------------------------------------------------------------------
{
	GraphicsConfiguration gc = currentPage.getDeviceConfiguration();
	BufferedImage bi = gc.createCompatibleImage(width,height);
	return new ewe.fx.Image(bi,0);
}
/**
Call this whithin the print() method of the Printable object to put the PageRect onto the paper.
**/
//-------------------------------------------------------------------
private void putPageRect(PageRect r)
//-------------------------------------------------------------------
{
	AffineTransform at = currentPage.getDeviceConfiguration().getDefaultTransform();
	at.translate(r.x,r.y);
	at.scale(72.0/r.xDPI,72.0/r.yDPI);
	currentPage.drawImage(r.image.getAWTImage(),at,null);
}
//##################################################################
}
//##################################################################
