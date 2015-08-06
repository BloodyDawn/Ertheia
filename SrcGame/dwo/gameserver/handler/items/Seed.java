package dwo.gameserver.handler.items;

import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2ChestInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author l3x
 */

public class Seed implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		if(CastleManorManager.getInstance().isDisabled())
		{
			return false;
		}

		L2Object tgt = playable.getTarget();
		if(!(tgt instanceof L2Npc))
		{
			playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
			playable.sendActionFailed();
			return false;
		}
		if(!(tgt instanceof L2MonsterInstance) || tgt instanceof L2ChestInstance || ((L2Character) tgt).isRaid())
		{
			playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			playable.sendActionFailed();
			return false;
		}

		L2MonsterInstance target = (L2MonsterInstance) tgt;
		if(target.isDead())
		{
			playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
			playable.sendActionFailed();
			return false;
		}

		if(target.isSeeded())
		{
			playable.sendActionFailed();
			return false;
		}

		int seedId = item.getItemId();
		if(!areaValid(seedId, MapRegionManager.getInstance().getAreaCastle(playable)))
		{
			playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}

		target.setSeeded(seedId, (L2PcInstance) playable);
		SkillHolder[] skills = item.getEtcItem().getSkills();
		if(skills != null)
		{
			if(skills[0] == null)
			{
				return false;
			}

			L2Skill itemskill = skills[0].getSkill();
			playable.useMagic(itemskill, false, false);
		}

		return true;
	}

	/**
	 * @param seedId
	 * @param castleId
	 * @return
	 */
	private boolean areaValid(int seedId, int castleId)
	{
		return ManorData.getInstance().getCastleIdForSeed(seedId) == castleId;
	}
}