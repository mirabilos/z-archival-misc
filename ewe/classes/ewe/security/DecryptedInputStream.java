package ewe.security;
import ewe.io.InputStream;
import ewe.io.DataProcessorStream;
/**
* This is an InputStream that passes its data through a DataProcessorStream() which decrypts
* it using a Decryptor object.
**/
//##################################################################
public class DecryptedInputStream extends InputStream{
//##################################################################

//===================================================================
public DecryptedInputStream(InputStream in,String password)
//===================================================================
{
	super(new DataProcessorStream(new Decryptor(password),in.toReadableStream()));
}
//===================================================================
public DecryptedInputStream(InputStream in,byte[] password)
//===================================================================
{
	super(new DataProcessorStream(new Decryptor(password),in.toReadableStream()));
}

//##################################################################
}
//##################################################################

