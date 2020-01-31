#if 0
.if "0" == "1"
#endif
/*-
 * Copyright © 2019
 *	mirabilos <m@mirbsd.org>
 *
 * Provided that these terms and disclaimer and all copyright notices
 * are retained or reproduced in an accompanying document, permission
 * is granted to deal in this work without restriction, including un‐
 * limited rights to use, publicly perform, distribute, sell, modify,
 * merge, give away, or sublicence.
 *
 * This work is provided “AS IS” and WITHOUT WARRANTY of any kind, to
 * the utmost extent permitted by applicable law, neither express nor
 * implied; without malicious intent or gross negligence. In no event
 * may a licensor, author or contributor be held liable for indirect,
 * direct, other damage, loss, or other issues arising in any way out
 * of dealing in the work, even if advised of the possibility of such
 * damage or existence of a defect, except proven that it results out
 * of said person’s immediate fault when using the work as intended.
 */

#include <errno.h>
#include <limits.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "do_jytter.h"

/* default limit */
#define POOLWORDS 64
#define POOLBYTES (POOLWORDS * 4)

uint32_t pool[POOLWORDS];

static void usage(int) __attribute__((__noreturn__));
static int parse_int(const char *, const char *, unsigned int *, unsigned int);

#if defined(__OpenBSD__) || defined(__NetBSD__) || defined(__FreeBSD__)
extern const char *__progname;
#else
#define __progname rnd_progname
const char *__progname;
#endif

int
main(int argc, char *argv[])
{
	int c;
	ssize_t z;
	char verbose = 0;
	unsigned int perround = 32;
	unsigned int numrounds = 2;
	struct timeval betweenrounds;
	unsigned int between_s = 0, between_us = 500000;
	const char *ccp;
	char *cp;
	jytter_scratchspace scratchspace;

#ifdef __progname
	__progname = argv[0];
#endif

	while ((c = getopt(argc, argv, "b:d:hr:v")) != -1)
		switch (c) {
		case 'b':
			if (parse_int("#bytes", optarg, &perround, POOLBYTES))
				usage(1);
			break;
		case 'd':
			switch (*(ccp = optarg)) {
			case '.':
				between_s = 0;
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				between_s = strtoul(ccp, &cp, 10);
				ccp = cp;
				if (between_s < 86400)
					break;
				/* FALLTHROUGH */
			default:
 invalid_delay:
				fprintf(stderr, "E: invalid delay: %s\n",
				    optarg);
				usage(1);
			}
			between_us = 0;
			if (!*ccp)
				break;
			if (*ccp++ != '.')
				goto invalid_delay;
			for (c = 0; c < 6; ++c) {
				between_us *= 10;
				switch (*ccp) {
				case '\0':
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					between_us += *ccp - '0';
					++ccp;
					break;
				default:
					goto invalid_delay;
				}
			}
			if (*ccp)
				goto invalid_delay;
			break;
		case 'h':
			usage(0);
			break;
		case 'r':
			if (parse_int("#rounds", optarg, &numrounds, 0))
				usage(1);
			break;
		case 'v':
			verbose = 1;
			break;
		default:
			usage(1);
		}
	argc -= optind;
	argv += optind;
	if (argc)
		usage(1);

	if (verbose)
		fprintf(stderr,
		    "D: %u bytes per round, %u rounds with %u.%06u s in betwixt\n",
		    perround, numrounds, between_s, between_us);

 loop:
	c = 0;
	while (((unsigned int)c * 4U) < perround)
		pool[c++] = do_jytter(&scratchspace);
	if ((z = write(STDOUT_FILENO, pool, perround)) != (ssize_t)perround)
		fprintf(stderr, "E: during write: %zd, %s\n", z,
		    z == -1 ? strerror(errno) : "(short write)");
	else if (verbose)
		fprintf(stderr, "I: gathered %zd random bytes\n", z);
	if (numrounds-- > 1) {
		if (between_s || between_us) {
			betweenrounds.tv_sec = between_s;
			betweenrounds.tv_usec = between_us;
			select(1, NULL, NULL, NULL, &betweenrounds);
		}
		goto loop;
	}

	return (0);
}

static void
usage(int rv)
{
	fprintf(stderr,
	    "E: usage: %s [-hv] [-b #bytes] [-d delay] [-r #rounds]\n"
	    "N: bytes default 32, max %u; delay max 86399.999999\n",
	    __progname, POOLBYTES);
	exit(rv);
}

static int
parse_int(const char *w, const char *ccp, unsigned int *vp, unsigned int max)
{
	unsigned long u;
	char *cp;

	if (!*ccp) {
		fprintf(stderr, "E: %s: %s: %s\n", w, ccp,
		    "empty");
		return (1);
	}
	u = strtoul(ccp, &cp, 0);
	if (*cp) {
		fprintf(stderr, "E: %s: %s: %s\n", w, ccp,
		    "trailing garbage");
		return (1);
	}
	if ((u == ULONG_MAX && errno == ERANGE) || u > INT_MAX) {
		fprintf(stderr, "E: %s: %s: %s\n", w, ccp,
		    "overflow");
		return (1);
	}
	if (max && u > max) {
		fprintf(stderr, "E: %s: %s: too large (max %u)\n", w, ccp, max);
		return (1);
	}
	*vp = u;
	return (0);
}

#if 0
.endif

PROG=		rnd_jytter
NOMAN=		Yes
MK_MAN=		no
SRCS=		rnd_jytter.c do_jytter.S

.include <bsd.prog.mk>
.include "${.CURDIR}/bsdinc.mk"
#endif
