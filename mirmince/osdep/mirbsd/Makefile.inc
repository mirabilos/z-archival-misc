#XXX this is gcc only right now

TG_CFLAGS+=	-Os -fweb -frename-registers
TG_CFLAGS+=	-fno-align-functions -fno-align-labels \
		-falign-loops=4 -falign-jumps=4

.if ${TG_ARCH} == "i386"
TG_CFLAGS+=	-march=i486 -mpush-args -mpreferred-stack-boundary=2
.elif ${TG_ARCH} == "sparc"
TG_CFLAGS+=	-mcpu=v8 -mtune=hypersparc
.else
.error Unknown MirBSD architecture "${TG_ARCH}"
.endif

TG_CFLAGS+=	-Wformat -Wstrict-aliasing -Wmissing-declarations
TG_CFLAGS+=	-fwrapv
TG_CFLAGS+=	-fomit-frame-pointer

MINCEBEG_OSDEP=	mincebeg_os.o
