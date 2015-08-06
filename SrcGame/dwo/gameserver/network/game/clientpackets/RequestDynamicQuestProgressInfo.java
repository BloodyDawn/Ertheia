package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.DynamicQuestManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExDynamicQuest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.11.11
 * Time: 13:27
 */

// UC Data: native final function RequestDynamicQuestProgressInfo (int Id, int Step);
public class RequestDynamicQuestProgressInfo extends L2GameClientPacket
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
		if(quest != null)
		{
			quest.sendProgress(activeChar, ExDynamicQuest.UpdateAction.ACTION_PROGRESS);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:96:02 RequestDynamicQuestProgressInfo";
	}
}
