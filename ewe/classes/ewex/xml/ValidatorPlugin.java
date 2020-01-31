/* ValidatorPlugin.java                                            NanoXML/Java
 *
 * $Revision: 1.3 $
 * $Date: 2002/01/04 21:03:29 $
 * $Name: RELEASE_2_2_1 $
 *
 * This file is part of NanoXML 2 for Java.
 * Copyright (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 */

package ewex.xml;


import ewe.io.Reader;
import ewe.io.IOException;
import ewe.util.Properties;


/**
 * ValidatorPlugin allows the application to insert additional validators into
 * NanoXML.
 *
 * @author Marc De Scheemaecker
 * @version $Name: RELEASE_2_2_1 $, $Revision: 1.3 $
 */
public class ValidatorPlugin
   implements IXMLValidator
{

   /**
    * The delegated.
    */
   private IXMLValidator delegated;


   /**
    * Initializes the plugin.
    */
   public ValidatorPlugin()
   {
      this.delegated = null;
   }


   /**
    * Cleans up the object when it's destroyed.
    */
   protected void finalize()
      throws Throwable
   {
      this.delegated = null;
      super.finalize();
   }


   /**
    * Returns the delegated.
    */
   public IXMLValidator getdelegated()
   {
      return this.delegated;
   }


   /**
    * Sets the delegated.
    *
    * @param delegated the delegated
    */
   public void setdelegated(IXMLValidator delegated)
   {
      this.delegated = delegated;
   }


   /**
    * Sets the parameter entity resolver.
    *
    * @param resolver the entity resolver.
    */
   public void setParameterEntityResolver(IXMLEntityResolver resolver)
   {
      this.delegated.setParameterEntityResolver(resolver);
   }


   /**
    * Returns the parameter entity resolver.
    *
    * @return the entity resolver.
    */
   public IXMLEntityResolver getParameterEntityResolver()
   {
      return this.delegated.getParameterEntityResolver();
   }


   /**
    * Parses the DTD. The validator object is responsible for reading the
    * full DTD.
    *
    * @param publicID       the public ID, which may be null.
    * @param reader         the reader to read the DTD from.
    * @param entityResolver the entity resolver.
    * @param external       true if the DTD is external.
    *
    * @throws ewe.lang.Exception
    *     if something went wrong.
    */
   public void parseDTD(String             publicID,
                        IXMLReader         reader,
                        IXMLEntityResolver entityResolver,
                        boolean            external)
      throws Exception
   {
      this.delegated.parseDTD(publicID, reader, entityResolver, external);
   }


   /**
    * Indicates that an element has been started.
    *
    * @param name       the name of the element.
    * @param systemId   the system ID of the XML data of the element.
    * @param lineNr     the line number in the XML data of the element.
    *
    * @throws ewe.lang.Exception
    *     if the element could not be validated.
    */
   public void elementStarted(String name,
                              String systemId,
                              int    lineNr)
      throws Exception
   {
      this.delegated.elementStarted(name, systemId, lineNr);
   }


   /**
    * Indicates that the current element has ended.
    *
    * @param name       the name of the element.
    * @param systemId   the system ID of the XML data of the element.
    * @param lineNr     the line number in the XML data of the element.
    *
    * @throws ewe.lang.Exception
    *     if the element could not be validated.
    */
   public void elementEnded(String name,
                            String systemId,
                            int    lineNr)
      throws Exception
   {
      this.delegated.elementEnded(name,systemId, lineNr);
   }


   /**
    * Indicates that an attribute has been added to the current element.
    *
    * @param key        the name of the attribute.
    * @param value      the value of the attribute.
    * @param systemId   the system ID of the XML data of the element.
    * @param lineNr     the line number in the XML data of the element.
    *
    * @throws ewe.lang.Exception
    *     if the attribute could not be validated.
    */
   public void elementAttributesProcessed(String     name,
                                          Properties extraAttributes,
                                          String     systemId,
                                          int        lineNr)
      throws Exception
   {
      this.delegated.elementAttributesProcessed(name, extraAttributes,
                                               systemId, lineNr);
   }


   /**
    * This method is called when the attributes of an XML element have been
    * processed.
    * If there are attributes with a default value which have not been
    * specified yet, they have to be put into <I>extraAttributes</I>.
    *
    * @param name            the name of the element.
    * @param extraAttributes where to put extra attributes.
    * @param systemId        the system ID of the XML data of the element.
    * @param lineNr          the line number in the XML data of the element.
    *
    * @throws ewe.lang.Exception
    *     if the element could not be validated.
    */
   public void attributeAdded(String key,
                              String value,
                              String systemId,
                              int    lineNr)
      throws Exception
   {
      this.delegated.attributeAdded(key, value, systemId, lineNr);
   }


   /**
    * Indicates that a new #PCDATA element has been encountered.
    *
    * @param systemId the system ID of the XML data of the element.
    * @param lineNr   the line number in the XML data of the element.
    *
    * @throws ewe.lang.Exception
    *     if the element could not be validated.
    */
   public void PCDataAdded(String systemId,
                           int    lineNr)
      throws Exception
   {
      this.delegated.PCDataAdded(systemId, lineNr);
   }


   /**
    * Throws an XMLValidationException to indicate that an element is missing.
    *
    * @param systemID           the system ID of the XML data of the element
    * @param lineNr             the line number in the XML data of the element
    * @param parentElementName  the name of the parent element
    * @param missingElementName the name of the missing element
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void missingElement(String systemID,
                              int    lineNr,
                              String parentElementName,
                              String missingElementName)
      throws XMLValidationException
   {
      XMLUtil.errorMissingElement(systemID, lineNr, parentElementName,
                                  missingElementName);
   }


   /**
    * Throws an XMLValidationException to indicate that an element is
    * unexpected.
    *
    * @param systemID              the system ID of the XML data of the
    *                              element
    * @param lineNr                the line number in the XML data of the
    *                              element
    * @param parentElementName     the name of the parent element
    * @param unexpectedElementName the name of the missing element
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void unexpectedElement(String systemID,
                                 int    lineNr,
                                 String parentElementName,
                                 String unexpectedElementName)
      throws XMLValidationException
   {
      XMLUtil.errorUnexpectedElement(systemID, lineNr, parentElementName,
                                     unexpectedElementName);
   }


   /**
    * Throws an XMLValidationException to indicate that an attribute is
    * missing.
    *
    * @param systemID      the system ID of the XML data of the element
    * @param lineNr        the line number in the XML data of the element
    * @param elementName   the name of the element
    * @param attributeName the name of the missing attribute
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void missingAttribute(String systemID,
                                int    lineNr,
                                String elementName,
                                String attributeName)
      throws XMLValidationException
   {
      XMLUtil.errorMissingAttribute(systemID, lineNr, elementName,
                                    attributeName);
   }


   /**
    * Throws an XMLValidationException to indicate that an attribute is
    * unexpected.
    *
    * @param systemID      the system ID of the XML data of the element
    * @param lineNr        the line number in the XML data of the element
    * @param elementName   the name of the element
    * @param attributeName the name of the unexpected attribute
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void unexpectedAttribute(String systemID,
                                   int    lineNr,
                                   String elementName,
                                   String attributeName)
      throws XMLValidationException
   {
      XMLUtil.errorUnexpectedAttribute(systemID, lineNr, elementName,
                                       attributeName);
   }


   /**
    * Throws an XMLValidationException to indicate that an attribute has an
    * invalid value.
    *
    * @param systemID       the system ID of the XML data of the element
    * @param lineNr         the line number in the XML data of the element
    * @param elementName    the name of the element
    * @param attributeName  the name of the attribute
    * @param attributeValue the value of the attribute
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void invalidAttributeValue(String systemID,
                                     int    lineNr,
                                     String elementName,
                                     String attributeName,
                                     String attributeValue)
      throws XMLValidationException
   {
      XMLUtil.errorInvalidAttributeValue(systemID, lineNr, elementName,
                                         attributeName, attributeValue);
   }


   /**
    * Throws an XMLValidationException to indicate that a #PCDATA element was
    * missing.
    *
    * @param systemID          the system ID of the XML data of the element
    * @param lineNr            the line number in the XML data of the element
    * @param parentElementName the name of the parent element
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void missingPCData(String systemID,
                             int    lineNr,
                             String parentElementName)
      throws XMLValidationException
   {
      XMLUtil.errorMissingPCData(systemID, lineNr, parentElementName);
   }


   /**
    * Throws an XMLValidationException to indicate that a #PCDATA element was
    * unexpected.
    *
    * @param systemID          the system ID of the XML data of the element
    * @param lineNr            the line number in the XML data of the element
    * @param parentElementName the name of the parent element
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void unexpectedPCData(String systemID,
                                int    lineNr,
                                String parentElementName)
      throws XMLValidationException
   {
      XMLUtil.errorUnexpectedPCData(systemID, lineNr, parentElementName);
   }


   /**
    * Throws an XMLValidationException.
    *
    * @param systemID       the system ID of the XML data of the element
    * @param lineNr         the line number in the XML data of the element
    * @param message        the error message
    * @param elementName    the name of the element (may be null)
    * @param attributeName  the name of the attribute (may be null)
    * @param attributeValue the value of the attribute (may be null)
    *
    * @throws ewex.xml.XMLValidationException
    *      of course :-)
    */
   public void validationError(String systemID,
                               int    lineNr,
                               String message,
                               String elementName,
                               String attributeName,
                               String attributeValue)
      throws XMLValidationException
   {
      XMLUtil.validationError(systemID, lineNr, message, elementName,
                              attributeName, attributeValue);
   }

}
