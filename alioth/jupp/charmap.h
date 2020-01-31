/*
 *	Character sets
 *	Copyright
 *		(C) 2004 Joseph H. Allen
 *
 *	This file is part of JOE (Joe's Own Editor)
 */

#ifndef _Icharmap
#define _Icharmap 1

#ifdef EXTERN
__RCSID("$MirOS: contrib/code/jupp/charmap.h,v 1.12 2017/12/20 23:33:56 tg Exp $");
#endif

/* For sorted from_map entries */

struct pair {
	int first;			/* Unicode */
	int last;			/* Byte */
};

/* A character set */

struct charmap {
	struct charmap *next;		/* Linked list of loaded character maps */
	const unsigned char *name;	/* Name of this one */

	int type;			/* 0=byte, 1=UTF-8 */

	/* Character predicate functions */

	int (*is_punct)(struct charmap *map,int c);
	int (*is_print)(struct charmap *map,int c);
	int (*is_space)(struct charmap *map,int c);
	int (*is_alphx)(struct charmap *map,int c);
	int (*is_alnux)(struct charmap *map,int c);

	/* Character conversion functions */

	int (*to_lower)(struct charmap *map,int c);
	int (*to_upper)(struct charmap *map,int c);
	int (*to_uni)(struct charmap *map,int c);
	int (*from_uni)(struct charmap *map,int c);

	/* Information for byte-oriented character sets */

	const int *to_map;		/* Convert byte to unicode */

	unsigned char lower_map[256];	/* Convert to lower case */
	unsigned char upper_map[256];

	struct pair from_map[256 + 2];	/* Convert from unicode to byte */

	int from_size;			/* No. pairs in from_map */

	unsigned char print_map[32];	/* Bit map of printable characters */
	unsigned char alphx_map[32];	/* Bit map of alphabetic characters and _ */
	unsigned char alnux_map[32];	/* Bit map of alphanumeric characters and _ */
};

/* Predicates */

#define joe_ispunct(map,c) ((map)->is_punct((map),(c)))
#define joe_isprint(map,c) ((map)->is_print((map),(c)))
#define joe_isspace(map,c) ((map)->is_space((map),(c)))
#define joe_isalphx(map,c) ((map)->is_alphx((map),(c)))
#define joe_isalnux(map,c) ((map)->is_alnux((map),(c)))
int joe_isblank(struct charmap *map,int c);
int joe_isspace_eof(struct charmap *map,int c);

/* Conversion functions */

#define joe_tolower(map,c) ((map)->to_lower((map),(c)))
#define joe_toupper(map,c) ((map)->to_upper((map),(c)))
#define joe_to_uni(map,c) ((map)->to_uni((map),(c)))
#define joe_from_uni(map,c) ((map)->from_uni((map),(c)))
unsigned char *joe_strtolower(unsigned char *s);

/* Find (load if necessary) a character set */
struct charmap *find_charmap(const unsigned char *name);

/* Get available encodings */
unsigned char **get_encodings(void);

int to_uni(struct charmap *cset, int c);
int from_uni(struct charmap *cset, int c);

#include "utf8.h"

#endif