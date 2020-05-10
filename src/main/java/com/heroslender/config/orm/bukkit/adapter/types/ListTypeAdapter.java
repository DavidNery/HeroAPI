package com.heroslender.config.orm.bukkit.adapter.types;

import com.heroslender.config.orm.bukkit.adapter.BukkitTypeAdapter;
import com.heroslender.config.orm.bukkit.adapter.BukkitTypeAdapterFactory;
import com.heroslender.config.orm.common.adapter.TypeAdapter;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ListTypeAdapter implements BukkitTypeAdapter<List> {
    public static final ListTypeAdapter INSTANCE = new ListTypeAdapter();
    private static final Class<List> CLAZZ = List.class;

    private ListTypeAdapter() {
    }

    @Override
    public Class<List> getType() {
        return CLAZZ;
    }

    @Override
    public List from(String value) {
        return Collections.singletonList(value);
    }

    @Override
    public List get(ConfigurationSection configuration, String path) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List get(ConfigurationSection configurationSection, String path, Type type) {
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("List without entry type?!?");
        }

        ParameterizedType stringListType = (ParameterizedType) type;
        Class<?> entryType = (Class<?>) stringListType.getActualTypeArguments()[0];

        TypeAdapter<?> adapter = BukkitTypeAdapterFactory.INSTANCE.getTypeAdapter(entryType);
        if (adapter == null) {
            System.out.println("No adapter found for " + entryType.getSimpleName());
            return Collections.emptyList();
        }

        List list = new ArrayList();
        for (String s : configurationSection.getStringList(path)) {
            list.add(adapter.from(s));
        }
        return list;
    }

    @Override
    public void saveDefault(ConfigurationSection configuration, String path, Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = Collections.emptyList();
        }

        save(configuration, path, defaultValue);
    }
}
