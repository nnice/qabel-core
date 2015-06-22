package de.qabel.core.module;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.ResourceActor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;

/**
 * The ModuleManager is responsible for loading and starting Modules as well as
 * stopping and removing them.
 */
public class ModuleManager {
	public static ClassLoader LOADER;

	private final EventEmitter eventEmitter;
	private final ResourceActor resourceActor;

	private HashMap<Module, ModuleThread> modules;


	public ModuleManager(EventEmitter emitter, ResourceActor resourceActor, ClassLoader classloader) {
		eventEmitter = emitter;
		modules = new HashMap<>();
		this.resourceActor = resourceActor;
		LOADER = classloader;
	}

	EventEmitter getEventEmitter() {
		return eventEmitter;
	}

	public ResourceActor getResourceActor() {
		return resourceActor;
	}

	/**
	 * Starts a given Module by its class
	 * @param module Module to start.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T extends Module> T startModule(Class<T> module) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		T m = module.getConstructor(ModuleManager.class).newInstance(this);
		m.init();
        ModuleThread t = new ModuleThread(m);
		modules.put(m, t);
		t.start();
		return m;
	}


	/**
	 * Shuts down all Modules
	 */
	public void shutdown() {
		while(!modules.isEmpty()) {
			modules.values().iterator().next().getModule().stopModule();
		}
	}

	public void removeModule(Module module) {
		modules.remove(module);
	}

	/**
	 * Wraps the EventListener on method. Allows to
	 * force unique namespaces for modules.
	 * @param event Event name to register for
	 * @param module Module to add as EventListener
	 * @return True if successfully registered
	 */
	public boolean on(String event, Module module) {
		if (true) { //TODO: This could enforce unique names and disallow registering to certain events.
			module.doOn(event, module);
			return true;
		}
		return false;
	}
}
