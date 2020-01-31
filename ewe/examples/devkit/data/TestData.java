/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.data;
import ewe.ui.*;
import ewe.data.*;

//##################################################################
public class TestData extends LiveObject{
//##################################################################

public FontChooser font = new FontChooser();
public String firstNames = "Michael Louis";
public String lastName = "Brereton";
public int age = 36;

public void fieldChanged(String field, Editor ed)
{
	super.fieldChanged(field,ed);
	ewe.sys.Vm.debug(toString());
}

public String toString()
{
	return lastName+", "+firstNames+" = "+age+", Font: "+font.toFont();
}

//##################################################################
}
//##################################################################
