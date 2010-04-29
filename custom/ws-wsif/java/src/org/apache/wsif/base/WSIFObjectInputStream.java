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

package org.apache.wsif.base;

import java.io.*;

/**
 * Class <code>FObjectInputStream</code> overloads the class <code>ObjectInputStream</code>
 * to solve the bug reported under BugID
 * <a href="http://developer.java.sun.com/developer/bugParade/bugs/4171142.html">4171142</a> .
 * <br>
 * If an object contains primitiv data types and you try to serialize/deserialize
 * it than you will get an ClassNotFoundException for the primitiv data type.
 * Reason for this exception is that the ObjectInputStream try to resolve the
 * data type from java.lang.&lt;primitiv&gt; which does not work.
 * <br>
 * The solution for the problem is to override the method resolveClass and to
 * bypass the resolution for primitiv data types.
 *
 * @author <a href="mailto:adietzsch@de.ibm.com?subject=FObjectInputStream">Alexander Dietzsch</a>
 */
class WSIFObjectInputStream extends ObjectInputStream {
  /**
   * Create an ObjectInputStream that reads from the specified InputStream.
   * The stream header containing the magic number and version number
   * are read from the stream and verified. This method will block
   * until the corresponding ObjectOutputStream has written and flushed the
   * header.
   *
   * @param in  the underlying <code>InputStream</code> from which to read
   * @exception StreamCorruptedException The version or magic number are
   * incorrect.
   * @exception IOException An exception occurred in the underlying stream.
   */
  WSIFObjectInputStream (InputStream in)
    throws IOException, StreamCorruptedException {

    super(in);
  }
  /**
   * Load the local class equivalent of the specified stream class description.
   *
   * Subclasses may implement this method to allow classes to be
   * fetched from an alternate source.
   *
   * The corresponding method in ObjectOutputStream is
   * annotateClass.  This method will be invoked only once for each
   * unique class in the stream.  This method can be implemented by
   * subclasses to use an alternate loading mechanism but must
   * return a Class object.  Once returned, the serialVersionUID of the
   * class is compared to the serialVersionUID of the serialized class.
   * If there is a mismatch, the deserialization fails and an exception
   * is raised. <p>
   *
   * By default the class name is resolved relative to the class
   * that called readObject. <p>
   *
   * @param v  an instance of class ObjectStreamClass
   * @return a Class object corresponding to <code>v</code>
   * @exception IOException Any of the usual Input/Output exceptions.
   * @exception ClassNotFoundException If class of
   * a serialized object cannot be found.
   */
  protected Class resolveClass(ObjectStreamClass v)
    throws IOException, ClassNotFoundException {

    try {
      // return super.resolveClass(v);
      return Class.forName(v.getName(), true, Thread.currentThread().getContextClassLoader());
    } catch (ClassNotFoundException xcpt) {
	 String className = v.getName() ;
      if (className.equals("boolean")) { return boolean.class; }
      if (className.equals("char")) { return char.class; }
      if (className.equals("byte")) { return byte.class; }
      if (className.equals("short")) { return short.class; }
      if (className.equals("int")) { return int.class; }
      if (className.equals("long")) { return long.class; }
      if (className.equals("float")) { return float.class; }
      if (className.equals("double")) { return double.class; }

      throw xcpt;
    }
  }
}
