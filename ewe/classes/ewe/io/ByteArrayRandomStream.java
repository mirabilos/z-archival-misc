package ewe.io;
import ewe.util.ByteArray;

//##################################################################
public class ByteArrayRandomStream extends RandomStream{
//##################################################################
private MemoryFile mf;

//===================================================================
public ByteArrayRandomStream(int capacity)
//===================================================================
{
	ras = mf = new MemoryFile();
	if (capacity > 0) mf.data.data = new byte[capacity];
}
//===================================================================
public ByteArrayRandomStream()
//===================================================================
{
	this(1024);
}
//===================================================================
public ByteArrayRandomStream(ByteArray dataToUse,String mode)
//===================================================================
{
	ras = mf = new MemoryFile(dataToUse,mode);
}
//===================================================================
public byte[] toByteArray()
//===================================================================
{
	return mf.data.toBytes();
}
//===================================================================
public ByteArray toByteArray(ByteArray dest)
//===================================================================
{
	if (dest == null) dest = new ByteArray();
	dest.data = mf.data.data;
	dest.length = mf.data.length;
	return dest;
}
/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	ByteArrayRandomStream rs = new ByteArrayRandomStream();
	PrintWriter pw = new PrintWriter(rs.toWritableStream());
	pw.println("Hello there!\nDid this print correctly?");
	pw.print(1234);
	pw.flush();
	rs.setLength(10);
	new ewesoft.apps.HexView(rs.toRandomAccessStream()).execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

