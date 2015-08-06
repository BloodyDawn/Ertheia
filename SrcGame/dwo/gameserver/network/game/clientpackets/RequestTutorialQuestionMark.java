package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.services.Tutorial;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	int _number;

	@Override
	protected void readImpl()
	{
		_number = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
		{
			return;
		}

		QuestState qs = player.getQuestState(Tutorial.class);
		if(qs != null)
		{
			qs.getQuest().notifyEvent("QM" + _number, null, player);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 87 RequestTutorialQuestionMark";
	}
}