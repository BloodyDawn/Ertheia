package dwo.gameserver.model.items;

import dwo.gameserver.datatables.xml.AugmentationData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.funcs.FuncAdd;
import dwo.gameserver.model.skills.base.funcs.LambdaConst;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.serverpackets.SkillCoolTime;

import java.util.List;

/**
 * Used to store an augmentation and its boni
 *
 * @author durgus
 */

public class L2Augmentation
{
	private int _effectsId;
	private AugmentationStatBoni _boni;
	private L2Skill _skill;

	public L2Augmentation(int effects, L2Skill skill)
	{
		_effectsId = effects;
		_boni = new AugmentationStatBoni(_effectsId);
		_skill = skill;
	}

	public L2Augmentation(int effects, int skill, int skillLevel)
	{
		this(effects, skill != 0 ? SkillTable.getInstance().getInfo(skill, skillLevel) : null);
	}

	public int getAttributes()
	{
		return _effectsId;
	}

	/**
	 * Get the augmentation "id" used in serverpackets.
	 *
	 * @return augmentationId
	 */
	public int getAugmentationId()
	{
		return _effectsId;
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	/**
	 * Applies the bonuses to the player.
	 *
	 * @param player
	 */
	public void applyBonus(L2PcInstance player)
	{
		boolean updateTimeStamp = false;
		_boni.applyBonus(player);

		// add the skill if any
		if(_skill != null)
		{
			player.addSkill(_skill);
			if(_skill.isActive())
			{
				long delay = player.getSkillRemainingReuseTime(_skill.getReuseHashCode());
				if(delay > 0)
				{
					player.disableSkill(_skill, delay);
					updateTimeStamp = true;
				}
			}
			player.sendSkillList();
			if(updateTimeStamp)
			{
				player.sendPacket(new SkillCoolTime(player));
			}
		}
	}

	/**
	 * Removes the augmentation bonuses from the player.
	 *
	 * @param player
	 */
	public void removeBonus(L2PcInstance player)
	{
		_boni.removeBonus(player);

		// remove the skill if any
		if(_skill != null)
		{
			if(_skill.isPassive())
			{
				player.removeSkill(_skill, false, true);
			}
			else
			{
				player.removeSkill(_skill, false, false);
			}

			player.sendSkillList();
		}
	}

	public static class AugmentationStatBoni
	{
		private Stats[] _stats;
		private float[] _values;
		private boolean _active;

		public AugmentationStatBoni(int augmentationId)
		{
			_active = false;
			List<AugmentationData.AugStat> as = AugmentationData.getInstance().getAugStatsById(augmentationId);

			_stats = new Stats[as.size()];
			_values = new float[as.size()];

			int i = 0;
			for(AugmentationData.AugStat aStat : as)
			{
				_stats[i] = aStat.getStat();
				_values[i] = aStat.getValue();
				i++;
			}
		}

		public void applyBonus(L2PcInstance player)
		{
			// make sure the bonuses are not applied twice..
			if(_active)
			{
				return;
			}

			for(int i = 0; i < _stats.length; i++)
			{
				player.addStatFunc(new FuncAdd(_stats[i], 0x40, this, new LambdaConst(_values[i])));
			}

			_active = true;
		}

		public void removeBonus(L2PcInstance player)
		{
			// make sure the bonuses are not removed twice
			if(!_active)
			{
				return;
			}

			player.removeStatsOwner(this);

			_active = false;
		}
	}
}
