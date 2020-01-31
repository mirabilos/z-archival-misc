package ewe.util;

/**
This class contains a set of useful methods for debugging an application.
**/
//##################################################################
public class Debug{
//##################################################################

private static OperationTimer timer;

/**
This method sets up a global OperationTimer that can be used to time
operations. Methods can call Debug.startTiming("An Operation") and this will
start the timing of an operation. That method can be safely called even if
enableTiming() is not called.
**/
//===================================================================
public static void enableTiming()
//===================================================================
{
	if (timer == null) timer = new OperationTimer();
}
/**
This method sets up a global OperationTimer that can be used to time
operations - using a specific timer. Methods can call Debug.startTiming("An Operation") and this will
start the timing of an operation. That method can be safely called even if
enableTiming() is not called.
**/
//===================================================================
public static void enableTiming(OperationTimer using)
//===================================================================
{
	timer = using;
	if (timer == null) timer = new OperationTimer();
}
/**
This method disables and returns the global OperationTimer, which was previously setup
by enableTiming(). If enableTiming() was not previously called, it returns a new empty OperationTimer.
**/
//===================================================================
public static OperationTimer disableTiming()
//===================================================================
{
	OperationTimer t = timer;
	timer = null;
	return t == null ? new OperationTimer() : t;
}
/**
If enableTiming() was previously called, this method will tell the debug timer to
start timing this operation, otherwise this method does nothing.
**/
//===================================================================
public static void startTiming(String operation)
//===================================================================
{
	if (timer != null) timer.start(operation);
}
/**
If enableTiming() was previously called, this method will tell the debug timer to
stop timing the current operation, otherwise this method does nothing.
**/
//===================================================================
public static void endTiming()
//===================================================================
{
	if (timer != null) timer.end();
}
//##################################################################
}
//##################################################################

