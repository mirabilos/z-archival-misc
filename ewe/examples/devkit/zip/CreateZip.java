/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.zip;
//import ewe.util.zip.*;
//import ewe.io.*;
import java.io.*;
import java.util.zip.*;

//##################################################################
public class CreateZip{
//##################################################################
static byte [] buff = new byte[1024];
//===================================================================
public static void transfer(InputStream in, OutputStream out) throws IOException
//===================================================================
{
	while(true){
		int got = in.read(buff);
		if (got <= 0) return;
		out.write(buff,0,got);
	}
}
//===================================================================
public static void addEntry(String name,int method,File f,ZipOutputStream zos) throws IOException
//===================================================================
{
	ewe.sys.Vm.debug("Adding: "+f);
	ZipEntry ze = new ZipEntry(name);
	ze.setMethod(method);
	if (method == ZipEntry.STORED){ //Must set these for STORED entries.
		CRC32 crc = new CRC32();
		crc.reset();
		byte [] buff = new byte[1024];
		FileInputStream is = new FileInputStream(f);
		while(true){
			int got = is.read(buff,0,buff.length);
			if (got <= 0) break;
			crc.update(buff,0,got);
		}
		is.close();
		ze.setCrc(crc.getValue());
		ze.setSize(f.length());
	}
	zos.putNextEntry(ze);
	FileInputStream is = new FileInputStream(f);
	transfer(is,zos);
	//new IOTransfer().transfer(is,new StreamAdapter(zos));
	is.close();
}

//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("Sample.zip",false));
	addEntry("First.java",ZipEntry.DEFLATED,new File("samples/ui/ShowExec.java"),zos);
	addEntry("Second.java",ZipEntry.STORED,new File("samples/ui/Events.java"),zos);
	zos.finish();
	zos.close();

	zos = new ZipOutputStream(new FileOutputStream("Sample.zip",true));
	addEntry("Third.java",ZipEntry.DEFLATED,new File("samples/ui/TestPanel.java"),zos);
	zos.finish();
	zos.close();
	ewe.sys.Vm.debug("Done.");
	ewe.sys.mThread.nap(1000);
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################
