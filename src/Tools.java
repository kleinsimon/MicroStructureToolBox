public class Tools {
	public static int getRoundedInt(Double from) {
		//Long rnd = Math.round(from);
		//return Math.toIntExact(rnd);
		return (int) (from + 0.5);
	}

	public static String BooleanToString(boolean[] bools) {
		String ret = "";
		for (boolean b : bools)
			ret += (b == true) ? "1" : "0";
		return ret;
	}

	public static boolean[] StringToBoolean(String str, int outLength) {
		boolean[] ret = new boolean[outLength];
		if (str == null)
			return ret;
		if (str.length() == outLength) {
			for (int i = 0; i < str.length(); i++) {
				ret[i] = (str.charAt(i) == '1') ? true : false;
			}
		}
		return ret;
	}
}
