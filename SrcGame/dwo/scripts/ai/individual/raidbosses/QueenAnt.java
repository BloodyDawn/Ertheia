package dwo.scripts.ai.individual.raidbosses;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emperorc, ANZO
 */

public class QueenAnt extends Quest
{
	private static final int QUEEN = 29001;
	private static final int LARVA = 29002;
	private static final int NURSE = 29003;
	private static final int GUARD = 29004;
	private static final int ROYAL = 29005;

	private static final int[] MOBS = {QUEEN, LARVA, NURSE, GUARD, ROYAL};

	private static final int QUEEN_X = -21610;
	private static final int QUEEN_Y = 181594;
	private static final int QUEEN_Z = -5734;

	//QUEEN Status Tracking :
	private static final byte ALIVE = 0; //Queen Ant is spawned.
	private static final byte DEAD = 1; //Queen Ant has been killed.

	private static L2BossZone _zone;

	private static SkillHolder HEAL1 = new SkillHolder(4020, 1);
	private static SkillHolder HEAL2 = new SkillHolder(4024, 1);
	private final List<L2MonsterInstance> _nurses = new ArrayList<>(5);
	private L2MonsterInstance _queen;
	private L2MonsterInstance _larva;

	public QueenAnt()
	{
		registerMobs(MOBS, QuestEventType.ON_SPAWN, QuestEventType.ON_KILL, QuestEventType.ON_AGGRO_RANGE_ENTER);
		addFactionCallId(NURSE);
		addExitZoneId(33017);
		_zone = GrandBossManager.getInstance().getZone(QUEEN_X, QUEEN_Y, QUEEN_Z);

		StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
		int status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		if(status == DEAD)
		{
			// load the unlock date and time for queen ant from DB
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			// if queen ant is locked until a certain time, mark it so and start the unlock timer
			// the unlock time has not yet expired.
			if(temp > 0)
			{
				startQuestTimer("queen_unlock", temp, null, null);
			}
			else
			{
				// the time has already expired while the server was offline. Immediately spawn queen ant.
				L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, QUEEN_X, QUEEN_Y, QUEEN_Z, 0, false, 0);
				GrandBossManager.getInstance().setBossStatus(QUEEN, ALIVE);
				spawnBoss(queen);
			}
		}
		else
		{
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			int hp = info.getInteger("currentHP");
			int mp = info.getInteger("currentMP");
			if(!_zone.isInsideZone(loc_x, loc_y, loc_z))
			{
				loc_x = QUEEN_X;
				loc_y = QUEEN_Y;
				loc_z = QUEEN_Z;
			}
			L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, loc_x, loc_y, loc_z, heading, false, 0);
			queen.setCurrentHpMp(hp, mp);
			spawnBoss(queen);
		}
	}

	public static void main(String[] args)
	{
		new QueenAnt();
	}

	private void spawnBoss(L2GrandBossInstance npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		if(Rnd.getChance(33))
		{
			_zone.movePlayersTo(-19480, 187344, -5600);
		}
		else if(Rnd.getChance(50))
		{
			_zone.movePlayersTo(-17928, 180912, -5520);
		}
		else
		{
			_zone.movePlayersTo(-23808, 182368, -5600);
		}
		GrandBossManager.getInstance().addBoss(npc);
		startQuestTimer("action", 10000, npc, null, true);
		startQuestTimer("heal", 1000, null, null, true);
		npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		_queen = npc;
		_larva = (L2MonsterInstance) addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
        if (event.equalsIgnoreCase("heal")) 
        {
            boolean larvaNeedHeal = _larva != null && _larva.getCurrentHp() < _larva.getMaxHp();
            boolean queenNeedHeal = _queen != null && _queen.getCurrentHp() < _queen.getMaxHp();
            
            for (L2MonsterInstance nurse : _nurses) 
            {
                if (_queen != null && !_queen.isDead() && nurse != null && !nurse.isDead()) 
                {
                    if (nurse.isCastingNow()) 
                    {
                        continue;
                    }
                    
                    boolean notCasting = nurse.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST;
                    
                    if (larvaNeedHeal) 
                    {
                        if (nurse.getTarget() == _larva && !notCasting) 
                        {
                            continue;
                        }
                        nurse.setTarget(_larva);
                        nurse.useMagic(Rnd.nextBoolean() ? QueenAnt.HEAL1.getSkill() : QueenAnt.HEAL2.getSkill());
                    }
                    else if (queenNeedHeal) 
                    {
                        if (nurse.getLeader() == _larva) 
                        {
                            continue;
                        }
                        if (nurse.getTarget() == _queen && !notCasting) 
                        {
                            continue;
                        }
                        nurse.setTarget(_queen);
                        nurse.useMagic(QueenAnt.HEAL1.getSkill());
                    }
                    else 
                    {
                        if (!notCasting || nurse.getTarget() == null)
                        {
                            continue;
                        }
                        nurse.setTarget(null);
                    }
                }
            }
        }
		else if(event.equalsIgnoreCase("action") && npc != null)
		{
			if(Rnd.getChance(25))
			{
				if(Rnd.getChance(33))
				{
					npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
				}
				else
				{
					npc.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
				}
			}
		}
		else if(event.equalsIgnoreCase("queen_unlock"))
		{
			L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, QUEEN_X, QUEEN_Y, QUEEN_Z, 0, false, 0);
			GrandBossManager.getInstance().setBossStatus(QUEEN, ALIVE);
			spawnBoss(queen);
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if(npcId == QUEEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD);
			//time is 36hour	+/- 17hour
			long respawnTime = (long) Config.INTERVAL_OF_QUEEN_ANT_SPAWN + Rnd.get(Config.RANDOM_OF_QUEEN_ANT_SPAWN);
			startQuestTimer("queen_unlock", respawnTime, null, null);
			cancelQuestTimer("action", npc, null);
			cancelQuestTimer("heal", null, null);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(QUEEN, info);
			_nurses.clear();
			_larva.getLocationController().delete();
			_larva = null;
			_queen = null;
		}
		else if(_queen != null && !_queen.isAlikeDead())
		{
			if(npcId == ROYAL)
			{
				L2MonsterInstance mob = (L2MonsterInstance) npc;
				if(mob.getLeader() != null)
				{
					mob.getLeader().getMinionList().onMinionDie(mob, (280 + Rnd.get(40)) * 1000);
				}
			}
			else if(npcId == NURSE)
			{
				L2MonsterInstance mob = (L2MonsterInstance) npc;
				_nurses.remove(mob);
				if(mob.getLeader() != null)
				{
					mob.getLeader().getMinionList().onMinionDie(mob, 10000);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		L2MonsterInstance mob = (L2MonsterInstance) npc;
		switch(npc.getNpcId())
		{
			case LARVA:
				mob.setIsImmobilized(true);
				mob.setIsMortal(false);
				mob.setIsRaidMinion(true);
				break;
			case NURSE:
				mob.disableCoreAI(true);
				mob.setIsRaidMinion(true);
				_nurses.add(mob);
				break;
			case ROYAL:
			case GUARD:
				mob.setIsRaidMinion(true);
				break;
		}

		return super.onSpawn(npc);
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if(caller == null || npc == null)
		{
			return super.onFactionCall(npc, caller, attacker, isPet);
		}

		if(!npc.isCastingNow() && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST)
		{
			if(caller.getCurrentHp() < caller.getMaxHp())
			{
				npc.setTarget(caller);
				((L2Attackable) npc).useMagic(HEAL1.getSkill());
			}
		}
		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc == null)
		{
			return null;
		}

		boolean isMage;
		L2Playable character;
		if(isPet)
		{
			isMage = false;
			character = player.getPets().getFirst();
		}
		else
		{
			isMage = player.isMageClass();
			character = player;
		}

		if(character == null)
		{
			return null;
		}

		if(!Config.RAID_DISABLE_CURSE && character.getLevel() - npc.getLevel() > 8)
		{
			L2Skill curse = null;
			if(isMage)
			{
				if(!character.isMuted() && Rnd.get(4) == 0)
				{
					curse = SkillTable.FrequentSkill.RAID_CURSE.getSkill();
				}
			}
			else
			{
				if(!character.isParalyzed() && Rnd.get(4) == 0)
				{
					curse = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();
				}
			}

			if(curse != null)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, character, curse.getId(), curse.getLevel(), 300, 0));
				curse.getEffects(npc, character);
			}

			((L2Attackable) npc).stopHating(character); // for calling again
			return null;
		}

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2Attackable)
		{
			L2Attackable mob = (L2Attackable) character;
			if(mob.getNpcId() == QUEEN && !_queen.isTeleporting())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new QueenReturn(mob), 1000);
			}
		}
		return super.onExitZone(character, zone);
	}

	private class QueenReturn implements Runnable
	{
		private final L2Attackable _queen;

		public QueenReturn(L2Attackable queen)
		{
			_queen = queen;
		}

		@Override
		public void run()
		{
			_queen.abortAttack();
			_queen.abortCast();
			_queen.clearAggroList();
			_queen.setCurrentHp(_queen.getMaxHp());
			_queen.setCurrentMp(_queen.getMaxMp());
			_queen.teleToLocation(QUEEN_X, QUEEN_Y, QUEEN_Z);
		}
	}
}