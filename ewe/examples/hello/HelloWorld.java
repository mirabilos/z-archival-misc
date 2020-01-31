// $MirOS: contrib/hosted/ewe/examples/hello/HelloWorld.java,v 1.1.1.1 2007/08/30 20:57:27 tg Exp $

public class HelloWorld {

public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	new ewe.ui.MessageBox("Hello","Hello world!\nWelcome to the Ewe SDK",ewe.ui.Form.MBOK).execute();
	ewe.sys.Vm.exit(0);
}

}
