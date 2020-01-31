//*******************************************************************
// This will be included TWICE in a file which targets Ewe and Java
// The first include should have DOING_EWE defined. The second should not.
//*******************************************************************
#include "ewe_implement.h"

#undef REGISTRY_CLASS
#undef REGISTRY_BASE_CLASS
#undef JavaObject

#ifdef DOING_EWE
#define REGISTRY_CLASS ewe_registry_key_object
#define REGISTRY_BASE_CLASS ewe_object
#define JavaObject ewe_object
#else
#define REGISTRY_CLASS java_registry_key_object
#define REGISTRY_BASE_CLASS java_object
#define JavaObject java_object
#endif

//########################################################################
class REGISTRY_CLASS : public REGISTRY_BASE_CLASS, public registry_key{
//########################################################################
public:
	static ClassRef clazz;
	static FieldRef remoteField, rootField, pathField, fullAccessField, createField;

	void setup(ObjectRef object)
	{
		if (clazz != 0) return;
		clazz = getClassRef(object);
		if (clazz != 0){
			SetupField(remoteField,"isRemote","Z");
			SetupField(fullAccessField,"fullAccess","Z");
			SetupField(createField,"createIfDoesntExist","Z");
			SetupField(rootField,"root","I");
			SetupField(pathField,"path","Ljava/lang/String;");
		}
	}

public:
	REGISTRY_CLASS (void *env,ObjectRef object,int dontOpen = 0):REGISTRY_BASE_CLASS(env,object)
	{
		setup(object);
		open(getStringField(pathField),getBooleanField(remoteField),getIntField(rootField),getBooleanField(fullAccessField),getBooleanField(createField),dontOpen);
	}
	REGISTRY_CLASS (void *env) : REGISTRY_BASE_CLASS(env,"ewex/registry/Registry")
	{
	}

	double nativeTest()
	{
		FieldRef fr = getAStaticFieldRef("testValue","D");
		if (fr == NULL) return 0;
		setStaticDoubleField(fr,-9876.5432);
		return getStaticDoubleField(fr);
	}
//########################################################################
};
//########################################################################

ClassRef REGISTRY_CLASS::clazz;
FieldRef REGISTRY_CLASS::remoteField;
FieldRef REGISTRY_CLASS::createField;
FieldRef REGISTRY_CLASS::fullAccessField;
FieldRef REGISTRY_CLASS::rootField;
FieldRef REGISTRY_CLASS::pathField;

//===================================================================
int saveRegistryData(void *env,REGISTRY_CLASS & key,ObjectRef dataRef,RegistryData data,int copyName)
//===================================================================
{
	if (!data) return 0;
	OBJECT rd(env,dataRef);
	if (copyName) rd.setObjectField(rd.getAFieldRef("name","Ljava/lang/String;"),rd.newString(key.toJavaString(data->name.data),1));
	int type = data->type;
	if (type == REG_SZ || type == REG_EXPAND_SZ || type == REG_MULTI_SZ){
		JavaString str = key.toJavaString(data->data.data);
		//wsprintf(buff,L"Length: %d - ",str->length);
		//wcsncat(buff,str->data,str->length);
		//MessageBox(NULL,buff,L"String!",MB_OK);
		rd.setObjectField(rd.getAFieldRef("value","Ljava/lang/Object;"),rd.newString(str,1));
	}else if (type == REG_DWORD_LITTLE_ENDIAN || type == REG_DWORD_BIG_ENDIAN){
		int intValue = *((int *)data->data.data);
		if (type != REG_DWORD_LITTLE_ENDIAN) intValue = registry_key::changeBigLittle(intValue);
		rd.setIntField(rd.getAFieldRef("intValue","I"),intValue);
	}else {
		ObjectRef ba = rd.newArray("B",data->data.dataLength);
		rd.setByteArrayRegion(ba,0,data->data.dataLength,(char *)data->data.data);
		rd.setObjectField(rd.getAFieldRef("value","Ljava/lang/Object;"),ba);
	}
	if (data != &registryDataBuffer) delete data;
	return 1;
}

//***********************************************************************
// These are the native methods.
//***********************************************************************
extern "C"{
//***********************************************************************
#ifndef RAPI_H
//===================================================================
JavaStaticFunc(int,ewex_registry_Registry_nativeGetPlatform,"ewex/registry/Registry")
(void *env,ClassRef clazz)
//===================================================================
{
	OSVERSIONINFO osInfo;
	osInfo.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
	if (!GetVersionEx(&osInfo)) return -1;
	return osInfo.dwPlatformId;
}
JavaStaticFuncEnd()
//===================================================================

//===================================================================
JavaStaticFunc(int,ewex_registry_Registry_createShortcut,"ewex/registry/Registry")
(void *env,ClassRef clazz,ObjectRef exeName,ObjectRef exeArgs,ObjectRef shortcut)
//===================================================================
{
	REGISTRY_BASE_CLASS obj(env,NULL);
	JavaString exePath = exeName == NULL ? NULL : obj.objectToJavaString(exeName);
	JavaString exePars = exeArgs == NULL ? NULL : obj.objectToJavaString(exeArgs);
	JavaString linkPath = shortcut == NULL ? NULL : obj.objectToJavaString(shortcut);

	WCHAR *ep = (exePath == NULL) ? NULL : exePath->toUnicode();
	WCHAR *ea = (exePars == NULL) ? NULL : exePars->toUnicode();
	WCHAR *lp = (linkPath == NULL) ? NULL : linkPath->toUnicode();
	int ret = (ep == NULL || lp == NULL) ? 0 : CreateLink(ep,ea,lp,NULL);

	if (ep) delete ep; if (ea) delete ea; if (lp) delete lp;
	if (exePath) delete exePath; if (exePars) delete exePars; if (linkPath) delete linkPath;

	return ret;
}
JavaStaticFuncEnd()
//===================================================================

#define FOLDER_PROGRAMS           0x0002
#define FOLDER_STARTMENU          0x000b

//===================================================================
JavaStaticFunc(ObjectRef,ewex_registry_Registry_nativeGetSpecialFolder,"ewex/registry/Registry")
(void *env,ClassRef clazz,int which)
//===================================================================
{
	REGISTRY_BASE_CLASS obj(env,NULL);
	static TCHAR buff[MAX_PATH+1];
	DWORD len = MAX_PATH;
#if _WIN32_WCE == 0
	if (which == -1){ //Windows folder
		if (!GetWindowsDirectory(buff,MAX_PATH))
			return 0;
		return obj.newString((new java_string())->make(buff),1);
	}
	return 0;
#else
	char *ret = NULL;
#ifdef HPC
	if (which == -1) ret = "\\Windows";
	else if (which == FOLDER_PROGRAMS) ret = "\\Program Files";
	else if (which == FOLDER_STARTMENU) ret = "\\Windows\\Programs";
#else
#if _WIN32_WCE >= 300
	if (which == -1) ret = "\\Windows";
	else if (which == FOLDER_PROGRAMS) ret = "\\Program Files";
	else if (which == FOLDER_STARTMENU) ret = "\\Windows\\Start Menu";
	/*
	else {
		wcscpy(buff,TEXT("\\Windows"));
		SHGetSpecialFolderPath(NULL,buff,which,0);
		return obj.newString((new java_string())->make(buff),1);
	}
	*/
#else
	if (which == -1) ret = "\\Windows";
	else if (which == FOLDER_PROGRAMS) ret = "\\Windows\\Start Menu\\Programs";
	else if (which == FOLDER_STARTMENU) ret = "\\Windows\\Start Menu";
#endif
#endif
	return (ret == NULL ? 0 : obj.newString((new java_string())->make(ret),1));
#endif
}
JavaStaticFuncEnd()
//===================================================================

#endif

//===================================================================
#ifndef RAPI_H
JavaFunc(ObjectRef,ewex_registry_RegistryKey_getSubKey)
#else
JavaFunc(ObjectRef,ewex_registry_RemoteRegistryKey_getSubKey)
#endif
(void *env,ObjectRef me,int index)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	return key.newString(key.getSubKey(index),1);
}
JavaFuncEnd()
//===================================================================
//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_getSubKeyCount)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_getSubKeyCount)
#endif
(void *env,ObjectRef me)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	int subKeys;
	key.queryInfo(NULL,&subKeys);
	return subKeys;
}
JavaFuncEnd()
//===================================================================
//===================================================================
#ifndef RAPI_H
JavaFunc(ObjectRef,ewex_registry_RegistryKey_getSubKeys)
#else
JavaFunc(ObjectRef,ewex_registry_RemoteRegistryKey_getSubKeys)
#endif
(void *env,ObjectRef me,int options)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	int num;
	JavaString all = key.getSubKeys(num);
	JavaString *keys = new JavaString[num];
	int *toSort = new int[num];
	if (num != 0){
		for (int i = 0; all != NULL; i++){
			JavaString one = all;
			all = (JavaString)all->next;
			keys[i] = one;
			toSort[i] = i;
		}
	}
	if ((options & SORT_DONT_SORT) == 0) {
		sort_info info;
		info.original = toSort;
		info.sourceLen = num;
		info.function = compareJavaString;
		info.descending = options & SORT_DESCENDING;
		info.functionData = keys;
		sort(&info);
	}
	ObjectRef array;
	if (options & GET_INDEXES_AS_LONGS){
		array = key.newArray("J",num);
		_int64 val;
		for (int i = 0; i<num; i++){
			val = toSort[i];
			key.setLongArrayRegion(array,i,1,&val);
		}
	}else if (options & GET_INDEXES){
		array = key.newArray("I",num);
		int val;
		for (int i = 0; i<num; i++){
			val = toSort[i];
			key.setIntArrayRegion(array,i,1,&val);
		}
	}else{
		array = key.newArray("Ljava/lang/String;",num);
		for (int i = 0; i<num; i++){
			JavaString js = keys[toSort[i]];
			key.setObjectArrayElement(array,i,key.newString(js,0));
		}
	}
	for (int i = 0; i<num; i++) delete keys[i];
	delete keys; delete toSort;
	return array;
}
JavaFuncEnd()

//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_getIndexedValue)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_getIndexedValue)
#endif
(void *env,ObjectRef me,int index,ObjectRef regData)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	return saveRegistryData(env,key,regData,key.read(index),1);
}
JavaFuncEnd()
//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_getNamedValue)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_getNamedValue)
#endif
(void *env,ObjectRef me,ObjectRef name,ObjectRef regData)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	return saveRegistryData(env,key,regData,key.read(key.objectToJavaString(name)),0);
}
JavaFuncEnd()

//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_setAStringValue)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_setAStringValue)
#endif
(void *env,ObjectRef me,ObjectRef name,ObjectRef value)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	return key.setString(key.objectToJavaString(name),key.objectToJavaString(value));
}
JavaFuncEnd()

//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_setABinaryValue)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_setABinaryValue)
#endif
(void *env,ObjectRef me,ObjectRef name,ObjectRef value)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	int len = key.arrayLength(value);
	char *bytes = new char[len];
	key.getByteArrayRegion(value,0,len,bytes);
	int ret = key.set(key.objectToJavaString(name),REG_BINARY,(unsigned char *)bytes,len);
	delete bytes;
	return ret;
}
JavaFuncEnd()
//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_setAnIntValue)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_setAnIntValue)
#endif
(void *env,ObjectRef me,ObjectRef name,int value)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	return key.set(key.objectToJavaString(name),REG_DWORD_LITTLE_ENDIAN,(unsigned char *)&value,4);
}
JavaFuncEnd()

//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_deleteAValue)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_deleteAValue)
#endif
(void *env,ObjectRef me,ObjectRef name)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	return key.deleteValue(key.objectToJavaString(name));
}
JavaFuncEnd()

//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_deleteAKey)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_deleteAKey)
#endif
(void *env,ObjectRef me)
//===================================================================
{
	REGISTRY_CLASS key(env,me,1); //Don't open the key or you won't delete it.
	return key.deleteKey();
}
JavaFuncEnd()
//===================================================================
#ifndef RAPI_H
JavaFunc(int,ewex_registry_RegistryKey_checkValid)
#else
JavaFunc(int,ewex_registry_RemoteRegistryKey_checkValid)
#endif
(void *env,ObjectRef me)
//===================================================================
{
	REGISTRY_CLASS key(env,me);
	return key.key != NULL;
}
JavaFuncEnd()


//===================================================================
JavaFunc(ObjectRef,ewex_registry_Registry_nativeTest)
(void *env,ClassRef me,ObjectRef obj)
//===================================================================
{
	REGISTRY_CLASS key(env);
	JavaObject object(env,obj);
	myfunc afunc = NULL;

	afunc = (myfunc)&sendToMe;
	int got = afunc(1234,12345.67890,(_int64)987654321L,'p',5678);
	MethodRef mr = object.getAStaticMethodRef("doCheck","(IDI)D");
	if (mr){
		JValue p[3];
		p[0].i = 1234567;
		object.setJdouble(&p[1],-1234567.89012);
		p[2].i = 89012;
		object.eweMonitorEnter();
		object.callMyStaticDoubleMethod(mr,p);
		object.eweMonitorExit();
	}
	return 0;
}
JavaFuncEnd()
//***********************************************************************
#ifdef DOING_EWE
//***********************************************************************
#ifndef RAPI_H
EweStaticFunc(int,ewex_registry_Registry_nativeGetPlatform,"ewex/registry/Registry")
//(void *env,ClassRef clazz)
//===================================================================
))
EweStaticFuncEnd()
//===================================================================
EweStaticFunc(int,ewex_registry_Registry_createShortcut,"ewex/registry/Registry")
//(void *env,ClassRef clazz)
//===================================================================
,stack[0].obj, stack[1].obj, stack[2].obj))
EweStaticFuncEnd()
//===================================================================
//===================================================================
EweStaticFunc(int,ewex_registry_Registry_nativeGetSpecialFolder,"ewex/registry/Registry")
//(void *env,ClassRef clazz)
//===================================================================
,stack[0].intValue))
EweStaticFuncEnd()
//===================================================================
#endif

//===================================================================
#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_getSubKeyCount)
//(void *env,ObjectRef me)
//===================================================================
))
EweFuncEnd()
#else
//===================================================================
EweFunc(int,ewex_registry_RemoteRegistryKey_getSubKeyCount)
//(void *env,ObjectRef me)
//===================================================================
))
EweFuncEnd()
//===================================================================
#endif

#ifndef RAPI_H
EweFunc(ObjectRef,ewex_registry_RegistryKey_getSubKey)
//(void *env,ObjectRef me,int index)
//===================================================================
,stack[1].intValue))
EweFuncEnd()
#else
EweFunc(ObjectRef,ewex_registry_RemoteRegistryKey_getSubKey)
//(void *env,ObjectRef me,int index)
//===================================================================
,stack[1].intValue))
EweFuncEnd()
#endif

#ifndef RAPI_H
EweFunc(ObjectRef,ewex_registry_RegistryKey_getSubKeys)
//(void *env,ObjectRef me,int options)
//===================================================================
,stack[1].intValue))
EweFuncEnd()
//===================================================================
#else
EweFunc(ObjectRef,ewex_registry_RemoteRegistryKey_getSubKeys)
//(void *env,ObjectRef me,int options)
//===================================================================
,stack[1].intValue))
EweFuncEnd()
//===================================================================
#endif

#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_getIndexedValue)
//(void *env,ObjectRef me,int index,ObjectRef regData)
//===================================================================
,stack[1].intValue,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#else
EweFunc(int,ewex_registry_RemoteRegistryKey_getIndexedValue)
//(void *env,ObjectRef me,int index,ObjectRef regData)
//===================================================================
,stack[1].intValue,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#endif
#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_getNamedValue)
//(void *env,ObjectRef me,ObjectRef name,ObjectRef regData)
//===================================================================
,(ObjectRef)stack[1].obj,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#else
EweFunc(int,ewex_registry_RemoteRegistryKey_getNamedValue)
//(void *env,ObjectRef me,ObjectRef name,ObjectRef regData)
//===================================================================
,(ObjectRef)stack[1].obj,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#endif

#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_setAStringValue)
//(void *env,ObjectRef me,ObjectRef name,ObjectRef value)
//===================================================================
,(ObjectRef)stack[1].obj,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#else
EweFunc(int,ewex_registry_RemoteRegistryKey_setAStringValue)
//(void *env,ObjectRef me,ObjectRef name,ObjectRef value)
//===================================================================
,(ObjectRef)stack[1].obj,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#endif

#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_setABinaryValue)
//(void *env,ObjectRef me,ObjectRef name,ObjectRef value)
//===================================================================
,(ObjectRef)stack[1].obj,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#else
EweFunc(int,ewex_registry_RemoteRegistryKey_setABinaryValue)
//(void *env,ObjectRef me,ObjectRef name,ObjectRef value)
//===================================================================
,(ObjectRef)stack[1].obj,(ObjectRef)stack[2].obj))
EweFuncEnd()
//===================================================================
#endif

//===================================================================
#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_setAnIntValue)
//(void *env,ObjectRef me,ObjectRef name,int value)
//===================================================================
,(ObjectRef)stack[1].obj,stack[2].intValue))
EweFuncEnd()
//===================================================================
#else
EweFunc(int,ewex_registry_RemoteRegistryKey_setAnIntValue)
//(void *env,ObjectRef me,ObjectRef name,int value)
//===================================================================
,(ObjectRef)stack[1].obj,stack[2].intValue))
EweFuncEnd()
//===================================================================
#endif

#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_deleteAValue)
//(void *env,ObjectRef me,ObjectRef name)
//===================================================================
,(ObjectRef)stack[1].obj))
EweFuncEnd()
//===================================================================
#else
EweFunc(int,ewex_registry_RemoteRegistryKey_deleteAValue)
//(void *env,ObjectRef me,ObjectRef name)
//===================================================================
,(ObjectRef)stack[1].obj))
EweFuncEnd()
//===================================================================
#endif

#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_checkValid)
//(void *env,ObjectRef me,ObjectRef name)
//===================================================================
))
EweFuncEnd()
//===================================================================
#endif

#ifndef RAPI_H
EweFunc(int,ewex_registry_RegistryKey_deleteAKey)
//(void *env,ObjectRef me)
//===================================================================
))
EweFuncEnd()
//===================================================================
#else
EweFunc(int,ewex_registry_RemoteRegistryKey_deleteAKey)
//(void *env,ObjectRef me)
//===================================================================
))
EweFuncEnd()
//===================================================================
#endif

//===================================================================
EweFunc(ObjectRef,ewex_registry_Registry_nativeTest)
//(void *env,ClassRef me)
//===================================================================
,(ObjectRef)stack[0].obj))
EweFuncEnd()
//===================================================================


//***********************************************************************
#endif //DOING_EWE
//***********************************************************************
//***********************************************************************
} //extern "C" {
//***********************************************************************

extern "C" {
EweExport ObjectRef EweCall EEE_ewex_registry_Registry_nativeTest(ClassRef myClass,JValue *pars)
{
	return 0;//Ewe_ewex_registry_Registry_nativeTest(Eni,myClass,pars[0].l);
}
}
