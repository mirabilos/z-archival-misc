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
 * Font is the character font used when drawing text on a surface.
 * <p>
 * Fonts have a name, such as "Helvetica", a style and a point size (usually
 * around 10). It's important to note that many devices have an extremely
 * limited number of fonts. For example, most PalmPilot devices have only
 * two fonts: plain and bold. If the font specified can't be found during
 * drawing, the closest matching font will be used.
  */
public class Font
{
String name;
int style;
int size;

/** A plain font style. */
public static final int PLAIN  = 0;
/** A bold font style. */
public static final int BOLD   = 0x1;
/** An italic font style. */
public static final int ITALIC = 0x2;
/** An underlined font style. */
public static final int UNDERLINE = 0x4;
/** An outlined font style. */
public static final int OUTLINE = 0x8;
/** A strikethrough font style. */
public static final int STRIKETHROUGH = 0x10;
/** A superscript font style. */
public static final int SUPERSCRIPT = 0x80;
/** A subscript font style. */
public static final int SUBSCRIPT = 0x100;

/**
  * Creates a font of the given name, style and size. Font styles are defined
  * in this class.
  * @see #PLAIN
  * @see #BOLD
  * @see Graphics
  */
public Font(String name, int style, int size)
	{
	this.name = name;
	this.style = style;
	this.size = size;
	}


/** Returns the name of the font. */
public String getName()
	{
	return name;
	}

/** Returns the size of the font. */
public int getSize()
	{
	return size;
	}

/**
 * Returns the style of the font. Font styles are defined in this class.
 * @see #PLAIN
 * @see #BOLD
 */
public int getStyle()
	{
	return style;
	}
/**
 * Return a new Font with the same name and size, but a different style.
 * @param newStyle The new style for the Font.
 * @return A new Font (or the same font if the style is the same).
 */
//===================================================================
public Font changeStyle(int newStyle)
//===================================================================
{
	if (newStyle == style) return this;
	return new Font(name,newStyle,size);
}
/**
 * Change the name and/or size of a Font.
 * @param newName The newName for the Font. If it is null the old name is used.
 * @param newSize The newSize for the Font. If it is <= 0, the old size is used.
 * @return A new Font.
 */
//===================================================================
public Font changeNameAndSize(String newName,int newSize)
//===================================================================
{
	if (newName == null) newName = name;
	if (newSize <= 0) newSize = size;
	return new Font(newName,style,newSize);
}
/**
* List the names of all the available fonts for a particular ISurface.
**/
public static native String [] listFonts(ISurface surface);
public String toString()
{
	return name+":"+size;
}

//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof Font)) return super.equals(other);
	Font f = (Font)other;
	return name.equalsIgnoreCase(f.name) && size == f.size && style == f.style;
}

}

