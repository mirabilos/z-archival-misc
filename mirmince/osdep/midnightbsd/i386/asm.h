#ifndef MIRMINCE_OSDEP_MIDNIGHTBSD_I386_ASM_H
#define MIRMINCE_OSDEP_MIDNIGHTBSD_I386_ASM_H

#define SYSCALLB(cname,kname)			\
		.globl	_mince_syscall;		\
	ENTRY(cname)				\
		mov	al,SYS_ ## kname;	\
		jmp	_mince_syscall;		\
	END(cname)

#define SYSCALLD(cname,kname)			\
		.globl	_mince_syscallEx;	\
	ENTRY(cname)				\
		mov	eax,SYS_ ## kname;	\
		jmp	_mince_syscallEx;	\
	END(cname)

#endif
