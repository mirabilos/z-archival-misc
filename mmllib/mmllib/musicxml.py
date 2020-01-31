# ~*~ coding: utf-8 ~*~
#-
# Copyright © 2016
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

""" Functions to convert an mmllib playlist into a MusicXML document """

from copy import deepcopy
from xml.dom import minidom

try:
    from math import gcd
except ImportError:
    def gcd(num_a, num_b):
        while num_b:
            (num_a, num_b) = (num_b, num_a % num_b)
        return num_a

from .mml import MML_NOTE2PITCH
from .parser import mml_file, mml_file_meta

try:
    str = unicode
except:
    pass

## MusicXML note lengths, for display
_MUSICXML_NOTETYPES = {64: u'64th',
                       32: u'32nd',
                       16: u'16th',
                       8: u'eighth',
                       4: u'quarter',
                       2: u'half',
                       1: u'whole'}

def _overridden_play(res, bpm, art, note, length, dots, extra):
    """ Overridden function to pass to MML core

    This function is supposed to be passed as second argument
    to mml_file to collect the detailed data necessary for an
    export in the playlist.

    Same parameters as mmllib.mml.mml_play.
    """

    res.append((bpm, art, length, dots, extra))

def _create_xml_doc(meta, staves):
    """ Create a MusicXML document from collected data and metadata

    Exports metadata and staves as MusicXML document.

    meta - the metadata, as returned by mml_file_meta()
    staves - the collected tracks, as returned by mml_file() with _overridden_play

    Returns the MusicXML document as MiniDOM document.
    """

    # Use deep copies because we change and delete entries
    meta = deepcopy(meta)

    # create MusicXML document
    mdi = minidom.getDOMImplementation('')
    doc = mdi.createDocument(None, 'score-partwise',
                             mdi.createDocumentType('score-partwise',
                                                    '-//Recordare//DTD MusicXML 3.0 Partwise//EN',
                                                    'http://www.musicxml.org/dtds/partwise.dtd'))
    score = doc.documentElement
    score.setAttribute('version', '3.0')

    # carry over floppi.music-specific metadata
    if 'title' in meta:
        elem = doc.createElement('movement-title')
        elem.appendChild(doc.createTextNode(meta['title']))
        score.appendChild(elem)
        del meta['title']
    tmpel = doc.createElement('identification')
    if 'composer' in meta:
        elem = doc.createElement('creator')
        elem.setAttribute('type', 'composer')
        elem.appendChild(doc.createTextNode(meta['composer']))
        tmpel.appendChild(elem)
        del meta['composer']
    if 'lyrics' in meta:
        elem = doc.createElement('creator')
        elem.setAttribute('type', 'poet')
        elem.appendChild(doc.createTextNode(meta['lyrics']))
        tmpel.appendChild(elem)
        del meta['lyrics']
    if 'arranger' in meta:
        elem = doc.createElement('creator')
        elem.setAttribute('type', 'arranger')
        elem.appendChild(doc.createTextNode(meta['arranger']))
        tmpel.appendChild(elem)
        del meta['arranger']
    if 'translator' in meta:
        elem = doc.createElement('creator')
        elem.setAttribute('type', 'translator')
        elem.appendChild(doc.createTextNode(meta['translator']))
        tmpel.appendChild(elem)
        del meta['translator']
    if 'artist' in meta:
        elem = doc.createElement('creator')
        elem.appendChild(doc.createTextNode(meta['artist']))
        tmpel.appendChild(elem)
        del meta['artist']
    if 'copyright' in meta:
        elem = doc.createElement('rights')
        elem.appendChild(doc.createTextNode(meta['copyright']))
        tmpel.appendChild(elem)
        del meta['copyright']
    tmpex = doc.createElement('encoding')
    if 'encoder' in meta:
        elem = doc.createElement('encoder')
        elem.appendChild(doc.createTextNode(meta['encoder']))
        tmpex.appendChild(elem)
        del meta['encoder']
    elem = doc.createElement('software')
    elem.appendChild(doc.createTextNode(u'MMLlib by Natureshadow, Creeparoo, and mirabilos'))
    tmpex.appendChild(elem)
    tmpel.appendChild(tmpex)
    if 'source' in meta:
        elem = doc.createElement('source')
        elem.appendChild(doc.createTextNode(meta['source']))
        tmpel.appendChild(elem)
        del meta['source']
    tmpex = doc.createElement('miscellaneous')
    meta["duration"] = int(meta["duration"])
    for tmp in sorted(meta):
        elem = doc.createElement('miscellaneous-field')
        elem.setAttribute('name', tmp)
        elem.appendChild(doc.createTextNode(str(meta[tmp])))
        tmpex.appendChild(elem)
    tmpel.appendChild(tmpex)
    score.appendChild(tmpel)

    # required metadata
    tmpel = doc.createElement('part-list')
    for trkno in range(1, len(staves) + 1):
        score_part = doc.createElement('score-part')
        score_part.setAttribute('id', 'P' + str(trkno))
        part_name = doc.createElement('part-name')
        part_name.appendChild(doc.createTextNode(u'Track ' + str(trkno)))
        score_part.appendChild(part_name)
        tmpel.appendChild(score_part)
    score.appendChild(tmpel)

    # figure out which duration to use
    notelens = 4
    for trkno in range(1, len(staves) + 1):
        for ply in staves[trkno - 1]:
            if isinstance(ply, tuple):
                dottedlen = ply[2]
                for tmp in range(0, ply[3]):
                    dottedlen *= 2
                # calculate lowest common multiple
                notelens = (notelens * dottedlen) // gcd(notelens, dottedlen)
    divisions = notelens // 4

    # add individual staves
    for trkno in range(1, len(staves) + 1):
        staff = staves[trkno - 1]
        trknode = doc.createElement('part')
        trknode.setAttribute('id', 'P' + str(trkno))

        # attribute node, once per part, located in the first bar
        tmpel = doc.createElement('attributes')
        elem = doc.createElement('divisions')
        elem.appendChild(doc.createTextNode(str(divisions)))
        tmpel.appendChild(elem)
        # use treble clef by default
        tmpex = doc.createElement('clef')
        elem = doc.createElement('sign')
        elem.appendChild(doc.createTextNode(u'G'))
        tmpex.appendChild(elem)
        elem = doc.createElement('line')
        elem.appendChild(doc.createTextNode(u'2'))
        tmpex.appendChild(elem)
        tmpel.appendChild(tmpex)

        # "current" bar node and number (first, here)
        barno = 1
        barnode = doc.createElement('measure')
        barnode.setAttribute('number', str(barno))
        barnode.appendChild(tmpel)
        # hack to never end on a bar line – end needs special love
        while len(staff) > 0 and staff[-1] == 1:
            staff.pop()

        # now iterate through the staff
        bpm = -1
        lastslur = None
        inslur = 0
        for ply in staff:
            # finish a bar?
            if ply == 1:
                trknode.appendChild(barnode)
                # force re-init on next note
                barnode = None
                continue

            # start a new bar?
            if barnode is None:
                barno += 1
                barnode = doc.createElement('measure')
                barnode.setAttribute('number', str(barno))

            # tempo change?
            if bpm != ply[0]:
                tmpel = doc.createElement('direction')
                tmpex = doc.createElement('metronome')
                elem = doc.createElement('beat-unit')
                elem.appendChild(doc.createTextNode(u'quarter'))
                tmpex.appendChild(elem)
                elem = doc.createElement('per-minute')
                elem.appendChild(doc.createTextNode(str(ply[0])))
                tmpex.appendChild(elem)
                elem = doc.createElement('direction-type')
                elem.appendChild(tmpex)
                tmpel.appendChild(elem)
                elem = doc.createElement('sound')
                elem.setAttribute('tempo', str(ply[0]))
                tmpel.appendChild(elem)
                barnode.appendChild(tmpel)

            # unpack and convert raw note to pitch (best guess)
            (bpm, art, length, ndots, extra) = ply
            if not isinstance(ply, tuple) and extra != -1:
                extra = MML_NOTE2PITCH[extra]

            # convert to MusicXML
            tmpel = doc.createElement('note')
            if extra == -1:
                tmpex = doc.createElement('rest')
            else:
                tmpex = doc.createElement('pitch')
                elem = doc.createElement('step')
                elem.appendChild(doc.createTextNode(str(extra[1])))
                tmpex.appendChild(elem)
                if extra[2] != u'♮':
                    elem = doc.createElement('alter')
                    if extra[2] == u'♭':
                        elem.appendChild(doc.createTextNode(u'-1'))
                    elif extra[2] == u'♯':
                        elem.appendChild(doc.createTextNode(u'1'))
                    tmpex.appendChild(elem)
                elem = doc.createElement('octave')
                elem.appendChild(doc.createTextNode(str(extra[0] + 2)))
                tmpex.appendChild(elem)
            tmpel.appendChild(tmpex)
            # if notelens calculation is correct, dottedlen is always integer
            dottedlen = notelens / length
            for tmp in range(0, ndots):
                dottedlen *= 1.5
            elem = doc.createElement('duration')
            elem.appendChild(doc.createTextNode(str(int(dottedlen))))
            tmpel.appendChild(elem)
            if length in _MUSICXML_NOTETYPES.keys():
                elem = doc.createElement('type')
                elem.appendChild(doc.createTextNode(_MUSICXML_NOTETYPES[length]))
                tmpel.appendChild(elem)
            # order is important!
            for tmp in range(0, ndots):
                tmpel.appendChild(doc.createElement('dot'))
            # articulation - is this a note (no pause)?
            if extra != -1:
                # not a pause then… ML started on the previous note?
                if inslur == 1:
                    # start Binde-/Haltebogen
                    elem = doc.createElement('slur')
                    elem.setAttribute('type', 'start')
                    lastslur.appendChild(elem)
                # ML while slur is active?
                if inslur > 0 and art == 'L':
                    # just cache this node and go on
                    lastslur = tmpel
                    inslur = 2
                # ML start?
                if inslur == 0 and art == 'L':
                    # cache notations node, filled in next note
                    lastslur = doc.createElement('notations')
                    tmpel.appendChild(lastslur)
                    inslur = 1
                # ML end on this note and/or MS? => common XML element
                if (inslur > 0 and art != 'L') or art == 'S':
                    tmpex = doc.createElement('notations')
                    tmpel.appendChild(tmpex)
                # ML end on this note?
                if inslur > 0 and art != 'L':
                    # end Binde-/Haltebogen
                    elem = doc.createElement('slur')
                    elem.setAttribute('type', 'stop')
                    tmpex.appendChild(elem)
                    lastslur = None
                    inslur = 0
                # MS for this note?
                if art == 'S':
                    # create staccato dot
                    elem = doc.createElement('articulations')
                    elem.appendChild(doc.createElement('staccato'))
                    tmpex.appendChild(elem)
                # MN does not need any extra handling
            else:
                # ML started on the previous note, detached legato
                if inslur == 1:
                    elem = doc.createElement('articulations')
                    elem.appendChild(doc.createElement('detached-legato'))
                    lastslur.appendChild(elem)
                    lastslur = None
                    inslur = 0
                # ML started before the previous note, end Binde-/Haltebogen
                if inslur == 2:
                    tmpex = doc.createElement('notations')
                    elem = doc.createElement('slur')
                    elem.setAttribute('type', 'stop')
                    tmpex.appendChild(elem)
                    lastslur.appendChild(tmpex)
                    lastslur = None
                    inslur = 0
            barnode.appendChild(tmpel)
        # any unfinished ML? detached legato or end Binde-/Haltebogen
        if inslur == 1:
            elem = doc.createElement('articulations')
            elem.appendChild(doc.createElement('detached-legato'))
            lastslur.appendChild(elem)
        if inslur == 2:
            tmpex = doc.createElement('notations')
            elem = doc.createElement('slur')
            elem.setAttribute('type', 'stop')
            tmpex.appendChild(elem)
            lastslur.appendChild(tmpex)
        # finish staff
        tmpex = doc.createElement('barline')
        elem = doc.createElement('bar-style')
        elem.appendChild(doc.createTextNode(u'light-heavy'))
        tmpex.appendChild(elem)
        barnode.appendChild(tmpex)
        trknode.appendChild(barnode)
        score.appendChild(trknode)

    return doc

def _create_xml_file(meta, staves):
    """ Export collected data and metadata as MusicXML

    Exports metadata and staves as MusicXML document,
    rendered as an XML file.

    The XML file is minified; if human inspection is required,
    pipe through “xmlstarlet fo”. Do n̲o̲t̲ use .toprettyxml() as
    it adds extra format-violating whitespace to text nodes!

     meta - the metadata, as returned by mml_file_meta()
     staves - the collected tracks, as returned by mml_file() with _overridden_play

    Returns the MusicXML document encoded as UTF-8 string
    """

    return _create_xml_doc(meta, staves).toxml("UTF-8")

def convert_mml_file(filename):
    """ Convert MML file to MusicXML

    Reads the MML file twice (once for metadata, once for data),
    converts it to MusicXML and renders it as XML file.

     filename - the MML file

    Returns the MusicXML document encoded as UTF-8 string.
    """

    meta = mml_file_meta(filename)
    staves = mml_file(filename, _overridden_play)
    return _create_xml_file(meta, staves)
