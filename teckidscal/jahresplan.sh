#!/bin/mksh
# -*- mode: sh -*-
#-
# Copyright © 2015, 2016, 2017, 2018
#	Thorsten Glaser <thorsten.glaser@teckids.org>
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
# Teckids e.V. Wandkalender (Jahresplan, interne Termine)
# https://www.teckids.org/docs/verein/docs/jahresplan_a3.pdf (DIN ISO A3)

cd "$(dirname "$0")"
exec >jahresplan.def
while read datum typ label; do
	case x$datum {
	(x|x\#*)
		continue
		;;
	(xPlaner:)
		planer="$typ $label"
		continue
		;;
	(xZeitraum:)
		if [[ $typ != ?(1)[0-9]/2[0-9][0-9][0-9] || \
		    $label != 'bis '?(1)[0-9]/2[0-9][0-9][0-9] ]]; then
			print -ru2 "Zeitraum-Syntax: m/yyyy bis m/yyyy"
			exit 1
		fi
		label=${label/bis }
		print -r -- "\\teckidscalprep{${typ/'/'/'}{'}}{${label/'/'/'}{'}}"
		print
		continue
		;;
	(x2[0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9])
		;;
	(*)
		print -ru2 "E: unbekannte Zeile:	$datum	$typ	$label"
		exit 1
		;;
	}
	case $typ {
	(Veranstaltung)
		m=teckidscalveranst
		;;
	(Workday-normal)
		m=teckidscalworkday
		;;
	(Workday-klein)
		m=teckidscalworkklein
		;;
	(noch-ungeplant)
		m=teckidscalextveranst
		;;
	(Gruppentreffen)
		m=teckidscallabel
		if [[ -z $label ]]; then
			print -ru2 "E: Gruppentreffen $datum ohne Label!"
			exit 1
		fi
		;;
	(Onlinemeeting)
		m=teckidscalonline
		if [[ -n $label && $label != +([0-9]):[0-5][0-9] ]]; then
			print -ru2 "E: Onlinemeeting $datum mit Label: $label"
			exit 1
		fi
		;;
	(*)
		print -ru2 "E: unbekannter Typ:	$datum	$typ	$label"
		exit 1
		;;
	}
	m="\\$m"
	[[ -n $label ]] && m+="[$label]"
	m+="{$datum}"
	print -r -- "$m"
done <jahresplan.txt

print -r -- "\\newcommand{\\teckidscalPlaner}{$planer}%"
exec >/dev/null
