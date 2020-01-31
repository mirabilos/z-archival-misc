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
/**
* A MosaicPanel is an extension of a Mosaic which process pen/mouse
* events to trap conditions when the pen/mouse is pressed or dragged
* on images in the Mosaic.
**/

//##################################################################
public class MosaicPanel extends Mosaic {
//##################################################################
//==================================================================
{
	modify(WantDrag|WantHoldDown,0);
	dragResolution = 1;
}
//==================================================================
public MosaicPanel() {PenEvent.wantPenMoved(this,PenEvent.WANT_PEN_MOVED_ONOFF|PenEvent.WANT_PEN_MOVED_INSIDE,true);}
//==================================================================

public int hotCursor = ewe.sys.Vm.HAND_CURSOR;

//==============================================================
protected boolean isActive = true;
protected AniImage pressedImage = null, restedImage = null, onImage = null;
protected ImageDragContext dragged;
private Point lastDrag = new Point(0,0);
protected boolean isDragging = false, isScrolling = false, needDrag = false;
protected boolean canRest = true, resting = false, inPanel = false, isOn = false;
protected int restCount = 0;
protected int xScroll, yScroll;
public int scrollStep = 10;
public boolean autoScrolling = true;
public void setPressedImage(AniImage which)
{
	pressedImage = which;
}
/**
* This should be called by mousePressed() or mouseDragged() to indicate
* to the panel that an image is being dragged.
*/
protected final  void
//==============================================================
	dragging(ImageDragContext dc)
//==============================================================
{
	if (dc.image != null) {
		pressedImage = dc.image;
		if (images.find(dc.image) == -1) {
			images.add(dc.image);
			images.moveOnTop(dc.image);
		}
		if (quickDragging) setMovingImage(dc.image);
	}
//..........................
	dragged = dc;
	isDragging = true;
}
/**
* This should be called by mousePressed() or mouseDragged() to indicate
* to the panel that an image is being dragged.
*/
protected final  ImageDragContext
//==============================================================
	dragging(AniImage which)
//==============================================================
{
	if (dragged != null) {
		dragged.image = which;
		dragging(dragged);
		return dragged;
	}
	return null;
}
/**
* Use this to set the number of milliseconds in between drag messages.
*/
public int minDragRate = 50, dragRate;
public  void
//==============================================================
	setDragRate(int howLong)
//==============================================================
{
	if (howLong < minDragRate) howLong = minDragRate;
	dragRate = howLong;
}

private AniImage
//--------------------------------------------------------------
	which(int x,int y)
//--------------------------------------------------------------
{
	return images.findHotImage(new Point(x,y));
}
private  void
//--------------------------------------------------------------
	noRest()
//--------------------------------------------------------------
{
	if (resting == true) {
		imageNotRestingOn(onImage);
		resting = false;
	}
	restCount = 0;
}
private  void
//--------------------------------------------------------------
	noOn()
//--------------------------------------------------------------
{
	movingOnTo = null;
	if (isOn == true) {
		imageMovedOff(onImage);
		isOn = false;
	}
}

/**
* This variable is valid during an imageMovedOff() call. It indicates the image
* that the pointer is moving on to. This may be null.
**/
protected AniImage movingOnTo;
/**
* This variable is valid during an imageMovedOn() call. It indicates the image
* that the pointer moved off of before moving on to this. This may be null.
**/
protected AniImage movedOffOf;

private  void
//--------------------------------------------------------------
	checkOn(int x,int y)
//--------------------------------------------------------------
{
	if (!isActive) return;
	AniImage on = which(x,y);
	movingOnTo = on;
	movedOffOf = onImage;
//.......................................................
	if (on == null) {
		if (isOn) imageMovedOff(onImage);
		onImage = null;
		isOn = false;
	}else{
		if (isOn) {
			if (on != onImage) {
				imageMovedOff(onImage);
				onImage = on;
				imageMovedOn(onImage);
			}
		}else{
			onImage = on;
			imageMovedOn(onImage);
			isOn = true;
		}
	}
}
/**
* This deactivates detecting mouse interaction with the images on the
* panel. It still allows mouseEntered(), mouseExited() etc. If the mouse
* is on any image it will call mouseMovedOff() etc.
*/
public final void
//================================================================
	deactivate()
//================================================================
{
	isActive = false;
	noRest();
	noOn();
}
/**
* Allows mouse interaction with images on the
*/
public final void
//================================================================
	activate() {isActive = true;}
//================================================================

public final boolean
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	doMouseExit(PenEvent ev,int x,int y)
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
{
	x += origin.x; y += origin.y;
	noRest();
	noOn();
	inPanel = false;
	if (autoScrolling && isDragging) {
		if (dragged.image != null) {
			xScroll = yScroll = 0;
			if (x <= origin.x) xScroll = -scrollStep;
			if (x >= origin.x+width) xScroll = scrollStep;
			if (y <= origin.y) yScroll = -scrollStep;
			if (y >= origin.y+height) yScroll = scrollStep;
			isScrolling = true;
		}
	}
	return(mouseExited(ev,new Point(x,y)));
}
public final boolean
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	doMouseEnter(PenEvent ev,int x,int y)
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
{
	x += origin.x; y += origin.y;
	noRest();
	checkOn(x,y);
	inPanel = true;
	isScrolling = false;
	return(mouseEntered(ev,new Point(x,y)));
}
public final boolean
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	doMouseMove(PenEvent ev,int x,int y)
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
{
	x += origin.x; y += origin.y;
	noRest();
	checkOn(x,y);
	inPanel = true;
	return(mouseMoved(ev,new Point(x,y)));
}

private boolean justPressed = false, actuallyMoved = false;
private Point pressedPoint = null;
protected Point lastPoint = new Point();

//===================================================================
public void onPenEvent(PenEvent ev)
//===================================================================
{
	if (ev.type == PenEvent.PEN_MOVED_ON){
		doMouseEnter(ev,ev.x,ev.y);
	}else if (ev.type == PenEvent.PEN_MOVED_OFF){
		doMouseExit(ev,ev.x,ev.y);
	}else if (ev.type == PenEvent.PEN_MOVE){
		doMouseMove(ev,ev.x,ev.y);
	}else super.onPenEvent(ev);
}
public final void//boolean
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	penPressed(Point ev)
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
{
//....................................................................
// This is necessary because JDK1.1 generates mouseDown sporadically.
//....................................................................
	int x = ev.x, y = ev.y;
	int flags = getModifiers(true);
	if (isDragging || !(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) return;// true;
	x += origin.x; y += origin.y;
	pressedPoint = new Point(x,y);
	lastPoint.move(x,y);
	if (!isActive) return;// true;
	inPanel = true;
	noRest();
	canRest = false;
	checkOn(x,y);
	AniImage pressed = which(x,y);
	actuallyMoved = false;
	justPressed = true;
//	if (pressed != null){
		pressedImage = pressed;
		isDragging = false;
		dragged = new ImageDragContext((AniImage)null);
		dragged.set(null,new Point(x,y));
//	} //else return false;
	//return(
	imagePressed(pressed,new Point(x,y));
}
public final void// boolean
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	dragged(DragContext mdc)//oMouseDrag(PenEvent ev)
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
{
	//int x = ev.x, y = ev.y;
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) return;
	int x = mdc.curPoint.x, y = mdc.curPoint.y;
	x += origin.x; y += origin.y;
	//int dx = lastPoint.x-x, dy = lastPoint.y-y;
	//if (dx < dragResolution && dx > -dragResolution && dy < dragResolution && dy > -dragResolution) return;// true;
	//lastPoint.move(x,y);
	if (pressedPoint != null) {
		if (pressedPoint.x != x || pressedPoint.y != y) actuallyMoved = true;
		if (!actuallyMoved && justPressed) return;// true;
	}
	inPanel = true;
	noRest();
	if (!isActive) return;// true;
	canRest = false;
		if (justPressed && !isDragging) {
			boolean wantDragging = imageBeginDragged(pressedImage,new Point(pressedPoint.x,pressedPoint.y));
			if (wantDragging && !isDragging) dragging(pressedImage);
			isDragging = wantDragging;
		}
		justPressed = false;
		if (!isDragging) return;// true;
		lastDrag.move(x,y);
		//if (dragTicker != 0) needDrag = true;
		//else {
			//System.out.println("Direct dragging!");
		//return
		imageDragged(dragged,new Point(x,y));
		return;
		//}
	//return true;
}
public final void //boolean
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	penReleased(Point ev)
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
{
	int flags = getModifiers(true);
	if (!(((flags & (NotEditable|DisplayOnly)) == 0) || ((flags & NotAnEditor) != 0))) return;
	int x = ev.x, y = ev.y;
	x += origin.x; y += origin.y;
	inPanel = true;
	canRest = true;
	justPressed = false;
	if (!isActive) return;// true;
	noRest();
//..............................................................
		isScrolling = false;
		needDrag = false;
		if (isDragging) {
			isDragging = false;
			imageNotDragged(dragged,new Point(x,y));
			setMovingImage(null);
		}
//..............................................................
	checkOn(x,y);
	//if (pressedImage == null) return(false);
	AniImage pressed = images.findHotImage(new Point(x,y));
	if (pressed == pressedImage) {
		pressedImage = null;
		//return
		imageReleased(pressed,new Point(x,y));
		return;
	}
	//return(false);
}
/**
* Indicates the mouse has entered the panel.
*/
public boolean mouseEntered(PenEvent ev,Point pos) {return false;}
/**
* Indicates the mouse has left the panel.
*/
public boolean mouseExited(PenEvent ev,Point pos) {return false;}
/**
* Indicates the mouse has left the panel.
*/
public boolean mouseMoved(PenEvent ev,Point pos) {return false;}
/**
* Indicates the pen/mouse is pressed on an image.
*/
public boolean imagePressed(AniImage which,Point pos) {return false;}
/**
* Indicates the pen/mouse is released from an image.
*/
public boolean imageReleased(AniImage which,Point pos) {return false;}
/**
* Indicates the pen/mouse is attempting to drag an image. If you wish to drag the
* image return true. You can call the dragging() method to change the default
* ImageDragContext.
*/
public boolean imageBeginDragged(AniImage which,Point pos) {return false;}
/**
* Indicates the pen/mouse is dragging an image. Will only be called if imageBeingDragged
* returned true.
*/
public boolean imageDragged(ImageDragContext drag,Point pos) {return false;}
/**
* Indicates the pen/mouse has released dragging an image.
*/
public boolean imageNotDragged(ImageDragContext drag,Point pos) {return false;}
/**
* Indicates the pen/mouse is over an image without the pen/mouse being down (yet).
*/
//===================================================================
public boolean imageMovedOn(AniImage image)
//===================================================================
{
	if (hotCursor == 0) return false;
	setCursor(ewe.sys.Vm.HAND_CURSOR);
	return false;
}
/**
* Indicates the pen/mouse is no longer over an image.
*/
//===================================================================
public boolean imageMovedOff(AniImage image)
//===================================================================
{
	if (hotCursor == 0) return false;
	if (movingOnTo == null) setCursor(0);
	return false;
}
/**
* Indicates the pen/mouse is resting on an image (after half second delay).
*/
public boolean imageRestingOn(AniImage which) {return false;}
/**
* Indicates the pen/mouse is no longer resting on an image.
*/
public boolean imageNotRestingOn(AniImage which) {return false;}
//##################################################################
}
//##################################################################
/**
*
**/

