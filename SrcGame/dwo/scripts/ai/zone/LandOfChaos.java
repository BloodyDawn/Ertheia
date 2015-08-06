package dwo.scripts.ai.zone;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.FlyToLocation;
import dwo.gameserver.network.game.serverpackets.FlyToLocation.FlyType;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 12.01.13
 * Time: 15:26
 */

/**
 * Кровь с душой - своеобразные нпц которые спавнятся в Землях Хаоса. Если бить этих нпц то с определенным шансом кидается баф дающий ХП, но понижающий скорость передвижения.
 */
public class LandOfChaos extends Quest
{
	// Духи
	private static final int Дух_Зеленый = 19460;
	private static final int Дух_Синий = 19461;
	private static final int Дух_Красный = 19462;
	private static final int Кровь_с_душой = 19463;
	// Странные и непонятные пока что нпц
	private static final int Рега = 19475;
	private static final int Червяк_Смерти = 23336;
	// Остальные нпц в кратере
	private static final int Скалейбл = 23330;
	private static final int Исчезающий_Зомби = 23333;
	private static final int Селроп = 23334;
	private static final int Порус = 23335;
	private static final int[][] SPAWNLIST = {
		/**
		 * Структура:
		 * [0] - npcId;
		 * [1] - min count
		 * [2] - max count
		 * [3] - chance
		 */
		{Скалейбл, 1, 2, 40}, {Исчезающий_Зомби, 1, 2, 40}, {Селроп, 1, 2, 40}, {Порус, 1, 2, 40},
		{Червяк_Смерти, 1, 2, 40}
	};
	private static final int Рог_Хаоса = 23348;
	private static final SkillHolder Кровавая_возможность = new SkillHolder(15537, 2); /* У скилла 2а уровня возможно уровень рандомно вылетает */
	private static final SkillHolder Дух_Зел = new SkillHolder(15569, 1);
	private static TIntIntHashMap _mob = new TIntIntHashMap();
	private static int craterZone1 = 4600090;
	private static int craterZone2 = 4600091;
	private static int craterZone3 = 4600092;
	private static int craterZone4 = 4600093;
	private static int craterZone5 = 4600094;
	private static int craterZone6 = 4600095;
	private static int craterZone7 = 4600096;
	private static int craterZone8 = 4600097;
	private static int craterZone9 = 4600098;
	private static int craterZone10 = 4600099;
	private L2Attackable _mobs;

	public LandOfChaos()
	{
		addAttackId(Дух_Зеленый, Дух_Синий, Дух_Красный);
		addSpawnId(Кровь_с_душой, Дух_Зеленый, Дух_Синий, Дух_Красный, Рега, Червяк_Смерти);
		onSpawnRerun(Кровь_с_душой, Дух_Зеленый, Дух_Синий, Дух_Красный, Рега, Червяк_Смерти);
		addFirstTalkId(Кровь_с_душой);
		addKillId(Рог_Хаоса);

		// Очищаем хеш меп весь раз в 10 минут.
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new clearMobs(), 600000, 600000);

	}

	public static void main(String[] args)
	{
		new LandOfChaos();
	}

	/**
	 * @param npc
	 * @param player
	 * @param damage
	 * @param isPet
	 *
	 * @return
	 */
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(npc == null)
		{
			return super.onAttack(npc, player, damage, isPet);
		}

		int npcId = npc.getNpcId();

		if(npcId == Дух_Красный)
		{
			// Спавнит мобов они сразу нападают на того кто бил Духа.
			if(Rnd.getChance(30))
			{
				for(int[] spawns : SPAWNLIST)
				{
					if(Rnd.getChance(spawns[3]))
					{
						for(int i = 0; i < Rnd.get(spawns[1], spawns[2]); i++)
						{
							/* Еще спавнится невидимый нпц 19467 - Призрачность Земель Хаоса. Судя по всему это НПЦ камера для эффекта призыва. */
							L2Attackable npcs = (L2Attackable) addSpawn(spawns[0], npc.getX() + Rnd.get(100), npc.getY() + Rnd.get(100), npc.getZ(), 0, false, 0, false, player.getInstanceId());
							npcs.attackCharacter(player);
						}
					}
				}
				npc.getLocationController().delete();
			}
		}
		else if(npcId == Дух_Синий)
		{
			if(Rnd.getChance(30))
			{
				if(!_mob.containsValue(player.getObjectId()))
				{
					// Спавнит особых мобов 3 - 4 штуки.
					for(int i = 0; i < Rnd.get(2, 4); i++)
					{
					/* Еще спавнится невидимый нпц 19467 - Призрачность Земель Хаоса. Судя по всему это НПЦ камера для эффекта призыва. */
						//addSpawn(23348, npc.getX() + Rnd.get(100), npc.getY() + Rnd.get(100), npc.getZ(), 0, false, 0, false, player.getInstanceId());
						_mobs = (L2Attackable) addSpawn(Рог_Хаоса, npc.getX() + Rnd.get(100), npc.getY() + Rnd.get(100), npc.getZ(), 0, false, 0, false, player.getInstanceId());
						_mob.put(_mobs.getObjectId(), player.getObjectId());
					}

					/**
					 * Не удалять этот хлам!
					 */
					// За одно запустим очистку хешмепа от мусора, если игрок перестал качаться в локации или вылетел надолго, необходимо во избежании возможных багов, хотя они вроде как исключены
					// т.к. если другие игроки будут убивать Рог Хаоса активированные другими игроками они так же будут удаляться корректно из хешмепа, но на всякий пусть будет? мало ли разрастаться начнет :D
					// Установил чтобы чистка происходила через 10 минут, если будет мало увеличить.
					//_mobsHashClean = ThreadPoolManager.getInstance().scheduleGeneral(new clearMobs(player), 600000);
				}
				npc.getLocationController().delete();
			}
		}
		else if(npcId == Дух_Зеленый) /* Нужно понаблюдать на оффе, есть вероятность что притягивает только определенных мобов! */
		{
			/* Пришлось поиск мобов делать через зоны т.к. через скил с типом скилла TARGET_AURA работает крайне некорректно, кноулисты работают очень нестабильно */
			if(ZoneManager.getInstance().getZoneById(craterZone1).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone1).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone2).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone2).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone3).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone3).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone4).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone4).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone5).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone5).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone6).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone6).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone7).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone7).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone8).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone8).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone9).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone9).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
			else if(ZoneManager.getInstance().getZoneById(craterZone10).isInsideZone(npc) && Rnd.getChance(30))
			{
				/* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
				ZoneManager.getInstance().getZoneById(craterZone10).getCharactersInside().stream().filter(mobs -> mobs.isMonster() && mobs.getAttackable().getNpcId() != Дух_Красный && mobs.getAttackable().getNpcId() != Дух_Синий && mobs.getAttackable().getNpcId() != Дух_Зеленый).forEach(mobs -> {
					if(Rnd.getChance(10))
					{
						npc.doCast(Дух_Зел.getSkill()); /* Если не моежем использовать для всего этого скил, то хотя бы будем его кастовать для визуального эффекта */
						moveToPoint(npc, player, mobs);
					}
				});
				npc.getLocationController().delete();
			}
		}

		return super.onAttack(npc, player, damage, isPet);
	}

	/**
	 *
	 * @param npc
	 * @param player
	 * @param isPet
	 * @return
	 */
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == Рог_Хаоса)
		{
			if(_mob.containsKey(npc.getObjectId()))
			{
				_mob.remove(npc.getObjectId());
			}
		}
		return super.onKill(npc, player, isPet);
	}

	/**
	 * @param npc
	 * @param player
	 *
	 * @return
	 */
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}

		int npcId = npc.getNpcId();

		if(npcId == Кровь_с_душой)
		{
			npc.setTarget(player);
			player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1802306), ExShowScreenMessage.TOP_CENTER, 5000));
			npc.doCast(Кровавая_возможность.getSkill());
			npc.getLocationController().delete();
		}

		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == Рега)
		{
			npc.setIsNoAnimation(true);
		}
		else if(npc.getNpcId() == Червяк_Смерти)
		{
			npc.setIsNoRndWalk(true);
		}
		else if(npc.getNpcId() == Дух_Зеленый || npc.getNpcId() == Дух_Красный || npc.getNpcId() == Дух_Синий)
		{
			npc.setIsInvul(true); /* Делаем духов бессмертными, на оффе я их бил но ХП не понижалось, они просто исчезали рандомно после какого-то кол-ва ударов. */
			npc.setIsNoRndWalk(true);
			npc.setIsCanMove(false);
			npc.setIsNoAttackingBack(true);
		}

		return super.onSpawn(npc);
	}

	/**
	 * @param npc
	 * @param player
	 * @param cha
	 */
	protected void moveToPoint(L2Npc npc, L2PcInstance player, L2Character cha)
	{
		int x;
		int y;
		int z;

		x = npc.getX() + Rnd.get(100);
		y = npc.getY() + Rnd.get(100);
		z = npc.getZ();

		player.broadcastPacket(new FlyToLocation(cha, x, y, z, FlyType.THROW_HORIZONTAL, 800, 0, 0));
		cha.setXYZ(x, y, z);
		cha.broadcastPacket(new ValidateLocation(npc));
	}

	/**
	 *  Не удалять этот хлам!
	 */
	/*
	private class clearMobs implements Runnable
	{
		private final L2PcInstance _player;

		public clearMobs(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			for (int npcObjectId : _mob.keys())
			{
				if (_mob.get(npcObjectId) == _player.getObjectId())
				{
					_mob.remove(npcObjectId);
				}
			}
			//System.out.println("clear out: " + _mob.size());
		}
	}
	*/

	private class clearMobs implements Runnable
	{
		public clearMobs()
		{
			/***/
		}

		@Override
		public void run()
		{
			for(int npcObjectId : _mob.keys())
			{
				_mob.remove(npcObjectId);
			}
		}
	}
}
