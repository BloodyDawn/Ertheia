package dwo.scripts.npc.town;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.olympiad.CompetitionType;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.util.arrays.L2FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.12.12
 * Time: 14:02
 */

public class OlympiadManagerNpc extends Quest
{
	private static final int NPC = 31688;

	public OlympiadManagerNpc()
	{
		addFirstTalkId(NPC);
		addAskId(NPC, -50);
		addAskId(NPC, -51);
		addAskId(NPC, -52);
		addAskId(NPC, -53);
		addAskId(NPC, -54);
		addAskId(NPC, -55);
		addAskId(NPC, -58);
		addAskId(NPC, -70);
		addAskId(NPC, -71);
		addAskId(NPC, -110);
	}

	public static void main(String[] args)
	{
		new OlympiadManagerNpc();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -50:
				return player.getLevel() >= 85 && player.isNoble() && player.isAwakened() ? "olympiad_operator001.htm" : "olympiad_operator002.htm";
			case -51: // Подать заявку
				if(player.isSubClassActive())
				{
					return "olympiad_operator010c.htm";
				}

				String content;
				if(Config.OLY_IGNORE_WEEKLY_COMPTYPE)
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/olympiad_operator010a.htm");
					content = content.replace("<?olympiad_round?>", String.valueOf(Olympiad.getInstance().getCurrentCycle()));
					content = content.replace("<?olympiad_week?>", String.valueOf(Olympiad.getInstance().getOlympiadWeek()));
					content = content.replace("<?olympiad_participant?>", String.valueOf(OlympiadManager.getInstance().getRegisteredNonClassBased().size()));
					return content;
				}
				switch(Olympiad.getInstance().getOlympiadWeek())
				{
					case 1:
					case 2:
					case 3:
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/olympiad_operator010a.htm");
						content = content.replace("<?olympiad_round?>", String.valueOf(Olympiad.getInstance().getCurrentCycle()));
						content = content.replace("<?olympiad_week?>", String.valueOf(Olympiad.getInstance().getOlympiadWeek()));
						content = content.replace("<?olympiad_participant?>", String.valueOf(OlympiadManager.getInstance().getRegisteredNonClassBased().size()));
						return content;
					case 4:
					case 5:
						int count = 0;
						List<Integer> partList = OlympiadManager.getInstance().getRegisteredClassBased().get(player.getBaseClassId());
						if(partList != null)
						{
							count = partList.size();
						}
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/olympiad_operator010b.htm");
						content = content.replace("<?olympiad_round?>", String.valueOf(Olympiad.getInstance().getCurrentCycle()));
						content = content.replace("<?olympiad_week?>", String.valueOf(Olympiad.getInstance().getOlympiadWeek()));
						content = content.replace("<?olympiad_participant?>", String.valueOf(count));
						return content;
				}
			case -52:
				switch(reply)
				{
					case 0: // Назад
						return "olympiad_operator001.htm";
					case 4: // Посмотреть рейтинг прошедших Олимпиад
						return "olympiad_operator010g.htm";
				}
				break;
			case -53: // Сражение 1 на 1 общее
				if(reply == 0)
				{
					return "olympiad_operator001.htm";
				}
				if(reply == 1)
				{
					if(player.isSubClassActive())
					{
						return "olympiad_operator010c.htm";
					}
					else
					{
						if(player.getLevel() >= 85 && player.isNoble() && player.isAwakened())
						{
							// Выдаем очки если игрока нету в базе.
							Olympiad.getInstance().generateNobleStats(player);
							// Проверяем очки
							int passes = Olympiad.getInstance().getNoblePoints(player.getObjectId());
							if(passes > 0)
							{
								if(OlympiadManager.getInstance().isRegistered(player))
								{
									return "olympiad_operator010n.htm";
								}
								else
								{
									OlympiadManager.getInstance().registerNoble(player, CompetitionType.NON_CLASSED);
									return null;
								}
							}
							else
							{
								return "olympiad_operator010i.htm";
							}
						}
						else
						{
							return "olympiad_operator010j.htm";
						}
					}
				}
				break;
			case -54: // Сражение 1 на 1 по классам
				if(reply == 0)
				{
					return "olympiad_operator001.htm";
				}
				if(reply == 1)
				{
					if(player.isSubClassActive())
					{
						return "olympiad_operator010c.htm";
					}
					else
					{
						if(player.getLevel() >= 85 && player.isNoble() && player.isAwakened())
						{
							// Выдаем очки если игрока нету в базе.
							Olympiad.getInstance().generateNobleStats(player);
							// Проверяем очки
							int passes = Olympiad.getInstance().getNoblePoints(player.getObjectId());
							if(passes > 0)
							{
								if(OlympiadManager.getInstance().isRegistered(player))
								{
									return "olympiad_operator010n.htm";
								}
								else
								{
									OlympiadManager.getInstance().registerNoble(player, CompetitionType.CLASSED);
									return null;
								}
							}
							else
							{
								return "olympiad_operator010i.htm";
							}
						}
						else
						{
							return "olympiad_operator010j.htm";
						}
					}
				}
				break;
			case -55: // Получить награду
				return "olympiad_operator030.htm";
			case -58: // Отмена участия игрока в Олимпиаде
				OlympiadManager.getInstance().unRegisterNoble(player);
				return null;
			case -70: // Получить награду - Опции
				switch(reply)
				{
					case 0: // Назад
						return "olympiad_operator001.htm";
					case 1: // Получить расчет
						int passes = Olympiad.getInstance().getNoblessePasses(player, false);
						if(passes == 0)
						{
							return "olympiad_operator031a.htm";
						}
						else if(passes < 20)
						{
							return player.getOlympiadController().isHero() ? "olympiad_operator031.htm" : "olympiad_operator031a.htm";
						}
						else
						{
							return "olympiad_operator031.htm";
						}
					case 699: // Мультиселлы
					case 712:
						MultiSellData.getInstance().separateAndSend(reply, player, npc);
						return null;
				}
				break;
			case -71: // Получить расчет
				if(reply == 0)
				{
					return "olympiad_operator030.htm";
				}
				if(reply == 1)
				{
					int passes = Olympiad.getInstance().getNoblessePasses(player, true);
					if(passes > 0)
					{
						L2ItemInstance item = player.addItem(ProcessType.OLYMPIAD, Config.ALT_OLY_COMP_RITEM, passes, npc, true);

						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(item);
						player.sendPacket(iu);
					}
					break;
				}
				break;
			case -110: // Просмотр рейтинга по выбранной профе (где ID профы = reply)
				if(reply >= 148)
				{
					L2FastList<String> names = Olympiad.getInstance().getClassLeaderBoard(reply);
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/olympiad_operator_rank_class.htm");
					int index = 1;
					for(String name : names)
					{
						content = content.replace("<?Rank" + index + "?>", String.valueOf(index));
						content = content.replace("<?Name" + index + "?>", name);
						index++;
						if(index == 15)
						{
							break;
						}
					}
					// Если таблица получилась меньше 15, то заполянем пустотой оставшиеся маркеры
					for(; index <= 15; index++)
					{
						content = content.replace("<?Rank" + index + "?>", "");
						content = content.replace("<?Name" + index + "?>", "");
					}
					return content;
				}
				break;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getItemsCount(13560) > 0 || player.getItemsCount(13561) > 0 || player.getItemsCount(13562) > 0 || player.getItemsCount(13563) > 0 || player.getItemsCount(13564) > 0 || player.getItemsCount(13565) > 0 || player.getItemsCount(13566) > 0 || player.getItemsCount(13567) > 0 || player.getItemsCount(13568) > 0)
		{
			return "flagman.htm";
		}
		else
		{
			return player.getLevel() >= 85 && player.isNoble() && player.isAwakened() ? "olympiad_operator001.htm" : "olympiad_operator002.htm";
		}
	}
}