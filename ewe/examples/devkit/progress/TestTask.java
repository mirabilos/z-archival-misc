/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.progress;
import ewe.sys.*;
//##################################################################
abstract class TestTask extends TaskObject{
//##################################################################
public int sleepTime = 100;
public static boolean showBounce = false;

//-------------------------------------------------------------------
protected boolean doSubTask(String what,int period,boolean allowStop)
//-------------------------------------------------------------------
{
	if (showBounce){
		//......................................................
		// This sets the handle.doing field.
		//......................................................
		handle.doing = what;
		//......................................................
		// This initializes the progress to zero to indicate the beginning of a subtask.
		//......................................................
		handle.progress = -1;
		//......................................................
		// This wakes any thread which may have called one of the handle's wait methods
		// to alert them that some change in the status of the handle has occured.
		//......................................................
		handle.changed();
	}else{
		handle.resetTime(what);
	}
	period *= 1000;

	int start = Vm.getTimeStamp();
	int end = start+period;
	int now;
	do {
		now = Vm.getTimeStamp();
		handle.progress = (now-start)/(float)period;
		if (handle.progress > 1) handle.progress = 1;
		if (showBounce) handle.progress = -1;
		handle.changed();
		sleep(sleepTime);
	}while((!shouldStop || !allowStop) && ((now) < end));
	return !shouldStop;
}

//##################################################################
}
//##################################################################
