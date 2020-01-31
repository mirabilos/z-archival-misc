#! /bin/sh
#
# The script is contributed by Sylvain Pion <Sylvain.Pion@sophia.inria.fr>.
#  Subject: (mgp-users 00071) Re: remarks against 1.04a
#
# Expected usage:
#	%filter "tex2eps.sh b"
#	My \TeX\ is nicer with $Magic$ Point
#	%endfilter
#	%image "b.eps" 250x200
#

# temporary filename (without .eps suffix)
tmp=$1

printf '\\nopagenumbers\n' > $tmp.tex
cat >> $tmp.tex
echo '\end' >> $tmp.tex
tex $tmp.tex > /dev/null 2> /dev/null
dvips -q -E $tmp.dvi -o $tmp.eps
/bin/rm -f $tmp.tex $tmp.log $tmp.dvi
