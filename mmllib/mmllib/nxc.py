# ~*~ coding: utf-8 ~*~
#-
# Copyright © 2017
#       mirabilos <m@mirbsd.org>
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

""" Functions to convert an mmllib playlist into an NXC program """

import re
from copy import deepcopy
from .mml import MML_NOTE2PITCH
from .parser import mml_file, mml_file_meta

def _nxc_meta(meta, fn):
    s = u"/*-\n * Automatically converted from " + fn + "\n * using MMLlib by Natureshadow, Creeparoo, and mirabilos\n *"

    # Use deep copies because we change and delete entries
    meta = deepcopy(meta)

    # carry over floppi.music-specific metadata
    if 'title' in meta:
        s = s + "\n * " + meta['title']
        del meta['title']
    if 'composer' in meta:
        s = s + "\n * by " + meta['composer']
        del meta['composer']
    if 'lyrics' in meta:
        s = s + "\n * Lyrics: " + meta['lyrics']
        del meta['lyrics']
    if 'arranger' in meta:
        s = s + "\n * Arranger: " + meta['arranger']
        del meta['arranger']
    if 'translator' in meta:
        s = s + "\n * Translator: " + meta['translator']
        del meta['translator']
    if 'artist' in meta:
        s = s + "\n * Artist: " + meta['artist']
        del meta['artist']
    if 'encoder' in meta:
        s = s + "\n * Encoder: " + meta['encoder']
        del meta['encoder']
    if 'source' in meta:
        s = s + "\n * Source: " + meta['source']
        del meta['source']
    if 'copyright' in meta:
        s = s + "\n *\n * " + meta['copyright']
        del meta['copyright']
    s = s + "\n *\n * +++ Further metadata +++"
    meta["duration"] = int(meta["duration"])
    for tmp in sorted(meta):
        s = s + (u"\n * %s: %s" % (tmp, meta[tmp]))
    s = s.replace('*/', '*\\') + "\n */\n\n"
    return s.encode("UTF-8")

def _nxc_track(trklist, name):
    s = "Tone " + name + "[] = {\n"
    for ply in trklist:
        if isinstance(ply, tuple):
            s = s + "\t{ " + str(int(ply[0] + .5)) + ", " + \
              str(int(ply[1] * 1000 + .5)) + " },\n"
        else:
            s = s + "\t/* barline */\n"
    s = s + "};\n"
    return s.encode("UTF-8")

def _create_nxc_mono(meta, staves, fn):
    hdr = _nxc_meta(meta, fn)
    trk = _nxc_track(staves[0], "melody")
    if 'title' in meta:
        npu = meta['title']
    else:
        npu = fn
    if 'composer' in meta:
        npu = npu + ' by ' + meta['composer']
        if 'lyrics' in meta:
            npu = npu + '/' + meta['lyrics']
        if 'arranger' in meta:
            npu = npu + '/' + meta['arranger']
    elif 'artist' in meta:
        npu = npu + ' by ' + meta['artist']
    if 'source' in meta:
        npu = npu + ' from ' + meta['source']
    if 'now playing' in meta:
        npu = meta['now playing']
    npe = ''.join([x == '\t' and ' ' or (x if ord(x) >= 32 and ord(x) < 127 else '') for x in list(npu)]).encode('UTF-8')
    drw = ("\nvoid drawFilename() {\n" + \
      '\n'.join(["\tTextOut(0, LCD_LINE%d, \"%s\");" % (n+1, npe[n*16:(n+1)*16].decode('UTF-8').replace('\\', '\\\\').replace('"', '\\"').rstrip()) for n in range(8)]) + \
      "\n}\n").encode("UTF-8")
    return hdr + trk + drw + '\n'.join([''.join([chunk == '    ' and '\t' or chunk for chunk in
      re.findall('    |.+', line[8:])]) for line in """
        task main() {
            ClearScreen();
            drawFilename();
            PlayTones(melody);
        }
        """.splitlines()[:-1]]).encode("UTF-8")

def convert_mml_track(filename):
    """ Convert first track of MML file to NXC

    Reads the MML file twice (once for metadata, once for data),
    converts it to an NXC program.

     filename - the MML file

    Returns the NXC program.
    """

    meta = mml_file_meta(filename)
    staves = mml_file(filename)
    return _create_nxc_mono(meta, staves, filename)
