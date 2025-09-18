package com.github.egubot.managers.reactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.github.egubot.interfaces.ReactionInterceptor;

public class ReactionRegistry {
	private static final Logger logger = LogManager.getLogger(ReactionRegistry.class.getName());
	private static ReactionRegistry instance;
	private final List<ReactionInterceptor> interceptors = new ArrayList<>();

	private ReactionRegistry() {
		loadInterceptors();
	}

	public static synchronized ReactionRegistry getInstance() {
		if (instance == null) {
			instance = new ReactionRegistry();
		}
		return instance;
	}

	private void loadInterceptors() {
		Reflections reflections = new Reflections("com.github.egubot.reactions", Scanners.SubTypes);

		Set<Class<? extends ReactionInterceptor>> interceptorClasses = reflections.getSubTypesOf(ReactionInterceptor.class);

		for (Class<? extends ReactionInterceptor> interceptorClass : interceptorClasses) {
			try {
				ReactionInterceptor interceptorInstance = interceptorClass.getDeclaredConstructor().newInstance();
				registerInterceptor(interceptorInstance);
			} catch (Exception e) {
				logger.warn("Failed to load reaction interceptor: {}", interceptorClass.getSimpleName());
				logger.error(e);
			}
		}

		interceptors.sort((i1, i2) -> Integer.compare(i1.getPriority(), i2.getPriority()));
	}

	private void registerInterceptor(ReactionInterceptor interceptor) {
		interceptors.add(interceptor);
		logger.debug("Registered reaction interceptor: {}", interceptor.getName());
	}

	public List<ReactionInterceptor> getInterceptors() {
		return new ArrayList<>(interceptors);
	}
}