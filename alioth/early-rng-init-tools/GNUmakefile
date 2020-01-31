CC?=		gcc
CPPFLAGS?=
CFLAGS?=	-O2
LDFLAGS?=

IS_X86:=	$(shell x=$$(printf '%s\n' '\#if defined(__i386__) || defined(__x86_64__)' 'char is_x86_yes;' '\#else' 'char is_x86_no;' '\#endif' | ${CC} ${CPPFLAGS} ${CFLAGS} -E - 2>&1) || x="errorlevel non-zero! $$x"; case $$x in (*is_x86_yes*) echo 1 ;; (*is_x86_no*) echo 0 ;; (*) echo error; printf >&2 'N: %s\n' "$$x" ;; esac)
ifneq (0,${IS_X86})
ifneq (1,${IS_X86})
$(error cannot determine whether target is x86 or not: ${IS_X86})
endif
endif
USE_JYTTER?=	${IS_X86}
CPPFLAGS+=	-DUSE_JYTTER=${USE_JYTTER}
USE_RDTSC?=	${IS_X86}
CPPFLAGS+=	-DUSE_RDTSC=${USE_RDTSC}

ifneq (0,${USE_JYTTER})
PROGS+=		rnd_jytter
endif
PROGS+=		rnd_shuf

all: ${PROGS}

INSTALL?=	install
INSTALL_COPY?=	-c
INSTALL_STRIP?=	#-s
BINOWN?=	root
BINGRP?=	root
BINMODE?=	755

install:
	${INSTALL} ${INSTALL_COPY} ${INSTALL_STRIP} -o ${BINOWN} -g ${BINGRP} \
	    -m ${BINMODE} ${PROGS} ${DESTDIR}${BINDIR}/

clean:
	-rm -f *.o ${PROGS}

rnd_jytter_O=	do_jytter.o rnd_jytter.o

rnd_jytter: ${rnd_jytter_O}
	${CC} ${CFLAGS} ${LDFLAGS} -o $@ ${rnd_jytter_O} ${LDADD}

rnd_shuf_O=	rnd_shuf.o
rnd_shuf_O+=	arcfour_base.o arcfour_ksa.o
rnd_shuf_O+=	irc4random_base.o irc4random_buf.o
rnd_shuf_O+=	irc4random_uniform.o
ifneq (0,${USE_JYTTER})
rnd_shuf_O+=	do_jytter.o
endif

rnd_shuf: ${rnd_shuf_O}
	${CC} ${CFLAGS} ${LDFLAGS} -o $@ ${rnd_shuf_O} ${LDADD}

.c.o:
	${CC} ${CPPFLAGS} ${CFLAGS} -c -o $@ $<

.S.o:
	${CC} -D_ASM_SOURCE ${AFLAGS} ${CPPFLAGS} -c -o $@ $<

include depend.mk
