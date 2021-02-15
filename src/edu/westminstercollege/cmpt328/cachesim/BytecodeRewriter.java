package edu.westminstercollege.cmpt328.cachesim;

import java.util.*;

import edu.westminstercollege.cmpt328.cachesim.annotations.Memory;
import edu.westminstercollege.cmpt328.cachesim.annotations.MemoryAware;
import javassist.bytecode.*;

import static javassist.bytecode.Opcode.*;
import static edu.westminstercollege.cmpt328.cachesim.PoolInfo.*;

public class BytecodeRewriter {

    private int runtimePoolIndex;
    private EnumMap<PoolInfo, Integer> methodrefIndices = new EnumMap<>(PoolInfo.class);
    private boolean hasAnnotation = false;
    private boolean hasMemoryAnnotation = false;

    private ClassFile classFile;

    public BytecodeRewriter(ClassFile classFile) {
        this.classFile = classFile;

        AnnotationsAttribute annotationInfo = (AnnotationsAttribute)classFile.getAttribute(AnnotationsAttribute.visibleTag);
        if (annotationInfo != null) {
            if (annotationInfo.getAnnotation(MemoryAware.class.getName()) != null)
                hasAnnotation = true;
            if (annotationInfo.getAnnotation(Memory.class.getName()) != null)
                hasAnnotation = hasMemoryAnnotation = true;
        }
    }

    public static Optional<ClassFile> rewriteIfAware(ClassFile classFile) throws BadBytecode {
        BytecodeRewriter rewriter = new BytecodeRewriter(classFile);
        if (rewriter.hasAnnotation)
            return Optional.of(rewriter.rewrite());
        else
            return Optional.empty();
    }

    public ClassFile rewrite() throws BadBytecode {
        Class<?> runtimeClass = edu.westminstercollege.cmpt328.cachesim.Runtime.class;
        runtimePoolIndex = classFile.getConstPool().addClassInfo(runtimeClass.getName());

        for (MethodInfo method : classFile.getMethods())
            rewrite(method);

        return classFile;
    }

    void rewrite(MethodInfo method) throws BadBytecode {
        CodeAttribute code = method.getCodeAttribute();
        CodeIterator it = code.iterator();

        insertEnterMethod(it, 0, code.getMaxLocals());

        boolean wide = false;
        while (it.hasNext()) {
            int index = it.next();
            int opcode = it.byteAt(index);

            switch (opcode) {
                // ---- Leaving the method ----
                case ARETURN:
                case DRETURN:
                case FRETURN:
                case IRETURN:
                case LRETURN:
                case RETURN:
                    insertLeaveMethod(it, index);
                    break;

                // ---- Array loads ----
                case AALOAD:
                case BALOAD:
                case CALOAD:
                case DALOAD:
                case FALOAD:
                case IALOAD:
                case LALOAD:
                case SALOAD:
                    insertLoadFromArray(it, index);
                    break;

                // ---- Array stores ----
                case AASTORE:
                case BASTORE:
                case CASTORE:
                case DASTORE:
                case FASTORE:
                case IASTORE:
                case LASTORE:
                case SASTORE:
                    replaceStoreToArray(it, index, opcode);
                    break;

                // ---- Local variable loads (single stack position) ----
                case ALOAD:
                case FLOAD:
                case ILOAD:
                    insertLoadLocal(it, index, it.byteAt(index + 1), 1);
                    break;

                case ALOAD_0:
                case FLOAD_0:
                case ILOAD_0:
                    insertLoadLocal(it, index, 0, 1);
                    break;

                case ALOAD_1:
                case FLOAD_1:
                case ILOAD_1:
                    insertLoadLocal(it, index, 1, 1);
                    break;

                case ALOAD_2:
                case FLOAD_2:
                case ILOAD_2:
                    insertLoadLocal(it, index, 2, 1);
                    break;

                case ALOAD_3:
                case FLOAD_3:
                case ILOAD_3:
                    insertLoadLocal(it, index, 3, 1);
                    break;

                // ---- Local variable loads (two stack positions) ----
                case DLOAD:
                case LLOAD:
                    insertLoadLocal(it, index, it.byteAt(index + 1), 2);
                    break;

                case DLOAD_0:
                case LLOAD_0:
                    insertLoadLocal(it, index, 0, 2);
                    break;

                case DLOAD_1:
                case LLOAD_1:
                    insertLoadLocal(it, index, 1, 2);
                    break;

                case DLOAD_2:
                case LLOAD_2:
                    insertLoadLocal(it, index, 2, 2);
                    break;

                case DLOAD_3:
                case LLOAD_3:
                    insertLoadLocal(it, index, 3, 2);
                    break;

                // ---- Local variable stores (single stack position) ----
                case ASTORE:
                case FSTORE:
                case ISTORE:
                    insertStoreLocal(it, index, it.byteAt(index + 1), 1);
                    break;

                case ASTORE_0:
                case FSTORE_0:
                case ISTORE_0:
                    insertStoreLocal(it, index, 0, 1);
                    break;

                case ASTORE_1:
                case FSTORE_1:
                case ISTORE_1:
                    insertStoreLocal(it, index, 1, 1);
                    break;

                case ASTORE_2:
                case FSTORE_2:
                case ISTORE_2:
                    insertStoreLocal(it, index, 2, 1);
                    break;

                case ASTORE_3:
                case FSTORE_3:
                case ISTORE_3:
                    insertStoreLocal(it, index, 3, 1);
                    break;

                // ---- Local variable stores (two stack positions) ----
                case DSTORE:
                case LSTORE:
                    insertStoreLocal(it, index, it.byteAt(index + 1), 2);
                    break;

                case DSTORE_0:
                case LSTORE_0:
                    insertStoreLocal(it, index, 0, 2);
                    break;

                case DSTORE_1:
                case LSTORE_1:
                    insertStoreLocal(it, index, 1, 2);
                    break;

                case DSTORE_2:
                case LSTORE_2:
                    insertStoreLocal(it, index, 2, 2);
                    break;

                case DSTORE_3:
                case LSTORE_3:
                    insertStoreLocal(it, index, 3, 2);
                    break;

                // ---- Array creation ----
                case ANEWARRAY:
                case MULTIANEWARRAY:
                case NEWARRAY:
                    insertNewArray(it, index, opcode);
                    break;

                // ---- Miscellaneous ----
                case IINC:
                    int localIndex = it.byteAt(index + 1);
                    insertLoadLocal(it, index, localIndex, 1);
                    insertStoreLocal(it, index, localIndex, 1);
                    break;

                case NEW:
                    System.out.println("new");
                    // Do we need to do something here?
                    break;

                case WIDE:
                    System.out.println("wide");
                    break;
            }

            wide = opcode == WIDE;
        }

        if (hasMemoryAnnotation
                && method.getName().equals("main")
                && method.getDescriptor().equals("([Ljava/lang/String;)V")
                && (method.getAccessFlags() & AccessFlag.STATIC) != 0)
            insertMemorySystemHook(method);

        try {
            code.computeMaxStack();
        } catch (BadBytecode bbc) {
            System.err.printf("Error rewriting bytecode for %s %s:\n", method.getName(), method.getDescriptor());
            bbc.printStackTrace();
            System.err.println();
        }
    }

    private void insertMemorySystemHook(MethodInfo method) throws BadBytecode {
        int classInfoIndex = classFile.getConstPool().addClassInfo(classFile.getName());
        CodeAttribute code = method.getCodeAttribute();
        CodeIterator it = code.iterator();
        it.insert(0, bytes()
                .u8(LDC_W)
                .u16(classInfoIndex)
                .u8(INVOKESTATIC)
                .u16(getMethodrefIndex(initMemorySystem))
        .build());
    }

    private int getMethodrefIndex(PoolInfo pi) {
        return methodrefIndices.computeIfAbsent(pi, p ->
                classFile.getConstPool().addMethodrefInfo(runtimePoolIndex, p.name, p.descriptor));
    }

    private void insertEnterMethod(CodeIterator it, int index, int numLocals) throws BadBytecode {
        it.insert(index, bytes()
            .u8(BIPUSH)
            .u8(numLocals)
            .u8(INVOKESTATIC)
            .u16(getMethodrefIndex(enterMethod))
        .build());
    }

    private void insertLeaveMethod(CodeIterator it, int index) throws BadBytecode {
        it.insert(index, bytes()
            .u8(INVOKESTATIC)
            .u16(getMethodrefIndex(leaveMethod))
        .build());
    }

    private void insertLoadFromArray(CodeIterator it, int index) throws BadBytecode {
        byte[] bytecode = bytes()
            .u8(DUP2)
            .u8(INVOKESTATIC)
            .u16(getMethodrefIndex(PoolInfo.loadFromArray))
            .build();
        it.insert(index, bytecode);
    }

    private void replaceStoreToArray(CodeIterator it, int index, int opcode) throws BadBytecode {
        PoolInfo methodrefInfo = null;
        switch (opcode) {
            case AASTORE:
                methodrefInfo = storeToArrayL;
                break;
            case BASTORE:
                throw new BadBytecode("Can't handle bastore");
            case CASTORE:
                methodrefInfo = storeToArrayC;
                break;
            case DASTORE:
                methodrefInfo = storeToArrayD;
                break;
            case FASTORE:
                methodrefInfo = storeToArrayF;
                break;
            case IASTORE:
                methodrefInfo = storeToArrayI;
                break;
            case LASTORE:
                methodrefInfo = storeToArrayJ;
                break;
            case SASTORE:
                methodrefInfo = storeToArrayS;
                break;
            default:
                assert (false);
        }

        byte[] bytecode = bytes()
                .u8(INVOKESTATIC)
                .u16(getMethodrefIndex(methodrefInfo))
                .build();
        it.insert(index, bytecode);
        it.writeByte(NOP, index + bytecode.length);
    }

    private void insertLoadLocal(CodeIterator it, int index, int localIndex, int localSize) throws BadBytecode {
        it.insert(index, bytes()
            .u8(BIPUSH)
            .u8(localIndex)
            .u8(BIPUSH)
            .u8(localSize)
            .u8(INVOKESTATIC)
            .u16(getMethodrefIndex(loadLocal))
        .build());
    }

    private void insertStoreLocal(CodeIterator it, int index, int localIndex, int localSize) throws BadBytecode {
        it.insert(index, bytes()
            .u8(BIPUSH)
            .u8(localIndex)
            .u8(BIPUSH)
            .u8(localSize)
            .u8(INVOKESTATIC)
            .u16(getMethodrefIndex(loadLocal))
        .build());
    }

    private void insertNewArray(CodeIterator it, int index, int opcode) throws BadBytecode {
        // multianew instructions are 4 bytes, anewarray instructions are 3, newarray are 2
        int skipBytes =
            (opcode == MULTIANEWARRAY) ? 4
            : (opcode == ANEWARRAY) ? 3
            : 2;

        it.insert(index + skipBytes, bytes()
            .u8(DUP)
            .u8(INVOKESTATIC)
            .u16(getMethodrefIndex(allocateArray))
        .build());
    }

    private Bytes bytes() {
        return new Bytes();
    }
}
