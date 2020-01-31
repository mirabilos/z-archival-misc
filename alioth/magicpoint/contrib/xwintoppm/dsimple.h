/* $XConsortium: dsimple.h /main/6 1995/12/07 10:23:30 kaleb $ */
/*

Copyright (c) 1993  X Consortium

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE X CONSORTIUM BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of the X Consortium shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from the X Consortium.

*/
/*
 * Just_display.h: This file contains the definitions needed to use the
 *                 functions in just_display.c.  It also declares the global
 *                 variables dpy, screen, and program_name which are needed to
 *                 use just_display.c.
 *
 * Written by Mark Lillibridge.   Last updated 7/1/87
 *
 * Send bugs, etc. to chariot@athena.mit.edu.
 */

    /* Global variables used by routines in just_display.c */

#ifdef DSIMPLE_ONLY_PROTOS
extern const char *program_name;
extern Display *dpy;
extern int screen;
#else
const char *program_name = "unknown_program"; /* Name of this program */
Display *dpy;                                 /* The current display */
int screen;                                   /* The current screen */

#define INIT_NAME program_name=argv[0]        /* use this in main to setup
                                                 program_name */
#endif
extern void usage(void) __attribute__((__noreturn__));

    /* Declaritions for functions in just_display.c */

char *Malloc(size_t);
char *Realloc(char *, size_t);
char *Get_Display_Name(int *, char **);
Display *Open_Display(char *);
void Setup_Display_And_Screen(int *, char **);
XFontStruct *Open_Font(char *);
Pixmap ReadBitmapFile(char *, int *, int *, int *, int *);
void WriteBitmapFile(char *, Pixmap, int, int, int, int);
Window Select_Window_Args(int *, char **);

#define X_USAGE "[host:display]"              /* X arguments handled by
						 Get_Display_Name */
#define SELECT_USAGE "[-root|-id <id>|-name <name>]"

/*
 * Other_stuff.h: Definitions of routines in other_stuff.
 *
 * Written by Mark Lillibridge.   Last updated 7/1/87
 *
 * Send bugs, etc. to chariot@athena.mit.edu.
 */

unsigned long Resolve_Color(Window, const char *);
Pixmap Bitmap_To_Pixmap(Display *, Drawable, GC, Pixmap, int, int);
Window Select_Window(Display *);
Window Window_With_Name(Display *, Window, char *);

void outl(const char *, ...)
#if HAVE_ATTRIBUTE_FORMAT
    __attribute__((__format__(__printf__, 1, 2)))
#endif
    ;
void Fatal_Error(const char *, ...)
#if HAVE_ATTRIBUTE_FORMAT
    __attribute__((__format__(__printf__, 1, 2)))
#endif
#if HAVE_ATTRIBUTE_NORETURN
    __attribute__((__noreturn__))
#endif
    ;
