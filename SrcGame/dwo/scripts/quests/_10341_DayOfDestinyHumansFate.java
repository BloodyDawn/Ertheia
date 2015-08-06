package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.TutorialShowQuestionMark;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Util;
import dwo.scripts.instances.AQ_EvilIncubator;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * @author ANZO
 * @correct: GenCloud
 * Date: 20.03.12
 * Time: 21:37
 *
 * TODO: Скилы остальных двои хелперов
 * TODO: NpcSay перенести в скрипт с AutoChat.xml
 * TODO: 9-12 пункты
 */

public class _10341_DayOfDestinyHumansFate extends Quest
{
    // Квестовые персонажи
    private static final int Орвен = 30857;
    private static final int ИнтендантАдена = 33407;
    private static final int[] Трупы = {33166, 33167, 33168, 33169};
    private static final int ЧленАвангарда = 33165;

    // Квестовые предметы
    private static final int РеликвияВоиновАдена = 17748;
    private static final int КрикСудьбыКольцо = 17484; // Их штук 30 в клиенте, какой для чего ХЗ

    public _10341_DayOfDestinyHumansFate()
    {
        addStartNpc(Орвен);
        addFirstTalkId(AQ_EvilIncubator.Адольф, AQ_EvilIncubator.ЖрецЭллис, AQ_EvilIncubator.СержантБартон, AQ_EvilIncubator.СнайперХаюк, AQ_EvilIncubator.ВолшебникЭллия);
        addTalkId(AQ_EvilIncubator.Адольф, AQ_EvilIncubator.ЖрецЭллис, AQ_EvilIncubator.СержантБартон, AQ_EvilIncubator.СнайперХаюк, AQ_EvilIncubator.ВолшебникЭллия);
        addTalkId(Орвен, ИнтендантАдена, ЧленАвангарда);
        addTalkId(Трупы);
        addEventId(HookType.ON_LEVEL_INCREASE);
        addEventId(HookType.ON_ENTER_WORLD);
        questItemIds = new int[]{РеликвияВоиновАдена, КрикСудьбыКольцо};
    }

    public static void main(String[] args)
    {
        new _10341_DayOfDestinyHumansFate();
    }

    public void Cast(L2Npc npc, L2Character target, int skillId, int level)
    {
        target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
        target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
    }

    @Override
    public int getQuestId()
    {
        return 10341;
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

        if(player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.Human) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
        {
            return null;
        }
        if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
        {
            return null;
        }

        int Class;
        int prevClass;

        switch(event)
        {
            case "30857-07.htm":
                st.startQuest();
                break;
            case "33407-01.htm":
                st.setCond(2);
                st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                break;
            case "corpse_search":
                if(st.getBool(String.valueOf(npc.getNpcId())))
                {
                    event = "corpse-02.htm";
                }
                else
                {
                    st.set(String.valueOf(npc.getNpcId()), "1");
                    st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
                    st.giveItem(РеликвияВоиновАдена);
                    if(st.getQuestItemsCount(РеликвияВоиновАдена) == 4)
                    {
                        st.setCond(3);
                        st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                    }
                    event = "corpse-01.htm";
                }
                break;
            case "33407-04.htm":
                st.setCond(4);
                st.addRadar(172333, 31219, -3696);
                st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                st.takeItems(РеликвияВоиновАдена, 4);
                break;
            case "33165-01.htm":
                st.setCond(5);
                AQ_EvilIncubator.getInstance().enterInstance(player);
                break;
            case "33170-01.htm":
                st.setCond(6);
                st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                break;
            case "select_helper":
                if(!st.getBool("1"))
                {
                    st.set("1", String.valueOf(npc.getNpcId()));
                    event = null;
                    npc.getLocationController().delete();
                }
                else if(!st.getBool("2"))
                {
                    st.set("2", String.valueOf(npc.getNpcId()));
                    event = null;
                    npc.getLocationController().delete();
                }
                if(st.getBool("1") || st.getBool("2"))
                {
                    Instance inst = InstanceManager.getInstance().getInstance(player.getInstanceId());
                    inst.getNpcs().stream().filter(mob -> ArrayUtils.contains(AQ_EvilIncubator.Помощники, mob.getNpcId())).forEach(mob -> mob.getLocationController().delete());
                    st.setCond(7);
                    st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                }
                break;
            case "start_instance":
                npc.getLocationController().delete();
                world = InstanceManager.getInstance().getPlayerWorld(player);
                ((AQ_EvilIncubator.DayOfDestinyWorld) world).player = player;
                AQ_EvilIncubator.getInstance().startInstance((AQ_EvilIncubator.DayOfDestinyWorld) world);
                st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                event = null;
                break;
            case "start_second_part":
                player.sendPacket(new ExShowScreenMessage(NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF, ExShowScreenMessage.TOP_CENTER, 10000));
                world = InstanceManager.getInstance().getPlayerWorld(player);
                AQ_EvilIncubator.getInstance().startInstance((AQ_EvilIncubator.DayOfDestinyWorld) world);
                event = null;
                break;
            case "33170-04.htm":
                st.giveItem(Util.getThirdClassForId(player.getActiveClassId()) + 17396);
                st.setCond(10);
                st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
                break;
            case "red":
                st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
                st.addExpAndSp(2050000, 0);
                st.giveAdena(5000000, true);
                st.giveItem(6622);
                st.giveItem(9570);

                Class = Util.getThirdClassForId(player.getClassId().getId());
                prevClass = player.getClassId().getId();
                player.setClassId(Class);
                if(!player.isSubClassActive() && player.getBaseClassId() == prevClass)
                {
                    player.setBaseClassId(Class);
                }

                Cast(npc, player, 4339, 1);

                player.broadcastUserInfo();
                st.exitQuest(QuestType.ONE_TIME);
                player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
                return "30857-11.htm";
            case "blue":
                st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
                st.addExpAndSp(2050000, 0);
                st.giveAdena(5000000, true);
                st.giveItem(6622);
                st.giveItem(9571);

                Class = Util.getThirdClassForId(player.getClassId().getId());
                prevClass = player.getClassId().getId();
                player.setClassId(Class);
                if(!player.isSubClassActive() && player.getBaseClassId() == prevClass)
                {
                    player.setBaseClassId(Class);
                }

                Cast(npc, player, 4339, 1);

                player.broadcastUserInfo();
                st.exitQuest(QuestType.ONE_TIME);
                player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
                return "30857-11.htm";
            case "green":
                st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
                st.addExpAndSp(2050000, 0);
                st.giveAdena(5000000, true);
                st.giveItem(6622);
                st.giveItem(9572);

                Class = Util.getThirdClassForId(player.getClassId().getId());
                prevClass = player.getClassId().getId();
                player.setClassId(Class);
                if(!player.isSubClassActive() && player.getBaseClassId() == prevClass)
                {
                    player.setBaseClassId(Class);
                }

                Cast(npc, player, 4339, 1);

                player.broadcastUserInfo();

                player.getVariablesController().set("_3rd_profession_ci" + player.getClassIndex(), true);
                st.exitQuest(QuestType.ONE_TIME);
                return "30857-11.htm";
        }
        return event;
    }


    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        QuestState st = player.getQuestState(_10341_DayOfDestinyHumansFate.class);
        if(st == null)
        {
            return getNoQuestMsg(player);
        }

        // Следующие НПЦ во время боя не разговаривают
        if(npc.getNpcId() == AQ_EvilIncubator.Адольф)
        {
            switch(st.getCond())
            {
                case 7:
                    return "33170-02.htm";
                case 8:
                case 11:
                    return "33170-battle.htm";
                default:
                    return "33170.htm";

            }
        }
        if(npc.getNpcId() == AQ_EvilIncubator.СержантБартон)
        {
            switch(st.getCond())
            {
                case 8:
                case 11:
                    return "33172-battle.htm";
                default:
                    return "33172.htm";
            }
        }
        if(npc.getNpcId() == AQ_EvilIncubator.ЖрецЭллис)
        {
            switch(st.getCond())
            {
                case 8:
                case 11:
                    return "33171-battle.htm";
                default:
                    return "33171.htm";
            }
        }
        if(npc.getNpcId() == AQ_EvilIncubator.СнайперХаюк)
        {
            switch(st.getCond())
            {
                case 8:
                case 11:
                    return "33173-battle.htm";
                default:
                    return "33173.htm";
            }
        }
        if(npc.getNpcId() == AQ_EvilIncubator.ВолшебникЭллия)
        {
            switch(st.getCond())
            {
                case 8:
                case 11:
                    return "33174-battle.htm";
                default:
                    return "33174.htm";
            }
        }
        return super.onFirstTalk(npc, player);
    }

    @Override
    public String onTalk(L2Npc npc, QuestState st)
    {
        L2PcInstance player = st.getPlayer();

        if(npc.getNpcId() == Орвен)
        {
            switch(st.getState())
            {
                case COMPLETED:
                    if(player.getVariablesController().get("_3rd_profession_ci" + player.getClassIndex(), Boolean.class, false))
                    {
                        return "30857-02.htm";
                    }
                    else
                    {
                        if(player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.Human) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
                        {
                            st.exitQuest(QuestType.REPEATABLE);
                            return "30857-02.htm";
                        }
                        else if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
                        {
                            st.exitQuest(QuestType.REPEATABLE);
                            return "30857-10.htm";
                        }
                        else
                        {
                            return "30857-01.htm";
                        }
                    }
                case CREATED:
                    if(player.getVariablesController().get("_3rd_profession_ci" + player.getClassIndex(), Boolean.class, false) || player.getLevel() < 76 || !Util.hasChildClassWithRace(player.getClassId(), Race.Human) || player.getClassId().level() >= ClassLevel.THIRD.ordinal())
                    {
                        st.exitQuest(QuestType.REPEATABLE);
                        return "30857-02.htm";
                    }
                    else if(player.getClassId().level() < ClassLevel.SECOND.ordinal())
                    {
                        st.exitQuest(QuestType.REPEATABLE);
                        return "30857-10.htm";
                    }
                    else
                    {
                        return "30857-01.htm";
                    }
                case STARTED:
                    switch(st.getCond())
                    {
                        case 1:
                            return "30857-08.htm";
                        case 13:
                            return "30857-09.htm";
                    }
                    break;
            }
        }
        else if(npc.getNpcId() == ИнтендантАдена)
        {
            if(st.isStarted())
            {
                switch(st.getCond())
                {
                    case 1:
                        return "33407-00.htm";
                    case 2:
                        return "33407-02.htm";
                    case 3:
                        return "33407-03.htm";
                    case 4:
                        return "33407-04.htm";
                }
            }
        }
        else if(ArrayUtils.contains(Трупы, npc.getNpcId()))
        {
            if(st.isStarted())
            {
                if(st.getCond() == 2)
                {
                    return "corpse.htm";
                }
            }
        }
        else if(npc.getNpcId() == ЧленАвангарда)
        {
            if(st.isStarted())
            {
                if(st.getCond() >= 4 && st.getCond() < 13)
                {
                    return "33165-00.htm";
                }
            }
        }
        if(npc.getNpcId() == AQ_EvilIncubator.Адольф)
        {
            switch(st.getCond())
            {
                case 4:
                case 5:
                    return "33170-00.htm";
                case 6:
                    return "33170-01.htm";
                case 7:
                    return "33170-02.htm";
                case 9:
                    return "33170-03.htm";
                case 10:
                    return "33170-05.htm";
            }
        }
        else if(npc.getNpcId() == AQ_EvilIncubator.СержантБартон)
        {
            if(st.getCond() == 5)
            {
                return "33172-no.htm";
            }
            else if(st.getCond() == 6)
            {
                return "33172-00.htm";
            }
        }
        else if(npc.getNpcId() == AQ_EvilIncubator.ЖрецЭллис)
        {
            if(st.getCond() == 5)
            {
                return "33171-no.htm";
            }
            else if(st.getCond() == 6)
            {
                return "33171-00.htm";
            }
        }
        else if(npc.getNpcId() == AQ_EvilIncubator.СнайперХаюк)
        {
            if(st.getCond() == 5)
            {
                return "33173-no.htm";
            }
            else if(st.getCond() == 6)
            {
                return "33173-00.htm";
            }
        }
        else if(npc.getNpcId() == AQ_EvilIncubator.ВолшебникЭллия)
        {
            if(st.getCond() == 5)
            {
                return "33174-no.htm";
            }
            else if(st.getCond() == 6)
            {
                return "33174-00.htm";
            }
        }
        return null;
    }

    @Override
    public void onLevelIncreased(L2PcInstance player)
    {
        if(player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.Human) && player.getClassId().level() < ClassLevel.THIRD.ordinal())
        {
            QuestState st = player.getQuestState(getClass());
            if(st == null)
            {
                player.sendPacket(new TutorialShowQuestionMark(101));
            }
        }
    }

    @Override
    public void onEnterWorld(L2PcInstance player)
    {
        if(player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.Human) && player.getClassId().level() < ClassLevel.THIRD.ordinal())
        {
            QuestState st = player.getQuestState(getClass());
            if(st == null)
            {
                player.sendPacket(new TutorialShowQuestionMark(101));
            }
        }
    }

    @Override
    public boolean canBeStarted(L2PcInstance player)
    {
        return player.getLevel() >= 76 && Util.hasChildClassWithRace(player.getClassId(), Race.Human) && !player.isSubClassActive() && player.getClassId().level() < ClassLevel.THIRD.ordinal();
    }
}