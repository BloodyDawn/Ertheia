package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

public class TownTeleporter extends Quest
{
	private static final int[] TELEPORTERS = {
		30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275,
		31320, 31964, 32163, 31210
	};

	public TownTeleporter()
	{
		addAskId(TELEPORTERS, -19);
		addAskId(TELEPORTERS, -20);
		addAskId(TELEPORTERS, -21);
		addAskId(TELEPORTERS, -22);
		addAskId(TELEPORTERS, -1816);
		addAskId(TELEPORTERS, -1055);
		addAskId(TELEPORTERS, -1056);
		addAskId(TELEPORTERS, 255);
	}

	public static void main(String[] args)
	{
		new TownTeleporter();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(player.getTransformationId() == 111 || player.getTransformationId() == 112 || player.getTransformationId() == 124)
		{
			return "q194_noteleport.htm";
		}

		if(ask == -1816) // Телепорт на Остров Фантазий
		{
			if(reply == 3)
			{

				int i0 = player.getVariablesController().get(getClass().getSimpleName(), Integer.class, 0);
				int i1 = i0 / 1000000;
				if(i1 >= 99 || i1 < 0)
				{
					i1 = 0;
				}
				switch(npc.getNpcId())
				{
					case 30256:
						i1 = 1000000;
						break;
					case 30320:
						i1 = 2000000;
						break;
					case 30059:
						i1 = 3000000;
						break;
					case 30080:
						i1 = 4000000;
						break;
					case 30177:
						i1 = 5000000;
						break;
					case 30233:
						i1 = 6000000;
						break;
					case 30848:
						i1 = 7000000;
						break;
					case 1030899:
						i1 = 8000000;
						break;
					case 31210:
						i1 = 9000000;
						break;
					case 31275:
						i1 = 10000000;
						break;
					case 31320:
						i1 = 11000000;
						break;
					case 31964:
						i1 = 12000000;
						break;
				}
				int i2 = i0 / 1000000;
				if(i2 > 0)
				{
					i2 = 100;
					i2 *= 1000000;
				}
				else
				{
					i2 = 0;
				}
				if(i0 < 0)
				{
					player.getVariablesController().set(getClass().getSimpleName(), i1);
				}
				else
				{
					player.getVariablesController().set(getClass().getSimpleName(), i0 - i2 + i1);
				}
				if(Rnd.get(3) < 1)
				{
					player.teleToLocation(-58752, -56898, -2032);
				}
				else if(Rnd.get(2) < 1)
				{
					player.teleToLocation(-59722, -57866, -2032);
				}
				else
				{
					player.teleToLocation(-60695, -56894, -2032);
				}
			}
		}
		else if(ask == 255) // Телепорт на Арену и Ипподром
		{
			int i3 = npc.getNpcId();
			int i0 = 0;

			if(reply == 1 && i3 == 30256)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 100);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 2 && i3 == 30320)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 200);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 3 && i3 == 30059)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 300);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 4 && i3 == 30080)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 400);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 5 && i3 == 30177)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 500);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 6 && i3 == 30233)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 600);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 7 && i3 == 30848)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 700);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 8 && i3 == 30899)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 800);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 9 && i3 == 31210)
			{
				if(player.isCursedWeaponEquipped())
				{
					return "race_gatekeeper1010.htm";
				}
				else
				{
					player.teleToLocation(12661, 181687, -3540);
					return null;
				}
			}
			else if(reply == 10 && i3 == 31275)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 1000);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 11 && i3 == 31320)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 1100);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
			else if(reply == 12 && i3 == 31964)
			{
				player.getVariablesController().set(getClass().getSimpleName(), i0 + 1200);
				player.teleToLocation(12661, 181687, -3540);
				return null;
			}
		}
		else if(ask == -1055) // Телепортироваться в Верфь Летающих Кораблей Глудио
		{
			if(reply == 0)
			{
				player.teleToLocation(-149406, 255247, -85);
			}
		}
		else if(ask == -1056) // Переместиться на Говорящий Остров
		{
			if(reply == 0)
			{
				player.teleToLocation(-111988, 257240, -1376);
			}
		}
		else if(ask == -19)
		{
			if(reply == 0)
			{
				return player.isNoble() ? "fornobless.htm" : "fornonobless.htm";
			}
		}
		else if(ask == -20)
		{
			if(reply == 2)
			{
				if(player.getItemsCount(13722) > 0)
				{
					npc.showTeleportList(player, 3);
				}
				else
				{
					return "fornonoblessitem.htm";
				}
			}
		}
		else if(ask == -21)
		{
			if(reply == 2)
			{
				npc.showTeleportList(player, 2);
			}
		}
		else if(ask == -22)
		{
			return npc.getServerName() + "001.htm";
		}
		return null;
	}
}
