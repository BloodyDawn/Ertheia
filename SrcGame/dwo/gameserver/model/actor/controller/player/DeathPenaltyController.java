package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Player Death Penalty controller.
 * Implemented as hook for catching die moments without code injections into PC Instance.
 *
 * @author Yorie
 */
public class DeathPenaltyController extends PlayerController
{
	public static final int DEATH_PENALTY_SKILL_ID = 14571;
	public static final short DEATH_PENALTY_MAX_LEVEL = 5;
	public static final List<Integer> RAID_LIST = new FastList<>();

	static
	{
		RAID_LIST.add(25779); // Spacia Common
		RAID_LIST.add(25867); // Spacia Extreme
		RAID_LIST.add(25785); // Trajan
		RAID_LIST.add(25796); // Emambifi 1
		RAID_LIST.add(25881); // Emambifi 2
		RAID_LIST.add(25797); // Kechi
		RAID_LIST.add(25799); // Michael
		RAID_LIST.add(25876); // Melissa
		RAID_LIST.add(25877); // Isadora

		// Fortuna RBs
		RAID_LIST.add(25837); // Yui
		RAID_LIST.add(25840); // Kinnen
		RAID_LIST.add(25845); // Konyar
		RAID_LIST.add(25841); // Resinda
		RAID_LIST.add(25838); // Mukhshu
		RAID_LIST.add(25839); // Konapi
		RAID_LIST.add(25846); // Yoentumak
		RAID_LIST.add(25825); // Fron

		// Instances
		RAID_LIST.add(29194); // Octavis Common
		RAID_LIST.add(29212); // Octavis Extreme
		RAID_LIST.add(29195); // Istina Common
		RAID_LIST.add(29196); // Istina Extreme
		RAID_LIST.add(29218); // Balok
		RAID_LIST.add(29213); // Baylor
		RAID_LIST.add(29068); // Antharas
	}

	public DeathPenaltyController(L2PcInstance player)
	{
		super(player);
		player.getHookContainer().addHook(HookType.ON_DIE, new DieHook());
	}

	/**
	 * Computes DP level at now.
	 * @return DP level.
	 */
	public int getDeathPenaltyLevel()
	{
		L2Effect effect = getEffect();
		return effect == null ? 0 : effect.getSkill().getLevel();
	}

	/**
	 * Sets up DP level to @level value.
	 * @param level DP level.
	 */
	public void setDeathPenaltyLevel(int level)
	{
		if(level <= 0)
		{
			cancelDeathPenalty();
		}
		else
		{
			level = Math.min(level, DEATH_PENALTY_MAX_LEVEL);
			SkillTable.getInstance().getInfo(DEATH_PENALTY_SKILL_ID, level).getEffects(player, player);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(level));
		}
	}

	/**
	 * Increases DP level if possible.
	 */
	public void increaseDeathPenalty()
	{
		setDeathPenaltyLevel(getDeathPenaltyLevel() + 1);
	}

	/**
	 * Reduces DP level if possible.
	 */
	public void reduceDeathPenalty()
	{
		setDeathPenaltyLevel(getDeathPenaltyLevel() - 1);
	}

	/**
	 * Cancels DP independent from its level.
	 */
	public void cancelDeathPenalty()
	{
		L2Effect effect = getEffect();
		if(effect != null)
		{
			effect.exit();
			player.sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		}
	}

	/**
	 * Returns DP effect instance for current player.
	 * @return DP L2Effect instance.
	 */
	@Nullable
	protected L2Effect getEffect()
	{
		return player.getFirstEffect(DEATH_PENALTY_SKILL_ID);
	}

	/**
	 * @author Yorie
	 */
	private class DieHook extends AbstractHookImpl
	{
		/**
		 * Increases DP level if it's possible based on who killed current player.
		 * @param killer Player killer.
		 */
		@Override
		public void onDie(L2PcInstance player, L2Character killer)
		{
			L2Npc killerAsNpc = killer.getNpcInstance();
			if((killerAsNpc != null && RAID_LIST.contains(killerAsNpc.getNpcId()) || Rnd.getChance(2)) // Must be RB or 2% chance to gain
				&& !player.getCharmOfLuck() // Does not have Charm of Luck buff
				&& !player.isPhoenixBlessed() // Have no Phoenix buff
				&& !player.isLucky() // No Lucky passive
				&& !(EventManager.isStarted() && EventManager.isPlayerParticipant(player)) // Not an participant of event
				&& !(player.isInsideZone(L2Character.ZONE_PVP) || player.isInsideZone(L2Character.ZONE_SIEGE)) // Not in PvP zone
				)
			{
				increaseDeathPenalty();
			}
		}
	}
}
