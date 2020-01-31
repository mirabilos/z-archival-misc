/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.15, February 13, 2002                        *
 *  Copyright (c) 2007, 2008 Thorsten Glaser <tg@mirbsd.de>                      *
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

#include "cldefs.h"

__IDSTRING(rcsid_main, "$MirOS: contrib/hosted/ewe/vm/ewe.cpp,v 1.8 2008/05/11 23:14:15 tg Exp $");

// IMPORTANT NOTICE: To compile this program, you need to uncomment one of the
// platform lines below:
//
// For WinCE: uncomment #define WINCE 1
// For PalmOS: uncomment #define PALMOS 1
// For Windows NT, 98, 2000 or similar: uncomment #define WIN32 1
//
// and comment out or remove the NO_PLATFORM_DEFINED line below

//MLB July-2000
#define MLB 1

//#define NO_PLATFORM_DEFINED 1
//#define WIN32 1
//#define PALMOS 1
//#define WINCE 1
//#define POCKETPC 1
#define CODED_EWE_FILE

#ifdef DEBUG_LIBRARY
#undef CODED_EWE_FILE
#endif
//#define MAKING_POOL
//#define SHOW_MISSING_NATIVE_METHODS

#ifdef ZAURUS
#define PDA
#endif

#ifdef PDA
#define EMBEDDED
#define MOBILE
#endif

#ifdef HANDHELD
#define EMBEDDED
#define MOBILE
#endif

#ifdef NOEMBEDDEWE
#undef CODED_EWE_FILE
#endif

#ifdef MAKING_POOL
#undef CODED_EWE_FILE
#endif

#ifdef DEBUG_LIBRARY
#undef CODED_EWE_FILE
#endif

#ifdef UNIX
#include <sys/types.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <dlfcn.h>
#include <err.h>
#include <fcntl.h>
#include <pwd.h>
#include <signal.h>
#include <stdbool.h>
#include <stdint.h>
#include <unistd.h>
#endif
#include <ctype.h>
#include <errno.h>
#include <float.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "ewe_vm.c"


//##########################################################33
//
// Command line parsing.
//
//##########################################################33

extern "C" void usage(bool) __attribute__((noreturn));

int parseLine(TCHAR *cmdLine,TCHAR **words)
{
	int got = 0;
	TCHAR quote = 0;
	int end = 0;
	TCHAR *ln = cmdLine;

	while (*ln != 0)
		if (*ln == '\r' || *ln == '\n') *ln = 0;
		else ln++;

	ln = cmdLine;

	while(1){
		while (*ln == ' ') ln++;
		if (*ln == 0) return got;
		if (*ln == '\'' || *ln == '\"') quote = *(ln++);
		if (words != NULL) words[got] = ln;
		got++;
		while(1){
			if (*ln == 0) end = 1;
			if (end || (*ln == ' ' && !quote) || *ln == quote){
				if (words != NULL) *ln = 0;
				if (end) return got;
				else break;
			}
			if (end) return got;
			if (*ln == 0) break;
			ln++;
		}
		ln++;
		quote = 0;
	}

}

static int getValue(TCHAR *val)
{
	int value = 0;
	while (*val >= '0' && *val <= '9') value = (value*10)+((*val++)-'0');
	return value;
}

static int isWarp(TCHAR *word)
{
	TCHAR *w = word;
	int len = textLength(word);
	if (len <= 4) return 0;
	word += len;
	word--; if (*word != 'e' && *word != 'E') return 0;
	word--; if (*word != 'w' && *word != 'W') return 0;
	word--; if (*word != 'e' && *word != 'E') return 0;
	word--; if (*word != '.') return 0;
	return 1;
}


static TCHAR *inWarp = NULL;

static int alreadyMapped(TCHAR *warpFile)
{
	int len = textLength(warpFile);
	int wl;
	TCHAR *p;
	int i;
	if (inWarp == NULL) return 0;
	wl = textLength(inWarp);
	if (len-4 < wl) return 0;
	p = warpFile+len-4-wl;
	for (i = 0; i<wl; i++){
		TCHAR c1 = p[i];
		TCHAR c2 = inWarp[i];
		if (c1 >= 'a' && c1 <= 'z') c1 ^= 0x20;
		if (c2 >= 'a' && c2 <= 'z') c2 ^= 0x20;
		if (c1 != c2) return 0;
	}
	if (p == warpFile) return 1;
	if (*p == '\\' || *p == '/') return 1;
	return 0;
}
static int takeArguments = 0;

#define DefaultApplication TEXT("ewe/ui/Welcome")

#ifndef USE_EXTERNAL_START_APP
int CommandLineParsed(TCHAR *className)
{
	return 1;
}
#endif

/*
static int g_mainWinWidth = 0;
static int g_mainWinHeight = 0;
static WObject mainWin;
static TCHAR *pszWndClassName;//[] = TEXT("WabaWndClass");
static TCHAR g_windowTitle[50];
*/

static WObject startApp(TCHAR *cmdLine, BOOL *alreadyRunning,int level)
{
	int vmStackSize, nmStackSize, classHeapSize, objectHeapSize;
	int i;
	TCHAR **words;
	TCHAR *className = mainClassName;
	TCHAR c;
	int num;
	WObject ret;
//MLB
	vmStackSize = 10000;
	nmStackSize = 100;
	classHeapSize =  32000;
	objectHeapSize = 32000;
	textCopy(TEXT("eWe"),g_windowTitle,50);
	if (level == 0) programDir[0] = 0;
/*
	vmStackSize = 1500;
	nmStackSize = 300;
	classHeapSize = 14000;
	objectHeapSize = 8000;
*/
	className = 0;
	*alreadyRunning = 0;

	num = parseLine(cmdLine,NULL);
	if (num == 0){
		TCHAR *def = DefaultApplication;
		TCHAR *t2 = (TCHAR *)malloc(sizeof(TCHAR)*50);
		textCopy(def,t2,textLength(def)+1);
		cmdLine = t2;
		num = 1;
	}
	words = (TCHAR **)malloc(sizeof(TCHAR *)*num);
	parseLine(cmdLine,words);

	for (i = 0; i<num; i++){
		int value =
			i<num-1 ? getValue(words[i+1]):0;
		if ((*words[i] == '/' || *words[i] == '-') && !isWarp(words[i])) {
			c = *(words[i]+1);
			if (className != 0){
				if (c == '-') i++; //Skip over "/-" or "--".
				break;
			}
			if (i == num-1)
				if (!strchr("-?hmnOoprsvz",c)) break;
			i++;
			if (c == '-') break;
			switch(c){
				case '?': usage(false); break;
				case 'b': textCopy(words[i],g_windowTitle,50); break;
				case 'c':
					if (textCompare(words[i-1]+1,-1,TEXT("cp"),2) == 0){
						int len = textLength(words[i])+1;
						classPath = (TCHAR *)malloc(sizeof(TCHAR)*len);
						textCopy(words[i],classPath,len);
						break;
					}
					if (value > classHeapSize) classHeapSize = value;
					break;
				case 'd': textCopy(words[i],programDir,textLength(words[i])+1); programDirDefined = 1; break;
				case 'h': if (!value) usage(false); g_mainWinHeight = value; break;
				case 'l': textCopy(words[i],localeData,textLength(words[i])+1); break;
				//case 'm': if (value > objectHeapSize) objectHeapSize = value; break;
				case 'm': VmFlags |= VM_FLAG_IS_LOW_MEMORY; i--; break;
				case 'n': VmFlags |= VM_FLAG_NO_WINDOWS; i--; break;
				case 'O': VmFlags |= VM_FLAG_COUNTER_ROTATE_SCREEN; i--; break;
				case 'o': VmFlags |= VM_FLAG_ROTATE_SCREEN; i--; break;
				//case 'p': VmFlags |= VM_FLAG_NO_MOUSE_POINTER|VM_FLAG_NO_KEYBOARD; i--; break;
				case 'p':
					VmFlags |= VM_FLAG_NO_MOUSE_POINTER|VM_FLAG_NO_KEYBOARD; SimulateSip = 0x1; i--;
					if (g_mainWinWidth == 0 && g_mainWinHeight == 0)
						g_mainWinWidth = 240, g_mainWinHeight = 320;
					break;
				case 'r': VmFlags |= VM_FLAG_IS_MOBILE; i--; break;
				//case 's': if (value > vmStackSize) vmStackSize = value; break;
				case 's':
					//VmFlags |= VM_FLAG_NO_MOUSE_POINTER|VM_FLAG_NO_KEYBOARD; i--;
					VmFlags |= VM_FLAG_HAS_SOFT_KEYS|VM_FLAG_NO_WINDOWS|VM_FLAG_NO_MOUSE_POINTER|VM_FLAG_NO_KEYBOARD|VM_FLAG_NO_PEN|VM_FLAG_USE_NATIVE_TEXT_INPUT; i--;
					if (g_mainWinWidth == 0 && g_mainWinHeight == 0)
						g_mainWinWidth = 176, g_mainWinHeight = 220;
					break;
				case 't': if (value > nmStackSize) nmStackSize = value; break;
				case 'v': usage(true); break;
				case 'w': g_mainWinWidth = value; break;
				case 'x': textCopy(extPath,programDir,textLength(extPath)+1); break;
				case 'z': VmFlags |= VM_FLAG_IS_MONOCHROME; i--; break;
			}
		}else{
			if (!isWarp(words[i])){
				if (className == 0)
					className = words[i];
				else
					break;
			}else{
				if (alreadyMapped(words[i])) continue;
				if (tryToMemMapFile(words[i],1) && (level == 0)){
					TCHAR * p = words[i]+textLength(words[i])-1;
					TCHAR * e = p;
					if (eweFile == NULL){
						TCHAR *cd = getCwd();
						if (words[i][0] != '/'){
							eweFile = new TCHAR[textLength(words[i])+textLength(cd)+2];
							txtcpy(eweFile,cd);
							txtcat(eweFile,"/");
              txtcat(eweFile,words[i]);
            }else{
              eweFile = new TCHAR[textLength(words[i])+1];
						  txtcpy(eweFile,words[i]);
						}
					}
					*p-- = 'n';
					*p-- = 'u';
					*p-- = 'r';
					e = p;
					for (;p>words[i] && *p != '/' && *p != '\\'; p--);
					if (p>words[i] && (*p == '/' || *p == '\\')) *p = 0, p++;
					if (programDir[0] == 0)
						if (p != words[i])
							textCopy(words[i],programDir,textLength(words[i])+1);
						else{
							TCHAR *cd = getCwd();
							textCopy(cd,programDir,textLength(cd)+1);
						}
					inWarp = (TCHAR *)malloc(sizeof(TCHAR)*(e-p+1));
					textCopy(p,inWarp,e-p+1);
					inWarp[e-p] = 0;
					if (className == 0){
						unsigned int len;
						int fr;
						char * asciiPath = nativeTextToAscii(p,&fr);
						char * command = (char *)loadFromMem(asciiPath,textLength(p),&len,NULL);
						if (fr) free(asciiPath);
						if (command != NULL){
							WObject ret;
							TCHAR *cl = (TCHAR *)malloc(sizeof(TCHAR)*(len+1));
							asciiToNativeText(command,cl,len+1);
							cl[len] = 0;
							//MessageBox(NULL,cl,TEXT("Command!"),MB_OK);
							takeArguments = (i == num-1);
							ret = startApp(cl,alreadyRunning,1);
							//free(cl); Don't free it. It may be used for arguments.
							className = mainClassName;
							//return ret; Don't continue parsing original line.
						}
					}
				}
			}
		}

	}

	if (level > 0) {
		if (className != NULL){
			mainClassName = (TCHAR *)malloc(sizeof(TCHAR)*(textLength(className)+1));
			textCopy(className,mainClassName,textLength(className)+1);
		}
		if (takeArguments){
			arguments = words+i;
			numArguments = num-i;
		}else{
			free(words);
			free(cmdLine);
		}
		return 0;
	}else{
		if (arguments == NULL){
			arguments = words+i;
			numArguments = num-i;
		}
	}
	if (className == 0)
		className = DefaultApplication;

	mainClassName = className;
// MLB July-2000
#ifdef MLB
#ifdef WIN32
#ifndef WINCE
	//memMapFile(WARP_CORE_PATH);
#endif
#endif
#endif

#ifndef WARP_CORE_PATH
#define WARP_CORE_PATH "ewe.ewe"
#endif

	tryToMemMapFile(WARP_CORE_PATH,1);
#ifndef EMBEDDED
	tryToMemMapFile(TEXT("EweConfig.ewe"),0);
#endif

#ifdef DEBUGCMDLINE
	AllocConsole();
	cprintf("mainWinWidth %d\n", g_mainWinWidth);
	cprintf("mainWinHeight %d\n", g_mainWinHeight);
	cprintf("vmStackSize %d\n", vmStackSize);
	cprintf("nmStackSize %d\n", nmStackSize);
	cprintf("classHeapSize %d\n", classHeapSize);
	cprintf("objectHeapSize %d\n", objectHeapSize);
	cprintf("className #%s#\n", className);
#endif

		if (!CommandLineParsed(className)) return 0;

		VmInit(vmStackSize, nmStackSize, classHeapSize, objectHeapSize);
		LOCKTHREAD
		ret = VmStartApp(nativeTextToAscii(className,NULL));
		vmInSystemQueue = FALSE;
		UNLOCKTHREAD
		if (ret == 0)
			if (vmStatus.errNum == 0)
				vmStatus.errNum = ERR_BadAppClass;
		return ret;
}


int NormalEweIteration()
{
	int wait;
	LOCKTHREAD
	canCallInSystemQueue = 1; //Fix this later.
	wait = TimerThreadCheck();
	canCallInSystemQueue = 0;
	UNLOCKTHREAD
	return wait;
}

#ifndef USE_EXTERNAL_EWE_ITERATION
#define EweIteration NormalEweIteration
#endif

#define MAX_WAIT 100
#ifndef USE_EXTERNAL_MAIN_LOOP

static int HandleMessage()
{
	return 1000;
}
static void MainLoop()
{
	if (timerEntries == NULL) expandTimerEntries();
	while(TRUE){
		int wait = EweIteration();
		int guiWait = HandleMessage();
		if (wait == -1 || ((guiWait < wait) && (guiWait >= 0))) wait = guiWait;
		if ((wait > MAX_WAIT) || (wait < 0))
			wait = MAX_WAIT;

			//TranslateMessage(&msg);
			//DispatchMessage(&msg);

		//Get keyboard and mouse event.
		//Dispatch event.
		  msleep(wait);
	}
}
#endif

#ifndef USE_EXTERNAL_START_APP
int EweMain(char *cmdLine)
{
	WObject mainWinObj;
	//int cmdLen;
	BOOL alreadyRunning;

#ifdef MOBILE
#ifdef HANDHELD
	VmFlags |= VM_FLAG_IS_MOBILE|VM_FLAG_IS_SLOW_MACHINE|VM_FLAG_NO_MOUSE_POINTER;
#elif defined(PDA)
	VmFlags |= VM_FLAG_IS_MOBILE|VM_FLAG_IS_SLOW_MACHINE|VM_FLAG_NO_KEYBOARD|VM_FLAG_NO_MOUSE_POINTER;
#else
	VmFlags |= VM_FLAG_IS_MOBILE|VM_FLAG_IS_SLOW_MACHINE;
#endif
#endif

#ifdef NOGUI
	VmFlags |= VM_FLAG_NO_GUI;
#endif

#ifdef USE_LOG
	Log("---\n",4);
#endif
	mainWinObj = startApp(cmdLine, &alreadyRunning,0);
	if (mainWinObj == 0) {
		if (vmStatus.errNum <= 0) debugString("Unknown VM error!");
		else debugString(errorMessages[vmStatus.errNum-1]);
		return -1;
	}
#ifdef USING_WIN32_API
	{
	//HDC dc = CreateDC(TEXT("DISPLAY"), NULL, NULL, NULL);
	//if (GetDeviceCaps(dc,NUMCOLORS) < 256 && GetDeviceCaps(dc,NUMCOLORS) != -1) VmFlags |= VM_FLAG_IS_MONOCHROME;
//	DeleteDC(dc);
	}
#endif

	MainLoop();

#ifdef NEVER

	hPrevInstance = hPrev;
	extModule = externalModule;
	myModule = yourModule;
	//MessageBox(NULL,lpCmdLine,TEXT("Command"),MB_OK|MB_SETFOREGROUND);
	if (extModule == NULL){
#ifndef WINCE
		_tgetcwd(extPath,sizeof(extPath)-1);
#endif
	}
	findFilePath(externalModule,extPath);
	findFilePath(yourModule,myPath);

	g_hInstance = hInstance;
/*
	if (!hPrevInstance)
		{
		WNDCLASS wc;
		xmemzero(&wc, sizeof(wc));
		wc.hIcon = getIcon(yourModule,externalModule);
		if (wc.hIcon == NULL) wc.hIcon = getIcon(yourModule,yourModule);
		wc.hInstance = g_hInstance;
		wc.lpfnWndProc = MainWndProc;
#ifdef WINCE
		wc.hCursor = NULL;
#else
		wc.hCursor = LoadCursor(NULL, IDC_ARROW);
#endif
		wc.hbrBackground = (HBRUSH)GetStockObject(NULL_BRUSH);//WHITE_BRUSH);
		wc.lpszClassName = pszWndClassName;
		if (!RegisterClass(&wc)){
			DWORD err = GetLastError();
			if (wc.hIcon != NULL) DestroyIcon(wc.hIcon);
			return FALSE;
		}
		}
*/
	// NOTE: We need to make a copy of the command line since we modify it
	// when parsing the command line in startApp()
		{
		int i;
		//MessageBox(NULL,lpCmdLine,TEXT("In DLL"),MB_SETFOREGROUND);

		if (lpCmdLine != NULL)
#ifdef UNICODE//WINCE
		{
			cmdLen = lstrlen(lpCmdLine);
		}
#else
			cmdLen = xstrlen(lpCmdLine);
#endif
		else
			cmdLen = 0;
		cmdLine = (TCHAR *)xmalloc(sizeof(TCHAR)*(cmdLen + 1));
		if (!cmdLine)
			return -1;
		for (i = 0; i < cmdLen; i++)
			cmdLine[i] = (TCHAR)lpCmdLine[i];
		cmdLine[i] = 0;
		}

	SetCursor(waitCursor = LoadCursor(NULL, IDC_WAIT));
	waitEvent = CreateEvent(NULL,0,0,NULL);
	InitializeCriticalSection(&sendMessageSection);
	EnterCriticalSection(&sendMessageSection);
	mainWinObj = mainWin = startApp(cmdLine, &alreadyRunning,0);
	if (mainWinObj != 0) appStarted = TRUE;
	LeaveCriticalSection(&sendMessageSection);
#ifdef NEVER
	if (memFileNotSupported && !cmdLine[0])
		MessageBox(NULL, TEXT("This Windows device does not fully support\n"
			"memory mapping. This means Ewe programs will\n"
			"use more memory than they normally should."),
			TEXT("Device Not Optimal"), MB_ICONEXCLAMATION);
#endif
	xfree(cmdLine);
	usage();
#ifdef QUICKBIND
	/*
	if (mainWinObj != 0)
		{
		WClass *vclass;
		WClass *ctrlclass;
		// cache method map numbers for commonly called methods
		vclass = WOBJ_class(mainWinObj);
		postPaintMethodMapNum = getMethodMapNum(vclass, createUtfString("_doPaint"),
			createUtfString("(IIII)V"), SEARCH_ALL);
		postEventMethodMapNum = getMethodMapNum(vclass, createUtfString("_postEvent"),
			createUtfString("(IIIIII)V"), SEARCH_ALL);
		onTimerTickMethodMapNum = getMethodMapNum(vclass, createUtfString("_onTimerTick"),
			createUtfString("()V"), SEARCH_ALL);
		ctrlclass = getClass(createUtfString("ewe/ui/Control"));
		doPaintMethodMapNum = getMethodMapNum(ctrlclass, createUtfString("doPaint"),
			createUtfString("(Lewe/fx/Graphics;Lewe/fx/Rect;)V"), SEARCH_ALL);
		if (postPaintMethodMapNum == -1 || postEventMethodMapNum == -1 ||
			onTimerTickMethodMapNum == -1)// || doPaintMethodMapNum == -1)
			mainWinObj = 0;
		}
	*/
#endif
	if ((mainWinObj == 0 || vmStatus.errNum > 0) && !alreadyRunning)
		postErrorDialog(NULL);

	while (mainWinObj && GetMessage(&msg, NULL, 0, 0))
		{
		if (g_startTime == 0)
			g_startTime = msg.time;
		g_messageTime = msg.time;
		TranslateMessage(&msg);
		DispatchMessage(&msg);
		}

	stopApp(mainWinObj);
	if (win32WSAStarted)
		WSACleanup();
#endif

	return 0;
}
#endif


#ifndef USE_EXTERNAL_START_APP
int EweStartApp(int argc,char *argv[])
{
	int maxSize = 0, i;
	char *nc = "";
	for (i = 1; i<argc; i++){
		maxSize += strlen(argv[i])+1;
	}
	nc = (char *)malloc(maxSize+1);
	*nc = 0;
	for (i = 1; i<argc; i++){
		strcat(nc,argv[i]);
		strcat(nc," ");
	}

	//static char nc[256] = "tests.TestUnixEwe";
	return EweMain(nc);
}
#endif

#ifndef EWE_AS_SHARED_LIBRARY
int main(int argc,char *argv[])
{
	return EweStartApp(argc,argv);
}
#endif

NativeMethod theNativeMethods[] =
	{
#ifndef NO_NATIVES
//	// ewe/sys/Vm_getClipboardText_(Ljava/lang/String;)Ljava/lang/String;
//	{ 113967542U, VmGetClipboardText },
	// ewe/sys/Vm_createStringWithChars_([C)Ljava/lang/String;
	{ 113967595U, VmCreateStringWithChars },
	// ewe/sys/Vm_exec_(Ljava/lang/String;Ljava/lang/String;IZ)I
	{ 113969325U, VmExec },
	// ewe/sys/Vm_mutateString_(Ljava/lang/String;[CIIZ)Ljava/lang/String;
	{ 113969335U, VmMutateString},
	// ewe/sys/Vm_getResourceData_(II[BII)Z
	{ 113974168U, VmGetResourceData },
	// ewe/sys/Vm_getResource_(Ljava/lang/String;[I)Z
	{ 113974754U, VmGetResource },
	// ewe/sys/Vm_callInSystemQueue_(Lewe/sys/CallBack;Ljava/lang/Object;)I
	{ 113979641U, VmCallInSystemQueue },
	// ewe/sys/Vm_setCursorHandle_(Lewe/ui/Window;I)I
	{ 113980387U, VmSetCursorHandle },
	// ewe/sys/Vm_setSIP_(ILewe/ui/Window;)V
	{ 113983386U, VmSetSIP},
	// ewe/sys/Vm_toInt_(Ljava/lang/Object;)I
	{ 113986074U, VmToInt},
	// ewe/sys/Vm_getStringChars_(Ljava/lang/String;)[C
	{ 113987044U, VmGetStringChars },
	// ewe/sys/Vm_requestTick_(Lewe/sys/TimerProc;IZ)I
	{ 113987940U, VmRequestTimer},
	// ewe/sys/Vm_cancelTimer_(I)V
	{ 113989071U, VmCancelTimer},
//	// ewe/sys/Vm_releaseResource_(Ljava/lang/String;)I <- Deprecated
//	{ 113989860U, VmReleaseResource },
	// ewe/sys/Vm_getTimeStamp_()I
	{ 113990543U, VmGetTimeStamp },
	// ewe/sys/Vm_gc_()V
	{ 113990725U, VmGc },
	// ewe/sys/Vm_debug_(Ljava/lang/String;I)V
	{ 113993179U, VmDebug },
	// ewe/sys/Vm_getParameter_(I)I
	{ 113996048U, VmGetParameter},
//	// ewe/sys/Vm_setClipboardText_(Ljava/lang/String;)V
//	{ 113996325U, VmSetClipboardText },
//	// ewe/sys/Vm_applicationError_(Ljava/lang/String;)V <- Deprecated
//	{ 113998181U, VmAppError },
	// ewe/sys/Vm_countObjects_(Z)I
	{ 113998288U, VmCountObjects },
	// ewe/sys/Vm_setParameter_(II)I
	{ 114001489U, VmSetParameter},
	// ewe/sys/Vm_nativeLoadLibrary_(Ljava/lang/String;)Z
	{ 114002406U, VmLoadLibrary },
//	// ewe/sys/Vm_messageBox_(Ljava/lang/String;Ljava/lang/String;I)I
//	{ 114003058U, VmMessageBox },
//	// ewe/sys/Vm_loadResourceOrFile_(Ljava/lang/String;Lewe/util/ByteArray;)I
//	{ 114003260U, VmLoadResourceOrFile },
	// ewe/sys/Vm_nativeGetStream_(Lewe/io/Stream;I)Lewe/io/Stream;
	{ 114003314U, VmGetStream },

	// ewe/sys/Vm_getProperty_(Ljava/lang/String;)Ljava/lang/String;
	{ 114003377U, VmGetProperty },
	// ewe/sys/Vm_getUsedMemory_(Z)I
	{ 114003857U, VmGetUsedMemory },
	// ewe/sys/Vm_copyArray_(Ljava/lang/Object;ILjava/lang/Object;II)Z
	{ 114004019U, copyArray },
	// ewe/sys/Vm_getClassMemory_()I
	{ 114004561U, VmGetUsedClassMemory },
	// ewe/sys/Vm_nativeGetStackTrace_(Ljava/lang/Throwable;)Ljava/lang/String;
	{ 114004924U, VmGetStackTrace },
//	// ewe/sys/Vm_getMessage_(Lewe/sys/SystemMessage;ZZ)I
//	{ 114007847U, VmGetMessage },
	// ewe/sys/Vm_amInSystemQueue_()Z
	{ 114011794U, VmAmInSystemQueue },
	// ewe/sys/Vm_getSIP_()I
	{ 114012553U, VmGetSIP},
	// ewe/sys/Vm_getCursorHandle_(I)I
	{ 114015059U, VmGetCursorHandle},
	// ewe/sys/Vm_getTimeStampLong_()J
	{ 114016211U, VmGetTimeStampLong},
//	// ewe/sys/Vm_readResource_(Ljava/lang/String;Ljava/lang/String;I)I <- Deprecated.
//	{ 114016756U, VmReadResource },
	// ewe/sys/Vm_sleep_(I)V
	{ 114016841U, VmSleep },
	// ewe/sys/Vm_getProgramArguments_()[Ljava/lang/String;
	{ 114017960U, VmGetProgramArguments },
	// ewe/sys/Vm_getReferencedObjects_()[Ljava/lang/Object;
	{ 114018921U, VmGetReferencedObjects },
//	// ewe/sys/Vm_setDeviceAutoOff_(I)I
//	{ 114019540U, VmSetDeviceAutoOff },
//	// ewe/sys/Vm_playSound_(Ljava/lang/String;I)I
//	{ 114020511U, VmPlaySound },

	// ewe/sys/Vm_getAsyncKeyState_(I)I
	{ 114021076U, VmGetAsyncKeyState },
	// ewe/sys/Vm_getUserName_()Ljava/lang/String;
	{ 114021471U, VmGetUserName },
	// ewe/sys/Vm_getPlatform_()Ljava/lang/String;
	{ 114023839U, VmGetPlatform },
	// ewe/sys/Vm_isColor_()Z
	{ 114024842U, VmIsColor },
	// ewe/sys/Vm_getNewId_()I
	{ 114027595U, VmGetNewId },
	// ewe/sys/Vm_debugObject_(Ljava/lang/Object;I)V
	{ 114029473U, VmDebugObject },

	// ewe/net/ServerSocket_pauseUntilAccepted_(I)I
	{ 135596438U, SSPause },
	// ewe/net/ServerSocket_getAcceptedSocket_()Lewe/net/Socket;
	{ 135614692U, SSGetAcceptedSocket },
	// ewe/net/ServerSocket_getLocalPort_()I
	{ 135617167U, SSGetLocalPort },
	// ewe/net/ServerSocket_close_()V
	{ 135638856U, SocketClose },
	// ewe/net/ServerSocket_isOpen_()Z
	{ 135644745U, SocketIsOpen },
	// ewe/net/ServerSocket__nativeCreate_(IILjava/lang/String;)Ljava/lang/String;
	{ 135649141U, SSCreate },
	// ewe/net/ServerSocket_checkIO_(I)I
	{ 135650891U, SSCheckIO },

//	// ewe/sys/Vm_nativeDoPause_(I)Lewe/sys/Handle;
//	{ 114032481U, VmDoPause },

	// ewe/io/File_getLength_()I
	{ 340528908U, FileGetLength},
	// ewe/io/File_createDir_()Z
	{ 340529036U, FileCreateDir },
	// ewe/io/File_doList_(Ljava/lang/String;I)[Ljava/lang/String;
	{ 340535598U, FileListDir },
	// ewe/io/File_nativeSetInfo_(ILjava/lang/Object;I)Z
	{ 340541220U, FileSetInfo },
	// ewe/io/File_isDirectory_()Z
	{ 340545294U, FileIsDir},
	// ewe/io/File_deleteOnExit_()V
	{ 340548687U, FileDeleteOnExit},
	// ewe/io/File_nativeGetInfo_(ILjava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;
	{ 340551111U, FileGetInfo },
	// ewe/io/File_nativeGetSetPermissionsAndFlags_(ZII)I
	{ 340557157U, FileGetSetAttributes},
	// ewe/io/File_delete_()Z
	{ 340576137U, FileDelete },
	// ewe/io/File_move_(Lewe/io/File;)Z
	{ 340577941U, FileRename },
	// ewe/io/File_exists_()Z
	{ 340579017U, FileExists },
	// ewe/io/File_getFullPath_()Ljava/lang/String;
	{ 340579423U, FileGetFullPath},
	// ewe/io/File__nativeCreate_(Lewe/io/File;Ljava/lang/String;)Ljava/lang/String;
	{ 340584705U, FileCreate },
	// ewe/io/File_getSetModified_(Lewe/sys/Time;Z)V
	{ 340590369U, FileGetSetModified},

	// ewe/fx/Mask_nativeBitManipulate_(Ljava/lang/Object;I)Ljava/lang/Object;
	{ 416044090U, MaskBitManipulate },

	// ewe/database/RecordFoundEntries_nativeWrite_(Lewe/util/IntArray;Lewe/util/ByteArray;)Z
	{ 417340855U, RecordFoundEntriesWrite},
	// ewe/database/RecordFoundEntries_nativeRead_(Lewe/util/ByteArray;Lewe/util/IntArray;)Z
	{ 417397238U, RecordFoundEntriesRead},

	// ewe/fx/Font_listFonts_(Lewe/fx/ISurface;)[Ljava/lang/String;
	{ 462184624U, FontListFonts},

	// ewe/math/MPN_native_modPow_(Lewe/math/MPN;Lewe/math/MPN;)V
	{ 596446062U, MPNModPow},
	// ewe/math/MPN_nativeMul_([I[II[II)V
	{ 596489876U, MPNmul},
	// ewe/math/MPN_test_(Lewe/math/MPN;Lewe/math/MPN;Lewe/math/MPN;Lewe/math/MPN;I)I
	{ 596491780U, MPNTest },
	// ewe/math/MPN_nativeDivide_([II[II)V
	{ 596498261U, MPNdivide },

	// ewe/fx/Image_nativeToCursor_(Lewe/fx/Image;II)I
	{ 780995938U, ImageToCursor },
	// ewe/fx/Image_nativeFree_()V
	{ 781003789U, ImageFree},
//	// ewe/fx/Image_setPixels_(I[IIIILjava/lang/Object;)V <-Deprecated version.
//	{ 781003812U, ImageSetPixels },
	// ewe/fx/Image__nativeLoad_(Lewe/io/BasicStream;)V
	{ 781004003U, ImageLoadBitmapFile },

	// ewe/fx/Image_doRotate_(Lewe/fx/Image;I)V
	{ 781016283U, ImageDoRotate},
//	// ewe/fx/Image_getCursorSize_(Lewe/fx/Dimension;)Z <- Not used in Unix
//	{ 781009443U, ImageGetCursorSize },
	// ewe/fx/Image_toIcon_(Lewe/fx/Image;Lewe/fx/Image;I)I
	{ 781022312U, ImageToIcon },
	// ewe/fx/Image__nativeCreate_(I)V
	{ 781027985U, ImageCreate },
	// ewe/fx/Image_grayPixels_([IIII)V
	{ 781030290U, ImageGrayPixels },
	// ewe/fx/Image_getNativeResourcePointer_()I
	{ 781032091U, ImageGetNativeResource },
	// ewe/fx/Image_setPixels_([IIIIIII)V
	{ 781037716U, ImageSetPixelsRect},
	// ewe/fx/Image_getPixels_([IIIIIII)[I
	{ 781041941U, ImageGetPixelsRect },
//	// ewe/fx/Image_fixMasks_(Lewe/fx/Image;Lewe/fx/Image;Lewe/fx/Color;)V
//	{ 781054776U, ImageFixMasks }, //Not used for LINUX

	// ewe/sys/nativeProcess_processOperation_(ILjava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
	{ 798405321U, processOperation},

	// ewe/net/DatagramSocket_sendDatagram_(Lewe/net/DatagramPacket;)Ljava/lang/Object;
	{ 848770873U, DSSend },
	// ewe/net/DatagramSocket_getLocalPort_()I
	{ 848779919U, SSGetLocalPort },
	// ewe/net/DatagramSocket_nativePauseUntilReady_(II)I
	{ 848784538U, DSPause },
	// ewe/net/DatagramSocket_receiveDatagram_(Lewe/net/DatagramPacket;)Ljava/lang/Object;
	{ 848790908U, DSReceive },
	// ewe/net/DatagramSocket_close_()V
	{ 848801608U, ServerSocketClose },
	// ewe/net/DatagramSocket__nativeCreate_(IILjava/lang/String;)Ljava/lang/String;
	{ 848811893U, DSCreate },
	// ewe/net/DatagramSocket_getSetBufferSize_(ZIZ)I
	{ 848821782U, DSGetSetBufferSize},

	// ewe/fx/Curve_nativeCalculateCurves_([D[FLjava/lang/Object;Ljava/lang/Object;III)I
	{ 923603139U, CurveCalculateCurves },
	// ewe/fx/Curve_nativeCalculatePoints_([D[DI)V
	{ 923643933U, CurveCalculatePoints},


//	// ewe/fx/Sound_beep_()V
//	{ 940413127U, SoundBeep },
//	// ewe/fx/Sound_beep_(I)V
//	{ 940417800U, SoundBeep2 },
//	// ewe/fx/Sound_tone_(II)V <- Deprecated
//	{ 940424137U, SoundTone },

	// ewe/sys/Math_constant_(I)D
	{ 948769292U, MathConstantNative},
	// ewe/sys/Math_calculate_(IDD)D
	{ 948782351U, MathCalculateNative },
	// ewe/sys/Math_rand_()I
	{ 948801479U, MathRand },
	// ewe/sys/Math_srand_(I)V
	{ 948814345U, MathSrand },

//	// ewe/sys/Time_setTime_(J)Lewe/sys/Time;
//	{ 969750809U, TimeSetTime },
	// ewe/sys/Time__nativeCreate_()V
	{ 969766992U, TimeCreate },
	// ewe/sys/Time_nativeSetTime_(II)V
	{ 969776850U, TimeSetDate },

//	// ewe/sys/Time_getTime_()J
//	{ 969792138U, TimeGetTime },

	// ewe/sys/Long_toString_(II)Ljava/lang/String;
	{ 973977438U, LongToString },
	// ewe/sys/Long_fromString_(Ljava/lang/String;)V
	{ 973986975U, LongFromString},

	// ewe/io/RandomAccessFile_getLength_()I
	{ 1092095756U, RafileGetLength },
	// ewe/io/RandomAccessFile_nonBlockingWrite_([BII)I
	{ 1092096535U, RafileWrite },
	// ewe/io/RandomAccessFile__nativeCreate_(Ljava/lang/String;I)Z
	{ 1092105123U, RafileCreate },
	// ewe/io/RandomAccessFile__flushStream_()Z
	{ 1092111950U, RafileFlush },
	// ewe/io/RandomAccessFile_nativeRead_([BIIZ)I
	{ 1092127122U, RafileQuickRead },
	// ewe/io/RandomAccessFile_nativeWrite_([BII)I
	{ 1092130514U, RafileQuickWrite },
	// ewe/io/RandomAccessFile_seek_(I)Z
	{ 1092134664U, RafileSeek },
	// ewe/io/RandomAccessFile_getFilePosition_()I
	{ 1092135890U, RafileGetPosition },
	// ewe/io/RandomAccessFile_close_()Z
	{ 1092137032U, RafileClose },
	// ewe/io/RandomAccessFile_nativeSetLength_(J)I
	{ 1092140627U, RafileSetLength},
	// ewe/io/RandomAccessFile_nonBlockingRead_([BII)I
	{ 1092152918U, RafileRead },

	// ewe/database/RecordFile_nativeGetAllRecords_(I[II)I
	{ 1129863322U, RecordFileGetAllRecords },

	// ewe/datastore/DataTable_native_sortFieldData_(Lewe/datastore/FieldSortEntry;Lewe/sys/Locale;Lewe/io/RandomAccessFile;Lewe/util/IntArray;Lewe/io/DataProcessor;)I
	{ 1217925196U, DataTableSortFields },

	// java/lang/Math_IEEEremainder_(DD)D
	{ 1259240210U, MathRemainder },
	// java/lang/Math_log_(D)D
	{ 1259243207U, MathLog },
	// java/lang/Math_tan_(D)D
	{ 1259243271U, MathTan },
	// java/lang/Math_cos_(D)D
	{ 1259243399U, MathCos },
	// java/lang/Math_sin_(D)D
	{ 1259243719U, MathSin},
	// java/lang/Math_exp_(D)D
	{ 1259243911U, MathExp },
	// java/lang/Math_pow_(DD)D
	{ 1259248840U, MathPow },
	// java/lang/Math_ceil_(D)D
	{ 1259249032U, MathCeil },
	// java/lang/Math_atan_(D)D
	{ 1259249480U, MathAtan },
	// java/lang/Math_acos_(D)D
	{ 1259249608U, MathAcos },
	// java/lang/Math_asin_(D)D
	{ 1259249928U, MathAsin },
	// java/lang/Math_sqrt_(D)D
	{ 1259251912U, MathSqrt },
	// java/lang/Math_atan_(DD)D
	{ 1259253833U, MathAtan2 },
	// java/lang/Math_floor_(D)D
	{ 1259257545U, MathFloor },

	// ewe/fx/Buffer_nativeClear_(IIIII)Z
	{ 1280221587U, BufferClear },

	// ewe/ui/DisplayLineSpecs_calculate_(I)Z
	{ 1289234573U, displayLineSpecsCalculate },
	// ewe/ui/DisplayLineSpecs_getWidth_(Lewe/ui/DisplayLine;Lewe/fx/FontMetrics;I)I
	{ 1289284342U, displayLineSpecsGetWidth},

//	// apps/demo/spinner/Ticker_pause_(I)I <- For testing only.
//	{ 1373159497U, TickerPause },

//	// ewe/ui/Window_releaseMouseCapture_()V
//	{ 1406015830U, WindowReleaseCapture },
//	// ewe/ui/Window_captureAppKeys_(ILewe/ui/Window;)V
//	{ 1406016994U, WindowCaptureAppKeys},
	// ewe/ui/Window_closeWindow_()Z
	{ 1406029390U, WindowClose},
	// ewe/ui/Window_nativeGetInfo_(ILjava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;
	{ 1406035399U, WindowGetInfo },
	// ewe/ui/Window__nativeCreate_()V
	{ 1406040144U, WindowCreate },
	// ewe/ui/Window_windowToFront_()Z
	{ 1406043088U, WindowToFront },
	// ewe/ui/Window_createNativeWindow_(Lewe/fx/Rect;Ljava/lang/String;IILjava/lang/Object;)Z
	{ 1406052169U, WindowCreateNative },
	// ewe/ui/Window_doSpecialOperation_(ILjava/lang/Object;)Z
	{ 1406053992U, WindowDoSpecial },
	// ewe/ui/Window_nativeGetGuiInfo_(ILjava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;
	{ 1406054154U, WindowGetGuiInfo },
	// ewe/ui/Window_nativeSetInfo_(ILjava/lang/Object;Ljava/lang/Object;I)Z
	{ 1406066486U, WindowSetInfo },

	// ewe/reflect/Constructor_nativeNewInstance_([Lewe/reflect/Wrapper;)Ljava/lang/Object;
	{ 1629001276U, ConstructorNewInstance },

#if 0
	// ewe/io/Catalog_listCatalogs_()[Ljava/lang/String;
	{ 1661930913U, CatalogListCatalogs },
	// ewe/io/Catalog_addRecord_(I)I
	{ 1661934285U, CatalogAddRecord },
	// ewe/io/Catalog_skipBytes_(I)I
	{ 1661937741U, CatalogSkipBytes },
	// ewe/io/Catalog__nativeCreate_(Ljava/lang/String;I)V
	{ 1661940387U, CatalogCreate },
	// ewe/io/Catalog_readBytes_([BII)I
	{ 1661950736U, CatalogRead },
	// ewe/io/Catalog_deleteRecord_()Z
	{ 1661951823U, CatalogDeleteRecord },
	// ewe/io/Catalog_setRecordPos_(I)Z
	{ 1661957200U, CatalogSetRecordPos },
	// ewe/io/Catalog_getRecordSize_()I
	{ 1661957392U, CatalogGetRecordSize },
	// ewe/io/Catalog_resizeRecord_(I)Z
	{ 1661958480U, CatalogResizeRecord },
	// ewe/io/Catalog_writeBytes_([BII)I
	{ 1661959889U, CatalogWrite },
	// ewe/io/Catalog_getRecordCount_()I
	{ 1661964433U, CatalogGetRecordCount },
	// ewe/io/Catalog_close_()Z
	{ 1661972552U, CatalogClose },
	// ewe/io/Catalog_isOpen_()Z
	{ 1661978185U, CatalogIsOpen },
	// ewe/io/Catalog_delete_()Z
	{ 1661978505U, CatalogDelete },
#endif

//	{ 1678751829U, FileSysGetTempName },

	// java/lang/Class_getInterfaces_()[Ljava/lang/Class;
	{ 1712260065U, ClassGetInterfaces },
	// java/lang/Class_isInterface_()Z
	{ 1712277006U, ClassIsInterface },
//	// java/lang/Class_isAssignableFrom_(Ljava/lang/Class;)Z
//	{ 1712278756U, ClassIsAssignableFrom }, - Defunct
	// java/lang/Class_getModifiers_()I
	{ 1712283407U, ClassGetModifiers},
	// java/lang/Class_getName_()Ljava/lang/String;
	{ 1712286875U, ClassGetName},
	// java/lang/Class_getDeclaredClasses_()[Ljava/lang/Class;
	{ 1712290662U, ClassGetDeclaredClasses },
	// java/lang/Class_getClassLoader_()Ljava/lang/ClassLoader;
	{ 1712297255U, ClassGetClassLoader},
	// java/lang/Class_isInstance_(Ljava/lang/Object;)Z
	{ 1712312095U, ClassIsInstance},
	// java/lang/Class_getSuperclass_()Ljava/lang/Class;
	{ 1712321888U, ClassGetSuperClass },
	// java/lang/Class_forName_(Ljava/lang/String;)Ljava/lang/Class;
	{ 1712322092U, ClassForName},

	// ewe/database/EntriesView_nativeAdjustIndexes_([IIII)I
	{ 1759082203U, EntriesViewAdjustIndexes },
	// ewe/database/EntriesView_nativeToRanges_([IILewe/util/IntArray;)V
	{ 1759095208U, EntriesViewToRanges},

	// ewe/sys/Locale_compare_(CCI)I
	{ 1779373069U, LocaleCompareChar },
	// ewe/sys/Locale_getAllIDs_(I)[I
	{ 1779377614U, LocaleGetAllIDs },
	// ewe/sys/Locale_changeCase_(CZ)C
	{ 1779385551U, LocaleChangeCase },
	// ewe/sys/Locale_nativeGetString_(III)Ljava/lang/String;
	{ 1779399334U, LocaleGetString },
	// ewe/sys/Locale_changeCase_([CIIZ)V
	{ 1779401938U, LocaleChangeCaseArray},
	// ewe/sys/Locale_getDefaultLanguage_()Ljava/lang/String;
	{ 1779402406U, LocaleGetDefaultLanguage },
	// ewe/sys/Locale_compare_([CII[CIII)I
	{ 1779403411U, LocaleCompareString},
	// ewe/sys/Locale_parse_(Ljava/lang/String;ILjava/lang/Object;Ljava/lang/Object;)Z
	{ 1779411967U, LocaleParse },
	// ewe/sys/Locale_format_(ILjava/lang/Object;Ljava/lang/Object;)Ljava/lang/String;
	{ 1779413247U, LocaleFormat },
	// ewe/sys/Locale_fromString_(ILjava/lang/String;I)I
	{ 1779432929U, LocaleFromString },


	// ewe/net/Socket_nonBlockingWrite_([BII)I
	{ 1783566871U, SocketWrite },
	// ewe/net/Socket_getLocalPort_()I
	{ 1783585423U, SocketGetLocalPort },
	// ewe/net/Socket_getSetSockParameter_(IZIZ)I
	{ 1783586650U, SocketGetSetParameter},
	// ewe/net/Socket_nativeGetLocalHost_()Ljava/lang/String;
	{ 1783597158U, SocketGetLocalHost },
	// ewe/net/Socket_close_()Z
	{ 1783607368U, SocketClose },
	// ewe/net/Socket_isOpen_()Z
	{ 1783613001U, SocketIsOpen },
	// ewe/net/Socket_pauseUntilReady_(II)I
	{ 1783616212U, SocketPause},
	// ewe/net/Socket_checkIO_(I)I
	{ 1783619147U, SocketCheckIO },
	// ewe/net/Socket__nativeCreate_(Ljava/lang/String;ILjava/lang/String;I)V
	{ 1783622902U, SocketCreate },
	// ewe/net/Socket_nonBlockingRead_([BII)I
	{ 1783623254U, SocketRead },

	// ewe/sys/Double_is_(I)Z
	{ 1825534982U, DoubleIs },
	// ewe/sys/Double_matrixFunction_([DIII[Lewe/sys/MathFunction;[DI)V
	{ 1825536945U, DoubleMatrixFunction },
	// ewe/sys/Double_setSpecial_(I)Lewe/sys/Double;
	{ 1825552478U, DoubleSetSpecial },
	// ewe/sys/Double_toString_(III)Ljava/lang/String;
	{ 1825556895U, DoubleToString },
	// ewe/sys/Double_fromString_(Ljava/lang/String;)V
	{ 1825561759U, DoubleFromString },

	// ewe/ui/Control_doPaintChildren_(Lewe/ui/Control;ILewe/fx/Graphics;Lewe/fx/Rect;)V
	{ 1846487236U, ControlPaintChildren },

	// ewe/fx/Polygon_nativeIntersects_([I[II[I[II)Z
	{ 1875877405U, PolygonIntersects },
	// ewe/fx/Polygon_nativeIsIn_([I[IIII)Z
	{ 1875882516U, PolygonIsIn },


	// ewe/util/Utils_getIntSequence_([IIIII)V
	{ 1913586903U, UtilsGetIntSequence},
	// ewe/util/Utils_sort_([IILewe/util/CompareInts;Z)V
	{ 1913591394U, UtilsSort },
	// ewe/util/Utils_readInt_([BII)I
	{ 1913594894U, UtilsReadInt},
	// ewe/util/Utils_findCRLF_([BII)I
	{ 1913594959U, UtilsFindCRLF },
	// ewe/util/Utils_sizeofJavaUtf8String_([BII)I
	{ 1913613915U, UtilsSizeofJavaUtf8Bytes},
	// ewe/util/Utils_sizeofJavaUtf8String_([CII)I
	{ 1913613979U, UtilsSizeofJavaUtf8String },
	// ewe/util/Utils_makeHashCode_([BII)I
	{ 1913624979U, UtilsMakeHashCodeBytes },
	// ewe/util/Utils_makeHashCode_([CII)I
	{ 1913625043U, UtilsMakeHashCodeChars },
	// ewe/util/Utils_encodeJavaUtf8String_([CII[BI)I
	{ 1913626526U, UtilsEncodeJavaUtf8},
	// ewe/util/Utils_decodeJavaUtf8String_([BII[CI)[C
	{ 1913631327U, UtilsDecodeJavaUtf8},
	// ewe/util/Utils_nativeZero_(Ljava/lang/Object;II)V
	{ 1913649185U, UtilsZero },

	// java/lang/Object_getClass_()Ljava/lang/Class;
	{ 2119199131U, ObjectGetClass },
	// java/lang/Object_makeClone_()Ljava/lang/Object;
	{ 2119211037U, ObjectClone },

//	// ewe/fx/Graphics_copyRect_(Lewe/fx/ISurface;IIIIII)V <-Deprecated
//	{ 2182088099U, GraphicsCopyRect },

	// ewe/fx/Graphics_clearClip_()V
	{ 2182090124U, GraphicsClearClip },
	// ewe/fx/Graphics_setFont_(Lewe/fx/Font;)V
	{ 2182094808U, GraphicsSetFont },
	// ewe/fx/Graphics_setDrawOp_(I)V
	{ 2182095437U, GraphicsSetDrawOp },
	// ewe/fx/Graphics_setClip_(IIII)V
	{ 2182096846U, GraphicsSetClip },

	// ewe/fx/Graphics_nativeDrawImage_(Lewe/fx/Image;Lewe/fx/Image;Lewe/fx/Color;Lewe/fx/Rect;Lewe/fx/Rect;I)V
	{ 2182096924U, GraphicsScaleImage},
//	// ewe/fx/Graphics_draw3DRect_(Lewe/fx/Rect;IZLewe/fx/Color;Lewe/fx/Color;)V
//	{ 2182097147U, GraphicsDraw3DRect },
	// ewe/fx/Graphics_nativeFree_()V
	{ 2182097933U, GraphicsFree},
	// ewe/fx/Graphics_setColor_(III)V
	{ 2182099790U, GraphicsSetColor },
	// ewe/fx/Graphics_drawText_(Lewe/fx/FontMetrics;[Ljava/lang/String;Lewe/fx/Rect;IIII)V
	{ 2182100101U, GraphicsDrawTextInArea},
	// ewe/fx/Graphics_getClip_(Lewe/fx/Rect;)Lewe/fx/Rect;
	{ 2182102117U, GraphicsGetClip },
	// ewe/fx/Graphics_fillRect_(IIII)V
	{ 2182103055U, GraphicsFillRect },
	// ewe/fx/Graphics_drawLine_(IIII)V
	{ 2182103119U, GraphicsDrawLine },
	// ewe/fx/Graphics_translate_(II)V
	{ 2182103502U, GraphicsTranslate },
	// ewe/fx/Graphics_drawRect_(IIII)V
	{ 2182103503U, GraphicsDrawRect },

//	// ewe/fx/Graphics_drawDots_(IIII)V <- Deprecated
//	{ 2182104271U, GraphicsDrawDots },

	// ewe/fx/Graphics_drawText_([CIIII)V
	{ 2182115089U, GraphicsDrawChars },

	// ewe/fx/Graphics_copyGraphics_(Lewe/fx/Graphics;IIIIII)V
	{ 2182116903U, GraphicsCopyGraphics },
	// ewe/fx/Graphics_getSize_(Lewe/fx/FontMetrics;[Ljava/lang/String;IILewe/fx/Dimension;)V
	{ 2182117383U, GraphicsGetSize },
	// ewe/fx/Graphics__nativeCreate_()V
	{ 2182117456U, GraphicsCreate },
//	// ewe/fx/Graphics_drawCursor_(IIII)V <- Deprecated
//	{ 2182118865U, GraphicsDrawCursor },

	// ewe/fx/Graphics_getArcPoints_(IIIIFFI)[Ljava/lang/Object;
	{ 2182118952U, GraphicsGetArcPoints},
	// ewe/fx/Graphics_nativeSetPen_(Lewe/fx/Pen;)V
	{ 2182119324U, GraphicsSetPen },
//	// ewe/fx/Graphics_setClipRect_(IIII)V
//	{ 2182122322U, GraphicsSetClip },

	// ewe/fx/Graphics_fillEllipse_(IIII)V
	{ 2182123538U, GraphicsFillEllipse },
	// ewe/fx/Graphics_drawEllipse_(IIII)V
	{ 2182123986U, GraphicsDrawEllipse },
	// ewe/fx/Graphics_fillPolygon_([I[II)V
	{ 2182132179U, GraphicsFillPolygon },
	// ewe/fx/Graphics_drawText_(Ljava/lang/String;II)V
	{ 2182138655U, GraphicsDrawString },
	// ewe/fx/Graphics_nativeDrawImage_(Lewe/fx/Image;Lewe/fx/Image;Lewe/fx/Color;IIII)V
	{ 2182146243U, GraphicsDrawImage2 },
	// ewe/fx/Graphics_native_setColor_(III)V
	{ 2182147285U, GraphicsSetColor},
	// ewe/fx/Graphics_nativeSetBrush_(Lewe/fx/Brush;)V
	{ 2182148128U, GraphicsSetBrush },

	// ewe/datastore/DataStorage_native_getAllChildIds_(Lewe/io/RandomAccessFile;ILewe/util/IntArray;)Z
	{ 2212137671U, getAllChildIds},

	// java/lang/String_equals_(Ljava/lang/String;I)Z
	{ 2253429277U, StringEquals },

	// ewe/sys/Convert_toLong_([CII)J
	{ 2387611533U, ConvertCharsToLong},
	// ewe/sys/Convert_toDouble_([CII)D
	{ 2387624143U, ConvertCharsToDouble},
	// ewe/sys/Convert_parseInt_([CIII)I
	{ 2387629648U, ConvertParseInt},
	// ewe/sys/Convert_parseLong_([CIII)J
	{ 2387636177U, ConvertParseLong },
	// ewe/sys/Convert_formatInt_(I[CIII)I
	{ 2387641362U, ConvertFormatInt },
	// ewe/sys/Convert_toIntBitwise_(FZ)I
	{ 2387642321U, ConvertFloatToBits },
	// ewe/sys/Convert_parseDouble_([CII)D
	{ 2387644114U, ConvertParseDouble },
	// ewe/sys/Convert_formatLong_(J[CIII)I
	{ 2387647891U, ConvertFormatLong },
	// ewe/sys/Convert_toLongBitwise_(DZ)J
	{ 2387648722U, ConvertDoubleToBits },
	// ewe/sys/Convert_toString_(C)Ljava/lang/String;
	{ 2387649437U, ConvertCharToString },
	// ewe/sys/Convert_toString_(D)Ljava/lang/String;
	{ 2387649501U, ConvertDoubleToString },
	// ewe/sys/Convert_toFloatBitwise_(I)F
	{ 2387649554U, ConvertBitsToFloat },
	// ewe/sys/Convert_toString_(F)Ljava/lang/String;
	{ 2387649629U, ConvertFloatToString },
	// ewe/sys/Convert_toString_(I)Ljava/lang/String;
	{ 2387649821U, ConvertIntToString },
	// ewe/sys/Convert_toString_(J)Ljava/lang/String;
	{ 2387649885U, ConvertLongToString },
	// ewe/sys/Convert_toString_(Z)Ljava/lang/String;
	{ 2387650909U, ConvertBooleanToString },
	// ewe/sys/Convert_formatDouble_(D[CII)I
	{ 2387655828U, ConvertFormatDouble },
	// ewe/sys/Convert_toDoubleBitwise_(J)D
	{ 2387655955U, ConvertBitsToDouble },

	// ewe/security/SecureRandom_arc4random_pushb_([BI)I
	{ 2388327190U, ewe_arc4random },

	// ewe/fx/ScaleInfo_nativeScale_()V
	{ 2463188366U, ScaleInfoScale},
	// ewe/fx/ImageTool_argbConvert_(ZII[ILjava/lang/Object;II[IIII)V
	{ 2517750124U, ImageToolRGBConvert },

//	// ewe/fx/SoundClip_free_()V
//	{ 2584842823U, SoundClipFree },
//	// ewe/fx/SoundClip_stop_()Z
//	{ 2584845383U, SoundClipStop },
//	// ewe/fx/SoundClip_play_(I)Z
//	{ 2584849032U, SoundClipPlay },
//	// ewe/fx/SoundClip_play_(I)Z
//	{ 2584860831U, SoundClipInit },

	// ewe/util/WeakSet_add_(Ljava/lang/Object;)V
	{ 2597393432U, wsAdd },
	// ewe/util/WeakSet_find_(Lewe/util/ObjectFinder;)Ljava/lang/Object;
	{ 2597410800U, wsFind },
	// ewe/util/WeakSet_remove_(Ljava/lang/Object;)V
	{ 2597416283U, wsRemove },
	// ewe/util/WeakSet_getRefs_()[Ljava/lang/Object;
	{ 2597420828U, wsGetRefs },
	// ewe/util/WeakSet_contains_(Ljava/lang/Object;)Z
	{ 2597429917U, wsContains },
	// ewe/util/WeakSet_clear_()V
	{ 2597432200U, wsClear },
	// ewe/util/WeakSet_isEmpty_()Z
	{ 2597447050U, wsIsEmpty },

	// ewe/util/LinkedListElement_getPrev_(Lewe/util/LinkedListElement;I)Lewe/util/LinkedListElement;
	{ 2635815812U, LLEGetPrev },
	// ewe/util/LinkedListElement_getNext_(Lewe/util/LinkedListElement;I)Lewe/util/LinkedListElement;
	{ 2635815940U, LLEGetNext },
	// ewe/util/LinkedListElement_countInRange_(Lewe/util/LinkedListElement;Lewe/util/LinkedListElement;)I
	{ 2635847561U, LLECountRange },

	// ewe/fx/ImageCodec_toImagePixels_(Lewe/fx/pngSpecs;Lewe/io/BasicStream;Lewe/fx/Image;)Z
	{ 2786220998U, PNGToImagePixels },
	// ewe/fx/ImageCodec_toJPEGPixels_(Lewe/fx/pngSpecs;Lewe/io/BasicStream;Lewe/fx/Image;Lewe/fx/Rect;)Z
	{ 2786224019U, JPEGToImagePixels },
	// ewe/fx/ImageCodec_decodeGif_(Lewe/fx/pngSpecs;Lewe/io/BasicStream;Lewe/fx/Image;Lewe/fx/Rect;[I[B)Z
	{ 2786227924U, GifDecode},
	// ewe/fx/ImageCodec_getJPEGSpecs_(Lewe/fx/pngSpecs;Lewe/io/BasicStream;)Z
	{ 2786252214U, JPEGGetSpecs },

	// ewe/reflect/Field_nativeGetValue_(Ljava/lang/Object;Lewe/reflect/Wrapper;)Lewe/reflect/Wrapper;
	{ 2962374798U, FieldGetValue },
	// ewe/reflect/Field_nativeSetValue_(Ljava/lang/Object;Lewe/reflect/Wrapper;)Lewe/reflect/Wrapper;
	{ 2962375566U, FieldSetValue },

	// ewe/ui/MainWindow_nativeExit_(I)V
	{ 3037873230U, MainWinExit},
//	// ewe/ui/MainWindow__nativeCreate_()V
//	{ 3037886544U, MainWinCreate },
//	// ewe/ui/MainWindow__setTimerInterval_(I)V
//	{ 3037919317U, MainWinSetTimerInterval },


//	// ewe/fx/PrinterJob_nativeEnd_()V
//	{ 3046248780U, PJEnd},
	// ewe/io/SerialPort_nonBlockingWrite_([BII)I
	{ 3046248983U, SerialPortWrite},
//	// ewe/fx/PrinterJob_nativeStart_()Z
//	{ 3046264846U, PJStart},
//	// ewe/fx/PrinterJob_nativePrintPage_(Lewe/fx/Graphics;)Z
//	{ 3046265572U, PJPrintPage },
//	// ewe/fx/PrinterJob_nativeCancel_()V
//	{ 3046268175U, PJCancel},
	// ewe/io/SerialPort__nativeCreate_(Ljava/lang/String;IIII)V
	{ 3046271334U, SerialPortCreate },
	// ewe/io/SerialPort_canOpen_(Ljava/lang/String;)Z
	{ 3046278108U, SerialPortCanOpen},
//	// ewe/fx/PrinterJob_nativePrinterSetupDialog_()Z
//	{ 3046284763U, PJPrinterDialog},
//	// ewe/io/SerialPort_setFlowControl_(I)Z
//	{ 3046288978U, SerialPortSetFlowControl  },
	// ewe/io/SerialPort_close_()Z
	{ 3046289480U, SerialPortClose },
	// ewe/io/SerialPort_nativeKill_()Ljava/lang/Object;
	{ 3046291742U, SerialPortKill },
	// ewe/io/SerialPort_isOpen_()Z
	{ 3046295113U, SerialPortIsOpen },
//	// ewe/fx/PrinterJob_nativeGetPage_(Lewe/fx/PageFormat;)Lewe/fx/Graphics;
//	{ 3046298165U, PJGetPage },
//	// ewe/io/SerialPort_pauseUntilReady_(II)I
//	{ 3046298324U, SerialPortPause},
//	// ewe/fx/PrinterJob_nativeGetPrinterJob_()Lewe/fx/PrinterJob;
//	{ 3046298921U, PJGetPrinterJob},
//	// ewe/io/SerialPort__nativeCreate_(IIIZI)V
//	{ 3046299605U, SerialPortCreate },
	// ewe/io/SerialPort_nonBlockingRead_([BII)I
	{ 3046305366U, SerialPortRead},

	// ewe/reflect/Array_getSetElement_(Ljava/lang/Object;ILewe/reflect/Wrapper;Z)V
	{ 3075625402U, ArrayGetSetElement},

	// ewe/util/IntArray_nativeIndexOf_(I)I
	{ 3092415953U, IntArrayIndexOf},

	// ewe/zip/InflaterInputStream_inflateEnd_()I
	{ 3210487053U, ZLibInflateEnd },
	// ewe/zip/InflaterInputStream_inflateInit_(Z)I
	{ 3210500815U, ZLibInflateInit },
	// ewe/zip/InflaterInputStream_inflate_(Lewe/util/ByteArray;Lewe/util/ByteArray;)I
	{ 3210528756U, ZLibInflate },

	// ewe/sys/Coroutine_getCurrent_()Lewe/sys/Coroutine;
	{ 3289514720U, CoroutineGetCurrent },
	// ewe/sys/Coroutine_interrupt_()V
	{ 3289523468U, CoroutineInterrupt},
	// ewe/sys/Coroutine__nativeCreate_(I)V
	{ 3289549457U, CoroutineCreate },
	// ewe/sys/Coroutine_join_(Lewe/sys/Coroutine;I)I
	{ 3289549468U, CoroutineJoin},
	// ewe/sys/Coroutine_count_()I
	{ 3289559240U, CoroutineCount },
	// ewe/sys/Coroutine_sleep_(I)I
	{ 3289562889U, CoroutineSleep },
	// ewe/sys/Coroutine_wakeup_()V
	{ 3289566473U, CoroutineWakeup},

	// java/lang/Character_charOperation_(CI)I
	{ 3419643218U, CharacterOperation},

	// ewe/fx/PixelBuffer_nativePixbufOperation_(Ljava/lang/Object;Ljava/lang/Object;I)I
	{ 3436421757U, PixbufOperation },

	// ewe/reflect/Method_nativeInvoke_(Ljava/lang/Object;[Lewe/reflect/Wrapper;Lewe/reflect/Wrapper;)Lewe/reflect/Wrapper;
	{ 3486727331U, MethodInvoke },

/* --- FontMetrics Methods.*/

	// ewe/fx/FontMetrics_getTextWidth_(Ljava/lang/String;)I
	{ 3511879649U, FontMetricsGetStringWidth },
	// ewe/fx/FontMetrics_getFormattedTextPositions_(Ljava/lang/String;Lewe/fx/FormattedTextSpecs;[II)V
	{ 3511897037U, FontMetricsGetFormattedTextPositions },
	// ewe/fx/FontMetrics_getCharWidth_(C)I
	{ 3511903952U, FontMetricsGetCharWidth },
	// ewe/fx/FontMetrics__nativeCreate_()V
	{ 3511908432U, FontMetricsCreate },
	// ewe/fx/FontMetrics_getTextWidth_([CII)I
	{ 3511921619U, FontMetricsGetCharArrayWidth },

	// java/lang/Throwable_fillInStackTrace_(I)V
	{ 3532901588U, ThrowableFillInStackTrace },
	// ewe/util/SubString_indexOf_(C[CIIII)I
	{ 3591592721U, SubStringIndexOf },
	// ewe/util/SubString_equals_([CII[CIII)Z
	{ 3591600082U, SubStringEquals },
	// ewe/util/SubString_compare_([CII[CII)I
	{ 3591600210U, SubStringCompare },

	// ewe/zip/DeflaterOutputStream_deflateEnd_()I
	{ 3692896653U, ZLibDeflateEnd },
	// ewe/zip/DeflaterOutputStream_deflateInit_(IZ)I
	{ 3692915088U, ZLibDeflateInit },
	// ewe/zip/DeflaterOutputStream_deflate_(Lewe/util/ByteArray;Lewe/util/ByteArray;Z)I
	{ 3692944117U, ZLibDeflate },

	// ewe/sys/pipeStream_nativeClose_()V
	{ 3700640014U, PipeClose},
	// ewe/sys/pipeStream_readWrite_([BIIZ)I
	{ 3700650641U, PipeReadWriteV },

	// ewe/math/BigInteger_nativeIsProbablePrime_(Lewe/math/BigInteger;Lewe/math/BigInteger;II[Lewe/math/BigInteger;I)Z
	{ 3772029406U, BIProbablePrime},
	// ewe/math/BigInteger_nativeCheckPrimes_([Lewe/math/BigInteger;)I
	{ 3772052779U, BICheckPrimes },

	// ewe/net/InetAddress_isANetAddress_(Ljava/lang/String;)Z
	{ 3851687266U, IAIsNetAddress },
	// ewe/net/InetAddress__nativeGetAllByName_(Ljava/lang/String;Lewe/io/IOHandle;)[Lewe/net/InetAddress;
	{ 3851698256U, IAGetAllByName },
	// ewe/net/InetAddress_getLocalHostName_()Ljava/lang/String;
	{ 3851701988U, IAGetLocalHost },

	// ewe/util/DataParser_nativeParse_([Ljava/lang/Object;[I[CII)V
	{ 3885266471U, DataParserParse},


	// ewe/reflect/Reflect_getInterface_(I)Ljava/lang/String;
	{ 3906208481U, ReflectGetInterface },
	// ewe/reflect/Reflect_getSuperClass_()Ljava/lang/String;
	{ 3906211233U, ReflectGetSuperClass },
	// ewe/reflect/Reflect_newArrayInstance_(Ljava/lang/String;I)Ljava/lang/Object;
	{ 3906211383U, ReflectNewArrayInstance },
	// ewe/reflect/Reflect_nativeGetField_(Ljava/lang/String;ILewe/reflect/Field;)Z
	{ 3906214456U, ReflectGetField },
	// ewe/reflect/Reflect_getNumberOfInterfaces_()I
	{ 3906223896U, ReflectGetNumberOfInterfaces },
	// ewe/reflect/Reflect_getMethodsConstructors_(IZ)[Ljava/lang/Object;
	{ 3906223917U, ReflectGetMethodsOrConstructors },
	// ewe/reflect/Reflect_getReflectedClass_()Ljava/lang/Class;
	{ 3906227492U, ReflectGetReflectedClass },
	// ewe/reflect/Reflect_nativeGetForName_(Ljava/lang/String;)Lewe/reflect/Reflect;
	{ 3906230970U, ReflectGetForName },
	// ewe/reflect/Reflect_isArray_(Ljava/lang/Object;)Z
	{ 3906240924U, ReflectIsArray},
	// ewe/reflect/Reflect_nativeGetForObject_(Ljava/lang/Object;)Lewe/reflect/Reflect;
	{ 3906242620U, ReflectGetForObject },
	// ewe/reflect/Reflect_newArray_(I)Ljava/lang/Object;
	{ 3906246877U, ReflectNewArray },
//	// ewe/reflect/Reflect_isTypeOf_(Ljava/lang/String;)Z
//	{ 3906248605U, ReflectIsTypeOf }, - Defunct
	// ewe/reflect/Reflect_isInstance_(Ljava/lang/Object;)Z
	{ 3906260767U, ReflectIsInstance },
//	// ewe/reflect/Reflect_isAssignableFrom_(Lewe/reflect/Reflect;)Z
//	{ 3906260905U, ReflectIsAssignableFrom }, - Defunct
	// ewe/reflect/ReflectNativeSetup(Ljava/lang/Class;)V
	{ 3906262943U, ReflectNativeCreate },
	// ewe/reflect/Reflect_nativeIsTypeOf_(Ljava/lang/String;Ljava/lang/String;)Z
	{ 3906265461U, ReflectIsTypeOf2 },
	// ewe/reflect/Reflect_arrayLength_(Ljava/lang/Object;)I
	{ 3906266848U, ReflectArrayLength},
	// ewe/reflect/Reflect_nativeGetMethodConstructor_(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;Z)Z
	{ 3906269013U, ReflectGetMethodConstructor},
	// ewe/reflect/Reflect_getFields_(I)[Lewe/reflect/Field;
	{ 3906270561U, ReflectGetFields },

	// ewe/ui/formatted/TextPosition_nativeFindCharacter_(I)Z
	{ 4053672535U, FTMFindCharacter },


	// ewe/reflect/MethodConstructor_getExceptionTypes_()[Ljava/lang/Class;
	{ 4183716837U, MethodConstructorGetThrows },

	// java/lang/ClassLoader_nativeCreate_()V
	{ 4225065103U, ClassLoaderCreate},
	// java/lang/ClassLoader_defineClass_(Ljava/lang/String;[BII)Ljava/lang/Class;
	{ 4225083444U, ClassLoaderDefineClass },
#endif

	};

NativeMethod *nativeMethods = theNativeMethods;
int sizeofNativeMethods = sizeof(theNativeMethods);
