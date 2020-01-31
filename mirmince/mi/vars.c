/*
 * global variables that every program needs
 */

#include <mirmince/asm.h>

/* required by the CSU */

char **environ mirmince_bss = NULL;

/* required by the CRT */

int errno mirmince_bss = 0;
