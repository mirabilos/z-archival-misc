# ~*~ coding: utf-8 ~*~
#-
# Copyright © 2013, 2017
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

""" Code to control a floppy drive for floppy music and an engine of
musical floppy drives.
"""

from threading import Thread, Event
from time import sleep

class MusicalFloppy(Thread):
    """ Class for controlling one musical floppy drive.

    This class controls one floppy drive in its own thread,
    so several drives can be used in parallel.
    """

    def __init__(self, gpio, pins, engine):
        """ Set up the drive thread.

         gpio - reference to the GPIO object in use
         pins - A tuple defining the pair of (direction, step) pins
        """

        Thread.__init__(self)

        # Holds reference to the GPIO object in use
        self._gpio = gpio
        # Holds the native pin number of the GPIO pin connected to the
        # DIRECTION pin of the floppy interface.
        self._pin_direction = pins[0]
        # Holds the native pin number of the GPIO pin connected to the
        # STEP pin of the floppy interface.
        self._pin_step = pins[1]
        # Queue of tones to play, containing tuples of (frequency, duration)
        self._playqueue = []
        # Exit flag for the thread.
        self._exit = False
        # Reference to the FloppyMusicEngine
        self._engine = engine
        self._track = 0
        # An event used by the engine to wake the thread up
        self.wakeup = Event()

    def run(self):
        """ Run the drive thread

        Main loop function for the drive thread. It will set up the drive, then
        start playing tones from the _playqueue list.
        """

        # Explicitly set pins high
        self._gpio.high(self._pin_direction)
        self._gpio.high(self._pin_step)

        # Go to home position, which is the middle track
        self.home()

        # Wait for initial synchronisation mark
        self._engine.syncmark(self)
        self.wakeup.wait()
        self.wakeup.clear()
        # Give the engine some time to fill our playlist
        sleep(0.01)

        while not self._exit:
            if self._playqueue:
                tone = self._playqueue.pop(0)
                if tone == 1:
                    self._engine.syncmark(self)
                    self.wakeup.wait()
                    self.wakeup.clear()
                else:
                    self.tone(tone[0], tone[1])
            else:
                self._engine.finished(self)
                break

    def stop(self):
        """ Stop this thread (set the exit flag). """

        self._exit = True

    def tracks(self, count):
        """ Move the floppy head a certain number of tracks

         count - the number of tracks to move
        """

        for _ in range(count):
            self._gpio.toggle(self._pin_step)
            sleep(.002)
            self._gpio.toggle(self._pin_step)
            if self._engine._mode == 1:
                self._track += 1
                if self._track >= 80:
                    self._gpio.toggle(self._pin_direction)
                    self._track = 0
            else:
                self._gpio.toggle(self._pin_direction)

    def home(self):
        """ Move to home position, which is the middle track

        This method moves the head to the home position.
        Currently, this is the middle track (40), but this may
        change as we advance the floppy music quality.
        """

        # Assume we are at track 0, this doesn't matter now
        self._track = 0
        # Move 80 tracks, just to be sure
        self.tracks(80)
        # Toggle direction
        self._gpio.toggle(self._pin_direction)
        # Move 40 tracks back
        self.tracks(40)
        self._track = 40

    def vibrate(self, interval, count, rest):
        """ Shake the head with a certain interval

        Moves the head back and forth some times, pausing in between.
        You can make sounds at certain frequencies by setting the interval
        to 1/freq. Adjust the duration of the vibration by setting
        the count to freq*duration.

         interval - the pause between movements, in seconds
         count - how often to move the head
         rest - how long to wait to compensate rounding problems
        """

        for _ in range(count):
            self._gpio.toggle(self._pin_step)
            sleep(interval)
            self._gpio.toggle(self._pin_step)
            if self._engine._mode == 1:
                self._track += 1
                if self._track >= 80:
                    self._gpio.toggle(self._pin_direction)
                    self._track = 0
            else:
                self._gpio.toggle(self._pin_direction)
        sleep(rest)

    def tone(self, freq, duration):
        """ Play a certain frequency, for duration seconds

        This method wraps the vibrate method with another form of arguments,
        more convenient for playing notes.

         freq - the frequency of the desired tone, in Hertz
         duration - duration of the tone, in seconds
        """

        if duration == 0:
            return

        if freq == 0:
            sleep(duration)
        else:
            self.vibrate(1.0/freq, int(freq*duration), duration-int(freq*duration)/freq)

    def play(self, tone):
        """ Add a tone to the play queue

        This method adds a tone to the _playqueue, so the main loop
        will pick it up and play it.

         tone - a tuple of (frequency, duration)
        """

        if isinstance(tone, list):
            self._playqueue.extend(tone)
        else:
            self._playqueue.append(tone)

class MusicalFloppyEngine(Thread):
    """ Class for controlling a synchronised set of floppy drives. """

    def __init__(self, gpio, pins, mode=0):
        """ Set up the engine thread.

         gpio - reference to the GPIO object in use
         pins - a list of (direction, step) pin pairs for the drives
         mode - mode to put the engine in. 0 for loud, 1 for visual
        """

        Thread.__init__(self)

        # Setup all the drives
        self._drives = []
        for pair in pins:
            self._drives.append(MusicalFloppy(gpio, pair, self))

        # Queue of tracks to play, containing lists of tuples of (frequency, duration)
        self._playqueue = []
        # List of all drives used in the current playback
        self._active = []
        # List of drives waiting at syncmarks
        self._waiting = []
        # List all drives that finished playing
        self._finished = []
        # loud or visual mode
        self._mode = mode
        # Exit flag for the thread.
        self._exit = False

    def run(self):
        """ Run the engine thread

        Main loop function for the engine thread. It will set up the drives, then
        start playing tracks from the _playqueue list.
        """

        # Start all the drive threads
        for drive in self._drives:
            drive.start()

        # Issue a short pause after all drives have set up themselves
        # (are at the home track) and synchronise their wake-up event
        sleep(0.5)
        for drive in self._waiting:
            drive.wakeup.set()
        self._waiting = []

        # Main loop
        while not self._exit:
            if self._playqueue:
                # Get one track from the queue
                track = self._playqueue.pop(0)

                # Count tracks
                trackn = 0
                self._active = []
                while track:
                    # Assign track to drive
                    self._active.append(self._drives[trackn])
                    self._drives[trackn].play(track.pop(0))
                    trackn += 1

            # Wait for all drives to stop playing
            if len(self._finished) == len(self._drives):
                break

            # Check whether all drives have reached a syncmark
            if len(self._waiting) == len(self._active):
                for drive in self._waiting:
                    drive.wakeup.set()
                self._waiting = []

        # Signal all floppy drive threads to stop
        for drive in self._drives:
            drive.stop()

        # Wait for all drive threads to end
        for drive in self._drives:
            drive.join()

    def stop(self):
        """ Stop this thread (set the exit flag). """

        self._exit = True

    def play(self, track):
        """ Add a track to the play queue

        This method adds a track to the _playqueue, so the main loop
        will pick it up and play it.

         track - a list of lists of (frequency, duration) tuples
        """

        self._playqueue.append(track)

    def syncmark(self, drive):
        """ Drive has reached a syncmark, register

        This method is called by member drives when they reach a
        syncmark. The method appends the drive to the list of waiting
        drives.

         drive - the relevant drive
        """

        self._waiting.append(drive)

    def finished(self, drive):
        """ Drive has reached the end

        This method is called by member drives when they reach the end
        of the playqueue. The method appends the drive to the list of
        finished drives.

         drive - the relevant drive
        """

        self._finished.append(drive)
