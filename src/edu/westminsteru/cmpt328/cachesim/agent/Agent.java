package edu.westminsteru.cmpt328.cachesim.agent;

import edu.westminsteru.cmpt328.cachesim.Version;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.printf("-=-=-=- Cachesim version %s -=-=-=-\n", Version.latest().tag());
        System.out.println("-=-=-=- [cachesim agent] Transforming bytecode -=-=-=-");
        inst.addTransformer(new Transformer());
    }
}
