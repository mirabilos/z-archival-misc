package ewex.registry;
import ewe.ui.*;
import ewe.util.*;
import ewe.fx.*;
//##################################################################
public class RegistryTableModel extends GridTableModel{
//##################################################################

{
	verticalScrollUnit = 2;
	fillToEqualWidths = true;
	numCols = 1;
	numRows = 0;
	hasRowHeaders = hasColumnHeaders = false;
	cursorSize = new ewe.fx.Dimension(-1,2);
}

//===================================================================
public void setKey(RegistryKey myKey)
//===================================================================
{
	Grid values = new Grid();
	if (myKey == null){
		numRows = 0;
	}else{
		StringBuffer sb = new StringBuffer();
		for (int i = 0;;i++){
			Object value = myKey.getValue(i,sb);
			if (value == null) break;
			String name = sb.toString();
			if (name.length() == 0) name = "(Default Value)";
			values.add(name,true);
			if (value instanceof byte []){
				String out = "[";
				byte [] v = (byte [])value;
				int max = Math.min(v.length,8);
				for (int j = 0; j<max; j++){
					if (j != 0) out += " ";
					out += ewe.sys.Long.l1.set(v[j] & 0xff).toString(2,ewe.sys.Long.HEX|ewe.sys.Long.ZERO_FILL);
				}
				if (max != v.length) out += " ..";
				out += "]";
				values.add(out,true);
			}else
				values.add(value.toString(),true);
		}
	}
	setDataAndHeaders(values,null,null);
	if (table != null) {
		calculateSizes(table.getFontMetrics());
		table.update(true);
	}
}
Insets in = new Insets(4,4,4,4);
//===================================================================
public TableCellAttributes getCellAttributes(int row,int col,boolean isSelected,TableCellAttributes ta)
//===================================================================
{
	ta = super.getCellAttributes(row,col,isSelected,ta);
	ta.alignment = Control.LEFT;
	ta.anchor = Control.WEST;
	ta.fontMetrics = table.getFontMetrics();
	if ((row % 2) == 0) {
		FontMetrics fm = ta.fontMetrics;
		Font f = fm.getFont();
		ta.fontMetrics = table.getFontMetrics(new Font(f.getName(),Font.BOLD,f.getSize()));
	}
	return ta;
}
//===================================================================
public Insets getCellInsets(int row,int col,Insets insets)
//===================================================================
{
	return in;
}

//##################################################################
}
//##################################################################

