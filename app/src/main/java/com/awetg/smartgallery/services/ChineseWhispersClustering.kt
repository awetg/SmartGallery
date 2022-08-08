package com.awetg.smartgallery.services

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

class ChineseWhispersClustering(encodingHashMap: HashMap<String, FloatArray>, private val encodingDim: Int) {
    private val numEncodings:Int
    private val fileNames: Array<String>
    private val encodings: Array<FloatArray>
    private val graph: Graph

    init {
        numEncodings = encodingHashMap.size
        fileNames = encodingHashMap.keys.toTypedArray()
        encodings = encodingHashMap.values.toTypedArray()
        graph = Graph(numEncodings)
    }

    companion object {
        private const val Threshold = 78f
        private const val clusterIteration = 100
    }

    private inner class Edge(var nbr: Int, var weight: Float)

    private inner class Graph(N: Int) {
        var adjLists: Array<ArrayList<Edge>>
        var clusters: IntArray

        init {
            adjLists = Array(N){ arrayListOf()}
            clusters = IntArray(N) {it}
        }

        fun addEdge(src: Int, dest: Int, weight: Float) {
            adjLists[src].add(Edge(dest, weight))
            adjLists[dest].add(Edge(src, weight))
        }
    }

    private fun makeGraph() {
        val numEncodings = encodings.size
        for (i in 0 until numEncodings) {
            val encoding = encodings[i]
            if (i == numEncodings - 1) break
            /**if last encoding */
            for (j in i + 1 until numEncodings) {
                val nbrEncoding = encodings[j]
                var distance = 0.0f
                for (k in 0 until encodingDim) {
                    distance += encoding[k] * nbrEncoding[k]
                }
                if (distance > Threshold) {
                    graph.addEdge(i, j, distance)
                }
            }
        }
    }

    private fun clusterGraph() {
        for (ci in 0 until clusterIteration) {
            /**randomly loop through nodes */
            for (i in 0 until numEncodings) {
                /**summed weights for the clusters of the nbrs */
                val clusterWeights: HashMap<Int, Float> = HashMap()
                /**go through the nbrs and see which clusters they belong to */
                for (j in 0 until graph.adjLists[i].size) {
                    val nbrEdge = graph.adjLists[i][j]
                    val nbr = nbrEdge.nbr
                    val nbrWt = nbrEdge.weight
                    val nbrCluster = graph.clusters[nbr]

                    if (clusterWeights.containsKey(nbrCluster)) {
                        clusterWeights[nbrCluster] = clusterWeights[nbrCluster]!! + nbrWt
                    } else {
                        clusterWeights[nbrCluster] = nbrWt
                    }
                }
                var maxCluster = -1
                var maxClusterWt = 0.0f
                clusterWeights.forEach { (k, v) ->
                    if (v > maxClusterWt) {
                        maxClusterWt = v
                        maxCluster = k
                    }
                }
                graph.clusters[i] = maxCluster
            }
        }
    }

    fun cluster(): Map<Int, ArrayList<String>> {
        makeGraph()
        clusterGraph()
        val clusterMap = mutableMapOf<Int, ArrayList<String>>()
        for (i in graph.clusters.indices) {
            val clusterId = graph.clusters[i]
            if(clusterId == -1) {
                continue
            }
            if (clusterMap.containsKey(clusterId)) {
                clusterMap[clusterId]?.add(fileNames[i])
            } else {
                clusterMap[clusterId] = arrayListOf(fileNames[i])
            }
        }
        return clusterMap
    }
}