package net.nycjava.java_ouroboros;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TailRecursionTest {
	private TailRecursionExample1 tailRecursionExample1;

	@Before
	public void setUp() {
		tailRecursionExample1 = new TailRecursionExample1();
	}

	@Test
	public void shallowRecursiveCallShouldReturn() {
		assertEquals(15, tailRecursionExample1.recur(createList(5)));
	}

	@Test
	public void shallowIterativeCallShouldReturn() {
		assertEquals(15, tailRecursionExample1.iterate(createList(5)));
	}

	@Test
	public void deepRecursiveCallShouldReturn() {
		assertEquals(1250025000, tailRecursionExample1.recur(createList(50000)));
	}

	@Test
	public void deepIterativeCallShouldReturn() {
		assertEquals(1250025000, tailRecursionExample1.iterate(createList(50000)));
	}

	private List<Integer> createList(final int anN) {
		List<Integer> result = new ArrayList<>();
		for (int i = 1; i <= anN; i++) {
			result.add(i);
		}
		return result;
	}
}
