/* imagetypes.c:
 *
 * this contains things which reference the global ImageTypes array
 *
 * jim frost 09.27.89
 *
 * Copyright 1989, 1991 Jim Frost.
 * See LICENCE file for complete legalities.
 */

#include <errno.h>
#include "xloadimage.h"

/* load a named image
 */

Image *
loadImage(const char *name)
{ char   fullname[MAXPATHLEN];

  if (findImage(name, fullname) < 0) {
    if (errno == ENOENT)
      fprintf(stderr, "%s: image not found\n", name);
    else
      perror(fullname);
    return(NULL);
  }
  return (imLoad(fullname, name));
}

/* identify what kind of image a named image is
 */

void
identifyImage(const char *name)
{ char fullname[MAXPATHLEN];

  if (findImage(name, fullname) < 0) {
    if (errno == ENOENT)
      fprintf(stderr, "%s: image not found\n", name);
    else
      perror(fullname);
    return;
  }
}

void
goodImage(Image *image, const char *func)
{
  if (!image) {
    fprintf(stderr, "%s: nil image\n", func);
    cleanup(-1);
  }
  switch (image->type) {
  case IBITMAP:
  case IRGB:
  case ITRUE:
    break;
  default:
    fprintf(stderr, "%s: bad destination image\n", func);
    cleanup(-1);
  }
}
