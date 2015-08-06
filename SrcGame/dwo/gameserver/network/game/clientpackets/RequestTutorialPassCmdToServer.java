package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.services.Tutorial;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	String _bypass;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
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
			qs.getQuest().notifyEvent(_bypass, null, player);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 86 RequestTutorialPassCmdToServer";
	}
}
