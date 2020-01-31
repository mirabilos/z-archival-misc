/* send.c:
 *
 * send an Image to an X pixmap
 *
 * jim frost 10.02.89
 *
 * Copyright 1989, 1990, 1991 Jim Frost.
 * See LICENCE file for complete legalities.
 */
#define XLIB_ILLEGAL_ACCESS

#include "../mgp.h"

static int GotError;

static int
pixmapErrorTrap(Display *disp, XErrorEvent *pErrorEvent)
{
#define MAXERRORLEN 100
    char buf[MAXERRORLEN+1];
    GotError = 1;
    XGetErrorText(disp, pErrorEvent->error_code, buf, MAXERRORLEN);
    fprintf(stderr, "serial #%d (request code %d) Got Error %s\n",
	(int) pErrorEvent->serial,
	pErrorEvent->request_code,
	buf);
    return(0);
}

Pixmap
ximageToPixmap(Display *disp, Window parent, XImageInfo *ximageinfo)
{
  int         (*old_handler)(Display *, XErrorEvent *);
  Pixmap        pixmap2;

  GotError = 0;
  old_handler = XSetErrorHandler(pixmapErrorTrap);
  XSync(disp, False);
  pixmap2= XCreatePixmap(disp, parent,
			ximageinfo->ximage->width, ximageinfo->ximage->height,
			ximageinfo->depth);
  (void)XSetErrorHandler(old_handler);
  if (GotError)
    return(None);
  ximageinfo->drawable= pixmap2;
  sendXImage(ximageinfo, 0, 0, 0, 0,
	     ximageinfo->ximage->width, ximageinfo->ximage->height);
  return(pixmap2);
}

/* find the best pixmap depth supported by the server for a particular
 * visual and return that depth.
 *
 * this is complicated by R3's lack of XListPixmapFormats so we fake it
 * by looking at the structure ourselves.
 */

static unsigned int
bitsPerPixelAtDepth(Display *disp, unsigned int depth2)
{
#if 1 /* the way things are */
  unsigned int a;

  for (a= 0; a < (unsigned int)disp->nformats; a++)
    if ((unsigned int)disp->pixmap_format[a].depth == depth2)
      return(disp->pixmap_format[a].bits_per_pixel);

#else /* the way things should be */
  XPixmapFormatValues *xf;
  unsigned int nxf, a;

  xf = XListPixmapFormats(disp, &nxf);
  for (a = 0; a < nxf; a++)
    if (xf[a].depth == closest_depth)
      return(disp->pixmap_format[a].bits_per_pixel);
#endif

  /* this should never happen; if it does, we're in trouble
   */

  fprintf(stderr, "bitsPerPixelAtDepth: Can't find pixmap depth info!\n");
  cleanup(-1);
  return (-1); /* To avoid -Wall warnings */
}

XImageInfo *
imageToXImage(Display *disp, int scrn,
    /* visual to use */ Visual *visual2,
    /* depth of the visual to use */ unsigned int ddepth,
    Image *image, unsigned int private_cmap,
    unsigned int fit, unsigned int back, unsigned int verbose2)
{ Pixel        *index2, *redvalue, *greenvalue, *bluevalue;
  unsigned int  a, b=0, newmap, x, y, linelen, dpixlen, dbits, again_flag=0;
  XColor        xcolor;
  XImageInfo   *ximageinfo;
  Image        *orig_image;

  goodImage(image, "imageToXimage");

  xcolor.flags= DoRed | DoGreen | DoBlue;
  index2= redvalue= greenvalue= bluevalue= NULL;
  orig_image= image;
  ximageinfo= (XImageInfo *)lmalloc(sizeof(XImageInfo));
  ximageinfo->disp= disp;
  ximageinfo->scrn= scrn;
  ximageinfo->depth= 0;
  ximageinfo->drawable= None;
  ximageinfo->foreground= ximageinfo->background= 0;
  ximageinfo->gc= NULL;
  ximageinfo->ximage= NULL;

  /* process image based on type of visual we're sending to
   */

  switch (image->type) {
  case ITRUE:
    switch (visual2->class) {
    case TrueColor:
    case DirectColor:
      /* goody goody */
      break;
    default:
      if (visual2->bits_per_rgb > 1)
	image= reduce(image, depthToColors(visual2->bits_per_rgb), verbose2);
      else
	image= dither(image, verbose2);
    }
    break;

  case IRGB:
    switch(visual2->class) {
    case TrueColor:
    case DirectColor:
      /* no problem, we handle this just fine */
      break;
    default:
      if (visual2->bits_per_rgb < 2)
	image= dither(image, verbose2);
      break;
    }

  case IBITMAP:
    /* no processing ever needs to be done for bitmaps */
    break;
  }

  /* do color allocation
   */

  switch (visual2->class) {
  case TrueColor:
  case DirectColor:
  if (!BITMAPP(image)) { Pixel pixval;
      unsigned int redcolors, greencolors, bluecolors;
      unsigned int redstep, greenstep, bluestep;
      unsigned int redbottom, greenbottom, bluebottom;
      unsigned int redtop=0, greentop=0, bluetop=0;

      redvalue= (Pixel *)lmalloc(sizeof(Pixel) * 256);
      greenvalue= (Pixel *)lmalloc(sizeof(Pixel) * 256);
      bluevalue= (Pixel *)lmalloc(sizeof(Pixel) * 256);

      if (visual2 == DefaultVisual(disp, scrn))
	ximageinfo->cmap= DefaultColormap(disp, scrn);
      else
	ximageinfo->cmap= XCreateColormap(disp, RootWindow(disp, scrn),
					  visual2, AllocNone);

      retry_direct: /* tag we hit if a DirectColor allocation fails on
		     * default colormap */

      /* calculate number of distinct colors in each band
       */

      redcolors= greencolors= bluecolors= 1;
      for (pixval= 1; pixval; pixval <<= 1) {
	if (pixval & visual2->red_mask)
	  redcolors <<= 1;
	if (pixval & visual2->green_mask)
	  greencolors <<= 1;
	if (pixval & visual2->blue_mask)
	  bluecolors <<= 1;
      }

      /* sanity check
       */

      if ((redcolors > (unsigned int)visual2->map_entries) ||
	  (greencolors > (unsigned int)visual2->map_entries) ||
	  (bluecolors > (unsigned int)visual2->map_entries)) {
	fprintf(stderr, "\
Warning: inconsistency in color information (this may be ugly)\n");
      }

      redstep= 256 / redcolors;
      greenstep= 256 / greencolors;
      bluestep= 256 / bluecolors;
      redbottom= greenbottom= bluebottom= 0;
      for (a= 0; a < (unsigned int)visual2->map_entries; a++) {
	if (redbottom < 256)
	  redtop= redbottom + redstep;
	if (greenbottom < 256)
	  greentop= greenbottom + greenstep;
	if (bluebottom < 256)
	  bluetop= bluebottom + bluestep;

	xcolor.red= (redtop - 1) << 8;
	xcolor.green= (greentop - 1) << 8;
	xcolor.blue= (bluetop - 1) << 8;
	if (! XAllocColor(disp, ximageinfo->cmap, &xcolor)) {

	  /* if an allocation fails for a DirectColor default visual then
	   * we should create a private colormap and try again.
	   */

	  if ((visual2->class == DirectColor) &&
	      (visual2 == DefaultVisual(disp, scrn))) {
	    ximageinfo->cmap= XCreateColormap(disp, RootWindow(disp, scrn),
					      visual2, AllocNone);
	    goto retry_direct;
	  }

	  /* something completely unexpected happened
	   */

	  fprintf(stderr, "\
imageToXImage: XAllocColor failed on a TrueColor/Directcolor visual\n");
	  return(NULL);
	}

	/* fill in pixel values for each band at this intensity
	 */

	while ((redbottom < 256) && (redbottom < redtop))
	  redvalue[redbottom++]= xcolor.pixel & visual2->red_mask;
	while ((greenbottom < 256) && (greenbottom < greentop))
	  greenvalue[greenbottom++]= xcolor.pixel & visual2->green_mask;
	while ((bluebottom < 256) && (bluebottom < bluetop))
	  bluevalue[bluebottom++]= xcolor.pixel & visual2->blue_mask;
      }
      break;
    }
    /* FALLTHROUGH */

  default:
  retry: /* this tag is used when retrying because we couldn't get a fit */
    index2= (Pixel *)lmalloc(sizeof(Pixel) * image->rgb.used);

    /* private_cmap flag is invalid if not a dynamic visual
     */

    switch (visual2->class) {
    case StaticColor:
    case StaticGray:
      private_cmap= 0;
    }

    /* get the colormap to use.
     */

    if (private_cmap) { /* user asked us to use a private cmap */
      newmap= 1;
      fit= 0;
    }
    else if ((visual2 == DefaultVisual(disp, scrn)) ||
	     (visual2->class == StaticGray) ||
	     (visual2->class == StaticColor)) {

      /* if we're using the default visual, try to alloc colors shareable.
       * otherwise we're using a static visual and should treat it
       * accordingly.
       */

      if (visual2 == DefaultVisual(disp, scrn))
	ximageinfo->cmap= DefaultColormap(disp, scrn);
      else
	ximageinfo->cmap= XCreateColormap(disp, RootWindow(disp, scrn),
					  visual2, AllocNone);
      newmap= 0;

      /* allocate colors shareable (if we can)
       */

      for (a= 0; a < image->rgb.used; a++) {
	xcolor.red= *(image->rgb.red + a);
	xcolor.green= *(image->rgb.green + a);
	xcolor.blue= *(image->rgb.blue + a);
	if (! XAllocColor(disp, ximageinfo->cmap, &xcolor)) {
	  if ((visual2->class == StaticColor) ||
	      (visual2->class == StaticGray)) {
	    fprintf(stderr, "imageToXImage: XAllocColor failed on a static visual\n");
	    return(NULL);
	  }
	  else {

	    /* we can't allocate the colors shareable so free all the colors
	     * we had allocated and create a private colormap (or fit
	     * into the default cmap if `fit' is true).
	     */

	    XFreeColors(disp, ximageinfo->cmap, index2, a, 0);
	    newmap= 1;
	    break;
	  }
	}
	*(index2 + a)= xcolor.pixel;
      }
    }
    else {
      newmap= 1;
      fit= 0;
    }

    if (newmap) {

      /* either create a new colormap or fit the image into the one we
       * have.  to create a new one, we create a private cmap and allocate
       * the colors writable.  fitting the colors is harder, we have to:
       *  1. grab the server so no one can goof with the colormap.
       *  2. count the available colors using XAllocColorCells.
       *  3. free the colors we just allocated.
       *  4. reduce the depth of the image to fit.
       *  5. allocate the colors again shareable.
       *  6. ungrab the server and continue on our way.
       * someone should shoot the people who designed X color allocation.
       */

      if (fit) {
	if (verbose2)
	  fprintf(stderr, "  Fitting image into default colormap\n");
	XGrabServer(disp);
      }
      else {
	if (verbose2)
	  fprintf(stderr, "  Using private colormap\n");

	/* create new colormap
	 */

	if (private_cmap)
		ximageinfo->cmap= colormap;
	else
		ximageinfo->cmap= XCreateColormap(disp, RootWindow(disp, scrn),
					  visual2, AllocNone);
      }

again:
      for (a= 0; a < image->rgb.used; a++) /* count entries we got */
	if (! XAllocColorCells(disp, ximageinfo->cmap, False, NULL, 0,
			       index2 + a, 1))
	  break;

      if (fit) {
	if (a > 0)
	  XFreeColors(disp, ximageinfo->cmap, index2, a, 0);
	if (a <= 2) {
	  if (verbose2) {
	    fprintf(stderr, "  Cannot fit into default colormap, dithering...");
	    fflush(stderr);
	  }
	  image= dither(image, 0);
	  if (verbose2)
	    fprintf(stderr, "done\n");
	  fit= 0;
	  lfree(index2);
	  goto retry;
	}
      }

      if (a == 0) {
	if (again_flag == 0){
		free_alloc_colors(&image_clr);
		again_flag = 1;
		goto again;
	} else {
	fprintf(stderr, "imageToXImage: Color allocation failed!\n");
	lfree(index2);
	return(NULL);
	}
      }

      if (a < image->rgb.used)
	image= reduce(image, a, verbose2);

      if (fit) {
	for (a= 0; a < image->rgb.used; a++) {
	  xcolor.red= *(image->rgb.red + a);
	  xcolor.green= *(image->rgb.green + a);
	  xcolor.blue= *(image->rgb.blue + a);

	  /* if this fails we're in trouble
	   */

	  if (! XAllocColor(disp, ximageinfo->cmap, &xcolor)) {
	    fprintf(stderr, "XAllocColor failed while fitting colormap!\n");
	    return(NULL);
	  }
	  *(index2 + a)= xcolor.pixel;
	}
	XUngrabServer(disp);
      }
      else {
	for (b = 0; b < a; b++) {
	  xcolor.pixel= *(index2 + b);
	  xcolor.red= *(image->rgb.red + b);
	  xcolor.green= *(image->rgb.green + b);
	  xcolor.blue= *(image->rgb.blue + b);
	  XStoreColor(disp, ximageinfo->cmap, &xcolor);
	}
      }

      /* regist free_clr array */
      if (private_cmap && ximageinfo->cmap == colormap) {
	if (back)
	  regist_alloc_colors(&back_clr, index2, b);
	else
	  regist_alloc_colors(&image_clr, index2, b);
      }
    }
    break;
  }

  /* create an XImage and related colormap based on the image type
   * we have.
   */

  if (verbose2) {
    fprintf(stderr, "  Building XImage...");
    fflush(stderr);
  }

  switch (image->type) {
  case IBITMAP:
    { byte *data;

      /* we copy the data to be more consistent
       */

      data= lmalloc((image->width + 7) / 8 * image->height);
      memcpy(data, image->data, ((image->width + 7) / 8) * image->height);

      ximageinfo->ximage= XCreateImage(disp, visual2, 1, XYBitmap,
				       0, (void *)data, image->width, image->height,
				       8, 0);
      ximageinfo->depth= ddepth;
      ximageinfo->foreground= *(index2 + 1);
      ximageinfo->background= *index2;
      ximageinfo->ximage->bitmap_bit_order= MSBFirst;
      ximageinfo->ximage->byte_order= MSBFirst;
      break;
    }

  case IRGB:
  case ITRUE:

    /* modify image data to match visual and colormap
     */

    dbits= bitsPerPixelAtDepth(disp, ddepth);
    ximageinfo->depth= ddepth;
    dpixlen= (dbits + 7) / 8;

    switch (visual2->class) {
    case DirectColor:
    case TrueColor:
      { byte *data, *destptr, *srcptr;
	Pixel pixval, newpixval;

	ximageinfo->ximage = XCreateImage(disp, visual2, ddepth, ZPixmap, 0,
					  NULL, image->width, image->height,
					  8, 0);
	data= lmalloc(image->width * image->height * dpixlen);
	ximageinfo->ximage->data= (char *)data;
	destptr= data;
	srcptr= image->data;
	switch (image->type) {
	case ITRUE:
	  for (y= 0; y < image->height; y++)
	    for (x= 0; x < image->width; x++) {
	      pixval= memToVal(srcptr, image->pixlen);
	      newpixval= redvalue[TRUE_RED(pixval)] |
		greenvalue[TRUE_GREEN(pixval)] | bluevalue[TRUE_BLUE(pixval)];
	      valToMem(newpixval, destptr, dpixlen);
	      srcptr += image->pixlen;
	      destptr += dpixlen;
	    }
	  break;
	case IRGB:
	  for (y= 0; y < image->height; y++)
	    for (x= 0; x < image->width; x++) {
	      pixval= memToVal(srcptr, image->pixlen);
	      pixval= redvalue[image->rgb.red[pixval] >> 8] |
		greenvalue[image->rgb.green[pixval] >> 8] |
		  bluevalue[image->rgb.blue[pixval] >> 8];
	      valToMem(pixval, destptr, dpixlen);
	      srcptr += image->pixlen;
	      destptr += dpixlen;
	    }
	  break;
	default: /* something's broken */
	  fprintf(stderr, "Unexpected image type for DirectColor/TrueColor visual!\n");
	  cleanup(-1);
	}
	ximageinfo->ximage->byte_order= MSBFirst; /* trust me, i know what
						   * i'm talking about */
	break;
      }
    default:

      /* only IRGB images make it this far.
       */

      /* if our XImage doesn't have modulus 8 bits per pixel, it's unclear
       * how to pack bits so we instead use an XYPixmap image.  this is
       * slower.
       */

      if (dbits % 8) {
	byte *data, *destdata, *destptr, *srcptr, mask;
	Pixel pixmask, pixval;

	ximageinfo->ximage = XCreateImage(disp, visual2, ddepth, XYPixmap, 0,
					  NULL, image->width, image->height,
					  8, 0);

	data= (byte *)lmalloc(image->width * image->height * dpixlen);
	ximageinfo->ximage->data= (char *)data;
	memset(data, 0, image->width * image->height * dpixlen);
	ximageinfo->ximage->bitmap_bit_order= MSBFirst;
	ximageinfo->ximage->byte_order= MSBFirst;
	linelen= (image->width + 7) / 8;
	for (a= 0; a < dbits; a++) {
	  pixmask= 1 << a;
	  destdata= data + ((dbits - a - 1) * image->height * linelen);
	  srcptr= image->data;
	  for (y= 0; y < image->height; y++) {
	    destptr= destdata + (y * linelen);
	    *destptr= 0;
	    mask= 0x80;
	    for (x= 0; x < image->width; x++) {
	      pixval= memToVal(srcptr, image->pixlen);
	      srcptr += image->pixlen;
	      if (index2[pixval] & pixmask)
		*destptr |= mask;
	      mask >>= 1;
	      if (mask == 0) {
		mask= 0x80;
		destptr++;
	      }
	    }
	  }
	}
      }
      else {
	byte *data, *srcptr, *destptr;

	ximageinfo->ximage = XCreateImage(disp, visual2, ddepth, ZPixmap, 0,
					  NULL, image->width, image->height,
					  8, 0);

	dpixlen= (ximageinfo->ximage->bits_per_pixel + 7) / 8;
	data= (byte *)lmalloc(image->width * image->height * dpixlen);
	ximageinfo->ximage->data= (char *)data;
	ximageinfo->ximage->byte_order= MSBFirst; /* trust me, i know what
						   * i'm talking about */
	srcptr= image->data;
	destptr= data;
	for (y= 0; y < image->height; y++)
	  for (x= 0; x < image->width; x++) {
	    valToMem(index2[memToVal(srcptr, image->pixlen)], destptr, dpixlen);
	    srcptr += image->pixlen;
	    destptr += dpixlen;
	  }
      }
      break;
    }
  }

  if (verbose2)
    fprintf(stderr, "done\n");

  if (index2)
    lfree((byte *)index2);
  if (redvalue) {
    lfree((byte *)redvalue);
    lfree((byte *)greenvalue);
    lfree((byte *)bluevalue);
  }
  if (image != orig_image)
    freeImage(image);
  return(ximageinfo);
}

/* Given an XImage and a drawable, move a rectangle from the Ximage
 * to the drawable.
 */

void
sendXImage(XImageInfo *ximageinfo,
    int src_x, int src_y, int dst_x, int dst_y, unsigned int w, unsigned int h)
{
  XGCValues gcv;

  /* build and cache the GC
   */

  if (!ximageinfo->gc) {
    gcv.function= GXcopy;
    if (ximageinfo->ximage->depth == 1) {
      gcv.foreground= ximageinfo->foreground;
      gcv.background= ximageinfo->background;
      ximageinfo->gc= XCreateGC(ximageinfo->disp, ximageinfo->drawable,
				GCFunction | GCForeground | GCBackground,
				&gcv);
    }
    else
      ximageinfo->gc= XCreateGC(ximageinfo->disp, ximageinfo->drawable,
				GCFunction, &gcv);
  }
  XPutImage(ximageinfo->disp, ximageinfo->drawable, ximageinfo->gc,
	    ximageinfo->ximage, src_x, src_y, dst_x, dst_y, w, h);
}

/* free up anything cached in the local Ximage structure.
 */

void
freeXImage(XImageInfo *ximageinfo)
{
  if (ximageinfo->gc)
    XFreeGC(ximageinfo->disp, ximageinfo->gc);
  lfree((byte *)ximageinfo->ximage->data);
  ximageinfo->ximage->data= NULL;
  XDestroyImage(ximageinfo->ximage);
  lfree((byte *)ximageinfo);
}
