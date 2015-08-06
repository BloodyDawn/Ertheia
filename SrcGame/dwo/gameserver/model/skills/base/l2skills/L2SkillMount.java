package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;

public class L2SkillMount extends L2Skill
{
	private final int _npcId;
	private final int _itemId;

	public L2SkillMount(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_itemId = set.getInteger("itemId", 0);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if(!caster.isPlayer())
		{
			return;
		}

		L2PcInstance activePlayer = caster.getActingPlayer();

		if(!activePlayer.getFloodProtectors().getItemPetSummon().tryPerformAction(FloodAction.SKILL_MOUNT))
		{
			return;
		}

		if(!EventManager.onItemSummon(activePlayer))
		{
			return;
		}

		// Dismount Action
		if(_npcId == 0)
		{
			activePlayer.dismount();
			return;
		}

		if(activePlayer.isSitting())
		{
			activePlayer.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}

		if(activePlayer.getObserverController().isObserving())
		{
			return;
		}

		if(activePlayer.getOlympiadController().isParticipating())
		{
			activePlayer.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

		if(!activePlayer.getPets().isEmpty() || activePlayer.isMounted())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}

		if(activePlayer.isAttackingNow() || activePlayer.isCursedWeaponEquipped())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}

		if(activePlayer.isCursedWeaponEquipped())
		{
			return;
		}

		activePlayer.mount(_npcId, _itemId, false);
	}
}
