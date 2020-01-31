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
import ewe.util.ByteArray;

//##################################################################
public class ImageControl extends Canvas{
//##################################################################

IImage image;
{
	isFullScrollClient = false;
	//modify(PreferredSizeOnly,0);
}

/**
* If an image is smaller than the control, this option will expand it.
**/
public static final int STRETCH = 0x1;
/**
* If an image is larger than the control, this option will shrink it.
**/
public static final int SHRINK = 0x2;
/**
* If an image is being stretched or shrunk, this option tells it not
* to change the aspect ration.
**/
public static final int FIT = 0x4;
/**
* When the setData() method is called with the encoded image bytes, this option tells
* the ImageControl to limit the decoded size if possible.
**/
public static final int LIMIT_DECODED_SIZE = 0x8;
/**
* This options tells the ImageControl to scale the image using a rough scaling instead
* of smooth scaling. Rough scaling is faster than smooth scaling.
**/
public static final int ROUGH_SCALING = 0x10;

/**
By default this is STRETCH|SHRINK|FIT - telling the control to stretch or shrink the image
into the display, but preserve the aspect ration.
**/
public int options = STRETCH|SHRINK|FIT;

/**
* If this is true then the control will have a background equal to the
* image background IF the image has a background color associated with
* it (e.g. PNG images allow a background color to be specified).
**/
public boolean showImageBackground = true;
//===================================================================
public ImageControl(IImage what){setImage(what);}
//===================================================================
public IImage getImage() {return image;}
//===================================================================
public void setImage(IImage im)
//===================================================================
{
	if ((image instanceof ewe.graphics.AniImage) && (im != image))
		((ewe.graphics.AniImage)image).closing();
	if (im instanceof Image) {
		image = new mImage(im);
	}else
		image = im;
	if (im instanceof ewe.graphics.AniImage){
		((ewe.graphics.AniImage)im).displayControl = this;
		((ewe.graphics.AniImage)im).shown();
	}else if (im instanceof ewe.fx.OnScreenImage){
		((OnScreenImage)im).setRefresher(this);
	}
	repaintNow();
}

//===================================================================
public void shown()
//===================================================================
{
	super.shown();
	if (image instanceof ewe.graphics.AniImage)
		((ewe.graphics.AniImage)image).shown();
}
//-------------------------------------------------------------------
protected void formClosing()
//-------------------------------------------------------------------
{
	super.formClosing();
	if (image instanceof ewe.graphics.AniImage)
		((ewe.graphics.AniImage)image).closing();
}
//===================================================================
public void setData(Object data)
//===================================================================
{
	if (data instanceof IImage) {
		setImage((IImage)data);
	}else if (data instanceof byte [] || data instanceof ewe.util.ByteArray){
		try{
			ByteArray ba = (data instanceof byte []) ? new ByteArray((byte [])data) : (ByteArray)data;
			if (((options & LIMIT_DECODED_SIZE) != 0) && width > 0 && height > 0){
				try{
					setImage(new Image(ba,0,width,height));
				}catch(IllegalArgumentException e){
					setImage(new Image(ba,0));
				}
			}else
				setImage(new Image(ba,0));
		}catch(Exception e){
			setImage(null);
		}
	}
}
ImageBuffer buffer = new ImageBuffer();

//===================================================================
Rect getScale(boolean fit,int width,int height)
//===================================================================
{
	Rect r = new Rect();
	int mw = this.width-borderWidth*2;
	int mh = this.height-borderWidth*2;
	if (!fit) {
		r.width = mw;
		r.height = mh;
	}else{
		double xscale = width == 0 ? 1 : (double)mw/width;
		double yscale = height == 0 ? 1: (double)mh/height;
		double scale = Math.min(xscale,yscale);
		r.width = (int)(scale*width);
		r.height = (int)(scale*height);
	}
	return r;
}
static ewe.util.IntArray pixels = new ewe.util.IntArray();
static Image scaled;

//-------------------------------------------------------------------
private Image fromIImage(IImage what)
//-------------------------------------------------------------------
{
	if (what == null || what.usesAlpha() || !(what instanceof mImage)) return null;
	return ((mImage)what).image;
}
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
/* - Rotates the image.
	Image image = new Image(this.image.getWidth(),this.image.getHeight(),Image.TRUE_COLOR);
	Graphics gr = new Graphics(image);
	gr.setColor(getBackground());
	gr.fillRect(0,0,image.getWidth(),image.getHeight());
	this.image.draw(gr,0,0,0);
	image = image.rotate(null,90);
*/
	int flags = getModifiers(true);
	if (!((flags & Invisible) == 0)) return;
	Color oldBack = backGround;
	Color iback = image == null ? null : image.getBackground();
	if (iback == null) iback = getBackground();
	backGround = iback;
	super.doPaint(g,area);
	int width = this.width-borderWidth*2;
	int height = this.height-borderWidth*2;
	if (true){
		if (image != null){
			//ewe.sys.Vm.debug(width+", "+height);
			int w = image.getWidth();
			int h = image.getHeight();
			//ewe.sys.Vm.debug(" and "+w+", "+h);
		  /*
			Graphics g2 = buffer.get(w,h,true);
			g2.setColor(backGround);
			g2.fillRect(0,0,width,height);
			image.draw(g2,0,0,0);
			*/
			Rect rect = new Rect(0,0,w,h);
			if (w > width || h > height)
				if ((options & SHRINK) != 0)
					rect = getScale(((options & FIT) != 0),w,h);
			if (w < width || h < height)
				if ((options & STRETCH) != 0)
					rect = getScale(((options & FIT) != 0),w,h);
			rect.x = borderWidth+(width-rect.width)/2;
			rect.y = borderWidth+(height-rect.height)/2;
			if (rect.width == w && rect.height == h)
				image.draw(g,rect.x,rect.y,0);//g.drawImage(image,null,null,new Rect(0,0,w,h),rect,0);
			else{
				if (scaled == null || scaled.getWidth() != rect.width || scaled.getHeight() != rect.height){
					if (scaled != null) scaled.free();
					scaled = new Image(rect.width,rect.height,Image.ARGB_IMAGE);
				}
				scaled = ImageTool.scale(ImageTool.toImageData(image),rect.width,rect.height,(options & ROUGH_SCALING) != 0 ? ImageTool.SCALE_ROUGH : 0);
				scaled.draw(g,rect.x,rect.y,0);
			}
		}
	}else
		image.draw(g,borderWidth,borderWidth,0);
	backGround = oldBack;
	//if (image != this.image) image.free();
}

//-------------------------------------------------------------------
protected void calculateSizes()
//-------------------------------------------------------------------
{
	preferredWidth = preferredHeight = 10;
	if (image != null) {
		preferredWidth = image.getWidth()+borderWidth*2;
		preferredHeight = image.getHeight()+borderWidth*2;
	}

	//System.out.println("Image ps: "+Geometry.toString(preferredSize));
}

//##################################################################
}
//##################################################################

