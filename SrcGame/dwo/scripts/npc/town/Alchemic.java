package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;

import java.util.List;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public final class Alchemic extends Quest
{
    private static final int ZEPHYRA = 33978;
    private static final String TUTORIAL_LINK = "..\\L2text\\QT_026_alchemy_01.htm";

    private Alchemic()
    {
        addFirstTalkId(ZEPHYRA);
        addLearnSkillId(ZEPHYRA);
        addAskId(ZEPHYRA, 33978);
    }

    public static void main(String[] args)
    {
        new Alchemic();
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        return "33978.htm";
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        if(ask == 33978)
        {
            switch (reply)
            {
                case 1:
                {
                    if (player.getRace() == Race.Ertheia )
                    {
                        player.sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, TUTORIAL_LINK));
                    }
                    break;
                }
            }
        }
        return super.onAsk(player, npc, ask, reply);
    }

    @Override
    public String onLearnSkill(L2Npc npc, L2PcInstance player)
    {
        List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableAlchemySkills(player);
        ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Alchemy);

        int count = 0;

        for(L2SkillLearn s : skills)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());

            if(sk == null)
            {
                continue;
            }

            count++;
            asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 1);
        }

        if(count == 0)
        {
            int minLevel = SkillTreesData.getInstance().getMinLevelForNewSkill(player, SkillTreesData.getInstance().getAlchemySkillTree());

            if(minLevel > 0)
            {
                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
                sm.addNumber(minLevel);
                player.sendPacket(sm);
            }
            else
            {
                player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
            }
        }
        else
        {
            player.sendPacket(asl);
        }
        return null;
    }
}