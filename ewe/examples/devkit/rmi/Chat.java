package samples.rmi;

//##################################################################
public class Chat{
//##################################################################

//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	boolean ars = ChatServerObject.amRunningSocketServer;
	ChatTerminal got =  ars ?
		ChatTerminal.join(ChatServerObject.localServer,new ChatTerminal()) :
		ChatTerminal.join(null);
	if (got == null) ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################
