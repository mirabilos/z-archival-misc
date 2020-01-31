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
 * Color represents a color.
 * <p>
 * A color is defined as a mixture of red, green and blue color values.
 * Each value is in the range of 0 to 255 where 0 is darkest and 255 is brightest.
 * For example, Color(255, 0, 0) is the color red.
 * <p>
 * Here are some more examples:
 * <ul>
 * <li>Color(0, 0, 0) is black
 * <li>Color(255, 255, 255) is white
 * <li>Color(255, 0, 0 ) is red
 * <li>Color(0, 255, 0) is green
 * <li>Color(0, 0, 255) is blue
 * </ul>
 */

public class Color
{
int red;
int green;
int blue;
int alpha;

boolean nullColor;

//===================================================================
public void setNull(boolean set)
//===================================================================
{
	nullColor = set;
}
//===================================================================
public boolean isNull()
//===================================================================
{
	return nullColor;
}
//===================================================================
public static boolean isNull(Color c)
//===================================================================
{
	return c == null || c.isNull();
}
/**
 * Constructs a color object with the given red, green and blue values.
 * @param red the red value in the range of 0 to 255
 * @param green the green value in the range of 0 to 255
 * @param blue the blue value in the range of 0 to 255
 */
public Color(int red, int green, int blue){this(red,green,blue,0xff);}
public Color(int red, int green, int blue, int alpha)
	{
	this.red = red;
	this.green = green;
	this.blue = blue;
	this.alpha = alpha;
	}
/** Returns the blue value of the color. */
public int getBlue()
	{
	return blue;
	}

/** Returns the green value of the color. */
public int getGreen()
	{
	return green;
	}

/** Returns the red value of the color. */
public int getRed()
	{
	return red;
	}
public int getAlpha() {return alpha;}
//==================================================================
// MLB
//==================================================================
public static final Color Null = new Color(0,0,0);
static{
	Null.setNull(true);
}
public static Color
	Black = new Color(0,0,0),
	White = new Color(0xff,0xff,0xff),
	DarkGray = new Color(96,96,96),
	LightGray = new Color(192,192,192),
	LighterGray = new Color(220,220,220),
	DarkBlue = new Color(0,0,128),
	Sand = new Color(217,217,195),
	LightBlue = new Color(220,220,255),
	MediumBlue = new Color(128,214,255),
	LightGreen = new Color(220,255,220),
	Pink = new Color(255,220,220);

private static boolean isMono = false;
public static void setMonochrome(boolean mono)
{
	isMono = mono;
	if (mono) {
		DarkGray.set(Black);
		LightGray.set(White);
		LighterGray.set(White);
		DarkBlue.set(Black);
	}else{
		DarkGray.set(new Color(96,96,96));
		LightGray.set(new Color(192,192,192));
		LighterGray.set(new Color(220,220,220));
		DarkBlue.set(new Color(0,0,128));
	}
}
public static boolean getMonochrome()
{
	return isMono;
}
public void set(int r,int g,int b){set(r,g,b,0xff);}
public void set(int r,int g,int b,int a)
{
	setNull(false);
	red = r; green = g; blue = b; alpha = a;
}
//===================================================================
public void set(Color other)
//===================================================================
{
	if (other != null) set(other.red,other.green,other.blue,other.alpha);
	if (other == null || other.nullColor) nullColor = true;
}
public String toString()
{
	return red+", "+green+", "+blue;
}
public boolean equals(Object other)
{
	if (!(other instanceof Color)) return super.equals(other);
	if (other == this) return true;
	Color c = (Color)other;
	return (c.red == red && c.blue == blue && c.green == green && c.alpha == alpha);
}
//===================================================================
public int toInt()
//===================================================================
{
	return alpha << 24 | red << 16 | green << 8 | blue;
}
//===================================================================
public void fromInt(int value)
//===================================================================
{
	set((value >> 16)&0xff,(value >> 8)&0xff,(value >> 0)&0xff,(value >> 24)&0xff);
}
//===================================================================
public Color toOpaque()
//===================================================================
{
	if (alpha == 0xff) return this;
	else return new Color(red,green,blue,0xff);
}
//===================================================================
public Color getCopy()
//===================================================================
{
	Color c = new Color(red,green,blue,alpha);
	c.nullColor = nullColor;
	return c;
}
}
