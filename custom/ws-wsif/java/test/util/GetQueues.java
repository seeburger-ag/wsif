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

package util;

import java.io.*;

/**
 * used by the wsifDynQTidy.bat script to delete the mq dynamic queues
 */
public class GetQueues {
	
	public static void main(String[] args) {

       if ( args.length != 2 ) {
          System.err.println( "usage: GetQueues infile outfile" );
          System.exit( 1 );
       }

       try {
          BufferedReader input =  new BufferedReader(new FileReader( args[0] ) );
          BufferedWriter output = new BufferedWriter(new FileWriter( args[1] ) );

          String s, qn;
          while ((s = input.readLine()) != null) {
             s = s.trim();
             if ( s .startsWith( "QUEUE(" ) ) {
                qn = s.substring( 6, s.indexOf( ")" ) ); 
                output.write( "DELETE QLOCAL(" + qn + ")\n" );
             } 	       
          }
          
          input.close();
          output.close();
          
       } catch (Exception ex) {
       	  ex.printStackTrace();
       }

	}

}