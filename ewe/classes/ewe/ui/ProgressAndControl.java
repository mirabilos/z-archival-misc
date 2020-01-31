/*
Copyright (c) 2001 Michael L Brereton  All rights reserved.

This software is furnished under the Gnu General Public License, Version 2, June 1991,
and may be used only in accordance with the terms of that license. This source code
must be distributed with a copy of this license. This software and documentation,
and its copyrights are owned by Michael L Brereton and are protected by copyright law.

If this notice is followed by a Wabasoft Copyright notice, then this software
is a modified version of the original as provided by Wabasoft. Wabasoft also
retains all rights as stipulated in the Gnu General Public License. These modifications
were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May
2001.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.
*/

package ewe.ui;
import ewe.sys.Handle;
/**
* A ProgressAndControl panel allows a ProgressBar to be displayed in the same
* location as another Control. When a task starts, the ProgressBar can be displayed
* and the other Control hidden. When the task ends, the other Control will be
* redisplayed.
**/
//##################################################################
public class ProgressAndControl extends CardPanel{
//##################################################################
/**
* This is the ProgressBarForm. You can alter its parameters as you wish.
**/
public ProgressBarForm pbf;
/**
* Add your controls to this panel.
**/
public CellPanel controls;

//===================================================================
public ProgressAndControl(ProgressBarForm form)
//===================================================================
{
	pbf = form;
	addItem(controls = new CellPanel(),"controls",null);
	addItem(pbf,"progress",null);
}
//===================================================================
public ProgressAndControl()
//===================================================================
{
	pbf = new ProgressBarForm();
	pbf.setBorder(EDGE_ETCHED,2);
	pbf.showMainTask = true; pbf.showSubTask = false;
	pbf.showTaskInBar = true; pbf.showStop = true;
	pbf.horizontalLayout = true;
	pbf.exitOnCompletion = false;
	addItem(controls = new CellPanel(),"controls",null);
	addItem(pbf,"progress",null);
}
/**
* Call this when the task begins.
**/
//===================================================================
public Handle startTask(Handle h,String task)
//===================================================================
{
	pbf.setTask(h,task);
	select("progress");
	return h;
}
/**
* Call this when you consider the task complete and want to show
* the controls again.
**/
//===================================================================
public void endTask()
//===================================================================
{
	select("controls");
}
//##################################################################
}
//##################################################################

