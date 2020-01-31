# Copyright © 2013
#	Thorsten Glaser <tg@mirbsd.org>
#
# Provided that these terms and disclaimer and all copyright notices
# are retained or reproduced in an accompanying document, permission
# is granted to deal in this work without restriction, including un‐
# limited rights to use, publicly perform, distribute, sell, modify,
# merge, give away, or sublicence.
#
# This work is provided “AS IS” and WITHOUT WARRANTY of any kind, to
# the utmost extent permitted by applicable law, neither express nor
# implied; without malicious intent or gross negligence. In no event
# may a licensor, author or contributor be held liable for indirect,
# direct, other damage, loss, or other issues arising in any way out
# of dealing in the work, even if advised of the possibility of such
# damage or existence of a defect, except proven that it results out
# of said person’s immediate fault when using the work as intended.
#-
# Top-level Makefile for mirₘᵢₙcⒺ (experimental)

# settings (will change later)
CC?=		mgcc
LD?=		ld
STRIP?=		strip
OBJCOPY?=	objcopy
DST?=		/opt/mirmince
TG_ARCH?=	i386
TG_OS?=		mirbsd

# not settings
SHELL=		/bin/mksh
LIBCDIR=	${.CURDIR}
OBJADIR!=	realpath .
TG_CC:=		${CC}
TG_CFLAGS+=	-fno-builtin
CC:=		${OBJADIR}/cc
_CONF:=		${LIBCDIR}/obj/.conf-${TG_ARCH:Q}-${TG_OS:Q}-${OBJADIR:S!/!_!g:Q}
TGS_ALL:=	mirbsd/i386
TGS_ALL+=	linux/i386
TGS_ALL+=	midnightbsd/amd64
TGS_ALL+=	midnightbsd/i386
TGS_OS:=	${TGS_ALL:H}
TGS_CPU:=	${TGS_ALL:T}

.if defined(V) && $V > 0
CC+=		-v
.endif

all:

obj:
	mkdir -p ${.CURDIR:Q}/obj

CPPFLAGS:=	-D_ALL_SOURCE
CPPFLAGS+=	-I${LIBCDIR:Q}/include
CPPFLAGS+=	-D__mirmincE_bootstrap -I${LIBCDIR:Q} -I${OBJADIR:Q}
CFLAGS:=
AFLAGS:=
LDFLAGS:=
LDADD:=

.PATH: ${LIBCDIR}/osdep/${TG_OS}/${TG_ARCH}
.PATH: ${LIBCDIR}/osdep/${TG_OS}
.PATH: ${LIBCDIR}/md/${TG_ARCH}
.PATH: ${LIBCDIR}/mi

TG_VARIANT_DEF:=${TG_OS}-${TG_ARCH}

.include "${LIBCDIR}/mi/Makefile.inc"
.include "${LIBCDIR}/md/${TG_ARCH}/Makefile.inc"
.include "${LIBCDIR}/osdep/${TG_OS}/Makefile.inc"
.include "${LIBCDIR}/osdep/${TG_OS}/${TG_ARCH}/Makefile.inc"

TG_VARIANT?=	${TG_VARIANT_DEF}

.if !make(obj) && !make(depend) && !make(clean) && !make(cleandir)
.include "${LIBCDIR}/obj/Makefile.gen"
.endif

.ifdef DEBUG
CFLAGS+=	-g3 -fno-omit-frame-pointer
.endif

.SUFFIXES: .a .o .S .i .c .h
.LIBS: .a

COMPILE.S:=	${CC} ${CPPFLAGS} ${AFLAGS} -c
COMPILE.c:=	${CC} ${CPPFLAGS} ${CFLAGS} -c
LINK.c:=	${CC} ${CPPFLAGS} ${CFLAGS} ${LDFLAGS}

.o:
	${LINK.c} -o $@ ${.IMPSRC} ${LDADD}
.S.o:
	${COMPILE.S} ${AFLAGS_${.TARGET:T:R}:M*} ${.IMPSRC}
.c.o:
	${COMPILE.c} ${CFLAGS_${.TARGET:T:R}:M*} ${.IMPSRC}
.c.i:
	${COMPILE.c} ${CFLAGS_${.TARGET:T:R}:M*} ${.IMPSRC} -E -o $@

all: mincebeg.o

MINCEBEG_OSDEP?=
MINCEBEG_OSMD?=

# un-nice: despite localising, _mince_csu_run is not discarded
mincebeg.o: ${MINCEBEG_OSDEP} ${MINCEBEG_OSMD} mincebeg_mi.o
	${LD} -X -r -o $@.o $>
	${OBJCOPY} -G _mince_csu_init -x $@.o $@
	@rm -f $@.o

${_CONF}:
	@:>$@

DCLEANFILES+=	cc cc-spec .cc-done

depend: cc cc-spec
cc cc-spec: .cc-done
.cc-done: ${_CONF} gcc-mk gcc-spec gcc-wrap
	${SHELL} ${.CURDIR:Q}/gcc-mk ${TG_CC:Q} ${DST:Q} \
	    ${TG_ARCH:Q} ${TG_OS:Q} ${TG_CFLAGS:M*:Q} \
	    ${OBJADIR:Q} ${OBJADIR:Q} ${OBJADIR:Q}
	@:>$@

DCLEANFILES+=	Makefile.gen
CVSYSCLS+=	Makefile.gen
Makefile.gen: ${_CONF} cvsyscls ${LIBCDIR}/osdep/${TG_OS}/syscalls
	${SHELL} ${LIBCDIR:Q}/cvsyscls $@ \
	    ${LIBCDIR:Q}/osdep/${TG_OS}/syscalls

.for _i in ${TGS_OS}
CVSYSCLS+=	osdep/${_i}/syscall.h osdep/${_i}/syscallc.h

osdep/${_i}/syscall.h osdep/${_i}/syscallc.h: osdep/${_i} \
    ${_CONF} cvsyscls ${LIBCDIR}/osdep/${_i}/syscalls
	@mkdir -p osdep/${_i}
	${SHELL} ${LIBCDIR:Q}/cvsyscls $@ \
	    ${LIBCDIR}/osdep/${_i}/syscalls
.endfor

SYSCALLS+=${SYSCALLS_${TG_ARCH}}

CLEANFILES+=	sys_*.S
.for _n _k _a in ${SYSCALLS}
SOBJS+=		sys_${_n}.o

sys_${_n}.S:
	x='#include <mirmince/asm.h>'; \
	x+=$$'\n#include mirmince_hdr_osdep(syscall.h)'; \
	x+=$$'\nSYSCALL${_a}(${_n}, ${_k})'; \
	    print -r -- "$$x" >$@

sys_${_n}.o: sys_${_n}.S
	${COMPILE.S} -o $@.o $>
	${LD} -X -r -o $@ $@.o
	@rm -f $@.o
.endfor

depend: ${CVSYSCLS}

all: libminze.a

SOBJS+=		${SRCS:M*.S:.S=.o}

libminze.a: ${SOBJS}
	ar rc $@~ ${SOBJS}
	ranlib $@~
	mv -f $@~ $@

all: libmince.a

COBJS+=		${SRCS:M*.c:.c=.o}

libmince.a: ${COBJS}
	ar rc $@~ ${COBJS}
	ranlib $@~
	mv -f $@~ $@

install:
	${SHELL} ${.CURDIR:Q}/do-install ${TG_CC:Q} ${DST:Q} \
	    ${TG_ARCH:Q} ${TG_OS:Q} ${TG_CFLAGS:M*:Q} ${TG_VARIANT:Q}

install-source: install .WAIT do-install-source
do-install-source: gcc-mk gcc-spec gcc-wrap ${SRCS:M*.c}
	mkdir -p "$$DESTDIR"${DST:Q}/libdata/${TG_VARIANT:Q}/source
	print -r -- SRCS=${SRCS:M*.c:Q} \
	    >"$$DESTDIR"${DST:Q}/libdata/${TG_VARIANT:Q}/source/Makefile
.for _i in TG_CC DST TG_ARCH TG_OS TG_CFLAGS TG_VARIANT
	print -r -- ${_i}=${${_i}:M*:Q} \
	    >>"$$DESTDIR"${DST:Q}/libdata/${TG_VARIANT:Q}/source/Makefile
.endfor
	cat ${.CURDIR:Q}/Makefrag.inst \
	    >>"$$DESTDIR"${DST:Q}/libdata/${TG_VARIANT:Q}/source/Makefile
	cp -f ${.ALLSRC} "$$DESTDIR"${DST:Q}/libdata/${TG_VARIANT:Q}/source/
.PHONY: install-source do-install-source

regress:
	@echo No regression tests here, but feel free to clone the
	@echo mirmince/tests.git repository and play around with that.

clean:
	test -d ${.CURDIR:Q}/obj/.
	rm -f *~ *.o *.a .conf-* ${CLEANFILES}

cleandir: clean
	rm -f ${DCLEANFILES}
	rm -rf osdep

.PHONY: obj clean cleandir depend all install regress
