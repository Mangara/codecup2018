# CodeCup 2018 Bot

This is my code for the [2018 CodeCup](https://www.codecup.nl/intro.php). My final submission came in 14th out of 109 competitors.

## Strategy

I implemented a bunch of different players for comparison, but the final submission was an iterative deepening aspiration search with a number of standard optimizations, such as killer moves and a transposition table with Zobrist hashing (see [IterativeDFSPlayer](src/codecup2018/player/IterativeDFSPlayer.java)).

**Endgame** Once all free spots are holes (spots without free neighbours), the optimal strategy is simply to fill the worst hole each turn, and the search can stop.

**Evaluation** Partial game states were evaluated by computing the exact expected value of a completely random playout (see [ExpectedValue](src/codecup2018/evaluator/ExpectedValue.java) and [IncrementalExpectedValue](src/codecup2018/evaluator/IncrementalExpectedValue.java)).

**Move Generation** The search only considered two kinds of moves: playing the lowest value tile in the hole that most favored the opponent, or playing the highest-value tile in a non-hole, trying positions with more free neighbours first (see [BucketSortMaxMovesOneHole](src/codecup2018/movegenerator/BucketSortMaxMovesOneHole.java)).

**Time Control** Since the final moves take so little time, and the game can be decided quite early, I allocated more time to early moves than later moves, falling off drastically where most games reached the endgame (see [ProportionalController.LINEAR_DECAY](src/codecup2018/timecontrol/ProportionalController.java)).

## Build

To build the project, run `ant jar`, or open it with a recent version of [NetBeans](https://netbeans.org/).

`Runner` is the entry point for the competition player, `GameHost` plays a game between different players, and `Tournament` can play a full tournament between multiple players.

## Authors

* **Sander Verdonschot** - [Mangara](https://bitbucket.org/Mangara/)

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details
