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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;

/**
 * WSIFDefaultCorrelationService provides a default implementation of a
 * WSIFCorrelationService using a Hashmap as the backing store.
 * @author Ant Elder <antelder@apache.org>
 */
public class WSIFDefaultCorrelationService implements WSIFCorrelationService {

	private HashMap correlatorStore; // associates IDs with WSIFOperators  
	private HashMap timeouts; // associates IDs with a timeout time
	private Thread timeoutWatcher; // watches for timeout times expiring	
	private boolean shutdown; // has the correlation service been shutdown  

	/**
	 * WSIFCorrelationServiceLocator should be used to
	 * create a correlation service.
	 */
	public WSIFDefaultCorrelationService() {
		Trc.entry(this);
		Trc.exit();
	}

	/**
	 * Adds an entry to the correlation service.
	 * @param correlator   the key to associate with the state. This will be 
	 *                     a JMS message correlation ID.
	 * @param state   the state to be stored. This will be a WSIFOperation.
	 * @param timeout   a timeout period after which the key and associated
	 *                  state will be deleted from the correlation service. A
	 *                  value of zero indicates there should be no timeout. 
	 */
	public synchronized void put(
		WSIFCorrelationId correlator,
		Serializable state,
		long timeout)
		throws WSIFException {
		Trc.entry(this, correlator, state, new Long(timeout));
		if (correlator != null && state != null) {
			if (correlatorStore == null) {
				initialise();
			}
			try {
				correlatorStore.put(correlator, serialize(state));
				if (timeout > 0) {
					if (timeouts == null) {
						initTimeouts();
					}
					timeouts.put(
						correlator,
						new Long(System.currentTimeMillis() + timeout));
				}
			} catch (IOException ex) {
				Trc.exception(ex);
				throw new WSIFException(ex.toString());
			}
		} else {
			throw new IllegalArgumentException(
				"cannot put null "
					+ ((correlator == null) ? "correlator" : "state"));
		}
		Trc.exit();
	}

	/**
	 * Retrieves an entry (a WSIFOperation) from the correlation service.
	 * @param id   the key of the state to retrieved
	 * @return the state associated with the id, or null if there is no
	 *         match for the id. 
	 */
	public synchronized Serializable get(WSIFCorrelationId id)
		throws WSIFException {
		Trc.entry(this, id);
		if (correlatorStore == null) {
			throw new WSIFException("get called on correlation service but put never done");
		} else if (id == null) {
			throw new IllegalArgumentException("cannot get null");
		} else {
			try {
				Serializable s =
					(Serializable) unserialize((byte[]) correlatorStore
						.get(id));
				Trc.exit(s);
				return s;
			} catch (Exception ex) {
				Trc.exception(ex);
				throw new WSIFException(ex.toString());
			}
		}
	}

	/**
	 * Removes an entry form the correlation service.
	 * @param id   the key of entry to be removed
	 */
	public synchronized void remove(WSIFCorrelationId id)
		throws WSIFException {
		Trc.entry(this, id);
		if (correlatorStore == null) {
			throw new WSIFException("corelation service has been shutdown");
		} else if (id == null) {
			throw new IllegalArgumentException("cannot remove null");
		} else {
			correlatorStore.remove(id);
			if (timeouts != null) {
				timeouts.remove(id);
			}
			Trc.exit();
		}
	}

	private synchronized void remove(ArrayList expiredKeys) {
		Trc.entry(this, expiredKeys);
		if (expiredKeys != null && correlatorStore != null) {
			Serializable id;
			for (Iterator i = expiredKeys.iterator(); i.hasNext();) {
				id = (Serializable) i.next();
				correlatorStore.remove(id);
				timeouts.remove(id);
				MessageLogger.log("WSIF.0008W", id);
			}
		}
		Trc.exit();
	}

	/**
	 * Shutsdown the correlation service.
	 */
	public void shutdown() {
		Trc.entry(this);
		shutdown = true;
		Trc.exit();
	}

	private void initialise() {
		shutdown = false;
		correlatorStore = new HashMap();
	}

	private void initTimeouts() {
		timeouts = new HashMap();
		timeoutWatcher = new Thread() {
			public void run() {
				while (!shutdown) {
					try {
						sleep(WSIFConstants.CORRELATION_TIMEOUT_DELAY);
					} catch (InterruptedException ex) {
						Trc.ignoredException(ex);
					}
					checkForTimeouts();
				}
				if (correlatorStore != null)
					correlatorStore = null;
				if (timeouts != null)
					timeouts = null;
			}
		};
		timeoutWatcher.setName("WSIFDefaultCorrelationService timeout watcher");
		timeoutWatcher.start();
	}

	private void checkForTimeouts() {
		Long expireTime;
		Serializable key;
		ArrayList expiredKeys = new ArrayList();
		Long now = new Long(System.currentTimeMillis());
		// add to expiredKeys all the keys whose timouts have expired 
		try {
			for (Iterator i = timeouts.keySet().iterator(); i.hasNext();) {
				key = (Serializable) i.next();
				expireTime = (Long) timeouts.get(key);
				if (now.compareTo(expireTime) > 0) {
					// now greater than expireTime
					expiredKeys.add(key);
				}
			}
		} catch (ConcurrentModificationException ex) {
			Trc.ignoredException(ex);
		} // ignore this, get the others next time

		if (expiredKeys.size() > 0) {
			remove(expiredKeys);
		}

	}

	private byte[] serialize(Object o) throws IOException {
		if (o == null) {
			return null;
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream so = new ObjectOutputStream(baos);
			so.writeObject(o);
			so.flush();
			return baos.toByteArray();
		}
	}

	private Object unserialize(byte[] bytes)
		throws IOException, ClassNotFoundException {
		if (bytes == null) {
			return null;
		} else {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			//ObjectInputStream si = new ObjectInputStream(bais);
			WSIFObjectInputStream si = new WSIFObjectInputStream(bais);
			return si.readObject();
		}
	}

}
