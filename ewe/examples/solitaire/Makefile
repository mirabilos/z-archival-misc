# $MirOS: contrib/hosted/ewe/examples/solitaire/Makefile,v 1.1 2008/04/10 17:18:02 tg Exp $

JPROG=		Solitaire
JSRCS=		${JPROG}.java SolitaireBoard.java SolitaireForm.java
JOBJS=		${JSRCS:S/.java$/.class/}
CLEANFILES+=	${JOBJS}

CLEANFILES+=	SolitaireBoard\$$Cursor.class

all: ${JOBJS}

run: ${.OBJDIR}/EXE_ICON.ICO
	ewe ${JPROG}

${.OBJDIR}/EXE_ICON.ICO:
	cd ${.CURDIR} && pax -rw images EXE_ICON.ICO Icon* ${.OBJDIR}/

.SUFFIXES: .class .java

.java.class:
	gcj -C --bootclasspath=${.CURDIR}/../../classes \
	    --classpath=${.CURDIR} -d . ${.IMPSRC}

cleandir: clean-data

clean-data:
	rm -rf images EXE_ICON.ICO Icon*

.include <bsd.prog.mk>
