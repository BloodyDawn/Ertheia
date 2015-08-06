package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2FeedableBeastInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;

public class BeastSpice implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;

		if(!(activeChar.getTarget() instanceof L2FeedableBeastInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		int skillId = 0;
		switch(item.getItemId())
		{
			case 6643:
				skillId = 2188;
				break;
			case 6644:
				skillId = 2189;
				break;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
		if(skill != null)
		{
			activeChar.useMagic(skill, false, false);
		}
		return true;
	}
}