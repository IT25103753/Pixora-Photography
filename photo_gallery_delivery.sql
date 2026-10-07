-- PIXORA Photo Gallery & Delivery Management module
-- Extracted from the project database schema for IT25103768 module reference.
-- Requires existing: users and bookings tables.

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

CREATE INDEX IX_album_gallery ON gallery_albums(gallery_id,active);
