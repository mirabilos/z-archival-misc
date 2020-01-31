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

/*
 * Global variables common to "mgp" and "mgp2ps".
 * (hate to define these in two places, mgp.c and print.c, I've made
 * a separate file)
 */
u_char *page_data[MAXPAGE][MAXLINE];
struct ctrl *page_control[MAXPAGE][MAXLINE];
struct ctrl *default_control[MAXLINE];
struct ctrl *tab_control[MAXTAB+MAXSTYLE];
struct ctrl *init_control[MAXLINE];
struct ctrl *fontdef_control[MAXFONTDEF];
struct page_attribute page_attribute[MAXPAGE];

u_int mgp_flag;
int verbose;
u_int maxpage;
u_int cur_page = 0;
char *mgp_fname;
char *mgp_wname;    /* window title */

u_int parse_error = 0;
u_int parse_debug = 0;

Display *display;
Visual *visual;
Window window;
int screen;
int window_width;
int window_height;
int caching = 0;
unsigned int cached_page = 0;
int cache_hit = 0;
int cache_mode = 0;
int cache_effect = 0;
int cache_value = 60;
Pixmap pixmap;
Pixmap cachewin;
Pixmap cachetmp;
struct bgpixmap bgpixmap[MAXBGPIXMAP];
Colormap colormap;
int free_clr_num;
u_long *free_clr = NULL;

u_int char_size[2];
u_int nonscaled_size[2];
float sup_scale;
float sup_off;
float sub_off;
u_int horiz_gap[2] = {DEFAULT_HGAP, DEFAULT_HGAP};
u_int vert_gap[2] = {DEFAULT_VGAP, DEFAULT_VGAP};
u_int depth;
u_long fore_color[2];
u_long back_color[2];
u_long ctrl_color[2];
u_int b_quality[2] = {DEFAULT_BQUALITY, DEFAULT_BQUALITY};
u_int quality_flag = 0;

char mgp_charset[256];

struct alloc_color image_clr = { NULL, 0 };
struct alloc_color back_clr = { NULL, 0 };
struct alloc_color font_clr = { NULL, 0 };

const struct ctl_words icon_words[] = {
	{ "box",    3, 1, T_SP },
	{ "arc",    3, 2, T_SP },
	{ "delta1", 6, 3, T_SP },
	{ "delta2", 6, 4, T_SP },
	{ "delta3", 6, 5, T_SP },
	{ "delta4", 6, 6, T_SP },
	{ "dia",    3, 7, T_SP },
	{ NULL,     0, 0, 0 }
};

/*
 * 禁則 (きんそく)
 *    1)  Verbot
 *        prohibition
 *    2)  (vor bzw. nach best. Zeichen)
 *        {Druckw.} Verhinderung eines Umbruchs
 * … but in which encoding is this table?
 */
static const u_short kinsokutable[] = {
	0x2121, 0x2122, 0x2123, 0x2124, 0x2125, 0x2126, 0x2127, 0x2128,
	0x2129, 0x212a, 0x212b, 0x212c, 0x212d, 0x212e, 0x212f, 0x2130,
	0x2133, 0x2134, 0x2135, 0x2136, 0x213c, 0x2147, 0x2149, 0x214b,
	0x214d, 0x214f, 0x2151, 0x2153, 0x2155, 0x2157, 0x2159, 0x216b,
	0x2242, 0x2244, 0
};

int
iskinsokuchar(u_int code)
{
	const u_short *kinsoku;

	for (kinsoku = kinsokutable; *kinsoku; kinsoku++) {
		if (code == *kinsoku)
			return (1);
	}
	return (0);
}

ssize_t
writex(int fd, const void *buf_, size_t size)
{
	const unsigned char *buf = buf_;
	ssize_t rv = 0, z;

	while (size) {
		if ((z = write(fd, buf, size)) < 0) {
			if (errno == EINTR)
				continue;
			return (rv ? /* fucked up since we got some */ -2 : -1);
		}
		rv += z;
		buf += z;
		size -= z;
	}
	return (rv);
}
