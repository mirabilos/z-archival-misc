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

#define	PL_X_MARGIN	10

static Window pl_titlewin = None;
static Window pg_win = None;
static Pixmap pg_tmp;
static int pg_lastpage = -1;

static int draw_kstring(Drawable, char *, int, int);

/*
 * Display Page List at the bottom of the window
 */

void
pl_on(struct render_state *state)
{
	XSetWindowAttributes wattr;
	unsigned int i;
	int ny, pl_x;
	u_int pl_nx, pl_ny, pl_y;

	if (pl_titlewin || !plfs)
		return;

	/* number of pages in x-axis */
	pl_nx = (window_width - 2 * PL_X_MARGIN) / pl_fw / 3;
	/* number of pages in y-axis */
	pl_ny = maxpage / pl_nx + 1;
	/* page list coordinate */
	pl_x = PL_X_MARGIN;
	/* keep 5 for timebar*/
	pl_y = window_height - (pl_ny + 1) * pl_fh - (t_fin ? 5 : 0);
	pl_titlewin = XCreateSimpleWindow(display, window,
		0, pl_y - pl_fh, window_width, pl_fh, 0,
		BlackPixel(display, 0), back_color[caching]);
	XSelectInput(display, pl_titlewin, StructureNotifyMask);

	for (i = 1; i <= maxpage; i++) {
		ny = (i - 1) / pl_nx;
		plwin[i] = XCreateSimpleWindow(display, window,
			pl_x + 3 * ((i - 1) % pl_nx) * pl_fw,
			pl_y + pl_fh * ny,
			3 * pl_fw, pl_fh, 0,
			BlackPixel(display, 0), WhitePixel(display, 0));
		wattr.do_not_propagate_mask =
			ButtonPressMask|ButtonReleaseMask;
		XChangeWindowAttributes(display, plwin[i],
			CWDontPropagate, &wattr);
		XSelectInput(display, plwin[i],
			ButtonPressMask|EnterWindowMask|
			LeaveWindowMask|StructureNotifyMask);
	}

	XMapSubwindows(display, window);
	for (i = 1; i <= maxpage; i++)
		pl_pdraw(state, i, gc_pl);
}

/*
 * Turn off the page list
 */
void
pl_off(void)
{
	unsigned int i;

	pg_lastpage = -1;
	if (!pl_titlewin)
		return;
	XUnmapSubwindows(display, window);
	XDestroyWindow(display, pl_titlewin);
	pl_titlewin = None;
	for (i = 1; i <= maxpage; i++) {
		XDestroyWindow(display, plwin[i]);
		plwin[i] = None;
	}
	XMapSubwindows(display, window);
	XFlush(display);
}

/*
 * Draw page numbers in each small window
 * Enbold the current page
 */
void
pl_pdraw(struct render_state *state, unsigned int i, GC gc)
{
	char buf[10];

	if (!pl_titlewin)
		return;

	sprintf(buf, "%02d", i % 100);
	XDrawImageString(display, plwin[i], gc, (int)(pl_fw / 2),
		(int)(pl_fh / 1.2), buf, 2);
	if (i == state->page)
		XDrawString(display, plwin[i], gc, (int)(pl_fw / 2) + 1,
			(int)(pl_fh / 1.2), buf, 2);
}

/*
 * Show the title of the page when the mouse enters a window.
 * Turn it off if the page number is zero.
 */
void
pl_title(u_int page)
{
	char buf[BUFSIZ];

	if (!pl_titlewin)
		return;

	XClearArea(display, pl_titlewin, 0, 0, window_width, pl_fh, 0);
	if (page == 0)
		return;
	sprintf(buf, "page %d: %s", page, page_title(page));
	XSetForeground(display, gc_pta, ctrl_color[caching]);
	XSetForeground(display, gc_ptk, ctrl_color[caching]);
	XSetBackground(display, gc_pta, back_color[caching]);
	XSetBackground(display, gc_ptk, back_color[caching]);
	draw_kstring(pl_titlewin, buf,
		PL_X_MARGIN, pl_fh - plfs->max_bounds.descent);
}

/*
 * Returns a pointer to title of specified page.
 */
const char *
page_title(u_int page)
{
	unsigned int l;
	const char *p;
	struct ctrl *cp;

	if( page_attribute[page].pg_title_str &&
		*page_attribute[page].pg_title_str) {
		return page_attribute[page].pg_title_str ;
	} else {
	p = "";
	for (l = 0; l <= page_attribute[page].pg_linenum; l++) {
		cp = page_control[page][l];
		while (cp && cp->ct_op != CTL_TEXT)
			cp = cp->ct_next;
		if (!cp)
			continue;
		if (cp->ctc_value && *cp->ctc_value) {
			p = cp->ctc_value;
			break;
		}
	}

	while (*p && isspace(*p))
		p++;

	return p;
	}
}

void
pg_on(void)
{
	if (pg_win)
		return;

	/* 5 for timebar*/
	pg_win = XCreateSimpleWindow(display, window,
		0, window_height - pl_fh - (t_fin ? 5 : 0),
		window_width, pl_fh, 0,
		BlackPixel(display, screen), back_color[caching]);
	XSelectInput(display, pg_win, StructureNotifyMask);
	XMapSubwindows(display, window);

	pg_tmp = XCreatePixmap(display, pg_win, window_width, pl_fh, depth);

	pg_lastpage = -1;
}

void
pg_clean(void)
{
	XClearArea(display, pg_win, 0, 0, window_width, pl_fh, 0);
	pg_lastpage = -1;
}

void
pg_draw(struct render_state *state)
{
	char buf[BUFSIZ], *p;
	int n;
	u_int page;
	int showlast;

	if (!pg_mode || !pg_win || !plfs)
		return;

	page = state->page;
	if (page == (unsigned int)pg_lastpage)
		return;

	showlast = /* !state->cp || (state->cp->ct_op == CTL_PAUSE && state->cp->cti_value) */
	    !(page > 1 && page < maxpage);
	XSetForeground(display, gc_pta, ctrl_color[caching]);
	XSetForeground(display, gc_ptk, ctrl_color[caching]);
	XSetBackground(display, gc_pta, back_color[caching]);
	XSetBackground(display, gc_ptk, back_color[caching]);

	pg_clean();
	if (page > 1) {
		snprintf(buf, sizeof(buf), "\033$B\"+\033(B %s",
			page_title(page - 1));
		draw_kstring(pg_win, buf,
			PL_X_MARGIN, pl_fh - plfs->max_bounds.descent);
	}
	if (page < maxpage) {
		snprintf(buf, sizeof(buf), "%s \033$B\"*\033(B",
			page_title(page + 1));
		for (p = buf; *p; p++)
			if (*p == '\n')
				*p = ' ';
		n = draw_kstring(pg_tmp, buf, 0, pl_fh);
		snprintf(buf, sizeof(buf), "%s \033$B\"*\033(B",
			page_title(page + 1));
		for (p = buf; *p; p++)
			if (*p == '\n')
				*p = ' ';
		draw_kstring(pg_win, buf, window_width - 10 - n,
			pl_fh - plfs->max_bounds.descent);
	}
	if (showlast == 0) {
		snprintf(buf, sizeof(buf), "-- %d/%d --", page, maxpage);
		n = draw_kstring(pg_tmp, buf, 0, pl_fh);
		/*
		 * The contents of buf is not destroyed by draw_kstring
		 * as it doesnot include Kanji sequence.
		 */
		draw_kstring(pg_win, buf, window_width / 2 - n / 2,
			pl_fh - plfs->max_bounds.descent);
	}

	pg_lastpage = state->page;
}

void
pg_off(void)
{
	if (!pg_win)
		return;
	XUnmapSubwindows(display, window);
	XFreePixmap(display, pg_tmp);
	XDestroyWindow(display, pg_win);
	XMapSubwindows(display, window);
	XFlush(display);
	pg_win = None;
	pg_lastpage = -1;
}

static int
draw_kstring(Drawable d, char *buf, int x, int y)
{
	char *p;
	int x0 = x;

	p = buf + strlen(buf) - 1;
	if (*p == '\n')
		*p = '\0';
	for (p = buf; *p; p++) {
		if (strncmp(p, "\033$@", 3) == 0 ||
		    strncmp(p, "\033$B", 3) == 0) {
			*p = '\0'; p += 3;
			/* draw 8bit char */
			XDrawImageString(display, d, gc_pta, x, y,
				buf, strlen(buf));
			x += XTextWidth(plfs, buf, strlen(buf));
			buf = p--;
		} else if (strncmp(p, "\033(B", 3) == 0 ||
			   strncmp(p, "\033-A", 3) == 0 ||
			   strncmp(p, "\033(J", 3) == 0) {
			*p = '\0'; p += 3;
			/* draw 16bit char */
			XDrawImageString16(display, d, gc_ptk, x, y,
				(XChar2b *)buf, strlen(buf)/2);
			x += XTextWidth16(plkfs, (XChar2b *)buf, strlen(buf)/2);
			buf = p--;
		}
	}
	/* draw 8bit char */
	if (*buf != '\0') {	/* Assumed ASCII at the end of string */
		XDrawImageString(display, d, gc_pta, x, y, buf, strlen(buf));
		x += XTextWidth(plfs, buf, strlen(buf));
	}
	return x - x0;
}
