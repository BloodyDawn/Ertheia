package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowUsm;

/**
 * User: GenCloud
 * Date: 23.01.2015
 * Team: La2Era Team
 */
public class _10732_ForeignLand extends Quest
{
    private static final int КоролеваНавари = 33931;
    private static final int Терениус = 33932;

    private static final String TUTORIAL_LINK =  "..\\L2Text\\QT_001_Radar_01.htm";

    public _10732_ForeignLand()
    {
        addStartNpc(КоролеваНавари);
        addTalkId(КоролеваНавари, Терениус);
    }

    public static void main(String[] args)
    {
        new _10732_ForeignLand();
    }

    @Override
    public int getQuestId()
    {
        return 10732;
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        QuestState qs = player.getQuestState(_10732_ForeignLand.class);
        String htmltext = null;

        if (qs == null)
        {
            return null;
        }

        switch (event)
        {
            case "33931-03.htm":
            {
                qs.startQuest();
                player.sendPacket(new ExShowUsm(ExShowUsm.ARTEAS_FIRST_QUEST));
                htmltext = event;
                break;
            }
            case "33932-02.htm":
            {
                player.sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, TUTORIAL_LINK));
                qs.giveAdena(3000, true);
                qs.addExpAndSp(75, 2);
                qs.exitQuest(QuestType.ONE_TIME);
                break;
            }
            case "33931-02.htm":
            {
                htmltext = event;
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        QuestState st = player.getQuestState(_10732_ForeignLand.class);
        String htmltext = getNoQuestMsg(player);

        switch (npc.getNpcId())
        {
            case КоролеваНавари:
            {
                if (st.isCreated())
                {
                    htmltext = "33931-01.htm";
                }
                else if (st.isStarted())
                {
                    htmltext = "33931-04.htm";
                }
                else if (st.isCompleted())
                {
                    htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
                }
                break;
            }
            case Терениус:
            {
                if (st.isStarted())
                {
                    htmltext = "33932-01.htm";
                }
                else if (st.isCompleted())
                {
                    htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public boolean canBeStarted(L2PcInstance player)
    {
        return player.getLevel() < 20;
    }
}
