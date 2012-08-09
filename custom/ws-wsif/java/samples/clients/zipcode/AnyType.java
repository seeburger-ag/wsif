/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package clients.zipcode;


import java.io.Serializable;

import java.lang.reflect.Array;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.xerces.dom.DocumentImpl;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


/**
 * This is the base class for all generated beans.
 */
public abstract class AnyType implements Serializable
{
  /**
   * This is the namespace of the type of element that this class serializes
   */
  protected String namespaceURI;

  /**
   * This is the local name of the type of element that this class serializes.
   */
  protected String localName = "default";

  /**
   * This keeps track of the parent bean.
   */
  protected AnyType parent;

  /**
   * This maps an attribute property URI to its value.
   */
  protected Map uriAttributeMap = new HashMap();

  /**
   * This maps an element property URI to its list of values.
   */
  protected Map uriElementMap = new HashMap();

  /**
   * This maps the one text property to its value.
   */
  protected Map uriTextMap = new HashMap();

  /**
   * This maps a property URI to the class that represents it.
   */
  protected Map uriClassMap = new HashMap();

  /**
   * This keeps an ordered list of all property URIs.
   */
  protected List uriList = new ArrayList();

  /**
   * This creates a generic bean instance.
   */
  public AnyType()
  {
    this.localName = "default";
  }

  /**
   * This  is called in the derived constructor to set up the metadata about elements.
   */
  protected void addElement(String uri, Class aClass)
  {
    List list = new ArrayList();
    uriElementMap.put(uri, list);
    uriClassMap.put(uri, aClass);
    uriList.add(uri);
  }

  /**
   * This returns a map from a String representing an element-based bean property URI to a list of values stored in that property.
   */
  public Map elements()
  {
    return uriElementMap;
  }

  /**
   * This  is called in the derived constructor to set up the metadata about attributes.
   */
  protected void addAttribute(String uri, Class aClass)
  {
    uriAttributeMap.put(uri, null);
    uriClassMap.put(uri, aClass);
    uriList.add(uri);
  }

  /**
   * This returns a map from a String representing an attribute-based bean property URI to the value stored in that property.
   */
  public Map attributes()
  {
    return uriAttributeMap;
  }

  /**
   * This  is called in the derived constructor to set up the metadata about text.
   */
  protected void addText(String uri, Class aClass)
  {
    uriTextMap.put(uri, null);
    uriClassMap.put(uri, aClass);
    uriList.add(uri);
  }

  /**
   * This returns a map from a String representing a text property URI to the value stored in that property.
   */
  public Map text()
  {
    return uriTextMap;
  }

  /**
   * This returns a map from a property URI to the class that implements it.
   */
  public Map uriToClassMap()
  {
    return uriClassMap;
  }

  /**
   * This returns an ordered list of all the properties.
   */
  public List properties()
  {
    return uriList;
  }

  /**
   * This returns the parent bean.
   */
  protected AnyType parent()
  {
    return parent;
  }

  /**
   * This sets the parent bean.
   */
  protected void changeParent(AnyType parent)
  {
    this.parent = parent;
  }

  /**
   * This returns the namespace of the type of the bean.
   */
  public String namespaceURI()
  {
    return namespaceURI;
  }

  /**
   * This sets the namespace of the type of the bean.
   */
  public void changeNamespaceURI(String namespaceURI)
  {
    this.namespaceURI = namespaceURI;
  }

  /**
   * This returns the local name of the type of the bean.
   */
  public String localName()
  {
    return localName;
  }

  /**
   * This sets the local name of the type of the bean.
   */
  public void changeLocalName(String localName)
  {
    this.localName = localName;
  }

  /**
   * This creates a default serialization of this bean and all it's children.
   */
  public Element createElement()
  {
    Document document = createDocument();
    Element result = createElement(document);
    document.appendChild(result);
    return result;
  }

  /**
   * This creates a serialization of this bean and all it's children using the specified type of element.
   */
  public Element createElement(String namespaceURI, String localName)
  {
    Document document = createDocument();
    Element result = createElement(document, namespaceURI, localName);
    document.appendChild(result);
    return result;
  }

  /**
   * This creates a serialization of this bean and all it's children using the specified document; 
   * the element is not added to the document.
   */
  public Element createElement(Document document)
  {
    Element result = createElement(document, namespaceURI, localName);
    return result;
  }

  /**
   * This creates a serialization of this bean and all it's children using the specified type of element and document; 
   * the element is not added to the document.
   */
  public Element createElement(Document document, String namespaceURI, String localName)
  {
    List namespaces = new ArrayList();
    Element result;
    if (namespaceURI == null)
    {
      result = document.createElementNS(null, localName);
    }
    else
    {
      namespaces.add(namespaceURI);
      result = document.createElementNS(namespaceURI, "Q1:" + localName);
    }
    populateTo(result, namespaces);
    for (ListIterator i = namespaces.listIterator(); i.hasNext(); )
    {
      String namespace = (String)i.next();
      result.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:Q" + i.nextIndex(), namespace);
    }
    return result;
  }

  /**
   * This is a helper function that parses the URI and then delegates.
   */
  protected Element createElement(Document document, String uri, List namespaces)
  {
    int index = uri.lastIndexOf("#");
    if (index == -1)
    {
      return  document.createElementNS(null, uri);
    }
    else
    {
      String namespaceURI = uri.substring(0, index);
      String localName = uri.substring(index + 1);
      int namespaceIndex = namespaces.indexOf(namespaceURI);
      if (namespaceIndex == -1)
      {
        namespaces.add(namespaceURI);
        namespaceIndex = namespaces.size() - 1;
      }
      return document.createElementNS(namespaceURI, "Q" + (namespaceIndex + 1) + ":" + localName);
    }
  }

  /**
   * This is a helper function that parses the URI and then delegates.
   */
  protected void createAttribute(Element element, String uri, String value, List namespaces)
  {
    int index = uri.lastIndexOf("#");
    if (index == -1)
    {
      createAttribute(element, null, uri, value, namespaces);
    }
    else
    {
      createAttribute(element, uri.substring(0, index), uri.substring(index + 1), value, namespaces);
    }
  }

  /** 
   * This is called to set the element specifed attribute to the given values.
   */
  protected void createAttribute(Element element, String namespaceURI, String localName, String value, List namespaces)
  {
    if (value == null)
    {
      element.removeAttributeNS(namespaceURI, localName);
    }
    else
    {
      if (namespaceURI == null)
      {
        element.setAttributeNS(null, localName, value);
      }
      else
      {
        int namespaceIndex = namespaces.indexOf(namespaceURI);
        if (namespaceIndex == -1)
        {
          namespaces.add(namespaceURI);
          namespaceIndex = namespaces.size() - 1;
        }
        element.setAttributeNS(namespaceURI, "Q" + (namespaceIndex + 1) + ":" + localName, value);
      }
    }
  }

  /**
   * This is called to create a document.
   */
  protected Document createDocument()
  {
    return new DocumentImpl();
  }

  /**
   * This is called to create a text node.
   */
  protected Text createText(Document document, String stringValue)
  {
    return document.createTextNode(stringValue);
  }

  /**
   * This returns a list of specified property values.
   */
  protected List basicGet(String uri)
  {
    List result = (List)uriElementMap.get(uri);
    return result;
  }

  /**
   * This sets the list of property objects.
   */
  protected void basicSet(String uri, List values)
  {
    List oldList = (List)uriElementMap.get(uri);
    if (oldList != null)
    {
      for (Iterator i = oldList.iterator(); i.hasNext(); )
      {
        orphan(uri, i.next());
      }
    }

    uriElementMap.put(uri, values);
    for (Iterator i = values.iterator(); i.hasNext(); )
    {
      adopt(uri, i.next());
    }
  }

  /**
   * This returns the specified property object.
   */
  protected Object basicGet(String uri, int index)
  {
    List list = (List)uriElementMap.get(uri);
    if (list != null)
    {
      if (index < list.size() )
      {
        return list.get(index);
      }
    }
    else if (index == 0)
    {
      Object result = uriAttributeMap.get(uri);
      if (result == null)
      {
        result = uriTextMap.get(uri);
      }
      return result;
    }

    return null;
  }

  /**
   * This set the specified property object.
   */
  protected void basicSet(String uri, int index, Object value)
  {
    Object oldValue = basicGet(uri, index);
    orphan(uri, oldValue);

    List list = (List)uriElementMap.get(uri);
    if (list != null)
    {
      if (index < list.size())
      {
        list.set(index, value);
      }
      else
      {
        list.add(value);
      }
    }
    else if (uriAttributeMap.containsKey(uri))
    {
      uriAttributeMap.put(uri, value);
    }
    else if (uriTextMap.containsKey(uri))
    {
      uriTextMap.put(uri, value);
    }
    adopt(uri, value);
  }

  /**
   * This is called to detach the value, if it is a bean.
   */
  protected void orphan(String uri, Object value)
  {
    if (value instanceof AnyType)
    {
      ((AnyType)value).changeParent(null);
    }
  }

  /**
   * This is called to attach the value, if it is a bean.
   */
  protected void adopt(String uri, Object value)
  {
    if (value instanceof AnyType)
    {
      AnyType anyType = (AnyType)value;
      anyType.changeParent(this);
      int i = uri.lastIndexOf("#");
      if (i == -1)
      {
        anyType.changeNamespaceURI(null);
        anyType.changeLocalName(uri);
      }
      else
      {
        anyType.changeNamespaceURI(uri.substring(0, i));
        anyType.changeLocalName(uri.substring(i + 1));
      }
      ((AnyType)value).changeParent(this);
    }
  }

  /**
   * This set the text content to the specified value.
   */
  public void basicSetText(String value)
  {
    uriTextMap.put(uriTextMap.keySet().iterator().next(), value);
  }

  /**
   * This is used to populate the given element with this bean's serialization.
   */
  protected void populateTo(Element element, List namespaces)
  {
    Document document = element.getOwnerDocument();

    // Handle all the attributes.
    //
    for (Iterator attributes = uriAttributeMap.entrySet().iterator(); attributes.hasNext(); )
    {
      Map.Entry entry = (Map.Entry)attributes.next();
      String attrURI = (String)entry.getKey();
      Object value = entry.getValue();
      createAttribute(element, attrURI, value == null ? null : convertValueToString(value), namespaces);
    }

    // Handle the remaining properties in order.
    //
    for (Iterator properties = properties().iterator(); properties.hasNext(); )
    {
      String propertyURI = (String)properties.next();
      List list = (List)uriElementMap.get(propertyURI);
      if (list != null)
      {
        for (Iterator values = list.iterator(); values.hasNext(); )
        {
          Object value = values.next();
          populateValueTo(value, (Class)uriClassMap.get(propertyURI), propertyURI, element, namespaces);
        }
      }
      else 
      {
        Object value  = uriTextMap.get(propertyURI);
        if (value != null)
        {
          element.appendChild(createText(document, convertValueToString(value)));
        }
      }
    }
  }

  /**
   * This is used to populate the value of the given propertyURI to the given element.
   */
  protected void populateValueTo(Object value, Class aClass, String propertyURI, Element element, List namespaces)
  {
    if (AnyType.class.isAssignableFrom(aClass))
    {
      Element childElement = ((AnyType)value).createElement(element.getOwnerDocument(), propertyURI, namespaces);
      element.appendChild(childElement);
      ((AnyType)value).populateTo(childElement, namespaces);
    }
    else
    {
      Element childElement = createElement(element.getOwnerDocument(), propertyURI, namespaces);
      if (value != null)
      {
         if (Element.class.isAssignableFrom(aClass))
         {
           Element actualElement = (Element)element.getOwnerDocument().importNode((Element)value, true);
           childElement.appendChild(actualElement);
         }
        else if (aClass.isArray())
        {
          for (int i = 0, length = Array.getLength(value); i < length; ++i)
          {
            populateValueTo(Array.get(value, i), childElement, namespaces);
          }
        }
        else if (Hashtable.class.isAssignableFrom(aClass))
        {
          for (Iterator entries = ((Map)value).entrySet().iterator(); entries.hasNext(); )
          {
            Map.Entry entry = (Map.Entry)entries.next();
            populateValueTo(entry.getKey(), childElement, namespaces);
            populateValueTo(entry.getValue(), childElement, namespaces);
          }
        }
        else if (Vector.class.isAssignableFrom(aClass))
        {
          for (Iterator objects = ((Vector)value).iterator(); objects.hasNext(); )
          {
            Object object = objects.next();
            populateValueTo(object, childElement, namespaces);
          }
        }
        else
        {
          childElement.appendChild(createText(element.getOwnerDocument(), convertValueToString(value)));
        }
      }

      element.appendChild(childElement);
    }
  }

  /**
   * This uses the type name as the property uri.
   */
  protected void populateValueTo(Object value, Element element, List namespaces)
  {
    if (value == null)
    {
      populateValueTo(value, Object.class, "null", element, namespaces);
    }
    else if (value instanceof Element)
    {
      Element actualElement = (Element)element.getOwnerDocument().importNode((Element)value, true);
      element.appendChild(actualElement);
    }
    else
    {
      populateValueTo(value, value.getClass(), value.getClass().getName(), element, namespaces);
    }
  }

  /**
   * This can be called to deserialize the bean from the given element.
   */
  public void populateFrom(Element element)
  {
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0, size = attributes.getLength(); i < size; ++i)
    {
      Attr attr = (Attr)attributes.item(i);
      String attrNamespaceURI = attr.getNamespaceURI();
      String attrLocalName = attr.getLocalName();
      String attrURI = (attrNamespaceURI == null ? "" : attrNamespaceURI + "#") + attrLocalName;
      if (uriAttributeMap.containsKey(attrURI))
      {
        basicSet(attrURI, 0, convertStringToValue((Class)uriClassMap.get(attrURI), attr.getNodeValue()));
      }
    }

    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
    {
      if (child.getNodeType() == Node.ELEMENT_NODE)
      {
        String elementNamespaceURI = child.getNamespaceURI();
        String elementLocalName = child.getLocalName();
        String elementURI = (elementNamespaceURI == null ? "" : elementNamespaceURI + "#") + elementLocalName;
        List list = (List)uriElementMap.get(elementURI);
        if (list != null)
        {
          Object object = populateValueFrom((Class)uriClassMap.get(elementURI), (Element)child);
          if (object != null)
          {
            basicSet(elementURI, list.size(), object);
          }
        }
      }
      else if (child.getNodeType() == Node.TEXT_NODE)
      {
        if (!uriTextMap.isEmpty())
        {
          basicSetText(((Text)child).getData());
        }
      }
    }
  }

  /**
   * This helper method returns the text of the element.
   */
  protected String elementText(Element element)
  {
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
    {
      if (child.getNodeType() == Node.TEXT_NODE)
      {
        return ((Text)child).getData();
      }
    }

    return "";
  }

  /**
   * This helper method returns the list of children converted to objects based on their tag encoding their class name.
   */
  protected List elementChildren(Element element)
  {
    List result = new ArrayList();
    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
    {
      if (child.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elementChild = (Element)child;
        try
        {
          Class aClass = Class.forName(elementChild.getTagName());
          result.add(populateValueFrom(aClass, elementChild));
        }
        catch (ClassNotFoundException exception)
        {
          result.add(elementChild);
        }
      }
    }

    return result;
  }

  /**
   * This creates a value of the given type from the given element.
   */
  protected Object populateValueFrom(Class aClass, Element element)
  {
    try
    {
      if (Element.class.isAssignableFrom(aClass))
      {
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
        {
          if (child.getNodeType() == Node.ELEMENT_NODE)
          {
            return child;
          }
        }
      }
      else if (AnyType.class.isAssignableFrom(aClass))
      {
        AnyType object = (AnyType)aClass.newInstance();
        object.populateFrom(element);
        return object;
      }
      else if (GregorianCalendar.class.isAssignableFrom(aClass))
      {
        GregorianCalendar result = new GregorianCalendar();
        try
        {
          result.setTime(gregorianCalandarDateFormat.parse(elementText(element)));
        }
        catch (Exception exception)
        {
          exception.printStackTrace();
        }
        return result;
      }
      else if (Date.class.isAssignableFrom(aClass))
      {
        Date result = new Date();
        try 
        {
          result = standardDateFormat.parse(elementText(element));
        } 
        catch (ParseException standardException) 
        {
          try 
          {
            result = preciseDateFormat.parse(elementText(element));
          }
          catch (ParseException preciseException) 
          {
          }
        }
        return result;
      }
      else if (Hashtable.class.isAssignableFrom(aClass))
      {
        Hashtable hashtable = new Hashtable();
        for (Iterator iterator = elementChildren(element).iterator(); iterator.hasNext(); )
        {
          Object key = iterator.next();
          Object value = iterator.next();
          hashtable.put(key, value);
        }
        return hashtable;
      }
      else if (Vector.class.isAssignableFrom(aClass))
      {
        Vector vector = new Vector();
        for (Iterator iterator = elementChildren(element).iterator(); iterator.hasNext(); )
        {
          Object value = iterator.next();
          vector.add(value);
        }
        return vector;
      }
      else if (aClass == String.class)
      {
        return elementText(element);
      }
      else if (aClass.isArray())
      {
        List children = elementChildren(element);
        Object result = Array.newInstance(aClass.getComponentType(), children.size());
        int index = 0;
        for (Iterator iterator = elementChildren(element).iterator(); iterator.hasNext(); ++index)
        {
          Object value = iterator.next();
          Array.set(result, index, value);
        }
        return result;
      }
      else
      {
        try
        {
          return aClass.getConstructor(stringConstructor).newInstance(new Object [] { elementText(element) });
        }
        catch (Exception exception)
        {
          exception.printStackTrace();

          return elementText(element);
        }
      }
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }

    return null;
  }

  protected static Class [] stringConstructor = { String.class };
  protected static DateFormat gregorianCalandarDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  protected static DateFormat standardDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  protected static DateFormat preciseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

  /**
   * This is called to convert a string to its bean representation.
   */
  protected Object convertStringToValue(Class aClass, String string)
  {
    if (aClass == String.class)
    {
      return string;
    }
    else
    {
      if (GregorianCalendar.class.isAssignableFrom(aClass))
      {
        GregorianCalendar result = new GregorianCalendar();
        try
        {
          result.setTime(gregorianCalandarDateFormat.parse(string));
        }
        catch (Exception exception)
        {
          exception.printStackTrace();
        }
        return result;
      }
      else if (Date.class.isAssignableFrom(aClass))
      {
        Date result = new Date();
        try 
        {
          result = standardDateFormat.parse(string);
        } 
        catch (ParseException standardException) 
        {
          try 
          {
            result = preciseDateFormat.parse(string);
          }
          catch (ParseException preciseException) 
          {
          }
        }
        return result;
      }
      else
      {
        try
        {
          return aClass.getConstructor(stringConstructor).newInstance(new Object [] { string });
        }
        catch (Exception exception)
        {
          exception.printStackTrace();

          return string;
        }
      }
    }
  }

  /**
   * This is called to convert value to its string.
   */
  protected String convertValueToString(Object value)
  {
    if (value == null)
    {
      return "";
    }
    else if (value instanceof GregorianCalendar)
    {
      String result = gregorianCalandarDateFormat.format(((GregorianCalendar)value).getTime());
      return result;
    }
    else if (value instanceof Date)
    {
      String result = standardDateFormat.format((Date)value);
      return result;
    }
    else
    {
      return value.toString();
    }
  }

  /**
   * This fills in each index of the array with an element from the list; primitives are unwrapped.
   */
  protected Object basicToArray(List list, Object array)
  {
    int length = Array.getLength(array);
    for (int i = 0; i < length; ++i)
    {
      Array.set(array, i, list.get(i));
    }
    return array;
  }

  /**
   * This returns list of the array's contents; primitives are wrapped.
   */
  protected List basicToList(Object array)
  {
    int length = Array.getLength(array);
    ArrayList result = new ArrayList(length);
    for (int i = 0; i < length; ++i)
    {
      result.add(Array.get(array, i));
    }
    return result;
  }
}
