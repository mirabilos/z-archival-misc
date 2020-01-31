/*-
 * Copyright (c) 2010, 2011, 2012, 2014, 2019
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
 * but this has been reimplemented, improved, corrected, modularised.
 * The idea of pushing entropy back to the kernel on stir and after a
 * fork or before an exit is MirBSD specific.
 */

#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "irc4random.h"

struct arcfour_status i4state;

uint32_t
irc4random(void)
{
	uint32_t v;

	/* randomly skip a byte or two */
	/*XXX this should be constant-time */
	if (arcfour_byte(&i4state) & 1)
		(void)arcfour_byte(&i4state);

	v = ((uint32_t)arcfour_byte(&i4state) << 24) |
	    ((uint32_t)arcfour_byte(&i4state) << 16) |
	    ((uint32_t)arcfour_byte(&i4state) << 8) |
	    ((uint32_t)arcfour_byte(&i4state));

	return (v);
}

void
irc4random_init(const void *buf, size_t len, int firsttime)
{
	unsigned int carry = 0;

	if (firsttime)
		arcfour_init(&i4state);
	else
		carry += arcfour_byte(&i4state);

	arcfour_ksa(&i4state, buf, len);

	/* discard early keystream */
	carry += 256 * 12 + (arcfour_byte(&i4state) & 0x1F);
	while (carry--)
		(void)arcfour_byte(&i4state);
}
