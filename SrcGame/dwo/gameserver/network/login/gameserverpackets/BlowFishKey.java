/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.login.gameserverpackets;

import dwo.gameserver.util.network.BaseSendablePacket;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import java.security.interfaces.RSAPublicKey;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends BaseSendablePacket
{
	private static Logger _log = LogManager.getLogger(BlowFishKey.class);

	/**
	 * @param blowfishKey
	 * @param publicKey
	 */
	public BlowFishKey(byte[] blowfishKey, RSAPublicKey publicKey)
	{
		writeC(0x00);
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] encrypted = rsaCipher.doFinal(blowfishKey);
			writeD(encrypted.length);
			writeB(encrypted);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error While encrypting blowfish key for transmision (Crypt error): " + e.getMessage(), e);
		}
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
