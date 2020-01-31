__IDSTRING(rcsid_nmunix_gui_qt2, "$MirOS: contrib/hosted/ewe/vm/nmunix_gui_qt2.cpp,v 1.1 2008/04/30 19:57:23 tg Exp $");

#include <qpainter.h>
#include <qevent.h>
#include <qpixmap.h>
#include <qtimer.h>
#include <qfont.h>
#include <qfontmetrics.h>
#include <qimage.h>
#include <qlist.h>
#include <qbutton.h>
#include <qfontdatabase.h>
#include <qclipboard.h>
#include <qdatetime.h>
#include <qbitmap.h>
#include <qsound.h>
#include <qnamespace.h>
#ifdef QTOPIA
#include <qpe/qpeapplication.h>
#ifdef ZAURUS
#include <qpe/custom-sharp.h>
#include <qpe/qcopenvelope_qws.h>
#include <qpe/global.h>
#endif
#define APP_BASE QPEApplication
#else
#define APP_BASE QApplication
#include <qapplication.h>
#endif

QRect & set(QRect & qr, class rect& r)
{
	qr.setRect(r.x,r.y,r.width,r.height);
	return qr;
}
void toUnicode(QString *str,WCHAR *dest)
{
	if (str == NULL) return;
	int len = str->length();
	const QChar *chars = str->unicode();
	for (int i = 0; i<len; i++)
		dest[i] = chars[i].unicode();
}
WObject newString(QString *str)
{
	if (str == NULL) return 0;
	int length;
	WCHAR *ret;
	WObject nstr = createNewString(str->length(),&ret,NULL);
	toUnicode(str,ret);
	return nstr;
}
QString newQString(WObject str)
{
	QString ret;
	if (str == 0) return ret;
	WCHAR *codes = WOBJ_StringChars(str);
	int len = WOBJ_StringLength(str);
	ret.setUnicodeCodes(codes,len);
	return ret;
}
static int compareChars(uint16 one,uint16 two,int id,int options)
{
	QChar o((ushort)one), t((ushort)two);
	if (options & NORM_IGNORECASE) o = o.lower(), t = t.lower();
	if (o < t) return -1;
	else if (o > t) return 1;
	else return 0;
}
//
// FIXME - cache this for better performance.
//
/*
//###################################################
class qtFont{
//###################################################
	QFont* qf;
public:
	qtFont() {qf = NULL;}
	~qtFont() {if (qf != NULL) delete qf;}
bool makeFrom(class graphics_font& f)
{
	int flags = f.style;
	int wt = flags & Font_BOLD ? QFont::Bold : QFont::Normal;
	bool it = flags & Font_ITALIC;
	qf = new QFont(QString(f.name),f.size,wt,it);
	return true;
}
	QFont* font() {return qf;}
//###################################################
};
//###################################################
*/
static FONT defFont;

static QFont* loadFont(class graphics_font& f,int &ascent,int &descent,int &leading)
{
	int flags = f.style;
	int wt = flags & Font_BOLD ? QFont::Bold : QFont::Normal;
	bool it = flags & Font_ITALIC;
	QFont* qf = new QFont(QString(f.name.str),f.size,wt,it);
	QFontMetrics fm(*qf);
	ascent = fm.ascent();
	descent = fm.descent();
	leading = fm.leading();
	return qf;
}


//###################################################
class qtFontMetrics : public eweFontMetrics {
//###################################################

	virtual void destroy(){}

	virtual int width(WCHAR ch)
	{
		QFontMetrics qfm(*(QFont*)nativeFont.nativeFont);
		return qfm.width(QChar(ch));
	}
	virtual int width(WCHAR *chars,int count)
  	{
		QFontMetrics qfm(*(QFont*)nativeFont.nativeFont);
		QString str;
		str.setUnicodeCodes((const ushort *)chars,(unsigned int)count);
		return qfm.width(str);
	}
//###################################################
};
//###################################################

/**

Qt insists on closing the application if a window is deleted when there are no other visible windows.
Hiding the window does not show this effect. So destroying a window will put the window on hold until another
window becomes visible, at which point the windows on hold will be deleted.

**/
static void deleteWaiting();


static void writeEweRGB(QPaintDevice* dev, eweRGBImage &data,int destX,int destY,class rect& areaInImage)
{
	QPainter p;
	if (!p.begin(dev)) return;
	QImage image(areaInImage.width,areaInImage.height,32);
	if (data.usesAlpha) image.setAlphaBuffer(true);
	for (int y = 0; y<areaInImage.height; y++){
		uint32 *pix = data.scanLine(y+areaInImage.y)+areaInImage.x;
		memcpy(image.scanLine(y),pix,4*areaInImage.width);
	}
	p.drawImage(destX,destY,image);
	p.end();
}
static void writeEweRGB(QPaintDevice* dev, eweRGBImage &data,int destX,int destY,class rect *destClip = NULL)
{
	rect use(destX,destY,data.width,data.height);
	if (destClip != NULL) use = use.intersect(*destClip);
	if (use.width <= 0 || use.height <= 0) return;
	int xoff = use.x-destX, yoff = use.y-destY;
	destX = use.x; destY = use.y;
	use.x = xoff; use.y = yoff;
	writeEweRGB(dev,data,destX,destY,use);
}

static void getTrueScreenDim(int &width,int &height)
{
#ifdef ZAURUS
		width = 320;//480;
		height = 240;//640;
#else
		width = 480;
		height = 640;
#endif

		QWidget *desktop = QApplication::desktop();
		if (desktop != NULL){
			height = desktop->height();
			width = desktop->width();
		}
}

//###################################################
class qtWindow : public eweWindow, public QWidget{
//###################################################
public:
//QPainter* painter;

virtual void requestDelete();

qtWindow()
{
	setMouseTracking(true);
	//printf("Created: %x\n",this);
	//painter = NULL;
}
virtual int setCursor(int handle)
{
	if (handle != NULL) QWidget::setCursor(*(QCursor *)handle);
	else QWidget::setCursor(Qt::arrowCursor);
}

virtual void destroy() {}
virtual void* getDrawable() {return (QPaintDevice*)this;}
virtual void writeEweRGB(eweRGBImage &data,int destX,int destY,class rect *destClip = NULL)
{
	::writeEweRGB(this,data,destX,destY,destClip);
}

virtual bool getClientRect(class rect &r)
{
	QRect fr = frameGeometry();
	QRect rc = geometry();
	r.set(rc.x()-fr.x(),rc.y()-fr.y(),rc.width(),rc.height());
	return true;
}

virtual bool getSetInfo(int code, WObject source, WObject dest, int options, Var& ret)
{
	switch(code){
	case INFO_NATIVE_WINDOW:
		{
			if (options == 0){//Containing window.
				setLong(dest,(int64)this);
			}else if (options == 1){//Drawing Surface.
				setLong(dest,(int64)this);
			}
			ret.obj = dest;
			return true;
		}
	}
	return false;
}
virtual bool specialOperation(int operation, WObject data, bool& ret)
{
	switch(operation){
#ifdef ZAURUS
	case 5: //Restart Qtopia
	{
		QCopEnvelope e("QPE/System","restart()");
		return true;
	}
#endif
	default:
		return false;
	}
}

virtual bool getWindowRect(class rect &r)
{
	QRect rc = geometry();
	r.set(rc.x(),rc.y(),rc.width(),rc.height());
	return true;
}
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


virtual int getFlagsForSize(class rect &requested,class rect &screen,int flagsToSet,int flagsToClear)
{
		int flags = 0;
		if (requested.width < 0 || requested.width >= screen.width)
			flags |= FLAG_IS_DEFAULT_SIZE;
		if (requested.height < 0 || requested.height >= screen.height)//-TASKBARSIZE)
			flags |= FLAG_IS_DEFAULT_SIZE;
		flags |= FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON;
		return adjustFlags(flags,flagsToSet,flagsToClear,NULL,NULL);
}
/*
virtual int getFlagsForSize(rect &requested,rect &screen,int flagsToSet,int flagsToClear)
{
		int flags = 0;
		int ww = requested.width;
		int wh = requested.height;

		RECT rc;
		int width, height;
		int setFlags = WOBJ_RectX(r);
		checkRotated(flagsToSet);
		getParentRect(NULL,&rc,screenIsRotated);
		width = rc.right;
		height = rc.bottom-rc.top;
		if (r == 0) return v;
		if (ww < 0 || ww >= width)
			flags |= FLAG_IS_DEFAULT_SIZE;
		if (wh < 0 || wh >= height-TASKBARSIZE)
			flags |= FLAG_IS_DEFAULT_SIZE;
		flags |= FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON;

#ifdef WINCE
#if defined(HPC) || defined(CASIOBE300) || (_WIN32_WCE < 300)
//		if (flags & FLAG_IS_DEFAULT_SIZE)
//			flags &= ~(FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON);
#endif
#endif

		flags = adjustMainWinFlags(flags,WOBJ_RectX(r),WOBJ_RectY(r),NULL,NULL);
		flags |= FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON;
		if (dest != 0) setLong(dest,flags);
		v.obj = dest;
		return v;
}
*/
virtual void setLocation(int x,int y)
{
	QRect rc = frameGeometry();
	setGeometry(x,y,rc.width(),rc.height());
}
virtual void setSize(int w,int h)
{
	class rect r(0,0,w,h);
	setRect(r,true);
}
virtual void setRect(class rect &r,bool isClient)
{
	QRect rc = geometry();
	if (isClient)
		setGeometry(rc.x(),rc.y(),r.width,r.height);
	else
		setGeometry(r.x-4,r.y-23,r.width+8,r.height+30);
}
void resizeEvent(QResizeEvent *re)
{
	QSize sz = re->size();
	eweWindow::resizeEvent(sz.width(),sz.height());
}
void paintEvent(QPaintEvent *qp)
{
	QRect r = qp->rect();
	class rect rr;
	rr.set(r.x(),r.y(),r.width(),r.height());
	eweWindow::paintEvent(rr);
}
virtual void grabMouse(void* grabData)
{
	QWidget::grabMouse();
}
virtual void ungrabMouse(void* ungrabData)
{
	QWidget::releaseMouse();
}
int getKeyModifiers(int state)
{
	int modifiers = 0;
	if (state & Qt::ShiftButton) modifiers |= 0x4;
	if (state & Qt::ControlButton) modifiers |= 0x2;
	if (state & Qt::AltButton) modifiers |= 0x1;
	return modifiers;
}

void mousePressEvent(QMouseEvent *me)
{
	int modifiers = 0;
	eweWindow::mousePressed(me->x(),me->y(),me->button() == QMouseEvent::RightButton,
		getKeyModifiers(me->state()),me);
}
void mouseReleaseEvent(QMouseEvent *me)
{
	eweWindow::mouseReleased(me->x(),me->y(),me->button() == QMouseEvent::RightButton,
		getKeyModifiers(me->state()),me);
}
void mouseMoveEvent(QMouseEvent *me)
{
	eweWindow::mouseMoved(me->x(),me->y(),getKeyModifiers(me->state()));
}

void closeEvent(QCloseEvent *ev)
{
	ev->ignore();
	eweWindow::closeEvent();
}
void focusInEvent(QFocusEvent *ev);
void focusOutEvent(QFocusEvent *ev)
{
	activated(false);
}

bool mapKey(QKeyEvent *ke,int &key, int &mod)
{
	mod |= SPECIALKEY;
	key = 0;
	switch(ke->key()){
		case 0x1005: key = 75315; break; // Keypad Enter
		case Qt::Key_Prior  : key = 75000; break;
		case Qt::Key_Next  : key = 75001; break;
		case Qt::Key_Home  : key = 75002; break;
		case Qt::Key_End  : key = 75003; break;
		case Qt::Key_Up  : key = 75004; break;
		case Qt::Key_Down  : key = 75005; break;
		case Qt::Key_Left  : key = 75006; break;
		case Qt::Key_Right  : key = 75007; break;
		case Qt::Key_Insert  : key = 75008; break;
		case Qt::Key_Return  : key = 75009; break;
		case Qt::Key_Backtab:
		case Qt::Key_Tab  : key = 75010; break;
		case Qt::Key_Backspace  : key = 75011; break;
		case Qt::Key_Escape  :
#ifdef ZAURUS //Suppress CANCEL since it sends a CLOSE message and then the ESC key.
			return false;
#else
			key = 75012; break;
#endif
		case Qt::Key_Delete  : key = 75013; break;
		case Qt::Key_F11 :
		case Qt::Key_Menu  : key = 75014; break;
		case Qt::Key_F9 : key = 75001+0xc0; break;
		case Qt::Key_F10 : key = 75002+0xc0; break;
		case Qt::Key_F12 : key = 75003+0xc0; break;
		case Qt::Key_F13 : key = 75004+0xc0; break;
		//case Qt::Key_Enter
	}
	if (key == 0){
		if (ke->key() >= Qt::Key_F1 && ke->key() <= Qt::Key_F12)
			key = ke->key()-Qt::Key_F1+75192;
	}
	if (key == 0){
		mod &= ~SPECIALKEY;
		QString txt = ke->text();
		if (txt.length() != 0) key = QChar(txt[0]).unicode();
	}
	return key != 0;
}

bool processKey(QKeyEvent *ke,bool isPress)
{
	//printf("Key: %x %d\n",ke->key(),isPress);
	ke->ignore();
	int key = ke->key();
	int mod = getKeyModifiers(ke->state());
	if (mapKey(ke,key,mod)) keyEvent(key,mod,isPress);
	//printf("key: %d, mod: %x\n",key,mod);
}
void keyPressEvent(QKeyEvent *ke)
{
	processKey(ke,true);
}
void keyReleaseEvent(QKeyEvent *ke)
{
	processKey(ke,false);
}

virtual void setTitle(TCHAR *title) {topLevelWidget()->setCaption(title);}
virtual void create(int flags)
{
}//= 0;
void shown(bool vis);
virtual void close() {hide();}
virtual void showMaximized() {QWidget::showMaximized(); deleteWaiting();}
virtual void showMinimized() {QWidget::showMinimized(); deleteWaiting();}
virtual void show() {QWidget::show(); deleteWaiting();}
virtual void hide() {QWidget::hide();}
virtual void showNormal() {QWidget::showNormal(); deleteWaiting();}
virtual void setIcon(int icon)
{
	QWidget::setIcon(*(QPixmap*)icon);
};
virtual void toFront() {setActiveWindow(); raise();}
virtual bool isVisible() {return QWidget::isVisible();}
virtual class eweRGBImage *toEweRGB(class rect & area,class eweRGBImage *dest = NULL)
{
	return NULL;
}
class eweRGBImage *toEweRGB()
{
	class rect r;
	getClientRect(r);
	r.x = 0; r.y = 0;
	return toEweRGB(r);
}

virtual void create(TCHAR *title,class rect &r,int flags)
{
	create(flags);
	if (title != NULL) setTitle(title);
	setSize(r.width,r.height);
	setLocation(r.x,r.y);
	//painter = new QPainter(this);
}
void setEweWindowIcon()
{
#ifndef ZAURUS
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
					eweIcon = ei->createIcon(em,0);
				}
			}
			if (eweIcon != 0) setIcon(eweIcon);
	UNLOCKTHREAD
#endif
}
//###################################################
};
//###################################################

#define DITHER (Qt::AvoidDither)

typedef class qtImage* QtImage;

//###################################################
class qtPixelReadWriteBuffer : public pixelReadWriteBuffer{
//###################################################
QImage image;
public:
	qtPixelReadWriteBuffer(QPixmap &buf,class rect& r)// : image(buf.convertToImage())
	{
		QPixmap qpix(r.width, r.height);
		// Qt 3.3
		//copyBlt(&qpix,0,0,this,location.width,location.height);

		QPainter qp(&qpix);
		qp.drawPixmap(-r.x,-r.y,buf);
		qp.end();

		image = qpix.convertToImage();

		pixels = NULL;
		if (!image.isNull()){
			rowStride = image.bytesPerLine();
			hasAlpha = true;
			pixels = image.scanLine(0);//(uchar*)(image.scanLine(0)+(rowStride*r.y)+r.x);
		}
	}
	bool isValid() {return pixels != NULL;}
	void release()
	{
		delete this;
	}
//###################################################
};
//###################################################


//###################################################
class qtRgbSection : public rgbSection{
//###################################################
protected:
QImage image;
class rect source;
/*
private:
	void setup(QImage image, class rect source)
	{
		this->image = image;
		width = source.width;
		height = source.height;
	}
*/
public:
	//qtRgbSection() {}
	qtRgbSection(QPixmap &buf, class rect r)
	{
		//setup(map.convertToImage(),source);
		QPixmap qpix(r.width, r.height);
		// Qt 3.3
		//copyBlt(&qpix,0,0,this,location.width,location.height);

		QPainter qp(&qpix);
		qp.drawPixmap(-r.x,-r.y,buf);
		qp.end();

		image = qpix.convertToImage();

		pixels = NULL;
		if (!image.isNull()){
			rowStride = image.bytesPerLine();
			usesAlpha = true;
			pixels = (uint32*)image.scanLine(0);//(uchar*)(image.scanLine(0)+(rowStride*r.y)+r.x);
		}
	}
/*
virtual uint32* getScanLine(int line)
{
	return (uint32*)image.scanLine(line+source.y)+source.x;
}
*/
//###################################################
};
//###################################################

QBitmap* toMask(rgbSection *pixmap, rgbSection *mask, COLORREF* ref)
{
	int
		w = mask == NULL ? pixmap->width : mask->width,
		h = mask == NULL ? pixmap->height: mask->height;
	QBitmap* myMask = new QBitmap(w,h);
	QPainter qp;
	qp.begin(myMask);
	qp.fillRect(0,0,w,h,Qt::color1);
	qp.setPen(Qt::color0);
	if (mask != NULL){
		for (int y = 0; y<h; y++){
			uint32 *value = mask->getScanLine(y);
			for (int x = 0; x<w; x++)
				if ((value[x] & 0xffffff) == 0)
					qp.drawPoint(x,y);
		}
	}else{
		uint32 t1 = mapColor(*ref).toRGB() & 0xffffff, t2 = ref->toRGB() & 0xffffff;
		for (int y = 0; y<h; y++){
			uint32 *value = pixmap->getScanLine(y);
			for (int x = 0; x<w; x++)
				if ((value[x] & 0xffffff) == t1 || (value[x] & 0xffffff) == t2)
					qp.drawPoint(x,y);
		}
	}
	return myMask;
}

//###################################################
class qtImage : public eweImage , public QPixmap {
//###################################################

QBitmap* myMask;

public:
	qtImage(int width,int height,int options,bool fullCreate = false) : eweImage(width,height,options), QPixmap(width,height)
	{
		myMask = NULL;
	}
~qtImage()
{
	if (myMask != NULL) delete myMask;
}
int width() {return eweImage::width;}
int height() {return eweImage::height;}
virtual void setup(WObject obj){}

void setMask(QBitmap* mask)
{
	myMask = mask;
	QPixmap::setMask(*myMask);
}
virtual bool maskIsSet()
{
	return myMask != NULL;
}
virtual bool setMask(COLORREF *ref)
{
	if (myMask != NULL || ref == NULL) return;
	qtRgbSection rgb(*this,class rect(0,0,width(),height()));
	myMask = toMask(&rgb,NULL,ref);
	if (myMask != NULL) QPixmap::setMask(*myMask);
}
virtual bool setMask(eweImage* mask)
{
	if (myMask != NULL || mask == NULL) return;
	qtRgbSection rgb(*(QtImage)mask,class rect(0,0,mask->width,mask->height));
	myMask = toMask(NULL,&rgb,NULL);
	if (myMask != NULL) QPixmap::setMask(*myMask);
}

virtual void drawDirectly(eweGraphics *g,int x, int y, bool withMask);

virtual rgbSection* toRGB(bool forWriting)
{
	return new qtRgbSection(*this, class rect(0,0,width(),height()));
}

virtual void getSetPixels(uint32 *pixelData,class rect location,int isGet)
{
	unsigned char *alpha = getImageAlpha();
	int ww = width();
	if (!isGet){
		QImage qi(location.width, location.height,32);
		for (int i = 0; i<location.height; i++){
			uint32* d = pixelData+i*location.width;
			memcpy(qi.scanLine(i),d,location.width*sizeof(uint32));
			if (alpha){
				unsigned char* p = alpha+(location.y+i)*ww+location.x;
				for (int c = 0; c<location.width; c++, d++, p++)
					*p = (unsigned char)((*d >> 24)& 0xff);
			}
		}
		QPixmap qpix(location.width,location.height);
		qpix.convertFromImage(qi);
		// Qt 3.3
		//copyBlt(this,location.x,location.y,&qp,0,0,location.width,location.height);

		QPainter qp(this);
		qp.drawPixmap(location.x,location.y,qpix);
		qp.end();

	}else{
		QPixmap qpix(location.width, location.height);
		// Qt 3.3
		//copyBlt(&qpix,0,0,this,location.width,location.height);

		QPainter qp(&qpix);
		qp.drawPixmap(-location.x,-location.y,*this);
		qp.end();

		QImage qi = qpix.convertToImage();
		for (int i = 0; i<location.height; i++){
			uint32* d = pixelData+i*location.width;
			memcpy(d,qi.scanLine(i),location.width*sizeof(uint32));
			if (alpha){
				unsigned char* p = alpha+(location.y+i)*ww+location.x;
				for (int c = 0; c<location.width; c++, d++, p++)
					*d = (*d & 0xffffff)|((uint32)*p << 24);
			}
		}

	}
}

virtual void destroy() {}
virtual void* getDrawable() {return (QPaintDevice*)this;}

virtual void writeEweRGBToNative(eweRGBImage &data,int destX,int destY,class rect *destClip = NULL)
{
	::writeEweRGB(this,data,destX,destY,destClip);
}

virtual PixelReadBuffer getPixelReadBuffer(class rect &area)
{
	qtPixelReadWriteBuffer* b = new qtPixelReadWriteBuffer(*this,area);
	if (b->isValid()) return b;
	b->release();
	return NULL;
}
virtual int createCursor(EweImage cmask,int hotX,int hotY)
{
	QtImage em = (QtImage)cmask;

	if (em == NULL){
		QCursor *qc = new QCursor(*this,hotX,hotY);
		return (int)qc;
	}else{
		QBitmap image, mask;
		image = *this;
		mask = *em;
		QCursor *qc = new QCursor(image,mask,hotX,hotY);
		return (int)qc;
	}

}
virtual int createIcon(EweImage mask,int options)
{
	QPixmap *icon = new QPixmap(*this);
	QPixmap *em = (QPixmap *)(QtImage)mask;
	if (em != NULL){
		QBitmap qb;
		qb = *em;
		icon->setMask(qb);
	}
	return (int)icon;
}

//###################################################
};
//###################################################

//###################################################
class qtImageMaker : public imageMaker{
//###################################################
QtImage image;
QImage qi;
public:
	qtImageMaker(QtImage i) {image = i;}
	unsigned char *getBits()
	{
		qi  = image->convertToImage();
		return (unsigned char *)qi.scanLine(0);
	}
	void releaseBits(unsigned char *bits,int writeBack = 1)
	{
		//printf("Releasing %i: %i %i\n",writeBack,image->width(),image->height());
		if (writeBack) image->convertFromImage(qi,DITHER);
	}
	void setScanLine(unsigned char *pixels,int bytesPerPixel,int row,int firstX,int xStep){}
	int getBytesPerLine()	{return 4*image->width();}
	int getBitsPerPixel() {return 32;}
	unsigned char *getAlpha()
	{
		if (image != NULL) {
			unsigned char *got = image->getImageAlpha();
			if (got != NULL){
				unsigned char *copy = new unsigned char[image->width()*image->height()];
				memcpy(copy,got,image->width()*image->height());
				got = copy;
			}
			return got;
		}
		return NULL;
	}
	virtual void releaseAlpha(unsigned char *alpha)
	{
		if (alpha == NULL) return;
		unsigned char *got = image->getImageAlpha();
		if (got != NULL)
			memcpy(got,alpha,image->width()*image->height());
		delete alpha;
	}
//###################################################
};
//###################################################

static QList<qtWindow> waitingToDelete;

static void deleteWaiting()
{
	while(waitingToDelete.first() != NULL){
		qtWindow *ew = waitingToDelete.take(0);
		//printf("Deleting!\n");
		if (ew == NULL) break;
		delete ew;
	}
}

typedef class qtWindow *QtWindow;
typedef class eweApplication *EweApplication;
typedef class pendingEvent *PendingEvent;

class pendingEvent
{
	public:
	bool isResize;
	int width, height;
	QEvent *event;
	QObject *dest;
	pendingEvent(QEvent *ev,QObject *obj)
	{
		isResize = false;
		event = ev; dest = obj;
	}
	pendingEvent(int wd,int hg,QObject *obj)
	{
		isResize = true;
		width = wd;
		height = hg;
		event = NULL;
		dest = obj;
	}
};

//###################################################
class eweApplication : public APP_BASE
//###################################################
{
	Q_OBJECT
	public slots:
  void tick();
	int tickIteration();
	public:
	QTimer timer;

	static QList<pendingEvent> waitingEvents;
	static eweApplication *mainApplication;
	static bool isLocked;
	static bool grab()
	{
		if (isLocked) return false;
		isLocked = true;
		return true;
	}
	static void release()
	{
		isLocked = false;
	}
	static void doTick()
	{
		mainApplication->tick();
	}
	int MaxTime;
	eweApplication(int & argc,char **argv) : APP_BASE(argc,argv) ,timer(this), MaxTime(100)
	{
		mainApplication = this;
		grab();
	}

	void wakeupAfter(int millis)
	{
		LOCKTHREAD
		PULSEEVENT
		if (millis == INFINITE) millis = 10000; //10 Second max.
		if (millis < 0) millis = 0;
		if (millis > MaxTime) millis = MaxTime;
		timer.changeInterval(millis);
		UNLOCKTHREAD
	}

	bool notify(QObject *rx,QEvent *ev)
	{
		bool ret = APP_BASE::notify(rx,ev);
		//wakeupAfter(0);
		return ret;
	}
	void wakeup()
	{
		wakeupAfter(0);
	}
	void mainLoop()
	{
		release();
		connect(&timer,SIGNAL(timeout()),this,SLOT(tick()));
		timer.start(100);
		wakeup();
		//printf("Calling exec()\n");
		exec();
	}
	static QtWindow mainWindow;
	static bool mainWindowIsVisible;

void post(QEvent *ev,QObject *dest)
{
	waitingEvents.append(new pendingEvent(ev,dest));
	wakeup();
}
void post(int w, int h, QObject *dest)
{
	waitingEvents.append(new pendingEvent(w,h,dest));
	wakeup();
}
void closing(QObject *obj)
{
	QList<pendingEvent> toRemove;
	for (PendingEvent p = waitingEvents.first(); p != NULL; p = waitingEvents.next())
		if (p->dest == obj) toRemove.append(p);
	while (toRemove.first() != NULL){
		PendingEvent p = toRemove.take(0);
		waitingEvents.remove(p);
		delete p->event;
		delete p;
	}
}
//###################################################
};
//###################################################

void qtWindow::requestDelete()
{
	waitingToDelete.append(this);
	hide();
}
void qtWindow::focusInEvent(QFocusEvent *ev)
{

// Under Qtopia, pressing the icon will bring the mainApp to the front.
// If mApp is not supposed to be visible, this will hide it and bring the
// true front window to the front.
	if (this == eweApplication::mainWindow){
		if (!eweApplication::mainWindowIsVisible){
			if (preparePost(54))
				doPost();
			hide();
			return;
		}
	}
	activated(true);
}

//###################################################
class qtGraphics : public eweGraphics{
//###################################################
public:
QPaintDevice* paint;
virtual void free()
{
	//if (painter) delete painter;
	//painter = NULL;
}
virtual void destroy() {free();}

virtual void setupDrawable(void *d)
{
	paint = (QPaintDevice *)d;
	if (d != NULL) {
		//gc = gdk_gc_new(drawable);
		//gdk_gc_set_line_attributes(gc,1,GDK_LINE_SOLID,LINECAP,GDK_JOIN_MITER);
		setFont(defFont); //FIXME - default font
	}
}
QColor toQColor(class colorref cr)
{
	return QColor(cr.red,cr.green,cr.blue);
}
Qt::PenStyle toStyle(int penStyle)
{
	switch(penStyle){
	case 1: return Qt::DashLine;
	case 2: return Qt::DotLine;
	case 3: return Qt::DashDotLine;
	case 4: return Qt::DashDotDotLine;
	case 0:
	default:
		return Qt::SolidLine;

	}
}
Qt::RasterOp toRasterOp(int drawOp)
{
	switch(drawOp){
		case DRAW_XOR:
			return Qt::NotXorROP;
/*
			op = (Qt::RasterOp)(CopyROP+lastRop);
			if (numOp == 10 || true) {
				numOp = 0;
				if (op == LastROP) lastRop = 0;
				else lastRop++;
				printf("Now set to: %d\n",op);
			}else numOp++;
*/
		case DRAW_AND:
			return Qt::OrROP;
		case DRAW_OR:
			return Qt::AndROP;
		case DRAW_ALPHA:
			return Qt::SetROP;
		default: // including DRAW_OVER
			return Qt::CopyROP;
	}
}
#define PENCAP Qt::SquareCap

bool begin(QPainter& p, bool setTheBrush = false)
{
	if (paint != NULL){
		if (!p.begin(paint)) return false;
		p.translate((double)tx,(double)ty);
		class graphics_pen* cp = getCurPen();
		if (cp == NULL) {
			//printf("No pen!\n");
			QPen qp(toQColor(curColor),1,Qt::SolidLine);
			qp.setCapStyle(PENCAP);
			p.setPen(Qt::NoPen);
		}else{
			QPen qp(toQColor(cp->color),cp->thickness,toStyle(cp->style));
			qp.setCapStyle(PENCAP);
			p.setPen(qp);
		}
		if (clip != NULL)
			p.setClipRect(clip->x,clip->y,clip->width,clip->height);
		p.setRasterOp(toRasterOp(drawOp));
		if (setTheBrush) {
			class graphics_brush* cb = getCurBrush();
			if (cb != NULL){
				QBrush qb(toQColor(cb->color));
				p.setBrush(qb);
			}
		}
		if (curFont != NULL) p.setFont(*(QFont*)curFont.nativeFont);
		return true;
	}
	return false;
}
void end(QPainter& p)
{
	p.end();
}

virtual void doTranslate(int x,int y){}
virtual void doSetDrawOp(int op){}
virtual void doSetPen(PEN *p) {}
virtual void doSetBrush(BRUSH *b) {}
virtual void doSetFont(FONT *f) {}
virtual void drawLine(int x1,int y1,int x2,int y2)
{
	QPainter p;
	if (!begin(p)) return;
	p.drawLine(x1,y1,x2,y2);
	end(p);
}
virtual void doSetForeground(COLORREF c) {} // = 0;
virtual int getTextWidth(WCHAR *chars,int count)
{
	if (curFont == NULL) return;
	QFontMetrics qfm(*(QFont*)curFont.nativeFont);
	QString str;
	str.setUnicodeCodes((const ushort *)chars,(unsigned int)count);
	return qfm.width(str);
}
virtual void drawText(int x,int y,WCHAR *chars,int count)
{
	if (curFont == NULL) return;
	QPainter p;
	if (!begin(p)) return;
	QString qs;
	qs.setUnicodeCodes(chars,count);
	p.drawText(x,y+curFont.ascent,qs);
	end(p);
}
virtual void drawRect(int x,int y,int width,int height,int fill = 0)
{
	QPainter p;
	if (!begin(p,fill)) return;
	p.drawRect(x,y,width,height);
	end(p);
}
virtual void drawEllipse(int x,int y,int width,int height,int fill = 0)
{
	QPainter p;
	if (!begin(p,fill)) return;
	p.drawEllipse(x,y,width,height);
	end(p);
}
virtual void fillPolygon(int *x,int *y,int length)
{
	QPointArray points(length);
	for (int i = 0; i<length; i++)
		points.setPoint(i,*x++,*y++);
	QPainter p;
	if (!begin(p,true)) return;
	p.drawPolygon(points);
	end(p);
}
virtual void doSetClip(rect & r){}
virtual void doClearClip(){}

virtual void drawEweRGBImage(EweRGBImage ei,int x,int y)
{
	::writeEweRGB(paint,*ei,x+tx,y+ty,NULL);
}
void drawImage(qtImage *image, int x, int y)
{
	QPainter p;
	if (!begin(p)) return;
	p.drawPixmap(x,y,*image);
	end(p);
}
void drawImage(QBitmap *image, int x, int y)
{
	QPainter p;
	if (!begin(p)) return;
	p.drawPixmap(x,y,*image);
	end(p);
}
virtual void drawEweImage(EweImage image,int x, int y)
{
	drawImage((qtImage *)image,x,y);
}
//virtual void drawImage(EweImage image,EweImage mask,COLORREF *transparent,rect location);

//###################################################
};
//###################################################
typedef class qtGraphics* QtGraphics;
void qtImage::drawDirectly(eweGraphics *g,int x, int y, bool withMask)
{
	if (!withMask && myMask != NULL) QPixmap::setMask(QBitmap(0,0));
	((qtGraphics *)g)->drawImage(this,x,y);
	if (!withMask && myMask != NULL) QPixmap::setMask(*myMask);
}
/*
//###################################################
class qtCapture : public qtRgbSection {
//###################################################
public:
	QtGraphics from;
	QRect r;
	QPoint saved;
	bool isValid;
	qtCapture(QtGraphics g,int x,int y,int w,int h)
	{
		isValid = true;
		from = g;
		if (g->surfaceType != SURF_IMAGE){
			isValid = false;
			printf("Cannot capture from screen!\n");
			return;
		}
		QPixmap tpm(w,h);
		//
		//printf("Want W: %d, H: %d Got W: %d, H: %d\n",w,h,tpm->width(),tpm->height());
		//
		saved.setX(0); saved.setY(0);
		if (x+g->tx < 0) {
			int dx = x+g->tx;
			x = -g->tx; w+=dx;
			saved.setX(-dx);
		}
		if (y+g->ty < 0){
			int dy = y+g->ty;
			y = -g->ty; h+=dy;
			saved.setY(-dy);
		}
		bitBlt(&tpm,saved.x(),saved.y(),g->paint,x+g->tx,y+g->ty,w,h);
		setup(tpm.convertToImage(),class rect(0,0,w,h));
		//printf("%x Image size: %d, %d\n",tpm,image.width(),image.height());
		r.setRect(x,y,w,h);
		r.moveBy(from->tx,from->ty);
		if (from->clip != NULL){
			QRect onScreen;
			set(onScreen,*from->clip);
			QRect inter = onScreen.intersect(r);
			isValid = inter.isValid();
		}
	}
	bool restore()
	{
		QPainter qp(from->paint);
		if (from->clip == NULL)
			qp.drawImage(r.x(),r.y(),image,saved.x(),saved.y(),r.width(),r.height());
			//bitBlt(from->getSurface(),r.x(),r.y(),tpm,saved.x(),saved.y(),r.width(),r.height());
		else{
			QRect onScreen;
			set(onScreen,*from->clip);
			QRect inter = onScreen.intersect(r);
			if (inter.isValid())
				//bitBlt(from->getSurface(),inter.x(),inter.y(),tpm,inter.x()-r.x()+saved.x(),inter.y()-r.y()+saved.y(),inter.width(),inter.height());
				qp.drawImage(inter.x(),inter.y(),image,inter.x()-r.x()+saved.x(),inter.y()-r.y()+saved.y(),inter.width(),inter.height());
		}
		qp.end();
		return true;
	}

//###################################################
};
//###################################################
rgbSection* qtGraphics::capture(class rect location)
{
	//return new qtCapture(this,location.x,location.y,location.width,location.height);
	return new defaultCapture(this,location.x,location.y,location.width,location.height);
}
*/

/*
void qtGraphics::drawImage(eweImage *im,eweImage *msk,COLORREF *color,class rect loc)
{
	QtGraphics g = this;
	qtImage *image = (qtImage *)im, *mask = (qtImage *)msk;
	unsigned char *alpha = image->getImageAlpha();
	QRect location;
	set(location,loc);
	int x = loc.x, y = loc.y, w = loc.width, h = loc.height;
	int sx = 0, sy = 0, sw = w, sh = h;
	if (image == NULL || w <= 0 || h <= 0) return;
	if (g->clip){
		QRect lt = location, clp;
		set(clp, *g->clip);
		lt.moveBy(g->tx,g->ty);
		lt = lt.intersect(clp);
		lt.moveBy(-g->tx,-g->ty);
		sx = lt.x()-location.x();
		sy = lt.y()-location.y();
		sw = lt.width();
		sh = lt.height();
		//printf("Y: %d Now: %d %d %d %d\n",loc.y,sx,sy,sw,sh);
	}
		if (color != NULL && !image->maskIsSet()){
			if (!image->setMask(color)){ // Have to mask it ourselves.
			}
		}
		if (mask != NULL && !image->maskIsSet()) {
			if (!image->setMask(mask)){ // Have to mask it ourselves.
			}
		}
		if (drawOp == DRAW_OVER || color != NULL){
			image->drawDirectly(this,x,y,color != NULL || mask != NULL);
		}else{
			qtCapture dest(g,x,y,w,h);
			if (dest.isValid){
				qtRgbSection iImage(*image,class rect(0,0,im->width,im->height));
				//printf("Now: %d %d %d %d\n",im->width,im->height,sw,sh);
				for (int r = 0; r<sh; r++){
					uint32 *lTemp = dest.getScanLine(r+sy)+sx;
					uint32 *lImage = iImage.getScanLine(r+sy)+sx;
					unsigned char *al = (alpha == NULL ? NULL : alpha+r*image->width());
				switch(g->drawOp){
				case DRAW_XOR:
					for (int c = 0; c<sw; c++, lTemp++)
						*lTemp = (*lTemp) ^ (~(*lImage++) & 0xffffff);
					break;

				case DRAW_AND:
					for (int c = 0; c<sw; c++, lTemp++)
						*lTemp = (*lTemp)  | (*lImage++ & 0xffffff);
					break;

				case DRAW_OR:
					for (int c = 0; c<sw; c++, lTemp++)
						*lTemp = (*lTemp)  & (*lImage++ & 0xffffff);
					break;
				case DRAW_ALPHA:
					for (int c = 0; c<sw; c++, lTemp++){
						int d = *lTemp & 0xffffff, s = *lImage++;
						int as = al == NULL ? 0xff : ((int)*al++) & 0xff,
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
            			}//End case
				}//End for(r...
				dest.restore();
			}// (isValid
		}//else
	if (g->drawOp == DRAW_ALPHA) g->setDrawOp(DRAW_OVER);
}
*/
#include "qtEweApplication.cpp"
QList<pendingEvent> eweApplication::waitingEvents;
QtWindow eweApplication::mainWindow = NULL;
EweApplication eweApplication::mainApplication = NULL;
bool eweApplication::isLocked = false;
bool eweApplication::mainWindowIsVisible = false;

void qtWindow::shown(bool vis)
{
	if (this == eweApplication::mainWindow)
		eweApplication::mainWindowIsVisible = vis;
}

static bool inTick = false;
static EweApplication MainApp = NULL;

int eweApplication::tickIteration()
{
	if (inTick) return;
	inTick = true;
		while (waitingEvents.first() != NULL){
			PendingEvent ev = waitingEvents.take(0);
			if (ev->isResize)
				((QtWindow)ev->dest)->postResize(ev->width,ev->height);
			else
				postEvent(ev->dest,ev->event);
			delete ev;
		}
		inTick = false;
		grab();
		int wait = NormalEweIteration();
		release();
	//printf("Ewe Iteration Wait: %d\n",wait);
		wakeupAfter(wait);
		return wait;
}
void eweApplication::tick()
{
	tickIteration();
}


//###################################################
class defaultEweSystem : public eweSystem {
//###################################################
public:
virtual int getCursor(int which)
{
	int ret = 0;
	switch(which){
		case 1: ret = (int)&Qt::waitCursor; break;
		case 2: ret = (int)&Qt::ibeamCursor; break;
		//case 3: ret = (int)&Qt::pointingHandCursor; break;
		case 4: ret = (int)&Qt::crossCursor; break;
		case 5: ret = (int)&Qt::sizeHorCursor; break;
		case 6: ret = (int)&Qt::sizeVerCursor; break;
		case 13: ret = (int)&Qt::sizeAllCursor; break;
		case 14: ret = (int)&Qt::sizeFDiagCursor; break;
		case 15: ret = (int)&Qt::blankCursor; break;
	}
	return ret;
}
virtual EweWindow getNewWindow() { return new qtWindow();}
virtual class eweFontMetrics *getNewFontMetrics() { return new qtFontMetrics();}
virtual class eweImage *getNewImage(int width,int height,int options,bool fullCreate = false)
	{ return new qtImage(width,height,options,fullCreate);}
virtual class eweGraphics *getNewGraphics() { return new qtGraphics();}
virtual ImageMaker getNewImageMaker(class eweImage *image)  { return new qtImageMaker((class qtImage*)image);}
virtual FONT createFont(class graphics_font &f)
{
		FONT ret;
		ret.nativeFont = loadFont(f,ret.ascent,ret.descent,ret.leading);
		if (defFont == NULL) defFont = ret;
		return ret;
}
virtual bool getTrueScreenRect(rect &r)
{
	r.x = 0; r.y = 0;
	r.width = 640; r.height = 480;
	getTrueScreenDim(r.width,r.height);
	return true;
}
virtual bool getGuiInfo(int which, WObject source, WObject dest, int options, Var &v)
{
	switch(which){
		case INFO_GUI_FLAGS:
			if (dest != 0)
#ifdef ZAURUS
				setLong(dest,GUI_FLAGS_REVERSE_OK_CANCEL);
#else
				setLong(dest,0);
#endif
			v.obj = dest;
			return true;
	}
	return false;
}
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
virtual void setTimerInterval(int time)
{
	eweApplication::mainApplication->wakeupAfter(time);
}
//
// Cause a timer event to be generated immediately.
//
virtual void forceTimerEvent()
{
	eweApplication::mainApplication->wakeup();
}
virtual class eweWindow *getParent(class eweWindow *win)
{
	return NULL;
}
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
virtual void setMainWindow(class eweWindow *ew,int flags)
{
	QtWindow q = (QtWindow)ew;
	eweApplication::mainWindow = q;
	eweApplication::mainApplication->setMainWidget(q);
	eweApplication::mainWindowIsVisible = (flags & FLAG_IS_VISIBLE) != 0;
}
//
// This returns an array of font names.
// Each name must be a new copy of a string, since each will be deleted after.
//
virtual TCHAR **listFonts(WObject surface,int &num)
{
	TCHAR **all = new TCHAR *[3];
	all[0] = copyString("Helvetica");
	all[1] = copyString("Courier");
	all[2] = copyString("Times");
	num = 3;
	return all;
}
/*
Since we use EXTERNAL_START_APP this method will not get called.
*/
virtual int doGuiLoop()
{
	printf("Error: doGuiLoop() should not be called.");
/*
	if (MainApp == NULL){
		char** argv = NULL;
		int argc = 0;
		printf("MainApp is NULL!\n");
		MainApp = new eweApplication(argc,argv);//ac,av2);
		//MainApp = new eweApplication(argc,argv);
	}
	printf("Looping...\n");
	MainApp->mainLoop();
	delete MainApp;
*/
}
//====================================================

//###################################################
};
//###################################################

// TODO: Inherit from eweSystem and override the necessary methods. Then replace "eweSystem" with the name of
// the new class.

static defaultEweSystem defaultSystem;
EweSystem ewe = &defaultSystem;

#define USE_EXTERNAL_START_APP
#define USE_EXTERNAL_EWE_ITERATION

static int EweIteration()
{
	return eweApplication::mainApplication->tickIteration();
}

static void *MainLoopThread(void *data)
{
	MainLoop();
}

int EweStartApp(int argc,char **argv)
{
	int alreadyRunning;
	int maxSize = 0, i;
	char *nc = "";
	for (i = 1; i<argc; i++){
		maxSize += strlen(argv[i])+1;
	}
	nc = (char *)malloc(maxSize+1);
	*nc = 0;
	char **av2 = new char *[1];
	int ac = 1;
	av2[0] = argv[0];
	for (i = 1; i<argc; i++){
			if (strlen(argv[i]) > 10)
				if (strncmp(argv[i],"-XappName=",10) == 0){
					av2[0] = new char[strlen(argv[i])-9];
					strcpy(av2[0],argv[i]+10);
					continue;
				}
		strcat(nc,argv[i]);
		strcat(nc," ");
	}
#ifdef ZAURUS
VmFlags |= VM_FLAG_IS_MOBILE|VM_FLAG_IS_SLOW_MACHINE|VM_FLAG_NO_KEYBOARD|VM_FLAG_NO_MOUSE_POINTER|VM_FLAG_IS_LOW_MEMORY;
#endif

#ifdef WINCE
#ifdef HPC
	VmFlags |= VM_FLAG_IS_MOBILE|VM_FLAG_IS_SLOW_MACHINE|VM_FLAG_NO_MOUSE_POINTER;
#else
	VmFlags |= VM_FLAG_IS_MOBILE|VM_FLAG_IS_SLOW_MACHINE|VM_FLAG_NO_KEYBOARD|VM_FLAG_NO_MOUSE_POINTER;
#if _WIN32_WCE >= 300 && !defined(CASIOBE300)
	VmFlags |= VM_FLAG_SIP_BUTTON_ON_SCREEN;
#endif
#endif
#endif

#ifdef USE_LOG
	Log("---\n",4);
#endif
	if (MainApp == NULL){
		MainApp = new eweApplication(ac,av2);
		//MainApp = new eweApplication(argc,argv);
	}
	mainWinObj = startApp(nc, &alreadyRunning,0);
	if (mainWinObj == 0) {
		if (vmStatus.errNum <= 0) debugString("Unknown VM error!");
		else debugString(errorMessages[vmStatus.errNum-1]);
		return -1;
	}
	//QTimer::singleShot(1000,MainApp,SLOT(tick()));
	//MainApp->exec();
	//printf("Main looping...\n");

#ifdef USE_PTHREADS
#ifdef USE_BACKGROUND_THREAD
// Cannot do this, background threads cannot call QtMethods.
	{
		//pthread_t t;
		//pthread_create(&t,NULL,MainLoopThread,NULL);
	}
#endif
#endif
	MainApp->mainLoop();
	delete MainApp;
}

static void wakeupVM()
{
	eweApplication::mainApplication->wakeup();
}

int CommandLineParsed(char *className)
{
	return 1;
}

static void cancelHoldDown()
{
	static WClass *controlClass = NULL;
	static WClassMethod *cancelMethod = NULL;

	if (controlClass == NULL){
		controlClass = tryGetClass(createUtfString("ewe/ui/Control"));
		if (controlClass != NULL)
			cancelMethod = getMethod(controlClass,createUtfString("cancelHoldDown"),createUtfString("()V"),NULL);
	}
	if (cancelMethod != NULL){
		Var pars[1];
		executeMethod(controlClass,cancelMethod,pars,1);
		//printf("Hold cancelled!\n");
	}
}

#ifdef ZAURUS
#define VmSetSIP_defined
static int SipLocked = 0, SipFrozen = 0;
static int sipOn = 0;

static Var VmGetSIP(Var stack[])
{
	Var v;
	v.intValue = sipOn;
	return v;
}
static Var VmSetSIP(Var stack[])
{
	if (!UseSip) return returnVar(0);
	int par = stack[0].intValue;
/*
	if (stack[1].obj != 0) {
		windowFlags = WOBJ_WindowFlags(stack[1].obj);
		hw = WOBJ_WindowHWnd(stack[1].obj);
	}
*/
	//hw = curHWnd;
	if (SipFrozen & !(par & 0x10)) return returnVar(0);
	if (par & 0x8) {
		SipLocked = 0;
		SipFrozen = 1;
	}else if (par & 0x10){
		SipFrozen = SipLocked = 0;
		return returnVar(0);
	}
	if (!(par & 3)) {//Requesting to switch it off.
		if (!(par & 4) && SipLocked) return returnVar(0);
		SipLocked = 0;
		if (true/*sip.fdwFlags & SIPF_ON */) {
			//Global::hideInputMethod();
		}
	}else{ //Requesting to switch it on.
		SipLocked |= (par & 4);
		if (par & 1){
			Global::showInputMethod();
			cancelHoldDown();
		}
	}
	return returnVar(0);
}
#endif //ZAURUS
#define USE_EXTRA_GET_PROPERTY
static TCHAR *extraGetProperty(TCHAR *text)
{
	if (strcmp(text,"DOCUMENTS_DIR") == 0){
#ifdef ZAURUS
		return "/home/root/Documents";
/*
		QString d = QPEApplication::documentDir();
		if (d.isNull()) return NULL;
		return (TCHAR *)d.latin1();
*/
#else
		return "/home/root";
#endif
	}
	return NULL;
}
#ifdef ZAURUS
static void doBeep(int which)
{
	QApplication::beep();
	//CUSTOM_BUZZER(which); - this doesn't appear to work.
}
#endif

static Var SoundBeep(Var stack[])
{
#ifdef ZAURUS
	doBeep(SHARP_PDA_ERRORSOUND);
#else
	QApplication::beep();
#endif
	return returnVar(1);
}
/*
public static final int MB_OK                       = 0x00000000;
public static final int MB_ICONHAND                 = 0x00000010;
public static final int MB_ICONQUESTION             = 0x00000020;
public static final int MB_ICONEXCLAMATION          = 0x00000030;
public static final int MB_ICONASTERISK             = 0x00000040;
*/
static Var SoundBeep2(Var stack[])
{
	int val = stack[0].intValue;
#ifdef ZAURUS
	switch(val){
	case 0: val = SHARP_PDA_WARNSOUND; break;
	case 0x10: val = SHARP_PDA_WARNSOUND; break;
	case 0x20: val = SHARP_PDA_WARNSOUND; break;
	case 0x30: val = SHARP_PDA_CRITICALSOUND; break;
	case 0x40: val = SHARP_PDA_CRITICALSOUND; break;
	default:
		val = SHARP_PDA_ERRORSOUND;
		break;
	}
	doBeep(val);
#else
	QApplication::beep();
#endif
	return returnVar(1);
}
#define WOBJ_SoundNative(OBJ) objectPtr(OBJ)[3].refValue

static Var SoundClipPlay(Var stack[])
{
	QSound *qs = (QSound *)WOBJ_SoundNative(stack[0].obj);
	if (qs != NULL){
		qs->play();
	}
	return returnVar(0);
}

static Var SoundClipStop(Var stack[])
{
	return returnVar(0);
}

static Var SoundClipFree(Var stack[])
{
	QSound *qs = (QSound *)WOBJ_SoundNative(stack[0].obj);
	if (qs != NULL){
		delete qs;
		WOBJ_SoundNative(stack[0].obj) = NULL;
	}
	return returnVar(1);
}
static Var SoundClipInit(Var stack[])
{
	QString got = newQString(stack[1].obj);
	if (got.isNull()) return returnVar(0);
	QSound *qs = new QSound(got);
	WOBJ_SoundNative(stack[0].obj) = qs;
	return returnVar(1);
}
static Var VmGetClipboardText(Var stack[])
{
	Var v;
	v.obj = stack[0].obj;
#ifndef ZAURUS //Bug in Zaurus clipboard() - crashes the program.
	QClipboard *c = QApplication::clipboard();
	if (c == NULL) return v;
	QString str = c->text();
	if (!str.isNull()) v.obj = newString(&str);
#endif
	return v;
}

static Var VmSetClipboardText(Var stack[])
{
	WObject obj = stack[0].obj;
#ifndef ZAURUS //Bug in Zaurus clipboard() - crashes the program.
	QClipboard *c = QApplication::clipboard();
	if (obj == 0) c->clear();
	c->setText(newQString(obj));
#endif
	return returnVar(1);
}
