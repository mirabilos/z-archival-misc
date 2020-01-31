package ewe.util;
/**
* This is an object that can be used to implement an object that is both an
* Iterator and an Enumeration. To do this simply override and provide an
* implementation for Iterator.hasNext() and Iterator.next() and (optionally)
* Iterator.remove(). By default remove() will throw an UnsupportedOperationException
* (or a RuntimeException on a Java 1.1 system).
**/
//##################################################################
public abstract class IteratorEnumerator implements Enumeration, Iterator{
//##################################################################
/**
* This returns the hasNext() method.
**/
//===================================================================
public boolean hasMoreElements() {return hasNext();}
//===================================================================
/**
* This returns the next() method.
**/
//===================================================================
public Object nextElement() {return next();}
//===================================================================
/**
 * The default version of this will throw an UnsupportedOperationException.
 */
//===================================================================
public void remove()
//===================================================================
{
	try{
		throw new UnsupportedOperationException("remove()");
	}catch(NoClassDefFoundError e){
		throw new RuntimeException("remove() not supported in this Iterator");
	}
}
//##################################################################
}
//##################################################################

