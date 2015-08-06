package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2FriendlyMobInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExChangeNPCState;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.scripts.quests._10331_StartOfFate;
import javolution.util.FastList;

public class LabyrinthOfBelis extends Quest
{
    private static final int LABIRYNTH_OF_BELIS = 400001;

    // NPC's
    private static final int OFFICER = 19155;
    private static final int SYSTEM = 33215;
    private static final int BELIS_MARK = 17615;

    private static final int HANDYMAN = 22997;
    private static final int OPERATIVE = 22998;
    private static final int NEMERTESS = 22984;
    private static final int HANDYMAN_ATTACKER = 23119;
    private static final int OPERATIVE_ATTACKER = 23120;

    private static final int SARIL_NECKLACE = 17580;

    private static final int[][] SPAWN1 = {{22998, -117989, 210863, -8592, 28969}, {22998, -118639, 210883, -8592, 27848}, {22998, -118689, 211023, -8592, 23793}, {22998, -118636, 211262, -8592, 30715}, {22998, -118136, 211262, -8592, 45176}, {22998, -118736, 211319, -8592, 36199},};

    private final static int[][] OFFICER_POINTS = {{-117032, 212568, -8617}, {-117896, 214264, -8617}, {-119208, 213768, -8617}, {-118328, 212968, -8704},};

    private static final Location ENTRY_POINT = new Location(-119929, 211162, -8584);
    private static final Location EXIT_POINT = new Location(-111774, 231933, -3160);

    private class LOB extends InstanceManager.InstanceWorld
    {
        private FastList<L2Npc> first_wave = null;
        private FastList<L2Npc> machine = null;
        private L2PcInstance _player;
        private L2Npc _officer;
        private short _marksInserted = 0;
        private short _attackersKilled = 0;
        private int wave_count = 0;

        public LOB()
        {
            first_wave = new FastList<>();
            machine = new FastList<>();
        }

        public void setPlayer(L2PcInstance player)
        {
            _player = player;
        }

        public L2PcInstance getPlayer()
        {
            return _player;
        }

        public void setOfficer(L2Npc guard)
        {
            _officer = guard;
            if(_officer != null && _officer instanceof L2FriendlyMobInstance)
            {
                _officer.setIsNoRndWalk(true);
                ((L2FriendlyMobInstance) _officer).setisReturningToSpawnPoint(false);
            }
        }

        public L2Npc getOfficer()
        {
            return _officer;
        }

        public void insertBelisMark()
        {
            ++_marksInserted;
        }

        public short getBelisMarksInserted()
        {
            return _marksInserted;
        }

        public short getAttackersKilled()
        {
            return _attackersKilled;
        }

        public void attackerKilled()
        {
            ++_attackersKilled;
        }
    }

    protected void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if(world != null)
        {
            if(!(world instanceof LOB))
            {
                player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
                return;
            }
            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if(inst != null)
            {
                player.teleToInstance(ENTRY_POINT, world.instanceId);
                init((LOB) world, player);
            }
        }
        else
        {
            int instanceId = InstanceManager.getInstance().createDynamicInstance("LabyrinthOfBelis.xml");

            world = new LOB();
            world.instanceId = instanceId;
            world.templateId = InstanceZoneId.LABYRINTH_OF_BELIS.getId();
            InstanceManager.getInstance().addWorld(world);
            world.allowed.add(player.getObjectId());
            world.status = 0;
            player.teleToInstance(ENTRY_POINT, instanceId);
            init((LOB) world, player);
        }
    }

    private void init(LOB world, L2PcInstance player)
    {
        if (world != null)
        {
            world.setPlayer(player);

            // Ищем гварда в спауне
            for (L2Npc spawn : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
            {
                if (spawn.getNpcId() == OFFICER)
                {
                    world.setOfficer(spawn);
                    break;
                }
            }

        }
    }

    @Override
    public final String onAdvEvent(String event, final L2Npc npc, final L2PcInstance player)
    {
        if(event.equalsIgnoreCase("enterInstance"))
        {
            QuestState st = player.getQuestState(_10331_StartOfFate.class);
            if(st != null && st.isStarted() && st.getCond() == 3)
            {
                enterInstance(player);
                return null;
            }
        }

        InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
        if(!(tmpWorld instanceof LOB))
        {
            return null;
        }

        final LOB world = (LOB) tmpWorld;

        if(event.equalsIgnoreCase("follow"))
        {
            ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
            {
                @Override
                public void run()
                {
                    if((world.status == 1 || world.status == 3) && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
                    {
                        npc.getSpawn().setLocx(player.getX());
                        npc.getSpawn().setLocy(player.getY());
                        npc.getSpawn().setLocz(player.getZ());
                        npc.setTarget(player);
                        npc.getAI().stopFollow();
                        npc.setIsRunning(true);
                        npc.getAI().startFollow(player);
                    }
                    else if(world.status < 8)
                    {
                        ThreadPoolManager.getInstance().scheduleGeneral(this, 2000);
                    }
                }
            }, 2000);
        }
        else if(event.equalsIgnoreCase("msg"))
        {
            player.sendPacket(new ExShowScreenMessage(NpcStringId.MARK_OF_BELIS_CAN_BE_ACQUIRED_FROM_ENEMIES_NUSE_THEM_IN_THE_BELIS_VERIFICATION_SYSTEM, ExShowScreenMessage.TOP_CENTER, 8000));
        }
        else if(event.equalsIgnoreCase("helper_attack"))
        {
            player.sendPacket(new ExShowScreenMessage(NpcStringId.BEHIND_YOU_THE_ENEMY_IS_AMBUSHING_YOU, ExShowScreenMessage.TOP_CENTER, 5000));
            startQuestTimer("wave_23119", 8000, npc, player);
        }
        else if(event.equalsIgnoreCase("drop_belis_mark"))
        {
            if(npc instanceof L2MonsterInstance)
            {
                ((L2MonsterInstance) npc).dropItem(player, BELIS_MARK, 1);
            }
        }
        else if(event.equalsIgnoreCase("wave_23119") || event.equalsIgnoreCase("wave_23120"))
        {
            ((LOB) tmpWorld).wave_count++;
            if(((LOB) tmpWorld).wave_count < 6)
            {
                final L2Npc attacker;
                if(event.equalsIgnoreCase("wave_23119"))
                {
                    attacker = addSpawn(HANDYMAN_ATTACKER, -116701, 213173, -8602, 0, false, 0, false, player.getInstanceId());
                }
                else
                {
                    attacker = addSpawn(OPERATIVE_ATTACKER, -116701, 213173, -8602, 0, false, 0, false, player.getInstanceId());
                }

                if(attacker != null && attacker instanceof L2MonsterInstance)
                {
                    ThreadPoolManager.getInstance().scheduleGeneral(() -> ((L2MonsterInstance) attacker).attackCharacter(world.getOfficer()), 4000);

                    attacker.setIsRunning(true);
                    attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-117880, 214336, -8600, 0));
                    attacker.broadcastPacket(new NS(attacker.getObjectId(), ChatType.NPC_ALL, attacker.getNpcId(), NpcStringId.FOCUS_ON_ATTACKING_THE_GUY_IN_THE_ROOM));
                }

                if(event.equalsIgnoreCase("wave_23119"))
                {
                    startQuestTimer("wave_23120", 8000, npc, player);
                }
                else
                {
                    startQuestTimer("wave_23119", 8000, npc, player);
                }

                if(Rnd.getChance(50))
                {
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.BEHIND_YOU_THE_ENEMY_IS_AMBUSHING_YOU, ExShowScreenMessage.TOP_CENTER, 5000));
                }
                else
                {
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.IF_TERAIN_DIES_THE_MISSION_WILL_FAIL, ExShowScreenMessage.TOP_CENTER, 5000));
                }
            }
        }
        return null;
    }

    @Override
    public String onNpcDie(L2Npc npc, L2Character killer)
    {
        InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if(tmpWorld instanceof LOB)
        {
            LOB world = (LOB) tmpWorld;
            L2PcInstance player = world.getPlayer();

            if(world.status == 1 && npc.getNpcId() == OPERATIVE)
            {
                if(world.first_wave.contains(npc))
                {
                    world.first_wave.remove(npc);
                }

                if(world.first_wave.isEmpty() || world.first_wave.size() == 0)
                {
                    world.status = 2;
                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240003).openMe();

                    L2Npc guard = world.getOfficer();
                    if(guard != null)
                    {
                        guard.setRunning();
                        guard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(OFFICER_POINTS[0][0], OFFICER_POINTS[0][1], OFFICER_POINTS[0][2]));
                    }
                }
            }
            else if(world.status == 3)
            {
                if(Rnd.getChance(40))
                {
                    startQuestTimer("drop_belis_mark", 2000, npc, player);
                }
            }
            // Офицер бьет электрогенератор, бегут мобы (7-8 штук), нужно убить всех
            else if(world.status == 5 && (npc.getNpcId() == HANDYMAN_ATTACKER || npc.getNpcId() == OPERATIVE_ATTACKER))
            {
                world.attackerKilled();

                if(world.getAttackersKilled() >= 6)
                {
                    world.status = 6;
                    cancelQuestTimer("wave", null, null);
                    for(L2Npc machine : world.machine)
                    {
                        machine.getSpawn().stopRespawn();
                        machine.getLocationController().delete();
                    }
                    world.machine.clear();
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.ELECTRONIC_DEVICE_HAS_BEEN_DESTROYED, ExShowScreenMessage.TOP_CENTER, 5000));
                    world.getOfficer().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(OFFICER_POINTS[2][0], OFFICER_POINTS[2][1], OFFICER_POINTS[2][2], 0));
                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240007).openMe();
                }
            }
            else if(world.status == 7 && npc.getNpcId() == NEMERTESS)
            {
                world.status = 8;
                player.showQuestMovie(ExStartScenePlayer.SCENE_TALKING_ISLAND_BOSS_ENDING);

                QuestState st = player.getQuestState(_10331_StartOfFate.class);
                if(st != null)
                {
                    st.giveItems(SARIL_NECKLACE, 1);
                    st.setCond(4);
                }

                final L2Npc officer = world.getOfficer();
                if(officer != null)
                {
                    ThreadPoolManager.getInstance().scheduleGeneral(() -> officer.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(OFFICER_POINTS[3][0], OFFICER_POINTS[3][1], OFFICER_POINTS[3][2])), 15000);
                }
            }
            else if(npc.getNpcId() == OFFICER)
            {
                InstanceManager.getInstance().destroyInstance(world.instanceId);
                player.getInstanceController().setInstanceId(0);
            }
        }
        return null;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        onNpcDie(npc, player);
        return null;
    }

    /**
     * Заставляем гварда ассистить атаки игрока.
     *
     * @param npc Атакованный NPC.
     * @param attacker Атакующий персонаж.
     * @param damage Урон.
     * @param isPet Пет?
     * @param skill Скилл.
     */
    @Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
    {
        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(attacker.getInstanceId());
        if(tmpworld instanceof LOB)
        {
            LOB lob = (LOB) tmpworld;
            L2Npc officer = lob.getOfficer();

            if(officer == null)
            {
                return "";
            }

            if(officer.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
            {
                ((L2FriendlyMobInstance) officer).attackCharacter(npc);
            }
            else
            {
                ((L2FriendlyMobInstance) officer).addDamageHate(npc, 0, 1);
            }
        }

        return onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onExitZone(L2Character character, L2ZoneType zone)
    {
        if(!(character instanceof L2PcInstance))
        {
            return null;
        }

        if(InstanceManager.getInstance().getWorld(character.getInstanceId()) instanceof LOB)
        {
            InstanceManager.getInstance().destroyInstance(character.getInstanceId());
            character.getInstanceController().setInstanceId(0);
        }

        return null;
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
        if(!(tmpWorld instanceof LOB))
        {
            return null;
        }

        LOB world = (LOB) tmpWorld;
        if(npc.getNpcId() == OFFICER)
        {
            player.setLastQuestNpcObject(npc.getObjectId());
            switch(world.status)
            {
                case 0:
                    return "vel_illusion_teri001.htm";
                case 1:
                    return "vel_illusion_teri003.htm";
                case 2:
                    return "vel_illusion_teri004.htm";
                case 3:
                    return "vel_illusion_teri003.htm";
                case 4:
                    return "vel_illusion_teri005.htm";
                case 5:
                    return "vel_illusion_teri003.htm";
                case 6:
                    return "vel_illusion_teri006.htm";
                case 7:
                    return "vel_illusion_teri003.htm";
                case 8:
                    return "vel_illusion_teri007.htm";
            }
        }
        else if(npc.getNpcId() == SYSTEM)
        {
            player.setLastQuestNpcObject(npc.getObjectId());
            return "si_illusion_people36001.htm";
        }
        return null;
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
        if(!(tmpWorld instanceof LOB))
        {
            return null;
        }

        final LOB world = (LOB) tmpWorld;

        if(npc.getNpcId() == OFFICER)
        {
            if(ask == -4200 || ask == -4202 || ask == -4204 || ask == -4206)
            {
                if(reply == 1)
                {
                    switch(world.status)
                    {
                        case 0:
                            world.status = 1;
                            // Open Door
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240002).openMe();
                            startQuestTimer("follow", 1000, npc, player, true);
                            for(int[] sp : SPAWN1)
                            {
                                L2Npc mobs = addSpawn(sp[0], sp[1], sp[2], sp[3], sp[4], false, 0, false, player.getInstanceId());
                                world.first_wave.add(mobs);
                            }
                            return "vel_illusion_teri002.htm";
                        case 2:
                            world.status = 3;
                            // Open Door
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240004).openMe();
                            player.sendPacket(new ExShowScreenMessage(NpcStringId.MARK_OF_BELIS_CAN_BE_ACQUIRED_FROM_ENEMIES_NUSE_THEM_IN_THE_BELIS_VERIFICATION_SYSTEM, ExShowScreenMessage.TOP_CENTER, 8000));
                            startQuestTimer("msg", 10000, npc, player, true);
                            return "vel_illusion_teri002.htm";
                        case 4:
                            world.status = 5;
                            // Open Door
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240006).openMe();
                            final L2Npc machine = addSpawn(33216, -118253, 214706, -8584, 57541, false, 0, false, player.getInstanceId());
                            ThreadPoolManager.getInstance().scheduleGeneral(() ->
                            {
                                if (machine == null)
                                    return;

                                machine.setDisplayEffect(0x01); // Lightning
                            }, 10000);
                            ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if(machine == null)
                                    {
                                        return;
                                    }

                                    // Current shock
                                    machine.doCast(SkillTable.getInstance().getInfo(14698, 1));
                                    ThreadPoolManager.getInstance().scheduleGeneral(this, 4000);
                                }
                            }, 1100);
                            player.sendPacket(new ExChangeNPCState(machine.getObjectId(), 0x01));
                            world.machine.add(machine);

                            npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.DONT_COME_BACK_HERE));
                            if(npc.getNpcId() == OFFICER)
                            {
                                ((L2FriendlyMobInstance) npc).attackCharacter(machine);
                            }

                            cancelQuestTimer("follow", npc, player);
                            startQuestTimer("helper_attack", 500, npc, player);
                            return "vel_illusion_teri002.htm";
                        case 6:
                            world.status = 7;
                            // Open Door
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240008).openMe();
                            player.showQuestMovie(ExStartScenePlayer.SCENE_TALKING_ISLAND_BOSS_OPENING);
                            addSpawn(22984, -118344, 212979, -8688, 0, false, 0, false, player.getInstanceId());
                            return "vel_illusion_teri002.htm";
                    }
                }
            }
            else if(ask == -4207)
            {
                if(reply == 1)
                {
                    player.teleToInstance(EXIT_POINT, 0);
                    return null;
                }
            }
        }
        else if(npc.getNpcId() == SYSTEM)
        {
            if(ask == -2353)
            {
                if(reply == 1)
                {
                    if(player.getItemsCount(BELIS_MARK) == 0)
                    {
                        return "si_illusion_people36002.htm";
                    }
                    else
                    {
                        player.destroyItemByItemId(ProcessType.NPC, BELIS_MARK, 1, npc, true);
                        world.insertBelisMark();

                        short marksInserted = world.getBelisMarksInserted();
                        if(marksInserted < 3)
                        {
                            switch(marksInserted)
                            {
                                case 1:
                                    return "si_illusion_people36003.htm";
                                case 2:
                                    return "si_illusion_people36004.htm";
                            }
                        }
                        else
                        {
                            cancelQuestTimers("msg");
                            world.status = 4;
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240005).openMe();

                            L2Npc guard = world.getOfficer();
                            if(guard != null)
                            {
                                guard.setRunning();
                                guard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(OFFICER_POINTS[1][0], OFFICER_POINTS[1][1], OFFICER_POINTS[1][2]));
                            }
                            return "si_illusion_people36005.htm";
                        }
                    }
                }
            }
        }
        return null;
    }

    public LabyrinthOfBelis()
    {
        super();
        addStartNpc(OFFICER);
        addFirstTalkId(OFFICER);
        addTalkId(OFFICER);
        addFirstTalkId(SYSTEM);
        addTalkId(SYSTEM);
        addKillId(OPERATIVE, OPERATIVE_ATTACKER, HANDYMAN_ATTACKER, NEMERTESS, OFFICER, HANDYMAN);

        addExitZoneId(LABIRYNTH_OF_BELIS);
        addAttackId(HANDYMAN, OPERATIVE, NEMERTESS);

        addAskId(OFFICER, -4200);
        addAskId(OFFICER, -4202);
        addAskId(OFFICER, -4204);
        addAskId(OFFICER, -4206);
        addAskId(OFFICER, -4207);
        addAskId(SYSTEM, -2353);
    }

    public static void main(String[] args)
    {
        new LabyrinthOfBelis();
    }
}