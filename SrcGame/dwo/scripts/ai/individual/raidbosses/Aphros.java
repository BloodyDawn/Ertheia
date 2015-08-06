package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2EffectZone;
import dwo.gameserver.util.Rnd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 15.04.12
 * Time: 18:56
 */

/**
 * Основная логика Афроса:
 * при 80% хп спавнит дву штуки 25865 и включает какую то зону (включает зону 1)
 * при 60% хп спавнит 3 штук 25865
 * при 40% еще 4 штуки таких же спавнит
 * при 20% спавнит 25866   (когда 25866 спавнит, тот его деспавнит выставляет себе 10% хп, выключает первую зону - включает вторую)
 * при 10% включает проверку, и пробивает при каждой атаке если не в своей территории то делает ТП на спавн
 * если его 10 минут не бить, то он деспавнится, а все дормены ТПехаются вверх на +1095 по Z
 * через какое-то время двери возвращаются на место (_реинициализацияРейда)
 */

public class Aphros extends Quest
{
	// Рейдовые босы
	private static final int Афрос = 25775;
	private static final int Афрос10 = 25866;

	// Миньоны Афроса
	private static final int МиньонАфроса = 25865;
	private static final Location КоординатыАфроса = new Location(213732, 115288, -856);
	// в первой зоне 14464-1 дебафф, во второй 14624-1
	private static final L2EffectZone _зонаЭффектов = (L2EffectZone) ZoneManager.getInstance().getZoneById(1200581);
	private static final int[] ДвериНпц = {
		33133, 33134, 33135, 33136
	};
	private static final Location[] КоординатыДверей = {
		new Location(213700, 115925, -864), new Location(214354, 115265, -864), new Location(213700, 114595, -800),
		new Location(213030, 115265, -800)
	};
	private static final int[] Двери = {
		26210041, 26210042, 26210043, 26210044
	};
	private static final int ХранительСада = 25776;
	private static final int КлючАфроса = 17373;
	private static final List<L2Npc> АктивныеДвери = new ArrayList<>();
	// Таски
	protected ScheduledFuture<?> _проверкаНаАктивность;
	protected ScheduledFuture<?> _реинициализацияРейда;
	private L2Npc АфросНПЦ;
	private int RaidHpState;

	public Aphros()
	{
		addAskId(ДвериНпц, -502);
		addAskId(ДвериНпц, -503);
		addAskId(ДвериНпц, -504);
		addAskId(ДвериНпц, -505);
		addAttackId(Афрос, Афрос10);
		initializeRaid();
	}

	public static void main(String[] args)
	{
		new Aphros();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.isDead())
		{
			return null;
		}

		/**
		 * при 80% хп спавнит дву штуки 25865 и включает какую то зону (включает зону 1)
		 * при 60% хп спавнит 3 штук 25865
		 * при 40% еще 4 штуки таких же спавнит
		 * при 20% спавнит 25866   (когда 25866 спавнит, тот его деспавнит выставляет себе 10% хп, выключает первую зону - включает вторую)
		 * при 10% включает проверку, и пробивает при каждой атаке если не в своей территории то делает ТП на спавн
		 */
		if(npc.getNpcId() == Афрос)
		{
			double currentHp = npc.getCurrentHp();
			if(currentHp <= 80 && currentHp > 60 && RaidHpState == 0)
			{
				spawnMinions(2);
				_зонаЭффектов.setEnabled(true);
				_зонаЭффектов.addSkill(14464, 1);
				RaidHpState++;
			}
			else if(currentHp <= 60 && currentHp > 40 && RaidHpState == 1)
			{
				spawnMinions(3);
				RaidHpState++;
			}
			else if(currentHp <= 40 && currentHp > 20 && RaidHpState == 2)
			{
				spawnMinions(4);
				RaidHpState++;
			}
			else if(currentHp <= 20 && currentHp > 10 && RaidHpState == 3)
			{
				Location oldLoc = АфросНПЦ.getLoc();
				АфросНПЦ.getLocationController().delete();
				АфросНПЦ = addSpawn(Афрос10, oldLoc);
				АфросНПЦ.setCurrentHp(АфросНПЦ.getCurrentHp() / 10);
				_зонаЭффектов.removeSkill(14464);
				_зонаЭффектов.addSkill(14624, 1);
				RaidHpState++;
			}
			// Если Афроса увели дальше чем на 500 юнитов от места спауна - возвращаем его обратно
			else if(currentHp <= 10 && RaidHpState == 4)
			{
				if(_зонаЭффектов.getDistanceToZone(АфросНПЦ.getX(), АфросНПЦ.getY()) > 500)
				{
					АфросНПЦ.teleToLocation(АфросНПЦ.getSpawn().getLoc());
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -502:
			case -503:
			case -504:
			case -505:
				if(reply == 1)
				{
					if(player.getItemsCount(КлючАфроса) > 0)
					{
						if(npc.getNpcId() == 33133)
						{
							player.getVariablesController().unset(getClass().getSimpleName() + "_33134");
							player.getVariablesController().unset(getClass().getSimpleName() + "_33135");
							player.getVariablesController().unset(getClass().getSimpleName() + "_33136");
							player.destroyItemByItemId(ProcessType.NPC, КлючАфроса, 1, npc, true);
							startRaid();
							return "mgg_aphros_door1005.htm";
						}
						else
						{
							if(player.getVariablesController().get(getClass().getSimpleName() + '_' + npc.getNpcId(), Boolean.class, false))
							{
								return npc.getServerName() + "004.htm";
							}
							else
							{
								player.getVariablesController().set(getClass().getSimpleName() + '_' + npc.getNpcId(), true);
								for(int i = 0; i < 3; i++)
								{
									L2Attackable guard = addSpawn(ХранительСада, player.getLoc()).getAttackable();
									guard.attackCharacter(player);
								}
								return npc.getServerName() + "003.htm";
							}
						}
					}
					else
					{
						return npc.getServerName() + "002.htm";
					}
				}
		}
		return null;
	}

	private void startRaid()
	{
		// Открываем все двери
		for(int doorId : Двери)
		{
			DoorGeoEngine.getInstance().getDoor(doorId).openMe();
		}

		// Спауним Афроса
		АфросНПЦ = addSpawn(Афрос, КоординатыАфроса);

		// Стартуем таск на проверку состояния рейда
		_проверкаНаАктивность = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ThinkStopBattleTask(), 100, 600000);
	}

	private void stopRaid()
	{
		// Переносим все двери на островок вверху
		for(L2Npc door : АктивныеДвери)
		{
			door.teleToLocation(door.getX(), door.getY(), door.getZ() + 1095);
		}

		// Отменяем таск на проверку состояния РБ
		if(_проверкаНаАктивность != null)
		{
			_проверкаНаАктивность.cancel(false);
			_проверкаНаАктивность = null;
		}
		RaidHpState = 0;
	}

	/**
	 * Спаун заданного количество миньонов и агр их на рандомного персонажа в KnownList
	 * @param count количество миньонов
	 */
	private void spawnMinions(int count)
	{
		L2Npc minion;
		L2PcInstance[] attackers = (L2PcInstance[]) АфросНПЦ.getKnownList().getKnownPlayersInRadius(900).toArray();
		for(int i = 0; i < count; i++)
		{
			minion = addSpawn(МиньонАфроса, АфросНПЦ.getLoc(), true);
			minion.getAttackable().attackCharacter(attackers[Rnd.get(attackers.length)]);
		}
	}

	private void initializeRaid()
	{
		// Удаляем двери с прошлого рейда
		for(L2Npc door : АктивныеДвери)
		{
			door.teleToLocation(door.getX(), door.getY(), door.getZ() - 1095);
		}

		// Спаун дверей в рандомном порядке
		List<Location> temp = new ArrayList<>(4);
		for(Location loc : КоординатыДверей)
		{
			temp.add(new Location(loc.getX(), loc.getY(), loc.getZ()));
		}

		for(int door : ДвериНпц)
		{
			int index = Rnd.get(0, temp.size() - 1);
			АктивныеДвери.add(addSpawn(door, temp.get(index)));
			temp.remove(index);
		}

		_зонаЭффектов.setEnabled(false);
	}

	private class ThinkStopBattleTask implements Runnable
	{
		@Override
		public void run()
		{
			if(АфросНПЦ != null)
			{
				if(!АфросНПЦ.isInCombat())
				{
					АфросНПЦ.getLocationController().delete();
					stopRaid();
				}
			}
		}
	}
}