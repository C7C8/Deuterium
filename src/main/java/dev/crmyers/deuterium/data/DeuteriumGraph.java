/*
 * Copyright (c) 2019 Christopher Myers
 *
 * This file is part of Deuterium.
 *
 * Deuterium is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Deuterium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Deuterium.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.crmyers.deuterium.data;

import com.google.common.graph.*;
import dev.crmyers.deuterium.data.exception.CycleException;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to represent a Deuterium graph. Implements the MutableGraph interface but delegates to a Guava graph.
 */
@SuppressWarnings({"UnstableApiUsage", "NullableProblems"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeuteriumGraph implements MutableGraph<Node> {
	UUID id;
	String name;
	String description;
	Map<UUID, Node> nodes = new HashMap<>();
	List<NodeHistory> history = new ArrayList<>();

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	MutableGraph<Node> graph = GraphBuilder.directed().build();

	/**
	 * Determines whether the given node has the given dependency
	 * @param node Node to check on
	 * @param dependency Potential dependency
	 * @return Whether the node has dependency in its successor list
	 */
	public boolean dependsOn(final Node node, final Node dependency) {
		return successors(node).contains(dependency);
	}

	/**
	 * For a given dependent node, determine all dependencies and return a topological sort of them.
	 * @return Topologically sorted set of node's dependencies, with dependent at the end. Note that this is NOT the
	 * same as a topological sort of the graph!
	 * @throws CycleException If a cycle is detected in the node's dependency tree; the exception will contain a set of
	 * nodes involved in the cycle.
	 */
	public List<Node> solveDependencies(final Node dependent) throws CycleException {
		// Run cycle detection on a graph formed from only this node plus its successors
		MutableGraph<Node> dependencyGraph = Graphs.copyOf(graph);
		Set<Node> dependencies = Graphs.reachableNodes(graph, dependent);
		for (Node node : graph.nodes().stream().filter(n -> !dependencies.contains(n)).collect(Collectors.toList()))
			dependencyGraph.removeNode(node);
		if (Graphs.hasCycle(dependencyGraph))
			throw new CycleException("Cycle detected");

		Stack<Node> stack = new Stack<>();
		Stack<Node> sorted = new Stack<>();
		HashSet<Node> visited = new HashSet<>();

		stack.push(dependent);
		while (!stack.empty()) {
			final Node top = stack.pop();
			if (!visited.contains(top)) {
				sorted.push(top);
				visited.add(top);
			}

			for (Node node : dependencyGraph.successors(top)) {
				if (!visited.contains(node))
					stack.push(node);
			}
		}

		// This DFS returns the nodes in inverse order; this reverses them.
		ArrayList<Node> ret = new ArrayList<>(sorted.size());
		while (!sorted.empty())
			ret.add(sorted.pop());
		return ret;
	}


	// Methods from MutableGraph<Node> that delegate to the contained graph object

	@Override
	public Set<Node> nodes() {
		return graph.nodes();
	}

	@Override
	public Set<EndpointPair<Node>> edges() {
		return graph.edges();
	}

	@Override
	public boolean isDirected() {
		return graph.isDirected();
	}

	@Override
	public boolean allowsSelfLoops() {
		return graph.allowsSelfLoops();
	}

	@Override
	public ElementOrder<Node> nodeOrder() {
		return graph.nodeOrder();
	}

	@Override
	public Set<Node> adjacentNodes(Node node) {
		return graph.adjacentNodes(node);
	}

	@Override
	public Set<Node> predecessors(Node node) {
		return graph.predecessors(node);
	}

	@Override
	public Set<Node> successors(Node node) {
		return graph.successors(node);
	}

	@Override
	public Set<EndpointPair<Node>> incidentEdges(Node node) {
		return graph.incidentEdges(node);
	}

	@Override
	public int degree(Node node) {
		return graph.degree(node);
	}

	@Override
	public int inDegree(Node node) {
		return graph.inDegree(node);
	}

	@Override
	public int outDegree(Node node) {
		return graph.outDegree(node);
	}

	@Override
	public boolean hasEdgeConnecting(Node nodeU, Node nodeV) {
		return graph.hasEdgeConnecting(nodeU, nodeV);
	}

	@Override
	public boolean hasEdgeConnecting(EndpointPair<Node> endpoints) {
		return graph.hasEdgeConnecting(endpoints);
	}

	@Override
	public boolean addNode(Node node) {
		return graph.addNode(node);
	}

	@Override
	public boolean putEdge(Node nodeU, Node nodeV) {
		return graph.putEdge(nodeU, nodeV);
	}

	@Override
	public boolean putEdge(EndpointPair<Node> endpoints) {
		return graph.putEdge(endpoints);
	}

	@Override
	public boolean removeNode(Node node) {
		return graph.removeNode(node);
	}

	@Override
	public boolean removeEdge(Node nodeU, Node nodeV) {
		return graph.removeEdge(nodeU, nodeV);
	}

	@Override
	public boolean removeEdge(EndpointPair<Node> endpoints) {
		return graph.removeEdge(endpoints);
	}
}
