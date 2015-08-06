package dwo.gameserver.network.game.serverpackets.packet.gmview;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Tempy
 */

public class GMViewQuestInfo extends L2GameServerPacket
{
	private L2PcInstance _activeChar;

	public GMViewQuestInfo(L2PcInstance cha)
	{
		_activeChar = cha;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_activeChar.getName());

		Quest[] questList = _activeChar.getAllActiveQuests();

		if(questList.length == 0)
		{
			writeC(0);
			writeH(0);
			writeH(0);
			return;
		}

		writeH(questList.length); // quest count
		for(Quest q : questList)
		{
			writeD(q.getQuestId());

			QuestState qs = _activeChar.getQuestState(q.getName());

			if(qs == null)
			{
				writeD(0);
				continue;
			}

			writeD(qs.getInt("cond"));   // stage of quest progress
		}
	}
}
