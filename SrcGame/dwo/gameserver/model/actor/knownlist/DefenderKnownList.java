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

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DefenderInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;

public class DefenderKnownList extends AttackableKnownList
{
	public DefenderKnownList(L2DefenderInstance activeChar)
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

		Castle castle = getActiveChar().getCastle();
		Fort fortress = getActiveChar().getFort();
		ClanHallSiegable hall = getActiveChar().getConquerableHall();
		// Check if siege is in progress
		if(fortress != null && fortress.getZone().isSiegeActive() || castle != null && castle.getZone().isSiegeActive() || hall != null && hall.getSiegeZone().isSiegeActive())
		{
			L2PcInstance player = null;
			if(object.isPlayable())
			{
				player = object.getActingPlayer();
			}
			int activeSiegeId = fortress != null ? fortress.getFortId() : castle != null ? castle.getCastleId() : hall != null ? hall.getId() : 0;

			// Check if player is an enemy of this defender npc
			if(player != null && (player.getSiegeSide() == PlayerSiegeSide.DEFENDER && !player.isRegisteredOnThisSiegeField(activeSiegeId) || player.getSiegeSide() == PlayerSiegeSide.ATTACKER || player.getSiegeSide() == PlayerSiegeSide.NONE))
			{
				if(getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		return true;
	}

	@Override
	public L2DefenderInstance getActiveChar()
	{
		return (L2DefenderInstance) super.getActiveChar();
	}
}
