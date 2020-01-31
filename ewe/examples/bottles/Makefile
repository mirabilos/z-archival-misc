# $MirOS: contrib/hosted/ewe/examples/bottles/Makefile,v 1.1 2008/04/10 17:18:01 tg Exp $

JPROG=		bottles
JSRCS=		${JPROG}.java
JOBJS=		${JSRCS:S/.java$/.class/}
CLEANFILES+=	${JOBJS}

all: ${JOBJS}

run:
	eweclne ${JPROG}

.SUFFIXES: .class .java

.java.class:
	gcj -C --bootclasspath=${.CURDIR}/../../classes \
	    --classpath=${.CURDIR} -d . ${.IMPSRC}

.include <bsd.prog.mk>
