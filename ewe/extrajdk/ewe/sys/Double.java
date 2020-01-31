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
public class Double extends ewe.data.DataObject implements Value{
//##################################################################
public double value;

//public int highWord; //Do not move or use this variable.
//public int lowWord;//Do not move or use this variable.

/**
* This is the default number of decimal places to print when toString() is called.
**/
public int decimalPlaces = 2;


public static final Class TYPE = java.lang.Double.TYPE;
protected final static String Infinity = "Inf";
protected final static String NaN = "NaN";
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
//public Double set(float floatValue){value = floatValue; return this;}
//===================================================================
/**
* This sets the value of the Double to the integer value and returns itself.
**/
//===================================================================
//public Double set(int intValue){value = intValue; return this;}
//===================================================================
/**
* This sets the value of the Double to the value of the other Double value and returns itself.
**/
//===================================================================
//public Double set(Double doubleValue){value = doubleValue.value; return this;}
//===================================================================
/**
* This sets the value of the Double to the value of the Long value and returns itself.
**/
//===================================================================
//public Double set(ewe.sys.Long longValue){value = longValue.value; return this;}
//===================================================================

//-------------------------------------------------------------------
private static String cantFit(String tryValue,int length)
//-------------------------------------------------------------------
{
	if (tryValue != null)
		if ((tryValue.length() <= length) || (length == 0))
			return tryValue;
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<length; i++) sb.append('#');
	return sb.toString();
}
public static final int ZERO_FILL = 0x1;
public static final int TRUNCATE = 0x2;
public static final int FREE_DECIMAL = 0x8;
public static final int EXP_NOTATION = 0x10;
public static final int NO_EXP_NOTATION = 0x20;
public static final int AT_LEAST_ONE_DECIMAL = 0x40;
/**
* This prints out the value in a particular format.
* totalLength = the total length of the output string. Set to zero to specify no length constraint.
* decimal = the number of decimal places to output.
* options = The OR'ed value of the following:
*    ZERO_FILL = Add zeros to the front of the output to pad up to the length specified.
*                if this is not specified AND length is not zero, then spaces will be used to
*                pad up to the length.
*    TRUNCATE = The last decimal place is not rounded.
*    FREE_DECIMAL = Only as many decimal places is shown as is needed (up to a maximum of decimal).
*                   Also allows decimal places to be sacrificed to fit into totalLength.
**/
protected static double ln10 = 0;
protected static double maxF = 0, minF = 0;
//===================================================================
public String toString(int totalLength,int decimal,int options)
//===================================================================
{return toString(value,totalLength,decimal,options);}
//===================================================================

//===================================================================
private static int lengthOfLong(long value)
//===================================================================
{
	int len = 1;
	if (value < 0) value *= -1;
	if (value != 0){
		long cur = value;
		while(true){
			cur /= 10;
			if (cur == 0) break;
			len++;
		}
	}
	return len;
}

//===================================================================
public static String toString(double value,int totalLength,int decimal,int options)
//===================================================================
{
	if (value == java.lang.Double.NEGATIVE_INFINITY) return cantFit("-"+Infinity,totalLength);
	else if (value == java.lang.Double.POSITIVE_INFINITY) return cantFit("+"+Infinity,totalLength);
	else if (java.lang.Double.isNaN(value)) return cantFit("NaN",totalLength);
	double val = value;
	boolean neg = val<0;
	long pre;
	int frontLength = 1,digits = 0;
	int needLength = 0;
	long div = 10;
	long cur;
	boolean zeroFill = (options & ZERO_FILL) != 0;
	boolean doSci = false;
	double original;
	StringBuffer ret = new StringBuffer();

	if (neg) val *= -1;
	original = val;
	if (decimal < 0) {
		decimal = -decimal;
		options |= FREE_DECIMAL;
	}

	//checkSymbols();
	char decimalPoint = '.', negSign = '-', plusSign = '+';

	if (maxF == 0){
		maxF = java.lang.Math.pow(2,60);
		minF = java.lang.Math.pow(2,-50);
		ln10 = java.lang.Math.log(10.0);
	}

/*
	if (_isnan(val)){
		if (3 > totalLength && totalLength != 0) return cantFit
		strcpy(dest,NaN);
		return;
	}else if (bits(val) == POSITIVE_INFINITY_VALUE || bits(val) == NEGATIVE_INFINITY_VALUE){
		if (4 > totalLength && totalLength != 0) goto cant_fit;
		strcpy(dest,Infinity);
		dest[0] = neg ? negSign : plusSign;
		return;
	}
*/

	if (val != 0)
	// Should we do Sci. Notation?
	if (doSci || ((val >= maxF  || val <= minF || ((options & EXP_NOTATION) != 0)) && ((options & NO_EXP_NOTATION) == 0))) {
	// Have to do e version.
		double power = java.lang.Math.log(val)/ln10;
		double man;
		boolean negpow = power < 0;
		if (negpow) power *= -1;
		if (negpow){
			man = 1-(power-(long)power);
			power = (double)(1+(long)power);
			if (man >= 1) {
				man -= 1;
				power -= 1;
			}
		}else{
			man = power-(long)power;
			power = (double)(long)power;
		}
		man = java.lang.Math.pow(10,man);
		if (neg) man *= -1;
		if (totalLength == 0) {
			ret.append(toString(man,0,decimal,options|NO_EXP_NOTATION|AT_LEAST_ONE_DECIMAL));
		}else{
			int expPart = 2+lengthOfLong((long)power);
			if (expPart >= totalLength) return cantFit(null,totalLength);
			ret.append(toString(man,totalLength-expPart,decimal,options|NO_EXP_NOTATION|AT_LEAST_ONE_DECIMAL));
		}
		if (ret.toString().charAt(0) == '#') return cantFit(null,totalLength);
		ret.append('e');
		ret.append(negpow ? negSign : plusSign);
		ret.append(toString(power,0,0,NO_EXP_NOTATION));
		return ret.toString();
	}

	if (true/*(options & FREE_DECIMAL) != 0*/){
		int places = 0;
		double dc = val-(long)val;
		int i = 1;
		int d = 20;
		if (decimal != 0) d = decimal;
		for (i = 1; i<=d; i++){
			dc *= 10.0;
			if (dc >= 1.0) places = i;
			dc -= (long)dc;
		}
		if ((places == 0) && ((long)val == 0) && (original != 0.0) && ((options & NO_EXP_NOTATION) == 0)){
			return toString(original,totalLength,decimal,options | EXP_NOTATION);
		}
		if ((options & FREE_DECIMAL) != 0) decimal = places;
	}
// Round up.
	if ((decimal == 0) && ((options & AT_LEAST_ONE_DECIMAL) != 0)) decimal = 1;

	if ((options & TRUNCATE) == 0){
		double add = 0.5555555555;
		int i = 0;
		for (i = 0; i<decimal; i++) add /= 10.0;
		val += add;

	}

//Count found decimal places.

	if ((options & FREE_DECIMAL) != 0){
		int places = 0;
		double dc = val-(long)val;
		int i = 1;
		for (i = 1; i<=decimal; i++){
			dc *= 10.0;
			if (dc >= 1.0) places = i;
			dc -= (long)dc;
		}
		decimal = places;
	}
	if ((decimal == 0) && ((options & AT_LEAST_ONE_DECIMAL) != 0)) decimal = 1;
	pre = (long)val;
	if (pre != 0){
		cur = pre;
		while(true){
			cur -= cur%div;
			if (cur == 0) break;
			div *= 10;
			frontLength++;
		}
	}
	digits = frontLength;
	if (neg) frontLength++;
	needLength = frontLength;
	if (decimal > 0) needLength += decimal+1;
	if (needLength > totalLength && totalLength > 0) {
		int extra = needLength-totalLength;
		if (((options & FREE_DECIMAL) == 0) || (extra > decimal-1))
			return cantFit(null,totalLength);
		decimal -= extra;
	}
	if (totalLength > 0){
		int padLength = totalLength-(decimal > 0 ? decimal+1 : 0)-frontLength;
		char padChar = zeroFill ? '0':' ';
		int i = 0;
		if (zeroFill)
			if (neg) ret.append(negSign);
			//else *d++ = plusSign;
		for (i = 0; i<padLength; i++) ret.append(padChar);
		if (!zeroFill)
			if (neg) ret.append(negSign);
			//else ret.append(plusSign;
	}else{
		if (neg) ret.append(negSign);
		//else ret.append(plusSign;
	}
	for (cur = pre, div /= 10;div >=1 ; div /= 10){
		int dig = (int)(cur/div);
		cur -= dig*div;
		ret.append((char)('0'+dig));
	}
	if (decimal != 0) {
		int i;
		ret.append(decimalPoint);
		val -= (long)val;
		for (i = 0; i<decimal; i++){
			val *= 10;
			ret.append((char)('0'+(int)(((long)val)%10)));
			val -= (long)val;
		}
	}
	return ret.toString();
	/*
	java.text.NumberFormat nm = java.text.NumberFormat.getNumberInstance();
	if (decimal < 0) {
		decimal = -decimal;
		options |= FREE_DECIMAL;
	}
	if ((options & FREE_DECIMAL) != 0){
		if ((options & AT_LEAST_ONE_DECIMAL) != 0)
			nm.setMinimumFractionDigits(1);
		else
			nm.setMinimumFractionDigits(0);
		nm.setMaximumFractionDigits(10);
	}
	nm.setGroupingUsed(false);
	String st = nm.format(value);
	if (totalLength != 0){
		int toAdd = totalLength - st.length();
		while (toAdd-- > 0) st = " "+st;
	}
	return st;
*/
/*
	StringBuffer sb = new StringBuffer();
//==================================================================
	if (java.lang.Double.isNaN(value)) {
		if (NaN.length() > totalLength && totalLength != 0) return cantFit(totalLength);
		return NaN;
	}else if (java.lang.Double.isInfinite(value)){
		if (Infinity.length()+1 > totalLength && totalLength != 0) return cantFit(totalLength);
		if (value < 0) return "-"+Infinity;
		else return "+"+Infinity;
	}
	double val = value;
	boolean neg = val<0;
	long pre;
	int frontLength = 1,digits = 0;
	int needLength = 0;
	int d = 0;
	double div = 10;
	double cur;
	boolean zeroFill = (options & ZERO_FILL) != 0;
	if (neg) val *= -1;
	if (maxF == 0){
		maxF = java.lang.Math.pow(2,60);
		minF = java.lang.Math.pow(2,-50);
		ln10 = java.lang.Math.log(10.0);
	}
	// Should we do Sci. Notation?
	if (val != 0)
	if (((val >= maxF  || val <= minF || ((options & EXP_NOTATION) != 0)) && ((options & NO_EXP_NOTATION) == 0))) {
	// Have to do e version.
		double pow = java.lang.Math.log(val)/ln10;
		boolean negpow = pow < 0;
		if (negpow) pow *= -1;
		double man;
		if (negpow){
			man = 1-(pow-(long)pow);
			pow = 1+(long)pow;
			if (man >= 1) {
				man -= 1;
				pow -= 1;
			}
		}else{
			man = pow-(long)pow;
			pow = (long)pow;
		}
		man = java.lang.Math.pow(10,man);
		if (totalLength == 0) {
			if (neg) sb.append('-');
			sb.append(toString(man,0,decimal,options|NO_EXP_NOTATION));
			sb.append('e');
			if (negpow) sb.append('-');
			else sb.append('+');
			sb.append(toString(pow,0,0,NO_EXP_NOTATION));
		}else{
			int expPart = 2+Long.lengthOf((long)pow);
			String front = toString(man*(neg ? -1 : 1),totalLength-expPart,decimal,options|NO_EXP_NOTATION);
			//System.out.println("Front: "+front+"Pow: "+pow);
			if (front.indexOf('#') != -1) return cantFit(totalLength);
			sb.append(front);
			sb.append('e');
			if (negpow) sb.append('-');
			else sb.append('+');
			sb.append(toString(pow,0,0,NO_EXP_NOTATION));
		}
		return sb.toString();
	}
	if ((options & FREE_DECIMAL) != 0){
		int places = 0;
		double dc = val-(long)val;
		int i = 1;
		if (decimal == 0) decimal = 20;
		for (i = 1; i<=decimal; i++){
			dc *= 10.0;
			if ((long)dc >= 1) places = i;
			dc -= (long)dc;
		}
		decimal = places;
	}
// Round up.
	if ((options & TRUNCATE) == 0){
		double add = 0.5;
		int i = 0;
		for (i = 0; i<decimal; i++) add /= 10.0;
		val += add;
	}
	if ((options & FREE_DECIMAL) != 0){
		int places = 0;
		double dc = val-(long)val;
		int i = 1;
		for (i = 1; i<=decimal; i++){
			dc *= 10.0;
			if ((long)dc >= 1) places = i;
			dc -= (long)dc;
		}
		decimal = places;
	}
	pre = (long)val;
	if (pre != 0){
		cur = pre;
		while(true){
			cur -= cur%div;
			if (cur == 0) break;
			div *= 10;
			frontLength++;
		}
	}
	digits = frontLength;
	if (neg) frontLength++;
	needLength = frontLength;
	if (decimal > 0) needLength += decimal+1;
	if (needLength > totalLength && totalLength > 0) {
		int extra = needLength-totalLength;
		if (((options & FREE_DECIMAL) == 0) || (extra > decimal-1))
			return cantFit(totalLength);
		decimal -= extra;
	}
	if (totalLength > 0){
		int padLength = totalLength-(decimal > 0 ? decimal+1 : 0)-frontLength;
		char padChar = zeroFill ? '0':' ';
		int i = 0;
		if (zeroFill && neg) sb.append('-');
		for (i = 0; i<padLength; i++) sb.append(padChar);
		if (!zeroFill && neg) sb.append('-');
	}else{
		if (neg) sb.append('-');
	}
	for (cur = pre, div /= 10;div >= 1; div /= 10){
		int dig = (int)(cur/div);
		cur -= dig*div;
		sb.append((char)('0'+dig));
	}
	if (decimal != 0) {
		int i;
		sb.append('.');
		for (i = 0; i<decimal; i++){
			val *= 10;
			sb.append((char)('0'+((long)val)%10));
		}
	}
	return sb.toString();
	*/
}



//===================================================================
public String toString() {return toString(0,decimalPlaces,0);}
//===================================================================
public void fromString(String v)
//===================================================================
{
	value = 0;
	try{
		value = java.text.NumberFormat.getNumberInstance().parse(v).doubleValue();
			//java.lang.Double.valueOf(v).doubleValue();
	}catch(Exception e){}
}
//===================================================================
//public float toFloat() {return (float)value;}
//public int toInt() {return (int)value;}
//public Long toLong(Long dest) {if (dest == null) dest = new Long(); dest.value = (long)value; return dest;}
//===================================================================
/*
//-------------------------------------------------------------------
static Double setAndReturn(Double toReturn,double v)
//-------------------------------------------------------------------
{
	if (toReturn == null) toReturn = new Double();
	toReturn.value = v;
	return toReturn;
}
*/
/*
//===================================================================
public Double multiply(Double other,Double ret)
//===================================================================
{return setAndReturn(ret,value*other.value);}
*/
/*
//===================================================================
public Double divide(Double other,Double ret)
//===================================================================
{
	if (other.value == 0.0){
		if (value == 0.0) return setAndReturn(ret,java.lang.Double.NaN);
		else if (value > 0.0) return setAndReturn(ret,java.lang.Double.POSITIVE_INFINITY);
		else return setAndReturn(ret,java.lang.Double.NEGATIVE_INFINITY);
	}else
		return setAndReturn(ret,value/other.value);
}
*/
//===================================================================
//public Double add(Double other,Double ret){return setAndReturn(ret,value+other.value);}
//===================================================================
//public Double subtract(Double other,Double ret){return setAndReturn(ret,value-other.value);}
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (!(other instanceof Double)) return 1;
	double v2 = ((Double)other).value;
	if (value == v2) return 0;
	else if (value > v2) return 1;
	return -1;
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
//==================================================================
/**
* This checks to see if the value of the double is one of the special values
* (POSITIVE_INFINITY, NEGATIVE_INFINITY, INFINITY(either), NAN) or if it
* is NEGATIVE, POSITIVE or ZERO.
**/
//===================================================================
public boolean is(int special)
//===================================================================
{
	switch(special){
	case NEGATIVE: return value < 0;
	case POSITIVE: return value > 0;
	case ZERO: return value == 0;
	case POSITIVE_INFINITY: return value == java.lang.Double.POSITIVE_INFINITY;
	case NEGATIVE_INFINITY: return value == java.lang.Double.NEGATIVE_INFINITY;
	case INFINITY: return value == java.lang.Double.POSITIVE_INFINITY || value == java.lang.Double.NEGATIVE_INFINITY;
	case NAN: return java.lang.Double.isNaN(value);
	default: return false;
	}
}
/*
//-------------------------------------------------------------------
Double set(double val)
//-------------------------------------------------------------------
{
	value = val;
	return this;
}
*/

//-------------------------------------------------------------------
Double set(boolean b)
//-------------------------------------------------------------------
{
	return set(b ? (int)1 : (int)0);
}
//-------------------------------------------------------------------
boolean getBoolean()
//-------------------------------------------------------------------
{
	return (value != 0);
}
/**
* This sets the double to one of the following values: POSITIVE_INFINITY(same as INFINITY), NEGATIVE_INFINITY
* NAN, NEGATIVE (-1), POSITIVE (+1), ZERO.
**/
//===================================================================
public Double setSpecial(int special)
//===================================================================
{
	switch(special){
	case NEGATIVE: return set(-1.0);
	case POSITIVE: return set(1.0);
	case ZERO: return set(0.0);
	case INFINITY:
	case POSITIVE_INFINITY: return set(java.lang.Double.POSITIVE_INFINITY);
	case NEGATIVE_INFINITY: return set(java.lang.Double.NEGATIVE_INFINITY);
	case NAN: return set(java.lang.Double.NaN);
	default: return this;
	}
}
/**
* This creates an array of natively stored double values. It is FAR more memory effecient
* than creating an array of Double objects. It is actually implemented currently as a byte
* array with eight bytes per double value.
**/
/*
//===================================================================
public static Object createArray(int length)
//===================================================================
{
	return new double[length];
}
*/
/**
* This gets an individual double value at the specified index in the doubleArray (which
* must have been created using createArray() and stores it in dest. If dest is null a
* new Double will be created. The return value is the Double which contains the value.
* If the index is out of bounds null is returned. Modifying the value stored in the destination for
* this method will NOT afect the data in the array. You MUST use setArrayValue() for this.
**/
/*
//===================================================================
public static Double getArrayValue(Object doubleArray,int index,Double dest)
//===================================================================
{
	return setAndReturn(dest,((double [])doubleArray)[index]);
}
*/
/**
* This sets an individual double value at the specified index in the doubleArray (which
* must have been created using createArray(). The return value is the source value or
* null if the index is out of bounds.
**/
/*
//===================================================================
public static Double setArrayValue(Object doubleArray,int index,Double source)
//===================================================================
{
	((double [])doubleArray)[index] = source.value;
	return source;
}
*/
/**
* This returns the length of the Double array. If the array is null then a null pointer
* error will be thrown.
**/
/*
//===================================================================
public static int arrayLength(Object doubleArray)
//===================================================================
{
	return ((double [])doubleArray).length;
}
*/
/**
* This copies from the source array into the dest array for length number of
* entries. If the destination array is null it will be set to an empty array.
* If the destination array is not big enough (or null) to hold all the data starting at
* the offset specified, a new array will be created which is big enough and
* this new array will be returned.
*
* This will NOT safely move the contents of an array within itself.
*
* This function can be used to:
* 1. Get a subarray or copy of an array. if (destArray == null).
* 2. Copy one array into another if (destArray != null)
* 3. Append one array to another if (destStart == destArray.length)
**/
/*
//===================================================================
public static Object arrayJoin(Object sourceArray,int sourceStart,Object destArray,int destStart,int length)
//===================================================================
{
	double [] da = destArray == null ? new double[0] : (double [])destArray;
	if (sourceArray == null) return da;
	double [] sa = (double [])sourceArray;
	int extraNeeded = destStart+length-da.length;
	if (extraNeeded > 0) {
		double [] nda = new double[da.length+extraNeeded];
		Vm.copyArray(da,0,nda,0,da.length);
		da = nda;
	}
	Vm.copyArray(sa,sourceStart,da,destStart,length);
	return da;
}
*/
/**
* This gets the double value from the bit-wise representation of it in the Long
* value. It returns itself;
**/
/*
//===================================================================
public Double fromBits(Long longBits)
//===================================================================
{
	value = java.lang.Double.longBitsToDouble(longBits.value);
	return this;
}
*/
/**
* This stores the bit-wise representation of the double in the Long value. If dest
* is null a new Long will be created. The return value is the Long with the bit-wise
* representation stored within it.
**/
/*
//===================================================================
public Long toBits(Long dest)
//===================================================================
{
	if (dest == null) dest = new Long();
	dest.value = java.lang.Double.doubleToLongBits(value);
	return dest;
}
*/
//===================================================================
public void copyFrom(Object other)
//===================================================================
{
	if (other instanceof Double)
		value = ((Double)other).value;
		//set((Double)other);
}
/**
* Return an int array representing the data in the array.
**/
/*
//===================================================================
public static int [] toIntArray(Object doubleArray,int start,int length)
//===================================================================
{
	if (doubleArray == null) return null;
	double [] da = (double [])doubleArray;
	int [] ret = new int[da.length];
	for (int i = 0; i<ret.length; i++) ret[i] = (int)da[i];
	return ret;
}
*/
/**
* Return a float array representing the data in the array.
**/
/*
//===================================================================
public static float [] toFloatArray(Object doubleArray,int start,int length)
//===================================================================
{
	if (doubleArray == null) return null;
	double [] da = (double [])doubleArray;
	float [] ret = new float[da.length];
	for (int i = 0; i<ret.length; i++) ret[i] = (float)da[i];
	return ret;
}
*/
/**
* Return a Long array representing the data in the array.
**/
/*
//===================================================================
public static Object toLongArray(Object doubleArray,int start,int length)
//===================================================================
{
	if (doubleArray == null) return null;
	double [] da = (double [])doubleArray;
	long [] ret = new long[da.length];
	for (int i = 0; i<ret.length; i++) ret[i] = (long)da[i];
	return ret;
}
*/
/**
* Write out double values as bits.
**/
/*
//==============================================================================================
public static void save(Object doubleArray,int start,byte [] dest,int destStart,int length)
//==============================================================================================
{
	if (doubleArray == null) return;
	double [] da = (double [])doubleArray;
	ewe.sys.Long l = new ewe.sys.Long();
	int d = destStart;
	for (int i = 0; i<length; i++) {
		l.value = java.lang.Double.doubleToLongBits(da[i]);
		l.save(dest,d);
		d += 8;
	}
}
*/
/**
* Load the values in the array as bit values from the source byte array.
**/
/*
//==============================================================================================
public static void load(Object doubleArray,int start,byte [] source,int sourceStart,int length)
//==============================================================================================
{
	//if (doubleArray == null) return;
	double [] da = (double [])doubleArray;
	ewe.sys.Long l = new ewe.sys.Long();
	int d = sourceStart;
	for (int i = 0; i<length; i++) {
		l.load(source,d);
		da[i] = java.lang.Double.longBitsToDouble(l.value);
		d += 8;
	}
}
*/
//===================================================================
public static void matrixFunction(
double [] doubleMatrix,int startIndex,int rows,int columns,
MathFunction [] functions,double [] destDoubleArray,int destStartIndex)
//===================================================================
{
	double [] ret = (double [])destDoubleArray;
	double [] buffer = new double[functions.length];
	double [] data = (double [])doubleMatrix;

	MathFunctionData fd = new MathFunctionData();
	fd.doubleArrayOfParameters = doubleMatrix;
	fd.parameterCount = columns;
	fd.parameterStartIndex = startIndex;
	for (int r = 0; r<rows; r++){
		fd.rowIndex = r;
		for (int f = 0; f < functions.length; f++){
			fd.result = 0;
			fd.colIndex = f;
			functions[f].doFunction(fd);
			buffer[f] = fd.result;
		}
		for (int f = 0; f < functions.length; f++)
			ret[destStartIndex++] = buffer[f];
		fd.parameterStartIndex += columns;
	}
}
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

//##################################################################
}
//##################################################################
