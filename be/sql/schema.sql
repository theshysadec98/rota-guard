DROP TABLE IF EXISTS staff;

CREATE TABLE staff (
    ma_nv VARCHAR(20) PRIMARY KEY,
    ho_ten VARCHAR(255) NOT NULL,
    khoa VARCHAR(100) NOT NULL,
    he_so DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    vai_tro VARCHAR(20) NOT NULL DEFAULT 'NURSE',
    sdt VARCHAR(15),
    email VARCHAR(255),
    nam_kn INT NOT NULL DEFAULT 0
);

INSERT INTO staff (ma_nv, ho_ten, khoa, he_so, vai_tro, sdt, email, nam_kn) VALUES
    ('NV001', 'Nguyen Thi Lan', 'ICU', 1.0, 'NURSE', '0901000001', 'lan.nguyen@bv.vn', 5),
    ('NV002', 'Tran Van Minh', 'ICU', 1.2, 'DOCTOR', '0901000002', 'minh.tran@bv.vn', 12),
    ('NV003', 'Le Hoang Nam', 'ER', 1.0, 'NURSE', '0901000003', NULL, 2);
