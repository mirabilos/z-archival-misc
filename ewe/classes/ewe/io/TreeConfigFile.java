/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
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
import ewe.sys.*;
import ewe.data.*;
import ewe.util.*;
//##################################################################
public class TreeConfigFile extends ewe.util.Errorable implements ewe.util.Comparable{
//##################################################################

public String configFileName = "";

TreeConfigNode root = new TreeConfigNode("/");

public static boolean cacheFullConfigFiles = false;

public static Vector configFiles = new Vector();


//===================================================================
public static TreeConfigFile getConfigFile(Stream stream,String uniqueName)
//===================================================================
{
	int idx = configFiles.find(uniqueName);
	if (idx != -1) return (TreeConfigFile)configFiles.get(idx);
	if (stream == null) return null;
	TreeConfigFile tcf = new TreeConfigFile();
	tcf.configFileName = uniqueName;
	boolean ok = tcf.decodeFrom(stream);
	stream.close();
	if (!ok) return null;
	if (cacheFullConfigFiles) configFiles.add(tcf);
	return tcf;
}
//===================================================================
public static TreeConfigFile getConfigFile(File file)
//===================================================================
{
	if (file == null) return null;
	String path = file.getFullPath();
	int idx = configFiles.find(path);
	if (idx != -1) return (TreeConfigFile)configFiles.get(idx);
	try{
		return getConfigFile(file.toReadableStream(),path);
	}catch(IOException e){
		return null;
	}
}
//===================================================================
public static TreeConfigFile getConfigFile(String path)
//===================================================================
{
	int idx = configFiles.find(path);
	if (idx != -1) return (TreeConfigFile)configFiles.get(idx);
	try{
		return getConfigFile(ewe.sys.Vm.openRandomAccessStream(path,"r"),path);
	}catch(IOException e){
		return null;
	}
}
/**
This is used by ewe.sys.Locale - use getConfigFile() instead.
**/
//===================================================================
public TreeConfigFile findOrMake(String path)
//===================================================================
{
	return getConfigFile(path);
}
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	return compareTo(other) == 0;
}
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (other instanceof String) return configFileName.compareTo((String)other);
	else if (other instanceof TreeConfigFile) return configFileName.compareTo(((TreeConfigFile)other).configFileName);
	return super.equals(other) ? 0 : 1;
}
//===================================================================
public TreeConfigFile()
//===================================================================
{
}

//===================================================================
public TreeConfigNode find(String name)
//===================================================================
{
	if (name == null) return null;
	if (name.startsWith("/")) name = name.substring(1);
	if (name.length() == 0) return root;
	int [] got = root.addressOfChild(root,name);
	if (got == null) return null;
	return (TreeConfigNode)root.getChildAt(got);
}
//===================================================================
public TreeNode getRoot(){return root;}
//===================================================================

//===================================================================
public boolean decodeFrom(Stream in)
//===================================================================
{
	StreamReader br = new StreamReader(in);
	if (!br.isOpen()) return returnError("Could not read from stream.",false);
	TreeConfigNode tcn = root;
	while(true){
		String got = br.readLine();
		if (got == null) break;
		got = got.trim();
		if (got.startsWith(";")) continue;
		if (got.startsWith("{..}")){
			if (tcn == root) break;
			else tcn = (TreeConfigNode)tcn.getParent();
			continue;
		}
		if (got.startsWith("{")){
			int idx = got.indexOf('}');
			if (idx == -1) idx = got.length();
			TreeConfigNode nn = new TreeConfigNode(got.substring(1,idx));
			tcn.addChild(nn);
			tcn = nn;
			continue;
		}
		int eq = got.indexOf('=');
		if (eq != -1){
			tcn.getProperties().add(got.substring(0,eq),ewe.util.TextDecoder.decode(ewe.sys.Vm.getStringChars(got),eq+1,got.length()-eq-1));
		}
	}
	in.close();
	return true;
}

//===================================================================
public LocalResource getLocalResourceObject(Locale forWho,String moduleName)
//===================================================================
{
	String language = forWho.getString(forWho.LANGUAGE_SHORT,0,0);
	String fullLanguage = language+"-"+forWho.getString(forWho.COUNTRY_SHORT,0,0);
	TreeConfigNode tcf = find(moduleName+"/LocalResources");
	if (tcf == null) return null;
	TreeConfigNode ret = (TreeConfigNode)LiveTreeNode.findNamedChild(tcf,fullLanguage);
	if (ret == null) ret = (TreeConfigNode)LiveTreeNode.findNamedChild(tcf,language);
	if (ret != null && !cacheFullConfigFiles) ret.setParent(null);
	return ret;
}

//##################################################################
}
//##################################################################

