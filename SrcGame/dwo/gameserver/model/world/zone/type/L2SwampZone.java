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
package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.EventTrigger;

/**
 * another type of zone where your speed is changed
 *
 * @author kerberos
 */
public class L2SwampZone extends L2ZoneType
{
	private int _move_bonus;
    private int _eventId;
	private int _castleId;
	private Castle _castle;

	public L2SwampZone(int id)
	{
		super(id);

		// Setup default speed reduce (in %)
		_move_bonus = -50;
        _eventId = 0;
		// no castle by default
		_castleId = 0;
		_castle = null;
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch(name)
		{
			case "move_bonus":
				_move_bonus = Integer.parseInt(value);
				break;
            case "eventId":
                _eventId = Integer.parseInt(value);
                break;
			case "castleId":
				_castleId = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(getCastle() != null)
		{
			// castle zones active only during siege
			if(!getCastle().getSiege().isInProgress() || !isEnabled())
			{
				return;
			}

			// defenders not affected
			L2PcInstance player = character.getActingPlayer();
			if(player != null && player.isInSiege() && player.getSiegeSide() == PlayerSiegeSide.DEFENDER)
			{
				return;
			}
		}

		character.setInsideZone(L2Character.ZONE_SWAMP, true);
		if(character instanceof L2PcInstance)
		{
            if (_eventId > 0)
            {
                character.sendPacket(new EventTrigger(_eventId, true));
            }
            
			((L2PcInstance) character).broadcastUserInfo();
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		// don't broadcast info if not needed
		if(character.isInsideZone(L2Character.ZONE_SWAMP))
		{
			character.setInsideZone(L2Character.ZONE_SWAMP, false);
			if(character instanceof L2PcInstance)
			{
                if (_eventId > 0)
                {
                    character.sendPacket(new EventTrigger(_eventId, false));
                }
                
				((L2PcInstance) character).broadcastUserInfo();
			}
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	private Castle getCastle()
	{
		if(_castleId > 0 && _castle == null)
		{
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		}

		return _castle;
	}

	public int getMoveBonus()
	{
		return _move_bonus;
	}
}