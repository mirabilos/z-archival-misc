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
package ewe.filechooser;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.io.*;
import ewe.sys.Time;
import ewe.sys.Locale;

//##################################################################
public class ImageFileChooser extends FileChooser{
//##################################################################

ImageControl preview;
IImage lastImage;
Dimension lastSize;
mLabel info;
//===================================================================
public ImageFileChooser()
//===================================================================
{
	this(OPEN,baseDir);

}
//===================================================================
public ImageFileChooser(int type,String initial)
//===================================================================
{
	this(type,initial,ewe.sys.Vm.newFileObject());
}
//===================================================================
public ImageFileChooser(int type,String initial,File aFileModel)
//===================================================================
{
	super(type|EXTRA_CONTROL,initial,aFileModel);
	Dimension d = getPreferredSize(null);
	d.width += 100;
	setPreferredSize(d.width,d.height);
	String msk = "*.png,*.bmp";
	if (ImageCodec.canDecodeGIF) msk+= ",*.gif";
	if (ImageCodec.canDecodeJPEG) msk+= ",*.jpg,*.jpeg";
	msk+= " - Image Files";
	addMask(msk);
	addMask(allFilesMask);
	extraPanel.setBorder(mInput.inputEdge|BF_RECT,3);
	extraPanel.addLast(new mLabel("Image Preview:"),HSTRETCH,DONTFILL|CENTER);
	extraPanel.addLast(info = new mLabel(" "),HSTRETCH,FILL);
	info.anchor = CENTER;
	extraPanel.addLast((preview = new ImageControl(null)).setPreferredSize(50,50));
	//preview.backGround = Color.Sand;
}
//===================================================================
public void resizeTo(int width,int height)
//===================================================================
{
	super.resizeTo(width,height);
	setImage(lastImage,lastSize);
}
//-------------------------------------------------------------------
void setImage(IImage img,Dimension trueSize)
//-------------------------------------------------------------------
{
	IImage old = lastImage;
	lastImage = img;
	Dimension d = preview.getSize(null);
	if (img == null) {
		img = new DrawnIcon(DrawnIcon.CROSS,d.width,d.height,Color.Black);
		String txt = "No Preview";
		if (trueSize != null)
			txt += " ("+trueSize.width+" x "+trueSize.height+")";
		info.setText(txt);
	}else
		if (trueSize == null)
			info.setText(img.getWidth()+" x "+img.getHeight());
		else
			info.setText(trueSize.width+" x "+trueSize.height);
	lastSize = trueSize;
	if (old != null && old != img) old.free();
	preview.setImage(img);
}
File previewed;
//-------------------------------------------------------------------
protected void newFileSelected(File f)
//-------------------------------------------------------------------
{
	if (f != null)
		if (f.isDirectory())
			return;
	previewed = f;
	info.setText("Creating...");
	Dimension s = preview.getSize(null);
	Dimension trueSize = new Dimension();
	IImage got = getImage(f,s.width,s.height,true,trueSize);
	if (f == previewed){
		setImage(got,trueSize);
	}else
		if (got != null) {
			got.free();
			//ewe.sys.Vm.debug("Freeing; "+got);
		}
}

/**
* This is the maximum size of an image to preview or get in bytes.
**/
public static int imageSizeLimit = ewe.sys.Vm.isMobile() ? 500000 : 5000000;
//===================================================================
public static IImage getImage(File f,boolean limitedSize)
//===================================================================
{
	return getImage(f,0,0,limitedSize,null);
}
//===================================================================
public static IImage getImage(File f,int width,int height, boolean limitedSize,Dimension trueSize)
//===================================================================
{
	if (f == null) return null;
	int len = f.getLength();
	if (len < 16) return null;
	Stream trueStream = f.getInputStream();
	RandomAccessStream in = RewindableStream.toRewindableStream(trueStream);
	if (in == null) return null;
	ImageInfo ii = null;
	try{
		ii = Image.getImageInfo(in,null);
		if (trueSize != null){
			trueSize.width = ii.width;
			trueSize.height = ii.height;
		}
		if (!ii.canScale && limitedSize && ii.width*ii.height*4 > imageSizeLimit)
			throw new Exception();
	}catch(Exception e){
		in.close();
		return null;
	}finally{
	}
	/*
	byte [] start = new byte[16];
	try{
		if (in.readBytes(start,0,16) != 16) return null;
	}finally{
		in.close();
	}
	if (!ImageCodec.isDecodable(start)) return null;
	*/
	try{
		RewindableStream.rewind(in);
		if (ii.format == ii.FORMAT_GIF){
			IImage got = ewe.graphics.AnimatedIcon.getAnimatedImageFromGIF(trueStream);
			if (got instanceof mImage) return got;
			else return new mImage(got);
		}
		Image i = ii.canScale ? new Image(in,0,width,height) : new Image(in,0);
		return new mImage(i);
	}catch(Exception e){
		//ewe.sys.Vm.debug(ewe.sys.Vm.getStackTrace(e,5));
		//e.printStackTrace();
		return null;
	}finally{
		in.close();
	}
	/*
	MemoryFile mf = MemoryFile.createFrom(in,null);
	if (mf == null) return null;
	try{
		return new mImage(new Image(mf.data,0));
	}catch(Exception e){
		return null;
	}
	*/
}
/**
 * This will execute an ImageFileChooser and return the chosen image.
 * @param title The title for the displayed ImageFileChooser
 * @param initial The intial directory - may be null.
 * @param limitedSize true to limit the size of the image to half a megabyte.
 * @return The Image selected or null if cancelled.
 */
//===================================================================
public static IImage getImage(String title,String initial,boolean limitedSize)
//===================================================================
{
	ImageFileChooser ifc = new ImageFileChooser(OPEN,initial);
	ifc.title = title;
	if (ifc.execute() == IDCANCEL) return null;
	return getImage(ifc.getChosenFile(),limitedSize);
}
/**
* This can be called after the user has selected a file.
**/
//===================================================================
public IImage getChosenImage(boolean limitedSize)
//===================================================================
{
	return getImage(getChosenFile(),limitedSize);
}
/**
* This can be called after the user has selected a file or files.
**/
//===================================================================
public IImage[] getChosenImages(boolean limitedSize)
//===================================================================
{
	Vector v = new Vector();
	File [] all = getAllChosenFiles();
	for (int i = 0; i<all.length; i++){
		IImage im = getImage(all[i],limitedSize);
		if (im != null) v.add(im);
	}
	IImage[] ret = new IImage[v.size()];
	v.copyInto(ret);
	return ret;
}
//##################################################################
}

//##################################################################

