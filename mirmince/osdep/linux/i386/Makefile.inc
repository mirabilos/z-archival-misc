#XXX not thought about this much

TG_CFLAGS+=	-march=i486 -mpush-args -mpreferred-stack-boundary=2

MINCEBEG_OSMD=	mincebeg_md.o

SRCS+=		syscalls.S
