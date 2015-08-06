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
package dwo.loginserver;

import dwo.config.Config;
import dwo.loginserver.network.loginclientcon.serverpackets.LoginOk;
import dwo.loginserver.network.loginclientcon.serverpackets.PlayOk;

/**
 * <p>This class is used to represent session keys used by the client to authenticate in the gameserver</p>
 * <p>A SessionKey is made up of two 8 bytes keys. One is send in the {@link LoginOk}
 * packet and the other is sent in {@link PlayOk}</p>
 * @author -Wooden-
 *
 */
public class SessionKey
{
	public final int playOkID1;
	public final int playOkID2;
	public final int loginOkID1;
	public final int loginOkID2;

	public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
	{
		playOkID1 = playOK1;
		playOkID2 = playOK2;
		loginOkID1 = loginOK1;
		loginOkID2 = loginOK2;
	}

	public boolean checkLoginPair(int loginOk1, int loginOk2)
	{
		return loginOkID1 == loginOk1 && loginOkID2 == loginOk2;
	}

	/**
	 * Only checks the PlayOk part of the session key if server doesn't show the license when player logs in.
	 * @param o
	 * @return true if keys are equal.
	 */
	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(!(o instanceof SessionKey))
		{
			return false;
		}
		SessionKey key = (SessionKey) o;
		// when server doesnt show licence it deosnt send the LoginOk packet, client doesnt have this part of the key then.
		return Config.SHOW_LICENCE ? playOkID1 == key.playOkID1 && loginOkID1 == key.loginOkID1 && playOkID2 == key.playOkID2 && loginOkID2 == key.loginOkID2 : playOkID1 == key.playOkID1 && playOkID2 == key.playOkID2;
	}

	@Override
	public String toString()
	{
		return "PlayOk: " + playOkID1 + ' ' + playOkID2 + " LoginOk:" + loginOkID1 + ' ' + loginOkID2;
	}
}