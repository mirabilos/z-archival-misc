Description of the (extended) music macro language
==================================================

Based on http://www.antonis.de/qbebooks/gwbasman/play.html and
https://www.mirbsd.org/man4/spkr .

Symbols of MML
--------------

:A-G[#,+,-][length]:
    A-G are notes. # or + following a note produces a sharp; - produces a
    flat.

:L(n):

    Sets the length of each note. L4 is a quarter note, L1 is a whole note,
    and so on. n may be from 1 to 64. Length may also follow the note to
    change the length for that note only. A16 is equivalent to L16A. Default
    is L4.

:ML:
    Music legato. Each note plays the full period set by L.

:MN:
    Music normal. Each note plays seven-eighths of the time determined by L
    (length).

:MS:
    Music staccato. Each note plays three-quarters of the time determined by
    L.

:N(n):
    Play note n. n may range from 0 to 84. In the 7 possible octaves, there
    are 84 notes. n set to 0 (or omitted) indicates a rest.

:O(n):
    Octave 0 sets the current octave. There are 7 octaves (0 through 6).
    Default is 4. Middle C is at the beginning of octave 2. The special
    values L and N control octave tracking. L enables octave tracking, N
    disables it; with octave tracking enabled, jumps between notes are
    limited to 6 half-tones, otherwise, the octave is incremented or
    decremented accordingly. Octave tracking is disabled by default.

:P(n):
    Pause. n may range from 1-64; the current L value is used if omitted.

:T(n):
    Tempo. T sets the number of L4s in a minute. n may range from 32-255.
    Default is 120.

:. (period):
    A period after a note increases the playing time of the note by 3/2
    times the period determined by L (length of note) times T (tempo).
    Multiple periods can appear after a note, and the playing time is scaled
    accordingly. For example, A. will cause the note A to play one and half
    times the playing time determined by L (length of the note) times T (the
    tempo); two periods placed after A (A..) will cause the note to be
    played at 9/4 times its ascribed value; an A with three periods (A...)
    at 27/8, etc. Periods may also appear after a P (pause), and increase
    the pause length as described above.

:>:
    A greater-than symbol raises the current octave by one.

:<:
    A less-than symbol lowers the current octave by one.

:\|:
    Optionally used as a synchronisation mark for multi-track music. This is
    a proprietary extension in the Floppi-Music project.

Comments
--------

Lines starting with # are comments. At the beginning of the file,
comments may be used to encode metadata. This is yet to be specified.
The current implementation parses key/value pairs separated by a colon
and a space, strips both key and value, lower-cases the key and adds it
to a dictionary.

The MusicXML export currently specifically recognises these keys:

-  Title, Copyright, Encoder (person), Source
-  Composer, Lyrics, Arranger, Translator *xor* Artist (deprecated, only
   one)

Any other key is treated as miscellaneous field.

Voices
------

The voices of a song are interleaved. They are grouped per notation
system, and the notation systems are seperated by empty lines.
