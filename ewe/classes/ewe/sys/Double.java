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
package ewe.sys;

//##################################################################
public class Double extends ewe.data.DataObject implements Value {
//##################################################################
	/**
	 * This is the same value as java.lang.Double.TYPE - it is the Class
	 * that represents the primitive type double.
	 */
public static final Class TYPE = java.lang.Double.TYPE;
public double value;
/*
public int highWord; //Do not move or use this variable.
public int lowWord;//Do not move or use this variable.
*/

//public ewe.sys.Double.set(
/**
* This sets the default number of decimal places for this Double when a
* toString() is done.
**/
public int decimalPlaces = 2;
/**
* Convenience temporary variable.
**/
public static final Double d1 = new Double();
/**
* Convenience temporary variable.
**/
public static final Double d2 = new Double();
/**
* Convenience temporary variable.
**/
public static final Double d3 = new Double();
//===================================================================
public Double set(double d) {value = d; return this;}
public Double set(Double d) {return set(d == null ? 0.0 : d.value);}
//===================================================================
//public Double(float value) {set(value);}
//===================================================================
//public Double(){this(0);}
//===================================================================
/**
* This sets the value of the Double to the float value and returns itself.
**/
//===================================================================
//public native Double set(float floatValue);
//===================================================================
/**
* This sets the value of the Double to the integer value and returns itself.
**/
//===================================================================
//public native Double set(int intValue);
//===================================================================
/**
* This sets the value of the Double to the value of the other Double value and returns itself.
**/
//===================================================================
//public native Double set(Double doubleValue);
//===================================================================
/**
* This sets the value of the Double to the value of the Long value and returns itself.
**/
//===================================================================
//public native Double set(Long longValue);
//===================================================================
/**
* Option for toString().
**/
public static final int ZERO_FILL = 0x1;
/**
* Option for toString().
**/
public static final int TRUNCATE = 0x2;
/**
* Option for toString().
**/
public static final int FREE_DECIMAL = 0x8;
/**
* Option for toString().
**/
public static final int EXP_NOTATION = 0x10;
/**
* Option for toString().
**/
public static final int NO_EXP_NOTATION = 0x20;
/**
* Option for toString().
**/
public static final int AT_LEAST_ONE_DECIMAL = 0x40;
/**
* This prints out the value in a particular format.
* @param length The total length of the output string. Set this to zero to specify no maximum length constraint.
* @param decimalLength the number of decimal places to output. If FREE_DECIMAL is specified as an option, then this will specify the
* MAXIMUM number of decimal digits to display - it may actually display less. If FREE_DECIMAL is specified AND decimal places is zero, then the VM will decide how many
* decimal places to display.
* @param options Must be the bitwise ORing of:
<br>ZERO_FILL - Pad with leading Zeros to the specified length.
<br>TRUNCATE - Truncate any digits after the maximum decimal digits (by default it will round up).
<br>FREE_DECIMAL - Actual number of decimals output depend on the value of decimalLength and the number of significant decimal digits.
<br>EXP_NOTATION - Specify that Exp. notation MUST be used.
<br>NO_EXP_NOTATION - Specify that Exp. notation MUST NOT be used.
<br>AT_LEAST_ONE_DECIMAL - Specify that at least one decimal place must be shown.
* @return A string representation of the double value.
*/
//===================================================================
public native String toString(int length,int decimalLength,int options);
public String toString() {return toString(0,decimalPlaces,0);}
//===================================================================
public native void fromString(String value);
//===================================================================
//public native float toFloat();
//public native int toInt();
//public native Long toLong(Long dest);
//===================================================================
//public native Double multiply(Double other,Double ret);
//public native Double divide(Double other,Double ret);
//public native Double add(Double other,Double ret);
//public native Double subtract(Double other,Double ret);
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (other == this) return 0;
	if (!(other instanceof ewe.sys.Double)) return 1;
	if (value > ((ewe.sys.Double)other).value) return 1;
	if (value < ((ewe.sys.Double)other).value) return -1;
	return 0;
}
//==================================================================
public final static int NEGATIVE = 1;
public final static int POSITIVE = 2;
public final static int ZERO = 3;
public final static int POSITIVE_INFINITY = 4;
public final static int NEGATIVE_INFINITY = 5;
public final static int INFINITY = 6;
public final static int NAN = 7;
public final static int VALID = 8;

//-------------------------------------------------------------------
private static Double buffer = new Double();
//-------------------------------------------------------------------
/**
* Returns true if the number is NOT NaN and is NOT an INFINITY value.
**/
//===================================================================
public static boolean isValid(double value)
//===================================================================
{
	return buffer.set(value).is(VALID);
}
/**
* This checks to see if the value of the double is one of the special values
* (POSITIVE_INFINITY, NEGATIVE_INFINITY, INFINITY(either), NAN) or if it
* is NEGATIVE, POSITIVE or ZERO.
**/
//===================================================================
public native boolean is(int special);
//===================================================================
/**
* This sets the double to one of the following values: POSITIVE_INFINITY(same as INFINITY), NEGATIVE_INFINITY
* NAN, NEGATIVE (-1), POSITIVE (+1), ZERO.
**/
//===================================================================
public native Double setSpecial(int special);
//===================================================================
/**
* This creates an array of natively stored double values. It is FAR more memory effecient
* than creating an array of Double objects. It is actually implemented currently as a byte
* array with eight bytes per double value.
**/
//===================================================================
//public native static Object createArray(int length);
//===================================================================
/**
* This gets an individual double value at the specified index in the doubleArray (which
* must have been created using createArray() and stores it in dest. If dest is null a
* new Double will be created. The return value is the Double which contains the value.
* If the index is out of bounds null is returned. Modifying the value stored in the destination for
* this method will NOT afect the data in the array. You MUST use setArrayValue() for this.
**/
//===================================================================
//public native static Double getArrayValue(Object doubleArray,int index,Double dest);
//===================================================================
/**
* This sets an individual double value at the specified index in the doubleArray (which
* must have been created using createArray(). The return value is the source value or
* null if the index is out of bounds.
**/
//===================================================================
//public native static Double setArrayValue(Object doubleArray,int index,Double source);
//===================================================================
/**
* This returns the length of the Double array. If the array is null then a null pointer
* error will be thrown.
**/
//===================================================================
//public native static int arrayLength(Object doubleArray);
//===================================================================
/**
* This copies from the source array into the dest array for length number of
* entries. If the destination array is null it will be set to an empty array.
* If the destination array is not big enough (or null) to hold all the data starting at
* the offset specified, a new array will be created which is big enough and
* this new array will be returned.
*
* This will NOT safely move the contents of an array within itself.
* 1. Get a subarray or copy of an array. if (destArray == null).
* 2. Copy one array into another if (destArray != null)
* 3. Append one array to another if (destStart == destArray.length)
**/
//===================================================================
//public native static Object arrayJoin(Object sourceArray,int sourceStart,Object destArray,int destStart,int length);
//===================================================================
/**
* Return an int array representing the data in the array.
**/
//===================================================================
//public native static int [] toIntArray(Object doubleArray,int start,int length);
//===================================================================
/**
* Return a float array representing the data in the array.
**/
//===================================================================
//public native static float [] toFloatArray(Object doubleArray,int start,int length);
//===================================================================
/**
* Return a Long array representing the data in the array.
**/
//===================================================================
//public native static Object toLongArray(Object doubleArray,int start,int length);
//===================================================================
/**
* Save the values in the array as bit values in the destination byte array.
**/
//==============================================================================================
//public native static void save(Object doubleArray,int start,byte [] dest,int destStart,int length);
//==============================================================================================
/**
* Load the values in the array as bit values from the source byte array.
**/
//==============================================================================================
//public native static void load(Object doubleArray,int start,byte [] source,int sourceStart,int length);
//==============================================================================================
public  native static void matrixFunction(
double [] doubleMatrix,int startIndex,int rows,int columns,
MathFunction [] functions,double [] destDoubleArray,int destStartIndex);
//===================================================================
/*
{
	double [] ret = (double [])destDoubleArray;
	double [] buffer = new double[functions.length];
	double [] data = (double [])doubleMatrix;

	MathFunctionData fd = new MathFunctionData();
	fd.doubleArrayOfParameters = doubleMatrix;
	fd.parameterCount = columns;
	fd.parameterStartIndex = startIndex;
	Double dest = new Double();
	for (int r = 0; r<rows; r++){
		fd.rowIndex = r;
		for (int f = 0; f < functions.length; f++){
			fd.result = dest.set(0);
			fd.colIndex = f;
			functions[f].doFunction(fd);
			buffer[f] = dest.value;
		}
		for (int f = 0; f < functions.length; f++)
			ret[destStartIndex++] = buffer[f];
		fd.parameterStartIndex += columns;
	}
}
*/
/**
* This gets the double value from the bit-wise representation of it in the Long
* value. It returns itself;
**/
//===================================================================
//public native Double fromBits(Long longBits);
//===================================================================
/**
* This stores the bit-wise representation of the double in the Long value. If dest
* is null a new Long will be created. The return value is the Long with the bit-wise
* representation stored within it.
**/
//===================================================================
//public native Long toBits(Long dest);
//===================================================================

//===================================================================
public void copyFrom(Object other)
//===================================================================
{
	if (other instanceof Double)
		value = ((Double)other).value;
		//set((Double)other);
}
/**
 * Place the value in the standard java Wrapper (e.g. java.lang.Double, java.lang.Float, etc)
 * into this ewe.sys.Double. The Character and Boolean values may not be converted to double.
 * @param javaWrapper one of the java.lang primitive wrapper values representing
	a value that may be converted into a double value.
 * @return this ewe.sys.Double after the value has been placed in it.
* @exception IllegalArgumentException if the javaWrapper parameter is one of the illegal types.
*/
//===================================================================
public ewe.sys.Double fromJavaWrapper(Object javaWrapper) throws IllegalArgumentException
//===================================================================
{
	if (javaWrapper == null) return set(0);
	if (javaWrapper instanceof	java.lang.Long)
		return set(((java.lang.Long)javaWrapper).doubleValue());
	if (javaWrapper instanceof java.lang.Integer)
		return set(((java.lang.Integer)javaWrapper).doubleValue());
	if (javaWrapper instanceof java.lang.Short)
		return set(((java.lang.Short)javaWrapper).doubleValue());
	if (javaWrapper instanceof java.lang.Byte)
		return set(((java.lang.Byte)javaWrapper).doubleValue());
	if (javaWrapper instanceof java.lang.Double)
		return set(((java.lang.Double)javaWrapper).doubleValue());
	if (javaWrapper instanceof java.lang.Float)
		return set(((java.lang.Float)javaWrapper).doubleValue());
	throw new IllegalArgumentException();
}
//##################################################################
}
//##################################################################

