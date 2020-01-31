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
import ewe.fx.*;
import ewe.sys.TaskObject;
import ewe.sys.Task;
import ewe.ui.Frame;
import ewe.ui.Window;
//##################################################################
public class PrinterJob extends PrinterJobObject{
//##################################################################
private double imageableX, imageableY;

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
	PrinterJob pj = nativeGetPrinterJob();
	if (pj == null) return null;
	if (pj.nativeJob == null)
		return null;
	if (!pj.printerSelected)
		if (!pj.printDialog(null))
			return null;
	return pj;
}
/**
* This displays the printer select/setup dialog box, which may be a native OS
* dialog box.
* @param parent The parent Frame - which can be null.
* @return true if the user pressed OK, false if the user pressed Cancel.
*/
//===================================================================
public boolean printDialog(Frame parent)
//===================================================================
{
	final Window w = parent == null ? null : parent.getWindow();
	return new ewe.sys.MessageThreadTask(){
		protected boolean doTask(Object data){
			Window.enterNativeDialog();
			try{
				return nativePrinterSetupDialog(w);
			}finally{
				Window.exitNativeDialog();
			}
		}
	}.execute(null);
}

//-------------------------------------------------------------------
private PrintSurface getPrintSurface(PageFormat pf)
//-------------------------------------------------------------------
{
	return new PrintSurfaceObject(pf){
		public void putPageRect(PageRect r){
			PrinterJob.this.putPageRect(r.image,r.x-imageableX,r.y-imageableY,r.width,r.height,format.xDPI,format.yDPI);
		}
		public Image getImageBuffer(int width, int height){
			return PrinterJob.this.getImageBuffer(width,height);
		}
	};
}
/**
Return a Task object that does the printing.
**/
//-------------------------------------------------------------------
protected Task getPrintTask(final Printable toPrint,final PageFormat format,final PrintOptions po)
//-------------------------------------------------------------------
{
		return new TaskObject(){
			protected void doRun(){
				PageFormat pf = format;
				try{
					nativeStart();
					PageCounter pc = new PageCounter(handle,toPrint,format,po);
					while (pc.moveToNextPage()){
						int i = pc.currentPageIndex;
						nativeGetPage(pf);
						imageableX = pf.imageableX;
						imageableY = pf.imageableY;
						if (!toPrint.print(handle,getPrintSurface(pf),pf,i)) break;
						nativePrintPage();
					}
					if (handle.shouldStop) handle.set(handle.Aborted);
					else handle.set(handle.Succeeded);
				}catch(Throwable e){
					handle.fail(e);
				}finally{
					printingComplete();
					nativeEnd();
				}
			}
		};
}
//===================================================================
private native void nativeEnd();
private native void nativeCancel();
private native boolean nativeStart();
private native void nativeGetPage(PageFormat format);
private native boolean nativePrintPage();
private native static PrinterJob nativeGetPrinterJob();
private native boolean nativePrinterSetupDialog(Window w);
private native Image getImageBuffer(int width, int height);
private native void putPageRect(Image im, double x, double y, double width, double height,double xdpi,double ydpi);
//##################################################################
}
//##################################################################




