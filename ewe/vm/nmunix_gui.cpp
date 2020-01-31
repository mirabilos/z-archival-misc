__IDSTRING(rcsid_nmunix_gui, "$MirOS: contrib/hosted/ewe/vm/nmunix_gui.cpp,v 1.4+git1 2008/04/11 00:27:24 tg Exp $");

//###################################################
class EventInterrupter{
//###################################################
int writePipe, readPipe;
int written;
static pthread_mutex_t mutex;
struct timeval timeout;
fd_set readSet;

public:
	EventInterrupter()
	{
		int pipes[2];
		pipe(pipes);
		writePipe = pipes[1];
		readPipe = pipes[0];
		written = 0;
		FD_ZERO(&readSet);
		FD_SET(readPipe,&readSet);
	}
	void lock()
	{
		pthread_mutex_lock(&mutex);
	};
	void unlock()
	{
		pthread_mutex_unlock(&mutex);
	};
	//
	int getReadFD() { return readPipe;}
	//
	void resetInterrupt() //Only call this if an interrupt did occur.
	{
		lock();
		int did = written;
		written = 0;
		unlock();
		int data;
		for (int i = 0; i<did; i++)
			read(readPipe,&data,1);
	}
	//
	void triggerInterrupt()
	{
		lock();
		written++;
		unlock();
		int data = 0;
		write(writePipe,&data,1);
	}
	//
	void waitWithSelect(int timerInterval)
	{
		if (timerInterval == 0) return;
		if (timerInterval < 0 || timerInterval > 10000) timerInterval = 10000;
		timeout.tv_sec = timerInterval/1000;
		timeout.tv_usec = (timerInterval%1000)*1000;
		select(readPipe+1,&readSet,NULL,NULL,&timeout);
	}
//###################################################
};
//###################################################
static EventInterrupter interrupter;
pthread_mutex_t EventInterrupter::mutex = PTHREAD_MUTEX_INITIALIZER;

static Var ControlPaintChildren(Var stack[])
{
	return returnException("java/lang/UnsatisfiedLinkError","Use non-native methods for painting controls");
}

static Var ImageLoadBitmapFile(Var stack[])
{
	return returnException("java/lang/UnsatisfiedLinkError","Use java decode instead");
}

void exitSystem(int code)
{
	exit(code);
}

TCHAR *copyString(const TCHAR *from)
{
	if (from == NULL) return NULL;
	int len = strlen(from);
	TCHAR *ret = new TCHAR[len+1];
	strcpy(ret,from);
	return ret;
}

struct _win32rect {
	int top, left, bottom, right;
};
typedef struct _win32rect RECT;

class rect
{
	public:
	int x, y, width, height;
	rect(int xx,int yy,int ww,int hh) {x = xx, y = yy, width = ww, height = hh;}
	rect() {x = y = width = height = 0;}

	class rect & set(int xx, int yy, int ww, int hh)
	{
		x = xx, y = yy, width = ww, height = hh;
		return *this;
	}

	class rect & moveBy(int dx,int dy) {x += dx; y += dy; return *this;}

	bool isValid() {return width > 0 && height > 0;}

	class rect intersect(class rect &r)
	{
		rect dest;
		int x = 0,y = 0,width = 0,height = 0;
		rect * r1 = this;
		rect * r2 = &r;
		rect * left = r1;
		rect * right = r2;
		if (r2->x < r1->x) {
			left = r2;
			right = r1;
		}
		x = right->x;
		width = left->x+left->width-right->x;
		if (width > 0){
			if (width > right->width) width = right->width;
			rect * above = r1;
			rect * below = r2;
			if (r2->y < r1->y){
				above = r2;
				below = r1;
			}
			y = below->y;
			height = above->y+above->height-below->y;

			if (height > 0) {
				if (height > below->height) height = below->height;
				return dest.set(x,y,width,height);
			}
		}
		return dest.set(0,0,0,0);
	}

};

class point
{
	public:
	int x, y;
	point() {x = y = 0;}
	point(int xx,int yy){x = xx, y = yy;}
};

class colorref {
public:
	unsigned char alpha, red, green, blue;
	colorref(){alpha = red = green = blue = 0;}
	colorref(int32 c){alpha = 0xff;
		red = (unsigned char)((c >> 16) & 0xff),
		green = (unsigned char)((c >> 8) & 0xff),
		blue = (unsigned char)((c >> 0) & 0xff);}
	colorref(int r,int g,int b){alpha = 0xff;
		alpha = 0xff,
		red = (unsigned char)(r & 0xff),
		green = (unsigned char)(g & 0xff),
		blue = (unsigned char)(b & 0xff);}
	colorref(int a,int r,int g,int b){alpha = 0xff;
		alpha = (unsigned char)(a & 0xff),
		red = (unsigned char)(r & 0xff),
		green = (unsigned char)(g & 0xff),
		blue = (unsigned char)(b & 0xff);}
	int toRGB()
	{
		return ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
	}
	int toARGB()
	{
		return toRGB() | ((alpha & 0xff) << 24);
	}
};
typedef class colorref COLORREF;

static rect & ewe2rect(WObject source,rect &dest)
{
	if (source == 0) return dest;
	dest.x = WOBJ_RectX(source);
	dest.y = WOBJ_RectY(source);
	dest.width = WOBJ_RectWidth(source);
	dest.height = WOBJ_RectHeight(source);
	return dest;
}
static void rect2ewe(rect &source,WObject dest)
{
	if (dest == 0) return;
	WOBJ_RectX(dest) = source.x;
	WOBJ_RectY(dest) = source.y;
	WOBJ_RectWidth(dest) = source.width;
	WOBJ_RectHeight(dest) = source.height;
}

static void setLong(WObject obj,int64 d);
static int64 getLong(WObject obj);

//###################################################


typedef class linkable *Linkable;
//###################################################
class linkable {
//###################################################

public:
	Linkable next;
	linkable(){
		next = NULL;
	}

void add(Linkable another)
{
	Linkable last;

	if (another == NULL) return;
	for (last = this; last->next != NULL; last = last->next)
		;
	last->next = another;
	if (another != NULL) another->next = NULL;
}

void remove(Linkable what)
{
	Linkable last;

	if (what == NULL) return;
	for (last = this; last->next != what && last->next != NULL; last = last->next)
		;
	if (last->next == what) last->next = what->next;
	what->next = NULL;
}

Linkable pop()
{
	Linkable ret = next;
	if (ret != NULL) next = ret->next;
	return ret;
}

//###################################################
};
//###################################################


typedef class runnable *Runnable;
//###################################################
class runnable : public linkable{
//###################################################

public:
	virtual void run() = 0;
//###################################################
};
//###################################################


typedef class windowTask *WindowTask;
//###################################################
class windowTask : public runnable{
//###################################################

public:
	class eweWindow *window;
	windowTask(class eweWindow *win){window = win;}
	virtual void run() {}
//###################################################
};
//###################################################

//###################################################
class closeWindowTask : public windowTask{
//###################################################
public:
	closeWindowTask(class eweWindow *closing) : windowTask(closing) {}

	void removeTasks(class eweWindow *win,windowTask* taskList)
	{
		for (windowTask *wait = taskList; wait->next != NULL;){
			windowTask *task = (windowTask *)wait->next;
			if (task->window == win){
				wait->next = task->next;
				delete task;
			}else
				wait = task;
		}
	}
	void addClosing(class eweWindow *closing,class windowTask *waiting)
	{
		for (windowTask *wt = (windowTask *)next; wt != NULL; wt = (windowTask *)wt->next)
			if (wt->window == closing) return;
		closeWindowTask *cw = new closeWindowTask(closing);
		cw->next = next;
		next = cw;
		removeTasks(closing,waiting);
	}

	void removeClosed(class eweWindow *closed)
	{
		removeTasks(closed,this);
	}

	void addIfNotClosing(class windowTask *newTask,class windowTask *waiting)
	{
		for (windowTask *wt = (windowTask *)next; wt != NULL; wt = (windowTask *)wt->next){
			if (wt->window == newTask->window) {
				delete newTask;
				return;
			}
		}
		waiting->add(newTask);
	}

	void run(){}
//###################################################
};
//###################################################

//###################################################
class resizeTask : public windowTask {
//###################################################
	int width, height;

public:
	resizeTask(eweWindow *win,int w,int h) : windowTask(win)
	{
		width = w;
		height = h;
	}
	virtual void run();

//###################################################
};
//###################################################

//###################################################
class eventTask : public windowTask {
//###################################################
	int type;

public:
	eventTask(eweWindow *win,int ty) : windowTask(win)
	{
		type = ty;
	}
	virtual void run();

//###################################################
};
//###################################################


typedef class eweSystem * EweSystem;
extern EweSystem ewe;

//###################################################
class FONT
//###################################################
{
public:
	void* nativeFont;
	int ascent;
	int descent;
	int leading;
public:
	FONT(void* val = NULL) {nativeFont = val;}
	FONT operator = (void *val) {nativeFont = val; return *this;}
	bool operator == (void *val) {return nativeFont == val;}
	bool operator != (void *val) {return nativeFont != val;}
//###################################################
};
//###################################################


//###################################################
class eweSystem {
//###################################################
windowTask deferred;
closeWindowTask closing;

public:

//
// This is the maximum time to wait between ticks in milliseconds.
//
int MaxTime;

eweSystem() : closing(NULL), deferred(NULL), MaxTime(100){}

void closingWindow(class eweWindow *win)
{
	closing.addClosing(win,&deferred);
}
void closedWindow(class eweWindow *win)
{
	closing.removeClosed(win);
}
void addWindowTask(windowTask *task)
{
	closing.addIfNotClosing(task,&deferred);
}

bool doNextRunnable()
{
	Runnable r = (Runnable)deferred.pop();
	if (r != NULL) {
		r->run();
		delete r;
		return TRUE;
	}
	return FALSE;
}

virtual bool getScreenRect(rect &r)
{
		r.x = 0, r.y = 0, r.width = 640, r.height = 480;
		getTrueScreenRect(r);
		if (g_mainWinWidth != 0) r.width = g_mainWinWidth;
		if (g_mainWinHeight != 0) r.height = g_mainWinHeight;
		//printf("Got: %d %d\n",r.width,r.height);
		return true;
}
//
// This is called before doing an EweIteration in the
// tick() method.
//
virtual void doBeforeIteration(){}
//
virtual int getCursor(int which) {return 0;}
//
virtual bool getGuiInfo(int which, WObject source, WObject dest, int options, Var &ret){return false;}
virtual bool getTrueScreenRect(rect &r) = 0;
virtual class eweWindow *getNewWindow() = 0;
virtual class eweFontMetrics *getNewFontMetrics() = 0;
virtual class eweImage *getNewImage(int width,int height,int options,bool fullCreate = false) = 0;
virtual class eweGraphics *getNewGraphics() = 0;
virtual ImageMaker getNewImageMaker(class eweImage *image) = 0;
virtual FONT createFont(class graphics_font &f) = 0;
//
//
//====================================================
// This should tell the GUI to generate an timer event
// after the specified interval which is specified in milliseconds.
// The result of that event should eventually result in
// a call to tick().
//
// time can be INFINITE or <= 0 or a valid wait time.
//
virtual void setTimerInterval(int time) = 0;
//
// Cause a timer event to be generated immediately.
//
virtual void forceTimerEvent() = 0;
virtual class eweWindow *getParent(class eweWindow *win) = 0;
virtual void getParentRect(class eweWindow *win,rect &r,bool doRotate) = 0;
virtual void setMainWindow(class eweWindow *window,int flags) = 0;
//
// This returns an array of font names.
// Each name must be a new copy of a string, since each will be deleted after.
//
virtual TCHAR **listFonts(WObject surface,int &num) = 0;
virtual int doGuiLoop() = 0;
//====================================================

//#define LOCKTHREAD
//#define UNLOCKTHREAD
//#define PULSEEVENT ewe->forceTimerEvent();

//------------------------------------------------------
	static bool isLocked, inTick;
	static bool grab(){return isLocked ? false : (isLocked = true);}
	static void release() { isLocked = false;}
	static void doTick() {ewe->tick();}
//------------------------------------------------------
virtual void tick()
//------------------------------------------------------
{
	LOCKTHREAD
	if (inTick) return;
	inTick = true;
	doBeforeIteration();
	while (doNextRunnable());
	inTick = false;
	grab();
	int wait = NormalEweIteration();
	release();
	setTimerInterval(wait);
	UNLOCKTHREAD
}
//------------------------------------------------------
virtual int mainLoop()
//------------------------------------------------------
{
	tick();
	return doGuiLoop();
}

//###################################################
};

bool eweSystem::inTick = false, eweSystem::isLocked = false;
//###################################################

#define USE_EXTERNAL_MAIN_LOOP
void MainLoop()
{
	ewe->mainLoop();
}

static rect getScreenRect()
{
 	rect r;
	ewe->getTrueScreenRect(r);
	return r;
}
//###################################################
typedef class eweNativeObject *EweNativeObject;
//###################################################
class eweNativeObject {
//###################################################
public:
	WObject eweObject;

	eweNativeObject()
	{
		eweObject = 0;
	}
	virtual ~eweNativeObject() {/*printf("Deleting: %x\n",this);*/}

	virtual void **getNativeStoragePointer(WObject obj) = 0;
	virtual void destroy() = 0;
	virtual void setup(WObject obj) = 0;
	virtual void requestDelete() {delete this;}
	void setupNativeObject(WObject obj)
  	{
		eweObject = obj;
		void **native = getNativeStoragePointer(obj);
		if (native != NULL) *native = this;
		setup(obj);
	}

	void clearNativeObject()
	{
		if (eweObject != 0){
			void **native = getNativeStoragePointer(eweObject);
			if (native != NULL) *native = NULL;
			eweObject = 0;
		}
	}

	void destroyAndClear()
	{
		clearNativeObject();
		destroy();
	}

//###################################################
};
//###################################################

typedef class eweDrawable *EweDrawable;
//###################################################
class eweDrawable : public eweNativeObject {
//###################################################

public:
int surfaceType;

virtual void *getDrawable() = 0;

//###################################################
};
//###################################################

static graphics_font *savedFonts = NULL;

//###################################################
class graphics_font : public linkable {
//###################################################
public:
	UtfString name; //The name will be zero terminated.
	int style;
	int size;
	FONT nativeFont;

bool equals(graphics_font &other)
{
	return (
		other.style == style && other.size == size && other.name.len == name.len &&
		strcmp(other.name.str,name.str) == 0);
}

/** A plain font style. */
#define FONT_PLAIN   0
/** A bold font style. */
#define FONT_BOLD    0x1
/** An italic font style. */
#define FONT_ITALIC  0x2
/** An underlined font style. */
#define FONT_UNDERLINE  0x4
/** An outlined font style. */
#define FONT_OUTLINE  0x8
/** A strikethrough font style. */
#define FONT_STRIKETHROUGH  0x10
/** A superscript font style. */
#define FONT_SUPERSCRIPT  0x80
/** A subscript font style. */
#define FONT_SUBSCRIPT  0x100

	//graphics_font(){name = NULL;}
	graphics_font(WObject font){
		if (font != 0){
			char *n = stringToNativeText(WOBJ_FontName(font));
			if (!n)
				asprintf(&n, "UnnamedFont%08X", (unsigned)font);
			name.str = copyString(n);
			name.len = strlen(name.str);
			free(n);
			style = WOBJ_FontStyle(font);
			size = WOBJ_FontSize(font);
		}
	}

	~graphics_font(){
		if (name.str) delete name.str;
	}

	FONT intern()
	{
		for (graphics_font* gf = savedFonts; gf != NULL; gf = (graphics_font*)gf->next){
			if (gf->equals(*this)){
				delete this;
				return gf->nativeFont;
			}
		}
		nativeFont = ewe->createFont(*this);
		next = savedFonts;
		savedFonts = this;
		return nativeFont;
	}

/*
	class graphics_font *getCopy()
	{
		graphics_font *gr = new graphics_font();
		gr->name.str = copyString(name.str);
		gr->name.len = name.len;
		gr->size = size;
		gr->style = style;
		return gr;
	}
*/
//###################################################
};
//###################################################


//###################################################
typedef class eweFont * EweFont;
class eweFont : public eweNativeObject {
//###################################################

FONT nativeFont;

public:

virtual void destroy(){} // Native fonts are cached.

virtual void setup(WObject obj)
{
	nativeFont = (new graphics_font(obj))->intern();
}
virtual void **getNativeStoragePointer(WObject obj)
{
	if (obj != 0) return &(WOBJ_FontNative(obj));
	return NULL;
}
FONT getNativeFont() {return nativeFont;}

static FONT getNativeFont(WObject obj)
{
	if (obj == 0) return NULL;
	if (WOBJ_FontNative(obj) == NULL)
		(new eweFont())->setupNativeObject(obj);
	FONT ret = ((EweFont)WOBJ_FontNative(obj))->nativeFont;
	return ret;
}
//###################################################
};
//###################################################

//###################################################
typedef class eweFontMetrics * EweFontMetrics;
class eweFontMetrics : public eweNativeObject {
//###################################################
protected:
FONT nativeFont;

virtual void **getNativeStoragePointer(WObject obj)
{
	if (obj != 0) return &(WOBJ_FontMetricsNative(obj));
	return NULL;
}
protected:
	virtual void setup(WObject obj)
	{
		nativeFont = eweFont::getNativeFont(WOBJ_FontMetricsFont(obj));
		WOBJ_FontMetricsAscent(obj) = nativeFont.ascent;
		WOBJ_FontMetricsDescent(obj) = nativeFont.descent;
		WOBJ_FontMetricsLeading(obj) = nativeFont.leading;
	}

public:
FONT getNativeFont() {return nativeFont;}
virtual int width(WCHAR *chars,int length) = 0;
virtual int width(WCHAR ch) = 0;
//###################################################
};
//###################################################

static inline int blend(int as,int ad,int s,int d,int shift)
{
	int cs, cd, ascs, adcd;
	ascs = (((s >> shift) & 0xff)*as) >> 8; cd = (d >> shift) & 0xff;
	if (ad == 0xff) ascs += cd-((cd *as) >> 8);
	else if (ad != 0){
		adcd = (cd*ad) >> 8; ascs += adcd; adcd = (adcd*as)>>8; ascs -= adcd;
	}
	if (ascs < 0) ascs = 0;
	else ascs &= 0xff;
	return	ascs << shift;
}

static int numImages = 0;

#include "images.cpp"

//###################################################
typedef class eweRGBImage *EweRGBImage;
class eweRGBImage {
//###################################################

public:
bool usesAlpha, deletePixels;
uint32 *pixels;
int width;
int height;
bool persist;
rect valid;

eweRGBImage(int w,int h,bool alpha = false,uint32 *pixelData = NULL)
{
	numImages++;
	//if (numImages % 10 == 0) printf("Images: %d\n",numImages);
	valid.x = valid.y = 0;
	valid.width = w;
	valid.height = h;

	persist = false;
	width = w, height = h;
	usesAlpha = alpha;
	pixels = NULL;
	deletePixels = true;
	if (pixelData != NULL){
		pixels = pixelData;
		deletePixels = false;
	}else
		if (width*height >= 0)
			pixels = new uint32[width*height];
}

~eweRGBImage()
{
	if (persist) printf("Should not delete me!\n");
	if (pixels != NULL && deletePixels) delete pixels;
	pixels = NULL;
	numImages--;
}


virtual EweRGBImage getNew(int width,int height)
{
	return new eweRGBImage(width,height);
}

EweRGBImage getSubArea(rect &source,EweRGBImage dest = NULL)
{
	rect actual = valid;//(0,0,width,height);
	rect s = actual.intersect(source);
	if (dest == NULL) dest = getNew(source.width,source.height);
	dest->valid.x = s.x-source.x, dest->valid.y = s.y-source.y;
	dest->valid.width = s.width, dest->valid.height = s.height;
	uint32 *src = pixels+(s.y*width+s.x), *dst = dest->pixels+(s.y-source.y)*source.width+(s.x-source.x);
	for (int r = 0; r<s.height; r++, src += width, dst += source.width)
		memcpy(dst,src,4*s.width);
	return dest;
}
void fromSubArea(EweRGBImage area,int destX,int destY)
{
	if (area == this) return;
	rect actual(0,0,width,height);
	rect source(destX,destY,area->width,area->height);
	rect s = actual.intersect(source);
	uint32 *dst = pixels+(s.y*width+s.x), *src = area->pixels+(s.y-source.y)*source.width+(s.x-source.x);
	for (int r = 0; r<s.height; r++, src += source.width, dst += width)
		memcpy(dst,src,4*s.width);
}

uint32 *scanLine(int line)
{
	if (pixels == NULL || line >= height || line < 0) {
		printf("NULL: %p %d %d\n",pixels,line,height);
		return NULL;
	}
	return pixels+(line*width);
}

eweRGBImage *scale(rect &source,int newWidth,int newHeight)
{
	if (newHeight < 0) newHeight = 0;
	if (newWidth < 0) newWidth = 0;

	rect actual(0,0,width,height);
	rect s = actual.intersect(source);
	int h = newHeight, w = newWidth;
	int sh = s.height, sw = s.width;

	eweRGBImage *ret = new eweRGBImage(w,h,usesAlpha);

	double xsc = newWidth > 0 ? (double)s.width/(double)newWidth : 0;
	double ysc = newHeight > 0 ? (double)s.height/(double)newHeight : 0;
	double y = 0;
	for (int line = 0; line < h; line++, y += ysc){
		uint32 *dl = ret->scanLine(line);
		if (y >= sh) y = s.height-1;
		uint32 *sl = scanLine((int)y+s.y);
		sl += s.x;
		double x = 0;
		for (int col = 0; col < w; col++, x += xsc){
			if (x >= sw) x = sw-1;
			dl[col] = sl[(int)x];
		}
	}
	return ret;
}

eweRGBImage *createMask(int transparentColor,bool reverse = false)
{
	eweRGBImage *ret = new eweRGBImage(width,height,usesAlpha);
	uint32 *s = pixels;
	uint32 *d = ret->pixels;
	for (int i = 0; i<width*height; i++, d++, s++){
		*d = 0xffffff;
		if (reverse){
			if ((*s & 0xffffff) == transparentColor) *d = 0;
		}else{
			if ((*s & 0xffffff) != transparentColor) *d = 0;
		}
	}
	return ret;
}

virtual void release()
{
	if (!persist) delete this;
}

rgbSection getRgbSection(rect& area)
{
	rgbSection r;
	r.width = area.width;
	r.height = area.height;
	r.rowStride = width;
	r.usesAlpha = usesAlpha;
	r.pixels = pixels+area.y*width+area.x;
	return r;
}
//###################################################
};
//###################################################

static int numEweImages = 0;

/*
This is used for reading pixels only. The bytes pointed to
by pixels is either R,G,B or A,R,G,B depending on if hasAlpha
is true.
*/
//###################################################
typedef class pixelReadBuffer {
//###################################################
public:
	int rowStride;
	bool hasAlpha;
	uchar *pixels;
	bool isBGR;

	pixelReadBuffer()
	{
		isBGR = false;
	}
virtual void release() {delete this;}

//###################################################
}* PixelReadBuffer;
//###################################################
/*
This is used for reading and writing pixels if possible. The bytes pointed to
by pixels is either R,G,B or A,R,G,B depending on if hasAlpha
is true.
*/
//###################################################
typedef class pixelReadWriteBuffer : public pixelReadBuffer{
//###################################################

//###################################################
}* PixelReadWriteBuffer;
//###################################################


//###################################################
typedef class eweImage *EweImage;
class eweImage : public eweDrawable {
//###################################################
public:
	int createOptions;
	int width, height;
	EweRGBImage myImage;
	bool freed;

eweImage(int w,int h,int options)
{
	surfaceType = SURF_IMAGE;
	freed = false;
	numEweImages++;
	//if (numEweImages % 10 == 0) printf("EweImages: %d\n",numEweImages);
	myImage = NULL;
	createOptions = options;
	width = w;
	height = h;
}

virtual ~eweImage()
{
	free();
}

virtual void **getNativeStoragePointer(WObject obj)
{
	if (obj != 0) return &(WOBJ_ImageNative(obj));
	return NULL;
}

bool hasAlpha()
{
	if (eweObject != 0)
		return WOBJ_ImageHasAlpha(eweObject);
	return false;
}

unsigned char *getImageAlpha()
{
	if (eweObject == 0) return NULL;
	WObject alpha = WOBJ_ImageAlphaChannel(eweObject);
	return alpha == 0 ? NULL : (unsigned char *)WOBJ_arrayStart(alpha);
}

virtual void writeEweRGBToNative(eweRGBImage &data,int destX,int destY,class rect *destClip = NULL) = 0;

virtual PixelReadBuffer getPixelReadBuffer(class rect &area) = 0;

virtual PixelReadWriteBuffer getPixelReadWriteBuffer(class rect &area){return NULL;}

virtual rgbSection* toRGB(bool forWriting = false) {return NULL;}
/*
If the image can save internaly a transparency mask, then it should do so and return true.
If not it should return false.

It will be assumed that the same mask will always be used for the image so that once this
is done, it can internally save the mask.
*/
virtual bool setMask(eweImage *mask) {return false;}
virtual bool maskIsSet() {return false;}
virtual void drawDirectly(eweGraphics *g, int x, int y,bool withMask){}

virtual void writeEweRGB(eweRGBImage &data,int destX,int destY,class rect *destClip = NULL)
	{
		rect imageSize(0,0,width,height);
		if (destClip == NULL) destClip = &imageSize;
		else destClip->intersect(imageSize);
		if (myImage) myImage->fromSubArea(&data,destX,destY);
		writeEweRGBToNative(data,destX,destY,destClip);
	//
	// Update the image alpha bytes.
	//
		bool usesAlpha = data.usesAlpha;
		unsigned char *alpha = eweImage::getImageAlpha();
		if (alpha == NULL) return;
		rect use(destX,destY,data.width,data.height);
		if (destClip != NULL)
			use = use.intersect(*destClip);
		int xoff = use.x-destX, yoff = use.y-destY;
		int total = use.width*use.height;
		if (total <= 0) return;
		for (int y = 0; y<use.height; y++){
			unsigned char *ac = alpha+(use.y+y)*width+use.x;
			if (!usesAlpha) memset(ac,0xff,use.width);
			else{
				uint32 *pix = data.scanLine(y+yoff)+xoff;
				for (int x = 0; x<use.width; x++){
					if (ac) *ac++ = (*pix >> 24) & 0xff;
					pix++;
				}
			}
		}
	}

/*
Override this for better performance.
*/
virtual void getSetPixels(uint32 *pixel,rect area,int isGet)
{
		if (!isGet){
			eweRGBImage ei(area.width,area.height,true/*false*/,pixel);
			writeEweRGB(ei,area.x,area.y);
		}else{
			EweRGBImage ei = toEweRGB(area);
			memcpy(pixel,ei->pixels,4*area.width*area.height);
			ei->release();
		}
}
/*
Override this for better performance.
*/
virtual class eweRGBImage *toFullEweRGB()
{
		eweRGBImage *ri = new eweRGBImage(width,height);
		ri->valid.x = ri->valid.y = 0;
		ri->valid.width = width;
		ri->valid.height = height;
		class rect r(0,0,width,height);
		PixelReadBuffer pb = getPixelReadBuffer(r);
		unsigned char *alpha = eweImage::getImageAlpha();
		if (pb != NULL) {
			uchar *bits = pb->pixels;
			int rs = pb->rowStride;
			int bpp = pb->hasAlpha ? 4 : 3;
			bool reverse = pb->isBGR;
			for (int y = 0; y<height; y++){
				uchar *p = bits+(rs*y);
				uint32 *pix = ri->scanLine(y);
				int ax = y*width;
				for (int x = 0; x<width; x++, p += bpp, ax++){
					if (reverse) *pix++ = ((*p & 0xff) << 16) | ((*(p+1) & 0xff) << 8) | ((*(p+2) & 0xff) << 0);
					else *pix++ = ((*p & 0xff) << 0) | ((*(p+1) & 0xff) << 8) | ((*(p+2) & 0xff) << 16);
					*(pix-1) |= alpha == NULL ? 0xff000000 : (int)alpha[ax] << 24;
				}
			}
			pb->release();
		}
		return ri;
}
/*
Override this for better performance.
*/

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
		PixelReadBuffer pb = getPixelReadBuffer(fromImage);
		if (pb != NULL) {
			int xoff = fromImage.x-r.x, yoff = fromImage.y-r.y;
			uchar *bits = pb->pixels;
			int rs = pb->rowStride;
			int bpp = pb->hasAlpha ? 4 : 3;
			bool reverse = pb->isBGR;
			for (int y = 0; y<fromImage.height; y++){
				uchar *p = bits+(rs*y);
				uint32 *pix = ri->scanLine(y+yoff)+xoff;
				int ax = (fromImage.y+y)*width+fromImage.x;
				for (int x = 0; x<fromImage.width; x++, p += bpp, ax++){
					if (reverse) *pix++ = ((*p & 0xff) << 16) | ((*(p+1) & 0xff) << 8) | ((*(p+2) & 0xff) << 0);
					else *pix++ = ((*p & 0xff) << 0) | ((*(p+1) & 0xff) << 8) | ((*(p+2) & 0xff) << 16);
					*(pix-1) |= alpha == NULL ? 0xff000000 : (int)alpha[ax] << 24;
				}
			}
			pb->release();
		}
		return ri;
	}

virtual class eweRGBImage *toEweRGB(rect & area,class eweRGBImage *dest = NULL)
{
	eweRGBImage *ri = myImage;
	if (!ri){
		if (!eweObject || !WOBJ_ImageWasLoaded(eweObject)){
			return toSubEweRGB(area,dest);
		}
		ri = toFullEweRGB();
		if (!ri) return NULL;
		myImage = ri;
		myImage->persist = true;
	}
	if (area.x == 0 && area.y == 0 && area.width == width && area.height == height) return ri;
  	eweRGBImage *i = ri->getSubArea(area,dest);
	ri->release();
	return i;
}

virtual class eweRGBImage *toEweRGB() { rect r(0,0,width,height); return toEweRGB(r);}

class eweRGBImage *createMask(int transparentColor,bool reverse = false)
{
	eweRGBImage *rgb = toEweRGB(), *ret;
	if (rgb == NULL) return NULL;
	ret = rgb->createMask(transparentColor,reverse);
	rgb->release();
	return ret;
}
virtual void rotate(eweImage *dest,int rot)
{
	EweRGBImage source = toEweRGB();
	EweRGBImage d = new eweRGBImage(dest->width,dest->height,source->usesAlpha);
	int mll = width, dll = d->width;
	uint32 *ss = source->pixels;
	uint32 *ds = d->pixels;
	int dchange = dll;
	int dd = 1;
	int x,y;
	if (ss != NULL && ds != NULL){
		if (rot == 90) ds += height-1, dchange = -1, dd = dll;
		else if (rot == 180) ds += width-1+dll*(height-1), dchange = -dll, dd = -1;
		else if (rot == 270) ds += dll*(width-1), dchange = 1, dd = -dll;
		for (y = 0; y<height; y++, ss += mll, ds += dchange){
			uint32 *se = ss;
			uint32 *de = ds;
			for (x = 0; x<width; x++, se += 1, de += dd){
				*de = *se;
			}
		}
	}
	dest->writeEweRGB(*d,0,0);
	d->release();
	source->release();
}

virtual int
createCursor(EweImage mask, int hotX, int hotY)
{
	return (0);
}
virtual int
createIcon(EweImage mask, int options)
{
	return (0);
}
virtual void free()
{
	if (freed) return;
	freed = true;
	numEweImages--;
	if (myImage) {
		myImage->persist = false;
		delete myImage;
		myImage = NULL;
	}
 }

//###################################################
};
//###################################################

static COLORREF mapColor(COLORREF c)
{
#define MAX_MAPPED 256
	static EweImage buf = NULL;
	static EweRGBImage rgb = NULL;
	static int mapped[MAX_MAPPED*2];
	static int numMapped = 0;
	static int lastMappedIndex = -1;
	int value = c.toRGB();

	if (lastMappedIndex != -1) {
		if (mapped[lastMappedIndex*2] == value){
			COLORREF ret(mapped[lastMappedIndex*2+1]);
			return ret;
		}
	}

	for (int i = 0; i<numMapped; i++){
		if (mapped[i*2] == value) {
			COLORREF ret(mapped[i*2+1]);
			lastMappedIndex = i;
			return ret;
		}
	}

	rect area(0,0,1,1);
	if (buf == NULL) buf = ewe->getNewImage(1,1,0,true);
	if (buf == NULL) return c;
	if (rgb == NULL) rgb = new eweRGBImage(1,1);
	rgb->pixels[0] = value;
	buf->writeEweRGB(*rgb,0,0);
	rgb->pixels[0] = 0xaaaaaaaa;
	buf->toEweRGB(area,rgb);

	if (numMapped < MAX_MAPPED) {
		mapped[numMapped*2] = value;
		mapped[numMapped*2+1] = rgb->pixels[0];
		lastMappedIndex = numMapped;
		numMapped++;
	}

	COLORREF ret(rgb->pixels[0]);
	return ret;
}

static int screenIsRotated = 0;

static int checkRotated(int flags)
{
	if (flags & FLAG_MAIN_WINDOW_ROTATED) screenIsRotated = 1;
	if (flags & FLAG_MAIN_WINDOW_COUNTER_ROTATED) screenIsRotated = 2;
	return screenIsRotated;
}

#define CW_USEDEFAULT -1
WObject mainWinObj;

static rect getScreenRect();

//###################################################
typedef class eweWindow *EweWindow;
//###################################################
class eweWindow : public eweDrawable {
//###################################################

public:
	WClass *postClass;
	WClassMethod *postMethod;
	Var params[10];
	int myFlags;

	eweWindow()
	{
		surfaceType = SURF_WINDOW;
	}
virtual void **getNativeStoragePointer(WObject obj)
{
	if (obj != 0) return &(WOBJ_WindowNative(obj));
	return NULL;
}
virtual void setup(WObject obj)
{
	postClass = WOBJ_class(obj);
	postMethod = getMethod(postClass, createUtfString("_postEvent"),createUtfString("(IIIIII)V"), &postClass);
}
virtual int setCursor(int handle) {return 0;}
virtual bool getClientRect(rect &r) = 0;
virtual bool getWindowRect(rect &r) = 0;
virtual int getFlagsForSize(rect &requested,rect &screen,int flagsToSet,int flagsToClear) = 0;
virtual int adjustFlags(int flags,int turnOn,int turnOff,int *style = NULL,int *exstyle = NULL) = 0;
virtual void setLocation(int x,int y) = 0;
virtual void setSize(int x,int y) = 0;
virtual void setRect(rect &r,bool isClient) = 0;
virtual void setTitle(TCHAR *title) = 0;
virtual void create(int flags) = 0;
virtual void close() = 0;
virtual void shown(bool isVisible){}
virtual void showMaximized() = 0;
virtual void showMinimized() = 0;
virtual void show() = 0;
virtual void hide() = 0;
virtual void showNormal(){show();}
virtual void setIcon(int icon){};
virtual void toFront() {}
virtual bool isVisible() = 0;
virtual bool getSetInfo(int which, WObject src, WObject dest, int options, Var &ret,bool isGet){return false;}
virtual bool specialOperation(int operation, WObject data, bool& ret) {return false;}

virtual class eweRGBImage *toEweRGB(rect & area,class eweRGBImage *dest = NULL)
{
	return NULL;
}
class eweRGBImage *toEweRGB()
{
	rect r;
	getClientRect(r);
	r.x = 0; r.y = 0;
	return toEweRGB(r);
}
virtual void writeEweRGB(eweRGBImage &data,int destX,int destY,rect *destClip = NULL) = 0;

virtual void create(TCHAR *title,rect &r,int flags)
{
	create(flags);
	if (title != NULL) setTitle(title);
	setSize(r.width,r.height);
	setLocation(r.x,r.y);
}
void setEweWindowIcon()
{
	static bool triedIcon = false;
	static int eweIcon = 0;
	LOCKTHREAD
			if (!triedIcon){
				triedIcon = true;
				Var params[1];
				Var ret;
				ret.obj = 0;
				WClass *gClass = tryGetClass(createUtfString("ewe/fx/Graphics"));
				WClassMethod *gMethod = gClass == NULL ? NULL :
					getMethod(gClass, createUtfString("getWindowIconImages"),
						createUtfString("()[Ljava/lang/Object;"), NULL);
				if (gMethod != NULL)
					executeMethodRet(gClass, gMethod, params, 0,&ret);
				if (ret.obj != 0){
					WObject *images = (WObject *)WOBJ_arrayStart(ret.obj);
					EweImage ei = (EweImage)WOBJ_ImageNative(images[0]);
					EweImage em = (images[1] != 0) ? (EweImage)WOBJ_ImageNative(images[1]) : NULL;
					if (ei != NULL) eweIcon = ei->createIcon(em,0);
				}
			}
			if (eweIcon != 0) setIcon(eweIcon);
	UNLOCKTHREAD
}
//#endif

virtual int createAbsoluteNative(rect &location,rect &max,TCHAR *title,int flags,WObject creationData)
{
	bool maxIt = false, minIt = false;
	rect &r = location;
#ifdef PDA
	if (flags & FLAG_MAXIMIZE_ON_PDA){
		maxIt = true;
		r.width = r.height = r.x = r.y = CW_USEDEFAULT;
	}
#endif
	if (flags & FLAG_MAXIMIZE) maxIt = true;
	if (flags & FLAG_MINIMIZE) minIt = true;

	if (r.width  == CW_USEDEFAULT) maxIt = true, r.width = max.width;//400;
	if (r.height == CW_USEDEFAULT) maxIt = true, r.height = max.height;//300;

	rect bounds = max;
	if ((VmFlags & VM_FLAG_IS_MOBILE) == 0){
		bounds = ::getScreenRect();
		if (SimulateSip && max.width != 0) bounds = max;
	}
	if (r.width != CW_USEDEFAULT) r.x = bounds.x+(bounds.width-r.width)/2;
	if (r.height != CW_USEDEFAULT) r.y = bounds.y+(bounds.height-r.height)/2;

	if (r.x == CW_USEDEFAULT) r.x = 0;
	if (r.y == CW_USEDEFAULT) r.y = 0;

/*	if (((VmFlags & VM_FLAG_IS_MOBILE) == 0) && SimulateSip && max.width != 0) {
		if (r.width >= max.width || r.height >= max.height)
			r.x = r.y = 0;
	}*/
	create(title,r,flags);

	bool iconSet = false;
	if (creationData != 0){  //Register a new class.
		if (objectPtr(creationData)[1].intValue){
			if (objectPtr(creationData)[3].obj != 0){//Icon specified.
					setIcon((int)getLong(objectPtr(creationData)[3].obj));
					iconSet = true;
			}
		}
	}
	if (!iconSet) setEweWindowIcon();

	if (mainWinObj == 0) {
		mainWinObj = eweObject;
		ewe->setMainWindow(this,flags);
	}

	if (flags & FLAG_IS_VISIBLE){
		if (maxIt) showMaximized();
    else if (minIt) showMinimized();
		else show();
	}

}

#ifndef TASKBARHEIGHT
#ifdef PDA
#define TASKBARHEIGHT 40
#else
#define TASKBARHEIGHT 0
#endif
#endif

virtual bool createNative(rect &size,WObject title,int flagsToSet,int flagsToClear,WObject creationData)
{
		int MainWinFlags;
		int style, exstyle;
		EweWindow parent = NULL;
		rect rc;
		ewe->getParentRect(parent,rc,0);
		int maxWidth = rc.width;
		int maxHeight = rc.height;
		maxHeight -= TASKBARHEIGHT;
		int px = rc.x;
		int py = rc.y;
		int width = CW_USEDEFAULT;  //g_mainWinWidth;
		int height = CW_USEDEFAULT; //g_mainWinHeight;
		int x = CW_USEDEFAULT;
		int y = CW_USEDEFAULT;
		checkRotated(flagsToSet);
		if (true) {
			if (size.width >= 0 && size.width <= maxWidth)
				width = size.width;
			if (size.height >= 0 && size.height <= maxHeight)
				height = size.height;
			if (width == CW_USEDEFAULT || height == CW_USEDEFAULT)
				width = height = CW_USEDEFAULT;
			x = size.x;
			y = size.y;
			if (x == -1) x = CW_USEDEFAULT;
			if (y == -1) y = CW_USEDEFAULT;
		}
		//printf("Max: %d %d - %d %d %d %d\n",maxWidth,maxHeight,x,y,width,height);
		if (width == CW_USEDEFAULT) x = CW_USEDEFAULT;
		if (height == CW_USEDEFAULT) y = CW_USEDEFAULT;
	if (width <= 0 || height <= 0){
		if (width <= 0) width = CW_USEDEFAULT;
		if (height <= 0) height = CW_USEDEFAULT;
	}
#ifndef PDA
	if (maxWidth >= 640 && maxHeight >= 480 && ((g_mainWinWidth == 0 || g_mainWinHeight == 0) || ((VmFlags & VM_FLAG_NO_WINDOWS) == 0))) maxWidth = maxHeight = 0;
	if (width == CW_USEDEFAULT) width = (maxWidth == 0 ? 240 : maxWidth);
	if (height == CW_USEDEFAULT) height = (maxHeight == 0 ? 320 : maxHeight);
	rect sw;
	ewe->getParentRect(NULL,sw,0);
	maxWidth = sw.width;//GetSystemMetrics(SM_CXSCREEN);
  maxHeight = sw.height;//GetSystemMetrics(SM_CYSCREEN);
#endif
		//style = WS_VISIBLE; // no border when default size under CE

	MainWinFlags = FLAG_IS_VISIBLE|FLAG_HAS_CLOSE_BUTTON|FLAG_HAS_TITLE|FLAG_CAN_RESIZE|FLAG_CAN_MAXIMIZE|FLAG_CAN_MINIMIZE|FLAG_HAS_TASKBAR_ENTRY;
	if (width == CW_USEDEFAULT || height == CW_USEDEFAULT)
		MainWinFlags |= FLAG_IS_DEFAULT_SIZE;

	MainWinFlags = adjustFlags(MainWinFlags,flagsToSet,flagsToClear,&style,&exstyle);

	myFlags = MainWinFlags;

	if (screenIsRotated){
		int t = width; width = height; height = t;
	}

	TCHAR *wTitle = stringToNativeText(title);

	rect full; full.x = x; full.y = y; full.width = width; full.height = height;
	rect max; max.x = max.y = 0; max.width = maxWidth; max.height = maxHeight;

	createAbsoluteNative(full,max,wTitle,MainWinFlags,creationData);

	if (wTitle != NULL) {
		//printf("Created: %s\n",wTitle);
		free(wTitle);
	}
	return true;
}
//
// This method should only be called by the GUI thread.
virtual void tick()
{
	ewe->forceTimerEvent();
}

// -----------------------------------------------------------------------------------
bool grab() {return eweSystem::grab();}
void release() {return eweSystem::release();}
// -----------------------------------------------------------------------------------
struct post_data {
	int type;
	int x,y;
	int time,key;
	int modifiers;
	bool rotateXY;
} postData;

// -----------------------------------------
bool preparePost(int type = 0,bool doGrab = true)
// -----------------------------------------
{
	if (eweObject == 0) return false;
	if (doGrab){
		if (!grab()) return false;
	}
	memset(&postData,0,sizeof(postData));
	postData.type = type;
	return true;
}
// -----------------------------------------
void doPost(bool doRelease = true,bool isPenKey = false)
// -----------------------------------------
{
	LOCKTHREAD
	if ((screenIsRotated == 1) && isPenKey){
		int t = postData.x;
		rect r;
		getClientRect(r);
		postData.x = postData.y;
		postData.y = r.width-t;
		int key = postData.key;
		if (key != 0) switch(key){
			case 75007: key = 75004; break; //Right to up.
			case 75005: key = 75007; break; //Down to right.
			case 75006: key = 75005; break; //Left to down.
			case 75004: key = 75006; break; //Up to left.
		}
		postData.key = key;
	}
	if ((screenIsRotated == 2) && isPenKey){
		int t = postData.y;
		rect r;
		getClientRect(r);
		postData.y = postData.x;
		postData.x = r.height-t;
		int key = postData.key;
		if (key != 0) switch(key){
			case 75007: key = 75005; break; //Right to up.
			case 75005: key = 75006; break; //Down to right.
			case 75006: key = 75004; break; //Left to down.
			case 75004: key = 75007; break; //Up to left.
		}
		postData.key = key;
	}

	if (postMethod != NULL && eweObject != 0){
			params[0].obj = eweObject;
			params[1].intValue = postData.type;
			params[2].intValue = postData.key; // key
			params[3].intValue = postData.x;
			params[4].intValue = postData.y;
			params[5].intValue = postData.modifiers; // modifiers
			params[6].intValue = getTimeStamp(); // timeStamp
			vmInSystemQueue = true;
			executeTopMethod(postClass, postMethod, params, 7);
			vmInSystemQueue = false;
			//eweSystem::doTick();
	}
	if (doRelease) {
		release();
	}
	UNLOCKTHREAD
}

void doPaint(rect &r)
{
	LOCKTHREAD
		WClass *vclass = WOBJ_class(eweObject); // get runtime class
#ifdef QUICKBIND
		WClassMethod *method = getMethodByMapNum(vclass, &vclass, (uint16)postPaintMethodMapNum);
#else
		WClassMethod *method = getMethod(vclass, createUtfString("_doPaint"),
			createUtfString("(IIII)V"), &vclass);
#endif
			if (method != NULL){
				Var params[5];
				params[0].obj = eweObject;
				// x, y, width, height
				params[1].intValue = r.x;
				params[2].intValue = r.y;
				params[3].intValue = r.width;
				params[4].intValue = r.height;
				vmInSystemQueue = true;
				executeTopMethod(vclass, method, params, 5);
				vmInSystemQueue = false;
				//PulseEvent(waitEvent);
			}
			release();
	UNLOCKTHREAD
}
void postResize(int width,int height)
{
	LOCKTHREAD
			WClass *vclass = WOBJ_class(eweObject); // get runtime class
			WClassMethod *method = getMethod(vclass, createUtfString("appResized"),
				createUtfString("(III)V"), &vclass);
			params[0].obj = eweObject;
			params[1].intValue = screenIsRotated ? height : width;
			params[2].intValue = screenIsRotated ? width : height;
			params[3].intValue = 0;
			vmInSystemQueue = TRUE;
			if (method != NULL) executeTopMethod(vclass,method,params,4);
			vmInSystemQueue = FALSE;
			//tick();
			//PulseEvent(waitEvent);
			release();
	UNLOCKTHREAD
}
virtual void resizeEvent(int width, int height)
{
	if (!grab()){
		//printf("deferResize: %d %d\n",ev->width,ev->height);
		deferResize(width,height);
	}else{
		//printf("postResize: %d %d\n",ev->width,ev->height);
		postResize(width,height);
	}
}
virtual void paintEvent(class rect &r)
{
	if (!grab()) {
		//deferPost(new QPaintEvent(qp->region(),qp->erased()));
	}else{
		doPaint(r);
	}
}

virtual void grabMouse(void* grabData){}
virtual void ungrabMouse(void* ungrabData){}

virtual bool mousePressed(int x, int  y, bool isRight, int keyModifiers, void* grabData)
{
	if (!setupMouseEvent(x,y,200,isRight,keyModifiers)) return false;
	penState = 1;
	grabMouse(grabData);
	doPost(true,true);
	return true;
}
virtual bool mouseReleased(int x, int y, bool isRight, int keyModifiers, void* ungrabData)
{
	if (!setupMouseEvent(x,y,202,isRight,keyModifiers)) return false;
	penState = 0;
	ungrabMouse(ungrabData);
	doPost(true,true);
	return true;
}
virtual bool mouseMoved(int x, int y, int keyModifiers)
{
	if (!setupMouseEvent(x,y,201,false,keyModifiers)) return false;
	doPost(true,true);
	return true;
}
virtual bool mouseEntered()
{
	if (!setupMouseEvent(0,0,204,false,0)) return false;
	doPost(true,true);
}
virtual bool mouseExited()
{
	if (!setupMouseEvent(0,0,205,false,0)) return false;
	doPost(true,true);
}
virtual bool setupMouseEvent(int x,int y,int type,bool isRight,int keyModifiers)
{
	if (!preparePost()) {
		//printf("X");
		//fflush(stdout);
		return false;
	}
	//printf(".");fflush(stdout);
	postData.type = type;
	postData.modifiers = keyModifiers;
	if (isRight) postData.modifiers |= 0x8;
	postData.x = x;
	postData.y = y;
	postData.key = 0;
	return true;
}

virtual void deferResize(int width,int height)
{
	ewe->addWindowTask(new resizeTask(this,width,height));
}
virtual void deferActivate(bool isActive)
{
	ewe->addWindowTask(new eventTask(this,isActive ? 51 : 52));
}
bool activated(bool isActive)
{
	//printf("Activated: %d\n",isActive);
	if (!preparePost(isActive ? 51 : 52)) {
		deferActivate(isActive);
		return true;
	}
	doPost();
	return true;
}

virtual void closeEvent()
{
	if (!preparePost(50,false)) return;
	doPost(false);
}

#define SPECIALKEY 0x8

virtual void keyEvent(int key,int modifiers,bool isPress)
{
	if ((VmFlags & VM_FLAG_HAS_SOFT_KEYS) != 0){
		if ((key == 75192) && (modifiers & 8)) key = 75022;
		else if (key == 75193) key = 75023;
	}
	if (!preparePost(isPress ? 100 : 101)) return;
	postData.modifiers = modifiers;
	postData.key = key;
	if (postData.key == 0) {
		release();
		return;
	}
	if (isPress) pressedKey = postData.key;
	else pressedKey = 0;
	doPost(true,true);
}


static class eweWindow *mainWindow;
static int penState, pressedKey;

/** Left Mouse Button or Pen **/
#define KEY_PEN 76000

static int getKeyState(int key)
{
	if (key == KEY_PEN && penState == 1)
		return 0x8000;
	else if (key != 0 && pressedKey == key)
		return 0x8000;
	return 0;
}

//###################################################
};
//###################################################

class eweWindow *eweWindow::mainWindow = NULL;
int eweWindow::penState = 0;
int eweWindow::pressedKey = 0;

void resizeTask::run()
{
	if (window != NULL) {
		window->postResize(width,height);
	}
}
void eventTask::run()
{
	if (window != NULL) {
		window->preparePost(type);
		window->doPost();
	}
}


//###################################################
class graphics_pen {
//###################################################
public:
	int style;
/*
public static final int SOLID           = 0;
public static final int DASH            = 1;        -------
public static final int DOT             = 2;        .......
public static final int DASHDOT         = 3;        _._._._
public static final int DASHDOTDOT      = 4;        _.._.._
public static final int NULL 		= 5;
*/
	int thickness;
	COLORREF color;
	graphics_pen(){};
	graphics_pen(int s,int t,COLORREF c)
	{
		style = s;
		thickness = t;
		color = c;
	}
//###################################################
};
typedef class graphics_pen PEN;
//###################################################


//###################################################
struct graphics_brush {
//###################################################
public:
	int style;
	COLORREF color;
	graphics_brush(){};
	graphics_brush(int s,COLORREF c)
	{
		style = s;
		color = c;
	}
//###################################################
};
typedef class graphics_brush BRUSH;
//###################################################

static WClass *bufferClass = NULL;

//###################################################
typedef class eweGraphics *EweGraphics;
class eweGraphics : public eweNativeObject {
//###################################################

private:
rect myClip;

public:
PEN curPen;
BRUSH curBrush;
FONT curFont;

PEN *getCurPen()
{
	if (curPen.style == -1) return NULL;
	return &curPen;
}
BRUSH *getCurBrush()
{
	if (curBrush.style == -1) return NULL;
	return &curBrush;
}
FONT *getCurFont()
{
	if (curFont == NULL) return NULL;
	return &curFont;
}
virtual void setFont(FONT f)
{
	curFont = f;
	doSetFont(getCurFont());
}

int surfaceType;
int drawOp, oldDrawOp;
WObject surface;
COLORREF curForeground;
rect *clip;
int tx, ty;

#define DRAW_OVER 1
#define DRAW_AND 2
#define DRAW_OR 3
#define DRAW_XOR 4
#define DRAW_ALPHA 5

eweGraphics():curPen(-1,0,COLORREF(0,0,0)),curBrush(-1,COLORREF(0,0,0)),curForeground(0,0,0)
{
	tx = ty = 0;
	clip = NULL;
	oldDrawOp = drawOp = DRAW_OVER;
}
virtual void setupDrawable(void* nativeDrawable) = 0;

virtual void setup(WObject obj)
{
	tx = ty = 0;
	curFont = NULL;
	surfaceType = SurfaceGetType(surface = WOBJ_GraphicsSurface(obj));
	//if (surfaceType == SURF_WINDOW) printf("Setting up window graphics!\n");
	//printf("Setup: %x %x %i\n",obj,surface,surfaceType);
	EweDrawable ed = NULL;
	if (surfaceType == SURF_WINDOW)
		ed = (EweDrawable)WOBJ_WindowNative(surface);
	else if (surfaceType == SURF_IMAGE)
		ed = (EweDrawable)WOBJ_ImageNative(surface);
	if (ed != NULL){
		void *drawable = ed->getDrawable();
		setupDrawable(drawable);
		setForeground(COLORREF(0,0,0));
	}else
		setupDrawable(NULL);
}

void *getNativeSurface()
{
	if (surfaceType == SURF_IMAGE) return WOBJ_ImageNative(surface);
	else if (surfaceType == SURF_WINDOW) return WOBJ_WindowNative(surface);
	else return NULL;
}
rect getClip()
{
	if (clip != NULL) return *clip;
	rect ret(0,0,0,0);
	if (surfaceType == SURF_IMAGE){
		EweImage ei = (EweImage)getNativeSurface();
		ret.width = ei->width; ret.height = ei->height;
	}else if (surfaceType == SURF_WINDOW){
		EweWindow ew = (EweWindow)getNativeSurface();
		ew->getClientRect(ret);
		ret.x = ret.y = 0;
	}
	return ret;
}

virtual bool canCopyFrom()
{
	return surfaceType == SURF_IMAGE || surfaceType == SURF_WINDOW;
}

virtual void translate(int x,int y)
{
	tx += x;
	ty += y;
	doTranslate(x,y);
}

COLORREF curColor;

virtual void setForeground(COLORREF c)
{
	curColor = c;
	doSetForeground(c);
}
//
// If style is -1 for setPen() and setBrush(), this indicates that
// a null pen and null brush is to be selected.
//
void setPen(PEN p)
{
	curPen = p;
	setForeground(p.color);
	doSetPen(getCurPen());
}
void setBrush(BRUSH b)
{
	curBrush = b;
	doSetBrush(getCurBrush());
}
COLORREF *setupBrush()
{
	BRUSH *b = getCurBrush();
	if (b == NULL) return NULL;
	doSetForeground(b->color);
	return &curColor;
}
void resetBrush(COLORREF *c)
{
	if (c == NULL) return;
	doSetForeground(*c);
}
void setDrawOp(int op)
{
	oldDrawOp = drawOp;
	drawOp = op;
	doSetDrawOp(op);
}
virtual void doTranslate(int x,int y){}
virtual void doSetDrawOp(int op){}
virtual void doSetPen(PEN *p) {}
virtual void doSetBrush(BRUSH *b) {}
virtual void doSetFont(FONT *f) {}
virtual void drawLine(int x1,int y1,int x2,int y2) = 0;
virtual void doSetForeground(COLORREF c) = 0;
virtual int getTextWidth(WCHAR *ch,int num) = 0;
virtual void drawText(int x,int y,WCHAR *text,int count) = 0;
virtual void drawRect(int x,int y,int width,int height,int fill = 0) = 0;
virtual void drawEllipse(int x,int y,int width,int height,int fill = 0) = 0;
virtual void fillPolygon(int *x,int *y,int length) = 0;
virtual void doSetClip(rect & r){}
virtual void doClearClip(){}

/*
This is used for scaling.
*/
virtual void scaleImage(EweImage image,EweImage mask,colorref *transparent,rect source,rect location)
{
	rect cl = getClip();
	rect lt(location.x+tx,location.y+ty,location.width,location.height);
	rect oi = lt.intersect(cl);
	rect ir(oi.x-lt.x,oi.y-lt.y,oi.width,oi.height);
	rect ca(oi.x-tx,oi.y-ty,oi.width,oi.height);
	if (ir.width <= 0 || ir.height <= 0) return;

	EweRGBImage t = image->toEweRGB();
	EweRGBImage ei = t->scale(source,location.width,location.height), em = NULL;
	t->release();
	if (mask != NULL){
		t = mask->toEweRGB();
		em = t->scale(source,location.width,location.height);
		t->release();
		//delete mask;
	}
	location = ca;
    	EweRGBImage cei = ei->getSubArea(ir);
	if (cei != ei) ei->release();
	EweRGBImage cem = (em == NULL) ? NULL : em->getSubArea(ir);
	if (cem != em) em->release();
	drawImage(cei,cem,transparent,location);
	cei->release();
	if (cem != NULL) cem->release();
}


//
virtual rgbSection* capture(class rect location);
virtual void drawEweRGBImage(EweRGBImage image,int x,int y) = 0;
virtual void drawEweImage(EweImage image,int x,int y)
{
	printf("Override drawEweImage() or drawImage(EweImage image...)\n");
}
//
virtual void drawImage(EweImage image,EweImage mask,colorref *transparent,rect location)
{
			if ((mask == 0 && transparent == 0 && drawOp != DRAW_ALPHA) || !canCopyFrom()){
				if (!isBufferSurface() && (drawOp == DRAW_OVER  || !canCopyFrom())){ //Copy operation
					drawEweImage(image, location.x, location.y);
					return;
				}
			}
			rect cl = getClip();
			rect lt(location.x+tx,location.y+ty,location.width,location.height);
			rect oi = lt.intersect(cl);
			rect ir(oi.x-lt.x,oi.y-lt.y,oi.width,oi.height);
			rect ca(oi.x-tx,oi.y-ty,oi.width,oi.height);
			if (ir.width <= 0 || ir.height <= 0) return;
      			location = ca;
			EweRGBImage ei = image->toEweRGB(ir);
			EweRGBImage em = (mask == NULL) ? NULL : mask->toEweRGB(ir);
			eweGraphics::drawImage(ei,em,transparent,location);
			ei->release();
			if (em != NULL) em->release();
}
//
virtual void drawImage(EweRGBImage ei, EweRGBImage em,colorref *transparent,rect location)
{
			if ((em == 0 && transparent == 0) || !canCopyFrom()){
				if ((!isBufferSurface() && (drawOp == DRAW_OVER || !canCopyFrom()))){ //Copy operation
					drawEweRGBImage(ei,location.x,location.y);
				}else{
					rgbSection* dest = capture(location);
					if (dest->isValid){
					rect valid(0,0,location.width,location.height);
					for (int r = valid.y, maxR = valid.height+valid.y; r<maxR; r++){
						uint32
							*lTemp = dest->getScanLine(r)+valid.x,
							*lImage = ei->scanLine(r)+valid.x;
					  switch(drawOp){
							case DRAW_OVER:
								for (int c = 0; c<valid.width; c++, lTemp++)
									*lTemp = *lImage++ & 0xffffff;
                break;
							case DRAW_XOR:
								for (int c = 0; c<valid.width; c++, lTemp++)
									*lTemp = (*lTemp) ^ (~(*lImage++) & 0xffffff);
                break;
							case DRAW_OR:
								for (int c = 0; c<valid.width; c++, lTemp++)
									*lTemp = (*lTemp)  & (*lImage++ & 0xffffff);
                break;
							case DRAW_AND:
								for (int c = 0; c<valid.width; c++, lTemp++)
									*lTemp = (*lTemp) | (*lImage++ & 0xffffff);
                break;
							case DRAW_ALPHA:
								for (int c = 0; c<valid.width; c++, lTemp++){
									int d = *lTemp & 0xffffff, s = *lImage++;
									int as = (s >> 24) & 0xff,
											ad = 0xff; //FIXME get the actual value.
									if (as == 0xff) *lTemp = s;
									else if (as == 0) continue;
									else{
										int save = 0;
										save |= blend(as,ad,s,d,16);
										save |= blend(as,ad,s,d,8);
										save |= blend(as,ad,s,d,0);
										save |= blend(as,ad,0xff000000,0xff000000,24);
										*lTemp = save;
									}
								}
								break;
							default: break;
            }
					}
					dest->restore();
					}
					dest->release();
				}
			}else if (transparent != 0){
				int col = mapColor(*transparent).toRGB();
				rgbSection* dest  = capture(location);
				if (dest->isValid){
					rect valid(0,0,location.width,location.height);
					for (int r = valid.y, maxR = valid.height+valid.y; r<maxR; r++){
						uint32
							*lTemp = dest->getScanLine(r)+valid.x,
							*lImage = ei->scanLine(r)+valid.x;
						for (int c = 0; c<valid.width; c++){
							if ((*lImage & 0xffffff) != col) *lTemp++ = *lImage++;
							else lTemp++, lImage++;
						}
					}
					dest->restore();
				}
				dest->release();
			}else{ //mask != 0
				rgbSection* dest  = capture(location);
				if (dest->isValid){
					uint32 col = mapColor(COLORREF(0xff,0xff,0xff)).toRGB();
					rect valid(0,0,location.width,location.height);
					for (int r = valid.y, maxR = valid.height+valid.y; r<maxR; r++){
						uint32
							*lTemp = dest->getScanLine(r)+valid.x,
							*lMask = em->scanLine(r)+valid.x,
							*lImage = ei->scanLine(r)+valid.x;
						for (int c = 0; c<valid.width; c++)
							if ((*lMask++ & 0xffffff) == col) *lTemp++ = *lImage++;
							else lTemp++, lImage++;
					}
					dest->restore();
				}
				dest->release();
			}
	if (drawOp == DRAW_ALPHA) setDrawOp(oldDrawOp);
}
/*
Implementing copy.
*/
virtual bool copyFrom(eweGraphics *from,rect source,int destX,int destY)
{
	EweRGBImage ei = NULL;
	source.x += from->tx;
	source.y += from->ty;

	if (from->surfaceType == SURF_IMAGE)
		ei = ((EweImage)WOBJ_ImageNative(from->surface))->toEweRGB(source);
	else if (from->surfaceType == SURF_WINDOW){
		ei = ((EweWindow)WOBJ_WindowNative(from->surface))->toEweRGB(source);
	}
	if (ei == NULL) return FALSE;
	drawEweRGBImage(ei,destX,destY);
	ei->release();
	return true;
}

virtual void setClip(rect &r)
{
	if (clip == NULL) clip = &myClip;
	clip->x = r.x+tx; clip->y = r.y+ty;
	clip->width = r.width; clip->height = r.height;
	doSetClip(r);
}
virtual void clearClip()
{
	doClearClip();
	//if (clip != NULL) delete clip;
	clip = NULL;
}
virtual bool getClip(rect &r)
{
	if (clip == NULL) return false;
	r.x = clip->x-tx;
	r.y = clip->y-ty;
	r.width = clip->width;
	r.height = clip->height;
	return true;
}

virtual void **getNativeStoragePointer(WObject obj)
{
	if (obj != 0) return &(WOBJ_GraphicsNative(obj));
	return NULL;
}
bool isBufferSurface()
{
	if (bufferClass == NULL) bufferClass = tryGetClass(createUtfString("ewe/fx/Buffer"));
	return (bufferClass != NULL && (WOBJ_class(surface) == bufferClass));
}
virtual void free() = 0;

//###################################################
};
//###################################################

//###################################################
class defaultCapture : public rgbSection{
//###################################################
	public:
	EweImage sourceImage;
	EweWindow sourceWindow;
	EweRGBImage image;
	EweGraphics from;
	rect r;
	point saved;

	defaultCapture(EweGraphics g,int x,int y,int w,int h)
	{
		isValid = true;
		from = g;
		saved.x = saved.y = 0;
		rect toGet(x+g->tx,y+g->ty,w,h);
		image = NULL;
    		sourceImage = NULL;
		sourceWindow = NULL;

		if (g->surfaceType == SURF_WINDOW){
			//printf("From window!\n");
			sourceWindow = (EweWindow)WOBJ_WindowNative(g->surface);
			if (sourceWindow != NULL)
				image = sourceWindow->toEweRGB(toGet);
		}else if (g->surfaceType == SURF_IMAGE){
			sourceImage = (EweImage)WOBJ_ImageNative(g->surface);
			if (sourceImage != NULL)
				image = sourceImage->toEweRGB(toGet);
		}
		if (image == NULL){
			printf("Cannot capture!\n");
			isValid = false;
			return;
		}//else printf("Captured: %x\n",image);
		r.set(x,y,w,h);
		r.moveBy(from->tx,from->ty);
	}

	uint32 *getScanLine(int row)
	{
		if (image == NULL) return NULL;
		uint32 *ret = image->scanLine(row);
		return ret;
	}

	rect validArea()
	{
		if (image == NULL) return rect(0,0,0,0);
		else return image->valid;
	}
	bool restore()
	{
		//if (from->clip != NULL)
			//printf("Clip: %d %d %d %d\n",from->clip->x, from->clip->y, from->clip->width, from->clip->height);
		if (image == NULL) return false;
		if (sourceImage != NULL)
			sourceImage->writeEweRGB(*image,r.x,r.y,from->clip);
		else if (sourceWindow != NULL)
			sourceWindow->writeEweRGB(*image,r.x,r.y,from->clip);
		image->release();
		return true;
	}

//###################################################
};
//###################################################
rgbSection* eweGraphics::capture(class rect location)
{
	return new defaultCapture(this,location.x,location.y,location.width,location.height);
}


//#include "nmunix_gui_impl.cpp"
#ifdef NOGUI
#include "nmunix_gui_none.cpp"
#elif defined(GTK_VERSION_1_2) || defined(GTK_VERSION_2_0)
#include "nmunix_gui_gtk.cpp"
#elif defined(QTOPIA) || defined(QT2)
#include "nmunix_gui_qt2.cpp"
#else
#error No GUI specified!
#endif

void destroyAndClear(void *eno)
{
	EweNativeObject e = (EweNativeObject)eno;
	if (e == NULL) return;
	e->destroyAndClear();
	e->requestDelete();
}

Var setupNew(EweNativeObject e,WObject obj)
{
		if (e == NULL) return returnVar(0);
		e->setupNativeObject(obj);
		return returnVar(1);
}

static Var WindowGetGuiInfo(Var stack[])
{
	Var v;
	WObject dest = stack[2].obj;
	int options = stack[3].intValue;
	v.obj = 0;
	if (ewe->getGuiInfo(stack[0].intValue,stack[1].obj,dest,options,v))
		return v;
	switch(stack[0].intValue){
	case INFO_SCREEN_RECT:
		{
		rect rc;
		ewe->getScreenRect(rc);
		rect2ewe(rc,v.obj = stack[2].obj);
		break;
		}
	case INFO_GUI_FLAGS:
		if (dest != 0) setLong(dest,0);
		v.obj = dest;
		break;
	}
	return v;
}

#define WOBJ_WindowHookVars 1

static Var WindowCreate(Var stack[])
{
		if (_winHookOffset == -1){
			WClass *wc;
			wc = getClass(createUtfString("ewe/ui/Window"));
			_winHookOffset = 1 + wc->numVars - WOBJ_WindowHookVars;
		}
		return setupNew(ewe->getNewWindow(),stack[0].obj);
}
static Var WindowClose(Var stack[])
{
	EweWindow ew = (EweWindow)WOBJ_WindowNative(stack[0].obj);
	if (ew != NULL) {
		ewe->closingWindow(ew);
		ew->close();
		ewe->closedWindow(ew);
	}
	//WindowDestroy(stack[0].obj);
	return returnVar(0);
}
static Var FontMetricsCreate(Var stack[])
{
	return setupNew(ewe->getNewFontMetrics(),stack[0].obj);
}
static Var WindowSetInfo(Var stack[])
{
	Var v;
	int options = stack[4].intValue;
	int style,exstyle;
	WObject dest = stack[3].obj;
	EweWindow ew = (EweWindow)WOBJ_WindowNative(stack[0].obj);
	v.obj = 0;
	if (ew == NULL) {
		v.obj = dest;
		return v;
	}
	if (ew->getSetInfo(stack[1].intValue,stack[2].obj,stack[3].obj,stack[4].intValue,v,false))
		return v;

	switch(stack[1].intValue){
	case INFO_WINDOW_TITLE:
		ew->setTitle(stringToTempUtf8(stack[2].obj).str);
		return returnVar(1);

	case INFO_WINDOW_FLAGS:
								{
		int flags = (int)getLong(stack[2].obj) & (FLAG_IS_VISIBLE|WINDOW_STATE);
		if (options & OPTION_FLAG_SET){
			ew->myFlags  = ew->adjustFlags(ew->myFlags,flags,0);
			if ((flags & FLAG_IS_VISIBLE) && !ew->isVisible()) {
				//if (ew == eweApplication::mainWindow) eweApplication::mainWindowIsVisible = true;
				ew->show();
				ew->shown(true);
			}
			if (flags & FLAG_MAXIMIZE) {
				ew->showMaximized();
				ew->shown(true);
      			}else if (flags & FLAG_MINIMIZE){
				ew->showMinimized();
				ew->shown(true);
      			}else if (flags & FLAG_RESTORE){
				ew->showNormal();
				ew->shown(true);
			}
		}else{
			ew->myFlags  = ew->adjustFlags(ew->myFlags,0,flags);
			if (flags & FLAG_IS_VISIBLE && ew->isVisible()) {
				ew->hide();
				ew->shown(false);
				//if (ew == eweApplication::mainWindow) eweApplication::mainWindowIsVisible = false;
			}
		}
		v.intValue = 1;
		return v;
								 }
	case INFO_SCREEN_RECT: return v;
	case INFO_CLIENT_RECT:
	case INFO_WINDOW_RECT: {
		WObject r = stack[2].obj;
		rect rc;
		if (r == 0) return v;
		ew->setRect(ewe2rect(r,rc),stack[1].intValue == INFO_CLIENT_RECT);
		v.intValue = 1;
		return v;
				}

	case INFO_WINDOW_ICON:{
		ew->setIcon((int)getLong(stack[2].obj));
		v.intValue = 1;
		return v;
						  }
	}

	return v;
}


static Var WindowGetInfo(Var stack[])
{
	Var v;
	WObject dest = stack[3].obj;
	int options = stack[4].intValue;
	EweWindow ew = (EweWindow)WOBJ_WindowNative(stack[0].obj);
	v.obj = 0;
	if (ew == NULL) {
		v.obj = dest;
		return v;
	}
	if (ew->getSetInfo(stack[1].intValue,stack[2].obj,stack[3].obj,stack[4].intValue,v,true))
		return v;
	switch(stack[1].intValue){
	case INFO_NATIVE_WINDOW:
		{
			if (options == 0){//Containing window.
				setLong(dest,(int64)(uintptr_t)ew);
			}else if (options == 1){//Drawing Surface.
				setLong(dest,(int64)(uintptr_t)ew);
			}
			v.obj = dest;
			break;
		}
	case INFO_WINDOW_FLAGS:
		{
		int flags = FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON & ~(WINDOW_STATE|FLAG_IS_VISIBLE);
		if (ew->isVisible()) flags |= FLAG_IS_VISIBLE;
/*
		flags |= FLAG_STATE_KNOWN;
		if (ew->isMaximized()) flags |= FLAG_MAXIMIZE;
		else if (ew->isMinimized()) flags |= FLAG_MINIMIZE;
*/
		flags |=  FLAG_STATE_KNOWN|ew->myFlags;

		if (dest != 0) setLong(dest,flags);
		v.obj = dest;
		return v;
		}
/*
	case INFO_PARENT_RECT:
		{
		RECT rc;
		WObject r = stack[3].obj;
		if (stack[1].intValue == INFO_SCREEN_RECT) ew = NULL;
		getParentRect(ew == NULL ? NULL : getParent(ew),&rc,screenIsRotated);
		WOBJ_RectWidth(r) = rc.right;
		WOBJ_RectHeight(r) = rc.bottom;
		WOBJ_RectX(r) = WOBJ_RectY(r) = 0;
		v.obj = r;
		return v;
				}
*/
	case INFO_SCREEN_RECT:
	{
		rect rc;
		ewe->getScreenRect(rc);
		rect2ewe(rc,v.obj = stack[3].obj);
		return v;
	}
	case INFO_FLAGS_FOR_SIZE: {
		int flags = 0;
		rect rc,rq;
		WObject r = stack[2].obj;
		ewe2rect(r,rq);
		rq.x = rq.y = 0;
		int setFlags = WOBJ_RectX(r);
		int clearFlags = WOBJ_RectY(r);
		checkRotated(setFlags);
		ewe->getParentRect(NULL,rc,screenIsRotated);
		if (r == 0) return v;
		flags = ew == NULL ? 0 : ew->getFlagsForSize(rq,rc,setFlags,clearFlags);
		if (dest != 0) setLong(dest,flags);
		v.obj = dest;
		return v;
		}
	case INFO_CLIENT_RECT:
	case INFO_WINDOW_RECT: {
		WObject r = stack[3].obj;
		if (r == 0) return v;
		rect rc;
		if (ew != NULL)
			if (stack[1].intValue == INFO_CLIENT_RECT) ew->getClientRect(rc);
			else ew->getWindowRect(rc);
		rect2ewe(rc,r);
		if (screenIsRotated) {
			int t = WOBJ_RectWidth(r);
			WOBJ_RectWidth(r) = WOBJ_RectHeight(r);
			WOBJ_RectHeight(r) = t;
		}
		v.obj = r;
		return v;
		}
	}//End switch
	return v;
}
static Var WindowToFront(Var stack[])
{
	EweWindow ew = (EweWindow)WOBJ_WindowNative(stack[0].obj);
	if (ew == NULL) return returnVar(0);
	ew->toFront();
	return returnVar(1);
}

static Var WindowCreateNative(Var stack[])
{
//Setup the window location.
		EweWindow ew = (EweWindow)WOBJ_WindowNative(stack[0].obj);
		rect r;
		WObject title = stack[2].obj;
		int flagsToSet = stack[3].intValue;
		int flagsToClear = stack[4].intValue;
		WObject creationData = stack[5].obj;
		if (ew != 0)
			return returnVar(ew->createNative(ewe2rect(stack[1].obj,r),title,flagsToSet,flagsToClear,creationData));
		return returnVar(0);
}

static Var WindowDoSpecial(Var stack[])
{
	int op = stack[1].intValue & 0xff;
	WObject data = stack[2].obj;
	bool ret = false;
	Var v;
	EweWindow ew = (EweWindow)WOBJ_WindowNative(stack[0].obj);
	if (ew != NULL && ew->specialOperation(op,data,ret))
		return returnVar(ret ? 1 : 0);
	if (op == 6){ //Change rotation
		screenIsRotated = (stack[1].intValue >> 8) & 0x3;
		return returnVar(1);
	}
	return returnVar(0);
}

void WindowDestroy(WObject window) {/*printf("Window destroy: %d\n",window);illegal = window;*/destroyAndClear(WOBJ_WindowNative(window));}
void FontMetricsDestroy(WObject fontMetrics) {destroyAndClear(WOBJ_FontMetricsNative(fontMetrics));}
void GraphicsDestroy(WObject graphics) {destroyAndClear(WOBJ_GraphicsNative(graphics));}
void FontDestroy(WObject font) {}
void ImageDestroy(WObject image)
{
	destroyAndClear(WOBJ_ImageNative(image));
}


static Var GraphicsFree(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	eg->free();
	return returnVar(1);
}
static Var GraphicsCreate(Var stack[])
{
	return setupNew(ewe->getNewGraphics(),stack[0].obj);
}
static Var GraphicsSetClip(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	rect r(stack[1].intValue,stack[2].intValue,stack[3].intValue,stack[4].intValue);
	eg->setClip(r);
	return returnVar(1);
}
static Var GraphicsGetClip(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL || stack[1].obj == 0) return returnVar(0);
	rect r;
	if (!eg->getClip(r)) return returnVar(0);
	rect2ewe(r,stack[1].obj);
	return returnVar(stack[1].obj);
}
static Var GraphicsClearClip(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg != NULL) eg->clearClip();
	return returnVar(1);
}
static Var GraphicsDrawLine(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	eg->drawLine(stack[1].intValue,stack[2].intValue,stack[3].intValue,stack[4].intValue);
	return returnVar(1);
}
static Var GraphicsDrawFillRect(Var stack[],bool fillIt)
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	eg->drawRect(stack[1].intValue,stack[2].intValue,stack[3].intValue,stack[4].intValue,fillIt);
	return returnVar(1);
}
static Var GraphicsDrawRect(Var stack[]) {return GraphicsDrawFillRect(stack,FALSE);}
static Var GraphicsFillRect(Var stack[]) {return GraphicsDrawFillRect(stack,TRUE);}

static Var GraphicsDrawFillEllipse(Var stack[],bool fillIt)
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	eg->drawEllipse(stack[1].intValue,stack[2].intValue,stack[3].intValue,stack[4].intValue,fillIt);
	return returnVar(1);
}
static Var GraphicsDrawEllipse(Var stack[]) {return GraphicsDrawFillEllipse(stack,FALSE);}
static Var GraphicsFillEllipse(Var stack[]) {return GraphicsDrawFillEllipse(stack,TRUE);}


static Var GraphicsTranslate(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	eg->translate(stack[1].intValue,stack[2].intValue);
	return returnVar(1);
}
static Var GraphicsSetColor(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	COLORREF c(stack[1].intValue,stack[2].intValue,stack[3].intValue);
	eg->curPen.color = c;
	eg->curPen.thickness = 1;
	eg->curPen.style = 0;
	eg->curBrush.color = c;
	eg->curBrush.style = 0;
	eg->setForeground(c);
	return returnVar(1);
}
static Var GraphicsSetDrawOp(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	eg->setDrawOp(stack[1].intValue);
	return returnVar(1);
}
static Var GraphicsSetPen(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	if (stack[1].obj == 0) eg->setPen(PEN(-1,0,COLORREF(0,0,0)));
	else{
		Var *v = objectPtr(stack[1].obj);
		WObject c = v[1].obj;
		eg->setPen(PEN(v[2].intValue,v[3].intValue,COLORREF(WOBJ_ColorRed(c),WOBJ_ColorGreen(c),WOBJ_ColorBlue(c))));
	}
	return returnVar(1);
}
static Var GraphicsSetBrush(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	if (stack[1].obj == 0) eg->setBrush(BRUSH(-1,COLORREF(0,0,0)));
	else{
		Var *v = objectPtr(stack[1].obj);
		WObject c = v[1].obj;
		eg->setBrush(BRUSH(v[2].intValue,COLORREF(WOBJ_ColorRed(c),WOBJ_ColorGreen(c),WOBJ_ColorBlue(c))));
	}
	return returnVar(1);
}
static Var GraphicsSetFont(Var stack[])
{
	EweGraphics eg = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (eg == NULL) return returnVar(0);
	eg->setFont(eweFont::getNativeFont(stack[1].obj));
	return returnVar(1);
}
ImageMaker getImageMaker(WObject image)
{
	EweImage i = (EweImage)WOBJ_ImageNative(image);
	if (i == NULL) return NULL;
	return ewe->getNewImageMaker(i);
}

static Var FontListFonts(Var stack[])
{
	int len = 0;
	Var v;
	TCHAR **all = ewe->listFonts(stack[0].obj,len);
	v.obj = createArray("Ljava/lang/String;",len);
	pushObject(v.obj);
	for (int i = 0; i<len; i++){
		WObject ns = createStringFromNativeText(all[i],-1);
		WObject *ptr = (WObject *)WOBJ_arrayStart(v.obj);
		ptr[i] = ns;
		delete all[i];
	}
	popObject();
	delete all;
	return v;
}

#define FM_STRINGWIDTH 1
#define FM_CHARARRAYWIDTH 2
#define FM_CHARWIDTH 3
#define FM_FORMATTED_POSITIONS 4
//MLB ==================================
#define STRBUFLEN 1024
static char strbuf[STRBUFLEN];
static char *strbufptr = strbuf;
//======================================

typedef int (* getCharWidthFunc)(WObject fontMetrics,int character);
typedef int (* getTextWidthFunc)(WObject fontMetrics,uint16 *chars,int len);

static int getCharArrayWidth(WObject fontMetrics,WCHAR *chars,int count)
{
	EweFontMetrics efm = (EweFontMetrics)WOBJ_FontMetricsNative(fontMetrics);
	if (efm == NULL) return 0;
	return efm->width(chars,count);
}
int getTextWidth(WObject fontMetrics,WCHAR *chars,int count)
{
	return getCharArrayWidth(fontMetrics,chars,count);
}

static int getCharWidth(WObject fontMetrics,int ch)
{
	EweFontMetrics efm = (EweFontMetrics)WOBJ_FontMetricsNative(fontMetrics);
	if (efm == NULL) return 0;
	return efm->width((WCHAR)ch);
}
static getCharWidthFunc setupGetCharWidth(WObject fontMetrics)
{
	return getCharWidth;
}
static void closeGetCharWidth(WObject fontMetrics)
{
}

static Var FontMetricsGetWidth(int type, Var stack[])
	{
	int32 width;

	EweFontMetrics efm = (EweFontMetrics)WOBJ_FontMetricsNative(stack[0].obj);
	if (efm == NULL) return returnVar(0);

	switch (type)
		{
		case FM_FORMATTED_POSITIONS:
			{
				WObject string = stack[1].obj,charArray;
				WObject fts = stack[2].obj;
				int *pos = (int *)WOBJ_arrayStart(stack[3].obj);
				int tw = stack[4].intValue;
				WCHAR *chars;
				int len, i, next, lenPos;

				if (string == 0) break;
				charArray = WOBJ_StringCharArrayObj(string);
				if (charArray == 0) break;
				chars = (WCHAR *)WOBJ_arrayStart(charArray);
				len = WOBJ_arrayLen(charArray);
				lenPos = WOBJ_arrayLen(stack[3].obj);
				next = 0;
				for (i = 0; i<len; i++){
					WCHAR  c = *chars++;
					if (c != 9) {
						next += efm->width(c);
					}else{
						next = next+tw;
						next -= next%tw;
					}
					*pos++ = next;
				}
				break;

			}
		case FM_CHARWIDTH:
			{
			width = efm->width((WCHAR)stack[1].intValue);
			break;
			}
		case FM_STRINGWIDTH:
		case FM_CHARARRAYWIDTH:
			{
			WObject string, charArray;
			int32 start, count;
			WCHAR *chars;
			//SIZE size;

			width = 0;
			if (type == FM_STRINGWIDTH)
				{
				string = stack[1].obj;
				if (string == 0)
					break;
				charArray = WOBJ_StringCharArrayObj(string);
				if (charArray == 0)
					break;
				start = 0;
				count = WOBJ_arrayLen(charArray);
				}
			else // FM_CHARARRAYWIDTH
				{
				charArray = stack[1].obj;
				start = stack[2].intValue;
				count = stack[3].intValue;
				if (arrayRangeCheck(charArray, start, count) == 0)
					break; // array null or range invalid
				}
			chars = (WCHAR *)WOBJ_arrayStart(charArray);
			chars = &chars[start];
			width = efm->width(chars,count);
			/*
#ifdef UNICODE//WINCE
			GetTextExtentPoint32(hDC, (TCHAR *)chars, count, &size);
			width = (int)size.cx;
#else
			while (count > 0)
				{
				char *buf = strbufptr; //MLB char buf[40];
				int32 i, n;

				n = STRBUFLEN; //MLB n = sizeof(buf);
				if (n > count)
					n = count;
				for (i = 0; i < n; i++)
					buf[i] = (char)chars[i];
				GetTextExtentPoint32(hDC, buf, count, &size);
				width += (int32)size.cx;
				count -= n;
				chars += n;
				}
#endif
				*/
			break;
			}
		}
	return returnVar(width);
}

static void getSize(WObject fontMetrics,WObject lines,int start,int end,int xGap,int yGap,rect *r)
{
	int w = 0, h = 0;
	int fh,leading;
	int numLines = WOBJ_arrayLen(lines);
	Var *strings = WOBJ_arrayStart(lines);
	int i;
	r->x = r->y = r->width = r->height = 0;
	fh = WOBJ_FontMetricsAscent(fontMetrics)+WOBJ_FontMetricsDescent(fontMetrics);
	leading = WOBJ_FontMetricsLeading(fontMetrics);
	for (i = start; i<end; i++){
		WObject str = strings[i].obj;
		if (str != 0){
			WObject charArray = WOBJ_StringCharArrayObj(str);
			if (charArray != 0){
				int count = WOBJ_arrayLen(charArray);
				WCHAR *chars = (WCHAR *)WOBJ_arrayStart(charArray);
				int cw = 0;
				cw = getCharArrayWidth(fontMetrics,chars,count);
				if (cw > w) w = cw;
			}
		}
		if (i != start) h += leading;
		h += fh;
	}
	h += yGap*2;
	w += xGap*2;
	r->width = w;
	r->height = h;
}
	// ewe/fx/Graphics_getSize_(Lewe/fx/FontMetrics;[Ljava/lang/String;IILewe/fx/Dimension;)V


static Var FontMetricsGetStringWidth(Var stack[])
	{
	return FontMetricsGetWidth(FM_STRINGWIDTH, stack);
	}

static Var FontMetricsGetCharArrayWidth(Var stack[])
	{
	return FontMetricsGetWidth(FM_CHARARRAYWIDTH, stack);
	}

static Var FontMetricsGetCharWidth(Var stack[])
	{
	return FontMetricsGetWidth(FM_CHARWIDTH, stack);
	}
static Var FontMetricsGetFormattedTextPositions(Var stack[])
	{
	return FontMetricsGetWidth(FM_FORMATTED_POSITIONS, stack);
	}

static void getSize(WObject fontMetrics,WObject lines,int start,int end,int xGap,int yGap,RECT *rect)
{
	int w = 0, h = 0;
	int fh,leading;
	int numLines = WOBJ_arrayLen(lines);
	Var *strings = WOBJ_arrayStart(lines);
	int i;
	rect->top = rect->left = rect->right = rect->bottom = 0;
	fh = WOBJ_FontMetricsAscent(fontMetrics)+WOBJ_FontMetricsDescent(fontMetrics);
	leading = WOBJ_FontMetricsLeading(fontMetrics);
	for (i = start; i<end; i++){
		WObject str = strings[i].obj;
		if (str != 0){
			WObject charArray = WOBJ_StringCharArrayObj(str);
			if (charArray != 0){
				int count = WOBJ_arrayLen(charArray);
				WCHAR *chars = (WCHAR *)WOBJ_arrayStart(charArray);
				int cw = 0;
				cw = getCharArrayWidth(fontMetrics,chars,count);
				if (cw > w) w = cw;
			}
		}
		if (i != start) h += leading;
		h += fh;
	}
	h += yGap*2;
	w += xGap*2;
	rect->right = w;
	rect->bottom = h;
}

#define LEFT 0x1
#define RIGHT 0x2
#define TOP 0x4
#define BOTTOM 0x8

//===================================================================
static void doAnchor(RECT *subArea,RECT *largeArea,int anchor)
//===================================================================
{
	int lw = largeArea->right-largeArea->left;
	int lh = largeArea->bottom-largeArea->top;
	int sw = subArea->right-subArea->left;
	int sh = subArea->bottom-subArea->top;

	subArea->left = ((lw-sw)/2);
	subArea->top = ((lh-sh)/2);
	if ((anchor & LEFT) != 0) subArea->left = 0;
	else if ((anchor & RIGHT) != 0) subArea->left = lw-sw;
	if ((anchor & TOP) != 0) subArea->top = 0;
	else if ((anchor & BOTTOM) != 0) subArea->top = lh-sh;

	subArea->left += largeArea->left;
	subArea->top += largeArea->top;
	subArea->bottom += subArea->top;
	subArea->right += subArea->left;
}

static WCHAR changeCaseDefault(WCHAR ch,int toUpper);

//===================================================================
static int getUnderlined(WCHAR *s,int len)
//===================================================================
{
	WCHAR ch, lc;
	int i;
	if (s == NULL) return -1;
	if (len < 3) return -1;
	if (s[len-1] != 0) return -1;
	ch = s[len-2];
	lc = (WCHAR)changeCaseDefault(ch,1);
	if (lc == ch) lc = (WCHAR)changeCaseDefault(ch,0);
	for (i = 0; i<len-2; i++)
		if (s[i] == ch || s[i] == lc) return i;
	return -1;
}

static void drawSomeText(EweGraphics p,WCHAR *chars,int count,RECT *r,COLORREF color)
{
	if (p == NULL) return;
	RECT rect = *r;
	WCHAR *oc = chars;
	int ocount = count;
	int hasUnder = 0;
	if (p->curFont == NULL) return;
	if (count >= 2)
	if (chars[count-1] == 0){
			hasUnder = 1;
			count -= 2;
	}
	p->drawText(rect.left,rect.top,chars,count);
			chars = oc;
			count = ocount;
			if (hasUnder){
				int idx = getUnderlined(chars,count);
				if (idx != -1){
					int before = p->getTextWidth(chars,idx);
					int width = p->getTextWidth(chars+idx,1);
					PEN old = p->curPen;
					p->setPen(PEN(0,1,old.color));
					int y = rect.top+p->curFont.ascent+p->curFont.descent;
					p->drawLine(before+rect.left,y,before+width-1+rect.left,y);
					p->setPen(old);
				}
			}
}
static Var GraphicsDrawTextInArea(Var stack[])
{
	Var v;
	v.intValue = 0;
	EweGraphics g = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (g == NULL) return v;
	WObject gr = stack[0].obj;
	WObject fontMetrics = stack[1].obj;
	WObject lines = stack[2].obj;
	WObject where = stack[3].obj;
	int alignment = stack[4].obj;
	int anchor = stack[5].obj;
	int startLine = stack[6].obj;
	int endLine = stack[7].obj;
	int numLines = WOBJ_arrayLen(lines);
	Var *strings = WOBJ_arrayStart(lines);
	RECT r1,r2;


	int transX, transY;
	int fh;
	int leading;
	int i;

	v.intValue = 0;
/*
	hRgn = (HRGN)WOBJ_GraphicsHRgn(gr);
	SelectClipRgn(hDC, hRgn);
	SetBkMode(hDC, TRANSPARENT);
	SetTextColor(hDC, (COLORREF)WOBJ_GraphicsRGB(gr));
	transX = WOBJ_GraphicsTransX(gr);
	transY = WOBJ_GraphicsTransY(gr);
	SetViewportOrgEx(hDC, transX, transY, NULL);
*/

	if (fontMetrics == 0)
		return v;
	g->setFont(eweFont::getNativeFont(WOBJ_FontMetricsFont(fontMetrics)));

	if (startLine > numLines) return v;
	if (endLine > numLines) endLine = numLines;

	getSize(fontMetrics,lines,startLine,endLine,0,0,&r1);
	r2.left = WOBJ_RectX(where);
	r2.top = WOBJ_RectY(where);
	r2.bottom = r2.top+WOBJ_RectHeight(where);
	r2.right = r2.left+WOBJ_RectWidth(where);
	doAnchor(&r1,&r2,anchor);

	fh = WOBJ_FontMetricsAscent(fontMetrics)+WOBJ_FontMetricsDescent(fontMetrics);
	leading = WOBJ_FontMetricsLeading(fontMetrics);
	r2.top = r1.top;
	//if (g->begin()){
	//g->setTempFont(fontMetrics);
	for (i = startLine; i<endLine; i++){
		WObject str = strings[i].obj;
		if (i != startLine) r2.top+=leading;
		if (str != 0){
			WObject charArray = WOBJ_StringCharArrayObj(str);
			if (charArray != 0){
				int count = WOBJ_arrayLen(charArray);
				WCHAR *chars = (WCHAR *)WOBJ_arrayStart(charArray);
				int x = r1.left;
				int w = getCharArrayWidth(fontMetrics,chars,count);
				if (alignment == RIGHT)
					x = (r1.right-w);
				else if (alignment != LEFT)
					x = ((r1.right-r1.left-w)/2)+r1.left;
				r2.left = x;
				drawSomeText(g,chars,count,&r2,COLORREF(0,0,0));//(COLORREF)WOBJ_GraphicsRGB(gr));
			}
		}
		r2.top += fh;
	//}
	//g->end();
  }
	return v;
}

static Var GraphicsDrawStringChars(bool doChars,Var stack[])
{
	Var v;
	v.intValue = 0;
	EweGraphics g = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (g == NULL) return v;
			WObject string, charArray;
			int32 x, y, start, count;
			uint16 *chars;
			int hasUnder = 0;
			RECT rect;
			COLORREF rgb;

			if (!doChars)
				{
				string = stack[1].obj;
				if (string == 0) return v;
				x = stack[2].intValue;
				y = stack[3].intValue;
				charArray = WOBJ_StringCharArrayObj(string);
				if (charArray == 0) return v;
				start = 0;
				count = WOBJ_arrayLen(charArray);
				}
			else
				{
				charArray = stack[1].obj;
				start = stack[2].intValue;
				count = stack[3].intValue;
				x = stack[4].intValue;
				y = stack[5].intValue;
				if (arrayRangeCheck(charArray, start, count) == 0) return v;
				}
			chars = (WCHAR *)WOBJ_arrayStart(charArray);
			chars = &chars[start];
			rect.left = x;
			rect.top = y;
			rect.right = 32767;
			rect.bottom = 32767;
/*
			SetBkMode(hDC, TRANSPARENT);
			rgb = (COLORREF)WOBJ_GraphicsRGB(gr);
			SetTextColor(hDC, rgb);
			hFont = (HFONT)WOBJ_GraphicsHFont(gr);
			oldFont = (HFONT)SelectObject(hDC, hFont);
*/
			//if (g->begin()){
			 drawSomeText(g,chars,count,&rect,rgb);
			 //debugString((char *)(const char *)QString().setUnicodeCodes(chars,count));
			// g->end();
			//}
	v.intValue = 1;
	return v;
}
static Var GraphicsDrawString(Var stack[])
{
	return GraphicsDrawStringChars(false,stack);
}
static Var GraphicsDrawChars(Var stack[])
{
	return GraphicsDrawStringChars(true,stack);
}

#define Ewe_Image(OBJ) ((OBJ == 0) ? NULL : (EweImage)WOBJ_ImageNative(OBJ))
#define Ewe_Color(OBJ) ((OBJ == 0) ? NULL : new colorref(WOBJ_ColorAlpha(OBJ),WOBJ_ColorRed(OBJ),WOBJ_ColorGreen(OBJ),WOBJ_ColorBlue(OBJ)))

static Var GraphicsDrawImage2(Var stack[])
{
	EweGraphics g = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (g != NULL && (stack[1].obj != 0)){
		colorref *c = NULL;
		g->drawImage(Ewe_Image(stack[1].obj),Ewe_Image(stack[2].obj),c = Ewe_Color(stack[3].obj),
									rect(stack[4].intValue,stack[5].intValue,stack[6].intValue,stack[7].intValue));
		if (c != NULL) delete c;
	}
	return returnVar(1);
}
static Var GraphicsScaleImage(Var stack[])
{
	EweGraphics g = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (g != NULL && (stack[1].obj != 0) && (stack[4].obj != 0) && (stack[5].obj != 0)){
		colorref *c = NULL;
		rect r1, r2;
		g->scaleImage(Ewe_Image(stack[1].obj),Ewe_Image(stack[2].obj),c = Ewe_Color(stack[3].obj),
			ewe2rect(stack[4].obj,r1),ewe2rect(stack[5].obj,r2));
		if (c != NULL) delete c;
	}
	return returnVar(1);
}

static Var GraphicsGetSize(Var stack[])
{
	WObject fm = stack[0].obj;
	WObject lines = stack[1].obj;
	int start = stack[2].intValue;
	int end = stack[3].intValue;
	WObject dim = stack[4].obj;
	rect r;
	Var v;
	v.intValue = 0;

	if (fm == 0 || lines == 0 || dim == 0) return v;
	getSize(fm,lines,start,end,0,0,&r);
	objectPtr(dim)[1].intValue = r.width;
	objectPtr(dim)[2].intValue = r.height;

	return v;
}
static int32 *xPoints=0,*yPoints=0,nPoints = 0;
static int startIndex,endIndex;

////////////////////////////////////////////////////////////////////////////
// draw an elliptical arc from startAngle to endAngle. c is the fill color and c2 is the outline color (if in fill mode - otherwise, c = outline color)
static void arcPiePointDrawAndFill(int xc, int yc, int rx, int ry, float startAngle, float endAngle, byte c, byte c2, byte fill, byte pie, int append)
{
   static int lastRX=-1,lastRY=-1,lastXC = -1, lastYC = -1,lastSize=0;
   static float lastPPD=0;
   // this algorithm was created by Guilherme Campos Hazan
   float ppd;
   int index,i;
   int nq,size=0;

   //int oldX,oldY;
   // step 0: correct angle values
   /*
   if (startAngle < 0.1 && endAngle > 359.9) // full circle? use the fastest routine instead
   {
      if (fill)
         fillEllipse(xc,yc,rx,ry,c);
      drawEllipse(xc,yc,rx,ry,fill?c2:c);
      return;
   }
   */
   // step 0: if possible, use cached results
   if (xc != lastXC || yc != lastYC || rx != lastRX || ry != lastRY)
   {
       // step 1: computes how many points the circle has (computes only 45 degrees and mirrors the rest)
       // intermediate terms to speed up loop
        long t1 = rx*rx, t2 = t1<<1, t3 = t2<<1;
        long t4 = ry*ry, t5 = t4<<1, t6 = t5<<1;
        long t7 = rx*t5, t8 = t7<<1, t9 = 0L;
        long d1 = t2 - t7 + (t4>>1);    // error terms
        long d2 = (t1>>1) - t8 + t5;

        register int x = rx, y = 0; // ellipse points

        while (d2 < 0)          // til slope = -1
        {
            y++;        // always move up here
            t9 += t3;
            if (d1 < 0) // move straight up
            {
                d1 += t9 + t2;
                d2 += t9;
            }
            else        // move up and left
            {
                x--;
                t8 -= t6;
                d1 += t9 + t2 - t8;
                d2 += t9 + t5 - t8;
            }
            size++;
        }

        do              // rest of top right quadrant
        {
            x--;        // always move left here
            t8 -= t6;
            if (d2 < 0) // move up and left
            {
                y++;
                t9 += t3;
                d2 += t9 + t5 - t8;
            }
            else        // move straight left
                d2 += t5 - t8;
          size++;

        } while (x >= 0);
       nq = size;
       size *= 4;
       // step 2: computes how many points per degree
       ppd = (float)size / 360.0f;
       // step 3: create space in the buffer so it can save all the circle
       size+=2;
       if (nPoints < size)
       {
          if (xPoints)
          {
             xfree(xPoints);
             xfree(yPoints);
          }
          xPoints = (int32 *)xmalloc(sizeof(int32)*size);
          yPoints = (int32 *)xmalloc(sizeof(int32)*size);
          if (!xPoints || !yPoints)
             return;
       }
       nPoints = size;
       // step 4: stores all the circle in the array. the odd arcs are drawn in reverse order
        // intermediate terms to speed up loop
        t2 = t1<<1; t3 = t2<<1;
        t8 = t7<<1; t9 = 0L;
        d1 = t2 - t7 + (t4>>1); // error terms
        d2 = (t1>>1) - t8 + t5;

        x = rx;
        y = 0;  // ellipse points

        i=0;
        while (d2 < 0)          // til slope = -1
        {
            // save 4 points using symmetry
            index = nq*0+i;   xPoints[index]=xc+x; yPoints[index]=yc-y; // 0/3
            index = nq*2-i-1; xPoints[index]=xc-x; yPoints[index]=yc-y; // 1/3
            index = nq*2+i;   xPoints[index]=xc-x; yPoints[index]=yc+y; // 2/3
            index = nq*4-i-1; xPoints[index]=xc+x; yPoints[index]=yc+y; // 3/3
            i++;

            y++;        // always move up here
            t9 += t3;
            if (d1 < 0) // move straight up
            {
                d1 += t9 + t2;
                d2 += t9;
            }
            else        // move up and left
            {
                x--;
                t8 -= t6;
                d1 += t9 + t2 - t8;
                d2 += t9 + t5 - t8;
            }
        }

        do              // rest of top right quadrant
        {
            // save 4 points using symmetry
            index = nq*0+i;   xPoints[index]=xc+x; yPoints[index]=yc-y; // 0/3
            index = nq*2-i-1; xPoints[index]=xc-x; yPoints[index]=yc-y; // 1/3
            index = nq*2+i;   xPoints[index]=xc-x; yPoints[index]=yc+y; // 2/3
            index = nq*4-i-1; xPoints[index]=xc+x; yPoints[index]=yc+y; // 3/3
            i++;

            x--;        // always move left here
            t8 -= t6;
            if (d2 < 0) // move up and left
            {
                y++;
                t9 += t3;
                d2 += t9 + t5 - t8;
            }
            else        // move straight left
                d2 += t5 - t8;
        } while (x >= 0);
       // save last arguments
       lastXC = xc;
       lastYC = yc;
       lastRX = rx;
       lastRY = ry;
       lastPPD = ppd;
       lastSize = size;
   }
   else ppd = lastPPD;
   // step 5: computes the start and end indexes that will become part of the arc
   if (!append)
	startIndex = (int)(ppd * startAngle);
   endIndex = (int)(ppd * endAngle);
   // step 5 1/2: if only computing the point, return it
   /*
   if (startAnglePoint)
   {
      startAnglePoint[0] = xPoints[startIndex];
      startAnglePoint[1] = yPoints[startIndex];
      return;
   }*/
   if (endIndex == (lastSize-2)) // 360?
      endIndex--;
   // step 6: fill or draw the polygons
	//else
   	//endIndex++;

   /*
   if (pie)
   {
      oldX = xPoints[endIndex];
      oldY = yPoints[endIndex];
      xPoints[endIndex] = xc;
      yPoints[endIndex] = yc;
      endIndex++;
   }
   if (fill)
      fillPolygon(xPoints+startIndex,yPoints+startIndex,endIndex-startIndex,c);
//   if (!fill || c != c2) always draw border
      drawPolygon(xPoints+startIndex,yPoints+startIndex,endIndex-startIndex,fill?c2:c,fill);
   if (pie) // restore saved points
   {
      xPoints[endIndex-1] = oldX;
      yPoints[endIndex-1] = oldY;
   }
	*/
}
////////////////////////////////////////////////////////////////////////////
// calls arcPiePointDrawAndFill twice if the angles cross the 0
static void preArcPiePointDrawAndFill(int xc, int yc, int rx, int ry, float startAngle, float endAngle, byte c, byte c2, byte fill, byte pie)
{
   // make sure the values are -359 <= x <= 359
   while (startAngle <= -360) startAngle += 360;
   while (endAngle   <= -360) endAngle   += 360;
   while (startAngle >   360) startAngle -= 360;
   while (endAngle   >   360) endAngle   -= 360;

   if (startAngle > endAngle) // eg 235 to 45
      startAngle -= 360; // set to -45 to 45 so we can handle it correctly
   if (startAngle >= 0 && endAngle <= 0) // eg 135 to -135
      endAngle += 360; // set to 135 to 225

   if (startAngle >= 0 && endAngle >= 0)
      arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, endAngle, c, c2, fill, pie, 0);
   else
   if (startAngle <= 0 && endAngle >= 0) // eg -45 to 45
   {
      startAngle += 360;
      arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, 360, c, c2, fill, pie, 0);
      arcPiePointDrawAndFill(xc, yc, rx, ry, 0, endAngle, c, c2, fill, pie, 1);
   } //else debug("arc/pie/point could not be filled with given angles");
}
static Var GraphicsGetArcPoints(Var stack[])
{
	Var v;
	WObject x,y;
	int len;
	int flags = stack[6].intValue;
	int extra = 0;
	preArcPiePointDrawAndFill(
		stack[0].intValue,stack[1].intValue,stack[2].intValue,stack[3].intValue,stack[4].floatValue,stack[5].floatValue,0,0,0,0);
	len = endIndex-startIndex+1;
	v.obj = 0;
	if (flags & 0x1) extra = 1;
	if (len >= 0){
		x = createArrayObject(arrayType('I'),len+extra);
		if (!x) return v;
		pushObject(x);
		y = createArrayObject(arrayType('I'),len+extra);
		if (!y){
			popObject();
			return v;
		}
		pushObject(y);
		memcpy((int32 *)WOBJ_arrayStart(x)+extra,xPoints+startIndex,len*sizeof(int32));
		memcpy((int32 *)WOBJ_arrayStart(y)+extra,yPoints+startIndex,len*sizeof(int32));
	}else{
		len = nPoints-startIndex-2;
		x = createArrayObject(arrayType('I'),len+endIndex+1+extra);
		if (!x) return v;
		pushObject(x);
		y = createArrayObject(arrayType('I'),len+endIndex+1+extra);
		if (!y){
			popObject();
			return v;
		}
		pushObject(y);
		memcpy((int32 *)WOBJ_arrayStart(x)+extra,xPoints+startIndex,len*sizeof(int32));
		memcpy((int32 *)WOBJ_arrayStart(y)+extra,yPoints+startIndex,len*sizeof(int32));
		memcpy((int32 *)WOBJ_arrayStart(x)+len+extra,xPoints,(endIndex+1)*sizeof(int32));
		memcpy((int32 *)WOBJ_arrayStart(y)+len+extra,yPoints,(endIndex+1)*sizeof(int32));
	}
	v.obj = createArrayObject(arrayType('L'),2);
	if (v.obj){
		((WObject *)WOBJ_arrayStart(v.obj))[0] = x;
		((WObject *)WOBJ_arrayStart(v.obj))[1] = y;
	}
	popObject();
	popObject();
	return v;
}

static Var GraphicsFillPolygon(Var stack[])
{
	EweGraphics g = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (g == NULL || (stack[1].obj == 0) || (stack[2].obj == 0) || stack[3].intValue <= 0) return returnVar(0);
	g->fillPolygon((int *)WOBJ_arrayStart(stack[1].obj),(int *)WOBJ_arrayStart(stack[2].obj),stack[3].intValue);
	return returnVar(1);
}
static Var GraphicsCopyGraphics(Var stack[])
{
	EweGraphics g = (EweGraphics)WOBJ_GraphicsNative(stack[0].obj);
	if (g == NULL || stack[1].obj == 0) return returnVar(0);
	EweGraphics other = (EweGraphics)WOBJ_GraphicsNative(stack[1].obj);
	if (other == NULL) return returnVar(0);
	rect r(stack[2].intValue,stack[3].intValue,stack[4].intValue,stack[5].intValue);
	return returnVar(g->copyFrom(other,r,stack[6].intValue,stack[7].intValue));
}

static Var ImageCreate(Var stack[])
{
	return setupNew(ewe->getNewImage(WOBJ_ImageWidth(stack[0].obj),WOBJ_ImageHeight(stack[0].obj),stack[1].intValue),stack[0].obj);
}
static Var ImageDoRotate(Var stack[])
{
	EweImage ei = (EweImage)WOBJ_ImageNative(stack[0].obj);
	ei->rotate((EweImage)WOBJ_ImageNative(stack[1].obj),stack[2].intValue);
	return returnVar(1);
}
static Var ImageToIcon(Var stack[])
{
	if (stack[0].obj != 0) {
		EweImage ei = (EweImage)WOBJ_ImageNative(stack[0].obj);
		if (ei != NULL)
			return returnVar(ei->createIcon(stack[1].obj == 0 ? NULL : (EweImage)WOBJ_ImageNative(stack[1].obj),stack[2].intValue));
	}
	return returnVar(0);
}

static Var BufferClear(Var stack[])
{
	EweImage ei = (EweImage)WOBJ_ImageNative(stack[0].obj);
	if (!ei->myImage) ei->myImage = new eweRGBImage(ei->width,ei->height);
	ei->myImage->persist = true;
	uint32 *p = ei->myImage->pixels;
	int x = stack[1].intValue, y = stack[2].intValue;
	int width = stack[3].intValue, height = stack[4].intValue;
	int max = width*height;
	int v = stack[5].intValue;
	//printf("Size: %d %d \n",width,height);
	//ei->myImage->valid.x = x;
	//ei->myImage->valid.y = y;
	//ei->myImage->valid.width = width;
	//ei->myImage->valid.height = height;

	if (x < 0) {
		width += x;
		x = 0;
	}
	else if (x >= ei->width) width = 0;
	if (width < 0) width = 0;
	if (x+width > ei->width) width = ei->width-x;
	if (y < 0) {
		height += y;
		y = 0;
	}
	else if (y >= ei->height) height = 0;
	if (height < 0) height = 0;
	if (y+height > ei->height) height = ei->height-y;


	for (int r = 0; r<height; r++){
		uint32 *p = ei->myImage->scanLine(r+y)+x;
		for (int c = 0; c<width; c++) *p++ = v;
	}
	return returnVar(1);
}

static Var ImageGetSetPixelsRect(Var stack[],int isGet)
{
	Var v;
	WObject image = stack[0].obj;
	WObject dest = stack[1].obj;
	int offset = stack[2].intValue;
	int x = stack[3].intValue;
	int y = stack[4].intValue;
	int w = stack[5].intValue;
	int h = stack[6].intValue;
	int opts = stack[6].intValue;
	int total = w*h+offset;
	int maxrow,maxcol;
	uint32 *by;
  	v.obj = 0;
	if (w <= 0 || h <= 0) return v;

	EweImage i = (EweImage)WOBJ_ImageNative(image);
//
// Always use 32 bpp images.
//

	if (dest != 0)
		if (WOBJ_arrayLen(dest) < total) dest = 0;
	if (dest == 0)
		dest = createArrayObject(arrayType('I'),total);
	v.obj = dest;
	if (dest == 0 || i == NULL) return v;

	by = (uint32 *)WOBJ_arrayStart(dest);
	by += offset;

	i->getSetPixels(by,rect(x,y,w,h),isGet);

	return v;
}
static Var ImageToCursor(Var stack[])
{
	EweImage i = (EweImage)WOBJ_ImageNative(stack[0].obj);
	if (i == NULL) return returnVar(0);
	return returnVar((int)i->createCursor((stack[1].obj == 0 ? NULL : (EweImage)WOBJ_ImageNative(stack[1].obj)),stack[2].intValue,stack[3].intValue));
}
static Var ImageFree(Var stack[])
{
	EweImage ei = (EweImage)WOBJ_ImageNative(stack[0].obj);
	if (ei == NULL) return returnVar(0);
	ei->free();
	return returnVar(1);
}

#ifndef fixColor
#define fixColor(CR) mapColor(CR)
#endif
static int masks[] ={0x80,0x40,0x20,0x10,0x08,0x04,0x02,0x01};

static void* getArray(WObject array, BOOL clear)
{
	if (array == 0) return NULL;
	else {
		void* ret = WOBJ_arrayStart(array);
		if (clear) memset(ret,0,WOBJ_arrayLen(array)*arrayTypeSize(WOBJ_arrayType(array)));
		return ret;
	}
}

static BOOL getSetScanLine(WObject scaleInfo, int whichLine, BOOL isGet)
{
	WClass *wc = WOBJ_class(scaleInfo);
	Var p[3];
	static WClassMethod *m = NULL;
	if (m == NULL){
		m = getMethod(wc,createUtfString("getSetScanLine"),createUtfString("(IZ)V"),NULL);
		if (m == NULL) return FALSE;
	}
	p[0].obj = scaleInfo;
	p[1].intValue = whichLine;
	p[2].intValue = isGet;
	executeMethod(wc,m,p,3);
	return thrownException == 0;
}
static void getSetImagePixelsRect(WObject image,int *by,int x,int y,int w,int h,int opts,int isGet)
{
	EweImage i = (EweImage)WOBJ_ImageNative(image);
	if (i == NULL) return;
	i->getSetPixels((uint32*)by,rect(x,y,w,h),isGet);
}
//
// We don't have access to the image bits,
// so we can't use images directly.
//
#define WOBJ_ImageBits(IMAGE) NULL

static WObject validateImage(WObject image)
{
#if 0
	if (image != 0 && WOBJ_ImageBits(image) != NULL) return image;
#endif
	return 0;
}
//===================================================================
static Var ScaleInfoScale(Var stack[])
//===================================================================
{
	WObject si = stack[0].obj;
	Var *v = objectPtr(si)+1;
/*
This is the data in a ScaleInfo object.

ImageData source, destination;
//
// This is set if source and destination are images respectively.
//
Image sourceImage, destinationImage;
//
int srcType, dstType;
//
int newWidth, newHeight, destX, destY;
//
int sWidth, sHeight, sbpl;
//
int dWidth, dHeight, dbpl;
//
int[] red, green, blue;
int[] scales;
//
byte[] srcB, destB;
int[] srcI;
//
int[] alpha;
//
int options;
*/

/*
The alpha array is only true if both the source and destination have alpha channels.
*/

#define S_GRAY  1
#define S_MONO  2
#define S_COLOR 3
	WObject source = (v++)->obj;
	WObject destination = (v++)->obj;
	WObject sourceImage = validateImage((v++)->obj);
	WObject destinationImage = validateImage((v++)->obj);
	int srcType = (v++)->intValue;
	int dstType = (v++)->intValue;
	int newWidth = (v++)->intValue;
	int newHeight = (v++)->intValue;
	int destX = (v++)->intValue;
	int destY = (v++)->intValue;
	int sWidth = (v++)->intValue;
	int sHeight = (v++)->intValue;
	int sbpl = (v++)->intValue;
	int dWidth = (v++)->intValue;
	int dHeight = (v++)->intValue;
	int dbpl = (v++)->intValue;
	WObject red = (v++)->obj;
	WObject green = (v++)->obj;
	WObject blue = (v++)->obj;
	WObject scales = (v++)->obj;
	WObject srcB = (v++)->obj;
	WObject destB = (v++)->obj;
	WObject srcI = (v++)->obj;
	WObject alpha = (v++)->obj;
	int options = (v++)->obj;
	//
	int curSourceScanLine = -1;
	//
	int dx = sWidth/newWidth;
	int rx = sWidth%newWidth;
	//
	int dy = sHeight/newHeight;
	int ry = sHeight%newHeight;
	int yq = 0;
	int maxX = dWidth+destX;
	//
	BOOL isRough = ((options & 0x1) != 0);
	int64 maxScale = isRough ? 1 : (int64)(dx+1)*(int64)(dy+1);
	BOOL runAverage = ((maxScale*255L) > 0x000000007fffffffL);//Integer.MAX_VALUE;
	BOOL hasAlpha = alpha != 0;
	//
	int *redP = NULL;
	int *greenP = NULL;
	int *blueP = NULL;
	int *scalesP = NULL;
	int *alphaP = NULL;
	byte *destBP = NULL;
	//
	//ewe.sys.Vm.debug(srcGray+", "+dstGray+", "+newWidth+", "+newHeight+", "+yOffset+", "+stripHeight);
	//
	int sy = 0;
	int redLength = WOBJ_arrayLen(red);
	int y; for(y = 0; y<destY+dHeight && y<newHeight; y++){
		int yt = dy;
		yq += ry;
		if (yq >= newHeight) {
			yt++; //How many source lines to use.
			yq -= newHeight;
		}
		if (y >= destY){
			//
			int yyt = yt == 0 ? 1 : yt;
			int yy;
			//
			// yyt now holds the number of source scan lines that will be used for the current
			// destination scan line.
			//
			BOOL dontPrepare = sy == curSourceScanLine;
			if (dontPrepare){
				yyt = 0;
			}else{
				redP = (int*)getArray(red,TRUE);
				greenP = (int*)getArray(green,TRUE);
				blueP = (int*)getArray(blue,TRUE);
				scalesP = (int*)getArray(scales,TRUE);
				alphaP = (int*)getArray(alpha,TRUE);
				destBP = (byte*)getArray(destB,TRUE);
				if (isRough) yyt = 1;
			}
			for(yy = 0; yy<yyt; yy++){
				//
				// Read in the current scan line.
				//
				//
				// Now go across horizontally.
				//
				int xq = 0, di = 0, sx = 0;
				int x;
				int srcLine = sy+yy;
				int* srcIP = (int*)getArray(srcI,FALSE);
				byte* srcBP = (byte*)getArray(srcB,FALSE);
				if (srcLine != curSourceScanLine){
					if (sourceImage != 0){
						if (srcIP)
							getSetImagePixelsRect(sourceImage,srcIP,0,srcLine,sWidth,1,0,TRUE);
						else
							memcpy(srcBP,(byte*)WOBJ_ImageBits(sourceImage)+(srcLine*sbpl),sbpl);
					}else{
						if (!getSetScanLine(si,srcLine,TRUE))
							return returnVar(0);
						redP = (int*)getArray(red,FALSE);
						greenP = (int*)getArray(green,FALSE);
						blueP = (int*)getArray(blue,FALSE);
						destBP = (byte*)getArray(destB,FALSE);
						scalesP = (int*)getArray(scales,FALSE);
						srcIP = (int*)getArray(srcI,FALSE);
						srcBP = (byte*)getArray(srcB,FALSE);
						alphaP = (int*)getArray(alpha,FALSE);
					}
				}
				curSourceScanLine = srcLine;
				for(x = 0; x<newWidth && x<maxX; x++){
					int xt = dx, xxt;
					xq += rx;
					if (xq >= newWidth){
						xt++;
						xq -= newWidth;
					}
					xxt = xt == 0 ? 1 : xt;
					if (isRough) xxt = 1;
					if (x >= destX){
						int ss = (srcType == S_MONO) ? sx/8 : sx;
						int smask = masks[sx%8];
						int xx; for(xx = 0; xx<xxt; xx++){
							int r, g, b, a = 0;
							if (srcType == S_COLOR){
								int value = srcIP[ss];
								a = (value >> 24) & 0xff;
								r = (value >> 16) & 0xff;
								g = (value >> 8) & 0xff;
								b = (value) & 0xff;
								if (hasAlpha && a == 0) r = g = b = 0;
								ss++;
							}else if (srcType == S_GRAY){
								r = ((int)srcBP[ss]) & 0xff;
								g = b = r;
								ss++;
							}else{ // MONO
								int value = 0;
								if ((srcBP[ss] & smask) != 0) value = 0xff;
								r = value;
								g = b = r;
								smask >>= 1;
								if (smask == 0) {
									smask = 0x80;
									ss++;
								}
							}
							if (runAverage){
								int64 didAlready = (yy*xxt)+xx;
								if (dstType == S_COLOR){
									redP[di] = (int)((((int64)redP[di]*didAlready)+r)/(didAlready+1));
									greenP[di] = (int)((((int64)greenP[di]*didAlready)+g)/(didAlready+1));
									blueP[di] = (int)((((int64)blueP[di]*didAlready)+b)/(didAlready+1));
									if (alpha != 0) alphaP[di] = (int)((((int64)alphaP[di]*didAlready)+a)/(didAlready+1));
								}else{
									redP[di] += (int)((((int64)redP[di]*didAlready)+((r+g+b)/3))/(didAlready+1));
								}
							}else{
								if (dstType == S_COLOR){
									redP[di] += r;
									greenP[di] += g;
									blueP[di] += b;
									if (alpha != 0) alphaP[di] += a;
								}else{
									redP[di] += (r+g+b)/3;
								}
							}
						}
						//
						// Done all the horizontal pixels on this line, for this dest pixel.
						// Now move on to the next pixel on this line.
						//
						scalesP[di] = xxt*yyt;
						di++;
					}
					sx += xt;
				}
			}
			//
			// Have done one destination scan line.
			// So now average it out and write it.
			//
			if (!dontPrepare){
				int i;
				if (!isRough && !runAverage)
					for (i = 0; i<redLength; i++){
						redP[i] /= scalesP[i];
						if (green != 0) greenP[i] /= scalesP[i];
						if (blue != 0) blueP[i] /= scalesP[i];
						if (alpha != 0) alphaP[i] /= scalesP[i];
					}
				//
				if (dstType == S_COLOR){
					for (i = 0; i<redLength; i++){
						redP[i] = ((redP[i] & 0xff) << 16)|((greenP[i] & 0xff) << 8)|(blueP[i] & 0xff);
						if (alpha == 0) redP[i] |= 0xff000000;
						else redP[i] |= (alphaP[i] & 0xff) << 24;
					}
				}else if (dstType == S_GRAY){
					for (i = 0; i<redLength; i++)
						destBP[i] = (byte)redP[i];
				}else{
					int mm = 0x80, dd = 0;
					for (i = 0; i<redLength; i++){
						if (redP[i] > 127) destBP[dd] |= (byte)mm;
						mm >>= 1;
						if (mm == 0) {
							mm = 0x80;
							dd++;
						}
					}
				}
			}
			if (destinationImage != 0){
				if (dstType == S_COLOR)
					getSetImagePixelsRect(destinationImage,redP,0,y-destY,dWidth,1,0,FALSE);
				else
					memcpy((byte*)WOBJ_ImageBits(destinationImage)+((y-destY)*dbpl),destBP,dbpl);
			}else{
				if (!getSetScanLine(si,y-destY,FALSE))
					return returnVar(0);
			}
		}
		sy += yt;
	}
	return returnVar(0);
}
/**
This is an option to be  used in Image initializers.
It is used to specify a 24-bit image RGB image. It is the same as TRUE_COLOR
**/
#define RGB_IMAGE 0x1
/**
This is an option to be  used in Image initializers.
It is used to specify a 32-bit image RGB image with an Alpha (transparency)
channel. It is the same as RGB_IMAGE|ALPHA_CHANNEL.
**/
#define ARGB_IMAGE (0x1|0x2)
/**
* This is an option for use with the constructor Image(ewe.io.Stream stream,int options,int requestedWidth,int requestedHeight)
* It specifies that the image should be scaled in the X and Y plane to be exactly the size
* specified. If this is omitted the image will be scaled to fit in the specfied dimensions
* but keeping the same aspect ratio.
**/
#define SCALE_IMAGE 0x10
/**
Use this to specify a monochrome image, thereby using the smallest amount of space
for the image.
**/
#define MONO_IMAGE 0x20
/**
Use this to specify a GRAY_SCALE_IMAGE image at one byte per pixel.
**/
#define GRAY_SCALE_256_IMAGE 0x400
/**
Use this to specify a GRAY_SCALE_IMAGE image at one byte per pixel.
**/
#define GRAY_SCALE_16_IMAGE 0x200
/**
Use this to specify a GRAY_SCALE_IMAGE image at two bits per pixel.
**/
#define GRAY_SCALE_4_IMAGE 0x100
/**
Use this to specify a GRAY_SCALE_IMAGE image at one bit per pixel - this is the same
as MONO_IMAGE;
**/
#define GRAY_SCALE_2_IMAGE MONO_IMAGE
/**
Use this to specify an INDEXED_IMAGE image at one byte per pixel.
**/
#define INDEXED_256_IMAGE 0x4000
/**
Use this to specify an INDEXED_IMAGE image at 4 bits per pixel.
**/
#define INDEXED_16_IMAGE 0x2000
/**
Use this to specify an INDEXED_IMAGE image at 2 bits per pixel.
**/
#define INDEXED_4_IMAGE 0x1000
/**
Use this to specify an INDEXED_IMAGE image at 2 bits per pixel.
**/
#define INDEXED_2_IMAGE 0x8000

// private static native void argbConvert
// (boolean isTo,int sourceImageDataType, int sourceWidth, int[] sourceColorTable,
// Object sourceScanLines, int sourceOffset, int sourceScanLineLength,
// int[] argbData, int argbOffset, int argbScanLineLength, int numScanLines);

static int argbMasks[] = {0x80,0xc0,0,0xf0,0,0,0,0xff};

static Var ImageToolRGBConvert(Var stack[])
{
	BOOL isTo = stack[0].intValue;
	int it = stack[1].intValue & 0xffff;
	int sourceWidth = stack[2].intValue;
	int* sourceColorTable = stack[3].obj == 0 ? NULL : (int*)WOBJ_arrayStart(stack[3].obj);
	int sourceColorTableLength = sourceColorTable == NULL ? 0 : WOBJ_arrayLen(stack[3].obj);
	WObject sourceScanLines = stack[4].obj;
	int sourceOffset = stack[5].intValue;
	int sourceScanLineLength = stack[6].intValue;
	int* argbData = (int*)WOBJ_arrayStart(stack[7].obj);
	int argbOffset = stack[8].intValue;
	int argbScanLineLength = stack[9].intValue;
	int numScanLines = stack[10].intValue;
	int bpp = 8;

	if (sourceOffset < 0 || sourceOffset+sourceScanLineLength*numScanLines > WOBJ_arrayLen(sourceScanLines))
		return returnException(ArrayIndexEx,NULL);
	if (argbOffset < 0 || argbOffset+argbScanLineLength*numScanLines > WOBJ_arrayLen(stack[7].obj))
		return returnException(ArrayIndexEx,NULL);
	//
	if (it == GRAY_SCALE_2_IMAGE || it == INDEXED_2_IMAGE) bpp = 1;
	else if (it == GRAY_SCALE_4_IMAGE || it == INDEXED_4_IMAGE) bpp = 2;
	else if (it == GRAY_SCALE_16_IMAGE || it == INDEXED_16_IMAGE) bpp = 4;
	//
	if (isTo){
		if (it != RGB_IMAGE && it != ARGB_IMAGE){
			byte* data = (byte*)WOBJ_arrayStart(sourceScanLines);
			int startMask = argbMasks[bpp-1];
			int startShift = 8-bpp;
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				int mask = startMask;
				int shift = startShift;
				int x;
				for (x = 0; x<sourceWidth; x++){
					int index = (((int)data[si]&0xff) & mask)>>shift;
					int color = 0xff000000|sourceColorTable[index];
					argbData[di++] = color;
					mask >>= bpp;
					shift -= bpp;
					if (mask == 0){
						mask = startMask;
						shift = startShift;
						si++;
					}
				}
			}
		}else if (WOBJ_arrayType(sourceScanLines) == 8){//Byte Array
			//
			// Source IS RGB/ARGB but data is in byte form.
			// The data must be in R,G,B(,A) sequence.
			//
			byte* data = (byte*)WOBJ_arrayStart(sourceScanLines);
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				int x;
				for (x = 0; x<sourceWidth; x++){
					int r = (int)data[si++] & 0xff;
					int g = (int)data[si++] & 0xff;
					int b = (int)data[si++] & 0xff;
					int a = it == ARGB_IMAGE ? data[si++] & 0xff : 0xff;
					argbData[di++] = (a<<24)|(r<<16)|(g<<8)|(b);
				}
			}
		}else if (it == ARGB_IMAGE){ // Just copy the data straight across.
			int* data = (int*)WOBJ_arrayStart(sourceScanLines);
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				memcpy(argbData+di,data+si,sourceWidth*sizeof(int));
			}
		}else{ // it == ImageData.TYPE_RGB - have to make sure that the alpha channel is 0xff
			int* data = (int*)WOBJ_arrayStart(sourceScanLines);
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				int x;
				for (x = 0; x<sourceWidth; x++){
					argbData[di++] = data[si++] & 0xff000000;
				}
			}
		}
	}else{ // !isTo
		if (it != RGB_IMAGE && it != ARGB_IMAGE){
			byte* data = (byte*)WOBJ_arrayStart(sourceScanLines);
			int startMask = argbMasks[bpp-1];
			int startShift = 8-bpp;
			int lastIndex = -1;
			int lastColor = 0;
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				int mask = startMask;
				int shift = startShift;
				int x;
				for (x = 0; x<sourceWidth; x++){
					int color = argbData[di++] & 0xffffff;
					int index = -1;
					if (color == lastColor && lastIndex != -1){
						index = lastIndex;
					}else{
						int i;
						for (i = 0; i<sourceColorTableLength; i++){
							if ((sourceColorTable[i] & 0xffffff) == color){
								index = i;
								break;
							}
						}
						//
						// Didn't find an exact match, so have to find
						// closest match.
						//
						if (index == -1){
							int diff = 0;
							int r = (color >> 16) & 0xff, g = (color >> 8) & 0xff, b = color & 0xff;
							for (i = 0; i<sourceColorTableLength; i++){
								int cc = sourceColorTable[i];
								int rd = ((cc >> 16) & 0xff)-r;
								int gd = ((cc >> 8) & 0xff)-g;
								int bd = ((cc) & 0xff)-b;
								int t;
								if (rd < 0) rd = -rd;
								if (gd < 0) gd = -gd;
								if (bd < 0) bd = -bd;
								t = rd+gd+bd;
								if (t < diff || index == -1){
									index = i;
									diff = t;
								}
							}
						}
						lastColor = color;
						lastIndex = index;
					}
					data[si] &= (byte)~mask;
					data[si] |= (byte)(index << shift);
					mask >>= bpp;
					shift -= bpp;
					if (mask == 0){
						mask = startMask;
						shift = startShift;
						si++;
					}
				}
			}
		}else if (WOBJ_arrayType(sourceScanLines) == 8){//Byte Array
			//
			// source IS RGB/ARGB but data is in byte form.
			// The data must be in R,G,B(,A) sequence.
			//
			byte* data = (byte*)WOBJ_arrayStart(sourceScanLines);
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				int x;
				for (x = 0; x<sourceWidth; x++){
					int color = argbData[di++];
					data[si++] = (byte)(color >> 16);
					data[si++] = (byte)(color >> 8);
					data[si++] = (byte)(color);
					if (it == ARGB_IMAGE)
						data[si++] = (byte)(color >> 24);
				}
			}
		}else if (it == ARGB_IMAGE){ // Just copy the data straight across.
			int* data = (int*)WOBJ_arrayStart(sourceScanLines);
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				memcpy(data+si,argbData+di,sourceWidth*sizeof(int));
			}
		}else{ // it == ImageData.TYPE_RGB - have to make sure that the alpha channel is 0xff
			int* data = (int*)WOBJ_arrayStart(sourceScanLines);
			int y;
			for (y = 0; y<numScanLines; y++){
				int si = sourceOffset+sourceScanLineLength*y;
				int di = argbOffset+argbScanLineLength*y;
				int x;
				for (x = 0; x<sourceWidth; x++){
					data[si++] = argbData[di++] | 0xff000000;
				}
			}
		}
	}
	return returnVar(0);
}


static Var ImageSetPixelsRect(Var stack[]) {return ImageGetSetPixelsRect(stack,0);}
static Var ImageGetPixelsRect(Var stack[]) {return ImageGetSetPixelsRect(stack,1);}
static Var ImageGrayPixels(Var stack[])
{
	Var v;
	WObject pixels = stack[0].obj;
	int start = stack[1].intValue;
	int length = stack[2].intValue;
	int transparent = fixColor(COLORREF((stack[3].intValue) & 0xffffff)).toRGB();
	int *buff = (int *)WOBJ_arrayStart(pixels);
	int i;

	for (i = 0; i<length; i++){
		int val = buff[i+start], r,g,b, tot;
		if ((val & 0xffffff) == transparent) continue;
		r = (val >> 16) & 0xff, g = (val >> 8) & 0xff, b = val & 0xff;
		tot = r+g+b;
		if (tot < 3*128) tot = 3*128;
		tot /= 3;
		buff[i+start] = (val & 0xff000000) | (tot << 16) | (tot << 8) | tot;
	}
	v.intValue = 0;
	return v;
}

static Var VmSetCursorHandle(Var stack[])
{
	if (stack[0].obj == 0) return returnVar(0);
	EweWindow ew = (EweWindow)WOBJ_WindowNative(stack[0].obj);
	if (ew == NULL) ew = eweWindow::mainWindow;
	if (ew == NULL) return returnVar(0);
	return returnVar(ew->setCursor(stack[1].intValue));
}
static Var VmGetCursorHandle(Var stack[])
{
	return returnVar(ewe->getCursor(stack[0].intValue));
}
static Var VmGetAsyncKeyState(Var stack[])
{
	return returnVar(eweWindow::getKeyState(stack[0].intValue));
}

//########################################################################
typedef struct vm_image_access *VmImageAccess;
//########################################################################
struct vm_image_access {
//########################################################################
	void *eniImageRef;
	void *nativeRef;
	int bitsPerPixel;
	int bytesPerLine;
	int width;
	int height;
	unsigned char *(*holdReleasePixels)(VmImageAccess access,unsigned char *pixels);
	unsigned char *(*holdReleaseAlpha)(VmImageAccess access,unsigned char *alpha);
	void (*getSetPixels)(VmImageAccess access,int *pixelData,int x,int y,int width,int height,int options,int isGet);
	int (*drawCapture)(VmImageAccess access,void *nativeDrawingSurface,int imageX,int imageY,int width,int height,int surfaceX,int surfaceY,int isCapture);
	void (*pasteCopy)(VmImageAccess access,VmImageAccess other,int imageX,int imageY,int width,int height,int otherX,int otherY,int isCopy);
	void (*freeImageAccess)(VmImageAccess access);
//########################################################################
};
//########################################################################

static unsigned char *holdReleasePixels(VmImageAccess access,unsigned char *pixels)
{
	return NULL;
}
static unsigned char *holdReleaseAlpha(VmImageAccess access,unsigned char *alpha)
{
	return NULL;
}
static void getSetPixels(VmImageAccess access,int *pixelData,int x,int y,int width,int height,int options,int isGet)
{
	EweImage i = (EweImage)WOBJ_ImageNative((WObject)access->eniImageRef);
	i->getSetPixels((uint32 *)pixelData,rect(x,y,width,height),isGet);
}
static int drawCapture(VmImageAccess access,void *nativeSurface,
							int imageX,int imageY,int width,int height,
							int surfaceX, int surfaceY, int isCapture)
{
	//if (isCapture) return 0;
	if (access == NULL || nativeSurface == NULL || access->nativeRef == NULL){
		return 1;
	}else{
			EweWindow surf = (EweWindow)nativeSurface;
			EweImage image = (EweImage)access->nativeRef;
			if (!isCapture){
				rect r(imageX,imageY,width,height);
				EweRGBImage i = image->toEweRGB(r);
				surf->writeEweRGB(*i,surfaceX,surfaceY);
				delete i;
			}else{
				rect r(surfaceX,surfaceY,width,height);
				EweRGBImage i = surf->toEweRGB(r);
				image->writeEweRGB(*i,imageX,imageY);
				delete i;
			}
			return 1;
	}
}
#ifdef USE_PTHREADS
static mLock copyLock;
#endif
static void pasteCopy(VmImageAccess access,VmImageAccess otherImage,
							int imageX,int imageY,int width,int height,
							int otherX, int otherY, int isCopy)
{
	static int numPixels = 0;
	static uint32 *pixels = NULL;
	if (access == NULL || otherImage == NULL || access->nativeRef == NULL || otherImage->nativeRef == NULL){
		return;
	}else{
		int total = width*height;
		if (total <= 0) return;
#ifdef USE_PTHREADS
		copyLock.lock();
#endif
		if (total > numPixels)
			if (pixels != NULL) delete pixels;
		pixels = new uint32[numPixels = total];

		EweImage other = (EweImage)otherImage->nativeRef;
		EweImage image = (EweImage)access->nativeRef;
		if (!isCopy){
			image->getSetPixels(pixels,rect(imageX,imageY,width,height),1);
			other->getSetPixels(pixels,rect(otherX,otherY,width,height),0);
		}else{
			other->getSetPixels(pixels,rect(otherX,otherY,width,height),1);
			image->getSetPixels(pixels,rect(imageX,imageY,width,height),0);
		}
#ifdef USE_PTHREADS
		copyLock.unlock();
#endif
		return;
	}
}

static void freeImageAccess(VmImageAccess access)
{
	free(access);
}
VmImageAccess createImageAccess(WObject image)
{
	VmImageAccess va = (VmImageAccess)mMalloc(sizeof(struct vm_image_access));
	if (va == NULL) return NULL;
	va->eniImageRef = (void *)image;
	EweImage qp = (EweImage)WOBJ_ImageNative(image);
	va->nativeRef = (void *)qp;
	va->width = WOBJ_ImageWidth(image);
	va->height = WOBJ_ImageHeight(image);
	va->bitsPerPixel = 0;//qp->bitsPerPixel();
	va->bytesPerLine = 0;//qp->bytesPerLine();
	va->holdReleasePixels = holdReleasePixels;
	va->holdReleaseAlpha = holdReleaseAlpha;
	va->getSetPixels = getSetPixels;
	va->drawCapture = drawCapture;
	va->pasteCopy = pasteCopy;
	va->freeImageAccess = freeImageAccess;
	return va;
}
static Var ImageGetNativeResource(Var stack[])
{
	return returnVar((int)createImageAccess(stack[0].obj));
}
