/*
 * Copyright (C) 1995, 1996, 1997, and 1998 WIDE Project.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
/*
 * Most of X11 code was derived from twiddler driver.
 */

#include <sys/types.h>
#if HAVE_SYS_TIME_H
#include <sys/time.h>
#elif HAVE_TIME_H
#include <time.h>
#endif
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <err.h>
#include <fcntl.h>
#include <termios.h>
#include <signal.h>
#include <string.h>
#include <X11/X.h>
#include <X11/extensions/XTest.h>
#include <X11/keysym.h>

#if HAVE_UTIL_H
#include <util.h>
#endif

#if !HAVE_UU_LOCK
extern int uu_lock(const char *);
extern int uu_unlock(const char *);
#endif

#undef unused_parameter
#if HAVE_ATTRIBUTE_UNUSED
#define unused_parameter __attribute__((__unused__))
#else
#define unused_parameter /* nothing */
#endif

#ifdef X_DISPLAY_MISSING
# error this program cannot be compiled without X11.
#endif

#ifndef MDMBUF
# warning MDMBUF (DTR/DCD hardware flow control) missing, might not work
# define MDMBUF 0 /* kiddie You-nix */
#endif

#ifndef REMOTE_DEVICE
# define REMOTE_DEVICE	"/dev/tty02"	/*biased to VAIO505 :-P*/
#endif

static const char *remote = REMOTE_DEVICE;
static int debug = 0;
#define dprintf(x)	{ if (debug) fprintf x; }
static int verbose = 0;
#define verbmsg(x)	{ if (verbose) fprintf x; }

Display *display = NULL;
static uid_t uid, euid;
static gid_t gid, egid;
static int uidswapped = 0;

static void usage(void);
static void mainloop(int);
static void buttonpress(int, int);
static int uucplock(const char *);
static int uucpunlock(const char *);
static void sigtrap(int)
#if HAVE_ATTRIBUTE_NORETURN
    __attribute__((__noreturn__))
#endif
    ;
static void daemonuid(void);
static void useruid(void);

#define EFFECT	0x15
#define LEFT	0x16
#define RIGHT	0x17
#define RELEASE	0x55

int
main(int argc, char *argv[])
{
	int fd;
	struct termios old, raw;
	int ch;

	uid = getuid();
	euid = geteuid();
	gid = getgid();
	egid = getegid();
	useruid();

	while ((ch = getopt(argc, argv, "df:v")) != EOF) {
		switch (ch) {
		case 'd':
			debug++;
			break;
		case 'f':
			remote = optarg;
			break;
		case 'v':
			verbose++;
			break;
		default:
			usage();
			/*NOTREACHED*/
		}
	}
	argc -= optind;
	argv += optind;

	fprintf(stderr, "initializing...\r");

	verbmsg((stderr, "initializing X11... "));
	display = XOpenDisplay(getenv("DISPLAY"));
	if (!display)
		errx(1, "opening X11 display");
	verbmsg((stderr, "done.\n"));

	verbmsg((stderr, "initializing serial port %s... ", remote));
	daemonuid();
	if (!strchr(remote, '/')) {
		char *p;
		p = (char *)malloc(strlen("/dev/") + strlen(remote) + 1);
		if (!p)
			err(1, "malloc");
		sprintf(p, "/dev/%s", remote);
		remote = p;
	}
	if (uucplock(remote) < 0) {
		errx(1, "cannot lock %s", remote);
		/*NOTREACHED*/
	}
	signal(SIGINT, sigtrap);
	signal(SIGHUP, sigtrap);
	fd = open(remote, O_NONBLOCK);
	if (fd < 0) {
		uucpunlock(remote);
		err(1, "%s", remote);
		/*NOTREACHED*/
	}
	tcgetattr(fd, &old);
	raw = old;
	cfmakeraw(&raw);
	cfsetspeed(&raw, B1200);
	raw.c_cflag &= ~(CSIZE|PARENB|CSTOPB|MDMBUF);
	raw.c_cflag |= CS8|CREAD|CLOCAL;
	tcsetattr(fd, TCSANOW, &raw);
	sleep(1);
	close(fd);
	sleep(1);

	fd = open(remote, O_RDONLY);
	if (fd < 0) {
		uucpunlock(remote);
		err(1, "%s", remote);
		/*NOTREACHED*/
	}
	useruid();
	verbmsg((stderr, "done.\n"));

	fprintf(stderr, "xmindpath ready.\n");
	fprintf(stderr, "\007");

	mainloop(fd);

	uucpunlock(remote);
	exit(0);
}

static void
usage(void)
{
	fprintf(stderr, "usage: xmindpath [-dv] [-f dev]\n");
	exit(0);
}

static void
mainloop(int fd)
{
	struct timeval tv;
	fd_set rfd;
	unsigned char buf[BUFSIZ];
	int nfd;
	int len;
	int heartbeat = 0;
	int i;

	while (1) {
		tv.tv_sec = 0;
		tv.tv_usec = 500*1000;
		FD_ZERO(&rfd);
		FD_SET(fd, &rfd);
		nfd = select(fd + 1, &rfd, 0, 0, &tv);
		if (nfd < 0) {
			uucpunlock(remote);
			err(1, "select");
			/*NOTREACHED*/
		}

		switch (nfd) {
		case 0:	/*timeout*/
			dprintf((stderr, "%c\r", "\\|/-"[heartbeat++ % 4]));
			break;
		case 1:
			len = read(fd, buf, sizeof(buf));
			for (i = 0; i < len; i++) {
				/* filter out bogus remote commander code */
				switch (buf[i]) {
				case LEFT:
				case RIGHT:
				case EFFECT:
				case RELEASE:
					break;
				default:
					dprintf((stderr, "bogus char: %02x\n",
						buf[i]));
					continue;
				}

				switch (buf[i]) {
				case LEFT:
					dprintf((stderr, "left\n"));
					buttonpress(Button3, 1);
					break;
				case RIGHT:
					dprintf((stderr, "right\n"));
					buttonpress(Button1, 1);
					break;
				case EFFECT:
					dprintf((stderr, "effect\n"));
					buttonpress(Button2, 1);
					break;
				case RELEASE:
					dprintf((stderr, "release\n"));
					buttonpress(Button1, 0);
					buttonpress(Button2, 0);
					buttonpress(Button3, 0);
					break;
				default:
					dprintf((stderr, "%02x\n", buf[i]));
					continue;
				}
			}
			break;
		default:
			dprintf((stderr, "select=%d\n", nfd));
			break;
		}
	}
}

/* fake mouse button press */
static void
buttonpress(int button, int state)
{
	int offset;
	static int buttonstate[5];	/* X11 defines 5 buttons */

	offset = button - Button1;
	if (offset < 0 ||
	    sizeof(buttonstate)/sizeof(buttonstate[0]) <= (size_t)offset)
		return;

	/* edge trigger */
	if ((buttonstate[offset] && state) || (!buttonstate[offset] && !state))
		return;

	XTestFakeButtonEvent(display, button, state ? True : False, 0);
	buttonstate[offset] = state;
	XFlush(display);
}

static int
uucplock(const char *name)
{
	return uu_lock(strrchr(name, '/') + 1);
}

static int
uucpunlock(const char *name)
{
	return uu_unlock(strrchr(name, '/') + 1);
}

static void
sigtrap(int no unused_parameter)
{
	uucpunlock(remote);
	exit(1);
}

static void
daemonuid(void)
{
	if (uidswapped == 0)
		return;

#if HAVE_SETREUID
	if (setreuid(uid, euid))
		err(1, "could not switch to %s in %s", "daemonuid", "setreuid");
	if (setregid(gid, egid))
		err(1, "could not switch to %s in %s", "daemonuid", "setregid");
#else
	if (setuid(uid))
		err(1, "could not switch to %s in %s", "daemonuid", "setuid");
	if (seteuid(euid))
		err(1, "could not switch to %s in %s", "daemonuid", "seteuid");
	if (setgid(gid))
		err(1, "could not switch to %s in %s", "daemonuid", "setgid");
	if (setegid(egid))
		err(1, "could not switch to %s in %s", "daemonuid", "setegid");
#endif
	uidswapped = 0;
}

static void
useruid(void)
{
	if (uidswapped == 1)
		return;

#if HAVE_SETREUID
	if (setregid(egid, gid))
		err(1, "could not switch to %s in %s", "useruid", "setregid");
	if (setreuid(euid, uid))
		err(1, "could not switch to %s in %s", "useruid", "setreuid");
#else
	if (setgid(egid))
		err(1, "could not switch to %s in %s", "useruid", "setgid");
	if (setegid(gid))
		err(1, "could not switch to %s in %s", "useruid", "setegid");
	if (setuid(euid))
		err(1, "could not switch to %s in %s", "useruid", "setuid");
	if (seteuid(uid))
		err(1, "could not switch to %s in %s", "useruid", "seteuid");
#endif
	uidswapped = 1;
}
