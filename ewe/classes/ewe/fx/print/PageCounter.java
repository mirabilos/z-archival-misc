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
/**
You would only use this if you are inheriting from PrinterJobObject. It is used
to iterate through pages that are to be printed.
**/
//##################################################################
public class PageCounter{
//##################################################################

/**
This is the total number of pages in the document as reported by
Printable.countPages()
**/
public int pagesInDocument;
/**
This is the total number of pages that will be printed using the provided
PrinterOptions object.
**/
public int pagesToPrint;
/**
This is only valid after calls to moveToNextPage(). This is sent to the print() method of the Printable.
**/
public int currentPageIndex;
/**
This indicates which of the pagesToPrint is being printed.
**/
private int currentPage;

private PrintOptions po;
private Printable pr;
private PageFormat pf;
private Handle handle;

//===================================================================
public PageCounter(Handle handle, Printable printable, PageFormat pageFormat, PrintOptions options)
//===================================================================
{
	pr = printable;
	po = options;
	pf = pageFormat;
	pagesInDocument = printable.countPages(pageFormat);
	pagesToPrint = options.countPagesWillPrint(pagesInDocument);
	this.handle = handle;
	if (handle != null) ewe.sys.mThread.nap(1*250);
}

//-------------------------------------------------------------------
private boolean finished()
//-------------------------------------------------------------------
{
	if (handle != null){
		handle.doing = "Finished Printing.";
		handle.setProgress(1.0f);
		handle.changed();
	}
	return false;
}
/**
Call this to move to the next page. When it returns null it indicates that there are no more
pages to print.
**/
//===================================================================
public boolean moveToNextPage() throws IllegalStateException
//===================================================================
{
	if (handle != null && handle.shouldStop) return finished();
	currentPage++;
	int toGo = currentPage == 1 ? po.getFirstPage(pagesInDocument) : po.getNextPage();
	if (toGo == po.NO_MORE_PAGES) return finished();
	currentPageIndex = toGo-1;
	if (!pr.validatePage(pf,currentPageIndex)) return finished();
	if (handle == null) return true;
	if (pagesToPrint == Printable.UNKNOWN_NUMBER_OF_PAGES || pagesToPrint == 0){
		handle.doing = "Page "+currentPage+".";
		handle.setProgress(-1f);
	}else{
		handle.doing = "Page "+currentPage+" of "+pagesToPrint+".";
		handle.setProgress((float)(currentPage-1)/pagesToPrint);
	}
	handle.changed();
	return true;
}

//##################################################################
}
//##################################################################

