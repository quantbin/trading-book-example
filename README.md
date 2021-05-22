# trading-book-example
nanos per iteration: 7,541
nanos per parsing: 394

parsing input (e.g. 28800974 A g S 44.27 100) is not the slowest part.

java.util.TreeMap.get() is the worst offender: 65%.
