package ewe.sys;
import ewe.util.ByteArray;
import ewe.util.ByteEncodable;
import ewe.util.ByteDecodable;
import ewe.math.BigDecimal;
import ewe.data.DataObject;
/**
* A Decimal is a mutable version of BigDecimal. It contains a BigDecimal within itself which
* you can access and replace with a new value if needed.
**/
//##################################################################
public class Decimal extends DataObject implements Value, ByteEncodable, ByteDecodable{
//##################################################################
private BigDecimal bd;

//===================================================================
public Object getCopy()
//===================================================================
{
	return new Decimal(bd);
}
//===================================================================
public void copyFrom(Object other)
//===================================================================
{
	if (other instanceof Decimal)
		bd = ((Decimal)other).bd;
	super.copyFrom(other);
}
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (!(other instanceof Decimal)) return super.compareTo(other);
	return bd.compareTo(((Decimal)other).bd);
}
//===================================================================
public Decimal()
//===================================================================
{
	this(BigDecimal.valueOf(0));
}
//===================================================================
public Decimal(double value)
//===================================================================
{
	this(value == 0 ? BigDecimal.valueOf(0) : new BigDecimal(value));
}
//===================================================================
public Decimal(BigDecimal bd)
//===================================================================
{
	setBigDecimal(bd);
}
//===================================================================
public Decimal(String str)
//===================================================================
{
	fromString(str);
}
//===================================================================
public String toString()
//===================================================================
{
	return bd.toString();
}
//===================================================================
public void fromString(String value)
//===================================================================
{
	bd = new BigDecimal(value);
}
//===================================================================
public BigDecimal getBigDecimal()
//===================================================================
{
	return bd;
}
//===================================================================
public void setBigDecimal(BigDecimal bd)
//===================================================================
{
	if (bd == null) bd = BigDecimal.valueOf(0);
	this.bd = bd;
}
//===================================================================
public int encodeBytes(ByteArray dest)
//===================================================================
{
	int need = bd.write(null,0);
	if (dest == null) return need;
	dest.makeSpace(dest.length,need);
	bd.write(dest.data,dest.length-need);
	return need;
}
//===================================================================
public int decodeBytes(byte[] source,int offset,int length)
//===================================================================
{
	bd = new BigDecimal(source,offset,length);
	return length;
}
//===================================================================
public Decimal setDouble(double value)
//===================================================================
{
	setBigDecimal(new BigDecimal(value));
	return this;
}

//##################################################################
}
//##################################################################

