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

int window_x;
int window_y;

void
init_win1(char *geometry unused_parameter)
{
	if ((display = XOpenDisplay(NULL)) == NULL) {
		fprintf(stderr, "Can't open display\n");
		exit(-1);
	}
}

void
init_win2(void)
{
	XWindowAttributes xa;
	XSetWindowAttributes xsa;
	u_long mask = 0;

	screen = DefaultScreen(display);
	depth = DefaultDepth(display, screen);
	visual = DefaultVisual(display, screen);
	XGetWindowAttributes(display, DefaultRootWindow(display), &xa);
	if (window_width == 0 || window_height == 0) {
		window_width = xa.width;
		window_height = xa.height;
	}

	window = XCreateSimpleWindow(display, RootWindow(display, 0),
		0, 0, 800, 600, 0, fore_color[0], back_color[0]);

	pixmap  = XCreatePixmap(display, window,
		window_width, window_height, depth);
	xsa.override_redirect = True;
	mask |= CWOverrideRedirect;
	xsa.backing_store = Always;
	mask |= CWBackingStore;

	XChangeWindowAttributes(display, window, mask, &xsa);

	XFlush(display);
}

void
finish_win(void)
{
	XCloseDisplay(display);
}

int
get_color(const char *colorname, u_long *value)
{
	XColor c0, c1;

	screen = DefaultScreen(display);
	colormap = DefaultColormap(display, screen);
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
	Colormap cmap;
	XColor c0, c1;
	struct g_color *color;

	color = (struct g_color *)malloc(sizeof(struct g_color));
	cmap = DefaultColormap(display, 0);

	if (XLookupColor(display, cmap, colorname, &c1, &c0)) {
		color->r = (c1.red >> 8) & 0xff;
		color->g = (c1.green >> 8) & 0xff;
		color->b = (c1.blue >> 8) & 0xff;
	} else
		fprintf(stderr, "color '%s' unknown. ignored.\n", colorname);

	return color;
}

void
free_alloc_colors(struct alloc_color *clr)
{
	if (!(mgp_flag & FL_PRIVATE))
		return;
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

	if (!(mgp_flag & FL_PRIVATE))
		return;
	if (!clr->num)
		clr->colors = (u_long *)malloc(sizeof(u_long) * num);
	else
		clr->colors = (u_long *)realloc(clr->colors,
						sizeof(u_long) * (clr->num + num));
	for (i = 0; i < num; i++)
		clr->colors[clr->num +i] = (u_long)*(colors +i);
	clr->num += num;
}
