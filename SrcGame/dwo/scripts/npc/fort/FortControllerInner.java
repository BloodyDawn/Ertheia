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
 * Time: 1:43
 */

public class FortControllerInner extends Quest
{
	private static final int[] NPCs = {
		35675, 36376, 36338, 36303, 36269, 36231, 36193, 36158, 36127, 36093, 36055, 36020, 35986, 35948, 35913, 35879,
		35844, 35813, 35779, 35744, 35710
	};

	// Переменные из скриптов
	String fnHi = "fortress_inner_controller001.htm";
	String fnHi2 = "fortress_inner_controller002.htm";
	String fnHi3 = "fortress_inner_controller003.htm";
	int secret_number = 1;
	int broadcast_range = 1000;
	String fnGetPasswd = "fortress_inner_controller004.htm";
	int fstr_1stpasswd = 1300125;
	int fstr_2ndpasswd = 1300126;
	int fstr_nopasswd = 1300127;
	int fstr_try_limit = 1300128;
	int try_limit = 3;
	int try_delay = 30;
	int puzzle_range = 10;

	public FortControllerInner()
	{
		addSpawnId(NPCs);
		addFirstTalkId(NPCs);
		addAskId(NPCs, 505);
	}

	public static void main(String[] args)
	{
		new FortControllerInner();
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
		if(npc.getAiVarInt("i_ai3") >= try_limit)
		{
			return fnHi3;
		}
		if(ask == 505)
		{
			int i0 = -1;
			String content;
			if(player.getClassId().level() >= ClassLevel.SECOND.ordinal() && player.getRace() == Race.Dwarf)
			{
				if(npc.getAiVarInt("i_a0") > -1)
				{
					npc.setAiVar("i_a0", 2);
				}
				else if(npc.getAiVarInt("i_ai1") > -1)
				{
					npc.setAiVar("i_a1", 2);
				}
				else if(npc.getAiVarInt("i_ai2") > -1)
				{
					npc.setAiVar("i_a2", 2);
				}
				reply = 2;
			}
			else if(npc.getAiVarInt("i_a0") > -1)
			{
				i0 = npc.getAiVarInt("i_a0");
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
			if(npc.getAiVarInt("i_a0") > -1)
			{
				content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnGetPasswd);
				if(i0 == i1)
				{
					npc.setAiVar("i_ai1", -1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_1stpasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVarInt("i_ai3"))));
				}
				else
				{
					npc.setAiVar("i_ai3", npc.getAiVarInt("i_ai3") + 1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_nopasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVarInt("i_ai3"))));
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
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVarInt("i_ai3"))));
				}
				else
				{
					npc.setAiVar("i_ai3", npc.getAiVarInt("i_ai3") + 1);
					content = content.replace("<?passwd_status?>", FStringUtil.makeFString(fstr_1stpasswd));
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVarInt("i_ai3"))));
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
					content = content.replace("<?try_limit?>", FStringUtil.makeFString(fstr_try_limit, String.valueOf(npc.getAiVarInt("i_ai3"))));
					if(npc.getAiVarInt("i_ai3") >= try_limit)
					{
						startQuestTimer("1995", try_delay * 1000, npc, null);
					}
					return content;
				}
			}
			if(npc.getAiVarInt("i_a0") == -1 && npc.getAiVarInt("i_ai1") == -1 && npc.getAiVarInt("i_ai2") == -1)
			{
				npc.broadcastEvent(new String[]{"1006", String.valueOf(secret_number)}, broadcast_range, null);
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