package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * Restores some amount of MP and rest is going to HP.
 *
 * @author Yorie
 */

public class MpHpHealByLevel extends MpHealByLevel
{
	public MpHpHealByLevel(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if(target == null || target.isDead() || target instanceof L2DoorInstance)
		{
			return false;
		}

		double rest = restoreMp(target);

		// Restore HP
		if(rest > 0.0)
		{
			target.setCurrentHp(target.getCurrentHp() + rest);

			SystemMessage sm;
			if(getEffector().getObjectId() == target.getObjectId())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
				sm.addCharName(getEffector());
			}
			sm.addNumber((int) rest);
			target.sendPacket(sm);
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
		}

		return true;
	}

}
