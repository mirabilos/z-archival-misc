package samples.database;
import ewe.database.Database;
import ewe.database.DoubleSynchronizer;
import ewe.database.RecordFile;
import ewe.io.File;
import ewe.sys.Handle;

//##################################################################
public class SynchronizeTwo{
//##################################################################

//=================================================================
public static void main(String[] args) throws Exception
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Database local = new RecordFile(new File(args[0]),"rw");
	Database remote = new RecordFile(new File(args[1]),"rw");
	DoubleSynchronizer ds = new DoubleSynchronizer(local,remote);
	Handle h = ds.synchronize("Synchronizing");
	h.waitUntilStopped();
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
