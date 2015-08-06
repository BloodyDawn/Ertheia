package dwo.scripts.ai.zone;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * @author ANZO, Bacek
 * Date: 14.03.12
 * Time: 0:47
 * TODO: http://www.youtube.com/watch?v=c3c89vgZFMw&feature=youtu.be
 */

public class OrbisTemple extends Quest
{
	// Монстры в локации
	private static final int[] Жертва_Орбиса = {22911, 22912, 22913, 25833};
	private static final int Жертва_Орбиса_Проклятый = 18978;

	private static final int[] Страж_Орбиса = {22914, 22915, 22916, 25834};
	private static final int Страж_Орбиса_Проклятый = 18979;

	private static final int[] Метатель_Орбиса = {22917, 22918, 22919, 25835, 22920};    // TODO     22920    Разведчик
	private static final int Метатель_Орбиса_Проклятый = 18980; // Воин Ближнего боя

	private static final int[] Ученый_Орбиса = {22921, 22922, 22923, 25836};
	private static final int Ученый_Орбиса_Проклятый = 18981;

	private static final int[] Древний_Герой_Орбис = {22924, 22925};
	private static final int Древний_Герой_Орбис_Проклятый = 18982;

	private static final int[] Старший_Ученый_Орбиса = {22926, 22927};
	private static final int Старший_Ученый_Орбиса_Проклятый = 18983;

	private static final int[] Проклятые = {
		Жертва_Орбиса_Проклятый, Страж_Орбиса_Проклятый, Метатель_Орбиса_Проклятый, Ученый_Орбиса_Проклятый,
		Древний_Герой_Орбис_Проклятый, Старший_Ученый_Орбиса_Проклятый
	};

	// Монстры, заспауненные от статуй героев
	private static final Map<Integer, FastList<L2Npc>> _heroesMinionSpawnList = new HashMap<>();

	/**
	 * Арчеры, которые ходят по комнатам и агрятся
	 */
	private static final int SCOUT_WATCHER1 = 22917;
	private static final int SCOUT_WATCHER2 = 22918;
	private static final int SCOUT_WATCHER3 = 22919;

	private static final int[][] WATCHER_LOCATIONS = {
		// 1й этаж
		{206800, 54650, -8548}, {210667, 54679, -8676}, {211247, 54664, -8676}, {215920, 54659, -8676},
	};

	public OrbisTemple()
	{

		addSpawnId(Жертва_Орбиса);
		addSpawnId(Страж_Орбиса);
		addSpawnId(Метатель_Орбиса);
		addSpawnId(Ученый_Орбиса);
		addSpawnId(Древний_Герой_Орбис);
		addSpawnId(Старший_Ученый_Орбиса);
		addSpawnId(SCOUT_WATCHER1, SCOUT_WATCHER2);

		addAttackId(Жертва_Орбиса);
		addAttackId(Страж_Орбиса);
		addAttackId(Метатель_Орбиса);
		addAttackId(Ученый_Орбиса);
		addAttackId(Древний_Герой_Орбис);
		addAttackId(Старший_Ученый_Орбиса);

		// TODO addEventId(HookType.ON_SEE_PLAYER);

		// TODO addKillId(Древний_Герой_Орбис);

		// TODO addAggroRangeEnterId(Древний_Герой_Орбис);
	}

	public static void main(String[] args)
	{
		new OrbisTemple();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		// Вызываем стражу если это Древний_Герой_Орбис
		// TODO calledGuard(npc, attacker);

		// Включаем анимацию мобам ( на оффе дается пуха )
		if(ArrayUtils.contains(Метатель_Орбиса, npc.getNpcId()))
		{
			npc.setRHandId(17372); //Даем копья
		}
		else
		{
			if(!ArrayUtils.contains(Древний_Герой_Орбис, npc.getNpcId()))
			{
				npc.setRHandId(15280); //Всем остальным меч
			}
		}
		// Рандомно спауним Проклятых мобов
		if(!ArrayUtils.contains(Проклятые, npc.getNpcId()))
		{
			if(Rnd.getChance(0.005))  //  0.005%
			{
				npc.getLocationController().delete();
				spawnDamn(npc.getNpcId(), npc.getLoc(), attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(ArrayUtils.contains(Древний_Герой_Орбис, npc.getNpcId()))
		{
			if(_heroesMinionSpawnList != null && _heroesMinionSpawnList.containsKey(npc.getObjectId()))
			{
				_heroesMinionSpawnList.remove(npc.getObjectId());
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		// Убераем движение рамдомно у Мобы
		npc.setIsNoRndWalk(true);
		// Убераем рамдомную анимацию
		npc.setIsNoAnimation(true);
		// 22917
		if(npc.getNpcId() == SCOUT_WATCHER1)
		{
			// Даем копье
			npc.setRHandId(17372);

		}
		// 22918
		else if(npc.getNpcId() == SCOUT_WATCHER2)
		{
			// Даем копье
			npc.setRHandId(17372);

			WalkingManager.getInstance().startMoving(npc, 50);
			WalkingManager.getInstance().startMoving(npc, 51);
		}
		// 22919
		else if(npc.getNpcId() == SCOUT_WATCHER3)
		{
			// Даем копье
			npc.setRHandId(17372);

			WalkingManager.getInstance().startMoving(npc, 52);
			WalkingManager.getInstance().startMoving(npc, 53);
		}

		//Даем копье
		//npc.setRHandId(17372);

		/*
		if (ArrayUtils.contains(Древний_Герой_Орбис, npc.getNpcId()))
		{
			npc.getKnownList().setDistanceToWatch(150); // TODO: Unhardcode
			npc.startWatcherTask(150);
		}
        */
		// TODO
		/*
		if (SCOUT_WATCHER == npc.getNpcId())
		{
			//Даем копье
			npc.setRHandId(17372);

			// Двигаем охранников
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
			{
				@Override
				public void run()
				{
					// Выходим, если NPC заагрен, или уже перемещается
					if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO ||
							npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK ||
							npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST)
					{
						return;
					}

					Location currentLocation = npc.getLoc();
					int[] loc = WATCHER_LOCATIONS[0];
					for (int[] coords : WATCHER_LOCATIONS)
					{
						if (coords != loc)
						{
							double distanceToOldPoint = npc.getPlanDistanceSq(loc[0], loc[1]);
							double distanceToNewPoint = npc.getPlanDistanceSq(coords[0], coords[1]);

							// Пропускаем точку, если она дальше, чем старая и если NPC уже стоит в этой точке
							if (distanceToNewPoint < distanceToOldPoint && !(currentLocation.getX() == coords[0] && currentLocation.getY() == coords[1] && currentLocation.getZ() == coords[2]))
							{
								loc = coords;
							}
						}
					}

					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(loc[0], loc[1], loc[2]));
				}
			}, 0, 5000);
		}
		*/
		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == SCOUT_WATCHER1 || npc.getNpcId() == SCOUT_WATCHER2 || npc.getNpcId() == SCOUT_WATCHER3)
		{
			npc.getKnownList().getKnownNpcInRadius(600).stream().filter(L2Object::isMonster).forEach(character -> ((L2MonsterInstance) character).addDamageHate(player, 999, 1));
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public void spawnDamn(int npcId, Location loc, L2PcInstance attacker)
	{
		int проклятый = Жертва_Орбиса_Проклятый; // Заглушка от нпе

		if(ArrayUtils.contains(Жертва_Орбиса, npcId))
		{
			проклятый = Жертва_Орбиса_Проклятый;
		}
		else if(ArrayUtils.contains(Страж_Орбиса, npcId))
		{
			проклятый = Жертва_Орбиса_Проклятый;
		}
		else if(ArrayUtils.contains(Метатель_Орбиса, npcId))
		{
			проклятый = Метатель_Орбиса_Проклятый;
		}
		else if(ArrayUtils.contains(Ученый_Орбиса, npcId))
		{
			проклятый = Ученый_Орбиса_Проклятый;
		}
		else if(ArrayUtils.contains(Древний_Герой_Орбис, npcId))
		{
			проклятый = Древний_Герой_Орбис_Проклятый;
		}
		else if(ArrayUtils.contains(Старший_Ученый_Орбиса, npcId))
		{
			проклятый = Старший_Ученый_Орбиса_Проклятый;
		}

		L2Npc mob = addSpawn(проклятый, loc, 0, false, 0);
		mob.getAttackable().attackCharacter(attacker);
		ThreadPoolManager.getInstance().scheduleGeneral(new DespawnTask(mob, 60000), 5000);
	}

	public void calledGuard(L2Npc npc, L2PcInstance player)
	{
		// Спавним 6 мобов вокруг
		if(ArrayUtils.contains(Древний_Герой_Орбис, npc.getNpcId()) && !_heroesMinionSpawnList.containsKey(npc.getObjectId()))
		{
			int mobs = 25835;       // Метатель Орбиса
			if(Rnd.getChance(20))
			{
				mobs = 25834;       //Страж Орбиса
			}
			FastList<L2Npc> mobList = new FastList<>();
			for(int i = 0; i <= 5; i++)
			{
				if(Rnd.getChance(10))    // 10% то что будет проклетый
				{
					spawnDamn(mobs, npc.getLoc(), player);
				}
				else
				{
					mobList.add(addSpawn(mobs, npc.getLoc()));
				}
			}

			int i = 1;
			for(L2Npc m : mobList)
			{
				m.setRunning();
				int finalizedAngle = i;
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					// Включаем анимацию мобам ( на оффе дается пуха )
					if(ArrayUtils.contains(Метатель_Орбиса, m.getNpcId()))
					{
						m.setRHandId(17372); //Даем копья
					}
					else
					{
						m.setRHandId(15280); //Всем остальным меч
					}

					int x = m.getX() + (int) (Math.cos(finalizedAngle * Math.PI) * 90);
					int y = m.getY() + (int) (Math.sin(finalizedAngle * Math.PI) * 90);
					m.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, m.getZ()));
				}, 1000);

				i++;
			}
			_heroesMinionSpawnList.put(npc.getObjectId(), mobList);
		}
	}

	public class DespawnTask implements Runnable
	{
		private L2Npc _npc;
		private long _delay;
		private long _spawnTime;

		public DespawnTask(L2Npc npc, long delay)
		{
			_npc = npc;
			_delay = delay;
			_spawnTime = System.currentTimeMillis();
		}

		@Override
		public void run()
		{
			if(_npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK && System.currentTimeMillis() < _spawnTime + _delay)
			{
				_npc.getLocationController().delete();
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
			}
		}
	}
}