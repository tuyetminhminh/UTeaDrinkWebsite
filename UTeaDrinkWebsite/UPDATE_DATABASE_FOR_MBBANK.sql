-- =====================================================
-- SQL Script: Thêm MBBANK vào Payment Method
-- Database: UTEDrink2
-- =====================================================

USE UTEDrink2;
GO

-- 1. Xóa CHECK constraint cũ
ALTER TABLE payment_transactions
DROP CONSTRAINT CK__payment_t__metho__5CA1C101;
GO

-- 2. Tạo CHECK constraint mới có MBBANK
ALTER TABLE payment_transactions
ADD CONSTRAINT CK__payment_t__metho__5CA1C101 
CHECK (method IN ('COD', 'MOMO', 'VNPAY', 'MBBANK'));
GO

-- 3. Kiểm tra constraint đã được tạo
SELECT 
    tc.CONSTRAINT_NAME,
    cc.CHECK_CLAUSE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc 
    ON tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME
WHERE tc.TABLE_NAME = 'payment_transactions'
    AND tc.CONSTRAINT_TYPE = 'CHECK';
GO

PRINT 'Database updated successfully! MBBANK payment method is now supported.';
GO

