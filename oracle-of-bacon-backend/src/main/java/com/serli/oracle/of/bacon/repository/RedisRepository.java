package com.serli.oracle.of.bacon.repository;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisRepository {
    private final Jedis jedis;

    public RedisRepository() {
        this.jedis = new Jedis("localhost");
    }

    public List<String> getLastTenSearches() {
        List<String> listSearches = jedis.lrange("searches", 0, 9);
        return listSearches;
    }

    public void addSearches(String actorName) {
        jedis.lpush("searches", actorName);
    }
}
