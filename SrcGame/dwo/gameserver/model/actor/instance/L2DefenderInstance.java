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
package dwo.gameserver.model.actor.instance;

import dwo.config.Config;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.ai.L2FortSiegeGuardAI;
import dwo.gameserver.model.actor.ai.L2SiegeGuardAI;
import dwo.gameserver.model.actor.ai.L2SpecialSiegeGuardAI;
import dwo.gameserver.model.actor.knownlist.DefenderKnownList;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import org.apache.log4j.Level;

public class L2DefenderInstance extends L2Attackable
{
	private Castle _castle; // the castle which the instance should defend
	private Fort _fort; // the fortress which the instance should defend
	private ClanHallSiegable _hall; // the siegable hall which the instance should defend

	public L2DefenderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if(ai == null)
		{
			synchronized(this)
			{
				if(_ai == null)
				{
					if(getConquerableHall() == null && getCastle(10000) == null)
					{
						_ai = new L2FortSiegeGuardAI(new AIAccessor());
					}
					else
					{
						_ai = getCastle(10000) != null ? new L2SiegeGuardAI(new AIAccessor()) : new L2SpecialSiegeGuardAI(new AIAccessor());
					}
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if(attacker == null)
		{
			return;
		}

		if(!(attacker instanceof L2DefenderInstance))
		{
			if(damage == 0 && aggro <= 1 && attacker instanceof L2Playable)
			{
				L2PcInstance player = attacker.getActingPlayer();
				// Check if siege is in progress
				if(_fort != null && _fort.getZone().isSiegeActive() || _castle != null && _castle.getZone().isSiegeActive() || _hall != null && _hall.getSiegeZone().isSiegeActive())
				{
					int activeSiegeId = _fort != null ? _fort.getFortId() : _castle != null ? _castle.getCastleId() : _hall != null ? _hall.getId() : 0;
					if(player != null && player.getSiegeSide() == PlayerSiegeSide.DEFENDER && player.isRegisteredOnThisSiegeField(activeSiegeId))
					{
						return;
					}
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public DefenderKnownList getKnownList()
	{
		return (DefenderKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new DefenderKnownList(this));
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		_fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		_castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());
		_hall = getConquerableHall();
		if(_fort == null && _castle == null && _hall == null)
		{
			_log.log(Level.WARN, "L2DefenderInstance spawned outside of Fortress, Castle or Siegable hall Zone! NpcId: " + getNpcId() + " x=" + getX() + " y=" + getY() + " z=" + getZ());
		}
	}

	/**
	 * This method forces guard to return to home location previously set
	 */
	@Override
	public void returnHome()
	{
		if(getWalkSpeed() <= 0)
		{
			return;
		}
		if(getSpawn() == null) // just in case
		{
			return;
		}
		if(!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 40, false))
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();

			if(hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLoc());
			}
		}
	}

	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR><BR>
	 *
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by all except defenders
		if(!(attacker instanceof L2Playable))
		{
			return false;
		}

		L2PcInstance player = attacker.getActingPlayer();

		// Check if siege is in progress
		if(_fort != null && _fort.getZone().isSiegeActive() || _castle != null && _castle.getZone().isSiegeActive() || _hall != null && _hall.getSiegeZone().isSiegeActive())
		{
			int activeSiegeId = _fort != null ? _fort.getFortId() : _castle != null ? _castle.getCastleId() : 0;

			// Check if player is an enemy of this defender npc
			if(player != null && (player.getSiegeSide() == PlayerSiegeSide.DEFENDER && !player.isRegisteredOnThisSiegeField(activeSiegeId) || player.getSiegeSide() == PlayerSiegeSide.ATTACKER || player.getSiegeSide() == PlayerSiegeSide.NONE))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need
	 * extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if(!canTarget(player))
		{
			player.sendActionFailed();
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if(!equals(player.getTarget()))
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "new target selected:" + getObjectId());
			}

			// Send a ServerMode->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			// Send a ServerMode->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);

			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a ServerMode->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if(interact)
		{
			if(isAutoAttackable(player) && !isAlikeDead())
			{
				if(Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if(!isAutoAttackable(player))
			{
				if(!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		//Send a ServerMode->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}
}