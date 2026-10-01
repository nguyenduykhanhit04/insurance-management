package com.training.insurance.service.impl;

import com.training.insurance.dto.request.InsuranceFormRequest;
import com.training.insurance.dto.request.InsuranceSearchCriteria;
import com.training.insurance.dto.response.InsuranceItemResponse;
import com.training.insurance.entity.Company;
import com.training.insurance.entity.Insurance;
import com.training.insurance.entity.User;
import com.training.insurance.exception.AppException;
import com.training.insurance.repository.CompanyRepository;
import com.training.insurance.repository.InsuranceRepository;
import com.training.insurance.repository.UserRepository;
import com.training.insurance.service.InsuranceService;
import com.training.insurance.util.MD5Util;
import com.training.insurance.util.NameFormatter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements InsuranceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InsuranceRepository insuranceRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<InsuranceItemResponse> searchInsurances(InsuranceSearchCriteria criteria) {
        Specification<User> spec = createSearchSpecification(criteria);

        List<User> userList = new ArrayList<>(userRepository.findAll(spec));
        Comparator<User> comparator = (u1, u2) -> naturalCompare(u1.getUserFullName(), u2.getUserFullName());
        if ("DESC".equalsIgnoreCase(criteria.getOrder())) {
            comparator = comparator.reversed();
        }
        userList.sort(comparator);

        int page = Math.max(0, criteria.getPage());
        int size = criteria.getSize() > 0 ? criteria.getSize() : 5;
        int totalElements = userList.size();
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<InsuranceItemResponse> pageContent = userList.subList(fromIndex, toIndex).stream()
                .map(this::mapToItemResponse)
                .toList();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceItemResponse getInsuranceDetail(Integer insuranceId) {
        User user = userRepository.findByInsuranceInsuranceInternalId(insuranceId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin thẻ bảo hiểm!"));
        return mapToItemResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceFormRequest getFormDtoForEdit(Integer insuranceId) {
        User user = userRepository.findByInsuranceInsuranceInternalId(insuranceId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin thẻ bảo hiểm!"));
        Insurance ins = user.getInsurance();

        return InsuranceFormRequest.builder()
                .id(ins.getInsuranceInternalId())
                .userId(user.getUserInternalId())
                .companyType("EXIST")
                .companyId(user.getCompany().getCompanyInternalId())
                .username(user.getUsername())
                .password("")
                .userFullName(user.getUserFullName())
                .userSexDivision(user.getUserSexDivision())
                .birthdate(user.getBirthdate() != null ? user.getBirthdate().format(DATE_FORMATTER) : "")
                .insuranceNumber(ins.getInsuranceNumber())
                .startDate(ins.getInsuranceStartDate().format(DATE_FORMATTER))
                .endDate(ins.getInsuranceEndDate().format(DATE_FORMATTER))
                .placeOfRegister(ins.getPlaceOfRegister())
                .build();
    }

    @Override
    @Transactional
    public void createInsurance(InsuranceFormRequest form) {
        validateForm(form, true);

        Company company = resolveCompany(form);

        String formattedName = NameFormatter.formatName(form.getUserFullName());

        LocalDate birthDate = parseDate(form.getBirthdate(), "Ngày sinh");
        LocalDate startDate = parseDate(form.getStartDate(), "Ngày bắt đầu thẻ BH");
        LocalDate endDate = parseDate(form.getEndDate(), "Ngày kết thúc thẻ BH");

        Insurance insurance = Insurance.builder()
                .insuranceNumber(form.getInsuranceNumber().trim())
                .insuranceStartDate(startDate)
                .insuranceEndDate(endDate)
                .placeOfRegister(form.getPlaceOfRegister().trim())
                .build();
        insurance = insuranceRepository.save(insurance);

        String username = form.getUsername() != null && !form.getUsername().trim().isEmpty() 
                ? form.getUsername().trim() 
                : form.getInsuranceNumber().trim();
        String password = form.getPassword() != null && !form.getPassword().trim().isEmpty() 
                ? form.getPassword().trim() 
                : "123456";

        User user = User.builder()
                .company(company)
                .insurance(insurance)
                .username(username)
                .password(MD5Util.md5(password))
                .userFullName(formattedName)
                .userSexDivision(form.getUserSexDivision())
                .birthdate(birthDate)
                .build();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateInsurance(InsuranceFormRequest form) {
        validateForm(form, false);

        Insurance insurance = insuranceRepository.findById(form.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin thẻ bảo hiểm cần cập nhật!"));

        User user = userRepository.findByInsuranceInsuranceInternalId(insurance.getInsuranceInternalId())
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin người sử dụng!"));

        Company company = resolveCompany(form);

        LocalDate birthDate = parseDate(form.getBirthdate(), "Ngày sinh");
        LocalDate startDate = parseDate(form.getStartDate(), "Ngày bắt đầu thẻ BH");
        LocalDate endDate = parseDate(form.getEndDate(), "Ngày kết thúc thẻ BH");

        String formattedName = NameFormatter.formatName(form.getUserFullName());

        insurance.setInsuranceNumber(form.getInsuranceNumber().trim());
        insurance.setInsuranceStartDate(startDate);
        insurance.setInsuranceEndDate(endDate);
        insurance.setPlaceOfRegister(form.getPlaceOfRegister().trim());
        insuranceRepository.save(insurance);

        user.setCompany(company);
        if (form.getUsername() != null && !form.getUsername().trim().isEmpty()) {
            user.setUsername(form.getUsername().trim());
        }
        if (form.getPassword() != null && !form.getPassword().trim().isEmpty()) {
            user.setPassword(MD5Util.md5(form.getPassword().trim()));
        }
        user.setUserFullName(formattedName);
        user.setUserSexDivision(form.getUserSexDivision());
        user.setBirthdate(birthDate);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteInsurance(Integer insuranceId) {
        User user = userRepository.findByInsuranceInsuranceInternalId(insuranceId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
        insuranceRepository.deleteById(insuranceId);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportCsv(InsuranceSearchCriteria criteria, PrintWriter writer) {
        Company company = null;
        if (criteria.getCompanyId() != null) {
            company = companyRepository.findById(criteria.getCompanyId()).orElse(null);
        }

        Specification<User> spec = createSearchSpecification(criteria);
        List<User> userList = new ArrayList<>(userRepository.findAll(spec));
        Comparator<User> comparator = (u1, u2) -> naturalCompare(u1.getUserFullName(), u2.getUserFullName());
        if ("DESC".equalsIgnoreCase(criteria.getOrder())) {
            comparator = comparator.reversed();
        }
        userList.sort(comparator);

        writer.println("Danh sách thông tin thẻ bảo hiểm");
        writer.println();
        writer.println("Tên công ty," + (company != null ? escapeCsv(company.getCompanyName()) : ""));
        writer.println("Địa chỉ," + (company != null ? escapeCsv(company.getAddress()) : ""));
        writer.println("Email," + (company != null && company.getEmail() != null ? escapeCsv(company.getEmail()) : ""));
        writer.println("Số điện thoại," + (company != null && company.getTelephone() != null ? escapeCsv(company.getTelephone()) : ""));
        writer.println();

        writer.println("Họ và tên,Giới tính,Ngày sinh,Mã số thẻ bảo hiểm,Ngày bắt đầu,Ngày kết thúc,Nơi đăng ký KCB");

        for (User u : userList) {
            Insurance ins = u.getInsurance();
            String birthdateStr = u.getBirthdate() != null ? u.getBirthdate().format(DATE_FORMATTER) : "";
            String startStr = ins.getInsuranceStartDate() != null ? ins.getInsuranceStartDate().format(DATE_FORMATTER) : "";
            String endStr = ins.getInsuranceEndDate() != null ? ins.getInsuranceEndDate().format(DATE_FORMATTER) : "";

            writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    escapeCsv(u.getUserFullName()),
                    escapeCsv(u.getGenderText()),
                    birthdateStr,
                    escapeCsv(ins.getInsuranceNumber()),
                    startStr,
                    endStr,
                    escapeCsv(ins.getPlaceOfRegister())
            );
        }
        writer.flush();
    }

    private Specification<User> createSearchSpecification(InsuranceSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<User, Company> companyJoin = root.join("company");
            Join<User, Insurance> insuranceJoin = root.join("insurance");

            if (criteria.getCompanyId() != null) {
                predicates.add(cb.equal(companyJoin.get("companyInternalId"), criteria.getCompanyId()));
            }

            if (criteria.getUserFullName() != null && !criteria.getUserFullName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("userFullName")), "%" + criteria.getUserFullName().trim().toLowerCase() + "%"));
            }

            if (criteria.getInsuranceNumber() != null && !criteria.getInsuranceNumber().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(insuranceJoin.get("insuranceNumber")), "%" + criteria.getInsuranceNumber().trim().toLowerCase() + "%"));
            }

            if (criteria.getPlaceOfRegister() != null && !criteria.getPlaceOfRegister().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(insuranceJoin.get("placeOfRegister")), "%" + criteria.getPlaceOfRegister().trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private InsuranceItemResponse mapToItemResponse(User user) {
        Insurance ins = user.getInsurance();
        Company comp = user.getCompany();

        return InsuranceItemResponse.builder()
                .userId(user.getUserInternalId())
                .insuranceId(ins.getInsuranceInternalId())
                .companyId(comp != null ? comp.getCompanyInternalId() : null)
                .companyName(comp != null ? comp.getCompanyName() : "")
                .username(user.getUsername())
                .userFullName(user.getUserFullName())
                .userSexDivision(user.getUserSexDivision())
                .gender(user.getGenderText())
                .birthdate(user.getBirthdate())
                .insuranceNumber(ins.getInsuranceNumber())
                .startDate(ins.getInsuranceStartDate())
                .endDate(ins.getInsuranceEndDate())
                .placeOfRegister(ins.getPlaceOfRegister())
                .build();
    }

    private Company resolveCompany(InsuranceFormRequest form) {
        if ("NEW".equalsIgnoreCase(form.getCompanyType())) {
            if (form.getNewCompanyName() == null || form.getNewCompanyName().trim().isEmpty()) {
                throw new AppException("Hãy nhập Tên công ty!");
            }
            if (form.getNewAddress() == null || form.getNewAddress().trim().isEmpty()) {
                throw new AppException("Hãy nhập Địa chỉ!");
            }
            Company company = Company.builder()
                    .companyName(form.getNewCompanyName().trim())
                    .address(form.getNewAddress().trim())
                    .email(form.getNewEmail() != null ? form.getNewEmail().trim() : null)
                    .telephone(form.getNewTelephone() != null ? form.getNewTelephone().trim() : null)
                    .build();
            return companyRepository.save(company);
        } else {
            if (form.getCompanyId() == null) {
                throw new AppException("Hãy chọn Công ty!");
            }
            return companyRepository.findById(form.getCompanyId())
                    .orElseThrow(() -> new AppException("Công ty đã chọn không tồn tại!"));
        }
    }

    private void validateForm(InsuranceFormRequest form, boolean isCreate) {
        if (form.getUserFullName() == null || form.getUserFullName().trim().isEmpty()) {
            throw new AppException("Hãy nhập Họ và Tên!");
        }
        if (form.getUserSexDivision() == null || form.getUserSexDivision().trim().isEmpty()) {
            throw new AppException("Hãy chọn Giới tính!");
        }
        if (form.getInsuranceNumber() == null || form.getInsuranceNumber().trim().isEmpty()) {
            throw new AppException("Hãy nhập Mã số thẻ bảo hiểm!");
        }
        if (!form.getInsuranceNumber().trim().matches("^\\d{10}$")) {
            throw new AppException("Mã số thẻ bảo hiểm phải gồm 10 chữ số!");
        }
        if (form.getStartDate() == null || form.getStartDate().trim().isEmpty()) {
            throw new AppException("Hãy nhập Ngày bắt đầu thẻ BH!");
        }
        if (form.getEndDate() == null || form.getEndDate().trim().isEmpty()) {
            throw new AppException("Hãy nhập Ngày kết thúc thẻ BH!");
        }
        if (form.getPlaceOfRegister() == null || form.getPlaceOfRegister().trim().isEmpty()) {
            throw new AppException("Hãy nhập Nơi đăng ký KCB!");
        }

        String targetUsername = form.getUsername() != null && !form.getUsername().trim().isEmpty()
                ? form.getUsername().trim()
                : form.getInsuranceNumber().trim();
        if (isCreate) {
            if (userRepository.existsByUsername(targetUsername)) {
                throw new AppException("Tên đăng nhập đã tồn tại!");
            }
        } else if (form.getUserId() != null) {
            if (userRepository.existsByUsernameAndUserInternalIdNot(targetUsername, form.getUserId())) {
                throw new AppException("Tên đăng nhập đã tồn tại!");
            }
        }

        if (isCreate) {
            if (insuranceRepository.existsByInsuranceNumber(form.getInsuranceNumber().trim())) {
                throw new AppException("Đã tồn tại thông tin thẻ bảo hiểm!");
            }
        } else {
            if (insuranceRepository.existsByInsuranceNumberAndInsuranceInternalIdNot(form.getInsuranceNumber().trim(), form.getId())) {
                throw new AppException("Đã tồn tại thông tin thẻ bảo hiểm!");
            }
        }

        LocalDate start = parseDate(form.getStartDate(), "Ngày bắt đầu thẻ BH");
        LocalDate end = parseDate(form.getEndDate(), "Ngày kết thúc thẻ BH");
        if (end.isBefore(start)) {
            throw new AppException("Ngày kết thúc thẻ BH phải sau ngày bắt đầu!");
        }
    }

    private LocalDate parseDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new AppException("Lỗi nhập sai định dạng tại hạng mục " + fieldName + "!");
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private int naturalCompare(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;

        int i1 = 0, i2 = 0;
        while (i1 < s1.length() && i2 < s2.length()) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);

            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                int start1 = i1;
                while (i1 < s1.length() && Character.isDigit(s1.charAt(i1))) i1++;
                int start2 = i2;
                while (i2 < s2.length() && Character.isDigit(s2.charAt(i2))) i2++;

                long num1 = Long.parseLong(s1.substring(start1, i1));
                long num2 = Long.parseLong(s2.substring(start2, i2));
                if (num1 != num2) {
                    return Long.compare(num1, num2);
                }
            } else {
                int cmp = Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
                if (cmp != 0) {
                    return cmp;
                }
                i1++;
                i2++;
            }
        }
        return Integer.compare(s1.length(), s2.length());
    }
}
