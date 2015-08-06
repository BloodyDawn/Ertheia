package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

/**
 * User: Bacek
 * Date: 22.03.13
 * Time: 16:23
 */
public class ChaosFestivalManager extends Quest
{
	private static final int GRANKAIN_LUMIERE = 33685;

	/*
			// grankain_lumiere016.htm	Повелитель любит общаться со славными воинами. И я тоже, но сейчас не в настроении. <br1>
	 */
	public ChaosFestivalManager()
	{
		addAskId(GRANKAIN_LUMIERE, -1);
		addAskId(GRANKAIN_LUMIERE, -2);
		addAskId(GRANKAIN_LUMIERE, -3);
		addAskId(GRANKAIN_LUMIERE, -4);
		addAskId(GRANKAIN_LUMIERE, -5);
		addAskId(GRANKAIN_LUMIERE, 850);
	}

	public static void main(String[] args)
	{
		new ChaosFestivalManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -1:
				if(reply == 1)   // Я здесь, чтобы получить звание лучшего воина.
				{
					// grankain_lumiere004.htm     Кого я вижу! Получивший признание хозяина Лучший Воин, <?talker?> - это Вы.

					// grankain_lumiere005.htm      -- не побидителям
				}
				break;
			case -2:
				if(reply == 1)   // Какой клан в этот раз предоставил лучшего воина?
				{
					// grankain_lumiere007.htm    --    <?winner_player?> вашими стараниями <?winner_pledge?> клан признан Лучшим Кланом этой недели.

					// grankain_lumiere015.htm    -- 	нету победителя
				}
				break;
			case -3:
				if(reply == 1)   // Посмотреть список наград
				{
					MultiSellData.getInstance().separateAndSend(865, player, npc); // TODO другой мультиселл
					break;
				}
				if(reply == 3) // Получить  питомца (необходимо 4 Таинственных Знака)
				{
					// grankain_lumiere013.htm   Вам не хватает Таинственных Знаков.

					// Рамдом из 3  ( 34906 34905 34907 )
					// grankain_lumiere014.htm  Если воспитаете его с любовью, Ваша забота будет вознаграждена.
				}
				break;
			case -4:
				if(reply == 1)   // Получить награду за "Лучший Воин"
				{
					// grankain_lumiere009.htm   Да пребудет с Вами слава повелителя...
					// grankain_lumiere010.htm   Вы уже получили подарки повелителя.</br1> Приходите в следующий раз, когда опять станете чемпионом.

					/*
							11. Лучший из воинов получает у Таинственного Лакея следующие награды:
								- Пояс Полномочия Правителей;
								- 5000 репутации клана;
								- 50,000 личной репутации;
								- Сообщение о присуждении титула Лучшего из воинов.
								- Члены клана, к которому принадлежит Лучший из воинов, в течение месяца при прохождении вблизи от Таинственного Лакея получают усиливающее умение Благословение Правителя (длительность 30 мин);

					 */
				}
				break;
			case -5:
				if(reply == 1)   // Я хочу узнать о победителях этой недели.
				{
					//  За этот период  лучшие претенденты собрали <?point?>Таинственных Знаков.
					NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
					html.setFile(player.getLang(), "grankain_lumiere015.htm");
					html.replace("<\\?point\\?>", String.valueOf(0));    // TODO Вывод макс очков
					return html.toString();
				}
				break;
			case 850:
				if(reply == 1)   // Просмотреть список наград.
				{
					MultiSellData.getInstance().separateAndSend(865, player, npc);
					break;
				}
				break;
		}

		return null;
	}
}
