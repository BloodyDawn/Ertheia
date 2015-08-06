package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.PlayerClass;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.08.11
 * Time: 10:50
 */
public class ConditionPlayerClassLevel extends Condition
{
	private ClassLevel _level = ClassLevel.NONE;

	/**
	 * Instantiates a new condition player level.
	 *
	 * @param level the level
	 */
	public ConditionPlayerClassLevel(int level)
	{
		switch(level)
		{
			case 1:
				_level = ClassLevel.FIRST;
				break;
			case 2:
				_level = ClassLevel.SECOND;
				break;
			case 3:
				_level = ClassLevel.THIRD;
				break;
			case 4:
				_level = ClassLevel.AWAKEN;
				break;
		}
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(!(env.getCharacter() instanceof L2PcInstance))
		{
			return false;
		}
		return PlayerClass.values()[((L2PcInstance) env.getCharacter()).getClassId().getId()].getLevel() == _level;
	}
}