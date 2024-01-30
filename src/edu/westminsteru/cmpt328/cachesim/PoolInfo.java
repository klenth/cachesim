package edu.westminsteru.cmpt328.cachesim;

enum PoolInfo {

    initMemorySystem("initMemorySystem", "(Ljava/lang/Class;)V"),
    loadLocal("loadLocal", "(II)V"),
    storeLocal("storeLoad", "(II)V"),
    enterMethod("enterMethod", "(I)V"),
    leaveMethod("leaveMethod", "()V"),
    viewStatistics("viewStatistics", "()V"),
    allocateArray("allocateArray", "(Ljava/lang/Object;)V"),
    loadFromArray("loadFromArray", "(Ljava/lang/Object;I)V"),
    storeToArrayB("storeToArray", "([BIB)V"),
    storeToArrayS("storeToArray", "([SIS)V"),
    storeToArrayI("storeToArray", "([III)V"),
    storeToArrayJ("storeToArray", "([JIJ)V"),
    storeToArrayF("storeToArray", "([FIF)V"),
    storeToArrayD("storeToArray", "([DID)V"),
    storeToArrayC("storeToArray", "([CIC)V"),
    storeToArrayZ("storeToArray", "([ZIZ)V"),
    storeToArrayL("storeToArray", "([Ljava/lang/Object;ILjava/lang/Object;)V");

    final String name;
    final String descriptor;

    private PoolInfo(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }
}
