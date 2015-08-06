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
package dwo.loginserver.network.loginclientcon.serverpackets;

import dwo.loginserver.SessionKey;

/**
 *
 */
public class PlayOk extends L2LoginServerPacket
{
	private int _playOk1;
	private int _playOk2;

	public PlayOk(SessionKey sessionKey)
	{
		_playOk1 = sessionKey.playOkID1;
		_playOk2 = sessionKey.playOkID2;
	}

	@Override
	protected void write()
	{
		writeC(0x07);
		writeD(_playOk1);
		writeD(_playOk2);

		writeD(17708);
		writeH(35243);
		writeC(0);

		writeD(1698285319);
		writeD(0);

	 /*
		 [LS] size: 24 code: 0x7

		cc bc 00 00  // _playOk1
		60 48 65 00  // _playOk2

		2c 45 00 00 ab 89 00 07 2b 38 65 00 00 00 00

		20 bd 00 00 // _playOk1
		60 48 65 00 // _playOk2

		2c 45 00 00

		ab 89

		00


		07 c7 39 65
		00 00 00 00


	  */
	}
}
