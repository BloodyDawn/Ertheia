package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.stat.PcStat;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExVitalityEffectInfo;

import java.util.StringTokenizer;

/**
 * @author Psychokiller1888
 */

public class AdminVitality implements IAdminCommandHandler
{

	private static final String[] ADMIN_COMMANDS = {
		"admin_set_vitality", "admin_full_vitality", "admin_empty_vitality", "admin_get_vitality"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(!Config.ENABLE_VITALITY)
		{
			activeChar.sendMessage("Виталити система отключена на этом сервере!");
			return false;
		}

		int level = 0;
		int vitality = 0;

		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();

		if(activeChar.getTarget() instanceof L2PcInstance)
		{
			L2PcInstance target;
			target = (L2PcInstance) activeChar.getTarget();

			switch(cmd)
			{
				case "admin_set_vitality":
					try
					{
						vitality = Integer.parseInt(st.nextToken());
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Неверное значение виталити.");
					}

					target.setVitalityPoints(vitality);
					target.sendMessage("ГМ выставил Ваши очки виталити на " + vitality);
					break;
				case "admin_full_vitality":
					target.setVitalityPoints(PcStat.MAX_VITALITY_POINTS);
					target.sendMessage("ГМ полностью восставновил Вам виталити.");
					break;
				case "admin_empty_vitality":
					target.setVitalityPoints(PcStat.MIN_VITALITY_POINTS);
					target.sendMessage("ГМ полностью убрал Вам виталити.");
					break;
				case "admin_get_vitality":
					activeChar.sendMessage("Очки виталити игрока: " + target.getVitalityDataForCurrentClassIndex().getVitalityPoints());
					break;
			}
			target.sendPacket(new ExVitalityEffectInfo(target));
			return true;
		}
		else
		{
			activeChar.sendMessage("Цель не найдена, либо не является игроком.");
			return false;
		}
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
