package rw.gov.erp.payroll.service;

import rw.gov.erp.payroll.dto.deduction.DeductionRequestDTO;
import rw.gov.erp.payroll.dto.deduction.DeductionResponseDTO;

import java.util.List;

public interface DeductionService {
    
    DeductionResponseDTO createDeduction(DeductionRequestDTO dto);
    
    DeductionResponseDTO getDeductionById(Long id);
    
    DeductionResponseDTO getDeductionByCode(String code);
    
    List<DeductionResponseDTO> getAllDeductions();
    
    DeductionResponseDTO updateDeduction(Long id, DeductionRequestDTO dto);
    
    void deleteDeduction(Long id);
}
