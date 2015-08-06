package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.services.Tutorial;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	int eventId;

	@Override
	protected void readImpl()
	{
		eventId = readD();
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
			qs.getQuest().notifyEvent("CE" + eventId, null, player);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 88 RequestTutorialClientEvent";
	}
}