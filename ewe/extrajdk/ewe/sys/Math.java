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
import java.util.Random;
//##################################################################
public class Math{
//##################################################################

static Random random = new Random();
//===================================================================
public static void srand(int seed)
//===================================================================
{
	random = new Random(seed);
}
//===================================================================
public static int rand()
//===================================================================
{
	return random.nextInt() & 0x7fff;
}

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
* Log of operand1 using arbitrary (non-zero) base in operand2.
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
* Raise first operand to power of second.
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
static double nearZero(double val)
//===================================================================
{
	double value = val;
	if (value < 0) value *= -1;
	if (value < 1e-15) return 0;
	return val;
}
//===================================================================
static double nearInf(double val)
//===================================================================
{
	double value = val;
	if (value < 0) value *= -1;
	if (value > 1e+15) return val < 0 ? java.lang.Double.NEGATIVE_INFINITY : java.lang.Double.POSITIVE_INFINITY;
	return val;
}

private static Double one = new Double(), two = new Double();


//===================================================================
public static double calculate(int function,double one) {return calculate(function,one,1);}
//===================================================================
public static double calculate(int function,double one,double two)
//===================================================================
{
	return calculate(function,Math.one.set(one),Math.two.set(two)).value;
}
//===================================================================
private static ewe.sys.Double calculate(int function,ewe.sys.Double operand,ewe.sys.Double resultAndOperand2)
//===================================================================
{
	if (resultAndOperand2 == null) resultAndOperand2 = new ewe.sys.Double();
	switch(function){
		case ROUND: return resultAndOperand2.set(java.lang.Math.round(operand.value));
		case RINT: return resultAndOperand2.set(java.lang.Math.rint(operand.value));
		case CEIL: return resultAndOperand2.set(java.lang.Math.ceil(operand.value));
		case FLOOR: return resultAndOperand2.set(java.lang.Math.floor(operand.value));
		case SIN: return resultAndOperand2.set(nearZero(java.lang.Math.sin(operand.value)));
		case COS: return resultAndOperand2.set(nearZero(java.lang.Math.cos(operand.value)));
		case TAN: return resultAndOperand2.set(nearInf(nearZero(java.lang.Math.tan(operand.value))));
		case ASIN: return resultAndOperand2.set(java.lang.Math.asin(operand.value));
		case ACOS: return resultAndOperand2.set(java.lang.Math.acos(operand.value));
		case ATAN: return resultAndOperand2.set(java.lang.Math.atan(operand.value));
		case ATAN2: return resultAndOperand2.set(java.lang.Math.atan2(operand.value,resultAndOperand2.value));
		case TORADIANS:
			double deg = operand.value%360;
			if (deg > 180) deg = -(360-deg);
			else if (deg < -180) deg = 360+deg;
			return resultAndOperand2.set((java.lang.Math.PI * deg)/180.0);
		case TODEGREES:
			double pi = java.lang.Math.PI;
			double rad = operand.value%(pi*2);
			if (rad > pi) rad = -(pi*2-rad);
			else if (rad < -pi) rad = pi*2+rad;
			return resultAndOperand2.set(rad*180.0/pi);
		case LOG:
			return resultAndOperand2.set(java.lang.Math.log(operand.value));
		case LOG2:
			return resultAndOperand2.set(java.lang.Math.log(operand.value)/java.lang.Math.log(resultAndOperand2.value));
		case EXP:
			return resultAndOperand2.set(java.lang.Math.exp(operand.value));
		case SQRT:
			return resultAndOperand2.set(java.lang.Math.sqrt(operand.value));
		case POWER:
			return resultAndOperand2.set(java.lang.Math.pow(operand.value,resultAndOperand2.value));
		case MULTIPLY:
			return resultAndOperand2.set(operand.value*resultAndOperand2.value);
		case DIVIDE:
			return resultAndOperand2.set(operand.value/resultAndOperand2.value);
		case ADD:
			return resultAndOperand2.set(operand.value+resultAndOperand2.value);
		case SUBTRACT:
			return resultAndOperand2.set(operand.value-resultAndOperand2.value);
		case NEGATE:
			return resultAndOperand2.set(-operand.value);
		case ABSOLUTE:
			return resultAndOperand2.set(java.lang.Math.abs(operand.value));
		case REMAINDER:
			return resultAndOperand2.set(java.lang.Math.IEEEremainder(operand.value,resultAndOperand2.value));
		case GREATER:
			return resultAndOperand2.set(operand.value > resultAndOperand2.value);
		case GREATEROREQUAL:
			return resultAndOperand2.set(operand.value >= resultAndOperand2.value);
		case LESSER:
			return resultAndOperand2.set(operand.value < resultAndOperand2.value);
		case LESSEROREQUAL:
			return resultAndOperand2.set(operand.value <= resultAndOperand2.value);
		case EQUALS:
			return resultAndOperand2.set(operand.value == resultAndOperand2.value);
		case BOOLEAN_OR:
			return resultAndOperand2.set(operand.getBoolean() || resultAndOperand2.getBoolean());
		case BOOLEAN_AND:
			return resultAndOperand2.set(operand.getBoolean() && resultAndOperand2.getBoolean());
		case BOOLEAN_NOT:
			return resultAndOperand2.set(!operand.getBoolean());
		case BOOLEAN_EXOR:
			return resultAndOperand2.set(operand.getBoolean() != resultAndOperand2.getBoolean());
		case GREATER_OF:
			return resultAndOperand2.set(operand.value > resultAndOperand2.value ? operand.value : resultAndOperand2.value);
		case LESSER_OF:
			return resultAndOperand2.set(operand.value < resultAndOperand2.value ? operand.value : resultAndOperand2.value);
		case SET:
			return resultAndOperand2;
		default: return resultAndOperand2;

	}
}
/**
* PI constant.
**/
public static final int PI = 1;
/**
* E constant
**/
public static final int E = 2;
//===================================================================
public static double constant(int cons)
//===================================================================
{
	switch(cons){
		case PI: return java.lang.Math.PI;
		case E: return java.lang.Math.E;
		default: return 0;
	}
}
//##################################################################
}
//##################################################################
