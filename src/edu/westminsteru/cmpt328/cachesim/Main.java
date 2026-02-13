package edu.westminsteru.cmpt328.cachesim;

public class Main {

    public static void main(String... args) {
        System.out.printf("This is Cachesim version %s.\n", Version.latest().tag());
        System.out.println();
        System.out.println("To use, run as a Java agent, for example:");

        final String jarFilename = "cachesim-%s.jar".formatted(Version.latest().tag());
        System.out.printf("\tjava -cp .:%s -javaagent:%s ClassName\n",
            jarFilename, jarFilename);
    }
}
