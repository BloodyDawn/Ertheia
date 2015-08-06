package dwo.scripts.ai.group_template;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import dwo.scripts.instances.Fortuna;
import javolution.util.FastList;

import java.util.List;
import java.util.concurrent.Future;

/**
 * L2GOD Team
 * User: Yorie
 * Date: 10.10.11
 * Time: 10:42
 */

public class FortunaMonsters extends Quest
{
	public static final int LIGHTNING_SPHERE_BLUE = 19082;
	public static final int LIGHTNING_SPHERE_ORANGE = 19083;
	public static final int SEALED_WARRIOR = 23076;
	public static final int SEALED_MAGE = 23078;
	public static final int RAGE_WITHOUT_NAME = 23077;
	private static LightningSearchTask _lightningSearchTask;
	private static Future<?> _lightningSearchFuture;

	public FortunaMonsters()
	{

		addSpawnId(SEALED_WARRIOR, RAGE_WITHOUT_NAME, SEALED_MAGE);
		addAttackId(LIGHTNING_SPHERE_BLUE, LIGHTNING_SPHERE_ORANGE);
	}

	public static void main(String[] args)
	{
		new FortunaMonsters();
	}

	public void spawnOrangeLight(L2Npc blueLight)
	{
		addSpawn(LIGHTNING_SPHERE_ORANGE, blueLight.getX(), blueLight.getY(), blueLight.getZ(), blueLight.getHeading(), false, 0, false, blueLight.getInstanceId());
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		synchronized(this)
		{
			if(!npc.isInvul() && npc.getNpcId() == LIGHTNING_SPHERE_BLUE)
			{
				npc.setIsInvul(true);
				((L2MonsterInstance) npc).useMagic(SkillTable.getInstance().getInfo(14255, 1));
				for(L2Object object : WorldManager.getInstance().getVisibleObjects(npc, 150))
				{
					if(object instanceof L2MonsterInstance && (((L2MonsterInstance) object).getNpcId() == SEALED_WARRIOR || ((L2MonsterInstance) object).getNpcId() == SEALED_MAGE))
					{
						int npcReplacerCount = Rnd.get(3, 5);
						for(int i = 0; i < npcReplacerCount; ++i)
						{
							addSpawn(RAGE_WITHOUT_NAME, object.getX(), object.getY(), object.getZ(), 0, true, 0, false, object.getInstanceId());
						}
						((L2MonsterInstance) object).getLocationController().delete();
					}
				}
				npc.getLocationController().delete();
			}
			else if(npc.getNpcId() == LIGHTNING_SPHERE_ORANGE)
			{
				((L2MonsterInstance) npc).useMagic(SkillTable.getInstance().getInfo(14255, 1));
			}

			return null;
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == SEALED_WARRIOR || npc.getNpcId() == SEALED_MAGE)
		{
			if(_lightningSearchTask == null)
			{
				_lightningSearchTask = new LightningSearchTask();
				_lightningSearchFuture = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(_lightningSearchTask, 5000, 8000);
			}
			_lightningSearchTask.addMonster((L2MonsterInstance) npc);
		}

		return null;
	}

	private class LightningSearchTask implements Runnable
	{
		private List<L2MonsterInstance> _monsters = new FastList<>();

		public void addMonster(L2MonsterInstance monster)
		{
			_monsters.add(monster);
		}

		@Override
		public void run()
		{
			// Тут начинается адовый таск по пожиранию синих виспов в Фортуне
			// Дергаем всех зарегистрированных Скованных Воителей
			if(_monsters.isEmpty())
			{
				_lightningSearchFuture.cancel(false);
				_lightningSearchFuture = null;
			}
			else
			{
				for(final L2Npc monster : _monsters)
				{
					if(monster != null && !monster.isDead())
					{
						boolean foundLightning = false;
						// Ищем Виспы, если моб не заагрен, либо с вероятностью 10% отвлекаемся от игроков
						if(((L2MonsterInstance) monster).getAggroList().isEmpty() || Rnd.get() <= 0.1)
						{
							for(final L2Object object : WorldManager.getInstance().getVisibleObjects(monster, 900))
							{
								if(object instanceof L2MonsterInstance && ((L2MonsterInstance) object).getNpcId() == LIGHTNING_SPHERE_BLUE)
								{
									monster.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, object.getLoc());

									// Придется постоянно проверять дистанцию до объекта (Синего Виспа), чтобы захавать его :)
									ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
									{
										@Override
										public void run()
										{
											synchronized(object)
											{
												if(!((L2MonsterInstance) object).isInvul() && monster.getPlanDistanceSq(object) < 150)
												{
													((L2MonsterInstance) object).setIsInvul(true);
													spawnOrangeLight((L2Npc) object);
													((L2Npc) object).getLocationController().delete();
												}
												else if(monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
												{
													ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
												}
											}
										}
									}, 1000);
									foundLightning = true;
									break;
								}
							}
						}

						if(((L2MonsterInstance) monster).getAggroList().isEmpty() && !foundLightning)
						{
							Fortuna.FortunaWorld world = InstanceManager.getInstance().getInstanceWorld(monster, Fortuna.FortunaWorld.class);
							if(world != null && !world.playersInside.isEmpty())
							{
								((L2MonsterInstance) monster).attackCharacter(world.playersInside.get(Rnd.get(world.playersInside.size())));
							}
						}
					}
				}
			}
		}
	}
}
