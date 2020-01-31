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

#include "mgp.h"

/* covered by gcconf */
GC gcfore;
GC gcpen;
GC gcred;
GC gcgreen;
GC gcyellow;

/* not covered by gcconf */
GC gc_pl;
GC gc_plrev;
GC gc_pta;
GC gc_ptk;

/* for caching */
GC gc_cache;

long xeventmask = EVENT_DEFAULT | EVENT_RAKUGAKI;

static u_long zero = 0;

static struct gcconf {
	GC *gc;
	u_long *fore;
	u_long *back;
	const char *color;
	int func;
} gcconf[]  = {
	{ &gcfore,	&fore_color[0],	&zero,	NULL,		GXcopy },
	{ &gcpen,	NULL,		NULL,	"red",		GXcopy },
	{ &gcred,	NULL,		NULL,	"red",		GXcopy },
	{ &gcgreen,	NULL,		NULL,	"green",	GXcopy },
	{ &gcyellow,	NULL,		NULL,	"yellow",	GXcopy },
	{ NULL,		NULL,		NULL,	NULL,		0 }
};

static XWindowAttributes xa;

int window_x;
int window_y;

static Visual *
get_visual(Display *dpy, int scrn, u_int *depthp)
{
	XVisualInfo *best, *vinfo, vinfo_template;
	Visual *ret;
	int vinfo_mask, i, ninfo;

	vinfo_template.screen = scrn;
	vinfo_mask = VisualScreenMask;

	vinfo = XGetVisualInfo(dpy, vinfo_mask, &vinfo_template, &ninfo);
	best = NULL;
	for (i = 0; i < ninfo; i++) {
		switch (vinfo[i].class) {
		case TrueColor:
			if (vinfo[i].depth < 15 || 24 < vinfo[i].depth)
				break;
			if (best == NULL ||
			    best->class != TrueColor ||
			    best->depth < vinfo[i].depth)
				best = &vinfo[i];
			break;
		case PseudoColor:
			if (best == NULL)
				best = &vinfo[i];
			break;
		}
	}
	if (best) {
		ret = best->visual;
		*depthp = best->depth;
	} else {
		ret = DefaultVisual(dpy, scrn);
		*depthp = DefaultDepth(dpy, scrn);
	}
	XFree(vinfo);
	return ret;
}

void
init_win1(char *geometry)
{
	int xloc, yloc;
	unsigned int xsiz, ysiz;
	int mode;
	u_int i;

	if ((display = XOpenDisplay(NULL)) == NULL) {
		fprintf(stderr, "Can't open display\n");
		exit(-1);
	}
	screen = DefaultScreen(display);
	visual = get_visual(display, screen, &depth);
	depth_mask = 1;
	for (i = depth; i; i--)
		depth_mask *= 2;
	depth_mask--;
	/* depth_mask is the max value of pixel */

	XGetWindowAttributes(display, DefaultRootWindow(display), &xa);

	/* determine geometry to use. */
	window_width = window_height = -1;
	window_x = window_y = -1;
	if (geometry) {
		mode = XParseGeometry(geometry, &xloc, &yloc,
			&xsiz, &ysiz);
		if (mode == 0) {
			fprintf(stderr, "bad geometry string %s\n",
				geometry);
			exit(-1);
		}
		if (!(mode & WidthValue))
			xsiz = 0;
		if (!(mode & HeightValue))
			ysiz = 0;
		if (!(mode & XValue))
			xloc = -1;
		if (!(mode & YValue))
			yloc = -1;
	} else {
		xsiz = ysiz = 0;
		xloc = yloc = -1;
		mode = 0;
	}

	if (xsiz == 0) {
		window_width = ((mgp_flag & FL_OVER) && !(mgp_flag & FL_NODECORATION))
			? (int)(xa.width * 96 / 100)
			: xa.width;
	} else
		window_width = xsiz;
	if (ysiz == 0) {
		window_height = ((mgp_flag & FL_OVER) && !(mgp_flag & FL_NODECORATION))
			? (int)(xa.height * 96 / 100)
			: xa.height;
	} else
		window_height = ysiz;

	if (0 <= xloc) {
		window_x = (mode & XNegative)
			? xa.width - window_width - xloc
			: xloc;
	}
	if (0 <= yloc) {
		window_y = (mode & YNegative)
			? xa.height - window_height - yloc
			: yloc;
	}
#if 0
	if (window_x < 0)
		window_x = 0;	/*XXX*/
	if (window_y < 0)
		window_y = 0;	/*XXX*/
#endif
	if (visual != DefaultVisual(display, screen))
		colormap = XCreateColormap(display, RootWindow(display, screen), visual, AllocNone);
	else {
		colormap = DefaultColormap(display, screen);
		if (mgp_flag & FL_PRIVATE)
			colormap = XCopyColormapAndFree(display, colormap);
	}

	char_size[0] = window_height * DEFAULT_CHARSIZE / 100;
	nonscaled_size[0] = char_size[0];
	sup_off = DEFAULT_SUPOFF;
	sub_off = DEFAULT_SUBOFF;
	sup_scale = DEFAULT_SUPSCALE;
	(void)get_color(DEFAULT_FORE, &fore_color[0]);
	ctrl_color[0] = fore_color[0];

	(void)get_color(back_clname, &back_color[0]);
}

static const char res_mgp[] = "MagicPoint";
static char res_name[sizeof(res_mgp)];
static char res_class[sizeof(res_mgp)];
void
init_win2(void)
{
	XSetWindowAttributes xsa;
	XSizeHints hints;
	u_long mask;
	struct gcconf *pc;
	u_long color;
	XClassHint res = { res_name, res_class };

	memcpy(res_name, res_mgp, sizeof(res_mgp));
	memcpy(res_class, res_mgp, sizeof(res_mgp));
	xsa.border_pixel = fore_color[0];
	xsa.background_pixel = back_color[0];
	xsa.backing_store = Always;
	xsa.colormap = colormap;
	mask = CWBorderPixel | CWBackPixel | CWBackingStore | CWColormap;

#if 1	// this method is old. we should use WM_STATE_FULLSCREEN.
	if (!(mgp_flag & FL_OVER)) {
		xsa.override_redirect = True;
		mask |= CWOverrideRedirect;
	}
#endif

	window = XCreateWindow(display, RootWindow(display, screen),
		(window_x < 0) ? 0 : window_x,
		(window_y < 0) ? 0 : window_y,
		(window_width < 0) ? 0 : window_width,
		(window_height < 0) ? 0 : window_height,
		0, depth, InputOutput, visual, mask, &xsa);
	XStoreName(display, window, mgp_wname);
	XSetIconName(display, window, mgp_wname);
	XSetClassHint(display, window, &res);
	pixmap = XCreatePixmap(display, window, xa.width, xa.height, depth);
	maskpix = XCreatePixmap(display, window, xa.width, xa.height, depth);
	cachewin = XCreatePixmap(display, window, xa.width, xa.height, depth);
	cachetmp = XCreatePixmap(display, window, xa.width, xa.height, depth);
	gc_cache = XCreateGC(display, window, 0, 0);
	XSetGraphicsExposures(display, gc_cache, False);

	for (pc = &gcconf[0]; pc->gc; pc++) {
		*pc->gc = XCreateGC(display, window, 0, 0);
		XSetFunction(display, *pc->gc, pc->func);
		if (pc->fore)
			XSetForeground(display, *pc->gc, *pc->fore);
		if (pc->back)
			XSetBackground(display, *pc->gc, *pc->back);
		if (pc->color) {
			(void)get_color(pc->color, &color);
			XSetForeground(display, *pc->gc, color);
		}
	}

	if (mgp_flag & FL_OVER){
		if (0 <= window_x && 0 <= window_y) {
			hints.x = window_x;
			hints.y = window_y;
			hints.flags = USPosition;
			XSetNormalHints(display, window, &hints);
		}
	}
	if (mgp_flag & FL_NODECORATION)
		XSetTransientForHint(display, window, window);

	XMapRaised(display, window);

	gc_pl = XCreateGC(display, window, 0, 0);
	gc_plrev = XCreateGC(display, window, 0, 0);
	gc_pta = XCreateGC(display, window, 0, 0);
	gc_ptk = XCreateGC(display, window, 0, 0);
	plfs = XLoadQueryFont(display, PAGELIST_FONT);
	plkfs = XLoadQueryFont(display, PAGELIST_KFONT);
	if (plfs) {
		XSetFont(display, gc_pl, plfs->fid);
		XSetFont(display, gc_plrev, plfs->fid);
		XSetFont(display, gc_pta, plfs->fid);
	} else
		fprintf(stderr,
		"cannot find %s font. please modify PAGELIST_FONT to display page guide.\n",
				PAGELIST_FONT);

	if (plkfs)
		XSetFont(display, gc_ptk, plkfs->fid);
	else
		fprintf(stderr, "cannot find %s font.\n", PAGELIST_KFONT);

	XSetFunction(display, gc_pl, GXcopy);
	XSetFunction(display, gc_plrev, GXcopy);
	XSetFunction(display, gc_pta, GXcopy);
	XSetFunction(display, gc_ptk, GXcopy);
	XSetForeground(display, gc_pl, BlackPixel(display, screen));
	XSetBackground(display, gc_pl, WhitePixel(display, screen));
	XSetForeground(display, gc_plrev, WhitePixel(display, screen));
	XSetBackground(display, gc_plrev, BlackPixel(display, screen));
	if (plfs) {
		pl_fh = (plfs->max_bounds.ascent + plfs->max_bounds.descent) * 1.2;
		pl_fw = plfs->max_bounds.rbearing + plfs->max_bounds.lbearing;
		if (!plkfs) plkfs = plfs;
	}
}

void
init_win3(void)
{
	Window dummy;
	int revert;

	XSelectInput(display, window, xeventmask);

	if (!(mgp_flag & FL_OVER)) {
		XGetInputFocus(display, &dummy, &revert);
		XSetInputFocus(display, window, RevertToParent, CurrentTime);
		if (XGrabKeyboard(display, window, True,
			GrabModeAsync, GrabModeAsync, CurrentTime)
				!= GrabSuccess) {
			fprintf(stderr, "XGrabKeyboard failed. sorry \n");
			exit(-1);
		}
		if (XGrabPointer(display, window, True, ButtonPressMask,
			GrabModeAsync, GrabModeAsync, window, None, CurrentTime)
				!= GrabSuccess) {
			fprintf(stderr, "XGrabPointer failed. sorry \n");
			exit(-1);
		}
	}

	// do fullscreen
	if (!(mgp_flag & FL_OVER)) {
		toggle_fullscreen();
	}
}

void
toggle_fullscreen(void)
{
	static int fullscreen = 0;
	XClientMessageEvent  xev;

	fullscreen = (!fullscreen) & 1;
	memset(&xev, 0, sizeof(xev));
	xev.type=ClientMessage;
	xev.message_type=XInternAtom(display, "_NET_WM_STATE", False);
	xev.display=display;
	xev.window=window;
	xev.format=32;
	xev.data.l[0]=fullscreen;
	xev.data.l[1]= XInternAtom(display, "_NET_WM_STATE_FULLSCREEN", False);
	xev.data.l[2]=0;
	XSendEvent(display, DefaultRootWindow(display), False,
	SubstructureRedirectMask, (XEvent*)&xev);
}

void
finish_win(void)
{
	XUngrabKeyboard(display, CurrentTime);
	XUngrabPointer(display, CurrentTime);
	XCloseDisplay(display);
}

int
get_color(const char *colorname, u_long *value)
{
	XColor c0, c1;

	screen = DefaultScreen(display);
/*XXX*/
	if (strcasecmp(colorname, "darkblue") == 0)
		colorname = "#00008B";
	if (XAllocNamedColor(display, colormap, colorname, &c1, &c0) == 0)
		return -1;
	if (value)
		*value = c1.pixel;
	return 0;
}

struct g_color *
name2gcolor(const char *colorname)
{
	XColor c0, c1;
	struct g_color *color;

	color = (struct g_color *)malloc(sizeof(struct g_color));

	if (XLookupColor(display, colormap, colorname, &c1, &c0)) {
		color->r = (c1.red >> 8) & 0xff;
		color->g = (c1.green >> 8) & 0xff;
		color->b = (c1.blue >> 8) & 0xff;
	} else {
		fprintf(stderr,"color '%s' unknown. ignored.\n", colorname);
	}

	return color;
}

void
free_alloc_colors(struct alloc_color *clr)
{
	if (clr->num){
		XFreeColors(display, colormap, clr->colors, clr->num, 0);
		free(clr->colors);
		clr->colors = NULL;
		clr->num = 0;
	}
}

void
regist_alloc_colors(struct alloc_color *clr, u_long *colors, u_int num)
{
	u_int   i;

	if (!clr->num)
		clr->colors = (u_long *)malloc(sizeof(u_long) * num);
	else
		clr->colors = (u_long *)realloc(clr->colors,
						sizeof(u_long) * (clr->num + num));
	for (i = 0; i < num; i++)
		clr->colors[clr->num +i] = (u_long)*(colors +i);
	clr->num += num;
}
