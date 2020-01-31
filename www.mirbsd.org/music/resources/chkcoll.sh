#!/bin/mksh
# -*- mode: sh -*-
#-
# Copyright © 2017, 2018
#	mirabilos <t.glaser@tarent.de>
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
# Check for note collisions in a MuseScore score (first argument) by
# exporting it to MIDI, running a CSV-based analysis over it, produ‐
# cing a changed MIDI with collision markers *and* listing them.
#
# When called with -c, cleans up.
#
# Note: MuseScore should NOT be running at the same time!

# check shell we are running under
case x$KSH_VERSION in
x)
	echo >&2 "E: mksh is necessary to run this script"
	exit 255
	;;
x'@(#)MIRBSD KSH R'[5-9][0-9]*|x'@(#)MIRBSD KSH R'[1-9][0-9][0-9]*)
	;;
*)
	echo >&2 "E: a recent version of mksh is necessary to run this script"
	exit 255
	;;
esac

# initialisation
export LC_ALL=C.UTF-8
unset LANGUAGE

set -e
set -o pipefail
set +e

die() {
	print -ru2 -- "E: $*"
	exit 1
}

# check tools
v=0
for tool in musescore perl midicsv csvmidi; do
	x=$(whence -p "$tool") || x=
	[[ -n $x && -x $x ]] && continue
	print -ru2 -- "N: tool $tool not found"
	v=1
done
(( v )) && die install the necessary tools before continuing

# check options (cheap)
if [[ $1 = -c ]]; then
	cleanonly=1
	shift
else
	cleanonly=0
fi

# check argument
[[ -n $1 ]] || die no arguments given
[[ -s $1 ]] || die file "$1" does not exist

# convert to MIDI from MuseScore if necessary
fn=$(realpath "$1")
bn=${fn%.*}
rm -f "$bn".{chkcoll.csv,chkcoll.mid,csv,tmp}
if [[ $fn = *.msc[xz] ]]; then
	mn=$bn.mid
	rm -f "$mn"
	(( cleanonly )) && exit 0
	musescore -o "$mn" "$fn" || die musescore converter died with $?
	[[ -s $mn ]] || die musescore converter produced no output
elif [[ $fn = *.mid ]]; then
	mn=$fn
else
	die input file "$1" neither MuseScore nor MIDI
fi
(( cleanonly )) && exit 0

# convert to CSV
midicsv "$mn" "$bn.csv" || die midicsv died with $?
[[ -s $bn.csv ]] || die midicsv produced no output

# 1. Add the header to the temporary file for Perl
#    so it knows how to estimate crotchets for positioning
# 2. Scan for all MIDI port and note on/off events;
#    amend the latter’s channel number by the current port
# 3. Sort all note events by time and MIDI stream position
# 4. Insert separator followed by original CSV for output
# 5. Run Perl script to do some magic, outputting adjusted CSV

sed q <"$bn.csv" >"$bn.tmp" || die sed on CSV died
curport=0
grep -Fne Note_on_c -e Note_off_c -e MIDI_port <"$bn.csv" | \
    tr -s ':, ' '   ' | \
    while read streampos trk ts op ch note vel rest; do
	if [[ $op = MIDI_port ]]; then
		curport=$ch
	else
		# Note_on_c, Note_off_c
		print -r -- "$streampos $trk $ts $op $curport:$ch $note $vel" $rest
	fi
done | sort -nk3,3 -nk1 >>"$bn.tmp" || die grep/tr/amend/sort on CSV died
print -r -- = >>"$bn.tmp" || die WTF
cat "$bn.csv" >>"$bn.tmp" || die could not concatenate temporary file
perl -e '
	my %nptrk = ();		# now playing (track number it started)
	my %npts = ();		# now playing (time it started, for uniq)
	my %offts = ();		# last time the note was stopped
	my %errtrk = ();	# track+ts for errors
	my %errts = ();		# ts for errors ⇒ their description
	my $haserr = 0;		# any?
	my $div;		# divider from header

	# process header line
	if (defined($_ = <>)) {
		chomp;
		my ($trk, $ts, $op, $fmt, $ntrk, $division) = split /,\s*/;
		$div = $division;
	}

	# process sorted input
	while (<>) {
		chomp;
		last if /^=$/;

		# split up input line
		my ($streampos, $trk, $ts, $op, $ch, $note, $vel) = split;
		# fixup note off events if necessary
		$vel = "0" if $op eq "Note_off_c";

		# check whether note already playing on that channel
		my $pident = "$ch,$note";
		if ($vel eq "0") {
			# off event; is the note playing in the first place?
			if (exists $nptrk{$pident}) {
				# yes ⇒ remove from now-playing hashtable
				delete $nptrk{$pident};
				$offts{$pident} = $ts;
			} else {
				# uniq away simultaneous off events
				next if ($offts{$pident} == $ts);
				# emit an error for the last on event’s ts
				my $origonts = $npts{$pident} or 0;
				my $origoffts = $offts{$pident};
				$errtrk{"$trk,$origonts"} = 1;
				$errts{$origonts} .= ", " if exists($errts{$origonts});
				$errts{$origonts} .= "t$trk c$ch n$note off($origoffts,$ts)";
				$haserr = 1;
			}
		} elsif (exists $nptrk{$pident}) {
			# uniq away simultaneous on events
			next if ($npts{$pident} == $ts);
			# we found a collision as it’s in the hashtable
			my $ltk = $nptrk{$pident};
			my $origonts = $npts{$pident};
			$errtrk{"$trk,$ts"} = 1;
			$errtrk{"$ltk,$origonts"} = 1;
			$errts{$origonts} .= ", " if exists($errts{$origonts});
			$errts{$origonts} .= "t$trk c$ch n$note on($origonts,$ts)";
			$haserr = 1;
		} else {
			# on event, add to now-playing hashtable
			$nptrk{$pident} = $trk;
			$npts{$pident} = $ts;
		}
	}

	# output modified CSV: extra comment header
	if ($haserr) {
		foreach $key (sort {$a <=> $b} keys %errts) {
			my $dk = ($key / $div) + 1;
			my $v = $errts{$key};
			print "# error at $key (crotchet #$dk): $v\n";
		}
		print "# Look for the CLASH texts!\n";
	} else {
		print "# no errors found\n";
	}
	# of course, completely blank likes are NOT ignored…
	print "#-\n";

	# pass original lines, inserting CLASH texts where necessary
	while (<>) {
		my ($trk, $ts, $op, $rest) = split /,\s*/;
		if (exists $errtrk{"$trk,$ts"} &&
		    ($op eq "Note_on_c" || $op eq "Note_off_c")) {
			# only once per collision
			delete $errtrk{"$trk,$ts"};
			print "$trk, $ts, Text_t, \"CLASH\"\n";
		}
		print $_;
	}
' <"$bn.tmp" >"$bn.chkcoll.csv" || die perl died with errorlevel $?
[[ -s $bn.chkcoll.csv ]] || die no output during perl-based conversion
# convert to MIDI
csvmidi "$bn.chkcoll.csv" "$bn.chkcoll.mid" || die csvmidi died with $?
[[ -s $bn.chkcoll.mid ]] || die died produced no output

cat <<-EOF

	This tool helps you find colliding notes within a score.
	Note: cross-staff beams with ties can’t be worked around.
	$(sed -n '1,/^#-$/p' <"$bn.chkcoll.csv" | sed -e '$d')

	Check part of https://musescore.org/en/node/207346 and
	its related issues for background information. Above you
	will see either “no errors found”, which indicates that
	no action must be taken, or a number of MIDI tick offsets
	which have colliding notes (two or more) along with the
	offset in crotchets (quarter notes). To debug this you
	can open a MIDI generated from this script with
	\$ musescore ${bn@Q}.chkcoll.mid
	then look for “CLASH” marker text (below staves) within,
	or load the original score in extra mode with
	\$ musescore -e ${fn@Q}
	and use the debugger (right-click on a note, “Debugger”,
	go up to the parent “Chord” in the left panel, the group
	“ChordRest” field “tick” (in the right panel) contains a
	value you can manually compare with those displayed above.
	There’s currently no way to jump forthright to an arbitrary
	tick position in MuseScore — sorry about that.
EOF

exit 0
