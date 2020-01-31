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
package ewe.data;
import ewe.util.*;
import ewe.reflect.*;
/**
* This is an object which completely implements the DataUnit interface.
* The following is the default behavior.
* <p>
* getNew() will use the Reflection API to attempt to create a new instance
* of the object via a public default constructor. If successful, the object created will be returned.
* <p>
* getCopy() will call first call getNew() and then invoke copyFrom() on the
* created object passing it this object as a parameter.
* <p>
* copyFrom(Object other) envokes the ewe.util.Utils.copy(Object source,Object dest) with this as the
* destination and the "other"
* <p>
* compareTo() returns 0 if the two Objects are the same object, or 1 otherwise.
* <p>
* equals() returns true if compareTo() returns 0.
**/
//##################################################################
public class DataObject implements DataUnit{
//##################################################################
//===================================================================
public Object getCopy()
//===================================================================
{
	DataUnit du = (DataUnit)getNew();
	du.copyFrom(this);
	return du;
}
//==================================================================
public void copyFrom(Object other)
//==================================================================
{
	Utils.copy(other,this);
	copied(other);
}
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (other == this) return 0;
	return 1;
}
//===================================================================
public boolean equals(Object other) {return compareTo(other) == 0;}
//===================================================================
public Object getNew()
//===================================================================
{
	ewe.reflect.Reflect r = ewe.reflect.Reflect.getForObject(this);
	if (r == null) return null;
	return r.newInstance();
}
/**
* This is used for data transfer using a ewe.reflect.FieldTransfer object. It is used
* if the programmer requests a field transfer but there is no such field in the object. In
* that case the _getSetField() method will be called and the programmer can programatically
* get/set the field data.<p>
* By default this method calls getProperties() and then calls the _getSetField() method in PropertyList.
* @param fieldName The name of the field.
* @param wrapper A wrapper containing the data to be assigned to the field or the wrapper into
* which you should place the field data.
* @param isGet if this is true then it is a <b>get</b> operation (in which case you should set
* the wrapper value to be the field value) if it is false it is a <b>set</b> operation and
* the wrapper contains the field value to assign to the field.
* @return the Object should return true if the field data was successfully transfered, false if not.
*/
//===================================================================
public boolean _getSetField(String fieldName,ewe.reflect.Wrapper wrapper,boolean isGet)
//===================================================================
{
	return PropertyList.getSetProperties(this,fieldName,wrapper,isGet);
}
/**
 * Get a comma separated list of fields for the specified object.
 * @param objectOrClass the object, or Class of the object, or Reflect of the object.
 * @param declaredOnly if this is true only the field declared by the class (not those inherited)
 * are used.
 * @return a comma separatedlist of fields (e.g. "name,age,dob").
 */
//===================================================================
public static String getFieldList(Object objectOrClass,boolean declaredOnly)
//===================================================================
{
	return getFieldList(Reflect.toReflect(objectOrClass),Reflect.toNonReflect(objectOrClass),declaredOnly);
}
//===================================================================
public static String getFieldList(Object reflectOrClass,Object dataObject,boolean declaredOnly)
//===================================================================
{
	if (reflectOrClass == null) reflectOrClass = dataObject;
	String ret = "";
	for (Reflect r = Reflect.toReflect(reflectOrClass); r != null; r = r.superClass()){
		Field f = r.getField("_fields",r.DECLARED);
		String add = "";
		if (f != null && Modifier.isPublic(f.getModifiers()) && dataObject != null && f.fullType.equals("Ljava/lang/String;")){
			//ewe.sys.Vm.debug("Has _fields for: "+r);
			Wrapper w = f.getValue(dataObject,null);
			add = mString.toString(w.getObject());
		}else{
			Field [] all = r.getFields(r.DECLARED);
			for (int i = 0; i<all.length; i++){
				int mod = all[i].getModifiers();
				if (!Modifier.isStatic(mod) && Modifier.isPublic(mod) && (all[i].getName().charAt(0) != '_')){
					if (add.length() != 0) add += ",";
					//ewe.sys.Vm.debug("Adding: "+r+"."+all[i]);
					add += all[i].getName();
				}
			}
		}
		if (add.length() != 0){
			if (ret.length() != 0) add += ",";
			ret = add+ret;
		}
		if (declaredOnly) break;
	}
	return ret;
}
//-------------------------------------------------------------------
static Reflect getReflect(Object obj,String baseClassName) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	Reflect r = Reflect.getForObject(obj).findBaseClass(baseClassName);
	if (r == null) throw new IllegalArgumentException("\""+baseClassName+"\" is not in the class hierarchy.");
	return r;
}
/**
* Gets the declared field list for a particular class in the class hierarchy of this LiveObject.
* @param baseClassName This should be the last part of the class name or the fully qualified class name.
For example if the object is of type samples.data.PersonInfo, the baseClassName can be "PersonInfo".
* @return A comma separated list of field names.
* @exception IllegalArgumentException If the baseClassName does not appear in the class hierarchy.
*/
//===================================================================
public String getMyFieldList(String baseClassName) throws IllegalArgumentException
//===================================================================
{
	return getFieldList(getReflect(this,baseClassName),this,true);
}
/**
 * Get a declared field for this object for the specified baseClassName.
 * @param fieldName The fieldName
* @param baseClassName This should be the last part of the class name or the fully qualified class name.
For example if the object is of type samples.data.PersonInfo, the baseClassName can be "PersonInfo".
 * @return the Field or null if no declared field of that name was found.
* @exception IllegalArgumentException If the baseClassName does not appear in the class hierarchy.
 */
//===================================================================
public Field getDeclaredField(String fieldName,String baseClassName) throws IllegalArgumentException
//===================================================================
{
	Reflect r = getReflect(this,baseClassName);
	return r.getField(fieldName,r.DECLARED);
}
/**
 * Get the value of a declared field in a Wrapper object.
 * @param fieldName The fieldName
 * @param destination a destination wrapper for the value. This can be null in which case a new one will be created.
* @param baseClassName This should be the last part of the class name or the fully qualified class name.
For example if the object is of type samples.data.PersonInfo, the baseClassName can be "PersonInfo".
 * @return a Wrapper containing the field value or null if the field does not exist, or if the field value could not be retrieved.
* @exception IllegalArgumentException If the baseClassName does not appear in the class hierarchy.
 */
//===================================================================
public Wrapper getDeclaredFieldValue(String fieldName,Wrapper destination,String baseClassName) throws IllegalArgumentException
//===================================================================
{
	Field f = getDeclaredField(fieldName,baseClassName);
	if (f == null) return null;
	if (destination == null) destination = new Wrapper();
	return f.getValue(this,destination);
}
/**
 * This returns the value of a declared field as an object. If the declared field is not of an
 * object type, it will return null;
 * @param fieldName The fieldName
* @param baseClassName This should be the last part of the class name or the fully qualified class name.
For example if the object is of type samples.data.PersonInfo, the baseClassName can be "PersonInfo".
 * @return The object value of the field or null if the field does not exist, or could not be
 * accessed or was not an object type.
* @exception IllegalArgumentException If the baseClassName does not appear in the class hierarchy.
 */
//===================================================================
public Object getDeclaredFieldValue(String fieldName,String baseClassName) throws IllegalArgumentException
//===================================================================
{
	Wrapper w = getDeclaredFieldValue(fieldName,null,baseClassName);
	if (w == null) return null;
	if (w.getType() == w.OBJECT || w.getType() == w.ARRAY) return w.getObject();
	return null;
}
//-------------------------------------------------------------------
static Vector getClassList(Object reflectOrClass,String methodExclusion)
//-------------------------------------------------------------------
{
	Vector v = new Vector();
	for (Reflect r = Reflect.toReflect(reflectOrClass); r != null; r = r.superClass())
		if (r.getClassName().startsWith("ewe/data/")) break;
		else if (r.getMethod(methodExclusion,r.DECLARED) == null)
			v.add(0,r);
	return v;
}
//===================================================================
public static String appendAllFields(String fieldName,Object dataObject,boolean declaredOnly)
//===================================================================
{
	String ret = "";
	for (Reflect r = Reflect.toReflect(dataObject); r != null; r = r.superClass()){
		Field f = r.getField(fieldName,r.DECLARED);
		if (f != null && Modifier.isPublic(f.getModifiers()) && dataObject != null && f.fullType.equals("Ljava/lang/String;")){
			//ewe.sys.Vm.debug("Has _fields for: "+r);
			Wrapper w = f.getValue(dataObject,null);
			String add = mString.toString(w.getObject());
			if (add.length() == 0) continue;
			if (ret.length() != 0) add += ",";
			ret = add+ret;
		}
		if (declaredOnly) break;
	}
	return ret;
}
/**
This method is called after the base implementation of copyFrom() is executed. It gives
you a chance to do extra work after the standard field by field copy is done.
* @param from The object that data was copied from.
*/
//-------------------------------------------------------------------
protected void copied(Object from){}
//-------------------------------------------------------------------
//##################################################################
}
//##################################################################

