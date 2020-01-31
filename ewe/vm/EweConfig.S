/* $MirOS: contrib/hosted/ewe/vm/EweConfig.S,v 1.2 2008/05/11 23:33:55 tg Exp $ */

	.data
	.globl	EweConfigBytes		/* for ELF systems */
	.globl	_EweConfigBytes		/* for a.out and COFF systems */

	.align	4
EweConfigBytes:
_EweConfigBytes:
	.incbin "EweConfig.ewe"
