This directory contains various MuseScore sheets and MML scores.

If you are not mirabilos, feel free to look in the free/ subdirectory,
but you are *NOT* permitted to even look at the nonfree/ subdirectory,
which contains licenced music I am not allowed to publish freely.

I’m keeping this repository for my own work-in-progress and finished
pieces. Some aren’t completed yet and/or buggy, some need updating to
the latest standards and to incorporate what I learnt since working
on them last. Be warned and, more importantly, don’t complain to me,
it’s private after all even if some is publicly available.

The chor/ subdirectory contains files I cannot outright classify as
free, but which are available to members of the choirs I deal with.
No other licence is granted on them, although all of my own work on
them is covered by Ⓕ The MirOS Licence if protected by applicable law.

The misc/ subdirectory contains “others”, i.e. things that don’t fall
into those categories (such as exercise sheets, bug reproducers and
parodies). Take care, for you are not granted a licence on them either,
unless mentioned in the files — e.g. some are under CC-BY-NC, which is
a nōn-free licence but still one that permits several uses; some are
under the CPDL, which is a GNU GPLv2-inspired copyleft licence lacking
proper licence review, blessing by the appropriate institutions, and,
in some cases, decent wording. Most are at least distributable.

So, basically, free/ is free, misc/ may partially have usable content,
chor/ contains stuff usable for people in one of the choirs I deal with
only (except what can live in free/ or misc/), and don’t you even dare
look at nonfree/ however you can look at resources/ which holds extra
fonts etc.

In addition to that, my MuseScore style sheets and instrument files are
free for everyone to use, in case of doubt under Ⓕ The MirOS Licence.
The Mscore2 subdirectory contains files usable with MuseScore 2.x (all
released versions, tested with 2.3.2 and some with 2.0.3 or 2.1) while
the Mscore3 subdirectory has files for the 3.x (3.2+) release series.
* shortcuts.xml ⇒ default MuseScore shortcuts plus:
  ‣ - ⇒ ♭
  ‣ = ⇒ ♮
  ‣ # ⇒ ♯
  ‣ ( ⇒ add parenthesēs around
  ‣ ) ⇒ remove from beaming
  ‣ ` ⇒ (3.x) toggle-autoplace, because we already use =
* instr.xml ⇒ additional instruments (at the moment, only a rough
  draft of a Knabenalt and Counter-Tenor combined voice range, one
  for the typical church congregation, and copies of some generic
  instruments with separate MIDI channels)
* mirabilos.workspace ⇒ toolbar and palette setup, YMMV (3.x)
* vocal-free.mss and vocal-resv.mss ⇒ default score style to use,
  requires having the Free font families “Inconsolata zi4(varl,varqu)”
  from The MirOS Project and “Gentium” from SIL installed (which,
  alongside all other nōnstandard Free fonts used in my scores, are in
  the resources/ subdirectory in unmodified form) — they use a common
  rastral size/height №. 3, which is good for most instrumental/piano
  scores and songs, or choral works in suboptimal conditions (cramped,
  bad lighting, fast, …); choose the flavour based on whether a Free
  Sheet Music work will be produced or rights are managed; adjusting
  staffLowerBorder if you have more than one line of lyrics is expected

After creating a score with either .mss file selected in the preferences
it’s *still* necessary to load it (Style → Load Style…) in order for
MuseScore to apply all settings. (This is likely only relevant for 2.x)

For proper font rendering, the Freetype Autohinter must be disabled for
some fonts, enabled (with varying hint styles) for others — add my re‐
commended settings from resources/dot.fonts.conf to your ~/.fonts.conf
(old location) or ~/.config/fontconfig/fonts.conf (new path) file; the
fonts themselves go into ~/.fonts/ (old) or ~/.local/share/fonts/ (new).

Fonts:
• MuseScore fonts are (usually) free to use, embedding permitted: (2.x‥3.3)
  ‣ Bravura				(OFL 1.1)
  ‣ BravuraText				(OFL 1.1)
  ‣ Campania				(OFL 1.1) (≥3.3)
  ‣ FreeSans				(GPLv3 with font exception)
  ‣ FreeSerif				(GPLv3 with font exception)
  ‣ FreeSerifBold			(GPLv3 with font exception)
  ‣ FreeSerifBoldItalic			(GPLv3 with font exception)
  ‣ FreeSerifItalic			(GPLv3 with font exception)
  ‣ Gootville				(OFL 1.1)
  ‣ GootvilleText			(OFL 1.1)
  ‣ MScore				(GPL with font exception)
  ‣ MScoreBC				(OFL 1.1)
  ‣ MScoreTabulature			(OFL 1.1)
  ‣ MScoreText				(OFL 1.1)
  ‣ MuseJazz				(OFL 1.1) (3.x)
  ‣ MuseJazz Text			(OFL 1.1) (3.x)
  ‣ MuseJazz-Book			(OFL 1.1) (2.x)
  ‣ MuseJazz-Medium			(OFL 1.1) (2.x)
• Fonts from this repo are also mostly free to use:
  ‣ Gentium				(OFL 1.1)
  ‣ Gentium-Italic			(OFL 1.1)
  ⚠ Inconsolatazi4varl_qu-Bold		(Apache 2.0) !!! no embedding
  ‣ Inconsolatazi4varl_qu-Regular	(OFL 1.1)
  ‣ NotoSansSymbols-Regular		(OFL 1.1)
  ‣ UnifrakturMaguntia			(OFL 1.1)
• Embedded fonts can be detected by jupp(1)ing the PDF
  (lines around endcodespacerange show codepoints used)
• The default MuseScore soundfonts (FluidR3Mono_GM, MuseScore_General)
  both are MIT licenced, which also affects waveforms (WAV, MP3, …)
  generated using them. ⇒ https://musescore.org/en/node/270099
MuseScore copyright/licence characters *only* for meta:copyright:
which is rendered in Inconsolatazi4varl_qu and fallbacks:
• © Copyright
• €⃠ (U+20AC U+20E0) Creative Commons NC
• ɔ⃝ (U+0254 U+20DD) GNU Copyleft, CC-SA
• Ⓕ Copyfree; BSD Copycentre
• ⓪ CC-Zero
• C⃠ (‘C’ U+20E0) Public Domain / gemeinfrei
• F⃠ (‘F’ U+20E0) not Copyleft/Copyfree, e.g. CC-BY
The <sym>miscDoNotPhotocopy</sym><sym>miscDoNotCopy</sym> symbols are
in the F2 palette, second tab, under “Miscellaneous symbols”. They are
not suitable for the copyright metadata, only for the page footer.

Other tools for those who might like them:
• Mscore2/mscx-cln.xsl is a .mscx file cleaner: it will remove
  everything from the file except key and time signatures, accidentals,
  non-silenced voice 1 notes and rests, plus enough of the file header
  to make MuseScore 2.3.2 not crash upon opening (later I may add one
  removing all but time signatures for file structure)
• resources/mscz can decompress or create the typical MuseScore
  container files (mscz, mxl, mpal, workspace) using Info-ZIP
• resources/push* are my tools to publish the scores self-hosted and
  can also start MuseScore with only the “safe” fonts available
• resources/uprep is my tool preparing scores for upload: replace
  Gentium with Gentium Basic, which they carry, and convert to .mscz

Old or not yet ready to use scripts:
• resources/chkcoll.sh checks a score for note collisions (pre-2.2)
• Tuner/tuner.sh will eventually handle many sorts of tunings
  on a twelve-scale (i.e. enharmonics are tuned identically,
  for a Cembalo (harpsichord), not distinctly)

Fixup velocities: (2.x, 3.x)
  ppp  pp   p  mp  mf   f  ff fff
   16  33  49  64  80  96 112 126
      +17 +16 +15 +16 +16 +16 +14
  -17 -16 -15 -16 -16 -16 -14

Scientific pitch: (2.x)
Save the synthesiser settings once, so the configuration file at
~/.local/share/data/MuseScore/MuseScore2/synthesizer.xml gets
created by MuseScore. Then edit it and change //master/val@id=3
from presumably 440 to 430.539 which is as close as mscore gets
to tuning C4 to precisely 256 Hz at this moment.

‣‣‣ Formal licence:

This directory and everything in it is under collective-work Copyright
	© 2016, 2017, 2018, 2019 mirabilos
and published under Ⓕ The MirOS Licence.
Individual work copyright may apply. For any work protected by UrhG
(copyright) or neighbouring laws, my part is published under the
terms of Ⓕ The MirOS Licence, unless stated otherwise (in some cases,
dual licence terms permit choosing another licence for individual
works, (again) if licencing is applicable).

For my MuseScore style sheets and instrument files as well as the
utilities (fonts, shortcuts, scripts, …), for any MuseScore sheet
music produced utilising these, merely using the former to produce
said sheet music (and derivatives thereof) does not cause the latter
to fall under the terms of The MirOS Licence; this exception does
not however invalidate any other reasons why such sheet music might
be covered by The MirOS Licence (or any other applicable licence).

The subdirectory free/ contains various works, each under their own
licence, but in general, freely available to the public.

The subdirectory licences/ contains licence body texts that may be
referred from elsewhere. The subdirectory resources/ contains other
resources that, similarily, are referred from elsewhere, but which
all have their own (Free) licences.

For all other data in this directory, the following clause applies:

THIS IS UNPUBLISHED PROPRIETARY WORK OF mirabilos AND HIS CONTRIBUTORS,
AND OF THE RESPECTIVE ORIGINAL AUTHORS OF SUCH WORK. The copyright
notice above does not evidence any actual or intended publication of
such work. When all rights of others expire on any such work, I hereby
permit publication under the terms of The MirOS Licence, no matter
whether this occurs before or after my own death, as long as my own
rights in these works are extant.

This directory serves as backup and repository and as such is “for my
eyes only”. Password protection may be active to prevent casual viewing;
circumvention of password protection or knowledge of it does not result
in the right to access such data.

//mirabilos
