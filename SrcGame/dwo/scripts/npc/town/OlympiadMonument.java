package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.12.12
 * Time: 16:08
 */

public class OlympiadMonument extends Quest
{
	private static final int[] NPCs = {31690, 31769, 31770, 31771, 31772};

	private static final int[] HeroWeapons = {
		30392, 30393, 30394, 30395, 30396, 30397, 30398, 30399, 30400, 30401, 30402, 30403, 30404, 30405
	};

	public OlympiadMonument()
	{
		addAskId(NPCs, -50);
		addAskId(NPCs, -51);
		addAskId(NPCs, -52);
		addAskId(NPCs, -54);
		addAskId(NPCs, -60);
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new OlympiadMonument();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -50)
		{
			return player.isNoble() ? "obelisk001.htm" : "obelisk001a.htm";
		}
		if(ask == -51)
		{
			switch(reply)
			{
				case 1: // Признание
					// TODO: Система олимпиады несовместима с оф-взятием хиро
					return player.getOlympiadController().isHero() ? "obelisk010b.htm" : "obelisk010a.htm";
				case 2: // Получить Оружие
					if(player.getOlympiadController().isHero())
					{
						return hasHeroWeapon(player) ? "obelisk020b.htm" : "obelisk020.htm";
					}
					else
					{
						return "obelisk020a.htm";
					}
				case 4: // Получить диадему
					if(player.getOlympiadController().isHero())
					{
						if(player.getItemsCount(6842) == 0)
						{
							player.addItem(ProcessType.OLYMPIAD, 6842, 1, npc, true);
							return null;
						}
						else
						{
							return "obelisk020c.htm";
						}
					}
					else
					{
						return "obelisk020d.htm";
					}
			}
		}
		else if(ask == -52) // Признание
		{
			// TODO: Система олимпиады несовместима с оф-взятием хиро
			return "obelisk010c.htm";
		}
		else if(ask == -54)
		{
			if(reply == 1)  // Получить плащ (1-3 место)
			{
				if(player.getOlympiadController().isHero())
				{
					if(player.getItemsCount(30372) == 0)
					{
						player.addItem(ProcessType.OLYMPIAD, 30372, 1, npc, true);
						return null;
					}
					else
					{
						return "obelisk040c.htm";
					}
				}
				else if(player.getVariablesController().get("olympiadRank2or3", Boolean.class, false))
				{
					if(player.getItemsCount(30373) == 0)
					{
						player.addItem(ProcessType.OLYMPIAD, 30373, 1, npc, true);
						player.getVariablesController().unset("olympiadRank2or3");
						return null;
					}
					else
					{
						return "obelisk040c.htm";
					}
				}
				else
				{
					return "obelisk040d.htm";
				}
			}
		}
		else if(ask == -60)
		{
			if(reply == 0)
			{
				if(player.isNoble())
				{
					return "obelisk001.htm";
				}
				return "obelisk001a.htm";
			}
			else
			{
				if(player.getOlympiadController().isHero() && !hasHeroWeapon(player))
				{
					// Проверка на дюп
					if(reply < 1 && reply > 14)
					{
						return null;
					}

					player.addItem(ProcessType.OLYMPIAD, 30391 + reply, 1, npc, true);
					return null;
				}
			}

		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.isNoble())
		{
			return "obelisk001.htm";
		}
		return "obelisk001a.htm";
	}

	/***
	 * @param player проверяемый игрок
	 * @return {@code true} если у проверяемого игрока уже есть геройское оружие
	 */
	private boolean hasHeroWeapon(L2PcInstance player)
	{
		for(int i : HeroWeapons)
		{
			if(player.getItemsCount(i) > 0)
			{
				return true;
			}
		}
		return false;
	}
}