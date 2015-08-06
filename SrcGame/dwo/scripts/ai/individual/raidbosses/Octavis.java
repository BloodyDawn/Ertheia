package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.instances.RB_Octavis;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class Octavis extends Quest
{
	private static final Location LAIR_CENTER = new Location(207190, 120574, -10009);

	private static final int VOLCANO_NPC = 19161;
	private static final int OCTAVIS_POWER_NPC = 18984;

	private static final int OCTAVIS_LIGHT_FIRST = 29191;
	private static final int OCTAVIS_LIGHT_BEAST = 29192;
	private static final int OCTAVIS_LIGHT_SECOND = 29193;
	private static final int OCTAVIS_LIGHT_THIRD = 29194;

	private static final int OCTAVIS_HARD_FIRST = 29209;
	private static final int OCTAVIS_HARD_BEAST = 29210;
	private static final int OCTAVIS_HARD_SECOND = 29211;
	private static final int OCTAVIS_HARD_THIRD = 29212;

	private static final int CHAIN_STRIKE = 10015;
	private static final int CHAIN_HYDRA = 10016;

	private static final SkillHolder VOLCANO_ZONE = new SkillHolder(14025, 1);
	private static final SkillHolder OCTAVIS_POWER1 = new SkillHolder(14028, 1);
	private static final SkillHolder OCTAVIS_POWER2 = new SkillHolder(14029, 1);
	private static final SkillHolder BEAST_HERO_MOVEMENT = new SkillHolder(14023, 1);
	private static final SkillHolder BEAST_ANCIENT_POWER = new SkillHolder(14024, 1);
	private static final SkillHolder OCTAVIS_RAIN_OF_ARROWS = new SkillHolder(14285, 1);

	private static final int[][] OCTAVIS_MOVE_POINTS = {
		{207313, 120584, -10008}, {207641, 120626, -10008}, {208088, 120619, -10008}, {207988, 120926, -10014},
		{207544, 121363, -10014}, {206856, 121378, -10014}, {206407, 120949, -10014}, {206365, 120275, -10014},
		{206842, 119771, -10014}, {207488, 119759, -10014}, {207966, 120223, -10014},
	};

	public Octavis()
	{
		addSpawnId(VOLCANO_NPC, OCTAVIS_POWER_NPC);

		addSpawnId(OCTAVIS_LIGHT_FIRST, OCTAVIS_HARD_FIRST);
		addSpawnId(OCTAVIS_LIGHT_SECOND, OCTAVIS_HARD_SECOND);
		addSpawnId(OCTAVIS_LIGHT_THIRD, OCTAVIS_HARD_THIRD);
		addSpawnId(OCTAVIS_LIGHT_BEAST, OCTAVIS_HARD_BEAST);

		addAttackId(OCTAVIS_LIGHT_FIRST, OCTAVIS_HARD_FIRST);
		addAttackId(OCTAVIS_LIGHT_SECOND, OCTAVIS_HARD_SECOND);
		addAttackId(OCTAVIS_LIGHT_THIRD, OCTAVIS_HARD_THIRD);

		addSkillSeeId(OCTAVIS_LIGHT_BEAST, OCTAVIS_HARD_BEAST);
	}

	public static void main(String[] args)
	{
		new Octavis();
	}

	/**
	 * Находит одного из Октависов в инстансе.
	 *
	 * @param instance Инстанс зона.
	 * @return octavis
	 */
	private L2Npc findOctavis(Instance instance)
	{
		L2Npc octavis = null;
		for(L2Npc instanceNpc : instance.getNpcs())
		{
			int npcId = instanceNpc.getNpcId();
			if(npcId == OCTAVIS_LIGHT_FIRST || npcId == OCTAVIS_HARD_FIRST ||
				npcId == OCTAVIS_LIGHT_SECOND || npcId == OCTAVIS_HARD_SECOND ||
				npcId == OCTAVIS_LIGHT_THIRD || npcId == OCTAVIS_HARD_THIRD)
			{
				octavis = instanceNpc;
				break;
			}
		}

		return octavis;
	}

	private boolean isOctavisBeast(int npcId)
	{
		return npcId == OCTAVIS_LIGHT_BEAST || npcId == OCTAVIS_HARD_BEAST;
	}

	private boolean isOctavisBeast(L2Npc npc)
	{
		return isOctavisBeast(npc.getNpcId());
	}

	/**
	 * Задача передвижения первой тушки Октависа по кругу. Двигаемся зверями, коляску Октависа цепляем следом.
	 *
	 * @param npc
	 */
	private void startMovement(final L2Npc npc)
	{
		RB_Octavis.OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Octavis.OctavisWorld.class);
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		if(world == null || instance == null)
		{
			return;
		}

		int npcId = npc.getNpcId();

		if(npcId != OCTAVIS_LIGHT_FIRST && npcId != OCTAVIS_HARD_FIRST)
		{
			return;
		}

		// Ищем зверей Октависа
		L2Npc beast = null;

		for(L2Npc instanceNpc : instance.getNpcs())
		{
			int lookupNpcId = instanceNpc.getNpcId();
			if(isOctavisBeast(lookupNpcId))
			{
				beast = instanceNpc;
				break;
			}
		}

		final L2Npc finalizedBeast = beast;

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if(finalizedBeast.getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO && finalizedBeast != null)
				{
					int selectedIndex = -1;
					for(int i = 2; i < OCTAVIS_MOVE_POINTS.length; ++i)
					{
						int[] point = OCTAVIS_MOVE_POINTS[i];
						if(selectedIndex < 0)
						{
							selectedIndex = i;
						}
						else
						{
							double selectedDistance = Util.calculateDistance(finalizedBeast.getX(), finalizedBeast.getY(), OCTAVIS_MOVE_POINTS[selectedIndex][0], OCTAVIS_MOVE_POINTS[selectedIndex][1]);
							double currentDistance = Util.calculateDistance(finalizedBeast.getX(), finalizedBeast.getY(), point[0], point[1]);

							if(currentDistance < selectedDistance)
							{
								selectedIndex = i;
							}
						}
					}

					++selectedIndex;
					if(selectedIndex >= OCTAVIS_MOVE_POINTS.length)
					{
						selectedIndex = 2;
					}

					Location loc = new Location(OCTAVIS_MOVE_POINTS[selectedIndex][0], OCTAVIS_MOVE_POINTS[selectedIndex][1], OCTAVIS_MOVE_POINTS[selectedIndex][2]);

					finalizedBeast.setRunning();
					finalizedBeast.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
				}

				double angle = Util.convertHeadingToDegree(finalizedBeast.getHeading());
				double radians = Math.toRadians(angle);
				double radius = 120;
				int x = (int) (Math.cos(Math.PI + radians - 0) * radius);
				int y = (int) (Math.sin(Math.PI + radians - 0) * radius);

				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(finalizedBeast.getX() + x, finalizedBeast.getY() + y, finalizedBeast.getZ()));

				ThreadPoolManager.getInstance().scheduleGeneral(this, 50);
			}
		}, 50);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		RB_Octavis.OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Octavis.OctavisWorld.class);
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		if(world == null || instance == null)
		{
			return null;
		}

		L2Npc octavis = findOctavis(instance);

		if(octavis == null)
		{
			return null;
		}

		// Первая и вторая тушка являются неубиваемыми, при достижении уровня HP близкого к zero, убираем их и сообщаем об этом скрипту инстанса.
		if((npc.getNpcId() == OCTAVIS_LIGHT_BEAST || npc.getNpcId() == OCTAVIS_HARD_BEAST) && world.status == 1)
		{
			double hp = npc.getCurrentHp() / npc.getMaxHp();
			if(hp <= 0.5 && octavis.getDisplayEffect() == 0)
			{
				int effect = (int) (hp * 10);
				if(effect < 5 && effect > 0)
				{
                    octavis.setDisplayEffect(effect);
				}
				else if(effect == 0)
				{
                    octavis.setDisplayEffect(0x05);
				}
			}
			else if(npc.getCurrentHp() / npc.getMaxHp() <= 0.01)
			{
                octavis.setDisplayEffect(0x06);
                octavis.setDisplayEffect(0x00);
				RB_Octavis.getInstance().nextSpawn(world);
				npc.getLocationController().delete();
			}
			else if(npc.getCurrentHp() / npc.getMaxHp() > 0.5 && octavis.getDisplayEffect() == 0x01)
			{
                octavis.setDisplayEffect(0x06);
                octavis.setDisplayEffect(0x00);
			}
		}
		else if((npc.getNpcId() == OCTAVIS_LIGHT_SECOND || npc.getNpcId() == OCTAVIS_HARD_SECOND) && npc.getCurrentHp() / npc.getMaxHp() <= 0.01 && world.status == 2)
		{
			RB_Octavis.getInstance().nextSpawn(world);
			npc.getLocationController().delete();
		}
		else if((npc.getNpcId() == OCTAVIS_LIGHT_THIRD || npc.getNpcId() == OCTAVIS_HARD_THIRD) && npc.getCurrentHp() / npc.getMaxHp() <= 0.01 && world.status == 3)
		{
			npc.doDie(attacker);
			npc.getLocationController().decay();
			RB_Octavis.getInstance().nextSpawn(world);
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	/**
	 * Что корейцы курили - хз.
	 * Когда танк использует цепную гидру или цепной удар, зверь с некоторым шансом может сагриться.
	 * В это время бег по кругу останавливается и зверь юзает скиллы. Через некоторое время зверь опять начинает бегать.
	 *
	 * @param npc
	 * @param caster
	 * @param skill
	 * @param targets
	 * @param isPet
	 * @return
	 */
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(isOctavisBeast(npc) && (skill.getId() == CHAIN_STRIKE || skill.getId() == CHAIN_HYDRA))
		{
			if(Rnd.getChance(40))
			{
				if(Rnd.getChance(50))
				{
					npc.doCast(BEAST_HERO_MOVEMENT.getSkill());
				}
				else
				{
					npc.doCast(BEAST_ANCIENT_POWER.getSkill());
				}
			}
		}

		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		npc.setIsRunning(true);

		if(npc.getNpcId() == VOLCANO_NPC || npc.getNpcId() == OCTAVIS_POWER_NPC)
		{
			npc.setIsInvul(true);
			return null;
		}

		// Коляска Октависа неуязвима (поначалу)
		if(npc.getNpcId() == OCTAVIS_LIGHT_FIRST || npc.getNpcId() == OCTAVIS_HARD_FIRST)
		{
			npc.setIsInvul(true);
            npc.setIsOctavisRaid(true);
            
            _log.info(getClass().getSimpleName() + "state" + npc.isOctavisRaid());

            // Постоянно кастует Дождь Стрел
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					RB_Octavis.OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Octavis.OctavisWorld.class);

					if(world != null && world.volcanos != null && world.status == 1)
					{
						for(L2Npc volcano : world.volcanos)
						{
							world.playersInLairZone.stream().filter(player -> Rnd.get() <= 0.25).forEach(player -> {
								volcano.teleToInstance(player.getLoc(), world.instanceId);
								volcano.setTarget(volcano);
								volcano.doCast(VOLCANO_ZONE.getSkill());
							});
						}

						for(L2PcInstance player : world.playersInLairZone)
						{
							if(Rnd.get() <= 0.1)
							{
								npc.setTarget(player);
								npc.doCast(OCTAVIS_RAIN_OF_ARROWS.getSkill());
								break;
							}
						}
					}

					ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
				}
			}, 5000);
		}

		if(npc.getNpcId() != OCTAVIS_LIGHT_THIRD && npc.getNpcId() != OCTAVIS_HARD_THIRD)
		{
			npc.setIsMortal(false);
		}

		if(npc.getNpcId() == OCTAVIS_LIGHT_THIRD || npc.getNpcId() == OCTAVIS_HARD_THIRD)
		{
			npc.setIsMortal(false);
			// Постоянно кастует всякую гадость
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					RB_Octavis.OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Octavis.OctavisWorld.class);

					if(world != null && world.volcanos != null && world.status == 3)
					{
						if(Rnd.getChance(50))
						{
							L2Npc volcano = world.volcanos.get(0);
							volcano.teleToInstance(LAIR_CENTER, world.instanceId);
							SkillHolder skill = Rnd.getChance(50) ? OCTAVIS_POWER1 : OCTAVIS_POWER2;
							volcano.doCast(skill.getSkill());

							if(skill.equals(OCTAVIS_POWER1))
							{
								world.octavisPower.teleToInstance(LAIR_CENTER, world.instanceId);
								world.octavisPower.setDisplayEffect((world.octavisPower.getDisplayEffect() + 1) % 7);
							}
						}
					}

					ThreadPoolManager.getInstance().scheduleGeneral(this, 60000);
				}
			}, 60000);
		}

		// Для зверя стартуем таск на проверку уровня HP. Если HP < 50%, то можно бить Октависа, убираем инвуль.
		if(isOctavisBeast(npc))
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					boolean canBeKilled = false;
					if(npc.getCurrentHp() / npc.getMaxHp() < 0.5)
					{
						canBeKilled = true;
					}

					Instance instance = InstanceManager.getInstance().getInstance(npc.getInstanceId());
					if(instance == null)
					{
						return;
					}

					L2Npc octavis = findOctavis(instance);
					if(octavis != null)
					{
						if(octavis.isInvul())
						{
							octavis.setIsInvul(!canBeKilled);
						}

						if(npc != null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(this, 3000);
						}
					}
				}
			}, 3000);
		}

		startMovement(npc);

		return null;
	}
}