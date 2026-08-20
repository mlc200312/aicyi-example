package io.github.aicyi.example.test.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.github.aicyi.commons.util.ExcelUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ExcelUtils} 测试类
 */
@DisplayName("ExcelUtils Excel工具类测试")
public class ExcelUtilsTest {

    @Getter
    @Setter
    public static class Row {
        @ExcelProperty("名称")
        private String name;

        @ExcelProperty("分数")
        private Integer score;

        public Row() {
        }

        public Row(String name, Integer score) {
            this.name = name;
            this.score = score;
        }
    }

    private static List<Row> buildRows(int count) {
        List<Row> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            rows.add(new Row("row-" + i, i * 10));
        }
        return rows;
    }

    @Test
    @DisplayName("exportToBytes/readFromBytes 往返一致")
    public void testExportAndReadBytes() {
        List<Row> rows = buildRows(3);

        byte[] bytes = ExcelUtils.exportToBytes("sheet1", rows, Row.class);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        List<Row> parsed = ExcelUtils.readFromBytes(bytes, Row.class);
        assertEquals(3, parsed.size());
        assertEquals("row-1", parsed.get(0).getName());
        assertEquals(30, parsed.get(2).getScore());
    }

    @Test
    @DisplayName("exportToFile/readExcel 文件读写")
    public void testExportAndReadFile(@TempDir Path tempDir) {
        String filePath = tempDir.resolve("test.xlsx").toString();
        List<Row> rows = buildRows(2);

        ExcelUtils.exportToFile(filePath, "sheet1", rows, Row.class);
        List<Row> parsed = ExcelUtils.readExcel(filePath, Row.class);

        assertEquals(2, parsed.size());
        assertEquals("row-2", parsed.get(1).getName());
    }

    @Test
    @DisplayName("readExcelWithListener 按批回调")
    public void testReadWithListener(@TempDir Path tempDir) {
        String filePath = tempDir.resolve("batch.xlsx").toString();
        ExcelUtils.exportToFile(filePath, "sheet1", buildRows(5), Row.class);

        List<Integer> batchSizes = new ArrayList<>();
        List<Row> all = new ArrayList<>();

        ExcelUtils.readExcelWithListener(filePath, Row.class, new ExcelUtils.ExcelListener<Row>(2) {
            @Override
            protected void processBatch(List<Row> batchData) {
                batchSizes.add(batchData.size());
                all.addAll(batchData);
            }
        });

        assertEquals(5, all.size());
        // 5条数据按批大小2切分：2 + 2 + 1
        assertEquals(Arrays.asList(2, 2, 1), batchSizes);
    }

    @Test
    @DisplayName("readExcelInBatches 分批迭代")
    public void testReadInBatches(@TempDir Path tempDir) {
        String filePath = tempDir.resolve("batches.xlsx").toString();
        ExcelUtils.exportToFile(filePath, "sheet1", buildRows(5), Row.class);

        int total = 0;
        int batchCount = 0;
        for (List<Row> batch : ExcelUtils.readExcelInBatches(filePath, Row.class, 2)) {
            assertTrue(batch.size() <= 2);
            total += batch.size();
            batchCount++;
        }

        assertEquals(5, total);
        assertEquals(3, batchCount);
    }

    @Test
    @DisplayName("readExcelInBatches 参数校验")
    public void testReadInBatchesInvalid(@TempDir Path tempDir) {
        String filePath = tempDir.resolve("x.xlsx").toString();

        assertThrows(IllegalArgumentException.class,
                () -> ExcelUtils.readExcelInBatches(" ", Row.class, 10).iterator());
        assertThrows(IllegalArgumentException.class,
                () -> ExcelUtils.readExcelInBatches(filePath, null, 10).iterator());
        assertThrows(IllegalArgumentException.class,
                () -> ExcelUtils.readExcelInBatches(filePath, Row.class, 0).iterator());
    }

    @Test
    @DisplayName("readMultiSheetExcel 多sheet读取")
    public void testReadMultiSheet(@TempDir Path tempDir) {
        String filePath = tempDir.resolve("multi.xlsx").toString();

        try (ExcelWriter writer = EasyExcel.write(filePath, Row.class).build()) {
            WriteSheet sheet0 = EasyExcel.writerSheet(0, "first").build();
            writer.write(buildRows(2), sheet0);

            WriteSheet sheet1 = EasyExcel.writerSheet(1, "second").build();
            writer.write(buildRows(3), sheet1);
        }

        Map<Integer, Class<?>> sheetClasses = new HashMap<>();
        sheetClasses.put(0, Row.class);
        sheetClasses.put(1, Row.class);

        Map<Integer, List<?>> result = ExcelUtils.readMultiSheetExcel(filePath, sheetClasses);

        assertEquals(2, result.get(0).size());
        assertEquals(3, result.get(1).size());
    }

    @Test
    @DisplayName("空数据导出读取")
    public void testEmptyData() {
        byte[] bytes = ExcelUtils.exportToBytes("sheet1", new ArrayList<Row>(), Row.class);

        List<Row> parsed = ExcelUtils.readFromBytes(bytes, Row.class);
        assertNotNull(parsed);
        assertTrue(parsed.isEmpty());
    }
}
