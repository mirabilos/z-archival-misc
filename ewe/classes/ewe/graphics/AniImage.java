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
public class AniImage extends mImage{
//##################################################################
{
	properties |= IsAnimated;
}
//==================================================================
public AniImage(){super();}
public AniImage(String image,String mask) throws IllegalArgumentException
//==================================================================
{
	super(image,mask);
}
//===================================================================
public AniImage(String image) throws IllegalArgumentException
//===================================================================
{
	super(image);
}
//===================================================================
public AniImage(String image,Color transparent) throws IllegalArgumentException
//===================================================================
{
	super(image,transparent);
	//setImage(new Image(image));
	//transparentColor = transparent;
}
//===================================================================
public AniImage(Image image,int drawMode,boolean imageMayChange)
//===================================================================
{
	super(image,drawMode,imageMayChange);
}
//===================================================================
public AniImage(IImage image,int drawMode)
//===================================================================
{
	super(image,drawMode);
}
//===================================================================
public AniImage(IImage image)
//===================================================================
{
	super(image);
}
//===================================================================
public Object getNew() {return new AniImage();}
//===================================================================
public boolean
//============================================================
  canGo(Point where)
//============================================================
{
	return true;
}
//###############################################################
// Methods for hot images.
//###############################################################

/**
* The hotArea. If this is null then the entire image is hot.
*/
//public TwoDShape hotArea;
/**
* Gets the bounds of the hot area.
*/

//==================================================================
public Rect limits = null;
public Point relativeToOrigin = null;
/**
* Returns if this AniImage is touching the other AniImage.
*/
public boolean
//==============================================================
	isTouching(AniImage other)
//==============================================================
{
	if (other == null) return false;
	return getHotArea().intersects(other.getHotArea());
}
/**
* Returns if this AniImage is completely within the other AniImage.
*/
public final boolean
//==============================================================
	isCompletelyWithin(AniImage other)
//==============================================================
{
	if (other == null) return false;
	if (!other.onHotArea(location.x,location.y)) return false;
	return other.onHotArea(location.x+location.width-1,location.y+location.height-1);
}
/**
* Returns if this AniImage is "within" the other AniImage - where "within"
* is "touching" if CompletelyIn property is not set, otherwise it is "isCompletelyWithin".
*/
public final boolean
//==============================================================
	isWithin(AniImage other)
//==============================================================
{
	if (other == null) return false;
	if ((other.properties & CompletelyIn) != 0) return isCompletelyWithin(other);
	else return isTouching(other);
}

//===================================================================
public final boolean isIn(Point p)
//===================================================================
{
	if (p == null) return false;
	return getHotArea().isIn(p.x,p.y);
}
//===================================================================
public final boolean isDraggedOver(AniImage draggingOver,Point mouseLocation)
//===================================================================
{
	Point p = mouseLocation;
	if ((draggingOver.properties & MouseMustBeOver) != 0) return draggingOver.isIn(mouseLocation);
	return isWithin(draggingOver);
}
//###############################################################
// Methods for animation.
//###############################################################
/**
* Marks the image as having been changed in some way. This will cause the image
* to be refreshed when calling updateImage()/updateImages()
*/
public  void
//==============================================================
	changed() { properties |= HasChanged; }
//==============================================================
/**
* Marks the image as not having been changed.
*/
public  void
//==============================================================
	updated() { properties &= ~(HasChanged|HasMoved); hasBeen.set(location);}
//==============================================================
private Rect hasBeen = new Rect(0,0,0,0);
/**
* Moves the image. If the new location is different to the current
* location then the HasMoved bit in properties will be set. It will NOT clear
* this bit if the image actually has not moved. This bit must be cleared manually.
*/
public  void
//==============================================================
	move(int x,int y)
//==============================================================
{
	if ((x == location.x) && (y == location.y)) return;
	if ((properties & HasMoved) == 0) hasBeen.set(location);
	location.x = x; location.y = y;
	if (limits != null) {
		if (location.x+location.width > limits.x+limits.width) location.x = limits.x+limits.width-location.width;
		if (location.x < limits.x) location.x = limits.x;
		if (location.y+location.height > limits.y+limits.height) location.y = limits.y+limits.height-location.height;
		if (location.y < limits.y) location.y = limits.y;
	}
	hasBeen.getAddition(location,hasBeen);
	properties |= HasMoved;
}
/**
* Changes the image. If it has changed then HasChanged will be set in the
* properties. If it has not changed there is no effect on this bit.
*/
public  void
//==============================================================
	change(Image newImage)
//==============================================================
{
	if (image == newImage) return;
	setImage(newImage,drawMode);
	changed();
}
/**
* Changes the image. If it has changed then HasChanged will be set in the
* properties. If it has not changed there is no effect on this bit.
*/
public  void
//==============================================================
	change(Image newImage,Image newMask)
//==============================================================
{
	if (image == newImage && mask == newMask) return;
	setImage(newImage,newMask);
	changed();
}
public  void
//===================================================================
	change(IImage newImage)
//===================================================================
{
	setImage(newImage);
	changed();
}
/**
* Resize the image. Do not do this while the image is being displayed. It should
* be used before adding the image to the mosaic if for example the image is a buffer
* which is larger than actually needed.
*/
public void
//==============================================================
	resize(int width,int height)
//==============================================================
{
	location.width = width;
	location.height = height;
}
/**
* Translates the image. The variable previous will hold a list of the locations of
* the image before it was moved. If the new location is different to the current
* location then the HasMoved bit in properties will be set. It will NOT clear
* this bit if the image actually has not moved. This bit must be cleared manually.
*/
public void
//==============================================================
	translate(int dx,int dy)
//==============================================================
{
	move(location.x+dx,location.y+dy);
}
/**
* This resets the location of the image.
* The HasMoved bit in properties will be cleared.
*/
public void
//==============================================================
	setLocation(int x,int y)
//==============================================================
{
	location.x = x; location.y = y;
	if ((properties & RelativeToOrigin) != 0){
		relativeToOrigin = new Point(x,y);
		if (mosaic != null){
			location.x = mosaic.origin.x+x;
			location.y = mosaic.origin.y+y;
		}
	}
	hasBeen.set(location);
	properties &= ~HasMoved;
}
public Rect
//==================================================================
	getMovedBounds()
//==================================================================
{
	if ((properties & HasMoved) != 0) return hasBeen;
	return location;
}
public Mosaic mosaic;
public Control displayControl;
/** @deprecated use setRefresher() with a ewe.fx.ImageRefresher object.**/
public ImageRefresher refresher;

/** @deprecated use ewe.fx.ImageRefresher instead. **/
//##################################################################
public interface ImageRefresher {
//##################################################################

public void refresh(AniImage image);

//##################################################################
}
//##################################################################


/**
 * This returns true if there is some object that can refresh the image on screen.
 * It will check the mosaic, displayControl and refresher fields. If they are all null
 * it will return false.
 */
//===================================================================
public boolean hasARefresher()
//===================================================================
{
	if (mosaic != null || displayControl != null || refresher != null) return true;
	return getRefresher() != null;
}
//==================================================================
public void refresh()
//==================================================================
{
	if (mosaic != null) {
		if ((properties & KeepOnScreen) != 0)
			mosaic.refreshOnScreen(this);
		else
			mosaic.refresh(this,null);
	}
	else if (displayControl != null) {
		displayControl.repaintNow();
	}else{
		ewe.fx.ImageRefresher rf = getRefresher();
		if (rf != null) refresh(rf);
		else if (refresher != null)
			refresher.refresh(this);
	}
}
/**
* This is called if the AniImage is displayed within an ImageControl and that
* ImageControl has now been shown.
**/
//===================================================================
public void shown()
//===================================================================
{

}
/**
* This is called if the AniImage is displayed within an ImageControl and the Form that contained that
* ImageControl is now closing.
**/
//===================================================================
public void closing()
//===================================================================
{

}

/**
 * Refresh the AniImage.
 * @return true if there was a refresher to refresh the image, false if there was no refresher
 */
//===================================================================
public boolean refreshNow()
//===================================================================
{
	if (mosaic != null) {
		if ((properties & KeepOnScreen) != 0)
			mosaic.refreshOnScreen(this);
		else
			mosaic.refresh(this,null);
	}else if (displayControl != null) {
		displayControl.repaintNow();
	}else{
		ewe.fx.ImageRefresher rf = getRefresher();
		if (rf != null) refresh(rf);
		else if (refresher != null)
			refresher.refresh(this);
		else
			return false;
	}
	return true;
}
/**
Move this AniImage to a new location, moving it on screen within a certain time period.
This returns an ImageMover which is a Task object (an asynchronous task). You must call
startTask() on it to start the move and you can call waitUntilStopped() on the returned
Handle to wait until
the move is complete. You can also call stop() on the Handle to abort the move and there are other
options you can change as well.

@param destinationX The destination X position.
@param destinationY The destination Y position.
@param timeToMoveInMillis How long to take to move it in milliseconds.
@return An ImageMover object which you must call start() on
*/
//===================================================================
public ImageMover animateMoveTo(int destinationX, int destinationY,int timeToMoveInMillis)
//===================================================================
{
	return new ImageMover(this,new Point(location.x,location.y),new Point(destinationX, destinationY),timeToMoveInMillis);
}
//##################################################################
}
//##################################################################



