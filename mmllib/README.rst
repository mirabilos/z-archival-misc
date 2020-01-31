MMLlib - Modern library for handling Music Macro Language
=========================================================

About
-----

MMLlib is a pure Python implementation of functionality related to the
`Music Macro
Language <https://en.wikipedia.org/wiki/Music_Macro_Language>`__ as
implemented by Microsoft® GW-BASIC® and compatibles, which is its most
common form, also implemented by the PC speaker driver in Linux and
`BSD <http://www.mirbsd.org/man4/spkr>`__, with a number of extensions
and changes:

-  ``|`` denotes a bar line, ignored by the Linux/BSD driver
-  support for multiple (parallel) instrument tracks
-  a per-file header with work metadata (optional)
-  lines starting with a ``#`` are comments
-  the alias ``~`` is not supported

The library currently contains functions to:

-  parse an (extended) MML file into metadata and individual tracks
-  a duration estimate and the number of tracks are added to the metadata
-  return a list of tuples (frequency, duration) to play (in a threaded
   interpreter, such as Floppi-Music)
-  bar lines are indicated separately as integer ``1``
-  return an event-oriented list of measures / bars across all tracks;
   each measure has a duration and a list of time-fixed tone on/off
   events; facilitates playing in non-threaded interpreters and,
   possibly, MIDI export
-  export (extended) MML to MusicXML
-  which can be imported by e.g. `MuseScore <https://musescore.org/>`__
   to…

   -  double-check the MML score for mistakes in a visual representation
   -  play, arrange, etc. the music
   -  beautify the score to print it as sheet music
   -  export into other formats or share on their website

-  export (currently only the first track of) MML to an NXC program
-  check tracks for synchronisation error
-  missing tracks, i.e. with 0 bars
-  missing bars (can only be detected at the end, of course)
-  missing notes within a bar, relative to other tracks

Examples
--------

Some example extended MML files are contained within the examples/
directory, in lieu of better documentation for the extended format, in
addition to the `MML format documentation
<http://www.mirbsd.org/man4/spkr>`__.

Projects using MMLlib
---------------------

:Floppi-Music:
    `Floppi-Music <https://edugit.org/nik/floppi-music>`__ has
    MML as input format for floppy drive music on Raspberry Pi and uses
    MMLlib for processing. Floppi-Music is also the origin of MMLlib
    from before it was spun off into a separate project of its own.
