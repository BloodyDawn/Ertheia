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
package dwo.gameserver.model.actor.knownlist;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2TrainingDollInstance;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class GuardKnownList extends AttackableKnownList
{
	private static final Logger _log = LogManager.getLogger(GuardKnownList.class);

	public GuardKnownList(L2GuardInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(L2Object object)
	{
		if(!super.addKnownObject(object))
		{
			return false;
		}

		if(object.isPlayer())
		{
			// Check if the object added is a L2PcInstance that owns Karma
			if(object.getActingPlayer().hasBadReputation())
			{
				// Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
				if(getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if(Config.GUARD_ATTACK_AGGRO_MOB && getActiveChar().isInActiveRegion() && object instanceof L2MonsterInstance)
		{
			// Check if the object added is an aggressive L2MonsterInstance
			if(((L2MonsterInstance) object).isAggressive())
			{
				// Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
				if(getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}

        /*
            TODO: От этой бабуйни-хардкода избавится при первой возможности, когда понадобится чтобы гвард атаковал что-то
            TODO: другое кроме L2TrainingDollInstance. Кукла для игрока имеет тот же инстанс что и для гварда, для этого вводим проверку по ID
        */
		else if(object instanceof L2TrainingDollInstance && ((L2TrainingDollInstance) object).getNpcId() == 33023 && getActiveChar().isInActiveRegion())
		{
			if(getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		return true;
	}

	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if(!super.removeKnownObject(object, forget))
		{
			return false;
		}

		// Check if the _aggroList of the L2GuardInstance is Empty
		if(getActiveChar().noTarget())
		{
			// Set the L2GuardInstance to AI_INTENTION_IDLE
			if(getActiveChar().hasAI())
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}

		return true;
	}

	@Override
	public L2GuardInstance getActiveChar()
	{
		return (L2GuardInstance) super.getActiveChar();
	}
}