#ifndef __ELF__
# error mirmince currently requires an ELF platform
#else

#ifndef MIRMINCE_ASM_H
#define MIRMINCE_ASM_H

#define __STRING(x)		#x
#define __STRINGIND(x)		__STRING(x)

#ifndef mirmince_hdr_PFX
#ifdef __mirmincE_bootstrap
#define mirmince_hdr_PFX
#else
#define mirmince_hdr_PFX	mirmince/
#endif
#define mirmince_hdr_sysinc(x)	< mirmince_hdr_PFX x >
#define mirmince_hdr_md(x)	mirmince_hdr_sysinc(md/__mirmincE_arch__/x)
#define mirmince_hdr_osdep(x)	mirmince_hdr_sysinc(osdep/__mirmincE_os__/x)
#define mirmince_hdr_osmd(x)	mirmince_hdr_sysinc(osdep/__mirmincE_os__/__mirmincE_arch__/x)
#endif

#include mirmince_hdr_md(asm.h)
#include mirmince_hdr_osdep(asm.h)
#include mirmince_hdr_osmd(asm.h)

/* prefix an underscore on some platforms, like a.out */
#define _C_LABEL(x)		x

#ifndef _FTYPE
#define _FTYPE(x)		.type x,@function
#define _OTYPE(x)		.type x,@object
#endif

#ifndef _TEXT
#define _TEXT			.text
#define _DATA			.data
#define _BSS			.bss
#define _RODATA			.section .rodata
#define _COMMENT		.section .comment
#endif

#ifndef _ALIGN_TEXT
#define _ALIGN_TEXT		/* nothing */
#endif

#define ASENTRY(x)		.globl x; _FTYPE(x); x:
#define ENTRY(x)		_TEXT; _ALIGN_TEXT; ASENTRY(_C_LABEL(x))
#define TENTRY(x)		ASENTRY(_C_LABEL(x))
#define DENTRY(x)		.globl _C_LABEL(x); _OTYPE(_C_LABEL(x)); _C_LABEL(x):
#define ENDSYM(x)		.size x, . - x
#define END(x)			ENDSYM(_C_LABEL(x))
#define EXTRN(x)		.globl _C_LABEL(x)

#ifdef __LP64__
#define ASPTR			.quad
#else
#define ASPTR			.long
#endif

#ifndef __ASSEMBLER__

#ifdef __cplusplus
#define __BEGIN_DECLS		extern "C" {
#define __END_DECLS		}
#else
#define __BEGIN_DECLS
#define __END_DECLS
#endif

#ifdef __GNUC__
#define mirmince_bss		__attribute__((__section__ (".bss")))
#define mirmince_dead		__attribute__((__noreturn__))
#define mirmince_printf(x)	__attribute__((__printf__ x))
#define mirmince_unused		__attribute__((__unused__))
#else
#define mirmince_bss
#define mirmince_dead
#define mirmince_printf(x)
#define mirmince_unused
#endif

#ifndef mirmince_cc_userspc
#define mirmince_cc_userspc	mirmince_cc_cdecl
#endif

#ifndef mirmince_cc_syscall
#define mirmince_cc_syscall	mirmince_cc_cdecl
#endif

#include mirmince_hdr_osdep(syscallc.h)

#define NULL			((void *)0)

/* CPU register wide types */
#ifdef __mirmincE_arch_x32
typedef unsigned long long mirmince_ureg;
typedef signed long long mirmince_sreg;
#else
typedef unsigned long mirmince_ureg;
typedef signed long mirmince_sreg;
#endif

/* specific bit wide types */
typedef unsigned char mirmince_u8;
typedef unsigned short mirmince_u16;
typedef unsigned int mirmince_u32;
typedef unsigned long long mirmince_u64;
typedef signed char mirmince_s8;
typedef signed short mirmince_s16;
typedef signed int mirmince_s32;
typedef signed long long mirmince_s64;

#endif /* !__ASSEMBLER__ */

#ifndef MIRMINCE_SAFEGUARD_NOTE
#define MIRMINCE_SAFEGUARD_NOTE

#ifdef __ASSEMBLER__
	.section .note
	.p2align 2
	.long	2f-1f				/* name size */
	.long	4f-3f				/* desc size */
	.long	0				/* type (safeguard note) */
1:	.asciz	"MirMinze"			/* name */
2:	.p2align 2
3:	ASPTR	_C_LABEL(_mince_csu_init)	/* desc */
4:	.p2align 2
	.previous
#else
__asm__(".section .note"
"\n	.p2align 2"
"\n	.long	2f-1f"				/* name size */
"\n	.long	4f-3f"				/* desc size */
"\n	.long	0"				/* type (safeguard note) */
"\n1:	.asciz	\"MirMinze\""			/* name */
"\n2:	.p2align 2"
"\n3:	" __STRINGIND(ASPTR) "\t" \
	    __STRINGIND(_C_LABEL(_mince_csu_init))	/* desc */
"\n4:	.p2align 2"
"\n	.previous");
#endif
#endif /* !MIRMINCE_SAFEGUARD_NOTE */

#endif /* !MIRMINCE_ASM_H */
#endif /* ELF */
