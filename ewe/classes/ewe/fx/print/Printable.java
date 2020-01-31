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
* This is an interface which must be supported by objects which print to the printer.
* During the printing process the validatePage(int pageIndex) is called for each
* page to be printed (starting from 0) and if it returns true, then the print()
* method will be called for that page index. This process continues until validatePage()
* returns false.
**/
//##################################################################
public interface Printable{
//##################################################################

/**
* Prints the page at the specified index into the specified Graphics context in the specified format.
* A PrinterJob calls the Printable interface to request that a page be rendered into the context specified
* by graphics. The format of the page to be drawn is specified by pageFormat. The zero based index of the
* requested page is specified by pageIndex.
* <p>
* Within this method you should call printSurface.getPageRect() to get a Rectangular window onto the virtual
* printer page. You can then draw into the image provided by that PageRect and then afterwards,
* transfer the PageRect to the printer using printSurface.putPageRect().
* <p>
* The PageMosaic class is an excellent class for allowing you to draw onto an arbitrary section on
* the output page.
* @param handle a Handle that an outside thread can use to monitor the printing. During the printing
* you should check the shouldStop value of the Handle, and if it is true, then you should abort printing
* and return false.
* @param printSurface A printSurface that you should call getPageRect() on in order to get buffers to
* draw on.
* @param page the format for the page you are printing on.
* @param pageIndex the index of the page you are printing on, starting from index 0.
* @return true if printing was successful, false if the shouldStop value of handle was set.
*/
//===================================================================
public boolean print(Handle handle,PrintSurface printSurface,PageFormat page,int pageIndex);
//===================================================================
/**
* This returns true if the page with the specified index should be printed.
**/
//===================================================================
public boolean validatePage(PageFormat page, int pageIndex);
//===================================================================
/**
* This is called when printing is about to start and it requests a count of the number of pages
* that would be necessary to print. If this is unknown then the method should
* return UNKNOWN_NUMBER_OF_PAGES, but even if it returns a valid number of pages, the validatePage()
* method will still be called before each page is printed via print().
**/
//===================================================================
public int countPages(PageFormat page);
//===================================================================

public static final int UNKNOWN_NUMBER_OF_PAGES = -1;

//##################################################################
}
//##################################################################

