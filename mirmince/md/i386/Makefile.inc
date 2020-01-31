.ifdef DEBUG
MD_USE_REGPARM?=	No
MD_USE_RTD?=		No
.endif

MD_USE_REGPARM?=	Yes
MD_USE_RTD?=		Yes

.if ${MD_USE_REGPARM:L} == "yes"
TG_CFLAGS+=		-mregparm=3 -D__mirmincE_use_regparm__=3
TG_VARIANT_DEF:=	${TG_VARIANT_DEF}-regparm
.endif

.if ${MD_USE_RTD:L} == "yes"
TG_CFLAGS+=		-mrtd -D__mirmincE_use_rtd__
TG_VARIANT_DEF:=	${TG_VARIANT_DEF}-rtd
.endif
