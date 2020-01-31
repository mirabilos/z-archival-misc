/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/applet/JWindow.java,v 1.2 2008/05/02 20:52:02 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package ewe.applet;

//import ewe.applet.Applet;
import java.awt.event.*;
import java.awt.*;

public class JWindow extends java.awt.Window implements EweWindow
{
private static final long serialVersionUID = 7257915181309914832L;
public ewe.ui.Window window;
public java.awt.Window lastModal;
public boolean isModal;
public boolean handleEvent(java.awt.Event event)
	{
	if (event.id == java.awt.Event.WINDOW_DESTROY)
		{
			synchronized(Applet.uiLock){
				Applet.currentApplet.destroy();
				System.exit(0);
			}
		}
	return super.handleEvent(event);
	}

public JWindow(boolean modal,String title,ewe.ui.Window parent){
	super(new java.awt.Frame(title));//parent == null ? new java.awt.Frame(title) : parent.jWindow instanceof java.awt.Frame ? (java.awt.Frame)parent.jWindow : new java.awt.Frame(title));
	isModal = modal;
	if (isModal){
		//lastModal = ewe.applet.Frame.curModal;
		//ewe.applet.Frame.curModal = this;
	}
	addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent we){
				synchronized(Applet.uiLock){
					window._postEvent(ewe.ui.WindowEvent.CLOSE,0,0,0,0,0);
				}
		}
		public void windowActivated(WindowEvent we){
				synchronized(Applet.uiLock){
					if (ewe.applet.Frame.curModal == JWindow.this || ewe.applet.Frame.curModal == null)
						window._postEvent(ewe.ui.WindowEvent.ACTIVATE,0,0,0,0,0);
				}
		}
		public void windowDeactivated(WindowEvent we){
				synchronized(Applet.uiLock){
					//if (ewe.applet.Frame.curModal == Frame.this || ewe.applet.Frame.curModal == null)
						window._postEvent(ewe.ui.WindowEvent.DEACTIVATE,0,0,0,0,0);
				}
		}
	});
	addComponentListener(new ComponentAdapter(){
		public void componentResized(ComponentEvent ce)
		{
			if (window != null)
				if (isVisible())
				synchronized(Applet.uiLock){
					window.canDisplay = false;
					window.windowBoundsChanged(JWindow.this);
				}
		}
		public void componentShown(ComponentEvent ce)
		{
			wasShown = true;
			if (window != null){
				if (isVisible())
					//new Thread(){
						//public void run(){
							synchronized(Applet.uiLock){
								//window.canDisplay = false;
								window.windowBoundsChanged(JWindow.this);
								synchronized(window._winCanvas){
									window._winCanvas.hasBeenShown = true;
									if (window._winCanvas.hasBeenPainted)
										window._winCanvas.repaint();
								}
							}
						//}
					//}.start();
			}
		}
	});
}

boolean wasShown = false;
public boolean wasShown() {return wasShown;}

public void setVisible(boolean vis)
{
	if (!vis){
		if (ewe.applet.Frame.curModal == this){
			ewe.applet.Frame.curModal = lastModal;
		}
	}
	super.setVisible(vis);
}
}
