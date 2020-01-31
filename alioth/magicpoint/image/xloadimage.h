/* xloadimage.h:
 *
 * jim frost 06.21.89
 *
 * Copyright 1989 Jim Frost.
 * See LICENCE file for complete legalities.
 */

#include <sys/param.h>
#include <stdio.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>

#include "image.h"

#include <Imlib2.h>

/* This struct holds the X-client side bits for a rendered image.
 */

typedef struct {
  Display  *disp;       /* destination display */
  int       scrn;       /* destination screen */
  int       depth;      /* depth of drawable we want/have */
  Drawable  drawable;   /* drawable to send image to */
  Pixel     foreground; /* foreground and background pixels for mono images */
  Pixel     background;
  Colormap  cmap;       /* colormap used for image */
  GC        gc;         /* cached gc for sending image */
  XImage   *ximage;     /* ximage structure */
} XImageInfo;

#ifndef MAXIMAGES
#define MAXIMAGES BUFSIZ /* max # of images we'll try to load at once */
#endif

/* function declarations
 */

/* path.c */
char *expandPath(char *);
int findImage(const char *, char *)
#if HAVE_ATTRIBUTE_BOUNDED
    __attribute__((__bounded__(__minbytes__, 2, MAXPATHLEN)))
#endif
    ;
void loadPathsAndExts(void);
extern unsigned int NumPaths;
extern char *Paths[BUFSIZ];

/* send.c */
void        sendXImage(XImageInfo *, int, int, int, int,
    unsigned int, unsigned int);
XImageInfo *imageToXImage(Display *, int, Visual *, unsigned int,
    Image *, unsigned int, unsigned int, unsigned int, unsigned int);
Pixmap      ximageToPixmap(Display *, Window, XImageInfo *);
void        freeXImage(XImageInfo *);

/* imlib_loader.c */
Pixmap pixmap_fromimimage(Imlib_Image *, int, int, Window);
void manage_pixmap(Pixmap, int, int);
Imlib_Image *search_imdata(const char *);
