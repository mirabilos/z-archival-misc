/*
 * Copyright (C) 1997 and 1998 WIDE Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

/* $XConsortium: xwd.c /main/64 1996/01/14 16:53:13 kaleb $ */
/* $XFree86: xc/programs/xwd/xwd.c,v 3.2 1996/07/08 10:37:37 dawes Exp $ */

/*

Copyright (c) 1987  X Consortium

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
X CONSORTIUM BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of the X Consortium shall not be
used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from the X Consortium.

*/

/*
 * xwd.c MIT Project Athena, X Window system window raster image dumper.
 *
 * This program will dump a raster image of the contents of a window into a
 * file for output on graphics printers or for other uses.
 *
 *  Author:	Tony Della Fera, DEC
 *		17-Jun-85
 *
 *  Modification history:
 *
 *  11/14/86 Bill Wyatt, Smithsonian Astrophysical Observatory
 *    - Removed Z format option, changing it to an XY option. Monochrome
 *      windows will always dump in XY format. Color windows will dump
 *      in Z format by default, but can be dumped in XY format with the
 *      -xy option.
 *
 *  11/18/86 Bill Wyatt
 *    - VERSION 6 is same as version 5 for monchrome. For colors, the
 *      appropriate number of Color structs are dumped after the header,
 *      which has the number of colors (=0 for monochrome) in place of the
 *      V5 padding at the end. Up to 16-bit displays are supported. I
 *      don't yet know how 24- to 32-bit displays will be handled under
 *      the Version 11 protocol.
 *
 *  6/15/87 David Krikorian, MIT Project Athena
 *    - VERSION 7 runs under the X Version 11 servers, while the previous
 *      versions of xwd were are for X Version 10.  This version is based
 *      on xwd version 6, and should eventually have the same color
 *      abilities. (Xwd V7 has yet to be tested on a color machine, so
 *      all color-related code is commented out until color support
 *      becomes practical.)
 */

/*%
 *%    This is the format for commenting out color-related code until
 *%  color can be supported.
%*/

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <X11/Xos.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>

#include <X11/Xmu/WinUtil.h>
typedef unsigned long Pixel;
#include "X11/XWDFile.h"

#define FEEP_VOLUME 0

/* Include routines to do parsing */
#include "dsimple.h"
#include "list.h"
#include "wsutils.h"
#include "multiVis.h"

#ifdef XKB
#include <X11/extensions/XKBbells.h>
#endif

static void Error(const char *) __attribute__((__noreturn__));
static void Window_Dump(Window, FILE *);
int Image_Size(XImage *);
int Get_XColors(XWindowAttributes *, XColor **);

#if 0
static void _swapshort(register char *, register unsigned int);
static void _swaplong(register char *, register unsigned int);
#endif

/* Setable Options */

int format = ZPixmap;
Bool nobdrs = False;
Bool on_root = False;
Bool standard_out = True;
Bool debug = False;
Bool silent = False;
Bool use_installed = False;
long add_pixel_value = 0;

static long
parse_long(char *s)
{
    const char *fmt = "%lu";
    long retval = 0L;
    int thesign = 1;

    if (s && s[0]) {
	if (s[0] == '-') s++, thesign = -1;
	if (s[0] == '0') s++, fmt = "%lo";
	if (s[0] == 'x' || s[0] == 'X') s++, fmt = "%lx";
	(void) sscanf (s, fmt, &retval);
    }
    return (thesign * retval);
}

int
main(int argc, char *argv[])
{
    register int i;
    Window target_win;
    FILE *out_file = stdout;

    INIT_NAME;

    Setup_Display_And_Screen(&argc, argv);

    /* Get window select on command line, if any */
    target_win = Select_Window_Args(&argc, argv);

    for (i = 1; i < argc; i++) {
	if (!strcmp(argv[i], "-nobdrs")) {
	    nobdrs = True;
	    continue;
	}
	if (!strcmp(argv[i], "-debug")) {
	    debug = True;
	    continue;
	}
	if (!strcmp(argv[i], "-help"))
	  usage();
	if (!strcmp(argv[i], "-out")) {
	    if (++i >= argc) usage();
	    if (!(out_file = fopen(argv[i], "wb")))
	      Error("Can't open output file as specified.");
	    standard_out = False;
	    continue;
	}
	if (!strcmp(argv[i], "-xy")) {
	    format = XYPixmap;
	    continue;
	}
	if (!strcmp(argv[i], "-screen")) {
	    on_root = True;
	    continue;
	}
	if (!strcmp(argv[i], "-icmap")) {
	    use_installed = True;
	    continue;
	}
	if (!strcmp(argv[i], "-add")) {
	    if (++i >= argc) usage();
	    add_pixel_value = parse_long (argv[i]);
	    continue;
	}
	if (!strcmp(argv[i], "-frame")) {
	    continue;
	}
	if (!strcmp(argv[i], "-silent")) {
	    silent = True;
	    continue;
	}
	usage();
    }
#ifdef WIN32
    if (standard_out)
	_setmode(fileno(out_file), _O_BINARY);
#endif

    /*
     * Let the user select the target window.
     */
    if (!target_win) {
	fprintf(stderr, "window must be specified\n");
	exit(1);
    }

    /*
     * Dump it!
     */
    Window_Dump(target_win, out_file);

    XCloseDisplay(dpy);
    if (fclose(out_file)) {
	perror("xwintoppm");
	exit(1);
    }
    exit(0);
}

static int
Get24bitDirectColors(XColor **colors)
{
    int i , ncolors = 256 ;
    XColor *tcol ;

    *colors = tcol = (XColor *)malloc(sizeof(XColor) * ncolors) ;

    for(i=0 ; i < ncolors ; i++)
    {
	tcol[i].pixel = i << 16 | i << 8 | i ;
	tcol[i].red = tcol[i].green = tcol[i].blue = i << 8   | i ;
    }

    return ncolors ;
}

/*
 * Window_Dump: dump a window to a file which must already be open for
 *              writting.
 */

void
Window_Dump(Window window, FILE *outf)
{
    XColor *colors;
    unsigned buffer_size;
#if 0
    int win_name_size;
    int header_size;
#endif
    int ncolors;
    char *win_name;
    Bool got_win_name;
    XWindowAttributes win_info;
    XImage *image;
    int absx, absy, x, y;
    unsigned width, height;
    int dwidth, dheight;
    int bw;
    Window dummywin;

    int                 transparentOverlays , multiVis;
    int                 numVisuals;
    XVisualInfo         *pVisuals;
    int                 numOverlayVisuals;
    OverlayInfo         *pOverlayVisuals;
    int                 numImageVisuals;
    XVisualInfo         **pImageVisuals;
    list_ptr            vis_regions;    /* list of regions to read from */
    list_ptr            vis_image_regions ;
    Visual		vis_h;
/*  Visual		*vis; */
    int			allImage = 0 ;

    /*
     * Inform the user not to alter the screen.
     */
    if (!silent) {
#ifdef XKB
	XkbStdBell(dpy,None,50,XkbBI_Wait);
#else
	XBell(dpy,FEEP_VOLUME);
#endif
	XFlush(dpy);
    }

    /*
     * Get the parameters of the window being dumped.
     */
    if (debug) outl("xwintoppm: Getting target window information.\n");
    if(!XGetWindowAttributes(dpy, window, &win_info))
      Fatal_Error("Can't get target window attributes.");

    /* handle any frame window */
    if (!XTranslateCoordinates (dpy, window, RootWindow (dpy, screen), 0, 0,
				&absx, &absy, &dummywin)) {
	fprintf (stderr,
		 "%s:  unable to translate window coordinates (%d,%d)\n",
		 program_name, absx, absy);
	exit (1);
    }
    win_info.x = absx;
    win_info.y = absy;
    width = win_info.width;
    height = win_info.height;
    bw = 0;

    if (!nobdrs) {
	absx -= win_info.border_width;
	absy -= win_info.border_width;
	bw = win_info.border_width;
	width += (2 * bw);
	height += (2 * bw);
    }
    dwidth = DisplayWidth (dpy, screen);
    dheight = DisplayHeight (dpy, screen);

    /* clip to window */
    if (absx < 0) width += absx, absx = 0;
    if (absy < 0) height += absy, absy = 0;
    if ((unsigned int)absx + width > (unsigned int)dwidth)
	width = dwidth - absx;
    if ((unsigned int)absy + height > (unsigned int)dheight)
	height = dheight - absy;

    XFetchName(dpy, window, &win_name);
    if (!win_name || !win_name[0]) {
	win_name = strdup("xwdump");
	got_win_name = False;
    } else {
	got_win_name = True;
    }

#if 0
    /* sizeof(char) is included for the null string terminator. */
    win_name_size = strlen(win_name) + sizeof(char);
#endif

    /*
     * Snarf the pixmap with XGetImage.
     */

    x = absx - win_info.x;
    y = absy - win_info.y;

    multiVis = GetMultiVisualRegions(dpy,RootWindow(dpy, screen),
               absx, absy,
	       width, height,&transparentOverlays,&numVisuals, &pVisuals,
               &numOverlayVisuals,&pOverlayVisuals,&numImageVisuals,
               &pImageVisuals,&vis_regions,&vis_image_regions,&allImage) ;
    if (on_root || multiVis)
    {
	if(!multiVis)
	    image = XGetImage (dpy, RootWindow(dpy, screen), absx, absy,
                    width, height, AllPlanes, format);
	else
	    image = ReadAreaToImage(dpy, absx, absy,
                width, height,
		pVisuals, numOverlayVisuals, pOverlayVisuals,
                pImageVisuals, vis_regions,
                vis_image_regions, format, allImage);
    }
    else
	image = XGetImage (dpy, window, x, y, width, height, AllPlanes, format);
    if (!image) {
	fprintf (stderr, "%s:  unable to get image at %dx%d+%d+%d\n",
		 program_name, width, height, x, y);
	exit (1);
    }

    if (add_pixel_value != 0) XAddPixel (image, add_pixel_value);

    /*
     * Determine the pixmap size.
     */
    buffer_size = Image_Size(image);

    if (debug) outl("xwintoppm: Getting Colors.\n");

    if( !multiVis)
    {
       ncolors = Get_XColors(&win_info, &colors);
       /* vis = win_info.visual ; */
    }
    else
    {
       ncolors = Get24bitDirectColors(&colors) ;
       initFakeVisual(&vis_h) ;
       /* vis = &vis_h ; */
    }
    /*
     * Inform the user that the image has been retrieved.
     */
    if (!silent) {
#ifdef XKB
	XkbStdBell(dpy,window,FEEP_VOLUME,XkbBI_Proceed);
	XkbStdBell(dpy,window,FEEP_VOLUME,XkbBI_RepeatingLastBell);
#else
	XBell(dpy, FEEP_VOLUME);
	XBell(dpy, FEEP_VOLUME);
#endif
	XFlush(dpy);
    }

#if 0
    /*
     * Calculate header size.
     */
    if (debug) outl("xwintoppm: Calculating header size.\n");
    header_size = SIZEOF(XWDheader) + win_name_size;

    /*
     * Write out header information.
     */
    if (debug) outl("xwintoppm: Constructing and dumping file header.\n");
    header.header_size = (CARD32) header_size;
    header.file_version = (CARD32) XWD_FILE_VERSION;
    header.pixmap_format = (CARD32) format;
    header.pixmap_depth = (CARD32) image->depth;
    header.pixmap_width = (CARD32) image->width;
    header.pixmap_height = (CARD32) image->height;
    header.xoffset = (CARD32) image->xoffset;
    header.byte_order = (CARD32) image->byte_order;
    header.bitmap_unit = (CARD32) image->bitmap_unit;
    header.bitmap_bit_order = (CARD32) image->bitmap_bit_order;
    header.bitmap_pad = (CARD32) image->bitmap_pad;
    header.bits_per_pixel = (CARD32) image->bits_per_pixel;
    header.bytes_per_line = (CARD32) image->bytes_per_line;
    /****
    header.visual_class = (CARD32) win_info.visual->class;
    header.red_mask = (CARD32) win_info.visual->red_mask;
    header.green_mask = (CARD32) win_info.visual->green_mask;
    header.blue_mask = (CARD32) win_info.visual->blue_mask;
    header.bits_per_rgb = (CARD32) win_info.visual->bits_per_rgb;
    header.colormap_entries = (CARD32) win_info.visual->map_entries;
    *****/
    header.visual_class = (CARD32) vis->class;
    header.red_mask = (CARD32) vis->red_mask;
    header.green_mask = (CARD32) vis->green_mask;
    header.blue_mask = (CARD32) vis->blue_mask;
    header.bits_per_rgb = (CARD32) vis->bits_per_rgb;
    header.colormap_entries = (CARD32) vis->map_entries;

    header.ncolors = ncolors;
    header.window_width = (CARD32) win_info.width;
    header.window_height = (CARD32) win_info.height;
    header.window_x = absx;
    header.window_y = absy;
    header.window_bdrwidth = (CARD32) win_info.border_width;

    if (*(char *) &swaptest) {
	_swaplong((char *) &header, sizeof(header));
	for (i = 0; i < ncolors; i++) {
	    _swaplong((char *) &colors[i].pixel, sizeof(long));
	    _swapshort((char *) &colors[i].red, 3 * sizeof(short));
	}
    }

    if (fwrite((char *)&header, SIZEOF(XWDheader), 1, outf) != 1 ||
	fwrite(win_name, win_name_size, 1, outf) != 1) {
	perror("xwintoppm");
	exit(1);
    }
#else
    fprintf(outf, "P6\n%d %d %d\n", win_info.width, win_info.height, 255);
#endif

#if 0
    /*
     * Write out the color maps, if any
     */

    if (debug) outl("xwintoppm: Dumping %d colors.\n", ncolors);
    for (i = 0; i < ncolors; i++) {
	xwdcolor.pixel = colors[i].pixel;
	xwdcolor.red = colors[i].red;
	xwdcolor.green = colors[i].green;
	xwdcolor.blue = colors[i].blue;
	xwdcolor.flags = colors[i].flags;
	if (fwrite((char *) &xwdcolor, SIZEOF(XWDColor), 1, outf) != 1) {
	    perror("xwd");
	    exit(1);
	}
    }
#endif

    /*
     * Write out the buffer.
     */
    if (debug) outl("xwintoppm: Dumping pixmap.  bufsize=%d\n",buffer_size);

#if 0
    /*
     *    This copying of the bit stream (data) to a file is to be replaced
     *  by an Xlib call which hasn't been written yet.  It is not clear
     *  what other functions of xwd will be taken over by this (as yet)
     *  non-existant X function.
     */
    if (fwrite(image->data, (int) buffer_size, 1, outf) != 1) {
	perror("xwd");
	exit(1);
    }
#else
  {
    int x2, y2;
    int i;
    unsigned long pixel;
    unsigned char buf[3];
    unsigned long mask[3];
    unsigned int shift0[3], shift8[3];

    mask[0] = image->red_mask;
    mask[1] = image->green_mask;
    mask[2] = image->blue_mask;
    if (!mask[0] || !mask[1] || !mask[2]) {
	fprintf(stderr, "unsupported screen depth; bailing out\n");
	exit(1);
    }
    for (i = 0; i < 3; i++) {
	shift0[i] = 0;
	while (!(mask[i] & (1 << shift0[i])))
	    shift0[i]++;
	shift8[i] = shift0[i];
	while (mask[i] & (1 << shift8[i]))
	    shift8[i]++;
	shift8[i] = 8 - (shift8[i] - shift0[i]);
    }
    for (y2 = 0; y2 < win_info.height; y2++) {
	for (x2 = 0; x2 < win_info.width; x2++) {
	    pixel = (*image->f.get_pixel)(image, x2, y2);
#if 0
	    if (ncolors && pixel < ncolors) {
		buf[0] = colors[pixel].red;
		buf[1] = colors[pixel].green;
		buf[2] = colors[pixel].blue;
	    } else
#endif
	    {
		buf[0] = (pixel & mask[0]) >> shift0[0] << shift8[0];
		buf[1] = (pixel & mask[1]) >> shift0[1] << shift8[1];
		buf[2] = (pixel & mask[2]) >> shift0[2] << shift8[2];
	    }
	    fwrite(buf, 3, 1, outf);
	}
    }
  }
#endif

    /*
     * free the color buffer.
     */

    if(debug && ncolors > 0) outl("xwd: Freeing colors.\n");
    if(ncolors > 0) free(colors);

    /*
     * Free window name string.
     */
    if (debug) outl("xwd: Freeing window name string.\n");
    if (got_win_name) XFree(win_name); else free(win_name);

    /*
     * Free image
     */
    XDestroyImage(image);
}

/*
 * Report the syntax for calling xwd.
 */
void
usage(void)
{
    fprintf (stderr,
"usage: %s [-display host:dpy] [-debug] [-help] %s [-nobdrs] [-out <file>]",
	   program_name, SELECT_USAGE);
    fprintf (stderr, " [-xy] [-add value] [-frame] [-icmap] [-screen] [-silent]\n");
    exit(1);
}

/*
 * Error - Fatal xwd error.
 */

static void
Error(const char *string)
{
	outl("\nxwd: Error => %s\n", string);
	if (errno != 0) {
		perror("xwd");
		outl("\n");
	}

	exit(1);
}

/*
 * Determine the pixmap size.
 */

int
Image_Size(XImage *image)
{
    if (image->format != ZPixmap)
      return(image->bytes_per_line * image->height * image->depth);

    return(image->bytes_per_line * image->height);
}

#define lowbit(x) ((x) & (~(x) + 1))

static int
ReadColors(Visual *vis, Colormap cmap, XColor **colors)
{
    int i,ncolors ;

    ncolors = vis->map_entries;

    if (!(*colors = (XColor *) malloc (sizeof(XColor) * ncolors)))
      Fatal_Error("Out of memory!");

    if (vis->class == DirectColor ||
	vis->class == TrueColor) {
	Pixel red, green, blue, red1, green1, blue1;

	red = green = blue = 0;
	red1 = lowbit(vis->red_mask);
	green1 = lowbit(vis->green_mask);
	blue1 = lowbit(vis->blue_mask);
	for (i=0; i<ncolors; i++) {
	  (*colors)[i].pixel = red|green|blue;
	  (*colors)[i].pad = 0;
	  red += red1;
	  if (red > vis->red_mask)
	    red = 0;
	  green += green1;
	  if (green > vis->green_mask)
	    green = 0;
	  blue += blue1;
	  if (blue > vis->blue_mask)
	    blue = 0;
	}
    } else {
	for (i=0; i<ncolors; i++) {
	  (*colors)[i].pixel = i;
	  (*colors)[i].pad = 0;
	}
    }

    XQueryColors(dpy, cmap, *colors, ncolors);

    return(ncolors);
}

/*
 * Get the XColors of all pixels in image - returns # of colors
 */
int
Get_XColors(XWindowAttributes *win_info, XColor **colors)
{
    int i, ncolors;
    Colormap cmap = win_info->colormap;

    if (use_installed)
	/* assume the visual will be OK ... */
	cmap = XListInstalledColormaps(dpy, win_info->root, &i)[0];
    if (!cmap)
	return(0);
    ncolors = ReadColors(win_info->visual,cmap,colors) ;
    return ncolors ;
}

#if 0
static void
_swapshort(register char *bp, register unsigned int n)
{
    register char c;
    register char *ep = bp + n;

    while (bp < ep) {
	c = *bp;
	*bp = *(bp + 1);
	bp++;
	*bp++ = c;
    }
}

static void
_swaplong(register char *bp, register unsigned int n)
{
    register char c;
    register char *ep = bp + n;
    register char *sp;

    while (bp < ep) {
	sp = bp + 3;
	c = *sp;
	*sp = *bp;
	*bp++ = c;
	sp = bp + 1;
	c = *sp;
	*sp = *bp;
	*bp++ = c;
	bp += 2;
    }
}
#endif
