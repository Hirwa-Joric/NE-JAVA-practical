package rw.gov.erp.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.erp.payroll.dto.deduction.DeductionRequestDTO;
import rw.gov.erp.payroll.dto.deduction.DeductionResponseDTO;
import rw.gov.erp.payroll.exception.ResourceNotFoundException;
import rw.gov.erp.payroll.model.Deduction;
import rw.gov.erp.payroll.repository.DeductionRepository;
import rw.gov.erp.payroll.service.DeductionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeductionServiceImpl implements DeductionService {
    
    private final DeductionRepository deductionRepository;
    
    @Override
    @Transactional
    public DeductionResponseDTO createDeduction(DeductionRequestDTO dto) {
        // Check if code already exists
        if (deductionRepository.findByCode(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Deduction code already exists: " + dto.getCode());
        }
        
        // Check if name already exists
        if (deductionRepository.findByDeductionName(dto.getDeductionName()).isPresent()) {
            throw new IllegalArgumentException("Deduction name already exists: " + dto.getDeductionName());
        }
        
        // Create and save deduction
        Deduction deduction = Deduction.builder()
                .code(dto.getCode())
                .deductionName(dto.getDeductionName())
                .percentage(dto.getPercentage())
                .build();
        
        Deduction savedDeduction = deductionRepository.save(deduction);
        
        return mapToResponseDTO(savedDeduction);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DeductionResponseDTO getDeductionById(Long id) {
        Deduction deduction = deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction", "id", id));
        return mapToResponseDTO(deduction);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DeductionResponseDTO getDeductionByCode(String code) {
        Deduction deduction = deductionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction", "code", code));
        return mapToResponseDTO(deduction);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DeductionResponseDTO> getAllDeductions() {
        return deductionRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public DeductionResponseDTO updateDeduction(Long id, DeductionRequestDTO dto) {
        Deduction deduction = deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction", "id", id));
        
        // Check if name is being changed and if it's already in use by another deduction
        if (!deduction.getDeductionName().equals(dto.getDeductionName()) &&
                deductionRepository.findByDeductionName(dto.getDeductionName()).isPresent()) {
            throw new IllegalArgumentException("Deduction name already exists: " + dto.getDeductionName());
        }
        
        // The code field is considered immutable to maintain referential integrity
        // So we keep the original code but update the other fields
        
        deduction.setDeductionName(dto.getDeductionName());
        deduction.setPercentage(dto.getPercentage());
        
        Deduction updatedDeduction = deductionRepository.save(deduction);
        return mapToResponseDTO(updatedDeduction);
    }
    
    @Override
    @Transactional
    public void deleteDeduction(Long id) {
        if (!deductionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Deduction", "id", id);
        }
        
        // For now, implement a hard delete, though in production you might want to check
        // if the deduction is referenced in any payslips before allowing deletion
        deductionRepository.deleteById(id);
    }
    
    private DeductionResponseDTO mapToResponseDTO(Deduction deduction) {
        return DeductionResponseDTO.builder()
                .id(deduction.getId())
                .code(deduction.getCode())
                .deductionName(deduction.getDeductionName())
                .percentage(deduction.getPercentage())
                .createdAt(deduction.getCreatedAt())
                .updatedAt(deduction.getUpdatedAt())
                .build();
    }
}
