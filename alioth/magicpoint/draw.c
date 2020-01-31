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
#include <Imlib2.h>

/* state associated with the window - how should we treat this? */
static struct ctrl *bg_ctl, *bg_ctl_last, *bg_ctl_cache;
static int bgindex = 0;
struct render_state cache_state;

static struct pcache {
	u_int flag;
	u_int page;
	u_int mgpflag;
	u_int mode;
	u_int effect;
	u_int value;
} pcache;

#define COMPLEX_BGIMAGE (bg_ctl && \
	((bg_ctl->ct_op == CTL_BIMAGE) || bg_ctl->ct_op == CTL_BGRAD))

#define	COMPLEX_BGIMAGE2 (0)

#define	POSY(size)	(-(int)((size)/2))

static void process_direc(struct render_state *, int *);

static int set_position(struct render_state *);
static void draw_line_output(struct render_state *, char *);
static void cutin(struct render_state *, int, int, int);

static void draw_string(struct render_state *, char *);
static u_char *draw_fragment(struct render_state *, u_char *, size_t,
    const char *, int);
static struct render_object *obj_alloc(struct render_state *state);
static void obj_free(struct render_state *, struct render_object *);
static int obj_new_xfont(struct render_state *, int, int, int,
	u_int, const char *);
static int obj_new_image2(struct render_state *, int, int, Image *, int, int, Imlib_Image *, int);
static int obj_new_icon(struct render_state *, int, int, u_int, u_int, u_long, u_int, XPoint *);
static Pixel obj_image_color(Image *, Image *, Pixel, int *);
static Image *obj_image_trans(Image *, u_int, u_int);
static void obj_draw_image(Drawable, u_int, u_int, struct render_object *, int);
static void obj_draw(struct render_state *, Drawable, u_int, u_int);
static char *x_fontname(char *, int, const char *, int, const char *);
static int x_parsefont(char *, int *, int*);
static XFontStruct *x_setfont(const char *, u_int, const char *, int *);
static u_int draw_onechar_x(struct render_state *, u_int, int, int, int,
	const char *, int);

static void back_gradation(struct render_state *, struct ctrl_grad *);
static void image_load(struct render_state *, char *, int, int, int, int, int, int, int, int, int);
static void image_load_ps(struct render_state *, char *, int, int, int, int, int, int, int, int, int);
static void process_icon(struct render_state *, struct ctrl *);
static void draw_bar(struct render_state *, struct ctrl *);
static void process_system(struct render_state *, struct ctrl *);
static void process_xsystem(struct render_state *, struct ctrl *);
static void process_tsystem(struct render_state *, struct ctrl *);
static Window search_child_window(void);
static Window tsearch_child_window(const char *name);
static Window getNamedWindow(const char *name, Window top);
static void reparent_child_window(Window, int, int);
static char *epstoimage(const char *, int, int, int, int,
    volatile int, volatile int);
static void image_setcolor(struct render_state *);
static void x_registerseed(struct render_state *, char *, const char *);
static const char *x_findseed(struct render_state *, const char *);
static int get_regid(const char *);

static void XClearPixmap(Display *, Drawable);
static void cache_page(struct render_state *, unsigned int);
static void cache_effect1(void);
static void cache_effect2(void);
static void set_from_cache(struct render_state *);
static void pcache_process(unsigned int);
static void predraw(struct render_state *);
static void set_background_pixmap(struct ctrl *);
static void get_background_pixmap(struct ctrl *, struct render_state *);
static void regist_background_pixmap(XImageInfo *, Image *);
static int valign = VL_BOTTOM;

#define CHECK_CACHE	do {if (caching) {caching = -1; return; }} while (0)

static void set_xrender_color(unsigned long, int);
static XftDraw * xft_getdraw(Drawable);
static u_char *xft_draw_fragment(struct render_state *,
    u_char *, size_t, const char *, int);
static int obj_new_xftfont(struct render_state *, int, int, u_char *,
    size_t, const char *, const char *, int, int, XftFont *);
static XftFont * xft_setfont(const char *, int, const char *);
XftFont *xft_font;
XftDraw *xft_draw[2];
Drawable xft_xdraw[2];
XftColor xft_forecolor;
XRenderColor xft_render_color;

static void regist_zimage_position(struct render_object *, int, int, int, int, int);
static void clear_zimage(int);
static void clear_region(int, int, int, int);
#define ZIMAGENUM 100
static Imlib_Image *zimage[ZIMAGENUM];
static int zonzoom[ZIMAGENUM];
static int zpage[ZIMAGENUM];
static int zx[ZIMAGENUM];
static int zy[ZIMAGENUM];
static int zwidth[ZIMAGENUM];
static int zheight[ZIMAGENUM];

static int
ispsfilename(char *p0)
{
	char *p;

	p = p0;
	while (*p)
		p++;
	if (4 < p - p0 && strcasecmp(p - 4, ".eps") == 0)
		return 1;
	if (3 < p - p0 && strcasecmp(p - 3, ".ps") == 0)
		return 1;
	if (6 < p - p0 && strcasecmp(p - 6, ".idraw") == 0)
		return 1;
	return 0;
}

/*
 * state management.
 */
void
state_goto(struct render_state *state, u_int page, int repaint)
{
	if (!repaint) {
		purgechild(state->page);
		clear_zimage(state->page);
	}

	state->page = page;
	state->line = 0;
	state->cp = NULL;
	state->phase = P_NONE;
	free_alloc_colors(&image_clr);
	free_alloc_colors(&font_clr);

	colormap = XCopyColormapAndFree(display, colormap);
	predraw(state);
}

void
state_next(struct render_state *state)
{

	switch (state->phase) {
	case P_NONE:
		fprintf(stderr, "internal error\n");
		break;
	case P_DEFAULT:
		if (state->cp)
			state->cp = state->cp->ct_next;
		if (!state->cp) {
			state->cp = page_control[state->page][state->line];
			state->phase = P_PAGE;
		}
		break;
	case P_PAGE:
		if (state->cp)
			state->cp = state->cp->ct_next;
		if (!state->cp) {
			state->line++;
			state->cp = NULL;
			state->phase = P_NONE;
			state_init(state);
		}
		break;

	case P_END:
		/*nothing*/
		break;
	}

	/* next page */
	if (page_attribute[state->page].pg_linenum < state->line) {
		if (state->page < maxpage) {
			purgechild(state->page);
			clear_zimage(state->page);
			if (mgp_flag & FL_FRDCACHE &&
				cached_page == state->page + 1) {
					/* Hit cache */
					set_from_cache(state);
					pcache_process(state->page);
					cache_hit = 1;
			} else {
				state->phase = P_NONE;
				state->page++;
				state->line = 0;
				state_newpage(state);
				state_init(state);
			}
		} else
			state->phase = P_END;
	}
}

void
state_init(struct render_state *state)
{
	assert(state);

	if (state->phase == P_NONE || !state->cp) {
		state->cp = page_control[state->page][state->line];
		state->phase = P_PAGE;
	}
}

void
state_newpage(struct render_state *state)
{
	state->ypos = 0;
	state->have_mark = 0;
	state->charoff = 0;
	char_size[caching] = nonscaled_size[caching];
	free_alloc_colors(&image_clr);
	free_alloc_colors(&font_clr);

	colormap = XCopyColormapAndFree(display, colormap);
	predraw(state);
}

/*
 * page management.
 */
void
draw_page(struct render_state *state, struct ctrl *lastcp)
{
	int pause2;

	assert(state);

	/* initialize the state, if required. */
	if (state->phase != P_END && (state->phase == P_NONE || !state->cp)) {
		state_newpage(state);
		state_init(state);
	}

	while (1) {
		switch (state->phase) {
		case P_NONE:
			fprintf(stderr, "internal error\n");
			cleanup(-1);
			/* NOTREACHED */
		case P_DEFAULT:
		case P_PAGE:
			pause2 = 0;
			if (state->cp)
				process_direc(state, &pause2);
			if (caching == -1) {
				/* caching failed */
				caching = 0;
				return;
			}
			if (lastcp && state->cp == lastcp)
				goto done;
			if (pause2) {
				if (state->cp
				 && state->cp->ct_op == CTL_PAUSE
				 && state->cp->cti_value) {
					goto done;
				}
			}
			break;
		case P_END:
			goto done;
		}
		state_next(state);
	}
done:
	XFlush(display);
}

Bool
draw_one(struct render_state *state, XEvent *e)
{
	int pause2;
	fd_set fds;
	int xfd;
	struct timeval tout;
	long emask;
#ifdef TTY_KEYINPUT
	KeySym ks;
	char c;
#endif

	assert(state);

	/* initialize the state, if required. */
	if (state->phase != P_END && (state->phase == P_NONE || !state->cp)) {
		state_newpage(state);
		state_init(state);
	}

	switch (state->phase) {
	case P_DEFAULT:
	case P_PAGE:
		pause2 = 0;
		if (state->cp)
			process_direc(state, &pause2);
		break;
	case P_END:
		break;
	case P_NONE:
	default:
		fprintf(stderr, "internal error\n");
		cleanup(-1);
	}
	xfd = ConnectionNumber(display);
	if (state->phase != P_END && !pause2)
		emask = xeventmask;
	else
		emask = ~NoEventMask;
	for (;;) {
		if (XCheckMaskEvent(display, emask, e) == True) {
			/* we got some event in the queue*/
			if (2 <= parse_debug) {
				fprintf(stderr,
					"interrupted and "
					"got X11 event type=%d\n",
					e->type);
			}
  got_event:
			if (state->phase == P_END)
				XFlush(display);
			else if (!pause2)
				state_next(state);
			return True;
		}
#ifdef TTY_KEYINPUT
		if (ttykey_enable) {
			FD_ZERO(&fds);
			FD_SET(0, &fds);
			tout.tv_sec = tout.tv_usec = 0;
			if (select(1, &fds, NULL, NULL, &tout) > 0
			&&  read(0, &c, sizeof(c)) == sizeof(c)) {
				if (c > 0 && c < ' ')
					ks = 0xff00 | c;
				else if (c >= ' ' && c < '\177')
					ks = c;
				else if (c == '\177')
					ks = XK_Delete;
				else
					continue;
				e->xkey.display = display;
				e->xkey.type = KeyPress;
				e->xkey.keycode = XKeysymToKeycode(display, ks);
				if (e->xkey.keycode == 0)
					continue;
				goto got_event;
			}
		}
#endif
		if (state->phase != P_END && !pause2) {
			state_next(state);
			return False;
		}
		FD_ZERO(&fds);
		FD_SET(xfd, &fds);
#ifdef TTY_KEYINPUT
		if (ttykey_enable)
			FD_SET(0, &fds);
#endif
		remapchild();
		/* always cache next page */
		if ((mgp_flag & FL_FRDCACHE) && cache_mode) {
			if (XCheckMaskEvent(display, emask, e) == True)
				goto got_event;
			cache_page(&cache_state, state->page + 1);
			/* check if we got some events during caching */
			if (XCheckMaskEvent(display, emask, e) == True)
				goto got_event;
		}

		/* wait for something */
		tout.tv_sec = 2;
		tout.tv_usec = 0;
		(void)select(xfd + 1, &fds, NULL, NULL, &tout);

#ifdef TTY_KEYINPUT
		if (!(mgp_flag & FL_NOSTDIN) && !ttykey_enable)
			try_enable_ttykey();
#endif
		/* we have no event in 2sec, so..*/
		if (!FD_ISSET(xfd, &fds)) {
			if ((mgp_flag & FL_FRDCACHE) && !cache_mode)
				cache_page(&cache_state, state->page + 1);
			timebar(state);
			e->type = 0;
			return True;
		}
	}
	/*NOTREACHED*/
}

static void
process_direc(struct render_state *state, int *seenpause)
{
	struct ctrl *cp;

	if (seenpause)
		*seenpause = 0;
	cp = state->cp;

	if (2 <= parse_debug) {
		fprintf(stderr, "p%d/l%d: ", state->page, state->line);
		debug0(cp);
	}

	switch(cp->ct_op) {
	case CTL_SUP:
		if (sup_scale > 1.0 || sup_scale < 0.1) {
			sup_scale = DEFAULT_SUPSCALE;
		}
		if (sup_off > 1.0 || sup_scale < 0.1) {
			sup_off = DEFAULT_SUPOFF;
		}
		state->charoff = -sup_off * nonscaled_size[caching];
		char_size[caching] = (int)(nonscaled_size[caching] * sup_scale);
		break;
	case CTL_SUB:
		if (sup_scale > 1.0 || sup_scale < 0.1) {
			sup_scale = DEFAULT_SUPSCALE;
		}
		if (sub_off > 1.0 || sub_off < 0.1){
			sub_off = DEFAULT_SUBOFF;
		}
		state->charoff = sub_off * nonscaled_size[caching];
		char_size[caching] = (int)(nonscaled_size[caching] * sup_scale);
		break;
	case CTL_SETSUP:
		if (cp->cti3_value1 > 100 || cp->cti3_value1 < 10){
			sup_off = DEFAULT_SUPOFF;
		} else {
			sup_off = cp->cti3_value1 / 100.;
		}
		if (cp->cti3_value2 > 100 || cp->cti3_value2 < 10){
			sub_off = DEFAULT_SUBOFF;
		} else {
			sub_off = cp->cti3_value2 / 100.;
		}
		if (cp->cti3_value3 > 100 || cp->cti3_value3 < 10){
		     sup_scale = DEFAULT_SUPSCALE;
		} else {
		     sup_scale = cp->cti3_value3 / 100.;
		}
		break;
	case CTL_SIZE:
		nonscaled_size[caching] = state->height * cp->ctf_value / 100;
		char_size[caching] = nonscaled_size[caching];
		break;

	case CTL_VGAP:
		vert_gap[caching] = cp->cti_value;
		break;

	case CTL_HGAP:
		horiz_gap[caching] = cp->cti_value;
		break;

	case CTL_GAP:
		vert_gap[caching] = horiz_gap[caching] = cp->cti_value;
		break;

	case CTL_QUALITY:
		if (!quality_flag)
			b_quality[caching] = cp->cti_value;
		break;

	case CTL_PAUSE:
		CHECK_CACHE;
		if (seenpause)
			*seenpause = 1;
		break;

	case CTL_AGAIN:
		CHECK_CACHE;
		if (state->have_mark)
			state->ypos = state->mark_ypos;
		state->have_mark = 0;
		break;

	case CTL_FORE:
		fore_color[caching] = cp->ctl_value;
		XSetForeground(display, gcfore, fore_color[caching]);
		break;

	case CTL_BACK:
		if (state->line){
			fprintf(stderr, "warning: %%back directive should be put in the first line of the page. ignored.\n");
			break;
		}
		back_color[caching] = cp->ctl_value;
		bg_ctl = cp;	/*update later*/
		break;

	case CTL_CCOLOR:
		ctrl_color[caching] = cp->ctl_value;
		break;

	case CTL_CENTER:
		state->align = AL_CENTER;
		break;

	case CTL_LEFT:
		state->align = AL_LEFT;
		break;

	case CTL_LEFTFILL:
		state->align = AL_LEFTFILL0;
		break;

	case CTL_RIGHT:
		state->align = AL_RIGHT;
		break;

	case CTL_CONT:
		state->charoff = 0;
		char_size[caching] = nonscaled_size[caching];
		break;

	case CTL_XFONT2:
		x_registerseed(state, cp->ctc2_value1, cp->ctc2_value2);
		break;

	case CTL_BAR:
		draw_bar(state, cp);
		break;

	case CTL_IMAGE:
	    {
		if (state->align == AL_LEFTFILL0) {
			state->align = AL_LEFTFILL1;
			state->leftfillpos = state->linewidth;
		}

		/* quickhack for postscript */
		if (ispsfilename(cp->ctm_fname)) {
			image_load_ps(state, cp->ctm_fname, cp->ctm_numcolor,
				cp->ctm_ximagesize, cp->ctm_yimagesize, 0,
				cp->ctm_zoomflag, 0, cp->ctm_raise, cp->ctm_rotate, cp->ctm_zoomonclk);
		} else {
			image_load(state, cp->ctm_fname, cp->ctm_numcolor,
				cp->ctm_ximagesize, cp->ctm_yimagesize, 0,
				cp->ctm_zoomflag, 0, cp->ctm_raise, cp->ctm_rotate, cp->ctm_zoomonclk);
		}
		state->brankline = 0;
	    }
		break;

	case CTL_BIMAGE:
		if (mgp_flag & FL_BIMAGE)
			break;
		bg_ctl = cp;	/*update later*/
		break;

	case CTL_BGRAD:
		if (mgp_flag & FL_BIMAGE)
			break;
		bg_ctl = cp;	/*update later*/
		break;

	case CTL_LCUTIN:
		CHECK_CACHE;
		state->special = SP_LCUTIN;
		break;

	case CTL_RCUTIN:
		CHECK_CACHE;
		state->special = SP_RCUTIN;
		break;

	case CTL_SHRINK:
		CHECK_CACHE;
		state->special = SP_SHRINK;
		break;

	case CTL_PREFIX:
		state->curprefix = cp->ctc_value;
		break;

	case CTL_PREFIXN:
		state->xprefix = state->width * cp->ctf_value / 100;
		break;

	case CTL_TABPREFIX:
		state->tabprefix = cp->ctc_value;
		break;

	case CTL_TABPREFIXN:
		state->tabxprefix = state->width * cp->ctf_value / 100;
		break;

	case CTL_PREFIXPOS:
	    {
		char *p;

		p = (state->tabprefix) ? state->tabprefix : state->curprefix;
		if (p)
			draw_line_output(state, p);
		break;
	    }

	case CTL_TEXT:
		if (!cp->ctc_value)
			break;
		if (state->align == AL_LEFTFILL0) {
			state->align = AL_LEFTFILL1;
			state->leftfillpos = state->linewidth;
		}
		draw_line_output(state, cp->ctc_value);
		break;

	case CTL_LINESTART:
		state->charoff = 0;
		char_size[caching] = nonscaled_size[caching];
		if (state->line == 0) {
			/*
			 * set background of target
			 */
			if (bg_ctl) {
				if (!caching){
					/* target is window, so we need care bg_ctl_last */
					if (bg_ctl_last && !ctlcmp(bg_ctl, bg_ctl_last)){
						/* same as last time, we do nothing  */
						;
					} else {
						/* we have to change background */
						get_background_pixmap(bg_ctl, state);

						/* set window background */
						set_background_pixmap(bg_ctl);

						bg_ctl_last = bg_ctl;
					}
					XClearWindow(display, state->target);
				} else {
					get_background_pixmap(bg_ctl, state);
					bg_ctl_cache = bg_ctl;

					XClearPixmap(display, state->target);
				}
			} else {
				if (!caching)
					XClearWindow(display, state->target);
				else
					XClearPixmap(display, state->target);
			}

			if (t_fin)
				timebar(state);
		}
		draw_line_start(state);
		break;

	case CTL_LINEEND:
		/* blank lines */
		if (state->brankline) {	/*XXX*/
			state->max_lineascent = char_size[caching];
			state->maxascent = char_size[caching];
			state->maxdescent = VERT_GAP(char_size[caching]);
		}
		draw_line_end(state);
		/* reset single-line oriented state */
		state->tabprefix = NULL;
		state->tabxprefix = 0;
		state->special = 0;
		if (state->align == AL_LEFTFILL1) {
			state->align = AL_LEFTFILL0;
			state->leftfillpos = 0;
		}
		break;

	case CTL_MARK:
		state->have_mark = 1;
		state->mark_ypos = state->ypos;
		break;

	case CTL_SYSTEM:
		CHECK_CACHE;
		process_system(state, cp);
		break;

	case CTL_XSYSTEM:
		CHECK_CACHE;
		process_xsystem(state, cp);
		break;

	case CTL_TSYSTEM:
		CHECK_CACHE;
		process_tsystem(state, cp);
		break;

	case CTL_ICON:
		process_icon(state, cp);
		break;

	case CTL_NOOP:
	case CTL_NODEF:
	case CTL_TITLE:
		break;

	case CTL_XFONT:
		/* obsolete directives */
		fprintf(stderr, "internal error: obsolete directive "
			"\"%s\"\n", ctl_words[cp->ct_op].ctl_string);
		exit(1);
		/*NOTREACHED*/

	case CTL_PCACHE:
		if (!caching) {
			if (cp->ctch_flag)
				mgp_flag |= FL_FRDCACHE;
			else
				mgp_flag ^= FL_FRDCACHE;
			cache_mode   = cp->ctch_mode;
			cache_effect = cp->ctch_effect;
			cache_value  = cp->ctch_value;
		} else {
			pcache.flag = 1;
			pcache.page = state->page;
			pcache.mgpflag = cp->ctch_flag;
			pcache.mode = cp->ctch_mode;
			pcache.effect = cp->ctch_effect;
			pcache.value = cp->ctch_value;
		}
		break;

	case CTL_CHARSET:
		if (get_regid(cp->ctc_value) < 0){
			fprintf(stderr, "invalid charset \"%s\". ignored\n",
				cp->ctc_value);
			break;
		}
		strlcpy(mgp_charset, cp->ctc_value, sizeof(mgp_charset));
		break;

	case CTL_VALIGN:
		valign = cp->cti_value;
		break;

	case CTL_AREA:
		state->width = window_width * cp->ctar_width / 100;
		state->height = window_height * cp->ctar_height / 100;
		state->xoff = window_width * cp->ctar_xoff / 100;
		state->yoff = window_height * cp->ctar_yoff / 100;
		state->ypos = 0;
		break;

	case CTL_OPAQUE:
		if (cp->cti_value > 100){
			fprintf(stderr, "%%opaque: value should be 0-100\n");
			cp->cti_value = 100;
		}
		state->opaque = cp->cti_value;
		if (mgp_flag & FL_NOXFT && verbose){
			fprintf(stderr, "ignored %%opaque.\n");
		}
		break;
	case CTL_PSFONT:
		break;
	default:
		fprintf(stderr,
			"undefined directive %d at page %d line %d:\n\t",
			cp->ct_op, state->page, state->line);
		debug0(cp);
		break;
	}
}

/*
 * line management.
 */
static int
set_position(struct render_state *state)
{
	int x;

	x = 0;
	switch (state->align) {
	case AL_CENTER:
		x = (state->width - state->linewidth)/ 2;
		break;

	case AL_LEFT:
	case AL_LEFTFILL0:
	case AL_LEFTFILL1:
		x = 0;
		break;

	case AL_RIGHT:
		x = state->width - state->linewidth;
		break;
	}

	return x;
}

void
draw_line_start(struct render_state *state)
{
	struct render_object *obj;

	state->max_lineascent = 0;
	state->max_linedescent = 0;
	state->maxascent = 0;
	state->maxdescent = 0;
	state->linewidth = 0;
	state->brankline = 1;
	while ((obj = state->obj))
		obj_free(state, obj);
}

void
draw_line_itemsize(struct render_state *state,
    unsigned int ascent, unsigned int descent, int flheight)
{
	ascent -= state->charoff;
	descent += state->charoff;
	if (ascent > state->maxascent)
		state->maxascent = ascent;
	if (descent > state->maxdescent)
		state->maxdescent = descent;

	/*
	 * calculation for the height of a line should ignore
	 * character offset
	 */
	if (state->charoff == 0) {
		if (ascent > state->max_lineascent)
			state->max_lineascent = ascent;
		if (descent > state->max_linedescent)
			state->max_linedescent = descent;
	}

	if (flheight > state->maxflheight)
		state->maxflheight = flheight;
}

static void
draw_line_output(struct render_state *state, char *data)
{
	draw_string(state, data);
}

void
draw_line_end(struct render_state *state)
{
	int xpos;

	xpos = set_position(state);

	/* process the special attribute. */
	switch (state->special) {
	case SP_LCUTIN:
		cutin(state, xpos, state->ypos, 1);
		break;
	case SP_RCUTIN:
		cutin(state, xpos, state->ypos, -1);
		break;
	default:
		break;
	}
	if (state->obj) {
		obj_draw(state, state->target, xpos, state->ypos);
		while (state->obj)
			obj_free(state, state->obj);
	}

	state->ypos += state->max_lineascent;

	/*
	 * we should ignore height of images to calculate line gap.
	 * suggested by Toru Terao
	 */
	if (VERT_GAP(char_size[caching]) < state->max_linedescent)
		state->ypos += state->max_linedescent;
	else
		state->ypos += VERT_GAP(char_size[caching]);

	state->ypos += 2;
}

#undef min
#define min(x, y) (x < y ? x: y)
static void
cutin(struct render_state *state, int lx, int ly, int dir)
{
	u_int x, xoff, yoff;
	int i, sx, round2;
	int root_x, root_y, use_copy;
	Window cutinWin = 0, junkwin;
	XImage *copywin;
	static XWindowAttributes xa;
	XWindowAttributes wa;
	Pixmap ghostWin;
	GC saveGC = gc_cache;

	XGetWindowAttributes(display, window, &wa);
	ghostWin = XCreatePixmap(display, window, wa.width, wa.height, wa.depth);
	/* all drawing should be done on the image */
	gc_cache = XCreateGC(display, ghostWin, 0, 0);
	XCopyArea(display, state->target, ghostWin, gc_cache,
			0, 0, wa.width, wa.height, 0, 0);

	if (state->repaint)
		return;

	if (!state->linewidth)
		return;

	if (!xa.width)
		XGetWindowAttributes(display, DefaultRootWindow(display), &xa);
	XTranslateCoordinates(display, window, DefaultRootWindow(display),
		0, 0, &root_x ,&root_y, &junkwin);
	use_copy = 1;
	if ((root_x + window_width > xa.width) || (root_y + window_height > xa.height) ||
			(root_x < 0 || root_y < 0)) use_copy = 1;

	sx = (0 < dir) ? 0 : state->width - state->linewidth;
	round2 = 20;	/*XXX*/
#ifndef abs
#define abs(a)	(((a) < 0) ? -(a) : (a))
#endif
	if (abs(lx - sx) < round2){
		round2 = abs(lx - sx);
		if (!round2) round2 = 1;
	}

	if (!use_copy){
		cutinWin = XCreateSimpleWindow(display, state->target,
			sx, ly, state->linewidth, state->maxascent + state->maxdescent,
			0, fore_color[caching], back_color[caching]);
		XSetWindowBackgroundPixmap(display, cutinWin, None);
		XMapSubwindows(display, state->target);
	} else {
		copywin = XGetImage(display, window, state->xoff + min(sx, lx), ly + state->yoff, state->linewidth + abs(lx - sx),
					state->maxascent + state->maxdescent, AllPlanes, ZPixmap);
	}

	xoff = state->xoff;
	yoff = state->yoff;
	state->xoff = state->yoff = 0;
	if (state->obj && !use_copy) {
		obj_draw(state, cutinWin, 0, 0);
	}
	XFlush(display);

	x = sx;
	for (i = 0; i < round2; i++) {
		if (use_copy && state->obj) {
				obj_draw(state, ghostWin, x + xoff, ly + yoff);
				XCopyArea(display, ghostWin, state->target,
				    saveGC,
				    xoff + min(sx, lx),
				    ly + yoff,
				    state->linewidth + abs(lx - sx),
				    state->maxascent + state->maxdescent,
				    xoff + min(sx, lx),
				    ly + yoff);
		} else
			XMoveWindow(display, cutinWin, x + xoff, ly + yoff);

		XFlush(display);
		usleep(CUTIN_DELAY);
		if (use_copy && state->obj) {
			XPutImage(display, ghostWin, gc_cache, copywin,
				x - min(sx, lx) , 0, x + xoff, ly + yoff,
				state->linewidth, state->maxascent + state->maxdescent);
		}
		x = sx + ((i+1)*(lx - sx)) / round2;
	}
	XCopyArea(display, ghostWin, state->target, saveGC,
		0, 0, wa.width, wa.height, 0, 0);

	if (!use_copy) XDestroyWindow(display, cutinWin);
	state->xoff = xoff;
	state->yoff = yoff;

	/* freeing images */
	if(use_copy) XFree(copywin);

	/* restoring tho old GC */
	XFreeGC(display, gc_cache);
	XFreePixmap(display, ghostWin);
	gc_cache = saveGC;
}

/*
 * render characters.
 */
static void
draw_string(struct render_state *state, char *data)
{
	u_char *p, *q;
	const char *registry = NULL;
	u_int code2;
	static const char *rtab96[] = {
		NULL,			/* ESC - @ */
		"iso8859-1",		/* ESC - A */
		"iso8859-2",		/* ESC - B */
		"iso8859-3",		/* ESC - C */
		"iso8859-4",		/* ESC - D */
	};
#define RTAB96_MAX	(sizeof(rtab96)/sizeof(rtab96[0]))
	static const char *rtab9494[] = {
		"jisx0208.1978-*",	/* ESC $ @ or ESC $ ( @ */
		"gb2312.1980-*",	/* ESC $ A or ESC $ ( A */
		"jisx0208.1983-*",	/* ESC $ B or ESC $ ( B */
		"ksc5601.1987-*",	/* ESC $ ( C */
		NULL,			/* D */
		NULL,			/* E */
		NULL,			/* F */
		NULL,			/* G */
		NULL,			/* H */
		NULL,			/* I */
		NULL,			/* J */
		NULL,			/* K */
		NULL,			/* L */
		NULL,			/* M */
		NULL,			/* N */
		"jisx0213.2000-1",	/* ESC $ ( O */
		"jisx0213.2000-2",	/* ESC $ ( P */
	};
#define RTAB9494_MAX	(sizeof(rtab9494)/sizeof(rtab9494[0]))
	int charset16 = 0;

	p = (u_char *)data;
	while (*p && *p != '\n') {
		/* 94x94 charset */
		if (p[0] == 0x1b && p[1] == '$' &&
		    '@' <= p[2] && p[2] < 'C' && rtab9494[p[2] - '@']) {
			registry = rtab9494[p[2] - '@'];
			charset16 = 1;
			p += 3;
			continue;
		}
		if (p[0] == 0x1b && p[1] == '$' && p[2] == '(' &&
		    '@' <= p[3] && p[3] < '@' + RTAB9494_MAX &&
		    rtab9494[p[3] - '@']) {
			registry = rtab9494[p[3] - '@'];
			charset16 = 1;
			p += 4;
			continue;
		}
		/* ascii (or JIS roman) */
		if (p[0] == 0x1b && p[1] == '(' &&
		    (p[2] == 'B' || p[2] == 'J')) {
			registry = NULL;
			charset16 = 0;
			p += 3;
			continue;
		}
		/* 96 charset */
		if (p[0] == 0x1b && p[1] == '-' &&
		    '@' < p[2] && p[2] < '@' + RTAB96_MAX &&
		    rtab96[p[2] - '@']) {
			registry = rtab96[p[2] - '@'];
			charset16 = 0;
			p += 3;
			continue;
		}

		if (!registry && isspace(p[0])) {
			draw_fragment(state, p, 1, registry, 0);
			p++;
			continue;
		}

		if (charset16) {
			for (q = p + 2; 0x21 <= *q && *q <= 0x7e; q += 2) {
				code2 = q[0] * 256 + q[1];
				if (strncmp(registry, "jisx0208", 8) == 0
				 && !iskinsokuchar(code2)) {
					break;
				}
			}
		} else {
			q = p;
			while (*q && isprint(*q) && !isspace(*q))
				q++;
			if (q == p)
				q++;
			else {
				/*
				 * append spaces to the end of the word.
				 * fragments in the following line:
				 *	"this is test"
				 * are:
				 *	"this_" "is_" "test"
				 */
				while (*q && isspace(*q))
					q++;
			}
		}

		q = draw_fragment(state, p, q - p, registry, charset16);

		p = q;
	}
}

static u_char *
draw_fragment(struct render_state *state, u_char *p, size_t len,
    const char *registry, /* 2-octet charset? */ int charset16)
{
	u_int char_len, i;
	u_short code;
	struct render_object *tail;
	struct render_object *thisline;
	struct render_object *thislineend;
	u_int startwidth;
	struct render_state backup0, backup;
	enum { MODE_UNKNOWN, MODE_X }
		mode = MODE_UNKNOWN;

	if (!(mgp_flag & FL_NOXFT)){
		u_char *p0 = xft_draw_fragment(state, p, len, registry, charset16);
		if (p0) return p0;
	}

	if (state->obj)
		tail = state->objlast;
	else
		tail = NULL;
	startwidth = state->linewidth;

	while (len) {
		code = charset16 ? p[0] * 256 + p[1] : p[0];
		if (code != ' ')
			state->brankline = 0; /* This isn't brankline */

		if (code == '\t') {
			char_len = char_size[caching] / 2;
			p++;
			len--;

			char_len = HORIZ_STEP(char_size[caching], char_len) * 8;/*XXX*/
			state->linewidth = (state->linewidth + char_len) / char_len * char_len;
			continue;
		}

		/*
		 * decide which font to use.
		 * Japanese font:
		 *	VFlib - optional
		 *	then X.
		 * Western font:
		 *	X if truely scalable.
		 *	otherwise, X.
		 */
		mode = MODE_UNKNOWN;
		if (charset16) {
			if (mode == MODE_UNKNOWN)
				mode = MODE_X;
		} else {
			if (mode == MODE_UNKNOWN) {
				/*
				 * if we can have X font that is exactly
				 * matches the required size, we use that.
				 */
				int ts;

				x_setfont(x_findseed(state, registry),
				    char_size[caching], registry, &ts);
				if (ts)
					mode = MODE_X;
			}

			/* last resort: use X font. */
			if (mode == MODE_UNKNOWN)
				mode = MODE_X;
		}

		/* back it up before drawing anything */
		memcpy(&backup0, state, sizeof(struct render_state));

		switch (mode) {
		default:
			fprintf(stderr, "invalid drawing mode %d for %04x "
				"- fallback to X11\n", mode, code);
			/* fall through */
		case MODE_UNKNOWN:
		case MODE_X:
			char_len = draw_onechar_x(state, code,
				state->linewidth, state->charoff, char_size[caching],
				registry, (len == (charset16 ? 2 : 1)) ? 1 : 0);
			if (char_len == 0) {
				fprintf(stderr, "can't load font size %d "
					"(nor font in similar size) for "
					"font <%s:%d:%s>, glyph 0x%04x\n",
					char_size[caching], x_findseed(state, registry),
					char_size[caching], registry?registry:"NULL", code);
			}
			break;
		}

		p += (charset16 ? 2 : 1);
		len -= (charset16 ? 2 : 1);

		state->linewidth += HORIZ_STEP(char_size[caching], char_len);
		/* ukai */
		if (!charset16 && state->linewidth + HORIZ_STEP(char_size[caching],
				char_len) > state->width) {
			if (len >= 20) break; /* too long word */
			for (i = 0; i < len; i ++){
				if (isspace(*(p +i))) break;
			}
			if (i == len) break;
		}
	}

	if (state->width - state->leftfillpos / 2 < state->linewidth) {
		memcpy(&backup, state, sizeof(struct render_state));

		/* strip off the last fragment we wrote. */
		if (tail) {
			thisline = tail->next;
			thislineend = state->objlast;
			tail->next = NULL;
			state->objlast = tail;
			state->maxascent = backup0.maxascent;
			state->maxdescent = backup0.maxdescent;
		} else {
			thisline = state->obj;
			thislineend = state->objlast;
			state->obj = state->objlast = NULL;
			state->maxascent = backup0.maxascent;
			state->maxdescent = backup0.maxdescent;
		}
		state->linewidth = startwidth;
		draw_line_end(state);	/* flush the line. */

		/* start the new line with the last fragment we wrote. */
		draw_line_start(state);
		state->linewidth = state->leftfillpos;
		state->linewidth += (backup.linewidth - startwidth);
		if (state->obj && state->objlast)
			state->objlast->next = thisline;
		else
			state->obj = thisline;
		state->objlast = thislineend;
		state->align = backup.align;
		/* fix up x position and maxascent. */
		for (tail = state->obj; tail; tail = tail->next) {
			tail->x -= startwidth;
			tail->x += state->leftfillpos;
			draw_line_itemsize(state, tail->ascent, tail->descent, 0);
		}
	}
	return p;
}

static struct render_object *
obj_alloc(struct render_state *state)
{
	struct render_object *obj;

	obj = malloc(sizeof(*obj));
	if (obj == NULL)
		return NULL;
	obj->next = NULL;
	if (state->obj == NULL)
		state->obj = obj;
	else
		state->objlast->next = obj;
	state->objlast = obj;
	return obj;
}

static void
obj_free(struct render_state *state, struct render_object *obj)
{
	struct render_object *o;

	if (state->obj == obj)
		state->obj = obj->next;
	else {
		for (o = state->obj; o; o = o->next)
			if (o->next == obj)
				break;
		/* ASSERT(o != NULL); */
		o->next = obj->next;
	}
	if (state->objlast == obj)
		state->objlast = obj->next;
	switch (obj->type) {
	case O_IMAGE:
		freeImage(obj->data.image.image);
		break;
	case O_XFONT:
		free(obj->data.xfont.xfont);
		break;
	case O_ICON:
		if (obj->data.icon.xpoint)
			free(obj->data.icon.xpoint);
		break;
	case O_XTFONT:
		if (obj->data.xftfont.data)
			free(obj->data.xftfont.data);
		if (obj->data.xftfont.fontname)
			free(obj->data.xftfont.fontname);
		if (obj->data.xftfont.registry)
			free(obj->data.xftfont.registry);
		break;
	}
	free(obj);
}

static int
obj_new_xfont(struct render_state *state,
    int x, int y, int size, u_int code, const char *registry)
{
	struct render_object *obj;

	obj = obj_alloc(state);
	if (obj == NULL)
		return 0;
	obj->x = x;
	obj->y = y;
	obj->fore = fore_color[caching];
	obj->type = O_XFONT;
	obj->data.xfont.xfont = strdup(x_findseed(state, registry));
	obj->data.xfont.csize = size;
	obj->data.xfont.code = code;
	obj->data.xfont.registry = registry;
	obj->ascent = size - y;	/*XXX*/
	obj->descent = -y;	/*XXX*/
	obj->vertloc = VL_BASE;
	return 1;
}

static int
obj_new_image2(struct render_state *state,
    int x, int y, Image *image, int xzoom, int yzoom,
    Imlib_Image *imimage, int zoomonclk)
{
	struct render_object *obj;

	obj = obj_alloc(state);
	if (obj == NULL)
		return 0;
	obj->x = x;
	obj->y = y;
	obj->type = O_IMAGE;
	obj->data.image.image = image;
	obj->data.image.xzoom = xzoom;
	obj->data.image.yzoom = yzoom;
	obj->ascent = 0;	/*XXX*/
	obj->descent = image->height * yzoom / 100;	/*XXX*/
	obj->vertloc = VL_TOP;
	obj->data.image.imimage = imimage;
	obj->data.image.zoomonclk = zoomonclk;
	return 1;
}

static int
obj_new_icon(struct render_state *state, int x, int y,
    u_int itype, u_int isize, u_long color, u_int npoint, XPoint *xpoint)
{
	struct render_object *obj;
	unsigned int i;

	obj = obj_alloc(state);
	if (obj == NULL)
		return 0;
	obj->x = x;
	obj->y = y;
	obj->fore = color;
	obj->type = O_ICON;
	obj->data.icon.itype = itype;
	obj->data.icon.isize = isize;
	obj->data.icon.npoint = npoint;
	if (npoint) {
		obj->data.icon.xpoint = malloc(sizeof(XPoint) * npoint);
		if (obj->data.icon.xpoint == NULL) {
			obj_free(state, obj);
			return 0;
		}
		for (i = 0; i < npoint; i++)
			obj->data.icon.xpoint[i] = xpoint[i];
	} else
		obj->data.icon.xpoint = NULL;
	obj->ascent = 0;	/*XXX*/
	obj->descent = isize;	/*XXX*/
	obj->vertloc = VL_CENTER;
	return 1;
}

static Pixel
obj_image_color(Image *image, Image *bimage, Pixel d, int *inithist)
{
	unsigned int i, j;
	RGBMap rgb;
	int r, g, b;
	static char hist[256];
	byte *p;

	switch (bimage->type) {
	case IBITMAP:
		r = g = b = d ? 0xffff : 0;
		break;
	case IRGB:
		r = bimage->rgb.red[d];
		g = bimage->rgb.green[d];
		b = bimage->rgb.blue[d];
		break;
	case ITRUE:
		r = TRUE_RED(d) << 8;
		g = TRUE_GREEN(d) << 8;
		b = TRUE_BLUE(d) << 8;
		break;
	default:
		return 0;
	}
	if (image->type == ITRUE)
		return RGB_TO_TRUE(r, g, b);

	for (i = 0; i < image->rgb.used; i++) {
		if (image->rgb.red[i] == r &&
		    image->rgb.green[i] == g &&
		    image->rgb.blue[i] == b)
			return i;
	}
	if (i >= image->rgb.size) {
		if (i >= 256) {
			/* search a free slot */
			if (image->rgb.size == 256) {
				if (!*inithist) {
					*inithist = 1;
					memset(hist, 0, sizeof(hist));
					p = image->data;
					for (j = 0; j < image->height; j++)
						for (i = 0; i < image->width; i++)
							hist[*p++] = 1;
				}
				for (i = 0; i < 256; i++) {
					if (hist[i] == 0) {
						hist[i] = 1;
						goto freeslot;
					}
				}
			}
			return -1;
		}
		image->depth = 8;
		newRGBMapData(&rgb, depthToColors(image->depth));
		for (i = 0; i < image->rgb.used; i++) {
			rgb.red[i] = image->rgb.red[i];
			rgb.green[i] = image->rgb.green[i];
			rgb.blue[i] = image->rgb.blue[i];
		}
		rgb.used = i;
		freeRGBMapData(&image->rgb);
		image->rgb = rgb;
	}
  freeslot:
	image->rgb.red[i] = r;
	image->rgb.green[i] = g;
	image->rgb.blue[i] = b;
	if (image->rgb.used < i + 1)
		image->rgb.used = i + 1;
	return i;
}

static Image *
obj_image_trans(Image *image, u_int x, u_int y)
{
	Image *timage;
	unsigned int i, j;
	byte *p, *b;
	Pixel d, n, pd;
	static XColor xcol;
	int pl, bpl;
	int trans;
	u_int bw, bh, bx, by;
	int inithist;

	if (!COMPLEX_BGIMAGE) {
		if (back_color[caching] != xcol.pixel) {
			xcol.pixel = back_color[caching];
			xcol.flags = DoRed|DoGreen|DoBlue;
			XQueryColor(display, colormap, &xcol);
		}
		switch (image->type) {
		case IBITMAP:
		case IRGB:
			image->rgb.red[image->trans] = xcol.red;
			image->rgb.green[image->trans] = xcol.green;
			image->rgb.blue[image->trans] = xcol.blue;
			break;
		case ITRUE:
			d = image->trans;
			n = RGB_TO_TRUE(xcol.red, xcol.green, xcol.blue);
			pl = image->pixlen;
			p = image->data;
			for (j = 0; j < image->height; j++) {
				for (i = 0; i < image->width; i++, p += pl) {
					if (memToVal(p, pl) == d)
						valToMem(n, p, pl);
				}
			}
			break;
		}
		bw = bh = 0;	/* for lint */
		goto end;
	}
	bh = bgpixmap[bgindex].image->height;
	bw = bgpixmap[bgindex].image->width;
	j = 0;
	if (image->type == IBITMAP) {
  expand:
		timage = image;
		if (verbose)
			fprintf(stderr, "obj_image_trans: expanding image\n");
		image = expand(image);
		if (image != timage)
			freeImage(timage);
	}
	pl = image->pixlen;
	p = image->data + image->width * j * pl;
	bpl = bgpixmap[bgindex].image->pixlen;
	pd = -1;
	n = 0;	/* for lint */
	trans = image->trans;
	inithist = 0;
	for ( ; j < image->height; j++) {
		by = (y + j) % bh;
		bx = x % bw;
		b = bgpixmap[bgindex].image->data +
			(bgpixmap[bgindex].image->width * by + bx) * bpl;
		for (i = 0; i < image->width; i++, p += pl, b += bpl, bx++) {
			if (bx == bw) {
				bx = 0;
				b = bgpixmap[bgindex].image->data +
					bgpixmap[bgindex].image->width * by * bpl;
			}
			if ((int)memToVal(p, pl) != trans)
				continue;
			d = memToVal(b, bpl);
			if (d != pd) {
				pd = d;
				n = obj_image_color(image,
						bgpixmap[bgindex].image, d, &inithist);
				if (n == (Pixel)-1)
					goto expand;
			}
			valToMem(n, p, pl);
		}
	}
  end:
	if (verbose) {
		const char *p2;

		switch (image->type) {
		case IBITMAP:	p2 = "bitmap"; break;
		case IRGB:	p2 = "rgb"; break;
		default:	p2 = "true"; break;
		}
		fprintf(stderr, "obj_image_trans: %s: "
			"trans=%d, rgb_used=%d, rgb_size=%d\n",
			p2, image->trans, image->rgb.used, image->rgb.size);
		fprintf(stderr, "  image=%dx%d+%d+%d",
			image->width, image->height, x, y);
		if (COMPLEX_BGIMAGE)
			fprintf(stderr, "  bgpixmap[bgindex].image=%dx%d", bw, bh);
		fprintf(stderr, "\n");
	}
	image->trans = -1;	/* XXX: need recalculation to redraw? */
	return image;
}

static void
obj_draw_image(Drawable target, u_int x, u_int y,
    struct render_object *obj, int page)
{
	Image *image, *timage;
	XImageInfo *ximageinfo;
	XImage *xim;
	int private = mgp_flag & FL_PRIVATE;

	image = obj->data.image.image;
	if (obj->data.image.xzoom != 100.0 || obj->data.image.yzoom != 100.0) {
		timage = image;
		image = zoom(image,
			obj->data.image.xzoom, obj->data.image.yzoom, verbose);
		if (!image) {
			fprintf(stderr, "image zoom (%0.2fx%0.2f) failed in obj_draw_image\n",
				obj->data.image.xzoom, obj->data.image.yzoom);
			exit(1);
		}
		freeImage(timage);
	}
	if (image->trans >= 0)
		image = obj_image_trans(image, x, y);
	obj->data.image.image = image;	/* to free later */
	ximageinfo= imageToXImage(display, screen, visual, depth, image,
				private, 0,0, verbose);
	if (ximageinfo == NULL) {
		fprintf(stderr, "Cannot convert Image to XImage\n");
		cleanup(-1);
	}
	xim = ximageinfo->ximage;
	if (xim->format == XYBitmap)
		XSetBackground(display, gcfore, back_color[caching]);
	XPutImage(display, target, gcfore, xim, 0, 0,
		x, y, xim->width, xim->height);

	if (obj->data.image.zoomonclk) {
		regist_zimage_position(obj, x, y, xim->width, xim->height, page);
	}
	freeXImage(ximageinfo);
}

static void
obj_draw(struct render_state *state, Drawable target, u_int xpos, u_int ypos)
{
	struct render_object *obj;
	int x = 0, y = 0;
	u_long fore;
	u_int code;
	const char *registry;
	XChar2b kch[2];
	u_int isize;
	unsigned int i;
	int lineoff;   /* ypos correction for lines with superscripts */
	XftDraw *dummy;

	/*
	 * very complicated...
	 *
	 *	xpos, ypos	x/y position of the target,
	 *			leftmost and uppermost dot.
	 *	state->ypos	absolute y position in main window.
	 */
	xpos += state->tabxprefix ? state->tabxprefix : state->xprefix;
	xpos += state->xoff;
	ypos += state->yoff;
	fore = fore_color[caching];

	/*
	 * only used with superscript offset for calculating the
	 * exact line position (ypos correction)
	 */
	lineoff = state->maxascent - state->max_lineascent;

	for (obj = state->obj; obj; obj = obj->next) {
#if 0
		x = obj->x + offx;
		y = obj->y + offy;
#else
		x = obj->x;
		switch (obj->vertloc) {
		case VL_BASE:
			y = state->maxascent;
			break;
		case VL_ICENTER:
			if (state->maxflheight){
				y = (state->maxascent + state->maxflheight) / 2;
			} else
				y = (state->maxascent + state->maxdescent) / 2;
			y += (obj->ascent - obj->descent) / 2;
			break;
		case VL_CENTER:
			y = (state->maxascent + state->maxdescent) / 2;
			y += (obj->ascent - obj->descent) / 2;
			break;
		case VL_TOP:
			y = obj->ascent;
			break;
		case VL_BOTTOM:
			y = state->maxascent + state->maxdescent;
			y -= obj->descent;
			break;
		}
		x += xpos;
		y += ypos;
#endif
		switch (obj->type) {
		case O_IMAGE:
			obj_draw_image(target, x, y, obj, state->page);
			break;
		case O_XTFONT:
			y += obj->y;
			set_xrender_color(obj->fore, state->opaque);
			xft_font = xft_setfont(obj->data.xftfont.fontname,
						obj->data.xftfont.size,
						obj->data.xftfont.registry);

			dummy = xft_getdraw(target);
			if (obj->data.xftfont.charset16){
				XftDrawStringUtf8(dummy,
						&xft_forecolor, xft_font,
						x, y - lineoff,
						obj->data.xftfont.data,
						obj->data.xftfont.len);
			} else
				XftDrawString8(dummy,
						&xft_forecolor, xft_font,
						x, y - lineoff,
						obj->data.xftfont.data,
						obj->data.xftfont.len);
			XftDrawDestroy(dummy);
			break;
		case O_XFONT:
			y += obj->y;
			code = obj->data.xfont.code;
			registry = obj->data.xfont.registry;
			(void)x_setfont(obj->data.xfont.xfont,
				obj->data.xfont.csize,
				registry, NULL);
			if (obj->fore != fore) {
				fore = obj->fore;
				XSetForeground(display, gcfore, fore);
			}

#if 1
			/* is it always okay? */
			kch[0].byte1 = (code >> 8) & 0xff;
			kch[0].byte2 = code & 0xff;
			XDrawString16(display, target, gcfore,
					x, y - lineoff, kch, 1);
#else
			if (registry) {
				kch[0].byte1 = (code >> 8) & 0xff;
				kch[0].byte2 = code & 0xff;
				XDrawString16(display, target, gcfore,
					x, y - lineoff, kch, 1);
			} else {
				ch[0] = code & 0xff;
				XDrawString(display, target, gcfore,
					x, y - lineoff, ch, 1);
			}
#endif
			break;
		case O_ICON:
			if (obj->fore != fore) {
				fore = obj->fore;
				XSetForeground(display, gcfore, fore);
			}
			isize = obj->data.icon.isize;
			switch (obj->data.icon.itype) {
			case 1: /* this is box */
				XFillRectangle(display, target, gcfore, x, y,
					isize, isize);
				break;
			case 2: /* this is arc */
				XFillArc(display, target, gcfore, x, y,
					isize, isize, 0, 360 * 64);
				break;
			case 3: case 4: case 5: case 6:
			case 7:
				for (i = 0; i < obj->data.icon.npoint; i++) {
					obj->data.icon.xpoint[i].x += x;
					obj->data.icon.xpoint[i].y += y;
				}
				XFillPolygon(display, target, gcfore,
					obj->data.icon.xpoint,
					obj->data.icon.npoint,
					Convex, CoordModeOrigin);
				break;
			}
			break;
		default:
			break;
		}
	}
	if (fore != fore_color[caching]){
		XSetForeground(display, gcfore, fore_color[caching]);
	}
	/* ASSERT(state->obj == NULL); */
	/* ASSERT(state->objlast == NULL); */
}

static char *
x_fontname(char *buf, int bufsiz, const char *seed, int siz,
    /* already canonicalised */ const char *registry)
{
	int hyphen;
	const char *p;
	char tmp[BUFSIZ];
	char tmp2[BUFSIZ];
	char **fontlist;
	int count;

	if (!registry)
		registry = "iso8859-1";

	if (siz < 0)
		strlcpy(tmp2, "*", sizeof(tmp2));
	else
		sprintf(tmp2, "%d", siz);

	hyphen = 0;
	for (p = seed; *p; p++) {
		if (*p == '-')
			hyphen++;
	}
	switch (hyphen) {
	case 0:
		/* for "a14", "5x8", or such an short names */
		if ((fontlist = XListFonts(display, seed, 1, &count))) {
			XFreeFontNames(fontlist);
			strlcpy(buf, seed, bufsiz);
			break;
		}
		sprintf(tmp, "%s-*-*", seed);
		sprintf(buf, FONT_FORMAT, tmp, tmp2, registry);
		break;
	case 2:
		sprintf(buf, FONT_FORMAT, seed, tmp2, registry);
		break;
	case XLFD_HYPHEN:
		/* as is */
		strlcpy(buf, seed, bufsiz);
		break;
	case 1:	/* should not happen */
		fprintf(stderr, "internal error: invalid seed <%s>\n", seed);
		exit(1);
	}
	if (mgp_flag & FL_VERBOSE) {
		fprintf(stderr, "fontname: seed=<%s> siz=<%d> reg=<%s> "
			"result=<%s>\n",
			seed, siz, registry, buf);
	}
	return buf;
}

static int
x_parsefont(char *xfont, int *pixel, int *truescalable)
{
	char *p;
	int fsize;
	int i;

	/* go toward pixel size */
	p = xfont;
	for (i = 0; *p && i < 7; i++) {
		/* go toward minus sign */
		while (*p && *p != '-')
			p++;
		/* skip minus sign */
		if (*p)
			p++;
	}

	if (!*p)
		return -1;
	fsize = atoi(p);
	if (pixel)
		*pixel = fsize;

	/* skip pixel size */
	while (*p && (isdigit(*p) || *p == '*'))
		p++;
	if (*p == '-')
		p++;
	else
		return -1;

	/* skip point size */
	while (*p && (isdigit(*p) || *p == '*'))
		p++;
	if (*p == '-')
		p++;
	else
		return -1;

	if (truescalable) {
		if (fsize == 0 && (p[0] == '0' || p[0] == '*') && p[1] == '-')
			*truescalable = 1;
		else
			*truescalable = 0;
	}
	return 0;
}

static XFontStruct *
x_setfont(const char *xfont, u_int csize, const char *registry, int *truescalable)
{
	static XFontStruct *xfontstruct;
	int i, fsize;
	char fontstring[BUFSIZ];
#define	FONTTYPEMAX	10	/* number of used fontlist type (in cache) */
#define	FONTLISTMAX	20	/* number of list for specified font type */
#define	FONTALLOWMAX	105	/* % of desired font */
#define	FONTALLOWMIN	90	/* % of desired font */
	char **fontlist, **font;
	u_int error;
	int best, freeindex, count;
	int maxsize, minsize;
	int scalable, tscalable, tsflag;
	static struct {
		char *xlfd;
		char **list;
		int count;
	} fontnames[FONTTYPEMAX];
#define	FONTCACHEMAX	200	/* number of used font type (in cache) */
	static struct {
		char *xfont;
		u_int csize;
		const char *registry;
		char *xlfd;
		XFontStruct *xfontstruct;
	} fonts[FONTCACHEMAX];

	/*
	 * Check font cache first.
	 */
	for (i = 0; i < FONTCACHEMAX; i++) {
		if (!fonts[i].xfontstruct)
			continue;
		if (fonts[i].csize != csize || fonts[i].registry != registry
		 || strcmp(fonts[i].xfont, xfont) != 0) {
			continue;
		}

		XSetFont(display, gcfore, fonts[i].xfontstruct->fid);
		return fonts[i].xfontstruct;
	}

	/*
	 * load new font.
	 */
	if (csize < 5) {
		xfontstruct = XLoadQueryFont(display, "nil2");
		goto gotfont;
	}

	if (verbose) {
		fprintf(stderr, "need font <%s:%d:%s>\n",
			xfont, csize, registry?registry:"NULL");
	}

	/*
	 * Look for the best font possible.
	 * 1. Check for a font that is smaller than the required one.
	 *    By using smaller font, we won't make the screen garbled.
	 * 2. If 1. is impossible, look for slightly larger font than
	 *    the required one.
	 */
	fontlist = NULL;
	freeindex = -1;
	x_fontname(fontstring, sizeof(fontstring), xfont, -1, registry);
	if (verbose)
		fprintf(stderr, "fontstring <%s>\n", fontstring);
	for (i = 0; i < FONTTYPEMAX; i++) {
		if (fontnames[i].xlfd == NULL) {
			if (freeindex < 0)
				freeindex = i;
			continue;
		}
		if (strcmp(fontnames[i].xlfd, fontstring) == 0) {
			fontlist = fontnames[i].list;
			count = fontnames[i].count;
			freeindex = i;
			break;
		}
	}
	if (fontlist == NULL) {
		fontlist = XListFonts(display, fontstring, FONTLISTMAX, &count);
		if (fontlist == NULL)
			return NULL;
		if (freeindex >= 0) {
			if (fontnames[freeindex].xlfd)
				free(fontnames[freeindex].xlfd);
			fontnames[freeindex].xlfd = strdup(fontstring);
			fontnames[freeindex].list = fontlist;
			fontnames[freeindex].count = count;
		}
	}
	error = (u_int)-1;
	best = -1;
	maxsize = csize * FONTALLOWMAX / 100;		/* truncate */
	minsize = (csize * FONTALLOWMIN + 99) / 100;	/* roundup */
	if (verbose)
		fprintf(stderr, "checking %d to %d\n", minsize, maxsize);
	scalable = tscalable = -1;
	if (truescalable)
		*truescalable = 0;
	for (i = 0, font = fontlist; i < count; i++, font++) {
		if (x_parsefont(*font, &fsize, &tsflag) < 0) {
			if (verbose) {
				fprintf(stderr, " [%d] <%s>: nosize\n",
					i, *font);
			}
			continue;
		}
		if (fsize == 0) {
			if (scalable < 0)
				scalable = i;
			if (tsflag) {
				tscalable = i;
				if (truescalable)
					*truescalable = 1;
			}
			if (verbose) {
				fprintf(stderr, " [%d] <%s>: scalable (%d)\n",
					i, *font, tsflag);
			}
			continue;
		} else if (fsize > maxsize || fsize < minsize) {
			continue;
		}
		if ((unsigned int)fsize > csize) {
			fsize = fsize - csize + 100;
					/* penalty for larger font */
		} else
			fsize = csize - fsize;
		if (error > (unsigned int)fsize) {
			error = fsize;
			best = i;
			if (verbose) {
				fprintf(stderr, " [%d] <%s>: score %d best\n",
					i, *font, error);
			}
		} else {
			if (verbose) {
				fprintf(stderr, " [%d] <%s>: score %d\n",
					i, *font, error);
			}
		}
	}
	if (best >= 0) {
		if (verbose) {
			fprintf(stderr, "using best [%d] <%s>\n",
				best, fontlist[best]);
		}
		strlcpy(fontstring, fontlist[best], sizeof(fontstring));
	} else if (scalable >= 0 || tscalable >= 0) {
		x_fontname(fontstring, sizeof(fontstring), xfont, csize,
		    registry);
		if (verbose) {
			fprintf(stderr, "using %sscalable <%s>\n",
				tscalable >= 0 ? "true" : "", fontstring);
		}
	}
	xfontstruct = XLoadQueryFont(display, fontstring);

	if (freeindex < 0)
		XFreeFontNames(fontlist);

	/*
	 * Fill font cache.
	 */
	for (i = 0; i < FONTCACHEMAX; i++) {
		if (!fonts[i].xfontstruct)
			break;
	}
	if (FONTTYPEMAX <= i) {
		/* last resort.  always cache the font recently used */
		i = FONTTYPEMAX - 1;
		XFreeFont(display, fonts[i].xfontstruct);
		free(fonts[i].xfont);
		free(fonts[i].xlfd);
	}
	fonts[i].csize = csize;
	fonts[i].registry = registry;
	fonts[i].xfont = strdup(xfont);
	fonts[i].xlfd = strdup(fontstring);
	fonts[i].xfontstruct = xfontstruct;

  gotfont:
	if (xfontstruct == NULL)
		return NULL;
	XSetFont(display, gcfore, xfontstruct->fid);
	return xfontstruct;
}

static u_int
draw_onechar_x(struct render_state *state, u_int code,
    int x, int y, int size, const char *argregistry, int lastchar)
{
	u_int charlen;
	static XFontStruct *xfontstruct;
	int coffset;
	XCharStruct *cs;
	const char *metricsource;
	const char *seed;
	const char *registry;

	if (code >= 0xa0 && ((!argregistry || !argregistry[0]) && *mgp_charset))
		registry = mgp_charset;
	else
		registry = argregistry;
	seed = x_findseed(state, registry);
	xfontstruct = x_setfont(seed, char_size[caching], registry, NULL);

	if (xfontstruct == NULL)
		return 0;

	if (!xfontstruct->per_char) {
		metricsource = "max_bounds";
		coffset = 0;
		cs = &xfontstruct->max_bounds;
	} else if (!xfontstruct->min_byte1 && !xfontstruct->max_byte1) {
		metricsource = "bytewise offset";
		coffset = (code & 0xff) - xfontstruct->min_char_or_byte2;
		cs = &xfontstruct->per_char[coffset];
	} else {
		metricsource = "wordwise offset";
		coffset = (code & 0xff) - xfontstruct->min_char_or_byte2;
		coffset += (((code >> 8) & 0xff) - xfontstruct->min_byte1)
		    * (xfontstruct->max_char_or_byte2 - xfontstruct->min_char_or_byte2);
		cs = &xfontstruct->per_char[coffset];
	}

	/*
	 * It looks that there are some Japanese X11 fonts with bogus
	 * font metric (cs->width == 0).  This is a workaround for that.
	 * (or is there any mistake in above "coffset" computation?)
	 *
	 * TODO: report the X/Open group, or some other guys, about this.
	 */
	if (!cs->width) {
		if (verbose) {
			fprintf(stderr, "X11 font %s:%d:%s has bogus "
				"font metric for glyph 0x%04x\n"
				"\tcs->width=%d, source=%s, coffset=0x%04x\n",
				seed, char_size[caching], registry?registry:"NULL",
				code, cs->width, metricsource, coffset);
		}
		cs = &xfontstruct->max_bounds;
	}

	draw_line_itemsize(state, cs->ascent, cs->descent, 0);

	/* usually */
	charlen = cs->width;

	/*
	 * for the very first char on the line, the char may goes over the
	 * edge at the lefthand side.  offset the image to the right so that
	 * whole part of the bitmap appears on the screen.
	 * beware the sign-ness of cs->lbearing.
	 */
	if (x + cs->lbearing < 0) {
		x -= cs->lbearing;
		charlen -= cs->lbearing;
	}

	/*
	 * For the last char, make sure that the whole part of the bitmap
	 * appears on the screen.
	 */
	if (lastchar && cs->width < cs->rbearing)
		charlen += cs->rbearing - cs->width;

	obj_new_xfont(state, x, y, size, code, registry);

	return charlen;
}

/*
 * render misc items.
 */
static void
back_gradation(struct render_state *state, struct ctrl_grad *cg0)
{
	struct ctrl_grad cg1;
	struct ctrl_grad *cg;
	int srcwidth, srcheight;
	int dstwidth, dstheight;
	int dir, numcolor;
	float xzoomrate, yzoomrate;
	int hquality, vquality;

	Image *myimage, *image;
	XImageInfo *ximageinfo;
	byte *pic;
	int private = mgp_flag & FL_PRIVATE;
	static Cursor curs;

	/* okay, please wait for a while... */
	if (!curs)
		curs = XCreateFontCursor(display, XC_watch);
	XDefineCursor(display, window, curs);
	XFlush(display);

	/* just for safety */
	memcpy(&cg1, cg0, sizeof(struct ctrl_grad));
	cg = &cg1;

	/* grab parameters */
	dir = cg->ct_direction;
	numcolor = cg->ct_numcolor;
	hquality = b_quality[caching];
	vquality = b_quality[caching];

	/*
	 * XXX zoomflag is too complex to understand.
	 */
	if (!cg->ct_zoomflag) {
		int t;
		int i;

		dstwidth = window_width * cg->ct_width / 100;
		dstheight = window_height * cg->ct_height / 100;
		srcwidth = dstwidth;
		srcheight = dstheight;

		/*
		 * apply quality factor if srcwidth/height are large enough.
		 */
#define TOOSMALLFACTOR 8
		t = srcwidth;
		for (i = 100; hquality < i; i--) {
			t = srcwidth * i / 100;
			if (t < cg->ct_g_colors * TOOSMALLFACTOR)
				break;
		}
		srcwidth = t;

		t = srcheight;
		for (i = 100; vquality < i; i--) {
			t = srcheight * i / 100;
			if (t < cg->ct_g_colors * TOOSMALLFACTOR)
				break;
		}
		srcheight = t;
#undef TOOSMALLFACTOR
	} else {
		dstwidth = window_width;
		dstheight = window_height;
		srcwidth = state->width * cg->ct_width / 100;
		srcheight = state->height * cg->ct_height / 100;

		/*
		 * we don't apply quality factor here, since srcwidth/height
		 * is already smaller than dstwidth/height.
		 */
	}

	xzoomrate = 100.0 * dstwidth / srcwidth;
	yzoomrate = 100.0 * dstheight / srcheight;

	/* performace enhance hack for special case */
	if (dir % 90 == 0) {
		float *q;
		int *p, *r;

		/*
		 * 0 or 180: reduce width
		 * 90 or 270: reduce height
		 */
		p = (dir % 180 == 0) ? &srcwidth : &srcheight;
		q = (dir % 180 == 0) ? &xzoomrate : &yzoomrate;
		r = (dir % 180 == 0) ? &dstwidth : &dstheight;

		/* rely upon use X11 background image tiling. */
		*q = (float) 100.0;
		*p = 3;
		*r = 3;
	}

	if (verbose) {
		fprintf(stderr, "raw: %d,%d qu: %d,%d "
			"dst: %d,%d src: %d,%d zoom: %0.2f,%0.2f\n",
			cg->ct_width, cg->ct_height,
			hquality, vquality,
			dstwidth, dstheight, srcwidth, srcheight,
			xzoomrate, yzoomrate);
	}

	screen = DefaultScreen(display);

	/* make gradation image */
	pic = draw_gradation(srcwidth, srcheight, cg);
	myimage = make_XImage(pic, srcwidth, srcheight);

	if (numcolor < 64)
		myimage = reduce(myimage, numcolor, verbose);

	if (verbose) {
		fprintf(stderr, "background zoomrate: (%0.2f,%0.2f)\n",
			xzoomrate, yzoomrate);
		fprintf(stderr, "background zoom mode %d: "
			"(%d, %d)->(%d, %d)[%d]\n", cg->ct_zoomflag,
			srcwidth, srcheight, dstwidth, dstheight, b_quality[caching]);
	}

	if (xzoomrate != 100.0 || yzoomrate != 100.0) {
		image = myimage;
		myimage = zoom(image, xzoomrate, yzoomrate, verbose);
		if (!image) {
			fprintf(stderr, "image zoom (%0.2fx%0.2f) failed in back_gradataion\n",
				xzoomrate, yzoomrate);
			exit(1);
		}
		freeImage(image);
	}

	ximageinfo = imageToXImage(display, screen, visual, depth, myimage,
		private, 0, 1, verbose);
	if (!ximageinfo) {
		fprintf(stderr, "Cannot convert Image to XImage\n");
		cleanup(-1);
	}

	regist_background_pixmap(ximageinfo, myimage);

	XUndefineCursor(display, window);
	XFlush(display);
}

/* !TODO: move rotation code into some library */
/* rotate image by 90 degrees (counter clockwise) */
static void
rotate_image_p90(Image *image)
{
	unsigned int row, column, pl = image->pixlen;
	unsigned int new_height = image->width, new_width = image->height, new_linelen = new_width * pl;
	byte *src, *tgt, *col_head;
	Pixel d;
	/* allocate buffer for new image */
	byte *rot_data = lmalloc(new_linelen * new_height);

	/* do the rotation */
	for (row = 0, src = image->data, col_head = rot_data + (new_height - 1) * new_linelen;
			row < image->height;
			row++, col_head += pl) {
		for (column = 0, tgt = col_head;
				column < image->width;
				column++, src += pl, tgt -= new_linelen) {
			d = memToVal(src, pl);
			valToMem(d, tgt, pl);
		}
	}

	/* swap to rotated image, exchange height and width
	   and point to rotated data */
	image->height = new_height;
	image->width = new_width;
	lfree(image->data);
	image->data = rot_data;
}

/* rotate image by -90 degrees (clockwise) */
static void
rotate_image_m90(Image *image)
{
	unsigned int row, column, pl = image->pixlen;
	unsigned int new_height = image->width, new_width = image->height, new_linelen = new_width * pl;
	byte *src, *tgt;
	Pixel d;
	/* allocate buffer for new image */
	byte *rot_data = lmalloc(new_linelen * new_height);

	/* do the rotation */
	for (row = 0, src = image->data; row < image->height; row++) {
		for (column = 0, tgt = rot_data + new_linelen - (row + 1) * pl;
				column < image->width;
				column++, src += pl, tgt += new_linelen) {
			d = memToVal(src, pl);
			valToMem(d, tgt, pl);
		}
	}

	/* swap to rotated image, exchange height and width
	   and point to rotated data */
	image->height = new_height;
	image->width = new_width;
	lfree(image->data);
	image->data = rot_data;

	return;
}

/* rotate image by 180 degrees */
static void
rotate_image_180(Image *image)
{
	unsigned int row, column, pl = image->pixlen;
	unsigned int new_height = image->height, new_width = image->width, new_linelen = new_width * pl;
	byte *src, *tgt;
	Pixel d;
	/* allocate buffer for new image */
	byte *rot_data = lmalloc(new_linelen * new_height);

	/* do the rotation */
	for (row = 0, src = image->data; row < image->height; row++) {
		for (column = 0, tgt = rot_data + (new_height - row) * new_linelen - pl;
				column < image->width;
				column++, src += pl, tgt -= pl) {
			d = memToVal(src, pl);
			valToMem(d, tgt, pl);
		}
	}

	/* swap to rotated image, exchange height and width
	   and point to rotated data */
	image->height = new_height;
	image->width = new_width;
	lfree(image->data);
	image->data = rot_data;

	return;
}

static void
image_load(struct render_state *state, char *filename,
    int numcolor, int ximagesize, int yimagesize,
    int backflag, int zoomflag, int centerflag,
    int raise2, int rotate, int zoomonclk)
{
	Image *image, *myimage;
	XImageInfo *ximageinfo;
	u_int image_posx;
	int width, height;
	float xzoomrate, yzoomrate;
	int	private = mgp_flag & FL_PRIVATE;
	static Cursor curs;
	Imlib_Image *imimage;

	if (!caching){
		if (!curs)
			curs = XCreateFontCursor(display, XC_watch);
		XDefineCursor(display, state->target, curs);
		XFlush(display);
	}

	if ((myimage = loadImage(filename)) == NULL) {
		fprintf(stderr, "failed to load image file\n");
		cleanup(-1);
	}
	switch (rotate) {
		case 0:
			/* Do nothing */
			break;

		case -90:
		case 270:
			rotate_image_m90(myimage);
			break;

		case 90:
			rotate_image_p90(myimage);
			break;

		case -180:
		case 180:
			rotate_image_180(myimage);
			break;

		default:
			fprintf(stderr, "rotation by %d degrees not supported.\n", rotate);
			cleanup(-1);
	}
	width = myimage->width;
	height = myimage->height;

	if (myimage->depth == 1 && myimage->trans < 0) {
		XColor xc;

		xc.flags = DoRed | DoGreen | DoBlue;
		xc.pixel = fore_color[caching];
		XQueryColor(display, colormap, &xc);
		*(myimage->rgb.red + 1) = xc.red;
		*(myimage->rgb.green + 1) = xc.green;
		*(myimage->rgb.blue + 1) = xc.blue;
		myimage->trans = 0;	/* call obj_image_trans() later */
	}

	if (numcolor)
		myimage = reduce(myimage, numcolor, verbose);

	if (!ximagesize) ximagesize = 100;
	if (!yimagesize) yimagesize = 100;
	xzoomrate = (float) ximagesize;
	yzoomrate = (float) yimagesize;
	image_zoomratio(state, &xzoomrate, &yzoomrate, zoomflag, width, height);

	if (backflag) {
		if (xzoomrate != 100 || yzoomrate != 100) {
			image = myimage;
			myimage = zoom(image, xzoomrate, yzoomrate, verbose);
			if (!image) {
				fprintf(stderr, "image zoom (%fx%f) failed in image_load\n",
					xzoomrate, yzoomrate);
				exit(1);
			}
			freeImage(image);
		}

		ximageinfo= imageToXImage(display, screen, visual, depth,
				myimage, private, 0, 1, verbose);
		if (ximageinfo == NULL) {
			fprintf(stderr, "Cannot convert Image to XImage\n");
			cleanup(-1);
		}
		regist_background_pixmap(ximageinfo, myimage);
		goto end;
	}

#if 1  /* by h.kakugawa@computer.org */
	switch(valign){
	case VL_TOP:
		draw_line_itemsize(state,
				   (height * raise2) * yzoomrate / 10000,
				   height * (100 + raise2) * yzoomrate / 10000, 0);
		break;
	case VL_BOTTOM:
		draw_line_itemsize(state,
				   height * (100 + raise2) * yzoomrate / 10000,
				   (height * raise2) * yzoomrate / 10000, 0);
		break;
	case VL_CENTER:
		draw_line_itemsize(state,
				   height * (100 + raise2) * yzoomrate / 20000,
				   height * (100 + raise2) * yzoomrate / 20000, 0);
		break;
	}
#else
	switch(valign){
	case VL_TOP:
		draw_line_itemsize(state, 0, height * yzoomrate / 100, 0);
		break;
	case VL_BOTTOM:
		draw_line_itemsize(state, height * yzoomrate / 100, 0, 0);
		break;
	case VL_CENTER:
		draw_line_itemsize(state, height * yzoomrate / 200,
			height * yzoomrate / 200, 0);
		break;
	}
#endif

	if (centerflag)
		image_posx = char_size[caching] / 2 - (width * xzoomrate / 100) / 2;
	else
		image_posx = 0;

	imimage = search_imdata(filename);
	obj_new_image2(state, state->linewidth + image_posx,
		- height * yzoomrate / 100 / 2,
		myimage, xzoomrate, yzoomrate, imimage, zoomonclk);

	state->linewidth += (width * xzoomrate / 100);
end:
	if (!caching){
		XUndefineCursor(display, state->target);
		XFlush(display);
	}
}

static void
image_load_ps(struct render_state *state, char *filename,
    int numcolor, int ximagesize, int yimagesize,
    int backflag, int zoomflag, int centerflag,
    int raise2, int rotate, int zoomonclk)
{
	int x1, y1v, x2, y2;
	static Cursor curs;
	char fullname[MAXPATHLEN];
	char *imagefile;
	int width, height;
	float xzoom, yzoom, zratio;
	char *p;

	/* wait for a while, please. */
	if (!curs)
		curs = XCreateFontCursor(display, XC_watch);
	XDefineCursor(display, window, curs);
	XFlush(display);

	if (findImage(filename, fullname) < 0) {
		fprintf(stderr, "image file %s not found in path\n", filename);
		cleanup(-1);
	}
	if (ps_boundingbox(fullname, &x1, &y1v, &x2, &y2) < 0) {
		/* error message generated in ps_boundingbox() */
		cleanup(-1);
	}

	width = x2 - x1 + 1;
	height = y2 - y1v + 1;
	xzoom = (float) ximagesize;
	yzoom = (float) yimagesize;
	image_zoomratio(state, &xzoom, &yzoom, zoomflag, width, height);
	width = width * xzoom / 100;
	height = height * yzoom / 100;

	if (zoomonclk)
		zratio = (float) zoomonclk / 100.0 * window_width / width;
	else
		zratio = 1.0;
	imagefile = epstoimage(fullname, x1, y1v,
		width * zratio, height * zratio, xzoom * zratio, yzoom * zratio);
	if (imagefile == NULL) {
		fprintf(stderr, "WARN: cannot generate %s file from %s\n",
			gsdevice, filename);
		XUndefineCursor(display, window);
		XFlush(display);
		return;
	}

	if (mgp_flag & FL_VERBOSE) {
		fprintf(stderr, "image_load_ps: %s: %s file = %s\n",
			filename, gsdevice, imagefile);
	}
	image_load(state, imagefile, numcolor, 100.0 /zratio, 100.0/zratio, backflag,
		Z_NORMAL | (Z_NORMAL << Z_YSHIFT), centerflag, raise2, rotate, zoomonclk);
	/* XXX: unlink imagefile in /tmp */
	if ((p = strrchr(imagefile, '/')) != NULL)
		p++;
	else
		p = imagefile;
	if (strncmp(p, ".gscache", sizeof(".gscache") - 1) != 0)
		unlink(imagefile);

	if (!backflag && numcolor >= 0)
		image_setcolor(state);
}

void
timebar(struct render_state *state)
{
	int pos, n, p, barlen;
	GC pgc;

	if (t_start == 0 || tbar_mode == 0 || caching)
		return;

	pos = (window_width - 2) * (state->page - 1) / (maxpage - 1);
	p = time(NULL) - t_start;
	barlen = window_width - window_width * p / t_fin / 60;

	if (window_width / 2 < barlen)
		pgc = gcgreen;
	else if (window_width / 3 < barlen)
		pgc = gcyellow;
	else
		pgc = gcred;
	if (barlen > 0) {
		XClearArea(display, state->target, 0, window_height - 2,
			window_width, 2, 0);
		XFillRectangle(display, state->target, pgc,
			window_width - barlen, window_height - 1, barlen, 1);
		XFillRectangle(display, state->target, pgc,
			pos, window_height - 5, 2, 5);
	} else if (barlen < 0) {
		barlen = - barlen;
		n = p / t_fin / 60;
		if (n > window_height - 1)
			n = window_height - 1;
		if (n)
			XFillRectangle(display, state->target, gcred,
				0, window_height - n,
				barlen, n);
		XClearArea(display, state->target, 0, window_height - (n + 2),
			window_width, n + 2, 0);
		XFillRectangle(display, state->target, gcred,
			0, window_height - (n + 1),
			barlen % window_width, n + 1);
		XFillRectangle(display, state->target, gcred,
			pos, window_height - (n + 1 + 4),
			2, 5);
	}
}

static const struct icon_point {
	XPoint xpoint[4];
	unsigned int point_num;
} icon_point[] = {{ {{1, 0}, {0, 2}, {2, 2}, {0, 0}}, 3 },
		  { {{0, 0}, {2, 0}, {1, 2}, {0, 0}}, 3 },
		  { {{0, 0}, {0, 2}, {2, 1}, {0, 0}}, 3 },
		  { {{2, 0}, {2, 2}, {0, 1}, {0, 0}}, 3 },
		  { {{1, 0}, {0, 1}, {1, 2}, {2, 1}}, 4 }};

static void
process_icon(struct render_state *state, struct ctrl *cp)
{
	u_int i, icon_type, icon_size, icon_x, icon_y, index2;
	u_long tmp_color;
	XPoint xpoint[4];

	for (i = 0; icon_words[i].ctl_strlen != 0; i++) {
		if (!strncasecmp(cp->ctic_value, icon_words[i].ctl_string,
			strlen(cp->ctic_value))) {
				break;
		}
	}

	icon_type = icon_words[i].ctl_type; /* may be 0 */
	icon_size = char_size[caching] * cp->ctic_size / 100;

	switch(icon_type){
	case 0:
		/* this is image */
		icon_x = icon_size * 100 / state->width;
		icon_y = icon_size * 100 / state->height;
		if (icon_x == 0) icon_x = 1;
		if (icon_y == 0) icon_y = 1;
		tmp_color = fore_color[caching];
		fore_color[caching] = cp->ctic_color;
		image_load(state, cp->ctic_value, 0, icon_x, icon_y, 0, 0, 1, 0, 0, 0);
		fore_color[caching] = tmp_color;
		break;

	case 1:
		/* this is box */
		obj_new_icon(state,
			state->linewidth + char_size[caching]/2 - icon_size/2,
			POSY(icon_size), icon_type, icon_size,
			cp->ctic_color, 0, NULL);
		state->linewidth += char_size[caching];
		break;

	case 2:
		/* this is arc */
		obj_new_icon(state,
			state->linewidth + char_size[caching]/2 - icon_size/2,
			POSY(icon_size), icon_type, icon_size,
			cp->ctic_color, 0, NULL);
		state->linewidth += char_size[caching];
		break;

	case 3:
	case 4:
	case 5:
	case 6:
	case 7:
		index2 = icon_type - 3;
		icon_x = state->linewidth + (char_size[caching] - icon_size) / 2;
#if 0
		icon_y = POSY(icon_size);
#else
		icon_y = 0;
#endif
		for (i = 0; i < icon_point[index2].point_num; i ++){
			xpoint[i].x = icon_x +
				icon_point[index2].xpoint[i].x * icon_size / 2;
			xpoint[i].y = icon_y +
				icon_point[index2].xpoint[i].y * icon_size / 2;
		}
		obj_new_icon(state, 0, 0, icon_type, icon_size,
			cp->ctic_color, icon_point[index2].point_num, xpoint);
		state->linewidth += char_size[caching];
		break;

	default:
		break;
	}

	cp = NULL;
	state->brankline = 0;
}

static void
draw_bar(struct render_state *state, struct ctrl *cp)
{
	u_int width, swidth, st, len;
	XColor col, scol;
	static GC gcbar, gcsbar;
	static u_long prevcolor = -1;

	if (!gcbar) {
		gcbar = XCreateGC(display, state->target, 0, 0);
		XSetFunction(display, gcbar, GXcopy);
		gcsbar = XCreateGC(display, state->target, 0, 0);
		XSetFunction(display, gcsbar, GXcopy);
	}
	col.pixel = cp->ctb_color;
	if (col.pixel == (unsigned int)-1)
		col.pixel = fore_color[caching];
	if (col.pixel != prevcolor) {
		prevcolor = col.pixel;
		col.flags = DoRed|DoGreen|DoBlue;
		XQueryColor(display, colormap, &col);
		scol.red   = col.red   / 2;
		scol.green = col.green / 2;
		scol.blue  = col.blue  / 2;
		if (!XAllocColor(display, colormap, &scol))
			scol.pixel = col.pixel;
		XSetForeground(display, gcbar, col.pixel);
		XSetForeground(display, gcsbar, scol.pixel);
	}
	width = cp->ctb_width * state->height / 1000;
	swidth = width / 2;
	width -= swidth;
	st = cp->ctb_start * state->width / 100 + state->xoff;
	len = cp->ctb_length * state->width / 100;
	XFillRectangle(display, state->target, gcbar, st, state->ypos + state->yoff, len, width);
	XFillRectangle(display, state->target, gcsbar, st, state->ypos + state->yoff + width, len, swidth);

	state->ypos += width + swidth + VERT_GAP(char_size[caching]) / 2;
	if (state->maxascent < width + swidth)
		state->maxascent = width + swidth;
	state->brankline = 0;
}

static void
process_system(struct render_state *state, struct ctrl *cp)
{
	pid_t pid;
	unsigned int i;
	char **argv;
	char buf[BUFSIZ];

	if (state->repaint) {
		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "WARN: %%system directive skipping during repaint of same page\n");
		}
		return; /* don't relaunch on repaint */
	}

	if (mgp_flag & FL_NOFORK) {
		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "WARN: %%system ");
			for (i = 0; i < cp->cta_argc; i++) {
				fprintf(stderr, "%c%s", (i == 0) ? '"' : ' ',
					cp->cta_argv[i]);
			}
			fprintf(stderr, "\": directive skipped\n");
		}
		return;
	}

	if (checkchild(cp) != (pid_t)-1)
		return;	/*already running*/

	/*
	 * edit argument.
	 * if we have X11 geometry string
	 */
	argv = (char **)cp->cta_argv;
	for (i = 0; i < cp->cta_argc; i++) {
		if (*(argv[i]) == '%')
			break;
	}
	if (i < cp->cta_argc) {
		char *p;
		char *q;
		int myxpos, myypos;
		u_int rootxsiz, rootysiz;
		u_int xsiz, ysiz;
		int xloc, yloc;
		int mode;

	    {
		XWindowAttributes wa;
		Window junkwin;
		int junk;

		XGetWindowAttributes(display, window, &wa);
		XTranslateCoordinates(display, window, wa.root,
			-wa.border_width, -wa.border_width,
			&myxpos, &myypos, &junkwin);
		XGetGeometry(display, wa.root, &junkwin, &junk, &junk,
			&rootxsiz, &rootysiz, (void *)&junk, (void *)&junk);
	     }

		argv = (char **)malloc((cp->cta_argc + 1) * sizeof(char *));
		memcpy(argv, cp->cta_argv, (cp->cta_argc + 1) * sizeof(char *));
		p = argv[i];
		p++;	/*drop percent char*/
		q = buf;
		*q = '\0';

		mode = XParseGeometry(p, &xloc, &yloc, &xsiz, &ysiz);
		if (mode == 0)
			goto fail;
		if ((mode & WidthValue) && (mode & HeightValue)) {
			sprintf(q, "%dx%d", xsiz * state->width / 100,
				ysiz * state->height / 100);
			q += strlen(q);
		}
		if ((mode & XValue) && (mode & YValue)) {
			xloc = xloc * state->width / 100;
			yloc = yloc * state->height / 100;
			if (mode & XNegative)
				xloc = rootxsiz - myxpos + state->width - xloc;
			else
				xloc += myxpos;
			if (mode & YNegative)
				yloc = rootysiz - myypos + state->height - yloc;
			else
				yloc += myypos;
			sprintf(q, "+%d+%d", xloc + state->xoff, yloc + state->yoff);
		}

		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "relative geometry: "
				"%s (presentation %dx%d+%d+%d)\n",
				argv[i], state->width, state->height,
				myxpos, myypos);
			fprintf(stderr, "\t-> %s\n", buf);
		}
		argv[i] = buf;

		if (0) {
fail:
			if (mgp_flag & FL_VERBOSE) {
				fprintf(stderr,
					"relative geometry: %s failed\n",
					argv[i]);
			}
		}
	}
	pid = fork();
	if (pid < 0) {
		perror("fork");
		cleanup(-1);
	} else if (pid == 0) {
		execvp(argv[0], argv);
		perror(argv[0]);
		_exit(1);
	}

	if (!cp->cta_flag)	/*will be purged at the end of page*/
		regchild(pid, cp, -1, state->page);
	else
		regchild(pid, cp, -1, cp->cta_flag);
}

static void
process_xsystem(struct render_state *state, struct ctrl *cp)
{
	pid_t pid;
	unsigned int i;
	u_int dumint;
	int xloc, yloc;
	u_int xsiz, ysiz;
	char **argv;
	char buf[BUFSIZ];
	Window window_id, dumwin;

	if (state->repaint) {
		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "WARN: %%system directive skipping during repaint of same page\n");
		}
		return; /* don't relaunch on repaint */
	}

	if (mgp_flag & FL_NOFORK) {
		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "WARN: %%system ");
			for (i = 0; i < cp->cta_argc; i++) {
				fprintf(stderr, "%c%s", (i == 0) ? '"' : ' ',
					cp->cta_argv[i]);
			}
			fprintf(stderr, "\": directive skipped\n");
		}
		return;
	}

	/*
	 * edit argument.
	 * if we have X11 geometry string
	 */
	argv = (char **)cp->cta_argv;
	for (i = 0; i < cp->cta_argc; i++) {
		if (!strncmp(argv[i], "-geom", 5))
			break;
	}
	i ++;
	if (i < cp->cta_argc) {
		char *p;
		char *q;
		int mode;

		argv = (char **)malloc((cp->cta_argc + 1) * sizeof(char *)); /* XXX seems to be never freed */
		memcpy(argv, cp->cta_argv, (cp->cta_argc + 1) * sizeof(char *));
		p = argv[i];
		if (*p == '%') p++;	/*drop percent char*/
		q = buf;
		*q = '\0';

		mode = XParseGeometry(p, &xloc, &yloc, &xsiz, &ysiz);
		if (mode == 0)
			goto fail;
		if ((mode & WidthValue) && (mode & HeightValue)) {
			xsiz = xsiz * state->width / 100;
			ysiz = ysiz * state->height / 100;
			sprintf(q, "%dx%d", xsiz, ysiz);
			q += strlen(q);
		}
		/* make window raise outside of display */
		sprintf(q, "+%d+%d", DisplayWidth(display, DefaultScreen(display)),
						DisplayHeight(display, DefaultScreen(display)));

		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "relative geometry: "
				"%s (presentation %dx%d+%d+%d)\n",
				argv[i], state->width, state->height,
				xloc, yloc);
			fprintf(stderr, "\t-> %s\n", buf);
		}
		argv[i] = buf;

		if (0) {
fail:
			if (mgp_flag & FL_VERBOSE) {
				fprintf(stderr,
					"relative geometry: %s failed\n",
					argv[i]);
			}
		}
	} else {
		char geom_arg1[] = {"-geometry"};
		char geom_arg2[512];

		sprintf(geom_arg2, "+%d+%d", DisplayWidth(display,
			DefaultScreen(display)),
			DisplayHeight(display, DefaultScreen(display)));

		argv[cp->cta_argc] = geom_arg1;
		argv[cp->cta_argc+1] = geom_arg2;
		/*
		** XXX argv is now not generally NULL-terminated
		** the maximal allowed size of argv is ganatied to be
		** argc+2 so no NULL can appended
		*/
	}

	if ((window_id = checkchildwin(cp)) != (Window)-1)
		goto finish;	/*already running*/

	if (checkchild(cp) != (pid_t)-1)
		return;	/*already running*/

	pid = fork();
	if (pid < 0) {
		perror("fork");
		cleanup(-1);
	} else if (pid == 0){
		usleep(EXEC_DELAY);
		execvp(argv[0], argv);
		perror(argv[0]);
		_exit(1);
	}

	window_id = search_child_window();

	if (!cp->cta_flag)	/*will be purged at the end of page*/
		regchild(pid, cp, window_id, state->page);
	else
		regchild(pid, cp, window_id, cp->cta_flag);

	if (window_id != (Window)-1)
		reparent_child_window(window_id, window_width, window_height);
	else {
		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "WARN: %%xsystem cannot find child window:");
			for (i = 0; i < cp->cta_argc; i++) {
				fprintf(stderr, "%c%s", (i == 0) ? '"' : ' ',
					cp->cta_argv[i]);
			}
			fprintf(stderr, "\"\n");
		}
		return;
	}

finish:
	XGetGeometry(display, window_id, &dumwin,
		&xloc, &yloc, &xsiz, &ysiz, &dumint, &dumint);
	state->linewidth = xsiz;
	xloc = set_position(state) + state->xoff
		+ (state->tabxprefix ? state->tabxprefix : state->xprefix);
	yloc = state->ypos + state->yoff;
	XMoveWindow(display, window_id, xloc, yloc);
	state->ypos += ysiz;

#if 0 /* not implemented yet */
	state->linewidth += xsiz;
	state->maxascent += ysiz;
#endif
}

/*
 * tsystem does mostly the same like xsystem, but identifies the created
 * window by its name
 *
 * this hack is done because at some windowmanagers occures additional
 * xreparentevents, which cause xsystem to fail
 *
 * it is possible, that the title of some applications is reseted, than
 * tsystem will fail
 */
static void
process_tsystem(struct render_state *state, struct ctrl *cp)
{
	pid_t pid;
	unsigned int i, argc, dumint;
	int xloc, yloc;
	u_int xsiz, ysiz;
	char **argv;
	char buf[BUFSIZ];
	char title_arg1[] = "-title";
	char title_arg2[BUFSIZ];
	static unsigned int magicCnt=0;
	Window window_id, dumwin;

	if (mgp_flag & FL_NOFORK) {
		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "WARN: %%system ");
			for (i = 0; i < cp->cta_argc; i++) {
				fprintf(stderr, "%c%s", (i == 0) ? '"' : ' ',
					cp->cta_argv[i]);
			}
			fprintf(stderr, "\": directive skipped\n");
		}
		return;
	}

	/*
	 * edit argument.
	 * allways copy the argument vector, for adding -title magictitle
	 * it's assumed, that there is not -title in the argument vector
	 */
	argc=cp->cta_argc;
	argv = (char **)malloc((argc + 5) * sizeof(char *));
		/* +5 for NULL, title and potentally geometry */
	memcpy(argv, cp->cta_argv, (argc + 1) * sizeof(char *));

	/*
	 * search for X11 geometry string
	 */
	for (i = 0; i < argc; i++) {
		if (!strncmp(argv[i], "-geom", 5))
			break;
	}
	i ++;
	if (i < argc) {
	/*
	 * we have X11 geometry string
	 */
		char *p;
		char *q;
		int mode;

		p = argv[i];
		if (*p == '%') p++;	/*drop percent char*/
		q = buf;
		*q = '\0';

		mode = XParseGeometry(p, &xloc, &yloc, &xsiz, &ysiz);
		if (mode == 0)
			goto fail;
		if ((mode & WidthValue) && (mode & HeightValue)) {
			xsiz = xsiz * state->width / 100;
			ysiz = ysiz * state->height / 100;
			sprintf(q, "%dx%d", xsiz, ysiz);
			q += strlen(q);
		}
		/* make window raise outside of display */
		/* XXX potentially overflow, but BUFSIZ should be alway large enough*/
		sprintf(q, "+%d+%d", DisplayWidth(display, DefaultScreen(display)),
					DisplayHeight(display, DefaultScreen(display)));

		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "relative geometry: "
				"%s (presentation %dx%d+%d+%d)\n",
				argv[i], state->width, state->height,
				xloc, yloc);
			fprintf(stderr, "\t-> %s\n", buf);
		}
		argv[i] = buf;

		if (0) {
fail:
			if (mgp_flag & FL_VERBOSE) {
				fprintf(stderr,
					"relative geometry: %s failed\n",
					argv[i]);
			}
		}
	} else {
		/*
		 * we do not have X11 geometry string
		 */
		char geom_arg1[] = "-geometry";
		char geom_arg2[512];

		sprintf(geom_arg2, "+%d+%d", DisplayWidth(display,
			DefaultScreen(display)),
			DisplayHeight(display, DefaultScreen(display)));

		argv[argc] = geom_arg1;
		argv[argc+1] = geom_arg2;
		argc += 2;
	}

	/*
	 * adding magic title and incrementing magicCnt
	 * guaranteeing the NULL-termination of argv
	 */
	snprintf(title_arg2, BUFSIZ, "magictitle %u", magicCnt++);
	argv[argc] = title_arg1;
	argv[argc+1] = title_arg2;
	argv[argc+2] = NULL;
	argc += 2; /* seems not to be nessesary */

	if ((window_id = checkchildwin(cp)) != (Window)-1)
		goto finish;	/*already running*/

	if (checkchild(cp) != (pid_t)-1) {
		free(argv);
		return;	/*already running*/
	}

	/*
	 * using vfork() to first run the child
	 */
	pid = vfork();
	if (pid < 0) {
		perror("fork");
		cleanup(-1);
	} else if (pid == 0){
		execvp(argv[0], argv);
		perror(argv[0]);
		_exit(1);
	}

	window_id = tsearch_child_window(title_arg2);

	if (!cp->cta_flag)	/*will be purged at the end of page*/
		regchild(pid, cp, window_id, state->page);
	else
		regchild(pid, cp, window_id, cp->cta_flag);

	if (0 == window_id) {
		if (mgp_flag & FL_VERBOSE) {
			fprintf(stderr, "WARN: %%tsystem can not find child window:");
			for (i = 0; i < cp->cta_argc; i++) {
				fprintf(stderr, "%c%s", (i == 0) ? '"' : ' ',
					cp->cta_argv[i]);
			}
			fprintf(stderr, "\"\n");
		}
		return;
	}
finish:
	{
		Window root, par, *child;
		int newxloc, newyloc;
		unsigned int nchild;

		XGetGeometry(display, window_id, &dumwin,
				&xloc, &yloc, &xsiz, &ysiz, &dumint, &dumint);
		XQueryTree(display, window_id, &root, &par, &child, &nchild);
		if(child) XFree(child);

		state->linewidth = xsiz;
		newxloc = set_position(state) + state->xoff
			+ (state->tabxprefix ? state->tabxprefix : state->xprefix);
		newyloc = state->ypos + state->yoff;
		while((par!=window) || (xloc != newxloc)) {
			/*
			 * this hack should correct not moved windows
			 * if found, that XMoveWindow, XReparentWindow returns success,
			 * but the window is sometimes not moved etc in ion
			 */
			XReparentWindow(display, window_id, window, newxloc, newyloc);
			XGetGeometry(display, window_id, &dumwin,
					&xloc, &yloc, &xsiz, &ysiz, &dumint, &dumint);
			XQueryTree(display, window_id, &root, &par, &child, &nchild);
			if(child) XFree(child);
		}
	}

	state->ypos += ysiz;
	free(argv);

#if 0 /* not implemented yet */
	state->linewidth += xsiz;
	state->maxascent += ysiz;
#endif
}

Window
search_child_window(void)
{
	XEvent e;
	int	fd, found = 0;
	fd_set fdset, dumfdset, dumfdset2;
	struct timeval timeout;

	fd = ConnectionNumber(display);
	/* waiting for 2 second */
	timeout.tv_sec = 2;
	timeout.tv_usec = 0;

	/* get all client's ReparentNotify event */
	XSelectInput(display, DefaultRootWindow(display),
		SubstructureNotifyMask);

	while (!found) {
		while (XEventsQueued(display, QueuedAfterFlush) > 0) {
			XNextEvent(display, &e);
			if (e.type == ReparentNotify){
				found = 1;
				break;
			}
		}
		if (found) break;
		FD_ZERO(&fdset);
		FD_SET(fd, &fdset);
		FD_ZERO(&dumfdset);
		FD_ZERO(&dumfdset2);
		if (!select(fd+1, &fdset, &dumfdset, &dumfdset2, &timeout))
			break;
	}

	XSelectInput(display, DefaultRootWindow(display), NoEventMask);

	if (found == 1)
		return e.xreparent.window;
	else
		return (Window)-1;
}

/*
** looks for a window with the specified name
** return (Window)0 if not found
*/
Window
tsearch_child_window(const char *name)
{
	/* 100 ms between two searches for the specified window */
#define WAITTIME 100000
	/* maximal wait time = 1 minute */
#define WAITCYCLES 60000000 / WAITTIME
	int maxWait=WAITCYCLES;
	Window w=0;

	while (maxWait--) {
		if((w = getNamedWindow(name, DefaultRootWindow(display))))
			break;
		usleep(WAITTIME);
	}
	return w;
#undef WAITCYCLES
#undef WAITTIME
}

Window
getNamedWindow(const char *name, Window top)
{
	Window w=0;
	Window *child;
	Window dum;
	unsigned int nchild,i;
	char *w_name;

	if (XFetchName(display, top, &w_name) && (!strcmp(w_name, name)))
		return top;

	if (!XQueryTree(display, top, &dum, &dum, &child, &nchild))
		return (Window)0;

	for (i=0; i<nchild; ++i) {
		if ((w = getNamedWindow(name, child[i])))
			break;
	}
	if (child)
		XFree((char *)child);
	return w;
}

void
reparent_child_window(Window child_window, int x, int y)
{
	Window	dummyroot, *dummywin;
	Window	target, parent;
	u_int	dumint;

	target = child_window;
	while (1) {
		XQueryTree(display, target, &dummyroot, &parent, &dummywin,
			&dumint);
		if (parent == dummyroot)
			break;
		XFree(dummywin);
		target = parent;
	}
	XReparentWindow(display, child_window, window, x, y);
}

void
draw_reinit(struct render_state *state)
{
	/* invalidate the background image cache */

	bg_ctl_last = bg_ctl_cache = NULL;
	x_registerseed(state, NULL, NULL);
}

static char *
epstoimage(const char *epsfile, int x, int y,
    int width, int height, volatile int xzoom, volatile int yzoom)
{
	volatile int fd;
	volatile pid_t pid = 0;
	int i, j, status, pfd[3][2];
	FILE *fp;
	pid_t gspid;
	char *cp;
	int scale = 1;
	struct stat stbuf;
	char geom[32], device[64], scalebuf[32];
	static char imagefile[MAXPATHLEN];
	void (*sigpipe_handler)(int);

	fd = -1;
	for (i = 0; i < 3; i++) {
		for (j = 0; j < 2; j++)
			pfd[i][j] = -1;
	}
	strlcpy(imagefile, epsfile, sizeof(imagefile));
	if ((cp = strrchr(imagefile, '/')) != NULL)
		cp++;
	else
		cp = imagefile;
	sprintf(cp, ".gscache.%s.%dx%d", epsfile + (cp - imagefile),
		width, height);
	if (verbose)
		fprintf(stderr, "gs cache filename: %s\n", imagefile);

	/* check if we got any cached image file already. */
	if (stat(imagefile, &stbuf) == 0) {
		time_t cachetime;
		off_t cachesize;

		cachetime = stbuf.st_mtime;
		cachesize = stbuf.st_size;
		if (stat(epsfile, &stbuf) == 0) {
			if (stbuf.st_mtime < cachetime && cachesize > 0) {
				if (verbose) {
					fprintf(stderr, "gs cache valid, "
						"using it \n");
				}
				return imagefile;
			}
		}
		if (verbose) {
			fprintf(stderr, "gs cache looks older than source, "
				"generate again\n");
		}
	} else {
		if (verbose) {
			fprintf(stderr, "gs cache not found, convert eps\n");
		}
	}

	if (verbose)
		fprintf(stderr, "converting eps file...\n");

	/* convert eps file into readable form. */
	sprintf(device, "-sDEVICE=%s", gsdevice);

	/*
	 * a suffix of +scale in the device tipe means produce a larger
	 * image that can be scaled later for better antialiasing.
	 */
	if ((cp = strchr(device, '+')) != NULL) {
		*cp++ = '\0';
		scale = atoi(cp);
		if (scale <= 0)
			scale = 2;
		xzoom *= scale;
		yzoom *= scale;
		width *= scale;
		height *= scale;
	}
	if (width == 0 || height == 0) {
		fprintf(stderr, "WARN: epstoimage: scale=%d, xzoom=%d, "
			"yzoom=%d, width=%d, height=%d\n",
			scale, xzoom, yzoom, width, height);
		return NULL;
	}
	if (scale != 1)
		sprintf(scalebuf, "%f", 1. / (double)scale);
	sprintf(geom, "-g%dx%d", width, height);

	/* generate cache file. */
	fd = open(imagefile, O_RDWR|O_CREAT|O_TRUNC, 0600);
	if (fd < 0) {
		const char *ccp;

		/* last resort: generate output onto /tmp. */
		if ((ccp = getenv("TMPDIR")) == NULL)
			ccp = "/tmp";
		if (verbose) {
			fprintf(stderr, "could not write to \"%s\", using "
				"%s\n", imagefile, ccp);
		}
		strlcpy(imagefile, ccp, sizeof(imagefile));
		strlcat(imagefile, "/mgp.XXXXXXXX", sizeof(imagefile));
		if ((fd = mkstemp(imagefile)) < 0) {
			perror(imagefile);
			return NULL;
		}
	}
	if (scale != 1) {
		if (pipe(pfd[2]) < 0) {
			perror("pipe");
			goto error;
		}
		if ((pid = vfork()) == 0) {
			close(pfd[2][1]);
			dup2(pfd[2][0], 0); close(pfd[2][0]);
			dup2(fd, 1); close(fd);

			if (verbose)
				fprintf(stderr, "epstoimage: \"pnmdepth 256\"\n");
			close(2); /* XXX suppress message */
			execlp("pnmdepth", "pnmdepth", "255", NULL);
			perror("pnmdepth");
			_exit(1);
		}
		if (pid < 0) {
			perror("vfork");
			goto error;
		}
		close(pfd[2][0]); pfd[2][0] = -1;
		close(fd); fd = -1;

		if (pipe(pfd[1]) < 0) {
			perror("pipe");
			goto error;
		}
		if ((gspid = vfork()) == 0) {
			close(pfd[1][1]);
			dup2(pfd[1][0], 0); close(pfd[1][0]);
			dup2(pfd[2][1], 1); close(pfd[2][1]);

			if (verbose)
				fprintf(stderr, "epstoimage: \"pnmscale %s\"\n", scalebuf);
			close(2); /* XXX suppress message */
			execlp("pnmscale", "pnmscale", scalebuf, NULL);
			perror("pnmscale");
			_exit(1);
		}
		if (gspid < 0) {
			perror("vfork");
			goto error;
		}
		close(pfd[2][1]); pfd[2][1] = -1;
		close(pfd[1][0]); pfd[1][0] = -1;
		fd = pfd[1][1]; pfd[1][1] = -1;
	}
	if (pipe(pfd[0]) < 0) {
		perror("pipe");
		goto error;
	}
	if ((gspid = vfork()) == 0) {
		close(pfd[0][1]);
		dup2(pfd[0][0], 0); close(pfd[0][0]);
		dup2(fd, 1); close(fd);

		if (verbose)
			fprintf(stderr, "epstoimage: \"gs %s %s -sOutputFile=- -q -\"\n", geom, device);
		execlp("gs", "gs", geom, device, "-sOutputFile=-", "-q", "-", NULL);
		perror("gs");
		_exit(1);
	}
	close(fd); fd = -1;
	close(pfd[0][0]); pfd[0][0] = -1;

	if ((fp = fdopen(pfd[0][1], "w")) == NULL) {
		fprintf(stderr, "fdopen failed\n");
		goto error;
	}
	sigpipe_handler = signal(SIGPIPE, SIG_IGN);	/* XXX: avoid SIGPIPE */
	pfd[0][1] = -1;
	fprintf(fp, "%f %f scale\n", (double)xzoom/100., (double)yzoom/100.);
	fprintf(fp, "%d %d translate\n", -1 * x, -1 * y);
	fprintf(fp, "(%s) run\n", epsfile);
	fprintf(fp, "showpage\n");
	fprintf(fp, "quit\n");
	fflush(fp);
	fclose(fp);
	signal(SIGPIPE, sigpipe_handler);

	if (!pid)
		pid = gspid;
	while (waitpid(pid, &status, 0) < 0) {
		if (errno != EINTR)
			break;
	}
	if (stat(imagefile, &stbuf) == 0 && stbuf.st_size > 0)
		return imagefile;

  error:
	if (fd >= 0) close(fd);
	for (i = 0; i < 3; i++) {
		for (j = 0; j < 2; j++)
			if (pfd[i][j] >= 0)
				close(pfd[i][j]);
	}
	if (imagefile[0])
		unlink(imagefile);
	return NULL;
}

static void
image_setcolor(struct render_state *state)
{
	struct render_object *obj;
	Image *image;
	unsigned int i;
	Intensity *red, *green, *blue;
	XColor fore, back;

	obj = state->objlast;
	if (obj->type != O_IMAGE)
		return;

	image = obj->data.image.image;
	if (image->trans >= 0)
		return;

	switch (image->type) {
	case IBITMAP:
		/*
		 * XXX: Actually, no one comes here.
		 *      This translation for IBITMAP was done by image_load().
		 */
		fore.pixel = fore_color[caching];
		fore.flags = DoRed | DoGreen | DoBlue;
		XQueryColor(display, colormap, &fore);
		image->rgb.red  [1] = fore.red;
		image->rgb.green[1] = fore.green;
		image->rgb.blue [1] = fore.blue;
		image->trans = 0;
		break;

	case IRGB:
		red   = image->rgb.red;
		green = image->rgb.green;
		blue  = image->rgb.blue;
		for (i = 0; i < image->rgb.used; i++) {
			if (red[i] != green[i] || red[i] != blue[i])
				return;
		}
		/* grayscale */

		fore.pixel = fore_color[caching];
		fore.flags = DoRed | DoGreen | DoBlue;
		XQueryColor(display, colormap, &fore);

		if (!COMPLEX_BGIMAGE) {
			back.pixel = back_color[caching];
			back.flags = DoRed | DoGreen | DoBlue;
			XQueryColor(display, colormap, &back);
		} else {
			int  x, y, bpl;
			byte *p;
			Pixel d;

			/* XXX: use background color of center position */
			x = (obj->x + image->width/2) % bgpixmap[bgindex].image->width;
			y = (state->ypos + image->height/2)
					% bgpixmap[bgindex].image->height;
			bpl = bgpixmap[bgindex].image->pixlen;
			p = bgpixmap[bgindex].image->data
				+ (bgpixmap[bgindex].image->width * y + x) * bpl;
			d = memToVal(p, bpl);
			if (bgpixmap[bgindex].image->type == ITRUE) {
				back.red   = TRUE_RED(d) << 8;
				back.green = TRUE_GREEN(d) << 8;
				back.blue  = TRUE_BLUE(d) << 8;
			} else {
				back.red   = bgpixmap[bgindex].image->rgb.red  [d];
				back.green = bgpixmap[bgindex].image->rgb.green[d];
				back.blue  = bgpixmap[bgindex].image->rgb.blue [d];
			}
		}
		for (i = 0; i < image->rgb.used; i++) {
			if (red[i] >= 65000)	/*XXX*/
				image->trans = i;
			red[i]   = (back.red   * red  [i]
				  + fore.red   * (65535-red  [i])) / 65535;
			green[i] = (back.green * green[i]
				  + fore.green * (65535-green[i])) / 65535;
			blue[i]  = (back.blue  * blue [i]
				  + fore.blue  * (65535-blue [i])) / 65535;
		}
		break;

	case ITRUE:
		/* XXX: assume background color is on the left right corner */
		image->trans = memToVal(image->data, image->pixlen);
	}
}

static void
x_registerseed(struct render_state *state, char *seed, const char *registry)
{
	char tmp1[BUFSIZ], tmp2[BUFSIZ];
	const char *p;
	struct ctrl *cp;
	int hyphen;

	/* if both of arguments are NULL, initialize */
	if (!seed && !registry) {
		if (state->xfont)
			ctlfree(state->xfont);
		state->xfont = NULL;
		return;
	}

	if (!registry)
		registry = "iso8859-1";

	/* canonicalize seed */
	hyphen = 0;
	for (p = seed; *p; p++) {
		if (*p == '-')
			hyphen++;
		if (*p == ':') {
			hyphen = 0;
			break;
		}
	}
	switch (hyphen) {
	case 0:
		/* maybe alias, don't canonicalize */
		break;
	case 1:
		sprintf(tmp1, "%s-*", seed);
		seed = tmp1;
		break;
	case 2:
	case XLFD_HYPHEN:
		/* as is */
		break;
	default:
		fprintf(stderr, "invalid XFONT seed <%s>\n", seed);
		break;
	}

	/* canonicalize registry */
	if (!registry)
		registry = "iso8859-1";
	hyphen = 0;
	for (p = registry; *p; p++) {
		if (*p == '-')
			hyphen++;
	}
	switch (hyphen) {
	case 0:
		sprintf(tmp2, "%s-*", registry);
		registry = tmp2;
		break;
	case 1:
		/* as is */
		break;
	default:
		fprintf(stderr, "invalid XFONT registry <%s>\n", registry);
		exit(1);
	}

	cp = NULL;
	for (cp = state->xfont; cp; cp = cp->ct_next) {
		if (!cp->ctc2_value2) continue;
		if (strcmp(cp->ctc2_value2, registry) == 0)
			break;
	}
	if (cp) {
		if (!strcmp(cp->ctc2_value1, seed)) return;
		free(cp->ctc2_value1);
		cp->ctc2_value1 = strdup(seed);
	} else {
		cp = ctlalloc1(CTL_XFONT2);
		cp->ctc2_value1 = strdup(seed);
		cp->ctc2_value2 = strdup(registry);
		cp->ct_next = state->xfont;
		state->xfont = cp;
	}
}

static const char *
x_findseed(struct render_state *state, const char *registry)
{
	struct ctrl *cp;

	if (!registry)
		registry = "iso8859-1";
	for (cp = state->xfont; cp; cp = cp->ct_next) {
		if (strcmp(cp->ctc2_value2, registry) == 0) {
			return cp->ctc2_value1;
		}
	}
	return "*-*-*";		/* anything, canonicalised */
}

/* cache specified page */
static void
cache_page(struct render_state *state, unsigned int page)
{
	struct ctrl *tmp_bg_ctl;
	int tmp_bgindex;

	/* we don't need caching */
	if (cached_page == page || page > maxpage || page <= 0)
		return;

	if (!page_attribute[page].pg_linenum) return;

	XFlush(display);
	memset(state, 0, sizeof(struct render_state));
	state->target = cachewin;  /*XXX*/
	state->width = window_width;
	state->height = window_height;
	state->page = page;
	caching = 1;
	tmp_bg_ctl = bg_ctl;
	tmp_bgindex = bgindex;
	if (verbose){
		printf("now caching %d page ...\n", page);
		fflush(stdout);
	}
	draw_page(state, NULL);
	if (verbose){
		printf("caching done \n");
	}
	caching = 0;
	cached_page = page;
	bg_ctl = tmp_bg_ctl;
	bgindex = tmp_bgindex;
}

static void
set_from_cache(struct render_state *state)
{
	int	i;

	char_size[0] = char_size[1];
	horiz_gap[0] = horiz_gap[1];
	vert_gap[0] = vert_gap[1];
	fore_color[0] = fore_color[1];
	back_color[0] = back_color[1];
	ctrl_color[0] = ctrl_color[1];
	b_quality[0] = b_quality[1];

	memcpy(state, &cache_state, sizeof(struct render_state));
	state->target = window;

	XSetForeground(display, gcfore, fore_color[0]);
	XSetBackground(display, gcfore, back_color[0]);
	bg_ctl = bg_ctl_last = bg_ctl_cache;
	if (bg_ctl){
		for (i = 0; i < MAXBGPIXMAP; i ++){
			if (bgpixmap[i].ctl && ctlcmp(bg_ctl, bgpixmap[i].ctl) == 0)
				bgindex = i;
		}
		set_background_pixmap(bg_ctl);
	}

	switch(cache_effect){
		case 1:
			cache_effect1();
			break;
		case 2:
			cache_effect2();
			break;
		default:
			break;
	}
	XCopyArea(display, cachewin, window, gc_cache,
		0, 0, window_width, window_height, 0, 0);
	XFlush(display);
}

void
reset_background_pixmap(void)
{
	int	i = 0;

	bg_ctl_last = NULL;
	bg_ctl_cache = NULL;

	for (i = 0; i < MAXBGPIXMAP; i ++) {
		if (bgpixmap[i].image){
			XFreePixmap(display, bgpixmap[i].pixmap);
			freeXImage(bgpixmap[i].ximageinfo);
			freeImage(bgpixmap[i].image);
		}
		bgpixmap[i].ctl = NULL;
		bgpixmap[i].image = NULL;
		bgpixmap[i].ximageinfo = NULL;
	}
}

static void
cache_effect1(void)
{
	int x, step;
	XEvent e;

	step = cache_value ? window_width / cache_value : 1;
	if (!step) step = 1;

	for (x = window_width; x > step; x -= step){
		XCopyArea(display, window, window, gc_cache,
			step, 0,  window_width - step, window_height, 0, 0);

		XCopyArea(display, cachewin, window, gc_cache,
			window_width - x, 0, step, window_height,
			window_width - step, 0);
		XSync(display, False);
		if (XCheckMaskEvent(display, ~NoEventMask, &e) == True) {
			printf("event type=%d\n", e.type);
			XPutBackEvent(display, &e);
			break;
		}
	}
}

static void
cache_effect2(void)
{
	int x, step;

	step = cache_value ? window_width / (cache_value * 2) : 1;
	if (!step) step = 1;

	for (x = 0; x < window_width; x += step){
		XCopyArea(display, window, window, gc_cache,
			x, 0,  window_width - step -x , window_height, x + step, 0);

		XCopyArea(display, cachewin, window, gc_cache,
			x, 0, step, window_height, x, 0);

		XFlush(display);
	}
}

/*
	pcache directive process
*/
static void
pcache_process(unsigned int page)
{
	if (!pcache.flag)
		return;

	if (pcache.page != page)
		return;

	if (pcache.mgpflag)
		mgp_flag |= FL_FRDCACHE;
	else
		mgp_flag ^= FL_FRDCACHE;
	cache_mode   = pcache.mode;
	cache_effect = pcache.effect;
	cache_value  =  pcache.value;
	pcache.flag  = 0;
}

/*
	predraw: if this page contains texts only,
			   draw page in pixmap once, then copy to window.
*/
static void
predraw(struct render_state *state)
{
	if (!caching && cached_page != state->page
			&& page_attribute[state->page].pg_text
			&& page_attribute[state->page].pg_linenum){
		cache_page(&cache_state, state->page);
		set_from_cache(state);
		pcache_process(state->page);
	}
}

static void
get_background_pixmap(struct ctrl *ctl, struct render_state *state)
{
	int	i;

	/*
	 * check if background is already cached
	 */
	for (i = 0; i < MAXBGPIXMAP; i ++){
		if (bgpixmap[i].ctl && ctlcmp(ctl, bgpixmap[i].ctl) == 0){
			bgindex = i;
			return;
		}
	}

	if (i == MAXBGPIXMAP){
		/* this background is not cached, we have to generate one */
		switch(ctl->ct_op){
		case CTL_BIMAGE:
			image_load(state, ctl->ctm_fname, ctl->ctm_numcolor,
						ctl->ctm_ximagesize, ctl->ctm_yimagesize, 1,
						ctl->ctm_zoomflag, 0, 0, ctl->ctm_rotate, 0);
			break;
		case CTL_BGRAD:
			back_gradation(state, &ctl->ct_val.ctrl_grad);
			break;
		case CTL_BACK:
			break;
		default:
			fprintf(stderr, "fatal error in get_background_pixmap()\n");
			cleanup(-1);
			break;
		}
	}
}

static void
regist_background_pixmap(XImageInfo *ximageinfo, Image *image)
{
	Pixmap	pixmap2;
	int	i, j;

	/* search empty slot */
	for (i = 0; i < MAXBGPIXMAP; i ++){
		if (bgpixmap[i].ctl == NULL)
			break;
	}

	if (i == MAXBGPIXMAP){
		/* no empty slot, we need to make one  */
		XFreePixmap(display, bgpixmap[MAXBGPIXMAP -1].pixmap);
		freeXImage(bgpixmap[MAXBGPIXMAP -1].ximageinfo);
		freeImage(bgpixmap[MAXBGPIXMAP -1].image);
		for (j = MAXBGPIXMAP -2; j >= 0; j --){
			bgpixmap[j +1].ctl = bgpixmap[j].ctl;
			bgpixmap[j +1].pixmap = bgpixmap[j].pixmap;
			bgpixmap[j +1].image = bgpixmap[j].image;
			bgpixmap[j +1].ximageinfo = bgpixmap[j].ximageinfo;
		}
		bg_ctl_last = NULL;
		i = 0;
	}

	pixmap2 = ximageToPixmap(display,
			RootWindow(display, screen), ximageinfo);
	bgpixmap[i].ctl = bg_ctl;
	bgpixmap[i].pixmap = pixmap2;
	bgpixmap[i].image = image;
	bgpixmap[i].ximageinfo = ximageinfo;
	bgindex = i;
}

static void
set_background_pixmap(struct ctrl *ctl)
{
	int	i;

	switch(ctl->ct_op){
	case CTL_BIMAGE:
	case CTL_BGRAD:
		for (i = 0; i < MAXBGPIXMAP; i ++){
			if (bgpixmap[i].ctl && ctlcmp(ctl, bgpixmap[i].ctl) == 0)
				break;
		}
		if (i == MAXBGPIXMAP){
			fprintf(stderr, "fatal error in set_background_pixmap()\n");
			cleanup(-1);
		}
		XSetWindowBackgroundPixmap(display, window, bgpixmap[i].pixmap);
		break;
	case CTL_BACK:
		XSetWindowBackground(display, window, ctl->ctl_value);
		break;
	default:
		fprintf(stderr, "fatal error in set_background_pixmap() op=%d\n",
			ctl->ct_op);
		cleanup(-1);
		break;
	}
}

/*
 * Clear target pixmap
 */
static void
XClearPixmap(Display *dpy, Drawable target)
{
	int	i;
	int x, y;
	XImage *xim;

	switch(bg_ctl->ct_op){
	case CTL_BIMAGE:
	case CTL_BGRAD:
		for (i = 0; i < MAXBGPIXMAP; i ++){
			if (bgpixmap[i].ctl && ctlcmp(bg_ctl, bgpixmap[i].ctl) == 0)
				break;
		}
		if (i == MAXBGPIXMAP){
			fprintf(stderr, "fatal error in XClearPixmap()\n");
			cleanup(-1);
		}

		xim = bgpixmap[i].ximageinfo->ximage;
		for (y = 0; y < window_height; y += xim->height)
			for (x = 0; x < window_width; x += xim->width)
				XPutImage(dpy, target, gc_cache,
					xim, 0, 0, x, y,
					xim->width, xim->height);
		break;
	case CTL_BACK:
		XSetForeground(dpy, gc_cache, bg_ctl->ctl_value);
		XFillRectangle(dpy, target,
			gc_cache, 0, 0, window_width, window_height);
		break;
	default:
		fprintf(stderr, "fatal error in XClearPixmap()\n");
		cleanup(-1);
		break;
	}
}

int
get_regid(const char *registry)
{
	const char *p;

	if (!registry || registry[0] == '\0') return 0;
	if (strlen(registry) == 9 && !strncmp("iso8859-", registry, 8) &&
		registry[8] >= '1' && registry[8] <= '4') {
			p = registry + 8;
			return atoi(p) -1;
	} else
		return -1;
}

void
set_xrender_color(unsigned long value, int opaque)
{
	XColor xc;

	xft_forecolor.color.alpha = 65535 * opaque / 100;
	if (value == xft_forecolor.pixel) return;

	xc.flags = DoRed | DoGreen | DoBlue;
	xc.pixel = value;
	XQueryColor(display, colormap, &xc);

	xft_forecolor.pixel = value;
	xft_forecolor.color.red = xc.red;
	xft_forecolor.color.green = xc.green;
	xft_forecolor.color.blue = xc.blue;
}

static u_char *
xft_draw_fragment(struct render_state *state, u_char *p, size_t len,
    const char *registry, /* 2-octet charset? */ int charset16)
{
	XGlyphInfo extents;
	struct ctrl *cp;
	char *fontname = NULL;
	unsigned int i;
	static char etab[3][20] = { "iso-2022-jp", "gb2312", "ksc5601"};
	static char rtab[3][20] = { "jisx208", "gb2312", "ksc5601"};
	static char prefix[3][20] = { "\033$B", "\033$A", "\033$(C"};
	char buf16[1024], *p16;
	char out16[1024], *o16;
	size_t ileft, oleft;
#if HAVE_ICONV
	static iconv_t icv[3];
#endif

	for (i = 0; i < len; i ++){
		if (!isspace(*(p + i))) state->brankline = 0; /* This isn't brankline */
	}
	if (!registry) registry = "iso8859-1";
	for (cp = state->xfont; cp; cp = cp->ct_next) {
		if (!cp->ctc2_value2) continue;
		if (strcmp(cp->ctc2_value2, registry) == 0) {
			fontname = cp->ctc2_value1;
			break;
		}
	}
	if (!fontname) return NULL;
	if (!(xft_font = xft_setfont(fontname, char_size[caching], registry))) return NULL;

	if (charset16) {
#if HAVE_ICONV
		for (i = 0; i < 3; i ++) {
			if (!strncmp(registry, rtab[i], 3)) break;
		}
		if (i == 3) return NULL; /* cannot find codeset */
		snprintf(buf16, sizeof(buf16), "%s%s%c", prefix[i], p, 0);
		if (icv[i] == (iconv_t)0) icv[i] = iconv_open("UTF-8", etab[i]);
		if (icv[i] == (iconv_t)-1) {
			fprintf(stderr, "your iconv doesn't support %s\n",
			    etab[i]);
			return NULL;
		}
		p16 = buf16; o16 = out16;
		ileft = len + strlen(prefix[i]); oleft = sizeof(out16);
		if (iconv(icv[i], &p16, &ileft, &o16, &oleft) == (size_t)-1) {
			perror("iconv");
			return NULL;
		}

		/* measure drawing are */
		XftTextExtentsUtf8(display, xft_font, (XftChar8 *)out16,
		    sizeof(out16) - oleft, &extents);

		/* line folding check */
		if ((int)(state->width - state->leftfillpos / 2 - state->linewidth) <
		    extents.xOff) {
			draw_line_end(state);
			draw_line_start(state);
			state->linewidth = state->leftfillpos;
		}

		draw_line_itemsize(state, xft_font->ascent, xft_font->descent, 0);
		if (obj_new_xftfont(state, state->linewidth, 0, (u_char *)out16,
		    sizeof(out16) - oleft, fontname, registry,
		    char_size[caching], charset16, xft_font)) {
			state->linewidth += extents.xOff;
			return p + len;
		} else
#endif
			return NULL;
	}

	XftTextExtents8(display, xft_font, (XftChar8 *)p, len, &extents);

	/* line folding check */
	if ((int)(state->width - state->leftfillpos / 2 - state->linewidth) < extents.xOff) {
		if (isspace(*(p + len -1))) {
		    XftTextExtents8(display, xft_font, (XftChar8 *)p, len -1, &extents);
			if ((int)(state->width - state->leftfillpos / 2 - state->linewidth) >= extents.xOff)
				goto nofolding;
			draw_line_end(state);
			draw_line_start(state);
			state->linewidth = state->leftfillpos;
			return p;
		}

		draw_line_end(state);
		draw_line_start(state);
		state->linewidth = state->leftfillpos;
		return p;
	}

nofolding:

	draw_line_itemsize(state, xft_font->ascent, xft_font->descent, 0);

	if (obj_new_xftfont(state, state->linewidth, state->charoff, p, len, fontname,
	    registry, char_size[caching], charset16, xft_font)) {
		state->linewidth += extents.xOff;
		return p + len;
	} else
		return NULL;
}

static int
obj_new_xftfont(struct render_state *state, int x, int y, u_char *p,
    size_t len, const char *fontname, const char *registry, int size,
    int charset16, XftFont *xft_font2)
{
	struct render_object *obj;
	char buf[65535], *p1;

	p1 = buf;
	memset(buf, '\0', sizeof(buf));
	if (sizeof(buf) > len)
		memcpy(buf, p, len);
	else
		return 0;

	obj = obj_alloc(state);
	if (obj == NULL)
		return 0;
	obj->x = x;
	obj->y = y;
	obj->fore = fore_color[caching];
	obj->type = O_XTFONT;
	obj->data.xftfont.data = (u_char *)strdup(p1);
	obj->data.xftfont.fontname = strdup(fontname);
	obj->data.xftfont.registry = strdup(registry);
	obj->data.xftfont.len = len;
	obj->data.xftfont.size = size;
	obj->data.xftfont.charset16 = charset16;
	obj->ascent = xft_font2->ascent;
	obj->descent = xft_font2->descent;
	obj->vertloc = VL_BASE;
	return 1;
}

static XftDraw *
xft_getdraw(Drawable drawable)
{
	int i;
	for (i = 0; i < 2; i ++) {
		if (xft_xdraw[i] == drawable)
			return xft_draw[i];
	}
	for (i = 0; i < 2; i ++) {
		if (!xft_xdraw[i])
			xft_draw[i] = XftDrawCreate(display, drawable, visual,
			    colormap);
		return xft_draw[i];
	}
	return NULL; /* should not happen */
}

static
XftFont *
xft_setfont(const char *xfontarg, int csize, const char *registry)
{
	char *xfont;
	static XftFont *last_xftfont;
	static char lastfont[200];
	static int lastsize = 0;
	XftFont *xftfont;
	char *p, *p2;
	char style[100];
	char font[100];

	memset(style, '\0', sizeof(style));
	memset(font, '\0', sizeof(font));

	xfont = strdup(xfontarg);
	if (!xfont)
		return NULL;

	if (!strcmp(xfont, lastfont) && lastsize == csize) {
		free(xfont);
		return last_xftfont;
	}

	if ((p = strchr(xfont, ':')) != NULL) {
		/*
		 * if xfont contsins ":", we believe this is a Xft font name
		 * with the style expression.
		 */
		p2 = p + 1;
		/* allow using ":style=" syntax */
		if ((strstr(p2, "style=") != NULL) || (strstr(p2, "STYLE=") != NULL))
			p2 += 6;
		*p = '\0';
		strlcpy(font, xfont, sizeof(font));
		strlcpy(style, p2, sizeof(style));
	} else if ((p = strchr(xfont, '-')) != NULL) {
		/*
		 * if xfont contains "-", we believe this is a conventional
		 * xfont name and try to convert it for xft
		 */
		*p++ = 0;
		strlcpy(font, xfont, sizeof(font));
		if (strncmp(p, "bold-i", 6) == 0)
			strlcpy(style, "Bold Italic", sizeof(style));
		else if (strncmp(p, "bold-", 5) == 0)
			strlcpy(style, "Bold", sizeof(style));
		else if ((p = strchr(p, '-')) != NULL && p[1] == 'i')
			strlcpy(style, "Italic", sizeof(style));
	} else
		strlcpy(font, xfont, sizeof(font));
	if (style[0]) {
		xftfont = XftFontOpen(display, screen,
		    XFT_FAMILY, XftTypeString, font,
		    XFT_ENCODING, XftTypeString, registry,
		    XFT_STYLE, XftTypeString, style,
		    XFT_PIXEL_SIZE, XftTypeDouble, (float)csize, NULL);
	} else {
		xftfont = XftFontOpen(display, screen,
		    XFT_FAMILY, XftTypeString, font,
		    XFT_ENCODING, XftTypeString, registry,
		    XFT_PIXEL_SIZE, XftTypeDouble, (float)csize, NULL);
	}
	if (xftfont == 0) {
		free(xfont);
		return NULL;
	}
	if (style[0])
		snprintf(lastfont, sizeof(lastfont), "%s:%s", font, style);
	else
		snprintf(lastfont, sizeof(lastfont), "%s", font);
	if (verbose) {
		fprintf(stderr, "using xftfont [%s] size: %d\n", lastfont,
		    csize);
	}
	lastsize = csize;
	last_xftfont = xftfont;
	free(xfont);
	return last_xftfont;
}

void
regist_zimage_position(struct render_object *obj,
    int x, int y, int width, int height, int page)
{
	int i;

	for (i = 0; i < ZIMAGENUM; i ++){
		/* already registered */
		if (zimage[i] == obj->data.image.imimage) return;
	}
	for (i = 0; i < ZIMAGENUM; i ++){
		if (!zimage[i]) break;
	}
	if (i == ZIMAGENUM) {
		fprintf(stderr, "Warning: too many images\n");
		return;
	}
	zimage[i] = obj->data.image.imimage;
	zonzoom[i] = obj->data.image.zoomonclk;
	zx[i] = x;
	zy[i] = y;
	zwidth[i] = width;
	zheight[i] = height;
	zpage[i] = page;
}

static void
clear_zimage(int page)
{
	int i;
	zoomin = 0;
	manage_pixmap((Pixmap)NULL, 0, page);
	for (i = 0; i < ZIMAGENUM; i ++){
		if (zpage[i] == page) zimage[i] = 0;
	}
}

int
search_zimage(int x, int y, int page)
{
	int i;
	for (i = 0; i < ZIMAGENUM; i ++){
		if (!zimage[i]) continue;
		if (zx[i] <= x && zx[i] + zwidth[i] >= x &&
		    zy[i] <= y && zy[i] + zheight[i] >= y && zpage[i] == page) {
			return i;
		}
	}
	return -1;
}

void
zoomin_zimage(int id)
{
	Pixmap pixmap2;
	int i, w, h, x, y, xf, yf;
	int ratio = 10;
	float zstep = (window_width * zonzoom[id] / 100.0 - zwidth[id]) / (float)ratio;
	float xstep;
	float ystep;
	float xyratio = (float)zheight[id] / zwidth[id];

	xf = window_width * (100 - zonzoom[id]) / 200.0;
	yf = (window_height - (window_width * zonzoom[id] / 100.0 * xyratio)) / 2;
	xstep = (float)(xf - zx[id]) / ratio;
	ystep = (float)(yf - zy[id]) / ratio;

	 for (i = 0; i <= ratio; i ++) {
		w = zstep * i + zwidth[id];
		h = w * xyratio+1;
		x = zx[id] + xstep * i;
		y = zy[id] + ystep * i;
		pixmap2 = pixmap_fromimimage(zimage[id], w, h, window);
		manage_pixmap(pixmap2, 1, zpage[id]);
		if (i > 0) clear_region(id, i-1, i, 0);
		XCopyArea(display, pixmap2, window, gcfore, 0,0, w, h, x, y);
		XFlush(display);
		if (i < ratio) usleep(10000);
	 }
}

void
zoomout_zimage(int id)
{
	Pixmap pixmap2;
	int i, w, h, x, y, xf, yf;
	int ratio = 10;
	float zstep = (window_width * zonzoom[id] / 100.0 - zwidth[id]) / (float)ratio;
	float xstep;
	float ystep;
	float xyratio = (float)zheight[id] / zwidth[id];

	xf = window_width * (100 - zonzoom[id]) / 200.0;
	yf = (window_height - (window_width * zonzoom[id] / 100.0 * xyratio)) / 2;
	xstep = (float)(xf - zx[id]) / ratio;
	ystep = (float)(yf - zy[id]) / ratio;

	for (i = ratio-1; i >= 0; i --) {
		w = zstep * i + zwidth[id];
		h = w * xyratio+1;
		x = zx[id] + xstep * i;
		y = zy[id] + ystep * i;
		pixmap2 = pixmap_fromimimage(zimage[id], w, h, window);
		manage_pixmap(pixmap2, 1, zpage[id]);
		if (i < ratio) clear_region(id, i+1, i, 0);
		XCopyArea(display, pixmap2, window, gcfore, 0, 0, w, h, x, y);
		XFlush(display);
		if (i > 0) usleep(10000);
	}
	clear_region(id, ratio, 1, 1);
}

void
clear_region(int id, int prev, int cur, int clear)
{
	int xf, yf;
	int x1, x2, y1v, y2, w1, w2, h1, h2;
	int ratio = 10;
	float zstep = (window_width * zonzoom[id] / 100.0 - zwidth[id]) / (float)ratio;
	float xstep;
	float ystep;
	float xyratio = (float)zheight[id] / zwidth[id];

	if (prev > ratio) return;
	xf = window_width * (100 - zonzoom[id]) / 200.0;
	yf = (window_height - (window_width * zonzoom[id] / 100.0 * xyratio)) / 2;
	xstep = (float)(xf - zx[id]) / ratio;
	ystep = (float)(yf - zy[id]) / ratio;

	x1 = zx[id] + xstep * prev;
	y1v = zy[id] + ystep * prev;
	w1 = zstep * prev + zwidth[id];
	h1 = w1 * xyratio+1;

	x2 = zx[id] + xstep * cur;
	y2 = zy[id] + ystep * cur;
	w2 = zstep * cur + zwidth[id];
	h2 = w2 * xyratio+1;

	if (x2 > x1) XClearArea(display, window, x1-1, y1v, x2 - x1, h1, clear);
	if (y2 > y1v) XClearArea(display, window, x1, y1v, w1, y2 - y1v, clear);
	if (x2 + w2 < x1 + w1) XClearArea(display, window, x2 + w2, y1v, x1 + w1 - x2 - w2, h1, clear);
	if (y2 + h2 < y1v + h1) XClearArea(display, window, x1, y2 + h2, w1, y1v + h1 - y2 - h2, clear);
}
