#ifndef STRING_H
#define STRING_H

#ifndef MIRMINCE_ASM_H
#include <mirmince/asm.h>
#endif

#define MIRMINCE_NT_SIZE_T
#include <mirmince/types.h>

__BEGIN_DECLS
size_t strlen(const char *s)
	mirmince_cc_userspc;
__END_DECLS

#endif
