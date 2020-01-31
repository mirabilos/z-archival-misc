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

#include <mirmince/asm.h>

_TEXT
_ALIGN_TEXT
ASENTRY(_mince_csu_init)
	cld				/* required by the ABI */
	xor	ebp,ebp			/* clear the frame pointer */
	pop	eax			/* argc */
	mov	edx,esp			/* argv */
	lea	ecx,[edx+4*eax+4]	/* envp = argv[argc + 1] */
	push	ecx			/* envp */
	push	edx			/* argv */
	push	eax			/* argc */
	call	_C_LABEL(_mince_csu_run)
ENDSYM(_mince_csu_init)
