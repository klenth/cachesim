package edu.westminstercollege.cmpt328.cachesim;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class Test {

    public static void main(String... args) throws IOException, BadBytecode {
        Path testFile = Path.of("testinput").resolve("Hw04.class");
        ClassFile classFile;
        try (DataInputStream in = new DataInputStream(Files.newInputStream(testFile))) {
            classFile = new ClassFile(in);
        }

        BytecodeRewriter rewriter = new BytecodeRewriter(classFile);
        classFile = rewriter.rewrite();

        Path outFile = Path.of("out").resolve(testFile.getFileName().toString());
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(outFile))) {
            classFile.write(out);
        }
    }
}
