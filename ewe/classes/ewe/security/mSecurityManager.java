package ewe.security;
import ewe.reflect.Reflect;
import ewe.sys.*;
import ewe.util.WeakCache;
import ewe.data.PropertyList;

//##################################################################
public abstract class mSecurityManager extends SecurityManager{
//##################################################################
//private static boolean hasNative = true;
public abstract void checkLocalFileAccess();
private static WeakCache authorizers = new WeakCache();

//boolean native wasLoadedBy();
//static void calledBySystemClassOnly();
/*
private static Class classOfUntrustedLoader = null;
static
{
	classOfUntrustedLoader = ewe.reflect.Reflect.getForName("ewe.security.UntrustedClassLoader").getReflectedClass();
}
*/
//-------------------------------------------------------------------
private final void calledBySystemClassOnly()
//-------------------------------------------------------------------
{
	Class[] all = getClassContext();
	Class mine = Reflect.getForName("ewe.security.mSecurityManager").getReflectedClass();
	for (int i = 0; i<all.length; i++){
		if (!mine.isAssignableFrom(all[i])){
			String name = all[i].getName();
			if (name.startsWith("ewe.") || name.startsWith("java."))
				if (!name.equals("ewe.reflect.Method") && !name.equals("java.lang.Method"))
					return;
			throw new SecurityException();
		}
	}
}

//===================================================================
public PropertyList getAuthorizers(ClassLoader loader,boolean forWriting)
//===================================================================
{
	calledBySystemClassOnly();
	if (loader == null) return null;
	PropertyList found = (PropertyList)authorizers.get(loader);
	if (found != null) return found;
	if (!forWriting) throw new SecurityException();
	found = new PropertyList();
	authorizers.put(loader,found);
	return found;
}
//-------------------------------------------------------------------
protected Class findFirstInstanceof(Class aClass)
//-------------------------------------------------------------------
{
	Class[] all = getClassContext();
	for (int i = 0; i<all.length; i++){
		if (aClass.isAssignableFrom(all[i]))
			return all[i];
	}
	return null;
}
/*
//-------------------------------------------------------------------
protected boolean callIsUntrusted()
//-------------------------------------------------------------------
{
	if (mThread.inThread())
		if (mThread.currentThread().getThreadGroup() instanceof UntrustedThreadGroup)
			return true;
	if (loadedBy(null) != null) return true;
	return false;
}
//-------------------------------------------------------------------
protected static UntrustedClassLoader currentUntrustedLoader()
//-------------------------------------------------------------------
{
	try{
		mSecurityManager m = (mSecurityManager)System.getSecurityManager();
		return m.getCurrentUntrustedLoader();
	}catch(Exception e){
		throw new SecurityException("No mSecurityManager has been set!");
	}
}
//-------------------------------------------------------------------
mThreadGroup getCurrentMThreadGroup(boolean makeNew)
//-------------------------------------------------------------------
{
	if (mThread.inThread())
		return mThread.currentThread().getThreadGroup();
	try{
		mThreadGroup tg = ((JavaThreadGroup)Thread.currentThread().getThreadGroup()).getEweThreadGroup();
		if (tg != null) return tg;
	}catch(Exception e){
	}
	if (!makeNew) return null;
	UntrustedClassLoader ucl = (UntrustedClassLoader)loadedBy(null);
	if (ucl == null) return null;
	return new UntrustedThreadGroup(null,"Untrusted",ucl);
}

//-------------------------------------------------------------------
protected UntrustedClassLoader getCurrentUntrustedLoader()
//-------------------------------------------------------------------
{
	UntrustedClassLoader ucl = null;
	if (mThread.inThread())
		if (mThread.currentThread().getThreadGroup() instanceof UntrustedThreadGroup){
			UntrustedThreadGroup utg = (UntrustedThreadGroup)mThread.currentThread().getThreadGroup();
			//ewe.sys.Vm.debug("UTG: "+utg.getName());
			ucl = utg.getUntrustedClassLoader();
		}//else ewe.sys.Vm.debug(""+mThread.currentThread().getThreadGroup());
	try{
		UntrustedThreadGroup utg = (UntrustedThreadGroup)((JavaThreadGroup)Thread.currentThread().getThreadGroup()).getEweThreadGroup();
		ucl = utg.getUntrustedClassLoader();
		//ewe.sys.Vm.debug("Thread.utg: "+ucl);
	}catch(Exception e){
	}
	if (ucl == null) ucl = (UntrustedClassLoader)loadedBy(null);
	//ewe.sys.Vm.debug("ucl: "+ucl);
	return ucl;
}
*/
//===================================================================
public void checkCreateClassLoader()
//===================================================================
{
/*
	Class creating = findFirstInstanceof(Reflect.getForName("ewe.security.UntrustedClassLoader").getReflectedClass());
	if (creating == null) throw new SecurityException();
*/
	throw new SecurityException();
}
//===================================================================
public final void checkPassAuthority(ClassLoader from,ClassLoader to)
//===================================================================
{
	if (currentClassLoader() != from) throw new SecurityException();
}
//===================================================================
public void checkCreateSecurityManager(SecurityManager obj)
//===================================================================
{
	throw new SecurityException();
}
/*
//-------------------------------------------------------------------
protected ClassLoader loadedBy(Class classOfAClassLoader)
//-------------------------------------------------------------------
{
	if (classOfAClassLoader == null)
		classOfAClassLoader = classOfUntrustedLoader;
	if (classOfAClassLoader == null)
		classOfAClassLoader = classOfUntrustedLoader =
		ewe.reflect.Reflect.getForName("ewe.security.UntrustedClassLoader").getReflectedClass();
	Class[] c = getClassContext();
	for (int i = 0; i<c.length; i++){
		ClassLoader cl = c[i].getClassLoader();
		if (cl == null) continue;
		if (classOfAClassLoader.isAssignableFrom(cl.getClass())) return cl;
	}
	return null;
}
*/
//===================================================================
public mThreadGroup checkAssignMThreadGroup(mThreadGroup group)
//===================================================================
{
/*
	if (!callIsUntrusted() || group instanceof UntrustedThreadGroup) return group;
	if (group == null) return new UntrustedThreadGroup("Untrusted");
	throw new SecurityException();
*/
	throw new SecurityException();
}

//===================================================================
public final void checkAccess(Thread t)
//===================================================================
{
	//new Exception().printStackTrace();

	/*
	Class[] all = getClassContext();
	Class th = Reflect.getForName("java.lang.Thread").getReflectedClass();
	boolean found = false;
	for (int i = 0; i<all.length; i++){
		if (!th.isAssignableFrom(all[i])){
			String name = all[i].getName();
			if (found){
				if (name.startsWith("ewe.") || name.startsWith("java.")) return;
				throw new SecurityException();
			}
		}else
			found = true;
	}
	throw new SecurityException();
	*/
}
//===================================================================
public final void checkAccess(ThreadGroup tg)
//===================================================================
{
	checkAccess((Thread)null);
}
//===================================================================
public void checkRead(String filename, Object context)
//===================================================================
{
	checkRead(filename);
}
//===================================================================
public void checkDelete(String filename)
//===================================================================
{
	checkWrite(filename);
}
//##################################################################
}
//##################################################################

