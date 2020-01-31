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
import ewe.ui.*;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;

//##################################################################
public class FileChooserFolderChoice extends mChoice{
//##################################################################

{
	useMenuItems = true;
	alwaysDrop = true;
	indentDropItems = true;
	prompt = "Choose Folder";
	menuOptions |= MENU_FULL_WIDTH;
	menuOptions |= MENU_SHOW_TITLE_IF_EXPANDED;
	dontAllowKeyChangeChoice = true;
}

private Vector levels = new Vector();
//===================================================================
public void updateMenu(File newCurrentDirectory, String[] roots, Vector fileChooserLinks)
//===================================================================
{
	File afile = newCurrentDirectory;
	Vector v = new Vector();
	for(File p = newCurrentDirectory; p != null; p = p.getParentFile()){
		String s = p.getFileExt();
		if (!s.equals("."))
			v.add(0,s);
	}
	removeAll();
	levels.clear();
	File cur = null;
	FontMetrics fm = getMenuFontMetrics();
	int start = 0;
	Vector links = fileChooserLinks;
	if (links != null){
		start = links.size();
		for (int i = 0; i<start; i++){
			FileChooserLink l = (FileChooserLink)links.get(i);
			MenuItem mi = l.addToMenu(this,fm);
			mi.indentLevel = 0;
			levels.add(afile.getNew(l.path));
		}
	}
	String [] all = roots;
	String cd = (String)v.get(0);
	if (all == null) all = new String[]{cd};
	cd = mString.leftOf(cd,':');
	boolean did = false;
	int sel = 0;
	for (int r = 0; r<all.length; r++){
		if (!did && mString.leftOf(all[r],':').equalsIgnoreCase(cd)){
			did = true;
			int sz = v.size();
			for (int i = 0; i<sz; i++) {
				String dir = (String)v.get(i);
				levels.add(cur = afile.getNew(cur,dir));
				boolean isDrive = i == 0 ? dir.indexOf(':') != -1 : false;
				if (isDrive) dir = dir.toUpperCase();
				MenuItem mi = addItem(dir);
				mi.image = new IconAndText(File.getIcon(isDrive ? File.DriveIcon : File.OpenFolderIcon),dir,fm).setColor(null,null);
				mi.indentLevel = i;
			}
			sel = sz+r-1+start;
		}else{
			levels.add(afile.getNew(all[r]));
			//if (all[r].indexOf(':') != -1) all[r] = all[r].toUpperCase();
			MenuItem mi = addItem(all[r]);
			mi.image = new IconAndText(File.getIcon(File.DriveIcon),all[r],fm).setColor(null,null);
			mi.indentLevel = 0;
		}
	}
	select(sel);
}

//===================================================================
public File getSelectedFolder()
//===================================================================
{
	int idx = selectedIndex;
	if (idx == -1) return null;
	return (File)levels.get(idx);
}
//##################################################################
}
//##################################################################

