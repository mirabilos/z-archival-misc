/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.ui.Editor;
import ewe.ui.mInput;

//##################################################################
public class TextPalette extends Editor{
//##################################################################

public String value;
public TextPalette()
{
	addLast(addField(new mInput(),"value")).setCell(HSTRETCH);
}

//##################################################################
}
//##################################################################
