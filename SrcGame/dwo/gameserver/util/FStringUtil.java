package dwo.gameserver.util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.01.13
 * Time: 18:58
 */

public class FStringUtil
{
	public static String makeFString(int npcStringId, String... params)
	{
		String pattern = "<fstring p1=\"s1\" p2=\"s2\" p3=\"s3\" p4=\"s4\" p5=\"s5\">" + npcStringId + "</fstring>";
		for(int index = 0; index < 5; index++)
		{
			pattern = index < params.length ? pattern.replaceAll("s" + (index + 1), params[index]) : pattern.replaceAll("s" + (index + 1), "");
		}
		return pattern;
	}

	public static String makeFString(int npcStringId)
	{
		return "<fstring>" + npcStringId + "</fstring>";
	}
}