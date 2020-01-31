/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package ewe.fx;
/**
* A BufferedGraphics object provides a temporary off-screen Image for a Control
* to draw on, before being transferred to the on-screen Graphics.<p>
* This facility is necessary because some platforms (e.g. Qtopia on the Sharp Zaurus)
* do not support reading bits from a Window surface. This makes certain drawing operations
* impossible directly on this surface. e.g. XOR'ing of images and the drawing of transparent
* images. However all platforms should support reading bits from an Image and therefore
* these operations are possible using images.<p>
* You will generally create a BufferedGraphics in the doPaint() method of your control.
* You then call getGraphics() to get the Graphics object that you should use for your drawing
* and when you are done
* call release() on the BufferedGraphics to transfer what has been drawn to the original Graphics.<p>
* Note that you can construct the BufferedGraphics conditionally, which means that if the original
* Graphics was already drawing on an Image, that same graphics will be used and no temporary image
* will be created. In fact, when created conditionally, the canCopyFrom() method is called on the original
* Graphics, and if it returns true, then that original Graphics is used.<p>
**/
//##################################################################
public class BufferedGraphics{
//##################################################################

private Graphics original;
private Graphics myGraphics;
private Image image;
private Rect area;
/**
 * Create a new BufferedGraphics from the original.
 * @param original The original graphics.
 * @param drawingAreaNeeded A non-null rectangle specifying the area needed.
 * @param unconditionally If this is true then a temporary image will always be created. If it
	is false, then a temporary image will only be created if original.canCopyFrom() returns false.
 */
//===================================================================
public BufferedGraphics(Graphics original,Rect drawingAreaNeeded,boolean unconditionally)
//===================================================================
{
	this.original = original;
	boolean create = unconditionally;
	area = new Rect().set(drawingAreaNeeded);
	if (!original.canCopyFrom()) create = true;

	if (!create)
		myGraphics = original;
	else{
		image = new Image(drawingAreaNeeded.width,drawingAreaNeeded.height);
		//ewe.sys.Vm.debug("Got: "+area);
		myGraphics = new Graphics(image);
		myGraphics.translate(-drawingAreaNeeded.x,-drawingAreaNeeded.y);
	}
}
/**
 * Create a new BufferedGraphics from the original if the original does not support copying.
 * @param original The original graphics.
 * @param drawingAreaNeeded A non-null rectangle specifying the area needed.
 */
//===================================================================
public BufferedGraphics(Graphics original,Rect drawingAreaNeeded)
//===================================================================
{
	this(original,drawingAreaNeeded,false);
}
/**
 * Create a new BufferedGraphics from the original if the original does not support copying.
 * @param original The original graphics.
 * @param control The control that the original graphics was set up for. This will use a drawing
	area equal to the size of the control.
 */
//===================================================================
public BufferedGraphics(Graphics original,ewe.ui.Control control)
//===================================================================
{
	this(original,control.getDim(new Rect()),false);
}
/**
 * Get the graphics that you should use to draw on. This <b>may</b> be the same as the original Graphics
 * object, if that original Graphics already supported copying.
 * @return The Graphics that you should use to draw on.
 */
//===================================================================
public Graphics getGraphics()
//===================================================================
{
	return myGraphics;
}
/**
 * Copy the data already drawn on this surface, to the destination surface. You can continue
 * using the buffered graphics after if you need to. This does not call flush on the original graphics.
 * If you want to flush() the original Graphics as well, then you can call:
 * <pre>
 * 	 bufferedGraphics.flush().flush();
 * <pre>
 * @return The original graphics.
 */
//===================================================================
public Graphics flush()
//===================================================================
{
	if (myGraphics == original || myGraphics == null) return original;
	original.drawImage(image,area.x,area.y);
	return original;
}
/**
 * Copy the drawn image to the original graphics and free all temporary resources. You
	should not attempt to use this after calling this method.
 * @return The original Graphics.
 */
//===================================================================
public Graphics release()
//===================================================================
{
	flush();
	return cancel();
}
/**
 * Cancel drawing, free resources and do not update the original graphics.
 * @return The original Graphics.
 */
//===================================================================
public Graphics cancel()
//===================================================================
{
	if (myGraphics == original || myGraphics == null) return original;
	myGraphics.free();
	image.free();
	myGraphics = null;
	return original;
}
//##################################################################
}
//##################################################################

