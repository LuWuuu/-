import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Hbase {
    public static Configuration conf;
    public static Connection connection;
    public static Admin admin;
    public void getconnect() throws IOException {
        conf=HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "127.0.0.1");
        conf.set("hbase.zookeeper.property.clientPort","2181");
        try{
            connection=ConnectionFactory.createConnection(conf);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    //  判断表是否存在
    public boolean existTable(String tableName) throws Exception {
        TableName name = TableName.valueOf(tableName);
        return admin.tableExists(name);
    }

    //  disable表
    public void disableTable(String tableName) throws Exception {
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            if(admin.isTableEnabled(name)) {
                admin.disableTable(name);
            }else {
                System.out.println("table不是活动状态");
            }
        }else {
            System.out.println("table不存在");
        }
    }

    //  drop表
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
    }

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


    /**添加数据
     *tableName:    表明
     *rowKey:    行键
     *familyName:列簇
     *columnName:列名
     *value:        值
     */
    public void putData(String tableName, String rowKey, String familyName, String columnName, String value)             throws Exception {
        //转化为表名
        TableName name = TableName.valueOf(tableName);
        //添加数据之前先判断表是否存在，不存在的话先创建表
        if(admin.tableExists(name)){
        }else {
        //根据表明创建表结构
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

    public void putData(String tableName, String rowKey, String familyName, String columnName, String value, long timestamp) throws Exception {
        // 转化为表名
        TableName name = TableName.valueOf(tableName);
        // 添加数据之前先判断表是否存在，不存在的话先创建表
        if (admin.tableExists(name)) {
        } else {
            // 根据表明创建表结构
            HTableDescriptor tableDescriptor = new HTableDescriptor(name);
            // 定义列簇的名字
            HColumnDescriptor columnFamilyName = new HColumnDescriptor(familyName);
            tableDescriptor.addFamily(columnFamilyName);
            admin.createTable(tableDescriptor);
        }
        Table table = connection.getTable(name);
        Put put = new Put(rowKey.getBytes());
        //put.addColumn(familyName.getBytes(), columnName.getBytes(), value.getBytes());
        put.addImmutable(familyName.getBytes(), columnName.getBytes(), timestamp, value.getBytes());
        table.put(put);
    }

    // 根据rowkey查询数据
    public Result getResult(String tableName, String rowKey, String familyName) throws Exception {
        Result result;
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            Table table = connection.getTable(name);
            Get get = new Get(rowKey.getBytes());
            get.addFamily(familyName.getBytes());
            result = table.get(get);
        }else {
            result = null;
        }
        return result;
    }

    public Result getResult(String tableName, String rowKey, String familyName, String columnName) throws Exception {
        Result result;
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            Table table = connection.getTable(name);
            Get get = new Get(rowKey.getBytes());
            get.addColumn(familyName.getBytes(), columnName.getBytes());
            result = table.get(get);
        }else {
            result = null;
        }
        return result;
    }

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
//                s1.addColumn(Bytes.toBytes(s[0]), Bytes.toBytes(s[1]));
            }
            scan.setFilter(filterList);
            result = table.getScanner(scan);
        }
        else {
            result = null;
        }
        return result;
    }

    // 查询指定version
    public Result getResultByVersion(String tableName, String rowKey, String familyName, String columnName, int versions) throws Exception {
        Result result;
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            Table table = connection.getTable(name);
            Get get = new Get(rowKey.getBytes());
            get.addColumn(familyName.getBytes(), columnName.getBytes());
            get.setMaxVersions(versions);
            result = table.get(get);
        }else {
            result = null;
        }
        return result;
    }

    // scan全表数据
    public ResultScanner getResultScann(String tableName) throws Exception {
        ResultScanner result;
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            Table table = connection.getTable(name);
            Scan scan = new Scan();
            result = table.getScanner(scan);
        }else {
            result = null;
        }
        return result;
    }

    // 删除数据
    public void deleteColumn(String tableName, String rowKey) throws Exception {
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            Table table = connection.getTable(name);
            Delete delete = new Delete(rowKey.getBytes());
            table.delete(delete);
        }else {
            System.out.println("table不存在");
        }
    }

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
    }

    public void deleteColumn(String tableName, String rowKey, String falilyName, String columnName) throws Exception {
        TableName name = TableName.valueOf(tableName);
        if(admin.tableExists(name)) {
            Table table = connection.getTable(name);
            Delete delete = new Delete(rowKey.getBytes());
            delete.addColumn(falilyName.getBytes(), columnName.getBytes());
            table.delete(delete);
        }else {
            System.out.println("table不存在");
        }
    }
}


