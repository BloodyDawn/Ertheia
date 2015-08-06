package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.DynamicQuestManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.10.11
 * Time: 8:52
 *
 * d0 96 04 RequestDynamicQuestHTML (ch) cdd
 */

// UC Data: native final function RequestDynamicContentHtml (int Id, int Step);
public class RequestDynamicQuestHTML extends L2GameClientPacket
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

		if(!validate(activeChar))
		{
			return;
		}

		DynamicQuest quest = DynamicQuestManager.getInstance().getQuest(_id, _step);

		if(quest.isParticipiant(activeChar))
		{
			quest.showDialog(activeChar, "accept");
		}
		else
		{
			quest.showDialog(activeChar, "start");
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:96:04 RequestDynamicQuestHTML";
	}

	private boolean validate(L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return false;
		}

		DynamicQuest quest = DynamicQuestManager.getInstance().getQuest(_id, _step);

		if(quest == null)
		{
			return false;
		}

		return activeChar.getLevel() >= quest.getTemplate().getMinLevel();

	}
}