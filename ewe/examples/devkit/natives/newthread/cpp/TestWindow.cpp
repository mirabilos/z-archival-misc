/**

  Implement this function: samples.natives.newthread.TestNativeWindow.doSingleDraw()

**/
//===================================================================
void samples_natives_newthread_TestNativeWindow_doSingleDraw(object_access &obj,ObjectRef par1)
//===================================================================
{
	int num = obj.arrayLength(par1);
	int params[10];
	obj.getIntArrayRegion(par1,0,num,params);
#ifdef WIN32
	HWND hw = (HWND)params[0];
	HDC hdc = GetDC(hw);
	Ellipse(hdc,params[1],params[2],params[1]+params[3],params[2]+params[4]);
	ReleaseDC(hw,hdc);
#endif
	if (params[5] != 0){
		image_access ia((VmImageAccess)params[5]);
		ia.capture((void *)params[0],10,10,100,100,params[1],params[2]);
		ia.draw((void *)params[0],150,150,50,50,params[1]+20,params[2]+20);
	}
	return;
}
//..................................................................
extern "C" {
EWEEXPORT void EWECALL Eni_samples_natives_newthread_TestNativeWindow_doSingleDraw(ObjectRef obj,JValue * pars)
{
  ewe_object eo(Eni,obj);
  samples_natives_newthread_TestNativeWindow_doSingleDraw(eo,pars[0].l);
}
#ifndef NO_JNI_METHODS
JNIEXPORT void JNICALL Java_samples_natives_newthread_TestNativeWindow_doSingleDraw(void *Jni,ObjectRef obj,ObjectRef par1)
{
  java_object jo(Jni,obj);
  samples_natives_newthread_TestNativeWindow_doSingleDraw(jo,par1);
}
#endif //NO_JNI_METHODS
} //extern "C"
//..................................................................
//########################################################################
/**

  Implement this function: samples.natives.newthread.TestNativeWindow.doModifyImage()

**/
//===================================================================
void samples_natives_newthread_TestNativeWindow_doModifyImage(object_access &obj,int par1,int par2)
//===================================================================
{
	image_access ia((VmImageAccess)par1);
	image_access ia2((VmImageAccess)par2);
	ia2.paste(&ia,0,0);
	if (1) return;

	int w = ia.getWidth()/2;
	int h = ia.getHeight()/2;
	int total = w*h;
	int *get = new int[total];
	ia.getPixels(get,w/2,h/2,w,h,0);
	for (int i = 0; i<total; i++){
		int p = get[i];
		int avg = p&0xff;
		avg += (p>>8)&0xff;
		avg += (p>>16)&0xff;
		avg /= 3;
		avg &= 0xff;
		p &= 0xff000000;
		p |= avg | avg << 8 | avg << 16;

		p = get[i];
		int t = (p >> 16) & 0xff;
		p <<= 8;
		p |= 0xff000000|t;

/*
		int alpha = (p>>24)&0xff;
		alpha -= 10;
		alpha &= 0xff;
		p = (p & 0x00ffffff) | (alpha << 24);
*/
		get[i] = p;
	}
	ia.setPixels(get,w/2,h/2,w,h,0);
	delete get;
}
//..................................................................
extern "C" {
EWEEXPORT void EWECALL Eni_samples_natives_newthread_TestNativeWindow_doModifyImage(ObjectRef obj,JValue * pars)
{
  ewe_object eo(Eni,obj);
  samples_natives_newthread_TestNativeWindow_doModifyImage(eo,pars[0].i,pars[1].i);
}
#ifndef NO_JNI_METHODS
JNIEXPORT void JNICALL Java_samples_natives_newthread_TestNativeWindow_doModifyImage(void *Jni,ObjectRef obj,int par1,int par2)
{
  java_object jo(Jni,obj);
  samples_natives_newthread_TestNativeWindow_doModifyImage(jo,par1,par2);
}
#endif //NO_JNI_METHODS
} //extern "C"
//..................................................................
