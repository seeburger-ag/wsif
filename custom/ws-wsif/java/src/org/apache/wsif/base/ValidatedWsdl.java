/*
 * Copyright 2026 The Apache Software Foundation.
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
 */

package org.apache.wsif.base;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Remembers WSDL model objects (a <code>Definition</code> or a <code>PortType</code>)
 * that have already passed WSIF's structural validation, so that the validation -
 * which walks every message, port type and binding, or every operation - runs once
 * per object instead of on every <code>getService</code> / <code>getPort</code>.
 * <p>
 * Only successful validations are remembered: an invalid object is re-validated on
 * every use and therefore fails with the same exception every time.
 * <p>
 * This relies on WSDL model objects not being structurally modified after their
 * first successful use with WSIF (definitions are parsed once and then only read).
 * Set the system property <code>wsif.validation.memoize=false</code> to validate on
 * every use as before.
 * <p>
 * The objects are held weakly, so definitions of undeployed processes can be garbage
 * collected. wsdl4j's model classes do not override <code>equals</code> /
 * <code>hashCode</code>, so the lookup has identity semantics. This class is
 * thread-safe.
 *
 * @author Phase 2.3, see doc/PERFORMANCE_THREADSAFETY_PLAN.md
 */
final class ValidatedWsdl {

    /** Memoization switch, read once. */
    static final boolean ENABLED =
        !"false".equalsIgnoreCase(System.getProperty("wsif.validation.memoize"));

    /** Validated objects; guarded by itself. */
    private static final Map<Object, Boolean> VALIDATED = new WeakHashMap<>();

    /*
     * Lock-free fast path for the two most recently seen validated objects - a
     * getService typically checks one Definition and one PortType in turn.
     */
    private static volatile WeakReference<Object> last1 = new WeakReference<>(null);
    private static volatile WeakReference<Object> last2 = new WeakReference<>(null);

    private ValidatedWsdl() {
    }

    /**
     * @return true if <code>o</code> has already been validated successfully
     */
    static boolean isValidated(Object o) {
        if (!ENABLED || o == null) {
            return false;
        }
        if (last1.get() == o || last2.get() == o) {
            return true;
        }
        boolean v;
        synchronized (VALIDATED) {
            v = VALIDATED.containsKey(o);
        }
        if (v) {
            remember(o);
        }
        return v;
    }

    /**
     * Records that <code>o</code> has been validated successfully.
     */
    static void markValidated(Object o) {
        if (!ENABLED || o == null) {
            return;
        }
        synchronized (VALIDATED) {
            VALIDATED.put(o, Boolean.TRUE);
        }
        remember(o);
    }

    /** Puts <code>o</code> into the fast path (benign races: it is only a hint). */
    private static void remember(Object o) {
        last2 = last1;
        last1 = new WeakReference<>(o);
    }
}

