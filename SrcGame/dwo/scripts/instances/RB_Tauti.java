package dwo.scripts.instances;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.ai.individual.TautiMonsters;
import dwo.scripts.ai.individual.Zahak;
import dwo.scripts.ai.individual.raidbosses.Tauti;
import javolution.util.FastList;

import java.util.Calendar;
import java.util.List;

/**
 * TODO
 * Войдя в Тренировочную зону игроки увидят катсцену где будет происходить гражданская война между Софа и Кунда. Хотя у Софа численное превосходство Кунда значительно сильнее. Игроки должны помочь Софа пока их не уничтожили и пройти на следующую стадию временной зоны.
 *
 * Kunda NpcSay:
 * 1801600, 1801599, 1801602, 1801610, 1801603, 1801604, 1801605, 1801606, 1801607, 1801608, 1801609
 * Sofa NpcSay:
 * 1801614, 1801611, 1801612, 1801613, 1801614, 1801615, 1801616, 1801617, 1801618, 1801619
 *
 * http://l2central.info/wiki/Таути_(экстремальный)
 * http://forums.goha.ru/showthread_0_0_t772034
 *
 * http://www.youtube.com/watch?v=lxdEwA1uuHA
 * http://www.youtube.com/watch?v=qCtYitQVhFM
 * http://www.youtube.com/watch?v=Gsj9UsOoDz8
 *
 * 29233 u,Таути\0 a, 9C E8 A9 -1
 29234 u,Таути\0 a, 9C E8 A9 -1
 29235 u,Таути\0 a, 9C E8 A9 -1
 29236 u,Таути\0 a, 9C E8 A9 -1
 29237 u,Таути\0 a, 9C E8 A9 -1
 29238 u,Таути\0 a, 9C E8 A9 -1
 29239 u,Таути\0 a, 9C E8 A9 -1
 *
 * 35295	Бутыль с Душой Таути		u,Бутыль с душой Таути. Может вселить душу Таути в браслет Таути. Нельзя обменять/выбросить/продать в личной торговой лавке. Можно положить в личное хранилище. \0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 * 35723	Коробка с оружием Фантазмы	Таути	u,Коробка с неопознанным оружием Фантазмы.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35724	Коробка с доспехом Кадейры	Таути	u,Коробка с неопознанным доспехом Кадейры.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35725	Коробка с аксессуарами Кадейры	Таути	u,Коробка с неопознанными аксессуарами Кадейры\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35726	Коробка с усилителем ранга R	Таути	u,Коробка с усилителем ранга R.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35727	Коробка с материалами для Талисмана Семени	Таути	u,Коробка с материалами для Талисмана Семени.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35728	Сундук Поддержки Героя	Таути	u,При двойном щелчке на предмет можно получить следующие предметы: Ящик с Оружием Фантазмы, Ящик с Доспехами Кадейры, Ящик Усиления (R ), Ящик с Талисманом,  Семени, Адены, Заряд Души, Благословенный Заряд Духа, Свиок SP.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35293	Браслет Таути		u,MP +216, при ношении активирует 5 ячеек талисманов.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35294	Улучшенный Браслет Таути		u,Браслет Таути с улучшенными функциями. При ношении активирует 5 ячеек талисманов. MP +216 СИЛ +1, ИНТ +1. Нельзя обменять/выбросить/модифицировать, выставить на продажу в личной торговой лавке.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	4
 35570	Кольцо Таути		u,Кольцо Таути. Обладает эффектом увеличения Огня/Тьмы на 25, MP на 38, силы крит. атаки на 15%, силы атаки умениями на 5%, урона PvP на 5%, сопротивления параличу/оковам/изгнанию на 10, сопротивления отражению урона на 5, сопротивления вампиризму на 5; уменьшения получаемого урона PvP на 5%, увеличивает шанс критической атаки при ношении дробящего оружия на 54.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 34998	Одноручный топор Таути		u,Оружие из зачарованных осколков Таути. Будучи экипирован, позволяет использовать умение Бешенство Таути. Скор. Атк. +15%, шанс Крит. Атк. +150, Макс. HP +25%, Физ. Атк. +415. Нельзя модифицировать, придать свойства стихий/особые свойства и зачаровать.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 34999	Топор Таути		u,Оружие из зачарованных осколков Таути. Будучи экипирован, позволяет использовать умение Бешенство Таути. Скор. Атк. +15%, шанс Крит. Атк. +150, Макс. HP +25%, Физ. Атк. +415. Нельзя модифицировать, придать свойства стихий/особые свойства и зачаровать.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35000	Двуручный топор Таути		u,Оружие из зачарованных осколков Таути. Будучи экипирован, позволяет использовать умение Бешенство Таути. Скор. Атк. +15%, шанс Крит. Атк. +150, Макс. HP +25%, Физ. Атк. +415. Нельзя модифицировать, придать свойства стихий/особые свойства и зачаровать.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 35001	Парные топоры Таути		u,Оружие из зачарованных осколков Таути. Будучи экипирован, позволяет использовать умение Бешенство Таути. Скор. Атк. +15%, шанс Крит. Атк. +150, Макс. HP +25%, Физ. Атк. +415. Нельзя модифицировать, придать свойства стихий/особые свойства и зачаровать.\0	-1	0																																																								0	0						0					0	a,	0			0	a,	1
 *
 */

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class RB_Tauti extends Quest
{
    // Hellfire NPC (Tauti teleport controller)
    public static final int HELLFIRE = 33678;
    // ID инстансов
    private static final int INSTANCE_ID_LIGHT = InstanceZoneId.TAUTI_WARZONE.getId();
    private static final int INSTANCE_ID_HARD = InstanceZoneId.TAUTI_WARZONE_EPIC_BATTLE.getId();

    // Точка входа в инстанс
    private static final Location ENTRANCE = new Location(-146151, 186525, -11736);
    private static final Location EXIT_LIGHT = new Location(-147733, 152610, -14056);
    private static final Location EXIT_HARD = new Location(-146970, 250598, -14027);
    private static final Location TAUTI_ROOM = new Location(-149244, 209882, -10199);
    private static final Location TAUTI_SPAWN = new Location(-147264, 212896, -10056);
    private static final Location TAUTI_ROOM_TELEPORT = new Location(-147262, 211318, -10040);

    private static final Location TAUTI_LAST_STAGE = new Location(-149244, 209882, -10199);

    // Зоны
    private static final int TAUTI_ROOM_ZONE = 400081;

    // Двери
    private static final int TAUTI_HALL_DOOR = 15240001;
    private static final int TAUTI_ROOM_DOOR = 15240002;
    private static final int[] KUNDA_MESSAGES = {
            1801600, 1801599, 1801602, 1801610, 1801603, 1801604, 1801605, 1801606, 1801607, 1801608, 1801609
    };
    private static final int[] SOFA_MESSAGES = {
            1801614, 1801611, 1801612, 1801613, 1801614, 1801615, 1801616, 1801617, 1801618, 1801619
    };

    private static RB_Tauti _tautiInstance;

    protected OnHpChange _hpChange;
    private static final int KEY_OF_DARKNES = 34899;

    public RB_Tauti()
    {
        addKillId(TautiMonsters.KUNDA_MINION, TautiMonsters.ZAHAQ);

        addKillId(TautiMonsters.KUNDAS);

        addFirstTalkId(HELLFIRE);
        addTalkId(HELLFIRE);
        addAskId(HELLFIRE, -33678);
        addEnterZoneId(TAUTI_ROOM_ZONE);
    }

    public static void main(String[] args)
    {
        _tautiInstance = new RB_Tauti();
    }

    public static RB_Tauti getInstance()
    {
        return _tautiInstance;
    }

    private long getReuseTime(boolean isHardInstance)
    {
        // Откаты по времени в среду в 6:30
        Calendar _instanceTimeWednesday = Calendar.getInstance();

        Calendar currentTime = Calendar.getInstance();

        _instanceTimeWednesday.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        _instanceTimeWednesday.set(Calendar.HOUR_OF_DAY, 6);
        _instanceTimeWednesday.set(Calendar.MINUTE, 30);
        _instanceTimeWednesday.set(Calendar.SECOND, 0);

        if(_instanceTimeWednesday.compareTo(currentTime) < 0)
        {
            _instanceTimeWednesday.add(Calendar.DAY_OF_MONTH, 7);
        }

        return _instanceTimeWednesday.getTimeInMillis();
    }

    protected int enterInstance(L2PcInstance player, String template, boolean isHardInstance)
    {
        if(!player.isGM())
        {
            return 0;
        }

        int instanceId;
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

        if(player.isGM())
        {
            world = null;
        }

        if(world != null)
        {
            if(!(world instanceof TautiWorld))
            {
                player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
                return 0;
            }

            if(!((TautiWorld) world).playersInside.contains(player))
            {
                ((TautiWorld) world).playersInside.add(player);
            }

            if(world.status < 2)
            {
                player.teleToInstance(ENTRANCE, world.instanceId);
            }
            else
            {
                player.teleToInstance(TAUTI_ROOM_TELEPORT, world.instanceId);
            }
            return world.instanceId;
        }
        else
        {
            world = new TautiWorld();
            ((TautiWorld) world).isHardInstance = isHardInstance;
            int instanceTemplateId = ((TautiWorld) world).isHardInstance ? INSTANCE_ID_HARD : INSTANCE_ID_LIGHT;
            if(!checkConditions(player, instanceTemplateId))
            {
                return 0;
            }

            instanceId = InstanceManager.getInstance().createDynamicInstance(template);

            world.instanceId = instanceId;
            world.templateId = instanceTemplateId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            if(player.isGM() && player.getParty() == null)
            {
                player.teleToInstance(ENTRANCE, instanceId);
                world.allowed.add(player.getObjectId());
                ((TautiWorld) world).playersInside.add(player);
                ((TautiWorld) world).playersInLairZone.add(player);
                init((TautiWorld) world);
                return instanceId;
            }

            if(player.getParty() != null)
            {
                if(player.getParty().getCommandChannel() == null)
                {
                    for(L2PcInstance partyMember : player.getParty().getMembers())
                    {
                        partyMember.teleToInstance(ENTRANCE, instanceId);
                        world.allowed.add(partyMember.getObjectId());
                        ((TautiWorld) world).playersInside.add(partyMember);
                        ((TautiWorld) world).playersInLairZone.add(player);

                        for(L2Effect effect : partyMember.getAllEffects())
                        {
                            effect.exit();
                        }
                    }
                    init((TautiWorld) world);
                    return instanceId;
                }
                else
                {
                    for(L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
                    {
                        channelMember.teleToInstance(ENTRANCE, instanceId);
                        world.allowed.add(channelMember.getObjectId());
                        ((TautiWorld) world).playersInside.add(channelMember);
                        ((TautiWorld) world).playersInLairZone.add(player);

                        for(L2Effect effect : channelMember.getAllEffects())
                        {
                            effect.exit();
                        }
                    }
                    init((TautiWorld) world);
                    return instanceId;
                }
            }
            return 0;
        }
    }

    private void init(TautiWorld world)
    {
        _hpChange = new OnHpChange();

        Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
        if(instance != null)
        {
            world.status = 1;

            if(world.isHardInstance)
            {
                instance.setSpawnLoc(EXIT_HARD);
            }
            else
            {
                instance.setSpawnLoc(EXIT_LIGHT);
            }

            world.spawningWarMonsters = true;
            for(L2Spawn spawn : instance.getGroupSpawn("hall"))
            {
                L2Npc npc = spawn.spawnOne(false);
                if(TautiMonsters.isKunda(npc))
                {
                    world.kundas.add(npc);
                    ++world.kundasToKill;
                }
                else if(TautiMonsters.isSofa(npc))
                {
                    world.sofas.add(npc);
                }
            }
            world.spawningWarMonsters = false;

            for(L2Spawn spawn : instance.getGroupSpawn("upstair_hall"))
            {
                L2Npc npc = spawn.spawnOne(false);
                npc.setIsNoRndWalk(true);
            }

            instance.addTask("talkerTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
                if(world != null)
                {
                    L2Npc kunda = world.kundas.get(Rnd.get(world.kundas.size()));
                    L2Npc sofa = world.sofas.get(Rnd.get(world.sofas.size()));

                    int kundaMessage = KUNDA_MESSAGES[Rnd.get(KUNDA_MESSAGES.length)];
                    int sofaMessage = SOFA_MESSAGES[Rnd.get(SOFA_MESSAGES.length)];

                    kunda.broadcastPacket(new NS(kunda.getObjectId(), ChatType.NPC_ALL, kunda.getNpcId(), kundaMessage));
                    sofa.broadcastPacket(new NS(kunda.getObjectId(), ChatType.NPC_ALL, kunda.getNpcId(), sofaMessage));
                }
            }, 2000, 8000));
        }
    }

    public boolean enterInstance(L2PcInstance player, boolean isExtreme)
    {
        return enterInstance(player, "RB_Tauti.xml", isExtreme) != 0;
    }

    @Override
    public String onNpcDie(L2Npc npc, L2Character killer)
    {
        int npcId = npc.getNpcId();
        TautiWorld world = InstanceManager.getInstance().getInstanceWorld(npc, TautiWorld.class);
        Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

        if(npcId == TautiMonsters.KUNDA_MINION || npcId == TautiMonsters.ZAHAQ)
        {
            if(world != null)
            {
                if(npcId == TautiMonsters.KUNDA_MINION)
                {
                    world.kundaMinionKilled = true;
                }
                else if(npcId == TautiMonsters.ZAHAQ)
                {
                    world.zahaqKilled = true;
                    ((L2MonsterInstance) npc).dropItem(killer.getActingPlayer(), KEY_OF_DARKNES, 1);
                }
                else if(world.sofas.size() < 1)
                {
                    instance.setDuration(5000);
                    instance.setEmptyDestroyTime(0);
                }
                else if(world.kundasToKill > 0 && world.isKundaAttacker(npc))
                {
                    --world.kundasToKill;
                }
            }
        }
        else if(npcId == Tauti.TAUTI_HARD_AXE || npcId == Tauti.TAUTI_NORMAL_AXE)
        {
            ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(world), 1000);
        }

        return null;
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        TautiWorld world = InstanceManager.getInstance().getInstanceWorld(npc, TautiWorld.class);

        if(ask == -33678)
        {
            switch (reply)
            {
                case 1:
                {
                    if (player.isGM())
                    {
                        return "33678-1.htm";
                    }

                    if (player.getParty() == null)
                    {
                        player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
                        return null;
                    }

                    if (player.getParty().getCommandChannel() == null)
                    {
                        player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
                    }

                    if (!player.getParty().getCommandChannel().isLeader(player) && !player.getParty().isLeader(player))
                    {
                        return "33678-no.htm";
                    }

                    if (player.getInventory().getItemByItemId(KEY_OF_DARKNES).getCount() != 0)
                    {
                        return "33678-1.htm";
                    }
                    else
                    {
                        return "33678-nokey.htm";
                    }
                }
                case 2:
                    if (player.isGM()) {
                        player.getInventory().destroyItemByItemId(ProcessType.QUEST, KEY_OF_DARKNES, 1, player, npc);
                        player.teleToInstance(TAUTI_ROOM, world.instanceId);
                    }
                    else if (player.getParty().getLeader().getInventory().getItemByItemId(KEY_OF_DARKNES).getCount() != 0)
                    {
                        player.getParty().getLeader().getInventory().destroyItemByItemId(ProcessType.QUEST, KEY_OF_DARKNES, 1, player, npc);

                        for (L2PcInstance member : player.getParty().getCommandChannel().getMembers())
                        {
                            member.teleToInstance(TAUTI_ROOM, world.instanceId);
                        }
                    }
                    break;
            }
        }
        return null;
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if(npc.getNpcId() == HELLFIRE)
        {
            Instance instance = InstanceManager.getInstance().getInstance(player.getInstanceId());

            if(instance != null)
            {
                instance.getDoor(TAUTI_HALL_DOOR).openMe();
                instance.getDoor(TAUTI_ROOM_DOOR).openMe();
            }

            return "33678.htm";
        }

        return null;
    }

    @Override
    public String onEnterZone(L2Character character, L2ZoneType zone)
    {
        synchronized(this)
        {
            if(zone.getId() == TAUTI_ROOM_ZONE && character instanceof L2PcInstance)
            {
                TautiWorld world = InstanceManager.getInstance().getInstanceWorld(character, TautiWorld.class);

                if(world == null)
                {
                    return null;
                }

                if(!world.playersInRaidRoom.contains(character))
                {
                    world.playersInRaidRoom.add((L2PcInstance) character);
                }

                if(world.status < 2)
                {
                    world.status = 2;
                    ThreadPoolManager.getInstance().scheduleGeneral(() ->
                    {
                        for (L2PcInstance player : world.playersInRaidRoom)
                        {
                            player.showQuestMovie(ExStartScenePlayer.SCENE_TAUTI_OPENING);
                        }
                    }, 10000);

                    ThreadPoolManager.getInstance().scheduleGeneral(() ->
                    {
                        int tautiId;

                        if(world.isHardInstance)
                        {
                            tautiId = Tauti.TAUTI_LIGHT;
                        }
                        else
                        {
                            tautiId = Tauti.TAUTI_HARD;
                        }

                        if(tautiId > 0)
                        {
                            world.tauti = addSpawn(tautiId, TAUTI_SPAWN.getX(), TAUTI_SPAWN.getY(), TAUTI_SPAWN.getZ(), 0, false, 0, false, world.instanceId);
                            world.tauti.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);
                        }

                        Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

                        if(instance != null)
                        {
                            instance.getDoor(TAUTI_HALL_DOOR).closeMe();
                            instance.getDoor(TAUTI_ROOM_DOOR).closeMe();
                        }
                    }, 52000);
                }
            }

            return super.onEnterZone(character, zone);
        }
    }

    @Override
    public String onExitZone(L2Character character, L2ZoneType zone)
    {
        if (character instanceof L2PcInstance)
        {
            TautiWorld world = InstanceManager.getInstance().getInstanceWorld(character, TautiWorld.class);

            if (world != null)
            {
                if (zone.getId() == TAUTI_ROOM_ZONE)
                {
                    world.playersInRaidRoom.remove(character);
                }
            }
        }
        return super.onExitZone(character, zone);
    }

    private boolean checkConditions(L2PcInstance player, int instanceTemplateId)
    {
        L2Party party = player.getParty();

		/* Для дебага */
        if(player.isGM())
        {
            Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), instanceTemplateId);
            if(System.currentTimeMillis() < reEnterTime)
            {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
                return false;
            }
            return true;
        }

        if(player.getParty() == null)
        {
            player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
            return false;
        }

        if(!party.isInCommandChannel())
        {
            party.broadcastPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
            return false;
        }

        int minPlayers = instanceTemplateId != INSTANCE_ID_HARD ? Config.MIN_TAUTI_PLAYERS : Config.MIN_TAUTI_HARD_PLAYERS;
        int maxPlayers = instanceTemplateId != INSTANCE_ID_HARD ? Config.MAX_TAUTI_PLAYERS : Config.MAX_TAUTI_HARD_PLAYERS;
        int minLevel = instanceTemplateId != INSTANCE_ID_HARD ? Config.MIN_LEVEL_TAUTI_PLAYERS : Config.MIN_LEVEL_TAUTI_HARD_PLAYERS;

        L2CommandChannel channel = player.getParty().getCommandChannel();
        if(!channel.getLeader().equals(player))
        {
            party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
            return false;
        }
        if(channel.getMemberCount() < minPlayers || channel.getMemberCount() > maxPlayers)
        {
            player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
            return false;
        }

        for(L2PcInstance member : channel.getMembers())
        {
			/* В инст пускает только перерожденных чаров и минимальный лвл с которого пускает 85. */
            if(member == null || member.getLevel() < minLevel || !member.isAwakened())
            {
                party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
                return false;
            }
            if(!Util.checkIfInRange(1000, player, member, true))
            {
                party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
                return false;
            }
            Long reEnterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), instanceTemplateId);
            if(System.currentTimeMillis() < reEnterTime)
            {
                party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
                return false;
            }
        }
        return true;
    }

    /**
     * Хит поинт Ченжер, Контролит процен ХП у боссов.
     */
    private class OnHpChange extends AbstractHookImpl
    {
        @Override
        public void onHpChange(L2Character character, double damage, double fullDamage)
        {
            TautiWorld world = InstanceManager.getInstance().getInstanceWorld(character, TautiWorld.class);

            if(!character.isNpc())
                return;

            double percent = ((character.getCurrentHp() - damage) / character.getMaxHp()) * 100;
            if(percent <= 15 && world.status == 3)
            {
                world.playersInRaidRoom.forEach(player ->
                        player.showQuestMovie(ExStartScenePlayer.SCENE_TAUTI_PHASE));

                world.tauti.teleToInstance(TAUTI_LAST_STAGE, world.instanceId);

                world.tauti.getHookContainer().removeHook(HookType.ON_HP_CHANGED, _hpChange);

                world.tauti.getSpawn().stopRespawn();
                world.tauti.getLocationController().delete();

                ThreadPoolManager.getInstance().scheduleGeneral(() -> {

                    world.tauti_axe = world.isHardInstance ? addSpawn(Tauti.TAUTI_HARD_AXE, new Location(-149244, 209882, -10199), true, true, world.instanceId) : addSpawn(Tauti.TAUTI_NORMAL_AXE, new Location(-149244, 209882, -10199), true, true, world.instanceId);
                    world.tauti_axe.getSpawn().doSpawn();
                    world.tauti_axe.setCurrentHp(world.tauti.getMaxHp() * 0.15);

                    world.status = 4;
                }, 15000);

            }
            else if(percent <= 50 && world.status == 2)
            {
                world.playersInRaidRoom.forEach(player ->
                        player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1801649), ExShowScreenMessage.TOP_CENTER, 5000)));

                ThreadPoolManager.getInstance().scheduleAi(() -> {
                    world.zahaq = addSpawn(Zahak.ZAHAK, world.tauti.getLoc(), true, true, world.instanceId);
                    world.zahaq = addSpawn(Zahak.ZAHAK, world.tauti.getLoc(), true, true, world.instanceId);
                    world.zahaq = addSpawn(Zahak.ZAHAK, new Location(-147261, 212622, -10065), true, world.instanceId);

                    world.playersInRaidRoom.forEach(player ->
                            player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1801650), ExShowScreenMessage.TOP_CENTER, 5000)));

                }, 5000);
                world.status = 3;
                world.tauti.getHookContainer().addHook(HookType.ON_HP_CHANGED, _hpChange);
            }
        }
    }


    private class DeathTask implements Runnable
    {
        private TautiWorld _world;
        private Instance _instance;
        private long _instanceTime;

        public DeathTask(TautiWorld world)
        {
            _world = world;
            _instanceTime = getReuseTime(world.isHardInstance);
            _instance = InstanceManager.getInstance().getInstance(world.instanceId);
        }

        @Override
        public void run()
        {
            _world.playersInRaidRoom.forEach(player ->
                    player.showQuestMovie(ExStartScenePlayer.SCENE_TAUTI_ENDING));

            _instance.setDuration(5 * 60 * 1000);
            _instance.setEmptyDestroyTime(0);
            _world.playersInRaidRoom.forEach(p -> 
            {
                p.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                InstanceManager.getInstance().setInstanceTime(p.getObjectId(), _world.isHardInstance ? INSTANCE_ID_HARD : INSTANCE_ID_LIGHT, _instanceTime);
                p.getInstanceController().setInstanceId(0);
            });
        }
    }

    public class TautiWorld extends InstanceWorld
    {
        public boolean isHardInstance;
        public List<L2PcInstance> playersInside = new FastList<>();
        public List<L2PcInstance> playersInLairZone = new FastList<>();
        public List<L2PcInstance> playersInRaidRoom = new FastList<>();
        public L2Npc tauti;
        public L2Npc zahaq;
        public L2Npc tauti_axe;
        public List<L2Npc> kundas = new FastList<>();
        public List<L2Npc> sofas = new FastList<>();
        public int kundasToKill;
        public boolean kundaMinionKilled;
        public boolean zahaqKilled;
        public boolean spawningWarMonsters;

        public boolean isKundaAttacker(L2Npc npc)
        {
            return kundas.contains(npc);
        }

        public boolean isSofaDefender(L2Npc npc)
        {
            return sofas.contains(npc);
        }
    }
}
