#ifndef MIRMINCE_MD_I386_ASM_H
#define MIRMINCE_MD_I386_ASM_H

#ifdef __ASSEMBLER__
	.intel_syntax noprefix
#endif

#ifndef SMALL
#define _ALIGN_TEXT		.p2align 2,0x90
#endif

#ifdef __GNUC__
#define mirmince_cc_cdecl	__attribute__((__regparm__ (0), __cdecl__))
#define mirmince_cc_regparm(x)	__attribute__((__regparm__ (x)))
#else
#define mirmince_cc_cdecl	/* default */
#define mirmince_cc_regparm(x)	/* not used */
#if defined(__mirmincE_use_regparm__) || defined(__mirmincE_use_rtd__)
# error mirmince configured to use regparm/rtd needs GCC
#endif
#endif

#ifdef __mirmincE_use_regparm__
#define mirmince_cc_userspc	mirmince_cc_regparm(__mirmincE_use_regparm__)
#endif

#endif
