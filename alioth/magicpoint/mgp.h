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

#if !defined(MCPPF) || !defined(MCCF)
# error do not override flags at Makefile level
#else
#include <sys/param.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#if HAVE_BOTH_TIME_H
#include <sys/time.h>
#include <time.h>
#elif HAVE_SYS_TIME_H
#include <sys/time.h>
#elif HAVE_TIME_H
#include <time.h>
#endif
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#include <assert.h>
#if HAVE_SYS_WAIT_H
# include <sys/wait.h>
#endif
#include <sys/stat.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <X11/Xlib.h>
#include <X11/keysym.h>
#include <X11/cursorfont.h>
#include <X11/Xatom.h>
#include <ft2build.h>
#include FT_FREETYPE_H
#include <fontconfig/fontconfig.h>
#include <X11/Xft/Xft.h>
#if HAVE_ICONV_H
#include <iconv.h>
#endif
#include "image/xloadimage.h"

#if HAVE_TERMIOS_H
#define	TTY_KEYINPUT
#else
#undef	TTY_KEYINPUT
#endif

#if !HAVE_CAN_UCHAR
typedef unsigned char u_char;
#endif

#if !HAVE_CAN_USHORT
typedef unsigned short u_short;
#endif

#if !HAVE_CAN_UINT
typedef unsigned int u_int;
#endif

#if !HAVE_CAN_ULONG
typedef unsigned long u_long;
#endif

#undef __dead
#if HAVE_ATTRIBUTE_NORETURN
#define __dead __attribute__((__noreturn__))
#else
#define __dead /* nothing */
#endif

#undef unused_parameter
#if HAVE_ATTRIBUTE_UNUSED
#define unused_parameter __attribute__((__unused__))
#else
#define unused_parameter /* nothing */
#endif

#define DEFAULT_FORE	"yellow"
#define DEFAULT_BACK	"black"
#define PAGELIST_FONT	"a14"
#define PAGELIST_KFONT	"k14"

#define DEFAULT_CHARSIZE	10	/* 10% of height */
#define DEFAULT_SUPSCALE	0.6
#define DEFAULT_SUPOFF		0.4
#define DEFAULT_SUBOFF		0.15
#define DEFAULT_HGAP		0
#define DEFAULT_VGAP		15
#define DEFAULT_BQUALITY	100
#define DEFAULT_OPAQUE		100

#define XLFD_HYPHEN	14
#define FONT_FORMAT	"-*-%s-*-*-%s-*-*-*-*-*-%s"
#define DEFAULT_X_FONT	"times-medium-r"
#define CUTIN_DELAY	5000
#define SHRINK_DELAY	00500
#define EXEC_DELAY  20000

#define	DEFAULT_GSDEV	"pnmraw+"

#define MAXPAGE		512
#define MAXLINE		256
#define MAXVALLEN	512
#define MAXDIREC	16
#define MAXARG		32
#define MAXTAB		32
#define MAXSTYLE	100
#define MAXFONTDEF	100

#define MAXBGPIXMAP	2

#define SP_NONE		0
#define SP_SHRINK	1
#define SP_LCUTIN	2
#define SP_RCUTIN	3

#define AL_LEFT		0
#define AL_CENTER	1
#define AL_RIGHT	2
#define AL_LEFTFILL0	3
#define AL_LEFTFILL1	4

#define DEFAULT_GRADSTART	"blue"
#define DEFAULT_GRADEND		"black"
#define DEFAULT_GRADDEPTH	8
#define DEFAULT_GRADCOLORS	128

/* 	mgp command line flags 	*/
#define FL_OVER		0x0001
#define FL_BIMAGE	0x0002
#define FL_DEMO		0x0004
#define FL_VERBOSE	0x0008
#define FL_NOBEEP	0x0010
#define FL_NOFORK	0x0020
#define FL_PRIVATE	0x0040
#define FL_NODECORATION	0x0080
#define FL_NOAUTORELOAD	0x0100
#define	FL_NOSTDIN	0x0200
#define	FL_GLYPHEDGE	0x0400
#define	FL_FRDCACHE	0x0800
#define	FL_NOXFT	0x1000

/* 	page attribute flags 	*/
#define PGFLAG_NODEF	0x01	/* nodefault */

#define EVENT_DEFAULT \
	(KeyPressMask|KeyReleaseMask|ButtonPressMask|StructureNotifyMask|\
		ExposureMask)
#define EVENT_RAKUGAKI \
	(EVENT_DEFAULT|ButtonReleaseMask|Button1MotionMask)

#ifndef BUILDSH_LINKTEST
#include "ctlwords.h"
#endif

struct ctrl_double {
	double ct_value;
};

struct ctrl_int {
	u_int ct_value;
};

struct ctrl_int2 {
	u_int ct_value1;
        u_int ct_value2;
};

struct ctrl_int3 {
	u_int ct_value1;
        u_int ct_value2;
        u_int ct_value3;
	};

struct ctrl_long {
	u_long ct_value;
};

struct ctrl_char {
	char *ct_value;
};

struct ctrl_char2 {
	char *ct_value1;
	char *ct_value2;
};

struct ctrl_args {
	char **ct_argv;
	u_int ct_argc;
	int ct_flag;
};

struct ctrl_image {
	char *ct_fname;
	u_int ct_numcolor;
	u_int ct_ximagesize;
	u_int ct_yimagesize;
	u_int ct_zoomflag;
	u_int ct_raise;
	u_int ct_rotate; /* +/-180, +/-90, 0, 270 */
	u_int ct_zoomonclk;
#define Z_XMASK		0x0f
#define Z_YMASK		0xf0
#define Z_YSHIFT	4
#define Z_ABSOLUTE	0x00	/* absolute value */
#define Z_NORMAL	0x01	/* normal zoom ratio */
#define Z_SCREEN	0x02	/* screen relative zoom ratio */
#define Z_SCREEN0	0x03	/* original screen size specified */
#define Z_OBEY		0x04	/* obey other axis */
};

/* for gradation image generation*/
struct g_color {
	int r, g, b;
	int y;
};

struct ctrl_grad {
	struct g_color **colors;
	u_int ct_numcolor;
	int ct_direction;
	u_int ct_width;		/* resulting image width, percentage */
	u_int ct_height;	/* resulting image height, percentage */
	u_int ct_zoomflag;	/* zoom to full screen? */
	int ct_mode;		/* linear(0) / non-linear(1) */
	int ct_g_colors;
};

struct ctrl_bar {
	u_long ct_color;
	u_int ct_width;
	u_int ct_start;
	u_int ct_length;
};

struct ctrl_icon {
	char *ct_value;
	u_long ct_color;
	u_int ct_size;
};

struct ctrl_area {
	u_int ct_xoff;
	u_int ct_width;
	u_int ct_yoff;
	u_int ct_height;
};

struct ctrl_pcache {
	u_int ct_cflag;
	u_int ct_cmode;
	u_int ct_ceffect;
	u_int ct_cvalue;
};

struct ctrl {
	struct ctrl *ct_next;
	u_int ct_page;
	u_char ct_op;
	u_char ct_flag;
	union {
		struct ctrl_double ctrl_double;
		struct ctrl_int ctrl_int;
                struct ctrl_int2 ctrl_int2;
                struct ctrl_int3 ctrl_int3;
		struct ctrl_long ctrl_long;
		struct ctrl_char ctrl_char;
		struct ctrl_char2 ctrl_char2;
		struct ctrl_image ctrl_image;
		struct ctrl_grad ctrl_grad;
		struct ctrl_bar ctrl_bar;
		struct ctrl_args ctrl_args;
		struct ctrl_icon ctrl_icon;
		struct ctrl_area ctrl_area;
		struct ctrl_pcache ctrl_pcache;
	} ct_val;
};

#define ctf_value	ct_val.ctrl_double.ct_value
#define cti_value	ct_val.ctrl_int.ct_value
#define cti2_value1	ct_val.ctrl_int2.ct_value1
#define cti2_value2	ct_val.ctrl_int2.ct_value2
#define cti3_value1	ct_val.ctrl_int3.ct_value1
#define cti3_value2	ct_val.ctrl_int3.ct_value2
#define cti3_value3	ct_val.ctrl_int3.ct_value3
#define ctl_value	ct_val.ctrl_long.ct_value
#define ctc_value	ct_val.ctrl_char.ct_value
#define ctc2_value1	ct_val.ctrl_char2.ct_value1
#define ctc2_value2	ct_val.ctrl_char2.ct_value2
#define ctm_fname	ct_val.ctrl_image.ct_fname
#define ctm_numcolor	ct_val.ctrl_image.ct_numcolor
#define ctm_ximagesize	ct_val.ctrl_image.ct_ximagesize
#define ctm_yimagesize	ct_val.ctrl_image.ct_yimagesize
#define ctm_zoomflag	ct_val.ctrl_image.ct_zoomflag
#define ctm_raise	ct_val.ctrl_image.ct_raise
#define ctm_rotate	ct_val.ctrl_image.ct_rotate
#define ctm_zoomonclk	ct_val.ctrl_image.ct_zoomonclk
#define ctd_colors	ct_val.ctrl_grad.colors
#define ctd_g_colors	ct_val.ctrl_grad.ct_g_colors
#define ctd_numcolor	ct_val.ctrl_grad.ct_numcolor
#define ctd_dir		ct_val.ctrl_grad.ct_direction
#define ctd_basewidth	ct_val.ctrl_grad.ct_width
#define ctd_baseheight	ct_val.ctrl_grad.ct_baseheight
#define ctd_width	ct_val.ctrl_grad.ct_width
#define ctd_height	ct_val.ctrl_grad.ct_height
#define ctd_hquality	ct_val.ctrl_grad.ct_hquality
#define ctd_vquality	ct_val.ctrl_grad.ct_vquality
#define ctd_zoomflag	ct_val.ctrl_grad.ct_zoomflag
#define ctd_mode	ct_val.ctrl_grad.ct_mode
#define ctb_color	ct_val.ctrl_bar.ct_color
#define ctb_width	ct_val.ctrl_bar.ct_width
#define ctb_start	ct_val.ctrl_bar.ct_start
#define ctb_length	ct_val.ctrl_bar.ct_length
#define cta_argc	ct_val.ctrl_args.ct_argc
#define cta_argv	ct_val.ctrl_args.ct_argv
#define cta_flag	ct_val.ctrl_args.ct_flag
#define ctic_value	ct_val.ctrl_icon.ct_value
#define ctic_color	ct_val.ctrl_icon.ct_color
#define ctic_size	ct_val.ctrl_icon.ct_size
#define	ctar_xoff	ct_val.ctrl_area.ct_xoff
#define	ctar_width	ct_val.ctrl_area.ct_width
#define	ctar_yoff	ct_val.ctrl_area.ct_yoff
#define	ctar_height	ct_val.ctrl_area.ct_height
#define ctch_flag	ct_val.ctrl_pcache.ct_cflag
#define ctch_mode	ct_val.ctrl_pcache.ct_cmode
#define ctch_effect	ct_val.ctrl_pcache.ct_ceffect
#define ctch_value	ct_val.ctrl_pcache.ct_cvalue

struct ctl_words {
        const char *ctl_string;
        short ctl_strlen;
        u_char ctl_type;
	char ctl_vtype;
#define T_STR	'c'
#define T_STR2	'C'
#define T_INT	'i'
#define T_LONG	'l'
#define T_DOUBLE 'f'
#define T_SP	'x'
#define T_VOID	'-'
};

extern const struct ctl_words ctl_words[];
extern const struct ctl_words icon_words[];

struct page_attribute {
	char *pg_title_str;     /* a title has been defined */
	u_int pg_flag;
	u_int pg_linenum;
	u_int pg_b_numcolor;	/* background gradation number of colors */
	u_int pg_b_dir;		/* background gradation deg */
	u_int pg_text;		/* this page is text only */
};
extern struct page_attribute page_attribute[MAXPAGE];

extern int	caching;
extern unsigned int cached_page;
extern int	cache_hit;
extern int	cache_mode;
extern int	cache_effect;
extern int	cache_value;

struct render_state {
	/* state of the parser */
	struct ctrl *cp;
	char *curprefix;
	char *tabprefix;
	/* state of the renderer */
	struct ctrl *xfont;
	struct render_object *obj, *objlast;
	/* state of the parser */
	u_int page;
	u_int line;
	enum { P_NONE, P_DEFAULT, P_PAGE, P_END } phase;
		/*
		 * NONE	   - nothing
		 * DEFAULT - doing default_control
		 * PAGE    - doing page_control
		 */
	u_int align;
	u_int special;
	u_int leftfillpos;

	/*
	 * state of the renderer
	 * we don't have xpos here since that will be
	 * dynaimcally determined at CTL_LINEEND.
	 */
	Drawable target;
	u_int height;
	u_int width;
	int xprefix;
	int tabxprefix;
	int xoff;
	int yoff;
	u_int ypos;
	int have_mark;
	u_int mark_ypos;
	u_int repaint;
	unsigned int maxascent;
	unsigned int maxdescent;
	int maxflheight;
	/* max size above baseline ignoring supscript */
	unsigned int max_lineascent;
	/* max size below baseline ignoring subscript */
	unsigned int max_linedescent;
	u_int linewidth;
	u_int brankline;
	u_int opaque;
	u_int charoff;
};

struct render_object {
	struct render_object *next;
	union {
		struct {
			char *xfont;
			const char *registry;
			u_int csize;
			u_int code;
		} xfont;
		struct {
			Image *image;
			Imlib_Image *imimage;
			float xzoom, yzoom;
			int zoomonclk;
		} image;
		struct {
			XPoint *xpoint;
			u_int itype;
			u_int isize;
			u_int npoint;
		} icon;
		struct {
			u_char *data;
			char *fontname;
			char *registry;
			int len;
			int size;
			int charset16;
		} xftfont;
	} data;
	int x;	/* relative from left position of line */
	int y;	/* relative from center position of line (usually negative) */
	unsigned int ascent;	/* size above the baseline */
	unsigned int descent;	/* size below the baseline */
	int vertloc;	/* vertical placement control */
#define VL_BASE		0
#define VL_CENTER	1
#define VL_TOP		2
#define VL_BOTTOM	3
#define VL_ICENTER	4
	enum {
		O_XFONT,
		O_IMAGE,
		O_XTFONT,
		O_ICON
	} type;
	u_long fore;
};

struct alloc_color {
	u_long	*colors;
	int	num;
};

struct bgpixmap {
	struct ctrl *ctl;
	Image *image;
	XImageInfo *ximageinfo;
	Pixmap	pixmap;
};

/*
 * The following variables are defined in global.c, and therefore
 * they are available in both "mgp" and "mgp2ps" binary.
 */
extern struct ctrl *page_control[MAXPAGE][MAXLINE];
extern struct ctrl *default_control[MAXLINE];
extern struct ctrl *tab_control[MAXTAB+MAXSTYLE];
extern struct ctrl *init_control[MAXLINE];
extern struct ctrl *fontdef_control[MAXFONTDEF];

extern u_int mgp_flag;
extern int verbose;
extern u_int maxpage;
extern u_int cur_page;
extern char *mgp_fname;
extern char *mgp_wname;

extern u_int parse_error;
extern u_int parse_debug;

extern Display *display;
extern Window window;
extern int screen;
extern int window_width;
extern int window_height;
extern Pixmap pixmap;
extern Pixmap cachewin;
extern Pixmap cachetmp;
extern struct bgpixmap bgpixmap[MAXBGPIXMAP];
extern Colormap colormap;
extern struct alloc_color image_clr;
extern struct alloc_color back_clr;
extern struct alloc_color font_clr;

extern u_int char_size[2];
extern u_int nonscaled_size[2];
extern float sup_scale;
extern float sup_off;
extern float sub_off;
extern u_int horiz_gap[2];
extern u_int vert_gap[2];
extern u_int depth;
extern Visual *visual;
extern u_long fore_color[2];
extern u_long back_color[2];
extern u_long ctrl_color[2];
extern u_int b_quality[2];
extern u_int quality_flag;

extern char mgp_charset[256];

#define VERT_GAP(s)		((s) * vert_gap[caching] / 100)
#define HORIZ_GAP(s)		((s) * horiz_gap[caching] / 100)
#define VERT_STEP(s)		((s) + VERT_GAP(s))
#define HORIZ_STEP(s, x)	((x) + HORIZ_GAP(s))

/*
 * The following variable are defined in x11.c.  Therefore, these are
 * accessible only in "mgp" binary, not in "mgp2ps".
 * We should separate header files.
 * (or, if mgp.c and print.c get merged the problem will vanish)
 */
/* covered by gcconf */
extern GC gcfore;
extern GC gcpen;
extern GC gcred;
extern GC gcgreen;
extern GC gcyellow;

/* not covered by gcconf */
extern GC gc_pl;
extern GC gc_plrev;
extern GC gc_pta;
extern GC gc_ptk;

extern GC gc_cache;

extern long xeventmask;

/*
 * The following variable are defined in mgp.c.  Therefore, these are
 * accessible only in "mgp" binary, not in "mgp2ps".
 * We should separate header files.
 * (or, if mgp.c and print.c get merged the problem will vanish)
 */
extern Window plwin[MAXPAGE];
extern Pixmap maskpix;
extern XFontStruct *plfs;
extern XFontStruct *plkfs;
extern XFontStruct *plsfs;
extern u_int pg_mode;
extern time_t t_start;
extern u_int t_fin;
extern u_int tbar_mode;
extern u_long pl_fh, pl_fw;
extern u_long depth_mask;
extern int latin_unicode_map[3][256];
#ifdef TTY_KEYINPUT
extern volatile int ttykey_enable;
#endif
extern const char *back_clname;
extern const char *gsdevice;
extern char *htmlimage;
extern int zoomin;

/* mgp.c */
extern pid_t checkchild(void *);
extern Window checkchildwin(void *);
extern void regchild(pid_t, void *, Window, int);
extern void purgechild(int);
extern void remapchild(void);
#ifdef TTY_KEYINPUT
extern void try_enable_ttykey(void);
#endif
extern void reset_background_pixmap(void);

/*draw.c*/
extern void state_goto(struct render_state *, u_int, int);
extern void state_init(struct render_state *);
extern void state_newpage(struct render_state *);
extern void state_next(struct render_state *);
extern void draw_page(struct render_state *, struct ctrl *);
extern Bool draw_one(struct render_state *, XEvent *);
extern void timebar(struct render_state *);
extern void draw_reinit(struct render_state *);
extern void draw_line_itemsize(struct render_state *,
    unsigned int, unsigned int, int);
extern void draw_line_start(struct render_state *);
extern void draw_line_end(struct render_state *);

/*global.c*/
extern int iskinsokuchar(u_int);
extern ssize_t writex(int, const void *, size_t)
#if HAVE_ATTRIBUTE_BOUNDED
    __attribute__((__bounded__(__buffer__, 2, 3)))
#endif
    ;

/*parse.c*/
extern void load_file(char *);
extern void cleanup_file(void);
extern int ctlcmp(struct ctrl *, struct ctrl *);
extern FILE *fsearchopen(const char *, const char *, const char **);
extern int chkfile(char *);
extern struct ctrl *ctllastitem(struct ctrl *);
extern void ctlappend(struct ctrl *, struct ctrl *);
extern void ctlinsert(struct ctrl **, struct ctrl *);
extern struct ctrl *ctlalloc1(u_int);
extern void ctlfree(struct ctrl *);
extern struct ctrl *ctlcopy(struct ctrl *);
extern struct ctrl *ctlcopy1(struct ctrl *);
extern void debug0(struct ctrl *);
extern void debug1(struct ctrl *);

/*plist.c*/
extern void pl_on(struct render_state *);
extern void pl_off(void);
extern void pl_pdraw(struct render_state *, unsigned int, GC);
extern void pl_title(u_int);
extern const char *page_title(u_int);
extern void pg_on(void);
extern void pg_clean(void);
extern void pg_draw(struct render_state *);
extern void pg_off(void);

/*x11.c or x11dummy.c*/
extern int window_x;
extern int window_y;
extern void init_win1(char *);
extern void init_win2(void);
extern void init_win3(void);
extern void finish_win(void);
extern int get_color(const char *, u_long *);
extern struct g_color *name2gcolor(const char *);
extern void regist_alloc_colors(struct alloc_color *, u_long *, u_int);
extern void free_alloc_colors(struct alloc_color *);
extern void toggle_fullscreen(void);

/* background.c */
extern double cdist(int, int, int, int, int, int);
extern double lcdist(int, int, int, int, int, int, double, double);
extern byte *draw_gradation(int, int, struct ctrl_grad *);
extern Image *make_XImage(byte *, unsigned int, unsigned int);

/* postscript.c */
extern int ps_boundingbox(char *, int *, int *, int *, int *);
extern void image_zoomratio(struct render_state *, float *, float *, int, int, int);

/* unimap.c */
extern void latin_unicode_map_init(void);

/* draw.c */
int search_zimage(int, int, int);
void zoomin_zimage(int);
void zoomout_zimage(int);

/* grammar.y */
extern void yyerror(const char *, ...)
#if HAVE_ATTRIBUTE_FORMAT
    __attribute__((__format__(__printf__, 1, 2)))
#endif
    ;
#ifndef IN_GRAMMAR_Y
extern int yyparse(void);
#endif
extern int n_errors;
extern struct ctrl *yyroot;
extern char *yyfilename;
extern int yylineno;

/* scanner.l */
extern void lex_init(char *);

#endif
