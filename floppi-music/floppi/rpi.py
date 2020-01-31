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

""" Wrapper for RPi.GPIO, reduced to what we need. """

from timeit import timeit

# Import original GPIO as _GPIO because we define our own GPIO later
from RPi import GPIO as _GPIO

class NoSuchGPIO(Exception):
    """ Exception to be thrown when trying to control a non-GPIO pin. """
    pass

class GPIO(object):
    """ GPIO class, reduced to what we need for Floppi-Music. """

    # All pins that are GPIO pins
    _gpios = (3, 5, 7, 8, 10, 11, 12, 13, 15, 16, 18, 19, 21, 22, 23, 24, 26)

    @staticmethod
    def _gpio_bench():
        """ GPIO benchmark function to pass to timeit

        Returns a reference to the real benchmark function for timeit.
        """

        _GPIO.setmode(_GPIO.BOARD)
        _GPIO.setwarnings(False)
        _GPIO.setup(3, _GPIO.OUT)

        def _gpio_bench_do():
            _GPIO.output(3, not _GPIO.input(3))

        return _gpio_bench_do

    def __init__(self):
        """ Initializes the whole GPIO header as output. """

        # Run GPIO benchmark
        self._gpiodelay = timeit('gpio_bench()',
                                 'from floppi.rpi import GPIO; gpio_bench = GPIO._gpio_bench()',
                                 number=100000) / 100000

        # Set GPIO mode to native Broadcom
        _GPIO.setmode(_GPIO.BOARD)

        # Setup all pins as output
        for pin in self._gpios:
            _GPIO.setup(pin, _GPIO.OUT)

    def high(self, pin):
        """ Set a selected pin high, by native board pin number. """

        if pin in self._gpios:
            _GPIO.output(pin, _GPIO.HIGH)
        else:
            raise NoSuchGPIO("Pin %d is not a GPIO pin." % pin)

    def low(self, pin):
        """ Set a selected pin low, by native board pin number. """

        if pin in self._gpios:
            _GPIO.output(pin, _GPIO.LOW)
        else:
            raise NoSuchGPIO("Pin %d is not a GPIO pin." % pin)

    def toggle(self, pin):
        """ Set a selected pin low if it was high, and high if it was low. """

        if pin in self._gpios:
            _GPIO.output(pin, not _GPIO.input(pin))
        else:
            raise NoSuchGPIO("Pin %d is not a GPIO pin." % pin)
