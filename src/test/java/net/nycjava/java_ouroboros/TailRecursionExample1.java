package net.nycjava.java_ouroboros;

import java.util.List;

@PrintMe
public class TailRecursionExample1 {
	public int recur(final List<Integer> aListOfIntegers) {
		return recur0(0, aListOfIntegers);
	}

	public int iterate(final List<Integer> aListOfIntegers) {
		return iterate0(0, aListOfIntegers);
	}

	@TailRecursion
	private int recur0(final int anAccumulator, final List<Integer> aListOfIntegers) {
		if (aListOfIntegers.isEmpty()) {
			return anAccumulator;
		} else {
			return recur0(anAccumulator + aListOfIntegers.get(0), aListOfIntegers.subList(1, aListOfIntegers.size()));
		}
	}

	private int iterate0(int anAccumulator, List<Integer> aListOfIntegers) {
		iterate: while (true) {
			if (aListOfIntegers.isEmpty()) {
				return anAccumulator;
			} else {
				anAccumulator = anAccumulator + aListOfIntegers.get(0);
				aListOfIntegers = aListOfIntegers.subList(1, aListOfIntegers.size());
				continue iterate;
			}
		}
	}
}
