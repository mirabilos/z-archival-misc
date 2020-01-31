package ewe.security;
import ewe.io.OutputStream;
import ewe.io.DataProcessorStream;
/**
* This is an OutputStream that passes its data through a DataProcessorStream() which encrypts
* it using an Encryptor object.
**/
//##################################################################
public class EncryptedOutputStream extends OutputStream{
//##################################################################

//===================================================================
public EncryptedOutputStream(OutputStream out,String password)
//===================================================================
{
	super(new DataProcessorStream(new Encryptor(password),out.toWritableStream()));
}
//===================================================================
public EncryptedOutputStream(OutputStream out,byte[] password)
//===================================================================
{
	super(new DataProcessorStream(new Encryptor(password),out.toWritableStream()));
}

//##################################################################
}
//##################################################################

