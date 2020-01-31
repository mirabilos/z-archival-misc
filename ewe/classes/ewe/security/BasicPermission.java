package ewe.security;
/**
* This class is not used in the Ewe library but is provided for compatibility
* with other external libraries.
**/
//##################################################################
public class BasicPermission{
//##################################################################

String name;
String actions;

  public BasicPermission(String name)
  {
    this.name = name;
  }

  public BasicPermission(String name, String actions)
  {
    this.name = name;
		this.actions = actions;
  }

//##################################################################
}
//##################################################################

