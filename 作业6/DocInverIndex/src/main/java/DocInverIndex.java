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


public class DocInverIndex {
    public static class WordDocid implements WritableComparable<WordDocid> {
        private String word;
        private String docid;

        public WordDocid() {
            set("","");
        }

        public WordDocid(String word, String docid) {
            this.word = word;
            this.docid = docid;
        }

        public void set(String word, String docid) {
            this.word = word;
            this.docid = docid;
        }

        public void readFields(DataInput in) throws IOException {
            this.word = in.readUTF();
            this.docid = in.readUTF();
        }

        public void write(DataOutput out) throws IOException {
            out.writeUTF(this.word);
            out.writeUTF(this.docid);
        }

        public String toString() {
            return this.word + ":" + this.docid;
        }

        public int compareTo(WordDocid o) {
            if (!this.word.equals(o.word)) return this.word.compareTo(o.word);
            else {
                return this.docid.compareTo(o.docid);
            }
        }
    }

    public static class WordNumber implements WritableComparable<WordNumber> {
        private String word;
        private int number;

        public WordNumber() {
        }

        public WordNumber(String word, int number) {
            this.word = word;
            this.number = number;
        }

        public void set(String word, int number) {
            this.word = word;
            this.number = number;
        }

        public void readFields(DataInput in) throws IOException {
            this.word = in.readUTF();
            this.number = in.readInt();
        }

        public void write(DataOutput out) throws IOException {
            out.writeUTF(this.word);
            out.writeInt(this.number);
        }

        public String toString() {
            return this.word + ":" + this.number;
        }

        public int compareTo(WordNumber o) {
            if (!this.word.equals(o.word)) return this.word.compareTo(o.word);
            else {
                return this.number > o.number ? -1 : (this.number == o.number ? 0 : 1);
            }
        }
    }

    public static class DocidNumber implements WritableComparable<DocidNumber> {
        private String docid;
        private int number;

        public DocidNumber() {
        }

        public DocidNumber(String docid, int number) {
            this.docid = docid;
            this.number = number;
        }

        public void set(String docid, int number) {
            this.docid = docid;
            this.number = number;
        }

        public void readFields(DataInput in) throws IOException {
            this.docid = in.readUTF();
            this.number = in.readInt();
        }

        public void write(DataOutput out) throws IOException {
            out.writeUTF(this.docid);
            out.writeInt(this.number);
        }

        public String toString() {
            return this.docid + ":" + this.number;
        }

        public int compareTo(DocidNumber o) {
            if (!(this.number == o.number)) return this.number > o.number ? -1 : (this.number == o.number ? 0 : 1);
            else {
                return this.docid.compareTo(o.docid);
            }
        }
    }

    public static class DocidNumberComparator implements Comparator {
        public int compare(Object arg1, Object arg2) {
            DocidNumber o1 = (DocidNumber) arg1;
            DocidNumber o2 = (DocidNumber) arg2;
            if (!(o1.number == o2.number)) return o1.number > o2.number ? -1 : (o1.number == o2.number ? 0 : 1);
            else {
                return o1.docid.compareTo(o2.docid);
        }
    }
    }

    public static class TokenizerMapper extends Mapper<Object, Text, WordDocid, IntWritable> {   //继承泛型类Mapper
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
                        if (patternsFileName.contains("punctuation")) {
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
                        if (patternsFileName.contains("word")) {
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
//                ArrayList<String> lstResult = new ArrayList<String>();
                Configuration conf = new Configuration();
                FileSystem fs = FileSystem.get(URI.create(filePath),conf);
                FSDataInputStream fin = fs.open(new Path(filePath));
                String line;
                in = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
                while ((line = in.readLine()) != null) {
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
//                ArrayList<String> lstResult = new ArrayList<String>();
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
            Configuration conf = context.getConfiguration();
            String docid = "";

            try {
                InputSplit split = context.getInputSplit();
                FileSplit fileSplit = (FileSplit) split;
                Path filePath = fileSplit.getPath();
                docid = filePath.getName();
            } catch (Exception exc) {
                System.out.println(exc);
            }

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
                    WordDocid result = new WordDocid(word.toString(),docid);
                    context.write(result, one);

                }
            }
        }

    }

//    public static class PostingReducer extends Reducer<WordDocid, IntWritable, Text, Text> {    //继承泛型类Reducer
//        private IntWritable result = new IntWritable();   //实例化IntWritable
//        private String wordPrev = "";
//        private String wordNow = "";
//        private String postings = "";
////        private MultipleOutputs<IntWritable, Text> mos;
//
////        protected void setup(Context context) throws IOException, InterruptedException {
////            mos = new MultipleOutputs(context);
////        }
////
////        protected void cleanup(Context context) throws IOException, InterruptedException {
////            super.cleanup(context);
////            mos.close();
////        }
//
//        public void reduce(WordDocid key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
////            Configuration conf = context.getConfiguration();
////            String filePath = conf.get("mapreduce.inputpath");
////            Path tmpFilePath = new Path(filePath);
////            String fileName = tmpFilePath.getName();
//            try {
//                int sum = 0;
//                for (IntWritable val : values)
//                    sum += val.get();
//                String word = key.word;
//                String docid = key.docid;
//                wordNow = word;
//                if (!wordPrev.equals(word) && !wordPrev.equals("")) {
//                    context.write(new Text(wordPrev), new Text(postings));
//                    postings = "";
//                }
//                postings = postings + docid + ":" + sum + ";";
//                wordPrev = word;
//            } catch (Exception exc) {
//                System.out.println(exc.getStackTrace());
//            }
//        }
//        protected void cleanup(Context context) throws IOException, InterruptedException {
//            context.write(new Text(wordNow), new Text(postings));
//            super.cleanup(context);
//        }
//    }

    public static class SumCombiner extends Reducer<WordDocid, IntWritable, WordDocid, IntWritable> {    //继承泛型类Reducer
        private IntWritable result = new IntWritable();   //实例化IntWritable
//        private MultipleOutputs<IntWritable, Text> mos;

//        protected void setup(Context context) throws IOException, InterruptedException {
//            mos = new MultipleOutputs(context);
//        }
//
//        protected void cleanup(Context context) throws IOException, InterruptedException {
//            super.cleanup(context);
//            mos.close();
//        }

        public void reduce(WordDocid key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
//            Configuration conf = context.getConfiguration();
//            String filePath = conf.get("mapreduce.inputpath");
//            Path tmpFilePath = new Path(filePath);
//            String fileName = tmpFilePath.getName();
            try {
                int sum = 0;
                for (IntWritable val : values)
                    sum += val.get();
                result.set(sum);
                context.write(key, result);
            } catch (Exception exc) {
                System.out.println(exc.getStackTrace());
            }
        }
    }

    public static class Reducer1 extends Reducer<WordDocid, IntWritable, WordNumber, Text> {    //继承泛型类Reducer
        private IntWritable result = new IntWritable();   //实例化IntWritable
//        private MultipleOutputs<IntWritable, Text> mos;

//        protected void setup(Context context) throws IOException, InterruptedException {
//            mos = new MultipleOutputs(context);
//        }
//
//        protected void cleanup(Context context) throws IOException, InterruptedException {
//            super.cleanup(context);
//            mos.close();
//        }

        public void reduce(WordDocid key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
//            Configuration conf = context.getConfiguration();
//            String filePath = conf.get("mapreduce.inputpath");
//            Path tmpFilePath = new Path(filePath);
//            String fileName = tmpFilePath.getName();
            try {
                String word = key.word;
                String docid = key.docid;
                int sum = 0;
                for (IntWritable val : values)
                    sum += val.get();
                result.set(sum);

                WordNumber wordNumber = new WordNumber(word, sum);
                context.write(wordNumber, new Text(docid));
            } catch (Exception exc) {
                exc.printStackTrace();
//                System.out.println(exc);
            }
        }
    }

    public static class PostingMapper extends Mapper<WordNumber, Text, Text, Text> {    //继承泛型类Reducer
        private String wordPrev = "";
        private String wordNow = "";
        private String postings = "";
        private ArrayList<DocidNumber> postingList = new ArrayList<>();
//        private MultipleOutputs<IntWritable, Text> mos;

//        protected void setup(Context context) throws IOException, InterruptedException {
//            mos = new MultipleOutputs(context);
//        }
//
//        protected void cleanup(Context context) throws IOException, InterruptedException {
//            super.cleanup(context);
//            mos.close();
//        }

        public void map(WordNumber key, Text value, Context context) throws IOException, InterruptedException {
//            Configuration conf = context.getConfiguration();
//            String filePath = conf.get("mapreduce.inputpath");
//            Path tmpFilePath = new Path(filePath);
//            String fileName = tmpFilePath.getName();
            String word = key.word;
            int number = key.number;
            wordNow = word;
            if (!wordPrev.equals(word) && !wordPrev.equals("")) {
                postingList.sort(new DocidNumberComparator());
                for(DocidNumber item : postingList){
                    postings = postings + item.docid + "#" + item.number + ",";
                }
                postings = postings.substring(0,postings.length()-1);
                context.write(new Text(wordPrev+":"), new Text(postings));
                postings = "";
                postingList = new ArrayList<>();
            }
            DocidNumber docidNumber = new DocidNumber(value.toString(),number);
            postingList.add(docidNumber);
            wordPrev = word;

        }
        protected void cleanup(Context context) throws IOException, InterruptedException {
            postingList.sort(new DocidNumberComparator());
            for(DocidNumber item : postingList){
                postings = postings + item.docid + "#" + item.number + ",";
            }
            postings = postings.substring(0,postings.length()-1);
            context.write(new Text(wordPrev), new Text(postings));
            super.cleanup(context);
        }
    }

    public static class Mapper3 extends Mapper<Text, Text, Text, Text>{
        public void map(Text key, Text value, Context context) throws IOException, InterruptedException{
            context.write(key,value);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();   //实例化Configuration
        String HDFS_URL = "hdfs://127.0.0.1:8900";
        String[] remainingArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        Job job = Job.getInstance(conf, "DocInverIndex");   //实例化job

        List<String> otherArgs = new ArrayList<String>();
        for (int i = 0; i < remainingArgs.length; ++i) {
            if ("-skipP".equals(remainingArgs[i])) {
                job.addCacheFile(new Path(HDFS_URL + remainingArgs[++i]).toUri());
                job.getConfiguration().setBoolean("wordcount.skipP.patterns", true);
            } else if ("-skipS".equals(remainingArgs[i])) {
                job.addCacheFile(new Path(HDFS_URL + remainingArgs[++i]).toUri());
                job.getConfiguration().setBoolean("wordcount.skipS.patterns", true);
            } else if ("-sensitive".equals(remainingArgs[i])) {
//                job.getConfiguration().setBoolean("sensitive", true);
            } else {
                otherArgs.add(remainingArgs[i]);
            }
        }

        job.setJarByClass(DocInverIndex.class);
        Configuration map1Conf = new Configuration(false);
        ChainMapper.addMapper(job,TokenizerMapper.class,Object.class,Text.class,WordDocid.class,IntWritable.class,map1Conf);
        Configuration reduce1Conf = new Configuration(false);
        ChainReducer.setReducer(job, Reducer1.class,WordDocid.class,IntWritable.class,WordNumber.class,Text.class, reduce1Conf);
        Configuration map2Conf = new Configuration(false);
        ChainReducer.addMapper(job, PostingMapper.class, WordNumber.class, Text.class,Text.class,Text.class,map2Conf);
//        job.setJarByClass(DocInverIndex.class);
//        job.setMapperClass(TokenizerMapper.class);   //指定map类型
//        job.setCombinerClass(SumCombiner.class); //设置Combine类
//        job.setReducerClass(PostingReducer.class); //设置Reducer类
//
//        job.setMapOutputKeyClass(WordDocid.class);
//        job.setMapOutputValueClass(IntWritable.class);
//
//        job.setOutputKeyClass(Text.class); //设置job输出的key
//        job.setOutputValueClass(Text.class);

//        job.addCacheFile(new Path(HDFS_URL+"/user/jzt/patterns/punctuation.txt").toUri());
//        job.getConfiguration().setBoolean("wordcount.skipP.patterns", true);
//        job.addCacheFile(new Path(HDFS_URL+"/user/jzt/patterns/stop-word-list.txt").toUri());
//        job.getConfiguration().setBoolean("wordcount.skipS.patterns", true);

        FileInputFormat.addInputPath(job, new Path(HDFS_URL + otherArgs.get(0)));
        FileOutputFormat.setOutputPath(job, new Path(HDFS_URL + otherArgs.get(1)));
        job.waitForCompletion(true);
    }
}
