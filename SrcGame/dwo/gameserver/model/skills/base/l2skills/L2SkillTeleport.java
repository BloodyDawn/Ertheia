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
package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.log4j.Level;

public class L2SkillTeleport extends L2Skill
{
	private final String _recallType;
	private final Location _loc;

	public L2SkillTeleport(StatsSet set)
	{
		super(set);

		_recallType = set.getString("recallType", "");
		String coords = set.getString("teleCoords", null);
		if(coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_loc = new Location(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]), Integer.parseInt(valuesSplit[2]));
		}
		else
		{
			_loc = null;
		}
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if(activeChar instanceof L2PcInstance)
		{
			if(!EventManager.onEscapeUse((L2PcInstance) activeChar))
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isAfraid())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(((L2PcInstance) activeChar).isCombatFlagEquipped())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(((L2PcInstance) activeChar).getOlympiadController().isParticipating())
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return;
			}

			if(GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return;
			}
		}

		try
		{
			for(L2Character target : (L2Character[]) targets)
			{
				if(target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;

					// Check to see if player is in jail
					if(targetChar.isInJail())
					{
						targetChar.sendMessage("You can not escape from jail.");
						continue;
					}

					// Check to see if player is in a duel
					if(targetChar.isInDuel())
					{
						targetChar.sendMessage("You cannot use escape skills during a duel.");
						continue;
					}

					if(!targetChar.equals(activeChar))
					{
						if(targetChar.getOlympiadController().isParticipating())
						{
							continue;
						}

						if(GrandBossManager.getInstance().getZone(targetChar) != null)
						{
							continue;
						}

						if(targetChar.isCombatFlagEquipped())
						{
							continue;
						}
					}
				}
				Location loc = null;
				if(getSkillType() == L2SkillType.TELEPORT)
				{
					if(_loc != null)
					{
						// target is not player OR player is not flying or flymounted
						// TODO: add check for gracia continent coords
						if(!(target instanceof L2PcInstance) || !(target.isFlying() || ((L2PcInstance) target).isFlyingMounted()))
						{
							loc = _loc;
						}
					}
				}
				else
				{
					switch(_recallType)
					{
						case "Castle":
							loc = MapRegionManager.getInstance().getTeleToLocation(target, TeleportWhereType.CASTLE);
							break;
						case "ClanHall":
							loc = MapRegionManager.getInstance().getTeleToLocation(target, TeleportWhereType.CLANHALL);
							break;
						case "Fortress":
							loc = MapRegionManager.getInstance().getTeleToLocation(target, TeleportWhereType.FORTRESS);
							break;
						default:
							loc = MapRegionManager.getInstance().getTeleToLocation(target, TeleportWhereType.TOWN);
					}
				}
				if(loc != null)
				{
					target.getInstanceController().setInstanceId(0);
					target.teleToLocation(loc, true);
				}
			}
			activeChar.spsUncharge(this);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}
}