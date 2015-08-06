package dwo.gameserver.model.skills;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import javolution.util.FastList;

import java.util.List;

public class L2EnchantSkillGroup
{
	private final int _id;
	private List<EnchantSkillDetail> _enchantDetails = new FastList<>();

	public L2EnchantSkillGroup(int id)
	{
		_id = id;
	}

	public void addEnchantDetail(EnchantSkillDetail detail)
	{
		_enchantDetails.add(detail);
	}

	public int getId()
	{
		return _id;
	}

	public List<EnchantSkillDetail> getEnchantGroupDetails()
	{
		return _enchantDetails;
	}

	public static class EnchantSkillDetail
	{
		private final int _level;
		private final int _adenaCost;
		private final int _expCost;
		private final int _spCost;
		private final byte[] _rate;

		public EnchantSkillDetail(int lvl, int adena, int exp, int sp, byte[] rate)
		{
			_level = lvl;
			_adenaCost = adena;
			_expCost = exp;
			_spCost = sp;
			_rate = rate;
		}

		/**
		 * @return Returns the level.
		 */
		public int getLevel()
		{
			return _level;
		}

		/**
		 * @return Returns the spCost.
		 */
		public int getSpCost()
		{
			return _spCost;
		}

		public int getExpCost()
		{
			return _expCost;
		}

		public int getAdenaCost()
		{
			return _adenaCost;
		}

		public byte getRate(L2PcInstance ply)
		{
			if(ply.getLevel() < 76)
			{
				return 0;
			}
			return _rate[ply.getLevel() - 76];
		}
	}
}