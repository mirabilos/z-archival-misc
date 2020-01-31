.include <bsd.own.mk>
.include "${.CURDIR}/depend.mk"

IS_X86!=	x=$$({ echo '\#if defined(__i386__) || defined(__x86_64__)'; echo 'char is_x86_yes;'; echo '\#else'; echo 'char is_x86_no;'; echo '\#endif'; } | ${CC} ${CPPFLAGS} ${CFLAGS} -E - 2>&1) || x="errorlevel non-zero! $$x"; case $$x in (*is_x86_yes*) echo 1 ;; (*is_x86_no*) echo 0 ;; (*) echo error; echo >&2 "N: $$x" ;; esac
.if "${IS_X86}" != "0" && "${IS_X86}" != "1"
. error cannot determine whether target is x86 or not: ${IS_X86}
.endif
USE_JYTTER?=	${IS_X86}
CPPFLAGS+=	-DUSE_JYTTER=${USE_JYTTER}
USE_RDTSC?=	${IS_X86}
CPPFLAGS+=	-DUSE_RDTSC=${USE_RDTSC}
