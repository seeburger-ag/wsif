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
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.Definition;

/**
 * Service cache for the <code>WSIFServiceFactory.getService(Definition, ...)</code>
 * variants, keyed by the identity of the <code>Definition</code> object.
 * <p>
 * The former cache keyed these services on the definition's
 * <code>documentBaseURI</code>, so a new definition with the same base URI (for
 * example a redeployed business process) was served the service built from the old
 * one. Keying on the object itself makes that impossible.
 * <p>
 * Definitions are held weakly: an entry disappears when its definition is no longer
 * used, so there is no size limit and no LRU. This only works because the cached
 * templates do <em>not</em> reference their definition (it is detached and supplied
 * again by the caller when a template is copied, see
 * {@link WSIFServiceImpl#WSIFServiceImpl(WSIFServiceImpl, Definition)}); a value
 * referencing its key would keep it reachable forever.
 * <p>
 * Templates are immutable once published and are only ever copied. This class is
 * thread-safe.
 *
 * @author Phase 2.4, see doc/PERFORMANCE_THREADSAFETY_PLAN.md
 */
final class DefinitionServiceCache {

    /** Templates per definition; the outer map is guarded by itself. */
    private final Map<Definition, ConcurrentHashMap<String, WSIFServiceImpl>> byDefinition =
        new WeakHashMap<>();

    /** Lock-free fast path for the most recently used definition. */
    private volatile Last last = new Last(null, null);

    /** Immutable pair, published atomically through {@link #last}. */
    private static final class Last {
        final WeakReference<Definition> definition;
        final ConcurrentHashMap<String, WSIFServiceImpl> templates;

        Last(Definition def, ConcurrentHashMap<String, WSIFServiceImpl> templates) {
            this.definition = new WeakReference<>(def);
            this.templates = templates;
        }
    }

    /**
     * @return the cached template for <code>def</code> and <code>key</code>, or null
     */
    WSIFServiceImpl get(Definition def, String key) {
        ConcurrentHashMap<String, WSIFServiceImpl> templates = templatesFor(def, false);
        return (templates == null) ? null : templates.get(key);
    }

    /**
     * Caches <code>template</code>, which must not reference <code>def</code>.
     */
    void put(Definition def, String key, WSIFServiceImpl template) {
        templatesFor(def, true).putIfAbsent(key, template);
    }

    private ConcurrentHashMap<String, WSIFServiceImpl> templatesFor(
        Definition def,
        boolean create) {
        Last l = last;
        if (l.definition.get() == def && l.templates != null) {
            return l.templates;
        }
        ConcurrentHashMap<String, WSIFServiceImpl> templates;
        synchronized (byDefinition) {
            templates = byDefinition.get(def);
            if (templates == null && create) {
                templates = new ConcurrentHashMap<>();
                byDefinition.put(def, templates);
            }
        }
        if (templates != null) {
            last = new Last(def, templates);
        }
        return templates;
    }
}

