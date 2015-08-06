package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.RunnableImpl;
import dwo.scripts.quests._10301_TheShadowOfFear;

import java.util.ArrayList;
import java.util.List;

/**
 * User: GenCloud
 * Date: 16.03.2015
 * Team: La2Era Team
 */
public class TaintedDimension extends Quest
{
    private static final L2Skill skillPriest = SkillTable.getInstance().getInfo(14497, 1);
    private static final L2Skill skillAnt = SkillTable.getInstance().getInfo(14496, 1);

    private static final int[] corpsePhrases = new int[]{44440000, 44440001, 44440002, 44440003};

    private static final int[][] ants_spawns = {{33137, 183976, 85928, -7752, 0}, {33137, 183914, 85930, -7752, 0}, {33137, 183848, 85976, -7752, 0}, {33137, 183848, 85896, -7752, 0}, {33137, 183912, 85864, -7752, 0}, {33137, 183928, 85992, -7752, 0}, {33137, 184295, 85553, -7752, 25024},};

    private static final int[][] mysteryMans1_spawns = {{33363, 183563, 85890, -7752, 0}, {33363, 183505, 86102, -7752, 61780}, {33363, 183696, 85632, -7752, 10920}, {33363, 183492, 85726, -7752, 5816}, {33363, 183706, 86310, -7752, 54624}, {33363, 183694, 86225, -7752, 55244}, {33363, 183630, 86145, -7752, 58280}, {33363, 183500, 85824, -7752, 1136}, {33363, 183968, 85632, -7752, 18760}, {33363, 184057, 85587, -7752, 21700}, {33363, 183866, 85592, -7752, 15368}, {33363, 183774, 85541, -7752, 11264}, {33363, 183509, 85969, -7752, 62428}, {33363, 183605, 85612, -7752, 19568}, {33363, 183821, 86279, -7752, 47032}, {33363, 184288, 86104, -7752, 39860}, {33363, 184144, 86224, -7752, 40836}, {33363, 183952, 86273, -7752, 46772}, {33363, 184042, 86297, -7752, 46836}, {33363, 183598, 86239, -7752, 57240}, {33363, 184216, 86308, -7752, 41404}, {33363, 184283, 85973, -7752, 37256}, {33363, 184285, 86212, -7752, 40624}, {33363, 184294, 85894, -7752, 27796}};

    private static final int[][] mysteryMans2_spawns = {{33362, 183770, 86061, -7752, 58220}, {33362, 183750, 85793, -7752, 5416}, {33362, 184025, 86080, -7752, 44928}, {33362, 183961, 85734, -7752, 19716}, {33362, 184104, 85881, -7752, 33464}};

    private static final int[][] mysteryPriest_spawns = {{33361, 183825, 86049, -7752, 55496}, {33361, 183913, 86084, -7752, 49296}, {33361, 183767, 85872, -7752, 2944}, {33361, 183993, 86055, -7752, 42404}, {33361, 183989, 85803, -7752, 22740}, {33361, 183763, 85967, -7752, 64408}, {33361, 184048, 85887, -7752, 31132}, {33361, 184042, 85974, -7752, 35684}, {33361, 183904, 85780, -7752, 15428}, {33361, 183813, 85799, -7752, 9620}};

    // НПЦ, которые будут принесены в жертву
    private static final int[][] victims_spawns = {{33368, 183952, 85905, -7752, 30144}, {33371, 183856, 85905, -7752, 25720}, {33372, 183922, 85916, -7752, 56760}, {33365, 183952, 85953, -7752, 2932}, {33370, 183888, 85921, -7752, 7764}, {33369, 183920, 85953, -7752, 16456}, {33366, 183872, 85953, -7752, 11224}, {33367, 183904, 85969, -7752, 59344}, {33364, 183914, 85885, -7752, 44604}};

    // Трупы, появляющиеся после жертвоприношения НПЦ
    private static final int[][] victims_corpses_spawns =
            {
                    {19117, 183831, 85950, -7752, 0},
                    {19117, 184069, 85940, -7752, 0},
                    {19118, 183915, 85974, -7752, 0},
                    {19117, 183861, 85972, -7752, 0},
                    {19117, 183896, 85974, -7752, 0},
                    {19118, 183792, 85967, -7752, 0},
                    {19118, 183754, 85823, -7752, 0},
            };

    private static final Location ENTRY_POINT = new Location(184299, 85555, -7752);
    private static final Location EXIT_POINT = new Location(207528, 86576, -1000);

    public TaintedDimension()
    {
        super();
        addSpawnId(19117, 19118);
        addEnterZoneId(400115);
    }

    public static void main(String[] args)
    {
        new TaintedDimension();
    }

    public class TaintedDimensionWorld extends InstanceManager.InstanceWorld
    {
        public List<L2Npc> ants;
        public List<L2Npc> victims;
        public List<L2Npc> victims_corpses;
        public List<L2Npc> mysteryPriest;
        public List<L2Npc> mysteryMans1;
        public List<L2Npc> mysteryMans2;
        public L2PcInstance player;
    }

    protected void enterInstance(L2PcInstance player)
    {
        InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

        if(world == null)
        {
            int instanceId = InstanceManager.getInstance().createDynamicInstance("TaintedDimension.xml");

            world = new TaintedDimensionWorld();
            world.instanceId = instanceId;
            world.templateId = InstanceZoneId.TAINTED_DIMENSION.getId();

            InstanceManager.getInstance().addWorld(world);

            world.allowed.add(player.getObjectId());
            ((TaintedDimensionWorld) world).player = player;
            world.status = 0;

            player.teleToInstance(ENTRY_POINT, ((TaintedDimensionWorld) world).instanceId);
            startInstance(player);
        }
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        InstanceManager.InstanceWorld world = null;
        if(npc != null)
        {
            world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else if(player != null)
        {
            world = InstanceManager.getInstance().getPlayerWorld(player);
        }

        if(world == null || !(world instanceof TaintedDimensionWorld) || npc == null)
        {
            return null;
        }

        switch (event) {
            case "sayPriest1": {
                sayNpc(npc, 8888102);
                startQuestTimer("sayPriest2", 2000, npc, player);
                break;
            }
            case "sayPriest2": {
                sayNpc(npc, 8888103);
                startQuestTimer("sayPriest3", 2000, npc, player);
                break;
            }
            case "sayPriest3": {
                sayNpc(npc, 8888104);
                ThreadPoolManager.getInstance().scheduleGeneral(new IncreaseStage(((TaintedDimensionWorld) world)), 4000);
                break;
            }
        }

        return null;
    }

    private void sayNpc(L2Npc npc, int stringId)
    {
        npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.getNpcStringId(stringId)));
    }

    private void initializeSpawn(InstanceManager.InstanceWorld world) {

        ((TaintedDimensionWorld) world).ants = new ArrayList<>(7);
        for (int[] spawn : ants_spawns)
        {
            ((TaintedDimensionWorld) world).ants.add(addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId));
        }

        ((TaintedDimensionWorld) world).victims = new ArrayList<>(9);
        for (int[] spawn : victims_spawns)
        {
            ((TaintedDimensionWorld) world).victims.add(addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId));
        }

        ((TaintedDimensionWorld) world).mysteryPriest = new ArrayList<>(10);
        for (int[] spawn : mysteryPriest_spawns)
        {
            ((TaintedDimensionWorld) world).mysteryPriest.add(addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId));
        }

        ((TaintedDimensionWorld) world).mysteryMans1 = new ArrayList<>(24);
        for (int[] spawn : mysteryMans1_spawns)
        {
            ((TaintedDimensionWorld) world).mysteryMans1.add(addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId));
        }

        ((TaintedDimensionWorld) world).mysteryMans2 = new ArrayList<>(5);
        for (int[] spawn : mysteryMans2_spawns)
        {
            ((TaintedDimensionWorld) world).mysteryMans2.add(addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId));
        }

        ThreadPoolManager.getInstance().scheduleGeneral(new IncreaseStage(((TaintedDimensionWorld) world)), 5000);
    }

    private void startInstance(L2PcInstance player)
    {
        InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        switch(world.status)
        {
            case 0: // Спауним всех НПЦ и шлем игроку сообщения
            {
                initializeSpawn(world);
                // TODO: NpcString в чат: 8888119 Его поглотила тьма...
                player.sendMessage("Его поглотила тьма...");
                player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(2010112), ExShowScreenMessage.TOP_CENTER, 4500));
                _log.info("stage 0");
                break;
            }
            case 1: // Говорит главный прист
            {
                ((TaintedDimensionWorld) world).mysteryPriest.stream().forEach(sayMob ->
                        startQuestTimer("sayPriest1", 5000, sayMob, player));
                _log.info("stage 1");
                break;
            }
            case 2: // Говорят остальные присты "За разрущение и воскрешение!"
            {
                for (L2Npc sayMob : ((TaintedDimensionWorld) world).mysteryPriest) {
                    sayNpc(sayMob, 8888105);
                }

                ThreadPoolManager.getInstance().scheduleGeneral(new IncreaseStage(((TaintedDimensionWorld) world)), 4000);

                _log.info("stage 2");

                break;
            }
            case 3: // Говорят остальные присты "Богиня Разрушения... Сам свет страшится тебя..."
            {
                for (L2Npc sayMob : ((TaintedDimensionWorld) world).mysteryPriest) {
                    sayNpc(sayMob, 2010111);
                }

                for (L2Npc sayMob : ((TaintedDimensionWorld) world).mysteryMans2) {
                    sayNpc(sayMob, 8888105);
                }

                ThreadPoolManager.getInstance().scheduleGeneral(new IncreaseStage(((TaintedDimensionWorld) world)), 5000);

                _log.info("stage 3");

                break;
            }
            case 4:
            {
                ((TaintedDimensionWorld) world).mysteryPriest.stream().forEach(mob -> mob.doCast(skillPriest));
                ((TaintedDimensionWorld) world).ants.stream().forEach(mob -> mob.doCast(skillAnt));

                ThreadPoolManager.getInstance().scheduleGeneral(new IncreaseStage(((TaintedDimensionWorld) world)), 3000);

                _log.info("stage 4");

                break;
            }
            case 5:
            {
                ((TaintedDimensionWorld) world).victims.forEach(mob -> mob.doDie(player));

                ((TaintedDimensionWorld) world).victims_corpses = new ArrayList<>(7);
                for (int[] spawn : victims_corpses_spawns) {
                    ((TaintedDimensionWorld) world).victims_corpses.add(addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId));
                }
                ThreadPoolManager.getInstance().scheduleGeneral(new IncreaseStage(((TaintedDimensionWorld) world)), 5000);

                _log.info("stage 5");

                break;
            }
            case 6:
            {
                if (((TaintedDimensionWorld) world).victims_corpses != null)
                {
                    for (L2Npc mob : ((TaintedDimensionWorld) world).victims_corpses) 
                    {
                        sayNpc(mob, corpsePhrases[Rnd.get(corpsePhrases.length)]);
                    }
                }

                ThreadPoolManager.getInstance().scheduleGeneral(new IncreaseStage(((TaintedDimensionWorld) world)), 3000);

                _log.info("stage 6");

                break;
            }
            case 7:
            {
                player.sendMessage("Куда меня тянет!"); // TODO: NpcString 8888118	u,Куда меня тянет!\0
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                player.getInstanceController().setInstanceId(0);
                player.teleToLocation(EXIT_POINT, 25);
                ThreadPoolManager.getInstance().scheduleGeneral(() ->
                        player.showQuestMovie(ExStartScenePlayer.SCENE_SI_ARKAN_ENTER), 5000);
                _log.info("stage 7");
                break;
            }
        }
    }

    @Override
    public String onEnterZone(L2Character character, L2ZoneType zone)
    {
        L2PcInstance player = character.getActingPlayer();
        QuestState qs = player.getQuestState(_10301_TheShadowOfFear.class);

        if(player != null)
        {
            if(qs != null && qs.getCond() == 3/* && !player.getVariablesController().get("TAINTED_DIMESION_OK", Boolean.class, true)*/)
            {
                enterInstance(player);
                player.getVariablesController().set("TAINTED_DIMESION_OK", true);
            }
            else
            {
                player.teleToLocation(207528, 86576, -1000);
            }
        }
        return null;
    }

    public class IncreaseStage extends RunnableImpl
    {
        private TaintedDimensionWorld world;
        private L2PcInstance player;

        public IncreaseStage(TaintedDimensionWorld world)
        {
            this.world = world;
            player = world.player;
        }

        @Override
        public void runImpl() throws Exception {
            world.status += 1;
            startInstance(player);
        }
    }
}

