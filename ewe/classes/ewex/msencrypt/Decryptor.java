package ewex.msencrypt;
import ewe.util.ByteArray;

//##################################################################
public class Decryptor extends Encryptor{
//##################################################################

//===================================================================
public Decryptor(String password) {super(password);}
public Decryptor(byte [] password) {super(password);}
//===================================================================

//===================================================================
public Decryptor(String CSP,int keyTypeOptions,String password)
//===================================================================
{
	this(CSP,keyTypeOptions,ewe.util.mString.toBytes(password));
}
//===================================================================
public Decryptor(String CSP,int keyTypeOptions,byte [] password)
//===================================================================
{
	super(CSP,keyTypeOptions,password);
}
//===================================================================
public ByteArray processBlock(byte [] source,int offset,int length,boolean isLast,ByteArray output) throws ewe.io.IOException
//===================================================================
{
	//ewe.sys.Vm.debug("Decrypting!",0);
	if (output == null) output = new ByteArray();
	int needed = msdecryptorGetProcessedBlockSize(isLast,length);
	if (output.data.length < needed) output.data = new byte[needed];
	output.length = 0;
	int out = msdecryptorDecrypt(source,offset,length,isLast,output.data,needed);
	if (out < 0) throw new ewe.io.IOException("Decoding error");
	output.length = out;
	return output;
}

private native int msdecryptorGetProcessedBlockSize(boolean isFinal,int length);
private native int msdecryptorDecrypt(byte [] source,int offset,int length,boolean isFinal,byte [] dest,int destLength);

//##################################################################
}
//##################################################################

