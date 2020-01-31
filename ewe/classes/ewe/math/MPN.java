/* gnu.java.math.MPN
   Copyright (C) 1999, 2000, 2001 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

// Included from Kawa 1.6.62 with permission of the author,
// Per Bothner <per@bothner.com>.

package ewe.math;
import ewe.util.Utils;
import ewe.util.ByteEncodable;
import ewe.util.ByteEncoder;
import ewe.util.ByteArray;
import ewe.io.StreamCorruptedException;

/** This contains various low-level routines for unsigned bigints.
 * The interfaces match the mpn interfaces in gmp,
 * so it should be easy to replace them with fast native functions
 * that are trivial wrappers around the mpn_ functions in gmp
 * (at least on platforms that use 32-bit "limbs").
 */

public class MPN implements ByteEncodable
{
//
// Do not move the next two.
//
public int [] words;
public int length;

//===================================================================
public int encodeBytes(ByteArray dest)
//===================================================================
{
	minimize();
	int [] w = new int[length];
	System.arraycopy(words,0,w,0,length);
	return ByteEncoder.encodeObject(dest,w);
}
//===================================================================
public MPN(byte[] encodedBytes,int offset,int length)
throws StreamCorruptedException
//===================================================================
{
	try{
		words = (int [])ByteEncoder.decodeObject(encodedBytes,offset,length,null);
		length = words.length;
		ensureSpace(2);
	}catch(ClassNotFoundException e){
		throw new StreamCorruptedException();
	}
}
//===================================================================
public MPN()
//===================================================================
{
	words = new int[2];
	length = 1;
}
//===================================================================
public MPN(int numWords)
//===================================================================
{
	words = new int[numWords < 2 ? 2 : numWords];
	length = 1;
}
//===================================================================
public MPN fromBigInteger(BigInteger bi)
//===================================================================
{
	clear();
	if (bi.words == null) set(bi.ival);
	else {
		clear(bi.ival);
		System.arraycopy(bi.words,0,words,0,bi.ival);
		length = bi.ival;
	}
	return this;
}
//===================================================================
public BigInteger toBigInteger()
//===================================================================
{
	BigInteger bi = new BigInteger();
	toBI(bi);
	return bi.canonicalize();
}
//===================================================================
public MPN minimize()
//===================================================================
{
	while(length > 1){
		if (words[length-1] == 0 && words[length-2] >= 0) length--;
		else if (words[length-1] == -1 && words[length-2] < 0) length--;
		else return this;
	}
	return this;
}
//===================================================================
public String toString(int radix)
//===================================================================
{
	return toBigInteger().toString(radix);
}
//===================================================================
public String toString()
//===================================================================
{
	return toBigInteger().toString();
}
/**
* This returns -1 if the value is negative, +1 if it is positive, 0 if it is zero.
**/
//===================================================================
public int signum()
//===================================================================
{
	int top = words[length-1];
	if (length == 1 && top == 0) return 0;
	return top < 0 ? -1 : 1;
}
//===================================================================
public boolean isOdd()
//===================================================================
{
	return (words[0] & 1) == 1;
}
//===================================================================
public MPN set(int[] data,int offset,int length)
//===================================================================
{
	//clear(length);
	if (words.length < length) words = new int[length];
	System.arraycopy(data,offset,words,0,length);
	this.length = length;
	return this;
}
//===================================================================
public static MPN set(MPN source,MPN dest)
//===================================================================
{
	if (dest == null) dest = new MPN();
	return dest.set(source);
}
//===================================================================
public MPN set(BigInteger other)
//===================================================================
{
	fromBigInteger(other);
	return this;
}
//===================================================================
public MPN set(MPN other)
//===================================================================
{
	other.minimize();
	if (other == this) return this;
	clear(other.length);
	length = other.length;
	System.arraycopy(other.words,0,words,0,length);
	return this;
}
//===================================================================
public MPN set(int value)
//===================================================================
{
	length = 1;
	words[0] = value;
	return this;
}
//===================================================================
public MPN set(long value)
//===================================================================
{
	length = 2;
	words[0] = (int)value;
	words[1] = (int)(value >> 32);
	if (words[1] == -1 || words[1] == 0) length = 1;
	return this;
}

//-------------------------------------------------------------------
void clear()
//-------------------------------------------------------------------
{
	clear(1);
}
//-------------------------------------------------------------------
void clear(int forLength)
//-------------------------------------------------------------------
{
	if (forLength < 2) forLength = 2;
	if (words.length < forLength)
		words = new int[forLength];
	else
		ewe.util.Utils.zeroArrayRegion(words,0,words.length);
	length = 1;
}
//-------------------------------------------------------------------
void ensureSpace(int numWords)
//-------------------------------------------------------------------
{
	if (numWords < 2) numWords = 2;
	if (words.length < numWords){
		int[] n = new int[numWords];
		System.arraycopy(words,0,n,0,words.length);
		words = n;
	}
}
//-------------------------------------------------------------------
BigInteger toBI(BigInteger bi)
//-------------------------------------------------------------------
{
	if (length == 1) {
		bi.words = null;
		bi.ival = words[0];

	}else{
		if (bi.words == null || bi.words.length < length)
			bi.words = new int[length];
		else
			ewe.util.Utils.zeroArrayRegion(bi.words,0,bi.words.length);
		System.arraycopy(words,0,bi.words,0,length);
		bi.ival = length;
	}
	bi.canonicalizeMe();
	return bi;
}
//-------------------------------------------------------------------
private final boolean isNegative()
//-------------------------------------------------------------------
{
  return words[length-1] < 0;
}
/**
* Destructively shift this MPN to the right by count bits.
* @param count the number of bits to shift.
* @return itself after being shifted.
*/
//===================================================================
public MPN shiftRight(int count)
//===================================================================
{
	boolean neg = isNegative();
	int word_count = count >> 5;
	count &= 31;
	int d_len = length - word_count;
	if (d_len <= 0) set(neg ? -1 : 0);
	else{
    MPN.rshift0(words, words, word_count, d_len, count);
    length = d_len;
    if (neg) words[d_len-1] |= -2 << (31 - count);
  }
	return this;
}
/**
* Destructively shift this MPN to the left by count bits.
* @param count the number of bits to shift.
* @return itself after being shifted.
*/
//===================================================================
public MPN shiftLeft(int count)
//===================================================================
{
	int wordCount = count >> 5;
  count &= 31;
  int newLen = length + wordCount;
  if (count == 0){ //Shift is multiple of 32.
		if (wordCount == 0) return this;
		ensureSpace(newLen);
		System.arraycopy(words,0,words,wordCount,length);
		Utils.zeroArrayRegion(words,0,wordCount);
		length = newLen;
		return this;
	}
	newLen++;
	ensureSpace(newLen);
	int shift_out = MPN.lshift(words, wordCount, words, length, count);
	count = 32 - count;
	words[newLen-1] = (shift_out << count) >> count;  // sign-extend.
	length = newLen;
	if (wordCount != 0) Utils.zeroArrayRegion(words,0,wordCount);
	return this;
}
static MPN times1, times2;
/**
 * Destructively multiply this number with y.
 * @param y the number to multiply by.
 * @return this number, holding the result of the multiplication.
 */
//===================================================================
public MPN multiply(MPN y)
//===================================================================
{
	times1 = set(this,times1);
	times2 = set(y,times2);
	boolean negative = false;
	if (times1.isNegative()) {
		times1.toNegative();
		negative = !negative;
	}
	if (times2.isNegative()){
		times2.toNegative();
		negative = !negative;
	}
	if (times1.length < times2.length){
		MPN t = times1;
		times1 = times2;
		times2 = t;
	}
	clear(times1.length+times2.length+1);
	MPN.mul(words, times1.words, times1.length, times2.words, times2.length);
	length = times1.length+times2.length;
	if (negative) toNegative();
	minimize();
	return this;
}
  /** Copy the abolute value of this into an array of words.
   * Assumes words.length >= (this.words == null ? 1 : this.ival).
   * Result is zero-extended, but need not be a valid 2's complement number.
   */

	//-------------------------------------------------------------------
  private void getAbsolute(int[] words)
	//-------------------------------------------------------------------
  {
    int len = length;
		for (int i = len;  --i >= 0;)
	  	words[i] = this.words[i];
    if (words[len - 1] < 0)
      BigInteger.negate(words, words, len);
    for (int i = words.length;  --i > len; )
      words[i] = 0;
  }

	//-------------------------------------------------------------------
  private static void divide(long x, long y, MPN quotient, MPN remainder)
	//-------------------------------------------------------------------
  {
    boolean xNegative = false, yNegative = false;
    if (x < 0){
			xNegative = true;
			x = -x;
    }
    if (y < 0){
			yNegative = true;
			y = -y;
    }
    long q = x / y;
    long r = x % y;
    boolean qNegative = xNegative ^ yNegative;
    if (quotient != null){
			if (qNegative) q = -q;
			quotient.set(q);
		}
		if (remainder != null){
			if (xNegative) r = -r;
			remainder.set(r);
		}
  }


	int[] xwords, ywords;
  /** Divide two integers, yielding quotient and remainder.
   * @param y the denominator in the division
   * @param quotient is set to the quotient of the result (iff quotient!=null)
   * @param remainder is set to the remainder of the result
   *  (iff remainder!=null)
   */
	//===================================================================
  public void divide(MPN denominator,MPN quotient,MPN remainder)
	//===================================================================
  {
		MPN y = denominator;
		MPN x = this;
		if (y.equals(0)) throw new ArithmeticException("Divide by zero");
    if (x.hasLong() && y.hasLong()){
			long x_l = x.getLong();
			long y_l = y.getLong();
			if (x_l != Number.LONG_MIN_VALUE && y_l != Number.LONG_MIN_VALUE){
	   		divide(x_l, y_l, quotient, remainder);
	    	return;
	  	}
    }
    boolean xNegative = isNegative();
    boolean yNegative = y.isNegative();
    boolean qNegative = xNegative ^ yNegative;

		int ylen = y.length;
		if (ywords == null || ywords.length < ylen)
    	ywords = new int[ylen];
		Utils.zeroArrayRegion(ywords,0,ywords.length);
    y.getAbsolute(ywords);
    while (ylen > 1 && ywords[ylen - 1] == 0)  ylen--;

    int xlen = x.length;
		if (xwords == null || xwords.length < xlen+2)
    	xwords = new int[xlen+2];
		Utils.zeroArrayRegion(xwords,0,xwords.length);
    x.getAbsolute(xwords);
    while (xlen > 1 && xwords[xlen-1] == 0)  xlen--;


    int qlen, rlen;

    int cmpval = MPN.cmp(xwords, xlen, ywords, ylen);
    if (cmpval < 0)  // abs(x) < abs(y)
      { // quotient = 0;  remainder = num.
	int[] rwords = xwords;  xwords = ywords;  ywords = rwords;
	rlen = xlen;  qlen = 1;  xwords[0] = 0;
      }
    else if (cmpval == 0)  // abs(x) == abs(y)
      {
				xwords[0] = 1;  qlen = 1;  // quotient = 1
				ywords[0] = 0;  rlen = 1;  // remainder = 0;
      }
    else if (ylen == 1)
      {
	qlen = xlen;
	// Need to leave room for a word of leading zeros if dividing by 1
	// and the dividend has the high bit set.  It might be safe to
	// increment qlen in all cases, but it certainly is only necessary
	// in the following case.
	if (ywords[0] == 1 && xwords[xlen-1] < 0)
	  qlen++;
	rlen = 1;
	ywords[0] = MPN.divmod_1(xwords, xwords, xlen, ywords[0]);
      }
    else  // abs(x) > abs(y)
      {
	// Normalize the denominator, i.e. make its most significant bit set by
	// shifting it normalization_steps bits to the left.  Also shift the
	// numerator the same number of steps (to keep the quotient the same!).

	int nshift = MPN.count_leading_zeros(ywords[ylen - 1]);
	if (nshift != 0){
	    // Shift up the denominator setting the most significant bit of
	    // the most significant word.
	    MPN.lshift(ywords, 0, ywords, ylen, nshift);

	    // Shift up the numerator, possibly introducing a new most
	    // significant word.
	    int x_high = MPN.lshift(xwords, 0, xwords, xlen, nshift);
	    xwords[xlen++] = x_high;
	}

	if (xlen == ylen)
	  xwords[xlen++] = 0;


	MPN.divide(xwords, xlen, ywords, ylen);
	rlen = ylen;
	MPN.rshift0 (ywords, xwords, 0, rlen, nshift);

	qlen = xlen + 1 - ylen;
	if (quotient != null){
	  for (int i = 0;  i < qlen;  i++)
	    xwords[i] = xwords[i+ylen];
	}
}
if (quotient != null){
	quotient.set(xwords,0,qlen);
	if (qNegative) quotient.toNegative();
}
if (ywords[rlen-1] < 0){
  ywords[rlen] = 0;
  rlen++;
}
if (remainder != null){
	remainder.set(ywords, 0, rlen);
	if (xNegative) remainder.toNegative();
}

}
//===================================================================
public MPN mod(MPN m)
//===================================================================
{
	divide(m,null,this);
	return this;
}

static MPN s,t,u;
private native void native_modPow(MPN exponent,MPN m);
//===================================================================
public MPN modPowPositive(MPN exponent, MPN m)
//===================================================================
{
	if (hasNative) try{
		native_modPow(exponent,m);
		minimize();
		return this;
	}catch(Throwable e){
		hasNative = false;
	}
  if (m.isNegative() || m.equals(0))
    throw new ArithmeticException("non-positive modulo");
  if (exponent.isNegative())
    throw new ArithmeticException("non-positive exponent");
  if (exponent.equals(1))
    return mod(m);

	if (s == null) {
		s = new MPN();
		t = new MPN();
		u = new MPN();
	}
	s.set(1);
	t.set(this);
	u.set(exponent);
  while (!u.equals(0)){
	//if (u.and(ONE).isOne())
	  //s = times(s, t).mod(m);
		if (u.isOdd())
			s.multiply(t).mod(m);
		u.shiftRight(1);
		t.multiply(t).mod(m);
  }
	this.set(s);
	minimize();
	return this;
}
/*
//===================================================================
public int countLeadingZeros()
//===================================================================
{
	minimize();
	return count_leading_zeros(words[length-1]);
}
*/
//===================================================================
public boolean equals(int value)
//===================================================================
{
	minimize();
	if (length != 1) return false;
	return words[0] == value;
}
//===================================================================
public boolean equals(long value)
//===================================================================
{
	if (!hasLong()) return false;
	return getLong() == value;
}
//===================================================================
public int compareTo(int value)
//===================================================================
{
	minimize();
	if (!hasInt()) return signum();
	int val = getInt();
	if (val == value) return 0;
	else if (val < value) return -1;
	return 1;
}
//===================================================================
public int compareTo(long value)
//===================================================================
{
	if (!hasLong()) return signum();
	long val = getLong();
	if (val == value) return 0;
	else if (val < value) return -1;
	return 1;
}
private static MPN comp1, comp2;

//-------------------------------------------------------------------
private int compareUnsigned(MPN other)
//-------------------------------------------------------------------
{
	if (length < other.length) return -1;
	else if (length > other.length) return 1;
	else for (int i = length-1; i >= 0; i--){
		long one = (long)words[i] & 0xffffffffL;
		long two = (long)other.words[i] & 0xffffffffL;
		if (one < two) return -1;
		else if (one > two) return 1;
	}
	return 0;
}
//===================================================================
public int compareTo(MPN other)
//===================================================================
{
	if (isNegative()){
		if (other.isNegative()){
			comp1 = set(this,comp1);
			comp2 = set(this,comp2);
			return -comp1.compareUnsigned(comp2);
		}else return -1;
	}else if (other.isNegative()){
		return 1;
	}else
		return compareUnsigned(other);
}
//===================================================================
public boolean hasInt()
//===================================================================
{
	minimize();
	return length == 1;
}
//===================================================================
public boolean hasLong()
//===================================================================
{
	minimize();
	return length <= 2;
}
//===================================================================
public int getInt() throws IllegalStateException
//===================================================================
{
	minimize();
	if (length > 1) throw new IllegalStateException();
	return words[0];
}
//===================================================================
public long getLong() throws IllegalStateException
//===================================================================
{
	minimize();
	if (length > 2) throw new IllegalStateException();
	if (length == 1) return (long)words[0];
	long ret = ((long)words[1] << 32)|((long)words[0] & 0xffffffffL);
	return ret;
}
/**
* Multiply this MPN by -1 and return this.
**/
//===================================================================
public MPN toNegative()
//===================================================================
{
// Don't need a minimize() as this is done in hasLong().
	if (hasLong()){
		long val = getLong();
		if (val == 0) return this;
		if (val != 0x8000000000000000L){
			set(-val);
			return this;
		}
	}
	if (words[length-1] == 0x80000000) ensureSpace(length+1);
  if (BigInteger.negate(words, words, length))
    words[length++] = 0;
	return this;
}

  /** Add x[0:size-1] and y, and write the size least
   * significant words of the result to dest.
   * Return carry, either 0 or 1.
   * All values are unsigned.
   * This is basically the same as gmp's mpn_add_1. */
  public static int add_1 (int[] dest, int[] x, int size, int y)
  {
    long carry = (long) y & 0xffffffffL;
    for (int i = 0;  i < size;  i++)
      {
	carry += ((long) x[i] & 0xffffffffL);
	dest[i] = (int) carry;
	carry >>= 32;
      }
    return (int) carry;
  }

  /** Add x[0:len-1] and y[0:len-1] and write the len least
   * significant words of the result to dest[0:len-1].
   * All words are treated as unsigned.
   * @return the carry, either 0 or 1
   * This function is basically the same as gmp's mpn_add_n.
   */
  public static int add_n (int dest[], int[] x, int[] y, int len)
  {
    long carry = 0;
    for (int i = 0; i < len;  i++)
      {
	carry += ((long) x[i] & 0xffffffffL)
	  + ((long) y[i] & 0xffffffffL);
	dest[i] = (int) carry;
	carry >>>= 32;
      }
    return (int) carry;
  }

  /** Subtract Y[0:size-1] from X[0:size-1], and write
   * the size least significant words of the result to dest[0:size-1].
   * Return borrow, either 0 or 1.
   * This is basically the same as gmp's mpn_sub_n function.
   */

  public static int sub_n (int[] dest, int[] X, int[] Y, int size)
  {
    int cy = 0;
    for (int i = 0;  i < size;  i++)
      {
	int y = Y[i];
	int x = X[i];
	y += cy;	/* add previous carry to subtrahend */
	// Invert the high-order bit, because: (unsigned) X > (unsigned) Y
	// iff: (int) (X^0x80000000) > (int) (Y^0x80000000).
	cy = (y^0x80000000) < (cy^0x80000000) ? 1 : 0;
	y = x - y;
	cy += (y^0x80000000) > (x ^ 0x80000000) ? 1 : 0;
	dest[i] = y;
      }
    return cy;
  }

  /** Multiply x[0:len-1] by y, and write the len least
   * significant words of the product to dest[0:len-1].
   * Return the most significant word of the product.
   * All values are treated as if they were unsigned
   * (i.e. masked with 0xffffffffL).
   * OK if dest==x (not sure if this is guaranteed for mpn_mul_1).
   * This function is basically the same as gmp's mpn_mul_1.
   */

  public static int mul_1 (int[] dest, int[] x, int len, int y)
  {
    long yword = (long) y & 0xffffffffL;
    long carry = 0;
    for (int j = 0;  j < len; j++)
      {
        carry += ((long) x[j] & 0xffffffffL) * yword;
        dest[j] = (int) carry;
        carry >>>= 32;
      }
    return (int) carry;
  }

private static native void nativeMul(int[] dest,
			  int[] x, int xlen,
			  int[] y, int ylen);
private static boolean hasNative = true;
  /**
   * Multiply x[0:xlen-1] and y[0:ylen-1], and
   * write the result to dest[0:xlen+ylen-1].
   * The destination has to have space for xlen+ylen words,
   * even if the result might be one limb smaller.
   * This function requires that xlen >= ylen.
   * The destination must be distinct from either input operands.
   * All operands are unsigned.
   * This function is basically the same gmp's mpn_mul. */

  public static void mul (int[] dest,
			  int[] x, int xlen,
			  int[] y, int ylen)
  {
		if (hasNative)try{
			nativeMul(dest,x,xlen,y,ylen);
			return;
		}catch(Throwable t){
			hasNative = false;
		}
    dest[xlen] = MPN.mul_1 (dest, x, xlen, y[0]);

    for (int i = 1;  i < ylen; i++)
      {
	long yword = (long) y[i] & 0xffffffffL;
	long carry = 0;
	for (int j = 0;  j < xlen; j++)
	  {
	    carry += ((long) x[j] & 0xffffffffL) * yword
	      + ((long) dest[i+j] & 0xffffffffL);
	    dest[i+j] = (int) carry;
	    carry >>>= 32;
	  }
	dest[i+xlen] = (int) carry;
      }
  }

  /* Divide (unsigned long) N by (unsigned int) D.
   * Returns (remainder << 32)+(unsigned int)(quotient).
   * Assumes (unsigned int)(N>>32) < (unsigned int)D.
   * Code transcribed from gmp-2.0's mpn_udiv_w_sdiv function.
   */
  public static long udiv_qrnnd (long N, int D)
  {
    long q, r;
    long a1 = N >>> 32;
    long a0 = N & 0xffffffffL;
    if (D >= 0)
      {
	if (a1 < ((D - a1 - (a0 >>> 31)) & 0xffffffffL))
	  {
	    /* dividend, divisor, and quotient are nonnegative */
	    q = N / D;
	    r = N % D;
	  }
	else
	  {
	    /* Compute c1*2^32 + c0 = a1*2^32 + a0 - 2^31*d */
	    long c = N - ((long) D << 31);
	    /* Divide (c1*2^32 + c0) by d */
	    q = c / D;
	    r = c % D;
	    /* Add 2^31 to quotient */
	    q += 1 << 31;
	  }
      }
    else
      {
	long b1 = D >>> 1;	/* d/2, between 2^30 and 2^31 - 1 */
	//long c1 = (a1 >> 1); /* A/2 */
	//int c0 = (a1 << 31) + (a0 >> 1);
	long c = N >>> 1;
	if (a1 < b1 || (a1 >> 1) < b1)
	  {
	    if (a1 < b1)
	      {
		q = c / b1;
		r = c % b1;
	      }
	    else /* c1 < b1, so 2^31 <= (A/2)/b1 < 2^32 */
	      {
		c = ~(c - (b1 << 32));
		q = c / b1;  /* (A/2) / (d/2) */
		r = c % b1;
		q = (~q) & 0xffffffffL;    /* (A/2)/b1 */
		r = (b1 - 1) - r; /* r < b1 => new r >= 0 */
	      }
	    r = 2 * r + (a0 & 1);
	    if ((D & 1) != 0)
	      {
		if (r >= q) {
		        r = r - q;
		} else if (q - r <= ((long) D & 0xffffffffL)) {
                       r = r - q + D;
        		q -= 1;
		} else {
                       r = r - q + D + D;
        		q -= 2;
		}
	      }
	  }
	else				/* Implies c1 = b1 */
	  {				/* Hence a1 = d - 1 = 2*b1 - 1 */
	    if (a0 >= ((long)(-D) & 0xffffffffL))
	      {
		q = -1;
	        r = a0 + D;
 	      }
	    else
	      {
		q = -2;
	        r = a0 + D + D;
	      }
	  }
      }

    return (r << 32) | (q & 0xFFFFFFFFl);
  }

    /** Divide dividend[0:len-1] by (unsigned int)divisor.
     * Write result into quotient[0:len-1.
     * Return the one-word (unsigned) remainder.
     * OK for quotient==dividend.
     */

  public static int divmod_1 (int[] quotient, int[] dividend,
			      int len, int divisor)
  {
    int i = len - 1;
    long r = dividend[i];
    if ((r & 0xffffffffL) >= ((long)divisor & 0xffffffffL))
      r = 0;
    else
      {
	quotient[i--] = 0;
	r <<= 32;
      }

    for (;  i >= 0;  i--)
      {
	int n0 = dividend[i];
	r = (r & ~0xffffffffL) | (n0 & 0xffffffffL);
	r = udiv_qrnnd (r, divisor);
	quotient[i] = (int) r;
      }
    return (int)(r >> 32);
  }

  /* Subtract x[0:len-1]*y from dest[offset:offset+len-1].
   * All values are treated as if unsigned.
   * @return the most significant word of
   * the product, minus borrow-out from the subtraction.
   */
  public static int submul_1 (int[] dest, int offset, int[] x, int len, int y)
  {
    long yl = (long) y & 0xffffffffL;
    int carry = 0;
    int j = 0;
    do
      {
	long prod = ((long) x[j] & 0xffffffffL) * yl;
	int prod_low = (int) prod;
	int prod_high = (int) (prod >> 32);
	prod_low += carry;
	// Invert the high-order bit, because: (unsigned) X > (unsigned) Y
	// iff: (int) (X^0x80000000) > (int) (Y^0x80000000).
	carry = ((prod_low ^ 0x80000000) < (carry ^ 0x80000000) ? 1 : 0)
	  + prod_high;
	int x_j = dest[offset+j];
	prod_low = x_j - prod_low;
	if ((prod_low ^ 0x80000000) > (x_j ^ 0x80000000))
	  carry++;
	dest[offset+j] = prod_low;
      }
    while (++j < len);
    return carry;
  }

	private static native void nativeDivide(int[] zds, int nx, int[] y, int ny);

  /** Divide zds[0:nx] by y[0:ny-1].
   * The remainder ends up in zds[0:ny-1].
   * The quotient ends up in zds[ny:nx].
   * Assumes:  nx>ny.
   * (int)y[ny-1] < 0  (i.e. most significant bit set)
   */

  public static void divide (int[] zds, int nx, int[] y, int ny)
  {
    // This is basically Knuth's formulation of the classical algorithm,
    // but translated from in scm_divbigbig in Jaffar's SCM implementation.

    // Correspondance with Knuth's notation:
    // Knuth's u[0:m+n] == zds[nx:0].
    // Knuth's v[1:n] == y[ny-1:0]
    // Knuth's n == ny.
    // Knuth's m == nx-ny.
    // Our nx == Knuth's m+n.

    // Could be re-implemented using gmp's mpn_divrem:
    // zds[nx] = mpn_divrem (&zds[ny], 0, zds, nx, y, ny).

		if (hasNative)try{
			nativeDivide(zds,nx,y,ny);
			return;
		}catch(Throwable t){
			hasNative = false;
		}
    int j = nx;
    do
      {                          // loop over digits of quotient
	// Knuth's j == our nx-j.
	// Knuth's u[j:j+n] == our zds[j:j-ny].
	int qhat;  // treated as unsigned
	if (zds[j]==y[ny-1])
	  qhat = -1;  // 0xffffffff
	else
	  {
	    long w = (((long)(zds[j])) << 32) + ((long)zds[j-1] & 0xffffffffL);
	    qhat = (int) udiv_qrnnd (w, y[ny-1]);
	  }
	if (qhat != 0)
	  {
	    int borrow = submul_1 (zds, j - ny, y, ny, qhat);
	    int save = zds[j];
	    long num = ((long)save&0xffffffffL) - ((long)borrow&0xffffffffL);
            while (num != 0)
	      {
		qhat--;
		long carry = 0;
		for (int i = 0;  i < ny; i++)
		  {
		    carry += ((long) zds[j-ny+i] & 0xffffffffL)
		      + ((long) y[i] & 0xffffffffL);
		    zds[j-ny+i] = (int) carry;
		    carry >>>= 32;
		  }
		zds[j] += carry;
		num = carry - 1;
	      }
	  }
	zds[j] = qhat;
      } while (--j >= ny);
  }

  /** Number of digits in the conversion base that always fits in a word.
   * For example, for base 10 this is 9, since 10**9 is the
   * largest number that fits into a words (assuming 32-bit words).
   * This is the same as gmp's __mp_bases[radix].chars_per_limb.
   * @param radix the base
   * @return number of digits */
  public static int chars_per_word (int radix)
  {
    if (radix < 10)
      {
	if (radix < 8)
	  {
	    if (radix <= 2)
	      return 32;
	    else if (radix == 3)
	      return 20;
	    else if (radix == 4)
	      return 16;
	    else
	      return 18 - radix;
	  }
	else
	  return 10;
      }
    else if (radix < 12)
      return 9;
    else if (radix <= 16)
      return 8;
    else if (radix <= 23)
      return 7;
    else if (radix <= 40)
      return 6;
    // The following are conservative, but we don't care.
    else if (radix <= 256)
      return 4;
    else
      return 1;
  }

  /** Count the number of leading zero bits in an int. */
  public static int count_leading_zeros (int i)
  {
    if (i == 0)
      return 32;
    int count = 0;
    for (int k = 16;  k > 0;  k = k >> 1) {
      int j = i >>> k;
      if (j == 0)
	count += k;
      else
	i = j;
    }
    return count;
  }

  public static int set_str (int dest[], byte[] str, int str_len, int base)
  {
    int size = 0;
    if ((base & (base - 1)) == 0)
      {
	// The base is a power of 2.  Read the input string from
	// least to most significant character/digit.  */

	int next_bitpos = 0;
	int bits_per_indigit = 0;
	for (int i = base; (i >>= 1) != 0; ) bits_per_indigit++;
	int res_digit = 0;

	for (int i = str_len;  --i >= 0; )
	  {
	    int inp_digit = str[i];
	    res_digit |= inp_digit << next_bitpos;
	    next_bitpos += bits_per_indigit;
	    if (next_bitpos >= 32)
	      {
		dest[size++] = res_digit;
		next_bitpos -= 32;
		res_digit = inp_digit >> (bits_per_indigit - next_bitpos);
	      }
	  }

	if (res_digit != 0)
	  dest[size++] = res_digit;
      }
    else
      {
	// General case.  The base is not a power of 2.
	int indigits_per_limb = MPN.chars_per_word (base);
	int str_pos = 0;

	while (str_pos < str_len)
	  {
	    int chunk = str_len - str_pos;
	    if (chunk > indigits_per_limb)
	      chunk = indigits_per_limb;
	    int res_digit = str[str_pos++];
	    int big_base = base;

	    while (--chunk > 0)
	      {
		res_digit = res_digit * base + str[str_pos++];
		big_base *= base;
	      }

	    int cy_limb;
	    if (size == 0)
	      cy_limb = res_digit;
	    else
	      {
		cy_limb = MPN.mul_1 (dest, dest, size, big_base);
		cy_limb += MPN.add_1 (dest, dest, size, res_digit);
	      }
	    if (cy_limb != 0)
	      dest[size++] = cy_limb;
	  }
       }
    return size;
  }

  /** Compare x[0:size-1] with y[0:size-1], treating them as unsigned integers.
   * @result -1, 0, or 1 depending on if x<y, x==y, or x>y.
   * This is basically the same as gmp's mpn_cmp function.
   */
  public static int cmp (int[] x, int[] y, int size)
  {
    while (--size >= 0)
      {
	int x_word = x[size];
	int y_word = y[size];
	if (x_word != y_word)
	  {
	    // Invert the high-order bit, because:
	    // (unsigned) X > (unsigned) Y iff
	    // (int) (X^0x80000000) > (int) (Y^0x80000000).
	    return (x_word ^ 0x80000000) > (y_word ^0x80000000) ? 1 : -1;
	  }
      }
    return 0;
  }

  /** Compare x[0:xlen-1] with y[0:ylen-1], treating them as unsigned integers.
   * @result -1, 0, or 1 depending on if x<y, x==y, or x>y.
   */
  public static int cmp (int[] x, int xlen, int[] y, int ylen)
  {
    return xlen > ylen ? 1 : xlen < ylen ? -1 : cmp (x, y, xlen);
  }

  /* Shift x[x_start:x_start+len-1] count bits to the "right"
   * (i.e. divide by 2**count).
   * Store the len least significant words of the result at dest.
   * The bits shifted out to the right are returned.
   * OK if dest==x.
   * Assumes: 0 < count < 32
   */

  public static int rshift (int[] dest, int[] x, int x_start,
			    int len, int count)
  {
    int count_2 = 32 - count;
    int low_word = x[x_start];
    int retval = low_word << count_2;
    int i = 1;
    for (; i < len;  i++)
      {
	int high_word = x[x_start+i];
	dest[i-1] = (low_word >>> count) | (high_word << count_2);
	low_word = high_word;
      }
    dest[i-1] = low_word >>> count;
    return retval;
  }

  /* Shift x[x_start:x_start+len-1] count bits to the "right"
   * (i.e. divide by 2**count).
   * Store the len least significant words of the result at dest.
   * OK if dest==x.
   * Assumes: 0 <= count < 32
   * Same as rshift, but handles count==0 (and has no return value).
   */
  public static void rshift0 (int[] dest, int[] x, int x_start,
			      int len, int count)
  {
    if (count > 0)
      rshift(dest, x, x_start, len, count);
    else
      for (int i = 0;  i < len;  i++)
	dest[i] = x[i + x_start];
  }

  /** Return the long-truncated value of right shifting.
  * @param x a two's-complement "bignum"
  * @param len the number of significant words in x
  * @param count the shift count
  * @return (long)(x[0..len-1] >> count).
  */
  public static long rshift_long (int[] x, int len, int count)
  {
    int wordno = count >> 5;
    count &= 31;
    int sign = x[len-1] < 0 ? -1 : 0;
    int w0 = wordno >= len ? sign : x[wordno];
    wordno++;
    int w1 = wordno >= len ? sign : x[wordno];
    if (count != 0)
      {
	wordno++;
	int w2 = wordno >= len ? sign : x[wordno];
	w0 = (w0 >>> count) | (w1 << (32-count));
	w1 = (w1 >>> count) | (w2 << (32-count));
      }
    return ((long)w1 << 32) | ((long)w0 & 0xffffffffL);
  }

  /* Shift x[0:len-1] left by count bits, and store the len least
   * significant words of the result in dest[d_offset:d_offset+len-1].
   * Return the bits shifted out from the most significant digit.
   * Assumes 0 < count < 32.
   * OK if dest==x.
   */

  public static int lshift (int[] dest, int d_offset,
			    int[] x, int len, int count)
  {
    int count_2 = 32 - count;
    int i = len - 1;
    int high_word = x[i];
    int retval = high_word >>> count_2;
    d_offset++;
    while (--i >= 0)
      {
	int low_word = x[i];
	dest[d_offset+i] = (high_word << count) | (low_word >>> count_2);
	high_word = low_word;
      }
    dest[d_offset+i] = high_word << count;
    return retval;
  }

  /** Return least i such that word&(1<<i). Assumes word!=0. */

  public static int findLowestBit (int word)
  {
    int i = 0;
    while ((word & 0xF) == 0)
      {
	word >>= 4;
	i += 4;
      }
    if ((word & 3) == 0)
      {
	word >>= 2;
	i += 2;
      }
    if ((word & 1) == 0)
      i += 1;
    return i;
  }

  /** Return least i such that words & (1<<i). Assumes there is such an i. */

  public static int findLowestBit (int[] words)
  {
    for (int i = 0;  ; i++)
      {
	if (words[i] != 0)
	  return 32 * i + findLowestBit (words[i]);
      }
  }

  /** Calculate Greatest Common Divisior of x[0:len-1] and y[0:len-1].
    * Assumes both arguments are non-zero.
    * Leaves result in x, and returns len of result.
    * Also destroys y (actually sets it to a copy of the result). */

  public static int gcd (int[] x, int[] y, int len)
  {
    int i, word;
    // Find sh such that both x and y are divisible by 2**sh.
    for (i = 0; ; i++)
      {
	word = x[i] | y[i];
	if (word != 0)
	  {
	    // Must terminate, since x and y are non-zero.
	    break;
	  }
      }
    int initShiftWords = i;
    int initShiftBits = findLowestBit (word);
    // Logically: sh = initShiftWords * 32 + initShiftBits

    // Temporarily devide both x and y by 2**sh.
    len -= initShiftWords;
    MPN.rshift0 (x, x, initShiftWords, len, initShiftBits);
    MPN.rshift0 (y, y, initShiftWords, len, initShiftBits);

    int[] odd_arg; /* One of x or y which is odd. */
    int[] other_arg; /* The other one can be even or odd. */
    if ((x[0] & 1) != 0)
      {
	odd_arg = x;
	other_arg = y;
      }
    else
      {
	odd_arg = y;
	other_arg = x;
      }

    for (;;)
      {
	// Shift other_arg until it is odd; this doesn't
	// affect the gcd, since we divide by 2**k, which does not
	// divide odd_arg.
	for (i = 0; other_arg[i] == 0; ) i++;
	if (i > 0)
	  {
	    int j;
	    for (j = 0; j < len-i; j++)
		other_arg[j] = other_arg[j+i];
	    for ( ; j < len; j++)
	      other_arg[j] = 0;
	  }
	i = findLowestBit(other_arg[0]);
	if (i > 0)
	  MPN.rshift (other_arg, other_arg, 0, len, i);

	// Now both odd_arg and other_arg are odd.

	// Subtract the smaller from the larger.
	// This does not change the result, since gcd(a-b,b)==gcd(a,b).
	i = MPN.cmp(odd_arg, other_arg, len);
	if (i == 0)
	    break;
	if (i > 0)
	  { // odd_arg > other_arg
	    MPN.sub_n (odd_arg, odd_arg, other_arg, len);
	    // Now odd_arg is even, so swap with other_arg;
	    int[] tmp = odd_arg; odd_arg = other_arg; other_arg = tmp;
	  }
	else
	  { // other_arg > odd_arg
	    MPN.sub_n (other_arg, other_arg, odd_arg, len);
	}
	while (odd_arg[len-1] == 0 && other_arg[len-1] == 0)
	  len--;
    }
    if (initShiftWords + initShiftBits > 0)
      {
	if (initShiftBits > 0)
	  {
	    int sh_out = MPN.lshift (x, initShiftWords, x, len, initShiftBits);
	    if (sh_out != 0)
	      x[(len++)+initShiftWords] = sh_out;
	  }
	else
	  {
	    for (i = len; --i >= 0;)
	      x[i+initShiftWords] = x[i];
	  }
	for (i = initShiftWords;  --i >= 0; )
	  x[i] = 0;
	len += initShiftWords;
      }
    return len;
  }

  public static int intLength (int i)
  {
    return 32 - count_leading_zeros (i < 0 ? ~i : i);
  }

  /** Calcaulte the Common Lisp "integer-length" function.
   * Assumes input is canonicalized:  len==BigInteger.wordsNeeded(words,len) */
  public static int intLength (int[] words, int len)
  {
    len--;
    return intLength (words[len]) + 32 * len;
  }

  /* DEBUGGING:
  public static void dprint (BigInteger x)
  {
    if (x.words == null)
      System.err.print(Long.toString((long) x.ival & 0xffffffffL, 16));
    else
      dprint (System.err, x.words, x.ival);
  }
  public static void dprint (int[] x) { dprint (System.err, x, x.length); }
  public static void dprint (int[] x, int len) { dprint (System.err, x, len); }
  public static void dprint (java.io.PrintStream ps, int[] x, int len)
  {
    ps.print('(');
    for (int i = 0;  i < len; i++)
      {
	if (i > 0)
	  ps.print (' ');
	ps.print ("#x" + Long.toString ((long) x[i] & 0xffffffffL, 16));
      }
    ps.print(')');
  }
  */

//===================================================================
boolean equals(BigInteger bi)
//===================================================================
{
	return bi.equals(toBigInteger());
}
//-------------------------------------------------------------------
native static int test(MPN one,MPN two,MPN three,MPN four,int which);
//-------------------------------------------------------------------
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	BigInteger m;
/*
	BigInteger z = new BigInteger("2",16);
	m = new BigInteger("762358ed50a5cf28edfae10d",16);
	BigInteger ths = new BigInteger("3b11ac76a852e79476fd70869",16);
	BigInteger mp = z.modPow(m,ths);
	*/
/*
	BigInteger z = new BigInteger("c23ba9453a5027d51782f89feb36b7f96c0d34a612f156eb",16);
	m = new BigInteger("3b11ac76a852e79476fd70869",16);
	BigInteger ths = new BigInteger("3b11ac76a852e79476fd70869",16);
	BigInteger mp = new BigInteger();
	BigInteger.divide(z,m,ths,mp,3);
*/
	/*
	ewe.sys.Vm.debug("Java = Q: "+ths.toString(16)+", R: "+mp.toString(16));

	MPN zm = new MPN().set(z);
	MPN mm = new MPN().set(m);
	MPN thsm = new MPN().set(ths);
	MPN mpm = new MPN();
	test(zm,mm,thsm,mpm,2);
	ewe.sys.Vm.debug("Native = Q: "+thsm.toBigInteger().toString(16)+", R: "+mpm.toBigInteger().toString(16));

	if (true){
		ewe.sys.mThread.nap(10000);
		return;
	}
	*/
	ewe.security.SecureRandom sr = new ewe.security.SecureRandom();
	for (int pp = 0; pp < 10000; pp++){
		BigInteger bi = new BigInteger(400,80,sr);
		ewe.sys.Vm.debug("Got: "+bi);
	}
	if (true){
		ewe.sys.mThread.nap(10000);
		return;
		//System.exit(0);
	}
	BigInteger bi = new BigInteger(args[0]);
	BigInteger b2 = new BigInteger(args[1]);

	m = new BigInteger("12354884578475874858754875874853");
	BigInteger qo = new BigInteger();
	BigInteger rem = new BigInteger();

	MPN m1 = new MPN().set(bi);
	MPN m2 = new MPN().set(b2);
	MPN q = new MPN().set(m);
	MPN r = new MPN();
	long now = 0;

	try{
		//test(m1,m2,q,r,0);
		now = System.currentTimeMillis();
		//for (int i = 0; i<10000; i++)
			test(m1,m2,q,r,2);
		now = System.currentTimeMillis()-now;
		ewe.sys.Vm.debug("MPN R: "+r+", "+now);
	}catch(ArithmeticException e){
		e.printStackTrace();
	}catch(Throwable t){

	}
		now = System.currentTimeMillis();
		//for (int i = 0; i<10000; i++)
			//BigInteger.divide(bi,b2,qo,rem,3);
			rem = bi.modPow(b2,m);
			//qo = bi.multiply(b2);
		now = System.currentTimeMillis()-now;
		ewe.sys.Vm.debug(" BI R: "+rem+", "+now);

	if (true){
		ewe.sys.mThread.nap(10000);
		System.exit(0);
	}
	/*
	now = System.currentTimeMillis();
	for (int i = 0; i<10000; i++){
		m1.set(bi).multiply(m2);
	}
	now = System.currentTimeMillis()-now;
	ewe.sys.Vm.debug(q+", "+r+" MPN: "+now);
*/
	now = System.currentTimeMillis();
	for (int i = 0; i<100; i++){
		bi.multiply(b2);
	}
	now = System.currentTimeMillis()-now;
	ewe.sys.Vm.debug(qo+", "+rem+" BI: "+now);


	//BigInteger result = bi.multiply(b2);

	now = System.currentTimeMillis();
	BigInteger res = null;
	for (int i = 0; i<100; i++)
		res = bi.modPow(b2,m);
	now = System.currentTimeMillis()-now;
	ewe.sys.Vm.debug("Result: "+res+", "+now);

	/*
	MPN m3 = new MPN().set(m1);
	MPN m4 = new MPN().set(m);
 	now = System.currentTimeMillis();
	for (int i = 0; i<100; i++)
		m1.set(m3).modPow(m2,m4);
	now = System.currentTimeMillis()-now;
	ewe.sys.Vm.debug("Result: "+m1+", "+now);
	*/
/*
	bi = bi.shiftLeft(64);
	m1.shiftLeft(64);
	//MPN m = m1.multiply(m2);

	ewe.sys.Vm.debug(m1+", "+bi+", "+m1.equals(bi));
	*/
/* Shift test.
	int toShift = 10;
	for (int i = 0; i<10; i++){
		bi = bi.shiftRight(toShift);
		m.shiftRight(toShift);
		if (!m.equals(bi)){
			ewe.sys.Vm.debug("Not equal: "+i);
			break;
		}
		ewe.sys.Vm.out.println(i+": "+bi);
	}
*/
	//ewe.sys.Vm.exit(0);
}

}
