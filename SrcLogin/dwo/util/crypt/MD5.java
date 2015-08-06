package dwo.util.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5
{
	public static String getHash(String str)
	{
		MessageDigest md5;
		StringBuilder result = new StringBuilder();
		try
		{

			md5 = MessageDigest.getInstance("md5");
			md5.reset();
			byte[] digest = md5.digest(str.getBytes());
			for(byte aDigest : digest)
			{
				result.append(Integer.toHexString(0x0100 + (aDigest & 0x00FF)).substring(1));
			}

		}
		catch(NoSuchAlgorithmException e)
		{
			return e.toString();
		}

		return result.toString();
	}
}