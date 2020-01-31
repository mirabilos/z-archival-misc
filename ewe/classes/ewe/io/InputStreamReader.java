package ewe.io;
/**
* This is a thin wrapper around a TextReader to provide compatibility with the commonly
* used java.io.InputStreamReader class.
**/
//##################################################################
public class InputStreamReader extends TextReader{
//##################################################################
private String codecName;

//===================================================================
public InputStreamReader(InputStream s,String codec) throws UnsupportedEncodingException
//===================================================================
{
	super(s);
	setCodec(codec);
}
//===================================================================
public InputStreamReader(InputStream s)
//===================================================================
{
	super(s);
	try{
		setCodec(null);
	}catch(UnsupportedEncodingException e){}
}
//===================================================================
public InputStreamReader(BasicStream s,String codec) throws UnsupportedEncodingException
//===================================================================
{
	super(s);
	setCodec(codec);
}
//===================================================================
public InputStreamReader(BasicStream s)
//===================================================================
{
	super(s);
	try{
		setCodec(null);
	}catch(UnsupportedEncodingException e){}
}
//===================================================================
public String getEncoding()
//===================================================================
{
	return codecName;
}
//-------------------------------------------------------------------
protected void setCodec(String codec) throws UnsupportedEncodingException
//-------------------------------------------------------------------
{
	if (codec == null) codec = IO.JAVA_UTF8_CODEC;
	codecName = codec;
	this.codec = IO.getCodec(codec);
	if (this.codec == null) throw new UnsupportedEncodingException(codec);
}
//##################################################################
}
//##################################################################

