#ifndef MIRMINCE_MD_AMD64_ASM_H
#define MIRMINCE_MD_AMD64_ASM_H

#ifdef __ASSEMBLER__
	.intel_syntax noprefix
#endif

#ifndef SMALL
#define _ALIGN_TEXT		.p2align 3,0x90
#endif

#define mirmince_cc_cdecl	/* not used */
#define mirmince_cc_regparm(x)	/* default */

#endif
