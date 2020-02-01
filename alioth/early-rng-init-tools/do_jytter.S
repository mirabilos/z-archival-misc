/*-
 * Copyright © 2019, 2020
 *	mirabilos <m@mirbsd.org>
 * Copyright © 2011-06-25, 2017-09-20, 2019-12-15 Russell Leidich
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/* # unique event durations that must be observed before issuing output */
#ifndef UNIQUE_EVENT_DURATION_COUNT
#define UNIQUE_EVENT_DURATION_COUNT 16
#endif

/*
 * uint32_t do_jytter(jytter_scratchspace *arg);
 * arg is an opaque struct of five quadwords, 64-bit aligned,
 * whose address gets passed in ECX on i386 (using fastcall),
 * in RDI on amd64 and x32; result is in EAX.
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
	/* note: ebp (previous timestamp) is initially undefined */
	/* if this bothers you, raise UNIQUE_EVENT_DURATION_COUNT */
#ifdef __i386__
	mov	[ecx],edi
	mov	edi,ecx
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
	/* add lower half (richer in entropy) to accumulating rv in EDI */
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
	/* in the meantime, spread bits and add 0-guard in output (EDI) */
#ifdef __i386__
	lea	edi,[edi+8*edi+0x7F]
#else
	lea	edi,[rdi+8*rdi+0x7F]
#endif
	/* decrement BL except if bts did nothing (duration already seen) */
	adc	bl,-1
	/* now loop… until BL is 0 (UNIQUE_EVENT_DURATION_COUNT reached) */
	jnz	Lread_tsc

	/* store our output in return register */
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
