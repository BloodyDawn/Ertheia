package dwo.scripts.services;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowUsm;

/**
 * L2GOD Team
 * User: ANZO,Bacek
 * Date: 04.07.11
 * Time: 10:47
 */

/*
	При разговоре с Пантеоном после последнего окна с туториалом
	1600026	u,Для быстрого роста Вы должны выполнить задание.\0

  TODO: Команды выполняются так <a action="bypass -h TE">Закрыть окно</a></body></html>
*/
public class Tutorial extends Quest
{
	public static void main(String[] args)
	{
		new Tutorial();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(Config.DISABLE_TUTORIAL)
		{
			return null;
		}

		if(player.isGM() && player.isDebug())
		{
			player.sendMessage("Tutorial Event: " + event);
		}

		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}
		if(event.equalsIgnoreCase("CE1") || event.equalsIgnoreCase("CE2") || event.equalsIgnoreCase("CE8") || event.equalsIgnoreCase("CE10") || event.equalsIgnoreCase("CE12") || event.equalsIgnoreCase("CE40") || event.equalsIgnoreCase("CE30") || event.equalsIgnoreCase("CE45"))
		{
		}
		else if(event.startsWith("UC"))
		{
			// При входе в мир
			if(player.getLevel() < 6)
			{
				int uc = st.getInt("ucMemo");
				if(uc == 0)
				{
					player.showUsmVideo(ExShowUsm.GD1_INTRO);
					st.set("ucMemo", "1");
				}
			}
		}
		else if(event.startsWith("TE") || event.startsWith("tutorial_close_0"))
		{
			// При нажатиии "Выйти из туториала"
			st.closeTutorialHtml();
			return null;
		}
		else if(event.startsWith("QM"))
		{
			int MarkId = Integer.valueOf(event.substring(2));
			switch(MarkId)
			{
				case 101:
					st.showTutorialHTML("tutorial_q10341.htm");
					break;
				case 102:
					st.showTutorialHTML("tutorial_q10342.htm");
					break;
				case 103:
					st.showTutorialHTML("tutorial_q10343.htm");
					break;
				case 104:
					st.showTutorialHTML("tutorial_q10344.htm");
					break;
				case 105:
					st.showTutorialHTML("tutorial_q10345.htm");
					break;
				case 106:
					st.showTutorialHTML("tutorial_q10346.htm");
					break;
			}
		}
		return null;
	}
}