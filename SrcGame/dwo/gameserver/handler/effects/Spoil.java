/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 *
 * @author Ahmed
 *
 *         This is the Effect support for spoil.
 *
 *         This was originally done by _drunk_
 */
public class Spoil extends L2Effect
{
	public Spoil(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SPOIL;
	}

	@Override
	public boolean onStart()
	{

		if(!(getEffector() instanceof L2PcInstance))
		{
			return false;
		}

		if(!(getEffected() instanceof L2MonsterInstance))
		{
			return false;
		}

		L2MonsterInstance target = (L2MonsterInstance) getEffected();

		if(target == null)
		{
			return false;
		}

		if(target.isSpoil())
		{
			getEffector().sendPacket(SystemMessageId.ALREADY_SPOILED);
			return false;
		}

		// SPOIL SYSTEM by Lbaldi
		boolean spoil = false;
		if(!target.isDead())
		{
			spoil = MagicalDamage.calcMagicSuccess(getEffector(), target, getSkill());

			if(spoil)
			{
				target.setSpoil(true);
				target.setIsSpoiledBy(getEffector());
				getEffector().sendPacket(SystemMessageId.SPOIL_SUCCESS);
			}
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
		}
		return true;
	}

}
