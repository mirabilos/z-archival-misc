MKSH?=		mksh
GENERATED+=	linklist.htm linklist.tex

all: linklist.htm linklist.tex

linklist.htm linklist.tex: cvtdat.sh linklist.dat
	${MKSH} cvtdat.sh linklist

clean:
	rm -f ${CLEANFILES} ${GENERATED}

.PHONY: all clean
