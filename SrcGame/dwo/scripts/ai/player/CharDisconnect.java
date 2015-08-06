package dwo.scripts.ai.player;

import dwo.config.network.ConfigGuardEngine;
import dwo.gameserver.engine.guardengine.GuardHwidManager;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.PartySearchingManager;
import dwo.gameserver.model.actor.controller.player.EventController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.npc.L2Event;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.EventData;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 06.04.12
 * Time: 5:38
 */

public class CharDisconnect extends Quest
{
	public CharDisconnect(String name, String desc)
	{
		super(name, desc);
		addEventId(HookType.ON_DISCONNECT);
	}

	public static void main(String[] args)
	{
		new CharDisconnect("CharDisconnect", "ai");
	}

	@Override
	public void onDisconnect(L2PcInstance player)
	{
		if(ConfigGuardEngine.GUARD_ENGINE_ENABLE)
		{
			GuardHwidManager.getInstance().removeAuthorizedClient(player.getClient().getHWID());
		}

		// Если персонаж участвует в ивенте, то сохраняем текущий прогресс в L2Event чтобы восстановить при следующем входе в игру
		if(player.getEventController().isParticipant())
		{
			Location loc = player.getLocationController().getMemorizedLocation();
			if(loc != null)
			{
				EventController controller = player.getEventController();
				EventData data = new EventData(loc.getX(), loc.getY(), loc.getZ(), controller.getReputation(), controller.getPvPKills(), controller.getPkKills(), controller.getTitle(), controller.getKills(), controller.isForceSit());
				L2Event.connectionLossData.put(player.getName(), data);
			}
		}

		// Если игрок находился в листе ожидания системы поиска группы - удаляем его оттуда
		if(player.isInPartyWaitingList())
		{
			PartySearchingManager.getInstance().deleteFromWaitingList(player, false);
		}

		// Если на игроке висит Клановый Бафф "Рождение Клана" - снимаем его
		// TODO: Unhardcode
		if(player.getFirstEffect(19009) != null)
		{
			player.stopSkillEffects(19009);
		}

		// Снимаем бафы с флагом L2RemovedEffectType.ON_DISCONNECT
		for(L2Effect effect : player.getAllEffects())
		{
			if(effect != null && effect.getRemovedEffectType().contains(L2EffectStopCond.ON_DISCONNECT))
			{
				effect.exit(true);
			}
		}
	}
}
