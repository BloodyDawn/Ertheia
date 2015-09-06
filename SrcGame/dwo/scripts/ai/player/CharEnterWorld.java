package dwo.scripts.ai.player;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.Announcements;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.PetitionManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WeddingManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.communitybbs.Manager.RegionBBSManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSiegeEngine;
import dwo.gameserver.model.world.residence.clanhall.type.AuctionableHall;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortSiegeEngine;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.*;
import dwo.gameserver.network.game.serverpackets.packet.ability.ExAcquireAPSkillList;
import dwo.gameserver.network.game.serverpackets.packet.event.ExLightingCandleEvent;
import dwo.gameserver.network.game.serverpackets.packet.ex.*;
import dwo.gameserver.network.game.serverpackets.packet.friend.L2FriendList;
import dwo.gameserver.network.game.serverpackets.packet.henna.HennaInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoAbnormalVisualEffect;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoCubic;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoEquipSlot;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExUnReadMailCount;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPCCafePointInfo;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExPledgeCount;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListAll;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeSkillList;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeWaitingListAlarm;
import dwo.gameserver.network.game.serverpackets.packet.primeshop.ExBR_NewIConCashBtnWnd;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowUsm;
import dwo.scripts.services.Tutorial;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 03.04.12
 * Time: 6:02
 */

public class CharEnterWorld extends Quest
{
	public CharEnterWorld()
	{
		addEventId(HookType.ON_ENTER_WORLD);
	}

	public static void main(String[] args)
	{
		new CharEnterWorld();
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		// Restore to instanced area if enabled
		if(Config.RESTORE_PLAYER_INSTANCE)
		{
			player.getInstanceController().setInstanceId(InstanceManager.getInstance().getPlayerInstance(player.getObjectId()));
		}
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(player.getObjectId());
			if(instanceId > 0)
			{
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(player.getObjectId());
			}
		}

		if(WorldManager.getInstance().findObject(player.getObjectId()) != null)
		{
			return;
		}

		// Set dead status if applies
		if(player.getCurrentHp() < 0.5)
		{
			player.setIsDead(true);
		}

		// Отправляем / включаем админ функции
		sendAdminInfo(player);


		/* Отправка инормации клиенту */

		// ExSendManorList ( шлется от реквеста )

		// Отправляем информацию о шопе
		player.sendPacket(new ExBR_NewIConCashBtnWnd());

		// ExShowFortressInfo ( шлется от реквеста )

		// Отправляем информацию о эвенте
		player.sendPacket(new ExLightingCandleEvent());

		// Send Teleport Bookmark List
		player.sendPacket(new ExGetBookMarkInfo(player));

		// Отправляем информацию о макросах
		player.getMacroses().sendAllMacro();

		// Send GG check
		player.queryGameGuard();

		// Шлем содержимое инвентаря ( ItemList и ExQuestItemList )
		player.sendPacket(new ItemList(player, false));

		// Шлем панель ярлыков
		player.sendPacket(new ShortCutInit(player));

		// Send Action list
		player.sendPacket(ExBasicActionList.getStaticPacket(player));

		// отправка SkillList и ExAcquirableSkillListByClass
		player.sendSkillList();

		// Шлем информацию о татуировках
		player.sendPacket(new HennaInfo(player));

		// Шлем информацию о состоянии замков (тьма\свет)
		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			player.sendPacket(new ExCastleState(castle));
		}

		// Шлем инормацию о клане если он есть
		boolean showClanNotice = sendClanInfo(player);

		// Отправляем информацию о виталити
		player.sendPacket(new ExVitalityEffectInfo(player));

		// Шлем информацию о квестах
		Quest.playerEnter(player);
		player.sendPacket(new QuestList());

		// Загружаем информацию о премиуме
		if(Config.PREMIUM_ENABLED)
		{
			// Отправляем информацию игроку, если преимум-аккаунт активен
			if(player.isPremiumState())
			{
				player.startPremiumTask();
				player.sendPacket(new ExBR_PremiumState(player.getObjectId(), 1));
				long remainingTime = (player.getPremiumTime() - System.currentTimeMillis()) / 1000;
				int days = (int) (remainingTime / 86400);
				remainingTime %= 86400;
				int hours = (int) (remainingTime / 3600);
				remainingTime %= 3600;
				int minutes = (int) (remainingTime / 60);
				player.sendMessage("Ваш премиум аккаунт будет активен еще: " + days + " дней, " + hours + " часов, " + minutes + " минут.");
			}
			else
			{
				player.sendPacket(new ExBR_PremiumState(player.getObjectId(), 0));
			}
		}

		// Сообщения приветствия от сервера
		player.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);

		player.sendPacket(new EtcStatusUpdate(player));

		// Френд-лист
		player.sendPacket(new L2FriendList(player));

		player.sendPacket(new MagicAndSkillList(player));

		// Expand Skill
		player.sendPacket(new ExStorageMaxCount(player));

		// Шлем иконку поиска клана
		if(player.getClan() == null)
		{
			player.sendPacket(new ExPledgeWaitingListAlarm());
		}

		// Показывает кол-во адены и кол-во занятых ячеек в инвенторе
		player.sendPacket(new ExAdenaInvenCount(player));

        player.sendPacket(new ExUserInfoEquipSlot(player));

		player.sendPacket(new SkillCoolTime(player));

		// Показывает непрочитанные письма
		player.sendPacket(new ExUnReadMailCount(player));

		// Шлем ExReceiveShowPostFriend
		player.sendPacket(new ExReceiveShowPostFriend(player));

		// Шлем информацию о PCBang очках
		if(Config.PCBANG_ENABLED)
		{
			if(player.getPcBangPoints() > 0)
			{
				player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), 0, false, false, 1, 1));
			}
			else
			{
				player.sendPacket(new ExPCCafePointInfo());
			}
		}

		// Шлем анонсы
		Announcements.getInstance().showAnnouncements(player);

		// Шлем информацию о сабкласах персонажа
		player.sendPacket(new ExSubjobInfo(player));

		// Наставничество
		player.sendPacket(new ExMentorList(player));

		if(!Config.DISABLE_TUTORIAL)
		{
			loadTutorial(player);
		}

		if(Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			player.setSpawnProtection(true);
		}

		player.getLocationController().spawn(player.getX(), player.getY(), player.getZ());

		player.getInventory().applyItemSkills();

		player.getEventController().restore();

		// Свадьба
		if(Config.ALLOW_WEDDING)
		{
			engage(player);
			notifyPartner(player);
		}

		if(player.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(player.getCursedWeaponEquippedId()).cursedOnLogin();
		}

		player.updateEffectIcons();

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(player.getName());
		for(int id : RelationListManager.getInstance().getFriendList(player.getObjectId()))
		{
			L2Object obj = WorldManager.getInstance().findObject(id);
			if(obj != null)
			{
				obj.getActingPlayer().sendPacket(sm);
			}
		}

		if(showClanNotice)
		{
			NpcHtmlMessage notice = new NpcHtmlMessage(1);
			notice.setFile(player.getLang(), "clan_popup.htm");
			notice.replace("<\\?pledge_name\\?>", player.getClan().getName());
			notice.replace("<\\?content\\?>", player.getClan().getNotice().replaceAll("\r\n", "<br>"));
			notice.disableValidation();
			player.sendPacket(notice);
		}
		else if(Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm(player.getLang(), "servnews.htm");
			if(serverNews != null)
			{
				player.sendPacket(new NpcHtmlMessage(1, serverNews));
			}
		}

		if(Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(player);
		}

		player.onPlayerEnter();
		player.startRecommendationGiveTask();
		player.broadcastUserInfo();
        player.sendPacket(new StatusUpdate(player.getObjectId()));

        player.sendPacket(new ExUserInfoEquipSlot(player));
        player.sendPacket(new ExUserInfoCubic(player));
        player.sendPacket(new ExUserInfoAbnormalVisualEffect(player));

		for(L2ItemInstance i : player.getInventory().getItems())
		{
			if(i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
			if(i.isShadowItem() && i.isEquipped())
			{
				i.decreaseMana(false);
			}
		}

		for(L2ItemInstance i : player.getWarehouse().getItems())
		{
			if(i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}

		if(player.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		}

		// remove combat flag before teleporting
		if(player.getInventory().getItemByItemId(9819) != null)
		{
			Fort fort = FortManager.getInstance().getFort(player);

			if(fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(player, fort.getFortId());
			}
			else
			{
				long slot = player.getInventory().getSlotFromItem(player.getInventory().getItemByItemId(9819));
				player.getInventory().unEquipItemInBodySlot(slot);
				player.destroyItem(ProcessType.COMBATFLAG, player.getInventory().getItemByItemId(9819), null, true);
			}
		}

		// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
		if(!player.isGM()
			// inside siege zone
			&& player.isInsideZone(L2Character.ZONE_SIEGE)
			// but non-participant or attacker
			&& (!player.isInSiege() || player.getSiegeSide() != PlayerSiegeSide.DEFENDER))
		{
			player.teleToLocation(TeleportWhereType.TOWN);
		}

		if(Config.ALLOW_MAIL)
		{
			if(MailManager.getInstance().hasUnreadPost(player))
			{
				player.sendPacket(ExNoticePostArrived.valueOf(false));
			}
		}

		EventManager.onLogin(player);

		RegionBBSManager.getInstance().changeCommunityBoard();

		int birthday = player.getDaysToBirthDay();
		if(birthday == 0)
		{
			player.sendPacket(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED);
			//TODO: player.sendPacket(new ExNotifyBirthDay());
		}
		else if(birthday != -1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY).addString(Integer.toString(birthday)));
		}

		// Иконка о наличии итемов в премиум магазине
		if(!player.getPremiumItemList().isEmpty())
		{
			player.sendPacket(new ExNotifyPremiumItem());
		}

		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			if(!WorldStatisticsManager.getInstance().getAllCurrentStatistics().containsKey(player.getObjectId()))
			{
				// Создаем новый обьект мировой статистики и сохраняем его в базу
				WorldStatisticsManager.getInstance().createStatisticForNewPlayer(player.getObjectId());
			}
		}

		// Нельзя подымать вверх т.к не будет показыватся окно смерти !!
		if(player.isAlikeDead())
		{
			player.sendPacket(new Die(player));
		}

		// Проверка на привязку персонажа к IP
		if(player.getVariablesController().get("ipRestrict") != null)
		{
			if(!player.getVariablesController().get("ipRestrict").equals(player.getClient().getConnectionAddress().getHostAddress()))
			{
				player.logout(true);
			}
		}

        if (player.getOnlineTime() == 0 && player.getLevel() == 1)
        {
            if (player.getRace() == Race.Ertheia )
            {
                player.sendPacket(new ExShowUsm(ExShowUsm.INTRO_ARTEAS));
            }
            else
            {
                player.sendPacket(new ExShowUsm(ExShowUsm.INTRO_OTHERS));
            }
        }

        player.sendPacket(new ExAcquireAPSkillList(player));
        player.sendPacket(new ExWorldChatCnt(player));

    }

	/**
	 * Оповещение спонсора о входе в игру персонажа
	 * TODO: Проверить, возможно спонсорство заменено наставничеством
	 *
	 * @param activeChar персонаж-ученик
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if(activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = WorldManager.getInstance().getPlayer(activeChar.getSponsor());

			if(sponsor != null)
			{
				sponsor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addString(activeChar.getName()));
			}
		}
		else if(activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = WorldManager.getInstance().getPlayer(activeChar.getApprentice());

			if(apprentice != null)
			{
				apprentice.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addString(activeChar.getName()));
			}
		}
	}

	/**
	 * Оповещение скрипта Туториала о входе в игру
	 * @param player персонаж
	 */
	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState(Tutorial.class);

		if(qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}

	/**
	 * Оповещает мужа\жену о входе в игру персонажа
	 *
	 * @param cha персонаж
	 */
	private void notifyPartner(L2PcInstance cha)
	{
		if(cha.getPartnerId() != 0)
		{
			int objId = cha.getPartnerId();

			try
			{
				L2PcInstance partner = WorldManager.getInstance().getPlayer(objId);

				if(partner != null)
				{
					partner.sendMessage("Ваша половинка появилась в игровом мире.");
				}
			}
			catch(ClassCastException cce)
			{
				_log.log(Level.ERROR, "Wedding Error: ID " + objId + " is now owned by a(n) " + WorldManager.getInstance().findObject(objId).getClass().getSimpleName());
			}
		}
	}

	/**
	 * Назначает мужа\жену входящему в игру персонажу из CoupleManager
	 *
	 * @param cha персонаж
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();

		WeddingManager.getInstance().getCouples().stream().filter(cl -> cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid).forEach(cl -> {
			if(cl.getMaried())
			{
				cha.setMarried(true);
			}

			cha.setCoupleId(cl.getId());

			if(cl.getPlayer1Id() == _chaid)
			{
				cha.setPartnerId(cl.getPlayer2Id());
			}
			else
			{
				cha.setPartnerId(cl.getPlayer1Id());
			}
		});
	}

	/**
	 * Применяем настройки входа для админа
	 *
	 * @param player персонаж
	 */
	private void sendAdminInfo(L2PcInstance player)
	{
		// Apply special GM properties to the GM when entering
		if(player.isGM())
		{
			if(Config.ENABLE_SAFE_ADMIN_PROTECTION)
			{
				if(Config.SAFE_ADMIN_NAMES.contains(player.getName()))
				{
					player.getPcAdmin().setIsSafeAdmin(true);
					if(Config.SAFE_ADMIN_SHOW_ADMIN_ENTER)
					{
						_log.log(Level.INFO, "Safe Admin: " + player.getName() + '(' + player.getObjectId() + ") has been logged in.");
					}
				}
				else
				{
					player.getPcAdmin().punishUnSafeAdmin();
					_log.log(Level.WARN, "WARNING: Unsafe Admin: " + player.getName() + '(' + player.getObjectId() + ") as been logged in.");
					_log.log(Level.WARN, "If you have enabled some punishment, He will be punished.");
				}
			}

			if(Config.GM_STARTUP_INVULNERABLE && AdminTable.getInstance().hasAccess("admin_invul", player.getAccessLevel()))
			{
				player.setIsInvul(true);
			}

			if(Config.GM_STARTUP_INVISIBLE && AdminTable.getInstance().hasAccess("admin_invisible", player.getAccessLevel()))
			{
				player.getAppearance().setInvisible();
			}

			if(Config.GM_STARTUP_SILENCE && AdminTable.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
			{
				player.setSilenceMode(true);
			}

			if(Config.GM_STARTUP_DIET_MODE && AdminTable.getInstance().hasAccess("admin_diet", player.getAccessLevel()))
			{
				player.setDietMode(true);
				player.refreshOverloaded();
			}

			if(Config.GM_STARTUP_AUTO_LIST && AdminTable.getInstance().hasAccess("admin_gmliston", player.getAccessLevel()))
			{
				AdminTable.getInstance().addGm(player, false);
			}
			else
			{
				AdminTable.getInstance().addGm(player, true);
			}

			if(Config.GM_GIVE_SPECIAL_SKILLS)
			{
				SkillTreesData.getInstance().addGmSkills(player, false);
			}

			if(Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				SkillTreesData.getInstance().addGmSkills(player, true);
			}
		}
	}

	/**
	 * Отправляем информацию о клане
	 *
	 * @param player персонаж
	 */
	private boolean sendClanInfo(L2PcInstance player)
	{
		// Клан
		boolean showClanNotice = false;
		L2Clan clan = player.getClan();

		if(player.getClan() != null)
		{
			player.sendPacket(new PledgeSkillList(player.getClan()));

			AuctionableHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());

			if(clanHall != null)
			{
				if(!clanHall.isPaid())
				{
					player.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
				}
			}

			for(CastleSiegeEngine castleSiegeEngine : CastleSiegeManager.getInstance().getSieges())
			{
				if(!castleSiegeEngine.isInProgress())
				{
					continue;
				}

				if(castleSiegeEngine.checkIsAttacker(player.getClan()))
				{
					player.setSiegeSide(PlayerSiegeSide.ATTACKER);
					player.setActiveSiegeId(castleSiegeEngine.getCastle().getCastleId());
				}

				else if(castleSiegeEngine.checkIsDefender(player.getClan()))
				{
					player.setSiegeSide(PlayerSiegeSide.DEFENDER);
					player.setActiveSiegeId(castleSiegeEngine.getCastle().getCastleId());
				}
			}

			for(FortSiegeEngine siegeEngine : FortSiegeManager.getInstance().getSieges())
			{
				if(!siegeEngine.isInProgress())
				{
					continue;
				}

				if(siegeEngine.checkIsAttacker(player.getClan()))
				{
					player.setSiegeSide(PlayerSiegeSide.ATTACKER);
					player.setActiveSiegeId(siegeEngine.getFort().getFortId());
				}

				else if(siegeEngine.checkIsDefender(player.getClan()))
				{
					player.setSiegeSide(PlayerSiegeSide.DEFENDER);
					player.setActiveSiegeId(siegeEngine.getFort().getFortId());
				}
			}

			for(ClanHallSiegable hall : ClanHallSiegeManager.getInstance().getConquerableHalls().values())
			{
				if(!hall.isInSiege())
				{
					continue;
				}

				if(hall.isRegistered(player.getClan()))
				{
					player.setSiegeSide(PlayerSiegeSide.ATTACKER);
					player.setActiveSiegeId(hall.getId());
					player.setIsInHideoutSiege(true);
				}
			}

			// Residential skills support
			if(player.getClan().getCastleId() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(player.getClan()).giveResidentialSkills(player);
			}

			if(player.getClan().getFortId() > 0)
			{
				FortManager.getInstance().getFortByOwner(player.getClan()).giveResidentialSkills(player);
			}

			notifySponsorOrApprentice(player);

			clan.notifyClanEnterWorld(player);

			showClanNotice = player.getClan().isNoticeEnabled();

			// Шлем клиенту информацию
			player.sendPacket(new PledgeShowMemberListUpdate(player));
			player.sendPacket(new PledgeShowMemberListAll(player.getClan(), player));
			player.sendPacket(new PledgeSkillList(player.getClan()));
			player.sendPacket(new ExPledgeCount(clan.getOnlineMembersCount()));

		}
		return showClanNotice;
	}
}