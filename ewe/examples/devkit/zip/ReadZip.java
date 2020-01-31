/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.zip;
//import ewe.util.zip.*;
//import ewe.io.*;
import java.util.zip.*;
import java.io.*;
import java.util.Enumeration;
//##################################################################
public class ReadZip{
//##################################################################

//===================================================================
public static void output(String what)
//===================================================================
{
	System.out.println(what);
}
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	ZipInputStream zis = new ZipInputStream(new FileInputStream("Sample.zip"));
	while(true){
		ZipEntry ze = zis.getNextEntry();
		if (ze == null) break;
		byte [] buff = new byte[512];
		int total = 0;
		while(true){
			int got = zis.read(buff,0,buff.length);
			if (got <= 0) break;
			total += got;
		}
		output(ze.getName()+" = "+total+" bytes.");
	}
	zis.close();

	ZipFile zf = new ZipFile("Sample.zip");
	Enumeration it = zf.entries();
	while(it.hasMoreElements()){
		ZipEntry ze = (ZipEntry)it.nextElement();
		output(ze.getName()+" Uncompressed:"+ze.getSize()+" Compressed:"+ze.getCompressedSize());
	}
	//ewe.sys.mThread.nap(3000);
	//ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
