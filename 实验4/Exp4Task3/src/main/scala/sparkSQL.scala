import org.apache.parquet.format.IntType
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object sparkSQL {
  def main(args: Array[String]) {
    /**
     * SparkContext 的初始化需要一个SparkConf对象
     * SparkConf包含了Spark集群的配置的各种参数
     */

    val conf=new SparkConf()
      .setMaster("local[*]")//启动本地化计算
      .setAppName("Exp4 Task3 sparkSQL")//设置本程序名称

    val sc=new SparkContext(conf)
    sc.setLogLevel("WARN")

    val spark = SparkSession.builder.config(conf=conf).getOrCreate()
    import spark.implicits._

    val schema = new StructType(Array(
      StructField("loan_id",DataTypes.StringType),
      StructField("user_id",DataTypes.StringType),
      StructField("total_loan",DataTypes.DoubleType),
      StructField("year_of_loan",DataTypes.IntegerType),
      StructField("interest",DataTypes.DoubleType),
      StructField("monthly_payment",DataTypes.DoubleType),
      StructField("loan_class",DataTypes.StringType),
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
      StructField("issue_date",DataTypes.StringType),
      StructField("use",DataTypes.IntegerType),
      StructField("post_code",DataTypes.StringType),
      StructField("region",DataTypes.StringType),
      StructField("debt_loan_ratio",DataTypes.DoubleType),
      StructField("del_in_18month",DataTypes.IntegerType),
      StructField("scoring_low",DataTypes.IntegerType),
      StructField("scoring_high",DataTypes.IntegerType),
      StructField("pub_dero_bankrup",DataTypes.IntegerType),
      StructField("early_return",DataTypes.IntegerType),
      StructField("early_return_amount",DataTypes.IntegerType),
      StructField("early_return_amount_3mon",DataTypes.DoubleType),
      StructField("recircle_b",DataTypes.IntegerType),
      StructField("recircle_u",DataTypes.DoubleType),
      StructField("initial_list_status",DataTypes.IntegerType),
      StructField("earlies_credit_mon",DataTypes.StringType),
      StructField("title",DataTypes.IntegerType),
      StructField("policy_code",DataTypes.IntegerType),
      StructField("f0",DataTypes.IntegerType),
      StructField("f1",DataTypes.IntegerType),
      StructField("f2",DataTypes.IntegerType),
      StructField("f3",DataTypes.IntegerType),
      StructField("f4",DataTypes.IntegerType),
      StructField("f5",DataTypes.IntegerType),
      StructField("is_default",DataTypes.BooleanType),
    ))
//    case class Loan(loan_id:String, user_id:String, total_loan:Int, year_of_loan:Int, interest:Double, monthly_payment:
//                   Double, loan_class:String, sub_class:String, work_type:String, employer_type:String, industry:String,
//                    work_year:String, house_exist:Int, house_loan_status:Int, censor_status:Int, marriage:Int,
//                    offsprings:Int, issue_date:String, use:Int, post_code:String, region:String, debt_loan_ratio:Double,
//                    del_in_18month:Int, scoring_low:Int, scoring_high:Int, pub_dero_bankrup:Int, early_return:Int,
//                    early_return_amount:Int, early_return_amount_3mon:Double, recircle_b:Int, recircle_u:Double,
//                    initial_list_status:Int, earlies_credit_mon:String, title:Int, policy_code:Int, f0:Int,
//                    f1:Int, f2:Int, f3:Int, f4:Int, f5:Int, is_default:Boolean)
    //    val data=sc.textFile("E:\\IDEA\\ProjectSpace\\SparkTest\\hello.txt")//读取本地文件
    val df=spark.read.schema(schema).format("csv").option("header","true").load("hdfs://127.0.0.1:8900/user/jzt/train_data")

    df.createOrReplaceTempView("loan")

//    1.统计所有⽤户所在公司类型 employer_type 的数量分布占⽐情况。
    val emp_type_ratio = spark.sql("SELECT employer_type, COUNT(*)/(" +
      "SELECT COUNT(*) FROM loan" +
      ") AS ratio FROM loan GROUP BY employer_type")
    emp_type_ratio.coalesce(1).write.option("header","true").csv("hdfs://127.0.0.1:8900/user/jzt/emp_type_ratio.csv")


//    2.统计每个⽤户最终须缴纳的利息⾦额。
//    import org.apache.spark.sql.functions.col
    val id_total_mon = spark.sql("SELECT user_id, (year_of_loan  * monthly_payment * 12.0 - total_loan) AS total_money FROM loan")
    id_total_mon.coalesce(1).write.option("header","true").csv("hdfs://127.0.0.1:8900/user/jzt/id_total_mon.csv")

//    3.统计⼯作年限 work_year 超过 5 年的⽤户的房贷情况 censor_status。
    import org.apache.spark.sql.functions._
    //  将工作年限为空的设置为0，并且从原字符串中提取出数字年限
    val updatedDf = df.na.fill(value="0",cols=Array("work_year")).withColumn("work_year", regexp_replace
    (col("work_year"), "[^0-9+<]", ""))
    updatedDf.createOrReplaceTempView("updated_loan")
    val censor_status_over5 = spark.sql("SELECT user_id, censor_status, work_year FROM updated_loan WHERE " +
      "work_year='10+' OR work_year>5")
    censor_status_over5.coalesce(1).write.option("header","true").csv("hdfs://127.0.0.1:8900/user/jzt/censor_status_over5.csv")

  }
}
