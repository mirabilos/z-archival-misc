#!/bin/mksh
# -*- mode: sh -*-
#-
# Copyright © 2008, 2018
#	mirabilos <m@mirbsd.org>
#
# Provided that these terms and disclaimer and all copyright notices
# are retained or reproduced in an accompanying document, permission
# is granted to deal in this work without restriction, including un‐
# limited rights to use, publicly perform, distribute, sell, modify,
# merge, give away, or sublicence.
#
# This work is provided “AS IS” and WITHOUT WARRANTY of any kind, to
# the utmost extent permitted by applicable law, neither express nor
# implied; without malicious intent or gross negligence. In no event
# may a licensor, author or contributor be held liable for indirect,
# direct, other damage, loss, or other issues arising in any way out
# of dealing in the work, even if advised of the possibility of such
# damage or existence of a defect, except proven that it results out
# of said person’s immediate fault when using the work as intended.
#-
# Produces frequencies and cent deltas for all MIDI notes, MML notes
# marked, given a config file tune-*.dat and the pitch standard, for
# instance “A4=440”. Example call:
#	mksh tuner.sh -r 2 -n C4 -f 256 -c tune-kirnberger3.dat
#
# When you want to tune for one pitch standard but output cent delta
# values for another (say you want to tune to C₄=256 Hz but your sy‐
# stem only supports A₄=440 Hz) you can use the +f and +n options to
# specify the output tuning:
#	mksh tuner.sh -n C4 -f 256 +n A4 +f 440 -c tune-young2.dat
#
# Currently, only intonation systems which either do not need to use
# different values for enharmonics (equal temperament, most meantone
# and similar) or where they do not matter (just intonation, but you
# don’t use the off tones) are supported, as we cannot distinguish a
# note frequency from its enharmonic by way of MIDI note numbers. In
# a later version, this may be supported, as both MML and MuseScore,
# or MusicXML, support the original accidentals and key signature.
#
# Notes can be given in either of three formats: as MIDI note number
# (0‥127, for example, 69 is the General MIDI pitch standard A note)
# or as MML octave instruction and note name (O2a is that same note;
# note names are abcdefg followed by + or # for sharp or - for flat)
# or using scientific pitch notation: A₄ (or A4) designates the same
# note as earlier; # or ♯ are sharp, b or ♭ are flat, but the *name*
# of the note is to be given in uppercase.
#
# Configuration files use a line-based format with ‘!’ introducing a
# comment line that is skipped. Other lines are rules as follows:
#
# A line beginning with “DESC” and a space or tab is followed by ar‐
# bitrary (UTF-8) text serving as file description. Whitespace after
# backslash-newline for line concatenation and after DESC is ignored
# and comments cannot be included in a DESC line.
#
# Define a note (here only ABCDEFG#♯b♭ are used) by cent delta rela‐
# tive to its pitch in equal temperament:
#  note + double	! e.g. A + 0
#  note - double	! e.g. B♭ - 1.1
#
# Define a note (as above) by a tempered interval from another given
# note; intervals are automatically detected as major or minor third
# or perfect fifth:
#  note = note [+ [fraction] comma]	! e.g. E = C
#  note = note [- [fraction] comma]	! e.g. G = C - 1/6 P
# “fraction” is written as: “integer / integer”
# “comma” can be one from the following list:
#  P = pythagorean comma (≈ 23.46 ¢) = 531441:524288
#  S = syntonic comma (≈ 21.50629 ¢) = 81:80
#  D = schisma (P - S) (≈ 1.95372 ¢) = 32805:32768
# The ruleset is subject to future extensions if requested by users.
#
# Define a note (as above) by a fractional interval from another gi‐
# ven note; optionally tempered:
#  note = note * fraction [± [fraction] comma]	! e.g. G = C * 3/2
#
# Define a note (as above) by a cent delta from another given note —
# WARNING the RHS note must be higher, by the cent amount given!
#  note = note ^ number [± [fraction] comma]	! e.g. D = C ^ 200.0
#
# In the entire ruleset a note must not have more than 1 definition;
# additional interval rules may be given below a line “VALIDATE” and
# are used to double-check the frequencies resulting from the rules.
# Notes should not be undefined.
#
# For reference here is a mapping between note numbers / names:
#
# MIDI Note 0 = C₋₁ (69 = A₄); 0‥127 (C₋₁‥G₉)
# MML O2a = A₄ (O0c = C₂); O0c‥O6b (C₂‥B₈)
# SPN: scientific pitch notation (Middle C: C₄) ⇒ A₄ = 440 Hz (Stuttgart pitch)
# Hlm.: Helmholtz pitch notation (Middle C: c′) ⇒ c′ = 256 Hz (Scientific pitch)
#
# MML SPN Hlm. MIDI C♯  D   D♯  E   F   F♯  G   G♯  A   B♭  B   Hlm.-Oktavname
#  -  C₋₁ ,,,C   0   1   2   3   4   5   6   7   8   9  10  11  Subsubkonta-Okt.
#  -   C₀ ,,C   12  13  14  15  16  17  18  19  20  21  22  23  Subkontra-Oktave
#  -   C₁ ,C    24  25  26  27  28  29  30  31  32  33  34  35  Kontra-Oktave
# O0c  C₂ C     36  37  38  39  40  41  42  43  44  45  46  47  große Oktave
# O1c  C₃ c     48  49  50  51  52  53  54  55  56  57  58  59  kleine  " "
# O2c  C₄ c′    60  61  62  63  64  65  66  67  68  69  70  71  eingestrichene
# O3c  C₅ c″    72  73  74  75  76  77  78  79  80  81  82  83  zweigestrichene
# O4c  C₆ c‴    84  85  86  87  88  89  90  91  92  93  94  95  dreigestrichene
# O5c  C₇ c⁗    96  97  98  99 100 101 102 103 104 105 106 107  viergestrichene
# O6c  C₈ c‴″  108 109 110 111 112 113 114 115 116 117 118 119  fünfgestrichene
#  -   C₉ c‴‴  120 121 122 123 124 125 126 127                  sechsgestrichene
#
# (for tuning) 240 241 242 243 244 245 246 247 248 249 250 251

# Implementation notices
#
# bc function map:
# a(x) = abs(x)
# c(x) = Kosinus (math lib)
# e(x) = eˣ (math lib)
# j(n,x) = Bessel-Funktion (math lib)
# l(x) = lnₑ x (math lib)
# r(x,n) = round x to n decimal digits
# s(x) = Sinus (math lib)
# v(x) = sign (Vorzeichen)
# x(x,z,n) = x^(z/n)
#
# globals:
#  f	pitch standard frequency (e.g. 440)
#  m	output pitch standard MIDI number
#  n	counter
#  o	output pitch standard frequency
#  p	pitch standard number (e.g. 69)
#  s	output scale
#  t	temporary
#
#  f[]	0‥127: frequency for given note

unset LANGUAGE
export LC_ALL=C
set -U

usage() {
	local rv=1
	if [[ $1 = 0 ]]; then
		rv=0
		shift
	fi
	(( $# )) && print -ru2 -- "E: $*"
	print -ru2 -- "E: Usage: $0 [-c conf] [-d] [±f freq] [±n note] [-o note]"
	print -ru2 -- "E:	[-R note] [-r scale] [-s scale] [-T transpose]"
	print -ru2 -- "N: conf: defaults to equal temperament"
	print -ru2 -- "N: note + freq define the pitch standard, default A4 440"
	print -ru2 -- "N: +n and +f for output pitch standard, default to -n and -f values"
	print -ru2 -- "N:  -d = debug I/O between bc(1) and this script"
	print -ru2 -- "N:  -o = root note for .scl output (enable)"
	print -ru2 -- "N:  -R = root note for .scl input (switch to)"
	print -ru2 -- "N:  -r = use <scale> (3) precision for decimals (output)"
	print -ru2 -- "N:  -s = use <scale> (100) precision internally"
	print -ru2 -- "N:  -T = transpose by ±number semitones or note:note"
	exit $rv
}

die() {
	print -ru2 -- "E: $*"
	exit 1
}

# array to parse a note name
set -A recog_note -- \
    C B♯ 'B#' -- \
    C♯ 'C#' D♭ Db -- \
    D -- \
    D♯ 'D#' E♭ Eb -- \
    E F♭ Fb -- \
    F E♯ 'E#' -- \
    F♯ 'F#' G♭ Gb -- \
    G -- \
    G♯ 'G#' A♭ Ab -- \
    A -- \
    B♭ Bb A♯ 'A#' -- \
    B C♭ Cb --
function recog_note_simple {
	nameref outvar=$1
	local note=0 x
	local -u instr=$2

	for x in "${recog_note[@]}"; do
		if [[ $x = -- ]]; then
			let ++note
		elif [[ $instr = "$x" ]]; then
			break
		fi
	done
	outvar=$note
	(( note < 12 ))
}

# arrays to visualise notes in output
set -A visnote_map_spna C  C♯ D D♯ E  F  F♯ G G♯ A B♭ B
set -A visnote_map_spnb B♯ D♭ - E♭ F♭ E♯ G♭ - A♭ - A♯ C♭
set -A visnote_map_mmla c  c+ d d+ e  f  f+ g g+ a b- b
set -A visnote_map_mmlb b+ d- - e- f- e+ g- - a- - a+ c-
set -A visnote_map_octd 1  0  0 0  0  0  0  0 0  0 0  -1
set -A visnote_map_octv ₋₁ ₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉
visnote_map_octv[-1]=₋₂
i=-1
while (( ++i < 128 )); do
	visnote_map[i]=${visnote_map_spna[i%12]}${visnote_map_octv[i/12]}
	[[ ${visnote_map_spnb[i%12]} = - ]] || \
	    (( (n = i/12 - visnote_map_octd[i%12]) < 0 )) || \
	    visnote_map[i]+=" / "${visnote_map_spnb[i%12]}${visnote_map_octv[n]}
	(( i < 36 || i > 119 )) || \
	    visnote_map[i]+=" / "O$((i/12-3))${visnote_map_mmla[i%12]}
	[[ ${visnote_map_mmlb[i%12]} = - ]] || \
	    (( i <= 36 || i >= 119 )) || \
	    visnote_map[i]+=" / "O$((i/12-3-visnote_map_octd[i%12]))${visnote_map_mmlb[i%12]}
	visnote_map[i]+=" ($i)"
done
set -A visnote_pad 'C ' 'C♯' 'D ' 'D♯' 'E ' 'F ' 'F♯' 'G ' 'G♯' 'A ' 'B♭' 'B '

# cp1252 (ISO 8859-1 superset) parser (mixed content accepted)
set -A vistable \
	0x20AC 0x278A 0x201A 0x0192 0x201E 0x2026 0x2020 0x2021 \
	0x02C6 0x2030 0x0160 0x2039 0x0152 0x278B 0x017D 0x278C \
	0x278D 0x2018 0x2019 0x201C 0x201D 0x2022 0x2013 0x2014 \
	0x02DC 0x2122 0x0161 0x203A 0x0153 0x278E 0x017E 0x0178
# note 0x278A‥0x278E substitute undefined-in-cp1252 codepoints
function iso2utf8 {
	local s="$*"
	local -i lpos=-1 llen=${#s}
	local -i1 wc

	while (( ++lpos < llen )); do
		wc=1#${s:(lpos):1}
		(( (wc & 0xFF80) == 0xEF80 )) && (( wc &= 0x00FF ))
		(( wc > 0x7F && wc < 0xA0 )) && (( wc = vistable[wc & 0x1F] ))
		REPLY+=${wc#1#}
	done
}

# parse command line arguments
conf=/dev/null
iodebug=0
psfreq=440
psnote=A4
opsfrq=-
opsnote=-
oscale=3
iscale=100
irootnote=-
orootnote=-
transpose=
while getopts "c:df:hn:o:R:r:s:T:" ch; do
	case $ch {
	(c)	conf=$OPTARG ;;
	(d)	iodebug=1 ;;
	(+d)	iodebug=0 ;;
	(+f)	opsfrq=$OPTARG ;;
	(f)	psfreq=$OPTARG ;;
	(h)	usage 0 ;;
	(+n)	opsnote=$OPTARG ;;
	(n)	psnote=$OPTARG ;;
	(o)	orootnote=$OPTARG ;;
	(R)	irootnote=$OPTARG ;;
	(r)	oscale=$OPTARG ;;
	(s)	iscale=$OPTARG ;;
	(T)	transpose=$OPTARG ;;
	(*)	usage ;;
	}
done
shift $((OPTIND - 1))
(( $# )) && usage

[[ $opsfrq = - ]] && opsfrq=$psfreq
[[ $opsnote = - ]] && opsnote=$psnote

# validate command line arguments
[[ $conf = /dev/null ]] || [[ -f $conf && -s $conf ]] || \
    usage "Configuration file '$conf' missing or empty"
[[ $psfreq = [1-9]*([0-9])?(.+([0-9])) ]] || \
    usage "Pitch standard frequency '$psfreq' not numeric"
[[ $opsfrq = [1-9]*([0-9])?(.+([0-9])) ]] || \
    usage "Output pitch standard frequency '$opsfrq' not numeric"
[[ $oscale = [1-9]*([0-9]) ]] || usage "Output scale '$oscale' not integer"
[[ $iscale = [1-9]*([0-9]) ]] || usage "Internal scale '$oscale' not integer"
[[ $irootnote = - ]] || recog_note_simple irootnum "$irootnote" || \
    usage "Scala file input root note '$irootnote' not recognised"
[[ $orootnote = - ]] || recog_note_simple orootnum "$orootnote" || \
    usage "Scala file output root note '$orootnote' not recognised"
if [[ $transpose = '+'[1-9]*([0-9]) ]]; then
	(( transpose = ${transpose#'+'} % 12 ))
elif [[ $transpose = '-'[1-9]*([0-9]) ]]; then
	while (( transpose < 0 )); do
		(( transpose += 12 ))
	done
elif [[ $transpose = *:* ]]; then
	saveIFS=$IFS
	IFS=:
	set -A tarr -- $transpose
	IFS=$saveIFS
	(( ${#tarr[*]} == 2 )) || usage transpose needs exactly two notes
	recog_note_simple ta "${tarr[0]}" || \
	    usage "transpose source note '${tarr[0]}' not recognised"
	recog_note_simple tb "${tarr[1]}" || \
	    usage "transpose target note '${tarr[1]}' not recognised"
	(( transpose = (tb + 12 - ta) % 12 ))
elif [[ -n $transpose ]]; then
	usage invalid transpose argument
fi
[[ -z $transpose ]] || [[ $irootnote = - ]] || \
    usage transpose not useful for Scala files, change root note instead
: "${transpose:=0}"
function parse_ps {
	nameref dstnum=$1
	local s_note=$2 tlabel=$3 x= s line
	local -l llabel=$tlabel

	if [[ $s_note = [1-9]*([0-9]) ]]; then
		dstnum=$s_note
	elif [[ $s_note = [Oo]* ]]; then
		[[ ${s_note:1:1} = [0-6] ]] || \
		    usage "$tlabel '$s_note' octave part invalid"
		typeset -u s=${s_note:2}
		[[ $s = [A-G]?([#+-]) ]] || \
		    usage "$tlabel '$s_note' note part invalid"
		line=${s/+/♯}
		line=${line/-/♭}
		recog_note_simple dstnum "$line" || \
		    usage "$tlabel '$s_note' note part not recognised"
		(( dstnum += 12 * (${s_note:1:1} + 3) ))
	else
		line=$s_note
		dstnum=0
		for x in "${recog_note[@]}"; do
			if [[ $x = -- ]]; then
				let ++dstnum
			elif [[ $line = "$x"[0-9-]* ]]; then
				line=${line#"$x"}
				break
			fi
		done
		(( dstnum < 12 )) || \
		    usage "No note recognised in $llabel '$s_note'"
		[[ $line = -1 || $line = [0-9] ]] || \
		    usage "'$line' no PSN MIDI octave in $llabel '$s_note'"
		(( dstnum += 12 * ($line + 1) ))
	fi
	[[ $x = @(B♯|B#) ]] && let dstnum+=12
	[[ $x = @(C♭|Cb) ]] && let dstnum-=12
	(( dstnum < 0 )) && usage "$tlabel note '$s_note' ($dstnum) too small"
	(( dstnum > 127 )) && usage "$tlabel note '$s_note' ($dstnum) too large"
	print -ru2 -- "N: $llabel: ${visnote_map[dstnum]} = $psfreq"
}
parse_ps psnnum "$psnote" "Pitch standard"
parse_ps opsnum "$opsnote" "Output pitch standard"

# parse rules file
set -A tuned		# [0]=240, …, [11]=251; 1 if note was tuned
set -A rule_note1	# left hand side of =/+/-
set -A rule_note2	# lower note of = (empty if +/-)
set -A rule_rel		# right hand side of +/-; m3/M3/p5/CT*/^* if =
set -A rule_tempered	# + or - in = rules (empty if perfect)
set -A rule_temper_Z	# numerator in tempered rules
set -A rule_temper_N	# denominator in tempered rules
set -A rule_temper_C	# comma in tempered rules
set -A ZN_m3 6 5	# numerator/denominator for minor third
set -A ZN_M3 5 4	# for major third
set -A ZN_p5 3 2	# perfect fifth
set -A ZN_CP 531441 524288 # pyth. comma
set -A ZN_CS 81 80	# syntotic comma
set -A ZN_CD 32805 32768 # schisma
curCT=0

synerr() {
	print -ru2 -- "E: $*"
	print -ru2 -- "N: error in '$conf' line $linum: $line"
	print -ru2 -- "N:  $oline"
	exit 1
}

function read_line {
	local rv

	IFS= read -r line; rv=$?
	(( rv )) && return $rv
	let ++linum
	oline=$line
}

function sort_dat_pm_notes {
	case $(( (curnote1 + 12 - curnote2) % 12 )) {
	(3|4|7)
		rule_note1[nrules]=$curnote1
		rule_note2[nrules]=$curnote2
		;;
	(*)
		rule_note1[nrules]=$curnote2
		rule_note2[nrules]=$curnote1
		;;
	}
	case $(( (rule_note1[nrules] + 12 - rule_note2[nrules]) % 12 )) {
	(3)	rule_rel[nrules]=m3 ;;
	(4)	rule_rel[nrules]=M3 ;;
	(7)	rule_rel[nrules]=p5 ;;
	(*)	synerr interval between ${visnote_map_spna[curnote1]} and \
		    ${visnote_map_spna[curnote2]} not a minor or major third \
		    nor a perfect fifth, cannot use rule ;;
	}
}

function handle_dat_line {
	if [[ $oline = DESC[	 ]* ]]; then
		fdesc=${oline##DESC+([	 ])}
		while [[ $fdesc = *\\ ]]; do
			read_line || synerr missing continuation line
			fdesc+=${line##*([	 ])}
		done
		return
	fi
	line=${line%%'!'*}
	line=${line//+([	 ])}
	if [[ -z $line ]]; then
		return
	elif [[ $line = VALIDATE ]]; then
		vrulep=$nrules
		return
	fi
	# parse LHS
	curnote1=0
	for x in "${recog_note[@]}"; do
		if [[ $x = -- ]]; then
			let ++curnote1
		elif [[ $line = "$x"[=+-]* ]]; then
			line=${line#"$x"}
			break
		fi
	done
	(( curnote1 < 12 )) || synerr left-hand side note not recognised
	(( curnote1 = (curnote1 + transpose) % 12 ))
	# parse operator, handle delta-cent rules
	if [[ $line = [+-]@(0|[1-9]*([0-9])|@(*(0)|[1-9]*([0-9])).*([0-9])) ]]; then
		rule_note1[nrules]=$curnote1
		rule_rel[nrules++]=$line
		return
	fi
	[[ $line = [+-]* ]] && synerr right-hand side of ± rule not numeric
	# parse operator, handle non-interval rules
	[[ $line = =* ]] || synerr line not an interval rule
	line=${line#?}
	# parse RHS note
	curnote2=0
	for x in "${recog_note[@]}"; do
		if [[ $x = -- ]]; then
			let ++curnote2
		elif [[ $line = "$x"?([*^+-]*) ]]; then
			line=${line#"$x"}
			break
		fi
	done
	(( curnote2 < 12 )) || synerr right-hand side note not recognised
	(( curnote2 = (curnote2 + transpose) % 12 ))
	# sort notes
	if [[ $line = \** ]]; then
		(( rule_note1[nrules] = curnote1 > curnote2 ? curnote1 : curnote2 ))
		(( rule_note2[nrules] = curnote1 < curnote2 ? curnote1 : curnote2 ))
		# handle interval
		line=${line#?}
		[[ $line = [1-9]*([0-9])'/'[1-9]*([0-9])?([+-]*) ]] || \
		    synerr invalid interval fraction
		x=${line%%[+-]*}
		rule_rel[nrules]=CT$curCT
		nameref ZN_curCT=ZN_CT$((curCT++))
		set -A ZN_curCT
		ZN_curCT[0]=${x%/*}
		ZN_curCT[1]=${x#*/}
		# remaining line is tempering
		line=${line#"$x"}
	elif [[ $line = '^'* ]]; then
		# ordered
		rule_note1[nrules]=$curnote1
		rule_note2[nrules]=$curnote2
		# handle delta
		line=${line#?}
		[[ $line = @(0|[1-9]*([0-9]))?(.+([0-9]))?([+-]*) ]] || \
		    synerr invalid cent delta
		x=${line%%[+-]*}
		rule_rel[nrules]="^$x"
		# remaining line is tempering
		line=${line#"$x"}
	else
		sort_dat_pm_notes
	fi
	# any tempering?
	if [[ -z $line ]]; then
		let ++nrules
		return
	fi
	[[ $line = [+-]* ]] || \
	    synerr right-hand side note not followed by valid tempering operator
	rule_tempered[nrules]=${line::1}
	line=${line#?}
	case $line {
	(*P)	rule_temper_C[nrules]=P ;;
	(*S)	rule_temper_C[nrules]=S ;;
	(*D)	rule_temper_C[nrules]=D ;;
	(*)	synerr comma not recognised ;;
	}
	line=${line%?}
	[[ ${line:=1/1} = [1-9]*([0-9])'/'[1-9]*([0-9]) ]] || \
	    synerr invalid tempering fraction
	rule_temper_Z[nrules]=${line%/*}
	rule_temper_N[nrules++]=${line#*/}
}

function handle_scl_line {
	[[ $line = '!'* ]] && return
	oline=${oline%%*( )?()}
	line=${oline##*( )}
	case $state {
	(0)
		fdesc=${|iso2utf8 "$oline";}
		x=${visnote_map_spna[orootnum]}/${visnote_map_spnb[orootnum]}
		fdesc+=" — imported from .scl with root note ${x%/-}"
		;;
	(1)
		[[ $line = 12 ]] || synerr only twelve-step scales are supported
		;;
	(2|3|4|5|6|7|8|9|10|11|12)
		(( sclnum = (irootnum + state - 1) % 12 ))
		x=${line##*([0-9])?([./]*([0-9]))}
		line=${line%"$x"}
		if [[ $line = *.* ]]; then
			line=${line##+(0)}
			line=${line%%+(0)}
			[[ $line = .* ]] && line=0$line
			[[ $line = *. ]] && line=${line%.}
			rule_note1[nrules]=$sclnum
			rule_note2[nrules]=$irootnum
			rule_rel[nrules++]='^'$line
		elif [[ $line = /* ]]; then
			synerr "missing numerator in '$line'"
		elif [[ $line = */ ]]; then
			synerr "missing denominator in '$line'"
		else
			[[ $line = */* ]] || line+=/1
			(( rule_note1[nrules] = irootnum > sclnum ? irootnum : sclnum ))
			(( rule_note2[nrules] = irootnum < sclnum ? irootnum : sclnum ))
			rule_rel[nrules++]=CT$curCT
			nameref ZN_curCT=ZN_CT$((curCT++))
			set -A ZN_curCT
			ZN_curCT[0]=${line%/*}
			ZN_curCT[1]=${line#*/}
		fi
		;;
	(13)
		x=${line##*([0-9])?([./]*([0-9]))}
		line=${line%"$x"}
		[[ $line = 2?(/1) ]] || \
		    synerr "last interval '$line' not an octave"
		;;
	(14)
		synerr extra line in file
		;;
	}
	let ++state
}

# switch by input file type
alias handle_line=handle_dat_line
[[ $irootnote = - ]] || alias handle_line=handle_scl_line

fdesc=
nrules=0
vrulep=-1
linum=0
state=0
while read_line; do
	handle_line
done <"$conf"
(( vrulep = vrulep == -1 ? nrules : vrulep ))
[[ $irootnote = - ]] || (( state == 14 )) || \
    synerr not enough lines in input file
(( transpose )) && fdesc+=" (transposed up by $((transpose)) semitones)"

# dump rules, for checking
print -ru2 -- "I: parsed rules from '$conf'"
[[ -n $fdesc ]] && print -r -- "N: DESC $fdesc"
i=-1
while (( ++i < nrules )); do
	(( i == vrulep )) && print -ru2 -- "N: VALIDATE"
	if [[ -n ${rule_note2[i]} ]]; then
		x="=${visnote_pad[rule_note2[i]]}"
		if [[ ${rule_rel[i]} = CT* ]]; then
			nameref ZN_curCT=ZN_${rule_rel[i]}
			x+=" * ${ZN_curCT[0]}/${ZN_curCT[1]}"
		elif [[ ${rule_rel[i]} = '^'* ]]; then
			x+=" ^ ${rule_rel[i]:1}"
		fi
		if [[ -n ${rule_tempered[i]} ]]; then
			x+=" ${rule_tempered[i]} "
			(( rule_temper_Z[i] == 1 && rule_temper_N[i] == 1 )) || \
			    x+="${rule_temper_Z[i]}/${rule_temper_N[i]} "
			x+="${rule_temper_C[i]}"
		fi
		while (( ${%x} < 16 )); do x+=' '; done
		case ${rule_rel[i]} {
		(\^*|CT*) x=${x%%*( )} ;;
		(m3) x+='! minor third' ;;
		(M3) x+='! major third' ;;
		(p5) x+='! perfect fifth' ;;
		(*)  x+="!! invalid (${rule_rel[i]})" ;;
		}
	else
		x=${rule_rel[i]}
	fi
	print -ru2 -- "N:  ${visnote_pad[rule_note1[i]]} ${x::1} ${x:1}"
done
print -ru2 -- "N: ! $vrulep defining rules, $((nrules - vrulep)) validating, $nrules rules total."

# function to prepare a bc(1)-output number for printing
function visnum {
	local value=$1 wantplus=$2

	if [[ $value = ?([+-])*(0)?(.*(0)) ]]; then
		value=0
	elif [[ $value = -* ]]; then
		REPLY=-
		value=${value#-}
	elif [[ -n $wantplus ]]; then
		REPLY=+
	fi
	[[ $value = .* ]] && REPLY+=0
	REPLY+=$value
}

# prepare bc(1) for use
export BC_ENV_ARGS=-qs	# make GNU bc use POSIX mode and shut up
if (( iodebug )); then
	while IFS= read -r bcinline; do
		print -ru2 -- ">bc: $bcinline"
		print -r -- "$bcinline"
	done | bc -l | while IFS= read -r bcoutline; do
		print -ru2 -- "<bc: $bcoutline"
		print -r -- "$bcoutline"
	done |&
else
	bc -l |&
fi
function tobc {
	print -pr -- "$@" || die coprocess died trying to write
}
function frombc {
	read -p "$@" || die coprocess died trying to read
	# perhaps: check whether there’s more to read (errors?)
}
function quitbc {
	trap - HUP INT QUIT TRAP PIPE TERM
	exec 3>&p
	print -ru3 -- quit
	exec 3>&-
	:
}
function trapbc {
	local i=$1
	exec 3>&p
	exec 3>&-
	wait
	print -u2
	print -ru2 -- "E: killed by signal $i"
	exit $((128 + i))
}
trap 'trapbc 1' HUP
trap 'trapbc 2' INT
trap 'trapbc 3' QUIT
trap 'trapbc 5' TRAP
trap 'trapbc 13' PIPE
trap 'trapbc 15' TERM

# initialise our bc functions
tobc "scale = $iscale"

tobc "define a(x) {"
tobc "	if (x > 0) return (x)"
tobc "	return (-x)"
tobc "}"

tobc "define v(x) {"
tobc "	if (x < 0) return (-1)"
tobc "	if (x > 0) return (1)"
tobc "	return (0)"
tobc "}"

tobc "define r(x,n) {"
tobc "	auto o"
tobc "	o = scale"
tobc "	if (scale < (n + 1)) scale = (n + 1);"
tobc "	x += v(x) * 0.5 * A^-n"
tobc "	scale = n"
tobc "	x /= 1"
# drop trailing zeroes
tobc "  for (scale = 0; scale <= o; scale++) {"
tobc "		if (x == x/1) {"
tobc "			x /= 1"
tobc "			break"
tobc "		}"
tobc "	}"
tobc "	scale = o"
tobc "	return (x)"
tobc "}"

tobc "define x(x,z,n) {"
tobc "	z /= n"
tobc "	if (scale(z) == 0) return (x^z)"
tobc "	return (e(l(x) * z))"
tobc "}"

tobc "f = $psfreq"
tobc "p = $psnnum"
tobc "o = $opsfrq"
tobc "m = $opsnum"
tobc "s = $oscale"

# calculate A₄ for MuseScore from the given pitch standard
if (( opsnum == 69 )); then
	tobc "r(o, 3)"
else
	tobc "r(o * x(2, 69 - m, 12), 3)"
fi
frombc x
print -ru2 -- "I: A₄ frequency for MuseScore synthesizer.xml //master/val@id=3 is ${|visnum "$x";}"

# initialise a frequency table in equal temperament
tobc "for (n = 0; n < 128; ++n) {"
tobc "	f[n] = f * x(2, n - p, 12)"
tobc "}"

# … more processing here tbd

# initialise all untuned frequencies as equal temperament
i=-1
while (( ++i < 12 )); do
	[[ -z ${tuned[i]} ]] || continue
	x=${visnote_map_spna[i]}/${visnote_map_spnb[i]}
	print -ru2 -- "W: assuming 12-tET value for untuned note ${x%/-}"
	(( j = 240 + i ))
	tobc "f[$j] = f * x(2, $j - p, 12)"
done
# duplicate the tuning octave up
tobc "for (n = 240; n < 252; ++n) {"
tobc "	f[n+12] = 2 * f[n]"
tobc "}"

# read out frequency table
set -A dstfreq
i=-1
tobc "for (n = 0; n < 128; ++n) {"
tobc "	r(f[n], s)"
tobc "}"
while (( ++i < 128 )); do
	frombc dstfreq[i]
done

# read out cent deltas
set -A dstcent
i=-1
tobc "t = p"
tobc "while (t < 240) t += 12"
tobc "while (t >= 252) t -= 12"
tobc "for (n = 240; n < 252; ++n) {"
tobc "	r(1200 * l(f[n] / (f * x(2, n - p, 12))) / l(2), s)"
tobc "	r(1200 * l(f[n] / (f * x(2, t - p, 12))) / l(2), s)"
tobc "}"
while (( ++i < 12 )); do
	frombc dstcent[i]
	frombc dstcent[i+12]
done

# read out Scala deltas
if [[ $orootnote != - ]]; then
	set -A dstscala
	tobc "n = 240 + $orootnum"
	tobc "for (t = 1; t < 12; ++t) {"
	tobc "	r(1200 * l(f[n + t] / f[n]) / l(2), s)"
	tobc "}"
	i=0
	while (( ++i < 12 )); do
		frombc dstscala[i]
	done
fi

# thanks
quitbc

# display frequency table
print
print -r -- Frequency table:
set -A fL -- \
    'MML SPN Hlm.' \
    ' -  C₋₁ ,,,C' \
    ' -   C₀ ,,C ' \
    ' -   C₁ ,C  ' \
    'O0c  C₂ C   ' \
    'O1c  C₃ c   ' \
    'O2c  C₄ c′  ' \
    'O3c  C₅ c″  ' \
    'O4c  C₆ c‴  ' \
    'O5c  C₇ c⁗  ' \
    'O6c  C₈ c‴″ ' \
    ' -   C₉ c‴‴ '
set -A fl # length of decimal point plus following digits
set -A ft # texts
set -A fb 0 0 0 0 0 0 0 0 0 0 0 0 # base length
i=-1
while (( ++i < 128 )); do
	ft[i]=${|visnum ${dstfreq[i]};}
	if [[ ${ft[i]} = *.* ]]; then
		x=.${ft[i]#*.}
		fl[i]=${%x}
	else
		fl[i]=0
	fi
	(( fl[i] > fb[i%12] )) && (( fb[i%12] = fl[i] ))
done
set -A fl 2 2 2 2 2 2 2 2 2 2 2 2 # total length, plus one if fb[i%12]
i=-1
while (( ++i < 128 )); do
	if (( fb[i%12] )); then
		x=
		[[ ${ft[i]} = *.* ]] && x=.${ft[i]#*.}
		typeset -L$((fb[i%12] - ${%x})) s=
		ft[i]+=$s,
	fi
	(( ${%ft[i]} > fl[i%12] )) && (( fl[i%12] = ${%ft[i]} ))
done
i=-1
while (( ++i < 12 )); do
	if (( fb[i] )); then
		typeset -L$((fl[i] - fb[i] - 2)) s=
		x=$s
		unset s	# workaround for mksh bug with typeset -L
		s=.$x${visnote_map_spna[i]}
		while (( ${%s} < fl[i] )); do s+=' '; done
	else
		typeset -R${fl[i]} s=${visnote_map_spna[i]}
	fi
	fL[0]+=" ${s#.}"
done
i=-1
while (( ++i < 128 )); do
	typeset -R${fl[i%12]} s=${ft[i]}
	fL[i/12 + 1]+=" ${s%,}"
done
for x in "${fL[@]}"; do
	print -r -- "$x"
done

# display cent delta
print
print -r -- Cent delta values:
i=-1
print -n ' '
while (( ++i < 12 )); do
	print -nr -- " ${visnote_map_spna[i]} (${|visnum ${dstcent[i]} 1;})"
	(( i == 11 )) || print -n ,
	(( i == 5 )) && print -n '\n  '
done
print

print
print -r -- Cent delta table:
print -n ' '
x=
for i in 0 7 2 9 4 11 6 1 8 3 10 5; do
	y=${|visnum ${dstcent[i + 12]};}
	if [[ $y = *?.* ]]; then
		z=${y%?.*}
		typeset -L${%z} s=
		z=$s${visnote_map_spna[i]}
		while (( ${%z} < ${%y} )); do z+=' '; done
	else
		typeset -R${%y} s="${visnote_map_spna[i]}"
		z=$s
	fi
	print -nr -- " $z"
	typeset -L${%z} s=$y
	x+=\ $s
done
print
print -r -- " $x"

# display Scala file
if [[ $orootnote != - ]]; then
	print
	print -r -- "Scala file:"
	x=${conf%.*}.scl
	[[ $conf = /dev/null ]] && x=equal.scl
	print -r -- "! ${x//[- ]/_}"
	x=${visnote_map_spna[orootnum]}/${visnote_map_spnb[orootnum]}
	print -r -- "!  Converted by MirBSD tuner.sh, root note: ${x%/-}"
	print -r -- "!  The following description is in UTF-8 encoding:"
	print -r -- "$fdesc"
	print -r -- " 12"
	print -r -- "!"
	i=0
	while (( ++i < 12 )); do
		x=${|visnum ${dstscala[i]};}
		[[ $x = *.* ]] || x+=.0
		print -r -- " $x"
	done
	print -r -- " 2/1"
fi


print -ru2 -- E: aktuell nur Notizen
exit 1

:<<'EOF'

Rechnen mit Schwingungsverhältnissen:

C5 = C4 * 2:1
C4‥C♯4 ≘ 1/12 of that 2:1
       ≠ 2*1:1*12 (= 2:12 = 1:6)
       = (2:1)^(1/12) (= ¹²√(2:1))
       = eˡⁿ⁽²⁄₁⁾ ⃰⁽¹⁄₁₂⁾
       = e(l(2/1)*(1/12))

⅙py, ≘ 1/6 of 531441/524288
     = e(l(531441/524288)*(1/6))
     ≠ 3188646/3145728 ⚠


Beispiel für Bach/Lehman:

in vallotti = pure5 - ⅙py,    = *3/2*e(l(524288/531441)/6)
very gently = pure5 - 1/12py, = *3/2*e(l(524288/531441)/12)
⇒ left over = pure5 + 1/12py, = *3/2/e(l(524288/531441)/12)

F = 256/(3/2*e(l(524288/531441)/6))	171.052553882945146510123431473378401533544822248606797449681463
C = 256					256
G = 256*3/2*e(l(524288/531441)/6)	383.133712489599310995979280957159941540662992422907478507659520
D = G*3/2*e(l(524288/531441)/6)		573.404068929933386987388294156847035992861111046375187379529193
A = D*3/2*e(l(524288/531441)/6)		858.165740960029232576550668794507159973537185413922860404691537
E = A*3/2*e(l(524288/531441)/6)		1284.344633849233589843552849078394989543418601694379837664142227
B = E*3/2				1926.516950773850384765329273617592484315127902541569756496213340
F♯ = B*3/2				2889.775426160775577147993910426388726472691853812354634744320010
C♯ = F♯*3/2				4334.663139241163365721990865639583089709037780718531952116480015
G♯ = C♯*3/2*e(l(524288/531441)/12)	6494.656457401527319033173172554279132053351587642394831634422053
D♯ = G♯*3/2*e(l(524288/531441)/12)	9730.989732007546112575910768263304620574994332365186699359611116
A♯ = D♯*3/2*e(l(524288/531441)/12)	14580.010780481228688925566360884049509443067495767499264698665821
F == A♯*3/2/e(l(524288/531441)/12)	21894.72689701697875329579922859243539629373724782167007355911267477


Note deltas:
    D♭    E♭ F♭    G♭    A♭   B♭ C♭
 C  C♯ D  D♯ E  F  F♯ G  G♯ A A♯ B
 0  1  2  3  4  5  6  7  8  9 10 11

3 = 6:5 (D-F, minor 3rd)
4 = 5:4 (C-E, major 3rd)
5 = 4:3 (C-F, quart: reverse quint)
7 = 3:2 (C-G, perfect 5th = quint)
12 = 2:1 (Oktave)

octave:	2/1 = 1200 ¢
pure 5:	3/2 ≈ 701.955001 ¢
pure 4: 4/3
major3: 5/4
minor3: 6/5
Pcomma:	12*pure5 - 7*octave = 531441/524288 ≈ 23.46001 ¢
	⇒ 1200*l(531441/524288)/l(2)
Scomma: 4*pure5 - (2*octave + major3) = (3/2)⁴ / ((2/1)² * 5/4)
	= 81/16 / (4*5/4) = 81/80 ≈ 21.50629 ¢
Schism: Pcomma - Scomma = 531441/524288 / 81/80 = 32805/32768 ≈ 1.95372 ¢

1200 ¢: octave = 2:1 = 2^(1200/1200)
 100 ¢: semitone(equal) = 2^(100/1200) = 2^(1/12) = ¹²√2 = e(l(2)/12) ≈ 1.059463


EOF
