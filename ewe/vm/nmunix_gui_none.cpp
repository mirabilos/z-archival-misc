__IDSTRING(rcsid_nmunix_gui_none, "$MirOS: contrib/hosted/ewe/vm/nmunix_gui_none.cpp,v 1.3 2008/04/11 00:27:24 tg Exp $");

/*
class guiWindow : public eweWindow {

	virtual void destroy() {}
	virtual void setup(WObject obj)
	{
		eweWindow::setup();
	}

};
*/


class defaultEweSystem : public eweSystem {

int timerInterval;
public:
	virtual int getScreenSize(int &width,int &height)
	{
		width = 640;
		height = 480;
		return 1;
	}

virtual EweWindow getNewWindow() { return NULL;}
virtual class eweFontMetrics *getNewFontMetrics() { return NULL;}
virtual class eweImage *getNewImage(int width,int height,int options,bool fullCreate = false) { return NULL;}
virtual class eweGraphics *getNewGraphics() { return NULL;}
virtual ImageMaker getNewImageMaker(class eweImage *image)  { return NULL;}
virtual FONT createFont(class graphics_font &f) {return NULL;}
virtual void setMainWindow(class eweWindow *window,int flags){}

virtual bool getTrueScreenRect(rect &r)
{
	r.x = 0; r.y = 0;
	r.width = 640; r.height = 480;
	return true;
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
	timerInterval = time;
}
//
// Cause a timer event to be generated immediately.
//
virtual void forceTimerEvent()
{
	interrupter.triggerInterrupt();
}
virtual class eweWindow *getParent(class eweWindow *win)
{
	return NULL;
}
virtual void getParentRect(class eweWindow *win,rect &r,bool doRotate)
{
	r.x = r.y = 0;
	r.width = 640; r.height = 480;
}
virtual void setMainWindow(class eweWindow *window)
{
}
//
// This returns an array of font names.
// Each name must be a new copy of a string, since each will be deleted after.
//
virtual TCHAR **listFonts(WObject surface,int &num)
{
	return NULL;
}
virtual int doGuiLoop()
{
//	printf("Looping...\n");
	while(true){
		tick();
		interrupter.waitWithSelect(timerInterval);
	}
}
//====================================================

};

// TODO: Inherit from eweSystem and override the necessary methods. Then replace "eweSystem" with the name of
// the new class.

static defaultEweSystem defaultSystem;
EweSystem ewe = &defaultSystem;
