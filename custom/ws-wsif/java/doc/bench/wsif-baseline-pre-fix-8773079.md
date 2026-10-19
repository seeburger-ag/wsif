# WSIF Phase 0 baseline benchmark

- Date: 2026-10-19T17:44:21.9968894
- Label: pre-fix-8773079
- JVM: Java HotSpot(TM) 64-Bit Server VM 21.0.2+13-LTS-58
- OS: Windows 11, CPUs: 20
- Max heap: 16288 MB


## Hot spots (1 thread, bpengine-like TCCL hiding wsif.properties)

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| WSIFPluggableProviders.getProvider(java) | 1 | 0.016 | 63,257,483 | 0 |
| WSIFPluggableProviders.isProviderAvailable(java) | 1 | 0.247 | 4,055,775 | 1,208 |
| WSIFProperties.getSyncTimeout() [file hidden] | 1 | 1.152 | 868,281 | 1,480 |
| WSIFProperties.getSyncTimeout() [file visible] | 1 | 0.019 | 52,005,945 | 48 |
| WSIFMapperFactory.newMapper() | 1 | 0.444 | 2,251,776 | 288 |
| WSIFMappingConventionFactory.newMappingConvention() | 1 | 0.498 | 2,008,119 | 152 |

## Invocation path (1 thread) - WSDL: 5 ops, 1 portTypes, 10 messages

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| getService | 1 | 1.559 | 641,574 | 2,176 |
| getService + getPort | 1 | 3.730 | 268,113 | 8,016 |
| createOperation (ServiceInvoker.createOperation) | 1 | 5.447 | 183,597 | 10,576 |
| full sync invoke (ServiceInvoker.invoke) | 1 | 5.838 | 171,293 | 11,760 |
| full sync invoke, factory service caching ON | 1 | 2.843 | 351,794 | 5,808 |

## Invocation path (1 thread) - WSDL: 20 ops, 51 portTypes, 540 messages

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| getService | 1 | 2.924 | 341,984 | 2,176 |
| getService + getPort | 1 | 6.648 | 150,425 | 8,016 |
| createOperation (ServiceInvoker.createOperation) | 1 | 7.979 | 125,331 | 10,576 |
| full sync invoke (ServiceInvoker.invoke) | 1 | 9.183 | 108,903 | 11,760 |
| full sync invoke, factory service caching ON | 1 | 4.903 | 203,972 | 5,808 |

## Scalability - full sync invoke, WSDL: 20 ops / 51 portTypes

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| full sync invoke | 1 | 9.838 | 101,650 | 11,760 |
| full sync invoke | 2 | 9.153 | 218,500 | 11,760 |
| full sync invoke | 4 | 11.937 | 335,091 | 11,760 |
| full sync invoke | 8 | 27.088 | 295,333 | 11,760 |
| full sync invoke | 16 | 56.367 | 283,852 | 11,760 |

## Scalability - WSIFMapperFactory.newMapper() (global lock)

| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |
|---|---:|---:|---:|---:|
| newMapper | 1 | 0.319 | 3,137,782 | 288 |
| newMapper | 2 | 1.903 | 1,050,838 | 288 |
| newMapper | 4 | 2.644 | 1,513,019 | 288 |
| newMapper | 8 | 5.462 | 1,464,566 | 288 |
| newMapper | 16 | 12.844 | 1,245,740 | 288 |
