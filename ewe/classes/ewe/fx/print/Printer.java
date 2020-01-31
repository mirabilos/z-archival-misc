/*
 * Created on Jun 15, 2005
 *
 * Michael L Brereton - www.ewesoft.com
 *
 *
 */
package ewe.fx.print;

import ewe.sys.Handle;

/**
 * This represents an object that provides a Printable with pages and a PrintSurface
 * on which to print.
 */
//####################################################
public interface Printer {

	public Handle print(Printable printable, PageFormat format, PrintOptions options);

}
//####################################################
