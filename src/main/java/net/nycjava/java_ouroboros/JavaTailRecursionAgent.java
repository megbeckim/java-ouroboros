package net.nycjava.java_ouroboros;

import java.lang.instrument.Instrumentation;

public class JavaTailRecursionAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new JavaTailRecursionTransformer());
	}

	public static void agentmain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new JavaTailRecursionTransformer());
	}
}
