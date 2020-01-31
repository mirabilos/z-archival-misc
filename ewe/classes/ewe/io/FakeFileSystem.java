/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.io;
import ewe.sys.Time;
import ewe.data.*;
import ewe.util.*;
/**
A FakeFileSystem is used to simulate a FileSystem, possibly starting from another read-only File
system. The FakeFileSystem provides a File object that can be used to browse and access the
FakeFileSystem using the File methods getNew(), getChild() and list().
<p>
Usually you would start the FakeFileSystem with entries found in another file storage system
like a ZipFile or a EweFile. Then the FakeFileSystem would allow data to be written into the
FakeFileSystem - but the data would be written into memory and not into the original system
(e.g. ZipFile).
<p>
For example, when a Ewe application is being run in a WebBrowser by a Java VM, upon startup
the VM looks for a file called "_filesystem.zip" on the server. If that file is found it is
used as the start of the FakeFileSystem and the default file system for the application then
becomes that FileSystem. Here is an excerpt from the source code that does this:
<p>
<pre>
	try{
		ewe.zip.ZipFile zf = new ewe.zip.ZipFile(ewe.sys.Vm.openRandomAccessStream("_filesystem.zip","r"));
		System.out.println("Loading virtual file system.");
		ewe.zip.ZipEntryFile zef = new ewe.zip.ZipEntryFile(zf);
		fileSystem = new ewe.io.FakeFileSystem();
		fileSystem.addVolume("Disk1",zef.getNew("/"));
		zf.close();
	}catch(IOException e){} //No virtual file system found.
</pre>
<p>
The addVolume() method is used to add a volume (simulated root drive) to the FakeFileSystem
and the method <b>getFile()</b> returns a File object that can be used to access the
FakeFileSystem.
**/
//##################################################################
public class FakeFileSystem extends LiveTreeNode{
//##################################################################

/**
* This is the name of the File system which by default is
* "Virtual File System". You can change this to be anything else.
**/
public String deviceName = "Virtual File System";

//===================================================================
public void addVolume(String systemName,File root)
//===================================================================
{
	if (systemName == null) systemName = "Disk1:";
	if (!systemName.endsWith(":")) systemName += ":";
	addChild(new FileNode().set(root,systemName,""));
}
//===================================================================
public File getFile()
//===================================================================
{
	if (getChildCount() == 0) addVolume(null,null);
	if (sorted == null) sortNames();
	return new FakeFile();
}
//===================================================================
public String defaultVolume()
//===================================================================
{
	int count = getChildCount();
	if (count == 0) return null;
	return getChild(0).toString();
}

public PropertyList unsortedNames;// = new PropertyList();
public PropertyList sorted;

Comparer stringComparer;

//-------------------------------------------------------------------
protected void sortNames()
//-------------------------------------------------------------------
{
	if (unsortedNames == null) return;
	Object [] all = new Object[unsortedNames.size()];
	unsortedNames.copyInto(all);
	stringComparer = ewe.sys.Vm.getLocale().getStringComparer(0);
	ewe.util.Utils.sort(all,new Comparer(){
		public int compare(Object one,Object two){
			return stringComparer.compare(((Property)one).name,((Property)two).name);
		}
	}
	,false);
	sorted = new PropertyList();
	sorted.addAll(all);
}

//-------------------------------------------------------------------
public int find(String name)
//-------------------------------------------------------------------
{
	int size = sorted.size();
	if (size < 1) return 0;
	int ul = size, ll = -1;
	while(true) {
		if (ul-ll <= 1) {
			//System.out.println(ul);
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		Property p = (Property)sorted.get(where);
		int cmp = stringComparer.compare(name,p.name);
		if (cmp > 0) ll = where;
		else ul = where;
	}
/*
	for (int i = 0; i<max; i++){
		int cmp = comparer.compare(searchData,get(i,buffer,data));
		if (flip) cmp = -cmp;
		if (cmp <= 0) return i;
	}
	return max;
*/
}

//===================================================================
public FileNode findNode(String name)
//===================================================================
{
	int val = find(name);
	if (val >= sorted.size()) return null;
	Property p = (Property)sorted.get(val);
	if (p.name.equals(name)) return (FileNode)p.value;
	return null;
}
	//##################################################################
	class FileNode extends LiveTreeNode{
	//##################################################################

	public String name = "unnamed";
	public String getName() {return name.endsWith(":") ? name+"/" : name;}
	public Time time = new Time();

	boolean isDirectory = false;
	ewe.util.ByteArray data;

	public int getLength()
	{
		if (data == null) return 0;
		return data.length;
	}

	int openReads = 0, openWrites = 0;

	RandomAccessStream getRandomStream(final int mode) throws IOException
	{
		if (openWrites != 0) throw new IOException("File already open for writing.");
		if (mode != File.READ_ONLY){
			if (openReads != 0) throw new IOException("File already open.");
			openWrites++;
		}else{
			openReads++;
		}
		MemoryFile mf = new MemoryFile(){
		{
			this.mode = mode;
		}
			public boolean closeStream() throws ewe.io.IOException{
				if (!closed){
					if (this.mode != File.READ_ONLY){
						if (openWrites > 0) openWrites--;
					}else if (openReads > 0) openReads--;
					//ewe.sys.Vm.debug("openReads: "+openReads+", openWrites: "+openWrites);
				}
				return super.closeStream();
			}
		};
		if (data == null) data = new ByteArray();
		mf.data = data;
		mf.mode = mode;
		return mf;
	}
	public FileNode set(File f,String asName,String parentName)
	{
		name = asName;
		if (f != null){
			f.getModified(time);
			if (name == null) name = f.getFileExt();
			isDirectory = f.isDirectory();
		}else
			isDirectory = true;
		if (unsortedNames != null) unsortedNames.add(parentName+name,this);
		if (f != null){
			if (isDirectory){
				String [] all = f.list();
				for (int i = 0; i<all.length; i++)
					addChild(new FileNode().set(f.getChild(all[i]),all[i],parentName+name+"/"));
			}else{
				MemoryFile mf = MemoryFile.createFrom(f.getInputStream(),null);
				if (mf != null) data = mf.data;
			}
		}
		return this;
	}
	public String toString() {return getName();}

	//##################################################################
	}
	//##################################################################

	//##################################################################
	class FakeFile extends FileAdapter{
	//##################################################################


	//-------------------------------------------------------------------
	FileNode findNode()
	//-------------------------------------------------------------------
	{
		String path = getFullPath();
		if (path.indexOf(':') == -1) {
			path = path.replace('\\','/');
			if (path.startsWith("/")) path = path.substring(1);
			path = defaultVolume()+path;
		}
		if (unsortedNames != null) return FakeFileSystem.this.findNode(path);
		int [] got = addressOfChild(FakeFileSystem.this,path);
		if (got == null) return null;
		TreeNode tn = getChildAt(got);
		if (tn instanceof FileNode) return (FileNode)tn;
		return null;
	}

	//===================================================================
	public boolean exists()
	//===================================================================
	{
		FileNode fn = findNode();
		if (fn == null) return false;
		return true;
	}

	//===================================================================
	public void set(File f,String name)
	//===================================================================
	{
		if (f == null) {
			this.name = name;
			if (name.length() != 1)
				this.name = mString.removeTrailingSlash(this.name);
		}else {
			this.name = f.getFullPath();
			this.name = mString.removeTrailingSlash(this.name);
			if (name != null)
				if (name.trim().length() != 0){
					this.name = mString.removeTrailingSlash(this.name);
					this.name += "/"+name;
				}
		}
		this.name = fixupPath(this.name);
	}
	//===================================================================
	public File getNew(File parent,String file)
	//===================================================================
	{
		File f = new FakeFile();
		f.set(parent,file);
		return f;
	}
	//===================================================================
	public boolean isDirectory()
	//===================================================================
	{
		FileNode fn = findNode();
		if (fn == null) return false;
		return fn.isDirectory;
	}
	int curFind = 0;
	FileNode me = null;
	//-------------------------------------------------------------------
	protected int startFind(String mask)
	//-------------------------------------------------------------------
	{
		curFind = 0;
		me = findNode();
		if (me == null) return 0;
		if (!me.isDirectory) return 0;
		return 1;
	}
	//-------------------------------------------------------------------
	protected Object findNext(int search)
	//-------------------------------------------------------------------
	{
		if (curFind >= me.getChildCount()) return null;
		return ((LiveTreeNode)me.getChild(curFind++)).getName();
	}
	protected void endFind(int search){me = null;}


	//===================================================================
	public int getLength()
	//===================================================================
	{
		FileNode fn = findNode();
		if (fn == null) return 0;
		return fn.getLength();
	}
	//===================================================================
	FileNode makeEntry(FileNode with)
	//===================================================================
	{
		FileNode fn = findNode();
		if (fn != null) return null;
		FakeFile ff = ((FakeFile)getParentFile());
		if (ff == null) return null;
		FileNode p = ff.findNode();
		if (p == null) return null;
		if (with != null){
			LiveTreeNode ltn = (LiveTreeNode)with.getParent();
			if (!(ltn instanceof FileNode)) return null;
			ltn.removeChild(with);
			p.addChild(with);
			with.name = getFileExt();
			return with;
		}else{
			fn = new FileNode();
			fn.name = getFileExt();
			fn.isDirectory = false;
			fn.data = new ByteArray();
			p.addChild(fn);
			return fn;
		}
	}

	//===================================================================
	public boolean createDir()
	//===================================================================
	{
		FileNode fn = makeEntry(null);
		if (fn == null) return false;
		fn.isDirectory = true;
		return true;
	}

	//===================================================================
	public boolean move(File newFile)
	//===================================================================
	{
		FileNode fn = findNode();
		if (fn == null) return false;
		return ((FakeFile)newFile).makeEntry(fn) != null;
	}

	//===================================================================
	public boolean delete()
	//===================================================================
	{
		FileNode fn = findNode();
		if (fn == null) return false;
		if (fn.getChildCount() != 0) return false;
		TreeNode p = fn.getParent();
		if (!(p instanceof FileNode)) return false;
		((FileNode)p).removeChild(fn);
		return true;
	}
	//-------------------------------------------------------------------
	protected  void getSetModified(Time time,boolean doGet)
	//-------------------------------------------------------------------
	{
		FileNode fn = findNode();
		if (fn == null) return;
		if (doGet) time.setTime(fn.time.getTime());
		else fn.time.setTime(time.getTime());
	}

	//===================================================================
	public Object getInfo(int value,Object source,Object resultDestination,int options)
	//===================================================================
	{
		int count = getChildCount();
		switch(value){
				case INFO_DEVICE_NAME:
					return deviceName;
				case INFO_PROGRAM_DIRECTORY:
					return defaultVolume();
				case INFO_ROOT_LIST:
					String [] all = new String[count];
					for (int i = 0; i<all.length; i++)
						all[i] = FakeFileSystem.this.getChild(i).toString();
					return all;
				case INFO_FLAGS:
					if (!(resultDestination instanceof ewe.sys.Long))
						resultDestination = new ewe.sys.Long();
					((ewe.sys.Long)resultDestination).value = FLAG_CASE_SENSITIVE|FLAG_SLOW_LIST;
					return resultDestination;
		}
		return null;
	}

	//===================================================================
	public RandomAccessStream toRandomAccessStream(String mode) throws IOException
	//===================================================================
	{
		int imode = convertMode(mode);
		FileNode node = findNode();
		if (node == null){
			if (imode == READ_ONLY) throw new IOException("Cannot read from: "+this);
			node = makeEntry(null);
			if (node == null) throw new IOException("Cannot write to: "+this);;
		}
		return node.getRandomStream(imode);
	}
	//##################################################################
	}
	//##################################################################

//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	FakeFileSystem ffs = new FakeFileSystem();
	try{
		ewe.zip.ZipFile zf = new ewe.zip.ZipFile(ewe.sys.Vm.openRandomAccessStream("_filesystem.zip","r"));
		ewe.ui.ProgressBarForm.display("Reading","Reading Virtual File System",null);
		ewe.zip.ZipEntryFile zef = new ewe.zip.ZipEntryFile(zf);
		ffs.addVolume("Disk1",zef.getNew("/"));
		ffs.addVolume("Disk2",null);
		zf.close();
	}catch(IOException e){ //No virtual file system found.
	}finally{
		ewe.ui.ProgressBarForm.clear();
	}
	ewe.sys.Vm.setFileObject(ffs.getFile());
	new ewe.filechooser.FileChooserDemo().run();
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################

