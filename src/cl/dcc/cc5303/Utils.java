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
}
