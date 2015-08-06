package dwo.gameserver.handler;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handler manager manages command handlers: string and int commands sent by client.
 * Definition of manager contains definitions for type of command handlers and type of command itself.
 * Manager allows to register, remove and lookup handlers.
 *
 * Instances of handler manager must define {link @HandlerList} annotation on self where list of all command handlers is described.
 * Handlers itself must define {link @TextCommand} for each command they implements.
 * {link @TextCommand} annotation placed on method that implements command.
 * Each method implementing command should receive {link @HandlerParams} argument for catching default params sent to handler from core.
 *
 * See manager & handler implementations for more info and experience.
 *
 * @param <TCommandType> Command type.
 * @param <THandlerType> Handler type.
 *
 * @author Yorie
 */
public class HandlerManager<TCommandType, THandlerType extends ICommandHandler<TCommandType>> implements IHandlerManager<TCommandType, THandlerType>
{
	protected final Logger log = LogManager.getLogger(getClass());

	protected final Map<TCommandType, THandlerType> handlers = new FastMap<>();
	protected final Map<TCommandType, Method> methods = new FastMap<>();
	protected final Set<TCommandType> disabledCommands = new FastSet<>();

	protected HandlerManager()
	{
		if(!getClass().isAnnotationPresent(HandlerList.class))
		{
			log.error("Class [" + getClass().getName() + "] should be annotated with [" + HandlerList.class.getName() + "]!.");
		}
	}

	/**
	 * Receives handler class type and tries to create instance of it.
	 *
	 * @param cls Handler class.
	 * @return Handler instance.
	 */
	@Nullable
	protected THandlerType getHandlerInstance(Class<? extends THandlerType> cls)
	{
		try
		{
			return cls.getConstructor().newInstance();
		}
		catch(ClassCastException e)
		{
			log.error("The class [" + cls.getName() + "] is not a subclass of [" + ICommandHandler.class.getSimpleName() + "].");
		}
		catch(NoSuchMethodException e)
		{
			log.error("Seems that given handler [" + cls.getName() + "] does not have default constructor. Cannot initiate handler.");
		}
		catch(IllegalAccessException e)
		{
			log.error("Seems that default constructor of handler [" + cls.getName() + "] is private or protected. Cannot initiate.");
		}
		catch(InstantiationException e)
		{
			log.error("Oh, trying to initiate an abstract class [" + cls.getName() + "]? Failed.");
		}
		catch(InvocationTargetException | ExceptionInInitializerError e)
		{
			log.error("Invoking of constructor for handler [" + cls.getName() + "] produced error.", e);
		}
		catch(Exception e)
		{
			log.error("Failed to instantiate handler of class [" + cls.getName() + "].", e);
		}

		return null;
	}

	protected List<Method> getAnnotatedMethods(Class<? extends ICommandHandler<TCommandType>> handlerClass, Class<? extends Annotation> annotationClass)
	{
		List<Method> methods = new FastList<>();
		for(Method method : handlerClass.getDeclaredMethods())
		{
			if(method.isAnnotationPresent(annotationClass))
			{
				methods.add(method);
			}
		}

		return methods;
	}

	protected void addHandler(TCommandType command, THandlerType handler, Method method)
	{
		handlers.put(command, handler);
		methods.put(command, method);
	}

	@Override
	public int size()
	{
		return handlers.size();
	}

	@Override
	public void removeCommand(TCommandType command)
	{
		handlers.remove(command);
		methods.remove(command);
	}

	@Override
	public void disableCommand(TCommandType command)
	{
		disabledCommands.add(command);
	}

	@Override
	public void enableCommand(TCommandType command)
	{
		disabledCommands.remove(command);
	}

	@Override
	public boolean isCommandEnabled(TCommandType command)
	{
		return disabledCommands.contains(command);
	}

	@Override
	public boolean execute(HandlerParams<TCommandType> params)
	{
		TCommandType command = params.getCommand();

		if(isCommandEnabled(command))
		{
			log.info("Execution of disabled command [" + command + "] prevented. Please, enable command and try again.");
			return true;
		}

		ICommandHandler<TCommandType> handler = handlers.get(command);
		Method handlerMethod = methods.get(command);

		if(handler == null || handlerMethod == null)
		{
			log.warn("Client trying to execute unknown command [" + params.getCommand() + "].");
			return false;
		}

		if(!handler.isActive())
		{
			log.info("Execution of inactive handler of command [" + command + "] prevented.");
			return true;
		}

		try
		{
			return Boolean.TRUE.equals(handlerMethod.invoke(handler, params));
		}
		catch(Exception e)
		{
			log.error("Failed to execute command [" + command + "].", e);
			return true;
		}
	}
}
