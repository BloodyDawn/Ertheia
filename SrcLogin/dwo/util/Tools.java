package dwo.util;

public class Tools
{
	/**
	 * @param s
	 */
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while(s.length() < 78)
		{
			s = '-' + s;
		}
		System.out.println(s);
	}

}
