package edu.westminstercollege.cmpt328.cachesim.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RAM {

    int accessTime();

    long size() default -1L;

    String name() default "RAM";
}
