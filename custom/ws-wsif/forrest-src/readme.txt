This directory contains the source for the WSIF web site - http://ws.apache.org/wsif

Ultimately the source xml files contained within this directory and sub-directories will be used to generate the
distributed documentation for WSIF as well (i.e. the files in the docs dir).


Using forrest
-------------

This directory forms an Apache Forrest project. To build the pages, follow these simple steps:

1) Download Apache Forrest from http://www.apache.org/dist/xml/forrest/

2) Set up the Forrest environment as described in the Using Forrest article on the Forrest web site:
   http://xml.apache.org/forrest/your-project.html
   
   Bascially this involves setting a FORREST_HOME variable pointing at the directory you extracted the 
   forrest zip to, and then adding FORREST_HOME to your PATH variable.
   
3) Navigate to the forrest-src directory.

4) Run Forrest on the WSIF pages by simply typing forrest

5) Hopefully after quite a few lines of output you should see the line BUILD SUCCESSFUL and a build directory should have been 
   created under the forrest-src directory

6) Navigate to build/site (**) and launch index.html to view the WSIF site


** NOTE FOR COMMITTERS **

If you make changes to the source files, check them in but please DO NOT check in the generated build directory or any of its contents.
We should keep the forrest-src directory for source only...as the name suggests :-) See (**) below.


More info
---------

If you have problems building the site check the following that you have correctly configured Forrest 
(see http://xml.apache.org/forrest/your-project.html)

If you make changes to the source files and rebuild the site, you might find that the changes don't seem to take effect.
If this happens, try deleting the previously generated forrest-src/build directory before running forrest.

More inforamtion about using Forrest can be found at http://xml.apache.org/forrest


(**) To change the location that the built files get put, set the following property in the forrest.properties file:

project.build-dir

For example:
    project.build-dir=/myfolder/build