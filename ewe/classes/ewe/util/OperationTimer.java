package ewe.util;

/**
This class can be used to time the operation of a sequence of events to millisecond precision
(up to the resolution of the underlying os).<p>

You call the start() method to start timing and the end() method to end
timing. You can then call the start() method again to time another event.
You can also call start() repeatedly without calling end() - a start() method
will always call end() for any previously started timings.<p>

Call getEvents() to get an array of all the event names that were timed and getTimes() to get an
array of the corresponding times. Call toString() to get a list of times for each event in
seconds and ms.
**/
//##################################################################
public class OperationTimer{
//##################################################################

private Vector names = new Vector(), times = new Vector();

private String curOp;
private long started;

/**
Clear all the current timed values.
**/
//===================================================================
public void reset()
//===================================================================
{
	names.clear();
	times.clear();
}
/**
End timing an operation and add the timed name and value to the list of stored times.
**/
//===================================================================
public void end()
//===================================================================
{
	if (curOp == null) return;
	long took = ewe.sys.Vm.getTimeStampLong()-started;
	names.add(curOp);
	times.add(new ewe.sys.Long().set(took));
	curOp = null;
}
/**
Start timing an operation, ending any previous operation.
**/
//===================================================================
public void start(String operation)
//===================================================================
{
	if (curOp != null) end();
	started = ewe.sys.Vm.getTimeStampLong();
	curOp = operation;
}

//===================================================================
public String[] getEvents()
//===================================================================
{
	String[] ret = new String[names.size()];
	names.copyInto(ret);
	return ret;
}
//===================================================================
public long[] getTimes()
//===================================================================
{
	long[] ret = new long[times.size()];
	int n = times.size();
	for (int i = 0; i<n; i++)
		ret[i] = ((ewe.sys.Long)times.get(i)).value;
	return ret;
}
//===================================================================
public String toString()
//===================================================================
{
	String out = "";
	int n = times.size();
	for (int i = 0; i<n; i++){
		if (i != 0) out += "\n";
		out += names.get(i);
		out += ": ";
		long took = ((ewe.sys.Long)times.get(i)).value;
		String howLong = took < 1000 ? (took+" ms.") : (took/1000+" s "+(took%1000)+" ms.");
		out += howLong;
	}
	return out;
}
//##################################################################
}
//##################################################################

