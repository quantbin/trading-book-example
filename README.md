# trading-book-example
nanos per iteration: 7,541

nanos per parsing: 394

parsing input (e.g. 28800974 A g S 44.27 100) is not the slowest part.

java.util.TreeMap.get() is the worst offender: 55%.

processLimitOrder() is not even on the map. most of the time is spent in processMarketOrder(), which scans all price levels as it fills the order. 
java.util.TreeMap$NavigableSubMap$DescendingSubMapKeyIterator.next() and java.util.TreeMap$KeyIterator.next() take about 14%.
java.lang.String.split() takes about 8%.
