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

__IDSTRING(rcsid_nmunix_a, "$MirOS: contrib/hosted/ewe/vm/nmunix_a.c,v 1.3 2008/04/11 00:27:23 tg Exp $");

#undef FREE_ON_EXIT
#undef SECURE_CLASS_HEAP
#define LOCK_CLASS_HEAP
#define UNLOCK_CLASS_HEAP

//
// type converters
//
#define getUInt32(b) (uint32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) )
#define getUInt16(b) (uint16)(((b)[0]<<8)|(b)[1])

#define getInt32(b) (int32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) )
#define getInt16(b) (int16)(((b)[0]<<8)|(b)[1])

static float32 getFloat32(uchar *buf)
	{
	uint32 i;
	float32 f;

	// we need to make sure we're aligned before casting
	i = ((uint32)buf[0] << 24) | ((uint32)buf[1] << 16) | ((uint32)buf[2] << 8) | (uint32)buf[3];
	f = *((float32 *)&i);
	return f;
	}
/*
static double getFloat64(uchar *buf)
{
	Float64 f;
	f.l[1] = getUInt32(buf);
	f.l[0] = getUInt32(buf+4);
	return f.d;
}
static int64 getInt64(uchar *buf)
{
	Int64 i;
	i.l[1] = getUInt32(buf);
	i.l[0] = getUInt32(buf+4);
	return i.i;
}
*/

//
// x portability functions
//

// NOTE: The str, mem and malloc routines aren't documented in the reference
// manuals for CE, however, the methods do exist in the CE library and have
// prototypes, etc.

#define xstrncmp(s1, s2, n) strncmp(s1, s2, n)

#define xstrncpy(dst, src, n) strncpy(dst, src, n)

#define xstrlen(s) strlen(s)

#define xstrcat(dst, src) strcat(dst, src)

#define xmemmove(dst, src, size) memmove(dst, src, size)

#define xmemzero(mem, len) memset(mem, 0, len)

#define xmalloc(size) malloc(size);

#define xfree(ptr) free(ptr)


typedef struct tempText {
	int totalSize;
	TCHAR *text;
}* TempText;

struct tempText
	currencySymbol, groupSymbol, groupings, decimalSymbol,
	negativeSymbol, positiveSymbol, tempInfo, decDigits, formatted;

static int textLength(TCHAR *str)
{
	int sz = 0;
	if (str == NULL) return 0;
	for (;str[sz] != 0;sz++);
	return sz;
}
static int textStore(TCHAR *dest,TCHAR *src,int length)
{
	int did = 0;
	if (src == NULL) return did;
	for (;did<length;did++)
		if ((*dest++ = *src++) == 0) return did;
	return did;
}
static TCHAR * txtncpy(TCHAR *dest,TCHAR *src,int length)
{
	TCHAR *ret = dest;
	if (src == NULL || dest == NULL) return dest;
	else{
		int did = 0;
		for (;did<length || length < 0;did++)
			if ((*dest++ = *src++) == 0) break;
	}
	return ret;
}
static TCHAR *txtcpy(TCHAR *dest,TCHAR *src) {return txtncpy(dest,src,-1);}
static TCHAR *txtncat(TCHAR *dest,TCHAR *src,int length)
{
	TCHAR *ret = dest;
	if (src == NULL || dest == NULL) return dest;
    while (*dest != 0) dest++;
	txtncpy(dest,src,length);
	if (length >= 0) dest[length] = 0;
	return ret;
}
static TCHAR *txtcat(TCHAR *dest,TCHAR *src) {return txtncat(dest,src,-1);}

static int textCompare(TCHAR *one,int oneLen,TCHAR *two,int twoLen)
{
	int i = 0;
	if (one == two) return 0;
	if (two == NULL) return 1;
	if (one == NULL) return -1;
	if (oneLen <= -1) oneLen = textLength(one);
	if (twoLen <= -1) twoLen = textLength(two);
	for (i = 0; i<oneLen && i<twoLen; i++){
		TCHAR o = one[i];
		TCHAR t = two[i];
		if (o < t) return -1;
		else if (o > t) return -1;
	}
	if (oneLen > twoLen) return 1;
	else if (oneLen <twoLen) return -1;
	else return 0;
}
static void expandText(TempText tt,int newSize)
{
	if (newSize > tt->totalSize){
		TCHAR *newText = (TCHAR *)malloc(sizeof(TCHAR)*newSize);
		if (tt->text != NULL){
			memcpy(newText,tt->text,sizeof(TCHAR)*tt->totalSize);
			free(tt->text);
		}
		tt->text = newText;
		tt->totalSize = newSize;
	}
}
static int tempCat(TempText dest,int destPosition,TCHAR *src,int length)
{
	if (src == NULL) return 0;
	if (length < 0) length = textLength(src);
	while(destPosition+length+1 >= dest->totalSize) expandText(dest,(dest->totalSize+1)*2);
	textStore(dest->text+destPosition,src,length);
	*(dest->text+destPosition+length) = 0;
	return length;
}
//This must always return a newly allocated char *;
static TCHAR *toNativeText(char *text)
{
	int len = strlen(text);
	TCHAR *ret = (TCHAR *)malloc((len+1)*sizeof(TCHAR));
	int i;
	for (i = 0; i<len; i++) ret[i] = (TCHAR)text[i];
	ret[len] = 0;
	return ret;
}
static int textCopy(TCHAR *src, TCHAR *dst, int max)
{
	int i;
	for (i = 0; i < max - 1; i++)
		{
		dst[i] = (TCHAR)src[i];
		if (!dst[i])
			return i;
		}
	dst[i] = 0;
	return i;
}
