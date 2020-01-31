#!/bin/sh
srcversion='1.2 (Alpha)'
#-
# Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010,
#		2011, 2012, 2013, 2014, 2015, 2016, 2017, 2019
#	mirabilos <m@mirbsd.org>
# Copyright (c) 2018, 2019
#	mirabilos <t.glaser@tarent.de>
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. Neither the name of the project nor the names of its contributors
#    may be used to endorse or promote products derived from this software
#    without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS “AS IS” AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
# OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
# OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# This script is also available under The MirOS Licence.
#-
# People analysing the output must whitelist conftest.c for any kind
# of compiler warning checks (mirtoconf is by design not quiet).
#
# Used environment documentation is at the end of this file.

LC_ALL=C; LANGUAGE=C
export LC_ALL; unset LANGUAGE

case $ZSH_VERSION:$VERSION in
:zsh*) ZSH_VERSION=2 ;;
esac

if test -n "${ZSH_VERSION+x}" && (emulate sh) >/dev/null 2>&1; then
	emulate sh
	NULLCMD=:
fi

if test -d /usr/xpg4/bin/. >/dev/null 2>&1; then
	# Solaris: some of the tools have weird behaviour, use portable ones
	PATH=/usr/xpg4/bin:$PATH
	export PATH
fi

nl='
'
safeIFS='	'
safeIFS=" $safeIFS$nl"
IFS=$safeIFS
allu=QWERTYUIOPASDFGHJKLZXCVBNM
alll=qwertyuiopasdfghjklzxcvbnm
alln=0123456789
alls=______________________________________________________________

case `echo a | tr '\201' X` in
X)
	# EBCDIC build system
	lfcr='\n\r'
	;;
*)
	lfcr='\012\015'
	;;
esac

echo "For the build logs, demonstrate that /dev/null and /dev/tty exist:"
ls -l /dev/null /dev/tty

v() {
	$e "$*"
	eval "$@"
}

vv() {
	_c=$1
	shift
	$e "\$ $*" 2>&1
	eval "$@" >vv.out 2>&1
	sed "s^${_c} " <vv.out
}

vq() {
	eval "$@"
}

rmf() {
	for _f in "$@"; do
		case $_f in
		mgp.1|mgp.c|mgp.h) ;;
		*) rm -f "$_f" ;;
		esac
	done
}

tcfn=no
bi=
ui=
ao=
fx=
me=`basename "$0"`
orig_CFLAGS=$CFLAGS
phase=x

if test -t 1; then
	bi='[1m'
	ui='[4m'
	ao='[0m'
fi

upper() {
	echo :"$@" | sed 's/^://' | tr $alll $allu
}

# clean up after ac_testrun()
ac_testdone() {
	eval HAVE_$fu=$fv
	fr=no
	test 0 = $fv || fr=yes
	$e "$bi==> $fd...$ao $ui$fr$ao$fx"
	fx=
}

# ac_cache label: sets f, fu, fv?=0
ac_cache() {
	f=$1
	fu=`upper $f`
	eval fv=\$HAVE_$fu
	case $fv in
	0|1)
		fx=' (cached)'
		return 0
		;;
	esac
	fv=0
	return 1
}

# ac_testinit label [!] checkif[!]0 [setlabelifcheckis[!]0] useroutput
# returns 1 if value was cached/implied, 0 otherwise: call ac_testdone
ac_testinit() {
	if ac_cache $1; then
		test x"$2" = x"!" && shift
		test x"$2" = x"" || shift
		fd=${3-$f}
		ac_testdone
		return 1
	fi
	fc=0
	if test x"$2" = x""; then
		ft=1
	else
		if test x"$2" = x"!"; then
			fc=1
			shift
		fi
		eval ft=\$HAVE_`upper $2`
		shift
	fi
	fd=${3-$f}
	if test $fc = "$ft"; then
		fv=$2
		fx=' (implied)'
		ac_testdone
		return 1
	fi
	$e ... $fd
	return 0
}

# pipe .c | ac_test[n] [!] label [!] checkif[!]0 [setlabelifcheckis[!]0] useroutput
ac_testnnd() {
	if test x"$1" = x"!"; then
		fr=1
		shift
	else
		fr=0
	fi
	ac_testinit "$@" || return 1
	cat >conftest.c
	vv ']' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN conftest.c $LIBS $ccpr"
	test $tcfn = no && test -f a.out && tcfn=a.out
	test $tcfn = no && test -f a.exe && tcfn=a.exe
	test $tcfn = no && test -f conftest.exe && tcfn=conftest.exe
	test $tcfn = no && test -f conftest && tcfn=conftest
	if test -f $tcfn; then
		test 1 = $fr || fv=1
	else
		test 0 = $fr || fv=1
	fi
	vscan=
	if test $phase = u; then
		test $ct = gcc && vscan='unrecogni[sz]ed'
		test $ct = hpcc && vscan='unsupported'
		test $ct = pcc && vscan='unsupported'
		test $ct = sunpro && vscan='-e ignored -e turned.off'
	fi
	test -n "$vscan" && grep $vscan vv.out >/dev/null 2>&1 && fv=$fr
	return 0
}
ac_testn() {
	ac_testnnd "$@" || return
	rmf conftest.c conftest.o ${tcfn}* vv.out
	ac_testdone
}

# ac_ifcpp cppexpr [!] label [!] checkif[!]0 [setlabelifcheckis[!]0] useroutput
ac_ifcpp() {
	expr=$1; shift
	ac_testn "$@" <<-EOF
		#include <unistd.h>
		extern int thiswillneverbedefinedIhope(void);
		int main(void) { return (dup(0) +
		#$expr
		    0
		#else
		/* force a failure: expr is false */
		    thiswillneverbedefinedIhope()
		#endif
		    ); }
EOF
	test x"$1" = x"!" && shift
	f=$1
	fu=`upper $f`
	eval fv=\$HAVE_$fu
	test x"$fv" = x"1"
}

add_cppflags() {
	CPPFLAGS="$CPPFLAGS $*"
}

ac_cppflags() {
	test x"$1" = x"" || fu=$1
	fv=$2
	test x"$2" = x"" && eval fv=\$HAVE_$fu
	add_cppflags -DHAVE_$fu=$fv
}

ac_test() {
	ac_testn "$@"
	ac_cppflags
}

# ac_flags [-] add varname cflags [text] [ldflags]
ac_flags() {
	if test x"$1" = x"-"; then
		shift
		hf=1
	else
		hf=0
	fi
	fa=$1
	vn=$2
	f=$3
	ft=$4
	fl=$5
	test x"$ft" = x"" && ft="if $f can be used"
	save_CFLAGS=$CFLAGS
	CFLAGS="$CFLAGS $f"
	if test -n "$fl"; then
		save_LDFLAGS=$LDFLAGS
		LDFLAGS="$LDFLAGS $fl"
	fi
	if test 1 = $hf; then
		ac_testn can_$vn '' "$ft"
	else
		ac_testn can_$vn '' "$ft" <<-'EOF'
			/* evil apo'stroph in comment test */
			#include <unistd.h>
			int main(void) { return (dup(0)); }
		EOF
		#'
	fi
	eval fv=\$HAVE_CAN_`upper $vn`
	if test -n "$fl"; then
		test 11 = $fa$fv || LDFLAGS=$save_LDFLAGS
	fi
	test 11 = $fa$fv || CFLAGS=$save_CFLAGS
}

# ac_header [!] header [prereq ...]
ac_header() {
	if test x"$1" = x"!"; then
		na=1
		shift
	else
		na=0
	fi
	hf=$1; shift
	hv=`echo "$hf" | tr -d "$lfcr" | tr -c $alll$allu$alln $alls`
	echo "/* NeXTstep bug workaround */" >x
	for i
	do
		case $i in
		_time)
			echo '#if HAVE_BOTH_TIME_H' >>x
			echo '#include <sys/time.h>' >>x
			echo '#include <time.h>' >>x
			echo '#elif HAVE_SYS_TIME_H' >>x
			echo '#include <sys/time.h>' >>x
			echo '#elif HAVE_TIME_H' >>x
			echo '#include <time.h>' >>x
			echo '#endif' >>x
			;;
		*)
			echo "#include <$i>" >>x
			;;
		esac
	done
	echo "#include <$hf>" >>x
	echo '#include <unistd.h>' >>x
	echo 'int main(void) { return (dup(0)); }' >>x
	ac_testn "$hv" "" "<$hf>" <x
	rmf x
	test 1 = $na || ac_cppflags
}


curdir=`pwd` srcdir=`dirname "$0" 2>/dev/null`
case x$srcdir in
x)
	srcdir=.
	;;
*\ *|*"	"*|*"$nl"*)
	echo >&2 Source directory should not contain space or tab or newline.
	echo >&2 Errors may occur.
	;;
*"'"*)
	echo Source directory must not contain single quotes.
	exit 1
	;;
esac
CFLAGS="-DMCCF $CFLAGS"
add_cppflags '-Dmgp_version='\''"$(mgpversion)"'\'' -DMCPPF'

e=echo
eq=0
last=
pkgs='imlib2 libbsd-overlay xft'

for i
do
	case $last:$i in
	P:*)
		pkgs=$i
		last=
		;;
	:-g)
		# checker, debug, valgrind build
		add_cppflags -DDEBUG
		CFLAGS="$CFLAGS -g3 -fno-builtin"
		;;
	:-P)
		last=P
		;;
	:-Q)
		eq=1
		;;
	:-v)
		echo "Build.sh for MagicPoint $srcversion"
		exit 0
		;;
	:*)
		echo "$me: Unknown option '$i'!" >&2
		exit 1
		;;
	*)
		echo "$me: Unknown option -'$last' '$i'!" >&2
		exit 1
		;;
	esac
done

tfn=mgp
if test -d $tfn || test -d $tfn.exe; then
	echo "$me: Error: ./$tfn is a directory!" >&2
	exit 1
fi
rmf a.exe* a.out* conftest.c conftest.exe* *core core.* ${tfn}* *.bc *.dbg \
    *.ll *.o lft no x vv.out *~

if test -n "$CPPFLAGS"; then
	CPPFLAGS="-I. $CPPFLAGS"
else
	CPPFLAGS="-I."
fi
test -n "$LDSTATIC" && if test -n "$LDFLAGS"; then
	LDFLAGS="$LDFLAGS $LDSTATIC"
else
	LDFLAGS=$LDSTATIC
fi

if test -z "$TARGET_OS"; then
	x=`uname -s 2>/dev/null || uname`
	test x"$x" = x"`uname -n 2>/dev/null`" || TARGET_OS=$x
fi
if test -z "$TARGET_OS"; then
	echo "$me: Set TARGET_OS, your uname is broken!" >&2
	exit 1
fi
oswarn=
ccpc=-Wc,
ccpl=-Wl,
tsts=
ccpr='|| for _f in ${tcfn}*; do case $_f in mgp.1|mgp.c|mgp.h) ;; *) rm -f "$_f" ;; esac; done'

# Evil hack
if test x"$TARGET_OS" = x"Android"; then
	TARGET_OS=Linux
fi

# Evil OS
if test x"$TARGET_OS" = x"Minix"; then
	echo >&2 "
WARNING: additional checks before running Build.sh required!
You can avoid these by calling Build.sh correctly, see below.
"
	cat >conftest.c <<'EOF'
#include <sys/types.h>
const char *
#ifdef _NETBSD_SOURCE
ct="Ninix3"
#else
ct="Minix3"
#endif
;
EOF
	ct=unknown
	vv ']' "${CC-cc} -E $CFLAGS $CPPFLAGS $NOWARN conftest.c | grep ct= | tr -d \\\\015 >x"
	sed 's/^/[ /' x
	eval `cat x`
	rmf x vv.out
	case $ct in
	Minix3|Ninix3)
		echo >&2 "
Warning: you set TARGET_OS to $TARGET_OS but that is ambiguous.
Please set it to either Minix3 or Ninix3, whereas the latter is
all versions of Minix with even partial NetBSD(R) userland. The
value determined from your compiler for the current compilation
(which may be wrong) is: $ct
"
		TARGET_OS=$ct
		;;
	*)
		echo >&2 "
Warning: you set TARGET_OS to $TARGET_OS but that is ambiguous.
Please set it to either Minix3 or Ninix3, whereas the latter is
all versions of Minix with even partial NetBSD(R) userland. The
proper value couldn't be determined, continue at your own risk.
"
		;;
	esac
fi

# Configuration depending on OS revision, on OSes that need them
case $TARGET_OS in
SCO_SV)
	test x"$TARGET_OSREV" = x"" && TARGET_OSREV=`uname -r`
	;;
esac

# Configuration depending on OS name
case $TARGET_OS in
AIX)
	oswarn="; it is untested"
	add_cppflags -D_ALL_SOURCE
	;;
Darwin)
	oswarn="; it is untested"
	add_cppflags -D_DARWIN_C_SOURCE
	;;
FreeMiNT)
	oswarn="; it is untested"
	add_cppflags -D_GNU_SOURCE
	;;
GNU)
	oswarn="; it is untested"
	case $CC in
	*tendracc*) ;;
	*) add_cppflags -D_GNU_SOURCE ;;
	esac
	;;
GNU/kFreeBSD)
	oswarn="; it is untested"
	case $CC in
	*tendracc*) ;;
	*) add_cppflags -D_GNU_SOURCE ;;
	esac
	;;
Interix)
	oswarn="; it is untested"
	ccpc='-X '
	ccpl='-Y '
	add_cppflags -D_ALL_SOURCE
	;;
Linux)
	case $CC in
	*tendracc*) ;;
	*) add_cppflags -D_GNU_SOURCE ;;
	esac
	;;
midipix)
	oswarn="; it is untested"
	add_cppflags -D_GNU_SOURCE
	;;
MidnightBSD)
	oswarn="; it is untested"
	add_cppflags -D_WITH_DPRINTF
	add_cppflags -DUT_NAMESIZE=32
	;;
MirBSD)
	add_cppflags -D_ALL_SOURCE
	;;
MSYS_*)
	oswarn="; it is untested"
	# broken on this OE (from ir0nh34d)
	: "${HAVE_STDINT_H=0}"
	;;
NetBSD)
	oswarn="; it is untested"
	add_cppflags -D_NETBSD_SOURCE
	add_cppflags -D_OPENBSD_SOURCE
	;;
NEXTSTEP)
	oswarn="; it is untested"
	add_cppflags -D_NEXT_SOURCE
	add_cppflags -D_POSIX_SOURCE
	: "${CC=cc -posix}"
	;;
OS/390)
	oswarn="; it is untested"
	: "${CC=xlc}"
	add_cppflags -D_ALL_SOURCE
	;;
OSF1)
	oswarn="; it is untested"
	add_cppflags -D_OSF_SOURCE
	add_cppflags -D_POSIX_C_SOURCE=200112L
	add_cppflags -D_XOPEN_SOURCE=600
	add_cppflags -D_XOPEN_SOURCE_EXTENDED
	;;
QNX)
	oswarn="; it is untested"
	add_cppflags -D__NO_EXT_QNX
	add_cppflags -D__EXT_UNIX_MISC
	;;
SunOS)
	oswarn="; it is untested"
	add_cppflags -D_BSD_SOURCE
	add_cppflags -D__EXTENSIONS__
	;;
syllable)
	oswarn="; it is untested"
	add_cppflags -D_GNU_SOURCE
	;;
ULTRIX)
	oswarn="; it is untested"
	: "${CC=cc -YPOSIX}"
	add_cppflags -DMKSH_TYPEDEF_SSIZE_T=int
	;;
UWIN*)
	oswarn="; it is untested"
	ccpc='-Yc,'
	ccpl='-Yl,'
	tsts=" 3<>/dev/tty"
	;;
*)
	oswarn='; it may or may not work'
	test x"$TARGET_OSREV" = x"" && TARGET_OSREV=`uname -r`
	;;
esac

: "${CC=cc}"

# this aids me in tracing FTBFSen without access to the buildd
$e "Hi from$ao ${bi}mgp Build.sh $srcversion$ao on:"
case $TARGET_OS in
AIX)
	vv '|' "oslevel >&2"
	vv '|' "uname -a >&2"
	;;
Darwin)
	vv '|' "hwprefs machine_type os_type os_class >&2"
	vv '|' "sw_vers >&2"
	vv '|' "system_profiler -detailLevel mini SPSoftwareDataType SPHardwareDataType >&2"
	vv '|' "/bin/sh --version >&2"
	vv '|' "xcodebuild -version >&2"
	vv '|' "uname -a >&2"
	vv '|' "sysctl kern.version hw.machine hw.model hw.memsize hw.availcpu hw.ncpu hw.cpufrequency hw.byteorder hw.cpu64bit_capable >&2"
	vv '|' "sysctl hw.cpufrequency hw.byteorder hw.cpu64bit_capable hw.ncpu >&2"
	;;
IRIX*)
	vv '|' "uname -a >&2"
	vv '|' "hinv -v >&2"
	;;
OSF1)
	vv '|' "uname -a >&2"
	vv '|' "/usr/sbin/sizer -v >&2"
	;;
SCO_SV|UnixWare|UNIX_SV)
	vv '|' "uname -a >&2"
	vv '|' "uname -X >&2"
	;;
*)
	vv '|' "uname -a >&2"
	;;
esac
test -z "$oswarn" || echo >&2 "
Warning: MagicPoint has not yet been ported to or tested on your
operating system '$TARGET_OS'$oswarn. If you can provide
a shell account to the developer, this may improve; please
drop us a success or failure notice or even send in diffs,
at the very least, complete logs (Build.sh + make) will help.
"
$e "$bi$me: Building MagicPoint$ao ${ui}mgp $srcversion$ao on $TARGET_OS ${TARGET_OSREV}..."

#
# Start of mirtoconf checks
#
$e $bi$me: Scanning for functions... please ignore any errors.$ao

#
# Compiler: which one?
#
# notes:
# - ICC defines __GNUC__ too
# - GCC defines __hpux too
# - LLVM+clang defines __GNUC__ too
# - nwcc defines __GNUC__ too
CPP="$CC -E"
$e ... which compiler type seems to be used
cat >conftest.c <<'EOF'
const char *
#if defined(__ICC) || defined(__INTEL_COMPILER)
ct="icc"
#elif defined(__xlC__) || defined(__IBMC__)
ct="xlc"
#elif defined(__SUNPRO_C)
ct="sunpro"
#elif defined(__ACK__)
ct="ack"
#elif defined(__BORLANDC__)
ct="bcc"
#elif defined(__WATCOMC__)
ct="watcom"
#elif defined(__MWERKS__)
ct="metrowerks"
#elif defined(__HP_cc)
ct="hpcc"
#elif defined(__DECC) || (defined(__osf__) && !defined(__GNUC__))
ct="dec"
#elif defined(__PGI)
ct="pgi"
#elif defined(__DMC__)
ct="dmc"
#elif defined(_MSC_VER)
ct="msc"
#elif defined(__ADSPBLACKFIN__) || defined(__ADSPTS__) || defined(__ADSP21000__)
ct="adsp"
#elif defined(__IAR_SYSTEMS_ICC__)
ct="iar"
#elif defined(SDCC)
ct="sdcc"
#elif defined(__PCC__)
ct="pcc"
#elif defined(__TenDRA__)
ct="tendra"
#elif defined(__TINYC__)
ct="tcc"
#elif defined(__llvm__) && defined(__clang__)
ct="clang"
#elif defined(__NWCC__)
ct="nwcc"
#elif defined(__GNUC__)
ct="gcc"
#elif defined(_COMPILER_VERSION)
ct="mipspro"
#elif defined(__sgi)
ct="mipspro"
#elif defined(__hpux) || defined(__hpua)
ct="hpcc"
#elif defined(__ultrix)
ct="ucode"
#elif defined(__USLC__)
ct="uslc"
#elif defined(__LCC__)
ct="lcc"
#elif defined(MKSH_MAYBE_KENCC)
/* and none of the above matches */
ct="kencc"
#else
ct="unknown"
#endif
;
const char *
#if defined(__KLIBC__) && !defined(__OS2__)
et="klibc"
#else
et="unknown"
#endif
;
EOF
ct=untested
et=untested
vv ']' "$CPP $CFLAGS $CPPFLAGS $NOWARN conftest.c | \
    sed -n '/^ *[ce]t *= */s/^ *\([ce]t\) *= */\1=/p' | tr -d \\\\015 >x"
sed 's/^/[ /' x
eval `cat x`
rmf x vv.out
cat >conftest.c <<'EOF'
#include <unistd.h>
int main(void) { return (dup(0)); }
EOF
case $ct in
ack)
	# work around "the famous ACK const bug"
	CPPFLAGS="-Dconst= $CPPFLAGS"
	;;
adsp)
	echo >&2 'Warning: Analog Devices C++ compiler for Blackfin, TigerSHARC
    and SHARC (21000) DSPs detected. This compiler has not yet
    been tested for compatibility with mgp. Continue at your
    own risk, please report success/failure to the developers.'
	;;
bcc)
	echo >&2 "Warning: Borland C++ Builder detected. This compiler might
    produce broken executables. Continue at your own risk,
    please report success/failure to the developers."
	;;
clang)
	# does not work with current "ccc" compiler driver
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -version"
	# one of these two works, for now
	vv '|' "${CLANG-clang} -version"
	vv '|' "${CLANG-clang} --version"
	# ensure compiler and linker are in sync unless overridden
	case $CCC_CC:$CCC_LD in
	:*)	;;
	*:)	CCC_LD=$CCC_CC; export CCC_LD ;;
	esac
	;;
dec)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -V"
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -Wl,-V conftest.c $LIBS"
	;;
dmc)
	echo >&2 "Warning: Digital Mars Compiler detected. When running under"
	echo >&2 "    UWIN, mksh tends to be unstable due to the limitations"
	echo >&2 "    of this platform. Continue at your own risk,"
	echo >&2 "    please report success/failure to the developers."
	;;
gcc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -v conftest.c $LIBS"
	vv '|' 'eval echo "\`$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -dumpmachine\`" \
		 "gcc\`$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -dumpversion\`"'
	;;
hpcc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -V conftest.c $LIBS"
	;;
iar)
	echo >&2 'Warning: IAR Systems (http://www.iar.com) compiler for embedded
    systems detected. This unsupported compiler has not yet
    been tested for compatibility with mgp. Continue at your
    own risk, please report success/failure to the developers.'
	;;
icc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -V"
	;;
kencc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -v conftest.c $LIBS"
	;;
lcc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -v conftest.c $LIBS"
	add_cppflags -D__inline__=__inline
	;;
metrowerks)
	echo >&2 'Warning: Metrowerks C compiler detected. This has not yet
    been tested for compatibility with mgp. Continue at your
    own risk, please report success/failure to the developers.'
	;;
mipspro)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -version"
	;;
msc)
	ccpr=		# errorlevels are not reliable
	case $TARGET_OS in
	Interix)
		if [[ -n $C89_COMPILER ]]; then
			C89_COMPILER=`ntpath2posix -c "$C89_COMPILER"`
		else
			C89_COMPILER=CL.EXE
		fi
		if [[ -n $C89_LINKER ]]; then
			C89_LINKER=`ntpath2posix -c "$C89_LINKER"`
		else
			C89_LINKER=LINK.EXE
		fi
		vv '|' "$C89_COMPILER /HELP >&2"
		vv '|' "$C89_LINKER /LINK >&2"
		;;
	esac
	;;
nwcc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -version"
	;;
pcc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -v"
	;;
pgi)
	echo >&2 'Warning: PGI detected. This unknown compiler has not yet
    been tested for compatibility with mgp. Continue at your
    own risk, please report success/failure to the developers.'
	;;
sdcc)
	echo >&2 'Warning: sdcc (http://sdcc.sourceforge.net), the small devices
    C compiler for embedded systems detected. This has not yet
    been tested for compatibility with mgp. Continue at your
    own risk, please report success/failure to the developers.'
	;;
sunpro)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -V conftest.c $LIBS"
	;;
tcc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -v"
	;;
tendra)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -V 2>&1 | \
	    grep -F -i -e version -e release"
	;;
ucode)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -V"
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -Wl,-V conftest.c $LIBS"
	;;
uslc)
	case $TARGET_OS:$TARGET_OSREV in
	SCO_SV:3.2*)
		# SCO OpenServer 5
		CFLAGS="$CFLAGS -g"
		: "${HAVE_CAN_OTWO=0}${HAVE_CAN_OPTIMISE=0}"
		;;
	esac
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -V conftest.c $LIBS"
	;;
watcom)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -v conftest.c $LIBS"
	;;
xlc)
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -qversion"
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN $LIBS -qversion=verbose"
	vv '|' "ld -V"
	;;
*)
	test x"$ct" = x"untested" && $e "!!! detecting preprocessor failed"
	ct=unknown
	vv "$CC --version"
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -v conftest.c $LIBS"
	vv '|' "$CC $CFLAGS $CPPFLAGS $LDFLAGS $NOWARN -V conftest.c $LIBS"
	;;
esac
etd=" on $et"
case $et in
klibc)
	;;
unknown)
	# nothing special detected, don’t worry
	etd=
	;;
*)
	# huh?
	;;
esac
$e "$bi==> which compiler type seems to be used...$ao $ui$ct$etd$ao"
rmf conftest.c conftest.o conftest a.out* a.exe* conftest.exe* vv.out

#
# Compiler: works as-is, with -Wno-error and -Werror
#
save_NOWARN=$NOWARN
NOWARN=
DOWARN=
ac_flags 0 compiler_works '' 'if the compiler works'
test 1 = $HAVE_CAN_COMPILER_WORKS || exit 1
HAVE_COMPILER_KNOWN=0
test $ct = unknown || HAVE_COMPILER_KNOWN=1
if ac_ifcpp 'if 0' compiler_fails '' \
    'if the compiler does not fail correctly'; then
	save_CFLAGS=$CFLAGS
	: "${HAVE_CAN_DELEXE=x}"
	case $ct in
	dec)
		CFLAGS="$CFLAGS ${ccpl}-non_shared"
		ac_testn can_delexe compiler_fails 0 'for the -non_shared linker option' <<-'EOF'
			#include <unistd.h>
			int main(void) { return (dup(0)); }
		EOF
		;;
	dmc)
		CFLAGS="$CFLAGS ${ccpl}/DELEXECUTABLE"
		ac_testn can_delexe compiler_fails 0 'for the /DELEXECUTABLE linker option' <<-'EOF'
			#include <unistd.h>
			int main(void) { return (dup(0)); }
		EOF
		;;
	*)
		exit 1
		;;
	esac
	test 1 = $HAVE_CAN_DELEXE || CFLAGS=$save_CFLAGS
	ac_ifcpp 'if 0' compiler_still_fails \
	    'if the compiler still does not fail correctly' && exit 1
fi
if ac_ifcpp 'ifdef __TINYC__' couldbe_tcc '!' compiler_known 0 \
    'if this could be tcc'; then
	ct=tcc
	CPP='cpp -D__TINYC__'
	HAVE_COMPILER_KNOWN=1
fi

case $ct in
bcc)
	save_NOWARN="${ccpc}-w"
	DOWARN="${ccpc}-w!"
	;;
dec)
	# -msg_* flags not used yet, or is -w2 correct?
	;;
dmc)
	save_NOWARN="${ccpc}-w"
	DOWARN="${ccpc}-wx"
	;;
hpcc)
	save_NOWARN=
	DOWARN=+We
	;;
kencc)
	save_NOWARN=
	DOWARN=
	;;
mipspro)
	save_NOWARN=
	DOWARN="-diag_error 1-10000"
	;;
msc)
	save_NOWARN="${ccpc}/w"
	DOWARN="${ccpc}/WX"
	;;
sunpro)
	test x"$save_NOWARN" = x"" && save_NOWARN='-errwarn=%none'
	ac_flags 0 errwarnnone "$save_NOWARN"
	test 1 = $HAVE_CAN_ERRWARNNONE || save_NOWARN=
	ac_flags 0 errwarnall "-errwarn=%all"
	test 1 = $HAVE_CAN_ERRWARNALL && DOWARN="-errwarn=%all"
	;;
tendra)
	save_NOWARN=-w
	;;
ucode)
	save_NOWARN=
	DOWARN=-w2
	;;
watcom)
	save_NOWARN=
	DOWARN=-Wc,-we
	;;
xlc)
	case $TARGET_OS in
	OS/390)
		save_NOWARN=-qflag=e
		DOWARN=-qflag=i
		;;
	*)
		save_NOWARN=-qflag=i:e
		DOWARN=-qflag=i:i
		;;
	esac
	;;
*)
	test x"$save_NOWARN" = x"" && save_NOWARN=-Wno-error
	ac_flags 0 wnoerror "$save_NOWARN"
	test 1 = $HAVE_CAN_WNOERROR || save_NOWARN=
	ac_flags 0 werror -Werror
	test 1 = $HAVE_CAN_WERROR && DOWARN=-Werror
	test $ct = icc && DOWARN="$DOWARN -wd1419"
	;;
esac
NOWARN=$save_NOWARN

#
# Compiler: extra flags (-O2 -f* -W* etc.)
#
i=`echo :"$orig_CFLAGS" | sed 's/^://' | tr -c -d $alll$allu$alln`
# optimisation: only if orig_CFLAGS is empty
test x"$i" = x"" && case $ct in
hpcc)
	phase=u
	ac_flags 1 otwo +O2
	phase=x
	;;
kencc|tcc|tendra)
	# no special optimisation
	;;
sunpro)
	cat >x <<-'EOF'
		#include <unistd.h>
		int main(void) { return (dup(0)); }
		#define __IDSTRING_CONCAT(l,p)	__LINTED__ ## l ## _ ## p
		#define __IDSTRING_EXPAND(l,p)	__IDSTRING_CONCAT(l,p)
		#define pad			void __IDSTRING_EXPAND(__LINE__,x)(void) { }
	EOF
	yes pad | head -n 256 >>x
	ac_flags - 1 otwo -xO2 <x
	rmf x
	;;
xlc)
	ac_flags 1 othree "-O3 -qstrict"
	test 1 = $HAVE_CAN_OTHREE || ac_flags 1 otwo -O2
	;;
*)
	ac_flags 1 otwo -O2
	test 1 = $HAVE_CAN_OTWO || ac_flags 1 optimise -O
	;;
esac
# other flags: just add them if they are supported
i=0
case $ct in
bcc)
	ac_flags 1 strpool "${ccpc}-d" 'if string pooling can be enabled'
	;;
clang)
	i=1
	;;
dec)
	ac_flags 0 verb -verbose
	ac_flags 1 rodata -readonly_strings
	;;
dmc)
	ac_flags 1 decl "${ccpc}-r" 'for strict prototype checks'
	ac_flags 1 schk "${ccpc}-s" 'for stack overflow checking'
	;;
gcc)
	# The following tests run with -Werror (gcc only) if possible
	NOWARN=$DOWARN; phase=u
	# MagicPoint is not written in CFrustFrust!
	ac_flags 1 no_eh_frame -fno-asynchronous-unwind-tables
	ac_flags 1 fnostrictaliasing -fno-strict-aliasing
	ac_flags 1 fstackprotectorstrong -fstack-protector-strong
	test 1 = $HAVE_CAN_FSTACKPROTECTORSTRONG || \
	    ac_flags 1 fstackprotectorall -fstack-protector-all
	ac_flags 1 data_abi_align -malign-data=abi
	i=1
	;;
hpcc)
	phase=u
	# probably not needed
	#ac_flags 1 agcc -Agcc 'for support of GCC extensions'
	phase=x
	;;
icc)
	ac_flags 1 fnobuiltinsetmode -fno-builtin-setmode
	ac_flags 1 fnostrictaliasing -fno-strict-aliasing
	ac_flags 1 fstacksecuritycheck -fstack-security-check
	i=1
	;;
mipspro)
	ac_flags 1 fullwarn -fullwarn 'for remark output support'
	;;
msc)
	ac_flags 1 strpool "${ccpc}/GF" 'if string pooling can be enabled'
	echo 'int main(void) { char test[64] = ""; return (*test); }' >x
	ac_flags - 1 stackon "${ccpc}/GZ" 'if stack checks can be enabled' <x
	ac_flags - 1 stckall "${ccpc}/Ge" 'stack checks for all functions' <x
	ac_flags - 1 secuchk "${ccpc}/GS" 'for compiler security checks' <x
	rmf x
	ac_flags 1 wall "${ccpc}/Wall" 'to enable all warnings'
	ac_flags 1 wp64 "${ccpc}/Wp64" 'to enable 64-bit warnings'
	;;
nwcc)
	#broken# ac_flags 1 ssp -stackprotect
	i=1
	;;
pcc)
	ac_flags 1 fstackprotectorall -fstack-protector-all
	i=1
	;;
sunpro)
	phase=u
	ac_flags 1 v -v
	ac_flags 1 ipo -xipo 'for cross-module optimisation'
	phase=x
	;;
tcc)
	: #broken# ac_flags 1 boundschk -b
	;;
tendra)
	ac_flags 0 ysystem -Ysystem
	test 1 = $HAVE_CAN_YSYSTEM && CPPFLAGS="-Ysystem $CPPFLAGS"
	ac_flags 1 extansi -Xa
	;;
xlc)
	case $TARGET_OS in
	OS/390)
		# On IBM z/OS, the following are warnings by default:
		# CCN3296: #include file <foo.h> not found.
		# CCN3944: Attribute "__foo__" is not supported and is ignored.
		# CCN3963: The attribute "foo" is not a valid variable attribute and is ignored.
		ac_flags 1 halton '-qhaltonmsg=CCN3296 -qhaltonmsg=CCN3944 -qhaltonmsg=CCN3963'
		# CCN3290: Unknown macro name FOO on #undef directive.
		# CCN4108: The use of keyword '__attribute__' is non-portable.
		ac_flags 1 supprss '-qsuppress=CCN3290 -qsuppress=CCN4108'
		;;
	*)
		ac_flags 1 rodata '-qro -qroconst -qroptr'
		ac_flags 1 rtcheck -qcheck=all
		#ac_flags 1 rtchkc -qextchk	# reported broken
		ac_flags 1 wformat '-qformat=all -qformat=nozln'
		;;
	esac
	#ac_flags 1 wp64 -qwarn64	# too verbose for now
	;;
esac
# flags common to a subset of compilers (run with -Werror on gcc)
if test 1 = $i; then
	ac_flags 1 wall -Wall
	ac_flags 1 fwrapv -fwrapv
	# keep this sane
	ac_flags 1 wformat -Wformat
	ac_flags 1 wextra -Wextra # temporarily
	ac_flags 1 woldstyledecl -Wold-style-declaration
	ac_flags 1 woldstyledefn -Wold-style-definition
	ac_flags 1 wmissingdecl -Wmissing-declarations
	ac_flags 1 wmissingproto -Wmissing-prototypes
fi

phase=x
# The following tests run with -Werror or similar (all compilers) if possible
NOWARN=$DOWARN
test $ct = pcc && phase=u

#
# Compiler: check for stuff that only generates warnings
#
ac_test attribute_bounded '' 'for __attribute__((__bounded__))' <<-'EOF'
	#if defined(__TenDRA__) || (defined(__GNUC__) && (__GNUC__ < 2))
	extern int thiswillneverbedefinedIhope(void);
	/* force a failure: TenDRA and gcc 1.42 have false positive here */
	int main(void) { return (thiswillneverbedefinedIhope()); }
	#else
	#include <string.h>
	#undef __attribute__
	int xcopy(const void *, void *, size_t)
	    __attribute__((__bounded__(__buffer__, 1, 3)))
	    __attribute__((__bounded__(__buffer__, 2, 3)));
	int main(int ac, char *av[]) { return (xcopy(av[0], av[--ac], 1)); }
	int xcopy(const void *s, void *d, size_t n) {
		/*
		 * if memmove does not exist, we are not on a system
		 * with GCC with __bounded__ attribute either so poo
		 */
		memmove(d, s, n); return ((int)n);
	}
	#endif
EOF
ac_test attribute_format '' 'for __attribute__((__format__))' <<-'EOF'
	#if defined(__TenDRA__) || (defined(__GNUC__) && (__GNUC__ < 2))
	extern int thiswillneverbedefinedIhope(void);
	/* force a failure: TenDRA and gcc 1.42 have false positive here */
	int main(void) { return (thiswillneverbedefinedIhope()); }
	#else
	#define fprintf printfoo
	#include <stdio.h>
	#undef __attribute__
	#undef fprintf
	extern int fprintf(FILE *, const char *format, ...)
	    __attribute__((__format__(__printf__, 2, 3)));
	int main(int ac, char *av[]) { return (fprintf(stderr, "%s%d", *av, ac)); }
	#endif
EOF
ac_test attribute_nonnull '' 'for __attribute__((__nonnull__))' <<-'EOF'
	#if defined(__TenDRA__) || (defined(__GNUC__) && (__GNUC__ < 2))
	extern int thiswillneverbedefinedIhope(void);
	/* force a failure: TenDRA and gcc 1.42 have false positive here */
	int main(void) { return (thiswillneverbedefinedIhope()); }
	#else
	#include <stdio.h>
	#undef __attribute__
	int fnord(const char *) __attribute__((__nonnull__(1)));
	int main(void) { return (fnord("x")); }
	int fnord(const char *x) { return (fputc(*x, stderr)); }
	#endif
EOF
ac_test attribute_noreturn '' 'for __attribute__((__noreturn__))' <<-'EOF'
	#if defined(__TenDRA__) || (defined(__GNUC__) && (__GNUC__ < 2))
	extern int thiswillneverbedefinedIhope(void);
	/* force a failure: TenDRA and gcc 1.42 have false positive here */
	int main(void) { return (thiswillneverbedefinedIhope()); }
	#else
	#include <stdlib.h>
	#undef __attribute__
	void fnord(void) __attribute__((__noreturn__));
	int main(void) { fnord(); }
	void fnord(void) { exit(0); }
	#endif
EOF
ac_test attribute_pure '' 'for __attribute__((__pure__))' <<-'EOF'
	#if defined(__TenDRA__) || (defined(__GNUC__) && (__GNUC__ < 2))
	extern int thiswillneverbedefinedIhope(void);
	/* force a failure: TenDRA and gcc 1.42 have false positive here */
	int main(void) { return (thiswillneverbedefinedIhope()); }
	#else
	#include <unistd.h>
	#undef __attribute__
	int foo(const char *) __attribute__((__pure__));
	int main(int ac, char *av[]) { return (foo(av[ac - 1]) + dup(0)); }
	int foo(const char *s) { return ((int)s[0]); }
	#endif
EOF
ac_test attribute_unused '' 'for __attribute__((__unused__))' <<-'EOF'
	#if defined(__TenDRA__) || (defined(__GNUC__) && (__GNUC__ < 2))
	extern int thiswillneverbedefinedIhope(void);
	/* force a failure: TenDRA and gcc 1.42 have false positive here */
	int main(void) { return (thiswillneverbedefinedIhope()); }
	#else
	#include <unistd.h>
	#undef __attribute__
	int main(int ac __attribute__((__unused__)), char *av[]
	    __attribute__((__unused__))) { return (dup(0)); }
	#endif
EOF
ac_test attribute_used '' 'for __attribute__((__used__))' <<-'EOF'
	#if defined(__TenDRA__) || (defined(__GNUC__) && (__GNUC__ < 2))
	extern int thiswillneverbedefinedIhope(void);
	/* force a failure: TenDRA and gcc 1.42 have false positive here */
	int main(void) { return (thiswillneverbedefinedIhope()); }
	#else
	#include <unistd.h>
	#undef __attribute__
	static const char fnord[] __attribute__((__used__)) = "42";
	int main(void) { return (dup(0)); }
	#endif
EOF

# End of tests run with -Werror
NOWARN=$save_NOWARN
phase=x

add_cppflags -DNeedFunctionPrototypes

#
# Environment: headers
#
ac_header sys/time.h sys/types.h
ac_header time.h sys/types.h
test "11" = "$HAVE_SYS_TIME_H$HAVE_TIME_H" || HAVE_BOTH_TIME_H=0
ac_test both_time_h '' 'whether <sys/time.h> and <time.h> can both be included' <<-'EOF'
	#include <sys/types.h>
	#include <sys/time.h>
	#include <time.h>
	#include <unistd.h>
	int main(void) { struct tm tm; return ((int)sizeof(tm) + dup(0)); }
EOF
ac_header sys/wait.h sys/types.h
ac_header fcntl.h
ac_header iconv.h
ac_header locale.h
ac_header termios.h
ac_header util.h

#
# Environment: definitions
#
echo '#include <sys/types.h>
#include <fts.h>
#include <unistd.h>
/* check that off_t can represent 2^63-1 correctly, thx FSF */
#define LARGE_OFF_T ((((off_t)1 << 31) << 31) - 1 + (((off_t)1 << 31) << 31))
int off_t_is_large[(LARGE_OFF_T % 2147483629 == 721 &&
    LARGE_OFF_T % 2147483647 == 1) ? 1 : -1];
int main(void) { return (dup(0)); }' >lft.c
ac_testn can_lfs '' "for large file support" <lft.c
save_CPPFLAGS=$CPPFLAGS
add_cppflags -D_FILE_OFFSET_BITS=64
ac_testn can_lfs_sus '!' can_lfs 0 "... with -D_FILE_OFFSET_BITS=64" <lft.c
if test 0 = $HAVE_CAN_LFS_SUS; then
	CPPFLAGS=$save_CPPFLAGS
	add_cppflags -D_LARGE_FILES=1
	ac_testn can_lfs_aix '!' can_lfs 0 "... with -D_LARGE_FILES=1" <lft.c
	test 1 = $HAVE_CAN_LFS_AIX || CPPFLAGS=$save_CPPFLAGS
fi
rm -f lft.c
rmf lft*	# end of large file support test

PK_DEFS=
PK_LIBS=
for pkg in $pkgs; do
	$e Running pkg-config for $pkg...
	PK_DEFS="$PK_DEFS `pkg-config --cflags $pkg`"
	PK_LIBS="$PK_LIBS `pkg-config --libs $pkg`"
done

#
# Environment: types
#
ac_test can_uchar '' "for u_char" <<-'EOF'
	#include <sys/types.h>
	#include <stddef.h>
	int main(int ac, char *av[]) { return ((u_char)(unsigned char)av[ac - 1][0]); }
EOF
ac_test can_ushort '' "for u_short" <<-'EOF'
	#include <sys/types.h>
	#include <stddef.h>
	int main(int ac, char *av[]) { return ((u_short)(unsigned char)av[ac - 1][0]); }
EOF
ac_test can_uint '' "for u_int" <<-'EOF'
	#include <sys/types.h>
	#include <stddef.h>
	int main(int ac, char *av[]) { return ((u_int)(unsigned char)av[ac - 1][0]); }
EOF
ac_test can_ulong '' "for u_long" <<-'EOF'
	#include <sys/types.h>
	#include <stddef.h>
	int main(int ac, char *av[]) { return ((u_long)(size_t)av[ac]); }
EOF

#
# check whether the final link ought to succeed
#
HAVE_LINK_WORKS=x
save_CPPFLAGS=$CPPFLAGS
save_LIBS=$LIBS
CPPFLAGS="-I$srcdir $CPPFLAGS $PK_DEFS -DBUILDSH_LINKTEST"
LIBS="$LIBS $PK_LIBS"
ac_testinit link_works '' 'checking if the final link command may succeed'
fv=1
cat >conftest.c <<-EOF
	#include "mgp.h"
	#if HAVE_LOCALE_H
	#include <locale.h>
	#endif
	#include <fcntl.h>
	#ifdef TTY_KEYINPUT
	#include <termios.h>
	#endif

	int main(void) { printf("Hello, World!\\n"); return (dup(0)); }
EOF
v "$CC $CFLAGS $CPPFLAGS $NOWARN -c conftest.c" || fv=0
test $fv = 0 || v "$CC $CFLAGS $LDFLAGS -o $tcfn conftest.o $LIBS $ccpr"
test -f $tcfn || fv=0
ac_testdone
test $fv = 1 || exit 1
LIBS=$save_LIBS
CPPFLAGS=$save_CPPFLAGS

#
# Environment: library functions
#
cat >lft.c <<-'EOF'
	#if HAVE_ICONV_H
	#include <iconv.h>
	#endif
	#include <stdio.h>
	#include <string.h>
	int main(int ac, char *av[]) {
		iconv_t cd;
		char *srcp = av[1], *dstp = av[2];
		size_t ilen = strlen(srcp), olen = ac;

		if ((cd = iconv_open("dst", "src")) == (iconv_t)-1)
			return (2);
		if (iconv(cd, &srcp, &ilen, &dstp, &olen) == (size_t)-1)
			return (3);
		return (iconv_close(cd) != 0);
	}
EOF
ac_testn iconv '' 'iconv (no extra libraries)' <lft.c
iconv_L=
if test 0 = $HAVE_ICONV; then
	save_LIBS=$LIBS
	LIBS="$LIBS -liconv"
	HAVE_ICONV=x
	ac_testn iconv '' 'iconv (with libiconv)' <lft.c
	if test 1 = $HAVE_ICONV; then
		iconv_L=-liconv
	fi
	LIBS=$save_LIBS
fi
ac_cppflags
rm -f lft.c
rmf lft*

ac_test setlocale_ctype locale_h 0 'setlocale(LC_CTYPE, "")' <<-'EOF'
	#include <locale.h>
	#include <stddef.h>
	int main(void) { return ((int)(size_t)(void *)setlocale(LC_CTYPE, "")); }
EOF

ac_test setreuid '' 'for setreuid and setregid' <<-'EOF'
	#include <sys/types.h>
	#include <unistd.h>
	int main(void) { return (setreuid(0,0) + setregid(0,0)); }
EOF

save_LIBS=$LIBS
LIBS="$LIBS -lutil"
ac_test uu_lock <<-'EOF'
	#include <sys/types.h>
	#if HAVE_UTIL_H
	#include <util.h>
	#endif
	int main(int ac, char *av[]) { return (uu_lock(av[ac - 1])); }
EOF
LIBS=$save_LIBS
if test 0 = $HAVE_UU_LOCK; then
	uulock_L=
	uulock_O=uucplock.o
else
	uulock_L=-lutil
	uulock_O=
fi

#
# check headers for declarations
#

#
# other checks
#

#
# End of mirtoconf checks
#
$e ... done.
rmf vv.out

test 1 = "$HAVE_CAN_VERB" && CFLAGS="$CFLAGS -verbose"

$e $bi$me: Finished configuration testing, now producing output.$ao

case $tcfn in
a.exe|conftest.exe)
	suf=.exe
	;;
*)
	suf=
	;;
esac
rm -f Makefile
cat >Makefile~ <<\EOF
# Makefile for building MagicPoint
# generated, do not edit; re-run Build.sh if necessary!

# feel free to override those

# used for building
AR=		ar
AR_CREATE=	$(AR) cq
LEX=		flex
RANLIB=		ranlib
SH=		sh
YACC=		yacc

# used for installing
LINKMANPAGE=	ln -s
PERL=		/usr/bin/env perl
DESTDIR=
PREFIX=		/usr
SYSCONFDIR=	/etc
BINDIR=		$(PREFIX)/bin
MANDIR=		$(PREFIX)/share/man
DOCDIR=		$(PREFIX)/share/doc/mgp
EMACSDIR=	$(PREFIX)/share/emacs/site-lisp/mgp
MGPELDIR=	$(DOCDIR)/contrib/mgp-el
#SAMPLEDIR=	$(DOCDIR)/examples
SAMPLEDIR=	$(PREFIX)/share/examples/mgp
RCDIR=		$(SYSCONFDIR)/mgp

# set to empty to not install (Debian)
INST_LICENCE=	LICENCE
INST_RELNOTES=	RELNOTES

# or /usr/ucb/install
INSTALL=	install
INSTALL_COPY=	-c
INSTALL_STRIP=	-s
BINMODE=	555
CONFMODE=	644
NONBINMODE=	444
DIRMODE=	755
INSTALL_DIR=	$(INSTALL) -d -m $(DIRMODE)
INSTALL_PROGRAM=$(INSTALL) $(INSTALL_COPY) $(INSTALL_STRIP) -m $(BINMODE)
INSTALL_SCRIPT=	$(INSTALL) $(INSTALL_COPY) -m $(BINMODE)
INSTALL_DATA=	$(INSTALL) $(INSTALL_COPY) -m $(NONBINMODE)
INSTALL_CONF=	$(INSTALL) $(INSTALL_COPY) -m $(CONFMODE)

# do NOT override these, re-run Build.sh if necessary!
EOF
sed 's/#/\\&/g' >>Makefile~ <<EOF
CC=		$CC
CPPFLAGS=	$CPPFLAGS
CFLAGS=		$CFLAGS
LDFLAGS=	$LDFLAGS
LIBS=		$LIBS
SRCDIR=		$srcdir
PK_DEFS=	$PK_DEFS
PK_LIBS=	$PK_LIBS
iconv_L=	$iconv_L
uulock_L=	$uulock_L
uulock_O=	$uulock_O

E=		$suf
mgpversion=	$srcversion
EOF
cat >>Makefile~ <<\EOF

# internals
MAN1=		$(SRCDIR)/contrib/xmindpath/xmindpath.1 \
		$(SRCDIR)/contrib/xwintoppm/xwintoppm.1 \
		$(SRCDIR)/contrib/eqn2eps.1 \
		$(SRCDIR)/contrib/mgp2html.1 \
		$(SRCDIR)/contrib/mgpnet.1 \
		$(SRCDIR)/mgp.1 $(SRCDIR)/mgp2ps.1
MAN1LINKS=	'eqn2eps.1 latex2eps.1' \
		'eqn2eps.1 tex2eps.1' \
		'mgp2html.1 mgp2latex.1'

xmp_DEFS=	-I$(SRCDIR)/contrib/xmindpath
xmp_OBJS=	main.o $(uulock_O)
xmp_LIBS=	-lXtst -lX11 $(uulock_L)

xwp_DEFS=	-I$(SRCDIR)/contrib/xwintoppm
xwp_OBJS=	dsimple.o list.o multiVis.o xwintoppm.o
xwp_LIBS=	-lXmu -lXt -lX11

mgp_DEFS=	-I$(SRCDIR) -DMGPLIBDIR=\"$(RCDIR)\" $(PK_DEFS)
mgp_LIBS=	-L. -lmgpcommon $(PK_LIBS) $(iconv_L) -lX11 -lm

lib_DEFS=	$(mgp_DEFS) -I$(SRCDIR)/image
lib_OBJS=	ctlwords.o \
		dither.o halftone.o imagetypes.o imlib_loader.o \
		misc.o new.o path.o reduce.o send.o value.o zoom.o \
		background.o draw.o globals.o y.tab.o parse.o \
		plist.o postscript.o lex.yy.o unimap.o

mgp_OBJS=	mgp.o x11.o
mps_OBJS=	print.o x11dummy.o

all: mgp$E mgp2ps$E xmindpath$E xwintoppm$E mgp2html mgp2latex mgpnet

# also builds ctlwords.h and ctlwords.pl
ctlwords.c: $(SRCDIR)/ctlwords.sh
	$(SH) $(SRCDIR)/ctlwords.sh

mgp2html: ctlwords.c $(SRCDIR)/contrib/mgp2html.pl.in
	echo '#!$(PERL)' | cat >$@ - ctlwords.pl \
	    $(SRCDIR)/contrib/mgp2html.pl.in

mgp2latex: ctlwords.c $(SRCDIR)/contrib/mgp2latex.pl.in
	echo '#!$(PERL)' | cat >$@ - ctlwords.pl \
	    $(SRCDIR)/contrib/mgp2latex.pl.in

mgpnet: ctlwords.c $(SRCDIR)/contrib/mgpnet.in
	echo '#!$(PERL)' | cat >$@ - ctlwords.pl \
	    $(SRCDIR)/contrib/mgpnet.in

xmindpath$E: $(xmp_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $(xmp_OBJS) $(LIBS) $(xmp_LIBS)

xwintoppm$E: $(xwp_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $(xwp_OBJS) $(LIBS) $(xwp_LIBS)

mgp$E: $(mgp_OBJS) libmgpcommon.a
	$(CC) $(CFLAGS) $(mgp_DEFS) $(LDFLAGS) -o $@ \
	    $(mgp_OBJS) $(LIBS) $(mgp_LIBS)

mgp2ps$E: $(mps_OBJS) libmgpcommon.a
	$(CC) $(CFLAGS) $(mgp_DEFS) $(LDFLAGS) -o $@ \
	    $(mps_OBJS) $(LIBS) $(mgp_LIBS)

libmgpcommon.a: $(lib_OBJS)
	@rm -f $@
	$(AR_CREATE) $@ $(lib_OBJS)
	$(RANLIB) $@

# also builds y.tab.h
y.tab.c: $(SRCDIR)/grammar.y
	$(YACC) -d $(SRCDIR)/grammar.y

lex.yy.c: $(SRCDIR)/scanner.l
	$(LEX) $(SRCDIR)/scanner.l

EOF
dofile() {
	dstbn=`basename "$1" | sed 's/\.[^.]*$//'`
	srcf=
	srca=
	while test -n "$1"; do
		if test -f "$srcdir/$1"; then
			f='$(SRCDIR)/'"$1"
		else
			f=$1
		fi
		shift
		if test -n "$srcf"; then
			srca="$srca $f"
		else
			srca=$f
			srcf=$f
		fi
	done
	cat >>Makefile~ <<EOF
$dstbn.o: $srca
	\$(CC) \$(CPPFLAGS) \$(CFLAGS) $defs -c $srcf

EOF
}
xlih="image/xloadimage.h image/image.h"
mgph="mgp.h $xlih ctlwords.c" # ctlwords.h but that breaks parallel gmake
defs='$(xmp_DEFS)'
dofile contrib/xmindpath/main.c
dofile contrib/xmindpath/uucplock.c contrib/xmindpath/pathnames.h
defs='$(xwp_DEFS)'
dofile contrib/xwintoppm/dsimple.c contrib/xwintoppm/dsimple.h
dofile contrib/xwintoppm/list.c contrib/xwintoppm/list.h
dofile contrib/xwintoppm/multiVis.c contrib/xwintoppm/list.h \
    contrib/xwintoppm/wsutils.h contrib/xwintoppm/multiVis.h
dofile contrib/xwintoppm/xwintoppm.c contrib/xwintoppm/dsimple.h \
    contrib/xwintoppm/list.h \
    contrib/xwintoppm/wsutils.h contrib/xwintoppm/multiVis.h
defs='$(mgp_DEFS)'
dofile ctlwords.c
defs='$(lib_DEFS)'
dofile image/dither.c image/image.h
dofile image/halftone.c image/image.h
dofile image/imagetypes.c $xlih
dofile image/imlib_loader.c $xlih
dofile image/misc.c image/image.h
dofile image/new.c image/image.h
dofile image/path.c $xlih
dofile image/reduce.c image/image.h
dofile image/send.c $mgph
dofile image/value.c image/image.h
dofile image/zoom.c image/image.h
defs='$(mgp_DEFS)'
dofile background.c $mgph
dofile draw.c $mgph
dofile globals.c $mgph
dofile y.tab.c $mgph
dofile parse.c $mgph
dofile plist.c $mgph
dofile postscript.c $mgph
dofile lex.yy.c $mgph y.tab.c # y.tab.h but that breaks parallel gmake
dofile unimap.c $mgph
dofile mgp.c $mgph
dofile x11.c $mgph
dofile print.c $mgph
dofile x11dummy.c $mgph
cat >>Makefile~ <<\EOF
INSTALL_SYSCONFIG=install-sysconfig
install: install-program $(INSTALL_SYSCONFIG)
install-program:
	$(INSTALL_DIR) $(DESTDIR)$(BINDIR)
	$(INSTALL_PROGRAM) mgp $(DESTDIR)$(BINDIR)/
	$(INSTALL_PROGRAM) mgp2ps $(DESTDIR)$(BINDIR)/
	$(INSTALL_PROGRAM) xmindpath $(DESTDIR)$(BINDIR)/
	$(INSTALL_PROGRAM) xwintoppm $(DESTDIR)$(BINDIR)/
	$(INSTALL_SCRIPT) $(SRCDIR)/contrib/eqn2eps.sh \
	    $(DESTDIR)$(BINDIR)/eqn2eps
	$(INSTALL_SCRIPT) $(SRCDIR)/contrib/latex2eps.sh \
	    $(DESTDIR)$(BINDIR)/latex2eps
	$(INSTALL_SCRIPT) mgp2html $(DESTDIR)$(BINDIR)/
	$(INSTALL_SCRIPT) mgp2latex $(DESTDIR)$(BINDIR)/
	$(INSTALL_SCRIPT) mgpnet $(DESTDIR)$(BINDIR)/
	$(INSTALL_SCRIPT) $(SRCDIR)/contrib/tex2eps.sh \
	    $(DESTDIR)$(BINDIR)/tex2eps
	$(INSTALL_DIR) $(DESTDIR)$(MANDIR)/man1
	$(INSTALL_DATA) $(MAN1) $(DESTDIR)$(MANDIR)/man1/
	cd $(DESTDIR)$(MANDIR)/man1/ && for link in $(MAN1LINKS); do \
		$(LINKMANPAGE) $$link || exit 1; \
	done
	$(INSTALL_DIR) $(DESTDIR)$(EMACSDIR)
	cd $(SRCDIR)/contrib && $(INSTALL_DATA) \
	    mgp-el/mgp.el mgp-mode-cd.el mgp-mode.el mgp-mode20.el \
	    $(DESTDIR)$(EMACSDIR)/
	$(INSTALL_DIR) $(DESTDIR)$(MGPELDIR)
	cd $(SRCDIR)/contrib/mgp-el && $(INSTALL_DATA) \
	    README mgp.sty $(DESTDIR)$(MGPELDIR)/
	$(INSTALL_DIR) $(DESTDIR)$(DOCDIR)
	cd $(SRCDIR) && $(INSTALL_DATA) FAQ README README.fonts README.lang \
	    README.xft SYNTAX TODO.jp USAGE USAGE.jp $(INST_LICENCE) \
	    $(INST_RELNOTES) $(DESTDIR)$(DOCDIR)/
	$(INSTALL_DIR) $(DESTDIR)$(SAMPLEDIR)
	cd $(SRCDIR)/sample && $(INSTALL_DATA) \
	    README README.jp cloud.jpg dad.eps dad.jpg default.mgp \
	    gradation-jp.mgp gradation.mgp mgp-old1.jpg mgp-old2.jpg \
	    mgp-old3.jpg mgp-print6 mgp1.jpg mgp2.jpg mgp3.jpg mgp3.xbm \
	    multilingual.mgp sample-fr.mgp sample-jp.mgp sample.mgp \
	    sendmail6-jp.mgp sendmail6.mgp tutorial-jp.mgp tutorial.mgp \
	    utf8test.mgp v6-jp.mgp v6.mgp v6header.* \
	    $(DESTDIR)$(SAMPLEDIR)/

install-sysconfig:
	$(INSTALL_DIR) $(DESTDIR)$(RCDIR)
	$(INSTALL_CONF) $(SRCDIR)/sample/default.mgp $(DESTDIR)$(RCDIR)/

clean:
	rm -f *~ *.a *.o ctlwords.c ctlwords.h ctlwords.pl lex.yy.c y.tab.* \
	    mgp$E mgp2ps$E xmindpath$E xwintoppm$E mgp2html mgp2latex mgpnet \
	    conftest.c
EOF
mv Makefile~ Makefile || exit 1
$e
$e Generated Makefile successfully.
echo >&2 '
You can now use make(1) to compile MagicPoint.
'
exit 0

: <<'EOD'

=== Flags ===

-P list: set list of pkg-config query targets,
	default: imlib2 libbsd-overlay xft

=== Environment used ===

==== Makefile variables ====
AR_CREATE			default: $(AR) cq; AR=ar
RANLIB				default: ranlib; could be :
LEX				default: flex
YACC				default: yacc
SH				POSIX shell to use during build
DESTDIR				prefix to use while installing
PERL				written to shebang
PREFIX				default: /usr
SYSCONFDIR			default: /etc
INSTALL_STRIP			default: -s
mgpversion			may be overridden with package version

==== build environment ====
CC				default: cc
CFLAGS				if empty, defaults to -xO2 or +O2
				or -O3 -qstrict or -O2, per compiler
CPPFLAGS			default empty
LDFLAGS				default empty; added before sources
LDSTATIC			set this to '-static'; default unset
LIBS				default empty; added after sources
NOWARN				-Wno-error or similar
TARGET_OS			default: $(uname -s || uname)
TARGET_OSREV			[SCO] default: $(uname -r)

===== general format =====
HAVE_STRLEN			ac_test
HAVE_STRING_H			ac_header
HAVE_CAN_FSTACKPROTECTORALL	ac_flags

==== cpp definitions ====
MKSH_TYPEDEF_SSIZE_T		define to e.g. 'long' if your OS has no ssize_t

=== generic installation instructions ===

Set CC and possibly CFLAGS, CPPFLAGS, LDFLAGS, LIBS. If cross-compiling,
also set TARGET_OS. To disable tests, set e.g. HAVE_STRLCPY=0; to enable
them, set to a value other than 0 or 1 (e.g. to x).

Normally, the following command is what you want to run, then:
$ (sh Build.sh && make && sudo make install) 2>&1 | tee log

EOD
