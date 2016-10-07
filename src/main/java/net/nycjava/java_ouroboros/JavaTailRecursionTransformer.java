package net.nycjava.java_ouroboros;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class JavaTailRecursionTransformer implements ClassFileTransformer {
	private final List<Integer> RETURN_OP_CODES = Arrays.asList(RETURN, IRETURN, LRETURN, FRETURN, DRETURN, ARETURN);

	private final static Logger LOGGER = Logger.getLogger(JavaTailRecursionTransformer.class.getName());

	public JavaTailRecursionTransformer() {
	}

	@Override
	public byte[] transform(final ClassLoader aLoader, final String aClassName, final Class<?> aClassBeingRedefined, final ProtectionDomain aProtectionDomain,
			final byte[] aClassfileBuffer) throws IllegalClassFormatException {

		try {
			final ClassReader classReader = new ClassReader(aClassfileBuffer);
			final ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);

			// temporary code to print out the byte code of any class marked with the @PrintMe annotation
			if (classNode.invisibleAnnotations != null) {
				for (final AnnotationNode annotationNode : (List<AnnotationNode>) classNode.invisibleAnnotations) {
					if (annotationNode.desc.equals("Lnet/nycjava/java_ouroboros/PrintMe;")) {
						classReader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
					}
				}
			}

			boolean rewritten = false;
			if (classNode.methods != null) {
				for (final MethodNode methodNode : (List<MethodNode>) classNode.methods) {
					if (methodNode.invisibleAnnotations != null) {
						for (final AnnotationNode annotationNode : (List<AnnotationNode>) methodNode.invisibleAnnotations) {
							if (annotationNode.desc.equals("Lnet/nycjava/java_ouroboros/TailRecursion;")) {
								rewriteMethod(methodNode, classNode);
								rewritten = true;
							}
						}
					}
				}
			}

			if (rewritten) {
				// temporary code to print out the byte code of any rewritten class marked with the @PrintMe annotation
				if (classNode.invisibleAnnotations != null) {
					for (final AnnotationNode annotationNode : (List<AnnotationNode>) classNode.invisibleAnnotations) {
						if (annotationNode.desc.equals("Lnet/nycjava/java_ouroboros/PrintMe;")) {

							classNode.accept(new TraceClassVisitor(new PrintWriter(System.out)));
						}
					}
				}

				final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				classNode.accept(classWriter);

				System.out.println("RETURNING REWRITTEN CLASS");
				 return classWriter.toByteArray();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void rewriteMethod(MethodNode aMethodNode, ClassNode aClassNode) {
		// TODO verify that the method is private... that's the only way to know it isn't being called from elsewhere,
		// which means it could be called indirectly, though that may not be a problem

		// TODO verify that any time the method calls itself, it is always as the final operation before a return, which
		// may return the value or be void

		// TODO check for finally and try-catch blocks

		final Printer printer = new Textifier();
		printer.print(new PrintWriter(System.out));
		LOGGER.info("rewriting method " + aMethodNode.name);
		System.out.println(String.format("method is %s.%s%s", aClassNode.name, aMethodNode.name, aMethodNode.desc));

		// using an index so that working backwards from the "return" is simple
		for (int i = 0; i < aMethodNode.instructions.size(); i++) {
			final AbstractInsnNode insn = aMethodNode.instructions.get(i);
			System.out.println(insn.toString());
			if (RETURN_OP_CODES.contains(insn.getOpcode())) {
				System.out.println("is a return");
				if (insn.getOpcode() != Opcodes.RETURN) {
					final AbstractInsnNode returnValueInstruction = aMethodNode.instructions.get(i - 1);
					System.out.println("return value is result of " + returnValueInstruction);

					if (returnValueInstruction.getOpcode() == Opcodes.INVOKESPECIAL) {
						final MethodInsnNode methodInsnNode = (MethodInsnNode) returnValueInstruction;
						System.out.println(String.format("is an invoke special %s.%s%s", methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));

						if (aClassNode.name.equals(methodInsnNode.owner) && aMethodNode.name.equals(methodInsnNode.name)
								&& aMethodNode.desc.equals(methodInsnNode.desc)) {
							System.out.println("RECURSION!");

							// TODO manipulate byte code here
							// TODO currently hard-coded for the signature we are testing; should be able to work it out
							// from the method signature "desc", using the signature visitor class provided
							aMethodNode.instructions.insertBefore(methodInsnNode, new VarInsnNode(Opcodes.ASTORE, 2));
							aMethodNode.instructions.insertBefore(methodInsnNode, new VarInsnNode(Opcodes.ISTORE, 1));
							// TODO hardcoded - remove the "this" of the recursive invoke ... btw, should make sure this is "this" not "that"!!! 
							aMethodNode.instructions.insertBefore(methodInsnNode, new InsnNode(Opcodes.POP)); 
							aMethodNode.instructions.insertBefore(methodInsnNode, new JumpInsnNode(Opcodes.GOTO, (LabelNode) aMethodNode.instructions
									.getFirst()));
							aMethodNode.instructions.remove(methodInsnNode);
							aMethodNode.instructions.remove(insn);
						}
					}
				}
			}
		}
	}
}
