package ewex.registry;
import ewe.data.*;
import ewe.util.Vector;

//##################################################################
public class ComputerKeyNode extends LiveTreeNode{
//##################################################################

public String name = "My Computer";

public ComputerKeyNode(int [] roots,boolean remote,boolean fullAccess,boolean create)
{
	if (remote) name = "Mobile Device";
	for (int i = 0; i<roots.length; i++){
		if (remote)
			addChild(new RegistryKeyNode(Registry.getRemoteKey(roots[i],"",fullAccess,create)));
		else
			addChild(new RegistryKeyNode(Registry.getLocalKey(roots[i],"",fullAccess,create)));
	}
}

public String getName() {return name;}

//##################################################################
}
//##################################################################

