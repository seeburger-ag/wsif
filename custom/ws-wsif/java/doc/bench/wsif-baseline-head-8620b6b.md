# WSIF Phase 0 baseline benchmark

- Date: 2026-10-19T17:46:09.0017291
- Label: head-8620b6b
- JVM: Java HotSpot(TM) 64-Bit Server VM 21.0.2+13-LTS-58
- OS: Windows 11, CPUs: 20
- Max heap: 16288 MB


## Hot spots (1 thread, bpengine-like TCCL hiding wsif.properties)

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| WSIFPluggableProviders.getProvider(java) | 1 | 0.012 | 83,018,821 | 0 |
| WSIFPluggableProviders.isProviderAvailable(java) | 1 | 0.168 | 5,942,789 | 1,208 |
| WSIFProperties.getSyncTimeout() [file hidden] | 1 | 0.012 | 84,272,886 | 0 |
| WSIFProperties.getSyncTimeout() [file visible] | 1 | 0.016 | 61,742,754 | 48 |
| WSIFMapperFactory.newMapper() | 1 | 0.339 | 2,948,823 | 288 |
| WSIFMappingConventionFactory.newMappingConvention() | 1 | 0.460 | 2,175,681 | 152 |

## Invocation path (1 thread) - WSDL: 5 ops, 1 portTypes, 10 messages

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| getService | 1 | 1.229 | 813,616 | 2,176 |
| getService + getPort | 1 | 3.120 | 320,547 | 8,016 |
| createOperation (ServiceInvoker.createOperation) | 1 | 4.503 | 222,058 | 10,576 |
| full sync invoke (ServiceInvoker.invoke) | 1 | 6.131 | 163,095 | 11,760 |
| full sync invoke, factory service caching ON | 1 | 4.687 | 213,375 | 5,856 |

## Invocation path (1 thread) - WSDL: 20 ops, 51 portTypes, 540 messages

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| getService | 1 | 3.523 | 283,884 | 2,176 |
| getService + getPort | 1 | 6.858 | 145,808 | 8,016 |
| createOperation (ServiceInvoker.createOperation) | 1 | 8.708 | 114,839 | 10,576 |
| full sync invoke (ServiceInvoker.invoke) | 1 | 8.810 | 113,511 | 11,760 |
| full sync invoke, factory service caching ON | 1 | 5.093 | 196,350 | 5,808 |

## Scalability - full sync invoke, WSDL: 20 ops / 51 portTypes

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| full sync invoke | 1 | 9.115 | 109,705 | 11,760 |
| full sync invoke | 2 | 10.140 | 197,248 | 11,760 |
| full sync invoke | 4 | 13.544 | 295,342 | 11,760 |
| full sync invoke | 8 | 28.445 | 281,242 | 11,760 |
| full sync invoke | 16 | 95.054 | 168,325 | 11,760 |

## Scalability - WSIFMapperFactory.newMapper() (global lock)

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| newMapper | 1 | 0.589 | 1,699,200 | 288 |
| newMapper | 2 | 2.659 | 752,125 | 288 |
| newMapper | 4 | 3.643 | 1,097,984 | 288 |
| newMapper | 8 | 7.267 | 1,100,846 | 288 |
| newMapper | 16 | 15.849 | 1,009,522 | 288 |
