package ewex.locale;
import ewe.util.*;
import ewe.io.*;

//##################################################################
public class LocaleInfo{
//##################################################################

static
{
	ewe.sys.Vm.loadLibrary("ewex_locale");
}

public static final int LANGUAGE_ID           = 0x00000001; // language id
public static final int LANGUAGE              = 0x00000002; // localized name of language
public static final int ENGLANGUAGE           = 0x00001001; // English name of language
public static final int ABBREVLANGNAME        = 0x00000003; // abbreviated language name
public static final int NATIVELANGNAME        = 0x00000004; // native name of language

public static final int COUNTRY_ID            = 0x00000005; // country code
public static final int COUNTRY               = 0x00000006; // localized name of country
public static final int ENGCOUNTRY            = 0x00001002; // English name of country
public static final int ABBREVCTRYNAME        = 0x00000007; // abbreviated country name
public static final int NATIVECTRYNAME        = 0x00000008; // native name of country

//===================================================================
public native static int [] getAllIDs();
//===================================================================
public native static boolean supports(int ID);
//===================================================================
//-------------------------------------------------------------------
private static native String nativeGetInfo(int ID, int infoCode);
//-------------------------------------------------------------------

private int myID;

//===================================================================
private static int checkSupport(int id) throws IllegalArgumentException
//===================================================================
{
	if (!supports(id)) throw new IllegalArgumentException("Locale ID: "+id+" is not supported.");
	return id;
}
//===================================================================
public LocaleInfo(int id) throws IllegalArgumentException
//===================================================================
{
	setID(id);
}
//===================================================================
public String getInfo(int infoCode) throws IllegalArgumentException
//===================================================================
{
	return nativeGetInfo(myID, infoCode);
}

//===================================================================
public int getID()
//===================================================================
{
	return myID;
}

//===================================================================
public void setID(int id) throws IllegalArgumentException
//===================================================================
{
	myID = checkSupport(id);
}

/**
* This is the native Win32 definition of a Neutral locale ID.
**/
public static final int ID_NEUTRAL = 0;
/**
* This is the native Win32 definition of the user default locale ID.
**/
public static final int ID_USER_DEFAULT = 1024;
/**
* This is the native Win32 definition of the system default locale ID.
**/
public static final int ID_SYSTEM_DEFAULT = 2048;

//===================================================================
public String getTwoLetterLanguage()
//===================================================================
{
	String lang = getInfo(ABBREVLANGNAME);
	if (lang != null) lang = lang.substring(0,2);
	return lang;
}

//===================================================================
public String code()
//===================================================================
{
	return getTwoLetterLanguage()+"-"+getInfo(ABBREVCTRYNAME);
}
//===================================================================
public static void main(String args[]) throws IOException
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	ewe.sys.Vm.debug("Starting...");

	int [] got = getAllIDs();
	if (got == null) got = new int[0];
	ewe.sys.Vm.debug("Number of IDs: "+got.length);
	Vector all = new Vector();
	Hashtable ht = new Hashtable();
	for (int i = 0; i<got.length; i++){
		LocaleInfo li = new LocaleInfo(got[i]);
		if (true || li.getTwoLetterLanguage().equals("de")){
			LocaleData ld = new LocaleData(li,got[i]);
			ld.getData(li,ht);
			all.add(ld);
			//ewe.sys.Vm.debug(ld.textEncode());
			ewe.sys.Vm.debug("Length: "+ld.byteEncode().length);
		}
	}
	int tableSize = 8+all.size()*8;
	//
	// The first 2 bytes in the table denote the number of IDs.
	// The next 6 bytes are reserved.
	// Following that is a set of tuples (ID,Offset) each one is 4 bytes.
	// Then comes the data itself.
	//
	int offset = (all.size()+1)*8;
	byte [] table = new byte[offset];
	File out = new File("Locale.dat");
	RandomAccessStream ras = out.toRandomAccessStream("rw");
	ras.seek((long)0);
	try{
		ras.setLength(0);
	}catch(IOException e){}
	ras.write(table);

	Utils.writeInt(all.size(),table,0,2);
	int tableOffset = 8;
	for (int i = 0; i<all.size(); i++){
		LocaleData ld = (LocaleData)all.get(i);
		Utils.writeInt(ld.myID,table,tableOffset,4);
		Utils.writeInt(offset,table,tableOffset+4,4);
		tableOffset += 8;
		ld.offset = offset;
		ld.bytes = ld.byteEncode();
		offset += ld.bytes.length;
		ras.write(ld.bytes);
	}
	ras.seek((long)0);
	ras.write(table);
	ras.close();
	ewe.sys.Vm.debug("Done.");
}



//##################################################################
}
//##################################################################

//##################################################################
class LocaleData extends Vector{
//##################################################################

String langName;
IntArray values = new IntArray();
LocaleData base;
int offset;
int myID;
byte [] bytes;

//===================================================================
public int length()
//===================================================================
{
	return values.length;
}

//===================================================================
public LocaleData(LocaleInfo l,int id)
//===================================================================
{
	langName = l.getTwoLetterLanguage();
	myID = id;
}

//===================================================================
String getInfo(int id)
//===================================================================
{
	for (int i = 0; i<values.length; i++)
		if (values.data[i] == id)
			return (String)get(i);
	return null;
}
//-------------------------------------------------------------------
void getData(int id, LocaleInfo l, LocaleData stored)
//-------------------------------------------------------------------
{
	String got = l.getInfo(id);
	if (got != null && stored != null){
		String other = stored.getInfo(id);
		if (other != null)
			if (got.equals(other))
				got = null;
	}
	if (got != null) {
		values.append(id);
		add(got);
	}
}
//===================================================================
public void getData(LocaleInfo l, Hashtable stored)
//===================================================================
{
	LocaleData ld = (LocaleData)stored.get(langName);
	if (ld == null) stored.put(langName,this);
	else base = ld;
	for (int i = 0; i<0x58; i++){
		getData(i,l,ld);
		getData(i | 0x1000, l, ld);
	}
}
//===================================================================
public String textEncode()
//===================================================================
{
	String ret = "";
	for (int i = 0; i<values.length; i++){
		if (i != 0) ret += "|";
		ret += values.data[i]+"="+get(i);
	}
	return ret;
}
static byte [] buff = new byte[4];
//===================================================================
public byte [] byteEncode()
//===================================================================
{
	ByteArray ba = new ByteArray();
	Utils.writeInt(base == null ? 0 : base.offset, buff, 0, 4);
	ba.append(buff,0,4); //This will be a pointer to the location of the base locale.
	ba.append(buff,0,4); //This will hold the length of this locale not including this field and the previous field.
	for (int i = 0; i<values.length; i++){
		Utils.writeInt(values.data[i],buff,0,2);
		ba.append(buff,0,2);
		String toSave = get(i).toString();
		byte [] str = Utils.encodeJavaUtf8String(toSave);
		ba.append(str,0,str.length);
		buff[0] = 0;
		ba.append(buff,0,1);
	}
	Utils.writeInt(ba.length-8,ba.data,4,4);
	return ba.toBytes();
}
//===================================================================
public int hashCode() {return langName.hashCode();}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (other instanceof LocaleData) return langName.equals(((LocaleData)other).langName);
	return false;
}
//##################################################################
}
//##################################################################


