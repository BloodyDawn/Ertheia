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
package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

import java.util.Calendar;

/**
 * @author GKR
 */
public class HB_RankuFloor extends Quest
{
	private static final String qn = "HB_RankuFloor";
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	// NPCs
	private static final int GK_9 = 32752;
	private static final int CUBE = 32374;
	private static final int RANKU = 25542;
	private static final int SEAL_BREAKER_10 = 15516;
	// Телепорты
	private static final Location ENTRY_POINT = new Location(-19008, 277024, -15000);
	private static final Location EXIT_POINT = new Location(-19008, 277122, -13376);

	public HB_RankuFloor()
	{

		addStartNpc(GK_9, CUBE);
		addTalkId(GK_9, CUBE);
		addKillId(RANKU);
	}

	public static void main(String[] args)
	{
		new HB_RankuFloor();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int instanceId = npc.getInstanceId();
		if(instanceId > 0)
		{
			Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			inst.setSpawnLoc(EXIT_POINT);

			// Terminate instance in 10 min
			if(inst.getInstanceEndTime() - System.currentTimeMillis() > 600000)
			{
				inst.setDuration(600000);
			}

			inst.setEmptyDestroyTime(0);

			if(world instanceof RWorld)
			{
				setReenterTime(world);
			}

			addSpawn(CUBE, -19056, 278732, -15000, 0, false, 0, false, instanceId);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;

		if(npc.getNpcId() == GK_9)
		{
			htmltext = checkConditions(player);

			if(htmltext == null)
			{
				enterInstance(player, "HB_RankuFloor.xml");
			}
		}
		else if(npc.getNpcId() == CUBE)
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(world instanceof RWorld)
			{
				world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
				player.teleToInstance(EXIT_POINT, 0);
			}
		}

		return htmltext;
	}

	private String checkConditions(L2PcInstance player)
	{
		if(player.getParty() == null)
		{
			return "gk-noparty.htm";
		}
		if(player.getParty().getLeaderObjectId() != player.getObjectId())
		{
			return "gk-noleader.htm";
		}

		return null;
	}

	private boolean checkTeleport(L2PcInstance player)
	{
		L2Party party = player.getParty();

		if(party == null)
		{
			return false;
		}

		if(player.getObjectId() != party.getLeaderObjectId())
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}

		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getLevel() < 78)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			if(!Util.checkIfInRange(500, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			if(InstanceManager.getInstance().getPlayerWorld(player) != null)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			Long reenterTime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), InstanceZoneId.TOWER_OF_INFINITUM_10TH_FLOOR.getId());
			if(System.currentTimeMillis() < reenterTime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			if(partyMember.getInventory().getInventoryItemCount(SEAL_BREAKER_10, -1, false) < 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}

		return true;
	}

	private int enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if(world != null)
		{
			if(world instanceof RWorld)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
				return world.instanceId;
			}
			else
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
		}
		else
		{
			if(!checkTeleport(player))
			{
				return 0;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new RWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.TOWER_OF_INFINITUM_10TH_FLOOR.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			_log.log(Level.INFO, "Tower of Infinitum - Ranku floor started " + template + " Instance: " + instanceId + " created by player: " + player.getName());

			for(L2PcInstance partyMember : player.getParty().getMembers())
			{
				player.teleToInstance(ENTRY_POINT, instanceId);
				partyMember.destroyItemByItemId(ProcessType.QUEST, SEAL_BREAKER_10, 1, null, true);
				world.allowed.add(partyMember.getObjectId());
			}

			return instanceId;
		}
	}

	public void setReenterTime(InstanceWorld world)
	{
		if(world instanceof RWorld)
		{
			// Reenter time should be cleared every Wed and Sat at 6:30 AM, so we set next suitable
			Calendar reenter;
			Calendar now = Calendar.getInstance();
			Calendar reenterPointWed = (Calendar) now.clone();
			reenterPointWed.set(Calendar.AM_PM, Calendar.AM);
			reenterPointWed.set(Calendar.MINUTE, RESET_MIN);
			reenterPointWed.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
			reenterPointWed.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
			Calendar reenterPointSat = (Calendar) reenterPointWed.clone();
			reenterPointSat.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

			if(now.after(reenterPointSat))
			{
				reenterPointWed.add(Calendar.WEEK_OF_MONTH, 1);
				reenter = (Calendar) reenterPointWed.clone();
			}
			else
			{
				reenter = (Calendar) reenterPointSat.clone();
			}

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
			sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));
			// set instance reenter time for all allowed players
			for(int objectId : world.allowed)
			{
				L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
				if(player != null && player.isOnline())
				{
					InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
					player.sendPacket(sm);
				}
			}
		}
	}

	private class RWorld extends InstanceWorld
	{
		public RWorld()
		{
		}
	}
}
