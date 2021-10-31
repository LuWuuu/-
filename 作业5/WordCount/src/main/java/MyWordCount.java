import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.hadoop.mapred.lib.ChainMapper;
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

public class MyWordCount {





    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {   //继承泛型类Mapper
        private final static IntWritable one = new IntWritable(1);  //定义hadoop数据类型IntWritable实例one，并且赋值为1
        private Text word = new Text();                                    //定义hadoop数据类型Text实例word
        private Configuration conf = new Configuration();
        private Boolean caseSensitive;
        private ArrayList<String> PuncToSkip = new ArrayList<String>();
        private ArrayList<String> WordToSkip = new ArrayList<String>();

        public void setup(Context context) throws IOException, InterruptedException {

            conf = context.getConfiguration();
            caseSensitive = conf.getBoolean("sensitive", false);
            try {
                if (conf.getBoolean("wordcount.skipP.patterns", false)) {
                    URI[] patternsURIs = context.getCacheFiles();
//                    URI[] patternsURIs = Job.getInstance(conf).getCacheFiles();
                    for (URI patternsURI : patternsURIs) {
                        Path patternsPath = new Path(patternsURI.getPath());
                        String patternsFileName = patternsPath.getName();
                        System.out.println("!!!!!!!!" + patternsFileName);
                        if (patternsFileName.contains("punctuation")) {
//                            parseSkipPunc(patternsFileName);
                        parseSkipPunc("hdfs://127.0.0.1:8900"+patternsURI.getPath());
                        }
                    }
                }
                if (conf.getBoolean("wordcount.skipS.patterns", false)) {
//                    URI[] patternsURIs = Job.getInstance(conf).getCacheFiles();
                    URI[] patternsURIs = context.getCacheFiles();
                    for (URI patternsURI : patternsURIs) {
                        Path patternsPath = new Path(patternsURI.getPath());
                        String patternsFileName = patternsPath.getName();
                        System.out.println("!!!!!!!!" + patternsFileName);
                        if (patternsFileName.contains("word")) {
//                            parseSkipWord(patternsFileName);
                            parseSkipWord("hdfs://127.0.0.1:8900"+patternsURI.getPath());
                        }
                    }
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }

        }

        private void parseSkipPunc(String filePath) {
            try {
                BufferedReader in = null;
                ArrayList<String> lstResult = new ArrayList<String>();
                Configuration conf = new Configuration();
                System.out.println("1:" + filePath);
                FileSystem fs = FileSystem.get(URI.create(filePath),conf);
                System.out.println("2:" + filePath);
                FSDataInputStream fin = fs.open(new Path(filePath));
                System.out.println("3:" + filePath);
                String line;
                in = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
                while ((line = in.readLine()) != null) {
                    System.out.println("PUNCPUNCPUNC!!!!!!!!!!" + line);
                    PuncToSkip.add(line);
                }


//                BufferedReader fis = new BufferedReader(new FileReader(filePath));
//                String pattern = null;
//                while ((pattern = fis.readLine()) != null) {
//                    PuncToSkip.add(pattern);
//                }
            } catch (IOException ioe) {
                System.err.println("Caught exception while parsing the cached file '" + StringUtils.stringifyException(ioe));
            }
        }

        private void parseSkipWord(String filePath) {
            try {
                BufferedReader in = null;
                ArrayList<String> lstResult = new ArrayList<String>();
                Configuration conf = new Configuration();
                FileSystem fs = FileSystem.get(URI.create(filePath),conf);
                FSDataInputStream fin = fs.open(new Path(filePath));
                String line;
                in = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
                while ((line = in.readLine()) != null) {
                    WordToSkip.add(line);
                }

//                BufferedReader fis = new BufferedReader(new FileReader(fileName));
//                String pattern = null;
//                while ((pattern = fis.readLine()) != null) {
//                    WordToSkip.add(pattern);
//                }
            } catch (IOException ioe) {
                System.err.println("Caught exception while parsing the cached file '" + StringUtils.stringifyException(ioe));
            }
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException { //实现map函数
            // 试图在map过程中设置全局参数并在reduce过程中读取，未果。
//                try {
//                    InputSplit split = context.getInputSplit();
//                    FileSplit fileSplit = (FileSplit) split;
//                    String filePath = fileSplit.getPath().toUri().getPath();
////                    System.out.println("input path:" + filePath);
//                    Configuration conf = context.getConfiguration();
//                    conf.setInt("mapreduce.map.input.file", 4);
//                    System.out.println("map conf:" + conf);
//                } catch (Exception exc){
//                    System.out.println("err");
//                    System.out.println(exc);
//                }
            String line = (caseSensitive) ? value.toString() : value.toString().toLowerCase();
            for (String pattern : PuncToSkip) {
                line = line.replace(pattern, "");
            }
//                for (String pattern : WordToSkip) {
//                    if (line.startsWith(pattern)) {
//                        line.replaceFirst(pattern+"\\s","");
//                    }
//                    line = line.replaceAll("\\s"+pattern+"\\s"," ");
//                }
            StringTokenizer itr = new StringTokenizer(line);  //Java的字符串分解类，默认分隔符“空格”、“制表符(‘\t’)”、“换行符(‘\n’)”、“回车符(‘\r’)”
            while (itr.hasMoreTokens()) {  //循环条件表示返回是否还有分隔符。
                String wo = itr.nextToken();
                if (!WordToSkip.contains(wo) && !wo.matches("^[0-9]*$") && wo.length() >= 3) {
                    word.set(wo);
//                    word.set(itr.nextToken());   // nextToken()：返回从当前位置到下一个分隔符的字符串，word.set()：Java数据类型与hadoop数据类型转换
                    try {
                        context.write(word, one);   //hadoop全局类context输出函数write;
                    } catch (Exception exc) {
                        System.out.println("err");
                        System.out.println(exc);
                    }
                }
            }
        }

    }

    public static class IntSumCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {    //继承泛型类Reducer
        private IntWritable result = new IntWritable();   //实例化IntWritable

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {  //实现reduce
            try {
                int sum = 0;
                for (IntWritable val : values)    //循环values，并记录单词个数
                    sum += val.get();
                result.set(sum);   //Java数据类型sum，转换为hadoop数据类型result
                context.write(key, result);   //输出结果到hdfs

            } catch (Exception exc) {
                System.out.println(exc.getStackTrace());
            }
        }
    }

    public static class IntSumReducer extends Reducer<Text, IntWritable, IntWritable, Text> {    //继承泛型类Reducer
        private IntWritable result = new IntWritable();   //实例化IntWritable
        private MultipleOutputs<IntWritable, Text> mos;

        protected void setup(Context context) throws IOException, InterruptedException {
            mos = new MultipleOutputs(context);
        }

        protected void cleanup(Context context) throws IOException, InterruptedException {
            super.cleanup(context);
            mos.close();
        }

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {  //实现reduce
            Configuration conf = context.getConfiguration();
            String filePath = conf.get("mapreduce.inputpath");
//            String filePath = context.getConfiguration().getBoolean("mapreduce.map.input.file");
//            System.out.println(filePath);
            Path tmpFilePath = new Path(filePath);
            String fileName = tmpFilePath.getName();
            try {
                int sum = 0;
                for (IntWritable val : values)
                    sum += val.get();
                result.set(sum);
//                context.write(result, key);
                mos.write(result, key, fileName);
            } catch (Exception exc) {
                System.out.println(exc.getStackTrace());
            }
        }
    }

    public static class ReverseIntWritable implements WritableComparable<ReverseIntWritable> {
        private int value;

        public ReverseIntWritable() {
        }

        public ReverseIntWritable(int value) {
            this.set(value);
        }

        public void set(int value) {
            this.value = value;
        }

        public int get() {
            return this.value;
        }

        public void readFields(DataInput in) throws IOException {
            this.value = in.readInt();
        }

        public void write(DataOutput out) throws IOException {
            out.writeInt(this.value);
        }

        public boolean equals(Object o) {
            if (!(o instanceof ReverseIntWritable)) {
                return false;
            } else {
                ReverseIntWritable other = (ReverseIntWritable) o;
                return this.value == other.value;
            }
        }

        public int hashCode() {
            return this.value;
        }

        public int compareTo(ReverseIntWritable o) {
            int thisValue = this.value;
            int thatValue = o.value;
            return thisValue > thatValue ? -1 : (thisValue == thatValue ? 0 : 1);
        }

        public String toString() {
            return Integer.toString(this.value);
        }

    }

    public static class Mapper2 extends Mapper<Object, Text, ReverseIntWritable, Text> {
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException { //实现map函数
            try {
                String line = value.toString();
                StringTokenizer itr = new StringTokenizer(line);
                while (itr.hasMoreTokens()) {  //循环条件表示返回是否还有分隔符。
                    int num = Integer.parseInt(itr.nextToken());
                    word.set(itr.nextToken());   // nextToken()：返回从当前位置到下一个分隔符的字符串，word.set()：Java数据类型与hadoop数据类型转换

                    ReverseIntWritable numIW = new ReverseIntWritable();
                    numIW.set(num);
                    try {
                        context.write(numIW, word);   //hadoop全局类context输出函数write;
                    } catch (Exception exc) {
                        System.out.println("err");
                        System.out.println(exc);
                    }
                }
            } catch (Exception exc) {
                System.out.println("err");
                System.out.println(exc);
            }
        }
    }

    public static class RankWord implements Writable {
        private int rank;
        private String word;

        public RankWord() {
        }

        public RankWord(int r, String w) {
            this.rank = r;
            this.word = w;
        }

        public void set(int r, String w) {
            this.rank = r;
            this.word = w;
        }

        public void readFields(DataInput in) throws IOException {
            this.rank = in.readInt();
            this.word = in.readUTF();
        }

        public void write(DataOutput out) throws IOException {
            out.writeInt(this.rank);
            out.writeUTF(this.word);
        }

        public String toString() {
            return this.rank + ":" + this.word;
        }
    }

    public static class Reducer2 extends Reducer<ReverseIntWritable, Text, RankWord, ReverseIntWritable> {
        private int occupied = 0;  // 统计词频前100的词中已经出现几个

        public void reduce(ReverseIntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {  //实现reduce
            try {
                for (Text word : values) {
                    if (occupied < 100) {
                        RankWord rankword = new RankWord();
                        rankword.set(occupied + 1, word.toString());
                        context.write(rankword, key);
                        occupied++;
                    }
                }
            } catch (Exception exc) {
                System.out.println(exc.getStackTrace());
            }
        }
    }

    public static class Mapper3 extends Mapper<Object, Text, Text, IntWritable> {
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException { //实现map函数
            try {
                String line = value.toString();
                StringTokenizer itr = new StringTokenizer(line);
                while (itr.hasMoreTokens()) {  //循环条件表示返回是否还有分隔符。
                    int num = Integer.parseInt(itr.nextToken());
                    word.set(itr.nextToken());   // nextToken()：返回从当前位置到下一个分隔符的字符串，word.set()：Java数据类型与hadoop数据类型转换

                    IntWritable numIW = new IntWritable();
                    numIW.set(num);
                    try {
                        context.write(word, numIW);   //hadoop全局类context输出函数write;
                    } catch (Exception exc) {
                        System.out.println("err");
                        System.out.println(exc);
                    }
                }
            } catch (Exception exc) {
                System.out.println("err");
                System.out.println(exc);
            }
        }
    }

    public static class Reducer3 extends Reducer<Text, IntWritable, IntWritable, Text> {    //继承泛型类Reducer
        private IntWritable result = new IntWritable();   //实例化IntWritable
        private MultipleOutputs<IntWritable, Text> mos;

        protected void setup(Context context) throws IOException, InterruptedException {
            mos = new MultipleOutputs(context);
        }

        protected void cleanup(Context context) throws IOException, InterruptedException {
            super.cleanup(context);
            mos.close();
        }

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {  //实现reduce
//            String filePath = context.getConfiguration().getBoolean("mapreduce.map.input.file");
//            System.out.println(filePath);
            try {
                int sum = 0;
                for (IntWritable val : values)    //循环values，并记录单词个数
                    sum += val.get();
                result.set(sum);   //Java数据类型sum，转换为hadoop数据类型result
//                context.write(result, key);   //输出结果到hdfs
                mos.write(result, key, "totalStatistics");
            } catch (Exception exc) {
                System.out.println(exc.getStackTrace());
            }
        }
    }

    public static class CommaRecordWriter extends RecordWriter<RankWord, ReverseIntWritable> {
        private FSDataOutputStream out;
        private String separator = ",";

        public CommaRecordWriter() {
        }

        public CommaRecordWriter(FSDataOutputStream fileOut) {
            this.out = fileOut;
        }

        public void write(RankWord key, ReverseIntWritable value) throws IOException, InterruptedException {
            try{
                out.write((key.toString() + separator + value.toString()).getBytes(StandardCharsets.UTF_8));
                this.out.write("\n".getBytes(StandardCharsets.UTF_8));
            } catch (Exception exc) {
                System.out.println("err");
                System.out.println(exc);
            }
        }

        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
            out.close();
        }
    }

    public static class SPOutputFormat extends FileOutputFormat<RankWord, ReverseIntWritable> {

        public SPOutputFormat() {
        }

        @Override
        public RecordWriter<RankWord, ReverseIntWritable> getRecordWriter(TaskAttemptContext job) throws IOException,
                InterruptedException {
//            Path outputDir = FileOutputFormat.getOutputPath(job);
//            String subfix = job.getTaskAttemptID().getTaskID().toString();
//            Path path = new Path(outputDir.toString()+"/"+prefix+subfix.substring(subfix.length()-5,subfix.length()));
//            FSDataOutputStream fileOut = path.getFileSystem(job.getConfiguration()).create(path);
//            return new CommaRecordWriter(fileOut);
            Path outputDir = FileOutputFormat.getOutputPath(job);
            String[] sArray = outputDir.toString().split("\\/");
            String outputPath = outputDir.toString()+'/'+sArray[sArray.length-1]+".txt";
            Path path = new Path(outputPath);
//            System.out.println("outputPath: " + outputPath);
//            System.out.println("path: " + path);
            URI output = URI.create(outputPath);

            Configuration conf = job.getConfiguration();
            FileSystem fs = FileSystem.get(output, conf);
            FSDataOutputStream out = fs.create(path);
            RecordWriter<RankWord, ReverseIntWritable> commaRecordWriter = new CommaRecordWriter(out);
            return commaRecordWriter;
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();   //实例化Configuration
        String HDFS_URL = "hdfs://127.0.0.1:8900";
        String[] remainingArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

// //如果只有一个路径，则输出需要有输入路径和输出路径
// if (otherArgs.length < 2) {
// System.err.println("Usage: wordcount <in> [<in>...] <out>");
// System.exit(2);
// }
// if(!(remainingArgs.length != 2 || remainingArgs.length != 4)) {
//     System.err.println("Usage: wordcount <in> <out> [-skip skipPatternFile]");
//     System.exit(2);
// }

//        Job job = Job.getInstance(conf, "word count");   //实例化job
//
//        job.setJarByClass(MyWordCount.class);   //为了能够找到wordcount这个类
//        job.setMapperClass(TokenizerMapper.class);   //指定map类型
//        job.setCombinerClass(IntSumCombiner.class); //设置Combine类
//        job.setReducerClass(IntSumReducer.class); //设置Reducer类
//
//        job.setMapOutputKeyClass(Text.class);
//        job.setMapOutputValueClass(IntWritable.class);
//
//        job.setOutputKeyClass(IntWritable.class); //设置job输出的key
////设置job输出的value
//        job.setOutputValueClass(Text.class);


        List<String> otherArgs = new ArrayList<String>();
        for (int i = 0; i < remainingArgs.length; ++i) {
            if ("-skipP".equals(remainingArgs[i])) {
                i++;
//                job.addCacheFile(new Path(remainingArgs[++i]).toUri());
//                job.getConfiguration().setBoolean("wordcount.skipP.patterns", true);
            } else if ("-skipS".equals(remainingArgs[i])) {
                i++;
//                job.addCacheFile(new Path(remainingArgs[++i]).toUri());
//                job.getConfiguration().setBoolean("wordcount.skipS.patterns", true);
            } else if ("-sensitive".equals(remainingArgs[i])) {
//                job.getConfiguration().setBoolean("sensitive", true);
            } else {
                otherArgs.add(remainingArgs[i]);
            }
        }

        String path = HDFS_URL+otherArgs.get(0);
        File file = new File(path);
        Configuration tempConf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(path), tempConf);
        FileStatus fileList[] = fs.listStatus(new Path(path));
//        File[] fs = file.listFiles();
        for (FileStatus f : fileList) {
            if (!f.isDirectory()) {
                conf = new Configuration();
                Job job = Job.getInstance(conf, "word count");   //实例化job

                job.setJarByClass(MyWordCount.class);   //为了能够找到wordcount这个类
                job.setMapperClass(TokenizerMapper.class);   //指定map类型
                job.setCombinerClass(IntSumCombiner.class); //设置Combine类
                job.setReducerClass(IntSumReducer.class); //设置Reducer类

                job.setMapOutputKeyClass(Text.class);
                job.setMapOutputValueClass(IntWritable.class);

                job.setOutputKeyClass(IntWritable.class); //设置job输出的key
//设置job输出的value
                job.setOutputValueClass(Text.class);

//                List<String> otherArgs = new ArrayList<String>();
//                for (int i = 0; i < remainingArgs.length; ++i) {
//                    if ("-skipP".equals(remainingArgs[i])) {
//                        job.addCacheFile(new Path(remainingArgs[++i]).toUri());
//                        job.getConfiguration().setBoolean("wordcount.skipP.patterns", true);
//                    } else if ("-skipS".equals(remainingArgs[i])) {
//                        job.addCacheFile(new Path(remainingArgs[++i]).toUri());
//                        job.getConfiguration().setBoolean("wordcount.skipS.patterns", true);
//                    } else if ("-sensitive".equals(remainingArgs[i])) {
//                        job.getConfiguration().setBoolean("sensitive", true);
//                    } else {
////                        otherArgs.add(remainingArgs[i]);
//                    }
//                }

                job.addCacheFile(new Path(HDFS_URL+"/user/jzt/patterns/punctuation.txt").toUri());
                job.getConfiguration().setBoolean("wordcount.skipP.patterns", true);
                job.addCacheFile(new Path(HDFS_URL+"/user/jzt/patterns/stop-word-list.txt").toUri());
                job.getConfiguration().setBoolean("wordcount.skipS.patterns", true);

                FileInputFormat.addInputPath(job, new Path(f.getPath().toString()));
                job.getConfiguration().set("mapreduce.inputpath", f.getPath().toString());
                FileOutputFormat.setOutputPath(job, new Path(HDFS_URL+"/user/jzt/midout7/"+f.getPath().getName().split("\\.")[0]));
                job.waitForCompletion(true);
                Configuration conf2 = new Configuration();
                Job job2 = Job.getInstance(conf2, "word count2");   //实例化job
                job2.setJarByClass(MyWordCount.class);   //为了能够找到wordcount这个类

                job2.setOutputFormatClass(SPOutputFormat.class);
                job2.setMapperClass(Mapper2.class);   //指定map类型
                job2.setReducerClass(Reducer2.class); //设置Reducer类

                job2.setMapOutputKeyClass(ReverseIntWritable.class);
                job2.setMapOutputValueClass(Text.class);

                job2.setOutputKeyClass(RankWord.class); //设置job输出的key
                job2.setOutputValueClass(ReverseIntWritable.class);
                job2.setNumReduceTasks(1);
                FileInputFormat.addInputPath(job2, new Path(HDFS_URL+"/user/jzt/midout7/"+f.getPath().getName().split("\\.")[0]));
                FileOutputFormat.setOutputPath(job2, new Path(HDFS_URL+otherArgs.get(1)+'/'+f.getPath().getName().split("\\.")[0]));   //添加输出路径
                job2.waitForCompletion(true);


            }
        }
        Configuration finalConf = new Configuration();
        Job finalJob = Job.getInstance(finalConf, "final wordcount");
        finalJob.setJarByClass(MyWordCount.class);
//        finalJob.setOutputFormatClass(SPOutputFormat.class);
        finalJob.setMapperClass(Mapper3.class);   //指定map类型
        finalJob.setReducerClass(Reducer3.class); //设置Reducer类

        finalJob.setMapOutputKeyClass(Text.class);
        finalJob.setMapOutputValueClass(IntWritable.class);

        finalJob.setOutputKeyClass(IntWritable.class); //设置job输出的key
        finalJob.setOutputValueClass(Text.class);

        String allPath = "";
        for (FileStatus f : fileList) {
            allPath = allPath + HDFS_URL+"/user/jzt/midout7/"+f.getPath().getName().split("\\.")[0]+",";
//            FileInputFormat.addInputPath(finalJob, new Path(otherArgs.get(1)+f.getName()));
//            FileOutputFormat.setOutputPath(finalJob, new Path(otherArgs.get(2)+"/TotalStatistics"));
        }
        allPath = allPath.substring(0,allPath.length()-1);
        FileInputFormat.addInputPaths(finalJob, allPath);
        FileOutputFormat.setOutputPath(finalJob, new Path(HDFS_URL+"/user/jzt/midout7/"+"TotalStatistics"));
        finalJob.waitForCompletion(true);
        Configuration finalConf2 = new Configuration();
        Job finalJob2 = Job.getInstance(finalConf2, "final wordcount2");   //实例化job
        finalJob2.setJarByClass(MyWordCount.class);   //为了能够找到wordcount这个类

        finalJob2.setOutputFormatClass(SPOutputFormat.class);
        finalJob2.setMapperClass(Mapper2.class);   //指定map类型
        finalJob2.setReducerClass(Reducer2.class); //设置Reducer类

        finalJob2.setMapOutputKeyClass(ReverseIntWritable.class);
        finalJob2.setMapOutputValueClass(Text.class);

        finalJob2.setOutputKeyClass(RankWord.class); //设置job输出的key
        finalJob2.setOutputValueClass(ReverseIntWritable.class);
        finalJob2.setNumReduceTasks(1);
        FileInputFormat.addInputPath(finalJob2, new Path(HDFS_URL+"/user/jzt/midout7/"+"TotalStatistics"));
        FileOutputFormat.setOutputPath(finalJob2, new Path(HDFS_URL+otherArgs.get(1)+"/TotalStatistics"));   //添加输出路径
        System.exit(finalJob2.waitForCompletion(true) ? 0 : 1);
    }
}

//        FileInputFormat.addInputPath(job, new Path(otherArgs.get(0)));
//        job.getConfiguration().set("mapreduce.inputpath", otherArgs.get(0));
//        FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));   //添加输出路径
//
////        System.exit(job.waitForCompletion(true) ? 0 : 1);  //提交job
//        job.waitForCompletion(true);
//
//
//        Configuration conf2 = new Configuration();
//        Job job2 = Job.getInstance(conf2, "word count2");   //实例化job
//        job2.setJarByClass(MyWordCount.class);   //为了能够找到wordcount这个类
//
//        job2.setOutputFormatClass(SPOutputFormat.class);
////        SPOutputFormat.setOutputPath(job2, new Path("E:\\IDEA\\ProjectSpace\\myhadoop3"));
//
//        job2.setMapperClass(Mapper2.class);   //指定map类型
////        job2.setCombinerClass(IntSumCombiner.class); //设置Combine类
//        job2.setReducerClass(Reducer2.class); //设置Reducer类
//
//        job2.setMapOutputKeyClass(ReverseIntWritable.class);
//        job2.setMapOutputValueClass(Text.class);
//
//        job2.setOutputKeyClass(RankWord.class); //设置job输出的key
//        job2.setOutputValueClass(ReverseIntWritable.class);
//        job2.setNumReduceTasks(1);
//
////        conf2.set("mapred.textoutputformat.separator", ",");
//
//        FileInputFormat.addInputPath(job2, new Path(otherArgs.get(1) ));
//        FileOutputFormat.setOutputPath(job2, new Path(otherArgs.get(2)));   //添加输出路径
//        System.exit(job2.waitForCompletion(true) ? 0 : 1);  //提交job
//    }
//}
