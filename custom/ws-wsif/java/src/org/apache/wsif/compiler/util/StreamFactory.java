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

package org.apache.wsif.compiler.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.wsif.WSIFException;
import org.apache.wsif.compiler.schema.tools.Conventions;

/**
 * @author Matthew J. Duftler
 * @deprecated
 */
public class StreamFactory {
	
    public OutputStream getOutputStream(
        String root,
        String name,
        boolean overwrite)
        throws WSIFException {
        boolean verbose = Conventions.getVerbose();

        if (root != null) {
            File directory = new File(root);

            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new WSIFException("Failed to create directory '" + root + "'.");
                } else if (verbose) {
                    System.out.println("Created directory '" + directory.getAbsolutePath() + "'.");
                }
            }
        }

        File file = new File(root, name);
        String absolutePath = file.getAbsolutePath();

        if (file.exists()) {
            if (!overwrite) {
                throw new WSIFException(
                    "File '"
                        + absolutePath
                        + "' already exists. Please remove it or "
                        + "enable the overwrite option.");
            } else {
                file.delete();

                if (verbose) {
                    System.out.println("Deleted file '" + absolutePath + "'.");
                }
            }
        }

        if (verbose) {
            System.out.println("Created file '" + absolutePath + "'.");
        }

        try {
            return new FileOutputStream(absolutePath);
        } catch (FileNotFoundException e) {
            throw new WSIFException("Problem getting output stream.", e);
        }
    }

    public InputStream getInputStream(String root, String name)
        throws IOException {
        String fileName = (root != null) ? root + File.separatorChar + name : name;
        URL url = null;
        Object content = null;

        try {
            url = getURL(null, fileName, 1);
            content = url.getContent();
        } catch (SecurityException e) {
            throw new IOException(
                "Your JVM's security manager has disallowed "
                    + "access to '"
                    + fileName
                    + "'.");
        } catch (IOException e) {
            throw new IOException("The resource at '" + fileName + "' was not found.");
        }

        if (content == null) {
            throw new IllegalArgumentException("No content at '" + fileName + "'.");
        } else if (content instanceof InputStream) {
            return (InputStream) content;
        } else {
            throw new IOException("The content of '" + fileName + "' is not a stream.");
        }
    }

    private static URL getURL(URL contextURL, String spec, int recursiveDepth)
        throws MalformedURLException {
        URL url = null;
    
        try {
            url = new URL(contextURL, spec);
    
            try {
                url.openStream();
            } catch (IOException ioe1) {
                throw new MalformedURLException("This file was not found: " + url);
            }
        } catch (MalformedURLException e1) {
            url = new URL("file", "", spec);
    
            try {
                url.openStream();
            } catch (IOException ioe2) {
                if (contextURL != null) {
                    String contextFileName = contextURL.getFile();
                    String parentName = new File(contextFileName).getParent();
    
                    if (parentName != null && recursiveDepth < 3) {
                        return getURL(
                            new URL("file", "", parentName + '/'),
                            spec,
                            recursiveDepth + 1);
                    }
                }
    
                throw new MalformedURLException("This file was not found: " + url);
            }
        }
    
        return url;
    }
}