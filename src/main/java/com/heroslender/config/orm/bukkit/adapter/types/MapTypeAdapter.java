package com.heroslender.config.orm.bukkit.adapter.types;

import com.heroslender.config.orm.bukkit.adapter.BukkitTypeAdapter;
import com.heroslender.config.orm.bukkit.adapter.BukkitTypeAdapterFactory;
import com.heroslender.config.orm.common.adapter.TypeAdapter;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class MapTypeAdapter implements BukkitTypeAdapter<Map> {
    public static final MapTypeAdapter INSTANCE = new MapTypeAdapter();
    private static final Class<Map> CLAZZ = Map.class;

    private MapTypeAdapter() {
    }

    @Override
    public Class<Map> getType() {
        return CLAZZ;
    }

    @Override
    public Map from(String value) {
        return Collections.singletonMap(value, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map get(ConfigurationSection configurationSection, String path, Type type) {
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Map without types?!?");
        }

        ParameterizedType mapType = (ParameterizedType) type;
        Class<?> keyType = (Class<?>) mapType.getActualTypeArguments()[0];
        Type valType = mapType.getActualTypeArguments()[1];
        Class<?> valueType = (Class<?>) valType;

        TypeAdapter<?> KeyAdapter = BukkitTypeAdapterFactory.INSTANCE.getTypeAdapter(keyType);
        if (KeyAdapter == null) {
            System.out.println("No adapter found for " + keyType.getSimpleName());
            return Collections.emptyMap();
        }
        BukkitTypeAdapter<?> valueAdapter = BukkitTypeAdapterFactory.INSTANCE.getTypeAdapter(valueType);
        if (valueAdapter == null) {
            System.out.println("No adapter found for " + valueType.getSimpleName());
            return Collections.emptyMap();
        }

        ConfigurationSection section = configurationSection.getConfigurationSection(path);
        Map map = new HashMap();
        for (String key : section.getKeys(false)) {
            map.put(KeyAdapter.from(key), valueAdapter.get(section, key, valType));
        }

        return map;
    }

    @Override
    public void save(ConfigurationSection configuration, String path, Object defaultValue, Type type) {
        save(configuration, path, defaultValue, type, true);
    }

    @Override
    public void saveDefault(ConfigurationSection configuration, String path, Object defaultValue, Type type) {
        save(configuration, path, defaultValue, type, false);
    }

    private void save(ConfigurationSection configuration, String path, Object defaultValue, Type type, boolean forced) {
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Map without types?!?");
        }

        if (defaultValue == null) {
            defaultValue = Collections.emptyMap();
        }

        Type valType = ((ParameterizedType) type).getActualTypeArguments()[1];
        Class<?> valueType = (Class<?>) valType;
        BukkitTypeAdapter<?> valueAdapter = BukkitTypeAdapterFactory.INSTANCE.getTypeAdapter(valueType);
        if (valueAdapter == null) {
            System.out.println("No adapter found for " + valueType.getSimpleName());
            return;
        }

        ConfigurationSection section = configuration.createSection(path);
        Map map = (Map) defaultValue;
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;

            if (forced) {
                valueAdapter.save(section, entry.getKey().toString(), entry.getValue(), valType);
            } else {
                valueAdapter.saveDefault(section, entry.getKey().toString(), entry.getValue(), valType);
            }
        }
    }

    @Override
    public Map get(ConfigurationSection configuration, String path) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void saveDefault(ConfigurationSection configuration, String path, Object defaultValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
