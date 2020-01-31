package ewe.fx;
import ewe.io.*;
//##################################################################
public class RotatedGraphics extends Graphics{
//##################################################################

Image image;
ISurface destination;
int rotation;
boolean dontFreeImage;


/**
 * Create a Graphics that will rotate all drawing by the specified amount in degrees.
 * The drawing operations will not appear on the surface until a flush() operation is
 * done.
 * @param s The destination surface.
 * @param surfaceData If the surface data is already buffered in an image, then that image can be used.
 * @param rotation Should be 90, 180, 270 or 0.
 */
//===================================================================
public RotatedGraphics(ISurface s,Image surfaceData,int rotation)
//===================================================================
{
	super(surfaceData);
	image = surfaceData;
	this.rotation = rotation;
	this.destination = s;
	dontFreeImage = true;
}
/**
 * Create a Graphics that will rotate all drawing by the specified amount in degrees.
 * The drawing operations will not appear on the surface until a flush() operation is
 * done.
 * @param s The destination surface.
 * @param width The width of the surface.
 * @param height The height of the surface.
 * @param rotation Should be 90, 180, 270 or 0.
 */
//===================================================================
public RotatedGraphics(ISurface s,int width,int height,int rotation)
//===================================================================
{
	super(new Image(width,height));//,Image.TRUE_COLOR));
	image = (Image)surface;
	this.rotation = rotation;
	this.destination = s;
}

//===================================================================
public void free()
//===================================================================
{
	super.free();
	if (!dontFreeImage) image.free();
	destination = null;
}
//===================================================================
public void flush()
//===================================================================
{
	if (destination != null){
		Graphics g = new Graphics(destination);
		Image im = image.rotate(null,rotation);
		im.draw(g,0,0,0);
		im.free();
	}
}
//##################################################################
}
//##################################################################

