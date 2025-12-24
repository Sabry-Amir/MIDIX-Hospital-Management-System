
CREATE DATABASE hospital_db;
USE hospital_db;


CREATE TABLE users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) UNIQUE,
    [password] NVARCHAR(50),
    full_name NVARCHAR(100),
    role NVARCHAR(20),
    email NVARCHAR(100),
    department NVARCHAR(50)
);


CREATE TABLE patients (
    patient_id NVARCHAR(20) PRIMARY KEY, -- P001
    name NVARCHAR(100),
    age INT,
    gender NVARCHAR(10),
    phone NVARCHAR(20),
    blood_type NVARCHAR(5),
    status NVARCHAR(20),
    [address] NVARCHAR(MAX),
    emergency_contact NVARCHAR(100),
    emergency_phone NVARCHAR(20),
    allergies NVARCHAR(MAX),
    date_of_birth DATE
);

CREATE TABLE doctors (
    doctor_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT FOREIGN KEY REFERENCES users(user_id),
    name NVARCHAR(100),
    specialization NVARCHAR(50),
    phone NVARCHAR(20)
);
CREATE TABLE nurses (
    nurse_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT FOREIGN KEY REFERENCES users(user_id),
    name NVARCHAR(100),
    department NVARCHAR(50),
    phone NVARCHAR(20)
);


CREATE TABLE receptionists (
    receptionist_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT FOREIGN KEY REFERENCES users(user_id),
    name NVARCHAR(100),
    shift NVARCHAR(20),
    phone NVARCHAR(20)
);

CREATE TABLE appointments (
    appointment_id INT PRIMARY KEY IDENTITY(1,1),
    patient_id NVARCHAR(20) FOREIGN KEY REFERENCES patients(patient_id),
    doctor_id INT FOREIGN KEY REFERENCES doctors(doctor_id),
    appointment_date DATE,
    appointment_time TIME,
    reason NVARCHAR(MAX),
    status NVARCHAR(20),
    notes NVARCHAR(MAX)
);


CREATE TABLE medical_records (
    record_id INT PRIMARY KEY IDENTITY(1,1),
    patient_id NVARCHAR(20) FOREIGN KEY REFERENCES patients(patient_id),
    doctor_id INT FOREIGN KEY REFERENCES doctors(doctor_id),
    visit_date DATE,
    blood_pressure NVARCHAR(20),
    temperature_f DECIMAL(4,1),
    heart_rate_bpm INT,
    weight_lbs INT,
    diagnosis NVARCHAR(MAX),
    prescription NVARCHAR(MAX),
    treatment_plan NVARCHAR(MAX),
    notes NVARCHAR(MAX)
);


CREATE TABLE rooms (
    room_id INT PRIMARY KEY IDENTITY(1,1),
    room_number NVARCHAR(10) UNIQUE,
    room_type NVARCHAR(20),
    status NVARCHAR(20),
    floor_number INT,
    price_per_day DECIMAL(10,2),
    current_patient_id NVARCHAR(20)
);

CREATE TABLE billing (
    bill_id INT PRIMARY KEY IDENTITY(1,1),
    patient_id NVARCHAR(20) FOREIGN KEY REFERENCES patients(patient_id),
    total_amount DECIMAL(10,2),
    paid_amount DECIMAL(10,2),
    balance_due AS (total_amount - paid_amount), 
    [status] NVARCHAR(20),
    bill_date DATE
);

CREATE TABLE inventory (
    item_id NVARCHAR(20) PRIMARY KEY,
    item_name NVARCHAR(100),
    category NVARCHAR(50),
    quantity_in_stock INT,
    low_stock_threshold INT,
    unit NVARCHAR(20),
    price DECIMAL(10,2),
    expiry_date DATE,
    supplier NVARCHAR(100)
);


INSERT INTO users (username, [password], full_name, role, email, department) VALUES 
('admin', '123', 'John Administrator', 'Admin', 'admin@medix.com', 'Administration'),
('doctor1', '123', 'Dr. Sarah Johnson', 'Doctor', 'sarah@medix.com', 'Cardiology'),
('doctor2', '123', 'Dr. Michael Chen', 'Doctor', 'm.chen@medix.com', 'Pediatrics'),
('nurse1', '123', 'Emily Rose', 'Nurse', 'emily@medix.com', 'Emergency'),
('recep1', '123', 'Alice Smith', 'Receptionist', 'alice@medix.com', 'Front Desk');

INSERT INTO doctors (user_id, name, specialization, phone) VALUES 
(2, 'Dr. Sarah Johnson', 'Cardiologist', '01011111111'),
(3, 'Dr. Michael Chen', 'Pediatrician', '01022222222');


INSERT INTO patients (patient_id, name, age, gender, phone, blood_type, status, [address], emergency_contact, emergency_phone, allergies, date_of_birth) VALUES 
('P001', 'Robert Smith', 45, 'Male', '+1234567895', 'O+', 'admitted', '123 Main St, New York', 'Jane Smith', '01055555555', 'Peanuts', '1979-05-10'),
('P002', 'Maria Garcia', 32, 'Female', '+1234567897', 'A-', 'active', '456 Oak St, Cairo', 'Carlos Garcia', '01066666666', 'None', '1992-08-15'),
('P003', 'James Wilson', 50, 'Male', '+1234567899', 'B+', 'admitted', '789 Pine Rd, Alexandria', 'Sarah Wilson', '01077777777', 'Penicillin', '1974-12-01');


INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status, notes) VALUES 
('P002', 1, '2025-12-16', '09:00:00', 'Regular checkup', 'scheduled', 'Fasting required'),
('P003', 1, '2025-12-16', '10:30:00', 'Follow-up', 'confirmed', 'Bring previous reports'),
('P001', 1, '2025-12-17', '14:00:00', 'Cardiology consultation', 'scheduled', 'Check heart rate');


INSERT INTO rooms (room_number, room_type, status, floor_number, price_per_day, current_patient_id) VALUES 
('101', 'Private', 'occupied', 1, 500.00, 'P001'),
('102', 'General', 'occupied', 1, 200.00, 'P003'),
('103', 'ICU', 'available', 2, 1000.00, NULL),
('201', 'Emergency', 'available', 1, 300.00, NULL);

INSERT INTO billing (patient_id, total_amount, paid_amount, [status], bill_date) VALUES 
('P001', 1500.0, 1000.0, 'Partial', '2025-12-01'),
('P002', 500.0, 500.0, 'Paid', '2025-12-15');

INSERT INTO inventory (item_id, item_name, category, quantity_in_stock, low_stock_threshold, unit, price, expiry_date, supplier) VALUES 
('INV001', 'Paracetamol', 'medicine', 500, 100, 'tablets', 0.5, '2026-01-01', 'PharmaCorp'),
('INV004', 'Surgical Gloves', 'supplies', 80, 200, 'boxes', 12.0, NULL, 'HealthSupplies Co');

INSERT INTO medical_records (patient_id, doctor_id, visit_date, blood_pressure, temperature_f, heart_rate_bpm, weight_lbs, diagnosis, prescription, treatment_plan, notes) VALUES 
('P001', 1, '2025-12-01', '145/95', 98.6, 78, 185, 'Hypertension', 'Lisinopril 10mg', 'Lifestyle changes', 'Monitor BP daily'),
('P003', 2, '2025-12-10', '130/85', 98.4, 72, 195, 'Type 2 Diabetes', 'Metformin 500mg', 'Diet control', 'Exercise 30 mins'),
('P002', 1, '2025-12-20', '120/80', 98.2, 70, 150, 'Common Cold', 'Panadol', 'Rest for 3 days', 'Follow up if fever persists');

