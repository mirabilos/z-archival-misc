//===================================================================
// These are used by all applications.
//===================================================================
#undef OBJECT
#undef JavaFunc
#undef JavaStaticFunc
#undef JavaFuncEnd
#undef JavaStaticFuncEnd

#ifdef DOING_EWE
#define OBJECT ewe_object
#define JavaFunc(TYPE,NAME) TYPE Ewe_##NAME
#define JavaStaticFunc(TYPE,NAME,CLAZZ) TYPE Ewe_##NAME
#define JavaStaticFuncEnd()
#define JavaFuncEnd()
#else
#define OBJECT java_object
#define JavaFunc(TYPE,NAME) JNIEXPORT TYPE JNICALL Java_##NAME
#define JavaStaticFunc(TYPE,NAME,CLAZZ) JNIEXPORT TYPE JNICALL Java_##NAME
#define JavaFuncEnd()
#define JavaStaticFuncEnd()
#endif

#ifndef EweFunc
#define EweFunc(TYPE,NAME)\
	DllExport Var EweCall NAME(Var stack[]){Var v; return_##TYPE(v,Ewe_##NAME(&vm,(ObjectRef)stack[0].obj

#define EweStaticFunc(TYPE,NAME,CLAZZ)\
	DllExport Var EweCall NAME(Var stack[]){Var v; return_##TYPE(v,Ewe_##NAME(&vm,(ClassRef)vm.getClass(CLAZZ)

#define EweFuncEnd() ;return v;}
#define EweStaticFuncEnd() ;return v;}


#define return_char(VAR,VALUE) VAR.intValue = VALUE
#define return_short(VAR,VALUE) VAR.intValue = VALUE
#define return_int16(VAR,VALUE) VAR.intValue VALUE
#define return_int(VAR,VALUE) VAR.intValue = VALUE
#define return_float(VAR,VALUE) VAR.floatValue = VALUE
#define return_ObjectRef(VAR,VALUE) VAR.obj = (WObject)VALUE
#define return_void(VAR,VALUE) {VALUE;} VAR.obj = 0
#define return_int64(VAR,VALUE) VAR = vm.returnLong(VALUE)
#define return_double(VAR,VALUE) VAR = vm.returnDouble(VALUE)
#endif
