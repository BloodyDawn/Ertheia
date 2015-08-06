package dwo.gameserver.network.game;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.05.12
 * Time: 1:19
 */

public interface GameCrypt
{
	void setKey(byte[] key);

	void decrypt(byte[] raw, int offset, int size);

	void encrypt(byte[] raw, int offset, int size);

}
