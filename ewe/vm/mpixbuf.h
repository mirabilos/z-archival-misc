/* $MirOS: contrib/hosted/ewe/vm/mpixbuf.h,v 1.2 2007/08/30 21:30:10 tg Exp $ */

#ifndef MPIXBUF_DEF
#define MPIXBUF_DEF

#include <gtk/gtk.h>
#include <gdk/gdk.h>

//###################################################
struct mPixbuf {
//###################################################


int hasAlpha;
gchar *pixels;
int rowstride;
int width;
int height;
/*
mPixbuf()
{
	hasAlpha = false;
	pixels = NULL;
	rowstride = 0;
}

~mPixbuf()
{
	if (pixels) delete pixels;
	pixels = NULL;
}
*/
//###################################################
};
typedef struct mPixbuf *MPixbuf;
//###################################################
#ifdef __cplusplus
extern "C" {
#endif
	extern struct mPixbuf *getPixbufFromDrawable(GdkDrawable *src,GdkColormap *cmap,int x,int y,int width,int height);
	extern void destroyPixbuf(struct mPixbuf *pixbuf);
#ifdef __cplusplus
}
#endif

#endif
