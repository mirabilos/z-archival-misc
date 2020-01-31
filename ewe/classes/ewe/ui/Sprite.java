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
import ewe.util.*;

//##################################################################
public class Sprite{
//##################################################################

Control lastDrawn;
Point lastPoint = new Point();
public IImage image;

//===================================================================
public Sprite(IImage image) {this.image = image;}
public Sprite(int width,int height,Color color)
{
	image = new Image(width,height);
	Graphics g = new Graphics((Image)image);
	g.setColor(color == null ? Color.Black : color);
	g.fillRect(0,0,width,height);
	g.free();
}
//===================================================================

//-------------------------------------------------------------------
void draw(Control who,int x,int y,int op)
//-------------------------------------------------------------------
{
	lastDrawn = who;
	lastPoint.set(x,y);
	if (who == null) return;
	Graphics g = who.getGraphics();
	if (g == null) return;
	g.setDrawOp(op);
	image.draw(g,x,y,0);
	g.free();
}
Rect refresh = new Rect();
boolean inMove = false;
//===================================================================
public void moveTo(Control who,int x,int y)
//===================================================================
{
	if (inMove) return;
	try{
	inMove = true;
	if (who == lastDrawn && x == lastPoint.x && y == lastPoint.y){
 		return;
	}
	if (lastDrawn != null) {
		lastDrawn.repaintNow(null,refresh.set(lastPoint.x,lastPoint.y,image.getWidth(),image.getHeight()));//draw(lastDrawn,lastPoint.x,lastPoint.y,Graphics.DRAW_XOR);
		//ewe.sys.Vm.debug(refresh.toString());
	}
	draw(who,x,y,Graphics.DRAW_OVER);
	}finally{
		inMove = false;
	}
}
//##################################################################
}
//##################################################################


