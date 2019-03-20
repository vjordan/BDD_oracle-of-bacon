package com.serli.oracle.of.bacon.api;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import com.serli.oracle.of.bacon.repository.MongoDbRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository.GraphItem;
import com.serli.oracle.of.bacon.repository.RedisRepository;

import io.github.lukehutch.fastclasspathscanner.utils.Log;
import net.codestory.http.annotations.Get;
import net.codestory.http.convert.TypeConvert;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIEndPoint {
    private final Neo4JRepository neo4JRepository;
    private final ElasticSearchRepository elasticSearchRepository;
    private final RedisRepository redisRepository;
    private final MongoDbRepository mongoDbRepository;

    public APIEndPoint() {
        neo4JRepository = new Neo4JRepository();
        elasticSearchRepository = new ElasticSearchRepository();
        redisRepository = new RedisRepository();
        mongoDbRepository = new MongoDbRepository();
    }

    @Get("bacon-to?actor=:actorName")
    public String getConnectionsToKevinBacon(String actorName) {

        redisRepository.addSearches(actorName);

    	List<GraphItem> connections = neo4JRepository.getConnectionsToKevinBacon(actorName);
    	System.out.println(connections.toString());
    	
        List<Map<String, GraphItem>> res = new ArrayList<Map<String, GraphItem>>();
        for (GraphItem values : connections) {
            Map<String, GraphItem> map = new HashMap<String, GraphItem>();
            map.put("data", values);
            res.add(map);
        }
    	
        return TypeConvert.toJson(res);
    }

    @Get("suggest?q=:searchQuery")
    public List<String> getActorSuggestion(String searchQuery) throws IOException {
        return Arrays.asList("Niro, Chel",
                "Senanayake, Niro",
                "Niro, Juan Carlos",
                "de la Rua, Niro",
                "Niro, Simão");
    }

    @Get("last-searches")
    public List<String> last10Searches() {
        return redisRepository.getLastTenSearches();
    }

    @Get("actor?name=:actorName")
    public String getActorByName(String actorName) {
        return mongoDbRepository.getActorByName(actorName).get().toJson();
    }
}
