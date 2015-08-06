package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import javolution.util.FastMap;

public class MasterAnays extends Quest
{
	private static final int ANAIS = 25701;
	private static final int GUARD = 25702;
	private static final long STATUS_CHECK_TIME = 50000;
	private static final long BURNER_ACTION_TIME = 20000;
	private static final long GUARD_ACTION_TIME = 1000;
	// Burners spawns
	private static final int[][] BURNERS = {
		{113632, -75616, 50}, {111904, -75616, 58}, {111904, -77424, 51}, {113696, -77393, 48}
	};
	// Status
	private static boolean FIGHTHING;
	// Status of Burners
	private static int BURNERS_ENABLED;
	private static SkillHolder guard_skill = new SkillHolder(6326, 1);
	// Lists
	private FastList<L2Npc> burners = new FastList<>();
	private FastList<L2Npc> guards = new FastList<>();
	private FastMap<L2Npc, L2PcInstance> targets = new FastMap<>();

	public MasterAnays()
	{
		addAttackId(ANAIS, GUARD);
		addKillId(ANAIS, GUARD);
		spawnBurners();
	}

	public static void main(String[] args)
	{
		new MasterAnays();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == ANAIS)
		{
			if(FIGHTHING)
			{
				if(Rnd.getChance(10) && BURNERS_ENABLED < 4)
				{
					checkBurnerStatus(npc);
				}
			}
			else
			{
				FIGHTHING = true;
				startQuestTimer("check_status", STATUS_CHECK_TIME, npc, null);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("check_status"))
		{
			if(FIGHTHING)
			{
				if(npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					stopFight();
				}
				else
				{
					startQuestTimer("check_status", STATUS_CHECK_TIME, npc, null);
				}
			}
		}
		else if(event.equalsIgnoreCase("burner_action"))
		{
			if(FIGHTHING && npc != null)
			{
				L2Npc guard = addSpawn(GUARD, npc);
				if(guard != null)
				{
					guards.add(guard);
					startQuestTimer("guard_action", 500, guard, null);
				}
				startQuestTimer("burner_action", BURNER_ACTION_TIME, npc, null);
			}
		}
		else if(event.equalsIgnoreCase("guard_action"))
		{
			if(FIGHTHING && npc != null && !npc.isDead())
			{
				if(targets.containsKey(npc))
				{
					L2PcInstance target = targets.get(npc);
					if(target != null && target.isOnline() && target.isInsideRadius(npc, 5000, false, false))
					{
						npc.setIsRunning(true);
						npc.setTarget(target);

						if(target.isInsideRadius(npc, 200, false, false))
						{
							npc.doCast(guard_skill.getSkill());
						}
						else
						{
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
						}
					}
					else
					{
						// If player is not online anymore or he is not in 5k range
						npc.getLocationController().delete();
						if(targets.containsKey(npc))
						{
							targets.remove(npc);
						}
					}
				}
				else
				{
					FastList<L2PcInstance> result = FastList.newInstance();
					L2PcInstance target = null;
					for(L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(3000))
					{
						if(pl == null || pl.isAlikeDead())
						{
							continue;
						}
						if(pl.isGM() || pl.getAppearance().getInvisible())
						{
							continue;
						}
						if(pl.isInsideRadius(npc, 3000, true, false) && GeoEngine.getInstance().canSeeTarget(npc, pl))
						{
							result.add(pl);
						}
					}
					if(!result.isEmpty())
					{
						target = result.get(Rnd.get(result.size() - 1));
					}
					if(target != null)
					{
						npc.setTitle(target.getName());
						npc.broadcastPacket(new NpcInfo(npc));
						npc.setIsRunning(true);
						targets.put(npc, target);
					}
					FastList.recycle(result);
				}
				startQuestTimer("guard_action", GUARD_ACTION_TIME, npc, null);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onNpcDie(L2Npc npc, L2Character killer)
	{
		if(npc.getNpcId() == ANAIS)
		{
			stopFight();
		}

		return super.onNpcDie(npc, killer);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc.getNpcId() == GUARD)
		{
			if(guards.contains(npc))
			{
				guards.remove(npc);
			}

			npc.doDie(npc);
			npc.getLocationController().delete();
		}
		return super.onSpellFinished(npc, player, skill);
	}

	private void checkBurnerStatus(L2Npc anais)
	{
		synchronized(this)
		{
			switch(BURNERS_ENABLED)
			{
				case 0:
					enableBurner(1);
					BURNERS_ENABLED = 1;
					break;
				case 1:
					if(anais.getCurrentHp() <= anais.getMaxHp() * 0.750000)
					{
						enableBurner(2);
						BURNERS_ENABLED = 2;
					}
					break;
				case 2:
					if(anais.getCurrentHp() <= anais.getMaxHp() * 0.500000)
					{
						enableBurner(3);
						BURNERS_ENABLED = 3;
					}
					break;
				case 3:
					if(anais.getCurrentHp() <= anais.getMaxHp() * 0.250000)
					{
						enableBurner(4);
						BURNERS_ENABLED = 4;
					}
					break;
			}
		}
	}

	private void enableBurner(int index)
	{
		if(!burners.isEmpty())
		{
			L2Npc burner = burners.get(index - 1);
			if(burner != null)
			{
				burner.setDisplayEffect(1);
				startQuestTimer("burner_action", 1000, burner, null);
			}
		}
	}

	private void spawnBurners()
	{
		for(int[] SPAWN : BURNERS)
		{
			L2Npc npc = addSpawn(18915, SPAWN[0], SPAWN[1], SPAWN[2], 0, false, 0);
			if(npc != null)
			{
				npc.setTargetable(false);
				burners.add(npc);
			}
		}
	}

	private void stopFight()
	{
		FIGHTHING = false;
		BURNERS_ENABLED = 0;

		cancelQuestTimers("guard_action");
		cancelQuestTimers("burner_action");

		if(!targets.isEmpty())
		{
			targets.clear();
		}

		if(!burners.isEmpty())
		{
			burners.stream().filter(burner -> burner != null).forEach(burner -> burner.setDisplayEffect(2));
		}

		if(!guards.isEmpty())
		{
			guards.stream().filter(guard -> guard != null).forEach(guard -> guard.getLocationController().delete());
		}
	}
}