package dwo.scripts.npc.fort;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.01.13
 * Time: 16:35
 */

public class FortWizard extends Quest
{
	// Список нпц
	private static final int[] NPCs = {
		35662, 35694, 35731, 35763, 35800, 35831, 35863, 35900, 35932, 35970, 36007, 36039, 36077, 36114, 36145, 36177,
		36215, 36253, 36290, 36322, 36360
	};
	String fnHi = "fortress_wizard001.htm";
	String fnHi2 = "fortress_wizard002.htm";
	String fnHi3 = "fortress_wizard003.htm";
	String fnHi4 = "fortress_wizard004.htm";
	String fnHi5 = "fortress_wizard005.htm";
	String fnHi6 = "fortress_wizard006.htm";
	String fnHi7 = "fortress_wizard007.htm";
	String fnHi8 = "fortress_wizard008.htm";
	String fnHi9 = "fortress_wizard009.htm";

	public FortWizard()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, -301);
	}

	public static void main(String[] args)
	{
		new FortWizard();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(!npc.isMyLord(player, false))
		{
			return fnHi5;
		}
		if(ask == -301)
		{
			if(reply == 1)
			{
				MultiSellData.getInstance().separateAndSend(613, player, npc);
				return null;
			}
			else if(reply == 2)
			{
				MultiSellData.getInstance().separateAndSend(610, player, npc);
				return null;
			}
			else if(reply == 3)
			{
				MultiSellData.getInstance().separateAndSend(609, player, npc);
				return null;
			}
			else if(reply == 4)
			{
				return fnHi2;
			}
			else if(reply == 5)
			{
				int i0 = Rnd.get(100);
				if(player.getItemsCount(9912) >= 10)
				{
					player.destroyItemByItemId(ProcessType.FORT, 9912, 10, npc, true);
					if(i0 <= 5)
					{
						int i1 = Rnd.get(100);
						if(i1 < 25)
						{
							player.addItem(ProcessType.FORT, 9931, 1, npc, true);
						}
						else if(i1 <= 50)
						{
							player.addItem(ProcessType.FORT, 9932, 1, npc, true);
						}
						else if(i1 <= 75)
						{
							player.addItem(ProcessType.FORT, 10416, 1, npc, true);
						}
						else
						{
							player.addItem(ProcessType.FORT, 10417, 1, npc, true);
						}
					}
					else if(i0 <= 15)
					{
						int i1 = Rnd.get(100);
						if(i1 <= 20)
						{
							player.addItem(ProcessType.FORT, 9917, 1, npc, true);
						}
						else if(i1 <= 40)
						{
							player.addItem(ProcessType.FORT, 9918, 1, npc, true);
						}
						else if(i1 <= 60)
						{
							player.addItem(ProcessType.FORT, 9928, 1, npc, true);
						}
						else if(i1 <= 80)
						{
							player.addItem(ProcessType.FORT, 9929, 1, npc, true);
						}
						else
						{
							player.addItem(ProcessType.FORT, 9920, 1, npc, true);
						}
					}
					else if(i0 <= 30)
					{
						int i1 = Rnd.get(100);
						if(i1 <= 12)
						{
							player.addItem(ProcessType.FORT, 9916, 1, npc, true);
						}
						else if(i1 <= 25)
						{
							player.addItem(ProcessType.FORT, 9923, 1, npc, true);
						}
						else if(i1 <= 37)
						{
							player.addItem(ProcessType.FORT, 9924, 1, npc, true);
						}
						else if(i1 <= 50)
						{
							player.addItem(ProcessType.FORT, 9925, 1, npc, true);
						}
						else if(i1 <= 62)
						{
							player.addItem(ProcessType.FORT, 9926, 1, npc, true);
						}
						else if(i1 <= 75)
						{
							player.addItem(ProcessType.FORT, 9927, 1, npc, true);
						}
						else if(i1 <= 87)
						{
							player.addItem(ProcessType.FORT, 10518, 1, npc, true);
						}
						else
						{
							player.addItem(ProcessType.FORT, 10424, 1, npc, true);
						}
					}
					else
					{
						int i1 = Rnd.get(46);
						if(i1 == 0)
						{
							player.addItem(ProcessType.FORT, 9914, 1, npc, true);
						}
						else if(i1 == 1)
						{
							player.addItem(ProcessType.FORT, 9915, 1, npc, true);
						}
						else if(i1 == 2 || i1 == 3)
						{
							player.addItem(ProcessType.FORT, 9920, 1, npc, true);
						}
						else if(i1 == 4)
						{
							player.addItem(ProcessType.FORT, 9921, 1, npc, true);
						}
						else if(i1 == 5)
						{
							player.addItem(ProcessType.FORT, 9922, 1, npc, true);
						}
						else if(i1 == 6)
						{
							player.addItem(ProcessType.FORT, 9933, 1, npc, true);
						}
						else if(i1 == 7)
						{
							player.addItem(ProcessType.FORT, 9934, 1, npc, true);
						}
						else if(i1 == 8)
						{
							player.addItem(ProcessType.FORT, 9935, 1, npc, true);
						}
						else if(i1 == 9)
						{
							player.addItem(ProcessType.FORT, 9936, 1, npc, true);
						}
						else if(i1 == 10)
						{
							player.addItem(ProcessType.FORT, 9937, 1, npc, true);
						}
						else if(i1 == 11)
						{
							player.addItem(ProcessType.FORT, 9938, 1, npc, true);
						}
						else if(i1 == 12)
						{
							player.addItem(ProcessType.FORT, 9939, 1, npc, true);
						}
						else if(i1 == 13)
						{
							player.addItem(ProcessType.FORT, 9940, 1, npc, true);
						}
						else if(i1 == 14)
						{
							player.addItem(ProcessType.FORT, 9941, 1, npc, true);
						}
						else if(i1 == 15)
						{
							player.addItem(ProcessType.FORT, 9942, 1, npc, true);
						}
						else if(i1 == 16)
						{
							player.addItem(ProcessType.FORT, 9943, 1, npc, true);
						}
						else if(i1 == 17)
						{
							player.addItem(ProcessType.FORT, 9944, 1, npc, true);
						}
						else if(i1 == 18)
						{
							player.addItem(ProcessType.FORT, 9945, 1, npc, true);
						}
						else if(i1 == 19)
						{
							player.addItem(ProcessType.FORT, 9946, 1, npc, true);
						}
						else if(i1 == 20)
						{
							player.addItem(ProcessType.FORT, 9947, 1, npc, true);
						}
						else if(i1 == 21)
						{
							player.addItem(ProcessType.FORT, 9948, 1, npc, true);
						}
						else if(i1 == 22)
						{
							player.addItem(ProcessType.FORT, 9949, 1, npc, true);
						}
						else if(i1 == 23)
						{
							player.addItem(ProcessType.FORT, 9950, 1, npc, true);
						}
						else if(i1 == 25)
						{
							player.addItem(ProcessType.FORT, 9952, 1, npc, true);
						}
						else if(i1 == 26)
						{
							player.addItem(ProcessType.FORT, 9953, 1, npc, true);
						}
						else if(i1 == 27)
						{
							player.addItem(ProcessType.FORT, 9954, 1, npc, true);
						}
						else if(i1 == 28)
						{
							player.addItem(ProcessType.FORT, 9955, 1, npc, true);
						}
						else if(i1 == 29)
						{
							player.addItem(ProcessType.FORT, 9956, 1, npc, true);
						}
						else if(i1 == 30)
						{
							player.addItem(ProcessType.FORT, 9957, 1, npc, true);
						}
						else if(i1 == 31)
						{
							player.addItem(ProcessType.FORT, 9958, 1, npc, true);
						}
						else if(i1 == 32)
						{
							player.addItem(ProcessType.FORT, 9959, 1, npc, true);
						}
						else if(i1 == 33)
						{
							player.addItem(ProcessType.FORT, 9960, 1, npc, true);
						}
						else if(i1 == 34)
						{
							player.addItem(ProcessType.FORT, 9961, 1, npc, true);
						}
						else if(i1 == 35)
						{
							player.addItem(ProcessType.FORT, 9962, 1, npc, true);
						}
						else if(i1 == 36)
						{
							player.addItem(ProcessType.FORT, 9963, 1, npc, true);
						}
						else if(i1 == 37)
						{
							player.addItem(ProcessType.FORT, 9964, 1, npc, true);
						}
						else if(i1 == 24)
						{
							player.addItem(ProcessType.FORT, 9965, 1, npc, true);
						}
						else if(i1 == 38)
						{
							player.addItem(ProcessType.FORT, 10418, 1, npc, true);
						}
						else if(i1 == 39)
						{
							player.addItem(ProcessType.FORT, 10420, 1, npc, true);
						}
						else if(i1 == 40)
						{
							player.addItem(ProcessType.FORT, 10519, 1, npc, true);
						}
						else if(i1 == 41)
						{
							player.addItem(ProcessType.FORT, 10422, 1, npc, true);
						}
						else if(i1 == 42)
						{
							player.addItem(ProcessType.FORT, 10423, 1, npc, true);
						}
						else if(i1 == 43)
						{
							player.addItem(ProcessType.FORT, 10419, 1, npc, true);
						}
						else
						{
							player.addItem(ProcessType.FORT, 10421, 1, npc, true);
						}
					}
					return fnHi3;
				}
				else
				{
					return fnHi6;
				}
			}
			else if(reply == 6)
			{
				if(player.isClanLeader())
				{
					List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableSubPledgeSkills(player.getClan());
					ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.SubPledge);
					int count = 0;

					for(L2SkillLearn s : skills)
					{
						if(SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
						{
							asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
							count++;
						}
					}

					if(count == 0)
					{
						player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
					}
					else
					{
						player.sendPacket(asl);
					}
				}
				else
				{
					return fnHi4;
				}
			}
			else if(reply == 7)
			{
				MultiSellData.getInstance().separateAndSend(627, player, npc);
				return null;
			}
			else if(reply == 10)
			{
				return player.isAwakened() ? fnHi7 : fnHi9;
			}
			else if(reply == 11) // Выполнить обмен на обычный Талисман
			{
				return fnHi8;
			}
			else if(reply == 12) // Выполнить обмен на Талисман по классам
			{
				switch(Util.getGeneralIdForAwaken(player.getClassId().getId()))
				{
					case 139:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(797, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(805, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(813, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(821, player, npc);
							return null;
						}
					case 140:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(798, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(806, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(814, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(822, player, npc);
							return null;
						}
					case 141:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(799, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(807, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(815, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(823, player, npc);
							return null;
						}
					case 142:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(800, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(808, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(816, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(824, player, npc);
							return null;
						}
					case 143:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(801, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(809, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(817, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(825, player, npc);
							return null;
						}
					case 144:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(802, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(810, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(818, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(826, player, npc);
							return null;
						}
					case 145:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(803, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(811, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(819, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(827, player, npc);
							return null;
						}
					case 146:
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(804, player, npc);
							return null;
						}
						else if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(812, player, npc);
							return null;
						}
						else if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(820, player, npc);
							return null;
						}
						else if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(828, player, npc);
							return null;
						}
				}
				return null;
			}
			else if(reply == 13) // Активный
			{
				if(player.getLevel() >= 85 && player.getLevel() < 90)
				{
					MultiSellData.getInstance().separateAndSend(793, player, npc);
					return null;
				}
				else if(player.getLevel() >= 90 && player.getLevel() < 95)
				{
					MultiSellData.getInstance().separateAndSend(794, player, npc);
					return null;
				}
				else if(player.getLevel() >= 95 && player.getLevel() < 99)
				{
					MultiSellData.getInstance().separateAndSend(795, player, npc);
					return null;
				}
				else if(player.getLevel() >= 99)
				{
					MultiSellData.getInstance().separateAndSend(796, player, npc);
					return null;
				}
			}
			else if(reply == 14) // Пассивный
			{
				if(player.getLevel() >= 85 && player.getLevel() < 90)
				{
					MultiSellData.getInstance().separateAndSend(789, player, npc);
					return null;
				}
				else if(player.getLevel() >= 90 && player.getLevel() < 95)
				{
					MultiSellData.getInstance().separateAndSend(790, player, npc);
					return null;
				}
				else if(player.getLevel() >= 95 && player.getLevel() < 99)
				{
					MultiSellData.getInstance().separateAndSend(791, player, npc);
					return null;
				}
				else if(player.getLevel() >= 99)
				{
					MultiSellData.getInstance().separateAndSend(792, player, npc);
					return null;
				}
			}
			else
			{
				return fnHi4;
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.isMyLord(player, false) ? fnHi : fnHi5;
	}
}