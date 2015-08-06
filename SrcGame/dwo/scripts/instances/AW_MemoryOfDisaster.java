package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.EventTrigger;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExCallToChangeClass;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.RunnableImpl;
import dwo.scripts.quests._10338_OvercomeTheRock;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: GenCloud
 * Date: 16.03.2015
 * Team: La2Era Team
 */
public class AW_MemoryOfDisaster extends Quest {

    private static final int Сельфин = 33477;

    private static final Location ENTRY_POINT = new Location(119667, -181970, -1856, 52800);
    private static final Location ENTRY_POINT_2 = new Location(116063, -183167, -1488, 64960);
    private static final Location ENTRY_POINT_3 = new Location(10400, 17092, -4584, 44839);

    private static final TIntObjectHashMap<int[]> _npcPhrases = new TIntObjectHashMap<>();

    private static final int INSTANCE_ID = InstanceZoneId.MEMORY_OF_DISASTER.getId();

    private static int[] _dwarfsNpcIds = {
            19193, 19198, 19202, 19209, 19215, 19213, 19214, 19212, 19211, 19210, 19206, 19208, 19207, 19200, 19203, 19204,
            19205, 19201, 19199
    };

    private static final int[] Эльфы = {
            33536, 33538, 33540, 33542, 33544, 33546
    };

    private static final int Траджан = 19172;
    private static final int Крото = 19191;
    private static final int Бронк = 19192;
    private static final int Щупальца = 19171;
    private static final int Труп_Осадное_Орудие_1 = 19182;
    private static final int Труп_Осадное_Орудие_2 = 19183;
    private static final int Осадное_Орудие = 19190;
    private static final int Осадный_Голем = 19189;
    private static final int Солдат = 19196;
    private static final int Земляной_Червь = 19217;

    private static final SkillHolder SKILL_SWOOP_CANNON = new SkillHolder(16023, 1);
//    private static final SkillHolder ENRAGED_SKILL_ID = new SkillHolder(14505, 1);
//    private static final SkillHolder BODY_STRIKE_SKILL_ID_1 = new SkillHolder(14337, 1);
//    private static final SkillHolder BODY_STRIKE_SKILL_ID_2 =  new SkillHolder(14338, 1);

    private static AW_MemoryOfDisaster _instance;

    public AW_MemoryOfDisaster()
    {
        super();

        addAskId(Сельфин, 1);
        addEventId(HookType.ON_ENTER_WORLD);
        addEventId(HookType.ON_LEVEL_INCREASE);
        int[] mobs = {Крото, Бронк, Щупальца, Траджан, Труп_Осадное_Орудие_1, Труп_Осадное_Орудие_2, Осадное_Орудие, Осадный_Голем, Солдат, Земляной_Червь};
        registerMobs(mobs);
        registerMobs(_dwarfsNpcIds);

        _npcPhrases.put(19188, new int[]{1620046});
        _npcPhrases.put(19191, new int[]{1620059, 1620060, 1620062, 1620071, 1620069, 1620065});
        _npcPhrases.put(19192, new int[]{1620051, 1620052, 1620053, 1620054, 1620055, 1620056, 1620057});
        _npcPhrases.put(19193, new int[]{1620073, 1620074, 1620075, 1620076, 1620077});
        _npcPhrases.put(19194, new int[]{1620050});
        _npcPhrases.put(19195, new int[]{1620047, 1620049});
        _npcPhrases.put(19198, new int[]{1620058, 1620059, 1620060, 1620062, 1620064, 1620069});
        _npcPhrases.put(19199, new int[]{1620060, 1620061, 1620067, 1620069, 1620071});
        _npcPhrases.put(19200, new int[]{1620061, 1620062, 1620067, 1620068, 1620071, 1620072});
        _npcPhrases.put(19201, new int[]{1620059, 1620061, 1620062, 1620066, 1620068, 1620069, 1620070});
        _npcPhrases.put(19202, new int[]{1620059, 1620060, 1620061, 1620065, 1620068, 1620070});
        _npcPhrases.put(19203, new int[]{1620060, 1620062, 1620066, 1620069, 1620070});
        _npcPhrases.put(19204, new int[]{1620059, 1620061, 1620065, 1620068, 1620070, 1620072});
        _npcPhrases.put(19205, new int[]{1620059, 1620061, 1620067, 1620068, 1620070});
        _npcPhrases.put(19206, new int[]{1620059, 1620060, 1620061, 1620063, 1620068});
        _npcPhrases.put(19207, new int[]{1620059, 1620060, 1620065, 1620069, 1620070});
        _npcPhrases.put(19208, new int[]{1620060, 1620061, 1620062, 1620064, 1620071, 1620072});
        _npcPhrases.put(19209, new int[]{1620060, 1620061, 1620063, 1620068, 1620069, 1620070});
        _npcPhrases.put(19210, new int[]{1620059, 1620060, 1620061, 1620063, 1620068, 1620069, 1620070});
        _npcPhrases.put(19211, new int[]{1620060, 1620061, 1620066, 1620068, 1620072});
        _npcPhrases.put(19212, new int[]{1620060, 1620061, 1620067, 1620070, 1620071});
        _npcPhrases.put(19213, new int[]{1620059, 1620061, 1620062, 1620064, 1620068, 1620069});
        _npcPhrases.put(19214, new int[]{1620059, 1620060, 1620067, 1620068, 1620072});
        _npcPhrases.put(19215, new int[]{1620059, 1620061, 1620067, 1620069, 1620070, 1620072});

        _npcPhrases.put(33536, new int[]{50861});
        _npcPhrases.put(33538, new int[]{50854});
        _npcPhrases.put(33540, new int[]{50861});
        _npcPhrases.put(33542, new int[]{50857});
        _npcPhrases.put(33544, new int[]{50861});
        _npcPhrases.put(33546, new int[]{50856, 50855});
    }

    public static void main(String[] args)
    {
        _instance = new AW_MemoryOfDisaster();
    }

    public static AW_MemoryOfDisaster getInstance()
    {
        return _instance;
    }

    public boolean enterInstance(L2PcInstance player)
    {
        return enterInstance(player, "AW_MemoryOfDisaster.xml") != 0;
    }

    protected int enterInstance(L2PcInstance player, String template)
    {
        int instanceId;

        MemoryOfDisaster world;
        InstanceManager.InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);

        if (tmpWorld != null)
        {
            if (!(tmpWorld instanceof MemoryOfDisaster))
            {
                player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
                return 0;
            }

            ((MemoryOfDisaster) tmpWorld).playerInside = player;

            player.teleToInstance(ENTRY_POINT, tmpWorld.instanceId);

            return tmpWorld.instanceId;
        }
        else
        {
            world = (MemoryOfDisaster) (tmpWorld = new MemoryOfDisaster());

            int instanceTemplateId = INSTANCE_ID;
            if (!checkConditions(player))
            {
                return 0;
            }


            instanceId = InstanceManager.getInstance().createDynamicInstance(template);

            world.instanceId = instanceId;
            world.templateId = instanceTemplateId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(tmpWorld);

            player.teleToInstance(ENTRY_POINT, instanceId);
            player.showQuestMovie(ExStartScenePlayer.SCENE_SORROWFUL_REMEMBRANCE_ENTERING_DWARF_VILLAGE);
            player.sendPacket(new EventTrigger(23120700, true));

            world.allowed.add(player.getObjectId());
            world.playerInside = player;
            world.status++;//1
            startWorld(world);
            ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(world), 29000);

            return instanceId;
        }
    }

    private boolean checkConditions(L2PcInstance player)
    {
        int minLevel = 85;

        if(player.getLevel() < minLevel)
        {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
            return false;
        }
        return true;
    }

    /**
     * Основной котроллер состояния мира. В зависимости от статуса мира происходят
     * те или иные события
     * @param world инстанс-мир игрока
     */
    private void startWorld(final MemoryOfDisaster world)
    {
        Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

        if(world != null)
        {
            switch(world.status)
            {
                case 1: {
                    if (instance != null) {
                        world.status = 1;

                        for (L2Spawn spawn : instance.getGroupSpawn("global")) {
                            L2Npc npc = spawn.spawnOne(false);
                            if (ArrayUtils.contains(_dwarfsNpcIds, npc.getNpcId())) {
                                world._movingDwarfNpcs.add(npc);
                            } else if (npc.getNpcId() == Крото) {
                                world._krotoInstance = npc;
                            } else if (npc.getNpcId() == Бронк) {
                                world._bronkInstance = npc;
                            } else if (npc.getNpcId() == Щупальца) {
                                world._tentacleInstance = npc;
                            } else if (npc.getNpcId() == Осадный_Голем) {
                                world._golem = npc;
//                            } else if (npc.getNpcId() == Земляной_Червь) {
//                                world._trasken = npc;
                            } else if (npc.getNpcId() == Траджан) {
                                world._tradgan = npc;
                            } else if (ArrayUtils.contains(Эльфы, npc.getNpcId())) {
                                world._elves.add(npc);
                            }
                        }

                        instance.addTask("talkerTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
                            for (L2Npc npc : world._movingDwarfNpcs) {
                                npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.getNpcStringId(_npcPhrases.get(npc.getNpcId())[0])));
                            }
                        }, 2000, 8000));
                        world.status++;
                        startWorld(world);
                        ThreadPoolManager.getInstance().scheduleGeneral(new KillBronk(world.playerInside, world), 44000);
                    }
                    break;
                }
                case 2: {
                    world.status = 2;
                    instance.cancelTask("talkerTask");

                    instance.addTask("talkerTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
                    {
                        for (L2Npc npc : world._movingDwarfNpcs) {
                            npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.getNpcStringId(_npcPhrases.get(npc.getNpcId())[1])));
                        }
                    }, 2000, 8000));
                    break;
                }
                case 3: {
                    world.status = 3;
                    instance.addTask("talkerTask", ThreadPoolManager.getInstance().scheduleGeneral(() ->
                    {
                        for (L2Npc npc : world._movingDwarfNpcs) {
                            npc.getAI().startFollow(world._krotoInstance);
                            npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.getNpcStringId(_npcPhrases.get(npc.getNpcId())[2])));
                        }
                    }, 5000));

                    ThreadPoolManager.getInstance().scheduleGeneral(new KrotoMoveTask(world), 6000);
                    ThreadPoolManager.getInstance().scheduleGeneral(new SupportTask(world, world._krotoInstance), 5999);
                    ThreadPoolManager.getInstance().scheduleGeneral(() -> {
                        world._tradgan.doDie(world._golem);
                        ThreadPoolManager.getInstance().scheduleGeneral(() -> followTask(world), 1000);
                    }, 10000);
                    break;
                }
                case 4:
                {
                    world.status = 4;
                    world.playerInside.sendPacket(new ExStartScenePlayer(ExStartScenePlayer.SCENE_SORROWFUL_REMEMBRANCE_TRAJAN));
                    instance.addTask("teleTask", ThreadPoolManager.getInstance().scheduleGeneral(() -> TeleToDE(world), 22500));
                    break;
                }
                case 5:
                {
                    world.status = 5;
                    Quest qs = QuestManager.getInstance().getQuest(AW_MemoryOfDisaster.class);
                    instance.addTask("showTask", ThreadPoolManager.getInstance().scheduleGeneral(() -> {
                        qs.startQuestTimer("dieElves", 50, null, world.playerInside);
                        world.status++;
                        startWorld(world);
                    },  83000));
                    break;
                }
                case 6:
                {
                    world.status = 6;
                    world.playerInside.sendPacket(new ExStartScenePlayer(ExStartScenePlayer.SCENE_SORROWFUL_REMEMBRANCE_SHILLEN));
                    instance.addTask("showTask", ThreadPoolManager.getInstance().scheduleGeneral(() -> {
                        world.status++;
                        startWorld(world);
                    }, 30000));
                    break;
                }
                case 7:
                {
                    world.status = 7;
                    world.playerInside.sendPacket(new ExStartScenePlayer(ExStartScenePlayer.SCENE_SORROWFUL_REMEMBRANCE_EXIT));
                    instance.addTask("showTask", ThreadPoolManager.getInstance().scheduleGeneral(() -> {
                        world.status++;
                        startWorld(world);
                    },  38500));
                    break;
                }
                case 8:
                {
                    world.status = 8;
                    Location returnLoc = new Location(119667, -181970, -1864, 52800);
                    world.playerInside.teleToLocation(returnLoc);
                    //TODO: реализовать хтмлку
                    //ThreadPoolManager.getInstance().scheduleGeneral(new ShowTutorial(world.playerInside), 1500);
                    break;
                }
            }
        }
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        MemoryOfDisaster world = InstanceManager.getInstance().getInstanceWorld(npc, MemoryOfDisaster.class);

        switch (npc.getNpcId())
        {
            case Труп_Осадное_Орудие_1: {
                npc.setIsNoRndWalk(true);
                npc.setShowName(false);
                break;
            }
            case Труп_Осадное_Орудие_2: {
                npc.setIsNoRndWalk(true);
                npc.setShowName(false);
                break;
            }
            case Щупальца: {
                npc.setIsNoRndWalk(true);
                npc.getKnownList().getKnownNpcInRadius(400).forEach(cha ->
                {
                    if(cha.getNpcId() == Солдат)
                    {
                        ((L2MonsterInstance) npc).attackCharacter(cha);
                        ((L2GuardInstance) cha).attackCharacter(npc);
                    }
                });
                break;
            }
            case Осадный_Голем:
            {
                npc.setIsMortal(false);
                npc.getKnownList().getKnownNpcInRadius(1000).forEach(((L2GuardInstance) npc)::attackCharacter);
                break;
            }
            case Осадное_Орудие:
            {
                npc.setIsNoRndWalk(true);
                ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
                {
                    npc.setTarget(npc);
                    npc.setIsCastingNow(true);
                    npc.doCast(SKILL_SWOOP_CANNON.getSkill());
                }, 1500, 10000);
                break;
            }
            case Земляной_Червь:
            {
                ThreadPoolManager.getInstance().scheduleGeneral(() -> TraskenUse(world), 3000);
                break;
            }
            case Бронк:
            {
                npc.setIsMortal(true);
                break;
            }
        }
        return super.onSpawn(npc);
    }

    @Override
    public String onNpcDie(L2Npc npc, L2Character killer)
    {
        MemoryOfDisaster world = InstanceManager.getInstance().getInstanceWorld(npc, MemoryOfDisaster.class);
        switch (npc.getNpcId())
        {
            case Бронк:
            {
                world.status++;
                startWorld(world);//3
                break;
            }
            case 33536:
            case 33538:
            case 33540:
            case 33542:
            case 33544:
            case 33546:
                npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.getNpcStringId(_npcPhrases.get(npc.getNpcId())[1])));
                break;
        }
        return super.onNpcDie(npc, killer);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        MemoryOfDisaster world = InstanceManager.getInstance().getInstanceWorld(npc, MemoryOfDisaster.class);

        if(player == null)
        {
            return null;
        }
        if(event.equalsIgnoreCase("enterInstance"))
        {
            enterInstance(player);
        }
        else if(event.equalsIgnoreCase("dieElves"))
        {
            world._elves.get(Rnd.get(world._elves.size())).doDie(world.playerInside);
        }
        return null;
    }

    private void DieElves(MemoryOfDisaster _world)
    {
        ThreadPoolManager.getInstance().scheduleGeneral(() ->
        {
            _world.status++;
            startWorld(_world);
        }, 10000);
    }

    private void TeleToDE(MemoryOfDisaster _world)
    {
        _world.playerInside.teleToInstance(ENTRY_POINT_3, _world.instanceId);
        _world.playerInside.sendPacket(new EventTrigger(23120700, false));

        ThreadPoolManager.getInstance().scheduleGeneral(() ->
                _world.playerInside.showQuestMovie(ExStartScenePlayer.SCENE_SORROWFUL_REMEMBRANCE_ENTERING_DARKELF_VILLAGE), 1000);

        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> DieElves(_world), 1000, 25000);
    }

    private static class KrotoMoveTask extends RunnableImpl
    {
        private final MemoryOfDisaster _world;

        public KrotoMoveTask(MemoryOfDisaster world)
        {
            _world = world;
        }

        @Override
        public void runImpl() throws Exception //FIXME: добегает до опр. места, останавливается и пока не запуститься след. таск хуй обежит дальше
        {
            Instance instance = InstanceManager.getInstance().getInstance(_world.instanceId);

            if(_world == null || instance == null)
            {
                return;
            }

            ThreadPoolManager.getInstance().scheduleGeneral(() -> {
                if (_world._krotoInstance != null)
                {
                    if (_world._krotoInstance.getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO)
                    {

                        Location loc1 = new Location(116005, -181911, -1432);
                        Location loc2 = new Location(116005, -181911, -1432);
                        Location loc3 = new Location(116005, -181911, -1432);
                        Location loc4 = new Location(116005, -181911, -1432);

                        _world._krotoInstance.setRunning();
                        ((L2GuardInstance) _world._krotoInstance).setReturnHome(false);
                        _world._krotoInstance.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc1);
                        ThreadPoolManager.getInstance().scheduleGeneral(() -> _world._krotoInstance.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc2), 15000);
                        ThreadPoolManager.getInstance().scheduleGeneral(() -> _world._krotoInstance.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc3), 30000);
                        ThreadPoolManager.getInstance().scheduleGeneral(() -> _world._krotoInstance.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc4), 45000);
                        ThreadPoolManager.getInstance().scheduleGeneral(() -> _world._krotoInstance.getAI().stopFollow(), 66000);
                    }
                }
            }, 50);
        }
    }

//    private static class ShowTutorial extends RunnableImpl
//    {
//        private final L2PcInstance _player;
//        public ShowTutorial(L2PcInstance player)
//        {
//            _player = player;
//        }
//
//        @Override
//        public void runImpl() throws Exception //TODO: уточнить текст хтмлки
//        {
//            String text = HtmCache.getInstance().getHtm(_player.getLang(), "default_ex/awakening_germunkus_voice.htm");
//            _player.sendPacket(new TutorialShowHtml(TutorialShowHtml.SERVER_SIDE, text));
//        }
//    }

    public void followTask(MemoryOfDisaster _world)
    {
        _world._golem.setRunning();
        ((L2GuardInstance) _world._golem).setReturnHome(false);
        _world._golem.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(116560, -179440, -1144));
        if (_world != null)
        {
            ThreadPoolManager.getInstance().scheduleGeneral(() ->
                    addSpawn(Земляной_Червь, new Location(116511, -178729, -1176, 50366), _world.instanceId), 30000);
        }
    }

    private class TeleportTask extends RunnableImpl
    {
        private final MemoryOfDisaster _world;
        private final Instance _instance;

        public TeleportTask(MemoryOfDisaster world)
        {
            _world = world;
            _instance = InstanceManager.getInstance().getInstance(world.instanceId);
        }

        @Override
        public void runImpl()
        {
            final L2PcInstance player = _world.playerInside;

            player.teleToInstance(ENTRY_POINT_2, _instance.getId());
            player.sendPacket(new EventTrigger(23120700, true));
        }
    }

    /**
     * Таск на убийство Бронка
     */
    private static class KillBronk extends RunnableImpl
    {
        private final L2PcInstance _caller;
        private final MemoryOfDisaster _world;

        public KillBronk(L2PcInstance caller, MemoryOfDisaster world)
        {
            _caller = caller;
            _world = world;
        }

        @Override
        public void runImpl() throws Exception
        {
            if(_caller != null && !_caller.isDead())
            {
                if(_world != null)
                {
                    if(_world._bronkInstance != null && !_world._bronkInstance.isDead())
                    {
                        _world._bronkInstance.doDie(_world._tentacleInstance);
                    }
                }
            }
        }
    }


    /**
     * Задача предназначена для саппорт-npc.
     */
    private static class SupportTask extends RunnableImpl
    {
        private final MemoryOfDisaster _world;
        private final L2Npc _npc;

        public SupportTask(MemoryOfDisaster world, L2Npc npc)
        {
            _world = world;
            _npc = npc;
        }

        @Override
        public void runImpl() throws Exception
        {
            _world._movingDwarfNpcs.forEach(npc -> npc.getAI().startFollow(_npc));
        }
    }

    private void TraskenUse(MemoryOfDisaster _world)
    {

//            _world._trasken.doCast(ENRAGED_SKILL_ID.getSkill());
//
//            ThreadPoolManager.getInstance().scheduleGeneral(() ->
//            {
//                _world._trasken.doCast(BODY_STRIKE_SKILL_ID_1.getSkill());
//
//                ThreadPoolManager.getInstance().scheduleGeneral(() ->
//                        _world._trasken.doCast(BODY_STRIKE_SKILL_ID_2.getSkill()), 10000);
//            }, 10000);

        _world.status++;//4

        startWorld(_world);
    }

    @Override
    public void onEnterWorld(L2PcInstance player)
    {
        if(player.getLevel() >= 85 && !player.isAwakened() && player.getClassId().level() > 2 && !player.isSubClassActive())
        {
            QuestState st = player.getQuestState(_10338_OvercomeTheRock.class);
            if(st != null && st.isStarted())
            {
                return;
            }
            player.sendPacket(new ExCallToChangeClass(player.getClassId(), true));
            player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1811216), ExShowScreenMessage.TOP_CENTER, 6000));
        }
    }

    @Override
    public void onLevelIncreased(L2PcInstance player)
    {
        if(player.getLevel() >= 85 && !player.isAwakened() && player.getClassId().level() > 2 && !player.isSubClassActive())
        {
            QuestState st = player.getQuestState(_10338_OvercomeTheRock.class);
            if(st != null && st.isStarted())
            {
                return;
            }
            player.sendPacket(new ExCallToChangeClass(player.getClassId(), true));
            player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1811216), ExShowScreenMessage.TOP_CENTER, 6000));
        }
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        if(ask == 1)
        {
            switch(reply)
            {
                case 1:
                    return "selphin002.htm";
                case 11:
                    return "selphin002a.htm";
                case 2:
                    return "selphin003.htm";
                case 3:
                    return "selphin004.htm";
                case 100:
                    enterInstance(player);
                    return null;
            }
        }
        return null;
    }

    public static class MemoryOfDisaster extends InstanceManager.InstanceWorld
    {
        private L2PcInstance playerInside               = null;
        private L2Npc _krotoInstance                    = null;
        private L2Npc _bronkInstance                    = null;
        private L2Npc _tentacleInstance                 = null;
        private L2Npc _golem                            = null;
        //        private L2Npc _trasken                          = null;
        private L2Npc _tradgan                          = null;
        private List<L2Npc> _elves                      = new ArrayList<>();
        private FastList<L2Npc> _movingDwarfNpcs        = new FastList<>();
    }
}

