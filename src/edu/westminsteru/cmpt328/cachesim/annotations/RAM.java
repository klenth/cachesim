package edu.westminsteru.cmpt328.cachesim.annotations;

import edu.westminsteru.cmpt328.memory.Bits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RAM {

    int accessTime();

    int size() default Bits.NUM_ADDRESSES;

    String name() default "RAM";
}
