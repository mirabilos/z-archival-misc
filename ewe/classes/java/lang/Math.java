/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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
package java.lang;

//##################################################################
public final class Math{
//##################################################################

public static  final double E = ewe.sys.Math.constant(ewe.sys.Math.E);
public static  final double PI = ewe.sys.Math.constant(ewe.sys.Math.PI);
private static ewe.util.Random myRand = new ewe.util.Random();

/**
* Returns the absolute value of a double value.
**/
//===================================================================
public static double abs(double val)  {return val < 0 ? -val : val;}
//===================================================================
/**
* Returns the absolute value of a float value.
**/
//===================================================================
public static float abs(float val)  {return val < 0 ? -val : val;}
//===================================================================
/**
* Returns the absolute value of a int value.
**/
//===================================================================
public static int abs(int val)  {return val < 0 ? -val : val;}
//===================================================================
/**
* Returns the absolute value of a long value.
**/
//===================================================================
public static long abs(long val)  {return val < 0 ? -val : val;}
//===================================================================
/**
* Returns the arc cosine of an angle, in the range of 0.0 through pi.
**/
public static native double acos(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.ACOS,val,0);}
/**
* Returns the arc sine of an angle, in the range of -pi/2 through pi/2.
**/
public static native double asin(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.ASIN,val,0);}
/**
* Returns the arc tangent of an angle, in the range of -pi/2 through pi/2.
**/
public static native double atan(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.ATAN,val,0);}
/**
* the same as atan2()
**/
public static native double atan(double a,double b);
/**
* Converts rectangular coordinates (b, a) to polar (r, theta).
**/
public static double atan2(double a, double b)
{
	return atan(a,b);
}
/**
* Returns the smallest (closest to negative infinity) double value that is not less than the argument and is equal to a mathematical integer.
**/
public static native double ceil(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.CEIL,val,0);}
/**
* Returns the trigonometric cosine of an angle.
**/
public static native double cos(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.COS,val,0);}
/**
* Returns the exponential number e (i.e., 2.718...) raised to the power of a double value.
**/
public static native double exp(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.EXP,val,0);}
/**
* Returns the largest (closest to positive infinity) double value that is not greater than the argument and is equal to a mathematical integer.
**/
public static native double floor(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.FLOOR,val,0);}
/**
* Computes the remainder operation on two arguments as prescribed by the IEEE 754 standard.
**/
public static native double IEEEremainder(double f1,double f2);
//{return ewe.sys.Math.calculate(ewe.sys.Math.REMAINDER,f1,f2);}
/**
* Returns the natural logarithm (base e) of a double value.
**/
public static native double log(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.LOG,val,0);}
/**
* Returns the greater of two values.
**/
public static double max(double a,double b) {return (a > b) ? a : b;}
/**
* Returns the smaller of two values.
**/
public static double min(double a,double b) {return (a < b) ? a : b;}
/**
* Returns the greater of two values.
**/
public static float max(float a,float b) {return (a > b) ? a : b;}
/**
* Returns the smaller of two values.
**/
public static float min(float a,float b) {return (a < b) ? a : b;}
/**
* Returns the greater of two values.
**/
public static int max(int a,int b) {return (a > b) ? a : b;}
/**
* Returns the smaller of two values.
**/
public static int min(int a,int b) {return (a < b) ? a : b;}
/**
* Returns the greater of two values.
**/
public static long max(long a,long b) {return (a > b) ? a : b;}
/**
* Returns the smaller of two values.
**/
public static long min(long a,long b) {return (a < b) ? a : b;}
/**
* Returns of value of the first argument raised to the power of the second argument.
**/
public static native double pow(double a,double b);
//{return ewe.sys.Math.calculate(ewe.sys.Math.POWER,a,b);}
/**
* Returns a random number between 0.0 and 1.0.
**/
public static double random()
{
	return myRand.nextFloat();
}
/**
* returns the closest integer to the argument.
**/
public static double rint(double value)
{
	return ewe.sys.Math.calculate(ewe.sys.Math.RINT,value,0);
}
/**
* Returns the closest long to the argument.
**/
public static long round(double value)
{
	return (long)ewe.sys.Math.calculate(ewe.sys.Math.ROUND,value,0);
}
/**
* Returns the closest int to the argument.
**/
public static int round(float value)
{
	return (int)ewe.sys.Math.calculate(ewe.sys.Math.ROUND,value,0);
}
/**
* Returns the trigonometric sine of an angle.
**/
public static native double sin(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.SIN,val,0);}
/**
* Returns the square root of a double value.
**/
public static native double sqrt(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.SQRT,val,0);}
/**
* Returns the trigonometric tangent of an angle.
**/
public static native double tan(double val);
//{return ewe.sys.Math.calculate(ewe.sys.Math.TAN,val,0);}
//##################################################################
}
//##################################################################

