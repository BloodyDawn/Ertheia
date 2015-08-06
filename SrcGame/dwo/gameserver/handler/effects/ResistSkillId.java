package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.11.11
 * Time: 13:50
 */
public class ResistSkillId extends L2Effect
{
	private int[] _resistId;

	public ResistSkillId(Env env, EffectTemplate template)
	{
		super(env, template);

		String[] split = template.additional.split(",");
		_resistId = new int[split.length];
		for(int i = 0; i < _resistId.length; i++)
		{
			_resistId[i] = Integer.parseInt(split[i].trim());
		}
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RESIST_SKILL_ID;
	}

	@Override
	public boolean onStart()
	{
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_RESIST_SKILLID;
	}

	public boolean isResitedBySkillId(int id)
	{
		return ArrayUtils.contains(_resistId, id);
	}
}
