# Max Flow Explainer: Ford-Fulkerson and Edmonds-Karp

## The problem

Think of a network of pipes. Each pipe only goes one way and has a limit on how much water it can carry each second. Water starts at one spot, the source `s`, and we want to get as much of it as possible to another spot, the sink `t`.

In graph terms, we have a directed graph where every edge has a capacity. A flow says how much goes along each edge, and it has to follow two rules:

1. An edge can't carry more than its capacity, and can't carry a negative amount.
2. Every vertex except `s` and `t` has to pass along everything it gets. Flow in = flow out.

The value of the flow is how much leaves `s`. The max flow problem is finding the biggest value possible.

## Residual graphs

The simple idea is: find a path from `s` to `t` that still has room on every edge, send as much as fits, and repeat until there are no paths left. The problem is that an early path can be a bad choice that blocks a better answer later. The fix is to let the algorithm undo flow it already sent.

The residual graph shows every change we're still allowed to make. If an edge `u → v` has capacity `c` and is carrying `f`, the residual graph has:

- a forward edge `u → v` with room `c − f`, because we can still send that much more, and
- a reverse edge `v → u` with room `f`, because we can take back up to `f` of the flow already on it.

Sending flow along a reverse edge means cancelling some flow on `u → v` and sending it somewhere else instead.

An augmenting path is a path from `s` to `t` in the residual graph where every edge has room left. Its bottleneck is the smallest room on the path. Sending the bottleneck along the path keeps the flow valid and makes it bigger by that amount.

## Ford-Fulkerson

```
start with 0 flow on every edge
while there is an augmenting path in the residual graph:
    b = the bottleneck of the path
    send b along every edge of the path
return the total sent
```

Why this gives the biggest flow: when the loop stops, look at all the vertices you can still reach from `s` in the residual graph and call that group `S`. The sink isn't in `S`, or there would still be a path. Every edge going out of `S` has to be full, because if it had room we could reach the vertex on the other end. Every edge coming back into `S` has to be empty, because otherwise its reverse edge would let us reach the vertex it starts from. So the flow is exactly the total capacity of the edges leaving `S`. Those edges are called a cut: if you deleted them, nothing could get from `s` to `t`. No flow can ever be bigger than a cut, because all of it has to cross the cut. So the flow is the biggest possible, and this cut is the smallest cut. This is called the max-flow min-cut theorem. My code finds this cut the same way, in `FlowNetwork.minCutSourceSide`.

Runtime: if the capacities are whole numbers, each path adds at least 1 to the flow. So there are at most F paths, where F is the max flow value. Each search is O(E), so Ford-Fulkerson is O(E · F). That depends on how big the capacities are, not just on how big the graph is. If capacities are in the millions, it can be very slow.

My Ford-Fulkerson finds paths with depth-first search (DFS). DFS takes the first edge it sees with room and keeps going deeper, and only backs up when it gets stuck.

## Edmonds-Karp

Edmonds-Karp is the exact same loop, but it finds paths with breadth-first search (BFS) instead. BFS always finds a shortest path, meaning the one with the fewest edges. That one change makes the runtime stop depending on the capacities:

- The distance (number of edges) from `s` to any vertex in the residual graph never goes down while the algorithm runs.
- Every time we send flow, at least one edge on the path gets filled up, the bottleneck edge. Before that same edge can be the bottleneck again, flow has to come back across it the other way first. That can only happen after the distance to the edge's starting vertex has gone up by at least 2. Distances can't go past V, so each edge can be the bottleneck about V/2 times at most.
- E edges, each the bottleneck at most V/2 times, means at most V·E/2 paths. Each BFS costs O(E), so the total is O(V · E²).

## Worked example

This exact graph is also a test in `HW6Test.kt`.

```
          3           2
    s ────────▶ a ────────▶ t
     \          │          ▲
    2 \       2 │         / 3
       ▼        ▼        /
        ──────▶ b ──────
```

Edges: `s→a` 3, `s→b` 2, `a→b` 2, `a→t` 2, `b→t` 3.

In the tables below, "room" is how much more each edge can take (`c − f`). "Undo" is how much of its flow could be taken back (`f`), which is the room on its reverse edge.

### Ford-Fulkerson (DFS)

DFS tries each vertex's edges in the order they were added. For `a` it tries `a→b` before `a→t`. For `b` it tries the reverse edge `b→a` before `b→t`.

Step 1. DFS goes `s → a`, then `a → b` because that's the first edge out of `a`, then `b → t`.
Path s → a → b → t. Bottleneck = smallest of (3, 2, 3) = 2. Total so far: 2.

| edge | s→a | s→b | a→b | a→t | b→t |
|---|---|---|---|---|---|
| flow / capacity | 2/3 | 0/2 | 2/2 | 0/2 | 2/3 |
| room | 1 | 2 | 0 | 2 | 1 |
| undo | 2 | 0 | 2 | 0 | 2 |

Step 2. `s → a` still has room 1. Out of `a`, the edge `a→b` is full, so DFS takes `a → t`.
Path s → a → t. Bottleneck = smallest of (1, 2) = 1. Total so far: 3.

| edge | s→a | s→b | a→b | a→t | b→t |
|---|---|---|---|---|---|
| flow / capacity | 3/3 | 0/2 | 2/2 | 1/2 | 2/3 |
| room | 0 | 2 | 0 | 1 | 1 |
| undo | 3 | 0 | 2 | 1 | 2 |

Step 3 (this is where the reverse edge matters). `s → a` is full now, so DFS goes `s → b`. The first edge in `b`'s list is the reverse edge `b → a`. It has room 2 because `a→b` is carrying 2. DFS takes it, then goes `a → t`.
Path s → b → a → t. Bottleneck = smallest of (2, 2, 1) = 1. Total so far: 4.
Going backwards over `a→b` cancels one unit that was going from a to b. That unit now leaves `a` through `a→t` instead, and `b` gets a unit straight from `s` to make up for it. The flow on `a→b` goes from 2 down to 1.

| edge | s→a | s→b | a→b | a→t | b→t |
|---|---|---|---|---|---|
| flow / capacity | 3/3 | 1/2 | 1/2 | 2/2 | 2/3 |
| room | 0 | 1 | 1 | 0 | 1 |
| undo | 3 | 1 | 1 | 2 | 2 |

Step 4. DFS goes `s → b` and tries `b → a` (room 1). But from `a`, every way out is either full or goes back to a vertex DFS already visited. So it backs up to `b` and takes `b → t`.
Path s → b → t. Bottleneck = smallest of (1, 1) = 1. Total so far: 5.

| edge | s→a | s→b | a→b | a→t | b→t |
|---|---|---|---|---|---|
| flow / capacity | 3/3 | 2/2 | 1/2 | 2/2 | 3/3 |
| room | 0 | 0 | 1 | 0 | 0 |
| undo | 3 | 2 | 1 | 2 | 3 |

Step 5. Both edges out of `s` are full, so DFS can't get anywhere and the algorithm stops. The max flow is 5, and it took 4 paths.

Checking the rules: `a` gets 3 in and sends 1 + 2 = 3 out. `b` gets 2 + 1 = 3 in and sends 3 out. Nothing goes over capacity.

The min cut: the only vertex still reachable from `s` is `s` itself. The edges leaving it are `s→a` (3) and `s→b` (2). They add up to 5, the same as the max flow, just like the theorem says.

### Edmonds-Karp (BFS) on the same graph

BFS looks at all the neighbours of a vertex before going further, so it finds the shortest paths first.

| step | path found | bottleneck | total |
|---|---|---|---|
| 1 | s → a → t (2 edges) | smallest of (3, 2) = 2 | 2 |
| 2 | s → b → t (2 edges) | smallest of (2, 3) = 2 | 4 |
| 3 | s → a → b → t (3 edges, the only path left) | smallest of (1, 2, 1) = 1 | 5 |

It gets the same answer, 5, but in 3 paths, and it never needs a reverse edge. On this graph, DFS's first path (the long one through `a→b`) was a bad choice that it later had to partly undo. BFS skipped that by taking short paths first. On big graphs with big capacities this difference gets huge. See `benchmark_report.md`.
