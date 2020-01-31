/*
 * Copyright (C) 1997 and 1998 WIDE Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#include <math.h>
#include "mgp.h"

/* background gradation */
#define G_PI	3.1415926535897932385
#define G_PI2	1.5707963267948966192

static void draw_gradation0(int, int, int, int, int, int,
	byte *, byte *, int, int, u_int);
static void g_rotate(byte *, struct ctrl_grad *, int, int);

/*
 * generate gradation for single color plane.
 * x: color plane
 * y: axis for gradation
 * z: extra axis
 */
static void
draw_gradation0(int x1, int x2, int y1v, int y2, int z1, int z2,
    byte *p1, byte *p2, /* delta p in x/y direction */ int dpy, int dpz,
    u_int mask)
{
	int s, step;
	int dx, dy;
	int z;
	byte *p;

	dx = abs(x2 - x1);
	dy = abs(y2 - y1v);

	if (dx > dy) {
		if (x1 > x2) {
			step = (y1v > y2) ? 1 : -1;
			s = x1; x1 = x2; x2 = s; /*swap*/
			y1v = y2;
			p = p2;
		} else {
			step = (y1v < y2) ? 1 : -1;
			p = p1;
		}

		for (z = z1; z < z2; z++)
			*(p + z * dpz) = x1 & mask;
		s = dx >> 1;
		while (++x1 <= x2) {
			if ((s -= dy) < 0) {
				s += dx;
				y1v += step;
				p += (dpy * step);
			}
			for (z = z1; z < z2; z+=3){
				*(p + z * dpz) =     x1 * (30 + (y1v % 3)) / 32 & mask;
				*(p + (z+1) * dpz) = x1 * (30 + ((y1v + 2) % 3)) / 32 & mask;
				*(p + (z+2) * dpz) = x1 * (30 + ((y1v + 1) % 3)) / 32 & mask;
			}
		}
	} else {
		if (y1v > y2) {
			step = (x1 > x2) ? 1 : -1;
			s = y1v; y1v = y2; y2 = s; /*swap*/
			x1 = x2;
			p = p2;
		} else {
			step = (x1 < x2) ? 1 : -1;
			p = p1;
		}

		for (z = z1; z < z2; z++)
			*(p + z * dpz) = x1 & mask;
		s = dy >> 1;
		while (++y1v <= y2) {
			p += dpy;	/*y always inc*/
			if ((s -= dx) < 0) {
				s += dy;
				x1 += step;
			}
			for (z = z1; z < z2; z+=3){
				*(p + z * dpz) =     x1 * (30 + (y1v % 3)) / 32 & mask;
				*(p + (z+1) * dpz) = x1 * (30 + ((y1v + 2) % 3)) / 32 & mask;
				*(p + (z+2) * dpz) = x1 * (30 + ((y1v + 1) % 3)) / 32 & mask;
			}
		}
	}
}

byte *
draw_gradation(int width, int height, struct ctrl_grad *cg)
{
	int bmask[8] = { 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff };
	byte *pic;
	const u_int bits = 8;
	int i, j;
	int x1 = 0, x2 = 0;
	int y1v = 0, y2 = 0, dpy = 0;
	int z1 = 0, z2 = 0, dpz = 0;
	byte *p1 = NULL, *p2 = NULL;
	byte mask;

	pic = (byte *)malloc(width * height * 3 * sizeof(byte));
	if (!pic) {
		fprintf(stderr,"couldn't alloc space for gradation image\n");
		return NULL;
	}

	memset(pic, 0, width * height * 3 * sizeof(byte));

	if (cg->ct_direction % 90) {
		g_rotate(pic, cg, width, height);
		return pic;
	}

	for (j = 0; j < 3; j++) {	/*r, g, b*/
		for (i = 0; i < cg->ct_g_colors - 1; i++) {
			switch (j) {
			case 0:
				x1 = cg->colors[i]->r;
				x2 = cg->colors[i + 1]->r;
				break;
			case 1:
				x1 = cg->colors[i]->g;
				x2 = cg->colors[i + 1]->g;
				break;
			case 2:
				x1 = cg->colors[i]->b;
				x2 = cg->colors[i + 1]->b;
				break;
			}

			mask = bmask[bits - 1];
			switch (cg->ct_direction) {
			case 0:
				y1v = ((height - 1) * i) / (cg->ct_g_colors - 1);
				y2 = ((height - 1) * (i + 1))
					/ (cg->ct_g_colors - 1);
				p1 = pic + j + width * 3 * y1v;
				p2 = pic + j + width * 3 * y2;
				z1 = 0;
				z2 = width;
				dpy = width * 3;
				dpz = 3;
				break;
			case 90:
				y1v = ((width - 1) * i) / (cg->ct_g_colors - 1);
				y2 = ((width - 1) * (i + 1))
					/ (cg->ct_g_colors - 1);
				p1 = pic + j + 3 * y1v;
				p2 = pic + j + 3 * y2;
				z1 = 0;
				z2 = height;
				dpy = 3;
				dpz = width * 3;
				break;
			case 180:
				y1v = ((height - 1) * (cg->ct_g_colors - i - 1))
					/ (cg->ct_g_colors - 1);
				y2 = ((height - 1) * (cg->ct_g_colors - i - 2))
					/ (cg->ct_g_colors - 1);
				p1 = pic + j + width * 3 * y1v;
				p2 = pic + j + width * 3 * y2;
				z1 = 0;
				z2 = width;
				dpy = width * 3;
				dpz = 3;
				break;
			case 270:
				y1v = ((width - 1) * (cg->ct_g_colors - i - 1))
					/ (cg->ct_g_colors - 1);
				y2 = ((width - 1) * (cg->ct_g_colors - i - 2))
					/ (cg->ct_g_colors - 1);
				p1 = pic + j + 3 * y1v;
				p2 = pic + j + 3 * y2;
				z1 = 0;
				z2 = height;
				dpy = 3;
				dpz = width * 3;
				break;
			}
			draw_gradation0(x1, x2, y1v, y2, z1, z2, p1, p2,
				dpy, dpz, mask);
		}
	}

	return pic;
}

static double cost, sint;
static double cos2t, sin2t;
static double dcost, dsint;

/* rotate graphic */
static void
g_rotate(byte *pic, struct ctrl_grad *cg, int width, int height)
{
    byte *pp;
    double maxd, mind, del, d, rat, crat, cval;
    double theta, dy, ey, td1, td2;
    int    x, y, cx, cy, r, g, b, bc, nc1;
    int    rot, mode;
    struct g_color * c1;
    struct g_color * c2;

    rot   = cg->ct_direction;
    mode  = cg->ct_mode;

    cx = width/2;  cy = height/2;
    theta = (double) rot * G_PI / 180.0;
    cost  = cos(theta);
    sint  = sin(theta);
    dsint = sint*sint;
    dcost = cost*cost;
    sin2t = sin(2*theta);
    cos2t = cos(2*theta);

    nc1 = cg->ct_g_colors - 1;

    /* compute max/min distances */
    if (rot > 0 && rot < 90) {
	mind = cdist(0, 0, cx, cy, rot, mode);
	maxd = cdist(width-1, height-1, cx, cy, rot, mode);
    }
    else if (rot >= 90 && rot < 180) {
	mind = cdist(0, height-1, cx, cy, rot, mode);
	maxd = cdist(width-1, 0,  cx, cy, rot, mode);
    }
    else if (rot >= 180 && rot < 270) {
	mind = cdist(width-1, height-1, cx, cy, rot, mode);
	maxd = cdist(0, 0, cx, cy, rot, mode);
    }
    else {
	mind = cdist(width-1,  0, cx, cy, rot, mode);
	maxd = cdist(0, height-1, cx, cy, rot, mode);
    }

    del = maxd - mind;         /* maximum distance */

    /* loop start */

    td1 = cx - cx*dcost;
    td2 = cy + 0.5*cx*sin2t;

    for (y = 0; y < height; y++) {
	pp = pic + (y * width * 3);
	dy = (cy-y)*0.5*sin2t + td1;
	ey = (y-cy)*dsint + td2;

	for (x = 0; x < width; x++) {
	    d = lcdist(x, y, cx, cy, rot, mode, dy, ey);
	    rat = (d - mind) / del;

	    if (rat < 0.0){
		cval = bc = crat = 0.0;
	    } else if (rat > 1.0){
		cval = bc = (double)nc1;
		crat = 0.0;
	    } else {
		cval = rat * nc1;
		bc   = floor(cval);
		crat = cval - bc;
	    }

	    if (bc < nc1) {
		c1 = (cg->colors)[bc];
		c2 = (cg->colors)[bc+1];

		r = c1->r + crat * ((c2->r) - (c1->r));
		g = c1->g + crat * ((c2->g) - (c1->g));
		b = c1->b + crat * ((c2->b) - (c1->b));
	    } else {
		c1 = (cg->colors)[nc1];
		r = c1->r; g = c1->g; b = c1->b;
	    }
	    *pp++ = (byte) r;  *pp++ = (byte) g;  *pp++ = (byte) b;
	}
    }
}

/* compute distance */
double cdist(int x, int y, int cx, int cy, int rot, int mode)
{
    double x1, y1v, x2, y2, x3, d ;

    /* special case */
    if (rot == 0)   return (double) (y - cy);
    if (rot == 90)  return (double) (x - cx);
    if (rot == 180) return (double) (cy - y);
    if (rot == 270) return (double) (cx - x);

/* experimental routine for circle patern gradation */
    if (mode == 1) {
	d = sqrt((y-cy)*(y-cy)*cost + (x-cx)*(x-cx)*sint);
	if (x + y - cy < 0) d = -d;
	return d;
    }

    /* x1,y1v = original point */
    x1 = (double) x;  y1v = (double) y;
    x2 = cx + (cy-y1v)*0.5*sin2t - (cx-x1)*dcost;
    y2 = cy - (cy-y1v)*dsint + (cx-x1)*0.5*sin2t;
    x3 = x1-x2;

    d = sqrt(x3*x3 + (y1v - y2)*(y1v - y2));
    if ((rot < 180 && x3<0) || (rot > 180 && x3>0)) d = -d;
    return d;
}

double lcdist(int x, int y, int cx, int cy, int rot, int mode,
    double dy, double ey)
{
    double x1, y1v, x2, y2, x3, d ;

    /* special case */
    if (rot == 0)   return (double) (y - cy);
    if (rot == 90)  return (double) (x - cx);
    if (rot == 180) return (double) (cy - y);
    if (rot == 270) return (double) (cx - x);

/* experimental routine for circle patern gradation */
    if (mode == 1) {
	d = sqrt((y-cy)*(y-cy)*cost + (x-cx)*(x-cx)*sint);
	if (x + y - cy < 0) d = -d;
	return d;
    }

    x1 = (double) x;
    y1v = (double) y;

    x2 = dy + x1*dcost;
    y2 = ey - x1*0.5*sin2t;
    x3 = x1-x2;

    d = sqrt(x3*x3 + (y1v - y2)*(y1v - y2));
    if ((rot < 180 && x3<0) || (rot > 180 && x3>0)) d = -d;
    return d;
}

Image *
make_XImage(byte *pic, u_int width, u_int height)
{
    Image        *image;

    /* make Image structure (stolen from image/new.c)*/
    image= (Image *)lmalloc(sizeof(Image));
    image->type= ITRUE;
    image->title= strdup("");
    image->rgb.used= image->rgb.size= 0;
    image->width= width;
    image->height= height;
    image->depth= 24;
    image->pixlen= 3;
    image->data= pic;

    return(image);
}
