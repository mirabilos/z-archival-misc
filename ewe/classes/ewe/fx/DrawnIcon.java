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
package ewe.fx;

/**
* A DrawnIcon represents a number of Images which are "drawn" using Graphics primitives
* rather than from a Image.
**/
//##################################################################
public class DrawnIcon extends mImage{
//##################################################################
/**
* This is a 'X' cross.
**/
public static final int CROSS = 1;
/**
* This is a tick mark.
**/
public static final int TICK = 2;

Pen pen;
Brush brush;
int type;


/**
 * Create a new DrawnIcon
 * @param type either CROSS or TICK.
 * @param width The width of the icon.
 * @param height The height of the icon.
 * @param c The color of the icon.
 */
//===================================================================
public DrawnIcon(int type,int width,int height,Color c)
//===================================================================
{
	this(type,width,height,new Pen(c,Pen.SOLID,2),null);
}
/**
 * Create a new DrawnIcon
 * @param type either CROSS or TICK.
 * @param width The width of the icon.
 * @param height The height of the icon.
 * @param p The Pen to use for drawing.
 * @param b The Brush to use for drawing.
 */
//===================================================================
public DrawnIcon(int type,int width,int height,Pen p,Brush b)
//===================================================================
{
	location.set(0,0,width,height);
	this.type = type;
	pen = p;
	brush = b;
}
//==============================================================
public void	doDraw(Graphics g,int options)
//==============================================================
{
	Color oldC = g.getColor();
	Color opc = pen != null ? pen.color : null, obc = brush != null ? brush.color : null;
	if ((options & DISABLED) != 0){
		if (pen != null) pen.color = Color.DarkGray;
		if (brush != null) brush.color = Color.DarkGray;
	}
	Pen oldPen = g.setPen(pen);
	Brush oldBrush = g.setBrush(brush);
	Rect r = new Rect().set(location);
	int xt = pen.thickness-1;
	r.x += xt; r.width -= xt*2;
	r.y += xt; r.height -= xt*2;
	switch(type){
		case TICK:
			g.drawLine(r.x+r.width-1,r.y,r.x,r.y+r.height-1);
			g.drawLine(r.x,r.y+r.height-1,r.x,r.y+(r.height)/3);
			break;
		case CROSS:
			g.drawLine(r.x+r.width-1,r.y,r.x,r.y+r.height-1);
			g.drawLine(r.x,r.y,r.x+r.width-1,r.y+r.height-1);
			break;
	}
	if (pen != null) pen.color = opc;
	if (brush != null) brush.color = obc;
	g.setBrush(oldBrush);
	g.setPen(oldPen);
	g.setColor(oldC);
}
public boolean usesAlpha()
{
	return true;
}
//##################################################################
}
//##################################################################

