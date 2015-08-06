package dwo.gameserver.model.actor.stat;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.skills.base.L2Skill;

public class SummonStat extends PlayableStat
{
	public SummonStat(L2Summon activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}

	@Override
	public int getRunSpeed()
	{
		if(getActiveChar().getTemplate().getBaseRunSpd() < 0)
		{
			return 0;
		}

		return super.getRunSpeed();
	}

	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return super.getCriticalHit(target, skill);
	}

	@Override
	public int getMaxHp()
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getMaxHp();
	}

	@Override
	public int getMaxMp()
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getMaxMp();
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getMAtk(target, skill);
	}

	@Override
	public int getMAtkSpd()
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getMAtkSpd();
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getMDef(target, skill);
	}

	@Override
	public int getPAtk(L2Character target)
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getPAtk(target);
	}

	@Override
	public int getPAtkSpd()
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getPAtkSpd();
	}

	@Override
	public int getPDef(L2Character target)
	{
		if(getActiveChar() == null)
		{
			return 1;
		}
		return super.getPDef(target);
	}

	@Override
	public int getWalkSpeed()
	{
		if(getActiveChar().getTemplate().getBaseWalkSpd() < 0)
		{
			return 0;
		}

		return super.getWalkSpeed();
	}
}