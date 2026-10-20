# WSIF Phase 0 baseline benchmark

- Date: 2026-10-20T12:10:49.2952524
- Label: phase1-run2
- JVM: Java HotSpot(TM) 64-Bit Server VM 21.0.2+13-LTS-58
- OS: Windows 11, CPUs: 20
- Max heap: 16288 MB


## Hot spots (1 thread, bpengine-like TCCL hiding wsif.properties)

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| WSIFPluggableProviders.getProvider(java) | 1 | 0.016 | 64,359,712 | 0 |
| WSIFPluggableProviders.isProviderAvailable(java) | 1 | 0.058 | 17,272,321 | 120 |
| WSIFProperties.getSyncTimeout() [file hidden] | 1 | 0.015 | 65,056,504 | 0 |
| WSIFProperties.getSyncTimeout() [file visible] | 1 | 0.021 | 48,591,069 | 48 |
| WSIFMapperFactory.newMapper() | 1 | 0.450 | 2,219,923 | 288 |
| WSIFMappingConventionFactory.newMappingConvention() | 1 | 0.507 | 1,973,099 | 152 |

## Invocation path (1 thread) - WSDL: 5 ops, 1 portTypes, 10 messages

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| getService | 1 | 1.376 | 726,883 | 1,136 |
| getService + getPort | 1 | 3.577 | 279,566 | 6,976 |
| createOperation (ServiceInvoker.createOperation) | 1 | 5.712 | 175,070 | 9,488 |
| full sync invoke (ServiceInvoker.invoke) | 1 | 5.338 | 187,332 | 10,672 |
| full sync invoke, factory service caching ON | 1 | 3.281 | 304,749 | 5,688 |

## Invocation path (1 thread) - WSDL: 20 ops, 51 portTypes, 540 messages

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| getService | 1 | 3.159 | 316,507 | 1,136 |
| getService + getPort | 1 | 6.443 | 155,219 | 6,976 |
| createOperation (ServiceInvoker.createOperation) | 1 | 9.101 | 109,877 | 9,488 |
| full sync invoke (ServiceInvoker.invoke) | 1 | 10.137 | 98,650 | 10,672 |
| full sync invoke, factory service caching ON | 1 | 5.319 | 188,015 | 5,688 |

## Scalability - full sync invoke, WSDL: 20 ops / 51 portTypes

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| full sync invoke | 1 | 9.739 | 102,683 | 10,672 |
| full sync invoke | 2 | 9.788 | 204,329 | 10,672 |
| full sync invoke | 4 | 12.997 | 307,764 | 10,672 |
| full sync invoke | 8 | 35.122 | 227,774 | 10,672 |
| full sync invoke | 16 | 87.581 | 182,688 | 10,672 |

## Scalability - WSIFMapperFactory.newMapper() (global lock)

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| newMapper | 1 | 0.442 | 2,261,216 | 288 |
| newMapper | 2 | 1.577 | 1,267,990 | 288 |
| newMapper | 4 | 3.479 | 1,149,769 | 288 |
| newMapper | 8 | 6.821 | 1,172,892 | 288 |
| newMapper | 16 | 15.012 | 1,065,833 | 288 |
