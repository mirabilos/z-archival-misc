#XXX not thought about this much

TG_CFLAGS+=	-Os -fweb -frename-registers
TG_CFLAGS+=	-fno-align-functions -fno-align-labels \
		-falign-loops=4 -falign-jumps=4
TG_CFLAGS+=	-Wformat -Wstrict-aliasing -Wmissing-declarations
TG_CFLAGS+=	-fwrapv
TG_CFLAGS+=	-fomit-frame-pointer

SRCS+=		open.c
