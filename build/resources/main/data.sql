MERGE INTO tbl_company (company_internal_id, company_name, address, email, telephone) KEY (company_internal_id) VALUES
(1, 'Cty CP Phan Mem Luvina', '106, Hoàng Quốc Việt', 'info@luvina.net', '04123456'),
(2, 'FPT Software', 'Tòa nhà FPT Cầu Giấy, Hà Nội', 'contact@fsoft.com.vn', '02473007300'),
(3, 'CMC Telecom', 'CMC Tower, Duy Tân, Hà Nội', 'info@cmctelecom.vn', '02471090100');

MERGE INTO tbl_insurance (insurance_internal_id, insurance_number, insurance_start_date, insurance_end_date, place_of_register) KEY (insurance_internal_id) VALUES
(1, '0123456789', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(2, '0123456790', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(3, '0123456791', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(4, '0123456792', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(5, '0123456793', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(6, '0123456794', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(7, '0123456795', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(8, '0123456796', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(9, '0123456797', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(10, '0123456798', '2016-01-01', '2016-12-31', 'Bệnh Viện E'),
(11, '0123456799', '2016-01-01', '2016-12-31', 'Bệnh Viện E');

MERGE INTO tbl_user (user_internal_id, company_internal_id, insurance_internal_id, username, password, user_full_name, user_sex_division, birthdate) KEY (user_internal_id) VALUES
(1, 1, 1, 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Nguyen Van A1', '01', '1988-08-18'),
(2, 1, 2, 'user2', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A2', '01', '1989-08-18'),
(3, 1, 3, 'user3', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A3', '01', '1990-08-18'),
(4, 1, 4, 'user4', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A4', '01', '1991-08-18'),
(5, 1, 5, 'user5', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A5', '01', '1992-08-18'),
(6, 1, 6, 'user6', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A6', '01', '1993-08-18'),
(7, 1, 7, 'user7', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A7', '01', '1994-08-18'),
(8, 1, 8, 'user8', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A8', '01', '1995-08-18'),
(9, 1, 9, 'user9', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A9', '01', '1996-08-18'),
(10, 1, 10, 'user10', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A10', '01', '1997-08-18'),
(11, 1, 11, 'user11', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyen Van A11', '01', '1998-08-18');
