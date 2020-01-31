package ewe.io;
/**
* This is a Stream that you can use if you have a String that you need to
* read from as a Stream, or if you need to write to a Stream and then
* have the data you wrote converted into a String.<p>
* You would probably be better off using a StringWriter instead.<p>
*
**/
//##################################################################
public class StringStream extends MemoryFile{
//##################################################################
/**
 * Create a new empty StringStream() ready for writing to.
 */
//===================================================================
public StringStream()
//===================================================================
{
	this(null,"rw");
}
/**
 * Create a new StringStream() for reading only, initially containing the specified text.
* @param text The text initially stored in the stream.
*/
//===================================================================
public StringStream(String text)
//===================================================================
{
	this(text,"r");
}

/**
 * Create a new StringStream() which initially contains text and which can be read from
 * and/or written to.
* @param textData The initial text, encoded data as bytes.
* @param start The start of the text in textData.
* @param length The number of bytes to use from textData.
* @param mode either "r" or "rw".
*/
//===================================================================
public StringStream(byte[] textData, int start, int length, String mode)
//===================================================================
{
	super(textData,start,length,mode);
}
/**
 * Create a new StringStream() which initially contains text and which can be read from
 * and/or written to.
* @param text The initial text in the stream. This will be UTF8 encoded and stored as bytes in the Stream.
* @param mode either "r" or "rw".
*/
//===================================================================
public StringStream(String text,String mode)
//===================================================================
{
	super(new byte[0],0,0,mode);
	if (text != null) {
		byte [] t = ewe.util.Utils.encodeJavaUtf8String(text);
		data = new ewe.util.ByteArray(t);
	}
}
/**
* Return as a String, all the text data that was written into the Stream.
**/
//===================================================================
public String toString()
//===================================================================
{
	return ewe.util.Utils.decodeJavaUtf8String(data.data,0,data.length);
}
/*
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	//
	//Create an empty StringStream for writing.
	//
	StringStream ss = new StringStream();
	//
	//Use StreamWriter for easy line by line writing.
	//
	StreamWriter sw = new StreamWriter(ss);
	sw.println("Hello there!");
	sw.println("How are you?");
	sw.close();
	//
	// Now see what is stored in the StringStream by calling toString().
	//
	ewe.sys.Vm.debug("I got: \n"+ss.toString()+"-----------");
	//
	// Now create a StringStream for reading.
	//
	ss = new StringStream("This is preset data.\nThere are three lines\nIn this file.\n");
	//
	// Use StreamReader for easy line by line reading.
	//
	StreamReader sr = new StreamReader(ss);
	while(true){
		String line = sr.readLine();
		if (line == null) break;
		ewe.sys.Vm.debug("Line: "+line);
	}
	sr.close();
	ewe.sys.Vm.debug("-----------");
}
*/
//##################################################################
}
//##################################################################

