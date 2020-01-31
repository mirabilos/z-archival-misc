package ewe.util;
import ewe.sys.TaskObject;
import ewe.sys.ThreadPool;
import ewe.ui.Event;
import ewe.ui.EventListener;

/**
* An EventDispatcher is a utility class that can be used to keep track of a set of
* ewe.ui.EventListener objects that wish to listen to some kind of event via their
* onEvent(Event ev) method.
* <p>
* The EventDispatcher can be configured to dispatch events in the current thread or
* via one or more background threads.
* <p>
* Listeners can also be added weakly, where a reference to them is kept only as long
* as they are referenced by other objects other than the EventDispatcher. So when they
* are garbage collected they will automatically be removed from the EventDispatcher.
**/
//##################################################################
public class EventDispatcher extends TaskObject{
//##################################################################
private Vector listeners;
private WeakSet weakListeners;
private Vector toSend;
private ThreadPool pool;
private Event ev;
private EventListener dest;
/**
* If this is set true then events are not delivered via a separate
* thread, but within the same thread that called dispatch().
**/
public boolean dontUseSeparateThread;
//-------------------------------------------------------------------
private EventDispatcher(EventListener dest,Event ev)
//-------------------------------------------------------------------
{
	this.dest = dest;
	this.ev = ev;
}
//-------------------------------------------------------------------
protected final void doRun()
//-------------------------------------------------------------------
{
	if (!ev.consumed)
		dest.onEvent(ev);
}

//===================================================================
public EventDispatcher()
//===================================================================
{
	this(1);
}
/**
 * Create an EventDispatcher specifying the number of threads used to deliver events.
 * @param maxParallelDeliveries the maximum number of threads used to deliver evetns simultaneously.
 */
//===================================================================
public EventDispatcher(int maxParallelDeliveries)
//===================================================================
{
	if (maxParallelDeliveries < 1) maxParallelDeliveries = 1;
	this.maxParallelDeliveries = maxParallelDeliveries;
}
/**
 * This is the maximum number of parallel event deliveries allowed.
 */
private int maxParallelDeliveries = 1;
/**
 * Add an event listener.
 * @param listener The listener to add.
 * @param addToWeakSet If this is true the listener will be added to an internal
 * WeakSet. This means that if no other object is referring to that listener it will
 * eventually be garbage collected and removed from the list.
 */
//===================================================================
public void addListener(EventListener listener,boolean addToWeakSet)
//===================================================================
{
	if (listener == null) return;
	if (addToWeakSet){
		if (weakListeners == null) weakListeners = new WeakSet();
		weakListeners.add(listener);
	}else{
		if (listeners == null) listeners = new Vector();
		if (listeners.contains(listener)) return;
		listeners.add(listener);
	}
}
/**
 * Add an event listener.
 * @param listener The listener to add.
 */
//===================================================================
public void addListener(EventListener listener)
//===================================================================
{
	addListener(listener,false);
}
/**
 * Remove an event listener.
 * @param listener The listener to remove.
 */
//===================================================================
public void removeListener(EventListener listener)
//===================================================================
{
	if (weakListeners != null) weakListeners.remove(listener);
	if (listeners != null) listeners.remove(listener);
}
/**
* Get all current listeners.
* @param destination A destination Vector, which will be cleared first, to hold the listeners. This can be null in which case a new one will be created.
* @return The destination or new Vector containing all active listeners.
*/
//===================================================================
public Vector getCurrentListeners(Vector destination)
//===================================================================
{
	if (destination == null) destination = new Vector();
	destination.clear();
	if (listeners != null && listeners.size() != 0)
		destination.addAll(listeners);
	else
		listeners = null;
	if (weakListeners != null){
		Object[] refs = weakListeners.getRefs();
		boolean did = false;
		for (int i = 0; i<refs.length; i++)
			if (refs[i] != null && !destination.contains(refs[i])){
				destination.add(refs[i]);
				did = true;
			}
		if (!did) weakListeners = null;
	}
	return destination;
}
/**
 * Send off the event to the listeners. This is done via a separate thread unless
 * dontUseSeparateThread is true, in which case the onEvent() method of each
 * listener is called directly.
 * @param ev the event to send.
 */
//===================================================================
public void dispatch(Event ev)
//===================================================================
{
	if (ev == null) return;
	ev.consumed = false;
	if (listeners == null && weakListeners == null) return;
	toSend = getCurrentListeners(toSend);
	if (toSend.size() != 0){
		if (pool == null && !dontUseSeparateThread) pool = new ThreadPool(0,maxParallelDeliveries);
		for (int i = 0; i<toSend.size() && !ev.consumed; i++){
			if (dontUseSeparateThread)
				((EventListener)toSend.get(i)).onEvent(ev);
			else
				pool.addTask(new EventDispatcher((EventListener)toSend.get(i),ev));
		}
	}else
		toSend = null;
}
/**
 * Check if there are any listeners in the Dispatcher. If this returns
 * true it indicates that there are definitely no listeners. If it returns
 * false it indicates that there <b>may</b> be some listeners.
 */
//===================================================================
public boolean isEmpty()
//===================================================================
{
	int count = listeners == null ? 0 : listeners.size();
	if (weakListeners != null && !weakListeners.isEmpty()) count++;
	if (count == 0){
		listeners = null;
		weakListeners = null;
		return true;
	}
	return false;
}
/**
* Close the EventDispatcher.
**/
//===================================================================
public void close()
//===================================================================
{
	if (pool != null) pool.stopTask(0);
	pool = null;
}
//===================================================================
public void finalize()
//===================================================================
{
	synchronized(ewe.sys.Vm.getSyncObject()){
		close();
	}
}
//##################################################################
}
//##################################################################

