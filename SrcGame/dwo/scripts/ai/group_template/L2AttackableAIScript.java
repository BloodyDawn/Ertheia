package dwo.scripts.ai.group_template;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

import java.util.List;
import java.util.Set;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ATTACK;

/**
 *
 * Overarching Superclass for all mob AI
 * @author Fulminus
 *
 */
public class L2AttackableAIScript extends Quest
{
	public static void main(String[] args)
	{
		L2AttackableAIScript ai = new L2AttackableAIScript();
		// register all mobs here...
		for(int level = 1; level < 100; level++)
		{
			List<L2NpcTemplate> templates = NpcTable.getInstance().getAllOfLevel(level);
			if(templates != null && !templates.isEmpty())
			{
				for(L2NpcTemplate t : templates)
				{
					try
					{
						if(L2Attackable.class.isAssignableFrom(Class.forName("dwo.gameserver.model.actor.instance." + t.getType() + "Instance")))
						{
							ai.addEventId(t.getNpcId(), QuestEventType.ON_ATTACK);
							ai.addEventId(t.getNpcId(), QuestEventType.ON_KILL);
							ai.addEventId(t.getNpcId(), QuestEventType.ON_SPAWN);
							ai.addEventId(t.getNpcId(), QuestEventType.ON_SKILL_SEE);
							ai.addEventId(t.getNpcId(), QuestEventType.ON_FACTION_CALL);
							ai.addEventId(t.getNpcId(), QuestEventType.ON_AGGRO_RANGE_ENTER);
						}
					}
					catch(ClassNotFoundException ex)
					{
						_log.log(Level.ERROR, "Class not found " + t.getType() + "Instance");
					}
				}
			}
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(attacker != null && npc instanceof L2Attackable)
		{
			L2Attackable attackable = (L2Attackable) npc;

			L2Character originalAttacker = isPet ? attacker.getPets().getFirst() : attacker;
			attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
			attackable.addDamageHate(originalAttacker, damage, damage * 100 / (attackable.getLevel() + 7));
		}
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		synchronized(this)
		{
			if(npc instanceof L2MonsterInstance)
			{
				L2MonsterInstance mob = (L2MonsterInstance) npc;
				if(mob.getLeader() != null)
				{
					try
					{
						int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(npc.getNpcId()) ? Config.MINIONS_RESPAWN_TIME.get(mob.getNpcId()) * 1000 : -1;
						mob.getLeader().getMinionList().onMinionDie(mob, respawnTime);
					}
					catch(Exception e)
					{
						// TODO TEPORARY
						_log.log(Level.ERROR, "Leader: " + mob.getLeader());
						if(mob.getLeader() != null)
						{
							_log.log(Level.ERROR, "Minion list: " + mob.getLeader().getMinionList());
						}
						_log.log(Level.ERROR, "npc: " + npc + " killer: " + killer.getName() + ". Reason: " + e.getMessage(), e);
					}
				}

				if(mob.hasMinions())
				{
					mob.getMinionList().onMasterDie(false);
				}
			}
			return null;
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(caster == null)
		{
			return null;
		}
		if(!(npc instanceof L2Attackable))
		{
			return null;
		}

		L2Attackable attackable = (L2Attackable) npc;

		int skillAggroPoints = skill.getAggroPoints();

		if(!caster.getPets().isEmpty())
		{
			if(targets.length == 1)
			{
				for(L2Summon pet : caster.getPets())
				{
					if(ArrayUtils.contains(targets, pet))
					{
						skillAggroPoints = 0;
					}
				}
			}
		}

		if(skillAggroPoints > 0)
		{
			if(attackable.hasAI() && attackable.getAI().getIntention() == AI_INTENTION_ATTACK)
			{
				L2Object npcTarget = attackable.getTarget();
				for(L2Object skillTarget : targets)
				{
					if(npcTarget.equals(skillTarget) || npc.equals(skillTarget))
					{
						L2Character originalCaster = isPet ? caster.getPets().getFirst() : caster;
						attackable.addDamageHate(originalCaster, 0, skillAggroPoints * 150 / (attackable.getLevel() + 7));
					}
				}
			}
		}

		return null;
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if(attacker == null)
		{
			return null;
		}

		L2Character originalAttackTarget = isPet ? attacker.getPets().getFirst() : attacker;

		// By default, when a faction member calls for help, attack the caller's attacker.
		// Notify the AI with EVT_AGGRESSION
		// Preventing some strange behavior with CALLS (1 calls 2, 2 calls 1, 1 calls 2, etc)
		if(npc.getAI().getAttackTarget() != null)
		{
			return null;
		}
		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);

		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(player == null)
		{
			return null;
		}

		L2Character target = isPet ? player.getPets().getFirst() : player;

		((L2Attackable) npc).addDamageHate(target, 0, 1);

		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if(npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		return null;
	}

	/**
	 * This is used to register all monsters contained in mobs for a particular script<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method register ID for all QuestEventTypes<BR>
	 * Do not use for group_template AIs</B></FONT><BR>
	 * @param mobs
	 * @see #registerMobs(int[], QuestEventType...)
	 */
	@Override
	public void registerMobs(int[] mobs)
	{
		for(int id : mobs)
		{
			addEventId(id, QuestEventType.ON_ATTACK);
			addEventId(id, QuestEventType.ON_KILL);
			addEventId(id, QuestEventType.ON_SPAWN);
			addEventId(id, QuestEventType.ON_SPELL_FINISHED);
			addEventId(id, QuestEventType.ON_SKILL_SEE);
			addEventId(id, QuestEventType.ON_FACTION_CALL);
			addEventId(id, QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
	}

	/**
	 * Scripts are loaded after first npc spawn so we must reRun it.
	 * @param npcIds ids of npcs registered onSpawn
	 */
	@Override
	protected void onSpawnRerun(int... npcIds)
	{
		for(int npcId : npcIds)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				L2Npc lastSpawn = spawn.getLastSpawn();
				if(lastSpawn != null)
				{
					onSpawn(lastSpawn);
				}
			}
		}
	}
}