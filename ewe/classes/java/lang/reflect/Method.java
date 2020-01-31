package java.lang.reflect;
import ewe.reflect.Reflect;
import ewe.reflect.Wrapper;

//##################################################################
public class Method implements Member{
//##################################################################

private ewe.reflect.Method meth;

//===================================================================
public Method(ewe.reflect.Method method)
//===================================================================
{
	meth = method;
}

private Class[] parameterTypes;
private Class myType;

//===================================================================
public Class[] getParameterTypes()
//===================================================================
{
	if (parameterTypes == null){
		parameterTypes = meth.getParameterTypes();
		/*
		String mp =
		String[] all = Reflect.getParameters(meth.getParameters().substring(1,);
		parameterTypes = new Class[all.length];
		for (int i = 0; i<all.length; i++)
			parameterTypes[i] = Reflect.typeToClass(all[i]);
			*/
	}
	return parameterTypes;
}
/**
 * Return the type of the Field as a Class object.
 */
//===================================================================
public Class getReturnType()
//===================================================================
{
	if (myType != null) return myType;
	return myType = Reflect.typeToClass(meth.getType());
}

//===================================================================
public String getName() {return meth.getName();}
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
	if (!(other instanceof Method)) return super.equals(other);
	Method m = (Method)other;
	if (!m.getName().equals(getName())) return false;
	if (!m.meth.getParameterTypes().equals(meth.getParameterTypes())) return false;
	if (!m.getDeclaringClass().equals(getDeclaringClass())) return false;
	return true;
}

//===================================================================
public Object invoke(Object target, Object[] args)
throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
//===================================================================
{
	int m = getModifiers();
	if (!ewe.reflect.Modifier.isStatic(m)){
		if (target == null) throw new NullPointerException();
		if (!getDeclaringClass().isInstance(target)) throw new IllegalArgumentException();
	}
	//
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
	Wrapper got = meth.invoke(target,all,new Wrapper());
	//
	if (got == null){
		if (meth.invocationError instanceof InvocationTargetException)
			throw (InvocationTargetException)meth.invocationError;
		else if (meth.invocationError instanceof SecurityException)
			throw (SecurityException)meth.invocationError;
		else
			throw new InvocationTargetException(meth.invocationError);
	}
	//
	if (getReturnType() == Void.TYPE)
		return null;
	return got.toJavaWrapper();
}

//##################################################################
}
//##################################################################

