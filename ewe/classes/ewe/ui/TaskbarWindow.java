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
package ewe.ui;

import ewe.fx.*;
import ewe.sys.*;

/**
* This is a window that you can use to place and react to an icon in the
* taskbar under Win32. The window will be invisible but can receive mouse press events
* when the icon is pressed. Override iconPressed() to handle the event (by default it
will bring the application to the front).
<p>
Note that all icons are specified as Objects and not any kind of Image or IImage. This
is because the icon must be a native icon as created by Image.toIcon(Image mask). This
method converts an image to a native icon using a mask Image. That mask Image <b>must</b> have
been loaded as a <b>monochrome .bmp file</b> otherwise this will not work correctly
under Win32.

**/
//##################################################################
public class TaskbarWindow extends Window{
//##################################################################

/**
* The icons are stored in here.
**/
public ewe.util.Hashtable icons = new ewe.util.Hashtable();

/**
* Call this to find out if the current platform supports showing an icon in
* the Taskbar.
**/
//===================================================================
public static boolean supportsTaskbar()
//===================================================================
{
	return (Gui.getGuiFlags() & Window.GUI_FLAG_HAS_TASKBAR) != 0 && !Vm.getPlatform().equalsIgnoreCase("java");
}
/**
* Create a new TaskbarWindow with a specific title. Note that the Window will be
* invisible.
**/
//===================================================================
public TaskbarWindow(String title)
//===================================================================
{
	create(new Rect(-1,-1,-1,-1),title,0,FLAG_IS_VISIBLE,null);
}
Form taskForm;
ImageControl imageDisplay;
mLabel tipDisplay;
/**
* This creates a new TaskbarWindow, but if Taskbar icons are not supported,
* then a window will be displayed that will display the icon and tip.
**/
//===================================================================
public TaskbarWindow(final String title,final Dimension imageSizeIfNotSupported,int textLengthIfNotSupported)
//===================================================================
{
	if (supportsTaskbar()) create(new Rect(-1,-1,-1,-1),title,0,FLAG_IS_VISIBLE,null);
	else {
		Form f = new Form(){
			public boolean canExit(int code){
				return false;
			}
			public void penReleased(Point p){
				iconPressed();
			}
		};
		f.windowFlagsToClear |= FLAG_HAS_CLOSE_BUTTON;
		taskForm = f;
		f.defaultTags.set(INSETS,new Insets(2,2,2,2));
		f.title = title;
		f.resizable = false;
		f.exitButtonDefined = true;
		f.addNext(imageDisplay = new ImageControl(new Image(imageSizeIfNotSupported.width,imageSizeIfNotSupported.height))).setCell(DONTSTRETCH);
		f.addNext(tipDisplay = new mLabel(" ")).setTextSize(textLengthIfNotSupported,1);
		f.modifyAll(PenTransparent,0,false);
		f.show();
	}
}
//===================================================================
public boolean close()
//===================================================================
{
	if (taskForm != null) {
		taskForm.close(0);
		return true;
	}else
		return super.close();
}
/**
 * This creates a TaskbarWindow, which then immediately shows the specified icon and tip.
 * @param title The title for the window. This will not be displayed since it is invisible.
 * @param pathOfImageFile image file (must be a ".bmp")
 * @param pathOfImageMask image mask (must be a monochrome ".bmp")
 * @param tip An optional tip to display.
 */
//===================================================================
public TaskbarWindow(String title,String pathOfImageFile,String pathOfImageMask,String tip)
//===================================================================
{
	this(title);
	addIcon("icon",pathOfImageFile,pathOfImageMask);
	setIconAndTip("icon",tip);
}


/**
 * Create a TaskbarWindow with a specific icon and tip.
 * Additional verbose
 * @param title The title to display.
 * @param icon The icon as returned by Image.toIcon(Image mask).
 * @param tip A tip to display along with the icon.
 */
//===================================================================
public TaskbarWindow(String title,Object icon,String tip)
//===================================================================
{
	this(title);
	addIcon("icon",icon);
	setIconAndTip("icon",tip);
}
//===================================================================
public boolean addIcon(String name,String pathOfImageFile,String pathOfImageMask)
//===================================================================
{
	if (imageDisplay != null) {
		return addIcon(name,new mImage(pathOfImageFile,pathOfImageMask));
	}
	Image im = new Image(pathOfImageFile);
	if (im.getWidth() == 0 || im.getHeight() == 0) return false;
	Image mask = new Image(pathOfImageMask);
	if (mask.getWidth() == 0 || mask.getHeight() == 0) {
		im.free();
		return false;
	}
	Object icon = im.toIcon(mask);
	im.free();
	mask.free();
	return addIcon(name,icon);
}

//-------------------------------------------------------------------
private Object toNativeIcon(Object icon)
//-------------------------------------------------------------------
{
	if (icon instanceof IImage) icon = PixelBuffer.toIcon((IImage)icon);
	return icon;
}
//===================================================================
public boolean addIcon(String name,Object icon)
//===================================================================
{
	if (icon == null) return false;
	icons.put(name,imageDisplay == null ? toNativeIcon(icon) : icon);
	return true;
}

//-------------------------------------------------------------------
private void doSetIconAndTip(Object ic,String tip,int options)
//-------------------------------------------------------------------
{
	if (imageDisplay != null){
		if (options == 0) options = OPTION_TASKBAR_ICON_MODIFY_ICON|OPTION_TASKBAR_ICON_MODIFY_TIP;
		if ((options & OPTION_TASKBAR_ICON_MODIFY_ICON) != 0)
			imageDisplay.setImage((IImage)ic);
		if ((options & OPTION_TASKBAR_ICON_MODIFY_TIP) != 0)
			tipDisplay.setText(tip);
		return;
	}
	TaskBarIconInfo info = new TaskBarIconInfo();
	info.nativeIcon = ic;
	info.tip = tip;
	if (ic == null && tip == null)
		setInfo(INFO_TASKBAR_ICON,null,null,options);
	else
		setInfo(INFO_TASKBAR_ICON,info,null,options);
	currentIcon = info;
}
//-------------------------------------------------------------------
protected Object currentIcon;
//-------------------------------------------------------------------
/**
 * Get an object representing the of the icon and tip currently displayed. If you change the icon being displayed
	after calling this, you can restore it by calling restoreIcon().
 * @return the name of the icon currently displayed.
 */
//===================================================================
public Object getIcon() {return currentIcon;}
//===================================================================
/**
 * Restore the icon according to the value that was returned by getIcon().
 * @param was The value returned by getIcon().
 */
//===================================================================
public void restoreIcon(Object was)
//===================================================================
{
	if (was == null) clearIcon();
	else {
		Window.TaskBarIconInfo ti = (Window.TaskBarIconInfo)was;
 		doSetIconAndTip(ti.nativeIcon,ti.tip,0);
	}
}
//===================================================================
public boolean setIcon(String name)
//===================================================================
{
	stopAnimation();
	Object icon = icons.get(name);
	if (icon == null) return false;
	doSetIconAndTip(icon,null,OPTION_TASKBAR_ICON_MODIFY_ICON);
	return true;
}
//===================================================================
public boolean setTip(String tip)
//===================================================================
{
	doSetIconAndTip(null,tip,OPTION_TASKBAR_ICON_MODIFY_TIP);
	return true;
}
//===================================================================
public boolean setIconAndTip(String name,String tip)
//===================================================================
{
	stopAnimation();
	Object icon = icons.get(name);
	if (icon == null) return false;
	doSetIconAndTip(icon,tip,0);
	return true;
}
//===================================================================
public boolean clearIcon()
//===================================================================
{
	currentIcon = null;
	stopAnimation();
	doSetIconAndTip(null,null,0);
	return true;
}

//===================================================================
public void stopAnimation()
//===================================================================
{
	if (animation != null) animation.stop(0);
	animation = null;
}
protected Handle animation;
/**
 * Animate the icon on the taskbar.
 * This will sequence the taskbar icons through the specified icons, pausing for the specified times in between.
	The icons must have been added using addIcon() before. If an animation is already running, this will be stopped.
 * @param iconsAndTimes Concatenation of icon names and pause times in milliseconds, separated by '|' characters. e.g.
	"icon1|250|icon2|500|icon3|100"
 * @return a Handle that you can use to stop the animation by calling stop() on the handle.
 */
//===================================================================
public Handle animate(final String iconsAndTimes,final String tip)
//===================================================================
{
	stopAnimation();

	return animation = new TaskObject(){
		protected void doRun(){
			String [] all = ewe.util.mString.split(iconsAndTimes);
			while(!shouldStop){
				for (int i = 0;i<all.length-1 && !shouldStop; i+=2){
					doSetIconAndTip(icons.get(all[i]),tip,0);
					sleep(Convert.toInt(all[i+1]));
				}
			}
		}
	}.startTask();
}


//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Image msk = new Image("solitaire/IconMask2.bmp");
	Image img = new Image("solitaire/Icon.bmp");
	Object icon = img.toIcon(msk);
	icon = PixelBuffer.toIcon(new Image("copy.png"));
	Window w = new TaskbarWindow("Solitaire",new Image("solitaire/appicon.png"),"This is solitaire"){
		public void iconPressed(){
			ewe.sys.Vm.debug("Ouch!");
		}
	};
	mThread.nap(10000);
	w.close();
	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################

