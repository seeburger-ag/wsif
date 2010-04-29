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

package async;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFResponseHandler;

/**
 * A WSIFResponseHandler for the AsyncTest testcase.
 *
 * @author Ant Elder <antelder@apache.org>
 */
public class AsyncResponseHandler implements WSIFResponseHandler {

    // These are static because as the AsyncResponseHandler is serialized/unserialized 
    // by the correlation service the testcase loses its reference to the
    // handler which processes the reponse. Being static lets the testcase
    // see when the response has been processed. 
    // Obviously only a single AsyncResponseHandler should be used at a time!

    static private boolean done;
    static private int nrReplies;
    static private WSIFMessage[] outputs;
    static private WSIFMessage[] faults;

    public AsyncResponseHandler(int nrReplies) {
        this.nrReplies = nrReplies - 1; // zero = 1
        done = false;
        outputs = new WSIFMessage[nrReplies];
        faults = new WSIFMessage[nrReplies];
    }

    synchronized public void executeAsyncResponse(
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException {
        outputs[nrReplies] = output;
        faults[nrReplies] = fault;
        done = --nrReplies < 0;
    }

    public boolean isDone() {
        return done;
    }

    public WSIFMessage[] getOutputs() {
        return outputs;
    }

    public WSIFMessage[] getFaults() {
        return faults;
    }

}
