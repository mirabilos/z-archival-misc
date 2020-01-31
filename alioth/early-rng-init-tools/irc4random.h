/* From MirOS: src/include/stdlib.h,v 1.38 2015/02/11 21:42:22 tg Exp $ */
/* From MirOS: src/kern/include/libckern.h,v 1.43 2019/02/21 23:08:43 tg Exp $ */
/* From MirOS: src/lib/libc/crypt/arc4random.h,v 1.5 2014/03/29 10:35:45 tg Exp $ */

/*-
 * Copyright (c) 2008, 2010, 2011, 2013, 2014, 2015, 2019
 *	mirabilos <m@mirbsd.org>
 *
 * Provided that these terms and disclaimer and all copyright notices
 * are retained or reproduced in an accompanying document, permission
 * is granted to deal in this work without restriction, including un-
 * limited rights to use, publicly perform, distribute, sell, modify,
 * merge, give away, or sublicence.
 *
 * This work is provided "AS IS" and WITHOUT WARRANTY of any kind, to
 * the utmost extent permitted by applicable law, neither express nor
 * implied; without malicious intent or gross negligence. In no event
 * may a licensor, author or contributor be held liable for indirect,
 * direct, other damage, loss, or other issues arising in any way out
 * of dealing in the work, even if advised of the possibility of such
 * damage or existence of a defect, except proven that it results out
 * of said person's immediate fault when using the work as intended.
 *-
 * The idea of an arc4random(3) stems from David Mazieres for OpenBSD
 */

#ifndef IRC4RANDOM_H
#define IRC4RANDOM_H

#if defined(__OpenBSD__)
#define __bounded_attribute__(x) __attribute__(x)
#else
#define __bounded_attribute__(x) /* nothing */
#endif

/**
 * An arcfour_status is hereby defined carrying ca.
 * 212 octets (1696 bit) of entropic state, whereas
 * S contains 210 octets and 3.996 additional bits,
 * i is another 8 bit, and j adds enough to make up
 * for the 4 bit of additional entropy we assume.
 */
struct arcfour_status {
	uint8_t S[256];
	uint8_t i;
	uint8_t j;
};

extern struct arcfour_status i4state;

/* arcfour: base cipher */
void arcfour_init(struct arcfour_status *);
void arcfour_ksa(struct arcfour_status *, const uint8_t *, size_t)
    __bounded_attribute__((__bounded__(__string__, 2, 3)));
uint8_t arcfour_byte(struct arcfour_status *);

/* irc4random: for use in this program */
void irc4random_init(const void *, size_t, int)
    __bounded_attribute__((__bounded__(__string__, 1, 2)));
void irc4random_buf(void *, size_t)
    __bounded_attribute__((__bounded__(__string__, 1, 2)));
uint32_t irc4random(void);
uint32_t irc4random_uniform(uint32_t);

#endif
