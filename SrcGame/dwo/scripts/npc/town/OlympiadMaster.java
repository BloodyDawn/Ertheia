package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.12.12
 * Time: 15:56
 */

public class OlympiadMaster extends Quest
{
	private static final int NPC = 36402;

	public OlympiadMaster()
	{
		addAskId(NPC, -301);
		addFirstTalkId(NPC);
	}

	public static void main(String[] args)
	{
		new OlympiadMaster();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -301)
		{
			if(reply < 1 || reply > 9)
			{
				return null;
			}

			if(player.getOlympiadController().getRemainingGameBuffs() <= 0)
			{
				return "Olympiad_master003.htm";
			}

			L2Skill skill = SkillTable.getInstance().getInfo(14737 + reply, 1);
			npc.setTarget(player);
			if(skill != null)
			{
				player.getOlympiadController().consumeGameBuff();
				npc.broadcastPacket(new MagicSkillUse(npc, player, skill.getId(), skill.getLevel(), 0, 0));
				skill.getEffects(player, player);
				if(!player.getPets().isEmpty())
				{
					for(L2Summon pet : player.getPets())
					{
						npc.broadcastPacket(new MagicSkillUse(npc, pet, skill.getId(), skill.getLevel(), 0, 0));
						skill.getEffects(pet, pet);
					}
				}
			}
			if(player.getOlympiadController().hasGameBuffs())
			{
				return "Olympiad_master002.htm";
			}
			else
			{
				npc.getLocationController().delete();
				return "Olympiad_master003.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(!player.getOlympiadController().hasGameBuffs())
		{
			return "Olympiad_master003.htm";
		}
		return "Olympiad_master001.htm";
	}
}