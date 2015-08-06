package dwo.gameserver.model.actor.instance;

import dwo.config.events.ConfigEventKOTH;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.events.KOTH.KOTHEvent;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

public class L2KOTHEventNpcInstance extends L2Npc
{
	private static final String htmlPath = "mods/KOTHEvent/";

	public L2KOTHEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		KOTHEvent.onBypass(command, playerInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if(player == null)
		{
			return;
		}

		if(KOTHEvent.isParticipating())
		{
			boolean isParticipant = KOTHEvent.isPlayerParticipant(player.getObjectId());
			String htmContent;

			htmContent = !isParticipant ? HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Participation.htm") : HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "RemoveParticipation.htm");

			if(htmContent != null)
			{
				int[] teamsPlayerCounts = KOTHEvent.getTeamsPlayerCounts();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", ConfigEventKOTH.KOTH_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team2name%", ConfigEventKOTH.KOTH_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
				if(!isParticipant)
				{
					npcHtmlMessage.replace("%fee%", KOTHEvent.getParticipationFee());
				}

				player.sendPacket(npcHtmlMessage);
			}
		}
		else if(KOTHEvent.isStarting() || KOTHEvent.isStarted())
		{
			String htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Status.htm");

			if(htmContent != null)
			{
				int[] teamsPlayerCounts = KOTHEvent.getTeamsPlayerCounts();
				long[] teamsPointsCounts = KOTHEvent.getTeamsPoints();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%team1name%", ConfigEventKOTH.KOTH_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
				npcHtmlMessage.replace("%team2name%", ConfigEventKOTH.KOTH_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1]));
				player.sendPacket(npcHtmlMessage);
			}
		}

		player.sendActionFailed();
	}
}