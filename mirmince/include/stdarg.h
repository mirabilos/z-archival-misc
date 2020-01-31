#ifndef STDARG_H
#define STDARG_H

#define MIRMINCE_NT_VA_LIST
#include <mirmince/types.h>

#define va_start		__builtin_va_start
#define va_copy			__builtin_va_copy
#define va_arg			__builtin_va_arg
#define va_end			__builtin_va_end

#endif
