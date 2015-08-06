package dwo.gameserver.model.actor.ai;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Character.AIAccessor;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * This class manages AI of L2Playable.<BR><BR>
 * <p/>
 * L2PlayableAI :<BR><BR>
 * <li>L2SummonAI</li>
 * <li>L2PlayerAI</li>
 * <BR> <BR>
 *
 * @author JIV
 */

public abstract class L2PlayableAI extends L2CharacterAI
{
	/**
	 * @param accessor
	 */
	protected L2PlayableAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if(target instanceof L2Playable)
		{
			if(target.getActingPlayer().getProtectionBlessing() && _actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel() >= 10 && _actor.getActingPlayer().hasBadReputation() && !target.isInsideZone(L2Character.ZONE_PVP))
			{
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}

			if(_actor.getActingPlayer().getProtectionBlessing() && target.getActingPlayer().getLevel() - _actor.getActingPlayer().getLevel() >= 10 && target.getActingPlayer().hasBadReputation() && !target.isInsideZone(L2Character.ZONE_PVP))
			{
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}

			if(target.getActingPlayer().isCursedWeaponEquipped() && _actor.getActingPlayer().getLevel() <= 20)
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}

			if(_actor.getActingPlayer().isCursedWeaponEquipped() && target.getActingPlayer().getLevel() <= 20)
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
		}

		super.onIntentionAttack(target);
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if(target instanceof L2Playable && skill.isOffensive())
		{
			if(target.getActingPlayer().getProtectionBlessing() && _actor.getActingPlayer().getLevel() - target.getActingPlayer().getLevel() >= 10 && _actor.getActingPlayer().hasBadReputation() && !((L2Playable) target).isInsideZone(L2Character.ZONE_PVP))
			{
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				if(_actor.isDoubleCastingNow())
				{
					_actor.setIsDoubleCastingNow(false);
				}
				else
				{
					_actor.setIsCastingNow(false);
				}
				return;
			}

			if(_actor.getActingPlayer().getProtectionBlessing() && target.getActingPlayer().getLevel() - _actor.getActingPlayer().getLevel() >= 10 && target.getActingPlayer().hasBadReputation() && !((L2Playable) target).isInsideZone(L2Character.ZONE_PVP))
			{
				// Newbie Protection Buff,
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				if(_actor.isDoubleCastingNow())
				{
					_actor.setIsDoubleCastingNow(false);
				}
				else
				{
					_actor.setIsCastingNow(false);
				}
				return;
			}

			if(target.getActingPlayer().isCursedWeaponEquipped() && _actor.getActingPlayer().getLevel() <= 20)
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				_actor.setIsDoubleCastingNow(false);
				return;
			}

			if(_actor.getActingPlayer().isCursedWeaponEquipped() && target.getActingPlayer().getLevel() <= 20)
			{
				_actor.getActingPlayer().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				_actor.setIsDoubleCastingNow(false);
				return;
			}
		}
		super.onIntentionCast(skill, target);
	}
}
