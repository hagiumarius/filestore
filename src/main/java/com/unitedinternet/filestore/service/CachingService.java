package com.unitedinternet.filestore.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

@Service
public class CachingService {

    Jedis jedis = new Jedis();

    public void setKeyValue(String key, String value) {
        jedis.set(key, value);
    }

    public String getValue(String key) {
        if (jedis.exists(key)) {
            return jedis.get(key);
        } else
            return null;
    }

    public void updateKey(String key, String newValue) {
        jedis.getSet(key, newValue);
    }

    public void addToList(String listName, String path) {
        jedis.lpush(listName, path);
    }

    public void removeFromList(String listName, String path) {
        jedis.lrem(listName, 1, path);
    }

    public void addToSet(String setName, String regex) {
        jedis.sadd(setName, regex);
    }

    public Set<String> getElementsFromSet(String setname) {
        return jedis.smembers(setname);
    }

    public List<String> getElementsFromList(String listname) {
        long listLength = jedis.llen(listname);
        return jedis.lrange(listname, 0, listLength);
    }

    public boolean exists(String key) {
        return jedis.exists(key);
    }

    public void cleanAll() {
        jedis.flushAll();
    }

}
