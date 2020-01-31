package java.lang;

import ewe.sys.Vm;

/**
* DO NOT USE THIS CLASS. It is only present to provide compatibility with
* the java SecurityManager. Attempting to create an instance will generate
* an error.
**/
//##################################################################
public class Thread{
//##################################################################

//-------------------------------------------------------------------
private Thread(ewe.util.Vector v){throw new UnsatisfiedLinkError("You should not use a Thread!");}
//-------------------------------------------------------------------

//-------------------------------------------------------------------
public final static Thread currentThread()
//-------------------------------------------------------------------
{
	throw new IllegalThreadStateException();
}
public final static void sleep(long millis) throws InterruptedException
{
	Vm.sleep((int)millis);
}
//##################################################################
}
//##################################################################

