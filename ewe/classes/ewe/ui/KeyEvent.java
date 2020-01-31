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

/**
 * KeyEvent is a key press event.
 */
public class KeyEvent extends Event
{
/** The event type for a key press event. */
public static final int KEY_PRESS = 100;
public static final int KEY_RELEASE = 101;
/**
 * The key pressed or entered by other means (grafitti input). This
 * is either a normal character key (if the value is < 70000) or
 * one of the special keys defined in the IKeys interface.
 * @see IKeys
 */
public int key;

/**
This is used if a key or key-combination as pressed by the user has resulted
in the generation of a multiple character string. This value is placed in
text and the IKeys.MULTICHARACTER modifier will be set.
**/
public String text;
/**
This is used with the MULTICHARACTER modifier. It indicates how many, if any, of the last
set of characters already input should be erased before the new text is appended. If the
value is -1 this indicates that all characters should be erased.
**/
public int textReplacement = 0;
/**
 * The state of the modifier keys when the event occured. This is a
 * OR'ed combination of the modifiers present in the IKeys interface.
 * @see IKeys
 */
public int modifiers;

private static String [] theKeys = {
"PageUp","PageDown","Home","End",
"Up","Down","Left","Right",
"Ins","Enter","Tab","Bksp",
"Esc","Del","Menu","Cmnd"
};



/**
 * Convert specific control keys to their letter counterpart. For example the <TAB> key is converted to 'I'
 */
//===================================================================
public void controlToLetter()
//===================================================================
{
	if (key >= 1 && key <= 26) key = 'A'-1+key;
	else switch(key){
		case IKeys.BACKSPACE: key = 'H'; break;
		case IKeys.TAB: key = 'I'; break;
		case IKeys.ENTER: key = 'M'; break;
		case IKeys.ESCAPE: key = 'Z'; break;
	}
}
/**
 * Convert a KEYPAD_ type key into a standard key.
 */
//===================================================================
public void keypadToKey()
//===================================================================
{
	if (key >= IKeys.KEYPAD_0 && key <= IKeys.KEYPAD_DEL)
	switch(key){
	case IKeys.KEYPAD_0 : key = '0'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_1 : key = '1'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_2 : key = '2'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_3 : key = '3'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_4 : key = '4'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_5 : key = '5'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_6 : key = '6'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_7 : key = '7'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_8 : key = '8'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_9 : key = '9'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_POINT : key = '.'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_PLUS : key = '+'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_MINUS : key = '-'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_TIMES : key = '*'; modifiers &= ~IKeys.SPECIAL; break;
	case IKeys.KEYPAD_DIVIDE : key = '/'; modifiers &= ~IKeys.SPECIAL; break;

	case IKeys.KEYPAD_INS : key = IKeys.INSERT; break;
	case IKeys.KEYPAD_END : key = IKeys.END; break;
	case IKeys.KEYPAD_DOWN : key = IKeys.DOWN; break;
	case IKeys.KEYPAD_PAGE_DOWN : key = IKeys.PAGE_DOWN; break;
	case IKeys.KEYPAD_LEFT : key = IKeys.LEFT; break;
	case IKeys.KEYPAD_RIGHT : key = IKeys.RIGHT; break;
	case IKeys.KEYPAD_HOME : key = IKeys.HOME; break;
	case IKeys.KEYPAD_UP : key = IKeys.UP; break;
	case IKeys.KEYPAD_PAGE_UP : key = IKeys.PAGE_UP; break;
	case IKeys.KEYPAD_DEL : key = IKeys.DELETE; break;

	case IKeys.KEYPAD_ENTER : key = IKeys.ENTER; break;
	}
}
/**
 * Print a String representation of a key.
 * @param key A key as provided in a KeyEvent.
 * @return A String representation of the key.
 */
//===================================================================
public static final String toString(int key)
//===================================================================
{
	if (key >= IKeys.APP0 && key <= IKeys.APP15)
		return ((ewe.sys.Vm.getParameter(ewe.sys.Vm.VM_FLAGS) & ewe.sys.Vm.VM_FLAG_NO_KEYBOARD) != 0) ? "APP-"+(key-IKeys.APP0) : "F"+(1+key-IKeys.APP0);;
	if (key == IKeys.ACTION) return "Action";
	if (key >= IKeys.PAGE_UP && key <= IKeys.COMMAND) return theKeys[key-IKeys.PAGE_UP];
	if (key == 32) return "Space";
	return ""+(char)key;
}
/**
 * The produces a key code that can be used as a "Hot Key" for a Control.
 * @param modifier Any of the IKeys.SHIFT, IKeys.CONTROL, IKeys.ONLY ORed together.<p>
 * If none of these modifiers are used then when the character is pressed with ANY
	of SHIFT or CONTROL keys, the hot key will be activated. If IKeys.ONLY is specified
	then the character must be pressed ALONE for the hot key to be activated.
 * @param character The character for the key.
 * @return A key code that can be used as the hot key for a control.
 */
//===================================================================
public static final int toKey(int modifier,char character)
//===================================================================
{
	return toKey(modifier,((int)character)& 0xffff);
}
/**
 * The produces a key code that can be used as a "Hot Key" for a Control.
 * @param modifier Any of the IKeys.SHIFT, IKeys.CONTROL, IKeys.ONLY ORed together.<p>
 * If none of these modifiers are used then when the character is pressed with ANY
	of SHIFT or CONTROL keys, the hot key will be activated. If IKeys.ONLY is specified
	then the character must be pressed ALONE for the hot key to be activated.
 * @param key The key code for the key. You can use one of the IKeys key codes such as F1, HOME, etc.
 * @return A key code that can be used as the hot key for a control.
 */
//===================================================================
public static final int toKey(int modifier,int key)
//===================================================================
{
	return key |((modifier & (IKeys.INVISIBLE|IKeys.ONLY|IKeys.SHIFT|IKeys.CONTROL|IKeys.ALT)) << 24);
}
/**
 * Checks to see if a hot-key encoded key has a modifier. You would most likely not call this method directly.
 * @param key The hot-key encoded key code.
 * @return true if SHIFT or CONTROL has been pressed with the key.
 */
//===================================================================
public static final boolean hasModifier(int key) {return (key & ((IKeys.ONLY|IKeys.SHIFT|IKeys.CONTROL|IKeys.ALT) << 24)) != 0;}
//===================================================================
/**
 * Checks to see if the pressed hot-key should be considered to match the hot-key of a particular Control.
 * @param key The pressed key.
 * @param controlHotKey The assigned hot-key of a Control
 * @return true if the pressed key should be considered a match to the Control's hot-key.
 */
//===================================================================
public static final boolean isHotKey(int key,int controlHotKey)
//===================================================================
{
	if (hasModifier(controlHotKey)) return (key & ~((IKeys.ONLY|IKeys.INVISIBLE) << 24)) == (controlHotKey & ~((IKeys.ONLY|IKeys.INVISIBLE) << 24));
	else return (key & 0xffffff) == (controlHotKey & ~((IKeys.INVISIBLE) << 24));
}
/**
 * Checks to see if the key represented by this KeyEvent should be considered to match the hot-key of a particular Control.
 * @param controlHotKey The assigned hot-key of a Control
 * @return true if the pressed key should be considered a match to the Control's hot-key.
 */
//===================================================================
public final boolean isHotKey(int controlHotKey)
//===================================================================
{
	if (isHotKey(toKey(modifiers,key),controlHotKey)) return true;
	return isHotKey(toKey(modifiers,ewe.sys.Vm.getLocale().changeCase((char)key,false)),controlHotKey);
}
/**
* Convert this KeyEvent into a hot-key encoded key code.
**/
//===================================================================
public final int toKey()
//===================================================================
{
	return toKey(modifiers,key);
}
//===================================================================
public boolean isBackKey()
//===================================================================
{
	return key == IKeys.BACKSPACE;
}
//===================================================================
public boolean isActionKey()
//===================================================================
{
	return key == IKeys.ENTER || key == ' ';
}
//===================================================================
public boolean isMoveToNextControlKey(boolean moveForwards)
//===================================================================
{
	if (moveForwards)
		return (key == IKeys.NEXT_CONTROL || (key == IKeys.TAB && ((modifiers & IKeys.SHIFT) == 0)));
	else
		return (key == IKeys.PREV_CONTROL || (key == IKeys.TAB && ((modifiers & IKeys.SHIFT) != 0)));
}

//===================================================================
public boolean isCancelKey()
//===================================================================
{
	return key == IKeys.ESCAPE;
}

//===================================================================
public static int getBackKey(boolean forHotKey)
//===================================================================
{
	return IKeys.BACKSPACE;
}

//===================================================================
public static int getCancelKey(boolean forHotKey)
//===================================================================
{
	return IKeys.ESCAPE;
}
//===================================================================
public static int getActionKey(boolean forHotKey)
//===================================================================
{
	return IKeys.ENTER;
}

}

