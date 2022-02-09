To-do list
==========

Things to consider doing in the future:
* Provide access to memory addresses of variables, array elements
    * Have dummy static method to call (e.g. `MemorySystem.addressOf(x)`) — bytecode rewriter intercepts this call and rewrites
* Possible to determine line number (and even line of code?) for each memory access? If so, provide GUI showing accesses, how many times they run, hit ratios?
* Don't track accesses in static initializers (as memory system won't be initialized yet, and field accesses aren't tracked anyway)
    * Stuff like "private static Random random = new Random(0);" are implemented using static initializers!
* @Register annotation for parameters, local variables?
    * Annotations for local variables are not accessible by reflection: will need to trawl bytecode to pick it up
* @Inline annotation for methods?
    * Making it so argument loads are not tracked? Seems complicated
* Interface for compiling/running program within Cachesim GUI?
    * So we don't need to add the -javaagent argument on every program...
* Make MemorySystem.viewStatistics() modal
* Fix memory configuration GUI: does not commit change to replacement policy (LRU/FIFO/whatever) until another cell is clicked on
    * Really, make this GUI less janky in general (maybe only edit table cell on double click)
