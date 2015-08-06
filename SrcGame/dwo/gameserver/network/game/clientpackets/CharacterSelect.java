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
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.L2GameClient.GameClientState;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSubjobInfo;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterSelected;
import org.apache.log4j.Level;

public class CharacterSelect extends L2GameClientPacket
{
	// cd
	private int _charSlot;

	private int _unk1;     // new in C4
	private int _unk2;    // new in C4
	private int _unk3;    // new in C4
	private int _unk4;    // new in C4

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}

	@Override
	protected void runImpl()
	{
		if(!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction(FloodAction.CHARACTER_SELECT))
		{
			return;
		}

		if(Config.SECOND_AUTH_ENABLED && !getClient().getSecondaryAuth().isAuthed())
		{
			getClient().getSecondaryAuth().openDialog();
			return;
		}

		// we should always be abble to acquire the lock
		// but if we cant lock then nothing should be done (ie repeated packet)
		if(getClient().getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null
				// but if not then this is repeated packet and nothing should be done here
				if(getClient().getActiveChar() == null)
				{
					if(Config.DEBUG)
					{
						_log.log(Level.DEBUG, "selected slot:" + _charSlot);
					}

					//load up character fromdisk
					L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);
					if(cha == null)
					{
						return; // handled in L2GameClient
					}

					if(cha.getAccessLevel().getLevel() < 0)
					{
						cha.logout();
						return;
					}

					CharNameTable.getInstance().addName(cha);

					cha.setClient(getClient());
					getClient().setActiveChar(cha);
					cha.setOnlineStatus(true, true);

					getClient().setState(GameClientState.IN_GAME);
					sendPacket(new CharacterSelected(cha, getClient().getSessionId().playOkID1));
                    sendPacket(new ExSubjobInfo(cha));
				}
			}
			finally
			{
				getClient().getActiveCharLock().unlock();
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 0D CharacterSelect";
	}
}
