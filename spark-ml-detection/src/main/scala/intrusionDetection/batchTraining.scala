package intrusionDetection

import common._
import intrusionDetection.featurePreprocessing._
import intrusionDetection.utility._
import ml.dmlc.xgboost4j.scala.spark.XGBoostClassifier
import org.apache.spark.ml.classification.{DecisionTreeClassifier, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.StringIndexerModel
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.NANOSECONDS

object batchTraining {
  def main(args: Array[String]): Unit = {

    if(!args(0).startsWith("hdfs") && !args(0).startsWith("s3")){ //No input checking if input is from hdfs or s3
      parseBatchArgs(args)
    }

    val spark = SparkSession.builder()
      .appName("Model Training")
      //.master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    System.setProperty("com.amazonaws.services.s3.enableV4", "true")

    val trainFile = args(0)
    val modelsLocation = args(1)

    /*******************************
    ** Training file preparation ***
    ********************************/

    val trainDF = spark.read
      .schema(kddDataSchema)
      .csv(trainFile)

    //Drop score row and rows with null or nan values
    println("Preparing training file")
    val cleanTrainDF = trainDF
      .drop(col("score"))
      .na.drop("any")

    //Replace labels with their attack group name
    val replacedLabelsTrainDF = cleanTrainDF.withColumn("label", categorizeKdd2Labels(col("label")))
      .cache()

    //Create String indexer for categorical kdd features
    val stringIndexer = indexCategoricalKdd(replacedLabelsTrainDF)

    //Create Discretizer for large continuous kdd features for Chi Square Selection
    val discretizer = discretizeLargeContinuousKdd(replacedLabelsTrainDF)

    //Assemble features for model training (without discretization)
    //and for chi square selection (with discretization)
    val stages = Array(stringIndexer, discretizer)
    val pipelineModel = assembleKdd(replacedLabelsTrainDF, stages)
    pipelineModel.write.overwrite().save(modelsLocation + "/pipelineModel")

    println("Feature preprocessing")
    val assembledTrainDF = pipelineModel.transform(replacedLabelsTrainDF)

    //Chi Squared feature selection
    println("Applying Chi-squared selection")
    val chiSqModel = applyChiSqSelection2(assembledTrainDF,10)
    chiSqModel.write.overwrite().save(modelsLocation + "/chiSqModel")

    //Add class weight column
    //val weightedTrainDF = add2ClassWeights(assembledTrainDF)

    //Select features with the chiSq model
    val selectedTrainDF = chiSqModel.transform(assembledTrainDF)

    /********************************
     ******** Model training ********
     ********************************/

    //Decision tree training
    println("Training Decision Tree...")
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label_Indexed")
      .setFeaturesCol("selectedFeatures")
      //.setWeightCol("classWeight")
      .setMaxBins(71)
      .setMaxDepth(10)
    val dt_time = System.nanoTime
    val dtModel = dt.fit(selectedTrainDF)
    println(s"Decision Tree trained in: ${TimeUnit.NANOSECONDS.toSeconds(System.nanoTime - dt_time)} secs\n")
    dtModel.write.overwrite().save(modelsLocation + "/dtModel")

    //Random forest training
    println("Training Random Forest...")
    val rf = new RandomForestClassifier()
      .setLabelCol("label_Indexed")
      .setFeaturesCol("selectedFeatures")
      .setMaxBins(71)
      .setMaxDepth(7)
      //.setWeightCol("classWeight")
      //.setNumTrees(30)
    val rf_time = System.nanoTime
    val rfModel = rf.fit(selectedTrainDF)
    println(s"Random Forest trained in: ${TimeUnit.NANOSECONDS.toSeconds(System.nanoTime - rf_time)} secs\n")
    rfModel.write.overwrite().save(modelsLocation + "/rfModel")
/**
    //XGBoost tree training
    println("Training XGBoost...")
    val numClass = pipelineModel.stages(0).asInstanceOf[StringIndexerModel].labelsArray(3).length
    val xg = new XGBoostClassifier()
      .setLabelCol("label_Indexed")
      .setFeaturesCol("selectedFeatures")
      .setMaxBins(71)
      .setMissing(0)
      //.setObjective("multi:softprob")
      //.setNumClass(numClass)
      .setMaxDepth(7)
    val xg_time = System.nanoTime
    val xgModel = xg.fit(selectedTrainDF)
    println(s"XG Boost trained in: ${TimeUnit.NANOSECONDS.toSeconds(System.nanoTime - xg_time)} secs\n")
    xgModel.write.overwrite().save(modelsLocation + "/xgModel")
**/
    println(s"Models saved in ${modelsLocation}")
    spark.stop()
  }

}
/**  Grid search for best hyperparameters **

val paramGrid = new ParamGridBuilder()
.addGrid(rf.maxDepth, Range(3,9,2))
.addGrid(rf.numTrees, Range(10,30,10))
.addGrid(rf.weightCol, Array("classWeight",""))
.addGrid(rf.impurity, Array("entropy","gini"))
.build()

val evaluator = new MulticlassClassificationEvaluator()
.setLabelCol("label_Indexed")
.setPredictionCol("prediction")
.setMetricName("accuracy")

val cv = new CrossValidator()
.setEstimator(rf)
.setEstimatorParamMaps(paramGrid)
.setEvaluator(evaluator)
.setNumFolds(3)

val cvModel = cv.fit(selectedTrainDF)

val rfModel = cvModel.bestModel.asInstanceOf[RandomForestClassificationModel]
println(s"Random forest parameters:\n ${rfModel.extractParamMap()}")
rfModel.write.overwrite().save(modelsLocation + "/rfModel")

**/