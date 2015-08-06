package dwo.gameserver.util;

public class Tools
{
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