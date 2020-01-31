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
import ewe.sys.Handle;
import ewe.sys.Task;
import ewe.sys.Lock;

//##################################################################
public abstract class PrinterJobObject implements Printer{
//##################################################################
//
//Do not move the next two.
//
protected Object nativeJob;
protected boolean printerSelected;
//
private Lock printLock;
private Handle printingHandle;

/**
Return a Lock object for this PrinterJobObject
**/
//-------------------------------------------------------------------
private Lock getPrintLock()
//-------------------------------------------------------------------
{
	if (printLock == null) printLock = new Lock();
	return printLock;
}
/**
Return if the PrintJob is currently printing.
**/
//===================================================================
public boolean isPrinting()
//===================================================================
{
	getPrintLock().synchronize(); try{
		return printingHandle != null;
	}finally{
		getPrintLock().release();
	}
}
/**
If this returns true, then no printing is currently being done
and the current thread holds the printLock.
**/
//-------------------------------------------------------------------
private boolean lockIfNotPrinting()
//-------------------------------------------------------------------
{
	getPrintLock().synchronize();
	if (printingHandle != null) {
		getPrintLock().release();
		return false;
	}
	return true;
}

//-------------------------------------------------------------------
protected void printingComplete()
//-------------------------------------------------------------------
{
	getPrintLock().synchronize();
	printingHandle = null;
	getPrintLock().release();
}
/**
Get the Handle for the current printing operation.
If this returns null then there is no current printing.
**/
//===================================================================
public Handle getCurrentPrintingHandle()
//===================================================================
{
	Handle h = null;
	getPrintLock().synchronize();
	h = printingHandle;
	getPrintLock().release();
	return h;
}
/**
Cancel a print operation if one is underway. This calls stop(0) on the
current print handle if one is present.
**/
//===================================================================
public boolean cancel()
//===================================================================
{
	Handle h = getCurrentPrintingHandle();
	if (h == null) return false;
	h.stop(0);
	return true;
}
/**
* This starts the printing process going. The print() method of the toPrint
* object will be called with a page index starting from 0 and increasing until
* the validatePage method returns false. The format parameter will eventually be used
* to set a particular page format, but at the moment is not used.
**/
//===================================================================
public ewe.sys.Handle print(Printable toPrint,PageFormat format, PrintOptions po)
//===================================================================
{
	if (!lockIfNotPrinting()) return new Handle(new IllegalStateException("Still printing a Job."));
	//
	// Now I hold the lock.
	//
	try{
		if (format == null) format = new PageFormat();
		if (po == null) po = new PrintOptions();
		Task to = getPrintTask(toPrint, format, po);
		printingHandle = to.startTask();
		return printingHandle;
	}catch(RuntimeException e){
		e.printStackTrace();
		throw e;
	}catch(Error er){
		er.printStackTrace();
		throw er;
	}finally{
		getPrintLock().release();
	}
}

/**
Return a Task object that does the printing. Once the task is complete it MUST call
printingComplete() even if the task was cancelled.
**/
//-------------------------------------------------------------------
protected abstract Task getPrintTask(Printable toPrint,PageFormat format, PrintOptions po);
//-------------------------------------------------------------------
//##################################################################
}
//##################################################################

