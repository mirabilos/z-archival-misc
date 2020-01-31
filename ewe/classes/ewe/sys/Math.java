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

/**
@deprecated - use java.lang.Math instead.
**/
//##################################################################
public class Math{
//##################################################################

public static native void srand(int seed);
public static native int rand();

/**
* Sine trig function.
**/
public static final int SIN = 1;
/**
* Cosine trig function.
**/
public static final int COS = 2;
/**
* Tangent trig function.
**/
public static final int TAN = 3;
/**
* Arc Sine trig function.
**/
public static final int ASIN = 4;
/**
* Arc Cosine trig function.
**/
public static final int ACOS = 5;
/**
* Arc Tangent trig function.
**/
public static final int ATAN = 6;
/**
* Arc Tangent2 trig function.
**/
public static final int ATAN2 = 7;
/**
* Convert degrees to randians trig function.
**/
public static final int TORADIANS = 8;
/**
* Convert radians to degrees.
**/
public static final int TODEGREES = 9;
/**
* Log of operand1 to base operand2.
**/
public static final int LOG2 = 10;
/**
* Log (base e)
**/
public static final int LOG = 11;
/**
* Inverse Log (base e)
**/
public static final int EXP = 12;
/**
* Power raise first operand to power of second.
**/
public static final int POWER = 13;
/**
* Square Root.
**/
public static final int SQRT = 14;
/**
* Ceil() function.
**/
public static final int CEIL = 15;
/**
* Floor() function.
**/
public static final int FLOOR = 16;

public static final int MULTIPLY = 17;
public static final int DIVIDE = 18;
public static final int ADD = 19;
public static final int SUBTRACT = 20;
public static final int NEGATE = 21;
public static final int ABSOLUTE = 22;
public static final int REMAINDER = 23;
public static final int GREATER = 24;
public static final int GREATEROREQUAL = 25;
public static final int LESSER = 26;
public static final int LESSEROREQUAL = 27;
public static final int EQUALS = 28;
/**
* A boolean value is 0 = false, not 0 = true;
**/
public static final int BOOLEAN_OR = 29;
/**
* A boolean value is 0 = false, not 0 = true;
**/
public static final int BOOLEAN_AND = 30;
/**
* A boolean value is 0 = false, not 0 = true;
**/
public static final int BOOLEAN_NOT = 31;
/**
* A boolean value is 0 = false, not 0 = true;
**/
public static final int BOOLEAN_EXOR = 32;
public static final int GREATER_OF = 33;
public static final int LESSER_OF = 34;
/**
* The result is the value of the second parameter.
**/
public static final int SET = 35;
public static final int RINT = 36;
public static final int ROUND = 37;
//===================================================================
//public static ewe.sys.Double calculate(int function,ewe.sys.Double operand,ewe.sys.Double resultAndOperand2)
//===================================================================
//{
	//if (resultAndOperand2 == null) resultAndOperand2 = new ewe.sys.Double();
	//resultAndOperand2
//}
//===================================================================
public static native double calculate(int function,double op1,double op2);
public static double calculate(int function,double op1) {return calculate(function,op1,1);}
//===================================================================
/*
{
	if (resultAndOperand2 == null) resultAndOperand2 = new ewe.sys.Double();
	switch(function){
		case SIN: return resultAndOperand2.set(java.lang.Math.sin(operand.value));
		case COS: return resultAndOperand2.set(java.lang.Math.cos(operand.value));
		case TAN: return resultAndOperand2.set(java.lang.Math.tan(operand.value));
		case ASIN: return resultAndOperand2.set(java.lang.Math.asin(operand.value));
		case ACOS: return resultAndOperand2.set(java.lang.Math.acos(operand.value));
		case ATAN: return resultAndOperand2.set(java.lang.Math.atan(operand.value));
		case ATAN2: return resultAndOperand2.set(java.lang.Math.atan2(operand.value,resultAndOperand2.value));
		case TORADIANS:
			double deg = operand.value%360;
			if (deg > 180) deg = 360-deg;
			else if (deg < 180) deg = 360+deg;
			return resultAndOperand2.set((java.lang.Math.PI * deg)/180.0);
		case TODEGREES:
			double pi = java.lang.Math.PI;
			double rad = operand.value%(pi*2);
			if (rad > pi) rad = pi*2-rad;
			else if (rad < pi) rad = pi*2+rad;
			return resultAndOperand2.set(rad*180.0/pi);
		default: return resultAndOperand2;
	}
}
*/
/**
* PI constant.
**/
public static final int PI = 1;
/**
* E constant
**/
public static final int E = 2;
//===================================================================
//public static native ewe.sys.Double constant(int cons,ewe.sys.Double result);
public static native double constant(int cons);
//===================================================================
/*
{
	if (result == null) result = new ewe.sys.Double();
	switch(cons){
		case PI: return result.set(java.lang.Math.PI);
		case E: return result.set(java.lang.Math.E);
		default: return result.set(0);
	}
}
*/
//##################################################################
}
//##################################################################

