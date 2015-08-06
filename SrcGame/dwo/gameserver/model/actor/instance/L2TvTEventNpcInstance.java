package dwo.gameserver.model.actor.instance;

import dwo.config.events.ConfigEventTvT;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.events.TvT.TvTEvent;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

public class L2TvTEventNpcInstance extends L2Npc
{
	private static final String htmlPath = "mods/TvTEvent/";

	public L2TvTEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		TvTEvent.onBypass(command, playerInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance playerInstance, int val)
	{
		if(playerInstance == null)
		{
			return;
		}

		if(TvTEvent.isParticipating())
		{
			boolean isParticipant = TvTEvent.isPlayerParticipant(playerInstance.getObjectId());
			String htmContent;

			htmContent = !isParticipant ? HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Participation.htm") : HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "RemoveParticipation.htm");

			if(htmContent != null)
			{
				int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", ConfigEventTvT.TVT_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team2name%", ConfigEventTvT.TVT_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
				if(!isParticipant)
				{
					npcHtmlMessage.replace("%fee%", TvTEvent.getParticipationFee());
				}

				playerInstance.sendPacket(npcHtmlMessage);
			}
		}
		else if(TvTEvent.isStarting() || TvTEvent.isStarted())
		{
			String htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Status.htm");

			if(htmContent != null)
			{
				int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
				int[] teamsPointsCounts = TvTEvent.getTeamsPoints();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				//npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", ConfigEventTvT.TVT_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
				npcHtmlMessage.replace("%team2name%", ConfigEventTvT.TVT_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1])); // <---- array index from 0 to 1 thx DaRkRaGe
				playerInstance.sendPacket(npcHtmlMessage);
			}
		}

		playerInstance.sendActionFailed();
	}
}