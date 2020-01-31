package ewe.sys;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ewe.data.DataObject;
import ewe.reflect.Reflect;
import ewe.reflect.Wrapper;
import ewe.util.Copyable;
import ewe.util.Vector;
import ewe.util.mString;

/**
This class provides a number of useful static methods that can simplify a
number of common tasks that use the Java Reflection API.
**/
//
// TODO add forName();
//
//##################################################################
public class Reflection{
//##################################################################

//-------------------------------------------------------------------
private Reflection(){}
//-------------------------------------------------------------------
private static int hasWrapperMethods = -1;

private static boolean checkHasWrapperMethods()
{
	return true;
}
//private static native void wrapperInvoke(Object target, Object methodOrConstructor, Wrapper[] parameters, Wrapper dest);
//private static native void wrapperGetSetField(Object target, Field field, Wrapper data, boolean isGet);
//===================================================================
//public static final Class stringClass = forName("java.lang.String");
//public static final Class valueClass = forName("ewe.data.Value");
//public static final Class itemClass = forName("ewe.ui.Item");
public static final String encodedStringClass = "Ljava/lang/String;";
/** A zero length Class array. **/
public static final Class[] emptyClasses = new Class[0];
/** A zero length Object array. **/
public static final Object[] emptyParameters = new Object[0];
//===================================================================

/**
Get all the public fields for the data object, using the "_fields" variables for
the object if present. This method makes use of ewe.data.DataObject.getFieldList().
@param dataObject The dataObject to get the fields for.
@return all the public fields for the data object, using the "_fields" variables for
the object if present.
*/
//===================================================================
public static Field [] fieldsFor(Object dataObject)
//===================================================================
{
	Class c = dataObject.getClass();
	String [] all = mString.split(DataObject.getFieldList(c,dataObject,false),',');
	Vector v = new Vector();
	for (int i = 0; i<all.length; i++)
		try{
			v.addElement(c.getField(all[i]));
		}catch(Exception e){
		}
	Field[] ret = new Field[v.size()];
	v.copyInto(ret);
	return ret;
}

/**
 * Construct a new Object using Eve Wrapper values. This is useful for large number
 * of invocations, because Eve Wrappers are re-usable - unlike Java Wrappers which
 * are immutable.
 * @param c The Constructor to use.
 * @param parameters the parameters as an array of Wrapper values.
 * @return the created Object.
 * @throws IllegalAccessException
 * @throws IllegalArgumentException
 * @throws InvocationTargetException
 * @throws InstantiationException
 */
public static Object newInstance(Constructor c, Wrapper[] parameters)
throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
{
	if (parameters == null) parameters = Wrapper.noParameter;
	return c.newInstance(Wrapper.toJavaWrappers(parameters));
	/*
	if (hasWrapperMethods == -1) checkHasWrapperMethods();
	if (hasWrapperMethods == 0){
		//Must use Java methods.
	}
	ewe.reflect.Constructor ec = (ewe.reflect.Constructor)NativeAccess.getSetVariable(c,null,true);
	return ec.newInstance(parameters);
	*/
}
/**
 * Invoke a method using Eve Wrapper values. This is useful for large number
 * of invocations, because Eve Wrappers are re-usable - unlike Java Wrappers which
 * are immutable.
 * @param target the target Object. This can be null for static methods.
 * @param method the Method to invoke.
 * @param parameters the parameters as Wrapper values.
 * @param result a Wrapper to hold the destination.
 * @throws IllegalAccessException
 * @throws IllegalArgumentException
 * @throws InvocationTargetException
 */
public static void invoke(Object target, Method method, Wrapper[] parameters, Wrapper result)
throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
{
	if (parameters == null) parameters = Wrapper.noParameter;
	if (result == null) result = new Wrapper();
	result.fromJavaWrapper(method.invoke(target,Wrapper.toJavaWrappers(parameters)));
	/*
	if (hasWrapperMethods == -1) checkHasWrapperMethods();
	if (hasWrapperMethods == 0){
		//Must use Java methods.
		result.fromJavaWrapper(method.invoke(target,Wrapper.toJavaWrappers(parameters)));
		return;
	}
	ewe.reflect.Method em = (ewe.reflect.Method)NativeAccess.getSetVariable(method,null,true);
	em.invoke(target,parameters,result);
	*/
}
/**
This method invokes the method in the background returning a Handle used to monitor
the running task.
@param targetOrClass The target object or a Class object if the target method
is static.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param parameters the parameters to be sent to the method. Leave the parameter which
is supposed to be a provided Handle as null and a new Handle will be created and
provided.
@return a Handle to the task which is executing the Method.
*/
public static Handle invokeAsync(Object targetOrClass, String nameAndParametersAndType, Object[] parameters)
{
	return new AsyncTask().invoke(targetOrClass,nameAndParametersAndType, parameters);
}
/**
This method invokes the method in the background returning a Handle used to monitor
the running task.
@param targetOrClass The target object or a Class object if the target method
is static.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param parameters the parameters to be sent to the method. Leave the parameter which
is supposed to be a provided Handle as null and a new Handle will be created and
provided.
@return a Handle to the task which is executing the Method.
*/
public static Handle invokeAsync(Object target, Method method, Object[] parameters)
{
	return new AsyncTask().setMethod(method).invoke(target,parameters);
}
/**
 * Get a field value using a ewe.sys.Wrapper instead of a standard Java wrapper.
 * @param target the target Object.
 * @param field the Field to get.
 * @param destination the Wrapper that will hold the data read in.
 * @return the destination or a new Wrapper if the destination was null.
 * @throws IllegalAccessException
 * @throws IllegalArgumentException
 */
public static Wrapper getFieldValue(Object target, Field field, Wrapper destination)
throws IllegalAccessException, IllegalArgumentException
{
	if (destination == null) destination = new Wrapper();
	destination.fromJavaWrapper(field.get(target));
	return destination;
	/*
	if (hasWrapperMethods == -1) checkHasWrapperMethods();
	if (hasWrapperMethods == 0){
		//Must use Java methods.
		destination.fromJavaWrapper(field.get(target));
		return destination;
	}
	ewe.reflect.Field ef = (ewe.reflect.Field)NativeAccess.getSetVariable(field,null,true);
	ef.getValue(target,destination);
	return destination;
	*/
}
/**
 * Set a field value using a ewe.sys.Wrapper instead of a standard Java wrapper.
 * @param target the target Object.
 * @param field the Field to set.
 * @param data the Wrapper containing the data to be set.
 * @throws IllegalAccessException
 */
public static void setFieldValue(Object target, Field field, Wrapper data)
throws IllegalAccessException, IllegalArgumentException
{
	if (data == null) throw new NullPointerException();
	field.set(target,data.toJavaWrapper());
	return;
	/*
	if (hasWrapperMethods == -1) checkHasWrapperMethods();
	if (hasWrapperMethods == 0){
		//Must use Java methods.
		field.set(target,data.toJavaWrapper());
		return;
	}
	ewe.reflect.Field ef = (ewe.reflect.Field)NativeAccess.getSetVariable(field,null,true);
	ef.setValue(target,data);
	*/
}
/**
Create a new instance of an object, but do not throw an exception on failure,
return null instead.
@param c The class to create the new instance of.
@param parameterSpecs The java encoded parameter list for the constructor (e.g. "[BII").
@param parameters the list of parameters using standard Java wrappers or you can use ewe.sys.Wrapper objects.
If you use ewe.sys.Wrapper objects, the array must be of the type Wrapper[].
@return The new Object if successful or null if not.
*/
//===================================================================
public static Object newInstance(Class c,String parameterSpecs,Object[] parameters)
//===================================================================
{
	if (parameters == null) parameters = emptyParameters;
	Constructor cs = parameterSpecs != null ? getConstructor(c,parameterSpecs) : getDefaultConstructor(c);
	if (cs == null) return null;
	try{
		if (parameters instanceof Wrapper[]) return newInstance(cs,(Wrapper[])parameters);
		else return cs.newInstance(parameters);
	}catch(Exception e){
		return null;
	}
}
/**
Create a new instance of an object, but do not throw an exception on failure,
return null instead.
@param c The class to create the new instance of.
@param parameterSpecs The java encoded parameter list for the constructor (e.g. "[BII").
@param parameters The parameters for the constructor in an array.
@return The new Object if successful or null if not.
*/
/*
//===================================================================
public static Object newInstance(Class c,String parameterSpecs,Wrapper[] parameters)
//===================================================================
{
	if (parameters == null) parameters = Wrapper.noParameter;
	if (hasWrapperMethods == -1) checkHasWrapperMethods();
	if (hasWrapperMethods == 0){
		//Must use Java methods.

	}
	Constructor cs = parameterSpecs != null ? getConstructor(c,parameterSpecs) : getDefaultConstructor(c);
	if (cs == null) return null;
	try{
		return cs.newInstance(parameters);
	}catch(Exception e){
		return null;
	}
}
*/
/**
Create a new instance of an object, but do not throw an exception on failure,
return null instead.
@param c The class to create the new instance of.
@return The new Object if successful or null if not.
*/
//===================================================================
public static Object newInstance(Class c)
//===================================================================
{
	return newInstance(c,null,null);
}

/**
 * Attempt to get a class using all possible class loaders.
 * @param name The class name. This can be a Java encoded name or a non-encoded
 * name, and can specify an array, but it must not be a primitive value. For primitive
 * values use Reflection.forEncodedName()
 * @return a Class or null if it is not found.
 */
//===================================================================
public static Class forName(String name)
//===================================================================
{
	/*
	if (hasWrapperMethods == -1) checkHasWrapperMethods();
	if (hasWrapperMethods == 0)
		try{
			return Class.forName(name);
		}catch(ClassNotFoundException e){
		}
	else{
		Class ret = NativeAccess.systemForName(name);
		if (ret != null) return ret;
	}
	*/
	return Reflect.loadClass(name);
}
/**
 * Attempt to get a class using the system class loader and the class loader
 * of the requestor class.
 * @param name The class name. This can be a Java encoded name or a non-encoded
 * name, and can specify an array, but it must not be a primitive value. For primitive
 * values use Reflection.forEncodedName().
 * @param requestor The requesting class.
 * @return a Class or null if it is not found.
 */
//===================================================================
public static Class forName(String name,Class requestor)
//===================================================================
{
	return Reflect.loadClass(name);
}

/**
Convert a Java encoded type into a Class representing the type.
 * @param name the Java encoded type name.
 * @return a Class representing the name or null if the class was not found.
 */
//===================================================================
public static Class forEncodedName(String name)
//===================================================================
{
	switch(name.charAt(0)){
		case 'V': return Void.TYPE;
		case 'Z': return Boolean.TYPE;
		case 'B': return Byte.TYPE;
		case 'C': return Character.TYPE;
		case 'S': return Short.TYPE;
		case 'I': return Integer.TYPE;
		case 'J': return java.lang.Long.TYPE;
		case 'F': return Float.TYPE;
		case 'D': return java.lang.Double.TYPE;
		case '[': return forName(name.replace('/','.'));
		case 'L': return forName(name.substring(1,name.length()-1).replace('/','.'));
		default: return null;
	}
}
/**
 * Convert a Java type represented by the Class as the first letter of the java encoded type of the class.
 * @param type The class to represent as an encoded type.
 * @return the first letter of the java encoded type of the class. These are the same
 * values as the Wrapper data types (e.g. Wrapper.INT which is the same as 'I').
 */
//===================================================================
public static char getEncodedType(Class type)
//===================================================================
{
	if (type.isPrimitive()){
		if (type == Void.TYPE) return 'V';
		else if (type == Boolean.TYPE) return 'Z';
		else if (type == Byte.TYPE) return 'B';
		else if (type == Character.TYPE) return 'C';
		else if (type == Short.TYPE) return 'S';
		else if (type == Integer.TYPE) return 'I';
		else if (type == Long.TYPE) return 'J';
		else if (type == Float.TYPE) return 'F';
		else if (type == Double.TYPE) return 'D';
		else throw new IllegalArgumentException();
	}else if (type.isArray()) return '[';
	else return 'L';
}
/**
 * Exactly the same as getEncodedType(). Converts a Java type represented by the Class as the first letter of the java encoded type of the class.
 * @param type The class to represent as an encoded type.
 * @return the first letter of the java encoded type of the class. These are the same
 * values as the Wrapper data types (e.g. Wrapper.INT which is the same as 'I').
 */
public static char getWrapperType(Class type)
{
	return getEncodedType(type);
}
/**
 * Convert a Java type represented by the Class as a java encoded type string.
 * @param type The class to represent as an encoded type.
 * @return  a java encoded type string.
 */
//===================================================================
public static String getEncodedName(Class type)
//===================================================================
{
	if (type == Void.TYPE) return "V";
	else if (type == Boolean.TYPE) return "Z";
	else if (type == Byte.TYPE) return "B";
	else if (type == Character.TYPE) return "C";
	else if (type == Short.TYPE) return "S";
	else if (type == Integer.TYPE) return "I";
	else if (type == Long.TYPE) return "J";
	else if (type == Float.TYPE) return "F";
	else if (type == Double.TYPE) return "D";
	else if (type.isArray())
		return type.getName().replace('.','/');
	else
		return "L"+type.getName().replace('.','/')+";";
}
/**
 * This returns an encoded class name, but <b>without</b> the leading 'L' and
 * trailing ';' unless the class is an array of objects, in which case the 'L'
 * and ';' are left in the component specifier.
 * <p>
 * This will only work for arrays and classes - not primitive values.
 * @param type the Class to get the encoded name of.
 * @return the encoded name without a leading 'L' or trailing ';'
 * @throws IllegalArgumentException if the type is primitive.
 */
public static String getEncodedClassNameOnly(Class type) throws IllegalArgumentException
{
	if (type.isPrimitive()) throw new IllegalArgumentException();
	return type.getName().replace('.','/');
}
/**
Given a Class, find the superclass which ends with targetName.
 * @param actualClass the Class to search for.
 * @param targetName the target class name.
 * @return the Class found or null if not found.
 */
//===================================================================
public static Class getBaseClass(Class actualClass,String targetName)
//===================================================================
{
	for (Class c = actualClass; c != null; c = c.getSuperclass()){
		String n = c.getName();
		//Vm.debug("Name: "+n+", Target: "+targetName);
		if (!n.endsWith(targetName)) continue;
		int before = n.length()-targetName.length()-1;
		if (before < 0) return c;
		char ch = n.charAt(before);
		if (ch == '$' || ch == '.') return c;
	}
	return null;
}
/*
//===================================================================
public static Class encodedTypeToClass()
//===================================================================
*/
/**
Convert an encoded paramter list into an array of chars representing the
parameter types as encoded types.
 * @param encodedTypes a list of parameter types which may or may not be enclosed in "()" brackets.
 * @return An array of chars representing the
parameter types as encoded types.
 */
//===================================================================
public static char[] getParameterTypesAsEncodedType(String encodedTypes)
//===================================================================
{
	StringBuffer buff = new StringBuffer();
	char[] got = Vm.getStringChars(encodedTypes);
	int s = encodedTypes.indexOf('(');
	if (s == -1) s = 0;
	if (got[s] == '(') s++;
	int e = s;
	while(e < got.length && got[e] != ')'){
		if (got[e] == '['){
			while(got[e] == '[') e++;
			if (got[e] == 'L')
				while(got[e] != ';') e++;
			buff.append('[');
		}else if (got[e] == 'L'){
			while(got[e] != ';') e++;
			buff.append('L');
		}else
			buff.append(got[e]);
		e++;
		s = e;
	}
	char[] ret = new char[buff.length()];
	buff.getChars(0,ret.length,ret,0);
	return ret;
}

/**
Convert an encoded paramter list into an array of Classes representing the
parameter types.
 * @param encodedTypes a list of parameter types which may or may not be enclosed in "()" brackets.
 * @return An array of Class objects representing the types or null if any type is not found.
 */
//===================================================================
public static Class[] getParameterTypes(String encodedTypes)
//===================================================================
{
	if (encodedTypes == null || encodedTypes.length() == 0)
		return emptyClasses;
	Vector v = new Vector();
	char[] got = Vm.getStringChars(encodedTypes);
	int s = encodedTypes.indexOf('(');
	if (s == -1) s = 0;
	if (got[s] == '(') s++;
	int e = s;
	while(e < got.length && got[e] != ')'){
		if (got[e] == '['){
			while(got[e] == '[') e++;
			if (got[e] == 'L')
				while(got[e] != ';') e++;
		}else if (got[e] == 'L'){
			while(got[e] != ';') e++;
		}
		e++;
		Class c = forEncodedName(new String(got,s,e-s));
		if (c == null) return null;
		v.add(c);
		s = e;
	}
	Class[] ret = new Class[v.size()];
	v.copyInto(ret);
	return ret;
}
/**
	Get the return or field type as a class.
 * @param encodedParameterSpecsOrType either a single encoded type or parameters in brackets
	 followed by a single encoded type.
 * @return a Class representing the field or return type.
 */
//===================================================================
public static Class getReturnOrFieldType(String encodedParameterSpecsOrType)
//===================================================================
{
	int idx = encodedParameterSpecsOrType.lastIndexOf(')');
	if (idx == -1) idx = encodedParameterSpecsOrType.lastIndexOf(':');
	if (idx != -1) encodedParameterSpecsOrType = encodedParameterSpecsOrType.substring(idx+1);
	if (encodedParameterSpecsOrType.length() == 0) return null;
	return forEncodedName(encodedParameterSpecsOrType);
}

/**
This will find and invoke a method on an object or a static method on a class.
@param cl The class of the Object.
@param target The target Object, which may be null for static methods.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param parameters the list of parameters using standard Java wrappers or you can use ewe.sys.Wrapper objects,
but the array must be of the type Wrapper[].
@param declaredOnly set true if you are looking for methods declared by that Class.
@return if unsuccessful for any reason this returns null otherwise a Wrapper containing the
returned value is returned, or if the method is a void method, a Wrapper with type Wrapper.VOID
is returned.
@return
*/
//===================================================================
public static Wrapper invokeMethod(Class cl, Object target, String nameAndParametersAndType, Object[] parameters, boolean declaredOnly)
//===================================================================
{
	try{
		if (parameters == null) parameters = emptyParameters;
		if (parameters instanceof Wrapper[]){
			Object[] tp = new Object[parameters.length];
			for (int i = 0; i<tp.length; i++){
				Wrapper w = (Wrapper)parameters[i];
				tp[i] = w == null ? null : w.toJavaWrapper();
			}
			parameters = tp;
		}
		Method got = getMethod(cl,nameAndParametersAndType,declaredOnly);
		if (got == null) return null;
		Object ret = got.invoke(target,parameters);
		if (got.getReturnType().equals(Void.TYPE)) return new Wrapper().zero(Wrapper.VOID);
		Wrapper w2 = new Wrapper();
		w2.fromJavaWrapper(ret);
		return w2;
	}catch(Exception e){
		return null;
	}
}
/**
This will find and invoke a method on an object or a static method on a class.
@param objectOrClass for a static method this should be a Class object, for an instance method
it should be an instance of the class.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param parameters the list of parameters using standard Java wrappers or you can use ewe.sys.Wrapper objects,
but the array must be of the type Wrapper[].
@return if unsuccessful for any reason this returns null otherwise a Wrapper containing the
returned value is returned, or if the method is a void method, a Wrapper with type Wrapper.VOID
is returned.
*/
//===================================================================
public static Wrapper invokeMethod(Object objectOrClass, String nameAndParametersAndType, Object[] parameters)
//===================================================================
{
	return invokeMethod(toClass(objectOrClass),toObject(objectOrClass),nameAndParametersAndType,parameters,false);
}
/**
 * Find a Class method and optionally confirm the return type.
 * @param c The Class to look in.
 * @param name the name of the method.
 * @param parameters The parameter list of the method.
 * @param returnType The optional return type. If it is null the return type is not checked.
 * @param declaredOnly set true if you are looking for methods declared by that Class.
 * @return the matching method or null if none was found.
 */
//===================================================================
public static Method getMethod(Class c,String name,Class[] parameters,Class returnType,boolean declaredOnly)
//===================================================================
{
	try{
		Method m = declaredOnly ? c.getDeclaredMethod(name,parameters) : c.getMethod(name,parameters);
		if (returnType == null) return m;
		if (!m.getReturnType().equals(returnType)) return null;
		return m;
	}catch(Exception e){
		return null;
	}
}
/**
 * Find a Class method and optionally confirm the return type.
 * @param c The Class to look in.
 * @param name the name of the method.
 * @param parametersAndReturnType The parameter list and optional return type of the method as a Java encoded string.
 * @param declaredOnly set true if you are looking for methods declared by that Class.
 * @return the matching method or null if none was found.
 */
//===================================================================
public static Method getMethod(Class c,String name,String parametersAndReturnType,boolean declaredOnly)
//===================================================================
{
	Class[] parameters = getParameterTypes(parametersAndReturnType);
	Class returnType = getReturnOrFieldType(parametersAndReturnType);
	return getMethod(c,name,parameters,returnType,declaredOnly);
}
/**
 * Find a Class method and optionally confirm the return type.
 * @param c The Class to look in.
 * @param nameAndParametersAndReturnType The name and parameter list and optional return type of the method as a Java encoded string.
 * @param declaredOnly set true if you are looking for methods declared by that Class.
 * @return the matching method or null if none was found.
 */
//===================================================================
public static Method getMethod(Class c,String nameAndParametersAndReturnType,boolean declaredOnly)
//===================================================================
{
	int idx = nameAndParametersAndReturnType.indexOf('(');
	if (idx == -1) return null;
	String name = nameAndParametersAndReturnType.substring(0,idx);
	String parametersAndReturnType = nameAndParametersAndReturnType.substring(idx);
	Class[] parameters = getParameterTypes(parametersAndReturnType);
	Class returnType = getReturnOrFieldType(parametersAndReturnType);
	return getMethod(c,name,parameters,returnType,declaredOnly);
}

/**
 * Find a Class constructor.
 * @param c The Class to look in.
 * @return the matching Constructor or null if none was found.
 */
//===================================================================
public static Constructor getDefaultConstructor(Class c)
//===================================================================
{
	return getConstructor(c,emptyClasses);
}
/**
 * Find a Class constructor.
 * @param c The Class to look in.
 * @param parameters The parameter list of the constructor.
 * @return the matching Constructor or null if none was found.
 */
//===================================================================
public static Constructor getConstructor(Class c,Class[] parameters)
//===================================================================
{
	try{
		return c.getDeclaredConstructor(parameters);
	}catch(Exception e){
		return null;
	}
}
/**
 * Find a Class constructor.
 * @param c The Class to look in.
 * @param parameters The parameter list of the constructor as a Java Type Encoded String - with
 * or without the brackets - i.e. "BII" or "(BII)".
 * @return the matching Constructor or null if none was found.
 */
//===================================================================
public static Constructor getConstructor(Class c,String parameters)
//===================================================================
{
	Class[] pars = getParameterTypes(parameters);
	return getConstructor(c,pars);
}
/**
Get a class Field and optionally verify its type.
 * @param c The class to search for the field.
 * @param name the name of the field.
 * @param type the optional type of the field. If it is null the type of the field will not
	 be verified.
 * @param declaredOnly set true if you are looking for fields declared by that Class.
 * @return the Field found or null if not found.
 */
//===================================================================
public static Field getField(Class c,String name,Class type,boolean declaredOnly)
//===================================================================
{
	try{
		Field f = declaredOnly ? c.getDeclaredField(name) : c.getField(name);
		if (type == null) return f;
		if (!f.getType().equals(type)) return null;
		return f;
	}catch(Exception e){
		return null;
	}
}
/**
Get a class Field and optionally verify its type.
 * @param c The class to search for the field.
 * @param name the name of the field.
 * @param type the optional type of the field in Java type encoded format.
 	If the type of the field is '$' it is assumed to mean java.lang.String.
	If it is null the type of the field will not be verified.
 * @param declaredOnly set true if you are looking for fields declared by that Class.
 * @return the Field found or null if not found.
 */
//===================================================================
public static Field getField(Class c,String name,String type,boolean declaredOnly)
//===================================================================
{
	if (type != null && type.equals("$")) type = encodedStringClass;
	return getField(c,name,type == null ? null : forEncodedName(type),declaredOnly);
}
/**
Get a class Field and optionally verify its type.
 * @param c The class to search for the field.
 * @param nameAndType the name of the field optionally followed by a ':' and the type of the field.
 If the type of the field is '$' it is assumed to mean java.lang.String. If no ':' and
field type is present the type of the field will not be verified.
 * @param declaredOnly set true if you are looking for fields declared by that Class.
 * @return the Field found or null if not found.
 */
//===================================================================
public static Field getField(Class c,String nameAndType,boolean declaredOnly)
//===================================================================
{
	int idx = nameAndType.indexOf(':');
	String name = idx == -1 ? nameAndType : nameAndType.substring(0,idx);
	String type = idx == -1 ? null : nameAndType.substring(idx+1);
	return getField(c,name,type == null ? null : forEncodedName(type),declaredOnly);
}
/**
Get a field value.
@param cl The class to get the field value from.
@param target The target object to get the field value from, which can be null for static fields.
@param fieldNameAndType the name of the field optionally followed by a ':' and the type of the field.
If the type of the field is '$' it is assumed to mean java.lang.String. If no ':' and
field type is present the type of the field will not be verified.
@param declaredOnly set true if you are looking for fields declared by that Class.
@return the field value stored in a Wrapper object, or null if the method fails for any reason.
*/
//===================================================================
public static Wrapper getFieldValue(Class cl, Object target, String fieldNameAndType, boolean declaredOnly)
//===================================================================
{
	try{
		Field f = getField(cl,fieldNameAndType,declaredOnly);
		if (f == null) return null;
		Object got = f.get(target);
		Wrapper w2 = new Wrapper();
		w2.fromJavaWrapper(got);
		return w2;
	}catch(Exception e){
		return null;
	}
}
/**
Get a field value.
@param objectOrClass for a static field this should be a Class object, for an instance field
it should be an instance of the class.
@param fieldNameAndType the name of the field optionally followed by a ':' and the type of the field.
If the type of the field is '$' it is assumed to mean java.lang.String. If no ':' and
field type is present the type of the field will not be verified.
@param declaredOnly set true if you are looking for fields declared by that Class.
@return the field value stored in a Wrapper object, or null if the method fails for any reason.
*/
//===================================================================
public static Wrapper getFieldValue(Object classOrObject, String fieldNameAndType)
//===================================================================
{
	return getFieldValue(toClass(classOrObject),toObject(classOrObject),fieldNameAndType,false);
}
/**
Set a field value.
@param cl The class to set the field value.
@param target The target object to set the field value, which can be null for static fields.
@param fieldNameAndType the name of the field optionally followed by a ':' and the type of the field.
If the type of the field is '$' it is assumed to mean java.lang.String. If no ':' and
field type is present the type of the field will not be verified.
@param declaredOnly set true if you are looking for fields declared by that Class.
@param value the value to set as a Java Wrapper object or an ewe.sys.Wrapper object.
@return true on success, false if the method fails for any reason.
*/
//===================================================================
public static boolean setFieldValue(Class cl, Object target, String fieldNameAndType, boolean declaredOnly, Object value)
//===================================================================
{
	try{
		Field f = getField(cl,fieldNameAndType,declaredOnly);
		if (f == null) return false;
		Object toSet = value instanceof Wrapper ? ((Wrapper)value).toJavaWrapper() : value;
		f.set(target,toSet);
		return true;
	}catch(Exception e){
		return false;
	}
}
/**
Set a field value.
@param objectOrClass for a static field this should be a Class object, for an instance field
it should be an instance of the class.
@param fieldNameAndType the name of the field optionally followed by a ':' and the type of the field.
If the type of the field is '$' it is assumed to mean java.lang.String. If no ':' and
field type is present the type of the field will not be verified.
@param value the value to set as a Java Wrapper object or an ewe.sys.Wrapper object.
@return true on success, false if the method fails for any reason.
*/
//===================================================================
public static boolean setFieldValue(Object classOrObject, String fieldNameAndType, Object value)
//===================================================================
{
	return setFieldValue(toClass(classOrObject),toObject(classOrObject),fieldNameAndType,false,value);
}
//public static
/**
Create a zero value Java wrapper object.
@param type the Class representing the type of the data.
@return a zero value Java wrapper object.
*/
//===================================================================
public static Object createZeroValue(Class type)
//===================================================================
{
	Object value = null;
	if (type.isPrimitive()){
		if (type == Boolean.TYPE) value = Boolean.FALSE;
		else if (type == Byte.TYPE) value = new Byte((byte)0);
		else if (type == Character.TYPE) value = new Character((char)0);
		else if (type == Short.TYPE) value = new Short((short)0);
		else if (type == Integer.TYPE) value = new Integer(0);
		else if (type == Long.TYPE) value = new java.lang.Long(0);
		else if (type == Float.TYPE) value = new Float(0);
		else if (type == Double.TYPE) value = new java.lang.Double(0);
	}
	return value;
}
/**
See if the Class c is of the type aType. This simply calls aType.isAssignableFrom(c);
@param c The class to check.
@param aType the type.
@return true if c is of the specified type, false if not.
*/
//===================================================================
public static boolean isTypeOf(Class c, Class aType)
//===================================================================
{
	if (aType == null) return false;
	return aType.isAssignableFrom(c);
}
/**
Check if a Class is of the type aType, where aType is a Java encoded name.
@param c The class to check.
@param aType the Java encoded name of a type.
@return true if c is of the specified type, false if not.
*/
//===================================================================
public static boolean isTypeOf(Class c, String aType)
//===================================================================
{
	Class at = forEncodedName(aType);
	if (at == null) return false;
	return isTypeOf(c,at);
}
/**
Check if a Class is of the type aType, where aType is a Java encoded name.
@param c The class to check.
@param aType the Java encoded name of a type.
@return true if c is of the specified type, false if not.
*/
//===================================================================
public static boolean isTypeOf(String c,String aType)
//===================================================================
{
	if (c.length() == 1){
		if (aType.length() == 1) return c.charAt(0) == aType.charAt(0);
		return false;
	}
	Class cc = forEncodedName(c);
	if (c == null) return false;
	return isTypeOf(c,aType);
}
/**
* This copies one field from the source object to the destination object using the
* provided Reflect Object as the class specifier. If the
* field value being copied implements the Copyable interface, then getCopy() is called
* on the Object and the copy is assigned to the destination field. Otherwise the destination
* field is made equal to the source field.
* <p>
* In order for this to work the Class AND the Field must be public.
* @param field The field name.
* @param source The source object.
* @param dest The destination object.
* @param theClass A Class object representing the exact class which is having its field copied.
@param buffer an optional Wrapper that can be used as a buffer for the field transfer.
@return true if the copy was successful, false if not.
*/
//===================================================================
public static boolean copyField(String field,Object source,Object dest,Class theClass)
//===================================================================
{
	return copyField(field,source,dest,theClass,null);
}
/**
* This copies one field from the source object to the destination object using the
* provided Reflect Object as the class specifier. If the
* field value being copied implements the Copyable interface, then getCopy() is called
* on the Object and the copy is assigned to the destination field. Otherwise the destination
* field is made equal to the source field.
* <p>
* In order for this to work the Class AND the Field must be public.
* @param field The field name.
* @param source The source object.
* @param dest The destination object.
* @param theClass A Class object representing the exact class which is having its field copied.
@param buffer an optional Wrapper that can be used as a buffer for the field transfer.
@return true if the copy was successful, false if not.
*/
//===================================================================
public static boolean copyField(String field,Object source,Object dest,Class theClass,Wrapper buffer)
//===================================================================
{
	try{
		Field f = theClass.getDeclaredField(field);
		if (buffer == null) buffer = new Wrapper();
		if (!buffer.getFromField(f,source)) return false;
		if (buffer.getType() == buffer.OBJECT){
			Object got = buffer.getObject();
			if (got instanceof Copyable) buffer.setObject(((Copyable)got).getCopy());
		}
		return buffer.putInField(f,dest);
	}catch(Exception e){
		return false;
	}
}
/**
* This copies data from the source to the destination. Copying is done as
* follows. The class of the source is determined using the Reflection API. If
* that class DECLARES a "_fields" String variable (which must be public to
* work under Java) then a copyField() is done on each of the fields listed in
* the "_fields" variable. If no "_fields" variable is declared, all public fields
* will be copied. The value of this variable should be a comma separated
* list of fields. This process is repeated for each successive superclass of
* the source object.
**/
//===================================================================
public static boolean copy(Object source,Object dest)
//===================================================================
{
	if (source == null || dest == null) return false;
	Vector fields = new Vector();
	Wrapper w = new Wrapper();
	for (Class r = source.getClass(); r != null; r = r.getSuperclass()){
		String s = ewe.data.DataObject.getFieldList(r,source,true);
		if (s.length() == 0) continue;
		fields.clear();
		mString.split(s,',',fields);
		int l = fields.size();
		for (int i = 0; i<l; i++)
			copyField((String)fields.get(i),source,dest,r,w);
	}
	return true;
}

/**
A number of methods take either a Class specifying a type or an example object of the
type as a parameter. If the sampleObjectOrClass is a Class it will return that, otherwise
it will call getClass() on it and return that (if it is not null).
@param sampleObjectOrClass The Class of a type of an example object of the type.
@return The Class of the type.
*/
//===================================================================
public static Class toClass(Object sampleObjectOrClass)
//===================================================================
{
	if (sampleObjectOrClass instanceof Class) return (Class)sampleObjectOrClass;
	if (sampleObjectOrClass == null) return null;
	return sampleObjectOrClass.getClass();
}
/**
A number of methods take either a Class specifying a type or an example object of the
type as a parameter. If the sampleObjectOrClass is a Class it will return null, otherwise
it will return the sampleObjectOrClass.
@param sampleObjectOrClass The Class of a type of an example object of the type.
@return The sampleObjectOrClass if it is not a Class.
*/
//===================================================================
public static Object toObject(Object sampleObjectOrClass)
//===================================================================
{
	if (sampleObjectOrClass instanceof Class) return null;
	return sampleObjectOrClass;
}
/**
 * Convert an integer value into a Wrapper appropriate for
 * the specified primitive type, which should be byte, short, char, int, long or boolean
 * @param primitiveType the primitive type.
 * @param value the value to be wrapped.
 * @return the wrapper value.
 * @throws IllegalArgumentException if the value cannot be converted
 * to the primitive type.
 */
public static Object toWrapper(Class primitiveType, long value)
throws IllegalArgumentException
{
	Class c = primitiveType;
	if (c == Byte.TYPE) return new Byte((byte)value);
	if (c == Short.TYPE) return new Short((short)value);
	if (c == Character.TYPE) return new Character((char)value);
	if (c == Integer.TYPE) return new Integer((int)value);
	if (c == Long.TYPE) return new java.lang.Long((long)value);
	if (c == Boolean.TYPE) return value == 0 ? Boolean.FALSE : Boolean.TRUE;
	throw new IllegalArgumentException();
}
/**
 * Convert a floating point value into a Wrapper appropriate for
 * the specified primitive type, which should be double or float.
 * @param primitiveType the primitive type.
 * @param value the value to be wrapped.
 * @return the wrapper value.
 * @throws IllegalArgumentException if the value cannot be converted
 * to the primitive type.
 */
public static Object toWrapper(Class primitiveType, double value)
throws IllegalArgumentException
{
	Class c = primitiveType;
	if (c == Double.TYPE) return new java.lang.Double((double)value);
	if (c == Float.TYPE) return new Float((float)value);
	throw new IllegalArgumentException();
}
/**
 * Convert an char value into a Wrapper appropriate for
 * the specified primitive type, which should be byte, short, char, int, long or boolean
 * @param primitiveType the primitive type.
 * @param value the value to be wrapped.
 * @return the wrapper value.
 * @throws IllegalArgumentException if the value cannot be converted
 * to the primitive type.
 */
public static Object toWrapper(Class primitiveType, char value)
throws IllegalArgumentException
{
	return toWrapper(primitiveType,(long)value);
}
/**
 * Convert a boolean value into a Wrapper appropriate for
 * the specified primitive type, which should be boolean.
 * @param primitiveType the primitive type.
 * @param value the value to be wrapped.
 * @return the wrapper value.
 * @throws IllegalArgumentException if the value cannot be converted
 * to the primitive type.
 */
public static Object toWrapper(Class primitiveType, boolean value)
throws IllegalArgumentException
{
	Class c = primitiveType;
	if (c == Boolean.TYPE) return value ? Boolean.TRUE : Boolean.FALSE;
	throw new IllegalArgumentException();
}
public static long unwrapLong(Object value)
throws IllegalArgumentException
{
	if (value instanceof Byte) return ((Byte)value).longValue();
	if (value instanceof Short) return ((Short)value).longValue();
	if (value instanceof Character) return ((Character)value).charValue();
	if (value instanceof Integer) return ((Integer)value).longValue();
	if (value instanceof Long) return ((java.lang.Long)value).longValue();
	if (value instanceof Boolean) return ((Boolean)value).booleanValue() ? 1 : 0;
	throw new IllegalArgumentException();
}
public static double unwrapDouble(Object value)
throws IllegalArgumentException
{
	if (value instanceof Float) return ((Float)value).doubleValue();
	if (value instanceof Double) return ((java.lang.Double)value).doubleValue();
	throw new IllegalArgumentException();
}
public static boolean unwrapBoolean(Object value)
{
	if (value instanceof Boolean) return ((Boolean)value).booleanValue();
	throw new IllegalArgumentException();
}
/**
 * This returns the best object to get reference info on a particular class.
 * if data is null it will return classOrReflect, otherwise it will return data.
 * @param data An instance of a class.
 * @param classOrReflect A Class or Reflect object for the class.
 * @return the data if it is not null, or the classOrReflect object.
 */
//===================================================================
public static Object bestReference(Object data,Object classOrReflect)
//===================================================================
{
	if (data != null) return data;
	return classOrReflect;
}

//##################################################################
}
//##################################################################

