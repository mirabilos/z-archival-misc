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

import ewe.util.*;
import ewe.io.*;
import ewe.zip.*;

//##################################################################
class pngSpecs{
//##################################################################

int width; //Must be first.
int height; //Must be second.
int type; //Must be third.
int bitDepth; //Must be fourth.
int compression; //Must be fifth.
int filter; //Must be sixth.
int interlace; //Must be 7th.
byte [] paletteBytes; //Must be 8th.
byte [] transparencyBytes; //Must be 9th.
int transparentColor; //Must be 10th.
int transparentColorLow; //Must be 11th.
int pause;
int x;
int y;

pngSpecs(ImageCodec codec)
{
	width = codec.width;
	height = codec.height;
	type = codec.type;
	bitDepth = codec.bitDepth;
	compression= codec.compression;
	filter= codec.filter;
	interlace= codec.interlace;
	paletteBytes= codec.paletteBytes;
	transparencyBytes= codec.transparencyBytes;
	transparentColor= codec.transparentColor;
	transparentColorLow= codec.transparentColorLow;
}
//##################################################################
}
//##################################################################

