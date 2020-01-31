/*-
 * Copyright (c) 2010, 2014, 2016, 2019
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
 * Implement a useful arc4random(3) related API.
 */

#include <stdint.h>
#include <stdlib.h>
#include <unistd.h>

#include "irc4random.h"

void
irc4random_buf(void *buf_, size_t len)
{
	uint8_t *buf = (uint8_t *)buf_;
	size_t n;

	while (len) {
		/* randomly skip 1-4 bytes */
		/*XXX this should be constant-time */
		/* but this is userspace, so don't bother */
		n = arcfour_byte(&i4state) & 3;
		while (n--)
			(void)arcfour_byte(&i4state);

		/* fill the buffer in small increments */
		n = len < 128 ? len : 128;
		len -= n;
		while (n--)
			*buf++ = arcfour_byte(&i4state);
	}
}
