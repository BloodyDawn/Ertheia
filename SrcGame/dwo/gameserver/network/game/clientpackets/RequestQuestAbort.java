package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.QuestList;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _questId;

	@Override
	protected void readImpl()
	{
		_questId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		Quest qe = QuestManager.getInstance().getQuest(_questId);
		if(qe != null)
		{
			QuestState qs = activeChar.getQuestState(qe.getName());
			if(qs != null)
			{
				qs.exitQuest(QuestType.REPEATABLE);
				activeChar.sendPacket(new QuestList());
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 64 RequestQuestAbort";
	}
}