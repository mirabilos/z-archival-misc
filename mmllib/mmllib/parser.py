# ~*~ coding: utf-8 ~*~
#-
# Copyright © 2016
#       mirabilos <m@mirbsd.org>
# Copyright © 2013, 2016
#       Dominik George <nik@naturalnet.de>
# Copyright © 2013
#       Eike Tim Jesinghaus <eike@naturalnet.de>
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

""" Functions to parse extended MML files into metadata and playlists """

from __future__ import with_statement

import codecs

from .mml import mml
from .playlist import estimate_duration

def mml_file(path, _play=None, _barline=None):
    """ Parse a file in the music macro language

    The playlist returned is ordinarily a list of tracks,
    i.e. a list of lists of (frequency, duration) tuples.

    This changes depending on the override function.

     path - the path to the MML file
     _play - the override play function to pass to mml()
     _barline - the override barline function to pass to mml()

    Returns the playlist.
    """

    vstrings = []
    vlists = []
    vcount = 0

    with codecs.open(path, "r", "UTF-8") as mmlfile:
        for line in mmlfile:
            if line.strip().startswith("#"):
                continue
            elif line.strip() == "":
                vcount = 0
            else:
                if len(vstrings) <= vcount:
                    vstrings.append("")
                vstrings[vcount] += line.strip()
                vcount += 1

    for vstring in vstrings:
        vlists.append(mml(vstring, _play, _barline))

    return vlists

def mml_file_meta(path):
    """ Parse a file in the music macro language and return metadata

     path - the path to the MML file

    Returns a dictionary of metadata, with lowercase keys
    """

    state = 0
    vcount = 0
    meta = {}

    with codecs.open(path, "r", "UTF-8") as mmlfile:
        for line in mmlfile:
            if state == 0:
                # Header fields
                if line.strip() == "":
                    state = 1
                elif line.strip().startswith("# ") and ":" in line:
                    parts = line.strip()[1:].split(":")
                    key = str(parts.pop(0))
                    value = ":".join(parts)

                    meta[key.strip().lower()] = value.strip()
            if state == 1:
                # Skip first empty line (header/body separator)
                if not line.strip() == "":
                    state = 2
            if state == 2:
                if line.strip().startswith("#"):
                    continue
                if line.strip() == "":
                    if not vcount:
                        # comment block preceding music
                        continue
                    # everything of interest has gone
                    break
                vcount += 1

    # Add discovered voice count to meta if not explicitly given
    if "voices" not in meta:
        meta["voices"] = vcount

    # Estimate duration of tracks and add maximum to meta if not
    # explicitly given
    if "duration" not in meta:
        meta["duration"] = max([estimate_duration(x) for x in mml_file(path)])

    # Ensure number types
    meta["duration"] = float(meta["duration"])
    meta["voices"] = int(meta["voices"])

    return meta
