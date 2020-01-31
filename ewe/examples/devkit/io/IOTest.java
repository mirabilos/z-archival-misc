package samples.io;
import ewe.io.*;
import ewe.util.*;
import ewe.sys.*;

//##################################################################
public class IOTest{
//##################################################################

//=================================================================
public static void main(String[] args) throws Exception
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	OperationTimer ot = new OperationTimer();
	ot.start("Reading");
	File f = new File("F:\\projects\\ifworld\\data\\storage\\equipment\\equipment.db");
	ByteArray ba = new ByteArray();
	if (args.length == 0){
		IO.readAllBytes(f,ba,true);
	}else{
		Handle h = StreamUtils.readAllBytes(new InputStream(f.toReadableStream()),ba);
		h.waitUntilStopped();
	}
	ot.end();
	ewe.sys.Vm.debug("Read in: "+ba.length);
	ewe.sys.Vm.debug(ot.toString());
	//ewe.sys.Vm.exit(0);
}


//##################################################################
}
//##################################################################
