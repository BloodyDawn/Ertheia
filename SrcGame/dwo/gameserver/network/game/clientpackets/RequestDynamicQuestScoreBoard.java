package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.DynamicQuestManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.11.11
 * Time: 13:29
 */

// UC Data: native final function RequestDynamicQuestScoreInfo (int Id, int Step);
public class RequestDynamicQuestScoreBoard extends L2GameClientPacket
{
	private int _id;
	private int _step;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_step = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		DynamicQuest quest = DynamicQuestManager.getInstance().getQuest(_id, _step);

		if(quest == null)
		{
			return;
		}

		if(quest.isParticipiant(activeChar))
		{
			quest.sendResults(activeChar);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:96:03 RequestDynamicQuestScoreBoard";
	}
}

