package ewe.security;
import ewe.math.BigInteger;
/**
* This class is used to generate RSA Public Key pairs.
**/
//##################################################################
public class RSA{
//##################################################################
	/**
	* Create a new RSAKey pair with a length of 400 bits and using the current
	* time as the random seed.
	* @return An array of two keys - either one of which can be the private or public key.
	**/
	//===================================================================
	public static RSAKey[] createNew()
	//===================================================================
	{
		return createNew(400,System.currentTimeMillis());
	}
	/**
	* Create a new RSAKey pair. Note that this method can take quite some time to
	* execute, depending on the bit size required.
	* @param bitlen the number of bits for each key.
	* @param seed any random seed value.
	* @return An array of two keys - either one of which can be the private or public key.
	*/
	//===================================================================
	public static RSAKey[] createNew(int bitlen,long seed)
	//===================================================================
	{
		BigInteger n, d, e;
    SecureRandom r = new SecureRandom();
		if (seed == 0) seed = System.currentTimeMillis();
		r.setSeed(seed);
    BigInteger p = new BigInteger(bitlen / 2, 100, r);
    BigInteger q = new BigInteger(bitlen / 2, 100, r);
    n = p.multiply(q);
    BigInteger m = (p.subtract(BigInteger.ONE))
                   .multiply(q.subtract(BigInteger.ONE));
    e = new BigInteger("3");
    while(m.gcd(e).intValue() > 1) e = e.add(new BigInteger("2"));
    d = e.modInverse(m);

		RSAKey[] ret = new RSAKey[2];
		ret[0] = new RSAKey();
		ret[1] = new RSAKey();
		ret[0].exp = e;
		ret[1].exp = d;
		ret[0].value = ret[1].value = n;
		return ret;
	}
//-------------------------------------------------------------------
static String toString(byte[] bytes)
//-------------------------------------------------------------------
{
	String ret = "";
	for (int i = 0; i<bytes.length; i++){
		if (i != 0) ret += ", ";
		ret += ewe.sys.Convert.intToHexString((int)bytes[i] & 0xff);
	}
	return ret;
}
//##################################################################
}
//##################################################################

