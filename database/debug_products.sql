-- Debug script to check product data
USE maoqiu_gsx;

-- Show all products
SELECT id, name, type, price, stock, image, status, create_time 
FROM b_product 
ORDER BY create_time DESC;

-- Check image field details
SELECT id, name, image, 
       LENGTH(image) as image_length,
       CASE WHEN image IS NULL OR image = '' THEN 'EMPTY' ELSE 'HAS_VALUE' END as image_status
FROM b_product;
