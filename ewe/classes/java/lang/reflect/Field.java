package java.lang.reflect;
import ewe.reflect.Wrapper;
import ewe.reflect.Reflect;

//##################################################################
public class Field implements Member{
//##################################################################

private ewe.reflect.Field rf;
private Class myType;

//-------------------------------------------------------------------
private Field(){}
//-------------------------------------------------------------------

//===================================================================
public Field(ewe.reflect.Field field)
//===================================================================
{
	this.rf = field;
}

//===================================================================
public String getName() {return rf.getName();}
//===================================================================

//===================================================================
public int getModifiers() {return rf.getModifiers();}
//===================================================================

//===================================================================
public Class getDeclaringClass() {return rf.getDeclaringClass();}
//===================================================================
//===================================================================
public int hashCode() {return getName().hashCode()+getDeclaringClass().hashCode();}
//===================================================================
/**
 * Return the type of the Field as a Class object.
 */
//===================================================================
public Class getType()
//===================================================================
{
	if (myType != null) return myType;
	return myType = Reflect.typeToClass(rf.getType());
}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (!(other instanceof Field)) return super.equals(other);
	Field f = (Field)other;
	if (!f.getName().equals(getName())) return false;
	if (!f.getDeclaringClass().equals(getDeclaringClass())) return false;
	return true;
}
//-------------------------------------------------------------------
//private native Object getSet(Object dest, Object value, boolean isGet);// throws IllegalArgumentException, IllegalAccessException;
//-------------------------------------------------------------------

//-------------------------------------------------------------------
private Class check(Object dest, boolean forGet)
throws IllegalAccessException
//-------------------------------------------------------------------
{
	int m = rf.getModifiers();
	if (!ewe.reflect.Modifier.isStatic(m)){
		if (dest == null) throw new NullPointerException();
		if (!rf.getDeclaringClass().isInstance(dest)) throw new IllegalArgumentException();
	}
	//
	if (!forGet)
		if (ewe.reflect.Modifier.isFinal(m))
			throw new IllegalAccessException();
	//
	return getType();
}

private static Wrapper fieldWrapper = new Wrapper();

//===================================================================
public void set(Object dest,Object value)
throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	Class type = check(dest,false);
	boolean t = false;
	if (type.isPrimitive()){
		value = Wrapper.widenJavaWrapper(value,type);
	}else if (value != null){
		if (!type.isInstance(value)) t = true;
	}
	if (t) throw new IllegalArgumentException();
	fieldWrapper.fromJavaWrapper(value);
	rf.setValue(dest,fieldWrapper);
}
//===================================================================
public Object get(Object src)
throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	Class type = check(src,true);
	Wrapper w = rf.getValue(src,fieldWrapper);
	if (w == null) throw new IllegalAccessException();
	return w.toJavaWrapper();
}
//===================================================================
public String toString()
//===================================================================
{
	return rf.toString();
}

//===================================================================
public void setBoolean(Object dest,boolean value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Boolean(value));
}
//===================================================================
public void setByte(Object dest,byte value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Byte(value));
}
//===================================================================
public void setChar(Object dest,char value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Character(value));
}
//===================================================================
public void setDouble(Object dest,double value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Double(value));
}
//===================================================================
public void setFloat(Object dest,float value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Float(value));
}
//===================================================================
public void setInt(Object dest,int value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Integer(value));
}
//===================================================================
public void setLong(Object dest,long value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Long(value));
}
//===================================================================
public void setShort(Object dest,short value) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	set(dest,new Short(value));
}

//===================================================================
public boolean getBoolean(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Boolean)get(dest)).booleanValue();
}
//===================================================================
public byte getByte(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Byte)get(dest)).byteValue();
}
//===================================================================
public char getChar(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Character)get(dest)).charValue();
}
//===================================================================
public double getDouble(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Double)get(dest)).doubleValue();
}
//===================================================================
public float getFloat(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Float)get(dest)).floatValue();
}
//===================================================================
public int getInt(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Integer)get(dest)).intValue();
}
//===================================================================
public long getLong(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Long)get(dest)).longValue();
}
//===================================================================
public short getShort(Object dest) throws IllegalArgumentException, IllegalAccessException
//===================================================================
{
	return ((Short)get(dest)).shortValue();
}
//##################################################################
}
//##################################################################

