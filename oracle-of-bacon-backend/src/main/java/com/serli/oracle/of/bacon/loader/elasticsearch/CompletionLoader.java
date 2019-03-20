package com.serli.oracle.of.bacon.loader.elasticsearch;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletionLoader {
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) throws IOException, InterruptedException {
        RestHighLevelClient client = ElasticSearchRepository.createClient();

        if (args.length != 1) {
            System.err.println("Expecting 1 arguments, actual : " + args.length);
            System.err.println("Usage : completion-loader <actors file path>");
            System.exit(-1);
        }
        
        PutMappingRequest request = new PutMappingRequest("actor");
        request.source(
        	    "{\n" +
        	    "  \"properties\": {\n" +
        	    "    \"suggest\": {\n" +
        	    "      \"type\": \"completion\"\n" +
        	    "    },\n" +
        	    "    \"name\": \"text\"\n" +
        	    "  }\n" +
        	    "}", 
        	    XContentType.JSON);

        String inputFilePath = args[0];
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            bufferedReader
                    .lines()
                    .forEach(line -> {
                        //TODO ElasticSearch insert
                        System.out.println(line);
                    });
        }

        System.out.println("Inserted total of " + count.get() + " actors");

        client.close();
    }
}
