# $MirOS: contrib/hosted/ewe/vm/Makefile.inc,v 1.12 2008/05/11 23:15:25 tg Exp $

.ifndef _MODCONTRIB_HOSTED_EWE_VM_MAKEFILE_INC
_MODCONTRIB_HOSTED_EWE_VM_MAKEFILE_INC=1

PROG?=		ewe
NOMAN?=		Yes
SRCS+=		ewe.cpp
SRCS+=		version.c
CPPFLAGS+=	-DDONT_DEBUG_LIBRARY
CPPFLAGS+=	-DMACHINE=\"${MACHINE:Q}\"
CPPFLAGS+=	-DMACHINE_ARCH=\"${MACHINE_ARCH:Q}\"
AFLAGS+=	-Wa,-I${.CURDIR}/../../devel

.include <bsd.own.mk>

EXPERIMENTAL=	Yes	# prevent MirMake from adding -Werror
OUR_FLAGS=	-O1 -fwrapv -fno-omit-frame-pointer -fsigned-char -pthread
MKC_DEBG?=	No
.if ${MKC_DEBG:L} != "no"
OUR_FLAGS+=	-g3
.endif
OUR_FLAGS+=	-fno-strict-aliasing
COPTS+=		${OUR_FLAGS}
CXXOPTS+=	${OUR_FLAGS} -fpermissive
.if (${OStype} != "Darwin")
LDFLAGS+=	-Wl,--export-dynamic
.endif
LDFLAGS+=	-pthread
LDADD+=		-lm -lz
DPADD+=		${LIBM} ${LIBZ}
.if (${OStype} != "OpenBSD")
LDADD+=		-ldl
.endif
PREFIX?=	/usr/local
BINDIR?=	${PREFIX}/bin
CPPFLAGS+=	-DPREFIX=\"${PREFIX:Q}\"

# possible values: gtk1 gtk2 cli
USE_TOOLKIT?=	gtk1

.ifndef JPEG_PREFIX
.  if exists(/usr/include/jpeglib.h)
JPEG_PREFIX=	/usr
.  elif exists(/usr/mpkg/include/jpeglib.h)
JPEG_PREFIX=	/usr/mpkg
.  elif exists(/sw/include/jpeglib.h)
JPEG_PREFIX=	/sw
.  endif
.endif
JPEG_PREFIX?=	${PREFIX}

.if ${JPEG_PREFIX} != "no"
CPPFLAGS+=	-I${JPEG_PREFIX}/include
LDFLAGS+=	-L${JPEG_PREFIX}/lib
.endif
LDADD+=		-ljpeg

.if ${USE_TOOLKIT} == "gtk1"
.  if !make(obj) && !make(install) && !make(clean) && !make(cleandir)
GTK_CFLAGS!=	gtk-config --cflags
GTK_LIBS!=	gtk-config --libs
.  endif
CPPFLAGS+=	${GTK_CFLAGS} -DGTK_VERSION_1_2
LDADD+=		${GTK_LIBS}
SRCS+=		mpixbuf.c
.elif ${USE_TOOLKIT} == "gtk2"
.  if !make(obj) && !make(install) && !make(clean) && !make(cleandir)
PKGC_PATH?=	PKG_CONFIG_PATH=/usr/X11R6/lib/pkgconfig:/usr/libdata/pkgconfig:$$PKG_CONFIG_PATH
GTK_CFLAGS!=	${PKGC_PATH} pkg-config --cflags gtk+-2.0
GTK_LIBS!=	${PKGC_PATH} pkg-config --libs gtk+-2.0
.  endif
CPPFLAGS+=	${GTK_CFLAGS} -DGTK_VERSION_2_0
LDADD+=		${GTK_LIBS}
SRCS+=		mpixbuf.c
.elif ${USE_TOOLKIT} == "cli"
CPPFLAGS+=	-DNOGUI
.else
.  error wrong USE_TOOLKIT: ${USE_TOOLKIT}
.endif

EMBED_EWE?=	Yes
.if ${EMBED_EWE:L} == "no"
CPPFLAGS+=	-DNOEMBEDDEWE
.else
SRCS+=		EweConfig.S classes.S
.endif

EweConfig.o: ../devel/EweConfig.ewe
classes.o: ../devel/ewe.ewe

.PATH: ${.CURDIR}/..

.if exists(${.SYSMK}/libmirmake.a)
LDADD+=		-L${.SYSMK:Q} -lmirmake
CPPFLAGS+=	-D_MIRMAKE_DEFNS -include ${.SYSMK:Q}/mirmake.h
.endif

.if ${NOMAN:L} == "no"
MLINKS+=	ewe.1 ewene.1
MLINKS+=	ewe.1 ewecl.1
MLINKS+=	ewe.1 eweclne.1
MLINKS+=	ewe.1 eweg2.1
MLINKS+=	ewe.1 eweg2ne.1
MLINKS+=	ewe.1 ewedev.1
MLINKS+=	ewe.1 ewemp.1
.endif

.endif
