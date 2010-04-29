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
package mime;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;

public class WSIFFrame extends Frame {
    private static Frame[] frames = new Frame[2];
    private static int frameIndex = 0;
    private Image im = null;

    public static void display(Image im, String title) throws Exception {
        Frame f = new WSIFFrame(im,title);
        frames[frameIndex] = f;
        frameIndex++;
        f.add(new WSIFCanvas(im));
        f.pack();
        f.setLocation(300*frameIndex,200);
        f.show();
    }

    public WSIFFrame(Image im, String title) {
        super(title);
        this.im = im;
    }


    public static void close() {
        for (int i = 0; i < frameIndex; i++) {
            frames[i].dispose();
            frames[i] = null;
        }
        frameIndex=0;
    }
    
    public void finalize() throws Throwable {
        for (int i = 0; i < frameIndex; i++) {
            frames[i].dispose();
            frames[i] = null;
        }
        frameIndex=0;
    }
    
    static class WSIFCanvas extends Canvas {
        Image im;

        WSIFCanvas(Image im) {
            super();
            this.setSize(200,200);
            this.im = im;
        }

        public void paint(Graphics g) {
            g.drawImage(im, 0, 0, this);
        }
    }
}

