//########################################################################
#ifdef _WIN32_WCE
#define NO_JNI_METHODS
#endif

#define Step 0.1
#define SleepTime 100

#ifdef WIN32
#include "e:\ewe\vm\eni.h"
#else
#include "/hd3p1/ewe/vm/eni.h"
#endif

//########################################################################

//########################################################################
class background_thread : public new_thread {
//########################################################################
protected:
        virtual void run(void *data)
        {
               for (double i = 0.0; i<1.0; i+= Step){
                       nap(SleepTime);//Instead of doing work we just sleep.
                       if (handle->shouldStop()) {
                               handle->set(Stopped|Aborted);
                               return;
                       }
                       handle->setProgress((float)i,1);
               }
               handle->setProgress(1.0f,0);
               handle->set(Succeeded);
			   //printf("I've succeeded!\n");
			   //nap(3000);
        }
//########################################################################
};
//########################################################################
//########################################################################
class NativeThread: public an_object
//########################################################################
{
public:
	a_static_method averageMethod;
	a_method testBackgroundMethod;
	a_method backgroundTaskMethod;
	a_method runMethod;
	a_field aValue;
	a_static_field theStatic;
//===================================================================
	NativeThread(object_access &obj,int forNewThread = 0)
//===================================================================
:		an_object(obj,obj.me,forNewThread)
		,averageMethod(obj,"average","I",forNewThread)
		,testBackgroundMethod(obj,"testBackground","V",forNewThread)
		,backgroundTaskMethod(obj,"backgroundTask","Lewe/sys/Handle;",forNewThread)
		,runMethod(obj,"run","V",forNewThread)
		,aValue(obj,"aValue","D",forNewThread)
		,theStatic(obj,"theStatic","D",forNewThread)
	{}
//===================================================================
	int average(int par1,int par2)
//===================================================================
	{
	JValue jv[2];
	jv[0].i = par1;
	jv[1].i = par2;
	return averageMethod.callInt(jv);
	}
//===================================================================
	void testBackground()
//===================================================================
	{
	testBackgroundMethod.callVoid();
	}
//===================================================================
	ObjectRef backgroundTask()
//===================================================================
	{
	return backgroundTaskMethod.callObject();
	}
//===================================================================
	void run()
//===================================================================
	{
	runMethod.callVoid();
	}
//########################################################################
};
//########################################################################

/**

  Implement this function: samples.natives.newthread.NativeThread.backgroundTask()

**/
//===================================================================
ObjectRef samples_natives_newthread_NativeThread_backgroundTask(object_access &obj)
//===================================================================
{
	NativeThread nt(obj);
	int av = nt.average(15,23);
	double v = nt.aValue.getDouble();
	nt.theStatic.setDouble(v);
    return (new background_thread())->startAndMakeHandle(obj);
}
//..................................................................
extern "C" {
EWEEXPORT ObjectRef EWECALL Eni_samples_natives_newthread_NativeThread_backgroundTask(ObjectRef obj,JValue * pars)
{
  ewe_object eo(Eni,obj);
  return samples_natives_newthread_NativeThread_backgroundTask(eo);
}
#ifndef NO_JNI_METHODS
JNIEXPORT ObjectRef JNICALL Java_samples_natives_newthread_NativeThread_backgroundTask(void *Jni,ObjectRef obj)
{
  java_object jo(Jni,obj);
  return samples_natives_newthread_NativeThread_backgroundTask(jo);
}
#endif //NO_JNI_METHODS
} //extern "C"
//..................................................................

#include "TestWindow.cpp"
