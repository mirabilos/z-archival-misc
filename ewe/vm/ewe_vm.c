/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.15, February 13, 2002                        *
 *  Copyright (c) 2007, 2008, 2015 Thorsten "mirabilos" Glaser <tg@mirbsd.org>   *
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

__IDSTRING(rcsid_vm, "$MirOS: contrib/hosted/ewe/vm/ewe_vm.c,v 1.8 2008/04/11 03:57:47 tg Exp $");

struct static_asserts {
	char ilp32_only[sizeof(void *) <= sizeof(int) ? 1 : -1];
};

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


#if defined(NO_PLATFORM_DEFINED)
To compile, you need to define one of the platforms above.
#endif

//
// See if we are using WinCE
//
#ifndef WINCE
	#if defined(UNDER_CE) || defined(_WIN32_WCE)
		#define WINCE
	#endif
#endif
//
// Determine which CE platform we are using.
//

#if defined(WINCE)
#define WIN32 1
	#ifdef WCE_PLATFORM_CASIO_BE300 // Casio BE-300
		#define CASIOBE300
		#define USE_AYGSHELL
	#elif WCE_PLATFORM_RLC_ARM
		#define RLC
	#elif (WIN32_PLATFORM_HPCPRO || WIN32_PLATFORM_HPC2000)
		#define HPC
	#elif (WIN32_PLATFORM_WFSP)
		#define MS_SMARTPHONE
		#define SMARTPHONE_VERSION WIN32_PLATFORM_WFSP
	#elif (WCE_PLATFORM_STANDARDSDK)
		#define HPC
	#elif (WCE_PLATFORM_PPT8800C42)
		#define CE_NET
		#define USE_AYGSHELL
	#elif (WCE_PLATFORM_JTMOBHH)
		#define CE_NET
		#define USE_AYGSHELL
	#elif (WCE_PLATFORM_BONANZAPDA) //Added this line and the following one for the Radix FW700 SDK
		#define HPC
	#else
		#define PocketPC
		#if WIN32_PLATFORM_PSPC == 310
			#define PocketPC_Version 2002
		#elif WIN32_PLATFORM_PSPC == 400
			#define PocketPC_Version 2003
		#else
			#define PocketPC_Version 1
		#endif
	#endif
#endif

#ifdef PocketPC
	#define USE_AYGSHELL
#endif
//
// The emulator gives sound problems under WindowsXP
//
#if defined(_WIN32_WCE_EMULATION)
#define NO_SOUND
#endif

#ifdef WIN32
#define DllExport  __declspec( dllexport )
#else
#define DllExport static
#endif


#if (defined(_UNICODE) || defined(WINCE)) && !defined(UNICODE)
#define UNICODE
#endif

#define USE_FINALIZE
#define ALWAYS_SHOW_SIP 0
#define USE_POOLED_UTF
//#define USE_LOG

#ifdef MAKING_POOL
#define DONT_BIND
#undef CODED_EWE_FILE
#endif

/*
#if UNIX & ARM
#define LINUX_ARM
#endif
*/

#define OLDPC
static int debugged = 0;
#ifdef OLDPC
#define STACKSAVESIZE 4
#else
#define STACKSAVESIZE 3
#endif

#ifndef WINCE
#include <time.h>
#endif

/*

Welcome to the WabaVM source. This is the platform indepdenent code for the
interpreter, class parser and garbage collector. I like the code myself (of
course I wrote it, so that's not saying much) and hopefully it doesn't seem
too complicated. Actually, I'm hoping it reads rather easily given what it
does.

If you're looking through the source and wondering about the QUICKBIND stuff,
you should know that you can turn it off completely by removing all the code
with an #ifdef QUICKBIND around it. It's an optimization that speeds things
up quite a bit. So, if you're trying to figure out the code, ignore the
QUICKBIND stuff at first and then look at it later on.

The SMALLMEM define determines whether the VM uses a small or large memory
model (16 bit or 32 bit offsets). The default is SMALLMEM which means that if
progams use memory beyond a certain size, they jump to being slow since they
can't QUICKBIND any more. It still works since the QUICKBIND is adaptive, if
the offset fits, it uses it, if not, it doesn't use QUICKBIND.

This file should pretty much compile on any platform. To port the VM, you
create the 3 native method (nm) files. See the nmport_a.c if you are
interested in starting a port or to see how easy/difficult it would be.

Have a blast!

Rick Wild

*/

//#define SMALLMEM 1
#define QUICKBIND 1

typedef signed char jbyte;
// undefine sanity checks for final releases
//#define SANITYCHECK 1
#ifdef UNIX
typedef long long int64;
#define cINT64(WHAT) WHAT##L
typedef unsigned long long uint64;
typedef char TCHAR;
typedef unsigned short WCHAR;
typedef int BOOL;
#else
#define cINT64(WHAT) WHAT
typedef _int64 int64;
#endif
typedef unsigned char byte;
typedef int int32;
typedef unsigned int uint32;
typedef float float32;
typedef unsigned char uchar;
typedef short int16;
typedef unsigned short uint16;

#ifdef QUICKBIND
static int32 postPaintMethodMapNum = -1;
static int32 postEventMethodMapNum = -1;
static int32 handleEventMethodMapNum = -1;
static int32 onTimerTickMethodMapNum = -1;
static int32 doPaintMethodMapNum = -1;
#endif

static void gc();

#ifdef UNIX
#ifndef TEXT
#define TEXT(VALUE) VALUE
#endif

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif
#endif



void *mMalloc(int size)
{
	void *ret = malloc(size);
	if (ret != 0) return ret;
	gc();
	return malloc(size);
}

typedef struct byte_data {
	char *data;
	int space;
	int length;
}* ByteData;

struct byte_data className;

void expandSpaceFor(ByteData bd,int size,int growSize,int copyOld)
{
	int nsize = bd->space;
	char *nd;
	if (size <= nsize && bd->data != NULL) return;
	if (size < 0) size = 0;
	while (size > nsize) nsize += growSize;
	nd = (char *)malloc(nsize);
	if (copyOld && bd->data != NULL) memcpy(nd,bd->data,bd->space);
	if (bd->data != NULL) free(bd->data);
	bd->data = nd;
	bd->space = nsize;
}


void freeData(ByteData bd)
{
	if (bd->data != NULL) free(bd->data);
	bd->data = NULL;
	bd->space = bd->length = 0;
}

static uint32 tempUint;

#define putUInt32(b,v) tempUint = (v),(b)[0] = (uchar)((tempUint>>24) & 0xff),(b)[1] = (uchar)((tempUint>>16) & 0xff),(b)[2] = (uchar)((tempUint>>8) & 0xff),(b)[3] = (uchar)(tempUint & 0xff)
#define putUInt16(b,v) tempUint = (v),(b)[0] = (uchar)((tempUint>>8) & 0xff),(b)[1] = (uchar)((tempUint) & 0xff)

#if defined(PALMOS)
#include "nmpalm_a.c"
#elif defined(WIN32)
#include "nmwin32_a.c"
#elif defined(UNIX)
#include "nmunix_a.c"
#endif

typedef uint32 WObject;


#ifdef UNIX
typedef void *FILE_HANDLE;
typedef void *MEMORY_MAPPED_HANDLE;
#endif


/*
"True words are not beautiful, beautiful words are not true.
 The good are not argumentative, the argumentative are not good.
 Knowers do not generalize, generalists do not know.
 Sages do not accumulate anything but give everything to others,
 having more the more they give.
 The Way of heaven helps and does not harm.
 The Way for humans is to act without contention."

 - Lao-tzu, Tao Te Ching circa 500 B.C.
*/

//
// TYPES AND METHODS
//

// Access flags
#define ACCESS_PUBLIC       0x0001
#define ACCESS_PRIVATE      0x0002
#define ACCESS_PROTECTED    0x0004
#define ACCESS_STATIC       0x0008
#define ACCESS_FINAL        0x0010
#define ACCESS_SYNCHRONIZED 0x0020
#define ACCESS_VOLATILE     0x0040
#define ACCESS_TRANSIENT    0x0080
#define ACCESS_NATIVE       0x0100
#define ACCESS_INTERFACE    0x0200
#define ACCESS_ABSTRACT     0x0400
#define ACCESS_STRICT       0x0800

// Constant Pool tags
#define CONSTANT_Utf8               1
#define CONSTANT_Integer            3
#define CONSTANT_Float              4
#define CONSTANT_Long               5
#define CONSTANT_Double             6
#define CONSTANT_Class              7
#define CONSTANT_String             8
#define CONSTANT_Fieldref           9
#define CONSTANT_Methodref          10
#define CONSTANT_InterfaceMethodref 11
#define CONSTANT_NameAndType        12

// MLB Tags
#define CONSTANT_PooledUtf8_MASK    0xc0
#define CONSTANT_PooledUtf8_1       0xc0
#define CONSTANT_PooledUtf8_2       0x80


// Standard Opcodes
#define OP_nop             0
#define OP_aconst_null     1
#define OP_iconst_m1       2
#define OP_iconst_0        3
#define OP_iconst_1        4
#define OP_iconst_2        5
#define OP_iconst_3        6
#define OP_iconst_4        7
#define OP_iconst_5        8
#define OP_lconst_0        9
#define OP_lconst_1        10
#define OP_fconst_0        11
#define OP_fconst_1        12
#define OP_fconst_2        13
#define OP_dconst_0        14
#define OP_dconst_1        15
#define OP_bipush          16
#define OP_sipush          17
#define OP_ldc             18
#define OP_ldc_w           19
#define OP_ldc2_w          20
#define OP_iload           21
#define OP_lload           22
#define OP_fload           23
#define OP_dload           24
#define OP_aload           25
#define OP_iload_0         26
#define OP_iload_1         27
#define OP_iload_2         28
#define OP_iload_3         29
#define OP_lload_0         30
#define OP_lload_1         31
#define OP_lload_2         32
#define OP_lload_3         33
#define OP_fload_0         34
#define OP_fload_1         35
#define OP_fload_2         36
#define OP_fload_3         37
#define OP_dload_0         38
#define OP_dload_1         39
#define OP_dload_2         40
#define OP_dload_3         41
#define OP_aload_0         42
#define OP_aload_1         43
#define OP_aload_2         44
#define OP_aload_3         45
#define OP_iaload          46
#define OP_laload          47
#define OP_faload          48
#define OP_daload          49
#define OP_aaload          50
#define OP_baload          51
#define OP_caload          52
#define OP_saload          53
#define OP_istore          54
#define OP_lstore          55
#define OP_fstore          56
#define OP_dstore          57
#define OP_astore          58
#define OP_istore_0        59
#define OP_istore_1        60
#define OP_istore_2        61
#define OP_istore_3        62
#define OP_lstore_0        63
#define OP_lstore_1        64
#define OP_lstore_2        65
#define OP_lstore_3        66
#define OP_fstore_0        67
#define OP_fstore_1        68
#define OP_fstore_2        69
#define OP_fstore_3        70
#define OP_dstore_0        71
#define OP_dstore_1        72
#define OP_dstore_2        73
#define OP_dstore_3        74
#define OP_astore_0        75
#define OP_astore_1        76
#define OP_astore_2        77
#define OP_astore_3        78
#define OP_iastore         79
#define OP_lastore         80
#define OP_fastore         81
#define OP_dastore         82
#define OP_aastore         83
#define OP_bastore         84
#define OP_castore         85
#define OP_sastore         86
#define OP_pop             87
#define OP_pop2            88
#define OP_dup             89
#define OP_dup_x1          90
#define OP_dup_x2          91
#define OP_dup2            92
#define OP_dup2_x1         93
#define OP_dup2_x2         94
#define OP_swap            95
#define OP_iadd            96
#define OP_ladd            97
#define OP_fadd            98
#define OP_dadd            99
#define OP_isub            100
#define OP_lsub            101
#define OP_fsub            102
#define OP_dsub            103
#define OP_imul            104
#define OP_lmul            105
#define OP_fmul            106
#define OP_dmul            107
#define OP_idiv            108
#define OP_ldiv            109
#define OP_fdiv            110
#define OP_ddiv            111
#define OP_irem            112
#define OP_lrem            113
#define OP_frem            114
#define OP_drem            115
#define OP_ineg            116
#define OP_lneg            117
#define OP_fneg            118
#define OP_dneg            119
#define OP_ishl            120
#define OP_lshl            121
#define OP_ishr            122
#define OP_lshr            123
#define OP_iushr           124
#define OP_lushr           125
#define OP_iand            126
#define OP_land            127
#define OP_ior             128
#define OP_lor             129
#define OP_ixor            130
#define OP_lxor            131
#define OP_iinc            132
#define OP_i2l             133
#define OP_i2f             134
#define OP_i2d             135
#define OP_l2i             136
#define OP_l2f             137
#define OP_l2d             138
#define OP_f2i             139
#define OP_f2l             140
#define OP_f2d             141
#define OP_d2i             142
#define OP_d2l             143
#define OP_d2f             144
#define OP_i2b             145
#define OP_i2c             146
#define OP_i2s             147
#define OP_lcmp            148
#define OP_fcmpl           149
#define OP_fcmpg           150
#define OP_dcmpl           151
#define OP_dcmpg           152
#define OP_ifeq            153
#define OP_ifne            154
#define OP_iflt            155
#define OP_ifge            156
#define OP_ifgt            157
#define OP_ifle            158
#define OP_if_icmpeq       159
#define OP_if_icmpne       160
#define OP_if_icmplt       161
#define OP_if_icmpge       162
#define OP_if_icmpgt       163
#define OP_if_icmple       164
#define OP_if_acmpeq       165
#define OP_if_acmpne       166
#define OP_goto            167
#define OP_jsr             168
#define OP_ret             169
#define OP_tableswitch     170
#define OP_lookupswitch    171
#define OP_ireturn         172
#define OP_lreturn         173
#define OP_freturn         174
#define OP_dreturn         175
#define OP_areturn         176
#define OP_return          177
#define OP_getstatic       178
#define OP_putstatic       179
#define OP_getfield        180
#define OP_putfield        181
#define OP_invokevirtual   182
#define OP_invokespecial   183
#define OP_invokestatic    184
#define OP_invokeinterface 185
#define OP_new             187
#define OP_newarray        188
#define OP_anewarray       189
#define OP_arraylength     190
#define OP_athrow          191
#define OP_checkcast       192
#define OP_instanceof      193
#define OP_monitorenter    194
#define OP_monitorexit     195
#define OP_wide            196
#define OP_multianewarray  197
#define OP_ifnull          198
#define OP_ifnonnull       199
#define OP_goto_w          200
#define OP_jsr_w           201
#define OP_breakpoint      202
//
// Error Handling
//
//#define DUMPERRORTRACE 1
static char *errorMessages[] =
	{
	"sanity",
	"incompatible device",
	"can't access waba classes",
	"can't access app classes",
	"can't allocate memory",
	"out of class memory",
	"out of object memory",
	"native stack overflow",
	"native stack underflow",
	"stack overflow",

	"bad class",
	"bad opcode",
	"can't find class",
	"can't find method",
	"can't find field",
	"null object access",
	"null array access",
	"index out of range",
	"divide by zero",
	"bad class cast",
	"class too large",
	"application error",
	"User Error 1",
	"Location Error",
	"invalid Coroutine call",
	"Not a valid application class",
	"Can't create application class",
	"string index out of range",
	"Can't find class library",
	"Out of system resources"
	};

// fatal errors
#define ERR_SanityCheckFailed        1
#define ERR_IncompatibleDevice       2
#define ERR_CantAccessCoreClasses    3
#define ERR_CantAccessAppClasses     4
#define ERR_CantAllocateMemory       5
#define ERR_OutOfClassMem            6
#define ERR_OutOfObjectMem           7
#define ERR_NativeStackOverflow      8
#define ERR_NativeStackUnderflow     9
#define ERR_StackOverflow            10

// program errors
#define ERR_BadClass                 11
#define ERR_BadOpcode                12
#define ERR_CantFindClass            13
#define ERR_CantFindMethod           14
#define ERR_CantFindField            15
#define ERR_NullObjectAccess         16
#define ERR_NullArrayAccess          17
#define ERR_IndexOutOfRange          18
#define ERR_DivideByZero             19
#define ERR_ClassCastException       20
#define ERR_ClassTooLarge            21
#define ERR_Application				 22
#define ERR_USER1					 23
#define ERR_Location                 24
#define ERR_Coroutine                25
#define ERR_BadAppClass              26
#define ERR_CantCreateAppClass		 27
#define ERR_StringIndexOutOfRange    28
#define ERR_CantFindClasses    29
#define ERR_OutOfSystemResources 30
// flags for stringToUtf()
#define STU_NULL_TERMINATE 1
#define STU_USE_STATIC     2

//
// types and accessors
//
/*
struct CallEntry
{
	int16 stackOffset;
	int16 methodFlags;
};
*/

typedef union
{
	int32 intValue;
	float32 floatValue;
	void *classRef;
	uchar *pc;
	uchar *oldpc;
	void *refValue;
	WObject obj;
	unsigned long half64; //64Bits
	uint32 stackOffset;
	//struct CallEntry call;
} Var;

typedef Var (*NativeFunc)(Var stack[]);
typedef void (*ObjDestroyFunc)(WObject obj);

#ifdef USE_LOG
void Log(char *text,int length)
{

	FILE *f = fopen("\\storage card\\log.txt","ab");
	if (length <= 0) length = strlen(text);
	fwrite(text,1,length,f);
	fclose(f);
}
#endif
//
// more types and accessors
//

#define WOBJPTR_isFinalized(optr) ((optr[0].intValue) & 1)

#define WOBJPTR_setFinalized(optr) ((optr[0].intValue) |= 1)
#define WOBJPTR_clearFinalized(optr) ((optr[0].intValue) &= ~1)
#define WOBJPTR_class(optr) (WClass *)((int32)(optr[0].classRef) & ~1)

//#define WOBJ_class(o) (WClass *)((objectPtr(o)[0].classRef))
#ifdef USE_FINALIZE
#define WOBJ_class(o) (WClass *)((objectPtr(o)[0].intValue)& ~1)
#else
#define WOBJ_class(o) (WClass *)((objectPtr(o)[0].intValue))
#endif

#define WOBJ_classAssign(o) ((objectPtr(o))[0].classRef)
#define WOBJ_var(o, idx) (objectPtr(o))[idx + 1]

// NOTE: These get various values in objects at defined offsets.
// If the variables in the base classes change, these offsets will
// need to be recomputed. For example, the first (StringCharArray)
// get the character array var offset in a String object.
#define WOBJ_StringCharArrayObj(o) (objectPtr(o))[1].obj
#define WOBJ_StringBufferStrings(o) (objectPtr(o))[1].obj
#define WOBJ_StringBufferCount(o) (objectPtr(o))[2].intValue

#define WOBJ_arrayType(o) (objectPtr(o))[1].intValue
#define WOBJ_arrayLen(o) (objectPtr(o))[2].intValue
#define WOBJ_arrayComponent(o) (objectPtr(o))[3].refValue
#define WOBJ_arrayStart(o) (&(objectPtr(o)[4]))

#define WOBJ_StringChars(o) ((uint16 *)WOBJ_arrayStart((objectPtr(o))[1].obj))
#define WOBJ_StringLength(o) WOBJ_arrayLen((objectPtr(o))[1].obj)

// for faster access
#define WOBJ_arrayTypeP(objPtr) (objPtr)[1].intValue
#define WOBJ_arrayLenP(objPtr) (objPtr)[2].intValue
#define WOBJ_arrayStartP(objPtr) (&(objPtr[4]))

typedef struct UtfStringStruct
	{
	char *str;
	uint32 len;
	} UtfString;

typedef struct linked_element {
	struct linked_element *next;
	union {
		void *ptr;
		int64 value;
	};
}*LinkedElement;

LinkedElement filesToDelete = NULL;

/*
typedef union {
	struct WClassStruct *ptr;
	uint32 pooledIndex; //This is overkill, only really need 10 bits.
}ClassRef;
*/

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
	//char *nameAndDescriptions;
	} WClassField;
*/

//==============================================================================
// This is used by a pooled class.
//==============================================================================
typedef struct WPooledFieldStruct {
//==============================================================================
	uint16 access;
	uint16 nameIndex;
	uint16 descIndex;
	union{
		uint16 staticFieldIndex;
		uint16 varOffset;
	};
//	uint16 pooledClass;
//==============================================================================
}WPooledField;
//==============================================================================

//==============================================================================
// This is used by a non-pooled class.
//==============================================================================
typedef struct WClassFieldStruct {
//==============================================================================
	uint16 access;
	uint16 nameIndex;
	uint16 descIndex;
	union{
		uint16 staticFieldIndex;
		uint16 varOffset;
	};
	Var *staticVarAddress; // For static non-pooled.
	struct WClassStruct *myClass;
#ifdef MAKING_POOL
	uint16 initializerIndex;
#endif
//==============================================================================
}WClassField;
//==============================================================================

#define FIELD_accessFlags(f) ((f)->access)
#define is64(f) ((f)->access & ACCESS_IS_64)
#define FIELD_nameIndex(f) ((f)->nameIndex)
#define FIELD_descIndex(f) ((f)->descIndex)
#define FIELD_is64(f)     (FIELD_accessFlags(f) & ACCESS_IS_64)
#define FIELD_isStatic(f) (FIELD_accessFlags(f) & ACCESS_STATIC)
#define FIELD_isPublic(f) (FIELD_accessFlags(f) & ACCESS_PUBLIC)
#define FIELD_isPooled(f) (FIELD_accessFlags(f) & ACCESS_POOLED)
//#define FIELD_class(f) (FIELD_isPooled(f) ? resolveClass(((WPooledField *)(f))->pooledClass):(f)->myClass)
#define FIELD_hasInitializer(f) (FIELD_accessFlags(f) & ACCESS_HAS_INITIALIZER)
#define GetStaticVarPointer(FL) (!FIELD_isPooled(FL) ?  (FL)->staticVarAddress : PooledFields+((FL)->staticFieldIndex))
#define GetStaticVarPointer2(FL) (!FIELD_isPooled(FL) ?  ((FL)->staticVarAddress)+1: PooledFields+((FL)->staticFieldIndex)+1)
#define GetVarOffset(FL) ((FL)->varOffset)
#define GetVarOffset2(FL) (((FL)->varOffset)+1)

typedef struct WClassMethodStruct WClassMethod;

typedef uint16 ClassByteOffset;


typedef struct WExceptionHandlerStruct {
	uint16 startPc, endPc, handlerPc, catchType;
} WExceptionHandler;

//#define GetPooledClass(CLASSREF) ()

static char sprintBuffer[1024];

#ifndef UNIX
static int mMessageBox(HWND,WCHAR *,WCHAR *,int);
#endif

//#define DB() mMessageBox(NULL,asciiToTempUnicode(sprintBuffer),L"Debug",MB_OK)

static uint32* ClassPoolTable;
static uchar* ClassPool;
static uchar* ClassPoolUtf;
static uint16* ClassPoolNames;
static uint16* ClassPoolHash;
static NativeFunc *PooledNatives;
static Var *PooledFields;

static uchar *allocClassPart(uint32 size);

static void setupClassPool(uchar *pool,int size,int alloc)
{
	uint32 numClasses, *info;
	if (pool != NULL){
		ClassPool = pool;
		info = (uint32*)ClassPool;
		numClasses = info[0];
		pool += 16;
		ClassPoolTable = (uint32*)pool;
		pool += (numClasses+1)*4;
		ClassPoolNames = (uint16*)(pool);
		pool += (numClasses+1)*4;
		ClassPoolHash = (uint16*)pool;
		pool += 256*2;
		ClassPoolUtf = ClassPool+info[3]+4;
		//sprintf(sprintBuffer,"%x %d",ClassPool,numClasses);
		//mMessageBox(NULL,asciiToTempUnicode(sprintBuffer),L"Debug",MB_OK);
	}
	if (ClassPool != NULL && alloc){
		uint32 *nt;
		info = (uint32*)ClassPool;
		//
		// Move the table into dynamic memory.
		//
		numClasses = info[0];
		nt = (uint32 *)allocClassPart((info[0]+1)*4);
		memcpy(nt,ClassPoolTable,(info[0]+1)*4);
		ClassPoolTable = nt;
		//
		PooledNatives = (NativeFunc *)allocClassPart(info[1]);
		PooledFields = (Var *)allocClassPart(info[2]);
	}
}

struct WClassStruct *resolveClass(uint16 ref);
uint16 tempref;
#define ResolveClass(C) (((tempref = (C)) & 0x8000) ? NULL : ((ClassPoolTable[tempref] & 1) ? resolveClass(tempref) : (WClass *)(ClassPoolTable[tempref])))

//
// This is used instead of CAFEBABE to mark a pooled class.
// Turned upside down it reads "ES POOL, SI" (is it pooled? yes, for those who don't habla)
//
#define PooledClassMarker 0x15700d53

//==============================================================================
typedef struct {
//==============================================================================
	int16 numHandlers;
	WExceptionHandler *handlers; // For non-pooled, absolute location.
	uchar *throwsEx;
	uchar *lineNumbers;
//==============================================================================
}WClassMethodExtra;
//==============================================================================
/*
#define METH_offset(M,VALUE) ((uchar *)(M)+VALUE)
#define METH_extra(M) ((M)->extra)
#define METH_pooledExtraOff(M) ((M)->extraOffset)
#define METH_pooledExtra(M) ((struct WClassMethodPooledExtra *)METH_offset(M,METH_pooledExtraOff(M)))
#define METH_hasHandlers(M) (METH_accessFlags(M) & ACCESS_HAS_HANDLERS)
#define METH_handlers(M) (!METH_isPooled(M) ? METH_extra(M)->handlers : (WExceptionHandler *)METH_offset(M,METH_pooledExtra(M)->handlerOffset))
#define METH_lineNumbers(M) (METH_isPooled(M) ? 0 :(METH_extra(M) ? METH_extra(M)->lineNumbers : 0))
#define METH_throws(M) (METH_isPooled(M) ?(METH_pooledExtraOff(M) && METH_pooledExtra(M)->throwsOffset ? METH_offset(M,METH_pooledExtra(M)->throwsOffset) : 0):(METH_extra(M) ? METH_extra(M)->throwsEx : 0))
*/
#define METH_pooled(M) ((struct WPooledMethodStruct *)(M))
#define METH_hasHandlerThrows(M) (METH_pooled(M)->codeOffset)
#define METH_codePtr(M) ((uchar *)(M)+(METH_pooled(M)->codeOffset))
//#define METH_codeCount(M) (getUInt32(METH_codePtr(M)))
#define METH_afterCodePtr(M) ((METH_accessFlags(M) & ACCESS_HAS_CODE) ? METH_codePtr(M)+METH_codeCount(M)+8:METH_codePtr(M))
#define METH_pooledNumHandlers(M) (!METH_hasHandlerThrows(M) ? 0 :getUInt16((METH_afterCodePtr(M))))
#define METH_pooledThrows(M) (!METH_hasHandlerThrows(M) ? 0 : (METH_afterCodePtr(M)+8+4*METH_pooledNumHandlers(M)))
#define METH_class(M) (!METH_isPooled(M) ? (M)->myClass : ResolveClass(METH_pooled(M)->pooledClass))
#define METH_nativeFuncPointer(M) (!METH_isPooled(M) ? &((M)->code.nativeF) : &PooledNatives[(METH_pooled(M)->nativeIndex)])

#define METH_extra(M) ((M)->extra)

#define METH_lineNumbers(M) (METH_isPooled(M) ? 0 :(METH_extra(M) ? METH_extra(M)->lineNumbers : 0))
#define METH_numHandlers(M) (!METH_isPooled(M) ?(METH_extra(M) ? METH_extra(M)->numHandlers : 0) : METH_pooledNumHandlers(M))
#define METH_handlers(M) (METH_isPooled(M) ? (struct WExceptionHandlerStruct *)(METH_afterCodePtr(M)+2):METH_extra(M)->handlers)
#define METH_throws(M) (METH_isPooled(M) ? METH_pooledThrows(M) : METH_extra(M) ? METH_extra(M)->throwsEx : 0)
#define METH_codeAttr(M) (!(METH_accessFlags(M) & ACCESS_HAS_CODE) ? 0 : (METH_isPooled(M) ? METH_codePtr(M) : (M)->code.codePointer))

//==============================================================================
typedef union{
//==============================================================================
	//
	// For non-pooled classes.
	//
	uchar *codePointer; // Absolute pointer to code attribute.
	NativeFunc nativeF; // Absolute pointer to native function.
//==============================================================================
} Code;
//==============================================================================
//
//
#define ACCESS_POOLED 0x8000 // For classes, methods and fields.
#define ACCESS_HAS_CODE  0x4000 // For methods.
#define ACCESS_IS_64 0x4000 // For fields.
#define ACCESS_HAS_INITIALIZER 0x2000 // For fields.
//
// This should mirror the full pooled method struct.
//
//==============================================================================
struct WPooledMethodStruct{ // 16 bytes.
//==============================================================================
//............................................................................
	union{
		struct{
			uint16 nameIndex;
	//............................................................................
			uint16 descIndex;
		} indexes;
		char *methodName;
	};
	uint16 access;
//............................................................................
	uint16 nativeUsesJValue:   1; //Pooled classes cannot use JValue methods.
	uint16 methodNameLength:   7;
	uint16 numParams:   4; //15 Parameters max, limited by parameterIs64Bits.
	uint16 returnsValue:2;
	uint16 isInit:      1;
//............................................................................
	uint16 parameterIs64Bits;
//............................................................................
// The rest do not appear in the standard WClassMethodStruct
//............................................................................
	uint16 nativeIndex; //The index of the native method.
//............................................................................
	uint16 codeOffset; //The offset of the code attribute data.
//............................................................................
	uint16 pooledClass; //The class of the method.
//==============================================================================
};
//==============================================================================

typedef struct WPooledMethodStruct WPooledMethod;

//==============================================================================
struct WClassMethodStruct{
//==============================================================================
//............................................................................
	union{
		struct{
			uint16 nameIndex;
			uint16 descIndex;
		}indexes;
		char *methodName;
	};
//............................................................................
	uint16 access;
	uint16 nativeUsesJValue:   1; //Pooled classes cannot use JValue methods.
	uint16 methodNameLength:   7;
	uint16 numParams:   4; //16 Parameters max, limited by parameterIs64Bits.
	uint16 returnsValue:2;
	uint16 isInit:      1;
//............................................................................
	uint16 parameterIs64Bits;
//............................................................................
	struct WClassStruct *myClass;
//............................................................................
// These will not exist in pooled methods. The location of the extra data
// is determined in the code member.
//............................................................................
	union{
		Code code;
		//char *methodName;
	};
	union{
		WClassMethodExtra *extra;
		struct WClassMethodStruct *nextArrayMethod;
	};
	//
	// Exception handlers will be placed after throwsOffset or lineNumbersOffset.
	//
};

static Var ArrayToString(Var stack[]);
static Var ObjectGetClass(Var stack[]);
static Var ArrayFinalize(Var stack[]);
static Var ArrayEquals(Var stack[]);
static Var ObjectHashCode(Var stack[]);
static Var ObjectClone(Var stack[]);

WClassMethod *arrayMethods = NULL;

static WClassMethod *addArrayMethod(char *nameAndSignature,NativeFunc func,int numParams,int returnsValue)
{
	int i;
	WClassMethod *m = (WClassMethod *)mMalloc(sizeof(struct WClassMethodStruct));
	memset(m,0,sizeof(struct WClassMethodStruct));
	m->access = (ACCESS_PUBLIC|ACCESS_NATIVE);
	m->methodName = (char *)nameAndSignature;
	for (i = 0;nameAndSignature[i] != 0;i++)
			if (nameAndSignature[i] == '(') break;
	m->methodNameLength = i;
	m->code.nativeF = func;
	m->numParams = numParams;
	m->returnsValue = returnsValue;
	m->nextArrayMethod = arrayMethods;
	arrayMethods = m;
	return m;
}
static void setupArrayMethods()
{
	addArrayMethod("toString()Ljava/lang/String;",ArrayToString,0,1);
	addArrayMethod("getClass()Ljava/lang/Class;",ObjectGetClass,0,1);
	addArrayMethod("finalize()V",ArrayFinalize,0,0);
	addArrayMethod("equals(Ljava/lang/Object;)Z",ArrayEquals,1,1);
	addArrayMethod("hashCode()I",ObjectHashCode,0,1);
	addArrayMethod("clone()Ljava/lang/Object;",ObjectClone,0,1);
}

static int isArrayMethod(WClassMethod *m,UtfString name,UtfString signature)
{
	unsigned int sigLen = 0;
	if (name.len != m->methodNameLength) return 0;
	if (strncmp(m->methodName,name.str,name.len) != 0) return 0;
	sigLen = (unsigned int)strlen(m->methodName+name.len);
	if (signature.len != sigLen) return 0;
	return (strncmp(m->methodName+name.len,signature.str,sigLen) == 0);
}

static WClassMethod * getArrayMethod(UtfString name,UtfString desc)
{
	WClassMethod *m = arrayMethods;
	if (m == NULL) setupArrayMethods();
	m = arrayMethods;
	while (m != NULL){
		if (isArrayMethod(m,name,desc))
			return m;
		m = m->nextArrayMethod;
	}
	return NULL;
}

#define METH_accessFlags(m) (m->access)
#define METH_nameIndex(m) (m->indexes.nameIndex)
#define METH_descIndex(m) (m->indexes.descIndex)
#define METH_name(c,m) getUtfString(c,m->indexes.nameIndex)
#define METH_desc(c,m) getUtfString(c,m->indexes.descIndex)
#define METH_isPooled(m) ((m)->access & ACCESS_POOLED)
//#define METH_codeAttr(m) (!METH_isPooled(m) ? (m->code.codePointer) : METH_accessFlags(m) & ACCESS_HAS_CODE ?  (((uchar *)m)+METH_pooledExtra(m)->codeOffset) : 0)

#define METH_maxStack(m) getUInt16(METH_codeAttr(m))
#define METH_maxLocals(m) getUInt16(METH_codeAttr(m)+2)
#define METH_codeCount(m) getUInt32(METH_codeAttr(m)+4)
#define METH_code(m) (METH_codeAttr(m)+8)

#define METH_handler(m,IDX) (&(METH_handlers(m)[IDX]))



#define METH_isStatic(m) ((METH_accessFlags(m) & ACCESS_STATIC) != 0)
#define METH_isPublic(m) ((METH_accessFlags(m) & ACCESS_PUBLIC) != 0)
#define METH_isNative(m) ((METH_accessFlags(m) & ACCESS_NATIVE) != 0)



#define POOLED_redirect(wclass,idx) (wclass->constantLookup[idx])
#define POOLED_pointer(wclass,idx) (wclass->constantTable+((wclass->constantLookup[idx] & 0x3fff)*2))
#define POOLED_firstShort(PTR) getUInt16(PTR)
#define POOLED_secondShort(PTR) getUInt16(PTR+2)
#define POOLED_classNameIndex(wclass,idx) ((uint16)(POOLED_redirect(wclass,idx) & 0x7fff))
//#define POOLED_integer(wclass,idx) getInt32(POOLED_pointer(wclass,idx))
//#define POOLED_float(wclass,idx) getFloat32(POOLED_pointer(wclass,idx))

#define CONS_offset(wc, idx) wc->constantOffsets[idx - 1]
#define CONS_ptr(wc, idx) (WCLASS_isPooled(wc) ? (POOLED_pointer(wc,idx)-1):(wc->byteRep + CONS_offset(wc, idx)))
#define CONS_tag(wc, idx) CONS_ptr(wc, idx)[0]
#define CONS_integer(wc, idx) getInt32(&CONS_ptr(wc, idx)[1])
#define CONS_float(wc, idx) getFloat32(&CONS_ptr(wc, idx)[1])
#define CONS_stringIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_classIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_nameAndTypeIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[3])
#define CONS_nameIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_typeIndex(wc, idx) getUInt16(&CONS_ptr(wc, idx)[3])
#define CONS_wide(wc, idx, v1, v2) getInt64(&CONS_ptr(wc, idx)[1],v1,v2)


// The VM keeps an array of constant offsets for each constant in a class
// in the runtime class structure (see WClassStruct). For each constant,
// the offset is an offset from the start of the bytes defining the class.
// Depdending on whether SMALLMEM is defined, the offset is either a 16 or
// 32 bit quantity. So, if SMALLMEM is defined, the maximum offset is 2^16.
// However, we also keep a bit in the constant to determine whether the
// constant is an offset that is "bound" or not. So, the maximum value of
// an offset if SMALLMEM is defined (the small memory model) is 32767.
//
// This means under the small memory model, the biggest class constant
// pool we can have is 32K. Under the large memory model (SMALLMEM not
// defined) the maximum class constant pool size that we could have is
// 2^31 bytes. Using SMALLMEM can save quite a bit of memory since
// constant pools tend to be large.
//
// When a constant offset is "bound", instead of the offset being

// an offset into the constant pool, it is (with the exception of methods)

// a pointer offset from the start of the class heap to the actual data
// the constant refers to.
//
// For example, when a field constant is bound, it contains an offset
// from the start of the class heap to the actual WClassField * structure
// for the field. For class offsets, it is an offset to the WClass *
// structure. For method offsets, the offset is a virtual method number
// and class index. Only class, field and methods can be bound.
//
// A bound offset will only be bound if the offset of the actual structure
// in the class heap is within the range that can fit in the offset. For
// example, in a small memory model, if a WClassField * structure exists
// beyond 32K from the start of the class heap, its offset can't be bound.
// If that happens, the offset simply won't be bound and will retain
// an offset into the constant pool (known now as an "adaptive bind").
//
// Binding of constants (adaptive quickbind) will only be performed if
// QUICKBIND is defined. When an offset is bound, it's CONS_boundBit
// will be set to 1.

#ifdef SMALLMEM

typedef uint16 ConsOffset;
#define MAX_consOffset 0x7FFF
#define CONS_boundBit 0x8000
#define CONS_boundOffsetMask 0x7FFF

// 1 bit for bound bit, 7 bits for method, 8 bits for class index
#define MAX_boundMethodNum 127
#define MAX_boundClassIndex 255
#define CONS_boundMethodShift 8
#define CONS_boundClassMask 0xFF;

#else

typedef uint32 ConsOffset;
#define MAX_consOffset 0x7FFFFFFF
#define CONS_boundBit 0x80000000
#define CONS_boundOffsetMask 0x7FFFFFFF

// 1 bit for bound bit, 15 bits for method, 16 bits for class index
#define MAX_boundMethodNum 32767
#define MAX_boundClassIndex 65535
#define CONS_boundMethodShift 16
#define CONS_boundClassMask 0xFFFF;

#endif

typedef struct
	{
	uint16 classNum:6;
	uint16 methodNum:10;
	} VMapValue;

#define MAX_superClassNum 63
#define MAX_methodNum 1023

#ifdef QUICKBIND

typedef struct
	{
	VMapValue *mapValues; // maps virtual method number to class, virtual method index
	uint16 mapSize; // size of map = number of inherited methods
	uint16 numVirtualMethods; // number of new virtual methods in the class
	//uint16 numOverriddenMethods; // number of overridden methods in the class
	} VirtualMethodMap;

// search types for getMethodMapNum()
#define SEARCH_ALL 0
#define SEARCH_INHERITED 1
#define SEARCH_THISCLASS 2

// keep this a prime number for best distribution
#define OVERRIDE_HASH_SIZE 127

#endif

typedef struct sort_info *SortInfo;
typedef int (* CompareFunction)(SortInfo info,int one,int two,int *error);

struct sort_info {
	int32 *original, *source, *dest;
	int sourceLen;
	int descending;
	CompareFunction function;
	void *functionData;
};

//===================================================================
static int sort(SortInfo info);
//===================================================================

// NOTE: In the following structure, a constant offset can either be
// bound (by having boundBit set) in which case it is an offset into
// the classHeap directly or unbound in which case it is an offset into
// the byteRep of the class
typedef struct WClassStruct
	{
	uint16 access;
	struct WClassStruct **superClasses; // array of this classes superclasses
	uint16 numSuperClasses;
	uint16 classNameIndex;
	uchar *byteRep; // pointer to class representation in memory (bytes)
	uchar *attrib2; // pointer to area after constant pool (accessFlags)

	uint16 numConstants;
	union{
		ConsOffset *constantOffsets; // For non-pooled.
		uchar *constantTable; // For pooled.
	};
	union{
		WObject clazzLoader; // For non-pooled.
		uint16 *constantLookup; // For pooled.
	};
	uint16 numFields, initializedFields;
	WClassField *myFields;
	uint16 numMethods;
	WClassMethod *methods;
#ifdef QUICKBIND
	VirtualMethodMap vMethodMap;
#endif
	uint16 numVars; // computed number of object variables
	uint16 pooledIndex: 10;
	uint16 isThrowable : 1;
	uint16 hasFinalizer: 1;
	uint16 oddOffset: 1;
	uint16 isSystemClass: 1;

	ObjDestroyFunc objDestroyFunc;
	struct WClassStruct *nextClass; // next class in hash table linked list
	//uint16 sourceFileIndex;
	uchar *classAttributes;
	uchar *utfPool;
#ifdef MAKING_POOL
	uint32 sectionOne, sectionTwo, sectionThree;
#endif
	uchar *interfaceCache;
	struct WClassStruct *lastCast;
	} WClass;

//

#define WCLASS_accessFlags(wc) (wc->access)
#define WCLASS_isPooled(wc) (WCLASS_accessFlags(wc) & ACCESS_POOLED)

#define WCLASS_loader(wc) (WCLASS_isPooled(wc) ? 0 : wc->clazzLoader)
#define WCLASS_thisClass(wc) getUInt16(&wc->attrib2[2])
#define WCLASS_superClass(wc) getUInt16(&wc->attrib2[4])
#define WCLASS_numInterfaces(wc) getUInt16(&wc->attrib2[6])
#define WCLASS_interfaceIndex(wc, idx) getUInt16(&wc->attrib2[8 + (idx * 2)])
#define WCLASS_objectSize(wc) ((wc->numVars + 1) * sizeof(Var))
#define WCLASS_isInterface(wc) ((WCLASS_accessFlags(wc) & ACCESS_INTERFACE) > 0)
#define WCLASS_className(wclass) getUtfString(wclass, wclass->classNameIndex)
#define WCLASS_methodPtr(WC,IDX) (WCLASS_isPooled(WC) ? (WClassMethod *)(&((WPooledMethod *)WC->methods)[IDX]): WC->methods+IDX)
#define WCLASS_fieldPtr(WC,IDX) (WCLASS_isPooled(WC) ? (WClassField *)(&((WPooledField *)WC->myFields)[IDX]): WC->myFields+IDX)
//
//
#ifndef USE_POOLED_UTF
#define CONS_utfLen(wc, idx) getUInt16(&CONS_ptr(wc, idx)[1])
#define CONS_utfStr(wc, idx) &CONS_ptr(wc, idx)[3]
#define CONS_getUtfStr(utfp,wc,idx) {(utfp)->str = CONS_utfStr(wc,idx), (utfp)->len = CONS_utfLen(wc,idx);}
#else
void CONS_getUtfStr(UtfString *s,WClass *wc,int idx)
{
	uchar *ptr = CONS_ptr(wc,idx);
	uchar tag = *ptr & CONSTANT_PooledUtf8_MASK;
	if (tag == 0) {
		s->len = getUInt16(ptr+1);
		s->str = (char *)ptr+3;
		return;
	}else{
		int offset = tag == CONSTANT_PooledUtf8_1 ?
			(*ptr & ~CONSTANT_PooledUtf8_MASK) << 8 | *(ptr+1):
			(*ptr & ~CONSTANT_PooledUtf8_MASK) << 24 | *(ptr+1) << 16 | *(ptr+2) << 8 | *(ptr+3);

		if (wc->utfPool == NULL) {
			s->str = "";
			s->len = 0;
		}else{
			uchar *pool = wc->utfPool+(offset*4);
			uint32 str = getUInt32(pool);
			s->str = (char *)wc->utfPool+str;
			pool += 4;
			s->len = getUInt32(pool)-str;
		}
	}
}
#endif

typedef union JValue {
	int z;
	signed char    b;
	uint16     c;
	int16   s;
	int32     i;
	int64    j;
	float   f;
	double  d;

	WObject  l;
} JValue;

// MLB these are used by DLLs.
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


	void (*holdRelease)(WObject,int doHold);
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

typedef int (*WabaDLLInit)(VMAccess);

typedef struct
	{
	uint16 errNum;
	char className[128];
	char methodName[40];
	char arg1[128];
	char arg2[128];
	} ErrorStatus;

typedef struct vmContext {
	WObject coroutine;
	WObject joining;
//Monitor..............................
	/*
	WObject waitingOnMonitor;
	int monitorWaitCount;
	int monitorHoldCount;
	*/
//.....................................

	Var *vmStack;
	uint32 vmStackSize; // in Var units
	uint32 vmStackPtr;
	Var *stack;
	Var *var;
	uchar *pc;
	uchar *oldpc;
	uint32 baseFramePtr;
	WClass *wclass;
	WClassMethod *method;

	int fullyReturned;
	int sleepFor;
	int runYet;
}* VmContext;
/*
typedef struct monitor_entry {
	VmContext holder;
	WObject monitor;
	int count;
	struct monitor_entry *next;
	int serving, waiting;
}*MonitorEntry;
*/
int VmFlags = 0;
#define VM_FLAGS 4
#define VM_FLAG_USING_CLASSES 0x1

#define VM_FLAG_IS_MOBILE 0x2

#define VM_FLAG_IS_SLOW_MACHINE 0x4
#define VM_FLAG_IS_MONOCHROME 0x8
#define VM_FLAG_NO_KEYBOARD 0x10
#define VM_FLAG_NO_MOUSE_POINTER 0x20
#define VM_FLAG_IS_APPLET 0x40
#define VM_FLAG_NO_WINDOWS 0x80
#define VM_FLAG_SIP_BUTTON_ON_SCREEN 0x100
#define VM_FLAG_NO_CR 0x200
#define VM_FLAG_ROTATE_SCREEN 0x400
#define VM_FLAG_IS_LOW_MEMORY 0x1000
#define VM_FLAG_COUNTER_ROTATE_SCREEN 0x2000
/**
This is a VM flag bit which indicates that the platform has no mouse OR touchscreen capabilities and
only keyboard navigation is possible - for example on many SmartPhone devices.
**/
#define VM_FLAG_NO_PEN 0x4000
/**
This is a VM flag bit which indicates that text input is not possible unless a native platform
input box is used. This is necessary for certain platforms such as MS SmartPhone devices.
**/
#define VM_FLAG_USE_NATIVE_TEXT_INPUT 0x8000
/**
This is a VM flag bit which indicates that the device has at least 2 general purpose
"Soft" keys, such as MS SmartPhone devices.
**/
#define VM_FLAG_HAS_SOFT_KEYS 0x10000

#define VM_FLAG_SIP_BUTTON_ALWAYS_SHOWN 0x20000

#define VM_FLAG_NO_GUI 0x40000

#ifdef WIN32

struct virtual_memory{
	uint32 pageSize;
	uint32 totalReservedSize;
	uint32 totalCommittedSize;
	unsigned char*	virtualMemory;
};

typedef struct virtual_memory *VMP;

static uint32 roundToPageBoundary(VMP virtualMemoryStruct, uint32 value)
{
	VMP vm = (VMP)virtualMemoryStruct;
	uint32 pageSize = vm->pageSize;
	return ((value+pageSize-1)/pageSize)*pageSize;
}
VMP vmCreate(uint32 reservedSize)
{
	SYSTEM_INFO si;
	uint32 gotSoFar = 0;
	uint32 subtract;
	VMP vm = (VMP)malloc(sizeof(struct virtual_memory));
	if (vm == NULL) return NULL;
	GetSystemInfo(&si);
	vm->pageSize = si.dwAllocationGranularity;
	reservedSize = roundToPageBoundary(vm,reservedSize);
	subtract = roundToPageBoundary(vm,1024*1024);
	while(reservedSize >= vm->pageSize){
		vm->virtualMemory = (unsigned char*)VirtualAlloc(NULL,reservedSize,MEM_RESERVE,PAGE_READWRITE);
		if (vm->virtualMemory != NULL)
			break;
		if (subtract > reservedSize || reservedSize-subtract < subtract)
			subtract = vm->pageSize;
		reservedSize -= subtract;
	}
	if (reservedSize < vm->pageSize){
		free(vm);
		return NULL;
	}
	vm->totalReservedSize = reservedSize;
	vm->totalCommittedSize = 0;
	return vm;
}
/**
This will free the virtual_memory_heap structure but will not deallocate any of
the virtual memory.
**/
void vmFree(VMP virtualMemoryStruct)
{
	free(virtualMemoryStruct);
}
/**
This will free the virtual_memory_heap structure and will deallocate the virtual memory.
**/
void vmDestroy(VMP virtualMemoryStruct)
{
	VirtualFree(virtualMemoryStruct->virtualMemory,0,MEM_RELEASE);
}
/**
Set the size of the committed memory. The method returns the new size of the committed
memory.
**/
uint32 vmCommitInTotal(VMP virtualMemoryStruct, uint32 newCommitSize)
{
	VMP vm = virtualMemoryStruct;
	uint32 willCommit = roundToPageBoundary(vm,newCommitSize);
	if (willCommit > vm->totalReservedSize) willCommit = vm->totalReservedSize;
	if (willCommit == vm->totalCommittedSize) return vm->totalCommittedSize;
	else if (willCommit <= vm->totalCommittedSize){
		uint32 toRelease = vm->totalCommittedSize-willCommit;
		if (VirtualFree(vm->virtualMemory+willCommit,toRelease,MEM_DECOMMIT)){
			vm->totalCommittedSize = willCommit;
		}
	}else{
		uint32 toCommit = willCommit-vm->totalCommittedSize;
		while(toCommit > 0){
			if (VirtualAlloc(vm->virtualMemory+vm->totalCommittedSize,toCommit,MEM_COMMIT,PAGE_READWRITE))
				break;
			toCommit -= vm->pageSize;
		}
		vm->totalCommittedSize += toCommit;
	}
	return vm->totalCommittedSize;
}

//
// This is used to provide expanding-only memory allocation using virtual memory.
//
struct growing_memory{
	//
	// If this is true then as one virtual memory area is used up, allocate another
	// one.
	//
	BOOL reallocIfNeeded;
	//
	// The number of bytes to reserve for each virtual memory area.
	//
	uint32 reserveSize;
	//
	// The number of bytes used in the current virtual memory area.
	//
	uint32 used;
	//
	uint32 totalUsed;

	VMP vm;
};

typedef struct growing_memory * GMP;

GMP gmCreate(uint32 reserveSize, BOOL reallocIfNeeded)
{
	GMP ret = (GMP)malloc(sizeof(struct growing_memory));
	if (ret == NULL) return NULL;
	ret->reallocIfNeeded = reallocIfNeeded;
	ret->reserveSize = reserveSize;
	ret->used = ret->totalUsed = 0;
	ret->vm = vmCreate(reserveSize);
	if (ret->vm == NULL){
		free(ret);
		return NULL;
	}
	return ret;
}

unsigned char* gmAlloc(GMP gmp, uint32 size)
{
	if (gmp->vm == NULL) return NULL;
	if (size == 0) size = 1;
	//
	// See if we need to commit more memory.
	//
	if (gmp->used+size <= gmp->vm->totalCommittedSize){
		unsigned char *ret = NULL;
success:
		ret = gmp->vm->virtualMemory+gmp->used;
		gmp->used += size;
		gmp->totalUsed += size;
		return ret;
	}
	//
	// Yes we must commit more.
	//
	if (gmp->used+size <= gmp->vm->totalReservedSize){
		//
		// Try to commit more.
		//
		vmCommitInTotal(gmp->vm,gmp->used+size);
		if (gmp->used+size <= gmp->vm->totalCommittedSize)
			goto success;
		//
		// If could not commit enough we cannot use this
		// virtual memory area.
		//
	}
	//
	// The previous virtual memory area could not be used.
	// So create a new area if necessary.
	//
	if (gmp->reallocIfNeeded == FALSE) return NULL;
	else{
		uint32 toReserve = size > gmp->reserveSize ? size : gmp->reserveSize;
		gmp->totalUsed += gmp->vm->totalCommittedSize-gmp->used;
		vmFree(gmp->vm);
		gmp->used = 0;
		gmp->vm = vmCreate(toReserve);
		if (gmp->vm == NULL) return NULL; // Could not reserve any.
		if (size > gmp->vm->totalReservedSize) return NULL; // Could not reserve enough.
		vmCommitInTotal(gmp->vm,size);
		if (size <= gmp->vm->totalCommittedSize)
			goto success;
		return NULL; // Could not commit enough.
	}
}
#endif

VmContext currentContext,activeContext;
int exitContext = 0;

static int getMoreStackSpace(VmContext vc,int maxStackSize)

{
	int newSize = (vc->vmStackSize+250)*sizeof(Var);
	Var *ns;

	if ((newSize > maxStackSize) && (maxStackSize > 0))
		newSize = maxStackSize;
	if (newSize/sizeof(Var) <= vc->vmStackSize) return 0;
	ns = (Var *)mMalloc(newSize);
	if (ns == NULL) return 0;
	memcpy(ns,vc->vmStack,sizeof(Var)*vc->vmStackSize);

	vc->vmStackSize = newSize/sizeof(Var);
	vc->stack = ns+(vc->stack-vc->vmStack);
	vc->var = ns+(vc->var-vc->vmStack);
	free(vc->vmStack);

	vc->vmStack = ns;
	return 1;
}

int checkArray = 0;


static WClass *mathClass, *throwableClass;
static WObject thrownException = 0, handlingException = 0;

int NormalEweIteration();
#ifdef WIN32
static WObject startApp(WCHAR *cmdLine,BOOL *alreadyRunning,int level);
#else
static WObject startApp(TCHAR *cmdLine,BOOL *alreadyRunning,int level);
#endif
void resolveConflict(TCHAR *msg)
{
#ifdef RESOLVE_CONFLICT
#if (defined(WIN32) || defined(WINCE))
	MessageBox(NULL,msg,TEXT("Resolve Conflict"),MB_OK|MB_SETFOREGROUND);
#else
#endif
#endif
}

static void MainLoop();
static int32 getTimeStamp();
static int32 timeDifference(int earlier,int later);


//
// private function prototypes
//
static int callSecurityMethod1(char *name,char *sig,WObject parameter);
static int callSecurityMethod0(char *name,char *sig);

static void enterExitEweMonitor(int isEnter);
static Var returnException(char *name,char *message);
static Var returnExceptionUtf(char *name,UtfString message);

static Var returnExError(uint16);
static void VmInit(uint32 vmStackSizeInBytes, uint32 nmStackSizeInBytes,
	uint32 classHeapSize, uint32 objectHeapSize);
static void VmError(uint16 errNum, WClass *iclass, UtfString *desc1, UtfString *desc2);
static void VmQuickError(uint16 errNum);
static WObject VmStartApp(char *className);
static void VmStopApp(WObject mainWinObj);
static void VmFree();

static WClass *findLoadedClass(UtfString className);
static WClass *getClass(UtfString className);
static WClass *tryGetOrLoadClass(UtfString className,WObject classLoader);
static WClass *tryGetClass(UtfString className);
static uchar *nativeLoadClass(UtfString className, uint32 *size,uchar **PooledUtf8);
//static void freeClass(WClass *wclass);
static uchar *loadClassConstant(WClass *wclass, uint16 idx, uchar *p);
static uchar *loadClassField(WClass *wclass, WClassField *field, uchar *p);
static void initializeField(WClass *wclass, WClassField *field, uint16 initializerIndex);
static Var constantToVar(WClass *wclass, uint16 idx);
static uchar *loadClassMethod(WClass *wclass, WClassMethod *method, uchar *p, int options,int *error);
#ifdef QUICKBIND
static int createVirtualMethodMap(WClass *wclass);
#endif
static UtfString createUtfString(char *buf);
static UtfString getUtfString(WClass *wclass, uint16 idx);
static WObject createObject(WClass *wclass);
static int32 arrayTypeSize(int32 type);

static int32 arraySize(int32 type, int32 len);
static WObject createArrayObject(int32 type, int32 len);
static WObject wAlloc(int bytes,unsigned char **memory);
static uint16 arrayType(char c);
static WObject createMultiArray(int32 ndim, char *desc, Var *sizes);
static WObject createStringFromUtf(UtfString s);
static WObject createString(char *buf);
static WObject createNewString(int length,uint16 **dataPtr,uint16 *text);
static WObject createStringFromJavaUtf8(int numberOfBytes,uint16 **unicode,char *utf8Text);
static WObject createStringFromUtf8(UtfString s);
static int sizeofJavaUtf8String(unsigned char *data,int numberOfBytes);
static int javaUtf8ToStringData(unsigned char *data,uint16 *text,int numberOfBytes);

static int javaUtf8ToSubString(WObject substring,unsigned char *bytes,int byteLength);
static WCHAR *utf8ToUnicode(UtfString source);
static WCHAR *utf8AsciiToUnicode(char *source,int length);

static int readInt(char *source,int numBytes);
static UtfString stringToUtf(WObject str, int flags);
static UtfString stringToUtf8(WObject str, struct byte_data *dest,int append);
static UtfString stringToTempUtf8(WObject str);
static int arrayRangeCheck(WObject array, int32 start, int32 count);
static Var copyArray(Var stack[]);
static WClassField *getField(WClass *wclass, UtfString name, UtfString desc);

static WClass *getClassByIndex(WClass *wclass, uint16 classIndex);
#ifdef QUICKBIND
static int compareMethodNameDesc(WClass *wclass, uint16 mapNum, UtfString name, UtfString desc);
static int32 getMethodMapNum(WClass *wclass, UtfString name, UtfString desc, int searchType);
static WClassMethod *getMethodByMapNum(WClass *wclass, WClass **vclass, uint16 mapNum);
static WClassMethod *getCachedInterface(WClass *requestingClass,uint16 interfaceIndex,WClass *targetClass, WClass **vclass);
#endif
static WClassMethod *getMethod(WClass *wclass, UtfString name, UtfString desc, WClass **vclass);
static WClassMethod *getAMethod(WClass *wclass,char *name,char *desc,WClass **vclass);
static int32 countMethodParams(UtfString desc,uint16 *is64BitsFlags);
static int compatible(WClass *wclass, WClass *target);
static int compatibleArray(WObject obj, UtfString arrayName);
static uint32 getUnusedMem();
static int initObjectHeap(uint32 heapSize);
static void freeObjectHeap();
//static void markActiveStack(Var *stack,uint32 ptr,Var *max);
static void markStack(Var *stack,uint32 ptr);
static void markObject(WObject obj);
static int sweep();
static void gc();
static void debugString(const char *);
static void debugUtf(UtfString str);
static WObject allocObject(int32 size,int isOutOfMemory);
static int isFree(WObject obj);
static Var *objectPtr(WObject obj);
static int pushObject(WObject obj);
static WObject popObject();
static WClassField *tryGetFieldNonUtf(WClass *wclass,char *name,char *desc);
static NativeFunc getNativeMethod(WClass *wclass, UtfString methodName, UtfString methodDesc);
static void setClassHooks(WClass *wclass);
static WObject suspendResumeCoroutine(WObject routine);

static void executeTopMethod(WClass *wclass, WClassMethod *method,
						  Var params[], uint32 numParams);
static void executeMethod(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams);
static int compareStrings(uint16 *one,int lenOne,uint16 *two,int lenTwo,int localeID,int options);
//MLB added.
static int invokeAMethod(WClassMethod *method,WObject obj,JValue parameters [],JValue *ret,int nonVirtual);
static void executeMethodRet(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams,Var *returnValue);
static void executeMethodRet2(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams,Var *returnValue,Var *returnValueHigh);
static void fullExecuteMethod(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams, Var *retValue, VmContext context);

static void asyncExecuteMethod(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams,Var *returnValue,Var *returnHigh);

static Var executeJValueNative(WClassMethod *method,Var params[]);
static void asyncCallBack(WObject destination,WObject data);
static WObject getSetException(int isGet,WObject ex);
static WObject throwException(char *exceptionClass,char *message);//,Var **stack);
static WObject throwExceptionUtf(char *exceptionClass,UtfString message);//,Var **stack);
static WObject throwExceptionError(uint16 err);//,Var **stack);
static Var *getFieldVarPointer(WClassField *field,WObject obj);
static int reportException();
static WClass *getSuperclass(WClass *clazz);

static Var _retValue[2];

#define methodReturnHigh _retValue[1]

static char
*NullPointerEx = "java/lang/NullPointerException",
*ArrayIndexEx = "java/lang/ArrayIndexOutOfBoundsException",
*StringIndexEx = "java/lang/StringIndexOutOfBoundsException",
*NumberFormatEx = "java/lang/NumberFormatException",
*IllegalArgEx = "java/lang/IllegalArgumentException",
*DivideByZeroEx = "java/lang/ArithmeticException",
*RuntimeEx = "java/lang/RuntimeException",
*OutOfMemoryEx = "java/lang/OutOfMemoryError",
*IOException = "ewe/io/IOException",
*UnsatisfiedLink = "java/lang/UnsatisfiedLinkError",
*SecurityEx = "java/lang/SecurityException"
;

static WObject securityManager;

static Var unsatisfied()
{
	return returnException(UnsatisfiedLink,NULL);
}
//
// global vars
//
static int vmInitialized = 0;

// virtual machine stack
static Var *vmStack;
//static Var *stack;
static uint32 vmStackSize; // in Var units
static uint32 vmStackPtr;
static uchar  *curPC;
static uint32 VmStackSizeInBytes;
static Var *mainVmStack;
static uint32 mainVmStackSize; // in Var units
static uint32 mainVmStackPtr;

// native method stack
static WObject *nmStack;
static uint32 nmStackSize; // in WObject units
static uint32 nmStackPtr;



// keep these prime numbers for best distribution
#ifdef SMALLMEM
#define CLASS_HASH_SIZE 63
#else
#define CLASS_HASH_SIZE 255
#endif


// class heap

/*
*/

static WClass *classHashList[CLASS_HASH_SIZE], *loadedClassList[CLASS_HASH_SIZE];

// error status
static ErrorStatus vmStatus;

// pointer to String class (for performance)
static WClass *objectClass;
static WClass *stringClass;
static WClass *weakReferenceClass;

static WClass *weakSetClass;
static WClass *outOfMemoryClass;
static WClass *soundClipClass;
static WClass *lockClass;
static int outOfMemorySize;
// Buffer classes.

static int utfEqualsString(UtfString one,char *two)
{
	if (one.len != strlen(two)) return 0;
	return strncmp(one.str,two,one.len) == 0;
}

void catutf(ByteData dest, UtfString str)
{
	if (dest->length == 0) {
		expandSpaceFor(dest, 1, 100, FALSE);
		dest->data[0] = 0;
		dest->length = 1;
	}
	expandSpaceFor(dest, dest->length+str.len, 100, TRUE);
	strncpy(dest->data+(dest->length-1),str.str,str.len);
	dest->length += str.len;
	dest->data[dest->length-1] = 0;
}

void catstr(ByteData dest, char *str)
{
	catutf(dest,createUtfString(str));
}

static int debugStop;

static WClass *findInstanceMethod(WClassMethod *method,WObject obj)
{
	WClass *got = METH_class(method);
	if (obj == 0 || METH_isStatic(method)) return got;
	if (WOBJ_class(obj) == got) return got;
	getMethod(WOBJ_class(obj),getUtfString(got,METH_nameIndex(method)),getUtfString(got,METH_descIndex(method)),&got);
	return got;
}


WObject createArray(char *type,int length)
{
	if (*type != 'L')
		return createArrayObject(arrayType(*type),length);
	else{
		WClass *cl;
		WObject array;
		UtfString utf = createUtfString(type);
		utf.str++; utf.len -= 2;

		cl = tryGetClass(utf);

		if (cl == NULL) return 0;
		array = createArrayObject(arrayType('L'),length);
		if (array != 0) WOBJ_arrayComponent(array) = cl;
		return array;
	}
}

WClass *getAClass(char *name) {return tryGetClass(createUtfString(name));}



static int readFully(WObject stream,WObject buff,int count,WClass *wclass,WClassMethod *wmethod, char **bytes)
{
	Var ret;
	Var pars[4];
	pars[0].obj = stream;
	pars[1].obj = buff;
	pars[2].intValue = 0;
	pars[3].intValue = count;

	while(pars[3].intValue > 0){

		executeMethodRet(wclass,wmethod,pars,4,&ret);
		if (thrownException) return -1;
		if (ret.intValue < 0) return ret.intValue;
		if (ret.intValue != 0){
			pars[3].intValue -= ret.intValue;
			pars[2].intValue += ret.intValue;
		}

	}
	if (bytes != NULL) *bytes = (char *)WOBJ_arrayStart(buff);
	return count;
}

static int reverseDouble = 0, reverseLong = 0;

static double vars2double(Var *v)
{
	uint32 i[2];
	if (reverseDouble) {
		i[1] = v->half64;
		i[0] = (v+1)->half64;
	}else{
		i[0] = v->half64;
		i[1] = (v+1)->half64;
	}
	return *((double*)i);
}
static void double2vars(double d,Var *v)
{
	uint32 i[2];
	*((double*)i) = d;
	if (reverseDouble) {
		v->half64 = i[1];
		(v+1)->half64 = i[0];
	}else{
		v->half64 = i[0];
		(v+1)->half64 = i[1];
	}
}
static int64 vars2int64(Var *v)
{
	uint32 i[2];
	if (reverseLong) {
		i[1] = v->half64;
		i[0] = (v+1)->half64;
	}else{
		i[0] = v->half64;
		i[1] = (v+1)->half64;
	}
	return *((int64*)i);
}
static void int642vars(int64 d,Var *v)
{
	uint32 i[2];
	*((int64*)i) = d;
	if (reverseLong) {
		v->half64 = i[1];
		(v+1)->half64 = i[0];
	}else{
		v->half64 = i[0];
		(v+1)->half64 = i[1];
	}
}
static void varsToJValue(Var value,Var highValue,JValue *ret)
{
	if (ret == NULL) return;
	else{
		unsigned char *r = (unsigned char *)ret;
		memcpy(r,&value,4);
		memcpy(r+4,&highValue,4);
	}
}
static void jValueToVars(JValue value,Var *ret,Var *retHigh)
{
	unsigned char *j = (unsigned char *)&value;
	if (ret) memcpy(ret,j,4);
	if (retHigh) memcpy(retHigh,j+4,4);
}
static double getSetDoubleJValue(double value,JValue *jv,int isGet)
{
	Var p[2];
	if (isGet){
		jValueToVars(*jv,p,p+1);
		return vars2double(p);
	}else{
		double2vars(value,p);
		varsToJValue(*p,*(p+1),jv);
		return value;
	}
}

static JValue *varToJValue(WClassMethod *method,Var *pars,JValue *dest,uint32 *objOrClass)
{
	JValue *ret = dest, *d;
	Var *p = pars;
	unsigned int i, m;
	unsigned int num = method->numParams, is64 = method->parameterIs64Bits;
	if (ret == NULL) ret = (JValue *)mMalloc(method->numParams*sizeof(JValue));
	d = ret;
	if (!METH_isStatic(method)){
		*objOrClass = (uint32)p->obj;
		p++;
	}else{
		*objOrClass = (uint32)METH_class(method);
	}
	for (i = 0, m = 1; i<num; i++, p++, d++, m <<= 1){
		if (is64 & m){
			varsToJValue(*p,*(p+1),d);
			i++; p++;
		}else{
			Var v;
			v.intValue = 0;
			varsToJValue(*p,v,d);
		}
	}
	return ret;
}
static Var *jValueToVar(WClassMethod *method,WObject obj,JValue *pars,Var *dest)
{
	Var *ret = dest, *d;
	JValue *p;
	unsigned int i, m;
	unsigned int num = method->numParams, is64 = method->parameterIs64Bits;
	if (ret == NULL) ret = (Var *)mMalloc(sizeof(Var)*(num+(obj != 0 ? 1 : 0)));
	d = ret;
	if (obj != 0) {
		d->obj = obj;
		d++;
	}

	for (i = 0, m = 1, p = pars; i<num; i++, p++, d++, m <<= 1){

		if (is64 & m) {
			memcpy(d,p,8);
			i++;
			d++;
		}else
			memcpy(d,p,4);
	}
	return ret;
}

static void copy64(Var *src, Var *dest)
{
//   int64 i;
   *dest = *src;
   *(dest+1) = *(src+1);
//   i = vars2int64(dest);
}

Var returnDouble(double value)
{
	double2vars(value,_retValue);
	return _retValue[0];
}
Var returnLong(int64 value)
{
	int642vars(value,_retValue);
	return _retValue[0];
}

Var *getFieldVarPointer(WClassField *field,WObject obj)
{
	if (FIELD_isStatic(field)) return GetStaticVarPointer(field);
	else return obj == 0 ? NULL : objectPtr(obj)+GetVarOffset(field)+1;
}
static void holdObject(WObject obj);
static void releaseObject(WObject obj);
static void setHandleFlags(WObject handle,int onValues,int offValues);
static void setHandleValue(WObject handle,WObject returnValue);
static void setHandle(WObject handle,int newFlags,WObject returnValue);
#define Changed  0x80000000
#define Stopped  0x40000000
#define Success  0x20000000
#define Failure  0x10000000
#define Running  0x08000000
#define Aborted  0x04000000
#define Succeeded  Success|Stopped
#define Failed  Failure|Stopped
//
// This section provides support for performing an operation in the background
// and updating a Handle to reflect the operation.
//

typedef void (* ThreadStartFunction)(void *parameter);
struct threadFunctionData {

	WObject handle;
	Var *parameters;
	int numParameters;
	ThreadStartFunction func;
	int flags;
	void *data;

};

//
// This method must cause a new thread to be created, which must then invoke runThread(data).
//
extern void NativeStartNewThread(struct threadFunctionData *data);

static WClass *handleClass = NULL;
static WClassMethod *handleConstructor = NULL;
static WClassMethod *setMethod = NULL;

#define THREAD_CREATE_HANDLE 0x1
#define THREAD_ONLY_IF_IN_COROUTINE 0x2

struct threadFunctionData *setupThread(Var *parameters,int numParameters,WObject handle,ThreadStartFunction func,int flags)
{
	struct threadFunctionData *ret = (struct threadFunctionData *)mMalloc(sizeof(struct threadFunctionData));
	ret->data = NULL;
	ret->func = func;
	ret->flags = flags;
	ret->handle = handle;
	if (ret->handle == 0 && ((flags & THREAD_CREATE_HANDLE) != 0)){
		Var pars[1];
		if (handleClass == NULL) {
			handleClass = getClass(createUtfString("ewe/sys/Handle"));
			handleConstructor = getMethod(handleClass,createUtfString("<init>"),createUtfString("()V"),NULL);
			setMethod = getMethod(handleClass,createUtfString("setFlags"),createUtfString("(II)V"),NULL);
		}
		ret->handle = createObject(handleClass);
		pushObject(ret->handle);
		pars[0].obj = ret->handle;
		executeMethod(handleClass,handleConstructor,pars,1);
		popObject();
	}
	holdObject(ret->handle);
	ret->numParameters = numParameters;
	if (ret->numParameters != 0){
		int i;
		ret->parameters = (Var *)mMalloc(sizeof(Var) * numParameters);
		for (i = 0; i<numParameters; i++){
			holdObject(parameters[i].obj);

			ret->parameters[i] = parameters[i];
		}
	}else
		ret->parameters = NULL;
	return ret;
}

void cleanupThread(struct threadFunctionData *tfd)
{
	if (tfd == NULL) return;
	releaseObject(tfd->handle);
	if (tfd->parameters != NULL){
		int i;
		for (i = 0; i<tfd->numParameters; i++)
			releaseObject(tfd->parameters[i].obj);
	}
	free(tfd->parameters);
	if (tfd->data != NULL) free(tfd->data);
	free(tfd);
}

void runThread(struct threadFunctionData *tfd)
{
	tfd->func(tfd);

	if (tfd->handle != 0) setHandleFlags(tfd->handle,Stopped,Running);
	cleanupThread(tfd);
}

void startThread(struct threadFunctionData *tfd)
{
	if (((tfd->flags & THREAD_ONLY_IF_IN_COROUTINE) == 0) || (currentContext != NULL))
		NativeStartNewThread(tfd);
	else
		runThread(tfd);
}

static int vmInSystemQueue = TRUE;

#define setInQueue(VALUE,TEXT) vmInSystemQueue = VALUE
/*
static void setInQueue(int value,char *text)
{
	sprintf(sprintBuffer,"%d %s\n",value,text);
	debugString(sprintBuffer);
	vmInSystemQueue = value;

}
*/
static Var VmAmInSystemQueue(Var stack[])
{
	Var v;
	v.intValue = vmInSystemQueue;
	return v;

}

static Var returnVar(int value)
{
	Var v;
	v.intValue = value;
	return v;
}

/*
The following types be defined in the "nmXXX_a.c" file:

FILE_HANDLE, MEMORY_MAPPED_HANDLE

*/
//=======================================================
typedef struct {
//=======================================================
	FILE_HANDLE fileH;
	MEMORY_MAPPED_HANDLE mapH;
	int fromResource;
	int viewIsMapped;
	uint32 numberOfRecords;
	uchar *ptr;
	uchar *PooledUtf8;
//=======================================================
} MemFile;
//=======================================================


#define MAX_MEM_FILES 16
static MemFile memFiles[MAX_MEM_FILES];
static uint32 numMemFiles = 0;
static int memFileNotSupported = 0;

static struct byte_data recordData;

static char *getRecordAt(uint32 whichEweFile,uint32 index,uchar **data,uint32 *size)
{
	if (size) *size = 0;
	if (data) *data = NULL;
	if (whichEweFile >= numMemFiles) return NULL;
	else{
		MemFile *mf = memFiles+whichEweFile;
		if (index >= mf->numberOfRecords)
			return NULL;

		else{
			uchar *baseP = mf->ptr, *offP, *p;
			uint32 off, checkNameLen, nameLen;
			offP = baseP+8+(index*4);
			off = getUInt32(offP);
			p = baseP+off;
			checkNameLen = nameLen = getUInt16(p);
			p += 2;
			while (*(p+checkNameLen-1) == 0)
				checkNameLen--;
			expandSpaceFor(&recordData,checkNameLen+1,10,0);
			strncpy((char *)recordData.data,(char *)p,checkNameLen);
			recordData.data[checkNameLen] = 0;
			if (data) *data = p+nameLen;
			if (size) *size = getUInt32(offP+4)-off-nameLen-2;
			return recordData.data;
		}
	}
	return NULL;
}
static uchar *loadFromMemFile(MemFile *mf,char *path,uint32 pathLen,uint32 *size,uchar **PooledUtf8)
{
	uchar *baseP, *offP, *p;
	uint32 off, nextOff, top, bot, mid;
	uint32 nameLen, minLen, numRecs,checkNameLen;
	int cmp;

	if (PooledUtf8 != NULL) *PooledUtf8 = NULL;
	// look in memory mapped files
		{
		baseP = mf->ptr;
		numRecs = getUInt32(baseP + 4);
		if (numRecs == 0) return NULL;
		// NOTE: We do a binary search to find the class. So, a search
		// for N classes occurs in O(nlogn) time.
		top = 0;
		bot = numRecs;
		while (1)
			{
			mid = (bot + top) / 2;
			offP = baseP + 8 + (mid * 4);
			off = getUInt32(offP);
			p = baseP + off;
			checkNameLen = nameLen = getUInt16(p);
			p += 2;
			while (*(p+checkNameLen-1) == 0)
				checkNameLen--;
			if (pathLen > checkNameLen)
				minLen = checkNameLen;
			else
				minLen = pathLen;
			cmp = xstrncmp(path, (const char *)p, minLen);
			if (!cmp)
				{
				if (pathLen == checkNameLen)
					{
					if (size != NULL)

						{
						nextOff = getUInt32(offP + 4);
						*size = nextOff - off - nameLen - 2;
						}
					if (PooledUtf8 != NULL)
						*PooledUtf8 = mf->PooledUtf8;
					return p + nameLen;

					}
				if (pathLen > nameLen)
					cmp = 1;
				else
					cmp = -1;
				}
			if (mid == top)

				break; // not found
			if (cmp < 0)
				bot = mid;
			else
				top = mid;
			}
		}
	return NULL;
}

static uchar *loadFromMem(char *path, uint32 pathLen, uint32 *size,uchar **PooledUtf8)
{
	uint32 i;
	for (i = 0; i < numMemFiles; i++){
		uchar *got = loadFromMemFile(memFiles+i,path,pathLen,size,PooledUtf8);
		if (got != NULL) return got;
	}
	return NULL;
}

static uchar *loadFromMemString(WObject str,uint32 *size)
{
	UtfString path = stringToTempUtf8(str);

	return loadFromMem(path.str,path.len,size,NULL);
}


static void setupClassPool(uchar *pool,int size,int alloc);

static uchar *checkPooled(MemFile *memFile)
{
	uint32 size = 0;
	uchar *got = loadFromMemFile(memFile,"_ClassPool_",11,&size,NULL);
	if (got && ((uint32)memFile->ptr & 3)) {
#ifndef WINCE
		printf("Not 32-bit aligned!\n\r");
#else
		mMessageBox(NULL,L"Not 32-bit aligned!",L"Error",MB_OK);
#endif
	}
	memFile->numberOfRecords = getUInt32(memFile->ptr+4);
	if (got != NULL){
		setupClassPool(got,size,0);
		return NULL;
	}
	got = loadFromMemFile(memFile,"_UtfPool_",9,&size,NULL);
	//if (got != NULL && UtfPoolSize == 0)
	//	UtfPoolSize = size;
	return got;
}

static int64 getWindowHandle(WObject obj);
static void holdRelease(WObject obj,int doHold);
void externalThreadEnding();

void initializeVMAccess(VMAccess *vv)
{
	VMAccess va;
	va.objectPtr = &objectPtr;
	va.asyncCallBack = &asyncCallBack;
	va.asyncExecuteMethod = &asyncExecuteMethod;
	va.doubleToVar = double2vars;
	va.longToVar = int642vars;
	va.varToDouble = vars2double;

	va.varToLong = vars2int64;
	va.returnDouble = returnDouble;
	va.returnLong = returnLong;
	va.getClass = getAClass;
	va.createNewString = createNewString;
	va.createArray = createArray;
	va.pushObject = pushObject;
	va.popObject = popObject;
	va.sort = sort;
	va.compareStrings = compareStrings;
	va.getField = tryGetFieldNonUtf;
	va.holdRelease = holdRelease;
	va.getWindowHandle = getWindowHandle;
	va.suspendResumeCoroutine = suspendResumeCoroutine;
	va.getMethod = getAMethod;
	va.invokeMethod = invokeAMethod;
	va.createNewObject = createObject;
	va.getSetException = getSetException;
	va.isAssignableFrom = compatible;
	va.enterExitEweMonitor = enterExitEweMonitor;
	va.getSuperclass = getSuperclass;
	va.getSetDoubleJValue = getSetDoubleJValue;
	va.getFieldVarPointer = getFieldVarPointer;
	va.externalThreadEnding = externalThreadEnding;
	*vv = va;
}
//
// OS SPECIFIC AREA CONTAINING RUNNERS
//

static void debugStackTrace();
Var NullMethod(Var stack[]) {return returnVar(0);}


#if defined(PALMOS)
#include "nmpalm_b.c"
#elif defined(WIN32)
#include "nmwin32_b.c"
#elif defined(UNIX)
#include "nmunix_b.c"
#endif
static void wakeupVM();

#ifdef USE_PTHREADS
#include "mThread.cpp"

mLock eweLock;
#define LOCKTHREAD eweLock.lock();
#define UNLOCKTHREAD eweLock.unlock();
#define PULSEEVENT eweLock.notifyAll();
#define WAITEVENT(HOWLONG) eweLock.wait(HOWLONG);
void externalThreadEnding()
{
	mThread::currentThreadEnding();
}
#else
#ifndef LOCKTHREAD
#define LOCKTHREAD
#define UNLOCKTHREAD
#define PULSEEVENT
#define WAITEVENT(HOWLONG)
void externalThreadEnding(){}
#endif
#endif

#ifdef UNIX

void asyncExecuteMethod(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams,Var *returnValue,Var *returnHigh)

{
	//enterCriticalSection(&sendMessageSection);
	LOCKTHREAD
	executeMethodRet2(wclass,method,params,numParams,returnValue,returnHigh);
	UNLOCKTHREAD
	//leaveCriticalSection(&sendMessageSection);
}

static int invokeAMethod(WClassMethod *method,WObject obj,JValue parameters [],JValue *ret,int nonVirtual)
{
	WClass *clazz = METH_class(method);
	if (!nonVirtual) clazz = findInstanceMethod(method,obj);

	if (method == NULL) return 0;
	else{
		Var *p = jValueToVar(method,obj,parameters,NULL);
		int np = method->numParams;
		/*
		if (!METH_isStatic(method)){
			if (obj == 0) return 0;
			p = invokePars;
			if (np > 24) p = malloc(sizeof(Var)*(np+1));
			p->obj = obj;
			memcpy(p+1,params,sizeof(Var)*np);
		}
		*/
		Var retValue, retHigh;
		asyncExecuteMethod(clazz,method,p,np+(METH_isStatic(method) ? 0 : 1),&retValue,&retHigh);
		varsToJValue(retValue,retHigh,ret);
		//if (p != invokePars && p != params)
		free(p);
		return 1;
	}
}


//This is to be called by a separate thread.

static WObject getHandleValue(WObject handle)
{
	WObject ret;
	if (handle == 0) return 0;
	LOCKTHREAD
	ret = objectPtr(handle)[2].obj;
	UNLOCKTHREAD

	return ret;
}
//This is to be called by a separate thread.
static void setHandleFlags(WObject handle,int onValues,int offValues)
{
	Var pars[3];
	pars[0].obj = handle;
	pars[1].intValue = onValues;
	pars[2].intValue = offValues;
	LOCKTHREAD
	if (handleClass == NULL) {
		handleClass = getClass(createUtfString("ewe/sys/Handle"));
		handleConstructor = getMethod(handleClass,createUtfString("<init>"),createUtfString("()V"),NULL);
		setMethod = getMethod(handleClass,createUtfString("setFlags"),createUtfString("(II)V"),NULL);
	}
	if (handle != 0) executeTopMethod(handleClass,setMethod,pars,3);
	UNLOCKTHREAD
}
static void setHandleValue(WObject handle,WObject returnValue)
{
	if (handle == 0) return;
	LOCKTHREAD
	objectPtr(handle)[2].obj = returnValue;
	UNLOCKTHREAD
}
static void setHandle(WObject handle,int value,WObject returnValue)

{
	LOCKTHREAD
	setHandleValue(handle,returnValue);
	setHandleFlags(handle,value,~value);
	UNLOCKTHREAD
}

#endif

//
// public functions
//

/*

 "I have three treasures that I keep and hold:
  one is mercy,
  the second is frugality,
  the third is not presuming to be at the head of the world.
  By reason of mercy, one can be brave.
  By reason of frugality, one can be broad.
  By not presuming to be at the head of the world,
  one can make your potential last."
 */


static BOOL initClassPartHeap(uint32 reserveSize);

static void VmInit(uint32 vmStackSizeInBytes, uint32 nmStackSizeInBytes,
	uint32 _classHeapSize, uint32 _objectHeapSize)
	{
	int status;
	uint32 i;
#ifdef PALMOS
	char *extra;
#endif
#ifdef SANITYCHECK
	static uchar floatTest[] = { 64, 160, 0, 0 };
	static uchar intTest1[] = { 0, 0, 255, 255 };
	static uchar intTest2[] = { 255, 255, 128, 8 };
	static uchar intTest3[] = { 255, 240, 189, 193 };
	static uchar intTest4[] = { 255, 254 };
	static uchar intTest5[] = { 39, 16 };
#endif

	if (vmInitialized)
		return;

	// NOTE: ordering is important here. The first thing we
	// need to do is initialize the global variables so if we
	// return not fully initialized, a VmFree() call will still
	// operate correctly. Also, its important not to statically

	// initialize them since VmInit() can be called after VmFree()
	// and if they were just statically intialized, they wouldn't
	// get reset.
	VmStackSizeInBytes = vmStackSizeInBytes;
	mainVmStack = vmStack = NULL;
	mainVmStackSize = vmStackSize = vmStackSizeInBytes / sizeof(Var);
	mainVmStackPtr = vmStackPtr = 0;
	nmStack = NULL;
	nmStackSize = nmStackSizeInBytes / sizeof(WObject);

	nmStackPtr = 0;

#ifdef MAKING_POOL
	_classHeapSize = 1000000;
#endif

	_classHeapSize = (_classHeapSize + 3) & ~3;
	/*
	classHeapSize = _classHeapSize;
	classHeapUsed = 0;
	*/
	for (i = 0; i < CLASS_HASH_SIZE; i++)
		loadedClassList[i] =
		classHashList[i] = NULL;

	xmemzero((uchar *)&vmStatus, sizeof(vmStatus));


#ifdef SANITYCHECK
#ifdef WINCE
	// NOTE: This is required by the Catalog class
	if (sizeof(CEOID) > sizeof(void *))
		VmQuickError(ERR_SanityCheckFailed);
#endif
	// sanity checks
	if (
		sizeof(int16) != 2 || sizeof(float32) != 4 || sizeof(int32) != 4 ||
		sizeof(VMapValue) != 2 || sizeof(Var) != 4 || getInt32(intTest1) != 65535 ||
		getInt32(intTest2) != -32760 || getInt32(intTest3) != -999999 ||

		getInt16(intTest4) != -2 ||     //This fails due to a bug in the CEF format?
		getInt16(intTest5) != 10000 ||
		getFloat32(floatTest) != 5.0f ||
		sizeof(double) != sizeof(Var)*2
		)

		{
		VmQuickError(ERR_SanityCheckFailed);
		//VmQuickError(ERR_Location);
		return;
		}


	// heap marking sanity check
	i = 100001;
	i |= 0x80000000;
	if (i & 0x80000000)
		i &= 0x7FFFFFFF;
	else
		i = -1;
	if (i != 100001)
		{
		VmQuickError(ERR_SanityCheckFailed);
		return;
		}
#endif

#ifdef PALMOS
	// NOTE: We allocate 2.5K before allocating anything else when running
	// under PalmOS. PalmOS has problems if you allocate the full amount of
	// memory in the dynamic heap since PalmOS also uses this memory when
	// you switch programs. If you don't leave a little extra, PalmOS
	// may crash and, at very least, won't let you switch programs.
	extra = (char *)mMalloc(2500);
	if (extra == NULL)
		{
		VmQuickError(ERR_CantAllocateMemory);
		return;
		}
#endif

	// allocate stacks and init
	mainVmStack = vmStack = (Var *)mMalloc(vmStackSizeInBytes);
	nmStack = (WObject *)mMalloc(nmStackSizeInBytes);
#ifndef SECURE_CLASS_HEAP
	//classHeap = (uchar *)mMalloc(classHeapSize+4);
#endif
	if (!initClassPartHeap(_classHeapSize) || vmStack == NULL || nmStack == NULL)
		{
		if (vmStack != NULL)
			xfree(vmStack);
		if (nmStack != NULL)
			xfree(nmStack);
#ifndef SECURE_CLASS_HEAP
		/*
		if (classHeap != NULL)
			xfree(classHeap);
			*/
#endif
#ifdef PALMOS
		xfree(extra);
#endif
		VmQuickError(ERR_CantAllocateMemory);
		return;
		}

	// zero out memory areas
	xmemzero((uchar *)vmStack, vmStackSizeInBytes);
	xmemzero((uchar *)nmStack, nmStackSizeInBytes);
	LOCK_CLASS_HEAP
	//xmemzero((uchar *)classHeap, classHeapSize+4);
	UNLOCK_CLASS_HEAP


	status = initObjectHeap(_objectHeapSize);
#ifdef PALMOS
	xfree(extra);
#endif
	if (status != 0)
		{
		VmQuickError(ERR_CantAllocateMemory);
		return;
		}
	vmInitialized = 1;
	}

// copies one or two UtfStrings (by concatination) into a character
// buffer (null terminated) suitable for output
static void printToBuf(char *buf, int maxbuf, UtfString *s1, UtfString *s2)
	{
	uint16 len, len2;

	len = 0;
	if (s1 != NULL && s1->str != NULL)
		{
		len = s1->len;
		if (len >= maxbuf)
			len = maxbuf - 1;
		xstrncpy(buf, s1->str, len);
		}
	if (s2 != NULL && s2->str != NULL)
		{
		len2 = s2->len;
		if (len2 + len >= maxbuf)
			len2 = maxbuf - len - 1;
		xstrncpy(&buf[len], s2->str, len2);
		len += len2;
		}
	buf[len] = 0;

	}

static void VmError(uint16 errNum, WClass *iclass, UtfString *desc1, UtfString *desc2)
	{

	WClass *wclass;
	WClassMethod *method;
	UtfString className, iclassName, methodName, methodDesc;

	// NOTE: Don't overwrite an existing error since it may be the
	// root cause of this error.
	//messageBox(TEXT("Error"),TEXT("ERROR"),0);
	if (vmStatus.errNum != 0)
		return;
	vmStatus.errNum = errNum;

	// get current class and method off stack
	if (vmStackPtr > 0)
		{
		wclass = (WClass *)vmStack[vmStackPtr - 1].refValue;

		method = (WClassMethod *)vmStack[vmStackPtr - 2].refValue;
		}
	else
		{
		wclass = 0;
		method = 0;
		}

	// output class and method name
	if (wclass)
		{
		className = getUtfString(wclass, wclass->classNameIndex);
		printToBuf(vmStatus.className, 128, &className, NULL);
		}
	if (method)
		{
		methodName = getUtfString(wclass, METH_nameIndex(method));
		methodDesc = getUtfString(wclass, METH_descIndex(method));

		printToBuf(vmStatus.methodName, 40, &methodName, &methodDesc);
		}

	// output additional error arguments (target class, desc, etc.)
	if (iclass)
		{
		iclassName = getUtfString(iclass, iclass->classNameIndex);
		printToBuf(vmStatus.arg1, 128, &iclassName, NULL);
		}
	printToBuf(vmStatus.arg2, 128, desc1, desc2);

#ifdef WIN32
#ifndef WINCE
	dumpStackTrace();
#endif
#endif
	}


static void VmQuickError(uint16 errNum)
	{
	VmError(errNum, NULL, NULL, NULL);
	}


static Var VmAppError(Var stack[])
{
	Var v;
	UtfString msg = stringToUtf(stack[0].obj,STU_USE_STATIC);
	v.intValue = 0;

	VmError(ERR_Application,NULL,&msg,NULL);
	//VmQuickError(ERR_Application);
	return v;
}

static void VmFree()
	{
	if (!vmInitialized)
		return;
	// NOTE: The object heap is freed first since it requires access to
	// the class heap to call object destroy methods
	// destroy methods
	freeObjectHeap();
	if (vmStack != NULL)
		xfree(vmStack);
	if (nmStack != NULL)
		xfree(nmStack);
#ifndef SECURE_CLASS_HEAP
	/*
	if (classHeap != NULL)
		xfree(classHeap);
	*/
#endif
	vmInitialized = 0;
	}

static int setupMethods(WClass *vclass)
{
#ifdef QUICKBIND
		WClass *ctrlclass;
		// cache method map numbers for commonly called methods
		postPaintMethodMapNum = getMethodMapNum(vclass, createUtfString("_doPaint"),
			createUtfString("(IIII)V"), SEARCH_ALL);
		postEventMethodMapNum = getMethodMapNum(vclass, createUtfString("_postEvent"),
			createUtfString("(IIIIII)V"), SEARCH_ALL);
		handleEventMethodMapNum = getMethodMapNum(vclass, createUtfString("handleNativeMessage"),
			createUtfString("(IIII)Z"), SEARCH_ALL);
		onTimerTickMethodMapNum = getMethodMapNum(vclass, createUtfString("_onTimerTick"),

			createUtfString("()V"), SEARCH_ALL);
		ctrlclass = getClass(createUtfString("ewe/ui/Control"));
		doPaintMethodMapNum = getMethodMapNum(ctrlclass, createUtfString("doPaint"),
			createUtfString("(Lewe/fx/Graphics;Lewe/fx/Rect;)V"), SEARCH_ALL);
		if (postPaintMethodMapNum == -1 || postEventMethodMapNum == -1 ||
			onTimerTickMethodMapNum == -1)// || doPaintMethodMapNum == -1)
			return 0;
#endif
		return 1;
}

static Var returnClass(WClass *cl);

#define message(MSG) messageBox(TEXT("Message"),TEXT(MSG),0)

static uint32 NumberOfClasses = 0, ConstantsSize = 0, FieldsSize = 0, VMapSize = 0;
#ifdef MAKING_POOL
static uint32 NumberOfFields = 0, NumberOfMethods = 0, NumberOfConstants, MaxConstants;
static uint32 MaxFields = 0, MaxMethods = 0, MaxCodeCount = 0, MaxParams = 0;
static uint32 OriginalMethodSize = 0, NewMethodSize = 0, OriginalConstantSize = 0, NewConstantSize = 0;
#endif

static WObject VmStartApp(char *className)
	{
	WObject mainWinObj;
	WClass *wclass, *vclass, *mainWinClass;

	WClassMethod *method;
	UtfString fullName;
	Var params[2], v;
	int isForm = 0;
	unsigned int i;

	uint32 test[2];
	*((double *)test) = 1.0;
	reverseDouble = test[0] != 0;
	*((int64 *)test) = cINT64(1L);
	reverseLong = test[0] == 0;

	if (!vmInitialized)
		return 0;

	setupClassPool(NULL,0,1);

	objectClass = getClass(createUtfString("java/lang/Object"));
	if (objectClass == NULL){
		vmStatus.errNum = ERR_CantFindClasses;
		return 0;
	}
	//sprintf(sprintBuffer,"Got Object: %x",objectClass);
	//mMessageBox(NULL,asciiToTempUnicode(sprintBuffer),L"Debug",MB_OK);
	stringClass = getClass(createUtfString("java/lang/String"));
	weakReferenceClass = getClass(createUtfString("ewe/reflect/WeakReference"));
	weakSetClass = getClass(createUtfString("ewe/util/WeakSet"));
	outOfMemoryClass = getClass(createUtfString(OutOfMemoryEx));
	outOfMemorySize = WCLASS_objectSize(outOfMemoryClass);
	soundClipClass = getClass(createUtfString("ewe/fx/SoundClip"));
	//coroutineClass = getClass(createUtfString("ewe/sys/Coroutine"));
	//sleepMethod = getMethod(coroutineClass,createUtfString("sleep"),createUtfString("(I)V"),NULL);


#ifdef MAKING_POOL
	if(1){
		int poolAllClasses(WClass **hash,int numberOfClasses,int intoNew);
		int cs = 0, intoNew = 0, numClasses = 0;
		static char temp[256];
		for(cs=0;;cs++){
			char *name = getRecordAt(0,cs,NULL,NULL);
			if (name == NULL) break;
			if (strcmp("_UtfPool_",name) == 0) continue;
			else if (strcmp("_ClassPool_",name) == 0){
				debugString("This ewe file already has a ClassPool in it!");
				return 0;
			}else{
				int len = strlen(name);
				if (len > 6) {
					if (strcmp(name+len-6,".class")  == 0){
						name[len-6] = 0;
						if (strlen(name) > 9){
							if (strncmp(name,"ewe/lang/",9) == 0){
								strcpy(temp+1,name);
								strncpy(temp,"java",4);
								name = temp;
							}
						}
						if (!getClass(createUtfString(name)))
							return 0;
						numClasses++;
						continue;
					}
				}
				intoNew++;
			}
		}
		sprintf(sprintBuffer,"Classes: %d, Methods: %d, MaxMethod: %d, Fields: %d, MaxFields: %d, MaxCodeCount: %d, MaxParams: %d",
			NumberOfClasses,NumberOfMethods,MaxMethods,NumberOfFields,MaxFields,MaxCodeCount, MaxParams);
		debugString(sprintBuffer);
		cs = poolAllClasses(classHashList,numClasses,intoNew);
		exit(0);
		return 0;
	}
#endif

	if (stringClass == NULL)
		return 0;
	fullName = createUtfString(className);
	for (i = 0; i<fullName.len; i++)
		if (fullName.str[i] == '.') fullName.str[i] = '/';
	;
	mainWinClass = getClass(createUtfString("ewe/ui/mApp"));
	if (mainWinClass == NULL) goto exerror;
	if (!setupMethods(mainWinClass)) return 0;
	//
	wclass = getClass(fullName);
 	if (wclass == NULL) {
		goto exerror;
		//vmStatus.errNum = ERR_CantCreateAppClass;
		//return 0;
	}
	// see if its a mApp class
	if (!compatible(wclass, mainWinClass)){
		v = returnClass(wclass);
		pushObject(v.obj);
		mainWinObj = createObject(mainWinClass);
		pushObject(mainWinObj);
		method = getMethod(mainWinClass, createUtfString("<init>"), createUtfString("(Ljava/lang/Class;)V"), NULL);
		params[0].obj = mainWinObj;
		params[1].obj = v.obj;
		//message("Execute Method");
		executeMethod(mainWinClass,method,params,2);
		if (thrownException != 0){
exerror:
			if (!reportException()) return 0;
			vmStatus.errNum = ERR_CantCreateAppClass;
			return 0;

		}
		//message("Return");

		wclass = mainWinClass;
	}else{
		mainWinObj = createObject(wclass);
		if (mainWinObj == 0){
			vmStatus.errNum = ERR_CantCreateAppClass;

			return 0;
		}
		if (pushObject(mainWinObj) == -1)  // make sure it doesn't get GC'd
			return 0;
		params[0].obj = mainWinObj;
		// call MainWindow constructor
		method = getMethod(wclass, createUtfString("<init>"), createUtfString("()V"), NULL);
		if (method != NULL){
			executeMethod(wclass, method, params, 1);

			if (thrownException != 0) goto exerror;

		}else{
			vmStatus.errNum = ERR_CantCreateAppClass;
			return 0;
		}
	}
	// call onStart()
	method = getMethod(wclass, createUtfString("onStart"), createUtfString("()V"), &vclass);
	if (method != NULL){
		executeMethod(vclass, method, params, 1);
	// NOTE: main window object is pushed on native stack to prevent it being GC'd
		if (thrownException != 0) goto exerror;
	}
	vmStatus.errNum = 0;
	//message("Returning");
	return mainWinObj;
	}

/*
static WObject VmStartApp(char *className)
	{
	WObject mainWinObj;
	WClass *wclass, *vclass, *mainWinClass, *formClass;
	WClassMethod *method;
	UtfString fullName;
	Var params[2];
	int isForm = 0;
	unsigned int i;

	if (!vmInitialized)
		return 0;
	objectClass = getClass(createUtfString("java/lang/Object"));
	if (objectClass == NULL){
		vmStatus.errNum = ERR_CantFindClasses;

		return 0;
	}
	stringClass = getClass(createUtfString("java/lang/String"));
	weakReferenceClass = getClass(createUtfString("ewe/reflect/WeakReference"));
	weakSetClass = getClass(createUtfString("ewe/util/WeakSet"));
	outOfMemoryClass = getClass(createUtfString(OutOfMemoryEx));
	outOfMemorySize = WCLASS_objectSize(outOfMemoryClass);
	//coroutineClass = getClass(createUtfString("ewe/sys/Coroutine"));
	//sleepMethod = getMethod(coroutineClass,createUtfString("sleep"),createUtfString("(I)V"),NULL);

	if (stringClass == NULL)
		return 0;

	fullName = createUtfString(className);
	for (i = 0; i<fullName.len; i++)
		if (fullName.str[i] == '.') fullName.str[i] = '/';
	;
	mainWinClass = getClass(createUtfString("ewe/ui/mApp"));
	if (!setupMethods(mainWinClass)) return 0;
	wclass = getClass(fullName);
	//vmStatus.errNum = ERR_BadAppClass;
	if (wclass == NULL)
		return 0;
	// see if its a mApp class
	if (!compatible(wclass, mainWinClass)){
		formClass = getClass(createUtfString("java/lang/Object"));
		if (!compatible(wclass,formClass)){
			return 0;
		}else
			isForm = 1;
	}
	// create MainWindow object
	if (!isForm){
		mainWinObj = createObject(wclass);
		if (mainWinObj == 0){
			vmStatus.errNum = ERR_CantCreateAppClass;
			return 0;
		}
		if (pushObject(mainWinObj) == -1)  // make sure it doesn't get GC'd
			return 0;

		params[0].obj = mainWinObj;
		// call MainWindow constructor
		method = getMethod(wclass, createUtfString("<init>"), createUtfString("()V"), NULL);
		if (method != NULL)
			executeMethod(wclass, method, params, 1);
		else{
			vmStatus.errNum = ERR_CantCreateAppClass;
			return 0;
		}
	}else{ //isForm
		WObject form;
		mainWinObj = createObject(mainWinClass);
		method = getMethod(mainWinClass, createUtfString("<init>"), createUtfString("(Ljava/lang/Object;)V"), NULL);
		params[0].obj = mainWinObj;
		params[1].obj = 0;
		executeMethod(mainWinClass,method,params,2);

		form = createObject(wclass);
		method = getMethod(wclass,createUtfString("<init>"), createUtfString("()V"), NULL);
		params[0].obj = form;
		executeMethod(wclass,method,params,1);

		wclass = mainWinClass;
		method = getMethod(wclass, createUtfString("setRunObject"), createUtfString("(Ljava/lang/Object;)V"), NULL);
		params[0].obj = mainWinObj;
		params[1].obj = form;
		executeMethod(wclass,method,params,2);
	}
	// call onStart()
	method = getMethod(wclass, createUtfString("onStart"), createUtfString("()V"), &vclass);
	if (method != NULL)
		executeMethod(vclass, method, params, 1);
	// NOTE: main window object is pushed on native stack to prevent it being GC'd
	vmStatus.errNum = 0;
	return mainWinObj;

	}
*/

static void VmStopApp(WObject mainWinObj)
	{
	WClass *wclass, *vclass;
	WClassMethod *method;
	Var params[1];

	if (!vmInitialized || mainWinObj == 0)
		return;
	// call onExit()
	wclass = WOBJ_class(mainWinObj);
	method = getMethod(wclass, createUtfString("onExit"), createUtfString("()V"), &vclass);
	if (method != NULL)
		{
		params[0].obj = mainWinObj;

		executeMethod(vclass, method, params, 1);
		}
	popObject();
	}



//
// Class Loader
//

static uint32 genHashCode(UtfString name)
	{
	uint32 value, i;
	int len = name.len;


	value = 0;
	for (i = 0; i < name.len; i++){
	// Change ewe to waba.

	//
		if (name.str[i] == 'e')
			if (i < name.len-2)
				if (name.str[i+1] == 'w')
					if (name.str[i+2] == 'e'){
						value += 'w'+'a'+'b'+'a';
						i+= 2;
						len++;
						continue;
					}
		value += name.str[i];
	}
	value = (value << 6) + len;
	return value;
	}

#ifdef WIN32

static GMP classPartStorage;

static BOOL initClassPartHeap(uint32 reserveSize)
{
	return (classPartStorage = gmCreate(reserveSize,TRUE)) != NULL;
}
static uchar *allocClassPart(uint32 size)
{
	// align to 4 byte boundry
	uchar *ret;
	size = (size + 3) & ~3;
	ret = (uchar*) gmAlloc(classPartStorage,size);
	xmemzero(ret,size);
	return ret;
}
#else
static uchar *classHeap;
static uint32 classHeapSize;
static uint32 classHeapUsed, totalClassHeapUsed;

static BOOL initClassPartHeap(uint32 reserveSize)
{
	return (/* classHeapOriginal = */ classHeap =
	    (uchar *)mMalloc(classHeapSize+4));
}

int getMoreClassHeap(uint32 sizeNeeded)
{
	totalClassHeapUsed += classHeapSize;
	if (sizeNeeded > classHeapSize) classHeapSize = sizeNeeded;
	classHeap = (uchar *)mMalloc(classHeapSize+4);
	classHeapUsed = 0;
	if (classHeap != NULL)
		xmemzero(classHeap,classHeapSize+4);
	else{
#ifdef WINCE
		MessageBox(NULL,L"mMalloc Failed",L"Failed",MB_SETFOREGROUND);
#endif
	}
	return (classHeap != NULL);
}

//The class heap actual size is classHeapSize+4;
static uchar *allocClassPart(uint32 size)
{
	uchar *p, *ch;
	if (classHeap == NULL) return NULL;
	// align to 4 byte boundry
	size = (size + 3) & ~3;
	while (classHeapUsed + size > classHeapSize){
		if (!getMoreClassHeap(size)){

			throwException(OutOfMemoryEx,"Out of class memory");
			//VmQuickError(ERR_OutOfClassMem);
			return NULL;
		}
	}
	ch = (uchar *)(((int32)classHeap + 3) & ~3);
	p = &ch[classHeapUsed];
	classHeapUsed += size;
	return p;
}
#endif

static int convertJavaName(UtfString *className,int *isMath,int *isThrowable)
{
	int i;
	if (className->len > 10){
		char *s = className->str, *sp = "java/lang/";

		for (i = 0; i<10; i++) if (*s++ != *sp++) break;
		if (i == 10){

			char *nn = (char *)mMalloc(className->len-1);
			if (nn == NULL){
				throwException(OutOfMemoryEx,NULL);
				return 0;
			}
			strncpy(nn,className->str+1,className->len-1);
			strncpy(nn,"ewe",3);
			if (className->len == 19){
				if (!strncmp("Throwable",className->str+10,9))

					if (isThrowable) *isThrowable = 1;
			}else if (className->len == 14){
				if (!strncmp("Math",className->str+10,4))
					if (isMath) *isMath = 1;
			}
			//className->str = nn; className->len--;

		}
	}
	return 1;
}
//"SourceFile"
static uchar *getAttribute(WClass *wclass,uchar *attributes,char *name,int nameLength,uint32 *attrSize)
{
	uchar *p = attributes;

	uint16 attrCount = getUInt16(p), i;
	if (nameLength < 1) nameLength = strlen(name);
	p += 2;
	for (i = 0; i < attrCount; i++)
		{
		uint16 nameIndex = getUInt16(p);
		uint32 size = getUInt32(p+2);
		UtfString attrName = getUtfString(wclass, nameIndex);
		p += 6;
		if ((int)attrName.len == nameLength && !strncmp(attrName.str,name,nameLength)){
			if (attrSize) *attrSize = size;
			return p;
		}
		p += size;
	}
	return NULL;
}
static uint16 getAttributeIndex(WClass *wclass,uchar *attributes,char *name,int nameLength)
{
	uchar *p = getAttribute(wclass,attributes,name,nameLength,NULL);
	if (p == NULL) return 0;
	return getUInt16(p);
}
static uchar *mallocZ(uint32 size)
{
	void *ret = mMalloc(size);
	if (ret != NULL) memset(ret,0,size);
	return (uchar *)ret;
}

#ifdef MAKING_POOL
static uint16 nativeIndex = 0, staticFieldIndex = 0;
static WClassField *getFieldByIndex(WClass *,uint16);

static struct byte_data poolErrors;
static BOOL poolFailed = FALSE;

uchar *poolClass(uchar *start, WClass *wclass, uint16 index)
{
	ByteData errors = &poolErrors;
	uchar *p = start, *ptr;
	WPooledMethod *methods;
	uint16 i, j, *p2;
	uint16 *ctable;
	uint32 csize = 0;
	int size;
	//BOOL check = FALSE;

	UtfString className = WCLASS_className(wclass);
	/*
	if (className.len == 33 && strncmp("ewe/database/RecordDatabaseObject",className.str,33) == 0)
		check = TRUE;
		*/
	if (ClassPoolUtf == 0) ClassPoolUtf = wclass->utfPool;
	putUInt32(p,PooledClassMarker);
	p += 4;
	//Copy up to constants.
	memcpy(p,wclass->byteRep+4,wclass->sectionOne-4);
	p += wclass->sectionOne-4;
	//
	// Do constants.
	//
	putUInt16(p,index);
	p += 2;
	putUInt16(p,wclass->classNameIndex);
	p += 2;
	putUInt16(p,wclass->numConstants);
	p += 2;
	ctable = (uint16 *)p;
	p += wclass->numConstants*2;
	ptr = p; // The constant size in bytes will go here.
	p += 4;
	//
	j = 0;
	for (i = 0; i<wclass->numConstants; i++){
		if (i == 0) {
			*ctable++ = 0;
		}else{
			uchar tag = CONS_tag(wclass,i);
			uchar *cp = CONS_ptr(wclass,i);
			if ((tag & CONSTANT_PooledUtf8_MASK) == CONSTANT_PooledUtf8_1){
				*ctable++ = (uint16)(((*cp << 8) | *(cp+1)) & 0x3fff);
			}else if ((tag & CONSTANT_PooledUtf8_MASK) == CONSTANT_PooledUtf8_2){
				//
				// Although the tag is capable of handling bigger numbers,
				// the offset must be limited to 16 bits.
				//
				*ctable++ = (uint16)(((*(cp+2) << 8) | *(cp+3)) & 0xffff);
			}else{
				cp++;
				*ctable++ = j;
				switch(tag){

				case CONSTANT_String:
					*(ctable-1) |= (uint16)0xc000;
					memcpy(p,cp,2);
					p += 2; j += 1;
					break;

				case CONSTANT_Integer:
				case CONSTANT_Float:
					if (tag == CONSTANT_Integer) *(ctable-1) |= (uint16)0x8000;
					else *(ctable-1) |= (uint16)0x4000;
				case CONSTANT_InterfaceMethodref:
				case CONSTANT_NameAndType:
					memcpy(p,cp,4);
					p += 4; j += 2;
					break;

				case CONSTANT_Long:
				case CONSTANT_Double:
					memcpy(p,cp,8);
					p += 8; j += 4;
					i++;
					*ctable++ = 0;
					break;

				case CONSTANT_Class:
					{
						WClass *got = getClassByIndex(wclass,i);
						*(ctable-1) = got == NULL ? (CONS_nameIndex(wclass,i)|(uint16)0x8000) : got->pooledIndex;
						break;
					}
				case CONSTANT_Fieldref:
					{

						WClassField *got = getFieldByIndex(wclass,i);
						if (got == NULL)
							return 0; // Should never happen!
						else{
							WClass *cl = got->myClass;
							uint16 myIdx = (uint16)(got-cl->myFields);
							*(ctable-1) = cl->pooledIndex | (myIdx << 10);
						}
						break;
					}
				case CONSTANT_Methodref:
					{
						uint16 classIndex = CONS_classIndex(wclass, i);
						WClass *iclass = getClassByIndex(wclass, classIndex);
						if (iclass == NULL)
							return NULL; // Should never happen!
						else{
							uint16 nameAndTypeIndex = CONS_nameAndTypeIndex(wclass, i);
							//UtfString className = WCLASS_className(iclass);
							UtfString methodClassName = WCLASS_className(iclass);
							UtfString methodName = getUtfString(wclass, CONS_nameIndex(wclass, nameAndTypeIndex));
							UtfString methodDesc = getUtfString(wclass, CONS_typeIndex(wclass, nameAndTypeIndex));
							int32 mmn = getMethodMapNum(iclass,methodName,methodDesc,SEARCH_ALL);
							if (mmn < 0){
								poolFailed = TRUE;
								catutf(errors,className);
								catstr(errors," - bad method: ");
								catutf(errors,methodClassName);
								catstr(errors,".");
								catutf(errors,methodName);
								catutf(errors,methodDesc);
								catstr(errors, "\r\n");
								//return 0; // Should never happen!
							}
							putUInt16(p,classIndex);
							putUInt16(p+2,mmn);
							p += 4;
							j += 2;
						}
						break;
					}
				}
			}
		}
	}
	csize = (p-ptr)-4;
	if (csize > 32768)
		csize = 0;
	putUInt32(ptr,csize); // The size of constants in bytes.
	//
	//Copy attribs.
	//
	memcpy(p,wclass->attrib2,wclass->sectionTwo);
	p += wclass->sectionTwo;
	memcpy(p,wclass->classAttributes,wclass->sectionThree);
	p += wclass->sectionThree;
	//
	// Do fields, 16 bit aligned.
	//
	while ((uint32)p & 1) p++;
	size = wclass->numFields;
	putUInt16(p,size);
	p += 2;
	p2 = (uint16 *)(p+wclass->numFields*sizeof(WPooledField));
	for (i = 0; i<wclass->numFields; i++){
		WClassField *f = WCLASS_fieldPtr(wclass,i);
		f->access |= ACCESS_POOLED;
		if (FIELD_isStatic(f)){
			f->staticFieldIndex = staticFieldIndex++;

			if (FIELD_is64(f)) staticFieldIndex++;
			if (FIELD_hasInitializer(f)) *p2++ = f->initializerIndex;
		}
		//((WPooledField *)f)->pooledClass = index;
		memcpy(p,f,sizeof(WPooledField));
		p += sizeof(WPooledField);
	}
	p = (uchar *)p2;
	//
	//
	// Now copy the VirtualTable, 32 bit aligned.
	//
	while((uint32)p & 3) p++;
	memcpy(p,&wclass->vMethodMap,sizeof(wclass->vMethodMap));
	p += sizeof(wclass->vMethodMap);
	size = wclass->vMethodMap.mapSize*sizeof(VMapValue);
	if (size){
		memcpy(p,wclass->vMethodMap.mapValues,size);
		p += size;
	}
	//
	// Now copy the pooled method structure, 32 bit aligned.
	//
	size = wclass->numMethods;
	putUInt16(p,size);
	p += 2;
	methods = (WPooledMethod *)p;
	p += wclass->numMethods*sizeof(WPooledMethod);
	for (i = 0; i<wclass->numMethods; i++){
		WClassMethod *m = &wclass->methods[i];
		WPooledMethod *pooled = methods+i;
		memcpy(pooled,m,sizeof(WPooledMethod));
		pooled->access |= ACCESS_POOLED;
		pooled->pooledClass = index;

		pooled->codeOffset = 0;

		if (m->access & ACCESS_NATIVE)
			pooled->nativeIndex = nativeIndex++;
		else
			pooled->nativeIndex = 0;

		if ((m->access & ACCESS_HAS_CODE) || METH_numHandlers(m) || METH_throws(m)){
			uint16 nh, nt;

			uint32 off = (uint32)p-(uint32)pooled;
			if (off > 65535)

				return NULL;
			pooled->codeOffset = off;
			//
			// If there is code, copy it.
			//
			if (m->access & ACCESS_HAS_CODE){
				uint32 codeSize = METH_codeCount(m);
				memcpy(p,m->code.codePointer,4); //MaxLocals and MaxStack
				p += 4;
				memcpy(p+4,m->code.codePointer+8,codeSize);
				codeSize = (codeSize+1) & ~1; //16 bit align it.
				putUInt32(p,codeSize);
				p += codeSize+4;
			}
			//
			// Now put the number of handlers and the handlers, even if 0.
			//
			nh = METH_numHandlers(m);
			putUInt16(p,nh);
			p += 2;
			for (j = 0; j<nh; j++){
				memcpy(p,METH_handler(m,j),sizeof(WExceptionHandler));
				p += sizeof(WExceptionHandler);
			}
			//
			// Now put the number of throws and the throws, even if 0.
			//
			ptr = METH_throws(m);
			nt = ptr == 0 ? 0 : getUInt16(ptr);
			putUInt16(p,nt);
			p += 2;
			if (nt) memcpy(p,ptr+2,nt*2);
			p += nt*2;
		}
	}
	//
	//
	// Now copy the extra method info - handlers, code, etc.
	// 32 bit align it first though.
	//
	while((uint32)p & 3) p++;

	//Make sure we return a 32 bit aligned pointer.
	while ((uint32)p & 3) p++;
	return p;
}

int sizeUsed(uint32 dest,char *name,uint32 size){
	int willBe = dest+2+strlen(name);
	int pad = ((willBe + 3) & ~3)-willBe;
	return 2+strlen(name)+pad+size;
}

int putInEwe(uint32 dest,FILE *out,uchar *data,char *name,uint32 size)
{
	char buff[4];
	int len = strlen(name);
	int willBe = dest+2+strlen(name);
	int pad = ((willBe + 3) & ~3)-willBe;

	putUInt16(buff,len+pad);
	fwrite(buff,1,2,out);

	fwrite(name,1,len,out);

	memset(buff,0,4);
	fwrite(buff,1,pad,out);

	fwrite(data,1,size,out);
	return 2+strlen(name)+pad+size;
}
int poolAllClasses(WClass **hashList,int numberOfClasses,int nonClasses)
{
	uint16 index = 0, i, off;
	//uint32 fieldsSize = dynamicHeapUsed;
	//uint32 methodsSize = 0, dynamicNeeded = 0;
	uint32 *info, size = 2500000, *classTable;
	uchar *pool = (uchar *)malloc(size), *p, *utf;
	ClassPool = pool;
	memset(pool,0,size);

	info = (uint32 *)pool;
	info[0] = numberOfClasses;
	info[1] = 0; //Method bytes needed.
	info[2] = 0; //Field bytes needed.

	info[3] = 0; //UtfPool offset.

	classTable = &info[4];
	ClassPoolTable = classTable;
	p = ((uchar *)classTable)+(numberOfClasses+1)*4;
	ClassPoolNames = (uint16*)p; // Entry and next.
	p += (numberOfClasses+1)*4;
	ClassPoolHash = (uint16 *)p; // 256 entries.
	p += 256*2;
//
//  Put utf pool.
//
	utf = loadFromMem("_UtfPool_",9,&size,NULL);
	info[3] = p-pool;
	ClassPoolUtf = p;
	(*(uint32 *)p) = size;
	p += 4;
	memcpy(p,utf,size);
	p += size;
//
//  Set the indexes for all the classes.
//
	for (i = 0; i < CLASS_HASH_SIZE; i++){
		WClass *wclass = hashList[i];


		if (wclass == NULL) ClassPoolHash[i] = 0;
		else ClassPoolHash[i] = index+1;

		while (wclass != NULL){
			index++;
			wclass->pooledIndex = index;
			wclass = wclass->nextClass;
		}
	}
//
//  Now pool the classes
//
	while((uint32)p & 3) p++;
	for (i = 0; i < CLASS_HASH_SIZE; i++){
		WClass *wclass = hashList[i];
		while (wclass != NULL){
			index = wclass->pooledIndex;
			classTable[index] = ((p-pool) << 1) | 1;
			off = getUInt16(CONS_ptr(wclass,wclass->classNameIndex));
			//if ((off & CONSTANT_PooledUtf8_MASK) == CONSTANT_PooledUtf8_1)
			ClassPoolNames[index*2] = off & (uint16)(~(CONSTANT_PooledUtf8_MASK<<8));
			/*
			else {
				off = getUInt16(CONS_ptr(wclass,wclass->classNameIndex)+2);
				ClassPoolNames[index*2] = off;
			}
			*/
			if (!(p = poolClass(p,wclass,index))){
				debugString("This class could not be pooled: ");
				debugUtf(WCLASS_className(wclass));
failed:
#ifndef UNIX
				Sleep(5*60*1000);
#endif
				return 0;
			}
			wclass = wclass->nextClass;
			ClassPoolNames[index*2+1] = wclass ? index+1 : 0;
		}
	}
	if (poolFailed){
		debugString(poolErrors.data);
#ifndef UNIX
				Sleep(5*60*1000);
#endif
				return 0;
	}
	info[1] = nativeIndex*sizeof(NativeFunc *);
	info[2] = staticFieldIndex*sizeof(Var);
/*
	methodsSize = dynamicHeapUsed-fieldsSize;
	dynamicNeeded = dynamicHeapUsed+index;
	*info++ = dynamicNeeded;
	*info++ = fieldsSize;
	*info++ = methodsSize;
	*info++ = MapStaticPointer(table);
	//
	//
	//
	//
	//
	ClassInfo = allocDynamicPart(index);
	memset(ClassInfo,CLASS_INFO_INITIALIZED,index);

*/
	/*
	{
	FILE *out = fopen("_ClassPool_","wb");
	fwrite(ClassPool,1,p-ClassPool,out);
	fclose(out);
	}
	*/
	{
		uchar buff[4];
		uint32 where = 0, idx = 0, didPool = 0, classPoolSize = p-ClassPool;
		FILE *out = fopen("Pooled.ewe","wb");
		fwrite("Wrp1",1,4,out);
		nonClasses++; // Add one for _ClassPool_
		putUInt32(buff,nonClasses);
		fwrite(buff,1,4,out);
		where = (nonClasses+1)*4+8;
		putUInt32(buff,where);
		fwrite(buff,1,4,out);
		for(idx = 0;; idx++){
			uint32 size;
			char *name = getRecordAt(0,idx,NULL,&size);
			if (name == NULL && didPool) break;
			if (name != NULL && strcmp("_UtfPool_",name) == 0) continue;
			else{
				if (name != NULL){
					int len = strlen(name);
					if (len > 6 && !strcmp(name+len-6,".class"))
						continue;
				}
				if (!didPool && (name == NULL || (strcmp("_ClassPool_",name) < 0))){
					didPool = 1;
					where += sizeUsed(where,"_ClassPool_",classPoolSize);
					if (name != NULL) idx--;
				}else {
					where += sizeUsed(where,name,size);
				}
			}
			putUInt32(buff,where);
			fwrite(buff,1,4,out);
			if (name == NULL) break;
		}

		didPool = 0;
		where = (nonClasses+1)*4+8;
		for(idx = 0;; idx++){
			uint32 size;
			uchar *data;
			char *name = getRecordAt(0,idx,&data,&size);
			if (name == NULL && didPool) break;
			if (name != NULL && strcmp("_UtfPool_",name) == 0) continue;
			else{
				if (name != NULL){
					int len = strlen(name);
					if (len > 6 && !strcmp(name+len-6,".class"))
						continue;
				}
				if (!didPool && (name == NULL || (strcmp("_ClassPool_",name) < 0))){
					didPool = 1;
					where += putInEwe(where,out,ClassPool,"_ClassPool_",classPoolSize);
					if (name != NULL) idx--;
				}else{
					where += putInEwe(where,out,data,name,size);
				}
			}
		}
		fclose(out);
#ifdef UNIX
		printf("\n\nPooled Ewe file: Pooled.ewe has been created.\n");
#else
		mMessageBox(NULL,L"Pooled Ewe file: Pooled.ewe has been created.",L"Pooled Ewe Created",MB_SETFOREGROUND);
#endif
	}
	return index;
}
#endif

static WClass *defineClass(UtfString className,WObject classLoader,uchar *classBytes,int length,int makeCopy,uchar *PooledUtf8)
{
	WClass *wclass, *superClass, **hashList;

	WClassMethod *method;
	UtfString actualName;
	uint16 i, n, classIndex;
	uint32 classHash, size;
	int isMath = 0, isThrowable = 0, isPooled = 0;
	uchar *p = classBytes, *constantsStart;
	static char sprintBuffer[256];
	if (p == NULL)
		{
		//MLB this is the only change made here.
		//VmError(ERR_CantFindClass, NULL, &className, NULL);
		return NULL;
		}

	classHash = genHashCode(className) % CLASS_HASH_SIZE;

	if (makeCopy && length >= 0){
		uchar *clbytes = classLoader == 0 ? (uchar *)allocClassPart(length) : mallocZ(length);
		if (clbytes == NULL) return NULL;
		memcpy(clbytes,classBytes,length);
		p = classBytes = clbytes;
	}

	if (!convertJavaName(&className,&isMath,&isThrowable)) return NULL;
	// NOTE: The garbage collector may run at any time during the
	// loading process so we need to make sure the class is valid
	// whenever an allocation may occur (static class vars, <clinit>, etc)
	// The various int variables will be initialzed to zero since
	// the entire class areas is zeroed out to start with.

	if (classLoader == 0) {
		wclass = (WClass *)allocClassPart(sizeof(struct WClassStruct)) ;
		wclass->utfPool = PooledUtf8;
	}else{
		char *wc = (char *)mallocZ(sizeof(struct WClassStruct)+1);
		BOOL odd = FALSE;
		if ((wc != NULL) && (((int)wc & 0x1) != 0)){
			wc++;
			odd = TRUE;
		}

		wclass = (WClass *)wc;
		if (odd) wclass->oddOffset = 1;
	}
	if (wclass == NULL)
		return NULL;

	if (className.len > 4)
		if (strncmp(className.str,"ewe/",4) == 0)
			wclass->isSystemClass = 1;
		else if (strncmp(className.str,"java/lang/",10) == 0)
			wclass->isSystemClass = 1;
		else
			wclass->isSystemClass = 0;

	if (classLoader != 0 && wclass->isSystemClass){
		throwException("java/lang/SecurityException",sprintBuffer);
		goto unlockReturnNull;
	}
	wclass->clazzLoader = classLoader;
	if (isMath) mathClass = wclass;
	if (isThrowable) throwableClass = wclass;

	LOCK_CLASS_HEAP

	wclass->byteRep = p;

	// Initialization to NULL (NULL is required to be 0 - see sanity check) is
	// automatic since memory regions are zeroed out when created. So, can comment
	// out the following code since we don't have to NULL anything out, its
	// already 0
	//
	// wclass->superClasses = NULL;
	// wclass->numSuperClasses = 0;
	// wclass->attrib2 = NULL;
	// wclass->constantOffsets = NULL;
	// wclass->fields = NULL;
	// wclass->methods = NULL;
	// wclass->nextClass = NULL;
	// wclass->objDestroyFunc = NULL;

	// after this marker 32 bit alignment will be enforced.

	isPooled = (getUInt32(p) == PooledClassMarker);
	p += 8;
#ifdef MAKING_POOL
	wclass->sectionOne = p-wclass->byteRep;
#endif
	//
	if (isPooled){
		wclass->pooledIndex = getUInt16(p);
		p += 2;
		ClassPoolTable[wclass->pooledIndex] = (uint32)wclass;
		wclass->classNameIndex = getUInt16(p);
		p += 2;
	}

	wclass->numConstants = getUInt16(p);

	p += 2;
	//
	// FIXME - fix the constants.
	//
	if (isPooled){
		uint32 constantSizeInBytes;
		wclass->constantLookup = (uint16 *)p;
		p += wclass->numConstants*2;
		constantSizeInBytes = getUInt32(p);
		p += 4;
		wclass->constantTable = p;
		p += constantSizeInBytes;
	}else{
		constantsStart = p;
		if (wclass->numConstants != 0)
			{
			size = sizeof(ConsOffset) * wclass->numConstants;
			ConstantsSize += (size + 3) & ~3;
	#ifdef MAKING_POOL
			NumberOfConstants += wclass->numConstants;
			if (wclass->numConstants > MaxConstants)
				MaxConstants = wclass->numConstants;
	#endif

			wclass->constantOffsets = classLoader == 0 ? (ConsOffset *)allocClassPart(size) : (ConsOffset *)mallocZ(size);
			if (wclass->constantOffsets == NULL)
				{
				wclass->numConstants = 0;
				goto unlockReturnNull;
				}
			for (i = 1; i < wclass->numConstants; i++)
				{
				if (p - wclass->byteRep > MAX_consOffset)
					{
					strncpy(sprintBuffer,className.str,className.len);
					sprintBuffer[className.len] = 0;
					strcat(sprintBuffer,"- class too large for this VM");

					throwException("java/lang/LinkageError",sprintBuffer);
					//VmError(ERR_ClassTooLarge, NULL, &className, NULL);
					goto unlockReturnNull;
					}
				wclass->constantOffsets[i - 1] = p - wclass->byteRep;
				p = loadClassConstant(wclass, i, p);
				// after a long or double, next entry does not exist
				if (CONS_tag(wclass, i) == CONSTANT_Long ||
					CONS_tag(wclass, i) == CONSTANT_Double)
					i++;
				}
			}
	#ifdef MAKING_POOL
			OriginalConstantSize += p-constantsStart;
	#endif

	}
	// second attribute section
	wclass->attrib2 = p;
	wclass->access = getUInt16(p);
	if (isPooled) wclass->access |= ACCESS_POOLED;

	p += 6;

	// assign class name
	if (!isPooled)
		wclass->classNameIndex = CONS_nameIndex(wclass, WCLASS_thisClass(wclass));
	// verify class name
	actualName = getUtfString(wclass,wclass->classNameIndex);
	if (classLoader != 0)
		if ((actualName.len != className.len) || (strncmp(actualName.str,className.str,className.len) != 0)){
			int l = 0;
			strncpy(sprintBuffer,className.str,className.len);
			sprintBuffer[className.len] = 0;
			strcat(sprintBuffer," (Wrong name:");
			l = strlen(sprintBuffer);
			strncat(sprintBuffer,actualName.str,actualName.len); l+= actualName.len;
			sprintBuffer[l] = ')';
			sprintBuffer[l+1] = 0;
			throwException("java/lang/ClassNotFoundException",sprintBuffer);


			return NULL;
		}


	if (isThrowable)
		wclass->isThrowable = 1;

	// NOTE: add class to class list here so garbage collector can
	// find it during the loading process if it needs to collect.
	hashList = (classLoader == 0) ? classHashList : loadedClassList;
	wclass->nextClass = hashList[classHash];
	hashList[classHash] = wclass;

	// load superclasses (recursive) here so we can resolve var
	// and method offsets in one pass
	superClass = NULL;
	classIndex = WCLASS_superClass(wclass);

	if (classIndex != 0)
		{

		UNLOCK_CLASS_HEAP
		superClass = getClassByIndex(wclass, classIndex);
		if (superClass == NULL){ //Maybe due to out of memory.

			hashList[classHash] = wclass->nextClass;
			return NULL; // can't find superclass
		}
		// fill in superclasses table
		if (superClass->isThrowable)
			wclass->isThrowable = 1;
		if (superClass->hasFinalizer)
			wclass->hasFinalizer = 1;
		n = superClass->numSuperClasses + 1;
#ifdef QUICKBIND
		if (n > MAX_superClassNum)
			{
			strncpy(sprintBuffer,className.str,className.len);
			sprintBuffer[className.len] = 0;
			strcat(sprintBuffer," - class too large for this VM");
			throwException("java/lang/LinkageError",sprintBuffer);
			//VmQuickError(ERR_ClassTooLarge);
			return NULL;
			}
#endif
		size = n * sizeof(WClass *);
		LOCK_CLASS_HEAP
		wclass->superClasses = classLoader == 0 ? (WClass **)allocClassPart(size) : (WClass **)mallocZ(size);
		if (wclass->superClasses == NULL)
			{
			//The exception will be thrown in allocClassPart
			//VmQuickError(ERR_OutOfClassMem);
			goto unlockReturnNull;
			}
		size = (n - 1) * sizeof(WClass *);
		xmemmove(wclass->superClasses, superClass->superClasses, size);
		wclass->superClasses[n - 1] = superClass;
		wclass->numSuperClasses = n;

		// inherit num of superclass variables to start
		wclass->numVars = superClass->numVars;
		}

	//
	// skip past interfaces
	//
	n = getUInt16(p);
	p += 2 + (n * 2);
#ifdef MAKING_POOL
	wclass->sectionTwo = p-wclass->attrib2;
#endif
//==========================================================
// This is for non-pooled.
//==========================================================
	if (!isPooled){
	//==========================================================
	// Fields
	//==========================================================
		wclass->numFields = getUInt16(p);
		p += 2;
		if (wclass->numFields != 0){
			size = sizeof(WClassField) * wclass->numFields;
			wclass->myFields = classLoader == 0 ? (WClassField *)allocClassPart(size) : (WClassField *)mallocZ(size);
			FieldsSize += (size + 3) & ~3;
			if (wclass->myFields == NULL){
				wclass->numFields = 0;
				goto unlockReturnNull;
			}
			for (i = 0; i < wclass->numFields; i++){
				p = loadClassField(wclass, WCLASS_fieldPtr(wclass,i), p);
				wclass->initializedFields++;
			}
		}
	//==========================================================
	// Methods
	//==========================================================
		wclass->numMethods = getUInt16(p);
		p += 2;
		size = sizeof(WClassMethod) * wclass->numMethods;
#ifdef MAKING_POOL
		NumberOfFields += wclass->numFields;
		if (wclass->numFields > MaxFields) {
			debugUtf(actualName);
			MaxFields = wclass->numFields;
			sprintf(sprintBuffer,"%d",wclass->numFields);
			debugString(sprintBuffer);
		}
		if (wclass->numFields > 64){
			sprintf(sprintBuffer,"%d",wclass->numFields);
			debugString(sprintBuffer);
		}
		NumberOfMethods += wclass->numMethods;
		if (wclass->numMethods > MaxMethods) MaxMethods = wclass->numMethods;
#endif
		if (size != 0){
			wclass->methods = classLoader == 0 ? (WClassMethod *)allocClassPart(size)  : (WClassMethod *)mallocZ(size);
			if (wclass->methods == NULL){
				wclass->numMethods = 0;
				goto unlockReturnNull;
			}
			for (i = 0; i < wclass->numMethods; i++){
				int error = 0;
				p = loadClassMethod(wclass, WCLASS_methodPtr(wclass,i), p, 0, &error);//isMath ? 1 : 0);
				if (error != 0){
					strncpy(sprintBuffer,className.str,className.len);
					sprintBuffer[className.len] = 0;
					strcat(sprintBuffer,error == -2 ? " - parameter list too large for this VM." : " - class parameter specs invalid.");
					throwException("java/lang/LinkageError",sprintBuffer);
					return NULL;
				}
			}
		}
#ifdef QUICKBIND
	// sort the methods and create the virtual method map for fast lookup
		if (createVirtualMethodMap(wclass) < 0)
			goto unlockReturnNull;
#endif
		}
	wclass->classAttributes = p;
//
// Go past attributes to get to the end.
//
	p = wclass->classAttributes;
	{
		uint16 attrCount = getUInt16(p), i;
		p += 2;
		for (i = 0; i < attrCount; i++){
			uint32 size = getUInt32(p+2);
			p += 6;
			p += size;
		}
	}
#ifdef MAKING_POOL
	wclass->sectionThree = p-wclass->classAttributes;
#endif
//==========================================================
// Pooled Fields, Method Virtual table and Methods.
//==========================================================
	if (isPooled){
		uint16 *inits;
		//
		// Fields - 16 bit aligned.
		//
		while((uint32)p & 1) p++;
		wclass->numFields = getUInt16(p);
		p += 2;
		wclass->myFields = (WClassField *)p;
		p += sizeof(WPooledField)*wclass->numFields;
		inits = (uint16 *)p;
		for (i = 0; i<wclass->numFields; i++){
			WClassField *f = WCLASS_fieldPtr(wclass,i);
			if (FIELD_isStatic(f)){
				if (FIELD_hasInitializer(f)){
					initializeField(wclass,f,*inits);
					inits++;
				}
			}else{
				wclass->numVars++;
				if (FIELD_is64(f)) wclass->numVars++;
			}
			wclass->initializedFields++;
		}
		p = (uchar *)inits;
		//
		// Virtual table - 32 bit aligned.
		//
		while((uint32)p & 3) p++;
		wclass->vMethodMap = *((VirtualMethodMap *)p);
		p += sizeof(VirtualMethodMap);
		wclass->vMethodMap.mapValues = (VMapValue *)p;
		p += sizeof(VMapValue)*wclass->vMethodMap.mapSize;
		//
		// The methods.
		//
		wclass->numMethods = getUInt16(p);
		p += 2;
		wclass->methods = (WClassMethod *)p;
		p += sizeof(WPooledMethod)*wclass->numMethods;
		//
		// Initialize native methods
		//
		for (i = 0; i<wclass->numMethods; i++){
			WClassMethod *method = WCLASS_methodPtr(wclass,i);
			if (wclass->isSystemClass && (METH_accessFlags(method) & ACCESS_NATIVE) > 0)
			*METH_nativeFuncPointer(method) = getNativeMethod(wclass, getUtfString(wclass,METH_nameIndex(method)),getUtfString(wclass,METH_descIndex(method)));
		}

	}
//==========================================================
// Now do the rest.
//==========================================================
	// set hooks (before class init which might create/free objects of this type)
	setClassHooks(wclass);
	// if our superclass has a destroy func, we inherit it. If not, ours overrides
	// our base classes destroy func (the native destroy func should call the
	// superclasses)
	if (superClass != NULL && wclass->objDestroyFunc == NULL)
		wclass->objDestroyFunc = superClass->objDestroyFunc;


	if (wclass->hasFinalizer == 0 && objectClass != NULL)
		if (getMethod(wclass,createUtfString("finalize"),createUtfString("()V"),NULL) != NULL)
			wclass->hasFinalizer = 1;

	UNLOCK_CLASS_HEAP

	// call static class initializer method if present
#ifndef MAKING_POOL
	method = getMethod(wclass, createUtfString("<clinit>"), createUtfString("()V"), NULL);
	if (method != NULL)
		executeMethod(wclass, method, NULL, 0);
	if (thrownException != 0){
		WObject thrown = thrownException;
		hashList[classHash] = wclass->nextClass;
		/*
		if (WOBJ_class(thrown) != outOfMemoryClass){
			WObject ex;

			//thrownException = 0;
			//ex = throwException("java/lang/ExceptionInInitializerError",NULL);

			if (WOBJ_class(ex) != outOfMemoryClass)
				objectPtr(ex)[3].obj = thrown;
		}
		*/
		return NULL;
	}
#endif

/*
Pooled classes now read:

1. The virtual method map.
2. The method array.
3. The method data.

*/
	if (isPooled){ //32 bit align yourself.
		while((uint32)p & 3) p++;
	}

	NumberOfClasses++;
#ifdef MAKING_POOL
#endif
	//sprintf(sprintBuffer,"OriginalMethodSize: %d, NewMethodSize: %d, OriginalConstantSize: %d, NewConstantSize: %d, DynamicConstantSize: %d",OriginalMethodSize,NewMethodSize,OriginalConstantSize,NewConstantSize,ConstantsSize);
	/*
	if (NumberOfClasses > 594 || 1) {
		sprintf(sprintBuffer,"%d, %d(%d), %d(%d), %d, %d %d %d %d",totalClassHeapUsed+classHeapUsed,NumberOfClasses*((sizeof(WClass)+3)&~3),NumberOfClasses,NumberOfMethods*((sizeof(WClassMethod)+3)&~3),NumberOfMethods,ConstantsSize,FieldsSize,VMapSize,NumberOfConstants,MaxConstants);
		debugString(sprintBuffer);
	}
	*/
	return wclass;

unlockReturnNull:
	UNLOCK_CLASS_HEAP
	return NULL;
	}

UtfString getPooledUtf(uchar *stringPool,uint16 stringIndex)
{
	UtfString string;
	uchar *pool = stringPool+((stringIndex & 0x3fff)*4);
	uint32 str = getUInt32(pool);
	if (stringIndex == 54)

		stringIndex = 54;
	string.str = (char *)(stringPool+str);
	string.len = getUInt32(pool+4)-str;
	return string;
}
UtfString getPooledStringName(uint16 classRef)
{
	return getPooledUtf(ClassPoolUtf,ClassPoolNames[classRef*2]);
}
uint16 getPooledNext(uint16 ref)
{
	return ClassPoolNames[ref*2+1];
}

WClass *resolveClass(uint16 ref)
{
	uint32 val = ClassPoolTable[ref];
	if (!(val & 1)) return (WClass *)val;
	else{
		WClass *wc =
			defineClass(getPooledStringName(ref),0,(ClassPool+((val >> 1) & 0xefffffff)),0,0,ClassPoolUtf);
		return wc;
	}
}




static struct byte_data lookForClass;
static WObject replaceCharacter(WObject string,uint16 look,uint16 replace);
static WObject createStringCopy(WObject other,int *length,uint16 **data);

static Var ClassForName(Var stack[])
{
	WObject name = stack[0].obj;
	Var v;
	if (name == 0) return returnExError(ERR_NullObjectAccess);
	else{
		UtfString className = stringToUtf8(name,&lookForClass,0), component;
		unsigned int i;
		WClass *cl;
		for (i = 0; i<className.len; i++) {
			if (className.str[i] == '.') className.str[i] = '/';
		}
		if (className.len == 0) goto error;
		if (className.str[0] == '['){
			if (className.len < 2) goto error;
			switch(className.str[1]){
			case 'Z': case 'B': case 'C': case 'S': case 'I': case 'J':
			case 'D': case 'F': case '[':
				break;
			case 'L':
				if (className.str[className.len-1] != ';') goto error;
				component.str = className.str+2;
				component.len = className.len-3;
				if (tryGetClass(component) == NULL) goto error;
				break;
			default: goto error;
			}
			v.obj = createObject(getClass(createUtfString("java/lang/Class")));
			if (v.obj != 0){
				objectPtr(v.obj)[1].classRef = NULL;
				pushObject(v.obj);
				objectPtr(v.obj)[2].obj = replaceCharacter(createStringCopy(name,NULL,NULL),'.','/');
				popObject();
			}
			return v;
		}
		cl = tryGetClass(className);
		if (cl != NULL)
			if (WCLASS_loader(cl) == 0)
				return returnClass(cl);
		for (i = 0; i<className.len; i++) {
			if (className.str[i] == '/') className.str[i] = '.';
		}

error:
		return returnExceptionUtf("java/lang/ClassNotFoundException",className);

	}
}
static Var ClassLoaderDefineClass(Var stack[])
{
	WObject cl = stack[0].obj;
	WObject name = stack[1].intValue;
	WObject byteArray = stack[2].intValue;
	int offset = stack[3].intValue;
	int len = stack[4].intValue;
	Var v;
	v.obj = 0;

	if (name == 0 || byteArray == 0) return returnExError(ERR_NullObjectAccess);
	else{
		uchar *bytes = (uchar *)WOBJ_arrayStart(byteArray);
		int trueLen = WOBJ_arrayLen(byteArray);
		unsigned int i;
		WClass *got;
		UtfString className;
		if (bytes == NULL || offset < 0 || offset+len > trueLen) return returnExError(ERR_IndexOutOfRange);
		className = stringToUtf8(name,&lookForClass,0);
		for (i = 0; i<className.len; i++) if (className.str[i] == '.') className.str[i] = '/';
		got = defineClass(className,cl,bytes+offset,len,TRUE,NULL);
		return returnClass(got);
	}
	return v;
}
static WClass *classLoaderGetClass(UtfString className,WObject classLoader)

{
	if (classLoader == 0) return NULL;
	else{
		WClass *cl = WOBJ_class(classLoader);
		WClass *cl2 = cl;
		WClassMethod *load = getMethod(cl,createUtfString("loadClass"),createUtfString("(Ljava/lang/String;Z)Ljava/lang/Class;"),&cl2);
		if (load != NULL){
			Var pars[3];
			Var ret;
			unsigned int i;
			for (i = 0; i<className.len; i++) {
				if (className.str[i] == '/') className.str[i] = '.';
			}
			pars[0].obj = classLoader;
			pars[1].obj = createStringFromUtf(className);
			pars[2].intValue = 1;
			executeMethodRet(cl2,load,pars,3,&ret);
			if (ret.obj == 0) return NULL;
			return (WClass *)objectPtr(ret.obj)[1].classRef;
		}
	}
	return NULL;
}

static BOOL isInHeap(WObject what);

static void deleteClass(WClass *cl)
{
	int i;
	uchar *by;
	if (cl->byteRep) free(cl->byteRep);
	if (cl->constantOffsets) free(cl->constantOffsets);
	if (cl->superClasses) free(cl->superClasses);
	if (cl->myFields) free(cl->myFields);
	if (cl->methods != NULL){
		for (i = 0; i<cl->numMethods; i++){
			WClassMethod *m = WCLASS_methodPtr(cl,i);
			if (METH_numHandlers(m))
				free(METH_handlers(m));
		}
		free(cl->methods);
	}
#ifdef QUICKBIND
	if (cl->vMethodMap.mapValues != NULL) free(cl->vMethodMap.mapValues);
#endif
	by = (uchar *)cl;
	if (cl->oddOffset) by--;
	free(by);
}

static void cleanOutLoadedClasses()
{
	WObject inHeap[10], notInHeap[10], numIn = 0, numNotIn = 0;
	int i;
	for (i = 0; i<10; i++) inHeap[i] = notInHeap[i] = 0;
	for (i = 0; i<CLASS_HASH_SIZE; i++){
		WClass *cl = loadedClassList[i], *prev = NULL;
		while(cl != NULL){
			WObject loader = WCLASS_loader(cl);
			int h;
			//if (loader == 0) continue; //This really should never be the case.
			BOOL isIn = FALSE, notIn = FALSE;
			//UtfString cn = WCLASS_className(cl);
			if (TRUE)
				for (h = 0; h<10 && inHeap[h] != 0; h++)
					if (loader == inHeap[h]){
						isIn = TRUE;
						notIn = FALSE;
						break;
					}

			if (!isIn)
				for (h = 0; h<10 && notInHeap[h] != 0; h++)
					if (loader == notInHeap[h]){
						notIn = TRUE;
						isIn = FALSE;
						break;
					}
			if (!isIn && !notIn){
				notIn = !(isIn = isInHeap(loader));
				if (isIn) inHeap[(numIn++)%10] = loader;
				else notInHeap[(numNotIn++)%10] = loader;
			}
			if (isIn) {
				prev = cl;
				cl = cl->nextClass;
				continue;
			}else{
				WClass *toGo = cl;
				cl = cl->nextClass;
				if (prev == NULL) loadedClassList[i] = cl;
				else prev->nextClass = cl;
				deleteClass(toGo);
			}
		}
	}

}

static WClass *tryGetOrLoadClass(UtfString className,WObject classLoader)
	{
	WClass *wclass, **hashList;
	UtfString iclassName;
	uint32 classHash;
	int isMath = 0, isThrowable = 0;
	// look for class in hash list

	classHash = genHashCode(className) % CLASS_HASH_SIZE;
	hashList = classHashList;
tryAgain:
	wclass = hashList[classHash];
	while (wclass != NULL)
		{
		iclassName = getUtfString(wclass, wclass->classNameIndex);
		if (className.len == iclassName.len &&
			!xstrncmp(className.str, iclassName.str, className.len))
			break;
		wclass = wclass->nextClass;
		}
	if (wclass != NULL) return wclass;
	if (hashList == classHashList) {

		hashList = loadedClassList;

		goto tryAgain;

	}
//
// See if it is in the pooled classes.
//
	if (ClassPoolHash){
		uint16 pooled = ClassPoolHash[classHash];
		//sprintf(sprintBuffer,"First: %d",(uint32)pooled); DB();
		while(pooled){
			iclassName = getPooledStringName(pooled);
			if (className.len == iclassName.len &&
				!xstrncmp(className.str, iclassName.str, className.len))
					return resolveClass(pooled);
			else
				pooled = getPooledNext(pooled);
		}
	}


	// NOTE: Classes mapping to those in the java/lang package can be
	// found in the path ewe/lang. This is to avoid any confusion that
	// the java/lang classes exist in the base set of classes. Note that
	// we change the name only for loading purposes, not for hash table
	// lookup, etc. Also note that we aren't changing the name, just the
	// pointer which is on the stack so we aren't modifying the data
	// passed from the caller.
	{
		UtfString newName = className;
		uchar *p, *PooledUtf8 = NULL;
		if (!convertJavaName(&newName,NULL,NULL)) return NULL;
		p = nativeLoadClass(newName, NULL,&PooledUtf8);
		if (p != NULL) return defineClass(className,0,p,-1,0,PooledUtf8);
		wclass = classLoaderGetClass(className,classLoader);
		if (wclass == NULL) thrownException = 0;
		return wclass;
	}
}

static WClass *tryGetClass(UtfString className) {return tryGetOrLoadClass(className,0);}


static WClass *getOrLoadClass(UtfString className,WObject classLoader)
{
	WClass *ret = tryGetOrLoadClass(className,classLoader);
	if (ret != NULL) return ret;
	if (classLoader != 0) {

	}
	if (thrownException == 0)
		throwExceptionUtf("java/lang/NoClassDefFoundError",className);
	return NULL;
}
	//MLB added tryGetClass()
static WClass *getClass(UtfString className)
{
	WClass *ret = tryGetClass(className);
	if (ret == NULL && thrownException == 0){
		if (className.len == 16 && !strncmp(className.str,"java/lang/Object",16)) return NULL;
		throwExceptionUtf("java/lang/NoClassDefFoundError",className);
	}
		//VmError(ERR_CantFindClass, NULL, &className, NULL);
	return ret;
}

#ifdef QUICKBIND
//
//
// This will not work for pooled classes - must be pre-done.
//
//
static int createVirtualMethodMap(WClass *wclass)
	{
	WClass *superClass;
	VirtualMethodMap *superVMap;
	uint32 size;
	uint16 i, n, nLow, nHigh, numSuperClasses;
	WClassMethod *method, tempMethod;
	VirtualMethodMap *vMap;
	uint16 methodHash[OVERRIDE_HASH_SIZE];

	// This method is responsible for filling in the VirtualMethodMap
	// structure to allow fast method lookup.
	//
	// It also sorts the method table so new virtual methods appear first,
	// overridden virtual methods second and non-virtual methods last.
	//

	// The method map contains the list of this classes superclasses
	// as well as an array that maps virtual method indicies to
	// class and method indicies to handle overridden methods.
	vMap = &wclass->vMethodMap;

	// The following code is commented out since memory regions are
	// zeroed out when created. The following code is commented out to
	// show this type of initialization is not required.
	//
	// vMap->mapValues = NULL;
	// vMap->mapSize = 0;
	// vMap->numVirtualMethods = 0;
	// vMap->numOverriddenMethods = 0;

	// sort methods so virtual appear first and non-virtual last

	n = wclass->numMethods;
	nLow = 0;
	nHigh = 0;
	while (nLow + nHigh < n)
		{
		method = &wclass->methods[nLow];
		// NOTE: the virtual section will not include <init> methods since
		// they should not be inherited
		if (((METH_accessFlags(method) & (ACCESS_PRIVATE /*| ACCESS_STATIC*/)) == 0) && !method->isInit)
			nLow++;
		else
			{
			nHigh++;
			// swap non-virtual to bottom
			tempMethod = *method;
			*method = wclass->methods[n - nHigh];
			wclass->methods[n - nHigh] = tempMethod;
			}
		}
	vMap->numVirtualMethods = nLow;

	numSuperClasses = wclass->numSuperClasses;
	if (numSuperClasses == 0)
		{
		// Object class - no superclass map to inherit and no inherited methods
		// to override
		return 0;
		}

	superClass = wclass->superClasses[numSuperClasses - 1];
	superVMap = &superClass->vMethodMap;

	// create method map by copying superclass method map and inheriting
	// superclass virtual methods

	vMap->mapSize = superVMap->mapSize + superVMap->numVirtualMethods;
	if (vMap->mapSize + wclass->numMethods > MAX_methodNum + 1)
		{
		UtfString className = getUtfString(wclass,wclass->classNameIndex);
		strncpy(sprintBuffer,className.str,className.len);
		sprintBuffer[className.len] = 0;
		strcat(sprintBuffer,"- class too large for this VM");
		throwException("java/lang/LinkageError",sprintBuffer);
		//VmQuickError(ERR_ClassTooLarge);
		return -1;
		}
	size = vMap->mapSize * sizeof(VMapValue);

	VMapSize += (size + 3) & ~3;
	vMap->mapValues = WCLASS_loader(wclass) == 0 ? (VMapValue *)allocClassPart(size) : (VMapValue *)mallocZ(size);

	if (vMap->mapValues == NULL)
		{

		//Exception thrown in allocClassPart
		//VmQuickError(ERR_OutOfClassMem);
		return -1;
		}
	size = superVMap->mapSize * sizeof(VMapValue);
	xmemmove(vMap->mapValues, superVMap->mapValues, size);
	//add superclass #'s + method numbers into second portion
	n = 0;
	for (i = superVMap->mapSize; i < vMap->mapSize; i++)
		{
		vMap->mapValues[i].classNum = numSuperClasses - 1;
		vMap->mapValues[i].methodNum = n++;
		}

	// generate hash table of inherited methods allowing fast check

	// for overriden methods
	xmemzero(methodHash, sizeof(uint16) * OVERRIDE_HASH_SIZE);
	for (i = 0; i < vMap->mapSize; i++)
		{
		VMapValue mapValue;
		WClass *iclass;
		UtfString name, desc;
		uint16 hash;

		mapValue = vMap->mapValues[i];

		iclass = wclass->superClasses[mapValue.classNum];
		method = WCLASS_methodPtr(iclass,mapValue.methodNum);
		name = getUtfString(iclass, METH_nameIndex(method));
		desc = getUtfString(iclass, METH_descIndex(method));

		hash = (genHashCode(name) + genHashCode(desc)) % OVERRIDE_HASH_SIZE;
		methodHash[hash] = i + 1;
		}

	// in virtual method section, determine overrides and move overrides to
	// bottom of virtual section
	n = vMap->numVirtualMethods;
	nLow = 0;
	nHigh = 0;
	while (nLow + nHigh < n)
		{
		uint16 hash;

		int32 overrideIndex;
		UtfString name, desc;

		method = &wclass->methods[nLow];
		name = getUtfString(wclass, METH_nameIndex(method));
		desc = getUtfString(wclass, METH_descIndex(method));

		// look in hash table first
		hash = (genHashCode(name) + genHashCode(desc)) % OVERRIDE_HASH_SIZE;
		overrideIndex = methodHash[hash];
		if (!overrideIndex)
			{
			nLow++;
			continue;
			}
		overrideIndex -= 1;
		if (compareMethodNameDesc(wclass, (uint16)overrideIndex, name, desc))
			; // found it from hash
		else
			overrideIndex = getMethodMapNum(wclass, name, desc, SEARCH_INHERITED);
		if (overrideIndex == -1)
			nLow++;
		else
			{
			// override - swap overridden method to bottom and add to map
			nHigh++;
			tempMethod = wclass->methods[nLow];
			wclass->methods[nLow] = wclass->methods[n - nHigh];
			wclass->methods[n - nHigh] = tempMethod;
			vMap->mapValues[overrideIndex].classNum = numSuperClasses;
			vMap->mapValues[overrideIndex].methodNum = n - nHigh;
			}
		}
	vMap->numVirtualMethods -= nHigh;
	//vMap->numOverriddenMethods = nHigh;

#ifdef NEVER
	// NOTE: This is some code you can run under Windows to see what the
	// internal class structure/virtual method map looks like
	{
	VMapValue mapValue;
	int ii, methodIndex, superIndex;
	WClass *iclass;
	UtfString mname, mdesc;
	UtfString className;
	int i;

	AllocConsole();
	cprintf("className: ");
	className = getUtfString(wclass, wclass->classNameIndex);
	for (i = 0; i < className.len; i++)
		cprintf("%c", className.str[i]);
	cprintf("\n");
	cprintf("- nSuperClasses=%2d inherited=%2d new=%2d override=%2d total=%2d\n",
	wclass->numSuperClasses, vMap->mapSize, vMap->numVirtualMethods, vMap->numOverriddenMethods,
	wclass->numMethods);
	cprintf("- FULL METHOD MAP\n");

	for (ii = 0; ii < vMap->mapSize + wclass->numMethods; ii++)
		{
		if (ii < vMap->mapSize)
			{
			mapValue = vMap->mapValues[ii];
			superIndex = mapValue.classNum;
			if (superIndex < wclass->numSuperClasses)

				iclass = wclass->superClasses[superIndex];


			else
				iclass = wclass;
			methodIndex = mapValue.methodNum;
			cprintf("- inherited %d/%d %d ", superIndex, wclass->numSuperClasses, methodIndex);
			}
		else
			{
			iclass = wclass;
			methodIndex = ii - vMap->mapSize;
			cprintf("- this class %d ", methodIndex);
			}
		method = &iclass->methods[methodIndex];
		if (methodIndex >= iclass->numMethods)
			cprintf("*************************************\n");

		mname = getUtfString(iclass, METH_nameIndex(method));
		mdesc = getUtfString(iclass, METH_descIndex(method));
		cprintf("- [%d] %s %s\n", ii, mname.str, mdesc.str);
		}
	}
#endif

	return 0;
	}

#endif

static uchar *loadClassConstant(WClass *wclass, uint16 idx, uchar *p)
	{
	uchar tag = *p;
	uint16 len;
	p++;
#ifdef MAKING_POOL
	NewConstantSize += 2;
#endif
	if ((tag & CONSTANT_PooledUtf8_MASK) == CONSTANT_PooledUtf8_1){
		p++;
		return p;
	}else if ((tag & CONSTANT_PooledUtf8_MASK) == CONSTANT_PooledUtf8_2){
		p += 3;
		return p;
	}
	switch (CONS_tag(wclass, idx))
		{
		case CONSTANT_Utf8:
			p += 2;
			len = getUInt16(p-2);
			p += len;
#ifdef MAKING_POOL
			OriginalConstantSize -= len+1; //Pooled utf takes 4 bytes plus the String.
#endif
				//CONS_utfLen(wclass, idx);
			break;
		case CONSTANT_Fieldref:
		case CONSTANT_Methodref:
			p+= 4;
			break;
		case CONSTANT_Integer:
		case CONSTANT_Float:
		case CONSTANT_InterfaceMethodref:
		case CONSTANT_NameAndType:
#ifdef MAKING_POOL
			NewConstantSize += 4;
#endif
			p += 4;
			break;
		case CONSTANT_Class:
		case CONSTANT_String:
			p += 2;
			break;
		case CONSTANT_Long:
		case CONSTANT_Double:

#ifdef MAKING_POOL
			NewConstantSize += 8;
#endif
			p += 8;
			break;
		}
	return p;
	}
//
// Object Routines
//
/*
static int fieldIs64wide (WClass *wclass, WClassField *field)
{
	char cc = CONS_ptr(wclass, FIELD_descIndex(field))[3];

	return cc == 'D' || cc == 'J';
}
*/
/*
void getFieldInfo(char *buff,WClassField *field)
{
	strcpy(buff,field->nameAndDescriptions);
}
*/
static void getInt64(uchar* buf,Var* v1, Var* v2)
{
	v1->intValue = getUInt32(buf+4);
	v2->intValue = getUInt32(buf);
}
static void initializeField(WClass *wclass,WClassField *field,uint16 initIndex)
{
	Var *v1 = GetStaticVarPointer(field);
	if (!is64(field)) //64Bits
		*v1 = constantToVar(wclass, initIndex);
    else{
		Var *v2 = GetStaticVarPointer2(field);
		CONS_wide(wclass, initIndex, v1, v2);
	}
}

static uchar *loadClassField(WClass *wclass, WClassField *field, uchar *p)
	{
	uint32 i, bytesCount;
	uint16 attrCount, nameIndex;
	UtfString attrName, fieldDesc;
	uchar *cp;
/*
	UtfString fieldName, className;
	char *fieldInfo;
	int len = 0;
*/
	field->access = getUInt16(p);
	field->nameIndex = getUInt16(p+2);
	field->descIndex = getUInt16(p+4);
	cp = CONS_ptr(wclass, FIELD_descIndex(field));
    fieldDesc = getUtfString(wclass, FIELD_descIndex(field));
#ifdef MAKING_POOL
	field->myClass = wclass;
#endif
/*
	className = getUtfString(wclass, wclass->classNameIndex);
    fieldName = getUtfString(wclass, FIELD_nameIndex(field));

	fieldInfo = mMalloc(256);
	strncpy(fieldInfo,className.str,className.len); len += className.len;
	strcpy(fieldInfo+len,">"); len++;
	strncpy(fieldInfo+len,fieldDesc.str,fieldDesc.len); len += fieldDesc.len;
	strcpy(fieldInfo+len," "); len++;
	strncpy(fieldInfo+len,fieldName.str,fieldName.len); len += fieldName.len;
	fieldInfo[len] = 0;
	field->nameAndDescriptions = fieldInfo;
*/
    if (fieldDesc.str[0] == 'D' || fieldDesc.str[0] == 'J')
	   field->access |= ACCESS_IS_64;
	// compute offset of this field's variable in the object
    if (!FIELD_isStatic(field)){
		field->varOffset = wclass->numVars++;
		if (is64(field)) wclass->numVars++; //field->var2.varOffset = wclass->numVars++;
    }else{
		field->staticVarAddress = WCLASS_loader(wclass) ? (Var *)mallocZ(sizeof(Var)*(FIELD_is64(field) ? 2 : 1)) : (Var *)allocClassPart(sizeof(Var)*(FIELD_is64(field) ? 2 : 1));
	}
	p += 6;
	attrCount = getUInt16(p);
	p += 2;
	for (i = 0; i < attrCount; i++){
		nameIndex = getUInt16(p);
		attrName = getUtfString(wclass, nameIndex);
		p += 2;
		bytesCount = getUInt32(p);
		p += 4;
		if (FIELD_isStatic(field) && attrName.len == 13 && bytesCount == 2 &&
			!xstrncmp(attrName.str, "ConstantValue", 13)){
#ifdef MAKING_POOL
				field->access |= ACCESS_HAS_INITIALIZER;
				field->initializerIndex = getUInt16(p);
#endif
				initializeField(wclass,field,getUInt16(p));
		}else
			; // MS Java has COM_MapsTo field attributes which we skip
		p += bytesCount;
		}
	return p;
}
//
// Handle interned strings here.
//
struct constantString {
	struct constantString* next;
	WClass* wclass;
	uint16 idx;
	WObject string;
};
//
static int constantHashCode(WClass *wclass, uint16 idx)
{
	int i = idx;
	i <<= 24;
	i |= (int)wclass;
	return i & 0x7fffffff;
}
//
#define CS_LENGTH 251
static struct constantString* constants[CS_LENGTH];
//
static WObject findConstantString(WClass* wclass, uint16 idx)
{
	struct constantString *cs;
	int where = constantHashCode(wclass,idx) % CS_LENGTH;
	for (cs = constants[where]; cs != NULL; cs = cs->next)
		if (cs->wclass == wclass && cs->idx == idx)
			return cs->string;
	return 0;
}
static void putConstantString(WClass* wclass, uint16 idx, WObject string)
{
	int where = constantHashCode(wclass,idx) % CS_LENGTH;
	struct constantString *cs = (struct constantString*)malloc(sizeof(struct constantString));
	if (cs == NULL) return;
	cs->wclass = wclass;
	cs->idx = idx;
	cs->string = string;
	cs->next = constants[where];
	constants[where] = cs;
}
static void markConstants()
{
	int i = 0;
	for (i = 0; i<CS_LENGTH; i++){
		struct constantString *cs = constants[i];
		for(;cs != NULL; cs = cs->next)
			markObject(cs->string);
	}
}
//
static Var constantToVar(WClass *wclass, uint16 idx)
	{

	Var v;
	uint16 stringIndex;
	uchar tag;
	if (WCLASS_isPooled(wclass)){
		uint16 redir = POOLED_redirect(wclass,idx);
		switch(redir & 0xc000){
			case 0xc000: tag = CONSTANT_String; break;
			case 0x8000: tag = CONSTANT_Integer; break;
			case 0x4000: tag = CONSTANT_Float; break;
			case 0x0000:
				v.obj = findConstantString(wclass,idx);
				if (v.obj == 0){
					v.obj = createStringFromUtf8(getPooledUtf(ClassPoolUtf,redir));
					putConstantString(wclass,idx,v.obj);
				}
				return v;
		}
	}else
		tag = CONS_tag(wclass, idx);
	switch (tag)
		{
		case CONSTANT_Integer:
			v.intValue = CONS_integer(wclass, idx);
			break;
		case CONSTANT_Float:
			v.floatValue = CONS_float(wclass, idx);
			break;

		case CONSTANT_String:
				v.obj = findConstantString(wclass,idx);
				if (v.obj == 0){
					stringIndex = CONS_stringIndex(wclass, idx);
					v.obj = createStringFromUtf8(getUtfString(wclass, stringIndex));
					putConstantString(wclass,idx,v.obj);
				}
				return v;
			break;
		case CONSTANT_Long:
		case CONSTANT_Double:
		default:
			v.obj = 0; // bad constant
			break;
		}
	return v;
	}
/*
static char *mathNames[] = {"sin",NULL};

static void mapMathMethod(UtfString name,UtfString desc,WClassMethod *method)
{
	int i;
	for (i = 0; ; i++){
		char *nm = mathNames[i];
		unsigned int n, not = 0;

		if (nm == NULL) return;
		for (n = 0; n<name.len; n++){
			if (nm[n] != name.str[n]){
				not = 1;
				break;
			}
		}
		if (not) continue;
		if (nm[n] == 0) {
			method->directMap = i+1;
			return;
		}
		if (nm[n] != '(') continue;
		else{
			unsigned int d = 0;
			for (d = 0; d<desc.len; d++){
				if (nm[n+d] != name.str[d]){
					not = 1;
					break;
				}
			}
			if (not) continue;
			if (nm[n+d] == 0){
				method->directMap = i+1;
				return;
			}
		}

	}
}
*/
static WClassMethodExtra *makeExtra(WClass *wclass,WClassMethod *m)

{
	if (!m->extra)

		m->extra = WCLASS_loader(wclass) == 0 ?
			(WClassMethodExtra *)allocClassPart(sizeof(WClassMethodExtra)):
			(WClassMethodExtra *)mallocZ(sizeof(WClassMethodExtra));
	return m->extra;
}

static uchar *loadClassMethod(WClass *wclass, WClassMethod *method, uchar *p, int options,int *error)
	{
	uint32 i, j, bytesCount, codeCount;
	uint16 attrCount, attrNameIndex, numHandlers, numAttributes;
	int32 numParams;
	uchar *attrStart;
	UtfString attrName, methodName, methodDesc;
	int isMath = options & 1;
#ifdef MAKING_POOL
	uchar *originalStart = p;
#endif
//........................................................
	method->access = getUInt16(p); p += 2;
	method->indexes.nameIndex = getUInt16(p); p += 2;
	method->indexes.descIndex = getUInt16(p); p += 2;
	method->myClass = wclass;
//........................................................
	attrCount = getUInt16(p);
	p += 2;
	method->code.codePointer = NULL;
	for (i = 0; i < attrCount; i++)
		{
		attrStart = p;
		attrNameIndex = getUInt16(p);
		p += 2;
		attrName = getUtfString(wclass, attrNameIndex);
		bytesCount = getUInt32(p);
		p += 4;
		if (attrName.len == 10 && !xstrncmp(attrName.str,"Exceptions",10)){
			makeExtra(wclass,method)->throwsEx = p;
#ifdef MAKING_POOL
			NewMethodSize += bytesCount;
#endif
			p += bytesCount;
			continue;
		}
		if (attrName.len != 4 || xstrncmp(attrName.str, "Code", 4)){
			p += bytesCount;
			continue;
		}
		// Code Attribute
		method->access |= ACCESS_HAS_CODE;
		method->code.codePointer = attrStart+6;
		p += 4;
		codeCount = getUInt32(p);
#ifdef MAKING_POOL
		if (codeCount > MaxCodeCount) MaxCodeCount = codeCount;
		NewMethodSize += codeCount+8;
#endif
		p += 4 + codeCount;
//...................................................................
// Handle exceptions.
//...................................................................
		numHandlers = getUInt16(p);
		p += 2;
		if (numHandlers != 0){
			WExceptionHandler *h;
			makeExtra(wclass,method)->numHandlers = numHandlers;
			h = method->extra->handlers = WCLASS_loader(wclass) == 0 ? (WExceptionHandler *)allocClassPart(sizeof(struct WExceptionHandlerStruct)*numHandlers):
								(WExceptionHandler *)mallocZ(sizeof(struct WExceptionHandlerStruct)*numHandlers);
			for (j = 0; j < numHandlers; j++, p += 8){
				WExceptionHandler *eh = h+j;
				eh->startPc = getUInt16(p);
				eh->endPc = getUInt16(p+2);
				eh->handlerPc = getUInt16(p+4);
				eh->catchType = getUInt16(p+6);
			}
		}
//...................................................................
// Look for line numbers
//...................................................................
		numAttributes = getUInt16(p);
		p += 2;
		for (j = 0; j < numAttributes; j++)
			{
			uint16 ani = getUInt16(p);

			UtfString an = getUtfString(wclass,ani);
			p += 2;
			if (an.len == 15 && !strncmp(an.str,"LineNumberTable",15))
				makeExtra(wclass,method)->lineNumbers = p+4;
			p += getUInt32(p) + 4;
			}
		}
//...................................................................
	methodDesc = getUtfString(wclass, METH_descIndex(method));
	methodName = getUtfString(wclass, METH_nameIndex(method));
	// determine numParams, isInit and returnsValue
	//if (isMath){
	//	mapMathMethod(methodName,methodDesc,method);
	//}
	numParams = countMethodParams(methodDesc,&method->parameterIs64Bits);
	if (numParams < 0){
		*error = -1;
		return NULL;
	}
	if (numParams > 15){
		*error = -2;
		return NULL;
	}
	method->numParams = (uint16)numParams;
#ifdef MAKING_POOL
	if (method->numParams > MaxParams)
		MaxParams = method->numParams;
#endif
	if (methodName.len > 2 && methodName.str[0] == '<' && (methodName.str[1] == 'i'||methodName.str[1] == 'c'))
		method->isInit = 1;
	else
		method->isInit = 0;
	if (methodDesc.str[methodDesc.len - 1] == 'V')
		method->returnsValue = 0;
	else if (methodDesc.str[methodDesc.len - 1] == 'J' || methodDesc.str[methodDesc.len - 1] == 'D')
		method->returnsValue = 2;
	else
		method->returnsValue = 1;

	// resolve native functions

	if (wclass->isSystemClass && (METH_accessFlags(method) & ACCESS_NATIVE) > 0)
		*METH_nativeFuncPointer(method) = getNativeMethod(wclass, methodName, methodDesc);
#ifdef MAKING_POOL
	OriginalMethodSize += (p-originalStart);
	NewMethodSize += sizeof(struct WPooledMethodStruct);

#endif
	return p;
	}

static int32 countMethodParams(UtfString desc,uint16 *flags)
	{
	uint32 n;
	char *c;
	uint16 is64 = 0, mask64 = 1;


	c = desc.str;
	if (*c++ != '(')
		return -1;
	n = 0;
	while (1)
		{
		switch (*c)
			{

			case 'B':

			case 'C':


			case 'F':
			case 'I':
			case 'S':

			case 'Z': n++; c++; break;

			case 'D':
			case 'J': n+=2; is64 |= mask64; c++; break; //64Bits

			case 'L':
				c++;
				while (*c++ != ';')
					;
				n++;
				break;
			case '[':
				n++; //Arrays are always one parameter.
				while (*c == '[') c++; //Skip past array or arrays (if this exists).
				if (*c == 'L') //Skip past object.
					while (*c++ != ';');
				else
					c++; //Skip past primitive type.
				break;
			case ')':
				if (flags != NULL) *flags = is64;
				return n;
			default:
				if (flags != NULL) *flags = is64;
				return -1;
			}
			mask64 <<= 1;
		}
	if (flags != NULL) *flags = is64;
	}

//
// UtfString Routines
//

static UtfString createUtfString(char *buf)
	{
	UtfString s;

	s.str = buf;
	s.len = xstrlen(buf);
	return s;
	}

static UtfString getUtfString(WClass *wclass, uint16 idx)
	{
	UtfString s;

	if (WCLASS_isPooled(wclass))
		return getPooledUtf(ClassPoolUtf,POOLED_redirect(wclass,idx));

	if ((idx >= 1) && CONS_tag(wclass, idx) == CONSTANT_Utf8)
		{
			uchar *ptr = CONS_ptr(wclass,idx);
			s.len = getUInt16(ptr+1);
			s.str = (char *)(ptr+3);
		}
	else if (idx >= 1){
		CONS_getUtfStr(&s,wclass,idx);
	}else
		{
		s.str = "";
		s.len = 0;
		}
	return s;
	}

static void fillStackTrace(WObject th,int ignore);

static WObject createObject(WClass *wclass)
	{
	WObject obj;

	if (wclass == NULL)
		return 0;
	if ((WCLASS_accessFlags(wclass) & ACCESS_ABSTRACT) > 0)
		return 0; // interface or abstract class
	obj = allocObject(WCLASS_objectSize(wclass),wclass == outOfMemoryClass);
	if (obj == 0)
		return 0;
	WOBJ_classAssign(obj) = wclass;
	if (wclass->isThrowable && wclass != outOfMemoryClass) fillStackTrace(obj,0);
	return obj;
	}

static int32 arrayTypeSize(int32 type)
	{
	switch (type)
		{
		case 1:  // object
		case 2:  // array
			return 4;
		case 4: // boolean
		case 8: // byte
			return 1;
		case 5:  // char
		case 9:  // short
			return 2;
		case 6:  // float
		case 10: // int
			return 4;
		case 7:  // double
		case 11: // long
			return 8;
		}
	return 0;
	}


static int32 arraySize(int32 type, int32 len)
	{
	int32 typesize, size;

	typesize = arrayTypeSize(type);
	//MLB changed 3 to 4
	//size = (3 * sizeof(Var)) + (typesize * len);
	size = (4 * sizeof(Var)) + (typesize * len);

	// align to 4 byte boundry
	size = (size + 3) & ~3;

	return size;
	}


static WObject wAlloc(int bytes,unsigned char **memory)
{
	WObject got = createArrayObject(arrayType('B'),bytes);
	if (memory != NULL) *memory = NULL;
	if (got != 0 && memory != NULL) *memory = (unsigned char *)WOBJ_arrayStart(got);
	return got;
}
static WObject createArrayObjectClass(WClass *wclass, int32 len)
{
	if (wclass == NULL) return 0;
	else{
		WObject got = createArrayObject(1,len);
		if (got == 0) return got;
		WOBJ_arrayComponent(got) = wclass;
		return got;
	}
}
static WObject createArrayObject(int32 type, int32 len)
	{
	WObject obj;


	obj = allocObject(arraySize(type, len),0);
	if (len < 0 || obj == 0){
		//debugStackTrace();
		return 0;
	}
	// pointer to class is NULL for arrays
	WOBJ_classAssign(obj) = NULL;
	WOBJ_arrayType(obj) = type;
	WOBJ_arrayLen(obj) = len;
	return obj;

	}

static char arrayChar(int32 type)
{
	switch(type){
		case 1: // object
			return 'L';
		case 2: // array
			return '[';
		case 4: // boolean
			return 'Z';
		case 8: // byte
			return 'B';
		case 5: // char
			return 'C';
		case 9: // short
			return 'S';
		case 6: // float
			return 'F';
		case 10: // int
			return 'I';
		case 7: // double
			return 'D';
		case 11: // long
			return 'J';
		}
	return 0;
}

static uint16 arrayType(char c)
	{
	switch(c)
		{
		case 'L': // object
			return 1;

		case '[': // array
			return 2;
		case 'Z': // boolean
			return 4;
		case 'B': // byte
			return 8;
		case 'C': // char
			return 5;
		case 'S': // short

			return 9;
		case 'F': // float
			return 6;
		case 'I': // int
			return 10;
		case 'D': // double
			return 7;
		case 'J': // long
			return 11;
		}
	return 0;
	}

#define ARRAYTYPE_OBJECT 1
#define ARRAYTYPE_ARRAY 2
//
//The name must be a Java encoded class name starting with 'L' and ending with ';'
//
UtfString toClassName(char *name)
{
	UtfString ret;
	ret.len = 0;
	ret.str = NULL;

	if (*name == 'L'){
		int i = 0;
		ret.str = name+1;
		while(name[i+1] != ';') i++;
		ret.len = i;
	}
	return ret;
}
//
//The name must be a Java encoded class name starting with 'L' and ending with ';'
//
WClass *toClass(char *name)
{
	return getClass(toClassName(name));
}
//
// Call this only if the type of the array is '['
//
UtfString arrayTypeName(WObject array)
{
	UtfString ret;
	ret.str = NULL;
	ret.len = 0;
	if (WOBJ_arrayType(array) != ARRAYTYPE_ARRAY || WOBJ_arrayComponent(array) == NULL) return ret;
	else{
		char *name = (char *)WOBJ_arrayComponent(array);
		int i = 0;
		for (;;i++){
			if (name[i] == '[') continue;
			else if (name[i] == 'L'){
				while(name[i] != ';') i++;
				i++;
				break;
			}else{
				i++;
				break;
			}
		}
		ret.str = name;
		ret.len = i;
		return ret;
	}
}
//
// This should only be called from the executeMethod() function, becuase desc
// must be persistent, which means it must be stored within a class file.
//
static WObject createMultiArray(int32 ndim, char *desc, Var *sizes)
	{
	WObject arrayObj, subArray, *itemStart;
	int32 i, len, type;

	len = sizes[0].intValue;
	type = arrayType(desc[0]);
	if (type == ARRAYTYPE_OBJECT)
		arrayObj = createArrayObjectClass(toClass(desc),len);
	else
		arrayObj = createArrayObject(type, len);
	if (len < 0 || !arrayObj)
		return 0;

// MULTI_ARRAY

	if (type == ARRAYTYPE_ARRAY) WOBJ_arrayComponent(arrayObj) = desc;

	if ((type != ARRAYTYPE_ARRAY) || ndim <= 1)
		return arrayObj;
	// NOTE: it is acceptable to push only the "upper"
	// array objects and not the most derived subarrays
	// because if the array is only half filled and we gc,
	// the portion that is filled will still be found since
	// its container was pushed
	if (pushObject(arrayObj) == -1)
		return 0;
	// create subarray (recursive)
	for (i = 0; i < len; i++)
		{
		// NOTE: we have to recompute itemStart after calling createMultiArray()
		// since calling it can cause a GC to occur which moves memory around

		subArray = createMultiArray(ndim - 1, desc + 1, sizes + 1);
		if (subArray == 0) {
			popObject();
			return 0;

		}
		itemStart = (WObject *)WOBJ_arrayStart(arrayObj);
		itemStart[i] = subArray;
		}
	popObject();
	return arrayObj;
	}
static Var returnExceptionUtf(char *name,UtfString message)
{
	Var v;
	v.obj = 0;
	throwExceptionUtf(name,message);//,NULL);
	return v;
}
static Var returnException(char *name,char *message)
{
	Var v;
	v.obj = 0;
	throwException(name,message);//,NULL);
	return v;
}

static Var returnError(uint16 error)
{
	Var v;
	v.obj = 0;
	VmQuickError(error);
	return v;
}

static Var returnExError(uint16 which)
{
	if (which == ERR_NullObjectAccess || which == ERR_NullArrayAccess)
		return returnException(NullPointerEx,NULL);
	if (which == ERR_IndexOutOfRange)
		return returnException(ArrayIndexEx,NULL);
	if (which == ERR_StringIndexOutOfRange)
		return returnException(StringIndexEx,NULL);
	if (which == ERR_DivideByZero)
		return returnException(DivideByZeroEx,"/ by zero");//,stack);
	else
		return returnError(which);
}

static Var returnVal(int value)
{
	Var v;
	v.intValue = value;
	return v;
}

//===================================================================
static Var VmGetStringChars(Var stack[])
//===================================================================
{
	Var v;
	WObject str = stack[0].obj;
	char * bytes;
	v.intValue = 0;

	if (str == 0) return v;
	v.obj = WOBJ_StringCharArrayObj(str);
	if (v.obj == 0) {
		debugStackTrace();
		bytes = (char *)WOBJ_arrayStart(v.obj);
	}
	return v;
}
//===================================================================
static Var VmCreateStringWithChars(Var stack[])
//===================================================================
{
	Var v;
	v.obj = 0;
	if (stack[0].obj == 0) throwException(NullPointerEx,NULL);
	else{
		v.obj = createObject(stringClass);
		if (v.obj != 0)
			WOBJ_StringCharArrayObj(v.obj) = stack[0].obj;
	}
	return v;
}
//===================================================================
static Var VmMutateString(Var stack[])
//===================================================================
{
	Var v;
	WObject str = stack[0].obj, chars = stack[1].obj, strChars;
	int start = stack[2].intValue;
	int length = stack[3].intValue;
	int useThis = stack[4].intValue;

	v.obj = str;

	if (str == 0 || chars == 0) return returnException(NullPointerEx,NULL);
	if (start+length > WOBJ_arrayLen(chars) || start < 0) return returnExError(ERR_IndexOutOfRange);
	if (useThis && (start == 0) && (length == WOBJ_arrayLen(chars))){
		objectPtr(str)[1].obj = chars;
		return v;

	}

	strChars = objectPtr(str)[1].obj;
	if (WOBJ_arrayLen(strChars) != length)
		objectPtr(str)[1].obj = createArrayObject(arrayType('C'),length);
	if (length != 0){
		int16 *s = (int16 *)WOBJ_arrayStart(chars);
		int16 *d = (int16 *)WOBJ_arrayStart(objectPtr(str)[1].obj);
		int i = 0;
		for (;i<length; i++) *d++ = *s++;
	}
	return v;
}

//===================================================================
static int16 *getStringData(WObject obj,int *length)
//===================================================================
{
	if (length != NULL) *length = 0;
	if (obj == 0) return NULL;
	obj = WOBJ_StringCharArrayObj(obj);
	if (obj == 0) return NULL;
	if (length != NULL) *length = WOBJ_arrayLen(obj);
	return (int16 *)WOBJ_arrayStart(obj);
}
//===================================================================
static int sizeofJavaUtf8String(unsigned char *data,int numberOfBytes)
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

static int makeHashCode(char *bytes,int length)
{
	int val = 0;
	char *b;
	for (b = bytes; b<bytes+length; b++) val += *b;
	val = (val << 6) | length;
	return val;
}


static Var UtilsMakeHashCodeBytes(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int num = stack[2].intValue;
	v.intValue = 0;
	if (array == 0) return v;
	if (st < 0 || st+num>WOBJ_arrayLen(array)) return returnExError(ERR_IndexOutOfRange);
	v.intValue = makeHashCode((char *)WOBJ_arrayStart(array)+st,num);
	return v;
}
static Var UtilsMakeHashCodeChars(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int num = stack[2].intValue;

	v.intValue = 0;
	if (array == 0) return v;


	if (st < 0 || st+num>WOBJ_arrayLen(array)) return returnExError(ERR_IndexOutOfRange);
	v.intValue = makeHashCode((char *)((uint16 *)WOBJ_arrayStart(array)+st),num*2);
	return v;
}

//===================================================================
static Var UtilsSizeofJavaUtf8Bytes(Var stack[])
//===================================================================
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int num = stack[2].intValue;
	unsigned char *p;
	v.intValue = 0;
	if (array == 0) return v;

	p = (unsigned char *)WOBJ_arrayStart(array)+st;
	v.intValue = sizeofJavaUtf8String(p,num);
	return v;
}
//===================================================================
static int javaUtf8ToStringData(unsigned char *data,uint16 *text,int numberOfBytes)

//===================================================================
{
	int i = 0, size = 0, t = 0;

	for (i = 0; i<numberOfBytes; i++,t++){
		unsigned char c = *data++;
		if ((c & 0x80) == 0) {

			text[t] = (uint16)c;
			continue;
		}else if ((c & 0xe0) == 0xc0) {
			text[t] = (((uint16)c & 0x1f)<<6) + ((uint16)*(data) & 0x3f);
			data++;
			i++;

		}else if ((c & 0xf0) == 0xe0) {
			text[t] = (((uint16)c & 0x0f)<<12) + (((uint16)*(data) & 0x3f)<<6)+((uint16)*(data+1) & 0x3f);
			data += 2;
			i += 2;
		}
	}
	return t;
}

/**
* Returns the number of bytes needed to encode a String in the Java UTF8 format.
**/
static int
//===================================================================
	sizeofEncodedJavaUtf8String(uint16 *toEncode,int length)
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
static Var UtilsSizeofJavaUtf8String(Var stack[])
//===================================================================
{
	Var v;
	WObject chars = stack[0].obj;
	int start = stack[1].intValue;
	int length = stack[2].intValue;

	if (chars == 0) return returnExError(ERR_NullObjectAccess);
	if (start+length > WOBJ_arrayLen(chars)) return returnExError(ERR_IndexOutOfRange);
	v.intValue = sizeofEncodedJavaUtf8String((uint16 *)WOBJ_arrayStart(chars)+start,length);
	return v;
}

static int
//===================================================================

	encodeJavaUtf8String(uint16 *toEncode,int length,unsigned char *destination)
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
//===================================================================
static Var UtilsEncodeJavaUtf8(Var stack[])
//===================================================================
{

	Var v;
	WObject chars = stack[0].obj;
	int start = stack[1].intValue;
	int length = stack[2].intValue;
	int need;
	WObject dest = stack[3].obj;
	int offset = stack[4].intValue;


	if (chars == 0 || dest == 0) return returnExError(ERR_NullObjectAccess);
	if (start+length > WOBJ_arrayLen(chars)) return returnExError(ERR_IndexOutOfRange);
	need = sizeofEncodedJavaUtf8String((uint16 *)WOBJ_arrayStart(chars)+start,length);
	if (offset+need > WOBJ_arrayLen(dest)) return returnExError(ERR_IndexOutOfRange);
	v.intValue = encodeJavaUtf8String((uint16 *)WOBJ_arrayStart(chars)+start,length,(unsigned char *)WOBJ_arrayStart(dest)+offset);
	return v;
}

//===================================================================
static int stringDataToUtf8(uint16 *toEncode,int length,ByteData dest,int append)
//===================================================================
{
	int size = sizeofEncodedJavaUtf8String(toEncode,length);
	if (!append) dest->length = 0;
	expandSpaceFor(dest,dest->length+size+1,10,append);
	encodeJavaUtf8String(toEncode,length,(uchar *)dest->data+dest->length);
	dest->length += size;
	dest->data[dest->length] = 0;
	return size;
}
/**

* This converts a Utf8 encoded string into full 16 bit unicode into
* the destination byte_data. The length field of the byte_data will
* be twice the number of characters. A zero character will be appended.
* The number of characters is returned.
**/
//===================================================================
static int utf8ToStringData(UtfString source,ByteData dest)
//===================================================================
{
	int size = sizeofJavaUtf8String((unsigned char *)source.str,source.len);
	expandSpaceFor(dest,(size+1)*2,20,0);
	javaUtf8ToStringData((unsigned char *)source.str,(uint16 *)dest->data,source.len);
	((uint16 *)dest->data)[size] = 0;
	dest->length = size*2;
	return size;
}

static WCHAR *utf8ToUnicode(UtfString source)
{
	struct byte_data bd;
	bd.data = 0;

	bd.length = 0;
	bd.space = 0;
	utf8ToStringData(source,&bd);
	return (WCHAR *)bd.data;
}
static WCHAR *utf8AsciiToUnicode(char *source,int length)
{
	UtfString s;
	s.len = length == -1 ? strlen(source) : length;
	s.str = source;

	return utf8ToUnicode(s);
}
//===================================================================
static UtfString stringToUtf8(WObject string,ByteData dest,int append)
//===================================================================
{

	UtfString ret;
	int length = 0;
	uint16 *toEncode = (uint16 *)getStringData(string,&length);
	ret.len = stringDataToUtf8(toEncode,length,dest,append);
	ret.str = dest->data;
	return ret;
}

struct byte_data tempString;

//===================================================================
static UtfString stringToTempUtf8(WObject string)
//===================================================================
{
	return stringToUtf8(string,&tempString,0);
}
static TCHAR *stringToNewUtf8(WObject string)
{
	UtfString ut = stringToTempUtf8(string);
	unsigned int i;
	TCHAR *ret = (TCHAR *)mMalloc(sizeof(TCHAR)*(ut.len+1));
	for (i = 0; i<ut.len; i++)
		ret[i] = (ut.str[i])&0xff;
	ret[i] = 0;
	return ret;
}
/*
{
	int size = sizeofJavaUtf8String(data,start,numberOfBytes);
	if (buffer != null)
		if (buffer.length < size) buffer = null;
	if (buffer == null) buffer = new char[size];
	int i = 0, t = offset;
	for (i = 0; i<numberOfBytes; i++){
		byte c = data[i+start];
		if ((c & 0x80) == 0)
			buffer[t] = (char)c;
		else if ((c & 0xe0) == 0xc0) {
			buffer[t] = (char)((((char)c & 0x1f)<<6) + ((char)data[i+start+1] & 0x3f));
			i++;
		}else if ((c & 0xf0) == 0xe0) {
			buffer[t] = (char)((((char)c & 0x0f)<<12) + (((char)data[i+start+1] & 0x3f)<<6)+((char)data[i+start+2] & 0x3f));
			i += 2;
		}
		t++;
	}
	return buffer;
}
*/
//===================================================================
static Var UtilsDecodeJavaUtf8(Var stack[])
//===================================================================
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int num = stack[2].intValue;
	WObject dest = stack[3].obj;
	int offset = stack[4].intValue;

	int ds;
	v.intValue = 0;
	if (array == 0) return v;
	ds = sizeofJavaUtf8String((unsigned char *)WOBJ_arrayStart(array)+st,num);

	if (dest != 0)
		if (WOBJ_arrayLen(dest) < ds+offset) dest = 0;
	if (dest == 0)
		dest = createArrayObject(arrayType('C'),offset+ds);
	if (dest != 0)
		javaUtf8ToStringData((unsigned char *)WOBJ_arrayStart(array)+st,(uint16 *)WOBJ_arrayStart(dest)+offset,num);
	v.obj = dest;
	return v;
}
//===================================================================
static WObject createStringFromJavaUtf8(int numberOfBytes,uint16 **unicode,char *utf8Text)
//===================================================================
{
	uint16 *theData;
	WObject ret;
	int howBig = sizeofJavaUtf8String((uchar *)utf8Text,numberOfBytes);
	ret = createNewString(howBig,&theData,NULL);
	if (!ret) return 0;
	javaUtf8ToStringData((uchar *)utf8Text,theData,numberOfBytes);
	if (unicode != NULL) *unicode = theData;
	return ret;
}
//===================================================================
static WObject createStringFromUtf8(UtfString str)
//===================================================================
{
	return createStringFromJavaUtf8(str.len,NULL,str.str);
}

//===================================================================
static WObject createNewString(int length,uint16 **data,uint16 *text)
//===================================================================
{
	WObject obj, chars;
	uint16 *dest;
	if (length < 0) length = 0;
	chars = createArrayObject(5,length);
	if (chars == 0) return 0;
	if (pushObject(chars) == -1) return 0;
	dest = (uint16 *)WOBJ_arrayStart(chars);
	if (text != NULL){
		int l = 0;
		for (l = 0; l<length; l++) *dest++ = *text++;
	}
	obj = createObject(stringClass);
	if (obj != 0)
		WOBJ_StringCharArrayObj(obj) = chars;
	popObject(); // charArrayObj
	if (data != NULL) *data = (uint16 *)WOBJ_arrayStart(chars);
	return obj;
}
//===================================================================
static WObject createStringCopy(WObject other,int *length,uint16 **data)
//===================================================================
{
	if (other == 0) {
		if (data != NULL) *data = NULL;
		if (length != NULL) *length = 0;
		return 0;
	}else {
		int len = WOBJ_StringLength(other);
		WObject ret;
		uint16 *c;
		pushObject(other);

		c = (uint16 *)mMalloc(sizeof(uint16)*len);
		memcpy(c,WOBJ_StringChars(other),sizeof(uint16)*len);
		ret = createNewString(len,data,c);
		free(c);
		popObject();
		if (length != NULL) *length = len;
		return ret;
	}
}
static WObject replaceCharacter(WObject string,uint16 look,uint16 replace)
{
	if (string != 0){
		int len = WOBJ_StringLength(string);
		uint16 *chars = WOBJ_StringChars(string);
		if (chars != NULL)
			for (; len>=0; len--,chars++) if (*chars == look) *chars = replace;
	}
	return string;
}
static WObject createString(char *buf)
	{
	return createStringFromUtf(createUtfString(buf));
	}

static WObject createStringFromUtf2(UtfString s)
	{
	WObject obj;
	uint16 *charStart;
	uint32 i;
	obj = createNewString(s.len,&charStart,NULL);
	if (obj != 0)
		for (i = 0; i < s.len; i++)
			//charStart[i] =(uint16)s.str[i]; MLB Bug Fix.
			charStart[i] = (uint16)s.str[i] & (uint16)0xff;
	return obj;
	}

static WObject createStringFromUtf(UtfString s)
{
	WObject ret = createStringFromUtf2(s);
	if (ret == 0);
		//VmQuickError(ERR_USER1);
	return ret;
}


#define MaxStaticUTFLen 255
static uchar sbytes[MaxStaticUTFLen+1];

// NOTE: Only set STU_USE_STATIC if the string is temporary and will not be
// needed before stringToUtf is called again. The flags parameter is a
// combination of the STU constants.
static UtfString stringToUtf(WObject string, int flags)
	{
	UtfString s;
	WObject charArray;
	uint32 i, len, extra;
	uint16 *chars;

	uchar *bytes;
	int nullTerminate, useStatic;


	nullTerminate = flags & STU_NULL_TERMINATE;
	useStatic = flags & STU_USE_STATIC;
	s.str = "";
	s.len = 0;
	if (string == 0)
		return s;
	charArray = WOBJ_StringCharArrayObj(string);
	if (charArray == 0)
		return s;
	len = WOBJ_arrayLen(charArray);
	extra = 0;
	if (nullTerminate)
		extra = 1;
	if (useStatic && (len + extra) <= MaxStaticUTFLen)
		bytes = sbytes;
	else
		{
		/* This is a bad idea! The bytes can move!
		WObject byteArray;

		byteArray = createArrayObject(8, len + extra);
		if (byteArray == 0)
			return s;
		bytes = (uchar *)WOBJ_arrayStart(byteArray);
		*/
		bytes = (uchar *)mMalloc(len+extra);
		}
	chars = (uint16 *)WOBJ_arrayStart(charArray);
	for (i = 0; i < len; i++)
		bytes[i] = (uchar)chars[i];
	if (nullTerminate)
		bytes[i] = 0;
	s.str = (char *)bytes;
	s.len = len;
	return s;
	}

static UtfString unDot(UtfString s)
{
	unsigned i;
	for (i = 0; i<s.len; i++)
		if (s.str[i] == '.') s.str[i] = '/';
	return s;
}
static UtfString toDot(UtfString s)
{
	unsigned i;
	for (i = 0; i<s.len; i++)
		if (s.str[i] == '/') s.str[i] = '.';
	return s;
}
static int utfEquals(UtfString one,UtfString two)
{
	if (one.len != two.len) return 0;
	return !xstrncmp(one.str,two.str,one.len);
}
static int utfStarts(UtfString str,char val)
{
	if (str.len == 0) return 0;
	return *str.str == val;
}
static int arrayRangeCheck(WObject array, int32 start, int32 count)
	{
	int32 len;

	if (array == 0 || start < 0 || count < 0)
		return 0;
	len = WOBJ_arrayLen(array);

	if (start + count > len)
		return 0;
	return 1;

	}
int didGc;

static Var copyArray(Var stack[])
	{
	Var v;
	WObject srcArray, dstArray;
	int32 srcStart, dstStart, len, srcType, typeSize;
	uchar *srcPtr, *dstPtr;

	v.intValue = 0;
	srcArray = stack[0].obj;
	srcStart = stack[1].intValue;
	dstArray = stack[2].obj;
	dstStart = stack[3].intValue;
	len = stack[4].intValue;
	if (srcArray == 0 || dstArray == 0)
		{
		//VmQuickError(ERR_NullArrayAccess);
		return v;
		}
	// ensure both src and dst are arrays
	if (WOBJ_class(srcArray) != NULL || WOBJ_class(dstArray) != NULL)
		return v;
	// NOTE: This is not a full check to see if the two arrays are compatible.
	// Any two arrays of objects are compatible according to this check
	// see also: compatibleArray()
	srcType = WOBJ_arrayType(srcArray);
	if (srcType != WOBJ_arrayType(dstArray))
		return v;
	// check ranges
	if (arrayRangeCheck(srcArray, srcStart, len) == 0 ||
		arrayRangeCheck(dstArray, dstStart, len) == 0)
		{
		return returnExError(ERR_IndexOutOfRange);
		}
	typeSize = arrayTypeSize(srcType);
	srcPtr = (uchar *)WOBJ_arrayStart(srcArray) + (typeSize * srcStart);
	dstPtr = (uchar *)WOBJ_arrayStart(dstArray) + (typeSize * dstStart);
	xmemmove((uchar *)dstPtr, (uchar *)srcPtr, len * typeSize);
	v.intValue = 1;
	return v;
	}

//MLB added method WClassField *tryGetField(WClass *wclass, UtfString name, UtfString desc)
static WClassField *tryGetField(WClass *wclass, UtfString name, UtfString desc)
	{
	WClassField *field;
	UtfString fname, fdesc;
	uint16 i;

	for (i = 0; i < wclass->numFields; i++)
		{
		field = WCLASS_fieldPtr(wclass,i);
		fname = getUtfString(wclass, FIELD_nameIndex(field));
		fdesc = getUtfString(wclass, FIELD_descIndex(field));
		if (name.len == fname.len &&
			desc.len == fdesc.len &&
			!xstrncmp(name.str, fname.str, name.len) &&
			!xstrncmp(desc.str, fdesc.str, desc.len))
			return field;
		}
	//VmError(ERR_CantFindField, wclass, &name, &desc);
	return NULL;
	}
static WClassField *tryGetFieldNonUtf(WClass *wclass, char * name, char * desc)
{
	UtfString n,d;
	WClass *cl = wclass;
	n.str = name; n.len = strlen(name);
	d.str = desc; d.len = strlen(desc);
	while (cl != NULL){
		WClassField *ret = tryGetField(cl,n,d);
		if (ret != NULL) return ret;
		else{
			uint16 classIndex = WCLASS_superClass(cl);
			if (classIndex == 0) return NULL;

			cl = getClassByIndex(cl, classIndex);
		}
	}
	return NULL;
}
static WClassField *tryGetFieldSuper(WClass *wclass, UtfString name, UtfString desc)
{
	while (wclass != NULL){
		WClassField *ret = tryGetField(wclass,name,desc);
		if (ret != NULL) return ret;
		else{
			uint16 classIndex = WCLASS_superClass(wclass);

			if (classIndex == 0) return NULL;
			wclass = getClassByIndex(wclass, classIndex);
		}
	}
	return NULL;
}
//MLB added method tryGetField()
static WClassField *tryGetFieldAlone(WClass *wclass, UtfString name,WClass **gotClass)
	{
	WClassField *field;
	UtfString fname;


	uint16 i;
	WClass *cl = wclass;

	while (cl != NULL){
		for (i = 0; i < cl->numFields; i++){
			field = WCLASS_fieldPtr(cl,i);
			fname = getUtfString(cl, FIELD_nameIndex(field));
			if (name.len == fname.len && !xstrncmp(name.str, fname.str, name.len)){
				if (gotClass != NULL) *gotClass = cl;
				return field;
			}
		}
		if (gotClass == NULL) return NULL;
		else{
			uint16 classIndex = WCLASS_superClass(cl);
			if (classIndex == 0) return NULL;
			cl = getClassByIndex(cl, classIndex);
		}
	}
	return NULL;
}
int stop = 0;
static WClassField *getField(WClass *wclass, UtfString name, UtfString desc)
{
	WClassField *got = tryGetFieldSuper(wclass,name,desc);
	if (got == NULL && thrownException == 0)
		throwExceptionUtf("java/lang/NoSuchFieldError",name);
		//VmError(ERR_CantFindField, wclass, &name, &desc);
	return got;
}

static WClass *getClassByIndex(WClass *wclass, uint16 classIndex)
{
	UtfString className;
	WClass *targetClass;

	if (WCLASS_isPooled(wclass))
		return ResolveClass(POOLED_redirect(wclass,classIndex));

#ifdef QUICKBIND
	else{
		ConsOffset offset = CONS_offset(wclass, classIndex);
		if (offset & CONS_boundBit)
			return (WClass *)(offset << 1);
	}
#endif

	className = getUtfString(wclass, CONS_nameIndex(wclass, classIndex));
	if (className.len > 1 && className.str[0] == '[')

		return NULL; // arrays have no associated class
	targetClass = getOrLoadClass(className,WCLASS_loader(wclass));
	if (targetClass == NULL)
		return NULL;
#ifdef QUICKBIND
#ifndef DONT_BIND
	{
		LOCK_CLASS_HEAP
		CONS_offset(wclass, classIndex) = CONS_boundBit | ((uint32)targetClass >> 1);
		UNLOCK_CLASS_HEAP
	}
#endif
#endif
	return targetClass;
}
static uint32 toff;

//#define GetFieldByIndex(WC,FI) !(WC->pooledIndex) ? ((toff = CONS_offset(WC,FI)) & CONS_boundBit ? (WClassField *)(toff << 1) : getFieldByIndex(WC,FI)) : getFieldByIndex(WC,FI)

#define GetFieldByIndex(WC,FI) getFieldByIndex(WC,FI)

static WClassField *getFieldByIndex(WClass *wclass, uint16 fieldIndex)


{
	WClassField *field;
	WClass *targetClass;
	uint16 classIndex, nameAndTypeIndex;
	UtfString fieldName, fieldDesc;

	if (WCLASS_isPooled(wclass)){
		uint16 redir = POOLED_redirect(wclass,fieldIndex);
		targetClass = ResolveClass((uint16)(redir & 0x3ff));
		if (targetClass) return WCLASS_fieldPtr(targetClass,((redir >> 10) & 0x3f));
		else return NULL;
	}
#ifdef QUICKBIND
	else{
		ConsOffset offset = CONS_offset(wclass, fieldIndex);
		if (offset & CONS_boundBit)
			return (WClassField*)(offset << 1);
	}
#endif
	classIndex = CONS_classIndex(wclass, fieldIndex);
	targetClass = getClassByIndex(wclass, classIndex);
	if (targetClass == NULL) return NULL;
	nameAndTypeIndex = CONS_nameAndTypeIndex(wclass, fieldIndex);
	fieldName = getUtfString(wclass, CONS_nameIndex(wclass, nameAndTypeIndex));
	fieldDesc = getUtfString(wclass, CONS_typeIndex(wclass, nameAndTypeIndex));
	field = getField(targetClass, fieldName, fieldDesc);
	if (field == NULL)return NULL;
#ifdef QUICKBIND
#ifndef DONT_BIND
	{
		LOCK_CLASS_HEAP
		CONS_offset(wclass, fieldIndex) = CONS_boundBit | ((uint32)field >> 1);
		UNLOCK_CLASS_HEAP
	}
#endif
#endif
	return field;
}
//
// Method Routines
//
static WClassMethod *getInterfaceMethod(WClass *wclass, UtfString name, UtfString desc, WClass **vclass)
{
	int num = WCLASS_numInterfaces(wclass);
	int i;
	//UtfString className = WCLASS_className(wclass);
	for (i = 0; i<num; i++){

		WClass *ic = getClassByIndex(wclass,WCLASS_interfaceIndex(wclass,i));
		if (ic != NULL){
			//UtfString className = WCLASS_className(ic);
			WClassMethod *wc = getMethod(ic,name,desc,vclass);
			if (wc != NULL) return wc;
		}
	}
	return NULL;
}
#ifdef QUICKBIND

static int compareMethodNameDesc(WClass *wclass, uint16 mapNum, UtfString name, UtfString desc)
	{
	UtfString mname, mdesc;
	WClassMethod *method;
	WClass *iclass;
	int noRet = 0;

	if (desc.len > 0)
		if	(desc.str[desc.len-1] == ')')
			noRet = 1;


	method = getMethodByMapNum(wclass, &iclass, mapNum);
	mname = getUtfString(iclass, METH_nameIndex(method));
	mdesc = getUtfString(iclass, METH_descIndex(method));
	if (noRet)
		while((mdesc.len != 0) && (mdesc.str[mdesc.len-1] != ')'))
			mdesc.len--;
	if (name.len == mname.len &&
		desc.len == mdesc.len &&
		!xstrncmp(name.str, mname.str, name.len) &&
		!xstrncmp(desc.str, mdesc.str, desc.len))
		return 1;
	return 0;
	}

static int32 getMethodMapNum(WClass *wclass, UtfString name, UtfString desc, int searchType)
	{
	VirtualMethodMap *vMap;
	uint16 start, end, i;

	vMap = &wclass->vMethodMap;
	if (searchType == SEARCH_ALL)
		{
		start = 0;
		end = vMap->mapSize + wclass->numMethods;
		}
	else if (searchType == SEARCH_INHERITED)
		{
		start = 0;
		end = vMap->mapSize;
		}
	else // SEARCH_THISCLASS
		{
		start = vMap->mapSize;
		end = vMap->mapSize + wclass->numMethods;
		}
	for (i = start; i < end; i++)
		{
		if (compareMethodNameDesc(wclass, i, name, desc))
			return i;
		}
	return -1;
	}

typedef struct CachedInterfaceStruct{
	WClass *requestor;
	WClassMethod *foundMethod;
	uint16 interfaceIndex;
} CachedInterface;

#ifdef WINCE
#define INTERFACE_CACHE_SIZE 5
#else
#define INTERFACE_CACHE_SIZE 13
#endif
static WClassMethod *getCachedInterface(WClass *requestingClass,uint16 interfaceIndex,WClass *targetClass, WClass **vclass)
{
	CachedInterface *cached;
	static int check = 0;
	if (targetClass->interfaceCache == NULL)
		targetClass->interfaceCache = allocClassPart(sizeof(CachedInterface)*INTERFACE_CACHE_SIZE);

	cached = ((CachedInterface *)targetClass->interfaceCache)+(((uint32)requestingClass+interfaceIndex) % INTERFACE_CACHE_SIZE);
	if (cached->requestor != requestingClass || cached->interfaceIndex != interfaceIndex){
		uint16 nameAndTypeIndex = CONS_nameAndTypeIndex(requestingClass, interfaceIndex);
		UtfString methodName = getUtfString(requestingClass, CONS_nameIndex(requestingClass, nameAndTypeIndex));
		UtfString methodDesc = getUtfString(requestingClass, CONS_typeIndex(requestingClass, nameAndTypeIndex));
		WClass *found = NULL;
		cached->foundMethod = getMethod(targetClass,methodName,methodDesc,&found);
		cached->requestor = requestingClass;
		cached->interfaceIndex = interfaceIndex;
		//debugUtf(methodName);
		//sprintf(sprintBuffer,"%x ----------------------",++check);
		//debugString(sprintBuffer);
	}else{
		//sprintf(sprintBuffer,"%x",++check);
		//debugString(sprintBuffer);
	}
	if (cached->foundMethod) *vclass = METH_class(cached->foundMethod);
	return cached->foundMethod;
}

static WClassMethod *getMethodByMapNum(WClass *wclass, WClass **vclass, uint16 mapNum)
	{
	VirtualMethodMap *vMap;
	VMapValue mapValue;
	uint16 superIndex, methodIndex;

	vMap = &wclass->vMethodMap;
	if (mapNum < vMap->mapSize)
		{
		// inherited or overridden method
		mapValue = vMap->mapValues[mapNum];
		superIndex = mapValue.classNum;
		if (superIndex < wclass->numSuperClasses)
			wclass = wclass->superClasses[superIndex];
		methodIndex = mapValue.methodNum;
		}

	else
		methodIndex = mapNum - vMap->mapSize;
	if (vclass != NULL)
		*vclass = wclass;


	return WCLASS_methodPtr(wclass,methodIndex);
	}

static WClassMethod *getMethod(WClass *wclass, UtfString name, UtfString desc, WClass **vclass)
	{
	int searchType;
	int32 mapNum;

	if (wclass == NULL) //Array
		return getArrayMethod(name,desc);
	if (vclass != NULL)

		searchType = SEARCH_ALL;
	else
		searchType = SEARCH_THISCLASS;
	mapNum = getMethodMapNum(wclass, name, desc, searchType);
	if (mapNum < 0){
		if (searchType == SEARCH_ALL)
			return getInterfaceMethod(wclass,name,desc,vclass);
		return NULL;
	}
	return getMethodByMapNum(wclass, vclass, (uint16)mapNum);
	}

#else

// vclass is used to return the class the method was found in
// when the search is virtual (when a vclass is given)
static WClassMethod *getMethod(WClass *wclass, UtfString name, UtfString desc, WClass **vclass)
	{
	WClass *original = wclass;
	WClassMethod *method;
	UtfString mname, mdesc;
	uint32 i, n;
	//UtfString s = getUtfString(wclass,wclass->classNameIndex);
	int noRet = 0;

	if (wclass == NULL) //Array
		return getArrayMethod(name,desc);

	n = wclass->numSuperClasses;
	if (desc.len > 0)
		if	(desc.str[desc.len-1] == ')')
			noRet = 1;
	while (1)
		{
		for (i = 0; i < wclass->numMethods; i++)
			{
			method = WCLASS_methodPtr(wclass->methods,i);
			mname = getUtfString(wclass, METH_nameIndex(method));
			mdesc = getUtfString(wclass, METH_descIndex(method));
			if (noRet)
				while((mdesc.len != 0) && (mdesc.str[mdesc.len-1] != ')'))
					mdesc.len--;
			if (name.len == mname.len &&
				desc.len == mdesc.len &&
				!xstrncmp(name.str, mname.str, name.len) &&
				!xstrncmp(desc.str, mdesc.str, desc.len))
				{
				if (vclass)
					*vclass = wclass;
				return method;
				}
			}
		if (!vclass)
			break; // not a virtual lookup or no superclass
		if (n == 0)
			break;

		// look in superclass

		wclass = wclass->superClasses[--n];
		}
	if (vclass != NULL)
		return getInterfaceMethod(original,name,desc,vclass);
	return NULL;
	}
#endif

static WClassMethod *getAMethod(WClass *clazz,char *name,char *desc,WClass **vclass)
{
	UtfString nm,ds;
	nm.str = name; nm.len = strlen(name);
	ds.str = desc; ds.len = strlen(desc);
	return getMethod(clazz,nm,ds,vclass);
}

static int compatibleMultiArrays(UtfString elements, UtfString arrayElements)
{
	int i = 0;
	for (i = 0;;i++){
		char c = elements.str[i];
		if (c != arrayElements.str[i]) {
			if (arrayElements.str[i] == 'L' && c == '[')
				return toClass(arrayElements.str+i) == objectClass;
			return 0;
		}
		if (c == '[') continue;
		if (c == 'L')
			return compatible(toClass(elements.str+i),toClass(arrayElements.str+i));
		else return 1;
	}
}
static int compatibleArrayElement(WObject element,UtfString arrayElement)
{
	int type = WOBJ_arrayType(element);
	arrayElement.str++;
	arrayElement.len--;
	//
	//Array Element now points to the component type of the element.
	if (type == ARRAYTYPE_OBJECT){
		if (arrayElement.str[0] == 'L') return compatible((WClass *)WOBJ_arrayComponent(element),toClass(arrayElement.str));
		else return 0;
	}else if (type == ARRAYTYPE_ARRAY){
		if (arrayElement.str[0] == '[') return compatibleMultiArrays(arrayTypeName(element),arrayElement);
		else if (arrayElement.str[0] == 'L') return toClass(arrayElement.str) == objectClass;
		else return 0;
	}else return arrayChar(type) == arrayElement.str[0];
}

static int compatibleElement(WObject element, WObject array)
{
	WClass *source = WOBJ_class(element);
	if (array == 0) return 0;
	if (WOBJ_arrayType(array) == ARRAYTYPE_OBJECT && WOBJ_arrayComponent(array) != NULL)
		return compatible(source == NULL ? objectClass : source,(WClass *)WOBJ_arrayComponent(array));
	else if (WOBJ_arrayType(array) == ARRAYTYPE_ARRAY && WOBJ_arrayComponent(array) != NULL){
		//"array" is an array of arrays. So we have to make sure that the component (an array) is
		//compatible with the source being stored there.
		//
		if (source != NULL) return 0; //The source is not an array.
		return compatibleArrayElement(element,arrayTypeName(array));
	}else
		return compatible(source == NULL ? objectClass : source, objectClass);
}
typedef struct array_type *ArrayType;

struct array_type {
	ArrayType next;
	UtfString type;
};

int checkTypes(UtfString type, UtfString ctype)
{
	unsigned int i = 0;
	int same = 1;
	for (i = 0; same && i<type.len; i++){
		char ch = type.str[i] == '.' ? '/' : type.str[i];
		same = ch == ctype.str[i];
	}
	return same;
}

char *newArrayType(UtfString type)
{
	static ArrayType lastFound = NULL;
	static ArrayType first = NULL;
	ArrayType c = lastFound;
	if (c != NULL && c->type.len == type.len)
		if (checkTypes(type,c->type))
			return c->type.str;
	for(c = first; c != NULL; c = c->next){
		if (c->type.len != type.len) continue;
		else if (checkTypes(type,c->type)) break;
	}
	if (c != NULL) lastFound = c;
	else{
		ArrayType at = (ArrayType)mMalloc(sizeof(struct array_type));
		if (at == NULL) return NULL;
		at->type.str = (char *)mMalloc(type.len);
		if (at->type.str == NULL) return NULL;
		else{
			unsigned int i = 0;
			for (i = 0; i<type.len; i++)
				at->type.str[i] = type.str[i] == '.' ? '/' : type.str[i];
			at->type.len = type.len;
			at->next = first;
			first = at;
			lastFound = at;
		}
	}
	return lastFound->type.str;
}
char *getArrayTypeString(WObject forWho)
{
	char c = arrayChar(WOBJ_arrayType(forWho));
	char *ret = NULL;
	if (c != 'L'){
		if (c == '[' && WOBJ_arrayComponent(forWho) != NULL){
			unsigned int i = 0;
			UtfString name = arrayTypeName(forWho);
			ret = (char *)mMalloc(name.len+2);
			ret[0] = '[';
			strncpy(ret+1,name.str,name.len);
			ret[name.len+1] = 0;
			return ret;
		}else{
			ret = (char *)mMalloc(3);
			ret[0] = '[';
			ret[1] = c;
			ret[2] = 0;
			return ret;
		}
	}else{
		UtfString component;
		unsigned int i;
		WClass *cclass = (WClass *)WOBJ_arrayComponent(forWho);
		if (cclass == NULL) component = createUtfString("java/lang/Object");
		else component = getUtfString(cclass, cclass->classNameIndex);
		ret = (char *)mMalloc(component.len+4);
		ret[0] = '['; ret[1] = 'L'; ret[component.len+3-1] = ';'; ret[component.len+3] = 0;
		for (i = 0; i<component.len; i++) ret[2+i] = component.str[i];
		return ret;
	}
}

// return 1 if two classes are compatible (if wclass is compatible
// with target). this function is not valid for checking to see if
// two arrays are compatible (see compatibleArray()).
// see page 135 of the book by Meyers and Downing for the basic algorithm
static int compatible(WClass *source, WClass *target)
	{
	int targetIsInterface;
	uint32 i, n;


	if (!source || !target)
		return 0; // source or target is array

	if (source == target) return 1;


	targetIsInterface = 0;
	if (WCLASS_isInterface(target))
		targetIsInterface = 1;
	n = source->numSuperClasses;
	while (1)
		{
		if (targetIsInterface)

			{
			for (i = 0; i < WCLASS_numInterfaces(source); i++)
				{
				uint16 classIndex;
				WClass *interfaceClass;

				classIndex = WCLASS_interfaceIndex(source, i);
				interfaceClass = getClassByIndex(source, classIndex);
				// NOTE: Either one of the interfaces in the source class can
				// equal the target interface or one of the interfaces
				// in the target interface (class) can equal one of the
				// interfaces in the source class for the two to be compatible
				if (interfaceClass == target)
					return 1;
				if (compatible(interfaceClass, target))
					return 1;
				}
			}
		else if (source == target)
			return 1;
		if (n == 0)
			break;
		// look in superclass
		source = source->superClasses[--n];
		}
	return 0;
	}

static int compatibleArray(WObject obj, UtfString arrayName)
	{
	WClass *wclass;

	wclass = WOBJ_class(obj);
	if (wclass != NULL)
		return 0; // source is not array

	// NOTE: this isn't a full check to see if the arrays
	// are the same type. Any two arrays of objects (or other
	// arrays since they are objects) will test equal here.
	if (WOBJ_arrayType(obj) != arrayType(arrayName.str[1])){
		if (arrayName.str[1] == 'L' && WOBJ_arrayType(obj) == ARRAYTYPE_ARRAY)
			return toClass(arrayName.str+1) == objectClass;
		return 0;
	}
	arrayName.str++; arrayName.len--;

//If they are the same then check to see if the component types are compatible.

	if (WOBJ_arrayType(obj) == 2) {
		if (arrayName.str[0] != '[') return 0;
		return compatibleMultiArrays(arrayTypeName(obj),arrayName);
		//else return 0;
	}
//This checks the components of the array.
	if (WOBJ_arrayType(obj) == 1){
		arrayName.str++; arrayName.len -= 2;
		if (WOBJ_arrayComponent(obj) == NULL){//Null class, then assume an array of objects.
			if (arrayName.len != 16) return 0;
			return (strncmp("java/lang/Object",arrayName.str,16) == 0);
		}else{
			WClass *cl = tryGetClass(arrayName);
			if (cl == NULL) return 0;
			return compatible((WClass *)WOBJ_arrayComponent(obj),cl);

		}
	}
	return 1;
	}

//
// Memory Management
//

// Here's the garbage collector. I implemented the mark and sweep below
// after testing out a few different ones and reading:
//
// Garbage Collection, Algorithms for Automatic Dynamic Memory Management
// by Richard Jones and Rafael Lins
//
// which is an excellent book. Also, this collector has gone through a
// lot of testing. It runs when the system is completely out of memory
// which can happen at any time.. for example during class loading.
//
// To test it out, tests were run where 1000's of random objects were
// loaded, constructed and random methods called on them over some
// period of days. This found a couple subtle bugs that were
// fixed like when the garbage collector ran in the middle of array
// allocation and moved pointers around from under the array allocator
// code (those have all been fixed).
//
// The heap is comprised of Hos objects (an array) that grows from
// the "right" of object memory and objects that take up the space on
// on the "left" side. The Hos array keeps track of where the objects
// are on the left.

//
// The Hos structure (strange, but aptly named) is used to keep
// track of handles (pointers to memory locations), order (order
// of handles with respect to memory) and temporary items (used
// during the scan phase).
//
// The 3 items in the Hos structure do not relate to each other. They
// are each a part of 3 conceptually distinct arrays that grow
// from the right of the heap while the objects grow from the left.

// So, when the Hos array is indexed, it is always negative (first
// element is 0, next is -1, next is -2, etc).

typedef struct
	{
	Var *ptr;
	uint32 order;
	uint32 temp;
	} Hos;

// NOTE: The total amount of memory used up at any given
// time in the heap is: objectSize + (numHandles * sizeof(Hos))

typedef struct
	{
	Hos *hos; // handle, order and scan arrays (interlaced)
	uint32 numHandles;

	uint32 numFreeHandles;

	uchar *mem;
	uint32 memSize; // total size of memory (including free)
	uint32 objectSize; // size of all objects in heap
	} ObjectHeap;

static ObjectHeap heap;
static uint32 originalHeapSize;

#define FIRST_OBJ 100000//2244

#define VALID_OBJ(o) (o > FIRST_OBJ && o <= FIRST_OBJ + heap.numHandles)

// NOTE: this method is only for printing the status of memory
// and can be removed. Also note, there is no such thing as
// the "amount of free memory" because of garbage collection.
static uint32 getUnusedMem()
	{
	return heap.memSize - (heap.objectSize + (heap.numHandles * sizeof(Hos)));
	}


static uint32 maxHeap = 0;//1000000;
static uint32 maxMemorySize =
#ifdef WINCE
	4*1024*1024;
#else
	32*1024*1024;
#endif

static void* AllocateInitialHeap(uint32* committedSize, uint32 reservedSize);

static int initObjectHeap(uint32 heapSize)
{
	originalHeapSize = heapSize = (heapSize/sizeof(Hos)+1)*sizeof(Hos);
	// NOTE: we must intiailize all the variables since after
	// a freeObjectHeap() we get called again
	heap.numHandles = 0;
	heap.numFreeHandles = 0;
	heap.memSize = heapSize;

	// align to 4 byte boundry for correct alignment of the Hos array

	heap.memSize = (heap.memSize + 3) & ~3;
	if (heap.memSize > maxMemorySize) maxMemorySize = heap.memSize;
	/*
#ifdef WINCE
	wsprintf((LPTSTR)sprintBuffer,L"Max: %i, Size %i",maxMemorySize,heapSize);
	mMessageBox(NULL,(LPTSTR)sprintBuffer,L"Init",MB_OK);
#endif
	*/
	// allocate and zero out memory region
	heap.mem = (uchar*)AllocateInitialHeap(&heap.memSize, maxMemorySize);//(uchar *)mMalloc(heap.memSize);
	if (heap.mem == NULL)
		return -1;
	xmemzero(heap.mem, heap.memSize);
	heap.hos = (Hos *)(&heap.mem[heap.memSize - sizeof(Hos)]);
	heap.objectSize = 0;
	return 0;
}

//
// Heap allocation.
//
#define USE_REALLOC
#ifndef WIN32
//
// NON-Windows version.
//
static void* AllocateInitialHeap(uint32* committedSize, uint32 reservedSize)
{
	return mMalloc(*committedSize);
}
#ifdef USE_REALLOC
void* ReallocHeapSpace(void *old, uint32* newSize)
{
	return realloc(old, *newSize);
}
#endif
void FreeHeapSpace(void* old)
{
	xfree(old);
}
void* AllocHeapSpace(uint32* newSize)
{
	return mMalloc(*newSize);
}
static uint32 fixHeapSize(uint32 size)
{
	return (size/sizeof(Hos)+1)*sizeof(Hos);
}
#else // WIN32
//
// Use Virtual memory for Windows and WinCE
//
static uint32 pageSize = 0;
static uint32 totalReservedSize = 0;
static uint32 totalCommittedSize = 0;
static uint32 maxCommit = 0;
static unsigned char * virtualMemory = NULL;
//
static uint32 roundToPage (uint32 value)
{
	return ((value+pageSize-1)/pageSize)*pageSize;
}
//
static uint32 fixHeapSize(uint32 size)
{
	if (size == 0) size = sizeof(Hos);
	size  = ((size+sizeof(Hos)-1)/sizeof(Hos))*sizeof(Hos);
	return size;
}
//
// Set the new committed memory size - either increasing or decreasing as needed.
// Returns the number of bytes now actually committed.
//
static uint32 commitInTotal(uint32 newCommitSize)
{
	uint32 willCommit = roundToPage(newCommitSize);
	if (willCommit > totalReservedSize) willCommit = totalReservedSize;
	if (willCommit == totalCommittedSize) return totalCommittedSize;
	else if (willCommit <= totalCommittedSize){
		uint32 toRelease = totalCommittedSize-willCommit;
		if (VirtualFree(virtualMemory+willCommit,toRelease,MEM_DECOMMIT)){
			totalCommittedSize = willCommit;
		}
	}else{
		uint32 toCommit = willCommit-totalCommittedSize;
		while(toCommit > 0){
			if (VirtualAlloc(virtualMemory+totalCommittedSize,toCommit,MEM_COMMIT,PAGE_READWRITE))
				break;
			toCommit -= pageSize;
		}
		totalCommittedSize += toCommit;
	}
	if (totalCommittedSize > maxCommit){
		maxCommit = totalCommittedSize;
	}
	return totalCommittedSize;
}
//
// Initialize the heap.
//
static void* AllocateInitialHeap(uint32* committedSize, uint32 reservedSize)
{
	SYSTEM_INFO si;
	void* virt = NULL;
	uint32 gotSoFar = 0;
	uint32 subtract;
	GetSystemInfo(&si);
	pageSize = si.dwAllocationGranularity;
	reservedSize = roundToPage(reservedSize);
	*committedSize = roundToPage(*committedSize);
	subtract = roundToPage(1024*1024);
	//
	while(reservedSize >= pageSize){
		virt = VirtualAlloc(NULL,reservedSize,MEM_RESERVE,PAGE_READWRITE);
		if (virt != NULL)
			break;
		if (subtract > reservedSize || reservedSize-subtract < subtract)
			subtract = pageSize;
		reservedSize -= subtract;
	}
	if (reservedSize < pageSize){
		return NULL;
	}
	//
	virtualMemory = (unsigned char *)virt;
	//
	totalReservedSize = reservedSize;
	//
	*committedSize = commitInTotal(*committedSize);
	//
	return virtualMemory;
}

void* ReallocHeapSpace(void* old,uint32* newSize)
{
	uint32 got = commitInTotal(*newSize);
	if (got < *newSize) return NULL;
	return virtualMemory;
}

void FreeHeapSpace(void* old)
{
	VirtualFree(virtualMemory,totalReservedSize,MEM_RELEASE);
}

void* AllocHeapSpace(uint32 *newSize)
{
	return NULL;
}
#endif
/*
#ifdef WINCE
#ifdef USE_REALLOC
void* ReallocHeapSpace(void *old, uint32 newSize)
{
	//return realloc(old, newSize);
	return NULL; //Always fail.
}
#endif
void FreeHeapSpace(void* old)
{
	//LocalFree(old); <- Fails at 8MB just like malloc.
	VirtualFree(old,0,MEM_RELEASE);
}
void* AllocHeapSpace(uint32 newSize)
{
	return VirtualAlloc(NULL,newSize,MEM_COMMIT,PAGE_READWRITE);
}
#else // not WINCE
#endif // not WINCE
*/


//#ifndef USE_REALLOC
static int moveHeapTo(uchar *newHeap,uint32 newSize)
{

	uint32 i;

	uint32 oldHeap = (uint32)heap.mem;
	uint32 oldSize = heap.memSize;
	uint32 handleSize = heap.numHandles*sizeof(Hos);
	Hos *hos;
	xmemzero(newHeap,newSize);
	memcpy(newHeap,heap.mem,heap.objectSize);
	memcpy(newHeap+newSize-handleSize,heap.mem+heap.memSize-handleSize,handleSize);
	heap.mem = newHeap;
	heap.memSize = newSize;
	heap.hos = (Hos *)(&heap.mem[heap.memSize - sizeof(Hos)]);
	for (hos = (Hos *)(heap.mem+heap.memSize),i = 0; i<heap.numHandles; i++){
		hos--;
		if (hos->ptr != NULL){
			uint32 offset = (uint32)hos->ptr-oldHeap;
			if (offset > heap.objectSize)
				exit(0); //Severe error!
			hos->ptr = (Var *)(newHeap+offset);
		}
	}

	xmemzero((uchar *)oldHeap,oldSize);
	FreeHeapSpace((uchar *)oldHeap);
	return 1;
}

static int allocAndMoveNewHeap(uint32 newSize)
{
	uchar *newHeap;
	uint32 ns = newSize;
	if (maxHeap != 0)
		if (newSize >= maxHeap)
			return 0;
	//
	// Not using the full size at the moment.
	//
	newHeap = (uchar *)AllocHeapSpace(&ns);
	if (newHeap == NULL) {
		return 0;
	}
	return moveHeapTo(newHeap,newSize);
}
//#else
static void refitHeap(uchar *newHeap,uchar *oldHeap,uint32 oldSize,uint32 newSize)
{
	uint32 handleSize = heap.numHandles*sizeof(Hos);
	if (oldSize != newSize)
		memmove(newHeap+newSize-handleSize,newHeap+oldSize-handleSize,handleSize);
	if (newSize > oldSize)
		xmemzero(newHeap+oldSize-handleSize,newSize-oldSize);
	if (oldHeap != newHeap){
		Hos *hos;
		uint32 i, oh = (uint32)oldHeap;
		for (hos = (Hos *)(newHeap+newSize),i = 0; i<heap.numHandles; i++){
			hos--;
			if (hos->ptr != NULL){
				uint32 offset = (uint32)hos->ptr-oh;
				if (offset > heap.objectSize)
					exit(0); //Severe error!
				hos->ptr = (Var *)(newHeap+offset);
			}
		}
	}
}
//#endif

static int resizeHeapTo(uint32 newSize);

static int getMoreHeapSpace(uint32 expandSize)
{
#ifdef USE_REALLOC
	uint32 newSize = fixHeapSize(heap.memSize+expandSize);
	uint32 ns = newSize;
	uchar *newHeap;
	if (maxHeap != 0)
		if (newSize >= maxHeap)
			return 0;
	newHeap = (uchar *)ReallocHeapSpace(heap.mem,&ns);
	if (newHeap == NULL)
		return allocAndMoveNewHeap(newSize);
		//return 0;
	refitHeap(newHeap,heap.mem,heap.memSize,newSize);
	heap.mem = newHeap;
	heap.memSize = newSize;
	heap.hos = (Hos *)(&heap.mem[heap.memSize - sizeof(Hos)]);
	return 1;
#else
	return allocAndMoveNewHeap(fixHeapSize(heap.memSize+expandSize));
		//resizeHeapTo(heap.memSize+expandSize);
#endif
}

static int resizeHeapTo(uint32 newSize)
{
#ifdef USE_REALLOC
	if (newSize < heap.memSize) {
		uchar *newHeap;
		uint32 ns = newSize = fixHeapSize(newSize);
		refitHeap(heap.mem,heap.mem,heap.memSize,newSize);
		newHeap = (uchar *)ReallocHeapSpace(heap.mem,&ns);
		if (newHeap == NULL){
			if (allocAndMoveNewHeap(newSize)) return 1;
			refitHeap(heap.mem,heap.mem,newSize,heap.memSize);
		}else{
			refitHeap(newHeap,heap.mem,newSize,newSize);
			heap.mem = newHeap;
			heap.memSize = newSize;
		}
		heap.hos = (Hos *)(&heap.mem[heap.memSize - sizeof(Hos)]);
		return 1;
	}else
		return getMoreHeapSpace(newSize-heap.memSize);
#else
	return allocAndMoveNewHeap(fixHeapSize(newSize));
#endif
}
static int shiftHeap()
{
	return resizeHeapTo(heap.memSize);
}

static void freeObjectHeap()
	{
#ifdef FREE_ON_EXIT
		{
		WObject obj;
		uint32 h;
		WClass *wclass;

		// call any native object destroy methods to free system resources
		for (h = 0; h < heap.numHandles; h++)
			{
			obj = h + FIRST_OBJ + 1;
			if (objectPtr(obj) != NULL)
				{
				wclass = WOBJ_class(obj);
				if (wclass != NULL && wclass->objDestroyFunc)
					wclass->objDestroyFunc(obj);
				}
			}
		}
#endif
	if (heap.mem)
		FreeHeapSpace(heap.mem);
	}

// MLB - WeakReference support.



WObject *wsets = NULL;
int wsetSize = 0;
int wsetNum = 0;
static int maxRefs = 0;

WObject *wrefs = NULL;
int wrefSize = 0;
int wrefNum = 0;

static void addRef(WObject obj)
{
	if (objectPtr(obj)[1].obj == 0) return;
	while (wrefNum >= wrefSize) {
		int ns = wrefSize+10;
		WObject *nw = (WObject *)malloc(sizeof(WObject)*ns);
		if (wrefs != NULL){
			int i;
			for (i = 0; i<wrefSize; i++) nw[i] = wrefs[i];
			free(wrefs);
		}
		wrefs = nw;
		wrefSize = ns;
	}
	wrefs[wrefNum++] = obj;
}

static void addSet(WObject obj)
{
	while (wsetNum >= wsetSize) {
		int ns = wsetSize+10;
		WObject *nw = (WObject *)malloc(sizeof(WObject)*ns);
		if (wsets != NULL){
			int i;
			for (i = 0; i<wsetSize; i++) nw[i] = wsets[i];
			free(wsets);
		}
		wsets = nw;
		wsetSize = ns;
	}
	wsets[wsetNum++] = obj;
}
static void removeSetReference(WObject set,int idx)
{
	int num = objectPtr(set)[2].intValue;
	if (idx < num && idx >= 0)	{
		WObject *refs = (WObject *)WOBJ_arrayStart(objectPtr(set)[1].obj);
		int k;
		for (k = idx; k<num-1; k++)
			refs[k] = refs[k+1];
		refs[num-1] = 0;
	}
	num--;
	objectPtr(set)[2].intValue = num;
}
static void addSetReference(WObject set,WObject ref)
{
	WObject ar = objectPtr(set)[1].obj;
	int num = objectPtr(set)[2].intValue;
	int len = WOBJ_arrayLen(ar);
	WObject *refs;
	if (ar == 0 || ref == 0) return;
	else{
		int i;
		refs = (WObject *)WOBJ_arrayStart(ar);

		for (i = 0; i<num; i++)
			if (refs[i] == ref) return;
	}
// Expand if necessary.
	if (num >= len){
		pushObject(ar);// Push the array so none of the objects can be gc'ed.
		{
			int nl = len+10;
			WObject na = createArrayObject(arrayType('L'),nl);
			if (na != 0){
				WObject *nrefs = (WObject *)WOBJ_arrayStart(na);
				int i;
				refs = (WObject *)WOBJ_arrayStart(ar);
				for (i = 0; i<len; i++) nrefs[i] = refs[i];
				for (;i<nl;i++) nrefs[i] = 0;
				objectPtr(set)[1].obj = na;
				len = nl;
			}
		}
		popObject();
	}
	if (num >= len) return; //If failed to expand.
// Add object.
	refs = (WObject *)WOBJ_arrayStart(objectPtr(set)[1].obj);
	refs[num++] = ref;
	objectPtr(set)[2].intValue = num;
}
static WObject getAllSetReferences(WObject set)
{
	int num = objectPtr(set)[2].intValue;
	WObject ar = objectPtr(set)[1].obj;
	WObject ret;
	pushObject(ar);

	ret = createArrayObject(arrayType('L'),num);
	if (ret){
		WObject *refs = (WObject *)WOBJ_arrayStart(ar);
		WObject *r = (WObject *)WOBJ_arrayStart(ret);
		int i;
		for (i = 0; i<num; i++) r[i] = refs[i];
	}
	popObject();
	return ret;
}
static int checkSetContains(WObject set,WObject lookFor)
{

	if (lookFor == 0) return FALSE;
	else{
		Var *setVars = objectPtr(set);
		int num = setVars[2].intValue;
		WObject ar = setVars[1].obj;
		WObject ret = 0;
		if (num == 0) return 0;
		else{
			WObject *refs = (WObject *)WOBJ_arrayStart(ar);
			int i = 0;
			for (i = 0; i<num; i++)
				if (refs[i] == lookFor) return TRUE;
		}
	}
	return FALSE;
}

static WObject findSetReference(WObject set,WObject finder)
{
	if (finder == 0) return 0;
	else{
		int num = objectPtr(set)[2].intValue;

		WObject ar = objectPtr(set)[1].obj;
		WObject ret = 0;
		if (num == 0) return 0;
		else{
			pushObject(ar);
			{
				WClass *cl = WOBJ_class(finder);
				WClassMethod *wm = getMethod(cl,createUtfString("lookingFor"),createUtfString("(Ljava/lang/Object;)Z"),&cl);
				if (wm != NULL){
					int i;
					Var pars[2], got;
					pars[0].obj = finder;
					for (i = 0; i<num; i++){
						WObject *refs = (WObject *)WOBJ_arrayStart(ar);
						pars[1].obj = refs[i];
						got.intValue = 0;
						executeMethodRet(cl,wm,pars,2,&got);
						if (thrownException) break;
						if (got.intValue != 0) {
							ret = refs[i];
							break;
						}
					}
				}
			}
			popObject();
		}
		return ret;
	}
}

static int removeASetReference(WObject set,WObject ref)
{
	int num = objectPtr(set)[2].intValue;
	if (num != 0){
		int j;
		for (j = 0;j<num;j++){
			WObject *refs = (WObject *)WOBJ_arrayStart(objectPtr(set)[1].obj);
			if (refs[j] == ref) {
				removeSetReference(set,j);
				return 1;
			}
		}
	}
	return 0;
}
static void clearAllSetReferences(WObject set)
{
	int num = objectPtr(set)[2].intValue;
	WObject *refs = (WObject *)WOBJ_arrayStart(objectPtr(set)[1].obj);
	int j;
	for (j = 0;j<num;j++) refs[j] = 0;
}
static Var wsIsEmpty(Var stack[])
{
	Var v;
	int num = objectPtr(stack[0].obj)[2].intValue;
	v.intValue = num == 0;
	return v;
}
static Var wsAdd(Var stack[])

{
	Var v;
	v.intValue = 0;

	addSetReference(stack[0].obj,stack[1].obj);

	return v;

}
static Var wsRemove(Var stack[])
{
	Var v;
	v.intValue = 0;
	removeASetReference(stack[0].obj,stack[1].obj);
	return v;
}
static Var wsGetRefs(Var stack[])
{
	Var v;
	v.obj = getAllSetReferences(stack[0].obj);
	return v;
}
static Var wsFind(Var stack[])
{
	Var v;
	v.obj = findSetReference(stack[0].obj,stack[1].obj);
	return v;
}
static Var wsContains(Var stack[])
{

	Var v;
	v.intValue = checkSetContains(stack[0].obj,stack[1].obj);
	return v;
}
static Var wsClear(Var stack[])
{

	Var v;
	v.intValue = 0;
	clearAllSetReferences(stack[0].obj);
	return v;
}
/*
static BOOL debugTheGC = FALSE;
static WObject iaToDebug = 0;
static BOOL debugIA(WObject ia_notUsed)
{
	if (iaToDebug == 0) return TRUE;
	else{
		BOOL ret = TRUE;
		WObject ia = iaToDebug;
		WObject data = objectPtr(ia)[1].obj;
		int len = objectPtr(ia)[2].intValue;
		int fullLength = WOBJ_arrayLen(data);
		int *s = (int*)WOBJ_arrayStart(data);
		int i = 0;
		for (i = 0; i<len; i++)
			if (s[i] == 0) break;
		if (i == len)
			ret = TRUE;
		else
			ret = FALSE;
		return ret;
	}
}
*/
static int notclearing(WObject obj){return 0;}
static int clearing(WObject obj)
{
	int i;
	int did = 0;
	for (i = 0; i < wrefNum; i++){
		Var* ptr = heap.hos[- (int32)(wrefs[i] - FIRST_OBJ - 1)].ptr;
		if (ptr[1].obj == obj)
			ptr[1].obj = 0;
	}
	//
	for (i = 0; i < wsetNum; i++){
		if (removeASetReference(wsets[i],obj)) {
			did++;
		}
	}
	return did;
}


static void clearRefs() {wrefNum = 0; wsetNum = 0;}

// mark bits in the handle order array since it is not used during

// the mark object process (its used in the sweep phase)


#define MAX_HEAP_HANDLES 0x7fffffff
#define ORDER_MASK 0x7fffffff

#define MARK_BIT 0x80000000

#define MARK(o) heap.hos[- (int32)(o - FIRST_OBJ - 1)].order |= MARK_BIT
#define UNMARK(o) heap.hos[- (int32)(o - FIRST_OBJ - 1)].order &= ~MARK_BIT
#define IS_MARKED(o) (heap.hos[- (int32)(o - FIRST_OBJ - 1)].order & MARK_BIT)


static BOOL isInHeap(WObject what)
{
//	UtfString className;
	unsigned int i;

	int g = 0;
	for (i = 0; i<heap.numHandles-heap.numFreeHandles; i++){
		int h = heap.hos[-(int32)i].order & ORDER_MASK;
		WObject obj = h + FIRST_OBJ + 1;
		if (what == obj) return 1;
		/*
		WClass *wclass = WOBJ_class(obj);
		int len = wclass->numVars;
		int j;
		for (j = 0; j < len; j++)
			{
			WObject o = WOBJ_var(obj, j).obj;
			if (VALID_OBJ(o) && objectPtr(o) != NULL && o == 0x21db)
				{
					if (wclass != NULL) {
						className = getUtfString(wclass, wclass->classNameIndex);
						markObject(obj);
					}
				}
			else
				wclass = NULL;
		}
		*/

	}
	return 0;
}
/*
WObject pleaseCheck = 0;
void doCheck()
{
	int i;
	if (pleaseCheck != 0)
		if (!IS_MARKED(pleaseCheck)){
			i = 0;
		}
}

WObject checkObject;
int findVar(int which,WClass *cl)

{
	int super = cl->numSuperClasses;
	int i;
	for (i = 0; i <= super; i++){
		WClass *wc = i == super ? cl : cl->superClasses[i];
		UtfString cc = WCLASS_className(wc);
		if (wc->numVars > which) {
			if (i != 0) which -= cl->superClasses[i-1]->numVars;
			return which;
		}
	}
	return 0;
}
*/
// mark this object and all the objects this object refers to and all
// objects those objects refer to, etc.
static void markObject(WObject obj)
	{
	WClass *wclass;
	WObject *arrayStart, o;
	uint32 i, len, type, numScan;

	if (!VALID_OBJ(obj) || objectPtr(obj) == NULL || IS_MARKED(obj))
		return;
	MARK(obj);
	//
	//if (obj == checkObject) debugString("Found it!");
	//
	numScan = 0;
markinterior:
	wclass = WOBJ_class(obj);
	if (wclass == NULL)
		{
		// array - see if it contains object references
		type = WOBJ_arrayType(obj);
		if (type == 1 || type == 2)
			{
			// for an array of arrays or object array
			arrayStart = (WObject *)WOBJ_arrayStart(obj);
			len = WOBJ_arrayLen(obj);
			for (i = 0; i < len; i++)
				{
				o = arrayStart[i];
				if (VALID_OBJ(o) && objectPtr(o) != NULL && !IS_MARKED(o))
					{
					MARK(o);
					//
					//if (o == checkObject) debugString("Found it in array!");
					//
					heap.hos[- (int32)numScan].temp = o;
					numScan++;
					}
				}
			}
		}
	// MLB - WeakReference support.
	else if (wclass == weakReferenceClass){
		addRef(obj);
	}else if (wclass == weakSetClass){
		Var *v = objectPtr(obj);
		addSet(obj);
		if (VALID_OBJ(v[1].obj))
			MARK(v[1].obj); // Mark the array but not the elements.
	}else
		{
		// object
		len = wclass->numVars;

		for (i = 0; i < len; i++)
			{
			o = WOBJ_var(obj, i).obj;
			if (VALID_OBJ(o) && objectPtr(o) != NULL && !IS_MARKED(o))
				{
				MARK(o);
				/*
				if (o == checkObject) {
					debugString("Found it in variable!");
					findVar(i,wclass);
				}
				*/
				heap.hos[- (int32)numScan].temp = o;
				numScan++;
				}
			}
		o = WCLASS_loader(wclass);
		if (VALID_OBJ(o) && objectPtr(o) != NULL && !IS_MARKED(o))
			{
			MARK(o);
			heap.hos[- (int32)numScan].temp = o;
			numScan++;
			}
		}

	if (numScan > 0)
		{
		// Note: we use goto since we want to avoid recursion here
		// since structures like linked links could create deep
		// stack calls
		--numScan;
		obj = heap.hos[- (int32)numScan].temp;
		goto markinterior;
		}
	}


/*
static void old_sweep()

	{
	WObject obj;
	WClass *wclass;
	uint32 i, h, objSize, prevObjectSize, numUsedHandles;
	uchar *src, *dst;

	prevObjectSize = heap.objectSize;
	heap.objectSize = 0;

	// move all the marks over into the scan array so we don't have
	// to do lots of bit shifting
	for (i = 0; i < heap.numHandles; i++)
		{
		if (heap.hos[- (int32)i].order & 0x80000000)
			{
			heap.hos[- (int32)i].order &= 0x7FFFFFFF; // clear mark bit
			heap.hos[- (int32)i].temp = 1;
			}
		else
			{
			heap.hos[- (int32)i].temp = 0;
			}
		}
	numUsedHandles = 0;
	for (i = 0; i < heap.numHandles; i++)
		{
		// we need to scan in memory order so we can compact things without
		// copying objects over each other
		h = heap.hos[- (int32)i].order;
		obj = h + FIRST_OBJ + 1;
		if (!heap.hos[- (int32)h].temp)
			{
			// handle is free - dereference object
			if (objectPtr(obj) != NULL)
				{
				wclass = WOBJ_class(obj);
				// for non-arrays, call objDestroy if present
				if (wclass != NULL && wclass->objDestroyFunc)
					wclass->objDestroyFunc(obj);


				heap.hos[- (int32)h].ptr = NULL;
				}
			continue;
			}
		wclass = WOBJ_class(obj);
		if (wclass == NULL)
			objSize = arraySize(WOBJ_arrayType(obj), WOBJ_arrayLen(obj));
		else

			objSize = WCLASS_objectSize(wclass);

		// copy object to new heap
		src = (uchar *)heap.hos[- (int32)h].ptr;
		dst = &heap.mem[heap.objectSize];
		if (src != dst)
			// NOTE: overlapping regions need to copy correctly
			xmemmove(dst, src, objSize);
		heap.hos[- (int32)h].ptr = (Var *)dst;
		heap.hos[- (int32)numUsedHandles].order = h;
		heap.objectSize += objSize;
		numUsedHandles++;
		}
	heap.numFreeHandles = heap.numHandles - numUsedHandles;
	for (i = 0; i < heap.numHandles; i++)
		if (!heap.hos[- (int32)i].temp)
			{
			// add free handle to free section of order array
			heap.hos[- (int32)numUsedHandles].order = i;
			numUsedHandles++;
			}
	// zero out the part of the heap that is now junk
	xmemzero(&heap.mem[heap.objectSize], prevObjectSize - heap.objectSize);

	}
*/
/*
static void old_gc()
	{
	WClass *wclass;
	WObject obj;
	uint32 i, j;

	// mark thrown exception
	if (VALID_OBJ(thrownException)) markObject(thrownException);
	// mark objects on vm stack
	for (i = 0; i < vmStackPtr; i++)
		if (VALID_OBJ(vmStack[i].obj))
			markObject(vmStack[i].obj);
	// mark objects on native stack
	for (i = 0; i < nmStackPtr; i++)
		if (VALID_OBJ(nmStack[i]))
			markObject(nmStack[i]);
	// mark all static class objects
	for (i = 0; i < CLASS_HASH_SIZE; i++)
		{
		wclass = classHashList[i];
		while (wclass != NULL)
			{
			for (j = 0; j < wclass->numFields; j++)
				{
				WClassField *field;

				field = &wclass->fields[j];
				if (!FIELD_isStatic(field))
					continue;
				obj = field->var.staticVar.obj;
				if (VALID_OBJ(obj))
					markObject(obj);
				}
			wclass = wclass->nextClass;
			}
		}
	sweep();
#ifdef DEBUGMEMSIZE
		debugMemSize();
#endif
	}
*/
// NOTE: There are no waba methods that are called when objects are destroyed.
// This is because if a method was called, the object would be on its way to
// being GC'd and if we set another object (or static field) to reference it,
// after the GC, the reference would be stale.

int checkGc = 0;
static int inGC = 0;
static UtfString finalizeName, finalizeDesc;

static int sweep()
	{
	WObject obj;
	WClass *wclass;
	uint32 i, h, objSize, prevObjectSize, numUsedHandles, freeSize;
	uchar *src, *dst;
	WObject refCleared = 0;
	int didFinalize = 0, needToFinalize = 0;

	// move all the marks over into the scan array so we don't have
	// to do lots of bit shifting
	for (i = 0; i < heap.numHandles; i++){
		/*
		if (i+FIRST_OBJ+1 == checkObject){
			if (heap.hos[- (int32)i].order & MARK_BIT)
				debugString("Was found");
			else
				debugString("Was not found");
		}
		*/
		if (heap.hos[- (int32)i].order & MARK_BIT)
			{
			heap.hos[- (int32)i].order &= ~MARK_BIT; // clear mark bit
			heap.hos[- (int32)i].temp = 0;
			}
		else{
			Var * optr = objectPtr(obj = i+FIRST_OBJ+1);
			heap.hos[- (int32)i].temp = 1;
#ifdef USE_FINALIZE
			if (optr != NULL){
				if (!WOBJPTR_isFinalized(optr)){
					WClass *cl = WOBJPTR_class(optr);

					WOBJPTR_setFinalized(optr);
					if (cl != NULL)
						if (cl->hasFinalizer)
							WOBJPTR_clearFinalized(optr);
				}
				if (!WOBJPTR_isFinalized(optr))
					needToFinalize = heap.hos[- (int32)i].temp = 2;
			}
#endif

/* A Bug in here?
			int h = heap.hos[- (int32)i].order & ORDER_MASK;
			Var *optr = heap.hos[-h].ptr;
			obj = h + FIRST_OBJ + 1;
			if (obj == pleaseCheck){
				int cc = IS_MARKED(obj);
				cc = 0;
			}

			heap.hos[- (int32)i].temp = 1;
#ifdef USE_FINALIZE
			if (optr != NULL){
				if (!WOBJPTR_isFinalized(optr)){
					WClass *cl = WOBJPTR_class(optr);
					WOBJPTR_setFinalized(optr);

					if (cl != NULL)
						if (cl->hasFinalizer)
							WOBJPTR_clearFinalized(optr);
				}
				if (!WOBJPTR_isFinalized(optr))
					needToFinalize = heap.hos[- (int32)i].temp = 2;
			}
#endif
*/
		}
	}

	while(needToFinalize){
		int did = 0;
		WObject obj;
		Var pars[1];
		WClassMethod *cm;
		WClass *wclass;
		if (finalizeName.len == 0){
			finalizeName = createUtfString("finalize");
			finalizeDesc = createUtfString("()V");
		}
		for (i = 0; i<heap.numHandles; i++){
			if (heap.hos[- (int32)i].temp == 2){
				heap.hos[- (int32)i].temp = 0; //Dont sweep it this time round.
				didFinalize = did = 1;
				obj = i+FIRST_OBJ+1;
				/*
				int h = heap.hos[- (int32)i].order & ORDER_MASK;
				obj = h + FIRST_OBJ + 1;
				*/
				WOBJPTR_setFinalized(objectPtr(obj));
				wclass = WOBJ_class(obj);
				if (wclass){
					cm = getMethod(wclass,finalizeName,finalizeDesc,&wclass);
					if (cm == NULL) continue;
					pars[0].obj = obj;

					executeMethod(wclass,cm,pars,1);
					handlingException = thrownException = 0;
					break;
				}
			}
		}
		if (!did) break;
	}

	// The finalize() call could conceivably bring classes that weren't referenced back into reference,
	//  therefore we cannot do a sweep now. A full mark (via doGC) must be done.

	if (didFinalize) return 0;

	//UtfString className;
	prevObjectSize = heap.objectSize;
	heap.objectSize = 0;

	numUsedHandles = 0;
	for (i = 0; i < heap.numHandles; i++)
		{
		//if (iaToDebug != 0) debugIA(0);
		// we need to scan in memory order so we can compact things without
		// copying objects over each other
		h = heap.hos[- (int32)i].order & ORDER_MASK;
		obj = h + FIRST_OBJ + 1;
		if (heap.hos[- (int32)h].temp)
			{
				Var* op;
				//UtfString className, what;
			// MLB - WeakReference support.
			if (clearing(obj))
				refCleared = obj;
			//if (iaToDebug != 0) debugIA(0);
			// handle is free - dereference object
			op = objectPtr(obj);
			if (op != NULL){
				wclass = WOBJ_class(obj);
				///* Defunct - used for testing.
				/*
				if (wclass != NULL && iaToDebug != 0){
					className = getUtfString(wclass, wclass->classNameIndex);
					//what = stringToUtf(obj,STU_USE_STATIC|STU_NULL_TERMINATE);
					//if (strncmp(className.str,"java/",5) == 0) checkGc = 2;
				}
				*/
				// for non-arrays, call objDestroy if present
				if (wclass != NULL && wclass->objDestroyFunc)
					wclass->objDestroyFunc(obj);
				//
				//if (iaToDebug != 0) debugIA(0);
				heap.hos[- (int32)h].ptr = NULL;
			}
			//if (iaToDebug != 0) debugIA(0);
			continue;
			}
		wclass = WOBJ_class(obj);
		if (wclass == NULL)
			objSize = arraySize(WOBJ_arrayType(obj), WOBJ_arrayLen(obj));
		else
			objSize = WCLASS_objectSize(wclass);

		// copy object to new heap
		src = (uchar *)heap.hos[- (int32)h].ptr;
		dst = &heap.mem[heap.objectSize];
		if (src != dst)
			// NOTE: overlapping regions need to copy correctly
			xmemmove(dst, src, objSize);
		heap.hos[- (int32)h].ptr = (Var *)dst;
		heap.hos[- (int32)numUsedHandles].order = h;
		heap.objectSize += objSize;
		numUsedHandles++;
		//if (iaToDebug != 0) debugIA(0);
		}
	heap.numFreeHandles = heap.numHandles - numUsedHandles;
	for (i = 0; i < heap.numHandles; i++)
		if (heap.hos[- (int32)i].temp)
			{
			// add free handle to free section of order array
			heap.hos[- (int32)numUsedHandles].order = i;
			numUsedHandles++;
			}
	// zero out the part of the heap that is now junk
	freeSize = prevObjectSize - heap.objectSize;
	xmemzero(&heap.mem[heap.objectSize], freeSize);

	//if (freeSize >= 1000) {
	//* Reduce Heap

	//if (freeSize >= originalHeapSize && heap.memSize >= originalHeapSize*2){
	if (freeSize >= heap.memSize/2){
		unsigned reduceTo = heap.memSize+outOfMemorySize+sizeof(Hos)-freeSize;
		if (reduceTo < originalHeapSize) reduceTo = originalHeapSize;
		if (reduceTo < heap.memSize)
			resizeHeapTo(reduceTo);
	}
	//*/
	return 1;
	}

void markExtraObjects();
void markConstants();

static void markStack(Var *stack,uint32 ptr)
{
	uint32 i;

	for (i = 0; i < ptr; i++)
		if (VALID_OBJ(stack[i].obj)){
			markObject(stack[i].obj);
		}else
			;
}
/*
static void markActiveStack(Var *stack,uint32 ptr,Var *max)
{
	Var *v;

	if (TRUE || max == NULL || max > stack+ptr || max < stack) markStack(stack,ptr);
	else

		for (v = stack; v<max+1; v++)
			if (VALID_OBJ((*v).obj))
				markObject((*v).obj);
			else
				;
}
*/
void checkSpecial()
{
	/*
	unsigned int i;
	for (i = 0; i<heap.numHandles-heap.numFreeHandles && 0; i++){
		int h = heap.hos[-(int32)i].order & 0x7fffffff;
		WObject obj = h + FIRST_OBJ + 1;
		WClass * cl = WOBJ_class(obj);
	}
	*/
}


static WObject ClassLoaders = 0;



/*
static MonitorEntry monitors;

MonitorEntry findMonitor(WObject monitor,VmContext holder)
{
	MonitorEntry me;
	for (me = monitors; me != NULL; me = me->next){
		if (monitor != 0) {
			if (me->monitor == monitor) return me;

		}else{
			if (me->holder == holder) return me;
		}
	}
	return NULL;
}

MonitorEntry addMonitor(WObject monitor,VmContext holder)
{
	MonitorEntry me = mMalloc(sizeof(struct monitor_entry));
	me->count = 1;
	me->holder = holder;
	me->monitor = monitor;

	me->next = monitors;
	me->waiting = 1;
	me->serving = 0;
	monitors = me;

	return me;
}

void removeMonitor(MonitorEntry me)
{
	if (me == NULL) return;
	if (monitors != NULL){
		if (monitors == me){
			monitors = me->next;
		}else{
			MonitorEntry prev = monitors;
			while (prev->next != NULL && prev->next != me)
				prev = prev->next;
			if (prev->next == me)
				prev->next = me->next;
		}
	}
	free(me);
}
//Returns 0 = success, otherwise returns a wait index.
int tryMonitorEnter(WObject monitor,VmContext holder)
{

	if (monitor == 0){
		throwException(NullPointerEx,NULL);
		return -1;
	}else{
		MonitorEntry me = findMonitor(monitor,NULL);
		if (me == NULL){
			addMonitor(monitor,holder);
			return 0;
		}else if (me->holder == holder){
			me->count++;
			return 0;
		}else{
			if (holder == NULL){
				throwException("java/lang/IllegalMonitorStateException","Only an mThread can wait on a monitor.");
				return 0;
			}
			return me->waiting++;
		}
	}
}
void monitorExit(WObject monitor,VmContext holder)
{
	MonitorEntry me = findMonitor(monitor,NULL);

	if (me == NULL){
		throwException("java/lang/IllegalMonitorStateException","The current thread does not hold the monitor.");
		return;
	}
	//Reduce my hold on the monitor.
	me->count--;
	//If I am still the holder then continue.
	if (me->count > 0) return;
	//I no longer hold it, I am ready to accept the next thread
	//that wishes to hold me.
	me->serving++;
	//If no one wishes to hold me then remove and delete myself.
	if (me->serving == me->waiting)
		removeMonitor(me);
	else
		monitorFreed(me);
}
*/

/*
static void debugIAs(WObject ias)
{
	WObject *s = (WObject*)WOBJ_arrayStart(ias);
	int num = WOBJ_arrayLen(ias);
	int i;
	for (i = 0; i<num; i++)
		debugIA(s[i]);
}

static void deepDebugDB(int value)
{
	static WClass* ddc = NULL;
	static WClassMethod* ddm = NULL;
	static Var ddp[1];

	if (ddc == NULL) {
		ddc = getClass(createUtfString("ewe/database/DatabaseManager"));
		if (ddc != NULL)
			ddm = getMethod(ddc,createUtfString("deepDebug"),createUtfString("(I)V"),NULL);
	}
	if (ddm != NULL){
		ddp[0].intValue = value;
		executeMethod(ddc,ddm,ddp,1);
	}
}
static void debugDB(WObject which)
{
	debugTheGC = TRUE;
	iaToDebug = which;
}
*/
static int doGC()
	{
	WClass *wclass, **hashList;
	WObject obj;
	uint32 i, j;
	int ret = 0;
	//MonitorEntry me;
	//char buff[20];
//	messageBox(TEXT("GC"),TEXT("GC"),0);
	// MLB - WeakReference support.
	//debugString("GC()");
	//MessageBox(NULL,TEXT("GC"),TEXT("GC"),MB_SETFOREGROUND|MB_OK);
	//MessageBox(NULL,TEXT("GC2"),TEXT("GC2"),MB_SETFOREGROUND|MB_OK);
	didGc++;
	if (inGC != 0) return 1;
	inGC++;

	//sprintf(buff,"GC:%d",didGc);
	//debugString(buff);
	clearRefs();

	//markObject(ClassLoaders);
	markObject(thrownException);
	markObject(handlingException);
	// mark objects on vm stack
//
//if (checkObject) debugString("Marking stack.");
//
	markStack(vmStack,vmStackPtr);
	//for (me = monitors; me != NULL; me = me->next) markObject(me->monitor);
	//markActiveStack(vmStack,vmStackPtr,stack);

	// mark objects on native stack
//
//if (checkObject) debugString("Marking native stack.");
//
	for (i = 0; i < nmStackPtr; i++)
		if (VALID_OBJ(nmStack[i]))
			markObject(nmStack[i]);

	// mark all static class objects
//
//if (checkObject) debugString("Marking static fields.");
//
	hashList = classHashList;
doAgain:
	for (i = 0; i < CLASS_HASH_SIZE; i++)
		{
		wclass = hashList[i];
		while (wclass != NULL)
			{
//			UtfString className = getUtfString(wclass,wclass->classNameIndex);
//			for (j = 0; j < wclass->numFields; j++)
			for (j = 0; j < wclass->initializedFields; j++)
				{
				WClassField *field;
				field = WCLASS_fieldPtr(wclass,j);
				if (!FIELD_isStatic(field)) continue;
				obj = GetStaticVarPointer(field)->obj;
				if (VALID_OBJ(obj))
					markObject(obj);
				}
			wclass = wclass->nextClass;
			}
		}
	if (hashList == classHashList) {
		hashList = loadedClassList;
		goto doAgain;
	}
	// mark all static class objects
//
//if (checkObject) debugString("Marking threaded stacks.");
//
	markExtraObjects();
	markConstants();
	ret = sweep();
	cleanOutLoadedClasses();
	inGC--;
	return ret;
#ifdef DEBUGMEMSIZE

		debugMemSize();
#endif
	}

static void gc()
{
	static char did[50];
	int i;
	//pleaseCheck = 0;
	//debugString("gc()");
	//if (1) return;
#ifdef USE_LOG
	Log("G",1);
#endif
	for (i = 0; !doGC(); i++)
		;
#ifdef USE_LOG

	Log("g",1);
#endif
	//sprintf(did,"%d, %d",heap.objectSize+heap.numHandles*sizeof(Hos),heap.memSize);
	//debugString(did);
	//sprintf(did,"gc: %d",i);
	//debugString(did);
}

static Var ClassLoaderCreate(Var stack[])
{
	Var v;
	v.obj = 0;
	objectPtr(stack[0].obj)[1].obj = ClassLoaders;
	ClassLoaders = stack[0].obj;
	return v;
}
static Var VmGetUsedClassMemory(Var stack[])
{
	Var v;
#ifdef WIN32
	v.intValue = classPartStorage->totalUsed;//totalClassHeapUsed+classHeapUsed;
#else
	v.intValue = totalClassHeapUsed+classHeapUsed;
#endif
	return v;
}
static Var VmGetUsedMemory(Var stack[])
{
	Var v;
	v.intValue = 0;
	if (stack[0].intValue) gc();
	v.intValue = heap.objectSize+heap.numHandles*sizeof(Hos);
	return v;
}

static Var VmCountObjects(Var stack[])
{
	Var v;
	v.intValue = 0;

	if (stack[0].intValue) gc();
	v.intValue = heap.numHandles-heap.numFreeHandles;
	return v;
}
static Var VmGetReferencedObjects(Var stack[])
{
	Var v, *r;
	int num = 0;
	unsigned int i;
	v.intValue = 0;
	gc();
	num = heap.numHandles-heap.numFreeHandles;
	v.obj = createArray("Ljava/lang/Object;",num);
	r = (Var *)WOBJ_arrayStart(v.obj);
	for (i = 0; i<heap.numHandles-heap.numFreeHandles; i++){
		int h = heap.hos[-(int32)i].order & ORDER_MASK;
		WObject obj = h + FIRST_OBJ + 1;
		if (obj == v.obj) continue;
		r[i].obj = obj;
	}
	return v;
}


static unsigned int maxObj = 0;

// NOTE: size passed must be 4 byte aligned (see arraySize())
static WObject allocObject(int32 size,int isOutOfMemory)
	{
	uint32 i, sizeReq, hosSize, obj, extra = 0;
	char *memoryError = NULL;
	static char me[256];
	uint32 min;
	uint32 normal;
	int gotMore;

	//gc(); For testing the robustness of the memory allocation and gc system.
	if (size <= 0)
		return 0;
	sizeReq = size;
	if (!heap.numFreeHandles){

		if (heap.numHandles == MAX_HEAP_HANDLES-1 && !isOutOfMemory){
			memoryError = "cannot create more object handles";
			goto out_of_memory;
		}
		sizeReq += sizeof(Hos);
	}
	hosSize = heap.numHandles * sizeof(Hos);
	if (!isOutOfMemory) extra = outOfMemorySize+sizeof(Hos);
	if (sizeReq + extra + hosSize + heap.objectSize > heap.memSize)
	{
		gc();
		/*
		if (hosSize+heap.objectSize < heap.memSize){
			uint32 fs = heap.memSize-(hosSize+heap.objectSize);
			if (fs < heap.memSize >> 2)
				getMoreHeapSpace(heap.memSize >> 2);
		}
		*/
		// heap.objectSize changed or we are out of memory
		while(sizeReq + extra + hosSize + heap.objectSize > heap.memSize){
			min = sizeReq + extra + hosSize + heap.objectSize - heap.memSize;
			normal = heap.memSize >> 2;
			gotMore = 0;
			if (normal > min) gotMore = getMoreHeapSpace(normal);
			if (!gotMore) gotMore = getMoreHeapSpace(min);
			if (!gotMore)
			{
out_of_memory:
				//VmQuickError(ERR_OutOfObjectMem);
				if (!isOutOfMemory){
#ifdef WIN32
					MEMORYSTATUS ms;
					GlobalMemoryStatus(&ms);
					//sprintf(me,"Used: %d, Need: %d, Available: %d",heap.memSize,heap.memSize+min,ms.dwAvailPhys);
					sprintf(me,"Used: %d, Need: %d",heap.memSize,heap.memSize+min);
					memoryError = me;
#endif
					throwException(OutOfMemoryEx,(char *)(memoryError == NULL ? "no more object memory":memoryError));
				}else{
					if (handlingException != 0)
						if (WOBJ_class(handlingException) == outOfMemoryClass)
							thrownException = handlingException;
				}
				//mMessageBox(NULL,L"Out of Memory - Please note the time.",L"Out of Memory",MB_SETFOREGROUND|MB_OK);
				return 0;
			}
		}
	}


	if (heap.numFreeHandles)
		{
		i = heap.hos[- (int32)(heap.numHandles - heap.numFreeHandles)].order;
		heap.numFreeHandles--;
		}
	else
		{
		// no free handles, get a new one
		i = heap.numHandles;
		heap.hos[- (int32)i].order = i;
		heap.numHandles++;
		}

	heap.hos[- (int32)i].ptr = (Var *)&heap.mem[heap.objectSize];
	heap.hos[- (int32)i].temp = 0;
	obj = FIRST_OBJ + i + 1;
	heap.objectSize += size;
	/*
	if (heap.numHandles-heap.numFreeHandles > maxObj+10){
		maxObj = heap.numHandles-heap.numFreeHandles;
		if (maxObj > 15000){
			sprintf(sprintBuffer,"MO: %i",maxObj);
			debugString(sprintBuffer);
		}
	}
	*/
	//
	return FIRST_OBJ + i + 1;
}

// NOTE: we made this function a #define and it showed no real performance
// gain over having it a function on either PalmOS or Windows when
// optimization was turned on.

static int isFree(WObject obj)
{
	int i;
	for (i = heap.numFreeHandles; i>0; i--){
		if (heap.hos[- (int32)(heap.numHandles - heap.numFreeHandles)].order == obj-1-FIRST_OBJ){
				if (heap.hos[- (int32)(obj - FIRST_OBJ - 1)].ptr != NULL)
					return 1;
		}
	}
	return 0;
}
static Var *objectPtr(WObject obj)
	{
		return heap.hos[- (int32)(obj - FIRST_OBJ - 1)].ptr;
	}

//
// Native Method Stack
//

static int getMoreNativeStack()
{
	uint32 ns = nmStackSize < 1 ? 100 : nmStackSize*2;
	WObject *newStack = (WObject *)mMalloc(ns*sizeof(WObject));
	if (newStack == NULL)
		return 0;
	memcpy(newStack,nmStack,sizeof(WObject)*nmStackSize);
	free(nmStack);

	nmStack = newStack;
	nmStackSize = ns;


	return 1;
}
static int pushObject(WObject obj)
	{
	// prevent the pushed object from being freed by the garbage
	// collector. Used in native methods and with code calling
	// the VM. For example, if you have the following code
	//
	// obj1 = createObject(...);
	// obj2 = createObject(...);
	//
	// or..
	//
	// obj1 = createObject(...);
	// m = getMethod(..)
	//
	// since the second statement can cause a memory allocation
	// resulting in garbage collection (in the latter a class
	// load that allocates static class variables), obj1
	// would normally be freed. Pushing obj1 onto the "stack"
	// (which is a stack for this purpose) prevents that
	//
	// the code above should be change to:
	//
	// obj1 = createObject(...);
	// pushObject(obj1);
	// obj2 = createObject(...);
	// pushObject(obj2);
	// ..
	// if (popObject() != obj2)
	//   ..error..
	// if (popObject() != obj1)
	//   ..error..
	//

	// NOTE: Running out of Native Stack space can cause serious program
	// failure if any code doesn't check the return code of pushObject().
	// Any code that does a pushObject() should check for failure and if
	// failure occurs, then abort.
	if (nmStackPtr >= nmStackSize)
		if (!getMoreNativeStack()){
			throwException("java/lang/VirtualMachineError","out of native stack space");
			//VmQuickError(ERR_NativeStackOverflow);
			return -1;
		}
	nmStack[nmStackPtr++] = obj;
	return 0;
	}


static WObject popObject()
	{
	if (nmStackPtr == 0)
		{
			throwException("java/lang/VirtualMachineError","native stack space underflow");

			//VmQuickError(ERR_NativeStackOverflow);
			return ((uint32)-1);
		}
	return nmStack[--nmStackPtr];
	}

//
// Native Methods and Hooks
//

typedef struct
	{
	char *className;
	ObjDestroyFunc destroyFunc;
	uint16 varsNeeded;
	} ClassHook;

#define IGNORE_CASE 0x1
#define STARTS_WITH 0x2
#define BACKWARDS 0x4
//===================================================================
int equals(int16 *big,int bigStart,int bigLength,int16 *smll,int smallStart,int smallLength,int options);
//===================================================================
//
// Convert
//

static double charsToDouble(WObject chars,int start,int length);

#ifdef UNIX

static Var ConvertCharsToDouble(Var stack[])
{
	Var v;
	WObject chars = stack[0].obj;
	double d = 0.0;
	int64 i = 0;
	if (chars != 0)
		d = charsToDouble(chars,stack[1].intValue,stack[2].intValue);
	i = *(int64 *)&d;
	v.half64 = (int32)(i & 0xffffffff);
	methodReturnHigh.half64 = (int32)((i >> 32) & 0xffffffff);
	return v;
}
static int64 charsToLong(WObject chars,int start,int length);
static Var ConvertCharsToLong(Var stack[])

{
	Var v;
	WObject chars = stack[0].obj;
	int64 i = 0;
	if (chars != 0)
		i = charsToLong(chars,stack[1].intValue,stack[2].intValue);
	v.half64 = (int32)(i & 0xffffffff);
	methodReturnHigh.half64 = (int32)((i >> 32) & 0xffffffff);
	return v;
}

static Var ConvertIntToString(Var stack[])
	{
	Var v;
	char buf[20];
	sprintf(buf, "%d", stack[0].intValue);
	v.obj = createString(buf);
	return v;
	}

static Var ConvertFloatToString(Var stack[])
	{
	Var v;
	char buf[40];
	sprintf(buf, "%f", stack[0].floatValue);
	v.obj = createString(buf);
	return v;
	}

static Var ConvertCharToString(Var stack[])
	{
	Var v;
	uint16 ch = (uint16)(stack[0].intValue & 0xffff);
	v.obj = createNewString(1,NULL,&ch);
/* MLB Bug fix
	char buf[2];
	buf[0] = (char)stack[0].intValue;
	buf[1] = 0;
	v.obj = createString(buf);
*/
	return v;
	}

static Var ConvertBooleanToString(Var stack[])
	{
	Var v;
	char *s;

	if (stack[0].intValue == 0)
		s = "false";
	else
		s = "true";
	v.obj = createString(s);
	return v;

	}

TCHAR *mainClassName = NULL;
TCHAR **arguments = NULL;
int numArguments = 0;

#ifdef UNIX
static WObject createStringFromNativeText(TCHAR *s, int32 len)
	{
	WObject obj;
	uint16 *charStart;
	int32 i;
	int smaller = sizeof(TCHAR) < sizeof(uint16);
	if (len < 0) len = textLength(s);
	obj = createNewString(len,&charStart,NULL);
	if (obj)
		for (i = 0; i < len; i++){
			charStart[i] = (uint16)s[i];
			if (smaller) charStart[i] &= 0xff;
		}
	return obj;
}
static int asciiToNativeText(char *src, TCHAR *dst, int max)
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


static TCHAR *stringToNativeText(WObject string)
{
	if (string == 0) return NULL;
	else{
		int len = WOBJ_StringLength(string);
		TCHAR *ret = (TCHAR *)malloc(sizeof(TCHAR)*(len+1));
		WCHAR *str = WOBJ_StringChars(string);
		int i = 0;
		for (i = 0; i<len; i++)
			ret[i] = (TCHAR)str[i];
		ret[i] = 0;
		return ret;
	}
}

static TCHAR *stringToTextInPlace(WObject string,int *length,int *freeMe)
{
	*length = *freeMe = 0;
	if (string == 0) return NULL;
	*freeMe = 1;
	*length = WOBJ_StringLength(string);
	return stringToNativeText(string);
}

static char *nativeTextToAscii(TCHAR *string,int *created)
{
	if (created != NULL) *created = 0;
	if (sizeof(TCHAR) == 1) return (char *)string;
	else{
		int len = textLength(string),i = 0;
		char *ret = (char *)malloc(len+1);
		if (created != NULL) *created = 1;
		for (i = 0; i<len; i++) ret[i] = (char)string[i];
		ret[i] = 0;
		return ret;
	}
}
#endif

Var VmGetProgramArguments(Var stack[])
{
	Var v;
	WObject mapars = createArrayObject(arrayType('L'),numArguments);
	int i;
	v.obj = mapars;
	if (!mapars) return v;
	WOBJ_arrayComponent(mapars) = tryGetClass(createUtfString("java/lang/String"));
	pushObject(mapars);
	for (i = 0; i<numArguments; i++){
		WObject str = createStringFromNativeText(arguments[i],textLength(arguments[i]));
		WObject *where = (WObject *)WOBJ_arrayStart(mapars)+i;
		*where = str;
	}
	popObject();
	v.obj = mapars;
	return v;
}

extern ClassHook classHooks[];
void exitSystem(int value);

static Var MainWinExit(Var stack[])
	{
	Var v;
	int exitCode;

	exitCode = stack[1].intValue;
	exitSystem(exitCode);
	v.obj = 0;
	return v;
	}

#ifndef INFINITE
#define INFINITE -1
#endif

static TCHAR localeData[20] = TEXT("");

#endif

#if defined(PALMOS)
#include "nmpalm_c.c"
#elif defined(WIN32)
#include "nmwin32_c.c"
#elif defined(UNIX)
#include "nmunix_c.c"
#endif

typedef struct
	{
	uint32 hash;
	NativeFunc func;
	} NativeMethod;


//MLB
static Var ObjectGetClass(Var stack[]);
static Var ClassGetName(Var stack[]);
static Var ClassGetModifiers(Var stack[]);
//static Var ClassIsAssignableFrom(Var stack[]);
static Var ClassIsInstance(Var stack[]);
static Var ClassGetInterfaces(Var stack[]);
static Var ClassGetDeclaredClasses(Var stack[]);
static Var ClassIsInterface(Var stack[]);
static Var ClassGetSuperClass(Var stack[]);
static Var ClassGetClassLoader(Var stack[]);
static Var ArrayGetSetElement(Var stack[]);
static Var ReflectNativeCreate(Var stack[]);
static Var ReflectIsInstance(Var stack[]);
//static Var ReflectIsAssignableFrom(Var stack[]);
//static Var ReflectIsTypeOf(Var stack[]);
static Var ReflectIsTypeOf2(Var stack[]);
static Var ReflectIsArray(Var stack[]);
static Var ReflectArrayLength(Var stack[]);
static Var ReflectNewArrayInstance(Var stack[]);
static Var ReflectGetFields(Var stack[]);
static Var ReflectGetConstructors(Var stack[]);

static Var ReflectGetMethodsOrConstructors(Var stack[]);
static Var ReflectGetForName(Var stack[]);
static Var ReflectGetReflectedClass(Var stack[]);
static Var ReflectGetForObject(Var stack[]);
static Var ReflectGetSuperClass(Var stack[]);

static Var ReflectGetNumberOfInterfaces(Var stack[]);
static Var ReflectGetInterface(Var stack[]);
static Var ReflectGetField(Var stack[]);
static Var ReflectGetMethodConstructor(Var stack[]);
static Var ReflectNewArray(Var stack[]);
static Var FieldGetValue(Var stack[]);
static Var FieldSetValue(Var stack[]);

static Var MethodInvoke(Var stack[]);
static int VarToWrapper(WObject wrapper,Var *var);
static Var TrueMethodInvoke(Var stack[],int *params,void **toFree,WClass **wclass,WClassMethod **method);
static Var MethodConstructorGetThrows(Var stack[]);
static Var ConstructorNewInstance(Var stack[]);
static Var TrueConstructorNewInstance(Var stack[],int *params,void **toFree,WClass **wclass,WClassMethod **method);

static Var StringEquals(Var stack[]);
static Var VectorInsert(Var stack[]);
static Var SubStringEquals(Var stack[]);

static Var SubStringIndexOf(Var stack[]);
static Var SubStringCompare(Var stack[]);
static Var UtilsSort(Var stack[]);
static Var UtilsGetIntSequence(Var stack[]);

static Var LocaleChangeCase(Var stack[]);
static Var LocaleChangeCaseArray(Var stack[]);
static Var LocaleCompareChar(Var stack[]);
static Var LocaleCompareString(Var stack[]);
static Var CoroutineCreate(Var stack[]);
static Var CoroutineJoin(Var stack[]);
static Var CoroutineInterrupt(Var stack[]);
static Var CoroutineSleep(Var stack[]);
static Var CoroutineGetCurrent(Var stack[]);
static Var CoroutineWakeup(Var stack[]);
static Var displayLineSpecsCalculate(Var stack[]);
static Var displayLineSpecsGetWidth(Var stack[]);
static Var VmCallInSystemQueue(Var stack[]);


int cgc = 0;

static Var VmGc(Var stack[])
{
	Var v;
	checkGc = ++cgc;
	gc();
	checkGc = 0;
	v.intValue = 0;
	return v;
}
int dummy[10];
/*
int *checkLink(int *size,int attempt,WObject toCheck,WObject linkTo,int depth,int maxDepth)
{
	WClass *wc = WOBJ_class(toCheck);
	MARK(toCheck);
	if (wc == NULL){
		int num = WOBJ_arrayLen(toCheck);
		int type = WOBJ_arrayType(toCheck);
		int i = 0;
		if (type == 1 || type == 2){
			WObject *arrayStart = (WObject *)WOBJ_arrayStart(toCheck);
			for (i = 0; i<num; i++){
				WObject o = arrayStart[i];
				if (o == linkTo) {
					*size = depth;
					return dummy;
				}
				if (VALID_OBJ(o) && objectPtr(o) != NULL && !IS_MARKED(o) && depth != maxDepth){
					int * got = checkLink(size,attempt,o,linkTo,depth+1,maxDepth);
					if (got != NULL) {

						UNMARK(toCheck);
						return got;
					}
				}
			}
		}

	}else{
		UtfString str = WCLASS_className(wc);
		int i = 0;
		for (i = 0; i<wc->numVars; i++){
			WObject o = objectPtr(toCheck)[i+1].obj;
			if (o == linkTo) {
				findVar(i,wc);
				*size = depth;
				UNMARK(toCheck);
				return dummy;
			}
			if (VALID_OBJ(o) && objectPtr(o) != NULL && !IS_MARKED(o) && depth != maxDepth){
				int * got = checkLink(size,attempt,o,linkTo,depth+1,maxDepth);
				if (got != NULL) {
					findVar(i,wc);
					UNMARK(toCheck);
					return got;
				}
			}
		}
	}
	UNMARK(toCheck);
	return NULL;
}
*/
static Var VmDebug2(Var stack[])
{
	int size = 0;
	int *wp = (int *)WOBJ_arrayStart(stack[0].obj);
	return returnVar(size);
}
static Var VmDebugObject(Var stack[])
{
	int options = stack[1].intValue;
	//if (options == 1000) debugDB(stack[0].obj);
	return returnVar(0);
}
#ifdef WIN32
static Var VmDebug(Var stack[])
{
	WObject out = stack[0].obj;
	int options = stack[1].intValue;
	Var v;
	debugged = 1;
	v.intValue = 0;
	if (options == -1) {
		shiftHeap();
		return v;
	}
#ifndef WINCE
	if (options == 100){
		/*
		UtfString u = stringToUtf(out,STU_USE_STATIC|STU_NULL_TERMINATE);
		gc();
		AllocConsole();
		sprintf(sprintBuffer,"%s %d, %d\r\n",u.str,heap.numHandles-heap.numFreeHandles,heap.objectSize);
		cputs(sprintBuffer);
		*/
		debugHeld();
	}else
	if (out != 0){
		UtfString u = stringToUtf(out,STU_USE_STATIC|STU_NULL_TERMINATE);
		AllocConsole();
		cputs(u.str);
		cputs("\r\n");
	}else{
		if (options != 0){
			char objs[20];
			sprintf(objs,"<%d>",options);
			debugString(objs);
		}
	}
#endif
	return v;
}
#else
static Var VmDebug(Var stack[])
{
	WObject out = stack[0].obj;
	int options = stack[1].intValue;
	Var v;
	v.intValue = 0;
	if (options == -1) {
		shiftHeap();
		return v;
	}

	if (out != 0){
		UtfString u = stringToUtf(out,STU_USE_STATIC|STU_NULL_TERMINATE);
		debugString(u.str);
	}
	return v;
}
#endif

#ifdef UNIX

static Var VmSetParameter(Var stack[])
{
	Var v;
	int which = stack[0].intValue;
	int value = stack[1].intValue;
	v.intValue = 0;
	switch(which){
		/*
	case 1:
		v.intValue = VmTimerInterval;
		VmTimerInterval = which;
		if (VmTimerID != 0) requestVmTimer(which);
		break;
		*/
	case 2:
		SimulateSip = value;
		break;
	case 5:
		UseSip = value;
		/*
	case 3:
		maxTimerEntries = value;
		break;
		*/
	}
	return v;
}
#if !defined(QTOPIA)

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
	Var v;
	WObject winObj = stack[1].obj;
	//WClassMethod *method;
	//Var params[7];
	int par = stack[0].intValue;
	int32 type = 901, width = 0, height = 0;
	//HWND hwnd = winObj == 0 ? NULL : WOBJ_WindowHWnd(winObj);
	v.intValue = 0;
	if (SipFrozen && !(par & 0x10)) return v;
	if (par & 0x8) {
		SipLocked = 0;
		SipFrozen = 1;
	}else if (par & 0x10){
		SipFrozen = SipLocked = 0;
		return v;
	}
	if (!(par & 3)) {//Requesting to switch it off.
		if (!(par & 4) && SipLocked) return v;
		SipLocked = 0;
		if (!sipOn) return v;
		sipOn = 0;
	}else{ //Requesting to switch it on.
		SipLocked |= (par & 4);
		if (par & 1){
			if (sipOn) return v;
			sipOn = 1;
			/*
			RECT r;
			GetClientRect(curHWnd,&r);
			*/
			type = 900;
			width = 240;
			height = 320-100;
			//width = r.right-r.left;
			//height = r.bottom-r.top-80;
		}else return v;
	}
	if ((SimulateSip & 0x1) && winObj != 0){
		WClass* vclass = tryGetClass(createUtfString("ewe/ui/WindowHelper"));
		WClassMethod* method = NULL;
		if (vclass != NULL) method = getMethod(vclass,createUtfString("postToWindow"),
			createUtfString("(Lewe/ui/Window;IIIIII)V"),NULL);
		if (method != NULL){
			Var params[7];
			params[0].obj = winObj;
			params[1].intValue = type; // type
			params[2].intValue = 240; // key = Desktop Height
			params[3].intValue = width; // x = Visible Width
			params[4].intValue = height; // y  = Visible Height
			params[5].intValue = 0; // modifiers
			params[6].intValue = getTimeStamp(); // timeStamp
			executeTopMethod(vclass, method, params, 7);
		}
	}
	return v;
}
#endif

static int siged = 0;
void alarmed(int sig)
{
}

static void msleep(int milliseconds)
{
#if defined(QTOPIA) || defined(QT)
	int now = getTimeStamp();
	while(timeDifference(now,getTimeStamp()) < milliseconds)
		;
#else
	if (milliseconds == 0) return;
	struct itimerval tv;
	struct sigaction act;
	if (!siged) {
		act.sa_handler = alarmed;
		sigaction(SIGALRM,&act,NULL);
	}
	siged = 1;
	tv.it_interval.tv_sec = 0;
  tv.it_interval.tv_usec = 0;
	tv.it_value.tv_sec = milliseconds/1000;
  tv.it_value.tv_usec = (milliseconds % 1000)*1000;
	setitimer(ITIMER_REAL,&tv,NULL);
	pause();
#endif
}
#endif

static Var VmSleep(Var stack[])
{
	msleep(stack[0].intValue);
	return returnVar(0);
}


//#############################################################################
// DOUBLE VALUES
//#############################################################################
static char decimalPoint = 0, plusSign = 0, negSign = 0, groupPoint = 0;
static void checkSymbols()
{
	if (decimalPoint == 0) {
		char symbols[4] = { '.', '+', '-', ',' };
		getLocalSymbols(symbols,4);
		decimalPoint = symbols[0];
		plusSign = symbols[1];
		negSign = symbols[2];
		groupPoint = symbols[3];
	}
}

#define POSITIVE_INFINITY_VALUE cINT64(0x7ff0000000000000L)
#define NEGATIVE_INFINITY_VALUE cINT64(0xfff0000000000000L)
#define NAN_VALUE cINT64(0x7ff8000000000000L)
#define FLOAT_NAN_VALUE (0x7fc00000)

#ifdef UNIX
#define _isnan isnan
#endif

BOOL _floatbitsisnan(int bits)
{
	if ((bits & 0x7f800000) != 0x7f800000) return FALSE;
	return ((bits & 0x007fffff) != 0);
}
//
// Convert
//
static int floatToBits(float value, BOOL collapseNan)
{
	int ret;
	*((float*)(&ret)) = value;
	if (collapseNan && _floatbitsisnan(ret))
		ret = FLOAT_NAN_VALUE;
	return ret;
}
static float bitsToFloat(int value)
{
	float ret;
	*((int*)(&ret)) = value;
	if (_floatbitsisnan(value) && (value != FLOAT_NAN_VALUE))
		*((int*)(&ret)) = FLOAT_NAN_VALUE;
	return ret;
}

static int64 doubleToBits(double value, BOOL collapseNan)
{
	Var v[2];
	int64 ret;
	double2vars(value,v);
	ret = vars2int64(v);
	if (collapseNan && _isnan(value))
		ret = NAN_VALUE;
	return ret;
}
static double bitsToDouble(int64 value)
{
	Var v[2];
	double ret;
	int642vars(value,v);
	ret = vars2double(v);
	if (_isnan(ret) && (value != NAN_VALUE)){
		int642vars(NAN_VALUE,v);
		ret = vars2double(v);
	}
	return ret;
}
static Var ConvertDoubleToBits(Var stack[])
{
	return returnLong(doubleToBits(vars2double(stack),stack[2].intValue == 0));
}
static Var ConvertBitsToDouble(Var stack[])
{
	return returnDouble(bitsToDouble(vars2int64(stack)));
}
static Var ConvertFloatToBits(Var stack[])
{
	Var v;
	v.intValue = floatToBits(stack[0].floatValue,stack[1].intValue == 0);
	return v;
}
static Var ConvertBitsToFloat(Var stack[])
{
	Var v;
	v.floatValue = bitsToFloat(stack[0].intValue);
	return v;
}

static char *getDecimalDouble(char *from,double *dest)
{
	double ret = 0;
	double div = 10;

	double neg = 1;
	char *s = from;

	checkSymbols();
	*dest = 0;
	if (from == 0) return s;
	while(*s == ' ') s++;
	if (*s == negSign) neg = -1, s++;
	else if (*s == plusSign) neg = 1, s++;
	for(;*s != 0;s++){
		char c = *s;
		if (c == groupPoint) continue;
		if (c<'0' || c >'9') break;
		ret = (ret*10.0)+(double)(c-'0');
	}
	if (*s != decimalPoint){
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

static struct byte_data convertData;
static int64 stringCharsToLong(uint16 *chars,int len);

static int64 charsToLong(WObject chars,int start,int length)
{
	if (start < 0 || start+length > WOBJ_arrayLen(chars)) return 0;
	return stringCharsToLong((uint16 *)WOBJ_arrayStart(chars)+start,length);
}
static double charsToDouble(WObject chars,int start,int length)
{
	double ret = 0, exp = 0;
	char *s;
	if (start < 0 || start+length > WOBJ_arrayLen(chars)) return 0.0;
	stringDataToUtf8((uint16 *)WOBJ_arrayStart(chars)+start,length,&convertData,0);
	s = getDecimalDouble(convertData.data,&ret);
	if (*s != 'e' && *s != 'E') return ret;
	getDecimalDouble(s+1,&exp);
	ret = ret*pow(10,exp);
	return ret;
}

static double stringToDouble(WObject string)
{
	UtfString str;
	double ret = 0, exp = 0;
	char *s;

	if (string == 0)
		return 0;
	str = stringToUtf(string,STU_USE_STATIC|STU_NULL_TERMINATE);

	s = getDecimalDouble(str.str,&ret);
	if (*s != 'e' && *s != 'E') return ret;
	getDecimalDouble(s+1,&exp);
	ret = ret*pow(10,exp);
	return ret;
}

#define ZERO_FILL 0x1
#define TRUNCATE 0x2
#define HEX 0x4
#define FREE_DECIMAL 0x8
#define EXP_NOTATION 0x10
#define NO_EXP_NOTATION 0x20
#define AT_LEAST_ONE_DECIMAL 0x40


static double ln10 = 0;
static double maxF = 0, minF = 0;


int lengthOfLong(int64 value)
{
	int len = 1;
	if (value < 0) value *= -1;
	if (value != 0){
		int64 cur = value;
		while(1){
			cur /= 10;
			if (cur == 0) break;
			len++;
		}
	}
	return len;
}

#define bits(VAL) (*(int64 *)(&VAL))

//===================================================================
static double nearZero(double val)
//===================================================================
{
	double value = val;
	if (value < 0) value *= -1;
	if (value < 1e-15) return 0;
	return val;
}
//===================================================================
static double nearInf(double val)
//===================================================================
{
	double value = val;
	if (value < 0) value *= -1;
	if (value > 1e+15) {
		bits(value) = val < 0 ? NEGATIVE_INFINITY_VALUE : POSITIVE_INFINITY_VALUE;
		return value;
	}
	return val;
}

static char Infinity [] = "+Inf";
static char NaN [] = "NaN";

//============================================================
void doubleToString(char dest[],double val,int totalLength,int decimal,int options)
//============================================================
{
	int neg = val<0;
	int64 pre;
	int frontLength = 1,digits = 0;
	int needLength = 0;
	char *d = dest;
	int64 div = 10;
	int64 cur;
	int zeroFill = (options & ZERO_FILL) != 0, doSci = 0;
	double original;


	if (neg) val *= -1;
	original = val;
	if (decimal < 0) {
		decimal = -decimal;
		options |= FREE_DECIMAL;
	}

	checkSymbols();

	if (maxF == 0){
		maxF = pow(2,60);
		minF = pow(2,-50);
		ln10 = log(10.0);
	}


	if (_isnan(val)){
		if (3 > totalLength && totalLength != 0) goto cant_fit;
		strcpy(dest,NaN);
		return;
	}else if (bits(val) == POSITIVE_INFINITY_VALUE || bits(val) == NEGATIVE_INFINITY_VALUE){
		if (4 > totalLength && totalLength != 0) goto cant_fit;
		strcpy(dest,Infinity);
		dest[0] = neg ? negSign : plusSign;
		return;
	}

trySci:
	if (val != 0)
	// Should we do Sci. Notation?
	if (doSci || ((val >= maxF  || val <= minF || ((options & EXP_NOTATION) != 0)) && ((options & NO_EXP_NOTATION) == 0))) {
	// Have to do e version.
		double power = log(val)/ln10;
		double man;
		int negpow = power < 0;
		if (negpow) power *= -1;
		if (negpow){
			man = 1-(power-(int64)power);
			power = (double)(1+(int64)power);
			if (man >= 1) {
				man -= 1;
				power -= 1;
			}
		}else{
			man = power-(int64)power;
			power = (double)(int64)power;
		}
		man = pow(10,man);
		if (neg) man *= -1;
		if (totalLength == 0) {
			doubleToString(dest,man,0,decimal,options|NO_EXP_NOTATION|AT_LEAST_ONE_DECIMAL);
		}else{
			int expPart = 2+lengthOfLong((int64)power);
			if (expPart >= totalLength) goto cant_fit;
			doubleToString(dest,man,totalLength-expPart,decimal,options|NO_EXP_NOTATION|AT_LEAST_ONE_DECIMAL);
		}
		if (*dest == '#') goto cant_fit;
		while(*dest != 0) dest++;
		*dest++ = 'e';
		*dest++ = negpow ? negSign : plusSign;

		doubleToString(dest,power,0,0,NO_EXP_NOTATION);
		return;
	}

	if (1/*(options & FREE_DECIMAL) != 0*/){
		int places = 0;
		double dc = val-(int64)val;
		int i = 1;
		int d = 20;
		if (decimal != 0) d = decimal;
		for (i = 1; i<=d; i++){
			dc *= 10.0;
			if (dc >= 1.0) places = i;
			dc -= (int64)dc;
		}
		if ((places == 0) && ((int64)val == 0) && (original != 0.0) && ((options & NO_EXP_NOTATION) == 0)){
			doSci = 1;
			goto trySci;
		}
		if ((options & FREE_DECIMAL) != 0) decimal = places;
	}
// Round up.
	if ((decimal == 0) && ((options & AT_LEAST_ONE_DECIMAL) != 0)) decimal = 1;

	if ((options & TRUNCATE) == 0){
		double add = 0.5555555555;
		int i = 0;
		for (i = 0; i<decimal; i++) add /= 10.0;
		val += add;
	}

//Count found decimal places.

	if ((options & FREE_DECIMAL) != 0){
		int places = 0;
		double dc = val-(int64)val;
		int i = 1;
		for (i = 1; i<=decimal; i++){
			dc *= 10.0;
			if (dc >= 1.0) places = i;
			dc -= (int64)dc;
		}
		decimal = places;
	}
	if ((decimal == 0) && ((options & AT_LEAST_ONE_DECIMAL) != 0)) decimal = 1;
	pre = (int64)val;
	if (pre != 0){
		cur = pre;
		while(1){

			cur -= cur%div;
			if (cur == 0) break;
			div *= 10;
			frontLength++;
		}
	}
	digits = frontLength;
	if (neg) frontLength++;
	needLength = frontLength;
	if (decimal > 0) needLength += decimal+1;
	if (needLength > totalLength && totalLength > 0) {
		int extra = needLength-totalLength;
		if (((options & FREE_DECIMAL) == 0) || (extra > decimal-1))

			goto cant_fit;

		decimal -= extra;
	}
	if (totalLength > 0){
		int padLength = totalLength-(decimal > 0 ? decimal+1 : 0)-frontLength;
		char padChar = zeroFill ? '0':' ';
		int i = 0;

		if (zeroFill)
			if (neg) *d++ = negSign;
			//else *d++ = plusSign;
		for (i = 0; i<padLength; i++) *d++ = padChar;
		if (!zeroFill)
			if (neg) *d++ = negSign;
			//else *d++ = plusSign;
	}else{
		if (neg) *d++ = negSign;
		//else *d++ = plusSign;
	}
	for (cur = pre, div /= 10;div >=1 ; div /= 10){
		int dig = (int)(cur/div);
		cur -= dig*div;
		*d++ = '0'+dig;
	}
	if (decimal != 0) {
		int i;
		*d++ = decimalPoint;
		val -= (int64)val;
		for (i = 0; i<decimal; i++){
			val *= 10;
			*d++ = '0'+(int)(((int64)val)%10);
			val -= (int64)val;
		}
	}
	*d++ = 0;
	return;
cant_fit:
	{
		int i;
		for (i = 0; i<totalLength; i++) dest[i] = '#';
		dest[totalLength] = 0;
		return;
	}
}
WClass* doubleClass = NULL;
static WClass * getDoubleClass()
{
	if (doubleClass == NULL)
	doubleClass = getClass(createUtfString("ewe/sys/Double"));
	return doubleClass;
}
static int isADouble(WObject obj)
{
	if (obj == 0) return 0;
	return compatible(WOBJ_class(obj),doubleClass);
}
void setDouble(WObject obj,double d)
{
	if (obj == 0) return;
	double2vars(d,objectPtr(obj)+1);
	//*((double *)(objectPtr(obj)+1)) = d;
}
void setDoubleAsBits(WObject obj,int64 bits)
{
	if (obj == 0) return;
	int642vars(bits,objectPtr(obj)+1);
	//*((int64 *)(objectPtr(obj)+1)) = bits;
}
double getDouble(WObject obj)
{
	if (obj == 0) return 0;
	return vars2double(objectPtr(obj)+1);
	//return *((double *)(objectPtr(obj)+1));
}

int64 getLong(WObject obj);

int64 getDoubleAsBits(WObject obj)
{
	if (obj == 0) return 0;
	return vars2int64(objectPtr(obj)+1);
	//return *((int64 *)(objectPtr(obj)+1));
}

static Var setADouble(Var doub,double value)
{
	setDouble(doub.obj,value);
	return doub;
}
static Var setAndReturn(WObject doub,double value)
{
	Var v;
	if (doub == 0) {
		getDoubleClass();
		doub = createObject(doubleClass);
	}
	v.obj = doub;
	return setADouble(v,value);
}
static Var setAndReturnBits(WObject doub,int64 bits)
{
	Var v;
	if (doub == 0) {
		getDoubleClass();
		doub = createObject(doubleClass);
	}
	v.obj = doub;
	setDoubleAsBits(doub,bits);
	return v;
}
/*
static Var DoubleSetFloat(Var stack[]) {return setADouble(stack[0],(double)stack[1].floatValue);}
static Var DoubleSetInt(Var stack[]){return setADouble(stack[0],(double)stack[1].intValue);}
static Var DoubleSetDouble(Var stack[]){return setADouble(stack[0],getDouble(stack[1].obj));}
static Var DoubleSetLong(Var stack[]){return setADouble(stack[0],(double)getLong(stack[1].obj));}
static Var DoubleToFloat(Var stack[])
{
	Var v;
	v.floatValue = (float)getDouble(stack[0].obj);
	return v;
}
static Var DoubleToInt(Var stack[])
{
	Var v;
	v.intValue = (int)getDouble(stack[0].obj);
	return v;
}
Var setAndReturnLong(WObject lo,int64 value);
static Var DoubleToLong(Var stack[])
{
	return setAndReturnLong(stack[1].obj,(int64)getDouble(stack[0].obj));

}
*/
static Var DoubleToString(Var stack[])

{
	Var v;
	double value = getDouble(stack[0].obj);
	int len = stack[1].intValue;
	int dec = stack[2].intValue;
	int options = stack[3].intValue;
	doubleToString((char *)sbytes,value,len,dec,options);
	v.obj = createString((char *)sbytes);
	return v;
}
static Var DoubleFromString(Var stack[])
{return setAndReturn(stack[0].obj,stringToDouble(stack[1].obj));}
/*
static Var DoubleMultiply(Var stack[])
{
	double me = getDouble(stack[0].obj);
	double other = getDouble(stack[1].obj);
	return setAndReturn(stack[2].obj,me*other);
}
static Var DoubleDivide(Var stack[])
{
	double me = getDouble(stack[0].obj);
	double other = getDouble(stack[1].obj);
	return setAndReturn(stack[2].obj,me/other);
}
static Var DoubleAdd(Var stack[])
{return setAndReturn(stack[2].obj,getDouble(stack[0].obj)+getDouble(stack[1].obj));}
static Var DoubleSubtract(Var stack[])
{return setAndReturn(stack[2].obj,getDouble(stack[0].obj)-getDouble(stack[1].obj));}
static Var DoubleCompareTo(Var stack[])
{
	Var v;
	v.obj = 1;

	getDoubleClass();
	if (isADouble(stack[1].obj)){
		double me = getDouble(stack[0].obj);
		double other = getDouble(stack[1].obj);

		if (me == other) v.intValue = 0;
		else if (me > other) v.intValue = 1;
		else v.intValue = -1;
	}
	return v;
}
*/
#define NEGATIVE  1
#define POSITIVE  2
#define ZERO  3
#define POSITIVE_INFINITY  4
#define NEGATIVE_INFINITY  5
#define EWE_INFINITY  6
#define EWE_NAN 7
#define VALID 8

static Var DoubleIs(Var stack[])
{
	Var v;
	double me = getDouble(stack[0].obj);
	int64 mb = getDoubleAsBits(stack[0].obj);

	switch(stack[1].intValue){
	case NEGATIVE: v.intValue = (me < 0); break;
	case POSITIVE: v.intValue = (me > 0); break;
	case ZERO: v.intValue = (me == 0); break;
	case POSITIVE_INFINITY: v.intValue = (mb == POSITIVE_INFINITY_VALUE); break;
	case NEGATIVE_INFINITY: v.intValue = (mb == NEGATIVE_INFINITY_VALUE); break;
	case EWE_INFINITY: v.intValue = (mb == POSITIVE_INFINITY_VALUE || mb == NEGATIVE_INFINITY_VALUE); break;
	case EWE_NAN: v.intValue = _isnan(me); break;
	case VALID: v.intValue = !_isnan(me) && !(mb == POSITIVE_INFINITY_VALUE || mb == NEGATIVE_INFINITY_VALUE); break;
	default:
		v.intValue = 0;
	}
	return v;
}
static Var DoubleSetSpecial(Var stack[])
{
	Var v;
	v.obj = stack[0].obj;
	setDouble(v.obj,0);
	switch(stack[1].intValue){
	case NEGATIVE: setDouble(v.obj,-1); break;
	case POSITIVE: setDouble(v.obj,1); break;
	case ZERO: setDouble(v.obj,0); break;
	case EWE_INFINITY:
	case POSITIVE_INFINITY: setDoubleAsBits(v.obj,POSITIVE_INFINITY_VALUE); break;

	case NEGATIVE_INFINITY: setDoubleAsBits(v.obj,NEGATIVE_INFINITY_VALUE); break;
	case EWE_NAN: setDoubleAsBits(v.obj,NAN_VALUE); break;
	default:
		break;
	}
	return v;
}

static Var DoubleArrayLength(Var stack[])
{

	Var v;
	WObject array = stack[0].obj;
	v.intValue = 0;

	if (array == 0) return returnExError(ERR_NullArrayAccess);
	else v.intValue = WOBJ_arrayLen(array)/sizeof(double);
	return v;
}
/*
static WObject createDoubleArray(int len)
{
	WObject got;
	got = createArrayObject(arrayType('B'),len*sizeof(double));
	return got;
}
static Var DoubleToIntArray(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int len = stack[2].intValue;
	int al;
	v.obj = 0;

	if (array == 0) return v;
	al = WOBJ_arrayLen(array)/sizeof(double);
	if (st < 0 || st+len >al) return returnError(ERR_IndexOutOfRange);
	else{
		WObject intArray = createArrayObject(arrayType('I'),len);
		double *in = (double *)WOBJ_arrayStart(array)+st;
		int *out = (int *)WOBJ_arrayStart(intArray);
		int i;
		for (i = 0; i<len; i++) *out++ = (int)*in++;
		v.obj = intArray;
		return v;
	}
}

static Var DoubleToFloatArray(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int len = stack[2].intValue;
	int al;
	v.obj = 0;

	if (array == 0) return v;
	al = WOBJ_arrayLen(array)/sizeof(double);
	if (st < 0 || st+len >al) return returnError(ERR_IndexOutOfRange);
	else{
		WObject intArray = createArrayObject(arrayType('F'),len);
		double *in = (double *)WOBJ_arrayStart(array)+st;
		float *out = (float *)WOBJ_arrayStart(intArray);
		int i;
		for (i = 0; i<len; i++) *out++ = (float)*in++;
		v.obj = intArray;
		return v;
	}
}
static Var DoubleToLongArray(Var stack[])

{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int len = stack[2].intValue;
	int al;
	v.obj = 0;

	if (array == 0) return v;
	al = WOBJ_arrayLen(array)/sizeof(double);
	if (st < 0 || st+len >al) return returnError(ERR_IndexOutOfRange);
	else{
		WObject intArray = createArrayObject(arrayType('B'),len*sizeof(int64));

		double *in = (double *)WOBJ_arrayStart(array)+st;
		int64 *out = (int64 *)WOBJ_arrayStart(intArray);

		int i;
		for (i = 0; i<len; i++) *out++ = (int64)*in++;
		return v;
	}
}

static void saveAnInt(int32 value,char *dest,int bytes)
{
	register int i;
	register int32 me = value;
	for (i = bytes; i>=0; i--){
		dest[i] = (byte)(me & 0xff);
		me >>= 8;
	}
}
static int32 loadAnInt(char *from,int bytes)
{
	register int i;
	register int32 me = 0;
	for (i = 0; i<bytes; i++){
		me = me << 8;
		me |= ((int32)(*from++) & 0xff);
	}
	return me;
}
*/
static void saveALong(int64 value,char *dest)
{
	register int i;
	register int64 me = value;
	for (i = 7; i>=0; i--){
		dest[i] = (byte)(me & 0xff);
		me >>= 8;
	}
}
static int64 loadALong(char *from)
{
	register int i;
	register int64 me = 0;
	for (i = 0; i<8; i++){
		me = me << 8;
		me |= ((int64)(*from++) & 0xff);
	}
	return me;
}

//public static void save(Object doubleArray,int start,byte [] dest,int destStart,int length)
/*
static Var DoubleLongSave(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int len = stack[4].intValue;
	WObject dest = stack[2].intValue;
	int ds = stack[3].intValue;
	int al;
	v.obj = 0;

	if (array == 0|| dest == 0) return returnError(ERR_NullArrayAccess);
	al = WOBJ_arrayLen(array)/sizeof(double);
	if (st < 0 || st+len >al || ds < 0 || ds+(len*8) > WOBJ_arrayLen(dest))
		return returnError(ERR_IndexOutOfRange);
	else{
		int64 *in = (int64 *)WOBJ_arrayStart(array)+st;
		char *oo = (char*)WOBJ_arrayStart(dest)+ds;
		int i;
		for (i = 0; i<len; i++) {
			saveALong(*in,oo);
			in++; oo += sizeof(int64);
		}
		return v;
	}
}

//This assumes a little endian processor (e.g. x86)
//public static void load(Object doubleArray,int start,byte [] source,int destStart,int length)
static Var DoubleLongLoad(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int len = stack[4].intValue;

	WObject dest = stack[2].intValue;
	int ds = stack[3].intValue;
	int al;
	v.obj = 0;

	if (array == 0|| dest == 0) return returnError(ERR_NullArrayAccess);
	al = WOBJ_arrayLen(array)/sizeof(double);
	if (st < 0 || st+len >al || ds < 0 || ds+(len*8) > WOBJ_arrayLen(dest))
		return returnError(ERR_IndexOutOfRange);
	else{
		int64 *out = (int64 *)WOBJ_arrayStart(array)+st;
		char *oo = (char*)WOBJ_arrayStart(dest)+ds;
		int i;
		for (i = 0; i<len; i++) {
			*out = loadALong(oo);
			out++, oo += sizeof(int64);
		}
		return v;
	}
}
*/
/*
static Var DoubleCreateArray(Var stack[])
{

	Var v;
	int len = stack[0].intValue;
	if (len < 0) len = 0;
	v.obj = createDoubleArray(len);
	return v;
}
static Var DoubleSetArrayValue(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	WObject source = stack[2].obj;
	int idx = stack[1].intValue;
	int len;
	v.obj = 0;
	if (array == 0 || source == 0) return v;
	len = WOBJ_arrayLen(array)/sizeof(double);
	if (idx < 0 || idx >= len) return returnError(ERR_IndexOutOfRange);
	else{
		double *a = (double *)WOBJ_arrayStart(array);
		*(a+idx) = getDouble(source);
		v.obj = source;
	}
	return v;
}
static Var DoubleGetArrayValue(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	WObject dest = stack[2].obj;
	int idx = stack[1].intValue;
	int len;

	v.obj = 0;
	if (array == 0) return v;
	len = WOBJ_arrayLen(array)/sizeof(double);
	if (idx < 0 || idx >= len) return returnError(ERR_IndexOutOfRange);
	else{
		double *a = (double *)WOBJ_arrayStart(array);
		return setAndReturn(dest,*(a+idx));
	}
}
*/
/*
//===================================================================
public static Object arrayJoin(Object sourceArray,int sourceStart,Object destArray,int destStart,int length)
//===================================================================
{
	double [] da = destArray == null ? new double[0] : (double [])destArray;
	if (sourceArray == null) return da;
	double [] sa = (double [])sourceArray;
	int extraNeeded = destStart+length-da.length;
	if (extraNeeded > 0) {
		double [] nda = new double[da.length+extraNeeded];
		Vm.copyArray(da,0,nda,0,da.length);
		da = nda;
	}
	Vm.copyArray(sa,sourceStart,da,destStart,length);
	return da;
}
*/

//This will be used for both Double and Long Array Join.
/*
static Var DoubleLongArrayJoin(Var stack[])
{
	Var v;
	WObject source = stack[0].obj;
	int ss = stack[1].intValue;
	WObject dest = stack[2].obj;
	int ds = stack[3].intValue;
	int len = stack[4].intValue;
	int extra;
	int dlen;
	v.obj = 0;

	if (len < 0) len = 0;

	if (source == 0) {
		VmQuickError(ERR_NullArrayAccess);
		return v;
	}
	if (ss < 0 || ds < 0 || ss+len > WOBJ_arrayLen(source)){
		VmQuickError(ERR_IndexOutOfRange);
		return v;
	}

	if (dest == 0) dest = createArrayObject(arrayType('B'),8*0);

	pushObject(dest);
	dlen = WOBJ_arrayLen(dest)/8;
	extra = ds+len-dlen;
	if (extra > 0){
		WObject nd = createArrayObject(arrayType('B'),8*extra);
		int64 *sss = (int64 *)WOBJ_arrayStart(dest), *ddd = (int64 *)WOBJ_arrayStart(nd);
		int i;
		for (i = 0; i<dlen; i++) *ddd++ = *sss++;
		popObject();
		dest = nd;
		pushObject(dest);
	}
	if (len != 0){
		int64 *sss = (int64 *)WOBJ_arrayStart(source)+ss, *ddd = (int64 *)WOBJ_arrayStart(dest)+ds;
		int i;
		for (i = 0; i<len; i++) *ddd++ = *sss++;
	}
	popObject();
	v.obj = dest;
	return v;
}
*/
/*
static Var DoubleToBits(Var stack[])
{
	return setAndReturnLong(stack[1].obj,getDoubleAsBits(stack[0].obj));
}
static Var DoubleFromBits(Var stack[])
{
	Var v;
	v.obj = stack[0].obj;
	setDoubleAsBits(v.obj,getLong(stack[1].obj));
	return v;
}
*/
/*
//##################################################################
public class MathFunctionData{
//##################################################################
public Object doubleArrayOfParameters; - 1
public int parameterStartIndex; - 2
public int parameterCount; - 3

public Double result = new Double(); - 4
public int rowIndex; - 5
public int colIndex; - 6
//##################################################################
}
//##################################################################

//===================================================================
public static void matrixFunction(
double [] doubleMatrix,int startIndex,int rows,int columns,
MathFunction [] functions,double [] destDoubleArray,int destStartIndex)
//===================================================================
{
	double [] ret = (double [])destDoubleArray;
	double [] buffer = new double[functions.length];
	double [] data = (double [])doubleMatrix;

	MathFunctionData fd = new MathFunctionData();
	fd.doubleArrayOfParameters = doubleMatrix;
	fd.parameterCount = columns;
	fd.parameterStartIndex = startIndex;
	for (int r = 0; r<rows; r++){
		fd.rowIndex = r;
		for (int f = 0; f < functions.length; f++){
			fd.result = 0;
			fd.colIndex = f;
			functions[f].doFunction(fd);
			buffer[f] = fd.result;
		}
		for (int f = 0; f < functions.length; f++)
			ret[destStartIndex++] = buffer[f];
		fd.parameterStartIndex += columns;
	}
}

//##################################################################
public class MathFunctionData{
//##################################################################
public double [] doubleArrayOfParameters;
public int parameterStartIndex;
public int parameterCount;
public double result;
public int rowIndex;
public int colIndex;
//##################################################################
}
//##################################################################

*/
static Var DoubleMatrixFunction(Var stack[])
{
	Var v;
	WObject matrix = stack[0].obj;
	int startIndex = stack[1].intValue;
	int rows = stack[2].intValue;
	int columns = stack[3].intValue;
	WObject functions = stack[4].obj;
	WObject destArray = stack[5].obj;
	int destStart = stack[6].intValue;
	int matrixLength, destLength, numFunctions;

	v.obj = 0;

	if (matrix == 0 || destArray == 0 || functions == 0) return returnExError(ERR_NullArrayAccess);
	matrixLength = WOBJ_arrayLen(matrix);
	destLength = WOBJ_arrayLen(destArray);
	numFunctions = WOBJ_arrayLen(functions);
// Do some bounds checking.
	if (startIndex < 0 || destStart < 0)
		return returnExError(ERR_IndexOutOfRange);
	if (startIndex+(rows*columns) > matrixLength || destStart+(rows*numFunctions) > destLength)
		return returnExError(ERR_IndexOutOfRange);
	else {
		WObject fd, buffer;
		int r,f;
		Var pars[2];

		WClassMethod **funcMethods;
		WClass **funcs;
		int outi;
		funcMethods = (WClassMethod **)mMalloc(sizeof(WClassMethod *)*numFunctions);
		funcs = (WClass **)mMalloc(sizeof(WClass *)*numFunctions);

		for (f = 0; f<numFunctions; f++){
			Var *funcObjects = WOBJ_arrayStart(functions);
			if (funcObjects[f].obj == 0) return returnExError(ERR_NullObjectAccess);
			funcMethods[f] = getMethod(WOBJ_class(funcObjects[f].obj),
			createUtfString("doFunction"),
			createUtfString("(Lewe/sys/MathFunctionData;)V"),&funcs[f]);
		}
//
		buffer = createArrayObject(arrayType('B'),numFunctions*sizeof(double));
		if (buffer == 0) return v;
		pushObject(buffer);
//
		fd = createObject(getClass(createUtfString("ewe/sys/MathFunctionData")));
		pushObject(fd);
		objectPtr(fd)[1].obj = matrix;


		objectPtr(fd)[2].intValue = startIndex;
		objectPtr(fd)[3].intValue = columns;
		pars[1].obj = fd;
//
		getDoubleClass();
//
		outi = destStart;
//
		for (r = 0; r<rows; r++){
			objectPtr(fd)[6].intValue = r;
			for (f = 0; f<numFunctions; f++){
				double *buff;
				Var *funcObjects;
				objectPtr(fd)[7].intValue = f;
				funcObjects = WOBJ_arrayStart(functions);
				pars[0].obj = funcObjects[f].obj;
				executeMethod(funcs[f],funcMethods[f],pars,2);
				if (thrownException) break;
				buff = (double *)WOBJ_arrayStart(buffer);
				buff[f] = vars2double((Var *)objectPtr(fd)+4);
			}
			if (thrownException) break;
			{
			double *buff = (double *)WOBJ_arrayStart(buffer);
			double *out = (double *)WOBJ_arrayStart(destArray)+outi;
			for (f = 0; f<numFunctions; f++) *out++ = buff[f];
			outi += numFunctions;
			objectPtr(fd)[2].intValue = objectPtr(fd)[2].intValue+columns;
			}
		}
		popObject(); // FunctionData
		popObject(); // Buffer
		free(funcMethods);
		free(funcs);
	}
	return v;
}

//#############################################################################
// LONG VALUES
//#############################################################################


static int64 stringCharsToLong(uint16 *chars,int len)

{
	int32 i, isNeg = 0;
	int64 value = 0;
	if (len > 0 && chars[0] == '-')
		isNeg = 1;
	value = 0;
	for (i = isNeg; i < len; i++)
		{
		if (chars[i] < (uint16)'0' || chars[i] > (uint16)'9')
			return 0l;
		value = (value * 10) + (int64)((int32)chars[i] - (int32)'0');
		}
	if (isNeg)
		value = -(value);
	return value;
}

static int64 stringToLong(WObject string)
{
	WObject charArray;
	if (string == 0) return 0l;

	charArray = WOBJ_StringCharArrayObj(string);
	if (charArray == 0) return 0l;
	return stringCharsToLong((uint16 *)WOBJ_arrayStart(charArray),WOBJ_arrayLen(charArray));
}
//============================================================
static void longToString(char dest[],int64 val,int totalLength,int options)
//============================================================
{
	int neg = val<0;
	int64 pre;
	int frontLength = 1,digits = 0;
	int needLength = 0;
	char *d = dest;
	int64 cur;
	int64 div = 10;
	int divided = 0;
	int zeroFill = (options & ZERO_FILL) != 0;
	static char hex[] = "0123456789ABCDEF";
	if ((options & HEX) != 0){

		int64 mask = cINT64(0xf000000000000000L);
		int digits = 0;
		char padChar = zeroFill ? '0' : ' ';
		for (digits = 16; digits >0; digits--){
			if ((mask & val) != 0) break;
			mask >>= 4;
		}
		if (digits == 0) digits = 1;
		if (digits > totalLength && totalLength > 0) goto cant_fit;
		if (totalLength <= 0) totalLength = digits;
		d = dest+totalLength;
		*d = 0;
		while(d != dest){
			*(--d) = digits > 0 ? hex[(int)(val & 0xf)]:padChar;
			val >>= 4;
			digits--;
		}
		return;
	}
	if (neg) val *= -1;
	pre = val;
	if (pre != 0){
		cur = pre;
		while(1){

			cur -= cur%div;
			if (cur == 0) break;
			frontLength++;
			if (divided < 17) div *= 10;
			else break;
			divided++;

		}
	}
	digits = frontLength;
	if (neg) frontLength++;
	needLength = frontLength;
	//if (decimal > 0) needLength += decimal+1;
	if (needLength > totalLength && totalLength > 0) goto cant_fit;

	if (totalLength > 0){
		int padLength = totalLength-frontLength;
		char padChar = zeroFill ? '0':' ';
		int i = 0;
		if (zeroFill && neg) *d++ = '-';
		for (i = 0; i<padLength; i++) *d++ = padChar;
		if (!zeroFill && neg) *d++ = '-';
	}else{
		if (neg) *d++ = '-';
	}
	for (cur = pre;div != 0;){
		int dig;
		if (divided < 17) div /= 10;
		else divided = 0;
		if (div == 0) break;
		dig = (int)(cur/div);
		cur -= dig*div;
		if (dig < 0) dig = -dig;

		*d++ = '0'+dig;
	}

	*d++ = 0;
	return;
cant_fit:
	{
		int i;
		for (i = 0; i<totalLength; i++) dest[i] = '#';
		dest[totalLength] = 0;
		return;
	}
}
WClass* longClass = NULL;
static WClass * getLongClass()
{


	if (longClass == NULL)
	longClass = getClass(createUtfString("ewe/sys/Long"));
	return longClass;
}
static int isALong(WObject obj)
{
	if (obj == 0) return 0;
	return compatible(WOBJ_class(obj),longClass);
}
void setLong(WObject obj,int64 d)
{
	if (obj == 0) return;
	int642vars(d,objectPtr(obj)+1);
}
int64 getLong(WObject obj)
{
	if (obj == 0) return 0;
	else return vars2int64(objectPtr(obj)+1);
}
static Var setALong(Var lo,int64 value)
{
	setLong(lo.obj,value);
	return lo;
}
static Var setAndReturnLong(WObject lo,int64 value)
{
	Var v;
	if (lo == 0) {
		getLongClass();
		lo = createObject(longClass);
	}
	v.obj = lo;
	return setALong(v,value);
}

static Var LongSetInt(Var stack[]){return setALong(stack[0],(int64)stack[1].intValue);}
static Var LongSetInt2(Var stack[])
{
	int64 high = (((int64)stack[1].intValue) << 32) & cINT64(0xffffffff00000000L);
	int64 low =  (((int64)stack[2].intValue)) & cINT64(0x00000000ffffffffL);
	return setALong(stack[0],high|low);
}
static Var LongToString(Var stack[])
{
	Var v;
	char chars[40];
	int64 value = getLong(stack[0].obj);
	int len = stack[1].intValue;
	int options = stack[2].intValue;
	longToString(chars,value,len,options);
	v.obj = createString(chars);
	return v;
}
static Var LongFromString(Var stack[])
{return setAndReturnLong(stack[0].obj,stringToLong(stack[1].obj));}

/*
static Var LongCompareTo(Var stack[])
{
	Var v;
	v.obj = 1;

	getLongClass();
	if (isALong(stack[1].obj)){
		int64 me = getLong(stack[0].obj);
		int64 other = getLong(stack[1].obj);
		if (me == other) v.intValue = 0;
		else if (me > other) v.intValue = 1;

		else v.intValue = -1;
	}
	return v;
}
static Var LongDivide(Var stack[])
{
	int64 me = getLong(stack[0].obj);
	int64 other = getLong(stack[1].obj);
	if (other == 0)
		if (me == 0)
			return setAndReturnLong(stack[2].obj,0);
		else if (me < 0.0)
			return setAndReturnLong(stack[2].obj,cINT64(0x8000000000000000l));
		else return setAndReturnLong(stack[2].obj,cINT64(0x7fffffffffffffffl));
	return setAndReturnLong(stack[2].obj,me/other);
}
static Var LongAdd(Var stack[])
{return setAndReturnLong(stack[2].obj,getLong(stack[0].obj)+getLong(stack[1].obj));}
static Var LongSubtract(Var stack[])
{return setAndReturnLong(stack[2].obj,getLong(stack[0].obj)-getLong(stack[1].obj));}
static Var LongMultiply(Var stack[])
{return setAndReturnLong(stack[2].obj,getLong(stack[0].obj)*getLong(stack[1].obj));}
static Var LongModulus(Var stack[])
{return setAndReturnLong(stack[2].obj,getLong(stack[0].obj)%getLong(stack[1].obj));}
static Var LongShiftRight(Var stack[])
{return setAndReturnLong(stack[2].obj,getLong(stack[0].obj)>>stack[1].intValue);}
static Var LongShiftLeft(Var stack[])

{return setAndReturnLong(stack[2].obj,getLong(stack[0].obj)<<stack[1].intValue);}
static Var LongSetFloat(Var stack[]) {return setALong(stack[0],(int64)stack[1].floatValue);}
static Var LongSetLong(Var stack[]){return setALong(stack[0],getLong(stack[1].obj));}
static Var LongSetDouble(Var stack[]){return setALong(stack[0],(int64)getDouble(stack[1].obj));}
static Var LongToFloat(Var stack[])
{
	Var v;
	v.floatValue = (float)getLong(stack[0].obj);
	return v;
}

static Var LongToDouble(Var stack[])
{
	return setAndReturn(stack[1].obj,(double)getLong(stack[0].obj));
}
static Var LongToInt(Var stack[])
{
	Var v;
	v.intValue = (int)getLong(stack[0].obj);
	return v;
}
static Var LongCreateArray(Var stack[])
{
	Var v;
	int len = stack[0].intValue;
	if (len < 0) len = 0;
	v.obj = createArrayObject(arrayType('B'),len*sizeof(int64));
	if (len != 0){
		int i = 0;
		int64 *a = (int64 *)WOBJ_arrayStart(v.obj);
		for (i = 0; i<len; i++) *a++ = 0;
	}
	return v;
}
static Var LongSetArrayValue(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	WObject source = stack[2].obj;
	int idx = stack[1].intValue;
	int len;
	v.obj = 0;
	if (array == 0 || source == 0) return v;
	len = WOBJ_arrayLen(array)/sizeof(int64);
	if (idx < 0 || idx >= len) return returnError(ERR_IndexOutOfRange);
	else{
		int64 *a = (int64 *)WOBJ_arrayStart(array);
		*(a+idx) = getLong(source);
		v.obj = source;
	}
	return v;
}
static Var LongGetArrayValue(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	WObject dest = stack[2].obj;
	int idx = stack[1].intValue;
	int len;
	v.obj = 0;
	if (array == 0) return v;
	len = WOBJ_arrayLen(array)/sizeof(int64);
	if (idx < 0 || idx >= len) return returnError(ERR_IndexOutOfRange);
	else{
		int64 *a = (int64 *)WOBJ_arrayStart(array);
		return setAndReturnLong(dest,*(a+idx));
	}
}
static Var LongToIntArray(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	int st = stack[1].intValue;
	int len = stack[2].intValue;
	int al;
	v.obj = 0;

	if (array == 0) return v;
	al = WOBJ_arrayLen(array)/sizeof(int64);
	if (st < 0 || st+len >al) return returnError(ERR_IndexOutOfRange);
	else{
		WObject intArray = createArrayObject(arrayType('I'),len);
		int64 *in = (int64 *)WOBJ_arrayStart(array)+st;
		int *out = (int *)WOBJ_arrayStart(intArray);
		int i;
		for (i = 0; i<len; i++) *out++ = (int)*in++;
		v.obj = intArray;
		return v;
	}
}
static Var LongSave(Var stack[])

{
	Var v;
	WObject array = stack[1].obj;
	int start = stack[2].intValue;
	int len;
	v.intValue = 0;
	if (array == 0) return v;
	len = WOBJ_arrayLen(array);
	if (start < 0 || start+7 >= len) return v;
	saveALong(getLong(stack[0].obj),(char *)WOBJ_arrayStart(array)+start);
	return v;

}
static Var LongLoad(Var stack[])
{
	Var v;
	register int64 me = 0;
	WObject array = stack[1].obj;
	int start = stack[2].intValue;
	int len;
	v.intValue = 0;
	if (array == 0) return v;
	len = WOBJ_arrayLen(array);
	if (start < 0 || start+7 >= len) return v;
	setLong(stack[0].obj,loadALong((char *)WOBJ_arrayStart(array)+start));
	return v;
}
*/
static int readInt(char *source,int numBytes)
{
	int ret = 0,i;
	for (i = 0; i<numBytes; i++) {
		ret = (ret<<8) & 0xffffff00;
		ret |= ((int)*source++)&0xff;
	}
	return ret;
}

static Var UtilsReadInt(Var stack[])
{
	Var v;
	WObject array = stack[0].obj;
	v.intValue = 0;
	if (array == 0) return v;
	v.intValue = readInt((char *)WOBJ_arrayStart(array)+stack[1].intValue,stack[2].intValue);
	return v;
}

static Var UtilsFindCRLF(Var stack[])
{
	Var v;
	WObject bytes = stack[0].obj;
	int start = stack[1].intValue;
	int length = stack[2].intValue;

	v.intValue = -1;

	if (bytes == 0) return v;
	if (WOBJ_arrayLen(bytes) < start+length) return v;
	else{
		unsigned char *by = (unsigned char *)WOBJ_arrayStart(bytes);
		int i;
		for (i = start; i<start+length; i++){
			if (by[i] == 10) {
				v.intValue = i;
				return v;
			}
			if (by[i] == 13){
				if (i != start+length-1)
					if (by[i+1] == 10){
						v.intValue = i+1;
						return v;
					}
				v.intValue = i;
				return v;
			}
		}
	}
	return v;
}
#define SIN   1
#define COS   2
#define TAN   3
#define ASIN   4
#define ACOS   5
#define ATAN   6
#define ATAN2   7
#define TORADIANS   8
#define TODEGREES   9
#define LOG2   10
#define LOG   11
#define EXP   12
#define POWER   13
#define SQRT   14
#define CEIL 15
#define FLOOR 16

#define MULTIPLY  17
#define DIVIDE  18
#define ADD  19
#define SUBTRACT  20
#define NEGATE  21
#define M_ABSOLUTE  22
#define REMAINDER  23
#define GREATER  24
#define GREATEROREQUAL  25
#define LESSER  26
#define LESSEROREQUAL  27
#define EQUALS  28
/**
* A boolean value is 0 = false, not 0 = true
**/
#define BOOLEAN_OR  29
/**
* A boolean value is 0 = false, not 0 = true
**/
#define BOOLEAN_AND  30

/**

* A boolean value is 0 = false, not 0 = true
**/
#define BOOLEAN_NOT  31
/**
* A boolean value is 0 = false, not 0 = true
**/
#define BOOLEAN_EXOR  32
#define GREATER_OF  33
#define LESSER_OF  34
/**
* The result is the value of the second parameter.
**/
#define SET  35
#define RINT  36
#define ROUND  37

#define CONST_PI 1
#define CONST_E 2

static double cPI = 0.0, cE = 0.0;

/*
static Var MathCalculate(Var stack[])
{

	Var v;
	int op = stack[0].intValue;
	WObject operand = stack[1].obj;
	v.obj = stack[2].obj;
	if (v.obj == 0){
		getDoubleClass();
		v.obj = createObject(doubleClass);
	}
	if (cPI == 0) cPI = acos(-1.0);
	if (operand != 0){
		double d = getDouble(operand);
		double d2 = getDouble(v.obj);
		double r = 0;
		switch(op){
		case CEIL: r = ceil(d); break;
		case FLOOR: r = floor(d); break;
		case SIN: r = nearZero(sin(d)); break;
		case COS: r = nearZero(cos(d)); break;
		case TAN: r = nearInf(nearZero(tan(d))); break;
		case ASIN: r = asin(d); break;
		case ACOS: r = acos(d); break;
		case ATAN: r = atan(d); break;
		case ATAN2: r = atan2(d,d2); break;
		case LOG2: r = log(d)/log(d2); break;
		case POWER: r = pow(d,d2); break;
		case TORADIANS:{
			double deg = d;
			while(deg >= 360) deg -= 360;
			while(deg <= -360) deg += 360;
			if (deg > 180) deg = -(360-deg);
			else if (deg < -180) deg = 360+deg;
			r = cPI * deg/180.0;
			break;
					   }
		case TODEGREES: {
			double rad = d;
			double p2 = cPI*2;
			while(rad >= p2) rad-=p2;
			while(rad <= -p2) rad+=p2;
			if (rad > cPI) rad = -(p2-rad);
			else if (rad < -cPI) rad = p2+rad;
			r = rad*180.0/cPI;
			break;
						}
		case LOG: r = log(d); break;
		case EXP: r = exp(d); break;
		case SQRT: r = sqrt(d); break;
		case MULTIPLY: r = d*d2; break;
		case DIVIDE: r = d/d2; break;
		case ADD: r = d+d2; break;
		case SUBTRACT: r = d-d2; break;
		case NEGATE: r = -d; break;
		case M_ABSOLUTE: r = fabs(d); break;
		case REMAINDER: r = fmod(d,d2); break;
		case GREATER: r = (d > d2) ? 1 : 0; break;
		case GREATEROREQUAL: r = (d >= d2) ? 1 : 0; break;
		case LESSER:  r = (d < d2) ? 1 : 0; break;
		case LESSEROREQUAL:  r = (d <= d2) ? 1 : 0; break;
		case EQUALS: r = (d == d2) ? 1 : 0; break;
		case BOOLEAN_OR: r = ((d != 0) || (d2 != 0)) ? 1 : 0; break;
		case BOOLEAN_AND: r = ((d != 0) && (d2 != 0)) ? 1 : 0; break;
		case BOOLEAN_EXOR: r = ((d != 0) != (d2 != 0)) ? 1 : 0; break;
		case BOOLEAN_NOT: r = (d == 0) ? 1 : 0; break;
		case GREATER_OF: r = (d > d2) ? d : d2; break;
		case LESSER_OF: r = (d < d2) ? d : d2; break;
		case SET: r = d2; break;
		}

		return setADouble(v,r);
	}
	return v;
}

static Var MathConstant(Var stack[])
{
	Var v;

	WObject c = stack[0].obj;
	v.obj = stack[1].obj;
	if (v.obj == 0){
		getDoubleClass();



		v.obj = createObject(doubleClass);

	}
	if (cPI == 0) cPI = acos(-1.0);

	if (cE == 0) cE = exp(1.0);

	switch(c){
	case CONST_PI: return setADouble(v,cPI);
	case CONST_E: return setADouble(v,cE);

	}
	return v;
}
*/
#define MathSingleOp(NAME,FUNC) static Var NAME(Var stack[]){return returnDouble(FUNC(vars2double(stack)));}
#define MathDoubleOp(NAME,FUNC) static Var NAME(Var stack[]){return returnDouble(FUNC(vars2double(stack),vars2double(stack+2)));}


double nzSin(double val) {return nearZero(sin(val));}
double nzCos(double val) {return nearZero(cos(val));}
double nzTan(double val) {return nearInf(nearZero(tan(val)));}

MathSingleOp(MathSin,nzSin)
MathSingleOp(MathCos,nzCos)
MathSingleOp(MathTan,nzTan)
MathSingleOp(MathAsin,asin)
MathSingleOp(MathAcos,acos)
MathSingleOp(MathAtan,atan)
MathSingleOp(MathSqrt,sqrt)
MathSingleOp(MathFloor,floor)
MathSingleOp(MathCeil,ceil)
MathSingleOp(MathLog,log)
MathSingleOp(MathExp,exp)
MathDoubleOp(MathAtan2,atan2)
MathDoubleOp(MathPow,pow)
MathDoubleOp(MathRemainder,fmod)


/*
static Var MathPow(Var stack[])
{
	double d = vars2double(stack);
	double d2 = vars2double(stack+2);
	if (d == 0.0 && d2 <= 0.0)

		return returnException("java/lang/ArithmeticException",NULL);
	if (d <= 0.0 && (d2 <= 0.0 || floor(d2) != d2))
		return returnException("java/lang/ArithmeticException",NULL);
	return returnDouble(pow(d,d2));
}
*/
static Var MathRand(Var stack[])
{
	Var v;
	v.intValue = rand();
	return v;
}
static Var MathSrand(Var stack[])
{
	Var v;
	srand(stack[0].intValue);
	v.intValue = 0;
	return v;

}
static Var MathCalculateNative(Var stack[])
{
	int op = stack[0].intValue;
	double d = vars2double(stack+1);
	double d2 = vars2double(stack+3);
	double r = 0.0;

	if (cPI == 0) cPI = acos(-1.0);
	switch(op){
	case ROUND:

	case RINT: {
		double ce = ceil(d);
		double fl = floor(d);
		double dif = ce-d;
		if (dif > 0.5) r = fl;
		else r = ce;
		/*
		else if (dif < 0.5) r = ce;
		else if (fmod(ce,2.0) == 0) r = ce;
		else r = fl;
		*/
		break;

			   }

	case CEIL: r = ceil(d); break;
	case FLOOR: r = floor(d); break;
	case SIN: r = nearZero(sin(d)); break;
	case COS: r = nearZero(cos(d)); break;
	case TAN: r = nearInf(nearZero(tan(d))); break;
	case ASIN: r = asin(d); break;
	case ACOS: r = acos(d); break;
	case ATAN: r = atan(d); break;
	case ATAN2: r = atan2(d,d2); break;
	case LOG2: r = log(d)/log(d2); break;
	case POWER: r = pow(d,d2); break;
	case TORADIANS:{
		double deg = d;
		while(deg >= 360) deg -= 360;
		while(deg <= -360) deg += 360;
		if (deg > 180) deg = -(360-deg);
		else if (deg < -180) deg = 360+deg;
		r = cPI * deg/180.0;
		break;
				   }
	case TODEGREES: {
		double rad = d;
		double p2 = cPI*2;
		while(rad >= p2) rad-=p2;
		while(rad <= -p2) rad+=p2;
		if (rad > cPI) rad = -(p2-rad);
		else if (rad < -cPI) rad = p2+rad;
		r = rad*180.0/cPI;
		break;
					}
	case LOG: r = log(d); break;
	case EXP: r = exp(d); break;
	case SQRT: r = sqrt(d); break;
	case MULTIPLY: r = d*d2; break;
	case DIVIDE: r = d/d2; break;

	case ADD: r = d+d2; break;
	case SUBTRACT: r = d-d2; break;
	case NEGATE: r = -d; break;
	case M_ABSOLUTE: r = fabs(d); break;
	case REMAINDER: r = fmod(d,d2); break;

	case GREATER: r = (d > d2) ? 1 : 0; break;
	case GREATEROREQUAL: r = (d >= d2) ? 1 : 0; break;
	case LESSER:  r = (d < d2) ? 1 : 0; break;
	case LESSEROREQUAL:  r = (d <= d2) ? 1 : 0; break;
	case EQUALS: r = (d == d2) ? 1 : 0; break;
	case BOOLEAN_OR: r = ((d != 0) || (d2 != 0)) ? 1 : 0; break;
	case BOOLEAN_AND: r = ((d != 0) && (d2 != 0)) ? 1 : 0; break;
	case BOOLEAN_EXOR: r = ((d != 0) != (d2 != 0)) ? 1 : 0; break;
	case BOOLEAN_NOT: r = (d == 0) ? 1 : 0; break;
	case GREATER_OF: r = (d > d2) ? d : d2; break;
	case LESSER_OF: r = (d < d2) ? d : d2; break;
	case SET: r = d2; break;
	}
	return returnDouble(r);
}
static Var MathConstantNative(Var stack[])
{
	int c = stack[0].intValue;
	if (cPI == 0) cPI = acos(-1.0);
	if (cE == 0) cE = exp(1.0);
	switch(c){
	case CONST_PI: return returnDouble(cPI);
	case CONST_E: return returnDouble(cE);
	}
	return returnDouble(0);
}

//#########################################################################
// Curve Methods
//#########################################################################

/**
* This calculates the points on a quad curve, for either the x or y co-ordinate. This is called
* by the other calculateQuads method. If the destination object is null this should return the
* number of points in the curve.<p>
* This method does NOT include the last point on the curve, but DOES include the first one.
**/
//-------------------------------------------------------------------
static int calculateQuads(double *quads,int numQuads,float startx,float starty,float controlx,float controly,float endx,float endy,void *destx,void *desty,int offset,int options,int numPoints)
//-------------------------------------------------------------------
{

	int * dxi = options & 0x80000000 ? (int *)destx : NULL;
	float * dxf = !(options & 0x80000000) ? (float *)destx : NULL;
	int * dyi = options & 0x80000000 ? (int *)desty : NULL;
	float * dyf = !(options & 0x80000000) ? (float *)desty : NULL;
	if (dxi == NULL && dxf == NULL) return numPoints;
	else{
		int t = 0, i = 0;
		int dt = ((numQuads/3)/numPoints)*3;
		int skipped = 0;
		double sx = startx, ex = endx, c1x = controlx;
		double sy = starty, ey = endy, c1y = controly;
		for (i = 0; i<numPoints; i++){
			double x = (quads[t]*sx+quads[t+1]*c1x+quads[t+2]*ex);
			double y = (quads[t]*sy+quads[t+1]*c1y+quads[t+2]*ey);

			if (dxi != NULL) dxi[offset] = (int)x;
			else dxf[offset] = (float)x;

			if (dyi != NULL) dyi[offset] = (int)y;
			else dyf[offset] = (float)y;


			if (offset != 0 && dxi != NULL){
				if (dxi[offset-1] == dxi[offset] && dyi[offset-1] == dyi[offset]){
					offset--;
					skipped++;
				}else{
					if (offset > 1){
						if (dxi[offset-1] == dxi[offset] && dxi[offset-2] == dxi[offset]){
							offset--;
							skipped++;
							dyi[offset] = dyi[offset+1];
						}
						else if (dyi[offset-1] == dyi[offset] && dyi[offset-2] == dyi[offset]){
							offset--;
							skipped++;
							dxi[offset] = dxi[offset+1];
							/*
						}else if ((dxi[offset]-dxi[offset-1] == dxi[offset-1]-dxi[offset-2])
						 && (dyi[offset]-dyi[offset-1] == dyi[offset-1]-dyi[offset-2]))
							{
							offset--;
							skipped++;
							dyi[offset] = dyi[offset+1];
							*/
						}
					}
				}
			}

			offset++;
			t += dt;

		}
		return numPoints-skipped;
	}
}
//-------------------------------------------------------------------
static int calculateCubes(double *cubes,int numCubes,float startx,float starty,float control1x,float control1y,float control2x,float control2y,float endx,float endy,void * destx,void * desty,int offset,int options,int numPoints)
//-------------------------------------------------------------------
{
	int * dxi = options & 0x80000000 ? (int *)destx : NULL;
	float * dxf = !(options & 0x80000000) ? (float *)destx : NULL;
	int * dyi = options & 0x80000000 ? (int *)desty : NULL;
	float * dyf = !(options & 0x80000000) ? (float *)desty : NULL;
	if (dxi == NULL && dxf == NULL) return numPoints;
	else{
		int t = 0, i = 0;
		int dt = ((numCubes/4)/numPoints)*4;
		double sx = startx, ex = endx, c1x = control1x, c2x = control2x;
		double sy = starty, ey = endy, c1y = control1y, c2y = control2y;
		int skipped = 0;
		for (i = 0; i<numPoints; i++){
			double x = (cubes[t]*sx+cubes[t+1]*c1x+cubes[t+2]*c2x+cubes[t+3]*ex);
			double y = (cubes[t]*sy+cubes[t+1]*c1y+cubes[t+2]*c2y+cubes[t+3]*ey);
			if (dxi != NULL) dxi[offset] = (int)x;
			else dxf[offset] = (float)x;
			if (dyi != NULL) dyi[offset] = (int)y;
			else dyf[offset] = (float)y;

			if (offset > 0 && dxi != NULL){
				if (dxi[offset-1] == dxi[offset] && dyi[offset-1] == dyi[offset]){
					offset--;
					skipped++;
				}else{
					if (offset > 1){
						if (dxi[offset-1] == dxi[offset] && dxi[offset-2] == dxi[offset]){
							offset--;
							skipped++;
							dyi[offset] = dyi[offset+1];
						}
						else if (dyi[offset-1] == dyi[offset] && dyi[offset-2] == dyi[offset]){
							offset--;
							skipped++;
							dxi[offset] = dxi[offset+1];
							/*
						}else if ((dxi[offset]-dxi[offset-1] == dxi[offset-1]-dxi[offset-2])
						 && (dyi[offset]-dyi[offset-1] == dyi[offset-1]-dyi[offset-2]))
							{
							offset--;
							skipped++;
							dyi[offset] = dyi[offset+1];
							*/
						}

					}
				}
			}
			offset++;
			t += dt;
		}
		return numPoints-skipped;
	}
}
//-------------------------------------------------------------------
static Var CurveCalculatePoints(Var stack[])
//-------------------------------------------------------------------
{
	double * quads = (double *)WOBJ_arrayStart(stack[0].obj);
	double * cubes = (double *)WOBJ_arrayStart(stack[1].obj);
	int MAX_POINTS = stack[2].intValue;
	double dp = (double)1.0/(double)MAX_POINTS;
	double t = 0.0;
	int i = 0;
	for (i = 0; i<MAX_POINTS; i++){
		quads[i*3] = (1.0-t)*(1.0-t);
		quads[i*3+1] = 2*t*(1.0-t);
		quads[i*3+2] = t*t;
		t += dp;
	}
	dp = (double)1.0/(double)MAX_POINTS;
	t = 0.0;

	for (i = 0; i<MAX_POINTS; i++){
		cubes[i*4] = 	(1.0-t)*(1.0-t)*(1.0-t);
		cubes[i*4+1] = 3*t*(1.0-t)*(1.0-t);
		cubes[i*4+2] = 3*(1.0-t)*t*t;
		cubes[i*4+3] = t*t*t;
		t += dp;
	}
	return returnVar(0);
}

static Var CurveCalculateCurves(Var stack[])
{
	double *quads = (double *)WOBJ_arrayStart(stack[0].obj);
	int numQuads = WOBJ_arrayLen(stack[0].obj);
	float *points = (float*)WOBJ_arrayStart(stack[1].obj);
	void *destX = stack[2].obj == 0 ? NULL : WOBJ_arrayStart(stack[2].obj);
	void *destY = stack[3].obj == 0 ? NULL : WOBJ_arrayStart(stack[3].obj);
	int offset = stack[4].intValue;
	int options = stack[5].intValue;
	int numPoints = stack[6].intValue;
	if (options & 0x40000000)
		return returnVar(calculateCubes(quads,numQuads,points[0],points[1],points[2],points[3],points[4],points[5],points[6],points[7],destX,destY,offset,options,numPoints));
	else
		return returnVar(calculateQuads(quads,numQuads,points[0],points[1],points[2],points[3],points[4],points[5],destX,destY,offset,options,numPoints));
}
//#########################################################################
// Polygon methods.

//#########################################################################

static int isIn(int *xPoints, int *yPoints, int numPoints, int x, int y)
{
	int * xx = xPoints;
	int * yy = yPoints;
	int num = 0, i;
	for (i = 0; i<numPoints; i++){
		int x1 = xx[i], y1 = yy[i];
		int x2, y2;
		if (i == numPoints-1) x2 = xx[0];
		else x2 = xx[i+1];
		if (i == numPoints-1) y2 = yy[0];
		else y2 = yy[i+1];

		if ((y1 <= y && y2 > y) || (y2 <= y && y1 > y)){
			if ((y2-y1) == 0) num++;
			else{
				int xi = x1+((y-y1)*(x2-x1))/(y2-y1);
				if (xi >= x) {
					num++;
				}
			}
		}
	}
	return (num & 1) == 1;
}

static Var PolygonIsIn(Var stack[])
{
	int *xp = (int *)WOBJ_arrayStart(stack[0].obj);
	int *yp = (int *)WOBJ_arrayStart(stack[1].obj);
	return returnVar(isIn(xp,yp,stack[2].intValue,stack[3].intValue,stack[4].intValue));
}

//-------------------------------------------------------------------
static int intersects(int xa1, int ya1, int xa2, int ya2, int xb1, int yb1, int xb2, int yb2)
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("("+xa1+", "+ya1+")-"+"("+xa2+", "+ya2+")"+"("+xb1+", "+yb1+")-"+"("+xb2+", "+yb2+")");
	if (xa1 == xb1 && ya1 == yb1) return 1;
	if (xa1 == xb2 && ya1 == yb2) return 1;
	if (xa2 == xb1 && ya2 == yb1) return 1;
	if (xa2 == xb2 && ya2 == yb2) return 1;
	{
	double ma = (xa1 == xa2) ? 0 : (ya1-ya2)/(double)(xa1-xa2);
	double mb = (xb1 == xb2) ? 0 : (yb1-yb2)/(double)(xb1-xb2);
	double ca = (double)ya1-ma*xa1;
	double cb = (double)yb1-mb*xb1;

	if (xa1 == xa2) {//Vertical
		if (xb1 == xb2) {//Also vertical
			if (xa1 == xb1)
				if (
						(ya1 <= yb1 && yb1 <= ya2) || (ya1 >= yb1 && yb1 >= ya2) ||
					 	(ya1 <= yb2 && yb2 <= ya2) || (ya1 >= yb2 && yb2 >= ya2) ||
						(yb1 <= ya1 && ya1 <= yb2) || (yb1 >= ya1 && ya1 >= yb2) ||
					 	(yb1 <= ya2 && ya2 <= yb2) || (yb1 >= ya2 && ya2 >= yb2)
						)
						return 1;
		}else{
			if ((xb1 <= xa1 && xa1 <= xb2) || (xb1 >= xa1 && xa1 >= xb2)){
				double yi = mb*xa1+cb;
				if ((ya1 <= yi && yi <= ya2) || (ya1 >= yi && yi >= ya2))
					return 1;
			}
		}
	}else if (xb1 == xb2) {//Vertical
			if ((xa1 <= xb1 && xb1 <= xa2) || (xa1 >= xb1 && xb1 >= xa2)){
				double yi = ma*xb1+ca;
				if ((yb1 <= yi && yi <= yb2) || (yb1 >= yi && yi >= yb2))
					return 1;
			}
	}else{
		if (ma == mb){//Parallel
			if (ca == cb){
				if (
						((ya1 <= yb1 && yb1 <= ya2) || (ya1 >= yb1 && yb1 >= ya2) ||
					 	(ya1 <= yb2 && yb2 <= ya2) || (ya1 >= yb2 && yb2 >= ya2) ||
						(yb1 <= ya1 && ya1 <= yb2) || (yb1 >= ya1 && ya1 >= yb2) ||
					 	(yb1 <= ya2 && ya2 <= yb2) || (yb1 >= ya2 && ya2 >= yb2))
							&&
						((xa1 <= xb1 && xb1 <= xa2) || (xa1 >= xb1 && xb1 >= xa2) ||
					 	(xa1 <= xb2 && xb2 <= xa2) || (xa1 >= xb2 && xb2 >= xa2) ||
						(xb1 <= xa1 && xa1 <= xb2) || (xb1 >= xa1 && xa1 >= xb2) ||
					 	(xb1 <= xa2 && xa2 <= xb2) || (xb1 >= xa2 && xa2 >= xb2))
						)
						{
						//ewe.sys.Vm.debug("("+xa1+", "+ya1+")-"+"("+xa2+", "+ya2+")"+"("+xb1+", "+yb1+")-"+"("+xb2+", "+yb2+")");
						//ewe.sys.Vm.debug(xa1+", "+xa2+": "+xb1+", "+xb2);
						return 1;
						}
			}
		}else{
			double xi = ((cb-ca)/(ma-mb));
			if ((xa1 <= xi && xi <= xa2) || (xa1 >= xi && xi >= xa2))
				if ((xb1 <= xi && xi <= xb2) || (xb1 >= xi && xi >= xb2)){
					//if ((xa1 <= xi && xi <= xa2) || (xa1 >= xi && xi >= xa2))

						//if ((xb1 <= xi && xi <= xb2) || (xb1 >= xi && xi >= xb2))
							//ewe.sys.Vm.debug(xi+" = "+xa1+", "+xa2+": "+xb1+", "+xb2);
					return 1;
				}
		}
	}
	}
	return 0;
}
//===================================================================
static int * getLineParameters(int i,int * xPoints, int * yPoints,int numPoints,int * p)
//===================================================================
{
		int * xx = xPoints;
		int * yy = yPoints;
		int x1 = xx[i], y1 = yy[i];
		int x2, y2;
		if (i == numPoints-1) x2 = xx[0];
		else x2 = xx[i+1];
		if (i == numPoints-1) y2 = yy[0];
		else y2 = yy[i+1];
		p[0] = x1; p[1] = y1;
		p[2] = x2; p[3] = y2;
		return p;
}

//-------------------------------------------------------------------
static int intersectsLines(int * xPoints, int * yPoints, int num, int * xPoints2, int * yPoints2, int num2)
//-------------------------------------------------------------------
{
	int i, j;

	static int p1[4], p2[4];

	for ( i = 0; i<num; i++)

		if (isIn(xPoints2,yPoints2,num2,xPoints[i],yPoints[i])) return 1;
	for ( i = 0; i<num2; i++)
		if (isIn(xPoints,yPoints,num,xPoints2[i],yPoints2[i])) return 1;

	for ( i = 0; i<num; i++){
		getLineParameters(i,xPoints,yPoints,num,p1);
		for ( j = 0; j<num2; j++){
			getLineParameters(j,xPoints2,yPoints2,num2,p2);
			if (intersects(
				p1[0],p1[1],p1[2],p1[3],
				p2[0],p2[1],p2[2],p2[3]
			)) return 1;
		}
	}
	return 0;
}

static Var PolygonIntersects(Var stack[])
{
	int *xp = (int *)WOBJ_arrayStart(stack[0].obj);
	int *yp = (int *)WOBJ_arrayStart(stack[1].obj);
	int num = stack[2].intValue;

	int *xp2 = (int *)WOBJ_arrayStart(stack[3].obj);
	int *yp2 = (int *)WOBJ_arrayStart(stack[4].obj);
	int num2 = stack[5].intValue;
	return returnVar(intersectsLines(xp,yp,num,xp2,yp2,num2));
}
//#########################################################################
// HTML methods.
//#########################################################################

static Var FTMFindCharacter(Var stack[])
{
	Var *tp = objectPtr(stack[0].obj);
	int characterIndex = tp[3].intValue;
	int indexOfCharacter = stack[1].intValue;
	Var *line = objectPtr(tp[1].obj);
	if (characterIndex > indexOfCharacter) return returnVar(0);

	while(TRUE){
		//int tl = line.trueLength();
		int tl = WOBJ_StringLength(line[3].obj);
		if (line[4].intValue & 0x1) tl++;
		//
		if (characterIndex+tl > indexOfCharacter && tl != 0)
			return returnVar(1);
		characterIndex += tl;
		tp[3].intValue = characterIndex;
		//lineIndex++;
		tp[2].intValue++;
		if (line[1].obj == 0) return returnVar(0);
		//line = (DisplayLine)line.next;
		tp[1].obj = line[1].obj;
		line = objectPtr(tp[1].obj);
	}

}

//
// FIXME - fix the unix versions of this method.
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
extern int readAllFileBytes(WObject file,int64 location,char *dest,int numbytes,BOOL readAll);

#define SR_METHOD 1
#define SR_FILE 2
#define SR_RESOURCE 3
#define SR_PARTIAL_FILE 4
#define SR_PARTIAL_RESOURCE 5

//
// Used to read from a Stream.
// Methods that use this must be synchronized with the VM.
//
struct stream_read{
	WObject stream;
	byte* buffer;
	int bufferSize;
	int method;
	//
	// Used for SR_METHOD
	//
	Var pars[4];
	WClassMethod *read;
	WClass *wclass;
	WObject charArray;
	//
	// Used for SR_RESOURCE
	//
	int resourceMemory;
	int resourceOffset;
	int resourceSize;
	//
	// Used for SR_PARTIAL_FILE and SR_PARTIAL_RESOURCE
	//
	int64 partialPos;
	int64 partialStart;
	int64 partialSize;
};
static WClass* rafClass;
//
// Create and return a new stream_read
//
struct stream_read* srCreate(WObject stream,int bufferSize)
{
	struct stream_read* sr = (struct stream_read*)mMalloc(sizeof(struct stream_read));
	memset(sr,0,sizeof(struct stream_read));
	sr->stream = stream;
	sr->method = SR_METHOD;
	sr->wclass = WOBJ_class(stream);
	sr->read = getMethod(sr->wclass,createUtfString("nonBlockingRead"),createUtfString("([BII)I"),&(sr->wclass));
	sr->bufferSize = bufferSize;
	if (bufferSize != 0) sr->buffer = (byte*)mMalloc(bufferSize);
	//
	if (rafClass == NULL){
		rafClass = tryGetClass(createUtfString("ewe/io/RandomAccessFile"));
	}
	if (WOBJ_class(stream) == rafClass)
		sr->method = SR_FILE;
	return sr;
}
//
// Destroy the stream_read object.
//
void srDestroy(struct stream_read* sr)
{
	if (sr->buffer != NULL) free(sr->buffer);
	if (sr->charArray != 0) releaseObject(sr->charArray);
	free(sr);
}
//
// Returns: -1 = Error, -2 = All bytes not present, 0 = End of Stream, >0 = bytes read.
//
int srRead(struct stream_read* sr, byte* dest, int count, BOOL mustReadAll)
{
	int readIn = 0;
	switch(sr->method){
	case SR_METHOD:
		{
		Var ret;
		//
		// Make sure there is enough memory in the char array.
		//
		if (sr->charArray == 0 || WOBJ_arrayLen(sr->charArray) < count){
			if (sr->charArray != 0) releaseObject(sr->charArray);
			sr->charArray = createArrayObject(arrayType('B'),count);
			if (sr->charArray == 0) return -1;
			holdObject(sr->charArray);
		}
		sr->pars[0].obj = sr->stream;
		sr->pars[1].obj = sr->charArray;
		sr->pars[2].intValue = 0;
		sr->pars[3].intValue = count;
		while(readIn < count){
			executeMethodRet(sr->wclass,sr->read,sr->pars,4,&ret);
			if (thrownException) return -1;
			//
			// Check for error.
			//
			if (ret.intValue < -1) return ret.intValue;
			//
			// Check for EOS.
			//
			if (ret.intValue == -1){
				if (mustReadAll) return -2; // Failed to read all.
				if (readIn == 0) return -1;
				break;
			}
			//
			// Check for no bytes yet.
			//
			if (ret.intValue == 0) continue;
			//
			sr->pars[3].intValue -= ret.intValue;
			sr->pars[2].intValue += ret.intValue;
			readIn += ret.intValue;
		}
		memcpy(dest,WOBJ_arrayStart(sr->charArray),readIn);
		return readIn;
		}
	case SR_FILE:
		{
			int got = readAllFileBytes(sr->stream,-1,(char*)dest,count,TRUE);
			if (got < 0) return -1;
			if (mustReadAll && got != count) return -2;
			if (got == 0) return 0;
			return got;
		}
	default: return -2;
	}
}

//#########################################################################
// GIF methods.
//#########################################################################
#ifdef NOGIF
#else

static int gif_code_mask[] = {
     0,
     0x0001, 0x0003,
     0x0007, 0x000F,
     0x001F, 0x003F,
     0x007F, 0x00FF,
     0x01FF, 0x03FF,
     0x07FF, 0x0FFF
     };

typedef struct gifSpecs
{
/* Static variables */
int curr_size;                     /* The current code size */
int clear;                         /* Value for a clear code */
int ending;                        /* Value for a ending code */
int newcodes;                      /* First available code */
int top_slot;                      /* Highest code for current size */
int slot;                          /* Last read code */
/* The following static variables are used
 * for seperating out codes
 */
int navail_bytes;              /* # bytes left in block */
int nbits_left;                /* # bits left in current byte */
byte b1;                           /* Current byte */
int pbytes;                     /* Pointer to next byte in block */
int width;
int height;
int curLine;
int badCodeCount;
int transparentIndex;
WObject colors,buffer,sourceArea,image,stream;
WClassMethod *read;
WClass *wclass;
unsigned char *imageBits, *alpha;
int imageBytesPerPixel;
int ill;
int interlacePass;
int lastInterlaceValue;

#ifdef UNIX
ImageMaker im;
#endif

//Var pars[4]
/*
int stack[] = new int[MAX_CODES + 1];            // Stack for storing pixels
int suffix[] = new int[MAX_CODES + 1];           // Suffix table
int prefix[] = new int[MAX_CODES + 1];           // Prefix linked list
*/
}*GifSpecs;

#define MAX_CODES  4095

int stack[MAX_CODES + 1];            // Stack for storing pixels
int suffix[MAX_CODES + 1];           // Suffix table
int prefix[MAX_CODES + 1];           // Prefix linked list

/* This function initializes the decoder for reading a new image.
 */
//===================================================================
int init_exp(GifSpecs gs,int size)
//===================================================================
{
   gs->curLine = gs->badCodeCount = 0;
   gs->curr_size = size + 1;
   gs->top_slot = 1 << gs->curr_size;
   gs->clear = 1 << size;
   gs->ending = gs->clear + 1;
   gs->slot = gs->newcodes = gs->ending + 1;
   gs->navail_bytes = gs->nbits_left = 0;
   gs->lastInterlaceValue = -8;
   return(0);
}
//===================================================================
int getBytes(GifSpecs gs,byte *dest,int start,int length)
//===================================================================
{
	byte *got;
	if (gs->read == NULL){
		gs->wclass = WOBJ_class(gs->stream);
		gs->read = getMethod(gs->wclass,createUtfString("nonBlockingRead"),createUtfString("([BII)I"),&gs->wclass);
	}
	if (readFully(gs->stream,gs->buffer,length,gs->wclass,gs->read,(char **)&got) != length) return FALSE;
	memcpy(dest,got,length);
	return 1;
}
//===================================================================
int getByte(GifSpecs gs)
//===================================================================
{
	byte b;
	if (!getBytes(gs,&b,0,1)) return -1;
	return (int)b & 0xff;
}

byte gif_byte_buff[257];               // Current block

//===================================================================
int getMoreBytes(GifSpecs gs)
//===================================================================
{
	int ret = TRUE;
	if (gs->navail_bytes > 0) return ret;
	gs->pbytes = 0;
	if ((gs->navail_bytes = getByte(gs)) < 0) ret = FALSE;
	else if (gs->navail_bytes != 0){
		ret = getBytes(gs,gif_byte_buff,0,gs->navail_bytes);
	}
	//if (!ret) ewe.sys.Vm.debug("getMoreBytes() failed!");
	return ret;
}
/* get_next_code()
 * - gets the next code from the GIF file.  Returns the code, or else
 * a negative number in case of file errors...
 */
//===================================================================
int getNextCode(GifSpecs gs)
//===================================================================
   {
   int ret;

   if (gs->nbits_left == 0){
			if (!getMoreBytes(gs)) return -1;
      gs->b1 = gif_byte_buff[gs->pbytes++];
      gs->nbits_left = 8;
      --gs->navail_bytes;
   }
   ret = (int)gs->b1 & 0xff;
   ret = (ret >> (8 - gs->nbits_left)) & 0xff;
   while (gs->curr_size > gs->nbits_left){
			if (!getMoreBytes(gs)) return -1;
      gs->b1 = gif_byte_buff[gs->pbytes++];
      ret |= ((int)gs->b1 & 0xff) << gs->nbits_left;
      gs->nbits_left += 8;
      --gs->navail_bytes;
   }
   gs->nbits_left -= gs->curr_size;
   ret &= gif_code_mask[gs->curr_size];
   return ret;
   }

int interlaceValues[] = {8,0,8,4,4,2,2,1};

//===================================================================
int outputGifLine(GifSpecs gs,int *buff)
//===================================================================
{
	int *colors = NULL, i = 0, actualLine = gs->curLine;
	int lineWidth = gs->width;
	int max = 0;
	if (gs->colors != 0){
		colors = (int *)WOBJ_arrayStart(gs->colors);
		max = WOBJ_arrayLen(gs->colors);
	}
	for (i = 0; i<lineWidth; i++){
		int bi = buff[i];
		if (colors != NULL){
			if (bi < 0 || bi >= max)
				;
			else if (bi == gs->transparentIndex)
				buff[i] = colors[bi];
			else
				buff[i] = colors[bi]|0xff000000;
		}else{
			int off = bi*50;
			buff[i] = off << 16 | off << 8 | off;
		}
	}
	//
	// Find actualLine here.
	//
	if (gs->interlacePass >= 0){
		while(1){
			actualLine = gs->lastInterlaceValue+interlaceValues[gs->interlacePass*2];
			if (actualLine > gs->height-1){
				gs->interlacePass++;
				if (gs->interlacePass == 4) return 0;
				gs->lastInterlaceValue = interlaceValues[gs->interlacePass*2+1]-interlaceValues[gs->interlacePass*2];
				continue;
			}
			gs->lastInterlaceValue = actualLine;
			break;
		}
	}
	if (gs->transparentIndex != -1 && gs->alpha != NULL){
		unsigned char *a = gs->alpha+lineWidth*actualLine;
		for (i = 0; i<lineWidth; i++)
			*a++ = (unsigned char)(buff[i] >> 24) & 0xff;
	}
//
// buff should now hold ARGB pixel values.
// However if the imageBytesPerPixel is not 4 we will have to modify it.
//
	if (gs->imageBytesPerPixel != 4){
		unsigned char *dest = (unsigned char *)buff;
		int i = 0;
		for (i = 0; i<lineWidth; i++){
			int val = buff[i];
			*dest++ = (unsigned char)(val & 0xff);
			*dest++ = (unsigned char)((val >> 8) & 0xff);
			*dest++ = (unsigned char)((val >> 16) & 0xff);
		}
	}
#ifndef UNIX
			if (gs->imageBits) memcpy(gs->imageBits+gs->ill*actualLine,buff,gs->ill);
			else setImageScanLine(gs->image,(unsigned char *)buff,gs->imageBytesPerPixel,actualLine,0,1);
#else
			if (gs->imageBits) memcpy(gs->imageBits+gs->ill*actualLine,buff,gs->ill);
			else gs->im->setScanLine((unsigned char *)buff,gs->imageBytesPerPixel,actualLine,0,1);
#endif

	gs->curLine++;
	return 1;
}
//===================================================================
int gifDecode(GifSpecs gs)
//===================================================================
   {
    int sp, bufptr;
   	static int *buf = NULL;
	static int bufLen = 0;
    int code, fc, oc, bufcnt;
    int c, size, ret;
	int linewidth = gs->width;
	gs->badCodeCount = 0;
   /* Initialize for decoding a new image...
    */
   if ((size = getByte(gs)) < 0)
      return(size);
   if (size < 2 || 9 < size)
      return(-1);//BAD_CODE_SIZE);
   init_exp(gs,size);

   /* Initialize in case they forgot to put in a clear code.
    * (This shouldn't happen, but we'll try and decode it anyway...)
    */
   oc = fc = 0;

   /* Allocate space for the decode buffer
    */

   if (buf == NULL || bufLen < linewidth+1){
		if (buf != NULL) free(buf);
	   buf = (int *)mMalloc((linewidth+1)*sizeof(int));
	   if (buf == NULL) return -1;
	   bufLen = linewidth+1;
   }
   // FIXME
   // Dynamically create stack, etc.

	//buf = new int[linewidth+1];

   /* Set up the stack pointer and decode buffer pointer
    */
   sp = 0;//stack;
   bufptr = 0;//buf;
   bufcnt = linewidth;

   /* This is the main loop.  For each code we get we pass through the
    * linked list of prefix codes, pushing the corresponding "character" for
    * each code onto the stack.  When the list reaches a single "character"
    * we push that on the stack too, and then start unstacking each
    * character for output in the correct order.  Special handling is
    * included for the clear code, and the whole thing ends when we get
    * an ending code.
    */
   while ((c = getNextCode(gs)) != gs->ending)
      {

      /* If we had a file error, return without completing the decode
       */
      if (c < 0)
         {
				 //ewe.sys.Vm.debug("c<0");
         return(0);
         }

      /* If the code is a clear code, reinitialize all necessary items.
       */
      if (c == gs->clear)
         {
         gs->curr_size = size + 1;
         gs->slot = gs->newcodes;
         gs->top_slot = 1 << gs->curr_size;

         /* Continue reading codes until we get a non-clear code
          * (Another unlikely, but possible case...)
          */
         while ((c = getNextCode(gs)) == gs->clear)
            ;

         /* If we get an ending code immediately after a clear code
          * (Yet another unlikely case), then break out of the loop.
          */
         if (c == gs->ending)
            break;

         /* Finally, if the code is beyond the range of already set codes,
          * (This one had better NOT happen...  I have no idea what will
          * result from this, but I doubt it will look good...) then set it
          * to color zero.
          */
         if (c >= gs->slot)
            c = 0;

         oc = fc = c;

         /* And let us not forget to put the char into the buffer... And
          * if, on the off chance, we were exactly one pixel from the end
          * of the line, we have to send the buffer to the outputGifLine()
          * routine...
          */
		 buf[bufptr++] = c;
         //*bufptr++ = c;
         if (--bufcnt == 0)
            {
            if ((ret = outputGifLine(gs, buf)) < 0)
               {
               return(ret);
               }
			if (gs->height<=gs->curLine) return 0;
            bufptr = 0;//buf;
            bufcnt = linewidth;
            }
         }
      else
         {

         /* In this case, it's not a clear code or an ending code, so
          * it must be a code code...  So we can now decode the code into
          * a stack of character codes. (Clear as mud, right?)
          */
         code = c;

         /* Here we go again with one of those off chances...  If, on the
          * off chance, the code we got is beyond the range of those already
          * set up (Another thing which had better NOT happen...) we trick
          * the decoder into thinking it actually got the last code read.
          * (Hmmn... I'm not sure why this works...  But it does...)
          */
         if (code >= gs->slot)
            {
            if (code > gs->slot)
            ++gs->badCodeCount;
            code = oc;
			stack[sp++] = fc; //*sp++ = fc;
            }

         /* Here we scan back along the linked list of prefixes, pushing
          * helpless characters (ie. suffixes) onto the stack as we do so.
          */
         while (code >= gs->newcodes)
            {

            stack[sp++] = suffix[code];
            code = prefix[code];
            }

         /* Push the last character on the stack, and set up the new
          * prefix and suffix, and if the required slot number is greater
          * than that allowed by the current bit size, increase the bit
          * size.  (NOTE - If we are all full, we *don't* save the new
          * suffix and prefix...  I'm not certain if this is correct...
          * it might be more proper to overwrite the last code...
          */
         stack[sp++] = code;
         if (gs->slot < gs->top_slot)
            {
            suffix[gs->slot] = fc = code;
            prefix[gs->slot++] = oc;
            oc = c;
            }
         if (gs->slot >= gs->top_slot)
            if (gs->curr_size < 12)
               {
               gs->top_slot <<= 1;
               ++gs->curr_size;
               }

         /* Now that we've pushed the decoded string (in reverse order)
          * onto the stack, lets pop it off and put it into our decode
          * buffer...  And when the decode buffer is full, write another
          * line...
          */
         while (sp > 0)//stack)
            {
			buf[bufptr++] = stack[--sp];
            //*bufptr++ = *(--sp);
            if (--bufcnt == 0)
               {
               if ((ret = outputGifLine(gs, buf)) < 0)
                  {
                  //free(buf);
                  return(ret);
                  }
			   if (gs->height<=gs->curLine) return 0;
               bufptr = 0;
               bufcnt = linewidth;
               }
            }
         }
      }
   ret = 0;
   if (bufcnt != linewidth)
      ret = outputGifLine(gs, buf);//, (linewidth - bufcnt));
   //free(buf);
   return(ret);
   }

// static native boolean
// decodeGif(pngSpecs specs,BasicStream dataStream,Image image,Rect sourceArea,int [] colorTable, byte [] gifBuffer);

static Var GifDecode(Var stack[])
{
	static struct gifSpecs gs;
	WObject specs = stack[0].obj;
	Var *s = objectPtr(specs);
	int ret = 0;
	gs.width = s[1].intValue;
	gs.height = s[2].intValue;
	gs.interlacePass = s[7].intValue;
	gs.transparentIndex = s[10].intValue;

	gs.stream = stack[1].obj;
	gs.image = stack[2].obj;
	gs.sourceArea = stack[3].obj;
	gs.colors = stack[4].obj;
	gs.buffer = stack[5].obj;
	gs.read = NULL;
#ifndef UNIX
	gs.imageBits = getImageRGBBits(gs.image);
	gs.imageBytesPerPixel = WOBJ_ImageBitsPerPixel(gs.image) == 32 ? 4 : 3;
	gs.ill = WOBJ_ImageLineLength(gs.image);
	gs.alpha = gs.transparentIndex != -1 ? getImageAlpha(gs.image) : NULL;
#else
	gs.im = getImageMaker(gs.image);
	if (gs.im == NULL) return returnVar(0);
	gs.imageBytesPerPixel = gs.im->getBitsPerPixel() == 32 ? 4 : 3;
	gs.ill = gs.im->getBytesPerLine();//WOBJ_ImageLineLength(destImage);
	gs.imageBits = gs.im->getBits();//(unsigned char *)getImageBits(destImage);
	gs.alpha = gs.transparentIndex != -1 ? gs.im->getAlpha() : NULL;
#endif

	ret = gifDecode(&gs) == 0;
#ifndef UNIX
	if (gs.alpha != NULL) setImageAlpha(gs.image,gs.alpha);
#else
	if (gs.imageBits) gs.im->releaseBits(gs.imageBits);
	if (gs.alpha) gs.im->releaseAlpha(gs.alpha);
	delete gs.im;
#endif
	return returnVar(ret);
}
#endif

//#########################################################################
// JPEG methods.
//#########################################################################

#ifdef NOJPEG
static Var JPEGGetSpecs(Var stack[])
{
	return returnVar(0);
}
static Var JPEGToImagePixels(Var stack[])
{
	return returnVar(0);
}
#else
#ifdef UNIX
extern "C" {
#include <jpeglib.h>
}
#else
#include <jpeglib.h>
#endif
/*========================================================
These are definitions for error handling
========================================================*/

#include <setjmp.h>
/*
 * ERROR HANDLING:
 *
 * The JPEG library's standard error handler (jerror.c) is divided into
 * several "methods" which you can override individually.  This lets you
 * adjust the behavior without duplicating a lot of code, which you might
 * have to update with each future release.
 *
 * Our example here shows how to override the "error_exit" method so that
 * control is returned to the library's caller when a fatal error occurs,
 * rather than calling exit() as the standard error_exit method does.
 *
 * We use C's setjmp/longjmp facility to return control.  This means that the
 * routine which calls the JPEG library must first execute a setjmp() call to
 * establish the return point.  We want the replacement error_exit to do a
 * longjmp().  But we need to make the setjmp buffer accessible to the
 * error_exit routine.  To do this, we make a private extension of the
 * standard JPEG error handler object.  (If we were using C++, we'd say we
 * were making a subclass of the regular error handler.)
 *
 * Here's the extended error handler struct:
 */

struct my_error_mgr {
  struct jpeg_error_mgr pub;	/* "public" fields */

  jmp_buf setjmp_buffer;	/* for return to caller */
};

typedef struct my_error_mgr * my_error_ptr;

/*
 * Here's the routine that will replace the standard error_exit method:
 */

METHODDEF(void)
my_error_exit (j_common_ptr cinfo)
{
  /* cinfo->err really points to a my_error_mgr struct, so coerce pointer */
  my_error_ptr myerr = (my_error_ptr) cinfo->err;

  /* Always display the message. */
  /* We could postpone this until after returning, if we chose. */
  (*cinfo->err->output_message) (cinfo);

  /* Return control to the setjmp point */
  longjmp(myerr->setjmp_buffer, 1);
}

/*========================================================
These are definitions for an input source that reads from
a Ewe BasicStream.
========================================================*/
#include <jerror.h>

typedef struct {
  struct jpeg_source_mgr pub;	/* public fields */
  struct stream_read* sr;
/*
  WObject stream;		// source stream
  WObject buffer;		// buffer
  Var pars[4];
  WClassMethod *read;
  WClass *wclass;
*/
  boolean start_of_file;	/* have we gotten any data yet? */
} my_source_mgr;

typedef my_source_mgr * my_src_ptr;

#define INPUT_BUF_SIZE  4096	/* choose an efficiently fread'able size */
/*
 * Initialize source --- called by jpeg_read_header
 * before any data is actually read.
 */

METHODDEF(void)
init_source (j_decompress_ptr cinfo)
{
  my_src_ptr src = (my_src_ptr) cinfo->src;
  /* We reset the empty-input-file flag for each image,
   * but we don't clear the input buffer.
   * This is correct behavior for reading a series of images from one source.
   */
  src->start_of_file = TRUE;
}

METHODDEF(boolean)
fill_input_buffer (j_decompress_ptr cinfo)
{
  unsigned char *data;
  my_src_ptr src = (my_src_ptr) cinfo->src;
  size_t nbytes = 0;

  //Var ret;

  while(nbytes == 0){
	nbytes = srRead(src->sr,src->sr->buffer,INPUT_BUF_SIZE,FALSE);
	/*
	executeMethodRet(src->wclass,src->read,src->pars,4,&ret);
	if (thrownException) nbytes = -1;
	nbytes = ret.intValue;
	*/
  }

  data = src->sr->buffer;//(unsigned char *)WOBJ_arrayStart(src->buffer);
  if (nbytes <= 0) {
    if (src->start_of_file)	/* Treat empty input file as fatal error */
      ERREXIT(cinfo, JERR_INPUT_EMPTY);
    WARNMS(cinfo, JWRN_JPEG_EOF);
    /* Insert a fake EOI marker */
    data[0] = (JOCTET) 0xFF;
    data[1] = (JOCTET) JPEG_EOI;
    nbytes = 2;
  }


  src->pub.next_input_byte = data;
  src->pub.bytes_in_buffer = nbytes;
  src->start_of_file = FALSE;

  return TRUE;
}

METHODDEF(void)
skip_input_data (j_decompress_ptr cinfo, long num_bytes)
{
  my_src_ptr src = (my_src_ptr) cinfo->src;
  if (num_bytes > 0) {
    while (num_bytes > (long) src->pub.bytes_in_buffer) {
      num_bytes -= (long) src->pub.bytes_in_buffer;
      (void) fill_input_buffer(cinfo);
    }
    src->pub.next_input_byte += (size_t) num_bytes;
    src->pub.bytes_in_buffer -= (size_t) num_bytes;
  }
}
METHODDEF(void)
term_source (j_decompress_ptr cinfo)
{
  my_src_ptr src = (my_src_ptr) cinfo->src;
  srDestroy(src->sr);
  //if (src != NULL) releaseObject(src->buffer);
  free(src);
  cinfo->src = NULL;
}
GLOBAL(void)
jpeg_stream_src (j_decompress_ptr cinfo, WObject stream)
{
  my_src_ptr src;

  /* The source object and input buffer are made permanent so that a series
   * of JPEG images can be read from the same file by calling jpeg_stdio_src
   * only before the first one.  (If we discarded the buffer at the end of
   * one image, we'd likely lose the start of the next one.)
   * This makes it unsafe to use this manager and a different source
   * manager serially with the same JPEG object.  Caveat programmer.
   */
  if (cinfo->src == NULL) {	/* first time for this JPEG object? */
    cinfo->src = (struct jpeg_source_mgr *)
		mMalloc(sizeof(my_source_mgr));
		/*
      (*cinfo->mem->alloc_small) ((j_common_ptr) cinfo, JPOOL_PERMANENT,
				  sizeof(my_source_mgr));
				  */
    src = (my_src_ptr) cinfo->src;
	/*
    src->buffer = createArray("B",INPUT_BUF_SIZE);
	holdObject(src->buffer);
	*/
  }

  src = (my_src_ptr) cinfo->src;
  src->pub.init_source = init_source;
  src->pub.fill_input_buffer = fill_input_buffer;
  src->pub.skip_input_data = skip_input_data;
  src->pub.resync_to_restart = jpeg_resync_to_restart; /* use default method */
  src->pub.term_source = term_source;
  src->sr = srCreate(stream,INPUT_BUF_SIZE);
  /*
  src->stream = stream;
  src->pars[0].obj = src->stream;
  src->pars[1].obj = src->buffer;
  src->pars[2].intValue = 0;
  src->pars[3].intValue = INPUT_BUF_SIZE;
  src->read = getMethod(WOBJ_class(src->stream),createUtfString("nonBlockingRead"),createUtfString("([BII)I"),&src->wclass);
  */
  src->pub.bytes_in_buffer = 0; /* forces fill_input_buffer on first read */
  src->pub.next_input_byte = NULL; /* until buffer loaded */
}

static void mlb_destroy_decompress(struct jpeg_decompress_struct *cinfo)
{
	term_source(cinfo);
	jpeg_destroy_decompress(cinfo);
}
static Var JPEGGetInfo(Var stack[],int specsOnly)
{
	WObject specs = stack[0].obj;
	WObject sourceArea = stack[3].obj;
  /* This struct contains the JPEG decompression parameters and pointers to

   * working space (which is allocated as needed by the JPEG library).
   */
  struct jpeg_decompress_struct cinfo;
  /* We use our private extension JPEG error handler.
   * Note that this struct must live as long as the main JPEG parameter
   * struct, to avoid dangling-pointer problems.
   */
  struct my_error_mgr jerr;

  /* More stuff */
 // FILE * infile;		/* source file */
  JSAMPARRAY buffer;		/* Output row buffer */
  int row_stride;		/* physical row width in output buffer */
  //
  /* Now we can initialize the JPEG decompression object. */
  /* We set up the normal JPEG error routines, then override error_exit. */
  //
  cinfo.err = jpeg_std_error(&jerr.pub);
  jerr.pub.error_exit = my_error_exit;
  //
  /* Establish the setjmp return context for my_error_exit to use. */
  //
  if (setjmp(jerr.setjmp_buffer)) {
    /* If we get here, the JPEG code has signaled an error.
     * We need to clean up the JPEG object, close the input file, and return.
     */
    mlb_destroy_decompress(&cinfo);
    //fclose(infile);
    return returnVar(0);
  }

  jpeg_create_decompress(&cinfo);

  /* Step 2: specify data source (eg, a file) */

  jpeg_stream_src(&cinfo, stack[1].obj);

  /* Step 3: read file parameters with jpeg_read_header() */
  if (jpeg_read_header(&cinfo, TRUE) != JPEG_HEADER_OK){
	//fclose(infile);
	mlb_destroy_decompress(&cinfo);
	return returnVar(0);
  }


	if (specsOnly){
		//fclose(infile);
		int ret = 1;
		objectPtr(specs)[1].intValue = cinfo.image_width; //Width
		objectPtr(specs)[2].intValue = cinfo.image_height; //Height
		if (cinfo.num_components != 3 && cinfo.num_components != 1) ret= 0;
		mlb_destroy_decompress(&cinfo);
		return returnVar(ret);
	}else{
		WObject image = stack[2].obj;
		int row = 0;
#ifndef UNIX
		unsigned char *imageBits = getImageRGBBits(image);
		int imageBytesPerPixel = WOBJ_ImageBitsPerPixel(image) == 32 ? 4 : 3;
		int ill = WOBJ_ImageLineLength(image);
#else
	ImageMaker im = getImageMaker(image);
	if (im == NULL) return returnVar(0);
	int imageBytesPerPixel = im->getBitsPerPixel() == 32 ? 4 : 3;
	int ill = im->getBytesPerLine();//WOBJ_ImageLineLength(destImage);
	unsigned char *imageBits = im->getBits();//(unsigned char *)getImageBits(destImage);
#endif

		int w = 0;
		int xx = 0, yy = 0;
		int sw = objectPtr(specs)[1].intValue; //Width
		int sh = objectPtr(specs)[2].intValue; //Height
		int iw = (int)cinfo.image_width;
		int ih = (int)cinfo.image_height;
		int sx = 0, sy = 0;
		unsigned int endLine = 0;
		unsigned char *scanLine;
		unsigned char *scaledLine = (unsigned char *)mMalloc(ill);

		if (sourceArea){
			sx = WOBJ_RectX(sourceArea);
			sy = WOBJ_RectY(sourceArea);
			iw = WOBJ_RectWidth(sourceArea);
			ih = WOBJ_RectHeight(sourceArea);
		}

		endLine = (unsigned int)(sy+ih);

		if (sourceArea != 0 || sw != iw){
			scanLine = (unsigned char *)mMalloc(imageBytesPerPixel*cinfo.image_width);
		}else{
			scanLine = scaledLine;
		}

	    (void) jpeg_start_decompress(&cinfo);
	  /* We can ignore the return value since suspension is not possible
	   * with the stdio data source.
	   */

	  /* We may need to do some setup of our own at this point before reading
	   * the data.  After jpeg_start_decompress() we have the correct scaled
	   * output image dimensions available, as well as the output colormap
	   * if we asked for color quantization.
	   * In this example, we need to make an output work buffer of the right size.
	   */
	  /* JSAMPLEs per row in output buffer */
	  w =  cinfo.output_width;
	  row_stride = w * cinfo.output_components;
	  /* Make a one-row-high sample array that will go away when done with image */

	  buffer = (*cinfo.mem->alloc_sarray)
			((j_common_ptr) &cinfo, JPOOL_IMAGE, row_stride, 1);

	  /* Step 6: while (scan lines remain to be read) */
	  /*           jpeg_read_scanlines(...); */

	  /* Here we use the library's state variable cinfo.output_scanline as the
	   * loop counter, so that we don't have to keep track ourselves.
	   */
	  while (cinfo.output_scanline < endLine) {
		/* jpeg_read_scanlines expects an array of pointers to scanlines.
		 * Here the array is only one element long, but you could ask for
		 * more than one scanline at a time if that's more convenient.
		 */
		(void) jpeg_read_scanlines(&cinfo, buffer, 1);
		if (cinfo.output_scanline-1 < (unsigned int)sy) continue;
		if (row >= sh) continue;
		//
		//Correct the scan line so that each pixel takes up 4 bytes.
		//
		if (cinfo.output_components == 3){
			int s = 0;
			unsigned char *src = buffer[0];
			unsigned char *d = scanLine;

			for (s = 0; s<w; s++, src += 3){
				*d++ = *(src+2);
				*d++ = *(src+1);
				*d++ = *(src+0);
				if (imageBytesPerPixel == 4) *d++ = (unsigned char)0xff;
			}
			//memcpy(imageBits+ill*row,buffer[0],row_stride);
			//setImageScanLine(image,buffer[0],3,row,0,1);
		}else if (cinfo.output_components == 1){
			int s = 0;
			unsigned char *src = buffer[0];
			unsigned char *d = scanLine;
			for (s = 0; s<w; s++, src++){
				unsigned char c = *src;
				*d++ = c;
				*d++ = c;
				*d++ = c;

				if (imageBytesPerPixel == 4) *d++ = (unsigned char)0xff;
			}
		}
		//
		// At this point the scanline has the corrected full line.
		//
		if (scaledLine != scanLine){
			int col = 0;
			unsigned char *p = scanLine;
			unsigned char *d = scaledLine;
			int i;
			for (i = 0; i<w; i++, p += imageBytesPerPixel){
				if (i < sx) continue;
				if (col >= sw) continue;
				while(xx < sw){
					*d++ = *(p+0);
					*d++ = *(p+1);
					*d++ = *(p+2);
					if (imageBytesPerPixel == 4) *d++ = *(p+3);
					xx += iw;
					col++;
					if (col >= sw) break;
				}
				xx -= sw;
			}
		}
		//
		//
		//
		while(yy < sh){
#ifndef UNIX
			if (imageBits) memcpy(imageBits+ill*row,scaledLine,ill);
			else setImageScanLine(image,scaledLine,imageBytesPerPixel,row,0,1);
#else
			if (imageBits) memcpy(imageBits+ill*row,scaledLine,ill);
			else im->setScanLine(scaledLine,imageBytesPerPixel,row,0,1);
#endif
			yy += ih; // Did one row.
			row++;
			if (row >= sh) break;
		}
		yy -= sh;
	  }
	  if (scaledLine != NULL && scaledLine != scanLine) free(scaledLine);
	  if (scanLine != NULL) free(scanLine);
	  /* Step 7: Finish decompression */

	  (void) jpeg_abort_decompress(&cinfo);//finish_decompress(&cinfo);
	  /* We can ignore the return value since suspension is not possible
	   * with the stdio data source.
	   */

	  /* Step 8: Release JPEG decompression object */

	  /* This is an important step since it will release a good deal of memory. */
	  mlb_destroy_decompress(&cinfo);
#ifdef UNIX
	if (imageBits) im->releaseBits(imageBits);
	delete im;
#endif
	  /* After finish_decompress, we can close the input file.
	   * Here we postpone it until after no more JPEG errors are possible,
	   * so as to simplify the setjmp error logic above.  (Actually, I don't
	   * think that mlb_destroy can do an error exit, but why assume anything...)
	   */
	  //fclose(infile);

	  /* At this point you may want to check to see whether any corrupt-data
	   * warnings occurred (test whether jerr.pub.num_warnings is nonzero).
	   */

	  /* And we're done! */
	  return returnVar(1);

	}
}
static Var JPEGGetSpecs(Var stack[])
{
	return JPEGGetInfo(stack,1);
}
static Var JPEGToImagePixels(Var stack[])
{
	return JPEGGetInfo(stack,0);
}
#endif



//#########################################################################3
// ZLIB methods.
//#########################################################################3

#include "deflate.h"
#include <zlib.h>

//#define WOBJ_InflaterStream(inflater) (z_stream *)objectPtr(inflater)[1].refValue
#define WOBJ_InflaterStream(inflater) (z_stream *)WOBJ_arrayStart(objectPtr(inflater)[3].obj)
#define WOBJ_DeflaterStream(deflater) (z_stream *)WOBJ_arrayStart(objectPtr(deflater)[3].obj)


static Var ZLibInflateInit(Var stack[])
{
	Var v;
	WObject inflater = stack[0].obj;
	WObject nowrap = stack[1].intValue, temp;
	unsigned char *mem;
	z_stream *stream;


	temp = wAlloc(sizeof(z_stream),&mem);
	objectPtr(inflater)[3].obj = temp;
	//stream = mMalloc(sizeof(z_stream));
	//WOBJ_InflaterStream(inflater) = stream;
	stream = (z_stream *)mem;
	stream->next_in = (Bytef*)0;
    stream->avail_in = (uInt)0;

    stream->next_out = 0;
    stream->avail_out = 0;

    stream->zalloc = (alloc_func)0;
    stream->zfree = (free_func)0;

    v.intValue = nowrap ? inflateInit2(stream,-MAX_WBITS) : inflateInit(stream);
	return v;
}

static Var ZLibInflate(Var stack[])
{

	Var v;
	WObject inflater = stack[0].obj;
	WObject in = stack[1].obj, out = stack[2].obj;
	z_stream *stream = WOBJ_InflaterStream(inflater);
	Bytef *from = (Bytef *)WOBJ_arrayStart(objectPtr(in)[1].obj);
	uInt fromSize = objectPtr(in)[2].intValue, toSize = WOBJ_arrayLen(objectPtr(out)[1].obj);
	uInt ate, made;
	v.intValue = 0;

	stream->next_in = from;
	stream->avail_in = fromSize;
	stream->next_out = (Bytef *)WOBJ_arrayStart(objectPtr(out)[1].obj);
	stream->avail_out = toSize;



	v.intValue = inflate(stream,0);

	ate = fromSize-stream->avail_in;
	made = toSize-stream->avail_out;
	if (ate < fromSize && ate > 0)
		memmove(from,from+ate,stream->avail_in);
	objectPtr(in)[2].intValue = stream->avail_in;
	objectPtr(out)[2].intValue = made;

	return v;
}
static Var ZLibInflateEnd(Var stack[])
{

	WObject inflater = stack[0].obj;
	z_stream *stream = WOBJ_InflaterStream(inflater); //(z_stream *)WOBJ_arrayStart(objectPtr(inflater)[1].obj);

	Var v;
	inflateEnd(stream);
	v.intValue = 1;
	return v;
}

static Var ZLibDeflateInit(Var stack[])
{

	Var v;
	WObject deflater = stack[0].obj, temp;
	int level = stack[1].intValue;
	WObject nowrap = stack[2].intValue;
	unsigned char *mem;
	z_stream *stream;

	temp = wAlloc(sizeof(z_stream),&mem);
	objectPtr(deflater)[3].obj = temp;
	stream = (z_stream *)mem;
	stream->next_in = (Bytef*)0;
    stream->avail_in = (uInt)0;

    stream->next_out = 0;
    stream->avail_out = 0;

    stream->zalloc = (alloc_func)0;
    stream->zfree = (free_func)0;

    v.intValue = nowrap ?
		deflateInit2_(stream, level, Z_DEFLATED, -MAX_WBITS, DEF_MEM_LEVEL,
			 Z_DEFAULT_STRATEGY, ZLIB_VERSION, sizeof(z_stream))
		: deflateInit(stream,level);
	return v;
}
static Var ZLibDeflate(Var stack[])
{
	Var v;
	WObject deflater = stack[0].obj;
	WObject in = stack[1].obj, out = stack[2].obj;
	int end = stack[3].obj;

	z_stream *stream = WOBJ_DeflaterStream(deflater);//(z_stream *)WOBJ_arrayStart(objectPtr(deflater)[1].obj);
	Bytef *from = (Bytef *)WOBJ_arrayStart(objectPtr(in)[1].obj);
	uInt fromSize = objectPtr(in)[2].intValue, toSize = WOBJ_arrayLen(objectPtr(out)[1].obj);
	uInt ate, made;
	stream->next_in = from;
	stream->avail_in = fromSize;
	stream->next_out = (Bytef *)WOBJ_arrayStart(objectPtr(out)[1].obj);
	stream->avail_out = toSize;

	v.intValue = deflate(stream,end ? Z_FINISH : 0);
	ate = fromSize-stream->avail_in;
	made = toSize-stream->avail_out;

	if (ate < fromSize && ate > 0)
		memmove(from,from+ate,stream->avail_in);
	objectPtr(in)[2].intValue = stream->avail_in;
	objectPtr(out)[2].intValue = made;

	return v;
}
static Var ZLibDeflateEnd(Var stack[])
{
	WObject deflater = stack[0].obj;
	z_stream *stream = WOBJ_DeflaterStream(deflater);//(z_stream *)WOBJ_arrayStart(objectPtr(deflater)[1].obj);
	Var v;
	deflateEnd(stream);
	v.intValue = 1;
	return v;
}

//#########################################################################3
// PNG Methods
//#########################################################################3
//-------------------------------------------------------------------
int PaethPredictor (int a,int b,int c)
//-------------------------------------------------------------------
{
	int p = a+b-c;
	int pa = p-a;
	int pb = p-b;
	int pc = p-c;

	if (pa < 0) pa = -pa;
	if (pb < 0) pb = -pb;
	if (pc < 0) pc = -pc;
	if (pa <= pb && pa <= pc) return a;
	else if (pb <= pc) return b;
	else return c;
}

//-------------------------------------------------------------------
int unfilter(char * line,char * old,int length,int bbp)
//-------------------------------------------------------------------
{
		int w;
		if (line[0] == 1)
			for (w = 0; w<length-1; w++){
				if (w >= bbp)
					line[1+w] += line[1+w-bbp];
			}
		else if (line[0] == 2)
			for (w = 0; w<length-1; w++){
				if (old != NULL)
					line[1+w] += old[1+w];
			}
		else if (line[0] == 3)
			for (w = 0; w<length-1; w++){
				int av = 0;
				if (w >= bbp) av += (line[1+w-bbp] & 0xff);
				if (old != NULL) av += (old[1+w] & 0xff);
				av /= 2;
				line[1+w] += av;
			}
		else if (line[0] == 4)
			for (w = 0; w<length-1; w++){
					line[1+w] +=
						PaethPredictor(w >= bbp ? line[1+w-bbp] & 0xff: 0,old != NULL ? old[1+w] & 0xff: 0, old != NULL && w >= bbp ? old[1+w-bbp] & 0xff :0);
			}
		else if (line[0] != 0)
			return 0;//returnError("Unknown filter: "+line[0],false);
		return 1;

}

//ADAM7 interlacing use.
static int hoffset[] = {0,4,0,2,0,1,0};
static int voffset[] = {0,0,4,0,2,0,1};
static int hfreq[] = {8,8,4,4,2,2,1};
static int vfreq[] = {8,8,8,4,4,2,2};

#ifndef EXTERNAL_PNG_TO_PIXELS
#ifndef UNIX
static uchar *getImageRGBBits(WObject obj);
static uchar *getImageAlpha(WObject obj);
static void setImageAlpha(WObject obj,uchar *alpha);

static void setImageScanLine(WObject image,unsigned char *pixels,int bytesPerPixel,int row,int firstX,int xStep);


static Var PNGToImagePixels(Var stack[])
{
	WObject specs = stack[0].obj;
	WObject dataStream = stack[1].obj;
	WObject destImage = stack[2].obj;
	WObject destArray = stack[3].obj;
	Var *c = objectPtr(specs);
	Var v;
	WClass *sc = NULL;
	WClassMethod *sm = NULL;
 int width = c[1].intValue; //Must be first.
 int height = c[2].intValue; //Must be second.
 int type = c[3].intValue; //Must be third.
 int bitDepth = c[4].intValue; //Must be fourth.
 int compression = c[5].intValue; //Must be fifth.

 int filter = c[6].intValue; //Must be sixth.
 int interlace = c[7].intValue; //Must be 7th.
 WObject paletteBytes = c[8].obj; //Must be 8th.

 WObject transparencyBytes = c[9].obj;//Must be 9th.
 int transparentColor = c[10].intValue; //Must be 10th.
 int transparentColorLow = c[11].intValue; //Must be 11th.
 int maxIdx = paletteBytes != 0 ? WOBJ_arrayLen(paletteBytes)/3 : 0;
 int maxT = transparencyBytes != 0 ? WOBJ_arrayLen(transparencyBytes) : 0;
	//int time = ewe.sys.Vm.getTimeStamp();
	int bytesPerLine = width*3;
	int bbp = 3;

	int maskStart = 0xFF;
	int maskShift = 8;
	int scale = 0;
	int freq = 8/bitDepth;
	int byteStep = 1;

	v.obj = 1;
	sc = WOBJ_class(dataStream);
	if (sc == NULL) return v;
	sm = getMethod(sc,createUtfString("nonBlockingRead"),createUtfString("([BII)I"),&sc);
	if (sm == NULL) return v;
	if (type == 2 || type == 6){
		if (bitDepth == 16){
			bbp = type == 6 ? 8:6;

			bytesPerLine = width*bbp;
		}else{
			bbp = type == 6 ? 4:3;
			bytesPerLine = width*bbp;
		}
	}else if (type != 2){
		if (bitDepth >= 8) {
			bbp = bitDepth/8;
			bytesPerLine = width*bbp;
			byteStep = bbp;
		}else{
			bbp = 1;
			bytesPerLine = (width+freq-1)/freq;
			switch(bitDepth){
			case 1: maskStart = 0x80; maskShift = 1; scale = 7; break;
			case 2: maskStart = 0xC0; maskShift = 2; scale = 6; break;
			case 4: maskStart = 0xF0; maskShift = 4; scale = 4; break;
			case 8: maskStart = 0xFF; maskShift = 8; scale = 0; break;
			}
		}
		if (type == 4) {
			bbp *= 2;
			byteStep *= 2;
			bytesPerLine = width*bbp;
		}
	}

	{
	char *line,*old;
	WObject lineBuff, oldBuff, tempBuff, aLine = 0;
	unsigned char *all, *ach, *alphaChannel;
	int haveImagePixels;
	int linelength = bytesPerLine+1;
	int i, gray, alpha, color, fullV;
	int imageBytesPerPixel = WOBJ_ImageBitsPerPixel(destImage) == 32 ? 4 : 3;
	int ill = WOBJ_ImageLineLength(destImage);
	all = (unsigned char *)getImageRGBBits(destImage);
	alphaChannel = ach = (unsigned char *)getImageAlpha(destImage);
	haveImagePixels = all != NULL;

	if (!haveImagePixels)
		pushObject(aLine = wAlloc(width*imageBytesPerPixel,&all));
	pushObject(lineBuff = wAlloc(bytesPerLine+1,(uchar **)&line));

	pushObject(oldBuff = wAlloc(bytesPerLine+1,(uchar **)&old));

//..................................................................
	if (interlace == 0){
//..................................................................
		for (i = 0; i<height; i++){
			int count = linelength;
			int len = readFully(dataStream,lineBuff,count,sc,sm,&line);//IO.readFully(is,line);
			old = (char *)WOBJ_arrayStart(oldBuff);
			if (aLine != 0) all = (unsigned char *)WOBJ_arrayStart(aLine);
			if (len != count){
				goto returnNow;
				//return (Image)returnError("Error reading scan line: "+(i+1)+" only got: "+len+" bytes.",null);
			}
			if (!unfilter(line,i == 0 ? NULL : old,count,bbp))
				//return null;
				goto returnError;
			if (type == 2 || type == 6){
				//......................................................
				// True Color.
				//......................................................
				int o = haveImagePixels ? i*ill : 0, w = 0;
				for (w = 0; w<width; w++){
					int wo = w*bbp;
					int colorLow = 0;
					color = (bbp == 3 || bbp == 4) ?
						((line[1+wo] & 0xff) << 16)|((line[2+wo] & 0xff) << 8)|((line[3+wo] & 0xff)):
						((line[1+wo] & 0xff) << 16)|((line[3+wo] & 0xff) << 8)|((line[5+wo] & 0xff));
					alpha = 0xff000000;
					if (bbp > 4)
						colorLow = ((line[2+wo] & 0xff) << 16)|((line[4+wo] & 0xff) << 8)|((line[6+wo] & 0xff));

					if (bbp == 4) alpha = (line[4+wo] & 0xff) << 24;
					else if (bbp == 8) alpha = (line[7+wo] & 0xff) << 24;
					else if (transparencyBytes != 0 && color == transparentColor && colorLow == transparentColorLow)
						alpha = 0;
					//all[o] = alpha|color;
					all[o++] = color & 0xff;
					all[o++] = (color >> 8) & 0xff;
					all[o++] = (color >> 16) & 0xff;
					if (imageBytesPerPixel == 4)
						all[o++] = (alpha >> 24) & 0xff;
					if (ach != NULL) *ach++ = (alpha >> 24) & 0xff;
				}
			}else{
				int mask = maskStart;
				int idx = 1;

				int o = haveImagePixels ? i*ill : 0, w = 0;
				int shift = 8-maskShift;
				for (w = 0; w<width; w++){
					int maxed = 0;
					int v = (line[idx] & mask), fullV;
					maxed = v == mask;
					v = v >> shift;
					fullV = bitDepth == 16 ? (line[idx+1] & mask)|v << 8: v;
					alpha = 0xff000000;

					if ((transparencyBytes != 0) && (fullV == transparentColor)){
						//ewe.sys.Vm.debug(fullV+", "+transparentColor,0);
						alpha = 0;
					}
					if (type == 0 || type == 4){
						//......................................................
						// Grayscale.
						//......................................................
						if (maxed) v = 0xff;
						else v = v << scale;
						gray = (v << 16)|(v << 8)|(v);
						if (type == 4) alpha = (line[idx+bbp/2] & 0xff)<<24;
						//all[o+w] = alpha|gray;
						all[o++] = v & 0xff;
						all[o++] = v & 0xff;
						all[o++] = v & 0xff;
						if (imageBytesPerPixel == 4)
							all[o++] = (alpha >> 24) & 0xff;

						if (ach != NULL) *ach++ = (alpha >> 24) & 0xff;
					}else{
						//......................................................
						// Palette.
						//......................................................
						if (v >= maxIdx) v = 0;
						{
							char *pb = paletteBytes == 0 ? NULL : (char *)WOBJ_arrayStart(paletteBytes);
							char *tb = transparencyBytes == 0 ? NULL : (char *)WOBJ_arrayStart(transparencyBytes);

							color = ((pb[v*3] & 0xff) << 16)|((pb[v*3+1] & 0xff) << 8)|((pb[v*3+2] & 0xff));
							alpha = (v < maxT) ? (tb[v] & 0xff) << 24 : 0xff000000;
							//all[o+w] = alpha|color;
							all[o++] = color & 0xff;
							all[o++] = (color >> 8) & 0xff;
							all[o++] = (color >> 16) & 0xff;
							if (imageBytesPerPixel == 4)
								all[o++] = (alpha >> 24) & 0xff;
						}

					}
					if (shift == 0) {
						mask = maskStart;
						shift = 8-maskShift;
						idx += byteStep;
					}else{

						mask >>= maskShift;
						shift -= maskShift;
					}
				}
			}
			if (!haveImagePixels)
				setImageScanLine(destImage,all,imageBytesPerPixel,i,0,1);
			tempBuff = oldBuff; oldBuff = lineBuff; lineBuff = tempBuff;
		}
//..................................................................
	}else if (interlace == 1){
//..................................................................

		int pass;
		for (pass = 1; pass <= 7; pass++){
			int numLines = (height-voffset[pass-1]+(vfreq[pass-1]-1))/vfreq[pass-1];
			int numPixels = (width-hoffset[pass-1]+(hfreq[pass-1]-1))/hfreq[pass-1];
			int numBytes = bbp >= 2 ? bbp*numPixels : (numPixels+freq-1)/freq;
			int pixelLine = voffset[pass-1];

			int ln;
			for (ln = 0; ln<numLines; ln++){
				int len = readFully(dataStream,lineBuff,numBytes+1,sc,sm,&line);//IO.readFully(is,line);
				if (len != numBytes+1)
					goto returnError;
				old = (char *)WOBJ_arrayStart(oldBuff);

				if (aLine != 0) all = (unsigned char *)WOBJ_arrayStart(aLine);
					//return (Image)returnError("Error reading scan line: "+(ln+1)+" only got: "+len+" bytes.",null);
				if (!unfilter(line,ln == 0 ? NULL : old,numBytes+1,bbp))
					goto returnError;
					//return null;
				if (type == 2 || type == 6){
					//......................................................
					// True Color.
					//......................................................
					int o = haveImagePixels ? pixelLine*ill : 0;

					int pixelCol = hoffset[pass-1];
					int w, ax = pixelCol+pixelLine*height;
					o += pixelCol*imageBytesPerPixel;
					for (w = 0; w<numPixels; w++){
						int wo = w*bbp;
						int colorLow = 0;
						alpha = 0xff000000;
						color = (bbp == 3 || bbp == 4) ?
							((line[1+wo] & 0xff) << 16)|((line[2+wo] & 0xff) << 8)|((line[3+wo] & 0xff)):
							((line[1+wo] & 0xff) << 16)|((line[3+wo] & 0xff) << 8)|((line[5+wo] & 0xff));
						if (bbp > 4)
							colorLow = ((line[2+wo] & 0xff) << 16)|((line[4+wo] & 0xff) << 8)|((line[6+wo] & 0xff));
						if (bbp == 4) alpha = (line[4+wo] & 0xff) << 24;
						else if (bbp == 8) alpha = (line[7+wo] & 0xff) << 24;
						else if (transparencyBytes != 0 && color == transparentColor && colorLow == transparentColorLow)
							alpha = 0;
						//all[o+pixelCol] = alpha|color;
						all[o] = color & 0xff;
						all[o+1] = (color >> 8) & 0xff;
						all[o+2] = (color >> 16) & 0xff;
						if (imageBytesPerPixel == 4)
							all[o+3] = (alpha >> 24) & 0xff;
						if (ach != NULL){
							ach[ax] = (alpha >> 24) & 0xff;
							ax += hfreq[pass-1];
						}
						o += hfreq[pass-1]*imageBytesPerPixel;
					}
				}else{
					int mask = maskStart;
					int idx = 1;
					int o = haveImagePixels ? pixelLine*ill : 0;
					int pixelCol = hoffset[pass-1];
					int shift = 8-maskShift;
					int w, ax = pixelCol+pixelLine*height;
					o += pixelCol*imageBytesPerPixel;
					for (w = 0; w<numPixels; w++){
						int maxed = 0;
						int v = (line[idx] & mask);
						maxed = v == mask;
						v = v >> shift;
						fullV = bitDepth == 16 ? (line[idx+1] & mask)|v << 8: v;
						alpha = 0xff000000;

						if ((transparencyBytes != 0) && (fullV == transparentColor))
							alpha = 0;
						if (type == 0 || type == 4){
							//......................................................
							// GrayScale.

							//......................................................
							if (maxed) v = 0xff;
							else v = v << scale;
							gray = (v << 16)|(v << 8)|(v);
							if (type == 4) alpha = (line[idx+bbp/2] & 0xff)<<24;
							//all[o+pixelCol] = alpha|gray;
							all[o] = v & 0xff;
							all[o+1] = v & 0xff;
							all[o+2] = v & 0xff;
							if (imageBytesPerPixel == 4)
								all[o+3] = (alpha >> 24) & 0xff;
							o += hfreq[pass-1]*imageBytesPerPixel;
							if (ach != NULL){
								ach[ax] = (alpha >> 24) & 0xff;
								ax += hfreq[pass-1];
							}
						}else{
							//......................................................
							// Palette.
							//......................................................
							if (v >= maxIdx) v = 0;
							{
								char *pb = paletteBytes == 0 ? NULL : (char *)WOBJ_arrayStart(paletteBytes);
								char *tb = transparencyBytes == 0 ? NULL : (char *)WOBJ_arrayStart(transparencyBytes);
								color = ((pb[v*3] & 0xff) << 16)|((pb[v*3+1] & 0xff) << 8)|((pb[v*3+2] & 0xff));
								alpha = (v < maxT) ? (tb[v] & 0xff) << 24 : 0xff000000;
								all[o+pixelCol] = alpha|color;
								all[o] = color & 0xff;
								all[o+1] = (color >> 8) & 0xff;

								all[o+2] = (color >> 16) & 0xff;
								if (imageBytesPerPixel == 4)
									all[o+3] = (alpha >> 24) & 0xff;
								o += hfreq[pass-1]*imageBytesPerPixel;
								//pixelCol += hfreq[pass-1];
							}

						}
						if (shift == 0) {
							mask = maskStart;
							shift = 8-maskShift;
							idx += byteStep;
						}else{
							mask >>= maskShift;
							shift -= maskShift;
						}
					}
				}
				if (!haveImagePixels)
					setImageScanLine(destImage,all,imageBytesPerPixel,pixelLine,hoffset[pass-1],hfreq[pass-1]);
				tempBuff = oldBuff; oldBuff = lineBuff; lineBuff = tempBuff;
				pixelLine += vfreq[pass-1];

			}
		}
	}else{
		goto returnError;
		//return (Image)returnError("Unknown interlace method: "+interlace,null);
	}
/*
	int now = ewe.sys.Vm.getTimeStamp();
	time = now-time;
	im.setPixels(all,0,0,0,width,height,0);
	now = ewe.sys.Vm.getTimeStamp()-now;
	convertTime = time;
	setPixelTime = now;
	return im;
}
*/
returnError:
returnNow:
	popObject();popObject();
	if (!haveImagePixels) popObject();
	if (alphaChannel != NULL) setImageAlpha(destImage,alphaChannel);

	//if (destArray != 0 && haveImagePixels)

	//	memcpy(WOBJ_arrayStart(destArray),all,sizeof(int)*width*height);
	v.intValue = 1;
	}
	return v;
}

#else
/*================================
This is the template for the imageMaker class.
This will usually be defined in nmXXX_c.c
==================================*/
/*
typedef class imageMaker *ImageMaker;
class imageMaker {
public:
	virtual unsigned char *getBits() = 0;
	virtual void releaseBits(char *bits,int writeBack = 1) = 0;
	virtual void setScanLine(unsigned char *pixels,int bytesPerPixel,int row,int firstX,int xStep) = 0;
	virtual int getBytesPerLine() = 0;
	virtual int getBitsPerPixel() = 0;
	virtual unsigned char *getAlpha() = 0;
	virtual void releaseAlpha(unsigned char *alpha);
};


static ImageMaker getImageMaker(WObject image);
*/
static Var PNGToImagePixels(Var stack[])
{
	WObject specs = stack[0].obj;
	WObject dataStream = stack[1].obj;
	WObject destImage = stack[2].obj;
	WObject destArray = stack[3].obj;
	Var *c = objectPtr(specs);
	Var v;
	WClass *sc = NULL;
	WClassMethod *sm = NULL;

 int width = c[1].intValue; //Must be first.
 int height = c[2].intValue; //Must be second.
 int type = c[3].intValue; //Must be third.
 int bitDepth = c[4].intValue; //Must be fourth.
 int compression = c[5].intValue; //Must be fifth.
 int filter = c[6].intValue; //Must be sixth.
 int interlace = c[7].intValue; //Must be 7th.
 WObject paletteBytes = c[8].obj; //Must be 8th.
 WObject transparencyBytes = c[9].obj;//Must be 9th.
 int transparentColor = c[10].intValue; //Must be 10th.
 int transparentColorLow = c[11].intValue; //Must be 11th.
 int maxIdx = paletteBytes != 0 ? WOBJ_arrayLen(paletteBytes)/3 : 0;
 int maxT = transparencyBytes != 0 ? WOBJ_arrayLen(transparencyBytes) : 0;
	//int time = ewe.sys.Vm.getTimeStamp();
	int bytesPerLine = width*3;
	int bbp = 3;
	int maskStart = 0xFF;
	int maskShift = 8;
	int scale = 0;
	int freq = 8/bitDepth;
	int byteStep = 1;

	v.obj = 1;
	sc = WOBJ_class(dataStream);
	if (sc == NULL) return v;
	sm = getMethod(sc,createUtfString("nonBlockingRead"),createUtfString("([BII)I"),&sc);
	if (sm == NULL) return v;
	if (type == 2 || type == 6){
		if (bitDepth == 16){
			bbp = type == 6 ? 8:6;
			bytesPerLine = width*bbp;
		}else{
			bbp = type == 6 ? 4:3;
			bytesPerLine = width*bbp;
		}
	}else if (type != 2){
		if (bitDepth >= 8) {
			bbp = bitDepth/8;
			bytesPerLine = width*bbp;
			byteStep = bbp;
		}else{
			bbp = 1;
			bytesPerLine = (width+freq-1)/freq;
			switch(bitDepth){
			case 1: maskStart = 0x80; maskShift = 1; scale = 7; break;
			case 2: maskStart = 0xC0; maskShift = 2; scale = 6; break;
			case 4: maskStart = 0xF0; maskShift = 4; scale = 4; break;
			case 8: maskStart = 0xFF; maskShift = 8; scale = 0; break;
			}
		}
		if (type == 4) {
			bbp *= 2;
			byteStep *= 2;
			bytesPerLine = width*bbp;
		}
	}

	{
	ImageMaker im = getImageMaker(destImage);
	if (im == NULL) return v;
	char *line,*old;
	WObject lineBuff, oldBuff, tempBuff, aLine = 0;

	unsigned char *all,*ach, *alphaChannel;
	int haveImagePixels;
	int linelength = bytesPerLine+1;
	int i, gray, alpha, color, fullV;
	int imageBytesPerPixel = im->getBitsPerPixel() == 32 ? 4 : 3;
	int ill = im->getBytesPerLine();//WOBJ_ImageLineLength(destImage);
	all = im->getBits();//(unsigned char *)getImageBits(destImage);
	alphaChannel = ach = im->getAlpha();
	haveImagePixels = all != NULL;

	if (!haveImagePixels)
		pushObject(aLine = wAlloc(width*imageBytesPerPixel,&all));
	pushObject(lineBuff = wAlloc(bytesPerLine+1,(unsigned char **)&line));
	pushObject(oldBuff = wAlloc(bytesPerLine+1,(unsigned char **)&old));

//..................................................................
	if (interlace == 0){
//..................................................................
		for (i = 0; i<height; i++){
			int count = linelength;
			int len = readFully(dataStream,lineBuff,count,sc,sm,&line);//IO.readFully(is,line);
			old = (char *)WOBJ_arrayStart(oldBuff);
			if (aLine != 0) all = (unsigned char *)WOBJ_arrayStart(aLine);
			if (len != count){
returnError:
				goto returnNow;
				//return (Image)returnError("Error reading scan line: "+(i+1)+" only got: "+len+" bytes.",null);
			}
			if (!unfilter(line,i == 0 ? NULL : old,count,bbp))
				//return null;
				goto returnNow;
			if (type == 2 || type == 6){
				//......................................................
				// True Color.
				//......................................................
				int o = haveImagePixels ? i*ill : 0, w = 0;
				for (w = 0; w<width; w++){
					int wo = w*bbp;

					int colorLow = 0;
					color = (bbp == 3 || bbp == 4) ?
						((line[1+wo] & 0xff) << 16)|((line[2+wo] & 0xff) << 8)|((line[3+wo] & 0xff)):
						((line[1+wo] & 0xff) << 16)|((line[3+wo] & 0xff) << 8)|((line[5+wo] & 0xff));
					alpha = 0xff000000;
					if (bbp > 4)
						colorLow = ((line[2+wo] & 0xff) << 16)|((line[4+wo] & 0xff) << 8)|((line[6+wo] & 0xff));
					if (bbp == 4) alpha = (line[4+wo] & 0xff) << 24;
					else if (bbp == 8) alpha = (line[7+wo] & 0xff) << 24;
					else if (transparencyBytes != 0 && color == transparentColor && colorLow == transparentColorLow)
						alpha = 0;
					//all[o] = alpha|color;
					all[o++] = color & 0xff;
					all[o++] = (color >> 8) & 0xff;
					all[o++] = (color >> 16) & 0xff;
					if (imageBytesPerPixel == 4)
						all[o++] = (alpha >> 24) & 0xff;
					if (ach != NULL) *ach++ = (alpha >> 24) & 0xff;
				}
			}else{
				int mask = maskStart;
				int idx = 1;
				int o = haveImagePixels ? i*ill : 0, w = 0;
				int shift = 8-maskShift;
				for (w = 0; w<width; w++){
					int maxed = 0;
					int v = (line[idx] & mask), fullV;

					maxed = v == mask;
					v = v >> shift;
					fullV = bitDepth == 16 ? (line[idx+1] & mask)|v << 8: v;
					alpha = 0xff000000;

					if ((transparencyBytes != 0) && (fullV == transparentColor)){
						//ewe.sys.Vm.debug(fullV+", "+transparentColor,0);
						alpha = 0;
					}
					if (type == 0 || type == 4){
						//......................................................
						// Grayscale.
						//......................................................
						if (maxed) v = 0xff;
						else v = v << scale;
						gray = (v << 16)|(v << 8)|(v);
						if (type == 4) alpha = (line[idx+bbp/2] & 0xff)<<24;
						//all[o+w] = alpha|gray;
						all[o++] = v & 0xff;
						all[o++] = v & 0xff;
						all[o++] = v & 0xff;
						if (imageBytesPerPixel == 4)
							all[o++] = (alpha >> 24) & 0xff;
						if (ach != NULL) *ach++ = (alpha >> 24) & 0xff;
					}else{
						//......................................................
						// Palette.
						//......................................................
						if (v >= maxIdx) v = 0;
						{
							char *pb = paletteBytes == 0 ? NULL : (char *)WOBJ_arrayStart(paletteBytes);
							char *tb = transparencyBytes == 0 ? NULL : (char *)WOBJ_arrayStart(transparencyBytes);
							color = ((pb[v*3] & 0xff) << 16)|((pb[v*3+1] & 0xff) << 8)|((pb[v*3+2] & 0xff));
							alpha = (v < maxT) ? (tb[v] & 0xff) << 24 : 0xff000000;
							//all[o+w] = alpha|color;
							all[o++] = color & 0xff;
							all[o++] = (color >> 8) & 0xff;
							all[o++] = (color >> 16) & 0xff;
							if (imageBytesPerPixel == 4)
								all[o++] = (alpha >> 24) & 0xff;
							if (ach != NULL) *ach++ = (alpha >> 24) & 0xff;
						}
					}
					if (shift == 0) {
						mask = maskStart;
						shift = 8-maskShift;
						idx += byteStep;
					}else{
						mask >>= maskShift;

						shift -= maskShift;
					}
				}
			}
			if (!haveImagePixels)
				im->setScanLine(all,imageBytesPerPixel,i,0,1);
			tempBuff = oldBuff; oldBuff = lineBuff; lineBuff = tempBuff;
		}

//..................................................................
	}else if (interlace == 1){
//..................................................................
		int pass;

		for (pass = 1; pass <= 7; pass++){
			int numLines = (height-voffset[pass-1]+(vfreq[pass-1]-1))/vfreq[pass-1];
			int numPixels = (width-hoffset[pass-1]+(hfreq[pass-1]-1))/hfreq[pass-1];
			int numBytes = bbp >= 2 ? bbp*numPixels : (numPixels+freq-1)/freq;
			int pixelLine = voffset[pass-1];
			int ln;
			for (ln = 0; ln<numLines; ln++){
				int len = readFully(dataStream,lineBuff,numBytes+1,sc,sm,&line);//IO.readFully(is,line);
				if (len != numBytes+1)
					goto returnNow;
				old = (char *)WOBJ_arrayStart(oldBuff);
				if (aLine != 0) all = (unsigned char *)WOBJ_arrayStart(aLine);
					//return (Image)returnError("Error reading scan line: "+(ln+1)+" only got: "+len+" bytes.",null);
				if (!unfilter(line,ln == 0 ? NULL : old,numBytes+1,bbp))
					goto returnNow;
					//return null;
				if (type == 2 || type == 6){
					//......................................................
					// True Color.
					//......................................................
					int o = haveImagePixels ? pixelLine*ill : 0;
					int pixelCol = hoffset[pass-1];
					int w, ax = pixelCol+pixelLine*height;
					o += pixelCol*imageBytesPerPixel;
					for (w = 0; w<numPixels; w++){
						int wo = w*bbp;
						int colorLow = 0;

						alpha = 0xff000000;
						color = (bbp == 3 || bbp == 4) ?
							((line[1+wo] & 0xff) << 16)|((line[2+wo] & 0xff) << 8)|((line[3+wo] & 0xff)):
							((line[1+wo] & 0xff) << 16)|((line[3+wo] & 0xff) << 8)|((line[5+wo] & 0xff));
						if (bbp > 4)
							colorLow = ((line[2+wo] & 0xff) << 16)|((line[4+wo] & 0xff) << 8)|((line[6+wo] & 0xff));
						if (bbp == 4) alpha = (line[4+wo] & 0xff) << 24;
						else if (bbp == 8) alpha = (line[7+wo] & 0xff) << 24;
						else if (transparencyBytes != 0 && color == transparentColor && colorLow == transparentColorLow)
							alpha = 0;
						//all[o+pixelCol] = alpha|color;
						all[o] = color & 0xff;
						all[o+1] = (color >> 8) & 0xff;
						all[o+2] = (color >> 16) & 0xff;
						if (imageBytesPerPixel == 4)
							all[o+3] = (alpha >> 24) & 0xff;
						if (ach != NULL){
							ach[ax] = (alpha >> 24) & 0xff;
							ax += hfreq[pass-1];
						}

						o += hfreq[pass-1]*imageBytesPerPixel;

					}
				}else{
					int mask = maskStart;
					int idx = 1;
					int o = haveImagePixels ? pixelLine*ill : 0;
					int pixelCol = hoffset[pass-1];
					int shift = 8-maskShift;
					int w, ax = pixelCol+pixelLine*height;;
					o += pixelCol*imageBytesPerPixel;
					for (w = 0; w<numPixels; w++){
						int maxed = 0;
						int v = (line[idx] & mask);
						maxed = v == mask;
						v = v >> shift;
						fullV = bitDepth == 16 ? (line[idx+1] & mask)|v << 8: v;
						alpha = 0xff000000;

						if ((transparencyBytes != 0) && (fullV == transparentColor))
							alpha = 0;
						if (type == 0 || type == 4){
							//......................................................
							// GrayScale.
							//......................................................
							if (maxed) v = 0xff;
							else v = v << scale;
							gray = (v << 16)|(v << 8)|(v);
							if (type == 4) alpha = (line[idx+bbp/2] & 0xff)<<24;
							//all[o+pixelCol] = alpha|gray;
							all[o] = v & 0xff;
							all[o+1] = v & 0xff;
							all[o+2] = v & 0xff;
							if (imageBytesPerPixel == 4)

								all[o+3] = (alpha >> 24) & 0xff;
							o += hfreq[pass-1]*imageBytesPerPixel;
							if (ach != NULL){
								ach[ax] = (alpha >> 24) & 0xff;
								ax += hfreq[pass-1];
							}
						}else{
							//......................................................
							// Palette.
							//......................................................
							if (v >= maxIdx) v = 0;
							{
								char *pb = paletteBytes == 0 ? NULL : (char *)WOBJ_arrayStart(paletteBytes);
								char *tb = transparencyBytes == 0 ? NULL : (char *)WOBJ_arrayStart(transparencyBytes);
								color = ((pb[v*3] & 0xff) << 16)|((pb[v*3+1] & 0xff) << 8)|((pb[v*3+2] & 0xff));
								alpha = (v < maxT) ? (tb[v] & 0xff) << 24 : 0xff000000;
								all[o+pixelCol] = alpha|color;
								all[o] = color & 0xff;
								all[o+1] = (color >> 8) & 0xff;
								all[o+2] = (color >> 16) & 0xff;
								if (imageBytesPerPixel == 4)
									all[o+3] = (alpha >> 24) & 0xff;
								o += hfreq[pass-1]*imageBytesPerPixel;
								//pixelCol += hfreq[pass-1];
							}
						}
						if (shift == 0) {
							mask = maskStart;
							shift = 8-maskShift;
							idx += byteStep;
						}else{
							mask >>= maskShift;
							shift -= maskShift;
						}

					}
				}
				if (!haveImagePixels)
					im->setScanLine(all,imageBytesPerPixel,pixelLine,hoffset[pass-1],hfreq[pass-1]);
				tempBuff = oldBuff; oldBuff = lineBuff; lineBuff = tempBuff;
				pixelLine += vfreq[pass-1];
			}
		}
	}else{
		goto returnNow;
		//return (Image)returnError("Unknown interlace method: "+interlace,null);
	}
/*
	int now = ewe.sys.Vm.getTimeStamp();
	time = now-time;
	im.setPixels(all,0,0,0,width,height,0);
	now = ewe.sys.Vm.getTimeStamp()-now;
	convertTime = time;
	setPixelTime = now;
	return im;
}
*/
returnNow:
	if (haveImagePixels) im->releaseBits(all);
	if (alphaChannel)
		im->releaseAlpha(alphaChannel);
	delete im;
	popObject();popObject();
	if (!haveImagePixels) popObject();
	//if (destArray != 0 && haveImagePixels)
	//	memcpy(WOBJ_arrayStart(destArray),all,sizeof(int)*width*height);
	v.intValue = 1;
	}
	return v;
}
#endif //UNIX
#endif //EXTERNAL_PNG_TO_PIXELS

#define INVERT  0
#define INTERSECT  1
#define UNION  2
#define SUBAREA  3
#define TOMASKVALUES  4
#define FROMIMAGEMASK  5
#define FROMIMAGECOLOR  6
#define WHITEOUT  7
#define FROMIMAGEALPHA  8
#define MAKEOPAQUE  9
#define SCALE  10
#define BLEND  11
#define HAS_TRUE_ALPHA 12
#define MAKE_ALPHA 13

#define WOBJ_MaskWidth(OBJ) (objectPtr(OBJ)[1].intValue)
#define WOBJ_MaskHeight(OBJ) (objectPtr(OBJ)[2].intValue)
#define WOBJ_MaskBits(OBJ) (objectPtr(OBJ)[3].obj)

#ifndef UNIX
static int blend(int as,int ad,int s,int d,int shift)
{
	int cd, ascs, adcd;
	ascs = (((s >> shift) & 0xff)*as) >> 8; cd = (d >> shift) & 0xff;
	if (ad == 0xff) ascs += cd-((cd *as) >> 8);
	else if (ad != 0){
		adcd = (cd*ad) >> 8; ascs += adcd; adcd = (adcd*as)>>8; ascs -= adcd;
	}
	if (ascs < 0) ascs = 0;
	else ascs &= 0xff;
	return	ascs << shift;
}
#endif

#define PB_SET_ALPHA 1
#define PB_SCALE_ALPHA 2
#define PB_PUT 3
#define PB_SCALE 4
#define PB_TRANSFORM 10

#define PUT_BLEND  1
#define PUT_SET  2
#define PUT_NONTRANSPARENT_ONLY 0x80000000

static Var PixbufOperation(Var stack[])
{
	WObject pbuf = stack[0].obj;
	WObject par1 = stack[1].obj;
	WObject par2 = stack[2].obj;
	int operation = stack[3].intValue;
	Var *p = objectPtr(pbuf);
	int width = p[1].intValue;
	int height = p[2].intValue;
	int *buffer = (int *)WOBJ_arrayStart(p[3].obj);
	int bufferLen = WOBJ_arrayLen(p[3].obj);
	Var v;

	v.intValue = 0;
	if (operation > PB_TRANSFORM){
		int * src = buffer;
		int * dest = (int *)WOBJ_arrayStart(par1);
		int sx, sy, soff, doff;
		switch(operation-PB_TRANSFORM){
			case 1: // Rotate 90
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = height-sy-1;
					for (sx = 0; sx<width; sx++){
						dest[doff] = src[soff++];
						doff += height;
					}
				}
				break;
			case 2: // Rotate 180
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = (height-sy)*width;
					for (sx = 0; sx<width; sx++){
						dest[--doff] = src[soff++];
					}

				}
				break;
			case 3: // Rotate 270
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = ((width-1)*height)+sy;
					for (sx = 0; sx<width; sx++){
						dest[doff] = src[soff++];
						doff -= height;
					}
				}
				break;

			case 4: // HMirror
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = soff+width;
					for (sx = 0; sx<width; sx++)
						dest[--doff] = src[soff++];
				}
				break;
			case 5: // VMirror
				soff = 0;
				for (sy = 0; sy<height; sy++){
					doff = (height-1-sy)*width;
					for (sx = 0; sx<width; sx++)
						dest[doff++] = src[soff++];
				}
				break;

			default: return returnVar(0);
		}
		return returnVar(1);
	}


	switch(operation){
	case PB_SCALE_ALPHA:{

		int* dest = buffer;

		int* p = (int *)WOBJ_arrayStart(par1);
		int len = bufferLen;
		int alpha = p[0];
		int i;
		for (i = 0; i<len; i++){
			int d = dest[i];
			int a = (((d >> 24) & 0xff)*alpha) >> 8;
			if (a > 0xff) a = 0xff;
			dest[i] = (d & 0xffffff) | (a << 24);
		}
	break;
	}
	case PB_PUT: {
		WObject other = par1;
		WObject *pbuff = (WObject *)WOBJ_arrayStart(par2);
		WObject p2 = pbuff[0];
		uchar *masks = pbuff[1] == 0 ? NULL : (uchar *)WOBJ_arrayStart(pbuff[1]);
		int* dest = buffer;
		int* src = (int *)WOBJ_arrayStart(objectPtr(other)[3].obj);
		int x = WOBJ_RectX(p2), y = WOBJ_RectY(p2), op = WOBJ_RectWidth(p2) & ~PUT_NONTRANSPARENT_ONLY,
			w = objectPtr(other)[1].intValue, h = objectPtr(other)[2].intValue;
		int to = (WOBJ_RectWidth(p2) & PUT_NONTRANSPARENT_ONLY) != 0;
		int so = 0, yy, xx;
		for (yy = 0; yy < h; yy++){
			int off = (yy+y)*width+x;
			int bpl = (w+7)/8;
			int moff = bpl*yy-1;
			uchar by = 0;
			uchar mask = (uchar)0x01;
			for (xx = 0; xx < w; xx++){
				int s;
				if (masks){
					mask = (byte)((mask >> 1) & 0x7f);
					if (mask == 0) {
						mask = (uchar)0x80;
						moff++;
						by = masks[moff];
					}
					if ((by & mask) == 0) {
						so++;
						off++;
						continue;
					}
				}
				s = src[so++];
				if (op == PUT_SET){
					if (!to || ((s & 0xff000000) != 0)) dest[off] = s;
				}else if (op == PUT_BLEND){
					int as = (s >> 24) & 0xff;
					if (as == 0xff) dest[off] = s;
					else if (as == 0)
						;
					else{
						int d = dest[off], ad = (d >> 24) & 0xff;
						int save = 0;
						save |= blend(as,ad,s,d,16);
						save |= blend(as,ad,s,d,8);
						save |= blend(as,ad,s,d,0);
						save |= blend(as,ad,0xff000000,0xff000000,24);

						dest[off] = save;
					}
				}
				off++;
			}
		}
	}
	break;
	case PB_SET_ALPHA:{
		int * dest = buffer;
		int * p = (int *)WOBJ_arrayStart(par1);
		int alpha = p[0], ashift = 0;
		if (alpha < 0) alpha = 0;
		else if (alpha > 255) alpha = 255;
		ashift = alpha << 24;
		if (par2 != 0) {
			uchar * masks = (uchar *)WOBJ_arrayStart(par2);
			int bpl = (width+7)/8;
			int y = 0, i = 0;
			for (y = 0; y<height; y++){
				int off = bpl*y-1;
				byte by = 0;
				byte mask = (byte)0x01;
				int x;
				for (x = 0; x<width; x++){
					mask = (byte)((mask >> 1) & 0x7f);
					if (mask == 0) {
						mask = (byte)0x80;
						off++;
						by = masks[off];
					}
					if ((by & mask) != 0)
						dest[i] = (dest[i] & 0xffffff)|ashift;
					i++;
				}
			}
		}else{
			int len = bufferLen;
			int tc1 = 0, tc2 = 0;
			int hasColor = (tc1 & 0xff000000) == 0;
			int i;
			tc1 = p[1];
			tc2 = p[2];
			for (i = 0; i<len; i++){
				int d = dest[i];
				int d2 = d & 0xffffff;
				if (hasColor){

					if (d2 != tc1 && d2 != tc2) dest[i] = d2 | ashift;
					else dest[i] = d2;
				}else
					dest[i] = d2 | ashift;
			}
		}
	break;
	}
	case PB_SCALE:{
		int * dest = (int *)WOBJ_arrayStart(par1);
		int * src = buffer;
		WObject * pars = (WObject *)WOBJ_arrayStart(par2);
		WObject srcRect = pars[0];
		WObject dstRect = pars[1];
		int h = WOBJ_RectHeight(dstRect), w = WOBJ_RectWidth(dstRect);
		int sx = WOBJ_RectX(srcRect), sy = WOBJ_RectY(srcRect);
		int sh = WOBJ_RectHeight(srcRect), sw = WOBJ_RectWidth(srcRect);
		int line = 0;
		int yy = 0;
		int y = 0;
		for (y = 0; y<sh; y++){
			if (line >= h) continue;
			while(yy < h){
				int *sr = src+(y+sy)*width+sx, x, col = 0, xx = 0;
				for (x = 0; x<sw; x++, sr++){
					while(xx < w){
						*dest++ = *sr;
						xx += sw;
						col++;
						if (col >= w) break;
					}
					xx -= w;
				}
				yy += sh;
				line++;
				if (line >= h) break;
			}
			yy -= h;
		}
		break;
	}

	}
	return v;
}
static Var MaskBitManipulate(Var stack[])
{
	WObject mask = stack[0].obj;
	WObject pars = stack[1].obj;
	int operation = stack[2].intValue;

	Var *p = objectPtr(mask);
	int width = p[1].intValue;
	int height = p[2].intValue;
	WObject bitmap = p[3].obj;
	int * pixels;
	int num = width*height;

	int nb = height*((width+7)/8);
	byte * bits = bitmap == 0 ? NULL : (byte *)WOBJ_arrayStart(bitmap);
	int i;
	int hasTransparent = 0;
	Var v;
	v.obj = 0;

	switch(operation){
		case MAKE_ALPHA:{
			WObject *all = (WObject *)WOBJ_arrayStart(pars);
			int *bits = (int *)WOBJ_arrayStart(all[0]);
			int *mask = all[1] == 0 ? NULL : (int *)WOBJ_arrayStart(all[1]);
			int *others = (int *)WOBJ_arrayStart(all[2]);
			int offset = others[0], length = others[1], color = others[2], color2 = others[3];
			int i;
			if (mask == NULL){
				for (i = 0; i<length; i++){
					bits[offset] &= 0xffffff;
					if (bits[offset] != color && bits[offset] != color2) bits[offset] |= 0xff000000;
					offset++;
				}
			}else{
				int mo = 0;
				for (i = 0; i<length; i++){
					bits[offset] &= 0xffffff;
					if ((mask[mo++] & 0xffffff) != 0) bits[offset] |= 0xff000000;
					offset++;
				}

			}
			break;
		}
		case HAS_TRUE_ALPHA:
			{
				int dest;
				pixels = (int *)WOBJ_arrayStart(pars);
				num = WOBJ_arrayLen(pars);
				for (dest = 0; dest<num; dest++)
					if (((pixels[dest] & 0xff000000) != 0) && ((pixels[dest] & 0xff000000) != 0xff000000)){
						v.obj = stack[0].obj;
						break;
					}
				return v;
			}

		case BLEND:{
			WObject *ps = (WObject *)WOBJ_arrayStart(pars);
			int *dest = (int *)WOBJ_arrayStart(ps[0]);
			int *src = (int *)WOBJ_arrayStart(ps[1]);
			int len = WOBJ_arrayLen(ps[0]);
			int i;
			for (i = 0; i<len; i++){
				int d = dest[i] & 0xffffff, s = src[i];
				int as = (s >> 24) & 0xff,
						ad = 0xff; //FIXME get the actual value.
				if (as == 0xff) dest[i] = s;
				else if (as == 0) continue;
				else{
					int save = 0;
					save |= blend(as,ad,s,d,16);
					save |= blend(as,ad,s,d,8);
					save |= blend(as,ad,s,d,0);
					save |= blend(as,ad,0xff000000,0xff000000,24);
					dest[i] = save;
				}
			}
			break;
		}
		case SCALE:{
			WObject *ps = (WObject *)WOBJ_arrayStart(pars);
			int *src = (int *)WOBJ_arrayStart(ps[0]);
			int *dest = (int *)WOBJ_arrayStart(ps[1]);
			WObject srcRect = ps[2];
			WObject newDim = ps[3];
	int h = objectPtr(newDim)[2].intValue, w = objectPtr(newDim)[1].intValue;
	int sh = WOBJ_RectHeight(srcRect), sw = WOBJ_RectWidth(srcRect);
	double xsc = (double)sw/(double)w;
	double ysc = (double)sh/(double)h;
	double y = 0;
	int line;
	for (line = 0; line < h; line++, y += ysc){
		int off = line*w, col;
		int srcOff = (int)y*sw;
		double x = 0;
		if (y >= sh) y = sh;
		for (col = 0; col < w; col++, x += xsc){
			if (x >= sw) x = sw-1;
			dest[off++] = src[srcOff+(int)x];
		}
	}
			break;
		}
		case MAKEOPAQUE: {
			memset(WOBJ_arrayStart(pars),0xff,WOBJ_arrayLen(pars));
			break;
		}
		case INVERT: {
			byte *o = pars == 0 ? NULL : (byte *)WOBJ_arrayStart(pars);
			if (o == NULL)
				for (i = 0; i<nb; i++) bits[i] = (byte)~bits[i];
			else
				for (i = 0; i<nb; i++) bits[i] = (byte)(bits[i] ^ o[i]);
			}
			break;
		case INTERSECT:{

			byte *o = pars == 0 ? NULL : (byte *)WOBJ_arrayStart(pars);
			for (i = 0; i<nb; i++) bits[i] = (byte)(bits[i] & o[i]);
		}
			break;
		case UNION:{
			byte *o = pars == 0 ? NULL : (byte *)WOBJ_arrayStart(pars);
			for (i = 0; i<nb; i++) bits[i] = (byte)(bits[i] | o[i]);
		}
			break;

		case TOMASKVALUES:
		case WHITEOUT:
		/*case FROMIMAGEMASK:*/

		case FROMIMAGECOLOR:
		case FROMIMAGEALPHA:
		{
			int isFrom = operation == FROMIMAGEMASK || operation == FROMIMAGECOLOR || operation == FROMIMAGEALPHA;
			int isWhite = operation == WHITEOUT;
			int sl = (width+7)/8;
			int off = 0, dest = 0;
			int x, y;
			int tcolor = 0;
			if (operation == FROMIMAGECOLOR){
				WObject *p2 = (WObject *)WOBJ_arrayStart(pars);
				WObject c = p2[1];
				//if (true) return v;
				pixels = (int *)WOBJ_arrayStart(p2[0]);
				tcolor = (int)getLong(c) & 0xffffff;

			}else{
				pixels = (int *)WOBJ_arrayStart(pars);
			}
			for (y = 0; y<height; y++){
				byte by = 0;
				byte mask = (byte)0x01;
				off = sl*y-1;

				for (x = 0; x<width; x++){
					mask = (byte)((mask >> 1) & 0x7f);
					if (mask == 0) {
						mask = (byte)0x80;
						off++;
						by = bits[off];
					}
					if (operation == FROMIMAGEALPHA){
						if ((pixels[dest] & 0xff000000) != 0){
							bits[off] |= mask;

						}else{
							hasTransparent = 1;
						}
					}else if (isFrom){
						if ((pixels[dest] & 0xffffff) != tcolor) {
							bits[off] |= mask;
						}else
							hasTransparent = 1;
					}else{
						if ((by & mask) == 0) pixels[dest] = 0xffffffff;
						else if (!isWhite) pixels[dest] = 0xff000000;
						else pixels[dest] |= 0xff000000;
					}
					dest++;
				}
			}
		}
			break;
	}
	if (operation == FROMIMAGEALPHA || operation == FROMIMAGECOLOR)
 		if (hasTransparent) v.obj = stack[0].obj;
	return v;


}

#define DEFAULT_DOUBLE_DECIMAL_PLACES 12
static Var ConvertDoubleToString(Var stack[])
{
	Var v;
	double value = vars2double(stack);
	int len = 0;
	int dec = DEFAULT_DOUBLE_DECIMAL_PLACES;
	int options = FREE_DECIMAL|AT_LEAST_ONE_DECIMAL;
	doubleToString((char *)sbytes,value,len,dec,options);
	v.obj = createString((char *)sbytes);
	return v;
}
static Var ConvertLongToString(Var stack[])
{
	Var v;
	char chars[40];
	int64 value = vars2int64(stack);
	longToString(chars,value,0,0);
	v.obj = createString(chars);
	return v;
}
static char *convertText = NULL;
static int convertTextSize = 0;

static char *validateNumberChars(Var stack[],int *length)
{
	WObject chars = stack[0].obj;
	if (stack[0].obj == 0)
		throwException(NullPointerEx,NULL);
	else{
		int offset = stack[1].intValue;
		int len = stack[2].intValue;
		if (len <= 0)
			throwException(NumberFormatEx,NULL);
		else if (offset < 0 || offset+len > WOBJ_arrayLen(chars))
			throwException(ArrayIndexEx,NULL);
		else {
			WCHAR *wc = (WCHAR*)WOBJ_arrayStart(chars)+offset;
			if (convertTextSize < (len+1) || convertTextSize == 0){
				if (convertText != NULL) free(convertText);
				convertTextSize = len+1;
				convertText = (char *)mMalloc(convertTextSize);
			}
			if (convertText == NULL){
				convertTextSize = 0;
				throwException(OutOfMemoryEx,NULL);
			}else{
				int i;
				for (i = 0; i<len; i++)
					convertText[i] = (char)wc[i];
				convertText[i] = 0;
				*length = len;
				return convertText;
			}
		}
	}
	return NULL;
}
static Var ConvertParseIntLong(Var stack[],int isLong)
{
	int length;
	int radix = stack[3].intValue;
	char *text = validateNumberChars(stack,&length);
	if (text == NULL) return returnVar(0);
	else if (radix < 2 || radix > 36) throwException(IllegalArgEx,NULL);
	else{
		int neg = text[0] == '-';
		if (neg) text++, length--;
		if (length == 0) throwException(NumberFormatEx,text);
		else {
			int64 curValue = 0, ov;
			int i;
			for (i = 0; i<length; i++){
				char c = text[i];
				int v = -1;
				if (c >= '0' && c <= '9') v = c-'0';
				else if (c >= 'a' && c <= 'z') v = c-'a'+10;
				else if (c >= 'A' && c <= 'Z') v = c-'A'+10;
				if (v == -1 || v >= radix)
					return returnException(NumberFormatEx,text);
				ov = curValue;
				curValue *= radix;
				curValue += v;
				if (ov > curValue)
					if (curValue == cINT64(0x8000000000000000L) && neg && i == length-1 && isLong)
						return returnLong(curValue);
					else
						return returnException(NumberFormatEx,text);
			}
			if (neg) curValue = -curValue;
			if (isLong) return returnLong(curValue);
			else {
				int value = (int)curValue;
				int64 v2 = (int64)value;
				if (v2 != curValue) return returnException(NumberFormatEx,text);
				return returnVar(value);
			}
		}

	}
	return returnVar(0);
}
static Var ConvertParseLong(Var stack[])
{
	return ConvertParseIntLong(stack,1);
}
static Var ConvertParseInt(Var stack[])
{
	return ConvertParseIntLong(stack,0);
}
static char *parseDecimalDouble(char *from,double *dest,int allowDecimal)
{
	double ret = 0;

	double div = 10;

	double neg = 1;
	char *s = from;

	int numDigits = 0;

	checkSymbols();
	*dest = 0;
	if (from == 0) return s;
	if (*s == negSign) neg = -1;
	if (*s == negSign || *s == plusSign) s++;
	if (*s == 0) return NULL;
	for(;*s != 0;s++){
		char c = *s;
		//if (c == groupPoint) continue;
		if (c<'0' || c >'9') break;
		ret = (ret*10.0)+(double)(c-'0');
		numDigits++;
	}
	if (*s != decimalPoint){
		if (numDigits == 0) return NULL;
		*dest = ret*neg;
		return s;
	}
	if (!allowDecimal) return NULL;
	for(s++;*s != 0;s++){
		char c = *s;
		if (c<'0' || c >'9') break;
		ret += (double)(c-'0')/div;
		numDigits++;
		div *= 10.0;
	}
	if (numDigits == 0) return NULL;
	*dest = ret*neg;
	return s;
}
static Var ConvertParseDouble(Var stack[])
{
		int length;
		int radix = stack[3].intValue;
		char *text = validateNumberChars(stack,&length);
		if (text == NULL) return returnVar(0);
		else{
			double ret = 0, exp = 0;
			char *s = parseDecimalDouble(text,&ret,1);
			if (s == NULL) {
				return returnException(NumberFormatEx,text);
			}
			if (*s != 0){
				if (*s != 'e' && *s != 'E')
					return returnException(NumberFormatEx,text);
				s = parseDecimalDouble(s+1,&exp,0);
				if (s == NULL || *s != 0) return returnException(NumberFormatEx,text);
				ret = ret*pow(10,exp);
			}
			return returnDouble(ret);
		}
}

static WCHAR *validateFormatChars(Var stack[],int *maxLength,int isDoubleOrLong)
{
	int offset = (isDoubleOrLong ? 2 : 1);
	WObject chars = stack[offset].obj;
	*maxLength = 0;

	if (chars == 0) return NULL;
	else {
		int start = stack[offset+1].intValue;
		if (start < 0 || start >= WOBJ_arrayLen(chars)) {
			*maxLength = -1;
			throwException(ArrayIndexEx,NULL);
			return NULL;
		}else{
			*maxLength = WOBJ_arrayLen(chars)-start;
			return (WCHAR *)WOBJ_arrayStart(chars)+start;
		}
	}
}
static int ConvertFormatIntLong(WCHAR *dest, int max, int64 value, int radix, int options, int isLong)
{
	int cur = 0, i, j;
	char st = options & 2 ? 'A' : 'a';
	int neg = value < 0;
	int shift = 0, digit;
	int64 mask = 0, check = 0;
	if (radix < 2 || radix > 36) {
		throwException(IllegalArgEx,NULL);
		return -1;
	}
	if (options & 1) {
		if (!isLong) value &= cINT64(0xffffffffL);
		switch(radix){ // Unsigned
		case 2: shift = 1; mask = ~cINT64(0x8000000000000000L); check = 0x1; break;
		case 4: shift = 2; mask = ~cINT64(0xC000000000000000L); check = 0x3; break;
		case 8: shift = 3; mask = ~cINT64(0xE000000000000000L); check = 0x7; break;
		case 16: shift = 4; mask = ~cINT64(0xF000000000000000L); check = 0xf; break;
		case 32: shift = 5; mask = ~cINT64(0xF800000000000000L); check = 0x1f; break;
		default:
			throwException(IllegalArgEx,NULL);
			return -1;
		}
	}

	if (neg && shift == 0) value = -value;
	while(value != 0){
		if (shift == 0){
			digit = (int)(value % radix);
			value /= radix;
		}else{
			digit = (int)(value & check);
			value >>= shift;
			value &= mask;
		}
		if (dest){
			char c = (digit < 10 ? (char)('0'+digit) : (char)(st+digit-10));
			if (cur == max) {
				throwException(ArrayIndexEx,NULL);
				return -1;
			}
			dest[cur] = (WCHAR)c;
		}
		cur++;
	}
	if (cur == 0){
		if (dest){
			if (cur == max) {
				throwException(ArrayIndexEx,NULL);
				return -1;
			}
			dest[cur] = (WCHAR)'0';
		}
		cur++;
	}
	if (neg && (shift == 0)){
		if (dest){
			if (cur == max) {
				throwException(ArrayIndexEx,NULL);
				return -1;
			}
			dest[cur] = (WCHAR)'-';
		}
		cur++;
	}
	if (dest)
		for (i = 0, j = cur-1; i<j; i++, j--){

			WCHAR t = dest[i];
			dest[i] = dest[j];
			dest[j] = t;
		}
	return cur;
}
static Var ConvertFormatInt(Var stack[])
{
	int value = stack[0].intValue, radix = stack[3].intValue, options = stack[4].intValue, max;
	WCHAR *ch = validateFormatChars(stack,&max,0);
	if (max == -1) return returnVar(0);
	return returnVar(ConvertFormatIntLong(ch,max,(int64)value,radix,options,0));
}

static Var ConvertFormatLong(Var stack[])
{
	int64 value = vars2int64(stack);
	int radix = stack[4].intValue, options = stack[5].intValue, max;
	WCHAR *ch = validateFormatChars(stack,&max,1);
	if (max == -1) return returnVar(0);
	return returnVar(ConvertFormatIntLong(ch,max,value,radix,options,1));
}
static Var ConvertFormatDouble(Var stack[])
{
	double value = vars2double(stack);
	int upper = stack[4].intValue & 2, max;
	WCHAR *ch = validateFormatChars(stack,&max,1);
	if (max == -1) return returnVar(0);
	doubleToString((char *)sbytes,value,0,DEFAULT_DOUBLE_DECIMAL_PLACES,FREE_DECIMAL|AT_LEAST_ONE_DECIMAL);
	if (ch != NULL){
		int len = strlen((char *)sbytes), i;
		if (len > max) return returnException(ArrayIndexEx,NULL);
		for (i = 0; i<len; i++){
			ch[i] = (WCHAR)sbytes[i];
			if (upper && sbytes[i] == 'e')
				ch[i] = (WCHAR)'E';
		}
	}
	return returnVar(strlen((char *)sbytes));
}
Var UtilsIndexOf(Var stack[])
{
	WObject obj = stack[0].obj;
	if (obj == 0) return returnException(NullPointerEx,NULL);
	if (WOBJ_class(obj) != NULL) return returnException(IllegalArgEx,NULL);
	else{
		int64 lookingFor = vars2int64(stack+1);
		int minIndex = stack[3].intValue;
		int maxIndex = stack[4].intValue;
		int backwards = stack[5].intValue;
		int len = WOBJ_arrayLen(obj);
		void *st = WOBJ_arrayStart(obj);
		int type = WOBJ_arrayType(obj);
		int i;
		if (maxIndex > len) maxIndex = len;
		//
		if (backwards){
			if (minIndex >= maxIndex) minIndex = maxIndex-1;
			if (minIndex < 0) return returnVar(-1);
		}else{
			if (minIndex < 0) minIndex = 0;
			if (minIndex >= maxIndex) return returnVar(-1);
		}

		switch(arrayChar(type)){
		case 'Z':
		case 'B':
			{
				byte* s = (byte*)st;
				byte look = (byte)lookingFor;
				if (arrayChar(type) == 'Z' && look != 0)
					look = 1;
				if (backwards) for (i = maxIndex-1; i >= minIndex; i--) {if (s[i] == look) return returnVar(i);}
				else for (i = minIndex; i < maxIndex; i++) {if (s[i] == look) return returnVar(i);}
				break;
			}
		case 'J':
			{
				int64* s = (int64*)st;
				int64 look = (int64)lookingFor;
				if (backwards) for (i = maxIndex-1; i >= minIndex; i--) {if (s[i] == look) return returnVar(i);}
				else for (i = minIndex; i < maxIndex; i++) {if (s[i] == look) return returnVar(i);}
				break;
			}
		case 'D':
			{
				double* s = (double*)st;
				double look = vars2double(stack+1);
				if (backwards) for (i = maxIndex-1; i >= minIndex; i--) {if (s[i] == look) return returnVar(i);}
				else for (i = minIndex; i < maxIndex; i++) {if (s[i] == look) return returnVar(i);}
				break;
			}
		case 'F':
			{
				float* s = (float*)st;
				float look = (float)vars2double(stack+1);
				if (backwards) for (i = maxIndex-1; i >= minIndex; i--) {if (s[i] == look) return returnVar(i);}
				else for (i = minIndex; i < maxIndex; i++) {if (s[i] == look) return returnVar(i);}
				break;
			}

		case 'I':
			{
				int* s = (int*)st;
				int look = (int)lookingFor;
				if (backwards) for (i = maxIndex-1; i >= minIndex; i--) {if (s[i] == look) return returnVar(i);}
				else for (i = minIndex; i < maxIndex; i++) {if (s[i] == look) return returnVar(i);}
				break;
			}
		case 'C':
			{
				WCHAR* s = (WCHAR*)st;
				WCHAR look = (WCHAR)lookingFor;
				if (backwards) for (i = maxIndex-1; i >= minIndex; i--) {if (s[i] == look) return returnVar(i);}
				else for (i = minIndex; i < maxIndex; i++) {if (s[i] == look) return returnVar(i);}
				break;
			}
		case 'S':
			{
				short* s = (short*)st;
				short look = (short)lookingFor;
				if (backwards) for (i = maxIndex-1; i >= minIndex; i--) {if (s[i] == look) return returnVar(i);}
				else for (i = minIndex; i < maxIndex; i++) {if (s[i] == look) return returnVar(i);}
				break;
			}
		}
		return returnVar(-1);
	}
}
Var UtilsZero(Var stack[])
{
	WObject obj = stack[0].obj;
	int offset = stack[1].intValue;
	int length = stack[2].intValue;

	int size = arrayTypeSize(WOBJ_arrayType(obj));
	char *start = (char *)WOBJ_arrayStart(obj)+(offset*size);
	if (offset < 0 || offset+length > WOBJ_arrayLen(obj))
		return returnException(ArrayIndexEx,NULL);
	memset(start,0,length*size);
	return returnVar(1);
}
Var RafileQuickWrite(Var stack[])
{
	WObject data = stack[1].obj;
	int offset = stack[2].intValue;
	int length = stack[3].intValue;
	int ret = 0;
	if (data == 0) return returnException(NullPointerEx,NULL);
	if (offset < 0 || offset+length > WOBJ_arrayLen(data)) return returnException(ArrayIndexEx,NULL);
	ret = writeAllFileBytes(stack[0].obj,-1,(char *)WOBJ_arrayStart(data)+offset,length);
	if (ret != length) return returnException(IOException,NULL);
	return returnVar(length);
}
Var RafileQuickRead(Var stack[])
{
	WObject data = stack[1].obj;
	int offset = stack[2].intValue;
	int length = stack[3].intValue;
	int ret = 0;
	if (data == 0) return returnException(NullPointerEx,NULL);
	if (offset < 0 || offset+length > WOBJ_arrayLen(data)) return returnException(ArrayIndexEx,NULL);
	ret = readAllFileBytes(stack[0].obj,-1,(char *)WOBJ_arrayStart(data)+offset,length,TRUE);
	if (ret < 0) return returnException(IOException,NULL);//ret == -2 ? (char *)"Stream ended." : NULL);
	if (stack[4].intValue && ret != length) return returnException(IOException,"Stream ended.");
	if (ret == 0) return returnVar(-1);
	return returnVar(ret);
}
Var RecordFileGetAllRecords(Var stack[])
{
	WObject file = objectPtr(stack[0].obj)[1].obj;
	int64 next = (int64)stack[1].intValue;
	WObject dest = stack[2].obj;
	int offset = stack[3].intValue;
	int canRead = WOBJ_arrayLen(dest)-offset;
	int *d = (int *)WOBJ_arrayStart(dest)+offset;
	char buff[4];
	uint32 got, did = 0;
	if (canRead > 0)
		while(canRead){
			if (readAllFileBytes(file,next,buff,4,TRUE) != 4) return returnVar(-1);
			got = (uint32)(buff[3]);
			got = getUInt32(buff);
			if (got == 0) break;
			if ((got & 0xff000000) == 0x30000000){
				*d++ = (int)(next>>4);
				canRead--;
				did++;
			}
			next += (got & 0x00ffffff) << 4;
		}
	return returnVar(did);
}
static Var IntArrayIndexOf(Var stack[])
{
	WObject data = objectPtr(stack[0].obj)[1].obj;
	int length = objectPtr(stack[0].obj)[2].intValue;
	int *dest = (int *)WOBJ_arrayStart(data);
	int look = stack[1].intValue, i = 0;
	for (i = 0; i<length; i++)
		if (*dest++ == look) return returnVar(i);
	return returnVar(-1);
}
static int *appendingIntArray(int where,int numInts,WObject dest)
{
	WObject data = objectPtr(dest)[1].obj;
	int length = objectPtr(dest)[2].intValue;
	int growSize = objectPtr(dest)[3].intValue;
	int *d;
	if (where < 0) where = length;
	if (length+numInts > WOBJ_arrayLen(data)){
		WObject nd;
		if (growSize <= 0) growSize = length+1;
		if (growSize > 100000) growSize = 100000;
		if (growSize < numInts) growSize = numInts;
		nd = createArrayObject(arrayType('I'),length+growSize);
		if (nd == 0) return NULL;
		memcpy(WOBJ_arrayStart(nd),WOBJ_arrayStart(data),length*sizeof(int32));
		objectPtr(dest)[1].obj = nd;
	}
	objectPtr(dest)[2].intValue += numInts;
	d = (int *)WOBJ_arrayStart(objectPtr(dest)[1].obj)+where;
	if (where<length)
		memmove(d+numInts,d,(length-where)*sizeof(int32));
	return d;
}
static jbyte *appendingByteArray(int where,int numBytes,WObject dest,int *didAlloc)
{
	WObject data = objectPtr(dest)[1].obj;
	int length = objectPtr(dest)[2].intValue;
	int growSize = objectPtr(dest)[3].intValue;
	jbyte *d;
	*didAlloc = 0;
	if (where < 0) where = length;
	if (length+numBytes > WOBJ_arrayLen(data)){
		WObject nd;
		*didAlloc = 1;
		if (growSize <= 0) growSize = length+1;
		if (growSize > 100000) growSize = 100000;
		if (growSize < numBytes) growSize = numBytes;
		nd = createArrayObject(arrayType('B'),length+growSize);
		memcpy(WOBJ_arrayStart(nd),WOBJ_arrayStart(data),length*1);
		objectPtr(dest)[1].obj = nd;
	}
	objectPtr(dest)[2].intValue += numBytes;
	d = (jbyte *)WOBJ_arrayStart(objectPtr(dest)[1].obj)+where;
	if (where<length)
		memmove(d+numBytes,d,length-where);
	return d;
}
#define BytesFor(SOURCE) ((jbyte *)WOBJ_arrayStart(objectPtr(SOURCE)[1].obj))
//-------------------------------------------------------------------
int readVarInt(WObject from,int *where,int *value)
//-------------------------------------------------------------------
{
	jbyte *source = BytesFor(from);
	int offset = *where;
	int left = objectPtr(from)[2].intValue-offset;
	if (left < 1) return 0;
	else{
		jbyte one = source[offset];
		int cx = one & 0xc0;
		if (one == (jbyte)0xff || cx == 0) {
			*where += 1;
			*value = (int)one;
		}else if (cx == 0x40){
			if (left < 2) return 0;
			*where += 2;
			*value = ((one & 0x3f) << 8)|(source[offset+1] & 0xff);
		}else if (cx == 0x80){
			if (left < 3) return 0;
			*where += 3;
			*value = ((one & 0x3f) << 16)|((source[offset+1] & 0xff) << 8)| (source[offset+2] & 0xff);
		}else if ((one & 0xe0) == 0xc0){
			if (left < 4) return 0;
			*where += 4;
			*value = ((one & 0x1f) << 24)|((source[offset+1] & 0xff) << 16)|((source[offset+2] & 0xff) << 8)| (source[offset+3] & 0xff);
		}else if ((one & 0xe0) == 0xe0){
			if (left < 5) return 0;
			*where += 5;
			*value = ((source[offset+1] & 0xff) << 24)|((source[offset+2] & 0xff) << 16)|((source[offset+3] & 0xff) << 8)| (source[offset+4] & 0xff);
		}else
			return 0;
	}
	return 1;
}
//-------------------------------------------------------------------
int appendVarInt(int value,WObject dest)
//-------------------------------------------------------------------
{
	static jbyte indexBuffer[5], *toAppend;
	int a = 0, alloced = 0;
	if (value < -1 || value > 0x1fffffff){
		indexBuffer[0] = (jbyte)0xe0;
		indexBuffer[1] = (jbyte)((value >> 24) & 0xff);
		indexBuffer[2] = (jbyte)((value >> 16) & 0xff);
		indexBuffer[3] = (jbyte)((value >> 8) & 0xff);
		indexBuffer[4] = (jbyte)((value) & 0xff);
		a = 5;
	}else if (value == -1 || value <= 0x3f) {
		indexBuffer[0] = (jbyte)value;
		a = 1;
	}else if (value <= 0x3fff){
		indexBuffer[0] = (jbyte)(((value >> 8) & 0x3f)|0x40);
		indexBuffer[1] = (jbyte)((value) & 0xff);
		a = 2;
	}else if (value <= 0x3fffff){
		indexBuffer[0] = (jbyte)(((value >> 16) & 0x3f)|0x80);
		indexBuffer[1] = (jbyte)((value >> 8) & 0xff);
		indexBuffer[2] = (jbyte)((value) & 0xff);
		a = 3;
	}else{
		indexBuffer[0] = (jbyte)(((value >> 24) & 0x1f)|0xc0);
		indexBuffer[1] = (jbyte)((value >> 16) & 0xff);
		indexBuffer[2] = (jbyte)((value >> 8) & 0xff);
		indexBuffer[3] = (jbyte)((value) & 0xff);
		a = 4;
	}
	toAppend = appendingByteArray(-1,a,dest,&alloced);
	memcpy(toAppend,indexBuffer,a);
	return alloced;
}

static Var RecordFoundEntriesWrite(Var stack[])
{
	WObject source = stack[0].obj, dest = stack[1].obj;
	int max = objectPtr(source)[2].intValue;
	int *s;
	int i;
	appendVarInt(max,dest);
	s = (int *)WOBJ_arrayStart(objectPtr(source)[1].obj);
	for (i = 0; i<max; i++){
		if (appendVarInt(s[i],dest))
			s = (int *)WOBJ_arrayStart(objectPtr(source)[1].obj);
	}
	return returnVar(1);
}

static Var RecordFoundEntriesRead(Var stack[])
{
	WObject source = stack[0].obj, dest = stack[1].obj;
	int off = 0, used = 0;
	int max = objectPtr(source)[2].intValue;
	int baseSize, *d;
	Var *l;
	//
	objectPtr(dest)[2].intValue = 0; // Zero int IntArray.
	//
	if (!readVarInt(source,&off,&baseSize)) return returnVar(0);
	if (baseSize > 0){
		int *values = appendingIntArray(-1,baseSize,dest), v, i;
		if (values == NULL) return returnVar(0);
		for (i = 0; i<baseSize; i++){
			if (!readVarInt(source,&off,&v)) return returnVar(0);
			values[i] = v;
		}
	}
	d = (int *)WOBJ_arrayStart(objectPtr(dest)[1].obj);
	l = objectPtr(dest)+2;
	while(off < max){
		int oldIndex, newIndex, newPos;
		if (!readVarInt(source,&off,&oldIndex)) return returnVar(0);
		if (!readVarInt(source,&off,&newIndex)) return returnVar(0);
		if (!readVarInt(source,&off,&newPos)) return returnVar(0);
		if (oldIndex >= 0){
			if (newIndex != oldIndex) {
				//ids.removeAtIndex(oldIndex);
				if (oldIndex < l->intValue){
					l->intValue--;
					if (oldIndex < l->intValue)
						memmove(d+oldIndex,d+oldIndex+1,sizeof(int32)*(l->intValue-oldIndex));
				}
				if (newIndex < 0) continue;
				if (appendingIntArray(newIndex,1,dest) == NULL)
					return returnVar(0);
				d = (int *)WOBJ_arrayStart(objectPtr(dest)[1].obj);
				l = objectPtr(dest)+2;
				//ids.makeSpace(newIndex,1);
			}
			if (newIndex < l->intValue)
				d[newIndex] = newPos;
		}else if (newIndex >= 0 && newIndex <= l->intValue){
			//ids.makeSpace(newIndex,1);
			if (appendingIntArray(newIndex,1,dest) == NULL)
				return returnVar(0);
			d = (int *)WOBJ_arrayStart(objectPtr(dest)[1].obj);
			l = objectPtr(dest)+2;
			//ids.data[newIndex] = newPos;
			d[newIndex] = newPos;
		}
		/*
		if (h != null)
			if (size < 0) h.setProgress(-1);
			//else h.setProgress((float)actuallyRead/size);
		*/
	}
	return returnVar(1);
}

static Var EntriesViewToRanges(Var stack[])
{
	int *source = (int*)WOBJ_arrayStart(stack[0].obj);
	int length = stack[1].intValue;
	WObject dest = stack[2].obj;
	int *save;
	if (length > 0){
		int st = source[0];
		int last = st;
		int i;
		for (i = 1; i<length; i++){
			int now = source[i];
			if (now <= last+1) {
				last = now;
				continue;
			}
			//dest.append(st);
			//dest.append(last-st+1);
			save = appendingIntArray(-1,2,dest);
			if (save == NULL) return returnVar(0);
			*save++ = st;
			*save = last-st+1;
			source = (int*)WOBJ_arrayStart(stack[0].obj);
			//
			st = last = now;
		}
		//dest.append(st);
		//dest.append(last-st+1);
		save = appendingIntArray(-1,2,dest);
		if (save == NULL) return returnVar(0);
		*save++ = st;
		*save = last-st+1;
		source = (int*)WOBJ_arrayStart(stack[0].obj);
	}
	return returnVar(0);
}
static Var EntriesViewAdjustIndexes(Var stack[])
{
	int* data = (int*)WOBJ_arrayStart(stack[0].obj);
	int length = stack[1].intValue;
	int oldIndex = stack[2].intValue;
	int newIndex = stack[3].intValue;
	int old = -1, i = 0;
	if (newIndex == -1){
		for (i = 0; i<length; i++)
			if (data[i] == oldIndex) old = i;
			else if (data[i] > oldIndex) data[i]--;
	}else if (oldIndex == -1){
		for (i = 0; i<length; i++)
			if (data[i] >= newIndex) data[i]++;
	}else if (newIndex < oldIndex){
		for (i = 0; i<length; i++)
			if (data[i] == oldIndex) data[i] = newIndex;
			else if (data[i] >= newIndex && data[i] < oldIndex) data[i]++;
	}else{ //newIndex > oldIndex
		for (i = 0; i<length; i++)
			if (data[i] == oldIndex) data[i] = newIndex;
			else if (data[i] > oldIndex && data[i] <= newIndex) data[i]--;
	}
	return returnVar(old);
}

struct mpn {
	int *words;
	int length;
	int wordsLength;
};
typedef struct mpn *MPN;

#define MIN_MPN_WORDS 4

void mpn_expandTo(MPN m,int length)
{
	if (length < MIN_MPN_WORDS) length = MIN_MPN_WORDS;
	if (m->wordsLength < length){
		int *w = (int *)mMalloc(sizeof(int)*length);
		if (m->words){
			memcpy(w,m->words,m->length);
			free(m->words);
		}
		m->words = w;
		m->wordsLength = length;
	}
}

#define ensureSpace_mpn(MPN,LENGTH) {if((MPN)->wordsLength<(LENGTH))mpn_expandTo((MPN),(LENGTH));}

//-------------------------------------------------------------------
MPN new_mpn(int length)
//-------------------------------------------------------------------
{
	MPN r = (MPN)mMalloc(sizeof(struct mpn));
	r->words = NULL;
	r->length = 0;
	r->wordsLength = 0;
	ensureSpace_mpn(r,length);
	r->length = 1;
	r->words[0] = 0;
	return r;
}
//-------------------------------------------------------------------
MPN delete_mpn(MPN m)
//-------------------------------------------------------------------
{
	if (m == NULL) return NULL;
	if (m->words != NULL) free(m->words);
	free(m);
	return NULL;
}
//-------------------------------------------------------------------
void minimize_mpn(MPN m)
//-------------------------------------------------------------------
{
	while(m->length > 1){
		if (m->words[m->length-1] == 0 && m->words[m->length-2] >= 0) m->length--;
		else if (m->words[m->length-1] == -1 && m->words[m->length-2] < 0) m->length--;
		else break;
	}
}
//-------------------------------------------------------------------
int equals_mpn(MPN one,MPN two)
//-------------------------------------------------------------------
{
	if (one->length != two->length) return 0;
	else{
		int w;
		for (w = 0; w<one->length; w++)
			if (one->words[w] != two->words[w])
				return 0;
		return 1;
	}
}

//-------------------------------------------------------------------
MPN set_mpn(MPN m,int64 value)
//-------------------------------------------------------------------
{
	m->length = 2;
	m->words[0] = (int)value;
	m->words[1] = (int)(value >> 32);
	minimize_mpn(m);
	return m;
}
//===================================================================
MPN setInts_mpn(MPN m,int* words,int length)
//===================================================================
{
	if (m == NULL) m = new_mpn(length+1);
	else ensureSpace_mpn(m,length+1);
	if (length == 0){
		m->length = 1;
		m->words[0] = 0;
	}else{
		memcpy(m->words,words,length*sizeof(int));
		m->length = length;
	}
	return m;
}

MPN clearExcess_mpn(MPN m)
{
	int i = 0;
	for (i = m->length; i<m->wordsLength; i++)
		m->words[i] = 0;
	return m;
}
//===================================================================
MPN setMpn_mpn(MPN m,MPN other)
//===================================================================
{
	return setInts_mpn(m,other->words,other->length);
}
//===================================================================
MPN fromBI_mpn(MPN m,WObject bi)
//===================================================================
{
	int ival = objectPtr(bi)[1].intValue;
	WObject words = objectPtr(bi)[2].obj;
	int length = words == 0 ? 1 : ival;
	//
	if (m == NULL) m = new_mpn(length);
	else ensureSpace_mpn(m,length);
	//
	if (words == 0){
		m->length = 1;
		m->words[0] = ival;
	}else{
		memcpy(m->words,WOBJ_arrayStart(words),length*sizeof(int));
		m->length = length;
	}
	return clearExcess_mpn(m);
}
//===================================================================
MPN fromObj_mpn(MPN m,WObject eweMpn)
//===================================================================
{
	int length = eweMpn == 0 ? 0 : objectPtr(eweMpn)[2].intValue;
	if (m == NULL) m = new_mpn(length);
	else ensureSpace_mpn(m,length);
	if (length == 0){
		m->length = 1;
		m->words[0] = 0;
	}else{
		memcpy(m->words,WOBJ_arrayStart(objectPtr(eweMpn)[1].obj),length*sizeof(int));
		m->length = length;
	}
	return m;
}
//===================================================================
WObject toObj_mpn(MPN m,WObject eweMpn)
//===================================================================
{
	if (eweMpn == 0) return 0;
	if (WOBJ_arrayLen(objectPtr(eweMpn)[1].obj) < m->length){
		WObject obj = createArray("I",m->length+1);
		objectPtr(eweMpn)[1].obj = obj;
	}
	memcpy(WOBJ_arrayStart(objectPtr(eweMpn)[1].obj),m->words,m->length*sizeof(int));
	objectPtr(eweMpn)[2].intValue = m->length;
	return eweMpn;
}

#define isNegative_mpn(MMM) ((MMM)->words[(MMM)->length-1] < 0)
#define isOdd_mpn(MMM) ((MMM)->words[0] & 1)

//===================================================================
int hasLong_mpn(MPN m)
//===================================================================
{
	minimize_mpn(m);
	return (m->length <= 2);
}
//===================================================================
int64 getLong_mpn(MPN m)
//===================================================================
{
	minimize_mpn(m);
	if (m->length > 2) return 0;
	if (m->length == 1) return (int64)m->words[0];
	return ((int64)m->words[1] << 32)|((int64)m->words[0] & cINT64(0xffffffffL));
}
//
// OK for dest == src
//
//===================================================================
static int negate_bi(int* dest, int* src, int len)
//===================================================================
{
	int64 carry = 1;
	int i, negative = src[len-1] < 0;
	for (i = 0;  i < len;  i++){
		carry += ((int64) (~src[i]) & cINT64(0xffffffffL));
		dest[i] = (int) carry;
		carry >>= 32;
	}
	return (negative && dest[len-1] < 0);
}
//-------------------------------------------------------------------
static void getAbsolute_mpn(MPN m,int* words,int wordsLength)
//-------------------------------------------------------------------
{
	int len = m->length, i;
	for (i = len;  --i >= 0;)
		words[i] = m->words[i];
	if (words[len - 1] < 0)
		negate_bi(words, words, len);
	for (i = wordsLength;  --i > len; )
		words[i] = 0;
}
  /** Compare x[0:size-1] with y[0:size-1], treating them as unsigned integers.
   * @result -1, 0, or 1 depending on if x<y, x==y, or x>y.
   * This is basically the same as gmp's mpn_cmp function.
   */
static int cmp_mpn_samelen(int* x, int* y, int size)
{
	while (--size >= 0){
		int x_word = x[size];
		int y_word = y[size];
		if (x_word != y_word){
	// Invert the high-order bit, because:
	// (unsigned) X > (unsigned) Y iff
	// (int) (X^0x80000000) > (int) (Y^0x80000000).
			return (x_word ^ 0x80000000) > (y_word ^0x80000000) ? 1 : -1;
		}
	}
	return 0;
}

  /** Compare x[0:xlen-1] with y[0:ylen-1], treating them as unsigned integers.
   * @result -1, 0, or 1 depending on if x<y, x==y, or x>y.
   */
  static int cmp_mpn(int* x, int xlen, int* y, int ylen)
  {
    return xlen > ylen ? 1 : xlen < ylen ? -1 : cmp_mpn_samelen(x, y, xlen);
  }

//===================================================================
MPN toNegative_mpn(MPN m){
//===================================================================
	if (hasLong_mpn(m)){
		int64 val = getLong_mpn(m);
		if (val == 0) return m;
		if (val != cINT64(0x8000000000000000L)){
			set_mpn(m,-val);
			return m;
		}
	}
	if (m->words[m->length-1] == 0x80000000) ensureSpace_mpn(m,m->length+1);
  	if (negate_bi(m->words,m->words,m->length))
		m->words[m->length++] = 0;
	minimize_mpn(m);
	return m;
}


 /** Multiply x[0:len-1] by y, and write the len least
   * significant words of the product to dest[0:len-1].
   * Return the most significant word of the product.
   * All values are treated as if they were unsigned
   * (i.e. masked with cINT64(0xffffffffL)).
   * OK if dest==x (not sure if this is guaranteed for mpn_mul_1).
   * This function is basically the same as gmp's mpn_mul_1.
   */

  static int MPN_mul_1 (int* dest, int* x, int len, int y)
  {
    int64 yword = (int64) y & cINT64(0xffffffffL);
    int64 carry = 0;
	int j;
    for (j = 0;  j < len; j++)
      {
        carry += ((int64) x[j] & cINT64(0xffffffffL)) * yword;
        dest[j] = (int) carry;
        carry >>= 32;
		carry &= cINT64(0xffffffffL);
      }
    return (int) carry;
  }

  /**
   * Multiply x[0:xlen-1] and y[0:ylen-1], and
   * write the result to dest[0:xlen+ylen-1].
   * The destination has to have space for xlen+ylen words,
   * even if the result might be one limb smaller.
   * This function requires that xlen >= ylen.
   * The destination must be distinct from either input operands.
   * All operands are unsigned.
   * This function is basically the same gmp's mpn_mul. */

  static void MPN_mul (int* dest,
			  int* x, int xlen,
			  int* y, int ylen)
  {
	int i, j;
    dest[xlen] = MPN_mul_1 (dest, x, xlen, y[0]);

    for (i = 1;  i < ylen; i++)
      {
	int64 yword = (int64) y[i] & cINT64(0xffffffffL);
	int64 carry = 0;
	for (j = 0;  j < xlen; j++)
	  {
	    carry += ((int64) x[j] & cINT64(0xffffffffL)) * yword
	      + ((int64) dest[i+j] & cINT64(0xffffffffL));
	    dest[i+j] = (int) carry;
	    carry >>= 32;
		carry &= cINT64(0xffffffffL);
	  }
	dest[i+xlen] = (int) carry;
      }
  }
  static Var MPNmul(Var stack[])
  {
	  int *dest = (int *)WOBJ_arrayStart(stack[0].obj);
	  int *x = (int *)WOBJ_arrayStart(stack[1].obj);
	  int xlen = stack[2].intValue;
	  int *y = (int *)WOBJ_arrayStart(stack[3].obj);
	  int ylen = stack[4].intValue;
	  MPN_mul(dest,x,xlen,y,ylen);
	  return returnVar(0);
  }

//===================================================================
MPN multiply_mpn_d(MPN times1,MPN times2,MPN dest)
//===================================================================
{
	int negative = 0;
	if (dest == NULL) dest = new_mpn(2);
	if (isNegative_mpn(times1)) {
		toNegative_mpn(times1);
		negative = !negative;
	}
	if (isNegative_mpn(times2)) {
		toNegative_mpn(times2);
		negative = !negative;
	}
	if (times1->length < times2->length){
		MPN t = times1;
		times1 = times2;
		times2 = t;
	}
	ensureSpace_mpn(dest,times1->length+times2->length+1);
	MPN_mul(dest->words, times1->words, times1->length, times2->words, times2->length);
	dest->length = times1->length+times2->length;
	if (negative)
		toNegative_mpn(dest);
	minimize_mpn(dest);
	return dest;
}

 /* Divide (unsigned long) N by (unsigned int) D.
   * Returns (remainder << 32)+(unsigned int)(quotient).
   * Assumes (unsigned int)(N>>32) < (unsigned int)D.
   * Code transcribed from gmp-2.0's mpn_udiv_w_sdiv function.
   */
  static int64 udiv_qrnnd (int64 N, int D)
  {
    int64 q, r;
    int64 a1 = N >> 32 & cINT64(0xffffffffL);
    int64 a0 = N & cINT64(0xffffffffL);
    if (D >= 0)
      {
	if (a1 < ((D - a1 - ((a0 >> 31)&cINT64(0x1ffffffffL))) & cINT64(0xffffffffL)))
	  {
	    /* dividend, divisor, and quotient are nonnegative */
	    q = N / D;
	    r = N % D;
	  }
	else
	  {
	    /* Compute c1*2^32 + c0 = a1*2^32 + a0 - 2^31*d */
	    int64 c = N - ((int64) D << 31);
	    /* Divide (c1*2^32 + c0) by d */
	    q = c / D;
	    r = c % D;
	    /* Add 2^31 to quotient */
	    q += 1 << 31;
	  }
      }
    else
      {
	int64 b1 = (int64)((D >> 1) & cINT64(0x7fffffffL));	/* d/2, between 2^30 and 2^31 - 1 */
	//int64 c1 = (a1 >> 1); /* A/2 */
	//int c0 = (a1 << 31) + (a0 >> 1);
	int64 c = (N >> 1)& cINT64(0x7fffffffffffffffL);
	if (a1 < b1 || (a1 >> 1) < b1)
	  {
	    if (a1 < b1)
	      {
		q = c / b1;
		r = c % b1;
	      }
	    else /* c1 < b1, so 2^31 <= (A/2)/b1 < 2^32 */
	      {
		c = ~(c - (b1 << 32));
		q = c / b1;  /* (A/2) / (d/2) */
		r = c % b1;
		q = (~q) & cINT64(0xffffffffL);    /* (A/2)/b1 */
		r = (b1 - 1) - r; /* r < b1 => new r >= 0 */
	      }
	    r = 2 * r + (a0 & 1);
	    if ((D & 1) != 0)
	      {
		if (r >= q) {
		        r = r - q;
		} else if (q - r <= ((int64) D & cINT64(0xffffffffL))) {
                       r = r - q + D;
        		q -= 1;
		} else {
                       r = r - q + D + D;
        		q -= 2;
		}
	      }
	  }
	else				/* Implies c1 = b1 */
	  {				/* Hence a1 = d - 1 = 2*b1 - 1 */
	    if (a0 >= ((int64)(-D) & cINT64(0xffffffffL)))
	      {
		q = -1;
	        r = a0 + D;
 	      }
	    else
	      {
		q = -2;
	        r = a0 + D + D;
	      }
	  }
      }

    return (r << 32) | (q & cINT64(0xFFFFFFFFL));
  }

    /** Divide dividend[0:len-1] by (unsigned int)divisor.
     * Write result into quotient[0:len-1.
     * Return the one-word (unsigned) remainder.
     * OK for quotient==dividend.
     */

  static int divmod_1 (int *quotient, int *dividend,
			      int len, int divisor)
  {
    int i = len - 1;
    int64 r = dividend[i];
    if ((r & cINT64(0xffffffffL)) >= ((int64)divisor & cINT64(0xffffffffL)))
      r = 0;
    else
      {
	quotient[i--] = 0;
	r <<= 32;
      }

    for (;  i >= 0;  i--)
      {
	int n0 = dividend[i];
	int64 t = ((int64)n0 & cINT64(0xffffffffL));
	//
	// NOTE there appears to be a bug in the compiler.
	// the line r = (r & ~cINT64(0xffffffffL)) always resulted in r being zero.
	//
	r = (r & cINT64(0xffffffff00000000L));
	r |= t;
	r = udiv_qrnnd (r, divisor);
	quotient[i] = (int) r;
      }
    return (int)(r >> 32);
  }

  /* Subtract x[0:len-1]*y from dest[offset:offset+len-1].
   * All values are treated as if unsigned.
   * @return the most significant word of
   * the product, minus borrow-out from the subtraction.
   */
  static int submul_1 (int *dest, int offset, int *x, int len, int y)
  {
    int64 yl = (int64) y & cINT64(0xffffffffL);
    int carry = 0;
    int j = 0;
    do
      {
	int64 prod = ((int64) x[j] & cINT64(0xffffffffL)) * yl;
	int prod_low = (int) prod;
	int prod_high = (int) (prod >> 32);
	int x_j;
	int pl, cc;
	prod_low += carry;
	// Invert the high-order bit, because: (unsigned) X > (unsigned) Y
	// iff: (int) (X^0x80000000) > (int) (Y^0x80000000).
	pl = (prod_low ^ 0x80000000);
	cc = (carry ^ 0x80000000);
	carry = (pl < cc  ? 1 : 0)
	  + prod_high;
	x_j = dest[offset+j];
	prod_low = x_j - prod_low;
	pl = (prod_low ^ 0x80000000);
	cc = (x_j ^ 0x80000000);
	if (pl > cc)
	  carry++;
	dest[offset+j] = prod_low;
      }
    while (++j < len);
    return carry;
  }

  /** Divide zds[0:nx] by y[0:ny-1].
   * The remainder ends up in zds[0:ny-1].
   * The quotient ends up in zds[ny:nx].
   * Assumes:  nx>ny.
   * (int)y[ny-1] < 0  (i.e. most significant bit set)
   */

  static void divide (int *zds, int nx, int *y, int ny)
  {
    // This is basically Knuth's formulation of the classical algorithm,
    // but translated from in scm_divbigbig in Jaffar's SCM implementation.

    // Correspondance with Knuth's notation:
    // Knuth's u[0:m+n] == zds[nx:0].
    // Knuth's v[1:n] == y[ny-1:0]
    // Knuth's n == ny.
    // Knuth's m == nx-ny.
    // Our nx == Knuth's m+n.

    // Could be re-implemented using gmp's mpn_divrem:
    // zds[nx] = mpn_divrem (&zds[ny], 0, zds, nx, y, ny).

    int j = nx;
    do
      {                          // loop over digits of quotient
	// Knuth's j == our nx-j.
	// Knuth's u[j:j+n] == our zds[j:j-ny].
	int qhat;  // treated as unsigned
	if (zds[j]==y[ny-1])
	  qhat = -1;  // 0xffffffff
	else
	  {
	    int64 w = (((int64)(zds[j])) << 32) + ((int64)zds[j-1] & cINT64(0xffffffffL));
	    qhat = (int) udiv_qrnnd (w, y[ny-1]);
	  }
	if (qhat != 0)
	  {
	    int borrow = submul_1 (zds, j - ny, y, ny, qhat);
	    int save = zds[j];
	    int64 num = ((int64)save&cINT64(0xffffffffL)) - ((int64)borrow&cINT64(0xffffffffL));
            while (num != 0)
	      {
		int64 carry = 0;
		int i;
		qhat--;
		for (i = 0;  i < ny; i++)
		  {
		    carry += ((int64) zds[j-ny+i] & cINT64(0xffffffffL))
		      + ((int64) y[i] & cINT64(0xffffffffL));
		    zds[j-ny+i] = (int) carry;
		    carry >>= 32;
			carry  &= cINT64(0xffffffffL);
		  }
		zds[j] += (int)carry;
		num = carry - 1;
	      }
	  }
	zds[j] = qhat;
      } while (--j >= ny);
  }
//-------------------------------------------------------------------
  static void divide_long_mpn(int64 x, int64 y, MPN quotient, MPN remainder)
//-------------------------------------------------------------------
 {
    int xNegative = 0, yNegative = 0,qNegative = 0;
	int64 q, r;
    if (x < 0){
			xNegative = 1;
			x = -x;
    }
    if (y < 0){
			yNegative = 1;
			y = -y;
    }
    q = x / y;
    r = x % y;
    qNegative = xNegative ^ yNegative;
    if (quotient != NULL){
		if (qNegative) q = -q;
		set_mpn(quotient,q);
	}
	if (remainder != NULL){
		if (xNegative) r = -r;
		set_mpn(remainder,r);
	}
  }


//-------------------------------------------------------------------
int usri(int value,int count)
//-------------------------------------------------------------------
{
	if (count <= 0) return value;
	value >>= 1;
	value &= 0x7fffffff;
	value >>= count-1;
	return value;
}
//-------------------------------------------------------------------
  static int count_leading_zeros (int i)
//-------------------------------------------------------------------
  {
    int count = 0, k = 0;
    if (i == 0) return 32;
    for (k = 16;  k > 0;  k = k >> 1) {
      int j = usri(i,k);
      if (j == 0) count += k;
      else i = j;
    }
    return count;
  }
//-------------------------------------------------------------------
  static int lshift (int* dest, int d_offset,
			    int* x, int len, int count)
//-------------------------------------------------------------------
  {
    int count_2 = 32 - count;
    int i = len - 1;
    int high_word = x[i];
    int retval = usri(high_word , count_2);
    d_offset++;
    while (--i >= 0)
      {
	int low_word = x[i];
	dest[d_offset+i] = (high_word << count) | (usri(low_word , count_2));
	high_word = low_word;
      }
    dest[d_offset+i] = high_word << count;
    return retval;
  }
  /* Shift x[x_start:x_start+len-1] count bits to the "right"
   * (i.e. divide by 2**count).
   * Store the len least significant words of the result at dest.
   * The bits shifted out to the right are returned.
   * OK if dest==x.
   * Assumes: 0 < count < 32
   */

//-------------------------------------------------------------------
  int rshift (int* dest, int* x, int x_start,
			    int len, int count)
//-------------------------------------------------------------------
  {
    int count_2 = 32 - count;
    int low_word = x[x_start];
    int retval = low_word << count_2;
    int i = 1;
    for (; i < len;  i++)
      {
	int high_word = x[x_start+i];
	dest[i-1] = (usri(low_word , count)) | (high_word << count_2);
	low_word = high_word;
      }
    dest[i-1] = usri(low_word, count);
    return retval;
  }

  /* Shift x[x_start:x_start+len-1] count bits to the "right"
   * (i.e. divide by 2**count).
   * Store the len least significant words of the result at dest.
   * OK if dest==x.
   * Assumes: 0 <= count < 32
   * Same as rshift, but handles count==0 (and has no return value).
   */
//-------------------------------------------------------------------
  void rshift0 (int* dest, int* x, int x_start,
			      int len, int count)
//-------------------------------------------------------------------
  {
	int i;
    if (count > 0)
      rshift(dest, x, x_start, len, count);
    else
      for (i = 0;  i < len;  i++)
	dest[i] = x[i + x_start];
  }
//===================================================================
MPN shiftRight_mpn(MPN m,int count)
//===================================================================
{
	int neg = isNegative_mpn(m);
	int word_count = count >> 5;
	int d_len = m->length - word_count;
	if (d_len <= 0) set_mpn(m,neg ? -1 : 0);
	else{
		count &= 31;
		rshift0(m->words, m->words, word_count, d_len, count);
		m->length = d_len;
		if (neg) m->words[d_len-1] |= -2 << (31 - count);
	}
	return m;
}
//===================================================================
MPN multiply_mpn(MPN m,MPN other)
//===================================================================
{
	static MPN t1 = NULL, t2 = NULL;
	if (t1 == NULL){
		t1 = new_mpn(m->length+1);
		t2 = new_mpn(other->length+1);
	}
	setMpn_mpn(t1,m);
	setMpn_mpn(t2,other);
	multiply_mpn_d(t1,t2,m);
	return m;
}

static int *xwords = NULL, *ywords = NULL;
static int xwordsLength = 0, ywordsLength = 0;

  /** Divide two integers, yielding quotient and remainder.
   * @param y the denominator in the division
   * @param quotient is set to the quotient of the result (iff quotient!=null)
   * @param remainder is set to the remainder of the result
   *  (iff remainder!=null)
   */
//===================================================================
  void divide_mpn(MPN numerator,MPN denominator,MPN quotient,MPN remainder)
//===================================================================
{

	MPN y = denominator;
	MPN x = numerator;
	minimize_mpn(y);
	minimize_mpn(x);

	if (y->length == 1 && y->words[0] == 1) return;
    if (hasLong_mpn(x) && hasLong_mpn(y)){
			int64 x_l = getLong_mpn(x);
			int64 y_l = getLong_mpn(y);
			if (x_l != cINT64(0x8000000000000000L) && y_l != cINT64(0x8000000000000000L)){
	   		divide_long_mpn(x_l, y_l, quotient, remainder);
	    	return;
	  	}
    }else{

    int xNegative = isNegative_mpn(x);
    int yNegative = isNegative_mpn(y);
    int qNegative = xNegative ^ yNegative;
	int ylen = y->length, xlen = x->length, qlen, rlen;
	int cmpval;

	if (ywords == NULL || ywordsLength < ylen){
		if (ywords != NULL) free(ywords);
		ywords = (int *)mMalloc(sizeof(int)*ylen);
		ywordsLength = ylen;
	}
	memset(ywords,0,ywordsLength*sizeof(int));
    getAbsolute_mpn(y,ywords,ywordsLength);
    while (ylen > 1 && ywords[ylen - 1] == 0)  ylen--;

	if (xwords == NULL || xwordsLength < xlen+2){
		if (xwords != NULL) free(xwords);
		xwords = (int *)mMalloc(sizeof(int)*(xlen+2));
		xwordsLength = xlen+2;
	}
	memset(xwords,0,xwordsLength*sizeof(int));
    getAbsolute_mpn(x,xwords,xwordsLength);
    while (xlen > 1 && xwords[xlen - 1] == 0)  xlen--;
    cmpval = cmp_mpn(xwords, xlen, ywords, ylen);
    if (cmpval < 0)  // abs(x) < abs(y)
      { // quotient = 0;  remainder = num.
		int tx;
		int* rwords = xwords;  xwords = ywords;  ywords = rwords;
		tx = xwordsLength; xwordsLength = ywordsLength; ywordsLength = tx;
		rlen = xlen;  qlen = 1;  xwords[0] = 0;
      }
    else if (cmpval == 0)  // abs(x) == abs(y)
      {
				xwords[0] = 1;  qlen = 1;  // quotient = 1
				ywords[0] = 0;  rlen = 1;  // remainder = 0;
      }
    else if (ylen == 1)
      {
	qlen = xlen;
	// Need to leave room for a word of leading zeros if dividing by 1
	// and the dividend has the high bit set.  It might be safe to
	// increment qlen in all cases, but it certainly is only necessary
	// in the following case.
	if (ywords[0] == 1 && xwords[xlen-1] < 0)
	  qlen++;
	rlen = 1;
	ywords[0] = divmod_1(xwords, xwords, xlen, ywords[0]);
      }
    else  // abs(x) > abs(y)
      {
	// Normalize the denominator, i.e. make its most significant bit set by
	// shifting it normalization_steps bits to the left.  Also shift the
	// numerator the same number of steps (to keep the quotient the same!).

	int nshift = count_leading_zeros(ywords[ylen - 1]);
	if (nshift != 0){
	    // Shift up the numerator, possibly introducing a new most
	    // significant word.
	    int x_high = lshift(xwords, 0, xwords, xlen, nshift);
	    xwords[xlen++] = x_high;
	    // Shift up the denominator setting the most significant bit of
	    // the most significant word.
	    lshift(ywords, 0, ywords, ylen, nshift);
	}

	if (xlen == ylen)
	  xwords[xlen++] = 0;


	divide(xwords, xlen, ywords, ylen);
	rlen = ylen;
	rshift0 (ywords, xwords, 0, rlen, nshift);

	qlen = xlen + 1 - ylen;
	if (quotient != NULL){
		int i;
		for (i = 0;  i < qlen;  i++)
			xwords[i] = xwords[i+ylen];
	}
}
if (quotient != NULL){
	setInts_mpn(quotient,xwords,qlen);
	if (qNegative) toNegative_mpn(quotient);
}
if (ywords[rlen-1] < 0){
  ywords[rlen] = 0;
  rlen++;
}
if (remainder != NULL){
	setInts_mpn(remainder,ywords, rlen);
	if (xNegative) toNegative_mpn(remainder);
}
	}
}

#define MinFixNum  -100
#define MaxFixNum 1024

static int BI_primes[] =
    {   2,   3,   5,   7,  11,  13,  17,  19,  23,  29,  31,  37,  41,  43,
       47,  53,  59,  61,  67,  71,  73,  79,  83,  89,  97, 101, 103, 107,
      109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181,
      191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251 };
/** HAC (Handbook of Applied Cryptography), Alfred Menezes & al. Table 4.4. */
static int BI_k[] =
      {100,150,200,250,300,350,400,500,600,800,1250, 0x7fffffff};
#define BI_k_length 12

static int BI_t[] =
      { 27, 18, 15, 12,  9,  8,  7,  6,  5,  4,   3, 2};

static char *modPow_mpn_d(MPN val, MPN exp, MPN md, MPN result);

static int ppp = 0;

static Var BICheckPrimes(Var stack[])
{
	WObject me = stack[0].obj;
	WObject sfx = stack[1].obj;
	static MPN ths = NULL, prime = NULL;
	int i, max =  sizeof(BI_primes)/sizeof(int);

	if (ths == NULL){
		ths = new_mpn(10);
		prime = new_mpn(10);
	}

	fromBI_mpn(ths,me);

	for (i = 0; i < max; i++){
		if (ths->length == 1 && ths->words[0] == BI_primes[i])
			return returnVar(1);
		fromBI_mpn(prime,((WObject *)WOBJ_arrayStart(sfx))[BI_primes[i] - MinFixNum]);
		divide_mpn(ths,prime,NULL,prime);
		minimize_mpn(prime);
		if (prime->length == 1 && prime->words[0] == 0)
			return returnVar(-1);
	}
	return returnVar(0);
}
static Var BIProbablePrime(Var stack[])
{
	WObject me = stack[0].obj;
	WObject meMinus1 = stack[1].obj;
	WObject em = stack[2].obj;
	int b = stack[3].intValue;
	int bits = stack[4].intValue;
	WObject smallFixes = stack[5].obj;
	int certainty = stack[6].intValue;

	static MPN ths = NULL, pMinus1 = NULL, m = NULL, z = NULL, v2 = NULL;

	int i, trials, t;

	if (ths == NULL){

		ths = new_mpn(10);
		pMinus1 = new_mpn(10);
		m = new_mpn(10);
		z = new_mpn(10);
		v2 = new_mpn(10);
	}

	fromBI_mpn(ths,me);
	fromBI_mpn(pMinus1,meMinus1);
//
    for (i = 0; i < BI_k_length; i++)
      if (bits <= BI_k[i]) break;
    trials = BI_t[i];
//
    if (certainty > 80) trials *= 2;
//
	ppp = 0;
    for (t = 0; t < trials; t++){
        // The HAC (Handbook of Applied Cryptography), Alfred Menezes & al.
        // Remark 4.28 states: "...A strategy that is sometimes employed
        // is to fix the bases a to be the first few primes instead of
        // choosing them at random.
		//
	//z = smallFixNums[primes[t] - minFixNum].modPow(m, this);
		//
		WObject *sfx = (WObject *)WOBJ_arrayStart(smallFixes);
		fromBI_mpn(z,sfx[BI_primes[t] - MinFixNum]);
		fromBI_mpn(m,em);
		/*
		sprintf(sprintBuffer,"N->Before: %x%x, %x%x, %x%x",z->words[1], z->words[0],
			m->words[1], m->words[0], ths->words[1], ths->words[0]);
		if (ppp < 2) debugString(sprintBuffer);
		*/
		modPow_mpn_d(z,m,ths,z);
		minimize_mpn(z);
		/*
		sprintf(sprintBuffer,"NP: %x%x, %x%x",z->words[1], z->words[0],
			ths->words[1], ths->words[0]);
		if (ppp < 2) debugString(sprintBuffer);
		ppp++;
		*/
		//
	//if (z.isOne() || z.equals(pMinus1)) continue;			// Passes the test; may be prime.
		//
		if (z->length == 1 && z->words[0] == 1) continue; //Passes the test, may be prime.
		if (equals_mpn(z,pMinus1)) continue;
		//
		for (i = 0; i < b; ){
			if (z->length == 1 && z->words[0] == 1) return returnVar(0);
			i++;
			if (equals_mpn(z,pMinus1))
				break;
			set_mpn(v2,2);
			modPow_mpn_d(z,v2,ths,z);
			/*
			sprintf(sprintBuffer,"NT: %x%x, %x%x",z->words[1], z->words[0],
				pMinus1->words[1], pMinus1->words[0]);
			debugString(sprintBuffer);
			*/

	    //z = z.modPow(valueOf(2), this);
		}
		if (i == b && !equals_mpn(z,pMinus1)) return returnVar(0);
      }
    return returnVar(1);
 }


static Var MPNdivide(Var stack[])
{
	int *zds = (int *)WOBJ_arrayStart(stack[0].obj);
	int nx = stack[1].intValue;
	int *y = (int *)WOBJ_arrayStart(stack[2].obj);
	int ny = stack[3].intValue;
	divide(zds,nx,y,ny);
	return returnVar(0);
}
//
// val and exp are destroyed.
//
static char *modPow_mpn_d(MPN val, MPN exp, MPN md, MPN result)
{
	int lop = 0;
	minimize_mpn(val);
	minimize_mpn(exp);
	minimize_mpn(md);

	if ((md->words[md->length-1] < 0) ||
		((md->length == 1) && (md->words[0] == 0)))
		return ("non-positive modulo");
	if (exp->words[exp->length-1] < 0)
		return ("negative exponent");
	//
	// Just do a single modulo.
	//
	if (exp->length == 1 && exp->words[exp->length] == 1){
		divide_mpn(val,md,NULL,result);
		return NULL;
	}else{
		//
		// Do the full process.
		//
		static MPN s, t, u, t1, t2;
		if (s == NULL){
			s = new_mpn(val->length+1);
			t1 = new_mpn(val->length+1);
			t2 = new_mpn(val->length+1);
		}
		set_mpn(s,1);
		t = val;
		u = exp;
		while(1){
			minimize_mpn(u);
			if (u->length == 1 && u->words[0] == 0) break;
			if (u->words[0] & 1){// isOdd
				//s.multiply(t).mod(md);
				setMpn_mpn(t1,s);
				setMpn_mpn(t2,t);
				multiply_mpn_d(t1,t2,s);
				/*
				clearExcess_mpn(s);
				sprintf(sprintBuffer,"Times: %x%x%x%x",
					s->words[3],s->words[2],s->words[1],s->words[0]);
				if (lop >= 30 && lop < 33) debugString(sprintBuffer);
				*/
				divide_mpn(s,md,NULL,s);
				/*
				clearExcess_mpn(s);
				sprintf(sprintBuffer,"Modulo: %x%x%x%x",
					s->words[3],s->words[2],s->words[1],s->words[0]);
				if (lop >= 30 && lop < 33) debugString(sprintBuffer);
				*/
			}
			u = shiftRight_mpn(u,1);
			//t.multipy(t).mod(md);
			setMpn_mpn(t1,t);
			setMpn_mpn(t2,t);
			multiply_mpn_d(t1,t2,t);
			divide_mpn(t,md,NULL,t);

			/*
			clearExcess_mpn(s);
			clearExcess_mpn(t);
			clearExcess_mpn(u);
			sprintf(sprintBuffer,"N: %x%x%x%x, %x%x%x%x, %x%x%x%x,",
				s->words[3],s->words[2],s->words[1],s->words[0],
				t->words[3],t->words[2],t->words[1],t->words[0],
				u->words[3],u->words[2],u->words[1],u->words[0]);
			if (lop >= 30 && lop < 40) debugString(sprintBuffer);
			*/
			lop++;
		}
		setMpn_mpn(result,s);
		return NULL;
	}
}

static Var MPNModPow(Var stack[])
{
	static MPN value = NULL, exp, mod;
	char *err;
	if (value == NULL){
		value = new_mpn(10);
		exp = new_mpn(10);
		mod = new_mpn(10);
	}
	fromObj_mpn(value,stack[0].obj);
	fromObj_mpn(exp,stack[1].obj);
	fromObj_mpn(mod,stack[2].obj);
	err = modPow_mpn_d(value,exp,mod,value);
	if (err != NULL)
		return returnException("java/lang/ArithmeticException",err);
	toObj_mpn(value,stack[0].obj);
	return returnVar(0);
}

static void bytesToInts(int *words, signed char *bytes, int numBytes)
{
	int i, w;
	for (i = 0, w = -1; i<numBytes; i++){
		if ((i & 3) == 0) words[++w] = 0;
		words[w] <<= 8;
		words[w] |= (bytes[i] & 0xff);
	}
}
static void intsToBytes(int *words, signed char *bytes, int numBytes)
{
	int i, w;
	for (i = 0, w = 0; i<numBytes; w++){
		int v = words[w];
		if (numBytes-i < 4)
			v <<= 8*(4-(numBytes-i));
		bytes[i++] = (byte)((v >> 24)&0xff);
		if (i < numBytes)
			bytes[i++] = (byte)((v >> 16)&0xff);
		if (i < numBytes)
			bytes[i++] = (byte)((v >> 8)&0xff);
		if (i < numBytes)
			bytes[i++] = (byte)((v)&0xff);
	}
}
/*
static Var RSAKeyEncryptDecrypt(Var stack[])
{
	WObject bytes = stack[1].obj;
	int offset = stack[2].intValue;
	int length = stack[3].intValue;
	int isEncrypt = stack[4].intValue;
	Var v;

	static MPN value = NULL, exp, mod;
	char *err;
	if (value == NULL){
		value = new_mpn(10);
		exp = new_mpn(10);
		mod = new_mpn(10);
	}
	{
		int numInts = (length+3)/4, numBytes;
		WObject ret;
		signed char *data = (signed char *)WOBJ_arrayStart(bytes)+offset;
		//MPN m = new MPN(numInts+1);
		ensureSpace_mpn(value,numInts+1);
		bytesToInts(value->words,data,length);
		value->length = numInts;
		if (isEncrypt) value->words[value->length++] = length;
		//
		// Process it here.
		//
		fromBI_mpn(exp,objectPtr(stack[0].obj)[1].obj);
		fromBI_mpn(mod,objectPtr(stack[0].obj)[2].obj);
		err = modPow_mpn_d(value,exp,mod,value);
		if (err != NULL)
			return returnException("java/lang/ArithmeticException",err);
		minimize_mpn(value);
		//
		// Now get result.
		//
		numBytes = isEncrypt ? value->length*4 : value->words[value->length-1];
		ret = createArray("B",numBytes);
		intsToBytes(value->words,(signed char *)WOBJ_arrayStart(ret),numBytes);
		v.obj = ret;
		return v;
	}

}
*/
static Var MPNTest(Var stack[])
{
	MPN one = fromObj_mpn(NULL,stack[0].obj);
	MPN two = fromObj_mpn(NULL,stack[1].obj);
	MPN three = fromObj_mpn(NULL,stack[2].obj);
	MPN four = fromObj_mpn(NULL,stack[3].obj);
	int which = stack[4].intValue;
	int ret = 0;
	int i = 0;
	for (i = 0; i<1/*0000*/; i++){
		if (which == 0){
			multiply_mpn_d(one,two,three);
			toObj_mpn(three,stack[2].obj);
		}else if (which == 1){
			divide_mpn(one,two,three,four);
			toObj_mpn(three,stack[2].obj);
		}else if (which == 2){
			char * err = modPow_mpn_d(one,two,three,four);
			if (err != NULL)
				return returnException("java/lang/ArithmeticException",err);
		}
	}
	//toObj_mpn(one,stack[0].obj);
	//toObj_mpn(two,stack[1].obj);
	toObj_mpn(four,stack[3].obj);

	delete_mpn(one);
	delete_mpn(two);
	delete_mpn(three);
	delete_mpn(four);

	return returnVar(ret);
}

static Var SystemGetSetSecurityManager(Var stack[])
{
	Var v;
	v.obj = stack[0].obj;
	if (v.obj == 0) v.obj = securityManager;
	else {
		securityManager = v.obj;
		holdObject(securityManager);
	}
	return v;

}

static int callSecurityMethod(char *name,char *sig,Var *parameters,int numParameters)
{
	if (securityManager == 0) return 0;
	else{
		WClass *vclass;
		Var ret;
		WClassMethod *m = getMethod(WOBJ_class(securityManager),createUtfString(name),createUtfString(sig),&vclass);
		if (m == NULL) return 0;
		else {
			parameters[0].obj = securityManager;
			executeMethodRet(vclass,m,parameters,numParameters+1,&ret);
		}
		return ret.intValue;
	}
}
static int callSecurityMethod1(char *name,char *sig,WObject parameter)
{
	Var v[2];
	int ret;
	v[1].obj = parameter;
	pushObject(parameter);
	ret = callSecurityMethod(name,sig,v,1);
	popObject();
	return ret;
}
static int callSecurityMethod0(char *name,char *sig)
{
	Var v[1];
	return callSecurityMethod(name,sig,v,0);
}

/*==========================================================

  Security Methods.

==========================================================*/
static WClass *methodClass = NULL;
static WClass *constructorClass = NULL;
static WClass *securityClass = NULL;
static WClass *untrustedClassLoader = NULL;

WObject securityParameters;

#define SecurityManagerParameter 0

#define SecurityPar(WHICH) ((WObject *)WOBJ_arrayStart(securityParameters))[WHICH]

unsigned int executeSecurityManagerMethod(char *name,char *signature,Var *parameters,int numParameters)
{
	return 0;
}

//Find the first Class on the stack that is not myClass.
static WClass *getFirstThatIsNot(WClass *myClass)
{
	int stackPtr = vmStackPtr;
	while (stackPtr > 0){
		WClass *wclass = (WClass *)vmStack[--stackPtr].refValue;
		WClassMethod *method = (WClassMethod *)vmStack[--stackPtr].refValue;
		int native = (METH_accessFlags(method) & ACCESS_NATIVE) > 0;
		if (native) stackPtr -= vmStack[--stackPtr].intValue;
		else stackPtr -= METH_maxLocals(method)+METH_maxStack(method);
		stackPtr -= STACKSAVESIZE;
		if (wclass != myClass) return wclass;
	}
	return NULL;
}
static Var securityException(char *message)
{
	return returnException(SecurityEx,message);
}
static int mustBeCalledBySystem()
{
	WClass *calledBy = getFirstThatIsNot(securityClass);
	if (calledBy != NULL && calledBy->isSystemClass && calledBy != methodClass) return 1;
	throwException(SecurityEx,"Illegal Security method call.");
	return 0;
}
static Var SecuritySetup(Var stack[])
{
	if (securityParameters != 0) return securityException("setup() called illegally");
	securityClass = getClass(createUtfString("ewe/security/Security"));
	methodClass = getClass(createUtfString("ewe/reflect/Method"));
	securityParameters = createArray("Ljava/lang/Object;",5);
	holdObject(securityParameters);
	return returnVar(1);
}
static Var SecurityGetSetManager(Var stack[])
{
	Var v;
	WObject sm = stack[0].obj;
	int isGet = stack[1].intValue;
	if (!mustBeCalledBySystem()) return returnVar(0);
	v.obj = SecurityPar(SecurityManagerParameter);
	if (isGet) return v;
	if (v.obj != 0) return securityException("SecurityManager already set.");
	else SecurityPar(SecurityManagerParameter) = sm;
	return v;
}
static Var SecurityWasLoadedBy(Var stack[])
{
	WObject cl = stack[0].obj;
	WClass *clc = cl == 0 ? NULL : (WClass *)objectPtr(cl)[1].classRef;
	int stackPtr = vmStackPtr;
	if (!mustBeCalledBySystem()) return returnVar(0);
	if (clc == NULL){
		if (untrustedClassLoader == NULL)
			untrustedClassLoader = getClass(createUtfString("ewe/security/UntrustedClassLoader"));
		clc = untrustedClassLoader;
	}
	while (stackPtr > 0){
		WClass *wclass = (WClass *)vmStack[--stackPtr].refValue;
		WObject loader = WCLASS_loader(wclass);
		WClass *loaderClass = loader == 0 ? NULL : WOBJ_class(loader);
		WClassMethod *method = (WClassMethod *)vmStack[--stackPtr].refValue;
		int native = (METH_accessFlags(method) & ACCESS_NATIVE) > 0;
		if (native) stackPtr -= vmStack[--stackPtr].intValue;
		else stackPtr -= METH_maxLocals(method)+METH_maxStack(method);
		stackPtr -= STACKSAVESIZE;
		if (loaderClass == NULL) continue;
		if (compatible(loaderClass,clc)) return returnVar(1);
	}

	return returnVar(0);
}
static WObject getClassContext(WObject dest)
{
	int stackPtr = vmStackPtr;
	int need = 0;
	int first = 1;
	while (stackPtr > 0){
		WClass *wclass = (WClass *)vmStack[--stackPtr].refValue;
		WClassMethod *method = (WClassMethod *)vmStack[--stackPtr].refValue;
		int native = (METH_accessFlags(method) & ACCESS_NATIVE) > 0;
		if (native) stackPtr -= vmStack[--stackPtr].intValue;
		else stackPtr -= METH_maxLocals(method)+METH_maxStack(method);
		stackPtr -= STACKSAVESIZE;
		if (first){
			first = 0;
			continue;
		}
		if (dest != 0)
			((WObject *)WOBJ_arrayStart(dest))[need] = returnClass(wclass).obj;
		need++;
	}
	if (dest == 0){
		dest = createArray("Ljava/lang/Class;",need);
		pushObject(dest);
		getClassContext(dest);
		popObject();
	}
	return dest;
}
static Var SecurityManagerGetClassContext(Var stack[])
{
	Var v;
	if (!mustBeCalledBySystem()) return returnVar(0);
	v.obj = getClassContext(0);
	return v;
}
#define VM_FILE_SEPARATOR 6
#define VM_PATH_SEPARATOR 7

static Var VmGetParameter(Var stack[])
{
	Var v;
	v.intValue = 0;

	switch(stack[0].intValue){
	case VM_FLAGS:
		v.intValue = VmFlags;
		break;
	case VM_FILE_SEPARATOR:
		v.intValue = '/'; break;
	case VM_PATH_SEPARATOR:
#ifdef UNIX
		v.intValue = ':';
#else
		v.intValue = ';';
#endif
		break;
	case 2:
		v.intValue = SimulateSip;
		break;
	case 10:
		v.intValue = (int32)nmStackPtr;
		break;
	}
	return v;
}


#define LLENext(LLE) objectPtr(LLE)[1].obj
#define LLEPrev(LLE) objectPtr(LLE)[2].obj

static Var LLEGetNext(Var stack[])
{
	Var v;
	WObject start = stack[0].obj;
	int elements = stack[1].intValue;

	v.obj = 0;
	if (start != 0){
		int i;
		for (i = 0; i<elements || elements<0; i++,start = LLENext(start))
			if (LLENext(start) == 0){
				if (elements < 0) v.obj = start;
				return v;
			}
		v.obj = start;
	}
	return v;
}
static Var LLEGetPrev(Var stack[])
{
	Var v;
	WObject start = stack[0].obj;
	int elements = stack[1].intValue;

	v.obj = start;
	if (start != 0){
		int i;
		for (i = 0; i<elements || elements<0; i++,start = LLEPrev(start))
			if (LLEPrev(start) == 0){
				if (elements < 0) v.obj = start;
				return v;
			}
		v.obj = start;

	}
	return v;
}
/*

{
	boolean back = false;
	if (start == null)
		if (end == null) return 0;
		else {
			start = end;
			back = true;
		}
	for (int i = 0;;){
		if (start == null) return i;
		i++;



		if (start == end) return i;
		else start = back ? start.prev : start.next;
	}
}
*/
static Var LLECountRange(Var stack[])
{
	Var v;
	WObject start = stack[0].obj;
	WObject end = stack[1].obj;
	int back = 0;
	v.intValue = 0;

	if (start == 0)
		if (end == 0) return v;
		else{
			start = end;
			back = 1;
		}


	while(1){
		if (start == 0) return v;
		v.intValue++;
		if (start == end) return v;
		else start = back ? LLEPrev(start) : LLENext(start);
	}

}

static Var VmToInt(Var stack[])
{
	return stack[0];
}


static Var VmGetStackTrace(Var stack[])
{
	Var v;
	WObject t = stack[0].obj;
	v.obj = 0;
	if (t != 0) v.obj = objectPtr(t)[2].obj;
	return v;
}
static Var ThrowableFillInStackTrace(Var stack[]);

#define INTEGER  2
#define SKIP_INTEGER  3
#define FLOAT  4
#define SKIP_FLOAT  5
#define CHAR  6
#define SKIP_CHAR  7
#define TXT  8
#define SKIP_TEXT  9
#define QUOTED  10
#define SKIP_QUOTED  11

#define isWhitespace(ch) (((ch) >= 0x9 && (ch) <= 0xd) || ((ch) >= 0x1c && (ch) <= 0x1f) || (ch) == ' ')

static Var DataParserParse(Var stack[])
{
	Var v;
	WObject *values = (WObject *)WOBJ_arrayStart(stack[1].obj);
	int *formats = (int *)WOBJ_arrayStart(stack[2].obj);
	int numFormats = WOBJ_arrayLen(stack[2].obj);
	uint16 *chars = (uint16 *)WOBJ_arrayStart(stack[3].obj);
	int start = stack[4].intValue;
	int length = stack[5].intValue;
	int pos = start, p = 0;
	int end = start+length;
	int i;
	v.obj = 0;
	for (i = 0; i<numFormats; i++){
		int type = formats[i] & 0xff;

		int len = (formats[i] >> 8) & 0xffffff;
		int w = 0;
		start = pos;
		if (pos >= end) returnException("java/lang/IndexOutOfBoundsException","Cannot read element.");
		if (len == 0){
			for(;pos < end && isWhitespace(chars[pos]);pos++)
				;
			start = pos;
			if (pos >= end){
				i--;
				continue;
			}
			if (type == QUOTED || type == SKIP_QUOTED){
				uint16 q = 0;
				if (chars[pos] == '\'' || chars[pos] == '"'){
					w++;
					q = chars[pos];
					for(pos++;pos < end && chars[pos] != q;pos++)
						w++;
					if (pos < end){
						w++;
						pos++;
					}
				}else{
					for(;pos < end && !isWhitespace(chars[pos]);pos++)
						w++;
				}
			}else
				for(;pos < end && !isWhitespace(chars[pos]);pos++)
					w++;
		}else{
			pos += (w = len);
			if (pos > end) w = 0;
		}
		if (w == 0) {
			i--; //<= This will cause an exception.
		}else{ // Parse the line.
			WObject val;
			if ((type & 1) != 0) continue; //Skip over this section.
			val = values[p++];
			switch(type){
				case INTEGER:
					setLong(val,charsToLong(stack[3].obj,start,w));
					break;
				case FLOAT:
					setDouble(val,charsToDouble(stack[3].obj,start,w));
					break;
				default:{
					Var *sv = objectPtr(val);
					(sv+1)->obj = stack[3].obj;
					(sv+2)->intValue = start;
					(sv+3)->intValue = w;
					break;
						}
			}
		}
	}
	return v;
}

static void asyncCallBack(WObject destination,WObject data)
{

	Var stack[2];
	stack[0].obj = destination;
	stack[1].obj = data;
	VmCallInSystemQueue(stack);
}

extern NativeMethod *nativeMethods;
extern int sizeofNativeMethods;

static NativeFunc getNativeMethod(WClass *wclass, UtfString methodName, UtfString methodDesc)
	{
	UtfString className;
	NativeMethod *nm;
	uint32 hash, classHash, methodHash;
	uint16 top, bot, mid;


	className = getUtfString(wclass, wclass->classNameIndex);
	classHash = genHashCode(className) % 65536;
	methodHash = (genHashCode(methodName) + genHashCode(methodDesc)) % 65536;
	hash = (classHash << 16) + methodHash;

	// binary search to find matching hash value
	top = 0;
	bot = sizeofNativeMethods / sizeof(NativeMethod);
	if (bot == 0)
		return NULL;
	while (1)
		{
		mid = (bot + top) / 2;
		nm = nativeMethods+mid;
		if (hash == nm->hash)
			return nm->func;
		if (mid == top)
			break; // not found
		if (hash < nm->hash)
			bot = mid;
		else
			top = mid;
		}

#ifdef SHOW_MISSING_NATIVE_METHODS // Don't show console for missing native methods.
#ifdef WIN32
#ifndef WINCE
	{
	uint16 i;
	static int did = 0;
	//if (did >= 5) return NULL;
	did++;
	AllocConsole();
	cprintf("** Native Method Missing:\n");
	cprintf("// ");
	for (i = 0; i < className.len; i++)
		cprintf("%c", className.str[i]);

	cprintf("_");

	for (i = 0; i < methodName.len; i++)
		cprintf("%c", methodName.str[i]);
	cprintf("_");

	for (i = 0; i < methodDesc.len; i++)
		cprintf("%c", methodDesc.str[i]);
	cprintf("\n");
	cprintf("{ %u, func },\n", hash);
	}
#endif
#elif defined(UNIX)
	{
		char *tClass, *tName, *tDesc, *tRes;

		if (((tClass = (char *)malloc(className.len + 1)) == NULL) ||
		    ((tName = (char *)malloc(methodName.len + 1)) == NULL) ||
		    ((tDesc = (char *)malloc(methodDesc.len + 1)) == NULL))
			err(1, "cannot allocate memory");
		memcpy(tClass, className.str, className.len);
		memcpy(tName, methodName.str, methodName.len);
		memcpy(tDesc, methodDesc.str, methodDesc.len);
		tClass[className.len] = '\0';
		tName[methodName.len] = '\0';
		tDesc[methodDesc.len] = '\0';

		if (asprintf(&tRes, "** Native Method Missing:\n"
		    "// %s_%s_%s\n{ %uU, func },\n",
		    tClass, tName, tDesc, hash) == -1)
			err(1, "cannot format string");

		free(tClass);
		free(tName);
		free(tDesc);
		debugString(tRes);
		free(tRes);
	}
#endif
#endif
	return NULL;

	}



// Hooks are used for objects that access system resources. Classes
// that are "hooked" may allocate extra variables so system resource
// pointers can be stored in the object. All hooked objects
// have an object destroy function that is called just before they
// are garbage collected allowing system resources to be deallocated.
static void setClassHooks(WClass *wclass)
	{
	UtfString className;
	ClassHook *hook;
	uint16 i, nameLen;

	// NOTE: Like native methods, we could hash the hook class names into
	// a value if we make sure that the only time we'd check an object for
	// hooks is if it was in the waba package. This would make lookup
	// faster and take up less space. If the hook table is small, though,

	// it doesn't make much difference.
	className = getUtfString(wclass, wclass->classNameIndex);
	if (className.len < 6)
		return; // all hook classes are >= 6 character names
	i = 0;
	while (1)
		{
		hook = &classHooks[i++];
		if (hook->className == NULL)
			break;
		if (className.str[5] != hook->className[5])
			continue; // quick check to see if we should compare at all
		nameLen = xstrlen(hook->className);
		if (className.len == nameLen &&
			!xstrncmp(className.str, hook->className, nameLen))
			{
			wclass->objDestroyFunc = hook->destroyFunc;
			/*
			strncpy(sprintBuffer,className.str,className.len);
			sprintf(sprintBuffer+className.len," %d",wclass->numVars);
			debugString(sprintBuffer);
			*/
			wclass->numVars += hook->varsNeeded;
			// FIX - remove this! For testing only!
			wclass->numVars += 0;
			return;
			}
		}
	}

/*
 "Thirty spokes join at the hub;
  their use for the cart is where they are not.
  When the potter's wheel makes a pot,
  the use of the pot is precisely where there is nothing.
  When you open doors and windows for a room,
  it is where there is nothing that they are useful to the room.
  Therefore being is for benefit,
  Nonbeing is for usefulness."
*/

//
// Method Execution
//


//
// This is the interpreter code. Each method call pushes a frame on the
// stack. The structure of a stack frame is:
//
// local var 1
// local var 2
// local var N
// local stack 1
// local stack 2
// local stack N
//
// when a method is called, the following is pushed on the stack before
// the next frame:
//
// wclass

// method
// pc
// var
// stack
//
// NOTE: You can, of course, increase the performance of the interpreter
// in a number of ways. I've found a good book on assembly optimization
// to be:

//

// Inner Loops - A sourcebook for fast 32-bit software development
// by Rick Booth
//

#define NO_MLB_OPTIMIZE


static Var capitalize(Var stack[])
{
	Var v;
	WObject ca = stack[1].obj;
	v.intValue = 0;
	if (ca == 0) return v;
	else{
		int16 *ch = (int16 *)WOBJ_arrayStart(ca);
		int len = WOBJ_arrayLen(ca);
		int i;
		for (i = 0; i<len; i++)
			if (ch[i] >= 'a' && ch[i] <= 'z') ch[i] ^= 0x20;
	}
	return v;
}

static NativeFunc defaultEweNativeMethod(WClass *iclass,WClassMethod *imethod,UtfString name)
{
	UtfString cn = WCLASS_className(iclass);
	if (cn.len > 4)
		if (strncmp("ewe/",cn.str,4) == 0)
			return &NullMethod;
	return NULL;
}

static NativeFunc tryFindNativeMethodFunc(WClass *iclass,WClassMethod *imethod,UtfString name);

static void fillStackTrace(WObject ex,int ignore)
{
	if (1){
		uint32 i, stackPtr;
		WObject st;
		int ig = 0;
		uint32 len = 0;

		uchar *pc = curPC;
		char *buff = (char *)mMalloc(1);
		buff[0] = 0;


		pushObject(ex);
		stackPtr = vmStackPtr;
		while (stackPtr > 0)
			{
			WClass *wclass = (WClass *)vmStack[--stackPtr].refValue;
			WClassMethod *method = (WClassMethod *)vmStack[--stackPtr].refValue;
			int native = (METH_accessFlags(method) & ACCESS_NATIVE) > 0;
			if (native) stackPtr -= vmStack[--stackPtr].intValue;
			else stackPtr -= METH_maxLocals(method)+METH_maxStack(method);

			if (ig++ >= ignore){
				UtfString className = wclass == NULL ? createUtfString("<array>") : getUtfString(wclass, wclass->classNameIndex);
				UtfString methodName = wclass == NULL ? createUtfString(method->methodName) : getUtfString(wclass, METH_nameIndex(method));
				UtfString fileName =  wclass == NULL ? createUtfString("") : getUtfString(wclass,getAttributeIndex(wclass,wclass->classAttributes,"SourceFile",10));
				//methodDesc = getUtfString(wclass, METH_descIndex(method));
				uint32 need;
				char *nb;
				if (wclass == NULL) methodName.len = method->methodNameLength;
				need = 1+len+4+className.len+1+methodName.len+fileName.len+2+1;
				sprintBuffer[0] = 0;
				if (native) sprintf(sprintBuffer,":<native method>");
				else if (METH_lineNumbers(method)){
					uchar *p = METH_lineNumbers(method);
					uint16 where = (uint16)(pc-METH_code(method));
					uint32 line = 0;
					uint16 num = getUInt16(p), l = 0;
					p += 2;
					for (l = 0; l<num; l++, p += 4){
						uint16 st = getUInt16(p);
						uint16 ln = getUInt16(p+2);
						if (where < st) break;
						else line = ln;
					}
					if (line != 0)
						sprintf(sprintBuffer,":%u",line);
				}
				nb = (char *)mMalloc(need+strlen(sprintBuffer));
				strncpy(nb,buff,len);
				free(buff);
				buff = nb;
				strcpy(buff+(len++),"\n");
				strcpy(buff+len,"\tat "); len+=4;
				strncpy(buff+len,className.str,className.len); len += className.len;
				strcpy(buff+(len++),".");

				strncpy(buff+len,methodName.str,methodName.len); len += methodName.len;
				strncpy(buff+(len++),"(",1);
				strncpy(buff+len,fileName.str,fileName.len); len += fileName.len;
				strcpy(buff+len,sprintBuffer); len += strlen(sprintBuffer);
				strcpy(buff+len,")"); len += 1;
				buff[len] = 0;
			}
			stackPtr -= STACKSAVESIZE;
			if (stackPtr >= 0)
				pc = vmStack[stackPtr].oldpc;
			}
		for (i = 0; i<len; i++) if (buff[i] == '/') buff[i] = '.';
		st = createString(buff);
		if (ex == 0) debugString(buff);
		else objectPtr(ex)[2].obj = st;
		free(buff);
		popObject();
	}
}

static void debugStackTrace()
{
	fillStackTrace(0,0);
}


static Var ThrowableFillInStackTrace(Var stack[])
{
	Var v;
	v.obj = stack[0].obj;
	fillStackTrace(v.obj,stack[1].intValue);
	return v;
}

static int tryExpandStack(VmContext context,Var **stack,Var **var)
{
	if (context == NULL) return 0;
	context->stack = *stack;
	context->var = *var;
	if (!getMoreStackSpace(context,VmStackSizeInBytes)) return 0;
	*stack = context->stack;
	*var = context->var;
	vmStack = context->vmStack;
	vmStackSize = context->vmStackSize;
	return 1;
}

static WObject throwExceptionObject(char *exceptionClass, WObject messageString)
{
	WClass *ec = NULL;
	WObject ex = 0;
	WClassMethod *init = NULL;
	if (messageString != 0) pushObject(messageString);
	ec = getClass(createUtfString(exceptionClass));
	if (ec != NULL) {
		thrownException = ex = createObject(ec);
		if (ex != 0) objectPtr(ex)[1].obj = messageString;
	}
	if (messageString != 0) popObject();
	return ex;
}
static WObject throwExceptionUtf(char *exceptionClass,UtfString message)//,Var **stack)
{
	return throwExceptionObject(exceptionClass, message.len == -1 ? 0 : createStringFromUtf(message));
}
static WObject throwException(char *exceptionClass,char *message)//,Var **stack)
{
	UtfString msg;
	msg.len = (uint32_t)-1;
	if (message != NULL) msg = createUtfString(message);
	return throwExceptionUtf(exceptionClass,msg);
}

static WObject getSetException(int isGet,WObject ex)
{
	if (isGet) return thrownException;
	handlingException = 0;
	thrownException = ex;
	return ex;
}
static WObject throwExceptionError(uint16 which)//,Var **stack)
{
	if (which == ERR_NullObjectAccess || which == ERR_NullArrayAccess)
		return throwException(NullPointerEx,NULL);//,stack);
	if (which == ERR_IndexOutOfRange)
		return throwException(ArrayIndexEx,NULL);//,stack);
	if (which == ERR_StringIndexOutOfRange)
		return throwException(StringIndexEx,NULL);//,stack);
	if (which == ERR_DivideByZero)
		return throwException(DivideByZeroEx,"/ by zero");//,stack);
	else
		return returnError(which).obj;
}

int stepped = 0;
//int biggest = 0;


#define OffsetMask 0x3fffffff
#define MethodInvokeFlag 0x80000000
#define ConstructorNewInstanceFlag 0x40000000

static int maxmeth;
#ifdef UNIX
static int timerThreadExit = 0;
#endif

static UtfString a_utf;

static void fullExecuteMethod(WClass *wclass, WClassMethod *method,

	Var params[], uint32 numParams, Var *retValue, VmContext context)
	{
	 int64 ll,ll1,ll2;
    double d1,d2,d;
	//register
		Var *var;
	//register
		Var *stack;
	register uchar *pc;
	uchar *startpc;
	uchar *oldpc;
	//uchar lastPC;
	uint32 baseFramePtr;

	int pushReturnedValue; // see "goto methodreturn"
	Var returnedValue, *vptr;
//MLB

	static WClass *lastCastSource = NULL, *lastWClass = NULL;
	static uint16 lastCastIndex = (uint16)-1;
	VmContext saved;
	static int pass = 0;
	Var *stt;
#ifndef NO_MLB_OPTIMIZE
	register
		Var *dest,*src;
	static uint16 lastIdx = -1;
	static WClassField *lastfield = NULL;
	static WObject lastobj = 0;
	static WClass *lastclass = NULL;
#endif

	// the variables wclass, method, var, stack, and pc need to be
	// pushed and restored when calling methods using "goto methoinvoke"

	// also note that this method does recurse when we hit static class
	// initializers which execute within a class load operation and this
	// is why we exit when we keep trace of the baseFramePtr.


//MLB this is for context switching.

	if (timerThreadExit) return;

	saved = currentContext;
	if (saved != NULL){
		saved->vmStackPtr = vmStackPtr;
		//saved->vmStackSize = vmStackSize;
	}
	currentContext = context;
	if (context != NULL) context->fullyReturned = 0;

	if (context != NULL){
		vmStack = context->vmStack;
		vmStackSize = context->vmStackSize;
		vmStackPtr = context->vmStackPtr;
		if (wclass == NULL){
			wclass = context->wclass;
			method = context->method;
			var = context->var;
			pc = context->pc;
			oldpc = context->oldpc;
			stack = context->stack;
			baseFramePtr = context->baseFramePtr;

			goto step;
		}
	}else if (saved != NULL){
		vmStack = mainVmStack;
		vmStackPtr = mainVmStackPtr;
		vmStackSize = mainVmStackSize;
	}

	if ((METH_accessFlags(method) & ACCESS_NATIVE) > 0){
//MLB
		Var ret;
		NativeFunc *nf = METH_nativeFuncPointer(method);
		if (*nf == NULL)

			*nf = tryFindNativeMethodFunc(wclass,method,getUtfString(wclass,METH_nameIndex(method)));
		if (*nf == NULL){
			throwExceptionUtf(UnsatisfiedLink,getUtfString(wclass,METH_nameIndex(method)));
			goto return_now;
		}
		if (method->nativeUsesJValue) ret = executeJValueNative(method,params);
		else ret = (*nf)(params);
		if (retValue) *retValue = ret;
		goto return_now;
	}
	if (METH_codeAttr(method) == NULL)
		goto return_now;
	baseFramePtr = vmStackPtr;

	while (vmStackPtr + STACKSAVESIZE + METH_maxLocals(method) +
		METH_maxStack(method) + (uint32)2 >= vmStackSize){
		if (!tryExpandStack(activeContext,&stack,&var))
			goto stack_overflow_error;
	}
	// push an unused return stack frame. This is important since all stack
	// frames need to look the same. Stack frames that are pushed by invoke
	// need to look the same as stack frames that are pushed when a static
	// class initialzer method is executed or the stack could not be walked.


#ifdef NO_MLB_OPTIMIZE


#ifdef OLDPC
	vmStack[vmStackPtr++].oldpc = 0;
#endif
	vmStack[vmStackPtr++].pc = 0;
	vmStack[vmStackPtr++].refValue = 0;
	vmStack[vmStackPtr++].refValue = 0;

	// push params into local vars of frame
	while (numParams > 0)
		{
		numParams--;
		vmStack[vmStackPtr + numParams] = params[numParams];
		}
#else

	dest = vmStack+vmStackPtr;
#ifdef OLDPC
	(*(dest+0)).oldpc = 0;
	(*(dest+1)).pc = 0;
	(*(dest+2)).refValue = 0;
	(*(dest+3)).refValue = 0;
#else

	(*(dest+0)).pc = 0;
	(*(dest+1)).refValue = 0;
	(*(dest+2)).refValue = 0;
#endif
	vmStackPtr += STACKSAVESIZE;

	dest += STACKSAVESIZE;

	{
		register int i = numParams;
		src = params;
		while(i>0){
			i--;
			*(dest+i) = *(src+i);
		}
	}
	numParams = 0;
#endif

methodinvoke:
	// push active stack frame:
	//
	// local var 1
	// ...
	// local var N
	// local stack 1
	// ...
	// local stack N
	// method pointer
	// class pointer
	var = &vmStack[vmStackPtr];
	vmStackPtr += METH_maxLocals(method);
	stt = stack = &vmStack[vmStackPtr];
	vmStackPtr += METH_maxStack(method);
	vmStack[vmStackPtr++].refValue = method;
	vmStack[vmStackPtr++].refValue = wclass;
	if (handlingException != 0){
		Var *s = stack;

		for (s = stack; s <= vmStack+vmStackPtr-3; s++)
			(*s).obj = 0;
	}
	startpc = pc = METH_code(method);


step:
//#define SLOW_DOWN
//	checkGotMore();
#ifdef SLOW_DOWN
	Sleep(0); //This will slow it down to ce speed.
#endif
	stepped++;
//	lastPC = *pc;
	if (thrownException)
		goto throw_exception;

	curPC = oldpc = pc;
	switch (*pc)
		{
		case OP_nop:
			pc++;
			break;
		case OP_aconst_null:
			stack[0].obj = 0;
			stack++;
			pc++;
			break;
		case OP_iconst_m1:
		case OP_iconst_0:
		case OP_iconst_1:
		case OP_iconst_2:
		case OP_iconst_3:
		case OP_iconst_4:
		case OP_iconst_5:
			// NOTE: testing shows there is no real performance gain to
			// splitting these out into seperate case statements
			stack[0].intValue = (*pc - OP_iconst_0);
			stack++;
			pc++;
			break;
		case OP_fconst_0:
			stack[0].floatValue = 0.0f;
			stack++;
			pc++;
			break;
		case OP_fconst_1:
			stack[0].floatValue = 1.0f;
			stack++;
			pc++;
			break;
		case OP_fconst_2:
			stack[0].floatValue = 2.0f;
			stack++;
			pc++;
			break;
		case OP_bipush:
			stack[0].intValue = ((char *)pc)[1];
			stack++;

			pc += 2;
			break;
		case OP_sipush:
			stack[0].intValue = getInt16(&pc[1]);
			stack++;
			pc += 3;
			break;
		case OP_ldc:

			*stack = constantToVar(wclass, (uint16)pc[1]);
			stack++;
			pc += 2;

			break;
		case OP_ldc_w:
			*stack = constantToVar(wclass, getUInt16(&pc[1]));
			stack++;
			pc += 3;
			break;
		case OP_iload:
		case OP_fload:
		case OP_aload:
			*stack = var[pc[1]];
			stack++;
			pc += 2;
			break;
		case OP_iload_0:
		case OP_iload_1:
		case OP_iload_2:
		case OP_iload_3:
			*stack = var[*pc - OP_iload_0];
			stack++;
			pc++;
			break;
		case OP_fload_0:
		case OP_fload_1:
		case OP_fload_2:
		case OP_fload_3:
			*stack = var[*pc - OP_fload_0];
			stack++;
			pc++;
			break;
		case OP_aload_0:
		case OP_aload_1:
		case OP_aload_2:

		case OP_aload_3:
			*stack = var[*pc - OP_aload_0];
			stack++;
			pc++;
			break;
		case OP_iaload:
			{
			WObject obj;
			int32 i;
			Var *objPtr;

			obj = stack[-2].obj;
			i = stack[-1].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			stack[-2].intValue = ((int32 *)WOBJ_arrayStartP(objPtr))[i];
			stack--;
			pc++;
			break;
			}
		case OP_saload:
			{
			WObject obj;

			int32 i;
			Var *objPtr;

			obj = stack[-2].obj;
			i = stack[-1].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;

			stack[-2].intValue = (int32)(((int16 *)WOBJ_arrayStartP(objPtr))[i]);
			stack--;
			pc++;
			break;

			}
		case OP_faload:
			{
			WObject obj;
			int32 i;
			Var *objPtr;


			obj = stack[-2].obj;
			i = stack[-1].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			stack[-2].floatValue = ((float32 *)WOBJ_arrayStartP(objPtr))[i];
			stack--;
			pc++;
			break;
			}
		case OP_aaload:
			{

			WObject obj;
			int32 i;
			Var *objPtr;


			obj = stack[-2].obj;
			i = stack[-1].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			stack[-2].obj = ((WObject *)WOBJ_arrayStartP(objPtr))[i];
			stack--;
			pc++;
			break;
			}
		case OP_baload:
			{
			WObject obj;
			int32 i;
			Var *objPtr;

			obj = stack[-2].obj;
			i = stack[-1].intValue;
			if (obj == 0) goto null_array_error;

			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			stack[-2].intValue = (int32)(((char *)WOBJ_arrayStartP(objPtr))[i]);
			stack--;
			pc++;
			break;
			}
		case OP_caload:

			{
			WObject obj;
			int32 i;
			Var *objPtr;

			obj = stack[-2].obj;
			i = stack[-1].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			stack[-2].intValue = (int32)(((uint16 *)WOBJ_arrayStartP(objPtr))[i]);
			stack--;
			pc++;
			break;
			}
		case OP_astore:
		case OP_istore:

		case OP_fstore:
			stack--;
			var[pc[1]] = *stack;
			pc += 2;
			break;
		case OP_istore_0:
		case OP_istore_1:
		case OP_istore_2:
		case OP_istore_3:
			stack--;
			var[*pc - OP_istore_0] = *stack;
			pc++;
			break;
		case OP_fstore_0:
		case OP_fstore_1:
		case OP_fstore_2:
		case OP_fstore_3:
			stack--;
			var[*pc - OP_fstore_0] = *stack;
			pc++;
			break;
		case OP_astore_0:
		case OP_astore_1:

		case OP_astore_2:
		case OP_astore_3:
			stack--;
			var[*pc - OP_astore_0] = *stack;
			pc++;
			break;
		case OP_iastore:

			{

			WObject obj;

			int32 i;
			Var *objPtr;


			obj = stack[-3].obj;
			i = stack[-2].intValue;
			if (obj == 0)
				goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr))
				goto index_range_error;
			((int32 *)WOBJ_arrayStartP(objPtr))[i] = stack[-1].intValue;
			stack -= 3;
			pc++;
			break;
			}
		case OP_sastore:
			{
			WObject obj;
			int32 i;
			Var *objPtr;

			obj = stack[-3].obj;
			i = stack[-2].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			((int16 *)WOBJ_arrayStartP(objPtr))[i] = (int16)stack[-1].intValue;
			stack -= 3;
			pc++;
			break;
			}
		case OP_fastore:
			{
			WObject obj;
			int32 i;
			Var *objPtr;


			obj = stack[-3].obj;
			i = stack[-2].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			((float32 *)WOBJ_arrayStartP(objPtr))[i] = stack[-1].floatValue;
			stack -= 3;
			pc++;
			break;
			}
		case OP_aastore:
			{
			WObject obj, element;
			int32 i;
			Var *objPtr;

			obj = stack[-3].obj;
			i = stack[-2].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);

			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			if ((element = stack[-1].obj) != 0){
				if (!compatibleElement(element,obj)){
					throwException("java/lang/ArrayStoreException",NULL);
					goto throw_exception;
				}
			}
			((WObject *)WOBJ_arrayStartP(objPtr))[i] = element;
			stack -= 3;
			pc++;
			break;
			}
		case OP_bastore:
			{
			WObject obj;
			int32 i;
			Var *objPtr;

			obj = stack[-3].obj;
			i = stack[-2].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			((char *)WOBJ_arrayStartP(objPtr))[i] = (char)stack[-1].intValue;
			stack -= 3;

			pc++;
			break;
			}
		case OP_castore:
			{
			WObject obj;
			int32 i;
			Var *objPtr;

			obj = stack[-3].obj;
			i = stack[-2].intValue;
			if (obj == 0) goto null_array_error;
			objPtr = objectPtr(obj);
			if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
			((uint16 *)WOBJ_arrayStartP(objPtr))[i] = (uint16)stack[-1].intValue;
			stack -= 3;
			pc++;
			break;
			}
		case OP_pop:
			stack--;
			pc++;
			break;
		case OP_pop2:
			stack -= 2;
			pc++;
			break;
		case OP_dup:
			stack[0] = stack[-1];
			stack++;
			pc++;
			break;
		case OP_dup_x1:
			stack[0] = stack[-1];
			stack[-1] = stack[-2];
			stack[-2] = stack[0];
			stack++;
			pc++;
			break;
		case OP_dup_x2:
			stack[0] = stack[-1];
			stack[-1] = stack[-2];
			stack[-2] = stack[-3];
			stack[-3] = stack[0];
			stack++;
			pc++;
			break;
		case OP_dup2:
			stack[1] = stack[-1];
			stack[0] = stack[-2];
			stack += 2;
			pc++;
			break;

		case OP_dup2_x1:
			stack[1] = stack[-1];

			stack[0] = stack[-2];
			stack[-1] = stack[-3];
			stack[-2] = stack[1];
			stack[-3] = stack[0];
			stack += 2;
			pc++;
			break;
		case OP_dup2_x2:
			stack[1] = stack[-1];
			stack[0] = stack[-2];
			stack[-1] = stack[-3];
			stack[-2] = stack[-4];
			stack[-3] = stack[1];
			stack[-4] = stack[0];
			stack += 2;

			pc++;
			break;
		case OP_swap:
			{
			Var v;

			v = stack[-2];
			stack[-2] = stack[-1];
			stack[-1] = v;
			pc++;
			break;
			}
		case OP_iadd:


			stack[-2].intValue += stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_fadd:
			stack[-2].floatValue += stack[-1].floatValue;
			stack--;
			pc++;
			break;
		case OP_isub:
			stack[-2].intValue -= stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_fsub:
			stack[-2].floatValue -= stack[-1].floatValue;
			stack--;
			pc++;
			break;
		case OP_imul:
			stack[-2].intValue *= stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_fmul:
			stack[-2].floatValue *= stack[-1].floatValue;
			stack--;
			pc++;
			break;
		case OP_idiv:
			if (stack[-1].intValue == 0)
				goto div_by_zero_error;
			stack[-2].intValue /= stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_fdiv:
			if (stack[-1].floatValue == 0.0f)
				goto div_by_zero_error;
			stack[-2].floatValue /= stack[-1].floatValue;
			stack--;
			pc++;
			break;
		case OP_irem:
			if (stack[-1].intValue == 0)
				goto div_by_zero_error;
			stack[-2].intValue = stack[-2].intValue % stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_frem:
			{
			float32 f;

			if (stack[-1].floatValue == 0.0f)
				goto div_by_zero_error;
			f = stack[-2].floatValue / stack[-1].floatValue;
			f = (float32)((int32)f);
			f *= stack[-1].floatValue;
			stack[-2].floatValue = stack[-2].floatValue - f;
			stack--;
			pc++;
			break;
			}
		case OP_ineg:
			stack[-1].intValue = - stack[-1].intValue;
			pc++;
			break;
		case OP_fneg:
			stack[-1].floatValue = - stack[-1].floatValue;
			pc++;
			break;
		case OP_ishl:
			stack[-2].intValue = stack[-2].intValue << stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_ishr:
			stack[-2].intValue = stack[-2].intValue >> stack[-1].intValue;
			stack--;
			pc++;

			break;
		case OP_iushr:
			{

			int32 i;

			i = stack[-1].intValue;
			if (stack[-2].intValue >= 0)
				stack[-2].intValue = stack[-2].intValue >> i;
			else
				{
				stack[-2].intValue = stack[-2].intValue >> i;
				if (i >= 0)
					stack[-2].intValue += (int32)2 << (31 - i);
				else
					stack[-2].intValue += (int32)2 << ((- i) + 1);
				}
			stack--;
			pc++;
			break;
			}
		case OP_iand:
			stack[-2].intValue &= stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_ior:
			stack[-2].intValue |= stack[-1].intValue;
			stack--;
			pc++;
			break;

		case OP_ixor:
			stack[-2].intValue ^= stack[-1].intValue;
			stack--;
			pc++;
			break;
		case OP_iinc:
			var[pc[1]].intValue += (char)pc[2];
			pc += 3;
			break;
		case OP_i2f:
			stack[-1].floatValue = (float32)stack[-1].intValue;
			pc++;
			break;
		case OP_f2i:
			{
			float32 f;

			f = stack[-1].floatValue;

			if (f > 2147483647.0)
				stack[-1].intValue = 0x7FFFFFFF;
			else if (f < -2147483648.0)
				stack[-1].intValue = 0x80000000;
			else
				stack[-1].intValue = (int32)f;
			pc++;
			break;
			}
		case OP_i2b:
			stack[-1].intValue = (int32)((char)(stack[-1].intValue & 0xFF));
			pc++;
			break;
		case OP_i2c:

			stack[-1].intValue = (int32)((uint16)(stack[-1].intValue & 0xFFFF));
			pc++;
			break;
		case OP_i2s:
			stack[-1].intValue = (int32)((int16)(stack[-1].intValue & 0xFFFF));
			pc++;
			break;
		case OP_fcmpl:
		case OP_fcmpg:
			{
			float32 f;

			// NOTE: NaN values are currently not supported - NaN in either
			// value should return 1 or 0 depending on the opcode
			f = stack[-2].floatValue - stack[-1].floatValue;
			if (f > 0.0f)
				stack[-2].intValue = 1;
			else if (f < 0.0f)
				stack[-2].intValue = -1;
			else
				stack[-2].intValue = 0;
			stack--;

			pc++;
			break;
			}
		case OP_ifeq:
			if (stack[-1].intValue == 0)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack--;
			break;
		case OP_ifne:
			if (stack[-1].intValue != 0)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack--;
			break;
		case OP_iflt:
			if (stack[-1].intValue < 0)

				pc += getInt16(&pc[1]);
			else
				pc += 3;

			stack--;
			break;
		case OP_ifge:


			if (stack[-1].intValue >= 0)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack--;
			break;
		case OP_ifgt:
			if (stack[-1].intValue > 0)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack--;
			break;
		case OP_ifle:
			if (stack[-1].intValue <= 0)
				pc += getInt16(&pc[1]);

			else
				pc += 3;

			stack--;
			break;
		case OP_if_icmpeq:
			if (stack[-2].intValue == stack[-1].intValue)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack -= 2;
			break;
		case OP_if_icmpne:
			if (stack[-2].intValue != stack[-1].intValue)

				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack -= 2;
			break;
		case OP_if_icmplt:
			if (stack[-2].intValue < stack[-1].intValue)
				pc += getInt16(&pc[1]);

			else
				pc += 3;
			stack -= 2;
			break;
		case OP_if_icmpge:
			if (stack[-2].intValue >= stack[-1].intValue)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack -= 2;
			break;
		case OP_if_icmpgt:
			if (stack[-2].intValue > stack[-1].intValue)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack -= 2;
			break;
		case OP_if_icmple:
			if (stack[-2].intValue <= stack[-1].intValue)
			pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack -= 2;
			break;
		case OP_if_acmpeq:
			if (stack[-2].obj == stack[-1].obj)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack -= 2;
			break;
		case OP_if_acmpne:
			if (stack[-2].obj != stack[-1].obj)
				pc += getInt16(&pc[1]);

			else
				pc += 3;
			stack -= 2;
			break;
		case OP_goto:
			pc += getInt16(&pc[1]);
			break;
		case OP_jsr:
			stack[0].pc = pc + 3;
			stack++;
			pc += getInt16(&pc[1]);
			break;
		case OP_ret:
			pc = var[pc[1]].pc;
			break;
		case OP_tableswitch:
			{
			int32 key, low, high, defaultOff;
			uchar *npc;

			key = stack[-1].intValue;
			npc = pc + 1;
			npc += (4 - ((npc - METH_code(method)) % 4)) % 4;
			defaultOff = getInt32(npc);
			npc += 4;
			low = getInt32(npc);
			npc += 4;
			high = getInt32(npc);
			npc += 4;

			if (key < low || key > high)
				pc += defaultOff;
			else
				pc += getInt32(&npc[(key - low) * 4]);
			stack--;
			break;
			}
		case OP_lookupswitch:
			{
			int32 i, key, low, mid, high, npairs, defaultOff;
			uchar *npc;

			key = stack[-1].intValue;
			npc = pc + 1;
			npc += (4 - ((npc - METH_code(method)) % 4)) % 4;
			defaultOff = getInt32(npc);
			npc += 4;

			npairs = getInt32(npc);
			npc += 4;

			// binary search
			if (npairs > 0)
				{
				low = 0;
				high = npairs;
				while (1)
					{
					mid = (high + low) / 2;
					i = getInt32(npc + (mid * 8));
					if (key == i)
						{
						pc += getInt32(npc + (mid * 8) + 4); // found
						break;
						}
					if (mid == low)
						{
						pc += defaultOff; // not found
						break;
						}
					if (key < i)
						high = mid;
					else
						low = mid;
					}
				}
			else
				pc += defaultOff; // no pairs
			stack--;
			break;
			}
		case OP_ireturn:

		case OP_freturn:
		case OP_areturn:
		case OP_return:
			if (*pc != OP_return)
				{
				returnedValue = stack[-1];
				pushReturnedValue = 1;
				}
			else
				pushReturnedValue = 0;
			goto methodreturn;
		case OP_getfield:
			{
			WClassField *field;
			WObject obj;

			uint16 idx = getUInt16(&pc[1]);
			obj = stack[-1].obj;
			/*
			if (isFree(obj)){
				goto step;
			}
			*/

#ifndef NO_MLB_OPTIMIZE
			if (lastIdx == idx && lastobj == obj && lastclass == wclass){

				stack[-1] = WOBJ_var(obj, lastfield->var.varOffset);
				pc += 3;
				break;

			}
#endif


			field = GetFieldByIndex(wclass, idx);
			if (!field)
				goto throw_exception;
			if (obj == 0)
				goto null_obj_error;
            if (is64(field)) //64Bits
            {

               stack[-1] = WOBJ_var(obj, GetVarOffset(field));
               stack[0] = WOBJ_var(obj, GetVarOffset2(field));
               stack++;
            }
            else{
				Var *vv = objectPtr(obj);
				int off = GetVarOffset(field);
				stack[-1] = WOBJ_var(obj, GetVarOffset(field));
			}

			pc += 3;

#ifndef NO_MLB_OPTIMIZE
			lastIdx = idx;
			lastobj = obj;
			lastfield = field;
			lastclass = wclass;
#endif
			break;
			}
		case OP_putfield:
			{
			WClassField *field;
			WObject obj;
			uint16 idx = getUInt16(&pc[1]);
#ifndef NO_MLB_OPTIMIZE
			if (lastIdx == idx && lastobj == obj && lastclass == wclass){

				WOBJ_var(obj, lastfield->var.varOffset) = stack[-1];
				stack -= 2;
				pc += 3;
				break;
			}
#endif

			field = GetFieldByIndex(wclass, idx);
			if (!field)
				goto throw_exception;

            if (is64(field))//64Bits
            {
	            stack -= 3;
	            obj = stack[0].obj;
				/*
				if (isFree(obj)){
					goto step;
				}*/
	            if (obj == 0)
	                goto null_obj_error;
	            WOBJ_var(obj, GetVarOffset(field)) = stack[1];
	            WOBJ_var(obj, GetVarOffset2(field)) = stack[2];
            }
            else
            {
	            stack -= 2;


	            obj = stack[0].obj;
				/*
				if (isFree(obj)){
					goto step;
				}*/
	            if (obj == 0)
	                goto null_obj_error;
	            WOBJ_var(obj, GetVarOffset(field)) = stack[1];
	         }
			pc += 3;

#ifndef NO_MLB_OPTIMIZE
			lastIdx = idx;
			lastobj = obj;
			lastfield = field;
			lastclass = wclass;
#endif
			break;
			}

		case OP_getstatic:
			{
			WClassField *field;

			field = getFieldByIndex(wclass, getUInt16(&pc[1]));
			if (!field)
				goto throw_exception;
            if (is64(field))
            {
	            stack[1] = *GetStaticVarPointer2(field);
	            stack[0] = *GetStaticVarPointer(field);
	            stack+=2;
            }
            else
            {
	            stack[0] = *GetStaticVarPointer(field);
	            stack++;
	         }

			pc += 3;
			break;
			}
		case OP_putstatic:
			{
			WClassField *field;

			field = getFieldByIndex(wclass, getUInt16(&pc[1]));

			if (!field)
				goto throw_exception;
			LOCK_CLASS_HEAP
            if (is64(field))//fieldIs64wide(wclass, field))
            {
            	*GetStaticVarPointer(field) = stack[-2];
            	*GetStaticVarPointer2(field) = stack[-1];
				stack-=2;
            }
            else
            {
               *GetStaticVarPointer(field) = stack[-1];
               stack--;
            }
			UNLOCK_CLASS_HEAP
			pc += 3;
			break;
			}
		case OP_invokestatic:
		case OP_invokeinterface:
		case OP_invokevirtual:
		case OP_invokespecial:
			{
			int32 i;
			uint16 nparam, classIndex, methodIndex, nameAndTypeIndex;
			int methodNameValid;

			WObject obj;
			WClass *iclass = NULL;
			WClassMethod *imethod = NULL;

			UtfString methodName, methodDesc;
			BOOL dontBindThis = FALSE;

#ifdef QUICKBIND
			int32 methodMapNum = -1;
			int searchType;
#endif
			methodNameValid = 0;
			methodIndex = getUInt16(&pc[1]);
#ifdef QUICKBIND
			if (WCLASS_isPooled(wclass)){
				if (*pc != OP_invokeinterface){
					uchar *ptr = POOLED_pointer(wclass,methodIndex);
					classIndex = POOLED_firstShort(ptr);
					methodMapNum = POOLED_secondShort(ptr);
					iclass = getClassByIndex(wclass, classIndex);
					if (iclass == NULL) goto methoderror;
					imethod = getMethodByMapNum(iclass, *pc == OP_invokespecial ? &iclass : NULL, (uint16)methodMapNum);
					//className = WCLASS_className(iclass);
					//methodName = METH_name(iclass,imethod);
					//methodDesc = METH_desc(iclass,imethod);
				}
			}else{
				ConsOffset offset = CONS_offset(wclass, methodIndex);
				//
				if ((offset & CONS_boundBit) == 0){
					classIndex = CONS_classIndex(wclass, methodIndex);
					iclass = getClassByIndex(wclass, classIndex);
					offset = CONS_offset(wclass, methodIndex);
					if (iclass == NULL) goto methoderror;
				}
				//
				// It is possible that during getClassByIndex() the <clinit>
				// may get called, which _may_ execute this method again if there
				// are circular references, and that method may cause the bound bit
				// to be set. In which case, when getClassByIndex returns, we should
				// check it again.
				//
				if (offset & CONS_boundBit){
					offset &= ~CONS_boundBit;
					methodMapNum = offset >> CONS_boundMethodShift;
					classIndex = offset & CONS_boundClassMask;
					iclass = getClassByIndex(wclass, classIndex);
					if (iclass == NULL) goto methoderror;
					imethod = getMethodByMapNum(iclass, *pc == OP_invokespecial ? &iclass : NULL, (uint16)methodMapNum);
				}
			}
			//
			// Don't know what the method is.
			//
			if (methodMapNum == -1)
				{
				if (iclass == NULL){
					classIndex = CONS_classIndex(wclass, methodIndex);
					iclass = getClassByIndex(wclass, classIndex);
					if (iclass == NULL) {
						//printf("Can't find class!\n");
						goto methoderror;
					}
				}
				nameAndTypeIndex = CONS_nameAndTypeIndex(wclass, methodIndex);
				//className = WCLASS_className(iclass);
				methodName = getUtfString(wclass, CONS_nameIndex(wclass, nameAndTypeIndex));
				methodDesc = getUtfString(wclass, CONS_typeIndex(wclass, nameAndTypeIndex));
				methodNameValid = 1;
				if (*pc == OP_invokevirtual || *pc == OP_invokespecial || *pc == OP_invokestatic || *pc == OP_invokeinterface)
					searchType = SEARCH_ALL;
				else
					searchType = SEARCH_THISCLASS;
				//
				//
				if (*pc == OP_invokeinterface){
					//imethod = getMethod(iclass,methodName, methodDesc, &iclass);
					imethod = getCachedInterface(wclass,methodIndex,iclass,&iclass);
					if (imethod == NULL) {
						//printf("Can't find cached interface");
						goto methoderror;

					}
				}else{
					//
					methodMapNum = getMethodMapNum(iclass, methodName, methodDesc, searchType);
					if (methodMapNum < 0){
						//
						// I could not find the map number - if it is not virtual
						// then there is definitely no such method.
						//
						if (*pc != OP_invokevirtual) goto methoderror;
/*
 November 2004

If it is invokevirtual then there is a possibility where the following might occur.

 public interface Interface{ void iMethod();}
 public abstract class Test implements Interface{ void test() {iMethod();}}
 public class BigTest extends Test{void iMethod(){}}

In method test() most compilers would say: "iMethod() is not declared by Test or
its superclasses. iMethod is a method in Interface, so the call to iMethod() in test()
will be done via an invokeinterface bytecode."

However in Sun's Java Compiler v. 1.4 the call to iMethod() in test() is done via
an invokevirtual bytecode instead. However since Test does not declare iMethod()
explicitly, it is not listed in its class as one of its methods. Therefore it does
not appear in its method map, consequently getMethodMapNum() will fail.

To overcome this we must look at the actual object the method is being invoked on
and search for the method this way instead. To do that we need to count the number
of parameters to locate the actual object. Then we do getMethodMapNum() on that
object.
*/
						dontBindThis = TRUE; //Cannot bind the map number to the class.
						nparam = countMethodParams(methodDesc,NULL)+1;
						obj = stack[-(int32)nparam].obj;
						if (obj == 0) goto null_obj_error;
						iclass = (WClass *)WOBJ_class(obj);
						if (iclass == NULL) goto methoderror;
						methodMapNum = getMethodMapNum(iclass,methodName,methodDesc,SEARCH_ALL);
						if (methodMapNum < 0) goto methoderror;
					}
					// adaptive quickbind for methods - bind the constant to the
					// method num and class index if it fits in the constant and if
					// the class is not an interface. If the class is an interface, we
					// need the name and desc of the method later, so we can't bind over
					// the constant that contains the nameAndTypeIndex
#ifndef DONT_BIND
					if (!WCLASS_isPooled(wclass) && !dontBindThis)
						if (methodMapNum <= MAX_boundMethodNum
#ifdef SMALLMEM
						    && (unsigned)classIndex <= MAX_boundClassIndex
#endif
						    )
							CONS_offset(wclass, methodIndex) = CONS_boundBit |
								(methodMapNum << CONS_boundMethodShift) | classIndex;
#endif
					imethod = getMethodByMapNum(iclass, *pc == OP_invokespecial ? &iclass : NULL, (uint16)methodMapNum);
				}
			}
			//
#else		// NO QUICKBIND - search for the method via the name and description.
			//
			classIndex = CONS_classIndex(wclass, methodIndex);
			iclass = getClassByIndex(wclass, classIndex);
			if (iclass == NULL)
				goto methoderror;
			nameAndTypeIndex = CONS_nameAndTypeIndex(wclass, methodIndex);
			methodName = getUtfString(wclass, CONS_nameIndex(wclass, nameAndTypeIndex));
			methodDesc = getUtfString(wclass, CONS_typeIndex(wclass, nameAndTypeIndex));
			methodNameValid = 1;
			if (*pc == OP_invokeinterface || *pc == OP_invokevirtual || *pc == OP_invokespecial || *pc == OP_invokestatic || *pc == OP_invokeinterface)
				imethod = getMethod(iclass, methodName, methodDesc, &iclass);
			else
				imethod = getMethod(iclass, methodName, methodDesc, NULL);
			if (imethod == NULL)
				goto methoderror;
#endif
			// get object reference and inc param count (if needed)
			nparam = imethod->numParams;
			obj = 0;
			// FIX - double-check sometime, can we do an interface invoke on
			// a static method? if so, we should check the method for static,
			// not the invoke type here
			if (*pc != OP_invokestatic)
				{
				nparam++;
				obj = stack[- (int32)nparam].obj;
				if (obj == 0)
					goto null_obj_error;
				}

			// skip Object <init> method (and pop off object reference)
			if (iclass->numSuperClasses == 0 && imethod->isInit)//MLB change method->isInit to imethod->isInit
				{
				stack -= nparam;
				pc += 3;
				break;
				}

#ifdef QUICKBIND
			if (*pc == OP_invokevirtual  || *pc == OP_invokestatic)
				{
				if (*pc == OP_invokevirtual)
					iclass = (WClass *)WOBJ_class(obj);
				if (!iclass) {
					WClass *cl = METH_class(imethod);
					methodName = getUtfString(cl, METH_nameIndex(imethod));
					methodDesc = getUtfString(cl, METH_descIndex(imethod));
					imethod = getArrayMethod(methodName,methodDesc);
				}else{
					imethod = getMethodByMapNum(iclass, &iclass, (uint16)methodMapNum);
				}
				if (!imethod) goto methoderror;
				pc += 3;
				}
			else if (*pc == OP_invokeinterface)
				{
				iclass = (WClass *)WOBJ_class(obj);
				if (!iclass) goto methoderror;
				imethod = getCachedInterface(wclass,methodIndex,iclass,&iclass);
				if (imethod == NULL) goto methoderror;
				/*
				methodMapNum = getMethodMapNum(iclass, methodName, methodDesc, SEARCH_ALL);
				if (methodMapNum < 0)
					goto methoderror;
				imethod = getMethodByMapNum(iclass, &iclass, (uint16)methodMapNum);
				*/
				pc += 5;
				}
			else
				pc += 3; //For Special.
#else // NO QUICKBIND
			if (*pc == OP_invokevirtual || *pc == OP_invokeinterface || *pc == OP_invokespecial)
				{
				if (*pc != OP_invokespecial)
					iclass = (WClass *)WOBJ_class(obj);
				// get method (and class if virtual)
				if (!iclass) imethod = getArrayMethod(methodName,methodDesc);
				else imethod = getMethod(iclass, methodName, methodDesc, &iclass);
				if (imethod == NULL) goto methoderror; //classes are out of sync/corrupt
				}

			if (*pc == OP_invokeinterface)
				pc += 5;
			else
				pc += 3;
#endif
			// push return stack frame:
			//
			// program counter pointer
			// local var pointer
			// local stack pointer
			if ((METH_accessFlags(imethod) & ACCESS_ABSTRACT) != 0 || (int)imethod == 0x0306e640){
				if (methodNameValid)
					throwExceptionUtf("java/lang/AbstractMethodError",methodName);
					//VmError(ERR_CantFindMethod, iclass, &methodName, &methodDesc);
				else
					throwException("java/lang/AbstractMethodError",NULL);
					//VmQuickError(ERR_CantFindMethod);
				goto throw_exception;
			}
//-n-
			if ((METH_accessFlags(imethod) & ACCESS_NATIVE) > 0)
				{
				NativeFunc *nf = METH_nativeFuncPointer(imethod);
				//MLB
				if (*nf == NULL){
					if (!methodNameValid){
						methodName = getUtfString(iclass, METH_nameIndex(imethod));
						methodNameValid = 1;
					}
					*nf = tryFindNativeMethodFunc(iclass,imethod,methodName);
				}
				if (*nf == NULL){
					throwExceptionUtf(UnsatisfiedLink,methodName);
					goto throw_exception;
				}
				// return stack frame plus native method active frame
				while (vmStackPtr + STACKSAVESIZE + nparam + 3 >= vmStackSize)
					if (!tryExpandStack(activeContext,&stack,&var))
						goto stack_overflow_error;
				memset(&vmStack[vmStackPtr],0,(STACKSAVESIZE+nparam+3)*sizeof(Var));
				}
			else
				{

				if (METH_codeAttr(imethod) == NULL)
					goto methoderror;
				// return stack frame plus active frame
				while (vmStackPtr + STACKSAVESIZE + METH_maxLocals(imethod) +
					METH_maxStack(imethod) + 2 >= vmStackSize)

						if (!tryExpandStack(activeContext,&stack,&var))
							goto stack_overflow_error;
				memset(&vmStack[vmStackPtr],0,(STACKSAVESIZE+METH_maxLocals(imethod)+METH_maxStack(imethod)+2)*sizeof(Var));
				}

			//sprintf(sprintBuffer,">> %x %x",(int)(stack-nparam),vmStack+vmStackPtr); debugString(sprintBuffer);
			memset(stack,0,((int)(&vmStack[vmStackPtr-2])-(int)stack));
#ifdef OLDPC
			vmStack[vmStackPtr++].oldpc = oldpc;
#endif
			vmStack[vmStackPtr++].pc = pc;
//MLB - Modified this so that var and stack are stored as values relative
//			to the start of the current stack.
			vmStack[vmStackPtr++].stackOffset = (var-vmStack) & OffsetMask;
			vmStack[vmStackPtr++].stackOffset = (stack-vmStack-nparam) & OffsetMask;
			/*
			if (var-vmStack > biggest) {
				biggest = var-vmStack;
				sprintf(sprintBuffer,"%d\n",biggest);
				debugString(sprintBuffer);

			}
			*/
// Old version

//			vmStack[vmStackPtr++].refValue = var;
//			vmStack[vmStackPtr++].refValue = stack - nparam;

			/*

			(METH_maxLocals(imethod) +
					METH_maxStack(imethod) + 2)*sizeof(Var));
					*/
			// push params into local vars of next frame
			for (i = 0; i < nparam; i++)
				{
				vmStack[vmStackPtr + nparam - i - 1] = stack[-1];
				stack--;
				}

			wclass = iclass;
			method = imethod;

			// execute native method
			if ((METH_accessFlags(method) & ACCESS_NATIVE) > 0)
				{
				NativeFunc *nf = METH_nativeFuncPointer(method);
				VmContext savedContext = currentContext;
				// the active frame for a native method is:
				//
				// param 1
				// ...
				// param N

				// num params
				// method pointer

				// class pointer
				vmStackPtr += nparam;
				vmStack[vmStackPtr++].intValue = nparam;


				vmStack[vmStackPtr++].refValue = method;
				vmStack[vmStackPtr++].refValue = wclass;
				if ((int)method == 0x0306e640){
					maxmeth = 0;
				}

				if (method->nativeUsesJValue) returnedValue = executeJValueNative(method,stack);
				else if (*nf == MethodInvoke || *nf == ConstructorNewInstance){
					int params;
					void *toFree;
					int isInvoke = *nf == MethodInvoke;

					returnedValue =
						isInvoke ?
						TrueMethodInvoke(stack,&params,&toFree,&wclass,&method) :
						TrueConstructorNewInstance(stack,&params,&toFree,&wclass,&method);

					currentContext = savedContext;
					if (vmStatus.errNum != 0)
						goto error; // error occured during native method


					if (params >= 0){
						//TrueMethodInvoke did not invoke it - this means that it is NOT native.
						//So we have to invoke it within this function.
						//So we need to setup the stack frame, but flag it so that when it is returning
						//It can do correct return of a Wrapper.
						Var *stackToUse = (Var *)returnedValue.refValue;
						maxmeth = METH_maxLocals(method);
						while (vmStackPtr + STACKSAVESIZE + METH_maxLocals(method) +
							METH_maxStack(method) + (uint32)2 >= vmStackSize){
							if (!tryExpandStack(activeContext,&stack,&var))

								goto stack_overflow_error;
						}

						#ifdef OLDPC
						vmStack[vmStackPtr++].oldpc = oldpc;
						#endif
						vmStack[vmStackPtr++].pc = pc;
						vmStack[vmStackPtr++].stackOffset = 0;//This will not be used! (var-vmStack) & OffsetMask;
						vmStack[vmStackPtr++].stackOffset = isInvoke ? MethodInvokeFlag : ConstructorNewInstanceFlag;//This will let us know to return correctly.
						if (params != 0)
							memcpy(&(vmStack[vmStackPtr]),stackToUse,sizeof(Var)*params);
						xfree(toFree);
						goto methodinvoke;
					}
				}else{
					returnedValue = (*nf)(stack);
				}
				currentContext = savedContext;


				if (vmStatus.errNum != 0)
					goto error; // error occured during native method
				if (timerThreadExit)
					goto return_now;

				pushReturnedValue = method->returnsValue;
				goto methodreturn;
				}

			goto methodinvoke;
methoderror:
			if (thrownException == 0)
				if (methodNameValid)
					throwExceptionUtf("java/lang/NoSuchMethodError",methodName);
					//VmError(ERR_CantFindMethod, iclass, &methodName, &methodDesc);
				else
					throwException("java/lang/NoSuchMethodError",NULL);
					//VmQuickError(ERR_CantFindMethod);
				goto throw_exception;
			//goto error;
			}
		case OP_new:
			{

			uint16 classIndex;
			WClass *cl;
			WObject got;


			classIndex = getUInt16(&pc[1]);
			cl = getClassByIndex(wclass, classIndex);
			if (cl == NULL) goto throw_exception;
			stack[0].obj = got = createObject(cl);
			stack++;
			pc += 3;
			if (got == 0) {
				throwExceptionUtf("java/lang/InstantiationError",cl == NULL ? createUtfString("<no class>") : getUtfString(cl,cl->classNameIndex));
				goto throw_exception;
			}
			break;
			}
		case OP_newarray:
			if (stack[-1].intValue < 0){
				throwExceptionUtf("java/lang/NegativeArraySizeException",createUtfString("Bad new array size."));
				goto throw_exception;
			}
			stack[-1].obj = createArrayObject((int32)pc[1], stack[-1].intValue);
			pc += 2;
			break;

		case OP_anewarray:
			{
				uint16 ci = getUInt16(&pc[1]);
				int len = stack[-1].intValue;
				WClass *component = getClassByIndex(wclass, ci);
				if (component == NULL){
					UtfString className = getUtfString(wclass, CONS_nameIndex(wclass, ci));
					stack[-1].obj = 0;
					if (className.len > 1 && className.str[0] == '['){
						char *ct = newArrayType(className);
						if (ct != NULL){
							WObject aa = createArrayObject(ARRAYTYPE_ARRAY,len);

							if (aa != 0){
								WOBJ_arrayComponent(aa) = ct;
								stack[-1].obj = aa;
							}
						}
					}
				}else{
					stack[-1].obj = createArrayObjectClass(component, len);
					//stack[-1].obj = createArrayObject(1, stack[-1].intValue);
				}
				pc += 3;
			break;
			}
		case OP_arraylength:
			{
			WObject obj;

			obj = stack[-1].obj;
			if (obj == 0)
				goto null_array_error;
			stack[-1].intValue = WOBJ_arrayLen(obj);
			pc++;
			break;
			}
		case OP_instanceof:
		case OP_checkcast:
			{
			WObject obj;
			uint16 classIndex;
			//UtfString className, sourceName;
			WClass *source, *target;
			int comp;

			obj = stack[-1].obj;
			if (obj == 0)
				{
				if (*pc == OP_instanceof)
					stack[-1].intValue = 0;
				pc += 3;
				break;
				}


			classIndex = getUInt16(&pc[1]);


			//target = getClassByIndex(wclass, classIndex);

			//if (target) className = getUtfString(target, target->classNameIndex);

			source = WOBJ_class(obj);


//MLB.........................................................................
			if (lastCastSource != 0 && source == lastCastSource && classIndex == lastCastIndex && wclass == lastWClass){
				comp = 1;
			}
//............................................................................
			else{
				target = getClassByIndex(wclass, classIndex);
				if (target)
					{
					//className = getUtfString(target, target->classNameIndex);
					//sourceName = getUtfString(source, source->classNameIndex);
					if (source && source->lastCast == target){
						comp = 1;
					}else{
						if (comp = compatible(source, target)) // target is not array
							if (source) source->lastCast = target;
					}
				}else
					{
					// target is either array or target class was not found
					// if either of these cases is true, the index couldn't be
					// bound to a pointer, so we still have a pointer into the
					// constant pool and can use the string reference in the constant
					uint16 cni = WCLASS_isPooled(wclass) ? POOLED_classNameIndex(wclass,classIndex) : CONS_nameIndex(wclass, classIndex);
					UtfString className = getUtfString(wclass,cni);
					if (className.len > 1 && className.str[0] == '[')
						comp = compatibleArray(obj, className); // target is array
					else
						comp = 0;//goto error; // target class not found
					}
			}

			if (*pc == OP_checkcast)
				{
				if (!comp)
					{
					UtfString nm;
					char *created = NULL;
					nm.len = 0; nm.str = NULL;
					if (source == 0){
						created = getArrayTypeString(obj);
						if (created) nm = createUtfString(created);
					}else nm = getUtfString(source,source->classNameIndex);
					throwExceptionUtf("java/lang/ClassCastException",nm);
					if (created) free(created);
					goto throw_exception;
					/*

					VmError(ERR_ClassCastException, source, &className, NULL);
					goto error;
					*/


					}
				}
			else
				stack[-1].intValue = comp;
			pc += 3;
//MLB.........................................................................
			if (comp && source != 0){
				lastWClass = wclass;
				lastCastSource = source;
				lastCastIndex = classIndex;
			}
//............................................................................
			break;
			}
		case OP_wide:
			pc++;
			switch (*pc)
				{
				case OP_iload:

				case OP_fload:
				case OP_aload:
					stack[0] = var[getUInt16(&pc[1])];
					stack++;
					pc += 3;
					break;
/*
                case OP_dload:
                case OP_lload:
		              vptr = &var[getUInt16(&pc[1])];
			           copy64(vptr,stack);

                    stack+=2;
                    pc += 3;
                    break;
*/
				case OP_astore:
				case OP_istore:
				case OP_fstore:
					var[getUInt16(&pc[1])] = stack[-1];
					stack--;
					pc += 3;
					break;
                case OP_dstore:
                case OP_lstore:
		              vptr = &var[getUInt16(&pc[1])];
			           stack-=2;
			           copy64(stack,vptr);
                    pc += 3;
                    break;
				case OP_iinc:
					var[getUInt16(&pc[1])].intValue += getInt16(&pc[3]);
					pc += 5;
					break;
				case OP_ret:
					pc = var[getUInt16(&pc[1])].pc;
					break;
				}
			break;
		case OP_multianewarray:
			{
			uint16 classIndex;
			UtfString className;
			int32 ndim;
			char *cstr;

			classIndex = getUInt16(&pc[1]);
			// since arrays do not have associated classes which could be bound
			// to the class constant, we can safely access the name string in
			// the constant
			className = getUtfString(wclass, CONS_nameIndex(wclass, classIndex));
			ndim = (int32)pc[3];
			cstr = &className.str[1];
			stack -= ndim;
			stack[0].obj = createMultiArray(ndim, cstr, stack);
			stack++;
			pc += 4;
			break;
			}

		case OP_ifnull:
			if (stack[-1].obj == 0)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack--;

			break;
		case OP_ifnonnull:
			if (stack[-1].obj != 0)
				pc += getInt16(&pc[1]);
			else
				pc += 3;
			stack--;
			break;
		case OP_goto_w:
			pc += getInt32(&pc[1]);
			break;

		case OP_jsr_w:
			stack[0].pc = pc + 5;
			pc += getInt32(&pc[1]);

			stack++;
			break;
		case OP_monitorenter: // unsupported
			/*
			{
				WObject monitor = stack[-1].obj;
				int wait = tryMonitorEnter(monitor,context);
				stack--;
				pc++;
				if (thrownException != 0) goto throw_exception;
				if (wait == 0) break;
				//if context was NULL, then an exception would have been thrown in tryMonitorEnter.
				context->waitingOnMonitor = monitor;
				context->monitorHoldCount = 1;
				context->monitorWaitCount = wait;
				context->sleepFor = 0;
				waitOnMonitor(context);
				exitContext = 1;
				goto context_return;
			}
			*/
			stack--;
			pc++;
			break;
		case OP_monitorexit: // unsupported
			/*
			{
				WObject monitor = stack[-1].obj;
				stack--;
				pc++;
				monitorExit(monitor,context);
			}
			*/
			stack--;
			pc++;
			break;
//////////////////////////////////////////////////////
// guich@200 - added support of double types natively
        case OP_dconst_0:
            double2vars(0.0,stack);
            stack+=2;
            pc++;
            break;
        case OP_dconst_1:
            double2vars(1.0,stack);
            stack+=2;
            pc++;
            break;
        case OP_dload:
        case OP_lload:
            vptr = &var[pc[1]];
            copy64(vptr,stack);
            stack+=2;
            pc += 2;
            break;
        case OP_dload_0:
        case OP_dload_1:
        case OP_dload_2:
        case OP_dload_3:
            vptr = &var[*pc - OP_dload_0];
            copy64(vptr,stack);
            stack+=2;
            pc++;
            break;
        case OP_daload:
        case OP_laload:
            {
            WObject obj;
            int32 i;
            Var *objPtr;

            stack -= 2;
            obj = stack[0].obj;
            if (obj == 0) goto null_array_error;
            i = stack[1].intValue;
            objPtr = objectPtr(obj);
            if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
            vptr = (WOBJ_arrayStartP(objPtr))+(i*2);
            copy64(vptr,stack);
            stack += 2;
            pc++;
            break;
            }
        case OP_dastore:
        case OP_lastore:
            {
            WObject obj;
            int32 i;
            Var *objPtr;

            stack -= 4;
            obj = stack[0].obj;
            if (obj == 0) goto null_array_error;
            i = stack[1].intValue;
            objPtr = objectPtr(obj);
            if (i < 0 || i >= WOBJ_arrayLenP(objPtr)) goto index_range_error;
            vptr = (WOBJ_arrayStartP(objPtr))+(i*2);
            copy64(stack+2,vptr);

            pc++;
            break;
            }
        case OP_dstore:
        case OP_lstore:
            vptr = &var[pc[1]];
            stack-=2;
            copy64(stack,vptr);
            pc += 2;
            break;
        case OP_dstore_0:
        case OP_dstore_1:
        case OP_dstore_2:
        case OP_dstore_3:
            vptr = &var[*pc - OP_dstore_0];
            stack -= 2;
            copy64(stack,vptr);
            pc++;
            break;
        case OP_dadd:
            stack -= 4;
            d1 = vars2double(stack+2);
            d2 = vars2double(stack);
            d = d1 + d2;
            double2vars(d,stack);
            stack += 2;
            pc++;
            break;
        case OP_dsub:
            stack -= 4;
            d1 = vars2double(stack+2);
            d2 = vars2double(stack);
            d = d2 - d1;
            double2vars(d,stack);
            stack += 2;
            pc++;
            break;
        case OP_dmul:
            stack -= 4;

            d1 = vars2double(stack+2);
            d2 = vars2double(stack);
            d = d2 * d1;
            double2vars(d,stack);
            stack += 2;
            pc++;
            break;
        case OP_ddiv:
            stack -= 4;
            d1 = vars2double(stack+2);
            d2 = vars2double(stack);
            if (d1 == 0)//FlpIsZero(d1))

                goto div_by_zero_error;
            d = d2 / d1;
            double2vars(d,stack);
            stack += 2;

            pc++;
            break;
        case OP_drem:

            stack -= 4;

            d1 = vars2double(stack+2);
            d2 = vars2double(stack);
            if (d1 == 0)//FlpIsZero(d1))
                goto div_by_zero_error;
            double2vars(fmod(d2,d1),stack);
            stack += 2;
            pc++;
            break;
        case OP_dneg:
            stack -= 2;
            d = vars2double(stack);
            //if (!FlpIsZero(d))
               d = -d;
            double2vars(d,stack);
            stack += 2;
            pc++;
            break;

        case OP_i2d:

            stack--;
            double2vars(d1 = (double)stack[0].intValue,stack);
            stack += 2;
            pc++;

            break;
        case OP_f2d:
            stack--;
            double2vars(stack[0].floatValue,stack);
            stack+=2;
            pc++;
            break;
        case OP_d2i:
            stack-=2;
            d1 = vars2double(stack);
            stack[0].intValue = (int32)d1;//_d_dtoi(fcd.fd);
            stack++;
            pc++;
            break;
        case OP_d2f:
            stack-=2;
            d1 = vars2double(stack);
            stack[0].floatValue = (float)d1;//_d_dtof(fcd.fd);
            stack++;
            pc++;
            break;
        case OP_dcmpl:
        case OP_dcmpg:
            // NOTE: NaN values are currently not supported - NaN in either
            // value should return 1 or 0 depending on the opcode
            stack-=4;
            d1 = vars2double(stack);
            d2 = vars2double(stack+2);
			if (d1 > d2) stack[0].intValue = 1;
			else if (d1 < d2) stack[0].intValue = -1;
			else stack[0].intValue = 0;
			/*
            switch (_d_cmp(fcd.fd,fcd2.fd))

            {
               case flpGreater: stack[0].intValue =  1; break;
               case flpLess   : stack[0].intValue = -1; break;
               default:         stack[0].intValue =  0; break;
            }
			*/
            stack++;
            pc++;
            break;
        case OP_dreturn:
        case OP_lreturn:
            returnedValue = stack[-2];
            methodReturnHigh = stack[-1];
            pushReturnedValue = 2;
            goto methodreturn;
        case OP_ldc2_w:
			CONS_wide(wclass,  getUInt16(&pc[1]), stack, stack+1);
            stack+=2;
            pc += 3;
            break;

        case OP_lconst_0:
            ll = 0;
            int642vars(ll,stack);
            stack+=2;
            pc++;
            break;
        case OP_lconst_1:
            ll = 1;
            int642vars(ll,stack);
            stack+=2;
            pc++;
            break;
        case OP_lload_0:
        case OP_lload_1:
        case OP_lload_2:
        case OP_lload_3:
            vptr = &var[*pc - OP_lload_0];
            copy64(vptr,stack);
            stack+=2;
            pc++;
            break;


        case OP_lstore_0:
        case OP_lstore_1:
        case OP_lstore_2:
        case OP_lstore_3:
            vptr = &var[*pc - OP_lstore_0];
            stack -= 2;

            copy64(stack,vptr);
            pc++;
            break;
        case OP_ladd:
            stack -= 4;

            ll1 = vars2int64(stack+2);
            ll2 = vars2int64(stack);
            ll = ll1 + ll2;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_lsub:
            stack -= 4;
            ll1 = vars2int64(stack+2);
            ll2 = vars2int64(stack);
	         ll = ll2 - ll1;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;

        case OP_lmul:

            stack -= 4;
            ll1 = vars2int64(stack+2);
            ll2 = vars2int64(stack);
	         ll = ll1 * ll2;
            int642vars(ll,stack);
            stack += 2;

            pc++;
            break;
        case OP_ldiv:

            stack -= 4;
            ll1 = vars2int64(stack+2);
            ll2 = vars2int64(stack);
            if (ll1 == 0) //LLisZero(ll1))

                goto div_by_zero_error;
	        ll = ll2 / ll1;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;

        case OP_lrem:
            stack -= 4;
            ll1 = vars2int64(stack+2);
            ll2 = vars2int64(stack);

            if (ll1 == 0) //LLisZero(ll1))
                goto div_by_zero_error;
	         ll = ll2 % ll1;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_lneg:
            stack -= 2;
            ll1 = vars2int64(stack);
            //if (!LLisZero(ll1))
               ll = - ll1;
            int642vars(ll,stack);
            stack += 2;
            pc++;

            break;
        case OP_lshl:
            stack -= 3;
            ll1 = vars2int64(stack);
            ll = ll1 << stack[2].intValue;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_lshr:
            stack -= 3;
            ll1 = vars2int64(stack);
            ll = ll1 >> stack[2].intValue;

            //__rt_shrs64(&ll,ll1,stack[2].intValue);
            int642vars(ll,stack);
            stack += 2;
            pc++;


            break;
        case OP_lushr:
            stack -= 3;
            ll = ll1 = vars2int64(stack);
			{
				int toShift = stack[2].intValue;
				if (toShift > 0){
					int had = (ll1 & cINT64(0x8000000000000000L)) != 0;
					ll = ll1 >> stack[2].intValue;
					if (had){
						int64 mask = ((int64)cINT64(0x8000000000000000L)) >> (toShift-1);

						ll &= ~mask;
					}
				}
			}
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_land:
            stack -= 4;
            ll1 = vars2int64(stack);
            ll2 = vars2int64(stack+2);
            ll = ll1 & ll2;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_lor:
            stack -= 4;
            ll1 = vars2int64(stack);
            ll2 = vars2int64(stack+2);
            ll = ll1 | ll2;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_lxor:
            stack -= 4;
            ll1 = vars2int64(stack);
            ll2 = vars2int64(stack+2);
            ll = ll1 ^ ll2;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_i2l:
            stack -= 1;
            ll = (int64)stack[0].intValue;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_l2i:
            stack -= 2;
            ll = vars2int64(stack);
            stack[0].intValue = (int32)ll;
            stack++;
            pc++;
            break;
        case OP_l2f:
            stack -= 2;
            ll = vars2int64(stack);
            stack[0].floatValue = (float32)ll;
            stack++;
            pc++;
            break;
        case OP_f2l:
            stack -= 1;
            ll = (int64)stack[0].floatValue;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_l2d:
            stack -= 2;
            ll = vars2int64(stack);
			double2vars((double)ll,stack);

            //fcd.d = (double)ll;
            //double2vars(fcd.d,stack);
            stack+=2;
            pc++;
            break;
        case OP_d2l:
            stack -= 2;
			ll = (int64)vars2double(stack);
            //fcd.d = vars2double(stack);
            //ll = (int64)fcd.d;
            int642vars(ll,stack);
            stack += 2;
            pc++;
            break;
        case OP_lcmp:
            stack-=4;
            ll1 = vars2int64(stack);
            ll2 = vars2int64(stack+2);
			if (ll1 > ll2) stack[0].intValue = 1;
			else if (ll1 == ll2) stack[0].intValue = 0;
			else stack[0].intValue = -1;
            //stack[0].intValue = __rt_cmps64(ll1,ll2);
            stack++;
            pc++;
            break;

		// NOTE: this is the full list of unsupported opcodes. Adding all
		// these cases here does not cause the VM executable code to be any
		// larger, it just makes sure that the compiler uses a jump table
		// with no spaces in it to make sure performance is as good as we
		// can get (tested under Codewarrior for PalmOS).
/*
		case OP_lconst_0:
		case OP_lconst_1:
		case OP_dconst_0:
		case OP_dconst_1:
		case OP_ldc2_w:
		case OP_lload:

		case OP_dload:
		case OP_lload_0:
		case OP_lload_1:
		case OP_lload_2:
		case OP_lload_3:
		case OP_dload_0:
		case OP_dload_1:
		case OP_dload_2:

		case OP_dload_3:
		case OP_laload:
		case OP_daload:
		case OP_lstore:
		case OP_dstore:
		case OP_lstore_0:
		case OP_lstore_1:
		case OP_lstore_2:
		case OP_lstore_3:
		case OP_dstore_0:
		case OP_dstore_1:
		case OP_dstore_2:
		case OP_dstore_3:
		case OP_lastore:
		case OP_dastore:
		case OP_ladd:
		case OP_dadd:
		case OP_lsub:
		case OP_dsub:
		case OP_lmul:
		case OP_dmul:
		case OP_ldiv:
		case OP_ddiv:

		case OP_lrem:

		case OP_drem:
		case OP_lneg:
		case OP_dneg:
		case OP_lshl:
		case OP_lshr:
		case OP_lushr:
		case OP_land:
		case OP_lor:
		case OP_lxor:
		case OP_i2l:
		case OP_i2d:
		case OP_l2i:

		case OP_l2f:
		case OP_l2d:
		case OP_f2l:
		case OP_f2d:
		case OP_d2i:
		case OP_d2l:
		case OP_d2f:
		case OP_lcmp:
		case OP_dcmpl:
		case OP_dcmpg:
		case OP_lreturn:
		case OP_dreturn:
*/
		case OP_athrow:
			if (stack[-1].obj == 0){
				stack--;
				throwException("java/lang/NullPointerException",NULL);//,&stack);
			}
			thrownException = stack[-1].obj;
throw_exception:
			stack = var;
			stack += METH_maxLocals(method);
			stack++;
			stack[-1].obj = thrownException;
			{
			Var *s = stack;
			for(s = stack; s <= vmStack+vmStackPtr-3; s++)
				(*s).obj = 0;
			}
			goto handle_exception;

		default:
			VmQuickError(ERR_BadOpcode);
			goto error;
		}
	goto step;

handle_exception:
	{
		WObject wasThrown = handlingException = thrownException;
		uint32 where = oldpc-METH_code(method);
		uint32 i, nh = METH_numHandlers(method);
		pushReturnedValue = method->returnsValue;
		thrownException = 0;
		/*
		if (nh != 0){
			UtfString className = WCLASS_className(wclass);
			UtfString methodName = getUtfString(wclass,method->nameIndex);
		}*/

		for (i = 0; i <nh ; i++){
			WExceptionHandler * eh = METH_handlers(method)+i;
			if (eh->startPc <= where && eh->endPc >= where){
				if (eh->catchType != 0){
					WClass *iclass = getClassByIndex(wclass, eh->catchType);
					if (iclass == NULL) continue;
					if (!compatible(WOBJ_class(wasThrown),iclass)) continue;
				}
				pc = METH_code(method)+eh->handlerPc;
				goto step;

			}
		}
		thrownException = wasThrown;
		handlingException = 0;
		goto methodreturn;
	}




stack_overflow_error:
		throwException("java/lang/StackOverflowError",NULL);

		goto throw_exception;
		//VmQuickError(ERR_StackOverflow);
		//goto error;
null_obj_error:
null_array_error:
		throwExceptionError(ERR_NullObjectAccess);//,&stack);
		goto throw_exception;
div_by_zero_error:
		throwExceptionError(ERR_DivideByZero);
		goto throw_exception;
index_range_error:
		throwExceptionError(ERR_IndexOutOfRange);//,&stack);
		goto throw_exception;
error:
		vmStackPtr = baseFramePtr;

return_now:
		if (currentContext == NULL)
			mainVmStackPtr = vmStackPtr;

		currentContext = saved;
		if (currentContext == NULL){
			vmStack = mainVmStack;
			vmStackPtr = mainVmStackPtr;
			vmStackSize = mainVmStackSize;
		}else{
			vmStack = currentContext->vmStack;
			vmStackPtr = currentContext->vmStackPtr;
			vmStackSize = currentContext->vmStackSize;
		}
		return;
#ifdef DONT_UNWIND_ON_ERROR
		{
		if (method->returnsValue)
				{
				returnedValue.obj = 0;
				pushReturnedValue = 1;
				}
			else
				pushReturnedValue = 0;
		goto methodreturn;
		}
#endif

methodreturn:
	handlingException = 0;
	// pop frame and restore state
	if ((METH_accessFlags(method) & ACCESS_NATIVE) > 0)

		{
// MLB Bug Fix - used to be:
//		vmStackPtr -= 2;
//		vmStackPtr -= vmStack[--vmStackPtr].intValue;
// But this caused an error in the SH3 compiler. It did not subtract the extra 1 as
// was probably intended. In anycase it is bad coding.
		vmStackPtr -= 3;
		vmStackPtr -= vmStack[vmStackPtr].intValue; //Bug Fix
		}
	else
		vmStackPtr -= METH_maxLocals(method) + METH_maxStack(method) + 2;
	if (vmStackPtr == baseFramePtr + STACKSAVESIZE)
		{
		// fully completed execution
		vmStackPtr = baseFramePtr;
		if (retValue != NULL) *retValue = returnedValue;
		if (context != NULL) context->fullyReturned = 1;
		goto return_now;

		}
// MLB modifed:

	if (vmStack[vmStackPtr-1].stackOffset & (MethodInvokeFlag|ConstructorNewInstanceFlag)){
		int isMethodInvoke = vmStack[vmStackPtr-1].intValue & MethodInvokeFlag;
		vmStackPtr -= STACKSAVESIZE; //Bypass stack, var, pc, oldpc.
		//Now vmStackPtr is pointing to nparams of the stackframe of the MethodInvoke native call.
		if (isMethodInvoke) {//Need to convert to wrapper.
			Var toWrapper[2];
			WObject destWrapper = vmStack[vmStackPtr-4].obj;
			toWrapper[0] = returnedValue;
			toWrapper[1] = methodReturnHigh;
			if (pushReturnedValue) VarToWrapper(destWrapper,toWrapper);
			returnedValue.obj = destWrapper;

		}else{//Was constructor - so need to pop the newly created object.
			returnedValue.obj = popObject();
		}
		methodReturnHigh.intValue = 0;
		pushReturnedValue = 1;
		wclass = (WClass *)vmStack[vmStackPtr - 1].refValue;
		method = (WClassMethod *)vmStack[vmStackPtr - 2].refValue;
		goto methodreturn;
	}else
		stack = vmStack+(vmStack[--vmStackPtr].stackOffset & OffsetMask);
	//sprintf(sprintBuffer,"<< %x %x",stack,vmStack+(vmStackPtr-3)); debugString(sprintBuffer);
//	stack =  (Var *)vmStack[--vmStackPtr].refValue;
	if (pushReturnedValue != 0){
		stack[0] = returnedValue;
		stack++;
		if (pushReturnedValue == 2){
			stack[0] = methodReturnHigh;
			stack++;
		}
	}

	/* This is incorrect - we should not push the exception at this point.
	if (pushReturnedValue || thrownException)
		{
		//BE: stack[0] = returnedValue;
		if (thrownException != 0) stack[0].obj = thrownException;
		else stack[0] = returnedValue;
		stack++;
		//BE: if (pushReturnedValue == 2){
		if (pushReturnedValue == 2 && !thrownException){
			stack[0] = methodReturnHigh;
			stack++;
		}
		}
	*/

// MLB modifed:
	var = vmStack+(vmStack[--vmStackPtr].stackOffset & OffsetMask);
//	var = (Var *)vmStack[--vmStackPtr].refValue;
	pc = vmStack[--vmStackPtr].pc;
#ifdef OLDPC
	oldpc = vmStack[--vmStackPtr].oldpc;
#endif
	memset(stack,0,((int)(&vmStack[vmStackPtr-2])-(int)stack));

	wclass = (WClass *)vmStack[vmStackPtr - 1].refValue;
	method = (WClassMethod *)vmStack[vmStackPtr - 2].refValue;


//context_return:
	if (exitContext && context != 0) {//Return from context.
		exitContext = 0;
		context->baseFramePtr = baseFramePtr;

		context->var = var;
		context->pc = pc;
		context->oldpc = oldpc;
		context->wclass = wclass;
		context->method = method;
		context->vmStack = vmStack;
		context->vmStackPtr = vmStackPtr;
		context->vmStackSize = vmStackSize;
		context->fullyReturned = 0;
		context->stack = stack;
		goto return_now;
	}
	goto step;

	}

#ifdef WINCE
#define REPORT_TO_MESSAGEBOX
#else
//#define REPORT_TO_MESSAGEBOX
#endif

static int reportException()
{
	static WClass *eventDirectionException = NULL;
	if (thrownException != 0){
		WClass *ec = WOBJ_class(thrownException);
		if (eventDirectionException == NULL)
			eventDirectionException = getClass(createUtfString("ewe/sys/EventDirectionException"));
		if (compatible(ec,eventDirectionException)){
			thrownException = 0;
			return 1;
		}
		if (ec != NULL){
			UtfString className = getUtfString(ec, ec->classNameIndex);
			UtfString message;
			char *all = NULL;
			message.str = NULL;
			message.len = 0;
			if (objectPtr(thrownException)[1].obj != 0){
				message = stringToUtf(objectPtr(thrownException)[1].obj,STU_NULL_TERMINATE);
			}
			all = (char *)mMalloc(className.len+2+message.len+1);
			strncpy(all,className.str,className.len);
#ifdef REPORT_TO_MESSAGEBOX
			all[className.len] = '\r';
			all[className.len+1] = '\n';
#else
			all[className.len] = ':';

			all[className.len+1] = ' ';
#endif
			if (message.len != 0) {
				strcpy(all+className.len+2,message.str);
				free(message.str);

			}
			else all[className.len+2] = 0;
#ifndef REPORT_TO_MESSAGEBOX
			debugString(all);
			free(all);
			if (objectPtr(thrownException)[2].obj != 0){
				int j;
				char *m,*e;
				message = stringToUtf(objectPtr(thrownException)[2].obj,STU_NULL_TERMINATE);

				for (m = message.str, e = message.str+message.len, j = 0; m<e; m++){
					if (*m == '\n') {
						if (++j == 19 && e-m > 12){
							strcpy(m+1,"\t(more)...");
							break;
						}
					}
				}
				if (message.len > 1)
					debugString(message.str+1); //Ignore the first '\n'
				free(message.str);
			}

#else
#ifdef USE_LOG
			if (objectPtr(thrownException)[2].obj != 0){
				FILE *f = fopen("\\EweException.txt","wt");
				fprintf(f,"%s\n",all);
				message = stringToUtf(objectPtr(thrownException)[2].obj,STU_NULL_TERMINATE);

				fprintf(f,"%s\n",message.str);
				free(message.str);
				fclose(f);
			}
#endif
			if (TRUE){
				char *extra = "\r\n\r\nStop the application?";
				WCHAR *toDisplay;
				int ret;
				char *get = mMalloc(strlen(all)+strlen(extra)+1);
				sprintf(get,"%s%s",all,extra);
				toDisplay = asciiToTempUnicode(get);
				thrownException = 0;
				ret = mMessageBox(NULL,toDisplay,L"Exception Occured",MB_SETFOREGROUND|MB_YESNO) == IDNO;
				free(get);
				return ret;
			}
#endif
		}

		thrownException = 0;
	}
	return 1;
}
static void executeTopMethod(WClass *wclass, WClassMethod *method,
						  Var params[], uint32 numParams)

{
	fullExecuteMethod(wclass,method,params,numParams,NULL,NULL);
	if (!reportException()) exitSystem(-1);
}
static void executeMethod(WClass *wclass, WClassMethod *method,
						  Var params[], uint32 numParams)
{
	fullExecuteMethod(wclass,method,params,numParams,NULL,NULL);
}
static void executeMethodRet(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams, Var *retValue)
{
	fullExecuteMethod(wclass,method,params,numParams,retValue,NULL);
}
static void executeMethodRet2(WClass *wclass, WClassMethod *method,
	Var params[], uint32 numParams, Var *retValue, Var *retValueHigh)
{
	fullExecuteMethod(wclass,method,params,numParams,retValue,NULL);
	if (retValueHigh != NULL) *retValueHigh = methodReturnHigh;

}

typedef int (*BooleanFunc)(uint32 classOrObject,JValue *parameters);
typedef jbyte (*ByteFunc)(uint32 classOrObject,JValue *parameters);
typedef int16 (*ShortFunc)(uint32 classOrObject,JValue *parameters);
typedef uint16 (*CharFunc)(uint32 classOrObject,JValue *parameters);
typedef int (*IntFunc)(uint32 classOrObject,JValue *parameters);
typedef int64 (*LongFunc)(uint32 classOrObject,JValue *parameters);
typedef float (*FloatFunc)(uint32 classOrObject,JValue *parameters);
typedef double (*DoubleFunc)(uint32 classOrObject,JValue *parameters);
typedef uint32 (*ObjectFunc)(uint32 classOrObject,JValue *parameters);
typedef void (*VoidFunc)(uint32 classOrObject,JValue *parameters);


static Var executeJValueNative(WClassMethod *method,Var params[])
{
	Var v;
	static JValue jpars[32];
	uint32 classOrObject, i;
	NativeFunc nf = *METH_nativeFuncPointer(method);
	JValue *p = varToJValue(method,params,jpars,&classOrObject);

	UtfString desc = getUtfString(METH_class(method),METH_descIndex(method));
	for (i = 0; i<desc.len && desc.str[i] != ')'; i++)

		;
	i++;
	switch(desc.str[i]){
	case 'Z': v.intValue = ((BooleanFunc)nf)(classOrObject,jpars); break;

	case 'B': v.intValue = ((ByteFunc)nf)(classOrObject,jpars); break;
	case 'C': v.intValue = ((CharFunc)nf)(classOrObject,jpars); break;
	case 'S': v.intValue = ((ShortFunc)nf)(classOrObject,jpars); break;
	case 'I': v.intValue = ((IntFunc)nf)(classOrObject,jpars); break;
	case 'J': v = returnLong(((LongFunc)nf)(classOrObject,jpars)); break;
	case 'F': v.floatValue = ((FloatFunc)nf)(classOrObject,jpars); break;

	case 'D': v = returnDouble(((DoubleFunc)nf)(classOrObject,jpars)); break;
	case '[':
	case 'L': v.obj = ((ObjectFunc)nf)(classOrObject,jpars); break;
	case 'V':
	default:
		v.obj = 0; ((VoidFunc)nf)(classOrObject,jpars); break;
	}
	return v;
}


// MLB
// Reflection methods.
WClass *reflectClass;

static void checkReflectClass()
{
	if (reflectClass != NULL) return;
	reflectClass = getClass(createUtfString("ewe/reflect/Reflect"));
}
static WObject getStringNameFor(WClass *cl,WObject forWho,int pureType)
{

	WObject ret;
	if (cl == NULL && forWho != 0){
		char c = arrayChar(WOBJ_arrayType(forWho));
		if (c != 'L'){
			uint16 *chars;
			if (c == '[' && WOBJ_arrayComponent(forWho) != NULL){
				unsigned int i = 0;
				UtfString name = arrayTypeName(forWho);
				ret = createNewString(name.len+1,&chars,NULL);
				chars[0] ='[';
				for (i = 0; i<name.len; i++) chars[1+i] = name.str[i];
			}else{
				if (ret = createNewString(2,&chars,NULL)){
					chars[0] = '[';
					chars[1] = c;
				}
			}
		}else{

			UtfString component;
			uint16 *chars;
			unsigned int i;
			WClass *cclass = (WClass *)WOBJ_arrayComponent(forWho);
			if (cclass == NULL) component = createUtfString("java/lang/Object");
			else component = getUtfString(cclass, cclass->classNameIndex);
			if (ret = createNewString(component.len+3,&chars,NULL)){
				chars[0] = '['; chars[1] = 'L'; chars[component.len+3-1] = ';';
				for (i = 0; i<component.len; i++) chars[2+i] = component.str[i];
			}
		}
	}else if (cl != NULL){
		UtfString cn = getUtfString(cl, cl->classNameIndex);
		if (pureType){
			expandSpaceFor(&tempString,cn.len+2,10,0);
			strncpy(tempString.data+1,cn.str,cn.len);
			tempString.data[0] = 'L';
			tempString.data[cn.len+1] = ';';
			cn.str = tempString.data;
			cn.len += 2;
		}
		ret = createStringFromUtf(cn);
	}
	return ret;
}
static Var returnClass(WClass *cl)
{
	Var v;
	v.obj = 0;
	if (cl == NULL) return v; //Must always return nothing if cl is NULL;

	v.obj = createObject(getClass(createUtfString("java/lang/Class")));
	if (v.obj != 0){
		WObject sn;
		objectPtr(v.obj)[1].classRef = cl;
		pushObject(v.obj);
		sn = getStringNameFor(cl,0,TRUE);
		objectPtr(v.obj)[2].obj = sn;
		popObject();
		objectPtr(v.obj)[3].obj = WCLASS_loader(cl);

	}
	return v;
}
static int setupCreatedClass(WObject reflect,WClass *cl,WObject forWho)
{
	WObject jlcl,gsn;

	if (reflect == 0) return 0;
	pushObject(reflect);
	objectPtr(reflect)[1].refValue = cl;
	gsn = getStringNameFor(cl,forWho,FALSE);
	objectPtr(reflect)[2].obj = gsn;
	jlcl = returnClass(cl).obj;
	objectPtr(reflect)[3].obj = jlcl;
	popObject();
	return 1;
}
static int setupClass(Var *v,WClass *cl,WObject forWho)
{
	checkReflectClass();
	v->obj = createObject(reflectClass);
	return setupCreatedClass(v->obj,cl,forWho);
}
static Var ReflectIsArray(Var stack[])
{
	Var v;
	v.intValue = 0;
	if (stack[0].obj == 0) return v;
	if (WOBJ_class(stack[0].obj) != NULL) return v;

	v.intValue = 1;
	return v;
}
static WClass *classNameToClass(WObject str)
{
	UtfString cn = unDot(stringToUtf8(str,&className,0));
	if (cn.len >= 2)
		if (cn.str[cn.len-1] == ';') {
			cn.str[cn.len-1] = 0;
			cn.str++;
			cn.len -= 2;
		}
	if (cn.len < 2) return NULL;
	return tryGetClass(cn);
}


static Var ReflectNewArrayInstance(Var stack[])
{
	Var v;
	int type = 0;//arrayType((char)(stack[0].intValue));
	WObject spec = stack[0].obj;

	UtfString ty;
	int len = stack[1].intValue;
	int at;

	v.obj = 0;

	if (len < 0 || spec == 0) return v;
	ty = stringToTempUtf8(spec);
	if (ty.len <= 0) return v;
	at = arrayType((char)ty.str[0]);
	if (at == ARRAYTYPE_OBJECT) {//Object
		WClass *got = classNameToClass(spec);
		if (got == NULL) return v;
		v.obj = createArrayObjectClass(got,len);
	}else if (at == ARRAYTYPE_ARRAY){
		char *ct = newArrayType(ty);
		if (ct == NULL) return v;
		v.obj = createArrayObject(at,len);
		if (v.obj != 0)
			WOBJ_arrayComponent(v.obj) = ct;
	}else{
		v.obj = createArrayObject(at,len);
	}
	return v;
}

static Var ReflectArrayLength(Var stack[])
{
	Var v;
	WObject ar = stack[0].obj;
	v.intValue = -1;

	if (ar == 0) return v;
	if (WOBJ_class(ar) != NULL) return v;
	v.intValue = WOBJ_arrayLen(ar);
	return v;
}

#define T_VOID  'V'
#define T_BYTE  'B'
#define T_CHAR  'C'
#define T_SHORT  'S'
#define T_INT  'I'
#define T_BOOLEAN  'Z'
#define T_FLOAT  'F'
#define T_LONG  'J'
#define T_DOUBLE  'D'

#define T_OBJECT  'L'
#define T_ARRAY '['
#define T_VMREFERENCE  '?'


void wrapperTransfer(WObject wrapper,void *where,int isGet)
{
	Var *w = objectPtr(wrapper);
	if (isGet){
		switch(w[1].intValue){
			case T_BYTE: case T_BOOLEAN: w[2].intValue = (int)*((signed char *)where); break;
			case T_SHORT: case T_CHAR: w[2].intValue = (int)*((int16 *)where); break;
			case T_INT: w[2].intValue = (int)*((int *)where); break;
			case T_FLOAT: w[3].floatValue = (float)*((float *)where); break;
			case T_LONG: int642vars((int64)*((int64 *)where),w+6); break;
			case T_DOUBLE: double2vars((double)*((double *)where),w+8); break;
			default: w[4].obj = (WObject)*((WObject *)where); break;
		}
	}else{
		switch(w[1].intValue){
			case T_BYTE: case T_BOOLEAN: *((signed char *)where) = (signed char)w[2].intValue; break;
			case T_SHORT: case T_CHAR: *((int16 *)where) = (int16)w[2].intValue; break;
			case T_INT: *((int *)where) = (int)w[2].intValue; break;
			case T_FLOAT: *((float *)where) = (float)w[3].floatValue; break;
			case T_LONG: *((int64 *)where) = vars2int64(w+6); break;
			case T_DOUBLE: *((double *)where) = vars2double(w+8); break;
			default: *((WObject *)where) = (WObject)w[4].obj; break;
		}
	}
}

static Var ArrayGetSetElement(Var stack[])
{
	Var v;
	WObject ar = stack[0].obj;
	int index = stack[1].intValue;
	WObject wr = stack[2].obj;

	unsigned char *where;


	if (ar == 0 || wr == 0) return returnException(NullPointerEx,NULL);
	if (WOBJ_class(ar) != NULL) return returnException("java/lang/IllegalArgumentException","Argument is not an array.");
	if (index < 0 || index >= WOBJ_arrayLen(ar)) return returnException(ArrayIndexEx,NULL);
	objectPtr(wr)[1].intValue = arrayChar(WOBJ_arrayType(ar));
	where = (unsigned char *)WOBJ_arrayStart(ar)+arrayTypeSize(WOBJ_arrayType(ar))*index;
	wrapperTransfer(wr,where,stack[3].intValue);

	v.intValue = -1;
	return v;
}
static WClass *getSuperclass(WClass *clazz)
{
	if (!clazz) return NULL;
	else{

		uint16 classIndex = WCLASS_superClass(clazz);
		if (!classIndex) return NULL;
		return getClassByIndex(clazz,classIndex);
	}
}
static Var ObjectGetClass(Var stack[])
{
	WObject who = stack[0].obj;
	WClass *cl = WOBJ_class(who);
	if (cl != NULL){
		return returnClass(cl);
	}else{
		Var v;
		v.obj = createObject(getClass(createUtfString("java/lang/Class")));
		if (v.obj != 0){
			WObject sn;
			objectPtr(v.obj)[1].classRef = cl;
			pushObject(v.obj);
			sn = getStringNameFor(cl,who,TRUE);
			objectPtr(v.obj)[2].obj = sn;
			popObject();
		}
		return v;
	}
}
static Var ReflectGetReflectedClass(Var stack[])
{
	return returnClass((WClass *)objectPtr(stack[0].obj)[1].classRef);

}
static Var ClassGetSuperClass(Var stack[])
{
	WClass *myClass = (WClass *)objectPtr(stack[0].obj)[1].classRef;
	if (myClass != NULL){
		uint16 classIndex = WCLASS_superClass(myClass);
		if (classIndex != 0)
			return returnClass(getClassByIndex(myClass, classIndex));
	}
	return returnClass(NULL);
}

static Var ClassIsInterface(Var stack[])
{
	Var v;
	WClass *myClass = (WClass *)objectPtr(stack[0].obj)[1].classRef;
	v.intValue = 0;
	if (myClass != NULL) v.intValue = WCLASS_isInterface(myClass);
	return v;
}
static Var checkClass(Var stack[],int isClassCheck)
{
	Var v;
	WClass *myClass = (WClass *)objectPtr(stack[0].obj)[1].classRef;
	WClass *otherClass;
	v.intValue = 0;
	if (stack[1].obj == 0)
		if (isClassCheck) return returnError(ERR_NullObjectAccess);
		else return v;
	otherClass = isClassCheck ? (WClass *)objectPtr(stack[1].obj)[1].classRef : WOBJ_class(stack[1].obj);
	if (myClass == NULL || otherClass == NULL) return v;
	v.intValue = compatible(otherClass,myClass);
	return v;
}
static Var ClassIsInstance(Var stack[])
{
	return checkClass(stack,0);
}
/*
static Var ClassIsAssignableFrom(Var stack[])
{
	return checkClass(stack,1);
}
*/
static char *ClassClassName = "Ljava/lang/Class;";


static int countInnerClasses(WClass *wclass,WObject *dest)
{
	if (wclass == NULL) return 0;
	else{
		uchar *innerAttr = getAttribute(wclass,wclass->classAttributes,"InnerClasses",12,NULL);
		if (innerAttr == NULL) return 0;
		else {
			uchar *p = innerAttr+2;
			uint16 num = getUInt16(innerAttr),n;
			int c = 0;
			for (n = 0; n<num; n++, p+= 8){
				uint16 classIndex = getUInt16(p), nameIndex = getUInt16(p+4), outerIndex = getUInt16(p+2);
				if (classIndex == 0 || nameIndex == 0 || outerIndex == 0) continue;
				else {
					WClass *inner = getClassByIndex(wclass,classIndex);
					WClass *outer = getClassByIndex(wclass,outerIndex);
					if (inner != 0 && outer == wclass){
						if (dest)
							dest[c] = returnClass(inner).obj;

						c++;
					}
				}

			}
			return c;
		}
	}
}
static Var ClassGetDeclaredClasses(Var stack[])
{

	Var v;
	WClass *wclass = (WClass *)objectPtr(stack[0].obj)[1].classRef;
	int num = countInnerClasses(wclass,NULL);
	v.obj = createArray(ClassClassName,num);
	if (v.obj && num){
		pushObject(v.obj);
		countInnerClasses(wclass,(WObject *)WOBJ_arrayStart(v.obj));
		popObject();
	}
	return v;
}
static Var ClassGetInterfaces(Var stack[])

{
	Var v;
	WClass *myClass = (WClass *)objectPtr(stack[0].obj)[1].classRef;
	int num,i;
	v.obj = 0;

	if (myClass == NULL) return v;
	num = WCLASS_numInterfaces(myClass);
	v.obj = createArray(ClassClassName,num);
	if (v.obj == 0) return v;
	for (i = 0; i < num; i++){
		uint16 classIndex;
		WClass *interfaceClass;
		Var c;
		classIndex = WCLASS_interfaceIndex(myClass, i);
		interfaceClass = getClassByIndex(myClass, classIndex);
		c = returnClass(interfaceClass);
		((WObject *)WOBJ_arrayStart(v.obj))[i] = c.obj;
	}
	return v;
}
static Var ClassGetClassLoader(Var stack[])
{
	Var v;
	WClass *cl = (WClass *)objectPtr(stack[0].obj)[1].classRef;
	v.obj = (cl == NULL) ? 0 : WCLASS_loader(cl);
	return v;
}
static Var ClassGetModifiers(Var stack[])
{
	Var v;
	WObject wo = stack[0].obj;
	WClass *cl = (WClass *)objectPtr(wo)[1].classRef;
	if (cl == NULL) v.intValue = ACCESS_PUBLIC|ACCESS_FINAL|ACCESS_ABSTRACT;
	else v.intValue = WCLASS_accessFlags(cl);
	return v;
}
static Var ClassGetName(Var stack[])
{
	Var v;
	WObject wo = stack[0].obj;
	WClass *cl = (WClass *)objectPtr(wo)[1].classRef;
	v.obj = 0;
	if (cl != NULL){
		v.obj = replaceCharacter(createStringFromUtf8(getUtfString(cl,cl->classNameIndex)),'/','.');
	}else {
	//v.obj = createStringFromUtf(createUtfString("An Array!"));
		UtfString t = stringToTempUtf8(objectPtr(wo)[2].obj);
		if (t.str[0] == '['){
			v.obj = replaceCharacter(createStringCopy(objectPtr(wo)[2].obj,NULL,NULL),'/','.');
		}else {
			char *r = "?";
			switch(t.str[0]){
			case 'Z': r = "boolean"; break;
			case 'B': r = "byte"; break;
			case 'C': r = "char"; break;
			case 'S': r = "short"; break;
			case 'I': r = "int"; break;
			case 'J': r = "long"; break;

			case 'F': r = "float"; break;
			case 'D': r = "double"; break;
			case 'V': r = "void"; break;
			}
			v.obj = createStringFromUtf(createUtfString(r));
		}
	}
	return v;
}
static Var ReflectGetForObject(Var stack[])
{
	Var v;
	WObject wo = stack[0].obj;
	v.obj = 0;
	if (wo != 0) setupClass(&v,WOBJ_class(wo),wo);
	return v;
}
static Var ReflectNativeCreate(Var stack[])
{
	Var v;

	WObject aclass = stack[1].obj;
	v.obj = 0;
	setupCreatedClass(stack[0].obj,(WClass *)objectPtr(aclass)[1].classRef,0);
	return v;
}

static Var ReflectGetForName(Var stack[])
{
	Var v;
	WObject nm = stack[0].obj;
	v.obj = 0;
	if (nm != 0){
		UtfString cn = stringToUtf8(nm,&lookForClass,0);
		if (cn.len <= 0) return v;
		if (cn.str[0] == '['){
			if (cn.len <= 1) return v;
			checkReflectClass();
			v.obj = createObject(reflectClass);
			if (v.obj == 0) return v;

			objectPtr(v.obj)[1].refValue = NULL;
			objectPtr(v.obj)[2].obj = nm;
		}else{
			WClass *got = classNameToClass(nm);
			if (got != NULL)
				if (WCLASS_loader(got) == 0)
					setupClass(&v,got,0);
		}
	}
	return v;
}
static Var ReflectGetSuperClass(Var stack[])
{
	Var v;
	WObject thisReflect = stack[0].obj;
	v.obj = 0;
	if (thisReflect != 0) {
		WClass *myClass = (WClass *)objectPtr(thisReflect)[1].refValue;
		if (myClass != NULL){
			uint16 classIndex = WCLASS_superClass(myClass);
			if (classIndex != 0){
				WClass *sc = getClassByIndex(myClass, classIndex);
				if (sc != NULL)
					v.obj = createStringFromUtf8(getUtfString(sc,sc->classNameIndex));
			}
		}
	}
	return v;
}
static Var ReflectGetNumberOfInterfaces(Var stack[])
{
	Var v;
	WObject thisReflect = stack[0].obj;
	v.obj = 0;
	if (thisReflect != 0) {
		WClass *myClass = (WClass *)objectPtr(thisReflect)[1].refValue;
		if (myClass != NULL)
			v.intValue = WCLASS_numInterfaces(myClass);
	}
	return v;
}
static Var ReflectGetInterface(Var stack[])
{
	Var v;

	WObject thisReflect = stack[0].obj;
	int idx = stack[1].intValue;
	v.obj = 0;
	if (thisReflect != 0) {

		WClass *myClass = (WClass *)objectPtr(thisReflect)[1].refValue;
		if (myClass != NULL){
				int num = WCLASS_numInterfaces(myClass);
				if (idx >= 0 && idx<num){
					uint16 intIndex = WCLASS_interfaceIndex(myClass,idx);
					WClass *sc = getClassByIndex(myClass, intIndex);
					if (sc != NULL)
						v.obj = createStringFromUtf8(getUtfString(sc,sc->classNameIndex));
				}
		}
	}
	return v;
}



#define DECLARED_ONLY 0x1

static WClass *fieldClass = NULL;
static WObject makeField(WObject made,WObject reflect,WClass *wclass,WClassField *field,WObject name)
{
	Var clazz;
	UtfString fdesc;
	if (made == 0) {
		if (fieldClass == NULL) fieldClass = getClass(createUtfString("ewe/reflect/Field"));
		made = createObject(fieldClass);
	}
	pushObject(made);
	objectPtr(made)[6].obj = reflect;
	if (name == 0)
		name = createStringFromUtf8(getUtfString(wclass, FIELD_nameIndex(field)));
	objectPtr(made)[7].obj = name;
	clazz = returnClass(wclass);
	objectPtr(made)[5] = clazz;
	fdesc = getUtfString(wclass, FIELD_descIndex(field));
	if (fdesc.len > 0){
		WObject type = createStringFromUtf8(fdesc);
		if (type != 0) {
			objectPtr(made)[1].refValue = field;
			objectPtr(made)[2].intValue = ((int)*fdesc.str)&0xff;
			objectPtr(made)[3].obj = type;
			objectPtr(made)[4].intValue = FIELD_accessFlags(field);
		}else

			made = 0;
	}else
		made = 0;
	popObject();
	return made;
}


static int countFields(WClass *wclass,int declaredOnly)
{
	WClass *cl = wclass;

	int num = 0;
	while (cl != NULL){
		uint16 i;
		if ((WCLASS_accessFlags(cl) & ACCESS_PUBLIC) || declaredOnly)
			for (i = 0; i < cl->numFields; i++)
				if ((FIELD_accessFlags((WCLASS_fieldPtr(cl,i))) & ACCESS_PUBLIC) || declaredOnly) num++;
		if (declaredOnly) return num;
		else{
			uint16 classIndex = WCLASS_superClass(cl);
			if (classIndex == 0) return num;
			cl = getClassByIndex(cl, classIndex);
		}
	}
	return num;
}

static Var ReflectGetFields(Var stack[])
{
	WObject thisReflect = stack[0].obj;
	WClass *cl = (WClass *)objectPtr(thisReflect)[1].refValue;
	int declaredOnly = stack[1].intValue & DECLARED_ONLY;
	int num = countFields(cl,declaredOnly),n = 0;
	WObject ret = createArray("Lewe/reflect/Field;",num);
	Var v;
	pushObject(ret);
	while (cl != NULL){
		uint16 i;
		if ((WCLASS_accessFlags(cl) & ACCESS_PUBLIC)|| declaredOnly)
			for (i = 0; i < cl->numFields; i++)
				if ((FIELD_accessFlags((WCLASS_fieldPtr(cl,i))) & ACCESS_PUBLIC) || declaredOnly){
					WObject fld = makeField(0,thisReflect,cl,WCLASS_fieldPtr(cl,i),0);
					((WObject *)WOBJ_arrayStart(ret))[n++] = fld;
				}
		if (declaredOnly) break;
		else{
			uint16 classIndex = WCLASS_superClass(cl);
			if (classIndex == 0) break;
			cl = getClassByIndex(cl, classIndex);
		}
	}
	v.obj = ret;
	popObject();
	return v;
}

static Var ReflectGetField(Var stack[])
{
	Var v;
	WObject thisReflect = stack[0].obj;
	WObject fName = stack[1].obj;
	int options = stack[2].intValue;

	WObject dest = stack[3].obj;
	WClass *cl = (WClass *)objectPtr(thisReflect)[1].refValue;
	UtfString utf = stringToTempUtf8(fName);

	WClass *found = cl;
	WClassField *cf = tryGetFieldAlone(cl,utf,(options & DECLARED_ONLY) ? NULL : &found);
	v.intValue = 0;
	if (cf != 0)
		if ((FIELD_accessFlags(cf) & WCLASS_accessFlags(found) & ACCESS_PUBLIC) || (options & DECLARED_ONLY)){
			if (makeField(dest,thisReflect,found,cf,fName))
				v.intValue = 1;
		}
	return v;
}

static WObject makeMethod(WObject made,WObject reflect,WClass *wclass,WClassMethod *method,WObject name,int isConstructor)
{
	Var clazz;
	UtfString fdesc;
	if (made == 0) {
		if (methodClass == NULL) methodClass = getClass(createUtfString("ewe/reflect/Method"));
		if (constructorClass == NULL) constructorClass = getClass(createUtfString("ewe/reflect/Constructor"));
		made = createObject(isConstructor ? constructorClass : methodClass);
	}
	pushObject(made);
	objectPtr(made)[7].obj = reflect;
	if (name == 0)
		name = createStringFromUtf8(getUtfString(wclass, METH_nameIndex(method)));
	objectPtr(made)[8].obj = name;
	clazz = returnClass(wclass);
	objectPtr(made)[6] = clazz;
	fdesc = getUtfString(wclass, METH_descIndex(method));
	if (fdesc.len > 0){
		WObject type = createStringFromUtf8(fdesc);
		if (type != 0) {
			int w;
			for (w = 0; fdesc.str[w] != ')'; w++);
			w++;
			objectPtr(made)[1].refValue = method;
			objectPtr(made)[2].refValue = wclass;
			objectPtr(made)[3].intValue = ((int)fdesc.str[w])&0xff;
			objectPtr(made)[4].obj = type;
			objectPtr(made)[5].intValue = METH_accessFlags(method);
		}else
			made = 0;
	}else
		made = 0;
	popObject();
	return made;
}

static int countMethods(WClass *wclass,int declaredOnly,int constructors)
{
	WClass *cl = wclass;
	int num = 0;
	while (cl != NULL){
		uint16 i;
		//if ((WCLASS_accessFlags(cl) & ACCESS_PUBLIC) || declaredOnly)
			for (i = 0; i < cl->numMethods; i++){
				WClassMethod *m = WCLASS_methodPtr(cl,i);
				if (constructors){
					if (m->isInit && !METH_isStatic((m)))
						if ((METH_accessFlags(m) & ACCESS_PUBLIC) || declaredOnly)
							num++;
				}else{
					if (!m->isInit)
						if ((METH_accessFlags(m) & ACCESS_PUBLIC) || declaredOnly)
							num++;
				}
			}
		if (declaredOnly || constructors) return num;
		else{
			uint16 classIndex = WCLASS_superClass(cl);
			if (classIndex == 0) return num;
			cl = getClassByIndex(cl, classIndex);
		}
	}
	return num;
}

static struct byte_data nameData, descData;
static Var getReflectMethod(WClass *cl,WObject thisReflect,WObject fName,WObject fDesc,WObject dest,int options,int isConstructor)
{
	Var v;
	int declared = options & DECLARED_ONLY;
	v.intValue = 0;
	if (fName == 0 || fDesc == 0) return v;
	else{
		WClass *found = cl;
		UtfString ds = stringToUtf8(fDesc,&descData,0);
		UtfString name = stringToUtf8(fName,&nameData,0);
		WClassMethod *cm = getMethod(cl,name,ds,(declared || isConstructor) ? NULL : &found);
		if (cm != 0)
			if (isConstructor){
				if (cm->isInit && ((METH_accessFlags(cm) & WCLASS_accessFlags(found) & ACCESS_PUBLIC) || declared))
					v.obj = makeMethod(dest,thisReflect,found,cm,fName,isConstructor);
			}else{
				if (!cm->isInit && ((METH_accessFlags(cm) & WCLASS_accessFlags(found) & ACCESS_PUBLIC) || declared))

					v.obj = makeMethod(dest,thisReflect,found,cm,fName,isConstructor);
			}

	}
	return v;


}
static Var ReflectGetMethodConstructor(Var stack[])
{
	WObject thisReflect = stack[0].obj;
	WObject fName = stack[1].obj;
	WObject fDesc = stack[2].obj;
	int options = stack[3].intValue;
	WObject dest = stack[4].obj;
	int isConstructor = stack[5].intValue;
	WClass *cl = (WClass *)objectPtr(thisReflect)[1].refValue;
	return getReflectMethod(cl,thisReflect,fName,fDesc,dest,options,isConstructor);
}

static Var ReflectGetMethodsOrConstructors(Var stack[])
{
	WObject thisReflect = stack[0].obj;
	WClass *cl = (WClass *)objectPtr(thisReflect)[1].refValue;
	int declaredOnly = stack[1].intValue & DECLARED_ONLY;
	int constructors = stack[2].intValue;
	int num = countMethods(cl,declaredOnly,constructors),n = 0;
	WObject ret = createArray((char *)(constructors ? "Lewe/reflect/Constructor;" : "Lewe/reflect/Method;"),num);
	Var v;

	pushObject(ret);
	while (cl != NULL){
		uint16 i;
		for (i = 0; i < cl->numMethods; i++){
				WClassMethod *m = WCLASS_methodPtr(cl,i);
		/*
		if ((WCLASS_accessFlags(cl) & ACCESS_PUBLIC)|| declaredOnly)
			for (i = 0; i < cl->numMethods; i++)
				if (!cl->methods[i].isInit)
					if ((METH_accessFlags((&cl->fields[i])) & ACCESS_PUBLIC) || declaredOnly){
						WObject meth = makeMethod(0,thisReflect,cl,&cl->methods[i],0,0);
						((WObject *)WOBJ_arrayStart(ret))[n++] = meth;
					}
		if (declaredOnly) break;
		*/
			if (constructors){
				if (m->isInit && !METH_isStatic((m)))
					if ((METH_accessFlags((m)) & ACCESS_PUBLIC) || declaredOnly) {
						WObject meth = makeMethod(0,thisReflect,cl,m,0,constructors);
						((WObject *)WOBJ_arrayStart(ret))[n++] = meth;
					}
			}else{

				if (!m->isInit)
					if ((METH_accessFlags((m)) & ACCESS_PUBLIC) || declaredOnly) {
						WObject meth = makeMethod(0,thisReflect,cl,m,0,constructors);
						((WObject *)WOBJ_arrayStart(ret))[n++] = meth;
					}
			}
		}
		if (declaredOnly || constructors) break;
		else{
			uint16 classIndex = WCLASS_superClass(cl);
			if (classIndex == 0) break;
			cl = getClassByIndex(cl, classIndex);
		}
	}
	v.obj = ret;
	popObject();
	return v;
}
/*
static Var ReflectGetConstructor(Var stack[])
{
	Var v;
	WObject thisReflect = stack[0].obj;
	WObject fDesc = stack[1].obj;
	int options = stack[2].intValue;
	WObject dest = stack[3].obj;
	WClass *cl = (WClass *)objectPtr(thisReflect)[1].refValue;
	v.intValue = 0;
	if (fDesc == 0 || dest == 0) return v;
	else{
		UtfString ds = stringToUtf8(fDesc,&descData,0);
		WClassMethod *cm = getMethod(cl,createUtfString("<init>"),ds,NULL);
		free(ds.str);
		if (cm != 0)
			if (METH_accessFlags(cm) & WCLASS_accessFlags(cl) & ACCESS_PUBLIC){
				unsigned i = 0;
				Var clazz;
				Var * d = objectPtr(dest);
				d[1].refValue = cm;
				d[2].refValue = cl;
				d[3].intValue = METH_accessFlags(cm);
				clazz = returnClass(cl);
				objectPtr(dest)[4] = clazz;
				v.intValue = 1;

			}

	}
	return v;
}
*/
static int WrapperToStaticVar(WObject wrapper,WClassField *cf)
{
	Var *w = objectPtr(wrapper);
	switch(w[1].intValue){
	case T_INT: case T_BOOLEAN: case T_BYTE: case T_CHAR: case T_SHORT:
		GetStaticVarPointer(cf)->intValue = w[2].intValue; break;
	case T_FLOAT:
		GetStaticVarPointer(cf)->floatValue = w[3].floatValue; break;
	case T_LONG:
		GetStaticVarPointer2(cf)->half64 = w[7].half64;
		GetStaticVarPointer(cf)->half64 = w[6].half64; return 2;

	case T_DOUBLE:

		GetStaticVarPointer2(cf)->half64 = w[9].half64;
		GetStaticVarPointer(cf)->half64 = w[8].half64; return 2;

	default:
		GetStaticVarPointer(cf)->obj = w[4].obj; break;
	}
	return 1;
}
static int WrapperToVar(WObject wrapper,Var *var)
{
	Var *w = objectPtr(wrapper);
	switch(w[1].intValue){
	case T_INT: case T_BOOLEAN: case T_BYTE: case T_CHAR: case T_SHORT:
		var->intValue = w[2].intValue; break;
	case T_FLOAT:
		var->floatValue = w[3].floatValue; break;
	case T_LONG:
		var->half64 = w[6].half64;
		(var+1)->half64 = w[7].half64; return 2;
	case T_DOUBLE:
		var->half64 = w[8].half64;
		(var+1)->half64 = w[9].half64; return 2;

	default:
		var->obj = w[4].obj; break;
	}
	return 1;
}

static int StaticVarToWrapper(WObject wrapper,WClassField *cf)
{
	Var *w = objectPtr(wrapper);

	switch(w[1].intValue){
	case T_BOOLEAN:
	case T_INT: case T_BYTE: case T_CHAR: case T_SHORT:
		w[2].intValue = GetStaticVarPointer(cf)->intValue; break;
	case T_FLOAT:
		w[3].floatValue = GetStaticVarPointer(cf)->floatValue; break;
	case T_LONG:
		w[7].half64 = GetStaticVarPointer2(cf)->half64;
		w[6].half64 = GetStaticVarPointer(cf)->half64; break;
	case T_DOUBLE:
		w[9].half64 = GetStaticVarPointer2(cf)->half64;
		w[8].half64 = GetStaticVarPointer(cf)->half64; break;
	default:
		w[4].obj = GetStaticVarPointer(cf)->obj; break;
	}
	return 1;
}
static int VarToWrapper(WObject wrapper,Var *var)
{
	Var *w = objectPtr(wrapper);
	switch(w[1].intValue){
	case T_BOOLEAN:
	case T_INT: case T_BYTE: case T_CHAR: case T_SHORT:
		w[2].intValue = var->intValue; break;
	case T_FLOAT:
		w[3].floatValue = var->floatValue; break;
	case T_LONG:
		w[6].half64 = var->half64;
		w[7].half64 = (var+1)->half64; return 2;
	case T_DOUBLE:
		w[8].half64 = var->half64;
		w[9].half64 = (var+1)->half64; return 2;
	default:
		w[4].obj = var->obj; break;
	}
	return 1;
}



static Var FieldGetValue(Var stack[])
{
	Var v;
	WObject thisField = stack[0].obj;
	WObject from = stack[1].obj;
	WObject dest = stack[2].obj;
	WClassField * cf = (WClassField *)objectPtr(thisField)[1].refValue;
	int type = objectPtr(thisField)[2].intValue;
	v.obj = 0;
	if (cf == 0 || dest == 0) return v;
	if (!FIELD_isPublic(cf)) return v;
	objectPtr(dest)[1].intValue = type;
	if (FIELD_isStatic(cf)) StaticVarToWrapper(dest,cf);
	else {
		if (from == 0) return v;
		VarToWrapper(dest,&WOBJ_var(from,GetVarOffset(cf)));
	}
	v.obj = dest;
	return v;
}
static Var FieldSetValue(Var stack[])
{
	Var v;
	WObject thisField = stack[0].obj;
	WObject from = stack[1].obj;
	WObject dest = stack[2].obj;
	WClassField * cf = (WClassField *)objectPtr(thisField)[1].refValue;
	int type = objectPtr(thisField)[2].intValue;
	v.obj = 0;
	if (cf == 0 || dest == 0) return v;
	if (!FIELD_isPublic(cf)) return v;
	objectPtr(dest)[1].intValue = type;
	if (FIELD_isStatic(cf)) WrapperToStaticVar(dest,cf);
	else {
		if (from == 0) return v;
		WrapperToVar(dest,&WOBJ_var(from,GetVarOffset(cf)));
	}
	v.obj = dest;
	return v;
}

static int numVarParameters(WObject wrapperArray)
{
	int len = WOBJ_arrayLen(wrapperArray);
	int num = 0;
	for (len--;len >= 0; len--){
		WObject wrap = WOBJ_arrayStart(wrapperArray)[len].obj;
		int type = objectPtr(wrap)[1].intValue;
		if (type == T_LONG || type == T_DOUBLE) num+=2;
		else num++;
	}
	return num;
}
static Var getMethodThrows(WClassMethod *cm)
{
	Var v;
	uchar *ex = METH_throws(cm);
	WObject ret;
	int size = 0;
	WClass *mcl = METH_class(cm);

	v.obj = 0;

	if (ex){

		size = getUInt16(ex);
		ex += 2;
	}
	ret = createArray("Ljava/lang/Class;",size);
	if (ret){
		int i;
		pushObject(ret);
		for (i = 0; i<size; i++, ex += 2){
			WClass *cl = getClassByIndex(mcl,getUInt16(ex));
			if (cl == NULL) {
				popObject();
				return v; //An exception will be thrown.
			}else{
				Var clazz = returnClass(cl);
				((WObject *)WOBJ_arrayStart(ret))[i] = clazz.obj;
			}
		}
		popObject();
		v.obj = ret;
	}
	return v;
}
static Var MethodConstructorGetThrows(Var stack[])
{
	WObject thisMethod = stack[0].obj;
	WClassMethod * cm = (WClassMethod *)objectPtr(thisMethod)[1].refValue;
	return getMethodThrows(cm);
}
/* Original
static Var MethodInvoke(Var stack[])
{
	Var v;
	WObject thisMethod = stack[0].obj;
	WObject target = stack[1].obj;
	WObject pars = stack[2].obj;
	WObject dest = stack[3].obj;
	WClassMethod * cm = (WClassMethod *)objectPtr(thisMethod)[1].refValue;
	WClass * cl = (WClass *)objectPtr(thisMethod)[2].refValue;
	int type = objectPtr(dest)[1].intValue;
	v.obj = 0;

	if (!METH_isPublic(cm)) return v;
	if ((target == 0  && !METH_isStatic(cm))|| pars == 0) return v;
	else{
		int numVars = numVarParameters(pars);
		int numPars = WOBJ_arrayLen(pars);
		int i = 0, off = 0;
		Var ret[2];
		WObject * wrappers = (WObject *)WOBJ_arrayStart(pars);
		Var *dp;
		Var *np = (Var *)mMalloc((numVars+1)*sizeof(Var));
		dp = np;
		np[0].obj = target;
		for (i = 0; i<numPars; i++)
			if (WrapperToVar(wrappers[i],&np[i+1+off]) == 2)
				off++;

		if (METH_isStatic(cm)){
			numVars--;
			dp++;
		}
		if (!METH_isStatic(cm)){
			if (dp[0].obj == 15){
				dp[0].obj = 0;
			}
		}
		executeMethodRet(cl,cm,dp,numVars+1,&ret[0]);
		ret[1] = methodReturnHigh;
		if (cm->returnsValue && dest != 0) VarToWrapper(dest,&ret[0]);
		xfree(np);
		v.obj = dest;
		return v;
	}
}
*/
static Var MethodInvoke(Var stack[])
{

	Var v;
	v.obj = 0;
	return v;
}

static BOOL getInheritedMethod(WObject target, WClass **cl, WClassMethod **cm)
{
	if (METH_isStatic((*cm))) return TRUE;
	if (target == 0) return FALSE;
	if (WOBJ_class(target) == *cl) return TRUE;
	else{
		UtfString name, desc;
		WClass *c2;
		WClassMethod *m2;
		name = METH_name((*cl),(*cm));
		desc = METH_desc((*cl),(*cm));
		m2 = getMethod(WOBJ_class(target),name,desc,&c2);
		if (m2 == NULL) return FALSE;
		*cl = c2;
		*cm = m2;
		return TRUE;
	}
}
static Var TrueMethodInvoke(Var stack[],int *params,void **toFree,WClass **wc,WClassMethod **wm)
{
	Var v;
	WObject thisMethod = stack[0].obj;
	WObject target = stack[1].obj;
	WObject pars = stack[2].obj;
	WObject dest = stack[3].obj;
	WClassMethod * cm = (WClassMethod *)objectPtr(thisMethod)[1].refValue;
	WClass * cl = (WClass *)objectPtr(thisMethod)[2].refValue;
	int type = objectPtr(dest)[1].intValue;
	v.obj = 0;
	*params = -1;

	if (!getInheritedMethod(target,&cl, &cm))
		return v;
	if ((METH_accessFlags((cm)) & ACCESS_ABSTRACT) != 0)
		return v;
	if (!METH_isPublic(cm)) return v;
	if ((target == 0  && !METH_isStatic(cm))|| pars == 0) return v;
	else{
		int numVars = numVarParameters(pars);
		int numPars = WOBJ_arrayLen(pars);
		int i = 0, off = 0;
		Var ret[2];
		WObject * wrappers = (WObject *)WOBJ_arrayStart(pars);
		Var *dp;
		Var *np = (Var *)mMalloc((numVars+1)*sizeof(Var));
		*toFree = np;
		dp = np;
		np[0].obj = target;
		for (i = 0; i<numPars; i++)
			if (WrapperToVar(wrappers[i],&np[i+1+off]) == 2)
				off++;
		if (METH_isStatic(cm)){
			numVars--;
			dp++;
		}
		if (METH_isNative(cm)){
			executeMethodRet(cl,cm,dp,numVars+1,&ret[0]);
			ret[1] = methodReturnHigh;
			if (cm->returnsValue && dest != 0) VarToWrapper(dest,&ret[0]);
			xfree(np);
			v.obj = dest;
			return v;
		}else{
			*params = numVars+1;
			*wc = cl;
			*wm = cm;
			v.refValue = dp;
			return v;
		}

	}
}
static Var ConstructorNewInstance(Var stack[])
{
	Var v;
	v.obj = 0;
	return v;
}

static Var TrueConstructorNewInstance(Var stack[],int *params,void **toFree,WClass **wc,WClassMethod **wm)
{
	Var v;
	WObject thisMethod = stack[0].obj;
	WObject pars = stack[1].obj;

	WClassMethod * cm = (WClassMethod *)objectPtr(thisMethod)[1].refValue;
	WClass * cl = (WClass *)objectPtr(thisMethod)[2].refValue;
	WObject target = createObject(cl);
	*params = -1;
	if (pars == 0 || target == 0){
		v.obj = 0;
		return v;
	}
	pushObject(target);
	{
		int numVars = numVarParameters(pars);
		int numPars = WOBJ_arrayLen(pars);
		int i = 0, off = 0;
		WObject * wrappers = (WObject *)WOBJ_arrayStart(pars);
		Var *np = (Var *)mMalloc((numVars+1)*sizeof(Var));
		*toFree = np;
		np[0].obj = target;
		for (i = 0; i<numPars; i++)
			if (WrapperToVar(wrappers[i],&np[i+1+off]) == 2)
				off++;
		if (METH_isNative(cm)){
			executeMethod(cl,cm,np,numVars+1);
			xfree(np);
			popObject();
			v.obj = target;
			return v;
		}else{
			*params = numVars+1;
			*wc = cl;
			*wm = cm;
			v.refValue = np;
			return v;
		}
	}
}

/*
static Var ConstructorNewInstance(Var stack[])
{
	Var v;
	WObject thisMethod = stack[0].obj;

	WObject pars = stack[1].obj;
	WClassMethod * cm = (WClassMethod *)objectPtr(thisMethod)[1].refValue;
	WClass * cl = (WClass *)objectPtr(thisMethod)[2].refValue;
	WObject target = createObject(cl);
	if (pars == 0 || target == 0){
		v.obj = 0;
		return v;
	}
	pushObject(target);
	{
		int numVars = numVarParameters(pars);

		int numPars = WOBJ_arrayLen(pars);

		int i = 0, off = 0;
		WObject * wrappers = (WObject *)WOBJ_arrayStart(pars);
		Var *np = (Var *)mMalloc((numVars+1)*sizeof(Var));
		np[0].obj = target;
		for (i = 0; i<numPars; i++)
			if (WrapperToVar(wrappers[i],&np[i+1+off]) == 2)
				off++;
		executeMethod(cl,cm,np,numVars+1);
		xfree(np);
		popObject();
		v.obj = target;
		return v;
	}
}
*/
static Var ReflectNewArray(Var stack[])
{
	Var v;

	v.obj = createArrayObject(1,stack[0].intValue);
	return v;
}
//===================================================================
int equals(int16 *big,int bigStart,int bigLength,int16 *smll,int smallStart,int smallLength,int options)
//===================================================================
{
	int sw = (options & STARTS_WITH) != 0;
	int ig = (options & IGNORE_CASE) != 0;
	int back = (options & BACKWARDS) != 0;

	if (bigLength < smallLength) return 0;
	if (!sw && (bigLength != smallLength)) return 0;
	if (!back){
		int16 *s = smll+smallStart, *end = smll+smallLength+smallStart;
		int16 *b = big+bigStart;
		if (!ig){
			while(s<end)
				if (*s++ != *b++) return 0;
		}else{
			while(s<end){
				int16 c1 = *s++;
				int16 c2 = *b++;
				if (c1 >= 'a' && c1 <= 'z') c1 ^= 0x20;
				if (c2 >= 'a' && c2 <= 'z') c2 ^= 0x20;
				if (c1 != c2) return 0;
			}
		}

	}else{
		int16 *s = smll+smallStart+smallLength-1, *end = smll+smallStart;
		int16 *b = big+bigStart+smallLength-1;
		if (!ig){
			while(s>=end)
				if (*s-- != *b--) return 0;
		}else{
			while(s>=end){
				int16 c1 = *s--;
				int16 c2 = *b--;
				if (c1 >= 'a' && c1 <= 'z') c1 ^= 0x20;
				if (c2 >= 'a' && c2 <= 'z') c2 ^= 0x20;
				if (c1 != c2) return 0;
			}
		}
	}
	return 1;

}

static Var StringEquals(Var stack[])
{
	Var v;
	WObject me = stack[0].obj;

	WObject other = stack[1].obj;
	int start = stack[2].intValue;
	int options = stack[3].intValue;
	v.intValue = 0;
	if (other == me)
		v.intValue = 1;
	else if (other != 0){
		WObject mca = objectPtr(me)[1].obj;
		WObject oca = objectPtr(other)[1].obj;
		if (mca == oca)
			v.intValue = 1;
		else{
			int ml = WOBJ_arrayLen(mca);
			int16 *mc = (int16 *)WOBJ_arrayStart(mca);
			int ol = WOBJ_arrayLen(oca);
			int16 *oc = (int16 *)WOBJ_arrayStart(oca);
			int i = 0;
			if (start < 0 || start > ml) return v;
			ml -= start;
			mc += start;
			if ((ml != ol) && (options & 0x2)) return v; //Not the same length.
			if (ml < ol) return v; //My length is less.
			if (!(options & 0x1)) {
				for (i = 0; i<ol; i++,mc++,oc++)
					if (*mc != *oc) return v;
			}else{
				register int16 c1;
				register int16 c2;
				for (i = 0; i<ol; i++,mc++,oc++){
					c1 = *mc; c2 = *oc;
					if (c1 >= 'a' && c1 <= 'z') c1 ^= 0x20;
					if (c2 >= 'a' && c2 <= 'z') c2 ^= 0x20;
					if (c1 != c2) return v;
				}
			}
			v.intValue = 1;
		}
	}


	return v;
}

static void vectorDoInsert(WObject vector,int where,WObject obj)
{
	Var *array = objectPtr(vector)+1;
	Var *count = objectPtr(vector)+2;
	int c = count->intValue;
	if (c == WOBJ_arrayLen(array->obj)){
		WObject newArray = createArrayObject(arrayType('L'),c*2);
		if (newArray == 0) return;
		else{
			Var *src = WOBJ_arrayStart(array->obj);
			Var *dest = WOBJ_arrayStart(newArray);
			Var *end = src+c;
			while(src != end) *dest++ = *src++;
			array->obj = newArray;
		}
	}
	if (where <= c) {
		Var *dest = WOBJ_arrayStart(array->obj)+c;
		Var *end = WOBJ_arrayStart(array->obj)+where;
		for(;dest != end;dest--) *dest = *(dest-1);
		end->obj = obj;
	}

	count->intValue++;
}
static Var SubStringEquals(Var stack[])
{
	Var v;
	WObject big = stack[0].obj;
	int bigStart = stack[1].intValue;
	int bigLength = stack[2].intValue;
	WObject smll = stack[3].obj;
	int smallStart = stack[4].intValue;
	int smallLength = stack[5].intValue;
	int options = stack[6].intValue;

	v.intValue = 0;
	if (big != 0 && smll != 0 && bigStart >= 0 && bigLength >= 0 && smallStart >= 0 && smallLength >= 0)
		if (WOBJ_arrayLen(big) >= bigStart+bigLength && WOBJ_arrayLen(smll) >= smallStart+smallLength)
		v.intValue =
			equals((int16 *)WOBJ_arrayStart(big),bigStart,bigLength,(int16 *)WOBJ_arrayStart(smll),smallStart,smallLength,options);
	return v;
}
static int compareChars(uint16 *one,int oneLen,uint16 *two,int twoLen)
{
	int i;
	for (i = 0; i<oneLen && i<twoLen; i++,one++,two++)
		if (*one < *two) return -1;
		else if (*one > *two) return 1;
	if (oneLen > twoLen) return 1;

	else if (oneLen < twoLen) return -1;
	return 0;
}

static Var SubStringCompare(Var stack[])
{
	Var v;
	WObject big = stack[0].obj;
	int bigStart = stack[1].intValue;
	int bigLength = stack[2].intValue;
	WObject smll = stack[3].obj;
	int smallStart = stack[4].intValue;
	int smallLength = stack[5].intValue;


	if (big == smll) return returnVal(0);
	if (big == 0) return returnVal(-1);

	if (smll == 0) return returnVal(1);
	if (bigStart+bigLength > WOBJ_arrayLen(big)) return returnExError(ERR_IndexOutOfRange);
	if (smallStart+smallLength > WOBJ_arrayLen(smll)) return returnExError(ERR_IndexOutOfRange);


	v.intValue = compareChars((uint16*)WOBJ_arrayStart(big)+bigStart,bigLength,(uint16*)WOBJ_arrayStart(smll)+smallStart,smallLength);
	return v;
}
//===================================================================
static int indexOf(int16 what,int16 *str,int strStart,int strLength,int start,int options)
//===================================================================
{
	if ((options & BACKWARDS) == 0){
		int s = start, end = strStart+strLength;
		while(s<end)
			if (str[s++] == what) return s-1;
		return -1;
	}else{
		int s = start, end = strStart;
		while(s>=end)
			if (str[s--] == what) return s+1;
		return -1;
	}
}
static Var SubStringIndexOf(Var stack[])
{
	Var v;
	int16 what = stack[0].intValue;

	WObject str = stack[1].obj;
	int strStart = stack[2].intValue;
	int strLength = stack[3].intValue;
	int start = stack[4].intValue;
	int options = stack[5].intValue;

	v.intValue = -1;
	if (str != 0 && strStart >= 0 && strLength > 0 && start >= strStart && start < strStart+strLength)
		if (WOBJ_arrayLen(str) >= strStart+strLength)
		v.intValue =
			indexOf(what,(int16 *)WOBJ_arrayStart(str),strStart,strLength,start,options);
	return v;

}


static WCHAR *allocSubStringData(WObject substring,int lengthNeeded)
{
	if (substring == 0) return NULL;
	else {
		WObject ca = objectPtr(substring)[1].obj;
		int len = ca == 0 ? 0 : WOBJ_arrayLen(ca);
		Var *ss;
		if (len < lengthNeeded)
			ca = createArray("C",lengthNeeded);
		ss = objectPtr(substring);
		(ss++)->obj = ca;


		(ss++)->intValue = lengthNeeded;
		(ss++)->intValue = 0;
		return (WCHAR *)WOBJ_arrayStart(ca);
	}
}

static int javaUtf8ToSubString(WObject substring,uchar *bytes,int byteLength)
{
	int need = sizeofJavaUtf8String(bytes,byteLength);
	WCHAR *ptr = allocSubStringData(substring,need);
	if (ptr != NULL && need != 0)
		javaUtf8ToStringData(bytes,ptr,byteLength);
	return need;
}


static Var ReflectIsInstance(Var stack[])
{
	Var v;
	WObject myReflect = stack[0].obj;
	WObject target = stack[1].obj;
	v.intValue = 0;

	if (target != 0)
		v.intValue = compatible(WOBJ_class(target),(WClass *)objectPtr(myReflect)[1].refValue);
	//
	// Fixed Dec 25, 2004 - had this the wrong way!
	//
		//v.intValue = compatible((WClass *)objectPtr(myReflect)[1].refValue,WOBJ_class(target));

	return v;
}
// The next two are defunct
/*
static Var ReflectIsAssignableFrom(Var stack[])
{
	Var v;
	WObject myReflect = stack[0].obj;
	WObject target = stack[1].obj;
	v.intValue = 0;

	if (target != 0)
		v.intValue = compatible((WClass *)objectPtr(target)[1].refValue,(WClass *)objectPtr(myReflect)[1].refValue);
	return v;
}
static Var ReflectIsTypeOf(Var stack[])
{
	Var v;
	WObject myReflect = stack[0].obj;
	WObject aType = stack[1].obj;

	v.intValue = 0;
	if (aType != 0){
		WClass *w = (WClass *)objectPtr(myReflect)[1].refValue;
		WClass *at = classNameToClass(aType);
		if (w != NULL && at != NULL)
			v.intValue = compatible(w,at);
	}
	return v;
}
*/

static Var ReflectIsTypeOf2(Var stack[])
{
	Var v;
	WObject which = stack[0].obj;
	WObject aType = stack[1].obj;
	v.intValue = 0;
	if (which != 0 && aType != 0){

		WClass *w = classNameToClass(which);
		WClass *at = classNameToClass(aType);
		if (w != NULL && at != NULL)
			v.intValue = compatible(w,at);
	}
	return v;
}
typedef struct ewe_compare {
	WClass *wclass;
	WObject comparer;
	WClassMethod *method;
}* EweCompare;


static Var ArrayToString(Var stack[])

{
	Var v;
	UtfString nm = stringToUtf8(replaceCharacter(createStringCopy(getStringNameFor(NULL,stack[0].obj,TRUE),NULL,NULL),'/','.'),&tempString,FALSE);
	int sl;
	v.obj = 0;
	sprintf(sprintBuffer,"@%u",stack[0].obj);
	sl = strlen(sprintBuffer);
	expandSpaceFor(&tempString,nm.len+sl,10,TRUE);
	strcpy(tempString.data+nm.len,sprintBuffer);
	nm.len += sl;

	v.obj = createStringFromUtf8(nm);

	return v;
}
static Var ArrayFinalize(Var stack[]){return returnVar(0);}
static Var ArrayEquals(Var stack[]){return returnVar(stack[0].obj == stack[1].obj);}
static Var ObjectHashCode(Var stack[]){return returnVar((int)stack[0].obj);}
static Var ObjectClone(Var stack[])
{
	Var v;
	WObject obj = stack[0].obj;
	WClass *cl = WOBJ_class(obj);
	v.obj = 0;
	if (cl == 0){ //Array.
		WObject ret = createArrayObject(WOBJ_arrayType(obj),WOBJ_arrayLen(obj));
		if (ret != 0)
			memcpy(objectPtr(ret),objectPtr(obj),arraySize(WOBJ_arrayType(obj),WOBJ_arrayLen(obj)));

		v.obj = ret;
	}else{
		v.obj = createObject(cl);
		if (v.obj != 0)
			memcpy(objectPtr(v.obj),objectPtr(obj),WCLASS_objectSize(cl));
	}
	return v;
}

//==================================================================
static int eweCompare(SortInfo info,int one,int two,int *error)
//==================================================================
{
	EweCompare ec = (EweCompare)info->functionData;
	Var pars[3], ret;
	pars[0].obj = ec->comparer;
	pars[1].intValue = one;
	pars[2].intValue = two;
	executeMethodRet(ec->wclass,ec->method,pars,3,&ret);
	*error = vmStatus.errNum;
	return ret.intValue;
}

//==================================================================
static int merge(SortInfo info,int one,int two,int length)
//==================================================================
{
	int o = one, t = two, d = one;
	int omax = one+length, tmax = two+length;
	int sl = info->sourceLen;
	int *dest = info->dest, *source = info->source;
	int hasFunc = info->function != NULL;
	if (omax > sl) omax = sl;
	if (tmax > sl) tmax = sl;
	while(1) {
		if (o >= omax) {
			if (t >= tmax) return 0;
			dest[d++] = source[t++];
		}else {
			if (t >= tmax) dest[d++] = source[o++];
			else {
				int error = 0, c = 0;
				if (hasFunc) c = info->function(info,source[o],source[t],&error);
				else if (source[o] < source[t]) c = -1;
				else if (source[o] > source[t]) c = +1;
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
	int mergeLength = 1, passes = 0;
	buff = (int32 *)mMalloc(sizeof(int32)*len);
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
	xfree(buff);
	return 0;
}

Var UtilsGetIntSequence(Var stack[])
{
	WObject array = stack[0].obj;
	int offset = stack[1].intValue;
	int first = stack[2].intValue;
	int step = stack[3].intValue;
	int length = stack[4].intValue;
	int i;
	int *sequence, *end;
	Var v;

	if (array == 0) return returnExError(ERR_NullArrayAccess);
	if (offset < 0 || offset+length > WOBJ_arrayLen(array)) return returnExError(ERR_IndexOutOfRange);
	sequence = (int *)WOBJ_arrayStart(array)+offset;
	end = sequence+length;
	for (i = first; sequence < end; i += step) *sequence++ = i;
	v.intValue = 1;
	return v;
}


Var UtilsSort(Var stack[])
{
	Var v;
	WObject toSort = stack[0].obj;

	int sortLen = stack[1].intValue;
	WObject comparer = stack[2].obj;
	int descending = stack[3].intValue;
	SortInfo info;
	EweCompare ec = comparer == 0 ? NULL : (EweCompare)mMalloc(sizeof(struct ewe_compare));
	info = (SortInfo)mMalloc(sizeof(struct sort_info));
	v.intValue = 0;

	if (toSort != 0){
		WClass * wclass = comparer == 0 ? NULL : WOBJ_class(comparer);
		WClassMethod *method = comparer == 0 ? NULL :
			getMethod(wclass, createUtfString("compare"),createUtfString("(II)I"), &wclass);
		if (comparer == 0 || method != NULL){
			int * buff = (int32 *)mMalloc(sizeof(int32)*sortLen);
			memcpy(buff,WOBJ_arrayStart(toSort),sortLen*sizeof(int32));
			info->original = buff;
			info->sourceLen = sortLen;
			if (ec != NULL){
				ec->comparer = comparer;
				ec->method = method;
				ec->wclass = wclass;
				info->function = &eweCompare;
				info->functionData = ec;
			}else{
				info->function = NULL;
				info->functionData = NULL;
			}
			info->descending = descending;
			v.intValue = sort(info);
			//Cannot sort in place because the memory could move during the sort.
			memcpy(WOBJ_arrayStart(toSort),buff,sortLen*sizeof(int32));
			xfree(buff);
		}
	}
	free(info);
	if (ec != NULL) free(ec);
	return v;
}

//typedef int (* getCharWidthFunc)(WObject fontMetrics,int character);
static getCharWidthFunc setupGetCharWidth(WObject fontMetrics);
static void closeGetCharWidth(WObject fontMetrics);


#define dlsSource dls[1].obj
#define dlsStart dls[2].intValue
#define dlsLength dls[3].intValue
#define dlsFont dls[4].obj
#define dlsDisplayWidth dls[5].intValue
#define dlsWidthOfFirst dls[6].intValue

#define dlsWidthOfWidest dls[7].intValue
#define dlsWidthOfAll dls[8].intValue
#define dlsDisplayLength dls[9].intValue
#define dlsWidest dls[10].intValue
#define dlsFlags dls[11].intValue
#define dlsFormat dls[12].obj
#define dlsTabWidth dls[13].intValue
#define dlsFixedWidth dls[14].intValue
#define dlsWidthCacheChars dls[15].obj
#define dlsWidthCache dls[16].obj

//-------------------------------------------------------------------
static Var displayLineSpecsCalculate(Var stack[])
//-------------------------------------------------------------------
{
	Var ret;
	Var *dls = objectPtr(stack[0].obj);
	int options = stack[1].intValue;
	getCharWidthFunc gcw = setupGetCharWidth(dlsFont);
	int first = 1;
	int length = dlsLength;
	//widthOfWidest = widthOfFirst = widthOfAll = 0;
	int sw = gcw(dlsFont,' ');
	int cw = 0, lp = -1, lw = 0;
	uint16 * source = (uint16 *)WOBJ_arrayStart(dlsSource);
	int wrap = ((options & 1) == 0);
	int tabWidth = dlsTabWidth, fixedWidth = dlsFixedWidth;
	uint16 * widthCacheChars = fixedWidth != -1 ? NULL :
		(uint16*)WOBJ_arrayStart(dlsWidthCacheChars);
	int * widthCache = fixedWidth != -1 ? NULL :
		(int*)WOBJ_arrayStart(dlsWidthCache);
	//
	ret.intValue = 0;
	if (length > 0) {
		int i;
		for (i = 0; i<length+1; i++){
			if (i == length){
				dlsDisplayLength = i;
				break;
			}else{
				int si = dlsStart+i;
				uint16 ch = source[si];
				int sp = ch == ' ';
				int nl = ch == '\n';
				int tab = ch == '\t';
				int charWidth = sp ? sw :
					tab ? tabWidth-((lw+cw)%tabWidth) : fixedWidth;
				if (charWidth == -1) {
					int idx = ch & 0xff;
					if (widthCacheChars[idx] == ch) charWidth = widthCache[idx];
					else{
						widthCache[idx] = charWidth = gcw(dlsFont,ch);
						widthCacheChars[idx] = ch;
					}
				}

				if (tab) sp = 1;
				if (nl) {
					charWidth = 0;
					lp = i-1;
				}else
					if (sp) lp = i;
				//System.out.println(lw+cw+charWidth+" <> "+displayWidth);
				if ((lw+cw+charWidth >= dlsDisplayWidth && lp >= 0 && wrap) || nl){
					if (sp) cw += charWidth;
					if (cw > dlsWidthOfWidest) dlsWidthOfWidest = cw;
					dlsDisplayLength = lp+1;
					lw += cw;
					cw = 0;
					if (nl) dlsFlags |= 1;
					break;
				}
				cw += charWidth;

				if (cw > dlsWidthOfWidest) dlsWidthOfWidest = cw;
				if (sp) {
					lw += cw;
					if (first) dlsWidthOfFirst = cw;
					first = 0;
					cw = 0;
				}
			}
		}
		if (first)
			dlsWidthOfFirst = dlsWidthOfAll = dlsWidthOfWidest;
		else
			dlsWidthOfAll = lw+cw;
		if (!wrap) dlsWidthOfWidest = dlsWidthOfAll;
		if (dlsWidthOfWidest > dlsWidest) dlsWidest = dlsWidthOfWidest;
		ret.intValue = 1;
	}
	closeGetCharWidth(dlsFont);
	return ret;
}

//-------------------------------------------------------------------
static Var displayLineSpecsGetWidth(Var stack[])
//-------------------------------------------------------------------
{
	WObject first = stack[0].obj;
	WObject fm = stack[1].obj;
	int options = stack[2].obj;
	int widest = 0;
	Var v;

	while (first != 0){
		int w = objectPtr(first)[5].intValue;
		if (w > widest) widest = w;
		first = objectPtr(first)[1].obj;
	}
	v.intValue = widest;
	return v;
}

#ifdef UNIX

struct heldObject {
	struct heldObject *prev, *next;
	WObject obj;
};

struct heldObject head;
int numHeld = 0;
static void holdObject(WObject obj)
{
	if (obj == 0) return;
	else{
		struct heldObject *hobj = (struct heldObject *)malloc(sizeof(struct heldObject));

		hobj->next = head.next;
		hobj->prev = &head;
		if (hobj->next != NULL) hobj->next->prev = hobj;
		hobj->obj = obj;
		head.next = hobj;
		numHeld++;
	}
}
static void releaseObject(WObject obj)
{
	if (obj == 0) return;
	else{

		struct heldObject *hobj = head.next, *before = &head;
		for(;hobj != NULL; hobj = hobj->next){
			if (hobj->obj == obj) {
				before->next = hobj->next;
				if (hobj->next != NULL) hobj->next->prev = before;
				free(hobj);
				numHeld--;
				return;
			}
			before = hobj;
		}
	}
}
static void holdRelease(WObject obj,int doHold)
{
	if (doHold) holdObject(obj);
	else releaseObject(obj);
}
static void enterExitEweMonitor(int isEnter)
{
	if (isEnter) {LOCKTHREAD}//enterCriticalSection(&sendMessageSection);
	else {UNLOCKTHREAD}//leaveCriticalSection(&sendMessageSection);
}

// ---------- Timer and Coroutines -----------------

typedef struct TimerEntryStruct{
	WObject requestor;
	WObject extraData;
	VmContext context;
	int paused;
	int interrupted;
	int processing;
	int useCallBack;
	int repeat;
	int inUse;
	int interval;
	int nextTick;
	int lastTick;
	int numParams;
	WClass *vclass;
	WClassMethod *method;
	Var params[3];
	int id;
} TimerEntry;

static int curID = 0;

static int getNewId() {return ++curID;}
static Var VmGetNewId(Var stack[])
{
	Var v;
	v.intValue = getNewId();
	return v;
}

static int maxTimerEntries = 10;
//static TimerEntry timerEntriesArray[maxTimerEntries];
static TimerEntry *timerEntries = NULL;

//#define MAXHELD 100
//static WObject heldObject[MAXHELD];
//static WObject *held = NULL;

static uint32 VmTimerInterval = 1000;
static uint32 VmTimerID = 0;
static uint32 VmTimerRequest = 2000; // 1000 is used already.
static void * VmThread = NULL;
static void vmTimerTick();

void markExtraObjects()
{
	int i;
	struct heldObject *h;
	for (h = head.next; h != NULL; h = h->next)
		markObject(h->obj);
	if (timerEntries == NULL) return;
	for (i = 0; i<maxTimerEntries; i++){
		TimerEntry *t = timerEntries+i;
		if (!t->inUse && !t->processing) continue;
		if (t->requestor != 0) markObject(t->requestor);

		if (t->extraData != 0) markObject(t->extraData);
		if (t->context != 0) {

			markObject(t->context->coroutine);
			if (vmStack != t->context->vmStack)
				markStack(t->context->vmStack,t->context->vmStackPtr);
			//markActiveStack(t->context->vmStack,t->context->vmStackPtr,t->context->stack);
		}
	}
}

uint32 lastMessage;
int tick = 0;

/*
uint32 TimerThreadProc(void * lpParameter)
{
	int sleepTime = VmTimerInterval;
	//debugString(TEXT("Tick!"));
	while(1){
		//sprintf(sprintBuffer,"T:%d ",sleepTime);
		//debugString(sprintBuffer);
		if (sleepTime >= 0 || sleepTime == INFINITE)

			;
			//WaitForSingleObject(waitEvent,sleepTime);

		else
			Sleep(0);
		//_putch('x');
		//debugString(TEXT("Tick!"));

		sleepTime = TimerThreadCheck();
#ifdef USE_LOG
		Log("t",1);
#endif
		if (vmStatus.errNum != 0){
			//postErrorDialog(mainHWnd);
			timerThreadExit = 1;
			exitSystem(1);
		}
		if (timerThreadExit) break;
	}
	return 0;
}
*/
static int firstTick = 1;
static uint32 lastTick = 0;

static uint32 maxStamp = 1 << 30;
//
// Return a millisecond count (which may wrap around).
//
#ifdef VCC
#define getTickCount GetTickCount
#elif _WINGCC

static uint32 getTickCount()
{
	return (clock()*1000)/CLOCKS_PER_SEC;
}
#else
static uint32 getTickCount()
{
	struct timeval tv;
	gettimeofday(&tv,NULL);
	return tv.tv_sec*1000+(tv.tv_usec/1000);
}
#endif

#ifdef NEVER_DEF
uint32 get32BitStamp()
{
	static uint32 stamp = 0xfffffffe;
	return stamp++;
}
#else
#define get32BitStamp() getTickCount()
#endif

static int64 getTimeStamp64()
{
	static int first64Tick = 1;
	static int64 cur64Tick;
	static int64 last64Tick;

	if (first64Tick){
		first64Tick = 0;
		cur64Tick = 0;
		last64Tick = (int64)get32BitStamp() & cINT64(0x00000000ffffffffL);
	}else{
		int64 now = (int64)get32BitStamp() & cINT64(0x00000000ffffffffL);
		if (now < last64Tick) now |= cINT64(0x0000000100000000L);
		cur64Tick += now-last64Tick;
		last64Tick = now & cINT64(0x00000000ffffffffL);
	}
	return cur64Tick;
}
static int32 getTimeStamp()
	{
	uint32 now = ((uint32)getTickCount())%maxStamp;
	int32 ret;
	if (firstTick) {
		firstTick = 0;
		lastTick = now;
		ret = 0;
	}else{
		if (lastTick > now) ret = (maxStamp-lastTick)+now;
		else ret = now-lastTick;
	}
	return ret;
	}

static int32 timeDifference(int32 earlier,int32 later)
{
	if (later < earlier) later += maxStamp;
	return later-earlier;
}

static int canCallInSystemQueue = 0;
static int TimerThreadCheck()
{
	int sleepTime = VmTimerInterval;
		LOCKTHREAD
		{
#ifdef USE_LOG
		Log("T",1);
#endif
			if (!timerThreadExit){
				int smallest = -1;//VmTimerInterval;
				int shouldTick = 0;
				int i;
				int now = getTimeStamp();
				for (i = 0; i<maxTimerEntries; i++){
					TimerEntry *t = timerEntries+i;
					int nt = 0;
					if (!t->inUse || t->processing) continue;
					nt = t->nextTick-now;
					if (nt <= 0) shouldTick = 1;
				}
				if (shouldTick) {
					vmTimerTick();
					//doMainWndProc(mainHWnd,WM_TIMER,VmTimerID,0);
					now = getTimeStamp();
				}
				if (!timerThreadExit){
					int waiting = 0;
					//char wt[50];
					for (i = 0; i<maxTimerEntries; i++){
						TimerEntry *t = timerEntries+i;
						int nt = 0;
						if (!t->inUse || t->processing || t->paused) continue;
						waiting++;
						nt = t->nextTick-now;
						if (nt < 0) nt = 0;
						if (nt<smallest || smallest == -1) smallest = nt;
					}
					//sprintf(sprintBuffer,"Waiting: %d",waiting);
					//debugString(sprintBuffer);
					if (waiting == 0) sleepTime = INFINITE;
					else {

						sleepTime = smallest;
						if (sleepTime < 0) sleepTime = 0;
					}
				}
			}
		}
		UNLOCKTHREAD
		return sleepTime;
}
static void requestVmTimer(int32 interval)
{
	/*
	if (0) requestVmTimerNotThreaded(interval);
	else requestVmTimerThreaded(interval);

	*/
}

static void prepareForNextTick(TimerEntry *t,int now,int howLate)
{
	int it = t->interval;
	t->lastTick = now;
	if (it > 0)
		while(t->nextTick <= now) t->nextTick += it;
	else t->nextTick = now;
	t->paused = 0;
}
int maxRun = 0;
int numRunning = 0;
WObject lastHeldData = 0;
static int checkCoroutineReturn(VmContext c)
{
	static WClass *cl = NULL;
	static WClassMethod *cm = NULL;
	int isThread = (objectPtr(c->coroutine)[6].intValue & 0x2);
	if (cl == NULL) cl = tryGetClass(createUtfString("ewe/sys/mThread"));
	if (cm == NULL) cm = getMethod(cl,createUtfString("ended"),createUtfString("()V"),NULL);

	if (!isThread || cl == NULL) return reportException();
	if (thrownException != 0){
		static WClassMethod *ue = NULL;
		if (ue == NULL) ue = getMethod(cl,createUtfString("exceptionThrown"),createUtfString("(Ljava/lang/Throwable;)V"),NULL);
		if (ue){
			Var p[2];
			p[0].obj = objectPtr(c->coroutine)[2].obj;
			p[1].obj = thrownException;
			thrownException = 0;
			executeTopMethod(cl,ue,p,2);
		}
	}
	if (c->fullyReturned && cm){
		Var p[1];
		p[0].obj = objectPtr(c->coroutine)[2].obj;
		executeTopMethod(cl,cm,p,1);
	}
	return 1;
}


static void vmTimerTick()
{
	int now = getTimeStamp();
	int i,j;
//	MSG msg;
	for (i = 0; i<maxTimerEntries; i++){
		int ln = 0;
		int late;
		int elapsed;
		TimerEntry *t = timerEntries+i;
		if (!t->inUse || t->processing || t->paused) continue;
		late = now-t->nextTick;
		elapsed = now-t->lastTick;
		if (late < 0) continue;
		t->params[2].intValue = elapsed;
		prepareForNextTick(t,now,late);
		if (!t->repeat) t->inUse = 0;
		//gc();
		if (t->context != NULL){
			Var *vs = vmStack;
			uint32 vss = vmStackSize;
			uint32 vsp = vmStackPtr;
			//debugString("Coroutine!");
			activeContext = t->context;
			if (t->context->runYet) {
				int retValue = 1;

				if (t->interrupted == 2) retValue = -1;
				else if (t->interrupted == 1) retValue = 0;
				else if (t->context->joining != 0){

					if (objectPtr(t->context->joining)[1].refValue == 0)
						retValue = 1;
					else
						retValue = 0;
				}
				t->context->stack[-1].intValue = retValue;
				t->context->joining = 0;
				fullExecuteMethod(NULL,NULL,NULL,0,NULL,t->context);
			}else {
				t->context->runYet = 1;
				numRunning++;
				if (numRunning > maxRun)

					maxRun = numRunning;
				fullExecuteMethod(t->vclass,t->method,t->params,t->numParams,NULL,t->context);
			}
			t = timerEntries+i;
			if (!checkCoroutineReturn(t->context)) exitSystem(-1);
			activeContext = NULL;
			vmStack = vs;
			vmStackSize = vss;
			vmStackPtr = vsp;
			if (t->context->fullyReturned){ //Completed entry.
				numRunning--;
				t->inUse = 0;
				objectPtr(t->context->coroutine)[1].refValue = 0;
				for (j = 0; j<maxTimerEntries; j++){
					TimerEntry *t2 = timerEntries+j;
					if (!t2->inUse || t2->context == NULL) continue;
					if (t2->context->joining == t->context->coroutine){
						t2->nextTick = now;
						t2->paused = 0;
					}
				}
				free(t->context->vmStack);

				free(t->context);
				t->context = NULL;
			}else{ // Either a sleep() or join()
				t->interrupted = 0;
				t->interval = t->context->sleepFor;
				t->lastTick = getTimeStamp();
				if (t->interval == -1) t->paused = 1;
				else t->nextTick = t->lastTick+t->interval;
			}
		}else if (!t->useCallBack) {
			if (canCallInSystemQueue){
				vmInSystemQueue = 1;
				executeTopMethod(t->vclass,t->method,t->params,t->numParams);
				vmInSystemQueue = 0;
			}else{
				holdObject(t->requestor);
				t->processing = 1;
			//FIX: implement the post message.
			//PostMessage(mainHWnd,WABAM_TICK,0,(LPARAM)i);
			}
		}else {
			if (TRUE){//PeekMessage(&msg,mainHWnd,0,0,PM_NOREMOVE) == 0){
				if (canCallInSystemQueue){
					vmInSystemQueue = 1;
					executeTopMethod(t->vclass,t->method,t->params,t->numParams);
					t->inUse = 0;
					vmInSystemQueue = 0;
				}else{
					holdObject(t->requestor);
					holdObject(t->extraData);
					lastHeldData = t->extraData;
					t->processing = 1;
					t->inUse = 0;
					//FIX: implement the post message
					//PostMessage(mainHWnd,WABAM_CALLBACK,t->requestor,t->extraData);
				}
			}
		}
		t = timerEntries+i;
		if (vmStatus.errNum > 0) timerThreadExit = 1;
		//else gc();
		if (timerThreadExit) break;
		now = getTimeStamp();

	}
}

static VmContext createContext(WObject forWho,int stackSize)
{
	VmContext vc = (VmContext)malloc(sizeof(struct vmContext));
	WObject runnable = objectPtr(forWho)[2].obj;
	vc->runYet = 0;
	vc->joining = 0;
/*
	vc->monitorHoldCount = vc->monitorWaitCount = 0;
	vc->waitingOnMonitor = 0;
*/
	stackSize /= sizeof(Var);
	vc->vmStack = (Var *)malloc(sizeof(Var)*stackSize);
	vc->vmStackSize = stackSize;
	vc->vmStackPtr = 0;
	vc->coroutine = forWho;
	return vc;
}

#define TYPE_TIMER 0
#define TYPE_CALLBACK 1
#define TYPE_COROUTINE 2
#define TYPE_TICK 3

static void expandTimerEntries()
{
	TimerEntry *entries;
	int st = (timerEntries == NULL) ? 0 : maxTimerEntries;
	int newSize = (st == 0) ? maxTimerEntries : st+20;
	int i;

	entries = (TimerEntry *)malloc(sizeof(TimerEntry)*newSize);
	if (timerEntries == NULL)
		timerEntries = entries;
	else {
		memcpy(entries,timerEntries,sizeof(TimerEntry)*st);
		free(timerEntries);
		timerEntries = entries;
	}
	maxTimerEntries = newSize;

	for (i = st; i<newSize; i++)
		timerEntries[i].inUse = timerEntries[i].processing = 0;
}
static int createTimerEntry(WObject requestor,uint32 interval,int type,WObject extraData)
{
	int32 now = getTimeStamp();
	int i,st = 0;
	LOCKTHREAD
	if (timerEntries == NULL) expandTimerEntries();

	if (VmTimerID == 0) requestVmTimer(VmTimerInterval);

tryAgain:
	for (i = st; i<maxTimerEntries; i++){
		TimerEntry *t = timerEntries+i;
		if (t->inUse || t->processing) continue;
		t->inUse = 1;
		t->context = NULL;
		t->interrupted = t->paused = 0;
		t->processing = 0;
		t->requestor = requestor;
		t->interval = interval;
		t->vclass = WOBJ_class(requestor);
		t->id = getNewId();
		t->params[0].obj = requestor;
		t->useCallBack = (type == TYPE_CALLBACK);
		if (type == TYPE_TIMER || type == TYPE_TICK){
			t->extraData = 0;
			t->repeat = (type == TYPE_TIMER);
			t->method = getMethod(t->vclass,createUtfString("ticked"),
				createUtfString("(II)V"),&(t->vclass));
			t->params[1].intValue = t->id;
			t->numParams = 3;
		}else if (type == TYPE_COROUTINE){
			WObject run = objectPtr(requestor)[2].obj;
			WObject cor = requestor;
			t->context = createContext(cor,interval);
			t->interval = 1;
			t->extraData = 0;
			t->repeat = 1;

			if (run != requestor){
				t->params[0].obj = run;
				t->vclass = WOBJ_class(run);
				t->method = getMethod(t->vclass,createUtfString("run"),
					createUtfString("()V"),&(t->vclass));
				t->numParams = 1;
			}else{
				WObject method = t->params[0].obj = objectPtr(requestor)[3].obj;
				t->method = (WClassMethod *)objectPtr(method)[1].refValue;
				t->vclass = (WClass *)objectPtr(method)[2].refValue;
				t->params[0].obj = objectPtr(requestor)[4].obj;
				t->params[1].obj = objectPtr(requestor)[5].obj;
				t->numParams = 2;
				if (t->params[0].obj == 0) {
					t->params[0].obj = t->params[1].obj;
					t->numParams = 1;
				}
			}
			objectPtr(cor)[1].refValue = t->context;
		}else{

			t->extraData = extraData;
			t->repeat = 1; //May have to repeat.
			t->method = getMethod(t->vclass,createUtfString("callBack"),
				createUtfString("(Ljava/lang/Object;)V"), &(t->vclass));
			t->params[1].obj = extraData;
			t->numParams = 2;
		}
		t->lastTick = t->nextTick = now;
		prepareForNextTick(t,now,0);
		UNLOCKTHREAD
		return t->id;
	}
	st = maxTimerEntries;
	expandTimerEntries();
	goto tryAgain;
	/*
	{
		UtfString err = createUtfString("Out of Timer/Callback resources.");
		LeaveCriticalSection(&sendMessageSection);
		VmError(ERR_Application,NULL,&err,NULL);
	}
	*/
	return 0;
}

static Var VmRequestTimer(Var stack[])
{
	Var v;
	WObject who = stack[0].obj;
	int32 interval = stack[1].intValue;
	v.intValue = createTimerEntry(who,interval,stack[2].intValue ? TYPE_TIMER : TYPE_TICK,0);
	return v;
}
static TimerEntry *findTimerEntry(VmContext vc)
{
	int i;
	for (i = 0; i<maxTimerEntries; i++){
		TimerEntry *t = timerEntries+i;
		if (!t->inUse) continue;
		if (t->context == vc) return t;
	}
	return NULL;
}
static Var VmCancelTimer(Var stack[])
{
	Var v;
	int i;
	int id = stack[0].intValue;
	LOCKTHREAD
	for (i = 0; i<maxTimerEntries; i++){
		TimerEntry *t = timerEntries+i;
		if (!t->inUse) continue;
		if (t->id == id) {
			if (t->processing != 0)
				t->processing = -1;
			else{
				t->id = t->inUse = 0;
			}
		}
	}
	UNLOCKTHREAD

	v.intValue = 0;
	return v;
}
static Var VmCallInSystemQueue(Var stack[])
{
	Var v;
	v.intValue = 1;
	createTimerEntry(stack[0].obj,0,TYPE_CALLBACK,stack[1].obj);
	return v;
}
static void wakeUp(WObject co,int interrupting)
{
	if (co != 0){
		VmContext vc = (VmContext)objectPtr(co)[1].refValue;
		TimerEntry *t;
		if (vc == NULL) return; // Not sleeping. It has ended.
		if (!interrupting && (vc->joining != 0)) return; // Not sleeping, It is joining another.
		t = findTimerEntry(vc);
		if (t != NULL){
			t->nextTick = getTimeStamp();
			t->paused = 0;
			if (t->interrupted != 2)
				t->interrupted = interrupting ? 2 : 1;
		}
		PULSEEVENT
	}
}
static Var CoroutineCreate(Var stack[])
{
	Var v;
	WObject coroutine = stack[0].obj;
	int stackSize = stack[1].intValue;
	v.intValue = 1;
	createTimerEntry(coroutine,stackSize,TYPE_COROUTINE,0);
	return v;
}
static Var CoroutineJoin(Var stack[])
{
	Var v;
	WObject coroutine = stack[0].obj;

	v.intValue = 0;
	if (currentContext == NULL)
		return returnException(RuntimeEx,"Coroutine.join() can only be called from within a running Coroutine");
	if (currentContext->coroutine == coroutine)
		return returnException(RuntimeEx,"a Coroutine cannot join itself");
	if (coroutine == 0)
		return returnException(NullPointerEx,NULL);
	v.intValue = 1;
	if (objectPtr(coroutine)[1].refValue == 0) return v;
	currentContext->joining = coroutine;
	currentContext->sleepFor = stack[1].intValue;
	exitContext = 1;
	return v;
}
static Var CoroutineGetCurrent(Var stack[])
{
	Var v;
	v.obj = 0;
	if (currentContext != NULL)
		v.obj = currentContext->coroutine;
	return v;
}
static Var CoroutineCount(Var stack[])

{
	Var v;
	int i;
	v.intValue = 0;
	for (i = 0; i<maxTimerEntries; i++){
		TimerEntry *t = timerEntries+i;
		if (!t->inUse) continue;
		if (t->context != NULL) v.intValue++;
	}
	return v;
}
static Var CoroutineSleep(Var stack[])
{
	Var v;
	v.intValue = 0;
	if (currentContext == NULL)
		return returnException(RuntimeEx,"Coroutine.sleep() can only be called from within a running Coroutine");
	currentContext->joining = 0;
	currentContext->sleepFor = stack[0].intValue;
	objectPtr(currentContext->coroutine)[7].intValue = getTimeStamp();
	exitContext = 1;
	return v;
}

//
// If this is called with coroutine being 0, it will suspend
// the current Coroutine and return the Coroutine object. In this
// case the Coroutine MUST be actively running. So it can only be
// called within the running Coroutine.
//
// When coroutine is not 0 it will wakeup the specified Coroutine
// once it acquires the critical section lock.
//
static WObject suspendResumeCoroutine(WObject coroutine)
{
	if (coroutine == 0){
		WObject ret = 0;
		LOCKTHREAD
		if (currentContext != NULL){
			/*
			currentContext->sleepFor = -1;
			exitContext = 1;
			*/
			ret = currentContext->coroutine;
			holdObject(ret);
		}
		UNLOCKTHREAD
		return ret;
	}else{
		LOCKTHREAD
		if (!timerThreadExit){
			releaseObject(coroutine);
			wakeUp(coroutine,0);
		}
		UNLOCKTHREAD
		return 0;
	}
}

static Var CoroutineWI(Var stack[],int interrupt)
{
	Var v;
	WObject co = stack[0].obj;
	v.intValue = 0;
	if (co == 0)
		return returnExError(ERR_NullObjectAccess);
	wakeUp(co,interrupt);
	return v;
}
static Var CoroutineWakeup(Var stack[])
{
	return CoroutineWI(stack,0);
}
static Var CoroutineInterrupt(Var stack[])
{
	return CoroutineWI(stack,1);
}

static Var VmGetTimeStamp(Var stack[])
{
	return returnVar(getTimeStamp());
}
static Var VmGetTimeStampLong(Var stack[])
{
	return returnLong(getTimeStamp64());
}

char *getCwd();

static Var VmGetProperty(Var stack[])
{
	WObject name = stack[0].obj;
	TCHAR *txt, *value;
	int length = 0, doFree = 1;
	Var v;
	v.obj = 0;
	if (name == 0) return v;
	txt = stringToNativeText(name);
	if (strcmp(txt,"java.class.path") == 0) {
		free(txt);
		doFree = 0;
		txt = "CLASSPATH";
	}
	//if (checkString(&txt,&length,TEXT("java.class.path"),15,&doFree,TEXT("classpath")))
	//	;
	if (strcmp(txt,TEXT("classpath")) == 0 || strcmp(txt,TEXT("CLASSPATH")) == 0){
		if (numClassPaths == -1) setupClassPaths();
		value = classPath;
	}else{
	//else (checkString(&text,length,TEXT("toConvert"),15,&doFree,TEXT("convertTo")))
		value = getenv(txt);
		if (value == NULL){
			if (strcmp(txt,"PATH_TO_EWE_DIR") == 0) value = getCwd();

#ifdef USE_EXTRA_GET_PROPERTY
			else value = extraGetProperty(txt);
#endif
		}
	}
	if (doFree) free(txt);
	if (value == NULL) return v;
	v.obj = createStringFromNativeText(value,-1);
	return v;
}
#endif //UNIX

#ifndef HAVE_ARC4RANDOM_PUSHB
#include <sys/param.h>
/*-
 * use heuristics to determine: MirOS #9-current had it, and if
 * arc4random_pushk is available, arc4random_pushb probably is too
 */
#if defined(arc4random_pushk) || (defined(MirBSD) && (MirBSD > 0x09A1))
#define HAVE_ARC4RANDOM_PUSHB 1
#else
#define HAVE_ARC4RANDOM_PUSHB 0
#endif
#endif

static Var
ewe_arc4random(Var stack[])
{
	Var rv;
	WObject array;
	uint8_t *buf;
	uint32_t val, len;

	// WTF is at stack[0] ?
	array = stack[1].obj;
	len = stack[2].intValue;
	if (array == 0 || len == 0)
		val = arc4random();
	else {
		buf = array ? (uint8_t *)WOBJ_arrayStart(array) : NULL;
#if HAVE_ARC4RANDOM_PUSHB
		val = arc4random_pushb(buf, (size_t)len);
#else
		arc4random_addrandom(buf, (int)len);
		val = arc4random();
#endif
	}
	rv.intValue = (int32)val;
	return (rv);
}
