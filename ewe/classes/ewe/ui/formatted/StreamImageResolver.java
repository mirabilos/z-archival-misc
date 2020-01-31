/*
Copyright (c) 2001 Michael L Brereton  All rights reserved.

This software is furnished under the Gnu General Public License, Version 2, June 1991,
and may be used only in accordance with the terms of that license. This source code
must be distributed with a copy of this license. This software and documentation,
and its copyrights are owned by Michael L Brereton and are protected by copyright law.

If this notice is followed by a Wabasoft Copyright notice, then this software
is a modified version of the original as provided by Wabasoft. Wabasoft also
retains all rights as stipulated in the Gnu General Public License. These modifications
were made to the Version 1.0 source code release of Waba, throughout 2000 and up to May
2001.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. MICHAEL L BRERETON ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. MICHAEL L BRERETON SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

MICHAEL L BRERETON SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY MICHAEL L BRERETON.
*/

package ewe.ui.formatted;
import ewe.fx.*;
import ewe.sys.*;
import ewe.util.*;
import ewe.data.PropertyList;
import ewe.io.*;

//##################################################################
public class StreamImageResolver implements ImageResolver{
//##################################################################

//-------------------------------------------------------------------
protected ewe.util.Tag getStreamFor(String imageName) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	File f = File.getNewFile(imageName);
	if (!f.canRead()) throw new IOException();
	Tag t = new Tag();
	t.value = f.toReadableStream();
	t.tag = (int)f.length();
	return t;
}

private static ByteArray scaleArray;
private Hashtable imageCache = new Hashtable();

//===================================================================
public Handle resolveImage(final String name,final boolean allowAnimatedImages)
//===================================================================
{
	return new ewe.sys.TaskObject(){
		protected void doRun(){
			if (scaleArray == null) scaleArray = new ByteArray();
			try{
				Tag tag = getStreamFor(name);
				Object in = tag.value;
				InputStream is = null;
				if (in instanceof InputStream) is = (InputStream)in;
				else if (in instanceof Stream) is = ((Stream)in).toInputStream();
				if (is == null) throw new IOException("Could not get Stream to image.");
				int length = tag.tag;
				if (length == 0){
					is.close();
					throw new IOException();
				}
				try{
					IImage got = null;
					handle.doing = "Reading bytes...";
					ByteArray ba = ewe.io.StreamUtils.readAllBytes(handle,is,null,length,0);
					if (ba == null) {
						handle.set(handle.Aborted|handle.Stopped);
						return;
					}
					/*
					Handle h = ewe.io.IO.readAllBytes(in,length,true);
					if (!waitOn(h,h.Success,true))
						handle.set(handle.Failed);
					else{
					*/
					handle.doing = "Decoding image...";
					handle.setProgress(0);
					//ByteArray ba = (ByteArray)h.returnValue;
					ImageInfo info = Image.getImageInfo(ba,null);
					if (info.format == ImageInfo.FORMAT_GIF && allowAnimatedImages){
						ewe.io.MemoryFile mf = new ewe.io.MemoryFile();
						mf.data = ba;
						got = ewe.graphics.AnimatedIcon.getAnimatedImageFromGIF(mf);
					}else
						got = new mImage(new Image(ba,0));
					handle.returnValue = got;
					handle.set(Handle.Succeeded);
				}catch(Exception e){
					handle.fail(e);
				}
			}catch(ewe.io.IOException e){
				handle.fail(e);
			}
		}
	}.startTask();
}
//===================================================================
public Handle resolveImage(final PropertyList imageProperties,final boolean allowAnimatedImages,final Dimension maxSize)
//===================================================================
{
	final String src = imageProperties.getString("src",null);
	if (src == null) return new Handle(Handle.Failed,null);
	return new ewe.sys.TaskObject(){
		protected void doRun(){
			Handle res = (Handle)imageCache.get(src);
			if (res == null) {
				res = resolveImage(src,allowAnimatedImages);
				imageCache.put(src,res);
			}
			if (!waitOn(res,res.Success,true)){
				handle.set(Handle.Failed);
			}else{
				IImage got = (IImage)res.returnValue;
				//
				if (maxSize != null && !(got instanceof ewe.graphics.AniImage)){
					if (got.getWidth() > maxSize.width || got.getHeight() > maxSize.height){
						got = new PixelBuffer(got).scale(maxSize.width,maxSize.height,null,PixelBuffer.SCALE_KEEP_ASPECT_RATIO,scaleArray).toMImage();
					}
				}
				//
				PropertyList pl = PropertyList.toPropertyList(imageProperties);
				int ww = pl.getInt("width",got.getWidth());
				int hh = pl.getInt("height",got.getHeight());
				if (ww <= 0) ww = got.getWidth();
				if (hh <= 0) hh = got.getHeight();
				if (maxSize != null){
					if (ww > maxSize.width) ww = maxSize.width;
					if (hh > maxSize.height) hh = maxSize.height;
				}
				if (ww != got.getWidth() && hh != got.getHeight())
					got = new PixelBuffer(got).scale(ww,hh,null,0,scaleArray).toMImage();
				handle.returnValue = got;
				handle.set(Handle.Succeeded);
			}
		}
	}.startTask();
}



//##################################################################
}
//##################################################################

