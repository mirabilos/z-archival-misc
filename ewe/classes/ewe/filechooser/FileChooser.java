/* $MirOS: contrib/hosted/ewe/classes/ewe/filechooser/FileChooser.java,v 1.2 2008/04/10 19:34:57 tg Exp $ */

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
package ewe.filechooser;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.io.File;
import ewe.io.FileChooserParameters;
import ewe.io.FileChooserLink;
import ewe.sys.Vm;
import ewe.sys.VMOptions;
import ewe.sys.Time;
import ewe.sys.Locale;
import ewe.data.*;
import ewe.reflect.Type;
/**
* The FileChooser class is used to choose files or a directory on a file system. It provides the
* following features:
* <ul>
* <li>You can assign custom icons to file types.
* <li>You can set multiple file types using standard file masks (e.g. *.txt).
* <li>You can set a default file type.
* <li>The FileChooser will browse any file system that provides a File object. This includes Zip files, Ewe files
* and the FakeFileSystem (which maintains a file system in memory).
* <li>You can extend FileChooser to add custom controls (e.g. the ImageFileChooser).
* </ul>
* The simplest way to use a FileChooser is to construct it with the desired type and directory, and then
* call one of the execute() methods. If execute() returns IDCANCEL, then the user canceled the operation. If
* the user did not cancel, then you can use getChosen() or getChosenFile() to get the file chosen.
* <pre>
* FileChooser fc = new FileChooser(FileChooser.SAVE,null);
* fc.title = "Save Your Data Please...";
* if (fc.execute() == fc.IDCANCEL) return null;
* File toSaveTo = fc.getChosenFile();
* return toSaveTo;
* </pre>
* You can also allow MULTI_SELECT with the OPEN type to allow multiple file selection. In this case, you
* should use the chosenFiles Vector variable along with the chosenDirectory variable after execute() returns
* to determine the files chosen.
* <p>
**/

//##################################################################
public class FileChooser extends FileChooserBase{
//##################################################################

/**
* Use this to get the selected file - do NOT use the "file" variable. If multiple files are selected this will return the first one. Otherwise
* it will return the full path of the selected file.
**/
//===================================================================
public String getChosen()
//===================================================================
{
	File got = getChosenFile();
	if (got == null) return null;
	return got.getFullPath();
}
/**
* Get all the chosen file names - even if only a single file was selected. Each item
* in the array is the file name only, without the directory path the selected item is in.
* Use getChosenDirectory() to get the directory the selected items are in.
**/
//===================================================================
public String [] getAllChosen()
//===================================================================
{
	if (chosenDirectory == null || chosenFiles == null) return new String[0];
	String [] all = new String[chosenFiles.size()];
	chosenFiles.copyInto(all);
	return all;
}
/**
 * Get the directory the chosen file(s) is in.
 */
//===================================================================
public File getChosenDirectory()
//===================================================================
{
	if (chosenDirectory == null) return null;
	File got = afile.getNew(chosenDirectory);
	if (got == null) return null;
	if (!got.isDirectory()) return null;
	return got;
}
/**
* Get all chosen files, even if only one file was selected.
**/
//===================================================================
public File [] getAllChosenFiles()
//===================================================================
{
	String [] ch = getAllChosen();
	File d = getChosenDirectory();
	if (d == null) return new File[0];
	File [] ret = new File[ch.length];
	for (int i = 0; i<ret.length; i++)
		ret[i] = d.getChild(ch[i]);
	return ret;
}
/**
* Use this to get the selected file - do NOT use the "file" variable. If multiple files are selected this will return the first one. Otherwise
* it will return the full path of the selected file.
**/
//===================================================================
public File getChosenFile()
//===================================================================
{
	if (chosenDirectory == null || chosenFiles == null) return null;
	if (chosenFiles.size() < 1) return null;
	String cf = (String)chosenFiles.get(0);
	if (chosenDirectory.equals("/") && (cf.indexOf('\\') != -1 || cf.indexOf('/') != -1))
		return afile.getNew(cf);
	return afile.getNew(chosenDirectory).getChild(cf);
}
/**
* Do NOT use this to variable. Use getChosen() or getChosenFile() to get the file that the user selected.
**/
public String file = "\\";
/**
* Do NOT use this variable. Use defaultExtension to set a default extention for a file.
**/
public String fileType = "";
/**
* If no extension is given then this will be appended on to the selected file. By default this is null. When you
* set it, do not put the '.'
**/
public String defaultExtension = null;

mList list = new mList(10,10,false);
/**
* This is the table (list) of files/directory being displayed.
**/
public TableControl table;
/**
* This is the tree of directory being displayed.
**/
public TreeControl tree;
/**
* This is the FileListTableModel used by the table.
**/
public FileListTableModel files;
/**
* This is used internally.
**/
public Time time = new Time();

protected CellPanel dirNavigation;
/**
* Set this to an application specific name (e.g. "Ewesoft-Jewel") to keep and
* retrieve a persistent history for the FileChooser.
**/
public String persistentHistoryKey;

mChoice directories, historyList;
ButtonPullDownMenu roots = null;//new ButtonPullDownMenu("Drives",new Menu()),
ButtonPullDownMenu toolMenu;
String [] sroots;
int type = 2;
mButton accept;
mChoice maskChoices;

/**
* Put any extra controls in this panel.
**/
public CellPanel extraPanel;

/**
* These are links for the FileChooser - each of which must be of type FileChooserLink. It will initially be null, unless there are
* links inside of globalLinks. In that case a new Vector is created and all the links inside of globalLinks will
* be added to it. You can call addLink() to add links to it safely.
**/
public Vector links;

{
	dismantleOnClose = true;
}

//===================================================================
public void addLink(FileChooserLink link)
//===================================================================
{
	if (link != null)
		links = Vector.add(links,link);
}
//-------------------------------------------------------------------
private static void setupIcons(String icons,Color background)
//-------------------------------------------------------------------

{
	String all[] = mString.split(icons,'|');
	for (int i = 0; i<all.length-1; i+= 2){
		String ext [] = mString.split(all[i],';');
		String img = "ewe/"+all[i+1];
		if (!img.endsWith(".png")) img += ".bmp";
		try{
			IImage ii = new mImage(img,background);
			for (int j = 0; j<ext.length; j++)
				associateIcon(ext[j],ii);
		}catch(Exception e){}
	}
}

private static String newItems = "Folder|File";
static {
	setupIcons("ewe|ewesmall|exe|programsmall|zip|zipsmall",Color.White);
	setupIcons("bmp;png;jpg;jpeg;gif;ico;icon|imagesmall|txt;doc;lst|textsmall",new Color(0,255,0));
	setupIcons("htm;html|websmall.png",null);
	try{
		if (ewe.reflect.Reflect.getForName("ewex.registry.Registry") != null)
			newItems += "|Shortcut";
	}catch(Exception e){

	}
}

/**
* This associates an icon with the specified file extension. Do not place the leading '.' in the
* extension. e.g. associateIcon("exe",new mImage("myicon/programicon.bmp",Color.White));
* @param extension The extension WITHOUT the leading '.'
* @param icon The icon to be displayed. Should be a 16x16 icon.
*/
//===================================================================
public static void associateIcon(String extension,IImage icon)
//===================================================================
{
	int idx = extension.indexOf('.');
	if (idx != -1) associateIcon(extension.substring(idx+1),icon);
	else{
		extensions.add(extension.toCharArray());
		icons.add(icon);
	}
}

static String baseDir = File.getProgramDirectory();

/**
 * Get the associated icon for a file name.
 * @param fileName The file.
 * @return The icon associated with the file.
 */
//===================================================================
public static IImage getIconForFile(String fileName)
//===================================================================
{
	if (fileName == readingDirectory[0]) return ImageCache.cache.get("ewe/WaitSmall.bmp",Color.White);
	if (extensions.size() == 0) return TreeTableModel.page;
	int idx = fileName.lastIndexOf('.');
	if (idx != -1){
		Locale l = ewe.sys.Vm.getLocale();
		char [] look = ewe.sys.Vm.getStringChars(fileName);
		int offset = idx+1;
		int length = fileName.length()-offset;
		for (int i = 0; i<extensions.size(); i++){
			char [] got = (char [])extensions.get(i);
			int c = l.compare(look,offset,length,got,0,got.length,Locale.IGNORE_CASE);
			if (c == 0) return (IImage)icons.get(i);
		}
	}
	return TreeTableModel.page;
}
String [] viewChoices, sortChoices, newChoices;

/**
* This is a copy of the File model being used by the FileChooser. You should not set this, but you can
* reference it if you need to.

**/
public File afile;
/**
* This indicates that access to the file system will be slow.
**/
public boolean slowAccess = false;
/**
* This indicates that the wait cursor should be shown when listing is being done.
**/
public boolean showWait = false;


boolean hasFileList = true;
boolean masksSet = false;
/**
Set this true to initially show the FileChooser in list mode.
**/
public static boolean listModeDefault = false;

/**
* Do not set this directly.
**/
public boolean iconMode = true;
/**
* Do not set this directly.
**/
public boolean listMode = listModeDefault;
/**
* Do not set this directly.
**/
public boolean detailMode = false;
/**
* Do not set this directly.
**/
public boolean noKeyboard = false;
/**
* Do not set this directly.
**/
public boolean noMouse = false;
//-------------------------------------------------------------------
protected CellPanel getViewToolBar(String iconsAndFieldsAndTips)
//-------------------------------------------------------------------
{
	if (toolButtons == null) toolButtons = new ImageCache();
	CellPanel cp = new CellPanel();
	String all[] = mString.split(iconsAndFieldsAndTips);
	CheckBoxGroup group = new CheckBoxGroup();
	Color red = new Color(255,0,0);
	for (int i = 0; i<all.length-2; i += 3){
		mCheckBox cb = new ButtonCheckBox(toolButtons.get("ewe/"+all[i]+".bmp",red));
		if (all.length > 3) cb.setGroup(group);
		cp.addNext(addField(cb,all[i+1])).setToolTip(all[i+2]);
	}
	return cp;
}

static String []
needOne = mString.split("Rename|Properties"),
viewFiles = mString.split("View File|View As|Run"),
needOneOrMore = mString.split("Copy|Cut|Delete"),
pasteOp = mString.split("Paste");
//-------------------------------------------------------------------
void fixMenu(Menu m,boolean isPopup)
//-------------------------------------------------------------------
{
	int [] all = table.getSelectedIndexes();
	boolean none = all.length == 0;
	boolean one = all.length == 1;
	boolean multi = all.length > 1;
	boolean oneOrMore = one | multi;
	m.modifyItems(needOne,one ? 0 : MenuItem.Disabled,one ? MenuItem.Disabled : 0,true);
	//if (!allowFileViewing) m.modifyItems(viewFiles,MenuItem.Disabled,0,true);
	m.modifyItems(needOneOrMore,oneOrMore ? 0 : MenuItem.Disabled,oneOrMore ? MenuItem.Disabled : 0,true);
	boolean paste = true;
	if (multi) paste = false;
	if (!FileClipboard.clipboard.canPasteInto(files.parent)) paste = false;
	m.modifyItems(pasteOp,paste ? 0 : MenuItem.Disabled,paste ? MenuItem.Disabled : 0,true);
}

boolean noWrite;

//-------------------------------------------------------------------
Control makeToolMenu()
//-------------------------------------------------------------------
{
	Menu mm = new Menu(), sm;

	String toFile =  !noWrite ?
		"New|-|Rename|-|" : "";
	toFile += "Run";
	//if (allowFileViewing)
	toFile += "|View File";
	mm.addItem(sm = new Menu(mString.split(toFile),"File"));
	if (!noWrite)
		sm.getItemAt(0).subMenu = new Menu(newChoices = mString.split(newItems),"New");
	//if (allowFileViewing)
	sm.addItem(new Menu(mString.split(viewable),"View as"));
	sm.addItem("Properties");

	toFile =  !noWrite ?
		"Copy|Cut|Paste|Delete|-|" : "Copy|";
	toFile += "Select All|Invert Selection";
	if (noKeyboard) toFile += "|Multiple Select";
	mm.addItem(sm = new Menu(mString.split(toFile),"Edit"));
	mm.addItem(sm = new Menu(viewChoices = mString.split("Details|Names Only"),"View"));
	sm.addItem("-");
	sm.addItem("Pop-up File Info");
	sm.addItem("Refresh");
	mm.addItem(sm = new Menu(sortChoices = mString.split("By Name|By Date|By Type|By Length"),"Sort"));

	if ((type & LAUNCHER_TYPE) == LAUNCHER_TYPE){
		if (LaunchPanel.launcher != null)
			mm.addItem("Install");
	}
	if (((type & EXPLORER_TYPE) != 0) && !((type & LAUNCHER_TYPE) == LAUNCHER_TYPE)){
		mm.addItem("-");
		mm.addItem("Close");
	}
	sm.addItems(mString.split("-|Descending"));
	addField(toolMenu = new ButtonPullDownMenu("",mm){
		public boolean checkMenu(Menu m){
			fixMenu(m,false);
			return true;
		}
	}
	,"toolField");
	toolMenu.image = tools;
	toolMenu.getMenu().checkOnlyOne(viewChoices,viewChoices[0],true);
	toolMenu.getMenu().checkOnlyOne(sortChoices,sortChoices[0],true);
	return toolMenu.setToolTip("Additional Tools");
}
/**
 * Create a FileChooser in OPEN mode.
 */
//===================================================================
public FileChooser()
//===================================================================
{
	this(OPEN,baseDir);
}
/**
 * Create a FileChooser with a certain type, in the specified initial directory.
 */
//===================================================================
public FileChooser(int type,String initial)
//===================================================================
{
	this(type,initial,ewe.sys.Vm.newFileObject());
}
public MenuItem goBackMenu, goForwardMenu;

//-------------------------------------------------------------------
void addDirTools(CellPanel cp)
//-------------------------------------------------------------------
{
	mButton dirUp = new mButton();
	dirUp.image = folderUp;
	if (back != null){
		ButtonBar bb = new ButtonBar();
		bb.addNext(addField(back.setToolTip("Go back"),"goBack"));
		bb.addNext(addField(forward.setToolTip("Go forward"),"goForward"));
		addField(back,"goBackMenu");
		addField(forward,"goForwardMenu");
		cp.addNext(bb).setCell(DONTSTRETCH);
	}
	Object got = afile.getNew("").getInfo(File.INFO_ROOT_LIST,null,null,0);
	sroots = null;
	if (got instanceof String [])
		sroots = (String [])got;
		/*
	else if (got != null)
		ewe.sys.Vm.debug("Got: "+got+"\n"+got.getClass());
		*/
	if (sroots != null && roots != null){
		Gui.iconize(roots,drive,false,getFontMetrics());
		cp.addNext(addField(roots,"drives")).setCell(DONTSTRETCH).setToolTip("Select a disk drive");
	}
	cp.addNext(addField(directories,"directories")).setCell(HSTRETCH);
	/*if ((type & 3) != DIRECTORY_SELECT && !bigScreen){
		cp.addNext(accept).setCell(DONTSTRETCH);
		cp.addNext(cancel).setCell(DONTSTRETCH);
	}*/
	cp.addNext(addField(dirUp,"dirUp")).setCell(DONTSTRETCH).setToolTip("Go to higher directory");
}
VMOptions vmoptions = new VMOptions();

mButton back, forward;
String viewable = "Text File|Zip File|Ewe File|Image File|Binary File";

//-------------------------------------------------------------------
private static int toType(FileChooserParameters fcp)
//-------------------------------------------------------------------
{
	int ret = OPEN;
	String r = fcp.getString(fcp.TYPE,"open");
	if (r.equalsIgnoreCase("open") || r.equalsIgnoreCase("load")) ret = OPEN;
	else if (r.equalsIgnoreCase("save")) ret = SAVE;
	else if (r.toLowerCase().startsWith("directory")) ret = DIRECTORY_SELECT;
	else if (r.equalsIgnoreCase("browse")) ret = BROWSE;
	ret |= fcp.getInt(fcp.OPTIONS,0);
	return ret;
}

private FileChooserParameters fcp;

//===================================================================
public FileChooser(FileChooserParameters fcp)
//===================================================================
{
	this(toType(fcp),fcp.getString(fcp.START_LOCATION,null),(File)fcp.getValue(fcp.FILE_MODEL,ewe.sys.Vm.newFileObject()));
	title = fcp.getString(fcp.TITLE,title);
	for (Property p = fcp.get(fcp.FILE_MASK); p != null; p = fcp.get(fcp.FILE_MASK,p))
		addMask((String)p.value);
	defaultExtension = fcp.getString(fcp.DEFAULT_EXTENSION,defaultExtension);
	persistentHistoryKey = fcp.getString(fcp.PERSISTENT_HISTORY,persistentHistoryKey);
	this.fcp = fcp;
}
/**
 * Create a new FileChooser(). After creating it, use execute() to display it. If execute() returns Form.IDCANCEL then
 * the user canceled the choose operation. Otherwise you can use getChosen() or getChosenFile() to get the chosen file.
 * <p>
 * You can also change the title of the FileChooser by setting "title" after construction.
 * @param type either OPEN, SAVE, DIRECTORY_SELECT or BROWSE OR'ed with some of the other options.
 * @param initial The initial directory.
 * @param aFileModel The file mode to use.
 */
//===================================================================
public FileChooser(int type,String initial,File aFileModel)
//===================================================================
{
	if (web.exists()) viewable += "|HTML File";
	if (globalLinks != null && globalLinks.size() != 0){
		links = new Vector();
		links.addAll(globalLinks);
	}
	//addLink(new FileChooserLink("Ewesoft Home","H:/MyWeb/Ewesoft",new mImage("ewe/ewesmall.bmp",ewe.fx.Color.White)));
	wideScreen = Gui.screenIs(Gui.WIDE_SCREEN);
	desktopWide = Gui.screenIs(Gui.DESKTOP_WIDTH);
	if (wideScreen){
		back = new mButton(null,"ewe/leftarrowsmall.bmp",ewe.fx.Color.White);
		forward = new mButton(null,"ewe/rightarrowsmall.bmp",ewe.fx.Color.White);
	}
/*
	if ((type & LAUNCHER_TYPE) == LAUNCHER_TYPE){
		if (vmoptions.read()){
			initial = vmoptions.launcherStart;
			if (vmoptions.launcherTree) type |= DIRECTORY_TREE;
			else type &= ~DIRECTORY_TREE;
			if (vmoptions.showIcon)
				new TaskbarWindow("Ewe VM","ewe/ewesmall.bmp","ewe/ewesmallmask.bmp","Ewe VM");
		}
	}
*/
	title = "File Chooser";
	afile = aFileModel;
	if (initial == null) initial = "";
	if (initial.trim().length() == 0)
		initial = baseDir;
	noKeyboard = (Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_NO_KEYBOARD) != 0;
	noMouse = (Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_NO_MOUSE_POINTER) != 0;
	noKeyboard = noMouse; //Using a pen on a hand-held device is like that of a palm.
	noWrite = (type & NO_WRITING) != 0;
	noWrite |= (afile.getFlags() & afile.FLAG_FILE_SYSTEM_IS_READ_ONLY) != 0;
	if ((type & 3) == OPEN) type |= FILE_MUST_EXIST;
	IImage newFolderImage =ImageCache.cache.get("ewe/NewFolder.bmp",Color.White);
	if (desktopWide && ((type & 3) != DIRECTORY_SELECT)) type |= DESKTOP_VERSION;

	this.type = type;
	if ((type & EXPLORER_TYPE) == 0)
		windowFlagsToClear = Window.FLAG_HAS_CLOSE_BUTTON;
	if (((type & LAUNCHER_TYPE) == LAUNCHER_TYPE) && ewe.sys.Vm.isMobile()){
		//title = "File C;
		windowFlagsToClear = Window.FLAG_HAS_CLOSE_BUTTON|Window.FLAG_HAS_TITLE;
	}
	slowAccess = ((afile.getFlags() & afile.FLAG_SLOW_CHILD_COUNT) != 0);
	file = initial;
	CellPanel cp;
	//boolean bigScreen = Gui.screenIs(Gui.BIG_SCREEN);
	boolean haveAccept = (type & 3) != BROWSE;
	boolean haveCancel = (type & EMBEDDED) == 0;
	accept = desktopWide ? new mButton(new IconAndText(Form.tick,"Accept",getFontMetrics())) : new mButton(tick);
	cancel = desktopWide ? new mButton(new IconAndText(Form.cross,haveAccept ? "Cancel" : "Close",getFontMetrics())) : new mButton(cross);
	CellPanel treePanel = null;
	CellPanel filePanel = new CellPanel();
	CellPanel main = new CellPanel();
	//titleCancel = cancel;
	exitButtonDefined = true;
	if ((type & 3) == DIRECTORY_SELECT){
		ButtonBar bb = new ButtonBar(); bb.equalWidths = false;
		if (!noWrite) bb.addNext(addField(new mButton(new IconAndText(newFolderImage,"New Folder",getFontMetrics())),"newFolder"));
		bb.addNext(addField(accept = new mButton(new IconAndText(Form.tick,"Select Folder",getFontMetrics())),"acceptSelection"));
		bb.addNext(addField(cancel = new mButton(new IconAndText(Form.cross,"Cancel",getFontMetrics())),"cancelSelection"));
		addLast(bb).setCell(HSTRETCH);
	}

	Dimension d = Gui.getPreferredDialogSize();
	setPreferredSize(d.width,d.height);
	Rect got = (Rect)mApp.mainApp.getInfo(Window.INFO_PARENT_RECT,null,new Rect(),0);
	if (got != null) d.set(got.width,got.height);

	if ((type & DIRECTORY_TREE) != 0){
		int how = wideScreen ? SplittablePanel.HORIZONTAL : SplittablePanel.VERTICAL;
		SplittablePanel sp = new SplittablePanel(how);
		addLast(sp);
		treePanel = sp.getNextPanel(0);
		if (how == sp.HORIZONTAL) treePanel.setPreferredSize(d.width/3,50);
		else treePanel.setPreferredSize(50,d.height/2);
		treePanel.setBorder(mInput.inputEdge|BF_RECT,2);
		tree = new FileTreeControl();
		tree.backGround = Color.White;
		tree.setTableModel(new FileTree(null,afile,this));
		//if (slowAccess) tree.getTreeTableModel().showWaitCursor = true;
		ScrollBarPanel sbp;
		treePanel.addLast(sbp = new ScrollBarPanel(tree));
		sbp.hbar.setFollowTracking(true);
		sbp.vbar.setFollowTracking(true);
		if ((type & 3) != DIRECTORY_SELECT){
			filePanel = sp.getNextPanel();
			sp.theSplitter.doCloseFirst = true;
			if (how == sp.HORIZONTAL) {
				filePanel.setPreferredSize(d.width*2/3,50);
				filePanel.setMinimumSize(0,0);
				if (desktopWide) filePanel.mySplitter.arrowPosition = Right;
			}else{
				filePanel.setPreferredSize(50,200);//d.height*2/3);
				filePanel.setMinimumSize(0,0);
				//filePanel.mySplitter.arrowPosition = Down;
			}
		}else{
			hasFileList = false;
		}
		addField(tree,"dirTree");
	}else{
		addLast(filePanel);
	}
	main.setPreferredSize(200,200);
	if ((type & EXTRA_CONTROL) == 0){
		filePanel.addNext(main);
	}else{
		int how = wideScreen ? SplittablePanel.HORIZONTAL : SplittablePanel.VERTICAL;
		SplittablePanel sp = new SplittablePanel(how);
		filePanel.addNext(sp);
		sp.getNextPanel().addLast(main);
		sp.getNextPanel().addLast(extraPanel = new CellPanel());
	}
	ScrollBarPanel sbp;

	mComboBox fileInput;
 	addField(fileInput = new mComboBox(),"file");
	fileInput.input.wantReturn = true;
	fileInput.input.prompt = "File/Mask";

	directories = new mChoice();
	directories.useMenuItems = true;
	directories.alwaysDrop = true;
	directories.indentDropItems = true;
	directories.prompt = "Choose Folder";
	directories.menuOptions |= directories.MENU_FULL_WIDTH;
	directories.menuOptions |= directories.MENU_SHOW_TITLE_IF_EXPANDED;
	//
	// Remove this.
	//directories.menuFont = new Font("serif",Font.PLAIN,12);
	//
	//directories.menuOptions |= directories.MENU_WINDOW_WIDTH_ON_PDA;
	if ((type & DESKTOP_VERSION) == 0){
		if (desktopWide && ((type & 3) != DIRECTORY_SELECT)){
			ButtonBar bb = new ButtonBar();
			if (haveAccept) bb.addNext(accept);
			bb.addNext(haveCancel ? cancel : new Control());
			main.addLast(bb).setCell(HSTRETCH);
		}
		if ((type & (NO_DIRECTORY_CHANGE|DIRECTORY_TREE|EXPLORER_TYPE)) == 0) {
			cp = dirNavigation = new CellPanel();
			cp.defaultTags.set(INSETS,new Insets(0,1,0,0));
			addDirTools(cp);
			main.addLast(cp).setCell(HSTRETCH);
		}else if ((type & DIRECTORY_TREE) != 0)	{
		/*
			if (((type & 3) == DIRECTORY_SELECT) && ((type & INSTALL_SELECT) == 0)){
				titleOK = accept;
			}
		*/
		}

		if (((type & 3) != DIRECTORY_SELECT)){
			if (((type & EXPLORER_TYPE) == 0) && ((type & DIRECTORY_TREE) != 0)){
				cp = dirNavigation = new CellPanel();
				cp.defaultTags.set(INSETS,new Insets(0,1,0,0));
				addDirTools(cp);
				main.addLast(cp).setCell(HSTRETCH);
				cp.setPreferredSize(0,0);
			}
			cp = new CellPanel();
			cp.defaultTags.set(INSETS,new Insets(0,1,0,0));
			makeToolMenu();
			cp.addNext(toolMenu).setCell(DONTSTRETCH);
			//toolButton.arrowDirection = Down;
			if ((type & EXPLORER_TYPE) == 0){
				cp.addNext(new mLabel("File:")).setCell(DONTSTRETCH);
				cp.addNext(fileInput).setCell(HSTRETCH);
			}
			maskChoices = fileInput.choice;
			if (!desktopWide){
				if ((type & EXPLORER_TYPE) == 0){
					if (haveAccept) cp.addNext(accept).setCell(DONTSTRETCH);
					if (haveCancel) cp.addLast(cancel).setCell(DONTSTRETCH);
				}else{
					addDirTools(cp);
				}
			}
			main.addLast(cp).setCell(HSTRETCH);
		}
		main.addLast(sbp = new ScrollBarPanel(table = new FileTableControl()));
	/**
	* This is the desktop layout.
	**/
	}else{
		resizable = true;
		cp = new CellPanel();
		cp.defaultTags.set(INSETS,new Insets(2,2,2,2));
		makeToolMenu();
		cp.addNext(toolMenu).setCell(DONTSTRETCH);
		addDirTools(cp);
		/*
		sroots = (String [])afile.getNew("").getInfo(File.INFO_ROOT_LIST,null,null,0);
		if (sroots != null){
			Gui.iconize(roots,drive,false,getFontMetrics());
			cp.addNext(addField(roots,"drives")).setCell(DONTSTRETCH).setToolTip("Select a disk drive");
		}
		cp.addNext(addField(directories,"directories")).setCell(HSTRETCH);
		mButton dirUp = new mButton();
		dirUp.image = folderUp;
		cp.addNext(addField(dirUp,"dirUp")).setCell(DONTSTRETCH).setToolTip("Go to higher directory");
		*/
		if (!noWrite)cp.addNext(addField(new mButton(newFolderImage),"newFolder")).setCell(DONTSTRETCH).setToolTip("Create a new directory");
		cp.addNext(getViewToolBar("wideview|iconMode|View multiple columns|listview|listMode|View single column")).setCell(DONTSTRETCH);
		cp.addNext(getViewToolBar("detailview|detailMode|Show file details")).setCell(DONTSTRETCH);
		/*if ((type & 3) != DIRECTORY_SELECT && !bigScreen){
			cp.addNext(accept).setCell(DONTSTRETCH);
			cp.addNext(cancel).setCell(DONTSTRETCH);
		}*/
		main.addLast(cp).setCell(HSTRETCH);
		main.addLast(sbp = new ScrollBarPanel(table = new FileTableControl())).setTag(INSETS,new Insets(2,4,2,4));
		CellPanel selectors = new CellPanel();
		selectors.defaultTags.set(INSETS,new Insets(2,2,2,2));
		if (haveAccept){
			selectors.addNext(new mLabel("File name:"),DONTSTRETCH,DONTFILL|EAST);
			selectors.addNext(fileInput).setCell(HSTRETCH);
		}
		historyList = fileInput.choice;
		if (haveAccept)
			selectors.addLast(accept).setCell(DONTSTRETCH);
		selectors.endRow();
		selectors.addNext(new mLabel("Files of type:"),DONTSTRETCH,DONTFILL|EAST);
		selectors.addNext(addField(maskChoices = new mChoice(),"fileType")).setCell(HSTRETCH);
		maskChoices.alwaysDrop = true;
		selectors.addLast(haveCancel ? cancel : new Control()).setCell(DONTSTRETCH);
		if ((type & EXPLORER_TYPE) == 0)
			main.addLast(selectors).setCell(HSTRETCH).setControl(HFILL|NORTHWEST);
	}
	table.listMode = 1;
	table.multiSelect = !noKeyboard;//((type & MULTI_SELECT) != 0);
	table.modify(WantHoldDown,0);
	addMask(allFilesMask);
	masksSet = false;
	sbp.hbar.setFollowTracking(true);
	sbp.vbar.setFollowTracking(true);
	addField(table,"fileTable");
	table.setClickMode(true);
	table.clickClearsItself = false;
	table.setToolTip(null);
	sbp.setBorder(mInput.inputEdge|BF_RECT,2);
	table.backGround = Color.White;
	table.setTableModel(files = new FileListTableModel(this));
	files.fileCheck = afile;
	//resizable = true;
	time.format = "dd-MM-yyyy, HH:mm";
	if ((type & 3) == DIRECTORY_SELECT) {
		lastOptions.viewOptions |= lastOptions.VIEW_NAME_ONLY;
	}
	setOptions(lastOptions);
	addField(accept,"acceptSelection");
	if (cancel != null) cancel.setHotKey(0,IKeys.ESCAPE);
	setHistory(history);
	amExiting = false;
	if (roots != null && sroots != null){
		roots.getMenu().addItems(sroots);
		//sroots = null;
	}
}

	protected void showError(String message,boolean execute)
	{
		showError("Error",message,execute);
	}
	//-------------------------------------------------------------------
	protected void showError(String title,String message,boolean execute)
	//-------------------------------------------------------------------
	{
		MessageBox mb = new MessageBox(title,message,MBOK);
		mb.exec(getFrame(),Gui.CENTER_FRAME);
		if (execute) mb.waitUntilClosed();
	}
//-------------------------------------------------------------------
protected boolean doHotKey(Control from,KeyEvent ev)
//-------------------------------------------------------------------
{
	if ((ev.key == IKeys.F5) && ((ev.modifiers & (IKeys.CONTROL|IKeys.SHIFT)) == 0) && (parent != null)){
		refresh();
		return true;
	}else
		return super.doHotKey(from,ev);
}
/**
* Refresh the file list.
**/
//===================================================================
public void refresh()
//===================================================================
{
	if (parent != null){
		parent.refresh();
		setFile(parent);
	}
}
//===================================================================
public static String lengthToDisplay(int len)
//===================================================================
{
	return Utils.fileLengthDisplay(len);
}
	//##################################################################
	class FileTableControl extends TableControl{
	//##################################################################
	{
		autoScrollToVisible = false;

	}
	//===================================================================
	public void doPaint(Graphics g,Rect area)
	//===================================================================
	{
		boolean sw = (afile.getFlags() & File.FLAG_SLOW_LIST) != 0;
		//if (sw) ewe.sys.Vm.showWait(true);

		super.doPaint(g,area);
		//if (sw)Vm.showWait(false);
	}

	//===================================================================
	public boolean checkMenu(Menu m)
	//===================================================================
	{
		fixMenu(m,true);
		return true;
	}
	//===================================================================
	public void popupMenuEvent(final Object selected)
	//===================================================================
	{
		new ewe.sys.TaskObject(){
		public void doRun(){
			FileChooser.this.checkMenu(mString.toString(selected),FileChooser.this);
		}
		}.startTask();
	}
	//===================================================================
	public void onKeyEvent(KeyEvent ev)
	//===================================================================
	{
		if (ev.type == ev.KEY_PRESS){
			if (ev.key == IKeys.BACKSPACE) {
				if (lastInHistory > 0)
					setFile(afile.getNew(backForward.get(lastInHistory-1).toString()),true,-1);
			}else if (ev.key == IKeys.DELETE){
				int opts = 0;
				if ((ev.modifiers & IKeys.CONTROL) != 0) opts |= NOCONFIRM;
				deletePressed(opts);
			}if (ev.key == IKeys.LEFT && noKeyboard){
				dirUp();
			}
		}
		super.onKeyEvent(ev);
	}
	//##################################################################
	}
	//##################################################################


	//##################################################################
	class FileTreeControl extends TreeControl{
	//##################################################################

	public void resizeTo(int w,int h)
	{
		super.resizeTo(w,h);
		int m = FileChooser.this.modify(Invisible,0);
		if (dirNavigation != null){
			if (w < 10 || h < 10)
				dirNavigation.clearTag(dirNavigation.PREFERREDSIZE);
			else
				dirNavigation.setPreferredSize(0,0);
			dirNavigation.getParent().redisplay();
		}
		FileChooser.this.setFile(FileChooser.this.parent,false);
		FileChooser.this.restore(m,Invisible);
	}

	//##################################################################
	}
	//##################################################################

//===================================================================
static String toFullPath(Object what)
//===================================================================
{
	if (what instanceof File) return File.fixupPath(((File)what).getFullPath());
	else return what.toString();
}
/**
* This does not affect the "history" variable. This just assigns the Strings or Files
* in the Vector to the history drop down box (desktop layout only).
**/
//===================================================================
public void setHistory(Vector v)
//===================================================================
{
	if (historyList == null) return;
	historyList.shortenItems = true;
	historyList.items.clear();
	if (v == null) return;
	for (int i = 0; i<v.size(); i++)
		historyList.addItem(toFullPath(v.get(i)));
}

String [] found;
File parent;
Vector levels = new Vector();

//===================================================================
public void setOptions(FileChooserOptions options)
//===================================================================
{
	if (options == null) options = new FileChooserOptions();
	if (lastOptions == null) lastOptions = new FileChooserOptions();
	lastOptions.copyFrom(options);

	sortBy = lastOptions.sortOptions & ~File.LIST_DESCENDING;
	descending = lastOptions.sortOptions & File.LIST_DESCENDING;
	tips = lastOptions.viewOptions & FileChooserOptions.VIEW_FILE_TIPS;
	int which = ((lastOptions.viewOptions & FileChooserOptions.VIEW_NAME_ONLY) != 0) ? 1 : 0;
	files.setShowDetails(detailMode = (which == 0));
	//iconMode = !(listMode = detailMode);
	files.fileTips = tips != 0;

	if (viewChoices != null && toolMenu != null){
		toolMenu.getMenu().checkOnlyOne(viewChoices,viewChoices[which],true);
		which = 0;
		if ((lastOptions.sortOptions & File.LIST_BY_DATE) == File.LIST_BY_DATE)
			which = 1;
		if ((lastOptions.sortOptions & File.LIST_BY_TYPE) == File.LIST_BY_TYPE)
			which = 2;
		if ((lastOptions.sortOptions & File.LIST_BY_SIZE) == File.LIST_BY_SIZE)
			which = 3;
		toolMenu.getMenu().checkOnlyOne(sortChoices,sortChoices[which],true);
		MenuItem mi = toolMenu.getMenu().findItem("Descending",true);
		mi.modifiers &= ~mi.Checked;
		if ((descending & File.LIST_DESCENDING) != 0) mi.modifiers |= mi.Checked;
		mi = toolMenu.getMenu().findItem("Pop-up File Info",true);
		if (mi != null){
			mi.modifiers &= ~mi.Checked;
			if ((tips & FileChooserOptions.VIEW_FILE_TIPS) != 0) mi.modifiers |= mi.Checked;
		}
	}
	/*
	if (maskChoices != null)
	for (int i = 0; i<maskChoices.items.size(); i++)
		if (((String)maskChoices.items.get(0)).equals(lastOptions.mask)){
			mask = lastOptions.mask;
			break;
		}
		*/
}


/**
 * Use this to add a file mask to the chooser's mask list. The mask should
	be a string with a set of masks (e.g. *.bmp) separated by commas (no spaces in between)
	followed by a space and then any descriptive name.
	<p>For example: "*.bmp,*.png - Image files."
 * @param mask The mask to add.
 */
//===================================================================
public void addMask(String mask)
//===================================================================
{
	if (maskChoices != null) {
		if (!masksSet) {
			maskChoices.items.clear();
			this.mask = fixMask(mask);
			fileType = mask;
		}
		if (maskChoices.items.find(mask) == -1)
			maskChoices.addItem(mask);
	}
	masksSet = true;
}
/**
* This returns the list of masks currently set for the file chooser. You can add, remove masks
* as necessary, but this must be done before the chooser is shown.
**/
//===================================================================
public Vector getMaskList()
//===================================================================
{
	if (maskChoices != null) return maskChoices.items;
	return null;
}
/**
* Use this if you have updated the Vector returned by getMaskList().
**/
//===================================================================
public void masksChanged()
//===================================================================
{
	if (maskChoices.items.size() != 0) {
		fileType = (String)maskChoices.items.get(0);
		mask = fixMask(fileType);
		masksSet = true;
	}else
		masksSet = false;
}
//===================================================================
public void shown()
//===================================================================
{
	if (historyList != null && persistentHistoryKey != null)
		try{
			history = new Vector(mString.split(ewe.io.IO.getConfigInfo("FileHistory\\"+persistentHistoryKey),'\n'));
			setHistory(history);
		}catch(Exception e){
		}
	setFile(afile.getNew(file));
	super.shown();
}
/**
 * Setup the static history list for FileChoosers in this application using the specified
 * persistentHistoryKey. Call this method at the start of your application.
 * @param persistentHistoryKey a unique key name for your application.
 * @exception ewe.io.IOException if there was an error.
 */
//===================================================================
public static void retrieveApplicationHistory(String persistentHistoryKey)
throws ewe.io.IOException
//===================================================================
{
	history = new Vector(mString.split(ewe.io.IO.getConfigInfo("FileHistory\\"+persistentHistoryKey),'\n'));
}

/**
 * Store the specified list of Strings as the application history.
 * @param persistentHistoryKey a unique key name for your application.
 * @param history a Vector of Strings.
 * @exception ewe.io.IOException
 */
//===================================================================
public static void storeApplicationHistory(String persistentHistoryKey,Vector history)
throws ewe.io.IOException
//===================================================================
{
	if (persistentHistoryKey != null){
		String s = "";
		for (int i = 0; i<history.size(); i++){
			if (i != 0) s += "\n";
			s += File.fixupPath(history.get(i).toString());
		}
		ewe.io.IO.saveConfigInfo(s,"FileHistory\\"+persistentHistoryKey,ewe.io.IO.SAVE_IN_REGISTRY);
	}
}

/**
* Do not use this.
**/
public String mask = "*.*";

int sortBy = 0, descending = 0, tips;

/**
* Do not use this.
**/
//===================================================================
public String fixMask(String mask)
//===================================================================
{
	if (mask == null) return "";
	int idx = mask.indexOf(' ');
	if (idx == -1) return mask;
	return mask.substring(0,idx);
}
static final String [] readingDirectory = new String [] {"Reading directory..."};

boolean showDirsInTable()
{
	if ((type & NO_DIRECTORY_CHANGE) != 0) return false;
	if ((type & DIRECTORY_TREE) == 0) return true;
	Dimension d = tree.getSize(null);
	if (d.width < 10 || d.height < 10) return true;
	return false;
}
/**
* Do not use this.
**/
public boolean setFile(File file) {return setFile(file,true);}


String lastDir;
int lastInHistory = -1;
Vector backForward;
/**
* Do not use this.
**/
//===================================================================
public boolean setFile(File file,boolean expandTree)
//===================================================================
{
	return setFile(file,expandTree,0);
}
//-------------------------------------------------------------------
private boolean setFile(File file,boolean expandTree,int historyMoveDirection)
//-------------------------------------------------------------------
{
	try{
	if (back != null)
		if (backForward == null) backForward = new Vector();
	if (file == null) return false;
	table.clearSelectedCells(null); table.clearCursor();
	File theDir = file;
	String theFile = mask;
	if ((file.getFullPath().indexOf('*') != -1) || !file.isDirectory()) {
		theDir = file.getParentFile();
		theFile = file.getFileExt();
	}
	if (theDir == null) theDir = parent;//afile.getNew(baseDir);//return false;
	if (theDir == null) theDir = afile.getNew(baseDir);
	if (theFile == null) theFile = mask;
	if (theFile.indexOf('*') != -1) {
		mask = fixMask(theFile);
	}
	lastOptions.mask = mask;
	parent = theDir;

	try{
		if (backForward != null){
			lastDir = theDir.getFullPath();
			int n = lastInHistory+1;
			if (historyMoveDirection > 0){
				for (n = lastInHistory+1; n < backForward.size() ; n++)
					if (backForward.get(n).equals(lastDir)) break;
			}else if (historyMoveDirection < 0){
				for (n = lastInHistory-1; n >= 0 ; n--)
					if (backForward.get(n).equals(lastDir)) break;
			}else{
				while(n < backForward.size()) backForward.del(n);
			}
			if (n == -1){
				n = lastInHistory+1;
				while(n < backForward.size()) backForward.del(n);
			}
			/*
			if (n > backForward.size()) n = backForward.size();
			else if (n < backForward.size()){
				FileChooserLink fcl = (FileChooserLink)backForward.get(n);
				if (!fcl.equals(lastDir))
					while(n < backForward.size()) backForward.del(n);
			}
			*/
			if (n == backForward.size()){
				if (n > 0){
					if (!backForward.get(n-1).equals(lastDir))
						backForward.add(lastDir);
					else
						n--;
				}else
					backForward.add(lastDir);
			}
			while (backForward.size() > 10) {
				backForward.del(0);
				n--;
			}
			lastInHistory = n;
			FontMetrics fm = getFontMetrics();
			if (n == 0) back.modify(Disabled,0);
			else {
				back.modify(0,Disabled);
				Menu m = new Menu();
				for (int i = n-1; i >= 0; i--)
					new FileChooserLink(backForward.get(i).toString(),afile).addToMenu(m,fm);
				back.setMenu(m);
			}
			if (n == backForward.size()-1) forward.modify(Disabled,0);
			else {
				forward.modify(0,Disabled);
				Menu m = new Menu();
				for (int i = n+1; i < backForward.size(); i++)
					new FileChooserLink(backForward.get(i).toString(),afile).addToMenu(m,fm);
				forward.setMenu(m);
			}
			back.repaintNow(); forward.repaintNow();
			//ewe.sys.Vm.debug(backForward.toString()+", "+lastInHistory);

		}
	}catch(Exception e){e.printStackTrace();} //Just for safety.

	//new MessageBox("File",file.getFullPath()+"\n"+parent,0).exec();
	if ((type & LAUNCHER_TYPE) == LAUNCHER_TYPE)
		VMOptions.currentDir = parent.getFullPath();
	if (hasFileList)
		if ((afile.getFlags() & File.FLAG_SLOW_LIST) != 0 || ewe.sys.Vm.isMobile())
			files.setFiles(parent,readingDirectory);
	if (tree != null && expandTree){
		TreeTableModel tm = tree.getTreeTableModel();
		ewe.data.TreeNode root = tm.getRootObject();
		String fp = parent.getFullPath();
		//ewe.sys.Vm.debug("FP: "+fp);
		fp = mString.removeTrailingSlash(fp);
		int [] addr = ewe.data.LiveTreeNode.addressOfChild(root,fp);
		if (addr != null) tm.expandTo(addr,true);
	}
	if (hasFileList){
		int old = 0;
		boolean sw = (afile.getFlags() & File.FLAG_SLOW_LIST) != 0;
		if (sw) {
			//ewe.sys.Vm.showWait(true);
			//files.setFiles(parent,readingDirectory);
		}
		if ((mask.indexOf(',') == -1 && mask.indexOf(';') == -1) || ((type & 3) == DIRECTORY_SELECT)){
			int opts = File.LIST_ALWAYS_INCLUDE_DIRECTORIES;
			if ((type & 3) == DIRECTORY_SELECT) opts |= File.LIST_DIRECTORIES_ONLY;
			if (!showDirsInTable()) opts = File.LIST_FILES_ONLY;
			opts |= sortBy|descending;
			found = parent.list(mask,opts);
		}else{
			char c = mask.indexOf(',') == -1 ? ';' : ',';
			String masks [] = mString.split(mask,c);
			String dirs [] = new String[0];
			if (showDirsInTable())
				dirs = parent.list("*.*",File.LIST_DIRECTORIES_ONLY);
			found = parent.list("*.*",File.LIST_FILES_ONLY|sortBy|descending);

			FileComparer [] fcs = new FileComparer[masks.length];

			for (int i = 0; i<masks.length; i++)
				fcs[i] = new FileComparer(parent,ewe.sys.Vm.getLocale(),sortBy|descending,masks[i]);
			int left = found.length;
			for (int i = 0; i<found.length; i++){
				boolean matched = false;
				for (int f = 0; f<fcs.length; f++){
					if (fcs[f].matches(found[i])){
						matched = true;
						break;
					}
				}
				if (!matched) {
					found[i] = null;
					left--;
				}
			}
			String [] isMatching = new String[dirs.length+left];
			ewe.sys.Vm.copyArray(dirs,0,isMatching,0,dirs.length);
			for (int i = 0, d = dirs.length; i<found.length; i++)
				if (found[i] != null)
					isMatching[d++] = found[i];
			found = isMatching;
		}
		files.setFiles(parent,found);
		//if (sw) ewe.sys.Vm.showWait(false);
	}
	file = theDir;

	Vector v = new Vector();
	for(File p = file; p != null; p = p.getParentFile()){
		String s = p.getFileExt();
		if (!s.equals("."))
			v.add(0,s);
	}
	if (directories != null){
		directories.removeAll();
		levels.clear();
		File cur = null;
		FontMetrics fm = directories.getMenuFontMetrics();
		int start = 0;
		if (links != null){
			start = links.size();
			for (int i = 0; i<start; i++){
				FileChooserLink l = (FileChooserLink)links.get(i);
				MenuItem mi = l.addToMenu(directories,fm);
				mi.indentLevel = 0;
				levels.add(afile.getNew(l.path));
			}
		}
		String [] all = sroots;
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
					MenuItem mi = directories.addItem(dir);
					mi.image = new IconAndText(File.getIcon(isDrive ? File.DriveIcon : File.OpenFolderIcon),dir,fm).setColor(null,null);
					mi.indentLevel = i;
				}
				sel = sz+r-1+start;
			}else{
				levels.add(afile.getNew(all[r]));
				//if (all[r].indexOf(':') != -1) all[r] = all[r].toUpperCase();
				MenuItem mi = directories.addItem(all[r]);
				mi.image = new IconAndText(File.getIcon(File.DriveIcon),all[r],fm).setColor(null,null);
				mi.indentLevel = 0;
			}
		}
		directories.select(sel);
	}
	String next = mask == null ? "" : mask;
	if (this.file != next){
		this.file = next;
		toControls("file");
	}
	focusToFile();
	return false;
	}finally{
		//ewe.sys.Vm.debug("CD: "+files.parent);//+", "+chosenFiles.get(0));
	}
}
public MenuItem drives,toolField;
//===================================================================
public boolean checkLength(File f,int warningLength)
//===================================================================
{
	int len = f.getLength();
	if (len > warningLength)
		if (new MessageBox("Large File","This file is: "+
		ewe.sys.Vm.getLocale().format(Locale.FORMAT_PARSE_NUMBER,new ewe.sys.Long().set(len),",")
		+" bytes long.\nThis may take a while to open.\nDo you want to continue?",MBYESNO).execute(getFrame(),Gui.CENTER_FRAME)
		 != IDOK) return false;
	return true;
}
//===================================================================
public static boolean doPaste(File targetDirectory,FileChooser targetChooser)
//===================================================================
{
	FileClipboard c = FileClipboard.clipboard;
	if (!c.hasFiles()) return false;
	if (c.sourceFiles.length == 0) return false;
	File dest = targetDirectory;
	if (!dest.isDirectory()) return false;
	ewe.io.CopyOver co = new ewe.io.CopyOver();
	co.sourceDirectory = c.sourceDir;
	co.destDirectory = dest;
	co.sourceFiles = c.sourceFiles;
	co.doMove = c.isCut;
	co.makeCopies = !c.isCut;
	co.displayTitle = c.isCut ? "Move Files" : "Copy Files";
	co.execute(targetChooser == null ? null : targetChooser.getFrame(),null);
	c.taken();
	c.clear();
	targetDirectory.refresh();
	if (targetChooser != null) {
		targetChooser.setFile(targetChooser.files.parent);
		if (targetChooser.tree != null)
			targetChooser.tree.getTreeTableModel().reExpandNode(targetChooser.tree.getTreeTableModel().selectedLine);
	}
	return true;
}
//===================================================================
void detailModeChanged()
//===================================================================
{
	files.setShowDetails(detailMode);
	lastOptions.viewOptions = (lastOptions.viewOptions & ~FileChooserOptions.VIEW_NAME_ONLY) | ((detailMode) ? 0 : FileChooserOptions.VIEW_NAME_ONLY);
	if (detailMode) {
		//iconMode = false;
		//listMode = true;
		//toControls("iconMode,listMode");
	}
	toControls("detailMode");
	table.clearSelectedCells(null);
	table.clearCursor();
	table.update(true);
}

//===================================================================
public static String askClassName(String pathToClass,StringBuffer programDir,Frame parent)
//===================================================================
{
	String target = pathToClass;
	final String cls = target.replace('\\','.').replace('/','.').substring(0,target.length()-6);
	String className = null;
	int idx = cls.toLowerCase().indexOf("classes.");
	if (idx != -1)
		className = cls.substring(idx+8);
	else{
		PropertyList pl = new InputObject(){
			protected void setupInputStack(InputStack is,Editor ed){
				ed.windowFlagsToClear = Window.FLAG_HAS_TITLE;
				Vector v = new Vector();
				String got = cls;
				while(true){
					v.add(0,got);
					int i = got.indexOf('.');
					if (i == -1) break;
					got = got.substring(i+1);
				}
				String [] all = new String[v.size()];
				v.copyInto(all);
				ed.addField(is.addChoice("Class:",all,0),"className$");
			}
		}.input(parent,"Select Class Name",null);
		if (pl == null) return null;
		className = pl.getString("className","<none>");
	}
	if (className == null) return null;
	String path = target.substring(0,target.length()-className.length()-1-6);
	if (path.toLowerCase().endsWith("classes")) path = path.substring(0,path.length()-8);
	if (programDir != null) programDir.append(path);
	return className;
}

public void doNewShortcut(Editor f)
{
	if (!afile.getClass().getName().equals("ewe.io.File")){
		showError("Cannot create shortcut on this file system.",true);
		return;
	}
	NewShortcut ns = new NewShortcut();
	if (ns.getEditor(0).execute(f.getFrame(),Gui.CENTER_FRAME) == IDCANCEL) return;
	try{
		String sn = ns.shortcutName;
		String args = ns.arguments.trim();
		if (args.length() == 0) args = null;
		if (!sn.toLowerCase().endsWith(".lnk"))
			sn += ".lnk";
		File sc = afile.getNew(parent,sn);
		if (sc.exists()){
			showError("Shortcut already exists!",true);
			return;
		}
		ewex.registry.Registry.createShortcut(ns.target,args,sc.getFullPath());
	}catch(Exception e){
		showError("Could not create shortcut!",true);
		return;
	}
	setFile(parent);
}
//===================================================================
public void runFile(File file)
//===================================================================
{
	String where = file.getFullPath();
	String sl = where.toLowerCase();
	if (sl.endsWith(".ewe"))
		ewe.sys.Vm.executeEwe(file.getFullPath(),null);
	else if (sl.endsWith(".exe"))
		ewe.sys.Vm.execute(file.getFullPath(),null);
	else if (sl.endsWith(".class")){
		StringBuffer programDir = new StringBuffer();
		String clName = askClassName(where,programDir,null);
		if (clName == null) return;
		String all = "/d \""+programDir+"\" "+clName;
		ewe.sys.Vm.execute(ewe.sys.Vm.getPathToEweVM(),all);
	}else {
		showError("Cannot run that file.",true);
	}
}
//===================================================================
private void cantView()
//===================================================================
{
	showError("Cannot view that file type\non a mobile device.",true);
}
static final int NOCONFIRM = 1;
//===================================================================
public void deletePressed(int how)
//===================================================================
{
			String [] all = files.getSelectedFiles();
			if (all.length == 0) return;
			if ((how & NOCONFIRM) == 0)
			if (new MessageBox("Delete?","Delete the selected file(s)?",MBYESNO).execute(getFrame(),Gui.CENTER_FRAME) != IDOK)
				return;
			boolean alwaysContinue = false;
			for (int i = 0; i<all.length; i++){
				File file = files.parent.getChild(all[i]);
				if (!file.delete())
					if (!alwaysContinue){
						int ret = ewe.io.CopyOver.continueOp(getFrame(),"deleted",all[i]);
						if (ret == IDCANCEL) break;
						alwaysContinue = (ret == 1000);
					}
			}
			files.parent.refresh();
			setFile(files.parent);
			switchMultiSelect(false);
}
//===================================================================
public void checkMenu(String menuLabel,Editor f)
//===================================================================
{
		String which = menuLabel;
		if (toolMenu.getMenu().checkOnlyOne(viewChoices,which,true)){
			detailMode = which.equals(viewChoices[0]);
			detailModeChanged();
		}else if (toolMenu.getMenu().checkOnlyOne(sortChoices,which,true)){
			if (which.equals(sortChoices[0])) sortBy = 0;
			else if (which.equals(sortChoices[1])) sortBy = File.LIST_BY_DATE;
			else if (which.equals(sortChoices[2])) sortBy = File.LIST_BY_TYPE;
			else if (which.equals(sortChoices[3])) sortBy = File.LIST_BY_SIZE;
			lastOptions.sortOptions = sortBy | descending;
			setFile(parent);
		}else if (which.equals("Multiple Select")){
			switchMultiSelect(!table.multiSelect);
		}else if (which.equals("Descending")){
			MenuItem mi = toolMenu.getMenu().findItem(which,true);
			mi.modifiers ^= mi.Checked;
			if ((mi.modifiers & mi.Checked) != 0) descending = File.LIST_DESCENDING;
			else descending = 0;
			lastOptions.sortOptions = sortBy | descending;
			setFile(parent);
		}else if (which.equals("Folder")){
			doNewFolder(f);
		}else if (which.equals("File")){
			doNewFile(f);
		}else if (which.equals("Shortcut")){
			doNewShortcut(f);
		}else if (which.equals("Refresh")){
			parent.refresh();
			setFile(parent);
		}else if (which.equals("Pop-up File Info")){
			MenuItem mi = toolMenu.getMenu().findItem(which,true);
			if (mi != null){
				mi.modifiers ^= mi.Checked;
				if ((mi.modifiers & mi.Checked) != 0) tips = FileChooserOptions.VIEW_FILE_TIPS;
				else tips = 0;
				lastOptions.viewOptions &= ~FileChooserOptions.VIEW_FILE_TIPS;
				lastOptions.viewOptions |= tips;
				files.fileTips = tips != 0;
			}
		}else if (which.equals("Properties")){
			try{
				String [] all = files.getSelectedFiles();
				if (all.length != 1) return;
				File file = files.parent.getChild(all[0]);
				String prop = file.getPropertiesString();
				/*
				Form ff = new Form();
				ff.title = "File Properties";
				mTabbedPanel mt = new mTabbedPanel();
				mTextPad tp = new mTextPad(prop);
				mt.addItem(new ScrollBarPanel(tp),"General",null);
				ff.addLast(mt);
				*/
				MessageBox ff = new MessageBox("File Properties",prop,MessageBox.MBOK);
				ff.doBeep = false;
				ff.useTextMessage = ff.useScrollBars = true;
				ff.textAlignment = LEFT;
				ff.resizable = true;
				ff.execute();//getFrame(),Gui.CENTER_FRAME);
			}catch(Exception e){
				new ReportException(e,null,null,false).execute();
				return;
			}
		}else if (which.equals("Rename")){
			String [] all = files.getSelectedFiles();
			if (all.length != 1) return;
			File file = files.parent.getChild(all[0]);
			String newName = new InputBox("Rename File").input(getFrame(),all[0],30);
			if (newName == null) return;
			if (newName.indexOf('/') != -1 || newName.indexOf('\\') != -1 || newName.indexOf(':') != -1){
				showError("Bad file name.",false);
				return;
			}
			if (!file.rename(newName)){
				showError("The file could not be renamed.",false);
				return;
			}
			parent.refresh();
			int idx = files.indexOf(all[0]);
			if (idx == -1) return;
			files.files[idx] = newName;
			Point p = files.cellOf(idx);
			if (p == null) return;
			table.repaintCell(p.y,p.x);
		}else if (which.equals("Select All")){
			table.selectAllIndexes(true);
			table.repaintNow();
		}else if (which.equals("Invert Selection")){
			table.invertSelectedIndexes();


			table.repaintNow();
		}else if (which.equals("Cut")||which.equals("Copy")){
			boolean cut = which.equals("Cut");
			files.setFilesToCopy(cut);
			if (noKeyboard && table.multiSelect)
				switchMultiSelect(false);
			if (cut) table.repaintNow();
		}else if (which.equals("Delete")){
			deletePressed(0);
		}else if (which.equals("Paste") || which.equals("Paste In")){
			File dest = files.parent;
			if (which.equals("Paste In")){
				String [] all = files.getSelectedFiles();
				if (all.length != 1) return;
				dest = dest.getChild(all[0]);
			}
			doPaste(dest,this);
		}else if (which.equals("Close")){
			exit(0);
		}else if (which.equals("Install")){
			String [] all = files.getSelectedFiles();
			if (all.length != 1) return;
			String sl = all[0].toLowerCase();
			if (sl.endsWith(".class") || sl.endsWith(".ewe")){
				if (LaunchPanel.launcher != null)
					LaunchPanel.launcher.add(files.parent.getChild(all[0]).getFullPath(),getFrame());
			}
		}else if (which.equals("Run")){
			String [] all = files.getSelectedFiles();
			if (all.length != 1) return;
			runFile(files.parent.getChild(all[0]));
			return;
		}else if (which.endsWith(" File")){
			String [] all = files.getSelectedFiles();
			if (all.length != 1) return;
			String sl = all[0].toLowerCase();
			if (which.equals("View File")){
				if (sl.endsWith(".txt")) checkMenu("Text File",f);
				if (sl.endsWith(".zip")) checkMenu("Zip File",f);
				if (sl.endsWith(".ewe")) checkMenu("Ewe File",f);
				if (sl.endsWith(".htm")) checkMenu("HTML File",f);
				if (sl.endsWith(".html")) checkMenu("HTML File",f);
				if (sl.endsWith(".bmp") || sl.endsWith(".png") ||
				(sl.endsWith(".jpg") && ImageCodec.canDecodeJPEG) ||
				(sl.endsWith(".jpeg") && ImageCodec.canDecodeJPEG) ||
				(sl.endsWith(".gif") && ImageCodec.canDecodeGIF)

				) checkMenu("Image File",f);
				else return;
			}
			final File file = files.parent.getChild(all[0]);
			if (which.equals("Text File")){
				if (!allowFileViewing){
					cantView();
					return;
				}
				if (!checkLength(file,60000)) return;
				ewe.io.Stream is = file.getInputStream();
				if (is == null) {
					showError(file.getFileExt()+"\nThat file cannot be viewed.",true);
					return;
				}
				MessageBox mb = new MessageBox("Opening...","Opening the text file.\nPlease wait",0);
				is.close();
				Notepad np = new Notepad();
				np.saver.fileModel = afile;
				mb.doBeep = false;
				mb.exec();
				boolean op = false;
				try{
					op = np.open(file.getFullPath());
				}finally{
					mb.close(0);
				}
				if (op) np.exec(isModal());
				/*
				ewe.io.StreamReader br = new ewe.io.StreamReader(is);
				br.encoding = null;
				String txt = "";
				while(true){
					String line = br.readLine();
					if (line == null) break;
					txt += line+"\n";
				}
				br.close();
				final String nt = txt;
				mb.close(0);
				new Form(){
					{
						title = file.getFileExt();
						resizable = true;
						mTextPad mt = new mTextPad(10,40);
						addLast(new ScrollBarPanel(mt));
						mt.setText(nt);
						mt.modify(DisplayOnly,0);
					}
				}.exec(isModal());
				*/
			}else if (which.equals("Image File")){
				if (!checkLength(file,1000000)) return;
				int max = 0;
				if (ewe.sys.Vm.isMobile()) max = 200;
				final IImage im = ImageFileChooser.getImage(file,max,max,false,null);

				if (im == null) {
					showError(file.getFileExt()+"\nThat file cannot be viewed.",true);
					return;
				}
				new Form(){
					{
						title = file.getFileExt();
						resizable = true;
						addLast(new ImageControl(im).setTextSize(15,4));
					}
				}.exec(isModal());
			}else if (which.equals("Binary File")){
				if (!allowFileViewing){
					cantView();
					return;
				}
				try{
					Form ff = ewe.ui.HexTableModel.getViewOf(file,100000,null);
					ff.execute();
					return;
				}catch(ewe.io.IOException e){
					showError(file.getFileExt()+"\nThat file cannot be viewed.",true);
				}
			}else if (which.equals("HTML File")){
				if (!allowFileViewing){
					cantView();
					return;
				}
				if (web.exists()){
					IWebBrowser w = (IWebBrowser)web.newInstance();
					w.showFor(file.getFullPath(),true);
				}
			}else if (which.equals("Zip File")){
				if (!allowFileViewing){
					cantView();
					return;
				}
				Object got = zipView.newInstance("(Lewe/io/File;ZJ)V",new Object[]{file,new Boolean(isModal()),new java.lang.Long(10000000)});
				if (got == null) showError(file.getFileExt()+"\nThat file cannot be viewed.",true);
			}else if (which.equals("Ewe File")){
				if (!allowFileViewing){
					cantView();
					return;
				}
				Object got = eweView.newInstance("(Lewe/io/File;ZJ)V",new Object[]{file,new Boolean(isModal()),new java.lang.Long(1000000)});
				if (got == null) showError(file.getFileExt()+"\nThat file cannot be viewed.",true);
			}
		}
}
//===================================================================
public void fieldChanged(ewe.reflect.FieldTransfer ft,Editor f)
//===================================================================
{
	if (amExiting) return;
	if (ft.fieldName.equals("directories")){
		int i = directories.selectedIndex;
		if (i < 0 || i >= levels.size()) return;
		setFile((File)levels.get(i));
	}
	else if (ft.fieldName.equals("file")){
		Event ev = f.currentEvent;
		if (ev != null)
			if ((ev.flags & mInput.DATA_CHANGED_BY_ENTER) != 0) return;
		newFileInfo(false);
	}else if(ft.fieldName.equals("fileType")){
		file = fileType;
		f.toControls("file");
		newFileInfo(false);
	}else if (ft.fieldName.equals("drives")){
		setFile(afile.getNew(drives.label));
	}else if (ft.fieldName.equals("toolField")){
		checkMenu(toolField.label,f);
	}else if (ft.fieldName.equals("iconMode")){// || ft.fieldName.equals("listMode") || ft.fieldName.equals("detailMode")){
		files.verticalOnly = !iconMode;
		files.clearCellAdjustments();
		files.updateDisplay();
	}else if (ft.fieldName.equals("detailMode")){
		detailModeChanged();
	}else if (ft.fieldName.equals("goBackMenu")){
		setFile(afile.getNew(goBackMenu.data.toString()),true,-1);
	}else if (ft.fieldName.equals("goForwardMenu")){
		setFile(afile.getNew(goForwardMenu.data.toString()),true,+1);
	}
}

//-------------------------------------------------------------------
void focusToFile()
//-------------------------------------------------------------------
{
	try{
		if (noKeyboard) return;
		Control ip = (Control)findFieldTransfer("file").dataInterface;
		mInput mi = null;
		if (ip instanceof mComboBox)
			mi = ((mComboBox)ip).input;
		else if (ip instanceof mInput)
			mi = ((mInput)ip);
		if (ip instanceof mComboBox)
			if (((mComboBox)ip).choice.menuIsActive())
				mi = null;
		if (mi != null){
			if (Gui.focusedControl() != mi) return;
			mi.selectAll();
			//if (Gui.focusedControl() != mi) Gui.takeFocus(mi,ByRequest);
		}
	}catch(Exception e){}
}
/**
* This gets called when a new file has been clicked or entered. This file
* has not necessarily been accepted yet.
**/
//-------------------------------------------------------------------
protected void newFileSelected(File f)
//-------------------------------------------------------------------
{
}
//===================================================================
public boolean newFileInfo(boolean isAction)
//===================================================================
{
	boolean ret = doNewFileInfo(isAction);
	if (ret && !isAction)
		newFileSelected(afile.getNew(parent,file));
	return ret;
}
//-------------------------------------------------------------------
File existsAs(File f)
//-------------------------------------------------------------------

{
	if (f.exists()) return f;
	if (defaultExtension == null) return null;
	File f2 = afile.getNew(f.getFullPath()+"."+defaultExtension);
	if (f2.exists()) return f2;
	return null;
}
//-------------------------------------------------------------------
boolean doNewFileInfo(boolean isAction)
//-------------------------------------------------------------------
{
		if ((type & 3) == DIRECTORY_SELECT) {
			file = parent.getFullPath();
			if (isAction) exit(IDOK);
			return true;
		}else{
			if (file.indexOf('*') != -1){
				mask = fixMask(file);
				setFile(parent);
			}else{
				File nf = afile.getNew(parent,file);
				if (file.indexOf(':') != -1 || file.startsWith("\\") || file.startsWith("/")){
					nf = afile.getNew(null,file);
				}
				if (nf.isDirectory())
					if ((type & NO_DIRECTORY_CHANGE) == 0) setFile(nf);
					else return false;
				else{
					if (!table.multipleSelected()){
						if (((type & FILE_MUST_EXIST) != 0) && isAction)
							if (existsAs(nf) == null){
								showError("Bad File","That file does not exist.",true);
								return false;
							}
						//setFile(nf);
						if (isAction && ((type & 3) != BROWSE)) {
							file = nf.getFullPath();
							exit(IDOK);
						}
					}else{
						if ((type & MULTI_SELECT) == 0){
							showError("Too Many Files","Please select one file only.",true);
							return false;
						}
						if (isAction && ((type & 3) != BROWSE)){
							file = nf.getFullPath();
							exit(IDOK);
						}
					}
					return true;
				}
			}
		}
		focusToFile();
		return false;
}

boolean amExiting = false;

//===================================================================
public boolean exit(int value)
//===================================================================
{
	if ((type & EMBEDDED) != 0) {
		close(value);
		return false;
	}else
		return super.exit(value);
}
//===================================================================
public void close(int value)
//===================================================================
{
	if (value == IDOK){
		File ret = afile.getNew(file);
		int [] all = table.getSelectedIndexes();
		if (all.length > 1){
			chosenDirectory = files.parent.getFullPath();
			chosenFiles = new Vector();
			for (int i = 0; i<all.length; i++)
				chosenFiles.add(files.files[all[i]]);
			File f = afile.getNew(files.parent,files.files[all[0]]);
			if (f != null) {
				file = f.getFullPath();
				ret = f;
			}
		}else{
			if (!ret.exists()){
				int id = file.indexOf('.');
				if (id == -1 && defaultExtension != null){
					file = file+"."+defaultExtension;
				}else if (id != -1 && id == file.length()-1){
					file = file.substring(0,id);
				}
				ret = afile.getNew(file);
			}
			File p = ret.getParentFile();
			if (p == null) chosenDirectory = "/";
			else chosenDirectory = p.getFullPath();
			chosenFiles = new Vector();
			chosenFiles.add(ret.getFileExt());
		}
		putInHistory(history,ret,historyDirectoriesOnly);
		setHistory(history);
		storeHistory(history,persistentHistoryKey);
		//ewe.sys.Vm.debug("Choosing: "+chosenDirectory);
		if (fcp != null){
			fcp.set(fcp.CHOSEN_FILE,getChosenFile());
			fcp.set(fcp.CHOSEN_FILES,getAllChosen());
		}
	}
	if ((type & EMBEDDED) != 0){
		if (value == IDOK) notifyAction();
	}else
		super.close(value);
}

//-------------------------------------------------------------------
private static boolean storeHistory(Vector history,String persistentHistoryKey)
//-------------------------------------------------------------------
{
	if (persistentHistoryKey != null){
		String s = "";
		for (int i = 0; i<history.size(); i++){
			if (i != 0) s += "\n";
			s += File.fixupPath(history.get(i).toString());
		}
		try{
			ewe.io.IO.saveConfigInfo(s,"FileHistory\\"+persistentHistoryKey,ewe.io.IO.SAVE_IN_REGISTRY);
		}catch(Exception e){
			return false;
		}
	}
	return true;
}

//-------------------------------------------------------------------
private static void putInHistory(Vector history,File ret,boolean onlyDir)
//-------------------------------------------------------------------
{
	if (onlyDir)
		if (!ret.isDirectory())
				ret = ret.getParentFile();
	if (history != null){
		String where = File.fixupPath(ret.getFullPath());
		boolean found = false;
		for (int i = 0; i<history.size(); i++){
			if (where.equalsIgnoreCase(toFullPath(history.get(i)))){
				found = true;
				break;
			}
		}
		if (!found) history.add(0,ret);
		while(history.size() > historySize && history.size() > 0)
			history.del(history.size()-1);
	}
}

/**
 * Add a file or directory to the saved history.
 * @param historyKey the name of the history key.
 * @param fileOrDirectory a file or directory.
 * @return true if it saved successfully, false if not.
 */
//===================================================================
public static boolean addToSavedHistory(String historyKey,File fileOrDirectory)
//===================================================================
{
	try{
		if (fileOrDirectory == null) return true;
		Vector v = new Vector(mString.split(ewe.io.IO.getConfigInfo("FileHistory\\"+historyKey),'\n'));
		putInHistory(v,fileOrDirectory,true);
		return storeHistory(v,historyKey);
	}catch(Exception e){
		return false;
	}
}
//===================================================================
public boolean canExit(int value)
//===================================================================
{
	amExiting = true;
	if (value == IDCANCEL || ((type & 3) != SAVE) || ((type & NO_CONFIRM_OVERWRITE) != 0)) return true;
	final File f = existsAs(afile.getNew(file));
	if (f == null) return true;
	new ewe.sys.TaskObject(){
		protected void doRun(){
			int ret = new MessageBox("Confirm Overwrite","Overwrite existing file?\n"+f.getFileExt(),MBYESNOCANCEL).execute();
			if (ret == IDCANCEL) close(IDCANCEL);
			else if (ret == IDYES) close(IDOK);
			else {
				file = f.getFileExt();
				focusToFile();
				amExiting = false;
			}
		}
	}.startTask();
	return false;
}

//-------------------------------------------------------------------
void fileAction()
//-------------------------------------------------------------------
{
	File nf = afile.getNew(parent,file);
	boolean updated = false;
	if (file.indexOf(':') != -1 || file.startsWith("\\") || file.startsWith("/")){
		if (file.endsWith(":")) file += "/";
		nf = afile.getNew(null,file);
		file = nf.getFullPath();
		updated = true;
	}
	if (nf.isDirectory() || file.indexOf('*') != -1) {
		setFile(nf);
		return;
	}
	if (file.indexOf('*') != -1) return;
	if (existsAs(nf) == null)
		if ((type & FILE_MUST_EXIST) != 0){
			showError("Bad File","That file does not exist.",true);
			return;
		}
	if (!updated) file = afile.getNew(parent,file).getFullPath();
	if ((type & 3) != BROWSE)
		exit(IDOK);
	if ((type & LAUNCHER_TYPE) == LAUNCHER_TYPE)
		runFile(nf);
}
//===================================================================
public void dirUp()
//===================================================================
{
	if (parent != null){
		File up = parent.getParentFile();
		if (up != null) setFile(up);
	}
/*
	int l = levels.size();
	if (l <= 1) return;
	setFile((File)levels.get(l-2));
*/
}
//===================================================================
public void action(ewe.reflect.FieldTransfer ft,Editor f)
//===================================================================
{
	if (amExiting) return;
	if (ft.fieldName.equals("dirUp")){
		dirUp();
	}else if (ft.fieldName.equals("acceptSelection")){// || ft.fieldName.equals("file")){
		if (!newFileInfo(true)){
			if ((type & ACCEPT_ANY) != 0){
				file = afile.getNew(parent,file).getFullPath();
				exit(IDOK);
			}
		}
	}else if (ft.fieldName.equals("file")){
		f.fromControls(ft.fieldName);
		fileAction();
	}else if (ft.fieldName.equals("newFolder")){
		doNewFolder(f);
	}else if (ft.fieldName.equals("fileTable")){
		fileAction();
	}else if (ft.fieldName.equals("goBack")){
		if (lastInHistory > 0)
			setFile(afile.getNew(backForward.get(lastInHistory-1).toString()),true,-1);
	}else if (ft.fieldName.equals("goForward")){
		if (lastInHistory < backForward.size()-1)
			setFile(afile.getNew(backForward.get(lastInHistory+1).toString()),true,+1);
	}
}
//===================================================================
private File getNewFileFolder(Editor f,String type)
//===================================================================
{
	String newDir = new InputBox("New "+type).input(getFrame(),"New"+type,20);
	if (newDir == null) return null;
	File dir = afile.getNew(parent,newDir);
	if (dir.exists()){
		showError(type+" already exists.",true);
		return null;
	}
	return dir;
}
//===================================================================
void doNewFolder(Editor f)
//===================================================================
{
	File dir = getNewFileFolder(f,"Folder");
	if (dir == null) return;
	if (!dir.createDir()){
		showError("The folder could not be created!",true);
		return;
	}
	setFile(parent);
	if (/*((type & 3) == DIRECTORY_SELECT) && */((type & DIRECTORY_TREE) != 0)){
		tree.getTreeTableModel().reExpandSelectedNode();
	}
}
//===================================================================
void doNewFile(Editor f)
//===================================================================
{
	File file = getNewFileFolder(f,"File");
	if (file == null) return;
	ewe.io.Stream s = file.getOutputStream();
	if (s == null){
		showError("The file could not be created!",true);
		return;
	}
	s.close();
	setFile(parent);
}

//===================================================================
public void switchMultiSelect(boolean on)
//===================================================================
{
	if (toolMenu != null){
		MenuItem mi = toolMenu.getMenu().findItem("Multiple Select",true);
		if (mi == null) return;
		if (on) mi.modifiers |= mi.Checked;
		else mi.modifiers &= ~mi.Checked;
		table.multiSelect = on;
	}
	/*
	if (!on && table.getSelectedIndexes().length > 1) {
		table.clearSelectedCells(null);
		table.repaintNow();
	}
	*/
}

//===================================================================
public void fieldEvent(ewe.reflect.FieldTransfer ft,Editor f,Object ev)
//===================================================================
{
	if (ft.fieldName.equals("fileTable")){
		if (ev instanceof TableEvent){
			TableEvent te = (TableEvent)ev;
			if (te.type == te.CELL_CLICKED){
				int idx = files.getFileIndex(te.row,te.col);
				if (idx != -1){
					File nf = afile.getNew(parent,found[idx]);
					boolean selectedByKey = ((te.flags & TableEvent.FLAG_SELECTED_BY_ARROWKEY) != 0);
					if (nf.isDirectory() && !table.multiSelect && !selectedByKey) setFile(nf);
					else{
						file = found[idx];
						f.toControls("file");
						if ((type & QUICK_SELECT) != 0 && ((type & 3) != BROWSE) && !nf.isDirectory() && !selectedByKey) {
							file = nf.getFullPath();
							exit(IDOK);
						}
						focusToFile();
						newFileSelected(nf);
						//table.addToSelection(te.row,te.col);
						//table.repaintCell(te.row,te.col);
					}
				}
			}else if (te.type == te.CELL_DOUBLE_CLICKED){
				fileAction();
			}
		}/*
	}else if (ft.fieldName.equals("drives")){
		if (ev instanceof MenuEvent){
			MenuEvent me = (MenuEvent)ev;
			if (me.type == me.SELECTED){

			}
		}*/
	}else if (ft.fieldName.equals("dirTree")){
		if (ev instanceof TreeEvent){
			TreeEvent te = (TreeEvent)ev;
			File f2 = ((FileTree)tree.getTableModel()).getFileAt(te.selectedLine);
			if (f2 != null) {

				//ewe.sys.Vm.debug("Selected: "+f2);
				setFile(f2);
			}
		}
		/*
	}else if (ft.fieldName.equals("toolField")){
		if (ev instanceof MenuEvent){
			MenuEvent me = (MenuEvent)ev;
			if (me.type == me.SELECTED){
				String which = ((MenuItem)me.selectedItem).label;
				if (toolMenu.getMenu().checkOnlyOne(viewChoices,which,true)){
					files.setShowDetails(which.equals(viewChoices[0]));
					lastOptions.viewOptions = (which.equals(viewChoices[0])) ? 0 : FileChooserOptions.VIEW_NAME_ONLY;
					table.update(true);
				}else if (toolMenu.getMenu().checkOnlyOne(sortChoices,which,true)){
					if (which.equals(sortChoices[0])) sortBy = 0;
					else if (which.equals(sortChoices[1])) sortBy = File.LIST_BY_DATE;
					else if (which.equals(sortChoices[2])) sortBy = File.LIST_BY_TYPE;
					else if (which.equals(sortChoices[3])) sortBy = File.LIST_BY_SIZE;
					lastOptions.sortOptions = sortBy | descending;
					setFile(parent);
				}else if (which.equals("Descending")){
					MenuItem mi = toolMenu.getMenu().findItem(which,true);
					mi.modifiers ^= mi.Checked;
					if ((mi.modifiers & mi.Checked) != 0) descending = File.LIST_DESCENDING;
					else descending = 0;
					lastOptions.sortOptions = sortBy | descending;
					setFile(parent);
				}else if (which.equals("Folder")){
					doNewFolder(f);
				}
			}
		}*/
	}
}

//===================================================================
public static void main(String args[])
//===================================================================
{
	Vm.startEwe(args);
	FileChooser fc = new FileChooser(BROWSE|EXPLORER_TYPE|DIRECTORY_TREE,baseDir);
	fc.exitSystemOnClose = true;
	fc.show();
}
//##################################################################
}
//##################################################################
