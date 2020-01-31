/* image.h:
 *
 * portable image type declarations
 *
 * jim frost 10.02.89
 *
 * Copyright 1989 Jim Frost.
 * See LICENCE file for complete legalities.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef unsigned long  Pixel;     /* what X thinks a pixel is */
typedef unsigned short Intensity; /* what X thinks an RGB intensity is */
typedef unsigned char  byte;      /* byte type */

typedef struct rgbmap {
  unsigned int  size;       /* size of RGB map */
  unsigned int  used;       /* number of colors used in RGB map */
  unsigned int  compressed; /* image uses colormap fully */
  Intensity    *red;        /* color values in X style */
  Intensity    *green;
  Intensity    *blue;
} RGBMap;

/* image structure
 */

typedef struct {
  char         *title;  /* name of image */
  unsigned int  type;   /* type of image */
  RGBMap        rgb;    /* RGB map of image if IRGB type */
  unsigned int  width;  /* width of image in pixels */
  unsigned int  height; /* height of image in pixels */
  unsigned int  depth;  /* depth of image in bits if IRGB type */
  unsigned int  pixlen; /* length of pixel if IRGB type */
  byte         *data;   /* data rounded to full byte for each row */
  int		trans;	/* transparent index in rgb */
} Image;

#define IBITMAP 0 /* image is a bitmap */
#define IRGB    1 /* image is RGB */
#define ITRUE   2 /* image is true color */

#define BITMAPP(IMAGE) ((IMAGE)->type == IBITMAP)
#define RGBP(IMAGE)    ((IMAGE)->type == IRGB)
#define TRUEP(IMAGE)   ((IMAGE)->type == ITRUE)

#define TRUE_RED(PIXVAL)   (((PIXVAL) & 0xff0000) >> 16)
#define TRUE_GREEN(PIXVAL) (((PIXVAL) & 0xff00) >> 8)
#define TRUE_BLUE(PIXVAL)  ((PIXVAL) & 0xff)
#define RGB_TO_TRUE(R,G,B) \
  ((((R) & 0xff00) << 8) | ((G) & 0xff00) | ((B) >> 8))

/* special case 1-byte transfers so they're inline
 */

#define memToVal(PTR,LEN)    ((LEN) == 1 ? (unsigned long)(*(PTR)) : \
			      doMemToVal(PTR,LEN))
#define memToValLSB(PTR,LEN) ((LEN) == 1 ? (unsigned long)(*(PTR)) : \
			      doMemToValLSB(PTR,LEN))
#define valToMem(VAL,PTR,LEN)    ((LEN) == 1 ? \
				  (unsigned long)(*(PTR) = (byte)(VAL)) : \
				  doValToMem(VAL,PTR,LEN))
#define valToMemLSB(VAL,PTR,LEN) ((LEN) == 1 ? \
				  (unsigned long)(*(PTR) = (byte)(VAL)) : \
				  (int)doValToMemLSB(VAL,PTR,LEN))

/* SUPPRESS 558 */

/* function declarations
 */

/* dither.c */
Image *dither(Image *, unsigned int);

/* imagetypes.c */
Image *loadImage(const char *);
void   identifyImage(const char *);
void goodImage(Image *, const char *);

/* new.c */
extern unsigned long DepthToColorsTable[];
unsigned long colorsToDepth(unsigned long);
char  *dupString(const char *);
Image *newBitImage(unsigned int, unsigned int);
Image *newRGBImage(unsigned int, unsigned int, unsigned int);
Image *newTrueImage(unsigned int, unsigned int);
void   freeImage(Image *);
void   freeImageData(Image *);
void   newRGBMapData(RGBMap *, unsigned int);
void   freeRGBMapData(RGBMap *);
byte  *lcalloc(size_t);
byte  *lmalloc(size_t);
#define lfree free

#define depthToColors(n) DepthToColorsTable[((n) < 32 ? (n) : 32)]

/* reduce.c */
Image *reduce(Image *, unsigned int, unsigned int);
Image *expand(Image *);

/* doMemToVal and doMemToValLSB used to be void type but some compilers
 * (particularly the 4.1.1 SunOS compiler) couldn't handle the
 * (void)(thing= value) conversion used in the macros.
 */

/* value.c */
unsigned long doMemToVal(byte *, unsigned int);
unsigned long doValToMem(unsigned long, byte *, unsigned int);
unsigned long doMemToValLSB(byte *, unsigned int);
unsigned long doValToMemLSB(unsigned long, byte *, unsigned int);
void          flipBits(byte *, unsigned int);

/* zoom.c */
Image *zoom(Image *, float, float, int);

/* this returns the (approximate) intensity of an RGB triple
 */

#define colorIntensity(R,G,B) \
  (RedIntensity[(R) >> 8] + GreenIntensity[(G) >> 8] + BlueIntensity[(B) >> 8])

extern unsigned short RedIntensity[];
extern unsigned short GreenIntensity[];
extern unsigned short BlueIntensity[];

extern void cleanup(int)
#if HAVE_ATTRIBUTE_NORETURN
    __attribute__((__noreturn__))
#endif
    ;

extern Image *imLoad(const char *, const char *);

void memoryExhausted(void)
#if HAVE_ATTRIBUTE_NORETURN
    __attribute__((__noreturn__))
#endif
    ;
