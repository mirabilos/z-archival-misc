#if 0
.if "0" == "1"
#endif
/*-
 * Copyright © 2009, 2018, 2019
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

#include <sys/types.h>
#include <sys/time.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <ctype.h>
#include <errno.h>
#include <fcntl.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#if defined(__OpenBSD__)
#include <sys/param.h>
#include <sys/sysctl.h>
#include <dev/rndioctl.h>
#elif defined(__linux__)
#include <sys/auxv.h>
#include <linux/random.h>
# if defined(__KLIBC__) && !defined(AT_RANDOM)
#  define AT_RANDOM 25
# endif
#else
# error do not know how to accedit into RNG
#endif

#if USE_JYTTER
#include "do_jytter.h"
#endif
#include "irc4random.h"

#if defined(__OpenBSD__) || defined(__NetBSD__) || defined(__FreeBSD__)
extern const char *__progname;
#else
#define __progname rnd_progname
const char *__progname;
#endif

static uint8_t data[256];
static const char urnf[] = "/dev/urandom";
uint8_t earlyboot = 0;
uint8_t debug = 0;

size_t gather(int, size_t);
ssize_t readex(int, uint8_t *, size_t)
    __bounded_attribute__((__bounded__(__string__, 2, 3)));
ssize_t writex(int, const uint8_t *, size_t)
    __bounded_attribute__((__bounded__(__string__, 2, 3)));
int scatter(int, size_t);

/* not EBCDIC-safe: assumes ASCII */
#define fhex(c) (((c) & 0x0F) + (((c) & 0x40) >> 6) * 9)

int
main(int argc, char *argv[])
{
	size_t n = 0;
	ssize_t r;
	int fd, urnd;
	int rv = 0;
	const char *fn = NULL;
	struct stat sb;

#ifdef __progname
	__progname = argv[0];
#endif

	/* syntax check */
	while ((fd = getopt(argc, argv, "a:dEf:")) != -1)
		switch (fd) {
		case 'a':
			r = 0;
			while (isxdigit(optarg[r]) && isxdigit(optarg[r + 1])) {
				data[n++] = (fhex(optarg[r]) << 4) |
				    fhex(optarg[r + 1]);
				r += 2;
			}
			if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
			    r / 2, "command line");
			break;
		case 'd':
			++debug;
			break;
		case 'E':
			earlyboot = 1;
			break;
		case 'f':
			fn = optarg;
			break;
		default:
 usage:
			fprintf(stderr, "E: usage: %s [-dE] [-a hex] -f filename\n"
			    "N: -a add extra bytes; -d debug, -E early boot\n"
			    "N: seed file must be at least 128 octets in size\n",
			    __progname);
			return (1);
		}
	argc -= optind;
	argv += optind;
	if (argc || !fn)
		goto usage;

	/* open stuff */
	if ((urnd = open(urnf, O_RDWR)) == -1) {
		fprintf(stderr, "E: (%s) %s: %s\n", urnf,
		    "cannot open", strerror(errno));
		return (1);
	}
	if ((fd = open(fn, O_RDWR)) == -1) {
		fprintf(stderr, "E: (%s) %s: %s\n", fn,
		    "cannot open", strerror(errno));
		return (1);
	}
	if (fstat(fd, &sb)) {
		fprintf(stderr, "E: (%s) %s: %s\n", fn,
		    "cannot stat", strerror(errno));
		return (1);
	}
	if (sb.st_size < 128) {
		fprintf(stderr, "E: (%s) %s %llu bytes\n", fn,
		    "too small (less than 128 bytes)",
		    (unsigned long long)sb.st_size);
		return (1);
	}

	/* gather some variant data */
	n = gather(urnd, n);
	if (debug) fprintf(stderr,
	    "D: gathered %zu octets, will now read %zu from seedfile\n",
	    n, (256 - n) > 128 ? 128 : (256 - n));
	/* plus 128 seed file octets */
	if ((r = readex(fd, data + n, (256 - n) > 128 ? 128 : (256 - n))) < 0) {
		fprintf(stderr, "E: (%s) %s %llu bytes\n", fn,
		    "cannot read",
		    (unsigned long long)((256 - n) > 128 ? 128 : (256 - n)));
		return (1);
	}
	n += (size_t)r;

	/* for visual inspection of seed quality (developer only) */
	if (debug > 1) {
		r = 0;
		while ((size_t)r < n) {
			if (r && !(r & 15))
				fprintf(stderr, "\n");
			if (!(r & 15))
				fprintf(stderr, "D:  %04zX ", r);
			if ((r & 15) == 8)
				fprintf(stderr, " -");
			fprintf(stderr, " %02X", data[r++]);
		}
		fprintf(stderr, "\n");
	}

	/* initialise the aRC4 stretching RNG */
	irc4random_init(data, n, 1);

	/* minimum octets to write to kernel */
	n = 32;
	/* but allow for randomly more, up to buffer size */
	n += irc4random_uniform(sizeof(data) - n + 1);
	/* get them, then write them to the kernel, but do not accredit yet */
	irc4random_buf(data, n);
	if ((r = writex(urnd, data, n)) == (ssize_t)-1) {
		fprintf(stderr, "E: (%s) %s %llu bytes\n", urnf,
		    "cannot write", (unsigned long long)n);
		rv = 1;
		goto writeback;
	}
	if (r < 0) {
		fprintf(stderr, "W: (%s) partial write: %s\n",
		    urnf, strerror(errno));
		r = -(r + 1);
	}
	fprintf(stderr, "I: (%s) wrote %zd bytes\n", urnf, r);
	n = (size_t)r;

 writeback:
	/* write back an exact amount of 128 octets to the seed */
	irc4random_buf(data, 128);
	if (lseek(fd, (off_t)0, SEEK_SET) == -1) {
		fprintf(stderr, "E: (%s) %s: %s\n", fn,
		    "cannot rewind", strerror(errno));
		return (1);
	}
	if ((r = writex(fd, data, 128)) == (ssize_t)-1) {
		fprintf(stderr, "E: (%s) %s %llu bytes\n", fn,
		    "cannot write", 128ULL);
		return (1);
	}
	if (r < 0) {
		fprintf(stderr, "W: (%s) partial write: %s\n",
		    fn, strerror(errno));
		r = -(r + 1);
	}
	fprintf(stderr, "I: (%s) wrote %zd bytes\n", fn, r);
	/* ensure the rewritten seed hits the platters */
	if (fsync(fd)) {
		fprintf(stderr, "E: (%s) %s: %s\n", fn,
		    "cannot fsync", strerror(errno));
		return (1);
	}
	if (close(fd))
		fprintf(stderr, "E: (%s) %s: %s\n", fn,
		    "cannot close", strerror(errno));
	if (r < 16) {
		/* next run will not be different enough */
		fprintf(stderr,
		    "E: rewrote less than 16 octets, not accrediting\n");
		return (1);
	}
	/* finally we can accredit the data into the RNG */
	if (rv == 0) switch (scatter(urnd, n)) {
	case 0:
		break;
	case 2:
		fprintf(stderr, "N: %s\n", strerror(errno));
		/* FALLTHROUGH */
	default:
		fprintf(stderr, "E: could not accredit bytes into RNG\n");
		return (1);
	}
	/* all done */
	if (close(urnd))
		fprintf(stderr, "E: (%s) %s: %s\n", urnf,
		    "cannot close", strerror(errno));
	return (rv);
}

size_t
gather(int fd, size_t pos)
{
	ssize_t z;
	size_t need = earlyboot ? 4 : 16;
#if defined(__linux__) && defined(__KLIBC__)
	struct timeval tv;
#else
	struct timespec tp;
#endif
#if defined(__OpenBSD__)
	size_t n;
	int mib[2];
#endif
#if defined(__linux__)
	union {
		unsigned long ul;
		void *ptr;
	} auxrnd;
#endif
#if USE_RDTSC
	uint64_t tscval;
#endif
#if USE_JYTTER
	uint32_t jytter_result[3];
	jytter_scratchspace scratch;

	/* cache 32 bit from Jytter */
	jytter_result[0] = do_jytter(&scratch);
#endif
#if defined(__OpenBSD__)
	/* get some bytes from the kernel, always */
	mib[0] = CTL_KERN;
	mib[1] = KERN_ARND;
	n = earlyboot ? 4 : 16;
	if (sysctl(mib, 2, data + pos, &n, NULL, 0) != -1) {
		/* errors cannot happen, so this is always true */
		pos += n;
		if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
		    n, "sysctl kern.arnd");
		need = 0;
	}
#endif
#if defined(__linux__)
	/* check if the kernel filled in the auxvec */
	if ((auxrnd.ul = getauxval(AT_RANDOM)) != 0) {
		memcpy(data + pos, auxrnd.ptr, 16);
		pos += 16;
		if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
		    (size_t)16, "auxvec AT_RANDOM");
		need = earlyboot ? 0 : 8;
	}
#endif
	/* add the current and runtime, more for shuffling than entropy */
#if defined(__linux__) && defined(__KLIBC__)
	if (gettimeofday(&tv, NULL) == 0) {
		memcpy(data + pos, &tv, sizeof(tv));
		pos += sizeof(tv);
		if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
		    sizeof(tv), "gettimeofday");
	}
#else
	if (clock_gettime(CLOCK_REALTIME, &tp) == 0) {
		memcpy(data + pos, &tp, sizeof(tp));
		pos += sizeof(tp);
		if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
		    sizeof(tp), "clock_gettime CLOCK_REALTIME");
	}
	if (clock_gettime(CLOCK_MONOTONIC, &tp) == 0) {
		memcpy(data + pos, &tp, sizeof(tp));
		pos += sizeof(tp);
		if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
		    sizeof(tp), "clock_gettime CLOCK_MONOTONIC");
	}
#endif
#if USE_JYTTER
	/* another 32 bit from Jytter, for later */
	jytter_result[1] = do_jytter(&scratch);
#endif
	/* read what’s still needed from urandom */
	if (need) {
		do {
			z = read(fd, data + pos, need);
		} while (z < 0 && errno == EINTR);
		if (z > 0) {
			pos += (size_t)z;
			if (debug) fprintf(stderr,
			    "D: added %zu bytes from %s\n",
			    z, urnf);
		} else
			if (debug) fprintf(stderr,
			    "D: could not get %zu bytes from %s\n",
			    need, urnf);
	}
#if USE_RDTSC
	/* get the TSC */
	__asm__ __volatile__("rdtsc"
	    : "=A" (tscval));
	memcpy(data + pos, &tscval, sizeof(tscval));
	pos += sizeof(tscval);
	if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
	    sizeof(tscval), "TSC");
#endif
#if USE_JYTTER
	/* get another 32 bit from Jytter and apply them */
	jytter_result[2] = do_jytter(&scratch);
	memcpy(data + pos, jytter_result, sizeof(jytter_result));
	pos += sizeof(jytter_result);
	if (debug) fprintf(stderr, "D: added %zu bytes from %s\n",
	    sizeof(jytter_result), "Jytter");
#endif
	return (pos);
}

/*
 * Accredits data into RNG. We always write at least 32 octets,
 * maybe more, and wish to always accredit at least 16 octets
 * worth, but in general, we use ¾ as factor (that makes for
 * six bits per byte). For sanity we limit ourselves to 128 bytes
 * with seven bits each accredited, to stay below seed length.
 */
int
scatter(int fd, size_t numbytes)
{
#if defined(__OpenBSD__) || defined(__linux__)
	u_int ebits;

	ebits = numbytes * 6; /* numbytes * 8 * 3/4 */
	if (ebits > (128 * 7))
		ebits = 128 * 7;
	return (ioctl(fd, RNDADDTOENTCNT, &ebits) == -1 ? 2 : 0);
#else
	fprintf(stderr, "N: don't know how to do that\n");
	return (1);
#endif
}

ssize_t
readex(int fd, uint8_t *buf, size_t size)
{
	ssize_t rv = 0, z;

	while (size) {
		if ((z = read(fd, buf, size)) < 0) {
			if (errno == EINTR)
				continue;
			return (rv ? /* fucked up since we got some */ -2 : -1);
		}
		if (z == 0)
			break;
		rv += z;
		buf += z;
		size -= z;
	}
	return (rv);
}

ssize_t
writex(int fd, const uint8_t *buf, size_t size)
{
	ssize_t rv = 0, z;

	while (size) {
		if ((z = write(fd, buf, size)) < 0) {
			if (errno == EINTR)
				continue;
#if 0
			return (rv ? /* fucked up since we got some */ -2 : -1);
#else
			return (rv ? -rv - 1 : -1);
#endif
		}
		rv += z;
		buf += z;
		size -= z;
	}
	return (rv);
}

#if 0
.endif

PROG=		rnd_shuf
NOMAN=		Yes
MK_MAN=		no
SRCS=		rnd_shuf.c
SRCS+=		arcfour_base.c arcfour_ksa.c
SRCS+=		irc4random_base.c irc4random_buf.c
SRCS+=		irc4random_uniform.c

.include <bsd.prog.mk>
.include "${.CURDIR}/bsdinc.mk"

.if "${USE_JYTTER}" != "0"
SRCS+=		do_jytter.S
.endif
#endif
