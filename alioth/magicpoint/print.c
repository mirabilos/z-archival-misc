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
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLER
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#include "mgp.h"
#if HAVE_LOCALE_H
#include <locale.h>
#endif

static u_int align = AL_LEFT;
static char *curprefix = NULL;
static char *tabprefix = NULL;
static u_int lineheight;
static u_int linewidth;
static u_int gaplevel = 0;
static int char_off = 0;	/* y-offset of the chars (relative to the
				   current line, used for super/subscript */

#define DEFAULT_PAPER_SIZE	"a4"
#define PRINT_ASCIIFONT		"Times-Roman"
#define PRINT_ASCIITHICKFONT	"Helvetica"
#define PRINT_KANJIFONT		"Ryumin-Light-H"
#define PRINT_KANJITHICKFONT	"GothicBBB-Medium-H"

static FILE *fp;
static u_int reverse;
static char outputfile[MAXVALLEN];
static int colorps = 0;
static int painticon = 0;
static u_int curlinenum;
static u_int curpagenum=0;
static u_int xprefix = 0;
static u_int tabxprefix = 0;
static u_int valign = VL_BOTTOM;

/* by Arnd Schmitter 23.07.2004 */
static int lg; /* Counts the Recursionlevel in print_page() for new Pause-Mode */
static int PauseMode=0; /* Should Pause-Statements splitet into multiple Pages ? */

static struct imagepool {
	struct ctrl *image;
	int xoffset;
	int xsiz;
	int ysiz;
	int eps;
	int target_text;    /* by A. Ito */
} imagepool[256];		/*enough?*/
static int nimagepool = 0;
static struct textpool {
	int pfx;
	int xoffset;
	int yoffset;     /* offset for super/subscript */
	int xsiz;
	struct fontmap *font;
	int size;
	char *text;
	u_long fore;
	u_long back;
	char *charset;
} textpool[1024];		/*enough?*/
static int ntextpool = 0;

u_long fore;
u_long back;

/* lang code */
#define ASCII	0
#define KANJI	1
#define NOPE	-1

static int maxfontid = 0;
static struct fontmap {
	int ctrl;
	int lang;
	const char *font;
	const char *psfont;
	int fontid;	/* flag bit for generating font map */
	int loaded;
} fontmap[] = {
	{ CTL_XFONT2, ASCII, "century schoolbook l-medium-r", "NewCenturySchlbk-Roman", 0, 0 },
	{ CTL_XFONT2, ASCII, "century schoolbook l-medium-i", "NewCenturySchlbk-Italic", 0, 0 },
	{ CTL_XFONT2, ASCII, "century schoolbook l-bold-r", "NewCenturySchlbk-Bold", 0, 0 },
	{ CTL_XFONT2, ASCII, "century schoolbook l-bold-i", "NewCenturySchlbk-BoldItalic", 0, 0 },
	{ CTL_XFONT2, ASCII, "times-medium-r",	"Times-Roman", 0, 0 },
	{ CTL_XFONT2, ASCII, "times-medium-i",	"Times-Italic", 0, 0 },
	{ CTL_XFONT2, ASCII, "times-bold-r",	"Times-Bold", 0, 0 },
	{ CTL_XFONT2, ASCII, "times-bold-i",	"Times-BoldItalic", 0, 0 },
	{ CTL_XFONT2, ASCII, "helvetica-medium-r",	"Helvetica", 0, 0 },
	{ CTL_XFONT2, ASCII, "helvetica-medium-o",	"Helvetica-Oblique", 0, 0 },
	{ CTL_XFONT2, ASCII, "helvetica-bold-r",	"Helvetica-Bold", 0, 0 },
	{ CTL_XFONT2, ASCII, "helvetica-bold-o",	"Helvetica-BoldOblique", 0, 0 },
	{ CTL_XFONT2, ASCII, "courier-medium-r",	"Courier", 0, 0 },
	{ CTL_XFONT2, ASCII, "courier-medium-o",	"Courier-Oblique", 0, 0 },
	{ CTL_XFONT2, ASCII, "courier-bold-r",	"Courier-Bold", 0, 0 },
	{ CTL_XFONT2, ASCII, "courier-bold-i",	"Courier-BoldOblique", 0, 0 },
	{ CTL_XFONT2, ASCII, "times*",		"Times", 0, 0 },
	{ CTL_XFONT2, ASCII, "helvetica*",	"Helvetica", 0, 0 },
	{ CTL_XFONT2, ASCII, "courier*",		"Courier", 0, 0 },
	{ CTL_XFONT2, ASCII, "symbol*",		"Symbol", 0, 0 },
	{ CTL_XFONT2, ASCII, "*",		"Helvetica", 0, 0 },	/*last resort*/
	{ CTL_XFONT2, KANJI, "*",		"Ryumin-Light-H", 0, 0 }, /*last resort*/
	/*PSFONT*/
	{ CTL_PSFONT, ASCII, "Times-Roman", "Times-Roman", 0, 0 },
	{ CTL_PSFONT, ASCII, "Times-Italic", "Times-Italic", 0, 0 },
	{ CTL_PSFONT, ASCII, "Times-Bold", "Times-Bold", 0, 0 },
	{ CTL_PSFONT, ASCII, "Times-BoldItalic", "Times-BoldItalic", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetica", "Helvetica", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetica-BoldOblique", "Helvetica-BoldOblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetica-Bold", "Helvetica-Bold", 0, 0 },
	{ CTL_PSFONT, ASCII, "Courier", "Courier", 0, 0 },
	{ CTL_PSFONT, ASCII, "Courier-Oblique", "Courier-Oblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "Courier-Bold", "Courier-Bold", 0, 0 },
	{ CTL_PSFONT, ASCII, "Courier-BoldOblique", "Courier-BoldOblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "AvantGarde-Book", "AvantGarde-Book", 0, 0 },
	{ CTL_PSFONT, ASCII, "AvantGarde-BookOblique", "AvantGarde-BookOblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "AvantGarde-Demi", "AvantGarde-Demi", 0, 0 },
	{ CTL_PSFONT, ASCII, "AvantGarde-DemiQblique", "AvantGarde-DemiQblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "Bookman-Demi", "Bookman-Demi", 0, 0 },
	{ CTL_PSFONT, ASCII, "Bookman-DemiItalic", "Bookman-DemiItalic", 0, 0 },
	{ CTL_PSFONT, ASCII, "Bookman-Light", "Bookman-Light", 0, 0 },
	{ CTL_PSFONT, ASCII, "Bookman-LightItalic", "Bookman-LightItalic", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetica-Narrow", "Helvetica-Narrow", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetic-NarrowOblique","Helvetic-NarrowOblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetica-NarrowBold", "Helvetica-NarrowBold", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetica-NarrowBoldOblique", "Helvetica-NarrowBoldOblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "Helvetica-Oblique", "Helvetica-Oblique", 0, 0 },
	{ CTL_PSFONT, ASCII, "NewCenturySchlbk-Roman", "NewCenturySchlbk-Roman", 0, 0 },
	{ CTL_PSFONT, ASCII, "NewCenturySchlbk-Bold", "NewCenturySchlbk-Bold", 0, 0 },
	{ CTL_PSFONT, ASCII, "NewCenturySchlbk-Italic", "NewCenturySchlbk-Italic", 0, 0 },
	{ CTL_PSFONT, ASCII, "NewCenturySchlbk-BoldItalic", "NewCenturySchlbk-BoldItalic", 0, 0 },
	{ CTL_PSFONT, ASCII, "Palatino-Roman", "Palatino-Roman", 0, 0 },
	{ CTL_PSFONT, ASCII, "Palatino-Bold", "Palatino-Bold", 0, 0 },
	{ CTL_PSFONT, ASCII, "Palatino-Italic", "Palatino-Italic", 0, 0 },
	{ CTL_PSFONT, ASCII, "Palatino-BoldItalic", "Palatino-BoldItalic", 0, 0 },
	{ CTL_PSFONT, ASCII, "ZapfChancery-MediumItalic", "ZapfChancery-MediumItalic", 0, 0 },
	{ CTL_PSFONT, ASCII, "Symbol", "Symbol", 0, 0 },
	{ CTL_PSFONT, ASCII, "ZapfDingbats", "ZapfDingbats", 0, 0 },
	{ CTL_PSFONT, ASCII, "*", "Helvetica", 0, 0 }, /*Last Resort*/
	{ -1, 0, NULL, NULL, 0, 0 }
};
static struct fontmap *curfont[10];	/*indexed by lang*/

typedef struct papersize {
	const char *name;	/* name of paper size */
	int height, width;	/* height, width in points for LANDSCAPE */
} Paper;

static const Paper papersizes[] = {
	{ "a3",		842, 1191 },	/* 29.7cm * 42cm */
	{ "a4",		595, 842 },	/* 21cm * 29.7cm */
	{ "a5",		421, 595 },	/* 14.85cm * 21cm */
	{ "b5",		516, 729 },	/* 18.2cm * 25.72cm */
	{ "A3",		842, 1191 },	/* 29.7cm * 42cm */
	{ "A4",		595, 842 },	/* 21cm * 29.7cm */
	{ "A5",		421, 595 },	/* 14.85cm * 21cm */
	{ "B5",		516, 729 },	/* 18.2cm * 25.72cm */
	{ "letter",	612, 792 },	/* 8.5in * 11in */
	{ "legal",	612, 1008 },	/* 8.5in * 14in */
	{ "ledger",	1224, 792 },	/* 17in * 11in */
	{ "tabloid",	792, 1224 },	/* 11in * 17in */
	{ "statement",	396, 612 },	/* 5.5in * 8.5in */
	{ "executive",	540, 720 },	/* 7.6in * 10in */
	{ "folio",	612, 936 },	/* 8.5in * 13in */
	{ "quarto",	610, 780 },	/* 8.5in * 10.83in */
	{ "10x14",	720, 1008 },	/* 10in * 14in */
	{ NULL,	0, 0 }
};

static const char *latin1def =
"[ /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /space /exclam /quotedbl /numbersign /dollar /percent /ampersand /quoteright\n"
" /parenleft /parenright /asterisk /plus /comma /hyphen /period /slash /zero /one\n"
" /two /three /four /five /six /seven /eight /nine /colon /semicolon\n"
" /less /equal /greater /question /at /A /B /C /D /E\n"
" /F /G /H /I /J /K /L /M /N /O\n"
" /P /Q /R /S /T /U /V /W /X /Y\n"
" /Z /bracketleft /backslash /bracketright /asciicircum /underscore /quoteleft /a /b /c\n"
" /d /e /f /g /h /i /j /k /l /m\n"
" /n /o /p /q /r /s /t /u /v /w\n"
" /x /y /z /braceleft /bar /braceright /asciitilde /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /space /exclamdown /cent /sterling /currency /yen /brokenbar /section /dieresis /copyright\n"
" /ordfeminine /guillemotleft /logicalnot /hyphen /registered /macron /degree /plusminus /twosuperior /threesuperior\n"
" /acute /mu /paragraph /periodcentered /cedilla /onesuperior /ordmasculine /guillemotright /onequarter /onehalf\n"
" /threequarters /questiondown /Agrave /Aacute /Acircumflex /Atilde /Adieresis /Aring /AE /Ccedilla\n"
" /Egrave /Eacute /Ecircumflex /Edieresis /Igrave /Iacute /Icircumflex /Idieresis /Eth /Ntilde\n"
" /Ograve /Oacute /Ocircumflex /Otilde /Odieresis /multiply /Oslash /Ugrave /Uacute /Ucircumflex\n"
" /Udieresis /Yacute /Thorn /germandbls /agrave /aacute /acircumflex /atilde /adieresis /aring\n"
" /ae /ccedilla /egrave /eacute /ecircumflex /edieresis /igrave /iacute /icircumflex /idieresis\n"
" /eth /ntilde /ograve /oacute /ocircumflex /otilde /odieresis /divide /oslash /ugrave\n"
" /uacute /ucircumflex /udieresis /yacute /thorn /ydieresis] /isolatin1encoding exch def\n";

static const char *latin2def =
"[ /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /space /exclam /quotedbl /numbersign /dollar /percent /ampersand /quoteright\n"
" /parenleft /parenright /asterisk /plus /comma /minus /period /slash /zero /one\n"
" /two /three /four /five /six /seven /eight /nine /colon /semicolon\n"
" /less /equal /greater /question /at /A /B /C /D /E\n"
" /F /G /H /I /J /K /L /M /N /O\n"
" /P /Q /R /S /T /U /V /W /X /Y\n"
" /Z /bracketleft /backslash /bracketright /asciicircum /underscore /quoteleft /a /b /c\n"
" /d /e /f /g /h /i /j /k /l /m\n"
" /n /o /p /q /r /s /t /u /v /w\n"
" /x /y /z /braceleft /bar /braceright /asciitilde /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef\n"
" /space /Aogonek /breve /Lslash /currency /Lcaron /Sacute /section /dieresis /Scaron\n"
" /Scedilla /Tcaron /Zacute /hyphen /Zcaron /Zdotaccent /degree /aogonek /ogonek /lslash\n"
" /acute /lcaron /sacute /caron /cedilla /scaron /scedilla /tcaron /zacute /hungarumlaut\n"
" /zcaron /zdotaccent /Racute /Aacute /Acircumflex /Abreve /Adieresis /Lacute /Cacute /Ccedilla\n"
" /Ccaron /Eacute /Eogonek /Edieresis /Ecaron /Iacute /Icircumflex /Dcaron /Dbar /Nacute\n"
" /Ncaron /Oacute /Ocircumflex /Ohungarumlaut /Odieresis /multiply /Rcaron /Uring /Uacute /Uhungarumlaut\n"
" /Udieresis /Yacute /Tcedilla /germandbls /racute /aacute /acircumflex /abreve /adieresis /lacute\n"
" /cacute /ccedilla /ccaron /eacute /eogonek /edieresis /ecaron /iacute /icircumflex /dcaron\n"
" /dbar /nacute /ncaron /oacute /ocircumflex /ohungarumlaut /odieresis /divide /rcaron /uring\n"
" /uacute /uhungarumlaut /udieresis /yacute /tcedilla /dotaccent] /isolatin2encoding exch def\n";

static const struct encoding {
	const char *name;
	const char *defstr;
} encodings[] = {
	{ "iso-8859-1",	"isolatin1encoding", },
	{ "latin1",	"isolatin1encoding", },
	{ "iso-8859-2",	"isolatin2encoding", },
	{ "latin2",	"isolatin2encoding", },
	{ NULL, NULL, },
};
const struct encoding *encoding = NULL;

static const Paper *paper;
static int w_width, w_height;
static int paper_xmargin = 10;
static int paper_ymargin = 5;

static const Paper *findpaper(const char *);
static void print_out(void);
static void scan_font(u_int);
static void print_page(u_int);
static void print_init(void);
static void print_pageinit(u_int);
static void process_direc_print(struct ctrl *cp);
static void scan_backcolor(u_int);
static void line_start(void);
static void line_end(void);
static void icon_output(struct textpool *);
static void line_skip(int, int);
static void print_bar(struct ctrl *);
static void print_icon(struct ctrl *);
static void print_line(char *, int);
static void print_string(char *, int);
static void print_fragment(u_char *, u_int, int, int);
static void text_remember(char *, int, int, int, int);
static void icon_remember(int, int, int, u_long);
static void image_remember(struct ctrl *, struct imagepool *);
static void image_load_print(const char *, int, int, int, int, int);
static void print_usage(char *) __dead;
static void setpencolor(u_long);
static void print_full_image(XImage *, Visual *);
static void print_8_image(XImage *);
static void print_32_image(XImage *, Visual *);
static void print_eps(FILE *, char *, int, int, int);
static struct fontmap *findfont(int, int, char *);
static char *fontstring(struct fontmap *);
static void loadfont(struct fontmap *, const char *);
static int  count_pause(u_int page);   /* Added 23.07.2004 by Arnd Schmitter */

/*
 * Paper size selection code is based on psutil.c by Angus J. C. Duggan
 * but trivial
 */
static const Paper *
findpaper(const char *name)
{
	const Paper *pp;
	for (pp = papersizes; pp->name; pp++) {
		if (strcmp(pp->name, name) == 0)
			return pp;
	}
	return (const Paper *)NULL;
}

static void
print_out(void)
{
	u_int	i;
	u_int	width, height;

	width  = window_width;
	height = window_height;
	window_width  = w_width;
	window_height = w_height;

	char_size[0] = window_height * DEFAULT_CHARSIZE / 100;
	sup_off = DEFAULT_SUPOFF;
	sub_off = DEFAULT_SUBOFF;
	sup_scale = DEFAULT_SUPSCALE;

	if (outputfile[0]) {
		if ((fp = fopen(outputfile, "w")) == NULL) {
			perror("fopen");
			exit(-1);
		}
	} else
		fp = stdout;

	print_init();
	for (i = 1; i <= maxpage; i ++) {
		scan_font(i);
	}
	for (i = 1; i <= maxpage; i ++)
	  {
	    lg=0; /* Set the recursionlevel to 0 for each new page (Arnd Schmitter 23.07.2004) */
	    print_page(i);
	  }

	fclose(fp);

	window_width  = width;
	window_height = height;
}

static void
scan_font(u_int page)
{
	u_int line;
	struct ctrl *cp;
	struct fontmap *font;
	int code;

	if (mgp_flag & FL_VERBOSE)
		fprintf(fp, "%% scan_font page %d\n", page);
	for (line = 0;
	     line <= page_attribute[page].pg_linenum;
	     line++) {
		/* process normal control */
		for (cp = page_control[page][line]; cp; cp = cp->ct_next) {
			switch(cp->ct_op) {
			case CTL_PSFONT:
				font = findfont(cp->ct_op,ASCII,cp->ctc_value);
				if(font)
					curfont[ASCII] = font;
				break;

			case CTL_XFONT2:
				if (strcmp(cp->ctc2_value2, "iso8859-1") == 0)
					code = ASCII;
				else if (strncmp(cp->ctc2_value2,
						"jisx0208", 8) == 0) {
					code = KANJI;
				} else {
					fprintf(stderr,
						"unsupported XFONT registry %s\n",
						cp->ctc2_value2);
					exit(1);
				}
				font = findfont(cp->ct_op, code,
					cp->ctc2_value1);
				if (font)
					curfont[code] = font;
				break;

			case CTL_XFONT:
				fprintf(stderr, "obsolete directive XFONT\n");
				exit(1);
			case CTL_TEXT:
				if (strstr(cp->ctc_value, "\033$B")
				 || strstr(cp->ctc_value, "\033$@")) {
					if (curfont[KANJI]) {
						loadfont(curfont[KANJI],
							curfont[KANJI]->font);
					} else {
						fprintf(stderr,
"unable to find proper font for %s\n", "kanji");
						exit(1);
					}
				}
				if (curfont[ASCII]) {
					loadfont(curfont[ASCII],
						curfont[ASCII]->font);
				} else {
					fprintf(stderr,
"unable to find proper font for %s\n", "ascii");
					exit(1);
				}

			default:
				break;
			}
		}
	}

	memset(curfont, 0, sizeof(curfont));
}

/*
 * This Function counts the Number of Pause-Statements.
 */
static int
count_pause(u_int page)
{
  int count=0;
  u_int line;
  struct ctrl*cp;
  for (line = 0;
       line <= page_attribute[page].pg_linenum;
       line++) {
    curlinenum = line;
    /* process normal control */
    for (cp = page_control[page][line]; cp; cp = cp->ct_next)
      {
	if(cp->ct_op == CTL_PAUSE)
	  {
	    count++;
	  }
      }
  }
  return count;
}

static void
print_page(u_int page)
{
	u_int line;
	struct ctrl *cp;

	int ret=0;  /* A Pause Command was found */
	int lc = 0; /* Counter to prevent infinite recursion loops at Pause-Statements */
	int pcount; /* Stores the Number of Pause Statements for a Page */

	/* Modified by Arnd Schmitter 23.07.2004:
     * when in PauseMode for emulationg %pause in PS file (option "-m")
     * this function is called recursively to print the beginning of
     * the page again up to current %pause statement
     */
	/*curpagenum = page;*/
	curpagenum++;
	scan_backcolor(page);
	print_pageinit(curpagenum);
	/* End of Modification */

	for (line = 0;
	     line <= page_attribute[page].pg_linenum;
	     line++) {
		curlinenum = line;
		/* process normal control */
		for (cp = page_control[page][line]; cp; cp = cp->ct_next)
		  {
		    if ((PauseMode) && (cp->ct_op == CTL_PAUSE))
		      if (lc >= lg)
			ret=1;
		      else
			(lc)++;
		    else
		      process_direc_print(cp);

		    if (ret) goto pause; /* If a Pause has been found exit Loop immedeatly */
		  }
	}
 pause:
	fprintf(fp, "grestore\n\n");
	fprintf(fp, "showpage\n\n");
	/* If a Pause has been found -> Restart this Page */
	if (ret)
	  {
	    lg++;
	    /* If we are in Pause-Mode check if we are called because of the last CTL_PAUSE at End of Page */
	    /*   At the Endo of the Last Page is no PAUSE Statemtent. So we have to use all Statements in the
	     Page */
	    pcount=count_pause(page);
	    if ((pcount != lg)||(page == (maxpage)))
	      print_page(page);
	  }
}

static void
print_init(void)
{
	const char **p;
	const char *aligns[] = { "center", "left", "right", NULL };

	/* Added by Arnd Schmitter 23.07.2004 */
	/*   If we split Pages with Pause Statements the number of absolute Pages gets greater then maxpage */
	u_int i;
	int absolute_pages = maxpage+1;
	if (PauseMode)
	  {
	    for (i = 0; i < maxpage; i++)
	      {
		absolute_pages+=count_pause(i)-1;
	      }
	  }

	/*fprintf(stderr,"Pages %d\n",absolute_pages);*/

	fprintf(fp, "%%!PS-Adobe-2.0\n");
	fprintf(fp, "%%%%Creator: mgp2ps\n");
	fprintf(fp, "%%%%Title: %s\n", mgp_fname);
	fprintf(fp, "%%%%Pages: %d\n", absolute_pages);
	fprintf(fp, "%%%%BoundingBox: 0 0 %d %d\n", paper->height, paper->width);
	fprintf(fp, "%%%%DocumentPaperSizes: %s\n", paper->name);
	fprintf(fp, "%%%%Orientation: Landscape\n");
	fprintf(fp, "%%%%EndComments\n");

	/* define constants */
	fprintf(fp, "/XMARGIN %d def /YMARGIN %d def "
		"/WIDTH %d def /HEIGHT %d def\n",
		paper_xmargin, paper_ymargin, window_width, window_height);
	fprintf(fp, "/XBODY WIDTH XMARGIN 2 mul sub def\n");
	fprintf(fp, "/vertgap %d def /horizgap %d def\n", vert_gap[0], horiz_gap[0]);

	fprintf(fp, "/resety {/ymin 0 def /ymax 0 def} def\n");
	fprintf(fp, "/setymax {dup ymax gt {dup /ymax exch def} if pop} def\n");
	fprintf(fp, "/setymin {dup ymin lt {dup /ymin exch def} if pop} def\n");
	fprintf(fp, "/calcy {dup ( ) eq\n"
		"  {0 setymax 0 setymin pop}\n"
		"  {pop charsize 0.7 mul setymax charsize -0.15 mul setymin}\n"
		"  ifelse\n"
		"} def\n");

	fprintf(fp, "resety\n");

	/* define writebox */
	fprintf(fp, "/writebox {\n");
	fprintf(fp, "  XMARGIN YMARGIN -1 mul moveto "
			"0 HEIGHT -1 mul rlineto\n");
	fprintf(fp, "  WIDTH 0 rlineto 0 HEIGHT rlineto\n");
	fprintf(fp, "  WIDTH -1 mul 0 rlineto stroke\n");
	fprintf(fp, "} def\n");

	/* define writeboxfill */
	fprintf(fp, "/writeboxfill {\n");
	fprintf(fp, "  newpath XMARGIN YMARGIN -1 mul moveto "
			"0 HEIGHT -1 mul rlineto\n");
	fprintf(fp, "  WIDTH 0 rlineto 0 HEIGHT rlineto\n");
	fprintf(fp, "  WIDTH -1 mul 0 rlineto closepath eofill stroke\n");
	fprintf(fp, "} def\n");

	/* ypos = ypos - (charsize * (1 + vert_gap / 100)) + little margin */
	fprintf(fp, "/NL {\n");
	fprintf(fp, "  charsize imgsize gt\n");
	fprintf(fp, "	ymax ymin sub /csize exch def\n");
	fprintf(fp, "	csize 0 eq {/csize charsize def} if\n");
	fprintf(fp, "    { vertgap 100 div 1 add csize mul }\n");
	fprintf(fp, "    { vertgap 100 div csize mul imgsize add }\n");
	fprintf(fp, "  ifelse\n");
	fprintf(fp, "  ypos exch sub /ypos exch def\n");
	/* we can get similar results to xft2 by doing this */
	if (!gaplevel)
		fprintf(fp, "  HEIGHT 90 div ypos exch sub /ypos exch def\n");
	fprintf(fp, "} bind def\n");

	/* show with line wrapping */
	fprintf(fp, "/initcharsize { /charsize 0 def /imgsize 0 def resety} def\n");
	fprintf(fp, "initcharsize\n");
	fprintf(fp, "/setcharsize {\n");
	fprintf(fp, "  dup charsize gt { dup /charsize exch def } if pop\n");
	fprintf(fp, "} def\n");
	fprintf(fp, "/setimgsize {\n");
	fprintf(fp, "  dup imgsize gt { dup /imgsize exch def } if pop\n");
	fprintf(fp, "} def\n");

	fprintf(fp, "/updatetotlen {\n");
	fprintf(fp, "  dup totlen exch sub /totlen exch def\n");
	fprintf(fp, "} bind def\n");

	fprintf(fp, "/updatefillzero {\n");
	fprintf(fp, "  inmargin {\n");
	fprintf(fp, "    currentpoint pop /fillzero exch def\n");
	fprintf(fp, "    /inmargin false def\n");
	fprintf(fp, "  } if\n");
	fprintf(fp, "} bind def\n");

	/* centering */
	fprintf(fp, "/centerdefxpos {\n");
	fprintf(fp, "  totlen XBODY gt\n");
	fprintf(fp, "    { XMARGIN }\n");
	fprintf(fp, "    { XBODY totlen sub 2 div XMARGIN add }\n");
	fprintf(fp, "  ifelse /xpos exch def\n");
	fprintf(fp, "} bind def\n");

	/* leftfill */
	fprintf(fp, "/leftdefxpos {\n");
	fprintf(fp, "  /xpos fillzero def\n");
	fprintf(fp, "} bind def\n");

	/* rightfill */
	fprintf(fp, "/rightdefxpos {\n");
	fprintf(fp, "  totlen XBODY gt\n");
	fprintf(fp, "    { XMARGIN }\n");
	fprintf(fp, "    { XBODY totlen sub XMARGIN add }\n");
	fprintf(fp, "  ifelse /xpos exch def\n");
	fprintf(fp, "} bind def\n");

	/* check newline */
	for (p = aligns; *p; p++) {
		fprintf(fp, "/%snewlinecheck {\n", *p);
		fprintf(fp, "  currentpoint pop add XMARGIN XBODY add gt {\n");
		fprintf(fp, "    NL %sdefxpos xpos ypos charsize 2 div sub moveto\n",
			*p);
		fprintf(fp, "  } if\n");
		fprintf(fp, "} bind def\n");
	}

	/* a spell for EPS */
	fprintf(fp, "%%\n");
	fprintf(fp, "/BeginEPSF {%%def\n");
	fprintf(fp, "  /b4_Inc_state save def\n");
	fprintf(fp, "  /dict_count countdictstack def\n");
	fprintf(fp, "  /op_count count 1 sub def\n");
	fprintf(fp, "  userdict begin\n");
	fprintf(fp, "  /showpage {}def\n");
	fprintf(fp, "  0 setgray 0 setlinecap\n");
	fprintf(fp, "  1 setlinewidth 0 setlinejoin\n");
	fprintf(fp, "  10 setmiterlimit [] 0 setdash\n");
	fprintf(fp, "  newpath\n");
	fprintf(fp, "  /languagelevel where\n");
	fprintf(fp, "  {pop languagelevel\n");
	fprintf(fp, "  1 ne\n");
	fprintf(fp, "    {false setstrokeadjust\n");
	fprintf(fp, "     false setoverprint\n");
	fprintf(fp, "    }if\n");
	fprintf(fp, "  }if\n");
	fprintf(fp, "}bind def\n");
	fprintf(fp, "%%\n");
	fprintf(fp, "/EndEPSF {%%def\n");
	fprintf(fp, "  count op_count sub {pop}repeat\n");
	fprintf(fp, "  countdictstack\n");
	fprintf(fp, "  dict_count sub {end}repeat\n");
	fprintf(fp, "  b4_Inc_state restore\n");
	fprintf(fp, "}bind def\n");
	fprintf(fp, "%%\n");

	/* emit character encoding definition, if necessary */
	if (encoding && strcmp(encoding->defstr, "isolatin1encoding") == 0)
		fprintf(fp, "%s", latin1def);
	if (encoding && strcmp(encoding->defstr, "isolatin2encoding") == 0)
		fprintf(fp, "%s", latin2def);
}

static void
print_pageinit(u_int page)
{

	window_width  = paper->width - paper_xmargin * 2;
	window_height = paper->height - paper_ymargin * 2;
	fprintf(fp, "%%%%Page: %d %d\n", page, page);
	fprintf(fp, "/ypos YMARGIN -1 mul 4 sub def\n");
	fprintf(fp, "/xpos 0 def\n");
	fprintf(fp, "initcharsize\n");
	if (!colorps)
		fprintf(fp, "90 rotate newpath writebox\n");
	else {
		setpencolor(back);
		fprintf(fp, "90 rotate newpath writeboxfill\n");
	}
	fprintf(fp, "gsave\n");
}

static void
process_direc_print(struct ctrl *cp)
{
	struct fontmap *font;
	int code;

	switch(cp->ct_op) {
	case CTL_SIZE:
		char_size[0] = window_height * cp->ctf_value / 100;
		fprintf(fp, "%d setcharsize\n", char_size[0]);
		break;

	case CTL_PSFONT:
		font = findfont(cp->ct_op,ASCII,cp->ctc_value);
		if(font)
			curfont[ASCII] = font;
		break;

	case CTL_XFONT2:
		if (strcmp(cp->ctc2_value2, "iso8859-1") == 0)
			code = ASCII;
		else if (strncmp(cp->ctc2_value2, "jisx0208", 8) == 0)
			code = KANJI;
		else {
			fprintf(stderr, "unsupported XFONT registry %s\n",
				cp->ctc2_value2);
			exit(1);
		}
		font = findfont(cp->ct_op, code, cp->ctc2_value1);
		if (font)
			curfont[code] = font;
		break;

	case CTL_XFONT:
		fprintf(stderr, "internal error: "
			"obsolete directive CTL_XFONT\n");
		exit(1);

	case CTL_VGAP:
		vert_gap[0] = cp->cti_value;
		fprintf(fp, "/vertgap %d def\n", cp->cti_value);
		break;

	case CTL_HGAP:
		horiz_gap[0] = cp->cti_value;
		fprintf(fp, "/horizgap %d def\n", cp->cti_value);
		break;

	case CTL_GAP:
		vert_gap[0] = horiz_gap[0] = cp->cti_value;
		fprintf(fp, "/vertgap %d def ", cp->cti_value);
		fprintf(fp, "/horizgap %d def\n", cp->cti_value);
		break;

	case CTL_CENTER:
		align = AL_CENTER;
		break;

	case CTL_LEFT:
		align = AL_LEFT;
		break;

	case CTL_LEFTFILL:
		align = AL_LEFTFILL0;
		break;

	case CTL_RIGHT:
		align = AL_RIGHT;
		break;

	case CTL_IMAGE:
		image_remember(cp, &imagepool[nimagepool]);
		line_skip(imagepool[nimagepool].xsiz,
			imagepool[nimagepool].ysiz);

		/* placeholder */
		textpool[ntextpool].pfx = 0;
		textpool[ntextpool].xoffset = 0;
		textpool[ntextpool].xsiz = imagepool[nimagepool].xsiz;
		textpool[ntextpool].size = imagepool[nimagepool].ysiz;
		textpool[ntextpool].font = 0;		/*image*/
		textpool[ntextpool].text = NULL;
		textpool[ntextpool].fore = fore;	/*XXX*/
		textpool[ntextpool].back = back;	/*XXX*/
		ntextpool++;

		nimagepool++;
		break;

	case CTL_BAR:
		print_bar(cp);
		break;

	case CTL_PREFIX:
		curprefix = cp->ctc_value;
		break;

	case CTL_PREFIXN:
		xprefix = window_width * cp->ctf_value / 100;
		break;

	case CTL_TABPREFIX:
		tabprefix = cp->ctc_value;
		break;

	case CTL_TABPREFIXN:
		tabxprefix = window_width * cp->ctf_value / 100;
		break;

	case CTL_PREFIXPOS:
		if (tabprefix)
			print_line(tabprefix, 1);
		else if (curprefix)
			print_line(curprefix, 1);
		break;

	case CTL_TEXT:
	    {
		if (!cp->ctc_value)
			break;
		print_line(cp->ctc_value, 0);
		if (align == AL_LEFTFILL0)
			align = AL_LEFTFILL1;

		/* disable super/subscript for next line */
		if (char_off != 0)
		{
			char_size[0]=nonscaled_size[0];
			char_off=0;
		}
		break;
	    }

	case CTL_ICON:
		print_icon(cp);
		break;

	case CTL_LINESTART:
		line_start();
		break;

	case CTL_LINEEND:
		line_end();
		if (align == AL_LEFTFILL1)
			align = AL_LEFTFILL0;
		break;

	case CTL_FORE:
		fore = cp->ctl_value;
		break;

#if 0 /* it's not necessary */
	case CTL_BACK:
		back = cp->ctl_value;
		break;
#endif

	/*
     * MARK and AGAIN processing by A.Ito, 14 Jul. 2000
     */
	case CTL_MARK:
		fprintf(fp,"/markx xpos def /marky ypos def\n");
		break;

	case CTL_AGAIN:
		fprintf(fp,"markx marky moveto /xpos markx def /ypos marky def\n");
		break;

	case CTL_VALIGN:
		valign = cp->cti_value;
		break;

	case CTL_AREA:
		window_width = (paper->width - paper_xmargin * 2) * cp->ctar_width / 100;
		window_height = (paper->height - paper_ymargin * 2) * cp->ctar_height / 100;
		fprintf(fp, "grestore\ngsave\n");
		fprintf(fp, "%f dup scale\n", cp->ctar_height / 100.0);
		fprintf(fp, "WIDTH XMARGIN 2 mul sub %f mul 0 translate\n",
		    (double)cp->ctar_xoff / cp->ctar_height);
		fprintf(fp, "/XBODY WIDTH XMARGIN 2 mul sub %f mul def\n",
		    (double)cp->ctar_width / cp->ctar_height);
		fprintf(fp, "/ypos YMARGIN -1 mul 4 sub HEIGHT %f mul sub def\n",
		    (double)cp->ctar_yoff / cp->ctar_height);
		fprintf(fp, "/xpos 0 def\n");
		break;
	case CTL_OPAQUE:
		if (cp->cti_value > 100){
			fprintf(stderr, "%%opaque: value should be 0-100\n");
			cp->cti_value = 100;
		}
		fprintf(fp, "%f setgray\n", 1.0 - (float)cp->cti_value /100.0);
		break;

	/* setup for super/subscript */
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

	/* subscript */
	case CTL_SUB:
		/* save old size and set new one */
		nonscaled_size[0] = char_size[0];
		char_size[0] = char_size[0] * sup_scale;
		char_off = nonscaled_size[0] * (-sub_off);
		fprintf(fp, "%d setcharsize\n", char_size[0]);
		break;

	/* superscript */
	case CTL_SUP:
		/* save old size and set new one */
		nonscaled_size[0] = char_size[0];
		char_size[0] = char_size[0] * sup_scale;
		char_off = nonscaled_size[0] * sup_off;
		fprintf(fp, "%d setcharsize\n", char_size[0]);
		break;

	default:
	  break;
	}
}

static void
scan_backcolor(u_int page)
{
  struct ctrl *cp;
  u_int line;

	for (line = 0; line <= page_attribute[page].pg_linenum; line++) {
		for (cp = page_control[page][line]; cp; cp = cp->ct_next)
			switch(cp->ct_op) {
				case CTL_BACK:
					back = cp->ctl_value;
				break;
				default:
				break;
		}
	}
}

static void
line_start(void)
{
	linewidth = 0;
	nimagepool = 0;
	ntextpool = 0;
}

static void
line_end(void)
{
	int i;
	const char *alignstr;

	switch (align) {
	case AL_CENTER:
		alignstr = "center";
		break;
	case AL_LEFT:
	case AL_LEFTFILL0:
	case AL_LEFTFILL1:
		alignstr = "left";
		break;
	case AL_RIGHT:
		alignstr = "right";
		break;
	default:
		alignstr = "";
		break;
	}

	/*
	 * line output starts here
	 */
	if (mgp_flag & FL_VERBOSE)
		fprintf(fp, "%% a line starts here\n");

	/*
	 * if we have nothing on the line, skip it
	 */
	if (!ntextpool && !nimagepool){
		fprintf(fp, "initcharsize %d setcharsize\n", char_size[0]);
		goto done;
	}

	/*
	 * push strings in reverse order.
	 */
	fprintf(fp, "initcharsize\n");
	fprintf(fp, "0 %% sentinel for text width computation\n");
	fprintf(fp, "gsave newpath 0 0 moveto\n");

	for (i = ntextpool - 1; 0 <= i; i--) {
		if (!textpool[i].text) {
			fprintf(fp, "%d add\n", textpool[i].xsiz);
			continue;
		}
		fprintf(fp, "%d setcharsize %d %s %s calcy\n",
			textpool[i].size, textpool[i].size,
			fontstring(textpool[i].font),
			textpool[i].text);

		fprintf(fp, "%d setcharsize %d %s %s "
			"1 copy stringwidth pop 3 2 roll add\n",
			textpool[i].size, textpool[i].size,
			fontstring(textpool[i].font),
			textpool[i].text);
	}
	fprintf(fp, "grestore\n");

	if (mgp_flag & FL_VERBOSE) {
		fprintf(fp, "%% stack should have: str3 str2 str1 width\n");
		fprintf(fp, "%% alignment: %s\n", alignstr);
	}

	/*
	 * now show those chars with line wrapping. brief logic is:
	 *
	 *	position adjust;
	 *	foreach i (item in textpool) {
	 *		if (item exceeds the right edge) {
	 *			carriage return;
	 *			position adjust;
	 *		}
	 *		show;
	 *	}
	 */
	fprintf(fp, "/totlen exch def\n");
	fprintf(fp, "/totlen totlen %d add def\n", tabxprefix ? tabxprefix:xprefix);
	fprintf(fp, "/inmargin true def /fillzero XMARGIN def\n");
	fprintf(fp, "%sdefxpos ", alignstr);

	/*
	 * to determine text base line, we need to set imgsize first. pretty ugly..
	 */
    {
	struct ctrl *cp1;
		for (i = 0; i < nimagepool; i++) {
			cp1 = imagepool[i].image;
			if (cp1)
				fprintf(fp, "%d setimgsize\n", imagepool[i].ysiz);	/*XXX*/
		}
	}
	fprintf(fp, "/yypos ypos charsize imgsize gt \n");

	switch(valign){
	case VL_TOP:
		fprintf(fp, " { 0 } { 0 } ifelse sub def\n");
		break;
	case VL_BOTTOM:
		fprintf(fp, " { 0 } { imgsize charsize sub } ifelse sub def\n");
		break;
	case VL_CENTER:
		fprintf(fp, " { 0 } { imgsize charsize sub 2 div } ifelse sub def\n");
		break;
	}
	fprintf(fp, "/xpos xpos %d add def\n", tabxprefix ? tabxprefix:xprefix);
	fprintf(fp, "xpos yypos ymax sub moveto\n");

	for (i = 0; i < ntextpool; i++) {
		if (textpool[i].text) {
			fprintf(fp, "%d %s 1 copy stringwidth pop ",
				textpool[i].size,
				fontstring(textpool[i].font));
		} else
			fprintf(fp, "%d ", textpool[i].xsiz);
		fprintf(fp, "updatetotlen ");
		if (textpool[i].text)
			fprintf(fp, "%snewlinecheck ", alignstr);
		else
			fprintf(fp, "pop ");

		if (colorps
		 && (i == 0
		  || (textpool[i - 1].fore != textpool[i].fore)
		  || (textpool[i - 1].back != textpool[i].back))) {
			fprintf(fp, "\n");
			setpencolor(textpool[i].fore);
		}

		if (textpool[i].text) {
			if (!textpool[i].pfx)
				fprintf(fp, "updatefillzero ");

			/* for super/subscript: modify the current y-position */
			if (textpool[i].yoffset != 0)
				fprintf(fp, "currentpoint %d add moveto ",
					textpool[i].yoffset);
			fprintf(fp, "show\n");

			/* for super/subscript: reset the old y-position */
			if (textpool[i].yoffset != 0)
				fprintf(fp, "currentpoint %d sub moveto ",
					textpool[i].yoffset);
		} else {
			fprintf(fp, "\n");
			if (textpool[i].font) {
				/* icon */
				icon_output(&textpool[i]);
			} else {
				/* image */
				fprintf(fp, "%d 0 rmoveto\n", textpool[i].xsiz);
			}
		}
		fprintf(fp, "/xpos%d currentpoint pop def\n", i);
	}

	ntextpool = 0;

    {
	struct ctrl *cp1;

	for (i = 0; i < nimagepool; i++) {
		if (mgp_flag & FL_VERBOSE)
			fprintf(fp, "%% emit the content of imagepool\n");
		cp1 = imagepool[i].image;
		if (cp1) {
			image_load_print(cp1->ctm_fname, cp1->ctm_numcolor,
				cp1->ctm_ximagesize, cp1->ctm_yimagesize,
				cp1->ctm_zoomflag, cp1->ctm_rotate);
		}
		fprintf(fp, "/xpos xpos%d def xpos ypos moveto\n",
		    imagepool[i].target_text);

	}
	nimagepool = 0;
    }

done:
	fprintf(fp, "NL\n");
	tabprefix = NULL;
	tabxprefix = 0;
}

static void
icon_output(struct textpool *tp)
{
	int isize = tp->size;
	int csize = tp->xsiz;
	int ixoff, iyoff;
	int paintit;

	iyoff = (csize - isize) / 6;	/*XXX*/
	ixoff = tp->xoffset + (csize - isize) / 2;  /* XXX */

	paintit = (painticon || colorps);

	switch ((size_t)tp->font) {	/*XXX*/
	case 0:
		/* XXX: image is not supported yet */
		break;
	case 1: /* this is box */
		fprintf(fp, "currentpoint ");
		if (paintit)
			fprintf(fp, "currentpoint newpath moveto ");
		fprintf(fp, "%d %d rmoveto ", ixoff, iyoff);
		fprintf(fp, "0 %d rlineto ", isize);
		fprintf(fp, "%d 0 rlineto ", isize);
		fprintf(fp, "0 %d rlineto ", -isize);
		fprintf(fp, "%d 0 rlineto ", -isize);
		if (paintit)
			fprintf(fp, "closepath eofill ");
		fprintf(fp, "stroke moveto\n");
		fprintf(fp, "%d 0 rmoveto\n", ixoff * 2 + isize);
		break;
	case 2: /* this is arc */
		fprintf(fp, "currentpoint ");
		if (paintit)
			fprintf(fp, "currentpoint newpath moveto ");
		fprintf(fp, "%d %d rmoveto ", ixoff + isize, iyoff + isize/2);
		fprintf(fp, "currentpoint exch %d add exch %d 0 360 arc ",
			-isize/2, isize/2);
		if (paintit)
			fprintf(fp, "closepath eofill ");
		fprintf(fp, "stroke moveto\n");
		fprintf(fp, "%d 0 rmoveto\n", ixoff * 2 + isize);
		break;
	case 3:
		fprintf(fp, "currentpoint ");
		if (paintit)
			fprintf(fp, "currentpoint newpath moveto ");
		fprintf(fp, "%d %d rmoveto ", ixoff, iyoff);
		fprintf(fp, "%d 0 rlineto ", isize);
		fprintf(fp, "%d %d rlineto ", -isize/2, isize);
		fprintf(fp, "%d %d rlineto ", -isize/2, -isize);
		if (paintit)
			fprintf(fp, "closepath eofill ");
		fprintf(fp, "stroke moveto\n");
		fprintf(fp, "%d 0 rmoveto\n", ixoff * 2 + isize);
		break;
	case 4:
		fprintf(fp, "currentpoint ");
		if (paintit)
			fprintf(fp, "currentpoint newpath moveto ");
		fprintf(fp, "%d %d rmoveto ", ixoff, iyoff + isize);
		fprintf(fp, "%d 0 rlineto ", isize);
		fprintf(fp, "%d %d rlineto ", -isize/2, -isize);
		fprintf(fp, "%d %d rlineto ", -isize/2, isize);
		if (paintit)
			fprintf(fp, "closepath eofill ");
		fprintf(fp, "stroke moveto\n");
		fprintf(fp, "%d 0 rmoveto\n", ixoff * 2 + isize);
		break;
	case 5:
		fprintf(fp, "currentpoint ");
		if (paintit)
			fprintf(fp, "currentpoint newpath moveto ");
		fprintf(fp, "%d %d rmoveto ", ixoff, iyoff);
		fprintf(fp, "0 %d rlineto ", isize);
		fprintf(fp, "%d %d rlineto ", isize, -isize/2);
		fprintf(fp, "%d %d rlineto ", -isize, -isize/2);
		if (paintit)
			fprintf(fp, "closepath eofill ");
		fprintf(fp, "stroke moveto\n");
		fprintf(fp, "%d 0 rmoveto\n", ixoff * 2 + isize);
		break;
	case 6:
		fprintf(fp, "currentpoint ");
		if (paintit)
			fprintf(fp, "currentpoint newpath moveto ");
		fprintf(fp, "%d %d rmoveto ", ixoff + isize, iyoff);
		fprintf(fp, "0 %d rlineto ", isize);
		fprintf(fp, "%d %d rlineto ", -isize, -isize/2);
		fprintf(fp, "%d %d rlineto ", isize, -isize/2);
		if (paintit)
			fprintf(fp, "closepath eofill ");
		fprintf(fp, "stroke moveto\n");
		fprintf(fp, "%d 0 rmoveto\n", ixoff * 2 + isize);
		break;
	case 7:
		fprintf(fp, "currentpoint ");
		if (paintit)
			fprintf(fp, "currentpoint newpath moveto ");
		fprintf(fp, "%d %d rmoveto ", ixoff, iyoff + isize/2);
		fprintf(fp, "%d %d rlineto ",  isize/2,  isize/2);
		fprintf(fp, "%d %d rlineto ",  isize/2, -isize/2);
		fprintf(fp, "%d %d rlineto ", -isize/2, -isize/2);
		fprintf(fp, "%d %d rlineto ", -isize/2,  isize/2);
		if (paintit)
			fprintf(fp, "closepath eofill ");
		fprintf(fp, "stroke moveto\n");
		fprintf(fp, "%d 0 rmoveto\n", ixoff * 2 + isize);
		break;
	}
}

static void
line_skip(int x, int y)
{
	linewidth += x;
	lineheight = ((int)lineheight < y) ? (unsigned int)y : lineheight;
}

static void
print_bar(struct ctrl *cp)
{
	struct ctrl_bar pbar;

	pbar.ct_width = cp->ctb_width * w_height / 1000;
	pbar.ct_start = cp->ctb_start * w_width / 100;
	pbar.ct_length = cp->ctb_length * w_width / 100;

	fprintf(fp, "%%bar color %d %d %d\n",
		cp->ctb_width, cp->ctb_start, cp->ctb_length);
	fprintf(fp, "XMARGIN ypos moveto\n");
	fprintf(fp, "%d 0 rmoveto\n", pbar.ct_start);
	fprintf(fp, "%d %d rlineto\n", 0, pbar.ct_width * -1);
	fprintf(fp, "%d %d rlineto\n", pbar.ct_length, 0);
	fprintf(fp, "%d %d rlineto\n", 0, pbar.ct_width);
	fprintf(fp, "%d %d rlineto stroke\n", pbar.ct_length * -1, 0);
	fprintf(fp, "/ypos ypos %d sub def\n",
		cp->ctb_width + VERT_GAP(char_size[0]) / 2);
	fprintf(fp, "xpos ypos moveto\n");

	linewidth = 0;
}

static void
print_icon(struct ctrl *cp)
{
	int i;
	int itype, isize;

	for (i = 0; icon_words[i].ctl_strlen != 0; i++) {
		if (!strncasecmp(cp->ctic_value, icon_words[i].ctl_string,
			strlen(cp->ctic_value)))
				break;
	}
	itype = icon_words[i].ctl_type;
	isize = char_size[0] * cp->ctic_size / 100;
	icon_remember(itype, isize, linewidth, cp->ctic_color);
	linewidth += char_size[0];
}

static void
print_line(char *data, int pfx)
{
	if (data && *data)
		print_string(data, pfx);
}

static void
print_string(char *data, int pfx)
{
	u_char *p, *q;
	int kanji = 0;
	u_int code2;

	p = (u_char *)data;

	while (*p && *p != '\n') {
		if (p[0] == 0x1b && p[1] == '$'
		 && (p[2] == 'B' || p[2] == '@')) {
			kanji = 1;
			p += 3;
			continue;
		}
		if (p[0] == 0x1b && p[1] == '('
		 && (p[2] == 'B' || p[2] == 'J')) {
			kanji = 0;
			p += 3;
			continue;
		}

		if (kanji) {
			for (q = p + 2; 0x21 <= q[0] && q[0] <= 0x7e; q += 2) {
				code2 = q[0] * 256 + q[1];
				if (!iskinsokuchar(code2))
					break;
			}
		} else {
			q = p;
			while (*q && isprint(*q) && !isspace(*q))
				q++;
			if (q == p)
				q++;
			else {
				/* append spaces to the end of the word. */
				while (*q && isspace(*q))
					q++;
			}
		}

		print_fragment(p, q - p, kanji, pfx);

		p = q;
	}
}

static void
print_fragment(u_char *data, u_int len, int kanjimode, int pfx)
{
	u_char *p;
	char *q;
	char buf[4096];
	u_int code;
	int textstartpos = 0;
#define OPENBRACE(kanji)	((kanji) ? '<' : '(')
#define CLOSEBRACE(kanji)	((kanji) ? '>' : ')')
#define	DANGERLETTER(x)	\
	((x) == '(' || (x) == ')' || (x) == '\\')

	p = data;
	if (kanjimode) {
		q = &buf[1];
		while (len) {
			code = p[0] * 256 + p[1];
			sprintf(q, "%04x ", code);
			p += 2;
			len -= 2;
			q += 5;
		}
		buf[0] = OPENBRACE(kanjimode);
		*q++ = CLOSEBRACE(kanjimode);
		*q = '\0';
		text_remember(buf, KANJI, char_size[0], textstartpos, pfx);
	} else {
		/* must take care of escaping those "dangerous" letters */
		q = &buf[0];
		*q++ = OPENBRACE(kanjimode);
		while (len) {
			if (DANGERLETTER(p[0]))
				*q++ = '\\';
			*q++ = *p++;
			len--;
		}
		*q++ = CLOSEBRACE(kanjimode);
		*q++ = '\0';
		text_remember(buf, ASCII, char_size[0], textstartpos, pfx);
	}
	/* by A.Ito 14 Jul. 2000 */
	/* placeholder */
	imagepool[nimagepool].image = NULL;
	imagepool[nimagepool].target_text = ntextpool-1;
	nimagepool++;
}

static char *
checkeps(const char *fname)
{
	static char epsfile[MAXPATHLEN];
	static char fullname[MAXPATHLEN];
	char *p;

	/* rewrite file suffix, if it is not "ps" nor "eps". */
	strlcpy(epsfile, fname, sizeof(epsfile));
	p = strrchr(epsfile, '.');
	if (p) {
		if (strcmp(p, ".ps") != 0 && strcmp(p, ".eps") != 0 &&
			strcmp(p, ".idraw") != 0) {
			/* try "basename.eps" */
			memcpy(p, ".eps", 5);
		}
	}

	fullname[0] = '\0';
	if (findImage(epsfile, fullname) == 0)
		return fullname;
	else
		return NULL;
}

static void
text_remember(char *text, int ctype, int fontsize, int offset, int pfx)
{
	/*XXX*/
	if (strcmp(text, "()") == 0)
		return;
	if (!curfont[ctype]) {
		fprintf(stderr, "cannot find proper font, skipping\n");
		return;
	}
	textpool[ntextpool].pfx = pfx;
	textpool[ntextpool].xoffset = offset;
	textpool[ntextpool].yoffset = char_off;
	textpool[ntextpool].xsiz = linewidth - offset;
	textpool[ntextpool].size = fontsize;
	textpool[ntextpool].font = curfont[ctype];
	textpool[ntextpool].text = strdup(text);
	textpool[ntextpool].fore = fore;
	textpool[ntextpool].back = back;
	ntextpool++;
}

static void
icon_remember(int icon, int fontsize, int offset, u_long color)
{
	textpool[ntextpool].pfx = 1;
	textpool[ntextpool].xoffset = offset;
	textpool[ntextpool].xsiz = char_size[0];
	textpool[ntextpool].size = fontsize;
	textpool[ntextpool].font = (struct fontmap *)(size_t)icon;	/*XXX*/
	textpool[ntextpool].text = NULL;
	textpool[ntextpool].fore = color;
	textpool[ntextpool].back = back;	/*XXX*/
	ntextpool++;
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
image_remember(struct ctrl *cp, struct imagepool *pool)
{
	char *epsname;
	Image *myimage;
	float xzoom, yzoom;
	int x1, y1v, x2, y2, width, height, swidth, sheight;
	struct render_state state;

	if ((epsname = checkeps(cp->ctm_fname))){
		if (ps_boundingbox(epsname, &x1, &y1v, &x2, &y2) < 0) goto noneps;

		width = x2 - x1;
		height = y2 - y1v;
	} else {
		myimage = loadImage(cp->ctm_fname);
		if (!myimage) {
			fprintf(stderr, "failed to open %s\n", cp->ctm_fname);
			exit(1);
		}
		switch (cp->ctm_rotate) {
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
				fprintf(stderr, "rotation by %d degrees not supported.\n", cp->ctm_rotate);
				cleanup(-1);
		}
		width = myimage->width;
		height = myimage->height;
		freeImage(myimage);
	}
		xzoom = (!cp->ctm_ximagesize ? 100 : cp->ctm_ximagesize);
		yzoom = (!cp->ctm_yimagesize ? 100 : cp->ctm_yimagesize);

		state.width = window_width;
		state.height = window_height;
		image_zoomratio(&state, &xzoom, &yzoom, cp->ctm_zoomflag,
			width, height);
		swidth = (int) width * xzoom / 100;
		sheight = (int) height * yzoom / 100;

		pool->xsiz = swidth;
		pool->ysiz = sheight;
		pool->xoffset = linewidth;
		pool->image = cp;
		return;

noneps:
	myimage = loadImage(cp->ctm_fname);
	if (!myimage) {
		fprintf(stderr, "failed to open %s\n", cp->ctm_fname);
		exit(1);
	}
	switch (cp->ctm_rotate) {
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
			fprintf(stderr, "rotation by %d degrees not supported.\n", cp->ctm_rotate);
			cleanup(-1);
	}
	pool->xsiz = myimage->width;
	pool->ysiz = myimage->height;
	freeImage(myimage);
	pool->xoffset = linewidth;
	pool->image = cp;
}

static void
image_load_print(const char *filename, int numcolor,
    int ximagesize, int yimagesize, int zoomflag, int rotate)
{
	Image *myimage, *image;
	Pixmap mypixmap;
	XImageInfo *ximageinfo;
	XImage	*print_image;
	int width, height;
	float xzoomrate, yzoomrate;
	static Cursor curs;
	u_int	print_width, print_height;
	struct render_state state;

	if (zoomflag == 2)
		ximagesize = yimagesize = 0;

	/* hook for eps */
    {
	char *p;
	FILE *epsfp;

	p = checkeps(filename);
	if (p) {
		epsfp = fopen(p, "r");
		print_eps(epsfp, p, ximagesize, yimagesize, zoomflag);
		fclose(epsfp);
		return;
	}
    }

	if (!curs)
		curs = XCreateFontCursor(display, XC_watch);
	XDefineCursor(display, window, curs); XFlush(display);

	screen = DefaultScreen(display);

	if ((myimage = loadImage(filename)) == NULL)
		exit(-1);	/* fail to load image data */
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

	if (numcolor)
		myimage = reduce(myimage, numcolor, verbose);

	xzoomrate = (float) ximagesize;
	yzoomrate = (float) yimagesize;
	state.width = window_width;
	state.height = window_height;
	image_zoomratio(&state, &xzoomrate, &yzoomrate, zoomflag,
		width, height);
	image = myimage;
	myimage = zoom(image, xzoomrate, yzoomrate, verbose);
	freeImage(image);
	width = myimage->width;
	height = myimage->height;

	if (! (ximageinfo= imageToXImage(display, screen, visual, depth,
			myimage, 0, 0, 0, verbose))) {
		fprintf(stderr, "Cannot convert Image to XImage\n");
		exit(1);
	}

	mypixmap = ximageToPixmap(display, RootWindow(display, 0), ximageinfo);
	if (!mypixmap) {
		fprintf(stderr, "Cannot create image in server\n");
		exit(1);
	}

	switch (align) {
	case AL_LEFT:
	case AL_LEFTFILL0:
	case AL_LEFTFILL1:
		break;
	case AL_CENTER:
		break;
	case AL_RIGHT:
		break;
	}

	print_width = myimage->width;
	print_height = myimage->height;

	print_image =  XGetImage(display, mypixmap, 0, 0,
					print_width, print_height,
					AllPlanes, ZPixmap);

	fprintf(fp, "gsave\n");

	/* fixed by A.Ito */
	fprintf(fp, "xpos ypos %d sub translate\n",
		print_height);
	fprintf(fp, "%d %d scale\n", print_width, print_height);
	fprintf(fp, "%d %d 8\n", print_width, print_height);
	fprintf(fp, "[%d 0 0 %d 0 %d]\n", print_width,
			-1 * print_height, print_height);
	fprintf(fp, "{currentfile\n");
	fprintf(fp, "%d string readhexstring pop}\n", print_width * 3);
	fprintf(fp, "false 3\n");
	fprintf(fp, "colorimage\n");

	/* XXX is there any generic way of doing this? */
	switch (print_image->bits_per_pixel) {
	case 8:
		print_8_image(print_image);
		break;
	case 16:
		print_full_image(print_image, visual);
		break;
	case 32:
		print_32_image(print_image, visual);
		break;
	default:
		fprintf(stderr, "Sorry unsupported visual %d\n",
			print_image->bits_per_pixel);
		exit(-1);
	}

	fprintf(fp, "grestore\n");

	XFreePixmap(display, mypixmap);
	freeXImage(ximageinfo);
	freeImage(myimage);
	XUndefineCursor(display, window); XFlush(display);
}

static void
print_usage(char *name)
{
	fprintf(stderr, "Usage: %s [-cimPrv] [-e encoding] [-f psfile] "
	    "[-g gap] [-p paper] [-x xmargin] [-y ymargin] mgpfile\n", name);
	exit(0);
}

static void
setpencolor(u_long c)
{
	/*
	 * update the color of the pen.
	 * XXX background is ignored at this moment
	 */
	XColor color;

	color.pixel = c;
	XQueryColor(display, colormap, &color);
	fprintf(fp, "%f %f %f setrgbcolor %% #%08x depth %d\n",
		color.red / 65535.0,
		color.green / 65535.0,
		color.blue / 65535.0,
		(int) color.pixel, depth);
}

static void
print_full_image(XImage *print_image, Visual *visual2)
{
	u_int	x, y;
	u_int	r, g, b;
	u_int	count = 0;
	u_int	width, height, byte_width;
	u_short	*data;

	data = (u_short *)print_image->data;
	width = print_image->width;
	height = print_image->height;
	byte_width = print_image->bytes_per_line / sizeof(u_short);

	for (y = 0; y < height; y ++) {
		for (x = 0; x < width; x ++) {
			r = ((data[x + y * byte_width] & visual2->red_mask) >> 11) & 0xff;
			g = ((data[x + y * byte_width] & visual2->green_mask) >> 5) & 0xff;
			b = (data[x + y * byte_width] & visual2->blue_mask) & 0xff;

			if (reverse)
				fprintf(fp, "%02x%02x%02x",
					~(r << 3) & 0xff, ~(g << 2) & 0xff, ~(b << 3) & 0xff);
			else
				fprintf(fp, "%02x%02x%02x", r << 3, g << 2, b << 3);

			count ++;
			if (count == 10) {
				count = 0;
				fprintf(fp, "\n");
			}
		}
	}
}

static void
print_32_image(XImage *print_image, Visual *visual2)
{
	u_int	x, y;
	u_int	r, g, b;
	u_int	count = 0;
	u_int	width, height;

	width = print_image->width;
	height = print_image->height;

	for (y = 0; y < height; y ++) {
		for (x = 0; x < width; x ++) {
			unsigned long pix;
			pix = XGetPixel(print_image, x, y);
			r = ((pix & visual2->red_mask)   >> 16) & 0xff;
			g = ((pix & visual2->green_mask) >>  8) & 0xff;
			b =   pix & visual2->blue_mask          & 0xff;

			if (reverse)
				fprintf(fp, "%02x%02x%02x",
					~r & 0xff, ~g & 0xff, ~b & 0xff);
			else
				fprintf(fp, "%02x%02x%02x", r, g, b);

			count ++;
			if (count == 10) {
				count = 0;
				fprintf(fp, "\n");
			}
		}
	}
}

static void
print_8_image(XImage *print_image)
{
	u_int	x, y;
	u_int	count = 0;
	u_char	*data;
	u_int	width, height, byte_width;

	data = (u_char *)print_image->data;
	width = print_image->width;
	height = print_image->height;
	byte_width = print_image->bytes_per_line / sizeof(u_char);

	for (y = 0; y < height; y ++) {
		for (x = 0; x < width; x ++) {
			if (reverse)
				fprintf(fp, "%02x%02x%02x",
					~data[x + y * byte_width] & 0xff,
					~data[x + y * byte_width] & 0xff,
					~data[x + y * byte_width] & 0xff);
			else
				fprintf(fp, "%02x%02x%02x",
					data[x + y * byte_width],
					data[x + y * byte_width],
					data[x + y * byte_width]);

			count ++;
			if (count == 10) {
				count = 0;
				fprintf(fp, "\n");
			}
		}
	}
}

static void
print_eps(FILE *epsfp, char *filename,
    int ximagesize, int yimagesize, int zoomflag)
{
	char line1[BUFSIZ];
	char line2[BUFSIZ];
	char line[BUFSIZ];
	int x1, y1v, x2, y2, height, width;
	float xzoomrate, yzoomrate;
	double xscale, yscale;
	int noboundingbox;
	struct render_state state;

	if (fgets(line1, sizeof(line1), epsfp) == NULL) {
		fprintf(stderr, "no first line in %s.\n", filename);
		exit(1);
	}
	if (strncmp(line1, "%!", 2) != 0) {
		fprintf(stderr, "non eps file %s used as eps.\n", filename);
		exit(1);
	}

	noboundingbox = 1;
	while (1) {
		if (fgets(line2, sizeof(line2), epsfp) == NULL)
			break;
		if (line2[0] != '%')
			break;
		if (strncmp(line2, "%%EndComments", 12) == 0)
			break;
		if (sscanf(line2, "%%%%BoundingBox: %d %d %d %d",
				&x1, &y1v, &x2, &y2) == 4) {
			noboundingbox = 0;
			break;
		}
	}
	if (noboundingbox) {
		fprintf(stderr, "no bounding box in %s.\n", filename);
		exit(1);
	}

	/* width/height of original image */
	width = x2 - x1;
	height = y2 - y1v;

	ximagesize = (!ximagesize ? 100 : ximagesize);
	yimagesize = (!yimagesize ? 100 : yimagesize);

	xzoomrate = (float) ximagesize;
	yzoomrate = (float) yimagesize;
	state.width = window_width;
	state.height = window_height;
	image_zoomratio(&state, &xzoomrate, &yzoomrate, zoomflag,
		width, height);
	xscale = xzoomrate / 100.0;
	yscale = yzoomrate / 100.0;

	switch (align) {
	case AL_LEFT:
	case AL_LEFTFILL0:
	case AL_LEFTFILL1:
		break;
	case AL_CENTER:
		break;
	case AL_RIGHT:
		break;
	}

	fprintf(fp, "BeginEPSF\n");
/*
	fprintf(fp, "%d XMARGIN add ypos translate\n", xpos);
*/
	/* by A.Ito */
	fprintf(fp, "xpos ypos translate\n");
	fprintf(fp, "%f %f scale\n", xscale, yscale);
	fprintf(fp, "%d %d translate\n", -1 * x1, -1 * y2);
	fprintf(fp, "%%%%BeginDocument: %s\n", filename);
	fputs(line1, fp);
	fputs(line2, fp);
	while (fgets(line, sizeof(line), epsfp)) {
		if (strncmp(line, "%%PageBoundingBox:", 18) == 0
		||  strncmp(line, "%%Trailer", 9) == 0)
			line[1] = '#';
		fputs(line, fp);
	}
	fprintf(fp, "%%%%EndDocument\n");
	fprintf(fp, "EndEPSF\n");
}

/*------------------------------------------------------------*/

int
main(int argc, char *argv[])
{
	int opt;
	char *progname;
	static char pathbuf[MAXPATHLEN];

#if HAVE_SETLOCALE_CTYPE
	setlocale(LC_CTYPE, "");
#endif

	progname = argv[0];

	/* set default paper size */
	paper = findpaper(DEFAULT_PAPER_SIZE);

	while ((opt = getopt(argc, argv, "ce:f:g:imPp:rvx:y:")) != -1) {
		switch (opt) {
		case 'c':
			colorps++;
			break;

		case 'e':
			for (encoding = &encodings[0];
			     encoding->name;
			     encoding++) {
				if (strcmp(encoding->name, optarg) == 0)
					break;
			}
			if (!encoding->name) {
				fprintf(stderr, "unknown encoding %s\n", optarg);
				exit(1);
			}
			break;

		case 'f':
			strlcpy(outputfile, optarg, sizeof(outputfile));
			break;

		case 'g':
			gaplevel = atoi(optarg);
			break;

		case 'i':
			painticon++;
			break;

		case 'm':	/* Added by Arnd Schmitter 23.07.2004 */
			PauseMode=1;
			break;

		case 'P':
			parse_debug++;
			break;

		case 'p':
			paper = findpaper(optarg);
			if (!paper) {
				fprintf(stderr,"Paper size '%s' not recognised. Using %s instead.\n",
				    optarg, DEFAULT_PAPER_SIZE);
				paper = findpaper(DEFAULT_PAPER_SIZE);
			}
			break;

		case 'r':
			reverse = 1;
			break;

		case 'v':
			mgp_flag |= FL_VERBOSE;
			break;

		case 'x':
			paper_xmargin = atoi(optarg);
			break;

		case 'y':
			paper_ymargin = atoi(optarg);
			break;

		default:
			print_usage(progname);
			/*NOTREACHED*/
		}
	}

	w_width  = paper->width - paper_xmargin * 2;
	w_height = paper->height - paper_ymargin * 2;

	argc -= optind;
	argv += optind;

	if (argc != 1) {
		print_usage(progname);
		/*NOTREACHED*/
	}

	mgp_fname = argv[0];

	/* setting up the search path. */
    {
	char *p;
	loadPathsAndExts();
	strlcpy(pathbuf, mgp_fname, sizeof(pathbuf));
	if ((p = strrchr(pathbuf, '/'))) {
		*p = '\0';
		Paths[NumPaths++]= expandPath(pathbuf);
	}
    }

	init_win1(NULL);
	load_file(mgp_fname);
	init_win2();
	fore = 0UL;
	back = (1UL << depth) - 1;
	print_out();

	exit(0);
}

static struct fontmap *
findfont(int ctrl, int lang, char *font)
{
	struct fontmap *p, *q;
	char *star;

	if (!font[0])
		return NULL;

	for (p = fontmap; 0 <= p->ctrl; p++) {
		if (p->ctrl != ctrl || p->lang != lang)
			continue;
		star = strchr(p->font, '*');
		if (!star && strcmp(p->font, font) == 0)
			goto found;
		if (star && strncmp(p->font, font, star - p->font) == 0)
		{
			if(strcmp(font,"k14")==0)
				goto found;
			if(ctrl ==CTL_PSFONT)
				fprintf(stderr,"PSFONT: %s  not found\n",font);
			goto found;
		}
	}
	return NULL;

found:
	if (p->fontid)
		return p;

	p->fontid = ++maxfontid;

	if (mgp_flag & FL_VERBOSE) {
		char fonttype;

		switch (ctrl) {
		case CTL_XFONT2: fonttype = 'x'; break;

		case CTL_PSFONT: fonttype = 'p'; break;
		default:	fonttype = '?'; break;
		}
		fprintf(fp, "%% %cfont \"%s\" seen, mapped to ps font \"%s\"\n",
			fonttype, font, p->psfont);
		if (strcmp(font, p->font) != 0) {
			fprintf(fp, "%%\t(wildcard match against \"%s\")\n",
				p->font);
		}
	}
	/* use the same font index for same PS font names */
	for (q = fontmap; 0 <= q->ctrl; q++) {
		if (strcmp(p->psfont, q->psfont) == 0)
			q->fontid = maxfontid;
	}

	return p;
}

static char *
fontstring(struct fontmap *font)
{
	static char fontname[10];

	sprintf(fontname, "F%03d", font->fontid);
	return fontname;
}

static void
loadfont(struct fontmap *font, const char *name)
{
	if (!font) {
		fprintf(stderr, "unable to find proper font for %s\n", name);
		exit(1);
	}

	if (font->loaded)
		return;

	/* define font calling sequence */
	if (mgp_flag & FL_VERBOSE) {
		fprintf(fp, "%% loading font \"%s\" for %s\n",
			font->psfont, font->font);
	}
	if (font->lang == ASCII && encoding) {
		/*
		 * Symbol font does not need latin1-encoding
		 */
		if (strcmp(font->psfont, "Symbol") == 0) {
			fprintf(fp,
				"/%s%s\n"
				"	/%s findfont\n"
				"		dup length dict begin\n"
				"		{1 index /FID ne {def} {pop "
				"pop} ifelse} forall\n"
				"	currentdict end\n"
				"definefont pop\n",
				fontstring(font), "t", font->psfont);
		} else {
			fprintf(fp,
"/%s%s\n"
"	/%s findfont\n"
"		dup length dict begin\n"
"		{1 index /FID ne {def} {pop pop} ifelse} forall\n"
"		/Encoding %s def\n"
"	currentdict end\n"
"definefont pop\n",
			fontstring(font), "t", font->psfont, encoding->defstr);
		}

		fprintf(fp, "/%s {/%s%s findfont exch scalefont setfont} def\n",
			fontstring(font), fontstring(font), "t");
	} else {
		fprintf(fp, "/%s {/%s findfont exch scalefont setfont} def\n",
			fontstring(font), font->psfont);
	}
	font->loaded = 1;
}

void
cleanup(int sig)
{
	/* dummy */
	exit(-sig);
}
