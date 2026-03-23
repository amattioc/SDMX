/* Copyright 2010,2014 Bank Of Italy
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package it.bancaditalia.oss.sdmx.util;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class Cache<V> {
    private Map<String, V> storage;

    public Cache() {
        this.storage = new ConcurrentHashMap<>();
    }

    /**
     * get() will retrieve a stored value associated with the given key, null otherwise.
     * get does not differentiate between the case in which the key exists but the stored value is null and the case
     * where the key is not present inside the Cache: use contains() method to differentiate between these two cases.
     * @param key
     * @return if the key is present inside the Cache, return the value associated, null otherwise.
     */
    public V get(String key) {
        return storage.get(key);
    }

    /**
     * put() will insert a new (Key, Value) entry.
     * @param key
     * @param v
     */
    public void put(String key, V v) {
        storage.put(key, v);
    }

    /**
     * contains() will check if the given key is contained inside the cache.
     * @param key
     * @return true if key is contained in the cache, false otherwise.
     */
    public boolean contains(String key) {
        return storage.containsKey(key);
    }

    /**
     * clear() will empty the cache.
     */
    public void clear() {
        storage.clear();
    }

    /**
     * join the given arguments with "::" as a delimeter into a single key, null values are skipped.
     * @param args
     * @return
     */
    public static String toKey(String... args) {
        if (args == null)
            return "";
        else
            return Arrays.stream(args).filter(s -> s != null).collect(Collectors.joining("::"));

    }
}
