/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/applet/Frame.java,v 1.2 2008/05/02 20:52:02 tg Exp $ */

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

public class Frame extends java.awt.Frame implements EweWindow
{
private static final long serialVersionUID = 8878514534361987829L;
public static java.awt.Window curModal;
public ewe.ui.Window window;
public boolean isModal;
public java.awt.Window lastModal;

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

public Frame(boolean modal){
	isModal = modal;
	if (isModal){
		//lastModal = curModal;
		//curModal = this;
	}
	addMouseListener(new MouseAdapter(){
		public void mouseExited(MouseEvent me){
			synchronized(Applet.uiLock){
				window._postEvent(ewe.ui.PenEvent.PEN_MOVED_OFF,0,0,0,0,0);
			}
		}
	});
	addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent we){
				synchronized(Applet.uiLock){
					window._postEvent(ewe.ui.WindowEvent.CLOSE,0,0,0,0,0);
				}
		}
		public void windowActivated(WindowEvent we){
				synchronized(Applet.uiLock){
					if (ewe.applet.Frame.curModal == Frame.this || ewe.applet.Frame.curModal == null)
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
				if (isVisible()){
					synchronized(Applet.uiLock){
						window.canDisplay = false;
						window.windowBoundsChanged(Frame.this);
					}
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
								window.windowBoundsChanged(Frame.this);
							}
							synchronized(window._winCanvas){
								window._winCanvas.hasBeenShown = true;
								if (window._winCanvas.hasBeenPainted)
									window._winCanvas.repaint();
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
		if (curModal == this){
			curModal = lastModal;
		}
	}
	super.setVisible(vis);
}
}
