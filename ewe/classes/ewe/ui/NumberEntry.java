package ewe.ui;
import ewe.sys.Convert;

//##################################################################
public class NumberEntry extends TextDisplayButton{
//##################################################################

/**
This will return, on a device with a keyboard, an mInput that allows only
numeric entry, while, on a device without a keyboard, a NumberEntry that allows
entry as text.
* @param extraChars Extra input characters allowed in addition to '0' to '9'. If this is null
* only number keys will be allowed.
* @param passwordCharacter if this is 0 the no password hiding of the text will be done. If
it is not 0, then the character provided will be used as the password character (use '*' as default).
* @return a Control that is the best one for use for numeric entry on the current device.
*/
//===================================================================
public static Control getBestEntryForNumericText(final String extraChars,char passwordCharacter)
//===================================================================
{
	if (Gui.hasKeyboard){
		mInput ret = new mInput();
		ret.validator = new InputValidator(){
			char[] ec = ewe.sys.Vm.getStringChars(extraChars);
			public boolean isValidKeyPress(KeyEvent ev){return true;}
			public boolean isValidText(String text){
				char[] c = ewe.sys.Vm.getStringChars(text);
				for (int i = 0; i<c.length; i++){
					if (c[i] >= '0' && c[i] <= '9') continue;
					if (ec == null) return false;
					boolean found = false;
					for (int cc = 0; cc<ec.length; cc++)
						if (c[i] == ec[cc]) found = true;
					if (!found) return false;
				}
				return true;
			}
		};
		if (passwordCharacter != 0){
			ret.isPassword = true;
			ret.passwordCharacter = passwordCharacter;
		}
		return ret;
	}else{
		NumberEntry ret = new NumberEntry(ENTER_AS_TEXT,extraChars);
		if (passwordCharacter != 0){
			ret.isPassword = true;
			ret.passwordCharacter = passwordCharacter;
		}
		return ret;
	}
}
/**
The options selected for the NumberEntry.
**/
public int myOptions;

{
	columns = 10;
}
//public static int popupInputWidth

public static final int ALLOW_LEADING_ZEROS = 0x1;
public static final int ALLOW_BLANK = 0x2;
public static final int ENTER_AS_TEXT = (ALLOW_LEADING_ZEROS|ALLOW_BLANK);
//public static final int

//-------------------------------------------------------------------
protected String validateFinalText(String newText, String oldText)
//-------------------------------------------------------------------
{
	return newText;
}
//-------------------------------------------------------------------
protected String fixValue(String text,String oldText)
//-------------------------------------------------------------------
{
	text = text.trim();
	if (((myOptions & ALLOW_BLANK) == 0) && text.length() == 0) text = "0";
	if (((myOptions & ALLOW_LEADING_ZEROS) == 0) && text.length() != 0)
		text = Convert.toString(Convert.toInt(text));
	return text;
}
/**
Override this to validate the final text. Within this method you can call
setValue() on the InputKeyPad to change its value. If this method returns
false the InputKeyPad used will not exit.
*/
//-------------------------------------------------------------------
protected boolean canExitWithValue(String text, InputKeyPad kp)
//-------------------------------------------------------------------
{
	return true;
}
//===================================================================
public NumberEntry()
//===================================================================
{
	this(0,null);
}
//===================================================================
public NumberEntry(int options)
//===================================================================
{
	this(options,null);
}
//===================================================================
public NumberEntry(final int options, final String extraChars)
//===================================================================
{
	myOptions = options;
	new NumericKeyPad(){
		{
			if (extraChars != null){
				int c = 1;
				for (int i = 0; i<extraChars.length(); i++){
					addKey(extraChars.substring(i,i+1));
					c++;
					if ((c % 3) == 0) keys.endRow();
				}
			}
		}
		//-------------------------------------------------------------------
		protected String fixValue(String value,String oldText)
		//-------------------------------------------------------------------
		{
			return NumberEntry.this.fixValue(value, oldText);
		}
		//-------------------------------------------------------------------
		protected void doClear()
		//-------------------------------------------------------------------
		{
			setValue((myOptions & ALLOW_BLANK) == 0 ? "0" : "");
		}
		//-------------------------------------------------------------------
		protected boolean canExitWithValue(String value)
		//-------------------------------------------------------------------
		{
			return NumberEntry.this.canExitWithValue(value,this);
		}
		/*
		//-------------------------------------------------------------------
		protected void startingInput(Control c)
		//-------------------------------------------------------------------
		{
			super.startingInput(c);
			if (inputWidth > 0)
				curText.setPreferredSize(inputWidth,-1);
		}
		*/
	}.attachTo(this);
}
//##################################################################
}
//##################################################################

