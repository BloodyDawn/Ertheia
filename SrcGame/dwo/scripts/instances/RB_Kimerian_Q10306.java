package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.RunnableImpl;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.Calendar;
import java.util.List;

/**
 * La2Era Team
 * User: GenCloud
 * Date: 01.02.15
 * Time: 23.04
 * http://www.youtube.com/watch?v=LaDrBfxDgfk&feature=player_embedded#!
 */

public class RB_Kimerian_Q10306 extends Quest
{
    // ID инстансов
    private static final int INSTANCE_ID_LIGHT  = InstanceZoneId.KIMERIAN_2.getId();
    private static final int INSTANCE_ID_HARD   = InstanceZoneId.KIMERIAN_EPIC_BATTLE_2.getId();

    // Монсты
    private static final int KIMERIAN_LIGHT = 25745;    // Обычный Кимериан
    private static final int KIMERIAN_HARD = 25758;     // Экстримальный Кимериан
    private static final int KIMERIAN_NPC = 25747;      // НПЦ, Болтает при входу в зону

    private static final int PHANTOME_KIMERIAN_1 = 25746;
    private static final int PHANTOME_KIMERIAN_2 = 25759;

    private static final int NOETI_KHISHARU = 32896;
    private static final int NOETI_MIMILEAD_LIGHT_1 = 33098;    // 33098 - Лайт в начале инсты
    private static final int NOETI_MIMILEAD_HARD_1 = 33099;     // 33099 - Хард в начале инсты
    private static final int NOETI_MIMILEAD_LIGHT_2 = 33131;    // 33131 - Лайт Энд
    private static final int NOETI_MIMILEAD_HARD_2 = 33132;      // 33132 - Хард Энд

    private static final int FAIRY_REBEL    = 32913;        // Спавняться при входе в инсту, и бегают хвостиком за персонажем, помогают в битвах

    private static final int NAOMI_KASHERON_LIGHT = 32914;  // Спавняться при входе в инсту, и бегают хвостиком за персонажем, помогают в битвах
    private static final int NAOMI_KASHERON_HARD = 33097;   // Спавняться при входе в инсту, и бегают хвостиком за персонажем, помогают в битвах

    // Зоны
    private static final int ZONE_ID = 400113;

    // Локации
    private static final Location ENTRANCE = new Location(215473, 79962, 816);
    private static final Location RAID_SPAWN = new Location(224330, 70826, 1636);
    private static final Location MIMILEAD_SPAWN = new Location(215595, 79865, 827);
    private static final Location FAKE_KIMERIAN_SPAWN = new Location(217321, 78631, 962);

    // Предметы
    private static final int GLIMMER = 17374;
    private static final int FAIRYS_LEAF_FLUTE = 17378;

    public RB_Kimerian_Q10306()
    {
        super();

        addAskId(NOETI_KHISHARU, 111);
        addAskId(NOETI_KHISHARU, 112);
        addAskId(NOETI_MIMILEAD_LIGHT_1, 100);
        addAskId(NOETI_MIMILEAD_HARD_1, 100);

        addAttackId(KIMERIAN_LIGHT, KIMERIAN_HARD);

        addKillId(KIMERIAN_LIGHT, KIMERIAN_HARD);
        addKillId(PHANTOME_KIMERIAN_1, PHANTOME_KIMERIAN_2);

        addEnterZoneId(ZONE_ID);
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        KimerianWorld world = InstanceManager.getInstance().getInstanceWorld(npc, KimerianWorld.class);
        if(world == null)
        {
            return null;
        }

        if(npc.getNpcId() == KIMERIAN_LIGHT || npc.getNpcId() == KIMERIAN_HARD)
        {
            if(world.status == 0)
            {
                world.status = 1;
                world.kimerian.showChatText(ChatType.ALL, NpcStringId.WHO_DO_YOU_THINK_YOU_ARE_TO_TRY_MY_AUTHORITY);
            }
            else if(world.status == 1)
            {
                double hp = npc.getCurrentHp() / npc.getMaxHp();
                if (hp <= 0.5)
                {
                    // Raid is immortal
                    world.kimerian.setIsParalyzed(true);
                    world.kimerian.setIsInvul(true);
                    world.kimerian.setIsMortal(false);
                    world.kimerian.setTargetable(false);
                    world.kimerian.startAbnormalEffect(AbnormalEffect.S_INVINCIBLE);

                    world.kimerian.showChatText(ChatType.ALL, NpcStringId.FOOLISH__INSIGNIFICANT_CREATURES__HOW_DARE_YOU_CHALLENGE_ME);

                    L2MonsterInstance phantom;
                    for (int i = 0; i < 6; i++)
                    {
                        phantom = (L2MonsterInstance) addSpawn(world.isHardInstance ? PHANTOME_KIMERIAN_2 : PHANTOME_KIMERIAN_1, attacker.getLoc(), true, world.instanceId);
                        phantom.addDamage(attacker, 999, null);

                        world.phantoms.add(phantom);
                    }

                    world.status = 2;
                }
            }
        }

        return null;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
    {
        KimerianWorld world = InstanceManager.getInstance().getInstanceWorld(npc, KimerianWorld.class);
        if(world == null)
        {
            return null;
        }

        Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
        if(instance != null)
        {
            if (world.status == 2 && world.phantoms.contains(npc))
            {
                world.phantoms.remove(npc);
                if (world.phantoms.isEmpty())
                {
                    world.kimerian.setIsMortal(true);
                    world.kimerian.setIsInvul(false);
                    world.kimerian.setIsParalyzed(false);
                    world.kimerian.setTargetable(true);
                    world.kimerian.stopAbnormalEffect(AbnormalEffect.S_INVINCIBLE);

                    world.kimerian.showChatText(ChatType.ALL, NpcStringId.YOU_LIVING_YET);

                    world.status = 3;
                }
            }
            else if(world.status == 3 && (npc.getNpcId() == KIMERIAN_LIGHT || npc.getNpcId() == KIMERIAN_HARD))
            {
                ThreadPoolManager.getInstance().scheduleGeneral(() ->
                {
                    L2Npc spirit = addSpawn(KIMERIAN_NPC, npc.getLoc(), true, world.instanceId);

                    ThreadPoolManager.getInstance().scheduleGeneral(() -> spirit.showChatText(ChatType.ALL, NpcStringId.I_WILL_COME_BACK_ALIVE_WITH_ROTTING_AURA), 2000);
                    ThreadPoolManager.getInstance().scheduleGeneral(() -> spirit.showChatText(ChatType.ALL, NpcStringId.HA_HA_HA_HA), 5000);//2010067
                    ThreadPoolManager.getInstance().scheduleGeneral(() ->
                    {
                        spirit.getLocationController().delete();

                        L2Npc voicer = addSpawn(world.isHardInstance ? NOETI_MIMILEAD_HARD_2 : NOETI_MIMILEAD_LIGHT_2, npc.getLoc(), true);

                        ThreadPoolManager.getInstance().scheduleGeneral(() -> voicer.showChatText(ChatType.ALL, NpcStringId.UNFORTUNATELY__THEY_RAN_AWAY), 2000);
                    }, 7000);

                }, 1000);

                instance.setDuration(300000);
            }
        }

        return super.onKill(npc, killer, isPet);
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        KimerianWorld world = InstanceManager.getInstance().getInstanceWorld(npc, KimerianWorld.class);

        switch (npc.getNpcId())
        {
            case NOETI_KHISHARU:
            {
                if (!enterInstance(player, ask == 112))
                    return "noetieh_kisharu004.htm";
            }
            case NOETI_MIMILEAD_LIGHT_1:
            case NOETI_MIMILEAD_HARD_1:
            {
                if(world == null || world.instanceId != player.getInstanceId())
                {
                    return null;
                }

                if(world.useFairysFlute)
                {
                    return "noeti_mymirid_indun1002.htm";
                }

                if(!world.giveGlimmer)
                {
                    world.giveGlimmer = true;

                    player.addItem(ProcessType.QUEST, GLIMMER, 10, null, true);
                }

                if(player.getItemsCount(FAIRYS_LEAF_FLUTE) > 0)
                {
                    world.useFairysFlute = true;

                    world.followers.add(addSpawn(FAIRY_REBEL, player.getLoc(), true, world.instanceId));
                    world.followers.add(addSpawn(FAIRY_REBEL, player.getLoc(), true, world.instanceId));

                    player.destroyItem(ProcessType.QUEST, FAIRYS_LEAF_FLUTE, 1, null, true);
                }
                else
                    return "noeti_mymirid_indun1003.htm";
            }
        }

        return null;
    }

    @Override
    public String onEnterZone(L2Character character, L2ZoneType zone)
    {
        KimerianWorld world = InstanceManager.getInstance().getInstanceWorld(character, KimerianWorld.class);
        if(world == null) {
            return null;
        }

        if(!world.fakeKmerian1)
        {
            world.fakeKmerian1 = true;

            final L2Npc npc = addSpawn(KIMERIAN_NPC, FAKE_KIMERIAN_SPAWN, true, world.instanceId);

            ThreadPoolManager.getInstance().scheduleAi(() -> npc.showChatText(ChatType.ALL, NpcStringId.HOW_RIDICULOUS__YOU_THINK_YOU_CAN_FIND_ME), 1000);
            ThreadPoolManager.getInstance().scheduleAi(() -> npc.showChatText(ChatType.ALL, NpcStringId.THEN_TRY__HA_HA_HA), 6000);
            ThreadPoolManager.getInstance().scheduleAi(() -> npc.getLocationController().delete(), 15000);
        }

        return null;
    }

    private long getReuseTime()
    {
        final Calendar _instanceTime = Calendar.getInstance();

        Calendar currentTime = Calendar.getInstance();
        _instanceTime.set(Calendar.HOUR_OF_DAY, 6);
        _instanceTime.set(Calendar.MINUTE, 30);
        _instanceTime.set(Calendar.SECOND, 0);

        if(_instanceTime.compareTo(currentTime) < 0)
        {
            _instanceTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        return _instanceTime.getTimeInMillis();
    }

    private boolean enterInstance(L2PcInstance player, boolean isExtreme)
    {
        return enterInstance(player, "RB_Kimerian_Q10306.xml", isExtreme) != 0;
    }

    private int enterInstance(L2PcInstance player, String template, boolean isHardInstance)
    {
        if(!player.isGM())
        {
            return 0;
        }

        int instanceId;

        KimerianWorld world;
        InstanceManager.InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);

        if(player.isGM())
        {
            tmpWorld = null;
        }

        if(tmpWorld != null)
        {
            if(!(tmpWorld instanceof KimerianWorld))
            {
                player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
                return 0;
            }

            ((KimerianWorld) tmpWorld).playersInside = player;

            player.teleToInstance(ENTRANCE, tmpWorld.instanceId);

            return tmpWorld.instanceId;
        }
        else
        {
            world = (KimerianWorld) (tmpWorld = new KimerianWorld());

            world.isHardInstance = isHardInstance;

            int instanceTemplateId = isHardInstance ? INSTANCE_ID_HARD : INSTANCE_ID_LIGHT;
            if(!checkConditions(player, world))
            {
                return 0;
            }

            long reenter = getReuseTime();

            instanceId = InstanceManager.getInstance().createDynamicInstance(template);

            world.instanceId = instanceId;
            world.templateId = instanceTemplateId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(tmpWorld);
            InstanceManager.getInstance().setInstanceTime(player.getObjectId(), isHardInstance ? INSTANCE_ID_HARD : INSTANCE_ID_LIGHT, reenter);

            player.teleToInstance(ENTRANCE, instanceId);
            world.allowed.add(player.getObjectId());
            world.playersInside = player;

            startChallenge(player, world);

            return instanceId;
        }
    }

    private boolean checkConditions(L2PcInstance player, KimerianWorld world)
    {
       /* Для дебага */
        if(player.isGM())
        {
            Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), world.templateId);
            if(System.currentTimeMillis() < reEnterTime)
            {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
                return false;
            }
            return true;
        }

        if(player.getLevel() < minLevel || player.getLevel() > maxLevel || !player.isAwakened())
        {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
            return false;
        }

        Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), world.templateId);
        if(System.currentTimeMillis() < reEnterTime)
        {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
            return false;
        }

        return true;
    }

    private void startChallenge(L2PcInstance player, final KimerianWorld world)
    {
        L2Npc npc;
        Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

        Location loc = player.getLoc();

        npc = addSpawn(world.isHardInstance ? NAOMI_KASHERON_HARD : NAOMI_KASHERON_LIGHT, loc, true, world.instanceId);
        npc.showChatText(ChatType.ALL, NpcStringId.NOW__BEFORE_ITS_TOO_LATE__WE_NEED_TO_TRACK_KIMERIAN_TO_CATCH_HIM);

        world.followers.add(npc);

        npc = addSpawn(FAIRY_REBEL, loc, true, world.instanceId);
        npc.showChatText(ChatType.ALL, NpcStringId.DEFEAT__KIMERIAN);

        world.followers.add(npc);
        world.followers.add(addSpawn(FAIRY_REBEL, loc, true, world.instanceId));

        for(L2Npc guard : world.followers)
        {
            ((L2GuardInstance) guard).setCanAttackPlayer(false);
            ((L2GuardInstance) guard).setCanAttackGuard(false);
            ((L2GuardInstance) guard).setReturnHome(false);
        }

        addSpawn(world.isHardInstance ? NOETI_MIMILEAD_HARD_1 : NOETI_MIMILEAD_LIGHT_1, MIMILEAD_SPAWN, false, world.instanceId);
        world.kimerian = addSpawn(world.isHardInstance ? KIMERIAN_HARD : KIMERIAN_LIGHT, RAID_SPAWN, false, world.instanceId);

        instance.addTask("inviteTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new InviteTask(world), 2000, 2000));
        instance.addTask("supportTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SupportTask(world), 2000, 2000));
    }

    public class KimerianWorld extends InstanceManager.InstanceWorld
    {
        public boolean              isHardInstance  = false;
        public boolean              useFairysFlute  = false;
        public boolean              giveGlimmer     = false;
        public boolean              fakeKmerian1    = false;
        public L2PcInstance         playersInside   = null;
        public List<L2Npc> followers                = new FastList<>();
        public List<L2Npc> phantoms                 = new FastList<>();
        public L2Npc                kimerian        = null;
    }

    /**
     * Задача предназначена для саппорт-npc в соло-картии.
     * Помогают пинать мобов, хиляют, используют скиллы.
     */
    private class SupportTask extends RunnableImpl
    {
        private final KimerianWorld _world;
        private int _lastPlayerHeading;

        public SupportTask(KimerianWorld world)
        {
            _world = world;
            _lastPlayerHeading = -1;
        }

        @Override
        public void runImpl() throws Exception
        {
            // Если меняется heading персонажа, то будем перемещать всех саппортов
            boolean reFollowAll = false;
            for(L2Npc follower : _world.followers)
            {
                boolean needFollow = true;
                // NPC ничего не делает
                if((follower.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK || !follower.isAttackingNow()) && (follower.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST || !follower.isCastingNow()) && follower.getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO)
                {
                    // Ну жость у нас а не двиг агра NPC друг на друга. Чистим хейт-лист от возможных агров гвардов друг на друга :))
                    ((L2Attackable) follower).getAggroList().values().stream().filter(aggro -> aggro.getAttacker().isNpc() || aggro.getAttacker().isPlayer()).forEach(aggro ->
                    {
                        ((L2Attackable) follower).getAggroList().remove(aggro.getAttacker());
                    });

                    if(_world.playersInside != null && Util.calculateDistance(_world.playersInside, follower, true) > 600)
                    {
                        needFollow = true;
                    }
                    else if(_world.playersInside != null)
                    {
                        for(L2Npc npc : follower.getKnownList().getKnownNpcInRadius(600))
                        {
                            if(npc.isMonster() && !npc.isInvul())
                            {
                                ((L2GuardInstance) follower).attackCharacter(npc);
                                needFollow = false;
                                break;
                            }
                        }

                        // Проверим, менялся ли heading у персонажа
                        if(needFollow)
                        {
                            if(_lastPlayerHeading < 0 || (_lastPlayerHeading != _world.playersInside.getHeading()) || reFollowAll)
                            {
                                needFollow = true;
                                reFollowAll = true;
                            }
                            else
                            {
                                needFollow = false;
                            }

                            _lastPlayerHeading = _world.playersInside.getHeading();
                        }
                    }
                }
                else
                {
                    needFollow = false;
                }

                if(needFollow)
                {
                    if(Util.calculateDistance(_world.playersInside, follower, true) > 1000)
                    {
                        follower.teleToInstance(_world.playersInside.getLoc(), _world.instanceId, true);
                        continue;
                    }

                    // Хак для того, чтобы гварды атаковали везде, а не только в рендже своего спауна
                    follower.getSpawn().setLocx(follower.getX());
                    follower.getSpawn().setLocy(follower.getY());
                    follower.getSpawn().setLocz(follower.getZ());

                    double angle = Util.convertHeadingToDegree(_world.playersInside.getHeading());
                    double radians = Math.toRadians(angle);
                    double radius = 100.;
                    double course = 160;

                    int x = (int) (Math.cos(Math.PI + radians + course) * radius);
                    int y = (int) (Math.sin(Math.PI + radians + course) * radius);

                    follower.setRunning();
                    Location loc = _world.playersInside.getLoc();
                    loc.setX(loc.getX() + x + Rnd.get(-100, 100));
                    loc.setY(loc.getY() + y + Rnd.get(-100, 100));
                    follower.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
                }
            }
        }
    }

    private class InviteTask extends RunnableImpl
    {
        private final KimerianWorld world;
        private final Instance instance;

        public InviteTask(KimerianWorld _world)
        {
            world = _world;
            instance = InstanceManager.getInstance().getInstance(world.instanceId);
        }

        @Override
        public void runImpl() throws Exception
        {
            final L2PcInstance player = world.playersInside;

            instance.getAllByNpcId(FAIRY_REBEL, true).forEach(npc ->
            {
                if(Util.calculateDistance(player, npc, true) < 600)
                {
                    world.followers.add(npc);

                    ((L2GuardInstance) npc).setCanAttackPlayer(false);
                    ((L2GuardInstance) npc).setCanAttackGuard(false);
                    ((L2GuardInstance) npc).setReturnHome(false);
                }
            });
        }
    }

    public static void main(String[] args)
    {
        new RB_Kimerian_Q10306();
    }
}
