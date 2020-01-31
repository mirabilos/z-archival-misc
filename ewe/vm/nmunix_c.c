/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.15, February 13, 2002                        *
 *  Copyright (c) 2007 Thorsten Glaser <tg@mirbsd.de>                            *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/

__IDSTRING(rcsid_nmunix_c, "$MirOS: contrib/hosted/ewe/vm/nmunix_c.c,v 1.7 2008/05/11 23:14:16 tg Exp $");

#ifdef ZAURUS
#define MOBILE
#endif

#ifndef NO_IR
#ifdef MOBILE
#define HAS_IR
#define LINUX
#endif
#endif

//
#define USE_FD_FOR_RAF
//
#ifdef USE_FD_FOR_RAF
#define RafileDestroy PipeDestroy
#endif

static void RafileDestroy(WObject obj);
static void WindowDestroy(WObject obj);
static void GraphicsDestroy(WObject obj);
static void FontDestroy(WObject obj);
static void FontMetricsDestroy(WObject obj);
static void ImageDestroy(WObject obj);
static void SocketDestroy(WObject obj);
static void ServerSocketDestroy(WObject obj);
static void SerialPortDestroy(WObject obj);

ClassHook classHooks[] =
{
#ifndef NO_NATIVES
	{ "ewe/ui/Window", WindowDestroy, 1 },
	{ "ewe/io/RandomAccessFile", RafileDestroy, 0 },
	{ "ewe/fx/Graphics", GraphicsDestroy, 1},
	{ "ewe/fx/FontMetrics", FontMetricsDestroy, 1},
	{ "ewe/fx/Font", FontDestroy, 1},
	{ "ewe/fx/Image", ImageDestroy, 1},
	{ "ewe/net/Socket", SocketDestroy, 4},
	{ "ewe/net/ServerSocket", ServerSocketDestroy, 4 },
	{ "ewe/net/DatagramSocket", ServerSocketDestroy, 4 },
	{ "ewe/io/SerialPort", SerialPortDestroy, 1},
#endif
	{ NULL, NULL, 0 }
};

typedef int (* getCharWidthFunc)(WObject fontMetrics,int character);
typedef int (* getTextWidthFunc)(WObject fontMetrics,uint16 *chars,int len);

// Graphics
//
// var[0] = Class
// var[1] = Surface
// var[2] = Pen //Waba object.
// var[3] = Brush //Waba object
// var[4] = Color //Waba object
// var[5] = Background //Waba object
// var[6] = hook var - native class

#define WOBJ_GraphicsSurface(o) (objectPtr(o))[1].obj
#define WOBJ_GraphicsPen(o) (objectPtr(o))[2].refValue
#define WOBJ_GraphicsBrush(o) (objectPtr(o))[3].refValue
#define WOBJ_GraphicsColor(o) (objectPtr(o))[4].refValue
#define WOBJ_GraphicsBackground(o) (objectPtr(o))[5].refValue

#define WOBJ_GraphicsNative(o) (objectPtr(o))[6].refValue

#define DRAW_OVER 1
#define DRAW_AND 2
#define DRAW_OR 3
#define DRAW_XOR 4
//
// Font
//
// var[0] = Class
// var[1] = String name
// var[2] = int size
// var[3] = int style
//
#define WOBJ_FontName(o) (objectPtr(o))[1].obj
#define WOBJ_FontStyle(o) (objectPtr(o))[2].intValue
#define WOBJ_FontSize(o) (objectPtr(o))[3].intValue
#define Font_PLAIN 0x0
#define Font_BOLD 0x1
#define Font_ITALIC 0x2
#define Font_UNDERLINE 0x4
#define Font_OUTLINE 0x8
#define Font_STRIKETHROUGH 0x10
#define Font_SUPERSCRIPT 0x80
#define Font_SUBSCRIPT 0x100

#define WOBJ_FontNative(o) (objectPtr(o))[4].refValue
//
// FontMetrics
//
// var[0] = Class
// var[1] = Font
// var[2] = Surface
// var[3] = int ascent
// var[4] = int descent
// var[5] = int leading
//

#define WOBJ_FontMetricsFont(o) (objectPtr(o))[1].obj
#define WOBJ_FontMetricsSurface(o) (objectPtr(o))[2].obj
#define WOBJ_FontMetricsAscent(o) (objectPtr(o))[3].intValue
#define WOBJ_FontMetricsDescent(o) (objectPtr(o))[4].intValue
#define WOBJ_FontMetricsLeading(o) (objectPtr(o))[5].intValue
#define WOBJ_FontMetricsNative(o) (objectPtr(o))[6].refValue

//
// Image
//
// var[0] = Class
// var[1] = width
// var[2] = height
//

#define WOBJ_ImageWidth(o) (objectPtr(o))[1].intValue
#define WOBJ_ImageHeight(o) (objectPtr(o))[2].intValue
#define WOBJ_ImageTransparent(o) (objectPtr(o))[3].intValue
#define WOBJ_ImageBackground(o) (objectPtr(o))[4].intValue
#define WOBJ_ImageHasAlpha(o) (objectPtr(o))[10].intValue
#define WOBJ_ImageWasLoaded(o) (objectPtr(o))[11].intValue
#define WOBJ_ImageAlphaChannel(o) (objectPtr(o))[12].intValue

#define WOBJ_ImageNative(o) (objectPtr(o))[13].refValue

static int g_mainWinWidth = 0;
static int g_mainWinHeight = 0;
static TCHAR g_windowTitle[50];
static WObject mainWin;

#ifdef USING_WIN32_API
static HWND mainHWnd = NULL, curHWnd = NULL;
static DWORD g_startTime = 0;
static DWORD g_messageTime = 0;
static HINSTANCE g_hInstance;
static TCHAR *pszWndClassName;//[] = TEXT("WabaWndClass");
static WNDCLASS mainWindowClass;
static HCURSOR normalCursor = 0;
#endif
//
// Rect
//
// var[0] = Class
// var[1] = int x
// var[2] = int y
// var[3] = int width
// var[4] = int height
#ifndef WOBJ_RectX
#define WOBJ_RectX(o) (objectPtr(o))[1].intValue
#define WOBJ_RectY(o) (objectPtr(o))[2].intValue
#define WOBJ_RectWidth(o) (objectPtr(o))[3].intValue
#define WOBJ_RectHeight(o) (objectPtr(o))[4].intValue
#endif
//
// Control
//
// var[0] = Class
// var[1] = int x
// var[2] = int y
// var[3] = int width
// var[4] = int height

#define WOBJ_ControlX(o) (objectPtr(o))[1].intValue
#define WOBJ_ControlY(o) (objectPtr(o))[2].intValue
#define WOBJ_ControlWidth(o) (objectPtr(o))[3].intValue
#define WOBJ_ControlHeight(o) (objectPtr(o))[4].intValue
#define WOBJ_ControlParent(o) (objectPtr(o))[5].obj
#define WOBJ_ControlNext(o) (objectPtr(o))[6].obj
#define WOBJ_ControlPrev(o) (objectPtr(o))[7].obj
#define WOBJ_ControlChildren(o) (objectPtr(o))[8].obj
#define WOBJ_ControlTail(o) (objectPtr(o))[9].obj
#define WOBJ_ControlModifiers(o) (objectPtr(o))[10].intValue
#define WOBJ_ControlDoPaintMethod(o) (objectPtr(0))[11].ref
#define WOBJ_ControlMyClass(o) (objectPtr(0))[12].ref
// MLB
// Color
//
// var[0] = Class
// var[1] = int red
// var[2] = int green
// var[3] = int blue
// var[4] = int alpha

#define WOBJ_ColorRed(o) (objectPtr(o))[1].intValue
#define WOBJ_ColorGreen(o) (objectPtr(o))[2].intValue
#define WOBJ_ColorBlue(o) (objectPtr(o))[3].intValue
#define WOBJ_ColorAlpha(o) (objectPtr(o))[4].intValue

//
// Window
//
// var[0] = Class
// var[n] = ...other locals...
// var[n + 1] = hook var - hWnd under Win32
//

// since Window inherits from other classes, we need to calculate the
// right base offset to start with when reading/writing to variables
static int _winHookOffset = -1;
#define WOBJ_WindowNative(OBJ) (objectPtr(OBJ))[_winHookOffset + 0].refValue
/*
#define WOBJ_WindowHWnd(o) (objectPtr(o))[_winHookOffset + 0].refValue
#define WOBJ_WindowFlags(o) (objectPtr(o))[_winHookOffset + 1].intValue
#define WOBJ_WindowLastModal(o) (objectPtr(o))[_winHookOffset + 2].refValue
*/
#define FLAG_HAS_CLOSE_BUTTON  0x1
#ifndef FLAG_IS_VISIBLE
#define FLAG_IS_VISIBLE  0x2
#endif
#define FLAG_IS_ICONIZED  0x4
#define FLAG_HAS_TITLE 0x8
#define FLAG_CAN_MAXIMIZE  0x10
#define FLAG_CAN_MINIMIZE  0x20
#define FLAG_CAN_RESIZE   0x40
#define FLAG_IS_MODAL   0x80
#define FLAG_DONT_CLEAR_BACKGROUND 0x100
#define FLAG_HAS_TASKBAR_ENTRY 0x200
#define FLAG_SHOW_SIP_BUTTON 0x400
#define FLAG_MAXIMIZE_ON_PDA 0x800
#define FLAG_MAXIMIZE 			 0x1000
#define FLAG_MINIMIZE FLAG_IS_ICONIZED
#define FLAG_MAIN_WINDOW_ROTATED 			 0x2000
#define FLAG_FULL_SCREEN 0x4000
#define FLAG_ALWAYS_ON_TOP 0x10000
#define FLAG_VISIBLE_ON_TO_FRONT 0x20000
#define FLAG_MAIN_WINDOW_COUNTER_ROTATED 0x40000

#define FLAG_RESTORE 0x8000
#define FLAG_STATE_KNOWN 0x8000


#define FLAG_IS_DEFAULT_SIZE 0x80000000

#define WINDOW_STATE (FLAG_MAXIMIZE|FLAG_MINIMIZE|FLAG_STATE_KNOWN)



#define SURF_WINDOW 1
#define SURF_IMAGE 2
#define SURF_PRINT_JOB 3

static WClass *windowClass = 0;
static WClass *imageClass = 0;
static WClass *printClass = 0;

static int SurfaceGetType(WObject surface)
	{
	WClass *wclass;

	if (surface == 0)
		return 0;

	// cache class pointers for performance
	if (!windowClass)
		windowClass = getClass(createUtfString("ewe/ui/Window"));
	if (!imageClass)
		imageClass = getClass(createUtfString("ewe/fx/Image"));
	if (!printClass)
		printClass = getClass(createUtfString("ewe/fx/print/PrinterJob"));

	wclass = WOBJ_class(surface);
	if (compatible(wclass, windowClass))
		return SURF_WINDOW;
	if (compatible(wclass, imageClass))
		return SURF_IMAGE;
	if (compatible(wclass, printClass))
		return SURF_PRINT_JOB;
	return 0;
	}


static int64 getWindowHandle(WObject obj)
{
	if (obj == 0) return 0;
	return (int64)(uintptr_t)WOBJ_WindowNative(obj);
}

//
// Time
//
// var[0] = Class
// var[1] = int year
// var[2] = int month
// var[3] = int day
// var[4] = int hour
// var[5] = int minute
// var[6] = int second
// var[7] = int millis
//

#define WOBJ_TimeYear(o) (objectPtr(o))[1].intValue
#define WOBJ_TimeMonth(o) (objectPtr(o))[2].intValue
#define WOBJ_TimeDay(o) (objectPtr(o))[3].intValue
#define WOBJ_TimeHour(o) (objectPtr(o))[4].intValue
#define WOBJ_TimeMinute(o) (objectPtr(o))[5].intValue
#define WOBJ_TimeSecond(o) (objectPtr(o))[6].intValue
#define WOBJ_TimeMillis(o) (objectPtr(o))[7].intValue
#define WOBJ_TimeDOW(o) (objectPtr(o))[8].intValue


/**
* Use this to get or set the main window (i.e. native application window) location
* and size on the screen. When calling getInfo you must provide a Rect object
* as the result destination. When calling setInfo you must provide a Rect object
* as the sourceParameter.
**/
#define INFO_WINDOW_RECT  1
/**
* Use this to get or set the main window (i.e. native application window) flags. This
* includes the FLAG_ values. When calling getInfo you must provide a Long object as
* the result destination. When calling setInfo you must provide a Long object as the
* sourceParameter.
**/
#define INFO_WINDOW_FLAGS  2
/**
* Use this to get or set the user's screen rectangle. When calling getInfo you must provide a Rect object
* as the result destination. When calling setInfo you must provide a Rect object
* as the sourceParameter.
**/
#define INFO_PARENT_RECT  3

#define INFO_CLIENT_RECT 4

#define INFO_FLAGS_FOR_SIZE 5

#define INFO_SCREEN_RECT 6
#define INFO_TASKBAR_ICON 7
#define INFO_WINDOW_ICON 8

#define OPTION_FLAG_SET  0x1
#define OPTION_FLAG_CLEAR  0x2

#define INFO_GUI_FLAGS 9
#define GUI_FLAGS_HAS_TASK_BAR 1
#define GUI_FLAGS_REVERSE_OK_CANCEL 2

#define INFO_WINDOW_TITLE 10
#define INFO_NATIVE_WINDOW 13

typedef class imageMaker *ImageMaker;
class imageMaker {
public:
	virtual unsigned char *getBits() = 0;
	virtual void releaseBits(unsigned char *bits,int writeBack = 1) = 0;
	virtual void setScanLine(unsigned char *pixels,int bytesPerPixel,int row,int firstX,int xStep) = 0;
	virtual int getBytesPerLine() = 0;
	virtual int getBitsPerPixel() = 0;
	virtual unsigned char *getAlpha() = 0;
	virtual void releaseAlpha(unsigned char *alpha) = 0;
};

static ImageMaker getImageMaker(WObject image);

static getCharWidthFunc setupGetCharWidth(WObject fontMetrics);
static void closeGetCharWidth(WObject fontMetrics);

static int UseSip = 0;
static int SimulateSip = 0;

#include "nmunix_gui.cpp"

//
// Platform independant ewe.io.File definitions
//

static int compareFiletime(time_t one,time_t two);
static time_t fromTime(WObject time);
static void toTime(WObject time,time_t tt);
/**
* This requests the icon for the file - returns an IImage object.
* sourceParameters - none, resultDestination - unused,
* options - INFO_ICON_SMALL, INFO_ICON_MEDIUM, INFO_ICON_LARGE.
**/
#define INFO_ICON  0x1
#define INFO_ICON_SMALL  0x1
#define INFO_ICON_MEDIUM  0x2
#define INFO_ICON_LARGE  0x3
/**
* This requests the names of the root directory of all drives - returns an array of Strings.
* sourceParameters - none, resultDestination - unused,
* options - none defined yet.
**/
#define INFO_ROOT_LIST  0x2
/**
* If this File is considered a link/shortcut to another file or directory, this will return
* the name of the target file/directory, otherwise it will return null.
* sourceParamerters - none, resultDestination - unused,
* options - none defined yet.
**/
#define INFO_LINK_DESTINATION  0x3

#define INFO_PROGRAM_DIRECTORY 0x4
#define INFO_TEMPORARY_DIRECTORY  0x5
#define INFO_OWNER 15
#define INFO_GROUP 17

//
// Platform dependant File access.
//
static char cwd[1025];

char *getCwd(void)
{
	getcwd(cwd,sizeof(cwd)-1);
	return cwd;
}

typedef struct FileListItemStruct
	{
	TCHAR *fileName;
	int fileLength;
	time_t ftLastWriteTime;
    int32 dwFileAttributes;
	int32 nFileSizeLow;
	struct FileListItemStruct *next;
	} FileListItem;

typedef struct file_sort_data{
	FileListItem **array;
	int options;
}*FileSortData;

static int doList(FileListItem **list,WObject file,WObject mask,int options);
static int getFileAttributes(FileListItem *dest,TCHAR *fullPath);
static void setModifiedTime(TCHAR *fullPath,time_t newModified);

#define LIST_DESCENDING  0x1
#define LIST_DIRECTORIES_LAST  0x2
#define LIST_DIRECTORIES_ONLY  0x4
#define LIST_FILES_ONLY  0x8
#define LIST_BY_DATE  0x10
#define LIST_BY_TYPE  0x20
#define LIST_DONT_SORT 0x40

#define LIST_IGNORE_DIRECTORY_STATUS 0x80
#define LIST_ALWAYS_LIST_DIRECTORIES 0x100
#define LIST_CHECK_FOR_ANY_MATCHING_CHILDREN 0x200
#define LIST_BY_SIZE 0x400

#ifndef IS_WINDOWS
#define FILE_ATTRIBUTE_DIRECTORY 0x1
#else
#include <direct.h>
#endif

static int compareTchars(LCID id,int options,TCHAR *strOne,int lenOne,TCHAR *strTwo,int lenTwo);

/**
* The full name of a month. Associated values must be in the range 1 to 12.
**/
#define MONTH  1
/**
* The short name of a month. Associated values must be in the range 1 to 12.
**/
#define SHORT_MONTH  2
/**
* The full name of a DAY IN THE WEEK. Associated values must be in the range 1 to 7.
* A value of 1 implies the first day of the week. This may be the equivalent of Monday
* or Sunday or Saturday(?).
**/
#define DAY_OF_WEEK  3
/**
* The short name of a DAY IN THE WEEK. Associated values must be in the range 1 to 7.
* A value of 1 implies the first day of the week. This may be the equivalent of Monday
* or Sunday or Saturday(?).
**/
#define SHORT_DAY_OF_WEEK  4
#define CURRENCY  5
/**
* The full name of the AM/PM. Associated values must be in the range 0 to 1.
**/
#define AM_PM  6
/**
* The full name of the day which is equivalent to Monday - Sunday, where 1  Monday, 2  Tuesday
* etc.
**/
#define DAY  7
/**
* The short name of the day which is equivalent to Monday - Sunday, where 1  Monday, 2  Tuesday
* etc.
**/
#define SHORT_DAY  8
/**
* The index of the first day of the week. 1  Monday, 2  Tuesday, etc.
**/
#define FIRST_DAY_OF_WEEK  9
/**
* The full name of the locale language.
**/
#define LANGUAGE  10
/**
* The ISO English name of the locale language.
**/
#define LANGUAGE_ENGLISH  11
/**
* The full native name of the locale language.
**/
#define LANGUAGE_NATIVE  12
/**
* The ISO short name of the locale language.
**/
#define LANGUAGE_SHORT  13
/**
* The character(s) for the time separator.
**/
#define TIME_SEPARATOR  14
/**
* The character(s) for the date separator.
**/
#define DATE_SEPARATOR  15
/**
* The format for the time display.
**/
#define TIME_FORMAT  16
/**
* The format for the short date display.
**/
#define SHORT_DATE_FORMAT  17
/**
* The format for the long date display.
**/
#define LONG_DATE_FORMAT  18
/**
* The correct format for display month and year only.
**/
#define MONTH_YEAR_FORMAT  19
/**
* The full name of the locale country.
**/
#define COUNTRY 20
/**
* The full English name of the locale country.
**/
#define COUNTRY_ENGLISH 21
/**
* The full native name of the locale country.
**/
#define COUNTRY_NATIVE 22
/**
* The short name of the locale country.
**/
#define COUNTRY_SHORT 23

/**
* Parameter for format() or parse() method, used when formating a numeric value.
**/
#define FORMAT_PARSE_NUMBER  0x10
/**
* Parameter for format() or parse() method, used when formating a currency value.
**/
#define FORMAT_PARSE_CURRENCY  0x20
/**
* Parameter for format() or parse() method, used when formating a date/time value.
**/
#define FORMAT_PARSE_DATE  0x30
/**
* Option for format() method when using FORMAT_NUMBER or FORMAT_CURRENCY.
* It specifies that values after the decimal point should not be displayed.
**/
//#define NO_DECIMAL_VALUE  0x100
/**
* Option for format() method when using FORMAT_NUMBER or FORMAT_CURRENCY.
* It specifies that values before the decimal point should not be grouped.
* e.g. in English, display 1000000 instead of 1,000,000
**/
//#define NO_GROUPINGS  0x20
/**
* Option for format() method when using  FORMAT_CURRENCY.
* It specifies that currency symbol should not be displayed.
**/
//#define NO_CURRENCY_SYMBOL  0x40
/**
* Option for format() method when using  FORMAT_PARSE_CURRENCY or FORMAT_PARSE_NUMBER
* It specifies not to round up the last decimal digit. This option cannot be placed
* in the string format specifier.
**/
#define NO_ROUNDING 0x100
/**
* Option for format() method when using  FORMAT_CURRENCY.
* It specifies that a standard numeric negative notation should
* be used instead of the currency negative notation.
**/
//#define NO_CURRENCY_NEGATIVE_NOTATION  0x100
#ifndef VCC
#define NORM_IGNORECASE 1
#endif

static TCHAR dontKnow[] = TEXT("???");
static TCHAR info[256];

//
// Platform independant File functions
//
// var[0] = Class
// var[1] = String full path name. <= This is not a hook variable, it is declared in ewe.io.FileBase

#define WOBJ_FileName(o) (objectPtr(o))[1].obj
//
int sortDirectory(SortInfo info,int one,int two,int *error)
{
	FileSortData fsd = (FileSortData)info->functionData;
	FileListItem *f1 = fsd->array[one], *f2 = fsd->array[two];

	if (!(fsd->options & LIST_IGNORE_DIRECTORY_STATUS))
		if (f1->dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY){
			if (!(f2->dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
				return ((fsd->options & LIST_DIRECTORIES_LAST) == 0) ? -1 : 1;
		}else if ((f2->dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
				return ((fsd->options & LIST_DIRECTORIES_LAST) == 0) ? 1 : -1;


	if (fsd->options & LIST_BY_SIZE) {
		if (f1->nFileSizeLow > f2->nFileSizeLow) return 1;
		else if (f1->nFileSizeLow < f2->nFileSizeLow) return -1;
	}else if (fsd->options & LIST_BY_DATE){
		time_t *ot = &f1->ftLastWriteTime;
		time_t *tw = &f2->ftLastWriteTime;
		int c = compareFiletime(*ot,*tw);
		if (c != 0) return c;
	}else if (fsd->options & LIST_BY_TYPE){
		int oneSt = -1, twoSt = -1;
		int ret;
		for (oneSt = f1->fileLength-1; oneSt >= 0; oneSt--)
			if (f1->fileName[oneSt] == '.') break;
		for (twoSt = f2->fileLength-1; twoSt >= 0; twoSt--)
			if (f2->fileName[twoSt] == '.') break;
		if (oneSt == -1) oneSt = f1->fileLength;
		else oneSt++;
		if (twoSt == -1) twoSt = f2->fileLength;
		else twoSt++;
		ret = compareTchars(LOCALE_SYSTEM_DEFAULT,NORM_IGNORECASE,f1->fileName+oneSt,f1->fileLength-oneSt,
			f2->fileName+twoSt,f2->fileLength-twoSt);
		if (ret != 0) return ret;
	}

	return compareTchars(LOCALE_SYSTEM_DEFAULT,NORM_IGNORECASE,
		f1->fileName,f1->fileLength,
		f2->fileName,f2->fileLength);

}


static TCHAR *_FileAlloc2(TCHAR *dir,TCHAR *file)
{
	int len = textLength(dir);
	int addLen = (file == NULL) ? 0 : textLength(file);
	TCHAR *s = (TCHAR *)malloc((len + addLen + 2) * sizeof(TCHAR));
	if (s == NULL) return NULL;
	else{
		int i = 0, j = 0;
		for (i = 0; i < len; i++)
			s[j++] = dir[i];
		i = 0;
		if (addLen != 0 && len != 0){
			if (s[j-1] != '\\' && s[j-1] != '/')
				s[j++] = '/';
		}
//
// Ensure that if you are appending something that starts with "\" to
// something that ends with "\" that you don't end up with "\\"
//
		if (j != 0)
			if (s[j-1] == '\\' || s[j-1] == '/' || s[j-1] == ':')
				if (addLen != 0)
					if (file[0] == '\\' || file[0] == '/')
						i++;

		for (; i < addLen; i++)
			s[j++] = file[i];
		s[j] = 0;
	}
	return s;
}
static TCHAR *_FileAlloc(WObject string, TCHAR *add)
	{
	TCHAR *s, *ret;

	s = stringToNativeText(string);
	if (s == 0) return NULL;
	ret = _FileAlloc2(s,add);
	free(s);
	return ret;
}

static void removeTrailingSlash(TCHAR *path)
{
	int len = textLength(path);
	if (len <= 1) return;
	if (path[len-1] == '/' || path[len-1] == '\\')
		if (path[len-2] != ':')
			path[len-1] = 0;
}

static TCHAR *_FileAllocMask(WObject string, WObject mask)
{
	uint16 *maskChars;
	int maskLen;
	maskChars = (uint16 *)getStringData(mask,&maskLen);
	if (maskLen == 0) return _FileAlloc(string,TEXT("/*"));
	else{
		TCHAR *tc = (TCHAR *)malloc(sizeof(TCHAR)*(maskLen+2));
		TCHAR *ret;
		int i = 0;
		tc[0] = (TCHAR)'/';
		for (i = 0; i<maskLen; i++)
			tc[i+1] = (TCHAR)maskChars[i];
		tc[i+1] = 0;
		ret = _FileAlloc(string,tc);
		free(tc);
		return ret;
	}
}

static int getFileAttributes2(FileListItem *dest,TCHAR *path,TCHAR *file)
{
	TCHAR *full = _FileAlloc2(path,file);
	int ret = 0;
	if (full == NULL) return 0;
	ret = getFileAttributes(dest,full);
	free(full);
	return ret;
}

static void _FileFree(TCHAR * path)
	{
	if (path != NULL)
		xfree((void *)path);
	}

#define FILE_CREATE_DIR 1
#define FILE_IS_DIR 2
#define FILE_DELETE 3
#define FILE_RENAME 4
#define FILE_EXISTS 5
#define FILE_GETFULLPATH 6
#define FILE_GETLENGTH 7
#define FILE_GET_SET_MODIFIED 8
#define FILE_GET_ATTRIBUTES 9
#define FILE_SET_ATTRIBUTES 10

static Var FileOp(Var stack[],int op)
{
	FileListItem fli;
	TCHAR *path = _FileAlloc(WOBJ_FileName(stack[0].obj),NULL);
	int ret = 0;
	int exists = getFileAttributes(&fli,path);
	switch(op){
	case FILE_GET_ATTRIBUTES:
	case FILE_SET_ATTRIBUTES:
		if (!exists) ret = -1;
		else{
			struct stat st;
			stat(path,&st);
			ret = st.st_mode;

			if (op == FILE_GET_ATTRIBUTES) {
				ret = ret & 0x1ff;
				if (((ret & (S_IWUSR|S_IWGRP|S_IWOTH)) == 0)) ret |= 0x8000; //FLAG_READONLY
				break;
			}

			ret |= stack[2].intValue & ~0x8000;
			ret &= ~(stack[3].intValue & ~0x8000);
			if (stack[3].intValue & 0x8000) //Clearing READONLY mode.
				ret |= S_IWUSR;
			if (stack[2].intValue & 0x8000) //Setting READONLY mode.
				ret &= ~(S_IWUSR|S_IWGRP|S_IWOTH);
			chmod(path,ret);
			stat(path,&st);
			ret = st.st_mode & 0x1ff;
			if (((ret & (S_IWUSR|S_IWGRP|S_IWOTH)) == 0)) ret |= 0x8000; //FLAG_READONLY
		}
		break;
	case FILE_EXISTS:
		ret = exists;
		break;
	case FILE_GETLENGTH:
		if (!exists) break;
		ret = fli.nFileSizeLow;
		break;
	case FILE_IS_DIR:
		if (!exists) break;
		ret = (fli.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0;
		break;
	case FILE_DELETE:
		if (!exists) break;
		if ((fli.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0)
			ret = rmdir(path) == 0;
		else
			ret = remove(path) == 0;
		break;
	case FILE_CREATE_DIR:
		if (exists) break;
#ifdef S_IRGRP

	ret = mkdir(path,S_IREAD|S_IWRITE|S_IEXEC|S_IRGRP|S_IROTH|S_IXGRP|S_IXOTH) == 0;
#else
	ret = mkdir(path,S_IREAD|S_IWRITE|S_IEXEC) == 0;
#endif
		break;
	case FILE_RENAME:
		{
		if (stack[1].obj == 0) return returnException(NullPointerEx,NULL);
		else{
			TCHAR *dstPath = _FileAlloc(WOBJ_FileName(stack[1].obj), NULL);
			if (dstPath != NULL){
				ret = rename(path,dstPath) == 0;
				free(dstPath);
			}
			break;
			}
		}
	 case FILE_GET_SET_MODIFIED:
		{
			WObject time = stack[1].obj;
			int get = stack[2].intValue;
			if (get)
				toTime(time,fli.ftLastWriteTime);
			else {
			  time_t t = fromTime(time);
				setModifiedTime(path,t);
			}
			ret = 1;
		}
	}
	_FileFree(path);
	return returnVar(ret);
}

static Var FileIsDir(Var stack[]) {return FileOp(stack,FILE_IS_DIR);}
static Var FileExists(Var stack[]) {return FileOp(stack,FILE_EXISTS);}
static Var FileDelete(Var stack[]) {return FileOp(stack,FILE_DELETE);}
static Var FileCreateDir(Var stack[]) {return FileOp(stack,FILE_CREATE_DIR);}
static Var FileRename(Var stack[]) {return FileOp(stack,FILE_RENAME);}
static Var FileGetLength(Var stack[]) {return FileOp(stack,FILE_GETLENGTH);}
static Var FileGetSetModified(Var stack[])
{
	return FileOp(stack,FILE_GET_SET_MODIFIED);
}

static Var FileGetSetAttributes(Var stack[])
{
	if ((stack[2].intValue|stack[3].intValue) & ~0x81ff) return returnVar(-2);
	if (stack[1].intValue) return FileOp(stack, FILE_GET_ATTRIBUTES);
	else return FileOp(stack,FILE_SET_ATTRIBUTES);
}

static Var FileDeleteOnExit(Var stack[])
{
	Var v;
	LinkedElement le = (LinkedElement)malloc(sizeof(struct linked_element));
	le->next = filesToDelete;
	filesToDelete = le;
	le->ptr = _FileAlloc(WOBJ_FileName(stack[0].obj),NULL);
	v.intValue = 1;
	return v;
}

static Var FileListDir(Var stack[])
	{
	WObject file, stringArray, *strings, mask;
	FileListItem *list, *item, **all;
	int i, numItems = 0, *indexes;
	int options = stack[2].intValue;
	int listOpts = 0;
	Var v;

	v.obj = 0;
	file = stack[0].obj;
	mask = stack[1].obj;
	list = NULL;

	if (options & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) {
		if (doList(NULL,file,mask,options)) {
			v.obj = createArrayObject(1,0);
			if (!v.obj) return v;
			WOBJ_arrayComponent(v.obj) = tryGetClass(createUtfString("java/lang/String"));
		}
		return v;
	}
	if (!(options & LIST_IGNORE_DIRECTORY_STATUS)) listOpts = LIST_FILES_ONLY;
	if (options & LIST_FILES_ONLY) listOpts |= LIST_FILES_ONLY;

	if (!(options & LIST_DIRECTORIES_ONLY))
		numItems += doList(&list,file,mask,listOpts);

	listOpts = LIST_DIRECTORIES_ONLY;
	if (!(options & (LIST_IGNORE_DIRECTORY_STATUS|LIST_FILES_ONLY))){
		if (options & LIST_ALWAYS_LIST_DIRECTORIES) mask = 0;
		numItems += doList(&list,file,mask,listOpts);
	}

	all = (FileListItem **)xmalloc(sizeof(FileListItem *)*numItems);
	indexes = (int *)xmalloc(sizeof(int)*numItems);
	i = numItems;
	item = list;
	for(item = list; item; item = item->next) {
		all[--i] = item;
		indexes[i] = i;
	}
//
//Sort if necessary.
//
	if (!(options & LIST_DONT_SORT)){
		SortInfo info;
		FileSortData fsd;
		//HICON oldc = 0;
		info = (SortInfo)xmalloc(sizeof(struct sort_info));
		fsd = (FileSortData)xmalloc(sizeof(struct file_sort_data));
		fsd->array = all;
		fsd->options = options;
		info->functionData = fsd;
		info->descending = options & LIST_DESCENDING;
		info->function = &sortDirectory;
		info->original = indexes;
		info->sourceLen = numItems;
		sort(info);
		xfree(fsd);
		xfree(info);
	}

	stringArray = createArrayObject(1, numItems);
	if (!stringArray) goto freereturn;
	WOBJ_arrayComponent(stringArray) = tryGetClass(createUtfString("java/lang/String"));

	if (pushObject(stringArray) == -1)
		goto freereturn;

	for (i = 0; i<numItems; i++) {
		WObject str;
		item = all[indexes[i]];
		strings = (WObject *)WOBJ_arrayStart(stringArray);
#ifdef UNICODE//WINCE
		str = createStringFromNativeText(item->fileName,
			lstrlen(item->fileName));
#else

		str = createString(item->fileName);
#endif
		if (!str) break;
		((WObject *)WOBJ_arrayStart(stringArray))[i] = str;
	}
/*
	i = numItems - 1;
	item = list;
	while (item)
		{
		// we need to recompute the start pointer each iteration
		// in case garbage collection during a string create causes
		// memory to move around
		strings = (WObject *)WOBJ_arrayStart(stringArray);
#ifdef UNICODE//WINCE
		strings[i--] = createStringFromUnicode(item->fileName,
			lstrlen(item->fileName));
#else
		strings[i--] = createString(item->fileName);
#endif
		item = item->next;
		}
*/
	popObject(); // stringArray

freereturn:
	// free linked list
	while (list)
		{
		item = list;
		list = list->next;
		xfree(item->fileName);
		xfree(item);
		}
	xfree(all);
	xfree(indexes);
	v.obj = stringArray;
	return v;
	}


//
// Platform dependent local functions
//
//=======================================
//
// compareTchars() - compare two native text strings.
//
//=======================================
#ifdef VCC
static int mCompareString(LCID id,int options,TCHAR *one,int oneLength,TCHAR *two,int twoLength)
{
	options |= SORT_STRINGSORT;
	if (oneLength != twoLength) options |= NORM_IGNORECASE;
	return CompareString(id,options,one,oneLength,two,twoLength);
}

static int compareTchars(LCID id,int options,TCHAR *strOne,int lenOne,TCHAR *strTwo,int lenTwo)
{
	int got = mCompareString(id,options,strOne,lenOne,strTwo,lenTwo);
	if (got == CSTR_EQUAL) return 0;
	else if (got == CSTR_LESS_THAN) return -1;
	else return 1;
}
#else
static int compareTchars(LCID id,int options,TCHAR *strOne,int lenOne,TCHAR *strTwo,int lenTwo)
{
	int i = 0;
	for (i = 0; i<lenOne; i++){
		TCHAR o,t;
		if (i >= lenTwo) return 1;
		o = strOne[i];
		t = strTwo[i];
		if (options & NORM_IGNORECASE){
			o = (TCHAR)toupper(o);
			t = (TCHAR)toupper(t);
		}
		if (o > t) return 1;
		else if (o < t) return -1;
	}
	if (i < lenTwo) return -1;
	return 0;
}
#endif


#ifndef LCTYPE
#define LCTYPE int
#endif


static int getFirstDayOfWeek(LCID id)
{
	return 1; //Monday
}

TCHAR * monthNames[] = {"<Start from 1>","January","February","March","April","May","June","July","August","September","October","November","December"};
TCHAR * shortMonthNames[] = {"<S1>","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

TCHAR * dayNames[] = {"<Start from 1>","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
TCHAR * shortDayNames[] = {"<S1>","Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
TCHAR localeBuff[20];

static int numLocales, localesSize, *locales;

static int triedLocale = 0;
static uchar *storedLocaleData;
static LCID lastId = -1;
static uchar *lastData;

static uchar *getLocaleData()
{
	uint size;
	if (storedLocaleData == NULL && !triedLocale){
		storedLocaleData = loadFromMem("Locale.dat", 10, &size, NULL);
		if (storedLocaleData == NULL)
			storedLocaleData = readFileIntoMemory("Locale.dat",0,&size);
		triedLocale = 1;
	}
	return storedLocaleData;
}

static uchar *getIdData(LCID id)
{
	if (id == -1) return NULL;
	if (id == lastId) return lastData;
	uchar *ld = getLocaleData();
	if (ld == NULL) return NULL;
	uint16 num = getUInt16(ld);
	ld += 8;
	for (int i = 0; i<num; i++, ld += 8){
		int fid = getInt32(ld);
		if (fid == id) {
			lastId = id;
			lastData = storedLocaleData+getUInt32(ld+4);
			return lastData;
		}
	}
	return NULL;
}

static char *getLocaleInfoFromData(uchar *idData, LCTYPE localeInfo)
{
	if (idData == NULL) return NULL;
	uchar *d = idData+8;
	uchar *end = d+getUInt32(idData+4);
	while(d < end){
		LCTYPE found = getUInt16(d) & 0xffff;
		if (found == localeInfo)
			return (char *)(d+2);
		d += 2;
		while (*d != 0) d++;
		d++;
	}
	uint32 base = getUInt32(idData);
	if (base != 0) return getLocaleInfoFromData(storedLocaleData+base, localeInfo);
	return NULL;
}

static char *getLocaleInfoValue(LCID id, LCTYPE localeInfo)
{
	return getLocaleInfoFromData(getIdData(id),localeInfo);
}

#ifdef IS_WINDOWS
static BOOL CALLBACK enumLocalesProc(TCHAR *text)
{
	while (numLocales >= localesSize) {
		int *nl = (int *)malloc(sizeof(int)*(localesSize+1)*2);
		if (locales != NULL) {
			memcpy(nl,locales,sizeof(int)*localesSize);
			free(locales);
		}
		locales = nl;
		localesSize = (localesSize+1)*2;
	}
	{
		int ret = 0;
		for (;*text != 0; text++){
			TCHAR c = *text;
			if (c >= '0' && c <= '9')
				ret = (ret*16)+(c-'0');
			else
				ret = (ret*16)+(c-'A'+10);
		}
		locales[numLocales++] = ret;
	}

	return 1;
}
#endif
static int *getAllLocales(int options,int *size)
{
	if (locales != NULL)
		free(locales);
	numLocales = localesSize = 0;
#ifdef IS_WINDOWS
	if (options == 0) options = LCID_SUPPORTED;
	EnumSystemLocales(enumLocalesProc,options);
#else
	*size = 0;
	uchar *ld = getLocaleData();
	if (ld == NULL) return NULL;
	uint16 num = getUInt16(ld);
	locales = (int *)malloc(sizeof(int)*num);
	ld += 8;
	for (int i = 0; i<num; i++, ld += 8){
		int fid = getInt32(ld);
		locales[i] = fid;
	}
	numLocales = num;
#endif
	*size = numLocales;
	return locales;
}
static WClass * getLongClass();
static WClass * getDoubleClass();
static void setDouble(WObject,double);
static void setLong(WObject,int64);
static double getDouble(WObject);
static int64 getLong(WObject);

static WClass * timeClass;
static WClassMethod *timeToString;
static WClassMethod *timeFromString;

//
//  The following LCTypes are mutually exclusive in that they may NOT
//  be used in combination with each other.
//
#define LOCALE_ILANGUAGE              0x00000001   // language id
#define LOCALE_SLANGUAGE              0x00000002   // localized name of language
#define LOCALE_SENGLANGUAGE           0x00001001   // English name of language
#define LOCALE_SABBREVLANGNAME        0x00000003   // abbreviated language name
#define LOCALE_SNATIVELANGNAME        0x00000004   // native name of language

#define LOCALE_ICOUNTRY               0x00000005   // country code
#define LOCALE_SCOUNTRY               0x00000006   // localized name of country
#define LOCALE_SENGCOUNTRY            0x00001002   // English name of country
#define LOCALE_SABBREVCTRYNAME        0x00000007   // abbreviated country name
#define LOCALE_SNATIVECTRYNAME        0x00000008   // native name of country

#define LOCALE_IDEFAULTLANGUAGE       0x00000009   // default language id
#define LOCALE_IDEFAULTCOUNTRY        0x0000000A   // default country code
#define LOCALE_IDEFAULTCODEPAGE       0x0000000B   // default oem code page
#define LOCALE_IDEFAULTANSICODEPAGE   0x00001004   // default ansi code page
#define LOCALE_IDEFAULTMACCODEPAGE    0x00001011   // default mac code page

#define LOCALE_SLIST                  0x0000000C   // list item separator
#define LOCALE_IMEASURE               0x0000000D   // 0 = metric, 1 = US

#define LOCALE_SDECIMAL               0x0000000E   // decimal separator
#define LOCALE_STHOUSAND              0x0000000F   // thousand separator
#define LOCALE_SGROUPING              0x00000010   // digit grouping
#define LOCALE_IDIGITS                0x00000011   // number of fractional digits
#define LOCALE_ILZERO                 0x00000012   // leading zeros for decimal
#define LOCALE_INEGNUMBER             0x00001010   // negative number mode
#define LOCALE_SNATIVEDIGITS          0x00000013   // native ascii 0-9

#define LOCALE_SCURRENCY              0x00000014   // local monetary symbol
#define LOCALE_SINTLSYMBOL            0x00000015   // intl monetary symbol
#define LOCALE_SMONDECIMALSEP         0x00000016   // monetary decimal separator
#define LOCALE_SMONTHOUSANDSEP        0x00000017   // monetary thousand separator
#define LOCALE_SMONGROUPING           0x00000018   // monetary grouping
#define LOCALE_ICURRDIGITS            0x00000019   // # local monetary digits
#define LOCALE_IINTLCURRDIGITS        0x0000001A   // # intl monetary digits
#define LOCALE_ICURRENCY              0x0000001B   // positive currency mode
#define LOCALE_INEGCURR               0x0000001C   // negative currency mode

#define LOCALE_SDATE                  0x0000001D   // date separator
#define LOCALE_STIME                  0x0000001E   // time separator
#define LOCALE_SSHORTDATE             0x0000001F   // short date format string
#define LOCALE_SLONGDATE              0x00000020   // long date format string
#define LOCALE_STIMEFORMAT            0x00001003   // time format string
#define LOCALE_IDATE                  0x00000021   // short date format ordering
#define LOCALE_ILDATE                 0x00000022   // long date format ordering
#define LOCALE_ITIME                  0x00000023   // time format specifier
#define LOCALE_ITIMEMARKPOSN          0x00001005   // time marker position
#define LOCALE_ICENTURY               0x00000024   // century format specifier (short date)
#define LOCALE_ITLZERO                0x00000025   // leading zeros in time field
#define LOCALE_IDAYLZERO              0x00000026   // leading zeros in day field (short date)
#define LOCALE_IMONLZERO              0x00000027   // leading zeros in month field (short date)
#define LOCALE_S1159                  0x00000028   // AM designator
#define LOCALE_S2359                  0x00000029   // PM designator

#define LOCALE_ICALENDARTYPE          0x00001009   // type of calendar specifier
#define LOCALE_IOPTIONALCALENDAR      0x0000100B   // additional calendar types specifier
#define LOCALE_IFIRSTDAYOFWEEK        0x0000100C   // first day of week specifier
#define LOCALE_IFIRSTWEEKOFYEAR       0x0000100D   // first week of year specifier

#define LOCALE_SDAYNAME1              0x0000002A   // long name for Monday
#define LOCALE_SDAYNAME2              0x0000002B   // long name for Tuesday
#define LOCALE_SDAYNAME3              0x0000002C   // long name for Wednesday
#define LOCALE_SDAYNAME4              0x0000002D   // long name for Thursday
#define LOCALE_SDAYNAME5              0x0000002E   // long name for Friday
#define LOCALE_SDAYNAME6              0x0000002F   // long name for Saturday
#define LOCALE_SDAYNAME7              0x00000030   // long name for Sunday
#define LOCALE_SABBREVDAYNAME1        0x00000031   // abbreviated name for Monday
#define LOCALE_SABBREVDAYNAME2        0x00000032   // abbreviated name for Tuesday
#define LOCALE_SABBREVDAYNAME3        0x00000033   // abbreviated name for Wednesday
#define LOCALE_SABBREVDAYNAME4        0x00000034   // abbreviated name for Thursday
#define LOCALE_SABBREVDAYNAME5        0x00000035   // abbreviated name for Friday
#define LOCALE_SABBREVDAYNAME6        0x00000036   // abbreviated name for Saturday
#define LOCALE_SABBREVDAYNAME7        0x00000037   // abbreviated name for Sunday
#define LOCALE_SMONTHNAME1            0x00000038   // long name for January
#define LOCALE_SMONTHNAME2            0x00000039   // long name for February
#define LOCALE_SMONTHNAME3            0x0000003A   // long name for March
#define LOCALE_SMONTHNAME4            0x0000003B   // long name for April
#define LOCALE_SMONTHNAME5            0x0000003C   // long name for May
#define LOCALE_SMONTHNAME6            0x0000003D   // long name for June
#define LOCALE_SMONTHNAME7            0x0000003E   // long name for July
#define LOCALE_SMONTHNAME8            0x0000003F   // long name for August
#define LOCALE_SMONTHNAME9            0x00000040   // long name for September
#define LOCALE_SMONTHNAME10           0x00000041   // long name for October
#define LOCALE_SMONTHNAME11           0x00000042   // long name for November
#define LOCALE_SMONTHNAME12           0x00000043   // long name for December
#define LOCALE_SMONTHNAME13           0x0000100E   // long name for 13th month (if exists)
#define LOCALE_SABBREVMONTHNAME1      0x00000044   // abbreviated name for January
#define LOCALE_SABBREVMONTHNAME2      0x00000045   // abbreviated name for February
#define LOCALE_SABBREVMONTHNAME3      0x00000046   // abbreviated name for March
#define LOCALE_SABBREVMONTHNAME4      0x00000047   // abbreviated name for April
#define LOCALE_SABBREVMONTHNAME5      0x00000048   // abbreviated name for May
#define LOCALE_SABBREVMONTHNAME6      0x00000049   // abbreviated name for June
#define LOCALE_SABBREVMONTHNAME7      0x0000004A   // abbreviated name for July
#define LOCALE_SABBREVMONTHNAME8      0x0000004B   // abbreviated name for August
#define LOCALE_SABBREVMONTHNAME9      0x0000004C   // abbreviated name for September
#define LOCALE_SABBREVMONTHNAME10     0x0000004D   // abbreviated name for October
#define LOCALE_SABBREVMONTHNAME11     0x0000004E   // abbreviated name for November
#define LOCALE_SABBREVMONTHNAME12     0x0000004F   // abbreviated name for December
#define LOCALE_SABBREVMONTHNAME13     0x0000100F   // abbreviated name for 13th month (if exists)

#define LOCALE_SPOSITIVESIGN          0x00000050   // positive sign
#define LOCALE_SNEGATIVESIGN          0x00000051   // negative sign
#define LOCALE_IPOSSIGNPOSN           0x00000052   // positive sign position
#define LOCALE_INEGSIGNPOSN           0x00000053   // negative sign position
#define LOCALE_IPOSSYMPRECEDES        0x00000054   // mon sym precedes pos amt
#define LOCALE_IPOSSEPBYSPACE         0x00000055   // mon sym sep by space from pos amt
#define LOCALE_INEGSYMPRECEDES        0x00000056   // mon sym precedes neg amt
#define LOCALE_INEGSEPBYSPACE         0x00000057   // mon sym sep by space from neg amt



static TCHAR *getStaticLocaleInfo(LCID id,LCTYPE data)
{
	//return NULL;
	getLocaleInfoValue(id,data);
}

static int GetLocaleInfo(LCID id,LCTYPE data,TCHAR *dest,int bytesAvailable)
{
	TCHAR *got = getStaticLocaleInfo(id,data);
	if (got == NULL) return 0;
	int need = textLength(got)+1;
	if (bytesAvailable >= need && dest != NULL)
		strcpy(dest,got);
	return need;
}
static TCHAR *getLocaleInfo(LCID id,LCTYPE which)
{
	int size = GetLocaleInfo(id,which,info,256);
	if (size <= 0) return NULL;
	return info;
}
static int getLocaleText(TempText tt,LCID id,LCTYPE data,TCHAR *defValue)
{
	int sz = GetLocaleInfo(id,data,NULL,0);
	int useDefault = 0;
	if (sz == 0){
		if (defValue == NULL) return 0;
		useDefault = 1;
		sz = textLength(defValue)+1;
	}
	if (sz > tt->totalSize) {
		if (tt->totalSize != 0) free(tt->text);
		tt->text = (TCHAR *)malloc(sizeof(TCHAR)*sz);
		tt->totalSize = sz;
	}
	if (useDefault) strcpy(tt->text,defValue);
	else GetLocaleInfo(id,data,tt->text,sz);
	return 1;
}

static char * countryConversion =
"AFGAFALBALDZADZASMASANDADAGOAOAIAAIATGAGARGARARMAMABWAWAUSAUAUTATAZEAZBHSBSBHRBHBGDBDBRBBBBLRBYBELBEBLZBZBENBJBMUBMBTNBTBOLBOBIHBABWABWBRABRVGBVGBRNBNBGRBGBFABFBDIBIKHMKHCMRCMCANCACPVCVCYMKYCAFCFTCDTDCHLCLCHNCNHKGHKCOLCOCOMKMCOGCGCOKCKCRICRCIVCIHRVHRCUBCUCYPCYCZECZPRKKPCODCDDNKDKDJIDJDMADMDOMDOTMPTPECUECEGYEGSLVSVGNQGQERIERESTEEETHETFROFKFLKFOFJIFJFINFIFRAFRGUFGFPYFPFGABGAGMBGMGEOGEDEUDEGHAGHGIBGIGRCGRGRLGLGRDGDGLPGPGUMGUGTMGTGINGNGNBGWGUYGYHTIHTVATVAHNDHKHUNHUISLISINDINIDNIDIRNIRIRQIQIRLIEISRILITAITJAMJMJPNJPJORJOKAZKZKENKEKIRKIKWTKWKGZKGLAOLALVALVLBNLBLSOLSLBRLRLBYLYLIELILTULTLUXLUMDGMGMWIMWMYSMYMDVMVMLIMLMLTMTMHLMHMTQMQMRTMRMUSMUMEXMXFSMFMMCOMCMNGMNMSRMSMARMAMOZMZMMRMMNAMNANRUNRNPLNPNLDNLANTANNCLNCNZLNZNICNINERNENGANGNIUNUNFKNFMNPMPNORNOOMNOMPAKPKPLWPWPSEPSPANPAPNGPGPRYPYPERPEPHLPHPCNPNPOLPLPRTPTPRIPRQATQAKORKRMDAMDREUREROMRORUSRURWARWSHNSHKNAKNLCALCSPMPMVCTVCWSMWSSMRSMSTPSTSAUSASENSNSYCSCSLESLSGPSGSVKSKSVNSISLBSBSOMSOZAFZAESPESLKALKSDNSDSURSRSJMSJSWZSZSWESECHECHSYRSYTWNTWTJKTJTHATHMKDMKTGOTGTKLTKTONTOTTOTTTUNTNTURTRTKMTMTCATCTUVTVUGAUGUKRUAAREAEGBRGBTZATZUSAUSVIRVIURYUYUZBUZVUTVUVENVEVNMVNWLFWFESHEHYEMYEYUGYUZMBZMZWEZW"
;
/**
* This will convert a three letter (uppercase) ISO country code to the two letter (uppercase) ISO country code
* OR the other way. It will return an empty string if no conversion was found.
**/
//===================================================================
static char *convertCountryCode(char *source,char *dest)
//===================================================================
{
	int len, i;
	char *cc = countryConversion;
	if (source == NULL) return NULL;
	len = strlen(source);
	if (len != 2 && len != 3) return NULL;

	if (len == 3){
		for(;*cc != 0; cc+=5){
			if (*cc != *source) continue;
			if (*(cc+1) != *(source+1)) continue;
			if (*(cc+2) != *(source+2)) continue;
			dest[0] = *(cc+3);
			dest[1] = *(cc+4);
			dest[2] = 0;
			return dest;
		}
	}else{
		for(;*cc != 0; cc+=5){
			if (*(cc+3) != *source) continue;
			if (*(cc+4) != *(source+1)) continue;
			dest[0] = *(cc);
			dest[1] = *(cc+1);
			dest[2] = *(cc+2);
			dest[3] = 0;
			return dest;
		}
	}
	return NULL;
}

static Var LocaleGetDefaultLanguage(Var stack[])
{
	Var v;
	v.obj = 0;
	static char *lang = getenv("LANG");
	static char nl[7];
	if (localeData[0] != 0){
		v.obj = createStringFromJavaUtf8(strlen(localeData),NULL,localeData);
		return v;
	}
	if (lang != NULL) {
			//printf("LANG: %s\n",lang);
			if (strlen(lang) == 5)
				if (lang[2] == '_'){
					char *got;
					strncpy(nl,lang,2);
					nl[2] = '-';
					got = convertCountryCode(lang+3,nl+3);
					if (got != NULL){
						//printf("Converted to: %s\n",nl);
						v.obj = createStringFromJavaUtf8(strlen(nl),NULL,nl);
					}
				}
	}
	if (v.obj == 0) {
		lang = "en-USA";
		v.obj = createStringFromJavaUtf8(strlen(lang),NULL,lang);
	}
	//NativeText(localeData,-1);
	return v;
}
static void checkTimeClass()
{
	if (timeClass == NULL){
		timeClass = getClass(createUtfString("ewe/sys/Time"));
		timeToString = getMethod(timeClass,createUtfString("toString"),createUtfString("(Lewe/sys/Time;Ljava/lang/String;Lewe/sys/Locale;)Ljava/lang/String;"),NULL);
		timeFromString = getMethod(timeClass,createUtfString("fromString"),createUtfString("(Ljava/lang/String;Lewe/sys/Time;Ljava/lang/String;Lewe/sys/Locale;)Z"),NULL);
	}
}
typedef struct numericFormat {
	struct tempText source;
	int sourceLength;
	int minIntegerDigits;
	int maxIntegerDigits;
	int minDecimalDigits;
	int maxDecimalDigits;
	int useGroupings;
	int showCurrencySymbol;
	int monetary;
	int dontRound;
}* NumericFormat;


struct numericFormat currencyFormat, numberFormat;


static char decimalPointl = 0, plusSignl = 0, negSignl = 0, groupPointl = 0;

static LCTYPE locals[4] = {LOCALE_SDECIMAL,LOCALE_SPOSITIVESIGN,LOCALE_SNEGATIVESIGN,LOCALE_STHOUSAND};

static char *getDecimalDoubleL(char *from,double *dest)
{
	double ret = 0;
	double div = 10;
	double neg = 1;
	char *s = from;

	*dest = 0;
	if (from == 0) return s;
	while(*s == ' ') s++;
	if (*s == negSignl) neg = -1, s++;
	else if (*s == plusSignl) neg = 1, s++;
	for(;*s != 0;s++){
		char c = *s;
		if (c == groupPointl) continue;
		if (c<'0' || c >'9') break;
		ret = (ret*10.0)+(double)(c-'0');
	}
	if (*s != decimalPointl){
		*dest = ret*neg;
		return s;
	}
	for(s++;*s != 0;s++){
		char c = *s;
		if (c<'0' || c >'9') break;
		ret += (double)(c-'0')/div;
		div *= 10.0;
	}
	*dest = ret*neg;
	return s;
}
static void setLocalSymbol(char *symbols,int max,int index,LCTYPE type,LCID id)
{
	TCHAR ch = 0;
	if (index >= max) return;
	if (getLocaleText(&tempInfo,id,type,NULL))
		if (tempInfo.totalSize >= 1)
			ch = (char)tempInfo.text[0];
	if (ch != 0 && ch != ' ') symbols[index] = (char)ch;
}

static void getLocalSymbolsL(char *symbols,int max,LCID id)
{
	int i = 0;
	for (i = 0; i<4; i++)
		setLocalSymbol(symbols,max,i,locals[i],id);
}
static void getLocalSymbols(char *symbols,int max)
{
	getLocalSymbolsL(symbols,max,LOCALE_USER_DEFAULT);
}

static void getSymbolsL(LCID id)
{
	char symbols[4] = { '.', '+', '-', ',' };
	getLocalSymbolsL(symbols,4,id);
	decimalPointl = symbols[0];
	plusSignl = symbols[1];
	negSignl = symbols[2];
	groupPointl = symbols[3];
}


static int revCat(TempText dest,int destPosition,TCHAR *src,int length)
{
	if (length < 0) length = textLength(src);
	while(destPosition+length >= dest->totalSize) expandText(dest,(dest->totalSize+1)*2);
	{
		TCHAR *s;
		TCHAR *d = dest->text+destPosition;
		for (s = src+length-1; s >= src; s--) *d++ = *s;
		*d = 0;
	}
	return length;
}
static void getSymbols(LCID id,int monetary)
{
	getLocaleText(&currencySymbol,id,LOCALE_SCURRENCY,"$");
	getLocaleText(&groupSymbol,id,monetary ? LOCALE_SMONTHOUSANDSEP:LOCALE_STHOUSAND,",");
	getLocaleText(&groupings,id,monetary ? LOCALE_SMONGROUPING:LOCALE_SGROUPING,"3");
	getLocaleText(&decimalSymbol,id,monetary ? LOCALE_SMONDECIMALSEP:LOCALE_SDECIMAL,".");
	getLocaleText(&decDigits,id,monetary ? LOCALE_ICURRDIGITS: LOCALE_IDIGITS,"2");
	getLocaleText(&negativeSymbol,id,LOCALE_SNEGATIVESIGN,"-");
	getLocaleText(&positiveSymbol,id,LOCALE_SPOSITIVESIGN,"+");
}
static TCHAR *getCurrencySymbol(LCID id)
{
	getLocaleText(&currencySymbol,id,LOCALE_SCURRENCY,"$");
	return currencySymbol.text;
}
static TCHAR *findValue(TCHAR *text,int which)
{
	while(which != 0){
		while(*text != ';' && *text != 0) text++;
		if (*text != 0) text++;
		which--;
	}
	return text;
}
static int getIntValue(TCHAR *text)
{
	int value = 0;
	if (text == NULL) return value;
	while(1){
		TCHAR ch = *text;
		if (ch < '0' || ch > '9') return value;
		value *= 10;
		value += (int)(ch-'0');
		text++;
	}
}

static int inegMap [] = {0,1,9,6,10};
static int icurMap [] = {1,5,9,8};

struct tempText negFormatText;

static char * numberFormats [] =
{"($.)","-$.","$-.","$.-","(.$)",
"-.$",".-$",".$-","-. $","-$ .",
". $-","$ .-","$ -.",".- $","($ .)","(. $)"};

static char * getNumberFormat(LCID id,LCTYPE which,int *mapping,int mapLength)
{
/*
	if (which == LOCALE_INEGCURR) return "($.)";
	else if (which == LOCALE_ICURRENCY) return "$.";
	else if (which == LOCALE_INEGNUMBER) return "-.";
	else return ".";
*/
	getLocaleText(&negFormatText,id,which,"0");
	int v = getIntValue(negFormatText.text);
	if (mapping != NULL && v < mapLength)
		v = mapping[v];
	if (v <= 16) return numberFormats[v];
	else return numberFormats[0];
}

static int getGroupValue(TCHAR *text,int which,int last)
{
	int v = getIntValue(findValue(text,which));
	if (v == 0) return last;
	return v;
}
static double parse(LCID id,WObject string,NumericFormat nf)
{
	UtfString str;
	double ret = 0, exp = 0;
	char *s;
	if (string == 0)
		return 0;
	str = stringToUtf(string,STU_USE_STATIC|STU_NULL_TERMINATE);
	getSymbolsL(id);
	s = getDecimalDoubleL(str.str,&ret);
	if (*s != 'e' && *s != 'E') return ret;
	getDecimalDoubleL(s+1,&exp);
	ret = ret*pow(10,exp);
	return ret;
}

static void parseNumericFormat(NumericFormat nf)
{
	int i;
	int afterDec = 0;

	nf->dontRound =
	nf->showCurrencySymbol =
	nf->useGroupings =
	nf->minDecimalDigits = nf->maxDecimalDigits =
	nf->maxIntegerDigits = nf->minIntegerDigits = 0;

	for (i = 0; i<nf->sourceLength; i++){
		TCHAR ch = nf->source.text[i];
		switch(ch){
		case '$': nf->showCurrencySymbol = 1; break;
		case ',': nf->useGroupings = 1; break;
		case '.': afterDec = 1; nf->minDecimalDigits = nf->maxDecimalDigits = -1; break;
		case '#': if (afterDec) nf->minDecimalDigits = 0; break;
		case '0': if (afterDec) {
					nf->maxDecimalDigits++;
					nf->minDecimalDigits++;
					if (nf->maxDecimalDigits == 0){
						nf->maxDecimalDigits++;
						nf->minDecimalDigits++;
					}
				  }else{
					nf->minIntegerDigits++;
				  }
				  break;
		default: break;
		}
	}
}
static void setNumericFormat(WObject format,TCHAR *defaultFormat,NumericFormat dest)
{
	TCHAR *use = defaultFormat;
	int flen = textLength(use);
	int freeMe = 0;
	int doParse = 1;

	if (format != 0)
		if (compatible(WOBJ_class(format),stringClass))
			use = stringToTextInPlace(format,&flen,&freeMe);

	if (dest->source.totalSize != 0)
		if (textCompare(dest->source.text,dest->sourceLength,use,flen) == 0)
			doParse = 0;
	if (doParse) {
		tempCat(&dest->source,0,use,flen);
		dest->sourceLength = flen;
		parseNumericFormat(dest);
	}
	if (freeMe) free(use);
}


static TCHAR *format(LCID id,double value,NumericFormat nf)
{
	int64 div = 10;
	int neg = value<0;
	int frontLength = 1;
	int decimal = 0;

	getSymbols(id,nf->monetary);
	decimal = nf->maxDecimalDigits;
	if (decimal == -1) decimal = getIntValue(decDigits.text);
	//if (decimal == 0) decimal =
	//if ((options & NO_DECIMAL_VALUE) != 0) decimal = 0;

	if (neg) value = -value;
// Round up.
	if (!nf->dontRound){
		double add = 0.5555555555;
		int i = 0;
		for (i = 0; i<decimal; i++) add /= 10.0;
		value += add;
	}

// Do front part.
	{
		TempText dest = &tempInfo;
		int64 pre = (int64)floor(value);
		int did = 0;
		int grouped = 0;
		int lastGroupSize = getGroupValue(groupings.text,grouped,0);
		int pos = 0;
		int digits = 0;
		TCHAR val;
		do{
			if (did == lastGroupSize) {
				if (nf->useGroupings)
					pos += tempCat(dest,pos,groupSymbol.text,-1);
				did = 0;
				grouped++;
				lastGroupSize = getGroupValue(groupings.text,grouped,lastGroupSize);
			}
			val = (TCHAR)('0'+pre%10);
			pos += tempCat(dest,pos,&val,1);
			did++;
			digits++;
			pre /= 10;
		}while(pre != 0 || digits < nf->minIntegerDigits);
	//At this point: dest->text holds the reversed non decimal part.
		{
			TempText f = &formatted;
			int fp = 0;
			char * format = ".";
			char *fm;
			TCHAR *ms = currencySymbol.text;
			TCHAR *ns = negativeSymbol.text;
			if (nf->monetary){
				if (neg)
					format = getNumberFormat(id,LOCALE_INEGCURR,NULL,0);
				else
					format = getNumberFormat(id,LOCALE_ICURRENCY,icurMap,4);
			}else{
				if (neg) format = getNumberFormat(id,LOCALE_INEGNUMBER,inegMap,5);
			}
			if (!nf->monetary || !nf->showCurrencySymbol) ms = NULL;
			if (!neg) ns = NULL;
			for (fm = format; *fm != 0; fm++){
				switch(*fm){
				case '-': fp += tempCat(f,fp,ns,-1); break;
				case '$': fp += tempCat(f,fp,ms,-1); break;
				case '.':
					{
					TCHAR tch = (TCHAR)*fm;
					double dv = value-floor(value);
					int64 dig;
					int i;
					fp += revCat(f,fp,dest->text,-1);
					// Put value after decimal point here!
					if (decimal > 0){
						int min = nf->minDecimalDigits;
						fp += tempCat(f,fp,decimalSymbol.text,-1);
						for (i = 0; i<decimal; i++){
							dv *= 10;
							dig = ((int64)dv)%10;
							dv -= dig;
							tch = (TCHAR)('0'+dig);
							fp += tempCat(f,fp,&tch,1);
						}
						if (min != -1 && min < decimal){
							min = decimal-min;
							for (;min > 0; min--)
								if (f->text[fp-1] != '0') break;
								else f->text[--fp] = 0;
							if (min == 0) f->text[--fp] = 0; //Get rid of decimal point.
						}
					}
					}
					break;
				default:
					{
						TCHAR tch = (TCHAR)*fm;
						fp += tempCat(f,fp,&tch,1);
						break;
					}
				}
			}
			return f->text;
		}
	}
// Now do rest.
	return nf->source.text;
}

/*
TCHAR ** getLocaleStrings(int length,LCTYPE types[])
{
	int i;
	TCHAR **ret = malloc(sizeof(TCHAR *)*length);
	for (i = 0; i<length; i++){
		ret[i] = getLocaleInfo(types[i],localeID);
		if (ret[i] == NULL) ret[i] = TEXT("");
	}
	return ret;
}
void getAllLocaleInfo()
{
	TCHAR *info;
	localeDays = getLocaleStrings(7,dayTypes);
	localeDaysShort = getLocaleStrings(7,dayTypesShort);
	localeMonths = getLocaleStrings(13,monthTypes);
	localeMonthsShort = getLocaleStrings(13,monthTypesShort);
	localeAMPM = getLocaleStrings(2,ampmTypes);

	info = getLocaleInfo(LOCALE_IFIRSTDAYOFWEEK);
	if (info != NULL)
		firstDayOfWeek = info[0]-(TCHAR)'0';
	//firstDayOfWeek = 6; // For testing only. This set it to Sunday
}
void checkLocale()
{
	if (localeDays == NULL) getAllLocaleInfo();
}
*/
static int checkBeginsWith(TCHAR *b,int16 *str,int len,int all)
{
	int16 *end = str+len;
	for (;*b != 0 && str != end; b++,str++){
		TCHAR c = *b;
		TCHAR c2 = (TCHAR)(*str & 0xff);
		if (c >= 'a' && c <= 'z') c ^= 0x20;
		if (c2 >= 'a' && c2 <= 'z') c2 ^= 0x20;
		if (c != c2) return 0;
	}
	if (!all) return (str == end);
	return (str == end && *b == 0);
}
static Var LocaleFormat(Var stack[])
{
	Var v;
	WObject me = stack[0].obj;
	LCID myId = (LCID)objectPtr(me)[1].intValue;
	int toFormat = stack[1].intValue;
	WObject value = stack[2].obj;
	WObject layout = stack[3].obj;
	int what = toFormat & 0xf0;

	v.intValue = 0;
	if (value == 0) return v;

	switch(what){
	case FORMAT_PARSE_NUMBER:
	case FORMAT_PARSE_CURRENCY:
		{
			double dv = 0;
			NumericFormat nf;
			if (what == FORMAT_PARSE_NUMBER){
				nf = &numberFormat;
				nf->monetary = 0;
				setNumericFormat(layout,TEXT("#.#"),nf);
			}else{
				nf = &currencyFormat;
				nf->monetary = 1;
				setNumericFormat(layout,TEXT("$,.00"),nf);
			}

			nf->dontRound = (toFormat & NO_ROUNDING);

			if (compatible(WOBJ_class(value),getDoubleClass()))
				dv = getDouble(value);
			else if (compatible(WOBJ_class(value),getLongClass()))
				dv = (double)getLong(value);
			else
				return v;
			TCHAR *ret = format(myId,dv,nf);
			v.obj = createStringFromJavaUtf8(strlen(ret),NULL,ret);
			return v;
		}
	case FORMAT_PARSE_DATE:
		{
			checkTimeClass();
			if (compatible(WOBJ_class(value),timeClass)){
				Var pars[3];
				if (layout != 0)
					if (!compatible(WOBJ_class(layout),stringClass)) return v;
				if (timeToString != NULL){
					pars[0].obj = value;
					pars[1].obj = layout;
					pars[2].obj = me;
					executeMethodRet(timeClass,timeToString,pars,3,&v);
				}
			}
			return v;
		}
	}
	return v;
}


static Var LocaleParse(Var stack[])
{
	Var v;
	WObject me = stack[0].obj;
	LCID myId = (LCID)objectPtr(me)[1].intValue;
	WObject source = stack[1].obj;
	int toFormat = stack[2].intValue;
	WObject value = stack[3].obj;
	WObject layout = stack[4].obj;
	int what = toFormat & 0xf0;

	v.intValue = 0;
	if (value == 0 || source == 0) return v;

	switch(what){
	case FORMAT_PARSE_NUMBER:
	case FORMAT_PARSE_CURRENCY:
		{
			double dv = 0;
			NumericFormat nf;
			if (what == FORMAT_PARSE_NUMBER){
				nf = &numberFormat;
				nf->monetary = 0;
				setNumericFormat(layout,TEXT("#.#"),nf);
			}else{
				nf = &currencyFormat;
				nf->monetary = 1;
				setNumericFormat(layout,TEXT("$,.00"),nf);
			}

			nf->dontRound = (toFormat & NO_ROUNDING);

			dv = parse(myId,source,nf);
			if (compatible(WOBJ_class(value),getDoubleClass()))
				setDouble(value,dv);
			else if (compatible(WOBJ_class(value),getLongClass()))
				setLong(value,(int64)dv);
			v.intValue = 1;
			return v;
		}
	case FORMAT_PARSE_DATE:
		{
			checkTimeClass();
			if (compatible(WOBJ_class(value),timeClass)){
				Var pars[4];
				if (layout != 0)
					if (!compatible(WOBJ_class(layout),stringClass)) return v;
				if (timeToString != NULL){
					pars[0].obj = source;
					pars[1].obj = value;
					pars[2].obj = layout;
					pars[3].obj = me;
					executeMethodRet(timeClass,timeFromString,pars,4,&v);
				}
			}
			return v;
		}
	}
	return v;
}



static int fullEquals(LCID id,TCHAR *one,int oneLength,TCHAR *two,int twoLength,int ignoreCase)
{
/*
	int ret = mCompareString(id,ignoreCase ? NORM_IGNORECASE : 0,one,oneLength,two,twoLength);
	int eq = CSTR_EQUAL;
	return ret == eq;
*/
	return compareTchars(id,ignoreCase ? NORM_IGNORECASE : 0,one,oneLength,two,twoLength) == 0;
}

static int checkStrings(LCID id,TCHAR *what,int oneLength,LCID start,int num)
{
	int i;
	for (i = 0; i<num; i++)
		if (fullEquals(id,what,oneLength,getLocaleInfo(id,start+i),-1,1)) {
			free(what);
			return i;
		}
	free(what);
	return -1;
}
static Var returnInt(int val) {Var v; v.intValue = val; return v;}
/*
static LCID getMyId(WObject who)
{
	int val = objectPtr(who)[1].intValue;
	return MAKELCID(
		*/
static Var LocaleFromString(Var stack[])
	{
	Var v;
	WObject me = stack[0].obj;
	LCID myId = (LCID)objectPtr(me)[1].intValue;
	int what = stack[1].intValue;
	WObject str = stack[2].intValue;
	int options = stack[3].intValue;
	int length = 0;
	int16 *s;
	TCHAR *sv;
	int r;
	v.intValue = -1;
	if (str == 0) return v;
	str = WOBJ_StringCharArrayObj(str);
	if (str == 0) return v;
	s = (int16*)WOBJ_arrayStart(str);
	length = WOBJ_arrayLen(str);
	sv = stringToNewUtf8(stack[2].intValue);
	switch(what){
		case(SHORT_MONTH):
			if ((r = checkStrings(myId,sv,length,LOCALE_SABBREVMONTHNAME1,13)) >= 0) return returnInt(r+1);
		case(MONTH):
			if ((r = checkStrings(myId,sv,length,LOCALE_SMONTHNAME1,13)) >= 0) return returnInt(r+1);
			break;
		case(SHORT_DAY):
			if ((r = checkStrings(myId,sv,length,LOCALE_SABBREVDAYNAME1,7)) >= 0) return returnInt(r+1);
		case(DAY):
			if ((r = checkStrings(myId,sv,length,LOCALE_SDAYNAME1,7)) >= 0) return returnInt(r+1);
			break;
		case(AM_PM):
			if ((r = checkStrings(myId,sv,length,LOCALE_S1159,2)) >= 0) return returnInt(r);
			break;
	}
	free(sv);
	return v;
}
/*
static int getFirstDayOfWeek(LCID id)
{
	TCHAR *ret = getLocaleInfo(id,LOCALE_IFIRSTDAYOFWEEK);
	if (ret == NULL) return 0;
	return *ret-'0';
}
*/

static TCHAR *localeGetStringValue(LCID id,int what,int value,int options)
{
	LCTYPE lookup = -1;
	switch(what){
	case MONTH:
		lookup = LOCALE_SMONTHNAME1+value-1; break;
	case SHORT_MONTH:
		lookup = LOCALE_SABBREVMONTHNAME1+value-1; break;
	case DAY:
		lookup = LOCALE_SDAYNAME1+value-1; break;
	case SHORT_DAY:
		lookup = LOCALE_SABBREVDAYNAME1+value-1; break;
	case DAY_OF_WEEK:
		if (value >= 1 && value <= 7) {
			value = (((value-1)+getFirstDayOfWeek(id))%7)+1;
			return localeGetStringValue(id,DAY,value,options);
		}
		break;
	case SHORT_DAY_OF_WEEK:
		if (value >= 1 && value <= 7) {
			value = (((value-1)+getFirstDayOfWeek(id))%7)+1;
			return localeGetStringValue(id,SHORT_DAY,value,options);
		}
		break;
	case AM_PM:
		lookup = LOCALE_S1159+value; break;
	case LANGUAGE:
		lookup = LOCALE_SLANGUAGE; break;
	case LANGUAGE_NATIVE:
		lookup = LOCALE_SNATIVELANGNAME; break;
	case LANGUAGE_ENGLISH:
		lookup = LOCALE_SENGLANGUAGE; break;
	case LANGUAGE_SHORT:{
		TCHAR *ret = getLocaleInfo(id,LOCALE_SABBREVLANGNAME);
		int i = 0;
		if (ret != NULL) {
			ret[2] = 0;
			for (i = 0; i<2; i++)
				ret[i] = tolower(ret[i]);
		}
		return ret;
						}
	case COUNTRY:
		lookup = LOCALE_SCOUNTRY; break;
	case COUNTRY_NATIVE:
		lookup = LOCALE_SNATIVECTRYNAME; break;
	case COUNTRY_ENGLISH:
		lookup = LOCALE_SENGCOUNTRY; break;
	case COUNTRY_SHORT:
		lookup = LOCALE_SABBREVCTRYNAME; break;
	case FIRST_DAY_OF_WEEK:
		{
		TCHAR *ret = getLocaleInfo(id,LOCALE_IFIRSTDAYOFWEEK);
		if (ret != NULL) (*ret)++;
		return ret;
		}
	case CURRENCY:
		lookup = LOCALE_SCURRENCY; break;
	case SHORT_DATE_FORMAT:
		lookup = LOCALE_SSHORTDATE; break;
	case LONG_DATE_FORMAT:
		lookup = LOCALE_SLONGDATE; break;
	case TIME_FORMAT:
		lookup = LOCALE_STIMEFORMAT; break;
	default:
		return NULL;
	}
	if (lookup == -1) return NULL;
	return getLocaleInfo(id,lookup);
	}

static Var LocaleGetAllIDs(Var stack[])
{
	Var v;
	int options = stack[0].intValue;
	int num;
	int *got = getAllLocales(options,&num);
	int32 *dest;
	v.obj = createArrayObject(arrayType('I'),num);
	if (!v.obj) return v;
	dest = (int32*)WOBJ_arrayStart(v.obj);
	for (;num>0;num--) *dest++ = (int32)(*got++);
	if (locales != NULL) free(locales);
	locales = NULL;
	localesSize = 0;
	numLocales = 0;
	return v;
}
/*
static TCHAR *localeGetStringValue(LCID id,int what,int value,int options)
{
	LCTYPE lookup = -1;
	switch(what){
	case MONTH:
		//lookup = LOCALE_SMONTHNAME1+value-1; break;
		if (value >= 0 && value <= 12) return monthNames[value];
	case SHORT_MONTH:
		//lookup = LOCALE_SABBREVMONTHNAME1+value-1; break;
		if (value >= 0 && value <= 12) return shortMonthNames[value];
	case DAY:
		//lookup = LOCALE_SDAYNAME1+value-1; break;
		if (value >= 0 && value <= 7) return dayNames[value];
	case SHORT_DAY:
		//lookup = LOCALE_SABBREVDAYNAME1+value-1; break;
		if (value >= 0 && value <= 7) return shortDayNames[value];
	case DAY_OF_WEEK:
		if (value >= 1 && value <= 7) {
			value = (((value-1)+getFirstDayOfWeek(id)-1)%7)+1;
			return localeGetStringValue(id,DAY,value,options);
		}
		break;
	case SHORT_DAY_OF_WEEK:
		if (value >= 1 && value <= 7) {
			value = (((value-1)+getFirstDayOfWeek(id)-1)%7)+1;
			return localeGetStringValue(id,SHORT_DAY,value,options);
		}
		break;
	case FIRST_DAY_OF_WEEK:
		{
			localeBuff[0] = getFirstDayOfWeek(id)+'0';
			localeBuff[1] = 0;
			return localeBuff;
		//TCHAR *ret = getLocaleInfo(id,LOCALE_IFIRSTDAYOFWEEK);
		//if (ret != NULL) (*ret)++;
		//return ret;
		}
	case SHORT_DATE_FORMAT:
		//lookup = LOCALE_SSHORTDATE; break;
		return "dd-MM-yy";
	case LONG_DATE_FORMAT:
		//lookup = LOCALE_SLONGDATE; break;
		return "E dd-MMMM-yyyy";
	case TIME_FORMAT:
		//lookup = LOCALE_STIMEFORMAT; break;
		return "hh:mm:ss";
	case AM_PM:
		if (value == 0) return "AM";
		else return "PM";

		lookup = LOCALE_S1159+value; break;


	case LANGUAGE:
		lookup = LOCALE_SLANGUAGE; break;
	case LANGUAGE_NATIVE:
		lookup = LOCALE_SNATIVELANGNAME; break;
	case LANGUAGE_ENGLISH:
		lookup = LOCALE_SENGLANGUAGE; break;
	case LANGUAGE_SHORT:{
		TCHAR *ret = getLocaleInfo(id,LOCALE_SABBREVLANGNAME);
		if (ret != NULL) ret[2] = 0;
		return ret;
						}
	case COUNTRY:
		lookup = LOCALE_SCOUNTRY; break;
	case COUNTRY_NATIVE:
		lookup = LOCALE_SNATIVECTRYNAME; break;
	case COUNTRY_ENGLISH:
		lookup = LOCALE_SENGCOUNTRY; break;
	case COUNTRY_SHORT:
		lookup = LOCALE_SABBREVCTRYNAME; break;
	case CURRENCY:
		lookup = LOCALE_SCURRENCY; break;
		default:
		return NULL;
	}
	if (lookup == -1) return NULL;
	//printf("Looking up: %d\n",lookup);
	return getLocaleInfo(id,lookup);
}
*/

static Var LocaleGetString(Var stack[])
	{
	Var v;
	WObject me = stack[0].obj;
	LCID myId = (LCID)objectPtr(me)[1].intValue;
	int what = stack[1].intValue;
	int value = stack[2].intValue;
	int options = stack[3].intValue;
	TCHAR *ret = localeGetStringValue(myId,what,value,options);
	if (ret == NULL) ret = dontKnow;
	v.obj = createStringFromJavaUtf8(strlen(ret),NULL,ret);
	return v;
}

#ifndef USE_EXTERNAL_CHAR_OPERATIONS
static Var CharacterOperation(Var stack[])
{
	Var v;
	int ret = 0;
	TCHAR ch = (TCHAR)stack[0].intValue;
	switch(stack[1].intValue){
	case 1:
		ret = isupper(ch); break;
	case 8: ret = 0; break;
	case 2: ret = islower(ch); break;
	case 3: ret = ch == ' '; break;
	case 4: ret = (ch >= 0x9 && ch <= 0xd) || (ch >= 0x1c && ch <= 0x1f) || ch == ' '; break;
	case 5: ret = isdigit(ch); break;
	case 6: ret = isalpha(ch); break;
	case 7: ret = isalnum(ch); break;

	case 101:
	case 103:
		ret = toupper(ch); break;
	case 102:
		ret = tolower(ch); break;
	}
	v.intValue = ret;
	return v;
}


TCHAR cstrOne, cstrTwo;
static uint16 changeCaseDefault(uint16 ch,int toUpper)
{
#ifdef IS_WINDOWS
#ifdef UNICODE
	TCHAR c = ch;
#else
	TCHAR c = (TCHAR)(ch & 0xff);
#endif
	if (toUpper) CharUpperBuff(&c,1);
	else CharLowerBuff(&c,1);
#ifdef UNICODE
	return c;
#else
	return (uint16)c & 0xff;
#endif
#else
	return (uint16)(toUpper ? toupper(ch) : tolower(ch));
#endif
}
static uint16 changeCase(WObject locale,uint16 ch,int toUpper)
{
	return changeCaseDefault(ch,toUpper);
}
#endif

static Var LocaleChangeCase(Var stack[])
{
	Var v;
	v.intValue = changeCase(stack[0].obj,(uint16)stack[1].intValue,stack[2].intValue);
	return v;
}

static Var LocaleChangeCaseArray(Var stack[])
{
	Var v;
	WObject array = stack[1].obj;
	int start = stack[2].intValue;
	int length = stack[3].intValue;
	int toUpper = stack[4].intValue;

	if (array == 0) return returnExError(ERR_NullArrayAccess);
	if (start < 0 || start+length > WOBJ_arrayLen(array)) return returnExError(ERR_IndexOutOfRange);
	else{
		uint16 *c = (uint16 *)(WOBJ_arrayStart(array))+start;
		int i;
		for (i = 0; i<length; i++,c++) *c = changeCase(stack[0].obj,*c,toUpper);
	}
	v.intValue = 1;
	return v;
}

static Var LocaleCompareChar(Var stack[])
{
	Var v;
	LCID id = objectPtr(stack[0].obj)[1].intValue;
	int flags = stack[3].intValue;
	int opts = (flags & 1) ? NORM_IGNORECASE : 0;

	cstrOne = (TCHAR) stack[1].intValue;
	cstrTwo = (TCHAR) stack[2].intValue;
	/*
	got = mCompareString(id,opts,&cstrOne,1,&cstrTwo,1);
	if (got == CSTR_EQUAL) v.intValue = 0;
	else if (got == CSTR_LESS_THAN) v.intValue = -1;
	else v.intValue = 1;
	*/
	v.intValue = compareTchars(id,opts,&cstrOne,1,&cstrTwo,1);
	return v;
}
static Var LocaleCompareString(Var stack[])
{
	Var v;
	LCID id = objectPtr(stack[0].obj)[1].intValue;
	int flags = stack[7].intValue;
	int opts = (flags & 1) ? NORM_IGNORECASE : 0;
	WObject one = stack[1].obj, two = stack[4].obj;
	int sOne = stack[2].intValue, sTwo = stack[5].intValue;
	int lenOne = stack[3].intValue, lenTwo = stack[6].intValue;

	if (one == two) v.intValue = 0;
	else if (one == 0) v.intValue = -1;
	else if (two == 0) v.intValue = 1;
	//Wildcard? (flags & 8) != 0
	else {
		WCHAR *o = (WCHAR *)WOBJ_arrayStart(one)+sOne;
		WCHAR *t = (WCHAR *)WOBJ_arrayStart(two)+sTwo;

		if ((flags & 8) == 0)
			v.intValue = compareStrings(
				o,lenOne,
				t,lenTwo,
				id,opts);
		else{
			int i = 0;
			WCHAR star = '*', question = '?';
			v.intValue = 0;
			for (i = 0; i<lenOne && i<lenTwo; i++, o++, t++){
				int ret = 0;
				if (*o == star || *t == star) return v;
				if (*o == question || *t == question) continue;
				ret = compareStrings(o,1,t,1,id,opts);
				if (ret == 0) continue;
				v.intValue = ret;
				return v;
			}
			v.intValue = lenOne-lenTwo;
			return v;
		}
	}
	return v;
}

struct byte_data fileBuffer;

static Var FileSysGetTempName(Var stack[])
{
	TCHAR *t = stringToNativeText(stack[0].obj);

}

static Var FileCreate(Var stack[])
{
	WObject parentFile = stack[1].obj;
	WObject childPath = stack[2].obj;
	int i;
	Var v;
	fileBuffer.length = 0;
	if (parentFile != 0)
		stringToUtf8(WOBJ_FileName(parentFile),&fileBuffer,0);
	if (fileBuffer.length != 0){
		int len = fileBuffer.length;
		if (fileBuffer.data[len-1] != '\\' && fileBuffer.data[len-1] != '/' && fileBuffer.data[len-1] != ':'){
			expandSpaceFor(&fileBuffer,len+2,10,1);
			fileBuffer.data[len] = '/';
			fileBuffer.data[len+1] = 0;
			fileBuffer.length++;
		}
	}
	if (childPath != 0)
		stringToUtf8(childPath,&fileBuffer,1);
	for (i = fileBuffer.length-1; i>=0; i--){
		char c = fileBuffer.data[i];
		if (c == '\\') c = fileBuffer.data[i] = '/';
		if (i == 0) break;
		if (c == '/' || c == '\\') {
			char c2 = fileBuffer.data[i-1];
			if (c2 != '/' && c2 != '\\' && c2 != ':'){
				fileBuffer.data[i] = 0;
				fileBuffer.length--;
			}
		}else break;
	}
again:
	char *p = (char *)fileBuffer.data;
	for (int i = 0; i<fileBuffer.length; i++, p++){
		if (*p == '.'){
			if (i != 0)
				if (*(p-1) != '/' && *(p-1) != ':') continue;
			if (i != fileBuffer.length-1)
				if (*(p+1) != '/') continue;
			char *cwd = getCwd();
			int len = strlen(cwd);
			int oldLen = fileBuffer.length;
			//printf("Was: %s\n",fileBuffer.data);
			expandSpaceFor(&fileBuffer,len+fileBuffer.length,20,1);
			memcpy(fileBuffer.data+i+1+len-1,fileBuffer.data+i+1,oldLen-i-1+1);
			memcpy(fileBuffer.data+i,cwd,len);
			//printf("Now: %s\n",fileBuffer.data);
			break;
		}
	}
	v.obj = createStringFromJavaUtf8(fileBuffer.length,NULL,fileBuffer.data);
	return v;
}

static Var FileSetInfo(Var stack[])
{
	Var v;
	int which = stack[1].intValue;
	v.intValue = 0;


	switch(which){
	case INFO_OWNER:
	case INFO_GROUP:
		{
		TCHAR *s = stringToNativeText(stack[2].obj);
		struct passwd *pw = getpwnam(s);
		free(s);
		if (pw == NULL) break;
		TCHAR *path = _FileAlloc(WOBJ_FileName(stack[0].obj),NULL);
		v.intValue = (which == INFO_OWNER ? chown(path,pw->pw_uid,(gid_t)-1) : chown(path,(uid_t)-1,pw->pw_uid)) == 0;
		_FileFree(path);
		break;
		}
	}
	return v;
}
//
//This is not a static method in File.
//
static Var FileGetInfo(Var stack[])
{
	Var v;
	int which = stack[1].intValue;
	v.obj = 0;


	switch(which){
	case INFO_OWNER:
	case INFO_GROUP:
		{
		TCHAR *path = _FileAlloc(WOBJ_FileName(stack[0].obj),NULL);
		struct stat st;
		if (stat(path,&st) == 0){
			struct passwd *pw = getpwuid(which == INFO_OWNER ? st.st_uid : st.st_gid);
			if (pw != NULL) v.obj = createStringFromUtf(createUtfString(pw->pw_name));
		}
		_FileFree(path);
		break;
		}
#ifdef CYGWIN_NEVER
	case INFO_ROOT_LIST:
		{
			WObject rt;
			v.obj = createArrayObject(arrayType('L'),1);
			if (!v.obj) return v;
			WOBJ_arrayComponent(v.obj) = tryGetClass(createUtfString("java/lang/String"));
			pushObject(v.obj);
			rt = createStringFromUtf(createUtfString("/cygdrive"));
			if (rt == 0){
				popObject();
				v.obj = 0;
				break;
			}
			((WObject *)WOBJ_arrayStart(v.obj))[0] = rt;
			popObject();
			break;
		}
#endif

/*
#ifndef WINCE
	case INFO_ROOT_LIST:
		{

			int len = GetLogicalDriveStrings(0,(TCHAR *)fileBuffer.data)+1;
			fileBuffer.length = 0;
			expandSpaceFor(&fileBuffer,len*sizeof(TCHAR),10,0);
			if (GetLogicalDriveStrings(len,(TCHAR *)fileBuffer.data) == 0) return v;
			else{
				TCHAR *p;
				WObject strarray;
				int size = 0;
				for (p = (TCHAR *)fileBuffer.data; *p != 0; p++){
					size++;
					while(*p != 0) p++;
				}
				v.obj = strarray = createArrayObject(arrayType('L'),size);
				if (!v.obj) return v;
				WOBJ_arrayComponent(v.obj) = tryGetClass(createUtfString("java/lang/String"));
				pushObject(strarray);
				size = 0;
				for (p = (TCHAR *)fileBuffer.data; *p != 0; p++){
					TCHAR *s = p;
					int lend = 0;
					while(*p != 0) p++, lend++;
					(WOBJ_arrayStart(strarray)+size)->obj = createStringFromUnicode(s,lend);
					if (!(WOBJ_arrayStart(strarray)+size)->obj) break;
					size++;
				}
				popObject();
			}
		}
		break;
#endif
	case INFO_TEMPORARY_DIRECTORY:
#ifndef WINCE
		{
			TCHAR * buff;
			int len = 0;

			len = GetTempPath(len,NULL);
			if (len == 0) break;
			buff = malloc(sizeof(TCHAR)*(len+1));
			len = GetTempPath(len+1,buff);
			if (len != 0)
				if (buff[len-1] == '\\' || buff[len-1] == '/')
					len--;
			v.obj = createStringFromUnicode(buff,len);
			free(buff);
			break;
		}
#endif
		*/
	case INFO_TEMPORARY_DIRECTORY:
		v.obj = createStringFromUtf(createUtfString("/tmp"));
		break;
	case INFO_PROGRAM_DIRECTORY:
		{
		TCHAR * ret = programDir;
		int len;
		if (*ret == 0)
			//ret = TEXT("./");
			ret = getCwd();
		if (*ret == 0)
			ret = myPath;
		len = textLength(ret);
		if (len > 1)
			if (ret[len-1] == '\\' || ret[len-1] == '/')
				len--;
		v.obj = createStringFromNativeText(ret,len);
		}
		break;
	}


	return v;
}

static Var FileGetFullPath(Var stack[])
{
	Var v;
	v.obj = WOBJ_FileName(stack[0].obj);
	return v;
}

static int compareFiletime(time_t one,time_t two)
{
	if (one > two) return 1;
	else if (one < two) return -1;
	else return 0;
}


#ifdef VCC
#include <io.h>
#include <sys/types.h>
#include <sys/stat.h>
static int getFileAttributes(FileListItem *dest,TCHAR *path)
{
	if (access(path,0) != 0)
		return 0;
	else{
		struct stat buff;
		stat(path,&buff);
		dest->dwFileAttributes = GetFileAttributes(path);
		dest->nFileSizeLow = buff.st_size;
		dest->ftLastWriteTime = buff.st_mtime;
	}
	return 1;
}

int doList(FileListItem **list,WObject file,WObject mask,int options)
{
	TCHAR *path, *use;
	WIN32_FIND_DATA findData;
	HANDLE findH;
	int numItems = 0;

	path = _FileAllocMask(WOBJ_FileName(file), mask);
	if (path == NULL) return 0;
	use = path;
	findH = FindFirstFile(use, &findData);
	_FileFree(path);
	if (findH == INVALID_HANDLE_VALUE) return 0;
	do {
		FileListItem cur;
		FileListItem *item;
		TCHAR *fileName = findData.cFileName;
		int len = textLength(fileName);
		int isDir = getFileAttributes2(&cur,use,fileName);
		if (isDir) isDir = (cur.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0;
			if (len == 1)
				if (fileName[0] == '.') continue;
			if (len == 2)
				if (fileName[0] == '.' && fileName[1] == '.') continue;

		if ((options & LIST_DIRECTORIES_ONLY) && !isDir) continue;
		if ((options & LIST_FILES_ONLY) && isDir) continue;

		if (options & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) {
			FindClose(findH);
			return 1;
		}

		item = (FileListItem *)xmalloc(sizeof(FileListItem));
		if (item == NULL)break;

		item->fileLength = lstrlen(fileName);
		item->fileName = (TCHAR *)malloc((item->fileLength + 1) * sizeof(TCHAR));
		if (item->fileName == NULL){
			free(item);
			break;
		}
		lstrcpy(item->fileName, findData.cFileName);
		item->dwFileAttributes = isDir ? FILE_ATTRIBUTE_DIRECTORY : 0;
		item->ftLastWriteTime = cur.ftLastWriteTime;
		item->nFileSizeLow = cur.nFileSizeLow;
		item->next = *list;
		*list = item;
		numItems++;
	} while (FindNextFile(findH, &findData));
	FindClose(findH);
	return numItems;
}
#else

#include <dirent.h>
#include <sys/stat.h>
typedef struct dirent *DirEntry;
typedef DIR *Directory;

#ifndef _A_NORMAL
#define _A_NORMAL 1
#define _A_SUBDIR 2
#define _A_HIDDEN 4

#ifndef _MAX_PATH
#define _MAX_PATH 1024
#define _MAX_DRIVE 256
#endif

#define _MAX_DIR 256
#define _MAX_FNAME 256
#define _MAX_EXT 256
#endif
/*
struct find_t
{
	char reserved[21];
	char attrib;
	int wr_time;
	int wr_date;
	long size;
	char name[_MAX_FNAME];
};
*/
//
// F_OK was in the man pages, but did not seem to be defined.
//
#ifndef F_OK
#define F_OK 0
#endif

static int getFileAttributes(FileListItem *dest,TCHAR *path)
{
	if (access(path,F_OK) != 0)
		return 0;
	else{
		struct stat buff;
		stat(path,&buff);
		dest->dwFileAttributes = S_ISDIR(buff.st_mode) ? FILE_ATTRIBUTE_DIRECTORY : 0;
		dest->nFileSizeLow = buff.st_size;
		dest->ftLastWriteTime = buff.st_mtime;
		return 1;
	}
}

int matches(const char *str,const char *mask)
{
	int s = 0, m = 0;
	if (!str) return 0;
	if (!mask) return 1;
	if (!strcmp(mask,"")) return 1;
	if(!strcmp(str,"")) return 0;

	while(1){
		if (mask[m] == '*') {
			if (mask[m+1] != '.') return 1;
			else if (mask[m+2] == '*') return 1;
			while(1) {
				if (str[s] == '.') break;
				if (str[s] == 0) return 0;
				s++;
			}
			m++;
			continue;
		}else if (mask[m] == '?') {
			if (str[s] == '.' || str[s] == 0) return 0;
			s++; m++;
			continue;
		}else {
			if (str[s] != mask[m]) return 0;
			if (str[s] == 0) return 1;
			s++; m++;
			continue;
		}
	}
}

int doList(FileListItem **list,WObject file,WObject mask,int options)
{
	TCHAR *path, *use, *maskStr = NULL;
	int numItems = 0;
	Directory dir;

	path = _FileAlloc(WOBJ_FileName(file), NULL);
	if (path == NULL) return 0;
	removeTrailingSlash(path);
	if (textLength(path) == 1 && path[0] == '.'){
		TCHAR *cwd = getCwd();
		dir = opendir(use = cwd);
	}else
		dir = opendir(use = path);
	if (dir == NULL) {
		_FileFree(path);
		return 0;
	}
	maskStr = stringToNativeText(mask);
	while(1){
		DirEntry de = readdir(dir);
		if (de == NULL) break;
		else{
			FileListItem cur;
			FileListItem *item;
			TCHAR *fileName = de->d_name;
			int len = textLength(de->d_name);
			int isDir = getFileAttributes2(&cur,use,fileName);
			if (isDir) isDir = (cur.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0;
			if (len == 1)
				if (fileName[0] == '.') continue;
			if (len == 2)
				if (fileName[0] == '.' && fileName[1] == '.') continue;

			if ((options & LIST_DIRECTORIES_ONLY) && !isDir) continue;
			if ((options & LIST_FILES_ONLY) && isDir) continue;

			if (!isDir)
				if (maskStr != NULL)
					if (!matches(fileName,maskStr))
						continue;

			if (options & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) {
				closedir(dir);
				_FileFree(path);
				if (maskStr != NULL) free(maskStr);
				return 1;
			}

			item = (FileListItem *)xmalloc(sizeof(FileListItem));
			if (item == NULL)break;

			item->fileLength = len;
			item->fileName = (TCHAR *)malloc((len + 1) * sizeof(TCHAR));
			if (item->fileName == NULL){
				free(item);
				break;
			}
			txtncpy(item->fileName, fileName, len);
			item->fileName[len] = 0;
			item->dwFileAttributes = isDir ? FILE_ATTRIBUTE_DIRECTORY : 0;
			item->ftLastWriteTime = cur.ftLastWriteTime;
			item->nFileSizeLow = cur.nFileSizeLow;
			item->next = *list;
			*list = item;
			numItems++;
		}
	}
	closedir(dir);
	_FileFree(path);
	if (maskStr != NULL) free(maskStr);
	return numItems;
}

#ifndef _WINGCC
#include <utime.h>
static void setModifiedTime(TCHAR *fullPath,time_t t)
{
	struct utimbuf tb;
	tb.actime = tb.modtime = t;
	utime(fullPath,&tb);
}
#endif
#endif

static Var VmIsColor(Var stack[]) {return returnVar(1);}

static Var VmGetPlatform(Var stack[])
	{
	Var v;
	v.obj = createString("Unix");
	return v;
	}


#ifdef IS_WINDOWS//VCC and WINGCC

void systemTimeToObject(WObject time,SYSTEMTIME *tm)
{
	WOBJ_TimeYear(time) = tm->wYear;
	WOBJ_TimeMonth(time) = tm->wMonth;
	WOBJ_TimeDay(time) = tm->wDay;
	WOBJ_TimeHour(time) = tm->wHour;
	WOBJ_TimeMinute(time) = tm->wMinute;
	WOBJ_TimeSecond(time) = tm->wSecond;
	WOBJ_TimeMillis(time) = tm->wMilliseconds;
	WOBJ_TimeDOW(time) = tm->wDayOfWeek;
	if (tm->wDayOfWeek == 0) WOBJ_TimeDOW(time) = 7; // Sunday is 7 in Waba.
}
void objectToSystemTime(WObject time,SYSTEMTIME *tm)
{
	tm->wYear = WOBJ_TimeYear(time);
	tm->wMonth = WOBJ_TimeMonth(time);
	tm->wDay = WOBJ_TimeDay(time);
	tm->wHour = WOBJ_TimeHour(time);
	tm->wMinute = WOBJ_TimeMinute(time);
	tm->wSecond = WOBJ_TimeSecond(time);
	tm->wMilliseconds = WOBJ_TimeMillis(time);
	tm->wDayOfWeek = WOBJ_TimeDOW(time);
	if (tm->wDayOfWeek == 7) tm->wDayOfWeek = 0; // Sunday is 0 in Win32.
}

static Var TimeCreate(Var stack[])
	{
	Var v;
	SYSTEMTIME tm;
	GetLocalTime(&tm);
	systemTimeToObject(stack[0].obj,&tm);
	v.obj = 0;
	return v;
	}
static int dim[] = {31,28,31,30,31,30,31,31,30,31,30,31};

static Var TimeSetDate(Var stack[])
{
	WObject time = stack[0].obj;
	int source = stack[1].intValue;
	int year = stack[2].intValue;
	int month = 0;

	for (;;year++){
		int leap = 1;
		int total = 365;
		if ((year % 4) != 0) leap = 0;
		else
			if (((year % 100) == 0) && ((year % 400) != 0)) leap =  0;
		if (leap) total = 366;
		if (source >= total) {
			source -= total;
			continue;
		}else{
			int m;
			month = 1;
			source++;
			for (m = 1; m<=12; m++){
				int dm = dim[m-1];
				if (leap && m == 2) dm++;
				if (source > dm) {
					month++;
					source -= dm;
				}else{
					WOBJ_TimeDay(time) = source;
					WOBJ_TimeMonth(time) = month;
					WOBJ_TimeYear(time) = year;
					return returnVar(0);
				}
			}
			//ewe.sys.Vm.debug("Should not be here!");
			return returnVar(0);
		}
	}
}

static Var setAndReturnLong(WObject lo,int64 value);

static BOOL gotEpoch = FALSE;
static int64 epoch = 0;

void getEpoch()
{
	SYSTEMTIME tm;
	tm.wYear = 1970;
	tm.wMonth = 1;
	tm.wDay = 1;
	tm.wDayOfWeek = 0;
	tm.wHour = 0;
	tm.wMilliseconds = 0;
	tm.wMinute = 0;
	tm.wSecond = 0;
	SystemTimeToFileTime(&tm,(FILETIME *)&epoch);
	epoch /= 10000L;
	gotEpoch = TRUE;
}
static int64 timeToMillis(WObject time,int convertLocal)
{
	SYSTEMTIME tm;
	FILETIME ft, lt;
	int64 *t = (int64 *)&lt;
	objectToSystemTime(time,&tm);
	if (!SystemTimeToFileTime(&tm,&ft))
		return -1L;
	if (!gotEpoch) getEpoch();
	if (convertLocal)
		LocalFileTimeToFileTime(&ft,&lt);
	else
		lt = ft;
	*t /= 10000L; //Convert to milliseconds.
	*t -= epoch;
	return *t;
}
static Var TimeGetTime(Var stack[])
{
	WObject time = stack[0].obj;
	int64 t = timeToMillis(time,0);
	return returnLong(t);
}

int64 getLong(WObject obj);

static void millisToTime(WObject time,int64 ml,int convert)
{
	SYSTEMTIME tm;
	FILETIME ft, *in = (FILETIME *)&ml;
	if (!gotEpoch) getEpoch();
	ml *= 10000L;
	ml += epoch*10000L;
	if (convert)
		FileTimeToLocalFileTime(in,&ft);
	else
		ft = *in;
	if (!FileTimeToSystemTime(&ft,&tm)) return;
	systemTimeToObject(time,&tm);
}
static Var TimeSetTime(Var stack[])
{
	Var v;
	WObject time = stack[0].obj;
	millisToTime(time,vars2int64(stack+1),0);
	v.obj = time;
	return v;
}
void toTime(WObject time,time_t t)
{
	int64 t2 = (int64)t;
	t2 *= 1000L;
	millisToTime(time,t2,1);
}
time_t fromTime(WObject time)
{
	int64 t = timeToMillis(time,1)/1000L;
	return (time_t)t;
}
void setModifiedTime(TCHAR *path,time_t tt)
{
	int64 value = (int64)tt*1000;
	FILETIME *ft = (FILETIME *)&value;
	if (!gotEpoch) getEpoch();
	value *= 10000L;
	value += epoch*10000L;
	if (path != NULL) {
		HANDLE fileH = CreateFile(path, GENERIC_WRITE, FILE_SHARE_READ, NULL,
			OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
		if (fileH != INVALID_HANDLE_VALUE){
			FILETIME file;
			int err;
			//LocalFileTimeToFileTime(ft,&file);
			file = *ft;
			SetFileTime(fileH,NULL,NULL,&file);
			err = GetLastError();
			CloseHandle(fileH);
		}
	}
}

#else // Unix

static void fromTimeVal(WObject wtime,struct timeval *atime)
{
	time_t atime_sec = atime->tv_sec;
	struct tm *now;

	now = localtime(&atime_sec);
	WOBJ_TimeYear(wtime) = now->tm_year+1900;
	WOBJ_TimeMonth(wtime) = now->tm_mon+1;
	WOBJ_TimeDay(wtime) = now->tm_mday;
	WOBJ_TimeHour(wtime) = now->tm_hour;
	WOBJ_TimeMinute(wtime) = now->tm_min;
	WOBJ_TimeSecond(wtime) = now->tm_sec;
	WOBJ_TimeMillis(wtime) = atime->tv_usec/1000;
	WOBJ_TimeDOW(wtime) = now->tm_wday;
	if (now->tm_wday == 0) WOBJ_TimeDOW(wtime) = 7; // Sunday is 7 in Ewe.
}
static void toTime(WObject wtime,time_t t)
{
	struct timeval tv;
	tv.tv_sec = t;
	tv.tv_usec = 0;
	fromTimeVal(wtime,&tv);
}
static void toTimeVal(WObject wtime,struct timeval *atime)
{
	struct tm nowtm;
	struct tm *now = &nowtm;
	now->tm_year = WOBJ_TimeYear(wtime)-1900;
	now->tm_mon = WOBJ_TimeMonth(wtime)-1;
	now->tm_mday = WOBJ_TimeDay(wtime);
	now->tm_hour = WOBJ_TimeHour(wtime);
	now->tm_min = WOBJ_TimeMinute(wtime);
	now->tm_sec = WOBJ_TimeSecond(wtime);
	now->tm_wday = WOBJ_TimeDOW(wtime);
	if (now->tm_wday == 7) now->tm_wday = 0;// Sunday is 7 in Ewe.
	atime->tv_sec = mktime(now);
	atime->tv_usec = WOBJ_TimeMillis(wtime)*1000;
}
static time_t fromTime(WObject wtime)
{
	struct timeval tv;
  toTimeVal(wtime,&tv);
  return tv.tv_sec;
}

#ifndef USE_EXTERNAL_TIME
static Var TimeCreate(Var stack[])
	{
	struct timeval tv;
	gettimeofday(&tv,NULL);
	fromTimeVal(stack[0].obj,&tv);
	return returnVar(1);
	}
/*
static Var TimeGetTime(Var stack[])
{
	struct timeval tv;
	int64 t;
	toTimeVal(stack[0].obj,&tv);
	t = (int64)tv.tv_sec*1000;
	t += tv.tv_usec/1000;
	return returnLong(t);
}
static Var TimeSetTime(Var stack[])
{
	struct timeval tv;
	int64 t = vars2int64(stack+1);
	tv.tv_sec = (int32)(t/1000L);
	tv.tv_usec = (int32)(t%1000L)*1000;
	fromTimeVal(stack[0].obj,&tv);
	return returnVar(1);
}
*/
static int dim[] = {31,28,31,30,31,30,31,31,30,31,30,31};

static Var TimeSetDate(Var stack[])
{
	WObject time = stack[0].obj;
	int source = stack[1].intValue;
	int year = stack[2].intValue;
	int month = 0;

	for (;;year++){
		int leap = 1;
		int total = 365;
		if ((year % 4) != 0) leap = 0;
		else
			if (((year % 100) == 0) && ((year % 400) != 0)) leap =  0;
		if (leap) total = 366;
		if (source >= total) {
			source -= total;
			continue;
		}else{
			int m;
			month = 1;
			source++;
			for (m = 1; m<=12; m++){
				int dm = dim[m-1];
				if (leap && m == 2) dm++;
				if (source > dm) {
					month++;
					source -= dm;
				}else{
					WOBJ_TimeDay(time) = source;
					WOBJ_TimeMonth(time) = month;
					WOBJ_TimeYear(time) = year;
					return returnVar(0);
				}
			}
			//ewe.sys.Vm.debug("Should not be here!");
			return returnVar(0);
		}
	}
}


#endif //USE_EXTERNAL_TIME
#endif //UNIX

#ifdef USING_WIN32_API
#include "nmwin32api.cpp"
#endif

#include <sys/ioctl.h>
#include <sys/poll.h>
#include <sys/socket.h>
//Definition of sockaddr_in
#include <netinet/in.h>
#include <netinet/tcp.h> //For TCP_NODELAY
#include <arpa/inet.h>
#include <netdb.h>

#if defined(LINUX) && defined(HAS_IR)
#include <linux/types.h>
#include <linux/irda.h>
#endif

static int isNetAddress(WObject host)
{
	UtfString s;
	if (host == 0) return 0;
	s = stringToUtf(host, STU_NULL_TERMINATE | STU_USE_STATIC);
	if (s.len == 0) return 0;
	return (inet_addr(s.str) != INADDR_NONE);
}
static Var IAIsNetAddress(Var stack[]) {return returnVar(isNetAddress(stack[0].obj));}

//
// Socket
//
// var[0] = Class
// var[1] = Error string from ewe.util.Errorable
// var[2] = Closed boolean from ewe.io.StreamObject
// var[3] = String hostName (either local or remote)
// var[4] = int portNumber (either local or remote)
// var[5] = inputStream
// var[6] = outputStream
// var[7] = inputIsClosed
// var[8] = outputIsClosed

#define WOBJ_NativeSocket(o) (objectPtr(o))[9].refValue
#define SOCKET int
#define SOCKET_IS_IR 0x1
#define SOCKET_IR_DEVICE_FOUND 0x2

void initSocket(SOCKET sock,int isServer)
{
	u_long argp = 1;
	ioctl(sock,FIONBIO,&argp);
}
static WObject getHost(struct sockaddr_in *addr)
{
	char *host = inet_ntoa(addr->sin_addr);
	UtfString hst;
	if (host == NULL) return 0;
	hst.str = host;
	hst.len = strlen(host);
	return createStringFromUtf(hst);
}



typedef class eweSocket *EweSocket;
static void setErrorableError(WObject errorable,char *err)
{
	WObject str = createStringFromUtf(createUtfString(err));
	objectPtr(errorable)[1].obj = str;
}
static Var setError(WObject errorable,int errValue,Var toReturn)
{
	char *err = "Unknown error.";
	switch(errValue){
		default:
			//if (errValue < sys_nerr) err = (char *)sys_errlist[errValue];
			err = strerror(errValue);
	}
	//printf("Error: %s\n",err);
	WObject str = createStringFromUtf(createUtfString(err));
	objectPtr(errorable)[1].obj = str;
	return toReturn;
}


class eweSocket{
public:
int socket;
int port;
int isServer;
int flags;
WObject object;
WObject sockToAccept;
int32 lastAccepted;
int attempt;

eweSocket(WObject obj,int server = 0)
{
	isServer = server;
	port = flags = 0;
	socket = -1;
	attempt = 0;
	object = obj;
	sockToAccept = 0;
	lastAccepted = getTimeStamp();
}

void setError(int errValue)
{
	Var v;
	v.obj = 0;
	::setError(object,errValue,v);
}
/*
	Check the IO state.
	Return 0 if not ready for IO, >=1 if it is ready for IO or -1 if an error has
	occured.
*/
// For Stream socket
#define CHECK_CONNECT 0
#define CHECK_READ 1
#define CHECK_WRITE 2
// For Server socket
#define CHECK_LISTEN 0
#define CHECK_ACCEPT 1

int checkIO(int forWhat)
{
	if (socket == -1) return -1;
	struct pollfd pfd;
	pfd.fd = socket;
	pfd.events = pfd.revents = 0;
	if (!isServer){
		if (forWhat == -1){ //Infra red client connect.
#ifdef HAS_IR
			irda_device_list devList;             // Device list
			sockaddr_irda address = {AF_IRDA, 0, 0, "EweIR"};
			int iDevListLen = sizeof (devList), index;
			devList.len = 0;
			if (getsockopt (socket, SOL_IRLMP, IRLMP_ENUMDEVICES,
				(char *)&devList, &iDevListLen) == -1){
					//perror("getsockopt()");
					if (errno == EAGAIN) return 0;
					return -1;
			}
			if (devList.len == 0) {
			//MessageBox (NULL, TEXT ("Nope!"),
			//		TEXT ("Not Found"), MB_OK|MB_SETFOREGROUND);
				//sprintf(sprintBuffer,"No: %d, ",++attempt);
				//printf(sprintBuffer);
				//fflush(stdout);
				return 0;
			}
		//MessageBox (NULL, TEXT ("Found someone!"),
		//		TEXT ("Connecting!"), MB_OK|MB_SETFOREGROUND);
			if (port != 0) sprintf(address.sir_name,"EweIR-%d",(port & 0xffff));
			address.sir_addr = devList.dev[0].daddr;
			//printf("Now connecting...\n");
      initSocket(socket,0);
		  connect(socket, (struct sockaddr *)&address, sizeof (sockaddr_irda));
      flags |= SOCKET_IR_DEVICE_FOUND;
			return 0;
#else
			return -1;
#endif
		}

		if (forWhat == CHECK_READ) pfd.events |= POLLIN|POLLPRI;
		else if (forWhat == CHECK_WRITE||forWhat == CHECK_CONNECT) pfd.events |= POLLOUT;
		int ret = poll(&pfd,1,0);
		if (ret != 0 && forWhat == CHECK_CONNECT){
			int err;
			socklen_t len = sizeof(int);
			getsockopt(socket,SOL_SOCKET,SO_ERROR,&err,&len);
			if (err == 0) return 1;
			setError(err);
			return -1;
		}
		if (ret == -1 || (pfd.revents & (POLLERR|POLLHUP|POLLNVAL)) != 0) return -1;
		return ret;
	}else{
		if (forWhat == CHECK_LISTEN) {
			return 1;
		}
		if (sockToAccept != 0) return 1;
		// Must be CHECK_ACCEPT. Check for a read event.
/*
		pfd.events = CHECK_READ
		int ret = poll(&pfd,1,0);
		if (ret == -1 || (pfd.revents & (POLLERR|POLLHUP|POLLNVAL)) != 0) return -1;
		if (ret == 0) return 0; // No connection waiting.
*/
		struct sockaddr_in addr;
		socklen_t len = sizeof(addr);

		int got = (flags & SOCKET_IS_IR) ? accept(socket,NULL,NULL) : accept(socket,(sockaddr *)&addr,&len);
		if (got == -1) {
			if (errno == EAGAIN) return 0;
			else {
				setError(errno);
				return -1;
			}
		}
		lastAccepted = getTimeStamp();
		WClass *wclass = getClass(createUtfString("ewe/net/Socket"));
		initSocket(got,0);
		sockToAccept = createObject(wclass);
		if (!sockToAccept) return 0;
		pushObject(sockToAccept);
		WObject obj = getHost(&addr);
		objectPtr(sockToAccept)[3].obj = obj;
		objectPtr(sockToAccept)[4].intValue = ntohs(addr.sin_port);
  	EweSocket es = new eweSocket(sockToAccept,false);
		WOBJ_NativeSocket(sockToAccept) = es;
		es->socket = got;
		holdObject(sockToAccept);
		popObject();
		return 1;
	}
}

int getLocalPort()
{
	if (socket == -1 || (flags & SOCKET_IS_IR)) return 0;
	struct sockaddr_in addr;
	socklen_t len = sizeof(addr);
	getsockname(socket,(struct sockaddr*)&addr,&len);
	return ntohs(addr.sin_port);
}
int close()
{
	if (socket != -1){
		shutdown(socket,2);
		::close(socket);
	}
	socket = -1;
	return 1;
}
~eweSocket()
{
	close();
}

};

static int32 _SocketClose(WObject obj)
	{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(obj);
	if (es == NULL) return 0;
	delete es;
	WOBJ_NativeSocket(obj) = NULL;
	return 1;
	}
static void SocketDestroy(WObject obj)
{
	_SocketClose(obj);
}
static void ServerSocketDestroy(WObject socket)
{
	_SocketClose(socket);
}

static Var SocketClose(Var stack[])
{
	_SocketClose(stack[0].obj);
	return returnVar(1);
	//return returnVar(_SocketClose(stack[0].obj));
}
static Var ServerSocketClose(Var stack[])
{
	return SocketClose(stack);
	//return returnVar(_SocketClose(stack[0].obj));
}

static Var SocketIsOpen(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	return returnVar(es != NULL);
}
static Var SocketGetLocalPort(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	return returnVar(es == NULL ? 0 : es->getLocalPort());
}
static Var SocketCheckIO(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return returnVar(-1);
	int checkType = stack[1].intValue;
	if ((es->flags & SOCKET_IS_IR)&& !es->isServer)
		if (!(es->flags & SOCKET_IR_DEVICE_FOUND))
			checkType = -1;
	return returnVar(es->checkIO(checkType));
}
static Var SocketPause(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return returnVar(0);
	if (stack[1].intValue == 2) return returnVar(0);
	//
	// Ready to read/write? If it is then return 0 - i.e. don't wait.
	//
	int ready = es->checkIO(stack[1].intValue ? CHECK_READ : CHECK_WRITE);
	if (ready) return returnVar(0);
	int time = stack[2].intValue;
	if (time < 0 || time > 100) return returnVar(100); //Wait 1 second max.
	return returnVar(time);
}
static int pipeIgnored = 0;

static Var SocketReadWriteBytes(Var stack[], int isRead)
	{
	WObject byteArray;
	SOCKET socketId;
	int32 start, count;//, countSoFar, n;
	uchar *bytes;
	Var v;
	int did;

	v.intValue = -1;
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return returnVar(-2);
	byteArray = stack[1].obj;
	start = stack[2].intValue;
	count = stack[3].intValue;
	socketId = es->socket;
	if (socketId == -1) return v; // socket not open
	if (arrayRangeCheck(byteArray, start, count) == 0)
		return v; // array null or range invalid
	bytes = (uchar *)WOBJ_arrayStart(byteArray);
	bytes = &bytes[start];

	if (!pipeIgnored) {
		signal(SIGPIPE,SIG_IGN);
		pipeIgnored = 1;
	}

	did = isRead ?
		recv(socketId,(char *)bytes, count, 0)://MSG_NOSIGNAL) :
		send(socketId,(char *)bytes, count, 0);//MSG_NOSIGNAL) ; <- This caused an invalid argument error on the Zaurus
 /*
	printf("did: %d, count: %d, socketId: %d, bytes: %x\n",did,count,socketId,(int)((char *)bytes));
	if (did != count && !isRead){
		perror("send()");
	}
*/
	if (did == -1) {
		if ((errno == EPIPE) ||
			(errno == 104 && (es->flags & SOCKET_IS_IR))) //Closing an IR socket results in this.
			did = -1;
		else if (errno == EAGAIN)
			did = 0;
		else{
			did = -2;
			es->setError(errno);
		}
	}else if (did == 0){
		did = -1;
	}

	v.intValue = did;
	return v;
	}

static Var SocketRead(Var stack[])
	{
	return SocketReadWriteBytes(stack, 1);
	}

static Var SocketWrite(Var stack[])
	{
	return SocketReadWriteBytes(stack, 0);
	}

#ifndef MAX_TRY_SOCKET
#define MAX_TRY_SOCKET 2
#endif

//
// Under Red Hat Linux 7.xx socket() can sometimes return descriptor 0. I have
// read of at least one other individual who has come across this problem, but
// I have yet to see a resolution or a reason for it.
//
// As a workaround, I simply ignore it and try again.
//
static int mySocket(int domain, int type, int protocol)
{
	for(int i = 0; i<MAX_TRY_SOCKET; i++){
		int socketId = socket(domain,type,protocol);
		if (socketId != 0) return socketId;
		//printf("Zero socket!\n");
	}
	return -1;
}

static Var SocketCreate(Var stack[])
	{
	WObject sock, host, localHost;
	int32 port, status, localPort;
	SOCKET socketId;
	UtfString s;
	unsigned long ipAddr;
	struct sockaddr_in sockAddr, localAddr;
	struct hostent *hostEnt;
	Var v;

	v.obj = 0;
	sock = stack[0].obj;
	host = stack[1].obj;
	port = stack[2].intValue;
	localHost = stack[3].obj;
	localPort = stack[4].intValue;

	WOBJ_NativeSocket(sock) = NULL;

	//if (!startWinsock()) return v;
	//
	// See if a bind to a local address is needed.
	//
	if (localHost != 0){
		s = stringToUtf(localHost, STU_NULL_TERMINATE | STU_USE_STATIC);
		xmemzero(&localAddr, sizeof(localAddr));
		localAddr.sin_family = AF_INET;
		localAddr.sin_port = htons((u_short)localPort);
		hostEnt = gethostbyname(s.str);
		if (hostEnt == NULL){
			setErrorableError(sock,"Could not bind to local host.");
			return v;
		}
		xmemmove(&localAddr.sin_addr, hostEnt->h_addr_list[0], hostEnt->h_length);
	}

	xmemzero(&sockAddr, sizeof(sockAddr));
	sockAddr.sin_family = AF_INET;
	sockAddr.sin_port = htons((u_short)port);
	s = stringToUtf(host, STU_NULL_TERMINATE | STU_USE_STATIC);
	if (s.len == 0) return v;

	if (strlen(s.str) >= 9)
	if (strncmp(s.str,"infra-red",9) == 0){
#ifndef HAS_IR
	return v;
#else
  // Create a socket that is bound to the server.
  if ((socketId = mySocket(AF_IRDA, SOCK_STREAM, 0)) == -1)
  {
		return setError(sock,errno,v);
  }

	EweSocket es = new eweSocket(sock);
	WOBJ_NativeSocket(sock) = es;
	es->socket = socketId;
	es->flags |= SOCKET_IS_IR;
	es->port = port;
	v.intValue = 1;
	return v;
#endif
}
	ipAddr = inet_addr(s.str);
	if (ipAddr != INADDR_NONE)
		xmemmove(&sockAddr.sin_addr, &ipAddr, sizeof(unsigned long));
	else
		{
		hostEnt = gethostbyname(s.str);
		if (hostEnt == NULL)
			return v;
		xmemmove(&sockAddr.sin_addr, hostEnt->h_addr_list[0], hostEnt->h_length);
		}

	socketId = mySocket(PF_INET, SOCK_STREAM, 0);
	if (socketId == -1)
		return setError(sock,errno,v);
	if (localHost != 0 && bind(socketId,(struct sockaddr *)&localAddr,sizeof(localAddr)) != 0){
		close(socketId);
		return setError(sock,errno,v);
	}
	EweSocket es = new eweSocket(sock);
	WOBJ_NativeSocket(sock) = es;
	es->socket = socketId;
	initSocket(socketId,0);
	status = connect(socketId, (struct sockaddr *)&sockAddr, sizeof(sockAddr));
	if (status == -1) {
		if (errno == EINPROGRESS) v.intValue = 1; //Good - will wait for it later.
		else {
			es->setError(errno);
			v.intValue = 0;
		}
	}
	else
		v.intValue = 1;
	return v;
	}
static Var SocketGetLocalHost(Var stack[])
{
	Var v;
	v.obj = 0;
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return v;
	struct sockaddr_in addr;
	socklen_t len = sizeof(addr);
	getsockname(es->socket,(struct sockaddr*)&addr,&len);
	v.obj = getHost(&addr);
	return v;
}
#define ETCP_NODELAY  0x1
#define ESO_LINGER  0x2
#define ESO_TIMEOUT  0x3
#define ERX_BUFFERSIZE  0x4
#define ETX_BUFFERSIZE  0x5
#define ESO_KEEPALIVE  0x6

static Var DSGetSetBufferSize(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	int isReceive = stack[1].intValue;
	int bs = stack[2].intValue;
	int isGet = stack[3].intValue;
	int ret = bs;
	if (es == NULL) return returnVar(ret);
	SOCKET s = es->socket;
	socklen_t size = sizeof(ret);
	if (isGet)
		getsockopt(s,SOL_SOCKET,isReceive ? SO_RCVBUF : SO_SNDBUF,(char *)&ret,&size);
	else
		setsockopt(s,SOL_SOCKET,isReceive ? SO_RCVBUF : SO_SNDBUF,(char *)&ret,size);
	return returnVar(ret);
}

static Var SocketGetSetParameter(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return returnVar(0);
	int par = stack[1].intValue;
	int bvalue = stack[2].intValue;
	int ivalue = stack[3].intValue;
	int isGet = stack[4].intValue;
	int ret = 0;
	socklen_t size = sizeof(ret);
	struct linger l;
	l.l_linger = ivalue;
	l.l_onoff = bvalue;
	SOCKET s = es->socket;
	if (es == NULL) return returnVar(ret);


	if (isGet){
		switch(par){
		case ETCP_NODELAY: getsockopt(s,IPPROTO_TCP,TCP_NODELAY,(char *)&ret,&size); break;
		case ESO_LINGER:
			size = sizeof(l);
			getsockopt(s,SOL_SOCKET,SO_LINGER,(char *)&l,&size);
			ret = l.l_onoff == 0 ? -1 : l.l_linger;
			break;
		case ESO_TIMEOUT: break;    //getsockopt(s,IPPROTO_TCP,TCP_NODELAY,(char *)&ret,&size); break;
		case ERX_BUFFERSIZE: getsockopt(s,SOL_SOCKET,SO_RCVBUF,(char *)&ret,&size); break;
		case ETX_BUFFERSIZE: getsockopt(s,SOL_SOCKET,SO_SNDBUF,(char *)&ret,&size); break;
		case ESO_KEEPALIVE:  getsockopt(s,SOL_SOCKET,SO_KEEPALIVE,(char *)&ret,&size); break;
		}
		return returnVar(ret);
	}else{
		switch(par){
		case ETCP_NODELAY: ret = bvalue; setsockopt(s,IPPROTO_TCP,TCP_NODELAY,(char *)&ret,size); break;
		case ESO_LINGER:
			size = sizeof(l);
			setsockopt(s,SOL_SOCKET,SO_LINGER,(const char *)&l,size);
			break;
		case ESO_TIMEOUT: break;   //setsockopt(s,IPPROTO_TCP,TCP_NODELAY,(char *)&ret,size); break;
		case ERX_BUFFERSIZE: ret = ivalue; setsockopt(s,SOL_SOCKET,SO_RCVBUF,(char *)&ret,size); break;
		case ETX_BUFFERSIZE: ret = ivalue; setsockopt(s,SOL_SOCKET,SO_SNDBUF,(char *)&ret,size); break;
		case ESO_KEEPALIVE:  ret = bvalue; setsockopt(s,SOL_SOCKET,SO_KEEPALIVE,(char *)&ret,size); break;
		}
		return returnVar(1);
	}
}

static Var SSCheckIO(Var stack[])
{
	return SocketCheckIO(stack);
}
static Var SSPause(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return returnVar(0);
	if (es->checkIO(CHECK_ACCEPT) > 0) return returnVar(0);
	int time = stack[1].intValue;
	if (time == 0) return returnVar(time);
  //Effectively will go to sleep for 1 second if not accepted for 1 minute.
	//if (timeDifference(es->lastAccepted,getTimeStamp()) >= 60000) return returnVar(100);
	if (timeDifference(es->lastAccepted,getTimeStamp()) >= 1000) return returnVar(100);
	return returnVar(0);
}

static Var SSGetAcceptedSocket(Var stack[])
{
	Var v;
	v.obj = 0;
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return v;
	v.obj = es->sockToAccept;
	releaseObject(es->sockToAccept);
	es->sockToAccept = 0;
	return v;
}
static Var SSGetLocalPort(Var stack[])
{
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	if (es == NULL) return returnVar(0);
	if (!(es->flags & SOCKET_IS_IR))
		return SocketGetLocalPort(stack);
	else return returnVar(es->port);
}

int lastIRPort = 1023;

static Var ServerDatagramCreate(Var stack[],int isDatagram)
	{
	WObject sock, host;
	int32 port, status, back;
	SOCKET socketId;
	UtfString s;
	unsigned long ipAddr;
	struct sockaddr_in sockAddr;
	struct hostent *hostEnt;
	int type = isDatagram ? SOCK_DGRAM : SOCK_STREAM;
	socklen_t slen;
	Var v;
	int isIR = 0;

	v.obj = 0;
	sock = stack[0].obj;
	port = stack[1].intValue;
	back = stack[2].intValue;
	host = stack[3].intValue;

	if (back == 0) back = SOMAXCONN;
	WOBJ_NativeSocket(sock) = NULL;

	xmemzero(&sockAddr, sizeof(sockAddr));
	sockAddr.sin_family = AF_INET;
	sockAddr.sin_port = htons((u_short)port);
	s = stringToUtf(host, STU_NULL_TERMINATE | STU_USE_STATIC);
	//if (s.len == 0)
	//	return v;

	if (strlen(s.str) >= 9)
	if (strncmp(s.str,"infra-red",9) == 0){
#ifndef HAS_IR
	v.obj = createStringFromUtf(createUtfString("Infra-Red not supported"));
	return v;
#else
		bool findPort = port == 0;

		//TCHAR szError[100];             // Error message string
		//SOCKADDR_IRDA address = {AF_IRDA, 0, 0, 0, 0, "IRServer"};
		sockaddr_irda address = {AF_IRDA,LSAP_ANY,0,"EweIR"};
		int index = 0;
		//int32 * p = (int32 *)&address.irdaDeviceID[0];
		//*p = port;
		isIR = 1;

		socketId = mySocket(AF_IRDA, type, 0);
		if (socketId == -1) {
			v.obj = createStringFromUtf(createUtfString("Could not create infra-red socket."));
			return v;
		}
		if (findPort){
			port = (lastIRPort+1) & 0xffff;
			if (port<1024) port = 1024;
		}
		while(1){
			sprintf(address.sir_name,"EweIR-%d",(port & 0xffff));
			if (bind (socketId, (struct sockaddr *)&address, sizeof (address))
				== 0) break;
			if (!findPort) {
				v.obj = createStringFromUtf(createUtfString("Could not bind to port."));
				return v;
			}
			port = (port+1) & 0xffff;
			if (port<1024) port = 1024;
			if (port == lastIRPort){
				v.obj = createStringFromUtf(createUtfString("Could not find available port."));
				return v; //Can't find any more.
			}
		}
		if (findPort) lastIRPort = port;
/*
		if (port != 0) sprintf(address.sir_name,"EweIR-%d",(port & 0xffff));
		if (bind (socketId, (struct sockaddr *)&address, sizeof (address))
		  != 0) {
			perror("bind()");
			close(socketId);
			return v;
  		}
*/
	goto gotSock;
#endif
	}
	if (s.len != 0) ipAddr = inet_addr(s.str);
	else ipAddr = INADDR_ANY;

	if (ipAddr != INADDR_NONE)
		xmemmove(&sockAddr.sin_addr, &ipAddr, sizeof(unsigned long));
	else
		{
		hostEnt = gethostbyname(s.str);
		if (hostEnt == NULL){
			v.obj = createStringFromUtf(createUtfString("Could not bind to host address."));
			return v;
		}
		xmemmove(&sockAddr.sin_addr, hostEnt->h_addr_list[0], hostEnt->h_length);
		}

		socketId = mySocket(PF_INET, type, 0);
		if (socketId == -1){
			v.obj = createStringFromUtf(createUtfString("Could not create new socket."));
			return v;
		}

	if (bind(socketId,(struct sockaddr *)&sockAddr,sizeof(sockAddr)) != 0) {
		close(socketId);
		v.obj = createStringFromUtf(createUtfString("Could not bind to port."));
		return v;
	}
	slen = sizeof(sockAddr);
	getsockname(socketId,(struct sockaddr *)&sockAddr,&slen);
	//WOBJ_SocketPort(sock) = ntohs(sockAddr.sin_port);

#ifdef HAS_IR
gotSock:
#endif

	initSocket(socketId,1);
	if (!isDatagram){
		status = listen(socketId,back);
		if (status != 0){
			close(socketId);
			v.obj = createStringFromUtf(createUtfString("Could not listen to socket"));
			return v;
		}
	}else{// isDatagram
		int allowBroadcast = 1;
		setsockopt(socketId,SOL_SOCKET,SO_BROADCAST,&allowBroadcast,sizeof(allowBroadcast));
	}
	EweSocket es = new eweSocket(sock,true);
	WOBJ_NativeSocket(sock) = es;
	es->socket = socketId;
	es->port = port;
	if (isIR) es->flags |= SOCKET_IS_IR;
	v.intValue = 0;
	return v;
}
static Var SSCreate(Var stack[])
{
	return	ServerDatagramCreate(stack,0);
}
static Var DSCreate(Var stack[])
{
	return	ServerDatagramCreate(stack,1);
}
static Var DSPause(Var stack[])
{
	return SocketPause(stack);//returnVar(stack[2].intValue);
}

int getNumAddresses(struct hostent * he)
{
	int i = 0;
	for (i = 0; he->h_addr_list[i] != NULL; i++)
		;
	return i;
}

WClass * IAClass = NULL;
WObject toNewIA(WObject oldIA, struct sockaddr_in *sockAddr,int isIR)
{
	UtfString addr;
	WObject ia = oldIA, str;
	if (ia == 0) {
		if (IAClass == NULL){
			IAClass = getClass(createUtfString("ewe/net/InetAddress"));
		}
		if (IAClass == NULL) return 0;
		ia = createObject(IAClass);
		if (ia == 0) return 0;
	}
	pushObject(ia);
	if (!isIR){
		addr.str = inet_ntoa(sockAddr->sin_addr);
		addr.len = strlen(addr.str);
	}else{
		addr = createUtfString("infra-red");
	}
	str = createStringFromUtf(addr);
	objectPtr(ia)[1].obj = objectPtr(ia)[2].obj = str;
	popObject();
	return ia;
}

WObject getIA(struct hostent * he,int index)
{
	WObject ia;
	WObject str;
	struct sockaddr_in sockAddr;

	xmemmove(&sockAddr.sin_addr, he->h_addr_list[index], he->h_length);
	ia = toNewIA(0,&sockAddr,0);
	if (ia == 0) return 0;
	pushObject(ia);
	str = createStringFromUtf(createUtfString(he->h_name));
	objectPtr(ia)[1].obj = str;
	popObject();
	return ia;
}
WObject getAllIA(struct hostent * he)
{
	WObject ret;
	int num,i;
	if (he == NULL) return 0;
	num = getNumAddresses(he);
	if (num < 1) return 0;
	ret = createArrayObject(arrayType('L'),num);
	if (!ret) return 0;
	WOBJ_arrayComponent(ret) = tryGetClass(createUtfString("ewe/net/InetAddress"));
	pushObject(ret);
	for (i = 0; i<num; i++){
		WObject got = getIA(he,i);
		WOBJ_arrayStart(ret)[i].obj = got;
	}
	popObject();
	return ret;
}
static void * LookupThread(void *data)
{
	WObject handle = (WObject)data;
	UtfString str;
	struct hostent *he = NULL;
	char *lookup;
	LOCKTHREAD
	str = stringToUtf(getHandleValue(handle),STU_NULL_TERMINATE|STU_USE_STATIC);
	lookup = (char *)malloc(strlen(str.str)+1);
	strcpy(lookup,str.str);
	UNLOCKTHREAD
	he = gethostbyname(lookup);
	free(lookup);
	LOCKTHREAD
		if (he != NULL){
			WObject ret = getAllIA(he);
			setHandle(handle,Succeeded,ret);
		}else
			setHandle(handle,Failed,0);
	releaseObject(handle);
	UNLOCKTHREAD
	return NULL;
}
#ifdef USE_PTHREADS
class lookup_thread : public mThread {
public:
	lookup_thread(void *data) : mThread(data){}
	void run(void *data){
		nap(100); //Why? Because there seems to be a problem with gethostbyname() with pthreads.
		LookupThread(data);
	}
};
#endif

static Var IAGetLocalHost(Var stack[])
{
	Var v;
	char hn[256];
	v.obj = 0;

	if (gethostname(hn,256) == 0)
		v.obj = createStringFromUtf(createUtfString(hn));
	return v;
}
static Var IAGetAllByName(Var stack[])
{
	Var v;
	WObject handle = stack[1].obj;
	v.intValue = 0;
	if (handle == 0) return v;
	holdObject(handle);
	objectPtr(handle)[2].obj = stack[0].obj;
#ifdef USE_PTHREADS
	(new lookup_thread((void *)handle))->startRunning(1);
#else
	releaseObject(handle);
	LookupThread(handle);
#endif
	return v;
}
static Var DSSend(Var stack[])
{
	WObject socket, datagram;
	Var v;
	v.obj = 0;
	socket = stack[0].obj;
	datagram = stack[1].obj;
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	int socketId = es == NULL ? -1 : es->socket;
	if (socketId == -1)
		v.obj = createStringFromUtf(createUtfString("Socket closed."));
	else{
		int len = objectPtr(datagram)[2].intValue;
		int toSend = objectPtr(datagram)[2].intValue;
		unsigned char *data = (unsigned char *)WOBJ_arrayStart(objectPtr(datagram)[1].obj);
		if (es->flags & SOCKET_IS_IR){
#ifdef HAS_IR
			sockaddr_irda address = {AF_IRDA, 0, 0, 0, 0, "EweIR"};
			int sent = 0;
			sprintf(address.sir_name,"EweIR-%d",objectPtr(datagram)[4].intValue);
			if ((sent = sendto(socketId,data,toSend,0,(struct sockaddr *)&address,sizeof(address))) == -1){
				if (errno == EAGAIN) v.obj = 0;
				else v.obj = createStringFromUtf(createUtfString("Socket write error."));
			}if (sent != toSend){
				v.obj = createStringFromUtf(createUtfString("Full data not sent."));
			}else
				v.obj = datagram;
#endif
		}else{
			struct sockaddr_in address;
			int socklen = sizeof(address), sent;
			unsigned long ipAddr;
			WObject addr = objectPtr(datagram)[3].obj;
			UtfString s = stringToUtf(objectPtr(addr)[2].obj, STU_NULL_TERMINATE | STU_USE_STATIC);
			ipAddr = inet_addr(s.str);
			xmemzero(&address,sizeof(address));
			address.sin_family = AF_INET;
			address.sin_port =  htons((u_short)objectPtr(datagram)[4].intValue);
			if (ipAddr != INADDR_NONE)
				xmemmove(&address.sin_addr,&ipAddr,sizeof(unsigned long));
			else{
				struct hostent *hostEnt = gethostbyname(s.str);
				if (hostEnt == NULL){
					v.obj = createStringFromUtf(createUtfString("Bad host address."));
					return v;
				}
				xmemmove(&address.sin_addr, hostEnt->h_addr_list[0], hostEnt->h_length);
			}
			if ((sent = sendto(socketId,data,toSend,0,(struct sockaddr *)&address,sizeof(address))) == -1){
				if (errno == EAGAIN) v.obj = 0;
				else v.obj = createStringFromUtf(createUtfString("Socket write error."));
			}if (sent != toSend){
				v.obj = createStringFromUtf(createUtfString("Full data not sent."));
			}else
				v.obj = datagram;
		}
	}
	return v;
}
static Var DSReceive(Var stack[])
{
	WObject socket, datagram;
	Var v;
	v.obj = 0;
	socket = stack[0].obj;
	datagram = stack[1].obj;
	EweSocket es = (EweSocket)WOBJ_NativeSocket(stack[0].obj);
	int socketId = es == NULL ? -1 : es->socket;
	if (socketId == -1)
		v.obj = createStringFromUtf(createUtfString("Socket closed."));
	else{
		int len = objectPtr(datagram)[2].intValue;
		unsigned char *data = (unsigned char *)WOBJ_arrayStart(objectPtr(datagram)[1].obj);
		if (es->flags & SOCKET_IS_IR){
#ifdef HAS_IR
			sockaddr_irda address = {AF_IRDA, 0, 0, 0, 0, "EweIR"};
			socklen_t socklen = sizeof(address);
			int read = recvfrom(socketId,data,len,0,(struct sockaddr *)&address,&socklen);
			if (read > 0){
				WObject addr = objectPtr(datagram)[3].obj;
				int port = 0;
				objectPtr(datagram)[2].intValue = read;
				addr = toNewIA(addr,NULL,1);
				objectPtr(datagram)[3].obj = addr;
				if (strncmp("EweIR-",address.sir_name,6) == 0)
					sscanf(address.sir_name+6,"%d",&port);
				objectPtr(datagram)[4].intValue = port;
				v.obj = datagram;
			}else if (read == -1){
				if (errno == EAGAIN) v.obj = 0;
				else v.obj = createStringFromUtf(createUtfString("Socket read error."));
			}else{
				v.obj = createStringFromUtf(createUtfString("Socket closed."));
			}
#endif
		}else{
			struct sockaddr_in address;
			socklen_t socklen = sizeof(address);
			int read = recvfrom(socketId,data,len,0,(struct sockaddr *)&address,&socklen);
			if (read > 0){
				WObject addr = objectPtr(datagram)[3].obj;
				objectPtr(datagram)[2].intValue = read;
				addr = toNewIA(addr,&address,0);
				objectPtr(datagram)[3].obj = addr;
				objectPtr(datagram)[4].intValue = ntohs(address.sin_port);
				v.obj = datagram;
			}else if (read == -1){
				if (errno == EAGAIN) v.obj = 0;
				else v.obj = createStringFromUtf(createUtfString("Socket read error."));
			}else{
				v.obj = createStringFromUtf(createUtfString("Socket closed."));
			}
		}
	}
	return v;
}


static Var VmGetUserName(Var stack[])
{
	Var v;
	v.obj = 0;
	char *nm = getlogin();
	if (nm != NULL) v.obj = createStringFromUtf(createUtfString(nm));
	return v;
}
#define WOBJ_PipeNative(OBJ) objectPtr(OBJ)[3].intValue

int setError(WObject errorable,char *error,int ret)
{
	objectPtr(errorable)[1].obj = createStringFromUtf(createUtfString(error));
	return ret;
}

static WObject setupPipe(WObject pipe,int fd)
{
	static WClass *wc = NULL;
	u_long argp = 1;
	//printf("Starting!\n");
	if (wc == NULL) wc = tryGetClass(createUtfString("ewe/sys/pipeStream"));
	if (wc == NULL) return pipe;
	if (pipe == 0) pipe = createObject(wc);
	if (pipe == 0) return 0;
	pushObject(pipe);
	WOBJ_PipeNative(pipe) = fd;
	ioctl(fd,FIONBIO,&argp);
//	perror("ioctl()");
	popObject();
	return pipe;
}

void PipeDestroy(WObject obj)
{
	int fd = WOBJ_PipeNative(obj);
	if (fd == -1) return;
	close(fd);
	WOBJ_PipeNative(obj) = -1;
}
static Var PipeClose(Var stack[])
{
	PipeDestroy(stack[0].obj);
	return returnVar(1);
}
static Var PipeSeek(Var stack[])
{
	int fd = WOBJ_PipeNative(stack[0].obj);
	if (fd == -1) return returnVar(0);
	int ret = lseek(fd,stack[1].intValue,SEEK_SET) != -1;
 	return returnVar(ret);
}
static Var PipeFlush(Var stack[])
{
	int fd = WOBJ_PipeNative(stack[0].obj);
	if (fd == -1) return returnException("ewe/io/IOException","File not open.");
	if (fsync(fd) != 0) return returnException("ewe/io/IOException","Flush error.");
	return returnVar(1);
}
static Var PipeSetLength(Var stack[])
{
	int fd = WOBJ_PipeNative(stack[0].obj);
	if (fd == -1) return returnVar(-1);
	return returnVar((ftruncate(fd,(off_t)vars2int64(stack+1)) == 0) ? 1 : -1);
}
static Var PipeTell(Var stack[])
{
	int fd = WOBJ_PipeNative(stack[0].obj);
	if (fd == -1) return returnVar(0);
 	return returnVar(lseek(fd,0,SEEK_CUR));
}
static Var PipeGetLength(Var stack[])
{
	int fd = WOBJ_PipeNative(stack[0].obj);
	if (fd == -1) return returnVar(0);
	struct stat st;
	if (fstat(fd,&st) != 0) return returnVar(-1);
 	return returnVar(st.st_size);
}
static void ignoreSigPipe()
{
 	if (!pipeIgnored) {
		signal(SIGPIPE,SIG_IGN);
		pipeIgnored = 1;
	}
}

static Var PipeReadWrite(Var stack[],int isRead)
{
	int fd = WOBJ_PipeNative(stack[0].obj);
	if (fd == -1) return returnVar(setError(stack[0].obj,"Stream closed.",-2));//returnVar(0);
	char *buf = (char *)WOBJ_arrayStart(stack[1].obj)+stack[2].intValue;
	int count = stack[3].intValue;
 	if (!pipeIgnored) ignoreSigPipe();
	if (isRead){
		int readin = read(fd,buf,count);
		int ret = 0;
		if (readin == 0)  ret = -1;
		else if (readin > 0) ret = readin;
		else if (errno == EAGAIN) {
			//printf("."); fflush(stdout);
			ret = 0;
		}
		//else if (errno == EPIPE) ret = -1;
		else ret = setError(stack[0].obj,strerror(errno),-2);
		return returnVar(ret);
	}else {
		int wrote = write(fd,buf,count);
		int ret = 0;
		if (wrote == 0)  ret = -1;
		else if (wrote > 0) ret = wrote;
		else if (errno == EAGAIN) {
			//printf("."); fflush(stdout);
			ret = 0;
		}
		//else if (errno == EPIPE) ret = -1;
		else ret = setError(stack[0].obj,strerror(errno),-2);
		return returnVar(ret);
	}
}
static Var PipeReadWriteV(Var stack[])
{
	return PipeReadWrite(stack,stack[4].intValue);
}
static Var VmGetStream(Var stack[])
{
	WObject str = stack[0].obj;
	int which = stack[1].intValue;
	int fd = -1;
	Var v;
	v.obj = str;
	if (str != 0) return v;
	switch(which){
	case 0: fd = 0; break;
	case 1: fd = 1; break;
	case 2: fd = 2; break;
	}
	if (fd == -1) return v;
	v.obj = setupPipe(0,fd);
	return v;
}
//** Read-only open mode.
#define READ_ONLY  1
//** Write-only open mode.
#define WRITE_ONLY 2
//** Read-write open mode.
#define READ_WRITE 3 // READ | WRITE
//** Create open mode. Used to create a file if one does not exist.
#define CREATE  4

#ifdef USE_FD_FOR_RAF

#define RafileClose PipeClose
#define RafileDestroy PipeDestroy
#define RafileGetLength PipeGetLength
#define RafileSeek PipeSeek
#define RafileGetPosition PipeTell
#define RafileSetLength PipeSetLength
#define RafileFlush PipeFlush

static Var RafileRead(Var stack[])
{
	return PipeReadWrite(stack,1);
}
static Var RafileWrite(Var stack[])
{
	return PipeReadWrite(stack,0);
}

static Var RafileCreate(Var stack[])
{
	WObject file = stack[0].obj;
	WObject str = stack[1].obj;
	int mode = stack[2].intValue;
	if (str == 0) return returnException(NullPointerEx,NULL);
	else{
		TCHAR *path = stringToNativeText(str);
		int openMode = O_RDONLY;
		if (mode == WRITE_ONLY) openMode = O_WRONLY;
		if (mode == READ_WRITE) openMode = O_RDWR;
		if (mode != READ_ONLY) openMode |= O_CREAT;
		openMode |= O_NONBLOCK;
		int ret = 0;
		int fd = open(path,openMode);
		if (fd != -1) {
			fchmod(fd,S_IRUSR|S_IWUSR|S_IXUSR|S_IRGRP|S_IROTH);
			setupPipe(file,fd);
			ret = 1;
		}
		free(path);
		return returnVar(ret);
	}
}

static int readFileBytes(WObject file,int source,char *dest,int numbytes)
{
	int fd = WOBJ_PipeNative(file);
	if (fd == -1) return -1;
	if (!pipeIgnored) ignoreSigPipe();
	if (lseek(fd,source,SEEK_SET) == -1) return -1;
	int toRead = numbytes;
	while(toRead > 0){
		int numRW = read(fd,dest,toRead);
		if (numRW < 0){
			if (errno == EAGAIN) continue;
			return -1;
		}else if (numRW == 0)
			return 0;
		toRead -= numRW;
		dest += numRW;
	}
	return numbytes;
}
static int writeAllFileBytes(WObject file,int location,char *src,int numbytes)
{
	int numRW, toWrite = numbytes;
  int fd = WOBJ_PipeNative(file);
	if (fd == -1) return -1;
	if (!pipeIgnored) ignoreSigPipe();
	if (location != -1)
		if (lseek(fd,location,SEEK_SET) == -1) return -1;
	while(toWrite > 0){
		int numRW = write(fd,src,toWrite);
		if (numRW < 0){
			if (errno == EAGAIN) continue;
			return -1;
		}
		toWrite -= numRW;
		src += numRW;
	}
	return numbytes;
}
/*
static int readAllFileBytes(WObject file,int location,char *dest,int numbytes,int readAll)
{
	int numRW, toRead = numbytes, didRead = 0;
  int fd = WOBJ_PipeNative(file);
	if (fd == -1) return -1;
	if (!pipeIgnored) ignoreSigPipe();
	if (location != -1)
		if (lseek(fd,location,SEEK_SET) == -1) return -1;
	while(toRead > 0){
		int numRW = read(fd,dest,toRead);
		if (numRW < 0){
			if (errno == EAGAIN) continue;
			return -1;
		}
		if (numRW == 0)
			if (readAll) return -2;
			else return didRead;
		didRead += numRW;
		if (!readAll) break;
		toRead -= numRW;
		dest += numRW;
	}
	return didRead;
}
*/
//
//
// Read bytes from a file.
// This will not return until at least one byte have been read.
//
// If readAll is true, it will not return until all numbytes have been read, or until it reaches
// the end of the stream.
//
// Returns: -1 = IO error occured, 0 = End of File, >0 = number of bytes read.
//
int readAllFileBytes(WObject file,int64 location,char *dest,int numbytes,BOOL readAll)
{
	int numRW, toRead = numbytes, didRead = 0;
	int low = (int)(location & 0xffffffff),
		high = (int)((location >> 32) & 0xffffffff);
	int fd = WOBJ_PipeNative(file);
	if (fd == -1) return -1;
	if (!pipeIgnored) ignoreSigPipe();
	//
	// Fixme - can't use a long with lseek.
	//
	if (location != -1)
		if (lseek(fd,low,SEEK_SET) == -1) return -1;
	while(toRead > 0){
		int numRW = read(fd,dest,toRead);
		if (numRW == 0) return didRead;
		didRead += numRW;
		if (!readAll) break;
		toRead -= numRW;
		dest += numRW;
	}
	return didRead;
}
/* ---- Windows version.

static int readAllFileBytes(WObject file,int64 location,char *dest,int numbytes,BOOL readAll)
{
	int numRW, toRead = numbytes, didRead = 0;
	int low = (int)(location & 0xffffffff),
		high = (int)((location >> 32) & 0xffffffff);
	int wasHigh = high;
	HANDLE fileH = WOBJ_RafileHandle(file);
	if (fileH == INVALID_HANDLE_VALUE) return -1;
	if (location != -1)
		if (SetFilePointer(fileH, low, &high, FILE_BEGIN) != (DWORD)location || high != wasHigh) return -1;
	while(toRead > 0){
		if (!ReadFile(fileH, (LPVOID)dest, toRead, &numRW, NULL)) return -1;
		if (numRW == 0) return didRead;
		didRead += numRW;
		if (!readAll) break;
		toRead -= numRW;
		dest += numRW;
	}
	return didRead;
}
----- */
#else
//
// RandomAccessFile
//
// var[0] = Class
// var[1] = Error string from ewe.util.Errorable
// var[2] = Closed boolean from ewe.io.StreamObject
// var[3] = nativeObject
// var[4] = int mode

#define WOBJ_RafFile(OBJ) objectPtr(OBJ)[3].obj
#define WOBJ_RafMode(OBJ) objectPtr(OBJ)[4].intValue

//
// FIX make this async
//
static Var RafileWrite(Var stack[])
{
	Var v;
	FILE *f = (FILE *)WOBJ_RafFile(stack[0].obj);
	v.intValue = -1;
	if (f == NULL) return v;
	else{
		WObject byteArray = stack[1].obj;
		int start = stack[2].intValue;
		int count = stack[3].intValue;
		if (arrayRangeCheck(byteArray, start, count) == 0)
			return returnException(ArrayIndexEx,NULL);
		else{
			char *toGo = (char *)WOBJ_arrayStart(byteArray)+start;
			v.intValue = fwrite(toGo,1,count,f);
			if (v.intValue < count) v.intValue = -2;
			return v;
		}
	}
}
//
// FIX make this async
//
static Var RafileRead(Var stack[])
{
	Var v;
	FILE *f = (FILE *)WOBJ_RafFile(stack[0].obj);
	v.intValue = -1;
	if (f == NULL) return v;
	else{
		WObject byteArray = stack[1].obj;
		int start = stack[2].intValue;
		int count = stack[3].intValue;
		if (arrayRangeCheck(byteArray, start, count) == 0)
			return returnException(ArrayIndexEx,NULL);
		else{
			char *toRead = (char *)WOBJ_arrayStart(byteArray)+start;
			v.intValue = fread(toRead,1,count,f);
			if (v.intValue <= 0)
				if (feof(f)) v.intValue = -1;
				else if (ferror(f)) v.intValue = -2;
			return v;
		}
	}
}
static int readFileBytes(WObject file,int source,char *dest,int numbytes)
{
	int numRW;
	FILE *f = (FILE *)WOBJ_RafFile(file);
	if (f == NULL) return -1;
	if (fseek(f,source,SEEK_SET) != 0) return -1;
	numRW = fread(dest,1,numbytes,f);
	if (numRW <= 0)
		if (feof(f) || ferror(f)) return -1;
	return numRW;
}

void RafileDestroy(WObject obj)
{
	FILE *f = (FILE *)WOBJ_RafFile(obj);
	if (f != NULL) fclose(f);
	WOBJ_RafFile(obj) = (WObject)NULL;
}

//** Read-only open mode.
#define READ_ONLY  1
//** Write-only open mode.
#define WRITE_ONLY 2
//** Read-write open mode.
#define READ_WRITE 3 // READ | WRITE
//** Create open mode. Used to create a file if one does not exist.
#define CREATE  4

static Var RafileCreate(Var stack[])
{
	WObject file = stack[0].obj;
	WObject str = stack[1].obj;
	int mode = stack[2].intValue;
	if (str == 0) return returnException(NullPointerEx,NULL);
	else{
		TCHAR *path = stringToNativeText(str);
		TCHAR *fmode = "rb";
		int ret = 0;
		FileListItem item;
		int ex = getFileAttributes(&item,path);
		if (ex || (mode != READ_ONLY)) {
			FILE *f = NULL;
			if (mode != READ_ONLY)
				if (!ex) fmode = "w+b";
				else fmode = "r+b";
			f = fopen(path,fmode);
			if (mode != READ_ONLY){
				struct stat buff;
				//printf("Going to check!\n");
				if (stat(path,&buff) == 0){
					//printf("Changing!\n");
					if (chmod(path,buff.st_mode|S_IXUSR) != 0){
						//printf("Chmod failed!\n");
					}
				}
			}
			if (f != NULL) {
				WOBJ_RafFile(file) = (uint32)f;
				ret = 1;
			}
		}
		free(path);
		return returnVar(ret);
	}
}

static Var RafileGetLength(Var stack[])
{
	FILE *f = (FILE *)WOBJ_RafFile(stack[0].obj);
	int ret = 0;
	if (f != NULL) {
		long now = ftell(f);
		fseek(f,0,SEEK_END);
		ret = (int)ftell(f);
		fseek(f,now,SEEK_SET);
	}
	return returnVar(ret);
}
static Var RafileFlush(Var stack[])
{
	FILE *f = (FILE *)WOBJ_RafFile(stack[0].obj);
	int ret = 0;
	if (f != NULL)
		fflush(f);
	return returnVar(1);
}
static Var RafileSeek(Var stack[])
{
	FILE *f = (FILE *)WOBJ_RafFile(stack[0].obj);
	int ret = 0;
	if (f != NULL)
		ret = fseek(f,(long)stack[1].intValue,SEEK_SET) == 0;
	return returnVar(ret);
}
static Var RafileSetLength(Var stack[])
{
	FILE *f = (FILE *)WOBJ_RafFile(stack[0].obj);
	if (f == NULL) return returnVar(-1);
	int fd = fileno(f);
	if (fd == -1) return returnVar(-1);
	return returnVar((ftruncate(fd,(off_t)vars2int64(stack+1)) == 0) ? 1 : -1);
}

static Var RafileGetPosition(Var stack[])
{
	FILE *f = (FILE *)WOBJ_RafFile(stack[0].obj);
	int ret = 0;
	if (f != NULL)
		ret = (int)ftell(f);
	return returnVar(ret);
}
static Var RafileSetLength(Var stack[])
{
	WObject file;
	HANDLE fileH;
	Var v;
	int64 len = vars2int64(stack+1), lastPos;
	int cur = 0, last = 0, lastHigh = 0;
	int low = (int)(len & 0xffffffff);
	int high = (int)((len >> 32) & 0xffffffff);

	v.intValue = -1;
	file = stack[0].obj;
	fileH = WOBJ_RafileHandle(file);
	if (fileH == INVALID_HANDLE_VALUE)
		return v;

	last = SetFilePointer(fileH, 0, &lastHigh, FILE_CURRENT);
	if (last == -1 && (GetLastError() != NO_ERROR)) return v;
	lastPos = ((int64)lastHigh << 32) | (((int64)last) & 0xffffffff);
	cur = SetFilePointer(fileH, low, &high, FILE_BEGIN);
	if (cur == -1 && (GetLastError() != NO_ERROR)) return v;
	if (SetEndOfFile(fileH)) v.intValue = 1;
	if (lastPos < len) SetFilePointer(fileH, last, &lastHigh, FILE_BEGIN);
	return v;
}

static Var RafileClose(Var stack[])
{
	RafileDestroy(stack[0].obj);
	return returnVar(1);
}
#endif

#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <signal.h>
#include <fcntl.h>

class ewe_process {

public:
int stdIn, stdOut, stdErr;
int pid;
int win, wout, werr;
char *error;
int hasExited;
int exitCode;

private:

static char **paths;
static uid_t userID;
static gid_t groupID;

void getAllPaths ()
{
		if (paths != NULL) return;
    userID = geteuid();
    groupID = getegid();
		char *path = getenv("PATH");
    if (path == NULL) return;
    path = strdup(path);
    int num = 0;
    for (char *c = path; *c != 0; c++)
			if (*c == ':') num++;
    paths = new char *[num+3];
		int i = 0;
		paths[i++] = "";
		paths[i++] = path;
		for (char *c = path; *c != 0; c++)
			if (*c == ':') {
				*c = 0;
				paths[i++] = c+1;
			}
		paths[i] = NULL;
}

char *canExecute(char *exe, struct stat *st)
{
		if (S_ISDIR(st->st_mode)) goto cantExe;
		if (st->st_uid == userID)
			if (st->st_mode & S_IXUSR) return NULL;
			else goto cantExe;
    if (st->st_gid == groupID)
			if (st->st_mode & S_IXGRP) return NULL;
			else goto cantExe;
    if (st->st_mode & S_IXOTH) return NULL;
cantExe:
		return "Command is not executable: ";
}

char *canExecuteInPaths(char *exe,char **error)
{
		getAllPaths();
		int len = strlen(exe);
		for (int i = 0;;i++){
			char *ps = paths[i];
			if (ps == NULL) break;
			int pl = strlen(ps);
			char *p = new char[len+pl+2];
			strcpy(p,ps);
			if (pl > 0)
				if (ps[pl-1] != '/')
					strcat(p,"/");
			strcat(p,exe);
			//printf("Trying: <%s>\n",p);
			struct stat st;
    	if (stat(p, &st) == 0){
				*error = canExecute(p,&st);
				if (*error != NULL) {
					delete p;
					return NULL;
				}else
					return p;
			}
			delete p;
			if (strchr(exe,'/') != NULL) break;
		}
		*error = "Command not found: ";
		return NULL;
}

void close(int fd)
{
	if (fd > -1) ::close(fd);
}

int fixFD(int *fd)
{
    int newFd = fcntl(*fd, F_DUPFD, 2);
    if (newFd < 0) return 0;
    close(*fd);
    *fd = newFd;
    return 1;
}

void closeAll(int *fd,int num)
{
	for (int i = 0; i<num; i++)
		close(fd[i]);
}
int *makePipes()
{
	int *ret = new int[6];
	for (int i = 0; i<6; i++) ret[i] = -1;
	if (pipe(ret+0) < 0 || pipe(ret+2) < 0 || pipe(ret+4) < 0){
		error = "Could not pipe.";
		goto errorReturn;
	}
	if (!fixFD(ret) || !fixFD(ret+3) || !fixFD(ret+5)){
		error = "Bad pipe file descriptors.";
		goto errorReturn;
	}
	return ret;
errorReturn:
	{
		for (int i = 0; i<6; i++) close(ret[i]);
		delete ret;
		return NULL;
	}
}

public:

ewe_process(char **commline,char **env):
stdIn(-1), stdOut(-1), stdErr(-1), error(NULL), pid(-1),
win(0), wout(0), werr(0), hasExited(0)
{
	char *exePath = canExecuteInPaths(commline[0],&error);
	if (exePath == NULL) return;
	int *pipes = makePipes();
	if (pipes == NULL) return;
	stdIn = pipes[1];
	stdOut = pipes[2];
	stdErr = pipes[4];
	if ((pid = fork()) == 0){
		::close(0); ::close(1); ::close(2);
		dup2(pipes[0],0); close(pipes[0]);
		dup2(pipes[3],1); close(pipes[3]);
		dup2(pipes[5],2); close(pipes[5]);
		int all = sysconf(_SC_OPEN_MAX);
		for (int i = 3; i < all; i++) close(i);
		if (env)
			execve(exePath,commline,env);
		else
			execv(exePath,commline);
		printf("Sorry, I can't execv()\n");
		exit(0);
	}else{
		delete exePath;
		if (pid == -1){
			closeAll(pipes,6);
			error = "Could not fork().";
			return;
		}
		close(pipes[0]); close(pipes[3]); close(pipes[5]);
	}
}

int finished()
{
	if (hasExited) return 1;
	int status = 0;
	exitCode = -1;
	int id = waitpid(pid,&status,WNOHANG);
	if (id != pid) return 0;
	hasExited = 1;
	if (WIFEXITED(status)) exitCode = WEXITSTATUS(status);
  return 1;
}

int terminate()
{
	if (pid == -1) return 0;
	return kill(pid,SIGKILL);
}

};

char **ewe_process::paths = NULL;
uid_t ewe_process::userID = 0;
gid_t ewe_process::groupID = 0;

typedef class ewe_process *EweProcess;

#define EXEC  0
#define CHECK_EXIT  1
#define GET_ERROR  2
#define GET_INPUT  3
#define GET_OUTPUT  4
#define FINALIZE  5
#define DESTROY  6

#define NativeProcess(OBJ) (objectPtr(OBJ)[1].refValue)

static WObject setupStream(WObject *stream,int fd)
{
	if (*stream != 0) return *stream;
	*stream = setupPipe(*stream,fd);
	return *stream;
}

static char *empty = "";

static char **toStringArray(WObject p)
{
	if (p == 0) return NULL;
	int num = WOBJ_arrayLen(p);
	char **ret = new char *[num+1];
	for (int i = 0; i<num; i++){
		WObject s = ((WObject *)WOBJ_arrayStart(p))[i];
		ret[i] = s == 0 ? empty : stringToNativeText(s);
	}
	ret[num] = NULL;
	return ret;
}

static void freeStringArray(char **array)
{
	if (array == NULL) return;
	for (int i = 0;;i++){
		char *p = array[i];
		if (p == NULL) break;
		if (p == empty) continue;
		free(p);
	}
	delete array;
}

static Var processOperation(Var stack[])
{
	WObject process = stack[0].obj;
	int op = stack[1].intValue;
	WObject p1 = stack[2].obj, p2 = stack[3].obj, pathString, argsString;
	Var v;
	v.obj = 0;
	switch(op){
	case EXEC:
		{
			char **command = toStringArray(p1);
			char **env = toStringArray(p2);
			EweProcess ep = new ewe_process(command,env);
			freeStringArray(command);
			freeStringArray(env);
			if (ep->pid == -1){
				v.obj = createStringFromUtf(createUtfString(ep->error != NULL ? (char *)ep->error : (char *)"Cannot exec: "));
			}else
				NativeProcess(process) = ep;
			break;
		}
	case CHECK_EXIT:
		{
			EweProcess ep =  (EweProcess)NativeProcess(process);
			if (ep != NULL){
				if (ep->finished()){
					setLong(p1,ep->exitCode);
					v.obj = p1;
				}
			}
			break;
		}
	case GET_ERROR:
		{
			EweProcess ep = (EweProcess)NativeProcess(process);
			if (ep != NULL)
				v.obj = setupStream((WObject*)&ep->werr,ep->stdErr);
			break;
		}
	case GET_INPUT:
		{
			EweProcess ep = (EweProcess)NativeProcess(process);
			if (ep != NULL)
				v.obj = setupStream(((WObject*)&ep->wout),ep->stdOut);
			break;
		}
	case GET_OUTPUT:
		{
			EweProcess ep = (EweProcess)NativeProcess(process);
			if (ep != NULL)
				v.obj = setupStream(((WObject*)&ep->win),ep->stdIn);
			break;
		}
	case DESTROY:
		{
			EweProcess ep = (EweProcess)NativeProcess(process);
			if (ep != NULL)
				ep->terminate();
			break;
		}
	case FINALIZE:
		{
			EweProcess ep = (EweProcess)NativeProcess(process);
			if (ep != NULL) delete ep;
			NativeProcess(process) = NULL;
			break;
		}
	}
	return v;
}

#include "nmunix_serial.c"
