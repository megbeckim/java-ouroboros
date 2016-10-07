package net.nycjava.java_ouroboros;

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RecursionTest {
	private RecursionExample1 recursionExample1;

	@Before
	public void setUp() {
		recursionExample1 = new RecursionExample1();
	}

	@Test
	public void shallowRecursiveCallShouldReturn() {
		assertEquals(15, recursionExample1.recur(5));
	}

	@Test
	public void shallowIterativeCallShouldReturn() {
		assertEquals(15, recursionExample1.iterate(5));
	}

	@Test
	public void deepRecursiveCallShouldReturn() {
		assertEquals(1250025000, recursionExample1.recur(50000));
	}

	@Test
	public void deepIterativeCallShouldReturn() {
		assertEquals(1250025000, recursionExample1.iterate(50000));
	}
}
