package ewex.registry;
import ewe.sys.*;
//##################################################################
public class RegistryLocalResource implements LocalResource{
//##################################################################


//===================================================================
public Object get(int id,Object defaultValue)
//===================================================================
{
	return get(ewe.sys.Convert.toString(id),defaultValue);
}
//===================================================================
public Object get(String resourceName,Object defaultValue)
//===================================================================
{
	Object got = myKey.getValue(resourceName);
	return (got == null ? defaultValue : got);
}

RegistryKey myKey;

//-------------------------------------------------------------------
RegistryLocalResource(RegistryKey key)
//-------------------------------------------------------------------
{
	myKey = key;
}
//===================================================================
public static LocalResource getLocalResourceObject(Locale forWho,String moduleName)
//===================================================================
{
	if (forWho == null || moduleName == null) return null;
	RegistryKey rk = Registry.getLocalKey(Registry.HKEY_LOCAL_MACHINE,"Software\\Ewesoft\\Applications\\LocalResources",false,false);
	if (rk == null) return null;
	String [] keys = (String [])rk.getSubKeys(0);
	if (keys == null) return null;
	for (int i = 0; i<keys.length; i++){
		if (moduleName.equals(keys[i])){
			rk = rk.getSubKey(moduleName);
			String value = (String)rk.getValue("ConfigFile");
			if (value != null){
				ewe.io.TreeConfigFile tcf = ewe.io.TreeConfigFile.getConfigFile(new ewe.io.File(value));
				if (tcf != null){
					LocalResource lr = tcf.getLocalResourceObject(forWho,moduleName);
					if (lr != null) return lr;
				}
			}
			keys = (String[]) rk.getSubKeys(0);
			if (keys == null) return null;
			String language = forWho.getString(forWho.LANGUAGE_SHORT,0,0);
			String fullLanguage = language+"-"+forWho.getString(forWho.COUNTRY_SHORT,0,0);
			for (int j = 0; j<keys.length; j++){
				if (fullLanguage.equals(keys[j]))
					return new RegistryLocalResource(rk.getSubKey(keys[j]));
			}
			for (int j = 0; j<keys.length; j++){
				if (language.equals(keys[j]))
					return new RegistryLocalResource(rk.getSubKey(keys[j]));
			}
			return null;
		}
	}
	return null;
}

//##################################################################
}
//##################################################################

