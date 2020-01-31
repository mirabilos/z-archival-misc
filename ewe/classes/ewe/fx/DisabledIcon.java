package ewe.fx;
import ewe.ui.*;

/**
* This is a mImage that displays one of two images (the normal and disabled image)
* depending on whether it is asked to draw the image in normal or Disabled mode. Disabled
* mode is selected when a draw() method call is done with the DISABLED option selected. A
* normal mImage would display a grayed version of the image when disabled, but this mImage
* will display the disabled image instead.
**/
//##################################################################
public class DisabledIcon extends mImage{
//##################################################################
/**
* The image to display normally.
**/
public IImage normal;
/**
* The image to display when disabled.
**/
public IImage disabled;
/**
 * @param normal The image to display normally.
 * @param disabled The image to display when disabled.
 */
//===================================================================
public DisabledIcon(IImage normal,IImage disabled)
//===================================================================
{
	this.normal = normal;
	this.disabled = disabled;
}
//===================================================================
public void doDraw(Graphics g, int options)
//===================================================================
{
	drawable = ((options & mImage.DISABLED) != 0) ? disabled : normal;
	super.doDraw(g,options & ~DISABLED);
}
/**
 * This returns the width of the normal image.
 */
//===================================================================
public int getWidth() {return normal.getWidth();}
//===================================================================
/**
 * This returns the height of the normal image.
 */
//===================================================================
public int getHeight() {return normal.getHeight();}
//===================================================================

/**
* If this is true then a call to free() will not free the supplied normal and disabled
* images, but it will still call super.free().
**/
public boolean dontFreeImages;

//===================================================================
public void free()
//===================================================================
{
	normal.free();
	disabled.free();
	super.free();
}
//##################################################################
}
//##################################################################

