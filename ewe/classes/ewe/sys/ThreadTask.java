package ewe.sys;
import ewe.reflect.Wrapper;

//##################################################################
public abstract class ThreadTask implements Runnable{
//##################################################################
/**
* This is set true when doTask() has returned.
**/
public boolean complete = false;
/**
* This is set to any exception thrown when run in a separate mThread.
**/
public Throwable exception = null;
/**
* You must override this method. If the task should return a value it
* should call one of the setXXX() methods in the returnValue wrapper.
**/
//-------------------------------------------------------------------
protected abstract void doTask(boolean runningInSeparateThread,Object data,Wrapper returnValue);
//-------------------------------------------------------------------

private Object data;

//===================================================================
public final void run()
//===================================================================
{
	try{
		doTask(true,data,new Wrapper());
	}catch(Throwable t){
		exception = t;
	}finally{
		complete = true;
	}
}
/**
 * This attempts to execute the task. If the current thread is an mThread
	then it will call doTask() directly and return the Wrapper that will hold
	the return value of the task.<p>
	If the current Thread is not an mThread, then it will create a new mThread
	which will then call doTask() - and this method will return null.
 * @param data optional data to send to the task.
 * @return A Wrapper holding the return value from doTask() or null if the task
 * was postponed to be run in an mThread.
 */
//===================================================================
public Wrapper execute(Object data)
//===================================================================
{
	if (Coroutine.getCurrent() != null){
		Wrapper w = new Wrapper();
		doTask(false,data,w);
		complete = true;
		return w;
	}else{
		this.data = data;
		new mThread(this).start();
		return null;
	}
}
//##################################################################
}
//##################################################################

