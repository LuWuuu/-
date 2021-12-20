import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier, FMClassifier, GBTClassifier, LinearSVC, LogisticRegression, MultilayerPerceptronClassifier, NaiveBayes, RandomForestClassifier}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator

import scala.math.Ordered.orderingToOrdered
import scala.math.Ordering.Implicits.infixOrderingOps
object sparkML {
  def main(args: Array[String]): Unit ={
    val conf=new SparkConf()
      .setMaster("local[*]")//启动本地化计算
      .setAppName("Exp4 Task4 sparkML")//设置本程序名称

    val sc=new SparkContext(conf)
    sc.setLogLevel("WARN")

    val spark = SparkSession.builder.config(conf=conf).getOrCreate()

    val schema = new StructType(Array(
      StructField("loan_id",DataTypes.StringType),
      StructField("user_id",DataTypes.StringType),
      StructField("total_loan",DataTypes.DoubleType),
      StructField("year_of_loan",DataTypes.IntegerType),
      StructField("interest",DataTypes.DoubleType),
      StructField("monthly_payment",DataTypes.DoubleType),
      StructField("class",DataTypes.StringType),
      StructField("sub_class",DataTypes.StringType),
      StructField("work_type",DataTypes.StringType),
      StructField("employer_type",DataTypes.StringType),
      StructField("industry",DataTypes.StringType),
      StructField("work_year",DataTypes.StringType),
      StructField("house_exist",DataTypes.IntegerType),
      StructField("house_loan_status",DataTypes.IntegerType),
      StructField("censor_status",DataTypes.IntegerType),
      StructField("marriage",DataTypes.IntegerType),
      StructField("offsprings",DataTypes.IntegerType),
      StructField("issue_date",DataTypes.DateType),
      StructField("use",DataTypes.IntegerType),
      StructField("post_code",DataTypes.StringType),
      StructField("region",DataTypes.IntegerType),
      StructField("debt_loan_ratio",DataTypes.DoubleType),
      StructField("del_in_18month",DataTypes.DoubleType),
      StructField("scoring_low",DataTypes.DoubleType),
      StructField("scoring_high",DataTypes.DoubleType),
      StructField("pub_dero_bankrup",DataTypes.DoubleType),
      StructField("early_return",DataTypes.IntegerType),
      StructField("early_return_amount",DataTypes.IntegerType),
      StructField("early_return_amount_3mon",DataTypes.DoubleType),
      StructField("recircle_b",DataTypes.DoubleType),
      StructField("recircle_u",DataTypes.DoubleType),
      StructField("initial_list_status",DataTypes.IntegerType),
      StructField("earlies_credit_mon",DataTypes.StringType),
      StructField("title",DataTypes.IntegerType),
      StructField("policy_code",DataTypes.DoubleType),
      StructField("f0",DataTypes.DoubleType),
      StructField("f1",DataTypes.DoubleType),
      StructField("f2",DataTypes.DoubleType),
      StructField("f3",DataTypes.DoubleType),
      StructField("f4",DataTypes.DoubleType),
      StructField("f5",DataTypes.DoubleType),
      StructField("is_default",DataTypes.IntegerType),
    ))
    val df=spark.read.schema(schema).format("csv").option("header","true").load("hdfs://127.0.0.1:8900/user/jzt/train_data")
    df.createOrReplaceTempView("loan")

    val indexed =new StringIndexer().
      setInputCols(Array("class","sub_class","work_type","employer_type","industry"))
      .setOutputCols(Array("indexed_class","indexed_sub_class","indexed_work_type","indexed_employer_type",
        "indexed_industry"))
      .setHandleInvalid("keep")
      .fit(df)
      .transform(df)
//      loan_id,user_id,post_code,title,earlies_credit_mon这几个特征对于分类预测没有作用，故去除
      .drop("loan_id","user_id","post_code","title","earlies_credit_mon")
      .drop("class","sub_class","work_type","employer_type","industry")
//      原始数据中有debt_loan_ratio为负的脏数据，去除
      .filter("debt_loan_ratio >= 0")
//    indexed.createOrReplaceTempView("parsed_loan")

    def parseWorkYear(workYear:String) = {
      workYear match {
        case "< 1 year" => 0
        case "1 year" => 1
        case "2 years" => 2
        case "3 years" => 3
        case "4 years" => 4
        case "5 years" => 5
        case "6 years" => 6
        case "7 years" => 7
        case "8 years" => 8
        case "9 years" => 9
        case "10+ years" => 10
        case _ => 0
      }
    }

    val udf_parse_work_year = udf(parseWorkYear _)

    val indexed2 = indexed.withColumn("issue_date_diff", datediff(indexed("issue_date"), lit("2007-07-01")))
      .withColumn("parsed_work_year", udf_parse_work_year(indexed("work_year")))
      .withColumn("total_loan", indexed("total_loan")/1000)
      .withColumnRenamed("is_default","label")
      .drop("issue_date","work_year")

    val assembler: VectorAssembler = new VectorAssembler().setHandleInvalid("skip").setInputCols(Array(
      "monthly_payment","house_exist","house_loan_status","censor_status",
      "marriage","offsprings","use","debt_loan_ratio","del_in_18month","scoring_low","scoring_high",
      "initial_list_status","pub_dero_bankrup","early_return","early_return_amount","early_return_amount_3mon","recircle_b","recircle_u",
      "policy_code","f0","f1","f2","f3","f4","f5","indexed_industry",
      "indexed_class","indexed_work_type","indexed_employer_type","parsed_work_year"
    )).setOutputCol("features")

    val assmblerDf:DataFrame = assembler.transform(indexed2)

//    logistic回归，AUC=0.5
    //    将数据切分成8:2的训练集和测试集
//    val Array(trainData,testData):Array[Dataset[Row]] = assmblerDf.randomSplit(Array(0.8,0.2))
//    val lr = new LogisticRegression()
//      .setMaxIter(10)
//      .setRegParam(0.3)
//      .setElasticNetParam(0.8)
//
//    val lrModel = lr.fit(assmblerDf)
//
//    val testPre: DataFrame = lrModel.transform(testData)
//
//    val acc: Double = new BinaryClassificationEvaluator().setMetricName("areaUnderROC").evaluate(testPre)
//
//    print(acc)

//    决策树，平均AUC=0.708
    val labelIndexer = new StringIndexer()
      .setInputCol("label")
      .setOutputCol("indexedLabel")
      .fit(assmblerDf)

    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(10) // features with > 10 distinct values are treated as continuous.
      .fit(assmblerDf)

    assmblerDf.show()

    val Array(trainData, testData) = assmblerDf.randomSplit(Array(0.8, 0.2))

    val dt = new DecisionTreeClassifier()
      .setMaxBins(64)
      .setLabelCol("indexedLabel")
      .setFeaturesCol("indexedFeatures")

    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(labelIndexer.labelsArray(0))

    val pipeline = new Pipeline()
      .setStages(Array(labelIndexer, featureIndexer, dt, labelConverter))

    val model = pipeline.fit(trainData)

    val predictions = model.transform(testData)

    val evaluator = new BinaryClassificationEvaluator()
      .setLabelCol("indexedLabel")
      .setRawPredictionCol("prediction")
      .setMetricName("areaUnderROC")

    val accuracy = evaluator.evaluate(predictions)

    print(accuracy)

//    随机森林,平均AUC=0.553
//    val labelIndexer = new StringIndexer()
//      .setInputCol("label")
//      .setOutputCol("indexedLabel")
//      .fit(assmblerDf)
//    // Automatically identify categorical features, and index them.
//    // Set maxCategories so features with > 4 distinct values are treated as continuous.
//    val featureIndexer = new VectorIndexer()
//      .setInputCol("features")
//      .setOutputCol("indexedFeatures")
//      .setMaxCategories(4)
//      .fit(assmblerDf)
//
//    // Split the data into training and test sets (30% held out for testing).
//     val Array(trainingData, testData) = assmblerDf.randomSplit(Array(0.8, 0.2))
//   val rf = new RandomForestClassifier()
//      .setLabelCol("indexedLabel")
//      .setFeaturesCol("indexedFeatures")
//      .setNumTrees(10)
//     .setMaxBins(64)
//
//    // Convert indexed labels back to original labels.
//    val labelConverter = new IndexToString()
//      .setInputCol("prediction")
//      .setOutputCol("predictedLabel")
//      .setLabels(labelIndexer.labelsArray(0))
//
//    // Chain indexers and forest in a Pipeline.
//    val pipeline = new Pipeline()
//      .setStages(Array(labelIndexer, featureIndexer, rf, labelConverter))
//
//    // Train model. This also runs the indexers.
//    val model = pipeline.fit(trainingData)
//
//    // Make predictions.
//    val predictions = model.transform(testData)
//
//    val evaluator = new BinaryClassificationEvaluator()
//          .setLabelCol("indexedLabel")
//          .setRawPredictionCol("prediction")
//          .setMetricName("areaUnderROC")
//
//    val accuracy = evaluator.evaluate(predictions)
//
//    print(accuracy)

//    梯度提升树分类器（GBDT），平均AUC=0.702
//val labelIndexer = new StringIndexer()
//  .setInputCol("label")
//  .setOutputCol("indexedLabel")
//  .fit(assmblerDf)
//    // Automatically identify categorical features, and index them.
//    // Set maxCategories so features with > 4 distinct values are treated as continuous.
//    val featureIndexer = new VectorIndexer()
//      .setInputCol("features")
//      .setOutputCol("indexedFeatures")
//      .setMaxCategories(4)
//      .fit(assmblerDf)
//
//    // Split the data into training and test sets (30% held out for testing).
//    val Array(trainingData, testData) = assmblerDf.randomSplit(Array(0.7, 0.3))
//
//    // Train a GBT model.
//    val gbt = new GBTClassifier()
//      .setLabelCol("indexedLabel")
//      .setFeaturesCol("indexedFeatures")
//      .setMaxIter(10)
//      .setFeatureSubsetStrategy("auto")
//      .setMaxBins(64)
//
//    // Convert indexed labels back to original labels.
//    val labelConverter = new IndexToString()
//      .setInputCol("prediction")
//      .setOutputCol("predictedLabel")
//      .setLabels(labelIndexer.labelsArray(0))
//
//    // Chain indexers and GBT in a Pipeline.
//    val pipeline = new Pipeline()
//      .setStages(Array(labelIndexer, featureIndexer, gbt, labelConverter))
//
//    // Train model. This also runs the indexers.
//    val model = pipeline.fit(trainingData)
//
//    // Make predictions.
//    val predictions = model.transform(testData)
//
//    val evaluator = new BinaryClassificationEvaluator()
//              .setLabelCol("indexedLabel")
//              .setRawPredictionCol("prediction")
//              .setMetricName("areaUnderROC")
//
//        val accuracy = evaluator.evaluate(predictions)
//
//        print(accuracy)

//    多层感知器分类器（MLPC）,平均AUC=0.604
//    val splits = assmblerDf.randomSplit(Array(0.8, 0.2))
//    val train = splits(0)
//    val test = splits(1)
//
//    // specify layers for the neural network:
//    val layers = Array[Int](36, 36, 18, 10,5,2)
//
//    // create the trainer and set its parameters
//    val trainer = new MultilayerPerceptronClassifier()
//      .setLayers(layers)
//      .setBlockSize(128)
//      .setSeed(1234L)
//      .setMaxIter(100)
//
//    // train the model
//    val model = trainer.fit(train)
//
//    // compute accuracy on the test set
//    val result = model.transform(test)
//    val evaluator = new BinaryClassificationEvaluator()
//      .setMetricName("areaUnderROC")
//    val accuracy = evaluator.evaluate(result)
//    print(accuracy)

//    线性支持向量机分类，平均AUC=0.706
//    val splits = assmblerDf.randomSplit(Array(0.8, 0.2))
//    val train = splits(0)
//    val test = splits(1)

  val lsvc = new LinearSVC()
      .setMaxIter(10)
      .setRegParam(0.1)

    // Fit the model
    val lsvcModel = lsvc.fit(trainData)
    val result = lsvcModel.transform(testData)
    val evaluator2 = new BinaryClassificationEvaluator()
      .setMetricName("areaUnderROC")
    val accuracy2 = evaluator2.evaluate(result)
    print(accuracy2)

//    朴素贝叶斯，平均AUC=0.539
//      val Array(trainingData, testData) = assmblerDf.randomSplit(Array(0.8, 0.2))
//      val model = new NaiveBayes().fit(trainingData)
//      val predictions = model.transform(testData)
//      val evaluator = new BinaryClassificationEvaluator()
//          .setMetricName("areaUnderROC")
//      val accuracy = evaluator.evaluate(predictions)
//      println(accuracy)

//    分解机分类器,平均AUC=0.500
//    val labelIndexer = new StringIndexer()
//      .setInputCol("label")
//      .setOutputCol("indexedLabel")
//      .fit(assmblerDf)
//    // Scale features.
//    val featureScaler = new MinMaxScaler()
//      .setInputCol("features")
//      .setOutputCol("scaledFeatures")
//      .fit(assmblerDf)
//
//    // Split the data into training and test sets (30% held out for testing).
//    val Array(trainingData, testData) = assmblerDf.randomSplit(Array(0.8, 0.2))
//
//    // Train a FM model.
//    val fm = new FMClassifier()
//      .setLabelCol("indexedLabel")
//      .setFeaturesCol("scaledFeatures")
//      .setStepSize(0.001)
//
//    // Convert indexed labels back to original labels.
//    val labelConverter = new IndexToString()
//      .setInputCol("prediction")
//      .setOutputCol("predictedLabel")
//      .setLabels(labelIndexer.labelsArray(0))
//
//    // Create a Pipeline.
//    val pipeline = new Pipeline()
//      .setStages(Array(labelIndexer, featureScaler, fm, labelConverter))
//
//    // Train model.
//    val model = pipeline.fit(trainingData)
//
//    // Make predictions.
//    val predictions = model.transform(testData)
//
//    // Select (prediction, true label) and compute test accuracy.
//    val evaluator = new BinaryClassificationEvaluator()
//      .setLabelCol("indexedLabel")
//      .setRawPredictionCol("prediction")
//      .setMetricName("areaUnderROC")
//
//    val accuracy = evaluator.evaluate(predictions)
//    println(accuracy)

  }}
