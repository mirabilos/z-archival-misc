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

//###############################################################
public class DragContext {
//###############################################################
/**
* This is a possible modifier.
**/
static public final int NoDragOver = 0x80000000;

/**
* Used for Drag and Drop - this is any data to be dragged.
**/
public Object dataToDrag = null;
/**
* Used for Drag and Drop - this indicates that any data being dragged should be considered multiple data.
**/
public boolean isMultiple = false;
/**
* Used for Drag and Drop - this indicates that a copy of the data should be dragged.
**/
public boolean isCopy = false;
/**
* Used for Drag and Drop - these are options to display when the data is dropped. If this is null then no
menu will be displayed when the pen/mouse is released.
**/
public MenuItem [] dropOptions;
/**
* Used for Drag and Drop - if this is not 0 then it will be used as the special drag and drop mouse pointer.
**/
public int dragCursor;
/**
* You can use this for attaching custom data to your drag.
**/
public Object dragData;
/**
 * Sets up the drag and drop menu options.
 * @param alwaysShow true if you want the menu to show unconditionally. If it is false the menu
	will only show if the right mouse button is used to drag the data.
 * @param showIfNoMouse true if you want the menu to show if the system is pen based.
 */
//===================================================================
public void setDropOptions(MenuItem [] options,boolean alwaysShow,boolean showIfNoMouse)
//===================================================================
{
	boolean doShow = alwaysShow || ((modifiers & PenEvent.RIGHT_BUTTON) != 0);
	if (showIfNoMouse && ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_MOUSE_POINTER) != 0))
		doShow = true;
	if (doShow) dropOptions = options;
}
/**
 * Sets up the standard Copy, Move, Cancel DragAndDrop popup menu.
 * @param alwaysShow true if you want the menu to show unconditionally. If it is false the menu
	will only show if the right mouse button is used to drag the data.
 * @param showIfNoMouse true if you want the menu to show if the system is pen based.
 */
//===================================================================
public void setDropOptions(boolean alwaysShow,boolean showIfNoMouse)
//===================================================================
{
	setDropOptions(moveOrCopyOptions,alwaysShow,showIfNoMouse);
}

public int modifiers = 0;
public Point start = new Point(0,0), prevPoint = new Point(0,0), curPoint = new Point(0,0);
public Point point1, point2;
public boolean didDrag = false;
public int resolution = 3;
public int rate = 0;// Pause in millis between drags.
public int lastDragTime = 0;
public PenEvent penEvent;
public boolean cancelled = false;

public static MenuItem [] moveOrCopyOptions  = new MenuItem[]{new MenuItem("Move Here","move"),new MenuItem("Copy Here","copy")};

public String dropAction;

public DragContext(){}
public DragContext(PenEvent ev) {set(ev);}
public void set(PenEvent ev,Point where)
{
	start.move(where.x,where.y);
	prevPoint.move(where.x,where.y);
	curPoint.move(where.x,where.y);
	if (ev != null) modifiers = ev.modifiers;
	penEvent = ev;
	/*
	if ((ev.modifiers & Event.META_MASK) != 0) modifiers |= RightButton;
	if ((ev.modifiers & Event.CTRL_MASK) != 0) modifiers |= CtrlPressed;
	if ((ev.modifiers & Event.ALT_MASK) != 0) modifiers |= AltPressed;
	if ((ev.modifiers & Event.SHIFT_MASK) != 0) modifiers |= ShiftPressed;
	*/
}
public void set(PenEvent ev)
{
	set(ev,new Point(ev.x,ev.y));
}
public boolean hasDragged(Point newPoint)
{
	int d = newPoint.x-curPoint.x; if (d < 0) d = -d;
	if (d >= resolution) return true;
	d = newPoint.y-curPoint.y; if (d < 0) d = -d;
	return (d >= resolution);
}
/**
* Get the current ImageDragInWindow if there is one.
**/
//===================================================================
public ImageDragInWindow getImageDrag()
//===================================================================
{
	if (dragData instanceof ImageDragInWindow) return (ImageDragInWindow)dragData;
	return null;
}
//##################################################################
public static class ImageDragInWindow{
//##################################################################

int cx, cy, iw, ih, wx, wy;
Control relativeTo;
//IImage capturedWindow;
Window window;
Rect updateArea = new Rect();
ImageBuffer ib = new ImageBuffer();
Graphics wg;
Image copied;

/**
* If this is not null the image will be limited to being within this area.
**/
public Rect dragLimits;
/**
* This is the image being dragged.
**/
public IImage image;
/**
* By default this is true. It will cause a clearPendingDrags() to be called on the DragContext after each
* redrawing of the image on the window.
**/
public boolean clearPendingDrags = true;

/**
* This is the location of the last valid drag. It is null initially but wil be updated after the first
* dragImage() call to the DragContext. This is not the actual x,y position of the top left of the image, but the position of
* the last pen/mouse pointer.
**/
public Point imagePos = null;
/**
* This is the position of the top-left of the image, relative to the top left of the Control.
**/
public Point relativeImagePos = null;
//===================================================================
public ImageDragInWindow(IImage image,Point cursorOffsetInImage,Control relativeTo,Window window)//,IImage capturedWindow)
//===================================================================
{
	this.image = image;
	cx = cursorOffsetInImage.x;
	cy = cursorOffsetInImage.y;
	iw = image.getWidth();
	ih = image.getHeight();
	Point p = Gui.getPosInParent(relativeTo,window);
	wx = p.x; wy = p.y;
	this.relativeTo = relativeTo;
	//this.capturedWindow = capturedWindow;
	this.window = window;
	if (true){
		//ewe.sys.Vm.debug("Getting copy!");
		Dimension s = window.getSize(new Dimension());
		copied = new Image(s.width,s.height);
		Graphics g = new Graphics(copied);
		if (Graphics.canCopy)
			g.copyRect(window,0,0,s.width,s.height,0,0);
		else
			window.repaintNow(g,null);
		g.free();
	}else{

		//ewe.sys.Vm.debug("Nope, can't copy!");
	}
	wg = window.getGraphics();
}
//-------------------------------------------------------------------
private void getUpdateArea(Point prevPoint,Point curPoint,IImage newImage,Point newCursorOffset,Point fixedPoint)
//-------------------------------------------------------------------
{
	updateArea.set(prevPoint.x-cx,prevPoint.y-cy,iw,ih);
	fixedPoint.x = curPoint.x;
	fixedPoint.y = curPoint.y;
	if (newImage != null){
		image = newImage;
		cx = newCursorOffset.x;
		cy = newCursorOffset.y;
		iw = image.getWidth();
		ih = image.getHeight();
	}
	if (dragLimits != null){
		if (fixedPoint.x-cx+wx < dragLimits.x) fixedPoint.x = dragLimits.x+cx-wx;
		if (fixedPoint.y-cy+wy < dragLimits.y) fixedPoint.y = dragLimits.y+cy-wy;
		if (fixedPoint.x-cx+wx+iw > dragLimits.x+dragLimits.width) fixedPoint.x = dragLimits.x+cx-wx-iw+dragLimits.width;
		if (fixedPoint.y-cy+wy+ih > dragLimits.y+dragLimits.height) fixedPoint.y = dragLimits.y+cy-wy-ih+dragLimits.height;
	}
	updateArea.getAddition(Rect.buff.set(fixedPoint.x-cx,fixedPoint.y-cy,iw,ih),updateArea);
	updateArea.x += wx;
	updateArea.y += wy;
}
//-------------------------------------------------------------------
void updateArea(DragContext dc,boolean doImage,boolean update)
//-------------------------------------------------------------------
{
	if (imagePos == null) imagePos = new Point().set(dc.prevPoint.x,dc.prevPoint.y);
	if (update) getUpdateArea(imagePos,dc.curPoint,null,null,imagePos);
	Graphics g = ib.get(updateArea.width,updateArea.height,true);
	g.setColor(new Color(0xff,0,0));
	g.fillRect(0,0,updateArea.width,updateArea.height);
	g.translate(-updateArea.x,-updateArea.y);
	if (copied == null){
		window.repaintNow(g,updateArea);
		if (doImage) image.draw(g,imagePos.x-cx+wx,imagePos.y-cy+wy,0);

	}else{
		g.drawImage(copied,0,0);
		if (doImage) image.draw(g,imagePos.x-cx+wx,imagePos.y-cy+wy,0);
	}
	if (relativeImagePos == null) relativeImagePos = new Point();
	relativeImagePos.set(imagePos.x-cx,imagePos.y-cy);
	wg.drawImage(ib.image,updateArea.x,updateArea.y);
	wg.flush();
	g.translate(updateArea.x,updateArea.y);
	if (clearPendingDrags) dc.clearPendingDrags();
}
//===================================================================
void updateArea(DragContext dc,IImage newImage,Point newCursorOffset)
//===================================================================
{
	if (imagePos == null) imagePos = new Point().set(dc.prevPoint.x,dc.prevPoint.y);
	getUpdateArea(imagePos,dc.curPoint,newImage,newCursorOffset,imagePos);
	updateArea(dc,true,false);
}
//-------------------------------------------------------------------
void dispose()
//-------------------------------------------------------------------
{
	ib.free();
	wg.free();
	if (copied != null) copied.free();
}
//##################################################################
}
//##################################################################


/**
 * Start dragging an Image around the window.
	This should be called from the startDragging() method of the calling control.
 * @param drag The image to drag.
 * @param cursorInImage The location in pixels of the cursor point within the image.
 * @param c The control from which the image is being dragged.
 * @return An ImageDragInWindow context which the DragContext will be using for the drag.
 */
//===================================================================
public ImageDragInWindow startImageDrag(IImage drag,Point cursorInImage,Control c)
//===================================================================
{
	return startImageDrag(new ImageDragInWindow(drag,cursorInImage,c,c.getWindow()));
}
/**
 * Start dragging an Image around the window.
	This should be called from the startDragging() method of the calling control.
 * @param dw The ImageDragInWindow object used for dragging the image.
 * @return The dw parameter.
 */
//===================================================================
public ImageDragInWindow startImageDrag(ImageDragInWindow dw)
//===================================================================
{
	dragData = dw;
	return dw;
}

/**
* Call this within the dragged(DragContext dc) to continue dragging the same
* image.

**/
//===================================================================
public void imageDrag()
//===================================================================
{
	((ImageDragInWindow)dragData).updateArea(this,true,true);
}

/**
 * Call this within the dragged(DragContext dc) to drag a new Image. If newImage is null
 * or the same as the previous image this will have the same effect as imageDrag().
 * @param newImage The new image to drag.
 * @param cursorInImage The position of the mouse pointer within the new image.
 */
//===================================================================
public void imageDrag(IImage newImage,Point cursorInImage)
//===================================================================
{
	ImageDragInWindow dw = (ImageDragInWindow)dragData;
	if (newImage == dw.image || newImage == null)
		dw.updateArea(this,true,true);
	else
		dw.updateArea(this,newImage,cursorInImage);
}
/**
* Stop dragging the image. The area under the image is repainted and dragging
* resources are freed. The image will also be freed.
**/
//===================================================================
public void stopImageDrag()
//===================================================================
{
	stopImageDrag(true);
}
//===================================================================
public void stopImageDrag(boolean freeImage)
//===================================================================
{
	((ImageDragInWindow)dragData).updateArea(this,false,true);
	((ImageDragInWindow)dragData).dispose();
	if (freeImage) ((ImageDragInWindow)dragData).image.free();
}


/**
 * This clears all pending drag messages to this window. This prevents
 * the drag operation lagging behind the actual drag messages.
 */
//===================================================================
public void clearPendingDrags()
//===================================================================
{
	Window.clearQueuedMoves();
}
//###############################################################
}
//###############################################################

