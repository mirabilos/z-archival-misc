%{
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
/*
 * partly derived from lbl libpcap source code, which has the following
 * copyright notice:
 */
/*
 * Copyright (c) 1988, 1989, 1990, 1991, 1992, 1993, 1994, 1995, 1996
 *	The Regents of the University of California.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that: (1) source code distributions
 * retain the above copyright notice and this paragraph in its entirety, (2)
 * distributions including binary code include the above copyright notice and
 * this paragraph in its entirety in the documentation or other materials
 * provided with the distribution, and (3) all advertising materials mentioning
 * features or use of this software display the following acknowledgement:
 * ``This product includes software developed by the University of California,
 * Lawrence Berkeley Laboratory and its contributors.'' Neither the name of
 * the University nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior
 * written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

#define IN_GRAMMAR_Y
#include "mgp.h"
#include <stdarg.h>

int yylex(void);

int n_errors = 0;
struct ctrl *yyroot;
char *yyfilename;
int yylineno;

void
yyerror(const char *msg, ...)
{
	va_list ap;
	va_start(ap, msg);
	++n_errors;
	fprintf(stderr, "%s:%d: error: ", yyfilename, yylineno);
	vfprintf(stderr, msg, ap);
	fprintf(stderr, "\n");
	va_end(ap);
}

static void yywarn(const char *msg, ...)
#if HAVE_ATTRIBUTE_FORMAT
    __attribute__((__format__(__printf__, 1, 2)))
#endif
    ;

static void
yywarn(const char *msg, ...)
{
	va_list ap;
	va_start(ap, msg);
	fprintf(stderr, "%s:%d: warning: ", yyfilename, yylineno);
	vfprintf(stderr, msg, ap);
	fprintf(stderr, "\n");
	va_end(ap);
}

static struct ctrl *
gen_void(int op)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate void node");
		return ct;
	}
	return ct;
}

static struct ctrl *
gen_double_int(int op, int v)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate double node");
		return ct;
	}
	ct->ctf_value = (double)v;
	return ct;
}

static struct ctrl *
gen_double(int op, double v)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate double node");
		return ct;
	}
	ct->ctf_value = v;
	return ct;
}

static struct ctrl *
gen_int(int op, int v)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate integer node");
		return ct;
	}
	ct->cti_value = v;
	return ct;
}

static struct ctrl *
gen_int3(int op, int v1, int v2, int v3)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate integer3 node");
		return ct;
	}
	ct->cti3_value1 = v1;
	ct->cti3_value2 = v2;
	ct->cti3_value3 = v3;
	return ct;
}

static struct ctrl *
gen_str(int op, const char *str)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate str1 node");
		return ct;
	}
	ct->ctc_value = strdup(str);
	return ct;
}

static struct ctrl *
gen_str2(int op, const char *str1, const char *str2)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate str2 node");
		return ct;
	}
	ct->ctc2_value1 = strdup(str1);
	ct->ctc2_value2 = strdup(str2);
	return ct;
}

static struct ctrl *
gen_color(int op, char *color)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate colour node");
		return ct;
	}
	if (get_color(color, &ct->ctl_value) < 0)
		yyerror("cannot allocate colour \"%s\"", color);
	return ct;
}

static struct ctrl *
gen_bgrad(int w, int h, int colors, int dir, int zoomflg, struct ctrl *extra)
{
	struct ctrl *ct;
	struct ctrl *p;
	int siz;

	if (!(ct = ctlalloc1(CTL_BGRAD))) {
		yyerror("cannot allocate node (op=BGRAD)");
		return ct;
	}
	ct->ctd_width = w;
	ct->ctd_height = h;
	ct->ctd_numcolor = colors;
	ct->ctd_dir = dir;
	ct->ctd_zoomflag = zoomflg;

	/* process color list. */
	siz = ct->ctd_g_colors = 0;
	for (p = extra; p; p = p->ct_next)
		siz++;
	if (siz <= 2) {
		ct->ct_val.ctrl_grad.colors =
			malloc(3 * sizeof(struct gcolor *));
	} else {
		ct->ct_val.ctrl_grad.colors =
			malloc((siz + 1) * sizeof(struct gcolor *));
	}
	if (!ct->ct_val.ctrl_grad.colors) {
		yyerror("cannot allocate color table");
		return ct;
	}

	ct->ctd_g_colors = 2;
	ct->ct_val.ctrl_grad.colors[0] = name2gcolor(DEFAULT_GRADSTART);
	ct->ct_val.ctrl_grad.colors[1] = name2gcolor(DEFAULT_GRADEND);
	switch (siz) {
	case 0:
		break;
	case 1:
		ct->ct_val.ctrl_grad.colors[0] = name2gcolor(extra->ctc_value);
		break;
	default:
		ct->ctd_g_colors = siz;
		siz = 0;
		for (p = extra; p; p = p->ct_next) {
			ct->ct_val.ctrl_grad.colors[siz] =
				name2gcolor(p->ctc_value);
			siz++;
		}
	}

	/* normalize */
	if (ct->ctd_dir < 0) {	/*circle*/
		ct->ctd_mode = 1;
		ct->ctd_dir = abs(ct->ctd_dir);
	} else			/*linear*/
		ct->ctd_mode = 0;
	while (ct->ctd_dir < 0)
		ct->ctd_dir += 360;
	ct->ctd_dir %= 360;
	if (ct->ctd_width <= 0)
		ct->ctd_width = 100;
	if (ct->ctd_height <= 0)
		ct->ctd_height = 100;

	if (extra)
		ctlfree(extra);

	return ct;
}

static struct ctrl *
gen_newimage(struct ctrl *arg)
{
	struct ctrl *p;
	struct ctrl *ct;

	if (!(ct = ctlalloc1(CTL_IMAGE))) {
		yyerror("cannot allocate node (op=IMAGE)");
		return ct;
	}

	/* default setting */
	ct->ctm_ximagesize = 100;
	ct->ctm_yimagesize = 100;
	ct->ctm_zoomflag = Z_NORMAL | (Z_NORMAL << Z_YSHIFT);
	ct->ctm_raise = 0;
	ct->ctm_rotate = 0;
	ct->ctm_zoomonclk = 0;

	for (p = arg; p; p = p->ct_next) {
		if (p->ctc_value[0] != '-')
			break;

		if (strcmp(p->ctc_value, "-colors") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_numcolor = atoi(p->ctc_value);
		} else if (strcmp(p->ctc_value, "-xysize") == 0
			&& p->ct_next && p->ct_next->ct_next) {
			p = p->ct_next;
			ct->ctm_ximagesize = atoi(p->ctc_value);
			p = p->ct_next;
			ct->ctm_yimagesize = atoi(p->ctc_value);
			ct->ctm_zoomflag = Z_ABSOLUTE | (Z_ABSOLUTE << Z_YSHIFT);
		} else if (strcmp(p->ctc_value, "-zoom") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_ximagesize = atoi(p->ctc_value);
			ct->ctm_yimagesize = atoi(p->ctc_value);
			ct->ctm_zoomflag = Z_NORMAL | (Z_NORMAL << Z_YSHIFT);
		} else if (strcmp(p->ctc_value, "-xyzoom") == 0
			&& p->ct_next && p->ct_next->ct_next) {
			p = p->ct_next;
			ct->ctm_ximagesize = atoi(p->ctc_value);
			p = p->ct_next;
			ct->ctm_yimagesize = atoi(p->ctc_value);
			ct->ctm_zoomflag = Z_NORMAL | (Z_NORMAL << Z_YSHIFT);
		} else if (strcmp(p->ctc_value, "-scrzoom") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_ximagesize = atoi(p->ctc_value);
			ct->ctm_yimagesize = atoi(p->ctc_value);
			ct->ctm_zoomflag = Z_SCREEN | (Z_SCREEN << Z_YSHIFT);
		} else if (strcmp(p->ctc_value, "-xscrzoom") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_ximagesize = atoi(p->ctc_value);
			ct->ctm_yimagesize = 100;
			ct->ctm_zoomflag = Z_SCREEN | (Z_OBEY << Z_YSHIFT);
		} else if (strcmp(p->ctc_value, "-yscrzoom") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_ximagesize = 100;
			ct->ctm_yimagesize = atoi(p->ctc_value);
			ct->ctm_zoomflag = Z_OBEY | (Z_SCREEN << Z_YSHIFT);
		} else if (strcmp(p->ctc_value, "-xyscrzoom") == 0
			&& p->ct_next && p->ct_next->ct_next) {
			p = p->ct_next;
			ct->ctm_ximagesize = atoi(p->ctc_value);
			p = p->ct_next;
			ct->ctm_yimagesize = atoi(p->ctc_value);
			ct->ctm_zoomflag = Z_SCREEN | (Z_SCREEN << Z_YSHIFT);
		} else if (strcmp(p->ctc_value, "-raise") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_raise = atoi(p->ctc_value);
		} else if (strcmp(p->ctc_value, "-rotate") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_rotate = atoi(p->ctc_value);
		} else if (strcmp(p->ctc_value, "-zoomonclk") == 0 && p->ct_next) {
			p = p->ct_next;
			ct->ctm_zoomonclk = atoi(p->ctc_value);
		} else {
			yyerror("invalid argument %s specified for newimage",
				p->ctc_value);
			return ct;
		}
	}

	if (!p) {
		yyerror("no filename specified to newimage");
		return ct;
	}

	if (p->ct_next) {
		yyerror("multiple filename specified to newimage");
		return ct;
	}

	ct->ctm_fname = p->ctc_value;
	chkfile(ct->ctm_fname);
	return ct;
}

static struct ctrl *
gen_image(int op, char *fname, int colors, int xsiz, int ysiz, int zoomflg)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate node (op=IMAGE)");
		return ct;
	}
	ct->ctm_fname = fname;
	ct->ctm_numcolor = colors;
	ct->ctm_ximagesize = xsiz;
	ct->ctm_yimagesize = ysiz;
	switch (zoomflg) {
	case 0:
		ct->ctm_zoomflag = 0;
		if (ct->ctm_ximagesize == 0) {
			ct->ctm_ximagesize = 100;
			ct->ctm_zoomflag |= Z_NORMAL;
		} else
			ct->ctm_zoomflag |= Z_SCREEN;
		if (ct->ctm_yimagesize == 0) {
			ct->ctm_yimagesize = 100;
			ct->ctm_zoomflag |= (Z_NORMAL << Z_YSHIFT);
		} else
			ct->ctm_zoomflag |= (Z_SCREEN << Z_YSHIFT);
		break;
	case 1:
		ct->ctm_zoomflag = Z_NORMAL | (Z_NORMAL << Z_YSHIFT);
		break;
	case 2:
		ct->ctm_zoomflag = Z_SCREEN0 | (Z_SCREEN0 << Z_YSHIFT);
		break;
	}
	chkfile(ct->ctm_fname);
	return ct;
}

static struct ctrl *
gen_bar(const char *color, int thick, int start, int len)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(CTL_BAR))) {
		yyerror("cannot allocate node (op=BAR)");
		return ct;
	}
	if (get_color(color, &ct->ctb_color) < 0)
		yyerror("cannot allocate colour %s", color);

	/* normalise */

	if (thick < 0)
		ct->ctb_width = 0;
	else if (thick > 1000)
		ct->ctb_width = 1000;
	else
		ct->ctb_width = thick;

	if (start < 0)
		ct->ctb_start = 0;
	else if (start > 100)
		ct->ctb_start = 100;
	else
		ct->ctb_start = start;

	if ((ct->ctb_start + len) > 100)
		ct->ctb_length = 100 - ct->ctb_start;
	else
		ct->ctb_length = len;

	return ct;
}

static struct ctrl *
gen_icon(char *n, char *color, int siz)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(CTL_ICON))) {
		yyerror("cannot allocate node (op=ICON)");
		return ct;
	}
	ct->ctic_value = n;
	if (get_color(color, &ct->ctic_color) < 0)
		yyerror("cannot allocate colour %s", color);
	ct->ctic_size = siz;
	return ct;
}

static struct ctrl *
gen_pcache(int flag, int mode, int effect, int value)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(CTL_PCACHE))) {
		yyerror("cannot allocate node (op=PCACHE)");
		return ct;
	}
	ct->ctch_flag = flag;
	ct->ctch_mode = mode;
	ct->ctch_effect = effect;
	ct->ctch_value = value;

	return ct;
}

static struct ctrl *
gen_valign(char *align)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(CTL_VALIGN))) {
		yyerror("cannot allocate node (op=VALIGN)");
		return ct;
	}
	if (!strcmp(align, "center"))
		ct->cti_value = VL_CENTER;
	else {
		if (!strcmp(align, "top"))
			ct->cti_value = VL_TOP;
		else {
			if (!strcmp(align, "bottom"))
				ct->cti_value = VL_BOTTOM;
			else {
				yyerror("%%valign not center|top|bottom");
				ctlfree(ct);
				return NULL;
			}
		}
	}
	return ct;
}

static struct ctrl *
gen_area(int width, int height, int xoff, int yoff)
{
	struct ctrl *ct;

	if (!(ct = ctlalloc1(CTL_AREA))) {
		yyerror("cannot allocate node (op=AREA)");
		return ct;
	}
	if (width < 0 || width > 100)
		width = 100;
	if (height < 0 || height > 100)
		height = 100;
	if (xoff < 0)
		xoff = (100 - width) / 2;
	else if (width + xoff > 100)
		xoff = 100 - width;
	if (yoff < 0)
		yoff = (100 - height) / 2;
#ifdef notdef	/* mgp doesn't check overflow in y axis, anyway. */
	else if (height + yoff > 100)
		yoff = 100 - height;
#endif
	ct->ctar_width = width;
	ct->ctar_height = height;
	ct->ctar_xoff = xoff;
	ct->ctar_yoff = yoff;
	return ct;
}

static struct ctrl *
gen_argsfromstr(int op, char *str, int flag)
{
	struct ctrl *ct;
	unsigned int siz;
	char **h;

	if (!(ct = ctlalloc1(op))) {
		yyerror("cannot allocate args node");
		return ct;
	}

	ct->cta_argc = 0;
	ct->cta_argv = malloc((siz = 16) * sizeof(char *));	/*initial siz*/
	ct->cta_flag = flag;
	if (!ct->cta_argv) {
		yyerror("cannot allocate args table");
		return ct;
	}
	for (h = (char **)ct->cta_argv;
	     (*h = strsep((char **)&str, " "));
	     /*none*/) {
		if (**h != '\0') {
			h++;
			ct->cta_argc++;
			if (siz < ct->cta_argc + 2) {
				siz *= 2;
				ct->cta_argv = realloc(ct->cta_argv,
					siz * sizeof(char *));
				if (!ct->cta_argv) {
					yyerror("cannot allocate args table");
					return ct;
				}
			}
		}
	}
	ct->cta_argv[ct->cta_argc] = NULL;

	return ct;
}

static void
check_xfont(const char *seed, const char *registry)
{
	int hyphen;
	const char *p;

	hyphen = 0;
	for (p = seed; *p; p++) {
		if (*p == '-')
			hyphen++;
	}
	switch (hyphen) {
	case 0:
	case 1:
	case 2:
	case XLFD_HYPHEN:
		break;
	default:
		yyerror("invalid XFONT seed <%s>", seed);
		break;
	}

	hyphen = 0;
	for (p = registry; *p; p++) {
		if (*p == '-')
			hyphen++;
	}
	switch (hyphen) {
	case 0:
	case 1:
		break;
	default:
		yyerror("invalid XFONT registry <%s>", registry);
		break;
	}
}

%}

%union {
	int i;
	double d;
	char *s;
	struct ctrl *ct;
}

%token COMMA NUM DOUBLE ID STR WINSIZ
%token KW_NOOP KW_DEFAULT KW_TAB KW_SIZE KW_FORE KW_BACK KW_LEFT KW_CENTER
%token KW_RIGHT KW_SHRINK KW_LCUTIN KW_RCUTIN KW_CONT KW_NODEF KW_XFONT
%token KW_IMAGE KW_BIMAGE KW_PAGE KW_HGAP KW_VGAP KW_GAP KW_PAUSE
%token KW_PREFIX KW_AGAIN KW_CCOLOR KW_BAR KW_INCLUDE KW_BGRAD KW_TEXT
%token KW_LINESTART KW_LINEEND KW_MARK KW_SYSTEM KW_FILTER KW_ENDFILTER
%token KW_QUALITY KW_ICON KW_LEFTFILL KW_XSYSTEM KW_TSYSTEM
%token KW_DEFFONT KW_FONT KW_NEWIMAGE KW_PSFONT
%token KW_CHARSET KW_PCACHE KW_VALIGN KW_AREA
%token KW_OPAQUE KW_SUP KW_SUB KW_SETSUP KW_TITLE

%type <ct> toplevel
%type <ct> line defaultline tabline shellline deffontline
%type <ct> cmd defaultcmd tabcmd  deffontcmd
%type <ct> nid args arg
%type <i> NUM
%type <d> DOUBLE
%type <s> ID STR WINSIZ STRorID

%%
toplevel: line			{ yyroot = $$; }
	| defaultline		{ yyroot = $$; }
	| tabline		{ yyroot = $$; }
	| shellline		{ yyroot = $$; }
	| deffontline		{ yyroot = $$; }
	;
line:	  cmd			{ $$ = $1; }
	| cmd COMMA line	{ $$ = $1; $$->ct_next = $3; }
	;
defaultline: defaultcmd line	{ $$ = $1; $$->ct_next = $2; }
	;
tabline: tabcmd line		{ $$ = $1; $$->ct_next = $2; }
	;
deffontline: deffontcmd line	{ $$ = $1; $$->ct_next = $2; }
	;
STRorID:  STR
	| ID			{ yywarn("\"%s\" should be quoted", $1); }
	;
shellline:
	  KW_SYSTEM STR	NUM	{ $$ = gen_argsfromstr(CTL_SYSTEM, $2, $3); }
	| KW_SYSTEM STR		{ $$ = gen_argsfromstr(CTL_SYSTEM, $2, 0); }
	| KW_XSYSTEM STR NUM	{ $$ = gen_argsfromstr(CTL_XSYSTEM, $2, $3); }
	| KW_XSYSTEM STR	{ $$ = gen_argsfromstr(CTL_XSYSTEM, $2, 0); }
	| KW_TSYSTEM STR NUM	{ $$ = gen_argsfromstr(CTL_TSYSTEM, $2, $3); }
	| KW_TSYSTEM STR	{ $$ = gen_argsfromstr(CTL_TSYSTEM, $2, 0); }
	| KW_FILTER STR		{ $$ = gen_argsfromstr(CTL_FILTER, $2, 0); }
	| KW_ENDFILTER		{ $$ = gen_void(CTL_ENDFILTER); }
	;
nid:	  STRorID	{ $$ = gen_str(CTL_NOOP, $1); }
	| STRorID nid	{ $$ = gen_str(CTL_NOOP, $1); $$->ct_next = $2; }
	;
args:	  arg		{ $$ = $1; }
	| arg args	{ $$ = $1; $$->ct_next = $2; }
	;
arg:	  STR		{ $$ = gen_str(CTL_NOOP, $1); }
	| ID		{ $$ = gen_str(CTL_NOOP, $1); }
	| NUM		{ char buf[30];
			  snprintf(buf, sizeof(buf), "%d", $1);
			  $$ = gen_str(CTL_NOOP, buf);
			}
	;
cmd:	  KW_NOOP	{ $$ = gen_void(CTL_NOOP); }
	| KW_LEFT	{ $$ = gen_void(CTL_LEFT); }
	| KW_LEFTFILL	{ $$ = gen_void(CTL_LEFTFILL); }
	| KW_RIGHT	{ $$ = gen_void(CTL_RIGHT); }
	| KW_CENTER	{ $$ = gen_void(CTL_CENTER); }
	| KW_SHRINK	{ $$ = gen_void(CTL_SHRINK); }
	| KW_LCUTIN	{ $$ = gen_void(CTL_LCUTIN); }
	| KW_RCUTIN	{ $$ = gen_void(CTL_RCUTIN); }
	| KW_CONT	{ $$ = gen_void(CTL_CONT); }
	| KW_NODEF	{ $$ = gen_void(CTL_NODEF); }
	| KW_PAUSE	{ $$ = gen_int(CTL_PAUSE, 0); }
	| KW_AGAIN	{ $$ = gen_void(CTL_AGAIN); }
	| KW_MARK	{ $$ = gen_void(CTL_MARK); }
	| KW_PAGE	{ $$ = gen_void(CTL_PAGE); }
	| KW_SETSUP NUM NUM NUM { $$ = gen_int3(CTL_SETSUP, $2, $3, $4); }
	| KW_SUP	{ $$ = gen_void(CTL_SUP); }
	| KW_SUB	{ $$ = gen_void(CTL_SUB); }
	| KW_SIZE NUM	{ $$ = gen_double_int(CTL_SIZE, $2); }
	| KW_SIZE DOUBLE	{ $$ = gen_double(CTL_SIZE, $2); }
	| KW_HGAP NUM	{ $$ = gen_int(CTL_HGAP, $2); }
	| KW_VGAP NUM	{ $$ = gen_int(CTL_VGAP, $2); }
	| KW_GAP NUM	{ $$ = gen_int(CTL_GAP, $2); }
	| KW_QUALITY NUM
			{ if (!quality_flag)
				$$ = gen_int(CTL_QUALITY, $2);
			  else
				$$ = ctlalloc1(CTL_NOOP);
			}
	| KW_FORE STRorID	{ $$ = gen_color(CTL_FORE, $2); }
	| KW_BACK STRorID	{ $$ = gen_color(CTL_BACK, $2); }
	| KW_CCOLOR STRorID	{ $$ = gen_color(CTL_CCOLOR, $2); }
	| KW_BGRAD NUM NUM NUM NUM NUM nid
			{ $$ = gen_bgrad($2, $3, $4, $5, $6, $7); }
	| KW_BGRAD NUM NUM NUM NUM NUM
			{ $$ = gen_bgrad($2, $3, $4, $5, $6,
				(struct ctrl *)NULL);
			}
	| KW_BGRAD NUM NUM NUM NUM
			{ $$ = gen_bgrad($2, $3, $4, $5, 1,
				(struct ctrl *)NULL);
			}
	| KW_BGRAD NUM NUM NUM
			{ $$ = gen_bgrad($2, $3, $4, 0, 1,
				(struct ctrl *)NULL);
			}
	| KW_BGRAD NUM NUM
			{ $$ = gen_bgrad($2, $3, DEFAULT_GRADCOLORS, 0, 1,
					(struct ctrl *)NULL);
			}
	| KW_BGRAD NUM	{ $$ = gen_bgrad($2, 100, DEFAULT_GRADCOLORS, 0, 1,
					(struct ctrl *)NULL);
			}
	| KW_BGRAD	{ $$ = gen_bgrad(100, 100, DEFAULT_GRADCOLORS, 0, 1,
					(struct ctrl *)NULL);
			}
	| KW_XFONT STRorID
			{ char *p;
			  if (strncmp($2, "medium", 6) == 0
			   || strncmp($2, "bold", 4) == 0) {
				/* for backward compatibility */
				p = malloc(strlen($2) + 1 + 6);
				sprintf(p, "times-%s", $2);
			  } else
				p = $2;
			  check_xfont(p, "iso8859-1"); /* lexical check */
			  $$ = gen_str2(CTL_XFONT2, p, "iso8859-1");
			}
	| KW_XFONT STRorID STRorID
			{ check_xfont($2, $3);	/* lexical check */
			  $$ = gen_str2(CTL_XFONT2, $2, $3);
			}
	| KW_BIMAGE STRorID	{ $$ = gen_image(CTL_BIMAGE, $2, 0, 0, 0, 0); }
	| KW_BIMAGE STRorID WINSIZ
			{ int x, y;
			  x = atoi($3); y = atoi(strchr($3, 'x') + 1);
			  $$ = gen_image(CTL_BIMAGE, $2, 0, x, y, 2);
			}
	| KW_PSFONT STRorID     {
					$$ = gen_str2(CTL_PSFONT, $2, "iso8859-1");
				}
	| KW_INCLUDE STRorID	{ $$ = gen_str(CTL_INCLUDE, $2); }
	| KW_PREFIX ID	{ char *p;
			  $$ = gen_str(CTL_PREFIX, $2);
			  for (p = $$->ctc_value; *p; p++) {
				if (*p == '_') *p = ' ';
			  }
			}
	| KW_PREFIX STR	{ $$ = gen_str(CTL_PREFIX, $2); }
	| KW_PREFIX NUM	{ $$ = gen_double_int(CTL_PREFIXN, $2); }
	| KW_PREFIX DOUBLE	{ $$ = gen_double(CTL_PREFIXN, $2); }
	| KW_NEWIMAGE args
			{ $$ = gen_newimage($2); }
	| KW_IMAGE STRorID WINSIZ
			{ int x, y;
			  x = atoi($3); y = atoi(strchr($3, 'x') + 1);
			  $$ = gen_image(CTL_IMAGE, $2, 0, x, y, 2);
			}
	| KW_IMAGE STRorID NUM WINSIZ
			{ int x, y;
			  x = atoi($4); y = atoi(strchr($4, 'x') + 1);
			  $$ = gen_image(CTL_IMAGE, $2, $3, x, y, 2);
			}
	| KW_IMAGE STRorID NUM NUM NUM NUM
			{ $$ = gen_image(CTL_IMAGE, $2, $3, $4, $5, $6 ? 1 : 0); }
	| KW_IMAGE STRorID NUM NUM NUM
			{ $$ = gen_image(CTL_IMAGE, $2, $3, $4, $5, 0); }
	| KW_IMAGE STRorID NUM NUM
			{ $$ = gen_image(CTL_IMAGE, $2, $3, $4, 0, 0); }
	| KW_IMAGE STRorID NUM
			{ $$ = gen_image(CTL_IMAGE, $2, $3, 0, 0, 0); }
	| KW_IMAGE STRorID 	{ $$ = gen_image(CTL_IMAGE, $2, 0, 0, 0, 0); }
	| KW_BAR STRorID NUM NUM NUM
			{ $$ = gen_bar($2, $3, $4, $5); }
	| KW_BAR STRorID NUM NUM
			{ $$ = gen_bar($2, $3, $4, 100); }
	| KW_BAR STRorID NUM	{ $$ = gen_bar($2, $3, 0, 100); }
	| KW_BAR STRorID	{ $$ = gen_bar($2, 10, 0, 100); }
	| KW_BAR	{ $$ = gen_bar("black", 10, 0, 100); }
	| KW_ICON STR STRorID NUM
			{ $$ = gen_icon($2, $3, $4); }
	| KW_ICON ID STRorID NUM
			{ $$ = gen_icon($2, $3, $4); }
	| KW_FONT STR	{ $$ = gen_str(CTL_FONT, $2); }
	| KW_TITLE STR	{ $$ = gen_str(CTL_TITLE, $2); }
	| KW_TEXT STR	{ $$ = gen_str(CTL_TEXT, $2); }	/*easter egg*/
	| KW_CHARSET STR	{ $$ = gen_str(CTL_CHARSET, $2); }
	| KW_AREA NUM NUM { $$ = gen_area($2, $3, -1, -1); }
	| KW_AREA NUM NUM NUM NUM { $$ = gen_area($2, $3, $4, $5); }
	| KW_PCACHE NUM NUM NUM NUM
			{ $$ = gen_pcache($2, $3, $4, $5); }
	| KW_PCACHE NUM
			{ $$ = gen_pcache($2, 0, 0, 0); }
	| KW_VALIGN STRorID	{
			$$ = gen_valign($2);
	}
	| KW_OPAQUE NUM	{ $$ = gen_int(CTL_OPAQUE, $2); }
	;
tabcmd:	  KW_TAB NUM	{ $$ = gen_int(CTL_TAB, $2); }
	| KW_TAB ID	{ $$ = gen_str(CTL_TAB, $2); }
	;
defaultcmd: KW_DEFAULT NUM
			{ $$ = gen_int(CTL_DEFAULT, $2); }
	;
deffontcmd: KW_DEFFONT STR
			{ $$ = gen_str(CTL_DEFFONT, $2); }
	;
%%
