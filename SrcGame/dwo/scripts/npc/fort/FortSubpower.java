package dwo.scripts.npc.fort;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.FStringUtil;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.01.13
 * Time: 1:42
 */

public class FortSubpower extends Quest
{
	private static final int[] NPCs = {
		36374, 36336, 36267, 36229, 36191, 36091, 36053, 35984, 35946, 35877, 35777, 35708
	};

	// Переменные из скриптов
	String fnHi = "fortress_subpower001.htm";
	String fnHi2 = "fortress_subpower002.htm";
	String fnHi3 = "fortress_subpower003.htm";
	int secret_number = 1;
	int broadcast_range = 2000;
	int broadcast_msg = 1005;
	String fnGetPasswd = "fortress_subpower004.htm";
	int fstr_1stpasswd = 1300129;
	int fstr_2ndpasswd = 1300130;
	int fstr_nopasswd = 1300131;
	int fstr_try_limit = 1300128;
	int try_limit = 3;
	int try_delay = 30;
	int puzzle_range = 4;

	public FortSubpower()
	{
		addSpawnId(NPCs);
		addFirstTalkId(NPCs);
		addAskId(NPCs, 505);
	}

	public static void main(String[] args)
	{
		new FortSubpower();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}
		if(event.equals("1995"))
		{
			npc.setAiVar("i_ai0", Rnd.get(puzzle_range));
			npc.setAiVar("i_ai1", Rnd.get(puzzle_range));
			npc.setAiVar("i_ai2", Rnd.get(puzzle_range));
			npc.setAiVar("i_ai3", 0);
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		String content;
		if(npc.getAiVarInt("i_ai3") >= try_limit)
		{
			return fnHi3;
		}
		if(ask == 505)
		{
			int i0 = 0;
			if(player.getClassId().level() >= ClassLevel.SECOND.ordinal() && player.getRace() == Race.Dwarf)
			{
				if(npc.getAiVarInt("i_ai0") > -1)
				{
					npc.setAiVar("i_ai0", 2);
				}
				else if(npc.getAiVarInt("i_ai1") > -1)
				{
					npc.setAiVar("i_ai1", 2);
				}
				else if(npc.getAiVarInt("i_ai2") > -1)
				{
					npc.setAiVar("i_ai2", 2);
				}
				reply = 2;
			}
			else if(npc.getAiVarInt("i_ai0") > -1)
			{
				i0 = npc.getAiVarInt("i_ai0");
			}
			else if(npc.getAiVarInt("i_ai1") > -1)
			{
				i0 = npc.getAiVarInt("i_ai1");
			}
			else if(npc.getAiVarInt("i_ai2") > -1)
			{
				i0 = npc.getAiVarInt("i_ai2");
			}
			int i1 = reply;
			if(npc.getAiVarInt("i_ai0") > -1)
			{
				content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnGetPasswd);
				if(i0 == i1)
				{
					npc.setAiVar("i_ai0", -1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_1stpasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVar("i_ai3"))));
				}
				else
				{
					npc.setAiVar("i_ai3", npc.getAiVarInt("i_ai3") + 1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_nopasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVar("i_ai3"))));
					if(npc.getAiVarInt("i_ai3") >= try_limit)
					{
						startQuestTimer("1995", try_delay * 1000, npc, null);
					}
				}
				return content;
			}
			if(npc.getAiVarInt("i_ai1") > -1)
			{
				content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnGetPasswd);
				if(i0 == i1)
				{
					npc.setAiVar("i_ai1", -1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_2ndpasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVar("i_ai3"))));
				}
				else
				{
					npc.setAiVar("i_ai3", npc.getAiVarInt("i_ai3") + 1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_1stpasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVar("i_ai3"))));
					if(npc.getAiVarInt("i_ai3") >= try_limit)
					{
						startQuestTimer("1995", try_delay * 1000, npc, null);
					}
				}
				return content;
			}
			if(npc.getAiVarInt("i_ai2") > -1)
			{
				if(i0 == i1)
				{
					npc.setAiVar("i_ai2", -1);
				}
				else
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnGetPasswd);
					npc.setAiVar("i_ai3", npc.getAiVarInt("i_ai3") + 1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_2ndpasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVar("i_ai3"))));
					if(npc.getAiVarInt("i_ai3") >= try_limit)
					{
						startQuestTimer("1995", try_delay * 1000, npc, null);
					}
					return content;
				}
			}
			if(npc.getAiVarInt("i_ai0") == -1 && npc.getAiVarInt("i_ai1") == -1 && npc.getAiVarInt("i_ai2") == -1)
			{
				npc.broadcastEvent(new String[]{"1005", String.valueOf(secret_number)}, broadcast_range, null);
				return fnHi2;
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getAiVarInt("i_ai3") >= try_limit)
		{
			return fnHi3;
		}
		return npc.getAiVarInt("i_ai0") == -1 && npc.getAiVarInt("i_ai1") == -1 && npc.getAiVarInt("i_ai2") == -1 ? fnHi2 : fnHi;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setAiVar("i_ai0", Rnd.get(puzzle_range));
		npc.setAiVar("i_ai1", Rnd.get(puzzle_range));
		npc.setAiVar("i_ai2", Rnd.get(puzzle_range));
		npc.setAiVar("i_ai3", 0);
		return super.onSpawn(npc);
	}
}