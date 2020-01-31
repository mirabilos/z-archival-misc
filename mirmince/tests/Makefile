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
# Top-level Makefile for mirₘᵢₙcⒺ/tests (experimental)

# settings (will change later)
DST?=		/opt/mirmince

# not settings
SHELL=		/bin/mksh
LIBCDIR=	${.CURDIR/..}
CC:=		${DST:Q}/bin/cc

.if defined(V) && $V > 0
CC+=		-v
.endif

all:

obj:
	mkdir -p ${.CURDIR:Q}/obj

depend:

PROGS+=		hw-unix
CLEANFILES+=	hw-unix.out

PROGS+=		env

CPPFLAGS:=
CFLAGS:=
AFLAGS:=
LDFLAGS:=
LDADD:=

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

all: ${PROGS}

.for _i in ${PROGS}
SRCS_${_i}?=${_i:=.c}
OBJS_${_i}?=${SRCS_${_i}:S/.c/.o/:S/.S/.o/}
${_i}: ${OBJS_${_i}}
.endfor

clean cleandir:
	rm -f ${PROGS} ${PROGS:=.o} ${CLEANFILES} *.out.*

TESTRUNS+=	hw-unix.0 hw-unix.1
RUN_hw-unix.0=	./hw-unix
RUN_hw-unix.1=	./hw-unix foo bar

TESTRUNS+=	env.0
RUN_env.0=	/usr/bin/env -i FOO=bar ./env

regress: ${PROGS}
	@:>tests.out.failed
	@:>tests.out.passed
.for _i in ${TESTRUNS}
	@set +e; d=${_i:Q}; s=${.CURDIR:Q}/$$d; o=PASS; :>"$$d.out.0"; \
	if [[ -s $$s.t.0 ]]; then \
		cat "$$s.t.0"; \
	else \
		:; \
	fi | ( ( ${RUN_${_i}} ); echo $$? >"$$d.out.e" ) \
	    >"$$d.out.1" 2>"$$d.out.2"; \
	if [[ -s $$s.t.e ]]; then \
		diff -u "$$d.out.e" "$$s.t.e" >>"$$d.out.0" || o=FAIL; \
	else \
		ec=$$(<$$d.out.e); \
		if (( ec )); then \
			echo "Unexpected errorlevel $$ec" >>"$$d.out.0"; \
			o=FAIL; \
		fi; \
	fi; \
	if [[ -s $$s.t.1 ]]; then \
		diff -u "$$d.out.1" "$$s.t.1" >>"$$d.out.0" || o=FAIL; \
	elif [[ -s $$d.out.1 ]]; then \
		diff -u "$$d.out.1" /dev/null >>"$$d.out.0"; \
		o=FAIL; \
	fi; \
	if [[ -s $$s.t.2 ]]; then \
		diff -u "$$d.out.2" "$$s.t.2" >>"$$d.out.0" || o=FAIL; \
	elif [[ -s $$d.out.2 ]]; then \
		diff -u "$$d.out.2" /dev/null >>"$$d.out.0"; \
		o=FAIL; \
	fi; \
	if [[ -s $$s.t.do ]]; then \
		${SHELL} "$$s.t.do" "$$d" "$$s" >>"$$d.out.0" || o=FAIL; \
	fi; \
	print -r -- $$o ${_i:Q}: ${RUN_${_i}:Q}; \
	cat "$$d.out.0"; \
	if [[ $$o = PASS ]]; then \
		print -r -- ${_i:Q} >>tests.out.passed; \
	else \
		print -r -- ${_i:Q} >>tests.out.failed; \
	fi
.endfor
	@print -r -- Total failed: $$(wc -l <tests.out.failed)
	@print -r -- Total passed: $$(wc -l <tests.out.passed)
	@[[ ! -s tests.out.failed ]]
