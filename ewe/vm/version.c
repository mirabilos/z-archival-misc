#define EWE_VERSION	"1.49mb4"

/*-
 * Copyright (c) 2008
 *	Thorsten Glaser <tg@mirbsd.de>
 * This is orphaned work; 1.49mb4 is the last release; entities inte-
 * rested in taking over this project are welcome to send a request.
 *
 * Provided that these terms and disclaimer and all copyright notices
 * are retained or reproduced in an accompanying document, permission
 * is granted to deal in this work without restriction, including un-
 * limited rights to use, publicly perform, distribute, sell, modify,
 * merge, give away, or sublicence.
 *
 * This work is provided "AS IS" and WITHOUT WARRANTY of any kind, to
 * the utmost extent permitted by applicable law, neither express nor
 * implied; without malicious intent or gross negligence. In no event
 * may a licensor, author or contributor be held liable for indirect,
 * direct, other damage, loss, or other issues arising in any way out
 * of dealing in the work, even if advised of the possibility of such
 * damage or existence of a defect, except proven that it results out
 * of said person's immediate fault when using the work as intended.
 */

#include <sys/param.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "cldefs.h"

__IDSTRING(__rcsid, "$MirOS: contrib/hosted/ewe/vm/version.c,v 1.5 2008/07/22 20:16:05 tg Exp $");

#ifdef UNIX
#define EWE_ISUNIX	"UNIX"
#else
#define EWE_ISUNIX	"non-UNIX"
#endif

#if defined(__APPLE__)
#define EWE_OS		"Darwin"
#elif defined(__INTERIX)
#define EWE_OS		"Interix/SFU"
#elif defined(__GNU_KFreeBSD__)
#define EWE_OS		"GNU/kFreeBSD"
#elif defined(BSD)
#if defined(__MidnightBSD__) || defined(__MidnightBSD_version)
#define EWE_OS		"MidnightBSD"
#elif defined(__MirBSD__)
#define EWE_OS		"MirOS BSD"
#elif defined(__DragonFly__)
#define EWE_OS		"DragonFly BSD"
#elif defined(__FreeBSD__)
#define EWE_OS		"FreeBSD"
#elif defined(__NetBSD__)
#define EWE_OS		"NetBSD"
#elif defined(__OpenBSD__)
#define EWE_OS		"OpenBSD"
#else
#define EWE_OS		"unknown BSD"
#endif
#elif defined(linux) || defined(__linux) || defined(__linux__) || defined(__gnu_linux__)
#define EWE_OS		"GNU/Linux"
#elif defined(__gnu_hurd__)
#define EWE_OS		"GNU/HURD"
#elif defined(__GLIBC__)
#define EWE_OS		"GNU/*"
#elif defined(_AIX)
#define EWE_OS		"AIX"
#elif defined(__hpux)
#define EWE_OS		"HP-UX"
#elif defined(__sgi)
#define EWE_OS		"IRIX"
#elif defined(__sun__)
#define EWE_OS		"SunOS"
#elif defined(__ultrix)
#define EWE_OS		"Ultrix"
#else
#define EWE_OS		"unknown UNIX"
#endif

#if defined(MAKING_POOL)
#define EWE_TOOLKIT	"MakePool"
#elif defined(GTK_VERSION_1_2)
#define EWE_TOOLKIT	"GTK+1.2"
#elif defined(GTK_VERSION_2_0)
#define EWE_TOOLKIT	"GTK+2"
#elif defined(QT2)
#define EWE_TOOLKIT	"Qt2"
#elif defined(NOGUI)
#define EWE_TOOLKIT	"CLI"
#else
#define EWE_TOOLKIT	"unknown"
#endif

#if defined(SHOW_MISSING_NATIVE_METHODS)
#define EWE_EXTRA	" (shows missing native methods)"
#elif defined(NOEMBEDDEWE)
#define EWE_EXTRA	" (class library ewe.ewe loaded)"
#else
#define EWE_EXTRA	" (embeds its own class library)"
#endif

#ifndef MACHINE
#define MACHINE		""
#endif
#ifndef MACHINE_ARCH
#define MACHINE_ARCH	""
#endif

void usage(bool) __attribute__((noreturn));

void
usage(bool vflag)
{
	if (vflag) {
		fprintf(stderr,
		    "MirEwe version %s for %s (%s) on (%s|%s) with %s UI%s\n",
		    EWE_VERSION, EWE_ISUNIX, EWE_OS, MACHINE, MACHINE_ARCH,
		    EWE_TOOLKIT, EWE_EXTRA);
		exit(0);
	}
	fprintf(stderr,
	    "usage:\tewe [-?hmnOoprsvxz] [-b wintitle] [-c classheapsize] [-cp classpath]\n"
	    "\t    [-d progdir] [-h height] [-l locale] [-t stacksize] [-w width]\n"
	    "\t    [run_class] [ewe_files] [--] [app_args]\n");
	exit(1);
}
