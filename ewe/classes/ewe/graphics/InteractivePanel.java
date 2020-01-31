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
package ewe.graphics;
import ewe.ui.*;
import ewe.fx.*;

//##################################################################
public class InteractivePanel extends MosaicPanel{
//##################################################################

/**
* Add images to this that will be considered to be touchable.
**/
public ImageList touching = null;

public boolean dragBackground = true;
{
	//preferredSize.width = preferredSize.height = 100;
}

/**
* If this is true then moveable images will be moved to the top
* when pressed or dragged.
**/
public boolean autoMoveToTop = true;

//-------------------------------------------------------------------
protected void checkTouching(ImageDragContext dc,boolean dropped)
//-------------------------------------------------------------------
{
	if (dc.image == null) return;
	ReactiveImage ri = null;
	if (dc.image instanceof ReactiveImage) ri = (ReactiveImage)dc.image;
	if (touching == null || ((dc.modifiers & dc.NoDragOver) != 0)) return;
//............................................................
	for(int i = 0; i<touching.size();i++) {
		AniImage im = (AniImage)touching.get(i);
		if (im == dc.image) continue;
		if (dc.image.isDraggedOver(im,dc.curPoint)) {
			if (!dropped) {
				if (dc.draggingOver == im) return;
				if (dc.draggingOver != null) draggedOff(dc);
				dc.draggingOver = im;
				draggedOver(dc);
				if (ri != null) ri.dragEvent(this,ri.DragOver,dc);
			}else {
				if ((dc.draggingOver != im) && (dc.draggingOver != null)) draggedOff(dc);
				dc.draggingOver = im;
				droppedOn(dc);
				if (ri != null) ri.dragEvent(this,ri.DropOn,dc);
			}
			return;
		}
	}
//............................................................
// Not over anything.
//............................................................
	if (dc.draggingOver != null) {
		draggedOff(dc);
		if (ri != null) ri.dragEvent(this,ri.DragOff,dc);
	}
	dc.draggingOver = null;
}
protected boolean amDragging = false;
//============================================================
public boolean imagePressed(AniImage which,Point pos)
//============================================================
{
	if (which != null && autoMoveToTop)
		if (images.moveOnTop(which)) which.refresh();
	amDragging = false;
	return true;
}
//============================================================
public boolean imageBeginDragged(AniImage which,Point pos)
//============================================================
{
//...............................................................
	if (which == null){
		if (!dragBackground) return false;
		ImageDragContext dc = dragging((AniImage)null);
		return true;
	}
//...............................................................
	if ((which.properties & AniImage.IsMoveable) == 0) return false;
//	which.properties |= which.HasBorder;
	ImageDragContext dc = new ImageDragContext(which,null,pos);
	dc.start.translate(-which.location.x,-which.location.y);
	//dc.mousePos = new Point(pos.x,pos.y);
	dragging(dc);
	draggingStarted(dc);
	ReactiveImage ri = null;
	if (dc.image instanceof ReactiveImage) ri = (ReactiveImage)dc.image;
	if (ri != null) ri.dragEvent(this,ri.DragStarted,dc);
	amDragging = true;
	checkTouching(dc,false);
	refresh(dc.image,null);//updateImage(dc.image);
	return true;
}

//============================================================
public boolean imageDragged(ImageDragContext dc,Point where)
//============================================================
{
	ReactiveImage ri = null;
	if (dc.image instanceof ReactiveImage) ri = (ReactiveImage)dc.image;
	dc.curPoint = new Point(where.x,where.y);
	AniImage moving = dc.image;
	Rect r = getDim(null);
	boolean didAutoScroll = false;
	if (moving == null) {
		if (!dragBackground) return true;
		int dx = dc.start.x-where.x, dy = dc.start.y-where.y;
		if (where.x < origin.x || where.x >= origin.x+r.width || where.y < origin.y || where.y >= origin.y+r.height && autoScrolling){
			if (where.x <= origin.x) dx = scrollStep;
			if (where.x >= origin.x+r.width) dx = -scrollStep;
			if (where.y <= origin.y) dy = scrollStep;
			if (where.y >= origin.y+r.height) dy = -scrollStep;
			dc.start.x = where.x; dc.start.y = where.y;
		}
		//dc.start.move(where.x,where.y);
		if (dx != 0 || dy != 0) scroll(dx,dy);
		refresh();
		return true;
	}else if (where.x < origin.x || where.x >= origin.x+r.width || where.y < origin.y || where.y >= origin.y+r.height){
 			if (autoScrolling) {
				didAutoScroll = true;
				xScroll = yScroll = 0;
				if (where.x <= origin.x) xScroll = -scrollStep;
				if (where.x >= origin.x+r.width) xScroll = scrollStep;
				if (where.y <= origin.y) yScroll = -scrollStep;
				if (where.y >= origin.y+r.height) yScroll = scrollStep;
				scroll(xScroll,yScroll);
				if (dragged.image != null){
			}
		}
	}
	Point to = new Point(where.x-dc.start.x,where.y-dc.start.y);
	if (moving.canGo(to)) {
		moving.move(to.x,to.y);
		draggingImage(dc);
		if (ri != null) ri.dragEvent(this,ri.Drag,dc);
	}
	checkTouching(dc,false);
	if (didAutoScroll) refresh();
	else refresh(dc.image,null);//updateImage(dc.image);
	return(true);
}
//============================================================
public boolean imageNotDragged(ImageDragContext dc,Point pos)
//============================================================
{
	//System.out.println("Image Not Dragged!");
	ReactiveImage ri = null;
	if (dc.image instanceof ReactiveImage) ri = (ReactiveImage)dc.image;
	dc.curPoint = new Point(pos.x,pos.y);
	if (dc.image == null) return true;
	checkTouching(dc,true);
	draggingStopped(dc);
	if (ri != null) ri.dragEvent(this,ri.DragStopped,dc);
	refresh(dc.image,null);//updateImage(dc.image);
	return true;
}
//============================================================
public boolean imageReleased(AniImage which,Point pos)
//============================================================
{
	ReactiveImage ri = null;
	if (which instanceof ReactiveImage) ri = (ReactiveImage)which;
	if (!amDragging) {
		imageClicked(which,pos);
		if (ri != null) ri.clickEvent(this,ri.Clicked);
	}
	refresh(which,null);//updateImage(which);
	return true;
}
//-----------------------------------------------------------
public void draggingStopped(ImageDragContext dc){}
public void draggingStarted(ImageDragContext dc){}
public void draggingImage(ImageDragContext dc){}
public void draggedOver(ImageDragContext dc){}
public void draggedOff(ImageDragContext dc){}
public void droppedOn(ImageDragContext dc){}
public void imageClicked(AniImage which,Point pos){}
//-----------------------------------------------------------
public boolean imageMovedOn(AniImage which){
	super.imageMovedOn(which);
	return true;
}
public boolean imageMovedOff(AniImage which){
	super.imageMovedOff(which);
	return true;
}

public void dragCopyOfImage(ImageDragContext dc)
{
	dc.image = (AniImage)dc.image.getCopy();
	dragging(dc);
}


//##################################################################
}
//##################################################################

