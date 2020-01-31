package ewex.security;

//##################################################################
public class Security{
//##################################################################

private static boolean managerSet = false;
private static SecurityManager curManager = null;

//-------------------------------------------------------------------
private Security(){}
//-------------------------------------------------------------------

//===================================================================
public static void setSecurityManager(SecurityManager sm)
//===================================================================
{
	if (managerSet) throw new SecurityException("SecurityManager already set.");
	curManager = sm;
}
//===================================================================
public static SecurityManager getSecurityManager()
//===================================================================
{
	return curManager;
}
//##################################################################
}
//##################################################################

