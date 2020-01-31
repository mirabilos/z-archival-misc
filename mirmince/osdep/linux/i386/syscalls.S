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

EXTRN(errno)

_TEXT
_ALIGN_TEXT
ASENTRY(_mince_syscall)
	movzx	ebx,bl
ASENTRY(_mince_syscallEx)
	/*
	 * regparm(3) is EAX EDX ECX
	 * we need EAX + EBX ECX EDX ESI EDI EBP
	 * the SYSCALL* macro already pushed EBX
	 * which now contains the syscall number
	 */
	push	ebp
	mov	ebp,esp
	push	esi
	push	edi
	xchg	eax,ebx
	xchg	ecx,edx
	mov	esi,[ebp+16]
	mov	edi,[ebp+20]
	mov	ebp,[ebp+24]
	int	0x80
	cmp	eax,(-4095)
	jae	2f
1:	pop	edi
	pop	esi
	pop	ebp
	pop	ebx
	ret
2:	neg	eax
	mov	[_C_LABEL(errno)],eax
	mov	eax,0xFFFFFFFF
	/* no edx: no 64-bit retvals in Linux/i386 */
	jmp	1b
ENDSYM(_mince_syscall)
