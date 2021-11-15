## 《金融大数据处理技术》作业7报告

191870068  嵇泽同

### 一、题目要求

​	Iris数据集是常用的分类实验数据集，由Fisher, 1936收集整理。Iris也称鸢尾花卉数据集，是一类多重变量分析的数据集。数据集包含150个数据，分为3类，每类50个数据，每个数据包含4个属性。可通过花萼长度，花萼宽度，花瓣长度，花瓣宽度4个属性预测鸢尾花卉属于（Setosa，Versicolour，Virginica）三个种类中的哪一类。在MapReduce上任选一种分类算法（KNN，朴素贝叶斯或决策树）对该数据集进行分类预测，采用留出法对建模结果评估，70%数据作为训练集，30%数据作为测试集，评估标准采用精度accuracy。可以尝试对结果进行可视化的展示（可选）。

### 二、结果截图

![image-20211115222934104](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211115222934104.png)

![image-20211115223047793](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211115223047793.png)

![image-20211115223210270](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211115223210270.png)

![image-20211115223345664](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211115223345664.png)

![image-20211115223322292](C:\Users\Jzt\AppData\Roaming\Typora\typora-user-images\image-20211115223322292.png

### 三、设计思路

#### 1.基本思路

​	KNN算法中，可以将训练集放到内存中，也可以将测试集放到内存中。本次作业中我参照讲义上的实现，将训练集放到内存中，自定义数据类型IdDistance，在map阶段将每一行数据（以id标识）分别与训练集（内存中）中的每一行数据对比，计算对应的距离，并且将得到的信息保存到自定义的数据类型中并作为key输出。在combine阶段则在每个id对应的所有map输出中只保留距离前k小的k个，以减少发送到reducer的信息量。在reduce阶段则根据每个id对应的k条信息，根据多数原则“票选”出分类结果，并且记录该分类结果与实际结果是否一致，用于最后的accuracy计算。

#### 2.距离计算

​	本次作业中我考虑了三种较简单的距离计算方式：欧氏距离、曼哈顿距离以及切比雪夫距离，并且允许用户通过传入参数来决定使用哪种距离计算方式：

```java
public class Distance {
    static public double CalcuDistance(String method, double[] a, double[] b) {
        int length = a.length;
        double result = 0;
        if (method.equals("manhattan")) {  //  曼哈顿距离
            for(int i = 0; i <length;i++) {
                result = result + Math.abs(a[i]-b[i]) ;
            }
            return result;
        }
        else if (method.equals("chebyshev")) {  //  切比雪夫距离
            for(int i = 0; i <length;i++) {
                if (Math.abs(a[i]-b[i]) > result)
                    result = Math.abs(a[i]-b[i]);
            }
            return result;
        }
        else {
            for(int i = 0; i <length;i++) {  //  欧氏距离，默认情况下所采用的方式
                result = result + (a[i]-b[i]) * (a[i] - b[i]);
            }
            return Math.sqrt(result);
        }
    }
}
```

#### 3.自定义数据类型

​	在map阶段进行距离计算后，需要把得到的信息传输到reducer，用自带的数据类型作为key/value较难实现，因此我自定义了一个数据类型IdDistance，用以保存测试样本的id、与其比较的训练样本的id、两者间的距离。为了后续shuffle过程中排序顺利，自定义该数据类型的compareTo方法使得优先比较测试样本id，在测试样本id相同的情况下再比较距离。这样的处理使得combine/reduce阶段接收到的前k个数据就是前k小的k个数据。

```java
    public static class IdDistance implements WritableComparable<IdDistance> {
        private String id;  //  测试样本的id
        private double distance;  //  测试样本和训练样本间的距离
        private String target;  //  训练样本的id

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
```

