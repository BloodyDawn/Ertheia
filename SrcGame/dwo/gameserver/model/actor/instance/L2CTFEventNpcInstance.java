package dwo.gameserver.model.actor.instance;

import dwo.config.events.ConfigEventCTF;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.events.CTF.CTFEvent;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

public class L2CTFEventNpcInstance extends L2Npc
{
	private static final String htmlPath = "mods/CTFEvent/";

	public L2CTFEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		CTFEvent.onBypass(command, playerInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if(player == null)
		{
			return;
		}

		if(CTFEvent.isParticipating())
		{
			boolean isParticipant = CTFEvent.isPlayerParticipant(player.getObjectId());
			String htmContent;

			htmContent = !isParticipant ? HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Participation.htm") : HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "RemoveParticipation.htm");

			if(htmContent != null)
			{
				int[] teamsPlayerCounts = CTFEvent.getTeamsPlayerCounts();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", ConfigEventCTF.CTF_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team2name%", ConfigEventCTF.CTF_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
				if(!isParticipant)
				{
					npcHtmlMessage.replace("%fee%", CTFEvent.getParticipationFee());
				}

				player.sendPacket(npcHtmlMessage);
			}
		}
		else if(CTFEvent.isStarting() || CTFEvent.isStarted())
		{
			String htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Status.htm");

			if(htmContent != null)
			{
				int[] teamsPlayerCounts = CTFEvent.getTeamsPlayerCounts();
				long[] teamsPointsCounts = CTFEvent.getTeamsPoints();
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%team1name%", ConfigEventCTF.CTF_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
				npcHtmlMessage.replace("%team2name%", ConfigEventCTF.CTF_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1]));
				player.sendPacket(npcHtmlMessage);
			}
		}

		player.sendActionFailed();
	}
}