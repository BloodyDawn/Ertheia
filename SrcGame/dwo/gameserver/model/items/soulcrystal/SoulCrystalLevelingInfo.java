package dwo.gameserver.model.items.soulcrystal;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.06.12
 * Time: 17:48
 */

public class SoulCrystalLevelingInfo
{
	private final SoulCrystalAbsorbType _soulCrystalAbsorbType;
	private final boolean _isSkillNeeded;
	private final int _chance;

	public SoulCrystalLevelingInfo(SoulCrystalAbsorbType soulCrystalAbsorbType, boolean isSkillNeeded, int chance)
	{
		_soulCrystalAbsorbType = soulCrystalAbsorbType;
		_isSkillNeeded = isSkillNeeded;
		_chance = chance;
	}

	public SoulCrystalAbsorbType getAbsorbCrystalType()
	{
		return _soulCrystalAbsorbType;
	}

	public boolean isSkillNeeded()
	{
		return _isSkillNeeded;
	}

	public int getChance()
	{
		return _chance;
	}
}