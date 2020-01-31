#! /bin/sh
#
# latex2eps.sh - translates latex math formula to EPS
#
# Expected usage:
#   %filter "latex2eps.sh eqn"
#   \begin{displaymath}
#   \frac{1}{x+y}
#   \end{displaymath}
#   %endfilter
#   %image "eqn.eps" 250x200
#
# additional style can be used with option -sty, eg.
#   latex2eps -sty pslatex -sty color eqn
#
# History:
#   18.03.2003  first creation
#   06.03.2003  support for subdirs added
#

#parse command line options
STYLES=""
while [ $# -gt 0 ]
do
	case "$1" in
	# additional styles
	-sty) STYLES="$STYLES $2"; shift;;
	# temporary filename (without .eps suffix)
	*)    tmp=$1;;
	esac
	shift
done

# in case a different directory is given:
datadir="`dirname $tmp`"
if [ ! -z "$datadir" ]
then
	tmp="`basename $tmp`"
	cd "$datadir"
fi

# target TeX file and temporary new file
tex=$tmp.tex
texnew=$tmp.tex.new

# write new TeX file
echo '\documentclass[fleqn]{article}' > $texnew
echo '\usepackage[latin1]{inputenc}' >> $texnew
for sty in $STYLES
do
	echo "\usepackage{$sty}" >> $texnew
done
echo '\begin{document}' >> $texnew
echo '\thispagestyle{empty}' >> $texnew
echo '\mathindent0cm' >> $texnew
echo '\parindent0cm' >> $texnew
cat >> $texnew
echo '\end{document}' >> $texnew

# exit when no change
test -e $tmp.eps && test -e $tex && diff $tex $texnew >/dev/null 2>&1 && {
	/bin/rm -f $texnew
	exit 0; }

# otherwise process TeX file
mv $texnew $tex
latex $tex > /dev/null 2> /dev/null
dvips -q -E $tmp.dvi -o $tmp.eps
/bin/rm -f $tmp.log $tmp.dvi $tmp.aux
