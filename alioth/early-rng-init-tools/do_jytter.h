/* uncopyrightable interface definition */

#ifndef DO_JYTTER_H
#define DO_JYTTER_H

typedef struct {
	uint64_t opaque[5];
} jytter_scratchspace;

#if defined(__i386__)
extern uint32_t do_jytter(jytter_scratchspace *ecx)
    __attribute__((__fastcall__));
#elif defined(__x86_64__)
extern uint32_t do_jytter(jytter_scratchspace *rdi);
#else
# error jytter is only available for x86 (i386, amd64 and x32)
#endif

#endif
