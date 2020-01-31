/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/security/SecureRandom.java,v 1.1 2008/04/11 03:30:16 tg Exp $ */

/* SHA1PRNG.java --- Secure Random SPI SHA1PRNG
   Copyright (C) 1999, 2001, 2003 Free Software Foundation, Inc.

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


package ewe.security;
import ewe.util.Random;
import ewe.sys.Vm;
public class SecureRandom extends Random
{
  //MessageDigest digest;
	SHA1 digest;
  byte seed[];
  byte data[];
  int seedpos;
  int datapos;
  private boolean seeded = false; // set to true when we seed this

	//===================================================================
  public SecureRandom()
	//===================================================================
  {
		digest = new SHA1();
		seed = new byte[20];
    seedpos = 0;
    data = new byte[40];
    datapos = 20;  // try to force hashing a first block
  }
	//===================================================================
  public SecureRandom(byte[] seed)
	//===================================================================
  {
    this();
    setSeed(seed);
  }
	//===================================================================
  public void setSeed(byte[] seed)
	//===================================================================
  {
		//ewe.sys.Vm.debug("SS");
		if (this.seed == null) this.seed = new byte[20];
    for(int i = 0; i < seed.length; i++)
      this.seed[seedpos++ % 20] ^= seed[i];
    seedpos %= 20;
		//printSeed();
		seeded = true;
  }
  /**
     Seeds the SecureRandom. The class is re-seeded for each call and
     each seed builds on the previous seed so as not to weaken security.

     @param seed 8 seed bytes to seed with
   */
	//===================================================================
  public void setSeed(long seed)
	//===================================================================
  {
    // This particular setSeed will be called by Random.Random(), via
    // our own constructor, before secureRandomSpi is initialized.  In
    // this case we can't call a method on secureRandomSpi, and we
    // definitely don't want to throw a NullPointerException.
    // Therefore we test.
        byte tmp[] = { (byte) (0xff & (seed >> 56)),
		       (byte) (0xff & (seed >> 48)),
		       (byte) (0xff & (seed >> 40)),
		       (byte) (0xff & (seed >> 32)),
		       (byte) (0xff & (seed >> 24)),
		       (byte) (0xff & (seed >> 16)),
		       (byte) (0xff & (seed >> 8)),
		       (byte) (0xff & seed)
        };
        setSeed(tmp);
  }
	//===================================================================
	void printSeed()
	//===================================================================
	{
		String s = new String();
		for (int i = 0; i<seed.length; i++){
			int v = seed[i] & 0xff;
			s += ewe.sys.Convert.formatInt(v,16);
		}
		ewe.sys.Vm.debug("Seed: "+s);
	}
	boolean shown = false;
	//===================================================================
  public void nextBytes(byte[] bytes)
	//===================================================================
  {
    ensureIsSeeded ();
    int loc = 0;
    while (loc < bytes.length)
      {
	int copy = Math.min (bytes.length - loc, 20 - datapos);

	if (copy > 0)
	  {
	    Vm.copyArray (data, datapos, bytes, loc, copy);
	    datapos += copy;
	    loc += copy;
	  }
	else
	  {
	    // No data ready for copying, so refill our buffer.
			//if (!shown) printSeed();
	    Vm.copyArray( seed, 0, data, 20, 20);
	    byte[] digestdata = digest.digest( data );
	    Vm.copyArray( digestdata, 0, data, 0, 20);
	    datapos = 0;
	  }
		shown = true;
      }
  }
	//===================================================================
  public byte[] generateSeed(int numBytes)
	//===================================================================
  {
    byte tmp[] = new byte[numBytes];
    nextBytes( tmp );
    return tmp;
  }
  /**
     Generates an integer containing the user specified
     number of random bits. It is right justified and padded
     with zeros.

     @param numBits number of random bits to get, 0 <= numBits <= 32;

     @return the random bits
   */
	//-------------------------------------------------------------------
  protected final int next(int numBits)
	//-------------------------------------------------------------------
  {
    if (numBits == 0)
      return 0;

    byte tmp[] = new byte[numBits / 8 + (1 * (numBits % 8))];
    nextBytes(tmp);
    int ret = 0;
    for (int i = 0; i < tmp.length; i++)
      ret |= (tmp[i] & 0xFF) << (8 * i);
    long mask = (1L << numBits) - 1;
    return (int) (ret & mask);
  }
	//-------------------------------------------------------------------
  private void ensureIsSeeded()
	//-------------------------------------------------------------------
  {
    if (!seeded)
      {
        new Random().nextBytes(seed);
        byte[] digestdata = digest.digest(data);
        Vm.copyArray(digestdata, 0, data, 0, 20);
        seeded = true;
      }
  }
}
