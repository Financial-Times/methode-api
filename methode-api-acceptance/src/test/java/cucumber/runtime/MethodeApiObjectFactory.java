package cucumber.runtime;

import com.ft.methodeapi.SetUpHelper;
import com.ft.methodeapi.acceptance.AcceptanceTestConfiguration;
import cucumber.runtime.java.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * This is loaded automagically by Cucumber, as long as there is only one implementation of ObjectFactory in runtime.
 * Consult the expert if you have more questions: Neil Green.
 */
public class MethodeApiObjectFactory implements ObjectFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeApiObjectFactory.class);
    public static final String CONFIG_FILE_PROPERTY_NAME = "test.methodeApi.configFile";

    private final Set<Class<?>> classes = new HashSet<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private AcceptanceTestConfiguration configuration;

    public void start() {
        LOGGER.debug("starting");
        configuration = SetUpHelper.readConfiguration();
    }

    public void stop() {
        instances.clear();
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        T instance = type.cast(instances.get(type));
        if (instance == null) {
            instance = cacheNewInstance(type);
        }
        return instance;
    }

    private <T> T cacheNewInstance(Class<T> type) {
        T t;
        try {
            t = create(type);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new CucumberException(format("Failed to instantiate %s", type), e);
        }
        instances.put(type, t);
        return t;
    }

    private <T> T create(Class<T> type) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T t;
        try {
            t = createWithConfig(type);
        } catch (NoSuchMethodException exWithConfig) {
            try {
                t = createWithoutConfig(type);
            } catch (NoSuchMethodException exWithoutConfig) {
                throw new CucumberException(format("%s doesn't have a suitable constructor. " +
                        "There must be a constructor with a param MethodeBridgeAcceptanceTestConfiguration, or a no-arg constructor", type), exWithoutConfig);
            }
        }
        return t;
    }

    private <T> T createWithConfig(Class<T> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = type.getConstructor(AcceptanceTestConfiguration.class);
        return constructor.newInstance(configuration);
    }

    private <T> T createWithoutConfig(Class<T> type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = type.getConstructor();
        return constructor.newInstance();
    }

}
