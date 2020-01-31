#!/bin/sh
#
# The script is originary contributed by
# Sylvain Pion <Sylvain.Pion@sophia.inria.fr>
# 	modified by Youjiro UO to support of eqn format formula
#   modifictaions by Christoph Dalitz:
#     - caching mechanism added
#     - support for eqn/eps files in different directory
#
# Expected usage:
#	%filter "eqn2eps.sh eqn1"
#	1 over sqrt {ax sup 2+bx+c}
#	%endfilter
#	%center, image "eqn1.eps" 0 400 400 1
#
# temporary filename (without .eps suffix)
tmp=$1

# in case a different directory is given:
datadir="`dirname $tmp`"
if [ ! -z "$datadir" ]
then
	tmp="`basename $tmp`"
	cd "$datadir"
fi

# target eqn file and temporary new file
eqn=$tmp.eqn
eqnnew=$tmp.eqn.new

# write new eqn file
echo '.EQN' > $eqnnew
echo '.EQ' >> $eqnnew
cat >> $eqnnew
echo '.EN' >> $eqnnew

# exit when no change
test -e $tmp.eps && test -e $eqn && diff $eqn $eqnnew >/dev/null 2>&1 && {
	/bin/rm -f $eqnnew
	exit 0; }

# otherwise process eqn file
mv $eqnnew $eqn
groff -e $eqn > $tmp.ps
ps2epsi $tmp.ps $tmp.eps
/bin/rm -f $tmp.ps
