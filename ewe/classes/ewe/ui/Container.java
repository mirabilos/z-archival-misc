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
import ewe.util.Iterator;
import ewe.sys.Vm;
/**
 * Container is a control that contains child controls.
 */

public class Container extends Control
{
/**
* If this is set to true, then when child Controls gain the focus the GUI
* will not attempt to scroll so that the focused Control is visible.
**/
public boolean dontAutoScroll = false;

//===================================================================
public void addDirectly(Control control)
//===================================================================
{
	add(control);
}
/**
 * Adds a child control to this container.
 */
//===================================================================
public void add(Control control)
//===================================================================
	{
	if (control.parent != null)
		control.parent.remove(control);
	// set children, next, prev, tail and parent
	control.next = null;
	if (children == null)
		children = control;
	else
		tail.next = control;
	control.prev = tail;
	tail = control;
	control.parent = this;
	control.penStatus |= PenIsOn;
	if (!(control instanceof Container) && hasModifier(MouseSensitive,false))
		control.modify(MouseSensitive,0);
	if (control.hasModifier(MouseSensitive,false) && !(control instanceof Container) && ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_NO_MOUSE_POINTER) == 0)){
		PenEvent.wantPenMoved(control,PenEvent.WANT_PEN_MOVED_ONOFF,true);
		control.penStatus &= ~PenIsOn;
	}
	// numChildren++;
	// MLB
	if (mApp.mainApp == null) control.repaint();
	}

/**
 * Removes a child control from the container.
 */
public void remove(Control control)
	{
	if (control.parent != this)
		return;
	// set children, next, prev, tail and parent
	Control prev = control.prev;
	Control next = control.next;
	if (prev == null)
		children = next;
	else
		prev.next = next;
	if (next != null)
		next.prev = prev;
	if (tail == control)
		tail = prev;
	control.next = null;
	control.prev = null;
	// numChildren--;
	// MLB
	if (mApp.mainApp == null) control.repaint();
	control.parent = null;
	}


/** Returns the child located at the given x and y coordinates. */
public Control findChild(int x, int y)
	{
	Container container;
	Control child;

	container = this;
	while (true)
		{
		// search tail to head since paint goes head to tail
		child = container.tail;
		while (child != null && !child.contains(x, y))
			child = child.prev;
		if (child == null)
			return container;
		if (!(child instanceof Container))
			return child;
		x -= child.x;
		y -= child.y;
		container = (Container)child;
		}
	}

/** Called by the system to draw the children of the container. */
public void _paintChildren(Graphics g, int x, int y, int width, int height)
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
// MLB
public Iterator getChildren() {return new ControlIterator(children,false);}
public Iterator getChildrenBackwards() {return new ControlIterator(tail,true);}
//===================================================================
  public void repaintDataNow()
//===================================================================
{
	for (Iterator it = getChildren(); it.hasNext();)
		((Control)it.next()).repaintDataNow();
}

//MLB
//===================================================================
public void removeAll()
//===================================================================
{
	while(children != null) remove(children);
}

//===================================================================
public void dismantle(Control stopAt)
//===================================================================
{
	if (stopAt == this) return;
	Iterator it = getChildren();
	while(it.hasNext()){
		Control c = (Control)it.next();
		if (c instanceof Container)
			((Container)c).dismantle(stopAt);
	}
	removeAll();
}
//===================================================================
public final void dismantle()
//===================================================================
{
	dismantle(null);
}
//-------------------------------------------------------------------
protected boolean doHotKey(Control from,KeyEvent ev)
//-------------------------------------------------------------------
{
	if (super.doHotKey(from,ev)) return true;
	Iterator it = getChildren();
	while(it.hasNext()) {
		Control c2 = (Control)it.next();
		if (c2 == from) continue;
		if (c2.doHotKey(this,ev)) return true;
	}
	if (from == parent || this instanceof Frame || parent == null) return false;
	return parent.doHotKey(this,ev);
}

/**
If this is true then the keyboard focus cannot be moved to the Container's child controls
using the TAB or cursor keys. You should also call modifyAll(NoFocus,TakesKeyFocus,false)
on the Container after adding the sub-controls.
**/
public boolean dontFocusOnChildren = false;

/**
If this is true then the keyboard focus cannot be moved outside of this Container using
the TAB or cursor keys.
**/
public boolean closedFocus = false;
/**
If this is true then the keyboard focus will cycle from the first to the last control
in this container when the focus has moved to the very first or very last control.
**/
public boolean cycleFocus = false;
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
	if (dontFocusOnChildren) return super.getNextKeyFocus(sourceChild,forwards);
	Iterator it = forwards ? getChildren() : getChildrenBackwards();

	if (sourceChild == null){ // Request came from parent or myself, so find the first/last available control.
		while(it.hasNext()) {
			Control c2 = (Control)it.next();
			Control c =  c2.getNextKeyFocus(null,forwards);
			if (c != null) return c;
		}
		return null;
	}

	// It came from one of my children, so find the next control.
	while(it.hasNext()){
		Control c = (Control)it.next();
		if (c == sourceChild) {
			while(it.hasNext()){
				Control c2 = (Control)it.next();
				Control ch = c2.getNextKeyFocus(null,forwards);
				if (ch != null) return ch;
			}
		}
	}
	// Could not find another one inside me, so ask my parent.
	Control pc = ((parent == null) || closedFocus ? null : parent.getNextKeyFocus(this,forwards));
	if (pc != null) return pc;
	if (!cycleFocus) return null;
	return getNextKeyFocus(null,forwards);
}

//-------------------------------------------------------------------
protected Control getFirstFocus()
//-------------------------------------------------------------------
{
	Iterator it = getChildren();
	while(it.hasNext()){
		Control c2 = (Control)it.next();
		Control c = (c2 instanceof Container) ? ((Container)c2).getFirstFocus() : c2.getNextKeyFocus(null,true);
		if (c != null) return c;
	}
	return null;
}
/**
 * On a normal Control this will set the promptControl variable to "prompt", but
 * on a Container, the first child non-container will have "prompt" assigned to it.
 * @param prompt the Control acting as the prompt (usually an mLabel).
 */
protected boolean takePromptControl(Control prompt)
{
	for (Iterator it = getSubControls(); it.hasNext();)
		if (((Control)it.next()).takePromptControl(prompt)) return true;
	return false;
}

private boolean takeTheFocus = false;
private boolean haveTheFocus = false;
/**
Returns true if the Container itself has the focus instead of one of its
children.
**/
//===================================================================
public boolean containerHasFocus()
//===================================================================
{
	return haveTheFocus;
}
/**
Put the focus on the container itself rather than any of its children.
@param how one of the Control.ByXXX values (e.g. ByRequest)
**/
//===================================================================
public void focusOnContainer(int how)
//===================================================================
{
	takeTheFocus = true;
	Gui.takeFocus(this,how);
}
/**
Put the focus on the data within the container. This calls focusFirst().
@param how one of the Control.ByXXX values (e.g. ByRequest)
**/
//===================================================================
public void focusOnData(int how)
//===================================================================
{
	focusFirst(how);
}
/**
If a container gets the focus via an explicit focusOnContainer() then it
will take the focus, otherwise it will call pass focus to the first
control within it that wants the focus. If there are none then it will
take the focus itself.
**/
//===================================================================
public void gotFocus(int how)
//===================================================================
{
	if (takeTheFocus){
		haveTheFocus = true;
		takeTheFocus = false;
	}else{
	//if (how == ByPen) return;
		Control c = getFirstFocus();
		if (c != null) Gui.takeFocus(c,how);//focusFirst();
		else haveTheFocus = true;
	}
}
//===================================================================
public void lostFocus(int how)
//===================================================================
{
	haveTheFocus = false;
}
//===================================================================
public void focusFirst()
//===================================================================
{
	focusFirst(ByRequest);
}
//===================================================================
public void focusFirst(int how)
//===================================================================
{
	Control c = getFirstFocus();
	if (c == null) c = this;
	Gui.takeFocus(c,how);
	//Window w = getWindow();
	//if (w != null) w.setFocus(c);
}
//===================================================================
public void takeFocus(int how)
//===================================================================
{
	focusFirst(how);
}

//===================================================================
public boolean scrollToVisible(int x, int y, int width, int height)
//===================================================================
{
	if (parent != null){
		return parent.scrollToVisible(x+this.x,y+this.y, width, height);
	}else{
		return false;
	}
}

}
//##################################################################
class ControlIterator extends ewe.util.IteratorEnumerator{
//##################################################################

Control current;
boolean back;
public ControlIterator(Control first,boolean backwards)
{
	current = first;
	back = backwards;
}
public boolean hasNext() {return current != null;}
public Object next() {Control c = current; if (c != null) current = back ? c.prev:c.next; return c;}

//##################################################################
}
//##################################################################

