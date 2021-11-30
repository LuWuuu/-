import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.*;

public class run{
    public static void main(String[] args) {
        Hbase test = new Hbase();
        try{
            test.getconnect();
            test.createtable("student","BasicInfo", "Math", "Computer Science", "English");

            //  插入学生基础信息
            test.putData("student", "2015001", "BasicInfo", "S_No", "2015001");
            test.putData("student", "2015001", "BasicInfo", "S_Name", "Li Lei");
            test.putData("student", "2015001", "BasicInfo", "S_Sex", "male");
            test.putData("student", "2015001", "BasicInfo", "S_Age", "23");
            test.putData("student", "2015002", "BasicInfo", "S_No", "2015002");
            test.putData("student", "2015002", "BasicInfo", "S_Name", "Han Meimei");
            test.putData("student", "2015002", "BasicInfo", "S_Sex", "female");
            test.putData("student", "2015002", "BasicInfo", "S_Age", "22");
            test.putData("student", "2015003", "BasicInfo", "S_No", "2015003");
            test.putData("student", "2015003", "BasicInfo", "S_Name", "Zhang San");
            test.putData("student", "2015003", "BasicInfo", "S_Sex", "male");
            test.putData("student", "2015003", "BasicInfo", "S_Age", "24");

            //  插入课程信息
            test.putData("student","2015001","Math","C_No","123001");
            test.putData("student","2015001","Math","C_Name","Math");
            test.putData("student","2015001","Math","C_Credit","2.0");
            test.putData("student","2015001","English","C_No","123003");
            test.putData("student","2015001","English","C_Name","English");
            test.putData("student","2015001","English","C_Credit","3.0");

            test.putData("student","2015002","Computer Science","C_No","123002");
            test.putData("student","2015002","Computer Science","C_Name","Computer Science");
            test.putData("student","2015002","Computer Science","C_Credit","5.0");
            test.putData("student","2015002","English","C_No","123003");
            test.putData("student","2015002","English","C_Name","English");
            test.putData("student","2015002","English","C_Credit","3.0");

            test.putData("student","2015003","Math","C_No","123001");
            test.putData("student","2015003","Math","C_Name","Math");
            test.putData("student","2015003","Math","C_Credit","2.0");
            test.putData("student","2015003","Computer Science","C_No","123002");
            test.putData("student","2015003","Computer Science","C_Name","Computer Science");
            test.putData("student","2015003","Computer Science","C_Credit","5.0");

            //  插入学生课程成绩
            test.putData("student","2015001","Math","SC_Score","86");
            test.putData("student","2015001","English","SC_Score","69");
            test.putData("student","2015002","Computer Science","SC_Score","77");
            test.putData("student","2015002","English","SC_Score","99");
            test.putData("student","2015003","Math","SC_Score","98");
            test.putData("student","2015003","Computer Science","SC_Score","95");

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

            //  增加新的列族Contact和新列S_Email
            test.addColumnFamily("student","Contact");
            test.putData("student","2015001","Contact","S_Email","lilei@qq.com");
            test.putData("student","2015002","Contact","S_Email","hmm@qq.com");
            test.putData("student","2015003","Contact","S_Email","zs@qq.com");

            //  删除学号为2015003的学生选课记录
            test.deleteColumn("student","2015003","Math");
            test.deleteColumn("student","2015003","Computer Science");
            test.deleteColumn("student","2015003","English");

            //  删除所创建的表
            test.dropTable("student");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}