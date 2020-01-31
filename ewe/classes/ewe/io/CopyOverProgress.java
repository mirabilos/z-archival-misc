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
package ewe.io;
import ewe.ui.*;
import ewe.sys.*;

//##################################################################
public class CopyOverProgress extends Editor{
//##################################################################

public ProgressBarForm overallProgress;
public ProgressBarForm fileProgress;
CopyOver co;

{
	windowFlagsToSet &= ~Window.FLAG_MAXIMIZE_ON_PDA;
}
//===================================================================
public CopyOverProgress(CopyOver co)
//===================================================================
{
	this.co = co;
	addLast(overallProgress = new ProgressBarForm());
	overallProgress.setPreferredSize(200,-1);
	addLast(fileProgress = new ProgressBarForm());
	overallProgress.exitOnCompletion = true;
	overallProgress.showSubTask = fileProgress.showSubTask = true;
	overallProgress.showMainTask = fileProgress.showMainTask = false;
	overallProgress.showStop = fileProgress.showStop = false;
	fileProgress.exitOnCompletion = false;
	title = "Copying Files";
	exitButtonDefined = true; //Tell it not to put an "X" button in the corner.
	addLast(addField(new mButton(new ewe.fx.IconAndText(stop,"Cancel",getFontMetrics())),"stopCopying")).setControl(DONTFILL);
}

//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor ed)
//===================================================================
{
	if (ft.fieldName.equals("stopCopying")){
		co.stopCopying();
	}
}
//##################################################################
}
//##################################################################

