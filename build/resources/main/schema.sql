CREATE TABLE IF NOT EXISTS tbl_company (
    company_internal_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(50) NOT NULL,
    address VARCHAR(100) NOT NULL,
    email VARCHAR(50),
    telephone VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS tbl_insurance (
    insurance_internal_id INT AUTO_INCREMENT PRIMARY KEY,
    insurance_number VARCHAR(10) NOT NULL UNIQUE,
    insurance_start_date DATE NOT NULL,
    insurance_end_date DATE NOT NULL,
    place_of_register VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS tbl_user (
    user_internal_id INT AUTO_INCREMENT PRIMARY KEY,
    company_internal_id INT NOT NULL,
    insurance_internal_id INT NOT NULL,
    username VARCHAR(15) NOT NULL UNIQUE,
    password VARCHAR(32) NOT NULL,
    user_full_name VARCHAR(50) NOT NULL,
    user_sex_division CHAR(2) NOT NULL,
    birthdate DATE,
    CONSTRAINT fk_user_company FOREIGN KEY (company_internal_id) REFERENCES tbl_company(company_internal_id),
    CONSTRAINT fk_user_insurance FOREIGN KEY (insurance_internal_id) REFERENCES tbl_insurance(insurance_internal_id)
);
