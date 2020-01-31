/*-
 * Copyright © 2003, 2004, 2005, 2013
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

#include <mirmince/asm.h>

#ifndef __ELF__
#error huh?
#endif

	.section .note.miros.ident,"a",@progbits
	.p2align 2
	.long	2f-1f			/* name size */
	.long	4f-3f			/* desc size */
	.long	1			/* type (OS version note) */
1:	.asciz	"MirOS BSD"		/* name */
2:	.p2align 2
3:	.long	0			/* desc */
4:	.p2align 2
