package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.07.12
 * Time: 19:14
 */

public class FantasyIsleCompanyMobs extends Quest
{
	// Нпц
	private static final int[] ПрячущиесяМобы = {33651, 33652, 33653, 33654, 33655, 33656, 33657};
	private static final int ЛетучиеМышки = 33658;

	// Предметы
	private static final int ФейерверкУжаса = 34761;

	// Другое
	private static final int СоциалкаХлопка = 33;
	private static final SkillHolder ОтменаСкрытия = new SkillHolder(14839, 1);
	private static final int СкиллФейерверка = 9399;

	public FantasyIsleCompanyMobs()
	{
		addSocialSeeId(ПрячущиесяМобы);
		addSkillSeeId(ЛетучиеМышки);
	}

	public static void main(String[] args)
	{
		new FantasyIsleCompanyMobs();
	}

	@Override
	public String onSocialSee(L2Npc npc, L2PcInstance player, L2Object target, int socialId)
	{
		if(target != null && target instanceof L2Npc && socialId == СоциалкаХлопка)
		{
			L2Npc t = (L2Npc) target;
			if(!t.isDead())
			{
				if(player.isInsideRadius(t, 80, true, false))
				{
					// Кастуем мобом отмену хайда
					npc.doCast(ОтменаСкрытия.getSkill());

					// Добавляем игроку в инвентарь Фейерверк и показываем сообщение
					InventoryUpdate iu = new InventoryUpdate();
					iu.addItem(player.getInventory().addItem(ProcessType.NPC, ФейерверкУжаса, Rnd.getChance(10) ? 2 : 1, player, npc));
					player.sendPacket(iu);
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1811344), ExShowScreenMessage.MIDDLE_CENTER, 2000));

					// Двигаем НПЦ хз куда и удаляем :D
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + Rnd.get(-100, 100), npc.getY() + Rnd.get(-100, 100), npc.getZ()));
					npc.getLocationController().decay();
				}
			}
		}
		return super.onSocialSee(npc, player, target, socialId);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(npc.getNpcId() == ЛетучиеМышки && skill.getId() == СкиллФейерверка && caster.isInsideRadius(npc, 80, true, false))
		{
			npc.doDie(caster);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
}