/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.15, February 13, 2002                        *
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

__IDSTRING(rcsid_nmunix_b, "$MirOS: contrib/hosted/ewe/vm/nmunix_b.c,v 1.5 2008/05/03 23:42:49 tg Exp $");

static void deleteTempFiles()
{
	LinkedElement le;
	//for (le = libraries; le != NULL; le = le->next)
		//FreeLibrary((HMODULE)le->ptr);
	for (le = filesToDelete; le != NULL; le = le->next)
		remove((TCHAR *)le->ptr);
}

#ifdef VCC
#include <conio.h>
#endif
static void debugString(const char *what)
{
#ifdef VCC
	//extern int _cputs(const char *);
	//extern int _putch(int);
	static int alloced = 0;
	if (!alloced) AllocConsole();
	//_putch('X');

	//puts(what);
	//puts("\n");
	_cputs(what);
	_cputs("\n");
#else
	printf("%s\n",what);
#endif
}
static void debugUtf(UtfString str)
{
	static char ch[256];
	int len = str.len;
	if (len > 255) len = 255;


	strncpy(ch,str.str,len);
	ch[len] = 0;
	debugString(ch);
}

static TCHAR *classPath = NULL;
#define MAX_CLASSPATHS 20
static int numClassPaths = -1;
static TCHAR *classPaths[MAX_CLASSPATHS];

#ifndef MAX_PATH
#define MAX_PATH 256
#endif

TCHAR extPath[MAX_PATH+1] = TEXT(PREFIX "/share/ewe");
TCHAR myPath[MAX_PATH+1] = TEXT("");
TCHAR newpath[MAX_PATH+1];
TCHAR programDir[MAX_PATH+1] = TEXT("");
TCHAR *eweFile = NULL;

int programDirDefined = 0;

static void setupClassPaths()
{
#define MULTIPLE
	if (numClassPaths == -1){
		if (classPath == NULL){
			TCHAR *v = getenv("CLASSPATH");
#ifdef DEBUG_LIBRARY
			v = TEXT("/hd3p1/ewe/classes/gtk:/hd3p1/ewe/classes/zaurus:/hd3p1/ewe/classes");
#endif
			int sz, wasNull = 0;
			if (v == NULL) {
				v = TEXT("./");
				wasNull = 1;
			}
			sz = textLength(v)+2+(programDirDefined ? textLength(programDir)+10 : 0)+3;
			classPath = (TCHAR *)malloc(sz*sizeof(TCHAR));
			classPath[0] = 0;
			if (programDirDefined){
				txtcat(classPath,programDir);
				txtcat(classPath,TEXT("/classes:"));
			}
			txtcat(classPath,v);
			if (!wasNull) txtcat(classPath,TEXT(":./"));
		}
		numClassPaths = 0;
		VmFlags |= VM_FLAG_USING_CLASSES;
#ifdef MULTIPLE
		if (classPath != NULL){
			TCHAR *s = classPath, *sp;
			// NOTE: we duplicate the CLASSPATH here since strtok() modifies
			// it but we never explicitly free it, we let the OS free it when
			// the program exits. Also note we don't need to deal with UNICODE
			// since this section does not applicable to WinCE
			int i = textLength(s);//xstrlen(s);
			sp = (TCHAR *)malloc(sizeof(TCHAR)*(i + 1));
			//xstrncpy(sp, s, i);
			txtncpy(sp,s,i);
			sp[i] = 0;
			s = sp;

			// parse through the elements of CLASSPATH
			sp = strtok(s, ":");
			while (sp != NULL){
				classPaths[numClassPaths++] = sp;
				if (numClassPaths == MAX_CLASSPATHS)
					break;
				sp = strtok(NULL, ":");
			}
		}
#endif
	}
}

static uchar *readFileIntoMemory(char *path,int nullTerminate,uint32 *size)
{
	FILE *in = fopen(path,"rb");
	uchar *got,*cur;
	int left, sz;
	if (in == NULL) return NULL;
	fseek(in,0,SEEK_END);
	sz = (uint32)ftell(in);
	fseek(in,0,SEEK_SET);
	if (size != NULL) *size = sz;
	got = (uchar *)malloc(sz);
	if (got == NULL) return NULL;
	for (cur = got,left = sz; left > 0;){
		int read = fread(cur,1,left,in);
		if (read < 0) {
			free(got);
			return NULL;
		}
		cur += read;
		left -= read;
	}
	return got;
}



static void pathConvert(TCHAR *src, TCHAR *dst, int max)
	{
	int start, len;

	start = 0;
	if (src[0] == '"')
		start = 1;
	len = textCopy(&src[start], dst, max);
	if (len > 0 && dst[len - 1] == '"')
		dst[len - 1] = 0;
	for (start = 0; start < len; start++)
		if (dst[start] == '\\') dst[start] = '/';
	}

static uchar *tryMemMapFile(TCHAR *path,int reportError)
	{
	MemFile memFile;
	int mapped;
	TCHAR uniPath[128];

	//debugString(path);
	if (numMemFiles == MAX_MEM_FILES && reportError)
		{
		debugString(TEXT("Too many warp files"));
		}
	memFile.fromResource = 0;
	memFile.ptr = 0;
	memFile.PooledUtf8 = 0;
	mapped = 0;

	pathConvert(path, uniPath, 128);
	if (access(path,0) != 0) return NULL;
	struct stat buff;
	stat(path,&buff);
	int length = buff.st_size;

	if (!memFileNotSupported){
		int fd = open(path,O_RDONLY);
		if (fd == -1) return NULL;
		void *mapped = mmap((caddr_t)0,length,PROT_READ,MAP_PRIVATE,fd,0);
		if (mapped == MAP_FAILED) {
			close(fd);
			memFileNotSupported = 1;
		}else{
			memFile.ptr = (uchar *)mapped;
			memFile.viewIsMapped = 1;
		}
	}
	if (memFileNotSupported)//Unix should always be able to memory map.
	{
		//printf("Cannot memory map!\n");
		// NOTE: Here we read the file into memory instead of memory
		// mapping it. This is to work around WindowsCE devices that do not
		// support "Page-In" - those devices can't memory map files that are
		// not created with CreateFileForMapping(). Its possible we could fix
		// this by using the CE routines to create a memory mapping file. We
		// could create a file for memory mapping and then copy the one on
		// the system over to it but that sounded dicey so we don't do it.
		memFile.viewIsMapped = 0;
		memFile.ptr = readFileIntoMemory(path, 0, NULL);
	}
	memFile.PooledUtf8 = checkPooled(&memFile);
	memFiles[numMemFiles++] = memFile;
	//if (memFile.ptr) debugString("Found!");
	return memFile.ptr;
	}


static uchar *memMapFile(TCHAR *path)
{

	return tryMemMapFile(path,1);
}
static uchar *tryToMapInPath(TCHAR *path,TCHAR *name)
{
	TCHAR *p = newpath;
	TCHAR *e = p+MAX_PATH;
	TCHAR *s = path;

	for (;*s != 0 && p<e; s++,p++) *p = (TCHAR)*s;
	if (p != newpath && *(p-1) != '\\' && *(p-1) != '/' && p<e)
		*p++ = '/';
	for (;*name != 0 && p<e; name++, p++) *p = *name;
	*p = 0;
	return tryMemMapFile(newpath,0);
}

struct coded {
	char *name;
	uchar *bytes;
};
#ifdef CODED_EWE_FILE
extern unsigned char EweFileBytes [];
extern unsigned char EweConfigBytes [];
struct coded codedEweFile = {"ewe.ewe",EweFileBytes};
struct coded codedEweConfig = {"EweConfig.ewe",EweConfigBytes};
#endif

struct coded *codedFiles [] =
{
#ifdef CODED_EWE_FILE
	&codedEweFile, &codedEweConfig,
#endif
	NULL
};

static uchar *tryResourceMapFile(TCHAR *name)
{
	for (int i = 0;;i++){
		struct coded *s = codedFiles[i];
		if (s == NULL) break;
		if (strcmp(s->name,name) == 0) {
			if (numMemFiles == MAX_MEM_FILES) return NULL;
			MemFile memFile;
			memFile.ptr = s->bytes;
			if (memFile.ptr == NULL) return NULL;
			memFile.fromResource = 1;
			memFileNotSupported = 1;
			memFile.viewIsMapped = 0;
			memFile.PooledUtf8 = checkPooled(&memFile);
			memFiles[numMemFiles++] = memFile;
			return memFile.ptr;
		}
	}
	return NULL;
}
static uchar *tryToMemMapFile(TCHAR *path,int reportError)
{
	TCHAR *sp = path;
	uchar *got;

	got = tryMemMapFile(path,0);
	if (got != NULL) return got;
// Get the name of the warp file alone.
	sp += textLength(sp);
	for (;sp != path && *sp != '/';sp--);
	if (*sp == '/') sp++;
// Try to find it in the module directories.
	got = tryToMapInPath(myPath,sp);
	if (got != NULL) return got;
	got = tryToMapInPath(extPath,sp);
	if (got != NULL) return got;
#ifdef ZAURUS
	got = tryToMapInPath("/home/QtPalmtop/bin/",sp);
	if (got != NULL) return got;
#endif
// Try to get it as a resource.
	got = tryResourceMapFile(sp);
	if (got != NULL) return got;
	/*
	got = tryResourceMapFile(sp,myModule,0);
	if (got != NULL) return got;
*/
	if (reportError)
		printf("File Not Found: %s\n",path);

	return got;
}

//FIX - this only deals with utf encoded names characters.

static uchar *nativeLoadClass(UtfString className, uint32 *size, uchar **PooledUtf8)
{
	static TCHAR path[MAX_PATH];
	static int did = 0;
	int i,len;
	uchar *p = NULL;
	char *cp = (char *)path;
	strncpy(cp, className.str, className.len);
	strncpy(&cp[className.len], ".class", 6);
	cp[className.len+6] = 0;
	did++;
	if (PooledUtf8) *PooledUtf8 = NULL;
	p = loadFromMem(cp, className.len+6, size, PooledUtf8);
	if (p != NULL)
		return p;
	if (numClassPaths == -1) setupClassPaths();
	for (i = 0; i < numClassPaths; i++){
		txtcpy(path, classPaths[i]);
		len = textLength(path);
		if (path[len-1] != '\\' && path[len-1] != '/')
			txtcat(path,TEXT("/"));
		txtncat(path,(TCHAR *)className.str,className.len);
		txtcat(path,TEXT(".class"));
		p = readFileIntoMemory(path, 0, size);
		if (p != NULL) break;
	}
	if (p == NULL){
		//MessageBox(NULL,path,TEXT("NOT FOUND"),MB_OK);
		return NULL;
	}
	// validate
	if (getUInt32(p) != (uint32)0xCAFEBABE){
		VmError(ERR_BadClass, NULL, &className, NULL);
		free(p);
		return NULL;
	}
//#endif
	return p;
}

static Var VmGetResource(Var stack[])
{
	WObject name = stack[0].obj;
	WObject specs = stack[1].obj;
	Var v;
	uchar *p;
	uint32 *sp,size;

	v.intValue = 0;
	if (name == 0 || specs == 0) return v;
	p = loadFromMemString(name, &size);
	if (p == NULL) return v;
	sp = (uint32 *)WOBJ_arrayStart(specs);
	sp[0] = (uint32)p;
	sp[1] = size;
	v.intValue = 1;
	return v;
}

typedef void * HINSTANCE;
typedef void * FARPROC;

#define MaxLibs 32
HINSTANCE libs[MaxLibs];
unsigned int numLibs = 0;
static TCHAR dllName[256];
#define CHR TCHAR

static NativeFunc checkLibraries(CHR *name,CHR *toFree)
{
	uint32 i;
	for (i = 0; i<numLibs; i++){
		FARPROC fp = dlsym(libs[i],name);
		dlerror();
		if (fp != NULL) {
			if (toFree) free(toFree);
			return (NativeFunc)fp;
		}
	}
	return NULL;
}
static NativeFunc defaultEweNativeMethod(WClass *iclass,WClassMethod *imethod,UtfString name);
static NativeFunc tryFindNativeMethodFunc(WClass *iclass,WClassMethod *imethod,UtfString name)
{
	unsigned int i;
	UtfString str = getUtfString(iclass,iclass->classNameIndex);
	CHR *nm = (CHR *)malloc(sizeof(CHR)*(name.len+str.len+2+4));
	CHR *n = nm, *nameAlone;
	NativeFunc got;
	*n++ = 'E'; *n++ = 'n'; *n++ = 'i'; *n++ = '_';
	for (i = 0; i<str.len; i++,n++) {
		if (str.str[i] == '/') *n = '_';
		else *n = (CHR)str.str[i];
	}
	*n++ = '_';
	nameAlone = n;
	for (i = 0; i<name.len; i++,n++) {
		if (name.str[i] == '/') *n = '_';
		else *n = (CHR)name.str[i];
	}
	*n = 0;

	if (got = checkLibraries(nm,nm)) {
		imethod->nativeUsesJValue = 1;
		return got;
	}
	if (got = checkLibraries(nm+4,nm))
		return got;
	if (got = checkLibraries(nameAlone,nm))
		return got;
	free(nm);
	return defaultEweNativeMethod(iclass,imethod,name);
}
#undef CHR
LinkedElement libraries = NULL;

static HINSTANCE myLoadLibrary(TCHAR *name)
{
	HINSTANCE got = (HINSTANCE)dlopen(name,RTLD_LAZY);
	if (got != NULL){
		LinkedElement le = (LinkedElement)malloc(sizeof(struct linked_element));
		le->next = libraries;
		libraries = le;
		le->ptr = got;
	}
	return got;
}

static int64 getWindowHandle(WObject obj);

static TCHAR *stringToNativeText(WObject string);
static void holdRelease(WObject obj,int doHold);
static void enterExitEweMonitor(int isEnter);

static Var VmLoadLibrary(Var stack[])
{
	Var v;
	HINSTANCE ll = NULL;
	v.intValue = 0;
	if (numLibs >= MaxLibs) return v;
	TCHAR *nm = stringToNativeText(stack[0].obj);
	if ((ll = myLoadLibrary(nm)) == NULL) {
		//int ret = GetLastError();
		TCHAR *full = (TCHAR *)malloc(sizeof(TCHAR)*(textLength(programDir)+textLength(nm)+2));
		TCHAR *src, *dest;
		for (dest = full,src = programDir; *src != 0;) *dest++ = *src++;
		if (dest != full)
			if (*(dest-1) != '/' && *(dest-1) != '\\')
				*dest++ = '/';
		for (src = nm; *src != 0;) *dest++ = *src++;
		*dest = 0;
		ll = myLoadLibrary(full);
		if (ll == NULL){
			//MessageBox(NULL,TEXT("Could not find library!"),TEXT("Error"),MB_OK);
			//printf("Could not find library: %s\n",nm);
			free(nm);
			free(full);
			return v;
		}
		//MessageBox(NULL,full,TEXT("LOADED!"),MB_OK);
		free(full);
	}
	free(nm);
	if (ll != NULL){
		VMAccess va;
		FARPROC fp = dlsym(ll,"WabaDLLInit");
		if (fp == NULL) {
			//printf("Could not initialize library!\n");
			//MessageBox(NULL,TEXT("Could not initialize library!"),TEXT("Error"),MB_OK);
			return v;
		}
		libs[numLibs++] = ll;
		initializeVMAccess(&va);
		((WabaDLLInit)fp)(va);
		//printf("LoadLibrary successful.\n");
		//MessageBox(NULL,TEXT("LoadLibrary successful."),TEXT("Success"),MB_OK);
		v.intValue = 1;
		return v;
	}
	v.intValue = 0;
	return v;

}

static Var returnError(uint16 error);

static Var VmGetResourceData(Var stack[])
{
	uchar *mem = (uchar *)stack[0].intValue;
	int where = stack[1].intValue;
	WObject dest = stack[2].obj;
	int st = stack[3].intValue;
	int count = stack[4].intValue;
	int len;
	Var v;

	if (dest == 0 || mem == NULL)

		return returnExError(ERR_NullObjectAccess);
	len = WOBJ_arrayLen(dest);
	if (st+count > len)
		return returnExError(ERR_IndexOutOfRange);
	memcpy((uchar *)WOBJ_arrayStart(dest)+st,mem+where,count);
	v.intValue = 1;
	return v;
}


struct byte_data stOne, stTwo;
//
// Platform independant ewe.sys.Locale definitions and declarations
//
#ifndef IS_WINDOWS
typedef int32 LCID;
#define LOCALE_SYSTEM_DEFAULT 0
#define LOCALE_USER_DEFAULT 0
#define NORM_IGNORECASE 1
#endif

static int compareTchars(LCID id,int options,TCHAR *strOne,int lenOne,TCHAR *strTwo,int lenTwo);

TCHAR *toCompareText(ByteData arr,uint16 *str,int len)
{
	int i;
	if (str == NULL) return NULL;
	if (sizeof(TCHAR) == sizeof(uint16)) return (TCHAR *)str;
	expandSpaceFor(arr,len,100,0);
	for (i = 0; i<len; i++) arr->data[i] = (char)str[i];
	return (TCHAR *)arr->data;
}
static int compareStrings(uint16 *one,int lenOne,uint16 *two,int lenTwo,int id,int options)
{
	TCHAR *strOne = NULL, *strTwo = NULL;
	//int freeOne, freeTwo;
	int ret = 0;

	if (id == 0) id = LOCALE_SYSTEM_DEFAULT;
	if (one != 0) strOne = toCompareText(&stOne,one,lenOne);
		//strOne = charsToTextInPlace(one,lenOne,&freeOne);
	if (two != 0) strTwo = toCompareText(&stTwo,two,lenTwo);
		//strTwo = charsToTextInPlace(two,lenTwo,&freeTwo);
	if (strOne == strTwo) ret = 0;
	else if (strOne == NULL) ret = -1;
	else if (strTwo == NULL) ret = 1;
	else ret = compareTchars(id,options,strOne,lenOne,strTwo,lenTwo);
	//if (freeOne) free(strOne);
	//if (freeTwo) free(strTwo);
	return ret;
}
/**
* This is the Integer (32-bit) field type.
**/
#define FIELD_INTEGER  1
/**
* This is the Long Integer (64-bit) field type.
**/
#define FIELD_LONG  2
/**
* This is the Boolean field type.
**/
#define FIELD_BOOLEAN  3
/**
* This is the String field type.
**/
#define FIELD_STRING  4
/**
* This is the double precision floating point (64-bit) type. No single-precision
* floating point type is provided.
**/
#define FIELD_DOUBLE  5
/**
* This is the byte array type.
**/
#define FIELD_BYTE_ARRAY  6
/**
* This is for a date/time value (saved as a 64-bit integer).
**/
#define FIELD_DATE_TIME  7

/**
* This is an option for a SortOrder.
**/
#define SORT_DESCENDING  0x1
/**
* This is an option for a SortOrder.
**/
#define SORT_IGNORE_CASE  0x2
/**
* This is an option for a SortOrder.
**/
#define SORT_UNKNOWN_FIRST  0x4
/**
* This is an option for a SortOrder.
**/
#define SORT_DATE_ONLY  0x8
/**
* This is an option for a SortOrder.
**/
#define SORT_TIME_ONLY 0x10

typedef int (*ByteReader)(WObject source,int location,char *dest,int length);
static int readFileBytes(WObject source,int location,char *dest,int length);
ByteReader standardReader = &readFileBytes;

typedef struct field_data {
	WObject stream;
	WObject decryptor;
	WClass *clazz;
	WClassMethod *method;
	WObject input,output;
	ByteReader reader;
	int forWho;
	char idRead;
	int type;
	struct byte_data source;
	struct byte_data field;
	union {
		int intValue;
		int64 longValue;
		double doubleValue;
	} value;
} *FieldData;

typedef struct sort_data {
	LCID lcid;
	char id[4];
	int type[4];
	int options;
	int stringOptions;
	struct field_data field[2];
}*SortData;


struct sort_data sortData;
struct field_data fieldData;
WClass *byteArrayClass = NULL;
static char entrybuff[32];

void setupSortData(SortData sd,WObject sortEntry,WObject locale,WObject stream,WObject decryptor)
{
	Var *se = objectPtr(sortEntry);
	int i;
	sd->options = se[2].intValue;
	sd->field[0].stream = sd->field[1].stream = stream;
	sd->field[0].reader = sd->field[1].reader = standardReader;
	sd->field[0].decryptor = sd->field[1].decryptor = decryptor;

	if (!decryptor){
		sd->field[0].clazz = sd->field[1].clazz = NULL;
		sd->field[0].input = sd->field[1].input = 0;
		sd->field[0].output = sd->field[1].output = 0;
	}else{
		WClass *cl = WOBJ_class(decryptor);
		sd->field[0].method = sd->field[1].method =
			getMethod(cl,createUtfString("processBlock"),
			createUtfString("([BIIZLewe/util/ByteArray;)Lewe/util/ByteArray;"),&cl);
		sd->field[0].clazz = sd->field[1].clazz = cl;
		if (byteArrayClass == NULL)
			byteArrayClass = getClass(createUtfString("ewe/util/ByteArray"));
		pushObject(sd->field[0].input = sd->field[1].input =
			createObject(byteArrayClass));
		objectPtr(sd->field[0].input)[1].obj = wAlloc(0,NULL);
		pushObject(sd->field[0].output = sd->field[1].output =
			createObject(byteArrayClass));
		objectPtr(sd->field[0].output)[1].obj = wAlloc(0,NULL);
	}
	for (i = 0; i<4; i++){
		sd->id[i] = (char)(se[3+i].intValue & 0xff);
		sd->type[i] = se[7+i].intValue;
	}
	sd->field[1].forWho = sd->field[0].forWho = 0;
	sd->lcid = objectPtr(locale)[1].intValue;
	sd->stringOptions = 0;
	if (sd->options & SORT_IGNORE_CASE) sd->stringOptions = NORM_IGNORECASE;
}
//
//Add an integer to the ByteData which is acting as a buffer.
//
int addInt(ByteData bd,int value)
{
	int nl = (bd->length+1)*4;
	if (nl >= bd->space) expandSpaceFor(bd,nl,40,1);
	((int *)bd->data)[bd->length] = value;
	bd->length++;
	return 1;
}

char * expandIt(WObject byteArray,int size)
{
	WObject bytes = objectPtr(byteArray)[1].obj;
	if (WOBJ_arrayLen(bytes) < size)
		bytes = objectPtr(byteArray)[1].obj = wAlloc(size,NULL);
	return (char *)WOBJ_arrayStart(bytes);
}
//
// Get the source entry data for a particular location.
// After calling this you can call getFieldValue().
//
int getEntryData(FieldData fd,int entryLocation)
{
	if (fd->forWho == entryLocation) return 1;
	fd->idRead = 0;
	if (fd->reader(fd->stream,entryLocation+16,entrybuff,8) != 8) return 0;
	else{
		int where = readInt(entrybuff,4);
		int size = readInt(entrybuff+4,4);
		fd->forWho = entryLocation;
		if (fd->decryptor){
			if (fd->reader(fd->stream,where,entrybuff,4) != 4) return 0;
			size = readInt(entrybuff,4);
			where += 4;
		}
		expandSpaceFor(&fd->source,size,16,0);
		if (fd->reader(fd->stream,where,fd->source.data,size) != size) return 0;
// Decrypt here.
		if (fd->decryptor != 0 && fd->method != NULL){
			Var pars[6];
			memcpy(expandIt(fd->input,size),fd->source.data,size);
			pars[0].obj = fd->decryptor;
			pars[1].obj = objectPtr(fd->input)[1].obj;
			pars[2].intValue = 0;
			pars[3].intValue = size;
			pars[4].intValue = 1;
			pars[5].obj = fd->output;
			executeTopMethod(fd->clazz,fd->method,pars,6);
			if (thrownException != 0){
				thrownException = 0;
				return 0;
			}
			memcpy(fd->source.data,WOBJ_arrayStart(objectPtr(fd->output)[1].obj),size);
		}
		fd->source.length = readInt(fd->source.data,4);
		return 1;
	}
}
//
//Call this after you have copied the field data (including the 4 byte
//length header) into the fd->source structure.
//
int getFieldValue(FieldData fd,char id,int type)
{
	char *cp = fd->source.data+4, *max = cp+fd->source.length;
	int len = 0;
	if (fd->idRead == id) return 1;
	fd->idRead = id;
	fd->type = type;
	while (cp < max){
		char sid = *cp++;
		len = 0;
		cp++; //Reserved byte.
		len = ((int)*(cp++) & 0xff) << 8;
		len += (int)*(cp++) & 0xff;
		if (sid == id) break;
		cp += len;
	}
	if (cp >= max){
		fd->idRead = 0;
		return 1;
	}
// Found the ID,now get the encoded data.
	switch(type){
	case FIELD_STRING:{
		int numChars = sizeofJavaUtf8String((unsigned char *)cp,len);
		expandSpaceFor(&fd->field,numChars*2,16,0);
		javaUtf8ToStringData((unsigned char *)cp,(uint16 *)fd->field.data,len);
		fd->field.length = numChars;
		return 1;
					  }
	default:
		fd->field.length = len;
		expandSpaceFor(&fd->field,len,16,0);
		memcpy(fd->field.data,cp,len);
		return 1;
	}
	return 1;
}

static int64 loadALong(char *from);

static int compareStrings(uint16 *one,int lenOne,uint16 *two,int lenTwo,int localeID,int options);
static int compareDoubles(char *one,char *two)
{
	int64 o = loadALong(one);
	int64 t = loadALong(two);
	double d1 = *((double *)&o);
	double d2 = *((double *)&t);
	if (d1 > d2) return 1;
	else if (d1 < d2) return -1;
	else return 0;
}
static int compareInts(char *one,char *two,int len)
{
	if (len <= 4){
		int o = readInt(one,len);
		int t = readInt(two,len);
		if (o > t) return 1;
		else if (o < t) return -1;
		else return 0;
	}else{
		int64 o = loadALong(one);
		int64 t = loadALong(two);
		if (o > t) return 1;
		else if (o < t) return -1;
		else return 0;
	}
}
static int compareDates(char *one,char *two,int options)
{
	int o = readInt(one,4) & 0x01ffffff; //Year - month - day
	int t = readInt(two,4) & 0x01ffffff;
	int cmp = 0;

	if ((options & SORT_TIME_ONLY) == 0)
		cmp = o-t;

	if (cmp != 0) return cmp;

	o = readInt(one+4,4) & 0x07ffffff; // Hour - Min - Sec - Milli
	t = readInt(two+4,4) & 0x07ffffff;

	if ((options & SORT_DATE_ONLY) == 0)
		cmp = o-t;

	return cmp;
}
//
// Call this AFTER you have got the field values.
//
int compareFieldValues(SortData sd,int whichField)
{
	FieldData one = &sd->field[0], two = &sd->field[1];
	if (one->idRead == 0)
		return (sd->options & SORT_UNKNOWN_FIRST) ? 1 : -1;
	if (two->idRead == 0)
		return (sd->options & SORT_UNKNOWN_FIRST) ? -1 : 1;

	switch(sd->type[whichField]){
	case FIELD_STRING:{
		return compareStrings((uint16 *)one->field.data,one->field.length,(uint16 *)two->field.data,two->field.length,sd->lcid,sd->stringOptions|NORM_IGNORECASE);
					  }
	case FIELD_INTEGER:
	case FIELD_BOOLEAN:
	case FIELD_LONG:
		return compareInts(one->field.data,two->field.data,one->field.length);
	case FIELD_DOUBLE:
		return compareDoubles(one->field.data,two->field.data);
	case FIELD_DATE_TIME:
		return compareDates(one->field.data,two->field.data,sd->options);
	}
	return 0;
}

//
// Do a full get and compare of two locations, for one field.
//
int getAndCompareOne(SortData sd,int whichField,int oneLocation,int twoLocation,int *error)
{
	FieldData one = &sd->field[0], two = &sd->field[1];
	if (!getEntryData(one,oneLocation))
		return *error = 1;
	if (!getEntryData(two,twoLocation))
		return *error = 1;
	if (!getFieldValue(one,sd->id[whichField],sd->type[whichField]))
		return *error = 1;
	if (!getFieldValue(two,sd->id[whichField],sd->type[whichField]))
		return *error = 1;
	*error = 0;
	return compareFieldValues(sd,whichField);
}
//
// Do a full get and compare of two locations, for all.
//
int fullCompare(SortData sd,int oneLocation,int twoLocation,int *error)
{
	int ret = 0;
	int i = 0;
	*error = 0;

	for (i = 0; i<4; i++){
		if (ret != 0 || sd->id[i] == 0) return ret;
		ret = getAndCompareOne(sd,i,oneLocation,twoLocation,error);
		if (*error) return 0;
	}
	return ret;
}

//==================================================================
static int mergeFieldValues(SortData sd,int32 source[],int sourceLen,int one,int two,int length,int32 dest[])
//==================================================================
{
	int descending = sd->options & SORT_DESCENDING;
	int o = one, t = two, d = one;
	int omax = one+length, tmax = two+length;
	if (omax > sourceLen) omax = sourceLen;
	if (tmax > sourceLen) tmax = sourceLen;
	while(1) {
		if (o >= omax) {
			if (t >= tmax) return 0;
			dest[d++] = source[t++];
		}else {
			if (t >= tmax) dest[d++] = source[o++];
			else {
				int error = 0;
				int c = fullCompare(sd,source[o],source[t],&error);//compare(source[o],source[t]);
				if (((c <= 0) && !descending) || ((c > 0) && descending))
					dest[d++] = source[o++];
				else
					dest[d++] = source[t++];
				if (error != 0)
					return error;
			}
		}
	}
}


static int sortLocation(SortData sd,int32 locations[],int sourceLen)
{
	int len = sourceLen;
	int32 *source = locations;
	int32 *buff;
	int32 *dest;
	int32 *temp;
	int mergeLength = 1, passes = 0;
	buff = (int32 *)xmalloc(sizeof(int32)*len);
	dest = buff;
	while(1) {
		int mergesDid = 0, one = 0, two = 0;
		while(1) {
			int ret = 0;
			if (one >= len) break;
			two = one+mergeLength;
			if ((two >= len) && (mergesDid == 0)) break;
			mergesDid++;
			ret = mergeFieldValues(sd,source,sourceLen,one,two,mergeLength,dest);
			if (ret != 0) return ret;
			one += mergeLength*2;

		}
		if (mergesDid == 0) break;
		temp = dest; dest = source; source = temp;
		mergeLength *= 2;
	}
	if (source != locations)
		memcpy(locations,source,sourceLen*sizeof(int32));
	xfree(buff);
	return 0;
}

int doSortFields(SortData sd,WObject sortEntry,WObject locale,WObject stream,WObject intArray,WObject decryptor)
{
	int length = objectPtr(intArray)[2].intValue;
	int32 *from = (int32 *)malloc(sizeof(int32)*length);
	int ret;
	memcpy(from,WOBJ_arrayStart(objectPtr(intArray)[1].obj),length*sizeof(int32));
	setupSortData(sd,sortEntry,locale,stream,decryptor);
	ret = sortLocation(sd,from,objectPtr(intArray)[2].intValue);
	memcpy(WOBJ_arrayStart(objectPtr(intArray)[1].obj),from,length*sizeof(int32));
	free(from);
	if (sd->field[0].input) {popObject(); popObject();}
	return ret;
}

Var DataTableSortFields(Var stack[])
{
	Var v;
	v.intValue = doSortFields(&sortData,stack[1].obj,stack[2].obj,stack[3].obj,stack[4].obj,stack[5].obj);
	return v;
}
//
//Get all the child ids of a location.
//
int getChildIds(ByteData bd,int entryLocation,ByteReader reader,WObject stream)
{
	if (reader(stream,entryLocation+12,entrybuff,4) != 4) return 0;
	else{
		int child = 0;
		while(1){
			child = readInt(entrybuff,4);
			if (child == 0) break;
			addInt(bd,child);
//
// Testing - try reading the first field.
//
			/*
			fieldData.stream = stream;
			getEntryData(&fieldData,child);
			getFieldValue(&fieldData,2,FIELD_STRING);
			*/
//
			if (reader(stream,child,entrybuff,4) != 4) return 0;
		}
		return 1;
	}
}
static Var getAllChildIds(Var stack[])
{
	WObject stream = stack[0].obj;
	int parentLocation = stack[1].intValue;
	ByteData bd = (ByteData)malloc(sizeof(struct byte_data));
	WObject intArray = stack[2].obj;
	WObject intList = objectPtr(intArray)[1].obj;
	int len;
	Var v;
	v.intValue = 1;
	bd->data = NULL;
	bd->space = bd->length = 0;
	getChildIds(bd,parentLocation,standardReader,stream);

	len = bd->length;
	if (intList != 0){
		if (WOBJ_arrayLen(intList) < len)
			intList = 0;
	}
	if (intList == 0){
		intList = createArrayObject(arrayType('I'),len);
		if (intList != 0)
			objectPtr(intArray)[1].obj = intList;
	}
	if (intList != 0){
		memcpy(WOBJ_arrayStart(intList),bd->data,sizeof(int)*len);
		objectPtr(intArray)[2].intValue = len;
	}
	freeData(bd);
	free(bd);
	return v;
}

/*
static uchar *nativeLoadClass(UtfString className, uint32 *size)
	{
//className will be Java UTF encoded.
	uchar *p;
	uint16 len;
	static struct byte_data fullName;
	TCHAR path[256];
	char *cp;
	int i;
	// try loading from memory mapped files first
	// make full path by appending .class
	len = className.len + 6;
	if (len > 128)
		return NULL;

	cp = (char *)path;
	xstrncpy(cp, className.str, className.len);
	xstrncpy(&cp[className.len], ".class", 6);
	p = loadFromMem(cp, len, size);
	if (p != NULL)
		return p;

	fullName.length = utf8ToStringData(className,&fullName);
#ifndef UNICODE //Must convert to pure ascii.
	{
		uint16 *s = (uint16 *)fullName.data;
		char *d = fullName.data;
		int i = 0;
		for(i = 0; i<fullName.length; i++)
			*d++ = (char)*s++;
	}
#endif
	if (numClassPaths == -1) setupClassPaths();
//#ifndef WINCE

	// NOTE: we never free the memory pointers we allocate here. We let the
	// OS clean up memory when the process exits. This works well but if we
	// ever do free these pointers, we need to make sure we differentiate
	// them from the memory mapped file pointers (nativeLoadClassFromMem())
	for (i = 0; i < numClassPaths; i++)
		{
		txtcpy(path, classPaths[i]);
		len = textLength(path);
		if (path[len-1] != '\\')
			txtcat(path,TEXT("/"));
		txtncat(path,(TCHAR *)fullName.data,fullName.length);
		//xstrncpy(&path[len], className.str, className.len);
		txtcat(path,TEXT(".class"));
		//xstrncpy(&path[len], ".class", 6);
		//MessageBox(NULL,path,TEXT("Path"),MB_OK);
		p = readFileIntoMemory(path, 0, size);
		if (p != NULL)
			break;
		}
	if (p == NULL){
		//MessageBox(NULL,path,TEXT("NOT FOUND"),MB_OK);
		return NULL;
	}
	// validate
	if (getUInt32(p) != (uint32)0xCAFEBABE)
		{
		VmError(ERR_BadClass, NULL, &className, NULL);
		xfree(p);
		return NULL;
		}
//#endif
	return p;
	}
*/

void NativeStartNewThread(struct threadFunctionData *tfd)
{
}

TCHAR *stringToNativeText(WObject obj);

static Var VmExec(Var stack[])
{
	WObject file = stack[0].obj;
	WObject arg = stack[1].obj;
	if (file == 0) return returnVar(0);
	TCHAR *target = stringToNativeText(file);
	TCHAR *args = arg == 0 ? NULL : stringToNativeText(arg);
	//printf("I am going to run: %s, %s\n",target,(args == NULL) ? "NULL" : args);
	int f = fork();
	if (f == -1) return returnVar(0);
	else if (f != 0) {
		if (target) free(target);
		if (args) free(args);
		//printf("Forked, and I am the parent!\n");
		return returnVar(1);
	}
	//printf("Forked, and I am the child!\n");
	if (args != NULL) execl(target,target,args,(char *)NULL);
	else execl(target,target,(char *)NULL);
	exit(0);
	return returnVar(1);
}
