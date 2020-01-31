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
// MLB
//##################################################################
public interface UIConstants extends CellConstants, ControlConstants{
//##################################################################

/* 3D border styles */
public static final int BDR_RAISEDOUTER = 0x0001;
public static final int BDR_SUNKENOUTER = 0x0002;
public static final int BDR_RAISEDINNER = 0x0004;
public static final int BDR_SUNKENINNER = 0x0008;

public static final int BDR_OUTER       = 0x0003;
public static final int BDR_INNER       = 0x000c;

public static final int BDR_OUTLINE = 0x0010;
public static final int BDR_NOBORDER = 0x0020;
public static final int BDR_DOTTED = 0x0040;
/* Border flags */
public static final int BF_LEFT         = 0x00010000;
public static final int BF_TOP          = 0x00020000;
public static final int BF_RIGHT        = 0x00040000;
public static final int BF_BOTTOM       = 0x00080000;
public static final int BF_RECT         = (BF_LEFT | BF_TOP | BF_RIGHT | BF_BOTTOM);

public static final int BF_TOPLEFT      = (BF_TOP | BF_LEFT);
public static final int BF_TOPRIGHT     = (BF_TOP | BF_RIGHT);
public static final int BF_BOTTOMLEFT   = (BF_BOTTOM | BF_LEFT);
public static final int BF_BOTTOMRIGHT  = (BF_BOTTOM | BF_RIGHT);

public static final int BF_DIAGONAL     = 0x00100000;

// For diagonal lines, the BF_RECT flags specify the end point of the
// vector bounded by the rectangle parameter.
public static final int BF_DIAGONAL_ENDTOPRIGHT     = (BF_DIAGONAL | BF_TOP | BF_RIGHT);
public static final int BF_DIAGONAL_ENDTOPLEFT      = (BF_DIAGONAL | BF_TOP | BF_LEFT);
public static final int BF_DIAGONAL_ENDBOTTOMLEFT   = (BF_DIAGONAL | BF_BOTTOM | BF_LEFT);
public static final int BF_DIAGONAL_ENDBOTTOMRIGHT  = (BF_DIAGONAL | BF_BOTTOM | BF_RIGHT);

//
/**
* This flag instructs the border to be done exactly as specified. This is for special
* controls like buttons which have their borders drawn for then in different styles
* depending on the state of the button. BF_EXACT says to draw the border exactly
* as specified instead of the normal border.
*/
public static final int BF_SQUARE       = 0x01000000;
public static final int BF_BUTTON       = 0x02000000;
public static final int BF_EXACT        = 0x04000000;
public static final int BF_MIDDLE       = 0x08000000;  /* Fill in the middle */
public static final int BF_SOFT         = 0x10000000;  /* For softer buttons */
public static final int BF_PALM         = 0x20000000;  /* Calculate the space left over */
public static final int BF_FLAT         = 0x40000000;  /* For flat rather than 3D borders */
public static final int BF_MONO         = 0x80000000;  /* For monochrome borders */

public static final int EDGE_RAISED     = (BDR_RAISEDOUTER | BDR_RAISEDINNER)|BF_RECT;
public static final int EDGE_SUNKEN     = (BDR_SUNKENOUTER | BDR_SUNKENINNER)|BF_RECT;
public static final int EDGE_ETCHED     = (BDR_SUNKENOUTER | BDR_RAISEDINNER)|BF_RECT;
public static final int EDGE_BUMP       = (BDR_RAISEDOUTER | BDR_SUNKENINNER)|BF_RECT;

//##################################################################
}
//##################################################################

