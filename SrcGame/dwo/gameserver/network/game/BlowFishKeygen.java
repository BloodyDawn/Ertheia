package dwo.gameserver.network.game;

import dwo.gameserver.util.Rnd;

/**
 * Blowfish keygen for GameServerStartup client connections
 *
 * @author KenM
 */
public class BlowFishKeygen
{
	private static final int CRYPT_KEYS_SIZE = 20;
	private static final byte[][] CRYPT_KEYS = new byte[CRYPT_KEYS_SIZE][16];

	static
	{
		// init the GS encryption keys on class load

		for(int i = 0; i < CRYPT_KEYS_SIZE; i++)
		{
			// randomize the 8 first bytes
			for(int j = 0; j < CRYPT_KEYS[i].length; j++)
			{
				CRYPT_KEYS[i][j] = (byte) Rnd.get(255);
			}

			// the last 8 bytes are static
			fixKey(CRYPT_KEYS[i]);
		}
	}

	// block instantiation

	private BlowFishKeygen()
	{

	}

	/**
	 * Returns a key from this keygen pool, the logical ownership is retained by this keygen.<BR>
	 * Thus when getting a key with interests other then read-only a copy must be performed.<BR>
	 *
	 * @return A key from this keygen pool.
	 */
	public static byte[] getRandomKey()
	{
		return CRYPT_KEYS[Rnd.get(CRYPT_KEYS_SIZE)];
	}

	public static void fixKey(byte[] key)
	{
		key[8] = -56;
		key[9] = 39;
		key[10] = -109;
		key[11] = 1;
		key[12] = -95;
		key[13] = 108;
		key[14] = 49;
		key[15] = -105;
	}
}
