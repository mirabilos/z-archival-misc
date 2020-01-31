/*
 * Created on Nov 25, 2005
 *
 * Michael L Brereton - www.ewesoft.com
 *
 *
 */
package ewe.sys;

/**
 * @author Michael L Brereton
 *
 */
//####################################################
public class Test {
	private static String toHex(double value)
	{
		return java.lang.Long.toHexString(java.lang.Double.doubleToLongBits(value));
	}
	public static long aLong = 1234;
	public static double aDouble = 567.8901;
	public static final long finalLong = 1234;
	public static final double finalDouble = 567.8901;

	public static void main(String[] args)
	{
		Vm.startEwe(args);
		if (true){
			Vm.debug("Test values: "+finalLong+" & "+finalDouble+", Should be 1234 & 567.8901");
			try{
				long fl = Test.class.getField("finalLong").getLong(null);
				double fd = Test.class.getField("finalDouble").getDouble(null);
				Vm.debug("Test values: "+fl+" & "+fd+", Should be 1234 & 567.8901");
			}catch(Exception e){e.printStackTrace();}
			Vm.debug("Test values: "+aLong+" & "+aDouble+", Should be 1234 & 567.8901");
			int al = (int)aLong;
			float af = (float)aDouble;
			Vm.debug("Test values: "+al+" & "+af+", Should be 1234 & 567.8901 (aprox.)");
			long bits = 0x1234567812345678L;
			double db = java.lang.Double.longBitsToDouble(bits);
			bits = java.lang.Double.doubleToRawLongBits(db);
			Vm.debug("Test values: "+java.lang.Long.toHexString(bits)+" & "+db);
			Vm.debug(toHex(java.lang.Double.NaN)+", "+toHex(java.lang.Double.POSITIVE_INFINITY)+", "+toHex(java.lang.Double.NEGATIVE_INFINITY));
		}
		mThread.nap(5000);
		Vm.exit(0);
	}
}

//####################################################
