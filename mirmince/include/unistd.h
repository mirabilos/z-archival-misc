#ifndef UNISTD_H
#define UNISTD_H

#ifndef MIRMINCE_ASM_H
#include <mirmince/asm.h>
#endif

#define MIRMINCE_NT_GID_T
#define MIRMINCE_NT_INTPTR_T
#define MIRMINCE_NT_OFF_T
#define MIRMINCE_NT_PID_T
#define MIRMINCE_NT_SIZE_T
#define MIRMINCE_NT_SSIZE_T
#define MIRMINCE_NT_UID_T
#include <mirmince/types.h>

#define STDIN_FILENO		0
#define STDOUT_FILENO		1
#define STDERR_FILENO		2

__BEGIN_DECLS
void _exit(int status)
	mirmince_dead
	mirmince_cc_userspc;
int close(int fd)
	mirmincE_cc_close;
ssize_t read(int fd, void *buf, size_t nbytes)
	mirmincE_cc_read;
ssize_t write(int fd, const void *buf, size_t nbytes)
	mirmincE_cc_write;
__END_DECLS

#endif
