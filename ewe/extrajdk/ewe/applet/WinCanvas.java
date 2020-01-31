/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/applet/WinCanvas.java,v 1.2 2008/05/02 20:52:02 tg Exp $ */

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

import ewe.ui.*;
import ewe.sys.*;

//##################################################################
class MouseWheelListener implements java.awt.event.MouseWheelListener{
//##################################################################

WinCanvas wc;
MouseWheelListener(WinCanvas wc)
{
	this.wc = wc;
}
public void mouseWheelMoved(java.awt.event.MouseWheelEvent me){
	wc.handleEvent(me);
}

//##################################################################
}
//##################################################################

public class WinCanvas extends java.awt.Panel
{
private static final long serialVersionUID = 4699601170314893726L;
public Window win;
public boolean hasBeenShown = false;
public boolean hasBeenPainted = false;

{
	setLayout(null);
}
SystemMessage capture;
java.util.Vector queue = new java.util.Vector();

int msg = 0;

public java.awt.Dimension preferredSize;

public static boolean inCallback = true;

public static boolean amInMessageThread()
{
	boolean ret = false;
	synchronized(Applet.uiLock){
		ret = inCallback;
	}
	return ret;
}
public java.awt.Dimension getPreferredSize()
{
	if (preferredSize == null)
		return new java.awt.Dimension(Applet.currentApplet.width,Applet.currentApplet.height);
	else return preferredSize;
}
	public void addToQueue(FullEvent fe){
		//ewe.sys.Vm.debug("Adding: "+fe.type+", "+fe.systemType);
		final Window w = win;
		if (fe.systemType == SystemMessage.PAINT) {
			synchronized(this){
				if (Applet.currentApplet != null)
					if (Applet.currentApplet.frame == null)
						hasBeenShown = true;
				hasBeenPainted = true;
				if (!hasBeenShown) return;
			}
			win.canDisplay = true;
			synchronized(Applet.uiLock){
				w._doPaint(fe.x, fe.y, fe.modifiers, fe.timestamp);
			}
		}else if (fe.systemType == SystemMessage.TIMER){
			if (w instanceof MainWindow)
				synchronized(Applet.uiLock){
					((MainWindow)w)._onTimerTick();
				}
		}else if (fe.systemType == SystemMessage.CALLBACK){
			if (fe.who instanceof CallBack){
				synchronized(Applet.uiLock){
					inCallback = true;
					((CallBack)fe.who).callBack(fe.data);
					inCallback = false;
				}
			}
		}else{
			synchronized(Applet.uiLock){
				w._postEvent(fe.type,fe.key,fe.x,fe.y,fe.modifiers,fe.timestamp);
			}
		}
	}
	/*
		synchronized(Applet.uiLock){
			queue.addElement(fe);
			Applet.uiLock.notifyAll();
		}
	*/

	public void addToQueueExclusive(FullEvent fe){
		addToQueue(fe);
		/*
		synchronized(Applet.uiLock){
			for (int i = 0; i<queue.size(); i++){
				if (((FullEvent)queue.elementAt(i)).systemType == fe.systemType) return;
			}
			queue.addElement(fe);
			Applet.uiLock.notifyAll();
		}
		*/
	}
		void handleEvent(java.awt.event.KeyEvent event)
		{
			int type = 0;
			int key = 0;
			int x = 0;
			int y = 0;
			int modifiers = 0;
			if ((event.getModifiers() & event.SHIFT_MASK) > 0)
				modifiers |= IKeys.SHIFT;
			if ((event.getModifiers() & event.CTRL_MASK) > 0)
				modifiers |= IKeys.CONTROL;
			boolean isRight = (event.getModifiers() & event.META_MASK) != 0;
			boolean doPostEvent = false;
			int stype = 0;
			//System.out.println(event.getID()+": "+event.getKeyChar()+", "+event.getKeyCode()+", "+event.isActionKey());
			switch(event.getID()){
			//case java.awt.event.KeyEvent.KEY_TYPED:
			case java.awt.event.KeyEvent.KEY_PRESSED:
				{
				stype = SystemMessage.KEYFIRST;
				type = KeyEvent.KEY_PRESS;
				newkey nk;
				if (!event.isActionKey())
					keyValue(nk = new newkey(event.getKeyChar(), modifiers));
				else
					key = actionKeyValue(nk = new newkey(event.getKeyCode(),modifiers));
				key = nk.key; modifiers = nk.modifiers;
				doPostEvent = key != 65535 && key != 0;//true;
				//if (doPostEvent) System.out.println("Key: "+key+", "+modifiers);
				break;
				}
				/*
			case java.awt.Event.KEY_ACTION:
				{
				newkey nk;
				key = actionKeyValue(nk = new newkey(event.key,modifiers));
				key = nk.key; modifiers = nk.modifiers;
				if (key != 0)
					{
					stype = SystemMessage.KEYFIRST;
					type = KeyEvent.KEY_PRESS;
					doPostEvent = true;
					}
				break;
				}
				*/
				}
			if (doPostEvent){
				int timestamp = (int)ewe.sys.Vm.getTimeStamp();//(int)event.when;
				addToQueue(new FullEvent(stype,type,key,x,y,modifiers,timestamp));
			}

		}
		void handleEvent(java.awt.event.MouseWheelEvent event)
		{
			int type = 0;
			int key = 0;
			int x = 0;
			int y = 0;
			int modifiers = 0;
			if ((event.getModifiers() & event.SHIFT_MASK) > 0)
				modifiers |= IKeys.SHIFT;
			if ((event.getModifiers() & event.CTRL_MASK) > 0)
				modifiers |= IKeys.CONTROL;
			boolean isRight = (event.getModifiers() & event.META_MASK) != 0;
			boolean doPostEvent = false;
			int stype = 0;
			x = event.getX();
			y = event.getY();
			if (!checkModal()) return;
			stype = SystemMessage.MOUSEFIRST;
			type = event.getWheelRotation() < 0 ? PenEvent.SCROLL_UP : PenEvent.SCROLL_DOWN;
			doPostEvent = true;
			if (doPostEvent){
				int timestamp = (int)ewe.sys.Vm.getTimeStamp();//(int)event.when;
				addToQueue(new FullEvent(stype,type,key,x,y,modifiers,timestamp));
			}
		}
		void handleEvent(java.awt.event.MouseEvent event)
		{
			int type = 0;
			int key = 0;
			int x = 0;
			int y = 0;
			int modifiers = 0;
			if ((event.getModifiers() & event.SHIFT_MASK) > 0)
				modifiers |= IKeys.SHIFT;
			if ((event.getModifiers() & event.CTRL_MASK) > 0)
				modifiers |= IKeys.CONTROL;
			boolean isRight = (event.getModifiers() & event.META_MASK) != 0;
			boolean doPostEvent = false;
			int stype = 0;
			x = event.getX();
			y = event.getY();
			switch(event.getID()){
				case java.awt.event.MouseEvent.MOUSE_MOVED:
				case java.awt.event.MouseEvent.MOUSE_DRAGGED:
					if (!checkModal()) break;
					stype = SystemMessage.MOUSEFIRST;
					type = PenEvent.PEN_MOVE;
					doPostEvent = true;
					break;
				case java.awt.event.MouseEvent.MOUSE_PRESSED:
					if (!checkModal()) break;
					stype = SystemMessage.MOUSEFIRST;
					type = PenEvent.PEN_DOWN;
					if (isRight) modifiers |= PenEvent.RIGHT_BUTTON;
					setMouseState(isRight ? 0x2 : 0x1,0);
					doPostEvent = true;
					break;
				case java.awt.event.MouseEvent.MOUSE_RELEASED:
					stype = SystemMessage.MOUSEFIRST;
					type = PenEvent.PEN_UP;
					if (isRight) modifiers |= PenEvent.RIGHT_BUTTON;
					setMouseState(0,isRight ? 0x2 : 0x1);
					doPostEvent = true;
					break;
			}
			if (doPostEvent){
				int timestamp = (int)ewe.sys.Vm.getTimeStamp();//(int)event.when;
				addToQueue(new FullEvent(stype,type,key,x,y,modifiers,timestamp));
			}
		}

public WinCanvas(final Window win)
	{
	this.win = win;
	try{
		addMouseWheelListener(new MouseWheelListener(this));
	}catch(Error e){
	}
	addKeyListener(new java.awt.event.KeyListener(){
		public void keyPressed(java.awt.event.KeyEvent ev){
			handleEvent(ev);
		}
		public void keyReleased(java.awt.event.KeyEvent ev){
			handleEvent(ev);
		}
		public void keyTyped(java.awt.event.KeyEvent ev){
			handleEvent(ev);
		}
	});
	addMouseMotionListener(new java.awt.event.MouseMotionListener(){
		public void mouseMoved(java.awt.event.MouseEvent ev){
			handleEvent(ev);
		}
		public void mouseDragged(java.awt.event.MouseEvent ev){
			handleEvent(ev);
		}
	});
	addMouseListener(new java.awt.event.MouseListener(){
		public void mousePressed(java.awt.event.MouseEvent ev){
			handleEvent(ev);
		}
		public void mouseReleased(java.awt.event.MouseEvent ev){
			handleEvent(ev);
		}
		public void mouseEntered(java.awt.event.MouseEvent ev){
			handleEvent(ev);
		}
		public void mouseExited(java.awt.event.MouseEvent ev){
			handleEvent(ev);
		}
		public void mouseClicked(java.awt.event.MouseEvent ev){
			handleEvent(ev);
		}
	});
	/*
	new Thread(new Runnable(){
		public void run(){
				//if (true) return;
				while(true){
				try{
					synchronized(Applet.uiLock){
						while(queue.size() == 0) {
							try{
								Applet.uiLock.wait();
							}catch(Exception e){
							}
						}
						FullEvent fe = (FullEvent)queue.elementAt(0);
						queue.removeElementAt(0);
						if (fe.systemType == SystemMessage.PAINT) {
							win.canDisplay = true;
							w._doPaint(fe.x, fe.y, fe.modifiers, fe.timestamp);
						}else if (fe.systemType == SystemMessage.TIMER){
							if (w instanceof MainWindow) ((MainWindow)w)._onTimerTick();
						}else if (fe.systemType == SystemMessage.CALLBACK){
							if (fe.who instanceof CallBack){
								((CallBack)fe.who).callBack(fe.data);
							}
						}else{
							w._postEvent(fe.type,fe.key,fe.x,fe.y,fe.modifiers,fe.timestamp);
						}
					}
				}catch(Throwable t){
					t.printStackTrace();
				}
				}
		}
	}).start();
	*/
}

// The only way I would be here is if I am in the Dispatch Thread.
public int getMessage(SystemMessage message,boolean peek,boolean remove)
{
	synchronized(Applet.uiLock){
		while (queue.size() == 0){
			if (peek) return 0;
			else try{
				Applet.uiLock.wait();
			}catch(Exception e){
			}
		}
		FullEvent fe = (FullEvent)queue.elementAt(0);
		boolean isPaint = fe.systemType == SystemMessage.PAINT;
		if (message != null){
			message.type = fe.systemType;
			message.wparam = message.lparam = 0;
			message.x = fe.x;
			message.y = fe.y;
			message.time = isPaint ? 0 : fe.timestamp;
			message.state = isPaint ? 0 : (remove ? message.REMOVED : 0);
		}
		if (!isPaint && remove) queue.removeElementAt(0);
		return 1;
	}
}

public int callBackInMessageThread(CallBack who,Object data)
{
	final FullEvent fe = new FullEvent(SystemMessage.CALLBACK,who);
	fe.data = data;
	new Thread(){
		public void run(){
			addToQueue(fe);
		}
	}.start();
	return 1;
}
//===================================================================
public java.awt.Window getParentWindow()
//===================================================================
{
	return (getParent() instanceof java.awt.Window ? (java.awt.Window)getParent():null);
}
//-------------------------------------------------------------------
boolean checkModal()
//-------------------------------------------------------------------
{
/*
	if (getParent() instanceof ewe.applet.Frame){
		ewe.applet.Frame fr = (ewe.applet.Frame)getParent();
		if (fr.curModal != null && fr.curModal != fr){
			fr.curModal.toFront();
			return false;
		}
	}
	if (getParent() instanceof ewe.applet.JWindow){
		ewe.applet.JWindow fr = (ewe.applet.JWindow)getParent();
		if (ewe.applet.Frame.curModal != null && ewe.applet.Frame.curModal != fr){
			ewe.applet.Frame.curModal.toFront();
			return false;
		}
	}
*/
	java.awt.Window mw = getParentWindow();
	if (ewe.applet.Frame.curModal != mw) {
		if (ewe.applet.Frame.curModal != null){
			ewe.applet.Frame.curModal.toFront();
			return false;
		}
	}
	return true;
}

class newkey {
	int key;
	int modifiers;
	public newkey(int k,int m){key = k; modifiers = m;}
}

public static int mouseState = 0;

static synchronized void setMouseState(int on,int off)
{
		mouseState &= ~off;
		mouseState |= on;
}

public static synchronized int getMouseState()
{
	return mouseState;
}

public boolean handleEvent(java.awt.Event event)
	{
	int type = 0;
	int key = 0;
	int x = 0;
	int y = 0;
	int modifiers = 0;
	if ((event.modifiers & java.awt.Event.SHIFT_MASK) > 0)
		modifiers |= IKeys.SHIFT;
	if ((event.modifiers & java.awt.Event.CTRL_MASK) > 0)
		modifiers |= IKeys.CONTROL;
	boolean isRight = (event.modifiers & event.META_MASK) != 0;
	boolean doPostEvent = false;
	int stype = 0;
	switch (event.id)
		{
		case java.awt.Event.MOUSE_MOVE:
		case java.awt.Event.MOUSE_DRAG:
			if (!checkModal()) break;
			stype = SystemMessage.MOUSEFIRST;
			type = PenEvent.PEN_MOVE;
			x = event.x;
			y = event.y;
			doPostEvent = true;
			break;
		case java.awt.Event.MOUSE_DOWN:
			if (!checkModal()) break;
			stype = SystemMessage.MOUSEFIRST;
			type = PenEvent.PEN_DOWN;
			if (isRight) modifiers |= PenEvent.RIGHT_BUTTON;
			setMouseState(isRight ? 0x2 : 0x1,0);
			x = event.x;
			y = event.y;
			doPostEvent = true;
			break;
		case java.awt.Event.MOUSE_UP:
			stype = SystemMessage.MOUSEFIRST;
			type = PenEvent.PEN_UP;
			if (isRight) modifiers |= PenEvent.RIGHT_BUTTON;
			setMouseState(0,isRight ? 0x2 : 0x1);
			x = event.x;
			y = event.y;
			doPostEvent = true;
			break;
		case java.awt.Event.KEY_PRESS:
		{
			stype = SystemMessage.KEYFIRST;
			type = KeyEvent.KEY_PRESS;
			newkey nk;
			keyValue(nk = new newkey(event.key, modifiers));
			key = nk.key; modifiers = nk.modifiers;
			doPostEvent = true;
			break;
			}
		case java.awt.Event.KEY_ACTION:
			{
			newkey nk;
			key = actionKeyValue(nk = new newkey(event.key,modifiers));
			key = nk.key; modifiers = nk.modifiers;
			if (key != 0)
				{
				stype = SystemMessage.KEYFIRST;
				type = KeyEvent.KEY_PRESS;
				doPostEvent = true;
				}
			break;
			}
		}
	if (doPostEvent)
		{
		int timestamp = (int)event.when;
		addToQueue(new FullEvent(stype,type,key,x,y,modifiers,timestamp));
		/*
		synchronized(Applet.uiLock)
			{
				win._postEvent(type, key, x, y, modifiers, timestamp);
			}
		*/
		}
	return super.handleEvent(event);
	}

public static int keyValue(newkey nk)
	{
	int key = nk.key;
	nk.modifiers |= IKeys.SPECIAL;
	switch (key)
		{
		case 8:
			key = IKeys.BACKSPACE;
			break;
		case 10:
			key = IKeys.ENTER;
			break;
		case 127:
			key = IKeys.DELETE;
			break;
		case 27:
			key = IKeys.ESCAPE;
			break;
		case 9:
			key = IKeys.TAB;
			break;
		default:
			nk.modifiers &= ~(IKeys.SPECIAL);
		}
	nk.key = key;
	return key;
	}

public static int actionKeyValue(newkey nk)
	{
	int key = 0;
	nk.modifiers |= IKeys.SPECIAL;
	//System.out.println(nk.key+", "+java.awt.Event.UP+", "+java.awt.event.KeyEvent.VK_UP);
	switch (nk.key)
		{
		case java.awt.event.KeyEvent.VK_PAGE_UP:       key = IKeys.PAGE_UP; break;
		case java.awt.event.KeyEvent.VK_PAGE_DOWN:       key = IKeys.PAGE_DOWN; break;
		case java.awt.event.KeyEvent.VK_HOME:       key = IKeys.HOME; break;
		case java.awt.event.KeyEvent.VK_END:        key = IKeys.END; break;
		case java.awt.event.KeyEvent.VK_UP:         key = IKeys.UP; break;
		case java.awt.event.KeyEvent.VK_DOWN:       key = IKeys.DOWN; break;
		case java.awt.event.KeyEvent.VK_LEFT:       key = IKeys.LEFT; break;
		case java.awt.event.KeyEvent.VK_RIGHT:      key = IKeys.RIGHT; break;
		case java.awt.event.KeyEvent.VK_INSERT:     key = IKeys.INSERT; break;
		case java.awt.event.KeyEvent.VK_ENTER:      key = IKeys.ENTER; break;
		case java.awt.event.KeyEvent.VK_TAB:        key = IKeys.TAB; break;
		case java.awt.event.KeyEvent.VK_BACK_SPACE: key = IKeys.BACKSPACE; break;
		case java.awt.event.KeyEvent.VK_ESCAPE:     key = IKeys.ESCAPE; break;
		case java.awt.event.KeyEvent.VK_DELETE:     key = IKeys.DELETE; break;
		case java.awt.event.KeyEvent.VK_F1:     key = IKeys.F1; break;
		case java.awt.event.KeyEvent.VK_F2:     key = IKeys.F2; break;
		case java.awt.event.KeyEvent.VK_F3:     key = IKeys.F3; break;
		case java.awt.event.KeyEvent.VK_F4:     key = IKeys.F4; break;
		case java.awt.event.KeyEvent.VK_F5:     key = IKeys.F5; break;
		case java.awt.event.KeyEvent.VK_F6:     key = IKeys.F6; break;
		case java.awt.event.KeyEvent.VK_F7:     key = IKeys.F7; break;
		case java.awt.event.KeyEvent.VK_F8:     key = IKeys.F8; break;
		case java.awt.event.KeyEvent.VK_F9:     key = IKeys.F9; break;
		case java.awt.event.KeyEvent.VK_F10:     key = IKeys.F10; break;
		case java.awt.event.KeyEvent.VK_F11:     key = IKeys.F11; break;
		case java.awt.Event.PGUP:       key = IKeys.PAGE_UP; break;
		case java.awt.Event.PGDN:       key = IKeys.PAGE_DOWN; break;
		case java.awt.Event.HOME:       key = IKeys.HOME; break;
		case java.awt.Event.END:        key = IKeys.END; break;
		case java.awt.Event.UP:         key = IKeys.UP; break;
		case java.awt.Event.DOWN:       key = IKeys.DOWN; break;
		case java.awt.Event.LEFT:       key = IKeys.LEFT; break;
		case java.awt.Event.RIGHT:      key = IKeys.RIGHT; break;
		case java.awt.Event.INSERT:     key = IKeys.INSERT; break;
		//case java.awt.Event.ENTER:      key = IKeys.ENTER; break;
		//case java.awt.Event.TAB:        key = IKeys.TAB; break;
		//case java.awt.Event.BACK_SPACE: key = IKeys.BACKSPACE; break;
		//case java.awt.Event.ESCAPE:     key = IKeys.ESCAPE; break;
		//case java.awt.Event.DELETE:     key = IKeys.DELETE; break;
		case java.awt.Event.F1:     key = IKeys.F1; break;
		case java.awt.Event.F2:     key = IKeys.F2; break;
		case java.awt.Event.F3:     key = IKeys.F3; break;
		case java.awt.Event.F4:     key = IKeys.F4; break;
		case java.awt.Event.F5:     key = IKeys.F5; break;
		case java.awt.Event.F6:     key = IKeys.F6; break;
		case java.awt.Event.F7:     key = IKeys.F7; break;
		case java.awt.Event.F8:     key = IKeys.F8; break;
		case java.awt.Event.F9:     key = IKeys.F9; break;
		case java.awt.Event.F10:     key = IKeys.F10; break;
		case java.awt.Event.F11:     key = IKeys.F11; break;
		case java.awt.event.KeyEvent.VK_F12:
		case java.awt.Event.F12:
				key = IKeys.F12;
				if ((nk.modifiers & IKeys.CONTROL) != 0)
					key = IKeys.PDA_CANCEL;
				break;
		default:
		}
	nk.key = key;
	return key;
	}

public void update(java.awt.Graphics g)
	{
	paint(g);
	}

public java.awt.Graphics lastPaintGraphics;

public void paint(java.awt.Graphics g)
	{
	java.awt.Rectangle r = null;
	lastPaintGraphics = g;
	// getClipRect() is missing in the Kaffe distribution for Linux
	try { r = g.getClipBounds(); }
	catch (NoSuchMethodError e) { r = g.getClipRect(); }
	addToQueue(new FullEvent(SystemMessage.PAINT,0,0,r.x,r.y,r.width,r.height));
	/*
	synchronized(Applet.uiLock)
		{
			win._doPaint(r.x, r.y, r.width, r.height);
		}
	*/
	}
	public void postTimerMessage()
	{
		addToQueueExclusive(new FullEvent(SystemMessage.TIMER,0,0,0,0,0,0));
	}

	public java.awt.Graphics getGraphics()
	{
		//if (lastPaintGraphics != null) return lastPaintGraphics;
		return super.getGraphics();
	}

	public static void place(ewe.ui.Window win,java.awt.Component c,ewe.fx.Rect where)
	{
		if (win == null) return;
		java.awt.Container awtWin = win._winCanvas;
		if (awtWin == null) return;
		if (c.getParent() != awtWin) awtWin.add(c);
		if (where != null) c.setBounds(where.x,where.y,where.width,where.height);
	}
}
