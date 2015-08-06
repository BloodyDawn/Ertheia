package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Env;

/**
 * This condition becomes true whether the player is transformed
 * and the transformation Id match the parameter or the parameter is -1
 * which returns true if player is transformed regardless the transformation Id.
 * @author Zoey76
 */
public class ConditionPlayerTransformationId extends Condition
{
	private final int _id;

	/**
	 * Instantiates a new condition player is transformed.
	 * @param id the transformation Id.
	 */
	public ConditionPlayerTransformationId(int id)
	{
		_id = id;
	}

	@Override
	public boolean testImpl(Env env)
	{
		L2PcInstance player = env.getCharacter().getActingPlayer();
		if(player == null)
		{
			return false;
		}
		if(_id == -1)
		{
			return player.isTransformed();
		}
		return player.getTransformationId() == _id;
	}
}
