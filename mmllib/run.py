#!/usr/bin/env python
# ~*~ coding: utf-8 ~*~
#-
# Copyright © 2017
#	mirabilos <m@mirbsd.org>
#	Dominik George <nik@naturalnet.de>
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

import os.path
import sys
sys.path.append(os.path.join(os.path.dirname(__file__), 'src'))

import mmllib.cmds

if len(sys.argv) < 2 or sys.argv[1] not in vars(mmllib.cmds):
    print("Syntax: python run.py command ...")
    print("Commands are:")
    for cmd in mmllib.cmds.__all__:
        print(u" \u2022 %s\u200A\u2014\u200A%s" % (cmd,
          vars(mmllib.cmds)[cmd]._shortdesc))
    sys.exit(1)
sys.argv.pop(0)

sys.exit(vars(mmllib.cmds)[sys.argv[0]]())
