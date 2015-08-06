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
package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.fort.Fort;

/**
 * The Class ConditionSiegeZone.
 *
 * @author Gigiikun
 */

public class ConditionSiegeZone extends Condition
{
	//	conditional values
	public static final int COND_NOT_ZONE = 0x0001;
	public static final int COND_CAST_ATTACK = 0x0002;
	public static final int COND_CAST_DEFEND = 0x0004;
	public static final int COND_CAST_NEUTRAL = 0x0008;
	public static final int COND_FORT_ATTACK = 0x0010;
	public static final int COND_FORT_DEFEND = 0x0020;
	public static final int COND_FORT_NEUTRAL = 0x0040;
	public static final int COND_TW_CHANNEL = 0x0080;
	public static final int COND_TW_PROGRESS = 0x0100;

	private final int _value;
	private final boolean _self;

	/**
	 * Instantiates a new condition siege zone.
	 *
	 * @param value the value
	 * @param self the self
	 */

	public ConditionSiegeZone(int value, boolean self)
	{
		_value = value;
		_self = self;
	}

	/**
	 * Check if ok.
	 *
	 * @param activeChar the active char
	 * @param castle the castle
	 * @param value the value
	 * @return true, if successful
	 */
	public static boolean checkIfOk(L2Character activeChar, Castle castle, int value)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) activeChar;

		if(castle == null || castle.getCastleId() <= 0)
		{
			if((value & COND_NOT_ZONE) != 0)
			{
				return true;
			}
		}
		else if(!castle.getZone().isSiegeActive())
		{
			if((value & COND_NOT_ZONE) != 0)
			{
				return true;
			}
		}
		else if((value & COND_CAST_ATTACK) != 0 && player.isRegisteredOnThisSiegeField(castle.getCastleId()) && player.getSiegeSide() == PlayerSiegeSide.ATTACKER)
		{
			return true;
		}
		else if((value & COND_CAST_DEFEND) != 0 && player.isRegisteredOnThisSiegeField(castle.getCastleId()) && player.getSiegeSide() == PlayerSiegeSide.DEFENDER)
		{
			return true;
		}
		else if((value & COND_CAST_NEUTRAL) != 0 && player.getSiegeSide() == PlayerSiegeSide.NONE)
		{
			return true;
		}

		return false;
	}

	/**
	 * Check if ok.
	 *
	 * @param activeChar the active char
	 * @param fort the fort
	 * @param value the value
	 * @return true, if successful
	 */
	public static boolean checkIfOk(L2Character activeChar, Fort fort, int value)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) activeChar;

		if(fort == null || fort.getFortId() <= 0)
		{
			if((value & COND_NOT_ZONE) != 0)
			{
				return true;
			}
		}
		else if(!fort.getSiege().isInProgress())
		{
			if((value & COND_NOT_ZONE) != 0)
			{
				return true;
			}
		}
		else if((value & COND_FORT_ATTACK) != 0 && player.isRegisteredOnThisSiegeField(fort.getFortId()) && player.getSiegeSide() == PlayerSiegeSide.ATTACKER)
		{
			return true;
		}
		else if((value & COND_FORT_DEFEND) != 0 && player.isRegisteredOnThisSiegeField(fort.getFortId()) && player.getSiegeSide() == PlayerSiegeSide.DEFENDER)
		{
			return true;
		}
		else if((value & COND_FORT_NEUTRAL) != 0 && player.getSiegeSide() == PlayerSiegeSide.NONE)
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean testImpl(Env env)
	{
		L2Character target = _self ? env.getCharacter() : env.getTarget();
		Castle castle = CastleManager.getInstance().getCastle(target);
		Fort fort = FortManager.getInstance().getFort(target);

		if(castle == null && fort == null)
		{
			return (_value & COND_NOT_ZONE) != 0;
		}
		return castle != null ? checkIfOk(target, castle, _value) : checkIfOk(target, fort, _value);
	}
}
