package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.effects.L2Effect;
import javolution.util.FastMap;

import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.03.12
 * Time: 6:55
 */

public class Naviarope extends L2Transformation
{
	private static final int[] SKILLS = {619, 11311, 11312, 11313, 11314, 11299, 11269, 11302, 11309, 11310};

	/**
	 * Список умений, который были у персонажа до трансформации, и которые совпадают с умениями трансформации.
	 */
	private final Map<Integer, Integer> _nativeSkills = new FastMap<>();

	public Naviarope()
	{
		// id, colRadius, colHeight
		super(509, 15, 39.50);
	}

	@Override
	public void transformedSkills()
	{
		int level = 1;

		L2PcInstance player = getPlayer();
		L2Effect eff = player.getFirstEffect(11267);
		if(eff != null)
		{
			level = eff.getSkill().getLevel();
		}

		if(level > 3)
		{
			level = 3;
		}

		// Transform Dispel
		player.addSkill(SkillTable.getInstance().getInfo(619, level), false);      // 1-3

		// Удар Набиаропа
		player.addSkill(SkillTable.getInstance().getInfo(11311, level), false);    // 1-3

		// Пламя Набиаропа
		player.addSkill(SkillTable.getInstance().getInfo(11312, level), false);    // 1-3

		// Взрыв Набиаропа
		player.addSkill(SkillTable.getInstance().getInfo(11313, level), false);    // 1-3

		// Взрыв Останков
		player.addSkill(SkillTable.getInstance().getInfo(11314, level), false);    // 1-3

		// Баланс Жизни Слуги
		if(player.getSkills().containsKey(11299))
		{
			_nativeSkills.put(11299, player.getSkillLevel(11299));
		}

		player.addSkill(SkillTable.getInstance().getInfo(11299, 1), false);

		// Разделение Маны
		if(player.getSkills().containsKey(11269))
		{
			_nativeSkills.put(11269, player.getSkillLevel(11269));
		}

		player.addSkill(SkillTable.getInstance().getInfo(11269, 1 + level), false);  // 1-4

		// Основное Лечение Слуги
		if(player.getSkills().containsKey(11302))
		{
			_nativeSkills.put(11302, player.getSkillLevel(11302));
		}

		player.addSkill(SkillTable.getInstance().getInfo(11302, 5 + level), false); // 1-8

		// Благословение Слуги - Массовое
		if(player.getSkills().containsKey(11309))
		{
			_nativeSkills.put(11309, player.getSkillLevel(11309));
		}

		player.addSkill(SkillTable.getInstance().getInfo(11309, 1), false);

		// Абсолютная Защита Слуги - Массовое
		if(player.getSkills().containsKey(11310))
		{
			_nativeSkills.put(11310, player.getSkillLevel(11310));
		}

		player.addSkill(SkillTable.getInstance().getInfo(11310, 1), false);

		player.setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void removeSkills()
	{
		for(int skillId : SKILLS)
		{
			if(getPlayer().getSkills().containsKey(skillId))
			{
				getPlayer().removeSkill(skillId, false);
			}
		}

		for(Map.Entry<Integer, Integer> integerIntegerEntry : _nativeSkills.entrySet())
		{
			getPlayer().addSkill(SkillTable.getInstance().getInfo(integerIntegerEntry.getKey(), integerIntegerEntry.getValue()), false);
		}

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
