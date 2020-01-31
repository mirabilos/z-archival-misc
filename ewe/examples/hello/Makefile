# $MirOS: contrib/hosted/ewe/examples/hello/Makefile,v 1.1 2008/04/10 17:18:02 tg Exp $

JPROG=		HelloWorld
JSRCS=		${JPROG}.java
JOBJS=		${JSRCS:S/.java$/.class/}
CLEANFILES+=	${JOBJS}

all: ${JOBJS}

run:
	ewe ${JPROG}

.SUFFIXES: .class .java

.java.class:
	gcj -C --bootclasspath=${.CURDIR}/../../classes \
	    --classpath=${.CURDIR} -d . ${.IMPSRC}

.include <bsd.prog.mk>
