package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author l3x
 */
public class Harvester implements IItemHandler
{
	L2PcInstance _activeChar;
	L2MonsterInstance _target;

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance _item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		if(CastleManorManager.getInstance().isDisabled())
		{
			return false;
		}

		_activeChar = (L2PcInstance) playable;

		if(!(_activeChar.getTarget() instanceof L2MonsterInstance))
		{
			_activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			_activeChar.sendActionFailed();
			return false;
		}

		_target = (L2MonsterInstance) _activeChar.getTarget();

		if(_target == null || !_target.isDead())
		{
			_activeChar.sendActionFailed();
			return false;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(2098, 1); //harvesting skill
		if(skill != null)
		{
			_activeChar.useMagic(skill, false, false);
		}
		return true;
	}
}
