package dwo.scripts.npc.town;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;
import dwo.scripts.quests._00136_MoreThanMeetsTheEye;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 01.12.12
 * Time: 23:31
 */

public class TransformManager extends Quest
{
	public static final int master_transformation = 32323;

	public TransformManager()
	{
		addAskId(master_transformation, -299);
	}

	/**
	 * This displays Transformation Skill List to the player.
	 *
	 * @param player the active character.
	 */
	public static void showTransformSkillList(L2PcInstance player)
	{
		List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableTransformSkills(player);
		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Transform);
		int counts = 0;

		for(L2SkillLearn s : skills)
		{
			if(SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
			{
				counts++;
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
			}
		}

		if(counts == 0)
		{
			int minLevel = SkillTreesData.getInstance().getMinLevelForNewSkill(player, SkillTreesData.getInstance().getTransformSkillTree());
			if(minLevel > 0)
			{
				//No more skills to learn, come back when you level.
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(minLevel));
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
	}

	/**
	 * @param player проверяемый игрок
	 * @return {@code true} если персонаж соответствует условиям для получения трансформации
	 */
	public static boolean canTransform(L2PcInstance player)
	{
		if(Config.ALLOW_TRANSFORM_WITHOUT_QUEST)
		{
			return true;
		}
		QuestState st = player.getQuestState(_00136_MoreThanMeetsTheEye.class);
		return st != null && st.isCompleted();
	}

	public static void main(String[] args)
	{
		new TransformManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(reply)
		{
			// Научиться Перевоплощению
			case 0:
				if(canTransform(player))
				{
					showTransformSkillList(player);
					return null;
				}
				else
				{
					return "master_transformation003.htm";
				}
				// Купить вещи для Перевоплощений
			case 1:
				if(canTransform(player))
				{
					MultiSellData.getInstance().separateAndSend(621, player, npc);
					return null;
				}
				else
				{
					return "master_transformation004.htm";
				}
		}
		return "master_transformation001.htm";
	}
}
