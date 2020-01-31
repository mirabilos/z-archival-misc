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
import java.awt.Image;
import java.awt.image.*;
//##################################################################
public class ImagePreparer{
//##################################################################

Image image;
boolean succeeded = false;
boolean infoOnly;

//===================================================================
public boolean prepare(Image image,boolean infoOnly)
//===================================================================
{
	this.image = image;
	this.infoOnly = infoOnly;
	Thread t = new Thread(new imageObserver());
	t.start();
	try{
		t.join();
	}catch(Exception e){
	}
	return succeeded;
}
//===================================================================
public boolean prepare(Image image)
//===================================================================
{
	return prepare(image,false);
}


	//##################################################################
	private class imageObserver implements Runnable,ImageObserver{
	//##################################################################
	int soFar = 0;
	//===================================================================
	public synchronized void run()
	//===================================================================
	{
		//sleep(2000);

		if (java.awt.Toolkit.getDefaultToolkit().prepareImage(image,-1,-1,this)){
			succeeded = true;
			return;
		}
		try{
			wait();
		}catch(Exception e){
		}
		if (!didAll) try{
			wait(1000);
		}catch(Exception e){
		}
	}
	int lines = 0;
	boolean didAll = false;

	//-------------------------------------------------------------------
	boolean success()
	//-------------------------------------------------------------------
	{
		didAll = true;
		succeeded = true;
		notify();
		return false;
	}
//==============================================================
	public synchronized boolean
	imageUpdate(Image im,int flags,int x,int y,int width,int height)
//==============================================================
{
	soFar |= flags;
	if (infoOnly && ((soFar & (ImageObserver.WIDTH|ImageObserver.HEIGHT)) == (ImageObserver.WIDTH|ImageObserver.HEIGHT))) {
		return success();
	}
	if ((flags & ImageObserver.ALLBITS) != 0) {
		return success();
	}
	if ((flags & ImageObserver.SOMEBITS) != 0) {
		lines += height;
		if (lines >= im.getHeight(null)) {
			succeeded = true;
			notify();
		}
		//if ((count % 10) == 0) System.out.println("SomeBits ready:"+count);
		//count++;
		//System.out.println(x+", "+y+", "+width+", "+height);
	}
	if ((flags & ImageObserver.ERROR) != 0) {
		succeeded = false;
		notify();
		return false;
	}
	return(true);
}
	//##################################################################
	}
	//##################################################################



//##################################################################
}
//##################################################################
