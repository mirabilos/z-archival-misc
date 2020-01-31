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

//##################################################################
public class DragPanel extends CellPanel{
//##################################################################

{
	modify(WantDrag,0);
	//backGround = Color.Black;
}

public boolean canDrag = true;
Image dragImage;
Point last;
Point start;
//==================================================================
public boolean isTopBar = true;
//==================================================================

//==================================================================
public void calculateSizes()
//==================================================================
{
	super.calculateSizes();
	if (preferredWidth < 4) preferredWidth = 6;
	if (preferredHeight < 4) preferredHeight = 6;
}
//==================================================================
public void penPressed(Point where)
//==================================================================
{
	super.penPressed(where);
}
//==================================================================
public void startDragging(DragContext dc)
//==================================================================
{
	if (!canDrag) return;
	if (Gui.isWindowFrame(getFrame())){
		getWindow().doSpecialOp(isTopBar ? Window.SPECIAL_MOUSE_MOVE:Window.SPECIAL_MOUSE_RESIZE,null);
		return;
	}
	if (isTopBar) {
		Rect r = getDim(null);
		dragImage = new Image(r.width,r.height);
		Graphics g = new Graphics(dragImage);
		repaintNow(g,r);
		g.draw3DRect(r,EDGE_BUMP,false,null,null);
		g.free();
		dc.startImageDrag(dragImage,new Point().set(dc.curPoint),this);
	}else {
		mImage mi = new mImage("ewe/ResizeFrame.bmp",Color.White);
		dc.startImageDrag(mi,new Point(5,5),this);
	}
}
//==================================================================
public void dragged(DragContext dc)
//==================================================================
{
	if (!canDrag||Gui.isWindowFrame(getFrame())) return;
	dc.imageDrag();
}
//==================================================================
public void stopDragging(DragContext dc)
//==================================================================
{
	if (!canDrag||Gui.isWindowFrame(getFrame())) return;
	dc.stopImageDrag();
	Frame f = getFrame();
	if (f == null) return;
	Rect r = f.getRect();
	if (isTopBar) {
		int dx = dc.getImageDrag().relativeImagePos.x;
		int dy = dc.getImageDrag().relativeImagePos.y;
		Gui.moveFrameTo(f,new Rect(r.x+dx,r.y+dy,r.width,r.height));
	}else{
		Rect ar = Gui.getAppRect(this);
		int tx = ar.x+dc.curPoint.x+5;
		int ty = ar.y+dc.curPoint.y+5;
		ar = Gui.getAppRect(f);
		if (tx <= ar.x || ty <= ar.y) {
			Sound.beep();
			return;
		}
		f.setRect(r.x,r.y,tx-ar.x,ty-ar.y);
		Gui.moveFrameTo(f,new Rect(r.x,r.y,tx-ar.x,ty-ar.y));
	}
}
//##################################################################
}
//##################################################################


