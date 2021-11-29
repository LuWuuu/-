基本环境配置
虚拟机快照
CSDN——>Stackoverflow
扩展磁盘空间后需要分配——胡悦
列出参考教程



windows单机HBase配置

hadoop3.3.0+hbase2.2.4

参考教程：

https://www.jianshu.com/p/ccd3e4529d8a

https://blog.csdn.net/Tabgiver/article/details/80153476

https://blog.csdn.net/catfishlyf/article/details/113715475

start-hbase.cmd报错：![image-20211120220554005](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211120220554005.png)











linux单机启动：

![image-20211123105351620](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211123105351620.png)





HBaseAdmin类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218091841514.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

HBaseConfiguration类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218092311197.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2NhcnNvbjA0MDg=,size_16,color_FFFFFF,t_70)

HTable类的主要方法：

![img](https://img-blog.csdnimg.cn/20181218092912102.png)

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



java编程运行无响应，到shell中执行指令报错：

![image-20211126012615869](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126012615869.png)

上网查找后发现是由于集群处于安全模式，退出安全模式并重启hbase：

![image-20211126012652705](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126012652705.png)

但是又报出新错误：

![image-20211126020601381](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126020601381.png)

jps查看发现zookeeper没有启动：

![image-20211126021508513](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126021508513.png)

尝试调用stop-hbase.sh时弹出信息如下：

![image-20211126021550913](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126021550913.png)

hbase-env.sh

![image-20211126022306118](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126022306118.png)

![image-20211126021737068](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126021737068.png)

结果还是不行。

在反复探索尝试过程中，发现每次启动hadoop都会处于safemode状态，上网查找之后发现是由于HDFS被异常关闭后造成数据库的不一致状态而形成的，解决方法之一是重新格式化hdfs，简单粗暴。

所以我按照网上的教程重新进行格式化，之后发现果然不再处于safemode状态了，hbase shell里也可以正常操作。

IDEA报错：

![image-20211126140103121](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126140103121.png)

重装hbase之后问题依然存在

在试图解决的过程中发现stop-hbase时提示no hbase master found，但是jps显示一切正常：

![image-20211126160638458](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126160638458.png)

![image-20211126160654404](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126160654404.png)

![image-20211126160712126](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126160712126.png)

![image-20211126161453017](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211126161453017.png)

通过上述操作后问题成功解决，IDEA运行也不报错了！其实之前已经尝试过修改pid文件存放路径，但是修改后没有通过kill等手段关闭hbase相关进程，导致修改未生效。这个IDEA报错的问题在网上找了很久，大部分说法是数据损坏等，但是没想到最后以这种方法解决。这也提示我们要善于观察发现，如果没有发现stop-hbase时提示的no hbase master found，一味地按照网上的解决方案尝试，可能即使再花上很多时间也无法解决。



查询选修Computer Science的学生的成绩：

![image-20211129155904648](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211129155904648.png)

增加新的列族Contact和新列S_Email：



![img](file:///D:\QQ\1446295629\Image\C2C\Image2\R1UV_O88`OHQPQ1PDZ32`SL.png)

![image-20211129193533085](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211129193533085.png)

![image-20211130004114743](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211130004114743.png)

![image-20211129235315230](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211129235315230.png)

删除学号为2015003的学生选课记录

一切正常

删除表

一切正常

shell：

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

