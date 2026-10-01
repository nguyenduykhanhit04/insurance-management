package com.training.insurance.controller;

import com.training.insurance.dto.request.InsuranceFormRequest;
import com.training.insurance.dto.request.InsuranceSearchCriteria;
import com.training.insurance.dto.response.InsuranceItemResponse;
import com.training.insurance.entity.Company;
import com.training.insurance.exception.AppException;
import com.training.insurance.service.CompanyService;
import com.training.insurance.service.InsuranceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/insurance")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;
    private final CompanyService companyService;

    @GetMapping
    public String list(
            @ModelAttribute("criteria") InsuranceSearchCriteria criteria,
            Model model
    ) {
        List<Company> companies = companyService.getAllCompanies();

        if (criteria.getCompanyId() == null && !companies.isEmpty()) {
            criteria.setCompanyId(companies.get(0).getCompanyInternalId());
        }

        if (criteria.getOrder() == null || criteria.getOrder().trim().isEmpty()) {
            criteria.setOrder("ASC");
        }

        Page<InsuranceItemResponse> pageData = insuranceService.searchInsurances(criteria);

        model.addAttribute("companies", companies);
        model.addAttribute("pageData", pageData);
        model.addAttribute("criteria", criteria);

        return "insurance-list";
    }

    @GetMapping("/detail/{id}")
    public String detail(
            @PathVariable Integer id,
            @ModelAttribute("criteria") InsuranceSearchCriteria criteria,
            Model model
    ) {
        InsuranceItemResponse insurance = insuranceService.getInsuranceDetail(id);
        model.addAttribute("insurance", insurance);
        model.addAttribute("criteria", criteria);
        return "insurance-detail";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            insuranceService.deleteInsurance(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/insurance";
    }

    @GetMapping("/create")
    public String createView(
            @RequestParam(required = false) Integer companyId,
            Model model
    ) {
        List<Company> companies = companyService.getAllCompanies();
        InsuranceFormRequest form = InsuranceFormRequest.builder()
                .companyType("EXIST")
                .companyId(companyId != null ? companyId : (!companies.isEmpty() ? companies.get(0).getCompanyInternalId() : null))
                .userSexDivision("01")
                .build();

        model.addAttribute("form", form);
        model.addAttribute("companies", companies);
        model.addAttribute("isEdit", false);
        return "insurance-form";
    }

    @PostMapping("/create")
    public String create(
            @ModelAttribute("form") InsuranceFormRequest form,
            Model model
    ) {
        try {
            insuranceService.createInsurance(form);
            return "redirect:/insurance";
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("companies", companyService.getAllCompanies());
            model.addAttribute("isEdit", false);
            return "insurance-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editView(
            @PathVariable Integer id,
            @ModelAttribute("criteria") InsuranceSearchCriteria criteria,
            Model model
    ) {
        InsuranceFormRequest form = insuranceService.getFormDtoForEdit(id);
        List<Company> companies = companyService.getAllCompanies();

        model.addAttribute("form", form);
        model.addAttribute("companies", companies);
        model.addAttribute("criteria", criteria);
        model.addAttribute("isEdit", true);
        return "insurance-form";
    }

    @PostMapping("/edit")
    public String edit(
            @ModelAttribute("form") InsuranceFormRequest form,
            Model model
    ) {
        try {
            insuranceService.updateInsurance(form);
            return "redirect:/insurance";
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("companies", companyService.getAllCompanies());
            model.addAttribute("isEdit", true);
            return "insurance-form";
        }
    }

    @GetMapping("/export-csv")
    public void exportCsv(
            @ModelAttribute InsuranceSearchCriteria criteria,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"Danh_sach_the_bao_hiem.csv\"");
        response.getWriter().write('\ufeff');
        insuranceService.exportCsv(criteria, response.getWriter());
    }
}
