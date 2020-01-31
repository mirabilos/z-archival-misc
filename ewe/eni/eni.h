/* $MirOS: contrib/hosted/ewe/eni/eni.h,v 1.4 2008/04/30 22:02:13 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser <tg@mirbsd.de>                *
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

#ifndef EWEDEFS_DEFINED
#define EWEDEFS_DEFINED

#if defined(LINUX) || defined(unix) || defined(__unix__)
#ifndef UNIX
#define UNIX
#endif
#endif

#ifdef _WIN32_WCE
#ifndef WIN32
#define WIN32
#endif
#endif

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#define uchar unsigned char
#define int32 int
#define uint32 unsigned int
#define float32 float
#define int16 short
#define uint16 unsigned short
typedef _int64 int64;

#else

#ifndef UNIX
#define UNIX
#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

typedef long long int64;
typedef long long _int64;
typedef unsigned long long uint64;
typedef char byte;
typedef int int32;
typedef unsigned int uint32;
typedef float float32;
typedef unsigned char uchar;
typedef short int16;
typedef unsigned short uint16;

typedef int DWORD;
typedef int BOOL;
typedef char TCHAR;
typedef unsigned short WCHAR;

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#endif

typedef uint32 WObject;
typedef union
	{
	int32 intValue;
	float32 floatValue;
	void *classRef;
	uchar *pc;
	void *refValue;
	WObject obj;
	int32 half64;
	} Var;

typedef Var (*NativeFunc)(Var stack[]);
typedef void (*ObjDestroyFunc)(WObject obj);

//
// more types and accessors
//

#ifdef WIN32
#define DllExport __declspec(dllexport)
#define DllImport __declspec(dllimport)
#define EweCall
#define EWECALL
#define EweExport DllExport
#define EWEEXPORT EweExport
#else
#define DllExport
#define DllImport
#define EweCall
#define EWECALL
#define EweExport DllExport
#define EWEEXPORT EweExport
#endif


#define WOBJ_class(o) (WClass *)((objectPtr(o)[0].intValue)& ~1)
//#define WOBJ_class(o) (vm.objectPtr(o))[0].classRef
//#define WOBJ_var(o, idx) (vm.objectPtr(o))[idx + 1]

// NOTE: These get various values in objects at defined offsets.
// If the variables in the base classes change, these offsets will
// need to be recomputed. For example, the first (StringCharArray)
// get the character array var offset in a String object.

#define WOBJ_StringCharArrayObj(o) (vm.objectPtr(o))[1].obj
#define WOBJ_StringBufferStrings(o) (vm.objectPtr(o))[1].obj
#define WOBJ_StringBufferCount(o) (vm.objectPtr(o))[2].intValue

#define WOBJ_arrayType(o) (vm.objectPtr(o))[1].intValue
#define WOBJ_arrayLen(o) (vm.objectPtr(o))[2].intValue

#define WOBJ_arrayComponent(o) (vm.objectPtr(0))[3].refValue
#define WOBJ_arrayStart(o) (&(vm.objectPtr(o)[4]))

// for faster access
#define WOBJ_arrayTypeP(objPtr) (objPtr)[1].intValue
#define WOBJ_arrayLenP(objPtr) (objPtr)[2].intValue
#define WOBJ_arrayComponentP(objPtr) (objPtr)[3].refValue
#define WOBJ_arrayStartP(objPtr) (&(objPtr[4]))

typedef struct UtfStringStruct
	{
	char *str;
	uint32 len;
	} UtfString;
/*
typedef union
	{
	// FieldVar is either a reference to a static class variable (staticVar)
	// or an offset of a local variable within an object (varOffset)
	Var staticVar;
	uint32 varOffset; // computed var offset in object
	} FieldVar;

typedef struct WClassFieldStruct
	{
	uchar *header;
	FieldVar var;
    FieldVar var2; //64Bits
	int flags;
	} WClassField;

#define FIELD_accessFlags(f) getUInt16(f->header)
#define FIELD_nameIndex(f) getUInt16(&f->header[2])
#define FIELD_descIndex(f) getUInt16(&f->header[4])
#define FIELD_isStatic(f) ((FIELD_accessFlags(f) & ACCESS_STATIC) > 0)

typedef union
	{
	// Code is either pointer to bytecode or pointer to native function
	// NOTE: If accessFlags(method) & ACCESS_NATIVE then nativeFunc
	// is set, otherwise codeAttr is set. Native methods don't have
	// maxStack, maxLocals so it is OK to merge the codeAttr w/nativeFunc.
	uchar *codeAttr;
	NativeFunc nativeFunc;
	} Code;
typedef struct WClassMethodStruct
	{
	uchar *header;
	Code code;
	uint16 directMap:   8;
	uint16 numParams:   5; //32 Parameters max.
	uint16 returnsValue:2;
	uint16 isInit:      1;
	uint16 numHandlers;
	//WExceptionHandler
	void *handlers;
	uchar *lineNumbers;
	unsigned int parameterIs64Bits;
	void *wclass;
	} WClassMethod;
*/
/*
#define METH_accessFlags(m) getUInt16(m->header)
#define METH_nameIndex(m) getUInt16(&m->header[2])
#define METH_descIndex(m) getUInt16(&m->header[4])
#define METH_maxStack(m) getUInt16(&m->code.codeAttr[6])
#define METH_maxLocals(m) getUInt16(&m->code.codeAttr[8])
#define METH_codeCount(m) getUInt32(&m->code.codeAttr[10])
#define METH_code(m) &m->code.codeAttr[14]

#define CONS_offset(wc, idx) wc->constantOffsets[idx - 1]
#define CONS_ptr(wc, idx) (wc->byteRep + CONS_offset(wc, idx))
#define CONS_tag(wc, idx) CONS_ptr(wc, idx)[0]
#define CONS_utfLen(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_utfStr(wc, idx) &CONS_ptr(wc, idx)[3]
#define CONS_integer(wc, idx) getInt32(&CONS_ptr(wc, idx)[1])
#define CONS_float(wc, idx) getFloat32(&CONS_ptr(wc, idx)[1])
#define CONS_stringIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_classIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_nameAndTypeIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[3])
#define CONS_nameIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_typeIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[3])
*/

#define ARRAY_BYTE 8
#define ARRAY_BOOLEAN 4
#define ARRAY_OBJECT 1
#define ARRAY_ARRAY 2
#define ARRAY_CHAR 5
#define ARRAY_SHORT 9
#define ARRAY_FLOAT 6
#define ARRAY_INT 10
#define WClass void
#define WClassField void
#define WClassMethod void

typedef struct sort_info *SortInfo;
typedef int (* CompareFunction)(SortInfo info,int one,int two,int *error);

struct sort_info {
	int32 *original, *source, *dest;
	int sourceLen;
	int descending;
	CompareFunction function;
	void *functionData;
};

typedef union JValue {
	int z;
	signed char    b;
	uint16     c;
	int16   s;
	int32     i;
	int64    j;
	float   f;
	double  dDontUse;
	int32  l;
} JValue;

typedef int ObjectRef;
typedef int ClassRef;
typedef int FieldRef;
typedef int MethodRef;
typedef int VMRef;

//
// These are functions in the ewe VM which you may need to call.
// This will be expanded over time.
//
typedef struct vmAccess{
	// These are function pointers to VM function.
	Var *(*objectPtr)(WObject);
	void (*asyncCallBack)(WObject,WObject);

	void (*asyncExecuteMethod)
		(WClass *wclass, WClassMethod *method,
		Var params[], uint32 numParams,Var *returnValue,Var *returnHigh);
	Var (*returnDouble)(double);
	Var (*returnLong)(int64);
	void (*doubleToVar)(double,Var *);
	double (*varToDouble)(Var *);
	void (*longToVar)(int64,Var *);
	int64 (*varToLong)(Var *);
	WObject (*createNewString)(int length,uint16** dataPtr,uint16 *text);
	WObject (*createArray)(char *type,int length);
	WClass *(*getClass)(char *name);
	WClassField *(*getField)(WClass *clazz,char *name,char *desc);
	int (*pushObject)(WObject);
	WObject (*popObject)();
	int (*sort)(SortInfo info);
	int (*compareStrings)(uint16 *one,int lenOne,uint16 *two,int lenTwo,int localeID,int options);
	void (*holdRelease)(WObject obj,int doHold);
	int64 (*getWindowHandle)(WObject);
	WObject (*suspendResumeCoroutine)(WObject);
	WClassMethod *(*getMethod)(WClass *clazz,char *name,char *desc,WClass **actualClazz);
	int (*invokeMethod)(WClassMethod *method,WObject obj,JValue parameters [],JValue *ret,int nonVirtual);
	WObject (*createNewObject)(WClass *clazz);
	WObject (*getSetException)(int isGet,WObject cur);
	int (*isAssignableFrom)(WClass *clazz,WClass *target);
	void (*enterExitEweMonitor)(int isEnter);
	WClass *(*getSuperclass)(WClass *clazz);
	double (*getSetDoubleJValue)(double value,JValue *jv,int isGet);
	Var *(*getFieldVarPointer)(WClassField *field,WObject obj);
	void (*externalThreadEnding)();
}VMAccess;

extern VMAccess vm;
#define Eni &vm

#ifdef NO_EWE_METHODS
#define NO_DLL_SETUP
#endif

#ifndef NO_DLL_SETUP
//***********************************************************************
// This setups up the DLL for ewe.
//***********************************************************************
/**
* REQUIRED - This is the DLL start routine. You should not need to add any code
* within it.
**/
#ifdef WIN32
//===================================================================
BOOL APIENTRY DllMain(HANDLE hModule,DWORD  ul_reason_for_call,LPVOID lpReserved)
//===================================================================
{
    switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:
			break;
    }
    return TRUE;
}
#endif
/**
* REQUIRED - This is a pointer to a structure which contains info about the running Waba VM,
* including function addresses and variable addresses.
**/
//===================================================================
VMAccess vm;
extern "C" {
DllExport int WabaDLLInit(VMAccess vma)
{
	vm = vma;
	return 0;
}
}
//===================================================================
#endif //NO_DLL_SETUP


#ifndef NO_UTILITIES
//***********************************************************************
// These are utilities which are useful and which are needed by the Object classes
//***********************************************************************
//==================================================================
static int merge(SortInfo info,int one,int two,int length)
//==================================================================
{
	int o = one, t = two, d = one;
	int omax = one+length, tmax = two+length;
	int sl = info->sourceLen;
	int *dest = info->dest, *source = info->source;
	if (omax > sl) omax = sl;
	if (tmax > sl) tmax = sl;
	while(1) {
		if (o >= omax) {
			if (t >= tmax) return 0;
			dest[d++] = source[t++];
		}else {
			if (t >= tmax) dest[d++] = source[o++];
			else {
				int error = 0;
				int c = info->function(info,source[o],source[t],&error);
				if (error != 0) return error;
				if (((c <= 0) && !info->descending) || ((c > 0) && info->descending))
				 dest[d++] = source[o++];
				else dest[d++] = source[t++];
			}
		}
	}
}
//==================================================================
static int sort(SortInfo info)
//==================================================================
{
	int len = info->sourceLen;
	int32 *what = info->original;
	int32 *source = what;
	int32 *buff;
	int32 *dest;
	int32 *temp;
	int mergeLength = 1;
	buff = new int32[len];
	dest = buff;
	while(1) {
		int mergesDid = 0, one = 0, two = 0;
		info->dest = dest;
		info->source = source;
		while(1) {
			int ret = 0;
			if (one >= len) break;
			two = one+mergeLength;
			if ((two >= len) && (mergesDid == 0)) break;
			mergesDid++;
			ret = merge(info,one,two,mergeLength);
			if (ret != 0) return ret;
			one += mergeLength*2;

		}
		if (mergesDid == 0) break;
		temp = dest; dest = source; source = temp;
		mergeLength *= 2;
	}
	if (source != what)
		memcpy(what,source,info->sourceLen*sizeof(int32));
	delete buff;
	return 0;
}
static int stringLength(const WCHAR *chars)
{
#ifdef WIN32
	return wcslen(chars);
#else
	if (chars == NULL) return 0;
	for (int i = 0;; i++)
		if (*chars++ == 0) return i;
#endif
}
//===================================================================
static int textLength(const TCHAR *text)
//===================================================================
{
	if (text == NULL) return 0;
	int i = 0;
	for (i = 0; text[i] != 0; i++)


		;
	return i;
}
//===================================================================
static void txtcpy(TCHAR *dest,const TCHAR *src,int maxChars = -1)
//===================================================================
{
	for (int i = 0; i<maxChars || maxChars < 0; i++,src++,dest++){
		*dest = *src;
		if (*src == 0) break;
	}
	*dest = 0;
}
//===================================================================
int sizeofJavaUtf8String(const unsigned char *data,int numberOfBytes)

//===================================================================
{
	int i = 0, size = 0;
	for (i = 0; i<numberOfBytes; i++,size++){
		unsigned char c = *data++;
		if ((c & 0x80) == 0) continue;
		else if ((c & 0xe0) == 0xc0) {
			data++;
			i++;
		}
		else if ((c & 0xf0) == 0xe0) {
			data += 2;
			i += 2;
		}
	}
	return size;
}
//===================================================================
int javaUtf8ToStringData(const unsigned char *data,int numberOfBytes,WCHAR *text)
//===================================================================
{
	int i = 0, t = 0;
	for (i = 0; i<numberOfBytes; i++,t++){
		unsigned char c = *data++;
		if ((c & 0x80) == 0) {
			text[t] = (WCHAR)c;
			continue;
		}else if ((c & 0xe0) == 0xc0) {
			text[t] = (((WCHAR)c & 0x1f)<<6) + ((WCHAR)*(data) & 0x3f);
			data++;
			i++;
		}else if ((c & 0xf0) == 0xe0) {
			text[t] = (((WCHAR)c & 0x0f)<<12) + (((WCHAR)*(data) & 0x3f)<<6)+((WCHAR)*(data+1) & 0x3f);
			data += 2;
			i += 2;

		}
	}
	return t;
}
/**
* Returns the number of bytes needed to encode a String in the Java UTF8 format.
**/
//===================================================================
int sizeofEncodedJavaUtf8String(const WCHAR *toEncode,int length)
//===================================================================
{
	int max = length;
	int size = 0;
	int i;
	if (toEncode == NULL) return 0;
	for (i = 0; i<max; i++){
		uint16 c = toEncode[i];
		size++;
		if (c >= 0x1 && c <= 0x7f) continue;

		else if (c == 0 || (c >= 0x80 && c <= 0x7ff)) size++;
		else size += 2;
	}
	return size;
}
//===================================================================
int	encodeJavaUtf8String(const WCHAR *toEncode,int length,unsigned char *destination)
//===================================================================
{
	int max = length;
	int size = 0;
	int i;
	if (toEncode == NULL) return 0;
	for (i = 0; i<max; i++){
		uint16 c = toEncode[i];
		if (c >= 0x1 && c <= 0x7f) {
			destination[size++] = (unsigned char)c;
		}else if (c == 0 || (c >= 0x80 && c <= 0x7ff)) {
			destination[size++] = (unsigned char)(0xc0 | ((c >> 6) & 0x1f));
			destination[size++] = (unsigned char) (0x80 | (c & 0x3f));
		}else{

			destination[size++] = (unsigned char)(0xe0 | ((c >> 12) & 0xf));
			destination[size++] = (unsigned char) (0x80 | ((c >> 6) & 0x3f));
			destination[size++] = (unsigned char) (0x80 | (c  & 0x3f));
		}
	}
	return size;
}
// Convert a Unicode string into a null terminated Utf8 encoded native text string.
// If length is < 0 then it will assume that the incoming data is also
// null terminated.
//===================================================================
char *toTextUtf8(const WCHAR *data,int length = -1)
//===================================================================
{
	if (length < 0){
		int i = 0;
		for (i = 0; data[i] != 0; i++)
			;
		length = i;
	}
	int size = sizeofEncodedJavaUtf8String(data,length);
	char *ret = new char[size+1];
	encodeJavaUtf8String(data,length,(unsigned char *)ret);
	ret[size] = 0;
	return ret;
}

// Convert a Unicode string into a null terminated native text string.
// If length is < 0 then it will assume that the incoming data is also
// null terminated.
//===================================================================
TCHAR *toText(const WCHAR *data,int length = -1)
//===================================================================
{

	if (length < 0){
		int i = 0;
		for (i = 0; data[i] != 0; i++);
		length = i;
	}
	TCHAR *ret = new TCHAR[length+1];
	if (sizeof(WCHAR) == sizeof(TCHAR))
		for (int j = 0; j<length; j++) ret[j] = (TCHAR)data[j];
	else
		for (int j = 0; j<length; j++) ret[j] = (TCHAR)(data[j] & 0xff);
	ret[length] = 0;
	return ret;
}

//===================================================================
WCHAR *toUnicode(const WCHAR *data,int length = -1)
//===================================================================
{
	if (length < 0){
		int i = 0;
		for (i = 0; data[i] != 0; i++);
		length = i;
	}
	WCHAR *ret = new WCHAR[length+1];
	for (int j = 0; j<length; j++) ret[j] = data[j];
	ret[length] = 0;
	return ret;
}
//===================================================================
WCHAR *utf8ToUnicode(const char *data,int length = -1)
//===================================================================
{
	if (length < 0) length = strlen(data);
	int need = sizeofJavaUtf8String((const unsigned char *)data,length);
	WCHAR *ret = new WCHAR[need+1];
	javaUtf8ToStringData((const unsigned char *)data,length,ret);

	ret[need] = 0;
	return ret;
}
//===================================================================
WCHAR *asciiToUnicode(const char *data,int length = -1)
//===================================================================
{
	if (length < 0) length = strlen(data);
	WCHAR *ret = new WCHAR[length+1];
	for (int j = 0; j<length; j++) ret[j] = (WCHAR)data[j] & 0xff;
	ret[length] = 0;
	return ret;
}
//===================================================================
WCHAR *toUnicode(const char *data,int length = -1)
//===================================================================
{
	return utf8ToUnicode(data,length);
}
//===================================================================
static void toCharArray(WCHAR *dest,const char *chars)
//===================================================================
{
	for (int i = 0; chars[i] != 0; i++)
			dest[i] = ((WCHAR)chars[i]) & 0xff;
}
//===================================================================
static void toCharArray(WCHAR *dest,const WCHAR *chars,int length = -1)
//===================================================================
{
	for (int i = 0; chars[i] != 0 && (length < 0 || i<length); i++)
			dest[i] = (WCHAR)chars[i];
}

//===================================================================
int uniLength(const WCHAR *text)
//===================================================================
{
	if (text == NULL) return 0;
	int i = 0;
	for (i = 0; text[i] != 0; i++)
		;
	return i;

}
//===================================================================

static int lengthOf(const char *chars){return strlen(chars);}
//===================================================================
static int lengthOf(const WCHAR *chars){return uniLength(chars);}
//===================================================================

//===================================================================
typedef class linked_element {
//===================================================================
public:
	class linked_element *next, *prev;
	linked_element(){next = prev = NULL;}
//===================================================================
}*LinkedElement;
//===================================================================

//===================================================================

typedef class byte_buffer{
//===================================================================
	public:
		unsigned char *data;
		int dataLength; //This is in the data unit (e.g. WCHAR or TCHAR or byte).
		int bufferLength; //This is always in bytes.

		byte_buffer(int initialSize = 0)
		{
			data = new unsigned char[initialSize];
			bufferLength = dataLength = initialSize;
		}
		unsigned char *need(int length)
		{
			if (length > bufferLength){
				delete data;
				data = new unsigned char[length];
				bufferLength = length;
			}
			return data;
		}
		~byte_buffer()
		{
			delete data;
		}
	int wasNeeded;
	private:
		int isText;
		int unitSize;

		DWORD *getSize(int text,int size)
		{
			unitSize = size;
			isText = text;
			wasNeeded = bufferLength/unitSize;
			return (DWORD *)&wasNeeded;
		}
	public:
		DWORD *getTextCharSize(int charSize) {return getSize(1,charSize);}
		DWORD *getByteSize() {return getSize(0,1);}
		DWORD *getEmptySize() {getByteSize(); wasNeeded = 0; return (DWORD *)&wasNeeded;}
		int checkSize()
		{
			dataLength = wasNeeded;
			if (isText){
				if ((wasNeeded+1)*unitSize <= bufferLength) return 0;
				need((wasNeeded+1)*unitSize);
				return 1;
			}else{
				if (wasNeeded*unitSize <= bufferLength) return 0;
				need(wasNeeded*unitSize);
				return 1;
			}
		}
//===================================================================
}*ByteBuffer;
//===================================================================

#ifndef WIN32
#define LCID int
#define LOCALE_SYSTEM_DEFAULT 0
#define NORM_IGNORECASE 1
#define SORT_STRINGSORT 2
#define CSTR_EQUAL 0
#define CSTR_LESS_THAN -1
#define CSTR_MORE_THAN 1
//===================================================================
static int CompareString(LCID id,int options,TCHAR *strOne,int lenOne,TCHAR *strTwo,int lenTwo)
//===================================================================
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

//===================================================================
typedef class java_string : public linked_element{
//===================================================================
	public:
		WCHAR *data;
		int length;
		java_string(){data = NULL; length = 0;}
		~java_string(){if (data) delete data;}
		WCHAR *make(int len)
		{
			length = len;
			return data = new WCHAR[len];
		}
		int checkChars(const WCHAR *chars,int &len)
		{
			if (!chars) return FALSE;
			if (len == -1) len = stringLength(chars);
			return TRUE;
		}
		int checkChars(const char *chars,int &len)
		{
			if (!chars) return FALSE;
			if (len == -1) len = strlen(chars);
			return TRUE;
		}
		void expand(int byHowMuch) {
			WCHAR *nd = new WCHAR[length+byHowMuch];
			if (data != NULL){
				memcpy(nd,data,sizeof(WCHAR)*length);
				delete data;
			}
			data = nd;
			length += byHowMuch;
		}

		java_string &cat(const WCHAR *chars,int len = -1){
			if (checkChars(chars,len)){
				expand(len);
				memcpy(data+length-len,chars,sizeof(WCHAR)*len);
			}
			return *this;
		}
		java_string &catAndDelete(WCHAR *chars,int len = -1){
			cat(chars,len);
			if (chars) delete chars;
			return *this;
		}

		java_string &cat(const char *chars,int len = -1){
			WCHAR *uni = ::toUnicode(chars,len);
			return catAndDelete(uni,-1);
		}

		java_string &catAndDelete(char *chars,int len = -1){
			cat(chars,len);
			if (chars) delete chars;
			return *this;
		}

		java_string(const char *text,int len = -1){data = NULL; length = 0; cat(text,len);}
		java_string(const WCHAR *text,int len = -1){data = NULL; length = 0; cat(text,len);}

		java_string & operator << (const char *text)  {return cat(text);}
		java_string & operator << (const WCHAR *text) {return cat(text);}
		java_string & operator << (java_string &js)   {return catAndDelete(js.toUnicode());}
/*
		java_string *makeNoUtf8(const char *chars,int len = -1)
		{
			if (!checkChars(chars,len)){
				make(0);
				return this;
			}
			make(lengthOf(chars));

			toCharArray(data,chars);
			return this;
		}
*/
		java_string *make(const WCHAR *chars,int length = -1)
		{
			make(0);
			cat(chars,length);
			return this;
		}
		java_string *make(const char *chars,int length = -1)
		{
			make(0);
			cat(chars,length);
			return this;
		}
		char *toText()
		{

			return ::toTextUtf8(data,length);
		}
		TCHAR *toTextNoUtf8()
		{
			return ::toText(data,length);
		}
		//Convert to the native text (TCHAR) and limit it to maxLength (if maxLength >= 0)

		TCHAR *toNativeText(int maxLength = -1)
		{
			TCHAR *ret = toTextNoUtf8();
			if (maxLength < length && maxLength >= 0)
				ret[maxLength] = (TCHAR)0;
			return ret;
		}
		WCHAR *toUnicode()
		{
			return ::toUnicode(data,length);
		}
		TCHAR *toText(byte_buffer *arr)
		{
			int i;
			if (sizeof(TCHAR) == sizeof(WCHAR)) return (TCHAR *)data;
			arr->need(length);
			for (i = 0; i<length; i++) arr->data[i] = (char)data[i];
			return (TCHAR *)arr->data;
		}
		static byte_buffer strOne, strTwo;
		static int compare(java_string * one,java_string * two)
		{
			if (one == two) return 0;
			if (one == NULL) return -1;
			else if (two == NULL) return 1;
			int options = SORT_STRINGSORT;
			if (one->length != two->length) options |= NORM_IGNORECASE;
			int got = CompareString(LOCALE_SYSTEM_DEFAULT,options,one->toText(&strOne),one->length,two->toText(&strTwo),two->length);
			if (got == CSTR_EQUAL) return 0;
			else if (got == CSTR_LESS_THAN) return -1;
			else return 1;
		}
//===================================================================
}*JavaString;
//===================================================================
byte_buffer java_string::strOne,java_string::strTwo;
//===================================================================
// This is used for sorting. It assumes that info->functionData is an
// array of JavaStrings.
//===================================================================
static int compareJavaString(SortInfo info,int one,int two,int *error)
//===================================================================
{
	JavaString *keys = (JavaString *)info->functionData;
	JavaString jo = keys[one];
	JavaString jt = keys[two];
	return java_string::compare(jo,jt);
}

#ifndef NO_OBJECTS

//########################################################################
//
// This is the base class for object access. This will be used to create
// two implementations, one for Ewe and one for Java JNI.
//
//########################################################################

//########################################################################
typedef class object_access {
//########################################################################
private:
	ObjectRef vmSyncObject;
protected:
	void clear()
	{
		me = vmSyncObject = 0;

		myClass = 0;
		env = NULL;
		myVm = 0;
	}
	object_access()
	{
		clear();
	}
public:
	ObjectRef me;
	void *env;
	ClassRef myClass;
	VMRef myVm;

	virtual ClassRef getClassRef(ObjectRef objectRef) = 0;
	virtual ClassRef getClassRef(char *className) = 0;
	virtual ClassRef getSuperclass(ClassRef clazz) = 0;
	object_access(void *theEnv,ObjectRef myObject = 0)
	{
		clear();
		env = theEnv;
		me = myObject;
	}
	object_access(void *theEnv,char *className) {clear(); env = theEnv; /*myClass = getClassRef(className);*/}
	object_access(VMRef vm){clear(); myVm = vm;}

	object_access & setClass(ClassRef clazz) {myClass = clazz; return *this;}
	object_access & setClass(char *className) {myClass = getClassRef(className); return *this;}
	object_access & setObject(ObjectRef obj) {me = obj; myClass = (!me ? 0 : getClassRef(me)); return *this;}
	virtual object_access & setVM(VMRef vm) {clear(); myVm = vm; return *this;}
	virtual object_access *getNew(ObjectRef forWho = 0) = 0;

	object_access *getForNewThread(int keepObjectRef = 1)
	{
		object_access *ret = getNew();
		ret->myVm = getVM();
		ret->env = env;
		if (keepObjectRef){
			if (me) ret->setObject(ret->holdRef(me));
			else if (myClass) ret->setClass(myClass);
		}
		return ret;
	}
	void setNewThread()
	{
		ObjectRef oldMe = me;
		ClassRef oldClass = myClass;
		setVM(myVm);
		me = oldMe;
		myClass = oldClass;
		if (me != 0) myClass = getClassRef(me);
	}
	virtual ObjectRef holdRef(ObjectRef obj) = 0;
	virtual void releaseRef(ObjectRef obj) = 0;

	//virtual void freeObject(ObjectRef ref) = 0;

	virtual FieldRef getFieldRef(ClassRef classRef,char *fieldName,char *fieldType) = 0;
	virtual FieldRef getStaticFieldRef(ClassRef classRef,char *fieldName,char *fieldType) = 0;
	virtual MethodRef getMethodRef(ClassRef classRef,char *methodName,char *methodSignature,ClassRef *vclassRef = NULL) = 0;
	virtual MethodRef getStaticMethodRef(ClassRef classRef,char *methodName,char *methodSignature,ClassRef *vclassRef = NULL) = 0;
	virtual int isAssignableFrom(ClassRef clazz,ClassRef targetClazz) = 0;
	virtual ObjectRef getCurrentException() = 0;
	virtual void clearCurrentException() = 0;
	virtual ObjectRef throwException(ObjectRef exception) = 0;
	virtual ObjectRef allocObject(ClassRef clazz) = 0;
	virtual int isSameObject(ObjectRef one,ObjectRef two) = 0;
	virtual int monitorEnter(ObjectRef monitor) = 0;
	virtual int monitorExit(ObjectRef monitor) = 0;
	ObjectRef allocNew() {if (!myClass) myClass = getClassRef(me); return allocObject(myClass);}

	FieldRef getAFieldRef(char *fieldName,char *fieldType)
	{
		if (!myClass) myClass = getClassRef(me);
		return getFieldRef(myClass,fieldName,fieldType);
	}
	FieldRef getAStaticFieldRef(char *fieldName,char *fieldType)
	{
		if (!myClass) myClass = getClassRef(me);
		return getStaticFieldRef(myClass,fieldName,fieldType);
	}
	MethodRef getAMethodRef(char *methodName,char *methodSignature,ClassRef *vclassRef = NULL)
	{
		if (!myClass) myClass = getClassRef(me);
		return getMethodRef(myClass,methodName,methodSignature,vclassRef);
	}
	MethodRef getAStaticMethodRef(char *methodName,char *methodSignature)
	{
		if (!myClass) myClass = getClassRef(me);
		return getStaticMethodRef(myClass,methodName,methodSignature);
	}
	int amOfType(ClassRef targetClass)
	{
		if (!myClass) myClass = getClassRef(me);
		return isAssignableFrom(myClass,targetClass);
	}
	int amOfType(char *targetClass)
	{
		return amOfType(getClassRef(targetClass));
	}
	int amSameAs(ObjectRef other)

	{
		return isSameObject(me,other);
	}

	virtual double getJdouble(JValue jv) = 0;
	virtual void setJdouble(JValue *jv,double value) = 0;

#undef DefineGet
#define DefineGet(TYPE,NAME,METHOD) virtual TYPE NAME(FieldRef fieldRef) = 0;

	DefineGet(int,getBooleanField,GetBooleanField)
	DefineGet(ObjectRef,getObjectField,GetObjectField)
	DefineGet(char,getByteField,GetByteField)
	DefineGet(WCHAR,getCharField,GetCharField)
	DefineGet(int16,getShortField,GetShortField)
	DefineGet(int,getIntField,GetIntField)
	DefineGet(_int64,getLongField,GetLongField)
	DefineGet(double,getDoubleField,GetDoubleField)
	DefineGet(float,getFloatField,GetFloatField)

#undef DefineSet
#define DefineSet(TYPE,NATIVE,NAME,METHOD) virtual void NAME(FieldRef fieldRef,TYPE value) = 0;
	DefineSet(int,jboolean,setBooleanField,SetBooleanField)
	DefineSet(ObjectRef,jobject,setObjectField,SetObjectField)
	DefineSet(char,jbyte,setByteField,SetByteField)
	DefineSet(WCHAR,jchar,setCharField,SetCharField)
	DefineSet(int16,jshort,setShortField,SetShortField)
	DefineSet(int,jint,setIntField,SetIntField)
	DefineSet(_int64,jlong,setLongField,SetLongField)
	DefineSet(double,jdouble,setDoubleField,SetDoubleField)

	DefineSet(float,jfloat,setFloatField,SetFloatField)

#undef DefineGet
#define DefineGet(TYPE,NAME) virtual TYPE getStatic##NAME##Field(FieldRef fieldRef) = 0;
	DefineGet(int,Boolean)
	DefineGet(ObjectRef,Object)
	DefineGet(char,Byte)
	DefineGet(WCHAR,Char)
	DefineGet(int16,Short)
	DefineGet(int,Int)
	DefineGet(float,Float)
	DefineGet(_int64,Long)
	DefineGet(double,Double)

#undef DefineSet
#define DefineSet(TYPE,NAME) virtual void setStatic##NAME##Field(FieldRef fieldRef,TYPE value) = 0;
	DefineSet(int,Boolean)
	DefineSet(ObjectRef,Object)
	DefineSet(char,Byte)
	DefineSet(WCHAR,Char)
	DefineSet(int16,Short)
	DefineSet(int,Int)
	DefineSet(float,Float)
	DefineSet(_int64,Long)
	DefineSet(double,Double)

#undef GetArrayRgn
#define GetArrayRgn(TYPE,NATIVE,NATIVEARRAY,NAME,METHOD) virtual void NAME(ObjectRef array,int start,int length,TYPE *dest) = 0;

	GetArrayRgn(char,jbyte,jbyteArray,getByteArrayRegion,GetByteArrayRegion)
	GetArrayRgn(WCHAR,jchar,jcharArray,getCharArrayRegion,GetCharArrayRegion)
	GetArrayRgn(int16,jshort,jshortArray,getShortArrayRegion,GetShortArrayRegion)
	GetArrayRgn(int,jint,jintArray,getIntArrayRegion,GetIntArrayRegion)
	GetArrayRgn(_int64,jlong,jlongArray,getLongArrayRegion,GetLongArrayRegion)
	GetArrayRgn(float,jfloat,jfloatArray,getFloatArrayRegion,GetFloatArrayRegion)
	GetArrayRgn(double,jdouble,jdoubleArray,getDoubleArrayRegion,GetDoubleArrayRegion)
	GetArrayRgn(int,jboolean,jbooleanArray,getBooleanArrayRegion,GetBooleanArrayRegion)
#undef SetArrayRgn
#define SetArrayRgn(TYPE,NATIVE,NATIVEARRAY,NAME,METHOD) virtual void NAME(ObjectRef array,int start,int length,const TYPE *src) = 0;

	SetArrayRgn(char,jbyte,jbyteArray,setByteArrayRegion,SetByteArrayRegion)
	SetArrayRgn(WCHAR,jchar,jcharArray,setCharArrayRegion,SetCharArrayRegion)
	SetArrayRgn(int16,jshort,jshortArray,setShortArrayRegion,SetShortArrayRegion)
	SetArrayRgn(int,jint,jintArray,setIntArrayRegion,SetIntArrayRegion)
	SetArrayRgn(_int64,jlong,jlongArray,setLongArrayRegion,SetLongArrayRegion)
	SetArrayRgn(float,jfloat,jfloatArray,setFloatArrayRegion,SetFloatArrayRegion)
	SetArrayRgn(double,jdouble,jdoubleArray,setDoubleArrayRegion,SetDoubleArrayRegion)
	SetArrayRgn(int,jboolean,jbooleanArray,setBooleanArrayRegion,SetBooleanArrayRegion)

	virtual ObjectRef getObjectArrayElement(ObjectRef array,int index) = 0;
	virtual void setObjectArrayElement(ObjectRef array,int index,ObjectRef value) = 0;

	virtual JavaString objectToJavaString(ObjectRef jString) = 0;
	JavaString getStringField(FieldRef fieldRef)
	{
		return objectToJavaString(getObjectField(fieldRef));
	}
	virtual int arrayLength(ObjectRef array) = 0;

	virtual ObjectRef newString(JavaString string,int deleteIt = 0) = 0;
	virtual ObjectRef newArray(char *classTypeName,int length) = 0;

	//After an object has been freed it should not be used!
	//===================================================================
	virtual void freeObject(ObjectRef obj) = 0;
	//===================================================================
	virtual int64 getWindowHandle(ObjectRef obj) = 0;
	virtual ObjectRef suspendResume(ObjectRef obj = 0) = 0;
	virtual VMRef getVM() = 0;
	virtual void callBack(ObjectRef obj,ObjectRef data) = 0;
	virtual void threadEnding() = 0;
//
// Instance Methods
//
#undef DefineCall
#define DefineCall(NAME,TYPE,METHOD) virtual TYPE call##NAME##Method(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0) = 0; TYPE callMy##NAME##Method(MethodRef method,JValue *pars) {return call##NAME##Method(me,method,pars);}

	DefineCall(Int,int,i)
	DefineCall(Double,double,d)
	DefineCall(Byte,char,b)
	DefineCall(Char,WCHAR,c)
	DefineCall(Short,int16,s)
	DefineCall(Long,_int64,j)
	DefineCall(Float,float,f)
	DefineCall(Boolean,int,z)
	DefineCall(Object,ObjectRef,l)

	virtual void callVoidMethod(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0) = 0;
	void callMyVoidMethod(MethodRef method,JValue *pars,int callAsynchronously = 0) {callVoidMethod(me,method,pars,callAsynchronously);}
//
// Static Methods
//
#undef DefineCall
#define DefineCall(NAME,TYPE,METHOD) virtual TYPE callStatic##NAME##Method(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0) = 0; TYPE callMyStatic##NAME##Method(MethodRef method,JValue *pars){return callStatic##NAME##Method(myClass,method,pars);}

	DefineCall(Int,int,i)

	DefineCall(Double,double,d)
	DefineCall(Byte,char,b)
	DefineCall(Char,WCHAR,c)
	DefineCall(Short,int16,s)
	DefineCall(Long,_int64,j)
	DefineCall(Float,float,f)
	DefineCall(Boolean,int,z)
	DefineCall(Object,ObjectRef,l)

	virtual void callStaticVoidMethod(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0) = 0;
	void callMyStaticVoidMethod(MethodRef method,JValue *pars,int callAsynchronously = 0) {callStaticVoidMethod(myClass,method,pars,callAsynchronously);}


	ObjectRef createNewObject(ClassRef clazz,char * constructorSpecs,JValue *parameters)
	{
		MethodRef init = getMethodRef(clazz,"<init>",constructorSpecs,NULL);
		if (init == 0) return 0;
		ObjectRef got = allocObject(clazz);
		if (got == 0) return 0;
		callVoidMethod(got,init,parameters);
		return got;
	}
	ObjectRef createANewObject(char * constructorSpecs,JValue *parameters)
	{
		if (!myClass) myClass = getClassRef(me);
		if (myClass) return createNewObject(myClass,constructorSpecs,parameters);
		return 0;
	}
	ObjectRef createNewException(char *exceptionName,char *message = NULL)
	{
		ClassRef cr = getClassRef(exceptionName);

		if (!cr) return 0;
		JValue jv[1];
		if (message) {
			ObjectRef msg = newString((new java_string())->make(message),1);
			jv[0].l = msg;
			ObjectRef ex = createNewObject(cr,"(Ljava/lang/String;)V",jv);
			return ex;
		}else{
			return createNewObject(getClassRef(exceptionName),"()V",jv);
		}
	}
	ObjectRef throwNewException(char *exceptionName,char *message = NULL)
	{
		ObjectRef toThrow = createNewException(exceptionName,message);
		if (toThrow) return throwException(toThrow);
		else return 0;
	}

	ObjectRef getEweSyncObject()
	{
		if (vmSyncObject == 0){
			ClassRef cr = getClassRef("ewe/sys/Vm");
			if (!cr) return 0;
			MethodRef mr = getStaticMethodRef(cr,"getSyncObject","()Ljava/lang/Object;");
			if (!mr) return 0;
			JValue p[1];
			vmSyncObject = callStaticObjectMethod(cr,mr,p,1);
		}
		return vmSyncObject;
	}
	int eweMonitorEnter() {return monitorEnter(getEweSyncObject());}
	int eweMonitorExit() {return monitorExit(getEweSyncObject());}

	void setNativeData(void *data)
	{
		setIntField(getAFieldRef("nativeData","I"),(int)data);
	}
	void *getNativeData()
	{
		return (void *)getIntField(getAFieldRef("nativeData","I"));
	}

	FieldRef checkFieldRef(char *fieldName,char *fieldType,FieldRef *ref = NULL)
	{
		FieldRef r = 0;
		if (ref != NULL)
			if (*ref != 0) r = *ref;
		if (r == 0) r = getAFieldRef(fieldName,fieldType);
		if (ref != NULL) *ref = r;
		return r;
	}
	ObjectRef getStringField(char *fieldName,FieldRef *ref = NULL)
	{
		FieldRef r = checkFieldRef(fieldName,"Ljava/lang/String;",ref);
		if (r == 0) return 0;
		return getObjectField(r);
	}
	void setStringField(char *fieldName,ObjectRef str,FieldRef *ref = NULL)
	{
		FieldRef r = checkFieldRef(fieldName,"Ljava/lang/String;",ref);
		if (r != 0) setObjectField(r,str);
	}
	JavaString getAString(char *fieldName,FieldRef *ref = NULL)
	{
		return objectToJavaString(getStringField(fieldName,ref));
	}
	void stringFieldToText(TCHAR *dest,int maxChars,char *fieldName,FieldRef *ref = NULL)
	{
		if (!dest) return;
		*dest = 0;
		JavaString s = getAString(fieldName,ref);
		TCHAR *got = s->toNativeText();
		txtcpy(dest,got,maxChars);
		delete got;
		delete s;
	}
	JavaString setAString(JavaString js,char *fieldName,FieldRef *ref = NULL)
	{
		setStringField(fieldName,newString(js,0),ref);
		return js;
	}

//########################################################################
}*ObjectAccess;
//########################################################################

//########################################################################
typedef class an_object {
//########################################################################
protected:
object_access *object;
int held;
void setup(object_access &obj,ObjectRef theObject,int forNewThread = 0)
{
	held = forNewThread;
	if (forNewThread) {
		object = obj.getForNewThread(0);
		object->setObject(obj.holdRef(theObject));
	}else
		object = obj.getNew(theObject);
}
public:
an_object(object_access &obj,char *className,int forNewThread = 0){

	JValue p[1];
	ObjectRef ob = obj.createNewObject(obj.getClassRef(className),"()V",p);
	setup(obj,ob,forNewThread);
}

an_object(object_access &obj,ObjectRef theObject,int forNewThread = 0)
{
	setup(obj,theObject,forNewThread);
}

virtual ~an_object()
{
	if (held && object) object->releaseRef(object->me);
	if (object) delete object;
}
an_object &setClass(ClassRef cl) {object->setClass(cl); return *this;}
an_object &setClass(char *cl) {object->setClass(cl); return *this;}
MethodRef checkMethod(char *name,char *sig,MethodRef &value)
{
	if (!value)
		value = object->getAMethodRef(name,sig);
	return value;
}
FieldRef checkField(char *name,char *type,FieldRef &value)
{
	if (!value)
		value = object->getAFieldRef(name,type);
	return value;
}
void setNewThread(){
	object->setNewThread();
}
ObjectRef getObject() {return object->me;}
//########################################################################
}*AnObject;
//########################################################################


//########################################################################
typedef class ewe_handle : public an_object{
//########################################################################
protected:
static MethodRef setRef, changedRef, setFlagsRef, checkRef;
static FieldRef progressRef, stopRef;
//
// Flags
//
#define	Changed 0x80000000
#define	Stopped 0x40000000
#define	Success 0x20000000

#define	Failure 0x10000000
#define	Running 0x08000000
#define	Aborted 0x04000000
#define	Succeeded  (Success|Stopped)
#define	Failed (Failure|Stopped)

public:
	ewe_handle(object_access &obj,ObjectRef theHandle,int forNewThread = 0) : an_object(obj,theHandle,forNewThread)
	{}
	void changed(){
		JValue p[1];
		object->callVoidMethod(object->me,checkMethod("changed","()V",changedRef),p);
	}
	void setProgress(float value,int notifyChanged = 0){
		object->setFloatField(checkField("progress","F",progressRef),value);
		if (notifyChanged) changed();
	}
	int shouldStop(){
		return object->getBooleanField(checkField("shouldStop","Z",stopRef));
	}
	void set(int newState){
		JValue p[1];
		p[0].i = newState;
		object->callVoidMethod(object->me,checkMethod("set","(I)V",setRef),p);
	}
	int check(){
		JValue p[1];
		return object->callIntMethod(object->me,checkMethod("check","()I",checkRef),p);
	}
	void setFlags(int toSet,int toClear){
		JValue p[2];
		p[0].i = toSet; p[1].i = toClear;
		object->callVoidMethod(object->me,checkMethod("setFlags","(II)V",setFlagsRef),p);
	}
	void setErrorCode(int code){
		object->setIntField(object->getAFieldRef("errorCode","I"),code);
	}
	void setError(ObjectRef errorString){
		object->setObjectField(object->getAFieldRef("error","Ljava/lang/String;"),errorString);
	}
	void setErrorObject(ObjectRef error){
		object->setObjectField(object->getAFieldRef("errorObject","Ljava/lang/Object;"),error);
	}
	void setReturnValue(ObjectRef value){

		object->setObjectField(object->getAFieldRef("returnValue","Ljava/lang/Object;"),value);
	}
//########################################################################
}*EweHandle;
//########################################################################

MethodRef
	ewe_handle::changedRef = 0,
	ewe_handle::setFlagsRef = 0,
	ewe_handle::checkRef = 0,
	ewe_handle::setRef = 0;

FieldRef
	ewe_handle::stopRef,
	ewe_handle::progressRef = 0;

#ifndef NO_ENI_THREADS

//########################################################################
//
// This is the mThread unifying thread class, providing the same API on
// Windows and Unix (via POSIX threads).
//
// The API provides two classes: an mLock for synchronization and the mThread itself.
// The mLock is recursive (the same thread can hold the lock multiple times) and
// you can wait() on the lock and call notify() on the lock - both while holding the lock.
//
// The mThread requires you to override void run(void *data) to do your functions.
//
// Because of the problems with deleting objects in different threads you should never
// explicitly delete an mThread. Use ref() to add a reference to the thread and unref()
// to unreference it. When first created it has a reference count of 1. Should unref()
// ever count down to zero then the thread may be deleted at any point and therefore
// should not be referred to again - even for a call to join(). Therefore if you wish
// to join() the thread you must keep at least one reference to it. However remember
// that unless a thread is completely dereferenced it will continue to take up resources
// and may limit the creation of other threads.
//
// The POSIX thread only allows for 255 active threads at any time.
//
//########################################################################

#include <stdio.h>

//##############################################################
class mClass
//##############################################################
{
public:
	mClass(){}
	virtual ~mClass(){}
//##############################################################
};
typedef mClass *MClass;
//##############################################################

//typedef void *MClass;

//##############################################################
class mList : public mClass
//##############################################################
{
private:
	MClass *elements;
	int numElements;
	int sizeElements;

public:
	~mList() {if (elements != NULL) delete elements;}

	mList()
	{
		elements = NULL;
		sizeElements = 0;
		numElements = 0;
	}

	int indexOf(MClass who)
	{
		if (who == NULL || numElements == 0) return -1;
		for (int i = 0; i<numElements; i++)
			if (elements[i] == who) return i;
		return -1;
	}
	int contains(MClass who)
	{
		return indexOf(who) != -1;
	}
	int size()
	{
		return numElements;
	}
	MClass get(int index)
	{
		if (index < 0 || index >= numElements) return NULL;
		return elements[index];
	}
	void remove(int index)
	{
		if (index < 0 || index >= numElements) return;
		for (int i = 0; i<numElements-index-1; i++)
			elements[index+i] = elements[index+i+1];
		numElements--;
	}
	void remove(MClass who)
	{
		remove(indexOf(who));
	}
	MClass pop()
	{
		if (numElements == 0) return NULL;
		MClass ret = elements[0];
		remove(0);
		return ret;
	}
	void add(MClass what)
	{
		if (contains(what)) return;
		if (numElements+1 > sizeElements){
			MClass *np = new MClass[sizeElements*2+1];
			sizeElements = sizeElements*2+1;
			if (elements != NULL){
				for (int i = 0; i<numElements; i++)
					np[i] = elements[i];
				delete elements;
			}
			elements = np;
		}
		elements[numElements] = what;
		numElements++;
	}
//##############################################################
};
typedef mList *MList;
//##############################################################

typedef class mThread *MThread;

#define VERY_LONG 0x7fffffff
#define ABSOLUTE_INDEFINITE -1
#define FOREVER ABSOLUTE_INDEFINITE

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
void *getCurrentThreadHandle()
{
	return (void *)GetCurrentThreadId();
}
void discardCurrentThreadHandle(void *id)
{
}

CRITICAL_SECTION allLock;

void lockall()
{
	static int initialized = 0;
	if (!initialized) {
		InitializeCriticalSection(&allLock);
		initialized = 1;
	}
	EnterCriticalSection(&allLock);
}
void unlockall()
{
	LeaveCriticalSection(&allLock);
}
#else
#include <pthread.h>
#include <sys/time.h>
#include <errno.h>

static pthread_mutex_t global = PTHREAD_MUTEX_INITIALIZER;

void lockall()
{
	pthread_mutex_lock(&global);
}
void unlockall()
{
	pthread_mutex_unlock(&global);
}
int waitForSignal(int howLong,pthread_mutex_t *waiter,pthread_cond_t *signaller,int doLock = 1)
{
	if (doLock) pthread_mutex_lock(waiter);
	int signalled = 1;
	if (howLong == ABSOLUTE_INDEFINITE){
		pthread_cond_wait(signaller,waiter);
	}else{
		struct timeval now;
		struct timespec tv;
		int milliseconds = howLong;
		int nanoseconds = 0;
		gettimeofday(&now,NULL);
		tv.tv_sec = now.tv_sec+(milliseconds/1000);
		int micros = now.tv_usec+(milliseconds%1000)*1000;
		micros += nanoseconds/1000;
		tv.tv_sec += micros/1000000;
		tv.tv_nsec = (micros%1000000)*1000;
		signalled = pthread_cond_timedwait(signaller,waiter,&tv) == 0;
	}
	if (doLock) pthread_mutex_unlock(waiter);
	return signalled;
}
static pthread_t current;
void *getCurrentThreadHandle()
{
	current = pthread_self();
	return &current;
}
void discardCurrentThreadHandle(void *id)
{
}

#endif

#define LOCKALL lockall();
#define UNLOCKALL unlockall();

static mList activeThreads;

//##############################################################
class mThread : public mClass {
//##############################################################
private:
friend class mLock;

int amDummy;
int refCount;
int hasEnded;
class mLock *waitingOn;
int interrupted;
//
// Should only be called within a LOCKALL.
//
//--------------------------------------------------
static MThread getCurrent();
//
// This should be called by an external thread that is ending.
//
//--------------------------------------------------
void dummyThreadEnding()
{
	LOCKALL
	hasEnded = 1;
	int deleteIt = (refCount == 0);
	UNLOCKALL
	if (deleteIt) delete this;
}

#ifdef WIN32
	class Event {
		HANDLE event;

		public:
		Event()
		{
			event = CreateEvent(NULL,0,0,NULL);
		}
		~Event()
		{
			CloseHandle(event);
		}
		void set()
		{
			SetEvent(event);
		}
		void reset()
		{
			ResetEvent(event);
		}
		int wait(int howLong)
		{
			return WaitForSingleObject(event,howLong == ABSOLUTE_INDEFINITE ? INFINITE : howLong)
				 == WAIT_OBJECT_0;
		}
	};
	Event waitEvent, joinEvent;
	HANDLE threadHandle;
	void *myID;
	int isMyID(void *id) {return myID == id;}
	void setMyID(void *id) {myID = id;}
	void nativeInit(){}
	void nativeDestroy(){}
	void detachAndDelete()
	{
		CloseHandle(threadHandle);
		delete this;
	}
	static unsigned long _stdcall threadStartPoint(void *data);
//--------------------------------------------------
	int createThread()
//--------------------------------------------------
	{
		DWORD id;
		threadHandle = CreateThread(NULL,0,threadStartPoint,(void *)this,0,&id);
		myID = (void *)id;
		return threadHandle != NULL;
	}
#else

	pthread_t thread;
	static pthread_mutex_t initMutex;
	static pthread_cond_t initCond;


	class Event {
		pthread_mutex_t mutex;
		pthread_cond_t cond;
		int hasWaiting;
		int isSignalled;

		public:
		Event()
		{
			hasWaiting = isSignalled = 0;
			mutex = initMutex;
			cond = initCond;
		}
		void set()
		{
			pthread_mutex_lock(&mutex);
			isSignalled = 1;
			if (hasWaiting) pthread_cond_signal(&cond);
			pthread_mutex_unlock(&mutex);
		}
		void reset()
		{
			pthread_mutex_lock(&mutex);
			isSignalled = 0;
			pthread_mutex_unlock(&mutex);
		}
		int wait(int howLong)
		{
			pthread_mutex_lock(&mutex);
			if (isSignalled) {
				pthread_mutex_unlock(&mutex);
				return 1;
			}
			hasWaiting = 1;
			::waitForSignal(howLong,&mutex,&cond,0);
			hasWaiting = 0;
			int ret = isSignalled;
			pthread_mutex_unlock(&mutex);
			return ret;
		}
	};

	Event waitEvent, joinEvent;
	int isMyID(void *id) {return pthread_equal(*((pthread_t *)id),thread);}
	void setMyID(void *id) {thread = *((pthread_t *)id);}
	void nativeInit(){}
	void nativeDestroy(){}
	static void *threadStartPoint(void *data);
	//--------------------------------------------------
	int createThread()
	//--------------------------------------------------
	{
		int ret = pthread_create(&thread,NULL,threadStartPoint,(void *)this) == 0;
		return ret;
	}
	//--------------------------------------------------
	void detachAndDelete()
	//--------------------------------------------------
	{
		pthread_detach(thread);
		delete this;
	}
#endif
	//
	//Must be called with LOCKALL
	//
	// Returns 1 = signalled, 0 = timedout, -1 = interrupted.
//--------------------------------------------------
	int wait(int howLong)
//--------------------------------------------------
	{
		interrupted = 0;
		waitEvent.reset();
		UNLOCKALL
		int ret = waitEvent.wait(howLong);
		LOCKALL
		return ret;
	}

	//
	//Must be called with LOCKALL. We should never call wakeup()
  //before a wait(), although wakeup() may execute before waitForEvent().
	//
//--------------------------------------------------
	void wakeup()
//--------------------------------------------------
	{
		if (interrupted) return;
		waitEvent.set();
	}

//--------------------------------------------------
void init(void *data)
//--------------------------------------------------
{
	amDummy = 0;
	refCount = 1;
	hasEnded = 0;
	theData = data;
	waitingOn = NULL;
	nativeInit();
}

//--------------------------------------------------
		void threadShutdown()
//--------------------------------------------------
		{
			LOCKALL
			joinEvent.set();
			activeThreads.remove(this);
			hasEnded = 1;
		  int doDelete = refCount == 0;
			UNLOCKALL
			if (doDelete)
				if (!amDummy)
					detachAndDelete();
				else
					delete this;
		}
//--------------------------------------------------
		void threadBeginning()
//--------------------------------------------------
		{
			LOCKALL
			activeThreads.add(this);
			UNLOCKALL
			threadStart();
			threadShutdown();
		}
protected:
	void *theData;
//--------------------------------------------------
		virtual void threadStart()
//--------------------------------------------------
		{
			run(theData);
		}
		//
		// Override to do custom starting.
		//
//--------------------------------------------------
		virtual void run(void *data){};
//--------------------------------------------------

public:
//=================================================
	mThread *ref()
//=================================================
	{
		LOCKALL
		refCount++;
		UNLOCKALL
		return this;
	}
//=================================================
	void unref()
//=================================================
	{
		int doDelete = 0;
		LOCKALL
		if (refCount == 1 && hasEnded) doDelete = 1;
		if (refCount > 0) refCount--;
		UNLOCKALL
		if (doDelete)
			if (amDummy)
				delete this;
			else
				detachAndDelete();
	}
//=================================================
	int startRunning(int doUnref = 0)
//=================================================
	{
		LOCKALL
		if (doUnref && refCount > 0) refCount--;
		int ret = createThread();
		UNLOCKALL
		return ret;
	}
#define TIMEDOUT 0
#define INTERRUPTED -1
#define NOTIFIED 1
/**
Interrupt a thread ONLY if it is waiting to be notified.
**/
//=================================================
	void interrupt()
//=================================================
	{
		LOCKALL
		if (interrupted || waitingOn == NULL) {
			UNLOCKALL
			return;
		}
		wakeup();
		interrupted = 1;
		UNLOCKALL
	}

//=================================================
int join(int howLong = FOREVER)
//=================================================
{
	LOCKALL
		if (hasEnded){
			UNLOCKALL
			return 1;
		}
	UNLOCKALL
	joinEvent.wait(howLong);
	LOCKALL
	if (hasEnded){
		joinEvent.set();
		UNLOCKALL
		return 1;
	}
	UNLOCKALL
	return 0;
}
//=================================================
static void nap(int milliseconds,int nanoseconds = 0);
//=================================================
	mThread(void *data = NULL) {init(data);}
//=================================================
	virtual ~mThread(){nativeDestroy();}
//=================================================
static MThread getCurrentThread();
static void currentThreadEnding();
//=================================================
private:
	mThread(int isDummy)
	{
		init(NULL);
		refCount = 0;
		amDummy = 1;
	}
//##############################################################

//##############################################################
};
typedef mThread *MThread;
//##############################################################

//
// Called by the Lock object only.
//
//--------------------------------------------------
MThread mThread::getCurrent()
//--------------------------------------------------
{
	void *id = getCurrentThreadHandle();
	MThread ret = NULL;
	for (int i = 0; i<activeThreads.size(); i++){
		MThread t = (MThread)activeThreads.get(i);
		if (t->isMyID(id)){
			ret = t;
			break;
		}
	}
	if (ret == NULL){
		ret = new mThread(1);
		ret->setMyID(id);
		activeThreads.add(ret);
	}
	discardCurrentThreadHandle(id);
	return ret;
}
//=================================================
void mThread::currentThreadEnding()
//=================================================
{
	LOCKALL
	void *id = getCurrentThreadHandle();
	MThread ret = NULL;
	for (int i = 0; i<activeThreads.size(); i++){
		MThread t = (MThread)activeThreads.get(i);
		if (t->isMyID(id)){
			if (t->amDummy) {
				ret = t;
				activeThreads.remove(t);
			}else {
				UNLOCKALL
				return;
			}
			break;
		}
	}
	UNLOCKALL
	if (ret != NULL) ret->dummyThreadEnding();
}
//=================================================
MThread mThread::getCurrentThread()
//=================================================
{
	LOCKALL
	MThread ret = getCurrent();
	UNLOCKALL
	return ret;
}

#ifdef WIN32
//--------------------------------------------------
unsigned long _stdcall mThread::threadStartPoint(void *data)
//--------------------------------------------------
{
	mThread *thread = (mThread *)data;
	thread->threadBeginning();
	return 0;
}
//=================================================
void mThread::nap(int milliseconds,int nanoseconds)
//=================================================
{
	Sleep(milliseconds);
}
#else
//--------------------------------------------------
void *mThread::threadStartPoint(void *data)
//--------------------------------------------------
{
	mThread *thread = (mThread *)data;
	thread->threadBeginning();
	return 0;
}
//--------------------------------------------------
static pthread_mutex_t napper = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t napperCond = PTHREAD_COND_INITIALIZER;
pthread_mutex_t mThread::initMutex  = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t mThread::initCond = PTHREAD_COND_INITIALIZER;

//=================================================
void mThread::nap(int milliseconds,int nanoseconds)
//=================================================
{
	::waitForSignal(milliseconds,&napper,&napperCond);
}
#endif

//##############################################################
class mLock : public mClass
//##############################################################
{
private:
	int entered;
	MThread owner;
	mList waitingToHold, waitingForNotify, waitingForReacquire;
	int refs;

	//-------------------------------------------------------------------
	void own(MThread  newOwner)
	//-------------------------------------------------------------------
	{
		owner = newOwner;
		entered++;
	}

//-------------------------------------------------------------------
int doHold(int t,int waitForIt)
//-------------------------------------------------------------------
{
	LOCKALL
	MThread m = mThread::getCurrent();
	if (m == NULL){
		UNLOCKALL
		return 0;
	}
	if (entered == 0 || m == owner) {
		own(m);
		UNLOCKALL
		return 1;
	}
	if (!waitForIt){
		UNLOCKALL
		return 0;
	}
	waitingToHold.add(m);
	m->wait(t);
	if (owner == m){
		UNLOCKALL
		return 1;
	}else{
		waitingToHold.remove(m);
		UNLOCKALL
		return 0;
	}
}
//-------------------------------------------------------------------
void wakeWaiting()
//-------------------------------------------------------------------
{
	MThread w = (MThread)waitingForReacquire.pop();
	if (w == NULL) w = (MThread)waitingToHold.pop();
	if (w != NULL) {
		own(w);
		w->wakeup();
	}
}

public:
//===================================================================
int lock()
//===================================================================
{
	return doHold(FOREVER,1);
}
//===================================================================
void unlock()
//===================================================================
{
	LOCKALL
	MThread m = mThread::getCurrent();
	if (m == owner && entered > 0){
		entered--;
		if (entered == 0){
			owner = NULL;
			wakeWaiting();
		}
	}else{
		//printf("Critical error!\n");
	}
	UNLOCKALL
}

//===================================================================
int wait(int howLong = FOREVER)
//===================================================================
{
	LOCKALL
	MThread c = mThread::getCurrent();
	if (owner != c || entered == 0) {
		UNLOCKALL
		return 0;
	}
//..................................................................
	int num = entered;
	entered = 0;
	owner = NULL;
	wakeWaiting();
//..................................................................
	waitingForNotify.add(c);
	c->waitingOn = this;
	c->wait(howLong);
	c->waitingOn = NULL;
	//boolean wasInterrupted = Coroutine.sleep(howLong.remaining()) == -1;
	int notified = !waitingForNotify.contains(c);
	int interrupted = c->interrupted;
	c->interrupted = 0;
	if (!notified) waitingForNotify.remove(c);
	if (entered != 0){
		waitingForReacquire.add(c);
		c->wait(ABSOLUTE_INDEFINITE);
	}
	owner = c;
	entered = num;
	UNLOCKALL
	//if (wasInterrupted) throw new InterruptedException();
	if (interrupted) return INTERRUPTED;
	else if (notified) return NOTIFIED;
	else return TIMEDOUT;
}
//===================================================================
void notify(int all = 0)
//===================================================================
{
	LOCKALL
	MThread c = mThread::getCurrent();
	if (owner != c || entered == 0) {
		UNLOCKALL
		return;
	}
	while((c = (MThread)waitingForNotify.pop()) != NULL){
		c->wakeup();
		c->waitingOn = NULL;
		if (!all) break;
	}
	UNLOCKALL
}

//===================================================================
void notifyAll() {notify(1);}
//===================================================================

//===================================================================
	mLock()
//===================================================================
	{
		owner = NULL;
		entered = 0;
		refs = 1;
	}

//===================================================================
mLock *ref()
//===================================================================
{
	LOCKALL
	refs++;
	UNLOCKALL
	return this;
}
//===================================================================
void unref()
//===================================================================
{
	LOCKALL
	if (refs > 0) refs--;
	if (refs == 0) delete this;
	UNLOCKALL
}

//##############################################################
};
typedef mLock *MLock;
//##############################################################


/**

  This is used to start a new thread running that will interact with
  the running VM. It provides the following functions:

  1. It will handle whatever is needed to start a new thread running,
  all you have to do is override the protected member void run(void *data);

  2. It will optionally take care of setting up an object_access object for
  the new thread once it starts running. This is important because an object_access
  object is only valid for the thread it is created in.

  3. It will optionally provide a Ewe Handle for other Ewe objects to monitor
  and control the progress of the Thread.

**/

//########################################################################
typedef class new_thread : public mThread{
//########################################################################
	private:
		struct thread_data {
			class new_thread *thread;
			void *data;
		};

#ifdef WIN32
		//static unsigned long _stdcall createThread(void *data);
#ifdef notyet
		static DWORD WINAPI createThread(LPVOID data);
#endif
#endif

	protected:
		//
		// This is where you do your stuff.
		//
		virtual void run(void *data) = 0;
		//
		// Set this true if you don't want to delete the object and Handle on end.
		//
		int dontDeleteObjects;
		//
		// Set this true if you don't want to wakeup the coroutine, on ending.
		//
		int dontWakeup;
		//
		object_access *object;
		//
		ewe_handle *handle;
		//
		ObjectRef callingCoroutine;
		//
		void *threadData;
		//
		void wakeupCallingThread()

		{
			if (!object || !callingCoroutine) return;
			object->suspendResume(callingCoroutine);
			dontWakeup = TRUE;
		}
		//
		// This should be called from within the new Thread of execution.
		// It will not return until run() returns.
		//
		virtual void threadStart()
		{
			if (object != NULL) object->setNewThread();
			if (handle != NULL) {
				handle->setNewThread();
				handle->setFlags(Running,Stopped);
			}
			run(theData);
			if (handle) handle->setFlags(Stopped,Running);
			if (!dontWakeup) wakeupCallingThread();
		}
		virtual ~new_thread()
		{
			if (!dontDeleteObjects){
				if (handle) delete handle;
				if (object) {
					if (callingCoroutine) object->releaseRef(callingCoroutine);
					object->threadEnding();
					delete object;
				}
			}
		}

		void init_newThread()
		{
			handle = NULL;
			object = NULL;
			callingCoroutine = 0;
			dontWakeup = FALSE;
			dontDeleteObjects = FALSE;
		}

	public:
		new_thread(void * data = NULL):mThread((void *)data){init_newThread();}
		new_thread *keepAccess(object_access &obj,int keepObject = 1)
		{
			callingCoroutine = obj.holdRef(obj.suspendResume());
			object = obj.getForNewThread(keepObject);
			return this;
		}
		int start(object_access &obj,int doUnref = 0)
		{
			keepAccess(obj);
			return startRunning(doUnref);
		}
		int startWithHandle(object_access &obj,ObjectRef theHandle,int doUnref = 0)
		{
			handle = new ewe_handle(obj,theHandle,1);
			dontWakeup = true;
			return start(obj,doUnref);
		}
		//
		// Note that, by default, this method does an unRef() on the new thread.
		// If you wish to hold a reference to the thread do ref() BEFORE you call this
		// method.
		//
		ObjectRef startAndMakeHandle(object_access &obj,int doUnref = 1)
		{
			JValue p[1];
			ObjectRef theHandle = obj.createNewObject(obj.getClassRef("ewe/sys/Handle"),"()V",p);
			if (!startWithHandle(obj,theHandle,doUnref)) return 0;
			return theHandle;
		}

//########################################################################
}*NewThread;
//########################################################################

#ifdef WIN32
//unsigned long _stdcall new_thread::createThread(void *data)
#ifdef notyet
DWORD WINAPI new_thread::createThread(LPVOID data)
{
	thread_data *td = (thread_data *)data;
	td->thread->threadStart(td->data);
	delete td;
	return 0;
}
#endif
#endif

#endif

#define SetupField(FIELDVAR,FIELDNAME,FIELDTYPE) FIELDVAR = getFieldRef(clazz,FIELDNAME,FIELDTYPE)

#ifndef NO_EWE_METHODS

//########################################################################
//
// This is an implementation of object_access for Ewe.
//
//########################################################################

typedef class held_object : public linked_element {
public:
	ObjectRef object;
	held_object(ObjectRef ref) {object = ref;}
}*HeldObject;


//########################################################################
typedef class ewe_object : public object_access{
//########################################################################

	HeldObject held;

	//===================================================================
	WObject newEweString(JavaString string,int deleteIt = 0)
	//===================================================================
	{
		if (!string) return 0;
		WObject ret = vm.createNewString(string->length,NULL,(uint16 *)string->data);
		if (deleteIt) delete string;
		return ret;
	}
	//===================================================================
	static WCHAR * stringChars(WObject str)
	//===================================================================
	{
		if (str == 0) return NULL;
		WObject ch = WOBJ_StringCharArrayObj(str);
		if (ch == 0) return NULL;
		return (WCHAR *)WOBJ_arrayStart(ch);
	}
	//===================================================================
	static int stringCharsLength(WObject str)
	//===================================================================
	{
		if (str == 0) return 0;
		WObject ch = WOBJ_StringCharArrayObj(str);
		if (ch == 0) return 0;
		return WOBJ_arrayLen(ch);
	}
	//===================================================================
	void doRelease(ObjectRef obj)
	//===================================================================
	{
		if (obj != 0) ((VMAccess *)env)->holdRelease((WObject)obj,0);
	}
	//===================================================================
	void doHold(ObjectRef obj)
	//===================================================================
	{
		if (obj != 0) ((VMAccess *)env)->holdRelease((WObject)obj,1);
	}
	//===================================================================
	ObjectRef holdObject(ObjectRef obj)
	//===================================================================
	{
		if (obj == 0) return obj;
		HeldObject ho = new held_object(obj);
		ho->next = held;
		held = ho;
		doHold(obj);
		return obj;
	}
	//===================================================================
	void freeAll()
	//===================================================================
	{
		for (HeldObject ptr = held; ptr != NULL;){
			HeldObject nx = (HeldObject)ptr->next;
			doRelease(ptr->object);
			delete ptr;
			ptr = nx;
		}
		held = NULL;
	}
protected:
	ewe_object(){}
public:
	ewe_object(void *Env,ObjectRef myRef = 0) : object_access(Env,myRef){if (myRef) myClass = getClassRef(myRef); held = NULL;}
	ewe_object(void *Env,char *className) : object_access(Env,className){myClass = getClassRef(className);held = NULL;}
	ewe_object(VMRef vmref) : object_access(vmref){
		env = &vm;
		held = NULL;
	}
	virtual ~ewe_object()
	{
		freeAll();
	}
	virtual object_access & setVM(VMRef vmref)
	{
		object_access::setVM(vmref);
		env = &vm;
		held = NULL;
		return *this;
	}

	virtual object_access *getNew(ObjectRef forWho = 0)
	{
		ewe_object *ret = new ewe_object();
		ret->env = env;
		if (forWho != 0) ret->setObject(forWho);
		return ret;
	}
	virtual ClassRef getClassRef(ObjectRef objectRef)
	{
		if (objectRef == 0) return 0;
		return (ClassRef)(((VMAccess *)env)->objectPtr((WObject)objectRef)[0].intValue & ~1);
	}
	virtual ClassRef getClassRef(char *className)
	{
		return (ClassRef)((VMAccess *)env)->getClass(className);
	}
	virtual ClassRef getSuperclass(ClassRef clazz)
	{
		return (ClassRef)((VMAccess *)env)->getSuperclass((WClass *)clazz);
	}
	virtual FieldRef getFieldRef(ClassRef classRef,char *fieldName,char *fieldType)
	{
		return (FieldRef)((VMAccess *)env)->getField((WClass *)classRef,fieldName,fieldType);
	}
	virtual FieldRef getStaticFieldRef(ClassRef classRef,char *fieldName,char *fieldType)
	{
		return (FieldRef)((VMAccess *)env)->getField((WClass *)classRef,fieldName,fieldType);
	}
	virtual MethodRef getMethodRef(ClassRef classRef,char *methodName,char *methodSignature,ClassRef *vclassRef = NULL)
	{
		return (ClassRef)((VMAccess *)env)->getMethod((WClass *)classRef,methodName,methodSignature,(WClass **)vclassRef);
	}
	virtual MethodRef getStaticMethodRef(ClassRef classRef,char *methodName,char *methodSignature,ClassRef *vclassRef = NULL)
	{
		return getMethodRef(classRef,methodName,methodSignature,vclassRef);
	}
	virtual int isAssignableFrom(ClassRef clazz,ClassRef targetClazz)
	{
		return ((VMAccess *)env)->isAssignableFrom((WClass *)clazz,(WClass *)targetClazz);
	}
	virtual ObjectRef allocObject(ClassRef classRef)
	{
		return (ObjectRef)((VMAccess *)env)->createNewObject((WClass *)classRef);
	}
	virtual ObjectRef getCurrentException()
	{
		return (ObjectRef)((VMAccess *)env)->getSetException(1,0);
	}
	virtual void clearCurrentException()
	{
		((VMAccess *)env)->getSetException(0,0);
	}
	virtual ObjectRef throwException(ObjectRef exception)
	{
		return (ObjectRef) ((VMAccess *)env)->getSetException(0,(WObject)exception);
	}
	virtual int isSameObject(ObjectRef one,ObjectRef two)
	{
		return one == two;
	}
	virtual int monitorEnter(ObjectRef monitor)
	{
		if (monitor == getEweSyncObject())
			((VMAccess *)env)->enterExitEweMonitor(1);
		return 0;
	}
	virtual int monitorExit(ObjectRef monitor)
	{
		if (monitor == getEweSyncObject())
			((VMAccess *)env)->enterExitEweMonitor(0);
		return 0;
	}
	//===================================================================
	virtual double getJdouble(JValue jv){return vm.getSetDoubleJValue(0,&jv,1);}
	//===================================================================
	virtual void setJdouble(JValue *jv,double value){vm.getSetDoubleJValue(value,jv,0);}
	//===================================================================


	//===================================================================

	virtual void freeObject(ObjectRef obj)
	//===================================================================
	{
		if (obj == 0) return;
		HeldObject ptr = held;
		HeldObject *last = &held;
		while(ptr != NULL){
			if (ptr->object == obj) {
				*last = (HeldObject)ptr->next;
				doRelease(obj);

				delete ptr;
				return;
			}
			last = (HeldObject *)&(ptr->next);
			ptr = (HeldObject)ptr->next;
		}
	}
	virtual ObjectRef holdRef(ObjectRef obj)
	{
		((VMAccess *)env)->holdRelease((WObject)obj,1);

		return obj;
	}
	virtual void releaseRef(ObjectRef obj)
	{
		if (obj == 0) return;
		((VMAccess *)env)->holdRelease((WObject)obj,0);
	}
//
// Instance Variable Access.
//

#undef DefineGet
#define DefineGet(TYPE,NAME,METHOD) virtual TYPE NAME(FieldRef fieldRef){ if (!me || !fieldRef) return 0; return (TYPE)((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,(WObject)me)->METHOD;}

	DefineGet(int,getBooleanField,intValue)
	//DefineGet(ObjectRef,getObjectField,obj)
	DefineGet(char,getByteField,intValue)
	DefineGet(WCHAR,getCharField,intValue)
	DefineGet(int16,getShortField,intValue)
	DefineGet(int,getIntField,intValue)
	//DefineGet(_int64,getLongField,GetLongField)
	//DefineGet(double,getDoubleField,GetDoubleField)
	DefineGet(float,getFloatField,floatValue)

	virtual _int64 getLongField(FieldRef fieldRef)
	{


		if (!me || !fieldRef) return 0;
		Var *where = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,(WObject)me);
			//&((VMAccess *)env)->objectPtr((WObject)me)[((WClassField *)fieldRef)->var.varOffset+1];
		return ((VMAccess *)env)->varToLong(where);
	}
	virtual double getDoubleField(FieldRef fieldRef)
	{
		if (!me || !fieldRef) return 0;
		Var *where = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,(WObject)me);
			//&((VMAccess *)env)->objectPtr((WObject)me)[((WClassField *)fieldRef)->var.varOffset+1];
		return ((VMAccess *)env)->varToDouble(where);
	}
	virtual ObjectRef getObjectField(FieldRef fieldRef)
	{
		if (!me || !fieldRef) return 0;
		return holdObject((ObjectRef)((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,(WObject)me)->obj);
			//((VMAccess *)env)->objectPtr((WObject)me)[((WClassField *)fieldRef)->var.varOffset+1].obj);
	}
#undef DefineSet
#define DefineSet(TYPE,NATIVE,NAME,METHOD) virtual void NAME(FieldRef fieldRef,TYPE value){	if (!me || !fieldRef) return;	((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,(WObject)me)->METHOD = (NATIVE)value;}
	DefineSet(int,int,setBooleanField,intValue)
	DefineSet(ObjectRef,int,setObjectField,obj)
	DefineSet(char,int,setByteField,intValue)
	DefineSet(WCHAR,int,setCharField,intValue)
	DefineSet(int16,int,setShortField,intValue)
	DefineSet(int,int,setIntField,intValue)
	//DefineSet(_int64,jlong,setLongField,SetLongField)
	//DefineSet(double,jdouble,setDoubleField,SetDoubleField)
	DefineSet(float,float,setFloatField,floatValue)
	virtual void setLongField(FieldRef fieldRef,_int64 value)
	{
		if (!me || !fieldRef) return;
		Var *where = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,(WObject)me);
		((VMAccess *)env)->longToVar(value,where);
	}
	virtual void setDoubleField(FieldRef fieldRef,double value)
	{
		if (!me || !fieldRef) return;
		Var *where = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,(WObject)me);
		((VMAccess *)env)->doubleToVar(value,where);
	}
//
// Static Variable Access.
//
#undef DefineGet
#define DefineGet(TYPE,NAME,METHOD) virtual TYPE getStatic##NAME##Field(FieldRef fieldRef){	if (!fieldRef) return 0;	return (TYPE)(((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,0)->METHOD);}

	DefineGet(int,Boolean,intValue)
	//DefineGet(ObjectRef,Object,obj)
	DefineGet(char,Byte,intValue)
	DefineGet(WCHAR,Char,intValue)
	DefineGet(int16,Short,intValue)
	DefineGet(int,Int,intValue)
	DefineGet(float,Float,floatValue)

	virtual _int64 getStaticLongField(FieldRef fieldRef)
	{
		Var vars[2];
		if (!fieldRef) return 0;
		Var *v = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,0);
		vars[0] = v[1];
		vars[1] = v[0];
		return ((VMAccess *)env)->varToLong(vars);
	}
	virtual double getStaticDoubleField(FieldRef fieldRef)
	{
		Var vars[2];
		if (!fieldRef) return 0;
		Var *v = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,0);
		vars[0] = v[1];
		vars[1] = v[0];
		return ((VMAccess *)env)->varToDouble(vars);
	}
	virtual ObjectRef getStaticObjectField(FieldRef fieldRef)
	{
		if (!me || !fieldRef) return 0;
		return holdObject((ObjectRef)(((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,0)->obj));
	}

#undef DefineSet
#define DefineSet(TYPE,NAME,METHOD) virtual void setStatic##NAME##Field(FieldRef fieldRef,TYPE value){	if (!fieldRef) return;	((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,0)->METHOD = value;}

	DefineSet(int,Boolean,intValue)
	DefineSet(ObjectRef,Object,obj)
	DefineSet(char,Byte,intValue)
	DefineSet(WCHAR,Char,intValue)
	DefineSet(int16,Short,intValue)
	DefineSet(int,Int,intValue)
	DefineSet(float,Float,floatValue)

	virtual void setStaticLongField(FieldRef fieldRef,_int64 value)
	{
		Var vars[2];
		if (!fieldRef) return;
		((VMAccess *)env)->longToVar(value,vars);
		Var *v = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,0);
		v[1] = vars[0];
		v[0] = vars[1];
	}
	virtual void setStaticDoubleField(FieldRef fieldRef,double value)
	{
		Var vars[2];
		if (!fieldRef) return;
		((VMAccess *)env)->doubleToVar(value,vars);
		Var *v = ((VMAccess *)env)->getFieldVarPointer((WClassField *)fieldRef,0);
		v[1] = vars[0];
		v[0] = vars[1];
	}

#undef GetArrayRgn
#define GetArrayRgn(TYPE,NATIVE,NATIVEARRAY,NAME,METHOD) virtual void NAME(ObjectRef array,int start,int length,TYPE *dest){		if (!array) return;	memcpy(dest,&((TYPE *)WOBJ_arrayStart(array))[start],sizeof(TYPE)*length);}

	GetArrayRgn(char,jbyte,jbyteArray,getByteArrayRegion,GetByteArrayRegion)
	GetArrayRgn(WCHAR,jchar,jcharArray,getCharArrayRegion,GetCharArrayRegion)
	GetArrayRgn(int16,jshort,jshortArray,getShortArrayRegion,GetShortArrayRegion)
	GetArrayRgn(int,jint,jintArray,getIntArrayRegion,GetIntArrayRegion)
	GetArrayRgn(_int64,jlong,jlongArray,getLongArrayRegion,GetLongArrayRegion)
	GetArrayRgn(float,jfloat,jfloatArray,getFloatArrayRegion,GetFloatArrayRegion)
	GetArrayRgn(double,jdouble,jdoubleArray,getDoubleArrayRegion,GetDoubleArrayRegion)
	GetArrayRgn(int,jboolean,jbooleanArray,getBooleanArrayRegion,GetBooleanArrayRegion)
#undef SetArrayRgn
#define SetArrayRgn(TYPE,NATIVE,NATIVEARRAY,NAME,METHOD)virtual void NAME(ObjectRef array,int start,int length,const TYPE *src){	if (!array) return;	memcpy(&((TYPE *)WOBJ_arrayStart(array))[start],src,sizeof(TYPE)*length);}

	SetArrayRgn(char,jbyte,jbyteArray,setByteArrayRegion,SetByteArrayRegion)
	SetArrayRgn(WCHAR,jchar,jcharArray,setCharArrayRegion,SetCharArrayRegion)
	SetArrayRgn(int16,jshort,jshortArray,setShortArrayRegion,SetShortArrayRegion)
	SetArrayRgn(int,jint,jintArray,setIntArrayRegion,SetIntArrayRegion)
	SetArrayRgn(_int64,jlong,jlongArray,setLongArrayRegion,SetLongArrayRegion)
	SetArrayRgn(float,jfloat,jfloatArray,setFloatArrayRegion,SetFloatArrayRegion)
	SetArrayRgn(double,jdouble,jdoubleArray,setDoubleArrayRegion,SetDoubleArrayRegion)
	SetArrayRgn(int,jboolean,jbooleanArray,setBooleanArrayRegion,SetBooleanArrayRegion)


	virtual ObjectRef getObjectArrayElement(ObjectRef array,int index)
	{
		if (!array) return 0;
		return holdObject((ObjectRef)((WObject*)WOBJ_arrayStart(array))[index]);
	}
	virtual void setObjectArrayElement(ObjectRef array,int index,ObjectRef value)
	{
		if (!array) return;
		((WObject*)WOBJ_arrayStart(array))[index] = (WObject)value;
	}
	virtual JavaString objectToJavaString(ObjectRef jString)
	{
		WCHAR *chars = stringChars((WObject)jString);
		int len = stringCharsLength((WObject)jString);
		if (chars == NULL) return NULL;
		JavaString js = new java_string;
		js->make(chars,len);
		return js;
	}


	virtual ObjectRef newString(JavaString string,int deleteIt = 0)
	{
		return holdObject((ObjectRef)newEweString(string,deleteIt));
	}
	virtual ObjectRef newArray(char *classOrTypeName,int length)
	{
		return holdObject((ObjectRef)((VMAccess *)env)->createArray(classOrTypeName,length));
	}
	virtual int arrayLength(ObjectRef array)

	{
		if (array == 0) return 0;
		return WOBJ_arrayLen((WObject)array);
	}
	virtual int64 getWindowHandle(ObjectRef obj)
	{
		return ((VMAccess *)env)->getWindowHandle((WObject)obj);
	}
	virtual ObjectRef suspendResume(ObjectRef obj = NULL)

	{
		return holdObject((ObjectRef)((VMAccess *)env)->suspendResumeCoroutine((WObject)obj));
	}
	virtual VMRef getVM() {return (VMRef)env;}
	virtual void callBack(ObjectRef obj,ObjectRef data)
	{
		((VMAccess *)env)->asyncCallBack(obj,data);
	}
	virtual void threadEnding()
	{
		((VMAccess *)env)->externalThreadEnding();
	}

//
// Instance Methods
//
#undef DefineCall
#define DefineCall(NAME,TYPE,METHOD)virtual TYPE call##NAME##Method(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0){	JValue ret; ret.METHOD = 0;	((VMAccess *)env)->invokeMethod((WClassMethod *)method,obj,pars,&ret,0); return ret.METHOD;}

	DefineCall(Int,int,i)
	//DefineCall(Double,double,d)
	DefineCall(Byte,char,b)
	DefineCall(Char,WCHAR,c)
	DefineCall(Short,int16,s)
	DefineCall(Long,_int64,j)
	DefineCall(Float,float,f)
	DefineCall(Boolean,int,z)
	//DefineCall(Object,ObjectRef,l)


virtual double callDoubleMethod(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JValue ret;
	((VMAccess *)env)->invokeMethod((WClassMethod *)method,obj,pars,&ret,0);
	return getJdouble(ret);
}
virtual ObjectRef callObjectMethod(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JValue ret; ret.l = 0;
	((VMAccess *)env)->invokeMethod((WClassMethod *)method,obj,pars,&ret,0);
	return holdObject(ret.l);
}
virtual void callVoidMethod(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JValue ret;
	((VMAccess *)env)->invokeMethod((WClassMethod *)method,obj,pars,&ret,0);
}

//
// Static Methods
//
#undef DefineCall
#define DefineCall(NAME,TYPE,METHOD)	virtual TYPE callStatic##NAME##Method(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0){	JValue ret; ret.METHOD = 0;	((VMAccess *)env)->invokeMethod((WClassMethod *)method,0,pars,&ret,0); return ret.METHOD;}

	DefineCall(Int,int,i)
	//DefineCall(Double,double,d)
	DefineCall(Byte,char,b)
	DefineCall(Char,WCHAR,c)
	DefineCall(Short,int16,s)
	DefineCall(Long,_int64,j)
	DefineCall(Float,float,f)
	DefineCall(Boolean,int,z)
//	DefineCall(Object,ObjectRef,l)

virtual double callStaticDoubleMethod(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JValue ret;
	((VMAccess *)env)->invokeMethod((WClassMethod *)method,0,pars,&ret,0);
	return getJdouble(ret);
}
virtual ObjectRef callStaticObjectMethod(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JValue ret; ret.l = 0;
	((VMAccess *)env)->invokeMethod((WClassMethod *)method,0,pars,&ret,0);
	return holdObject(ret.l);
}
virtual void callStaticVoidMethod(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JValue ret;
	((VMAccess *)env)->invokeMethod((WClassMethod *)method,0,pars,&ret,0);
}

//########################################################################
}*EweObject;
//########################################################################

#endif //NO_EWE_METHODS

#ifndef NO_JNI_METHODS
#include <jni.h>
//########################################################################
//
// This is an implementation of object_access for JNI.
//
//########################################################################
typedef class java_object : public object_access{
//########################################################################
protected:
	java_object(){}
public:
	virtual object_access *getNew(ObjectRef ref = 0)
	{
		java_object *ret = new java_object();
		ret->env = env;
		if (ref != 0) ret->setObject(ref);
		return ret;
	}
	java_object(void *Env,ObjectRef myRef = 0) : object_access(Env,myRef){if (myRef) myClass = getClassRef(myRef);}
	java_object(void *Env,char *className) : object_access(Env,className){myClass = getClassRef(className);}
	java_object(VMRef vmref) : object_access(vmref){
		((JavaVM *)vmref)->AttachCurrentThread(&env,NULL);
	}
	virtual object_access & setVM(VMRef vmref)
	{
		object_access::setVM(vmref);
		((JavaVM *)vmref)->AttachCurrentThread(&env,NULL);
		return *this;
	}
	virtual ClassRef getClassRef(ObjectRef objectRef)
	{
		if (objectRef == 0) return 0;
		JNIEnv *jni = (JNIEnv *)env;
		ObjectRef before = getCurrentException();
		ClassRef ret = (ClassRef) jni->NewGlobalRef(jni->GetObjectClass((jobject)objectRef));
		if (!before && getCurrentException()) clearCurrentException();
		return ret;
	}
	virtual ClassRef getClassRef(char *className)
	{

		JNIEnv *jni = (JNIEnv *)env;
		ObjectRef before = getCurrentException();
		ClassRef ret = (ClassRef) jni->NewGlobalRef(jni->FindClass(className));
		if (!before && getCurrentException()) clearCurrentException();
		return ret;
	}
	virtual ClassRef getSuperclass(ClassRef clazz)
	{
		JNIEnv *jni = (JNIEnv *)env;
		return (ClassRef)jni->GetSuperclass((jclass)clazz);
	}

	virtual FieldRef getFieldRef(ClassRef classRef,char *fieldName,char *fieldType)
	{
		JNIEnv *jni = (JNIEnv *)env;
		ObjectRef before = getCurrentException();
		FieldRef ret = (FieldRef) jni->GetFieldID((jclass)classRef,fieldName,fieldType);
		if (!before && getCurrentException()) clearCurrentException();
		return ret;
	}
	virtual FieldRef getStaticFieldRef(ClassRef classRef,char *fieldName,char *fieldType)
	{
		JNIEnv *jni = (JNIEnv *)env;
		ObjectRef before = getCurrentException();
		FieldRef ret = (FieldRef) jni->GetStaticFieldID((jclass)classRef,fieldName,fieldType);
		if (!before && getCurrentException()) clearCurrentException();
		return ret;
	}
	virtual MethodRef getMethodRef(ClassRef classRef,char *methodName,char *methodSignature,ClassRef *vclassRef = NULL)
	{
		JNIEnv *jni = (JNIEnv *)env;
		ObjectRef before = getCurrentException();
		if (vclassRef != NULL) *vclassRef = classRef;
		MethodRef ret = (MethodRef) jni->GetMethodID((jclass)classRef,methodName,methodSignature);
		if (!before && getCurrentException()) clearCurrentException();
		return ret;
	}
	virtual MethodRef getStaticMethodRef(ClassRef classRef,char *methodName,char *methodSignature,ClassRef *vclassRef = NULL)
	{
		JNIEnv *jni = (JNIEnv *)env;
		ObjectRef before = getCurrentException();
		if (vclassRef != NULL) *vclassRef = classRef;
		MethodRef ret = (MethodRef) jni->GetStaticMethodID((jclass)classRef,methodName,methodSignature);
		if (!before && getCurrentException()) clearCurrentException();
		return ret;
	}
	//===================================================================
	virtual double getJdouble(JValue jv){return jv.dDontUse;}
	//===================================================================
	virtual void setJdouble(JValue *jv,double value){jv->dDontUse = value;}
	//===================================================================

	virtual int isAssignableFrom(ClassRef clazz,ClassRef targetClazz)
	{
		JNIEnv *jni = (JNIEnv *)env;
		return jni->IsAssignableFrom((jclass)clazz,(jclass)targetClazz);
	}

	virtual void freeObject(ObjectRef obj)
	{
		if (obj == 0) return;
		JNIEnv *jni = (JNIEnv *)env;
		jni->DeleteLocalRef((jobject)obj);
	}

	virtual ObjectRef allocObject(ClassRef classRef)
	{
		JNIEnv *jni = (JNIEnv *)env;
		return (ObjectRef)jni->AllocObject((jclass) classRef);
	}
	virtual ObjectRef getCurrentException()

	{
		JNIEnv *jni = (JNIEnv *)env;
		return (ObjectRef)jni->ExceptionOccurred();
	}
	virtual void clearCurrentException()
	{
		JNIEnv *jni = (JNIEnv *)env;
		jni->ExceptionClear();
	}
	virtual ObjectRef throwException(ObjectRef exception)
	{

		JNIEnv *jni = (JNIEnv *)env;
		if (jni->Throw((jthrowable)exception)) return exception;
		return 0;
	}
	virtual int isSameObject(ObjectRef one,ObjectRef two)
	{
		JNIEnv *jni = (JNIEnv *)env;
		return jni->IsSameObject((jobject)one,(jobject)two);
	}
	virtual int monitorEnter(ObjectRef monitor)
	{
		JNIEnv *jni = (JNIEnv *)env;
		return jni->MonitorEnter((jobject)monitor);
	}
	virtual int monitorExit(ObjectRef monitor)
	{
		JNIEnv *jni = (JNIEnv *)env;
		return jni->MonitorExit((jobject)monitor);
	}

//
// Instance Variable Access.
//

#undef DefineGet
#define DefineGet(TYPE,NAME,METHOD)	virtual TYPE NAME(FieldRef fieldRef){		JNIEnv *jni = (JNIEnv *)env;		if (!me || !fieldRef) return 0;		return (TYPE)jni->METHOD((jobject)me,(jfieldID)fieldRef);}

	DefineGet(int,getBooleanField,GetBooleanField)
	DefineGet(ObjectRef,getObjectField,GetObjectField)
	DefineGet(char,getByteField,GetByteField)
	DefineGet(WCHAR,getCharField,GetCharField)
	DefineGet(int16,getShortField,GetShortField)

	DefineGet(int,getIntField,GetIntField)
	DefineGet(_int64,getLongField,GetLongField)
	DefineGet(double,getDoubleField,GetDoubleField)
	DefineGet(float,getFloatField,GetFloatField)

#undef DefineSet
#define DefineSet(TYPE,NATIVE,NAME,METHOD)	virtual void NAME(FieldRef fieldRef,TYPE value){		JNIEnv *jni = (JNIEnv *)env;		if (!me || !fieldRef) return;		jni->METHOD((jobject)me,(jfieldID)fieldRef,(NATIVE)value);}
	DefineSet(int,jboolean,setBooleanField,SetBooleanField)
	DefineSet(ObjectRef,jobject,setObjectField,SetObjectField)


	DefineSet(char,jbyte,setByteField,SetByteField)
	DefineSet(WCHAR,jchar,setCharField,SetCharField)
	DefineSet(int16,jshort,setShortField,SetShortField)
	DefineSet(int,jint,setIntField,SetIntField)
	DefineSet(_int64,jlong,setLongField,SetLongField)
	DefineSet(double,jdouble,setDoubleField,SetDoubleField)
	DefineSet(float,jfloat,setFloatField,SetFloatField)

//
// Static Variable Access.
//
#undef DefineGet
#define DefineGet(TYPE,NATIVE,NAME) 	virtual TYPE getStatic##NAME##Field(FieldRef fieldRef){		JNIEnv *jni = (JNIEnv *)env;		if (!fieldRef) return 0;		return (TYPE)jni->GetStatic##NAME##Field((jclass)myClass,(jfieldID)fieldRef);}

	DefineGet(int,jboolean,Boolean)
	DefineGet(ObjectRef,jobject,Object)
	DefineGet(char,jbyte,Byte)
	DefineGet(WCHAR,jchar,Char)
	DefineGet(int16,jshort,Short)
	DefineGet(int,jint,Int)
	DefineGet(float,jfloat,Float)
	DefineGet(double,jdouble,Double)
	DefineGet(_int64,jlong,Long)

#undef DefineSet
#define DefineSet(TYPE,NATIVE,NAME) virtual void setStatic##NAME##Field(FieldRef fieldRef,TYPE value){	JNIEnv *jni = (JNIEnv *)env;	if (!fieldRef) return;	jni->SetStatic##NAME##Field((jclass)myClass,(jfieldID)fieldRef,(NATIVE)value);}

	DefineSet(int,jboolean,Boolean)
	DefineSet(ObjectRef,jobject,Object)
	DefineSet(char,jbyte,Byte)
	DefineSet(WCHAR,jchar,Char)
	DefineSet(int16,jshort,Short)
	DefineSet(int,jint,Int)
	DefineSet(float,jfloat,Float)
	DefineSet(double,jdouble,Double)
	DefineSet(_int64,jlong,Long)


	virtual JavaString objectToJavaString(ObjectRef jString)
	{
		JNIEnv *jni = (JNIEnv *)env;
		if (!jString) return 0;
		const WCHAR *chars = jni->GetStringChars((jstring)jString,NULL);
		int len = jni->GetStringLength((jstring)jString);
		if (chars == NULL) return NULL;
		JavaString js = new java_string;
		js->make(chars,len);
		jni->ReleaseStringChars((jstring)jString,chars);
		return js;
	}
	virtual ObjectRef newString(JavaString string,int deleteIt = 0)
	{
		if (!string)
			return (0);
		JNIEnv *jni = (JNIEnv *)env;
		ObjectRef ret = (ObjectRef)jni->NewString(string->data,string->length);
		if (deleteIt) delete string;
		return ret;
	}
	virtual ObjectRef newArray(char *classOrTypeName,int length)
	{
		JNIEnv *jni = (JNIEnv *)env;
		jobject at = NULL;
		switch(*classOrTypeName){
		case 'B': at = jni->NewByteArray(length); break;
		case 'C': at = jni->NewCharArray(length); break;
		case 'S': at = jni->NewShortArray(length); break;
		case 'I': at = jni->NewIntArray(length); break;
		case 'J': at = jni->NewLongArray(length); break;
		case 'F': at = jni->NewFloatArray(length); break;
		case 'D': at = jni->NewDoubleArray(length); break;
		case 'Z': at = jni->NewBooleanArray(length); break;
		case '[':
		case 'L': {
			jclass cl = jni->FindClass(classOrTypeName);
			if (cl != NULL) at = jni->NewObjectArray(length,cl,NULL);
				  }
		}
		return (ObjectRef)at;
	}

#undef GetArrayRgn
#define GetArrayRgn(TYPE,NATIVE,NATIVEARRAY,NAME,METHOD)	virtual void NAME(ObjectRef array,int start,int length,TYPE *dest){		JNIEnv *jni = (JNIEnv *)env; if (!array) return;		jni->METHOD((NATIVEARRAY)array,start,length,(NATIVE *)dest);}

	GetArrayRgn(char,jbyte,jbyteArray,getByteArrayRegion,GetByteArrayRegion)
	GetArrayRgn(WCHAR,jchar,jcharArray,getCharArrayRegion,GetCharArrayRegion)
	GetArrayRgn(int16,jshort,jshortArray,getShortArrayRegion,GetShortArrayRegion)
	GetArrayRgn(int,jint,jintArray,getIntArrayRegion,GetIntArrayRegion)
	GetArrayRgn(_int64,jlong,jlongArray,getLongArrayRegion,GetLongArrayRegion)
	GetArrayRgn(float,jfloat,jfloatArray,getFloatArrayRegion,GetFloatArrayRegion)
	GetArrayRgn(double,jdouble,jdoubleArray,getDoubleArrayRegion,GetDoubleArrayRegion)
	GetArrayRgn(int,jboolean,jbooleanArray,getBooleanArrayRegion,GetBooleanArrayRegion)

#undef SetArrayRgn
#define SetArrayRgn(TYPE,NATIVE,NATIVEARRAY,NAME,METHOD)	virtual void NAME(ObjectRef array,int start,int length,const TYPE *src){		JNIEnv *jni = (JNIEnv *)env; if (!array) return;		jni->METHOD((NATIVEARRAY)array,start,length,(NATIVE *)src);}

	SetArrayRgn(char,jbyte,jbyteArray,setByteArrayRegion,SetByteArrayRegion)
	SetArrayRgn(WCHAR,jchar,jcharArray,setCharArrayRegion,SetCharArrayRegion)
	SetArrayRgn(int16,jshort,jshortArray,setShortArrayRegion,SetShortArrayRegion)
	SetArrayRgn(int,jint,jintArray,setIntArrayRegion,SetIntArrayRegion)
	SetArrayRgn(_int64,jlong,jlongArray,setLongArrayRegion,SetLongArrayRegion)
	SetArrayRgn(float,jfloat,jfloatArray,setFloatArrayRegion,SetFloatArrayRegion)
	SetArrayRgn(double,jdouble,jdoubleArray,setDoubleArrayRegion,SetDoubleArrayRegion)
	SetArrayRgn(int,jboolean,jbooleanArray,setBooleanArrayRegion,SetBooleanArrayRegion)

	virtual ObjectRef getObjectArrayElement(ObjectRef array,int index)
	{
		JNIEnv *jni = (JNIEnv *)env;

		if (!array)
			return (0);
		return (ObjectRef)jni->GetObjectArrayElement((jobjectArray)array,index);
	}
	virtual void setObjectArrayElement(ObjectRef array,int index,ObjectRef value)
	{
		JNIEnv *jni = (JNIEnv *)env; if (!array) return;
		jni->SetObjectArrayElement((jobjectArray)array,index,(jobject)value);
	}
	virtual int arrayLength(ObjectRef array)
	{
		JNIEnv *jni = (JNIEnv *)env; if (!array) return 0;
		return jni->GetArrayLength((jarray)array);
	}
	virtual int64 getWindowHandle(ObjectRef obj)
	{
		return 0L;
	}
	virtual ObjectRef holdRef(ObjectRef obj)
	{
		JNIEnv *jni = (JNIEnv *)env;
		return (ObjectRef)jni->NewGlobalRef((jobject)obj);
	}
	virtual void releaseRef(ObjectRef obj)
	{
		if (obj == 0) return;
		JNIEnv *jni = (JNIEnv *)env;
		jni->DeleteGlobalRef((jobject)obj);
	}
	virtual ObjectRef suspendResume(ObjectRef obj = NULL)
	{
		JNIEnv *jni = (JNIEnv *)env;
		if (!obj){
			jclass cc = jni->FindClass("Lewe/sys/Coroutine;");
			jmethodID getCurrent = jni->GetStaticMethodID(cc,"getCurrent","()Lewe/sys/Coroutine;");
			if (getCurrent != NULL) return (ObjectRef)jni->CallStaticObjectMethod(cc,getCurrent);
		}else{
			jclass cc = jni->FindClass("Lewe/sys/Coroutine;");
			jmethodID wake = jni->GetMethodID(cc,"wakeup","()V");
			if (wake != NULL) jni->CallNonvirtualVoidMethod((jobject)obj,cc,wake);
		}
		return (ObjectRef)0;//((VMAccess *)env)->suspendResumeCoroutine((WObject)obj);
	}
	virtual void callBack(ObjectRef obj,ObjectRef data)
	{
		JNIEnv *jni = (JNIEnv *)env;
		jclass cc = jni->FindClass("Lewe/sys/Vm;");
		jmethodID callback = jni->GetStaticMethodID(cc,"callInSystemQueue","(Lewe/sys/CallBack;Ljava/lang/Object;)I");
		if (callback != NULL) jni->CallStaticIntMethod(cc,callback,obj,data);
	}
	virtual VMRef getVM()
	{
		JNIEnv *jni = (JNIEnv *)env;
		VMRef vr;
		if (jni->GetJavaVM((JavaVM **)&vr) != 0) return 0;
		return vr;
	}

//
// Instance Methods
//
#undef DefineCall
#define DefineCall(NAME,TYPE,METHOD)	virtual TYPE call##NAME##Method(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0){	JNIEnv *jni = (JNIEnv *)env;	if (!callAsynchronously) eweMonitorEnter();	TYPE ret = (TYPE)jni->Call##NAME##MethodA((jobject)obj,(jmethodID)method,(union jvalue *)pars);	if (!callAsynchronously) eweMonitorExit();	return ret;}

	DefineCall(Int,int,i)
	DefineCall(Double,double,d)
	DefineCall(Byte,char,b)
	DefineCall(Char,WCHAR,c)
	DefineCall(Short,int16,s)
	DefineCall(Long,_int64,j)
	DefineCall(Float,float,f)
	DefineCall(Boolean,int,z)
	DefineCall(Object,ObjectRef,l)

virtual void callVoidMethod(ObjectRef obj,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JNIEnv *jni = (JNIEnv *)env;
	if (!callAsynchronously) eweMonitorEnter();
	jni->CallVoidMethodA((jobject)obj,(jmethodID)method,(union jvalue *)pars);
	if (!callAsynchronously) eweMonitorExit();
	}

//
// Static Methods
//
#undef DefineCall
#define DefineCall(NAME,TYPE,METHOD)	virtual TYPE callStatic##NAME##Method(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0){	JNIEnv *jni = (JNIEnv *)env;	if (!callAsynchronously) eweMonitorEnter();	TYPE ret = (TYPE)jni->CallStatic##NAME##MethodA((jclass)clazz,(jmethodID)method,(union jvalue *)pars);	if (!callAsynchronously) eweMonitorExit();	return ret;}

	DefineCall(Int,int,i)
	DefineCall(Double,double,d)
	DefineCall(Byte,char,b)
	DefineCall(Char,WCHAR,c)
	DefineCall(Short,int16,s)
	DefineCall(Long,_int64,j)
	DefineCall(Float,float,f)
	DefineCall(Boolean,int,z)
	DefineCall(Object,ObjectRef,l)

virtual void callStaticVoidMethod(ClassRef clazz,MethodRef method,JValue *pars,int callAsynchronously = 0){
	JNIEnv *jni = (JNIEnv *)env;
	if (!callAsynchronously) eweMonitorEnter();
	jni->CallStaticVoidMethodA((jclass)clazz,(jmethodID)method,(union jvalue *)pars);

	if (!callAsynchronously) eweMonitorExit();
	}
	virtual void threadEnding()
	{
	}

//########################################################################
}*JavaObject;
//########################################################################
#endif //NO_JNI_METHODS
#endif //NO_OBJECTS
#endif //NO_UTILITIES
#endif //EWEDEFS_DEFINED
