package com.ironsource.mobilcore;

class Guard {

	public Guard() {

	}

	protected static String encrypt(String s) {
		String x = "";

		for (int i = 0; i < s.length(); i++) {
			if ((int) s.charAt(i) < 107) {
				x = x.concat(String.valueOf(s.charAt(i)));
				x = x.concat("g#s");
			} else if ((int) s.charAt(i) >= 107) {
				x = x.concat(String.valueOf(s.charAt(i)));
				x = x.concat("d%1");
			}
		}
		return reverser(x);
	}

	protected static String decrypt(String s) {
		String ans = reverser(s);
		ans = ans.replaceAll("g#s", "").replaceAll("d%1", "");
		return ans;
	}

	private static String reverser(String s) {
		char[] chs = s.toCharArray();

		int i = 0, j = chs.length - 1;
		while (i < j) {
			char t = chs[i];
			chs[i] = chs[j];
			chs[j] = t;
			i++;
			j--;
		}
		return new String(chs);
	}
}
