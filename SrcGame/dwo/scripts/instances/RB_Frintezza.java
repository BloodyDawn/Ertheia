package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DecoyInstance;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneForm;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2EffectZone;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SpecialCamera;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.List;

import static dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;

public class RB_Frintezza extends Quest
{
	// NPCs
	//Bosses
	private static final int FRINTEZZA = 29045;
	private static final int HALISHA2 = 29046;
	private static final int HALISHA3 = 29047;
	//Picture mobs
	private static final int GHOST1 = 29050;
	private static final int GHOST2 = 29051;
	//Telepoters
	private static final int TOMB_TELE = 32011;
	private static final int CUBE = 29061;
	//Alarm
	private static final int ALARM = 18328;
	//Dark Choir mobs
	private static final int CAPTAIN = 18334;
	//Hall Keeper mobs
	private static final int[] KEEPER_MOBS = {18329, 18330, 18331, 18333};
	private static List<L2PcInstance> _PlayersInside = new FastList<>();
	private static int _lairZone = 11998;
	private static int _intervalSong = 35000;
	// Wednesday 6:30AM
	private Calendar reuse_date_1;
	// Saturday 6:30AM
	private Calendar reuse_date_2;
	private long nextCalendarReschedule;

	public RB_Frintezza()
	{
		addTeleportRequestId(CUBE);
		addKillId(HALISHA2, HALISHA3, FRINTEZZA, GHOST1, GHOST2, ALARM, CAPTAIN);
		addAttackId(HALISHA2, HALISHA3, FRINTEZZA);
		addSpellFinishedId(FRINTEZZA);
		addAskId(TOMB_TELE, 654);
		addEnterZoneId(_lairZone);
		addExitZoneId(_lairZone);
	}

	public static void main(String[] args)
	{
		new RB_Frintezza();
	}

	private boolean checkConditions(L2PcInstance player)
	{
		if(player.getParty() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		L2Party party = player.getParty();
		if(!party.isInCommandChannel())
		{
			party.broadcastPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		if(party.getCommandChannel().getMembers().size() < 28)
		{
			party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(28));
			return false;
		}
		if(party.getCommandChannel().getMembers().size() > 42 || party.getCommandChannel().getPartyCount() > 6)
		{
			party.getCommandChannel().broadcastMessage(SystemMessageId.CANNOT_ENTER_MAX_ENTRANTS);
			return false;
		}
		if(!party.getCommandChannel().getLeader().equals(player))
		{
			party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_FOR_ALLIANCE_CHANNEL_LEADER);
			return false;
		}
		if(party.getCommandChannel().getLeader().getInventory().getItemByItemId(8073) == null)
		{
			party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT).addPcName(party.getCommandChannel().getLeader()));
			return false;
		}
		for(L2PcInstance member : party.getCommandChannel().getMembers())
		{
			if(member == null || member.getLevel() < 80)
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
				return false;
			}
			if(!Util.checkIfInRange(1000, player, member, true))
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
				return false;
			}
			long reentertime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), InstanceZoneId.LAST_IMPERIAL_TOMB.getId());
			if(System.currentTimeMillis() < reentertime)
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
				return false;
			}
			_PlayersInside.add(member);
		}
		return true;
	}

	private void teleportplayer(L2PcInstance player, Location teleto, FWorld world, boolean random)
	{
		player.getInstanceController().setInstanceId(teleto.getId());
		if(random)
		{
			player.teleToLocation(teleto.getX() + Rnd.get(-250, 250), teleto.getY() + Rnd.get(-250, 250), teleto.getZ());
		}
		else
		{
			player.teleToLocation(teleto.getX(), teleto.getY(), teleto.getZ());
		}
		if(!player.getPets().isEmpty())
		{
			for(L2Summon pet : player.getPets())
			{
				pet.getInstanceController().setInstanceId(0);
				pet.teleToLocation(teleto.getX(), teleto.getY(), teleto.getZ());
			}
		}
		world.PlayersInInstance.add(player);
	}

	protected int enterInstance(L2PcInstance player, String template, Location teleto1, Location teleto2)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		int inst = checkWorld(player);
		if(inst == 0)
		{
			player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
			return 0;
		}
		else if(inst == 1)
		{
			teleto1.setId(world.instanceId);
			teleto2.setId(world.instanceId);
			teleportplayer(player, teleto2, (FWorld) world, true);
			return world.instanceId;
		}
		else
		{
			if(!checkConditions(player))
			{
				return 0;
			}

			L2Party party = player.getParty();
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new FWorld(System.currentTimeMillis() + 7200000);
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.LAST_IMPERIAL_TOMB.getId();
			InstanceManager.getInstance().addWorld(world);
			// teleport players
			teleto1.setId(instanceId);
			teleto2.setId(instanceId);
			if(party == null)
			{
				// this can happen only if debug is true
				teleportplayer(player, teleto2, (FWorld) world, true);
				world.allowed.add(player.getObjectId());
				player.destroyItemByItemId(ProcessType.QUEST, 8073, 1, player, true);
			}
			else
			{
				List<L2PcInstance> channelMembers = party.getCommandChannel().getMembers();
				for(L2PcInstance group2 : channelMembers)
				{
					teleportplayer(group2, teleto2, (FWorld) world, true);
					world.allowed.add(group2.getObjectId());
				}

				party.getCommandChannel().getLeader().destroyItemByItemId(ProcessType.QUEST, 8073, 1, party.getCommandChannel().getLeader(), true);
			}
			return instanceId;
		}
	}

	protected void exitInstance(L2PcInstance player, Location tele)
	{
		player.getInstanceController().setInstanceId(0);
		player.teleToLocation(tele);
		if(!player.getPets().isEmpty())
		{
			for(L2Summon pet : player.getPets())
			{
				pet.getInstanceController().setInstanceId(0);
				pet.teleToLocation(tele);
			}
		}
	}

	private int checkWorld(L2PcInstance player)
	{
		InstanceWorld checkworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(checkworld != null)
		{
			if(!(checkworld instanceof FWorld))
			{
				return 0;
			}
			return 1;
		}
		return 2;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			if(npcId == HALISHA2 && !world._isMorphed)
			{
				synchronized(world._morphing)
				{
					if(npc.getCurrentHp() < npc.getMaxHp() * 0.66 && !world._morphing)
					{
						world._morphing = Boolean.TRUE;
						cancelQuestTimers("attack");
						cancelQuestTimers("song");
						enterMovieMode(world.instanceId);
						startQuestTimer("FirstPolymorph_1", 1000, npc, null);
					}
				}
			}
			else if(npcId == HALISHA2 && world._isMorphed)
			{
				synchronized(world._morphing)
				{
					if(npc.getCurrentHp() < npc.getMaxHp() * 0.25 && !world._morphing)
					{
						world._morphing = Boolean.TRUE;
						cancelQuestTimers("attack");
						cancelQuestTimers("song");
						enterMovieMode(world.instanceId);
						startQuestTimer("SecondPolymorph_1", 1000, npc, attacker);
					}
				}
			}
			else if(npcId == HALISHA2 || npcId == HALISHA3)
			{
				L2Npc h = null;
				if(world._halisha3 != null && !world._halisha3.isDead())
				{
					h = world._halisha3;
				}
				else if(world._halisha2 != null && !world._halisha2.isDead())
				{
					h = world._halisha2;
				}
				getSkillAI(h);
			}
			//Si on attaque les ghost mages
			else if(npcId == GHOST1 && !npc.isCastingNow())
			{
				npc.setTarget(attacker);
				//attaque suicide
				if(Rnd.getChance(10))
				{
					npc.doCast(SkillTable.getInstance().getInfo(5011, 1));
				}
				else if(Rnd.getChance(25))
				{
					npc.doCast(SkillTable.getInstance().getInfo(5010, 1));
				}
			}
			//Si on attaque les ghost warrior
			else if(npcId == GHOST2 && !npc.isCastingNow())
			{
				npc.setTarget(attacker);
				//attaque suicide
				if(Rnd.getChance(10))
				{
					npc.doCast(SkillTable.getInstance().getInfo(5011, 1));
				}
				else if(Rnd.getChance(25))
				{
					npc.doCast(SkillTable.getInstance().getInfo(5009, 1));
				}
			}
			else if(npcId == FRINTEZZA)
			{
				L2Npc h = null;
				if(world._halisha2 != null && !world._halisha2.isDead())
				{
					h = world._halisha2;
				}
				else if(world._halisha3 != null && !world._halisha3.isDead())
				{
					h = world._halisha3;
				}
				if(h == null && !world._cinameEnRoute)
				{
					world._cinameEnRoute = true;
					startQuestTimer("cinema", 1000, npc, attacker);
					closeDoor(17130046, world.instanceId);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			L2Skill _skill;
			/*
			 * LAUNCH CINEMATICS AND SPAWNS AT THE BEGINING
			 */
			switch(event)
			{
				case "cinema":
					world._frintezza.setIsInvul(true);
					for(int i = 17130061; i <= 17130070; i++)
					{
						closeDoor(i, world.instanceId);
					}
					closeDoor(17130045, world.instanceId);
					closeDoor(17130046, world.instanceId);
					world._frintezza.disableAllSkills();
					startQuestTimer("cinema2", 1000, world._frintezza, null);
					break;
				case "cinema2":
					enterMovieMode(world.instanceId);
					world._dummy3 = createNewSpawn(29052, -87784, -153301, -9180, 56847, world.instanceId);
					world._dummy3.setIsImmobilized(true);
					specialCamera(world._dummy3, 30, -180, -20, 0, 4100, 0, 0);
					startQuestTimer("cinema3", 4000, world._frintezza, null);
					break;
				case "cinema3":
					specialCamera(world._dummy3, 800, 90, -90, 6000, 6100, 0, 0);
					startQuestTimer("cinema4", 5000, world._frintezza, null);
					break;
				case "cinema4":
					specialCamera(world._frintezza, 1500, 90, 10, 2000, 3000, 0, 0);
					world._dummy3.getLocationController().delete();
					startQuestTimer("cinema5", 2000, world._frintezza, null);
					break;
				case "cinema5":
					specialCamera(world._frintezza, 200, 90, -10, 3000, 4100, 0, 0);
					startQuestTimer("cinema6", 4000, world._frintezza, null);
					break;
				case "cinema6":
					specialCamera(world._frintezza, 100, 90, 25, 3000, 11000, 0, 0);
					startQuestTimer("cinema7", 4000, world._frintezza, null);
					break;
				case "cinema7":
					specialCamera(world._frintezza, 80, 70, -10, 0, 11000, 0, 0);
					social(world._frintezza, 1);
					world._portrait3 = createNewSpawn(29050, -89408, -153984, -9037, 64817, world.instanceId);
					world._portrait4 = createNewSpawn(29051, -89382, -152443, -9037, 57730, world.instanceId);
					world._dummy = createNewSpawn(29053, -87788, -153297, -9187, 57730, world.instanceId);
					world._dummy.setIsImmobilized(true);
					world._dummy2 = createNewSpawn(29053, -86523, -154022, -9173, 45719, world.instanceId);
					world._dummy2.setIsImmobilized(true);
					startQuestTimer("cinema8", 7000, world._frintezza, null);
					break;
				case "cinema8":
					world._portrait1 = createNewSpawn(29050, -86183, -152443, -9037, 35048, world.instanceId);
					world._portrait2 = createNewSpawn(29051, -86140, -153991, -9037, 28205, world.instanceId);
					specialCamera(world._dummy2, 250, -55, 10, 0, 6000, 0, 0);
					startQuestTimer("cinema9", 1500, world._frintezza, null);
					break;
				case "cinema9":
					social(world._portrait1, 1);
					social(world._portrait2, 1);
					startQuestTimer("cinema10", 4000, world._frintezza, null);
					break;
				case "cinema10":
					specialCamera(world._frintezza, 200, 90, -10, 0, 10000, 0, 0);
					startQuestTimer("cinema11", 1000, world._frintezza, null);
					break;
				case "cinema11":
					specialCamera(world._frintezza, 100, 90, 25, 5000, 10000, 0, 0);
					social(world._frintezza, 3);
					startQuestTimer("cinema12", 5000, world._frintezza, null);
					break;
				case "cinema12":
					specialCamera(world._frintezza, 10, 170, 40, 0, 10000, 0, 0);
					world._frintezza.enableAllSkills();
					_skill = SkillTable.getInstance().getInfo(5006, 1);
					cast(world._frintezza, _skill);
					world._halisha2 = createNewSpawn(29046, -87790, -153313, -9180, 42682, world.instanceId);
					startQuestTimer("cinema13", 5000, world._frintezza, null);
					break;
				case "cinema13":
					specialCamera(world._frintezza, 1100, 160, 30, 4000, 4100, 0, 0);
					world._dummy1 = createNewSpawn(29053, -88697, -153721, -7377, 48028, world.instanceId);
					world._dummy1.setIsImmobilized(true);
					startQuestTimer("cinema14", 4000, world._frintezza, null);
					break;
				case "cinema14":
					specialCamera(world._frintezza, 1600, 120, 30, 4000, 4100, 0, 0);
					world._frintezza.setIsInvul(false);
					world._halisha2.getLocationController().delete();
					startQuestTimer("cinema15", 4000, world._frintezza, null);
					break;
				case "cinema15":
					specialCamera(world._dummy1, 2100, 155, -45, 0, 6000, 0, 0);
					L2Skill _skil = SkillTable.getInstance().getInfo(5004, 1);
					cast(world._dummy, _skil);
					world._frintezza.setIsInvul(false);
					startQuestTimer("cinema16", 5000, world._frintezza, null);
					break;
				case "cinema16":
					specialCamera(world._dummy, 500, 0, 30, 4000, 4300, 0, 0);
					world._halisha2 = createNewSpawn(29046, -87790, -153313, -9180, 42682, world.instanceId);
					startQuestTimer("cinema17", 200, world._frintezza, null);
					break;
				case "cinema17":
					social(world._halisha2, 3);
					startQuestTimer("cinema18", 4000, world._frintezza, null);
					break;
				case "cinema18":
					specialCamera(world._halisha2, 200, -90, 20, 4000, 5000, 0, 0);
					startQuestTimer("cinema19", 5000, world._frintezza, null);
					break;
				case "cinema19":
					world._dummy.getLocationController().decay();
					world._dummy.getLocationController().delete();
					world._dummy1.getLocationController().decay();
					world._dummy1.getLocationController().delete();
					world._halisha2.abortCast();
					world._halisha2.enableAllSkills();
					ThreadPoolManager.getInstance().scheduleGeneral(new SetFrintezzaMobilised(world._halisha2), 16);

					world._frintezza.broadcastPacket(new Say2(world._frintezza.getObjectId(), ChatType.NPC_ALL, "Frintezza", "Halisha!!! Get rid of these ones!"));

					AttackStanceTaskManager.getInstance().addAttackStanceTask(world._frintezza);
					leaveMovieMode(world.instanceId);

					world._frintezza.abortCast();
					world._frintezza.enableAllSkills();

					startQuestTimer("attack", 1000, world._frintezza, null, true);
					startQuestTimer("song", 1000, world._frintezza, player);

					world._cinameEnRoute = false;
					break;
				/*
				 * ATTACK ROUTINES
				 */
				case "attack":
					L2Npc h = null;
					if(world._halisha2 != null && !world._halisha2.isDead())
					{
						h = world._halisha2;
					}
					else if(world._halisha3 != null && !world._halisha3.isDead())
					{
						h = world._halisha3;
					}
					if(h != null)
					{
						getSkillAI(h);
					}
					break;
				case "song":
					frintezzaSong(world.instanceId);
					startQuestTimer("song", _intervalSong, world._frintezza, null);
					break;
				/*
				 * FIRST POLYMORPH OF SCARLET VON HALISHA
				 */
				case "FirstPolymorph_1":
					setIdle(world._halisha2);
					startQuestTimer("FirstPolymorph_2", 2800, world._frintezza, null);
					break;
				case "FirstPolymorph_2":
					setIdle(world._halisha2);
					world._halisha2.setIsInSocialAction(true);
					world._halisha2.broadcastPacket(new Say2(world._halisha2.getObjectId(), ChatType.ALL, world._halisha2.getNpcId(), NpcStringId.getNpcStringId(1801662)));
					world._halisha2.broadcastPacket(new SocialAction(world._halisha2.getObjectId(), 2));
					startQuestTimer("FirstPolymorph_3", 1000, world._frintezza, null);
					world._halisha2.setTarget(world._target);
					break;
				case "FirstPolymorph_3":
					_skill = SkillTable.getInstance().getInfo(5017, 1);
					world._halisha2.doCast(_skill);
					cast(world._halisha2, _skill);
					world._halisha2.setRHandId(7903);
					npcUpdate(world._halisha2);
					world._isMorphed = true;
					ThreadPoolManager.getInstance().scheduleGeneral(new SetFrintezzaMobilised(world._halisha2), 200);
					startQuestTimer("attack", 1000, world._frintezza, null, true);
					startQuestTimer("song", 1000, world._frintezza, player);
					world._morphing = Boolean.FALSE;
					leaveMovieMode(world.instanceId);
					break;
				/*
				 * SECOND POLYMORPH OF SCARLET VON HALISHA
				 */
				case "SecondPolymorph_1":
					setIdle(world._halisha2);
					world._halisha2.setIsInSocialAction(true);
					world._frintezza.breakCast();
					_skill = SkillTable.getInstance().getInfo(5007, 5);
					cast(world._frintezza, _skill);
					world._halisha2.disableCoreAI(true);
					specialCamera(world._frintezza, 700, 120, 0, 0, 7000, 0, 0);
					startQuestTimer("SecondPolymorph_2", 1000, world._frintezza, null);
					break;
				case "SecondPolymorph_2":
					specialCamera(world._frintezza, 100, 90, 0, 3000, 4100, 0, 0);
					startQuestTimer("SecondPolymorph_3", 4000, world._frintezza, null);
					break;
				case "SecondPolymorph_3":
					specialCamera(world._frintezza, 100, 60, 0, 1000, 1100, 0, 0);
					startQuestTimer("SecondPolymorph_4", 1000, world._frintezza, null);
					break;
				case "SecondPolymorph_4":
					specialCamera(world._frintezza, 1200, 90, 20, 7000, 7100, 0, 0);
					startQuestTimer("SecondPolymorph_5", 7000, world._frintezza, null);
					break;
				case "SecondPolymorph_5":
					specialCamera(world._halisha2, 400, 260, 30, 0, 7000, 0, 0);
					world._halisha2.doDie(world._halisha2);
					startQuestTimer("SecondPolymorph_6", 7000, world._frintezza, null);
					break;
				case "SecondPolymorph_6":
					world._halisha3 = addSpawn(HALISHA3, world._halisha2);
					setIdle(world._halisha3);
					world._halisha3.setIsInSocialAction(true);
					world._halisha3.setHeading(world._halisha2.getHeading());
					world._halisha3.setTarget(world._target);
					specialCamera(world._halisha3, 200, 260, 30, 0, 9000, 0, 0);
					world._halisha2.stopAllEffects();
					world._halisha2.getLocationController().delete();
					world._halisha2 = null;
					world._halisha3.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					world._halisha3.abortAttack();
					world._halisha3.abortCast();
					startQuestTimer("SecondPolymorph_7", 1000, world._frintezza, null);
					break;
				case "SecondPolymorph_7":
					specialCamera(world._halisha3, 200, 270, -10, 2000, 9000, 0, 0);
					social(world._halisha3, 1);
					startQuestTimer("SecondPolymorph_8", 2000, world._frintezza, null);
					break;
				case "SecondPolymorph_8":
					specialCamera(world._halisha3, 200, 230, 0, 0, 9000, 0, 0);
					startQuestTimer("SecondPolymorph_9", 1000, world._frintezza, null);
					break;
				case "SecondPolymorph_9":
					specialCamera(world._halisha3, 200, 230, 0, 2000, 9000, 0, 20);
					startQuestTimer("SecondPolymorph_10", 2000, world._frintezza, null);
					break;
				case "SecondPolymorph_10":
					specialCamera(world._halisha3, 400, 250, 0, 2000, 4000, 0, 0);
					social(world._halisha3, 2);
					startQuestTimer("SecondPolymorph_11", 4000, world._frintezza, null);
					break;
				case "SecondPolymorph_11":
					ThreadPoolManager.getInstance().scheduleGeneral(new SetFrintezzaMobilised(world._halisha3), 200);
					startQuestTimer("attack", 1000, world._frintezza, null, true);
					startQuestTimer("song", 1000, world._frintezza, player);
					world._halisha3.broadcastPacket(new Say2(world._halisha3.getObjectId(), ChatType.ALL, world._halisha3.getNpcId(), NpcStringId.getNpcStringId(1801662)));
					world._morphing = Boolean.FALSE;
					leaveMovieMode(world.instanceId);
					break;
				case "final":
					specialCamera(world._halisha3, 500, 90, 80, 0, 10000, 0, 0);
					startQuestTimer("final2", 10000, world._frintezza, null);
					break;
				case "final2":
					specialCamera(world._frintezza, 100, 120, 10, 0, 12000, 0, 0);
					startQuestTimer("final3", 2000, world._frintezza, null);
					break;
				case "final3":
					specialCamera(world._frintezza, 100, 90, 30, 3000, 12000, 0, 0);
					world._frintezza.doDie(world._frintezza);
					startQuestTimer("final4", 5000, world._frintezza, null);
					break;
				case "final4":
					specialCamera(world._frintezza, 1000, 90, 30, 4000, 6000, 0, 0);
					InstanceManager.getInstance().getInstance(world.instanceId).setDuration(900000);
					InstanceManager.getInstance().getInstance(world.instanceId).removeNpcs();
					world.PlayersInInstance.stream().filter(p -> p != null).forEach(this::savePlayerReenter);
					break;
				case "deletezoneskill1":
					clearZoneSkill(1199870);
					break;
				case "deletezoneskill2":
					clearZoneSkill(1198880);
					break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 654)
		{
			if(reply == 1)
			{
				Location tele1 = new Location(-88914, -140526, -9173);
				Location tele2 = new Location(-87859, -141510, -9173);
				enterInstance(player, "RB_Frintezza.xml", tele1, tele2);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			if(npcId == ALARM)
			{
				for(int i = 17130051; i <= 17130058; i++)
				{
					openDoor(i, world.instanceId);
				}
				openDoor(17130042, world.instanceId);
				openDoor(17130043, world.instanceId);
				spawnRoom1(world.instanceId);
				spawnRoom2(world.instanceId);
			}
			else if(npcId == CAPTAIN)
			{
				world._captainKill++;
				if(world._captainKill == 3)
				{
					for(int i = 17130061; i <= 17130070; i++)
					{
						openDoor(i, world.instanceId);
					}
					openDoor(17130045, world.instanceId);
					openDoor(17130046, world.instanceId);
					for(int i = 17130051; i <= 17130058; i++)
					{
						closeDoor(i, world.instanceId);
					}
					closeDoor(17130042, world.instanceId);
					closeDoor(17130043, world.instanceId);
					world._frintezza = addSpawn(29045, -87777, -155080, -9072, 15450, false, 0, false, world.instanceId);
					world._frintezza.setIsImmobilized(true);
				}
			}
			//Lors de la mort de Halisha3
			else if(npcId == HALISHA3)
			{
				npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
				world._frintezza.getLocationController().delete();
				startQuestTimer("final", 100, npc, null);
				cancelQuestTimer("song", npc, null);
				cancelQuestTimer("attack", npc, null);
			}

			if(npc.equals(world._portrait1))
			{
				if(world._ghost1 != null && world._ghost1.getSpawn() != null)
				{
					world._ghost1.getSpawn().stopRespawn();
				}
			}
			else if(npc.equals(world._portrait2))
			{
				if(world._ghost2 != null && world._ghost2.getSpawn() != null)
				{
					world._ghost2.getSpawn().stopRespawn();
				}
			}
			else if(npc.equals(world._portrait3))
			{
				if(world._ghost3 != null && world._ghost3.getSpawn() != null)
				{
					world._ghost3.getSpawn().stopRespawn();
				}
			}
			else if(npc.equals(world._portrait4))
			{
				if(world._ghost4 != null && world._ghost4.getSpawn() != null)
				{
					world._ghost4.getSpawn().stopRespawn();
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		Location tele = new Location(-87762, -151574, -9173);
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			teleportplayer(player, tele, world, false);
			return null;
		}
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc.isInvul())
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return null;
		}
		if(npc.getNpcId() == HALISHA2 || npc.getNpcId() == HALISHA3)
		{
			getSkillAI(npc);
		}
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			if(npc.getNpcId() == FRINTEZZA)
			{
				L2EffectZone zone = (L2EffectZone) ZoneManager.getInstance().getZoneById(1199870);
				L2EffectZone zone2 = (L2EffectZone) ZoneManager.getInstance().getZoneById(1199880);
				int skillId = skill.getId();
				int skillLvl = skill.getLevel();
				if(skillId == 5007)
				{
					switch(skillLvl)
					{
						case 1: // RB_Frintezza's Concert Hall Melody
							zone.addSkill(5008, 1);
							startQuestTimer("deletezoneskill1", 30000, world._frintezza, null);
							world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2Playable).forEach(pc -> pc.sendPacket(new ExShowScreenMessage(NpcStringId.FUGUE_OF_JUBILATION, ExShowScreenMessage.TOP_CENTER, 3000)));
							break;
						case 2: // RB_Frintezza's Rampaging Opus en masse
							zone.addSkill(5008, 2);
							startQuestTimer("deletezoneskill1", 30000, world._frintezza, null);
							world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2Playable).forEach(pc -> pc.sendPacket(new ExShowScreenMessage(NpcStringId.RONDO_OF_SOLITUDE, ExShowScreenMessage.TOP_CENTER, 3000)));
							world._ghost1 = createNewSpawn(29050, -86183, -152443, -9037, 35048, world.instanceId);
							world._ghost2 = createNewSpawn(29051, -86140, -153991, -9037, 28205, world.instanceId);
							world._ghost3 = createNewSpawn(29051, -89408, -153980, -9037, 64817, world.instanceId);
							world._ghost4 = createNewSpawn(29050, -89382, -152443, -9037, 57730, world.instanceId);
							break;
						case 3: // RB_Frintezza Power Encore
							zone.addSkill(5008, 3);
							startQuestTimer("deletezoneskill1", 30000, world._frintezza, null);
							world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2Playable).forEach(pc -> pc.sendPacket(new ExShowScreenMessage(NpcStringId.FRENETIC_TOCCATA, ExShowScreenMessage.TOP_CENTER, 3000)));
							break;
						case 4: // Mournful Chorale Prelude
							zone2.addSkill(5008, 4);
							startQuestTimer("deletezoneskill2", 30000, world._frintezza, null);
							world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2Playable).forEach(pc -> pc.sendPacket(new ExShowScreenMessage(NpcStringId.REQUIEM_OF_HATRED, ExShowScreenMessage.TOP_CENTER, 3000)));
							break;
						case 5: //Hypnotic Mazurka
							zone2.addSkill(5008, 5);
							startQuestTimer("deletezoneskill2", 30000, world._frintezza, null);
							world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2Playable).forEach(pc -> pc.sendPacket(new ExShowScreenMessage(NpcStringId.HYPNOTIC_MAZURKA, ExShowScreenMessage.TOP_CENTER, 3000)));
							break;
					}
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(tmpworld instanceof FWorld)
			{
				FWorld world = (FWorld) tmpworld;
				if(zone.getId() == _lairZone)
				{
					world.PlayersInLairzone.add((L2PcInstance) character);
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(tmpworld instanceof FWorld)
			{
				FWorld world = (FWorld) tmpworld;
				if(zone.getId() == _lairZone)
				{
					world.PlayersInLairzone.remove(character);
				}
			}
		}
		return super.onExitZone(character, zone);
	}

	private void spawnRoom1(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			int[] position = {0, 0, 0};
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(119987);
			L2ZoneForm zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = createNewSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
			zone = ZoneManager.getInstance().getZoneById(119988);
			zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = addSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
			zone = ZoneManager.getInstance().getZoneById(119989);
			zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = addSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
			zone = ZoneManager.getInstance().getZoneById(119990);
			zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = addSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
			zone = ZoneManager.getInstance().getZoneById(119991);
			zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = addSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
			zone = ZoneManager.getInstance().getZoneById(119992);
			zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = addSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
			zone = ZoneManager.getInstance().getZoneById(119993);
			zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = addSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
			zone = ZoneManager.getInstance().getZoneById(119994);
			zoneform = zone.getZone();
			for(int i = 1; i <= 25; i++)
			{
				position = zoneform.getRandomPosition();
				L2Npc newnpc = addSpawn(KEEPER_MOBS[Rnd.get(KEEPER_MOBS.length)], position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);
				newnpc.setRunning();
				L2PcInstance target = world.PlayersInInstance.get(Rnd.get(world.PlayersInInstance.size()));
				if(target != null)
				{
					((L2Attackable) newnpc).addDamageHate(target, 0, 1000);
				}
			}
		}
	}

	private void spawnRoom2(int instanceId)
	{
		int[] position = {0, 0, 0};
		L2ZoneType zone = ZoneManager.getInstance().getZoneById(119981);
		L2ZoneForm zoneform = zone.getZone();
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18335, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		zone = ZoneManager.getInstance().getZoneById(119982);
		zoneform = zone.getZone();
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18337, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18338, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		zone = ZoneManager.getInstance().getZoneById(119983);
		zoneform = zone.getZone();
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18335, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		zone = ZoneManager.getInstance().getZoneById(119984);
		zoneform = zone.getZone();
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18335, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		zone = ZoneManager.getInstance().getZoneById(119985);
		zoneform = zone.getZone();
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18337, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18338, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		zone = ZoneManager.getInstance().getZoneById(119986);
		zoneform = zone.getZone();
		for(int i = 1; i <= 10; i++)
		{
			position = zoneform.getRandomPosition();
			addSpawn(18335, position[0], position[1], position[2], Rnd.get(33000), false, 0, false, instanceId);
		}
		addSpawn(CUBE, -87904, -141296, -9168, 0, false, 0, false, instanceId);
	}

	private void setReuseCalendars()
	{
		if(nextCalendarReschedule > System.currentTimeMillis())
		{
			return;
		}

		Calendar currentTime = Calendar.getInstance();
		reuse_date_1 = Calendar.getInstance();

		reuse_date_1.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		reuse_date_1.set(Calendar.HOUR_OF_DAY, 6);
		reuse_date_1.set(Calendar.MINUTE, 30);
		reuse_date_1.set(Calendar.SECOND, 0);

		if(currentTime.compareTo(reuse_date_1) > 0)
		{
			reuse_date_1.add(Calendar.DAY_OF_MONTH, 7);
		}

		reuse_date_2 = Calendar.getInstance();

		reuse_date_2.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		reuse_date_2.set(Calendar.HOUR_OF_DAY, 6);
		reuse_date_2.set(Calendar.MINUTE, 30);
		reuse_date_2.set(Calendar.SECOND, 0);

		if(currentTime.compareTo(reuse_date_2) > 0)
		{
			reuse_date_2.add(Calendar.DAY_OF_MONTH, 7);
		}

		nextCalendarReschedule = reuse_date_1.compareTo(reuse_date_2) < 0 ? reuse_date_1.getTimeInMillis() : reuse_date_2.getTimeInMillis();
	}

	private void savePlayerReenter(L2PcInstance player)
	{
		setReuseCalendars();

		long nextTime = 0L;

		nextTime = reuse_date_1.compareTo(reuse_date_2) < 0 ? reuse_date_1.getTimeInMillis() : reuse_date_2.getTimeInMillis();
		InstanceManager.getInstance().setInstanceTime(player.getObjectId(), InstanceZoneId.LAST_IMPERIAL_TOMB.getId(), nextTime);
	}

	protected void openDoor(int doorId, int instanceId)
	{
		InstanceManager.getInstance().getInstance(instanceId).getDoors().stream().filter(door -> door.getDoorId() == doorId).forEach(L2DoorInstance::openMe);
	}

	protected void closeDoor(int doorId, int instanceId)
	{
		InstanceManager.getInstance().getInstance(instanceId).getDoors().stream().filter(door -> door.getDoorId() == doorId).forEach(door -> {
			if(door.isOpened())
			{
				door.closeMe();
			}
		});
	}

	private void getSkillAI(L2Npc npc)
	{
		synchronized(this)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(tmpworld instanceof FWorld)
			{
				FWorld world = (FWorld) tmpworld;
				if(npc.isInvul() || npc.isCastingNow() || world._morphing)
				{
					return;
				}
				//30% de chance de changer de target
				if(Rnd.getChance(30) || world._target == null)
				{
					L2Character target = getRandomTarget(npc);
					if(target != null)
					{
						world._target = target;
					}
				}
				L2Character target = world._target;
				L2Skill skill = chooseSkill(npc);
				if(Util.checkIfInRange(skill.getCastRange(), npc, target, true))
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					npc.setTarget(target);
					npc.setIsCastingNow(true);
					world._target = null;
					npc.doCast(skill);
				}
				else
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);
					npc.setIsCastingNow(false);
				}
			}
		}
	}

	/*
	 * SONG 1 : Heal
	 * SONG 2 : Dash
	 * SONG 3 : pAttack / spped
	 * SONG 4 : reduce heal
	 * SONG 5 : Psycho symphony
	 * SONG 6 : Stun + dance
	 */
	private void frintezzaSong(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			L2Npc h = null;
			if(world._halisha3 != null && !world._halisha3.isDead())
			{
				h = world._halisha3;
			}
			else if(world._halisha2 != null && !world._halisha2.isDead())
			{
				h = world._halisha2;
			}
			if(h == null)
			{
				return;
			}
			if(world._song > 100)
			{
				world._song = Rnd.get(100);
			}
			if(world._nextSong > 100)
			{
				world._nextSong = Rnd.get(100);
			}
			if(world._song < 5) // RB_Frintezza's Concert Hall Melody
			{
				world._song = world._nextSong;
				world._frintezza.doCast(SkillTable.getInstance().getInfo(5007, 1));
				world._nextSong = Rnd.get(100);
			}
			else if(world._song < 35) // RB_Frintezza's Rampaging Opus en masse
			{
				world._song = world._nextSong;
				world._frintezza.doCast(SkillTable.getInstance().getInfo(5007, 2));
				world._nextSong = Rnd.get(100);
			}
			else if(world._song < 75) // RB_Frintezza Power Encore
			{
				world._song = world._nextSong;
				world._frintezza.doCast(SkillTable.getInstance().getInfo(5007, 3));
				world._nextSong = Rnd.get(100);
			}
			else if(world._song < 95) // Mournful Chorale Prelude
			{
				world._song = world._nextSong;
				world._frintezza.doCast(SkillTable.getInstance().getInfo(5007, 4));
				world._nextSong = Rnd.get(100);
			}
			else //Hypnotic Mazurka
			{
				world._song = world._nextSong;
				world._frintezza.doCast(SkillTable.getInstance().getInfo(5007, 5));
				world._nextSong = Rnd.get(100);
			}
		}
	}

	private L2Character getRandomTarget(L2Npc npc)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			FastList<L2Character> result = new FastList<>();
			for(L2Character obj : world.PlayersInLairzone)
			{
				if(!GeoEngine.getInstance().canSeeTarget(obj, npc))
				{
					continue;
				}

				if(obj instanceof L2PcInstance || obj instanceof L2Summon || obj instanceof L2DecoyInstance)
				{
					if(Util.checkIfInRange(9000, npc, obj, true) && !obj.isDead())
					{
						if(obj instanceof L2PcInstance && ((L2PcInstance) obj).getAppearance().getInvisible())
						{
							continue;
						}

						result.add(obj);
					}
				}
			}
			if(!result.isEmpty() && !result.isEmpty())
			{
				Object[] characters = result.toArray();
				return (L2Character) characters[Rnd.get(characters.length)];
			}
		}
		return null;
	}

	private L2Skill chooseSkill(L2Npc npc)
	{
		int npcId = npc.getNpcId();
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			if(npcId == HALISHA2)
			{
				if(world._isMorphed)
				{
					if(Rnd.getChance(10))
					{
						return SkillTable.getInstance().getInfo(5015, 2);
					}
					else if(Rnd.getChance(10))
					{
						return SkillTable.getInstance().getInfo(5015, 5);
					}
					else if(Rnd.getChance(10))
					{
						return SkillTable.getInstance().getInfo(5018, 1);
					}
					else
					{
						return Rnd.getChance(2) ? SkillTable.getInstance().getInfo(5016, 1) : SkillTable.getInstance().getInfo(5014, 2);
					}
				}
				else
				{
					if(Rnd.getChance(2))
					{
						return SkillTable.getInstance().getInfo(5016, 1);
					}
					else if(Rnd.getChance(10))
					{
						return SkillTable.getInstance().getInfo(5015, 1);
					}
					else
					{
						return Rnd.getChance(10) ? SkillTable.getInstance().getInfo(5015, 4) : SkillTable.getInstance().getInfo(5014, 1);
					}
				}
			}
			if(npcId == HALISHA3)
			{
				if(Rnd.getChance(10))
				{
					return SkillTable.getInstance().getInfo(5015, 3);
				}
				else if(Rnd.getChance(10))
				{
					return SkillTable.getInstance().getInfo(5015, 6);
				}
				else if(Rnd.getChance(10))
				{
					return SkillTable.getInstance().getInfo(5018, 2);
				}
				else if(Rnd.getChance(10))
				{
					return SkillTable.getInstance().getInfo(5019, 1);
				}
				else
				{
					return Rnd.getChance(2) ? SkillTable.getInstance().getInfo(5016, 1) : SkillTable.getInstance().getInfo(5014, 3);
				}
			}
		}
		return SkillTable.getInstance().getInfo(5014, 1);
	}

	private void clearZoneSkill(int zoneId)
	{
		L2EffectZone zone = (L2EffectZone) ZoneManager.getInstance().getZoneById(zoneId);
		zone.clearSkills();
	}

	public void enterMovieMode(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2Playable).forEach(pc -> enterMovieMode((L2Playable) pc));
		}
	}

	private void enterMovieMode(L2Playable player)
	{
		player.setTarget(null);
		player.stopMove(null);
		player.setIsInvul(true);
		player.setIsImmobilized(true);
	}

	public void leaveMovieMode(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2Playable).forEach(pc -> leaveMovieMode((L2Playable) pc));
		}
	}

	private void leaveMovieMode(L2Playable player)
	{
		player.setIsInvul(false);
		player.setIsImmobilized(false);
	}

	protected void specialCamera(L2Npc target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise)
	{
		if(target == null)
		{
			return;
		}
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(target.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2PcInstance).forEach(pc -> {
				pc.abortAttack();
				pc.abortCast();
				pc.setTarget(null);
				pc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				pc.sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, 1, 0));
			});
		}
	}

	protected void npcUpdate(L2Npc npc)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FWorld)
		{
			FWorld world = (FWorld) tmpworld;
			world.PlayersInLairzone.stream().filter(pc -> pc instanceof L2PcInstance).forEach(pc -> pc.sendPacket(new NpcInfo(npc)));
		}
	}

	protected L2Npc createNewSpawn(int templateId, int x, int y, int z, int heading, int instanceId)
	{
		L2Spawn tempSpawn = null;

		L2Npc npc;
		L2NpcTemplate template;

		try
		{
			template = NpcTable.getInstance().getTemplate(templateId);
			tempSpawn = new L2Spawn(template);

			tempSpawn.setLocx(x);
			tempSpawn.setLocy(y);
			tempSpawn.setLocz(z);
			tempSpawn.setHeading(heading);
			tempSpawn.setAmount(1);
			tempSpawn.setInstanceId(instanceId);
			tempSpawn.stopRespawn();

			SpawnTable.getInstance().addNewSpawn(tempSpawn);
		}
		catch(Throwable t)
		{
			_log.log(Level.ERROR, t.getMessage());
		}

		npc = tempSpawn.doSpawn();

		return npc;
	}

	public void setIdle(L2Character target)
	{
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		target.abortAttack();
		target.abortCast();
		target.setIsImmobilized(true);
		target.disableAllSkills();
	}

	protected void cast(L2Npc npc, L2Skill skill)
	{
		if(npc == null || skill == null)
		{
			return;
		}

		npc.broadcastPacket(new MagicSkillUse(npc, npc, skill.getDisplayId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
	}

	protected void social(L2Npc target, int action)
	{
		if(target == null)
		{
			return;
		}

		target.broadcastPacket(new SocialAction(target.getObjectId(), action));
	}

	private class FWorld extends InstanceWorld
	{
		public List<L2PcInstance> PlayersInInstance = new FastList<>();
		public List<L2PcInstance> PlayersInLairzone = new FastList<>();
		boolean _cinameEnRoute;
		Boolean _morphing = Boolean.FALSE;
		boolean _isMorphed;
		int _captainKill;
		int _song = 1000;
		int _nextSong = 1000;
		L2Character _target;
		L2Npc _frintezza;
		L2Npc _halisha2;
		L2Npc _halisha3;
		L2Npc _dummy;
		L2Npc _dummy1;
		L2Npc _dummy2;
		L2Npc _dummy3;
		L2Npc _portrait1;
		L2Npc _portrait2;
		L2Npc _portrait3;
		L2Npc _portrait4;
		L2Npc _ghost1;
		L2Npc _ghost2;
		L2Npc _ghost3;
		L2Npc _ghost4;

		public FWorld(Long time)
		{
			InstanceManager.getInstance();
		}
	}

	private class SetFrintezzaMobilised implements Runnable
	{
		private L2Npc _boss;

		public SetFrintezzaMobilised(L2Npc boss)
		{
			_boss = boss;
		}

		@Override
		public void run()
		{
			_boss.setIsImmobilized(false);
			_boss.setIsInSocialAction(false);
			_boss.enableAllSkills();
			_boss.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
}