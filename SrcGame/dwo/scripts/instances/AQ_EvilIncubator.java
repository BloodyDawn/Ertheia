package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.RunnableImpl;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * User: GenCloud
 * Date: 16.03.2015
 * Team: La2Era Team
 */
public class AQ_EvilIncubator extends Quest
{

    // Квестовые персонажи в инстансе (в отдельный скрипт)
    public static final int Адольф = 33170;
    public static final int ЖрецЭллис = 33171;
    public static final int СержантБартон = 33172;
    public static final int СнайперХаюк = 33173;
    public static final int ВолшебникЭллия = 33174;
    public static final int[] Помощники = {ЖрецЭллис, СержантБартон, СнайперХаюк, ВолшебникЭллия};

    // Квестовые монстры
    private static final int[] Монстры = {27430, 27430, 27432, 27433, 27434};

    private static final Location ENTER_POINT = new Location(56177, -172910, -7952, 20002);
    private static final Location BATTLE_POINT = new Location(56167, -175615, -7944, 49653);
    private static final Location EXIT_POINT = new Location(172292, 31158, -3696, 16839);

    private static AQ_EvilIncubator _instance;

    private static final int[][][] SPAWNLIST_MONSTERS_1_WAVE_TYPE_1 = {
            {
                    {27431, 56205, -177550, -7944, 63},
                    {27434, 56095, -177550, -7944, 233},
                    {27434, 56245, -177550, -7944, 255},
                    {27431, 56125, -177550, -7944, 97},
                    {27431, 56165, -177550, -7944, 33}
            },
            {
                    {27431, 55645, -176695, -7944, 15},
                    {27430, 55645, -176765, -7944, 97},
                    {27434, 55645, -176735, -7944, 209},
                    {27434, 55645, -176735, -7944, 47}
            },
            {
                    {27431, 56590, -176744, -7944, 141},
                    {27431, 56595, -176695, -7944, 269},
                    {27430, 56642, -176560, -7952, 263},
                    {27434, 56023, -177087, -7952, 16026},
                    {27434, 56212, -176074, -7944, 37403}
            }
    };

    public AQ_EvilIncubator()
    {
        super();
    }

    public static void main(String[] args)
    {
        _instance = new AQ_EvilIncubator();
    }

    public static AQ_EvilIncubator getInstance()
    {
        return _instance;
    }

    public class DayOfDestinyWorld extends InstanceManager.InstanceWorld
    {
        private Map<L2Npc, Boolean> monsters;
        private Map<Integer, L2Npc> helpers;
        public L2PcInstance player;

        public DayOfDestinyWorld(L2PcInstance player)
        {
            this.player = player;
            monsters = new HashMap<>();
            helpers = new HashMap<>(3);
        }
    }

    public void enterInstance(L2PcInstance player)
    {
        InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if(world != null)
        {
            if(world.templateId != InstanceZoneId.EVIL_INCUBATOR.getId())
            {
                player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
                return;
            }

            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if(inst != null)
            {
                player.teleToInstance(ENTER_POINT, world.instanceId);
            }
        }
        else
        {
            world = new DayOfDestinyWorld(player);
            world.instanceId = InstanceManager.getInstance().createDynamicInstance("DayOfDestiny.xml");
            world.templateId = InstanceZoneId.EVIL_INCUBATOR.getId();
            world.status = 0;
            InstanceManager.getInstance().addWorld(world);
            world.allowed.add(player.getObjectId());
            player.teleToInstance(ENTER_POINT, world.instanceId);
        }

    }

    public void startInstance(DayOfDestinyWorld world)
    {
        switch(world.status)
        {
            case 0:
                // Телепортируем игрока в место сражения
                world.player.teleToInstance(BATTLE_POINT, world.instanceId);

                // Спауним хелперов
                QuestState st = world.player.getQuestState(getClass());
                int helper1, helper2;
                if(st != null)
                {
                    helper1 = st.getInt("1");
                    helper2 = st.getInt("2");
                    world.helpers.put(helper1, addSpawn(helper1, new Location(56325, -175536, -7952, 49820)));
                    world.helpers.put(helper2, addSpawn(helper2, new Location(56005, -175536, -7952, 49044)));
                }
                world.helpers.put(Адольф, addSpawn(Адольф, new Location(56167, -175615, -7944, 49180)));

                // Если выбрали в помощники Жрицу Эллис, то стартуем FollowTask для нее на игрока
                if(world.helpers.containsKey(ЖрецЭллис))
                {
                    world.helpers.get(ЖрецЭллис).getAI().startFollow(world.player);
                }

                // Запускаем таймер на 10 секунд
                world.status++;
                ThreadPoolManager.getInstance().scheduleGeneral(new StartNewStage(world), 10000);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                world.monsters.clear();
                world.player.sendPacket(new ExShowScreenMessage(NpcStringId.CREATURES_RESURRECTED_DEATH_WOUND_HAS_BEEN_SUMMONED, ExShowScreenMessage.TOP_CENTER, 5000));
                for(int[] spawn : SPAWNLIST_MONSTERS_1_WAVE_TYPE_1[Rnd.get(SPAWNLIST_MONSTERS_1_WAVE_TYPE_1.length)])
                {
                    L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0);
                    world.monsters.put(mob, false);
                }
                for(L2Npc helper : world.helpers.values())
                {
                    if(helper.getNpcId() == ЖрецЭллис)
                    {
                        continue;
                    }
                    Random generator = new Random();
                    Object[] monsters = world.monsters.values().toArray();
                    L2Npc randomMonster = (L2Npc) monsters[generator.nextInt(monsters.length)];
                    helper.getAttackable().attackCharacter(randomMonster);
                }
                break;
            case 7:
                QuestState qst = world.player.getQuestState(getClass());
                if(qst != null)
                {
                    world.player.sendPacket(new ExShowScreenMessage(NpcStringId.CREATURES_HAVE_STOPPED_THEIR_ATTACK_REST_AND_THEN_SPEAK_WITH_ADOLPH, ExShowScreenMessage.TOP_CENTER, 5000));
                    qst.setCond(9);
                    qst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                }
                world.status++;
                break;
            case 8: // Вторая часть инстанса
                // TODO: Спаун и выход
                break;
        }
    }

    /**
     *
     * @param mob убитый моб
     * @param world инстанс мир
     * @return все-ли мобы из списка мертвы
     */
    public boolean checkKillProgress(L2Npc mob, DayOfDestinyWorld world)
    {
        if(world.monsters.containsKey(mob))
        {
            world.monsters.put(mob, true);
        }
        for(boolean isDead : world.monsters.values())
        {
            if(!isDead)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        QuestState st = player.getQuestState(getClass());
        InstanceManager.InstanceWorld world;

        if(st == null)
        {
            return event;
        }

        switch (event)
        {

        }
        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onNpcDie(L2Npc npc, L2Character killer)
    {
        if(ArrayUtils.contains(Монстры, npc.getNpcId()) && npc.getInstanceId() == killer.getInstanceId())
        {
            InstanceManager.InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
            if(world instanceof DayOfDestinyWorld)
            {
                if(checkKillProgress(npc, (DayOfDestinyWorld) world))
                {
                    world.status++;
                    startInstance((DayOfDestinyWorld) world);
                }
            }
        }
        return super.onNpcDie(npc, killer);
    }

    /**
     * Служит для отложенного переключения стадий инстанса
     */
    private class StartNewStage extends RunnableImpl
    {
        private DayOfDestinyWorld _world;

        public StartNewStage(DayOfDestinyWorld world)
        {
            _world = world;
        }

        @Override
        public void runImpl()
        {
            startInstance(_world);
        }
    }
}

