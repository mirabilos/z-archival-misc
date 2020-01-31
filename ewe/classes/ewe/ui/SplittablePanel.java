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
/**
* This class supports a moveable PanelSplitter between two CellPanels. ONLY two
* CellPanels are supported - any attempt to create more will throw a RuntimeException.
* Call getNextPanel() twice to get the two panels in left-to-right or top-to-bottom
* order.<p>
After you get the two panels, you can set up the open/closed and initial state of the
splitter (the bar between the two panels). The splitter is "open" when it is extended
to a predefined location to the right/down and "closed" when it is brought back to a
predefined location to the left/up. The splitter is toggled between these points when
the user clicks the bar or when <b>doOpenClose()</b> is called on the splitter.<p>
The exact location of the open and closed locations is specified by a value that consists of
either PanelSplitter.BEFORE or PanelSplitter.AFTER, bitwised ORed with one of the values:
PanelSplitter.MIN_SIZE, PanelSplitter.PREFERRED_SIZE, PanelSplitter.HIDDEN.<p>
The BEFORE/AFTER value indicates that the limit depends on the size of the control before (left/above)
or after (right/below) the splitter. The other values indicate that either the minimum size or preferred size
of the before/after control should be used as the limit, or HIDDEN indicates that the control should be
completely hidden.<p>
The <b>initial state</b> of the splitter can be PanelSplitter.OPENED or PanelSplitter.CLOSED or 0(indicating
the splitter should be placed proportionally between the two controls).<p>
Use <b>setSplitter(int openState,int closeState,int initialState)</b> to set the open/closed/initial state
of the splitter. For example:<br>
<pre>
	sp.setSplitter(PanelSplitter.AFTER|PanelSplitter.HIDDEN,PanelSplitter.BEFORE|PanelSplitter.HIDDEN,0);
</pre>
This will set the splitter such that when it is open the control after the splitter is completely hidden,
and when it is closed the control before the splitter is completely hidden. The initial state will show
both controls.<p>
<pre>
	sp.setSplitter(PanelSplitter.BEFORE|PanelSplitter.PREFERRED_SIZE,PanelSplitter.BEFORE|PanelSplitter.MIN_SIZE,PanelSplitter.CLOSED);
</pre>
This will set the splitter such that when it is open the control before the splitter is shown at its
preferred size, and when it is closed the control before the splitter is shown at its minimum size.<p>
By default the open type of the splitter is AFTER|MIN_SIZE and the closed type is BEFORE|MIN_SIZE.<p>
These open and closed positions do not put a limit on the range of motion of the splitter when being
dragged by the user. The user can drag the splitter to the full on-screen limits.
**/
//##################################################################
public class SplittablePanel extends CellPanel{
//##################################################################

public static final int VERTICAL = PanelSplitter.VERTICAL;
public static final int HORIZONTAL = PanelSplitter.HORIZONTAL;
//public static final int CLOSED = 0x1;
/**
* This is an option for getNextPanel(). It indicates that the defaultAddToMeCellConstraints of the
* retrieved panel should be set to DONT_STRETCH.
**/
public static final int DONT_STRETCH_CONTENTS = 0x2;
//public static final int MAX_TO_PREFERRED_SIZE = 0x4;
//public static final int DONT_MAX = 0x8;
//public static final int MAX_TO_HIDE = 0x10;
//public int splitterThickness = 6;
/**
* The type of the PanelSplitter. Either HORIZONTAL or VERTICAL.
**/
public int type;
/**
* This is created when the SplittablePanel is created, and is then added as a child during
* the first call to getNextPanel().
**/
public PanelSplitter theSplitter;
/**
* Create an new SplittablePanel.
* @param type This can be VERTICAL or HORIZONTAL
*/
//==================================================================
public SplittablePanel(int type)
//==================================================================
{
	this.type = type;
	theSplitter = new PanelSplitter(type == HORIZONTAL ? VERTICAL : HORIZONTAL);
}
//protected CellPanel last = null;
//==================================================================
/**
* Return a new section in the SplittablePanel.
* @return A new CellPanel.
* @param options can be DONT_STRETCH_CONTENTS.
*/
//===================================================================
public CellPanel getNextPanel(int options)
//===================================================================
{
	CellPanel cp = getNextPanel();
	if ((options & DONT_STRETCH_CONTENTS)!= 0)
		if (type == VERTICAL) cp.defaultAddToMeCellConstraints = HSTRETCH;
		else cp.defaultAddToMeCellConstraints = VSTRETCH;
	return cp;
}
/**
* Create and return the next cell panel either before or after the splitter.
* This will throw a runtime exception if it is called more than twice.
* @return A new CellPanel.
*/
//===================================================================
public CellPanel getNextPanel()
//===================================================================
{
	CellPanel p = new CellPanel();
	if (theSplitter.before == null) {
		theSplitter.before = p;
		p.nextSplitter = theSplitter;
		addNext(p);
		if (type == VERTICAL) endRow();
		addNext(theSplitter).setCell(DONTSTRETCH);
		if (type == VERTICAL) endRow();
	}else if (theSplitter.after == null) {
		theSplitter.after = p;
		p.mySplitter = theSplitter;
		addNext(p);
		endRow();
	}else throw new RuntimeException("SplittablePanel only supports two panels.");
	return p;
/*
	CellPanel p = new CellPanel();
	PanelSplitter sp = null;
	if (last != null) {
		if (type == VERTICAL) {
			sp = new PanelSplitter(PanelSplitter.HORIZONTAL,last,p);
			addLast(sp).setCell(HSTRETCH);
		}else{
			sp = new PanelSplitter(PanelSplitter.VERTICAL,last,p);
			addNext(sp).setCell(VSTRETCH);
		}
		sp.thickness = splitterThickness;
		last.nextSplitter = sp;
	}
	if (type == VERTICAL) addLast(p);
	else addNext(p);
	p.mySplitter = sp;
	if ((options & DONT_STRETCH_CONTENTS)!= 0)
		if (type == VERTICAL) p.defaultAddToMeCellConstraints = HSTRETCH;
		else p.defaultAddToMeCellConstraints = VSTRETCH;
	if ((options & CLOSED) != 0) p.modify(AddToPanelClosed,0);
	if (((options & DONT_MAX) != 0) && (sp != null)) sp.doHide = sp.doMaximize = sp.doPreferredSize = false;
	if (((options & MAX_TO_PREFERRED_SIZE) != 0) && (sp != null)) sp.doHide = sp.doMaximize = !(sp.doPreferredSize = true);
	if (((options & MAX_TO_HIDE) != 0) && (sp != null)) sp.doMaximize = sp.doPreferredSize = !(sp.doHide = true);
	last = p;
	return p;
*/
}


/**
 * Define the "opened" and "closed" state of the splitter along with its initial state.
 * Call this after calling getNextPanel() twice - calling it before will throw a RuntimeException
 * @param openType This sould be (PanelSplitter.BEFORE or PanelSplitter.AFTER) OR'ed with one of
	(PanelSplitter.PREFERRED_SIZE or PanelSplitter.MINIMUM_SIZE or PanelSplitter.HIDDEN).
 * @param closeType This sould be (PanelSplitter.BEFORE or PanelSplitter.AFTER) OR'ed with one of
	(PanelSplitter.PREFERRED_SIZE or PanelSplitter.MINIMUM_SIZE or PanelSplitter.HIDDEN).
 * @param initialState This should be PanelSplitter.OPENED or PanelSplitter.CLOSED or 0.
 * @return The PanelSplitter for this SplittablePanel.
 */
//===================================================================
public PanelSplitter setSplitter(int openType,int closeType,int initialState)
//===================================================================
{
	if (theSplitter.before == null || theSplitter.after == null) throw new RuntimeException(
	"PanelSplitter.setSplitter() must be called after calling getNextPanel() twice.");
	theSplitter.setOpenCloseTypes(openType,closeType);
	if (initialState == theSplitter.OPENED){
		if ((theSplitter.openType & theSplitter.BEFORE) != 0){
			if ((theSplitter.openType & theSplitter.MIN_SIZE) != 0)
				theSplitter.before.setCell(INITIALLY_MINIMIZED);
			else if ((theSplitter.openType & theSplitter.HIDDEN) != 0)
				theSplitter.before.setCell(INITIALLY_CLOSED);
			else
				theSplitter.before.setCell(INITIALLY_PREFERRED_SIZE);
		}else{
			if ((theSplitter.openType & theSplitter.MIN_SIZE) != 0)
				theSplitter.after.setCell(INITIALLY_MINIMIZED);
			else if ((theSplitter.openType & theSplitter.HIDDEN) != 0)
				theSplitter.after.setCell(INITIALLY_CLOSED);
			else
				theSplitter.after.setCell(INITIALLY_PREFERRED_SIZE);
		}
	}else if (initialState == theSplitter.CLOSED){
		if ((theSplitter.closeType & theSplitter.BEFORE) != 0){
			if ((theSplitter.closeType & theSplitter.MIN_SIZE) != 0)
				theSplitter.before.setCell(INITIALLY_MINIMIZED);
			else if ((theSplitter.closeType & theSplitter.HIDDEN) != 0)
				theSplitter.before.setCell(INITIALLY_CLOSED);
			else
				theSplitter.before.setCell(INITIALLY_PREFERRED_SIZE);
		}else{
			if ((theSplitter.closeType & theSplitter.MIN_SIZE) != 0)
				theSplitter.after.setCell(INITIALLY_MINIMIZED);
			else if ((theSplitter.closeType & theSplitter.HIDDEN) != 0)
				theSplitter.after.setCell(INITIALLY_CLOSED);
			else
				theSplitter.after.setCell(INITIALLY_PREFERRED_SIZE);
		}
	}
	theSplitter.state = initialState;
	return theSplitter;
}
//##################################################################
}
//##################################################################

