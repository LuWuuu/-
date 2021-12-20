import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URI;

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

public class IndustryDefault {
    public static class IndustryMapper extends Mapper<Object, Text, Text, IntWritable> {   //继承泛型类Mapper
        private final static IntWritable one = new IntWritable(1);  //定义hadoop数据类型IntWritable实例one，并且赋值为1

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException { //实现map函数
            String[] words = value.toString().split(",");
            String industry = words[10];
            if (!industry.equals("industry"))
                context.write(new Text(industry), one);
        }

    }

    public static class IndustryReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable value:values) {
                sum = sum + value.get();
            }
            context.write(key,new IntWritable(sum));
        }

    }

    public static class SortMapper extends Mapper<Text, IntWritable, Text, IntWritable> {
        HashMap<String, Integer> hm;
        protected void setup(Context context) throws IOException, InterruptedException{
            hm = new HashMap<String, Integer>();
        }

        public void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException {
            hm.put(key.toString(),value.get());
        }

        protected void cleanup(Context context) throws IOException, InterruptedException {
            List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(hm.entrySet());
            list.sort(new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            for (Map.Entry<String, Integer> entry: list) {
                context.write(new Text(entry.getKey()), new IntWritable(entry.getValue()));
            }
        }

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();   //实例化Configuration
        String HDFS_URL = "hdfs://127.0.0.1:8900";
        String[] remainingArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        Job job = Job.getInstance(conf, "IndustryDefault");
        List<String> otherArgs = new ArrayList<String>();
        for (int i = 0; i < remainingArgs.length; ++i) {
            otherArgs.add(remainingArgs[i]);
        }

        job.setJarByClass(IndustryDefault.class);
        Configuration map1Conf = new Configuration(false);
        ChainMapper.addMapper(job,IndustryMapper.class,Object.class,Text.class,Text.class,IntWritable.class,map1Conf);
        Configuration reduce1Conf = new Configuration(false);
        ChainReducer.setReducer(job, IndustryReducer.class,Text.class,IntWritable.class,Text.class,IntWritable.class, reduce1Conf);
        Configuration map2Conf = new Configuration(false);
        ChainReducer.addMapper(job,SortMapper.class,Text.class,IntWritable.class,Text.class,IntWritable.class,map2Conf);

//        job.setMapperClass(IndustryMapper.class);
////        job.setCombinerClass(ReserverKCombiner.class);
//        job.setReducerClass(IndustryReducer.class);
//
//        job.setMapOutputKeyClass(Text.class);
//        job.setMapOutputValueClass(IntWritable.class);
//
//        job.setOutputKeyClass(Text.class);
//        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(HDFS_URL + otherArgs.get(0)));
        FileOutputFormat.setOutputPath(job, new Path(HDFS_URL + otherArgs.get(1)));
        job.waitForCompletion(true);


    }
}
