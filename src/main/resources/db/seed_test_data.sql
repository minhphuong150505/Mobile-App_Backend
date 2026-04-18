-- ============================================================================
-- seed_test_data.sql
-- Realistic camera equipment test data for camera_shop database
-- Safe to run multiple times (idempotent)
--
-- Usage:
--   mysql -u root -p camera_shop < seed_test_data.sql
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. CLEAR EXISTING TEST DATA (preserve users)
-- ============================================================================
DELETE FROM payment_transactions;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM rentals;
DELETE FROM cart_items;
DELETE FROM favorites;
DELETE FROM notifications;
DELETE FROM product_images;
DELETE FROM asset_images;
DELETE FROM products;
DELETE FROM assets;
DELETE FROM categories;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 2. INSERT CATEGORIES
-- ============================================================================

-- PRODUCT categories
INSERT INTO categories (category_id, category_name, type) VALUES
    ('c1', 'May anh cao cap',       'PRODUCT'),
    ('c2', 'Medium Format',          'PRODUCT'),
    ('c3', 'May anh Mirrorless',    'PRODUCT'),
    ('c4', 'May anh Compact',       'PRODUCT'),
    ('c5', 'Ong kinh',              'PRODUCT'),
    ('c6', 'Phu kien',              'PRODUCT');

-- ASSET categories
INSERT INTO categories (category_id, category_name, type) VALUES
    ('a1', 'Flycam',                'ASSET'),
    ('a2', 'Gimbal / On dinh',     'ASSET'),
    ('a3', 'Thiet bi am thanh',    'ASSET'),
    ('a4', 'Den chup anh',         'ASSET'),
    ('a5', 'Ong kinh cho thue',    'ASSET'),
    ('a6', 'Chan may anh',         'ASSET');

-- ============================================================================
-- 3. INSERT PRODUCTS (12 products with realistic Vietnamese names, VND prices)
-- ============================================================================

-- user_id references the testuser created by DataInitializer.
-- Since user IDs are UUID generated at runtime, we look them up dynamically.
SET @testuser_id = (SELECT user_id FROM users WHERE user_name = 'testuser' LIMIT 1);
SET @admin_id    = (SELECT user_id FROM users WHERE user_name = 'johndoe' LIMIT 1);

-- Fallback: if no users exist yet, we cannot proceed with product inserts
-- that require a valid user_id. The DataInitializer should have created them.
-- If running this script standalone before DataInitializer, insert a minimal user:
-- INSERT IGNORE INTO users (user_id, user_name, email, password, role, trust_score, email_verified, created_at)
-- VALUES ('seed-user-00000000-0000-0000-0000-000000000001', 'testuser', 'test@example.com',
--         '$2a$10$dummyhash', 'USER', 85, 1, NOW());

INSERT INTO products (product_id, category_id, user_id, product_name, brand, description, price, stock_quantity, created_at) VALUES
    -- c1: May anh cao cap (Premium Camera)
    ('p-00000001-0000-4000-8000-000000000001', 'c1', @testuser_id,
     'Leica M11', 'Leica',
     'May anh rangefinder dinh cao cua Leica voi sensor 60MP full-frame, ket hop thiet ke co dien va cong nghe hien dai. Phu hop cho nhung ai dam me nhiep anh phong canh va duong pho.',
     199000000, 5, NOW()),

    ('p-00000002-0000-4000-8000-000000000002', 'c1', @admin_id,
     'Leica Q3', 'Leica',
     'May anh compact full-frame 60MP voi ong kinh Summilux 28mm f/1.7. Tich hop hy sinh anh va quay video 8K.',
     125000000, 3, NOW()),

    -- c2: Medium Format
    ('p-00000003-0000-4000-8000-000000000003', 'c2', @testuser_id,
     'Hasselblad X2D 100C', 'Hasselblad',
     'May anh medium format 100 megapixel voi sensor trung format lon nhat thi truong. He thong mau tu nhien Hasselblad Color Science danh tieng the gioi.',
     175000000, 2, NOW()),

    ('p-00000004-0000-4000-8000-000000000004', 'c2', @admin_id,
     'Fujifilm GFX 100S', 'Fujifilm',
     'May anh medium format 102MP trong lo the nhat thi truong. Sensor IBIS tich hop va 16-bit RAW xu ly mau tuyet voi.',
     98000000, 4, NOW()),

    -- c3: May anh Mirrorless
    ('p-00000005-0000-4000-8000-000000000005', 'c3', @testuser_id,
     'Sony Alpha 1', 'Sony',
     'Flagship mirrorless full-frame 50.1MP, quay lien tuc 30fps, quay video 8K 30fps va 4K 120fps. May anh da nang nhat cua Sony.',
     145000000, 10, NOW()),

    ('p-00000006-0000-4000-8000-000000000006', 'c3', @testuser_id,
     'Canon EOS R3', 'Canon',
     'May anh mirrorless full-frame cho the thao va hoang da voi sensor BSI 24.1MP, 30fps, va he thong AF nhan dien mat Eye Control AF.',
     135000000, 4, NOW()),

    ('p-00000007-0000-4000-8000-000000000007', 'c3', @admin_id,
     'Nikon Z9', 'Nikon',
     'May anh mirrorless full-frame chuyen nghiep dau tien khong co bu chim co khi. Sensor stacked 45.7MP, 20fps RAW, 120fps JPEG.',
     125000000, 7, NOW()),

    ('p-00000008-0000-4000-8000-000000000008', 'c3', @testuser_id,
     'Fujifilm X-H2S', 'Fujifilm',
     'May anh mirrorless APS-C sensor stacked toi uu cho video va chup lien tuc. 40fps va quay video 6.2K. Phu hop cho nguoi lam noi dung.',
     58000000, 15, NOW()),

    -- c4: May anh Compact
    ('p-00000009-0000-4000-8000-000000000009', 'c4', @testuser_id,
     'Sony RX100 VII', 'Sony',
     'May anh compact nho gon voi sensor 1-inch 20.1MP, ong kinh 24-200mm, quay 4K, AF mat nguoi nhanh nhat phan khuc.',
     32000000, 20, NOW()),

    -- c5: Ong kinh (Lens)
    ('p-00000010-0000-4000-8000-000000000010', 'c5', @testuser_id,
     'Sony FE 24-70mm f/2.8 GM II', 'Sony',
     'Ong kinh zoom tieu chuyen nhat cua Sony voi Optical Design moi, nhe hon va nho hon phien ban I. 2 lens XD Linear cho AF nhanh.',
     52000000, 12, NOW()),

    ('p-00000011-0000-4000-8000-000000000011', 'c5', @admin_id,
     'Canon RF 70-200mm f/2.8L IS USM', 'Canon',
     'Ong kinh tele zoom chuyen nghiep cho he thong Canon EOS R. Nho gon, AF nhanh, va IS toi uu 5 stop.',
     68000000, 8, NOW()),

    -- c6: Phu kien (Accessories)
    ('p-00000012-0000-4000-8000-000000000012', 'c6', @testuser_id,
     'Peak Design Everyday Backpack V2', 'Peak Design',
     'Balo chua do anh giai thuong thiet ke, cho phep tuy chinh bo phan ben trong. Phu hop van chuyen may anh va do ca nhan hang ngay.',
     6500000, 30, NOW()),

    ('p-00000013-0000-4000-8000-000000000013', 'c6', @testuser_id,
     'ProGrade Digital CFexpress Type B 512GB', 'ProGrade Digital',
     'The nho toc do cao cho quay video 8K va chup anh lien tuc. Toc do doc 1700MB/s, toc do ghi 1200MB/s.',
     12500000, 25, NOW());

-- ============================================================================
-- 4. INSERT ASSETS (12 rental items with daily rates in VND)
-- ============================================================================

INSERT INTO assets (asset_id, category_id, user_id, model_name, brand, daily_rate, status, serial_number, created_at) VALUES
    -- a1: Flycam (Drone)
    ('a-00000001-0000-4000-8000-000000000001', 'a1', @testuser_id,
     'DJI Mavic 3 Pro', 'DJI', 1500000, 'AVAILABLE', 'M3P-00123',
     'Flycam 3 camera Hasselblad voi camera wide, tele, va zoom. Quay video 4K/120fps va anh 48MP.',
     NOW()),

    ('a-00000002-0000-4000-8000-000000000002', 'a1', @admin_id,
     'DJI Inspire 3', 'DJI', 5000000, 'AVAILABLE', 'INS3-00456',
     'Flycam chuyen nghiep cho dien anh voi sensor full-frame 8K, he thong FPV, va thoi gian bay len den 28 phut.',
     NOW()),

    -- a2: Gimbal / On dinh (Stabilizer)
    ('a-00000003-0000-4000-8000-000000000003', 'a2', @testuser_id,
     'DJI RS 3 Pro', 'DJI', 450000, 'AVAILABLE', 'RS3P-001',
     'Gimbal 3 truc cho may anh mirrorless nang den 4.5kg. Tich hop Ronin Image Transmitter va ActiveTrack 5.0.',
     NOW()),

    ('a-00000004-0000-4000-8000-000000000004', 'a2', @testuser_id,
     'Zhiyun Crane 3S', 'Zhiyun', 350000, 'AVAILABLE', 'ZY-C3S-88',
     'Gimbal chuyen nghiep cho DSLR va mirrorless nang den 6.5kg. Tich hop Sync Motion va vi tri module lam phim.',
     NOW()),

    -- a3: Thiet bi am thanh (Audio)
    ('a-00000005-0000-4000-8000-000000000005', 'a3', @testuser_id,
     'Sennheiser MKH 416', 'Sennheiser', 300000, 'RENTED', 'SN-416-09',
     'Micro shotgun chuyen nghiep danh cho lam phim va san xuat video. Mau tu nhien va kha nang loai bo tieng on vuot troi.',
     NOW()),

    ('a-00000006-0000-4000-8000-000000000006', 'a3', @admin_id,
     'Rode Wireless GO II', 'Rode', 150000, 'AVAILABLE', 'RD-WG2-11',
     'He thong micro khong day 2 kenh voi receiver tich hop, phu hop cho vlog, phong van va san xuat noi dung.',
     NOW()),

    -- a4: Den chup anh (Lighting)
    ('a-00000007-0000-4000-8000-000000000007', 'a4', @testuser_id,
     'Aputure LS 600d Pro', 'Aputure', 700000, 'AVAILABLE', 'AP-600D-PRO',
     'Den LED day du 600W voi xuat anh tuong duong 576W, CRI 96+, dieu khien tu xa, va thiet ke tan nhiet nhanh.',
     NOW()),

    ('a-00000008-0000-4000-8000-000000000008', 'a4', @admin_id,
     'Profoto B10X Plus', 'Profoto', 600000, 'RENTED', 'PR-B10XP',
     'Den flash doi tay voi xuat anh tuong duong 250W. Ho tro HSS va AirX dieu khien tu smartphone.',
     NOW()),

    -- a5: Ong kinh cho thue (Lens Rental)
    ('a-00000009-0000-4000-8000-000000000009', 'a5', @testuser_id,
     'Canon EOS R5', 'Canon', 800000, 'AVAILABLE', 'R5-001239',
     'May anh mirrorless full-frame 45MP voi quay video 8K RAW noi bo. Dual Card Slot va IBIS toi uu 8 stop.',
     NOW()),

    ('a-00000010-0000-4000-8000-000000000010', 'a5', @testuser_id,
     'Sony FE 24-70mm f/2.8 GM II', 'Sony', 400000, 'AVAILABLE', 'GM2-45211',
     'Ong kinh zoom tieu chuyen II moi hon, nhe hon va nho hon. Hinh anh sac net voi 2 lens XD Linear Motor.',
     NOW()),

    ('a-00000011-0000-4000-8000-000000000011', 'a5', @admin_id,
     'Canon RF 70-200mm f/2.8L IS USM', 'Canon', 500000, 'AVAILABLE', 'RF72-1200',
     'Ong kinh tele zoom f/2.8L cho he thong Canon EOS R. IS 5 stop, AF nhanh va thiet ke kieu nho gọn.',
     NOW()),

    -- a6: Chan may anh (Tripod)
    ('a-00000012-0000-4000-8000-000000000012', 'a6', @testuser_id,
     'Manfrotto 055 Carbon Fiber', 'Manfrotto', 250000, 'AVAILABLE', 'MF-055CF-01',
     'Chan may anh carbon fiber chuyen nghiep voi 90° cot giua, phu hop cho chup anh macro va san pham. Tai toi 8kg.',
     NOW()),

    ('a-00000013-0000-4000-8000-000000000013', 'a6', @admin_id,
     'Peak Design Travel Tripod', 'Peak Design', 200000, 'AVAILABLE', 'PD-TT-007',
     'Chan may anh du lich gap gon nhat thi truong, carbon fiber, cao 152cm nhung chi 1.27kg. Tich hop dien thoai mount.',
     NOW());

-- ============================================================================
-- 5. INSERT PRODUCT IMAGES (with real Unsplash URLs)
-- ============================================================================

INSERT INTO product_images (image_id, product_id, url, is_primary) VALUES
    -- Leica M11 (p-01) - 2 images
    ('pi-0001-0000-4000-8000-000000000001', 'p-00000001-0000-4000-8000-000000000001',
     'https://images.unsplash.com/photo-1516961642265-531546e84af2?w=800&q=80', true),
    ('pi-0002-0000-4000-8000-000000000002', 'p-00000001-0000-4000-8000-000000000001',
     'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800&q=80', false),

    -- Leica Q3 (p-02) - 2 images
    ('pi-0003-0000-4000-8000-000000000003', 'p-00000002-0000-4000-8000-000000000002',
     'https://images.unsplash.com/photo-1511140973288-19bf21d7e771?w=800&q=80', true),
    ('pi-0004-0000-4000-8000-000000000004', 'p-00000002-0000-4000-8000-000000000002',
     'https://images.unsplash.com/photo-1585548601784-e319505354bb?w=800&q=80', false),

    -- Hasselblad X2D (p-03) - 2 images
    ('pi-0005-0000-4000-8000-000000000005', 'p-00000003-0000-4000-8000-000000000003',
     'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80', true),
    ('pi-0006-0000-4000-8000-000000000006', 'p-00000003-0000-4000-8000-000000000003',
     'https://images.unsplash.com/photo-1493770348161-369560ae357d?w=800&q=80', false),

    -- Fujifilm GFX 100S (p-04) - 2 images
    ('pi-0007-0000-4000-8000-000000000007', 'p-00000004-0000-4000-8000-000000000004',
     'https://images.unsplash.com/photo-1617005082833-18e8093153e7?w=800&q=80', true),
    ('pi-0008-0000-4000-8000-000000000008', 'p-00000004-0000-4000-8000-000000000004',
     'https://images.unsplash.com/photo-1514316454349-750a7fd3da3a?w=800&q=80', false),

    -- Sony Alpha 1 (p-05) - 2 images
    ('pi-0009-0000-4000-8000-000000000009', 'p-00000005-0000-4000-8000-000000000005',
     'https://images.unsplash.com/photo-1617005090635-f55ff6e57c48?w=800&q=80', true),
    ('pi-0010-0000-4000-8000-000000000010', 'p-00000005-0000-4000-8000-000000000005',
     'https://images.unsplash.com/photo-1519183071298-a29601bc7c68?w=800&q=80', false),

    -- Canon EOS R3 (p-06) - 2 images
    ('pi-0011-0000-4000-8000-000000000011', 'p-00000006-0000-4000-8000-000000000006',
     'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800&q=80', true),
    ('pi-0012-0000-4000-8000-000000000012', 'p-00000006-0000-4000-8000-000000000006',
     'https://images.unsplash.com/photo-1452423924765-680fa2a9121a?w=800&q=80', false),

    -- Nikon Z9 (p-07) - 2 images
    ('pi-0013-0000-4000-8000-000000000013', 'p-00000007-0000-4000-8000-000000000007',
     'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80', true),
    ('pi-0014-0000-4000-8000-000000000014', 'p-00000007-0000-4000-8000-000000000007',
     'https://images.unsplash.com/photo-1542640244-7c41e23f5b22?w=800&q=80', false),

    -- Fujifilm X-H2S (p-08) - 2 images
    ('pi-0015-0000-4000-8000-000000000015', 'p-00000008-0000-4000-8000-000000000008',
     'https://images.unsplash.com/photo-1560064278-65127ee6aa25?w=800&q=80', true),
    ('pi-0016-0000-4000-8000-000000000016', 'p-00000008-0000-4000-8000-000000000008',
     'https://images.unsplash.com/photo-1526170375885-4d9baaa10b5f?w=800&q=80', false),

    -- Sony RX100 VII (p-09) - 2 images
    ('pi-0017-0000-4000-8000-000000000017', 'p-00000009-0000-4000-8000-000000000009',
     'https://images.unsplash.com/photo-1520390138845-fd2d229dd553?w=800&q=80', true),
    ('pi-0018-0000-4000-8000-000000000018', 'p-00000009-0000-4000-8000-000000000009',
     'https://images.unsplash.com/photo-1505798472440-462b8dc6e0e6?w=800&q=80', false),

    -- Sony FE 24-70mm GM II (p-10) - 2 images
    ('pi-0019-0000-4000-8000-000000000019', 'p-00000010-0000-4000-8000-000000000010',
     'https://images.unsplash.com/photo-1526170375885-4d9baaa10b5f?w=800&q=80', true),
    ('pi-0020-0000-4000-8000-000000000020', 'p-00000010-0000-4000-8000-000000000010',
     'https://images.unsplash.com/photo-1493770348161-369560ae357d?w=800&q=80', false),

    -- Canon RF 70-200mm (p-11) - 2 images
    ('pi-0021-0000-4000-8000-000000000021', 'p-00000011-0000-4000-8000-000000000011',
     'https://images.unsplash.com/photo-1452423924765-680fa2a9121a?w=800&q=80', true),
    ('pi-0022-0000-4000-8000-000000000022', 'p-00000011-0000-4000-8000-000000000011',
     'https://images.unsplash.com/photo-1514316454349-750a7fd3da3a?w=800&q=80', false),

    -- Peak Design Backpack (p-12) - 2 images
    ('pi-0023-0000-4000-8000-000000000023', 'p-00000012-0000-4000-8000-000000000012',
     'https://images.unsplash.com/photo-1553062407-98eeb25aa5e0?w=800&q=80', true),
    ('pi-0024-0000-4000-8000-000000000024', 'p-00000012-0000-4000-8000-000000000012',
     'https://images.unsplash.com/photo-1621347371649-0cab730e4b40?w=800&q=80', false),

    -- ProGrade CFexpress (p-13) - 1 image
    ('pi-0025-0000-4000-8000-000000000025', 'p-00000013-0000-4000-8000-000000000013',
     'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=800&q=80', true);

-- ============================================================================
-- 6. INSERT ASSET IMAGES (with real Unsplash URLs)
-- ============================================================================

INSERT INTO asset_images (image_id, asset_id, url, is_primary) VALUES
    -- DJI Mavic 3 Pro (a-01) - Drone
    ('ai-0001-0000-4000-8000-000000000001', 'a-00000001-0000-4000-8000-000000000001',
     'https://images.unsplash.com/photo-1478720568477-1527462524df?w=800&q=80', true),
    ('ai-0002-0000-4000-8000-000000000002', 'a-00000001-0000-4000-8000-000000000001',
     'https://images.unsplash.com/photo-1507582020474-9a35b712f674?w=800&q=80', false),

    -- DJI Inspire 3 (a-02) - Drone
    ('ai-0003-0000-4000-8000-000000000003', 'a-00000002-0000-4000-8000-000000000002',
     'https://images.unsplash.com/photo-1529870703678-2ca1cbd2cd85?w=800&q=80', true),
    ('ai-0004-0000-4000-8000-000000000004', 'a-00000002-0000-4000-8000-000000000002',
     'https://images.unsplash.com/photo-1478720568477-1527462524df?w=800&q=80', false),

    -- DJI RS 3 Pro Gimbal (a-03) - Stabilizer
    ('ai-0005-0000-4000-8000-000000000005', 'a-00000003-0000-4000-8000-000000000003',
     'https://images.unsplash.com/photo-1536514494165-59f5841b34e4?w=800&q=80', true),
    ('ai-0006-0000-4000-8000-000000000006', 'a-00000003-0000-4000-8000-000000000003',
     'https://images.unsplash.com/photo-1505775565620-4e0f2da4266a?w=800&q=80', false),

    -- Zhiyun Crane 3S (a-04) - Stabilizer
    ('ai-0007-0000-4000-8000-000000000007', 'a-00000004-0000-4000-8000-000000000004',
     'https://images.unsplash.com/photo-1505775565620-4e0f2da4266a?w=800&q=80', true),
    ('ai-0008-0000-4000-8000-000000000008', 'a-00000004-0000-4000-8000-000000000004',
     'https://images.unsplash.com/photo-1536514494165-59f5841b34e4?w=800&q=80', false),

    -- Sennheiser MKH 416 (a-05) - Audio
    ('ai-0009-0000-4000-8000-000000000009', 'a-00000005-0000-4000-8000-000000000005',
     'https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=800&q=80', true),
    ('ai-0010-0000-4000-8000-000000000010', 'a-00000005-0000-4000-8000-000000000005',
     'https://images.unsplash.com/photo-1484857695926-8d5e8b2a1391?w=800&q=80', false),

    -- Rode Wireless GO II (a-06) - Audio
    ('ai-0011-0000-4000-8000-000000000011', 'a-00000006-0000-4000-8000-000000000006',
     'https://images.unsplash.com/photo-1484857695926-8d5e8b2a1391?w=800&q=80', true),
    ('ai-0012-0000-4000-8000-000000000012', 'a-00000006-0000-4000-8000-000000000006',
     'https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=800&q=80', false),

    -- Aputure LS 600d Pro (a-07) - Lighting
    ('ai-0013-0000-4000-8000-000000000013', 'a-00000007-0000-4000-8000-000000000007',
     'https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=800&q=80', true),
    ('ai-0014-0000-4000-8000-000000000014', 'a-00000007-0000-4000-8000-000000000007',
     'https://images.unsplash.com/photo-1478720568477-1527462524df?w=800&q=80', false),

    -- Profoto B10X Plus (a-08) - Lighting
    ('ai-0015-0000-4000-8000-000000000015', 'a-00000008-0000-4000-8000-000000000008',
     'https://images.unsplash.com/photo-1478720568477-1527462524df?w=800&q=80', true),
    ('ai-0016-0000-4000-8000-000000000016', 'a-00000008-0000-4000-8000-000000000008',
     'https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=800&q=80', false),

    -- Canon EOS R5 (a-09) - Camera body rental
    ('ai-0017-0000-4000-8000-000000000017', 'a-00000009-0000-4000-8000-000000000009',
     'https://images.unsplash.com/photo-1516961642265-531546e84af2?w=800&q=80', true),
    ('ai-0018-0000-4000-8000-000000000018', 'a-00000009-0000-4000-8000-000000000009',
     'https://images.unsplash.com/photo-1519183071298-a29601bc7c68?w=800&q=80', false),

    -- Sony FE 24-70mm GM II (a-10) - Lens rental
    ('ai-0019-0000-4000-8000-000000000019', 'a-00000010-0000-4000-8000-000000000010',
     'https://images.unsplash.com/photo-1526170375885-4d9baaa10b5f?w=800&q=80', true),
    ('ai-0020-0000-4000-8000-000000000020', 'a-00000010-0000-4000-8000-000000000010',
     'https://images.unsplash.com/photo-1493770348161-369560ae357d?w=800&q=80', false),

    -- Canon RF 70-200mm (a-11) - Lens rental
    ('ai-0021-0000-4000-8000-000000000021', 'a-00000011-0000-4000-8000-000000000011',
     'https://images.unsplash.com/photo-1452423924765-680fa2a9121a?w=800&q=80', true),
    ('ai-0022-0000-4000-8000-000000000022', 'a-00000011-0000-4000-8000-000000000011',
     'https://images.unsplash.com/photo-1514316454349-750a7fd3da3a?w=800&q=80', false),

    -- Manfrotto 055 Carbon Fiber (a-12) - Tripod
    ('ai-0023-0000-4000-8000-000000000023', 'a-00000012-0000-4000-8000-000000000012',
     'https://images.unsplash.com/photo-1505775565620-4e0f2da4266a?w=800&q=80', true),
    ('ai-0024-0000-4000-8000-000000000024', 'a-00000012-0000-4000-8000-000000000012',
     'https://images.unsplash.com/photo-1536514494165-59f5841b34e4?w=800&q=80', false),

    -- Peak Design Travel Tripod (a-13) - Tripod
    ('ai-0025-0000-4000-8000-000000000025', 'a-00000013-0000-4000-8000-000000000013',
     'https://images.unsplash.com/photo-1536514494165-59f5841b34e4?w=800&q=80', true),
    ('ai-0026-0000-4000-8000-000000000026', 'a-00000013-0000-4000-8000-000000000013',
     'https://images.unsplash.com/photo-1505775565620-4e0f2da4266a?w=800&q=80', false);

-- ============================================================================
-- DONE
-- ============================================================================