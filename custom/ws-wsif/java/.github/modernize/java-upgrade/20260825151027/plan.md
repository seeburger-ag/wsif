# Upgrade Plan: WSIF (20260825151027)

- **Generated**: 2026-08-25 17:35 (+02:00)
- **HEAD Branch**: appmod/java-upgrade-20260113150044
- **HEAD Commit ID**: N/A (not exposed by the version-control tool; working tree is clean)

## Available Tools

**JDKs**
- JDK 21.0.2: `C:\dev\jdk-21` (target JDK — used by every step; also serves as the base JDK for the baseline)

**Build Tools**
- Maven 3.9.16: `C:\dev\apache-maven\bin\mvn.cmd` (meets the Maven 3.9+ recommendation for Java 21)
- No Maven Wrapper (`mvnw`) present in the project — the system Maven is used.

> No JDK or build-tool installation is required. Step 1 is therefore a verification-only setup step.

## Guidelines

- Upgrade the Java runtime target and the Java framework dependencies to Java 21 LTS.
- Preserve the `javax.*` namespace. Apache Axis 1.4 (JAX-RPC) is the core SOAP engine of this library and has **no** Jakarta EE 9+ successor; a `javax` → `jakarta` namespace migration would require rewriting the entire SOAP provider stack and is explicitly **out of scope**. Framework specs are therefore aligned to **Jakarta EE 8**, which retains the `javax.*` namespace.
- Prefer drop-in, behaviour-preserving replacements over rewrites.

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260825151027
- Run tests before and after the upgrade: true

## Upgrade Goals

- **Java runtime: 21 (LTS)** — the build must genuinely compile the *entire* source tree on JDK 21.
- **Java framework dependencies: latest versions compatible with Java 21** and free of known CVEs.

## Technology Stack

| Technology/Dependency                                | Current   | Min Compatible | Why Incompatible                                                                                 |
| ---------------------------------------------------- | --------- | -------------- | ------------------------------------------------------------------------------------------------ |
| Java (`maven.compiler.*` / `release`)                | 21        | 21             | Already set — but 38 of 206 sources are excluded from compilation, so the target is not truly met  |
| Maven                                                | 3.9.16    | 3.9.0          | -                                                                                                  |
| `sun.tools.javac.Main` (JDK internal) ⚠️ EOL         | JDK 1.4   | N/A            | `tools.jar` removed in JDK 9+; replaced by `javax.tools.ToolProvider`                              |
| `javax.rmi.PortableRemoteObject` (JDK CORBA) ⚠️ EOL  | JDK 8     | N/A            | CORBA removed from the JDK in Java 11 (JEP 320); must come from an external artifact               |
| `java.lang.SecurityManager`                          | JDK 1.0   | N/A            | Deprecated for removal since JDK 17; replaced by `StackWalker`                                     |
| `java.security.AccessController`                     | JDK 1.2   | N/A            | Deprecated for removal since JDK 17; still functional on Java 21                                   |
| `log4j:log4j` ⚠️ EOL                                 | 1.2.17    | N/A            | EOL 2015; **6 CVEs (4 CRITICAL/HIGH), no fix in the 1.x line**. Drop-in successor: `reload4j`      |
| `org.apache.geronimo.specs:*` ⚠️ EOL                 | various   | N/A            | Apache Geronimo retired (2021); superseded by the official Jakarta EE 8 API jars (`javax` ns)      |
| `javax.mail:mail` ⚠️ EOL                             | 1.4.7     | N/A            | Unmaintained coordinates; successor is `com.sun.mail:javax.mail` (same `javax` ns)                 |
| `javax.activation:activation` ⚠️ EOL                 | 1.1.1     | N/A            | Unmaintained coordinates; successor is `com.sun.activation:javax.activation` (same `javax` ns)     |
| `javax.servlet:servlet-api` ⚠️ EOL                   | 2.5       | N/A            | **Zero references in the source tree** — dead dependency                                           |
| `commons-logging`                                    | 1.2       | 1.2            | Works, but 1.3.5 is the current maintained release                                                 |
| `junit:junit`                                        | 3.8.2     | 3.8.2          | Works, but 3.x is long superseded; no test sources currently exist in the build                    |
| `org.apache.axis:axis*` ⚠️ EOL                       | 1.4       | 1.4            | Terminal release (2006). No successor exists — **kept**, see Risks                                 |
| `soaprmi` (`lib/soaprmi-1_1.jar`) ⚠️ EOL             | 1.1       | N/A            | Defunct academic project; **not published to Maven Central** — see Risks                           |
| `wsdl4j:wsdl4j`                                      | 1.6.3     | 1.6.3          | - (already the latest release)                                                                     |
| `xerces:xercesImpl`                                  | 2.12.2    | 2.12.2         | - (already the latest release)                                                                     |
| `maven-compiler-plugin`                              | 3.14.1    | 3.11.0         | -                                                                                                  |
| `maven-surefire-plugin`                              | 3.0.0     | 3.0.0          | Works, but 3.5.2 is current and configured for a non-existent test include                         |
| `maven-bundle-plugin` (Felix)                        | 3.5.0     | 5.1.9          | 3.5.0 predates Java 9+ class-file support in bnd; brittle on Java 21 bytecode                      |
| `maven-javadoc-plugin`                               | 3.5.0     | 3.11.2         | Configured with `<source>8</source>`, which conflicts with Java 21 sources                         |

## Derived Upgrades

| Derived Upgrade | Justification |
| --- | --- |
| `sun.tools.javac.Main` → `javax.tools.ToolProvider` | Mandatory for Java 21: `tools.jar` and the `sun.tools.javac` package no longer exist. This is the sole blocker for the `compiler/**` tree (18 files). |
| Add `org.jboss.spec.javax.rmi:jboss-rmi-api_1.0_spec` | Mandatory for Java 21: JEP 320 removed CORBA (and `javax.rmi.PortableRemoteObject`) from the JDK in Java 11. Sole blocker for `providers/ejb/**`. |
| Add `soap:soap:2.3.1` (`provided`) | The `org.apache.soap.*` packages are the sole blocker for `providers/soap/apachesoap/**` (7 files) and `util/jms/JMS2HTTPBridge.java`. Available on Maven Central and CVE-clean. |
| Add missing `import java.io.Serial;` (5 files) | A previous partial modernization pass added Java 14+ `@Serial` annotations, but the affected files were hidden behind compiler excludes so the missing imports were never surfaced. |
| `SecurityManager` → `StackWalker` | Removes a deprecated-for-removal JDK API using an exactly equivalent Java 9+ replacement. |
| `log4j:log4j:1.2.17` → `ch.qos.reload4j:reload4j:1.2.26` | Resolves 6 CVEs with a binary drop-in that keeps the `org.apache.log4j` API (which `commons-logging` discovers reflectively for Axis). |
| Geronimo spec jars → Jakarta EE 8 API jars | Geronimo is retired. The Jakarta EE 8 artifacts are the official maintained successors and **retain the `javax.*` namespace**, so no source changes are required. |
| `maven-bundle-plugin` 3.5.0 → 5.1.9 | Required so bnd can reliably parse Java 21 class files when building the OSGi bundle. |
| `maven-javadoc-plugin` `<source>8</source>` → `21` | The sources are compiled at release 21; a `source=8` Javadoc run would fail. |

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|-----------|---------|--------|--------|--------|
| pom.xml | `org.jboss.spec.javax.rmi:jboss-rmi-api_1.0_spec` | – | add (`provided`) | 1.0.6.Final | Supplies `javax.rmi.PortableRemoteObject`, removed from the JDK by JEP 320 |
| pom.xml | `soap:soap` | – | add (`provided`, `optional`) | 2.3.1 | Supplies `org.apache.soap.*` for the Apache SOAP provider + JMS2HTTPBridge |
| pom.xml | `log4j:log4j` | 1.2.17 | replace | `ch.qos.reload4j:reload4j:1.2.26` | 6 CVEs (CVE-2019-17571, CVE-2021-4104, CVE-2022-23302/23305/23307, CVE-2023-26464) |
| pom.xml | `org.apache.geronimo.specs:geronimo-jms_1.1_spec` | 1.1.1 | replace | `jakarta.jms:jakarta.jms-api:2.0.3` | Geronimo retired; Jakarta EE 8 keeps the `javax.jms` namespace |
| pom.xml | `org.apache.geronimo.specs:geronimo-ejb_3.0_spec` | 1.0.1 | replace | `jakarta.ejb:jakarta.ejb-api:3.2.6` | Geronimo retired; Jakarta EE 8 keeps the `javax.ejb` namespace |
| pom.xml | `org.apache.geronimo.specs:geronimo-j2ee-connector_1.5_spec` | 2.0.0 | replace | `jakarta.resource:jakarta.resource-api:1.7.4` | Geronimo retired; Jakarta EE 8 keeps the `javax.resource` namespace |
| pom.xml | `javax.mail:mail` | 1.4.7 | replace | `com.sun.mail:javax.mail:1.6.2` | Maintained successor coordinates, same `javax.mail` namespace |
| pom.xml | `javax.activation:activation` | 1.1.1 | replace | `com.sun.activation:javax.activation:1.2.0` | Maintained successor coordinates, same `javax.activation` namespace |
| pom.xml | `javax.servlet:servlet-api` | 2.5 | remove | – | **Zero references** in the source tree; EOL dead weight (`provided` scope, no runtime impact) |
| pom.xml | `commons-logging:commons-logging` | 1.2 | upgrade | 1.3.5 | Current maintained release |
| pom.xml | `junit:junit` | 3.8.2 | upgrade | 4.13.2 | 3.x long superseded; `test` scope only |
| pom.xml | `org.apache.axis:axis`, `axis-jaxrpc`, `axis-saaj` | 1.4 | keep | 1.4 | Terminal EOL release; no successor exists (see Risks) |
| pom.xml | `wsdl4j:wsdl4j`, `xerces:xercesImpl`, `xml-apis:xml-apis`, `commons-discovery` | current | keep | current | Already at their latest sane releases |
| pom.xml | `maven-bundle-plugin` | 3.5.0 | upgrade | 5.1.9 | Reliable Java 21 class-file parsing in bnd |
| pom.xml | `maven-surefire-plugin` | 3.0.0 | upgrade | 3.5.2 | Current release; Java 21 support |
| pom.xml | `maven-javadoc-plugin` | 3.5.0 | upgrade | 3.11.2 | Current release |

### Source Code Changes

| File | Location | Current | Required Change | Reason |
|------|----------|---------|----------------|--------|
| `compiler/schema/tools/Conventions.java` | line 273 (`JDKcompile`) | `return new sun.tools.javac.Main(System.err, "javac").compile(args);` | Rewrite to `javax.tools.ToolProvider.getSystemJavaCompiler()`; return `compiler.run(null, null, System.err, args) == 0`. Retain the existing `catch (Throwable)` fallback and add a null-compiler guard (JRE-only runtime). | `sun.tools.javac` does not exist in JDK 9+ |
| `tools/WSDL2WSDL.java` | lines 326–330 (`FindThisClassName`) | `protected static class FindThisClassName extends SecurityManager { … getClassContext()[1].getName(); }` | Replace with `StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass().getName()`. `getClassContext()[1]` is the caller of `getName()`, which `getCallerClass()` returns exactly. | `SecurityManager` deprecated for removal (JDK 17+) |
| `tools/WSDL2WSDL.java` | line 387 | `String thisClassName = new FindThisClassName().getName();` | Update to the new helper call | Follows the change above |
| `providers/ejb/WSIFPort_EJB.java` | import block | *(missing)* | Add `import java.io.Serial;` | `@Serial` used at line 66 without its import |
| `providers/ejb/WSIFOperation_EJB.java` | import block | *(missing)* | Add `import java.io.Serial;` | `@Serial` used at line 77 without its import |
| `providers/soap/apachesoap/WSIFOperation_ApacheSOAP.java` | import block | *(missing)* | Add `import java.io.Serial;` | `@Serial` used without its import |
| `providers/soap/apachesoap/WSIFPort_ApacheSOAP.java` | import block | *(missing)* | Add `import java.io.Serial;` | `@Serial` used without its import |
| `providers/soap/soaprmi/WSIFOperation_SoapRMI.java` | import block | *(missing)* | Add `import java.io.Serial;` (file remains excluded — see Risks) | `@Serial` used without its import |

### Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|-----------------|--------|
| pom.xml | `maven-compiler-plugin/excludes` | 6 exclusion patterns hiding 38 of 206 sources | Remove **5 of 6**: `compiler/**`, `providers/ejb/**`, `wsdl/extensions/ejb/**`, `providers/soap/apachesoap/**`, `util/jms/JMS2HTTPBridge.java`. Keep only `providers/soap/soaprmi/**`. | Without this the Java 21 goal is only nominally met; 18% of the codebase is not compiled |
| pom.xml | `maven-javadoc-plugin/source` | `8` | `21` | Must match the compiled release level |
| pom.xml | `maven-surefire-plugin/includes` | `**/util/WSIFTestRunner.java` | Remove the stale `includes` block | The referenced class does not exist in the build (test sources live in the disabled `test.bak/` folder) |
| pom.xml | `osgi.imported.packages` | includes `sun.tools.*` | Drop `sun.tools.*;resolution:=optional` | The `sun.tools.javac` reference is being removed from the source |
| pom.xml | `maven.compiler.source` / `maven.compiler.target` | `21` / `21` | Keep (harmless alongside `<release>`) | No change needed |

### CI/CD Changes

| File | Location | Current | Required Change |
|------|----------|---------|-----------------|
| – | – | – | **None.** The repository contains no Dockerfile, CI workflow, pipeline or Jenkinsfile. The legacy `build.xml` / `build.bat` / `build.sh` Ant scripts set no `source`/`target` and are not part of the Maven build. |

### Risks & Warnings

- **SoapRMI provider cannot be restored (genuine limitation).** `providers/soap/soaprmi/**` depends on `soaprmi.*` and `xpp`, shipped only as `lib/soaprmi-1_1.jar`. SoapRMI is a defunct academic project never published to Maven Central. **Mitigation**: keep this single exclusion, add the missing `Serial` import for consistency, and document it. The provider is *not* registered in `META-INF/services/org.apache.wsif.spi.WSIFProvider`, so it is already unreachable dead code — no functional regression. Restoring it would require vendoring an unmaintained, unaudited 2001-era binary.

- **Latent runtime defect being fixed.** `META-INF/services/org.apache.wsif.spi.WSIFProvider` registers `WSIFDynamicProvider_EJB` and `WSIFDynamicProvider_ApacheSOAP`, but both classes are currently excluded from compilation and are therefore **absent from the produced JAR**. Any runtime provider scan fails on them today. **Mitigation**: restoring these two trees (Step 3) fixes the inconsistency; verify the packaged JAR contains both classes as part of Final Validation.

- **Apache Axis 1.4 is EOL with no successor.** It is the backbone of the `apacheaxis` provider and pins the whole project to the `javax.*` / JAX-RPC namespace. **Mitigation**: out of scope for a Java 21 upgrade — retained deliberately. The current CVE scan reports no known advisories for it. Recorded as an accepted architectural risk; a future migration would mean replacing the SOAP stack outright.

- **`AccessController.doPrivileged` retained in 6 files** (`WSIFUtils`, `WSIFProperties`, `WSIFPluggableProviders`, `Schema2Java`, `ModelWSIFProvider`, `WSIFDynamicProvider_EJB`, `WSIFDynamicProvider_Jms`). Deprecated for removal since JDK 17 but **fully functional on Java 21**, which is the stated target. **Mitigation**: deliberately not rewritten (removal would alter security semantics across 6 files for no benefit on Java 21). Documented as a forward-looking risk for any future JDK 24+/25 upgrade.

- **Zero automated test coverage.** The project has no test sources in the build (`test.bak/` is a deliberately disabled folder), so `mvn test` will always report 0 tests. **Compilation success alone cannot prove runtime correctness.** **Mitigation**: every source change in this plan is a mechanically equivalent, deterministic rewrite (documented line-by-line above); Final Validation additionally verifies the packaged OSGi bundle and asserts that the previously-excluded provider classes are present in the JAR. Adding a test suite is offered as a follow-up.

- **Jakarta EE 8 spec-jar swap uses wildcard imports.** The JCA provider (`WSIFOperation_JCA`, `WSIFPort_JCA`, `WSIFProviderJCAExtensions`, `WSIFUtils_JCA`) uses `import javax.resource.*;` / `import javax.resource.cci.*;`, and the swap moves JCA 1.5 → 1.7. **Mitigation**: performed in its own isolated step (Step 4) with a compile gate; if any ambiguity or missing type appears, revert that single artifact to the Geronimo spec jar and record the exception.

- **`src/wsif.properties` line 29** sets `wsif.servicefactory=customfactory.client.CustomServiceFactoryImpl`, a class from the `samples/` tree that is not on the library's runtime classpath. This is a **pre-existing** condition unrelated to the upgrade. **Mitigation**: left untouched to avoid changing behaviour; flagged here for the maintainers' awareness.

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Confirm the toolchain before changing anything. JDK 21.0.2 and Maven 3.9.16 are already installed, so nothing needs downloading.
  - **Changes to Make**: None (verification only).
  - **Verification**: `#appmod-list-jdks` — JDK 21 present at `C:\dev\jdk-21`; Maven 3.9.16 present. Expected: all required tooling available.

- **Step 2: Setup Baseline**
  - **Rationale**: Record the pre-upgrade build and test state to form the acceptance criteria. JDK 21 is both the base and the target JDK here.
  - **Changes to Make**: None.
  - **Verification**: `mvn clean test-compile` then `mvn clean test`, JDK 21. Expected: BUILD SUCCESS with 0 tests executed and 38 of 206 sources excluded — this is the baseline to beat.

- **Step 3: Restore all excluded source trees to the Java 21 build**
  - **Rationale**: This is the substance of the Java 21 goal. Today the build only succeeds because 18% of the codebase is hidden behind compiler excludes; two of the hidden provider classes are registered in `META-INF/services` and therefore already broken at runtime. Grouped into one step so the project compiles cleanly at the end of it.
  - **Changes to Make**: Apply all *Source Code Changes* except the `WSDL2WSDL` rows (the `ToolProvider` rewrite and all five `import java.io.Serial;` additions); add the `jboss-rmi-api_1.0_spec` and `soap:soap` entries from *Dependency Changes*; remove 5 of the 6 compiler `excludes` and drop `sun.tools.*` from `osgi.imported.packages` per *Configuration Changes*.
  - **Verification**: `mvn clean test-compile`, JDK 21. Expected: BUILD SUCCESS with 203 of 206 sources compiled (only the 3 SoapRMI files remain excluded).

- **Step 4: Modernize framework & spec dependencies**
  - **Rationale**: Delivers the "framework dependencies" half of the goal and eliminates the 6 log4j CVEs. Isolated from Step 3 so a spec-jar regression is trivially attributable and revertible.
  - **Changes to Make**: Apply the remaining *Dependency Changes* rows — reload4j, the three Jakarta EE 8 spec jars, mail, activation, commons-logging, junit, and removal of the unused `javax.servlet:servlet-api`.
  - **Verification**: `mvn clean test-compile`, JDK 21. Expected: BUILD SUCCESS, unchanged compiled-source count.

- **Step 5: Remove deprecated-for-removal JDK APIs and upgrade build plugins**
  - **Rationale**: Completes the Java 21 modernization by removing the last `SecurityManager` usage and bringing the build plugins to versions that handle Java 21 bytecode reliably.
  - **Changes to Make**: Apply the two `tools/WSDL2WSDL.java` rows from *Source Code Changes*; upgrade `maven-bundle-plugin`, `maven-surefire-plugin` and `maven-javadoc-plugin`, set Javadoc `<source>` to 21 and drop the stale surefire `<includes>` per *Configuration Changes*.
  - **Verification**: `mvn clean package`, JDK 21. Expected: BUILD SUCCESS and a valid OSGi bundle.

- **Step 6: CVE Validation & Fix**
  - **Rationale**: Confirm the dependency modernization actually eliminated the known vulnerabilities and catch anything introduced by the new artifacts.
  - **Changes to Make**: Extract direct dependencies via `mvn dependency:list -DexcludeTransitive=true`, scan with `#appmod-validate-cves-for-java`, and upgrade any flagged artifact that has a patched release.
  - **Verification**: `mvn clean test-compile`, JDK 21, followed by a re-scan. Expected: no remaining CVEs with an available fix; the 6 log4j CVEs resolved.

- **Step 7: Final Validation**
  - **Rationale**: Prove every goal is met and no deferred work or workaround remains.
  - **Changes to Make**: Resolve any outstanding TODOs; no planned changes.
  - **Verification**: `mvn clean test-compile` and `mvn clean test` on JDK 21, then `mvn clean package`. Additionally assert that `target/classes` and the packaged JAR contain `WSIFDynamicProvider_EJB` and `WSIFDynamicProvider_ApacheSOAP` (the classes named in `META-INF/services`), and confirm exactly 3 source files remain excluded. Expected: BUILD SUCCESS, test pass rate ≥ baseline, all goals met.

