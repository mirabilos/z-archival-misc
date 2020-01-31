#ifndef MIRMINCE_OSDEP_LINUX_I386_ASM_H
#define MIRMINCE_OSDEP_LINUX_I386_ASM_H

/* reset -mrtd but keep regparm */
#define mirmince_cc_syscall	mirmince_cc_cdecl mirmince_cc_regparm(3)

#define SYSCALLB(cname,kname)			\
		.globl	_mince_syscall;		\
	ENTRY(cname)				\
		push	ebx;			\
		mov	bl,SYS_ ## kname;	\
		jmp	_mince_syscall;		\
	END(cname)

#define SYSCALLD(cname,kname)			\
		.globl	_mince_syscallEx;	\
	ENTRY(cname)				\
		push	ebx;			\
		mov	ebx,SYS_ ## kname;	\
		jmp	_mince_syscallEx;	\
	END(cname)

#define MIRMINCE_UT_MODE_T	mirmince_u16

#endif
