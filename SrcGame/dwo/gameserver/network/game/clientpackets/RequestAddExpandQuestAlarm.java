package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.11.11
 * Time: 9:34
 */

public class RequestAddExpandQuestAlarm extends L2GameClientPacket
{
	int questId;

	@Override
	protected void readImpl()
	{
		questId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			Quest quest = QuestManager.getInstance().getQuest(questId);
			if(quest != null)
			{
				quest.sendNpcLogList(activeChar.getActingPlayer());
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:7D RequestAddExpandQuestAlarm";
	}
}