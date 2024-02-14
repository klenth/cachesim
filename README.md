# Cachesim: a cache simulation library and agent for Java

Cachesim is a simple multilevel cache simulator for use with Java programs. It provides a set of classes representing
memories (RAM and cache) and values resident in them, as well as some utilities for viewing memory statistics (number of
accesses, hit ratio, total access time).

Also included is a Java agent that can dynamically transform Java class code so that variable and array allocations and
accesses are tracked by the simulated cache system, making it easy to benchmark different cache configurations on Java
code.

Cachesim is used in my Computer Architecture class and is unlikely to be of much interest outside the classroom.
