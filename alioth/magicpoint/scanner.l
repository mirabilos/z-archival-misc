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

#include "mgp.h"
#include "y.tab.h"

static int stoi(char *);
static inline int xdtoi(int);

#ifdef FLEX_SCANNER
#define YY_NO_INPUT
#define YY_NO_UNPUT
#undef YY_INPUT
#define YY_INPUT(buf, result, max)				\
 {								\
	char *YYIsrc = in_buffer;				\
	int YYIi;						\
								\
	if (*YYIsrc == 0)					\
		result = YY_NULL;				\
	else {							\
		for (YYIi = 0; *YYIsrc && YYIi < max; ++YYIi)	\
			buf[YYIi] = *YYIsrc++;			\
		in_buffer += YYIi;				\
		result = YYIi;					\
	}							\
 }
#else
#undef getc
#define getc(fp)  (*in_buffer == 0 ? EOF : *in_buffer++)
#endif

static char *in_buffer;
%}

N	(-[0-9]+|[0-9]+|(0x|0X)[0-9A-Fa-f]+)
JISIN	(\033$B|\033$@)
ASCIIIN	(\033\(B|\033\(J)
ASTR	[\040-\176]*
ASTRN	[\040\041\043-\176]*
JSTR	{JISIN}([\041-\176][\041-\176])*{ASCIIIN}
XSTR	[\240-\377]*

%a 6000
%p 4000
%o 8000

%%
(NOOP|noop)		return KW_NOOP;
(DEFAULT|default)	return KW_DEFAULT;
(TAB|tab)		return KW_TAB;
(SIZE|size)		return KW_SIZE;
(FORE|fore)		return KW_FORE;
(BACK|back)		return KW_BACK;
(LEFT|left)		return KW_LEFT;
(LEFTFILL|leftfill)	return KW_LEFTFILL;
(CENTER|center)		return KW_CENTER;
(RIGHT|right)		return KW_RIGHT;
(SHRINK|shrink)		return KW_SHRINK;
(LCUTIN|lcutin)		return KW_LCUTIN;
(RCUTIN|rcutin)		return KW_RCUTIN;
(CONT|cont)		return KW_CONT;
(NODEFAULT|nodefault)	return KW_NODEF;
(XFONT|xfont)		return KW_XFONT;
(IMAGE|image)		return KW_IMAGE;
(NEWIMAGE|newimage)	return KW_NEWIMAGE;
(BIMAGE|bimage)		return KW_BIMAGE;
(PAGE|page)		return KW_PAGE;
(HGAP|hgap)		return KW_HGAP;
(VGAP|vgap)		return KW_VGAP;
(GAP|gap)		return KW_GAP;
(PAUSE|pause)		return KW_PAUSE;
(PREFIX|prefix)		return KW_PREFIX;
(AGAIN|again)		return KW_AGAIN;
(CCOLOR|ccolor)		return KW_CCOLOR;
(BAR|bar)		return KW_BAR;
(INCLUDE|include)	return KW_INCLUDE;
(BGRAD|bgrad)		return KW_BGRAD;
(TEXT|text)		return KW_TEXT;
(LINESTART|linestart)	return KW_LINESTART;
(LINEEND|lineend)	return KW_LINEEND;
(MARK|mark)		return KW_MARK;
(SYSTEM|system)		return KW_SYSTEM;
(XSYSTEM|xsystem)	return KW_XSYSTEM;
(TSYSTEM|tsystem)	return KW_TSYSTEM;
(FILTER|filter)		return KW_FILTER;
(ENDFILTER|endfilter)	return KW_ENDFILTER;
(BQUALITY|bquality)	return KW_QUALITY;
(ICON|icon)		return KW_ICON;
(DEFFONT|deffont)	return KW_DEFFONT;
(FONT|font)		return KW_FONT;
(CHARSET|charset)	return KW_CHARSET;
(PCACHE|pcache)	return KW_PCACHE;
(VALIGN|valign)	return KW_VALIGN;
(AREA|area)	return KW_AREA;
(OPAQUE|opaque)	return KW_OPAQUE;
(SUP|sup)	return KW_SUP;
(SUB|sub)	return KW_SUB;
(SETSUP|setsup)	return KW_SETSUP;
(PSFONT|psfont) return KW_PSFONT;
(TITLE|title)		return KW_TITLE;

[ \n\t]		;
","		return COMMA;
[0-9][0-9]*x[0-9][0-9]* { yylval.s = strdup((char *)yytext); return WINSIZ; }
{N}		{ yylval.i = stoi((char *)yytext); return NUM; }
[0-9][0-9]*\.[0-9][0-9]*	{ yylval.d = atof((char *)yytext); return DOUBLE; }
[-_.A-Za-z][-_.<>A-Za-z0-9]*	{ yylval.s = strdup((char *)yytext); return ID; }
\"({XSTR}|{ASTRN})({JSTR}{ASTRN}|{XSTR})*\" {
			yylval.s = strdup((char *)yytext) + 1;
			if (strlen(yylval.s))
				yylval.s[strlen(yylval.s) - 1] = '\0';
			return STR;
		}
\"({XSTR}|{ASTRN})*\" {
			if (mgp_flag & FL_NOFORK) {
				yyerror("invalid character <%c>", *(char *)yytext);
			} else {
				yylval.s = strdup((char *)yytext) + 1;
				if (strlen(yylval.s))
					yylval.s[strlen(yylval.s) - 1] = '\0';
				return STR;
			}
		}
.		{
		  yyerror("invalid character <%c>", *(char *)yytext);
		}
%%

void
lex_init(char *buf)
{
	n_errors = 0;
	in_buffer = buf;
}

/*
 * Also define a yywrap.  Note that if we're using flex, it will
 * define a macro to map this identifier to pcap_wrap.
 */
int
yywrap(void)
{
	return 1;
}

/* Hex digit to integer. */
static inline int
xdtoi(register int c)
{
	if (isdigit(c))
		return c - '0';
	else if (islower(c))
		return c - 'a' + 10;
	else
		return c - 'A' + 10;
}

/*
 * Convert string to integer.  Just like atoi(), but checks for
 * preceding 0x or 0 and uses hex or octal instead of decimal.
 */
static int
stoi(char *s)
{
	int base = 10;
	int n = 0;
	int sign;

	sign = 1;
	if (*s == '0') {
		if (s[1] == 'x' || s[1] == 'X') {
			s += 2;
			base = 16;
		}
		else {
			base = 8;
			s += 1;
		}
	} else if (*s == '-') {
		sign = -1;
		s++;
	}
	while (*s)
		n = n * base + xdtoi(*s++);

	return n * sign;
}
