# AGENTS.md

Guidance for AI coding agents working in this repository.

## Project overview

This is a **vendored, modernized fork of Apache WSIF 2.0** (Web Services Invocation
Framework) — a framework for invoking WSDL-described services through a single
provider-agnostic API, regardless of the underlying binding (SOAP, native Java, EJB,
JMS, JCA).

- **Artifact:** `org.apache.wsif:wsif:2.0`, packaged as an **OSGi bundle** (`bundle`).
- **Language level:** Java **21** (`maven.compiler.release=21`).
- **Upstream:** Apache WSIF, originally released 2004. The code is legacy but the
  build and dependencies have been modernized (see *Modernization context* below).
- **Location:** `custom/ws-wsif/java` inside the larger `wsif` repository. The git root
  is two levels up — always run build commands from this directory.

## Build & verify

Maven is the source of truth. A local repository is already populated, so the build
works offline.

```powershell
mvn -o clean package      # compile + build the OSGi bundle -> target/wsif-2.0.jar
mvn -o clean compile      # compile only
mvn -o clean verify       # full verification
mvn -o javadoc:javadoc    # generate javadocs
```

**Always run `mvn -o clean package` after making changes** and confirm it exits `0`.
A successful build produces `target/wsif-2.0.jar` and ~225 class files.

### Toolchain

Java 21 and Maven 3.9.x are on `PATH`. Do not attempt to install or switch JDKs.

### There are no tests

`src/test/java` and `test/` do not exist, so Surefire runs **0 tests**. Do not report
test results as meaningful verification — **compilation success is the only automated
signal available**.

`test.bak/` holds the legacy upstream JUnit 3 suite (`addressbook/`, `async/`,
`faults/`, `providers/`, `soap/`, …). It is **not part of the Maven build**, is not on
any source path, and does not compile. Treat it as read-only reference material for
understanding intended behaviour; do not try to wire it into the build unless the user
explicitly asks.

### Legacy Ant build — do not use

`build.xml`, `build.bat`, `build.sh`, `classpath.*`, `lcp.bat` and the pre-bundled jars
in `lib/` are the **original 2004 Ant build**. They are retained for historical
reference only, reference JDK 1.3-era tooling, and will not work. The `lib/` jars are
**not** on the Maven classpath — never add a dependency by pointing at `lib/`; add a
proper coordinate to `pom.xml` instead.

## Repository layout

```
pom.xml                        Maven build (the real one)
src/                           <- sourceDirectory (note: NOT src/main/java)
  wsif.properties              runtime configuration, packaged into the jar
  client-config.wsdd           Axis client deployment descriptor
  META-INF/services/
    org.apache.wsif.spi.WSIFProvider   provider registrations (ServiceLoader)
  org/apache/wsif/
    *.java                     public API: WSIFService, WSIFPort, WSIFOperation,
                               WSIFMessage, WSIFServiceFactory, WSIFException,
                               WSIFRequest/WSIFResponse, WSIFInterceptor, …
    spi/WSIFProvider.java      the provider SPI — one interface, 3 methods
    base/                      default API implementations (WSIFServiceImpl,
                               WSIFDefaultMessage, WSIFClientProxy, …)
    providers/                 provider implementations (see below)
    wsdl/extensions/           WSDL4J extensibility elements + serializers
                               (java/, ejb/, jms/, format/, instance/)
    compiler/schema/           XML Schema -> Java type generation
    format/, mapping/          type mapping and formatting
    util/                      WSIFProperties, WSIFUtils, WSIFPluggableProviders
    logging/                   Trc (tracing), MessageLogger
    naming/, catalog/, schema/, attachments/, tools/
samples/                       standalone examples, NOT compiled by Maven
doc/                           original Apache HTML documentation
test.bak/                      legacy JUnit 3 suite, NOT built
lib/                           original 2004 jars, NOT on the Maven classpath
```

`src` being the source root (rather than `src/main/java`) is deliberate and set
explicitly in `pom.xml`. Place new production sources under `src/org/apache/wsif/...`.

Resources are filtered from `src` by include pattern — only `**/*.properties`,
`**/*.wsdd`, and `**/META-INF/services/*` are packaged. A new resource type needs a new
`<include>` in `pom.xml`.

## Architecture: how a call flows

1. `WSIFServiceFactory.newInstance()` → `WSIFService` for a WSDL definition.
2. `WSIFService` asks each registered `WSIFProvider` — via
   `createDynamicWSIFPort(Definition, Service, Port, WSIFDynamicTypeMap)` — whether it
   can handle the port.
3. A provider inspects the port's binding extensibility elements. If it recognizes one
   of its own (e.g. `JavaBinding`), it returns a `WSIFPort`; **otherwise it returns
   `null`** so the next provider gets a chance.
4. `WSIFPort.createOperation(...)` → `WSIFOperation` → `executeRequestResponseOperation`
   / `executeInputOnlyOperation` with `WSIFMessage` in/out/fault.

Providers are matched by the **WSDL namespace URIs** they advertise from
`getBindingNamespaceURIs()` and `getAddressNamespaceURIs()`.

### Providers

Registered in `src/META-INF/services/org.apache.wsif.spi.WSIFProvider`:

| Package | Class | Notes |
|---|---|---|
| `providers/java` | `WSIFDynamicProvider_Java` | in-process Java/JavaBean calls |
| `providers/ejb` | `WSIFDynamicProvider_EJB` | needs `javax.rmi.PortableRemoteObject` |
| `providers/soap/apacheaxis` | `WSIFDynamicProvider_ApacheAxis` | Axis 1.4 |
| `providers/soap/apachesoap` | `WSIFDynamicProvider_ApacheSOAP` | Apache SOAP 2.3.1, optional |
| `providers/jms` | `WSIFDynamicProvider_Jms` | native JMS |

Also present but **not registered**: `providers/jca` (J2C/resource adapter) and
`providers/soap/soaprmi`.

**`providers/soap/soaprmi/**` is excluded from compilation** in `pom.xml`. SoapRMI is a
defunct project whose `soaprmi.*` / `xpp` packages exist only as `lib/soaprmi-1_1.jar`
and were never published to Maven Central. Leave the exclusion in place; do not try to
"fix" these files.

**Adding a provider** requires all three of: the `WSIFProvider` implementation, the
matching `WSIFPort`/`WSIFOperation` classes, and an entry in the `META-INF/services`
file. Providers **must be stateless and thread-safe** — this is a documented contract on
the SPI, and the same provider instance is used concurrently.

## Code style

Match the surrounding file. This is a 2004 codebase with consistent conventions:

- **4 spaces**, no tabs. K&R braces. Long parameter lists wrapped one-per-line, indented.
- **Apache License 2.0 header** on every source file, followed by the ASF/IBM
  attribution block. Copy it verbatim from a neighbouring file when creating new files.
- **Javadoc on public types and methods**, with `@author` tags. Preserve existing
  `@author` lines — do not remove or replace attribution.
- **Tracing idiom:** nearly every public method opens with `Trc.entry(this, ...)` and
  exits via `Trc.exit()` or `Trc.exit(returnValue)` from `org.apache.wsif.logging.Trc`.
  **Every exit path needs a matching `Trc.exit`**, including early returns. Follow this
  in new and modified methods.
- Logging goes through **`commons-logging`** (backed by reload4j). Do not introduce
  SLF4J, Log4j 2, or `System.out`.
- Raw collection types (`List`, `Iterator` without generics) are pervasive. Do not
  bulk-genericize or "modernize" code you were not asked to touch — it inflates diffs
  and risks behaviour changes in a codebase with no test coverage.

## Modernization context

This fork has been migrated from Java 1.3/1.4 to Java 21. `pom.xml` carries extensive
comments explaining each dependency decision — **read them before changing dependencies**.
Key points:

- **reload4j 1.2.26** replaces log4j 1.2.17, which is EOL with 6 unfixable CVEs. It is a
  binary-compatible drop-in. WSIF never calls `org.apache.log4j` directly; it is only the
  backend commons-logging discovers for Axis.
- **Jakarta EE 8 API artifacts** (`jakarta.jms-api` 2.0.3, `jakarta.ejb-api` 3.2.6,
  `jakarta.resource-api` 1.7.4) replace the retired Apache Geronimo spec jars. These
  **deliberately still use the `javax.*` namespace** — required because Axis 1.4 /
  JAX-RPC has no Jakarta EE 9+ successor. **Do not migrate `javax.*` → `jakarta.*`**;
  it will break the Axis and JAX-RPC integration.
- **`jboss-rmi-api_1.0_spec`** supplies `javax.rmi.PortableRemoteObject`, removed from
  the JDK in Java 11 with the CORBA modules (JEP 320). Required by the EJB provider.
- **`javax.mail`** excludes `javax.activation:activation:1.1` to avoid a split package
  with `com.sun.activation:javax.activation:1.2.0`.
- The Felix `maven-bundle-plugin` exports `org.apache.wsif.*` and imports
  `org.apache.soap.*`, `org.apache.axis.*`, `javax.xml.rpc.*`, `javax.ejb.*` as
  **optional** (see the `osgi.imported.packages` property), so consumers only need those
  jars when they actually select the corresponding provider. If you add a provider with
  a new optional backend, update that property.

Before adding or bumping a dependency, check it for known CVEs and prefer the maintained
successor artifact — and add a `pom.xml` comment explaining why, matching the existing style.

## Known quirks

- `src/wsif.properties` sets `wsif.servicefactory=customfactory.client.CustomServiceFactoryImpl`,
  which is a **sample class that is not on the runtime classpath**. This file is packaged
  into the jar. Be aware of it when diagnosing factory-resolution failures.
- `pom.xml` contains a commented-out `maven-jar-plugin` block. It is superseded by the
  bundle plugin; leave it alone unless asked.

## Working agreements

- Run the build and confirm it passes before reporting a change complete.
- Keep diffs minimal and scoped to the request. This codebase has **no test safety net**,
  so incidental refactoring is high-risk and cannot be validated.
- Preserve the Apache license headers and author attribution in every file you touch.
- When behaviour is ambiguous, consult the original documentation in `doc/`
  (`user-guide.html`, `how_to_provider.html`, `wsdl_extensions/`) and the reference
  tests in `test.bak/` before guessing.

