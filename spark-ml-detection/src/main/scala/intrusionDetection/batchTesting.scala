package intrusionDetection

import common._
import intrusionDetection.utility._
import ml.dmlc.xgboost4j.scala.spark.XGBoostClassificationModel
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, OneVsRestModel, RandomForestClassificationModel}
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator, MulticlassClassificationEvaluator}
import org.apache.spark.ml.feature.{ChiSqSelectorModel, IndexToString, StringIndexerModel, VectorSlicer}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.slf4j.LoggerFactory

object batchTesting {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Model Testing")
      .master("local[*]")
      .getOrCreate()

    val testFile = "/home/diomfeas/Desktop/Diplomatiki/Datasets/NSL-KDD/KDDTest+.txt"
    val pipelineLocation = "/home/diomfeas/Desktop/Diplomatiki/Pipeline/pipelineModel"
    val chiSqLocation = "/home/diomfeas/Desktop/Diplomatiki/Models/chiSqModel"
    val dtLocation = "/home/diomfeas/Desktop/Diplomatiki/Models/dtModel"
    val rfLocation = "/home/diomfeas/Desktop/Diplomatiki/Models/rfModel"
    val xgLocation = "/home/diomfeas/Desktop/Diplomatiki/Models/xgModel"

    val pipelineModel = PipelineModel.load(pipelineLocation)
    val chiSqModel = VectorSlicer.load(chiSqLocation)
    val dtModel = DecisionTreeClassificationModel.load(dtLocation)
    val rfModel = RandomForestClassificationModel.load(rfLocation)
    val xgModel = XGBoostClassificationModel.load(xgLocation)

    /******************************
     ** Testing file preparation ***
     ******************************/
    val testDF = spark.read
      .schema(kddDataSchema)
      .csv(testFile)

    //Drop score row and rows with null or nan values
    val cleanTestDF = testDF
      .drop(col("score"))//.drop(testDF.columns.last)
      .na.drop("any")

    //Replace labels with their attack group name
    val replacedLabelsTestDF = cleanTestDF.withColumn("realLabel", categorizeKdd5Labels(col("label")))
      .withColumn("label", categorizeKdd2Labels(col("label")))

    //Feature preprocess test file
    val assembledTestDF = pipelineModel.transform(replacedLabelsTestDF)

    //Select features with the chiSq model
    val selectedTestDF = chiSqModel.transform(assembledTestDF)
      .cache()

    //Add a stage to translate predictions back to strings
    val indexer = pipelineModel.stages(0).asInstanceOf[StringIndexerModel]
    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(indexer.labelsArray(3))

    /******************************
     ******* Model Testing ********
     ******************************/

//    //Decision Tree testing
//    val dtPredictions = dtModel.transform(selectedTestDF)
//    val convertedDtPredictions = labelConverter.transform(dtPredictions)

//    //Random Forest Testing
//    val rfPredictions = rfModel.transform(selectedTestDF)
//    val convertedRfPredictions = labelConverter.transform(rfPredictions)

    //XGBoost Testing
    val xgPredictions = xgModel.transform(selectedTestDF)
    val convertedXgPredictions = labelConverter.transform(xgPredictions)

    convertedXgPredictions.select(
      col("label_Indexed"),
      col("prediction"),
      col("realLabel"),
      col("label"),
      col("predictedLabel")
      ).show(200, false)

//    //Multiclass classification evaluation with 4 metrics
//    val metricNames = Array("accuracy","f1","weightedPrecision","weightedRecall")
//
//    val metrics = metricNames.map( metric =>
//      new MulticlassClassificationEvaluator()
//        .setMetricName(metric)
//        .setLabelCol("label_Indexed")
//        .setPredictionCol("prediction")
//        .evaluate(xgPredictions)
//    )
//    println(s"accuracy = ${metrics(0)}")
//    println(s"f1 = ${metrics(1)}")
//    println(s"weightedPrecision = ${metrics(2)}")
//    println(s"weightedRecall = ${metrics(3)}")

    //Binary classification evaluation
    val binaryMetricNames = Array("areaUnderROC", "areaUnderPR")

    val metrics = binaryMetricNames.map( metric =>
      new BinaryClassificationEvaluator()
        .setMetricName(metric)
        .setLabelCol("label_Indexed")
        .setRawPredictionCol("rawPrediction")
        .evaluate(xgPredictions)
    )
    println(s"Area under ROC = ${metrics(0)}")
    println(s"Area under PR = ${metrics(1)}")

    spark.stop()
  }

}
