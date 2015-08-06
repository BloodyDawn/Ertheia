package dwo.gameserver.util.crypt.datapack;

import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class CryptUtil
{
	private static final Logger _log = Logger.getLogger(CryptUtil.class);

	private static final Base64 base64Processor = new Base64();
	private static final String _pass = "8U4703<1M7LCDPI6EMH2P64-2A0Q18?M6P?15D1AD>0UQSDL3O404G?.DFNH3BLCGLF2LRU:1E<L8/L9R92<632T47C3P=77C<HT.7;G-H9C:BS<PLH-@0L;5QBM9@.J";
	private static final byte[] _salt = {
		(byte) 0x8e, 0x12, 0x39, (byte) 0x9c, 0x07, 0x72, 0x6f, 0x5a
	};
	private static Cipher _encCipher;
	private static Cipher _decCipher;
	private static SecretKey _key;
	private static boolean _initiated;

	private static void init()
	{
		if(_initiated)
		{
			return;
		}

		try
		{
			KeySpec keySpec = new PBEKeySpec(_pass.toCharArray(), _salt, 19);
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(_salt, 19);
			_key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

			_encCipher = Cipher.getInstance(_key.getAlgorithm());
			_decCipher = Cipher.getInstance(_key.getAlgorithm());

			_encCipher.init(Cipher.ENCRYPT_MODE, _key, paramSpec);
			_decCipher.init(Cipher.DECRYPT_MODE, _key, paramSpec);
		}
		catch(Exception e)
		{
			_log.error("Cannot init crypto engine.", e);
		}

		_initiated = true;
	}

	public static String encrypt(String data)
	{
		init();
		try
		{
			return Base64.encodeBase64String(_encCipher.doFinal(data.getBytes("UTF8")));
		}
		catch(Exception e)
		{
			_log.error("Cannot encrypt data.", e);
		}
		return null;
	}

	public static String decrypt(String data)
	{
		init();
		try
		{
			return new String(_decCipher.doFinal(base64Processor.decode(data)), "UTF8");
		}
		catch(Exception e)
		{
			_log.error("Cannot decrypt data.", e);
		}

		return null;
	}

	public static void encrypt(InputStream in, OutputStream out)
	{
		init();
		out = new CipherOutputStream(out, _encCipher);

		try
		{
			int num;
			byte[] buffer = new byte[1024];
			while((num = in.read(buffer)) >= 0)
			{
				out.write(buffer, 0, num);
			}
			out.flush();
			out.close();
		}
		catch(IOException e)
		{
			_log.error("Cannot write encrypted file.", e);
		}
	}

	public static InputStream decrypt(InputStream input, InputStream readable)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		decrypt(input, output);
		return new ByteArrayInputStream(output.toByteArray());
	}

	/**
	 * Decrypts file.
	 * @param file Input file to decrypt.
	 * @return Input stream with decrypted data
	 * @throws IOException
	 */
	public static InputStream decryptOnDemand(File file) throws IOException
	{
		InputStream input = new FileInputStream(file);
		InputStream output;
		if((byte) input.read() == 0x00)
		{
			byte[] bytes = new byte[0];
			output = new ByteArrayInputStream(bytes);
			output = decrypt(input, output);
			output.reset();
		}
		else
		{
			output = new FileInputStream(file);
		}

		input.close();

		return output;
	}

	/**
	 * Makes decrypting of file if it is needed.
	 * @param input Input stream to decrypt.
	 * @return Input stream with decrypted data.
	 */
	public static InputStream decryptOnDemand(InputStream input) throws IOException
	{
		InputStream output;
		if((byte) input.read() == 0x00)
		{
			byte[] bytes = new byte[0];
			output = new ByteArrayInputStream(bytes);
			output = decrypt(input, output);
		}
		else
		{
			output = input;
		}

		output.reset();
		return output;
	}

	public static void decrypt(InputStream in, OutputStream out)
	{
		init();
		in = new CipherInputStream(in, _decCipher);

		try
		{
			int num;
			byte[] buffer = new byte[1024];
			while((num = in.read(buffer)) >= 0)
			{
				out.write(buffer, 0, num);
			}
			out.flush();
			out.close();
		}
		catch(IOException e)
		{
			_log.error("Cannot decrypt file.", e);
		}
	}

	public static String encrypt(InputStream stream) throws IOException
	{
		init();
		StringBuilder buffer = new StringBuilder();
		int chr;
		while((chr = stream.read()) >= 0)
		{
			buffer.append(chr);
		}

		return encrypt(buffer.toString());
	}

	public static String decrypt(InputStream stream) throws IOException
	{
		init();
		StringBuilder buffer = new StringBuilder();
		int chr;
		while((chr = stream.read()) >= 0)
		{
			buffer.append(Character.toChars(chr));
		}

		return decrypt(buffer.toString());
	}

	public static int getKeyHash()
	{
		init();
		return _key.hashCode();
	}
}
