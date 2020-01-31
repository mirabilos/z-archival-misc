#ifndef STDLIB_H
#define STDLIB_H

#ifndef MIRMINCE_ASM_H
#include <mirmince/asm.h>
#endif

__BEGIN_DECLS
void exit(int status)
	mirmince_dead
	mirmincE_cc_exit;
__END_DECLS

#endif
