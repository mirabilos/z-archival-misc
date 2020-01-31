#XXX this is gcc only right now

TG_CFLAGS+=	-Os -fweb -frename-registers
TG_CFLAGS+=	-fno-align-functions -fno-align-labels \
		-falign-loops=4 -falign-jumps=4

.if ${TG_ARCH} == "i386"
TG_CFLAGS+=	-march=i486 -mpreferred-stack-boundary=2
.elif ${TG_ARCH} == "amd64"
TG_CFLAGS+=	-falign-loops=8
.else
.error Unknown MidnightBSD architecture "${TG_ARCH}"
.endif

TG_CFLAGS+=	-Wformat -Wstrict-aliasing -Wmissing-declarations
TG_CFLAGS+=	-fwrapv
TG_CFLAGS+=	-fomit-frame-pointer
