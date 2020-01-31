#!/usr/bin/env python
# ~*~ coding: utf-8 ~*~

# Copyright © 2013, 2017
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

from __future__ import with_statement

import codecs
import os

from setuptools import setup

# Get some information for the setup
MYDIR = os.path.dirname(__file__)
long_description = ""
for filename in ["README.rst"]:
    with codecs.open(os.path.join(MYDIR, filename), "r", "utf-8") as f:
        long_description += f.read() + "\n"

setup(
    # Basic information
    name             = 'Floppi-Music',
    version          = '0.1',
    license          = 'MirOS',
    description      = 'Crazily advanced floppy music player for Raspberry Pi',
    long_description = long_description,
    url              = 'https://edugit.org/nik/floppi-music',

    # Author information
    maintainer       = 'Dominik George',
    maintainer_email = 'nik@naturalnet.de',

    # Included code
    packages             = ['floppi'],
    entry_points         = {
                            'console_scripts': [
                                                'floppi-play = floppi.cmds:play'
                                               ]
                           },

    # Distribution information
    zip_safe         = False,
    install_requires = [
                        'RPi.GPIO',
                        'mmllib'
                       ],
    classifiers      = [
                        'Development Status :: 5 - Production/Stable',
                        'Environment :: Console',
                        'Intended Audience :: Developers',
                        'Intended Audience :: Science/Research',
                        'License :: OSI Approved',
                        'Operating System :: POSIX :: Linux',
                        'Programming Language :: Python',
                        'Topic :: Artistic Software',
                        'Topic :: Multimedia :: Sound/Audio :: Players'
                       ]
)
