package dwo.scripts.ai.zone;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.instances.Kartia;
import dwo.scripts.instances.Kartia.KartiaWorld;
import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class KartiaSupporters extends Quest
{
	public KartiaSupporters()
	{

		Kartia.SOLO85_MONSTERS.values().forEach(this::addSpawnId);

		Kartia.SOLO90_MONSTERS.values().forEach(this::addSpawnId);

		Kartia.SOLO95_MONSTERS.values().forEach(this::addSpawnId);

		Kartia.PARTY85_MONSTERS.values().forEach(this::addSpawnId);

		Kartia.PARTY90_MONSTERS.values().forEach(this::addSpawnId);

		Kartia.PARTY95_MONSTERS.values().forEach(this::addSpawnId);

		addAttackId(Kartia.SOLO85_MONSTERS.get("overseer"));
		addAttackId(Kartia.SOLO90_MONSTERS.get("overseer"));
		addAttackId(Kartia.SOLO95_MONSTERS.get("overseer"));
		addAttackId(Kartia.PARTY85_MONSTERS.get("overseer"));
		addAttackId(Kartia.PARTY90_MONSTERS.get("overseer"));
		addAttackId(Kartia.PARTY95_MONSTERS.get("overseer"));

		addKillId(Kartia.SOLO85_MONSTERS.get("ruler"));
		addKillId(Kartia.SOLO90_MONSTERS.get("ruler"));
		addKillId(Kartia.SOLO95_MONSTERS.get("ruler"));
		addKillId(Kartia.PARTY85_MONSTERS.get("ruler"));
		addKillId(Kartia.PARTY90_MONSTERS.get("ruler"));
		addKillId(Kartia.PARTY95_MONSTERS.get("ruler"));
	}

	public static void freeRuler(KartiaWorld world)
	{
		if(world.ruler != null)
		{
			world.ruler.stopAbnormalEffect(AbnormalEffect.HOLD_2);
			world.ruler.stopParalyze(false);
			world.ruler.setIsInvul(false);
			world.ruler.setIsParalyzed(false);
			world.wave.add(world.ruler);
		}
	}

	/**
	 * Уводим пленников из инстанса.
	 *
	 * @param world
	 */
	public static void saveCaptivateds(KartiaWorld world)
	{
		synchronized(KartiaSupporters.class)
		{
			int delay = 0;
			for(L2Npc captivated : world.captivateds)
			{
				++world.savedCaptivateds;

				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					if(captivated != null)
					{
						if(world.isPartyInstance)
						{
							captivated.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-118391, -10454, -11924));
						}
						else
						{
							captivated.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-108823, -10454, -11924));
						}
					}
				}, delay);

				ThreadPoolManager.getInstance().scheduleGeneral(() -> captivated.getLocationController().delete(), 10000 + delay);

				delay += 1000;
			}
			world.captivateds.clear();
		}
	}

	public static void main(String[] args)
	{
		new KartiaSupporters();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(isKartiaNpc(npc))
		{
			KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(npc, KartiaWorld.class);

			if(world.status == 1 && world.monsterSet.get("overseer") == npc.getNpcId() && npc.getCurrentHp() / npc.getMaxHp() <= 0.4 && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.YOU_VERY_STRONG_FOR_MORTAL_I_RETREAT));
				Location loc = world.isPartyInstance ? new Location(-120865, -13904, -11440) : new Location(-111297, -13904, -11440);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);

				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					npc.doDie(attacker);
					npc.getLocationController().delete();
					Kartia.getInstance().openRaidDoor(world);
				}, 10000);
			}
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		synchronized(this)
		{
			if(isKartiaNpc(npc))
			{
				KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(npc, KartiaWorld.class);

				if(world.monsterSet.get("ruler") == npc.getNpcId())
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.HOW_ITS_IMPOSSIBLE_RETURNING_TO_ABYSS_AGAIN));
				}
			}
			return null;
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(isKartiaNpc(npc))
		{
			npc.setRunning();

			// Отключаем возвращение гвардов домой
			if(npc instanceof L2GuardInstance)
			{
				((L2GuardInstance) npc).setReturnHome(false);
				npc.setBusy(true);
				npc.setIsNoAnimation(true);
			}
			else if(npc instanceof L2MonsterInstance)
			{
				npc.setIsNoRndWalk(true);

				KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(npc, KartiaWorld.class);

				// Бежим к пленникам
				if(world.monsterSet.get("captivated") != npc.getNpcId())
				{
					if(world.status == 0)
					{
						npc.setRunning();
						Location loc = world.isPartyInstance ? new Location(-120921 + Rnd.get(-150, 150), -10452 + Rnd.get(-150, 150), -11680) : new Location(-111353 + Rnd.get(-150, 150), -10452 + Rnd.get(-150, 150), -11680);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
					}
				}

				if(world.monsterSet.get("overseer") == npc.getNpcId())
				{
					if(world.status == 0)
					{
						npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.INTRUDERS_CANNOT_LEAVE_ALIVE));
					}
				}
				else if(world.monsterSet.get("ruler") == npc.getNpcId())
				{
					npc.setIsInvul(true);
					npc.startAbnormalEffect(AbnormalEffect.HOLD_2);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, world.ruler);
					npc.startParalyze();
					npc.setIsParalyzed(true);
				}
			}
		}

		return null;
	}

	private boolean isKartiaNpc(L2Npc npc)
	{
		return InstanceManager.getInstance().getInstanceWorld(npc, KartiaWorld.class) != null;

	}

	/**
	 * Задача предназначена для саппорт-npc в соло-картии.
	 * Помогают пинать мобов, хиляют, используют скиллы.
	 */
	public static class KartiaSupportTask implements Runnable
	{
		private final KartiaWorld _world;
		private int _lastPlyaerHeading;

		public KartiaSupportTask(KartiaWorld world)
		{
			_world = world;
			_lastPlyaerHeading = -1;
		}

		@Override
		public void run()
		{
			// Если меняется heading персонажа, то будем перемещать всех саппортов
			boolean refollowAll = false;
			for(L2Npc follower : _world.followers)
			{
				boolean needFollow = true;
				// NPC ничего не делает
				if((follower.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK || !follower.isAttackingNow()) && (follower.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST || !follower.isCastingNow()) && follower.getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO)
				{
					// Ну жость у нас а не двиг агра NPC друг на друга. Чистим хейт-лист от возможных агров гвардов друг на друга :))
					((L2Attackable) follower).getAggroList().values().stream().filter(aggro -> aggro.getAttacker().isNpc() || aggro.getAttacker().isPlayer()).forEach(aggro -> ((L2Attackable) follower).getAggroList().remove(aggro.getAttacker()));

					if(!_world.playersInLairZone.isEmpty() && Util.calculateDistance(_world.playersInLairZone.get(0), follower, true) > 600)
					{
						needFollow = true;
					}
					else
					{
						for(L2Npc npc : follower.getKnownList().getKnownNpcInRadius(600))
						{
							if(npc.isMonster() && npc.getNpcId() != _world.monsterSet.get("captivated") && npc.getNpcId() != _world.monsterSet.get("altar") && !npc.isInvul())
							{
								follower.setIsRunning(true);
								((L2GuardInstance) follower).addDamageHate(npc, 999, 999);
								needFollow = false;
								break;
							}
						}

						// Проверим, менялся ли heading у персонажа
						if(needFollow)
						{
							if(_lastPlyaerHeading < 0 || !_world.playersInLairZone.isEmpty() && _lastPlyaerHeading != _world.playersInLairZone.get(0).getHeading() || refollowAll)
							{
								needFollow = true;
								refollowAll = true;
							}
							else
							{
								needFollow = false;
							}

							_lastPlyaerHeading = !_world.playersInLairZone.isEmpty() ? _world.playersInLairZone.get(0).getHeading() : 0;
						}
					}
				}
				else
				{
					needFollow = false;
				}

				if(needFollow)
				{
					// Хак для того, чтобы гварды атаковали везде, а не только в рендже своего спауна
					follower.getSpawn().setLocx(follower.getX());
					follower.getSpawn().setLocy(follower.getY());
					follower.getSpawn().setLocz(follower.getZ());

					double angle = Util.convertHeadingToDegree(_world.playersInLairZone.get(0).getHeading());
					double radians = Math.toRadians(angle);
					double radius = 100.0;
					double course = 160;

					int x = (int) (Math.cos(Math.PI + radians + course) * radius);
					int y = (int) (Math.sin(Math.PI + radians + course) * radius);

					follower.setRunning();
					Location loc = _world.playersInLairZone.get(0).getLoc();
					loc.setX(loc.getX() + x + Rnd.get(-100, 100));
					loc.setY(loc.getY() + y + Rnd.get(-100, 100));
					follower.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
				}
			}
		}
	}

	public static class HealTask implements Runnable
	{
		private final KartiaWorld _world;

		public HealTask(KartiaWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			if(_world.playersInLairZone.size() <= 0)
			{
				return;
			}

			L2PcInstance player = _world.playersInLairZone.get(0);

			if(_world.healer != null)
			{
				double percentHp = player.getCurrentHp() / player.getMaxHp();

				if(percentHp <= 0.5)
				{
					// Хил 40%
					_world.healer.setTarget(player);

					switch(_world.type)
					{
						case SOLO85:
							_world.healer.doCast(SkillTable.getInstance().getInfo(14899, 1));
							break;
						case SOLO90:
							_world.healer.doCast(SkillTable.getInstance().getInfo(14900, 1));
							break;
						case SOLO95:
							_world.healer.doCast(SkillTable.getInstance().getInfo(14901, 1));
							break;
					}

					boolean needTree = true;
					Instance instance = InstanceManager.getInstance().getInstance(_world.instanceId);
					if(instance != null)
					{
						return;
					}

					for(L2Npc npc : instance.getNpcs())
					{
						if(npc.getNpcId() == 19256)
						{
							needTree = false;
							break;
						}
					}

					if(needTree)
					{
						// Дерево Жизни
						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							switch(_world.type)
							{
								case SOLO85:
									_world.healer.doCast(SkillTable.getInstance().getInfo(14903, 1));
									break;
								case SOLO90:
									_world.healer.doCast(SkillTable.getInstance().getInfo(14904, 1));
									break;
								case SOLO95:
									_world.healer.doCast(SkillTable.getInstance().getInfo(14905, 1));
									break;
							}

							ThreadPoolManager.getInstance().scheduleGeneral(() -> Kartia.getInstance().spawnHealingTree(_world), 2000);
						}, 2000);
					}
				}
				// Обычный хил
				else if(percentHp <= 0.85)
				{
					_world.healer.setTarget(player);
					_world.healer.doCast(SkillTable.getInstance().getInfo(14899, 1));
				}
			}
		}
	}

	/**
	 * Задача перемещения монстров первой комнаты. Двигаемся к пленникам.
	 */
	public static class MonsterMovementTask implements Runnable
	{
		public static final List<Location> SOLO_LEFT_KILLER_ROUTES = new FastList<>();

		static
		{
			SOLO_LEFT_KILLER_ROUTES.add(new Location(-110440, -10472, -11926));
			SOLO_LEFT_KILLER_ROUTES.add(new Location(-110085, -10876, -11920));
			SOLO_LEFT_KILLER_ROUTES.add(new Location(-109182, -10791, -11920));
			SOLO_LEFT_KILLER_ROUTES.add(new Location(-109162, -10453, -11926));
			SOLO_LEFT_KILLER_ROUTES.add(new Location(-109933, -10451, -11688));
		}

		public static final List<Location> SOLO_RIGHT_KILLER_ROUTES = new FastList<>();

		static
		{
			SOLO_RIGHT_KILLER_ROUTES.add(new Location(-110440, -10472, -11926));
			SOLO_RIGHT_KILLER_ROUTES.add(new Location(-110020, -9980, -11920));
			SOLO_RIGHT_KILLER_ROUTES.add(new Location(-109157, -10009, -11920));
			SOLO_RIGHT_KILLER_ROUTES.add(new Location(-109162, -10453, -11926));
			SOLO_RIGHT_KILLER_ROUTES.add(new Location(-109933, -10451, -11688));
		}

		public static final List<Location> PARTY_LEFT_KILLER_ROUTES = new FastList<>();

		static
		{
			PARTY_LEFT_KILLER_ROUTES.add(new Location(-120008, -10472, -11926));
			PARTY_LEFT_KILLER_ROUTES.add(new Location(-119653, -10876, -11920));
			PARTY_LEFT_KILLER_ROUTES.add(new Location(-118750, -10791, -11920));
			PARTY_LEFT_KILLER_ROUTES.add(new Location(-118730, -10453, -11926));
			PARTY_LEFT_KILLER_ROUTES.add(new Location(-119501, -10451, -11688));
		}

		public static final List<Location> PARTY_RIGHT_KILLER_ROUTES = new FastList<>();

		static
		{
			PARTY_RIGHT_KILLER_ROUTES.add(new Location(-120008, -10472, -11926));
			PARTY_RIGHT_KILLER_ROUTES.add(new Location(-119588, -9980, -11920));
			PARTY_RIGHT_KILLER_ROUTES.add(new Location(-118725, -10009, -11920));
			PARTY_RIGHT_KILLER_ROUTES.add(new Location(-118730, -10453, -11926));
			PARTY_RIGHT_KILLER_ROUTES.add(new Location(-119501, -10451, -11688));
		}

		private final KartiaWorld _world;
		private Location lastLoc;
		private int currentWave = -1;
		private int currentSubwave = -1;
		private List<Location> routes;

		public MonsterMovementTask(KartiaWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			if(_world.waveSpawnTime == 0 || (System.currentTimeMillis() - _world.waveSpawnTime) / 1000 < 15)
			{
				return;
			}

			if(currentWave >= 0 && currentWave != _world.currentWave || currentSubwave >= 0 && currentSubwave != _world.currentSubwave)
			{
				lastLoc = null;
				routes = null;
			}

			currentWave = _world.currentWave;
			currentSubwave = _world.currentSubwave;

			if(routes == null)
			{
				if(_world.isPartyInstance)
				{
					routes = Rnd.getChance(0.5) ? PARTY_LEFT_KILLER_ROUTES : PARTY_RIGHT_KILLER_ROUTES;
				}
				else
				{
					routes = Rnd.getChance(0.5) ? SOLO_LEFT_KILLER_ROUTES : SOLO_RIGHT_KILLER_ROUTES;
				}
			}

			for(L2Npc npc : _world.wave)
			{
				CtrlIntention intention = npc.getAI().getIntention();

				if(!((L2Attackable) npc).getAggroList().isEmpty() || intention == CtrlIntention.AI_INTENTION_MOVE_TO)
				{
					return;
				}
			}

			boolean takeNextRoute = false;
			for(Location loc : routes)
			{
				if(takeNextRoute)
				{
					lastLoc = loc;
					break;
				}

				if(lastLoc == null)
				{
					lastLoc = loc;
					break;
				}
				else if(lastLoc.equals(loc))
				{
					takeNextRoute = true;
				}
			}

			for(L2Npc npc : _world.wave)
			{
				CtrlIntention intention = npc.getAI().getIntention();

				if(!((L2Attackable) npc).getAggroList().isEmpty() || intention == CtrlIntention.AI_INTENTION_MOVE_TO)
				{
					continue;
				}

				if(lastLoc != null)
				{
					Location randomizedLoc = new Location(lastLoc.getX(), lastLoc.getY(), lastLoc.getZ());
					randomizedLoc.setX(randomizedLoc.getX() + Rnd.get(-30, 30));
					randomizedLoc.setY(randomizedLoc.getY() + Rnd.get(-30, 30));

					npc.setRunning();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, randomizedLoc);
				}
			}
		}
	}

	/**
	 * Задача аггра монстров Картии. Приоритет - игрок.
	 */
	public static class MonsterAggroTask implements Runnable
	{
		private final KartiaWorld _world;

		public MonsterAggroTask(KartiaWorld world)
		{
			_world = world;
		}

		private void aggroCheck(L2Npc npc)
		{
			if(npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Ищем на кого бы сагриться
				if(!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					int captivatedId = !_world.captivateds.isEmpty() ? _world.captivateds.get(0).getNpcId() : 0;
					npc.getKnownList().findObjects();
					List<L2Object> objects = WorldManager.getInstance().getVisibleObjects(npc, 450);
					for(L2Object object : objects)
					{
						if(object instanceof L2PcInstance || object instanceof L2GuardInstance && ((L2GuardInstance) object).getNpcId() != captivatedId)
						{
							((L2MonsterInstance) npc).attackCharacter((L2Character) object);
							break;
						}
					}
				}
			}
		}

		@Override
		public void run()
		{
			if(_world.status <= 2)
			{
				_world.wave.forEach(this::aggroCheck);

				if(_world.status == 3 && _world.ruler != null)
				{
					aggroCheck(_world.ruler);
				}
			}
		}
	}

	public static class AltharTask implements Runnable
	{
		private static final L2Skill HP_ABSORBTION85 = SkillTable.getInstance().getInfo(14984, 1);
		private static final L2Skill HP_ABSORBTION90 = SkillTable.getInstance().getInfo(14985, 1);
		private static final L2Skill HP_ABSORBTION95 = SkillTable.getInstance().getInfo(14986, 1);
		private final KartiaWorld _world;

		public AltharTask(KartiaWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			L2Skill castSkill = null;
			switch(_world.type)
			{
				case SOLO85:
				case PARTY85:
					castSkill = HP_ABSORBTION85;
					break;
				case SOLO90:
				case PARTY90:
					castSkill = HP_ABSORBTION90;
					break;
				case SOLO95:
				case PARTY95:
					castSkill = HP_ABSORBTION95;
					break;
			}

			if(castSkill == null)
			{
				return;
			}

			if(_world.captivateds != null && !_world.captivateds.isEmpty())
			{
				for(L2Npc npc : _world.wave)
				{
					if(npc.isDecayed() || npc.isDead())
					{
						continue;
					}

					double distance = Util.calculateDistance(npc, _world.kartiaAlthar, true);
					if(distance < 500 && npc.getZ() - _world.kartiaAlthar.getZ() < 150)
					{
						Kartia.getInstance().onNpcDie(npc, _world.kartiaAlthar);
						npc.doDie(_world.kartiaAlthar);
						npc.getLocationController().delete();

						_world.kartiaAlthar.setDisplayEffect(0x01);
						if(!_world.captivateds.isEmpty())
						{
							L2Npc captivated = _world.captivateds.get(Rnd.get(_world.captivateds.size()));
							if(captivated != null)
							{
								_world.kartiaAlthar.setTarget(captivated);
								_world.kartiaAlthar.doCast(castSkill);
								_world.kartiaAlthar.setHeading(0);

								ThreadPoolManager.getInstance().scheduleGeneral(() -> {
									captivated.getLocationController().delete();
									_world.captivateds.remove(captivated);
								}, 10000);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Саппорт, который появлется в рейд-комнате. Пинаем мобов :)
	 */
	public static class RaidSupportTask implements Runnable
	{
		private KartiaWorld _world;

		public RaidSupportTask(KartiaWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			for(L2Npc support : _world.supports)
			{
				if(!support.isAttackingNow() && !support.isCastingNow())
				{
					for(L2Npc monster : support.getKnownList().getKnownNpcInRadius(1200))
					{
						if(monster.isMonster() && !monster.isInvul())
						{
							support.setRunning();
							((L2GuardInstance) support).addDamageHate(monster, 999, 999);
							break;
						}
					}
				}
			}
		}
	}
}