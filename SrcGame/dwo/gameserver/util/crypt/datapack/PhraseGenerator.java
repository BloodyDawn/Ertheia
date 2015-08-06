package dwo.gameserver.util.crypt.datapack;

import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class PhraseGenerator
{
	public static void main(String[] s)
	{
		int keyLength = 64;
		try
		{
			if(s.length > 0)
			{
				keyLength = Integer.parseInt(s[0]);
			}
		}
		catch(NumberFormatException e)
		{
			System.out.println("Wrong number format for key length. Skip.");
			keyLength = 64;
		}
		System.out.println("Generating random key with length " + keyLength + "...");
		for(int i = 0; i < keyLength; ++i)
		{
			System.out.print(Character.toChars(Rnd.get(45, 85)));
		}
	}
}
