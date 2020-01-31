package ewex.msencrypt;
import ewe.util.ByteArray;

//##################################################################
public class Encryptor implements ewe.io.DataProcessor{
//##################################################################
//========================= Don't move these!
protected int key;
protected int hash;
protected int provider;
protected String csp;
protected int keyOptions;
public byte [] password;
//=========================

/**
* Check this after calling the constructor to make sure it is valid.
**/
public boolean isValid = false;
/**
* This is a provider type.
**/
public static final int RSA_FULL = 0x1;
/**
* This is a provider type.
**/
public static final int RSA_SIG = 0x2;
/**
* This is a provider type.
**/
public static final int DSS = 0x3;
/**
* This is a provider type.
**/
public static final int DSS_DH = 0x4 ;
/**
* This is a provider type.
**/
public static final int FORTEZZA = 0x5;
/**
* This is a provider type.
**/
public static final int MS_EXCHANGE = 0x6 ;
/**
* This is a provider type.
**/
public static final int RSA_SCHANNEL = 0x7;
/**
* This is a provider type.
**/
public static final int SSL = 0x8;

public static final int ENCRYPTION_ALGORITHM_MASK  = 0xf0;
/**
* This is an encryption algorithm.
**/
public static final int ALG_DEFAULT_STREAM  = 0x00;
/**
* This is an encryption algorithm.
**/
public static final int ALG_DEFAULT_BLOCK  = 0x10;
/**
* This is an encryption algorithm.
**/
public static final int ALG_RC4  = 0x20;
/**
* This is an encryption algorithm.
**/
public static final int ALG_RC2  = 0x30;
/**
* This is an encryption algorithm.
**/
public static final int ALG_DES  = 0x40;

public static final int HASH_ALGORITHM_MASK = 0xf00;

/**
* This is a hashing algorithm.
**/
public static final int HASH_DEFAULT = 0x000;
/**
* This is a hashing algorithm.
**/
public static final int HASH_HMAC = 0x100;
/**
* This is a hashing algorithm.
**/
public static final int HASH_MD2  = 0x200;
/**
* This is a hashing algorithm.
**/
public static final int HASH_MD4  = 0x300;
/**
* This is a hashing algorithm.
**/
public static final int HASH_MD5  = 0x400;
/**
* This is a hashing algorithm.
**/
public static final int HASH_SHA1 = 0x500;
/**
* This is a hashing algorithm.
**/
public static final int HASH_MAC  = 0x600;
/**
* This is a hashing algorithm.
**/
public static final int HASH_NO_HASH = 0xf00;

static boolean didSetup = false;
//-------------------------------------------------------------------
static void encryptorSetup()
//-------------------------------------------------------------------
{
	if (!didSetup)
	ewe.sys.Vm.loadLibrary("ewex_msencrypt");
	didSetup = true;
}

static
{
	encryptorSetup();
}
/**
* Create an Encryptor using the default (currently RSA_FULL) provider with the default
* stream encryption and hashing algorithm (currently ALG_RC4 and HASH_SHA1).
*
* Check the isValid member to ensure that the encryptor is valid.
**/
//===================================================================
public Encryptor(String password)
//===================================================================
{
	this(ewe.util.mString.toBytes(password));
}
/**
* Create an Encryptor using the default (currently RSA_FULL) provider with the default
* stream encryption and hashing algorithm (currently ALG_RC4 and HASH_SHA1).
*
* Check the isValid member to ensure that the encryptor is valid.
**/
//===================================================================
public Encryptor(byte [] password)
//===================================================================
{
	this(null,0,password);
}
/**
* Create an Encryptor using the specified Cryptography Service Provider and
* specified provider types, crypto algorithm and hash algorithm.
* <p>
* The CSP can be null to use the default provider.
* <p>
* The keyTypeOptions should be the bitwise OR'ing of one of the provider types
* with one of the ALG_ crypto algorithm and one of the HASH_ hashing algorithm.
* Check the isValid member to ensure that the encryptor is valid.
**/
//===================================================================
public Encryptor(String CSP,int keyTypeOptions,String password)
//===================================================================
{
	this(CSP,keyTypeOptions,ewe.util.mString.toBytes(password));
}
/**
* Create an Encryptor using the specified Cryptography Service Provider and
* specified provider types, crypto algorithm and hash algorithm.
* <p>
* The CSP can be null to use the default provider.
* <p>
* The keyTypeOptions should be the bitwise OR'ing of one of the provider types
* with one of the ALG_ crypto algorithm and one of the HASH_ hashing algorithm.
* Check the isValid member to ensure that the encryptor is valid.
**/
//===================================================================
public Encryptor(String CSP,int keyTypeOptions,byte [] password)
//===================================================================
{
	csp = CSP;
	this.password = password;
	keyOptions = keyTypeOptions;
	isValid = msencryptorCreate();
}
/**
* This destroys the crypto object. Don't use it after calling this.
**/
//===================================================================
public void closeProcess() {msencryptorDestroy();}
//===================================================================
public int getBlockSize() {return msencryptorGetBlockSize();}
//===================================================================
public int getMaxBlockSize() {return 0;}
//===================================================================
public ByteArray processBlock(byte [] source,int offset,int length,boolean isLast,ByteArray output) throws ewe.io.IOException
//===================================================================
{
	if (output == null) output = new ByteArray();
	int needed = msencryptorGetProcessedBlockSize(isLast,length);
	if (output.data.length < needed) output.data = new byte[needed];
	output.length = 0;
	int out = msencryptorEncrypt(source,offset,length,isLast,output.data,needed);
	if (out < 0) throw new ewe.io.IOException("Encrypting error.");
	output.length = out;
	return output;
}

/**
* This will return null if index is greater than or equal to the number of providers.
**/
//===================================================================
public static String getProviderName(int index)
//===================================================================
{
	if (index < 0) return null;
	int sz = msencryptorGetProviderNameSize(index);
	if (sz <= 0) return null;
	char [] chars = new char[sz];
	msencryptorGetProviderName(index,chars);
	return new String(chars);
}
/**
* This returns the type of the specified provider.
**/
//===================================================================
public static int getProviderType(int index)
//===================================================================
{
	if (index < 0) return 0;
	return msencryptorGetProviderType(index);
}
private native static int msencryptorGetProviderNameSize(int index);
private native static int msencryptorGetProviderName(int index,char [] dest);
private native static int msencryptorGetProviderType(int index);
private native boolean msencryptorCreate();
private native void msencryptorDestroy();
private native int msencryptorGetBlockSize();
private native int msencryptorGetProcessedBlockSize(boolean isFinal,int length);
private native int msencryptorEncrypt(byte [] source,int offset,int length,boolean isFinal,byte [] dest,int destLength);

//##################################################################
}
//##################################################################


