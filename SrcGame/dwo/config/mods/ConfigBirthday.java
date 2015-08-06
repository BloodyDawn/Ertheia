package dwo.config.mods;

import dwo.config.Config;
import dwo.config.ConfigProperties;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 01.09.11
 * Time: 10:49
 */
public class ConfigBirthday extends Config
{
	private static final String path = BIRTHDAY_EVENT_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			ConfigProperties properties = new ConfigProperties(path);
			ALT_BIRTHDAY_GIFT = getInt(properties, "AltBirthdayGift", 22187);
			ALT_BIRTHDAY_MAIL_SUBJECT = getString(properties, "AltBirthdayMailSubject", "С Днем Рождения!");
			ALT_BIRTHDAY_MAIL_TEXT = getString(properties, "AltBirthdayMailText", "Привет Странник! По случаю твоего Дня Рождения, я решил отправить тебе подарок :) Ты можешь его найти в этой коробке. Надеюсь что этот подарок сделает тебя более счастливым в этот знаменательный день! \\n\\nС наилучшими пожеланиями, Алегрия.");
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
