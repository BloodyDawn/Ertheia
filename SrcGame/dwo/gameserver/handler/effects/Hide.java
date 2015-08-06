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

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.DeleteObject;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 *
 * @author ZaKaX - nBd
 */
public class Hide extends L2Effect
{
	public Hide(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public Hide(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HIDE;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			addRemovedEffectType(L2EffectStopCond.ON_DAMAGE_BUFF);
			addRemovedEffectType(L2EffectStopCond.ON_ACTION_EXCEPT_MOVE);

			L2PcInstance activeChar = (L2PcInstance) getEffected();
			activeChar.getAppearance().setInvisible();
			activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);

			if(activeChar.getAI().getNextIntention() != null && activeChar.getAI().getNextIntention().getCtrlIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}

			L2GameServerPacket del = new DeleteObject(activeChar);
			for(L2Character target : activeChar.getKnownList().getKnownCharacters())
			{
				try
				{
					if(target.getTarget() == activeChar)
					{
						target.setTarget(null);
						target.abortAttack();
						target.abortCast();
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					}

					if(target instanceof L2PcInstance && !target.isGM())
					{
						target.sendPacket(del);
					}
				}
				catch(NullPointerException e)
				{
                    _log.info(getClass().getSimpleName() + "", e);
				}
			}
		}
		return true;
	}

	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			L2PcInstance activeChar = (L2PcInstance) getEffected();
			if(!activeChar.getObserverController().isObserving())
			{
				activeChar.getAppearance().setVisible();
			}
			activeChar.stopAbnormalEffect(AbnormalEffect.STEALTH);
		}
	}

}