package dwo.gameserver.model.skills.stats;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;

public class Env
{
	private L2Character _player;
	private L2CubicInstance _cubic;
	private L2Character _target;
	private L2ItemInstance _item;
	private L2Skill _skill;
	private L2Effect _effect;
	private double _value;
	private double _baseValue;
	private boolean _skillMastery;
	private byte _shld;
	private boolean _ss;
	private boolean _sps;
	private boolean _bss;

	public Env()
	{

	}

	public Env(byte shld, boolean ss, boolean sps, boolean bbs)
	{
		_shld = shld;
		_ss = ss;
		_sps = sps;
		_bss = bbs;
	}

	public L2Character getCharacter()
	{
		return _player;
	}

	/**
	 * @return the acting player.
	 */
	public L2PcInstance getPlayer()
	{
		return _player == null ? null : _player.getActingPlayer();
	}

	public void setPlayer(L2Character player)
	{
		_player = player;
	}

	public L2CubicInstance getCubic()
	{
		return _cubic;
	}

	public void setCubic(L2CubicInstance cubic)
	{
		_cubic = cubic;
	}

	public L2Character getTarget()
	{
		return _target;
	}

	public void setTarget(L2Character target)
	{
		_target = target;
	}

	public L2ItemInstance getItem()
	{
		return _item;
	}

	public void setItem(L2ItemInstance item)
	{
		_item = item;
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	public void setSkill(L2Skill skill)
	{
		_skill = skill;
	}

	public L2Effect getEffect()
	{
		return _effect;
	}

	public void setEffect(L2Effect effect)
	{
		_effect = effect;
	}

	public double getValue()
	{
		return _value;
	}

	public void setValue(double value)
	{
		_value = value;
	}

	public void addValue(double value)
	{
		_value += value;
	}

	public void subValue(double value)
	{
		_value -= value;
	}

	public void mulValue(double value)
	{
		_value *= value;
	}

	public void divValue(double value)
	{
		_value /= value;
	}

	public double getBaseValue()
	{
		return _baseValue;
	}

	public void setBaseValue(double baseValue)
	{
		_baseValue = baseValue;
	}

	public boolean isSkillMastery()
	{
		return _skillMastery;
	}

	public void setSkillMastery(boolean skillMastery)
	{
		_skillMastery = skillMastery;
	}

	public byte getShld()
	{
		return _shld;
	}

	public void setShld(byte shld)
	{
		_shld = shld;
	}

	public boolean isSs()
	{
		return _ss;
	}

	public void setSs(boolean ss)
	{
		_ss = ss;
	}

	public boolean isSps()
	{
		return _sps;
	}

	public void setSps(boolean sps)
	{
		_sps = sps;
	}

	public boolean isBss()
	{
		return _bss;
	}

	public void setBss(boolean bss)
	{
		_bss = bss;
	}
}
