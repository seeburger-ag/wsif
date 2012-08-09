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
package performance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Collect monitor point timing statistics.
 * 
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 */
public class Monitor {

   static HashMap timers = new HashMap();
   static long start;
   static long stop;
   
   public static void start() {
       start = System.currentTimeMillis();
   }
   
   public static void stop() {
       stop = System.currentTimeMillis();
   }

   public static long getTotalTime() {
       return stop - start;
   }
        
   public static void start(String s) {
      Timer t = (Timer)timers.get( s );
      if ( t == null ) {
   	     t = new Timer();
         timers.put( s, t );
      } 
      t.start();	
   }

   public static void stop(String s) {
      Timer t = (Timer)timers.get( s );
      t.stop();	
   }

   public static void pause(String s) {
      Timer t = (Timer)timers.get( s );
      if ( t != null ) {
         t.pause();
      }	
   }

   public static void resume(String s) {
      Timer t = (Timer)timers.get( s );
      if ( t != null ) {
         t.resume();	
      }
   }
   
   public static void clear() {
      timers = new HashMap();
      start = System.currentTimeMillis();
   }

   public static HashMap getAvgResults() {
      Timer t;
      String testName;
      HashMap results = new HashMap();
      for (Iterator i = timers.keySet().iterator(); i.hasNext(); ) {
         testName = (String)i.next();
         t = (Timer)timers.get( testName );
         float f1 = t.getAvgTime();
         Float f = new Float( t.getAvgTime() );
         results.put( testName, new Float( t.getAvgTime() ) );
      }
      return results;
   }

   public static void printResults() {
      Timer t; 
      String testName;
      float value;
      ArrayList l = new ArrayList(); 
      for (Iterator i = timers.keySet().iterator(); i.hasNext(); ) {
         l.add( timers.get((String)i.next()) );
      }
      Collections.sort( l, new Comparator() {
      	public int compare(Object o1, Object o2) {
      		 return Math.round(((Timer)o1).getTotalTime() - ((Timer)o2).getTotalTime());
      	}
      } );
      long maxWeight = ((Timer) l.get(l.size()-1)).getTotalTime();

      l = new ArrayList(); 
      for (Iterator i = timers.keySet().iterator(); i.hasNext(); ) {
         l.add( i.next() );
      }
      Collections.sort( l);

      System.out.println( "\n=== Monitor Results === (total time= " + getTotalTime() + " msecs)\n" );    	
      System.out.println(
        "Test Name                                           Weight       Avg Time              Total count");

      DecimalFormat formatter = new DecimalFormat("#0.00000");
      for (Iterator i = l.iterator(); i.hasNext(); ) {
         testName = (String)i.next();
         t = (Timer)timers.get( testName );
         int weight = Math.round((float)t.getTotalTime() / maxWeight * 100);
         System.out.println( pad(testName,50) + lpad( ""+weight, 8 ) + 
            " value=" + lpad(formatter.format( t.getAvgTime() ),8) + " msecs" +
            ", samples= " + lpad( ""+t.getIterations(), 8 ) );    	
      }
      System.out.println();    	
    }
 
    public static String pad(String s, int pad) {
       if ( s==null ) s = "";
       StringBuffer sb = new StringBuffer( pad );
       for (int i = 0; i < (pad-s.length()); i++) {
         sb = sb.append( " " );
       }
       return s + sb.toString();
    }

    public static String lpad(String s, int pad) {
       if ( s==null ) s = "";
       StringBuffer sb = new StringBuffer( pad );
       for (int i = 0; i < (pad-s.length()); i++) {
         sb = sb.append( " " );
       }
       return sb.toString() + s;
    }

}

class Timer {

   long start;
   long interval;
   long totalTime;
   int startCount;
   long minimumInterval, errorInterval;
   static final long CLOCK_ACCURACY = 10; // 10 milliseconds
         
   public Timer() {
   	   interval = 0;
   	   totalTime = 0;
   	   startCount = 0;
   	   minimumInterval = Long.MAX_VALUE;
   	   errorInterval = Long.MAX_VALUE;
   }

   public void start() {
   	   interval = 0;
   	   startCount +=1;
   	   totalTime += interval;
   	   start = System.currentTimeMillis();
   }

   public void pause() {
   	   interval += System.currentTimeMillis() - start;
   	   start = 0;
   }

   public void resume() {
   	   start = System.currentTimeMillis();
   }

   public void stop() {
   	   long thisInterval = System.currentTimeMillis() - start;
   	   if ( thisInterval > errorInterval ) {
   	      startCount-=1; // ignore this sample
   	   } else {
   	      interval += thisInterval;
   	      totalTime += thisInterval;
   	      if ( startCount > 1 && minimumInterval > 0 && interval < minimumInterval ) {
   	         minimumInterval = interval;
   	         errorInterval = minimumInterval + CLOCK_ACCURACY;
   	         startCount = 0;
   	         totalTime = 0;
   	      }
   	   }
   }

   public long getInterval() {
   	   return interval;
   }
	
   public float getAvgTime() {
   	   return (float)totalTime / startCount;
   }
	
   public int getIterations() {
   	   return startCount;
   }

   public long getTotalTime() {
   	   return totalTime;
   }
	
}