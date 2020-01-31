/* misc.c:
 *
 * miscellaneous funcs
 *
 * jim frost 10.05.89
 *
 * Copyright 1989, 1990, 1991 Jim Frost.
 * See LICENCE file for complete legalities.
 */

#include <stdio.h>

#include "image.h"

void
memoryExhausted(void)
{
  fprintf(stderr,
	  "Memory has been exhausted; operation cannot continue (sorry).\n");
  cleanup(-1);
}
