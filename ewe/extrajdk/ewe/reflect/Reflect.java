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
//import java.util.*;
import java.lang.reflect.Modifier;
import ewe.util.mClassLoader;
import ewe.util.Vector;
//##################################################################
public class Reflect{
//##################################################################
// Native variables, do not move.
protected int nativeClass;
protected String className;

// Used by Java version. Ignored by native version.
protected java.lang.Class theClass = null;
protected Constructor defaultConstructor = null;
protected java.lang.Class [] interfaces;
public static final int DECLARED = 1;
public static final int PUBLIC = 2;

//===================================================================
protected Reflect(){}
//===================================================================
protected Reflect mySuperClass;

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
	try{
		return java.lang.reflect.Array.newInstance(c,length);
	}catch(Throwable t){
		return null;
	}
}

//===================================================================
public Class getReflectedClass() {return theClass;}
//===================================================================

//===================================================================
public Reflect(Class aClass)
//===================================================================
{
	if (aClass == null) throw new NullPointerException();
	if (aClass.isPrimitive()) throw new RuntimeException("You cannot create a Reflect object to represent a primitive value.");
	theClass = aClass;
}
//-------------------------------------------------------------------
static String fixClassName(String name)
//-------------------------------------------------------------------
{
	if (name == null) return name;
	return name.replace('.','/');
}
//===================================================================
public static Reflect getForObject(Object obj)
//===================================================================
{
	if (obj == null) throw new NullPointerException();
	Reflect r = new Reflect();
	r.theClass = obj.getClass();
	return r;
}
//===================================================================
public static Reflect getForName(String name)
//===================================================================
{
	return getForName(name,null);
}
//===================================================================
public static Reflect getForName(String name,Class requestor)
//===================================================================
{
	if (name == null) return null;
	name = name.replace('/','.');
	if (name.charAt(name.length()-1) == ';' && name.charAt(0) == 'L') name = name.substring(1,name.length()-1);
	Reflect r = new Reflect();
	r.theClass = ewe.applet.Applet.loadClass(name);//
	if (r.theClass != null) return r;
	if (requestor == null) return null;
	ClassLoader cl = requestor.getClassLoader();
	if (cl == null) return null;
	try{
		return new Reflect(cl.loadClass(name));
	}catch(Exception e){
		return null;
	}
}
//===================================================================
public String getClassName()
//===================================================================
{
	String ret = fixClassName(theClass.getName());
	//if (ret.charAt(0) == '[') return ret.substring(0,2);
	//else
	return ret;
}
//===================================================================
public String toString() {return getClassName();}
//===================================================================

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

//===================================================================
public boolean nativeGetField(String name,int options,Field dest)
//===================================================================
{
	try{
		return dest.fromField(((options & DECLARED) != 0) ?
			theClass.getDeclaredField(name):
			theClass.getField(name));
	}catch(Throwable e){
		return false;
	}
}
//===================================================================
public Method getMethod(String nameAndSpecs,int options)
//===================================================================
{
	int idx = nameAndSpecs.indexOf('(');
	return getMethod(nameAndSpecs.substring(0,idx),nameAndSpecs.substring(idx),options);
}
//===================================================================
public Method getMethod(String name,String specs,int options)
//===================================================================
{
	Method m = new Method(this);
	m.methodName = name;
	m.methodSpecs = specs;
	if (!nativeGetMethod(name,specs,options,m)) return null;
	return m;
}
//===================================================================
public boolean nativeGetMethod(String name,String specs,int options,Method dest)
//===================================================================
{
	try{
		return dest.fromMethod(((options & DECLARED) != 0) ?
			theClass.getDeclaredMethod(name,getClassList(specs)):
			theClass.getMethod(name,getClassList(specs)));
	}catch(Throwable e){
		return false;
	}
}

//-------------------------------------------------------------------
protected static Class [] badSpecs(String specs)
//-------------------------------------------------------------------
{
	throw new RuntimeException("Bad parameter specs: "+specs);
}
//-------------------------------------------------------------------
static Class specToClass(char c)
//-------------------------------------------------------------------
{
	switch(c){
		case 'B': return Byte.TYPE;
		case 'C': return Character.TYPE;
		case 'S': return Short.TYPE;
		case 'I': return Integer.TYPE;
		case 'J': return Long.TYPE;
		case 'F': return Float.TYPE;
		case 'D': return Double.TYPE;
		case 'Z': return Boolean.TYPE;
		default: return null;
	}
}
//-------------------------------------------------------------------
static Class specToClass(String spec)
//-------------------------------------------------------------------
{
	spec = spec.replace('/','.');
	try{
		if (spec.length() == 1){
			return specToClass(spec.charAt(0));
		}else{
			if (spec.charAt(0) == '[') return Class.forName(spec);
			if (spec.charAt(0) == 'L'){
				int l = spec.length();
				if (spec.endsWith(";")) l--;
				spec = spec.substring(1,l);
				return Class.forName(spec);
			}
			return null;
		}
	}catch(Throwable e){
		return null;
	}
}
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

//-------------------------------------------------------------------
static String toFullType(Class c)
//-------------------------------------------------------------------
{
	if (c.equals(Integer.TYPE)) return "I";
	if (c.equals( Boolean.TYPE)) return "Z";
	if (c.equals( Byte.TYPE)) return "B";
	if (c.equals( Character.TYPE)) return "C";
	if (c.equals( Short.TYPE)) return "S";
	if (c.equals( Long.TYPE)) return "J";
	if (c.equals( Double.TYPE)) return "D";
	if (c.equals( Float.TYPE)) return "F";
	if (c.equals( Void.TYPE)) return "V";
	String nm = c.getName();
	if (nm.startsWith("[")) return fixClassName(nm);
	else return "L"+fixClassName(nm)+";";
}
//-------------------------------------------------------------------
static Class [] getClassList(String parameterSpecs)
//-------------------------------------------------------------------
{
	String cl = getMethodParameterList(parameterSpecs);
	if (cl == null) return null;
	String [] pars = getParameters(cl);
	if (pars == null) return null;
	Class [] ret = new Class[pars.length];
	for (int i = 0; i<pars.length; i++)
		ret[i] = specToClass(pars[i]);
	return ret;
/*
	try{
		Vector v = new Vector();
		char [] all = parameterSpecs.toCharArray();
		int l = all.length;
		if (all.length < 2) throw new Exception();
		if (all[0] != '(') throw new Exception();
		for (int i = 1; i<l; i++){
			char c = all[i];
			if (c == ')') break;
			if (isPrimitive(c)){
				Class cl = specToClass(all[i]);
				if (cl == null) throw new Exception();
				v.addElement(cl);
				continue;
			}
			if (c == '['){
				if (isPrimitive(all[i+1])){
					Class cl = specToClass(parameterSpecs.substring(i,i+2));
					if (cl == null) throw new Exception();
					v.addElement(cl);
					i++;
					continue;
				}else if (all[i+1] == 'L'){
					int j = i;
					while(all[j] != ';') j++;
					Class cl = specToClass(parameterSpecs.substring(i,j+1));
					if (cl == null) throw new Exception();
					v.addElement(cl);
					i = j;
					continue;
				}else
					throw new Exception();
			}else if (c == 'L'){
				int j = i;
				while(all[j] != ';') j++;
				Class cl = specToClass(parameterSpecs.substring(i,j+1));
				if (cl == null) throw new Exception();
				v.addElement(cl);
				i = j;
				continue;
			}else
				throw new Exception();
		}
		Class [] ret = new Class[v.size()];
		v.copyInto(ret);
		return ret;
	}catch(Throwable e){
		//e.printStackTrace();
		return badSpecs(parameterSpecs);
	}
*/
}
//-------------------------------------------------------------------
static void printList(Object [] all)
//-------------------------------------------------------------------
{
	Vector v = new Vector();
	for (int i = 0; i<all.length; i++) v.add(all[i]);
	System.out.println(v);

}
//===================================================================
public Constructor getConstructor(String specs,int options)
//===================================================================
{
	Constructor c = new Constructor(this);
	c.methodSpecs = specs;
	if (!nativeGetConstructor(specs,options,c)) return null;
	return c;
}
//===================================================================
protected boolean nativeGetConstructor(String specs,int options,Constructor c)
//===================================================================
{
	try{
		return c.fromConstructor(((options & DECLARED) != 0) ?
			theClass.getDeclaredConstructor(getClassList(specs)):
			theClass.getConstructor(getClassList(specs)));
	}catch(Throwable e){
		return false;
	}
}
//===================================================================
public  Object newArray(int num)
//===================================================================
{
	try{
		return java.lang.reflect.Array.newInstance(theClass,num);
	}catch(Throwable e){
		return null;
	}
}
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
public Reflect superClass()
//===================================================================
{
	if (mySuperClass == null){
		try{
			Class c = theClass.getSuperclass();
			if (c != null) {
				Reflect r = new Reflect();
				r.theClass = c;
				mySuperClass = r;
			}
		}catch(Throwable e){
		}
	}
	return mySuperClass;
}
//===================================================================
public int getNumberOfInterfaces()
//===================================================================
{
	try{
		if (interfaces == null) interfaces = theClass.getInterfaces();
		return interfaces.length;
	}catch(Throwable e){
		return 0;
	}
}
//===================================================================
public String getInterface(int index)
//===================================================================
{
	try{
		if (interfaces == null) interfaces = theClass.getInterfaces();
		if (index >= 0 && index < interfaces.length) return fixClassName(interfaces[index].getName());
		return null;
	}catch(Throwable e){
		return null;
	}
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
//===================================================================
public boolean isInstance(Object obj)
//===================================================================
{
	return theClass.isInstance(obj);
}
//===================================================================
public boolean isAssignableFrom(Reflect other)
//===================================================================
{
	if (other == null) return false;
	return theClass.isAssignableFrom(other.theClass);
}
//===================================================================
public boolean isTypeOf(Reflect other) {if (other == null) return false; return other.isAssignableFrom(this);}
//===================================================================
public boolean isTypeOf(String other)
//===================================================================
{
	Reflect o = getForName(other);
	if (o == null) return false;
	return isTypeOf(o);
}
//===================================================================
public static boolean isTypeOf(String which,String aType)
//===================================================================
{
	if (which.length() == 1){
		if (aType.length() == 1) return which.charAt(0) == aType.charAt(0);
		return false;
	}
	Reflect r = getForName(aType);
	if (r == null) return false;
	return r.isAssignableFrom(getForName(which));
}
/*
//===================================================================
private native String _arrayElementType();
//===================================================================
//===================================================================
public String getComponentType()
//===================================================================
{
	return null;
}
*/

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
* Returns if this Reflect object represents an array.
**/
//===================================================================
public boolean isArray()
//===================================================================
{
	String className = getClassName();
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
	String className = getClassName();
	if (!isArray()) return null;
	String ret = className.substring(1);
	//if (ret.charAt(0) == '[') ret = ret.substring(0,1);
	return ret;
}
//===================================================================
public static boolean isArray(Object obj)
//===================================================================
{
	if (obj == null) return false;
	return obj.getClass().isArray();
}
//===================================================================
public static int arrayLength(Object obj)
//===================================================================
{
	if (!isArray(obj)) return -1;
	return java.lang.reflect.Array.getLength(obj);
}
//===================================================================
public static Object newArrayInstance(String type,int len)
//===================================================================
{
	try{
		Class c = specToClass(type);
		return java.lang.reflect.Array.newInstance(c,len);
	}catch(Throwable t){
		return null;
	}
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
private Field [] fromFields(java.lang.reflect.Field all[])
//-------------------------------------------------------------------
{
	Field [] ret = new Field[all.length];
	for (int i = 0; i<ret.length; i++) {
		ret[i] = new Field(this);
		ret[i].fromField(all[i]);
	}
	return ret;
}
//-------------------------------------------------------------------
private Method [] fromMethods(java.lang.reflect.Method all[])
//-------------------------------------------------------------------
{
	Method [] ret = new Method[all.length];
	for (int i = 0; i<ret.length; i++) {
		ret[i] = new Method(this);
		ret[i].fromMethod(all[i]);
	}
	return ret;
}
//-------------------------------------------------------------------
private Constructor [] fromConstructors(java.lang.reflect.Constructor all[])
//-------------------------------------------------------------------
{
	Constructor [] ret = new Constructor[all.length];
	for (int i = 0; i<ret.length; i++) {
		ret[i] = new Constructor(this);
		ret[i].fromConstructor(all[i]);
	}
	return ret;
}
//===================================================================
public Field [] getFields(int options)
//===================================================================
{
	try{
		return fromFields(((options & DECLARED) != 0) ? theClass.getDeclaredFields() : theClass.getFields());
	}catch(Throwable t){
		return new Field[0];
	}
}

//===================================================================
public Method [] getMethods(int options)
//===================================================================
{
	try{
		return fromMethods(((options & DECLARED) != 0) ? theClass.getDeclaredMethods() : theClass.getMethods());
	}catch(Throwable t){
		return new Method[0];
	}
}
//===================================================================
public Constructor [] getConstructors(int options)
//===================================================================
{
	try{
		return fromConstructors(((options & DECLARED) != 0) ? theClass.getDeclaredConstructors() : theClass.getConstructors());
	}catch(Throwable t){
		return new Constructor[0];
	}
}

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
 * Creates a new class from a java encoded type string.
 * This is more useful than Class.forName() since it can also find classes loaded from .ewe files and also
 * get a Class representing a primitive type.
 * @param type the Java encoded type.
 * @param
 * @return the Class representing the type or null if the type was not found.
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
		//e.printStackTrace();
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
