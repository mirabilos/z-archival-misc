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
* A Mosaic is used to display multiple images, each of which may be able
* to move and or be animated. The Mosaic ensures that, should an image
* need to be refreshed due to being moved or changed, that an effecient

* screen refresh will be done.<p>
* Mosaic is also the base object for a MosaicPanel - which provides
* convenience methods to detect when the user presses/drags the pen/mouse
* on an image in the Mosaic.<p>
* MosaicPanel is also the base object for an InteractivePanel - which
* provides methods to move images when the user drags them with the pen/mouse
* and also to detect when one image is being dragged over another.<p>
**/
//##################################################################
public class Mosaic extends Canvas implements ImageRefresher{
//##################################################################
//==================================================================
{
	isFullScrollClient = true;
	origin = new Point(0,0);
}
/**
* Set this true IF, while one image is being dragged no other images
* will change. It makes the updating of moving images somewhat more
* effecient.
**/
public boolean quickDragging = false;
/**
* These are the images in the Mosaic. However don't add/remove from it
* directly. Use the addImage()/removeImage() methods instead.
**/
public ImageList images = new ImageList();
/**
* Set this to be a backgroundImage.
**/
public Image backgroundImage;
/**
* This sets the backgroundImage AND sets the virtualSize to be the same size as
* the image. It does NOT set the preferredSize of the mosaic. You still have to
* set that yourself.
**/
//===================================================================
public void setAndSizeToBackgroundImage(Image img)
//===================================================================
{
	backgroundImage = img;
	if (img != null)
		virtualSize = new Rect(0,0,img.getWidth(),img.getHeight());
}
private static boolean doReduceClip = true;
protected boolean pauseSnapShots = false;
//==================================================================
private ImageList inArea = new ImageList();
//==================================================================
/**
* Add an image to the mosaic.
**/
//===================================================================
public void addImage(AniImage im) {if (im == null) return; images.add(im); images.moveOnTop(im); im.mosaic = this; if (!pauseSnapShots) addToSnapShot(im);}
//===================================================================
/**
* Remove an image from the mosaic.
**/
//===================================================================
public void removeImage(AniImage im) {if (im == null) return; images.remove(im); im.mosaic = null; if (!pauseSnapShots) removeFromSnapShot(im);}
//===================================================================
//==================================================================
public void paintMosaicBackground(Graphics g)
//==================================================================
{
	g.setColor(getBackground());
	Dimension r = getMySize(null);
	g.fillRect(0,0,r.width,r.height);
}
private Image snapShot;
private AniImage movingImage;
//==================================================================
public Image confirmImage(Image what)
//==================================================================
{
	Dimension r = getMySize(null);
	int w = r.width, h = r.height;
	if (w < 1) w = 1;
	if (h < 1) h = 1;
	if (what == null) return new Image(w,h);
	if (what.getWidth() != w || what.getHeight() != h) {
		what.free();
		what = new Image(w,h);
	}
	return what;
}
protected boolean firstSnap = true;
/**
* This is used for advanced refreshing with quickDragging.
**/
//==================================================================
public void takeSnapShot()
//==================================================================
{
	pauseSnapShots = false;
	if (!quickDragging) return;
	snapShot = confirmImage(snapShot);
	//System.out.println(snapShot.getWidth()+","+snapShot.getHeight());
	Graphics g = new Graphics(snapShot);
	if (backgroundImage != null) g.drawImage(backgroundImage,0,0);
	else paintMosaicBackground(g);
	for (int i = 0; i<images.size(); i++){
		AniImage im = (AniImage)images.get(i);

		im.draw(g);
	}
	g.free();
}
/**
* This is used for advanced refreshing with quickDragging.
**/
//==================================================================
public void removeFromSnapShot(AniImage which)
//==================================================================
{
	if (which == null || !quickDragging || (snapShot == null)) return;
	Graphics g = Graphics.createNew(snapShot);
	if (backgroundImage != null) g.copyRect(backgroundImage,which.location.x,which.location.y,which.location.width,which.location.height,which.location.x,which.location.y);
	else {
		g.setClip(which.location.x,which.location.y,which.location.width,which.location.height);
		paintMosaicBackground(g);
	}
	g.free();
}
/**
* This is used for advanced refreshing with quickDragging.
**/
//==================================================================
public void addToSnapShot(AniImage which)
//==================================================================
{
	if (which == null || !quickDragging || (snapShot == null)) return;
	Graphics g = Graphics.createNew(snapShot);
	which.draw(g);
	g.free();
}
/**
* This is used with quickDragging. Call this method to set the image
* that is moving.
**/
//==================================================================
public void setMovingImage(AniImage which)
//==================================================================
{
	if (!quickDragging) return;
	if (snapShot == null) takeSnapShot();
	if ((which == null) && (movingImage != null))

		if (movingImage.mosaic != null)
			addToSnapShot(movingImage);
	movingImage = which;
	removeFromSnapShot(which);
}
/**
* This is used with quickDragging. This tells the mosaic that the previously
* moving image is no longer moving.
**/
//==================================================================
public void dropImage(AniImage which)
//==================================================================
{
	if (which == movingImage) setMovingImage(null);
	refresh(which,null);
}

ImageBuffer buffer = new ImageBuffer();
Dimension mysize = new Dimension();

/**
* Refresh the mosaic. If gr is null then a Graphics will be created.
* if area is null then the entire mosaic will be refreshed. Normally
* you will not call this method. Instead you will generally refresh
* just a single image.
**/
//==================================================================
public void refresh(Rect area,Graphics gr){refresh(area,gr,false);}
public void refresh(Rect area,Graphics gr,boolean doingOneImage)
//==================================================================
{
	if (gr == null)
		if (!Gui.requestPaint(this)) return;
	inArea.clear();
	images.findImages(area,inArea,doingOneImage);
	//if (doingOneImage) ewe.sys.Vm.debug("Refresh: "+area+" = "+inArea.size());
	getMySize(mysize);
	Graphics g = buffer.getBuffer(mysize.width,mysize.height,area,getBackground(),false);
	//g.setClip(area.x,area.y,area.width,area.height);
	if (quickDragging && snapShot != null) {
		g.copyRect(snapShot,area.x,area.y,area.width,area.height,area.x,area.y);
		if (movingImage != null) movingImage.draw(g);
	}else {
		if (backgroundImage != null) g.drawImage(backgroundImage,0,0);
		else paintMosaicBackground(g);
		//if (doingOneImage) ewe.sys.Vm.debug("---");
		for (int i = 0; i<inArea.size(); i++){
			mImage mi = (mImage)inArea.get(i);
			//if (doingOneImage) ewe.sys.Vm.debug(mi.location.toString());
			if (doingOneImage) mi.draw(g,mi.lastDrawn.x,mi.lastDrawn.y,0);
			else {
				mi.draw(g);
				mi.drawn(mi.location);
			}
		}
	}
	if (g == gr) {
		g.flush();
		return;
	}
	boolean freeIt = gr == null;
	if (gr == null) gr = getGraphics();
	if (gr == null) return;
	gr.translate(-origin.x,-origin.y);
		//gr.setClip(area.x,area.y,area.width,area.height);
	if (doReduceClip) reduceClip(gr,area);
			//if (hasBorder) doBorder(gr);//getControlGraphics());
	gr.drawImage(buffer.image,0,0);
	gr.translate(origin.x,origin.y);
			//if (hasBorder) doBorder(getControlGraphics());
	gr.flush();
	if (freeIt) gr.free();
}

Rect moved = new Rect();
/**
* This refreshes all images marked as changed. If gr is null, then a new
* graphics will be created and used.
**/
//===================================================================
public void refreshChanged(Graphics gr)
//===================================================================
{
	int num = 0;
	int sz = images.size();
	for (int i = 0; i<sz; i++){
		AniImage ai = (AniImage)images.get(i);
		if ((ai.properties & (ai.HasChanged|ai.HasMoved)) != 0){
			if (num == 0) moved.set(ai.getMovedBounds());
			else moved.unionWith(ai.getMovedBounds());
			ai.updated();
			ai.drawn(ai.location);
			num++;
		}
	}
	if (num == 0) return;
	refresh(moved,gr,true);
}
/**
* Refresh a particular image. If gr is null then a new Graphics will
* be created and used.
**/
//==================================================================
public void refresh(AniImage image,Graphics gr)
//==================================================================
{
	if (image == null) return;
	Rect r = image.getMovedBounds();
	image.drawn(image.location);
	refresh(r,gr,true);
	image.updated();
}
/**
* Paint the entire mosaic to the graphics. If gr is null then a new
* Graphics will be created and used.
**/
//==================================================================
public void refresh(Graphics gr)
//==================================================================
{
	int n = images.size();

	for (int i = 0; i<n; i++){
		AniImage ai = (AniImage)images.get(i);
		ai.drawn(null);
	}
	refresh(getVisibleArea(null),gr);
}
/**
* Repaint the entire mosaic.
**/
//==================================================================
public void refresh() {refresh(null);}
//==================================================================

private Rect full = new Rect(0,0,0,0);
/**
* The doPaint() method for the Control.
**/
//==================================================================
public void doPaint(Graphics g,Rect area)
//==================================================================
{

	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	//super.doPaint(g);
	Rect r = null;
	if (area != null){
		r = new Rect(area.x,area.y,area.width,area.height);
		r.x += origin.x; r.y += origin.y;
	}else
		r = getVisibleArea(null);
	//g.translate(-origin.x,-origin.y);

	BufferedGraphics bg = new BufferedGraphics(g,r);
	doBackground(bg.getGraphics());
	refresh(r,bg.getGraphics());
	bg.release();
	//g.translate(origin.x,origin.y);
	//ewe.sys.Vm.sleep(1000);
}
//===================================================================
public void onSetOrigin()
//===================================================================
{
	for (int i = 0; i<images.size(); i++){
		AniImage ai = (AniImage)images.get(i);
		if (((ai.properties & AniImage.RelativeToOrigin) != 0) && (ai.relativeToOrigin != null)){
			ai.location.x = origin.x+ai.relativeToOrigin.x;
			ai.location.y = origin.y+ai.relativeToOrigin.y;
		}
	}
	super.onSetOrigin();
}
/**
This method is used to refresh an Image that has been moved, but that you want
to make sure remains fully visible, by scrolling the Mosaic if necessary. Note
that this will only work correctly IF the <b>virtualSize</b> value has been set AND the image
remains in the range (0,0) to (virtualSize.x-imageWidth,virtualSize.y-imageHeight).
<p>
This method will also update the scroll bars if there are any.
 * @param movedImage The image that has been moved or changed in some way.
 * @return true if a scroll was done, false if no scrolling occured.
 */
//===================================================================
public boolean refreshOnScreen(AniImage movedImage)
//===================================================================
{
	int wa = movedImage.getWidth();
	int ha = movedImage.getHeight();
	int x = movedImage.location.x;
	int y = movedImage.location.y;
	boolean scroll = false;
	int ox = origin == null ? 0 : origin.x, oy = origin == null ? 0 : origin.y;
	if (x-ox+wa >= width){
		ox = x-width+wa;
		scroll = true;
	}
	if (y-oy+ha >= height){
		oy = y-height+ha;
		scroll = true;
	}
	if (x-ox < 0){
		ox = x;
		scroll = true;
	}
	if (y-oy < 0){
		oy = y;
		scroll = true;
	}
	if (scroll) {
		if (!setOrigin(ox,oy,null))
			refresh(movedImage,null);
		else{
			refresh();
			return true;
		}
	}else
		refresh(movedImage,null);
	return false;
}
//===================================================================
public void refresh(IImage image,int options)
//===================================================================
{
	if (!(image instanceof AniImage)) return;
	if ((options & KEEP_VISIBLE) != 0) refreshOnScreen((AniImage)image);
	else refresh((AniImage)image,null);
}

//##################################################################
}
//##################################################################


