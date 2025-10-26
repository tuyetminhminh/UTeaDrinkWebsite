package net.codejava.utea.manager.service;

import net.codejava.utea.manager.dto.OrderManagementDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportOrdersToExcel(List<OrderManagementDTO> orders) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Đơn hàng");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "STT", "Mã đơn", "Khách hàng", "Điện thoại", "Địa chỉ",
                "Subtotal", "Phí ship", "Tổng tiền", "Trạng thái", 
                "Shipper", "Ngày tạo", "Ghi chú"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data
            int rowNum = 1;
            for (OrderManagementDTO order : orders) {
                Row row = sheet.createRow(rowNum++);

                // STT
                row.createCell(0).setCellValue(rowNum - 1);

                // Mã đơn
                row.createCell(1).setCellValue(order.getOrderCode());

                // Khách hàng
                row.createCell(2).setCellValue(order.getCustomerName() != null ? order.getCustomerName() : "");

                // Điện thoại
                row.createCell(3).setCellValue(order.getCustomerPhone() != null ? order.getCustomerPhone() : "");

                // Địa chỉ
                row.createCell(4).setCellValue(order.getShippingAddress() != null ? order.getShippingAddress() : "");

                // Subtotal
                Cell subtotalCell = row.createCell(5);
                if (order.getSubtotal() != null) {
                    subtotalCell.setCellValue(order.getSubtotal().doubleValue());
                    subtotalCell.setCellStyle(currencyStyle);
                }

                // Phí ship
                Cell shippingFeeCell = row.createCell(6);
                if (order.getShippingFee() != null) {
                    shippingFeeCell.setCellValue(order.getShippingFee().doubleValue());
                    shippingFeeCell.setCellStyle(currencyStyle);
                }

                // Tổng tiền
                Cell totalCell = row.createCell(7);
                if (order.getTotal() != null) {
                    totalCell.setCellValue(order.getTotal().doubleValue());
                    totalCell.setCellStyle(currencyStyle);
                }

                // Trạng thái
                row.createCell(8).setCellValue(getStatusText(order.getStatus()));

                // Shipper
                row.createCell(9).setCellValue(order.getShipperName() != null ? order.getShipperName() : "Chưa phân công");

                // Ngày tạo
                Cell dateCell = row.createCell(10);
                if (order.getCreatedAt() != null) {
                    dateCell.setCellValue(order.getCreatedAt().format(DATE_FORMATTER));
                }

                // Ghi chú
                row.createCell(11).setCellValue("");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add some padding
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0 ₫"));
        return style;
    }

    private String getStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "NEW": return "Mới";
            case "CONFIRMED": return "Đã xác nhận";
            case "PREPARING": return "Đang chuẩn bị";
            case "DELIVERING": return "Đang giao";
            case "DELIVERED": return "Đã giao";
            case "CANCELED": return "Đã hủy";
            case "RETURNED": return "Trả hàng";
            case "REFUNDED": return "Hoàn tiền";
            default: return status;
        }
    }
}

