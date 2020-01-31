/* $MirOS: contrib/hosted/ewe/classes/ewe/security/SecureRandom.java,v 1.4 2008/05/03 01:09:30 tg Exp $ */

/*-
 * Copyright (c) 2008
 *	Thorsten Glaser <tg@mirbsd.de>
 *
 * Provided that these terms and disclaimer and all copyright notices
 * are retained or reproduced in an accompanying document, permission
 * is granted to deal in this work without restriction, including un-
 * limited rights to use, publicly perform, distribute, sell, modify,
 * merge, give away, or sublicence.
 *
 * This work is provided "AS IS" and WITHOUT WARRANTY of any kind, to
 * the utmost extent permitted by applicable law, neither express nor
 * implied; without malicious intent or gross negligence. In no event
 * may a licensor, author or contributor be held liable for indirect,
 * direct, other damage, loss, or other issues arising in any way out
 * of dealing in the work, even if advised of the possibility of such
 * damage or existence of a defect, except proven that it results out
 * of said person's immediate fault when using the work as intended.
 *-
 * Implement the ewe.security.SecureRandom API using arc4random(3)
 */

package ewe.security;
import ewe.util.Random;

/**
 * This class is intended to be used as a secure Random replacement.
 */
public class SecureRandom extends Random {
	/**
	 * Create a new instance of ourselves (no-op)
	 */
	public
	SecureRandom() {
		/* no need to initialise anything */
	}

	/**
	 * Create a new instance of ourselves (no-op)
	 * @param seed (byte[]) is fed into the PRNG
	 */
	public
	SecureRandom(byte[] seed) {
		this();
		setSeed(seed);
	}

	/**
	 * Return a 32-bit random number from arc4random(3)
	 * @param buf (byte[]) is fed into the PRNG
	 * @param len (int) number of bytes to use
	 * @return (uint32_t) secure random value
	 */
	public native int arc4random_pushb(byte[] buf, int len);

	/**
	 * Return a 32-bit random number from arc4random(3)
	 * @param buf (byte[]) is fed into the PRNG (entire array)
	 * @return (uint32_t) secure random value
	 */
	public int
	arc4random_pushb(byte[] buf)
	{
		return (arc4random_pushb(buf, buf.length));
	}

	/**
	 * Return a 32-bit random number from arc4random(3)
	 * @return (uint32_t) secure random value
	 */
	public int
	arc4random()
	{
		return (arc4random_pushb(null, 0));
	}

	/**
	 * Feed its argument into the PRNG (not needed)
	 * @param seed (byte[]) is fed into the PRNG (entire array)
	 */
	public void
	setSeed(byte[] seed)
	{
		arc4random_pushb(seed, seed.length);
	}

	/**
	 * Feed its argument into the PRNG (not needed)
	 * @param seed (long) is fed into the PRNG
	 */
	public void
	setSeed(long seed)
	{
		byte tmp[] = {
			(byte)((seed >> 56) & 0xFF),
			(byte)((seed >> 48) & 0xFF),
			(byte)((seed >> 40) & 0xFF),
			(byte)((seed >> 32) & 0xFF),
			(byte)((seed >> 24) & 0xFF),
			(byte)((seed >> 16) & 0xFF),
			(byte)((seed >> 8) & 0xFF),
			(byte)(seed & 0xFF)
		};
		setSeed(tmp);
	}

	void
	printSeed()
	{
		long i;		// treat as unsigned int

		i = arc4random_pushb(null, 0) & 0xFFFFFFFFL;
		ewe.sys.Vm.debug("Seed: using arc4random, " + i);
	}

	public void
	nextBytes(byte[] bytes)
	{
		int i = 0, pos = 0, rnd = 0;

		while (pos < bytes.length) {
			if (i == 0) {
				rnd = arc4random_pushb(null, 0);
				i = 4;
			}
			bytes[pos++] = (byte)(rnd & 0xFF);
			rnd >>= 8;
			i--;
		}
	}

	/**
	 * Generate a number of random bytes, like get_random_bytes(9)
	 * but into a newly created byte array
	 * @param numBytes (int) number of bytes to emit
	 * @return (byte []) array of secure random bytes
	 */
	public byte[]
	generateSeed(int numBytes)
	{
		byte tmp[] = new byte[numBytes];

		nextBytes(tmp);
		return (tmp);
	}

	/**
	 * Generates an integer containing the user-specified
	 * number of random bits, right-justified, zero-padded.
	 * @param numBits (int) number of random bits to get (0..32)
	 * @return (int) the random bits
	 */
	protected final int
	next(int numBits)
	{
		return ((int)(arc4random() & ((1L << numBits) - 1)));
	}

/*
	private void
	ensureIsSeeded()
	{
		// not needed, as arc4random(3) is self-seeding
	}
*/

}
