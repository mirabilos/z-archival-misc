package ewe.database;
/**
 * @deprecated - use ewe.security.Decryptor instead.
 */
//##################################################################
public class Decryptor extends Encryptor {
//##################################################################

//===================================================================
public Decryptor(String key)
//===================================================================
{
	super(key);
}
//===================================================================
public Decryptor()
//===================================================================
{

}
//-------------------------------------------------------------------
byte [] doTheProcess(long key,byte [] theBytes)
//-------------------------------------------------------------------
{
	return decrypt(key,theBytes);
}
//##################################################################
}
//##################################################################
