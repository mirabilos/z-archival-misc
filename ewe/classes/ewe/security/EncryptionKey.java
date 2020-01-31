package ewe.security;
/**
* An EncryptionKey is a class that can encrypt or decrypt a short block of data. It is
* usually implemented by Public/Private Key pairs (e.g. RSAKey).
**/
//##################################################################
public interface EncryptionKey{
//##################################################################

//===================================================================
public byte[] encrypt(byte[] source,int offset,int length) throws ewe.io.IOException;
//===================================================================
//===================================================================
public byte[] decrypt(byte[] source,int offset,int length) throws ewe.io.IOException;
//===================================================================

//##################################################################
}
//##################################################################

