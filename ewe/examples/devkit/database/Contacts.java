package samples.database;
import ewe.database.RemoteSynchronizer;
import ewe.io.IOException;
import ewe.io.RemoteConnection;
import ewe.sys.TimedOutException;
import ewex.registry.Registry;

//##################################################################
public class Contacts{
//##################################################################

//===================================================================
public static void sync()
throws IOException, TimedOutException, InterruptedException
//===================================================================
{
	ewe.ui.ProgressBarForm.display("Conctact Synchronizer",ewe.sys.Vm.isMobile() ? "Contacting desktop..." : "Contacting mobile device...",null);
	RemoteSynchronizer rs = null;
	try{
		rs = RemoteSynchronizer.synchronizeOnRemoteConnection("Contacts",
		"HKEY_LOCAL_MACHINE\\Software\\EweSoft\\Contacts\\Sync"
		//"\"/Windows/Start Menu/Programs/Ewe/Contacts.ewe\" sync"
		,new ewe.sys.TimeOut(30*1000),
		ewe.io.RemoteConnection.MOBILE_IS_EWE_APPLICATION
		|ewe.io.RemoteConnection.MOBILE_GET_COMMAND_FROM_REGISTRY
		|ewe.io.RemoteConnection.MANUAL_MOBILE_START_ON_EMULATOR
		);
	}finally{
		ewe.ui.ProgressBarForm.clear();
	}
	ewe.sys.Handle h = rs.synchronize("Synchronizing Contacts","The desktop is synchronizing");
	h.waitUntilStopped();
	ewe.sys.Vm.debug("h has Stopped!");
	ewe.sys.mThread.nap(5000);
	if (h.errorObject instanceof IOException)
		throw (IOException)h.errorObject;
	else if (h.errorObject instanceof TimedOutException)
		throw (TimedOutException)h.errorObject;
	else if (h.errorObject instanceof Throwable)
		throw new IOException(((Throwable)h.errorObject).getMessage());
}

//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
//
String syncKey = "HKEY_LOCAL_MACHINE\\Software\\EweSoft\\Contacts\\Sync";
//
if (args.length == 0){
	try{
		String commandLine = Registry.getEweCommandLineToMe("sync",null,false);
		Registry.getLocalKey(0,syncKey,true,true).setValue(null,commandLine);
		if (!ewe.sys.Vm.isMobile())
			RemoteConnection.registerAutoStartEweApp("Ewesoft Contacts Synchronizer",commandLine);
	}catch(Exception e){
		//Could not register.
	}
}

	try{
		if (args.length > 0 && args[0].equals("sync")){
			sync();
		}else{
			ewe.sys.Vm.debug("PD: "+ewe.io.File.getProgramDirectory());
			ContactTester.runTest(args);
		}
	}catch(Exception e){
		new ewe.ui.ReportException(e,null,null,false).execute();
	}
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
