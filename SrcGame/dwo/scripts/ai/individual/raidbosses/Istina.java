package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.EventTrigger;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.scripts.instances.RB_Isthina;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/*
 * TODO
 * Истхина:
 * + Барьер отражения (14215) использует с самого начала рейда и примерно каждые 10% снятого ХП.
 * + При этом отображается "Istina spreads the reflecting protective shield".
 * + При прохождении барьера вокруг Истхины появляется красный круг (Display Effect 0x01).
 *
 * + Постоянно использует Death Blow (14219)
 *
 * + Иногда использует Марку Истины (14218). Длится минуту.
 *
 * + Кидает Acid Eruption (14221, 14222, 14223) вокруг себя и пишется сообщение "Powerful eruptive energy spreads from Istina's body".
 * + Скилл скидывает всем вокруг ХП до минимума,
 * + но не убивает. Радиус около 250.
 *
 * + На -30%, -50%, -60%, -65%, -70%, -75%, -80%, -85%, -90% хп кастует на себя Неистовство (14220), который повышает урон и скорость атаки.
 * + При этом отображается сообщение (Istina gets furious and recklessly crazy)
 * + Когда Неистовство сходит, отправляется сообщение "Berserker of Istina has been disabled".
 *
 * + Когда на игрока ложится марка, в центре его экрана появляется сообщение "Istina's Mark shines above the head".
 * + Если Итсхина катсует Blow на игрока, на котором марка, то он получает 10кк урона.
 *
 * + В конце показывается видео, что-то там с катапультой связано.
 * + По центру появляется катапульта.
 * + Идут надписи по центру экрана "After X seconds , the charging magic Ballistas starts.", где X - от 5 до 1.
 * + Потом сообщение "Start charging mana ballista!"
 * + Тут, походу, по баллисте надо долбить любыми магическими скиллами, чтобы она набирала урон.
 * + Вверху показывается таймер. Нужно зачарджить баллисту за 30 секунд.
 * + Потом опять видео как баллиста стреляет в истхину.
 * + По центру появляется NPC Rumiese
 *
 * + По окончанию рейда все получают Shilen's Mark (дается квестом).
 *
 * 1-30% дают 90% сундук из которого достается Р гем, 10% что выпадет вкусняшка. 30-60% 50 на 50 либо сундук с р гемом либо с вкусняшкой, 100% заряд балисты 90% что дадут сундук с вкусняшкой и 10% Р гем
 *
 * + Важно: Заряд баллисты не влияет на получаемый опыт, она нужна для того, чтобы добить Истхину и дает возможность получить дополнительную награду у НПЦ (сундук, из которого чаще всего выпадает 1 R самоцвет, но есть шанс, что выпадет Свиток снятия оков).
 *
 * + Когда появляется сообщение о свечении (красный/синий/зеленый) перед этим показывается сообщение (1811140) и играется звук (SoundFile: istina.istina_voice_01).
 * + Круги делаются пакетов EventTrigger с параметрами 14220101,0x01. Шлется через 10 секунд после надписи. Еще для кругов шлется триггер с ID 14220102.
 * советую оставить браслет Истхины, ибо когда заработает Экстрим Истхина, его можно будет улучшить в Улучшенный Браслет Истхины "Активирует 5 ячеек талисмана. МР +216, ВЫН +1, ДУХ +1. Нельзя обменять/выбросить/модифицировать, положить в личное хранилище.") Квест "Истхина, Мать упадка")
 * + Выброс кислоты не должен убивать
 * + Зарядка катапульты нужна, чтобы получить сундук с R геймом и выполнить квест. Бывает хватает 10%, бывает на 99% мало. Чисто рендом, но чем больше зарядить тем больше шанс получить награду. (мы в 1 пак смогли только на 60%)
 * + Похоже, зависимость шанса идет от того, насколько паверфульно зарядили баллисту. Т.е., заряд баллисты - 60% -> 60% шанс получить сундук.
 *
 * Дебафф от поля "цветочков ебаных" отнимает по 666 ХП у простой Истхины.
 *
 * + 1811138	u,Камень души Истхины начинает излучать ярко-красный свет.\0
 * + 1811139	u,Камень души Истхины начинает излучать ярко-синий свет.\0
 * + 1811140	u,Камень души Истхины начинает излучать ярко-зеленый свет.\0
 * + 1811141	u,Истхина разгневана и теряет контроль над собой.\0
 * + 1811142	u,Еще есть время. Не останавливайтесь.\0
 * + 1811143	u,Еще немного, ваша сила нам поможет.\0
 * + 1811144	u,Истхина призывает своих созданий мощных криком.\0
 *
 * + Проверить рабочесть улучшенного браслета Истины.
 *
 * 7. Выброс кислоты теперь приводит к смерти
 *
 * Эксрим Истхина
 * дебаф от цвета сильнее (2й лвл проявления силы).
 * на обычной: 666 хп за тик и понижение скорости атаки\каста
 * тут: снижение обшего числа хп мп (15к->7к 17к->9к), снижение скорости бега, атаки\каста, 1300+ хп за тик
 * с 80% хп начинает призывать шарики. шарики подвешивают. подвес на 10 мин не клинсится. убирается шприцом по шарику. у шприцов откат.
 * с половины самонит мобов. вроде с каждым разом все больше и больше.
 * опять же примерно с половины с соотвествующей надписью самонит миньенов которые ее ресторят
 * в конце опять же катапульта. упало часть желтого р99кри и блесс ботинки. 0.38%
 *
 * Под конец надо использовать Energy Control Device. Снимает подвес.
 *
 * Экстрим Истхина призывает около 20 Созданий Истхины и 3 Допагена текст "Истхина призывает своих созданий мощным криком".
 *
 * В зону Семя Уничтожения добавлен новый легендарный Рейдовый Босс - Истхина в двух вариантах - обычном и экстремальном. Доступ к новому Рейдовому Боссу осуществляется через НПЦ Офицер Лимиэр войти могут игроки 90-99 уровней в составе командного канала в котором до 5 полных групп. В зависимости от стадии становится доступной обычная или экстремальная версия Рейдового Босса. Истхина (обычная) доступна во время третьей и пятой стадии, Истхина (экстремальная) доступна во время четвертой стадии цикла. Доступ во временные зоны Рейдовых Боссов сбрасывается в Среду и в Субботу в 6.30 утра.
Стадии цикла Семени Уничтожения.
Стадия Описание
1 стадия Убивайте монстров в Семени Уничтожения, по достижении определенного результата в 13.00 Семя Уничтожения перейдет на 2 стадию цикла.
2-3 стадия 2 стадия длится 2 недели, после этого наступает 3 стадия. Во время 3 стадии становится доступна Истхина (обычная).
4 стадия Если игроки сразили Истхина (обычная) 10 и более раз, то в Понедельник в 13.00 Семя Уничтожения переключится на 4 стадию. Во время 4 стадии становится доступна Истхина (экстремальная).
5 стадия Если во время 3 стадии игроки не сразили Истхина (обычная) 10 раз, то Семя Уничтожения переключится сразу на 5 стадию. 5 стадия длится 3 недели, после чего Семя Уничтожения возвращается на 1 стадию.
 *
 * Стадия	Описание
 * 1 стадия	Убивайте монстров в Семени Уничтожения, по достижении определенного результата в 13.00 Семя Уничтожения перейдет на 2 стадию цикла.
 * 2-3 стадия	2 стадия длится 2 недели, после этого наступает 3 стадия. Во время 3 стадии становится доступна Истхина (обычная).
 * 4 стадия	Если игроки сразили Истхина (обычная) 10 и более раз, то в Понедельник в 13.00 Семя Уничтожения переключится на 4 стадию. Во время 4 стадии становится доступна Истхина (экстремальная).
 * 5 стадия	Если во время 3 стадии игроки не сразили Истхина (обычная) 10 раз, то Семя Уничтожения переключится сразу на 5 стадию. 5 стадия длится 3 недели, после чего Семя Уничтожения возвращается на 1
 * Истхина (обычная) доступна во время третьей и пятой стадии, Истхина (экстремальная) доступна во время четвертой стадии цикла. Доступ во временные зоны Рейдовых Боссов сбрасывается в Среду и в Субботу в 6.30 утра.
 * У нас на серваке опять открыта блесс истхина. Получаеться что ты сначала набиваешь 10 100% балист и на след недел блесс истхина. потом только обычная, если набили то снова есть заход на блесс. Так ? Какой-то баг тут. Откат должен быть у нее и стадии должны последовательно сменяться.
 * == Миньоны ==
 * били минут 15-20. не засекал.

по поводу плотности мобов не ко мне. я хил. когда появляются штук 40 мобов как правило есть чем заняться... сумы говорили довольно плотные, поэтому без жнецов - с кошаками.

в первый заход сняли до 1трети, пошли мобы с рестором и простые мобы... как сдохли у нее уже было 2 трети.

шарики неубиваемы. тока шприцом.
 *
 * Видео: http://www.youtube.com/watch?v=f2O97hNztBs
 */

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class Istina extends Quest
{
	private static final int ISTINA_LIGHT = 29195;
	private static final int ISTINA_HARD = 29196;

	private static final SkillHolder BARRIER_OF_REFLECTION = new SkillHolder(14215, 1);
	private static final SkillHolder FLOOD = new SkillHolder(14220, 1);
	private static final SkillHolder MANIFESTATION_OF_AUTHORITY = new SkillHolder(14289, 1);

	private static final SkillHolder ACID_ERUPTION2 = new SkillHolder(14222, 1);
	private static final SkillHolder ACID_ERUPTION3 = new SkillHolder(14223, 1);
	private static final int DEATH_BLOW = 14219;
	private static final int ISTINA_MARK = 14218;

	/* Кольца инстанса Истхины */
	private static final int RED_RING = 14220101;
	private static final int BLUE_RING = 14220102;
	private static final int GREEN_RING = 14220103;

	/* Локации колец */
	private static final int RED_RING_LOC = 400023;
	private static final int BLUE_RING_LOC = 400022;
	private static final int GREEN_RING_LOC = 400021;

	// Миньон экстрим Истхины
	private static final int ISTINAS_CREATION = 23125;
	private static final int SEALING_ENERGY = 19036;
	private static Map<Integer, IstinaData> _istinasData = new FastMap();

	public Istina()
	{
		addSpawnId(ISTINA_LIGHT, ISTINA_HARD);
		addAttackId(ISTINA_LIGHT, ISTINA_HARD);
		addKillId(ISTINA_LIGHT, ISTINA_HARD);
	}

	public static void main(String[] args)
	{
		new Istina();
	}

	/**
	 * Вызываем по достижении определенного процента ХП (90%, 80% и т.д.).
	 *
	 * @param npc NPC.
	 * @param percent Процент HP npc.
	 */
	public void onPercentHpReached(L2Npc npc, int percent)
	{
		synchronized(this)
		{
			if(npc.getNpcId() != ISTINA_LIGHT && npc.getNpcId() != ISTINA_HARD)
			{
				return;
			}

			IstinaData data = _istinasData.get(npc.getObjectId());

			if(data == null)
			{
				return;
			}

			InstanceManager.InstanceWorld tempWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(tempWorld instanceof RB_Isthina.IstinaWorld)
			{
				RB_Isthina.IstinaWorld world = (RB_Isthina.IstinaWorld) tempWorld;
				if(percent == 5 && !data.finishLock)
				{
					data.finishLock = true;
					removeEventId(HookType.ON_ATTACK);
					npc.setIsInvul(true);
					npc.setIsParalyzed(true);
					npc.teleToInstance(new Location(-177123, 146938, -11389), 8190);
					npc.setTargetable(false);

					for(L2PcInstance player : world.playersInside)
					{
						if(player.getTarget() != null && player.getTarget().isMonster() && (player.getTarget().getNpcInstance().getNpcId() == ISTINA_LIGHT || player.getTarget().getNpcInstance().getNpcId() == ISTINA_HARD))
						{
							player.setTarget(null);
							player.abortAttack();
							player.abortCast();
							player.sendActionFailed();
						}

						if(!player.getPets().isEmpty())
						{
							for(L2Summon summon : player.getPets())
							{
								summon.setTarget(null);
								summon.abortAttack();
								summon.abortCast();
								summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
							}
						}
					}
					RB_Isthina.getInstance().presentBallista(npc);
					return;
				}

				// Кастуем Acid Eruption
				byte acidsCount = (byte) (Rnd.get(3) + 1);
				int playerInside = world.playersInside.size();
				List<L2PcInstance> unluckPlayers = new FastList(acidsCount);

				// Выберем случайных игроков в инстансе
				for(byte i = 0; i < acidsCount; ++i)
				{
					while(unluckPlayers.size() < playerInside)
					{

						L2PcInstance unluckyPlayer = world.playersInside.get(Rnd.get(playerInside));
						if(!unluckPlayers.contains(unluckyPlayer))
						{
							unluckPlayers.add(unluckyPlayer);
							break;
						}
					}
				}

				int index = 0;
				for(L2PcInstance player : unluckPlayers)
				{
					L2Npc camera = world.acidEruptionCameras.get(index);
					camera.teleToInstance(player.getLoc(), world.instanceId);
					L2Skill skillToCast;
					skillToCast = Rnd.get() <= 0.5 ? ACID_ERUPTION3.getSkill() : ACID_ERUPTION2.getSkill();
					camera.doCast(skillToCast);
					++index;
				}
			}

			// Кастуем Неистовство
			if(percent >= 50 && percent % 10 == 0 || percent < 50 && percent % 5 == 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() -> npc.doCast(FLOOD.getSkill()), 5000 + Rnd.get(0, 5000));
			}

			if(npc.getNpcId() == ISTINA_HARD)
			{
				long delay = Rnd.get(4000, 12000);
				// Спауним мобов для экстрим Истхины
				if(percent <= 50 && percent % 5 == 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						for(short i = 0; i < 7; ++i)
						{
							addSpawn(ISTINAS_CREATION, npc.getLoc().getX(), npc.getLoc().getY(), npc.getLoc().getZ(), npc.getHeading(), true, 0, false, npc.getInstanceId());
						}
					}, delay);
				}

				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					int energyCount = Rnd.get(1, 4);
					for(int i = 0; i < energyCount; ++i)
					{
						final L2Npc energy = addSpawn(SEALING_ENERGY, npc.getLoc().getX(), npc.getLoc().getY(), npc.getLoc().getZ(), npc.getHeading(), true, 0, false, npc.getInstanceId());

						energy.getHookContainer().addHook(HookType.ON_EFFECT_START, new AbstractHookImpl()
						{
							private int _counter = 1;
							private int _baseCounter;
							private Future<?> _task;

							@Override
							public void onEffectStart(L2Effect e)
							{
								++_counter;

								_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
									_baseCounter += _counter;

									if(_baseCounter >= 15)
									{
										energy.getLocationController().delete();

										if(_task != null)
										{
											_task.cancel(true);
										}
									}
								}, 0, 1000);

								ThreadPoolManager.getInstance().scheduleGeneral(() -> {
									if(_task != null)
									{
										_task.cancel(true);
									}
								}, 60000);
							}
						});

						ThreadPoolManager.getInstance().scheduleGeneral(() -> energy.getLocationController().delete(), 60000);
					}
				}, delay);

			}

			// Барьер через 60 секунд
			ThreadPoolManager.getInstance().scheduleGeneral(() -> npc.doCast(BARRIER_OF_REFLECTION.getSkill()), 60000);
		}
	}

	/**
	 * Основная задача - обработка триггеров при достижении определенных уровней ХП (90%, 80%, 45% и т.п.)
	 * Также, здесь обрабатывается наложение дебаффа "Проявление Силы".
	 *
	 * @param npc
	 * @param attacker
	 * @param damage
	 * @param isPet
	 * @return
	 */
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		synchronized(this)
		{
			IstinaData data = _istinasData.get(npc.getObjectId());

			if(data == null)
			{
				return null;
			}

			if(npc.getNpcId() == ISTINA_LIGHT || npc.getNpcId() == ISTINA_HARD)
			{
				if(data.effectCheckTask == null)
				{
					data.effectCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new EffectCheckTask(npc), 0, 2000);
				}

				double lastPercentHp = (npc.getCurrentHp() + damage) / npc.getMaxHp();
				double currentPercentHp = npc.getCurrentHp() / npc.getMaxHp();

				if(lastPercentHp > 0.9 && currentPercentHp <= 0.9)
				{
					onPercentHpReached(npc, 90);
				}
				else if(lastPercentHp > 0.8 && currentPercentHp <= 0.8)
				{
					onPercentHpReached(npc, 80);
				}
				else if(lastPercentHp > 0.7 && currentPercentHp <= 0.7)
				{
					onPercentHpReached(npc, 70);
				}
				else if(lastPercentHp > 0.6 && currentPercentHp <= 0.6)
				{
					onPercentHpReached(npc, 60);
				}
				else if(lastPercentHp > 0.5 && currentPercentHp <= 0.5)
				{
					onPercentHpReached(npc, 50);
				}
				else if(lastPercentHp > 0.45 && currentPercentHp <= 0.45)
				{
					onPercentHpReached(npc, 45);
				}
				else if(lastPercentHp > 0.4 && currentPercentHp <= 0.4)
				{
					onPercentHpReached(npc, 40);
				}
				else if(lastPercentHp > 0.35 && currentPercentHp <= 0.35)
				{
					onPercentHpReached(npc, 35);
				}
				else if(lastPercentHp > 0.3 && currentPercentHp <= 0.3)
				{
					onPercentHpReached(npc, 30);
				}
				else if(lastPercentHp > 0.25 && currentPercentHp <= 0.25)
				{
					onPercentHpReached(npc, 25);
				}
				else if(lastPercentHp > 0.2 && currentPercentHp <= 0.2)
				{
					onPercentHpReached(npc, 20);
				}
				else if(lastPercentHp > 0.15 && currentPercentHp <= 0.15)
				{
					onPercentHpReached(npc, 15);
				}
				else if(lastPercentHp > 0.1 && currentPercentHp <= 0.1)
				{
					onPercentHpReached(npc, 10);
				}
				else if(!data.lastHitLock && currentPercentHp <= 0.05)
				{
					data.lastHitLock = true;
					onPercentHpReached(npc, 5);
				}
				else if(!data.finishLock)
				{
					double seed = Rnd.get();
					// С шансом в 1/5000 при атаке Истхины будет раскидываться проявление силы
					if(seed < 0.0005 && !data.authorityLock)
					{
						authorityField(npc);
					}
				}
			}

			return "";
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		IstinaData data;
		if(_istinasData.containsKey(npc.getObjectId()))
		{
			data = _istinasData.get(npc.getObjectId());
		}
		else
		{
			data = new IstinaData();
			_istinasData.put(npc.getObjectId(), data);
		}
		data.isHard = npc.getNpcId() == ISTINA_HARD;

		npc.setIsMortal(false);
		InstanceManager.InstanceWorld tempWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if(tempWorld instanceof RB_Isthina.IstinaWorld)
		{
			RB_Isthina.IstinaWorld world = (RB_Isthina.IstinaWorld) tempWorld;
			IstinaMarkHook hook = new IstinaMarkHook();

			for(L2PcInstance player : world.playersInside)
			{
				player.getHookContainer().addHook(HookType.ON_EFFECT_START, hook);
			}
		}

		return "";
	}

	/**
	 * Istina Special.
	 * С некоторым шансом Истхина поднимает растения в своем инстансе и в таких местах игроки получют дебафф.
	 * Кольца делятся на три вида по удаленности от центра комнаты Истхины:
	 * - Зеленое в центре инстанса;
	 * - Синее;
	 * - Красное.
	 *
	 * @param npc NPC.
	 */
	private void authorityField(L2Npc npc)
	{
		IstinaData data = _istinasData.get(npc.getObjectId());

		if(data == null)
		{
			return;
		}

		data.authorityLock = true;

		double seed = Rnd.get();

		// Кольца: 0 - зеленое, 1 - синее, 2 - красное
		int ring = seed < 0.33 ? 0 : seed >= 0.33 && seed < 0.66 ? 1 : 2;
		NpcStringId message;

		// Выбираем безопасное кольцо
		if(seed < 0.33)
		{
			message = NpcStringId.ISTINA_SOUL_STONE_STARTS_POWERFULLY_ILLUMINATING_IN_GREEN;
		}
		else
		{
			message = seed >= 0.33 && seed < 0.66 ? NpcStringId.ISTINA_SOUL_STONE_STARTS_POWERFULLY_ILLUMINATING_IN_BLUE : NpcStringId.ISTINA_SOUL_STONE_STARTS_POWERFULLY_ILLUMINATING_IN_RED;
		}

		npc.broadcastPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, 5000));
		npc.broadcastPacket(new PlaySound("istina.istina_voice_01"));

		// Раскидываем дебафф через 10 секунд, дадим игрокам спрятатсо ^_^
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			int[] zones = {-1, -1};
			if(ring == 0)
			{
				npc.broadcastPacket(new EventTrigger(BLUE_RING, true));
				npc.broadcastPacket(new EventTrigger(RED_RING, true));
				zones[0] = BLUE_RING_LOC;
				zones[1] = RED_RING_LOC;
			}
			else if(ring == 1)
			{
				npc.broadcastPacket(new EventTrigger(GREEN_RING, true));
				npc.broadcastPacket(new EventTrigger(RED_RING, true));
				zones[0] = GREEN_RING_LOC;
				zones[1] = RED_RING_LOC;
			}
			else
			{
				npc.broadcastPacket(new EventTrigger(GREEN_RING, true));
				npc.broadcastPacket(new EventTrigger(BLUE_RING, true));
				zones[0] = GREEN_RING_LOC;
				zones[1] = BLUE_RING_LOC;
			}

			// Кастуем дебафф
			for(int zoneId : zones)
			{
				for(L2PcInstance player : ZoneManager.getInstance().getZoneById(zoneId).getPlayersInside())
				{
					MANIFESTATION_OF_AUTHORITY.getSkill().getEffects(npc, player);
				}
			}

			data.authorityLock = false;
		}, 10000);
	}

	public class IstinaData
	{
		private boolean isHard;
		private Future<?> effectCheckTask;
		private boolean hasFlood;
		private boolean hasBarrier;
		private boolean authorityLock;
		private boolean finishLock;
		private boolean lastHitLock;
	}

	/**
	 * Хук для проверки наложения Марки Истхины.
	 */
	public class IstinaMarkHook extends AbstractHookImpl
	{
		@Override
		public void onEffectStart(L2Effect effect)
		{
			if(effect.getSkill().getId() == ISTINA_MARK)
			{
				effect.getEffected().sendPacket(new ExShowScreenMessage(NpcStringId.ISTINA_MARK_SHINES_ABOVE_THE_HEAD, ExShowScreenMessage.MIDDLE_CENTER, 10000));
			}
			// -10k HP, если на игрока скастован Death Blow и на нем лежит Istina Mark.
			else if(effect.getSkill().getId() == DEATH_BLOW)
			{
				for(L2Effect charEffect : effect.getEffected().getAllEffects())
				{
					if(charEffect.getSkill().getId() == ISTINA_MARK)
					{
						charEffect.getEffected().setCurrentHp(charEffect.getEffected().getCurrentHp() - 10000000); // TODO: Нужно как-то сделать, чтобы считался пдеф и прочее. Чтобы можно было выжить, например, танку.
					}
				}
			}
		}
	}

	/**
	 * Проверка разнообразных эффектов для отображения сообщений.
	 */
	private class EffectCheckTask implements Runnable
	{
		private L2Npc _npc;

		public EffectCheckTask(L2Npc npc)
		{
			_npc = npc;
		}

		/**
		 * Проверяет наличие эффектов "Неистовство" и "Барьер Истхины".
		 * В соответствии с полученной инфой отображает различные сообщения игрокам.
		 */
		@Override
		public void run()
		{
			IstinaData data = _istinasData.get(_npc.getObjectId());

			if(data == null)
			{
				return;
			}

			if(_npc == null)
			{
				if(data.effectCheckTask != null)
				{
					data.effectCheckTask.cancel(false);
				}
			}

			boolean hasBarrier = false;
			boolean hasFlood = false;
			for(L2Effect effect : _npc.getEffects(L2EffectType.BUFF))
			{
				if(effect.getSkill().getId() == BARRIER_OF_REFLECTION.getSkillId())
				{
					hasBarrier = true;
					if(hasFlood)
					{
						break;
					}
				}
				else if(effect.getSkill().getId() == FLOOD.getSkillId())
				{
					hasFlood = true;
					if(hasBarrier)
					{
						break;
					}
				}
			}

			// Висел барьер, сейчас не висит
			if(data.hasBarrier && !hasBarrier)
			{
				_npc.setDisplayEffect(0x02);
				_npc.setDisplayEffect(0x00);
				_npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.ISTINA_SPREADS_PROTECTIVE_SHEET, ExShowScreenMessage.TOP_CENTER, 5000));
			}
			else if(!data.hasBarrier && hasBarrier)
			{
				_npc.setDisplayEffect(0x01);
			}

			if(!data.hasFlood && hasFlood)
			{
				_npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.ISTINA_GETS_FURIOUS_AND_RECKLESSLY_CRAZY, ExShowScreenMessage.TOP_CENTER, 5000));
			}
			else if(data.hasFlood && !hasFlood)
			{
				_npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.BERSERKER_OF_ISTINA_HAS_BEEN_DISABLED, ExShowScreenMessage.TOP_CENTER, 5000));
			}

			data.hasBarrier = hasBarrier;
			data.hasFlood = hasFlood;
		}
	}
}