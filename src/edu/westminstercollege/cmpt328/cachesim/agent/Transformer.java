package edu.westminstercollege.cmpt328.cachesim.agent;

import edu.westminstercollege.cmpt328.cachesim.BytecodeRewriter;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Optional;

public class Transformer implements ClassFileTransformer {

    private ClassPool classPool = ClassPool.getDefault();

    private byte[] transform(String className, byte[] classfileBuffer) {
        ClassFile classFile;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(classfileBuffer);
             DataInputStream dataIn = new DataInputStream(bais)) {
            classFile = new ClassFile(dataIn);
        } catch (IOException ex) {
            // Shouldn't happen - we're not actually doing any I/O!
            ex.printStackTrace();
            return classfileBuffer;
        }

        Optional<ClassFile> maybeRewritten;
        try {
            maybeRewritten = BytecodeRewriter.rewriteIfAware(classFile);
        } catch (BadBytecode ex) {
            System.err.println("---- Unable to rewrite bytecode ----");
            ex.printStackTrace();
            return classfileBuffer;
        }

        if (maybeRewritten.isPresent()) {
            ClassFile rewritten = maybeRewritten.get();
            CtClass cc = classPool.makeClass(rewritten);
            try {
                return cc.toBytecode();
            } catch (CannotCompileException | IOException ex) {
                System.err.println("---- Unable to rewrite bytecode ----");
                ex.printStackTrace();
            }
        }

        return classfileBuffer;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return transform(className, classfileBuffer);
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return transform(className, classfileBuffer);
    }
}
