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
import ewe.util.*;

//##################################################################
public class Frame extends CellPanel {
//##################################################################

public static final int PressedOutside = 1;

public boolean wantPressedOutside = false;
public boolean contentsOnly = false;
public boolean capturePressedOutside = false;
public boolean doSaveScreen = false;
public boolean resizeOnSIP = false;

public boolean isControlPanel = false;
//public boolean isPopup = false;

public PopupController popupController;

public boolean closeWindow = false;
public CellPanel top = new CellPanel(), bottom = new CellPanel(), contents = new CellPanel();
public SavedScreen savedScreen;
boolean isModal = false;
/**
* This is a collection of components that should be refreshed when the frame is removed. It is initially null
* until something is added.
**/
public Vector controlsToRefresh;
private Vector childFrames;
Frame parentFrame;

/**
* This resizes a Frame and its contents. It works if a Frame is in its own Window OR if it
* the child of another Frame.
* @param width The new width for the Frame.
* @param height The new height for the Frame.
* @param overrideMaximize If this is true then a resize is forced regardless of the possible maximized state of the window.
* If it is false, then if the window containing the Frame is maximized, no resize will be done.
*/
//===================================================================
public void resize(int width, int height, boolean overrideMaximize)
//===================================================================
{
	Frame f = this;
	try{
		if (Gui.isWindowFrame(f)){
			Window w = f.getWindow();
			int fl = w.getWindowFlags();
			if (overrideMaximize || (fl & (w.FLAG_MAXIMIZE|w.FLAG_MINIMIZE)) == 0){
				fl &= ~(w.FLAG_MAXIMIZE|w.FLAG_MINIMIZE|w.FLAG_MAXIMIZE_ON_PDA|w.FLAG_IS_DEFAULT_SIZE);
				Rect r = w.getWindowRect(new Rect(),true);
				Dimension d = f.getPreferredSize(null);
				r.x = fl;
				r.width = d.width; r.height = d.height;
				ewe.sys.Long l = (ewe.sys.Long)w.getInfo(w.INFO_FLAGS_FOR_SIZE,r,new ewe.sys.Long(),0);
				if ((l.value & w.FLAG_IS_DEFAULT_SIZE) != 0){
					w.setState(w.STATE_MAXIMIZED);
				}else{
					w.setState(w.STATE_NORMAL);
					r.width = d.width; r.height = d.height;
					w.setWindowRect(r,true);
				}
			}else{
				f.repaintNow(); //Maximized.
			}
		}else{
			Rect r = f.getRect(null);
			Dimension d = f.getPreferredSize(null);
			r.width = d.width; r.height = d.height;
			Gui.moveFrameTo(f,r);
		}
	}catch(Exception e){
		f.repaintNow();
	}
}
/**
* This resizes a Frame and its contents to be its preferred size. It works if a Frame is in its own Window OR if it
* the child of another Frame.
* @param overrideMaximize If this is true then a resize is forced regardless of the possible maximized state of the window.
* If it is false, then if the window containing the Frame is maximized, no resize will be done.
*/
//===================================================================
public void resize(boolean overrideMaximize)
//===================================================================
{
	Dimension d = getPreferredSize(null);
	resize(d.width,d.height,overrideMaximize);
}
/**
 * This does a relayout of the Frame and its children and then resizes the Frame
 * to fit the new layout.
* @param overrideMaximize If this is true then a resize is forced regardless of the possible maximized state of the window.
* If it is false, then if the window containing the Frame is maximized, no resize will be done.
 */
//===================================================================
public void relayoutAndResize(boolean overrideMaximize)
//===================================================================
{
	relayout(false);
	resize(overrideMaximize);
}
//===================================================================
public boolean isPopup() {return popupController != null;}
//===================================================================

//===================================================================
void addChildFrame(Frame cf)
//===================================================================
{
	childFrames = Vector.add(childFrames,cf);
	cf.parentFrame = this;
}
//===================================================================
void removeChildFrame(Frame cf)
//===================================================================
{
	if (childFrames != null) childFrames.remove(cf);
	cf.parentFrame = null;
}

/**
* This is set to either Gui.FILL_FRAME or Gui.CENTER_FRAME depending on the option chosen
* when it was shown/exec'ed
**/
public int displayOptions = 0;

protected CellPanel trueBottom = new CellPanel();
//==================================================================
{
	modify(SpecialBackground,0);
	borderColor = Color.Black;
}
//==================================================================
public void make(boolean reMake)
//==================================================================
{
	if (made) {
		//getPreferredSize();
		return;
	}
	trueBottom.borderStyle = Graphics.EDGE_SUNKEN;
	if (contentsOnly) top = trueBottom = bottom = null;
	if (!contentsOnly && top != null && !top.isEmpty()) addLast(top).setCell(HSTRETCH);
	if (contents != null)
		addLast(contents).setCell(STRETCH).setControl(FILL|CENTER);
	if (!contentsOnly) {
		if (bottom != null && !bottom.isEmpty())
			trueBottom.addNext(bottom).setCell(HSTRETCH);
		if (trueBottom != null && !trueBottom.isEmpty())
			addLast(trueBottom).setCell(HSTRETCH);
	}
	//calculatePreferredSize(null);
	super.make(reMake);
	if (top != null && top.isEmpty()) top = null;
	if (bottom != null && bottom.isEmpty()) bottom = null;
	if (trueBottom != null && trueBottom.isEmpty()) trueBottom = null;
	//top = trueBottom = bottom = null;
}
//==================================================================
public void pressedOutside(Point whereOnScreen)
//==================================================================
{
	postEvent(new FrameEvent(FrameEvent.PRESSED_OUTSIDE,this,whereOnScreen));
}
//==================================================================
public void setRect(int x,int y,int w,int h,Control relativeTo)
//==================================================================
{
	if (relativeTo != null) {
		Point p = Gui.getPosInParent(relativeTo,getParent());
		x+=p.x; y+=p.y;
	}
	setRect(x,y,w,h);
}
/*
//==================================================================
public void doPaint(Graphics g,Rect r)
//==================================================================
{
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	doBackground(g);
	super.doPaint(g,r);
}
*/
//==================================================================
public void setRect(int x,int y,int w,int h)
//==================================================================
{
	super.setRect(x,y,w,h);
	for (Iterator it = Vector.iterator(childFrames); it.hasNext();){
		Frame f = (Frame)it.next();
		if (f == this) continue;
		if (savedScreen == null) f.eraseSavedScreen();
		if ((f.displayOptions & Gui.FILL_FRAME) != 0){
			f.setRect(0,0,width,height);
		}else if ((f.displayOptions & Gui.CENTER_FRAME) != 0){
			f.setRect((width-f.width)/2,(height-f.height)/2,f.width,f.height);
		}else
			f.resetRect();
	}
	/*
	for (ewe.util.Iterator kids = getChildren(); kids.hasNext();){
		Control c = (Control)kids.next();
		if (c instanceof Frame) c.resetRect();
	}
	*/
	if (savedScreen != null) savedScreen.restore();
	if (!(parent instanceof Window))
	savedScreen = Gui.saveScreen(getWindow(),Gui.getAppRect(this),doSaveScreen);
}
//==================================================================
public void eraseSavedScreen()
//==================================================================
{
	if (savedScreen != null) savedScreen.free();
	savedScreen = null;
}
//==================================================================
/**
* This will add the frame to the container and call a make() on it.
* This also sets the frame to be modal.
* If you specify an option of Gui.FILL_FRAME or Gui.CENTER_FRAME then the
* frame will be positioned and displayed on screen. If not
* you will have to call setRect()/repaintNow() on the frame to cause it
* to be positioned and painted.
*/
public void exec(Container parent,int options) {Gui.execFrame(this,parent,options);}
/**
* This will add the frame to the container and call a make() on it.
* This also sets the frame to be non-modal.
* If you specify an option of Gui.FILL_FRAME or Gui.CENTER_FRAME then the
* frame will be positioned and displayed on screen. If not
* you will have to call setRect()/repaintNow() on the frame to cause it
* to be positioned and painted.
*/
public void show(Container parent,int options) {Gui.showFrame(this,parent,options);}
/**
* This closes the frame.
*/
public void hide(){Gui.hideFrame(this);}

static WeakSet sipResized = new WeakSet();

Rect beforeSip;

//-------------------------------------------------------------------
void hidden()
//-------------------------------------------------------------------
{
	if (lastClickedControl != null && lastClickedControl.isChildOf(this)) lastClickedControl = null;
	if (clipOwner != null && clipOwner.isChildOf(this)) clipOwner = null;
	sipResized.remove(this);
}
//-------------------------------------------------------------------
static void checkCurrentSip()
//-------------------------------------------------------------------
{
	if ((ewe.sys.Vm.getSIP() & ewe.sys.Vm.SIP_IS_ON) == ewe.sys.Vm.SIP_IS_ON) return;
	Window.visibleWidth = Window.visibleHeight = 0;
	if (sipResized.isEmpty()) return;
	SIPEvent ev = new SIPEvent();
	ev.type = ev.SIP_HIDDEN;
	checkSip(ev);
}
//-------------------------------------------------------------------
static void checkSip(SIPEvent ev)
//-------------------------------------------------------------------
{
	if (sipResized.isEmpty()) return;
	if (ev.type == SIPEvent.SIP_HIDDEN){
		Object [] all = sipResized.getRefs();
		for (int i = 0; i<all.length; i++){
			Frame f = (Frame)all[i];
			if (f != null) f.onEvent(ev);
		}
	}
}
//-------------------------------------------------------------------
static boolean hasSipResized(Window win)
//-------------------------------------------------------------------
{
	if (sipResized.isEmpty()) return false;
	Object [] all = sipResized.getRefs();
	for (int i = 0; i<all.length; i++){
		Frame f = (Frame)all[i];
		if (f != null){
			if (win == null) return true;
			else if (f.getWindow() == win) return true;
		}
	}
	return false;
}

//===================================================================
void sipOn(final int vw,final int vh)
//===================================================================
{
	if (width == 0 && height == 0){
		ewe.sys.Vm.callInSystemQueue(new ewe.sys.CallBack(){
			public void callBack(Object data){
				sipOn(vw,vh);
			}
		},null);
		return;
	}
	try{
		((FormFrame)this).myForm.fillSip(vh);
		return;
	}catch(Exception e){}
	Rect r = getRect();
	if (r.x+r.width > vw || r.y+r.height > vh){
		if (r.height <= vh) {
			//beforeSip = null;
			Gui.moveFrameTo(this,r.set(r.x,((vh-r.height)/2),r.width,r.height));
			//sipResized.remove(this);
			//ewe.sys.Vm.messageBox("Setting!","beforeSip is set to NULL!",0);
		}else{
			sipResized.add(this);
			beforeSip = r;
			Gui.moveFrameTo(this,new Rect().set(0,0,vw,vh));
			//ewe.sys.Vm.messageBox("Setting!","beforeSip is set to not NULL!",0);
		}
	}else{
		//ewe.sys.Vm.messageBox("Setting!","beforeSip is set to NULL!",0);
		//beforeSip = null;
		//sipResized.remove(this);
	}

}
//==================================================================
public void onEvent(Event ev)
{
	if (ev instanceof SIPEvent){
		//new MessageBox("SIP",((SIPEvent)ev).visibleWidth+","+((SIPEvent)ev).visibleHeight,MessageBox.MBOK)
		//.exec();
		if (resizeOnSIP){
			int vw = ((SIPEvent)ev).visibleWidth, vh =((SIPEvent)ev).desktopHeight;//visibleHeight;
			if (ev.type == SIPEvent.SIP_SHOWN){
				sipOn(vw,((SIPEvent)ev).visibleHeight);
			}else if (ev.type == SIPEvent.SIP_HIDDEN){
				try{
					((FormFrame)this).myForm.unfillSip();
					return;
				}catch(Exception e){}
				if (beforeSip != null){
					Gui.moveFrameTo(this,beforeSip);
					beforeSip = null;
					sipResized.remove(this);
				}else{
					//ewe.sys.Vm.messageBox("Null!","beforeSip is NULL",0);
				}
			}
		}
	}else if (ev instanceof FrameEvent){
		super.onEvent(ev);
	}else
		super.onEvent(ev);
}

//##################################################################
}
//##################################################################

