/*
 * this file does NOT have multiple-inclusion protection,
 * this is by design, as it needs to be multiply included
 * it instead has multiple definition protection for each
 * thing it defines
 */

/* depends on asm.h */
#ifdef MIRMINCE_NT_MODE_T
#ifndef MIRMINCE_DT_MODE_T
typedef MIRMINCE_UT_MODE_T mode_t;
#define MIRMINCE_DT_MODE_T
#endif
#undef MIRMINCE_NT_MODE_T
#endif

/* uses a compiler builtin */
#ifdef MIRMINCE_NT_SIZE_T
#ifndef MIRMINCE_DT_SIZE_T
#ifdef __SIZE_TYPE__
typedef __SIZE_TYPE__ size_t;
#endif
#define MIRMINCE_DT_SIZE_T
#endif
#undef MIRMINCE_NT_SIZE_T
#endif

/* uses a compiler builtin */
#ifdef MIRMINCE_NT_SSIZE_T
#ifndef MIRMINCE_DT_SSIZE_T
#ifdef __SIZE_TYPE__
#define unsigned /* nothing */
typedef signed __SIZE_TYPE__ ssize_t;
#undef unsigned
#endif
#define MIRMINCE_DT_SSIZE_T
#endif
#undef MIRMINCE_NT_SSIZE_T
#endif

/* uses a compiler builtin */
#ifdef MIRMINCE_NT_VA_LIST
#ifndef MIRMINCE_DT_VA_LIST
typedef __builtin_va_list va_list;
#define MIRMINCE_DT_VA_LIST
#endif
#undef MIRMINCE_NT_VA_LIST
#endif
