package edu.westminstercollege.cmpt328.cachesim.agent;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain()");
        inst.addTransformer(new Transformer());
    }
}
