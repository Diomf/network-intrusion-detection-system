package intrusionDetection

import common._
import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.functions.{col, regexp_replace, udf}
import org.apache.spark.ml.linalg.{DenseVector, SparseMatrix, SparseVector}
import java.io.File

object utility {

  def printBatchUsage() =
    println("Usage: training_jar <trainingFile> <modelsLocation>")


  def parseBatchArgs(args: Array[String]) = {
    if(args.length != 2) {
      System.err.println("Incorrect number of arguments")
      printBatchUsage()
      System.exit(1)
    }
    val trainFile = new File(args(0))
    if(!trainFile.exists ){
      System.err.println(s"Given path (${args(0)}) does not exist")
      printBatchUsage()
      System.exit(1)
    }
    if(!trainFile.isFile) {
      System.err.println(s"Given path (${args(0)}) is not a file")
      printBatchUsage()
      System.exit(1)
    }
    val loc = new File(args(1))
    if(!loc.isDirectory){
      System.err.println(s"Given path (${args(1)}) is not a directory")
      printBatchUsage()
      System.exit(1)
    }
    if(!loc.exists){
      System.err.println(s"Given path (${args(1)}) does not exist")
      printBatchUsage()
      System.exit(1)
    }

  }

  def printStreamUsage() =
    println("Usage: testing_jar <kafkaTopicName> <modelsLocation> <mlAlgorithm> <kafkaHost> <es_host>")

  def parseStreamArgs(args: Array[String]) = {
    if(args.length != 5) {
      System.err.println("Incorrect number of arguments")
      printStreamUsage()
      System.exit(1)
    }
    if(!args(2).matches("dt|rf|xg")){
      System.err.println("mlAlgorithm options: dt | rf")
      printStreamUsage()
      System.exit(1)
    }
    if(!args(1).startsWith("hdfs") && !args(1).startsWith("s3")) {
      val modelsLoc = new File(args(1))
      val mlLoc = new File(args(1) + "/" + args(2) + "Model")
      if (!modelsLoc.isDirectory) {
        System.err.println(s"Given path (${args(1)}) is not a directory")
        printStreamUsage()
        System.exit(1)
      }
      if (!modelsLoc.exists) {
        System.err.println(s"Given path (${args(1)}) does not exist")
        printStreamUsage()
        System.exit(1)
      }
      if (!mlLoc.exists) {
        System.err.println(s"Given path (${mlLoc}) does not exist")
        printStreamUsage()
        System.exit(1)
      }
    }
  }

  def categorizeKdd2Labels(label: Column): Column =
    regexp_replace(col("label"), "^(?!normal).*$", "attack")

  val categorizeKdd5Labels = udf((label:String) => {
    if (dosType(label))
      "dos"
    else if (probeType(label))
      "probe"
    else if (u2rType(label))
      "u2r"
    else if (r2lType(label))
      "r2l"
    else
      "normal"
  })

  val categorizeKdd3Labels = udf((label:String) => {
    if (dosType(label))
      "dos"
    else if (probeType(label)) //(probeType(label) || u2rType(label) || r2lType(label))
      "probe"
    else
      "normal"
  })

  val asDense = udf((v: SparseVector) => v.toDense)

  def add5ClassWeights(df: DataFrame): DataFrame = {

    val totalSize = df.count
    val numOfNormals = df.filter(col("label") === "normal").count
    val numOfDos = df.filter(col("label") === "dos").count
    val numOfProbe = df.filter(col("label") === "probe").count
    val numOfU2r = df.filter(col("label") === "u2r").count
    val numOfR2l = df.filter(col("label") === "r2l").count

    val (normalRatio,dosRatio,probeRatio,u2rRatio,r2lRatio) = (
      (totalSize - numOfNormals).toDouble / totalSize,
      (totalSize - numOfDos).toDouble / totalSize,
      (totalSize - numOfProbe).toDouble / totalSize,
      (totalSize - numOfU2r).toDouble / totalSize,
      (totalSize - numOfR2l).toDouble / totalSize,
    )

    val calculateWeights = udf((label:String) => label match {
      case "dos" => 1 * dosRatio
      case "probe" => 1 * probeRatio
      case "u2r" => 1 * u2rRatio
      case "r2l" => 1 * r2lRatio
      case _ => 1 * normalRatio
    })

    val weightedDf = df.withColumn("classWeight", calculateWeights(col("label")))
    weightedDf
  }

  def add3ClassWeights(df: DataFrame): DataFrame = {

    val totalSize = df.count
    val numOfNormals = df.filter(col("label") === "normal").count
    val numOfDos = df.filter(col("label") === "dos").count
    val numOfProbe = df.filter(col("label") === "probe").count

    val (normalRatio,dosRatio,probeRatio) = (
      (totalSize - numOfNormals).toDouble / totalSize,
      (totalSize - numOfDos).toDouble / totalSize,
      (totalSize - numOfProbe).toDouble / totalSize
    )

    val calculateWeights = udf((label:String) => label match {
      case "dos" => 1 * dosRatio
      case "probe" => 1 * probeRatio
      case _ => 1 * normalRatio
    })

    val weightedDf = df.withColumn("classWeight", calculateWeights(col("label")))
    weightedDf
  }

  def add2ClassWeights(df: DataFrame): DataFrame = {

    val totalSize = df.count
    val numOfNormals = df.filter(col("label") === "normal").count
    val numOfAttacks = df.filter(col("label") === "attack").count

    val (normalRatio,attackRatio) = (
      (totalSize - numOfNormals).toDouble / totalSize,
      (totalSize - numOfAttacks).toDouble / totalSize
    )

    val calculateWeights = udf((label:String) => label match {
      case "attack" => 1 * attackRatio
      case _ => 1 * normalRatio
    })

    val weightedDf = df.withColumn("classWeight", calculateWeights(col("label")))
    weightedDf
  }
  //how many nulls in each column
  //select(trainingData.columns.map(c => sum(col(c).isNull.cast("int")).alias(c)): _*)
}

