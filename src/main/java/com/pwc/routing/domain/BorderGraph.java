package com.pwc.routing.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Immutable adjacency view of the countries dataset. A fresh BFS runs on
 * every {@link #findRoute} call; nothing is precomputed or cached beyond the
 * adjacency lists themselves.
 */
public final class BorderGraph {

    private final Map<String, List<String>> neighbours;

    private BorderGraph(Map<String, List<String>> neighbours) {
        this.neighbours = neighbours;
    }

    public static BorderGraph of(List<Country> countries) {
        Map<String, List<String>> neighbours = new HashMap<>();
        for (Country country : countries) {
            // Sorted adjacency makes BFS deterministic: among equally short
            // routes the alphabetically first one wins.
            neighbours.put(country.cca3(), country.borders().stream().sorted().toList());
        }
        return new BorderGraph(Map.copyOf(neighbours));
    }

    /**
     * Shortest land route from {@code origin} to {@code destination} as an
     * inclusive list of {@code cca3} codes, found by breadth-first search.
     */
    public List<String> findRoute(String origin, String destination) {
        requireKnown(origin);
        requireKnown(destination);

        Map<String, String> parent = new HashMap<>();
        parent.put(origin, null);
        Queue<String> queue = new ArrayDeque<>();
        queue.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(destination)) {
                return pathTo(current, parent);
            }
            for (String neighbour : neighbours.getOrDefault(current, List.of())) {
                if (!parent.containsKey(neighbour)) {
                    parent.put(neighbour, current);
                    queue.add(neighbour);
                }
            }
        }
        throw new NoLandRouteException(origin, destination);
    }

    private void requireKnown(String cca3) {
        if (!neighbours.containsKey(cca3)) {
            throw new UnknownCountryCodeException(cca3);
        }
    }

    private static List<String> pathTo(String destination, Map<String, String> parent) {
        List<String> path = new ArrayList<>();
        for (String step = destination; step != null; step = parent.get(step)) {
            path.add(step);
        }
        return path.reversed();
    }
}
