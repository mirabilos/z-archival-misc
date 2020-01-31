/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.reflect;
import ewe.util.Vector;
import ewe.util.mClassLoader;
/**
ewe.reflect.Reflect provides methods for certain Class reflection operations which are not provided by
the Class object in Ewe. Note some important differences between Class and Reflect:
<p>
<b>1.</b> Reflect.forName() can load classes which are system classes (in the Ewe Library) AND
classes which are contained in the ".ewe" files as specified in the command line. However Class.forName()
can ONLY load system Classes. Therefore Reflect.forName() is a better way to locate reflection information
for your application classes. <b>Neither</b> Reflect.forName() nor Class.forName() can load classes which
have been loaded by a ClassLoader object.
<p>
<b>2.</b> You cannot use Reflect to represent primitive types - while you can with Class. Attempting to create
a Reflect with a Class that represents a primitive type will throw a RuntimeException.
<p>
**/
//##################################################################
public final class Reflect{
//##################################################################
//-------------- 3 Native variables, do not move.
int nativeClass;
String className;
Object jlClass;
//---------------------------------------
// Used by Java version. Ignored by native version.
//protected java.lang.Class class == null;
protected Constructor defaultConstructor = null;
protected Reflect mySuperClass;

/**
* An option for retrieving Fields, Methods and Constructors. This can be used OR PUBLIC.
**/
public static final int DECLARED = 1;
/**
* An option for retrieving Fields, Methods and Constructors. This can be used OR DECLARED.
**/
public static final int PUBLIC = 2;

//-------------------------------------------------------------------
private Reflect(){nativeClass = 0; jlClass = className = null;}
//-------------------------------------------------------------------
private static Vector loaders;
/**
* This attempts to get a Class first using the system and then using any
* application class loaders.
* @param name The name of the class.
* @return The resolved Class or null if not found.
*/
//===================================================================
public static Class loadClass(String name)
//===================================================================
{
	Reflect r = getForName(name);
	if (r != null) return r.getReflectedClass();
	if (loaders == null) loaders = new Vector();
	int num = mClassLoader.getClassLoaders(loaders);
	if (num == 0) return null;
	if (name.charAt(0) == 'L' && name.charAt(name.length()-1) == ';')
		name = name.substring(1,name.length()-1);
	for (int i = 0; i<num; i++){
		try{
			return ((ClassLoader)loaders.get(i)).loadClass(name);
		}catch(Exception e){
		}
	}
	return null;
}
/**
* This attempts to get a Reflect first using the system and then using any
* application class loaders.
* @param name The name of the class.
* @return The resolved Reflect or null if not found.
*/
//===================================================================
public static Reflect loadForName(String name)
//===================================================================
{
	Class c = loadClass(name);
	if (c == null) return null;
	return new Reflect(c);
}
/**
* This creates a new instance of a Class given the class name. All active ClassLoaders
* are also used to try to resolve the Class.
* @param name The name of the class.
* @return a new instance of the class or null if it could not be created.
*/
//===================================================================
public static Object newInstance(String name)
//===================================================================
{
	Reflect r = getForName(name);
	if (r != null) return r.newInstance();
	try{
		return loadClass(name).newInstance();
	}catch(Exception e){
		return null;
	}
}
//===================================================================
public static Object newArrayInstance(Class c,int length)
//===================================================================
{
	return newArrayInstance(getType(c),length);
}

/**
 * Get a Reflect object that represents this class. Will work for Objects and Arrays.
 * @param obj The object - should not be null.
 * @return A Reflect object if successful.
 */
//===================================================================
public static Reflect getForObject(Object obj) {if (obj == null) throw new NullPointerException(); return nativeGetForObject(obj);}
//===================================================================
/**
 * Get a Reflect object that represents the specified class name. It is better to call this method
	rather than Class.forName() because under Java, Class.forName() cannot find classes which are bound in a ".ewe"
	file - even though it is considered an application class.<p>
	This can only find classes that are not loaded from a class loader.
 * @param name The name of the class.
 * @return A Reflect object if successful - null otherwise.
 */
//===================================================================
public static Reflect getForName(String name)
//===================================================================
{
	return getForName(name,null);
}
/**
 * Get a Reflect object that represents the specified class name. It is better to call this method
	rather than Class.forName() because under Java, Class.forName() cannot find classes which are bound in a ".ewe"
	file - even though it is considered an application class.<p>
	If the class is not an application class or system class AND the requestor Class has been loaded
	via a ClassLoader - then the ClassLoader is checked to see if it can locate the requested class.
 * @param name The name of the class.
 * @param requestor A requesting class.
* @return A Reflect object if successful - null otherwise.
 */
//===================================================================
public static Reflect getForName(String name,Class requestor)
//===================================================================
{
	if (name.charAt(name.length()-1) == ';' && name.charAt(0) == 'L') name = name.substring(1,name.length()-1);
	//if (name.endsWith(";")) name = name.substring(1,name.length()-1);
	Reflect r = nativeGetForName(name);
	if (r != null) return r;
	if (requestor == null) return null;
	ClassLoader cl = requestor.getClassLoader();
	if (cl == null) return null;
	try{
		return new Reflect(cl.loadClass(name));
	}catch(Exception e){
		return null;
	}
}
/**
 * Create a Reflect object given a non-null and non-primitive Class.
 * @param aClass The class for the object which must be non-null and non-primitive.
 */
//===================================================================
public Reflect(Class aClass)
//===================================================================
{
	this();
	if (aClass == null) throw new NullPointerException();
	if (aClass.isPrimitive()) throw new RuntimeException("You cannot create a Reflect object to represent a primitive value.");
	nativeSetup(aClass);
}
/**
* This returns the className in '/' notation.
**/
//===================================================================
public String getClassName() {return className;}
//===================================================================

//-------------------------------------------------------------------
private native void nativeSetup(Class aClass);
//-------------------------------------------------------------------
/**
* This returns a Class object representing the class being reflected.
**/
//===================================================================
public native Class getReflectedClass();
//===================================================================

private static native Reflect nativeGetForObject(Object obj);
private static native Reflect nativeGetForName(String name);

//===================================================================
public String toString() {return getClassName();}
//===================================================================
/**
 * Look for a Field in the class - either instance or static.
 * @param name The name of the field.
 * @param options Either DECLARED or PUBLIC.
 * @return A Field object or null if the field is not found.
 */
//===================================================================
public Field getField(String name,int options)
//===================================================================
{
	Field f = new Field(this);
	f.fieldName = name;
	if (!nativeGetField(name,options,f)) return null;
	if (f.fullType.equals(Wrapper.stringClass)) f.fullType = Wrapper.stringClass;
	return f;
}
//-------------------------------------------------------------------
private native boolean nativeGetField(String name,int options,Field dest);
//-------------------------------------------------------------------
/**
 * Look for a Method in the class - either instance or static.
 * @param nameAndSpecs Must be in Java type specification. For example "aBooleanMethod(ILjava/lang/Object;J)Z"
	specifies a method named "aBooleanMethod" that takes three parameters:  (int, java.lang.Object, long) and
	returns a boolean value. Void methods will end with a "V".
 * @param options Either DECLARED or PUBLIC.
 * @return A Method object or null if the method is not found.
 */
//===================================================================
public Method getMethod(String nameAndSpecs,int options)
//===================================================================
{
	int idx = nameAndSpecs.indexOf('(');
	Method m = getMethod(nameAndSpecs.substring(0,idx),nameAndSpecs.substring(idx),options);
	if (m == null) return m;
	if (m.declaringClass == null) m.declaringClass = getReflectedClass();
	return m;
}
/**
 * Look for a Method in the class - either instance or static.
 * @param name The name of the method.
 * @param specs The specs (parameters) for the method - must be in Java type specification.
 * @param options Either DECLARED or PUBLIC.
 * @return A Method object or null if the method is not found.
 */
//===================================================================
public Method getMethod(String name,String specs,int options)
//===================================================================
{
	Method m = new Method(this);
	m.methodName = name;
	m.methodSpecs = specs;
	if (!nativeGetMethodConstructor(name,specs,options,m,false)) return null;
	return m;
}
/**
 * Look for a Constructor for the class.
 * @param specs The specs (parameters) for the method - must be in Java type specification.
 * @param options Either DECLARED or PUBLIC.
 * @return A Constructor object or null if not found.
 */
//===================================================================
public Constructor getConstructor(String specs,int options)
//===================================================================
{
	Constructor c = new Constructor(this);
	c.methodSpecs = specs;
	if (!nativeGetMethodConstructor("<init>",specs,options,c,true)) return null;
	return c;
}
//-------------------------------------------------------------------
private native boolean nativeGetMethodConstructor(String name,String specs,int options,Object dest,boolean isConstructor);

/**
 * Create a new Array where each element is of the type reflected by this Reflect object.
 * @param num The length of the array, must be >= 0.
 * @return A new Array if successful.
 */
//===================================================================
public /*native*/ Object newArray(int num)
//===================================================================
{
	String name = getClassName();
	if (!name.startsWith("[")){
		name = name.replace('.','/');
		name = "L"+name+";";
	}
	return newArrayInstance(name,num);
}
/**
* Create a new Instance of the Object IF it is not an array - using the default constructor.
Returns the new Object or null if failed.
**/
//===================================================================
public Object newInstance()
//===================================================================
{
	if (defaultConstructor == null)
		defaultConstructor = getConstructor("()V",0);
	if (defaultConstructor == null) return null;
	return defaultConstructor.newInstance(Wrapper.noParameter);
}
/**
* Create a new Instance of the Object IF it is not an array - using the constructor
with the specified parameters int Java type notation (eg "([BII)V").
* @param constructorParameters The parameters for the constructor without any brackets.
* @param parameters
* @return
*/
//===================================================================
public Object newInstance(String constructorSpecs,Wrapper[] parameters)
//===================================================================
{
	if (constructorSpecs.length() == 0 || constructorSpecs.charAt(0) != '(')
		constructorSpecs = "("+constructorSpecs+")V";
	Constructor c = getConstructor(constructorSpecs,0);
	if (c == null) return null;
	return c.newInstance(parameters);
}

//===================================================================
protected native String getSuperClass();
//===================================================================


/**
 * Get the number of interfaces implemented by this class.
 * @see getNumberOfInterfaces()
 */
//===================================================================
public native int getNumberOfInterfaces();
//===================================================================
/**
 * Get the name of the interface implemented by this class at the specified index.
	You can use Reflect.getForName() on the result to create a Reflect object to represent it.
 * @param idx The index of the interface required.
 * @return The name of the interface implemented by this class at the specified index.
 */
//===================================================================
public native String getInterface(int idx);
//===================================================================
/**
 * Get a Reflect Object representing the Superclass of this Reflect.
 */
//===================================================================
public Reflect superClass()
//===================================================================
{
	if (mySuperClass == null) {
		String sc = getSuperClass();
		if (sc != null) mySuperClass = getForName(sc);
	}
	return mySuperClass;
}
//===================================================================
public boolean isBaseClass(String baseName)
//===================================================================
{
	baseName = baseName.replace('.','/');
	String s = getClassName();
	if (!s.endsWith(baseName)) return false;
	int before = s.length()-baseName.length()-1;
	if (before < 0) return true;
	char b = s.charAt(before);
	return b == '.' || b == '/' || b == '$';
}
//===================================================================
public Reflect findBaseClass(String baseName)
//===================================================================
{
	for (Reflect r = this; r != null; r = r.superClass())
		if (r.isBaseClass(baseName)) return r;
	return null;
}
/**
 * Check if the specified object parameter is an instance of this reflected class.
 */
//===================================================================
public native boolean isInstance(Object obj);
//===================================================================
/**
Determines if the class or interface represented by this Reflect object is either the same as,
or is a superclass or superinterface of, the class or interface represented by the specified
Reflect parameter.
*/
//===================================================================
public boolean isAssignableFrom(Reflect other)
//===================================================================
{
	return isTypeOf(other.className,className);
}
/**
Determines if the class or interface represented by this Reflect object is either the same as,
or is a subclass of, or implements, the class or interface represented by the specified
Reflect parameter.
*/
//===================================================================
public boolean isTypeOf(Reflect other) {if (other == null) return false; return other.isAssignableFrom(this);}
//===================================================================
/**
Determines if the class or interface represented by this Reflect object is either the same as,
or is a subclass of, or implements, the class or interface represented by the specified
String parameter. This is done without creating any extra Reflect objects.
 */
//===================================================================
public boolean isTypeOf(String aType)
//===================================================================
{
	return isTypeOf(className,aType);
/*
	if (aType.charAt(0) == '[') {
		Reflect o = getForName(aType);
		if (o == null) return false;
		return isTypeOf(o);
	}
*/
	//return nativeIsTypeOf(aType);
}
//-------------------------------------------------------------------
//private native boolean nativeIsTypeOf(String other);
//-------------------------------------------------------------------
/**
* Even though this could be done by creating Reference objects, I have made it native
* for efficiency. It is done by the VM without the creation of any objects.
* This checks to see if "which" is an instance of, or inherits from or implements "aType".
**/
//===================================================================
public static boolean isTypeOf(String which,String aType)
//===================================================================
{
	try{
		if (which.charAt(0) == '['){
			if (aType.charAt(0) == '[')
				return isTypeOf(which.substring(1),aType.substring(1));
			else {
				aType = aType.replace('.','/');
				return aType.equals("java/lang/Object") || aType.equals("Ljava/lang/Object;");
			}
		}else if (which.length() == 1){
			if (aType.length() != 1) return false;
			return which.charAt(0) == aType.charAt(0);
		}else if (aType.charAt(0) == '['){
			return false;
		}else
			return nativeIsTypeOf(which,aType);
	}catch(Exception e){
		return false;
	}
}
//-------------------------------------------------------------------
private static native boolean nativeIsTypeOf(String which,String aType);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
static boolean isPrimitive(char c)
//-------------------------------------------------------------------
{
	switch(c){
		case 'B':
		case 'C':
		case 'S':
		case 'I':
		case 'J':
		case 'F':
		case 'D':
		case 'Z': return true;
		default: return false;
	}
}
/**
* This converts a parameter list (e.g. "IILjava/lang/String;") to an array
* of strings, each representing a parameter.
**/
//===================================================================
public static String [] getParameters(String parameterList)
//===================================================================
{
	ewe.util.Vector v = new ewe.util.Vector();
	char [] all = parameterList.toCharArray();
	for (int i = 0; i<all.length; i++){
		char c = all[i];
		if (isPrimitive(c)){
			v.add(ewe.sys.Convert.toString(c));
		}else if (c == '['){
			int s = i;
			while(all[i] == '[' && i<all.length) i++;
			if (i > all.length) return null;
			if (isPrimitive(all[i]))
				v.add(new String(all,s,i+1-s));
			else if (all[i] == 'L') {
				while(all[i] != ';' && i<all.length) i++;
				if (i > all.length) break;
				v.add(new String(all,s,i+1-s));
			}else
				return null; //Bad spec.
		}else if (c == 'L'){
			int s = i;
			while(all[i] != ';' && i<all.length) i++;
			if (i > all.length) break;
			v.add(new String(all,s,i+1-s));
		}else
			return null;
	}
	String [] ret = new String[v.size()];
	v.copyInto(ret);
	return ret;
}

/**
* This returns the string within brackets in a method description.
**/
//===================================================================
public static String getMethodParameterList(String list)
//===================================================================
{
	int st = list.indexOf('(');
	int end = list.indexOf(')');
	if (st == -1 || end == -1) return null;
	return list.substring(st+1,end);
}
/**
* Returns if the specified object is an array type.
**/
//===================================================================
public static native boolean isArray(Object obj);
//===================================================================
/**
* Gets the length of the array - if obj represents an array type. It returns
* -1 if it does not represent an array.
**/
//===================================================================
public static native int arrayLength(Object obj);
//===================================================================
/**
* Creates a new array of the specified component type (which should
* be "I" or "Z" or "Ljava/lang/String;" (etc.)
**/
//===================================================================
public static native Object newArrayInstance(String wrapperType,int length);
//===================================================================
/**
* Returns if this Reflect object represents an array.
**/
//===================================================================
public boolean isArray()
//===================================================================
{
	if (className == null) return false;
	if (className.length() < 2)	return false;
	return className.charAt(0) == '[';
}
/**
* Returns the type of the elements of the array if this Reflect
* represents an array. If it does not represent an array, null will be returned.
**/
//===================================================================
public String getComponentType()
//===================================================================
{
	if (!isArray()) return null;
	String ret = className.substring(1);
	//if (ret.charAt(0) == '[') ret = ret.substring(0,1);
	return ret;
}
/**
* This converts a encoded Java type to a printable type. e.g. it will convert
* "I" to "int", "Z" to "boolean" and "Ljava/lang/String;" to java.lang.String
**/
//===================================================================
public static String typeToString(String fullType)
//===================================================================
{
	switch(fullType.charAt(0)){
	case 'Z': return "boolean";
	case 'B': return "byte";
	case 'C': return "char";
	case 'S': return "short";
	case 'I': return "int";
	case 'J': return "long";
	case 'D': return "double";
	case 'F': return "float";
	case 'V': return "void";
	case 'L': return fullType.substring(1,fullType.length()-1).replace('/','.');
	case '[': return typeToString(fullType.substring(1))+"[]";
	default: return "???";
	}
}

//-------------------------------------------------------------------
private native Object [] getMethodsConstructors(int options,boolean constructors);
//-------------------------------------------------------------------
/**
 * Return an array of all the Fields of the class.
 * @param options Should be PUBLIC or DECLARED.
 * @return an array of all the Fields of the class.
 */
//===================================================================
public native Field [] getFields(int options);
//===================================================================
/**
 * Return an array of all the Constructors of the class.
 * @param options Should be PUBLIC or DECLARED.
 * @return an array of all the Constructor of the class.
 */
//===================================================================
public Constructor [] getConstructors(int options)
//===================================================================
{
	return (Constructor [])getMethodsConstructors(options,true);
}
/**
 * Return an array of all the Methods of the class.
 * @param options Should be PUBLIC or DECLARED.
 * @return an array of all the Methods of the class.
 */
//===================================================================
public Method [] getMethods(int options)
//===================================================================
{
	return (Method [])getMethodsConstructors(options,false);
}
/*
//-------------------------------------------------------------------
private Field [] getAllFields(int options)
//-------------------------------------------------------------------
{
	String [] all = getFields(options);
	Field [] ret = new Field[all.length];
	for (int i = 0; i<ret.length; i++){
		ret[i] = getField(all[i],options);
	}
	return ret;
}
//===================================================================
public Field [] getFields() {return getAllFields(0);}
//===================================================================
public Field [] getDeclaredFields() {return getAllFields(DECLARED);}
//===================================================================
*/

/**
* This converts an Object or a Class or a Reflect into a Reflect object.
* @param objectOrClassOrReflect This can be a Class or a Reflect or any other object whose Reflect will be created.
* @return A Reflect object.
*/
//===================================================================
public static Reflect toReflect(Object objectOrClassOrReflect)
//===================================================================
{
	if (objectOrClassOrReflect instanceof Reflect) return (Reflect)objectOrClassOrReflect;
	else if (objectOrClassOrReflect instanceof Class) return new Reflect((Class)objectOrClassOrReflect);
	else if (objectOrClassOrReflect != null) return getForObject(objectOrClassOrReflect);
	else return null;
}
/**
* If objectOrClassOrReflect is a Class or Reflect object, this will return null, otherwise it will
* return the objectOrClassOrReflect.
* @param objectOrClassOrReflect Any object.
* @return If objectOrClassOrReflect is a Class or Reflect object, this will return null, otherwise it will
* return the objectOrClassOrReflect.
*/
//===================================================================
public static Object toNonReflect(Object objectOrClassOrReflect)
//===================================================================
{
	return (objectOrClassOrReflect instanceof Class || objectOrClassOrReflect instanceof Reflect) ? null : objectOrClassOrReflect;
}
/**
* This converts an Object or a Class or a Reflect into a Class object.
* @param objectOrClassOrReflect This can be a Class or a Reflect or any other object whose Class will be created.
* @return A Class object.
*/
//===================================================================
public static Class toClass(Object objectOrClassOrReflect)
//===================================================================
{
	if (objectOrClassOrReflect instanceof Reflect) return ((Reflect)objectOrClassOrReflect).getReflectedClass();
	else if (objectOrClassOrReflect instanceof Class) return (Class)objectOrClassOrReflect;
	else if (objectOrClassOrReflect != null) return objectOrClassOrReflect.getClass();
	else return null;
}
/**
 * Creates a new class from a java encoded type string.
 * This is more useful than Class.forName() since it can also find classes loaded from .ewe files and also
 * get a Class representing a primitive type.
 * @param type the Java encoded type.
 * @param requestor an optional requesting class.
 * @return the Class representing the type or null if the type was not found.
 * @deprecated - use typeToClass() instead.
 */
//===================================================================
public static Class getClass(String type,Class requestor)
//===================================================================
{
	char ch = type.charAt(0);
	try{
		if (ch == 'L') {
			return getForName(type,requestor).getReflectedClass();
		}else if (ch == '['){
			return Class.forName(type);
		}else
			return Class.forName("["+ch).getComponentType();
	}catch(Exception e) {
		return null;
	}
}

/**
 * Convert a Java encoded primitive type to the Class representing that type.
 * @param primitiveType the single character primitive type (e.g. 'Z' = boolean, 'J' = long, 'V' = void).
 * @return the Class representing the primitive type.
 */
//===================================================================
public static Class primitiveTypeToClass(char primitiveType)
//===================================================================
{
	switch(primitiveType){
		case 'V' : return Void.TYPE;
		case 'Z' : return Boolean.TYPE;
		case 'B' : return Byte.TYPE;
		case 'C' : return Character.TYPE;
		case 'S' : return Short.TYPE;
		case 'I' : return Integer.TYPE;
		case 'J' : return Long.TYPE;
		case 'F' : return Float.TYPE;
		case 'D' : return Double.TYPE;
		default: return null;
	}
}
/**
 * Creates a new class from a java encoded type string, searching all the registered class loaders
 * if necessary.
 * This is more useful than Class.forName() since it can also find classes loaded from .ewe files and also
 * get a Class representing a primitive type.
 * @param type the Java encoded type.
 * @return the Class representing the type or null if the type was not found.
 */
//===================================================================
public static Class typeToClass(String type)
//===================================================================
{
	char ch = type.charAt(0);
	try{
		if (ch == 'L') {
			return loadForName(type).getReflectedClass();
		}else if (ch == '['){
		/*
			int i = 1;
			for (int i = 1; i<type.length() && type.charAt(i) == '['; i++)
				;
			if (i == type.length()) return null;
			if (type.charAt(i) == 'L')
				if (typeToClass(type.substring(i)) == null) return;
		*/
			return Class.forName(type);
		}else
			return primitiveTypeToClass(ch);
	}catch(Exception e) {
		return null;
	}
}
//===================================================================
public static char getWrapperType(Class c)
//===================================================================
{
	String name = c.getName();
	char ch = name.charAt(0);
	if (c.isPrimitive()) switch(ch){
		case 'b': if (name.charAt(1) == 'y') return 'B'; else return 'Z';
		case 'l': return 'J';
		default: return Character.toUpperCase(ch);
	}
	return ch;
}
/**
 * Returns the String encoded type of the class.
 * The returned String is a Java encoded type.
 * @param c The class.
 * @return the Java encoded type.
 */
//===================================================================
public static String getType(Class c)
//===================================================================
{
	String name = c.getName();
	char ch = name.charAt(0);
	if (c.isPrimitive()) switch(ch){
		case 'b': if (name.charAt(1) == 'y') return "B"; else return "Z";
		case 'l': return "J";
		default: return new String(new char[]{Character.toUpperCase(ch)});
	}
	if (ch == '[') return name;
	return "L"+name.replace('.','/')+";";
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

