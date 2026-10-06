IF DB_ID(N'PIXORA_DB') IS NULL
BEGIN
    CREATE DATABASE PIXORA_DB;
END
GO

USE PIXORA_DB;
GO

SET NOCOUNT ON;
GO

-- Drop tables in reverse dependency order for repeatable university-demo setup.
IF OBJECT_ID('audit_logs','U') IS NOT NULL DROP TABLE audit_logs;
IF OBJECT_ID('notifications','U') IS NOT NULL DROP TABLE notifications;
IF OBJECT_ID('reviews','U') IS NOT NULL DROP TABLE reviews;
IF OBJECT_ID('complaint_actions','U') IS NOT NULL DROP TABLE complaint_actions;
IF OBJECT_ID('complaints','U') IS NOT NULL DROP TABLE complaints;
IF OBJECT_ID('refunds','U') IS NOT NULL DROP TABLE refunds;
IF OBJECT_ID('invoices','U') IS NOT NULL DROP TABLE invoices;
IF OBJECT_ID('payments','U') IS NOT NULL DROP TABLE payments;
IF OBJECT_ID('photo_favorites','U') IS NOT NULL DROP TABLE photo_favorites;
IF OBJECT_ID('photos','U') IS NOT NULL DROP TABLE photos;
IF OBJECT_ID('gallery_albums','U') IS NOT NULL DROP TABLE gallery_albums;
IF OBJECT_ID('galleries','U') IS NOT NULL DROP TABLE galleries;
IF OBJECT_ID('schedule_change_history','U') IS NOT NULL DROP TABLE schedule_change_history;
IF OBJECT_ID('photographer_assignments','U') IS NOT NULL DROP TABLE photographer_assignments;
IF OBJECT_ID('event_schedules','U') IS NOT NULL DROP TABLE event_schedules;
IF OBJECT_ID('booking_status_history','U') IS NOT NULL DROP TABLE booking_status_history;
IF OBJECT_ID('bookings','U') IS NOT NULL DROP TABLE bookings;
IF OBJECT_ID('availability_slots','U') IS NOT NULL DROP TABLE availability_slots;
IF OBJECT_ID('photography_packages','U') IS NOT NULL DROP TABLE photography_packages;
IF OBJECT_ID('portfolio_items','U') IS NOT NULL DROP TABLE portfolio_items;
IF OBJECT_ID('photographer_profiles','U') IS NOT NULL DROP TABLE photographer_profiles;
IF OBJECT_ID('users','U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('roles','U') IS NOT NULL DROP TABLE roles;
GO

CREATE TABLE roles (
    role_id INT IDENTITY(1,1) PRIMARY KEY,
    role_code VARCHAR(40) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL
);

CREATE TABLE users (
                       user_id INT IDENTITY(1,1) PRIMARY KEY,
                       role_id INT NOT NULL,
                       username VARCHAR(60) NOT NULL UNIQUE,
                       email VARCHAR(150) NOT NULL UNIQUE,

    -- NULL is allowed for Google-only accounts
                       password_hash VARCHAR(255) NULL,

                       full_name NVARCHAR(150) NOT NULL,
                       phone VARCHAR(25) NULL,

                       status VARCHAR(20) NOT NULL
                           CONSTRAINT DF_users_status DEFAULT 'ACTIVE',

                       auth_provider VARCHAR(20) NOT NULL
                           CONSTRAINT DF_users_auth_provider DEFAULT 'LOCAL',

                       google_sub VARCHAR(255) NULL,

                       created_at DATETIME2 NOT NULL
                           CONSTRAINT DF_users_created DEFAULT SYSDATETIME(),

                       updated_at DATETIME2 NOT NULL
                           CONSTRAINT DF_users_updated DEFAULT SYSDATETIME(),

                       CONSTRAINT FK_users_roles
                           FOREIGN KEY(role_id) REFERENCES roles(role_id),

                       CONSTRAINT CK_users_status
                           CHECK(status IN ('ACTIVE','SUSPENDED','INACTIVE')),

                       CONSTRAINT CK_users_auth_provider
                           CHECK(auth_provider IN ('LOCAL','GOOGLE','BOTH'))
);

CREATE UNIQUE INDEX UX_users_google_sub
    ON users(google_sub)
    WHERE google_sub IS NOT NULL;

CREATE TABLE photographer_profiles (
    profile_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    bio NVARCHAR(1200) NOT NULL CONSTRAINT DF_pp_bio DEFAULT '',
    specialty NVARCHAR(180) NOT NULL CONSTRAINT DF_pp_specialty DEFAULT '',
    location NVARCHAR(180) NOT NULL CONSTRAINT DF_pp_location DEFAULT '',
    approval_status VARCHAR(20) NOT NULL CONSTRAINT DF_pp_approval DEFAULT 'PENDING',
    average_rating DECIMAL(4,2) NOT NULL CONSTRAINT DF_pp_rating DEFAULT 0,
    profile_image_path NVARCHAR(400) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_pp_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_pp_updated DEFAULT SYSDATETIME(),
    CONSTRAINT FK_pp_user FOREIGN KEY(user_id) REFERENCES users(user_id),
    CONSTRAINT CK_pp_approval CHECK(approval_status IN ('PENDING','APPROVED','REJECTED')),
    CONSTRAINT CK_pp_rating CHECK(average_rating BETWEEN 0 AND 5)
);

CREATE TABLE portfolio_items (
    portfolio_id INT IDENTITY(1,1) PRIMARY KEY,
    photographer_user_id INT NOT NULL,
    title NVARCHAR(160) NOT NULL,
    description NVARCHAR(700) NULL,
    image_path NVARCHAR(400) NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_portfolio_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_portfolio_photographer FOREIGN KEY(photographer_user_id) REFERENCES users(user_id)
);

CREATE TABLE photography_packages (
    package_id INT IDENTITY(1,1) PRIMARY KEY,
    photographer_user_id INT NOT NULL,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(800) NULL,
    price DECIMAL(12,2) NOT NULL,
    duration_hours INT NOT NULL,
    active BIT NOT NULL CONSTRAINT DF_pkg_active DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_pkg_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_pkg_updated DEFAULT SYSDATETIME(),
    CONSTRAINT FK_pkg_photographer FOREIGN KEY(photographer_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_pkg_price CHECK(price >= 0),
    CONSTRAINT CK_pkg_duration CHECK(duration_hours > 0)
);

CREATE TABLE availability_slots (
    availability_id INT IDENTITY(1,1) PRIMARY KEY,
    photographer_user_id INT NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME(0) NOT NULL,
    end_time TIME(0) NOT NULL,
    active BIT NOT NULL CONSTRAINT DF_avail_active DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_avail_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_avail_photographer FOREIGN KEY(photographer_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_avail_time CHECK(end_time > start_time)
);

CREATE TABLE bookings (
    booking_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_ref VARCHAR(40) NOT NULL UNIQUE,
    customer_user_id INT NOT NULL,
    photographer_user_id INT NOT NULL,
    package_id INT NOT NULL,
    event_type NVARCHAR(120) NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME(0) NOT NULL,
    end_time TIME(0) NOT NULL,
    venue NVARCHAR(300) NOT NULL,
    notes NVARCHAR(1200) NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_booking_status DEFAULT 'PENDING',
    decision_reason NVARCHAR(500) NULL,
    cancellation_reason NVARCHAR(500) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_booking_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_booking_updated DEFAULT SYSDATETIME(),
    CONSTRAINT FK_booking_customer FOREIGN KEY(customer_user_id) REFERENCES users(user_id),
    CONSTRAINT FK_booking_photographer FOREIGN KEY(photographer_user_id) REFERENCES users(user_id),
    CONSTRAINT FK_booking_package FOREIGN KEY(package_id) REFERENCES photography_packages(package_id),
    CONSTRAINT CK_booking_status CHECK(status IN ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
    CONSTRAINT CK_booking_time CHECK(end_time > start_time),
    CONSTRAINT CK_booking_amount CHECK(total_amount >= 0)
);

CREATE TABLE booking_status_history (
    history_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    old_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_by_user_id INT NOT NULL,
    reason NVARCHAR(500) NULL,
    changed_at DATETIME2 NOT NULL CONSTRAINT DF_booking_hist_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_bhist_booking FOREIGN KEY(booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT FK_bhist_user FOREIGN KEY(changed_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE event_schedules (
    schedule_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME(0) NOT NULL,
    end_time TIME(0) NOT NULL,
    venue NVARCHAR(300) NOT NULL,
    timeline NVARCHAR(1500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_schedule_status DEFAULT 'ACTIVE',
    coordinator_user_id INT NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_schedule_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_schedule_updated DEFAULT SYSDATETIME(),
    CONSTRAINT FK_schedule_booking FOREIGN KEY(booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT FK_schedule_coordinator FOREIGN KEY(coordinator_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_schedule_status CHECK(status IN ('ACTIVE','CANCELLED','ARCHIVED')),
    CONSTRAINT CK_schedule_time CHECK(end_time > start_time)
);

CREATE TABLE photographer_assignments (
    assignment_id INT IDENTITY(1,1) PRIMARY KEY,
    schedule_id INT NOT NULL,
    photographer_user_id INT NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_assignment_status DEFAULT 'ACTIVE',
    assigned_at DATETIME2 NOT NULL CONSTRAINT DF_assignment_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_assignment_updated DEFAULT SYSDATETIME(),
    CONSTRAINT FK_assignment_schedule FOREIGN KEY(schedule_id) REFERENCES event_schedules(schedule_id),
    CONSTRAINT FK_assignment_photographer FOREIGN KEY(photographer_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_assignment_status CHECK(status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE schedule_change_history (
    change_id INT IDENTITY(1,1) PRIMARY KEY,
    schedule_id INT NOT NULL,
    changed_by_user_id INT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    reason NVARCHAR(800) NOT NULL,
    changed_at DATETIME2 NOT NULL CONSTRAINT DF_schedule_hist_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_schhist_schedule FOREIGN KEY(schedule_id) REFERENCES event_schedules(schedule_id),
    CONSTRAINT FK_schhist_user FOREIGN KEY(changed_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE galleries (
    gallery_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    photographer_user_id INT NOT NULL,
    customer_user_id INT NOT NULL,
    title NVARCHAR(180) NOT NULL,
    access_token VARCHAR(80) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL CONSTRAINT DF_gallery_status DEFAULT 'UPLOADING',
    archived BIT NOT NULL CONSTRAINT DF_gallery_archived DEFAULT 0,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_gallery_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_gallery_updated DEFAULT SYSDATETIME(),
    CONSTRAINT FK_gallery_booking FOREIGN KEY(booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT FK_gallery_photographer FOREIGN KEY(photographer_user_id) REFERENCES users(user_id),
    CONSTRAINT FK_gallery_customer FOREIGN KEY(customer_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_gallery_status CHECK(status IN ('UPLOADING','PROCESSING','READY_FOR_VIEWING','DELIVERED'))
);


CREATE TABLE gallery_albums (
    album_id INT IDENTITY(1,1) PRIMARY KEY,
    gallery_id INT NOT NULL,
    name NVARCHAR(160) NOT NULL,
    description NVARCHAR(600) NULL,
    active BIT NOT NULL CONSTRAINT DF_album_active DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_album_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_album_gallery FOREIGN KEY(gallery_id) REFERENCES galleries(gallery_id)
);

CREATE TABLE photos (
    photo_id INT IDENTITY(1,1) PRIMARY KEY,
    gallery_id INT NOT NULL,
    album_id INT NULL,
    caption NVARCHAR(300) NULL,
    file_path NVARCHAR(400) NOT NULL,
    original_name NVARCHAR(260) NULL,
    file_size BIGINT NOT NULL,
    deleted BIT NOT NULL CONSTRAINT DF_photo_deleted DEFAULT 0,
    uploaded_at DATETIME2 NOT NULL CONSTRAINT DF_photo_uploaded DEFAULT SYSDATETIME(),
    CONSTRAINT FK_photo_gallery FOREIGN KEY(gallery_id) REFERENCES galleries(gallery_id),
    CONSTRAINT FK_photo_album FOREIGN KEY(album_id) REFERENCES gallery_albums(album_id),
    CONSTRAINT CK_photo_size CHECK(file_size >= 0)
);

CREATE TABLE photo_favorites (
    favorite_id INT IDENTITY(1,1) PRIMARY KEY,
    photo_id INT NOT NULL,
    customer_user_id INT NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_fav_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_fav_photo FOREIGN KEY(photo_id) REFERENCES photos(photo_id),
    CONSTRAINT FK_fav_customer FOREIGN KEY(customer_user_id) REFERENCES users(user_id),
    CONSTRAINT UQ_fav UNIQUE(photo_id,customer_user_id)
);

CREATE TABLE payments (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    payment_ref VARCHAR(40) NOT NULL UNIQUE,
    booking_id INT NOT NULL,
    customer_user_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_payment_status DEFAULT 'PENDING',
    gateway_message NVARCHAR(300) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_payment_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_payment_booking FOREIGN KEY(booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT FK_payment_customer FOREIGN KEY(customer_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_payment_amount CHECK(amount > 0),
    CONSTRAINT CK_payment_status CHECK(status IN ('PENDING','PARTIALLY_PAID','PAID','REFUNDED','FAILED','VOID'))
);

CREATE TABLE invoices (
    invoice_id INT IDENTITY(1,1) PRIMARY KEY,
    invoice_ref VARCHAR(40) NOT NULL UNIQUE,
    booking_id INT NOT NULL,
    payment_id INT NOT NULL UNIQUE,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_invoice_status DEFAULT 'ISSUED',
    issued_at DATETIME2 NOT NULL CONSTRAINT DF_invoice_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_invoice_booking FOREIGN KEY(booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT FK_invoice_payment FOREIGN KEY(payment_id) REFERENCES payments(payment_id),
    CONSTRAINT CK_invoice_status CHECK(status IN ('ISSUED','VOID'))
);

CREATE TABLE refunds (
    refund_id INT IDENTITY(1,1) PRIMARY KEY,
    refund_ref VARCHAR(40) NOT NULL UNIQUE,
    payment_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    reason NVARCHAR(700) NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT DF_refund_status DEFAULT 'PENDING',
    processed_by_user_id INT NULL,
    staff_note NVARCHAR(600) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_refund_created DEFAULT SYSDATETIME(),
    processed_at DATETIME2 NULL,
    CONSTRAINT FK_refund_payment FOREIGN KEY(payment_id) REFERENCES payments(payment_id),
    CONSTRAINT FK_refund_staff FOREIGN KEY(processed_by_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_refund_amount CHECK(amount > 0),
    CONSTRAINT CK_refund_status CHECK(status IN ('PENDING','REFUNDED','REJECTED','FAILED'))
);

CREATE TABLE complaints (
    complaint_id INT IDENTITY(1,1) PRIMARY KEY,
    complaint_ref VARCHAR(40) NOT NULL UNIQUE,
    booking_id INT NOT NULL,
    customer_user_id INT NOT NULL,
    assigned_to_user_id INT NULL,
    category NVARCHAR(120) NOT NULL,
    description NVARCHAR(2000) NOT NULL,
    evidence_path NVARCHAR(400) NULL,
    priority VARCHAR(20) NOT NULL CONSTRAINT DF_complaint_priority DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL CONSTRAINT DF_complaint_status DEFAULT 'SUBMITTED',
    resolution NVARCHAR(1500) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_complaint_created DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL CONSTRAINT DF_complaint_updated DEFAULT SYSDATETIME(),
    CONSTRAINT FK_complaint_booking FOREIGN KEY(booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT FK_complaint_customer FOREIGN KEY(customer_user_id) REFERENCES users(user_id),
    CONSTRAINT FK_complaint_staff FOREIGN KEY(assigned_to_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_complaint_priority CHECK(priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    CONSTRAINT CK_complaint_status CHECK(status IN ('SUBMITTED','UNDER_REVIEW','IN_PROGRESS','RESOLVED','CLOSED'))
);

CREATE TABLE complaint_actions (
    action_id INT IDENTITY(1,1) PRIMARY KEY,
    complaint_id INT NOT NULL,
    actor_user_id INT NOT NULL,
    action_text NVARCHAR(1500) NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_caction_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_caction_complaint FOREIGN KEY(complaint_id) REFERENCES complaints(complaint_id),
    CONSTRAINT FK_caction_actor FOREIGN KEY(actor_user_id) REFERENCES users(user_id)
);

CREATE TABLE reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    customer_user_id INT NOT NULL,
    photographer_user_id INT NOT NULL,
    rating INT NOT NULL,
    comment NVARCHAR(1200) NOT NULL,
    visible BIT NOT NULL CONSTRAINT DF_review_visible DEFAULT 1,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_review_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_review_booking FOREIGN KEY(booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT FK_review_customer FOREIGN KEY(customer_user_id) REFERENCES users(user_id),
    CONSTRAINT FK_review_photographer FOREIGN KEY(photographer_user_id) REFERENCES users(user_id),
    CONSTRAINT CK_review_rating CHECK(rating BETWEEN 1 AND 5)
);

CREATE TABLE notifications (
    notification_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    title NVARCHAR(180) NOT NULL,
    message NVARCHAR(800) NOT NULL,
    link_url NVARCHAR(400) NULL,
    is_read BIT NOT NULL CONSTRAINT DF_notification_read DEFAULT 0,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_notification_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_notification_user FOREIGN KEY(user_id) REFERENCES users(user_id)
);

CREATE TABLE audit_logs (
    audit_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL,
    action_type VARCHAR(40) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id INT NULL,
    details NVARCHAR(1200) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_audit_created DEFAULT SYSDATETIME(),
    CONSTRAINT FK_audit_user FOREIGN KEY(user_id) REFERENCES users(user_id)
);
GO

CREATE INDEX IX_users_role_status ON users(role_id,status);
CREATE INDEX IX_availability_lookup ON availability_slots(photographer_user_id,available_date,start_time,end_time) INCLUDE(active);
CREATE INDEX IX_booking_photographer_date ON bookings(photographer_user_id,event_date,status,start_time,end_time);
CREATE INDEX IX_booking_customer ON bookings(customer_user_id,status,event_date);
CREATE INDEX IX_schedule_date ON event_schedules(event_date,status);
CREATE INDEX IX_assignment_photographer ON photographer_assignments(photographer_user_id,status,schedule_id);
CREATE INDEX IX_gallery_customer ON galleries(customer_user_id,archived,status);
CREATE INDEX IX_album_gallery ON gallery_albums(gallery_id,active);
CREATE INDEX IX_payment_booking ON payments(booking_id,status);
CREATE INDEX IX_complaint_status ON complaints(status,priority,assigned_to_user_id);
CREATE INDEX IX_notification_user ON notifications(user_id,is_read,created_at DESC);
GO

INSERT INTO roles(role_code,role_name) VALUES
('CUSTOMER','Customer / Event Organizer'),
('PHOTOGRAPHER','Professional Photographer'),
('EVENT_COORDINATOR','Event Coordinator'),
('OPERATIONS_MANAGER','Operations Manager'),
('CUSTOMER_RELATIONS','Customer Relations Officer'),
('SYSTEM_ADMIN','System Administrator');

INSERT INTO users(role_id,username,email,password_hash,full_name,phone,status) VALUES
((SELECT role_id FROM roles WHERE role_code='CUSTOMER'),'customer.demo','customer@pixora.local','PBKDF2$120000$vUhHZGXfMNizelHGy182Zw==$2PU9oZ7IS3wP9aOff9AEYHSSxiIu3MI0CluzXF2slEE=',N'Amaya Perera','+94 77 100 1001','ACTIVE'),
((SELECT role_id FROM roles WHERE role_code='PHOTOGRAPHER'),'photo.demo','photographer@pixora.local','PBKDF2$120000$zhCW9I4ADH0si4rklSJYNA==$L0Inj4o60/xBoAD8doOM29NvywbaYC6MHRX9MCqEaKY=',N'Ravindu Jayasinghe','+94 77 100 1002','ACTIVE'),
((SELECT role_id FROM roles WHERE role_code='EVENT_COORDINATOR'),'coord.demo','coordinator@pixora.local','PBKDF2$120000$wXweL7uL7cZnJzLyctxjzg==$3izHDEGx6BzP20bffpzAfEXKiC0I3Oporx0BQUOsuLc=',N'Kasun Wijesinghe','+94 77 100 1003','ACTIVE'),
((SELECT role_id FROM roles WHERE role_code='OPERATIONS_MANAGER'),'ops.demo','operations@pixora.local','PBKDF2$120000$h8TD7yKTQetfeZQdFOf2VA==$cl4PibVd+teR3utcHTOIp4eibrM0ibNKX5IjvHe5kr4=',N'Nadeesha Perera','+94 77 100 1004','ACTIVE'),
((SELECT role_id FROM roles WHERE role_code='CUSTOMER_RELATIONS'),'support.demo','support@pixora.local','PBKDF2$120000$XV3yCFiT/GyVy9SiL4Ag1A==$Azj+d+rmIfNtfXgYQ23dEUOA4bVTNc8kNeVVPsJsddE=',N'Shanika Fernando','+94 77 100 1005','ACTIVE'),
((SELECT role_id FROM roles WHERE role_code='SYSTEM_ADMIN'),'admin.demo','admin@pixora.local','PBKDF2$120000$L/UyyN8vrPUsIV+Wttnfbg==$He03rhSLH8ljBRk8OEKHMUGicz11yWz7WBNPcVym+6c=',N'Dilini Rathnayake','+94 77 100 1006','ACTIVE');

DECLARE @customer INT=(SELECT user_id FROM users WHERE username='customer.demo');
DECLARE @photographer INT=(SELECT user_id FROM users WHERE username='photo.demo');
DECLARE @coordinator INT=(SELECT user_id FROM users WHERE username='coord.demo');
DECLARE @support INT=(SELECT user_id FROM users WHERE username='support.demo');
DECLARE @admin INT=(SELECT user_id FROM users WHERE username='admin.demo');

INSERT INTO photographer_profiles(user_id,bio,specialty,location,approval_status,average_rating)
VALUES(@photographer,N'Event photographer focused on natural storytelling and carefully delivered client galleries.',
       N'Weddings & Events',N'Colombo','APPROVED',5.00);

INSERT INTO photography_packages(photographer_user_id,name,description,price,duration_hours,active) VALUES
(@photographer,N'Essential Event',N'Four-hour event coverage with curated digital delivery.',65000.00,4,1),
(@photographer,N'Full Story',N'Eight-hour full event coverage with extended gallery delivery.',120000.00,8,1);

INSERT INTO availability_slots(photographer_user_id,available_date,start_time,end_time,active) VALUES
(@photographer,DATEADD(DAY,10,CAST(GETDATE() AS date)),'08:00','22:00',1),
(@photographer,DATEADD(DAY,20,CAST(GETDATE() AS date)),'08:00','22:00',1);

DECLARE @pkg INT=(SELECT TOP 1 package_id FROM photography_packages WHERE photographer_user_id=@photographer ORDER BY price);
INSERT INTO bookings(booking_ref,customer_user_id,photographer_user_id,package_id,event_type,event_date,start_time,end_time,venue,notes,total_amount,status)
VALUES('BK-DEMO-CONFIRMED',@customer,@photographer,@pkg,N'Birthday',DATEADD(DAY,10,CAST(GETDATE() AS date)),'14:00','18:00',N'Colombo',N'Demo confirmed event',65000.00,'CONFIRMED'),
      ('BK-DEMO-COMPLETED',@customer,@photographer,@pkg,N'Corporate Event',DATEADD(DAY,-7,CAST(GETDATE() AS date)),'09:00','13:00',N'Colombo',N'Demo completed event',65000.00,'COMPLETED');

DECLARE @confirmed INT=(SELECT booking_id FROM bookings WHERE booking_ref='BK-DEMO-CONFIRMED');
DECLARE @completed INT=(SELECT booking_id FROM bookings WHERE booking_ref='BK-DEMO-COMPLETED');

INSERT INTO booking_status_history(booking_id,old_status,new_status,changed_by_user_id,reason) VALUES
(@confirmed,NULL,'PENDING',@customer,N'Demo booking created'),
(@confirmed,'PENDING','CONFIRMED',@photographer,N'Photographer accepted after availability check'),
(@completed,NULL,'PENDING',@customer,N'Demo booking created'),
(@completed,'PENDING','CONFIRMED',@photographer,N'Photographer accepted'),
(@completed,'CONFIRMED','COMPLETED',@coordinator,N'Event completed');

INSERT INTO event_schedules(booking_id,event_date,start_time,end_time,venue,timeline,status,coordinator_user_id)
VALUES(@confirmed,DATEADD(DAY,10,CAST(GETDATE() AS date)),'14:00','18:00',N'Colombo',N'13:30 arrival; 14:00 coverage begins; 18:00 wrap.','ACTIVE',@coordinator);
DECLARE @schedule INT=SCOPE_IDENTITY();
INSERT INTO photographer_assignments(schedule_id,photographer_user_id,status) VALUES(@schedule,@photographer,'ACTIVE');
INSERT INTO schedule_change_history(schedule_id,changed_by_user_id,action_type,reason) VALUES(@schedule,@coordinator,'CREATE',N'Demo event schedule created.');

INSERT INTO payments(payment_ref,booking_id,customer_user_id,amount,method,status,gateway_message)
VALUES('PAY-DEMO-001',@completed,@customer,65000.00,'CARD_DEMO','PAID','SUCCESS');
DECLARE @payment INT=SCOPE_IDENTITY();
INSERT INTO invoices(invoice_ref,booking_id,payment_id,total_amount,status)
VALUES('INV-DEMO-001',@completed,@payment,65000.00,'ISSUED');

INSERT INTO galleries(booking_id,photographer_user_id,customer_user_id,title,access_token,status,archived)
VALUES(@completed,@photographer,@customer,N'Corporate Event Highlights','demo-gallery-token','READY_FOR_VIEWING',0);

INSERT INTO reviews(booking_id,customer_user_id,photographer_user_id,rating,comment,visible)
VALUES(@completed,@customer,@photographer,5,N'Professional coverage and a smooth delivery experience.',1);

INSERT INTO complaints(complaint_ref,booking_id,customer_user_id,assigned_to_user_id,category,description,priority,status)
VALUES('CMP-DEMO-001',@completed,@customer,@support,N'Photo Delivery',N'Example resolved support record for the university demo.','LOW','RESOLVED');
DECLARE @complaint INT=SCOPE_IDENTITY();
UPDATE complaints SET resolution=N'Issue clarified and customer notified.',updated_at=SYSDATETIME() WHERE complaint_id=@complaint;
INSERT INTO complaint_actions(complaint_id,actor_user_id,action_text)
VALUES(@complaint,@support,N'Reviewed the gallery delivery record and confirmed the final status.');

INSERT INTO notifications(user_id,title,message,link_url,is_read) VALUES
(@customer,N'Welcome to PIXORA',N'Your demo customer account is ready.','/dashboard',0),
(@photographer,N'Photographer profile approved',N'Your demo profile is searchable by customers.','/photographer/manage',0),
(@coordinator,N'Upcoming event',N'A demo event is scheduled for coordination.','/schedule',0);

INSERT INTO audit_logs(user_id,action_type,entity_type,entity_id,details)
VALUES(@admin,'SEED','SYSTEM',NULL,N'Demo data loaded by pixora_database.sql');
GO

PRINT 'PIXORA_DB created successfully.';
PRINT 'Demo password for every seeded account: Pixora@123';
GO
