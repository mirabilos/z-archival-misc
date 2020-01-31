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
import ewe.sys.*;

//##################################################################
public class ProgressBarForm extends Form implements CallBack, ProgressDisplay{
//##################################################################

public ProgressBar bar;
public ewe.sys.Handle handle;
public TimeOut t = TimeOut.Forever;
mLabel mainTask;
mLabel subTask;
mButton stop;
public boolean showMainTask = true;
public boolean showSubTask = false;
public boolean horizontalLayout = true;
public boolean showStop = false;
public boolean showBar = true;
public boolean showTaskInBar = false;
public boolean showTimeLeft = false;
/**
* Set this to false if you don't want the form to close its containing
* Frame upon completion.
**/
public boolean exitOnCompletion = true;

//===================================================================
public ProgressBarForm()
//===================================================================
{
	resizable = false;
	mainTask = new mLabel(1,30);
	subTask = new mLabel(1,30);
	bar = new ProgressBar();
	exitButtonDefined = true;
	windowFlagsToClear = Window.FLAG_HAS_CLOSE_BUTTON;
}

//===================================================================
public void make(boolean reMake)
//===================================================================
{
	if (made) return;
	if (showMainTask)
		if (showSubTask || !showTaskInBar)
			addNext(mainTask).setCell(HSTRETCH);
	endRow();
	if (showSubTask)
		if (!showTaskInBar)
			addNext(subTask).setCell(HSTRETCH);
	endRow();
	CellPanel cp = new CellPanel();
	if (showBar) cp.addNext(bar).setCell(HSTRETCH);
	if (!horizontalLayout) cp.endRow();
	if (showStop){
		cp.addNext((stop = new mButton("Cancel")).setHotKey(0,'c'),showBar ? DONTSTRETCH : HSTRETCH,DONTFILL|CENTER);
		Gui.iconize(stop,Form.stop,true,null);
		stop.modify(MouseSensitive|Disabled,0);
		if (handle != null) stop.modify(0,Disabled);
	}
	addLast(cp).setCell(HSTRETCH);
	super.make(reMake);
}
//===================================================================
public void updateTask()
//===================================================================
{
	//ewe.sys.Vm.debug(""+ewe.sys.Vm.getUsedMemory(false));
	bar.showTimeLeft = showTimeLeft;
	bar.set(handle.progress,handle.startTime);
	if (showSubTask) {
		if (!showTaskInBar) subTask.setText(handle.doing);
		if (showTaskInBar){
			if (ewe.util.mString.compare(bar.doing,handle.doing,false) != 0){
				bar.doing = handle.doing;
				bar.repaintNow();
			}
		}
	}
}
//===================================================================
public void clearTask()
//===================================================================
{
	bar.doing = null;
	bar.set(0,0);
	bar.repaintNow();
}
//-------------------------------------------------------------------
protected void doSetup(ewe.sys.Handle h,String processName,boolean execIt)
//-------------------------------------------------------------------
{
	if (processName == null) processName = h.doing;
	title = processName;
	if (execIt) getTopmostForm().exec();
	handle = h;
	h.start();
	if (stop != null) {
		stop.modify(0,Disabled);
		stop.repaintNow();
	}
	if (showMainTask) {
		mainTask.setText(processName);
		if (!showSubTask && showTaskInBar) {
			bar.doing = processName;
			bar.repaintNow();
		}
	}
}
//===================================================================
public void setTask(ewe.sys.Handle h,String processName)
//===================================================================
{
	doSetup(h,processName,false);
	h.callBackOnFlags(this,h.Changed,t.reset());
	updateTask();
}
//===================================================================
public int execute(ewe.sys.Handle h,String processName)
//===================================================================
{
	doSetup(h,processName,true);
	while(true){
		updateTask();
		if ((h.check() & h.Stopped) != 0) break;
		try{
			h.waitOn(h.Changed);
		}catch(Exception e){}
	}
	if (exitOnCompletion) getTopmostForm().exit(0);
	return h.check();
}

//===================================================================
public void callBack(Object obj)
//===================================================================
{
	if (obj instanceof HandleStatus){
		HandleStatus hs = (HandleStatus)obj;
		updateTask();
		if (hs.stopped){
			if (exitOnCompletion) getTopmostForm().exit(1);
		}else
			hs.handle.callBackOnFlags(this,Handle.Changed,t.reset());
	}
}

//===================================================================
public void onControlEvent(ControlEvent ev)
//===================================================================
{
	if (ev.type == ev.PRESSED && ev.target == stop){
		stopPressed();
	}
	super.onControlEvent(ev);
}
//===================================================================
public void stopPressed()
//===================================================================
{
	handle.stop(0);
}

private static ProgressBarForm displaying = null;
/**
 * This displays a modal progress message without a bar or cancel button. It is a good
 * way of disabling input to a Window while a process is running. To remove the box, simply
 * call clear() or you can call display() again without calling clear to display a different message.
 * @param title The title for the box - if it is null, then no title is displayed.
 * @param doing The message to display.
 * @param parent The parent frame. If it is null then a new window is used.
 * @return
 */
//===================================================================
public static void display(String title,String doing,Frame parent)
//===================================================================
{
	clear();
	displaying = new ProgressBarForm();
	displaying.moveable = false;
	displaying.showStop = false;
	displaying.showBar = false;
	displaying.mainTask = new mLabel(doing);
	if (title != null) displaying.title = title;
	else {
		displaying.hasTopBar = false;
		displaying.setBorder(EDGE_SUNKEN,2);
	}
	if (parent != null) displaying.exec(parent,Gui.CENTER_FRAME);
	else displaying.exec();
}
/**
 * Use this to clear a ProgressBarForm displayed using display().
 */
//===================================================================
public static void clear()
//===================================================================
{
	if (displaying != null) displaying.getTopmostForm().exit(0);
	displaying = null;
}
/**
* A quick way to execute a ProgressBarForm.
**/
//===================================================================
public static Handle execute(String doing,Handle h)
//===================================================================
{
	ProgressBarForm pbf = new ProgressBarForm();
	pbf.bar.setPreferredSize(300,-1);
	pbf.horizontalLayout = false;
	pbf.showStop =
	pbf.showSubTask =
	pbf.showTaskInBar =
	pbf.showTimeLeft = true;
	pbf.execute(h,doing);
	return h;
}
//##################################################################
}
//##################################################################

