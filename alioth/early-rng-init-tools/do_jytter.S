/*-
 * Copyright © 2019
 *	mirabilos <m@mirbsd.org>
 * Copyright © 2012, 2017-09-20 Russell Leidich
 *
 * Derived from the Jytter Library, which is published under the LGPL
 * (GNU Lesser General Public License) version 3. See the licence for
 * further information; at /usr/share/common-licenses/LGPL-3 on Debi‐
 * an systems, otherwise from http://www.gnu.org/licenses/ online.
 */

/* unique event durations that we must observe before issuing output */
#define UNIQUE_EVENT_DURATION_COUNT 19

/*
 * uint32_t do_jytter(jytter_scratchspace *arg);
 * arg is in RDI on amd64, ECX on i386 (using fastcall convention)
 * by convention, it’s an opaque struct of five quadwords,
 * but we use it as fit
 */

	.intel_syntax noprefix
	.text
	.globl	do_jytter
	.globl	_do_jytter
	.type	do_jytter,@function
	.type	_do_jytter,@function
do_jytter:
_do_jytter:
	/* save registers */
#ifdef __i386__
	mov	[ecx],edi
	mov	4[ecx],edx
	mov	8[ecx],ebx
	mov	12[ecx],ebp
	mov	16[ecx],esi
#else
	mov	[rdi],rcx
	mov	rcx,rdi
	mov	8[rcx],rdx
	mov	16[rcx],rbx
	mov	24[rcx],rbp
	mov	32[rcx],rsi
#endif
	/* clear the bitmap in ESI, as discussed below */
	xor	esi,esi
	/* set BL to number of events left to observe */
	mov	bl,UNIQUE_EVENT_DURATION_COUNT
	/* required jump and alignment to avoid bad jitter */
	jmp	Lread_tsc

	.balign 64
Lread_tsc:
	/* read TSC into EDX:EAX */
	rdtsc
	/* add lower half (rich(er) in entropy) to accumulating rv in EDI */
	add	edi,eax
	/* set EBP to current timestamp, EAX to delta */
	neg	ebp
	xadd	eax,ebp
	/* hash bits 0‥4 XOR 5‥9 of TSC delta into 0‥4 of AL */
	mov	edx,eax
	shr	edx,5
	xor	eax,edx
	/* set bit in bitmap according to that */
	bts	esi,eax
	/* in the meantime, spread output (EDI) bits */
#ifdef __i386__
	lea	edi,[edi+8*edi+0x7F]
#else
	lea	edi,[rdi+8*rdi+0x7F]
#endif
	/* if bts had a bit to set (initial value was 0), decrement BL */
	adc	bl,-1
	/* now loop until BL is 0 (UNIQUE_EVENT_DURATION_COUNT found) */
	jnz	Lread_tsc

	/* store output in return register */
	xchg	eax,edi
	/* restore registers */
#ifdef __i386__
	mov	esi,16[ecx]
	mov	ebp,12[ecx]
	mov	ebx,8[ecx]
	mov	edx,4[ecx]
	mov	edi,[ecx]
#else
	mov	rsi,32[rcx]
	mov	rbp,24[rcx]
	mov	rbx,16[rcx]
	mov	rdx,8[rcx]
	mov	rdi,rcx
	mov	rcx,[rdi]
#endif
	ret
	.size	do_jytter,(. - do_jytter)
	.size	_do_jytter,(. - _do_jytter)
