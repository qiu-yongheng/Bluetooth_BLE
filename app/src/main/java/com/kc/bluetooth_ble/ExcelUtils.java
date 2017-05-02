package com.kc.bluetooth_ble;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    public static WritableFont arial14font = null;

    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);
            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {

            e.printStackTrace();
        }
    }

    /**
     * 创建Excel表
     *
     * @param fileName
     * @param colName
     */
    public static void initExcel(String fileName, String[] colName) {
        //初始化字体大小
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            //创建文件
            if (!file.exists()) {
                file.createNewFile();
            }
            //创建excel
            workbook = Workbook.createWorkbook(file);
            //创建表单,其中sheet表示该表格的名字,0表示第一个表格
            WritableSheet sheet = workbook.createSheet("心率耳机返回数据", 0);
            /**
             * 添加到指定表格里
             * new label: 在指定单元格插入数据
             * 第一个参数表示,0表示第一列,第二个参数表示行,同样0表示第一行,第三个参数表示想要添加到单元格里的数据
             */
            sheet.addCell((WritableCell) new Label(0, 0, fileName, arial14format)); //第一个单元格写入文件路径
            //写入列名
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            //从内存写到文件中
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(List<T> objList, String fileName, Context c) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                /**设置编码*/
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                /**创建输入流*/
                in = new FileInputStream(new File(fileName));
                /**根据输入流获取原来的Excel*/
                Workbook workbook = Workbook.getWorkbook(in);
                /**每次插入数据,都要取原来的表,然后新建一个表,然后将原来的表的内容添加到新表上.但只要两个路径相同的话,效果相当于在原来的表添加*/
                writebook = Workbook.createWorkbook(new File(fileName), workbook);
                /**获取指定索引的表格*/
                WritableSheet sheet = writebook.getSheet(0);

                for (int j = 0; j < objList.size(); j++) {
                    byte[] list = (byte[]) objList.get(j);

                    for (int i = 0; i < list.length; i++) {
                        sheet.addCell(new Number(i, j + 1, list[i], arial12format));
                    }
                }
                writebook.write();
                Toast.makeText(c, "保存成功", Toast.LENGTH_SHORT).show();
                Log.d("=====", "保存成功");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public static Object getValueByRef(Class cls, String fieldName) {
        Object value = null;
        fieldName = fieldName.replaceFirst(fieldName.substring(0, 1), fieldName.substring(0, 1).toUpperCase());
        String getMethodName = "get" + fieldName;
        try {
            Method method = cls.getMethod(getMethodName);
            value = method.invoke(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
