package dwo.util.crypt;

import dwo.config.Config;
import dwo.util.Base64;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHash
{
	protected static Logger _log = LogManager.getLogger(NewCrypt.class);

	/**
	 * Сравнивает пароль и ожидаемый хеш
	 * @param password
	 * @param expected
	 * @return совпадает или нет
	 */
	public static boolean compare(String password, String expected)
	{
		try
		{
			if(Config.USE_OTHER_HASH)
			{
				// проверяем не зашифрован ли пароль одним из устаревших но поддерживаемых алгоритмов
				for(String metod : Config.OTHER_LEGACY_PASSWORD_HAS)
				{
					if (encrypt(password, metod).equals(expected))
						return true;
				}
			}
			return encrypt(password).equals(expected);
		}
		catch(Exception e)
		{
			_log.error(password + ": encryption error!", e);
			return false;
		}
	}

	/**
	 * Получает пароль и возвращает хеш
	 * @param password
	 * @return hash
	 */
	public static String encrypt(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{

		MessageDigest md = MessageDigest.getInstance("SHA");
		byte[] hash;
		hash = password.getBytes("UTF-8");
		hash = md.digest(hash);
		return Base64.encodeBytes(hash);
	}

	private static String encrypt(String password, String metod) throws NoSuchAlgorithmException
	{
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance(metod);
		checksum.setEncoding("BASE64");
		checksum.update(password.getBytes());
		return checksum.format("#CHECKSUM");
	}
}