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
import ewe.fx.*;
import ewe.io.IOException;
import ewe.io.File;
import ewe.io.PrintWriter;
import ewe.io.FileOutputStream;
//##################################################################
public class ReportException extends MessageBox{
//##################################################################
Throwable t;
mButton toClip, toFile;
mTabbedPanel tp;

public ReportException()
{
	this(new Exception("Hello there!"));
}
//===================================================================
public ReportException(Throwable t)
//===================================================================
{
	this(t,null,null,false);
}
/**
* "message" is the message that is shown before the exception class name. If this is null
* a standard message is shown.<p>
* "advice" is the message that is shown after the exception class name. If this is null
* no advice message is shown.<p>
* If "showContinue" is true then the buttons "Continue" and "Cancel" are shown, otherwise
* a single "Close" button is shown.
**/
//===================================================================
public ReportException(Throwable t,String message,String advice,boolean showContinue)
//===================================================================
{
	super("Application Error",
	(message == null ? "An error occured in the program:" : message)+"\n\n"+t.getClass().getName()+": "+t.getMessage()+
	(advice == null ? "" : "\n\n"+advice),0);
	windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
	this.t = t;
	tp = new mTabbedPanel();
	tp.cardPanel.autoScroll = false;
	addLast(tp);
	tp.addItem(addTo = new CellPanel(),"Error",null);
	mTextPad ma = new mTextPad(10,50);
	ma.wrapToScreenSize = false;
	//ma.modify(DisplayOnly,0);
	ma.setText(ewe.sys.Vm.getStackTrace(t).replace('\t',' '));
	//ScrollBarPanel sbp = new ScrollBarPanel(ma);
	tp.addItem(ma.setAsStaticDisplay(true),"Details",null);
	addTo.setBorder(mInput.inputEdge|BF_RECT,2);

	if (Gui.isSmartPhone){
		makeSoftKeys().
			setKey(1,(showContinue ? "Continue|":"Close|")+EXIT_IDOK,tick,null).
			setMenu(2,"Actions",null,(showContinue ? "Cancel|"+EXIT_IDCANCEL+"|" : "")+"Details|DETAILS|Error|ERROR|-|-|To Clipboard|TO_CLIPBOARD");
	}else{
		addButton(toClip = new mButton("To Clipboard"));
		addButton(toFile = new mButton("To File"));
		if (showContinue){
			addButton(ok = new mButton("Continue"));
			addButton(cancel = new mButton("Cancel"));
		}else{
			addButton(ok = new mButton("Close"));
		}
	}
	windowFlagsToClear = Window.FLAG_HAS_CLOSE_BUTTON;
	resizable = true;
}
public void onControlEvent(ControlEvent ev)
{
	if (ev.type == ev.PRESSED){
		if (ev.target == toClip){
			ewe.sys.Vm.setClipboardText(ewe.sys.Vm.getStackTrace(t).replace('\t',' '));
			Gui.flashMessage("Saved to clipboard. ",this);
		}else if (ev.target == toFile){
				PrintWriter pw = null;
				String f = "\\EweStackTrace.txt";
				try{pw = new PrintWriter(new FileOutputStream(f,true));
				}catch(IOException e){}
				if (pw == null) {
					f = "EweStackTrace.txt";
					try{pw = new PrintWriter(new FileOutputStream(f,true));
					}catch(IOException e){}
				}
				if (pw == null){
					Gui.flashMessage("Could not save stack trace.",this);
				}else{
					try{
						pw.println("-----------------------");
						pw.println(new ewe.sys.Time().format("dd-MMM-yyyy, HH:mm:ss"));
						pw.println(ewe.sys.Vm.getStackTrace(t).replace('\t',' '));
						Gui.flashMessage("Saved to: "+f,1500,this,Gui.FLASH_BEEP);
					}
					catch(Exception e)
					{
						Gui.flashMessage("Could not save stack trace.",this);
					}
					finally
					{
						pw.close();
					}
				}
		}else super.onControlEvent(ev);
	}
}
public boolean handleAction(String action)
{
	if (super.handleAction(action)) return true;
	if (action.equals("ERROR")) {
		tp.select(0);
		Gui.takeFocus(tp,ByRequest);
	}
	else if (action.equals("DETAILS")) {
		tp.select(1);
		Gui.takeFocus(tp,ByRequest);
	}
	else if (action.equals("TO_CLIPBOARD")) ewe.sys.Vm.setClipboardText(ewe.sys.Vm.getStackTrace(t).replace('\t',' '));
	else return false;
	return true;
}
//##################################################################
}
//##################################################################

