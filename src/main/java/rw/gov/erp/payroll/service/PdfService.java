package rw.gov.erp.payroll.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import rw.gov.erp.payroll.dto.payslip.PayslipResponseDTO;
import rw.gov.erp.payroll.model.enums.PayslipStatus;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class PdfService {

    public byte[] generatePayslipPdf(PayslipResponseDTO payslip) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();
            addPayslipContent(document, payslip);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addPayslipContent(Document document, PayslipResponseDTO payslip) throws DocumentException {
        // Add title
        String monthName = Month.of(payslip.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("PAYSLIP - " + monthName.toUpperCase() + " " + payslip.getYear(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Add employee information section
        PdfPTable employeeInfoTable = new PdfPTable(2);
        employeeInfoTable.setWidthPercentage(100);
        employeeInfoTable.setSpacingAfter(20);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        // Add employee details
        addTableCell(employeeInfoTable, "Employee Name:", headerFont);
        addTableCell(employeeInfoTable, payslip.getEmployeeName(), contentFont);
        
        addTableCell(employeeInfoTable, "Employee Code:", headerFont);
        addTableCell(employeeInfoTable, payslip.getEmployeeCode(), contentFont);
        
        addTableCell(employeeInfoTable, "Month/Year:", headerFont);
        addTableCell(employeeInfoTable, monthName + "/" + payslip.getYear(), contentFont);
        
        addTableCell(employeeInfoTable, "Status:", headerFont);
        String status = payslip.getStatus().equals(PayslipStatus.PAID) ? "PAID" : "PENDING";
        addTableCell(employeeInfoTable, status, contentFont);
        
        document.add(employeeInfoTable);
        
        // Add salary details section
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph salaryTitle = new Paragraph("SALARY DETAILS", sectionFont);
        salaryTitle.setSpacingAfter(10);
        document.add(salaryTitle);
        
        PdfPTable salaryTable = new PdfPTable(2);
        salaryTable.setWidthPercentage(100);
        salaryTable.setSpacingAfter(20);
        
        // Earnings
        addTableCell(salaryTable, "EARNINGS", headerFont);
        addTableCell(salaryTable, "AMOUNT (RWF)", headerFont);
        
        // Calculate basic salary (gross - allowances)
        BigDecimal basicSalary = payslip.getGrossSalary()
                .subtract(payslip.getHouseAmount())
                .subtract(payslip.getTransportAmount());
        
        addTableCell(salaryTable, "Basic Salary", contentFont);
        addTableCell(salaryTable, formatCurrency(basicSalary), contentFont);
        
        addTableCell(salaryTable, "Housing Allowance", contentFont);
        addTableCell(salaryTable, formatCurrency(payslip.getHouseAmount()), contentFont);
        
        addTableCell(salaryTable, "Transport Allowance", contentFont);
        addTableCell(salaryTable, formatCurrency(payslip.getTransportAmount()), contentFont);
        
        addTableCell(salaryTable, "Gross Salary", headerFont);
        addTableCell(salaryTable, formatCurrency(payslip.getGrossSalary()), headerFont);
        
        // Deductions
        salaryTable.completeRow();
        addTableCell(salaryTable, "DEDUCTIONS", headerFont);
        addTableCell(salaryTable, "AMOUNT (RWF)", headerFont);
        
        addTableCell(salaryTable, "Income Tax", contentFont);
        addTableCell(salaryTable, formatCurrency(payslip.getEmployeeTaxedAmount()), contentFont);
        
        addTableCell(salaryTable, "Pension Contribution", contentFont);
        addTableCell(salaryTable, formatCurrency(payslip.getPensionAmount()), contentFont);
        
        addTableCell(salaryTable, "Medical Insurance", contentFont);
        addTableCell(salaryTable, formatCurrency(payslip.getMedicalInsuranceAmount()), contentFont);
        
        addTableCell(salaryTable, "Other Deductions", contentFont);
        addTableCell(salaryTable, formatCurrency(payslip.getOtherTaxedAmount()), contentFont);
        
        addTableCell(salaryTable, "Total Deductions", headerFont);
        BigDecimal totalDeductions = payslip.getEmployeeTaxedAmount()
                .add(payslip.getPensionAmount())
                .add(payslip.getMedicalInsuranceAmount())
                .add(payslip.getOtherTaxedAmount());
        addTableCell(salaryTable, formatCurrency(totalDeductions), headerFont);
        
        document.add(salaryTable);
        
        // Net Salary
        PdfPTable netSalaryTable = new PdfPTable(2);
        netSalaryTable.setWidthPercentage(100);
        
        Font netSalaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        addTableCell(netSalaryTable, "NET SALARY", netSalaryFont);
        addTableCell(netSalaryTable, formatCurrency(payslip.getNetSalary()), netSalaryFont);
        
        document.add(netSalaryTable);
        
        // Add footer with payment information
        if (payslip.getStatus().equals(PayslipStatus.PAID)) {
            Paragraph footer = new Paragraph("This salary has been credited to your account.", contentFont);
            footer.setSpacingBefore(40);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            Paragraph processedDate = new Paragraph("Processed on: " + payslip.getProcessedDate(), contentFont);
            processedDate.setAlignment(Element.ALIGN_CENTER);
            document.add(processedDate);
            
            Paragraph paymentDate = new Paragraph("Payment date: " + payslip.getPaymentDate(), contentFont);
            paymentDate.setAlignment(Element.ALIGN_CENTER);
            document.add(paymentDate);
        } else {
            Paragraph footer = new Paragraph("This payslip is pending approval.", contentFont);
            footer.setSpacingBefore(40);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
        }
    }
    
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }
    
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.2f", amount.doubleValue());
    }
}
