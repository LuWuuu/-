## 《金融大数据处理技术》实验3报告

嵇泽同 191870068

[TOC]



### 一、环境设置

Ubuntu16.04 + Hadoop3.2.2 + HBase2.4.8 + maven + IDEA

pom.xml：

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>hbase</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.11</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>3.2.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-common</artifactId>
            <version>3.2.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-hdfs</artifactId>
            <version>3.2.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-it</artifactId>
            <version>2.4.8</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-client</artifactId>
            <version>2.4.8</version>
        </dependency>
    </dependencies>
</project>
```



### 二、HBase的安装和配置

HBase的安装和配置按照教程上的步骤执行，较为顺利。

单机模式：

![image-20211123105351620](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211123105351620.png)

伪分布模式：

hbase-site.xml文件配置：

![image-20211130104512187](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130104512187.png)

jps：

![image-20211130104632976](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130104632976.png)

localhost:16010:

![image-20211130105007071](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130105007071.png)

hdfs:

![image-20211130105137088](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130105137088.png)

### 三.HBase表格设计

本次试验中，我将所给关系数据库中的三张表合成为HBase中的一张表，格式如下：

![image-20211130105535313](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130105535313.png)

关于行键（Row Key）的设计：

由于本次实验中数据较少，而且以学号作为行键本身也不怎么会出现数据热点问题，因此本次实验中我没有对行键采取特殊的处理（如加盐、哈希、颠倒等），直接以学号作为行键。

关于列族的设计：

本次实验中我将学生的基础个人信息放在列族“Basic Info”中，该列族后续还可以添加其他基础信息列；

将学生的联系方式放在列族“Contact”中，该列族除了目前已有的邮箱列，后续还可以添加如电话号码列、地址列等等；

将每门课程单独设置为一个列族，该列族存储该课程的基础信息和该学生与该课程间的关联，如果该学生没有选修该课程，那么该行对应的该列族数据为空，比较易于理解。

其实当完成实验后，回过头来审视这张表时，我发觉关于课程的列族设计其实似乎欠缺合理性：

目前的课程只有三门，将每门课程都设置为一个列族可以轻易实现；但实际中，一个学校的所有课程门数是非常大的，如果将每门课程都设置为一个列族，那么将造成非常多的列族和空列族，而HBase的列族是不建议过多的，因此当数据量增大时，该表结构的设计是不合理的。可以考虑转为“开辟一个“Score”列族、下面存储该学生选修的所有课程的分数”这样的设计，或者设计多个表，不要将所有数据都放在一个表里。由于时间紧张且本人缺乏此类设计经验，因此在本次实验中没有较好地设计出这类表格，也是本次实验的一个不足和缺憾。

### 四、JAVA编程实现

在JAVA编程实现前，我先上网查找了一些常用类和常用方法，如下所示：

HBaseAdmin类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218091841514.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

HTable类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218093232308.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

HTableDescriptor类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218093640359.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

HColumnDescriptor类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218094212508.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

Put类的主要方法：

![img](https://img-blog.csdnimg.cn/2018121809521231.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

Get类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218095345914.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

Result类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218095617434.png)

![img](https://img-blog.csdnimg.cn/20181218095650201.png)

ResultScanner类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218100743265.png)

Scan类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218100945922.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

本次试验中，我将对于HBase的操作封装在一个类Hbase中：

![image-20211130112526160](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130112526160.png)

该类具有属性conf、connection和admin，用来绑定指定的hbase，当创建该类的实例后，通过getconnect()方法来获取并设置这些属性的值。之后该实例就与hbase绑定，通过该类的其他自定义操作方法即可对hbase中的表进行各类操作。

#### ①创建表

```JAVA
test.createtable("student","BasicInfo", "Math", "Computer Science", "English");
```

```JAVA
//  根据表名和列族名创建表
public void createtable(String tableName, String... columnFamily) throws IOException{
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            admin.disableTable(name);
            admin.deleteTable(name);
            System.out.println(tableName.toString() + " is exist,delete it");
        }
        HTableDescriptor tb = new HTableDescriptor(name);
        for(int i = 0;i <columnFamily.length;i++) {
            HColumnDescriptor family = new HColumnDescriptor(columnFamily[i]);
            tb.addFamily(family);
        }
        admin.createTable(tb);
    }
```

该方法接收表名和列族名，先判断该表是否存在，若存在则先删除该表，然后创建HTableDescriptor实例，并调用admin.createTable()方法实现表的创建，较为简单。

#### ②插入数据

```JAVA
//  插入学生基础信息
test.putData("student", "2015001", "BasicInfo", "S_No", "2015001");
test.putData("student", "2015001", "BasicInfo", "S_Name", "Li Lei");
test.putData("student", "2015001", "BasicInfo", "S_Sex", "male");
test.putData("student", "2015001", "BasicInfo", "S_Age", "23");
......
    
//  插入课程信息
test.putData("student","2015001","Math","C_No","123001");
test.putData("student","2015001","Math","C_Name","Math");
test.putData("student","2015001","Math","C_Credit","2.0");
test.putData("student","2015001","English","C_No","123003");
test.putData("student","2015001","English","C_Name","English");
test.putData("student","2015001","English","C_Credit","3.0");
......
    
//  插入学生课程成绩
test.putData("student","2015001","Math","SC_Score","86");
test.putData("student","2015001","English","SC_Score","69");
test.putData("student","2015002","Computer Science","SC_Score","77");
test.putData("student","2015002","English","SC_Score","99");
test.putData("student","2015003","Math","SC_Score","98");
test.putData("student","2015003","Computer Science","SC_Score","95");
```

```JAVA
public void putData(String tableName, String rowKey, String familyName, String columnName, String value) throws Exception {
    //转化为表名
    TableName name = TableName.valueOf(tableName);
    //添加数据之前先判断表是否存在，不存在的话先创建表
    if(admin.tableExists(name)){
    }else {
        //根据表名创建表结构
        HTableDescriptor tableDescriptor = new HTableDescriptor(name);
        //定义列簇的名字
        HColumnDescriptor columnFamilyName = new HColumnDescriptor(familyName);
        tableDescriptor.addFamily(columnFamilyName);
        admin.createTable(tableDescriptor);
    }

    Table table = connection.getTable(name);
    Put put = new Put(rowKey.getBytes());

    put.addColumn(familyName.getBytes(), columnName.getBytes(), value.getBytes());
    table.put(put);
}
```

该方法接收要插入的表名、行键、列族名、列限定符和要插入的值，通过put实例的addColumn方法将这些参数传入，然后调用Table.put()方法将该值插入，也较为简单。

#### ③查询选修Computer Science的学生的成绩

```JAVA

//  查询选修Computer Science的学生的成绩
List<String> filterStrList = new ArrayList<>();
filterStrList.add("Computer Science,C_Name,Computer Science");
ResultScanner results = test.getResultByFilter("student", filterStrList);
for (Result result=results.next(); result!=null; result=results.next()) {
    List<Cell> cells = result.listCells();
    for (Cell cell:cells){
        String family = new String(cell.getFamilyArray(),cell.getFamilyOffset(),cell.getFamilyLength(),"UTF-8");
        String qualifier = new String(cell.getQualifierArray(),cell.getQualifierOffset(),cell.getQualifierLength(),"UTF-8");
        String value = new String(cell.getValueArray(),cell.getValueOffset(),cell.getValueLength(),"UTF-8");
        if (family.equals("BasicInfo") && qualifier.equals("S_No")) {
            System.out.println("S_No: " + value);
        }
        else if (family.equals("BasicInfo") && qualifier.equals("S_Name")) {
            System.out.println("S_Name: " + value);
        }
        else if (family.equals("Computer Science") && qualifier.equals("SC_Score")){
            System.out.println("SC_Score: " + value);
        }
    }
}
```

```JAVA

public ResultScanner getResultByFilter(String tableName, List<String> filterStrList) throws Exception{
    ResultScanner result;
    TableName name = TableName.valueOf(tableName);
    if (admin.tableExists(name)) {
        Table table = connection.getTable(name);
        FilterList filterList = new FilterList();
        Scan scan = new Scan();
        for(String filterStr: filterStrList){
            String[] conditions = filterStr.split(",");
            SingleColumnValueFilter filter = new SingleColumnValueFilter(conditions[0].getBytes(), conditions[1].getBytes(),
                    CompareFilter.CompareOp.EQUAL, conditions[2].getBytes());
            filter.setFilterIfMissing(true);
            filterList.addFilter(filter);
              s1.addColumn(Bytes.toBytes(s[0]), Bytes.toBytes(s[1]));
        }
        scan.setFilter(filterList);
        result = table.getScanner(scan);
    }
    else {
        result = null;
    }
    return result;
    }
```

查询操作相对较为复杂，网上关于filter的介绍和资料也很多、五花八门、包含许多高级用法。针对此次实验，我选取了其中一种较为简单适用的通过SingleColumnValueFilter来查询的方法。

SingleColumnFilter实例通过传入指定列族、列限定符、比较运算符、比较值来构建，该filter能够对于每一行，检验该行对应列下cell的值是否满足筛选条件，若满足，则记录该行并返回。值得一提的是，若某一行对应的列下没有数据，则默认情况下该filter也会记录该行并返回，在本次实验的数据中，这导致查询结果会返回所有行（因为选修了该课程的学生自然会被返回，而没选修该课程的学生，由于该列数据为空，因此也会被返回）。通过SingleColumnFilter.setFilterIfMissing(true)方法可以对其进行设置，使得不返回该cell没有值的行，此次我就是采取了这种方法。

此外，值得一提的是，设置filter、scan并通过Table.getScanner(scan)获得结果后，得到的返回值result是一个ResultScanner类的实例，还需要对该实例进行一定的解析操作才能获得想要的字符串值。该解析方法在此次实验中我在网上没有找到能直接适用的，可能是由于Hbase版本不同导致接口变化较大。所以我就通过查看该实例存在的方法、查看源代码等方式一步步摸索，最终成功输出了想要的格式：

![image-20211129155904648](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211129155904648.png)

#### ④增加新的列族Contact和新列S_Email

```JAVA

//  增加新的列族Contact和新列S_Email
test.addColumnFamily("student","Contact");
test.putData("student","2015001","Contact","S_Email","lilei@qq.com");
test.putData("student","2015002","Contact","S_Email","hmm@qq.com");
test.putData("student","2015003","Contact","S_Email","zs@qq.com");
```

```JAVA

// 增加列族
public void addColumnFamily(String tableName, String addColumn) throws Exception {
    //转化为表名
    TableName name = TableName.valueOf(tableName);
    //判断表是否存在
    if(admin.tableExists(name)) {
        //判断表是否可用状态
        boolean tableEnabled = admin.isTableEnabled(name);
        if(tableEnabled) {
            //使表变成不可用
            admin.disableTable(name);
        }
        //根据表名得到表
        ColumnFamilyDescriptor cfd = ColumnFamilyDescriptorBuilder.newBuilder(addColumn.getBytes()).build();
        admin.addColumnFamily(name,cfd);
        admin.enableTable(name);
    }else {
        System.out.println("table不存在");
    }
}
```

该操作我是通过新建ColumnFamilyDescriptor实例，然后调用admin.addColumnFamily()方法实现列族的新建。数据插入方法则同上。看起来整体比较简单，但其中我也走了一些弯路，遇到了一些困难，这些在本报告的后续所遇到的困难部分给出。

#### ⑤删除学号为2015003的学生的选课记录

```JAVA

//  删除学号为2015003的学生选课记录
test.deleteColumn("student","2015003","Math");
test.deleteColumn("student","2015003","Computer Science");
test.deleteColumn("student","2015003","English");
```

```JAVA

public void deleteColumn(String tableName, String rowKey, String falilyName) throws Exception {
    TableName name = TableName.valueOf(tableName);
    if(admin.tableExists(name)) {
        Table table = connection.getTable(name);
        Delete delete = new Delete(rowKey.getBytes());
        delete.addFamily(falilyName.getBytes());
        table.delete(delete);
    }else {
        System.out.println("table不存在");
    }
```

该操作同样较为简单，先创建Delete实例，然后调用Table.delete()方法即可。

#### ⑥删除所创建的表

```JAVA
//  删除所创建的表
test.dropTable("student");
```

```JAVA

public void dropTable(String tableName) throws Exception {
    //转化为表名
    TableName name = TableName.valueOf(tableName);
    //判断表是否存在
    if(admin.tableExists(name)) {
        //判断表是否处于可用状态
        boolean tableEnabled = admin.isTableEnabled(name);
        if(tableEnabled) {
            //使表变成不可用状态
            admin.disableTable(name);
        }
        //删除表
        admin.deleteTable(name);
        //判断表是否存在
        if(admin.tableExists(name)) {
            System.out.println("删除失败");
        }else {
            System.out.println("删除成功");
        }
    }else {
        System.out.println("table不存在");
    }
```

该操作同样较为简单，先调用API disable要删除的表，再delete即可。细节是可以在删除后通过检验该表是否存在来判断删除是否成功。

### 五、HBase Shell实现

#### ①创建表

```shell
create 'student','BasicInfo','Math','Computer Science','English'
```

![image-20211130004555726](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130004555726.png)

#### ②插入学生信息

```shell
put 'student', '2015001', 'BasicInfo:S_No', '2015001'
put 'student', '2015001', 'BasicInfo:S_Name', 'Li Lei'
put 'student', '2015001', 'BasicInfo:S_Sex', 'male'
put 'student', '2015001', 'BasicInfo:S_Age', '23'

put 'student', '2015002', 'BasicInfo:S_No', '2015002'
put 'student', '2015002', 'BasicInfo:S_Name', 'Han Meimei'
put 'student', '2015002', 'BasicInfo:S_Sex', 'female'
put 'student', '2015002', 'BasicInfo:S_Age', '22'

put 'student', '2015003', 'BasicInfo:S_No', '2015003'
put 'student', '2015003', 'BasicInfo:S_Name', 'Zhang San'
put 'student', '2015003', 'BasicInfo:S_Sex', 'male'
put 'student', '2015003', 'BasicInfo:S_Age', '24'
```

插入结果：

![image-20211130005213911](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130005213911.png)

#### ③插入课程及选课成绩信息

```Shell
put 'student', '2015001', 'Math:C_No', '123001'
put 'student', '2015001', 'Math:C_Name', 'Math'
put 'student', '2015001', 'Math:C_Credit', '2'
put 'student', '2015001', 'Math:SC_Score', '86'
put 'student', '2015001', 'English:C_No', '123003'
put 'student', '2015001', 'English:C_Name', 'English'
put 'student', '2015001', 'English:C_Credit', '3'
put 'student', '2015001', 'English:SC_Score', '69'

put 'student', '2015002', 'CS:C_No', '123002'
put 'student', '2015002', 'CS:C_Name', 'Computer Science'
put 'student', '2015002', 'CS:C_Credit', '5'
put 'student', '2015002', 'CS:SC_Score', '77'
put 'student', '2015002', 'English:C_No', '123003'
put 'student', '2015002', 'English:C_Name', 'English'
put 'student', '2015002', 'English:C_Credit', '3'
put 'student', '2015002', 'English:SC_Score', '99'

put 'student', '2015003', 'Math:C_No', '123001'
put 'student', '2015003', 'Math:C_Name', 'Math'
put 'student', '2015003', 'Math:C_Credit', '2'
put 'student', '2015003', 'Math:SC_Score', '98'
put 'student', '2015003', 'CS:C_No', '123002'
put 'student', '2015003', 'CS:C_Name', 'Computer Science'
put 'student', '2015003', 'CS:C_Credit', '5'
put 'student', '2015003', 'CS:SC_Score', '95'
```

插入结果：

![image-20211130010350742](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130010350742.png)

![image-20211130010425317](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130010425317.png)

![image-20211130010450633](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130010450633.png)

#### ④查询选修Computer Science的学生的成绩

```Shell
scan 'student', {COLUMN=>'Computer Science:SC_Score'}
```

查询结果：

![image-20211130010926215](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130010926215.png)

#### ⑤增加新的列族和新列Contact:Email，并添加数据

```Shell
alter 'student', 'Contact'
put 'student', '2015001', 'Contact:S_Email', 'lilie@qq.com'
put 'student', '2015002', 'Contact:S_Email', 'hmm@qq.com'
put 'student', '2015003', 'Contact:S_Email', 'zs@qq.com'
```

添加并查询的结果：

![image-20211130011222118](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130011222118.png)

#### ⑥删除学号为2015003的学生的选课记录

```Shell
delete 'student', '2015003', 'Math:C_No'
delete 'student', '2015003', 'Math:C_Name'
delete 'student', '2015003', 'Math:C_Credit'
delete 'student', '2015003', 'Math:SC_Score'
delete 'student', '2015003', 'Computer Science:C_No'
delete 'student', '2015003', 'Computer Science:C_Name'
delete 'student', '2015003', 'Computer Science:C_Credit'
delete 'student', '2015003', 'Computer Science:SC_Score'
```

删除结果：

![image-20211130011811264](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130011811264.png)

#### ⑦删除所创建的表

```Shell
disable 'student'
drop 'student'
```

删除结果：

![image-20211130012000750](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130012000750.png)



### 六、遇到的主要困难

#### ①在shell和IDEA中执行指令/代码报错

一开始在shell和java代码中执行指令一切正常，但后来突然一下子，在java中执行代码无响应、在shell中执行命令报错（中间可能是由于我重启了虚拟机或者在程序的关闭问题上处理不小心）：

![image-20211126012615869](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126012615869.png)

上网查找后发现是由于集群处于安全模式，于是退出安全模式并重启hbase：

![image-20211126012652705](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126012652705.png)

但是又报出新错误：

![image-20211126020601381](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126020601381.png)

在调试过程中，尝试调用stop-hbase.sh时弹出信息如下，我直接看到了最后一行的信息：

![image-20211126021550913](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126021550913.png)

针对该信息，上网查找相关资料：![image-20211126022306118](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126022306118.png)

在hbase-env.sh中找到该行，并且取消注释、自定义了HBASE_PID_DIR路径：

![image-20211126021737068](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126021737068.png)

但结果还是不行。

在反复探索尝试过程中，我发现每次启动hadoop都会处于safemode状态，之前正常的时候则不会这样。上网查找之后发现是由于HDFS被异常关闭后造成数据库的不一致状态而形成的，解决方法之一是重新格式化hdfs，简单粗暴。

所以我按照网上的教程重新进行格式化，之后发现果然不再处于safemode状态了，hbase shell里也可以正常操作。

但是IDEA里还是报错：

![image-20211126140103121](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126140103121.png)

在StackOverflow上找到的一点解决办法也不奏效。无奈之下，我重装了hbase，但结果问题依然存在。

在试图解决的过程中发现stop-hbase时提示no hbase master found，但是jps显示一切正常：

![image-20211126160638458](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126160638458.png)

![image-20211126160654404](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126160654404.png)

针对“no hbase master found"报错信息，我上网查找了相关资料：

![image-20211126160712126](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126160712126.png)

![image-20211126161453017](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126161453017.png)

通过上述操作后问题成功解决，IDEA运行也不报错了！其实之前已经尝试过修改pid文件存放路径，但是相当于只执行了上图中的第（1）步，没有执行第（2）步，即修改后没有通过kill等手段关闭hbase相关进程，导致修改未生效。这个IDEA报错的问题在网上找了很久，大部分说法是数据损坏等，但是没想到最后以这种方法解决。这也提示我们要善于观察发现，如果没有发现stop-hbase时提示的no hbase master found，一味地按照网上的解决方案尝试，可能即使再花上很多时间也无法解决。

#### ②在已有表新建列族时报错"read only"

一开始我新建列族采取的如下方法，通过admin.getTableDescriptor()方法得到HTableDescriptor实例，然后调用该实例的addFamily()方法增加列族，最后调用admin.modifyTable()方法使修改生效：

![img](file:///D:\QQ\1446295629\Image\C2C\Image2\R1UV_O88`OHQPQ1PDZ32`SL.png)

但是报错说HTD是read-only的：

![image-20211129193533085](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211129193533085.png)

在上图中可以看到我在代码中插入了若干行输出代码，输出该HTD的isReadOnly()属性，结果显示一直为false，即并非只读，那么为什么会报错说是只读的呢？看源代码也没有发现任何异常：

![image-20211130004114743](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130004114743.png)

也曾经考虑过是不是由于该方法为Deprecated，所以会有一些问题。但是尝试了使用不Deprecated的方法、获取到了TableDiscriptor实例后发现该实例没有新增列族的方法：

![img](file:///D:\QQ\1446295629\Image\C2C\Image2\HES[OQ@DPO4V@MB798MHAR0.png)

上StackOverflow查找直接显示0 results，百度也找不到任何相关信息：

![img](file:///D:\QQ\1446295629\Image\C2C\Image2\FF5@SX{[6%0UFC4GE[X_FEH.png)

万般无奈之下向助教请教，然后助教给我看了这个：

![image-20211129235315230](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211129235315230.png)

在HBase的官方API文档中写明了返回的是一个**read-only**的HTD！但是在源代码中并没有显示写出（之前我一直觉得官方文档密密麻麻的看着头疼，所以看的比较少，这次也是给了我一个教训）。

在官方文档中查找后发现，HBase2.4.8中，admin的方法中，所有返回HTD实例的方法，返回的都是read-only的HTD。因此网上的很多（基本可是说是所有）插入列族的方法在我目前的环境下是无效的。

为了解决这个问题，我在官方文档中进一步查找，发现了如下方法：

![image-20211130123850928](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130123850928.png)

该方法传入ColumnFamilyDescriptor实例，看起来挺靠谱。

所以我在代码中尝试了该方法：

![image-20211130123949382](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130123949382.png)

结果发现能够成功执行！

不过，该方法**addColumnFamily**在官方API文档中并没有给出，官方文档中只有**addColumnFamilyAsync**方法，这一点我也不知道为什么。

这个问题让我学到的教训就是，随着版本的变化，很多接口可能都发生了巨大的变化，网上一些过去的教程可能不再有用，而新的教程可能也还未更新，这时候就需要我们自己去读源码、去读官方文档，自己发现问题所在并加以修正。

