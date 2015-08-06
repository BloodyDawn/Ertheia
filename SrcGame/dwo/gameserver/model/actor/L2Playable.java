/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.actor;

import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.controller.player.PvPFlagController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.knownlist.PlayableKnownList;
import dwo.gameserver.model.actor.stat.PlayableStat;
import dwo.gameserver.model.actor.status.PlayableStatus;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;

/**
 * This class represents all Playable characters in the world.<BR><BR>
 * <p/>
 * L2PlayableInstance :<BR><BR>
 * <li>L2PcInstance</li>
 * <li>L2Summon</li><BR><BR>
 */

public abstract class L2Playable extends L2Character
{
	L2PcInstance transferDmgTo;
	private L2Character _lockedTarget;
	private boolean _isTransferringDmg;
	private boolean _isUnderBetrayalMark;
	private L2Playable _isTransferringDmgTo;

	/**
	 * Constructor of L2PlayableInstance (use L2Character constructor).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and link copy basic Calculator set to this L2PlayableInstance </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the L2PlayableInstance
	 */
	protected L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized(this)
		{
			if(isDead())
			{
				return false;
			}
			// now reset currentHp to zero
			setCurrentHp(0);
			setIsDead(true);
		}

		// Set target to null and cancel Attack or Cast
		setTarget(null);

		// Stop movement
		stopMove(null);

		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();

		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if(isPhoenixBlessed())
		{
			if(getCharmOfLuck()) //remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			{
				stopCharmOfLuck(null);
			}
			if(isNoblesseBlessed())
			{
				stopNoblesseBlessing(null);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
		else if(isNoblesseBlessed())
		{
			stopNoblesseBlessing(null);

			if(getCharmOfLuck()) //remove Lucky Charm if player have Nobless blessing buff
			{
				stopCharmOfLuck(null);
			}
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		// Send the ServerMode->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();

		if(getLocationController().getWorldRegion() != null)
		{
			getLocationController().getWorldRegion().onDeath(this);
		}

		if(killer != null)
		{
			L2PcInstance player = killer.getActingPlayer();

			if(player != null)
			{
				player.onKillUpdatePvPReputation(this);
			}
		}

		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD);

		return true;
	}

	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}

	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}

	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}

	@Override
	public PlayableKnownList getKnownList()
	{
		return (PlayableKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new PlayableKnownList(this));
	}

	public boolean checkIfPvP(L2Character target)
	{
		L2PcInstance player = getActingPlayer();

		if(player == null)
		{
			return false;                      // Active player is null
		}
		if(player.hasBadReputation())
		{
			return false;                      // Active player has bad reputatuion
		}

		L2PcInstance targetPlayer = target.getActingPlayer();

		if(targetPlayer == null)
		{
			return false;                     // Target player is null
		}
		if(targetPlayer.equals(this))
		{
			return false;                     // Target player is self
		}
		if(targetPlayer.hasBadReputation())
		{
			return false;                     // Target player has karma
		}
		return targetPlayer.getPvPFlagController().isFlagged();

	}

	/**
	 * @return {@code true} if current character is attackable
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}

	@Override
	public boolean isPlayable()
	{
		return true;
	}

	/**
	 * @return {@code true} если персонаж находится под действием Благославления Дворянина
	 */
	public boolean isNoblesseBlessed()
	{
		return _effects.isAffected(CharEffectList.EFFECT_FLAG_NOBLESS_BLESSING);
	}

	public void stopNoblesseBlessing(L2Effect effect)
	{
		if(effect == null)
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		updateAbnormalEffect();
	}

	/**
	 * @return {@code true} если персонаж находится под действием Душой Феникса или сальвы
	 */
	public boolean isPhoenixBlessed()
	{
		return _effects.isAffected(CharEffectList.EFFECT_FLAG_PHOENIX_BLESSING);
	}

	public void stopPhoenixBlessing(L2Effect effect)
	{
		if(effect == null)
		{
			stopEffects(L2EffectType.PHOENIX_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		updateAbnormalEffect();
	}

	/**
	 * @return {@code true} if the SilentMoving mode is active.
	 */
	public boolean isSilentMoving()
	{
		return _effects.isAffected(CharEffectList.EFFECT_FLAG_SILENT_MOVE);
	}

	/**
	 * @return {@code true} если персонаж находится под защитой от хаотических персонажей
	 */
	public boolean getProtectionBlessing()
	{
		return _effects.isAffected(CharEffectList.EFFECT_FLAG_PROTECTION_BLESSING);
	}

	/**
	 * @param effect blessing
	 */
	public void stopProtectionBlessing(L2Effect effect)
	{
		if(effect == null)
		{
			stopEffects(L2EffectType.PROTECTION_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		updateAbnormalEffect();
	}

	/**
	 * @return {@code true} если персонаж находится под Амулетом Удачи (уменьшает штраф при убийстве персонажа рейдом)
	 */
	public boolean getCharmOfLuck()
	{
		return _effects.isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_LUCK);
	}

	public void stopCharmOfLuck(L2Effect effect)
	{
		if(effect == null)
		{
			stopEffects(L2EffectType.CHARM_OF_LUCK);
		}
		else
		{
			removeEffect(effect);
		}

		updateAbnormalEffect();
	}

	public boolean isLockedTarget()
	{
		return _lockedTarget != null;
	}

	public L2Character getLockedTarget()
	{
		return _lockedTarget;
	}

	public void setLockedTarget(L2Character cha)
	{
		_lockedTarget = cha;
	}

	public abstract int getReputation();

	public abstract boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove);

	public abstract PvPFlagController getPvPFlagController();

	public void setTransferDamageTo(L2PcInstance val)
	{
		transferDmgTo = val;
	}

	public L2PcInstance getTransferingDamageTo()
	{
		return transferDmgTo;
	}

	public void setIsUnderBetrayalMark(boolean value)
	{
		_isUnderBetrayalMark = value;
	}

	public boolean isUnderBetrayalMark()
	{
		return _isUnderBetrayalMark;
	}

	public void setIsTransferringDmg(boolean value)
	{
		_isTransferringDmg = value;
	}

	public boolean isTransferringDmg()
	{
		return _isTransferringDmg;
	}

	public void setIsTransferringDmgTo(L2Playable obj)
	{
		_isTransferringDmgTo = obj;
	}

	public L2Playable isTransferringDmgTo()
	{
		return _isTransferringDmgTo;
	}

	public abstract boolean isOnline();

	public abstract void store();

	public abstract void storeEffect(boolean storeEffects);

	public abstract void restoreEffects();
}