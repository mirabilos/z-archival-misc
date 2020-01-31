#ifndef FCNTL_H
#define FCNTL_H

#ifndef MIRMINCE_ASM_H
#include <mirmince/asm.h>
#endif

#define MIRMINCE_NT_MODE_T
#define MIRMINCE_NT_OFF_T
#define MIRMINCE_NT_PID_T
#include <mirmince/types.h>

#include mirmince_hdr_osdep(fcntl.h)

#ifdef _ALL_SOURCE
#define O_NDELAY		O_NONBLOCK
#endif

__BEGIN_DECLS
int open(const char *path, int oflag, ...)
	mirmincE_cc_open;
__END_DECLS

#endif
