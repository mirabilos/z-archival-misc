package java.lang;
import ewe.util.CharArray;
import ewe.sys.Vm;
import ewe.sys.Convert;

//##################################################################
public class StringBuilder{
//##################################################################

private CharArray chars = new CharArray();

/** Constructs an empty string buffer. */
//===================================================================
public StringBuilder(){}
//===================================================================
/** Constructs a string buffer containing the given string. */
//===================================================================
public StringBuilder(String s){append(s);}
//===================================================================
/** Constructs an empty string buffer with the specified capacity. */
//===================================================================
public StringBuilder(int length){ensureCapacity(length);}
//===================================================================
//public StringBuilder(CharSequence chars){append(chars == null ? (String)null : chars.toString());}
//===================================================================
public StringBuilder append(String s)
//===================================================================
{
	if (s == null) return append("null");
	if (s.length() != 0) return append(Vm.getStringChars(s),0,s.length());
	return this;
}
//===================================================================
public StringBuilder append(char[] chars)
//===================================================================
{
	if (chars == null) return append("null");
	return append(chars,0,chars.length);
}
//===================================================================
public StringBuilder append(char[] chars,int offset,int length)
//===================================================================
{
	if (chars == null) return append("null");
	this.chars.append(chars,offset,length);
	return this;
}
//===================================================================
public StringBuilder append(boolean b)
//===================================================================
{
	return append(Convert.toString(b));
}
private final static char [] buff = new char[1];
//===================================================================
public StringBuilder append(char c)
//===================================================================
{
	synchronized(buff){
		buff[0] = c;
		return append(buff,0,1);
	}
}
//===================================================================
public StringBuilder append(int i)
//===================================================================
{
	return append(Convert.toString(i));
}
//===================================================================
public StringBuilder append(float f)
//===================================================================
{
	return append(Convert.toString(f));
}
//===================================================================
public StringBuilder append(double d)
//===================================================================
{
	return append(Convert.toString(d));
}
//===================================================================
public StringBuilder append(long l)
//===================================================================
{
	return append(Convert.toString(l));
}
//===================================================================
public StringBuilder append(Object obj)
//===================================================================
{
	return append(obj == null ? "null" : obj.toString());
}
public StringBuilder append(StringBuilder other)
{
	if (other == null) append("null");
	else if (other.chars.length != 0){
		{
			chars.append(other.chars.data,0,other.chars.length);
		}
	}
	return this;
}
/**
* Returns the number of characters added to the StringBuilder.
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
 * Return a new String that contains the characters in the StringBuilder from start to end-1.
 * @param start The index of the first character - inclusive.
 * @param end The index of the last character - <b>exclusive</b>
 * @return a new String that contains the characters in the StringBuilder from start to end-1.
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
 * Return a new String that contains the characters in the StringBuilder from start to the end of the StringBuilder
 * @param start The index of the first character - inclusive.
 * @return a new String that contains the characters in the StringBuilder from start to the end of the StringBuilder
 */
//===================================================================
public String substring(int start)
//===================================================================
{
	return substring(start,chars.length);
}
/**
* Make sure that the StringBuilder's capacity is at least as large as minimumCapacity.
**/
//===================================================================
public void ensureCapacity(int minimumCapacity)
//===================================================================
{
	chars.ensureCapacity(minimumCapacity);
}
/**
* Set the length of the StringBuilder. If the current length is less than the specified length,
* then sufficient null characters (0) will be appended to make it up to the specified length.
* If the current length is greater than the specified length then the StringBuilder is truncated
* to the specified length.
* @param length The new length.
*/
//===================================================================
public void setLength(int length)
//===================================================================
{
	if (length < 0) throw new IndexOutOfBoundsException(Convert.toString(length));
	ensureCapacity(length);
	for (int i = chars.length; i < length; i++)
		chars.data[i] = 0;
	chars.length = length;
}
//===================================================================
public void setCharAt(int index,char ch)
//===================================================================
{
	{
		if (index < 0 || index >= chars.length) throw new IndexOutOfBoundsException();
		chars.data[index] = ch;
	}
}
//===================================================================
public StringBuilder reverse()
//===================================================================
{
	{
		for (int i = 0, j = chars.length-1; i<j;){
			char t = chars.data[i];
			chars.data[i] = chars.data[j];
			chars.data[j] = t;
			i++; j--;
		}
	}
	return this;
}
//===================================================================
public void getChars(int srcBegin,int srcEnd,char [] dest,int destBegin)
//===================================================================
{
	{
		if (srcBegin > srcEnd || srcBegin < 0 || srcEnd > chars.length) throw new IndexOutOfBoundsException();
		System.arraycopy(chars.data,srcBegin,dest,destBegin,srcEnd-srcBegin);
	}
}
/**
Remove characters from the StringBuilder..
 * @param start The index of the first character - inclusive.
 * @param end The index of the last character - <b>exclusive</b>
 * @return a new String that contains the characters in the StringBuilder from start to end-1.
**/
//===================================================================
public StringBuilder delete(int start,int end)
//===================================================================
{
	{
		if (start < 0 || start > end || start > chars.length) throw new StringIndexOutOfBoundsException();
		if (end > chars.length) end = chars.length;
		int num = end-start;
		if (num <= 0) return this;
		if (start+num < chars.length)
			System.arraycopy(chars.data,start+num,chars.data,start,chars.length-(start+num));
		chars.length -= num;
	}
	return this;
}
//===================================================================
public StringBuilder deleteCharAt(int index)
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
public StringBuilder insert(int index,char []ch,int offset,int length)
//===================================================================
{
	{
		ensureCapacity(chars.length+length);
		try{
			if (index > chars.length) throw new IndexOutOfBoundsException();
			else if (index < chars.length) System.arraycopy(chars.data,index,chars.data,index+length,chars.length-index);
			System.arraycopy(ch,offset,chars.data,index,length);
			chars.length += length;
		}catch(IndexOutOfBoundsException e){
			throw new StringIndexOutOfBoundsException();
		}
	}
	return this;
}
//===================================================================
public StringBuilder insert(int index,String string)
//===================================================================
{
	if (string == null) string = "null";
	return insert(index,Vm.getStringChars(string),0,string.length());
}
//===================================================================
public StringBuilder insert(int index,char [] chars)
//===================================================================
{
	if (chars == null) return insert(index,"null");
	return insert(index,chars,0,chars.length);
}
//===================================================================
public StringBuilder insert(int index,boolean value)
//===================================================================
{
	return insert(index,Convert.toString(value));
}
//===================================================================
public StringBuilder insert(int index,double value)
//===================================================================
{
	return insert(index,Convert.toString(value));
}
//===================================================================
public StringBuilder insert(int index,float value)
//===================================================================
{
	return insert(index,Convert.toString(value));
}
//===================================================================
public StringBuilder insert(int index,int value)
//===================================================================
{
	return insert(index,Convert.toString(value));
}
//===================================================================
public StringBuilder insert(int index,long value)
//===================================================================
{
	return insert(index,Convert.toString(value));
}
//===================================================================
public StringBuilder insert(int index,Object value)
//===================================================================
{
	return insert(index,(value == null ? "null" : value.toString()));
}
//===================================================================
public StringBuilder insert(int index,char value)
//===================================================================
{
	synchronized(buff){
		buff[0] = value;
		return insert(index,buff,0,1);
	}
}
//===================================================================
public StringBuilder replace(int start,int end,String str)
//===================================================================
{
	delete(start,end);
	return insert(start,str);
}
//##################################################################
/* (non-Javadoc)
 * @see java.lang.CharSequence#subSequence(int, int)
 */
/*
public CharSequence subSequence(int start, int end) {
	return substring(start,end);
}
public StringBuilder append(CharSequence chars) {
	return append(chars == null ? (String)null : chars.toString());
}
public StringBuilder append(CharSequence chars, int start, int end) {
	return append(chars == null ? (String)null : chars.subSequence(start,end).toString());
}
*/
}
//##################################################################

