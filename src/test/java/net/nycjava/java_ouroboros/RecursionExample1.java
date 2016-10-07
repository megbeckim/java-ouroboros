package net.nycjava.java_ouroboros;

@PrintMe
public class RecursionExample1 {
	@TailRecursion
	public int recur(int anInt) {
		if (anInt > 0) {
			return anInt + recur(anInt - 1);
		} else {
			return 0;
		}
	}

	public int iterate(int anInt) {
		int $result = 0;
		recursionLoop: while (true) {
			if (anInt > 0) {
				$result += anInt;
				anInt = anInt - 1;
				continue recursionLoop;
			} else {
				$result += 0;
				break;
			}
		}
		return $result;
	}
}
