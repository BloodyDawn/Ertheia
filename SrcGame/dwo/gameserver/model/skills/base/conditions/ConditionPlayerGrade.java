package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * The Class ConditionPlayerGrade.
 * @author Gigiikun
 */
public class ConditionPlayerGrade extends Condition
{
	//	conditional values
	public static final int COND_NO_GRADE = 0x0001;
	public static final int COND_D_GRADE = 0x0002;
	public static final int COND_C_GRADE = 0x0004;
	public static final int COND_B_GRADE = 0x0008;
	public static final int COND_A_GRADE = 0x0010;
	public static final int COND_S_GRADE = 0x0020;
	public static final int COND_S80_GRADE = 0x0040;
	public static final int COND_S84_GRADE = 0x0080;
	public static final int COND_R_GRADE = 0x0100;
	public static final int COND_R95_GRADE = 0x0120;
	public static final int COND_R99_GRADE = 0x0140;
	private final int _value;

	/**
	 * Instantiates a new condition player grade.
	 * @param value the value
	 */
	public ConditionPlayerGrade(int value)
	{
		_value = value;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if(env.getCharacter() instanceof L2PcInstance)
		{
			byte expIndex = (byte) ((L2PcInstance) env.getCharacter()).getExpertiseLevel();

			return _value == expIndex;
		}
		return false;
	}
}