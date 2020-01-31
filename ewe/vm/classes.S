/* $MirOS: contrib/hosted/ewe/vm/classes.S,v 1.3 2008/05/11 23:33:55 tg Exp $ */

	.data
	.globl	EweFileBytes		/* for ELF systems */
	.globl	_EweFileBytes		/* for a.out and COFF systems */

	.align	4
EweFileBytes:
_EweFileBytes:
	.incbin "ewe.ewe"
