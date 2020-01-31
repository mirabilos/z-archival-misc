package ewe.zip;
import ewe.sys.Time;
import ewe.io.*;
import ewe.util.*;
import ewe.data.PropertyList;
//##################################################################
public class ZipEntryFile extends FileAdapter{
//##################################################################

public ZipEntry root;
public ZipEntry myEntry;
public ZipFile zipFile;
public String zipFileName = "Zip File";
//===================================================================
public File getNew(File parent,String file)
//===================================================================
{
	ZipEntryFile zf = new ZipEntryFile(zipFile);
	zf.set(parent,file);
	return zf;
}
//===================================================================
public ZipEntryFile(ZipFile zf){this(zf,null);}
//===================================================================
public ZipEntryFile(ZipFile zf,ewe.sys.Handle h)
//===================================================================
{
	zipFile = zf;
	zipFileName = zipFile.zipName;
	this.root = zf.getTree(h);
	if (h != null)
		h.set(root != null ? h.Succeeded : h.Failed);
}
public boolean isValid() {return root != null;}
public boolean exists() {return myEntry != null;}
public boolean isDirectory() {if (myEntry == null) return false; return myEntry.isDir;}
public int getLength()
{
	ZipEntry ze = myEntry;
	if (ze == null) return 0;
	if (ze.linkTo != null) ze = ze.linkTo;
	return ze.getSize();
}
//-------------------------------------------------------------------
protected  void getSetModified(Time time,boolean doGet)
//-------------------------------------------------------------------
{
	ZipEntry ze = myEntry;
	if (ze == null) {
		if (doGet) time.setTime(new Time().getTime());
	}else{
		if (ze.linkTo != null) ze = ze.linkTo;
		if (doGet) ze.getTime(time);
	}
}
boolean showed = false;
public void set(File parent,String file)
{
	name = file.replace('\\','/');
	if (parent instanceof ZipEntryFile) {
		root = ((ZipEntryFile)parent).root;
		String nm = ((ZipEntryFile)parent).name;
		if (nm == null) nm = "";
		name = mString.removeTrailingSlash(nm)+"/"+file;
	}
	if (name.length() == 0) name = "/";
	else {
		if (name.charAt(0) != '/') name = "/"+name;
		if (name.length() != 1)
			name = mString.removeTrailingSlash(name);
	}
	myEntry = null;
	if (name.equals("/")) myEntry = root;
	else{
		String [] got = mString.split(name,'/');
		ZipEntry cur = root;
		SubString mine = new SubString();
		for (int i = 1; i<got.length; i++){
			mine.set(got[i]);
			int num = cur.getChildCount();
			boolean found = false;
			ZipEntry child = null;
			for (int tn = 0; tn<num; tn++){
				child = (ZipEntry)cur.getChild(tn);
				SubString cs = child.myFileName;
				//ewe.sys.Vm.debug(cs.toString()+" = "+mine.toString());
				if (SubString.equals(cs.data,cs.start,cs.length,mine.data,mine.start,mine.length,SubString.IGNORE_CASE)){
					//ewe.sys.Vm.debug("Found!");
					cur = child;
					found = true;
					break;
				}
			}
			if (found) continue;
			cur = null;
			break;
		}
		myEntry = cur;
	}
}
int curFind = 0;
//-------------------------------------------------------------------
protected int startFind(String mask)
//-------------------------------------------------------------------
{
	curFind = 0;
	if (!isDirectory()) return 0;
	return 1;
}
//-------------------------------------------------------------------
protected Object findNext(int search)
//-------------------------------------------------------------------
{
	if (curFind >= myEntry.getChildCount()) return null;
	ZipEntry ze = (ZipEntry)myEntry.getChild(curFind++);
	return ze.myFileName.toString();
}

protected void endFind(int search){}

//-------------------------------------------------------------------

static ewe.fx.IImage zipIcon;

public boolean simulateCanWrite = false;

//-------------------------------------------------------------------
String [] getDetails(ZipEntry ze,String [] dest,Object source,boolean detailView)
//-------------------------------------------------------------------
{
	if (dest == null) dest = new String[3];
	PropertyList pl = source instanceof PropertyList ? (PropertyList)source : new PropertyList();
	Time t = getModified(new Time());
	ewe.sys.Locale l = (ewe.sys.Locale)pl.getValue("locale",ewe.sys.Vm.getLocale());
	dest[0] = "?";

	if (detailView) dest[0] = Utils.fileLengthDisplay(getLength());
	else dest[0] = l.format(l.FORMAT_PARSE_NUMBER,ewe.sys.Long.l1.set(getLength()),",");

	int cl = ze == null ? -1 : ze.getCompressedSize();
	if (cl != -1)
		if (detailView) dest[1] = Utils.fileLengthDisplay(cl);
		else dest[1] = l.format(l.FORMAT_PARSE_NUMBER,ewe.sys.Long.l1.set(cl),",");

	t.format = pl.getString("dateFormat",l.getString(l.LONG_DATE_FORMAT,0,0));
	dest[2] = t.toString();
	if (!detailView){
		t.format = pl.getString("timeFormat",l.getString(l.TIME_FORMAT,0,0));
		dest[2] += " "+t;
	}
	return dest;
}

//===================================================================
public Object getInfo(int value,Object source,Object resultDestination,int options)
//===================================================================
{
	try{
	ZipEntry ze = myEntry;
	if (ze != null)
		if (ze.linkTo != null)
			ze = ze.linkTo;
	switch(value){
			case INFO_DEVICE_NAME:
				return zipFileName;
			case INFO_DEVICE_ICON:
				if (zipIcon == null)
					zipIcon = new ewe.fx.mImage("ewe/zipsmall.bmp",ewe.fx.Color.White);
				return zipIcon;
			case INFO_FLAGS:
				if (!(resultDestination instanceof ewe.sys.Long))
					resultDestination = new ewe.sys.Long();
				((ewe.sys.Long)resultDestination).value =
				FLAG_CASE_SENSITIVE|FLAG_SLOW_LIST|(!simulateCanWrite ? FLAG_FILE_SYSTEM_IS_READ_ONLY : 0);
				return resultDestination;
			case INFO_TOOL_TIP:
				String ret = getFileExt();
				if (isDirectory()) return ret;
				String [] dest = getDetails(ze,null,source,false);
				ret += "\n"+dest[0]+" bytes\n"+dest[1]+" compressed\n"+dest[2];
				return ret;
			case INFO_DETAIL_NAMES:
				return mString.split("Length|Compr.|Date");
			case INFO_DETAIL_WIDTHS:
				PropertyList pl = source instanceof PropertyList ? (PropertyList)source : new PropertyList();
				ewe.sys.Locale l = (ewe.sys.Locale)pl.getValue("locale",ewe.sys.Vm.getLocale());
				String format = pl.getString("dateFormat",l.getString(l.LONG_DATE_FORMAT,0,0));
				return mString.split("LengthXX|Compr.XX|XX-XX-XXXX-");
			case INFO_DETAILS:
				return getDetails(ze,null,source,true);
	}
	}catch(Exception e){
		//e.printStackTrace();
	}
	return null;
}
//===================================================================
public Stream toReadableStream() throws IOException
//===================================================================
{
	Stream ret = null;
	if (myEntry != null)
		ret = zipFile.getInputStream(myEntry);
	if (ret != null) return ret;
	throw new IOException("Could not read: "+myEntry);
}
//##################################################################
}
//##################################################################


