package com.serli.oracle.of.bacon.repository;


import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "pass2606"));
    }

    public List<GraphItem> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();
        Transaction transaction =session.beginTransaction(); 
        StatementResult result = transaction.run(
        			"MATCH (Bacon:Actor { name: 'Bacon, Kevin (I)'}), ( Actor: Actor {name: '" + actorName + "'}), "
        			+ "result = shortestPath((Bacon)-[:PLAYED_IN*]-(Actor)) "
        			+ "RETURN result"
        		);
        List<GraphItem> returnValue = result.list().stream()
			.flatMap(record -> record.values().stream().map(Value::asPath))
			.flatMap(path -> getGraphItems(path).stream())
			.collect(Collectors.toList());
        System.out.println(returnValue);
        session.close();
        return returnValue;
    }
    
    public List<GraphItem> getGraphItems(Path path) {
    	List<GraphItem> graphItems = new ArrayList<GraphItem>();
    	getGraphNodes(path.nodes(), graphItems);
    	getGraphRelationships(path.relationships(), graphItems);
    	return graphItems;
    }
    
    private void getGraphRelationships(Iterable<Relationship> relationships, List<GraphItem> graphItems) {
    	Iterator<Relationship> iterator = relationships.iterator();
        while(iterator.hasNext()) {
            Relationship relationship = iterator.next();
            GraphEdge edge = new GraphEdge(relationship.id(), relationship.startNodeId(), relationship.endNodeId(), relationship.type());
            graphItems.add(edge);
        }
	}

	private void getGraphNodes(Iterable<Node> nodes, List<GraphItem> graphItems) {
		Iterator<Node> iterator = nodes.iterator();
        while(iterator.hasNext()) {
            Node currentNode = iterator.next();
            String type = currentNode.labels().iterator().next();
            String value = type.equals("Actor") ? "name" : "title";
            GraphNode node = new GraphNode(currentNode.id(), currentNode.get(value).asString(), type);
            graphItems.add(node);
        }		
	}

    public static abstract class GraphItem {
        public final long id;

        private GraphItem(long id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphItem graphItem = (GraphItem) o;

            return id == graphItem.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class GraphNode extends GraphItem {
        public final String type;
        public final String value;

        public GraphNode(long id, String value, String type) {
            super(id);
            this.value = value;
            this.type = type;
        }
    }

    private static class GraphEdge extends GraphItem {
        public final long source;
        public final long target;
        public final String value;

        public GraphEdge(long id, long source, long target, String value) {
            super(id);
            this.source = source;
            this.target = target;
            this.value = value;
        }
    }
}
