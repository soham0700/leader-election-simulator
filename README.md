# Leader Election Simulator

A small Java simulation of a **ring-based leader election algorithm** (in the style of Chang–Roberts), built for a distributed systems course at Binghamton University.

## The problem

In a distributed system with no central coordinator, nodes sometimes need to agree on a single "leader" (for example, to serialize writes or coordinate a task). Ring-based election algorithms solve this by arranging processes in a logical ring and passing messages around it until every process agrees on who has the highest id.

## How it works

- `N` participants are created, each with a unique id and its own thread.
- Every participant starts by sending its own id to its neighbor in the ring.
- When a participant receives an id:
  - If the received id is **greater** than its own, it forwards the message onward (it knows it can't win).
  - If the received id **equals** its own, it declares itself the leader — the message has traveled all the way around the ring.
  - If the received id is **smaller**, the message is dropped, since that participant's own (higher) id is already circulating and will eventually complete the loop instead.
- Eventually exactly one participant recognizes its own id coming back around and becomes the leader.

## Running it

```bash
javac LeaderElectionSystem.java
java LeaderElectionSystem        # defaults to 5 participants
java LeaderElectionSystem 8      # or specify how many
```

Each run interleaves differently since participants run concurrently on separate threads, but the participant with the highest id always wins.

## Notes

This is a teaching/simulation implementation (all participants run in-process on threads rather than over a real network), meant to make the message-passing logic of the algorithm easy to trace and reason about.
