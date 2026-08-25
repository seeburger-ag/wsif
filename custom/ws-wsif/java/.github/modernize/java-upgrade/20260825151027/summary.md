# Java Upgrade Result

> **Executive Summary**\
> This report documents the upgrade of Apache WSIF (Web Services Invocation Framework) 2.0 to Java 21 LTS together with a modernization of its framework dependencies. The project nominally targeted Java 21 already, but that target was only met on paper: six `maven-compiler-plugin` exclusion patterns hid **38 of its 206 source files (18% of the codebase)** from the compiler, and two of the hidden classes were registered as service providers in `META-INF/services` yet absent from the shipped JAR — a latent runtime defect. This upgrade removed five of the six exclusions by fixing the underlying Java 21 incompatibilities (`sun.tools.javac`, `javax.rmi`, missing `java.io.Serial` imports, an unhandled `URISyntaxException`), bringing **203 of 206 sources** into the build. In parallel, the end-of-life dependency stack was modernized: `log4j 1.2.17` — carrying **6 CVEs with no fix available in the 1.x line, three of them CRITICAL** — was replaced with the `reload4j` drop-in, and the retired Apache Geronimo spec jars were replaced by the official Jakarta EE 8 API artifacts. The final CVE scan reports **zero known vulnerabilities across all 19 resolved artifacts**, the build compiles and packages cleanly on JDK 21 (bytecode major version 65) with zero OSGi bnd warnings, and the test result matches the baseline.

## 1. Upgrade Improvements

The project moved from a superficial Java 21 target — achieved by hiding non-compiling code — to a genuine one, while replacing every end-of-life framework dependency that had a maintained successor. The `javax.*` namespace was deliberately retained, because Apache Axis 1.4 / JAX-RPC underpins the SOAP providers and has no Jakarta EE 9+ successor; the Jakarta EE 8 artifacts used here keep that namespace, so the migration required no source changes at all.

| Area | Before | After | Improvement |
| ---- | ------ | ----- | ----------- |
| Sources compiled | 168 of 206 (38 excluded) | 203 of 206 (3 excluded) | Java 21 target genuinely met; +35 files, +40 classes |
| Service provider integrity | 2 of 5 registered providers missing from the JAR | All 5 present | Fixes a latent runtime provider-loading failure |
| JDK compiler API | `sun.tools.javac.Main` | `javax.tools.ToolProvider` | Removed a JDK-internal API deleted in Java 9 |
| CORBA / RMI-IIOP | `javax.rmi` from the JDK | `jboss-rmi-api_1.0_spec:1.0.6.Final` | Replaces the API removed by JEP 320 in Java 11 |
| Stack introspection | `SecurityManager.getClassContext()` | `StackWalker.getCallerClass()` | Removed an API deprecated for removal since JDK 17 |
| Logging backend | `log4j:log4j:1.2.17` | `ch.qos.reload4j:reload4j:1.2.26` | Eliminates 6 CVEs (3 CRITICAL); binary drop-in |
| JMS spec | `geronimo-jms_1.1_spec:1.1.1` | `jakarta.jms:jakarta.jms-api:2.0.3` | Retired Geronimo project → maintained Jakarta EE 8 |
| EJB spec | `geronimo-ejb_3.0_spec:1.0.1` | `jakarta.ejb:jakarta.ejb-api:3.2.6` | Retired Geronimo project → maintained Jakarta EE 8 |
| JCA spec | `geronimo-j2ee-connector_1.5_spec:2.0.0` | `jakarta.resource:jakarta.resource-api:1.7.4` | Retired Geronimo project → maintained Jakarta EE 8 |
| Mail | `javax.mail:mail:1.4.7` | `com.sun.mail:javax.mail:1.6.2` | Maintained successor coordinates |
| Activation | `javax.activation:activation:1.1.1` | `com.sun.activation:javax.activation:1.2.0` | Maintained successor; duplicate package eliminated |
| Servlet API | `javax.servlet:servlet-api:2.5` | *(removed)* | EOL artifact with zero references in the source tree |
| Commons Logging | 1.2 | 1.3.5 | Current maintained release |
| JUnit | 3.8.2 | 4.13.2 | Modern test framework available for future tests |
| OSGi bundling | maven-bundle-plugin 3.5.0 | 5.1.9 | Reliable Java 21 class-file parsing in bnd |
| Surefire / Javadoc | 3.0.0 / 3.5.0 (`source=8`) | 3.5.2 / 3.11.2 (`source=21`) | Toolchain aligned with the compiled release level |

### Key Benefits

**Performance & Security**

- Eliminated all 6 `log4j` 1.2.17 CVEs — including CVE-2019-17571, CVE-2022-23305 and CVE-2022-23307 (CRITICAL) — none of which had a fix available within the log4j 1.x line.
- Post-upgrade CVE scan across the full 19-artifact transitive closure reports **zero** known vulnerabilities.
- Removed three retired Apache Geronimo spec jars and one unused end-of-life servlet API, shrinking the unmaintained dependency surface.
- Access to Java 21 LTS security patches and JVM improvements (G1/ZGC, compact strings) through 2029+.

**Developer Productivity**

- 18% of the codebase is no longer invisible to the compiler, the IDE and static analysis — errors in the `compiler`, `ejb` and `apachesoap` trees now surface at build time instead of hiding behind exclusions.
- The OSGi bundle builds with **zero** bnd warnings, down from four unused `Import-Package` instructions that were symptoms of the hidden code.
- The build toolchain (compiler, surefire, javadoc, bundle plugin) is internally consistent at release 21.

**Future-Ready Foundation**

- The genuine Java 21 baseline (bytecode major version 65) makes any future JDK step a matter of resolving real, visible issues rather than rediscovering hidden ones.
- Jakarta EE 8 artifacts sit directly on the migration path to Jakarta EE 9+ should the SOAP stack ever be replaced.
- JUnit 4.13.2 is in place, so a test suite can be introduced without further build changes.

## 2. Build and Validation

### Build Validation

| Field      | Value                                                                                          |
| ---------- | ---------------------------------------------------------------------------------------------- |
| Status     | ✅ Success                                                                                      |
| Compiler   | Java 21.0.2 (`C:\dev\jdk-21`), `<release>21</release>`, verified bytecode major version 65      |
| Build Tool | Maven 3.9.16 (`C:\dev\apache-maven\bin\mvn.cmd`) — no wrapper present in the project            |
| Result     | `mvn clean test-compile` and `mvn clean package` both succeed. 203 of 206 sources compiled into 226 classes; OSGi bundle `wsif-2.0.jar` (589 KB, 294 entries) produced with zero bnd warnings |

### Test Validation

| Field          | Value                                                                                     |
| -------------- | ----------------------------------------------------------------------------------------- |
| Status         | ✅ Success (matches baseline)                                                              |
| Total Tests    | 0                                                                                          |
| Passed         | 0                                                                                          |
| Failed         | 0                                                                                          |
| Test Framework | JUnit 4.13.2 declared (`test` scope); no test sources are part of the Maven build          |

The project contains no test sources in the Maven build — the `test.bak/` directory is a deliberately disabled folder that predates this upgrade. Both the pre-upgrade baseline and the post-upgrade run report `No tests to run`, so the pass rate is unchanged at 0 of 0. Because compilation success alone cannot demonstrate runtime correctness, the following behavioural assertions were verified explicitly instead:

| Assertion | Result | Notes |
| --------- | ------ | ----- |
| Bytecode targets Java 21 | ✅ Passed | `WSIFService.class` major version = 65 |
| All 5 providers in `META-INF/services` present in the JAR | ✅ Passed | `_EJB`, `_Java`, `_ApacheAxis`, `_ApacheSOAP`, `_Jms` — 2 were missing before |
| Only the 3 SoapRMI files remain excluded | ✅ Passed | 203 of 206 sources compiled |
| No `log4j` 1.x, Geronimo or `javax.servlet` artifact resolves | ✅ Passed | Confirmed via `mvn dependency:tree` |
| Exactly one `activation` artifact on the classpath | ✅ Passed | Transitive `javax.activation:activation:1.1` excluded |
| No live `SecurityManager` / `sun.tools` references remain | ✅ Passed | Only explanatory comments matched |
| No TODO/FIXME introduced by the upgrade | ✅ Passed | All markers are pre-existing 2003-era Apache notes |
| OSGi manifest valid | ✅ Passed | `Bundle-SymbolicName: org.apache.wsif`, `Bundle-Version: 2.0.0` |

---

## 3. Limitations

- **SoapRMI provider cannot be compiled** (Genuinely unfixable)
  - **Root cause:** `providers/soap/soaprmi/**` (3 files) requires the `soaprmi.*` and `xpp` packages, which exist only as the vendored `lib/soaprmi-1_1.jar`. SoapRMI is a defunct academic project that was never published to Maven Central.
  - **Approaches attempted:** (1) resolution from Maven Central — the artifact does not exist; (2) substitution with a maintained successor — none exists, the project has been dead since ~2001; (3) `system`-scoped dependency on the vendored jar — rejected, as `system` scope is deprecated in Maven 3.9 and would embed an unaudited 2001-era binary into the build.
  - **Impact:** None. The provider is **not** registered in `META-INF/services/org.apache.wsif.spi.WSIFProvider`, so it was already unreachable dead code before this upgrade. Its single exclusion is retained and documented inline in `pom.xml`.

- **Apache Axis 1.4 retained** (Out of scope, accepted architectural risk)
  - **Root cause:** Axis 1.4 (2006, end-of-life) is the SOAP engine behind the `apacheaxis` provider and pins the project to the `javax.*` / JAX-RPC namespace. No successor implements the same API.
  - **Why not fixed:** Replacing it means rewriting the entire SOAP provider stack — far beyond a runtime upgrade. The Jakarta EE 8 artifacts chosen here deliberately preserve the `javax.*` namespace so Axis continues to work unchanged.
  - **Impact:** The current CVE scan reports no known advisories against `org.apache.axis:axis:1.4`, but it receives no security maintenance.

- **`AccessController.doPrivileged` retained in 6 files** (Acceptable)
  - **Root cause:** `java.security.AccessController` has been deprecated for removal since JDK 17, but remains fully functional on Java 21 — the stated target.
  - **Why not fixed:** Rewriting privileged blocks across `WSIFUtils`, `WSIFProperties`, `WSIFPluggableProviders`, `Schema2Java`, `ModelWSIFProvider`, `WSIFDynamicProvider_EJB` and `WSIFDynamicProvider_Jms` would alter security semantics for no benefit on Java 21.
  - **Impact:** None on Java 21. This will need attention before any move to JDK 24+, where the Security Manager was disabled by JEP 486.

- **`src/wsif.properties` references a sample class** (Pre-existing, unrelated to this upgrade)
  - Line 29 sets `wsif.servicefactory=customfactory.client.CustomServiceFactoryImpl`, a class from the `samples/` tree that is not on the library's runtime classpath. Left untouched to avoid changing behaviour; flagged for maintainer awareness.

---

## 4. Recommended next steps

I. **Generate unit test cases**: Line coverage is **0%** — the project has no test sources in the Maven build. This is the single largest risk to the upgrade, since correctness currently rests on compilation plus the targeted assertions in section 2. Use the "Generate Unit Tests" tool to establish a baseline suite, prioritising the four code paths changed by this upgrade: `Conventions.JDKcompile`, `StreamFactory.getURL`, `WSDL2WSDL.printUsage` and the restored EJB/Apache SOAP providers.

II. **Triage the legacy `test.bak/` suite**: The disabled folder contains a substantial historical test suite. Assess whether it can be revived against JUnit 4.13.2 (now on the classpath) and re-enabled as `src/test/java`, which would convert a large body of dead assets into real regression coverage.

III. **Smoke-test the two restored providers at runtime**: `WSIFDynamicProvider_EJB` and `WSIFDynamicProvider_ApacheSOAP` were absent from the JAR before this upgrade and have therefore not executed in a long time. Exercise them against a real endpoint to confirm the restored code paths behave as intended.

IV. **Plan the `AccessController` removal**: Required before any upgrade to JDK 24+ (JEP 486). Scope the rewrite of the 7 privileged blocks listed in section 3 as a standalone, security-reviewed change.

V. **Decide the long-term future of the Apache Axis 1.4 stack**: It is the only remaining unmaintained core dependency and the sole reason the project cannot move to the `jakarta.*` namespace. Evaluate whether the SOAP providers are still required, or whether the library can be reduced to the actively used providers.

VI. **Add a CI pipeline**: The repository has no CI/CD configuration. Adding a pipeline pinned to JDK 21 would prevent silent regressions — notably any reintroduction of compiler exclusions.

---

## 5. Additional details

<details>
<summary>Click to expand for upgrade details</summary>

### Project Details

| Field                 | Value                                             |
| --------------------- | ------------------------------------------------- |
| Session ID            | 20260825151027                                    |
| Upgrade executed by   | r.neubauer                                        |
| Upgrade performed by  | GitHub Copilot                                    |
| Project path          | C:\Users\r.neubauer\git\wsif\custom\ws-wsif\java   |
| Repository            | wsif (module `custom/ws-wsif/java`)               |
| Build tool (before)   | Maven 3.9.16                                      |
| Build tool (after)    | Maven 3.9.16 (unchanged — already meets Java 21)  |
| Files modified        | 9 project files (+ 2 session documents)           |
| Lines added / removed | +501 / -53                                        |
| Branch created        | appmod/java-upgrade-20260825151027                |

### Code Changes

1. **`pom.xml`**
   - **Changes:** Removed 5 of 6 compiler exclusions, modernized 11 dependencies, upgraded 3 build plugins, cleaned the OSGi import instructions
   - **Details:**
     - Excludes reduced to a single documented entry for `providers/soap/soaprmi/**`
     - Added `org.jboss.spec.javax.rmi:jboss-rmi-api_1.0_spec:1.0.6.Final` (`provided`) and `soap:soap:2.3.1` (`provided`, `optional`)
     - `log4j:log4j:1.2.17` → `ch.qos.reload4j:reload4j:1.2.26`
     - Geronimo JMS/EJB/JCA specs → `jakarta.jms-api:2.0.3`, `jakarta.ejb-api:3.2.6`, `jakarta.resource-api:1.7.4`
     - `javax.mail:mail:1.4.7` → `com.sun.mail:javax.mail:1.6.2`, with the transitive `javax.activation:activation:1.1` excluded
     - `javax.activation:activation:1.1.1` → `com.sun.activation:javax.activation:1.2.0`
     - `commons-logging` 1.2 → 1.3.5; `junit` 3.8.2 → 4.13.2; removed unused `javax.servlet:servlet-api:2.5`
     - `maven-bundle-plugin` 3.5.0 → 5.1.9, `maven-surefire-plugin` 3.0.0 → 3.5.2 (stale `<includes>` removed), `maven-javadoc-plugin` 3.5.0 → 3.11.2 with `<source>` 8 → 21
     - Dropped `sun.tools.*`, `soaprmi.*` and `xpp` from `osgi.imported.packages`

2. **`src/org/apache/wsif/compiler/schema/tools/Conventions.java`**
   - **Changes:** Replaced the JDK-internal compiler entry point in `JDKcompile`
   - **Before:** `return new sun.tools.javac.Main(System.err, "javac").compile(args);`
   - **After:** `javax.tools.ToolProvider.getSystemJavaCompiler()` with a null guard for JRE-only runtimes, returning `compiler.run(null, null, System.err, args) == 0`

3. **`src/org/apache/wsif/compiler/util/StreamFactory.java`**
   - **Changes:** Fixed an unhandled checked exception left by an earlier partial modernization
   - **Details:** `new URI(...).toURL()` had replaced a deprecated `URL` constructor without handling `URISyntaxException`; extracted a `toFileURL(String)` helper that re-throws it as `MalformedURLException`, preserving the declared contract of `getURL`. Added `import java.net.URISyntaxException;`

4. **`src/org/apache/wsif/tools/WSDL2WSDL.java`**
   - **Changes:** Removed the last `SecurityManager` usage
   - **Before:** `protected static class FindThisClassName extends SecurityManager` reading `getClassContext()[1]`
   - **After:** `private static String findThisClassName()` using `StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass()`, which resolves the identical stack frame

5. **`WSIFPort_EJB.java`, `WSIFOperation_EJB.java`, `WSIFPort_ApacheSOAP.java`, `WSIFOperation_ApacheSOAP.java`, `WSIFOperation_SoapRMI.java`**
   - **Changes:** Added the missing `import java.io.Serial;`
   - **Details:** All five already used the Java 14+ `@Serial` annotation; because they sat behind compiler exclusions, the missing imports had never been reported

All changes are committed to `appmod/java-upgrade-20260825151027` across three reviewable commits (`5d31b59`, `e4f6518`, `04457d3`) and are ready for review.

### Automated tasks

- Full-source compilation probe to identify the real cause of every compiler exclusion
- Restoration of 35 previously excluded source files into the build
- JDK-removed API replacement (`sun.tools.javac`, `javax.rmi`, `SecurityManager`)
- Framework and spec dependency modernization (11 dependencies)
- Build plugin upgrades and toolchain alignment
- Dependency-tree analysis and duplicate-package elimination
- CVE scanning of the full transitive closure, before and after
- Packaging, bytecode-level and OSGi manifest verification

### Potential Issues

#### CVEs

**Scan Status**: ✅ All CVEs resolved

**Scanned**: 19 dependencies (direct + transitive) | **Found**: 6 | **Auto-fixed**: 6 | **Remaining**: 0

| Severity | CVE ID         | Dependency        | Before | After                            | Status   |
| -------- | -------------- | ----------------- | ------ | -------------------------------- | -------- |
| Critical | CVE-2019-17571 | log4j:log4j       | 1.2.17 | ch.qos.reload4j:reload4j 1.2.26  | ✅ Fixed |
| Critical | CVE-2022-23305 | log4j:log4j       | 1.2.17 | ch.qos.reload4j:reload4j 1.2.26  | ✅ Fixed |
| Critical | CVE-2022-23307 | log4j:log4j       | 1.2.17 | ch.qos.reload4j:reload4j 1.2.26  | ✅ Fixed |
| High     | CVE-2021-4104  | log4j:log4j       | 1.2.17 | ch.qos.reload4j:reload4j 1.2.26  | ✅ Fixed |
| High     | CVE-2022-23302 | log4j:log4j       | 1.2.17 | ch.qos.reload4j:reload4j 1.2.26  | ✅ Fixed |
| High     | CVE-2023-26464 | log4j:log4j       | 1.2.17 | ch.qos.reload4j:reload4j 1.2.26  | ✅ Fixed |

None of these six CVEs had a fix available within the `log4j` 1.x line — the advisories direct users to migrate away from `log4j:log4j` entirely. `reload4j` is the maintained, binary-compatible fork created for exactly this purpose. Because WSIF contains **no direct references to `org.apache.log4j`** (the artifact only supplies the backend that `commons-logging` discovers reflectively on behalf of Apache Axis), the substitution removes the vulnerable code while leaving runtime behaviour unchanged.

</details>

