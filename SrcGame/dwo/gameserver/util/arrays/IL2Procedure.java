package dwo.gameserver.util.arrays;

/**
 * Interface for procedures with one Object parameter.
 * @author Battlecruiser
 * @param <T> the type of object on which the procedure will be executed
 */

public interface IL2Procedure<T>
{
	/**
	 * Executes this procedure. A false return value indicates that the application executing this procedure should not invoke this procedure again.
	 * @param arg the object on which the procedure will be executed
	 * @return {@code true} if additional invocations of the procedure are allowed.
	 */
	boolean execute(T arg);
}
