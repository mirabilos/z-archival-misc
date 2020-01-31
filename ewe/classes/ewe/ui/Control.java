/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
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
package ewe.ui;

import ewe.fx.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.reflect.FieldTransfer;
/**
Control is the base class for user-interface objects. Here are some common
tasks you may need to do on all Controls.<p>
<b>To repaint a Control</b> call the <i>repaintNow()</i> method.<p>
<b>To enable/disable a Control</b> use the <i>modify()</i> method. This
method will set/clear modifier flags (listed in the ewe.ui.ControlConstants interface)
for the Control.
*/

public class Control extends ControlBase
//extends ControlEntry
implements UIConstants,EventListener,ewe.sys.TimerProc,CellConstants,ImageRefresher
{
//...........................................
//Do NOT CHANGE OR MOVE THESE VARIABLES.
//...........................................

/** The control's x location */
protected int x;
/** The control's y location */
protected int y;
/** The control's width */
protected int width;
/** The control's height */
protected int height;
/** The parent of the control. */
protected Container parent;
/** The control's next sibling. */
protected Control next;
/** The control's previous sibling. */
protected Control prev;
/** The children of the container. */
protected Control children;
/** The tail of the children list. */
protected Control tail;
/** Modifiers of the control. */
protected int modifiers = 0;
//* This is a placeholder for the DoPaintMethod used in the native PaintChildren().*/
protected Object DoPaintMethod;
//* This is another placeholder for the natvie PaintChildren*/
protected Object MyClass;

//...........................................
// Variables after this may be moved.
//...........................................
/**
* This is the "HotKey" for the control - use setHotKey() to set it.
**/
public int hotKey = 0;
/**
* The border style of the Control. Should be one of the BDR_XXX or EDGE_XXX constants.
**/
public int borderStyle = 0;
/**
* The Color of the border of the Control (if a single line, non-3D border is used).
**/
public Color borderColor = Color.DarkGray;
/**
 * @deprecated Use ewe.sys.Vm.requestTimer();
 */
public Timer addTimer(int millis)
	{
	MainWindow win = MainWindow.getMainWindow();
	return win.addTimer(this, millis);
	}

/**
 * @deprecated Use ewe.sys.Vm.requestTimer() and ewe.sys.Vm.cancelTimer();
 */
public boolean removeTimer(Timer timer)
	{
	MainWindow win = MainWindow.getMainWindow();
	return win.removeTimer(timer);
	}

/** Returns the font metrics for a given font. */
public FontMetrics getFontMetrics(Font font)
	{
	MainWindow win = MainWindow.getMainWindow();
	return win.getFontMetrics(font);
	}

/** Sets or changes a control's position and size. */
/* MLB
public void setRect(int x, int y, int width, int height)
	{
	// MLB if (parent != null) repaint();
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
	// MLB if (parent != null) repaint();
	}
*/
/**
 * Returns a copy of the control's rectangle. A control's rectangle
 * defines its location and size.
 */
public Rect getRect()
	{
	return new Rect(this.x, this.y, this.width, this.height);
	}
/** Returns the control's parent container. */
public Container getParent()
	{
	return parent;
	}

/** Returns the next child in the parent's list of controls. */
public Control getNext()
	{
	return next;
	}

/**
 * Returns true if the given x and y coordinate in the parent's
 * coordinate system is contained within this control.
 */
public boolean contains(int x, int y)
	{
	int rx = this.x;
	int ry = this.y;
	if (x < rx || x >= rx + this.width || y < ry || y > ry + this.height)
		return false;
	return true;
	}


/**
 * This calls a repaintNow() using a CallBack - it does not repaint immediately.
 */
public void repaint()
	{
	ewe.sys.Vm.callInSystemQueue(new ewe.sys.CallBack(){
		public void callBack(Object data){
			repaintNow();
		}
	},null);
/*
	int x = 0;
	int y = 0;
	Control c = this;
	while (!(c instanceof Window))
		{
		x += c.x;
		y += c.y;
		c = c.parent;
		if (c == null)
			return;
		}
	Window win = (Window)c;
	win.damageRect(x, y, this.width, this.height);
*/
	}

/**
 * Do not use this - it is for Waba compatibility only, use getGraphics() instead.
	Creates a Graphics object which can be used to draw in the control.
 * This method finds the surface associated with the control, creates
 * a graphics assoicated with it and translates the graphics to the
 * origin of the control. It does not set a clipping rectangle on the
 * graphics.
 * @deprecated Use getGraphics() instead.
 */
public Graphics createGraphics()
	{
	int x = 0;
	int y = 0;
	Control c = this;
	while (!(c instanceof Window))
		{
		x += c.x;
		y += c.y;
		c = c.parent;
		if (c == null)
			return null;
		}
	Window win = (Window)c;
	//MLB
	Graphics g =
		mApp.mainApp == null ? new Graphics(win) : Graphics.createNew(win);
	g.translate(x, y);
	return g;
	}

/**
 * Posts an event. The event pass will be posted to this control
 * and all the parent controls of this control (all the containers
 * this control is within).
	 @deprecated
 * @see Event
 */
//MLB

public void oldPostEvent(Event event)
	{
	Control c;

	c = this;
	while (c != null)
		{
		c.onEvent(event);
 		c = c.parent;
		}
	}

//##################################################################
// MLB July-2000
// Additions made to control go here.
// Original class file size: 1758 bytes.
//##################################################################

public int penStatus = 0;

/*
protected static Dimension fx = new Dimension(100,100);
public Control()
{
	setTag(FIXEDSIZE,fx);
}
*/
/*
public static WeakSet all = new WeakSet();
public Control()
{
	if (this instanceof Editor || this instanceof Frame){
		all.add(this);
	}
}
*/
public int borderWidth = 0;
public String name = unnamed;
/**
* This is an optional string that is used as a user informative prompt for the control.
**/
public String prompt = emptyString;
/**
* This is optional - if an mLabel is used as the on-screen prompt for this control, then set this value
* to be that control. This ensures that if this control gets the focus and its container
* must scroll to make it visible, it will also attempt to make the prompt visible as well.
**/
public Control promptControl = null;

/**
 * On a normal Control this will set the promptControl variable to "prompt", but
 * on a Container, the first child non-container will have "prompt" assigned to it.
 * @param prompt the Control acting as the prompt (usually an mLabel).
 */
public void setPromptControl(Control prompt)
{
	takePromptControl(prompt);
}
protected boolean takePromptControl(Control prompt)
{
	this.promptControl = prompt;
	return true;
}
private static final Iterator noChildren = new ObjectIterator(null);
public String toString() {return (name == unnamed ? mString.rightOf(getClass().getName(),'.') : name)+(text != null && text.length() != 0 ? (" = "+text) : "");}
/**
* This iterator cycles through the components which are physically
* added to this Control
**/
//===================================================================
public Iterator getChildren() {return noChildren;}
public Iterator getChildrenBackwards() {return noChildren;}
//===================================================================
/**
* Get all the sub-controls for this Control and their sub-controls.
**/
//===================================================================
public Iterator getAllDescendants(boolean backwards)
//===================================================================
{
	Stack s = new Stack();
	Vector v = new Vector();
	Iterator it = backwards ? getChildrenBackwards() : getChildren();
	while(true){
		if (it.hasNext()){
			Control c = (Control)it.next();
			v.add(c);
			s.push(it);
			it = backwards ? c.getChildrenBackwards() : c.getChildren();
		}else if (s.size() == 0) break;
		else it = (Iterator)s.pop();
	}
	return v.iterator();
}
/**
* Get all the sub-controls for this Control and their sub-controls.
**/
//===================================================================
public Iterator getAllSubControls()
//===================================================================
{
	Stack s = new Stack();
	Vector v = new Vector();
	Iterator it = getSubControls();
	while(true){
		if (it.hasNext()){
			Control c = (Control)it.next();
			v.add(c);
			s.push(it);
			it = c.getSubControls();
		}else if (s.size() == 0) break;
		else it = (Iterator)s.pop();
	}
	return v.iterator();
}
//===================================================================
public Iterator getSubControls() {return getChildren();}
//===================================================================

/**
This does NOT enable a disabled control - but tells it to put itself in an "active"
state. For example, a button in an active state looks as if the mouse/pen is being
pressed down on it.<p>
* If you want to re-enable a disabled control
* do modify(0,Control.Disabled)
**/
//===================================================================
public void activate()
//===================================================================
{
}
/**
* This does NOT disable a control - but tells it to put itself in a "non-active" state.
* For example, a button that has is showing itself as being pressed, will show itself
* as not pressed when this is called.<p>
* If you want to disable a control
* do modify(Control.Disabled,0)
**/
//===================================================================
public void deactivate()
//===================================================================
{
}

//===================================================================
public boolean _debug = false;
//===================================================================
/**
 * Switch on or off a modifier depending on the status parameter.
 * @param flag The flags to set on or off.
 * @param status if true the flags will be set on, if false they will be set off.
 * @return the return value from the modify() call.
 */
//==================================================================
public final int set(int flag,boolean status)
//==================================================================
{
	if (status) return modify(flag,0);
	else return modify(0,flag);
}
/**
 * Set/Clear modifier flags for the Control. Examples of flags are Disabled, NotEditable, Invisible... Check
 * ControlConstants for the complete list. The return value can be used to restore the
 * modifiers to the original state like this:
	<pre>
	int old = myControl.modify(flagsSet,flagsClear);
	//do some processing...
	myControl.restore(old,flagsSet|flagsClear);
	</pre>
 * @param flagsToSet The flags to set.
 * @param flagsToClear The flags to clear.
 * @return A value that can be use to restore the modifier to its original state using
 * restore().
 */
//==================================================================
public int modify(int flagsToSet,int flagsToClear)
//==================================================================
{
	int ret = modifiers & (flagsToSet|flagsToClear);
	modifiers |= flagsToSet;
	modifiers &= ~flagsToClear;
	if (((modifiers & (Disabled|MouseSensitive)) == (Disabled|MouseSensitive))
	&& ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_NO_MOUSE_POINTER) == 0))
	penStatus &= ~PenIsOn;
	return ret;
}
/**
* Restore the modifier flags to their previous value. An example use:
	<pre>
	int old = myControl.modify(flagsSet,flagsClear);
	//do some processing...
	myControl.restore(old,flagsSet|flagsClear);
	</pre>
* @param oldValue the value returned by modify().
* @param mask the paramters flagsToSet OR'ed with flagsToClear as used in the modify() method.
*/
//==================================================================
public void restore(int oldValue,int mask)
//==================================================================
{
	modifiers = (modifiers &= ~mask)|oldValue;
}
/**
* This checks if the specified flags are set or clear.
* @param flagsSet Flags to check for being set.
* @param flagsClear Flags to check for being clear.
* @return true if all the flags are set/cleared as specified.
*/
//===================================================================
public boolean checkModifiers(int flagsSet,int flagsClear)
//===================================================================
{
	if ((modifiers & flagsSet) != flagsSet) return false;
	if (((~modifiers) & flagsClear) != flagsClear) return false;
	return true;
}
/**
* This ensures that the specified flags are set or clear. Return true
* if a change was made. Returns false if no change was made.
**/
//===================================================================
public boolean change(int flagsToSet,int flagsToClear)
//===================================================================
{
	if (checkModifiers(flagsToSet,flagsToClear)) return false;
	modify(flagsToSet,flagsToClear);
	return true;
}
/**
 * Check if the control has the specified modifier flags set.
 * @param what The modifier flags to check for.
 * @param shouldInherit if this is true then all parents of the control will also be checked.
 * @return true if the flag is set in this control or in any of its ancestors (if should inherit is true).
 */
//==================================================================
public boolean hasModifier(int what,boolean shouldInherit)
//==================================================================
{
	int got = getModifiers(shouldInherit);
	return ((got & what) == what);
}
/**
* This will get the modifiers for this control. If shouldInherit is true,
* then all the ancestor modifiers will be OR'ed together.
**/
//===================================================================
public int getModifiers(boolean shouldInherit)
//===================================================================
{
	int flags = modifiers;
	if (shouldInherit)
		for (Control c = parent; c != null; c = c.parent)
			flags |= c.modifiers;
	return flags;
}
/**
* Get particular modifiers flags from another control and optionally set children flags.
* @param fromWho The control to get from.
* @param inheritFromAll if this is true then also check the ancestors of the control for the flags.
* @param what the flags to get (or'ed together).
* @param setChildren if this is true then modify my children.
*/
//==================================================================
public final void inheritModifiers(Control fromWho,boolean inheritFromAll,int what,boolean setChildren)
//==================================================================
{
	if (fromWho == null) return;
	int toSet = fromWho.getModifiers(inheritFromAll) & what;
	int toClear = (~toSet) & what;
	if (setChildren) modifyAll(toSet,toClear,true);
	else modify(toSet,toClear);
}
/**
 * Modify this control and all child controls (and their children).
 * @param set The flags to set.
 * @param clear The flags to clear.
 */
//===================================================================
public void modifyAll(int set,int clear) {modifyAll(set,clear,true);}
//===================================================================
/**
 * Modify all child controls (and their children).
 * @param set The flags to set.
 * @param clear The flags to clear.
 * @param doThisOne if this is true then this control will also be modified.
 */
//===================================================================
public void modifyAll(int set,int clear,boolean doThisOne)
//===================================================================
{
	if (doThisOne) modify(set,clear);
	for (Iterator e = getSubControls(); e.hasNext();){
		Object obj = e.next();
		if (obj instanceof Control)
			((Control)obj).modifyAll(set,clear,true);
	}
}
/**
* The object's tool tip which should be a String, IImage, Control or ToolTip
**/
public Object toolTip;
/**
* Set the tooltip of the object.
* @param tip the tip which should be a String, IImage, Control or ToolTip
* @return this Control.
*/
//===================================================================
public Control setToolTip(Object tip)
//===================================================================
{
	toolTip = tip;
	PenEvent.wantPenMoved(this,PenEvent.WANT_PEN_MOVED_ONOFF,true);
	return this;
}
/**
* This should return an acceptable ToolTip object. That is either a String, IImage, Control or ToolTip
* @param x The x location of the mouse in this control.
* @param y The y location of the mouse in this control.
* @return A suitable ToolTip or null.
*/
//===================================================================
public Object getToolTip(int x,int y)
//===================================================================
{
	return toolTip;
}
//==================================================================
public final Dimension recalculatePreferredSize(Dimension dest) {modifyAll(0,CalculatedSizes,true); return getPreferredSize(dest);}
//==================================================================

//-------------------------------------------------------------------
protected boolean testDim(Dimension dest,int w,int h)
//-------------------------------------------------------------------
{
	if (w < 0 || h < 0) return false;
	dest.set(w,h);
	return true;
}
//......................................................
// These are values which will be calculated. Do not use
// them to set the preferred or minimum values.
//......................................................
/** Do not set this directly - use setPrefferedSize() instead. */
protected int preferredWidth = 10;
/** Do not set this directly - use setPrefferedSize() instead. */
protected int preferredHeight = 10;
/** Do not set this directly - use setMinimumSize() instead. */
protected int minWidth = 0;
/** Do not set this directly - use setMinimumSize() instead. */
protected int minHeight = 0;
/** Do not set this directly - use setMaximumSize() instead. */
protected int maxWidth = -1;
/** Do not set this directly - use setMaximumSize() instead. */
protected int maxHeight = -1;

//==================================================================
public Control setFixedSize(int width,int height) {return setTag(FIXEDSIZE,new Dimension(width,height));}
public Control setPreferredSize(int width,int height) {return setTag(PREFERREDSIZE,new Dimension(width,height));}
public Control setTextSize(int width,int height) {return setTag(TEXTSIZE,new Dimension(width,height));}
public Control setMinimumSize(int width,int height){return setTag(MINIMUMSIZE,new Dimension(width,height));}
public Control setMaximumSize(int width,int height){return setTag(MAXIMUMSIZE,new Dimension(width,height));}
//==================================================================
//-------------------------------------------------------------------
private void doCalculateSizes()
//-------------------------------------------------------------------
{
	modify(CalculatedSizes,0);
	/*
	preferredWidth = preferredHeight = 10;
	minWidth = minHeight = 0;
	maxWidth = maxHeight = -1; //No maximum.
	*/
	calculateSizes();
	Dimension d = (Dimension)getTag(TEXTSIZE,null);
	if (d != null) {
		calculateTextSize(d.width,d.height,tempD);
		preferredWidth = java.lang.Math.max(tempD.width,preferredWidth);
		preferredHeight = java.lang.Math.max(tempD.height,preferredHeight);
	}
}
//-------------------------------------------------------------------
private Dimension checkStandardSizes(Dimension d)
//-------------------------------------------------------------------
{
	int myFlags = modifiers;
	if (((myFlags & CalculatedSizes) == 0) ||((myFlags & AlwaysRecalculateSizes) != 0))
		doCalculateSizes();
//------------------------------------------------------------------
	for (Control c = parent; c != null; c = c.parent)
		myFlags |= c.modifiers;
	if ((myFlags & ShrinkToNothing) != 0) return d.set(0,0);
	Dimension d2 = (Dimension)getTag(FIXEDSIZE,null);
	if (d2 != null) return d.set(d2.width,d2.height);
	return null;
}
//-------------------------------------------------------------------
private Dimension getASize(Dimension dest,int tag)
//-------------------------------------------------------------------
{
	if (dest == null) dest = new Dimension();
//------------------------------------------------------------------
	if (checkStandardSizes(dest) != null) return dest;
//------------------------------------------------------------------
	Dimension d = (Dimension)getTag(tag,null);
//------------------------------------------------------------------
	if (tag == PREFERREDSIZE) dest.set(preferredWidth,preferredHeight);
	else if (tag == MINIMUMSIZE) dest.set(minWidth,minHeight);
	else if (tag == MAXIMUMSIZE) dest.set(maxWidth,maxHeight);
	if (d != null) {
		if (d.width >= 0) dest.width = d.width;
		if (d.height >= 0) dest.height = d.height;
		//return dest.set(d.width,d.height);
	}
	return dest;
}
static Dimension tempD = new Dimension();
/**
* This gets all of the control sizes. Currently it returns the
* preferredSize, minimumSize and maximumSize. They fit into the array
* as follows:
*
* preferredWidth,preferredHeight,minWidth,minHeight,maxWidth,maxHeight
*
**/
//===================================================================
public int [] getSizes(int [] values)
//===================================================================
{
	int myFlags = getModifiers(false);
	int flags = getModifiers(true);
	if (values == null) values = new int[6];
	if (values.length < 6) values = new int[6];
//..................................................................
	if (((myFlags & CalculatedSizes) == 0) ||((myFlags & AlwaysRecalculateSizes) != 0))
		doCalculateSizes();
//..................................................................
	if ((flags & ShrinkToNothing) != 0){
		values[0] = values[2] = values[4] =
		values[1] = values[3] = values[5] = 0;
		return values;
	}
//..................................................................
	Dimension d2 = (Dimension)getTag(FIXEDSIZE,null);
	if (d2 != null) {
		values[0] = values[2] = values[4] = d2.width;
		values[1] = values[3] = values[5] = d2.height;

		return values;
	}
//..................................................................
	Dimension dest = getPreferredSize(tempD);
	values[0] = dest.width; values[1] = dest.height;
	dest = getMinimumSize(dest);
	values[2] = dest.width; values[3] = dest.height;
	dest = getMaximumSize(dest);
	values[4] = dest.width; values[5] = dest.height;
	return values;
}
//==================================================================
public Dimension getPreferredSize(Dimension dest)
//==================================================================
{
	if (dest == null) dest = new Dimension();
//..................................................................
	Dimension ret = getASize(dest,PREFERREDSIZE);
	return ret;
}
//==================================================================
public Dimension getMinimumSize(Dimension dest)
//==================================================================
{
	return getASize(dest,MINIMUMSIZE);
}
//==================================================================
public Dimension getMaximumSize(Dimension dest)
//==================================================================
{
	return getASize(dest,MAXIMUMSIZE);
}
/**
* Override this to calculate the preferred, minimum and maximum size of your control. This is only
* called once unless the modifier flag AlwaysRecalculateSizes is true.
* During the calculation you will set the variables preferredWidth, preferredHeight, etc. directly.
**/
//-------------------------------------------------------------------
protected void calculateSizes() {preferredWidth = preferredHeight = 15;}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected Dimension calculateTextSize(int width,int height,Dimension dest)
//-------------------------------------------------------------------
{
	dest = Dimension.unNull(dest);
	FontMetrics fm = getFontMetrics();
	Rect sz = Gui.getAverageSize(fm,height < 0 ? 10 : height,width < 0 ? 10 : width,2,2);
	if (width >= 0)
	dest.width = width >= 0 ? sz.width : width;
	dest.height = height >= 0 ? sz.height : height;

	return dest;
}
/**
* This is used to "make" the control before being displayed. This is usually only
* overridden by containers.
* @param reMake if this is true then you should do a full re-make.
*/
//===================================================================
public void make(boolean reMake){}
//===================================================================
/*
public void setFixedSize(int width,int height) {fixedWidth = width; fixedHeight = height;}
public void setPreferredSize(int width,int height) {preferredWidth = fixedPreferredWidth = width; preferredHeight = fixedPreferredHeight = height;}
public void setTextSize(int width,int height) {textWidth = width; textHeight = height;}
*/
//===================================================================
/**
* This will change the width and height values.
*/
//==================================================================
public void resizeTo(int width,int height)
//==================================================================
{
	this.width = width;
	this.height = height;
	if (mApp.mainApp == null && parent != null) repaint();
}
/**
* Do not use this to repaint a control - use repaintNow() instead. This is used
* to cause a control to re-layout its components and re-calculate its preferred size.
**/
//==================================================================
public void redisplay()
//==================================================================
{
	modifyAll(ForceResize,CalculatedSizes,true);
	getPreferredSize(null);
}
/**
* This requests that the size be changed to the following dimensions
* but the control is free to ignore it or to change to a different value.
*/
//==================================================================
public void requestResizeTo(int width,int height)
//==================================================================
{
	int myFlags = getModifiers(false);
	if (width == this.width && height == this.height && ((myFlags & ForceResize) == 0)) return;
	modify(0,ForceResize);
	if (((myFlags & PreferredSizeOnly) != 0) || getTag(FIXEDSIZE,null) != null) {
		Dimension r = getPreferredSize(null);
		resizeTo(r.width,r.height);
	}else
		resizeTo(width,height);
}
//===================================================================
public Rect getRect(Rect dest){return Rect.unNull(dest).set(x,y,width,height);}
//===================================================================
public void setRect(Rect r) {setRect(r.x,r.y,r.width,r.height);}
//==================================================================
public void setRect(int x,int y,int width,int height)
//==================================================================
{
	Rect r = getRect(Rect.buff);
	boolean doRT = true;//(width != r.width || height != r.height || hasModifier(ForceResize,false));
	//modify(0,ForceResize);
	this.x = x;
	this.y = y;
	if (doRT) requestResizeTo(width,height);
	else if (mApp.mainApp == null && parent != null) repaint();
}
//===================================================================
public Rect getDim(Rect dest)
//===================================================================
{
	dest = Rect.unNull(dest);
	return dest.set(0,0,width,height);

}
//===================================================================
public Dimension getSize(Dimension dest){return Dimension.unNull(dest).set(width,height);}
//===================================================================
public Point getLocation(Point dest) {return Point.unNull(dest).set(x,y);}
//===================================================================
public void setLocation(int x,int y){this.x = x; this.y = y;}
//===================================================================
//public boolean do@DoubleBuffer() {return doubleBuffer;}
//===================================================================
/*
//==================================================================
public void repaintDataNow(boolean yes) {if (yes) repaintDataNow();}
//===================================================================
public void repaintDataNow()  {repaintDataNow(null);}
public void repaintDataNow(Graphics gr)
//===================================================================
{
	int flags = getModifiers(true);
	if ((flags & Invisible) != 0) return;
	boolean wasNull = gr == null;
	if (gr == null) gr = getGraphics();
	if (gr == null) return;
	//if (gr.isEmpty()) return;
	Rect where = getDataRect(null);
	Graphics g = gr;
	Image i = null;
	if (doubleBuffer ){
		i = getControlBuffer(where);
		g = Graphics.createNew(i);
	}
	if (g != gr) g.translate(-where.x,-where.y);
	else g.setClip(where.x,where.y,where.width,where.height);
	doPaintData(g);
	if (g != gr) g.translate(where.x,where.y);
	if (doubleBuffer ){
		g.free();
		gr.drawImage(i,where.x,where.y);
		i.free();
	}
	gr.free();
}
//===================================================================
protected void doPaintData(Graphics gr) {}
//===================================================================
*/

/**
 * Get the rectangle, relative to the Control, of the area considered to be the "data" part of the control.

This is only used for Controls with the HasData modifier. These are controls that distinguish between
their data (e.g. contained text) and non-data (e.g. border) on-screen display.

 * @param dest The destination rectangle.
 * @return The destination rectangle or a new rectangle if the destination is null.
 */
//-------------------------------------------------------------------
protected Rect getDataRect(Rect dest) {return Rect.unNull(dest).set(0,0,width,height);}
//-------------------------------------------------------------------

private static Rect dataRect = new Rect();


/**
 * Repaint the "data" part of the Control.

This is only used for Controls with the HasData modifier. These are controls that distinguish between
their data (e.g. contained text) and non-data (e.g. border) on-screen display.


 */
//-------------------------------------------------------------------
protected final void doPaintData()
//-------------------------------------------------------------------
{
	if (!Graphics.canCopy || ((modifiers & Transparent) != 0)){
		repaintNow();
		return;
	}
	if (!Gui.requestPaint(this)) return;
	int flags = getModifiers(true);
	if ((flags & Invisible) != 0) return;
	//ewe.sys.Vm.gc();
	Graphics gr = getGraphics();
	if (gr == null) return;
	Rect where = getDim(dataRect);
	Graphics g = gr;
	Image i = null;
	// where.x and where.y will always be zero.
	if (doubleBuffer){
		try{
			i = getControlBuffer(where);
			g = Graphics.createNew(i);
			g.setFont(getFont());
			Gui.captureControl(this,g,where);
		}catch(ewe.sys.SystemResourceException e){
		}
	}
	if (g != gr) g.translate(-where.x,-where.y);
	//gr.setClip(where.x,where.y,where.width,where.height);
	doPaintData(g);
	//if (g != gr) g.translate(where.x,where.y);
	if (g != gr){
		g.free();
		gr.drawImage(i,where.x,where.y);
		i.free();
	}
	gr.free();
	//Gui.refreshTopFrame(this);
}

/**
 * Repaint the "data" part of the Control.

This is only used for Controls with the HasData modifier. These are controls that distinguish between
their data (e.g. contained text) and non-data (e.g. border) on-screen display.

 * @param g The Graphics object to paint to.
 */
//-------------------------------------------------------------------
protected void doPaintData(Graphics g)
//-------------------------------------------------------------------
{

}
/**
 * Repaint the "data" part of the Control now.

This is only used for Controls with the HasData modifier. These are controls that distinguish between
their data (e.g. contained text) and non-data (e.g. border) on-screen display.

If the Control does not have the HasData modifier, then the entire control will be repainted via repaintNow().
 */
//===================================================================
public void repaintDataNow() {if ((modifiers & HasData) == 0) repaintNow(); else doPaintData();}
//===================================================================



/**
Repaint the Control now.
 */
//===================================================================
public void repaintNow() {repaintNow(null,null);}
//===================================================================
/**
Repaint the Control now if the "yes" parameter is true.
 */
//===================================================================
public void repaintNow(boolean yes) {if (yes) repaintNow(null,null);}
//==================================================================

/**
Repaint a section of the Control now.
 * @param gr The Graphics to paint to. If this is null a new Graphics will be created.
 * @param where The rectangle within the Control to paint. If this is null the entire Control will be painted.
 */
//===================================================================
public void repaintNow(Graphics gr,Rect where)
//===================================================================
{
	if (gr == null)
		if (!Gui.requestPaint(this)){
			//Vm.debug("No painty!");
			return;
		}
	//Boolean goAhead = Gui.repainting(gr,null);
	//if (goAhead == null) return;
	//try{
		int flags = getModifiers(true);
		if ((flags & Invisible) != 0) return;
		boolean wasNull = gr == null;
		//if (haveNativePaint) ewe.sys.Vm.gc();
		//ewe.sys.Vm.messageBox("AGC","AGC",ewe.sys.Vm.MB_OK);
		if (gr == null) gr = getGraphics();
		if (gr == null) {
			//Vm.debug("No painty 2!");
			return;
		}
		//if (gr.isEmpty()) return;
		if (where == null) where = getDim(null);
		else if (where.x < 0 || where.x+where.width > width || where.y < 0 || where.y+where.height > height){
			Rect nr = getDim(null);
			nr.getIntersection(where,nr);
			where = nr;
		}
		Graphics g = gr;
		Image i = null;
		//doubleBuffer = false; //Remove this!!
		if (doubleBuffer){
			try{
				i = getControlBuffer(where);
				if (i != null){
					g = Graphics.createNew(i);
					g.setFont(getFont());
				}
			}catch(ewe.sys.SystemResourceException e){}
		}
		if (g != gr) {
			g.translate(-where.x,-where.y);
		}else {
			//g.setClip(where.x,where.y,where.width,where.height);
			g.reduceClip(where.x,where.y,where.width,where.height,null);
		}

		if ((modifiers & Transparent) != 0)
			fillBackground(g);
		//paintBackground(g);
		doPaint(g,where);
		doPaintChildren(g,where.x,where.y,where.width,where.height);
		//ewe.sys.Vm.debug(where.toString());
		if (g != gr) g.translate(where.x,where.y);
		if (doubleBuffer && i != null){
			g.free();
			//if (_debug) ewe.sys.Vm.debug("OK, drawing: "+where.x+", "+where.y+", "+i.getWidth()+", "+i.getHeight());
			gr.drawImage(i,where.x,where.y);
			i.free();
		}
		if (wasNull) {
			//Gui.refreshTopFrame(this);
			gr.free();
		}
	//}finally{
//		Gui.repainting(null,goAhead);
//	}
	//ewe.sys.Vm.messageBox("ERN","ERN",ewe.sys.Vm.MB_OK);
}
/**
 * Do not use this - it is for Waba compatibility only.

 * @param gr The graphics to paint to.
 * @deprecated Use doPaint() instead.
 */
//===================================================================
public void onPaint(Graphics gr)
//==================================================================
{
	if (mApp.mainApp == null) return;
	int flags = getModifiers(true);
	if ((flags & Invisible) != 0) return;
	Rect r = getRect();
	r.x = r.y = 0;
	if (doubleBuffer){
		try{
			Image i = getControlBuffer();
			Graphics g = Graphics.createNew(i);
			//paintBackground(g);
			doPaint(g,r);
			gr.drawImage(i,0,0);
			i.free();
		}catch(ewe.sys.SystemResourceException e){
			doPaint(gr,r);
		}
	}else {
		//paintBackground(gr);
		doPaint(gr,r);
	}
}
	//mColor.setColor(gr,mColor.Black);
	//gr.fillRect(0,0,width,height);
	//ewe.sys.Vm.sleep(500);
	//doPaint(gr);
	//ewe.sys.Vm.sleep(500);

//==================================================================
public void doBackground(Graphics g)
//==================================================================
{
	//if (backGround == null  && !hasModifier(SpecialBackground,false)) return;
	paintBackground(g);
}
/**
 * This is used for paint buffering during a repaintNow() operation. If this
 * returns null then double buffering will not be used on the Control.
 * @param r the area to be buffered. If this is null you should return an Image
 * for the whole area.
 * @return an Image for paint buffering.
 * @throws ewe.sys.SystemResourceException if an Buffer could not be created.
 */
//==================================================================
public Image getControlBuffer(Rect r) throws ewe.sys.SystemResourceException
{
	if (r == null){
		if (width <= 0 || height <= 0) return null;
		return new Image(width,height);
	}
	if (r.width <= 0 || r.height <= 0) return new Image(1,1);
	else return new Image(r.width,r.height);
}
/**
 * This is used for paint buffering during a repaintNow() operation. If this
 * returns null then double buffering will not be used on the Control.
 * @return an Image for paint buffering.
 * @throws ewe.sys.SystemResourceException if an Buffer could not be created.
 */
public Image getControlBuffer() throws SystemResourceException
{
	return getControlBuffer(null);
}

//==================================================================
public void doPaint(Graphics g,Rect r)
//==================================================================
{

	int flags = getModifiers(true);
	if ((flags & Invisible) != 0) return;
	/*if ((borderStyle & BF_SOFT) != 0){
		g.draw3DRect(Rect.buff.set(0,0,width,height),BF_SOFT,true,getBackground(),null);
	}else{*/
		doBackground(g);
		g.setColor(Color.Black);
		doBorder(g);
	//}
}

//==================================================================
public void paintBackground(Graphics g)
//==================================================================
{
	if (hasModifier(Transparent,false)) {
		return;
	}
	g.setColor(getBackground());
	g.setDrawOp(g.DRAW_OVER);
	g.fillRect(0,0,width,height);
	//g.setColor(getForeground());
}

public Color backGround, foreGround;
public Font font;
public IImage image;

//===================================================================
public IImage getImage() {return image;}
//===================================================================

//===================================================================
public Font getFont()
//===================================================================
{
	for (Control c = this; c != null; c = c.parent)
		if (c.font != null) return c.font;
	return mApp.guiFont;
}
//==================================================================
public Color getBackground()
//==================================================================
{
	for (Control c = this; c != null; c = c.parent)
		if (c.backGround != null) return c.backGround;
	if (mApp.mainApp != null)
		if (mApp.mainApp.backGround != null)
			return mApp.mainApp.backGround;
	return Color.LightGray;
}
//==================================================================
public Color getForeground()
//==================================================================
{
	for (Control c = this; c != null; c = c.parent)
		if (c.foreGround != null) return c.foreGround;
	return Color.Black;
}
//==================================================================
public void doBorder(Graphics g)
//==================================================================
{
	if (borderWidth == 0 && borderStyle == 0) return;
	g.draw3DRect(getDim(null),borderStyle == 0 ? BDR_OUTLINE|BF_RECT : borderStyle, hasModifier(DrawFlat,true), null, borderColor);
}
//==================================================================
public void oldButWorksDoPaintChildren(Graphics g, int x, int y, int w, int h)
//==================================================================
{
	int flags = getModifiers(true);
	if ((flags & Invisible) != 0) return;
	Iterator kids = getChildren();
	if (!kids.hasNext()) return;
	Rect area = new Rect(x,y,w,h);
	Rect inter = new Rect(0,0,0,0);
	Rect oldClip = g.getClip(new Rect());
	Rect curClip = area.getIntersection(oldClip == null ? area:oldClip,null);
	while (kids.hasNext()){//kids.size()-1; i>=0; i--){
		Control mc = (Control)kids.next();
		Rect r = mc.getRect();
		curClip.getIntersection(r,inter);
		if (inter.width <= 0 || inter.height <= 0) continue;
		inter.x -= r.x; inter.y -= r.y;
		g.translate(r.x,r.y);
		//Rect prev = g.getClip(new Rect());

		g.setClip(inter.x,inter.y,inter.width,inter.height);
		//mc.paintBackground(g);
		mc.doPaint(g,inter);
		mc.doPaintChildren(g,inter.x,inter.y,inter.width,inter.height);
		if (oldClip != null) g.setClip(oldClip.x,oldClip.y,oldClip.width,oldClip.height);
		g.translate(-r.x,-r.y);
  }
}
protected static boolean haveNativePaint = true;
//==================================================================
public void doPaintChildren(Graphics g, int x, int y, int w, int h)
//==================================================================
{
	if (haveNativePaint)
		try{
			doPaintChildren(this,getModifiers(true),g,new Rect(x,y,w,h));
			return;
		}catch(Throwable e){
			haveNativePaint = false;
		}
	notNative_doPaintChildren(this,getModifiers(true),g,new Rect(x,y,w,h));

	//oldButWorksDoPaintChildren(g,x,y,w,h);
	//if (!Graphics.canCopy) // If can't then it must not be a native VM.
		//notNative_doPaintChildren(this,getModifiers(true),g,new Rect(x,y,w,h));
	//else
		//doPaintChildren(this,getModifiers(true),g,new Rect(x,y,w,h));
}

//==================================================================
public static native void doPaintChildren(Control who,int flags,Graphics g, Rect area);
//==================================================================

//==================================================================
public static void notNative_doPaintChildren(Control who,int flags,Graphics g, Rect area)
//==================================================================

{
	doPaintChildren(who,flags,g,area,g.getClip(new Rect()));
}
//-------------------------------------------------------------------
protected static void doPaintChildren(Control who,int flags,Graphics g,Rect area,Rect clip)
//-------------------------------------------------------------------
{
	int ax = area.x, ay = area.y, aw = area.width, ah = area.height;
	Rect curClip = new Rect();
	Control child;
	if ((flags & Invisible) != 0) return;
	if (who.children == null) return;

	if (clip == null) curClip.set(area);
	else area.getIntersection(clip,curClip);
	for (child = who.children; child != null; child = child.next){
		Rect r = new Rect();
		r.x = child.x; r.y = child.y; r.width = child.width; r.height = child.height;
		curClip.getIntersection(r,area);
		if (area.width <= 0 || area.height <= 0) continue;
		area.x -= r.x; area.y -= r.y;
		g.translate(r.x,r.y);
		g.setClip(area.x,area.y,area.width,area.height);
		//child.paintBackground(g);
		child.doPaint(g,area);
		//Vm.sleep(200);
		doPaintChildren(child,flags|child.modifiers,g,area,area);//inter.x,inter.y,inter.width,inter.height);
		if (clip != null) g.setClip(clip.x,clip.y,clip.width,clip.height);
		g.translate(-r.x,-r.y);
  }
	area.set(ax,ay,aw,ah);
}


public void oldPaintChildren(Graphics g, int x, int y, int width, int height)
	{
	Control child = children;
	while (child != null)
		{
		int x1 = x;
		int y1 = y;
		int x2 = x + width - 1;
		int y2 = y + height - 1;
		int cx1 = child.x;
		int cy1 = child.y;
		int cx2 = cx1 + child.width - 1;
		int cy2 = cy1 + child.height - 1;
		// trivial clip
		if (x2 < cx1 || x1 > cx2 || y2 < cy1 || y1 > cy2)
			{
			child = child.next;
			continue;
			}

		if (x1 < cx1)
			x1 = cx1;
		if (y1 < cy1)
			y1 = cy1;
		if (x2 > cx2)
			x2 = cx2;
		if (y2 > cy2)
			y2 = cy2;
		g.setClip(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
		g.translate(cx1, cy1);
		child.onPaint(g);
		g.clearClip();
		if (child instanceof Container)
			{
			Container c = (Container)child;
			c.paintChildren(g, x1 - cx1, y1 - cy1, x2 - x1 + 1, y2 - y1 + 1);
			}
		g.translate(- cx1, - cy1);
		child = child.next;
		}
	}
//==================================================================
public void paintChildren(Graphics gr, int x, int y, int w, int h)
//==================================================================
{
	if (mApp.mainApp == null) {
		oldPaintChildren(gr,x,y,w,h);
		return;
	}
	int flags = getModifiers(true);
	if ((flags & Invisible) != 0) return;
	gr.setClip(x,y,w,h);
	if (doubleBuffer){
		Image i = getControlBuffer();
		Graphics g = Graphics.createNew(i);
		//paintBackground(g);
		doPaintChildren(g,x,y,w,h);
		gr.drawImage(i,0,0);
		i.free();

	}else{
	  //paintBackground(gr);
		doPaintChildren(gr,x,y,w,h);
	}
	gr.clearClip();
}


//------------------------------------------------------------------
protected Vector listeners;
//===================================================================
public void addListener(EventListener list)
//===================================================================
{
	if (listeners == null) listeners = new Vector(1);
	listeners.remove(list);
	listeners.add(list);
}
//===================================================================
public void removeListener(EventListener list)
//===================================================================
{
	if (listeners == null) return;
	listeners.remove(list);
}
//===================================================================
public void sendToListeners(Event ev)
//===================================================================
{
	if (listeners != null)// && (ev instanceof ControlEvent))
		for (int i = 0; i<listeners.size(); i++){
			EventListener c = (EventListener)listeners.get(i);
			if ((c instanceof Control) && (ev instanceof PenEvent)) continue;
			c.onEvent(ev);
		}
}
public static final Point np = null;

//==================================================================
public void postEvent(Event ev)
//==================================================================
{
	//System.out.println(this+" "+ev);
	if (mApp.mainApp == null) {
		oldPostEvent(ev);
		return;
	}
	if (ev == null) return;
	if (!(ev instanceof KeyEvent))
		if (!fixFocus(ev)){
			sendToListeners(ev);
		}
	if ((ev instanceof PenEvent) && !hasModifier(SendUpPenEvents,false))  onEvent(ev);
	else if ((ev instanceof KeyEvent || ev instanceof TextEvent))  onEvent(ev);
	else {
		for (Control c = this; c != null; c = c.parent){
			//if (ev.consumed) break;
			c.onEvent(ev);
		}
	}
	//super.postEvent(ev);
}
//==================================================================
public boolean contains(Control who)
//==================================================================
{
	if (who == null) return false;
	for (Container c = who.parent; c != null; c = c.parent)
		if (c == this) return true;
	return false;
}

//-------------------------------------------------------------------
private final boolean fixFocus(Event ev)
//-------------------------------------------------------------------
{
	if (hasModifier(TakeControlEvents,false) && (ev.target != null) && (ev.target instanceof Control))
		if (contains((Control)ev.target) || ev.target == this){
			if (!(ev instanceof ControlEvent)) return false;
			if (hasModifier(TakeControlEvents,false) && (ev.target != null) && (ev.target instanceof Control))
			if (ev.type != ControlEvent.FOCUS_IN && ev.type != ControlEvent.FOCUS_OUT) return false;
			if (contains(((ControlEvent)ev).oldOrNewFocus) || ((ControlEvent)ev).oldOrNewFocus == this) return true;
		}
	return false;
}
//==================================================================
public void onControlEvent(ControlEvent ev)
//==================================================================
{
	if (hasModifier(TakeControlEvents,false) && (ev.target != null) && (ev.target instanceof Control))
		if (contains((Control)ev.target)){
			if (!fixFocus(ev)) {
				ev.target = this;
				if ((ev instanceof MenuEvent) && (ev.type == MenuEvent.SELECTED || ev.type == MenuEvent.ABORTED)){
					Object it = ((MenuEvent)ev).selectedItem;
					if (it instanceof MenuItem) lastSelected = (MenuItem)it;
				}
				sendToListeners(ev);
			}
		}
}
//==================================================================
public void onEvent(Event ev)
//==================================================================
{
	if (mApp.mainApp == null) return;
	if (ev.target == getMenu() && (ev instanceof MenuEvent)){
		popupMenuEvent((MenuEvent)ev);
	}else if (ev instanceof PenEvent){
		Control f = this;
		/*
		if (ev.type == PenEvent.SCROLL_UP || ev.type == PenEvent.SCROLL_DOWN){
			f = Gui.focusedControl();
			if (f == null) f = this;
		}
		*/
		f.onPenEvent((PenEvent)ev);
	}else if (ev instanceof KeyEvent){
		if (ev.type == KeyEvent.KEY_PRESS && Window.dragAndDropData != null && ((KeyEvent)ev).key == IKeys.ESCAPE && !ddMenuUp){
			try{
				Control c = (Control)Window.dragAndDropSource;
				PenEvent.resetCursor();
				c.dataTransferCancelled(Window.dragAndDropData);
				if (Window.lastDraggedOver != null)
					Window.lastDraggedOver.dataTransferCancelled(null);
				Window.dragAndDropData = null;
				c.dragging.cancelled = true;
			}catch(Exception e){}
			return;
		}
		Control f = Gui.focusedControl();
		if (f != null) {
			f.onKeyEvent((KeyEvent)ev);
	 		if (f.hasModifier(SendUpKeyEvents,false)){
				for (Control c = f.parent; c != null && !ev.consumed; c = c.parent)
					c.onKeyEvent((KeyEvent)ev);
			}
		}
	}else if (ev instanceof ControlEvent){
		onControlEvent((ControlEvent)ev);
	}else if (ev instanceof DataChangeEvent){
		if (contains((Control)ev.target) && hasModifier(TakeControlEvents,false)){
			ev.target = this;
			try{
				doChangeTransfer();
			}catch(Exception e){}
			sendToListeners(ev);
		}
	}else if (ev instanceof TextEvent){
		if (ev.type == TextEvent.TEXT_ENTERED){
			TextEvent te = (TextEvent)ev;
			if ((te.flags & te.FLAG_TEXT_WAS_ENTERED) != 0){
				setText(((TextEvent)ev).entered);
				notifyDataChange();
			}
		}
	}
}
//==================================================================
public int holdDownPause = 500; //Fifth of a second.
public int holdTick = 50; //Twenty times a second.
public  DragContext dragging;
//==================================================================

private static boolean holdDownCancelled = false;

public static void cancelHoldDown()
{
	holdDownCancelled = true;
}

private int holdId = 0;

//===================================================================
public static DragContext getDragAndDropContext()
//===================================================================
{
	if (Window.dragAndDropSource instanceof Control)
		return ((Control)Window.dragAndDropSource).dragging;
	return null;
}
//==================================================================
public void ticked(int id,int elapsed)
//==================================================================
{
	if (id == holdId){
		if (holdDownCancelled){
			holdDownCancelled = false;
			holdId = 0;
			return;
		}
		int myFlags = getModifiers(false);
		if ((penStatus & GotPenDown) != 0) {//((myFlags & GotPenDown) != 0) {
			if (((myFlags & WantDrag) != 0) && dragging.didDrag){
				if (!isOnMe(curPoint) && Window.dragAndDropData == null){
					dragging.curPoint.move(curPoint.x,curPoint.y);
					//dragging.penEvent = ev;
					dragged(dragging);
				}
			}else if (amOnTopFrame()){//Gui.focusedControl() == this){
				int s = ewe.sys.Vm.getAsyncKeyState(IKeys.PEN);
				if ((s & 0x8000) != 0){
					wantAnotherPenHeld = false;
					penHeld(curPoint);
					penStatus |= DidHoldDown;
					//modify(DidHoldDown,0);
					if (wantAnotherPenHeld) holdId = mApp.requestTick(this,holdTick);
				}
			}

		}
	}//else super.ticked(id,elapsed);
}
//===================================================================
public void gotFocus(int how)
//===================================================================
{
	//postEvent(new FocusEvent(FocusEvent.GOT_FOCUS,this));
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	//postEvent(new FocusEvent(FocusEvent.LOST_FOCUS,this));
}
//===================================================================
public final boolean isChildOf(Container who)
//===================================================================
{
	if (who == null) return false;
	for (Control c = this; c != null; c = c.parent)
		if (c == who) return true;
	return false;
}
//===================================================================
public boolean tryNext(boolean forwards)
//===================================================================
{
	if (parent == null) return false;
	Control c = parent.getNextKeyFocus(this,forwards);
	if (c == null) return false;
	Gui.takeFocus(c,ByKeyboard);
	return true;
}

/**
 * Format the String as a true Hotkey encoded string using the hotkey associated with this control (if any).
 * @param label The label to be displayed.
 * @return A hotkey encoded label for display.
 */
//===================================================================
public final String makeHot(String label)
//===================================================================
{
	return Gui.makeHot(label,hotKey);
}

public int [] exitKeys;

private ewe.sys.Lock exitLock;
/**
* If this returns false it means it is already in exitEntry.
**/
//===================================================================
public final boolean exitEntry(int exitKey,int modifiers)
//===================================================================
{
	if (exitLock == null) exitLock = new ewe.sys.Lock();
	exitLock.multipleEntry = false;
	if (!exitLock.grab()) return false;
	try{
		Control c = Gui.focusedControl();
		if (c == this || contains(c)) Gui.takeFocus(null,ByRequest);
		ControlEvent ce = new ControlEvent(ControlEvent.EXITED,this);
		ce.exitKey = exitKey;
		ce.exitKeyModifiers = modifiers;
		postEvent(ce);
	}finally{

		exitLock.release();
	}
	return true;
}

//-------------------------------------------------------------------
protected final boolean checkExitKey(KeyEvent ev)
//-------------------------------------------------------------------
{
	if (exitKeys != null){
		for (int i = 0; i<exitKeys.length; i++){
			if (exitKeys[i] == ev.key){
				exitEntry(ev.key,ev.modifiers);
				return true;
			}
		}
	}
	if (parent != null) return parent.checkExitKey(ev);
	return false;
}

//-------------------------------------------------------------------
protected boolean isSomeonesHotKey(KeyEvent ev)
//-------------------------------------------------------------------
{
	if (ev.type != ev.KEY_PRESS) return false;
	if (doHotKey(this,ev)) return true;
	if (parent == null) return false;
	return parent.doHotKey(this,ev);
}
/**
* This checks the incoming KeyEvent to see if it should cause this Control to show a
* menu. If it does then the Menu is shown and this returns true. Otherwise it returns false.
**/
//===================================================================
public boolean checkMenuKey(KeyEvent ev,Point where)
//===================================================================
{
	if (ev.type != ev.KEY_PRESS || ev.key != IKeys.MENU || menuIsActive()) return false;
	return doMenu(where);
}
/**
This is the standard Control onKeyEvent() - call this only after you have checked for
other keys that your control definitely wants to trap. This method will check for an
exit key, a menu key, will check for hot keys and check for keys that may cause a
shift to another control.
**/
//===================================================================
public void standardOnKeyEvent(KeyEvent ev)
//===================================================================
{
	if (ev.type != ev.KEY_PRESS) return;
	ev.consumed = true;
	if (checkExitKey(ev)) return;
	if (checkMenuKey(ev,new Point(0,0))) return;

	if ((ev.modifiers & IKeys.CONTROL) != 0) {
		ev.controlToLetter();
		if (!doHotKey(this,ev)){
					if (parent != null)
						if (!parent.doHotKey(this,ev))
							ev.consumed = false;
						else
							return;

		}else return;
	}else if (ev.key == IKeys.ENTER || ev.key == IKeys.ESCAPE){
		if (isSomeonesHotKey(ev))
			return;
	}
	{
		if (ev.key == IKeys.PREV_CONTROL || ev.key == IKeys.UP || ev.key == IKeys.LEFT || (ev.key == IKeys.TAB && ((ev.modifiers & IKeys.SHIFT) != 0))){
			tryNext(false);
		}else if (ev.key == IKeys.NEXT_CONTROL || ev.key == IKeys.DOWN || ev.key == IKeys.RIGHT || (ev.key == IKeys.TAB && ((ev.modifiers & IKeys.SHIFT) == 0))){
			tryNext(true);
		}else if (ev.key == IKeys.ENTER || ev.key == ' '){
			int flags = getModifiers(true);
			if (((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0))
				doActionKey(ev.key);
		}else if (!doHotKey(this,ev)){
			if (parent != null)
				if (!parent.doHotKey(this,ev))
					ev.consumed = false;
		}
	}
}
//==================================================================
public void onKeyEvent(KeyEvent ev)
//==================================================================
{
	standardOnKeyEvent(ev);
}

/**
 * This sets both the border style and width of the Control
 * @param style The border style. One or more of the BDR_XXX or EDGE_XXX constants ORed together.
 * @param width The distance between the edge of the Control and the data within it.
 * @return this Control
 */
//===================================================================
public final Control setBorder(int style,int width)
//===================================================================

{
	borderWidth = width;
	borderStyle = style;
	return this;
}
/**
 * Set the hot-key for this Control.
 * @param modifiers One or more of the IKeys.SHIFT, IKeys.CONTROL and IKeys.ONLY ORed together.
 * @param key The character for the hot-key.
 * @return this Control
 */
//===================================================================
public final Control setHotKey(int modifiers,char key)
//===================================================================
{
	hotKey = KeyEvent.toKey(modifiers,ewe.sys.Vm.getLocale().changeCase(key,false));
	return this;
}
/**
 * Set the hot-key for this Control.
 * @param modifiers One or more of the IKeys.SHIFT, IKeys.CONTROL and IKeys.ONLY ORed together.
 * @param key The KeyEvent key code for the hot-key.
 * @return this Control
 */
//===================================================================
public final Control setHotKey(int modifiers,int key)
//===================================================================
{

	hotKey = KeyEvent.toKey(modifiers,key);
	return this;
}
/**
* This method checks to see if a KeyEvent should be considered the Control's hot-key. If it
* is it calls doAction(ByKeyboard).
* @param from The Control (parent or sibling) that is passing the event to this Control.
* @param ev The KeyEvent representing the key press.
* @return true if the hot-key was accepted and handled.
*/
//-------------------------------------------------------------------
protected boolean doHotKey(Control from,KeyEvent ev)
//-------------------------------------------------------------------
{
	int flags = getModifiers(true);
	boolean en = (((flags & Disabled) == 0)  || ((flags & AlwaysEnabled) != 0));
	if (ev.isHotKey(hotKey) && en){
		doAction(ByKeyboard);
		notifyAction();
		return true;
	}
	return false;
}
//===================================================================
public void doActionKey(int key) {doAction(ByKeyboard); notifyAction();}
//===================================================================
public void doAction(int how){}
//===================================================================

//==================================================================
//==================================================================
public boolean checkPenTransparent(PenEvent ev)
//==================================================================
{
	if (!hasModifier(PenTransparent,false)) return false;
	Control c = getParent();
	if (c == null) return false;
	ev.target = c;
	ev.x += this.x;
	ev.y += this.y;
	c.onEvent(ev);
	return true;
}

//===================================================================
/**
* The minimum number of pixels that the Pen/Mouse has to move during a drag operation to
* be considered a drag to a new location.
**/
public int dragResolution = 3;
/**
* The number of pixels that the Pen/Mouse has to move before it is considered the start
* of a drag operation.
**/
public int startDragResolution = 3;

//==================================================================
public void notifyAction()
//==================================================================
{
	//Point p = null; p.x = 0;
	postEvent(new ControlEvent(ControlEvent.PRESSED,this));
}
/**
* Generate and dispatch a new DataChangeEvent.
**/
//===================================================================
public final void notifyDataChange()
//===================================================================
{
	notifyDataChange(new DataChangeEvent(DataChangeEvent.DATA_CHANGED,this));
}
//-------------------------------------------------------------------
private void doChangeTransfer()
//-------------------------------------------------------------------
{
	if (fieldTransfer != null){
		if ((modifiers & (NotEditable|DisplayOnly|Disabled)) != 0) fromField();
		else toField();
	}
}
/**
 * Dispatch a new DataChangeEvent.
 * @param dce The specific DataChangeEvent to send.
 */
//===================================================================
public void notifyDataChange(DataChangeEvent dce)
//===================================================================
{
	RuntimeException re = null;
	try{
		doChangeTransfer();
	}catch(RuntimeException e){
		re = e;
	}
	postEvent(dce);
	if (re != null) throw re;
}
/**
* This tells the control to consider its current data to be unchanged.
**/
//===================================================================
public void updateData()
//===================================================================
{
	for (Iterator it = getSubControls(); it.hasNext();){
		Object obj = it.next();
		if (obj instanceof Control)
			((Control)obj).updateData();
	}
}
/**
* This holds a MenuState Object used for controlling the Menu associated with
* the Control. You should not set this directly - it is set when you call <b>setMenu()</b>
**/
public MenuState menuState;


/**
 * Returns if the Menu associated with the Control is displayed.
 * @return true if the Menu associated with the Control is displayed.
 */
//===================================================================
public boolean menuIsActive()
//===================================================================
{
	if (menuState == null) return false;
	return menuState.isOpen();
}
/**
* If a menu for the control is being displayed, this will close it.
**/
//===================================================================
public void closeMenu()
//===================================================================
{
	if (menuState != null && menuState.isOpen())
		menuState.closeMenu();
}
/**
 * Associate a context sensitive menu with the Control. If there is already
	a menu associated with the Control it will be replaced by this new one.<p>
	You can set a Menu for most Controls including Buttons.
 * @param menu The Menu to set for the Control. This can be null.
 */
//===================================================================
public void setMenu(Menu menu)
//===================================================================
{
	if (menu == null){
		if (menuState != null){
			menuState.control = null;
			menuState.menu = null;
			menuState = null;
			return;
		}
	}
	if (menuState == null) menuState = new MenuState();
	menuState.menu = menu;
	menuState.control = this;
	menu.modify(KeepSIP,0);
}
/**
 * Return the Menu associated with the Control if any.
 * @return the Menu associated with the Control if any.
 */
//===================================================================
public Menu getMenu()
//===================================================================
{
	return
		menuState == null ? null:menuState.menu;
}
/**
 * This attempts to display a Menu for the Control if one exists AND is not already displayed.
	<p>By default it will call <b>doShowMenu(pen)</b> if the menu is not already active.
 * @param pen The point where the pen/mouse was pressed - or null if you are requesting the control to show
	its menu at no particular point.
 * @return true if the Menu was displayed by the method call.
 */
//===================================================================
public boolean tryStartMenu(Point pen)
//===================================================================
{
	if (menuIsActive()) return false;
	if (hasModifier(DisablePopupMenu,false)) return false;
	if (pen == null) pen = new Point(0,0);
	return doShowMenu(pen);
}
/**
 * Used to show the menu associated with the Control. This will be called by <b>tryStartMenu()</b> which
	is what you should use instead.
 * @param pen The point where the pen was pressed.
 * @return true always.
 */
//-------------------------------------------------------------------
protected boolean doShowMenu(Point pen)
//-------------------------------------------------------------------
{
	if (menuState == null) return false;
	menuState.doShowMenu(pen);
	return true;
}
/**
 * This is used to transfer a pen press to a Control that is contained
	within this child control tree of this Control.
 * @param toChild The child control to pass the pen press to.
 */
//===================================================================
public final void transferPenPress(Control toChild)
//===================================================================
{
	if (toChild == null) return;
	int dx = 0, dy = 0;
	for (Control c = toChild; c != this && c != null; c = c.getParent()){
		dx += c.x;
		dy += c.y;

	}
	transferPenPress(toChild,-dx,-dy);
}
/**
 * This is used to transfer a pen press to another Control.
* @param toWho The Control to send it to.
* @param dx The change in the x co-ordinate for the event.
* @param dy The change in the y co-ordinate for the event.
*/
//===================================================================
public final void transferPenPress(Control toWho,int dx,int dy)
//===================================================================
{
	modify(0,GotPenDown);
	getWindow().transferPenPress(currentPenEvent,toWho,dx,dy);
}

/**
* Use this to control the resolution of the pen drags - it is the minimum delay in milliseconds
* between processing pen drags. Set it to zero to get the highest
* resolution for dragging - best for when creating applications for drawing on the screen.
* Set it to a higer value to get less drag events and improve response. By default it is 100.
**/
public int dragTime = 100;

private static boolean ddMenuUp = false;

/**
* This is used to indicate that the control is going to show a pop-up Frame of some sort (e.g. for a Menu)
* in reaction to the specified PenEvent.
**/
//===================================================================
public boolean willShowFrame(PenEvent ev)
//===================================================================
{
	if (ev.type == ev.PEN_DOWN) return hasPopupFormAttached();
	return false;
}
/**
 * Called to handle an incoming PenEvent. By default this dispatches
	the event to the various pen handling methods like <b>penPressed()</b>
 * @param ev The incoming PenEvent.
 */
//==================================================================
public void onPenEvent(PenEvent ev)
//==================================================================
{
	currentPenEvent = ev;
	int flags = getModifiers(true);
	int myFlags = getModifiers(false);
	if (checkPenTransparent(ev)) return;
	curPoint.move(ev.x,ev.y);
	if (ev.type == ev.PEN_DOWN) {
		if (!firstPress) return;
		holdDownCancelled = false;
		firstPress = false;
		if (willShowFrame(ev)) ewe.sys.Vm.freezeSIP(this);
		Frame myFrame = getFrame();
		if (!amOnTopFrame()){
			//if (firstPress)
			//if (willShowFrame(ev)) ewe.sys.Vm.freezeSIP(this);
			boolean ma = menuIsActive();
			if (!Gui.pressedOutsideTopFrame(this,new Point(ev.x,ev.y))) return;
			if (ma && !menuIsActive()) return;
			if (!amOnTopFrame() && (myFrame == null || !myFrame.isControlPanel)){
				return;
			}
		}
		if (myFrame == null || !myFrame.isControlPanel)
			makeFrameTopMost();
		//Gui.frameOnTop(this);
		if (((flags & Disabled) != 0) && ((flags & AlwaysEnabled) == 0)){
			if ((flags & Invisible) == 0) Sound.beep();
			return;
		}else if ((myFlags & NoFocus) == 0 || Gui.focusedControl() == null) {
			Gui.takeFocus(this,ByMouse);
			//debugControl.setText("Focused control: "+Gui.focusedControl().name);
		}
		penStatus = (penStatus|GotPenDown)&~DidHoldDown;
		//modify(GotPenDown,DidHoldDown);
		dragging = new DragContext(ev);
		dragging.set(ev);
		dragging.resolution = startDragResolution;
		int dx = pressPoint.x - curPoint.x;
		int dy = pressPoint.y - curPoint.y;
		if (dx > 3 || dx < -3 || dy > 3 || dy < -3) lastClick -= doubleClickTime*2;
		pressPoint.move(curPoint.x,curPoint.y);
		penPressed(curPoint);
		if ((myFlags & WantHoldDown) != 0) {
			boolean wasC = holdDownCancelled;
			holdDownCancelled = false;
			if (!wasC) holdId = mApp.requestTick(this,holdDownPause);
		}
	}else if (ev.type == ev.PEN_UP) {
		holdId = 0;
		if (Window.dragAndDropData != null){
			Window.dragOverNew(this);
			boolean accepted = false;
			final Control c = Window.dragAndDropSource instanceof Control ? ((Control)Window.dragAndDropSource) : null;
			final DragContext dc = c == null ? null : c.dragging;
			if (acceptsData(Window.dragAndDropData,dc)){
				DragContext dc2 = dc;
				if (dc2 == null) {
					dc2 = new DragContext();
					dc2.dropOptions = null;
				}
				if (dc2.dropOptions != null){
					// The only way to be in here is if dc is the actual c.dragging - in which case c != null.
					PenEvent.resetCursor();
					final Menu m = new Menu();
					for (int i = 0; i<dc.dropOptions.length; i++){
						m.addItem(dc.dropOptions[i]);
					}
					m.addItem("-");
					m.addItem(new MenuItem("Cancel","cancel"));
					final Object theData = Window.dragAndDropData;
					final Point dp = new Point(curPoint.x,curPoint.y);
					ddMenuUp = true;
					m.modify(KeepSIP,0);
					m.exec(this,curPoint,new EventListener(){
						public void onEvent(Event ev){
							if (ev.target != m) return;
							if (ev.type == MenuEvent.SELECTED){
								m.close();
								ddMenuUp = false;
								MenuItem got = (MenuItem)((MenuEvent)ev).selectedItem;
								if (got.action.equals("cancel")){
									c.dataTransferCancelled(theData);
									dataTransferCancelled(null);
								}else{
									dc.dropAction = got.action;
									if (dataDroppedOn(theData,dp,dc)){
										c.dataAccepted(Control.this,theData,got.action);
									}else {
										c.dataTransferCancelled(theData);
										dataTransferCancelled(null);
									}
								}
							}else if (ev.type == MenuEvent.ABORTED){
								m.close();
								ddMenuUp = false;
								c.dataTransferCancelled(theData);
								dataTransferCancelled(null);
							}
						}
						});
					return;
				}else{
					String action = "copy";
					if (c != null){
						if (c.dragging.dropAction == null)
							action = c.dragging.dropAction = c.dragging.isCopy ? "copy" : "move";
					}
					if (dataDroppedOn(Window.dragAndDropData,curPoint,c == null ? null : c.dragging))
						if (c != null){
							c.dataAccepted(this,Window.dragAndDropData,action);
							accepted = true;

						}
				}
			}
			if (!accepted) {
				c.dataTransferCancelled(Window.dragAndDropData);
				dataTransferCancelled(null);
			}
			PenEvent.resetCursor();
			return;
		}
		if ((penStatus & GotPenDown) == 0) return; //(!hasModifier(GotPenDown,false)) return;
		penStatus &= ~GotPenDown;//modify(0,GotPenDown);
//..................................................................
		long cc = ewe.sys.Vm.getTimeStampLong();
		boolean doDouble = false;
		if ((int)(cc-lastClick) <= doubleClickTime && lastClickedControl == this) {
			lastClickedControl = null;
			doDouble = true;
		}else{
			lastClick = cc;
			lastClickedControl = this;
		}
//..................................................................
		if (dragging.didDrag && hasModifier(WantDrag,false)){
			dragging.curPoint.move(curPoint.x,curPoint.y);
			dragging.penEvent = ev;
			stopDragging(dragging);
		}else {
			if ((ev.modifiers & ev.RIGHT_BUTTON) != 0){
				penRightReleased(curPoint);
			}else{
				if ((penStatus & DidHoldDown) != 0) //(hasModifier(DidHoldDown,false))
					penReleased(curPoint);
				else if (doDouble) penDoubleClicked(curPoint);
				else penClicked(curPoint);
			}
		}
	}else if (ev.type == ev.PEN_DRAG){
		if (Window.nextMouseIsMove() && dragTime > 0){
			if (ewe.sys.Vm.getTimeStamp() - ev.timeStamp > dragTime) return;
		}
		if (Window.dragAndDropData != null){
			Window.dragOverNew(this);
			dataDraggedOver(Window.dragAndDropData,curPoint,ev);
			return;
		}
		if ((penStatus & GotPenDown) == 0) return;
		if (!dragging.hasDragged(curPoint)) return;

		holdId = 0;
		if (hasModifier(WantDrag,false) && !menuIsActive()){
			if (dragging.cancelled) return;
			dragging.resolution = dragResolution;
			dragging.curPoint.move(curPoint.x,curPoint.y);
			dragging.penEvent = ev;
			int now = ewe.sys.Vm.getTimeStamp();
			if (!dragging.didDrag) {
				dragging.lastDragTime = now;
				startDragging(dragging);
			}else{
				if (now < dragging.lastDragTime || now-dragging.lastDragTime>=dragging.rate){
					dragging.lastDragTime = now;

					dragged(dragging);
				}
			}
			dragging.prevPoint.set(dragging.curPoint);
		}
		dragging.didDrag = true;
	}else if (ev.type == ev.PEN_MOVED_ON && ((myFlags & MouseSensitive) != 0)){
		if ((flags & Disabled) == 0){
			penStatus |= PenIsOn;
			if (amOnTopFrame()) repaintNow();
		}
	}else if (ev.type == ev.PEN_MOVED_OFF && ((myFlags & MouseSensitive) != 0)){
		if ((flags & Disabled) == 0){
			penStatus &= ~PenIsOn;
			if (amOnTopFrame()) repaintNow();
		}
	}else if (ev.type == ev.SCROLL_UP || ev.type == ev.SCROLL_DOWN){
		if (this instanceof ScrollClient){
			ScrollClient sc = (ScrollClient)this;
			if (sc.getServer() != null){
				sc.doScroll(sc.needScrollBar(IScroll.Vertical,height) ? IScroll.Vertical : IScroll.Horizontal,ev.type == ev.SCROLL_UP ? IScroll.ScrollLower : IScroll.ScrollHigher, 0);
				return;
			}
		}
		if (parent != null) parent.onPenEvent(ev);
	}
}
/**
 * Checks if the Point p - which is relative to the top-left of this Control, is within the bounds
	of the Control.
 * @param p The point to check.
 * @return true if the point is within the Control.
 */
//==================================================================
protected boolean isOnMe(Point p)
//==================================================================
{
	if (p.x<0 || p.x>=width) return false;
	return !(p.y<0 || p.y>= height);
}
/**
* A temporary holding spot for data in the process of being moved.
**/
protected static Object beforeRemoved;
/**
* This is called either when data that was cut from this control was
* pasted into another control, OR when data that was dragged from this
* control is dropped into another control.<p>
* @param byWho The Control that accepted the data.
* @param data The data being transferred.
* @param action This is either "copy" or "move" indicating if the data
is meant to be copied to the other control or moved to the other Control.
*/
//-------------------------------------------------------------------

protected void dataAccepted(Control byWho,Object data,String action)
//-------------------------------------------------------------------
{
	if (action.equalsIgnoreCase("copy")) dataTransferCancelled(data);
}
/**
* This gets called either when that was cut from this control was not
* placed into another control (either it was rejected by a paste or a
* subsequent copy/cut into the clipboard occured before the data was pasted)
* or a drag and drop was initiated but not completed.
* @param data The data that was being transferred. If it is null it indicates
that this Control was a <b>destination</b> for a Drag and Drop operation, but the
operation was cancelled by the user.
*/
//-------------------------------------------------------------------
protected void dataTransferCancelled(Object data)
//-------------------------------------------------------------------
{
}
/**
* This tells the control that data is being removed from it either via
* a cut operation or by a drag operation that is not explicitly a copy operation.
* If the object is a Selectable object, then by default this will call
* delete selection. If you want more advanced operations (e.g. graying
* out the selection) you need to override this. If DragContext is null, it
* implies that it is via a "cut" operation, otherwise it is via a drag.
**/
//-------------------------------------------------------------------
protected void dataBeingRemoved(Object data,DragContext dc)
//-------------------------------------------------------------------
{

	if (this instanceof Selectable)

		if (canEdit())
			((Selectable)this).deleteSelection();
}
/**
* Called by a Control from within the startDragging() method to indicate that
* a DragAndDrop operation should begin.
* @param data The data to drag.
* @param dragCursor The cursor code for the special cursor to use. This should
be one of the ewe.sys.Vm.XXX_CURSOR constants OR a value returned by ewe.sys.Vm.createCursor().
* @param isCopy true if the operation is to be considered a copy operation. false if it is to be
considered a move operation.
* @return true if successful.
*/
//-------------------------------------------------------------------
protected boolean startDragAndDrop(Object data,int dragCursor,boolean isCopy)
//-------------------------------------------------------------------
{
	beforeRemoved = null;
	dragging.didDrag = true;
	dragging.isCopy = isCopy;
	if (!isCopy) dataBeingRemoved(data,dragging);
	return getWindow().beginDragAndDrop(this,data,dragCursor);
}
/**
* Called by a Control from within the startDragging() method to indicate that
* a DragAndDrop operation should begin.
* @param data The data to drag.
* @param isMultiple should be true if the data should be considered as multiple data. This
will affect the cursor that is used.
* @param isCopy true if the operation is to be considered a copy operation. false if it is to be
considered a move operation. This will affect the cursor that is used.
* @return true if successful.
*/
//-------------------------------------------------------------------
protected boolean startDragAndDrop(Object data,boolean isMultiple,boolean isCopy)
//-------------------------------------------------------------------
{
	beforeRemoved = null;
	dragging.didDrag = true;
	dragging.isCopy = isCopy;
	dragging.isMultiple = isMultiple;
	if (!isCopy) dataBeingRemoved(data,dragging);
	return getWindow().beginDragAndDrop(this,data,isMultiple,isCopy);
}
/**
	Called when the Pen or Mouse button is pressed on the control.
	This is called by the onPenEvent(PenEvent ev) method.<p>
	Within this method you can use the Control.currentPenEvent value
	to find out more about the PenEvent that caused this - including any
	modifiers (e.g. SHIFT keys) or determine if the left or right mouse
	button was pressed.<p>
 @param p The point on the Control where the pen was pressed.
 */
public void penPressed(Point p) {}
/**
	Called when the Pen or Mouse button is held down on the control. This will
	only get called if the WantHoldDown modifier has been set for this control.
	This is called by the onPenEvent(PenEvent ev) method.<p>
	Within this method you can use the Control.currentPenEvent value
	to find out more about the PenEvent that caused this - including any
	modifiers (e.g. SHIFT keys) or determine if the left or right mouse
	button was pressed.<p>
	This method is usually used to display a context sensitive menu. In fact
	the default code for it is:
	<pre>
		if (!menuIsActive()) doMenu(p);
	</pre>
 @param p The point on the Control where the pen was held.
 */
public void penHeld(Point p){if (!menuIsActive()) doMenu(p);}
/**
	Called when the right Mouse button is released on the control.
	This is called by the onPenEvent(PenEvent ev) method.<p>
	Within this method you can use the Control.currentPenEvent value
	to find out more about the PenEvent that caused this - including any
	modifiers (e.g. SHIFT keys) or determine if the left or right mouse
	button was pressed.<p>
	This method is usually used to display a context sensitive menu. In fact
	the default code for it is:
	<pre>
		if (!menuIsActive())
			if (doMenu(p)) return;
		penReleased(p);
	</pre>
 * @param p The point on the Control where the pen was released.
 */
public void penRightReleased(Point p)
{
	if (!menuIsActive())
		if (doMenu(p)) return;
	penReleased(p);
}
/**
	Called when the Pen or Mouse button is released on the control.
	This is called by the onPenEvent(PenEvent ev) method.<p>
	Within this method you can use the Control.currentPenEvent value
	to find out more about the PenEvent that caused this - including any
	modifiers (e.g. SHIFT keys) or determine if the left or right mouse
	button was pressed.<p>

 @param p The point on the Control where the pen was released.
 */
public void penReleased(Point p) {}
/**
	Called if the Pen or Mouse button is quickly pressed and released.
	This is called by the onPenEvent(PenEvent ev) method.<p>
	Within this method you can use the Control.currentPenEvent value
	to find out more about the PenEvent that caused this - including any
	modifiers (e.g. SHIFT keys) or determine if the left or right mouse
	button was pressed.<p>
	By default this method simply calls the penReleased() method.

 @param p The point on the Control where the pen was clicked.
 */
public void penClicked(Point p) {penReleased(p);}
/**
	Called if the Pen or Mouse button is double clicked on the Control.
	This is called by the onPenEvent(PenEvent ev) method.<p>
	Within this method you can use the Control.currentPenEvent value
	to find out more about the PenEvent that caused this - including any
	modifiers (e.g. SHIFT keys) or determine if the left or right mouse
	button was pressed.<p>

	By default this method simply calls the penClicked() method.
 @param p The point on the Control where the pen was double-clicked.
 */
public void penDoubleClicked(Point p) {penClicked(p);}
/**
 * This is a request to display a menu based on the pen being held or the mouse being right clicked on
	 the Control. By default this method calls <b>tryStartMenu(p);</b>
 * @param p The point where the pen was pressed or held.
 * @return true if a menu was displayed.
 */
public boolean doMenu(Point p){return tryStartMenu(p);}
/**
* This is called by a drop from a drag and drop operation. By default
* it will call takeData() unless it is from itself, in which case
* the data is rejected and this returns false.
**/
//===================================================================
public boolean dataDroppedOn(Object data,Point p,DragContext dc)
//===================================================================
{
	if (Window.dragAndDropSource == this) return false;
	return takeData(data,dc);
}
/**
 * This is called when data that was previously dragged into the control
	has now been dragged off.
 * @param data the data being dragged.
 */
//===================================================================
public void dataDraggedOff(Object data){}
//===================================================================

/**
 * This is called when data is being dragged into the control.
Within this method the control should call either willAcceptDrop()
or dontAcceptDrop() to indicate its willingness to accept the data.<p>
By default this method calls <b>dataDraggedOver(Object data)</b> -
which itself by default calls <b>acceptsData()</b> to determine whether
to call willAcceptDrop() or dontAcceptDrop().
 * @param data The data being dragged.
 * @param p The current location of the pen/mouse.
 * @param ev The PenEvent that prompted this call.

 */

//===================================================================
public void dataDraggedOver(Object data,Point p,PenEvent ev)
//===================================================================
{
	dataDraggedOver(data);
}
/**
 * This is called when data is being dragged into the control.
Within this method the control should call either willAcceptDrop()
or dontAcceptDrop() to indicate its willingness to accept the data.<p>
By default this method calls <b>acceptsData()</b> to determine whether
to call willAcceptDrop() or dontAcceptDrop().
 * @param data The data being dragged.
 */
//===================================================================
public void dataDraggedOver(Object data)
//===================================================================
{
	if (Window.dragAndDropSource == this) willAcceptDrop();
	else if (acceptsData(data,getDragAndDropContext())) willAcceptDrop();
	else dontAcceptDrop();

}
/**
 * This should indicate whether this control will accept the data either via
	 a drag and drop or via a paste operation.
 * @param data The data to be transferred.
 * @param how if the transfer is via DragAndDrop this will be a DragContext object,
	otherwise it will be null to indicate a clipboard operation.
 * @return true if the data will be accepted by this control.
 */
//===================================================================
public boolean acceptsData(Object data,DragContext how)
//===================================================================
{return false;}
/**
 * This tells the Control to take the data which has either come from a clipboard operation
	or from a DragAndDrop.
 * @param data The data to accept.

 * @param how if the transfer is via DragAndDrop this will be a DragContext object,
	otherwise it will be null to indicate a clipboard operation.
 * @return true if the data was successfully accepted.
 */
//===================================================================
public boolean takeData(Object data,DragContext how)
//===================================================================
{
	if (!acceptsData(data,how)) return false;
	if (this instanceof Selectable){
		if (!((Selectable)this).replaceSelection(data)) return false;
		notifyDataChange();
		return true;
	}else
		return false;
}

/**
 * Should be called from the dataDraggedOver() method to indicate that the Control is willing
	to accept the data.
 */
//-------------------------------------------------------------------
protected void dontAcceptDrop()

//-------------------------------------------------------------------
{
	ewe.sys.Vm.setCursor(this,ewe.sys.Vm.DONT_DROP_CURSOR);
}
/**
 * Should be called from the dataDraggedOver() method to indicate that the Control is not willing
	to accept the data.
 */
//-------------------------------------------------------------------
protected void willAcceptDrop()
//-------------------------------------------------------------------
{
	ewe.sys.Vm.setCursor(this,Window.dragAndDropCursor);
}
/*
public void penPressed(PenEvent ev) {penPressed(new Point(ev.x,ev.y));}
public void penReleased(PenEvent ev){penReleased(new Point(ev.x,ev.y));}
public void penClicked(PenEvent ev) {penClicked(new Point(ev.x,ev.y));}
public void penDoubleClicked(PenEvent ev) {penDoubleClicked(new Point(ev.x,ev.y));}
*/
/**
 * A quick way to provide DragAndDrop data. This will be called from tryDragAndDrop() which itself
	may be called from the startDragging() method.<p>
	To setup the DragContext you should set the dataToDrag, isMultiple and isCopy and (optionally)
	the dragCursor value.
 * @param dc the DragContext to setup for the drag and drop operation.
 * @return true if the data was successfully provided to drag and drop in the DragContext. false
	if there is no data to drag.
 */
//-------------------------------------------------------------------
protected boolean getDataToDragAndDrop(DragContext dc)
//-------------------------------------------------------------------
{return false;}

/**
* Call this from startDragging() if you want to try to do a drag and drop.
* @param dc the DragContext to setup for the drag and drop operation.
* @return
*/
//-------------------------------------------------------------------
protected boolean tryDragAndDrop(DragContext dc)
//-------------------------------------------------------------------
{
	dc.isCopy = ((dc.modifiers & IKeys.CONTROL) != 0);
	if (!getDataToDragAndDrop(dc)){
		dc.cancelled = true;

		return false;
	}
	if (dc.dragCursor != 0) startDragAndDrop(dc.dataToDrag,dc.dragCursor,dc.isCopy);
	else startDragAndDrop(dc.dataToDrag,dc.isMultiple,dc.isCopy);
	return true;
}
/**
 * This is called to indicate the start of a pen/mouse drag operation. This
	will only get called if the WantDrag modifier has been set for this control.
Within this method you can call startDragAndDrop() or tryDragAndDrop() to
begin a data DragAndDrop operation.
 * @param dc A DragContext indicating information about the dragging.
 */
//===================================================================
public void startDragging(DragContext dc)
//===================================================================
{dragged(dc);}
/**
 * Called when the pen was released during a drag operation. This will only
	be called if a data DragAndDrop was not initiated and if the cancelled member
	of the DragContext was not set true during the operation.
 * @param dc The DragContext for the dragging operation.
 */
//===================================================================
public void stopDragging(DragContext dc)
//===================================================================
{penReleased(dc.curPoint);}
/**
 * This is called during the drag operation. It is also called by default
	by the startDragging() method if you do not override it.
 * @param dc The DragContext for the dragging operation.
 */
//===================================================================
public void dragged(DragContext dc) {}
//===================================================================

//===================================================================
public void fillBackground(Graphics g)
//===================================================================
{
	if (g == null) return;
	if (!g.isValid()) return;
	Point p = new Point();
	Rect r = Gui.getRectInWindow(this,null,true,p);
	if (r == null) return;
	Window w = getWindow();

	g.translate(-p.x,-p.y);
	int m = modify(Invisible,0);
	w.repaintNow(g,r);
	restore(m,Invisible);
	g.translate(p.x,p.y);
}
/**
 * Return a Graphics context for this control for drawing directly onto the
	window surface.
 * @return a Graphics context for this control.
 */
//==================================================================
public Graphics getGraphics()
//==================================================================
{
	//if (hasModifier(Invisible,true)) return mGraphics.getEmptyGraphics();
	/*
	boolean debug = false;
  int x = 0,y = 0;
  Control c = this;
	Rect inter = Gui.getAppRect(this);
	Rect original = new Rect(0,0,0,0);
	original.set(inter);
  while (!(c instanceof ISurface)){
    Rect rect = c.getRect();
	  x += rect.x;
	  y += rect.y;
	  c = c.parent;
		if (c == null) return mGraphics.getEmptyGraphics();
		if (c.hasModifier(Invisible,false)) return mGraphics.getEmptyGraphics();
		Rect p = Gui.getAppRect(c);
		inter.getIntersection(p,inter);
  }
	Graphics g = null;
	if (c instanceof Window)

		if (!((Window)c).canDisplay) return null;
	if (c instanceof mApp) g = ((mApp)c).getGraphics(true);//false);
 	else g = Graphics.createNew((ISurface)c);
  g.translate(x,y);
	g.setClip(inter.x-x,inter.y-y,inter.width,inter.height);
	*/
	Point p = new Point();
	Rect r = Gui.getRectInWindow(this,null,true,p);
	if (r == null) {
		//Vm.debug("r == null");
		return null;
	}
	Window w = getWindow();
	Graphics g = Gui.getGraphics(w);
	if (g == null) {
		//Vm.debug("Gui.getGraphics() == null");
		return null;
	}
	if (!g.isValid()) {
		//Vm.debug("!g.isValid()");
		return null;
	}
	g.setClip(r.x,r.y,r.width,r.height);
	g.translate(p.x,p.y);

	g.setDrawOp(g.DRAW_OVER);
	g.setFont(getFont());
  return g;
}
//==================================================================
public Point getPosInParent(Container parent)
//==================================================================
{
	Point p = getLocation(null);
	Point p2 = new Point();
	for (Control c = getParent(); c != parent && c != null; c = c.parent){
		c.getLocation(p2);
		p.translate(p2.x,p2.y);
	}

	return p;
}
//==================================================================
public void makeFrameTopMost()
//==================================================================
{
	//if (true) return;
	Frame f = getFrame();
	if (this instanceof Frame) f = (Frame)this;
	if (f == null) return;
	Container mc = f.parent;
	if (mc == null) return;
	mc.makeFrameTopMost();
	for (Iterator it = mc.getChildrenBackwards(); it.hasNext();){
		Object obj = it.next();
		if (obj == f) return; //Already on top.
		if (obj instanceof Frame) break;
	}
	mc.remove(f);
	mc.add(f);//mc.addToFront(f);
	//mc.kids.removeElement(f);
	//mc.kids.insertElementAt(f,0);
	int got = f.modify(Invisible,0);
	Rect r = Gui.getAppRect(f);
	f.eraseSavedScreen();
	Gui.refreshScreen(getWindow(),r);
	f.resetRect();
	f.restore(got,Invisible);
	f.repaintNow();//Gui.refreshScreen(r);
}

//==================================================================
public void resetRect() {setRect(x,y,width,height);}
//==================================================================

/**
* The text associated with the control. You should use setText() to change it.
**/
public String text = emptyString;

//==================================================================
public void setText(String what) {text = what; if (text == null) text = ""; repaintNow();}
public String getText() {return text;}
/**
By default getDisplayText() returns getText();
*/
public String getDisplayText() {return getText();}
public String getPrompt()
{
	for (Control c = this;c != null;c = c.parent)
		if (c.prompt != null)
			if (c.prompt.length() != 0)
				return c.prompt;
	return emptyString;
}
//==================================================================
//==================================================================
public void show(Rect where)
//==================================================================
{
		setRect(where.x,where.y,where.width,where.height);
		shown();
}
//==================================================================
public void shown()
//==================================================================
{

	for (Iterator i = getChildren(); i.hasNext();)
		((Control)i.next()).shown();
}
/**
* This is called to indicate that the Form is about to close and it causes a FormClosed event to be posted.
* If you override it you should call super.formClosing(). There is no way of stopping the
* Form closing at this point.
**/
//===================================================================
protected void formClosing()
//===================================================================
{

	for (Iterator i = getSubControls(); i.hasNext();){
		Object obj = i.next();
		if (obj instanceof Control)
			((Control)obj).formClosing();
	}
}
/**
* This determines if the Control is being displayed in a modal frame.
**/
//===================================================================
public final boolean isModal()
//===================================================================
{
	for (Control c = this;c != null;c = c.parent)
		if (c instanceof Frame)
			if (((Frame)c).isModal) return true;
	return false;
}
//==================================================================
public Frame getFrame()
//==================================================================
{
	//if (myFrame != null) return myFrame;
	for (Control c = this;c != null;c = c.parent)
		if (c instanceof Frame && c != this) return (Frame)c;
	return null;
}
//===================================================================
public Window getWindow()
//===================================================================
{
	for (Control c = this; c != null; c = c.parent)
		if (c instanceof Window) return (Window)c;

	return null;
}
//==================================================================
public Container getFrameOrContainer()
//==================================================================
{
	for (Control c = this;c != null;c = c.parent)
		if (c instanceof Frame) return (Frame)c;
		else if (c.parent == null)
			if (c instanceof Container) return (Container)c;
			else return null;
	return null;
}
//==================================================================
public boolean amOnTopFrame()
//==================================================================
{
	if (getWindow() == null) return false;
	Frame tf = Gui.topFrame(this);
	if (tf == null) return true;
	for (Frame f = getFrame(); f != null; f = f.getFrame())
		if (f == tf) return true;
	return false;
}
//==================================================================
public Point getPosInFrame()
//==================================================================
{
	Point p = new Point(0,0);
	for (Control c = this;c != null;c = c.parent){
		if (c instanceof Frame) return p;
		Rect r = c.getRect();
		p.translate(r.x,r.y);
	}
	return p;
}

public int rows = 1,columns = 20;
//===================================================================
public FontMetrics getFontMetrics() {return getFontMetrics(getFont());}
//===================================================================
//==================================================================
public void onLabelPenEvent(PenEvent ev)
//==================================================================
{
	if (!(this instanceof Container)){
		ev.x = 2; ev.y = 2;
		onPenEvent(ev);
	}else{
		Control c = getNextKeyFocus(null,true);
		if (c != null) Gui.takeFocus(c,ByKeyboard);
	}
}
//==================================================================
public int constraints = STRETCH|FILL|CENTER;
public TagList tags = null;
//===================================================================
//===================================================================
public Control setControl(int val) {
	if ((val & (HEXPAND|HCONTRACT|VEXPAND|VCONTRACT)) == 0){
		if ((val & (LEFT|RIGHT)) == (LEFT|RIGHT)) val |= HFILL;
		if ((val & (TOP|BOTTOM)) == (TOP|BOTTOM)) val |= VFILL;
	}
	constraints = (constraints & ~CONTROLMASK)|(val & CONTROLMASK); return this;
}
public Control setCell(int val) {constraints = (constraints & ~CELLMASK)|(val & CELLMASK); return this;}
//===================================================================
public Control setTag(int tag,Object value)
//===================================================================
{
	if (tags == null) tags = new TagList();
	tags.set(tag,value);
	return this;
}
//===================================================================
public Object getTag(int tag,Object defaultValue)
//===================================================================
{
	if (tags == null) return defaultValue;
	return tags.getValue(tag,defaultValue);
}
//===================================================================
public Control clearTag(int tag)
//===================================================================
{
	if (tags != null) tags.clear(tag);
	return this;
}
//===================================================================
public Control setTags(TagList tl)
//===================================================================
{

	if (tags == null) tags = new TagList();
	tags.set(tl);
	return this;
}
//===================================================================
public Control defaultTo(int tag,Object value)
//===================================================================
{
	if (tags == null) tags = new TagList();
	tags.defaultTo(tag,value);
	return this;
}
//===================================================================
public boolean hasTag(int tag)
//===================================================================
{
	if (tags == null) return false;
	return tags.hasTag(tag);

}

/**
* Determine which sub-control should receive the keyboard focus. This method is normally
* called when the user moves the focus off of one control using TAB or the cursor keys.
* A control which has no children should not look for sub controls.
* @param sourceChild the child control from which the call came from, or null if the call
* came from the parent of this control, or this control itself.
* @param forwards true if the user wants to go to the next control, false if the user wants
* to go to the previous one.
* @return the control (which may be this control) that should receive the focus, or null
* if none should.
*/
//===================================================================
public Control getNextKeyFocus(Control sourceChild,boolean forwards)
//===================================================================
{
	if ((modifiers & TakesKeyFocus) != 0) return this;
	return null;
}
//===================================================================
public static void setClipObject(Object obj)
//===================================================================
{
	ewe.sys.Vm.setClipboardText(null);
	clipObject = obj;
	if (obj instanceof String []) {
		String [] all = (String [])obj;
		String out = new String();
		for (int i = 0; i<all.length; i++){
			if (all[i] != null) out += all[i];
			if (all.length != 1) out += "\n";
		}
		ewe.sys.Vm.setClipboardText(out);
	}else if (obj instanceof String){
		ewe.sys.Vm.setClipboardText((String)obj);
	}
}

//===================================================================
public static Object getClipObject()
//===================================================================
{
	if (clipOwner != null) return clipObject;
	String got = ewe.sys.Vm.getClipboardText(null);
	if (got == null) got = "";
	if (got.length() == 0) return clipObject;
	if (got.indexOf('\n') == -1) return got;
	return mString.split(got,'\n');
}
/**
 * This adds the standard clipboard menu to a menu to be used by this control.
 * @param addTo The menu under construction or null for a new menu.
 * @return the addTo parameter or a new Menu if it was null.
 */
//===================================================================
public Menu getClipboardMenu(Menu addTo)
//===================================================================
{
	if (clipItems == null){
		clipItems = new MenuItem[]
		{
			new MenuItem("Copy$c","ewe/copysmall.bmp",Color.White),
			new MenuItem("Cut$t","ewe/cutsmall.bmp",Color.White),
			new MenuItem("Paste$p","ewe/pastesmall.bmp",Color.White),
			//,new MenuItem("Exchange",0,null)
		};
	}
	if (addTo == null) addTo = new Menu();
	addTo.addSection(clipItems,true);
	return addTo;
}

//===================================================================
public boolean canEdit()
//===================================================================
{
	int flags = getModifiers(true);
	boolean editable = (((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
	return editable;
}
/**
* This is called before a menu is displayed. You can use it to adjust the items in the menu. If it returns
* false the menu will not be displayed.
**/
//===================================================================
public boolean checkMenu(Menu m)
//===================================================================
{
	return checkClipboardOperations(m);
}
/**
 * This is called to enable/disable the clipboard options depending on the state of the Control.
 * @param m The Menu which contains the clipboard operations.
 * @return always true.
 */
//===================================================================
public boolean checkClipboardOperations(Menu m)
//===================================================================
{
	int flags = getModifiers(true);
	boolean editable = (((flags & (Disabled|NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
	if (clipItems == null) return true;
	/*
	int s = m.itemsSize();
	for (int i = 0; i<s; i++){
		MenuItem mi = m.getItemAt(i);
		if (mi == clipItems[1] || mi == clipItems[2] || mi == clipItems[3]){
			mi.modifiers &= ~mi.Disabled;
			if (!editable) mi.modifiers |= mi.Disabled;
		}
	}
	*/
	clipItems[0].modifiers &= ~MenuItem.Disabled;
	if (editable){
		clipItems[1].modifiers &= ~MenuItem.Disabled;
		clipItems[2].modifiers &= ~MenuItem.Disabled;
	}else{
		clipItems[1].modifiers |= MenuItem.Disabled;
		clipItems[2].modifiers |= MenuItem.Disabled;
	}
	Object obj = getClipObject();
	if (obj == null) clipItems[2].modifiers |= MenuItem.Disabled;
	else if (!acceptsData(obj,null))
		clipItems[2].modifiers |= MenuItem.Disabled;
	if (this instanceof Selectable)
		if (!((Selectable)this).hasSelection()) {
			clipItems[0].modifiers |= MenuItem.Disabled;
			clipItems[1].modifiers |= MenuItem.Disabled;
		}
	return true;
}
/**
* This is called to get data from the control to put into the clipboard.
**/
//-------------------------------------------------------------------
protected Object getDataToCopy()
//-------------------------------------------------------------------
{
	if (!(this instanceof Selectable)) return null;
	Selectable s = (Selectable)this;
	return s.getSelection();
}
/**
* This calls takeData(data,null) and then calls dataAccepted(this,data,action)
* on the clipOwner.

**/
//-------------------------------------------------------------------
protected void takeFromClipboard(Object clip,String action)
//-------------------------------------------------------------------
{
	boolean took = takeData(clip,null);
	if (clipOwner != null){
		if (took) clipOwner.dataAccepted(this,clip,action);
		else clipOwner.dataTransferCancelled(clip);
		setClipObject(null);
		clipOwner = null;
	}
}
/**
* This converts an object into either a String or an array of Strings. If the Object
* cannot be converted into either, it will return null.
**/
//===================================================================
public final static Object toTextData(Object data)
//===================================================================
{
	if (data == null) return null;

	if (data instanceof String []) return data;
	return data.toString();
}

//===================================================================
public Object clipboardTransfer(Object clip,boolean toClipboard,boolean cut)
//===================================================================
{
	Object ret = clip;
	if (!toClipboard){ //................................Paste operation.
		if (clip == null) return clip;
		takeFromClipboard(clip,"move");

		return null;
	}
	//..................................................Cut/copy operation.
	if (clipOwner != null) clipOwner.dataTransferCancelled(clip);
	//..................................................
	ret = getDataToCopy();
	if (ret == null) return null;
	int flags = getModifiers(true);
	boolean editable = (((flags & (Disabled|NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0));
	if (cut && editable) {
		clipOwner = this;
		dataBeingRemoved(ret,null);
	}
	return ret;
}
/**
* Copy the selected contents of the control to clipboard. If cut is true
* the selected contents will be removed from the control if it is editable.
**/
//===================================================================
public void toClipboard(boolean cut)
//===================================================================
{
	setClipObject(clipboardTransfer(getClipObject(),true,cut));
}
/**
* Paste the contents of the clipboard into the control, overwriting any
* exisiting selected area. If exchange is true the selected area in the


* control will replace the current clipboard data.
**/
//===================================================================
public void fromClipboard()
//===================================================================
{
	clipboardTransfer(getClipObject(),false,false);
	notifyDataChange();
}
/**
* This gets closed after the popup menu for the Control has closed. You can clear the menu
* by doing setMenu(null).
**/
//-------------------------------------------------------------------
protected void popupMenuClosed(Menu m)
//-------------------------------------------------------------------
{
}
/**
 * This is called when the popup menu associated with the control generates an event.
	By default it calls popupMenuEvent(Object selectedItem) if the selectedItem is not null.
 * @param ev The event.
 */
//===================================================================
public void popupMenuEvent(MenuEvent ev)
//===================================================================
{
	if (ev.selectedItem != null)
		popupMenuEvent(ev.selectedItem);
}
/**
 * This is called when the user selects an item from the popup menu.
 * @param selectedItem The item selected.
 */
//===================================================================
public void popupMenuEvent(Object selectedItem)
//===================================================================

{
	if (clipItems == null) return;
	if (selectedItem == clipItems[0]) setClipObject(clipboardTransfer(getClipObject(),true,false));
	else if (selectedItem == clipItems[1]) setClipObject(clipboardTransfer(getClipObject(),true,true));
	else if (selectedItem == clipItems[2]) clipboardTransfer(getClipObject(),false,false);
	//else if (selectedItem == clipItems[3]) setClipObject(clipboardTransfer(getClipObject(),false,true));
	if (selectedItem == clipItems[1] || selectedItem == clipItems[2]) {
		repaintNow();
		notifyDataChange();
	}
}
//===================================================================
public String clipboardToString(Object what)
//===================================================================
{
	if (what == null) return new String();
	if (what instanceof Object []){
		Object [] w = (Object []) what;
		String s = new String();
		for (int i = 0; i<w.length; i++)
			s += clipboardToString(w[i])+"\n";
		return s;
	}else return what.toString();
}
/**
* If a Control is in the middle of a cut operation this will cancel the
* operation. If forWho is the source of the cut operation, OR forWho is
* null, then the operation will be cancelled.
**/

//===================================================================
public static void cancelCut(Control forWho)
//===================================================================
{
	if (clipOwner == null) return;
	if (clipOwner == forWho || forWho == null){
		clipOwner.dataTransferCancelled(clipObject);
		clipOwner = null;
		setClipObject(null);
	}
}
/**
* This is the sound that is played when a popup-menu or pull-down menu is first
* shown. Set this to null to disable this sound, or change it for a different sound.
**/
public static String popupSound = ewe.sys.Vm.isMobile() ? "ewe/Popup.wav": null;

public static ewe.fx.SoundClip popupSoundClip = null;
/**
* This is called when a popup-menu or pull-down menu is first shown. It can
* also be called at any time for any other purpose. If you set popupSound to
* null, then no beep will be played at these times.
**/
//===================================================================
public static void popupBeep()
//===================================================================
{
	if (popupSound != null){
		//new ewe.fx.SoundClip(popupSound);
		if (popupSoundClip == null)
			popupSoundClip = new ewe.fx.SoundClip(popupSound);
	}
	if (popupSoundClip != null)
		popupSoundClip.play(Vm.SND_ASYNC);
}


/**
 * Set the cursor for the control.
 * @param cursor Should be one of the ewe.sys.Vm.XXXX_CURSOR constants
	or a value returned from ewe.sys.Vm.createCursor().
 */
//===================================================================
public void setCursor(int cursor)
//===================================================================
{
	PenEvent.setCursor(this,cursor);
}
//-------------------------------------------------------------------
protected MenuItem lastSelected = null;
//-------------------------------------------------------------------
/**
* Find the menu item which was last selected. This may be from a sub menu.
**/
//===================================================================
public MenuItem getLastSelected()
//===================================================================
{
	return lastSelected;
}

protected ScrollServer ss;
public void setServer(ScrollServer server){ss = server;}
public ScrollServer getServer() {return ss;}

//===================================================================
public void takeFocus(int how)
//===================================================================
{
	if (hasModifier(NoFocus,false)) return;
	Gui.takeFocus(this,how);
}

public FieldTransfer fieldTransfer;

//===================================================================
public void fromField(FieldTransfer ft)
//===================================================================
{
	if (ft == null) return;
	if (ft.dataInterface != this) return;
	ft.transfer(ft.FROM_OBJECT);

}
//===================================================================
public void toField(FieldTransfer ft)
//===================================================================
{
	if (ft == null) return;
	if (ft.dataInterface != this) return;
	ft.transfer(ft.TO_OBJECT);
	//ewe.sys.Vm.debug("Transfered: "+ft.fieldName);
}
//===================================================================
public final void fromField() {fromField(fieldTransfer);}
public final void toField() {toField(fieldTransfer);}
//===================================================================
public static int standardEdge = EDGE_RAISED |
(((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_MOUSE_POINTER) != 0) ? Control.BDR_OUTLINE : 0);
public static int standardBorder = EDGE_ETCHED;


/**
 * This sets up the Control, such that if the "other" control generates a DataChangeEvent
 * then this Control will also generate a DataChangeEvent.
 * @param other The Control to listen for DataChangeEvents.
* @param fieldName An optional name to give the chained control.
*/
//===================================================================
public final void chainDataChange(Control other,final String fieldName)
//===================================================================
{
	other.addListener(new EventListener(){
		public void onEvent(Event ev){
			if (ev instanceof DataChangeEvent){
				DataChangeEvent dce = new DataChangeEvent(DataChangeEvent.DATA_CHANGED,Control.this);
				dce.fieldName = fieldName;
				dce.cause = ev;
				notifyDataChange(dce);
			}
		}
	});
}
//===================================================================
/**
 * This is called to update a Control on-screen <b>after</b> it has been scrolled.
 * In this case "being scrolled" means that the internal display co-ordinates of the
 * Control have been altered but a repaint has not been done. This method attempts to
 * do the most effecient repaint depending on the capabilities of the underlying graphics
 * system.<p>
 * If data can be moved directly on screen, then this will be done and only the
 * newly exposed portion will be updated via a repaintNow() operation. If no moving
 * is possible at all - or if the SIP may be covering the control then no scrolling
 * will be done.
 * @param sx The source x co-ordinate.
 * @param sy The source y co-ordinate.
 * @param sw The source width.
 * @param sh The source height.
 * @param destX The destination x co-ordinate.
 * @param destY The destination y co-ordinate.
 */
public void scrollAndRepaint(int sx,int sy,int sw,int sh,int destX,int destY)
//===================================================================
{
	if (!Graphics.canMove || (Vm.getSIP() != 0 && !(this instanceof EditControl))){
 		repaintNow();
 		return;
	}
	Rect srcScroll = new Rect(sx,sy,sw,sh);
	Rect dstScroll = new Rect(0,0,width,height);
	//
	// First make sure the source rectangle is within what is actually
	// in the control.
	//
	srcScroll.getIntersection(dstScroll,srcScroll);
	destX += srcScroll.x-sx;
	destY += srcScroll.y-sy;
	sx = srcScroll.x; sy = srcScroll.y; sw = srcScroll.width; sh = srcScroll.height;
	//
	// If nothing on screen actually moves then just repaint the whole thing.
	//
	if (sw <= 0 || sh <= 0){
		repaintNow();
		return;
	}
	//
	// Next see where it would actually end up on screen.
	//
	srcScroll.x = destX; srcScroll.y = destY;
	srcScroll.getIntersection(dstScroll,srcScroll);
	sx += srcScroll.x-destX;
	sy += srcScroll.y-destY;
	destX = srcScroll.x; destY = srcScroll.y; sw = srcScroll.width; sh = srcScroll.height;
	//
	// If nothing on screen actually moves then just repaint the whole thing.
	//
	if (sw <= 0 || sh <= 0){
		repaintNow();
		return;
	}
	//
	Graphics gr = getGraphics(), g = null;
	if (gr == null) return;
	Image i = null;
	g = gr;
	if (!doubleBuffer || !Graphics.canCopy) {
		gr.moveRect(sx,sy,sw,sh,destX,destY);
	}else{
		i = getControlBuffer();
		if (i == null){
			gr.moveRect(sx,sy,sw,sh,destX,destY);
		}else{
			g = Graphics.createNew(i);
			g.copyRect(gr,0,0,width,height,0,0);
			g.copyRect(gr,sx,sy,sw,sh,destX,destY);
			g.setFont(getFont());
		}
	}
	boolean was = doubleBuffer;
	try{
		if (g != gr) doubleBuffer = false;
		Rect r = srcScroll;
		boolean didVertical = false;
		if (destX != sx){
			didVertical = true;
			r.set(destX < sx ? destX+sw : 0,0,width-sw,height);
			repaintNow(g,r);
		}
		if (sh < height){
			r.set(didVertical ? destX: 0, destY < sy ? destY+sh : 0, didVertical ? sw : width,height-sh);
			repaintNow(g,r);
		}
		/*
		Rect r = new Rect().set(sx,sy,sw,sh);
		if (sx != destX){
			r.x = sx < destX ? sx : destX+sw;
			r.width = sx < destX ? destX-sx : sx-destX;
			repaintNow(g,r);
		}
		r.set(sx,sy,sw,sh);
		if (sy != destY){
			r.y = sy < destY ? sy : destY+sh;
			r.height = sy < destY ? destY-sy : sy-destY;
			repaintNow(g,r);
		}
		*/
		if (i != null){
			g.free();
			gr.drawImage(i,0,0);
			i.free();
		}
		gr.free();
	}finally{
		doubleBuffer = was;
	}
}
/**
 * This returns either the value of prompt, if it is not null and not an empty string, or
 * the text value of promptControl, if that is not null and not an empty string. If no non-empty
 * string prompt can be found, it will return null.
 */
//===================================================================
public String getPromptText()
//===================================================================
{
	if (prompt != null && prompt.length() != 0) return prompt;
	if (promptControl == null) return null;
	String p = promptControl.getText();
	if (p != null && p.length() != 0) return p;
	return null;
}


/**
 * Set the Font for the control.
 * @param f the Font to set.
 * @return this Control.
 */
//===================================================================
public Control setFont(Font f)
//===================================================================
{
	font = f;
	return this;
}
/**
 * Call this if you are going to be painting all or a portion of the control at an
 * arbitrary time. If it returns false then part of the control may be covered and
 * you should therefore not do a paint at this time.<p>
 * This method simply calls: Gui.requestPaint(this);
 * @return true if it is OK to go ahead and paint, false if you should not paint now.
 */
//===================================================================
public boolean requestPaint()
//===================================================================
{
	return Gui.requestPaint(this);
}


/**
 * The default version of this method simpy repaints the control - however for controls
 * which may display multiple images a better refresh scheme would be needed.
 * @param image the image to refresh.
 * @param options
 * @return
 */
//===================================================================
public void refresh(IImage image, int options)
//===================================================================
{
	repaintNow();
}

/**
If this Control represents the input of a password that must be kept
hidden, then this method should return a password character (usually a '*').
If not then this should return (char)0.
**/
//===================================================================
public char getPasswordCharacter()
//===================================================================
{
	return 0;
}
/**
Returns true if a ControlPopupForm is attached to this Control.
**/
//===================================================================
public boolean hasPopupFormAttached()
//===================================================================
{
	if (listeners == null) return false;
	for (int i = 0; i<listeners.size(); i++)
		if (listeners.get(i) instanceof ControlPopupForm) return true;
	return false;
}
//##################################################################
}
//##################################################################

//##################################################################
//MLB
class ModifierRecord {
//##################################################################

int mask, value;

//##################################################################
}
//##################################################################

