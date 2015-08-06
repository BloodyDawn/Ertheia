package dwo.dao.queries;

/**
 * @author Yorie
 *         Date: 17.03.12
 */
public class Account
{
	public static final String ACCOUNT_EXISTS = "SELECT COUNT(*) FROM `accounts` WHERE `login` = ? LIMIT 1";
	public static final String ADD_ACCOUNT = "INSERT INTO `accounts` (`login`, `password`, `lastactive`, `accessLevel`, `lastIP`) VALUES (?,?,?,?,?)";
	public static final String CHANGE_PASSWORD = "UPDATE `accounts` SET `password` = ? WHERE `login` = ?";
}
