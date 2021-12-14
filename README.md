package \
  ------- grafo \
          BatchAnalysis: java test class that tries to execute multiple times the algorithm for different parameters, in a batch. \
          DatabaseConnection: optional java test class that tries to store data in a MySql database. \
          GraphComparator: java class used to compare two graph (that should be a Log Graph)\
          GraphLogAnalyzer: java class used to analyze multiple traces to build the graph\
          -test\
          LogUtilsRepeatingGraph: The Graph version of old Log Utils. load the xes, build the graph for each log and construct the matrix of graphcompare. The graph are considered Repeating Graph, accepting repeating string patterns\
          Trace: support class for trace, containing single activities or a "traceLine"\
          TraceRepeatingEdgeInfo: support class to maintain the info of an edge (es. how many repetitions)\
 -------- sottostringhe\
          LogStringOccurrence: Under Development\
          -test\
          LogUtilisRepeatingTrace: The String version of old LogUtils. load the xes, build the RepeatingSet for each log and performs a "Cut" of this set.\
          TraceAnalyzer: java class used to analyze a Trace and build a Repeating Set\
          -test
