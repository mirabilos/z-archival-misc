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
import ewe.sys.*;
//#####################################################################
public class PropertyList extends Vector implements HasProperties, Textable{
//#####################################################################

public static final PropertyList nullPropertyList = new NullPropertyList();

/**
* Get the property with the specified name.
*/
/*
public Property get(String name,Property default)
{
	return get(name,null);
}
*/
/**
* This returns itself.
**/
public PropertyList getProperties() {return this;}
/**
*
*/
//==================================================================
public PropertyList(Object [] nameAndValues) {add(nameAndValues);}
//==================================================================

//===================================================================
public boolean add(Object [] nameAndValues)
//===================================================================
{
	if (nameAndValues != null) {
		for (int i = 0; i+1 < nameAndValues.length; i += 2){
			Object n = nameAndValues[i];
			Object v = nameAndValues[i+1];
			if (n == null || !(n instanceof String)) continue;
			add((String)n,v);
		}
	}
	return true;
}
//==================================================================
public PropertyList(){}
//==================================================================
public boolean defaultTo(String name,Object value)
//==================================================================
{
	if (get(name) == null) set(name,value);
	return true;
}
//===================================================================
public void defaultTo(String nameAndValues)
//===================================================================
{
	String [] got = mString.split(nameAndValues);
	for (int i = 0; i<got.length-1; i+=2)
		defaultTo(got[i],got[i+1]);
}
//===================================================================
public boolean setBoolean(String name,boolean value) {return set(name,new ewe.sys.Long().set(value ? 1 : 0));}
//===================================================================
public boolean setInt(String name,int value) {return set(name,new ewe.sys.Long().set(value));}
//===================================================================
public boolean getBoolean(String name,boolean def)
//===================================================================
{
	Object o = getValue(name,null);
	if (o == null) return def;
	if (o instanceof String){
		return Convert.toBoolean((String)o);
	}else if (o instanceof ewe.sys.Long){
		return ((ewe.sys.Long)o).value != 0;
	}else if (o instanceof Boolean)
		return ((Boolean)o).booleanValue();
	else
		return def;
}
//===================================================================
public double getDouble(String name,double def)
//===================================================================
{
	Object obj = getValue(name,null);
	if (obj == null) return def;
	if (obj instanceof ewe.sys.Double) return ((ewe.sys.Double)obj).value;
	else if (obj instanceof java.lang.Double) return ((java.lang.Double)obj).doubleValue();
	else if (obj instanceof String) return ewe.sys.Convert.toDouble((String)obj);
	else return def;
}
//===================================================================
public int getInt(String name,int def)
//===================================================================
{
	Object obj = getValue(name,null);
	if (obj == null) return def;
	if (obj instanceof ewe.sys.Long) return (int)((ewe.sys.Long)obj).value;
	else if (obj instanceof java.lang.Integer) return ((java.lang.Integer)obj).intValue();
	else if (obj instanceof String) return ewe.sys.Convert.toInt((String)obj);
	else return def;
}
/**
* Get the property with the specified name.
*/
public Property get(String name) {return get(name,null);}
/**
* Get the property with the specified name, starting with the previous one.
*/
//==================================================================
public Property get(String name,Property previous)
//==================================================================
{
	int idx = -1;
	int s = size();
	if (previous != null) {
		for (int i = 0; i<s; i++){
			if (get(i) == previous){
				idx = i;
				break;
			}
		}
		if (idx == -1) return null;
	}
	for (int i = idx+1; i<s; i++) {
		Property p = (Property)get(i);
		if (p.compareTo(name) == 0) return p;
	}
	return null;
/*
	boolean hasDot = false;
	name = mSystem.nullToEmptyString(name).toUpperCase();
	if (name.endsWith(".")) hasDot = true;
		//name = name.substring(name.length()-1);
	if (name.equals("")) return null;
	int start = -1;
	if (previous != null) start = indexOf(previous);
	for (int i = start+1; i < size(); i++){
		Property p2 = (Property)get(i);
		String n2 = p2.name.toUpperCase();
		if (!n2.startsWith(name)) continue;
		if (n2.equals(name)) return p2;
		if (n2.charAt(name.length()-1) == '.') return p2;
		if (n2.charAt(name.length()) == '.') return p2;
	}
	return null;
*/
}
/*
//==================================================================
public mVector getNew() {return new PropertyList();}
//==================================================================

/**
* Gets the value of a property.
*/
//public synchronized Object getValue(String name){return getValue(name,null);}
/**
* Gets the value of a property, with a default if it does not exist.
*/
public Object getValue(String name,Object deflt)
{
	Property p = get(name);
	if (p == null) return deflt;
	return p.value;
}
public String getString(String name,String deflt)
{
	Object obj = getValue(name,deflt);
	if (obj == null) return (String)null;
	else return obj.toString();
}
/**
* Gets the value of a property which may be named differently (e.g. "color" and "colour").
*/
public Object getValue(Vector v,Object deflt)
{
	if (v == null) return null;
	for (int i = 0; i<v.size(); i++){
		Property p = get((String)v.get(i));
		if (p != null) return p.value;
	}
	return deflt;
}
/**
* Gets the value of a property which may be named differently (e.g. "color" and "colour").
*/
public Object getValue(String [] v,Object deflt)
{
	if (v == null) return null;
	for (int i = 0; i<v.length; i++){
		Property p = get(v[i]);
		if (p != null) return p.value;
	}
	return deflt;
}
/**
* Sets an exclusive property. i.e. only one property with that name can exist.
*/
public boolean set(Property prop)
{
	if (prop == null) return false;
	Property p2 = get(prop.name);
	if (p2 != null) remove(p2);
	add(prop);
	return true;
}
/**
* Sets an exclusive property. i.e. only one property with that name can exist.
*/
public boolean set(String props,Object value)
{
	boolean ret = true;
	String [] p = mString.split(props);
	for (int i = 0; i<p.length; i++)
		if (!set(new Property(p[i],value))) ret = false;
	return true;
}
/**
*
*/
public boolean set(PropertyList pl)
{
	if (pl != null)
		for (int i = 0; i<pl.size(); i++)
			set((Property)pl.get(i));
	return true;
}
/**
* Sets a set of properties, each associated with a string. For example to set:
* "first"="hello" and "second"="there", call the method with the value:
* "first|hello|second|there".
*/
public boolean setStrings(String propsAndValues)
{
	boolean ret = true;
	String [] got = mString.split(propsAndValues);
	for (int i = 0; i<got.length/2; i++)
		if (!set(new Property(got[i*2],got[i*2+1]))) ret = false;
	return ret;
}
/**
* Gets a set of properties, each associated with a string. For example to get:
* properties "first" and "second" call the method with the value:
* "first|second".
*/
public String [] getStrings(String props,String defaultValue)
{
	String [] got = mString.split(props);
	String [] ret = new String[got.length];
	for (int i = 0; i<got.length; i++){
		Object o = getValue(got[i],defaultValue);
		if (o != null) ret[i] = mString.toString(o);
		else ret[i] = null;
	}
	return ret;
}

/**
* Adds a non-exclusive property. i.e. more than one property with that name can exist.
*/

public boolean add(Property prop)
{
	if (prop == null) return false;
	super.add(prop);
	return true;
}

public boolean add(PropertyList prop)
{
	if (prop != null)
		for (int i = 0; i<prop.size(); i++) add((Property)prop.get(i));
	return true;
}
/**
* Adds a non-exclusive property. i.e. more than one property with that name can exist.
*/
public boolean add(String props,Object value)
{
	boolean ret = true;
	String [] p = mString.split(props);
	for (int i = 0; i<p.length; i++){
		if (!add(new Property(p[i],value))) ret = false;
	}
	return ret;
}
/**
* Removes all properties with the name.
*/
public boolean remove(String props)
{
	String [] p = mString.split(props);
	for (int i = 0; i<p.length; i++){
		while(true){
			Property prop = get(p[i]);
			if (prop == null) break;
			remove(prop);
		}
	}
	return true;
}
/*
public synchronized String catAllValues(String prop)
{
	String ret = null;
	Enumeration e = getProperties(prop);
	while (e.hasMoreElements()){
		Property p = (Property)e.nextElement();
		if (p.value == null) continue;
		if (!(p.value instanceof String)) continue;
		if (ret == null) ret = (String)p.value;
		else ret += p.value;
	}
	return ret;
}
*/
/**
Gather all the property values with a certain name into a Vector.
**/
//===================================================================
public Vector getPropertyValues(String propertyName)
//===================================================================
{
	Vector v = new Vector();
	for (Property p = get(propertyName); p != null; p = get(propertyName,p))
		v.add(p.value);
	return v;
}
/**
* Return an Enumeration of all the properties with a given name.
*/
public Iterator getProperties(String prop)
{
	Vector v = new Vector();
	for (Property p = get(prop); p != null; p = get(prop,p))
		v.add(p);
	return v.iterator();
}
/*
protected static String encodeThese = "{}=|";
public static String
//==================================================================
	textEncodeString(String got)
//==================================================================
{
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<got.length(); i++){
		int ch = got.charAt(i);
		if (ch == ' ') sb.append('+');
		else if (ch >= '0' && encodeThese.indexOf(ch) == -1)
			sb.append((char)ch);
		else{
			sb.append('%');
			ch = ch % 256;
			int c = (ch >> 4) + '0';
			if (c > '9') c = 'A'+c-10-'0';
			sb.append((char)c);
			c = (ch % 16)+'0';
			if (c > '9') c = 'A'+c-10-'0';
			sb.append((char)c);
		}
	}
	return sb.toString();
}
public static String
//==================================================================
	textDecodeString(String got)
//==================================================================
{
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<got.length(); i++){
		char ch = got.charAt(i);
		if (ch == '+') sb.append(' ');
		else if (ch == '%'){
			int val = 0;
			int c2 = got.charAt(i+1);
			if (c2 <= '9') val = 16*(c2-'0'); else val = 16*(c2-'A'+10);
			c2 = got.charAt(i+2);
			if (c2 <= '9') val += c2-'0'; else val += c2-'A'+10;
			sb.append((char)val);
			i+=2;
		}else
			sb.append(ch);
	}
	return sb.toString();
}
//===================================================================
public static String textEncode(PropertyList pl)
//===================================================================
{
	int s = pl.size();
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<s; i++){
		Property p = (Property)pl.get(i);
		if (i != 0) sb.append("&");
		String st = "";
		Object o = p.value;
		if (o != null) {
			if (o instanceof PropertyList)
				st = "{"+textEncodeString(textEncode((PropertyList)o))+"}";
			else
				st = textEncodeString(mString.toString(o));
		}
		sb.append(p.name+"="+st);
	}
	return sb.toString();
}
//===================================================================
public static PropertyList textDecode(String text)
//===================================================================
{
	PropertyList pl = new PropertyList();
	if (text == null) return pl;
	String [] props = mString.split(text,'&');
	for (int i = 0; i<props.length; i++){
		String [] tp = mString.split(props[i],'=');
		Object obj = null;
		if (tp[1].length() > 1)
			if (tp[1].charAt(0) == '{')
				obj = textDecode(textDecodeString(tp[1].substring(1,tp[1].length()-1)));
		if (obj == null) obj = textDecodeString(tp[1]);
		pl.add(new Property(tp[0],obj));
	}
	return pl;
}
*/
/*
	//##################################################################
	static class Decoder{
	//##################################################################

	private List substrings = new Vector();

	//===================================================================
	public PropertyList textDecode(String text)
	//===================================================================
	{
		return textDecode(text.toCharArray(),0,text.length());
	}
	//===================================================================
	public PropertyList textDecode(char [] text,int start,int length)
	//===================================================================
	{
		PropertyList pl = new PropertyList();
		if (text == null) return pl;
		while(length > 0){
			int i = 0;
			for (;i<length && text[start+i] != '=';i++);
			Property p = new Property(new String(text,start,i),null);
			pl.add(p);
			if (i == length) return pl;
			int e = i+1;
			if (e<length)
				for (;e<length && text[start+e] != '&'; e++);
			if (e-(i+1)<2) p.value = textDecodeString(new String(text,start+i+1,e-(i+1)));
			else if (text[i+1] != '{') p.value = textDecodeString(new String(text,start+i+1,e-(i+1)));
			else p.value = textDecode(textDecodeString(new String(text,start+i+2,e-(i+1)-2)));
			start += e+1;
			length -= e+1;
		}
		return pl;
	}
	//##################################################################
	}
	//##################################################################

//===================================================================
public String encodeAsText()
//===================================================================
{
	return textEncode(this);
}
//===================================================================
public void decodeAsText(String what)
//===================================================================
{
	add(textDecode(what));
}
*/

/**
* This will create a new PropertyList and call setStrings() on it.
* @param from The encoded PropertyValues.
* @return a new PropertyList.
* @see setStrings()
*/
//===================================================================
public static PropertyList make(String from)
//===================================================================
{
	PropertyList pl = new PropertyList();
	pl.setStrings(from);
	return pl;
}

//===================================================================
public String [] getNames()
//===================================================================
{
	Object [] get = new Object [size()];
	copyInto(get);
	String [] nm = new String[get.length];
	for (int i = 0; i<get.length; i++)
		nm[i] = ((Property)get[i]).name;
	return nm;
}

//===================================================================
public static PropertyList fromStrings(String s)
//===================================================================
{
	PropertyList pl = new PropertyList();
	pl.setStrings(s);
	return pl;
}

	//##################################################################
	public static class NullPropertyList extends PropertyList{
	//##################################################################

	public boolean set(Property p) {return false;}
	public boolean add(Property p) {return false;}
	public Property get(String name) {return null;}

	//##################################################################
	}
	//##################################################################

//===================================================================
public String getText()
//===================================================================
{
	Vector v1 = new Vector();
	Vector v2 = new Vector();
	for (int i = 0; i<size(); i++){
		Property p = (Property)get(i);
		v1.add(p.name); v2.add(p.value);
	}
	TextEncoder te = new TextEncoder();
	te.addValue(TextEncoder.toString(v1),TextEncoder.toString(v2));
	return te.toString();
}
//===================================================================
public void setText(String text)
//===================================================================
{
	clear();
	TextDecoder td = new TextDecoder(text);
	if (td.size() <= 0) return;
	Vector v1 = new Vector();
	Vector v2 = new Vector();
	TextEncoder.fromString(v1,td.getName(0));
	TextEncoder.fromString(v2,td.getValue(0));
	for (int i = 0; i<v1.size() && i<v2.size(); i++)
		add(new Property(mString.toString(v1.get(i)),v2.get(i)));
}
//===================================================================
public void setField(String name,ewe.reflect.Wrapper wrapper)
//===================================================================
{
	Property p = get(name);
	if (p == null) {
		p = new Property(name,null);
		p.set(wrapper);
		set(p.name,p.value);
	}else
		p.set(wrapper);
}
//===================================================================
public void getField(String name,ewe.reflect.Wrapper wrapper)
//===================================================================
{
	Property p = get(name);
	if (p == null) p = new Property(name,null);
	p.get(wrapper);
}
/*
//===================================================================
public static void main(String args[])
//===================================================================
{
	PropertyList pl = new PropertyList();
	pl.add("First","Is there!");
	pl.add("Second",new Time(22,8,1968));
	PropertyList main = new PropertyList();
	main.add("Left","This is on the left.");
	main.add("Inside",pl);
	main.add("Right",new Time(3,11,1965));
	String got = main.getText();
	PropertyList pl2 = new PropertyList();
	pl2.setText(got);
	System.out.println(pl2);
}
*/

//===================================================================
public static PropertyList getProperties(Object obj)
//===================================================================
{
	if (!(obj instanceof HasProperties)) return null;
	return ((HasProperties)obj).getProperties();
}
//===================================================================
public static boolean getSetProperties(Object obj,String fieldName,ewe.reflect.Wrapper wrapper,boolean isGet)
//===================================================================
{
	PropertyList pl = getProperties(obj);
	if (pl == null) return false;
	if (!isGet) pl.setField(fieldName,wrapper);
	else pl.getField(fieldName,wrapper);
	return true;
}
//===================================================================
public boolean _getSetField(String fieldName,ewe.reflect.Wrapper wrapper,boolean isGet)
//===================================================================
{
	return PropertyList.getSetProperties(this,fieldName,wrapper,isGet);
}
/**
 * This will convert a set of properties encoded as Strings separated by '|' characters into a PropertyList
	 OR it will return the PropertyList if the parameter implements HasProperties, OR it will return a nullPropertyList object
 * @param propertyListOrStrings If this is a String it will be decoded using
 * @return A new PropertyList or the nullPropertyList object if the specified cannot be converted to a PropertyList.
 */
//===================================================================
public static PropertyList toPropertyList(Object propertyListOrStrings)
//===================================================================
{
	if (propertyListOrStrings instanceof String) return make((String)propertyListOrStrings);
	else if (propertyListOrStrings instanceof HasProperties){
		PropertyList pl = ((HasProperties)propertyListOrStrings).getProperties();
		return pl == null ? nullPropertyList : pl;
	}else
		return nullPropertyList;
}
/**
 * This first converts the propertyListOrStrings parameters into a valid or empty PropertyList
 * and then calls getValue(name,defaultValue) on that list.
 * @param propertyListOrStrings
 * @param name
 * @param defaultValue
 * @return
 */
//===================================================================
public static Object getValue(Object propertyListOrStrings,String name,Object defaultValue)
//===================================================================
{
	return toPropertyList(propertyListOrStrings).getValue(name,defaultValue);
}
/**
 * This first converts the propertyListOrStrings parameters into a valid or empty PropertyList
 * and then calls getInt(name,defaultValue) on that list.
 **/
//===================================================================
public static int getInt(Object propertyListOrStrings,String name,int defaultValue)
//===================================================================
{
	return toPropertyList(propertyListOrStrings).getInt(name,defaultValue);
}
/**
 * This first converts the propertyListOrStrings parameters into a valid or empty PropertyList
 * and then calls getDouble(name,defaultValue) on that list.
 **/
//===================================================================
public static double getDouble(Object propertyListOrStrings,String name,double defaultValue)
//===================================================================
{
	return toPropertyList(propertyListOrStrings).getDouble(name,defaultValue);
}
/**
 * This first converts the propertyListOrStrings parameters into a valid or empty PropertyList
 * and then calls getBoolean(name,defaultValue) on that list.
 **/
//===================================================================
public static boolean getBoolean(Object propertyListOrStrings,String name,boolean defaultValue)
//===================================================================
{
	return toPropertyList(propertyListOrStrings).getBoolean(name,defaultValue);
}
/**
 * This first converts the propertyListOrStrings parameters into a valid or empty PropertyList
 * and then calls getString(name,defaultValue) on that list.
 **/
//===================================================================
public static String getString(Object propertyListOrStrings,String name,String defaultValue)
//===================================================================
{
	return toPropertyList(propertyListOrStrings).getString(name,defaultValue);
}
//===================================================================
public void readConfigFile(ewe.io.StreamReader br)
//===================================================================
{
	PropertyList cur = new PropertyList();
	add("default",cur);
	String previous = null;
		while(true){
			String line = br.readLine();
			if (line == null) break;
			if (previous != null){
				String thisData = line.trim();
				String p = previous;
				if (!thisData.endsWith("\\")) previous = null;
				//else thisData = thisData.substring(0,thisData.length()-1);
				cur.set(p,getString(p,"")+thisData);
				continue;
			}
			if (line.length() == 0) continue;
			if (line.charAt(0) == ';') continue;
			if (line.charAt(0) == '['){
				String nm = line.trim().substring(1,line.length()-1);
				cur = (PropertyList)getValue(nm,null);
				if (cur == null) {
					cur = new PropertyList();
					add(nm,cur);
				}
				continue;
			}
			int idx = line.indexOf('=');
			if (idx == -1) continue;
			previous = line.substring(0,idx).trim();
			String p = previous;
			String data = line.substring(idx+1).trim();
			if (!data.endsWith("\\")) previous = null;
			else data = data.substring(0,data.length()-1);
			cur.add(p,data);
		}
		if (size() > 1){
			PropertyList pl = (PropertyList)getValue("default",null);
			if (pl != null)
				if (pl.size() == 0)
					remove("default");
		}
		br.close();
}

/**
 * Put the PropertyList values into a Hashtable.
 * @param destination the destination Hashtable or null to return a new one.
 * @return the destination Hashtable or a new one if destination was null.
 */
//===================================================================
public Hashtable toHashtable(Hashtable destination)
//===================================================================
{
	if (destination == null) destination = new Hashtable();
	int max = size();
	for (int i = 0; i<max; i++){
		Property p = (Property)get(i);
		if (p != null) destination.put(p.name,p.value);
	}
	return destination;
}
/**
 * Put the values in the Hashtable into the PropertyList.
 * @param source the source Hashtable.
 */
//===================================================================
public void fromHashtable(Hashtable source)
//===================================================================
{
	for(Iterator it = source.entries(); it.hasNext();){
		Map.MapEntry me = (Map.MapEntry)it.next();
		set(me.getKey().toString(),me.getValue());
	}
}
//#####################################################################
}
//#####################################################################

