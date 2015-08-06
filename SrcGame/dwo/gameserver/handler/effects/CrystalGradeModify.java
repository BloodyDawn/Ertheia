package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class CrystalGradeModify extends L2Effect
{
	public CrystalGradeModify(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		L2PcInstance player = getEffected().getActingPlayer();
		if(player == null)
		{
			return false;
		}
		player.setExpertisePenaltyBonus((int) calc());
		return true;
	}

	@Override
	public void onExit()
	{
		L2PcInstance player = getEffected().getActingPlayer();
		if(player == null)
		{
			return;
		}
		player.setExpertisePenaltyBonus(0);
	}

}
