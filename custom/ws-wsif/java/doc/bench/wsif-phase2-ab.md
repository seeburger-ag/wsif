# WSIF Phase 2 – A/B benchmark (median of 3 runs each)

- Date: 2026-10-20
- Phase 1 = committed code `c6b54d1` (+ `325736c`, pom only), Phase 2 = this change set
- Runs interleaved on the same machine: Phase 1 `ab2`, `ab3`, `ab4`; Phase 2 `ab1`, `ab2`, `ab3`
- JVM: HotSpot 21.0.2, Windows 11, 20 CPUs
- **Note:** the machine was about 2.5–3× slower during these runs than during the Phase 0/1
  measurements (see the unchanged control benchmarks, e.g. `newMapper` 0.44 → 1.3 µs).
  Only the side-by-side columns of this table are comparable; absolute values must not be
  compared with `wsif-baseline-*.md`.
- Control rows (code unchanged in Phase 2): `getProvider`, `isProviderAvailable`,
  `getSyncTimeout`, `newMapper`, `newMappingConvention` – they agree within ±13%, which is
  the noise level of this comparison.

| Benchmark | Phase 1 µs/op | Phase 2 µs/op | Δ time | Phase 1 B/op | Phase 2 B/op | Δ alloc | Phase 1 ops/s | Phase 2 ops/s |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Hot spots :: WSIFPluggableProviders.getProvider(java) [1T] | 0.016 | 0.018 | +13% | 0 | 0 | – | 60,987,865 | 55,177,829 |
| Hot spots :: WSIFPluggableProviders.isProviderAvailable(java) [1T] | 0.172 | 0.151 | -12% | 120 | 120 | 0% | 5,830,764 | 6,623,845 |
| Hot spots :: WSIFProperties.getSyncTimeout() [file hidden] [1T] | 0.046 | 0.044 | -4% | 0 | 0 | – | 21,892,554 | 22,645,601 |
| Hot spots :: WSIFProperties.getSyncTimeout() [file visible] [1T] | 0.077 | 0.070 | -9% | 48 | 48 | 0% | 13,002,985 | 14,347,506 |
| Hot spots :: WSIFMapperFactory.newMapper() [1T] | 1.297 | 1.429 | +10% | 288 | 288 | 0% | 771,076 | 699,596 |
| Hot spots :: WSIFMappingConventionFactory.newMappingConvention() [1T] | 1.518 | 1.577 | +4% | 152 | 152 | 0% | 658,677 | 634,221 |
| Invocation path (1 thread) small WSDL :: getService [1T] | 3.990 | 0.789 | -80% | 1,136 | 936 | -18% | 250,632 | 1,267,930 |
| Invocation path (1 thread) small WSDL :: getService + getPort [1T] | 11.561 | 2.386 | -79% | 6,976 | 1,176 | -83% | 86,496 | 419,027 |
| Invocation path (1 thread) small WSDL :: createOperation (ServiceInvoker.createOperation) [1T] | 19.372 | 9.359 | -52% | 9,488 | 3,656 | -61% | 51,621 | 106,846 |
| Invocation path (1 thread) small WSDL :: full sync invoke (ServiceInvoker.invoke) [1T] | 21.660 | 11.099 | -49% | 10,672 | 4,840 | -55% | 46,167 | 90,096 |
| Invocation path (1 thread) small WSDL :: full sync invoke, factory service caching ON [1T] | 12.414 | 11.094 | -11% | 5,688 | 5,080 | -11% | 80,553 | 90,143 |
| Invocation path (1 thread) large WSDL :: getService [1T] | 10.884 | 0.833 | -92% | 1,136 | 936 | -18% | 91,877 | 1,200,634 |
| Invocation path (1 thread) large WSDL :: getService + getPort [1T] | 24.764 | 1.570 | -94% | 6,976 | 1,200 | -83% | 40,382 | 636,786 |
| Invocation path (1 thread) large WSDL :: createOperation (ServiceInvoker.createOperation) [1T] | 31.513 | 9.215 | -71% | 9,488 | 3,680 | -61% | 31,733 | 108,524 |
| Invocation path (1 thread) large WSDL :: full sync invoke (ServiceInvoker.invoke) [1T] | 39.179 | 12.546 | -68% | 10,672 | 4,864 | -54% | 25,524 | 79,708 |
| Invocation path (1 thread) large WSDL :: full sync invoke, factory service caching ON [1T] | 20.974 | 12.570 | -40% | 5,688 | 5,112 | -10% | 47,677 | 79,553 |
| Scalability full invoke :: full sync invoke [1T] | 36.757 | 13.215 | -64% | 10,672 | 4,896 | -54% | 27,206 | 75,674 |
| Scalability full invoke :: full sync invoke [2T] | 37.417 | 13.700 | -63% | 10,672 | 4,896 | -54% | 53,452 | 145,981 |
| Scalability full invoke :: full sync invoke [4T] | 44.035 | 17.882 | -59% | 10,672 | 4,896 | -54% | 90,836 | 223,690 |
| Scalability full invoke :: full sync invoke [8T] | 75.871 | 35.722 | -53% | 10,672 | 4,896 | -54% | 105,442 | 223,953 |
| Scalability full invoke :: full sync invoke [16T] | 140.442 | 80.623 | -43% | 10,672 | 4,896 | -54% | 113,926 | 198,455 |
| Scalability newMapper :: newMapper [1T] | 1.356 | 1.328 | -2% | 288 | 288 | 0% | 737,409 | 753,051 |
| Scalability newMapper :: newMapper [2T] | 2.961 | 2.993 | +1% | 288 | 288 | 0% | 675,516 | 668,336 |
| Scalability newMapper :: newMapper [4T] | 5.934 | 5.721 | -4% | 288 | 288 | 0% | 674,135 | 699,227 |
| Scalability newMapper :: newMapper [8T] | 14.093 | 11.677 | -17% | 288 | 288 | 0% | 567,658 | 685,108 |
| Scalability newMapper :: newMapper [16T] | 27.349 | 24.873 | -9% | 288 | 288 | 0% | 585,021 | 643,277 |

