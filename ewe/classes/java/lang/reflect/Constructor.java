package java.lang.reflect;
import ewe.reflect.Reflect;
import ewe.reflect.Wrapper;

//##################################################################
public class Constructor implements Member{
//##################################################################
private ewe.reflect.Constructor meth;

//===================================================================
public Constructor(ewe.reflect.Constructor constructor)
//===================================================================
{
	meth = constructor;
}

private Class[] parameterTypes;
private Class myType;

//===================================================================
public Class[] getParameterTypes()
//===================================================================
{
	if (parameterTypes == null){
		String[] all = Reflect.getParameters(meth.getParameters());
		parameterTypes = new Class[all.length];
		for (int i = 0; i<all.length; i++)
			parameterTypes[i] = Reflect.typeToClass(all[i]);
	}
	return parameterTypes;
}
//===================================================================
public String getName() {return getDeclaringClass().getName();}
//===================================================================

//===================================================================
public int getModifiers() {return meth.getModifiers();}
//===================================================================

//===================================================================
public Class getDeclaringClass() {return meth.getDeclaringClass();}
//===================================================================
//===================================================================
public int hashCode() {return getName().hashCode()+getDeclaringClass().hashCode();}
//===================================================================
//===================================================================
public Class[] getExceptionTypes()
//===================================================================
{
	return meth.getExceptionTypes();
}
//===================================================================
public String toString()
//===================================================================
{
	return meth.toString();
}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof Constructor)) return super.equals(other);
	Constructor m = (Constructor)other;
	if (!m.meth.getParameterTypes().equals(meth.getParameterTypes())) return false;
	if (!m.getDeclaringClass().equals(getDeclaringClass())) return false;
	return true;
}
//===================================================================
public Object newInstance(Object[] args)
throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
//===================================================================
{
	Class[] types = getParameterTypes();
	if (types.length != 0){
		if (args == null) throw new NullPointerException();
		if (args.length != types.length) throw new IllegalArgumentException();
		Object[] na = new Object[args.length];
		for (int i = 0; i<types.length; i++)
			na[i] = Wrapper.widenJavaWrapper(args[i],types[i]);
		args = na;
	}
	Wrapper[] all = Wrapper.toEweWrappers(args);
	Object got = meth.newInstance(all);
	if (got == null){
		if (meth.instantiationError instanceof InvocationTargetException)
			throw (InvocationTargetException)meth.instantiationError;
		else if (meth.instantiationError instanceof SecurityException)
			throw (SecurityException)meth.instantiationError;
		else if (meth.instantiationError instanceof InstantiationException)
			throw (InstantiationException)meth.instantiationError;
		else
			throw new InvocationTargetException(meth.instantiationError);
	}
	return got;
}


//##################################################################
}
//##################################################################

