package ewex.registry;
import ewe.ui.*;
import ewe.reflect.*;
//import ewex.rapi.*;
//##################################################################
public class RegistryView extends Form{
//##################################################################

//===================================================================
public RegistryView()
//===================================================================
{
	//RegistryKey rk = Registry.getLocalKey(Registry.HKEY_LOCAL_MACHINE,"Software\\EweSoft\\Testing",true,true);
	//rk.setValue("First","Hello there!");
	setPreferredSize(640,480);
	boolean remote = false;
	if (mApp.programArguments.length >= 1){
		if (mApp.programArguments[0].equalsIgnoreCase("remote"))
			remote = true;
	}
	mApp.addFont(new ewe.fx.Font("Helvetica",ewe.fx.Font.PLAIN,16),"GUI");
	if (remote){
		Reflect r = Reflect.getForName("ewex.rapi.Rapi");
		if (r == null) exit(0);
		Method m = r.getMethod("initialize(LZ)Z",0);
		if (m == null) exit(0);
		Wrapper w = m.invoke(null,new Wrapper[]{new Wrapper().setInt(5000),new Wrapper().setBoolean(true)},new Wrapper());
		if (w == null) exit(0);
		if (!w.getBoolean()) exit(0);
		//if (!Rapi.initialize(5000,true)) exit(0);
	}
	Editor ed = new RegistryViewForm(remote);
	ed.hasTopBar = false;
	addLast(ed);
	title = ed.title;
}
//===================================================================
public void formShown()
//===================================================================
{

	super.formShown();
	/*
	if (!Registry.isInitialized(false))
		new MessageBox("Initialization Error","The ewex_registry.dll could not load.\nRegistry access is not possible.",MBOK).exec();
	*/
}
//##################################################################
}
//##################################################################

