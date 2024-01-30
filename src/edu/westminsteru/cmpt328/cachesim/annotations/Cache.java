package edu.westminsteru.cmpt328.cachesim.annotations;

import edu.westminsteru.cmpt328.memory.ReplacementAlgorithm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    public static final int Unspecified = -1;

    String name();

    int accessTime();
    int lines();
    MappingAlgorithm mapping() default MappingAlgorithm.Direct;
    ReplacementAlgorithm replacement() default ReplacementAlgorithm.LRU;
    int ways() default Unspecified;
}
