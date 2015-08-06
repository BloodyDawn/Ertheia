package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
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
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSendUIEvent;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;

import java.util.List;

import static dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 22.07.11
 * Time: 16:21
 */

public class SOA_Sansillion extends Quest
{
    private static final int ENTRANCE_ZONE_ID1 = 33013;
    private static final int ENTRANCE_ZONE_ID2 = 33014;
    private static final int INSIDE_ZONE_ID = 33015;

    private static final int TIE_ID = 33152;
    private static final int INSTANCE_ID = InstanceZoneId.NURSERY.getId();
    private static final int REWARD_ID = 17602;

    private static final long INSTANCE_PENALTY = 6 * 60 * 60 * 1000; // 3h
    private static final long INSTANCE_TIME = 30 * 60 * 1000; // 30 min

    private static final int[] MOBS = {
            23033, // Failed Creation
            23034, // Failed Creation
            23035, // Failed Creation
            23036, // Failed Creation
            23037, // Failed Creation
    };

    private static final Location LOC_ENTRANCE = new Location(-185853, 147878, -15313);
    private static final Location LOC_OUT = new Location(-178465, 153685, 2488);
    private final NpcStringId TIE_SHOUT = NpcStringId.THERE_IS_STILL_LOTS_OF_TIME_LEFT_DO_NOT_STOP_HERE;

    private static final int MAGUEN = 19037;

    public SOA_Sansillion()
    {
        addAskId(TIE_ID, -913);
        addAskId(TIE_ID, -914);
        addFirstTalkId(TIE_ID);
        addKillId(MOBS);
        addSpawnId(MOBS);

        addEnterZoneId(ENTRANCE_ZONE_ID1);
        addEnterZoneId(ENTRANCE_ZONE_ID2);
        addEnterZoneId(INSIDE_ZONE_ID);

        addExitZoneId(ENTRANCE_ZONE_ID1);
        addExitZoneId(ENTRANCE_ZONE_ID2);
    }

    public static void main(String[] args)
    {
        new SOA_Sansillion();
    }

    public class SansillionWorld extends InstanceWorld
    {
        public long _startedTime;
        public long _endTime;
        public int _points;
        public L2Npc _tie;
        public L2Npc _maguen;
        public int _lastBuff;

        public void updateTimer()
        {
            L2PcInstance player;
            int timerStatus = 3;
            if(status == 2)
            {
                timerStatus = 1; // Hiding cuz instance is about to over
            }

            int timeLeft = (int) ((_endTime - System.currentTimeMillis()) / 1000);
            timeLeft = Math.max(0, timeLeft);

            for(int id : allowed)
            {
                player = WorldManager.getInstance().getPlayer(id);
                if(player != null && player.getInstanceId() == instanceId)
                {
                    player.sendPacket(new ExSendUIEvent(player, timerStatus, timeLeft, _points, 60, 1911119));
                }
            }
        }

        public void spawnMaguen(Location loc)
        {
            L2PcInstance player;
            loc.setX(loc.getX() + Rnd.get(-200, 200));
            loc.setY(loc.getY() + Rnd.get(-200, 200));

            for(int id : allowed)
            {
                player = WorldManager.getInstance().getPlayer(id);
                if(player != null && player.getInstanceId() == instanceId)
                {
                    _maguen = addSpawn(MAGUEN, loc, true, instanceId);
                    _maguen.setTargetable(false);
                    _maguen.setTarget(player);
                    _maguen.setRunning();
                    _maguen.getAI().startFollow(player);
                    startQuestTimer("pickUp", 5000, null, player);
                }
            }
        }
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        SansillionWorld world = null;
        if(player != null)
        {
            InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
            if(tmpWorld instanceof SansillionWorld)
            {
                world = (SansillionWorld) tmpWorld;
            }
        }

        switch(event)
        {
            case "tryEnter":
                enterInstance(player);
                break;
            case "stopWorld":
                if(world != null && world.status == 1)
                {
                    world.status = 2;

                    List<L2Npc> mobs = InstanceManager.getInstance().getInstance(world.instanceId).getNpcs();
                    mobs.stream().filter(mob -> mob instanceof L2Attackable).forEach(mob -> {
                        mob.getSpawn().stopRespawn();
                        mob.getLocationController().delete();
                    });
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1811146), ExShowScreenMessage.TOP_CENTER, 2000, String.valueOf(world._points)));
                    cancelQuestTimer("spawnMaguen", null, player);
                    cancelQuestTimer("updateUI", null, player);
                }
                break;
            case "updateUI":
                if(world != null)
                {
                    world.updateTimer();
                    if(world.status < 2)
                    {
                        startQuestTimer("updateUI", 1, null, player);
                    }
                }
                break;
            case "spawnMaguen":
                if(world != null)
                {
                    world.spawnMaguen(player.getLoc());
                    if(world.status < 2)
                    {
                        startQuestTimer("spawnMaguen", 180000, null, player);
                    }
                }
                break;
            case "pickUp":
                if(world != null && !world._maguen.isDead())
                {
                    world._maguen.setTargetable(true);
                    world._maguen.setIsMortal(true);
                    int consume_points = Rnd.get(50, 100);
                    world._points -= consume_points;
                    world._maguen.getSpawn().stopRespawn();
                    world._maguen.getSpawn().getLastSpawn().scheduleDespawn(1000);
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1811145), ExShowScreenMessage.TOP_CENTER, 2000, String.valueOf(consume_points)));
                }
                break;
        }

        return null;
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        SansillionWorld world = null;
        if(player != null)
        {
            InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
            if(tmpWorld instanceof SansillionWorld)
            {
                world = (SansillionWorld) tmpWorld;
            }
        }
        if(world != null)
        {
            if(ask == -913)
            {
                if(reply == 1) // Да, приложу все усилия
                {
                    if(world.status == 0)
                    {
                        world.status = 1;
                        world._startedTime = System.currentTimeMillis();
                        world._endTime = world._startedTime + INSTANCE_TIME;

                        Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

                        for(L2Spawn spawn : instance.getGroupSpawn("creations"))
                            spawn.spawnOne(false);

                        startQuestTimer("spawnMaguen", 180000, null, player);
                        startQuestTimer("updateUI", 1, null, player);
                        startQuestTimer("stopWorld", INSTANCE_TIME, null, player);
                    }
                }
            }
            else if(ask == -914)
            {
                if(reply == 1)
                {
                    if(world.status == 2)
                    {
                        world.status = 3;
                        int crystal_count = 0;
                        int points = world._points;
                        if(points > 1 && points < 800)
                        {
                            crystal_count = 10;
                        }
                        else if(points < 1600)
                        {
                            crystal_count = 60;
                        }
                        else if(points < 2000)
                        {
                            crystal_count = 160;
                        }
                        else if(points < 2000)
                        {
                            crystal_count = 160;
                        }
                        else if(points < 2400)
                        {
                            crystal_count = 200;
                        }
                        else if(points < 2800)
                        {
                            crystal_count = 240;
                        }
                        else if(points < 3200)
                        {
                            crystal_count = 280;
                        }
                        else if(points < 3600)
                        {
                            crystal_count = 320;
                        }
                        else if(points < 4000)
                        {
                            crystal_count = 360;
                        }
                        else if(points > 4000)
                        {
                            crystal_count = 400;
                        }

                        if(crystal_count > 0)
                        {
                            player.addItem(ProcessType.QUEST, REWARD_ID, crystal_count, npc, true);
                        }
                        player.teleToLocation(LOC_OUT);
                        InstanceManager.getInstance().destroyInstance(world.instanceId);
                        return "soldier_thai001d.htm";
                    }
                }
            }
        }

        return null;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        SansillionWorld world = null;
        InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
        if(tmpWorld instanceof SansillionWorld)
        {
            world = (SansillionWorld) tmpWorld;
        }

        if (npc.getNpcId() == MAGUEN)
        {
            if (world != null)
            {
                int give_points = Rnd.get(100, 200);
                world._points += give_points;
                world._maguen.getSpawn().stopRespawn();
                world._maguen.getLocationController().delete();
                player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1811147), ExShowScreenMessage.TOP_CENTER, 2000, String.valueOf(give_points)));
            }
        }

        if(world != null && npc.getNpcId() != MAGUEN)
        {
            world._points += Rnd.get(8, 15);

            if(Rnd.getChance(10) && world._lastBuff < 3)
            {
                L2Skill skill = SkillTable.getInstance().getInfo(14228 + Math.min(3, world._lastBuff++), 1);
                if(skill != null)
                {
                    skill.getEffects(player, player);
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.RECEIVED_REGENERATION_ENERGY, ExShowScreenMessage.TOP_CENTER, 2000));
                }
            }
        }

        return super.onKill(npc, player, isPet);
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        SansillionWorld world = null;
        InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
        if(tmpWorld instanceof SansillionWorld)
        {
            world = (SansillionWorld) tmpWorld;
        }

        if(world != null)
        {
            if(world.status == 0)
            {
                return "soldier_thai001a.htm";
            }
            else if(world.status == 1)
            {
                L2Effect effect = player.getFirstEffect(14228);
                if(effect != null)
                {
                    player.sendPacket(new ExShowScreenMessage(NpcStringId.SOLDER_TIE_RECEIVED_S1_PRIECES_OF_BIO_ENERGY_RESIDUE, ExShowScreenMessage.TOP_CENTER, 2000, "40"));
                    player.removeEffect(effect);
                    world._points += 40;
                    world._lastBuff = 0;
                }
                else
                {
                    effect = player.getFirstEffect(14229);
                    if(effect != null)
                    {
                        player.sendPacket(new ExShowScreenMessage(NpcStringId.SOLDER_TIE_RECEIVED_S1_PRIECES_OF_BIO_ENERGY_RESIDUE, ExShowScreenMessage.TOP_CENTER, 2000, "60"));
                        player.removeEffect(effect);
                        world._points += 60;
                        world._lastBuff = 0;
                    }
                    else
                    {
                        effect = player.getFirstEffect(14230);
                        if(effect != null)
                        {
                            player.sendPacket(new ExShowScreenMessage(NpcStringId.SOLDER_TIE_RECEIVED_S1_PRIECES_OF_BIO_ENERGY_RESIDUE, ExShowScreenMessage.TOP_CENTER, 2000, "80"));
                            player.removeEffect(effect);
                            world._points += 80;
                            world._lastBuff = 0;
                        }
                    }
                }
                return "soldier_thai001b.htm";
            }
            else if(world.status == 2)
            {
                return "soldier_thai001c.htm";
            }
        }
        return super.onFirstTalk(npc, player);
    }

    @Override
    public String onEnterZone(L2Character character, L2ZoneType zone)
    {
        L2PcInstance player = character.getActingPlayer();
        SansillionWorld world = null;
        if(player != null)
        {
            InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
            if(tmpWorld instanceof SansillionWorld)
            {
                world = (SansillionWorld) tmpWorld;
            }
        }

        if(zone.getId() == INSIDE_ZONE_ID)
        {
            if(world != null)
            {
                if(world.status == 1)
                {
                    world._tie.broadcastPacket(new NS(world._tie.getObjectId(), ChatType.NPC_SHOUT, world._tie.getNpcId(), TIE_SHOUT));
                }
            }
        }
        else if(zone.getId() == ENTRANCE_ZONE_ID1 || zone.getId() == ENTRANCE_ZONE_ID2)
        {
            startQuestTimer("tryEnter", 3 * 1000, null, player);
        }
        return super.onEnterZone(character, zone);
    }

    @Override
    public String onExitZone(L2Character character, L2ZoneType zone)
    {
        L2PcInstance player = character.getActingPlayer();

        // Player is out of zone so he don't want to join the instance :)
        if(player != null)
        {
            if(zone.getId() == ENTRANCE_ZONE_ID1 || zone.getId() == ENTRANCE_ZONE_ID2)
            {
                cancelQuestTimer("tryEnter", null, player);
            }
        }

        return super.onExitZone(character, zone);
    }

    protected void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if(world != null)
        {
            if(!(world instanceof SansillionWorld))
            {
                player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
                return;
            }
            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if(inst != null)
            {
                teleportPlayer(player, LOC_ENTRANCE, world.instanceId);
            }
        }
        else if(checkConditions(player))
        {
            int instanceId = InstanceManager.getInstance().createDynamicInstance("SOA_Sansillion.xml");

            world = new SansillionWorld();
            world.instanceId = instanceId;
            world.templateId = INSTANCE_ID;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);
            world.allowed.add(player.getObjectId());

            ((SansillionWorld) world)._tie = addSpawn(TIE_ID, -185857, 147420, -15296, 11632, false, 0, false, instanceId);

            teleportPlayer(player, LOC_ENTRANCE, instanceId);
            InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCE_ID, System.currentTimeMillis() + INSTANCE_PENALTY);
        }
    }

    private boolean checkConditions(L2PcInstance player)
    {
        long reenterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), INSTANCE_ID);
        if(player.isGM())
        {
            return true;
        }
        if(player.getLevel() < 85 || player.getLevel() > 89)
        {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
            return false;
        }
        if(System.currentTimeMillis() < reenterTime)
        {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
            return false;
        }
        return true;
    }

    private void teleportPlayer(L2PcInstance player, Location location, int instanceId)
    {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        player.getInstanceController().setInstanceId(instanceId);
        player.teleToInstance(location, instanceId);
    }
}