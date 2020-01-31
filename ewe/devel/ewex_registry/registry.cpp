#define NO_JNI_METHODS
//USE_RAPI must be defined for RAPI usage.
#if _WIN32_WCE == 0
#ifdef USE_RAPI
//Comment off the following line if you do not want remote registry functions for the desktop.
#include <rapi.h>
#endif
#endif
//
// To do JNI methods you must tell the compiler where to look find the "jni.h" header.
//
#if _WIN32_WCE != 0
#define NO_JNI_METHODS
#if (WIN32_PLATFORM_HPCPRO || WIN32_PLATFORM_HPC2000)
#define HPC
#endif
#endif

#include "eni.h" //Should always be included, even if only doing JNI methods.


//########################################################################
//
// We start here by doing platform (i.e. JNI vs Ewe) independant code for
// Registry access. We deal only with standard C++ types or our own constructed
// C++ classes and structures. When dealing with strings we use a java_string
// class which is a utility class that is platform independant. See eni.h
// for its definition.
//
//########################################################################

#define SORT_DONT_SORT  0x1
#define SORT_CASE_SENSITIVE  0x2
#define SORT_DESCENDING  0x4

#define GET_INDEXES  0x8
#define GET_INDEXES_AS_LONGS  0x10

//===================================================================
static HKEY roots [] = {
//===================================================================
	HKEY_CLASSES_ROOT,HKEY_CURRENT_USER,HKEY_LOCAL_MACHINE,HKEY_USERS,
#if _WIN32_WCE != 0
		0,0};
#else
		HKEY_CURRENT_CONFIG,HKEY_DYN_DATA};
#endif

//===================================================================
typedef class registry_data {
//===================================================================
public:
	int index;
	byte_buffer name;
	DWORD type;
	byte_buffer data;
	DWORD value;
//===================================================================
}*RegistryData;
//===================================================================
registry_data registryDataBuffer;
//===================================================================


static byte_buffer nameBuffer;
//########################################################################
//
// This is a non-java version of a class which provides registry access.
// It will be combined with an object_access class later to provide platform
// specific classes (i.e. for Ewe and Java JNI).
//
//########################################################################
class registry_key{
//########################################################################
public:
	TCHAR *textName;
	WCHAR *uniName;
	int isRemote;
	HKEY *key;
	HKEY myRoot;

	registry_key()
	{
		textName = NULL;
		key = NULL;
	}
	//===================================================================
	void setName(WCHAR *javaName,int length = -1)
	//===================================================================
	{
		textName = toText(javaName,length);
		if (sizeof(TCHAR) == sizeof(WCHAR)) uniName = (WCHAR *)textName;
		else uniName = toUnicode(javaName,length);
	}
	//===================================================================
	static int changeBigLittle(int value)
	//===================================================================
	{
		unsigned char *b = (unsigned char *)&value, t;
		t = *b; *b = *(b+3); *(b+3) = t;
		t = *(b+1); *(b+1) = *(b+2); *(b+2) = t;
		return value;
	}
	//===================================================================
	JavaString toJavaString(unsigned char *what,JavaString dest = NULL)
	//===================================================================
	{
		if (dest == NULL) dest = new java_string;
		if (isRemote) dest->make((WCHAR *)what);
		else dest->make((TCHAR *)what);
		return dest;
	}
	//===================================================================
	static HKEY toRoot(int jroot)
	//===================================================================
	{
		if (jroot < 1 || jroot > 6) return 0;
		return roots[jroot-1];
	}
	//===================================================================
	int open(JavaString toOpen,int remote,int jroot,int fullAccess,int create,int dontOpen = 0)
	//===================================================================
	{
		setName(toOpen->data,toOpen->length);
		delete toOpen;
		isRemote = remote;
		key = NULL;
	#ifndef RAPI_H
		if (isRemote) return 0;
	#endif
		HKEY root = myRoot = toRoot(jroot);
		if (root == 0 || dontOpen) return 0;
		REGSAM access = fullAccess ? KEY_ALL_ACCESS : KEY_READ;
		key = new HKEY;
		long res;
		DWORD disp;
	#ifdef RAPI_H
		if (isRemote){
			if (create)
				res = CeRegCreateKeyEx(root,uniName,0,NULL,0,access,NULL,key,&disp);
			else
				res = CeRegOpenKeyEx(root,uniName,0,access,key);
		}else
	#endif
		{
		if (create)
			res = RegCreateKeyEx(root,textName,0,NULL,0,access,NULL,key,&disp);
		else
			res = RegOpenKeyEx(root,textName,0,access,key);
		}
		if (!(res == ERROR_SUCCESS)){
			delete key;
			key = NULL;
		}
		return (res == ERROR_SUCCESS);
	}
	//===================================================================
	int set(JavaString name,int type,unsigned char *data,int dataLength)
	//===================================================================
	{
		if (data == NULL) {
			if (name) delete name;
			return 0;
		}
		if (!isRemote){
			TCHAR *nm = name ? name->toNativeText() : NULL;
			int ret = RegSetValueEx(*key,nm,0,type,data,dataLength);
			if (nm) delete nm;
			if (name) delete name;
			return ret == ERROR_SUCCESS;
		}else{
#ifdef RAPI_H
			WCHAR *nm = name ? name->toUnicode() : NULL;
			int ret = CeRegSetValueEx(*key,nm,0,type,data,dataLength);
			if (nm) delete nm;
			if (name) delete name;
			return ret == ERROR_SUCCESS;
#else
			if (name) delete name;
			return 0;
#endif
		}
	}
	//This will delete the strings after.
	//===================================================================
	int setString(JavaString name,JavaString value)
	//===================================================================
	{
		if (!name) name = new java_string;
		if (!value) value = new java_string;
		int ret = 0;
		if (isRemote){
			WCHAR *txt = value->toUnicode();
			ret = set(name,REG_SZ,(unsigned char *)txt,(value->length+1)*2);
			delete txt;
		}else{
			TCHAR *txt = value->toNativeText();
			ret = set(name,REG_SZ,(unsigned char *)txt,(value->length+1)*sizeof(TCHAR));
			delete txt;
		}
		delete value;
		return ret;
	}
	//===================================================================
	int deleteKey()
	//===================================================================
	{
		if (textName == NULL || myRoot == 0) return 0;
		int ret = ~ERROR_SUCCESS;
		if (!isRemote){
			ret = RegDeleteKey(myRoot,textName) == ERROR_SUCCESS;
		}else{
#ifdef RAPI_H
			ret = CeRegDeleteKey(myRoot,uniName) == ERROR_SUCCESS;
#endif
		}
		return ret == ERROR_SUCCESS;
	}
	//===================================================================
	int deleteValue(JavaString name)
	//===================================================================
	{
		if (name == NULL) name = new java_string;
		if (key == NULL) {
			delete name;
			return 0;
		}
		int ret;
		if (!isRemote){
			TCHAR *nm = name->toNativeText();
			ret = RegDeleteValue(*key,nm) == ERROR_SUCCESS;
			delete nm;
		}else{
#ifdef RAPI_H
			WCHAR *nm = name->toUnicode();
			ret = CeRegDeleteValue(*key,nm) == ERROR_SUCCESS;
			delete nm;
#endif
		}
		delete name;
		return ret;
	}
	//===================================================================
	static int deleteKey(JavaString name,HKEY root,int isRemote)
	//===================================================================
	{
		if (name == NULL) return 0;
		int ret = 0;
		if (!isRemote){
			TCHAR *nm = name->toNativeText();
			ret = RegDeleteKey(root,nm);
			delete nm;
		}else{
#ifdef RAPI_H
			WCHAR *nm = name->toUnicode();
			ret = CeRegDeleteKey(root,nm);
			delete nm;
#endif
		}
		delete name;
		return ret;
	}
	//===================================================================
	RegistryData read(JavaString name,RegistryData data = &registryDataBuffer)
	//===================================================================
	{
		if (name == NULL) name = new java_string;
		if (key == NULL) {
			delete name;
			return NULL;
		}
		int allocated = 0;
		if (data == NULL){
			allocated = 1;
			data = new registry_data;
		}
		int ret = ~ERROR_SUCCESS;
		if (!isRemote){
			TCHAR *nm = name->toNativeText();
			ret = RegQueryValueEx(*key,nm,0,&data->type,NULL,data->data.getByteSize());
			if (ret == ERROR_SUCCESS){
				data->data.checkSize();
				ret = RegQueryValueEx(*key,nm,0,&data->type,data->data.data,data->data.getByteSize());
				data->data.checkSize();
			}
			if (nm) delete nm;
		}else{
#ifdef RAPI_H
			WCHAR *nm = name->toUnicode();
			ret = CeRegQueryValueEx(*key,nm,0,&data->type,NULL,data->data.getByteSize());
			if (ret == ERROR_SUCCESS){
				data->data.checkSize();
				ret = CeRegQueryValueEx(*key,nm,0,&data->type,data->data.data,data->data.getByteSize());
				data->data.checkSize();
			}
			if (nm) delete nm;
#endif
		}
		delete name;
		if (ret != ERROR_SUCCESS){
			if (allocated) delete data;
			return NULL;
		}
		return data;
	}
	//===================================================================
	JavaString getSubKey(int index)
	//===================================================================
	{
		if (key == NULL) return NULL;
	#ifndef RAPI_H
		if (isRemote) return NULL;
	#endif
		JavaString str = new java_string;
		nameBuffer.need((MAX_PATH+1)*2);
		FILETIME ft;
		int ret = ~ERROR_SUCCESS;
		if (!isRemote){
			ret = RegEnumKeyEx(*key,index,
				(TCHAR *)nameBuffer.data,nameBuffer.getTextCharSize(sizeof(TCHAR)),NULL,NULL,NULL,&ft);
			if (ret == ERROR_SUCCESS) {
				nameBuffer.checkSize();
			}
			/*
			if (ret != ERROR_SUCCESS){
				ret = RegEnumKeyEx(*key,index,
					(TCHAR *)nameBuffer.data,nameBuffer.getTextCharSize(sizeof(TCHAR)),NULL,NULL,NULL,&ft);
				nameBuffer.checkSize();
			}
			*/
			if (ret == ERROR_SUCCESS)
				str->make((TCHAR *)nameBuffer.data);
		}else{
#ifdef RAPI_H
			ret = CeRegEnumKeyEx(*key,index,
				(WCHAR *)nameBuffer.data,nameBuffer.getTextCharSize(sizeof(WCHAR)),NULL,NULL,NULL,&ft);
			if (ret == ERROR_SUCCESS) {
				nameBuffer.checkSize();
			}
			/*
			if (ret != ERROR_SUCCESS){
				ret = CeRegEnumKeyEx(*key,index,
					(WCHAR *)nameBuffer.data,nameBuffer.getTextCharSize(sizeof(WCHAR)),NULL,NULL,NULL,&ft);
				nameBuffer.checkSize();
			}
			*/
			if (ret == ERROR_SUCCESS)
				str->make((WCHAR *)nameBuffer.data);
#endif
		}
		if (ret != ERROR_SUCCESS){
			delete str;
			return NULL;
		}
		return str;
	}
	//===================================================================
	JavaString getSubKeys(int &num)
	//===================================================================
	{
		JavaString first = NULL, last = NULL;
		for (int i = 0;; i++){
			JavaString got = getSubKey(i);
			if (got == NULL) break;
			if (last) last->next = got;
			if (!first) first = got;
			last = got;
		}
		num = i;
		return first;
	}
	//===================================================================
	int queryInfo(int *numValues,int *numSubKeys)
	//===================================================================
	{
		if (key == NULL) return NULL;
		int ret = ~ERROR_SUCCESS;
		if (!isRemote){
			ret = RegQueryInfoKey(*key,NULL,NULL,NULL,(DWORD *)numSubKeys,NULL,NULL,(DWORD *)numValues,NULL,NULL,NULL,NULL);
		}else{
#ifdef RAPI_H
			ret = CeRegQueryInfoKey(*key,NULL,NULL,NULL,(DWORD *)numSubKeys,NULL,NULL,(DWORD *)numValues,NULL,NULL,NULL,NULL);
#endif
		}
		return ret == ERROR_SUCCESS;
	}
	//===================================================================
	RegistryData read(int index,RegistryData data = &registryDataBuffer)
	//===================================================================
	{
		if (key == NULL) return NULL;
	#ifndef RAPI_H
		if (isRemote) return NULL;
	#endif
		int allocated = 0;
		if (data == NULL){
			allocated = 1;
			data = new registry_data;
		}
		data->index = index;
		data->name.need(1024);
		int ret = ~ERROR_SUCCESS;
		if (!isRemote){
			ret = RegEnumValue(*key,index,
				(TCHAR *)data->name.data,
				data->name.getTextCharSize(sizeof(TCHAR)),
				NULL,
				&data->type,
				NULL,
				0);
			data->name.checkSize();
		}else{
#ifdef RAPI_H
			ret = CeRegEnumValue(*key,index,
				(WCHAR *)data->name.data,
				data->name.getTextCharSize(sizeof(WCHAR)),
				NULL,
				&data->type,
				NULL,
				0);
			data->name.checkSize();
#endif
		}
		if (ret != ERROR_SUCCESS){
			if (allocated) delete data;
			return NULL;
		}
		return read(toJavaString(data->name.data),data);
		return data;
	}
	//===================================================================
	~registry_key()
	//===================================================================
	{
		if (key != NULL){
			if (isRemote){
		#ifdef RAPI_H
				CeRegCloseKey(*key);
		#endif
			}else
				RegCloseKey(*key);
		}
		if (textName != NULL){
			if (uniName != (WCHAR *)textName) delete uniName;
			delete textName;
			textName = NULL;
		}
	}
//########################################################################
};
//########################################################################

typedef int (*myfunc)(...);

int sendToMe(int one,double two,_int64 three,char four,int five)
{

	return one+five;
}


#if _WIN32_WCE == 0
#include <shlobj.h>
int CreateLink(WCHAR *pathObj,WCHAR *args, WCHAR *pathLink, WCHAR *desc)
{
    HRESULT hres;
    IShellLink* psl;
	CoInitialize(NULL);
    // Get a pointer to the IShellLink interface.
    hres = CoCreateInstance((_GUID)CLSID_ShellLink, NULL,
                             CLSCTX_INPROC_SERVER,
                             (_GUID)IID_IShellLink,
                             (void**)&psl);
    if (SUCCEEDED(hres)) {
        IPersistFile* ppf;
		TCHAR *txt;
        // Set the path to the shortcut target, and add the
        // description.
        psl->SetPath(txt = toText(pathObj)); delete txt;
		//
		// Set the start directory.
		//
		int last = -1;
		bool quote = false;
		const WCHAR *src = pathObj;
		for (int i = 0;; i++, src){
			if (src[i] == 0){
				if (i != 0 && src[i-1] == '\"')
					quote = true;
				if (last == -1) last = i;
				break;
			}else if (src[i] == '\\' || src[i] == '/')
				last = i;
		}
		WCHAR *where = new WCHAR[last+3];
		memcpy(where,pathObj,sizeof(WCHAR)*last);
		if (quote) where[last++] = '"';
		where[last++] = 0;
		psl->SetWorkingDirectory(txt = toText(where));
		delete txt;
		//
		if (args != NULL){
			psl->SetArguments(txt = toText(args));
			delete txt;
		}
		if (desc != NULL){
			psl->SetDescription(txt = toText(desc));
			delete txt;
		}

       // Query IShellLink for the IPersistFile interface
       //for saving the shortcut in persistent storage.
        hres = psl->QueryInterface((_GUID)IID_IPersistFile,
                                   (void**)&ppf);

        if (SUCCEEDED(hres)) {
            hres = ppf->Save(pathLink,TRUE);
            ppf->Release();
        }
        psl->Release();
    }
    return SUCCEEDED(hres);
}
#else
int CreateLink(WCHAR *pathObj,WCHAR *args, WCHAR *pathLink, WCHAR *desc)
{
	WCHAR *exe = pathObj;
	int foundSpace = 0;

	for (WCHAR *e = exe; *e != 0; e++)
		if (*e == (WCHAR)' ') foundSpace = 1;
	if (foundSpace){
		int lp = lengthOf(pathObj);
		WCHAR *e = exe = new WCHAR[lp+3];
		*e++ = (WCHAR)'"';
		wcscpy(e,pathObj);
		e += lp;
		*e++ = (WCHAR)'"';
		*e = (WCHAR)0;
	}
	if (args == NULL) SHCreateShortcut(pathLink,exe);
	else{
		int lp = lengthOf(exe), la = lengthOf(args);
		WCHAR *all = new WCHAR[lp+la+2];
		wcscpy(all,exe);
		wcscat(all,L" ");
		wcscat(all,args);
		SHCreateShortcut(pathLink,all);
		delete all;
	}
	if (exe != pathObj) delete exe;
	return 1;
}
#endif
//***********************************************************************
#ifndef NO_EWE_METHODS
#define DOING_EWE
#include "registry_implement.cpp"
#endif
//***********************************************************************

/***********************************************************************
THESE ARE THE JAVA NATIVE FILES
************************************************************************/
#ifndef NO_JNI_METHODS
//***********************************************************************
extern "C" {
#undef ewex_registry_Registry_HKEY_CLASSES_ROOT
#define ewex_registry_Registry_HKEY_CLASSES_ROOT 1L
#undef ewex_registry_Registry_HKEY_CURRENT_USER
#define ewex_registry_Registry_HKEY_CURRENT_USER 2L
#undef ewex_registry_Registry_HKEY_LOCAL_MACHINE
#define ewex_registry_Registry_HKEY_LOCAL_MACHINE 3L
#undef ewex_registry_Registry_HKEY_USERS
#define ewex_registry_Registry_HKEY_USERS 4L
#undef ewex_registry_Registry_HKEY_CURRENT_CONFIG
#define ewex_registry_Registry_HKEY_CURRENT_CONFIG 5L
#undef ewex_registry_Registry_HKEY_DYN_DATA
#define ewex_registry_Registry_HKEY_DYN_DATA 6L
}
//***********************************************************************
#undef DOING_EWE
#include "registry_implement.cpp"
#endif
//***********************************************************************
