package ewex.security;

//##################################################################
public abstract class SecurityManager{
//##################################################################

public abstract void checkPermission(Permission p,Object context);

public abstract void checkPermission(Permission p);

//##################################################################
}
//##################################################################

