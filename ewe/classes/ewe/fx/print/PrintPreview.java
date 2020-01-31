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
package ewe.fx.print;
import ewe.fx.*;
import ewe.util.Vector;
import ewe.sys.Handle;
import ewe.sys.TaskObject;
import ewe.sys.Task;
import ewe.ui.ImageControl;
import ewe.ui.ScrollBarPanel;
import ewe.ui.Control;
/**
A PrintPreview is very similar to a PrinterJob except that the output
for each page will be an Image of a resolution that you preset.<p>


**/
//##################################################################
public class PrintPreview extends PrinterJobObject{
//##################################################################

private double xDPI = 72, yDPI = 72;

public ImageTool finalImageCreator;
public ImageTool pageRectImageCreator;
/**
 * The options used when images are being created for the
 * PrintPreview. If finalImageCreator or pageRectImageCreator are not null,
 * then those image creators will be determine the image type created.
 * <p>
 * By default this field is Image.RGB_IMAGE
 */
public int createImageOptions = Image.RGB_IMAGE;
/**
If this is set true, then rough scaling is done when producing the
final images for output. Rough scaling is faster than smooth scaling.
**/
public boolean scaleRough;

//===================================================================
public void setImageDPI(double xDPI, double yDPI)
//===================================================================
{
	this.xDPI = xDPI;
	this.yDPI = yDPI;
}
//===================================================================
public void setImageDPI(double imageDPI)
//===================================================================
{
	setImageDPI(imageDPI, imageDPI);
}
//===================================================================
public Control getDisplayFor(Image im)
//===================================================================
{
	ImageControl ic = new ImageControl(im);
	ic.options = 0;
	ScrollBarPanel sp = new ScrollBarPanel(ic);
	ic.setPreferredSize(im.getWidth(),im.getHeight());
	return sp;
}
//-------------------------------------------------------------------
protected Task getPrintTask(final Printable toPrint,final PageFormat format,final PrintOptions po)
//-------------------------------------------------------------------
{
	return new TaskObject(){
		protected void doRun(){
			try{
				PageFormat pf = format;
				pf.acceptRequests();
				Vector v = new Vector();
				handle.returnValue = v;
				try{
					PageCounter pc = new PageCounter(handle,toPrint,format,po);
					while(pc.moveToNextPage())
						if (!toPrint.print(handle,getPrintSurface(pf,v),pf,pc.currentPageIndex)) break;
				}catch(Throwable e){
					handle.fail(e);
					return;
				}
				if (handle.shouldStop) handle.set(handle.Aborted);
				else handle.set(handle.Succeeded);
			}finally{
				printingComplete();
			}
		}
	};
}

/**
This is called by default by putPageRect() if the image in the PageRect must
be scaled down to the final image for display. This gives you an opportunity to
override the way that this scaling is done - to perhaps improve on the normal method.
<p>
The dest image is the exact size that the source image should be scaled down into and
will be of the same type as the final image for the current page. The source image
will be from the current PageRect. Do not free() either the source or dest images when
you are done.
* @param source The source image to scale down.
* @param dest The destination image to scale into.
* @return true if the scaling was successful, false if it was not - in which case the
* default scaling method will be used.
*/
//-------------------------------------------------------------------
protected boolean scaleDownToFinal(Image source, Image dest)
//-------------------------------------------------------------------
{
	try{
		ImageTool.scale(source,dest,scaleRough? ImageTool.SCALE_ROUGH : 0);
		return true;
	}catch(Throwable t){
		t.printStackTrace();
	}
	return false;
}
private Image scaleBuffer;

//-------------------------------------------------------------------
private void normalPutPageRect(PageRect r, Image destImage, Graphics finalImageGraphics, Rect destArea)
//-------------------------------------------------------------------
{
	Graphics dest = finalImageGraphics;
	dest.drawImage(r.getScaledImage(destArea.width,destArea.height),destArea.x,destArea.y);
}

//-------------------------------------------------------------------
private void scaleDownPutPageRect(PageRect r, Image destImage, Graphics finalImageGraphics, Rect destArea)
//-------------------------------------------------------------------
{
	try{
		Image source = r.getImage();
		if (source == null || destArea.width >= r.getBufferWidth()) normalPutPageRect(r,destImage,finalImageGraphics,destArea);
		else{
			if (scaleBuffer == null || scaleBuffer.getWidth() != destArea.width || scaleBuffer.getHeight() != destArea.height){
				if (scaleBuffer != null) scaleBuffer.free();
				scaleBuffer = ImageTool.createImageUsing(finalImageCreator,destArea.width,destArea.height,createImageOptions);
			}
			if (!scaleDownToFinal(source,scaleBuffer))
				normalPutPageRect(r,destImage,finalImageGraphics,destArea);
			else{
				Graphics g = finalImageGraphics;
				g.drawImage(scaleBuffer,destArea.x,destArea.y);
			}
		}
	}catch(Throwable e){
		normalPutPageRect(r,destImage,finalImageGraphics,destArea);
	}
}
/**
This is used to transfer a PageRect drawn by the Printable object onto the PrintPreview's
destination image. The <b>entire</b> area of the PageRect must be scaled and placed
into the destArea within the destImage.<p>
The default version of this method uses the getScaledImage() method of PageRect, but this
method may not be acceptable for your implementation - so you may choose to replace it
with a more appropriate version.
* @param r The PageRect that must be transferred to the destImage.
* @param destImage The destination image.
* @param finalImageGraphics A pre-created Graphics for the destImage - do not free() this Graphics.
* @param destArea The area within destImage that the PageRect must fit into.
*/
//-------------------------------------------------------------------
protected void putPageRect(PageRect r, Image destImage, Graphics finalImageGraphics, Rect destArea)
//-------------------------------------------------------------------
{
	scaleDownPutPageRect(r,destImage,finalImageGraphics,destArea);
}
//-------------------------------------------------------------------
private PrintSurface getPrintSurface(PageFormat pf,final Vector v)
//-------------------------------------------------------------------
{
	return new PrintSurfaceObject(pf){
		Image finalImage;
		Graphics finalImageGraphics;
		Dimension dim;
		Rect rect = new Rect();
		{
			allocatePageRectScaling = true;
			format.xDPI = xDPI;
			format.yDPI = yDPI;
			finalImage = ImageTool.createImageUsing(finalImageCreator,getWidthHeight(format.fullPageWidth,xDPI),getWidthHeight(format.fullPageHeight,yDPI),createImageOptions);
			v.add(finalImage);
			Graphics g = finalImageGraphics = new Graphics(finalImage);
			g.setColor(Color.White);
			g.fillRect(0,0,finalImage.getWidth(),finalImage.getHeight());
		}
		public Image getImageBuffer(int width, int height){
			return ImageTool.createImageUsing(pageRectImageCreator,width,height,createImageOptions);
		}
		public void putPageRect(PageRect r){
			dim = getOutputSize(r.getWidthOnPaper(),r.getHeightOnPaper(),dim);
			rect.set((int)((r.getX()*xDPI)/72.0),(int)((r.getY()*yDPI)/72.0),dim.width,dim.height);
			PrintPreview.this.putPageRect(r,finalImage,finalImageGraphics,rect);
		}
	};
}
//===================================================================
public Vector getImages(Handle fromPrint)
//===================================================================
{
	try{
		fromPrint.waitOn(fromPrint.Success);
		return (Vector)fromPrint.returnValue;
	}catch(Exception e){
		return null;
	}
}
//##################################################################
}
//##################################################################

