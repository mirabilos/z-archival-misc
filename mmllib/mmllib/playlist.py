# ~*~ coding: utf-8 ~*~
#-
# Copyright © 2016
#       mirabilos <m@mirbsd.org>
# Copyright © 2013
#       Dominik George <nik@naturalnet.de>
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

""" Functions to deal with mmllib playlists, which are lists
containing frequency and duration tuples, and integer
entries for special tokens such as bar lines.

Playlist format

 A standard MMLlib playlist is an ordered list whose elements are
 either the integer 1 (denoting a bar line) or a tuple, normally
 containing (frequency, duration). Other members may be present.
 The playlist generation code in the mml module can be overridden,
 in which case a different format is possible; this module does
 not support such formats.

 It is not permitted to have a bar line at the beginning of a
 playlist, nor is it permitted to have two subsequent bar lines.
 It is recommended a playlist end with a bar line, but not required.
 A duration is a floating-point number, measured in seconds, and
 must be strictly greater than 0.
"""

def estimate_duration(track):
    """ Calculate playback length of a list of (frequency, duration) tuples

    This function looks at all the tuples in a playback list and estimates
    the playback duration by adding up the durations.

     track - the playlist

    Returns the estimated duration in seconds.
    """

    # Remove all non-tuples from the list; integer 1 is abused by mml_parse
    # for syncmarks; extract second entries of tuples and add them up and
    # return the result, all in one list comprehension -- I ❤ Python!
    return sum([x[1] for x in track if isinstance(x, tuple)])

def timedlist_duration(timedlist):
    """ Calculate playback length for a timedlist

    This function returns the sum of the length of all measures.

     timedlist - the timedlist

    Returns the length of the piece in seconds.
    """

    return sum([x[0] for x in timedlist])

def timedlist_ntracks(timedlist):
    """ Calculate amount of tracks a timedlist uses

    This function returns the highest track number addressed,
    and the amount of tracks that actually are addressed.

     timedlist - the timedlist

    Returns tuple(highesttrack, ntracks).
    """

    addressed = {}
    for measure in timedlist:
        for event in measure[1]:
            for action in event[1]:
                addressed[action[0]] = True
    tracknos = sorted(addressed.keys())
    return (tracknos[-1], len(tracknos))

def timedlist_flatten(timedlist):
    """ Flatten a timedlist

    Converts a multi-measure timedlist into a timed document,
    i.e. a timedlist with only one measure.

     timedlist - the timedlist

    Returns a new timedlist as flattened document.
    """

    newlen = 0
    newev = []

    for measure in timedlist:
        for ev in measure[1]:
            newev.append((newlen + ev[0], ev[1]))
        newlen += measure[0]

    newmeasure = (newlen, newev)
    return (newmeasure,)

def convert_to_timedlist(staves):
    """ Convert playlist to timedlist

    Converts a list of playlists (staves) to a (timedlist, errors) tuple,
    with them being:

     timedlist: sequence of measures
     measure: sequence of (length, list of events)
     length: length of this measure in seconds (float)
     event: sequence of (offset, list of actions), ascending by offset
     offset: time relative to the start of this measure, in seconds (float)
     action: sequence of (track, frequency), ascending by track number
     track: 1-based track number
     frequency: 0 means stop playing
     errors: sequence of (track, barno, delta)
     barno: 1-based number of problematic bar or 0 to indicate a missing track
     delta: amount by which the bar in this track is shorter than the maximum
            in seconds, or 0, if the bar isn’t present in this track

    This gives:

     ([(0.8, [(0, [(1, 440), …]), (0.7, [(1, 0), …])]), …], []) on success
     ([], [(2, 0, 0), (3, 1, 0.0125), (3, 3, 0), …]) on failure

    Example input for these:
      • success: t75o2mna…|
      • failure: track 1 with 3 bars, track 2 with empty playlist,
                 track 3 with only 2 bars, the first of which is
                 0.0125 seconds shorter than bar 1 in track 0

    Note that both can be returned, i.e. an error is not fatal

     staves - list of playlists (modified to end with a bar line)

    Returns tuple(timedlist, errors).
    """

    # return values
    timedlist = []
    errors = []

    # sanitise input first
    for (trkno, staff) in enumerate(staves):
        while len(staff) > 0 and staff[-1] == 1:
            staff.pop()
        if len(staff) == 0:
            errors.append((trkno + 1, 0, 0))
        else:
            staff.append(1)
    if len(errors) > 0:
        return (timedlist, errors)

    # now we have a list of staves with at least one bar each,
    # a bar line terminating each – collect events and length,
    # for each staff separately at first
    stavev = []
    nstaves = len(staves)
    nbars = 0
    for trkno in range(0, nstaves):
        stev = []
        # currently open bar
        curlen = 0.
        curevt = {}
        curfreq = 0
        for token in staves[trkno]:
            if token == 1:
                # bar line
                stev.append((curlen, curevt))
                curlen = 0.
                curevt = {}
                continue
            # note or rest
            if curfreq != token[0]:
                curfreq = token[0]
                curevt[curlen] = curfreq
            curlen += token[1]
        # at the end we’re guaranteed to have just finished a bar
        # deal with any left-over sound
        if curfreq > 0:
            stev[-1][1][stev[-1][0]] = 0
        # finish the staff
        if len(stev) > nbars:
            nbars = len(stev)
        stavev.append(stev)

    # now we have a list of events for all staves separately:
    # stavev = [staff1, staff2, …]
    # staffN = [bar1, bar2, …]
    # barN = (len, {offset: frequency, …})

    for barno in range(0, nbars):
        # highest bar length
        mslen = max([len(stavev[trkno]) > barno and stavev[trkno][barno][0] or 0
                     for trkno in range(0, nstaves)])
        # events
        msev = {}
        # merge tracks one by one
        for trkno in range(0, nstaves):
            if not len(stavev[trkno]) > barno:
                errors.append((trkno + 1, barno + 1, 0))
                continue
            trkbar = stavev[trkno][barno]
            # check length (against an epsilon value)
            if mslen - trkbar[0] > 0.0001:
                errors.append((trkno + 1, barno + 1, mslen - trkbar[0]))
            # merge events
            for tbev in trkbar[1].keys():
                ihatefloatingpoint = int(tbev * 100000) / 100000.
                if not ihatefloatingpoint in msev:
                    msev[ihatefloatingpoint] = []
                msev[ihatefloatingpoint].append((trkno + 1, trkbar[1][tbev]))
        # get them into a list
        msevkeys = sorted(msev.keys())
        evlist = []
        for evkey in msevkeys:
            evlist.append((evkey, msev[evkey]))
        # finish this measure
        timedlist.append((mslen, evlist))

    return (timedlist, errors)
