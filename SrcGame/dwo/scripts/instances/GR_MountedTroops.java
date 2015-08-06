package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import dwo.scripts.quests._00693_DefeatingDragonkinRemnants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

import java.util.Calendar;

public class GR_MountedTroops extends Quest
{
	private static final int[] TEMPLATE_IDS = {
		InstanceZoneId.SEED_OF_DESTRUCTION_CHAMBLAINS_MOUNTED_TROOP.getId(),
		InstanceZoneId.SEED_OF_DESTRUCTION_SOLDIERS_MOUNTED_TROOP.getId(),
		InstanceZoneId.SEED_OF_DESTRUCTION_WARRIORS_MOUNTED_TROOP.getId(),
		InstanceZoneId.SEED_OF_DESTRUCTION_GREAT_WARRIORS_MOUNTED_TROOP.getId(),
	};
	// NPC's
	private static final int EDRIC = 32527;
	private static final int CONTROLLER = 18703;
	private static final int DOOR1 = 12240001;
	private static final int DOOR2 = 12240002;
	private static final int[] MOBS = {18784, 18785, 18786, 18787, 18788, 18789, 18790};

	// Teleports
	private static final Location ENTRY_POINT = new Location(-242754, 219982, -10011);
	private static final Location EXIT_POINT = new Location(-248525, 250156, 4335);

	private static final int[] OPENER_CHAMBERLAINS = {18786, -239504, 219984, -10092, 32767};
	private static final int[][] SPAWNS_CHAMBERLAINS = {
		// Temporary teleporter
		{32602, -242754, 219980, -10011, 0},
		// Controller
		{18703, -238320, 219983, -10112, 0},
		// First Room
		{18789, -240416, 220540, -10135, 50000}, {18789, -239833, 220540, -10135, 50000},
		{18789, -239836, 219420, -10135, 16000}, {18789, -240417, 219420, -10135, 16000},
		{18786, -239720, 219705, -10140, 31900}, {18786, -239720, 219996, -10140, 31900},
		{18786, -239720, 220260, -10140, 31900},
		// Second Room
		{18789, -238500, 219724, -10144, 31900}, {18789, -238500, 220265, -10144, 31900},
		{18786, -238620, 219904, -10144, 31900}, {18786, -238620, 220076, -10144, 31900},
		{18786, -238820, 219989, -10144, 31900}, {18703, -238320, 219983, -10112, 0},
	};
	private static final int[] OPENER_SOLDIERS = {18786, -239504, 219984, -10112, 32767};
	private static final int[][] SPAWNS_SOLDIERS = {
		// Temporary teleporter
		{32602, -242754, 219980, -10011, 0},
		// Controller
		{18703, -238320, 219983, -10112, 0},
		// First Room
		{18785, -239836, 220547, -10135, 49262}, {18785, -240420, 220546, -10135, 48510},
		{18785, -240419, 219423, -10135, 15463}, {18785, -239836, 219418, -10135, 15212},
		{18786, -240260, 219975, -10144, 32502}, {18786, -240060, 220070, -10144, 32578},
		{18786, -240043, 219866, -10144, 33178}, {18789, -239721, 220195, -10144, 30437},
		{18789, -239811, 219987, -10144, 32880}, {18789, -239744, 219733, -10144, 32424},
		// Second Room
		{18786, -238855, 220154, -10144, 49075}, {18786, -238604, 220168, -10144, 49952},
		{18786, -238864, 219828, -10144, 16383}, {18786, -238605, 219799, -10144, 16800},
		{18789, -238684, 219546, -10140, 16304}, {18789, -238688, 220428, -10139, 48609},
		{18789, -238580, 219979, -10144, 32767},
	};
	private static final int[] OPENER_WARRIORS = {18785, -239504, 219984, -10112, 32767};
	private static final int[][] SPAWNS_WARRIORS = {
		// Temporary teleporter
		{32602, -242754, 219980, -10011, 0},
		// Controller
		{18703, -238320, 219983, -10112, 0},
		// First Room
		{18785, -240369, 219992, -10144, 32088}, {18786, -239965, 219754, -10144, 32263},
		{18786, -239985, 219989, -10144, 32581}, {18786, -239985, 220179, -10144, 33365},
		{18790, -239833, 219417, -10135, 18038}, {18790, -239836, 220550, -10135, 48548},
		{18784, -239639, 220414, -10147, 41703}, {18784, -239620, 219518, -10144, 22517},
		{18784, -239823, 219985, -10144, 32510}, {18783, -239685, 219980, -10144, 31967},
		// Second Room
		{18784, -238694, 219547, -10140, 16077}, {18784, -238541, 219967, -10142, 35323},
		{18784, -238699, 220401, -10143, 48747}, {18788, -238945, 220541, -10144, 64856},
		{18788, -238712, 220008, -10144, 32944}, {18788, -238960, 219413, -10144, 65304},
		{18786, -238504, 219717, -10144, 32767}, {18786, -238491, 220243, -10144, 33074},
	};
	private static final int[] OPENER_GREAT_WARRIORS = {18783, -239504, 219984, -10112, 32767};
	private static final int[][] SPAWNS_GREAT_WARRIORS = {
		// Temporary teleporter
		{32602, -242754, 219980, -10011, 0},
		// Controller
		{18703, -238320, 219983, -10112, 0},
		// First Room
		{18783, -239834, 220544, -10135, 49151}, {18783, -239617, 219989, -10144, 32523},
		{18783, -239834, 219428, -10135, 15910}, {18784, -240417, 219419, -10135, 15640},
		{18784, -240418, 220540, -10135, 48714}, {18787, -240130, 220722, -10144, 48319},
		{18787, -240120, 219208, -10144, 15640}, {18790, -239625, 219665, -10144, 32347},
		{18790, -239611, 220311, -10144, 32456},
		// Second Room
		{18784, -238545, 219906, -10144, 30049}, {18784, -238532, 220099, -10144, 30429},
		{18787, -238687, 220537, -10135, 48457}, {18787, -238683, 219426, -10135, 15551},
		{18790, -238686, 219172, -10144, 15427}, {18790, -238685, 220797, -10144, 48775},
		{18785, -238522, 219527, -10144, 16204}, {18785, -238884, 219515, -10144, 15369},
		{18785, -238884, 219986, -10144, 32871},
	};

	public GR_MountedTroops()
	{

		addStartNpc(EDRIC);
		addTalkId(EDRIC);
		addKillId(CONTROLLER);
		addKillId(MOBS);
	}

	private static boolean checkConditions(L2PcInstance player)
	{
		if(player.isGM())
		{
			return true;
		}

		L2Party party = player.getParty();
		// player must be in party
		if(party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		// ...and be party leader
		if(!party.getLeader().equals(player))
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			if(!partyMember.isInsideRadius(player, 1000, true, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return false;
			}
			if(partyMember.getLevel() < 75)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return false;
			}
			// Instances 123-126 are in one group. If player visit one, cannot visit other
			// So we must check all IDs
			for(int inst : TEMPLATE_IDS)
			{
				Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), inst);
				if(System.currentTimeMillis() < reentertime)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
					sm.addPcName(partyMember);
					party.broadcastPacket(sm);
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args)
	{
		new GR_MountedTroops();
	}

	private void enterInstance(L2PcInstance player, int templateId)
	{
		synchronized(this)
		{
			// Check for existing instances for this player
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			// Player already in the instance
			if(world != null)
			{
				// but not in our instance
				if(!(world instanceof MTCworld))
				{
					player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
					return;
				}
				// Check if instance still exist, if yes - teleport player
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				if(inst != null)
				{
					player.teleToInstance(ENTRY_POINT, world.instanceId);
				}
			}
			else
			{
				// Check
				if(!checkConditions(player))
				{
					return;
				}

				int instanceId = InstanceManager.getInstance().createDynamicInstance("GR_MountedTroops.xml");

				world = new MTCworld();
				world.instanceId = instanceId;
				world.templateId = templateId;
				world.status = 0;

				InstanceManager.getInstance().addWorld(world);

				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				inst.setDuration(1200000);
				inst.setEmptyDestroyTime(300000);
				inst.setAllowSummon(false);
				inst.setSpawnLoc(EXIT_POINT);

				startWorld((MTCworld) world, templateId);

				// And finally teleport party into instance
				L2Party party = player.getParty();
				if(party == null || player.isGM())
				{
					// Remove buffs, set reenter
					setupPlayer((MTCworld) world, player, templateId);
					// Port player
					player.teleToInstance(ENTRY_POINT, instanceId);
					return;
				}
				for(L2PcInstance partyMember : party.getMembers())
				{
					// Remove buffs, set reenter
					setupPlayer((MTCworld) world, partyMember, templateId);
					// Port player
					partyMember.teleToInstance(ENTRY_POINT, instanceId);
				}
				_log.log(Level.INFO, "GR_MountedTroops[" + templateId + "] instance started: " + instanceId + " created by player: " + player.getName());
			}
		}
	}

	private void startWorld(MTCworld world, int template_id)
	{
		world.startTime = System.currentTimeMillis();

		switch(template_id)
		{
			case 123:
				world.opener = addSpawn(OPENER_CHAMBERLAINS[0], OPENER_CHAMBERLAINS[1], OPENER_CHAMBERLAINS[2], OPENER_CHAMBERLAINS[3], OPENER_CHAMBERLAINS[4], false, 0, false, world.instanceId);
				spawnMobs(world, SPAWNS_CHAMBERLAINS);
				break;
			case 124:
				world.opener = addSpawn(OPENER_SOLDIERS[0], OPENER_SOLDIERS[1], OPENER_SOLDIERS[2], OPENER_SOLDIERS[3], OPENER_SOLDIERS[4], false, 0, false, world.instanceId);
				spawnMobs(world, SPAWNS_SOLDIERS);
				break;
			case 125:
				world.opener = addSpawn(OPENER_WARRIORS[0], OPENER_WARRIORS[1], OPENER_WARRIORS[2], OPENER_WARRIORS[3], OPENER_WARRIORS[4], false, 0, false, world.instanceId);
				spawnMobs(world, SPAWNS_WARRIORS);
				break;
			case 126:
				world.opener = addSpawn(OPENER_GREAT_WARRIORS[0], OPENER_GREAT_WARRIORS[1], OPENER_GREAT_WARRIORS[2], OPENER_GREAT_WARRIORS[3], OPENER_GREAT_WARRIORS[4], false, 0, false, world.instanceId);
				spawnMobs(world, SPAWNS_GREAT_WARRIORS);
				break;
			default:
				_log.log(Level.WARN, "GR_MountedTroops: Unable to determine template_id, closing instance.");
				InstanceManager.getInstance().destroyInstance(world.instanceId);
				break;
		}
	}

	private void spawnMobs(MTCworld world, int[][] spawns)
	{
		for(int[] SP : spawns)
		{
			addSpawn(SP[0], SP[1], SP[2], SP[3], SP[4], false, 0, false, world.instanceId);
		}
	}

	private void setupPlayer(MTCworld world, L2PcInstance player, int template_id)
	{
		// Remove buffs from player
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		// Set reenter to 6:30
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, 30);
		if(reenter.get(Calendar.HOUR_OF_DAY) >= 6)
		{
			reenter.add(Calendar.DATE, 1);
		}
		reenter.set(Calendar.HOUR_OF_DAY, 6);
		// Limit enter
		InstanceManager.getInstance().setInstanceTime(player.getObjectId(), template_id, reenter.getTimeInMillis());
		// Add player to allowed list
		world.allowed.add(player.getObjectId());
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			newQuestState(player);
		}

		if(npc.getNpcId() == EDRIC && Util.isDigit(event) && ArrayUtils.contains(TEMPLATE_IDS, Integer.valueOf(event)))
		{
			try
			{
				enterInstance(player, Integer.valueOf(event));
			}
			catch(Exception e)
			{
				_log.log(Level.WARN, "", e);
			}
			return null;
		}

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		if(player.getInstanceId() > 0 && player.getInstanceId() == npc.getInstanceId())
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
			if(!(tmpworld instanceof MTCworld))
			{
				return "";
			}

			MTCworld wrld = (MTCworld) tmpworld;

			if(npc.equals(wrld.opener))
			{
				Instance inst = InstanceManager.getInstance().getInstance(tmpworld.instanceId);
				if(inst != null)
				{
					inst.getDoor(DOOR1).openMe();
					inst.getDoor(DOOR2).openMe();
				}
			}
			else if(npc.getNpcId() == CONTROLLER)
			{
				Instance inst = InstanceManager.getInstance().getInstance(tmpworld.instanceId);
				if(inst != null)
				{
					inst.setDuration(300000);
				}
				// Set time difference only for players inside instance
				long timeDiff = (System.currentTimeMillis() - wrld.startTime) / 60000L;
				for(int playerId : wrld.allowed)
				{
					L2PcInstance pl = WorldManager.getInstance().getPlayer(playerId);
					if(pl != null && pl.isOnline() && pl.getInstanceId() == wrld.instanceId)
					{
						QuestState qst = pl.getQuestState(_00693_DefeatingDragonkinRemnants.class);
						if(qst != null)
						{
							qst.set("timeDiff", String.valueOf(timeDiff));
						}
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	private class MTCworld extends InstanceWorld
	{
		private L2Npc opener;
		private long startTime;

		public MTCworld()
		{
		}
	}
}
