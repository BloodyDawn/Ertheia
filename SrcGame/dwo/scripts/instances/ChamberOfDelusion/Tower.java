package dwo.scripts.instances.ChamberOfDelusion;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.EarthQuake;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.List;

public class Tower extends Quest
{
	private static final String qn = "Tower";
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	//NPCs
	private static final int ENTRANCE_GATEKEEPER = 32663;
	private static final int ROOM_GATEKEEPER_FIRST = 32693;
	private static final int ROOM_GATEKEEPER_LAST = 32701;
	private static final int AENKINEL = 25695;
	private static final int BOX = 18823;
	//Items
	private static final int ENRIA = 4042;
	private static final int ASOFE = 4043;
	private static final int THONS = 4044;
	private static final int LEONARD = 9628;
	private static final int DELUSION_MARK = 15311;
	//Skills
	private static final SkillHolder SUCCESS_SKILL = new SkillHolder(5758, 1);
	private static final SkillHolder FAIL_SKILL = new SkillHolder(5376, 4);
	private static final int[][] ROOM_ENTER_POINTS = {
		{-108976, -153372, -6688}, {-108960, -152524, -6688}, {-107088, -155052, -6688}, {-107104, -154236, -6688},
		{-108048, -151244, -6688}, {-107088, -152956, -6688}, {-108992, -154604, -6688}, {-108032, -152892, -6688},
		{-108048, -154572, -6688} //Raid room
	};
	private static final int[] AENKINEL_SPAWN = {-108714, -154994, -6688, 0};
	private static final int[] RETURN_POINT = {-114592, -152509, -6723};
	private static final long ROOM_CHANGE_INTERVAL = 480000; //8 min
	private static final int ROOM_CHANGE_RANDOM_TIME = 120; //2 min
	//Managers spawn coordinates (npcId, x, y, z, heading)
	private int[][] MANAGER_SPAWN_POINTS = {
		{32693, -108976, -153472, -6688, 0}, {32694, -108960, -152624, -6688, 0}, {32695, -107088, -155152, -6688, 0},
		{32696, -107104, -154336, -6688, 0}, {32697, -108048, -151344, -6688, 0}, {32698, -107088, -153056, -6688, 0},
		{32699, -108992, -154704, -6688, 0}, {32700, -108032, -152992, -6688, 0}, {32701, -108048, -154672, -6688, 0}

	};
	private int[][] BOX_SPAWN_POINTS = {
		{-108714, -154994, -6688, 0}, {-107894, -154923, -6688, 0}, {-107894, -154322, -6688, 0},
		{-108232, -154616, -6688, 0}
	};

	public Tower()
	{

		addStartNpc(ENTRANCE_GATEKEEPER);
		addTalkId(ENTRANCE_GATEKEEPER);
		for(int i = ROOM_GATEKEEPER_FIRST; i <= ROOM_GATEKEEPER_LAST; i++)
		{
			addStartNpc(i);
			addTalkId(i);
		}
		addKillId(AENKINEL);
		addAttackId(BOX);
		addSpellFinishedId(BOX);
	}

	public static void main(String[] args)
	{
		new Tower();
	}

	private boolean checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if(party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}

		if(!party.getLeader().equals(player))
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}

		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getLevel() < 80)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			if(!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), InstanceZoneId.DELUSION_CHAMBER_TOWER_OF_SEAL.getId());

			if(System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}

		return true;
	}

	private void teleportplayer(L2PcInstance player, TeleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
	}

	public void smallpenalty(InstanceWorld world)
	{
		if(world instanceof CDWorld)
		{
			Calendar reenter = Calendar.getInstance();
			reenter.add(Calendar.MINUTE, RESET_MIN);
			reenter.add(Calendar.HOUR_OF_DAY, RESET_HOUR);
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

	private void banishStrangers(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		L2Npc manager = world.managers[world.currentRoom - 1];
		TeleCoord tele = new TeleCoord();

		tele.x = RETURN_POINT[0];
		tele.y = RETURN_POINT[1];
		tele.z = RETURN_POINT[2];

		manager.getKnownList().getKnownPlayersInRadius(1000).stream().filter(player -> party == null || player.getParty() == null || !player.getParty().equals(world.getPartyInside())).forEach(player -> {
			world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
			exitInstance(player);
		});

		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

		//Schedule next banish task only if remaining time is enough
		if(inst.getInstanceEndTime() - System.currentTimeMillis() > 60000)
		{
			startQuestTimer("banish_strangers", 60000, world.managers[world.currentRoom - 1], null);
		}
	}

	private void changeRoom(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		if(party == null)
		{
			return;
		}

		//Change room from raid room is prohibited for Square and Tower
		if(world.currentRoom == ROOM_ENTER_POINTS.length)
		{
			return;
		}

		int newRoom = world.currentRoom;
		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

		if(world.currentRoom != ROOM_ENTER_POINTS.length && inst.getInstanceEndTime() - System.currentTimeMillis() < 600000) //teleport to raid room 10 min or lesser before instance end time
		{
			newRoom = ROOM_ENTER_POINTS.length;
		}
		else
		{
			while(newRoom == world.currentRoom) //otherwise teleport to another room, except current
			{
				newRoom = Rnd.get(ROOM_ENTER_POINTS.length - 1) + 1;
			}
		}

		cancelQuestTimer("banish_strangers", world.managers[world.currentRoom - 1], null);
		banishStrangers(world);

		for(L2PcInstance partyMember : party.getMembers())
		{
			if(world.instanceId == partyMember.getInstanceId())
			{
				partyMember.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				partyMember.teleToLocation(ROOM_ENTER_POINTS[newRoom - 1][0] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][1] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][2]);
			}
		}

		long nextInterval = ROOM_CHANGE_INTERVAL + Rnd.get(ROOM_CHANGE_RANDOM_TIME) * 1000;

		//Schedule next room change only if remaining time is enough and here is no raid room
		if(inst.getInstanceEndTime() - System.currentTimeMillis() > nextInterval && newRoom != ROOM_ENTER_POINTS.length)
		{
			startQuestTimer("prepare_change_room", nextInterval, world.managers[newRoom - 1], null);
		}
		else if(newRoom == ROOM_ENTER_POINTS.length)
		{
			inst.setDuration((int) (inst.getInstanceEndTime() - System.currentTimeMillis() + 1200000)); //Add 20 min to instance time if raid room is reached
			//world.managers[newRoom - 1].broadcastPacket(new NpcSay(world.managers[newRoom - 1].getObjectId(), Say2.ALL, world.managers[newRoom - 1].getNpcId(), 1080881)); TODO: Retail message
		}

		if(inst.getInstanceEndTime() - System.currentTimeMillis() > 60000)
		{
			startQuestTimer("banish_strangers", 60000, world.managers[newRoom - 1], null);
		}

		world.currentRoom = newRoom;
	}

	private void enter(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		if(party == null)
		{
			return;
		}

		int newRoom = Rnd.get(ROOM_ENTER_POINTS.length - 1) + 1;

		for(L2PcInstance partyMember : party.getMembers())
		{
			QuestState st = partyMember.getQuestState(qn);
			if(st == null)
			{
				st = newQuestState(partyMember);
			}

			if(st.hasQuestItems(DELUSION_MARK))
			{
				st.takeItems(DELUSION_MARK, -1);
			}

			if(partyMember.getObjectId() == party.getLeaderObjectId())
			{
				st.giveItems(DELUSION_MARK, 1);
			}

			//Save location for teleport back into main hall
			st.set("return_point", Integer.toString(partyMember.getX()) + ';' + Integer.toString(partyMember.getY()) + ';' + Integer.toString(partyMember.getZ()));

			partyMember.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			partyMember.getInstanceController().setInstanceId(world.instanceId);
			world.allowed.add(partyMember.getObjectId());
			partyMember.teleToLocation(ROOM_ENTER_POINTS[newRoom - 1][0] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][1] - 50 + Rnd.get(100), ROOM_ENTER_POINTS[newRoom - 1][2]);
		}

		startQuestTimer("prepare_change_room", ROOM_CHANGE_INTERVAL + Rnd.get(ROOM_CHANGE_RANDOM_TIME - 10) * 1000, world.managers[newRoom - 1], null); //schedule room change
		startQuestTimer("banish_strangers", 60000, world.managers[newRoom - 1], null); //schedule checkup for player without party or another party

		world.currentRoom = newRoom;
	}

	private void earthQuake(CDWorld world)
	{
		L2Party party = world.getPartyInside();

		if(party == null)
		{
			return;
		}

		party.getMembers().stream().filter(partyMember -> world.instanceId == partyMember.getInstanceId()).forEach(partyMember -> partyMember.sendPacket(new EarthQuake(partyMember.getX(), partyMember.getY(), partyMember.getZ(), 20, 10)));

		startQuestTimer("change_room", 5000, world.managers[world.currentRoom - 1], null);
	}

	protected void spawnState(CDWorld world)
	{
		addSpawn(AENKINEL, AENKINEL_SPAWN[0], AENKINEL_SPAWN[1], AENKINEL_SPAWN[2], AENKINEL_SPAWN[3], false, 0, false, world.instanceId);

		for(int i = 0; i < MANAGER_SPAWN_POINTS.length; i++)
		{
			world.managers[i] = addSpawn(MANAGER_SPAWN_POINTS[i][0], MANAGER_SPAWN_POINTS[i][1], MANAGER_SPAWN_POINTS[i][2], MANAGER_SPAWN_POINTS[i][3], MANAGER_SPAWN_POINTS[i][4], false, 0, false, world.instanceId);
		}
	}

	protected int enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if(world != null)
		{
			if(!(world instanceof CDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			CDWorld currentWorld = (CDWorld) world;
			TeleCoord tele = new TeleCoord();
			tele.x = ROOM_ENTER_POINTS[currentWorld.currentRoom - 1][0];
			tele.y = ROOM_ENTER_POINTS[currentWorld.currentRoom - 1][1];
			tele.z = ROOM_ENTER_POINTS[currentWorld.currentRoom - 1][2];
			tele.instanceId = world.instanceId;
			teleportplayer(player, tele);
			return instanceId;
		}
		//New instance
		else
		{
			if(!checkConditions(player))
			{
				return 0;
			}
			L2Party party = player.getParty();
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new CDWorld(party);
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.DELUSION_CHAMBER_TOWER_OF_SEAL.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			_log.log(Level.INFO, "Chamber Of Delusion started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			spawnState((CDWorld) world);
			smallpenalty(world);    //Set reenter restriction
			enter((CDWorld) world);
			return instanceId;
		}
	}

	private void exitInstance(L2PcInstance player)
	{
		int x = RETURN_POINT[0];
		int y = RETURN_POINT[1];
		int z = RETURN_POINT[2];

		QuestState st = player.getQuestState(qn);

		if(st != null)
		{
			String return_point = st.get("return_point");
			if(return_point != null)
			{
				String[] coords = return_point.split(";");
				if(coords.length == 3)
				{
					try
					{
						x = Integer.parseInt(coords[0]);
						y = Integer.parseInt(coords[1]);
						z = Integer.parseInt(coords[2]);
					}
					catch(Exception e)
					{
						x = RETURN_POINT[0];
						y = RETURN_POINT[1];
						z = RETURN_POINT[2];
					}
				}
			}
		}

		player.getInstanceController().setInstanceId(0);
		player.teleToLocation(x, y, z);
		if(!player.getPets().isEmpty())
		{
			for(L2Summon pet : player.getPets())
			{
				pet.getInstanceController().setInstanceId(0);
				pet.teleToLocation(x, y, z);
			}
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(attacker);
		if(tmpworld instanceof CDWorld)
		{
			CDWorld world = (CDWorld) tmpworld;

			if(npc.getNpcId() == BOX && !world.rewardedBoxes.contains(npc.getObjectId()) && npc.getCurrentHp() < npc.getMaxHp() / 10)
			{
				L2MonsterInstance box = (L2MonsterInstance) npc;
				ItemHolder item;
				if(Rnd.get(100) < 25) //25% chance to reward
				{
					world.rewardedBoxes.add(box.getObjectId());
					if(Rnd.get(100) < 33)
					{
						item = new ItemHolder(ENRIA, (int) (3 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}
					if(Rnd.get(100) < 50)
					{
						item = new ItemHolder(THONS, (int) (4 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}
					if(Rnd.get(100) < 50)
					{
						item = new ItemHolder(ASOFE, (int) (4 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}
					if(Rnd.get(100) < 16)
					{
						item = new ItemHolder(LEONARD, (int) (2 * Config.RATE_DROP_ITEMS));
						box.dropItem(attacker, item);
					}

					box.doCast(SUCCESS_SKILL.getSkill());
				}
				else
				{
					box.doCast(FAIL_SKILL.getSkill());
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if(tmpworld != null && tmpworld instanceof CDWorld && npc.getNpcId() >= ROOM_GATEKEEPER_FIRST && npc.getNpcId() <= ROOM_GATEKEEPER_LAST)
		{
			CDWorld world = (CDWorld) tmpworld;

			switch(event)
			{
				case "prepare_change_room":
					earthQuake(world);
					break;
				case "change_room":
					changeRoom(world);
					break;
				case "banish_strangers":
					banishStrangers(world);
					break;
			}

			//Timers part ends here, further player cannot be null
			if(player == null)
			{
				return null;
			}

			QuestState st = player.getQuestState(qn);

			if(st == null)
			{
				st = newQuestState(player);
			}

			//Change room from dialog
			switch(event)
			{
				case "next_room":
					if(player.getParty() == null)
					{
						htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_party.htm");
					}
					else if(player.getParty().getLeaderObjectId() != player.getObjectId())
					{
						htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_leader.htm");
					}
					else if(st.hasQuestItems(DELUSION_MARK))
					{
						st.takeItems(DELUSION_MARK, 1);
						cancelQuestTimer("prepare_change_room", npc, null);
						cancelQuestTimer("change_room", npc, null);
						changeRoom(world);
					}
					else
					{
						htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_item.htm");
					}
					break;
				case "go_out":
					if(player.getParty() == null)
					{
						htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_party.htm");
					}
					else if(player.getParty().getLeaderObjectId() != player.getObjectId())
					{
						htmltext = HtmCache.getInstance().getHtmQuest(player.getLang(), "instances/ChambersOfDelusion/no_leader.htm");
					}
					else
					{
						Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

						cancelQuestTimer("prepare_change_room", npc, null);
						cancelQuestTimer("change_room", npc, null);
						cancelQuestTimer("banish_strangers", npc, null);

						for(L2PcInstance partyMember : player.getParty().getMembers())
						{
							exitInstance(partyMember);
							world.allowed.remove(world.allowed.indexOf(partyMember.getObjectId()));
						}

						inst.setEmptyDestroyTime(0);
					}
					break;
				case "look_party":
					if(player.getParty() != null && player.getParty().equals(world.getPartyInside()))
					{
						player.teleToLocation(ROOM_ENTER_POINTS[world.currentRoom - 1][0], ROOM_ENTER_POINTS[world.currentRoom - 1][1], ROOM_ENTER_POINTS[world.currentRoom - 1][2]);
					}
					break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(tmpworld instanceof CDWorld)
		{
			if(npc.getNpcId() == AENKINEL)
			{
				CDWorld world = (CDWorld) tmpworld;
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

				if(inst.getInstanceEndTime() - System.currentTimeMillis() > 60000)
				{
					cancelQuestTimer("prepare_change_room", world.managers[MANAGER_SPAWN_POINTS.length - 1], null);
					cancelQuestTimer("change_room", world.managers[MANAGER_SPAWN_POINTS.length - 1], null);
					startQuestTimer("change_room", 60000, world.managers[MANAGER_SPAWN_POINTS.length - 1], null);
				}

				for(int[] BOX_SPAWN_POINT : BOX_SPAWN_POINTS)
				{
					L2MonsterInstance box = (L2MonsterInstance) addSpawn(BOX, BOX_SPAWN_POINT[0], BOX_SPAWN_POINT[1], BOX_SPAWN_POINT[2], BOX_SPAWN_POINT[3], false, 0, false, world.instanceId);
					box.setIsNoRndWalk(true);
				}
			}
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);

		if(st == null)
		{
			st = newQuestState(player);
		}

		if(npcId == ENTRANCE_GATEKEEPER)
		{
			enterInstance(player, "COD_Tower.xml");
		}

		return "";
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc.getNpcId() == BOX && skill.getId() == 5376 || skill.getId() == 5758 && !npc.isDead())
		{
			npc.doDie(player);
		}

		return super.onSpellFinished(npc, player, skill);
	}

	private class CDWorld extends InstanceWorld
	{
		protected L2Npc[] managers = new L2Npc[9];

		protected int currentRoom;
		protected List<Integer> rewardedBoxes;
		private L2Party partyInside;

		public CDWorld(L2Party party)
		{
			partyInside = party;
			rewardedBoxes = new FastList<>();
		}

		protected L2Party getPartyInside()
		{
			return partyInside;
		}
	}

	private class TeleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
}