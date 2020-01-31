#!/bin/mksh
# -*- mode: sh -*-
# From MirOS: www/mk/common,v 1.11 2018/08/29 02:54:14 tg Exp $
#-
# Copyright © 2019
#	Thorsten Glaser <t.glaser@tarent.de>
# Copyright © 2007, 2008, 2012, 2013, 2014, 2018
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
# Take input in nroff-like syntax and create Teχ and XHTML from it.

nline=0
die() {
	print -ru2 -- "E: $0: $*"
	print -ru2 -- "N: line #$nline '$oline'"
	exit 1
}

dl=0
while getopts "d" ch; do
	case $ch {
	(d)	dl=1 ;;
	(*)	die syntax error ;;
	}
done
shift $((OPTIND - 1))
bn=$1

if (( dl )); then
	ul=dl
	li=dd
else
	ul=ul
	li=li
fi

function tohtm {
	REPLY=${1//'&'/'&amp;'}
	REPLY=${REPLY//'<'/'&lt;'}
	REPLY=${REPLY//'>'/'&gt;'}
	REPLY=${REPLY//'"'/'&#34;'}
}

function totex {
	REPLY=${1//'\'/'\textbackslash'}
	REPLY=${REPLY@/[{\}#&_%\$]/\\$KSH_MATCH}
	REPLY=${REPLY//'^'/'\textasciicircum '}
	REPLY=${REPLY//'~'/'\textasciitilde '}
	REPLY=${REPLY//' '/'~'}
	REPLY=${REPLY//' — '/' \dash '}
}

# filter stdin into an HTML/XHTML JavaScript block
function script_escape {
	print -r -- "<script type=\"text/javascript\"><!--//--><![CDATA[//><!--"
	print -r -- "$(cat)"
	print -r -- "//--><!]]></script>"
}

# filter stdin into an HTML/XHTML inline CSS block
function css_escape {
	print -r -- "<style type=\"text/css\"><!--/*--><![CDATA[/*><!--*/"
	print -r -- "$(cat)"
	print -r -- "/*]]>*/--></style>"
}

function doheader {
	css_escape <<\EOF
 a[href^="ftp://"]:after,
 a[href^="http://"]:after,
 a[href^="https://"]:after,
 a[href^="irc://"]:after,
 a[href^="mailto:"]:after,
 a[href^="news:"]:after,
 a[href^="nntp://"]:after {
	content:url(vextlnk.png);
	margin:0 0 0 3px;
 }

 a[href^="https://edugit.org/mirabilos/mscore-workshop/"]:after,
 a[href^="https://mirabilos.edugit.org/mscore-workshop/"]:after,
 a[href^="http://www.mirbsd.org/~tg/mscore-workshop/"]:after {
	content:"";
	margin:0px;
 }

 .marker {
	background-color:#FFFF99;
 }
EOF
	script_escape <<\EOF
 function geladen() {
	var curr = false;
	var ohash = false;
	var hashchgfn = function hashchgfn() {
		/* XXX see if :target CSS can’t do this */
		var nhash = window.location.hash;
		if (nhash === ohash)
			return;
		if (curr !== false) {
			curr.classList.remove('marker');
			curr = false;
		}
		if (!nhash)
			return;
		var el = document.getElementById(nhash.substr(1));
		if (!el)
			return;
		curr = el;
		ohash = nhash;
		curr.classList.add('marker');
		curr.scrollIntoView(true);
	};
	if ('onhashchange' in window)
		window.onhashchange = hashchgfn;
	else
		window.setInterval(function onhashchange_emul() {
			if (window.location.hash !== ohash)
				hashchgfn();
		    }, 500);
	hashchgfn();
 }
EOF
}

# newline and block indent
nli=$'\n '
nlt=$'\n ' # for TeX, which can have more

exec <"$bn.dat"
exec 4>"$bn.htm"
exec 5>"$bn.tex"
print -ru4 -- '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"'
print -ru4 -- ' "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">'
print -ru5 -- '\newcommand{\myLink}[2]{\href{#1}{#2}\Hair\footnote{\texttt{[\url{#1}]}}}%'

# end item
function endit {
	(( init )) || return 0
	print -ru4 -- "</$li>"
	xnl=
	print -ru5 -- '%'
	print -ru5 -- '}'
	print -ru5
	tnl=
	init=0
}

# retrieve a command word
function getcw {
	if [[ $line = *' '* ]]; then
		cw=${line%% *}
		line=${line#* }
	else
		cw=$line
		line=
	fi
}

# retrieve language code (Nm, It, No, Li)
function getlang {
	local cw

	if [[ $line != '&'* ]]; then
		xmllang=
		ltxlang=
		return
	fi

	getcw
	xmllang=${cw#?}
	case $xmllang {
	(de)		ltxlang=ngerman ;;
	(de-DE-1901)	ltxlang=german ;;
	(en)		ltxlang=british ;;
	(fr)		ltxlang=french ;;
	(*)		die "Unknown language: $xmllang" ;;
	}
	xmllang=" xml:lang=\"$xmllang\""
	[[ -n $1 ]] || ltxlang="\\foreignlanguage{$ltxlang}"
}

# trailing punctuation: a No Li
# after .Pq: a ac(ao) No Li
function getpc {
	pc=
	if [[ $line = *' '[.,:\;\)?!] ]]; then
		pc=${line##* }
		line=${line% *}
	fi
	if (( inpq & 1 )); then
		pc+=')'
		(( inpq &= ~1 ))
	fi
}

function anchorIt {
	local id=$cw idhtm idltx lbl=$line lblhtm lblltx ltxit

	[[ $id = +([\ -~]) ]] || die \
	    "item identifyer ${id@Q} must be comprised of printable ASCII"
	[[ $id = [A-Z_a-z]*([A-Z_a-z0-9.-]) ]] || die \
	    "item identifyer ${id@Q} is not a valid ASCII xml:id NCName"
	[[ $id = [A-Za-z]*([A-Za-z0-9_:.-]) ]] || die \
	    "item identifyer ${id@Q} is not a valid HTML 4 id attribute"
	[[ $id = *[\"\#\'\(\),=\{\}%\ \~\\]* ]] && die \
	    "item identifyer ${id@Q} contains characters prohibited in Tₑχ"
	local -l idlc=$id
	local idid=ID_${idlc//-/D}
	idid=${idid//./P}
	[[ $idid = ID_+([0-9A-Z_a-z]) ]] || die \
	    "internal consistency error on ${idid@Q}"
	nameref idv=$idid
	[[ -z $idv ]] || die "ID ${id@Q} already used as ${idv@Q}"
	idv=$id

	idhtm=${|tohtm "$id";}
	idltx=${|totex "$id";}
	if [[ -z $lbl && $idltx = "$id" ]]; then
		lblhtm=$idhtm
		lblltx=$idltx
		ltxit="{$id}"
	else
		lblhtm=${|tohtm "${lbl:-$id}";}
		lblltx=${|totex "${lbl:-$id}";}
		ltxit="[$lblltx]{$id}"
	fi
	local attr=" id=\"$idhtm\""
	if [[ $li = li ]]; then
		attr+=" title=\"$lblhtm\""
	else
		print -nru4 -- "<dt><a href=\"#$idhtm\">$lblhtm</a></dt>"
	fi
	print -nru4 -- "<$li$xmllang$attr>"
	xnl=
	if [[ $li = li ]]; then
		print -nru4 -- "<a href=\"#$idhtm\">∞</a>"
		xnl=' '
	fi
	print -ru5 -- "\\bibitem$ltxit$ltxlang{%"
	tnl=' '
}

init=0
inpq=0
inao=0
xnl=
tnl=
while IFS= read -r line; do
	oline=$line; let ++nline
	if [[ $line != .* ]]; then
		print -nru4 -- "$xnl${|tohtm "$line";}"
		xnl=$nli
		print -nru5 -- "$tnl${|totex "$line";}"
		tnl=$nlt
		continue
	fi
	while [[ $line = *\\ ]]; do
		IFS= read -r oline || break
		let ++nline
		line=${line%?}${oline##*([	 ])}
	done
	getcw
	case ${cw#.} {
	(Pq)
		(( inpq |= 1 ))
		;&
	(Po)
		print -nru4 -- "$xnl("
		xnl=
		print -nru5 -- "$tnl("
		tnl=
		getcw
		;;
	}
	case ${cw#.} {
	(Nm)
		getlang raw
		[[ -n $xmllang ]] || die no document language given
		print -ru4 -- "<html xmlns=\"http://www.w3.org/1999/xhtml\"$xmllang><head>"
		print -ru4 -- ' <meta http-equiv="content-type" content="text/html; charset=utf-8" />'
		print -ru4 -- " <title>${|tohtm "$line";}</title>"
		doheader >&4
		print -ru4 -- '</head><body onload="javascript:geladen();">'
		xnl=
		print -ru5 -- "\\begin{otherlanguage*}{$ltxlang}"
		print -ru5
		tnl=
		;;
	(Ss)
		print -ru4 -- "<h3>${|tohtm "$line";}</h3>"
		print -ru4
		xnl=
		;;
	(Bl)
		print -ru4 -- "<h4>${|tohtm "$line";}</h4><$ul>"
		xnl=
		print -ru5 -- '\defcaptionname{german,british}{\refname}{'"${|totex "$line";}"'}%'
		print -ru5 -- '\begin{autosizedbibliography}'
		print -ru5
		tnl=
		init=0
		;;
	(El)
		[[ -z $line ]] || die .El takes no arguments
		(( init )) || die empty list
		endit
		print -ru4 -- "</$ul>"
		print -ru4
		xnl=
		print -ru5 -- '\end{autosizedbibliography}'
		print -ru5
		tnl=
		;;
	(It)
		endit
		getlang
		getcw
		if [[ -z $cw ]]; then
			[[ $li = li ]] || print -nru4 -- "<dt>✹</dt>"
			print -nru4 -- "<$li$xmllang>"
			xnl=
			print -ru5 -- '\item[\TwelweStar\hfill]'"$ltxlang{%"
			tnl=' '
		else
			anchorIt
		fi
		init=1
		;;
	(a)
		getcw
		getpc
		print -nru4 -- "$xnl<a href=\"${|tohtm "$cw";}\">${|tohtm "$line";}</a>$pc"
		xnl=$nli
		print -nru5 -- "$tnl\\myLink{${|totex "$cw";}}{%"
		print -nru5 -- "$nlt ${|totex "$line";}%"
		print -nru5 -- "$nlt}$pc"
		tnl=' '
		;;
	(ao)
		(( inao )) && die already in a .ao
		(( inpq <<= 1 ))
		inao=1
		print -nru4 -- "$xnl<a href=\"${|tohtm "$line";}\">"
		xnl=
		print -nru5 -- "$tnl\\myLink{${|totex "$line";}}{%"
		nlt+=' '
		tnl=$nlt
		;;
	(ac)
		nlt=${nlt% }
		tnl=${tnl% }
		(( inpq >>= 1 ))
		getpc
		print -nru4 -- "</a>$pc"
		print -nru5 -- "%$nlt}$pc"
		[[ -n $tnl ]] && tnl=' '
		inao=0
		;;
	(Em)
		xmlcmd=em ltxcmd=emph
		;|
	(Li)
		xmlcmd=tt ltxcmd=texttt
		;|
	(No)
		xmlcmd=span ltxcmd=
		;|
	(Em|Li|No)
		getlang
		getpc
		if [[ -z $ltxcmd ]]; then
			ltxcmd="$ltxlang{"
			ltxend='}'
		elif [[ -z $ltxlang ]]; then
			ltxcmd="\\$ltxcmd{"
			ltxend='}'
		else
			ltxcmd="$ltxlang{\\$ltxcmd{"
			ltxend='}}'
		fi
		print -nru4 -- "$xnl<$xmlcmd$xmllang>${|tohtm "$line";}</$xmlcmd>$pc"
		xnl=$nli
		print -nru5 -- "$tnl$ltxcmd${|totex "$line";}$ltxend$pc"
		tnl=$nlt
		;;
	(RH)
		print -ru4 -- "$xnl$line"
		xnl=
		;;
	(RT)
		print -ru5 -- "$tnl$line"
		tnl=
		;;
	(*)
		die "Unknown command: $cw"
		;;
	}
done
(( init )) && die still in .It at EOF
(( inpq )) && die still in .Pq at EOF: $inpq
(( inao )) && die still in .ao at EOF

print -ru4 -- "$xnl</body></html>"
print -ru5 -- '\end{otherlanguage*}'
exec 4>&-
exec 5>&-
print -ru2 -- "I: $bn.{htm,tex} generated"
exit 0
