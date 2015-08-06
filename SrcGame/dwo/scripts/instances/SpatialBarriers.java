package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;

import java.util.List;

/**
 * User: GenCloud
 * Date: 01.04.2015
 * Team: La2Era Team
 * * 
 * TODO:
 * 1) найти инфу по рамунам
 * 2) найти инфу по лавушкам
 * 3) найти инфу по мобам и их спауну
 * 4) дописать этажи + стадии к ним
 * *
 */
public class SpatialBarriers extends Quest
{
    //instance info
    private static final int INSTANCE_ID = InstanceZoneId.SPATIAL_BARRIERS.getId();
    private static final long INSTANCE_TIME = 15 * 60 * 1000; // 15 min

    //npc
    private static final int NPC_EINSTEIN = 33975;
    private static final int NPC_RESHET = 33974;

    private static final int[] _traps =
            {
                    19556,
                    19557,
                    19558
            };

    private static final int TRAP_STAGE_PURPLE = 19556; //npc state: 1-11 (1), 12-22 (2), 23-34 (3)
    private static final int TRAP_STAGE_RED = 19557; //npc state: 1-11 (1), 12-22 (2), 23-34 (3)
    private static final int TRAP_STAGE_YELLOW = 19558; //npc state: 1-11 (1), 12-22 (2), 23-34 (3)

    private static final int DIMENSIONAL_LIGHT = 19562;

    //1-11
    private static final int[] _stage1_11 = new int[]
            {
                    23462, 23463, 23464, 23465, 23466, 23467, 23468, 23569
            };

    //12-22
    private static final int[] _stage12_22 = new int[]
            {
                    23470, 23471, 23472, 23473, 23474, 23475, 23476
            };

    //23-34    
    private static final int[] _stage23_34 = new int[]
            {
                    23477, 23478, 23479, 23480, 23481, 23482, 23483
            };

    // Raid Bosses
    private static final int ABYSSAL_MAKKUM = 26090;

    //location's
    private static final Location LOC_ENTRANCE = new Location(-206525, 242248, 439);
    private static final Location LOC_EXIT = new Location(-82088, 249880, -3392); //TODO
    private static final Location LOC_EINSTEIN = new Location(-207223, 241570, 389); //FIXME: "Z" coord is not allowed

    private static final int[][] _locStage1Traps =
            {
                    {-207440, 241051, 389},
                    {-207624, 241234, 389},
                    {-207886, 241329, 389},
                    {-207060, 241572, 389}
            };

    //item's
    private static final int CONVERTER_BARRIER = 39597;

    private static SpatialBarriers _instance;

    public SpatialBarriers()
    {
        super();
        addAskId(NPC_EINSTEIN, 33975);
        addAskId(NPC_RESHET, 33974);
    }

    public static void main(String[] args)
    {
        _instance = new SpatialBarriers();
    }

    public SpatialBarriers getInstance()
    {
        return _instance;
    }

    public class SBWorld extends InstanceWorld
    {
        private List<L2PcInstance> _playersInside   = new FastList<>();
        private List<L2Npc> _monsters               = new FastList<>();
        private List<L2Npc> _traps                  = new FastList<>();
        private L2Npc _einstein                     = null;
        private int _chanceToSpawn                  = 0;
        private int _stageOnFloor                   = 0;
    }

    protected void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if(world != null)
        {
            if(!(world instanceof SBWorld))
            {
                player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
                return;
            }
            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if(inst != null)
            {
                player.teleToInstance(LOC_ENTRANCE, world.instanceId);
            }
        }
        else if(checkConditions(player))
        {
            int instanceId = InstanceManager.getInstance().createDynamicInstance("SpatialBarriers.xml");//TODO: XML template

            world = new SBWorld();
            world.instanceId = instanceId;
            world.templateId = INSTANCE_ID;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            if(player.isGM() || player.getParty() == null)
            {
                teleportPlayer(player, LOC_ENTRANCE, instanceId);
                world.allowed.add(player.getObjectId());
                ((SBWorld) world)._playersInside.add(player);
            }


            if(player.getParty() != null)
            {
                for(L2PcInstance partyMember : player.getParty().getMembers())
                {
                    teleportPlayer(partyMember, LOC_ENTRANCE, instanceId);
                    world.allowed.add(partyMember.getObjectId());
                    ((SBWorld) world)._playersInside.add(partyMember);
                }
            }

            ((SBWorld) world)._einstein = addSpawn(NPC_EINSTEIN, LOC_EINSTEIN, world.instanceId);
        }
    }

    private boolean checkConditions(L2PcInstance player)
    {
        if(player.isGM())
        {
            return true;
        }

        if(player.getLevel() < 99)
        {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
            return false;
        }

        if (player.getInventory().getItemByItemId(CONVERTER_BARRIER).getCount() < 3)
        {
            return false; //todo: message
        }

        if (player.getParty() != null)
        {
            if (player.getParty().getMembers().size() > 4)
            {
                return false; //todo: message
            }
        }

        if (player.getParty() == null)
        {
            return true; //todo: message
        }

        return true;
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        SBWorld world = InstanceManager.getInstance().getInstanceWorld(player, SBWorld.class);

        switch (event)
        {
            case "stopWorld":
            {
                List<L2Npc> mobs = InstanceManager.getInstance().getInstance(world.instanceId).getNpcs();
                mobs.stream().filter(mob -> mob instanceof L2Attackable).forEach(mob -> {
                    mob.getSpawn().stopRespawn();
                    mob.getLocationController().delete();
                });

                if (player.getParty() != null)
                {
                    for (L2PcInstance party : player.getParty().getMembers())
                    {
                        party.teleToLocation(LOC_EXIT);
                    }
                }
                else
                {
                    player.teleToLocation(LOC_EXIT);
                }

                InstanceManager.getInstance().destroyInstance(world.instanceId);
            }
            break;
        }
        return null;
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        SBWorld world = InstanceManager.getInstance().getInstanceWorld(player, SBWorld.class);

        switch (npc.getNpcId())
        {
            case NPC_EINSTEIN:
            {
                if (ask == 33975)
                {
                    switch (reply)
                    {
                        case 1: //12 Преобразователь Барьера
                        {
                            destroyItemByReplyId(player, reply);
                            world._chanceToSpawn = 20; //in percent
                            world.status = 1;
                            world._stageOnFloor = 0;
                            startNextFloor(world);
                            startQuestTimer("stopWorld", INSTANCE_TIME, null, player);
                            npc.getSpawn().stopRespawn();
                            npc.getLocationController().delete();
                            break;
                        }
                        case 2: //240 Преобразователь Барьера
                        {
                            destroyItemByReplyId(player, reply);
                            world._chanceToSpawn = 27; //in percent
                            world.status = 1;
                            world._stageOnFloor = 0;
                            startNextFloor(world);
                            startQuestTimer("stopWorld", INSTANCE_TIME, null, player);
                            npc.getSpawn().stopRespawn();
                            npc.getLocationController().delete();
                            break;
                        }
                        case 3: //1200 Преобразователь Барьера
                        {
                            destroyItemByReplyId(player, reply);
                            world._chanceToSpawn = 34; //in percent
                            world.status = 1;
                            world._stageOnFloor = 0;
                            startNextFloor(world);
                            startQuestTimer("stopWorld", INSTANCE_TIME, null, player);
                            npc.getSpawn().stopRespawn();
                            npc.getLocationController().delete();
                            break;
                        }
                        case 4: //4 todo xz
                        {
                            break;
                        }
                    }
                }
                break;
            }
            case NPC_RESHET: //TODO
            {
                if (ask == 33974)
                {
                    switch (reply)
                    {
                        case 1:
                            enterInstance(player);
                            break;
                    }
                }
                break;
            }
        }
        return null;
    }

    public void startNextFloor(final SBWorld world) //todo: info = 0 35 этажей
    {
        L2Npc traps;

        switch (world.status)
        {
            case 1:
                switch (world._stageOnFloor)
                {
                    case 0:
                        for (int[] spawnInfo : _locStage1Traps)
                        {
                            traps = addSpawn(spawnInfo[0], spawnInfo[1], spawnInfo[2], true, world.instanceId, _traps);
                            traps.setDisplayEffect(Rnd.get(1, 9));
                            world._traps.add(traps);
                        }
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                break;
            case 2:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 3:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 4:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 5:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 6:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 7:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 8:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 9:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 10:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 11:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 12:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 13:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 14:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 15:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 16:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 17:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 18:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 19:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 20:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 21:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 22:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 23:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 24:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 25:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 26:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 27:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 28:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 29:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 30:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 31:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 32:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 33:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 34:
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 35: //final part, spawn RB
                switch (world._stageOnFloor)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
                break;
            case 36: //exit
//                world._playersInside.stream().forEach(p -> p.teleToLocation(LOC_OUT));
                break;
        }
    }

    private void destroyItemByReplyId(L2PcInstance player, int replyId)
    {
        if (replyId == 1) //12
        {
            if (player.getParty() != null)
            {
                for (L2PcInstance party : player.getParty().getMembers())
                {
                    if (party.getParty().getMembers().size() == 2)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 6, party, "Instance");
                    }
                    else if (party.getParty().getMembers().size() == 3)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 4, party, "Instance");
                    }
                    else if (party.getParty().getMembers().size() == 4)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 3, party, "Instance");
                    }
                }
            }
            else
            {
                player.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 12, player, "Instance");
            }
        }
        else if (replyId == 2) //240
        {
            if (player.getParty() != null)
            {
                for (L2PcInstance party : player.getParty().getMembers())
                {
                    if (party.getParty().getMembers().size() == 2)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 120, party, "Instance");
                    }
                    else if (party.getParty().getMembers().size() == 3)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 80, party, "Instance");
                    }
                    else if (party.getParty().getMembers().size() == 4)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 60, party, "Instance");
                    }
                }
            }
            else
            {
                player.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 240, player, "Instance");
            }
        }
        else if (replyId == 3) //1200
        {
            if (player.getParty() != null)
            {
                for (L2PcInstance party : player.getParty().getMembers())
                {
                    if (party.getParty().getMembers().size() == 2)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 600, party, "Instance");
                    }
                    else if (party.getParty().getMembers().size() == 3)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 400, party, "Instance");
                    }
                    else if (party.getParty().getMembers().size() == 4)
                    {
                        party.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 300, party, "Instance");
                    }
                }
            }
            else
            {
                player.getInventory().destroyItemByItemId(ProcessType.QUEST, CONVERTER_BARRIER, 1200, player, "Instance");
            }
        }
    }

    /**
     * @param count количество
     * @param radius разброс
     * @param loc локация
     * @param npcIds списко монстров
     */
    private void spawnTraps(SBWorld world, int count, int radius, Location loc, int... npcIds)
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

            world._traps.add(npc);
        }
    }

    /**
     * @param count количество
     * @param radius разброс
     * @param loc локация
     * @param npcIds списко монстров
     */
    private void spawnMobs(SBWorld world, int count, int radius, Location loc, int... npcIds)
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

            world._monsters.add(npc);
        }
    }

    private void teleportPlayer(L2PcInstance player, Location location, int instanceId)
    {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        player.getInstanceController().setInstanceId(instanceId);
        player.teleToInstance(location, instanceId);
    }
}
