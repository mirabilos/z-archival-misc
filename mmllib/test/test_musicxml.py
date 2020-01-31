#!/usr/bin/env python
# ~*~ coding: utf-8 ~*~

# Copyright © 2017
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

import os
import unittest

from mmllib.musicxml import convert_mml_file

class MusicXMLTests(unittest.TestCase):
    def setUp(self):
        self.datadir = os.path.join(os.path.dirname(__file__), "data")

    def test_convert_mml_file(self):
        mmlfile = os.path.join(self.datadir, "loreley.mml")
        xmlfile = os.path.join(self.datadir, "loreley.xml")
        xml = convert_mml_file(mmlfile)
        with open(xmlfile, "rb") as f:
            expected = f.read()
        self.assertEqual(xml, expected[:-1])

if __name__ == "__main__":
    unittest.main()
