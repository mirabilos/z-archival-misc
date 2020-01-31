/*-
 * Copyright © 2013
 *	Thorsten Glaser <tg@mirbsd.org>
 *
 * Provided that these terms and disclaimer and all copyright notices
 * are retained or reproduced in an accompanying document, permission
 * is granted to deal in this work without restriction, including un‐
 * limited rights to use, publicly perform, distribute, sell, modify,
 * merge, give away, or sublicence.
 *
 * This work is provided “AS IS” and WITHOUT WARRANTY of any kind, to
 * the utmost extent permitted by applicable law, neither express nor
 * implied; without malicious intent or gross negligence. In no event
 * may a licensor, author or contributor be held liable for indirect,
 * direct, other damage, loss, or other issues arising in any way out
 * of dealing in the work, even if advised of the possibility of such
 * damage or existence of a defect, except proven that it results out
 * of said person’s immediate fault when using the work as intended.
 */

#include <fcntl.h>
#include <stdarg.h>

extern int _open__(const char *path, int oflag, mirmince_ureg mode)
	mirmincE_cc__open__;

#define NEEDS_MODE_CREAT	O_CREAT

#ifdef O_TMPFILE
#define NEEDS_MODE_TMPFILE	__O_TMPFILE
#else
#define NEEDS_MODE_TMPFILE	0
#endif

mirmincE_cc_open int
open(const char *path, int oflag, ...)
{
	mode_t mode;

	if (oflag & (NEEDS_MODE_CREAT | NEEDS_MODE_TMPFILE)) {
		va_list ap;

		va_start(ap, oflag);
		mode = va_arg(ap, int);
		va_end(ap);
	}

	return _open__(path, oflag | O_LARGEFILE, mode);
}
