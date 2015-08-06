package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.config.events.ConfigEventCTF;
import dwo.config.events.ConfigEventKOTH;
import dwo.config.events.ConfigEventTvT;
import dwo.gameserver.instancemanager.events.CTF.CTFEvent;
import dwo.gameserver.instancemanager.events.CTF.CTFManager;
import dwo.gameserver.instancemanager.events.KOTH.KOTHEvent;
import dwo.gameserver.instancemanager.events.KOTH.KOTHManager;
import dwo.gameserver.instancemanager.events.LastHero.LastHeroEvent;
import dwo.gameserver.instancemanager.events.TvT.TvTEvent;
import dwo.gameserver.instancemanager.events.TvT.TvTManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * User: Bacek
 * Date: 21.02.13
 * Time: 17:36
 */
public class EventBBSManager
{
	private static EventBBSManager _instance = new EventBBSManager();

	public static EventBBSManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new EventBBSManager();
		}
		return _instance;
	}

	public String parsecmd(String var, L2PcInstance activeChar, String content)
	{
		if(var != null)
		{
			if(var.startsWith("tvt"))
			{
				if(!ConfigEventTvT.TVT_EVENT_ENABLED)
				{
					return content.replace("<?html?>", getEventHtml(activeChar, "event_msg_off.htm"));
				}

				int nextPeriod = Math.max(0, (int) ((TvTManager.getInstance().getNextStartTime() - System.currentTimeMillis()) / 1000));
				int hours = nextPeriod / 60;
				int minutes = nextPeriod % 60;

				String html = getEventHtml(activeChar, "ev_tvt.htm");
				html = html.replace("<?time?>", hours + " ч. " + minutes + " мин.");
				html = html.replace("<?count?>", String.valueOf(TvTEvent.getMemberCount()));

				if(var.equalsIgnoreCase("tvt_reg"))
				{
					if(TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
					{
						html = html.replace("<?msg?>", getEventHtml(activeChar, TvTEvent.isParticipating() && TvTEvent.removeParticipant(activeChar.getObjectId()) ? "event_msg_unregistered.htm" : "event_msg_reg_ok.htm"));
					}
					else
					{
						TvTEvent.onBypass("tvt_event_participation", activeChar);
						html = html.replace("<?msg?>", getEventHtml(activeChar, TvTEvent.isPlayerParticipant(activeChar.getObjectId()) ? "event_msg_reg_ok.htm" : "event_msg_unregistered.htm"));
					}
				}
				else if(var.equalsIgnoreCase("tvt_exit"))
				{

				}
				else
				{
					html = html.replace("<?msg?>", getEventHtml(activeChar, TvTEvent.isPlayerParticipant(activeChar.getObjectId()) ? "event_msg_reg_ok.htm" : "event_msg_unregistered.htm"));
				}

				return content.replace("<?html?>", html);
			}
			else if(var.startsWith("ctf"))
			{
				if(!ConfigEventCTF.CTF_EVENT_ENABLED)
				{
					return content.replace("<?html?>", getEventHtml(activeChar, "event_msg_off.htm"));
				}

				int nextPeriod = Math.max(0, (int) ((CTFManager.getInstance().getNextStartTime() - System.currentTimeMillis()) / 1000));
				int hours = nextPeriod / 60;
				int minutes = nextPeriod % 60;

				String html = getEventHtml(activeChar, "ev_ctf.htm");
				html = html.replace("<?time?>", hours + " ч. " + minutes + " мин.");
				html = html.replace("<?count?>", String.valueOf(CTFEvent.getMemberCount()));

				if(var.equalsIgnoreCase("ctf_reg"))
				{
					if(TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
					{
						html = html.replace("<?msg?>", getEventHtml(activeChar, CTFEvent.isParticipating() && CTFEvent.removeParticipant(activeChar) ? "event_msg_unregistered.htm" : "event_msg_reg_ok.htm"));
					}
					else
					{
						CTFEvent.onBypass("CTF_event_participation", activeChar);
						html = html.replace("<?msg?>", getEventHtml(activeChar, CTFEvent.isPlayerParticipant(activeChar.getObjectId()) ? "event_msg_reg_ok.htm" : "event_msg_unregistered.htm"));
					}
				}
				else
				{
					html = html.replace("<?msg?>", getEventHtml(activeChar, CTFEvent.isPlayerParticipant(activeChar.getObjectId()) ? "event_msg_reg_ok.htm" : "event_msg_unregistered.htm"));
				}

				return content.replace("<?html?>", html);
			}
			else if(var.startsWith("koth"))
			{
				if(!ConfigEventKOTH.KOTH_EVENT_ENABLED)
				{
					return content.replace("<?html?>", getEventHtml(activeChar, "event_msg_off.htm"));
				}

				int nextPeriod = Math.max(0, (int) ((KOTHManager.getInstance().getNextStartTime() - System.currentTimeMillis()) / 1000));
				int hours = nextPeriod / 60;
				int minutes = nextPeriod % 60;

				String html = getEventHtml(activeChar, "ev_koth.htm");
				html = html.replace("<?time?>", hours + " ч. " + minutes + " мин.");
				html = html.replace("<?count?>", String.valueOf(KOTHEvent.getMemberCount()));

				if(var.equalsIgnoreCase("koth_reg"))
				{
					if(TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
					{
						html = html.replace("<?msg?>", getEventHtml(activeChar, KOTHEvent.isParticipating() && KOTHEvent.removeParticipant(activeChar.getObjectId()) ? "event_msg_unregistered.htm" : "event_msg_reg_ok.htm"));
					}
					else
					{
						KOTHEvent.onBypass("KOTH_event_participation", activeChar);
						html = html.replace("<?msg?>", getEventHtml(activeChar, KOTHEvent.isPlayerParticipant(activeChar.getObjectId()) ? "event_msg_reg_ok.htm" : "event_msg_unregistered.htm"));
					}
				}
				else
				{
					html = html.replace("<?msg?>", getEventHtml(activeChar, KOTHEvent.isPlayerParticipant(activeChar.getObjectId()) ? "event_msg_reg_ok.htm" : "event_msg_unregistered.htm"));
				}

				return content.replace("<?html?>", html);
			}
			else if(var.startsWith("lasthero"))
			{
				int nextPeriod = Math.max(0, (int) ((LastHeroEvent.getInstance().getNextPediod() - System.currentTimeMillis()) / 1000));
				int hours = nextPeriod / 60;
				int minutes = nextPeriod % 60;

				String html = getEventHtml(activeChar, "ev_lasthero.htm");

				if(var.startsWith("lasthero_reg"))
				{
					String result = LastHeroEvent.getInstance().onTalk(null, activeChar);
					html = result.endsWith(".htm") ? html.replace("<?msg?>", getEventHtml(activeChar, result)) : html.replace("<?msg?>", result);
				}
				else if(var.startsWith("lasthero_exit"))
				{
					String result = LastHeroEvent.getInstance().onAdvEvent("exit", null, activeChar);
					html = result.endsWith(".htm") ? html.replace("<?msg?>", getEventHtml(activeChar, result)) : html.replace("<?msg?>", result);
				}
				else
				{
					html = html.replace("<?msg?>", LastHeroEvent.getInstance().isParticipiant(activeChar) ? getEventHtml(activeChar, "event_msg_reg_ok.htm") : "");
				}
				html = html.replace("<?time?>", nextPeriod > 0 ? hours + " мин. " + minutes + " сек." : "В процессе");
				html = html.replace("<?count?>", String.valueOf(LastHeroEvent.getInstance().getParticipiantCount()));

				return content.replace("<?html?>", html);
			}
			else if(var.startsWith("peklo"))
			{
				return content.replace("<?html?>", getEventHtml(activeChar, "event_msg_off.htm"));
			}
		}
		return content.replace("<?html?>", getEventHtml(activeChar, "event_msg_select.htm"));
	}

	private String getEventHtml(L2PcInstance activeChar, String html)
	{
		return TopBBSManager.getInstance().getHtml(activeChar, "event/" + html);
	}
}
