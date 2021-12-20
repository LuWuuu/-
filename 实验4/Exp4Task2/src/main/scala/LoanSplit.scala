import org.apache.spark.SparkContext
import org.apache.spark.SparkConf


object LoanSplit {
  def splitTotalLoan(record: String): String = {
    val totalLoan = record.split(",")(2)
    //  获取到的total_loan为double类型，需要提取其整数部分
    val thousand = totalLoan.split("""\.""")(0).toInt / 1000
    val lowBound = thousand * 1000
    val highBound = (thousand+1) * 1000
    return "("+lowBound+","+highBound+")"
  }
  def main(args: Array[String]) {
    /**
     * SparkContext 的初始化需要一个SparkConf对象
     * SparkConf包含了Spark集群的配置的各种参数
     */

    val conf=new SparkConf()
      .setMaster("local")//启动本地化计算
      .setAppName("testRdd")//设置本程序名称

    //Spark程序的编写都是从SparkContext开始的
    val sc=new SparkContext(conf)
    //    val spark = SparkSession.builder().config(sparkconf).getOrCreate()
    sc.setLogLevel("WARN")
    //以上的语句等价与val sc=new SparkContext("local","testRdd")
    //    val data=sc.textFile("E:\\IDEA\\ProjectSpace\\SparkTest\\hello.txt")//读取本地文件
    val data=sc.textFile("hdfs://127.0.0.1:8900/user/jzt/train_data")

    val header = data.first()
    // 去除首行表头
    val dataWithoutHeader = data.filter(_ != header)

    val splitTL = splitTotalLoan _
    dataWithoutHeader.map(splitTL)
      .map((_,1))//将每一项转换为key-value，数据是key，value是1
      .reduceByKey(_+_)//将具有相同key的项相加合并成一个
      .sortBy(_._1.split(",")(0).split("""\(""")(1).toInt)
      .collect()
      .foreach(println)
  }
}

