package ewe.util;
/**
A DataParser is used to extract numeric and textual information from a formated line of text.
It works in a similar fashion to C/C++ scanf() routines, in that you specify the format of the
data using "%" fields in a format String.
<p>
To parse a large text file you should use the ewe.io.StreamScanner class. This can scan entire
files at the maximum speed while not creating any objects for each scan.
<p>
<b>Specifying Data Format</b>
<p>
You specify the data you wish to parse as a String with a set of '%' formats <b>separated by spaces</b>, similar to C/C++ scanf
function. You can scan for Strings (either as fixed length Strings or individual words),
integer/long values or floating point values.
<p>
The formats you can use for scanning numbers are:<p>
<b>%#i  %#f %#d</b><br>
'i' indicates an integer value and 'f' or 'd' indicates a floating point (double) value. <br>
'#' indicates an <b>optional</b> number specifying the number of digits to read.
If you do not specify a number of digits, then all non-space characters will be read in and
then converted to a number.
<p>
For strings, words or characters use:<p>
<b>%#c %#s %q</b><br>
'c' indicates a single character (byte) to read. <br>
's' indicates a single word or String to read.<br>
'q' indicates a word or set of words that may be in quotes. In other words, if the first
character read is a ' or " character, then all characters will be read until a matching
quote is found. If the first character is not a quote character, then only the first word
is read in.<br>
'#' indicates an <b>optional</b> number specifying the number of characters to read (note that
this cannot be used with the 'q' format).<br>
Note that '%10c' and '%10s' will have the same effect - i.e. both will read in a string of 10
characters, but '%c' reads a single character and '%s' reads the next single word.
<p>
<b>Skipping fields</b> - Using a '!' character instead of a '%' character will indicate that the specified
field should be skipped over instead of being converted and returned.
<p>
<b>Retrieving the Parsed Data</b>
<p>
This can be done in two ways. The parse() methods return an Object array that contains a single
Object for each '%' field in the scan string (but NOT for any '!' fields). Each object will
be either a ewe.sys.Long object (for %i fields), ewe.sys.Double object (for %f fields) and a
ewe.util.SubString object (for all text fields). So a scan of "%10s !5s %i %f" will return an
array of 3 objects. The object at index 0 will be a SubString, the one at index 1 will be a Long
object and the one at index 2 will be a Double object. Note that these objects are re-used for
the next parse().
<p>
You can also ignore the return value of parse() and instead call one of the getXXX() methods
to retrieve a particular data type from the scanned array of values. Using the same example
"%10s !5s %i %f" after a parse you could call getString(0) followed by getInt(1) followed by
getDouble(2). These calls are only valid until the next parse().
**/
//##################################################################
public class DataParser{
//##################################################################
private static boolean hasNativeParse = true;
private int [] formats;
private Object [] values;

private static final int INTEGER = 2;
private static final int SKIP_INTEGER = 3;
private static final int FLOAT = 4;
private static final int SKIP_FLOAT = 5;
private static final int CHAR = 6;
private static final int SKIP_CHAR = 7;
private static final int TEXT = 8;
private static final int SKIP_TEXT = 9;
private static final int QUOTED = 10;
private static final int SKIP_QUOTED = 11;

String format;

//===================================================================
public String getFormat()
//===================================================================
{
	return format;
}
/**
 * Create a new DataParser for the specified format.
 * @param format The format in % notation.
 * @exception IllegalArgumentException if the format is malformed.
 */
//===================================================================
public DataParser(String format) throws IllegalArgumentException
//===================================================================
{
	this.format = format;
	String [] got = ewe.util.mString.split(format,' ');
	int [] specs = new int[got.length];
	Object [] parses = new Object[specs.length];
	int p = 0;
	for (int i = 0; i<got.length; i++){
		int s = 0;
		String f = got[i];
		switch(f.charAt(f.length()-1)){
			case 'i': s = INTEGER; parses[p++] = new ewe.sys.Long(); break;
			case 'd':
			case 'f': s = FLOAT; parses[p++] = new ewe.sys.Double(); break;
			case 'c': s = CHAR; parses[p++] = new ewe.util.SubString(); break;
			case 's': s = TEXT; parses[p++] = new ewe.util.SubString(); break;
			case 'q': s = QUOTED; parses[p++] = new ewe.util.SubString(); break;
			default:
				throw new IllegalArgumentException("Bad format: "+f);
		}
		if (f.charAt(0) == '!'){
			s |= 0x1;
			p--;
		}else if (f.charAt(0) != '%') throw new IllegalArgumentException("Bad format: "+f);
		if (f.length() > 2){
			int len = ewe.sys.Convert.toInt(f.substring(1,f.length()-1));
			if (len < 1) throw new IllegalArgumentException("Bad format: "+f);
			s |= (len << 8);
		}else{
			if (s == CHAR || s == SKIP_CHAR) s |= 1 << 8;
		}
		specs[i] = s;
	}
	if (p != parses.length) {
		Object [] o2 = new Object[p];
		ewe.sys.Vm.copyArray(parses,0,o2,0,p);
		parses = o2;
	}
	formats = specs;
	values = parses;
}

private char [] chars;
/**
 * This creates a new DataParser for the specified format and then parses the String.
 * @param data The data to parse.
 * @param format The format string.
 * @return The DataParser created and used for the parsing. Use the getXXX() methods to retrieve
	the parsed values.
 * @exception IllegalArgumentException if the format is malformed.
* @exception IndexOutOfBoundsException if there was not enough data to parse all formats.
 */
//===================================================================
public static DataParser parseString(String data,String format) throws IllegalArgumentException, IndexOutOfBoundsException
//===================================================================
{
	DataParser dp = new DataParser(format);
	dp.parse(data);
	return dp;
}
/**
 * Parse a string.
 * @param data the String data.
 * @return A set of objects for each '%' element in the format. This will be
 * a ewe.sys.Long object for integer/long values, a ewe.sys.Double object for double values
 * or a ewe.util.SubString for string/text values.
* @exception IndexOutOfBoundsException if there was not enough data to parse all formats.
 */
//===================================================================
public Object [] parse(String data) throws IndexOutOfBoundsException
//===================================================================
{
	return parse(ewe.sys.Vm.getStringChars(data),0,data.length());
}
/**
 * Parse a string of UTF encoded bytes.
 * @param buffer the array containing the bytes.
 * @param start the start of the data bytes in the array.
 * @param length the number of data bytes in the array.
 * @return A set of objects for each '%' element in the format. This will be
 * a ewe.sys.Long object for integer/long values, a ewe.sys.Double object for double values
 * or a ewe.util.SubString for string/text values.
* @exception IndexOutOfBoundsException if there was not enough data to parse all formats.
 */
//===================================================================
public Object [] parse(byte[] buffer,int start,int length) throws IndexOutOfBoundsException
//===================================================================
{
	chars = ewe.util.Utils.decodeJavaUtf8String(buffer,start,length,chars,0);
	int size = ewe.util.Utils.sizeofJavaUtf8String(buffer,start,length);
	parse(chars,0,size);
	return values;
}

//-------------------------------------------------------------------
private native void nativeParse(Object [] values,int [] types,char [] chars,int start,int length);
//-------------------------------------------------------------------

/**
 * Parse a string of UTF encoded bytes.
 * @param chars the array containing the characters.
 * @param start the start of the data bytes in the array.
 * @param length the number of data bytes in the array.
 * @return A set of objects for each '%' element in the format. This will be
 * a ewe.sys.Long object for integer/long values, a ewe.sys.Double object for double values
 * or a ewe.util.SubString for string/text values.
* @exception IndexOutOfBoundsException if there was not enough data to parse all formats.
*/
//===================================================================
public Object [] parse(char [] chars,int start,int length) throws IndexOutOfBoundsException
//===================================================================
{
	if (hasNativeParse)try{
		nativeParse(values,formats,chars,start,length);
		return values;
	}catch(UnsatisfiedLinkError e){
		hasNativeParse = false;
	}catch(SecurityException e){
		hasNativeParse = false;
	}
	int pos = start, p = 0;
	int end = start+length;
	for (int i = 0; i<formats.length; i++){
		int type = formats[i] & 0xff;
		if (pos >= end) throw new IndexOutOfBoundsException("Cannot read element: "+(i+1));
		int len = (formats[i] >> 8) & 0xffffff;
		start = pos;
		int w = 0;
		if (len == 0){
			for(;pos < end && Character.isWhitespace(chars[pos]);pos++)
				;
			start = pos;
			if (pos >= end){
				i--;
				continue;
			}
			char q = 0;
			if (type == QUOTED || type == SKIP_QUOTED){
				if (chars[pos] == '\'' || chars[pos] == '"'){
					w++;
					q = chars[pos];
					for(pos++;pos < end && chars[pos] != q;pos++)
						w++;
					if (pos < end){
						w++;
						pos++;
					}
				}else{
					for(;pos < end && !Character.isWhitespace(chars[pos]);pos++)
						w++;
				}
			}else
				for(;pos < end && !Character.isWhitespace(chars[pos]);pos++)
					w++;
		}else{
			pos += (w = len);
			if (pos > end) w = 0;
		}
		if (w == 0) {
			i--; //<= This will cause an exception.
		}else{ // Parse the line.
			if ((type & 1) != 0) continue; //Skip over this section.
			switch(type){
				case INTEGER: ((ewe.sys.Long)values[p++]).value = ewe.sys.Convert.toLong(chars,start,w); break;
				case FLOAT: ((ewe.sys.Double)values[p++]).value = ewe.sys.Convert.toDouble(chars,start,w); break;
				default:
					ewe.util.SubString ss = (ewe.util.SubString)values[p++];
					ss.data = chars;
					ss.start = start;
					ss.length = w;
					break;
			}
		}
	}
	return values;
}

/**
 * Get the parsed value at the specified index. This will either be a ewe.sys.Long or a ewe.sys.Double
	or a ewe.util.SubString;
 * @param index The index of the retrieved value.
 * @return The Object at that value.
 * @exception IndexOutOfBoundsException if the index is out of bounds.
 */
//===================================================================
public Object getValue(int index) throws IndexOutOfBoundsException
//===================================================================
{
	return values[index];
}
/**
 * Use this to get the value that was just parsed at the specified index.
 * @param index The index of the value for the '%i' element as specified in the format string.
 * @return The long value at that index.
 * @exception IllegalArgumentException If the element did not denote an integer value.
 * @exception IndexOutOfBoundsException If the index is out of bounds.
 */
//===================================================================
public long getLong(int index) throws IllegalArgumentException, IndexOutOfBoundsException
//===================================================================
{
	try{
		return ((ewe.sys.Long)values[index]).value;
	}catch(ClassCastException e){
		throw new IllegalArgumentException();
	}
}
/**
 * Use this to get the value that was just parsed at the specified index.
 * @param index The index of the value for the '%i' element as specified in the format string.
 * @return The integer value at that index.
 * @exception IllegalArgumentException If the element did not denote an integer value.
 * @exception IndexOutOfBoundsException If the index is out of bounds.
 */
//===================================================================
public int getInt(int index) throws IllegalArgumentException, IndexOutOfBoundsException
//===================================================================
{
	return (int)getLong(index);
}
/**
 * Use this to get the value that was just parsed at the specified index.
 * @param index The index of the value for the '%f' element as specified in the format string.
 * @return The double value at that index.
 * @exception IllegalArgumentException If the element did not denote an integer value.
 * @exception IndexOutOfBoundsException If the index is out of bounds.
 */
//===================================================================
public double getDouble(int index) throws IllegalArgumentException, IndexOutOfBoundsException
//===================================================================
{
	try{
		return ((ewe.sys.Double)values[index]).value;
	}catch(ClassCastException e){
		throw new IllegalArgumentException();
	}
}
/**
 * Use this to get the value that was just parsed at the specified index.
 * @param index The index of the value for the '%' element as specified in the format string.
 * @return The SubString value at that index.
 * @exception IllegalArgumentException If the element did not denote an integer value.
 * @exception IndexOutOfBoundsException If the index is out of bounds.
 */
//===================================================================
public SubString getSubString(int index) throws IllegalArgumentException, IndexOutOfBoundsException
//===================================================================
{
	try{
		return (ewe.util.SubString)values[index];
	}catch(ClassCastException e){
		throw new IllegalArgumentException();
	}
}
/**
 * Use this to get the value that was just parsed at the specified index.
 * @param index The index of the value for the '%' element as specified in the format string.
 * @return The String value at that index.
 * @exception IllegalArgumentException If the element did not denote an integer value.
 * @exception IndexOutOfBoundsException If the index is out of bounds.
 */
//===================================================================
public String getString(int index) throws IllegalArgumentException, IndexOutOfBoundsException
//===================================================================
{
	return getSubString(index).toString();
}
/*
//===================================================================
public static void main(String args[])
//===================================================================
{
	DataParser dp = DataParser.parseString("Hello abcdef1234.5678","%s %4c !3c %f");
	ewe.sys.Vm.debug(dp.getString(0)+", "+dp.getString(1)+", "+dp.getDouble(2));
}
*/
//##################################################################
}
//##################################################################

