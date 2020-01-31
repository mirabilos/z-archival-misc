# ~*~ coding: utf-8 ~*~
#-
# Copyright © 2013, 2016, 2017
#       Dominik George <nik@naturalnet.de>
# Copyright © 2013, 2016, 2017
#       mirabilos <m@mirbsd.org>
#-
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

""" Script entry points for use by setuptools """

__all__ = [
    "mml2musicxml",
    "mml2nxcmono",
    "mmllint",
]

import argparse
import sys
from pprint import pprint

from .musicxml import convert_mml_file as convert_mml_file_to_musicxml
from .mml import mml
from .nxc import convert_mml_track as convert_mml_track_to_nxc
from .parser import mml_file
from .playlist import convert_to_timedlist, timedlist_duration, timedlist_ntracks

def _filter(func):
    # parse arguments
    aparser = argparse.ArgumentParser()
    aparser.add_argument("path", help="path to the file to convert")
    args = aparser.parse_args()

    if hasattr(sys.stdout, "buffer"):
        sys.stdout.buffer.write(func(args.path))
    else:
        sys.stdout.write(func(args.path))
    sys.stdout.write("\n")
    return 0

def mml2musicxml():
    """ Entry point for converting MML to MusicXML

    Returns 0 on success, >0 on error.
    """

    return _filter(convert_mml_file_to_musicxml)
mml2musicxml._shortdesc = 'converts MML to MusicXML'

def mml2nxcmono():
    """ Entry point for converting MML to MusicXML

    Returns 0 on success, >0 on error.
    """

    return _filter(convert_mml_track_to_nxc)
mml2nxcmono._shortdesc = 'converts first MML track to NXC'

def mmllint():
    """ Entry point for checking MML

    Returns 0 on success, >0 on error.
    """

    aparser = argparse.ArgumentParser()
    aparser.add_argument("path_or_mml", help="path to the file to check or mml string")
    aparser.add_argument("-q", "--quiet", help="only output final state", action="store_true")
    args = aparser.parse_args()
    if not args.path_or_mml.endswith(".mml"):
        playlist = [mml(args.path_or_mml)]
    else:
        playlist = mml_file(args.path_or_mml)
    timedlist, errors = convert_to_timedlist(playlist)

    if not args.quiet:
        pprint(timedlist)
    if len(timedlist):
        (highesttrack, ntracks) = timedlist_ntracks(timedlist)
        print("Tracks  : " + str(ntracks) + ", up to #" + str(highesttrack))
        print("Measures: " + str(len(timedlist)))
        print("Duration: " + str(timedlist_duration(timedlist)))
    if len(errors):
        for (track, barno, delta) in errors:
            if barno == 0:
                print("Error: missing track " + str(track))
            elif delta == 0:
                print("Error: track " + str(track) + " missing measure " + str(barno))
            else:
                print("Error: underfull measure (-" +
                      str(delta) + "s) in track " + str(track) +
                      ", measure " + str(barno))
        return 1
    else:
        print("No errors.")
        return 0
mmllint._shortdesc = 'checks MML for some errors'
