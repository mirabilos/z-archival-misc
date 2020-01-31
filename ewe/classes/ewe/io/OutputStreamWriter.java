package ewe.io;
/**
* This is a thin wrapper around a TextWriter to provide compatibility with the commonly
* used java.io.OutputStreamWriter class.
**/
//##################################################################
public class OutputStreamWriter extends TextWriter{
//##################################################################
private String codecName;

//===================================================================
public OutputStreamWriter(OutputStream s,String codec) throws UnsupportedEncodingException
//===================================================================
{
	super(s);
	setCodec(codec);
}
//===================================================================
public OutputStreamWriter(OutputStream s)
//===================================================================
{
	super(s);
	try{
		setCodec(null);
	}catch(UnsupportedEncodingException e){}
}
//===================================================================
public OutputStreamWriter(BasicStream s,String codec) throws UnsupportedEncodingException
//===================================================================
{
	super(s);
	setCodec(codec);
}
//===================================================================
public OutputStreamWriter(BasicStream s)
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

