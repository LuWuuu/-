import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URI;

import javafx.geometry.Pos;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;

import java.io.*;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.util.hash.Hash;
import org.apache.zookeeper.data.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
//import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
//import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.lib.HashPartitioner;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.apache.kerby.config.Conf;
import sun.awt.Symbol;

import javax.lang.model.type.IntersectionType;
import javax.print.Doc;

import jzt.knn.distance.Distance;

public class KNN {
    public static class IdDistance implements WritableComparable<IdDistance> {
        private String id;
        private double distance;
        private String target;

        public IdDistance() { }

        public IdDistance(String id, double distance, String target) {
            this.id = id;
            this.distance = distance;
            this.target = target;
        }

        public void set(String id, double distance, String target) {
            this.id = id;
            this.distance = distance;
            this.target = target;
        }

        public void readFields(DataInput in) throws IOException {
            this.id = in.readUTF();
            this.distance = in.readDouble();
            this.target = in.readUTF();
        }

        public void write(DataOutput out) throws IOException {
            out.writeUTF(this.id);
            out.writeDouble(this.distance);
            out.writeUTF(this.target);
        }

        public String toString() {
            return this.id + ":" + this.distance + "-" + this.target;
        }

        public int compareTo(IdDistance o) {
            if (!this.id.equals(o.id)) return Integer.parseInt(this.id) < Integer.parseInt(o.id) ? -1:
                    (Integer.parseInt(this.id)==Integer.parseInt(o.id)?0 : 1);
            else {
                return this.distance < o.distance ? -1 : (this.distance == o.distance ? 0 : 1);
            }
        }
    }

    public static class CalcuDistanceMapper extends Mapper<Object, Text, IdDistance, Text> {
        private ArrayList<String> trainData = new ArrayList<String>();
        public String distanceMethod;
        public double reserveRate;

        protected void setup(Mapper<Object, Text, IdDistance, Text>.Context context) throws IOException, InterruptedException{
            Configuration conf = context.getConfiguration();
            distanceMethod = conf.get("distance_method","euclid");
            URI[] patternsURIs = context.getCacheFiles();

            for (URI patternsURI : patternsURIs) {
                String patternsPath = patternsURI.getPath();
                Configuration conf2 = new Configuration();
                FileSystem fs = FileSystem.get(URI.create("hdfs://127.0.0.1:8900" + patternsPath),conf);
                System.out.println(patternsPath);
                FSDataInputStream fin = fs.open(new Path("hdfs://127.0.0.1:8900" + patternsPath));
//                int totalLine = 0;
                BufferedReader in = null;
                String line;
//                in = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
//                while ((line = in.readLine()) != null) {
//                    totalLine++;
//                }
//                totalLine--;  // 排除首行非数据行
//                System.out.println("totalLine:"+totalLine);
//                int trainNum = (int) (reserveRate * totalLine);
//                int recordNum = 0;
//                fin = fs.open(new Path("hdfs://127.0.0.1:8900" + patternsPath));
                in = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
                line = in.readLine();
                while((line = in.readLine()) != null) {

                    trainData.add(line);
                }
            }
            super.setup(context);
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            System.out.println("line:" + line);
            String[] data = line.split(",");
            if (!data[0].equals("")) {
                double[] test = {Double.parseDouble(data[1]),Double.parseDouble(data[2]),
                        Double.parseDouble(data[3]),Double.parseDouble(data[4])};
                for (String trainSample:trainData) {
//                    System.out.println("trainData:"+trainSample);
                    String[] sampleData = trainSample.split(",");
                    double[] train = {Double.parseDouble(sampleData[1]),Double.parseDouble(sampleData[2]),
                            Double.parseDouble(sampleData[3]),Double.parseDouble(sampleData[4])};
//                    System.out.println("train:"+train[0]);

                    double distance = Distance.CalcuDistance(distanceMethod, train, test);
                    System.out.println("distance:"+data[0] + "-" + distance + "-" + sampleData[5] + "-" + data[5]);
                    context.write(new IdDistance(data[0],distance,sampleData[5]), new Text(data[5]));
                }
            }
        }
    }

    public class IdPartitioner extends Partitioner<IdDistance, Text> {
        @Override
        public int getPartition(IdDistance idDistance, Text text, int i) {
            return (idDistance.id.hashCode() & 2147483647) % i;
        }
    }

    public static class ReserverKCombiner extends Reducer<IdDistance, Text, IdDistance, Text> {
        public int k;
        public int left;
        public String curId;

        protected void setup(Reducer<IdDistance, Text, IdDistance, Text>.Context context) throws IOException, InterruptedException{
            Configuration conf = context.getConfiguration();
            k = conf.getInt("k", 10);
            curId=null;
            left=k;
            super.setup(context);
        }

        public void reduce(IdDistance key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String id = key.id;
            if (curId == null) {
                curId = id;
            }
            if (!curId.equals(id)) {
                left=k;
                curId = id;
            }
//            if (id.equals(curId)) {
//                if (occupied < k) {
//                    occupied++;
//                    context.write(key,);
//                }
//            }
            if (left!=0){
                for (Text value:values) {
                        left--;
                        context.write(key, value);
                        if (left==0)
                            break;
                    }
                }
            }
        }

    public static class Reducer1 extends Reducer<IdDistance, Text, Text, NullWritable> {
        public int k;
        public int left;
        public double totalNum;
        public double correctNum;
        public String distanceMethod;
        HashMap<String, Integer> hm;

        protected void setup(Reducer<IdDistance, Text, Text, NullWritable>.Context context) throws IOException, InterruptedException{
            hm = new HashMap<String, Integer>();
            Configuration conf = context.getConfiguration();
            k = conf.getInt("k", 10);
            distanceMethod = conf.get("distance_method", "euclid");
            left = k;
            totalNum = 0;
            correctNum = 0;
            super.setup(context);
        }

        public String getVoteResult (HashMap<String, Integer> hm) {
            String result = null;
            Integer count = 0;
            for (Map.Entry<String, Integer> entry: hm.entrySet()){
                if (entry.getValue()>count){
                    result = entry.getKey();
                }
            }
            return result;
        }

        public void reduce(IdDistance key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text value:values) {
                if (left!=0){
                       Integer count = 1;
                       if (hm.containsKey(key.target)){
                           count += hm.get(key.target);
                       }
                       hm.put(key.target, count);
                       left--;
                       if (left==0) {
                            String result = getVoteResult(hm);
                            String resultText = key.id + ":" + value + "——predicted:" + result;
                            left = k;
                            if (result.equals(value.toString())){
                                correctNum++;
                            }
                            totalNum++;
                            hm.clear();
                            context.write(new Text(resultText), NullWritable.get());
                       }
                }
            }
        }

        protected void cleanup(Reducer<IdDistance, Text, Text, NullWritable>.Context context)
            throws IOException, InterruptedException {
            context.write(new Text("距离计算方式："+distanceMethod), NullWritable.get());
            context.write(new Text("k: " + k), NullWritable.get());
            context.write(new Text("accuracy: "+ (correctNum/totalNum)), NullWritable.get());
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();   //实例化Configuration
        String HDFS_URL = "hdfs://127.0.0.1:8900";
        String[] remainingArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        Job job = Job.getInstance(conf, "KNN");
        List<String> otherArgs = new ArrayList<String>();
        for (int i = 0; i < remainingArgs.length; ++i) {
            if ("-train".equals(remainingArgs[i])) {
                job.addCacheFile(new Path(HDFS_URL + remainingArgs[++i]).toUri());
//                job.getConfiguration().setBoolean("wordcount.skipP.patterns", true);
            } else if ("-distance".equals(remainingArgs[i])) {
//                job.addCacheFile(new Path(remainingArgs[++i]).toUri());
                job.getConfiguration().set("distance_method", remainingArgs[++i]);
            } else if ("-k".equals(remainingArgs[i])) {
//                job.getConfiguration().setBoolean("sensitive", true);
                job.getConfiguration().set("k", remainingArgs[++i]);
            } else if ("-rate".equals(remainingArgs[i])) {
                job.getConfiguration().setDouble("reserve_rate", Double.parseDouble(remainingArgs[++i]));
            }
            else {
                otherArgs.add(remainingArgs[i]);
            }
        }

        job.setJarByClass(KNN.class);   //为了能够找到wordcount这个类
        job.setMapperClass(CalcuDistanceMapper.class);   //指定map类型
        job.setCombinerClass(ReserverKCombiner.class); //设置Combine类
        job.setReducerClass(Reducer1.class); //设置Reducer类

        job.setMapOutputKeyClass(IdDistance.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        job.setPartitionerClass(IdPartitioner.class);
//        job.addCacheFile(new Path(HDFS_URL+"/user/jzt/patterns/punctuation.txt").toUri());
//        job.getConfiguration().setBoolean("wordcount.skipP.patterns", true);
//        job.addCacheFile(new Path(HDFS_URL+"/user/jzt/patterns/stop-word-list.txt").toUri());
//        job.getConfiguration().setBoolean("wordcount.skipS.patterns", true);

        FileInputFormat.addInputPath(job, new Path(HDFS_URL + otherArgs.get(0)));
        FileOutputFormat.setOutputPath(job, new Path(HDFS_URL + otherArgs.get(1)));
        job.waitForCompletion(true);


    }
}
