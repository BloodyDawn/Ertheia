package dwo.scripts.ai.individual.raidbosses;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2DecoyInstance;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestTimer;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.network.game.serverpackets.EarthQuake;
import dwo.gameserver.network.game.serverpackets.MoveToPawn;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_IDLE;

/**
 * Baium AI
 *
 * Note1: if the server gets rebooted while players are still fighting Baium, there is no lock, but
 *   players also lose their ability to wake baium up.  However, should another person
 *   enter the room and wake him up, the players who had stayed inside may join the raid.
 *   This can be helpful for players who became victims of a reboot (they only need 1 new player to
 *   enter and wake up baium) and is not too exploitable since any player wishing to exploit it
 *   would have to suffer 5 days of being parked in an empty room.
 * Note2: Neither version of Baium should be a permanent spawn.  This script is fully capable of
 *   spawning the statue-version when the lock expires and switching it to the mob version promptly.
 *
 * Additional notes ( source http://aleenaresron.blogspot.com/2006_08_01_archive.html ):
 *   * Baium only first respawns five days after his last death. And from those five days he will
 *       respawn within 1-8 hours of his last death. So, you have to know his last time of death.
 *   * If by some freak chance you are the only one in Baium's chamber and NO ONE comes in
 *       [ha, ha] you or someone else will have to wake Baium. There is a good chance that Baium
 *       will automatically kill whoever wakes him. There are some people that have been able to
 *       wake him and not die, however if you've already gone through the trouble of getting the
 *       bloody fabric and camped him out and researched his spawn time, are you willing to take that
 *       chance that you'll wake him and not be able to finish your quest? Doubtful.
 *       [ this powerful attack vs the player who wakes him up is NOT yet implemented here]
 *   * once someone starts attacking Baium no one else can port into the chamber where he is.
 *       Unlike with the other raid bosses, you can just show up at any time as long as you are there
 *       when they die. Not true with Baium. Once he gets attacked, the port to Baium closes. byebye,
 *       see you in 5 days.  If nobody attacks baium for 30 minutes, he auto-despawns and unlocks the
 *       vortex
 *
 * @author Fulminus version 0.1
 */

public class Baium extends Quest
{
	private static final int STONE_BAIUM = 29025;
	private static final int LIVE_BAIUM = 29020;
	private static final int ARCHANGEL = 29021;
	private static final int BAIUM_TELEPORT_CUBIC = 31842;
	//Baium status tracking
	private static final byte ASLEEP = 0;  // baium is in the stone version, waiting to be woken up.  Entry is unlocked
	private static final byte AWAKE = 1;   // baium is awake and fighting.  Entry is locked.
	private static final byte DEAD = 2;    // baium has been killed and has not yet spawned.  Entry is locked
	// fixed archangel spawnloc
	private static final int[][] ANGEL_LOCATION = {
		{114239, 17168, 10080, 63544}, {115780, 15564, 10080, 13620}, {114880, 16236, 10080, 5400},
		{115168, 17200, 10080, 0}, {115792, 16608, 10080, 0},
	};
	private static final L2BossZone _baiumBossZone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
	protected final List<L2Npc> _Minions = new ArrayList<>(5);
	private L2Character _target;
	private L2Skill _skill;
	private long _LastAttackVsBaiumTime;

	public Baium()
	{
		registerMobs(new int[]{LIVE_BAIUM});
		addAskId(STONE_BAIUM, 9999);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
		int status = GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM);
		if(status == DEAD)
		{
			// load the unlock date and time for baium from DB
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if(temp > 0)
			{
				// the unlock time has not yet expired.  Mark Baium as currently locked (dead).  Setup a timer
				// to fire at the correct time (calculate the time between now and the unlock time,
				// setup a timer to fire after that many msec)
				startQuestTimer("baium_unlock", temp, null, null);
			}
			else
			{
				// the time has already expired while the server was offline.  Delete the saved time and
				// immediately spawn the stone-baium.  Also the state need not be changed from ASLEEP
				addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
				GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
			}
		}
		else if(status == AWAKE)
		{
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			int hp = info.getInteger("currentHP");
			int mp = info.getInteger("currentMP");
			L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM, loc_x, loc_y, loc_z, heading, false, 0);
			GrandBossManager.getInstance().addBoss(baium);
			L2Npc _baium = baium;
			ThreadPoolManager.getInstance().scheduleGeneral(() -> {
				try
				{
					_baium.setCurrentHpMp(hp, mp);
					_baium.setIsInvul(true);
					_baium.setIsImmobilized(true);
					_baium.setRunning();
					_baium.broadcastPacket(new SocialAction(_baium.getObjectId(), 2));
					startQuestTimer("baium_wakeup", 15000, _baium, null);
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "", e);
				}
			}, 100L);
		}
		else
		{
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
		}
	}

	public static void main(String[] args)
	{
		// Quest class and state definition
		new Baium();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(!_baiumBossZone.isInsideZone(attacker))
		{
			attacker.reduceCurrentHp(attacker.getCurrentHp(), attacker, false, false, null);
			return super.onAttack(npc, attacker, damage, isPet);
		}
		if(npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return super.onAttack(npc, attacker, damage, isPet);
		}
		if(npc.getNpcId() == LIVE_BAIUM && !npc.isInvul())
		{
			if(attacker.getMountType() == 1)
			{
				int sk_4258 = 0;
				L2Effect[] effects = attacker.getAllEffects();
				if(effects != null && effects.length != 0)
				{
					for(L2Effect e : effects)
					{
						if(e.getSkill().getId() == 4258)
						{
							sk_4258 = 1;
						}
					}
				}
				if(sk_4258 == 0)
				{
					npc.setTarget(attacker);
					L2Skill skill = SkillTable.getInstance().getInfo(4258, 1);
					if(skill.isMagic())
					{
						if(npc.isMuted())
						{
							return super.onAttack(npc, attacker, damage, isPet);
						}
					}
					else
					{
						if(npc.isPhysicalMuted())
						{
							return super.onAttack(npc, attacker, damage, isPet);
						}
					}
					npc.doCast(skill);
				}
			}
			// update a variable with the last action against baium
			_LastAttackVsBaiumTime = System.currentTimeMillis();
			callSkillAI(npc);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("baium_unlock"))
		{
			GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
		}
		else if(event.equalsIgnoreCase("skill_range") && npc != null)
		{
			callSkillAI(npc);
		}
		else if(event.equalsIgnoreCase("clean_player"))
		{
			_target = getRandomTarget(npc);
		}
		else if(event.equalsIgnoreCase("baium_wakeup") && npc != null)
		{
			if(npc.getNpcId() == LIVE_BAIUM)
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
				npc.broadcastPacket(new EarthQuake(npc.getX(), npc.getY(), npc.getZ(), 40, 5));
				// start monitoring baium's inactivity
				_LastAttackVsBaiumTime = System.currentTimeMillis();
				startQuestTimer("baium_despawn", 60000, npc, null, true);
				startQuestTimer("skill_range", 500, npc, null, true);
				L2Npc baium = npc;
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					try
					{
						baium.setIsInvul(false);
						baium.setIsImmobilized(false);
						for(L2Npc minion : _Minions)
						{
							minion.setShowSummonAnimation(false);
						}
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "", e);
					}
				}, 11100L);
				// TODO: the person who woke baium up should be knocked across the room, onto a wall, and lose massive amounts of HP.
				for(int[] aANGEL_LOCATION : ANGEL_LOCATION)
				{
					L2Npc angel = addSpawn(ARCHANGEL, aANGEL_LOCATION[0], aANGEL_LOCATION[1], aANGEL_LOCATION[2], aANGEL_LOCATION[3], false, 0, true);
					angel.setIsInvul(true);
					_Minions.add(angel);
				}
			}
		}
		else if(event.equalsIgnoreCase("baium_despawn") && npc != null)
		{
			// despawn the live baium after 30 minutes of inactivity
			// also check if the players are cheating, having pulled Baium outside his zone...
			if(npc.getNpcId() == LIVE_BAIUM)
			{
				// just in case the zone reference has been lost (somehow...), restore the reference
				if(_LastAttackVsBaiumTime + 1800000 < System.currentTimeMillis())
				{
					npc.getLocationController().delete(); // despawn the live-baium
					_Minions.stream().filter(minion -> minion != null).forEach(minion -> {
						minion.getSpawn().stopRespawn();
						minion.getLocationController().delete();
					});
					_Minions.clear();
					addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);  // spawn stone-baium
					GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);    // mark that Baium is not awake any more
					_baiumBossZone.oustAllPlayers();
					cancelQuestTimer("baium_despawn", npc, null);
				}
				else if(_LastAttackVsBaiumTime + 300000 < System.currentTimeMillis() && npc.getCurrentHp() < npc.getMaxHp() * 3 / 4.0)
				{
					npc.setIsCastingNow(false); //just in case
					npc.setTarget(npc);
					L2Skill skill = SkillTable.getInstance().getInfo(4135, 1);
					if(skill.isMagic())
					{
						if(npc.isMuted())
						{
							return super.onAdvEvent(event, npc, player);
						}
					}
					else
					{
						if(npc.isPhysicalMuted())
						{
							return super.onAdvEvent(event, npc, player);
						}
					}
					npc.doCast(skill);
					npc.setIsCastingNow(true);
				}
				else if(!_baiumBossZone.isInsideZone(npc))
				{
					npc.teleToLocation(116033, 17447, 10104);
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 9999)
		{
			if(reply == 1)
			{
				if(GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP)
				{
					if(_baiumBossZone.isPlayerAllowed(player))
					{
						// once Baium is awaken, no more people may enter until he dies, the server reboots, or
						// 30 minutes pass with no attacks made against Baium.
						GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, AWAKE);
						npc.getLocationController().delete();
						L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM, npc, true);
						GrandBossManager.getInstance().addBoss(baium);
						L2Npc _baium = baium;
						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							try
							{
								_baium.setIsInvul(true);
								_baium.setRunning();
								_baium.broadcastPacket(new SocialAction(_baium.getObjectId(), 2));
								startQuestTimer("baium_wakeup", 15000, _baium, null);
								_baium.setShowSummonAnimation(false);
							}
							catch(Throwable e)
							{
								_log.log(Level.ERROR, "", e);
							}
						}, 100L);
					}
					else
					{
						return null;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		cancelQuestTimer("baium_despawn", npc, null);
		npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

		// spawn the "Teleportation Cubic" for 15 minutes (to allow players to exit the lair)
		addSpawn(BAIUM_TELEPORT_CUBIC, 115017, 15549, 10090, 0, false, 900000); // should we teleport everyone out if the cubic despawns??

		// "lock" baium for 5 days and 1 to 8 hours [i.e. 432,000,000 +  1*3,600,000 + random-less-than(8*3,600,000) millisecs]
		long respawnTime = (long) Config.INTERVAL_OF_BAIUM_SPAWN + Rnd.get(Config.RANDOM_OF_BAIUM_SPAWN);
		GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, DEAD);
		startQuestTimer("baium_unlock", respawnTime, null, null);

		// also save the respawn time so that the info is maintained past reboots
		StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatsSet(LIVE_BAIUM, info);
		_Minions.stream().filter(minion -> minion != null).forEach(minion -> {
			minion.getSpawn().stopRespawn();
			minion.getLocationController().delete();
		});
		_Minions.clear();
		QuestTimer timer = getQuestTimer("skill_range", npc, null);
		if(timer != null)
		{
			timer.cancelAndRemove();
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		npc.setTarget(caster);
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		if(npc.getNpcId() == LIVE_BAIUM && !npc.isInvul())
		{
			callSkillAI(npc);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		return super.onSpawn(npc);
	}

	public L2Character getRandomTarget(L2Npc npc)
	{
		FastList<L2Character> result = FastList.newInstance();

		for(L2Object obj : npc.getKnownList().getKnownCharactersInRadius(9000))
		{
			if(obj instanceof L2Playable || obj instanceof L2DecoyInstance)
			{
				if(obj instanceof L2PcInstance)
				{
					if(((L2PcInstance) obj).getAppearance().getInvisible())
					{
						continue;
					}
				}

				if(obj.getZ() < npc.getZ() - 100 && (obj.getZ() > npc.getZ() + 100 || !GeoEngine.getInstance().canSeeTarget(obj, npc)))
				{
					continue;
				}
				if(!((L2Character) obj).isDead())
				{
					result.add((L2Character) obj);
				}
			}
		}

		if(result.isEmpty())
		{
			result.addAll(_Minions.stream().filter(minion -> minion != null).collect(Collectors.toList()));
		}

		if(result.isEmpty())
		{
			FastList.recycle(result);
			return null;
		}

		Object[] characters = result.toArray();
		QuestTimer timer = getQuestTimer("clean_player", npc, null);
		if(timer != null)
		{
			timer.cancel();
		}
		startQuestTimer("clean_player", 20000, npc, null);
		L2Character target = (L2Character) characters[Rnd.get(characters.length)];
		FastList.recycle(result);
		return target;

	}

	public void callSkillAI(L2Npc npc)
	{
		synchronized(this)
		{
			if(npc.isInvul() || npc.isCastingNow())
			{
				return;
			}

			if(_target == null || _target.isDead() || !_baiumBossZone.isInsideZone(_target))
			{
				_target = getRandomTarget(npc);
				if(_target != null)
				{
					_skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
				}
			}

			L2Character target = _target;
			L2Skill skill = _skill;
			if(skill == null)
			{
				skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
			}

			if(skill.isMagic())
			{
				if(npc.isMuted())
				{
					return;
				}
			}
			else
			{
				if(npc.isPhysicalMuted())
				{
					return;
				}
			}

			if(target == null || target.isDead() || !_baiumBossZone.isInsideZone(target))
			{
				npc.setIsCastingNow(false);
				return;
			}

			if(Util.checkIfInRange(skill.getCastRange(), npc, target, true))
			{
				npc.getAI().setIntention(AI_INTENTION_IDLE);
				npc.setTarget(target);
				npc.setIsCastingNow(true);
				_target = null;
				_skill = null;
				if(getDist(skill.getCastRange()) > 0)
				{
					npc.broadcastPacket(new MoveToPawn(npc, target, getDist(skill.getCastRange())));
				}
				try
				{
					npc.stopMove(null);
					npc.doCast(skill);
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "", e);
				}
			}
			else
			{
				npc.getAI().setIntention(AI_INTENTION_FOLLOW, target, null);
				npc.setIsCastingNow(false);
			}
		}
	}

	public int getRandomSkill(L2Npc npc)
	{
		int skill;
		if(npc.getCurrentHp() > npc.getMaxHp() * 3 / 4.0)
		{
			if(Rnd.getChance(10))
			{
				skill = 4128;
			}
			else
			{
				skill = Rnd.getChance(10) ? 4129 : 4127;
			}
		}
		else if(npc.getCurrentHp() > (npc.getMaxHp() << 1) / 4.0)
		{
			if(Rnd.getChance(10))
			{
				skill = 4131;
			}
			else if(Rnd.getChance(10))
			{
				skill = 4128;
			}
			else
			{
				skill = Rnd.getChance(10) ? 4129 : 4127;
			}
		}
		else if(npc.getCurrentHp() > npc.getMaxHp() / 4.0)
		{
			if(Rnd.getChance(10))
			{
				skill = 4130;
			}
			else if(Rnd.getChance(10))
			{
				skill = 4131;
			}
			else if(Rnd.getChance(10))
			{
				skill = 4128;
			}
			else
			{
				skill = Rnd.getChance(10) ? 4129 : 4127;
			}
		}
		else if(Rnd.getChance(10))
		{
			skill = 4130;
		}
		else if(Rnd.getChance(10))
		{
			skill = 4131;
		}
		else if(Rnd.getChance(10))
		{
			skill = 4128;
		}
		else
		{
			skill = Rnd.getChance(10) ? 4129 : 4127;
		}
		return skill;
	}

	public int getDist(int range)
	{
		int dist = 0;
		switch(range)
		{
			case -1:
				break;
			case 100:
				dist = 85;
				break;
			default:
				dist = range - 85;
				break;
		}
		return dist;
	}
}