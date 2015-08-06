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
package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;

public class QuestList extends L2GameServerPacket
{
	private Quest[] _quests;
	private L2PcInstance _activeChar;

	@Override
	public void runImpl()
	{
		if(getClient() != null && getClient().getActiveChar() != null)
		{
			_activeChar = getClient().getActiveChar();
			_quests = _activeChar.getAllActiveQuests();
		}
	}

	@Override
	protected void writeImpl()
	{
		if(_quests != null)
		{
			writeH(_quests.length);
			for(Quest q : _quests)
			{
				writeD(q.getQuestId());
				QuestState qs = _activeChar.getQuestState(q.getName());
				if(qs == null)
				{
					writeD(0);
					continue;
				}

				int states = qs.getInt("__compltdStateFlags");
				if(states == 0)
				{
					writeD(qs.getInt("cond"));
				}
				else
				{
					writeD(states);
				}
			}
		}
		else
		{
			writeH(0x00);
		}
		//TODO дорозобрать пакет
		writeB(new byte[128]);
	}
}
