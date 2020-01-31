package java.lang;

//##################################################################
public class StringBuffer{
//##################################################################

private ewe.util.CharArray chars = new ewe.util.CharArray();

/** Constructs an empty string buffer. */
//===================================================================
public StringBuffer(){}
//===================================================================
/** Constructs a string buffer containing the given string. */
//===================================================================
public StringBuffer(String s){append(s);}
//===================================================================
/** Constructs an empty string buffer with the specified capacity. */
//===================================================================
public StringBuffer(int length){ensureCapacity(length);}
//===================================================================

//===================================================================
public StringBuffer append(String s)
//===================================================================
{
	if (s == null) return append("null");
	if (s.length() != 0) return append(ewe.sys.Vm.getStringChars(s),0,s.length());
	return this;
}
//===================================================================
public StringBuffer append(char[] chars)
//===================================================================
{
	if (chars == null) return append("null");
	return append(chars,0,chars.length);
}
//===================================================================
public StringBuffer append(char[] chars,int offset,int length)
//===================================================================
{
	if (chars == null) return append("null");
	this.chars.append(chars,offset,length);
	return this;
}
//===================================================================
public StringBuffer append(boolean b)
//===================================================================
{
	return append(ewe.sys.Convert.toString(b));
}
private static char [] buff = new char[1];
//===================================================================
public StringBuffer append(char c)
//===================================================================
{
	buff[0] = c;
	return append(buff,0,1);
}
//===================================================================
public StringBuffer append(int i)
//===================================================================
{
	return append(ewe.sys.Convert.toString(i));
}
//===================================================================
public StringBuffer append(float f)
//===================================================================
{
	return append(ewe.sys.Convert.toString(f));
}
//===================================================================
public StringBuffer append(double d)
//===================================================================
{
	return append(ewe.sys.Convert.toString(d));
}
//===================================================================
public StringBuffer append(long l)
//===================================================================
{
	return append(ewe.sys.Convert.toString(l));
}
//===================================================================
public StringBuffer append(Object obj)
//===================================================================
{
	return append(obj == null ? "null" : obj.toString());
}
/**
* Returns the number of characters added to the StringBuffer.
**/
//===================================================================
public int length()
//===================================================================
{
	return chars.length;
}
/**
* Convert the added characters to a String.
**/
//===================================================================
public String toString()
//===================================================================
{
	return new String(chars.data,0,chars.length);
}
/**
 * Return a new String that contains the characters in the StringBuffer from start to end-1.
 * @param start The index of the first character - inclusive.
 * @param end The index of the last character - <b>exclusive</b>
 * @return a new String that contains the characters in the StringBuffer from start to end-1.
 */
//===================================================================
public String substring(int start,int end)
//===================================================================
{
	try{
		return new String(chars.data,start,end-start);
	}catch(ArrayIndexOutOfBoundsException e){
		throw new StringIndexOutOfBoundsException();
	}
}
/**
 * Return a new String that contains the characters in the StringBuffer from start to the end of the StringBuffer
 * @param start The index of the first character - inclusive.
 * @return a new String that contains the characters in the StringBuffer from start to the end of the StringBuffer
 */
//===================================================================
public String substring(int start)
//===================================================================
{
	return substring(start,chars.length);
}
/**
* Make sure that the StringBuffer's capacity is at least as large as minimumCapacity.
**/
//===================================================================
public void ensureCapacity(int minimumCapacity)
//===================================================================
{
	chars.ensureCapacity(minimumCapacity);
}
/**
* Set the length of the StringBuffer. If the current length is less than the specified length,
* then sufficient null characters (0) will be appended to make it up to the specified length.
* If the current length is greater than the specified length then the StringBuffer is truncated
* to the specified length.
* @param length The new length.
*/
//===================================================================
public void setLength(int length)
//===================================================================
{
	if (length < 0) throw new IndexOutOfBoundsException(ewe.sys.Convert.toString(length));
	ensureCapacity(length);
	for (int i = chars.length; i < length; i++)
		chars.data[i] = 0;
	chars.length = length;
}
//===================================================================
public void setCharAt(int index,char ch)
//===================================================================
{
	if (index < 0 || index >= chars.length) throw new IndexOutOfBoundsException();
	chars.data[index] = ch;
}
//===================================================================
public StringBuffer reverse()
//===================================================================
{
	for (int i = 0, j = chars.length-1; i<j;){
		char t = chars.data[i];
		chars.data[i] = chars.data[j];
		chars.data[j] = t;
		i++; j--;
	}
	return this;
}
//===================================================================
public void getChars(int srcBegin,int srcEnd,char [] dest,int destBegin)
//===================================================================
{
	if (srcBegin > srcEnd || srcBegin < 0 || srcEnd > chars.length) throw new IndexOutOfBoundsException();
	ewe.sys.Vm.copyArray(chars.data,srcBegin,dest,destBegin,srcEnd-srcBegin);
}
/**
Remove characters from the StringBuffer..
 * @param start The index of the first character - inclusive.
 * @param end The index of the last character - <b>exclusive</b>
 * @return a new String that contains the characters in the StringBuffer from start to end-1.
**/
//===================================================================
public StringBuffer delete(int start,int end)
//===================================================================
{
	if (start < 0 || start > end || start > chars.length) throw new StringIndexOutOfBoundsException();
	if (end > chars.length) end = chars.length;
	int num = end-start;
	if (num <= 0) return this;
	if (start+num < chars.length)
		ewe.sys.Vm.copyArray(chars.data,start+num,chars.data,start,chars.length-(start+num));
	chars.length -= num;
	return this;
}
//===================================================================
public StringBuffer deleteCharAt(int index)
//===================================================================
{
	return delete(index,index+1);
}
//===================================================================
public char charAt(int index)
//===================================================================
{
	return chars.data[index];
}
//===================================================================
public int capacity()
//===================================================================
{
	return chars.data.length;
}
//===================================================================
public StringBuffer insert(int index,char []ch,int offset,int length)
//===================================================================
{
	ensureCapacity(chars.length+length);
	try{
		if (index > chars.length) throw new IndexOutOfBoundsException();
		else if (index < chars.length) ewe.sys.Vm.copyArray(chars.data,index,chars.data,index+length,chars.length-index);
		ewe.sys.Vm.copyArray(ch,offset,chars.data,index,length);
		chars.length += length;
	}catch(IndexOutOfBoundsException e){
		throw new StringIndexOutOfBoundsException();
	}
	return this;
}
//===================================================================
public StringBuffer insert(int index,String string)
//===================================================================
{
	if (string == null) string = "null";
	return insert(index,ewe.sys.Vm.getStringChars(string),0,string.length());
}
//===================================================================
public StringBuffer insert(int index,char [] chars)
//===================================================================
{
	if (chars == null) return insert(index,"null");
	return insert(index,chars,0,chars.length);
}
//===================================================================
public StringBuffer insert(int index,boolean value)
//===================================================================
{
	return insert(index,ewe.sys.Convert.toString(value));
}
//===================================================================
public StringBuffer insert(int index,double value)
//===================================================================
{
	return insert(index,ewe.sys.Convert.toString(value));
}
//===================================================================
public StringBuffer insert(int index,float value)
//===================================================================
{
	return insert(index,ewe.sys.Convert.toString(value));
}
//===================================================================
public StringBuffer insert(int index,int value)
//===================================================================
{
	return insert(index,ewe.sys.Convert.toString(value));
}
//===================================================================
public StringBuffer insert(int index,long value)
//===================================================================
{
	return insert(index,ewe.sys.Convert.toString(value));
}
//===================================================================
public StringBuffer insert(int index,Object value)
//===================================================================
{
	return insert(index,(value == null ? "null" : value.toString()));
}
//===================================================================
public StringBuffer insert(int index,char value)
//===================================================================
{
	buff[0] = value;
	return insert(index,buff,0,1);
}
//===================================================================
public StringBuffer replace(int start,int end,String str)
//===================================================================
{
	delete(start,end);
	return insert(start,str);
}
//##################################################################
}
//##################################################################

