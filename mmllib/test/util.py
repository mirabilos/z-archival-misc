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

import sys
import unittest

def round_sequence_r(seq, digits=7):
    if isinstance(seq, list) or isinstance(seq, tuple):
        return type(seq)([round_sequence_r(x) for x in seq])
    elif isinstance(seq, float):
        return round(seq, digits)
    else:
        return seq

# The following is a backport of some of Python 2.7’s new
# unittest functions, under the Python licence which you
# already accepted seeming as you run this under Python.
# No rights are claimed for this by the MMLlib developers.

if sys.version_info < (2, 7, 0):
    import difflib
    import pprint

    def safe_repr(obj, short=False):
        _MAX_LENGTH = 80
        try:
            result = repr(obj)
        except Exception:
            result = object.__repr__(obj)
        if not short or len(result) < _MAX_LENGTH:
            return result
        return result[:_MAX_LENGTH] + ' [truncated]...'

    class TestCase(unittest.TestCase):
        def assertListEqual(self, seq1, seq2, msg=None):
            seq_type=list
            seq_type_name = seq_type.__name__
            if not isinstance(seq1, seq_type):
                raise self.failureException('First sequence is not a %s: %s'
                                        % (seq_type_name, safe_repr(seq1)))
            if not isinstance(seq2, seq_type):
                raise self.failureException('Second sequence is not a %s: %s'
                                        % (seq_type_name, safe_repr(seq2)))

            differing = None
            try:
                len1 = len(seq1)
            except (TypeError, NotImplementedError):
                differing = 'First %s has no length.    Non-sequence?' % (
                        seq_type_name)

            if differing is None:
                try:
                    len2 = len(seq2)
                except (TypeError, NotImplementedError):
                    differing = 'Second %s has no length.    Non-sequence?' % (
                            seq_type_name)

            if differing is None:
                if seq1 == seq2:
                    return

                seq1_repr = safe_repr(seq1)
                seq2_repr = safe_repr(seq2)
                if len(seq1_repr) > 30:
                    seq1_repr = seq1_repr[:30] + '...'
                if len(seq2_repr) > 30:
                    seq2_repr = seq2_repr[:30] + '...'
                elements = (seq_type_name.capitalize(), seq1_repr, seq2_repr)
                differing = '%ss differ: %s != %s\n' % elements

                for i in xrange(min(len1, len2)):
                    try:
                        item1 = seq1[i]
                    except (TypeError, IndexError, NotImplementedError):
                        differing += ('\nUnable to index element %d of first %s\n' %
                                     (i, seq_type_name))
                        break

                    try:
                        item2 = seq2[i]
                    except (TypeError, IndexError, NotImplementedError):
                        differing += ('\nUnable to index element %d of second %s\n' %
                                     (i, seq_type_name))
                        break

                    if item1 != item2:
                        differing += ('\nFirst differing element %d:\n%s\n%s\n' %
                                     (i, safe_repr(item1), safe_repr(item2)))
                        break
                else:
                    if (len1 == len2 and seq_type is None and
                        type(seq1) != type(seq2)):
                        # The sequences are the same, but have differing types.
                        return

                if len1 > len2:
                    differing += ('\nFirst %s contains %d additional '
                                 'elements.\n' % (seq_type_name, len1 - len2))
                    try:
                        differing += ('First extra element %d:\n%s\n' %
                                      (len2, safe_repr(seq1[len2])))
                    except (TypeError, IndexError, NotImplementedError):
                        differing += ('Unable to index element %d '
                                      'of first %s\n' % (len2, seq_type_name))
                elif len1 < len2:
                    differing += ('\nSecond %s contains %d additional '
                                 'elements.\n' % (seq_type_name, len2 - len1))
                    try:
                        differing += ('First extra element %d:\n%s\n' %
                                      (len1, safe_repr(seq2[len1])))
                    except (TypeError, IndexError, NotImplementedError):
                        differing += ('Unable to index element %d '
                                      'of second %s\n' % (len1, seq_type_name))
            standardMsg = differing
            diffMsg = '\n' + '\n'.join(
                difflib.ndiff(pprint.pformat(seq1).splitlines(),
                              pprint.pformat(seq2).splitlines()))
            standardMsg = self._truncateMessage(standardMsg, diffMsg)
            msg = self._formatMessage(msg, standardMsg)
            self.fail(msg)

        def assertDictEqual(self, d1, d2, msg=None):
            self.assertIsInstance(d1, dict, 'First argument is not a dictionary')
            self.assertIsInstance(d2, dict, 'Second argument is not a dictionary')

            if d1 != d2:
                standardMsg = '%s != %s' % (safe_repr(d1, True), safe_repr(d2, True))
                diff = ('\n' + '\n'.join(difflib.ndiff(
                               pprint.pformat(d1).splitlines(),
                               pprint.pformat(d2).splitlines())))
                standardMsg = self._truncateMessage(standardMsg, diff)
                self.fail(self._formatMessage(msg, standardMsg))

        def assertIsInstance(self, obj, cls, msg=None):
            if not isinstance(obj, cls):
                standardMsg = '%s is not an instance of %r' % (safe_repr(obj), cls)
                self.fail(self._formatMessage(msg, standardMsg))

        def _truncateMessage(self, message, diff):
            return message + diff

        def _formatMessage(self, msg, standardMsg):
            return msg or standardMsg

else:
    class TestCase(unittest.TestCase):
        pass
