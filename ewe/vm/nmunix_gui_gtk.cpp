__IDSTRING(rcsid_nmunix_gui_gtk, "$MirOS: contrib/hosted/ewe/vm/nmunix_gui_gtk.cpp,v 1.3 2008/04/11 00:27:24 tg Exp $");

#include <gtk/gtk.h>
#include <gdk/gdk.h>
#include <gdk/gdkkeysyms.h>
#include "mpixbuf.h"
//#include <gdk-pixbuf/gdk-pixbuf.h>

char* toUtf(UtfString& buffer, WCHAR* text, int count, int &convertedLength)
{
	int len = sizeofEncodedJavaUtf8String((uint16*)text,count);
	if (buffer.len < len+1 || buffer.str == NULL){
		if (buffer.str != NULL) delete buffer.str;
		buffer.str = new char[len+1];
		buffer.len = len+1;
	}
	convertedLength = len;
	encodeJavaUtf8String((uint16*)text,count,(uint8_t *)buffer.str);
	buffer.str[len] = 0;
	return buffer.str;
}
#ifdef GTK_VERSION_2_0
void setPangoLayoutText(PangoLayout* layout, WCHAR *ch, int count)
{
	static UtfString converted;
	int length;
	if (count != 0 && ch[count-1] == 0) count -= 2;
	if (count < 0) count = 0;
	char* out = toUtf(converted,ch,count,length);
	//printf("Out: <%s>\n",out);
	pango_layout_set_text(layout,out,length);
}
#endif

//###################################################
static class GlobalGuiObjects{
//###################################################
public:
	GtkWidget* mainWidget;
#ifdef GTK_VERSION_2_0
	PangoLayout* layout;
	PangoContext* context;
	int textWidth(PangoFontDescription* fd, WCHAR* ch, int count)
	{
		if (fd == NULL) return 10*count;
		pango_layout_set_font_description(layout,fd);
		setPangoLayoutText(layout,ch,count);
		PangoRectangle pixRect;
		pango_layout_get_pixel_extents(layout,NULL,&pixRect);
		return pixRect.width;
	}
#endif

	GlobalGuiObjects(GtkWidget* area)
	{
		mainWidget = area;
#ifdef GTK_VERSION_2_0
		layout = gtk_widget_create_pango_layout(area,"");
		pango_layout_set_single_paragraph_mode(layout,true);
		pango_layout_set_width(layout,-1);
		context = gtk_widget_get_pango_context(area);
#endif
	}
//###################################################
} *guiObjects;
//###################################################


#define LINECAP GDK_CAP_PROJECTING

//TODO - fix toFront()
//TODO - fix listFonts()

GdkPoint *toPoints(int *x,int *y,int length,int tx,int ty)
{
	GdkPoint *p = new GdkPoint[length];
	for (int i = 0; i<length; i++)
		p[i].x = tx+(*x++), p[i].y = ty+(*y++);
	return p;
}
static FONT defFont = NULL;

#ifdef GTK_VERSION_2_0

//###################################################
class gtkPangoFont{
//###################################################
public:
	PangoFont* font;
	PangoFontDescription* description;
//###################################################
};
//###################################################

//###################################################
class gtkFontMetrics : public eweFontMetrics {
//###################################################
	virtual void destroy()
	{
		//if (gf != NULL) gdk_font_unref(gf);
	}
	virtual int width(WCHAR ch) {return width(&ch,1);}
	virtual int width(WCHAR *ch,int count)
	{
		if (guiObjects == NULL) return 10*count;
		return guiObjects->textWidth(((gtkPangoFont*)nativeFont.nativeFont)->description,ch,count);
	}
//###################################################
};
//###################################################

static gtkPangoFont *tryGetFont(class graphics_font* f, int& ascent, int& descent,PangoFontDescription *alt)
{
	PangoFontDescription *fd = alt == NULL ? pango_font_description_new() : alt;
	PangoFont* ft = NULL;
	if (alt == NULL) pango_font_description_set_family(fd,f->name.str);
	//else ft = pango_context_load_font(guiObjects->context,fd);
	for (int i = 0; i<2 && ft == NULL; i++){
		pango_font_description_set_style(fd,i == 0 && (f->style & FONT_ITALIC) ? PANGO_STYLE_ITALIC : PANGO_STYLE_NORMAL);
		pango_font_description_set_weight(fd, i == 0 && (f->style & FONT_BOLD) ? PANGO_WEIGHT_BOLD : PANGO_WEIGHT_NORMAL);
		for (int sz = f->size; sz > 0 && ft == NULL; sz--){
			pango_font_description_set_size(fd,f->size*PANGO_SCALE);
			ft = pango_context_load_font(guiObjects->context,fd);
		}
	}
	if (ft == NULL){
		if (alt == NULL) pango_font_description_free(fd);
		return NULL;
	}
	gtkPangoFont *ret = new gtkPangoFont();
	ret->description = fd;
	ret->font = ft;
	PangoFontMetrics *fm = pango_font_get_metrics(ft,NULL);
	ascent = pango_font_metrics_get_ascent(fm)/PANGO_SCALE;
	descent = pango_font_metrics_get_descent(fm)/PANGO_SCALE;
	return ret;
}
static gtkPangoFont *loadFont(class graphics_font* f, int& ascent, int& descent)
{
	if (guiObjects == NULL) return NULL;
	gtkPangoFont* pf = tryGetFont(f,ascent,descent,NULL);
	if (pf == NULL) pf = tryGetFont(f,ascent,descent,pango_font_description_from_string("Luxi Mono 12"));
	return pf;

}
#else
static GdkFont *tryFont(class graphics_font* f, int& ascent, int& descent, const char*altName = NULL)
{
		const char *name = altName == NULL ? f->name.str : altName, //"helvetica",
		*bold = (f->style & FONT_BOLD) ? "bold" : "medium",
		*italic = (f->style & FONT_ITALIC) ? "I" : "R",
		*width = "normal";
		const char *rb = bold, *ri = italic;
		int point = f->size;
		static char fontname[256];

		sprintf(fontname,"-*-%s-%s-%s-%s--*-%d-*-*-*-*-*-*",
			name,bold,italic,width,point*10);
		GdkFont *gf = gdk_font_load(fontname);

		for (int i = 0; gf == NULL && i<2; i++){
			bold = i == 0 ? rb : "medium";
			italic = i == 0 ? ri : "R";
			for (int point = f->size;gf == NULL && point != 0; point--) {
				sprintf(fontname,"-*-%s-%s-%s-%s--*-%d-*-*-*-*-*-*",
					name,bold,italic,width,point*10);
				gf = gdk_font_load(fontname);
			}
		}
		if (gf != NULL){
			gdk_string_extents(gf,"Ty",NULL,NULL,NULL,&ascent,&descent);
			ascent += 3;
		}
		return gf;
}
static GdkFont *loadFont(class graphics_font* f, int& ascent, int& descent)
{
	const char* useName = f->name.str;
	if (f->name.len == 5 && (!strncmp(f->name.str,"fixed",5)||!strncmp(f->name.str,"Fixed",5)||!strncmp(f->name.str,"FIXED",5)))
		useName = "Courier";
	GdkFont *gf = tryFont(f,ascent,descent,useName);
	if (gf == NULL) gf = tryFont(f,ascent,descent,"Helvetica");
	if (gf == NULL) gf = tryFont(f,ascent,descent,"Arial");
	if (gf == NULL) gf = tryFont(f,ascent,descent,"Times");
	if (gf == NULL) gf = tryFont(f,ascent,descent,"Courier");
	if (gf == NULL) gf = tryFont(f,ascent,descent,"*");
	if (gf == NULL) printf("Font: %s or substitute not loaded!\n",f->name.str);
	//else printf("Font: %s loaded!\n",f->name.str);
	return gf;
}

GdkWChar *toWC(WCHAR *ch,int &count)
{
		GdkWChar *gwc = new GdkWChar[count];
		for (int i = 0; i<count; i++) {
			gwc[i] = ch[i];
			if (ch[i] == 0 && i != 0){
				count = i-1;
				break;
			}
		}
		return gwc;
}

GdkWChar nbsp(0xa0);

//###################################################
class gtkFontMetrics : public eweFontMetrics {
//###################################################
	virtual void destroy()
	{
		//if (gf != NULL) gdk_font_unref(gf);
	}
	virtual int width(WCHAR ch)
	{
		static char s[10];
		GdkFont* gf = (GdkFont*)nativeFont.nativeFont;
		if (gf == NULL) return 10;
		if (wctomb(s,ch) != 1 || ch == 0xa0) ch = ' ';//return 0;
		return gdk_char_width_wc(gf,ch);
	}
	virtual int width(WCHAR *ch,int count)
        {
		GdkFont* gf = (GdkFont*)nativeFont.nativeFont;
		if (gf == NULL) return 10*count;
		GdkWChar *gwc = toWC(ch,count);
		for (int i = 0; i<count; i++)
			if (ch[i] == 0xa0) gwc[i] = ' ';
		int ret = gdk_text_width_wc(gf,gwc,count);
		delete gwc;
/*
		if (true){
			TCHAR *check = new TCHAR[count+1];
			for (int i = 0; i<count+1; i++) check[i] = i == count ? 0 : (TCHAR)(ch[i] & 0xff);
			if (true){
				printf("Width: %s %d = %d\n",check,count,ret);
			}
			delete check;
		}
*/
		return ret;
	}
//###################################################
};
//###################################################
#endif

static GtkWidget *creator = NULL;

static uint32 colors[1024];
static int numCols = 0;

GdkWindow *getCreator()
{
	if (creator == NULL) {
		creator = gtk_window_new(GTK_WINDOW_TOPLEVEL);
		gtk_widget_realize(creator);
	}
	return creator->window;
}
GdkColormap *getColormap()
{
	return gdk_window_get_colormap(getCreator());
}
GdkColor toColor(int red,int blue,int green)
{
	GdkColor c;
	c.red = red;
	c.green = green;
	c.blue = blue;
	gdk_colormap_alloc_color(getColormap(),&c,false,true);
	return c;
}
GdkColor toColor(COLORREF color)
{
	return toColor(color.red,color.blue,color.green);
}
#define fixColor(CR) mapColor(CR)
/*
COLORREF fixColor(COLORREF color)
{
	return color;
}
*/
	void writeEweRGB(GdkDrawable *dest,GdkGC *gc,eweRGBImage &data,int destX,int destY,rect *destClip = NULL)
	{
		rect use(destX,destY,data.width,data.height);
		if (destClip != NULL)
			use = use.intersect(*destClip);
		int xoff = use.x-destX, yoff = use.y-destY;
		int total = use.width*use.height;
		if (total <= 0) return;
		GdkGC *gg = gc;
		if (gg == NULL) gg = gdk_gc_new(dest);
		char *rgb = new char[total*3];
		char *b = rgb;
		for (int y = 0; y<use.height; y++){
			uint32 *pix = data.scanLine(y+yoff)+xoff;
			for (int x = 0; x<use.width; x++){
				*b++ = (*pix >> 16) & 0xff;
				*b++ = (*pix >> 8) & 0xff;
				*b++ = *pix & 0xff;
				pix++;
			}
		}
		gdk_draw_rgb_image(dest,gg,use.x,use.y,use.width,use.height,GDK_RGB_DITHER_NORMAL,(guchar *)rgb,use.width*3);
		delete rgb;
		if (gg != gc) gdk_gc_unref(gg);
	}

/*
	QPixmap colorChanger(1,1);
	for (int i = 0; i<numCols; i+=2){
		if (colors[i] == color) return colors[i+1];
	}
		QImage ti = colorChanger.convertToImage();
		uint32 *p = (uint32 *)ti.scanLine(0);
		*p = color;
		colorChanger.convertFromImage(ti);
		ti = colorChanger.convertToImage();
		p = (uint32 *)ti.scanLine(0);
		if (numCols < sizeof(colors)/2-2)
			colors[numCols*2] = color, colors[numCols*2+1] = *p;
		return *p;
*/
//###################################################
class gtkIcon {
//###################################################
public:
	GdkPixmap *pixmap;
	GdkBitmap *mask;
	gtkIcon(GdkPixmap *p,GdkBitmap *m)
	{
		pixmap = gdk_pixmap_ref(p);
		mask = gdk_bitmap_ref(m);
	}
~gtkIcon()
	{
		gdk_pixmap_unref(pixmap);
		gdk_bitmap_unref(mask);
	}
//###################################################
};
//###################################################

//###################################################
class gtkPixelReadBuffer : public pixelReadBuffer{
//###################################################
MPixbuf pixbuf;
public:
	gtkPixelReadBuffer(MPixbuf pb)
	{
		isBGR = true;
		pixbuf = pb;
		rowStride = pb->rowstride;
		hasAlpha = pb->hasAlpha;
		pixels = (uchar*)pb->pixels;
	}
	void release()
	{
		destroyPixbuf(pixbuf);
		delete this;
	}
//###################################################
};
//###################################################

//###################################################
class gtkImage : public eweImage {
//###################################################
public:
	GdkPixmap *pixmap;
	gtkImage(int width,int height,int options,bool fullCreate = false) : eweImage(width,height,options)
	{
		if (fullCreate) pixmap = gdk_pixmap_new(getCreator(),width,height,-1);
	}
	virtual void *getDrawable() {return pixmap;}
	virtual void free() {if (pixmap != NULL) gdk_pixmap_unref(pixmap); pixmap = NULL; eweImage::free();}
	virtual void destroy() {free();}
	virtual void setup(WObject obj)
	{
		pixmap = gdk_pixmap_new(getCreator(),width,height,-1);
	}
	GdkColormap *colorMap()
	{
		if (creator == NULL) return NULL;
		return gdk_window_get_colormap(creator->window);
	}
	virtual PixelReadBuffer getPixelReadBuffer(class rect &area)
	{
		MPixbuf pb = getPixbufFromDrawable(pixmap,colorMap(),area.x,area.y,area.width,area.height);
		if (pb == NULL) return NULL;
		return new gtkPixelReadBuffer(pb);
	}

/*
	virtual eweRGBImage *toFullEweRGB()
	{
		eweRGBImage *ri = new eweRGBImage(width,height);
		ri->valid.x = ri->valid.y = 0;
		ri->valid.width = width;
		ri->valid.height = height;
		MPixbuf pb = getPixbufFromDrawable(pixmap,colorMap(),0,0,width,height);
		unsigned char *alpha = eweImage::getImageAlpha();
		if (pb != NULL) {
			gchar *bits = pb->pixels;
			int rs = pb->rowstride;
			int bpp = pb->hasAlpha ? 4 : 3;
			for (int y = 0; y<height; y++){
				gchar *p = bits+(rs*y);
				uint32 *pix = ri->scanLine(y);
				int ax = y*width;
				for (int x = 0; x<width; x++, p += bpp, ax++){
					*pix++ = ((*p & 0xff) << 16) | ((*(p+1) & 0xff) << 8) | ((*(p+2) & 0xff) << 0);
					*(pix-1) |= alpha == NULL ? 0xff000000 : (int)alpha[ax] << 24;
				}
			}
			destroyPixbuf(pb);
		}
		return ri;
	}
	virtual eweRGBImage *toSubEweRGB(rect &r,eweRGBImage *dest = NULL)
	{
		if (dest != NULL)
			if (dest->width != width || dest->height != height)
				dest = NULL;
		if (dest == NULL) dest = new eweRGBImage(r.width,r.height);
		eweRGBImage *ri = dest;
		ri->usesAlpha = eweImage::hasAlpha();
		unsigned char *alpha = eweImage::getImageAlpha();
		rect image(0,0,width,height);
		rect fromImage = r.intersect(image);
		if (!fromImage.isValid()) {
			ri->valid.x = ri->valid.y = ri->valid.width = ri->valid.height = 0;
			return ri;
		}
		ri->valid.x = fromImage.x-r.x, ri->valid.y = fromImage.y-r.y;
		ri->valid.width = fromImage.width, ri->valid.height = fromImage.height;
		MPixbuf pb = getPixbufFromDrawable(pixmap,colorMap(),fromImage.x,fromImage.y,fromImage.width,fromImage.height);
		if (pb != NULL) {
			int xoff = fromImage.x-r.x, yoff = fromImage.y-r.y;
			gchar *bits = pb->pixels;
			int rs = pb->rowstride;
			int bpp = pb->hasAlpha ? 4 : 3;
			for (int y = 0; y<fromImage.height; y++){
				gchar *p = bits+(rs*y);
				uint32 *pix = ri->scanLine(y+yoff)+xoff;
				int ax = (fromImage.y+y)*width+fromImage.x;
				for (int x = 0; x<fromImage.width; x++, p += bpp, ax++){
					*pix++ = ((*p & 0xff) << 16) | ((*(p+1) & 0xff) << 8) | ((*(p+2) & 0xff) << 0);
					*(pix-1) |= alpha == NULL ? 0xff000000 : (int)alpha[ax] << 24;
				}
			}
			destroyPixbuf(pb);
		}
		return ri;
	}
*/
	virtual void writeEweRGBToNative(eweRGBImage &data,int destX,int destY,rect *destClip = NULL)
	{
		::writeEweRGB(pixmap,NULL,data,destX,destY,destClip);
	}

	void setFullRGB(unsigned char *rgb)
	{
		GdkGC *gc = gdk_gc_new(pixmap);
		gdk_draw_rgb_image(pixmap,gc,0,0,width,height,GDK_RGB_DITHER_NORMAL,(guchar *)rgb,width*3);
		gdk_gc_unref(gc);
	}

	static GdkBitmap *toMonochrome(EweRGBImage rgb,bool reverse = false)
	{
		int width = rgb->width, height = rgb->height;
		int bpl = (width+7)/8;
		gchar *bits = new gchar[height*bpl];
		gchar *loc = bits;
		for (int  y = 0; y<height; y++){
			uint32 *p = rgb->scanLine(y);
			gchar b = 0;
			gchar mask = 0x01;
			for (int x = 0; x<width; x++){
				if (!reverse)
					{if ((*p++ & 0xffffff) != 0) b |= mask;}
				else
					{if ((*p++ & 0xffffff) == 0) b |= mask;}
				mask <<= 1;
				if ((x == width-1) || ((x%8) == 7)){
					mask = 0x01;
					*loc++ = b;
					b = 0;
				}
			}
		}
		GdkBitmap *ret = gdk_bitmap_create_from_data(getCreator(),bits,width,height);
		delete bits;
		return ret;
	}
	GdkBitmap *toMonochrome(bool reverse = false)
	{
		EweRGBImage rgb = eweImage::toEweRGB();
		if (rgb == NULL) return NULL;
		GdkBitmap *ret = toMonochrome(rgb,reverse);
		rgb->release();
		return ret;
	}
	virtual int createCursor(EweImage cmask,int hotX,int hotY)
	{

		GdkBitmap *image = toMonochrome(true);
		GdkBitmap *pmask = (cmask == NULL ? NULL : ((gtkImage *)cmask)->toMonochrome(true));
  	GdkCursor *cursor = NULL;
    if (pmask != NULL){
			if (cmask->width == width && cmask->height == height){
  			GdkColor fg = { 0, 0, 0, 0 };
  			GdkColor bg = { 0, 0xffff, 0xffff, 0xffff };
  			cursor = gdk_cursor_new_from_pixmap (image, pmask, &fg, &bg, hotX, hotY);
		  }
		}
		gdk_pixmap_unref(image);
		if (pmask) gdk_bitmap_unref(pmask);
		return (int)cursor;
	}
	virtual int createIcon(EweImage mask,int options)
	{
		if (mask != NULL)
			if (mask->height != height || mask->width != width)
				mask = NULL;
		GdkBitmap *pmask = (mask == NULL ? NULL : ((gtkImage *)mask)->toMonochrome(true));
		if (pmask == NULL){
			EweRGBImage rm = createMask(0xffffff,false);
			if (rm != NULL) {
				pmask = toMonochrome(rm,true);
				delete rm;
			}
		}
		return (int)(new gtkIcon(pixmap,pmask));
		if (pmask) gdk_bitmap_unref(pmask);
	}
//###################################################
};
//###################################################

//###################################################
class gtkImageMaker : public imageMaker{
//###################################################
EweImage image;

public:
	gtkImageMaker(EweImage i) {image = i;}

	virtual unsigned char *getBits()
	{
		int need = image->width*image->height;
		if (need > 0) return new unsigned char[need*3];
		return NULL;
	}
	virtual void releaseBits(unsigned char *bits,int writeBack = 1)
	{
		if (bits) {
			int num = image->width*image->height;
			unsigned char *one = bits,*two = bits+2;
			for (int i = 0; i<num; i++,one += 3, two +=3){
				unsigned char t = *two;
				*two = *one;
				*one = t;
			}
			((gtkImage *)image)->setFullRGB(bits);
			delete bits;
		}
	}
	virtual void setScanLine(unsigned char *pixels,int bytesPerPixel,int row,int firstX,int xStep){}
	virtual int getBytesPerLine() {return image->width*3;}
	virtual int getBitsPerPixel() {return 24;}
	virtual unsigned char *getAlpha()
	{
		if (WOBJ_ImageAlphaChannel(image->eweObject) != 0)
			return new unsigned char[image->width*image->height];
		return NULL;
	}
	virtual void releaseAlpha(unsigned char *alpha)
	{
		if (alpha == NULL) return;
		WObject ac = WOBJ_ImageAlphaChannel(image->eweObject);
		if (ac != 0)
			memcpy(WOBJ_arrayStart(ac),alpha,image->width*image->height);
		delete alpha;
	}

//###################################################
};
//###################################################

static int gotEvent(GtkObject *who,GdkEvent *event,void *data);
static int activated(GtkObject *who,void *data);


//###################################################
class gtkWindow : public eweWindow {
//###################################################

#define WIDGET (GTK_WIDGET(area))
#define GDKWINDOW (WIDGET->window)
#define TOPWINDOW (GTK_WIDGET(gw)->window)
#define SIGNALOBJECT GTK_OBJECT(WIDGET)
#define WINDOW (gw)

	GtkWindow *gw;
	GtkWidget *area;
	virtual void *getDrawable() {return GDKWINDOW;}
	virtual void destroy()
	{
		/*if (gw != NULL) gtk_widget_unref(GTK_WIDGET(gw));*/
	}
	virtual void setup(WObject obj)
	{
		gw = NULL;
		eweWindow::setup(obj);
	}

	virtual void close()
	{
		if (gw != NULL) {
			gtk_widget_hide(GTK_WIDGET(gw));
			gtk_widget_unrealize(GTK_WIDGET(gw));
			gtk_widget_set_events (GTK_WIDGET(gw),0);
			//gtk_widget_unref(GTK_WIDGET(gw));
			gw = NULL;
		}
	}
	virtual void create(int flags)
	{
		gw = (GtkWindow *)gtk_window_new(GTK_WINDOW_TOPLEVEL);
	if (flags & FLAG_CAN_RESIZE) gtk_window_set_policy(gw,TRUE,TRUE,TRUE);
	else gtk_window_set_policy(gw,FALSE,FALSE,FALSE);

  GtkWidget *vbox = gtk_vbox_new (FALSE, 0);
  gtk_container_add (GTK_CONTAINER (gw), vbox);
  gtk_widget_show (vbox);

  area = gtk_drawing_area_new ();
  gtk_drawing_area_size (GTK_DRAWING_AREA (area), 200, 200);
  gtk_box_pack_start (GTK_BOX (vbox), area, TRUE, TRUE, 0);
  gtk_widget_show (area);
  if (guiObjects == NULL) guiObjects = new GlobalGuiObjects(area);
		gtk_signal_connect(SIGNALOBJECT,"button-press-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"button-release-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"motion-notify-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"enter-notify-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"leave-notify-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"key-press-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"key-release-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"expose-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(SIGNALOBJECT,"configure-event",GTK_SIGNAL_FUNC(gotEvent),this);
#ifdef GTK_VERSION_2_0
		gtk_signal_connect(SIGNALOBJECT,"scroll-event",GTK_SIGNAL_FUNC(gotEvent),this);
#endif
		gtk_widget_set_events (WIDGET,0 |GDK_ALL_EVENTS_MASK
			 | GDK_POINTER_MOTION_MASK | GDK_POINTER_MOTION_HINT_MASK
			 |  GDK_EXPOSURE_MASK
			 | GDK_ENTER_NOTIFY_MASK| GDK_LEAVE_NOTIFY_MASK
			 | GDK_BUTTON_PRESS_MASK | GDK_BUTTON_RELEASE_MASK
			 | GDK_KEY_PRESS_MASK | GDK_KEY_RELEASE_MASK
#ifdef GTK_VERSION_2_0
| GDK_SCROLL_MASK
#endif
		);
		gtk_signal_connect(GTK_OBJECT(gw),"delete-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(GTK_OBJECT(gw),"key-press-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(GTK_OBJECT(gw),"key-release-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(GTK_OBJECT(gw),"focus-in-event",GTK_SIGNAL_FUNC(gotEvent),this);
		gtk_signal_connect(GTK_OBJECT(gw),"focus-out-event",GTK_SIGNAL_FUNC(gotEvent),this);
/* Causes segmentation fault during fast window opening/closing!
		gtk_widget_set_events(GTK_WIDGET(gw),GDK_FOCUS_CHANGE_MASK|GDK_KEY_PRESS_MASK|GDK_KEY_RELEASE_MASK|GDK_PROPERTY_CHANGE_MASK);
*/
		gtk_widget_realize(GTK_WIDGET(gw));

	}
/*
	int getIntArgument(void *obj,char *name)
	{
		GtkArg a[1];
		a[0].name = name;
		a[0].type = GTK_TYPE_INT;
		gtk_object_getv(GTK_OBJECT(obj),1,a);
		return a[0].d.int_data;
	}
*/
	void setIntArgument(void *obj,char *name,int value)
	{
#ifdef GTK_VERSION_2_0
		gtk_object_set_data(GTK_OBJECT(obj),name,(void *)value);
#else
		GtkArg a[1];
		a[0].name = name;
		a[0].type = GTK_TYPE_INT;
		a[0].d.int_data = value;
		gtk_object_setv(GTK_OBJECT(obj),1,a);
#endif
	}
	virtual bool getClientRect(rect &r)
	{
		gdk_window_get_size(TOPWINDOW,&r.width,&r.height);
		r.x = r.y = 0;
		return 1;
	}
	virtual bool getWindowRect(rect &r)
	{
		gdk_window_get_size(TOPWINDOW,&r.width,&r.height);
		gdk_window_get_position(TOPWINDOW,&r.x,&r.y);
		return 1;
	}
	eweRGBImage *toEweRGB(rect &r,eweRGBImage *dest = NULL)
	{
		rect image;
		getClientRect(image);
		image.x = image.y = 0;
		if (dest != NULL)
			if (dest->width != image.width || dest->height != image.height)
				dest = NULL;
		if (dest == NULL) dest = new eweRGBImage(r.width,r.height);
		eweRGBImage *ri = dest;
		rect fromImage = r.intersect(image);
		if (!fromImage.isValid()) return ri;
		MPixbuf pb = getPixbufFromDrawable((GdkDrawable *)getDrawable(),getColormap(),fromImage.x,fromImage.y,fromImage.width,fromImage.height);
		if (pb != NULL) {
			int xoff = fromImage.x-r.x, yoff = fromImage.y-r.y;
			gchar *bits = pb->pixels;
			int rs = pb->rowstride;
			int bpp = pb->hasAlpha ? 4 : 3;
			for (int y = 0; y<fromImage.height; y++){
				gchar *p = bits+(rs*y);
				uint32 *pix = ri->scanLine(y+yoff)+xoff;
				for (int x = 0; x<fromImage.width; x++, p += bpp)
					*pix++ = ((*p & 0xff) << 16) | ((*(p+1) & 0xff) << 8) | ((*(p+2) & 0xff) << 0);
			}
			destroyPixbuf(pb);
		}
		return ri;
	}
	void writeEweRGB(eweRGBImage &data,int destX,int destY,rect *destClip = NULL)
	{
		::writeEweRGB((GdkDrawable *)getDrawable(),NULL,data,destX,destY,destClip);
	}

	virtual void setTitle(TCHAR *title)
	{
		gtk_window_set_title(gw,title);
	}
	virtual void setLocation(int x,int y)
	{
#ifdef GTK_VERSION_2_0
		gtk_window_move(gw,x,y);
#else
		setIntArgument(gw,"x",x);
		setIntArgument(gw,"y",y);
#endif
	}
	virtual void setSize(int w,int h)
	{
#ifdef GTK_VERSION_2_0
		gtk_widget_set_size_request(area,w,h);
		//gtk_window_resize(gw,w,h);
#else
		setIntArgument(gw,"width",w);
		setIntArgument(gw,"height",h);
#endif
	}
	virtual void setRect(rect &r,bool isClient)
	{
		setSize(r.width,r.height);
		if (!isClient) setLocation(r.x,r.y);
	}
	virtual void hide()
	{
		gtk_widget_hide(GTK_WIDGET(gw));
	}
	virtual void show()
	{
		gtk_widget_show(GTK_WIDGET(gw));
	}
	virtual void showMaximized()
	{
#ifdef GTK_VERSION_2_0
		show();
		gdk_window_maximize(TOPWINDOW);
#else
		setLocation(0,0);
		setSize(gdk_screen_width(),gdk_screen_height());
		show();
#endif
	}
	virtual void showMinimized()
	{
#ifdef GTK_VERSION_2_0
		show();
		gdk_window_iconify(TOPWINDOW);
#else
		show();
#endif
	}
#define TASKBARSIZE 0

int adjustFlags(int flags,int turnOn,int turnOff,int *style,int *exstyle)
{
	int st, ex;

	if (style == NULL) style = &st;
	if (exstyle == NULL) exstyle = &ex;

	if (turnOn & FLAG_RESTORE) turnOff |= (FLAG_MAXIMIZE|FLAG_MINIMIZE);
	if (turnOn & FLAG_MAXIMIZE) turnOff |= FLAG_MINIMIZE;
	if (turnOn & FLAG_MINIMIZE) turnOff |= FLAG_MAXIMIZE;

	flags |= turnOn;
#ifdef WINCE
	turnOff |= (FLAG_CAN_MAXIMIZE|FLAG_CAN_MINIMIZE|FLAG_CAN_RESIZE);
#endif
	flags &= ~turnOff;

	if (flags & FLAG_FULL_SCREEN) flags |= FLAG_MAXIMIZE;
	if (!(flags & FLAG_HAS_TITLE))
		flags &= ~(FLAG_HAS_CLOSE_BUTTON|FLAG_CAN_MAXIMIZE|FLAG_CAN_MINIMIZE);//|FLAG_CAN_RESIZE);

	*style = *exstyle = 0;

#ifdef WINCE
#if defined(HPC) || defined(CASIOBE300)
		if (flags & FLAG_IS_DEFAULT_SIZE)
			flags &= ~(FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON);
		if (flags & FLAG_HAS_TITLE)  *style |= WS_CAPTION;
		else flags &= ~FLAG_HAS_CLOSE_BUTTON;
		if (flags & FLAG_HAS_CLOSE_BUTTON) *exstyle |= WS_EX_CAPTIONOKBTN;
#elif _WIN32_WCE >= 300 //PocketPC
	if (flags & FLAG_HAS_CLOSE_BUTTON) *exstyle |= WS_EX_CAPTIONOKBTN;
	if (flags & FLAG_IS_DEFAULT_SIZE)
		flags |= FLAG_HAS_TITLE; //Always on for PocketPC.
#else //PalmPC - has neither title bar nor close button.
	if (flags & FLAG_IS_DEFAULT_SIZE)
		flags &= ~(FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON);
#endif
/*
	if (!(flags & FLAG_IS_DEFAULT_SIZE) && (flags & FLAG_HAS_TITLE)){
		*style = WS_OVERLAPPED;
		if (flags & FLAG_HAS_CLOSE_BUTTON){
			*style |= WS_SYSMENU;
		}
	}
*/
#else // Desktop
/*
	if (flags & FLAG_HAS_TITLE)  {
		*style |= WS_CAPTION;
		if (flags & FLAG_CAN_MAXIMIZE) *style |= WS_MAXIMIZEBOX;
		if (flags & FLAG_CAN_MINIMIZE) *style |= WS_MINIMIZEBOX;
		if (flags & FLAG_CAN_RESIZE) *style |= WS_SIZEBOX;
		if (flags & FLAG_HAS_CLOSE_BUTTON)  *style |= WS_SYSMENU;
	}else{
		*style |= WS_POPUP;
		if (flags & FLAG_CAN_RESIZE) *style |= WS_SIZEBOX;
	}
*/
#endif
	//if (flags & FLAG_IS_VISIBLE) *style |= WS_VISIBLE;
	if (!(flags & FLAG_HAS_TITLE))
		flags &= ~(FLAG_HAS_CLOSE_BUTTON|FLAG_CAN_MAXIMIZE|FLAG_CAN_MINIMIZE|FLAG_CAN_RESIZE);
	return flags;
}


	virtual int getFlagsForSize(rect &requested,rect &screen,int flagsToSet,int flagsToClear)
  {
		int flags = 0;
		if (requested.width < 0 || requested.width >= screen.width)
			flags |= FLAG_IS_DEFAULT_SIZE;
		if (requested.height < 0 || requested.height >= screen.height)//-TASKBARSIZE)
			flags |= FLAG_IS_DEFAULT_SIZE;
		flags |= FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON;
		return adjustFlags(flags,flagsToSet,flagsToClear,NULL,NULL);
	}

public:

void paintEvent(GdkEventExpose *ev)
{
	class rect r;
	r.set(ev->area.x,ev->area.y,ev->area.width,ev->area.height);
	eweWindow::paintEvent(r);
}

void resizeEvent(GdkEventConfigure *ev)
{
	eweWindow::resizeEvent(ev->width,ev->height);
}

int getKeyModifiers(int state)
{
	int m = 0;
	if (state & GDK_SHIFT_MASK) m |= 0x4;
	if (state & GDK_CONTROL_MASK) m |= 0x2;
	if (state & GDK_MOD1_MASK) m |= 0x1;
	return m;
}
virtual void grabMouse(void* grabData)
{
	GdkEventButton* me = (GdkEventButton*)grabData;
	gdk_pointer_grab(GDKWINDOW,FALSE,(GdkEventMask)(GDK_POINTER_MOTION_MASK|GDK_BUTTON_RELEASE_MASK),
		NULL,NULL,me->time);
}
virtual void ungrabMouse(void* ungrabData)
{
	GdkEventButton* me = (GdkEventButton*)ungrabData;
	gdk_pointer_ungrab(me->time);
}

void mousePressEvent(GdkEventButton *me)
{
	eweWindow::mousePressed((int)me->x,(int)me->y,me->button == 3,getKeyModifiers(me->state),me);
}
void mouseReleaseEvent(GdkEventButton *me)
{
	eweWindow::mouseReleased((int)me->x,(int)me->y,me->button == 3,getKeyModifiers(me->state),me);
}
#ifdef GTK_VERSION_2_0
void mouseScrollEvent(GdkEventScroll *me)
{
	if (!setupMouseEvent((int)me->x,(int)me->y,(me->direction == GDK_SCROLL_UP || me->direction == GDK_SCROLL_LEFT)
		? 206 : 207, 0, getKeyModifiers(me->state))) return;
	doPost(true,true);
}
#endif
void mouseMoveEvent(GdkEventMotion *me)
{
	int x,y;
  	GdkModifierType state;

 	gdk_window_get_pointer (GDKWINDOW, &x, &y, &state);
	mouseMoved((int)me->x, (int)me->y, getKeyModifiers(me->state));
}
void mouseEnterLeaveEvent(GdkEventCrossing *me,bool isEnter)
{
	if (!setupMouseEvent(0,0,isEnter ? 204 : 205,false,0)) return; // FIX
	doPost(true,true);
}

#define F1  75192
#define F2  75193
#define F3  75194
#define F4  75195
#define F5  75196
#define F6  75197
#define F7  75198
#define F8  75199
#define F9  75200
#define F10  75201
#define F11  75202
#define F12  75203

#define KEYPAD_0  75300
#define KEYPAD_1  75301
#define KEYPAD_2  75302
#define KEYPAD_3  75303
#define KEYPAD_4  75304
#define KEYPAD_5  75305
#define KEYPAD_6  75306
#define KEYPAD_7  75307
#define KEYPAD_8  75308
#define KEYPAD_9  75309
#define KEYPAD_POINT  75310
#define KEYPAD_PLUS  75311
#define KEYPAD_MINUS  75312
#define KEYPAD_TIMES  75313
#define KEYPAD_DIVIDE  75314
#define KEYPAD_ENTER  75315

#define KEYPAD_INS  75320
#define KEYPAD_END  75321
#define KEYPAD_DOWN  75322
#define KEYPAD_PAGE_DOWN  75323
#define KEYPAD_LEFT  75324
//#define KEYPAD_  75325
#define KEYPAD_RIGHT  75326
#define KEYPAD_HOME  75327
#define KEYPAD_UP  75328
#define KEYPAD_PAGE_UP  75329
#define KEYPAD_DEL  75330

#define SOFTKEY1 75022
#define SOFTKEY2 75023

bool mapKey(int &source,int &modifiers)
{
	modifiers |= SPECIALKEY;
	if (source >= GDK_F1 && source <= GDK_F12){
		source = source-GDK_F1+F1;
	}
	else if (source >= GDK_KP_0 && source <= GDK_KP_9)
		source = source-GDK_KP_0+KEYPAD_0;
	else
	switch(source){

	case GDK_KP_Up : source = KEYPAD_UP; break;
	case GDK_KP_Down : source = KEYPAD_DOWN; break;
	case GDK_KP_Left : source = KEYPAD_LEFT; break;
	case GDK_KP_Right : source = KEYPAD_RIGHT; break;
	case GDK_KP_Home : source = KEYPAD_HOME; break;
	case GDK_KP_Page_Up : source = KEYPAD_PAGE_UP; break;
	case GDK_KP_Page_Down : source = KEYPAD_PAGE_DOWN; break;
	case GDK_KP_End : source = KEYPAD_END; break;
	case GDK_KP_Insert : source = KEYPAD_INS; break;
	case GDK_KP_Delete : source = KEYPAD_DEL; break;
	case GDK_KP_Decimal : source = KEYPAD_POINT; break;
	case GDK_KP_Add : source = KEYPAD_PLUS; break;
	case GDK_KP_Subtract : source = KEYPAD_MINUS; break;
	case GDK_KP_Multiply : source = KEYPAD_TIMES; break;
	case GDK_KP_Divide : source = KEYPAD_DIVIDE; break;
	case GDK_KP_Enter : source = KEYPAD_ENTER; break;
	//case GDK_KP_ : source = KEYPAD_; break;

  case 0xff55: source = 75000; break; //PageUp
  case 0xff56: source = 75001; break; //PageDown
  case 0xff50: source = 75002; break; //Home
  case 0xff57: source = 75003; break; //End
	case 0xff52: source = 75004; break; //up
  case 0xff54: source = 75005; break; //down
  case 0xff51: source = 75006; break; //left
  case 0xff53: source = 75007; break; //right
  case 0xff63: source = 75008; break; //insert
	case 0xff0d: source = 75009; break; //Return
	case 0xfe20: case 0xff09: source = 75010; break; //Backtab and Tab.
	case 0xff08: source = 75011; break; // Backspace
  case 0xff1b: source = 75012; break; //escape
  case 0xffff: source = 75013; break; //delete


	default:
			if (source > 0xf000) source = 0;
			modifiers &= ~SPECIALKEY;
	}
	return TRUE;
}
bool handleEvent(GdkEvent *ev)
{
	if (eweObject == 0){
		//printf("eweObject is 0!\n");
		return false;
	}else{
		holdObject(eweObject);
	}
	if (true){
		//printf("~"); fflush(stdout);
	}else return false; // FIXME - remove this line.
	switch(ev->type){
		case GDK_EXPOSE: paintEvent((GdkEventExpose *)ev); break;
		case GDK_CONFIGURE: resizeEvent((GdkEventConfigure *)ev);break;
		case GDK_BUTTON_PRESS: mousePressEvent((GdkEventButton *)ev); break;
		case GDK_BUTTON_RELEASE: mouseReleaseEvent((GdkEventButton *)ev); break;
		case GDK_MOTION_NOTIFY: mouseMoveEvent((GdkEventMotion *)ev); break;
		case GDK_LEAVE_NOTIFY: mouseEnterLeaveEvent((GdkEventCrossing *)ev,FALSE); break;
		case GDK_ENTER_NOTIFY: break;//mouseEnterLeaveEvent((GdkEventCrossing *)ev,TRUE); break;
		case GDK_DELETE: closeEvent(); break;
		case GDK_FOCUS_CHANGE: activated(((GdkEventFocus *)ev)->in); break;
		case GDK_KEY_PRESS:
		case GDK_KEY_RELEASE:
			{
			GdkEventKey *ek = (GdkEventKey *)ev;
			int key = ek->keyval;
			int mod = getKeyModifiers(ek->state);
			if (mapKey(key,mod)) keyEvent(key,mod,ev->type == GDK_KEY_PRESS);
			}
			break;
#ifdef GTK_VERSION_2_0
		case GDK_SCROLL: mouseScrollEvent((GdkEventScroll *)ev); break;
#endif
	}
	tick();
	releaseObject(eweObject);
	return TRUE;
}


virtual int setCursor(int cursorHandle)
{
	if (GDKWINDOW == NULL)
		return (0);
	gdk_window_set_cursor(GDKWINDOW,(GdkCursor *)cursorHandle);
	return 1;
}
virtual void setIcon(int icon)
{
	gtkIcon *i = (gtkIcon *)icon;
	if (i == NULL) return;
	gdk_window_set_icon(TOPWINDOW,TOPWINDOW,i->pixmap,i->mask);
}
virtual void toFront()
{
	//printf("Raising!!\n");
	if (GDKWINDOW == NULL) return;
	gdk_window_raise(TOPWINDOW);
}
virtual bool isVisible()
{
	if (GDKWINDOW == NULL) return false;
	return gdk_window_is_visible(GDKWINDOW);
}
//###################################################
};
//###################################################

static int gotEvent(GtkObject *who,GdkEvent *event,void *data)
{
	gtkWindow *win = (gtkWindow *)data;
	return win->handleEvent(event);
}
static int activated(GtkObject *who,void *data)
{
	gtkWindow *win = (gtkWindow *)data;
	return win->activated(true);
}

//###################################################
class gtkGraphics : public eweGraphics {
//###################################################
public:
GdkDrawable *drawable;
GdkGC *gc;

virtual void free()
{
	if (gc != NULL) gdk_gc_unref(gc);
	//if (curFont != NULL) gdk_font_unref(curFont);
	gc = NULL;
	curFont = NULL;
}
virtual void destroy() {free();}

virtual void setupDrawable(void *d)
{
	drawable = (GdkDrawable *)d;
	if (d != NULL) {
		gc = gdk_gc_new(drawable);
		gdk_gc_set_line_attributes(gc,1,GDK_LINE_SOLID,LINECAP,GDK_JOIN_MITER);
		setFont(defFont); //FIXME - default font
	}
}

bool activateBrush()
{
	BRUSH *b = getCurBrush();
	if (!b) return FALSE;
	doSetForeground(b->color);
	return TRUE;
}
bool activatePen()
{
	PEN *p = getCurPen();
	if (!p) return FALSE;
	//doSetForeground(p->color);
	return TRUE;
}
virtual void drawLine(int x1,int y1,int x2,int y2)
{
	if (gc == NULL) return;
	activatePen();
	gdk_draw_line(drawable,gc,tx+x1,ty+y1,tx+x2,ty+y2);
}
virtual void drawRect(int x,int y,int width,int height,int fillIt = 0)
{
	if (gc == NULL || width <= 0 || height <= 0) return;
	if (fillIt){
		COLORREF *c = setupBrush();
		gdk_draw_rectangle(drawable,gc,TRUE,x+tx,y+ty,width,height);
		resetBrush(c);
	}
	if (!fillIt || activatePen()){
		//activatePen();
		gdk_draw_rectangle(drawable,gc,FALSE,x+tx,y+ty,width-1,height-1);
	}
}
virtual void drawEllipse(int x,int y,int width,int height,int fillIt = 0)
{
	if (gc == NULL) return;
	if (fillIt){
		COLORREF *c = setupBrush();
		gdk_draw_arc(drawable,gc,TRUE,x+tx,y+ty,width,height,0,64*360);
		resetBrush(c);
	}
	if (!fillIt || activatePen())
		gdk_draw_arc(drawable,gc,FALSE,x+tx,y+ty,width,height,0,64*360);
}
virtual void fillPolygon(int *x,int *y,int length)
{
	if (gc == NULL) return;
	GdkPoint *points = toPoints(x,y,length,tx,ty);
	COLORREF *c = setupBrush();
	gdk_draw_polygon(drawable,gc,TRUE,points,length);
	resetBrush(c);
	if (activatePen())
		gdk_draw_polygon(drawable,gc,FALSE,points,length);
	delete points;
}
#ifdef GTK_VERSION_2_0

/* From a HOWTO
 layout = gtk_widget_create_pango_layout (widget, text);
 fontdesc = pango_font_description_from_string ("Luxi Mono 12");
 pango_layout_set_font_description (layout, fontdesc);
 gdk_draw_layout (..., layout);
 pango_font_description_free (fontdesc);
 g_object_unref (layout);
*/


virtual void drawText(int x, int y,WCHAR *text,int count)
{
	if (curFont == NULL) {return;}
	if (guiObjects == NULL) return;
	GtkWidget* widget = guiObjects->mainWidget;
	PangoLayout* layout = guiObjects->layout;//gtk_widget_create_pango_layout(widget,out);
	pango_layout_set_font_description(layout,((gtkPangoFont*)curFont.nativeFont)->description);
	setPangoLayoutText(layout,text,count);
	//PangoFontDescription* fontdesc = pango_font_description_from_string("Luxi Mono 12");
	gdk_draw_layout(drawable,gc,x+tx,y+ty/*+curFont.ascent*/,layout);
	//pango_font_description_free(fontdesc);
	//g_object_unref(layout);
}
#else
virtual void drawText(int x,int y,WCHAR *text,int count)
{
	if (curFont == NULL) {return;}
	if (gc != NULL) {
		GdkWChar *t = toWC(text,count);
		gdk_draw_text_wc(drawable,(GdkFont*)curFont.nativeFont,gc,x+tx,y+ty+curFont.ascent,t,count);
		delete t;
	}
}
#endif

void toColor(int red,int green,int blue,GdkColor &c)
{
	c.red = (gushort)(red*256|red);
	c.green = (gushort)(green*256|green);
	c.blue = (gushort)(blue*256|blue);
	gdk_colormap_alloc_color(gdk_colormap_get_system(),&c,false,true);
}
virtual void doSetForeground(COLORREF color)
{
/*
	GdkColor c;
	toColor(color.red,color.green,color.blue,c);
	if (gc != NULL) {
		gdk_gc_set_foreground(gc,&c);
		//gdk_gc_set_background(gc,&c);
	}
*/
	gdk_rgb_gc_set_foreground(gc,color.toRGB());
}
GdkLineStyle toStyle(int penType)
{
	switch(penType){
		case 1: return GDK_LINE_ON_OFF_DASH;
	}
	return GDK_LINE_SOLID;
}
virtual void doSetPen(PEN *p)
{
	if (p == NULL){
		gdk_gc_set_line_attributes(gc,1,GDK_LINE_SOLID,LINECAP,GDK_JOIN_MITER);
		setForeground(COLORREF(0,0,0));
	}else{
		gdk_gc_set_line_attributes(gc,p->thickness,toStyle(p->style),LINECAP,GDK_JOIN_MITER);
		setForeground(p->color);
	}
}
virtual void doSetClip(rect &r)
{
	GdkRectangle gr;
	gr.x = tx+r.x; gr.y = ty+r.y;
	gr.width = r.width; gr.height = r.height;
	//gr.x = gr.y = 10;
	//printf("%d %d %d %d\n",gr.x,gr.y,gr.width,gr.height);
	//gr.width = gr.height = 20;
	gdk_gc_set_clip_rectangle(gc,&gr);
}
virtual void doClearClip()
{
	gdk_gc_set_clip_rectangle(gc,NULL);
}
virtual void drawEweRGBImage(EweRGBImage ei,int x,int y)
{
	::writeEweRGB(drawable,gc,*ei,x+tx,y+ty,NULL);
}
virtual void drawEweImage(EweImage image,int x,int y)
{
	gdk_draw_pixmap(drawable,gc,(GdkPixmap *)image->getDrawable(),0,0,x+tx,y+ty,-1,-1);
}
virtual int getTextWidth(WCHAR *ch,int count)
{
		if (curFont == NULL) return 10*count;
#ifdef GTK_VERSION_2_0
		if (guiObjects == NULL) return 10*count;
		gtkPangoFont *f = (gtkPangoFont*)curFont.nativeFont;
		return guiObjects->textWidth(f->description,ch,count);
#else
		GdkWChar *gwc = new GdkWChar[count];
		for (int i = 0; i<count; i++) gwc[i] = ch[i];
		int ret = gdk_text_width_wc((GdkFont*)curFont.nativeFont,gwc,count);
		delete gwc;
		return ret;
#endif
}
//###################################################
};
//###################################################


static int timeoutFunction(void *data);

//###################################################
class gtkSystem : public eweSystem {
//###################################################
public:
	int curTimeout;
	int timeoutTag;
	gtkSystem()
	{
		curTimeout = -1;
		gtk_init(NULL,NULL);
		gdk_rgb_init();
	}
	virtual bool getTrueScreenRect(rect &r)
	{
		r.x = r.y = 0; r.width = gdk_screen_width(); r.height = gdk_screen_height();
		return true;
	}

	virtual EweWindow getNewWindow() { return new gtkWindow;}
	virtual EweFontMetrics getNewFontMetrics() {return new gtkFontMetrics;}
	virtual EweImage getNewImage(int width,int height,int options,bool fullCreate = false) {return new gtkImage(width,height,options,fullCreate);}
	virtual EweGraphics getNewGraphics() {return new gtkGraphics();}
	virtual ImageMaker getNewImageMaker(EweImage im) {return new gtkImageMaker(im);}
	virtual FONT createFont(class graphics_font& f)
	{
		FONT ret;
		ret.nativeFont = loadFont(&f,ret.ascent,ret.descent);
		ret.leading = 0;
		if (defFont == NULL) defFont = ret;
		return ret;
	}
//====================================================
// This should tell the GUI to generate an timer event
// after the specified interval which is specified in milliseconds.
// The result of that event should eventually result in
// a call to tick().
//
// time can be INFINITE or <= 0 or a valid wait time.
//
virtual void setTimerInterval(int time)
{
	if (curTimeout != -1){
		if (curTimeout < time) return;
		gtk_timeout_remove(timeoutTag);
		curTimeout = -1;
	}
	if (time == INFINITE) time = 10000;
	if (time <= 0) time = 0;
	// This prevents certain events (e.g. Paint) from being always pushed aside by coroutines.
	if (time == 0 && gtk_events_pending()) time = 1;
	if (time > MaxTime) time = MaxTime;
	curTimeout = time;
	timeoutTag = gtk_timeout_add(time,timeoutFunction,this);
}
//
// Cause a timer event to be generated immediately.
//
virtual void forceTimerEvent()
{
	setTimerInterval(0);
}

class test_lock : public mThread {
protected:
	void run(void *data)
	{
		gdk_threads_enter();
		 printf("I've got the lock!\n");
	   printf("Napping for 5 seconds...\n");
		nap(5000);
		gdk_threads_leave();
	   printf("Now I've left...\n");
	}
};
virtual int doGuiLoop()
{
	gtk_main();
	return 1;
}

virtual EweWindow getParent(EweWindow win) {return NULL;}
virtual void getParentRect(EweWindow win,rect &r,bool doRotate)
{
	if (win == NULL){
		getScreenRect(r);
	}else{
		getScreenRect(r);
  	}
	if (doRotate) {
		int t = r.width;
		r.width = r.height;
		r.height = t;
	}
}
virtual void setMainWindow(EweWindow win,int flags)
{
}
virtual TCHAR **listFonts(WObject surface,int &num)
{
#ifdef GTK_VERSION_2_0
	PangoFontFamily**ff;
	if (guiObjects != NULL){
		pango_context_list_families(guiObjects->context,&ff,&num);
		TCHAR **all = new TCHAR*[num];
		for (int i = 0; i<num; i++){
			PangoFontFamily* p = ff[i];
			all[i] = copyString(pango_font_family_get_name(p));
		}
		g_free(ff);
		return all;
	}
#else
	{
	TCHAR **all = new TCHAR *[3];
	all[0] = copyString("Helvetica");
	all[1] = copyString("Courier");
	all[2] = copyString("Times");
	num = 3;
	return all;
	}
#endif
}
/**
* This is the only one apart from DEFAULT_CURSOR you should use directly with
* ewe.sys.Vm.setCursor(). All others should be used with Control.setCursor().
**
public static final int WAIT_CURSOR  = 1;
public static final int IBEAM_CURSOR  = 2;
public static final int HAND_CURSOR  = 3;
public static final int CROSS_CURSOR  = 4;
public static final int LEFT_RIGHT_CURSOR  = 5;
public static final int UP_DOWN_CURSOR  = 6;
/**
* This cursor is an arrow and an hour glass. Use this when an individual component
* will not respond because it is busy, but other components will work.
**
public static final int BUSY_CURSOR = 7;
public static final int DRAG_SINGLE_CURSOR = 8;
public static final int DRAG_MULTIPLE_CURSOR = 9;
public static final int COPY_SINGLE_CURSOR = 10;
public static final int COPY_MULTIPLE_CURSOR = 11;
public static final int DONT_DROP_CURSOR = 12;
public static final int MOVE_CURSOR = 13;
public static final int RESIZE_CURSOR = 14;
public static final int INVISIBLE_CURSOR = 15;

public static final int NO_CURSOR = 0;
public static final int DEFAULT_CURSOR = 0;
*/

int getCursor(int which)
{
	GdkCursorType toGet = GDK_LAST_CURSOR;
	switch(which){
		case 1: toGet = GDK_WATCH; break;
		case 2: toGet = GDK_XTERM; break;
		//case 3: toGet = GDK_HAND2; break;
		case 4: toGet = GDK_CROSS; break;
		case 5: toGet = GDK_SB_H_DOUBLE_ARROW; break;
		case 6: toGet = GDK_SB_V_DOUBLE_ARROW; break;
		case 13: toGet = GDK_FLEUR; break;
		case 14: toGet = GDK_SIZING; break;
	}
	if (toGet == GDK_LAST_CURSOR) return 0;
	return (int)gdk_cursor_new(toGet);
}

//###################################################
};
static gtkSystem defaultSystem;
EweSystem ewe = &defaultSystem;
//###################################################

static int timeoutFunction(void *data)
{
	gtkSystem *es = (gtkSystem *)data;
	//printf("."); fflush(stdout);
	es->curTimeout = -1;
	es->tick();
	return false;
}
