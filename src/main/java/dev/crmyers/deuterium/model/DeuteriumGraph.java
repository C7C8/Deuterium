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

package dev.crmyers.deuterium.model;

import com.google.common.collect.Sets;
import com.google.common.graph.*;
import dev.crmyers.deuterium.command.EditNodeCommand;
import dev.crmyers.deuterium.model.exception.CycleException;
import dev.crmyers.deuterium.model.exception.DependencyException;
import lombok.*;

import java.util.*;

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
	List<EditNodeCommand> history = new ArrayList<>();

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
		final MutableGraph<Node> dependencyGraph = Graphs.inducedSubgraph(graph, Graphs.reachableNodes(graph, dependent));
		final Set<Node> cycleNodes = findCycleBranchedFrom(dependencyGraph, dependent);
		if (!cycleNodes.isEmpty())
			throw new CycleException("Cycle detected", cycleNodes);

		final Stack<Node> stack = new Stack<>();
		final LinkedHashSet<Node> visited = new LinkedHashSet<>();

		// Depth-first search
		stack.push(dependent);
		while (!stack.empty()) {
			final Node top = stack.pop();
			visited.add(top);

			for (Node node : dependencyGraph.successors(top)) {
				if (!visited.contains(node))
					stack.push(node);
			}
		}

		// This DFS returns the nodes in inverse order; this reverses them.
		final ArrayList<Node> ret = new ArrayList<>(visited);
		Collections.reverse(ret);
		return ret;
	}

	/**
	 * Find a cycle in the graph that can be found by branching off the given node. Cycles do not necessarily have to
	 * involve the given node, the cycle just has to be accessible from it. Ex. A-> B <-> C will return (B, C) when run
	 * on A because the B/C cycle is accessible from A.
	 * @param graph Graph.
	 * @param node Node to search from.
	 * @return Set containing nodes involved in the cycle; if no cycle exists, the set will be empty.
	 */
	private static Set<Node> findCycleBranchedFrom(final Graph<Node> graph, final Node node) {
		Queue<Node> queue = new ArrayDeque<>();
		Set<Node> visited = new HashSet<>();
		visited.add(node);
		queue.add(node);

		// Breadth-first search
		boolean start = true;
		while (!queue.isEmpty()) {
			Node v = queue.remove();
			for (Node w : graph.successors(v)) {
				// Found a cycle!
				if (!start && node.equals(w)) // hack to make sure the algorithm works on the first iteration
					return visited;

				if (!visited.contains(w)) {
					visited.add(w);
					queue.add(w);
				}
				else {
					// Something weird is going on, we've re-encountered a node, so somehow this node is involved in
					// a cycle. Re-run the search from this node, but on a graph that contains only those nodes accessible
					// from this one.
					return findCycleBranchedFrom(Graphs.inducedSubgraph(graph, Graphs.reachableNodes(graph, w)), w);
				}
			}
			start = false;
		}

		// No cycle found!
		return Collections.emptySet();
	}

	/**
	 * Find all nodes reachable by this node that cannot be accessed by any other. That is, find all nodes that, if you
	 * were to remove the given node, would form one or more disjoint subgraphs of the original graph. Think of this
	 * function as a way to find choke points.
	 * @param node Node to start search from
	 * @return Set of nodes exclusively dependent on the given node. If there are none, the set will be empty.
	 */
	public Set<Node> findAllExclusivelyDependentOn(final Node node) {
		// Obtain the topologically-sorted dependency list for this node and iterate through it; anything that has a
		// dependency not already listed in the dependency list is removed. This has the effect of pruning any direct
		// children of it, too, since their parent will have just been removed.
		final List<Node> dependencies = solveDependencies(node);
		Collections.reverse(dependencies);
		final HashSet<Node> ret = new HashSet<>(dependencies);
		for (Node successor : dependencies) {
			if (successor.equals(node))
				continue;
			if (!Sets.difference(graph.predecessors(successor), ret).isEmpty())
				ret.remove(successor);
		}
		ret.remove(node);
		return ret;
	}

	/**
	 * Find the shortest path from one node to another.
	 * @param from Start node
	 * @param to End node
	 * @return Ordered list of nodes to traverse, starting with from and ending with to.
	 * @throws DependencyException Thrown if no path can be found.
	 */
	public List<Node> shortestPath(final Node from, final Node to) throws DependencyException {
		Queue<Node> queue = new ArrayDeque<>();
		Set<Node> visited = new HashSet<>();
		HashMap<Node, Node> parents = new HashMap<>();
		visited.add(from);
		queue.add(from);

		while (!queue.isEmpty()) {
			Node v = queue.remove();
			boolean finished = false;
			for (Node w : graph.successors(v)) {
				// Found "to"!
				if (w.equals(to)) {
					finished = true;
					parents.put(to, w);
					parents.put(w, v);
					break;
				}
				if (!visited.contains(w)) {
					visited.add(w);
					queue.add(w);
					parents.put(w, v);
				}
			}
			if (finished)
				break;
		}

		if (!parents.containsKey(to))
				throw new DependencyException("No path to node");

		ArrayList<Node> ret = new ArrayList<>();
		Node current = to;
		while (parents.containsKey(current)) {
			ret.add(current);
			current = parents.get(current);
		}
		ret.add(from);
		Collections.reverse(ret);
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
