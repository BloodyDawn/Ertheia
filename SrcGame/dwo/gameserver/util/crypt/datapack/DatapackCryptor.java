package dwo.gameserver.util.crypt.datapack;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class DatapackCryptor
{
	private static final String[] EXCLUDES = {"cache", "geodata", "xsd", "custom", "accesscontrol"};
	private static final String[] EXCLUDES_FILE = {
		"EnchantItemData.xml", "CharStartingItems.xml", "CommunityBuffs.xml", "CommunityTeleport.xml"
	};
	private String _dpPath;
	private String _cryptPath;
	private long _crypted;
	private boolean _excludeMode;

	DatapackCryptor(String dpPath, String cryptPath)
	{
		if(!(!dpPath.isEmpty() && dpPath.charAt(dpPath.length() - 1) == '/') && !(!dpPath.isEmpty() && dpPath.charAt(dpPath.length() - 1) == '\\'))
		{
			dpPath += '/';
		}
		if(!(!cryptPath.isEmpty() && cryptPath.charAt(cryptPath.length() - 1) == '/') && !(!cryptPath.isEmpty() && cryptPath.charAt(cryptPath.length() - 1) == '\\'))
		{
			cryptPath += '/';
		}
		_dpPath = dpPath;
		_cryptPath = cryptPath;
	}

	public static void main(String[] s) throws IOException
	{
		if(s.length < 2)
		{
			System.out.println("Usage: Datapack {DP_PATH} {CRYPT_PATH}\nWhere {DP_PATH} is where datapack files are, and {CRYPT_PATH} is where crypted files will be placed.");
			return;
		}
		DatapackCryptor dp = new DatapackCryptor(s[0], s[1]);
		dp.crypt();
	}

	private void crypt() throws IOException
	{
		System.out.println("Deleeting directory...");
		FileUtils.deleteDirectory(new File(_cryptPath));
		System.out.println("Crypting...");
		crypt(_dpPath);
		System.out.println("Crypted " + _crypted + " files...");
	}

	/**
	 * Crypts all files recoursively.
	 *
	 * @param path Path to directory where crypted files are.
	 */
	private void crypt(String path)
	{
		if(!(!path.isEmpty() && path.charAt(path.length() - 1) == '/'))
		{
			path += "/";
		}

		System.out.println("============================== Crypting directory: " + path + " ==============================");

		File dir = new File(path);

		File[] files = dir.listFiles();

		if(files == null)
		{
			return;
		}

		for(File file : files)
		{
			boolean localExcludeFile = false;
			if(file.isFile())
			{

				for(String exclude : EXCLUDES_FILE)
				{
					if(exclude.equalsIgnoreCase(file.getName()))
					{
						_excludeMode = true;
						localExcludeFile = true;
						break;
					}
				}
			}

			// Recursive parsing
			if(file.isDirectory())
			{
				boolean localExclude = false;
				for(String exclude : EXCLUDES)
				{
					if(exclude.equalsIgnoreCase(file.getName()))
					{
						_excludeMode = true;
						localExclude = true;
						break;
					}
				}
				crypt(path + file.getName());
				if(localExclude)
				{
					_excludeMode = false;
				}
			}
			// Allow to crypt only HTML & XML files
			else if(!_excludeMode && file.isFile() && (file.getName().endsWith(".xml") || file.getName().endsWith(".htm") || file.getName().endsWith(".json")))
			{
				++_crypted;
				FileOutputStream output;
				try
				{
					FileInputStream input = new FileInputStream(file);
					new File(_cryptPath + path.substring(_dpPath.length())).mkdirs();
					output = new FileOutputStream(_cryptPath + path.substring(_dpPath.length()) + file.getName(), false);
					output.write((byte) 0x00);
					CryptUtil.encrypt(input, output);
					input.close();
					output.close();
				}
				catch(Exception e)
				{
					System.out.println("Error during crypting file: " + e);
					System.exit(1);
				}
				if(_crypted % 1000 == 0)
				{
					System.out.println("Crypted " + _crypted + " files...");
				}
			}
			else
			{
				try
				{
					new File(_cryptPath + path.substring(_dpPath.length())).mkdirs();
					FileInputStream input = new FileInputStream(file);
					FileOutputStream output = new FileOutputStream(_cryptPath + path.substring(_dpPath.length()) + file.getName(), false);
					byte[] buffer = new byte[1024];
					int num;
					while((num = input.read(buffer)) >= 0)
					{
						output.write(buffer, 0, num);
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
			}

			if(localExcludeFile)
			{
				_excludeMode = false;
			}
		}
	}
}
