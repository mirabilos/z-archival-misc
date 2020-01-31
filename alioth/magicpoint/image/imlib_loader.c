#include "xloadimage.h"
#include <X11/extensions/shape.h>

#define IMFILENUM 500
static char imfile[IMFILENUM][1024];
static Imlib_Image *imdata[IMFILENUM];
static Image *imagedata[IMFILENUM];
static int imnum;
static Imlib_Context *id;

static void regist_imdata(const char *, Imlib_Image *, Image *);

Image *
imLoad(const char *fullname, const char *name)
{
	static Display *disp = NULL;
	Image *image = NULL;
	Imlib_Image *im;
	unsigned int w,h, size, i;
	DATA32 * argb_data;
	byte * rgb_ptr;

	if (disp == NULL) {
		disp=XOpenDisplay(NULL);
	}
	/*    if (id == NULL) id=Imlib_init(disp); */
	/* might needs more work */
	if (id == NULL) {
		/* dither for non-truecolor displays */
		imlib_context_set_dither(1);
		imlib_context_set_display(disp);
		imlib_context_set_visual(DefaultVisual(disp, DefaultScreen(disp)));
		imlib_context_set_colormap(DefaultColormap(disp, DefaultScreen(disp)));
	}
	if ((im = search_imdata(fullname)) == NULL) {
		/* im = Imlib_load_image(id, fullname); */
		im = imlib_load_image(fullname);
	}
	if (im == NULL) {
		return NULL;
	}
	imlib_context_set_image(im);
	w = imlib_image_get_width();
	h = imlib_image_get_height();
	size = w * h;

	if (image == NULL) image = newTrueImage(w, h);

	/* Imlib2 stores images in ARGB format (32 bpp).  MagicPoint
	 * wants RGB data (24 bpp). So we need a conversion pass. */
	argb_data = imlib_image_get_data_for_reading_only();
	rgb_ptr = image->data;
	for (i = 0; i < size; ++i)
	{
		if ((argb_data[i] >> 24) < 128)
		{
			/* If this is a transparent pixel, we store #FE00FE. */
			*rgb_ptr++ = 0xFE;
			*rgb_ptr++ = 0x00;
			*rgb_ptr++ = 0xFE;
			/* Tell mgp we have transparent pixels. */
			image->trans = 0xFE00FE;
		}
		else if ((argb_data[i] & 0x00FFFFFF) == 0x00FE00FE)
		{
		/* If that color is actually used, we substitute something close. */
		*rgb_ptr++ = 0xFF;
		*rgb_ptr++ = 0x00;
		*rgb_ptr++ = 0xFE;
		}
		else
		{
			/* Otherwise, we can copy the pixel. */
			*rgb_ptr++ = (argb_data[i] >> 16) & 0xFF; /* red */
			*rgb_ptr++ = (argb_data[i] >> 8) & 0xFF;  /* green */
			*rgb_ptr++ = argb_data[i] & 0xFF;         /* blue */
		}
	}
	image->title = dupString(name);
	regist_imdata(name, im, image);
	return image;
}

Imlib_Image *
search_imdata(const char *fullname)
{
	int i;
	for (i = 0; i < imnum; i ++){
		if (!strcmp(imfile[i], fullname)) {
			return imdata[i];
		}
	}
	return NULL;
}

static void
regist_imdata(const char *fullname, Imlib_Image *im, Image *image)
{
	strlcpy(imfile[imnum], fullname, 1024);
	imdata[imnum] = im;
	imagedata[imnum] = image;
	imnum ++;
}

Pixmap
pixmap_fromimimage(Imlib_Image *imimage, int width, int height, Window window)
{
	static Pixmap pixmap;

	imlib_context_set_image (imimage);
	imlib_context_set_drawable (window);
	imlib_render_pixmaps_for_whole_image_at_size(&pixmap, NULL, width, height);
	return pixmap;
}

#define MAXPMAP 100
void
manage_pixmap(Pixmap pixmap, int add, int page)
{
	static Pixmap pmap[MAXPMAP];
	static int ppage[MAXPMAP];
	int i;

	if (add) {
		for (i = 0; i < MAXPMAP; i ++) {
			if (pmap[i] == pixmap) return;
		}
		for (i = 0; i < MAXPMAP; i ++) {
			if (!pmap[i]) break;
		}
		if (i == MAXPMAP) {
			fprintf(stderr, "warning: too many images in manage_pixmap\n");
			return;
		}
		pmap[i] = pixmap;
		ppage[i] = page;
	} else {
		for (i = 0; i < MAXPMAP; i ++) {
			if (ppage[i] == page && pmap[i] != 0){
				imlib_free_pixmap_and_mask(pmap[i]);
				pmap[i] = 0;
			}
		}
	}
}
