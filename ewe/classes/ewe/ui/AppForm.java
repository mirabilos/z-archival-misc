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
package ewe.ui;
import ewe.fx.*;
import ewe.util.*;
import ewe.sys.*;
import ewe.data.*;
/**
An AppForm is usually the best Form to use as your application's main Form. Especially
when running on a mobile/PDA device. It provides an easy way to put together Menu Bars, Toolbars,
and TabbedPanels in a way that is "device aware". That is to say, if your application is
running on a PocketPC, it will place these controls on the bottom, in line with the
SIP button (which is placed on the bottom right). When running on other platforms, these
controls will be placed on the top.<p>

After creating the AppForm add your controls, if any, to the "menus", "tools", "tabs" and
"data" containers as necessary.
**/
//##################################################################
public class AppForm extends Editor{
//##################################################################

/**
* Place  your menus here.
**/
public MenuBar menus;
/**
* Place your tool buttons here.
**/
public CellPanel tools;
/**
* This is the tabbed panel (if selected in the constructor).
**/
public mTabbedPanel tabs;
/**
* Put the data in here.
**/
public CellPanel data;
/**
* This will be used to determine which menu item was chosen.
**/
public MenuItem chosenMenuItem;

protected boolean isAutoExpandingTabs = false;

/**
* Create a new AppForm() with no tabbed panels.
**/
//===================================================================
public AppForm() {this(false,false);}
//===================================================================
/**
* Create an AppForm that does not use tabs and is setup to edit a particular Object.
* @param objectToEdit The object that will be edited.
*/
//===================================================================
public AppForm(Object objectToEdit)
//===================================================================
{
	this(objectToEdit,false,false);
}
/**
 * Create an AppForm that can optionally use tabs. If it does you can optionally specify auto-expanding tabs.
 * The menus and tools controls will be created and automatically placed in the Form.
 * @param useTabs set this true if you want to use tabs.
 * @param autoExpandingTabs set this true if you want the tabs to be auto expanding.
 */
//===================================================================
public AppForm(boolean useTabs,boolean autoExpandingTabs)
//===================================================================
{
	this(null,useTabs,autoExpandingTabs);
}
/**
 * Create an AppForm that can optionally use tabs. If it does you can optionally specify auto-expanding tabs.
 * The menus and tools controls will be created and automatically placed in the Form.
* @param objectToEdit The object that will be edited - this can be null.
 * @param useTabs set this true if you want to use tabs.
 * @param autoExpandingTabs set this true if you want the tabs to be auto expanding.
 */
//===================================================================
public AppForm(Object objectToEdit,boolean useTabs,boolean autoExpandingTabs)
//===================================================================
{
	this(objectToEdit,useTabs,autoExpandingTabs,true);
}
/**
 * Create an AppForm that can optionally use tabs. If it does you can optionally specify auto-expanding tabs.
* @param objectToEdit The object that will be edited - this can be null.
 * @param useTabs set this true if you want to use tabs.
 * @param autoExpandingTabs set this true if you want the tabs to be auto expanding.
* @param placeMenuAndTools if this is true the menu and tools will be placed in the Form,
otherwise they will be created but not added in - you must add it in somewhere yourself.
*/
//===================================================================
public AppForm(Object objectToEdit,boolean useTabs,boolean autoExpandingTabs,boolean placeMenuAndTools)
//===================================================================
{
	isAutoExpandingTabs = autoExpandingTabs;
	if (objectToEdit != null)
		objectClass = ewe.reflect.Reflect.getForObject(objectToEdit);
	menus = new MenuBar();
	if (!placeMenuAndTools) tools = new CellPanel();
	if (useTabs){
		Control [] c = addTabbedPanel(autoExpandingTabs);
		tabs = (mTabbedPanel)c[0];
		tabs.cardPanel.autoScroll = false;
		data = (CellPanel)c[1];
		tabs.cardPanel.focusOnHide = data;
		if (placeMenuAndTools)
			tools = tabs.getExtraControls(false);
		tabs.getExtraControls(true).addLast(menus);
		tabs.dontExpandTabs = true;
	}else{
		CellPanel [] c = addToolbar();
		if (placeMenuAndTools){
			c[0].addNext(menus).setCell(DONTSTRETCH);
			c[0].addNext(tools = new CellPanel()).setCell(DONTSTRETCH);
		}
		data = c[1];
	}
	tools.defaultAddToMeCellConstraints = VSTRETCH;
	addField(menus,"chosenMenuItem");
	getProperties().set("EditorContents",data);
}

static String [] toolImages, toolCommands;
/**
* The order these come in are "New","Open","Save","Save As","Exit" (with a separator MenuItem in
* between "Save As" and "Exit".
**/
//===================================================================
public static void getStandardFileCommands(ewe.util.Vector menuItems,ewe.util.Vector toolButtons,String documentType,Editor ed)
//===================================================================
{
	if (documentType == null) documentType = "";
	else documentType = " "+documentType;

	if (toolImages == null){
		toolImages = ewe.util.mString.split("new|open|save|saveas|-|exit");
		toolCommands = ewe.util.mString.split("New$n|Open$o|Save$s|Save As$a|-|Exit$x");
		for (int i = 0; i<toolImages.length; i++)
			toolImages[i] = "ewe/"+toolImages[i]+"small.bmp";
	}

	for (int i = 0; i<6; i++){
		String s = toolCommands[i];
		if (menuItems != null) {
			MenuItem item = i == 4 ? new MenuItem("-") : new MenuItem(s,toolImages[i],Color.White);
			menuItems.add(item);
		}
		if (toolButtons != null)
			if (i != 4) {
				String s2 = s.substring(0,s.length()-2);
				mButton mb = new mButton(null,toolImages[i],Color.White);
				if (ed != null) ed.addField(mb,s2);
				mb.setToolTip(new IconAndText((IImage)ImageCache.cache.getValue(toolImages[i],null),
				i == 3 ? "Save"+documentType+" As" : s2+(i == 5 ? "" : documentType),null));
				toolButtons.add(mb);
			}
	}
}
public static final int SHOW_NEW_BUTTON = 0x1;
public static final int SHOW_OPEN_BUTTON = 0x2;
public static final int SHOW_SAVE_BUTTON = 0x4;
public static final int SHOW_SAVE_AS_BUTTON = 0x8;
public static final int SHOW_EXIT_BUTTON = 0x10;


/**
 * Create a tool button (one with an icon only, no text label) and optionally add it to the "tools"
 * bar.
 * @param fieldName The name of the field for the button. If this is not null it will be added
 * as a field of this AppForm. If it is null you will have to add it somewhere else.
 * @param icon The icon for the button.
 * @param toolTip The tool tip for the button.
 * @param addToTools if this is true and "tools" is not null this will cause it to be added to tools.
 * @return the button created.
 */
//===================================================================
public mButton addToolButton(String fieldName,IImage icon,String toolTip,boolean addToTools)
//===================================================================
{
	mButton mb = new mButton(icon);
	if (fieldName != null) addField(mb,fieldName);
	if (addToTools && tools != null) tools.addNext(mb);
	mb.setToolTip(new IconAndText(icon,toolTip,null));
	return mb;
}
/**
 * Create a tool button (one with an icon only, no text label) and add it to the "tools"
 * bar.
* @param iconName The name of the icon. The ImageCache will be searched for it first.
* @param maskOrColor An optional mask Image name or transparent color for the image.
* @param toolTip the tip for the button.
* @return the created mButton.
*/
//===================================================================
public mButton addToolButton(String iconName,Object maskOrColor,String toolTip)
//===================================================================
{
	return addToolButton(null,ImageCache.cache.get(iconName,maskOrColor),toolTip,true);
}
//===================================================================
public Card addExpandingTool(Control tool, String iconName, Object maskOrColor, String tabName) throws IllegalStateException
//===================================================================
{
	return addExpandingTool(tool, ImageCache.cache.get(iconName,maskOrColor), tabName);
}

//===================================================================
public Card addExpandingTool(Control tool, IImage icon, String tabName) throws IllegalStateException
//===================================================================
{
	if (!isAutoExpandingTabs || tabs == null) throw new IllegalStateException();
	Card c = tabs.addCard(tool,tabName,null);
	c.iconize(icon);
	return c;
}
/**
 * This creates the menu but does NOT add it to the Menu bar. It also places the requested tool buttons
	 in the toolLocation CellPanel.
 * @param editor The Editor being added to.
 * @param toolButtons A set of SHOW_XXX_BUTTON values ORed together.
 * @param toolLocation The CellPanel to hold the tool buttons.
 * @param documentType The name of the document.
 * @return The Menu that you can add to the AppForms menu bar (or place it elsewhere).
 */
//===================================================================
public static Menu setupStandardFileCommands(Editor editor,int toolButtons,CellPanel toolLocation,String documentType)
//===================================================================
{
	Vector m = new Vector();
	Vector b = new Vector();
	getStandardFileCommands(m,b,documentType,editor);
	Menu menu = new Menu((MenuItem [])m.toArray(new MenuItem[6]),"File");
	int mask = 1;
	for (int i = 0; i<5; i++, mask <<= 1)
		if ((toolButtons & mask) != 0)
			toolLocation.addNext((Control)b.get(i)).setCell(DONTSTRETCH);
	return menu;
}

/**
 * This creates the menu but does NOT add it to the Menu bar. It also places the requested tool buttons
	 in the toolLocation CellPanel.
 * @param toolButtons A set of SHOW_XXX_BUTTON values ORed together.
 * @param toolLocation The CellPanel to hold the tool buttons.
 * @param documentType The name of the document.
 * @return The Menu that you can add to the AppForms menu bar (or place it elsewhere).
 */
//===================================================================
public Menu setupStandardFileCommands(int toolButtons,CellPanel toolLocation,String documentType)
//===================================================================
{
	return setupStandardFileCommands(this,toolButtons,toolLocation,documentType);
}
/**
 * This creates the file menu and adds it to the "menus" menu bar, and places requested tools in the
 * tools tool bar.
 * @param toolButtons A set of SHOW_XXX_BUTTON values ORed together.
 * @param documentType The name of the document.
 * @return A PullDownMenu representing the File menu within the toolbar.
 */
//===================================================================
public PullDownMenu addStandardFileMenu(int toolButtons,String documentType)
//===================================================================
{
	return menus.addMenu(setupStandardFileCommands(toolButtons,tools,documentType),"File");
}
/**
 * This creates the file menu and adds it to the "menus" menu bar.
 * @return A PullDownMenu representing the File menu within the toolbar.
 */
//===================================================================
public PullDownMenu addStandardFileMenu()
//===================================================================
{
	return addStandardFileMenu(0,null);
}

//##################################################################
}
//##################################################################

