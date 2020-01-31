package ewex.rapi;
import ewe.io.*;
import ewe.sys.Time;
import ewe.ui.*;
import ewe.filechooser.*;

//##################################################################
public class RapiFileBrowser extends ewe.filechooser.FileChooserDemo{
//##################################################################

public RapiFileBrowser()
{
	aFile = new RapiFile("/");
}
public void run()
{
	if (!Rapi.initialize(3000,true)) return;
	super.run();
}
public static void main(String [] args)
{
	ewe.sys.Vm.startEwe(args);
	new RapiFileBrowser().run();
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################

