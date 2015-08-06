package dwo.scripts.ai.individual.raidbosses;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.*;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSendUIEvent;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * User: GenCloud
 * Date: 15:04 / 30.11.2014
 *
 На юге города Руна, в Лесу Неупокоенных находится Жертвенный Алтарь.
 С помощью него можно зайти в Рейдовую Зону к Линдвиору.
 Бросить вызов одному из сильнейших рейдовых боссов могут только персонажи 99 уровня,
 для входа к РБ нужно 49-112 человек (7-16 полных групп) находящиеся в составе командного канала.
 Внутрь сможет попасть только 1 командный канал.

 ZONE - 44269 -26152 -1405

 Рейд - Линдвиор
 Уровень - 99
 Количество человек - 49-112(7-16 групп)
 NPC -Kato Sycamore Maynooth (Като Сиканус) id=33881
 Примечание - 1 Командный канал (2ая или соло бомжи немогут зайти)

 Генератор - id=19426
 Генератор - id=19477
 Схема генератора - id=19479
 Линдрако - id=25895(мелкий ползающий) id=29243(летающий) id=29242 id=29241 id=25897 id=25896
 Линдрако (маленький) - id=19476

 Система оптимизации графики
 При походе на Линдвиора, что бы увеличить FPS (количество кадров в секунду),
 автоматически включается единая система оптимизации.
 У всех персонажей броня и оружие, кроме головных уборов, становятся одинаковыми.
 Если у вас нет необходимости в оптимизации графики, то отключите систему, нажав кнопку на мини-карте.

 Защита
 Алтарь Жертвоприношений - это огромное круглое здание, в центре которого находится большая башня,
 а вокруг неё 4 энергоблока. Линдвиор посылает своих бойцов, чтобы уничтожить Энергоблоки,
 а игроки не должны позволить сломать их.
 Когда Энергоблоки зарядятся до конца, пушки выстрелят по летающему Линдвиору,
 и он упадет на землю, после этого начинается фарм Эпического Босса.


 1 Этап
 Первый этап начинается когда Линдвиор спускается на землю, и до того как у него не станет 80% HP.
 Если стоять перед драконом, можно получить урон от удара ветром, а если стоять со спины,
 то вы получите урон от удара хвостом.
 Дракон будет использовать умение Дыхание Ветра - оно наносит урон и отбрасывает спереди дракона,
 и умение Body Slam, которое наносит урон и оглушает цели вокруг.
 Во время фарма Линдвиор будет призывать рейдовых бойцов (А).
 После того, как вы отнимите первые 20% HP у Линдвиора, он взлетит в воздух и использует умение Ярость,
 которое будет отнимать у всех персонажей по 2500 HP за тик.

 Линдрако - id=25895 id=29243 id=29242 id=29241 id=25897 id=25896

 2 Этап
 Когда HP Линдвиора меньше 80%, он поднимается в воздух и перемещается по всему Алтарю Жертвоприношения,
 нанося мега удары ветром.
 С небольшой вероятностью появляются вихри, которые будут затягивать персонажей и наносить урон.
 Когда HP Линдвиора снизится до 75%, он вызовет огромный вихрь, который будет перемещаться по всей локации.
 Если вы попадете в гигантский вихрь, ваше HP, MP, CP опустится до 1.
 Линдвиор также призывает новых рейдовых бойцов (B).
 Линдрако - id=25895 id=29243 id=29242 id=29241 id=25897 id=25896

 3 Этап
 Когда HP Линдвиора спускается ниже 60%, он спускается на землю.
 Он начинает использовать такие же умения, как и на 1 этапе,
 и дополнительно использует умение Тёмный Ветер - он отражает урон.
 Это умение легко заметить - Линдвиор потемнеет при его использовании.
 Также появляются новые рейдовые бойцы (C)
 Линдрако - id=25895 id=29243 id=29242 id=29241 id=25897 id=25896

 4 Этап
 HP Линдвиора спускается до 40%, и он опять поднимается в воздух.
 Этот этап похож на 2 стадию. Когда HP спустится ниже 35%, Рейд Босс призовет Малые Вихри.

 5 Этап
 HP Дракона спускается ниже 20%.
 Теперь, вам дается 10 минут, чтобы убить Линдвиора.
 Он использует умение Ярость, и урон по нему снизится.
 Если вы не успеете убить его за 10 минут, то всех выкинет из рейд зоны.
 На последнем Этапе Lindvior призывает рейдовых бойцов и вихри, использует умения как на 1 этапе,
 кроме этого, если вы попадёте в вихрь, то ваша скорость снизится и вас отбросит.

 зависимости от HP Линдвиора игрок получает Чешуйки Линдвиора.
 HP ниже 80%: 1 Чешуйка Линдвиора
 HP ниже 60%: 2 Чешуйки Линдвиора
 HP ниже 40%: 3 Чешуйки Линдвиора
 HP ниже 20%: 4 Чешуйки Линдвиора
 Чтобы получать Чешуйки Линдвиора, нужно заключить союз с НПЦ в Руне,
 если расформировать союз или выйти из него, то награду получить нельзя.
 100 Чешуек Линдвиора можно обменять на книгу для изучения умения езды на Линдвиоре.

 http://www.youtube.com/watch?v=hurIT0lyJ70

 14211701	u,Нужно запустить четыре генератора.\0
 14211702	u,Нужно защищать генераторы!\0
 14211703	u,Персонаж $s1 зарядил пушку.\0
 14211704	u,Чтобы использовать генератор, нужно зарядить пушку.\0
 14211705	u,От Линдвиора веет силой.\0
 14211706	u,Появился огромный вихрь.\0
 14211707	u,Линдвиор собирается с силами. Осталось $s1 мин.\0
 14211708	u,Линдвиор упал!\0
 14211709	u,Линдвиор приземлился!\0
 14211710	u,Нацельтесь на генератор и примените Цепное Умение.\0
 14211711	u,Генератор разрушен!\0
 14211712	u,Гонитель\0
 14211713	u,Стремительный\0
 14211714	u,Подлый\0
 14211715	u,Славные воины уничтожили Дракона Ветра Линдвиора!\0

 Social:
 1) standUp - id^30
 2) falldown_short - id^33
 */

public class Lindvior extends Quest
{
    private static Lindvior _instance;

    //Lindvior Status Tracking :
    private static final byte DORMANT = 0;        //Lindvior is spawned and no one has entered yet. Entry is unlocked
    private static final byte WAITING = 1;        //Lindvior is spawend and someone has entered, triggering a 30 minute window for additional people to enter before he unleashes his attack. Entry is unlocked
    private static final byte FIGHTING = 2;
    private static final byte DEAD = 3;            //Lindvior has been killed. Entry is locked

    // Config
    private static final int ACTIVITYTIME   = 120;

    private static final boolean EARTH_QUAKE_ON_SPAWN = true;

    // Монстры
    private static final int LINDVIOR_FAKE = 19423;
    private static final int LINDVIOR_RAID = 25899;
    private static final int LINDVIOR_FLY = 19424;

    private static final int[] LINDVIOR_SERVITOR = {25895, 29243, 29242, 29241, 25897, 25896};

    // НПЦ
    private static final int NPC_GENERATOR = 19477;
    private static final int NPC_SCHEME_GENERATOR = 19479;

    private static final int NPC_ATTACKER_GENERATORS_1 = 25897;
    private static final int NPC_ATTACKER_GENERATORS_2 = 25895;
    private static final int NPC_ATTACKER_GENERATORS_3 = 29242;
    private static final int NPC_ATTACKER_GENERATORS_4 = 25896;

    private static final int NPC_ATTACKER_SMALL_VORTEX = 25898;
    private static final int NPC_ATTACKER_BIG_VORTEX = 19427;

    // zone triggers
    private static final int eventTriggerEnergy = 21170110;
    private static final int eventTriggerGenerators2 = 21170102;
    private static final int eventTriggerGenerators1 = 21170104;
    private static final int eventTriggerGenerators3 = 21170106;
    private static final int eventTriggerGenerators4 = 21170108;
    private static final int eventTriggerProtect = 21170100;
    private static final int eventTriggerPower  = 21170112;

    // Локации
    private static final Location CENTER_LOCATION = new Location(46424, -26200, -1430);

    // Умения
    private static final SkillHolder SKILL = new SkillHolder(15606, 1);
    private static final SkillHolder SKILL_FLY = new SkillHolder(15279, 1);
    private static final SkillHolder SKILL_RABIES = new SkillHolder(15269, 1);
    private static final SkillHolder SKILL_REFLECT = new SkillHolder(15592, 1);
    private static final SkillHolder SKILL_FLY_UP = new SkillHolder(15278, 1);

    // Spawn data of monsters.
    protected TIntObjectHashMap<L2Spawn> _monsterSpawn = new TIntObjectHashMap<>();

    protected List<L2Npc>           _otherSpawn     = new ArrayList<>();
    protected List<L2GuardInstance> _generatorSpawn = new ArrayList<>();
    protected List<L2GuardInstance> _schemeSpawn    = new ArrayList<>();

    private static final L2GameServerPacket[] GENERATOR_MESSAGES =
            {
                    new ExShowScreenMessage(NpcStringId.PROTECT_THE_GENERATOR, ExShowScreenMessage.TOP_CENTER, 7000)
            };

    private static final int[] NS_Strings = {1802360, 1802361, 1802362, 1802363, 1802364, 1802365};

    private static final int[][] CONTROL_GENERATOR_SPAWNS =
            {
                    {45283, -30372, -1384, 24575},
                    {48485, -27179, -1384, 27642},
                    {45283, -23967, -1384, 24575},
                    {42086, -27179, -1384, 27642}
            };

    private static final int[][] SCHEME_GENERATOR_SPAWNS =
            {
                    {42240, -26906, -1413, 64987},
                    {42240, -27421, -1413, 64987},

                    {45556, -24118, -1413, 46871},
                    {45023, -24118, -1413, 46871},

                    {48324, -26906, -1413, 33713},
                    {48324, -27421, -1413, 33713},

                    {45556, -30214, -1413, 16383},
                    {45023, -30214, -1413, 16383}
            };

    private static final int[][] ATTACKER_GENERATOR_SPAWNS_1 =
            {
                    {45675, -30057, -1413, 64987},
                    {44863, -30057, -1413, 64987}
            };
    private static final int[][] ATTACKER_GENERATOR_SPAWNS_2 =
            {
                    {42350, -27563, -1413, 46871},
                    {42350, -26809, -1413, 46871}
            };
    private static final int[][] ATTACKER_GENERATOR_SPAWNS_3 =
            {
                    {44863, -24272, -1413, 33713},
                    {45675, -24272, -1413, 33713}
            };
    private static final int[][] ATTACKER_GENERATOR_SPAWNS_4 =
            {
                    {48220, -26809, -1413, 16383},
                    {48220, -27563, -1413, 16383}
            };
    private static final int[][] LINDVIOR_SPAWN =
            {
                    {46424, -26200, -1430, 16383},
            };

    private static final Location TRIGGER_G_1 = new Location(48485, -27179, -1384, 27642);
    private static final Location TRIGGER_G_2 = new Location(45283, -30372, -1384, 24575);
    private static final Location TRIGGER_G_3 = new Location(45283, -23967, -1384, 24575);
    private static final Location TRIGGER_G_4 = new Location(42086, -27179, -1384, 27642);

    // zone triggers
    private static final Map<Integer, Integer> TRIGGERS_BY_LOCS = new HashMap<Integer, Integer>()
    {{
            put(TRIGGER_G_1.toXYZString().hashCode(), eventTriggerGenerators1);
            put(TRIGGER_G_2.toXYZString().hashCode(), eventTriggerGenerators2);
            put(TRIGGER_G_3.toXYZString().hashCode(), eventTriggerGenerators3);
            put(TRIGGER_G_4.toXYZString().hashCode(), eventTriggerGenerators4);
        }};

    // Зоны
    private final static int ZONE_ID = 12020;

    // Tasks.
    protected ScheduledFuture<?> _socialTask;
    protected ScheduledFuture<?> _announceTask;
    protected ScheduledFuture<?> _skillCastTask;
    protected ScheduledFuture<?> _mobsSpawnTask;
    protected ScheduledFuture<?> _activityCheckTask;
    protected ScheduledFuture<?> _bigVortexesTask;
    protected ScheduledFuture<?> _smallVortexesTask;

    protected L2Npc _lindvior2 = null;
    protected L2GrandBossInstance _lindvior = null;
    protected L2BossZone _zoneLair;
    protected OnHpChange _hpChange;
    protected L2Npc _vortex = null;

    protected List<L2Npc> _chargedGenerator = new ArrayList<>();

    protected int _status = 0;

    private int generatorsTalkings;

    public Lindvior()
    {
        super();

        addAskId(NPC_GENERATOR, -1111);
        addEventId(HookType.ON_SEE_PLAYER);
        int[] mobs = {NPC_ATTACKER_GENERATORS_1, NPC_ATTACKER_GENERATORS_2, NPC_ATTACKER_GENERATORS_3, NPC_ATTACKER_GENERATORS_4, NPC_SCHEME_GENERATOR, NPC_ATTACKER_BIG_VORTEX, NPC_ATTACKER_SMALL_VORTEX, LINDVIOR_FAKE, LINDVIOR_FLY, LINDVIOR_RAID};

        registerMobs(mobs);
        _zoneLair = ZoneManager.getInstance().getZoneById(ZONE_ID, L2BossZone.class);

        init();
    }

    private void init()
    {
        _hpChange = new OnHpChange();

        L2NpcTemplate template;
        L2Spawn tempSpawn;
        try
        {
            _zoneLair = GrandBossManager.getInstance().getZone(44248, -26120, -1430);
            template = NpcTable.getInstance().getTemplate(LINDVIOR_RAID);
            tempSpawn = new L2Spawn(template);
            tempSpawn.setLocation(CENTER_LOCATION);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(ACTIVITYTIME << 1);

            SpawnTable.getInstance().addNewSpawn(tempSpawn);

            _monsterSpawn.put(LINDVIOR_RAID, tempSpawn);
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, e.getMessage(), e);
        }

        int status = GrandBossManager.getInstance().getBossStatus(LINDVIOR_RAID);
        StatsSet info = GrandBossManager.getInstance().getStatsSet(LINDVIOR_RAID);
        Long respawnTime = info.getLong("respawn_time");
        if (status == DEAD && respawnTime <= System.currentTimeMillis())
        {
            GrandBossManager.getInstance().setBossStatus(LINDVIOR_RAID, status = DORMANT);
        }
        if (status == DEAD)
            ThreadPoolManager.getInstance().scheduleGeneral(() -> GrandBossManager.getInstance().setBossStatus(LINDVIOR_RAID, DORMANT), respawnTime - System.currentTimeMillis());

    }


    public static void main(String[] args)
    {
        _instance = new Lindvior();
    }

    public static Lindvior getInstance()
    {
        return _instance;
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if(event.equalsIgnoreCase("waiting"))
        {
            setLindviorSpawnTask();
            GrandBossManager.getInstance().setBossStatus(LINDVIOR_RAID, WAITING);
        }
        return super.onAdvEvent(event, npc, player);
    }

    public void setLindviorSpawnTask()
    {
        if(_socialTask == null)
        {
            synchronized(this)
            {
                if(_socialTask == null)
                {
                    _socialTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> nextStage(1), 1000);
                }
            }
        }
    }

    private class Clean implements Runnable
    {
        @Override
        public void run()
        {
            _zoneLair.oustAllPlayers();

            if (_socialTask != null) {
                _socialTask.cancel(false);
                _socialTask = null;
            }

            if (_announceTask != null) {
                _announceTask.cancel(false);
                _announceTask = null;
            }

            if (_skillCastTask != null) {
                _skillCastTask.cancel(false);
                _skillCastTask = null;
            }

            if (_mobsSpawnTask != null) {
                _mobsSpawnTask.cancel(false);
                _mobsSpawnTask = null;
            }

            if (_activityCheckTask != null) {
                _activityCheckTask.cancel(false);
                _activityCheckTask = null;
            }

            if (_bigVortexesTask != null) {
                _bigVortexesTask.cancel(false);
                _bigVortexesTask = null;
            }

            if (_smallVortexesTask != null) {
                _smallVortexesTask.cancel(false);
                _smallVortexesTask = null;
            }

            _otherSpawn.forEach(npc ->
            {
                if (npc != null) {
                    npc.getSpawn().stopRespawn();
                    npc.getLocationController().delete();
                }
            });
            _otherSpawn.clear();

            GrandBossManager.getInstance().setBossStatus(_lindvior.getNpcId(), DORMANT);
        }
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        switch (npc.getNpcId())
        {
            case NPC_GENERATOR:
                if (ask == -1111)
                {
                    if (reply == 1)
                    {
                        final int trigger = TRIGGERS_BY_LOCS.get(npc.getSpawn().getLoc().toXYZString().hashCode());

                        if(npc.getDisplayEffect() == 0x02)
                        {
                            return "Вы уже активировали это устройство!";
                        }

                        ++generatorsTalkings;
                        if (generatorsTalkings == 1 && npc.getDisplayEffect() == 0x01)
                        {
                            if (npc.getSpawn().getLoc() != null) {
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EventTrigger(trigger, true)));
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EarthQuake(p.getX(), p.getY(), p.getZ(), 20, 10)));
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_MUST_ACTIVATE_THE_4_GENERATORS, ExShowScreenMessage.TOP_CENTER, 4000)));
                            }
                        }

                        if (generatorsTalkings == 2 && npc.getDisplayEffect() == 0x01)
                        {
                            if (npc.getSpawn().getLoc() != null) {
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EventTrigger(trigger, true)));
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EarthQuake(p.getX(), p.getY(), p.getZ(), 20, 10)));
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_MUST_ACTIVATE_THE_4_GENERATORS, ExShowScreenMessage.TOP_CENTER, 4000)));
                            }

                        }

                        if (generatorsTalkings == 3 && npc.getDisplayEffect() == 0x01)
                        {
                            if (npc.getSpawn().getLoc() != null) {
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EventTrigger(trigger, true)));
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EarthQuake(p.getX(), p.getY(), p.getZ(), 20, 10)));
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_MUST_ACTIVATE_THE_4_GENERATORS, ExShowScreenMessage.TOP_CENTER, 4000)));
                            }

                        }

                        if (generatorsTalkings == 4 && npc.getDisplayEffect() == 0x01)
                        {
                            if (npc.getSpawn().getLoc() != null) {
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EventTrigger(trigger, true)));
                                _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EarthQuake(p.getX(), p.getY(), p.getZ(), 20, 10)));
                                _announceTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> nextStage(2), 6000L);
                            }

                        }

                        if (generatorsTalkings > 0)
                        {
                            _zoneLair.getPlayersInside().stream().forEach(p -> p.broadcastPacket(new EventTrigger(eventTriggerEnergy, true)));
                        }
                        npc.setDisplayEffect(0x02);
                    }
                }
        }
        return super.onAsk(player, npc, ask, reply);
    }

    @Override
    public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
    {
        if(skill != null && caster != null && skill.getId() == 15606 && !_chargedGenerator.contains(npc))
        {
            final int charges = Math.min(npc.getAiVarInt("charges", 0) + 1, 120);
            final int max_charges = 120;

            npc.setAiVar("charges", charges);
            npc.setDisplayEffect(0x02);

            if(charges == 30)
            {
                npc.getKnownList().getKnownPlayersInRadius(1000).forEach(player ->
                        player.sendPacket(new ExShowScreenMessage(NpcStringId.S1_HAS_CHARGED_THE_CANNON, ExShowScreenMessage.TOP_CENTER, 10000, caster.getName())));
            }
            else if(charges == 120)
            {
                _chargedGenerator.add(npc);

                if(_chargedGenerator.size() == 4)
                {
                    npc.getKnownList().getKnownPlayersInRadius(1000).forEach(player ->
                            player.sendPacket(new ExShowScreenMessage(NpcStringId.S1_HAS_CHARGED_THE_CANNON, ExShowScreenMessage.TOP_CENTER, 10000, caster.getName())));

                    nextStage(3);

                    _chargedGenerator.clear();
                }
            }


            npc.getKnownList().getKnownPlayersInRadius(1000).forEach(player ->
                    player.sendPacket(new ExSendUIEvent(player, 5, -1, charges, max_charges, NpcStringId.CHARGING.getId())));
        }

        return null;
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        switch (npc.getNpcId())
        {
            case NPC_ATTACKER_GENERATORS_1:
            case NPC_ATTACKER_GENERATORS_2:
            case NPC_ATTACKER_GENERATORS_3:
            case NPC_ATTACKER_GENERATORS_4:
                npc.getKnownList().getKnownNpcInRadius(1000).forEach(cha ->
                {
                    if(cha.getNpcId() == NPC_SCHEME_GENERATOR)
                    {
                        ((L2MonsterInstance) npc).attackCharacter(cha);
                        ((L2GuardInstance) cha).attackCharacter(npc);
                    }

                    if(cha.getNpcId() == NPC_GENERATOR)
                    {
                        ((L2MonsterInstance) npc).addDamageHate(cha, 0, 98);
                    }
                });
                break;
            case NPC_SCHEME_GENERATOR:
            {
                ((L2GuardInstance) npc).setCanAttackPlayer(false);
                ((L2GuardInstance) npc).setCanAttackGuard(false);
                npc.setIsMortal(true);
                npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NS_Strings[Rnd.get(NS_Strings.length)]));
                break;
            }
            case NPC_GENERATOR:
            {
                npc.setIsNoRndWalk(false);
                npc.setIsMortal(true);
                break;
            }
            case NPC_ATTACKER_BIG_VORTEX:
            {
                npc.getKnownList().setDistanceToWatch(250);
                npc.startWatcherTask(250);
                npc.setIsRunning(true);
                break;
            }
            case NPC_ATTACKER_SMALL_VORTEX:
            {
                npc.getKnownList().setDistanceToWatch(500);
                npc.startWatcherTask(500);
                npc.setIsRunning(true);
                break;
            }
        }
        return super.onSpawn(npc);
    }

    @Override
    public void onSeePlayer(L2Npc watcher, L2PcInstance player)
    {
        if(watcher == null || watcher.getKnownList().getKnownPlayersInRadius(500).isEmpty() || player == null)
        {
            return;
        }

        if (!player.isDead() && !player.isAlikeDead() && !player.isInvul())
        {
            switch (watcher.getNpcId())
            {
                case NPC_ATTACKER_BIG_VORTEX:
                {
                    player.setCurrentHp(1.0);
                    player.setCurrentMp(1.0);
                    player.setCurrentCp(1.0);
                    break;
                }
                case NPC_ATTACKER_SMALL_VORTEX:
                {
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    player.startParalyze();

                    int _x = watcher.getX();
                    int _y = watcher.getY();
                    int _z = watcher.getZ();

                    if(Config.GEODATA_ENABLED)
                    {
                        Location destiny = GeoEngine.getInstance().moveCheck(player.getX(), player.getY(), player.getZ(), _x, _y, _z, player.getInstanceId());
                        _x = destiny.getX();
                        _y = destiny.getY();
                        _z = destiny.getZ();
                    }

                    player.broadcastPacket(new FlyToLocation(player, _x, _y, _z, FlyToLocation.FlyType.WARP_FORWARD, 1000, 1000, 1000));
                    player.setXYZ(_x, _y, _z);
                    player.broadcastPacket(new ValidateLocation(player));
                    break;
                }
            }
        }
    }

    @Override
    public String onNpcDie(L2Npc npc, L2Character killer)
    {
        if(npc.getNpcId() == LINDVIOR_RAID)
        {
            if(_mobsSpawnTask != null)
            {
                _mobsSpawnTask.cancel(true);
                _mobsSpawnTask = null;
            }

            _zoneLair.broadcastPacket(new ExShowScreenMessage(NpcStringId.HONORABLE_WARRIORS_HAVE_DRIVEN_OFF_LINDVIOR_THE_EVIL_WIND_DRAGON, ExShowScreenMessage.TOP_CENTER, 5000));

            GrandBossManager.getInstance().setBossStatus(LINDVIOR_RAID, DEAD);

            long respawnTime = (long) Config.INTERVAL_OF_LINDVIOR_SPAWN + Rnd.get(Config.RANDOM_OF_LINDVIOR_SPAWN);

            // also save the respawn time so that the info is maintained past reboots
            StatsSet info = GrandBossManager.getInstance().getStatsSet(LINDVIOR_RAID);

            info.set("respawn_time", (System.currentTimeMillis() + respawnTime));

            GrandBossManager.getInstance().setStatsSet(LINDVIOR_RAID, info);

            ThreadPoolManager.getInstance().scheduleAi(() -> npc.getLocationController().delete(), 5000L);
            ThreadPoolManager.getInstance().scheduleGeneral(new UnlockLindvior(), respawnTime);
        }

        if(npc.getNpcId() == NPC_GENERATOR && npc.isDead())
        {
            _zoneLair.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_GENERATOR_HAS_BEEN_DESTROYED, ExShowScreenMessage.TOP_CENTER, 5000));

            _activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(Clean::new, 20000L);
        }

        return super.onNpcDie(npc, killer);
    }

    private void nextStage(int _taskId)
    {
        switch (_taskId)
        {
            case 1: // Спавним генераторы, схемы и аттакеров
            {
                _zoneLair.getPlayersInside().forEach(player ->
                        player.broadcastPacket(new EventTrigger(eventTriggerPower, true)));

                L2GuardInstance guard;
                for(int[] spawnInfo : CONTROL_GENERATOR_SPAWNS)
                {
                    guard = (L2GuardInstance) addSpawn(NPC_GENERATOR, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, false);
                    guard.setDisplayEffect(0x01);

                    _generatorSpawn.add(guard);
                }

                _mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(() ->
                {
                    L2GuardInstance guard1;
                    for (int[] spawnInfo : SCHEME_GENERATOR_SPAWNS) {
                        guard1 = (L2GuardInstance) addSpawn(NPC_SCHEME_GENERATOR, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true);
                        guard1.setIsNoRndWalk(true);

                        _schemeSpawn.add(guard1);
                    }

                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_1) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_1, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }
                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_2) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_2, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }
                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_3) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_3, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }
                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_4) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_4, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }
                }, 7000L);
                break;
            }
            case 2: // После активации 4х генераторов, ждем их зарядки
            {
                if (_announceTask != null) {
                    _announceTask.cancel(true);
                    _announceTask = null;
                }

                _generatorSpawn.forEach(npc ->
                {
                    npc.setDisplayEffect(0x01);
                    npc.setIsMortal(true);
                    npc.broadcastPacket(new NpcInfo(npc));
                });

                _zoneLair.getPlayersInside().forEach(player ->
                {
                    player.broadcastPacket(new EventTrigger(eventTriggerGenerators1, false));
                    player.broadcastPacket(new EventTrigger(eventTriggerGenerators2, false));
                    player.broadcastPacket(new EventTrigger(eventTriggerGenerators3, false));
                    player.broadcastPacket(new EventTrigger(eventTriggerGenerators4, false));

                    player.broadcastPacket(new EventTrigger(eventTriggerPower, false));
                    player.broadcastPacket(new EventTrigger(eventTriggerProtect, true));

                    player.showQuestMovie(ExStartScenePlayer.LINDVIOR_ARRIVE);

                    _generatorSpawn.forEach(npc -> npc.sendInfo(player));
                });

                _mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(() ->
                {
                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_1) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_1, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }
                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_2) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_2, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }
                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_3) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_3, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }
                    for (int[] spawnInfo : ATTACKER_GENERATOR_SPAWNS_4) {
                        _otherSpawn.add(addSpawn(NPC_ATTACKER_GENERATORS_4, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true));
                    }

                    _lindvior2 = addSpawn(LINDVIOR_FAKE, CENTER_LOCATION, false);
                }, 30000L);

                _announceTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> _zoneLair.getPlayersInside().forEach(player ->
                {
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.CHARGE_THE_CANNON_USING_THE_GENERATOR, ExShowScreenMessage.TOP_CENTER, 7000));
                    player.sendPacket(Rnd.get(GENERATOR_MESSAGES));
                }), 30000, 10000);

                _schemeSpawn.forEach(npc ->
                {
                    npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NS_Strings[Rnd.get(NS_Strings.length)]));

                    for (L2GuardInstance generator : _generatorSpawn)
                    {
                        npc.setTarget(generator);
                        npc.doCast(SKILL.getSkill());
                    }
                });

                _skillCastTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(() ->
                        _generatorSpawn.forEach(npc ->
                        {
                            if (!npc.isCastingNow() && npc.getFirstEffect(SKILL.getSkill()) == null && !_chargedGenerator.contains(npc)) {
                                npc.setTarget(npc);
                                npc.doCast(SKILL.getSkill());
                            }
                        }), 15000, 15000);

                break;
            }
            case 3: // После зарядки всех генераторов
            {
                /**
                 * 1 Этап
                 * Первый этап начинается когда Линдвиор спускается на землю, и до того как у него не станет 80% HP.
                 * Если стоять перед драконом, можно получить урон от удара ветром, а если стоять со спины,
                 * то вы получите урон от удара хвостом.
                 * Дракон будет использовать умение Дыхание Ветра - оно наносит урон и отбрасывает спереди дракона,
                 * и умение Body Slam, которое наносит урон и оглушает цели вокруг.
                 * Во время фарма Линдвиор будет призывать рейдовых бойцов.
                 * Линдрако - id=25895 id=29243 id=29242 id=29241 id=25897 id=25896
                 */

                if (_announceTask != null) {
                    _announceTask.cancel(true);
                    _announceTask = null;
                }

                if (_skillCastTask != null) {
                    _skillCastTask.cancel(true);
                    _skillCastTask = null;
                }

                if (_mobsSpawnTask != null) {
                    _mobsSpawnTask.cancel(true);
                    _mobsSpawnTask = null;
                }

                _lindvior2.getSpawn().stopRespawn();
                _lindvior2.getLocationController().delete();

                _generatorSpawn.forEach(npc ->
                {
                    if (npc != null) {
                        npc.getSpawn().stopRespawn();
                        npc.getLocationController().delete();
                    }
                });
                _generatorSpawn.clear();

                _schemeSpawn.forEach(npc ->
                {
                    npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NS_Strings[Rnd.get(NS_Strings.length)]));

                    if (npc != null) {
                        npc.getSpawn().stopRespawn();
                        npc.getLocationController().delete();
                    }
                });
                _schemeSpawn.clear();

                for (int[] spawnInfo : LINDVIOR_SPAWN) {
                    _lindvior = (L2GrandBossInstance) addSpawn(LINDVIOR_RAID, spawnInfo[0], spawnInfo[1], spawnInfo[2], spawnInfo[3], false, 0, true);
                }
                _lindvior.broadcastPacket(new NpcInfo(_lindvior));

                GrandBossManager.getInstance().setBossStatus(LINDVIOR_RAID, FIGHTING);

                _lindvior.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);

                ThreadPoolManager.getInstance().scheduleGeneral(() -> _zoneLair.broadcastPacket(new ExShowScreenMessage(NpcStringId.LINDVIOR_HAS_FALLEN_FROM_THE_SKY, ExShowScreenMessage.TOP_CENTER, 5000)), 15000);

                ThreadPoolManager.getInstance().scheduleGeneral(() ->
                {
                    _otherSpawn.forEach(npc ->
                    {
                        if (npc != null) {
                            npc.getSpawn().stopRespawn();
                            npc.getLocationController().delete();
                        }
                    });
                    _otherSpawn.clear();

                    spawnServitor(15, 2000, _lindvior.getLoc(), LINDVIOR_SERVITOR);
                }, 25000);

                break;
            }
        }
    }

    /**
     * Призываем монстров
     * @param count количество
     * @param radius разброс
     * @param loc локация
     * @param npcIds списко монстров
     */
    private void spawnServitor(int count, int radius, Location loc, int... npcIds)
    {
        if(radius > 0)
        {
            loc.setX(loc.getX() + Rnd.get(-radius, radius));
            loc.setY(loc.getY() + Rnd.get(-radius, radius));
        }

        L2Npc npc;
        for(int i = 0; i < count; i++)
        {
            npc = addSpawn(Rnd.get(npcIds), loc, false, 0, true);

            attackRandomTarget(npc);

            _otherSpawn.add(npc);
        }
    }

    private void spawnVortexes(int count, int radius, Location loc, int npcId)
    {
        if(radius > 0)
        {
            loc.setX(loc.getX() + Rnd.get(-radius, radius));
            loc.setY(loc.getY() + Rnd.get(-radius, radius));
        }

        L2Npc npc;
        for(int i = 0; i < count; i++)
        {
            npc = addSpawn(npcId, loc, false, 0, true);

            attackRandomTarget(npc);

            _otherSpawn.add(npc);
        }
    }

    /**
     * Атакуем случайную цель
     * @param npc атакующий
     */
    private void attackRandomTarget(L2Npc npc)
    {
        if(!npc.isAttackable())
            return;

        Collection<L2PcInstance> players = npc.getKnownList().getKnownPlayers().values();
        if(players.size() > 0) {
            ((L2Attackable) npc).attackCharacter(Rnd.get(players.toArray(new L2PcInstance[players.size()])));
        }
    }

    /**
     * Хит поинт Ченжер, Контролит процен ХП у боссов.
     */
    private class OnHpChange extends AbstractHookImpl
    {
        @Override
        public void onHpChange(L2Character character, double damage, double fullDamage)
        {
            int status = GrandBossManager.getInstance().getBossStatus(LINDVIOR_RAID);

            if(!character.isNpc())
                return;

            double percent = ((character.getCurrentHp() - damage) / character.getMaxHp()) * 100;
            if(percent <= 20 && _status == 5)
            {
                /**
                 * 5 Этап
                 * HP Дракона спускается ниже 20%.
                 * Теперь, вам дается 10 минут, чтобы убить Линдвиора.
                 * Он использует умение Ярость, и урон по нему снизится.
                 * Если вы не успеете убить его за 10 минут, то всех выкинет из рейд зоны.
                 * На последнем Этапе Lindvior призывает рейдовых бойцов и вихри, использует умения как на 1 этапе,
                 * кроме этого, если вы попадёте в вихрь, то ваша скорость снизится и вас отбросит.
                 */
                _lindvior.doCast(SKILL_FLY.getSkill());

                _lindvior.getLocationController().delete();
                _lindvior.getSpawn().stopRespawn();
                _lindvior = (L2GrandBossInstance) addSpawn(LINDVIOR_RAID, _lindvior.getLoc(), false);

                _zoneLair.broadcastPacket(new ExShowScreenMessage(NpcStringId.LINDVIOR_HAS_LANDED, ExShowScreenMessage.TOP_CENTER, 5000));

                _lindvior.setCurrentHp(_lindvior.getMaxHp() * 0.2);
                _lindvior.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);

                _bigVortexesTask = ThreadPoolManager.getInstance().scheduleGeneral(() ->
                        spawnVortexes(1, 300, _lindvior.getLoc(), NPC_ATTACKER_BIG_VORTEX), 1000);

                spawnServitor(25, 2000, _lindvior.getLoc(), LINDVIOR_SERVITOR);

                int timeToExit = 600000;
                startTimerTask(timeToExit);

                if (status == DEAD)
                {
                    _activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(Clean::new, 5000);
                }
                _status = 6;
                _log.info(_status + " is farm Epic Lindvior");

            }
            else if(percent <= 35 && _status == 4)
            {
                /**
                 * 4 Этап
                 * Когда HP спустится ниже 35%, Рейд Босс призовет Малые Вихри.
                 */
                spawnVortexes(10, 2000, _lindvior.getLoc(), NPC_ATTACKER_SMALL_VORTEX);

                _status = 5;
                _log.info(_status + " is farm Epic Lindvior");

            }
            else if(percent <= 40 && _status == 3)
            {
                /**
                 * 4 Этап
                 * HP Линдвиора спускается до 40%, и он опять поднимается в воздух.
                 */
                if (_skillCastTask != null)
                {
                    _skillCastTask.cancel(true);
                    _skillCastTask = null;
                }

                _lindvior.doCast(SKILL_FLY.getSkill());
                _lindvior.getLocationController().delete();
                _lindvior.getSpawn().stopRespawn();
                _lindvior = (L2GrandBossInstance) addSpawn(LINDVIOR_FLY, _lindvior.getLoc(), false);
                _lindvior.setCurrentHp(_lindvior.getMaxHp() * 0.4);
                _lindvior.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);

                if (SpawnTable.getInstance().getFirstSpawn(NPC_ATTACKER_BIG_VORTEX) != null)
                {
                    if (_vortex.getNpcId() == NPC_ATTACKER_SMALL_VORTEX)
                    {
                        _vortex.getSpawn().stopRespawn();
                        _vortex.getLocationController().delete();
                    }
                }

                _status = 4;
                _log.info(_status + " is farm Epic Lindvior");

            }
            else if(percent <= 60 && _status == 2)
            {
                /**
                 * 3 Этап
                 * Когда HP Линдвиора спускается ниже 60%, он спускается на землю.
                 * Он начинает использовать такие же умения, как и на 1 этапе,
                 * и дополнительно использует умение Тёмный Ветер - он отражает урон.
                 * Это умение легко заметить - Линдвиор потемнеет при его использовании.
                 * Также появляются новые рейдовые бойцы (C)
                 * Линдрако - id=25895 id=29243 id=29242 id=29241 id=25897 id=25896
                 */
                _lindvior.doCast(SKILL_FLY.getSkill());

                _lindvior.getLocationController().delete();
                _lindvior.getSpawn().stopRespawn();
                _lindvior = (L2GrandBossInstance) addSpawn(LINDVIOR_RAID, _lindvior.getLoc(), false);
                _zoneLair.broadcastPacket(new ExShowScreenMessage(NpcStringId.LINDVIOR_HAS_LANDED, ExShowScreenMessage.TOP_CENTER, 5000));
                _lindvior.setCurrentHp(_lindvior.getMaxHp() * 0.6);
                _lindvior.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);

                spawnServitor(15, 2000, _lindvior.getLoc(), LINDVIOR_SERVITOR);

                _skillCastTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
                        _lindvior.doCast(SKILL_REFLECT.getSkill()), 80000, 80000);


                _status = 3;
                _log.info(_status + " is farm Epic Lindvior");

            }
            else if(percent <= 75 && _status == 1)
            {
                /**
                 * 2 Этап
                 * Когда HP Линдвиора снизится до 75%, он вызовет огромный вихрь, который будет перемещаться по всей локации.
                 * Если вы попадете в гигантский вихрь, ваше HP, MP, CP опустится до 1.
                 * Линдвиор также призывает новых рейдовых бойцов (B).
                 * Линдрако - id=25895 id=29243 id=29242 id=29241 id=25897 id=25896
                 */

                _bigVortexesTask = ThreadPoolManager.getInstance().scheduleGeneral(() ->
                        spawnVortexes(1, 300, _lindvior.getLoc(), NPC_ATTACKER_BIG_VORTEX), 1000);

                _zoneLair.getPlayersInside().stream().forEach(p ->
                        p.broadcastPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(14211706), ExShowScreenMessage.TOP_CENTER, 2000))); //Появился огромный вихрь.

                _status = 2;
                _log.info(_status + " is farm Epic Lindvior");

            }
            else if(percent <= 80 && _status == 0)
            {
                /**
                 * 2 Этап
                 * Когда HP Линдвиора меньше 80%, он поднимается в воздух и перемещается по всему Алтарю Жертвоприношения,
                 * нанося мега удары ветром.
                 * С небольшой вероятностью появляются вихри, которые будут затягивать персонажей и наносить урон.
                 */

                _zoneLair.getPlayersInside().stream().forEach(p ->
                        p.broadcastPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(14211705), ExShowScreenMessage.TOP_CENTER, 2000))); //От Линдвиора веет силой.

                _lindvior.doCast(SKILL_FLY_UP.getSkill());
                _lindvior.doCast(SKILL_RABIES.getSkill());
                _zoneLair.getPlayersInside().forEach(player ->
                        player.broadcastPacket(new EventTrigger(21170120, true)));

                ThreadPoolManager.getInstance().scheduleGeneral(() ->
                {
                    _lindvior.getSpawn().stopRespawn();
                    _lindvior.getLocationController().delete();

                    _lindvior = (L2GrandBossInstance) addSpawn(LINDVIOR_FLY, _lindvior.getLoc(), false, 0, true);
                    _lindvior.setCurrentHp(_lindvior.getMaxHp() * 0.8);
                    _lindvior.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);
                }, 2000);

                spawnVortexes(13, 2000, _lindvior.getLoc(), NPC_ATTACKER_SMALL_VORTEX);

                ThreadPoolManager.getInstance().scheduleGeneral(() ->
                        _zoneLair.getPlayersInside().forEach(player ->
                                player.broadcastPacket(new EventTrigger(21170120, false))), 15000);

                _status = 1;

                _log.info(_status + " is farm Epic Lindvior");
            }
        }
    }

    private void startTimerTask(int time)
    {
        _zoneLair.getPlayersInside().forEach(player ->
        {
            long _startedTime = System.currentTimeMillis() + time;
            final int timeLeft = (int) ((_startedTime - System.currentTimeMillis()) / 60000);

            ExShowScreenMessage sm = new ExShowScreenMessage(NpcStringId.LINDVIOR_GATHER_STRENGTH_LEFT_S1_MINUTES, ExShowScreenMessage.TOP_CENTER, 5000, String.valueOf(timeLeft));
            player.broadcastPacket(sm);

            if (timeLeft <= 1)
            {
                _activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(Clean::new, 5000);
            }
        });
    }

    /**
     * Обновляем статус босса при его респауне.
     */
    private class UnlockLindvior implements Runnable
    {
        @Override
        public void run()
        {
            GrandBossManager.getInstance().setBossStatus(LINDVIOR_RAID, DORMANT);
            if(EARTH_QUAKE_ON_SPAWN)
            {
                WorldManager.getInstance()
                        .getAllPlayers()
                        .valueCollection()
                        .forEach(player -> player.broadcastPacket(new EarthQuake(CENTER_LOCATION.getX(), CENTER_LOCATION.getY(), CENTER_LOCATION.getZ(), CENTER_LOCATION.getHeading(), 10)));
            }
        }
    }
}
