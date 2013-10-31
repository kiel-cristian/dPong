package cl.dcc.cc5303;

public class Utils {

	/**
	 * Counts how many elements of a boolean array are true
	 * 
	 * @param array the array to count
	 * @return the number of true elements
	 */
	public static int countTrue(boolean array[]) {
		int count = 0;
		for (int i=0; i<array.length; i++) {
			if (array[i]) count++;
		}
		return count;
	}
	
	public static void setFalse(boolean array[]) {
		for (int i=0; i<array.length; i++) {
			array[i] = false;
		}
	}
	
	public static class Pair<L,R> {
		private final L left;
		private final R right;

		public Pair(L left, R right) {
			this.left = left;
			this.right = right;
		}
		
		public L left() {
			return left;
		}
		
		public R right() {
			return right;
		}
	}
}
