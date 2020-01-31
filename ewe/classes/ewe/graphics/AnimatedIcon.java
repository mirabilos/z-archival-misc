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
import ewe.util.*;
import ewe.sys.Handle;

//##################################################################
public class AnimatedIcon extends AniImage implements Runnable{
//##################################################################
/*
This is true by default and tells the image to start looping on a draw method
call if it has not yet started.
*/
public boolean startOnDraw = true;
/**
* Create a blank AnimatedIcon.
**/
//===================================================================
public AnimatedIcon()
//===================================================================
{

}
/**
* This checks an encoded GIF byte Stream to see if the image contains is an
* animated GIF or a single Image GIF. If it is animated then an AnimatedIcon
* is returned, otherwise an mImage is returned.
* @param imageFile The encoded GIF stored in a Stream of bytes. If you have a byte
* array containing the data you can use ewe.io.MemoryFile to convert it into a ewe.io.Stream
* by using new MemoryFile(byte[] data, int start, int length, String mode).
* @exception IllegalArgumentException if there is an error decoding the GIF.
*/
//===================================================================
public static IImage getAnimatedImageFromGIF(ewe.io.Stream imageFile)
throws IllegalArgumentException
//===================================================================
{
	ImageInfo [] images = new ImageCodec().getImages(imageFile,true);
	if (images.length == 1) return new mImage(images[0].image);
	return new AnimatedIcon(images);
}
//===================================================================
public AnimatedIcon(ewe.io.Stream imageFile) throws IllegalArgumentException
//===================================================================
{
	this(new ImageCodec().getImages(imageFile,true));
}
//===================================================================
public AnimatedIcon(ImageInfo [] images)
//===================================================================
{
	if (images.length > 0){
 		int w = images[0].width, h = images[0].height;
		boolean misSized = false;
		for (int i = 1; i<images.length; i++)
			if (images[i].width != w || images[i].height != h){
				misSized = true;
				break;
			}
		if (misSized){
			PixelBuffer pb = new PixelBuffer(images[0].image);
			for (int i = 1; i<images.length; i++){
				Graphics g = pb.getDrawingBuffer(null,null,1.0);
				mImage mi = new mImage(images[i].image);
				mi.draw(g,images[i].x,images[i].y,0);
				mi.free();
				//g.drawImage(images[i].image,
				pb.putDrawingBuffer(pb.PUT_BLEND);
				images[i].image = pb.toImage();
			}
			pb.free();
		}
	}
	for (int i = 0; i<images.length; i++){
		if (i == 0){
			location.width = images[i].width;
			location.height = images[i].height;
		}
		String name = ewe.sys.Convert.toString(i);
		addImage(name,images[i].image);
		int pause = images[i].pauseInMillis;
		if (pause < 10) pause = 250;
		addLoopSegment(name,pause);
	}
	if (images.length > 0) change(images[0].image);
}
//===================================================================
public AnimatedIcon(String loopString)
//===================================================================
{
	this(loopString,null);
}
//===================================================================
public AnimatedIcon(String loopString,Dimension size)
//===================================================================
{
	this(loopString,null,size);
}
//===================================================================
public AnimatedIcon(String loopString,String prependString,Dimension size)
//===================================================================
{
	String before, after;
	before = after = "";
	if (prependString != null) {
		before = mString.leftOf(prependString,'*');
		after = mString.rightOf(prependString,'*');
	}
	setLoop(loopString);
	IImage first = null;
	for (int i = 0; i<loops.size(); i++){
		String name = ((Tag)loops.get(i)).value.toString();
		IImage g = fetchForName(before+name+after);
		addImage(name,g);
		if (first == null) first = g;
	}
	if (size == null && first != null)
		size = new Dimension(first.getWidth(),first.getHeight());
	if (size != null) {
		location.width = size.width;
		location.height = size.height;
	}
}
protected Vector loops;
/**
 * If you call this directly to specify the loop string, then you will have had to already
 * add images to the internal Hashtable using addImage().
 * @param loopString The string in the format: "Image1|PauseTime1|Image2|PauseTime2|..."
 */
//===================================================================
public void setLoop(String loopString)
//===================================================================
{
	loops = new Vector();
	String [] all = mString.split(loopString,'|');
	for (int i = 0; i<all.length-1; i+=2){
		Tag t = new Tag();
		t.value = all[i];
		t.tag = ewe.sys.Convert.toInt(all[i+1]);
		loops.add(t);
	}
}
//===================================================================
public void addLoopSegment(String imageName,int pauseTimeInMillis)
//===================================================================
{
	if (loops == null) loops = new Vector();
	Tag t = new Tag();
	t.value = imageName;
	t.tag = pauseTimeInMillis;
	loops.add(t);
}
//-------------------------------------------------------------------
protected IImage fetchForName(String name)
//-------------------------------------------------------------------
{
	try{
		return new mImage(name);
	}catch(Exception e){
		return null;
	}
}
/**
* This is the thread that is running the animation.
**/
protected ewe.sys.Task loopTask;
/**
 * This simply calls the start() method.
 */
//===================================================================
public void shown() {start();}
//===================================================================
/**
* This simply calls the stop() method.
**/
//===================================================================
public void closing() {stop();}
//===================================================================
/**
* Requests that the animation stop but does not free resources.
**/
//===================================================================
public void stop()
//===================================================================
{
	if (loopTask != null) loopTask.getHandle().stop(0);//stopTask(0);
	loopTask = null;
}
/**
This actually does the looping of the images. You would not normally
call this directly, rather you would call start() or you could create
a new Thread for this class and the run() method would then call doLoop().
<p>
This loop will exit if the passed Handle has its stop() method called or
if the ImageRefresher is set to null or has been garbage collected.
*/
//===================================================================
public void doLoop(Handle h)
//===================================================================
{
	if (loops != null){
		for(int i = 0; h == null || !h.shouldStop; i = (i+1)%loops.size()){
			try{
				Tag t = (Tag)loops.get(i);
				change(getImage(t.value.toString()));
				if (!refreshNow()) break;
				try{
					ewe.sys.mThread.sleep(t.tag);
				}catch(Exception e){

				}
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
		}
	}
}
/**
By default this calls startLoop().
**/
//===================================================================
public void start()
//===================================================================
{
	startLoop();
}
/**
* This starts a new mThread that calls doLoop() method.
**/
//===================================================================
public Handle startLoop()
//===================================================================
{
	stop();
	loopTask = new ewe.sys.TaskObject(){
		public void doRun(){
			doLoop(handle);
		}
	};
	return loopTask.startTask();
}
/**
 * Override this to free used resources.
 */
//===================================================================
public void free()
//===================================================================
{
	stop();
	if (images != null){
		for (Iterator i = images.entries(); i.hasNext();){
			IImage image = (IImage)((Map.MapEntry)i.next()).getValue();
			image.free();
		}
		images.clear();
	}
	super.free();
}
/**
* Override this to do custom animation. Call the change() or move() methods
* to change/move the image and then call refresh() to update it on the screen.
**/
//===================================================================
public void run()
//===================================================================
{
}
protected Hashtable images;
/**
 * This adds an image to the internal Hashtable using with the specified name.
 * @param image The image.
 * @param name The name used as the key for the image - it does not need to be the file/resource
	name of the image.
 */
//===================================================================
public void addImage(String name,IImage image)
//===================================================================
{
	if (image == null) return;
	if (images == null) images = new Hashtable();
	images.put(name,image);
}
/**
 * Get a named image from the list of images.
 * By default this gets the image from the Hashtable, but you can construct the image
 * from scratch here if you want.
 * @param name The key name for the image as specified in addImage().
 * @return The image if found.
 */
//===================================================================
public IImage getImage(String name)
//===================================================================
{
	if (images == null) return null;
	return (IImage)images.get(name);
}

//===================================================================
public void doDraw(Graphics g, int options)
//===================================================================
{
	if (startOnDraw){
		if (loopTask == null) start();
	}
	super.doDraw(g,options);
}
//##################################################################
}
//##################################################################

